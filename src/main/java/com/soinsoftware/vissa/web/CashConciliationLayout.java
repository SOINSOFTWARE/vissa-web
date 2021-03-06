package com.soinsoftware.vissa.web;

import static com.soinsoftware.vissa.web.VissaUI.KEY_SALESMAN_CONCILIATION;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.log4j.Logger;
import org.vaadin.ui.NumberField;

import com.soinsoftware.vissa.bll.CashRegisterConciliationBll;
import com.soinsoftware.vissa.bll.CollectionBll;
import com.soinsoftware.vissa.bll.DocumentBll;
import com.soinsoftware.vissa.bll.DocumentTypeBll;
import com.soinsoftware.vissa.bll.EgressBll;
import com.soinsoftware.vissa.bll.EgressTypeBll;
import com.soinsoftware.vissa.bll.PaymentTypeBll;
import com.soinsoftware.vissa.bll.UserBll;
import com.soinsoftware.vissa.model.CashConciliation;
import com.soinsoftware.vissa.model.Collection;
import com.soinsoftware.vissa.model.Document;
import com.soinsoftware.vissa.model.DocumentType;
import com.soinsoftware.vissa.model.EEgressType;
import com.soinsoftware.vissa.model.EPaymemtType;
import com.soinsoftware.vissa.model.ERole;
import com.soinsoftware.vissa.model.ETransactionType;
import com.soinsoftware.vissa.model.Egress;
import com.soinsoftware.vissa.model.EgressType;
import com.soinsoftware.vissa.model.PaymentType;
import com.soinsoftware.vissa.model.Person;
import com.soinsoftware.vissa.model.PersonType;
import com.soinsoftware.vissa.model.Product;
import com.soinsoftware.vissa.model.User;
import com.soinsoftware.vissa.util.Commons;
import com.soinsoftware.vissa.util.DateUtil;
import com.soinsoftware.vissa.util.NumericUtil;
import com.soinsoftware.vissa.util.ViewHelper;
import com.vaadin.data.provider.ConfigurableFilterDataProvider;
import com.vaadin.data.provider.ListDataProvider;
import com.vaadin.data.provider.Query;
import com.vaadin.server.FontAwesome;
import com.vaadin.server.SerializablePredicate;
import com.vaadin.ui.AbstractOrderedLayout;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.DateField;
import com.vaadin.ui.FormLayout;
import com.vaadin.ui.Grid;
import com.vaadin.ui.Grid.SelectionMode;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Panel;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.themes.ValoTheme;

@SuppressWarnings({ "unchecked", "deprecation" })
public class CashConciliationLayout extends AbstractEditableLayout<CashConciliation> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5076502522106126046L;

	protected static final Logger log = Logger.getLogger(CashConciliationLayout.class);

	private final CashRegisterConciliationBll conciliationBll;
	private final DocumentBll documentBll;
	private final DocumentTypeBll documentTypeBll;
	private final PaymentTypeBll paymentTypeBll;
	private final EgressBll egressBll;
	private final EgressTypeBll egressTypeBll;
	private final CollectionBll collectionBll;

	private final UserBll userBll;

	private Grid<CashConciliation> concilicationGrid;
	private Grid<Document> documentGrid;

	private TextField txFilterByName;
	private TextField txFilterByCode;

	private NumberField txtCashBase;
	private TextField txtPerson;
	private DateField dfConciliationDate;
	private NumberField txtSales;
	private NumberField txtCreditCollection;
	private NumberField txtRemnantSale;
	private NumberField txtGeneralExpense;
	private NumberField txtSupplierPaymentsLoan;
	private NumberField txtRemnantEgress;
	private NumberField txtTotalIngress;
	private NumberField txtTotalCash;
	private NumberField txtTotalEgress;
	private NumberField txtTotalCredit;
	private NumberField txtSupplierPayments;
	private NumberField txtCashRegisterBorrow;
	private NumberField txtBalance;

	private Window personSubwindow;
	private PersonLayout personLayout = null;
	private Person selectedPerson = null;

	private User user;
	private String loginRole;
	private String employeeRole;
	private VerticalLayout detailLayout;
	private boolean autoSaved = false;
	Date conciliationDate;
	Date iniDateFilter;
	Date endDateFilter;

	private ListDataProvider<Document> documentDataProvider;
	private ListDataProvider<Collection> collectionDataProvider;
	private ListDataProvider<Egress> expenseDataProvider;
	private ListDataProvider<Egress> loanDataProvider;
	private ConfigurableFilterDataProvider<CashConciliation, Void, SerializablePredicate<CashConciliation>> filterProductDataProvider;

	public CashConciliationLayout() throws IOException {
		super("Cuadre de caja", KEY_SALESMAN_CONCILIATION);
		conciliationBll = CashRegisterConciliationBll.getInstance();
		documentBll = DocumentBll.getInstance();
		documentTypeBll = DocumentTypeBll.getInstance();
		paymentTypeBll = PaymentTypeBll.getInstance();
		egressBll = EgressBll.getInstance();
		egressTypeBll = EgressTypeBll.getInstance();
		collectionBll = CollectionBll.getInstance();

		userBll = UserBll.getInstance();

	}

	@Override
	protected AbstractOrderedLayout buildListView() {

		this.user = getSession().getAttribute(User.class);
		this.loginRole = user.getRole().getName();

		employeeRole = Commons.ROLE;

		VerticalLayout layout = ViewHelper.buildVerticalLayout(false, false);
		Panel buttonPanel = buildButtonPanelForLists();
		// Panel filterPanel = buildFilterPanel();
		Panel dataPanel = buildGridPanel();
		layout.addComponents(buttonPanel, dataPanel);
		this.setMargin(false);
		this.setSpacing(false);
		return layout;
	}

	@Override
	protected AbstractOrderedLayout buildEditionView(CashConciliation entity) {
		VerticalLayout layout = ViewHelper.buildVerticalLayout(false, false);
		Panel buttonPanel = buildButtonPanelForEdition(entity);
		Component dataPanel = buildEditionComponent(entity);
		layout.addComponents(buttonPanel, dataPanel);
		return layout;
	}

	@Override
	protected Panel buildGridPanel() {
		VerticalLayout layout = ViewHelper.buildVerticalLayout(true, true);
		concilicationGrid = ViewHelper.buildGrid(SelectionMode.SINGLE);
		concilicationGrid.addColumn(cashRegisterConciliation -> {
			return DateUtil.dateToString(cashRegisterConciliation.getConciliationDate(), Commons.FORMAT_DATE);
		}).setCaption("Fecha");

		if (loginRole.equals(ERole.SUDO.getName()) || loginRole.equals(ERole.MANAGER.getName())) {
			concilicationGrid.addColumn(cashRegisterConciliation -> {
				if (cashRegisterConciliation.getPerson() != null) {
					return cashRegisterConciliation.getPerson().getName() + " "
							+ cashRegisterConciliation.getPerson().getLastName();
				} else {
					return "";
				}
			}).setCaption("Empleado");

		}
		concilicationGrid.addColumn(CashConciliation::getCashBase).setCaption("Base");
		if (Commons.ROLE.equals(ERole.SALESMAN.getName())) {
			concilicationGrid.addColumn(CashConciliation::getTotalIngress).setCaption("Total ventas");
			concilicationGrid.addColumn(CashConciliation::getTotalEgress).setCaption("Total egresos");
			concilicationGrid.addColumn(CashConciliation::getTotalCredit).setCaption("Créditos");
			concilicationGrid.addColumn(CashConciliation::getTotalCash).setCaption("Efectivo neto");
		} else if (Commons.ROLE.equals(ERole.ADMINISTRATOR.getName())) {
			concilicationGrid.addColumn(CashConciliation::getSupplierPayments).setCaption("Pago proveedores");
			concilicationGrid.addColumn(CashConciliation::getCashRegisterBorrow).setCaption("Prestamo caja");
			concilicationGrid.addColumn(CashConciliation::getBalance).setCaption("A devolver");
		}

		layout.addComponent(ViewHelper.buildPanel(null, concilicationGrid));
		fillGridData();
		return ViewHelper.buildPanel(null, layout);
	}

	protected Panel buildDocumentGrid() {
		VerticalLayout layout = ViewHelper.buildVerticalLayout(true, true);
		documentGrid = ViewHelper.buildGrid(SelectionMode.SINGLE);
		documentGrid.addColumn(Document::getCode).setCaption("Número factura");
		documentGrid.addColumn(Document::getReference).setCaption("Referencia proveedor");
		documentGrid.addColumn(document -> {
			if (document.getPerson() != null) {
				return document.getPerson().getName() + " " + document.getPerson().getLastName();
			} else {
				return "";
			}
		}).setCaption("Proveedor");
		documentGrid.addColumn(Document::getTotalValue).setCaption("Total factura");

		layout.addComponent(ViewHelper.buildPanel(null, documentGrid));
		getDailyPurchaseData();
		fillDocumentGridData();
		return ViewHelper.buildPanel(null, layout);
	}

	@Override
	protected Component buildEditionComponent(CashConciliation concilitation) {

		VerticalLayout layout = ViewHelper.buildVerticalLayout(false, false);
		FormLayout basicForm = ViewHelper.buildForm("", false, false);

		dfConciliationDate = new DateField("Fecha");
		dfConciliationDate.setStyleName(ValoTheme.DATEFIELD_TINY);
		dfConciliationDate.setDateFormat(Commons.FORMAT_DATE);
		dfConciliationDate.setValue(LocalDate.now());
		dfConciliationDate.setWidth("50%");
		dfConciliationDate.setRequiredIndicatorVisible(true);

		txtCashBase = new NumberField("Base caja");
		txtCashBase.setStyleName(ValoTheme.TEXTFIELD_TINY);
		txtCashBase.setWidth("50%");

		if (loginRole.equals(ERole.SUDO.getName()) || loginRole.equals(ERole.MANAGER.getName())) {
			txtCashBase.setReadOnly(false);
			txtCashBase.focus();
			txtPerson = new TextField();
			txtPerson.setRequiredIndicatorVisible(true);
			Button searchPersonBtn = new Button("Buscar proveedor", FontAwesome.SEARCH);
			searchPersonBtn.addClickListener(e -> buildPersonWindow(txtPerson.getValue()));
			searchPersonBtn.setStyleName("icon-only");
			HorizontalLayout personLayout = ViewHelper.buildHorizontalLayout(false, false);
			Label label = new Label("Empleado");
			// label.setStyleName(ValoTheme.LABEL_H1);
			personLayout.addComponents(label, txtPerson, searchPersonBtn);
			personLayout.setComponentAlignment(searchPersonBtn, Alignment.BOTTOM_CENTER);
			basicForm.addComponent(personLayout);
		} else {
			txtPerson = new TextField("Empleado");
			txtCashBase.setReadOnly(true);
			txtPerson.setReadOnly(true);
			basicForm.addComponent(txtPerson);
			txtPerson.setValue(user.getPerson().getName() + " " + user.getPerson().getLastName());
			selectedPerson = user.getPerson();

		}

		txtPerson.setWidth("50%");

		basicForm.addComponent(dfConciliationDate);

		basicForm.addComponent(txtCashBase);

		txtPerson.setStyleName(ValoTheme.TEXTFIELD_TINY);

		Panel basicPanel = ViewHelper.buildPanel("", basicForm);
		layout.addComponents(basicPanel);

		setFieldValues(concilitation);

		dfConciliationDate.addValueChangeListener(e -> {
			setFieldValues(concilitation);
			VerticalLayout detailLayout2 = (VerticalLayout) buildDetailLayout(concilitation);
			if (detailLayout2 != null) {
				layout.removeComponent(detailLayout);

				layout.addComponent(detailLayout2);
				detailLayout = detailLayout2;
			}
		});

		// -------------------------------------------------------------------------
		detailLayout = (VerticalLayout) buildDetailLayout(concilitation);
		if (detailLayout != null) {
			layout.addComponent(detailLayout);
		}

		// ----------------------------------------------------------------------------------

		return layout;
	}

	/*
	 * 
	 * Construir campos de acuerdo al perfil del empleado
	 */
	private Component buildDetailLayout(CashConciliation concilitation) {
		VerticalLayout detailLayout = null;
		if (employeeRole != null) {
			// Panel de acuerdo al rol del empleado
			if (employeeRole.equals(ERole.SALESMAN.getName())) {
				detailLayout = (VerticalLayout) buildSalesmanConcilitationPanel(concilitation);
			} else if (employeeRole.equals(ERole.ADMINISTRATOR.getName())) {
				detailLayout = (VerticalLayout) buildAdministratorConcilitationPanel(concilitation);
			}

		}

		return detailLayout;
	}

	private Component buildSalesmanConcilitationPanel(CashConciliation conciliation) {
		VerticalLayout layout = ViewHelper.buildVerticalLayout(false, false);
		txtSales = new NumberField("Ventas");

		txtSales.setStyleName(ValoTheme.TEXTFIELD_TINY);
		txtSales.setReadOnly(true);
		txtSales.setWidth("50%");
		txtSales.setDecimalSeparator(',');
		txtSales.setValue(getSumPaidDailySales());

		txtCreditCollection = new NumberField("Recaudo créditos");
		txtCreditCollection.setStyleName(ValoTheme.TEXTFIELD_TINY);
		txtCreditCollection.setWidth("50%");
		txtCreditCollection.setDecimalSeparator(',');
		txtCreditCollection.setValue(getSumDailyCollection());
		txtCreditCollection.addValueChangeListener(e -> setTotalSale());

		txtRemnantSale = new NumberField("Sobrante");
		txtRemnantSale.setStyleName(ValoTheme.TEXTFIELD_TINY);
		txtRemnantSale.setWidth("50%");
		txtRemnantSale.setDecimalSeparator(',');
		txtRemnantSale.addValueChangeListener(e -> setTotalSale());

		txtTotalIngress = new NumberField("Total Ventas");
		txtTotalIngress.setStyleName(ValoTheme.TEXTFIELD_TINY);
		txtTotalIngress.setWidth("50%");
		txtTotalIngress.setDecimalSeparator(',');
		txtTotalIngress.setReadOnly(true);

		FormLayout saleForm = ViewHelper.buildForm("", false, false);
		saleForm.addComponents(txtSales, txtCreditCollection, txtRemnantSale, txtTotalIngress);
		Panel salePanel = ViewHelper.buildPanel("VENTAS", saleForm);
		layout.addComponents(salePanel);

		// -------------------------------------------------------------------------
		txtGeneralExpense = new NumberField("Gastos generales");
		txtGeneralExpense.setStyleName(ValoTheme.TEXTFIELD_TINY);
		txtGeneralExpense.setWidth("50%");
		txtGeneralExpense.setDecimalSeparator(',');
		txtGeneralExpense.setValue(getSumDailyExpense());
		txtGeneralExpense.addValueChangeListener(e -> setTotalEgress());

		txtSupplierPaymentsLoan = new NumberField("Pago a proveedores");
		txtSupplierPaymentsLoan.setStyleName(ValoTheme.TEXTFIELD_TINY);
		txtSupplierPaymentsLoan.setWidth("50%");
		txtSupplierPaymentsLoan.setDecimalSeparator(',');
		txtSupplierPaymentsLoan.setValue(getSumDailyLoan());
		txtSupplierPaymentsLoan.addValueChangeListener(e -> setTotalEgress());

		txtRemnantEgress = new NumberField("Remanente");
		txtRemnantEgress.setStyleName(ValoTheme.TEXTFIELD_TINY);
		txtRemnantEgress.setWidth("50%");
		txtRemnantEgress.setDecimalSeparator(',');

		txtTotalEgress = new NumberField("Total egresos");
		txtTotalEgress.setStyleName(ValoTheme.TEXTFIELD_TINY);
		txtTotalEgress.setWidth("50%");
		txtTotalEgress.setDecimalSeparator(',');
		txtTotalEgress.setReadOnly(true);

		FormLayout egressForm = ViewHelper.buildForm("", true, false);
		egressForm.addComponents(txtGeneralExpense, txtSupplierPaymentsLoan, txtRemnantEgress, txtTotalEgress);
		Panel egressPanel = ViewHelper.buildPanel("EGRESOS", egressForm);
		layout.addComponents(egressPanel);

		// -------------------------------------------------------------------------
		txtTotalCredit = new NumberField("Créditos");
		txtTotalCredit.setStyleName(ValoTheme.TEXTFIELD_TINY);
		txtTotalCredit.setWidth("50%");
		txtTotalCredit.setDecimalSeparator(',');
		txtTotalCredit.setValue(getSumDailyCreditSales());

		txtTotalCash = new NumberField("Efectivo neto");
		txtTotalCash.setStyleName(ValoTheme.TEXTFIELD_TINY);
		txtTotalCash.setWidth("50%");
		txtTotalCash.setDecimalSeparator(',');
		txtTotalCash.setReadOnly(true);

		FormLayout totalForm = ViewHelper.buildForm("", false, false);
		totalForm.addComponents(txtTotalCredit, txtTotalCash);
		Panel totalPanel = ViewHelper.buildPanel("TOTALES", totalForm);
		layout.addComponents(totalPanel);

		setSalesmanFieldValues(conciliation);
		return layout;
	}

	private Component buildAdministratorConcilitationPanel(CashConciliation conciliation) {
		VerticalLayout layout = ViewHelper.buildVerticalLayout(false, false);
		txtSupplierPayments = new NumberField("Pago proveedores");
		txtSupplierPayments.setStyleName(ValoTheme.TEXTFIELD_TINY);
		txtSupplierPayments.setRequiredIndicatorVisible(true);
		txtSupplierPayments.setReadOnly(true);
		txtSupplierPayments.setWidth("50%");
		txtSupplierPayments.setValue(getSumDailyPurchases());
		txtSupplierPayments.addValueChangeListener(e -> setBalanceAdministrador());

		txtCashRegisterBorrow = new NumberField("Prestamo caja");
		txtCashRegisterBorrow.setStyleName(ValoTheme.TEXTFIELD_TINY);
		txtCashRegisterBorrow.setWidth("50%");
		txtCashRegisterBorrow.setValue(getSumDailyLoan());
		txtCashRegisterBorrow.addValueChangeListener(e -> setBalanceAdministrador());
		txtCashRegisterBorrow.setReadOnly(true);

		txtBalance = new NumberField("Saldo");
		txtBalance.setStyleName(ValoTheme.TEXTFIELD_TINY);
		txtBalance.setReadOnly(true);
		txtBalance.setWidth("50%");

		FormLayout payemntForm = ViewHelper.buildForm("", false, false);
		payemntForm.addComponents(txtSupplierPayments, txtCashRegisterBorrow, txtBalance);
		Panel egressPanel = ViewHelper.buildPanel("", payemntForm);
		Panel documentGrid = buildDocumentGrid();
		layout.addComponents(egressPanel, documentGrid);

		setAdminFieldValues(conciliation);

		return layout;
	}

	private void setFieldValues(CashConciliation concil) {
		conciliationDate = DateUtil.localDateToDate(dfConciliationDate.getValue());
		iniDateFilter = DateUtil.iniDate(conciliationDate);
		endDateFilter = DateUtil.endDate(conciliationDate);
		if (concil != null) {
			selectedPerson = concil.getPerson();
			txtPerson.setValue(concil.getPerson().getName() + " " + concil.getPerson().getLastName());
			dfConciliationDate.setValue(DateUtil.dateToLocalDate(concil.getConciliationDate()));
			txtCashBase.setValue(String.valueOf(concil.getCashBase()));

		}

	}

	private void setSalesmanFieldValues(CashConciliation concil) {

		if (concil != null) {
			selectedPerson = concil.getPerson();
			txtPerson.setValue(concil.getPerson().getName() + " " + concil.getPerson().getLastName());
			dfConciliationDate.setValue(DateUtil.dateToLocalDate(concil.getConciliationDate()));
			txtCashBase.setValue(String.valueOf(concil.getCashBase()));
			txtSales.setValue(String.valueOf(concil.getSales()));
			txtCreditCollection.setValue(String.valueOf(concil.getCreditCollection()));
			txtRemnantSale.setValue(String.valueOf(concil.getRemnantSale()));
			txtTotalIngress.setValue((String.valueOf(concil.getTotalIngress())));
			txtGeneralExpense.setValue(String.valueOf(concil.getGeneralExpense()));
			txtRemnantEgress.setValue(String.valueOf(concil.getRemnantEgress()));
			txtSupplierPaymentsLoan.setValue(String.valueOf(concil.getSupplierPaymentsLoan()));
			txtTotalEgress.setValue(String.valueOf(concil.getTotalEgress()));
			txtTotalCredit.setValue(String.valueOf(concil.getTotalCredit()));
			txtTotalCash.setValue(String.valueOf(concil.getTotalCash()));
		} else {
			txtCashBase.setValue(Commons.PARAM_SALESMAN_CASH_BASE);
		}
		setTotalSale();
		setTotalEgress();

	}

	private void setAdminFieldValues(CashConciliation concil) {
		if (concil != null) {
			txtSupplierPayments.setValue(
					concil.getSupplierPayments() != null ? String.valueOf(concil.getSupplierPayments()) : "0.0");
			txtCashRegisterBorrow.setValue(
					concil.getCashRegisterBorrow() != null ? String.valueOf(concil.getCashRegisterBorrow()) : "0.0");
			txtBalance.setValue(concil.getBalance() != null ? String.valueOf(concil.getBalance()) : "0.0");
		} else {
			txtCashBase.setValue(0.0);
			// txtSupplierPayments.setValue(txtSupplierPayments);
			// txtCashRegisterBorrow.setValue(0.0);
			// txtBalance.setValue(0.0);
		}
		setBalanceAdministrador();
	}

	private void setTotalSale() {
		String strLog = "[setTotalSale]";
		try {
			log.info(strLog + "txtSales string:" + txtSales.getValue());
			log.info(strLog + "txtSales double:" + txtSales.getDoubleValueDoNotThrow());
			log.info(strLog + "txtSales:" + BigDecimal.valueOf(txtSales.getDoubleValueDoNotThrow()));
			log.info(strLog + "txtCreditCollection:"
					+ BigDecimal.valueOf(txtCreditCollection.getDoubleValueDoNotThrow()));

			txtTotalIngress.setValue(String.valueOf((NumericUtil.stringToBigDecimal(txtSales.getValue()))
					.add(NumericUtil.stringToBigDecimal(txtCreditCollection.getValue()))));
			setTotalCash();

		} catch (Exception e) {
			log.error(strLog + "[Exception]" + e.getMessage());
		}
	}

	private void setTotalEgress() {
		String strLog = "[setTotalEgress]";
		try {
			log.info(strLog + "generalExpense:" + BigDecimal.valueOf(txtGeneralExpense.getDoubleValueDoNotThrow()));
			log.info(strLog + "supplierPayment:"
					+ BigDecimal.valueOf(txtSupplierPaymentsLoan.getDoubleValueDoNotThrow()));
			txtTotalEgress.setValue(String.valueOf((NumericUtil.stringToBigDecimal(txtGeneralExpense.getValue()))
					.add(NumericUtil.stringToBigDecimal(txtSupplierPaymentsLoan.getValue()))));
			setTotalCash();
		} catch (Exception e) {
			log.error(strLog + "[Exception]" + e.getMessage());
		}
	}

	private void setTotalCash() {
		String strLog = "[setTotalCash]";
		try {

			log.info(strLog + "txtTotalSale:" + txtTotalIngress.getDoubleValueDoNotThrow());
			log.info(strLog + "txtTotalEgress:" + txtTotalEgress.getDoubleValueDoNotThrow());

			BigDecimal ingress = NumericUtil.stringToBigDecimal(txtTotalIngress.getValue());
			BigDecimal egress = NumericUtil.stringToBigDecimal(txtTotalEgress.getValue());

			// Ingresos - Egresos
			txtTotalCash.setValue(String.valueOf(ingress.subtract(egress)));

		} catch (Exception e) {
			log.error(strLog + "[Exception]" + e.getMessage());
		}
	}

	private void setBalanceAdministrador() {
		String strLog = "[setBalanceAdministrador]";
		try {
			String cashBase = txtCashBase.getValue();
			String supplierPayments = txtSupplierPayments.getValue();
			String cashRegisterBorrow = txtCashRegisterBorrow.getValue();

			txtBalance.setValue(String.valueOf((NumericUtil.stringToBigDecimal(supplierPayments))
					.subtract(NumericUtil.stringToBigDecimal(cashBase))
					.subtract(NumericUtil.stringToBigDecimal(cashRegisterBorrow))));
			// setTotalCash();

		} catch (Exception e) {
			log.error(strLog + "[Exception]" + e.getMessage());
		}
	}

	@Override
	protected void fillGridData() {
		List<CashConciliation> concilitationList = null;
		if (loginRole.equals(ERole.SUDO.getName()) || loginRole.equals(ERole.MANAGER.getName())) {
			concilitationList = conciliationBll.selectAll();
		} else {
			concilitationList = conciliationBll.select(user.getPerson());
		}
		concilitationList = concilitationList.stream()
				.sorted(Comparator.comparing(CashConciliation::getConciliationDate).reversed())
				.collect(Collectors.toList());
		ListDataProvider<CashConciliation> dataProvider = new ListDataProvider<>(concilitationList);
		filterProductDataProvider = dataProvider.withConfigurableFilter();
		concilicationGrid.setDataProvider(filterProductDataProvider);

	}

	protected void fillDocumentGridData() {
		documentGrid.setDataProvider(documentDataProvider);
	}

	@Override
	protected void saveButtonAction(CashConciliation entity) {
		String message = validateRequiredFields();
		if (!message.isEmpty()) {
			ViewHelper.showNotification(message, Notification.Type.ERROR_MESSAGE);
		} else {
			saveConciliation(entity);
		}

	}

	private void saveConciliation(CashConciliation entity) {
		String strLog = "[saveButtonAction]";
		try {
			Date concilitationDate = null;
			CashConciliation.Builder conciliationBuilder = null;
			if (entity == null) {
				conciliationBuilder = CashConciliation.builder();

			} else {
				conciliationBuilder = CashConciliation.builder(entity);
			}

			concilitationDate = DateUtil.localDateToDate(dfConciliationDate.getValue());
			entity = conciliationBuilder.person(selectedPerson).conciliationDate(concilitationDate)
					.cashBase(NumericUtil.stringToBigDecimal(txtCashBase.getValue())).archived(false).build();

			// save(conciliationBll, entity, "");
			conciliationBll.save(entity, false);

			List<CashConciliation> cashConciliationList = conciliationBll.select(entity.getPerson(),
					entity.getConciliationDate());

			if (cashConciliationList != null && !cashConciliationList.isEmpty()) {
				// Si hay más de cuadre de caja se toma ell primero
				CashConciliation cashConciliation = cashConciliationList.get(0);
				if (autoSaved || employeeRole.equals(ERole.ADMINISTRATOR.getName())) {
					saveAdminConciliation(cashConciliation);
				}

				if (autoSaved || employeeRole.equals(ERole.SALESMAN.getName())) {
					saveSalesmanConciliation(cashConciliation);
				}
			} else {
				ViewHelper.showNotification("Ya existe un cuadre de caja para el día. Por favor edite el cuadre",
						Notification.Type.WARNING_MESSAGE);
			}
		} catch (Exception e) {
			log.error(strLog + "[Exception]" + e.getMessage());
			e.printStackTrace();
			ViewHelper.showNotification("Se generó un error al guardar el cuadre", Notification.Type.ERROR_MESSAGE);
		}

	}

	private void saveAdminConciliation(CashConciliation entity) {
		String strLog = "[saveAdminConciliation]";
		try {
			CashConciliation.Builder conciliationBuilder = null;
			if (entity == null) {
				conciliationBuilder = CashConciliation.builder();
			} else {
				conciliationBuilder = CashConciliation.builder(entity);
			}

			entity = conciliationBuilder
					.supplierPayments(NumericUtil.stringToBigDecimal(txtSupplierPayments.getValue()))
					.cashRegisterBorrow(NumericUtil.stringToBigDecimal(txtCashRegisterBorrow.getValue()))
					.balance(NumericUtil.stringToBigDecimal(txtBalance.getValue())).archived(false).build();

			if (autoSaved) {
				conciliationBll.save(entity);
			} else {
				save(conciliationBll, entity, "Cuadre de caja guardado");
			}
			log.info("Cuadre caja administrador guardado");
		} catch (Exception e) {
			log.error(strLog + "[Exception]" + e.getMessage());
			e.printStackTrace();
			ViewHelper.showNotification("Se generó un error al guardar el cuadre", Notification.Type.ERROR_MESSAGE);
		}

	}

	private void saveSalesmanConciliation(CashConciliation entity) {
		String strLog = "[saveSalesmanConciliation]";
		try {
			CashConciliation.Builder conciliationBuilder = null;
			if (entity == null) {
				conciliationBuilder = CashConciliation.builder();
			} else {
				conciliationBuilder = CashConciliation.builder(entity);
			}

			entity = conciliationBuilder.sales(NumericUtil.stringToBigDecimal(txtSales.getValue()))
					.creditCollection(NumericUtil.stringToBigDecimal(txtCreditCollection.getValue()))
					.totalIngress(NumericUtil.stringToBigDecimal(txtTotalIngress.getValue()))
					.generalExpense(NumericUtil.stringToBigDecimal(txtGeneralExpense.getValue()))
					.supplierPaymentsLoan(NumericUtil.stringToBigDecimal(txtSupplierPaymentsLoan.getValue()))
					.totalEgress(NumericUtil.stringToBigDecimal(txtTotalEgress.getValue()))
					.totalCredit(NumericUtil.stringToBigDecimal(txtTotalCredit.getValue()))
					.totalCash(NumericUtil.stringToBigDecimal(txtTotalCash.getValue()))
					.remnantSale(NumericUtil.stringToBigDecimal(txtRemnantSale.getValue()))
					.remnantEgress(NumericUtil.stringToBigDecimal(txtRemnantEgress.getValue())).archived(false).build();

			if (autoSaved) {
				conciliationBll.save(entity);
			} else {
				save(conciliationBll, entity, "Cuadre de caja guardado");
			}
			log.info("Cuadre caja vendedor guardado");
		} catch (Exception e) {
			log.error(strLog + "[Exception]" + e.getMessage());
			e.printStackTrace();
			ViewHelper.showNotification("Se generó un error al guardar el cuadre", Notification.Type.ERROR_MESSAGE);
		}

	}

	private String validateRequiredFields() {
		String message = "";
		String character = "|";

		if (txtPerson.getValue() == null || txtPerson.getValue().isEmpty()) {
			if (!message.isEmpty()) {
				message = message.concat(character);
			}
			message = message.concat("El empleado es obligatorio");
		}
		if (dfConciliationDate.getValue() == null) {
			if (!message.isEmpty()) {
				message = message.concat(character);
			}
			message = message.concat("La fecha obligatoria");
		}

		return message;

	}

	@Override
	public CashConciliation getSelected() {
		CashConciliation conciliationObj = null;
		Set<CashConciliation> conciliationSet = concilicationGrid.getSelectedItems();
		if (conciliationSet != null && !conciliationSet.isEmpty()) {
			conciliationObj = (CashConciliation) conciliationSet.toArray()[0];
		}
		return conciliationObj;
	}

	@Override
	protected void delete(CashConciliation entity) {
		if (entity != null) {
			entity = CashConciliation.builder(entity).archived(true).build();
			save(conciliationBll, entity, "Cuadre borrado");
		}
	}

	private Panel buildFilterPanel() {
		HorizontalLayout layout = ViewHelper.buildHorizontalLayout(true, true);
		txFilterByName = new TextField("Nombre");
		txFilterByName.addValueChangeListener(e -> refreshGrid());
		txFilterByCode = new TextField("Código");
		txFilterByCode.addValueChangeListener(e -> refreshGrid());
		layout.addComponents(txFilterByCode, txFilterByName);
		return ViewHelper.buildPanel("Filtrar por", layout);
	}

	private void refreshGrid() {
		filterProductDataProvider.setFilter(filterGrid());
		concilicationGrid.getDataProvider().refreshAll();
	}

	private SerializablePredicate<CashConciliation> filterGrid() {
		SerializablePredicate<CashConciliation> columnPredicate = null;
		// String codeFilter = txFilterByCode.getValue().trim();
		// String nameFilter = txFilterByName.getValue().trim();
		/*
		 * columnPredicate = warehouse ->
		 * (warehouse.getName().toLowerCase().contains(nameFilter.toLowerCase()) &&
		 * warehouse.getCode().toLowerCase().contains(codeFilter.toLowerCase()));
		 **********/
		return columnPredicate;
	}

	/**
	 * Metodo para construir la venta modal de personas
	 * 
	 * @param personFiltter
	 */

	private void buildPersonWindow(String personFiltter) {

		personSubwindow = ViewHelper.buildSubwindow("75%", null);
		personSubwindow.setCaption("Personas");

		VerticalLayout subContent = ViewHelper.buildVerticalLayout(true, true);

		// Panel de botones
		Button backBtn = new Button("Cancelar", FontAwesome.BACKWARD);
		backBtn.addStyleName("mystyle-btn");
		backBtn.addClickListener(e -> closeWindow(personSubwindow));

		Button selectBtn = new Button("Seleccionar", FontAwesome.CHECK);
		selectBtn.addStyleName("mystyle-btn");
		selectBtn.addClickListener(e -> selectPerson());

		HorizontalLayout buttonLayout = ViewHelper.buildHorizontalLayout(true, true);
		buttonLayout.addComponents(backBtn, selectBtn);
		Panel buttonPanel = ViewHelper.buildPanel(null, buttonLayout);

		try {
			Commons.PERSON_TYPE = PersonType.USER.getName();
			personLayout = new PersonLayout(true);

		} catch (IOException e) {
			log.error("Error al cargar lista de personas. Exception:" + e);
		}
		Panel personPanel = ViewHelper.buildPanel(null, personLayout);
		subContent.addComponents(buttonPanel, personPanel);

		personSubwindow.setContent(subContent);
		getUI().addWindow(personSubwindow);

	}

	private void closeWindow(Window w) {
		w.close();
	}

	/**
	 * Método para seleccionar proveedor o cliente
	 */
	private void selectPerson() {
		selectedPerson = personLayout.getSelected();

		if (selectedPerson != null) {
			txtPerson.setValue(selectedPerson.getName() + " " + selectedPerson.getLastName());
			// employeeRole = userBll.select(selectedPerson).getRole().getName();

			if (detailLayout != null) {
				removeComponent(detailLayout);
			}
			detailLayout = (VerticalLayout) buildDetailLayout(null);
			addComponent(detailLayout);
			personSubwindow.close();
		} else {
			ViewHelper.showNotification("Seleccione una persona", Notification.Type.WARNING_MESSAGE);
		}
	}

	private Double getSumDailySales() {
		String strLog = "[getTotalDailySales]";
		Double totalSale = null;
		try {
			getDailySalesData();
			totalSale = documentDataProvider.fetch(new Query<>()).mapToDouble(document -> {
				if (document.getTotalValue() != null) {
					return document.getTotalValue();
				} else {
					return 0.0;
				}
			}).sum();
		} catch (Exception e) {
			log.error(strLog + "[Exception]" + e.getMessage());
		}

		log.info(strLog + " totalSale by day: " + totalSale);
		return totalSale;
	}

	private void getDailySalesData() {
		String strLog = "[getDailySalesData]";
		try {
			List<DocumentType> types = documentTypeBll.select(ETransactionType.SALIDA);
			documentDataProvider = new ListDataProvider<>(documentBll.select(types));
			documentDataProvider.setFilter(document -> filterDocumentByDate(document, null));
			log.info(strLog + " docs ventas filtrados cant ->" + documentDataProvider.getItems().size());
		} catch (Exception e) {
			log.error(strLog + "[Exception]" + e.getMessage());
		}
	}

	private Double getSumPaidDailySales() {
		String strLog = "[getSumPaidDailySales]";
		Double totalSale = 0.0;
		try {
			getDailyPaidSalesData();
			totalSale = documentDataProvider.fetch(new Query<>()).mapToDouble(document -> {
				if (document.getTotalValue() != null) {
					return document.getTotalValue();
				} else {
					return 0.0;
				}
			}).sum();
		} catch (Exception e) {
			log.error(strLog + "[Exception]" + e.getMessage());
		}

		log.info(strLog + " totalPaidSale by day: " + totalSale);
		return totalSale;
	}

	private void getDailyPaidSalesData() {
		String strLog = "[getDailyPaidSalesData]";
		try {
			List<DocumentType> types = documentTypeBll.select(ETransactionType.SALIDA);
			/*
			 * List<PaymentType> payTypes = new ArrayList<>();
			 * 
			 * PaymentType payTypeObj = paymentTypeBll.select(EPaymemtType.PAID.getName());
			 * 
			 * payTypes.add(payTypeObj); payTypeObj =
			 * PaymentType.builder().code(EPaymemtType.PREPAY.getName()).build();
			 * payTypes.add(payTypeObj);
			 */
			PaymentType paymentType = paymentTypeBll.select(EPaymemtType.PAID.getName());
			documentDataProvider = new ListDataProvider<>(documentBll.select(types));
			documentDataProvider.setFilter(document -> filterDocumentByDate(document, paymentType));
			log.info(strLog + " docs ventas pagadas filtrados cant ->" + documentDataProvider.getItems().size());
		} catch (Exception e) {
			log.error(strLog + "[Exception]" + e.getMessage());
		}
	}

	private Double getSumDailyPurchases() {
		String strLog = "[getSumDailyPurchases]";
		Double totalPurchase = null;
		try {
			getDailyPurchaseData();
			totalPurchase = documentDataProvider.fetch(new Query<>()).mapToDouble(document -> {
				if (document.getTotalValue() != null) {
					return document.getTotalValue();
				} else {
					return 0.0;
				}
			}).sum();
		} catch (Exception e) {
			log.error(strLog + "[Exception]" + e.getMessage());
		}

		log.info(strLog + " total pruchase by day: " + totalPurchase);
		return totalPurchase;
	}

	private void getDailyPurchaseData() {
		String strLog = "[getDailyPurchaseData]";
		try {
			List<DocumentType> types = documentTypeBll.select(ETransactionType.ENTRADA);
			documentDataProvider = new ListDataProvider<>(documentBll.select(types));
			documentDataProvider.setFilter(document -> filterDocumentByDate(document, null));
			log.info(strLog + " docs compras filtrados cant ->" + documentDataProvider.getItems().size());
		} catch (Exception e) {
			log.error(strLog + "[Exception]" + e.getMessage());
		}
	}

	public Double getSumDailyCollection() {
		String strLog = "[getSumDailyCollection]";
		Double totalCollection = null;
		try {
			getDailyCollectionData();
			totalCollection = collectionDataProvider.fetch(new Query<>()).mapToDouble(collection -> {
				if (collection.getFee() != null) {
					return NumericUtil.bigDecimalToDouble(collection.getFee());
				} else {
					return 0.0;
				}
			}).sum();
		} catch (Exception e) {
			log.error(strLog + "[Exception]" + e.getMessage());
		}

		log.info(strLog + " sum total collection by day: " + totalCollection);
		return totalCollection;
	}

	private void getDailyCollectionData() {
		String strLog = "[getDailyCollectionData]";
		try {
			collectionDataProvider = new ListDataProvider<>(collectionBll.selectAll());
			collectionDataProvider.setFilter(collection -> filterCollectiobByDate(collection));
			log.info(strLog + " recaudos cant ->" + collectionDataProvider.getItems().size());
		} catch (Exception e) {
			log.error(strLog + "[Exception]" + e.getMessage());
		}
	}

	private Double getSumDailyExpense() {
		String strLog = "[getSumDailyExpense]";
		Double totalExpense = null;
		try {
			getDailyExpensesData();
			totalExpense = expenseDataProvider.fetch(new Query<>()).mapToDouble(expense -> {
				if (expense.getValue() != null) {
					return NumericUtil.bigDecimalToDouble(expense.getValue());
				} else {
					return 0.0;
				}
			}).sum();
		} catch (Exception e) {
			log.error(strLog + "[Exception]" + e.getMessage());
		}

		log.info(strLog + " sum total expense by day: " + totalExpense);
		return totalExpense;
	}

	private void getDailyExpensesData() {
		String strLog = "[getDailyExpensesData]";
		try {
			EgressType egressType = egressTypeBll.select(EEgressType.EXPENSE.getName());
			expenseDataProvider = new ListDataProvider<>(egressBll.select(selectedPerson));
			expenseDataProvider.setFilter(egress -> filterEgressByDate(egress, egressType));
			log.info(strLog + " Gastos cant ->" + expenseDataProvider.getItems().size());
		} catch (Exception e) {
			log.error(strLog + "[Exception]" + e.getMessage());
		}
	}

	private Double getSumDailyLoan() {
		String strLog = "[getSumDailyLoan]";
		Double totalLoan = null;
		try {
			getDailyLoansData();
			totalLoan = loanDataProvider.fetch(new Query<>()).mapToDouble(loan -> {
				if (loan.getValue() != null) {
					return NumericUtil.bigDecimalToDouble(loan.getValue());
				} else {
					return 0.0;
				}
			}).sum();
		} catch (Exception e) {
			log.error(strLog + "[Exception]" + e.getMessage());
		}

		log.info(strLog + " sum total loans by day: " + totalLoan);
		return totalLoan;
	}

	private void getDailyLoansData() {
		String strLog = "[getDailyLoansData]";
		try {
			EgressType egressType = egressTypeBll.select(EEgressType.LOAN.getName());
			loanDataProvider = new ListDataProvider<>(egressBll.select(selectedPerson));
			loanDataProvider.setFilter(egress -> filterEgressByDate(egress, egressType));
			log.info(strLog + " Prestamos cant ->" + loanDataProvider.getItems().size());
		} catch (Exception e) {
			log.error(strLog + "[Exception]" + e.getMessage());
		}
	}

	private Double getSumDailyCreditSales() {
		String strLog = "[getSumDailyCreditSales]";
		Double totalCreditSale = null;
		try {
			getDailyCreditSalesData();
			totalCreditSale = documentDataProvider.fetch(new Query<>()).mapToDouble(document -> {
				if (document.getTotalValue() != null) {
					return document.getTotalValue();
				} else {
					return 0.0;
				}
			}).sum();
		} catch (Exception e) {
			log.error(strLog + "[Exception]" + e.getMessage());
		}

		log.info(strLog + " totalCreditSale by day: " + totalCreditSale);
		return totalCreditSale;
	}

	private void getDailyCreditSalesData() {
		String strLog = "[getDailyCreditSalesData]";
		try {
			PaymentType paymentType = paymentTypeBll.select(EPaymemtType.CREDIT.getName());
			documentDataProvider.setFilter(document -> filterDocumentByDate(document, paymentType));
			log.info(strLog + " docs filtrados cant ->" + documentDataProvider.getItems().size());
		} catch (Exception e) {
			log.error(strLog + "[Exception]" + e.getMessage());
		}
	}

	private boolean filterDocumentByDate(Document document, PaymentType paymentType) {
		String strLog = "[filterDocumentByDate]";
		boolean result = false;
		try {
			// Date iniDateFilter =
			// DateUtil.localDateTimeToDate(DateUtil.getDefaultIniDate());
			// Date endDateFilter =
			// DateUtil.localDateTimeToDate(DateUtil.getDefaultEndDateTime());

			log.info(strLog + " iniDateFilter: " + iniDateFilter + ", endDateFilter:" + endDateFilter);

			result = document.getDocumentDate().before(endDateFilter)
					&& document.getDocumentDate().after(iniDateFilter);

			if (paymentType != null) {
				result = result && document.getPaymentType().equals(paymentType);
			}
		} catch (Exception e) {
			log.error(strLog + e.getMessage());
		}

		log.info(strLog + " result filterDocumentByDate: " + result);
		return result;
	}

	private boolean filterEgressByDate(Egress egress, EgressType egressType) {
		String strLog = "[filterEgressByDate]";
		boolean result = false;
		try {
			// Date iniDateFilter =
			// DateUtil.localDateTimeToDate(DateUtil.getDefaultIniDate());
			// Date endDateFilter =
			// DateUtil.localDateTimeToDate(DateUtil.getDefaultEndDateTime());

			log.info(strLog + " iniDateFilter: " + iniDateFilter + ", endDateFilter:" + endDateFilter);

			result = egress.getEgressDate().before(endDateFilter) && egress.getEgressDate().after(iniDateFilter);

			if (egressType != null) {
				result = result && egress.getType().equals(egressType);
			}
		} catch (Exception e) {
			log.error(strLog + e.getMessage());
		}

		log.info(strLog + " result filterEgressByDate: " + result);
		return result;
	}

	private boolean filterCollectiobByDate(Collection collection) {
		String strLog = "[filterCollectiobByDate]";
		boolean result = false;
		try {
			// Date iniDateFilter =
			// DateUtil.localDateTimeToDate(DateUtil.getDefaultIniDate());
			// Date endDateFilter =
			// DateUtil.localDateTimeToDate(DateUtil.getDefaultEndDateTime());

			log.info(strLog + " iniDateFilter: " + iniDateFilter + ", endDateFilter:" + endDateFilter);

			result = collection.getCollectionDate().before(endDateFilter)
					&& collection.getCollectionDate().after(iniDateFilter);

		} catch (Exception e) {
			log.error(strLog + e.getMessage());
		}

		log.info(strLog + " result filterCollectiobByDate: " + result);
		return result;
	}

	/**
	 * Guardar la conciliación de caja por dia y empleado
	 * 
	 * @param user
	 * @param conciliationDate
	 */
	public void saveDailyConciliation(User user, Date conciliationDate) {
		String strLog = "[saveDailyConciliation] ";
		try {
			log.info(strLog + "[parameters] user: " + user + ", conciliationDate: " + conciliationDate);
			this.user = user;
			this.loginRole = user.getRole().getName();
			this.employeeRole = user.getRole().getName();
			this.autoSaved = true;
			loginRole = ERole.SALESMAN.getName();
			employeeRole = ERole.SALESMAN.getName();
			// Guardar las entradas
			buildEditionComponent(null);

			//Se toma el primer cuadre
			CashConciliation cashConciliation = conciliationBll.select(user.getPerson(), conciliationDate).get(0);

			// Guardar las salidas
			loginRole = ERole.ADMINISTRATOR.getName();
			employeeRole = ERole.ADMINISTRATOR.getName();
			buildEditionComponent(null);

			saveConciliation(cashConciliation);
		} catch (Exception e) {
			log.error(strLog + "[Exception] " + e.getMessage());
			e.printStackTrace();
		}
	}

}
