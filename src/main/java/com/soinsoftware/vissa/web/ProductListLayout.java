package com.soinsoftware.vissa.web;

import static com.soinsoftware.vissa.web.VissaUI.KEY_INVOICES;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.log4j.Logger;
import org.springframework.transaction.annotation.Transactional;

import com.soinsoftware.vissa.bll.DocumentBll;
import com.soinsoftware.vissa.bll.DocumentTypeBll;
import com.soinsoftware.vissa.model.Document;
import com.soinsoftware.vissa.model.DocumentType;
import com.soinsoftware.vissa.model.Person;
import com.soinsoftware.vissa.model.PersonType;
import com.soinsoftware.vissa.model.TransactionType;
import com.soinsoftware.vissa.util.Commons;
import com.soinsoftware.vissa.util.DateUtil;
import com.soinsoftware.vissa.util.ViewHelper;
import com.vaadin.data.provider.ConfigurableFilterDataProvider;
import com.vaadin.data.provider.ListDataProvider;
import com.vaadin.data.provider.Query;
import com.vaadin.server.FontAwesome;
import com.vaadin.server.SerializablePredicate;
import com.vaadin.shared.ui.datefield.DateTimeResolution;
import com.vaadin.ui.AbstractOrderedLayout;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.DateTimeField;
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
public class ProductListLayout extends AbstractEditableLayout<Document> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5076502522106126046L;

	protected static final Logger log = Logger.getLogger(ProductListLayout.class);

	private final DocumentBll documentBll;
	private final DocumentTypeBll documentTypeBll;

	private TextField txtFilterByCode;
	private ComboBox<DocumentType> cbFilterByType;
	private TextField txtFilterByPerson;
	private DateTimeField dtfFilterIniDate;
	private DateTimeField dtfFilterEndDate;
	private TextField txtTotal;
	private Grid<Document> grid;

	private boolean listMode;
	private TransactionType transactionType;

	private Window personSubwindow;
	private PersonLayout personLayout = null;
	private Person personSelected = null;
	private DocumentType documentType;

	private ConfigurableFilterDataProvider<Document, Void, SerializablePredicate<Document>> filterDataProvider;

	public ProductListLayout() throws IOException {
		super("", KEY_INVOICES);

		documentBll = DocumentBll.getInstance();
		documentTypeBll = DocumentTypeBll.getInstance();
		transactionType = TransactionType.valueOf(Commons.TRANSACTION_TYPE);

		if (transactionType.equals(TransactionType.ENTRADA)) {
			Commons.PERSON_TYPE = PersonType.SUPPLIER.getName();
			documentType = documentTypeBll.select("CO");
		}
		if (transactionType.equals(TransactionType.SALIDA)) {
			Commons.PERSON_TYPE = PersonType.CUSTOMER.getName();
			documentType = documentTypeBll.select("VE");
		}

	}

	@Override
	protected AbstractOrderedLayout buildListView() {
		VerticalLayout layout = ViewHelper.buildVerticalLayout(false, false);
		Panel buttonPanel = null;
		if (listMode) {
			buttonPanel = buildButtonPanelListMode();
		} else {
			buttonPanel = buildButtonPanelForLists();
		}
		Panel filterPanel = buildFilterPanel();
		Panel totalPanel = builTotalPanel();
		Panel dataPanel = buildGridPanel();
		layout.addComponents(buttonPanel, filterPanel, totalPanel, dataPanel);
		this.setSpacing(false);
		this.setMargin(false);
		return layout;
	}

	@Override
	protected AbstractOrderedLayout buildEditionView(Document entity) {
		VerticalLayout layout = ViewHelper.buildVerticalLayout(false, false);
		Panel buttonPanel = buildButtonPanelForEdition(entity);
		Component dataPanel = buildEditionComponent(entity);
		Panel buttonPanel2 = buildButtonPanelForEdition(entity);
		layout.addComponents(buttonPanel, dataPanel, buttonPanel2);
		return layout;
	}

	@Override
	protected Panel buildGridPanel() {
		VerticalLayout layout = ViewHelper.buildVerticalLayout(true, true);
		grid = ViewHelper.buildGrid(SelectionMode.SINGLE);
		grid.addColumn(Document::getCode).setCaption("Número");
		grid.addColumn(document -> {
			if (document.getDocumentType() != null) {
				return document.getDocumentType().getName();
			} else {
				return "";
			}
		}).setCaption("Tipo");
		grid.addColumn(Document::getDocumentDate).setCaption("Fecha");

		grid.addColumn(document -> {
			if (document.getPerson() != null) {
				return document.getPerson().getDocumentNumber() + "-" + document.getPerson().getName()
						+ document.getPerson().getLastName();
			} else {
				return null;
			}
		}).setCaption(Commons.PERSON_TYPE);
		grid.addColumn(Document::getTotalValue).setCaption("Total");

		layout.addComponent(ViewHelper.buildPanel(null, grid));
		fillGridData();

		return ViewHelper.buildPanel(null, layout);
	}

	@Override
	protected Component buildEditionComponent(Document person) {
		VerticalLayout layout = ViewHelper.buildVerticalLayout(false, false);

		return layout;
	}

	protected Panel buildButtonPanelListMode() {
		HorizontalLayout layout = ViewHelper.buildHorizontalLayout(true, true);
		// Button btNew = buildButtonForNewAction(ValoTheme.BUTTON_TINY);
		Button btEdit = buildButtonForEditAction("mystyle-btn");
		// Button btDelete = buildButtonForDeleteAction(ValoTheme.BUTTON_TINY);
		layout.addComponents(btEdit);
		return ViewHelper.buildPanel(null, layout);
	}

	protected Panel buildButtonPanelForLists() {
		HorizontalLayout layout = ViewHelper.buildHorizontalLayout(true, true);
		Button btEdit = buildButtonForEditAction("mystyle-btn");

		btEdit.setCaption("Detalle");
		// Button btDelete = buildButtonForDeleteAction("mystyle-btn");
		layout.addComponents(btEdit);
		return ViewHelper.buildPanel(null, layout);
	}

	@Override
	protected void fillGridData() {

		// List<DocumentType> documentTypeList =
		// documentTypeBll.select(transactionType);
		ListDataProvider<Document> dataProvider = new ListDataProvider<>(documentBll.select(documentType));
		filterDataProvider = dataProvider.withConfigurableFilter();
		grid.setDataProvider(filterDataProvider);
		List<Document> documentList = filterDataProvider.fetch(new Query<>()).collect(Collectors.toList());
		txtTotal.setValue(String.valueOf(documentList.size()));

	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	protected void saveButtonAction(Document document) {

	}

	@Override
	public Document getSelected() {
		Document document = null;
		Set<Document> documents = grid.getSelectedItems();
		if (documents != null && !documents.isEmpty()) {
			document = (Document) documents.toArray()[0];
		}
		return document;
	}

	@Override
	protected void delete(Document entity) {
		entity = Document.builder(entity).archived(true).build();
		save(documentBll, entity, "Documento borrado");
	}

	private Panel buildFilterPanel() {
		HorizontalLayout layout = ViewHelper.buildHorizontalLayout(true, true);

		txtFilterByCode = new TextField("Número de fatura");
		txtFilterByCode.addValueChangeListener(e -> refreshGrid());
		txtFilterByCode.setStyleName(ValoTheme.TEXTFIELD_TINY);

		txtFilterByPerson = new TextField(Commons.PERSON_TYPE);
		txtFilterByPerson.addValueChangeListener(e -> refreshGrid());
		txtFilterByPerson.setStyleName(ValoTheme.TEXTFIELD_TINY);
		txtFilterByPerson.focus();
		Button searchPersonBtn = new Button("Buscar proveedor", FontAwesome.SEARCH);
		searchPersonBtn.addClickListener(e -> buildPersonWindow(txtFilterByPerson.getValue()));
		searchPersonBtn.setStyleName("icon-only");

		cbFilterByType = new ComboBox<>("Tipo de factura");
		cbFilterByType.setStyleName(ValoTheme.TEXTFIELD_TINY);
		ListDataProvider<DocumentType> docTypeDataProv = new ListDataProvider<>(
				documentTypeBll.select(transactionType));
		cbFilterByType.setDataProvider(docTypeDataProv);
		cbFilterByType.setItemCaptionGenerator(DocumentType::getName);
		cbFilterByType.addValueChangeListener(e -> refreshGrid());

		dtfFilterIniDate = new DateTimeField("Fecha inicial");
		dtfFilterIniDate.setResolution(DateTimeResolution.SECOND);
		// dtfFilterIniDate.setValue(LocalDateTime.now());
		dtfFilterIniDate.setDateFormat(Commons.FORMAT_DATE);
		dtfFilterIniDate.setStyleName(ValoTheme.DATEFIELD_TINY);
		dtfFilterIniDate.setRequiredIndicatorVisible(true);
		dtfFilterIniDate.addValueChangeListener(e -> refreshGrid());

		dtfFilterEndDate = new DateTimeField("Fecha final");
		dtfFilterEndDate.setResolution(DateTimeResolution.SECOND);
		// dtfFilterEndDate.setValue(LocalDateTime.now());
		dtfFilterEndDate.setDateFormat(Commons.FORMAT_DATE);
		dtfFilterEndDate.setStyleName(ValoTheme.DATEFIELD_TINY);
		dtfFilterEndDate.setRequiredIndicatorVisible(true);
		dtfFilterEndDate.addValueChangeListener(e -> refreshGrid());

		layout.addComponents(txtFilterByPerson, searchPersonBtn, dtfFilterIniDate, dtfFilterEndDate);
		layout.setComponentAlignment(searchPersonBtn, Alignment.BOTTOM_CENTER);
		return ViewHelper.buildPanel("Buscar por", layout);
	}

	private Panel builTotalPanel() {
		HorizontalLayout layout = ViewHelper.buildHorizontalLayout(true, true);

		txtTotal = new TextField("Total facturas:");
		txtTotal.setReadOnly(true);
		txtTotal.setStyleName(ValoTheme.TEXTFIELD_TINY);

		layout.addComponents(txtTotal);
		return ViewHelper.buildPanel(null, layout);
	}

	private void refreshGrid() {
		filterDataProvider.setFilter(filterGrid());
		List<Document> documentList = filterDataProvider.fetch(new Query<>()).collect(Collectors.toList());
		txtTotal.setValue(String.valueOf(documentList.size()));
		grid.getDataProvider().refreshAll();

	}

	private SerializablePredicate<Document> filterGrid() {
		SerializablePredicate<Document> columnPredicate = null;

		try {

			String codeFilter = txtFilterByCode.getValue().trim();

			String docTypeFilter = cbFilterByType.getSelectedItem().isPresent()
					? cbFilterByType.getSelectedItem().get().getName()
					: "";
			Date iniDateFilter = dtfFilterIniDate.getValue() != null
					? DateUtil.localDateTimeToDate(dtfFilterIniDate.getValue())
					: DateUtil.stringToDate("01-01-2000 00:00:00");

			Date endDateFilter = dtfFilterEndDate.getValue() != null
					? DateUtil.localDateTimeToDate(dtfFilterEndDate.getValue())
					: new Date();

			if (endDateFilter.before(iniDateFilter)) {
				throw new Exception("La fecha final debe ser mayor que la inicial");
			} else {

			}
			Person personFilter = !txtFilterByPerson.isEmpty() ? personSelected : null;

			columnPredicate = document -> (personFilter != null ? document.getPerson().equals(personFilter)
					: true && document.getDocumentDate().before(endDateFilter)
							&& document.getDocumentDate().after(iniDateFilter));
		} catch (Exception e) {
			ViewHelper.showNotification(e.getMessage(), Notification.Type.ERROR_MESSAGE);
		}
		return columnPredicate;

	}

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
		personSelected = personLayout.getSelected();

		if (personSelected != null) {
			txtFilterByPerson.setValue(personSelected.getName() + " " + personSelected.getLastName());
			personSubwindow.close();
			refreshGrid();

		} else {
			ViewHelper.showNotification("Seleccione un proveedor", Notification.TYPE_WARNING_MESSAGE);
		}

	}

}
