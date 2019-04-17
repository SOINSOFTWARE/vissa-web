package com.soinsoftware.vissa.web;

import java.io.IOException;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.time.DateUtils;
import org.apache.log4j.Logger;

import com.soinsoftware.vissa.bll.DocumentBll;
import com.soinsoftware.vissa.bll.DocumentTypeBll;
import com.soinsoftware.vissa.common.CommonsConstants;
import com.soinsoftware.vissa.common.EComparatorType;
import com.soinsoftware.vissa.model.Document;
import com.soinsoftware.vissa.model.DocumentType;
import com.soinsoftware.vissa.model.EPaymentStatus;
import com.soinsoftware.vissa.model.ETransactionType;
import com.soinsoftware.vissa.util.Commons;
import com.soinsoftware.vissa.util.DateUtil;
import com.soinsoftware.vissa.util.StringUtility;
import com.soinsoftware.vissa.util.ViewHelper;
import com.vaadin.data.provider.ListDataProvider;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.shared.ui.grid.ColumnResizeMode;
import com.vaadin.ui.Grid;
import com.vaadin.ui.Grid.SelectionMode;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ValoTheme;

public class NotificationLayout extends VerticalLayout implements View {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1009491925177013905L;
	protected static final Logger log = Logger.getLogger(NotificationLayout.class);

	// Bll
	private final DocumentBll documentBll;
	private final DocumentTypeBll documentTypeBll;

	// Components
	private Grid<Document> purchasesGrid;
	private Grid<Document> salesGrid;

	// Entities

	private ListDataProvider<Document> purchasesDataProvider;
	private ListDataProvider<Document> salesDataProvider;

	public NotificationLayout() throws IOException {
		super();

		documentBll = DocumentBll.getInstance();
		documentTypeBll = DocumentTypeBll.getInstance();

	}

	@Override
	public void enter(ViewChangeEvent event) {

		Label tittle = new Label("Notificaciones");
		tittle.addStyleName(ValoTheme.LABEL_H3);
		addComponent(tittle);
		VerticalLayout layout = ViewHelper.buildVerticalLayout(false, false);

		HorizontalLayout hl1 = ViewHelper.buildHorizontalLayout(true, false);
		// Facturas de compra próximas a vencer
		Panel panel1 = builPurchasesGrid(ETransactionType.ENTRADA);
		// Facturas de venta próximas a vencer
		Panel panel2 = buildSalesGridPanel(ETransactionType.SALIDA);
		hl1.addComponents(panel1);
		hl1.addComponents(panel2);

		/*
		 * HorizontalLayout hl2 = ViewHelper.buildHorizontalLayout(false, false); Panel
		 * panel3 = builPurchasesGrid(ETransactionType.ENTRADA); Panel panel4 =
		 * builPurchasesGrid(ETransactionType.ENTRADA); hl2.addComponents(panel3);
		 * hl2.addComponents(panel4);
		 */

		layout.addComponents(hl1);

		addComponent(layout);
		this.setMargin(false);
		this.setSpacing(false);

	}

	@SuppressWarnings("unchecked")
	protected Panel builPurchasesGrid(ETransactionType transactionType) {
		String strLog = "[buildDocumentGridPanel] ";
		VerticalLayout layout = ViewHelper.buildVerticalLayout(false, false);
		try {
			purchasesGrid = ViewHelper.buildGrid(SelectionMode.SINGLE);

			purchasesGrid.addColumn(Document::getCode).setCaption("N°");

			purchasesGrid.addColumn(document -> {
				if (document.getPerson() != null) {
					return StringUtility.concatName(document.getPerson().getName(), document.getPerson().getLastName());
				} else {
					return null;
				}
			}).setCaption("Proveedor");

			purchasesGrid.addColumn(document -> {
				if (document.getExpirationDate() != null) {
					return DateUtil.dateToString(document.getExpirationDate(), Commons.FORMAT_DATE);
				} else {
					return "";
				}
			}).setCaption("Vencimiento");

			purchasesGrid.addColumn(Document::getTotalValue).setCaption("Total");

			purchasesGrid.addColumn(document -> {
				Double payValue = document.getPayValue() != null ? document.getPayValue() :  0.0;
				Double balance = document.getTotalValue() - payValue;
				return (Math.round(balance));
			}).setCaption("Saldo").setResizable(true);

			purchasesGrid.setHeight("200px");
			purchasesGrid.setColumnResizeMode(ColumnResizeMode.ANIMATED);
			purchasesGrid.addStyleName(ValoTheme.TABLE_COMPACT);		

			layout.addComponent(purchasesGrid);

			fillDocumentGridData(transactionType);

		} catch (Exception e) {
			log.error(strLog + "[Exception]" + e.getMessage());
			e.printStackTrace();
		}
		Panel panel = ViewHelper.buildPanel("Facturas de compra próximas a vencer", layout);
		panel.setWidth("50%");
		panel.setHeight("50%");
		return panel;
	}

	@SuppressWarnings("unchecked")
	protected Panel buildSalesGridPanel(ETransactionType transactionType) {
		String strLog = "[salesGrid] ";
		VerticalLayout layout = ViewHelper.buildVerticalLayout(false, false);
		try {
			salesGrid = ViewHelper.buildGrid(SelectionMode.SINGLE);

			salesGrid.addColumn(Document::getCode).setCaption("N°");

			salesGrid.addColumn(document -> {
				if (document.getPerson() != null) {
					return StringUtility.concatName(document.getPerson().getName(), document.getPerson().getLastName());
				} else {
					return null;
				}
			}).setCaption("Cliente");

			salesGrid.addColumn(document -> {
				if (document.getExpirationDate() != null) {
					return DateUtil.dateToString(document.getExpirationDate(), Commons.FORMAT_DATE);
				} else {
					return "";
				}
			}).setCaption("Vencimiento");

			salesGrid.addColumn(Document::getTotalValue).setCaption("Total");

			salesGrid.addColumn(document -> {
				Double payValue = document.getPayValue() != null ? document.getPayValue() : 0.0;
				Double balance = document.getTotalValue() - payValue;
				return (Math.round(balance));
			}).setCaption("Saldo").setResizable(true);

			salesGrid.setHeight("200px");
			// salesGrid.setStyleName("v-grid-cell");
			salesGrid.setColumnResizeMode(ColumnResizeMode.ANIMATED);
			salesGrid.addStyleName(ValoTheme.TABLE_SMALL);

			layout.addComponent(salesGrid);

			fillDocumentGridData(transactionType);

		} catch (Exception e) {
			log.error(strLog + "[Exception]" + e.getMessage());
			e.printStackTrace();
		}
		Panel panel = ViewHelper.buildPanel("Facturas de ventas próximas a vencer", layout);
		panel.setWidth("50%");
		panel.setHeight("50%");
		return panel;
	}

	protected void fillDocumentGridData(ETransactionType transactionType) {
		String strLog = "[fillDocumentGridData] ";
		try {

			List<Document> documents = null;

			documents = getPendingPayments(transactionType);

			documents = documents.stream().sorted(Comparator.comparing(Document::getExpirationDate))
					.collect(Collectors.toList());
			if (transactionType.equals(ETransactionType.ENTRADA)) {
				purchasesDataProvider = new ListDataProvider<>(documents);
				purchasesGrid.setDataProvider(purchasesDataProvider);
			}
			if (transactionType.equals(ETransactionType.SALIDA)) {
				salesDataProvider = new ListDataProvider<>(documents);
				salesGrid.setDataProvider(salesDataProvider);
			}

		} catch (

		Exception e) {
			log.error(strLog + "[Exception] " + e.getMessage());
			e.printStackTrace();
		}
	}

	private List<Document> getPendingPayments(ETransactionType transactiontype) {
		String strLog = "[getPendingPayments] ";
		List<Document> documents = null;
		try {
			int days = CommonsConstants.PAYMENT_PENDING_DAYS;
			Date expirationDateTmp = DateUtils.addDays(new Date(), days);
			expirationDateTmp = DateUtils.truncate(expirationDateTmp, Calendar.DATE);
			Date expirationDate = DateUtil.endDate(expirationDateTmp);

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
