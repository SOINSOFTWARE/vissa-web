package com.soinsoftware.vissa.web;

import static com.soinsoftware.vissa.web.VissaUI.KEY_CASH_CONCILIATION;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.vaadin.ui.NumberField;

import com.soinsoftware.vissa.bll.CollectionBll;
import com.soinsoftware.vissa.bll.DocumentBll;
import com.soinsoftware.vissa.bll.DocumentTypeBll;
import com.soinsoftware.vissa.bll.PaymentTypeBll;
import com.soinsoftware.vissa.model.Collection;
import com.soinsoftware.vissa.model.Document;
import com.soinsoftware.vissa.model.DocumentType;
import com.soinsoftware.vissa.model.EPaymemtType;
import com.soinsoftware.vissa.model.PaymentType;
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
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.DateTimeField;
import com.vaadin.ui.FormLayout;
import com.vaadin.ui.Grid;
import com.vaadin.ui.Grid.SelectionMode;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Panel;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.themes.ValoTheme;

@SuppressWarnings("unchecked")
public class CollectionLayout extends AbstractEditableLayout<Collection> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5076502522106126046L;

	protected static final Logger log = Logger.getLogger(CollectionLayout.class);

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

	private Window personSubwindow;
	private PersonLayout personLayout = null;
	private Document selectedDocument = null;

	private User user;
	private String role;

	private ListDataProvider<Document> documentDataProvider;
	private ConfigurableFilterDataProvider<Collection, Void, SerializablePredicate<Collection>> filterProductDataProvider;

	public CollectionLayout() throws IOException {
		super("Recaudo", KEY_CASH_CONCILIATION);
		collectionBll = CollectionBll.getInstance();
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
		Panel filterPanel = buildFilterPanel();
		Panel dataPanel = buildGridPanel();
		layout.addComponents(buttonPanel, dataPanel);
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
		collectionGrid.addColumn(Collection::getCollectionDate).setCaption("Fecha");

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
		collectionGrid.addColumn(Collection::getFee).setCaption("Valor abono");
		collectionGrid.addColumn(Collection::getFinalBalance).setCaption("Saldo final");

		layout.addComponent(ViewHelper.buildPanel(null, collectionGrid));
		fillGridData();
		return ViewHelper.buildPanel(null, layout);
	}

	@SuppressWarnings("deprecation")
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
		txtDocumentNumber.setWidth("50%");

		txtDocumentDate = new TextField("Fecha de factura");
		txtDocumentDate.setStyleName(ValoTheme.TEXTFIELD_TINY);
		txtDocumentDate.setWidth("50%");

		txtPerson = new TextField("Cliente");
		txtPerson.setStyleName(ValoTheme.TEXTFIELD_TINY);
		txtPerson.setWidth("50%");

		txtDocumentValue = new NumberField("Valor factura");
		txtDocumentValue.setStyleName(ValoTheme.TEXTFIELD_TINY);
		txtDocumentValue.setReadOnly(true);
		txtDocumentValue.setWidth("50%");

		basicForm.addComponents(dtfCollectionDate, txtDocumentNumber, txtDocumentDate, txtPerson, txtDocumentValue);

		Panel basicPanel = ViewHelper.buildPanel("", basicForm);
		layout.addComponents(basicPanel);

		// -------------------------------------------------------------------------
		// Datos del recaudo
		txtInitialBalance = new NumberField("Saldo inicial");
		txtInitialBalance.setStyleName(ValoTheme.TEXTFIELD_TINY);
		txtInitialBalance.setWidth("50%");
		txtInitialBalance.setReadOnly(true);
		// txtInitialBalance.addValueChangeListener(e -> setTotalSale());

		txtFee = new NumberField("Abono");
		txtFee.setStyleName(ValoTheme.TEXTFIELD_TINY);
		txtFee.setReadOnly(true);
		txtFee.setWidth("50%");

		txtFinalBalance = new NumberField("Saldo final");
		txtFinalBalance.setStyleName(ValoTheme.TEXTFIELD_TINY);
		txtFinalBalance.setWidth("50%");
		txtFinalBalance.setReadOnly(true);
		// txtFinalBalance.addValueChangeListener(e -> setTotalSale());

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

			txtInitialBalance.setValue(String.valueOf(collection.getInitialBalance()));
			txtFee.setValue(String.valueOf(collection.getFee()));
			txtFinalBalance.setValue(String.valueOf(collection.getFinalBalance()));
		}

	}

	@Override
	protected void fillGridData() {
		List<Collection> concilitationList = null;

		concilitationList = collectionBll.selectAll();

		ListDataProvider<Collection> dataProvider = new ListDataProvider<>(concilitationList);
		filterProductDataProvider = dataProvider.withConfigurableFilter();
		collectionGrid.setDataProvider(filterProductDataProvider);

	}

	@Override
	protected void saveButtonAction(Collection entity) {
		String message = validateRequiredFields();
		if (!message.isEmpty()) {
			ViewHelper.showNotification(message, Notification.Type.ERROR_MESSAGE);
		} else {
			saveCollection(entity);
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

			entity = collectionBuilder.document(selectedDocument).collectionDate(collectionDate)
					.initialBalance(NumericUtil.stringToBigDecimal(txtInitialBalance.getValue()))
					.fee(NumericUtil.stringToBigDecimal(txtFee.getValue()))
					.finalBalance(NumericUtil.stringToBigDecimal(txtFinalBalance.getValue())).archived(false).build();

			save(collectionBll, entity, "Recaudo guardado");
		} catch (Exception e) {
			log.error(strLog + "[Exception]" + e.getMessage());
			e.printStackTrace();
			ViewHelper.showNotification("Se generó un error al guardar el recaudo", Notification.Type.ERROR_MESSAGE);
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
		collectionGrid.getDataProvider().refreshAll();
	}

	private SerializablePredicate<Collection> filterGrid() {
		SerializablePredicate<Collection> columnPredicate = null;
		String codeFilter = txFilterByCode.getValue().trim();
		String nameFilter = txFilterByName.getValue().trim();
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
		// selectBtn.addClickListener(e -> selectPerson());

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
