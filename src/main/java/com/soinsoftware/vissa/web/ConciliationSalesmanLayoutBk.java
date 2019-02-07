package com.soinsoftware.vissa.web;

import static com.soinsoftware.vissa.web.VissaUI.KEY_CASH_CONCILIATION;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Date;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.vaadin.ui.NumberField;

import com.soinsoftware.vissa.bll.CashRegisterConciliationBll;
import com.soinsoftware.vissa.bll.DocumentBll;
import com.soinsoftware.vissa.bll.DocumentTypeBll;
import com.soinsoftware.vissa.bll.PaymentTypeBll;
import com.soinsoftware.vissa.model.CashConciliation;
import com.soinsoftware.vissa.model.Document;
import com.soinsoftware.vissa.model.DocumentType;
import com.soinsoftware.vissa.model.EPaymemtType;
import com.soinsoftware.vissa.model.ERole;
import com.soinsoftware.vissa.model.PaymentType;
import com.soinsoftware.vissa.model.Person;
import com.soinsoftware.vissa.model.PersonType;
import com.soinsoftware.vissa.model.TransactionType;
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

@SuppressWarnings("unchecked")
public class ConciliationSalesmanLayoutBk extends AbstractEditableLayout<CashConciliation> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5076502522106126046L;

	protected static final Logger log = Logger.getLogger(ConciliationSalesmanLayoutBk.class);

	private final CashRegisterConciliationBll conciliationBll;
	private final DocumentBll documentBll;
	private final DocumentTypeBll documentTypeBll;
	private final PaymentTypeBll paymentTypeBll;

	private Grid<CashConciliation> concilicationGrid;

	private TextField txFilterByName;
	private TextField txFilterByCode;

	private NumberField txtCashBase;
	private TextField txtPerson;
	private DateField dfConciliationDate;
	private NumberField txtSales;
	private NumberField txtCreditCollection;
	private NumberField txtRemnantSale;
	private NumberField txtGeneralExpense;
	private NumberField txtSupplierPayment;
	private NumberField txtRemnantEgress;
	private NumberField txtTotalSale;
	private NumberField txtTotalCash;
	private NumberField txtTotalEgress;
	private NumberField txtTotalCredit;

	private Window personSubwindow;
	private PersonLayout personLayout = null;
	private Person selectedPerson = null;

	private User user;
	private String role;

	private ListDataProvider<Document> documentDataProvider;
	private ConfigurableFilterDataProvider<CashConciliation, Void, SerializablePredicate<CashConciliation>> filterProductDataProvider;

	public ConciliationSalesmanLayoutBk() throws IOException {
		super("Cuadre de caja", KEY_CASH_CONCILIATION);
		conciliationBll = CashRegisterConciliationBll.getInstance();
		documentBll = DocumentBll.getInstance();
		documentTypeBll = DocumentTypeBll.getInstance();
		paymentTypeBll = PaymentTypeBll.getInstance();

	}

	@Override
	protected AbstractOrderedLayout buildListView() {

		this.user = getSession().getAttribute(User.class);
		this.role = user.getRole().getName();

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
		concilicationGrid.addColumn(CashConciliation::getConciliationDate).setCaption("Fecha");
		if (role.equals(ERole.SUDO.getName()) || role.equals(ERole.MANAGER.getName())) {
			concilicationGrid.addColumn(cashRegisterConciliation -> {
				if (cashRegisterConciliation.getPerson() != null) {
					return cashRegisterConciliation.getPerson().getName() + " "
							+ cashRegisterConciliation.getPerson().getLastName();
				} else {
					return "";
				}
			}).setCaption("Empleado");

		}
		concilicationGrid.addColumn(CashConciliation::getTotalIngress).setCaption("Total ventas");
		concilicationGrid.addColumn(CashConciliation::getTotalEgress).setCaption("Total egresos");
		concilicationGrid.addColumn(CashConciliation::getTotalCredit).setCaption("Créditos");
		concilicationGrid.addColumn(CashConciliation::getTotalCash).setCaption("Efectivo neto");

		layout.addComponent(ViewHelper.buildPanel(null, concilicationGrid));
		fillGridData();
		return ViewHelper.buildPanel(null, layout);
	}

	@SuppressWarnings("deprecation")
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

		if (role.equals(ERole.SUDO.getName()) || role.equals(ERole.MANAGER.getName())) {
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
		// -------------------------------------------------------------------------

		txtSales = new NumberField("Ventas");

		txtSales.setStyleName(ValoTheme.TEXTFIELD_TINY);
		txtSales.setReadOnly(true);
		txtSales.setWidth("50%");
		txtSales.setValue(getSumDailySales());
		txtSales.setDecimalAllowed(true);
		txtSales.setDecimalPrecision(2);
		txtSales.setNegativeAllowed(false);

		txtSales.setDecimalSeparator(',');

		txtCreditCollection = new NumberField("Recaudo créditos");
		txtCreditCollection.setStyleName(ValoTheme.TEXTFIELD_TINY);
		txtCreditCollection.setWidth("50%");
		txtCreditCollection.addValueChangeListener(e -> setTotalSale());

		txtRemnantSale = new NumberField("Sobrante");
		txtRemnantSale.setStyleName(ValoTheme.TEXTFIELD_TINY);
		txtRemnantSale.setWidth("50%");
		txtRemnantSale.addValueChangeListener(e -> setTotalSale());

		txtTotalSale = new NumberField("Total Ventas");
		txtTotalSale.setStyleName(ValoTheme.TEXTFIELD_TINY);
		txtTotalSale.setWidth("50%");
		txtTotalSale.setReadOnly(true);

		FormLayout saleForm = ViewHelper.buildForm("", false, false);
		saleForm.addComponents(txtSales, txtCreditCollection, txtRemnantSale, txtTotalSale);
		Panel salePanel = ViewHelper.buildPanel("VENTAS", saleForm);
		layout.addComponents(salePanel);

		// -------------------------------------------------------------------------
		txtGeneralExpense = new NumberField("Gastos generales");
		txtGeneralExpense.setStyleName(ValoTheme.TEXTFIELD_TINY);
		txtGeneralExpense.setWidth("50%");
		txtGeneralExpense.addValueChangeListener(e -> setTotalEgress());

		txtSupplierPayment = new NumberField("Pago a proveedores");
		txtSupplierPayment.setStyleName(ValoTheme.TEXTFIELD_TINY);
		txtSupplierPayment.setWidth("50%");
		txtSupplierPayment.addValueChangeListener(e -> setTotalEgress());

		txtRemnantEgress = new NumberField("Remanente");
		txtRemnantEgress.setStyleName(ValoTheme.TEXTFIELD_TINY);
		txtRemnantEgress.setWidth("50%");

		txtTotalEgress = new NumberField("Total egresos");
		txtTotalEgress.setStyleName(ValoTheme.TEXTFIELD_TINY);
		txtTotalEgress.setWidth("50%");
		txtTotalEgress.setReadOnly(true);

		FormLayout egressForm = ViewHelper.buildForm("", true, false);
		egressForm.addComponents(txtGeneralExpense, txtSupplierPayment, txtRemnantEgress, txtTotalEgress);
		Panel egressPanel = ViewHelper.buildPanel("EGRESOS", egressForm);
		layout.addComponents(egressPanel);

		// -------------------------------------------------------------------------
		txtTotalCredit = new NumberField("Créditos");
		txtTotalCredit.setStyleName(ValoTheme.TEXTFIELD_TINY);
		txtTotalCredit.setWidth("50%");
		txtTotalCredit.setValue(getSumDailyCreditSales());

		txtTotalCash = new NumberField("Efectivo neto");
		txtTotalCash.setStyleName(ValoTheme.TEXTFIELD_TINY);
		txtTotalCash.setWidth("50%");
		txtTotalCash.setReadOnly(true);

		FormLayout totalForm = ViewHelper.buildForm("", false, false);
		totalForm.addComponents(txtTotalCredit, txtTotalCash);
		Panel totalPanel = ViewHelper.buildPanel("TOTALES", totalForm);
		layout.addComponents(totalPanel);

		setFieldValues(concilitation);
		// ----------------------------------------------------------------------------------

		return layout;
	}

	private void setFieldValues(CashConciliation concil) {

		if (concil != null) {
			selectedPerson = concil.getPerson();
			txtPerson.setValue(concil.getPerson().getName() + " " + concil.getPerson().getLastName());
			dfConciliationDate.setValue(DateUtil.dateToLocalDate(concil.getConciliationDate()));
			txtCashBase.setValue(String.valueOf(concil.getCashBase()));
			txtSales.setValue(String.valueOf(concil.getSales()));
			txtCreditCollection.setValue(String.valueOf(concil.getCreditCollection()));
			txtRemnantSale.setValue(String.valueOf(concil.getRemnantSale()));
			txtTotalSale.setValue((String.valueOf(concil.getTotalIngress())));
			txtGeneralExpense.setValue(String.valueOf(concil.getGeneralExpense()));
			txtRemnantEgress.setValue(String.valueOf(concil.getRemnantEgress()));
			txtSupplierPayment.setValue(String.valueOf(concil.getSupplierPaymentsLoan()));
			txtTotalEgress.setValue(String.valueOf(concil.getTotalEgress()));
			txtTotalCredit.setValue(String.valueOf(concil.getTotalCredit()));
			txtTotalCash.setValue(String.valueOf(concil.getTotalCash()));
		}
		setTotalSale();
		setTotalEgress();

	}

	private void setTotalSale() {
		String strLog = "[setTotalSale]";
		try {
			log.info(strLog + "txtSales string:" + txtSales.getValue());
			log.info(strLog + "txtSales double:" + txtSales.getDoubleValueDoNotThrow());
			log.info(strLog + "txtSales:" + BigDecimal.valueOf(txtSales.getDoubleValueDoNotThrow()));
			log.info(strLog + "txtCreditCollection:"
					+ BigDecimal.valueOf(txtCreditCollection.getDoubleValueDoNotThrow()));

			txtTotalSale.setValue(String.valueOf((NumericUtil.stringToBigDecimal(txtSales.getValue()))
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
			log.info(strLog + "supplierPayment:" + BigDecimal.valueOf(txtSupplierPayment.getDoubleValueDoNotThrow()));
			txtTotalEgress.setValue(String.valueOf((NumericUtil.stringToBigDecimal(txtGeneralExpense.getValue()))
					.add(NumericUtil.stringToBigDecimal(txtSupplierPayment.getValue()))));
			setTotalCash();
		} catch (Exception e) {
			log.error(strLog + "[Exception]" + e.getMessage());
		}
	}

	private void setTotalCash() {
		String strLog = "[setTotalCash]";
		try {
			log.info(strLog + "txtTotalSale double:" + txtTotalSale.getDoubleValueDoNotThrow());
			log.info(strLog + "txtTotalSale:" + BigDecimal.valueOf(txtTotalSale.getDoubleValueDoNotThrow()));
			log.info(strLog + "txtTotalEgress:" + BigDecimal.valueOf(txtTotalEgress.getDoubleValueDoNotThrow()));

			txtTotalCash.setValue(String.valueOf((NumericUtil.stringToBigDecimal(txtTotalSale.getValue()))
					.subtract(NumericUtil.stringToBigDecimal(txtTotalEgress.getValue()))));

		} catch (Exception e) {
			log.error(strLog + "[Exception]" + e.getMessage());
		}
	}

	@Override
	protected void fillGridData() {
		List<CashConciliation> concilitationList = null;
		if (role.equals(ERole.SUDO.getName()) || role.equals(ERole.MANAGER.getName())) {
			concilitationList = conciliationBll.selectAll();
		} else {
			concilitationList = conciliationBll.select(user.getPerson());
		}
		ListDataProvider<CashConciliation> dataProvider = new ListDataProvider<>(concilitationList);
		filterProductDataProvider = dataProvider.withConfigurableFilter();
		concilicationGrid.setDataProvider(filterProductDataProvider);

	}

	@Override
	protected void saveButtonAction(CashConciliation entity) {
		String message = validateRequiredFields();
		if (!message.isEmpty()) {
			ViewHelper.showNotification(message, Notification.Type.ERROR_MESSAGE);
		} else {
			voidSaveConciliation(entity);
		}

	}

	private void voidSaveConciliation(CashConciliation entity) {
		String strLog = "[saveButtonAction]";
		try {
			CashConciliation.Builder conciliationBuilder = null;
			if (entity == null) {
				conciliationBuilder = CashConciliation.builder();
			} else {
				conciliationBuilder = CashConciliation.builder(entity);
			}

			Date concilitationDate = DateUtil.localDateToDate(dfConciliationDate.getValue());
			entity = conciliationBuilder.person(selectedPerson).conciliationDate(concilitationDate)
					.cashBase(NumericUtil.stringToBigDecimal(txtCashBase.getValue()))
					.sales(NumericUtil.stringToBigDecimal(txtSales.getValue()))
					.creditCollection(NumericUtil.stringToBigDecimal(txtCreditCollection.getValue()))
					.totalIngress(NumericUtil.stringToBigDecimal(txtTotalSale.getValue()))
					.generalExpense(NumericUtil.stringToBigDecimal(txtGeneralExpense.getValue()))
					.supplierPayments(NumericUtil.stringToBigDecimal(txtSupplierPayment.getValue()))
					.totalEgress(NumericUtil.stringToBigDecimal(txtTotalEgress.getValue()))
					.totalCredit(NumericUtil.stringToBigDecimal(txtTotalCredit.getValue()))
					.totalCash(NumericUtil.stringToBigDecimal(txtTotalCash.getValue()))
					.remnantSale(NumericUtil.stringToBigDecimal(txtRemnantSale.getValue()))
					.remnantEgress(NumericUtil.stringToBigDecimal(txtRemnantEgress.getValue())).archived(false).build();

			save(conciliationBll, entity, "Cuadre de caja guardado");
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
	@SuppressWarnings("deprecation")
	private void buildPersonWindow(String personFiltter) {

		personSubwindow = ViewHelper.buildSubwindow("75%");
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
			List<DocumentType> types = documentTypeBll.select(TransactionType.SALIDA);
			documentDataProvider = new ListDataProvider<>(documentBll.select(types));
			documentDataProvider.setFilter(document -> filterDocumentByDate(document, null));
			log.info(strLog + " docs filtrados cant ->" + documentDataProvider.getItems().size());
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
			Date iniDateFilter = DateUtil.localDateTimeToDate(DateUtil.getDefaultIniDate());
			Date endDateFilter = DateUtil.localDateTimeToDate(DateUtil.getDefaultEndDate());

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

}
