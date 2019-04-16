package com.soinsoftware.vissa.web;

import static com.soinsoftware.vissa.web.VissaUI.KEY_SUPPLIER_PAYMENTS;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang.time.DateUtils;
import org.apache.log4j.Logger;
import org.vaadin.ui.NumberField;

import com.soinsoftware.vissa.bll.CollectionBll;
import com.soinsoftware.vissa.bll.DocumentBll;
import com.soinsoftware.vissa.bll.DocumentTypeBll;
import com.soinsoftware.vissa.bll.PaymentTypeBll;
import com.soinsoftware.vissa.common.CommonsConstants;
import com.soinsoftware.vissa.model.Collection;
import com.soinsoftware.vissa.model.Document;
import com.soinsoftware.vissa.model.DocumentType;
import com.soinsoftware.vissa.model.EPaymemtType;
import com.soinsoftware.vissa.model.EPaymentStatus;
import com.soinsoftware.vissa.model.ETransactionType;
import com.soinsoftware.vissa.model.PaymentType;
import com.soinsoftware.vissa.model.Person;
import com.soinsoftware.vissa.model.PersonType;
import com.soinsoftware.vissa.model.User;
import com.soinsoftware.vissa.util.Commons;
import com.soinsoftware.vissa.util.DateUtil;
import com.soinsoftware.vissa.util.ELayoutMode;
import com.soinsoftware.vissa.util.NumericUtil;
import com.soinsoftware.vissa.util.StringUtility;
import com.soinsoftware.vissa.util.ViewHelper;
import com.vaadin.data.provider.ListDataProvider;
import com.vaadin.data.provider.Query;
import com.vaadin.server.FontAwesome;
import com.vaadin.ui.AbstractOrderedLayout;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.DateTimeField;
import com.vaadin.ui.FormLayout;
import com.vaadin.ui.Grid;
import com.vaadin.ui.Grid.Column;
import com.vaadin.ui.Grid.SelectionMode;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Panel;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.components.grid.FooterRow;
import com.vaadin.ui.themes.ValoTheme;

@SuppressWarnings({ "unchecked", "deprecation" })
public class SupplierPaymentLayout extends AbstractEditableLayout<Collection> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5076502522106126046L;

	protected static final Logger log = Logger.getLogger(SupplierPaymentLayout.class);

	private final CollectionBll collectionBll;
	private final DocumentBll documentBll;
	private final DocumentTypeBll documentTypeBll;
	private final PaymentTypeBll paymentTypeBll;

	private Grid<Collection> collectionGrid;

	private TextField txFilterByName;
	private TextField txFilterByCode;

	private TextField txtDocumentNumber;
	private TextField txtDocumentDate;
	private TextField txtPerson;
	private DateTimeField dtfCollectionDate;
	private NumberField txtDocumentValue;
	private NumberField txtInitialBalance;
	private NumberField txtFee;
	private NumberField txtFinalBalance;
	private TextField txtFilterByPerson;
	private DateTimeField dtfFilterIniDate;
	private DateTimeField dtfFilterEndDate;
	private CheckBox checkFilterBalance;
	private TextField txtQuantity;
	private TextField txtTotal;

	private Window invoiceWindow;
	private InvoiceReportLayout invoicListLayout = null;
	private Document selectedDocument = null;
	private Window personSubwindow;
	private PersonLayout personLayout = null;
	private User user;

	private Person personSelected = null;
	private Column<?, ?> totalColumn;
	private Column<?, ?> feeColumn;
	private FooterRow footer;

	private ListDataProvider<Document> documentDataProvider;
	ListDataProvider<Collection> collectionDataProvider;

	public SupplierPaymentLayout() throws IOException {
		super("Pago a proveedores", KEY_SUPPLIER_PAYMENTS);
		collectionBll = CollectionBll.getInstance();
		documentBll = DocumentBll.getInstance();
		documentTypeBll = DocumentTypeBll.getInstance();
		paymentTypeBll = PaymentTypeBll.getInstance();

	}

	@Override
	protected AbstractOrderedLayout buildListView() {

		this.user = getSession().getAttribute(User.class);

		VerticalLayout layout = ViewHelper.buildVerticalLayout(false, false);
		Panel buttonPanel = buildButtonPanelForLists();
		Panel filterPanel = buildFilterPanel();
		Panel totalPanel = builTotalPanel();
		Panel dataPanel = buildGridPanel();
		layout.addComponents(buttonPanel, filterPanel, totalPanel, dataPanel);
		this.setMargin(false);
		this.setSpacing(false);
		return layout;
	}

	@Override
	protected AbstractOrderedLayout buildEditionView(Collection entity) {
		VerticalLayout layout = ViewHelper.buildVerticalLayout(false, false);
		Panel buttonPanel = buildButtonPanelForEdition(entity);
		Component dataPanel = buildEditionComponent(entity);
		layout.addComponents(buttonPanel, dataPanel);
		return layout;
	}

	@Override
	protected Panel buildGridPanel() {
		VerticalLayout layout = ViewHelper.buildVerticalLayout(true, true);
		collectionGrid = ViewHelper.buildGrid(SelectionMode.SINGLE);

		collectionGrid.addColumn(collection -> {
			if (collection.getCollectionDate() != null) {
				return DateUtil.dateToString(collection.getCollectionDate());
			} else {
				return "";
			}
		}).setCaption("Fecha");

		collectionGrid.addColumn(collection -> {
			if (collection.getDocument() != null && collection.getDocument().getPerson() != null) {
				return collection.getDocument().getPerson().getName() + " "
						+ collection.getDocument().getPerson().getLastName();
			} else {
				return "";
			}
		}).setCaption("Persona");

		collectionGrid.addColumn(collection -> {
			if (collection.getDocument() != null) {
				return collection.getDocument().getCode();
			} else {
				return "";
			}
		}).setCaption("Factura");

		collectionGrid.addColumn(collection -> {
			if (collection.getDocument() != null) {
				return collection.getDocument().getTotalValue();
			} else {
				return "";
			}
		}).setCaption("Valor factura");

		collectionGrid.addColumn(Collection::getInitialBalance).setCaption("Monto inicial");
		feeColumn = collectionGrid.addColumn(Collection::getFee).setCaption("Valor abono");
		totalColumn = collectionGrid.addColumn(Collection::getFinalBalance).setCaption("Saldo final");

		footer = collectionGrid.prependFooterRow();
		footer.getCell(feeColumn).setHtml("<b>Total deuda:</b>");

		layout.addComponent(ViewHelper.buildPanel(null, collectionGrid));
		fillGridData();
		refreshGrid();
		return ViewHelper.buildPanel(null, layout);
	}

	@Override
	protected Component buildEditionComponent(Collection collection) {

		VerticalLayout layout = ViewHelper.buildVerticalLayout(false, false);
		FormLayout basicForm = ViewHelper.buildForm("", false, false);

		dtfCollectionDate = new DateTimeField("Fecha");
		dtfCollectionDate.setStyleName(ValoTheme.DATEFIELD_TINY);
		dtfCollectionDate.setDateFormat(Commons.FORMAT_DATE_TIME);
		dtfCollectionDate.setValue(LocalDateTime.now());
		dtfCollectionDate.setWidth("50%");
		dtfCollectionDate.setRequiredIndicatorVisible(true);

		txtDocumentNumber = new TextField("Número de factura");
		txtDocumentNumber.setStyleName(ValoTheme.TEXTFIELD_TINY);
		txtDocumentNumber.setReadOnly(true);
		txtDocumentNumber.setWidth("50%");

		Button searchInvoiceBtn = new Button("Buscar factura", FontAwesome.SEARCH);
		searchInvoiceBtn.addClickListener(e -> builInvoiceWindow(txtDocumentNumber.getValue()));
		searchInvoiceBtn.setStyleName(ValoTheme.BUTTON_TINY);
		FormLayout numIvoiceLayout = ViewHelper.buildForm("", false, false);
		numIvoiceLayout.addComponents(txtDocumentNumber);
		HorizontalLayout invoiceLayout = ViewHelper.buildHorizontalLayout(false, false);
		invoiceLayout.addComponents(new Label("Factura"), numIvoiceLayout, searchInvoiceBtn);
		invoiceLayout.setComponentAlignment(searchInvoiceBtn, Alignment.BOTTOM_CENTER);
		// txtDocumentNumber.addContextClickListener(e ->
		// builInvoiceWindow(txtDocumentNumber.getValue()));

		txtDocumentDate = new TextField("Fecha de factura");
		txtDocumentDate.setStyleName(ValoTheme.TEXTFIELD_TINY);
		txtDocumentDate.setWidth("50%");

		txtPerson = new TextField("Cliente");
		txtPerson.setStyleName(ValoTheme.TEXTFIELD_TINY);
		txtPerson.setReadOnly(true);
		txtPerson.setWidth("50%");

		txtDocumentValue = new NumberField("Valor factura");
		txtDocumentValue.setStyleName(ValoTheme.TEXTFIELD_TINY);
		txtDocumentValue.setReadOnly(true);
		txtDocumentValue.setWidth("50%");

		basicForm.addComponents(dtfCollectionDate, searchInvoiceBtn, txtDocumentNumber, txtDocumentDate, txtPerson,
				txtDocumentValue);

		Panel basicPanel = ViewHelper.buildPanel("", basicForm);
		layout.addComponents(basicPanel);

		// -------------------------------------------------------------------------
		// Datos del recaudo
		txtInitialBalance = new NumberField("Saldo");
		txtInitialBalance.setStyleName(ValoTheme.TEXTFIELD_TINY);
		txtInitialBalance.setWidth("50%");
		txtInitialBalance.setRequiredIndicatorVisible(true);
		// txtInitialBalance.setReadOnly(true);
		// txtInitialBalance.addValueChangeListener(e -> setTotalSale());

		txtFee = new NumberField("Abono");
		txtFee.setStyleName(ValoTheme.TEXTFIELD_TINY);
		txtFee.setWidth("50%");
		txtFee.setRequiredIndicatorVisible(true);
		txtFee.addValueChangeListener(e -> setFinalBalance());

		txtFinalBalance = new NumberField("Saldo final");
		txtFinalBalance.setStyleName(ValoTheme.TEXTFIELD_TINY);
		txtFinalBalance.setReadOnly(true);
		txtFinalBalance.setWidth("50%");
		txtFinalBalance.setRequiredIndicatorVisible(true);

		FormLayout saleForm = ViewHelper.buildForm("", false, false);
		saleForm.addComponents(txtInitialBalance, txtFee, txtFinalBalance);
		Panel salePanel = ViewHelper.buildPanel("Recaudo", saleForm);
		layout.addComponents(salePanel);

		// -------------------------------------------------------------------------

		setFieldValues(collection);
		// ----------------------------------------------------------------------------------

		return layout;
	}

	private void setFieldValues(Collection collection) {

		if (collection != null) {
			selectedDocument = collection.getDocument();

			txtDocumentNumber.setValue(collection.getDocument().getCode());
			txtDocumentDate.setValue(DateUtil.dateToString(collection.getDocument().getDocumentDate()));
			txtPerson.setValue(collection.getDocument().getPerson().getName() + " "
					+ collection.getDocument().getPerson().getLastName());
			txtDocumentValue.setValue(String.valueOf(collection.getDocument().getTotalValue()));

			txtInitialBalance.setValue(String.valueOf(collection.getInitialBalance()));
			txtFee.setValue(String.valueOf(collection.getFee()));
			txtFinalBalance.setValue(String.valueOf(collection.getFinalBalance()));
		}

	}

	/**
	 * Calcular el saldo
	 */
	private void setFinalBalance() {
		String strLog = "[setFinalBalance]";
		try {
			String fee = txtFee.getValue();
			String initialBalance = txtInitialBalance.getValue();
			if (fee != null && !fee.isEmpty() && initialBalance != null && !initialBalance.isEmpty()) {
				BigDecimal finalBalance = NumericUtil.stringToBigDecimal(initialBalance)
						.subtract(NumericUtil.stringToBigDecimal(fee));
				txtFinalBalance.setValue(String.valueOf(finalBalance));
			}
		} catch (Exception e) {
			log.error(strLog + "[Exception]" + e.getMessage());
			ViewHelper.showNotification("Se presentó un error al calcular el saldo de la deuda",
					Notification.Type.ERROR_MESSAGE);
		}

	}

	@Override
	protected void fillGridData() {
		List<Collection> collectionList = collectionBll.selectAll();

		collectionList = collectionList.stream().sorted(Comparator.comparing(Collection::getCollectionDate).reversed())
				.collect(Collectors.toList());

		collectionDataProvider = new ListDataProvider<>(collectionList);

		collectionGrid.setDataProvider(collectionDataProvider);
		collectionDataProvider.addDataProviderListener(
				event -> footer.getCell(totalColumn).setHtml(calculateTotal(collectionDataProvider)));
		refreshGrid();
	}

	@Override
	protected void saveButtonAction(Collection entity) {
		String message = validateRequiredFields();
		if (!message.isEmpty()) {
			ViewHelper.showNotification(message, Notification.Type.ERROR_MESSAGE);
		} else {
			// Guardar recaudo
			saveCollection(entity);
			// Actualizar conciliación (cuadre de caja) por día y empleado
			// saveConciliation();
		}
	}

	private void saveCollection(Collection entity) {
		String strLog = "[saveButtonAction]";
		try {
			Collection.Builder collectionBuilder = null;
			if (entity == null) {
				collectionBuilder = Collection.builder();
			} else {
				collectionBuilder = Collection.builder(entity);
			}

			Date collectionDate = DateUtil.localDateTimeToDate(dtfCollectionDate.getValue());
			BigDecimal finalBalance = NumericUtil.stringToBigDecimal(txtFinalBalance.getValue());
			BigDecimal fee = NumericUtil.stringToBigDecimal(txtFee.getValue());

			entity = collectionBuilder.document(selectedDocument).collectionDate(collectionDate)
					.initialBalance(NumericUtil.stringToBigDecimal(txtInitialBalance.getValue())).fee(fee)
					.finalBalance(finalBalance).archived(false).build();

			save(collectionBll, entity, "Recaudo guardado");

			// Actualizar monto pagado y estado de la factura
			if (finalBalance.equals(new BigDecimal(0.0))) {
				log.info(strLog + "Se actualiza estado de la factura a PAGADA");
				selectedDocument.setPaymentStatus(EPaymentStatus.PAYED.getName());
			}
			log.info(strLog + "Se actualiza el total pagado de la factura");
			BigDecimal payValue = selectedDocument.getPayValue() != null
					? NumericUtil.doubleToBigDecimal(selectedDocument.getPayValue())
					: BigDecimal.ZERO;
			selectedDocument.setPayValue(NumericUtil.bigDecimalToDouble(payValue.add(fee)));

			updateDocument(selectedDocument);

		} catch (Exception e) {
			log.error(strLog + "[Exception]" + e.getMessage());
			e.printStackTrace();
			ViewHelper.showNotification("Se generó un error al guardar el recaudo", Notification.Type.ERROR_MESSAGE);
		}

	}

	/*
	 * Metodo para actualizar el estado de pago de la factura
	 */
	private void updateDocument(Document document) {
		String strLog = "[updateDocument] ";
		try {
			documentBll.save(document);
			log.info(strLog + "Documento actualizado: " + document);

		} catch (Exception e) {
			log.error(strLog + "[Exception]" + e.getMessage());
		}

	}

	private String validateRequiredFields() {
		String message = "";
		String character = "|";

		if (txtDocumentNumber.getValue() == null || txtDocumentNumber.getValue().isEmpty()) {
			if (!message.isEmpty()) {
				message = message.concat(character);
			}
			message = message.concat("La factura es obligatoria");
		}
		if (dtfCollectionDate.getValue() == null) {
			if (!message.isEmpty()) {
				message = message.concat(character);
			}
			message = message.concat("La fecha obligatoria");
		}

		if (txtFee.getValue() == null) {
			if (!message.isEmpty()) {
				message = message.concat(character);
			}
			message = message.concat("El valor del abono obligatorio");
		}

		return message;

	}

	/*
	 * 
	 * Actualizar conciliación (cuadre de caja) por día y empleado
	 */
	private void saveConciliation() {
		String strLog = "[saveConciliation] ";
		try {
			Date conciliationDate = DateUtil.localDateTimeToDate(dtfCollectionDate.getValue());
			conciliationDate = DateUtils.truncate(conciliationDate, Calendar.DATE);
			new CashConciliationLayout().saveDailyConciliation(user, conciliationDate);
		} catch (IOException e) {
			log.error(strLog + "Error al actualizar conciliación: " + e.getMessage());
			e.printStackTrace();
		}
	}

	@Override
	public Collection getSelected() {
		Collection conciliationObj = null;
		Set<Collection> conciliationSet = collectionGrid.getSelectedItems();
		if (conciliationSet != null && !conciliationSet.isEmpty()) {
			conciliationObj = (Collection) conciliationSet.toArray()[0];
		}
		return conciliationObj;
	}

	@Override
	protected void delete(Collection entity) {

	}

	private Panel builTotalPanel() {
		HorizontalLayout layout = ViewHelper.buildHorizontalLayout(true, true);

		txtQuantity = new TextField("Cantidad:");
		txtQuantity.setReadOnly(true);
		txtQuantity.setStyleName(ValoTheme.TEXTFIELD_TINY);

		txtTotal = new TextField("Total:");
		txtTotal.setReadOnly(true);
		txtTotal.setStyleName(ValoTheme.TEXTFIELD_TINY);

		layout.addComponents(txtQuantity, txtTotal);
		return ViewHelper.buildPanel(null, layout);
	}

	private Panel buildFilterPanel() {
		HorizontalLayout layout = ViewHelper.buildHorizontalLayout(true, true);
		txFilterByName = new TextField("Nombre");
		txFilterByName.addValueChangeListener(e -> refreshGrid());
		txFilterByCode = new TextField("Código");
		txFilterByCode.addValueChangeListener(e -> refreshGrid());

		txtFilterByPerson = new TextField("Cliente");
		txtFilterByPerson.addValueChangeListener(e -> refreshGrid());
		txtFilterByPerson.setStyleName(ValoTheme.TEXTFIELD_TINY);
		txtFilterByPerson.focus();

		Button searchPersonBtn = new Button("Buscar", FontAwesome.SEARCH);
		searchPersonBtn.addClickListener(e -> buildPersonWindow(txtFilterByPerson.getValue()));
		searchPersonBtn.setStyleName("icon-only");

		dtfFilterIniDate = new DateTimeField("Fecha inicial");
		dtfFilterIniDate.setValue(DateUtil.getDefaultIniMonthDateTime());
		dtfFilterIniDate.setDateFormat(Commons.FORMAT_DATE_TIME);
		dtfFilterIniDate.setStyleName(ValoTheme.DATEFIELD_TINY);
		dtfFilterIniDate.setRequiredIndicatorVisible(true);
		dtfFilterIniDate.addValueChangeListener(e -> refreshGrid());

		dtfFilterEndDate = new DateTimeField("Fecha final");
		dtfFilterEndDate.setValue(DateUtil.getDefaultEndDateTime());
		dtfFilterEndDate.setDateFormat(Commons.FORMAT_DATE_TIME);
		dtfFilterEndDate.setStyleName(ValoTheme.DATEFIELD_TINY);
		dtfFilterEndDate.setRequiredIndicatorVisible(true);
		dtfFilterEndDate.addValueChangeListener(e -> refreshGrid());

		checkFilterBalance = new CheckBox("Saldo 0");
		checkFilterBalance.setStyleName(ValoTheme.CHECKBOX_SMALL);
		checkFilterBalance.addValueChangeListener(e -> refreshGrid());

		layout.addComponents(txtFilterByPerson, searchPersonBtn, dtfFilterIniDate, dtfFilterEndDate,
				checkFilterBalance);
		layout.setComponentAlignment(searchPersonBtn, Alignment.BOTTOM_CENTER);
		layout.setComponentAlignment(checkFilterBalance, Alignment.BOTTOM_CENTER);
		return ViewHelper.buildPanel("Buscar por", layout);
	}

	private void refreshGrid() {
		collectionDataProvider.setFilter(collection -> filterGrid(collection));
		// collectionGrid.getDataProvider().refreshAll();
	}

	private boolean filterGrid(Collection collection) {

		boolean result = false;
		try {

			Date iniDateFilter = dtfFilterIniDate.getValue() != null
					? DateUtil.localDateTimeToDate(dtfFilterIniDate.getValue())
					: DateUtil.localDateToDate(DateUtil.getDefaultIniMonthDate());

			Date endDateFilter = dtfFilterEndDate.getValue() != null
					? DateUtil.localDateTimeToDate(dtfFilterEndDate.getValue())
					: DateUtil.getDefaultEndDate();

			if (endDateFilter.before(iniDateFilter)) {
				throw new Exception("La fecha final debe ser mayor que la inicial");
			}

			// Person personFilter = !txtFilterByPerson.isEmpty() ? personSelected : null;
			String personFilter = txtFilterByPerson.getValue().toUpperCase();

			boolean isZero = checkFilterBalance.getValue();

			result = collection.getCollectionDate().before(endDateFilter)
					&& collection.getCollectionDate().after(iniDateFilter)
					&& (!isZero ? (collection.getFinalBalance().compareTo(BigDecimal.ZERO) == 1) : true);

			///Filtro por el nombre del cliente
			if (personFilter != null && !personFilter.isEmpty()) {
				Person person = collection.getDocument().getPerson();
				result = result
						&& (StringUtility.concatName(person.getName(), person.getLastName())).contains(personFilter);
			}

		} catch (Exception e) {
			ViewHelper.showNotification(e.getMessage(), Notification.Type.ERROR_MESSAGE);
		}
		return result;

	}

	/**
	 * Metodo para construir la venta modal con la lista de facturas
	 * 
	 * @param invoiceFilter
	 */

	private void builInvoiceWindow(String invoiceFilter) {

		invoiceWindow = ViewHelper.buildSubwindow("80%", null);
		invoiceWindow.setCaption("Facturas por pagar pendientes");

		invoiceWindow.addCloseListener(e -> closeWindow(invoiceWindow));
		VerticalLayout subContent = ViewHelper.buildVerticalLayout(true, true);

		try {
			CommonsConstants.TRANSACTION_TYPE = ETransactionType.ENTRADA.getName();

			invoicListLayout = new InvoiceReportLayout(ELayoutMode.LIST);
			invoicListLayout.getCbFilterPaymentStatus().setValue(EPaymentStatus.PENDING);

			invoicListLayout.getGrid().addItemClickListener(listener -> {
				if (listener.getMouseEventDetails().isDoubleClick())
					selectDocument(listener.getItem());
			});

		} catch (IOException e) {
			log.error("Error al cargar lista de facturas. Exception:" + e);
		}
		Panel invoicePanel = ViewHelper.buildPanel(null, invoicListLayout);
		subContent.addComponents(invoicePanel);

		invoiceWindow.setContent(subContent);
		getUI().addWindow(invoiceWindow);

	}

	private void closeWindow(Window w) {
		w.close();
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
			log.info(strLog + " docs filtrados cant ->" + documentDataProvider.getItems().size());
		} catch (Exception e) {
			log.error(strLog + "[Exception]" + e.getMessage());
		}
	}

	private Double getSumDailyCreditSales() {
		String strLog = "[getSumDailyCreditSales]";
		Double totalCreditSale = null;
		try {
			getDailySalesData();
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
			// documentDataProvider = new
			// ListDataProvider<>(documentBll.select(paymentType));
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
			Date endDateFilter = DateUtil.localDateTimeToDate(DateUtil.getDefaultEndDateTime());

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

	/**
	 * Metodo para seleccionar el documento al que se cargará el recaudo
	 * 
	 * @param document
	 */
	private void selectDocument(Document document) {
		String strLog = "[selectDocument] ";
		try {
			log.info(strLog + "[parameters] " + document);
			selectedDocument = document;
			if (selectedDocument != null) {
				txtDocumentNumber.setValue(selectedDocument.getCode());
				txtDocumentDate.setValue(DateUtil.dateToString(selectedDocument.getDocumentDate()));
				txtDocumentValue.setValue(selectedDocument.getTotalValue());
				txtPerson.setValue(selectedDocument.getPerson().getDocumentNumber() + " -  "
						+ selectedDocument.getPerson().getName() + " " + selectedDocument.getPerson().getLastName());

				// Obtener los recaudos hechos a la factura para tomar el último saldo
				txtInitialBalance.setValue(getBalaceDocument(selectedDocument));

			}
			invoiceWindow.close();
		} catch (Exception e) {
			log.error(strLog + "[Exception]" + e.getMessage());
		}
	}

	/**
	 * Obtener el último saldo de una factura
	 * 
	 * @param document
	 * @return
	 */
	private Double getBalaceDocument(Document document) {
		String strLog = "[getBalaceDocument] ";
		Double finalBalance = 0.0;
		try {
			List<Collection> collections = collectionBll.select(selectedDocument);
			finalBalance = document.getTotalValue();
			if (collections != null && !collections.isEmpty()) {
				// Obtener la última factura, la lista está ordenada descendentemente
				Collection collection = collections.get(0);
				finalBalance = NumericUtil.bigDecimalToDouble((BigDecimal) collection.getFinalBalance());
			}
		} catch (Exception e) {
			log.error(strLog + "[Exception] " + e.getMessage());
			e.printStackTrace();
		}
		return finalBalance;
	}

	/**
	 * Construir venta para seleccionar persona
	 * 
	 * @param personFiltter
	 */
	private void buildPersonWindow(String personFiltter) {

		personSubwindow = ViewHelper.buildSubwindow("75%", null);
		personSubwindow.setCaption("Proveedores ");
		personSubwindow.addCloseListener(e -> closeWindow(personSubwindow));

		VerticalLayout subContent = ViewHelper.buildVerticalLayout(true, true);

		try {

			Commons.PERSON_TYPE = PersonType.SUPPLIER.getName();
			personLayout = new PersonLayout(true);

			personLayout.getGrid().addItemClickListener(listener -> {
				if (listener.getMouseEventDetails().isDoubleClick())
					selectPerson(listener.getItem());
			});

		} catch (IOException e) {
			log.error("Error al cargar lista de proveedores. Exception:" + e);
		}
		Panel personPanel = ViewHelper.buildPanel(null, personLayout);
		subContent.addComponents(personPanel);

		personSubwindow.setContent(subContent);
		getUI().addWindow(personSubwindow);

	}

	/**
	 * Método para seleccionar proveedor o cliente
	 */
	private void selectPerson(Person person) {
		personSelected = person;

		if (personSelected != null) {
			txtFilterByPerson.setValue(personSelected.getName() + " " + personSelected.getLastName());
			personSubwindow.close();
			refreshGrid();

		} else {
			ViewHelper.showNotification("Seleccione un proveedor", Notification.Type.WARNING_MESSAGE);
		}

	}

	private String calculateTotal(ListDataProvider<Collection> dataProvider) {
		String strLog = "[calculateTotal]";
		String total = null;
		try {

			total = String.valueOf(dataProvider.fetch(new Query<>()).mapToDouble(collection -> {
				return NumericUtil.bigDecimalToDouble(collection.getFinalBalance());
			}).sum());
			String quantity = String.valueOf(dataProvider.size(new Query<>()));
			txtQuantity.setValue(quantity);
			txtTotal.setValue(total);
			log.info(strLog + "total: " + total);
		} catch (Exception e) {
			log.error(strLog + "[Exception]" + e.getMessage());
		}
		return "<b>" + total + "</b>";

	}

}
