package com.soinsoftware.vissa.web;

import java.io.IOException;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.time.DateUtils;
import org.apache.log4j.Logger;
import org.jsoup.helper.StringUtil;

import com.soinsoftware.vissa.bll.CompanyBll;
import com.soinsoftware.vissa.bll.DocumentBll;
import com.soinsoftware.vissa.bll.DocumentTypeBll;
import com.soinsoftware.vissa.common.CommonsConstants;
import com.soinsoftware.vissa.common.EComparatorType;
import com.soinsoftware.vissa.model.Company;
import com.soinsoftware.vissa.model.Document;
import com.soinsoftware.vissa.model.DocumentType;
import com.soinsoftware.vissa.model.EPaymentStatus;
import com.soinsoftware.vissa.model.ETransactionType;
import com.soinsoftware.vissa.model.User;
import com.soinsoftware.vissa.util.Commons;
import com.soinsoftware.vissa.util.DateUtil;
import com.soinsoftware.vissa.util.PermissionUtil;
import com.soinsoftware.vissa.util.StringUtility;
import com.soinsoftware.vissa.util.ViewHelper;
import com.vaadin.data.provider.ListDataProvider;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.server.FontAwesome;
import com.vaadin.ui.Button;
import com.vaadin.ui.FormLayout;
import com.vaadin.ui.Grid;
import com.vaadin.ui.Grid.SelectionMode;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Panel;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ValoTheme;

@SuppressWarnings("deprecation")
public class NotificationLayout extends VerticalLayout implements View {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1009491925177013905L;
	protected static final Logger log = Logger.getLogger(NotificationLayout.class);

	// Bll
	private final CompanyBll companyBll;
	private final DocumentBll documentBll;
	private final DocumentTypeBll documentTypeBll;

	// Components
	private TextField txtNit;
	private TextField txtName;
	private TextField txtInvoiceResolution;
	private TextField txtRegimeType;
	private TextField txtAddress;
	private TextField txtPhone;
	private TextField txtMobile;
	private TextField txtEmail;
	private TextField txtWebsite;
	private Grid<Document> grid;

	// Entities
	private Company company;
	private PermissionUtil permissionUtil;
	private User user;

	private ListDataProvider<Document> docuemntDataProvider;

	public NotificationLayout() throws IOException {
		super();
		companyBll = CompanyBll.getInstance();
		documentBll = DocumentBll.getInstance();
		documentTypeBll = DocumentTypeBll.getInstance();
		setCaption("Notificaciones");

	}

	@Override
	public void enter(ViewChangeEvent event) {

		VerticalLayout layout = ViewHelper.buildVerticalLayout(false, false);

		this.user = getSession().getAttribute(User.class);
		this.permissionUtil = new PermissionUtil(user.getRole().getPermissions());

		HorizontalLayout hl1 = ViewHelper.buildHorizontalLayout(false, false);
		// Facturas de compra próximas a vencer
		Panel documentPanel1 = buildDocumentGridPanel(ETransactionType.ENTRADA);

		// Facturas de venta próximas a vencer
		Panel documentPanel2 = buildDocumentGridPanel(ETransactionType.SALIDA);
		hl1.addComponents(documentPanel1);
		hl1.addComponents(documentPanel2);

		HorizontalLayout hl2 = ViewHelper.buildHorizontalLayout(false, false);
		Panel panel = buildDocumentGridPanel(ETransactionType.ENTRADA);
		Panel panel2 = buildDocumentGridPanel(ETransactionType.ENTRADA);
		hl2.addComponents(panel);
		hl2.addComponents(panel2);

		layout.addComponents(hl1, hl2);

		addComponent(layout);
		this.setMargin(false);
		this.setSpacing(false);

	}

	@SuppressWarnings("unchecked")
	protected Panel buildDocumentGridPanel(ETransactionType transactionType) {
		String strLog = "[buildDocumentGridPanel] ";
		VerticalLayout layout = ViewHelper.buildVerticalLayout(true, true);
		try {
			grid = ViewHelper.buildGrid(SelectionMode.SINGLE);

			grid.addColumn(Document::getCode).setCaption("Número");
			grid.addColumn(document -> {
				if (document.getDocumentType() != null) {
					return document.getDocumentType().getName();
				} else {
					return "";
				}
			}).setCaption("Tipo");

			grid.addColumn(document -> {
				if (document.getDocumentDate() != null) {
					return DateUtil.dateToString(document.getDocumentDate());
				} else {
					return "";
				}
			}).setCaption("Fecha");

			grid.addColumn(document -> {
				if (document.getPerson() != null) {
					return StringUtility.concatName(document.getPerson().getName(), document.getPerson().getLastName());
				} else {
					return null;
				}
			}).setCaption("Proveedor");

			grid.addColumn(Document::getTotalValue).setCaption("Total");

			grid.addColumn(document -> {
				Double payValue = document.getPayValue() != null ? document.getPayValue() : document.getTotalValue();
				Double balance = document.getTotalValue() - payValue;
				return (Math.round(balance));
			}).setCaption("Saldo");

			grid.setHeight("200px");
			layout.addComponent(grid);

			fillDocumentGridData(transactionType);

		} catch (Exception e) {
			log.error(strLog + "[Exception]" + e.getMessage());
			e.printStackTrace();
		}
		Panel panel = ViewHelper.buildPanel("Facturas próximas a vencer", layout);
		panel.setWidth("50%");
		panel.setHeight("50%");
		return panel;
	}

	protected void fillDocumentGridData(ETransactionType transactionType) {
		String strLog = "[fillDocumentGridData] ";
		try {

			List<Document> documents = null;
			if (transactionType.equals(ETransactionType.ENTRADA)) {
				documents = getSupplierPayments(transactionType);
			}
			if (transactionType.equals(ETransactionType.SALIDA)) {
				documents = getSupplierPayments(transactionType);
			}
			documents = documents.stream().sorted(Comparator.comparing(Document::getExpirationDate))
					.collect(Collectors.toList());
			docuemntDataProvider = new ListDataProvider<>(documents);

			grid.setDataProvider(docuemntDataProvider);

		} catch (

		Exception e) {
			log.error(strLog + "[Exception] " + e.getMessage());
			e.printStackTrace();
		}
	}

	private List<Document> getSupplierPayments(ETransactionType transactiontype) {
		String strLog = "[getSupplierPayments] ";
		List<Document> documents = null;
		try {
			int days = CommonsConstants.PAYMENT_PENDING_DAYS;
			Date expirationDateTmp = DateUtils.addDays(new Date(), days);
			Date expirationDate = DateUtils.addHours(expirationDateTmp, 23);
			expirationDate = DateUtils.addMinutes(expirationDate, 59);
			expirationDate = DateUtils.addSeconds(expirationDate, 59);

			List<DocumentType> types = documentTypeBll.select(transactiontype);
			documents = documentBll.selectByExpirationDate(types, expirationDate, EPaymentStatus.PENDING.getName(),
					EComparatorType.LE);

		} catch (Exception e) {
			log.error(strLog + "[Exception] " + e.getMessage());
			e.printStackTrace();
		}
		return documents;
	}

}
