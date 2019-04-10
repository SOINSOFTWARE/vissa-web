package com.soinsoftware.vissa.web;

import static com.soinsoftware.vissa.web.VissaUI.KEY_REPORTS;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.log4j.Logger;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.springframework.transaction.annotation.Transactional;

import com.soinsoftware.vissa.bll.DocumentBll;
import com.soinsoftware.vissa.bll.DocumentTypeBll;
import com.soinsoftware.vissa.bll.PaymentDocumentTypeBll;
import com.soinsoftware.vissa.common.CommonsUtil;
import com.soinsoftware.vissa.model.Document;
import com.soinsoftware.vissa.model.DocumentType;
import com.soinsoftware.vissa.model.EPaymentStatus;
import com.soinsoftware.vissa.model.ETransactionType;
import com.soinsoftware.vissa.model.PaymentDocumentType;
import com.soinsoftware.vissa.model.PaymentType;
import com.soinsoftware.vissa.model.Person;
import com.soinsoftware.vissa.model.PersonType;
import com.soinsoftware.vissa.util.Commons;
import com.soinsoftware.vissa.util.DateUtil;
import com.soinsoftware.vissa.util.ELayoutMode;
import com.soinsoftware.vissa.util.StringUtil;
import com.soinsoftware.vissa.util.ViewHelper;
import com.vaadin.data.provider.ListDataProvider;
import com.vaadin.data.provider.Query;
import com.vaadin.server.FileDownloader;
import com.vaadin.server.FileResource;
import com.vaadin.server.FontAwesome;
import com.vaadin.shared.ui.datefield.DateTimeResolution;
import com.vaadin.ui.AbstractOrderedLayout;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.DateTimeField;
import com.vaadin.ui.Grid;
import com.vaadin.ui.Grid.Column;
import com.vaadin.ui.Grid.SelectionMode;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Panel;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.components.grid.FooterRow;
import com.vaadin.ui.themes.ValoTheme;

@SuppressWarnings("deprecation")
public class InvoiceReportLayout extends AbstractEditableLayout<Document> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5076502522106126046L;

	protected static final Logger log = Logger.getLogger(InvoiceReportLayout.class);

	private final DocumentBll documentBll;
	private final DocumentTypeBll documentTypeBll;
	private final PaymentDocumentTypeBll paymentDocumentTypeBll;

	private TextField txtFilterByCode;
	private ComboBox<DocumentType> cbFilterByType;
	private TextField txtFilterByPerson;
	private DateTimeField dtfFilterIniDate;
	private DateTimeField dtfFilterEndDate;
	private TextField txtQuantity;
	private TextField txtTotal;
	private ComboBox<PaymentDocumentType> cbFilterPaymentType;
	private ComboBox<EPaymentStatus> cbFilterPaymentStatus;
	private Grid<Document> grid;
	private Button printBtn;

	private ETransactionType transactionType;

	private Window personSubwindow;
	private PersonLayout personLayout = null;
	private Person personSelected = null;
	private DocumentType documentType;
	private FooterRow footer;
	private Column<?, ?> personColumn;
	private Column<?, ?> totalColumn;
	private ELayoutMode layoutMode;

	private ListDataProvider<Document> dataProvider;

	public InvoiceReportLayout() throws IOException {
		super("", KEY_REPORTS);

		documentBll = DocumentBll.getInstance();
		documentTypeBll = DocumentTypeBll.getInstance();
		paymentDocumentTypeBll = PaymentDocumentTypeBll.getInstance();
		transactionType = ETransactionType.valueOf(CommonsUtil.TRANSACTION_TYPE);
		this.layoutMode = ELayoutMode.ALL;

		if (transactionType.equals(ETransactionType.ENTRADA)) {
			Commons.PERSON_TYPE = PersonType.SUPPLIER.getName();
			documentType = documentTypeBll.select("CO");
		}
		if (transactionType.equals(ETransactionType.SALIDA)) {
			Commons.PERSON_TYPE = PersonType.CUSTOMER.getName();
			documentType = documentTypeBll.select("VE");
		}
	}

	public InvoiceReportLayout(ELayoutMode layoutMode) throws IOException {
		super("", KEY_REPORTS);

		this.layoutMode = layoutMode;
		documentBll = DocumentBll.getInstance();
		documentTypeBll = DocumentTypeBll.getInstance();
		paymentDocumentTypeBll = PaymentDocumentTypeBll.getInstance();
		transactionType = ETransactionType.valueOf(CommonsUtil.TRANSACTION_TYPE);

		if (transactionType.equals(ETransactionType.ENTRADA)) {
			Commons.PERSON_TYPE = PersonType.SUPPLIER.getName();
			documentType = documentTypeBll.select("CO");
		}
		if (transactionType.equals(ETransactionType.SALIDA)) {
			Commons.PERSON_TYPE = PersonType.CUSTOMER.getName();
			documentType = documentTypeBll.select("VE");
		}
		addListTab();
	}

	@Override
	protected AbstractOrderedLayout buildListView() {
		VerticalLayout layout = ViewHelper.buildVerticalLayout(false, false);
		/*
		 * Panel buttonPanel = null; if (listMode) { buttonPanel =
		 * buildButtonPanelListMode(); } else { buttonPanel =
		 * buildButtonPanelForLists(); }
		 */
		Panel filterPanel = buildFilterPanel();
		Panel totalPanel = builTotalPanel();
		Panel dataPanel = buildGridPanel();
		layout.addComponents(filterPanel, totalPanel, dataPanel);
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

	@SuppressWarnings("unchecked")
	@Override
	protected Panel buildGridPanel() {
		String strLog = "[buildGridPanel] ";
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
				if (document.getPaymentType() != null) {
					return document.getPaymentType().getName();
				} else {
					return "";
				}
			}).setCaption("Tipo de pago");
			grid.addColumn(document -> {
				if (document.getDocumentDate() != null) {
					return DateUtil.dateToString(document.getDocumentDate());
				} else {
					return "";
				}
			}).setCaption("Fecha");

			personColumn = grid.addColumn(document -> {
				if (document.getPerson() != null) {
					return StringUtil.concatName(document.getPerson().getName(), document.getPerson().getLastName());
				} else {
					return null;
				}
			}).setCaption(Commons.PERSON_TYPE);

			grid.addColumn(Document::getPaymentStatus).setCaption("Estado");
			totalColumn = grid.addColumn(Document::getTotalValue).setCaption("Total");

			grid.addColumn(document -> {
				Double payValue = document.getPayValue() != null ? document.getPayValue() : document.getTotalValue();
				Double balance = document.getTotalValue() - payValue;
				return (Math.round(balance));
			}).setCaption("Saldo");

			footer = grid.prependFooterRow();
			footer.getCell(personColumn).setHtml("<b>Total:</b>");

			layout.addComponent(ViewHelper.buildPanel(null, grid));
			grid.addItemClickListener(listener -> {
				if (listener.getMouseEventDetails().isDoubleClick()) {
					grid.select(listener.getItem());
				}
			});
			fillGridData();
			if (!layoutMode.equals(ELayoutMode.LIST)) {
				refreshGrid();
			}

		} catch (Exception e) {
			log.error(strLog + "[Exception]" + e.getMessage());
			e.printStackTrace();
		}
		return ViewHelper.buildPanel(null, layout);
	}

	@Override
	protected Component buildEditionComponent(Document person) {
		VerticalLayout layout = ViewHelper.buildVerticalLayout(false, false);

		return layout;
	}

	protected Panel buildButtonPanelListMode() {
		HorizontalLayout layout = ViewHelper.buildHorizontalLayout(true, true);
		Button btEdit = buildButtonForEditAction("mystyle-btn");
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
		String strLog = "[fillGridData] ";
		try {
			List<DocumentType> types = documentTypeBll.select(transactionType);
			//Consultar los documentospor los tipos del tipo de tx
			List<Document> documents = documentBll.select(types);
			documents = documents.stream().sorted(Comparator.comparing(Document::getDocumentDate).reversed())
				.collect(Collectors.toList());
			dataProvider = new ListDataProvider<>(documents);

			grid.setDataProvider(dataProvider);
			dataProvider.addDataProviderListener(
					event -> footer.getCell(totalColumn).setHtml(calculateTotal(dataProvider)));
		} catch (Exception e) {
			log.error(strLog + "[Exception]" + e.getMessage());
			e.printStackTrace();
		}
	}

	/*
	 * Metodo para calcular la suma del total de todas las facturas de acuerdo a los
	 * filtros
	 */

	private String calculateTotal(ListDataProvider<Document> detailDataProv) {
		String strLog = "[calculateTotal] ";
		String total = null;
		try {
			log.info(strLog + "[parameters] documentList: " + detailDataProv);
			total = String.valueOf(detailDataProv.fetch(new Query<>()).mapToDouble(Document::getTotalValue).sum());
			String quantity = String.valueOf(detailDataProv.size(new Query<>()));
			txtQuantity.setValue(quantity);
			txtTotal.setValue(total);
		} catch (Exception e) {
			log.error(strLog + "[Exception]" + e.getMessage());
			e.printStackTrace();
		}
		return "<b>" + total + "</b>";

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
		dtfFilterIniDate.setValue(DateUtil.getDefaultIniMonthDateTime());
		dtfFilterIniDate.setDateFormat(Commons.FORMAT_DATE_TIME);
		dtfFilterIniDate.setStyleName(ValoTheme.DATEFIELD_TINY);
		dtfFilterIniDate.setWidth("184px");
		dtfFilterIniDate.setRequiredIndicatorVisible(true);
		dtfFilterIniDate.addValueChangeListener(e -> refreshGrid());

		dtfFilterEndDate = new DateTimeField("Fecha final");
		dtfFilterEndDate.setResolution(DateTimeResolution.SECOND);
		dtfFilterEndDate.setValue(DateUtil.getDefaultEndDateTime());
		dtfFilterEndDate.setDateFormat(Commons.FORMAT_DATE_TIME);
		dtfFilterEndDate.setStyleName(ValoTheme.DATEFIELD_TINY);
		dtfFilterEndDate.setWidth("184px");
		dtfFilterEndDate.setRequiredIndicatorVisible(true);
		dtfFilterEndDate.addValueChangeListener(e -> refreshGrid());

		cbFilterPaymentType = new ComboBox<>("Forma de pago");
		cbFilterPaymentType.setEmptySelectionAllowed(true);
		cbFilterPaymentType.setEmptySelectionCaption("Seleccione");
		cbFilterPaymentType.setStyleName(ValoTheme.COMBOBOX_TINY);

		ListDataProvider<PaymentDocumentType> payTypeDataProv = new ListDataProvider<>(
				paymentDocumentTypeBll.select(documentType));
		cbFilterPaymentType.setDataProvider(payTypeDataProv);
		cbFilterPaymentType.setItemCaptionGenerator(paymentDocumentType -> {
			if (paymentDocumentType != null && paymentDocumentType.getPaymentType() != null) {
				return paymentDocumentType.getPaymentType().getName();
			} else {
				return null;
			}
		});
		cbFilterPaymentType.addValueChangeListener(e -> refreshGrid());

		cbFilterPaymentStatus = new ComboBox<>("Estado de pago");
		cbFilterPaymentStatus.setEmptySelectionAllowed(true);
		cbFilterPaymentStatus.setEmptySelectionCaption("Seleccione");
		cbFilterPaymentStatus.setStyleName(ValoTheme.COMBOBOX_TINY);
		ListDataProvider<EPaymentStatus> payStatusDate = new ListDataProvider<>(Arrays.asList(EPaymentStatus.values()));
		cbFilterPaymentStatus.setDataProvider(payStatusDate);
		cbFilterPaymentStatus.setItemCaptionGenerator(EPaymentStatus::getName);
		cbFilterPaymentStatus.addValueChangeListener(e -> refreshGrid());

		layout.addComponents(txtFilterByPerson, searchPersonBtn, dtfFilterIniDate, dtfFilterEndDate, cbFilterPaymentType,
				cbFilterPaymentStatus);
		layout.setComponentAlignment(searchPersonBtn, Alignment.BOTTOM_CENTER);
		return ViewHelper.buildPanel("Buscar por", layout);
	}

	private Panel builTotalPanel() {
		String strLog = "[builTotalPanel] ";
		HorizontalLayout layout = ViewHelper.buildHorizontalLayout(true, true);
		try {

			txtQuantity = new TextField("Cantidad:");
			txtQuantity.setReadOnly(true);
			txtQuantity.setStyleName(ValoTheme.TEXTFIELD_TINY);

			txtTotal = new TextField("Total:");
			txtTotal.setReadOnly(true);
			txtTotal.setStyleName(ValoTheme.TEXTFIELD_TINY);

			String fileName = "Reporte" + documentType.getName().replaceAll(" ", "");
			File fileTemp = File.createTempFile(fileName, ".xlsx");
			String filePath = fileTemp.getPath();
			log.info(strLog + "filePath:" + filePath);

			printBtn = new Button(FontAwesome.PRINT);
			printBtn.addStyleName(ValoTheme.BUTTON_ICON_ONLY);
			printBtn.addClickListener(e -> printReport(filePath));

			FileDownloader downloader = new FileDownloader(new FileResource(fileTemp));
			downloader.extend(printBtn);

			layout.addComponents(txtQuantity, txtTotal, printBtn);
			layout.setComponentAlignment(printBtn, Alignment.BOTTOM_CENTER);
		} catch (IOException e) {
			log.error(strLog + "[IOException]" + e.getMessage());
			e.printStackTrace();
		}
		return ViewHelper.buildPanel(null, layout);
	}

	private void refreshGrid() {
		if (dataProvider != null) {
			dataProvider.setFilter(document -> filterGrid(document));
		}
	}

	private boolean filterGrid(Document document) {

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
			} else {

			}
			//Person personFilter = !txtFilterByPerson.isEmpty() ? personSelected : null;
			String personFilter = txtFilterByPerson.getValue().toUpperCase();

			PaymentType paymentTypeFilter = null;
			if (cbFilterPaymentType.getSelectedItem().isPresent()) {
				paymentTypeFilter = cbFilterPaymentType.getSelectedItem().get().getPaymentType();
			}

			EPaymentStatus paymentStatusFilter = null;
			if (cbFilterPaymentStatus.getSelectedItem().isPresent()) {
				paymentStatusFilter = cbFilterPaymentStatus.getSelectedItem().get();
			}

			result =  document.getDocumentDate().before(endDateFilter)
							&& document.getDocumentDate().after(iniDateFilter);

			// Filtrar por tipo de pago
			if (paymentTypeFilter != null) {
				result = result && document.getPaymentType().equals(paymentTypeFilter);
			}

			// Filtrar por estado del pago
			if (paymentStatusFilter != null) {
				result = result && (document.getPaymentStatus() != null
						&& document.getPaymentStatus().equals(paymentStatusFilter.getName()));
			}
			
			//Filtrar por el nombre del cliente/proveedor
			if (personFilter != null && !personFilter.isEmpty()) {
				Person person = document.getPerson();
				result = result
						&& (StringUtil.concatName(person.getName(), person.getLastName())).contains(personFilter);
			}
		} catch (Exception e) {
			ViewHelper.showNotification(e.getMessage(), Notification.Type.WARNING_MESSAGE);
			e.printStackTrace();
		}
		return result;

	}

	private void buildPersonWindow(String personFiltter) {

		personSubwindow = ViewHelper.buildSubwindow("75%", null);
		personSubwindow.setCaption("Personas");

		VerticalLayout subContent = ViewHelper.buildVerticalLayout(true, true);

		try {
			personLayout = new PersonLayout(true);
			personLayout.getGrid().addItemClickListener(listener -> {
				if (listener.getMouseEventDetails().isDoubleClick())
					selectPerson(listener.getItem());
			});

		} catch (IOException e) {
			log.error("Error al cargar lista de personas. Exception:" + e);
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
			ViewHelper.showNotification("Seleccione una persona", Notification.Type.WARNING_MESSAGE);
		}

	}

	public Grid<Document> getGrid() {
		return grid;
	}

	public void setGrid(Grid<Document> grid) {
		this.grid = grid;
	}

	@SuppressWarnings("unchecked")
	public void printReport(String fileName) {
		List<Document> documents = grid.getDataProvider().fetch(new Query<>()).collect(Collectors.toList());
		log.info("size: " + documents.size());

		try {
			String sheetName = "Facturas";
			List<String> columns = Arrays.asList("N° FACTURA", "TIPO DE FACTURA", "TIPO DE PAGO", "ESTADO PAGO",
					"FECHA", "CLIENTE", "TOTAL FACTURA");

			InvoiceReportGenerator<Document> excelWriter = new InvoiceReportGenerator<Document>(fileName);
			excelWriter.createSheet(sheetName, columns, documents);
			excelWriter.exportFile();

		} catch (InvalidFormatException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public ComboBox<EPaymentStatus> getCbFilterPaymentStatus() {
		return cbFilterPaymentStatus;
	}

	public void setCbFilterPaymentStatus(ComboBox<EPaymentStatus> cbFilterPaymentStatus) {
		this.cbFilterPaymentStatus = cbFilterPaymentStatus;
	}

	public DateTimeField getDtfFilterIniDate() {
		return dtfFilterIniDate;
	}

	public void setDtfFilterIniDate(DateTimeField dtfFilterIniDate) {
		this.dtfFilterIniDate = dtfFilterIniDate;
	}

	public DateTimeField getDtfFilterEndDate() {
		return dtfFilterEndDate;
	}

	public void setDtfFilterEndDate(DateTimeField dtfFilterEndDate) {
		this.dtfFilterEndDate = dtfFilterEndDate;
	}
	
	

}
