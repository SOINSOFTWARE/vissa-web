package com.soinsoftware.vissa.web;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.persistence.PersistenceException;

import org.apache.commons.lang.time.DateUtils;
import org.apache.log4j.Logger;
import org.hibernate.HibernateException;
import org.springframework.transaction.annotation.Transactional;
import org.vaadin.dialogs.ConfirmDialog;
import org.vaadin.ui.NumberField;

import com.soinsoftware.report.dynamic.GeneratorException;
import com.soinsoftware.report.dynamic.PdfGenerator;
import com.soinsoftware.vissa.bll.CompanyBll;
import com.soinsoftware.vissa.bll.DocumentBll;
import com.soinsoftware.vissa.bll.DocumentDetailLotBll;
import com.soinsoftware.vissa.bll.DocumentStatusBll;
import com.soinsoftware.vissa.bll.DocumentTypeBll;
import com.soinsoftware.vissa.bll.InventoryTransactionBll;
import com.soinsoftware.vissa.bll.LotBll;
import com.soinsoftware.vissa.bll.MeasurementUnitProductBll;
import com.soinsoftware.vissa.bll.MuEquivalenceBll;
import com.soinsoftware.vissa.bll.PaymentDocumentTypeBll;
import com.soinsoftware.vissa.bll.PaymentMethodBll;
import com.soinsoftware.vissa.bll.ProductBll;
import com.soinsoftware.vissa.common.CommonsUtil;
import com.soinsoftware.vissa.exception.ModelValidationException;
import com.soinsoftware.vissa.model.Company;
import com.soinsoftware.vissa.model.Document;
import com.soinsoftware.vissa.model.DocumentDetail;
import com.soinsoftware.vissa.model.DocumentDetailLot;
import com.soinsoftware.vissa.model.DocumentStatus;
import com.soinsoftware.vissa.model.DocumentType;
import com.soinsoftware.vissa.model.EPaymemtType;
import com.soinsoftware.vissa.model.ERole;
import com.soinsoftware.vissa.model.ETransactionType;
import com.soinsoftware.vissa.model.InventoryTransaction;
import com.soinsoftware.vissa.model.Lot;
import com.soinsoftware.vissa.model.MeasurementUnit;
import com.soinsoftware.vissa.model.MeasurementUnitProduct;
import com.soinsoftware.vissa.model.MuEquivalence;
import com.soinsoftware.vissa.model.PaymentDocumentType;
import com.soinsoftware.vissa.model.PaymentMethod;
import com.soinsoftware.vissa.model.PaymentType;
import com.soinsoftware.vissa.model.Person;
import com.soinsoftware.vissa.model.PersonType;
import com.soinsoftware.vissa.model.Product;
import com.soinsoftware.vissa.model.Role;
import com.soinsoftware.vissa.model.User;
import com.soinsoftware.vissa.util.Commons;
import com.soinsoftware.vissa.util.DateUtil;
import com.soinsoftware.vissa.util.ELayoutMode;
import com.soinsoftware.vissa.util.NotificationUtil;
import com.soinsoftware.vissa.util.PermissionUtil;
import com.soinsoftware.vissa.util.ViewHelper;
import com.vaadin.data.Binder;
import com.vaadin.data.Binder.Binding;
import com.vaadin.data.provider.ListDataProvider;
import com.vaadin.data.provider.Query;
import com.vaadin.event.ShortcutAction;
import com.vaadin.event.ShortcutListener;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.server.FileResource;
import com.vaadin.server.FontAwesome;
import com.vaadin.server.Page;
import com.vaadin.server.VaadinService;
import com.vaadin.shared.ui.datefield.DateTimeResolution;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.DateTimeField;
import com.vaadin.ui.Embedded;
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

@SuppressWarnings("deprecation")
public class InvoiceLayout extends VerticalLayout implements View {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5187486966314714738L;
	protected static final Logger log = Logger.getLogger(InvoiceLayout.class);

	// Bll
	private final ProductBll productBll;
	private final PaymentMethodBll paymentMethodBll;
	private final DocumentBll documentBll;
	private final InventoryTransactionBll inventoryBll;
	private final DocumentTypeBll documentTypeBll;
	private final DocumentStatusBll docStatusBll;
	private final LotBll lotBll;
	private final DocumentDetailLotBll detailLotBll;
	private final CompanyBll companyBll;
	private final PaymentDocumentTypeBll paymentDocumentTypeBll;
	private final MeasurementUnitProductBll measurementUnitProductBll;
	private final MuEquivalenceBll muEquivalencesBll;

	// Components
	private TextField txtDocNumFilter;
	private TextField txtDocNumber;
	private TextField txtResolution;
	private TextField txtReference;
	private TextField txtPerson;
	private DateTimeField dtfDocumentDate;
	private ComboBox<PaymentDocumentType> cbPaymentType;
	private ComboBox<DocumentType> cbDocumentType;
	private TextField txtPaymentTerm;
	private DateTimeField dtfExpirationDate;
	private TextField txtTotal;
	private TextField txtTotalTax;
	private ComboBox<PaymentMethod> cbPaymentMethod;
	private ComboBox<DocumentStatus> cbDocumentStatus;
	private Grid<DocumentDetail> detailGrid;
	private Window personSubwindow;
	private Window productSubwindow;
	private Window lotSubwindow;

	private Person selectedPerson = null;
	private Product selectedProduct = null;
	private Lot selectedLot = null;
	private Document document;
	private List<DocumentDetail> itemsList = null;

	private ProductLayout productLayout = null;
	private LotLayout lotLayout = null;
	private PersonLayout personLayout = null;

	private DocumentType documentType;

	private PdfGenerator pdfGenerator = null;

	private HashMap<DocumentDetail, DocumentDetailLot> detailLotMap = new HashMap<DocumentDetail, DocumentDetailLot>();

	private ETransactionType transactionType;

	private ListDataProvider<DocumentDetail> dataProvider;

	private Column<DocumentDetail, String> columnQuantity;
	private Column<?, ?> columnSubtotal;

	private Column<DocumentDetail, String> columnTax;
	private Column<DocumentDetail, String> columnPrice;
	private Column<DocumentDetail, String> columnDiscount;
	private Column<DocumentDetail, MeasurementUnit> columnUM;
	private Column<DocumentDetail, MeasurementUnitProduct> columnUMProd;
	private FooterRow footer;

	private PermissionUtil permissionUtil;

	private User user;
	private Company company;

	private Button printBtn;
	private Role role;
	private PaymentType paymentType;
	private Double payValue;

	private Button addProductBtn;
	private Button deleteProductBtn;
	private Button searchPersonBtn;
	private CashChangeLayout cashChangeLayout;
	private Window cashChangeWindow;
	private int pos;

	public InvoiceLayout() throws IOException {
		super();

		productBll = ProductBll.getInstance();
		paymentMethodBll = PaymentMethodBll.getInstance();
		documentBll = DocumentBll.getInstance();
		inventoryBll = InventoryTransactionBll.getInstance();
		documentTypeBll = DocumentTypeBll.getInstance();
		docStatusBll = DocumentStatusBll.getInstance();
		detailLotBll = DocumentDetailLotBll.getInstance();
		companyBll = CompanyBll.getInstance();
		paymentDocumentTypeBll = PaymentDocumentTypeBll.getInstance();
		measurementUnitProductBll = MeasurementUnitProductBll.getInstance();
		muEquivalencesBll = MuEquivalenceBll.getInstance();
		lotBll = LotBll.getInstance();
		document = new Document();
		itemsList = new ArrayList<DocumentDetail>();
		transactionType = ETransactionType.valueOf(CommonsUtil.TRANSACTION_TYPE);
		company = companyBll.selectAll().get(0);

	}

	@Override
	public void enter(ViewChangeEvent event) {

		View.super.enter(event);

		this.user = getSession().getAttribute(User.class);
		this.role = user.getRole();
		this.permissionUtil = new PermissionUtil(user.getRole().getPermissions());
		// setMargin(true);
		String title = "";
		if (transactionType.equals(ETransactionType.ENTRADA)) {
			title = "Compra";
		} else {
			title = "Venta";
		}
		Label tittle = new Label(title);
		tittle.addStyleName(ValoTheme.LABEL_H3);
		addComponent(tittle);

		// Crear el generador de facturas
		String reportName = null;
		if (transactionType.equals(ETransactionType.ENTRADA)) {
			reportName = Commons.PURCHASE_REPORT_NAME;
		} else if (transactionType.equals(ETransactionType.SALIDA)) {
			reportName = Commons.SALE_REPORT_NAME;
		}
		if (reportName != null && !reportName.isEmpty()) {
			pdfGenerator = new PdfGenerator(
					new File(VaadinService.getCurrent().getBaseDirectory().getAbsolutePath() + reportName), title);
		}

		VerticalLayout layout = ViewHelper.buildVerticalLayout(false, false);

		// Panel de botones
		Panel buttonPanel = buildButtonPanel();

		// Panel de filtros
		Panel filterPanel = buildFilterPanel();

		// Panel de encabezado factura
		Panel headerPanel = buildHeaderPanel();

		// Panel de detalle factura
		Panel detailPanel = buildDetailPanel();

		layout.addComponents(buttonPanel, filterPanel, headerPanel, detailPanel);
		addComponent(layout);
		this.setMargin(false);
		this.setSpacing(false);
		// notificationWindow();
		// Commons.appWindow = getUI();
		// NotificationUtil.showNotification(this);

	}

	public void notificationWindow() {

		VerticalLayout popupContent = new VerticalLayout();
		// popupContent.addComponent(new TextField("Textfield"));
		// popupContent.addComponent(new Button("Button"));
		// PopupView popup = new PopupView("Pop it up", popupContent);
		// popup.setPopupVisible(true);
		Window w = new Window("Recordatorios");
		w.setContent(popupContent);
		w.setPosition(1050, 3);
		w.setWidth("20%");
		w.setHeight("25%");
		getUI().addWindow(w);

	}

	/**
	 * Construcción del panel de botones
	 * 
	 * @return
	 */

	private Panel buildButtonPanel() {

		HorizontalLayout layout = ViewHelper.buildHorizontalLayout(true, true);
		if (permissionUtil.canEdit(Commons.MENU_NAME)) {
			Button newBtn = new Button("Nuevo", FontAwesome.SAVE);
			newBtn.addStyleName("mystyle-btn");
			newBtn.addClickListener(e -> cleanButtonAction());
			layout.addComponents(newBtn);

			Button saveBtn = new Button("Guardar", FontAwesome.SAVE);
			saveBtn.addStyleName("mystyle-btn");
			saveBtn.addClickListener(e -> saveButtonAction(document));
			layout.addComponents(saveBtn);

			Button editBtn = new Button("Edit", FontAwesome.EDIT);
			editBtn.addStyleName("mystyle-btn");
			// editBtn.addClickListener(e -> saveButtonAction(null));
			// layout.addComponents(editBtn);

			layout.addComponents(saveBtn);
		}

		if (permissionUtil.canDelete(Commons.MENU_NAME)) {
			Button deleteBtn = new Button("Cancelar", FontAwesome.ERASER);
			deleteBtn.addStyleName("mystyle-btn");
			deleteBtn.addClickListener(e -> deleteButtonAction());
			layout.addComponents(deleteBtn);
		}

		printBtn = new Button("Imprimir", FontAwesome.PRINT);
		printBtn.addStyleName("mystyle-btn");
		printBtn.addClickListener(e -> printInvoice());
		layout.addComponents(printBtn);

		addComponent(layout);
		return ViewHelper.buildPanel(null, layout);
	}

	/**
	 * Construcción del panel de filtros
	 * 
	 * @return
	 */
	private Panel buildFilterPanel() {
		HorizontalLayout layout = ViewHelper.buildHorizontalLayout(true, true);

		txtDocNumFilter = new TextField("Buscar por N° de factura");
		txtDocNumFilter.addValueChangeListener(e -> searchDocument(txtDocNumFilter.getValue()));
		txtDocNumFilter.setStyleName(ValoTheme.TEXTFIELD_TINY);

		layout.addComponents(txtDocNumFilter);

		return ViewHelper.buildPanel("", layout);
	}

	/**
	 * Construcción del panel de encabezado factura
	 * 
	 * @return
	 */
	private Panel buildHeaderPanel() {
		VerticalLayout verticalLayout = ViewHelper.buildVerticalLayout(true, true);

		cbDocumentType = new ComboBox<DocumentType>("Tipo de factura");
		cbDocumentType.focus();
		cbDocumentType.setEmptySelectionAllowed(false);
		cbDocumentType.setEmptySelectionCaption("Seleccione");
		cbDocumentType.setStyleName(ValoTheme.COMBOBOX_TINY);
		cbDocumentType.setRequiredIndicatorVisible(true);

		ListDataProvider<DocumentType> docTypeDataProv = new ListDataProvider<>(
				documentTypeBll.select(transactionType));
		cbDocumentType.setDataProvider(docTypeDataProv);
		cbDocumentType.setItemCaptionGenerator(DocumentType::getName);
		cbDocumentType.addValueChangeListener(e -> {
			getNextDocumentNumber(cbDocumentType.getValue());
		});

		txtDocNumber = new TextField("N° de factura");
		txtDocNumber.setReadOnly(true);
		txtDocNumber.setStyleName(ValoTheme.TEXTFIELD_TINY);
		txtDocNumber.setWidth("120px");

		txtResolution = new TextField("Resolución de factura");
		txtResolution.setStyleName(ValoTheme.TEXTFIELD_TINY);
		txtResolution.setReadOnly(true);
		txtResolution.setValue(company.getInvoiceResolution());

		txtReference = new TextField("Referencia proveedor");
		txtReference.setStyleName(ValoTheme.TEXTFIELD_TINY);

		String title = "";
		if (transactionType.equals(ETransactionType.ENTRADA)) {
			title = "Proveedor";
		} else {
			title = "Cliente";
		}
		txtPerson = new TextField(title);
		// txtPerson.setWidth("28%");
		txtPerson.setReadOnly(true);
		txtPerson.setRequiredIndicatorVisible(true);
		txtPerson.setStyleName(ValoTheme.TEXTFIELD_TINY);
		searchPersonBtn = new Button("Buscar proveedor", FontAwesome.SEARCH);
		searchPersonBtn.addClickListener(e -> buildPersonWindow(txtPerson.getValue()));
		searchPersonBtn.setStyleName("icon-only");

		dtfDocumentDate = new DateTimeField("Fecha");
		dtfDocumentDate.setResolution(DateTimeResolution.SECOND);
		dtfDocumentDate.setValue(LocalDateTime.now());
		dtfDocumentDate.setDateFormat(Commons.FORMAT_DATE_TIME);
		dtfDocumentDate.setStyleName(ValoTheme.DATEFIELD_TINY);
		dtfDocumentDate.setRequiredIndicatorVisible(true);
		dtfDocumentDate.setWidth("184px");

		cbDocumentStatus = new ComboBox<DocumentStatus>("Estado de la factura");
		cbDocumentStatus.setEmptySelectionAllowed(false);
		cbDocumentStatus.setEmptySelectionCaption("Seleccione");
		ListDataProvider<DocumentStatus> docStatusDataProv = new ListDataProvider<>(docStatusBll.selectAll());
		cbDocumentStatus.setDataProvider(docStatusDataProv);
		cbDocumentStatus.setItemCaptionGenerator(DocumentStatus::getName);
		cbDocumentStatus.setSelectedItem(docStatusBll.select("Nueva").get(0));
		cbDocumentStatus.setStyleName(ValoTheme.COMBOBOX_TINY);
		cbDocumentStatus.setReadOnly(true);
		cbDocumentStatus.setWidth("150px");

		cbPaymentType = new ComboBox<>("Forma de pago");
		cbPaymentType.setEmptySelectionAllowed(false);
		cbPaymentType.setEmptySelectionCaption("Seleccione");
		cbPaymentType.setStyleName(ValoTheme.COMBOBOX_TINY);
		cbPaymentType.setRequiredIndicatorVisible(true);

		txtPaymentTerm = new TextField("Plazo en días");
		txtPaymentTerm.setStyleName(ValoTheme.TEXTFIELD_TINY);
		txtPaymentTerm.setReadOnly(true);
		txtPaymentTerm.setWidth("120px");

		cbPaymentType.addValueChangeListener(e -> {
			if (cbPaymentType.getSelectedItem().get().getPaymentType().getCode()
					.equals(EPaymemtType.CREDIT.getName())) {
				txtPaymentTerm.setReadOnly(false);
			} else {
				txtPaymentTerm.setReadOnly(true);
			}
		});

		dtfExpirationDate = new DateTimeField("Fecha de vencimiento");
		dtfExpirationDate.setReadOnly(true);
		dtfExpirationDate.setDateFormat(Commons.FORMAT_DATE_TIME);
		dtfExpirationDate.setStyleName(ValoTheme.DATEFIELD_TINY);
		dtfExpirationDate.setWidth("184px");
		dtfExpirationDate.setReadOnly(true);

		txtPaymentTerm.addValueChangeListener(e -> {
			setExpirationDate(txtPaymentTerm.getValue());
		});

		cbPaymentMethod = new ComboBox<PaymentMethod>("Método de pago");
		// cbPaymentMethod.setWidth("20%");
		cbPaymentMethod.setEmptySelectionAllowed(false);
		cbPaymentMethod.setEmptySelectionCaption("Seleccione");
		ListDataProvider<PaymentMethod> payMetDataProv = new ListDataProvider<>(paymentMethodBll.selectAll());
		cbPaymentMethod.setDataProvider(payMetDataProv);
		cbPaymentMethod.setItemCaptionGenerator(PaymentMethod::getName);
		cbPaymentMethod.setStyleName(ValoTheme.COMBOBOX_TINY);

		txtTotal = new TextField("Total Factura");
		txtTotal.setReadOnly(true);
		txtTotal.setStyleName(ValoTheme.TEXTFIELD_TINY);

		txtTotalTax = new TextField("Total IVA");
		txtTotalTax.setReadOnly(true);
		txtTotalTax.setStyleName(ValoTheme.TEXTFIELD_TINY);

		HorizontalLayout headerLayout1 = ViewHelper.buildHorizontalLayout(false, false);

		headerLayout1.addComponents(cbDocumentType, txtDocNumber);
		if (transactionType.equals(ETransactionType.SALIDA)) {
			headerLayout1.addComponents(txtResolution);
		} else {
			headerLayout1.addComponents(txtReference);
		}

		if (transactionType.equals(ETransactionType.SALIDA)) {
			DocumentType docType = docTypeDataProv.getItems().iterator().next();
			cbDocumentType.setValue(docType);
			getNextDocumentNumber(docType);
		}

		headerLayout1.addComponents(txtPerson, searchPersonBtn, dtfDocumentDate, cbDocumentStatus);
		headerLayout1.setComponentAlignment(searchPersonBtn, Alignment.BOTTOM_CENTER);

		HorizontalLayout headerLayout2 = ViewHelper.buildHorizontalLayout(false, false);
		headerLayout2.addComponents(cbPaymentType, txtPaymentTerm, dtfExpirationDate, cbPaymentMethod, txtTotal,
				txtTotalTax);

		verticalLayout.addComponents(headerLayout1, headerLayout2);
		verticalLayout.setWidth("100%");

		return ViewHelper.buildPanel(null, verticalLayout);
	}

	/**
	 * Construcción del panel con grid de items
	 * 
	 * @return
	 */
	private Panel buildDetailPanel() {
		VerticalLayout layout = ViewHelper.buildVerticalLayout(true, true);

		HorizontalLayout buttonlayout = ViewHelper.buildHorizontalLayout(false, false);
		addProductBtn = new Button("Agregar ítem", FontAwesome.PLUS);
		addProductBtn.addStyleName(ValoTheme.BUTTON_TINY);
		addProductBtn.addClickListener(e -> addRowToGrid());

		deleteProductBtn = new Button("Eliminar ítem", FontAwesome.ERASER);
		deleteProductBtn.addStyleName(ValoTheme.BUTTON_TINY);
		deleteProductBtn.addClickListener(e -> deleteItemDetail());

		Button newProductBtn = new Button("Crear producto", FontAwesome.ARCHIVE);
		newProductBtn.addStyleName(ValoTheme.BUTTON_TINY);
		newProductBtn.addClickListener(e -> {
			buildProductWindow(ELayoutMode.NEW, new ArrayList<Product>());
		});

		String label = "";
		if (transactionType.equals(ETransactionType.ENTRADA)) {
			label = "Ver producto";
		} else if (transactionType.equals(ETransactionType.SALIDA)) {
			label = "Lista de productos";
		}

		Button listProductBtn = new Button(label, FontAwesome.LIST);
		listProductBtn.addStyleName(ValoTheme.BUTTON_TINY);
		listProductBtn.addClickListener(e -> {

			List<Product> products = null;
			if (transactionType.equals(ETransactionType.SALIDA)) {
				DocumentDetail detail = !detailGrid.getSelectedItems().isEmpty()
						? detailGrid.getSelectedItems().iterator().next()
						: null;
				if (detail != null) {
					products = Arrays.asList(detail.getProduct());
				}
			}

			buildProductWindow(ELayoutMode.LIST, products);
		});

		buttonlayout.addComponents(addProductBtn, deleteProductBtn);
		if (transactionType.equals(ETransactionType.ENTRADA)) {
			buttonlayout.addComponent(newProductBtn);
		}

		buttonlayout.addComponent(listProductBtn);

		layout.addComponents(buttonlayout, builGridPanel());

		return ViewHelper.buildPanel("Productos", layout);
	}

	/**
	 * Metodo para agregar una fila a la grid de productos
	 */
	private void addRowToGrid() {
		DocumentDetail detail = new DocumentDetail();
		itemsList.add(detail);
		detailGrid.focus();
		detailGrid.select(detail);
		fillDetailGridData(itemsList);
	}

	/**
	 * Metodo para construir la grid de los items a facturar
	 * 
	 * @return
	 */
	@SuppressWarnings("unchecked")
	private Component builGridPanel() {
		detailGrid = ViewHelper.buildGrid(SelectionMode.SINGLE);
		detailGrid.setEnabled(true);

		// columns
		TextField txtCode = new TextField();
		detailGrid.addColumn(documentDetail -> {
			if (documentDetail.getProduct() != null) {
				return documentDetail.getProduct().getCode();
			} else {
				return "";
			}
		}).setCaption("Código").setEditorComponent(txtCode, DocumentDetail::setCode);

		TextField txtName = new TextField();
		txtName.setStyleName(ValoTheme.TEXTFIELD_TINY);
		detailGrid.addColumn(documentDetail -> {
			if (documentDetail.getProduct() != null) {
				return documentDetail.getProduct().getName();
			} else {
				return "";
			}
		}).setCaption("Nombre").setEditorComponent(txtName, DocumentDetail::setName);

		// Columna Unidad de medida editable
		columnUM = detailGrid.addColumn(DocumentDetail::getMeasurementUnit).setCaption("Unidad de medida");

		// Columna Unidad de medida Product
		columnUMProd = detailGrid.addColumn(DocumentDetail::getMeasurementUnitProduct).setCaption("Unidad de medida")
				.setHidden(true);

		// Columna Precio editable
		columnPrice = detailGrid.addColumn(DocumentDetail::getPriceStr).setCaption("Precio");

		if (transactionType.equals(ETransactionType.SALIDA)) {
			columnDiscount = detailGrid.addColumn(DocumentDetail::getDiscountStr).setCaption("Descuento")
					.setEditorComponent(new NumberField(), DocumentDetail::setDiscountStr);
		}

		columnTax = detailGrid.addColumn(DocumentDetail::getTaxStr).setCaption("% IVA");

		// Columna cantidad editable
		NumberField txtQuantity = new NumberField();
		columnQuantity = detailGrid.addColumn(DocumentDetail::getQuantity).setCaption("Cantidad")
				.setEditorComponent(txtQuantity, DocumentDetail::setQuantity);

		columnSubtotal = detailGrid.addColumn(documentDetail -> {
			if (documentDetail.getSubtotal() != null) {
				return documentDetail.getSubtotal();
			} else {
				return "";
			}
		}).setCaption("Subtotal");

		detailGrid.getEditor().addOpenListener(e -> {
			log.info("CURRENT DETAIL-" + CommonsUtil.CURRENT_DOCUMENT_DETAIL);
			detailGrid.getSelectionModel().deselectAll();
		});

		// Evento de enter
		txtName.addShortcutListener(new ShortcutListener("Shortcut Name", ShortcutAction.KeyCode.ENTER, null) {

			private static final long serialVersionUID = 6441523733731956234L;

			@Override
			public void handleAction(Object sender, Object target) {
				try {
					if (((TextField) target).equals(txtCode) || ((TextField) target).equals(txtName)) {
						dataProvider.refreshAll();
						searchProduct(txtCode.getValue(), txtName.getValue());
					}
				} catch (Exception e) {
					log.error("[ShortcutListener][handleAction][Exception] " + e.getMessage());
				}
			}
		});

		txtQuantity.setMaxLength(100);
		txtQuantity.setReadOnly(false);
		if (transactionType.equals(ETransactionType.ENTRADA)) {
			txtQuantity.setReadOnly(true);
		}

		txtQuantity.addBlurListener(e -> changeQuantity(txtQuantity.getValue()));
		// txtQuantity.addValueChangeListener(e ->
		// changeQuantity(txtQuantity.getValue()));

		footer = detailGrid.prependFooterRow();
		if (columnDiscount != null) {
			footer.getCell(columnDiscount).setHtml("<b>Total IVA:</b>");
		} else {
			footer.getCell(columnPrice).setHtml("<b>Total IVA:</b>");
		}
		footer.getCell(columnQuantity).setHtml("<b>Total:</b>");

		detailGrid.getEditor().setEnabled(true);

		detailGrid.getEditor().addSaveListener(e -> changeQuantity(txtQuantity.getValue()));

		// initializeGrid();

		HorizontalLayout itemsLayout = ViewHelper.buildHorizontalLayout(false, false);
		itemsLayout.setSizeFull();
		itemsLayout.addComponents(ViewHelper.buildPanel(null, detailGrid));
		return itemsLayout;

	}

	private void initializeGrid() {
		int i = 0;
		while (i < 6) {
			DocumentDetail docDetail = new DocumentDetail();
			docDetail.setIndex(i);
			itemsList.add(docDetail);
			i++;
		}

		log.info(itemsList);
		dataProvider = new ListDataProvider<>(itemsList);
		detailGrid.setDataProvider(dataProvider);
	}

	private void searchProduct(String code, String name) {
		String strLog = "[searchProduct] ";
		List<Product> products = null;
		try {
			log.info(strLog + "[parameters] code: " + code + ", name: " + name);

			if (code != null && !code.isEmpty()) {
				selectedProduct = null;
				Product product = productBll.select(code);
				if (product != null) {
					selectProduct(product);
				} else {
					buildProductWindow(ELayoutMode.LIST, products);
				}
			}
			if (name != null && !name.isEmpty()) {
				products = productBll.selectByName(name);
				int size = products.size();
				log.info(strLog + "Cantidad de productos consultados: " + size);
				if (size == 1) {
					selectProduct(products.get(0));
				} else {
					buildProductWindow(ELayoutMode.LIST, products);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			log.error(strLog + "[Exception]" + e.getMessage());
		}
	}

	/**
	 * Metodo que valida la cantidad ingresada por cada item
	 * 
	 * @param quantity
	 */
	private void changeQuantity(String quantity) {
		String strLog = "[changeQuantity]";
		dataProvider.refreshAll();
		String message = "";
		boolean correct = false;
		Double qty;
		DocumentDetail currentDetail = null;
		try {
			if (quantity != null && !quantity.isEmpty()) {
				qty = Double.parseDouble(quantity);

				if (CommonsUtil.CURRENT_DOCUMENT_DETAIL.getProduct() != null && qty > 0) {
					currentDetail = CommonsUtil.CURRENT_DOCUMENT_DETAIL;
					log.info(strLog + "currentDetail:" + currentDetail);
					// if (!withoutLot) {
					DocumentDetailLot detailLot = detailLotMap.get(currentDetail);
					log.info(strLog + "detailLot:" + detailLot);
					if (detailLot != null) {
						if (transactionType.equals(ETransactionType.SALIDA)
								&& qty > currentDetail.getMeasurementUnitProduct().getStock()) {
							message = "Cantidad ingresada es mayor al stock del producto";
							throw new Exception(message);
						} else {
							Double initialStockLot = 0.0;
							// Si la UM del item es diferente a la del lote, se convierte
							if (!currentDetail.getMeasurementUnit().equals(detailLot.getLot().getMeasurementUnit())) {
								qty = convertStockXMU(qty, currentDetail.getMeasurementUnit(),
										detailLot.getLot().getMeasurementUnit());
								initialStockLot = convertStockXMU(detailLot.getInitialStockLot(),
										currentDetail.getMeasurementUnit(), detailLot.getLot().getMeasurementUnit());
							}

							Double finalStockLot = 0.0;
							if (transactionType.equals(ETransactionType.ENTRADA)) {
								finalStockLot = qty;
							} else if (transactionType.equals(ETransactionType.SALIDA)) {
								finalStockLot = initialStockLot - qty;
							}
							DocumentDetailLot detailLotTmp = DocumentDetailLot.builder(detailLot).quantity(qty)
									.finalStockLot(finalStockLot).build();

							log.info(strLog + "currentDetail actualizado:" + currentDetail);
							log.info(strLog + "detailLotTmp:" + detailLotTmp);
							detailLotMap.put(currentDetail, detailLotTmp);
						}
					}

					correct = true;

				} else {
					if (CommonsUtil.CURRENT_DOCUMENT_DETAIL.getProduct() == null && qty <= 0) {
						message = "La cantidad debe ser mayor a 0";
						throw new Exception(message);
					}
				}
			}

		} catch (NumberFormatException nfe) {
			log.error(strLog + "[NumberFormatException]" + nfe.getMessage());
			message = "Formato de cantidad no valido";
		} catch (Exception e) {
			log.error(strLog + "[Exception]" + e.getMessage());

			message = e.getMessage();
			e.printStackTrace();
		} finally {
			log.info("Correct: " + correct);
			if (!correct && (message != null && !message.isEmpty())) {
				ViewHelper.showNotification(message, Notification.Type.ERROR_MESSAGE);
			}
		}
	}

	/**
	 * Convertir el stock que está en una MU a su equivalencia en otra MU
	 * 
	 * @param quantity
	 * @param muSource
	 * @param muTarget
	 * @return
	 */
	private Double convertStockXMU(Double quantity, MeasurementUnit muSource, MeasurementUnit muTarget) {
		String strLog = "[convertQuantityMU]";
		Double muTargetStock = 0.0;
		try {
			MuEquivalence muEquivalence = muEquivalencesBll.select(muSource, muTarget);
			if (muEquivalence != null) {
				Double sourceFactor = Double.parseDouble(muEquivalence.getMuSourceFactor());
				Double targetFactor = Double.parseDouble(muEquivalence.getMuTargetFactor());
				// Se calcula la equivalencia por la UM
				muTargetStock = (quantity * sourceFactor) * targetFactor;
			}

		} catch (Exception e) {
			log.error(strLog + "[Exception]" + e.getMessage());
		}
		return muTargetStock;
	}

	/**
	 * Metodo para calcular el total de la factura y establecer este valor en el
	 * campo TotalValue
	 * 
	 * @param detailDataProv
	 * @return
	 */
	private String calculateTotal(ListDataProvider<DocumentDetail> detailDataProv) {
		String strLog = "[calculateTotal]";
		try {
			log.info(strLog + "[parameters]" + detailDataProv);
			String total = String.valueOf(detailDataProv.fetch(new Query<>()).mapToDouble(documentDetail -> {
				if (documentDetail.getSubtotal() != null) {
					return documentDetail.getSubtotal();
				} else {
					return 0.0;
				}
			}).sum());

			txtTotal.setValue(total);
			return "<b>" + total + "</b>";
		} catch (Exception e) {
			log.error(strLog + "[Exception]" + e.getMessage());
		}

		return null;
	}

	/**
	 * Metodo para calcular el total de IVA de la factura y establecer este valor en
	 * el campo TotalTax
	 * 
	 * @param detailDataProv
	 * @return
	 */
	private String calculateTotalIVA(ListDataProvider<DocumentDetail> detailDataProv) {
		String strLog = "[calculateTotalIVA]";
		try {
			log.info(strLog + "[parameters]" + detailDataProv);
			String totalIVA = String.valueOf(detailDataProv.fetch(new Query<>()).mapToDouble(documentDetail -> {
				if (documentDetail.getTax() != null) {
					Double qty = 0.0;
					if (documentDetail.getQuantity() != null && !documentDetail.getQuantity().isEmpty()) {
						qty = Double.parseDouble(documentDetail.getQuantity());
					}
					return documentDetail.getTaxValue() * qty;
				} else {
					return 0.0;
				}
			}).sum());
			txtTotalTax.setValue(totalIVA);
			log.info("total iva:" + totalIVA);
			return "<b>" + totalIVA + "</b>";
		} catch (Exception e) {
			log.error(strLog + "[Exception]" + e.getMessage());
		}

		return null;
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
		selectBtn.addClickListener(e -> selectPerson(null));

		HorizontalLayout buttonLayout = ViewHelper.buildHorizontalLayout(true, true);
		buttonLayout.addComponents(backBtn, selectBtn);

		try {

			if (transactionType.equals(ETransactionType.ENTRADA)) {
				Commons.PERSON_TYPE = PersonType.SUPPLIER.getName();
			}
			if (transactionType.equals(ETransactionType.SALIDA)) {
				Commons.PERSON_TYPE = PersonType.CUSTOMER.getName();
			}
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
		selectedPerson = person != null ? person : personLayout.getSelected();

		if (selectedPerson != null) {
			txtPerson.setValue(selectedPerson.getName() + " " + selectedPerson.getLastName());
			personSubwindow.close();
		} else {
			ViewHelper.showNotification("Seleccione una persona", Notification.Type.WARNING_MESSAGE);
		}

	}

	/**
	 * Obtener el número máximo del tipo documento: venta, compra, remision
	 * 
	 * @param docType
	 */
	private void getNextDocumentNumber(DocumentType docType) {
		if (docType != null) {
			DocumentType.Builder docTypeBuilder = DocumentType.builder(docType);
			documentType = docTypeBuilder.sequence(docType.getSequence() + 1).build();
			txtDocNumber.setValue(String.valueOf(documentType.getSequence()));
			ListDataProvider<PaymentDocumentType> payTypeDataProv = new ListDataProvider<>(
					paymentDocumentTypeBll.select(documentType));
			cbPaymentType.setDataProvider(payTypeDataProv);
			cbPaymentType.setItemCaptionGenerator(paymentDocumentType -> {
				if (paymentDocumentType != null && paymentDocumentType.getPaymentType() != null) {
					return paymentDocumentType.getPaymentType().getName();
				} else {
					return null;
				}
			});

		} else {
			ViewHelper.showNotification("El tipo de factura no tiene consecutivo configurado",
					Notification.Type.ERROR_MESSAGE);
		}
	}

	/**
	 * Setear la fecha de vencimiento de acuerdo al plazo
	 * 
	 * @param val
	 */
	private void setExpirationDate(String val) {
		try {
			if (val != null && !val.isEmpty()) {
				Integer paymentTerm = Integer.parseInt(val);
				LocalDateTime docDate = dtfDocumentDate.getValue();
				if (docDate != null) {
					Date iniDate = DateUtil.localDateTimeToDate(docDate);
					Date endDate = DateUtil.addDaysToDate(iniDate, paymentTerm);
					LocalDateTime expDate = DateUtil.dateToLocalDateTime(endDate);
					dtfExpirationDate.setValue(expDate);
				}
			}
		} catch (NumberFormatException nfe) {
			ViewHelper.showNotification("Campo plazo inválido", Notification.Type.ERROR_MESSAGE);
		}
	}

	/**
	 * Metodo que construye la ventana para buscar productos
	 */
	private void buildProductWindow(ELayoutMode layoutMode, List<Product> productList) {
		String strLog = "[buildProductWindow] ";

		try {
			selectedProduct = null;
			selectedLot = null;

			productSubwindow = ViewHelper.buildSubwindow("75%", null);
			productSubwindow.setCaption("Productos");

			VerticalLayout subContent = ViewHelper.buildVerticalLayout(true, true);
			productLayout = new ProductLayout(layoutMode, productList);

			productLayout.getProductGrid().addItemClickListener(listener -> {
				if (listener.getMouseEventDetails().isDoubleClick())
					selectProduct(listener.getItem());
			});

			Panel productPanel = ViewHelper.buildPanel(null, productLayout);
			subContent.addComponents(productPanel);

			productSubwindow.setContent(subContent);
			getUI().addWindow(productSubwindow);
		} catch (IOException e) {
			log.error(strLog + "Error al cargar lista de productos. Exception:" + e);
			e.printStackTrace();
		}
	}

	/**
	 * Metodo para escoger productos a agregar a la factura
	 */
	private void selectProduct(Product product) {
		String strLog = "[selectProduct]";
		try {
			pos = -1;
			selectedLot = null;
			selectedProduct = product != null ? product : productLayout.getSelected();

			if (product != null) {
				pos = itemsList.indexOf(CommonsUtil.CURRENT_DOCUMENT_DETAIL);
				DocumentDetail docDetail = DocumentDetail.builder(CommonsUtil.CURRENT_DOCUMENT_DETAIL).product(product)
						.build();
				CommonsUtil.CURRENT_DOCUMENT_DETAIL.setIndex(pos);
				detailGrid.getSelectionModel().select(docDetail);
				detailGrid.focus();

				if (itemsList.contains(docDetail)) {
					ViewHelper.showNotification("Este producto ya está agregado a la factura",
							Notification.Type.ERROR_MESSAGE);
				} else {
					if (transactionType.equals(ETransactionType.SALIDA)
							&& (product.getStock() == null || product.getStock().equals(0.0))) {
						ViewHelper.showNotification("El producto no tiene stock disponible",
								Notification.Type.ERROR_MESSAGE);
					} else {
						try {
							if (transactionType.equals(ETransactionType.ENTRADA)) {
								// Construir panel de lotes
								buildLotWindow(docDetail);
							} else if (transactionType.equals(ETransactionType.SALIDA)) {
								// Obtener lote más reciente
								Lot lastLot = lotBll.getLastLotWithStockByProduct(product);
								selectLot(docDetail, lastLot);
							}

						} catch (Exception e) {
							log.error(strLog + "[Exception]" + "Error al cargar lotes del producto. Exception: "
									+ e.getMessage());
							e.printStackTrace();
						}
					}
				}

			} else {
				ViewHelper.showNotification("No ha seleccionado un producto", Notification.Type.WARNING_MESSAGE);
			}
		} catch (Exception e) {
			log.error(strLog + "[Exception]" + e.getMessage());
		}
	}

	/**
	 * Metodo para agregar un Item al detalle de una factura
	 * 
	 * @param docDetail
	 */
	private void addItemToDetail(DocumentDetail docDetail) {
		String strLog = "[addItemToDetail]";
		try {
			// Setear unidad de medida por defecto para item
			List<MeasurementUnit> muList = measurementUnitProductBll.selectMuByProduct(docDetail.getProduct());
			if (muList.size() > 0) {
				docDetail.setCode(docDetail.getProduct().getCode());
				docDetail.setMeasurementUnitList(muList);
				MeasurementUnit mu = muList.get(0);
				docDetail.setMeasurementUnit(mu);
				// Setear el precio del impuesto
				MeasurementUnitProduct priceXMu = selectMuXProduct(docDetail.getMeasurementUnit(),
						docDetail.getProduct());
				if (transactionType.equals(ETransactionType.SALIDA)
						&& (priceXMu == null || (priceXMu != null && priceXMu.getFinalPrice().equals(0.0)))) {
					ViewHelper.showNotification("Precio del producto no válido", Notification.Type.ERROR_MESSAGE);
				} else {
					setPriceComponent(priceXMu);
					docDetail.setMeasurementUnitProduct(priceXMu);

					if (transactionType.equals(ETransactionType.ENTRADA)) {
						docDetail.setPrice(priceXMu.getPurchasePrice());
						docDetail.setTax(priceXMu.getPurchaseTax());
					} else if (transactionType.equals(ETransactionType.SALIDA)) {
						docDetail.setPrice(priceXMu.getSalePrice());
						docDetail.setTax(priceXMu.getSaleTax());
					}

					// Actualizar el item
					if (pos >= 0) {
						itemsList.set(pos, docDetail);
					} else {
						itemsList.add(docDetail);
					}
					fillDetailGridData(itemsList);
					buildMeasurementUnitComponent();
					closeWindow(productSubwindow);
				}
			} else {
				ViewHelper.showNotification("El producto no tiene unidad de medida configurada",
						Notification.Type.ERROR_MESSAGE);
			}
		} catch (Exception e) {
			log.error(strLog + e.getLocalizedMessage());
			e.printStackTrace();
			ViewHelper.showNotification("Se presentó un error al agregar el producto", Notification.Type.ERROR_MESSAGE);
		}
	}

	/**
	 * Metodo para agregar un lote al item del detalle de una factura
	 * 
	 * @param docDetail
	 */

	private void addLotToDetail(DocumentDetail docDetail) {
		// Se asocia el lote al registro de detail
		if (selectedLot != null) {
			// Para las compras el stock inicial del lote es 0, pq es nuevo
			Double quantity = 0.0;

			if (transactionType.equals(ETransactionType.SALIDA)) {
				quantity = selectedLot.getQuantity();
			}
			DocumentDetailLot detailLot = DocumentDetailLot.builder().documentDetail(docDetail).lot(selectedLot)
					.initialStockLot(quantity).build();
			detailLotMap.put(docDetail, detailLot);
		}
	}

	/**
	 * Metodo que construye la ventana para buscar lores
	 */
	private void buildLotWindow(DocumentDetail detail) {
		String strLog = "[buildLotWindow]";
		try {
			Product product = detail.getProduct();
			lotSubwindow = ViewHelper.buildSubwindow("70%", "95%");
			lotSubwindow.setCaption("Lotes del producto " + product.getCode() + " - " + product.getName());

			VerticalLayout subContent = ViewHelper.buildVerticalLayout(true, true);

			Commons.LAYOUT_MODE = ELayoutMode.LIST;
			lotLayout = new LotLayout(product, productLayout, transactionType);
			lotLayout.setCaption("Lotes");
			lotLayout.setMargin(false);
			lotLayout.setSpacing(false);
			lotLayout.getLotGrid().addItemClickListener(listener -> {
				if (listener.getMouseEventDetails().isDoubleClick())
					selectLot(detail, listener.getItem());
			});
			subContent.addComponents(lotLayout);

			lotSubwindow.setContent(subContent);
			lotSubwindow.addCloseListener(e -> {
				if (selectedLot == null) {
					if (CommonsUtil.CURRENT_DOCUMENT_DETAIL.getProduct() != null) {
						CommonsUtil.CURRENT_DOCUMENT_DETAIL
								.setCode(CommonsUtil.CURRENT_DOCUMENT_DETAIL.getProduct().getCode());
						itemsList.set(pos, CommonsUtil.CURRENT_DOCUMENT_DETAIL);
					} else {
						// itemsList.set(pos, null);
					}

					fillDetailGridData(itemsList);
					closeWindow(lotSubwindow);
				}
			});

			getUI().addWindow(lotSubwindow);
		} catch (Exception e) {
			log.error(strLog + "[Exception]" + e.getMessage());
		}
	}

	/**
	 * Metodo para escoger lotes para tomar los productos
	 */
	private void selectLot(DocumentDetail detail, Lot lot) {
		String strLog = "[selectLot]";
		try {

			log.info(strLog + "[parameters] detail: " + detail + ", lot: " + lot);

			// Lote seleccionado en la grid
			// selectedProduct = detail.getProduct();
			selectedLot = lot != null ? lot : lotLayout.getSelected();

			// log.info(strLog + "selectedLot:" + selectedLot);

			if (lot != null) {
				if (transactionType.equals(ETransactionType.ENTRADA) && !lot.isNew()) {
					ViewHelper.showNotification("Debe escoger un nuevo lote ", Notification.Type.ERROR_MESSAGE);
				} else {
					if (transactionType.equals(ETransactionType.SALIDA) && lot.getQuantity() <= 0) {
						ViewHelper.showNotification("El lote no tiene un stock productos: " + selectedLot.getQuantity(),
								Notification.Type.ERROR_MESSAGE);
					} else {
						if (transactionType.equals(ETransactionType.ENTRADA)) {
							detail.setQuantity(String.valueOf(lot.getQuantity()));
							detail.setMeasurementUnit(lot.getMeasurementUnit());
						}
						// Se agrega el item al detail de la factura
						addItemToDetail(detail);

						// Se asocia el lote al registro del detail
						addLotToDetail(detail);
						closeWindow(lotSubwindow);
					}
				}
			} else {
				ViewHelper.showNotification("No ha seleccionado lote", Notification.Type.ERROR_MESSAGE);
			}
		} catch (Exception e) {
			log.error(strLog + "[Exception]" + e.getMessage());
		}

	}

	/**
	 * Metodo para llenar la data de la grid de detalle de la factura
	 * 
	 * @param detailList
	 */
	private void fillDetailGridData(List<DocumentDetail> detailList) {
		try {
			itemsList = detailList;
			dataProvider = new ListDataProvider<>(detailList);

			detailGrid.setDataProvider(dataProvider);

			dataProvider.addDataProviderListener(
					event -> footer.getCell(columnSubtotal).setHtml(calculateTotal(dataProvider)));
			dataProvider.addDataProviderListener(
					event -> footer.getCell(columnTax).setHtml(calculateTotalIVA(dataProvider)));

			dataProvider.refreshAll();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Metodo con la acción del botón guardar factura
	 * 
	 * @param documentEntity
	 */

	@Transactional(rollbackFor = Exception.class)
	private void saveButtonAction(Document documentEntity) {

		String message = validateRequiredFields();
		if (!message.isEmpty()) {
			ViewHelper.showNotification(message, Notification.Type.ERROR_MESSAGE);
		} else {
			ConfirmDialog.show(Page.getCurrent().getUI(), "Confirmar", "Está seguro de guardar la factura", "Si", "No",
					e -> {
						if (e.isConfirmed()) {
							paymentType = cbPaymentType.getSelectedItem().get().getPaymentType();
							if (transactionType.equals(ETransactionType.SALIDA)
									&& paymentType.getCode().equals(EPaymemtType.PAID.getName())) {
								// Mostrar ventana para el cambio
								buildCashChangeWindow();
							} else {
								saveInvoice(documentEntity);
							}

							// Actualizar conciliación (cuadre de caja) por día y empleado
							if (this.document != null) {
								saveConciliation();
							}
						}
					});
		}

	}

	/**
	 * Metodo para validar los campos obligatorios para guardar una factura
	 * 
	 * @return
	 */
	private String validateRequiredFields() {
		String message = "";
		String character = "|";

		if (txtTotal.getValue().equals("0.0")) {
			if (!message.isEmpty()) {
				message = message.concat(character);
			}
			message = "Total de la factura no válido: " + txtTotal.getValue();
		}
		if (!cbDocumentType.getSelectedItem().isPresent()) {
			if (!message.isEmpty()) {
				message = message.concat(character);
			}
			message = "El tipo de factura es obligatorio";
		}
		if (txtPerson.getValue() == null || txtPerson.getValue().isEmpty()) {
			if (!message.isEmpty()) {
				message = message.concat(character);
			}
			message = message.concat("El tercero obligatorio");
		}
		if (dtfDocumentDate.getValue() == null) {
			if (!message.isEmpty()) {
				message = message.concat(character);
			}
			message = message.concat("La fecha obligatoria");
		}

		if (!cbPaymentType.getSelectedItem().isPresent()) {
			if (!message.isEmpty()) {
				message = message.concat(character);
			}
			message = message.concat("La forma de pago es obligatoria");
		}

		if (txtTotal.getValue() != null && txtTotal.getValue().equals("0")) {
			if (!message.isEmpty()) {
				message = message.concat(character);
			}
			message = message.concat("El total de la factura es obligatorio");
		}

		return message;
	}

	/**
	 * Metodo para guardar el detalle de una factura
	 * 
	 * @param document
	 */
	public void saveInvoice(Document document) {
		String strLog = "[saveInvoice] ";
		Document documentEntity = saveInvoiceHeader(document);
		log.info(strLog + "Document saved:" + documentEntity);

		if (documentEntity != null) {
			boolean hasErrors = false;
			for (DocumentDetail detail : documentEntity.getDetails()) {

				DocumentDetail.Builder detailBuilder = DocumentDetail.builder();

				// Detail sin relacion al documento
				DocumentDetail detailTmp = detailBuilder.product(detail.getProduct()).quantity(detail.getQuantity())
						.description(detail.getDescription()).subtotal(detail.getSubtotal())
						.measurementUnit(detail.getMeasurementUnit())
						.measurementUnitProduct(detail.getMeasurementUnitProduct()).price(detail.getPrice())
						.tax(detail.getTax()).discount(detail.getDiscount()).build();

				// Relacion detail con lote. Se busca DetailLot a partir del Detail
				DocumentDetailLot detailLot = detailLotMap.get(detailTmp);

				// Detail con relacion al documento
				detailTmp = detailBuilder.document(documentEntity).build();

				// Se actualiza el detail del objeto DetailLot
				if (detailLot != null) {
					detailLot = DocumentDetailLot.builder(detailLot).documentDetail(detail).build();
				}

				log.info(strLog + "transactionType: " + transactionType);

				// Consultat UM x Product
				MeasurementUnitProduct muProduct = detail.getMeasurementUnitProduct();
				log.info(strLog + "muProduct: " + muProduct.getMeasurementUnit());

				Double initialStock = 0.0;
				Double quantity = 0.0;
				Double finalStock = 0.0;
				Double finalStockMU = 0.0;
				Double initialStockLot = 0.0;
				Double quantityLot = 0.0;
				Double finalStockLot = 0.0;

				quantity = Double.parseDouble(detail.getQuantity());
				log.info(strLog + "quantity ingresado: " + quantity);

				// Actualizar stock para la UM escogida
				finalStockMU = muProduct.getStock();
				log.info(strLog + "Stock actual para la UM: " + finalStockMU);

				// Actualizar cantidad y stock final de detailLot
				Lot lotTmp = detailLot.getLot();
				lotTmp.setNew(false);
				log.info(strLog + "Lote a actualizar stock:" + detailLot.getLot());

				// El stock en la UM por defecto del producto
				initialStock = muProduct.getStock() != null ? muProduct.getStock() : 0;
				log.info(strLog + "initialStock total en UM pral. : " + initialStock);

				// Para las compras la cantidad es la misma del lote, y esta cant ya se agregó
				// al crear el lote
				if (transactionType.equals(ETransactionType.ENTRADA)) {
					// Para el inventario general
					finalStock = initialStock;
					log.info(strLog + "finalStock: " + finalStock);
					// Si la UM es diferente a la UM principal se debe convertir
					if (!detail.getMeasurementUnit().equals(detail.getProduct().getMeasurementUnit())) {
						quantity = convertStockXMU(quantity, detail.getMeasurementUnit(),
								detail.getProduct().getMeasurementUnit());
						log.info(strLog + "quantity convertido: " + quantity);
					}
					// Se le debe restar pq ya estaba agregado al lote
					initialStock = initialStock - quantity;

					// Para el lote
					initialStockLot = 0.0;
					finalStockLot = lotTmp.getQuantity();
					quantityLot = quantity;

				} else if (transactionType.equals(ETransactionType.SALIDA)) {
					finalStockMU = finalStockMU - quantity;
					// Para inventario general
					if (!detail.getMeasurementUnit().equals(detail.getProduct().getMeasurementUnit())) {
						// Si la UM es diferente a la UM principal se debe convertir
						initialStock = convertStockXMU(initialStock, detail.getMeasurementUnit(),
								detail.getProduct().getMeasurementUnit());
						log.info(strLog + "initialStock convertido: " + quantity);

						quantity = convertStockXMU(quantity, detail.getMeasurementUnit(),
								detail.getProduct().getMeasurementUnit());
						log.info(strLog + "quantity convertido: " + quantity);
					}
					finalStock = initialStock != 0 ? initialStock - quantity : quantity;

					// Para el lote
					initialStockLot = lotTmp.getQuantity();
					quantityLot = quantity;
					finalStockLot = initialStockLot - quantityLot;

				}

				log.info(strLog + "finalStockMU: " + finalStockMU);
				log.info(strLog + "initialStockLot: " + initialStockLot);
				log.info(strLog + "quantityLot:" + quantityLot);
				log.info(strLog + "finalStockLot :" + finalStockLot);

				// Actualizar movimiento del lote
				detailLot.setQuantity(quantityLot);
				detailLot.setFinalStockLot(finalStockLot);

				// Actualizar cantidad del lote
				lotTmp.setQuantity(finalStockLot);

				// Crear objeto de inventario
				InventoryTransaction inventoryTransaction = InventoryTransaction.builder().product(detail.getProduct())
						.transactionType(transactionType).initialStock(initialStock).quantity(quantity)
						.finalStock(finalStock).document(documentEntity).build();

				// Actualizar stock total del producto
				Product product = productBll.select(detail.getProduct().getCode());
				product.setStock(finalStock);
				product.setStockDate(new Date());

				try {
					// Guardar relación item factura con lote
					if (detailLot != null) {
						detailLotBll.save(detailLot, false);
					}
					log.info(strLog + "detailLot saved:" + detailLot);

					// Guardar movimiento de inventario
					inventoryBll.save(inventoryTransaction, false);
					log.info(strLog + "inventoryTransaction saved:" + inventoryTransaction);

					if (transactionType.equals(ETransactionType.SALIDA)) {

						// Actualizar stock producto
						productBll.save(product, false);
						log.info("product saved:" + product);

						// Actualizar stock de lote
						lotBll.save(lotTmp, false);
						log.info(strLog + "lot saved:" + lotTmp);

						// Actualizar el stock para la UM escogida
						muProduct.setStock(finalStockMU);

						// Actualizar precio del producto
						updatePrice(muProduct, detail);

						// Actualizar stock de cada UM asociada al producto
						updateStockByMU(detail.getProduct(), muProduct);
					}

					closeWindow(cashChangeWindow);
				} catch (ModelValidationException ex) {
					hasErrors = true;
					documentEntity = null;
					log.error(strLog + "[ModelValidationException]" + ex);
					ViewHelper.showNotification(ex.getMessage(), Notification.Type.ERROR_MESSAGE);
				} catch (HibernateException ex) {
					hasErrors = true;
					documentEntity = null;
					log.error(strLog + "[HibernateException]" + ex);
					ViewHelper.showNotification(
							"Los datos no pudieron ser salvados, contacte al administrador del sistema",
							Notification.Type.ERROR_MESSAGE);
				}
			}

			if (!hasErrors) {
				// Actualizar consecutivo de tipo de factura
				documentTypeBll.save(documentType, false);
				log.info(strLog + "documentType saved:" + documentType);
				log.info(strLog + "document saved:" + documentEntity);
				documentTypeBll.commit();
				this.document = documentEntity;
				ViewHelper.showNotification("Factura guardada con exito", Notification.Type.WARNING_MESSAGE);
				disableComponents(true);
				if (transactionType.equals(ETransactionType.SALIDA)) {
					printInvoice();
				}
			} else {
				documentBll.rollback();
			}
		}
	}

	/**
	 * Metodo para guardar el encabezado de una factura
	 * 
	 * @param documentEntity
	 * @return
	 */
	private Document saveInvoiceHeader(Document documentEntity) {
		String strLog = "[saveInvoiceHeader]";
		Document documentSaved = null;
		boolean hasErrors = false;
		try {
			log.info(strLog + "[parameters] documentEntity: " + documentEntity);

			// Setear el detalle de items a la factura
			Set<DocumentDetail> details = detailGrid.getDataProvider().fetch(new Query<>()).collect(Collectors.toSet());

			Set<DocumentDetail> detailsTmp = new HashSet<>();
			for (DocumentDetail detail : details) {
				if (detail.getProduct() != null) {
					if (detail.getQuantity() == null || detail.getQuantity().isEmpty()) {
						hasErrors = true;
						ViewHelper.showNotification("Cantidad no ingresada para producto: " + detail.getCode(),
								Notification.Type.ERROR_MESSAGE);
						break;
					} else {
						detailsTmp.add(detail);
					}
				}
			}

			if (!hasErrors) {
				Document.Builder docBuilder = null;
				DocumentStatus documentStatus = null;
				if (documentEntity == null || documentEntity.getCode() == null) {
					docBuilder = Document.builder();
					documentStatus = docStatusBll.select("Registrada").get(0);
				} else {
					documentStatus = documentEntity.getStatus();
					docBuilder = Document.builder(documentEntity);
				}

				Date docDate = DateUtil.localDateTimeToDate(dtfDocumentDate.getValue());
				Date expirationDate = dtfExpirationDate.getValue() != null
						? DateUtil.localDateTimeToDate(dtfExpirationDate.getValue())
						: null;

				Double total = txtTotal.getValue() != null ? Double.parseDouble(txtTotal.getValue()) : 0;
				Double totalIVA = txtTotalTax.getValue() != null ? Double.parseDouble(txtTotalTax.getValue()) : 0;

				documentEntity = docBuilder.code(txtDocNumber.getValue())
						.reference(txtReference.getValue() != null ? txtReference.getValue() : "")
						.documentType(cbDocumentType.getValue()).resolution(txtResolution.getValue())
						.person(selectedPerson).documentDate(docDate).paymentMethod(cbPaymentMethod.getValue())
						.paymentType(paymentType).paymentTerm(txtPaymentTerm.getValue()).expirationDate(expirationDate)
						.totalValue(total).totalValueNoTax(totalIVA).status(documentStatus).salesman(user.getPerson())
						.payValue(payValue).details(detailsTmp).build();

				// Persistir documento
				documentBll.save(documentEntity, false);
				// Consultar el documento guardado
				documentSaved = documentBll.select(documentEntity.getCode(), documentEntity.getDocumentType());
			}
		} catch (ModelValidationException ex) {
			log.error(strLog + "[ModelValidationException]" + ex);
			documentBll.rollback();
			ViewHelper.showNotification(ex.getMessage(), Notification.Type.ERROR_MESSAGE);
		} catch (HibernateException ex) {
			log.error(strLog + "[HibernateException]" + ex);
			documentBll.rollback();
			ViewHelper.showNotification("Los datos no pudieron ser salvados, contacte al administrador",
					Notification.Type.ERROR_MESSAGE);
		} catch (PersistenceException ex) {
			log.error(strLog + "[PersistenceException]" + ex);
			documentBll.rollback();
			ViewHelper.showNotification("Se presentó un error, por favor contacte al adminisrador del sistema",
					Notification.Type.ERROR_MESSAGE);
		} catch (Exception ex) {
			log.error(strLog + "[Exception]" + ex.getLocalizedMessage());
			ex.printStackTrace();
			documentBll.rollback();
			ViewHelper.showNotification(
					"Se presentó un error al guardar la factura, por favor contacte al adminisrador del sistema",
					Notification.Type.ERROR_MESSAGE);
		}

		return documentSaved;

	}

	/**
	 * Metodo para limpiar los campos de la pantalla
	 */
	private void cleanButtonAction() {
		String strLog = "[cleanButtonAction]";

		try {
			document = null;
			selectedPerson = null;
			selectedProduct = null;
			selectedLot = null;

			txtDocNumber.clear();
			txtDocNumFilter.clear();
			txtReference.clear();
			txtPerson.clear();
			txtPaymentTerm.clear();
			cbPaymentMethod.clear();
			// cbPaymentType.clear();
			cbDocumentStatus.setSelectedItem(docStatusBll.select("Nueva").get(0));
			itemsList.clear();
			// initializeGrid();
			detailGrid.getDataProvider().refreshAll();
			txtDocNumFilter.clear();
			txtTotal.clear();
			txtTotalTax.clear();
			dtfDocumentDate.setValue(LocalDateTime.now());
			getNextDocumentNumber(cbDocumentType.getSelectedItem().get());
			disableComponents(false);
			CommonsUtil.CURRENT_DOCUMENT_DETAIL = null;
		} catch (Exception e) {
			log.error(strLog + "[Exception]" + e.getMessage());
			ViewHelper.showNotification(
					"Se presentó un error al limpiar los datos, por favor contacte al adminisrador del sistema",
					Notification.Type.ERROR_MESSAGE);
		}

	}

	/**
	 * Metodo para buscar una factura ya registrada
	 * 
	 * @param documentNumber
	 */
	private void searchDocument(String documentNumber) {
		String strLog = "[searchDocument]";
		try {
			// cleanButtonAction();
			log.info(strLog + "[parameters] documentNumber: " + documentNumber);
			List<DocumentType> docTypeList = documentTypeBll.select(transactionType);
			if (documentNumber != null && !documentNumber.isEmpty() && !docTypeList.isEmpty()) {
				document = documentBll.select(documentNumber, docTypeList);
				if (document != null) {
					cbDocumentType.setValue(document.getDocumentType());
					txtDocNumber.setValue(document.getCode());
					txtReference.setValue(document.getReference() != null ? document.getReference() : "");
					txtResolution.setValue(document.getResolution() != null ? document.getResolution() : "");
					dtfDocumentDate.setValue(DateUtil.dateToLocalDateTime(document.getDocumentDate()));

					PaymentDocumentType payDocType = paymentDocumentTypeBll.select(document.getDocumentType(),
							document.getPaymentType());

					cbPaymentType.setValue(payDocType);

					cbPaymentMethod.setValue(document.getPaymentMethod());
					txtPaymentTerm.setValue(document.getPaymentTerm() != null ? document.getPaymentTerm() : "");
					selectedPerson = document.getPerson();
					txtPerson.setValue(document.getPerson().getName() + " " + document.getPerson().getLastName());
					dtfExpirationDate.setValue(DateUtil.dateToLocalDateTime(document.getExpirationDate()));
					cbDocumentStatus.setValue(document.getStatus());
					Set<DocumentDetail> detailSet = document.getDetails();

					itemsList = new ArrayList<>();
					for (Iterator<DocumentDetail> iterator = detailSet.iterator(); iterator.hasNext();) {
						itemsList.add(iterator.next());
					}
					fillDetailGridData(itemsList);

					if (role.getName().equals(ERole.SUDO.getName()) || role.getName().equals(ERole.MANAGER.getName())) {
						disableComponents(false);
					} else {
						disableComponents(true);
					}
				}
			}

		} catch (Exception e) {
			log.error(strLog + "[Exception]" + e.getMessage());
			ViewHelper.showNotification("Se presentó un error al consultar la factura",
					Notification.Type.ERROR_MESSAGE);
		}
	}

	/**
	 * Método para inactivar componentes
	 * 
	 * @param disable
	 */
	private void disableComponents(Boolean disable) {
		cbDocumentType.setReadOnly(disable);
		txtDocNumber.setReadOnly(disable);
		txtReference.setReadOnly(disable);
		txtResolution.setReadOnly(disable);
		dtfDocumentDate.setReadOnly(disable);
		cbPaymentType.setReadOnly(disable);
		cbPaymentMethod.setReadOnly(disable);
		txtPaymentTerm.setReadOnly(disable);
		txtPerson.setReadOnly(disable);
		dtfExpirationDate.setReadOnly(disable);
		cbDocumentStatus.setReadOnly(disable);
		searchPersonBtn.setEnabled(!disable);
		addProductBtn.setEnabled(!disable);
		deleteProductBtn.setEnabled(!disable);
		detailGrid.getEditor().setEnabled(!disable);
	}

	/**
	 * Metodo para cerrar un componente Window
	 * 
	 * @param w
	 */
	private void closeWindow(Window w) {
		if (w != null) {
			w.close();
		}
	}

	/**
	 * Metodo para eliminar un item del detalle de la factura
	 */
	private void deleteItemDetail() {
		String strLog = "[deleteItemDetail]";
		try {
			DocumentDetail detail = getSelectedDetail();
			log.info(strLog + "detail:" + detail);
			if (detail != null) {
				itemsList.remove(detail);
				fillDetailGridData(itemsList);
			} else {
				ViewHelper.showNotification("Seleccione un ítem", Notification.Type.WARNING_MESSAGE);
			}
		} catch (Exception e) {
			log.error(strLog + "[Exception]" + e.getMessage());
			ViewHelper.showNotification("Se presentó une error al eliminar el ítem", Notification.Type.ERROR_MESSAGE);
		}
	}

	/**
	 * Método para obtener el item seleccionado del detalle de la factura
	 * 
	 * @return
	 */

	private DocumentDetail getSelectedDetail() {
		DocumentDetail detail = null;
		Set<DocumentDetail> detailSet = detailGrid.getSelectedItems();
		if (!detailSet.isEmpty()) {
			detail = detailSet.iterator().next();
		}
		return detail;
	}

	/**
	 * Método de acción del botón para cancelar facturas
	 */
	private void deleteButtonAction() {
		if (document != null) {
			ConfirmDialog.show(Page.getCurrent().getUI(), "Confirmar",
					"Desea cancelar la factura. Esta pasará a estado CANCELADA", "Si", "No", e -> {
						if (e.isConfirmed()) {
							deleteDocument();
						}
					});
		} else {
			ViewHelper.showNotification("No hay factura cargada", Notification.Type.WARNING_MESSAGE);
		}
	}

	/**
	 * Metodo para cancelar una factura, pasa a estado CANCELADA
	 */

	private void deleteDocument() {
		String strLog = "[deleteDocument]";
		try {

			DocumentStatus status = docStatusBll.select("Cancelada").get(0);
			Document documentEntity = Document.builder(document).status(status).build();
			saveButtonAction(documentEntity);
		} catch (Exception e) {
			log.error(strLog + "[Exception]" + e.getMessage());
			ViewHelper.showNotification("Se presentó un error al eliminar el registro",
					Notification.Type.ERROR_MESSAGE);
		}

	}

	private void printInvoice() {
		String strLog = "[printInvoice]";
		try {
			log.info(strLog + " document: " + document);
			if (document != null && document.getCode() != null) {
				if (pdfGenerator != null) {
					String filePath = pdfGenerator.generate(createReportParameters(), document.getDetails());

					Embedded c = new Embedded();
					c.setSource(new FileResource(new File(filePath)));
					c.setType(Embedded.TYPE_BROWSER);
					c.setWidth("960px");
					c.setHeight("750px");
					HorizontalLayout hr = new HorizontalLayout();
					hr.setWidth("980px");
					hr.setHeight("770px");
					hr.addComponent(c);

					Window pdfWindow = ViewHelper.buildSubwindow("75%", null);
					pdfWindow.setContent(hr);
					getUI().addWindow(pdfWindow);
				} else {
					ViewHelper.showNotification("Error al generar factura", Notification.Type.WARNING_MESSAGE);
				}

			} else {
				ViewHelper.showNotification("Debe cargar una factura", Notification.Type.WARNING_MESSAGE);
			}

		} catch (GeneratorException ex) {
			log.error(strLog + "[GeneratorException]" + ex.getMessage());
			ex.printStackTrace();
			ViewHelper.showNotification("Se presentó un error al imprimir la factura", Notification.Type.ERROR_MESSAGE);
		} catch (Exception e) {
			log.error(strLog + "[Exception]" + e.getMessage());
			ViewHelper.showNotification("Se presentó un error al imprimir la factura", Notification.Type.ERROR_MESSAGE);
		}
	}

	private Map<String, Object> createReportParameters() {
		String strLog = "[createParameters]";

		final Map<String, Object> parameters = new HashMap<>();
		try {
			log.info(strLog + " company: " + company);

			if (company != null) {
				parameters.put(Commons.PARAM_COMPANY, company.getName());
				parameters.put(Commons.PARAM_NIT, company.getNit() != null ? company.getNit() : "");
				parameters.put(Commons.PARAM_RESOLUTION,
						company.getInvoiceResolution() != null ? company.getInvoiceResolution() : "");
				parameters.put(Commons.PARAM_REGIMEN, company.getRegimeType() != null ? company.getRegimeType() : "");
				parameters.put(Commons.PARAM_ADDRESS, company.getAddress() != null ? company.getAddress() : "");
				parameters.put(Commons.PARAM_PHONE, company.getPhone() != null ? company.getPhone() : "");
				parameters.put(Commons.PARAM_MOBILE, company.getMobile() != null ? company.getMobile() : "");

				String basepath = VaadinService.getCurrent().getBaseDirectory().getAbsolutePath();
				parameters.put(Commons.PARAM_LOGO, basepath + "/WEB-INF/logoKisam.png");
			}
			if (document != null) {
				parameters.put(Commons.PARAM_INVOICE_NUMBER, document.getCode());
				parameters.put(Commons.PARAM_INVOICE_DATE, DateUtil.dateToString(document.getDocumentDate()));
				String invoiceType;

				if (document.getDocumentType().getTransactionType().equals(ETransactionType.ENTRADA)) {
					invoiceType = document.getDocumentType().getName();
				} else {
					PaymentType payType = document.getPaymentType();
					if (payType.getCode().equals(EPaymemtType.CREDIT.getName())) {
						invoiceType = "CREDITO";
					} else {
						invoiceType = "CONTADO";
					}
				}

				parameters.put(Commons.PARAM_INVOICE_TYPE, invoiceType);
				parameters.put(Commons.PARAM_SALESMAN,
						document.getSalesman().getName() + " " + document.getSalesman().getLastName());
				parameters.put(Commons.PARAM_CUSTOMER,
						document.getPerson().getName() + " " + document.getPerson().getLastName());

				parameters.put(Commons.PARAM_CUSTOMER_ID,
						document.getPerson().getDocumentNumber() != null ? document.getPerson().getDocumentNumber()
								: "");
				parameters.put(Commons.PARAM_CUSTOMER_ADDRESS,
						document.getPerson().getAddress() != null ? document.getPerson().getAddress() : "");
				parameters.put(Commons.PARAM_CUSTOMER_PHONE,
						document.getPerson().getMobile() != null ? document.getPerson().getMobile() : "");
				parameters.put(Commons.PARAM_CASH, document.getPayValue() != null ? document.getPayValue() : 0.0);
				parameters.put(Commons.PARAM_CHANGE,
						document.getPayValue() != null ? document.getPayValue() - document.getTotalValue() : 0.0);
				parameters.put(Commons.PARAM_TOTAL_IVA,
						document.getTotalValueNoTax() != null ? document.getTotalValueNoTax() : 0.0);
			}
		} catch (Exception e) {

		}
		return parameters;
	}

	/**
	 * Metodo que construye la ventana para mostrar el cambio de dinero
	 */
	private boolean buildCashChangeWindow() {

		cashChangeWindow = ViewHelper.buildSubwindow("50%", null);

		try {
			Double totalValue = txtTotal.getValue() != null ? Double.parseDouble(txtTotal.getValue()) : 0;
			cashChangeLayout = new CashChangeLayout(this, totalValue);
			cashChangeLayout.setMargin(false);
			cashChangeLayout.setSpacing(false);

			VerticalLayout subContent = ViewHelper.buildVerticalLayout(true, true);
			subContent.addComponents(cashChangeLayout);
			cashChangeWindow.setContent(subContent);

			getUI().addWindow(cashChangeWindow);
		} catch (IOException e) {
			log.error("Error al cargar lista de lotes. Exception:" + e);
		}

		return true;

	}

	public Document getDocument() {
		return document;
	}

	public void setDocument(Document document) {
		this.document = document;
	}

	private void buildMeasurementUnitComponent() {
		Product product = selectedProduct != null ? selectedProduct : CommonsUtil.CURRENT_DOCUMENT_DETAIL.getProduct();
		ComboBox<MeasurementUnit> cbMeasurementUnit = new ComboBox<>();

		cbMeasurementUnit.setEmptySelectionAllowed(false);
		List<MeasurementUnit> muList = measurementUnitProductBll.selectMuByProduct(product);
		ListDataProvider<MeasurementUnit> measurementDataProv = new ListDataProvider<>(muList);
		cbMeasurementUnit.setDataProvider(measurementDataProv);
		cbMeasurementUnit.setItemCaptionGenerator(MeasurementUnit::getName);
		cbMeasurementUnit.addValueChangeListener(e -> {
			MeasurementUnitProduct muProduct = selectMuXProduct(
					cbMeasurementUnit.getSelectedItem().isPresent() ? e.getValue() : null, product);
			setPriceComponent(muProduct);
			String qty = CommonsUtil.CURRENT_DOCUMENT_DETAIL != null ? CommonsUtil.CURRENT_DOCUMENT_DETAIL.getQuantity()
					: null;
			if (qty != null && !qty.isEmpty()) {
				setQuantityComponent(Double.parseDouble(qty), e.getOldValue(), e.getValue());
			}

		});

		Binder<DocumentDetail> binder = detailGrid.getEditor().getBinder();
		Binding<DocumentDetail, MeasurementUnit> binding = binder.bind(cbMeasurementUnit,
				DocumentDetail::getMeasurementUnit, DocumentDetail::setMeasurementUnit);
		columnUM.setEditorBinding(binding);

	}

	private void setQuantityComponent(Double value, MeasurementUnit sourceMU, MeasurementUnit targetMU) {
		// Binding de cantidad

		NumberField txtQuantity = new NumberField();
		Double newQty = convertStockXMU(value, sourceMU, targetMU);
		txtQuantity.setValue(newQty);
		Binder<DocumentDetail> binder = detailGrid.getEditor().getBinder();
		Binding<DocumentDetail, String> quantityBinding = binder.bind(txtQuantity, DocumentDetail::getQuantity,
				DocumentDetail::setQuantity);
		columnQuantity.setEditorBinding(quantityBinding);

	}

	/**
	 * Metodo para seleccionar el precio para una UM
	 * 
	 * @param mus
	 * @param product
	 * @return
	 */
	private MeasurementUnitProduct selectMuXProduct(MeasurementUnit mu, Product product) {
		MeasurementUnitProduct muProduct = null;
		String strLog = "[selectMuXProduct]";
		try {
			if (mu != null) {

				List<MeasurementUnitProduct> priceList = measurementUnitProductBll.select(mu, product);
				if (priceList.size() > 0) {
					muProduct = priceList.get(0);
				}
			}
		} catch (Exception e) {
			log.error(strLog + "[Exception]" + e.getMessage());

		}
		return muProduct;
	}

	/**
	 * Metodo para seleccionar el precio para una UM
	 * 
	 * @param mus
	 * @param product
	 * @return
	 */
	private void setPriceComponent(MeasurementUnitProduct muProduct) {
		String strLog = "[setPriceComponent]";
		try {
			if (muProduct != null) {
				Binder<DocumentDetail> binder = detailGrid.getEditor().getBinder();
				NumberField txtPrice = new NumberField();
				NumberField txtTax = new NumberField();
				txtTax.setReadOnly(true);

				CommonsUtil.MEASUREMENT_UNIT_PRODUCT = muProduct;
				if (transactionType.equals(ETransactionType.ENTRADA)) {
					txtPrice.setValue(muProduct.getPurchasePrice());
					txtTax.setValue(muProduct.getPurchaseTax());
					txtPrice.setReadOnly(false);

				} else if (transactionType.equals(ETransactionType.SALIDA)) {
					txtPrice.setValue(muProduct.getSalePrice());
					txtTax.setValue(muProduct.getSaleTax());
					txtPrice.setReadOnly(true);
				}

				// Binding de precio
				Binding<DocumentDetail, String> priceBinding = binder.bind(txtPrice, DocumentDetail::getPriceStr,
						DocumentDetail::setPriceStr);
				columnPrice.setEditorBinding(priceBinding);

				// Binding de impuesto
				Binding<DocumentDetail, String> taxBinding = binder.bind(txtTax, DocumentDetail::getTaxStr,
						DocumentDetail::setTaxStr);
				columnTax.setEditorBinding(taxBinding);

				// Binding de MU X producto
				ComboBox<MeasurementUnitProduct> cbMeasurementUnitProduct = new ComboBox<>();
				cbMeasurementUnitProduct.setValue(muProduct);
				Binding<DocumentDetail, MeasurementUnitProduct> muProductBinding = binder.bind(cbMeasurementUnitProduct,
						DocumentDetail::getMeasurementUnitProduct, DocumentDetail::setMeasurementUnitProduct);
				columnUMProd.setEditorBinding(muProductBinding);

			}

		} catch (Exception e) {
			log.error(strLog + "[Exception]" + e.getMessage());
		}

	}

	/**
	 * Actualizar equivalencia de stock por cada Unidad de Medida del producto
	 * 
	 * @param measurementUnit
	 */
	private void updateStockByMU(Product product, MeasurementUnitProduct paramMuProduct) {
		String strLog = "[updateStockByMU] ";
		try {

			List<MeasurementUnitProduct> muProductList = measurementUnitProductBll.select(product);
			for (MeasurementUnitProduct muProductObj : muProductList) {
				if (!muProductObj.getMeasurementUnit().equals(paramMuProduct.getMeasurementUnit())) {
					Double newStock = convertStockXMU(paramMuProduct.getStock(), paramMuProduct.getMeasurementUnit(),
							muProductObj.getMeasurementUnit());
					muProductObj.setStock(newStock);
				} else {
					muProductObj.setStock(paramMuProduct.getStock());
				}
				measurementUnitProductBll.save(muProductObj, false);

				log.info(strLog + " stock actualizado para UM: " + paramMuProduct);
			}
		} catch (Exception e) {
			log.error(strLog + "[Exception]" + e.getMessage());
		}
	}

	/**
	 * Actualizar el precio del producto x UM ingresado en la grid
	 * 
	 * @param muProduct
	 * @param price
	 */
	private void updatePrice(MeasurementUnitProduct muProduct, DocumentDetail detail) {
		String strLog = "[updatePrice] ";
		try {
			Double price = detail.getPrice();
			Double tax = detail.getTax();
			log.info(strLog + "[parameters] muProduct: " + muProduct + ", price: " + price);
			if (price != null && !price.equals(0.0) && muProduct != null) {
				MeasurementUnitProduct entity = null;
				MeasurementUnitProduct.Builder muProductBuilder = MeasurementUnitProduct.builder(muProduct);
				if (transactionType.equals(ETransactionType.ENTRADA)) {
					entity = muProductBuilder.purchasePrice(price).purchaseTax(tax).build();
				} else if (transactionType.equals(ETransactionType.SALIDA)) {
					entity = muProductBuilder.salePrice(price).saleTax(tax).build();
				}
				measurementUnitProductBll.save(entity, false);
				log.info(strLog + "Precio actualizado");
			}
		} catch (Exception e) {
			log.error(strLog + "[Exception]" + e.getMessage());
		}
	}

	/*
	 * 
	 * Actualizar conciliación (cuadre de caja) por día y empleado
	 */
	private void saveConciliation() {
		String strLog = "[saveConciliation] ";
		try {
			Date conciliationDate = DateUtil.localDateTimeToDate(dtfDocumentDate.getValue());
			conciliationDate = DateUtils.truncate(conciliationDate, Calendar.DATE);
			new CashConciliationLayout().saveDailyConciliation(user, conciliationDate);
		} catch (IOException e) {
			log.error(strLog + "Error al actualizar conciliación: " + e.getMessage());
			e.printStackTrace();
		}
	}

	public Double getPayValue() {
		return payValue;
	}

	public void setPayValue(Double payValue) {
		this.payValue = payValue;
	}

}
