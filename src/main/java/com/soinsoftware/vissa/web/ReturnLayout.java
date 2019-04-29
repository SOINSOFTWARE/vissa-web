package com.soinsoftware.vissa.web;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.persistence.PersistenceException;

import org.apache.log4j.Logger;
import org.hibernate.HibernateException;
import org.jsoup.helper.StringUtil;
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
import com.soinsoftware.vissa.bll.PersonBll;
import com.soinsoftware.vissa.bll.ProductBll;
import com.soinsoftware.vissa.common.CommonsConstants;
import com.soinsoftware.vissa.exception.ModelValidationException;
import com.soinsoftware.vissa.model.Company;
import com.soinsoftware.vissa.model.Document;
import com.soinsoftware.vissa.model.DocumentDetail;
import com.soinsoftware.vissa.model.DocumentDetailLot;
import com.soinsoftware.vissa.model.DocumentStatus;
import com.soinsoftware.vissa.model.DocumentType;
import com.soinsoftware.vissa.model.EDocumentType;
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
import com.vaadin.ui.PopupView;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.components.grid.HeaderRow;
import com.vaadin.ui.themes.ValoTheme;

@SuppressWarnings("deprecation")
public class ReturnLayout extends VerticalLayout implements View {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5187486966314714738L;
	protected static final Logger log = Logger.getLogger(ReturnLayout.class);

	// Bll
	private final ProductBll productBll;
	private final PaymentMethodBll paymentMethodBll;
	private final DocumentBll documentBll;
	private final InventoryTransactionBll inventoryBll;
	private final DocumentTypeBll documentTypeBll;
	private final DocumentStatusBll docStatusBll;
	private final LotBll lotBll;
	private final DocumentDetailLotBll detailLotBll;
	private final DocumentDetailLotBll docDetailLotBll;
	private final CompanyBll companyBll;
	private final PaymentDocumentTypeBll paymentDocumentTypeBll;
	private final MeasurementUnitProductBll measurementUnitProductBll;
	private final MuEquivalenceBll muEquivalencesBll;
	private final PersonBll personBll;

	// Components
	private TextField txtDocNumFilter;
	private NumberField txtReturnNum;
	private DateTimeField dtfReturnDate;
	private NumberField txtReturnTotal;
	private NumberField txtReturnTotalTax;

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
	private Document invoiceDocument;
	private Document returnDocument;
	private List<DocumentDetail> itemListOld = null;
	private List<DocumentDetail> itemList = null;

	private ProductLayout productLayout = null;
	private LotLayout lotLayout = null;
	private PersonLayout personLayout = null;

	private DocumentType documentType;

	private PdfGenerator pdfGenerator = null;

	private List<DocumentDetailLot> detailLotCollectionOld;

	private List<DocumentDetailLot> detailLotCollection;

	private ETransactionType transactionType;

	private ListDataProvider<DocumentDetail> dataProvider;

	private Column<DocumentDetail, String> columnQuantity;
	private Column<?, ?> columnSubtotal;

	private Column<DocumentDetail, String> columnTax;
	private Column<DocumentDetail, String> columnPrice;
	private Column<DocumentDetail, String> columnDiscount;
	private Column<DocumentDetail, MeasurementUnit> columnUM;
	private Column<DocumentDetail, MeasurementUnitProduct> columnUMProd;
	private HeaderRow header;

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
	private Double invoiceTotal;

	public ReturnLayout() throws IOException {
		super();

		productBll = ProductBll.getInstance();
		paymentMethodBll = PaymentMethodBll.getInstance();
		documentBll = DocumentBll.getInstance();
		inventoryBll = InventoryTransactionBll.getInstance();
		documentTypeBll = DocumentTypeBll.getInstance();
		docStatusBll = DocumentStatusBll.getInstance();
		detailLotBll = DocumentDetailLotBll.getInstance();
		docDetailLotBll = DocumentDetailLotBll.getInstance();
		companyBll = CompanyBll.getInstance();
		paymentDocumentTypeBll = PaymentDocumentTypeBll.getInstance();
		measurementUnitProductBll = MeasurementUnitProductBll.getInstance();
		muEquivalencesBll = MuEquivalenceBll.getInstance();
		lotBll = LotBll.getInstance();
		personBll = PersonBll.getInstance();

		invoiceDocument = new Document();
		itemListOld = new ArrayList<DocumentDetail>();
		itemList = new ArrayList<DocumentDetail>();

		transactionType = ETransactionType.valueOf(CommonsConstants.TRANSACTION_TYPE);
		company = companyBll.selectAll().get(0);

		detailLotCollectionOld = new ArrayList<DocumentDetailLot>();
		detailLotCollection = new ArrayList<DocumentDetailLot>();

	}

	@Override
	public void enter(ViewChangeEvent event) {

		View.super.enter(event);

		this.user = getSession().getAttribute(User.class);
		this.role = user.getRole();
		this.permissionUtil = new PermissionUtil(user.getRole().getPermissions());
		// setMargin(true);
		String title = "Devoluciones";
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

		log.info(w.getPositionX() + "/" + w.getPositionY() + " / " + getUI().getWidth() + " / "
				+ getUI().getWidthUnits().ordinal() + " / " + layout.getWidth());

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
			newBtn.addClickListener(e -> {
				VerticalLayout popupContent = new VerticalLayout();
				popupContent.addComponent(new TextField("Textfield"));
				popupContent.addComponent(new Button("Button"));
				PopupView popup = new PopupView("Pop it up", popupContent);
				popup.setPopupVisible(true);
				layout.addComponent(popup);
			});
			layout.addComponents(newBtn);

			Button saveBtn = new Button("Guardar", FontAwesome.SAVE);
			saveBtn.addStyleName("mystyle-btn");
			saveBtn.addClickListener(e -> saveButtonAction(returnDocument));
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

		txtDocNumFilter = new TextField("N° factura de venta");
		txtDocNumFilter.addValueChangeListener(e -> searchInvoice(txtDocNumFilter.getValue(), EDocumentType.VE));
		txtDocNumFilter.setStyleName(ValoTheme.TEXTFIELD_TINY);
		txtDocNumFilter.setWidth("130px");
		txtDocNumFilter.focus();

		txtReturnNum = new NumberField("N° devolución");
		txtReturnNum.addValueChangeListener(e -> searchInvoice(txtReturnNum.getValue(), EDocumentType.DV));
		txtReturnNum.setStyleName(ValoTheme.TEXTFIELD_TINY);
		txtReturnNum.setWidth("120px");

		getNextReturnNumber(EDocumentType.DV);

		dtfReturnDate = new DateTimeField("Fecha devolución");
		dtfReturnDate.setResolution(DateTimeResolution.SECOND);
		dtfReturnDate.setValue(LocalDateTime.now());
		dtfReturnDate.setDateFormat(Commons.FORMAT_DATE_TIME);
		dtfReturnDate.setStyleName(ValoTheme.DATEFIELD_TINY);
		dtfReturnDate.setRequiredIndicatorVisible(true);
		dtfReturnDate.setWidth("184px");

		txtReturnTotal = new NumberField("Total cambio");
		txtReturnTotal.setReadOnly(true);
		txtReturnTotal.setStyleName(ValoTheme.TEXTFIELD_TINY);

		txtReturnTotalTax = new NumberField("Total IVA para cambio");
		txtReturnTotalTax.setReadOnly(true);
		txtReturnTotalTax.setStyleName(ValoTheme.TEXTFIELD_TINY);

		layout.addComponents(txtDocNumFilter, txtReturnNum, dtfReturnDate, txtReturnTotal, txtReturnTotalTax);

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
		cbDocumentType.setEmptySelectionAllowed(false);
		cbDocumentType.setEmptySelectionCaption("Seleccione");
		cbDocumentType.setStyleName(ValoTheme.COMBOBOX_TINY);
		cbDocumentType.setRequiredIndicatorVisible(true);

		ListDataProvider<DocumentType> docTypeDataProv = new ListDataProvider<>(
				documentTypeBll.select(transactionType));
		cbDocumentType.setDataProvider(docTypeDataProv);
		cbDocumentType.setItemCaptionGenerator(DocumentType::getName);

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
		addProductBtn.addClickListener(e -> {
			itemListOld.add(new DocumentDetail());
			fillDetailGridData(itemListOld);
		});

		deleteProductBtn = new Button("Eliminar ítem", FontAwesome.ERASER);
		deleteProductBtn.addStyleName(ValoTheme.BUTTON_TINY);
		deleteProductBtn.addClickListener(e -> deleteItemDetail());

		Button newProductBtn = new Button("Crear producto", FontAwesome.PLUS);
		newProductBtn.addStyleName(ValoTheme.BUTTON_TINY);
		newProductBtn.addClickListener(e -> buildProductWindow(null));

		buttonlayout.addComponents(addProductBtn, deleteProductBtn);
		if (transactionType.equals(ETransactionType.ENTRADA)) {
			buttonlayout.addComponent(newProductBtn);
		}

		layout.addComponents(buttonlayout, builGridPanel());

		return ViewHelper.buildPanel("Productos", layout);
	}

	@SuppressWarnings("unchecked")
	private Component builGridPanel() {
		String strLog = "[builGridPanel] ";

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

		txtQuantity.setDecimalAllowed(true);
		txtQuantity.setDecimalPrecision(4);
		txtQuantity.setDecimalSeparator(',');
		columnQuantity = detailGrid.addColumn(DocumentDetail::getQuantityStr).setCaption("Cantidad")
				.setEditorComponent(txtQuantity, DocumentDetail::setQuantityStr);

		columnSubtotal = detailGrid.addColumn(documentDetail -> {
			if (documentDetail.getSubtotal() != null) {
				return documentDetail.getSubtotal();
			} else {
				return "";
			}
		}).setCaption("Subtotal");

		// Evento de enter para cmpo de persona
		txtPerson.addShortcutListener(new ShortcutListener("search person", ShortcutAction.KeyCode.ENTER, null) {

			private static final long serialVersionUID = 7441523733731956234L;

			@Override
			public void handleAction(Object sender, Object target) {
				try {
					if (((TextField) target).equals(txtPerson)) {
						searchPerson(txtPerson.getValue());
					}

					if (((TextField) target).equals(txtCode) || ((TextField) target).equals(txtName)) {
						dataProvider.refreshAll();
						searchProduct(txtCode.getValue(), txtName.getValue());
					}
				} catch (Exception e) {
					log.error("[search person][ShortcutListener][handleAction][Exception] " + e.getMessage());
				}
			}
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
					if (((TextField) target).equals(txtPerson)) {
						searchPerson(txtPerson.getValue());
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

		header = detailGrid.prependHeaderRow();
		if (columnDiscount != null) {
			header.getCell(columnDiscount).setHtml("<b>Total IVA:</b>");
		} else {
			header.getCell(columnPrice).setHtml("<b>Total IVA:</b>");
		}
		header.getCell(columnQuantity).setHtml("<b>Total:</b>");

		detailGrid.getEditor().setEnabled(true);

		// Eventos
		detailGrid.getEditor().addOpenListener(e -> {
			log.info(strLog + "CURRENT_DOCUMENT_DETAIL : " + CommonsConstants.CURRENT_DOCUMENT_DETAIL);
			// fillDetailGridData(itemsList);
		});

		// EVENTOS
		detailGrid.getEditor()
				.addSaveListener(e -> saveEditorAction(e.getBean().getQuantity(), e.getBean().getPrice()));
		// txtQuantity.addValueChangeListener(e -> changeQuantity(e.getValue()));
		// txtQuantity.addBlurListener(e -> changeQuantity(txtQuantity.getValue()));

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
			itemListOld.add(docDetail);
			i++;
		}

		log.info(itemListOld);
		dataProvider = new ListDataProvider<>(itemListOld);
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
					buildProductWindow(products);
				}
			}
			if (name != null && !name.isEmpty()) {
				products = productBll.selectByName(name);
				int size = products.size();
				log.info(strLog + "Cantidad de productos consultados: " + size);
				if (size == 1) {
					selectProduct(products.get(0));
				} else {
					buildProductWindow(products);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			log.error(strLog + "[Exception]" + e.getMessage());
		}
	}

	/**
	 * Metodo que valida la cantidad y precio ingresado por cada item
	 * 
	 * @param quantity
	 */
	private void saveEditorAction(Double quantity, Double price) {
		String strLog = "[changeQuantity] ";
		dataProvider.refreshAll();
		String message = "";
		boolean correct = false;

		DocumentDetail currentDetail = null;

		try {
			if (CommonsConstants.CURRENT_DOCUMENT_DETAIL.getProduct() != null && quantity != null) {
				// Validar la cantidad mayor a 0
				if (quantity <= 0) {
					message = "La cantidad debe ser mayor a 0";
					throw new Exception(message);
				}
				// Validar el precio
				if (price == null || price.equals(0.0)) {
					message = "Precio no valido";
					throw new Exception(message);
				}

				currentDetail = CommonsConstants.CURRENT_DOCUMENT_DETAIL;
				log.info(strLog + "currentDetail: " + currentDetail);

				if (transactionType.equals(ETransactionType.SALIDA)) {
					// Validar que la cantidad ingresada esté dentro del stock del producto
					if (quantity > currentDetail.getProduct().getStock()) {
						message = "Cantidad ingresada es mayor al stock total del producto";
						throw new Exception(message);
					}

					// Validar que la cantidad sea mayor a la ingresada en la factura original
					if (quantity <= currentDetail.getOldQuantity()) {
						message = "Cantidad del producto menor a la cantidad original";
						throw new Exception(message);
					}
				}

				// Validar los lotes asociados al detail
				List<DocumentDetailLot> detailLotList = getDetailLotsByDetail(currentDetail);
				log.info(strLog + "detailLotList: " + detailLotList.size() + detailLotList);

				if (detailLotList.size() > 0) {
					// Para la compra es un solo lote, para la venta por defecto es el más viejo
					DocumentDetailLot detailLot = detailLotList.get(0);
					if (transactionType.equals(ETransactionType.ENTRADA)) {

						Double finalStockLot = quantity;
						DocumentDetailLot detailLotTmp = DocumentDetailLot.builder(detailLot).quantity(quantity)
								.finalStockLot(finalStockLot).build();
						int pos = detailLotList.indexOf(detailLot);
						detailLotList.set(pos, detailLotTmp);
						correct = true;
					} else if (transactionType.equals(ETransactionType.SALIDA)) {

						// Actualizar la cantidad del ítem
						currentDetail.setQuantity(quantity);
						// Se actualiza la diferencia entre la cant origina y el cambio
						currentDetail.setDiffQuantity(quantity - currentDetail.getOldQuantity());
						currentDetail = documentDetail(currentDetail);

						validateLot(currentDetail, detailLotList);

						// Agregar item a nueva lista de detalles
						int pos = itemList.indexOf(currentDetail);
						if (pos < 0) {
							itemList.add(currentDetail);
						} else {
							itemList.set(pos, currentDetail);
						}

						Double returnTotal = !StringUtil.isBlank(txtReturnTotal.getValue())
								? Double.valueOf(txtReturnTotal.getValue())
								: 0.0;
						returnTotal += currentDetail.getSubtotal();
						txtReturnTotal.setValue(returnTotal);

						Double returnIVATotal = !StringUtil.isBlank(txtReturnTotalTax.getValue())
								? Double.valueOf(txtReturnTotalTax.getValue())
								: 0.0;
						Double totalTax = currentDetail.getTotalTaxValue();
						returnIVATotal += totalTax != null ? totalTax : 0.0;
						txtReturnTotalTax.setValue(returnIVATotal);
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
			log.info(strLog + "Correct: " + correct);
			if (!correct && (message != null && !message.isEmpty())) {
				ViewHelper.showNotification(message, Notification.Type.WARNING_MESSAGE);
			}
		}
	}

	/**
	 * Metodo para validar el lote asociado a un item de factura
	 * 
	 * @param detail
	 * @param detailLotList
	 */
	private void validateLot(DocumentDetail detail, List<DocumentDetailLot> detailLotList) {
		String strLog = "[validateLot] ";
		try {
			if (detailLotList.size() > 0) {
				Double quantity = detail.getQuantity();
				log.info(strLog + "quantity: " + quantity);

				// Se toma el primer detailLot por defecto de los asociados al detail
				DocumentDetailLot detailLotDefault = detailLotList.get(0);

				// Si la UM del item es diferente a la del lote, se convierte a la del lote
				if (!detail.getMeasurementUnit().equals(detailLotDefault.getLot().getMeasurementUnit())) {
					quantity = convertStockXMU(quantity, detail.getMeasurementUnit(),
							detailLotDefault.getLot().getMeasurementUnit());

				}

				if (quantity > detailLotDefault.getLot().getQuantity()) {

					log.info(strLog + "El lote escogido inicialmente es menor para la cantidad seleccionada");

					for (DocumentDetailLot detailLot : detailLotList) {
						// Se elimina del map de lotes los lotes iniciales para recalcular
						detailLotCollectionOld.remove(detailLot);
					}

					// Se bucan los nuevos lotes con stock ordenados de fecha de creación asc
					List<Lot> lots = lotBll.selecLotWithStock(detail.getProduct());
					lots = lots.stream().sorted(Comparator.comparing(Lot::getCreationDate))
							.collect(Collectors.toList());

					Double qtyTmp = quantity;
					for (Lot lotTmp : lots) {
						if (qtyTmp <= 0.0) {// Si la cant a validar es 0 o negativo pq sobra del lote, Siempre se resta
											// al cant de tx - cant lote
							break;
						} else {
							DocumentDetailLot detLot = DocumentDetailLot.builder().documentDetail(detail).lot(lotTmp)
									.initialStockLot(lotTmp.getQuantity()).build();

							// La nueva cantidad a buscar es la dif entre el lote y la cantidad inicial
							Double diff = qtyTmp - lotTmp.getQuantity();

							// Si sobra del lote, la cant de la tx es la de la venta, sino es la del lote
							// Si sobra del lote, el final es la dif entre lote y la cant de la tx
							Double qtyLot = 0.0;
							Double finalStockLot = 0.0;
							if (diff > 0) {
								qtyLot = lotTmp.getQuantity();
								finalStockLot = 0.0;
							} else {
								qtyLot = qtyTmp;
								finalStockLot = lotTmp.getQuantity() - qtyTmp;
							}

							detLot = DocumentDetailLot.builder(detLot).quantity(qtyLot).finalStockLot(finalStockLot)
									.build();
							qtyTmp = diff;

							// detail = documentDetail(detail);
							// detLot.setDocumentDetail(detail);
							detailLotCollectionOld.add(detLot);

							log.info(strLog + "DetailLot actualizado. initialStockLot: " + detLot.getInitialStockLot()
									+ ", quantity: " + detLot.getQuantity() + ", finalStockLot: "
									+ detLot.getFinalStockLot());
						}
					}

				} else {

					log.info(strLog + "El lote escogido está ok para la cantidad seleccionada");

					int pos = detailLotCollectionOld.indexOf(detailLotDefault);

					// Se actualiza movimiento con stock que realmente tiene el lote en el momento
					detailLotDefault.setInitialStockLot(detailLotDefault.getLot().getQuantity());
					detailLotDefault.setQuantity(quantity);
					Double finalStockLot = detailLotDefault.getInitialStockLot() - quantity;
					detailLotDefault.setFinalStockLot(finalStockLot);

					// detailLotDefault.setDocumentDetail(documentDetail(detailLotDefault.getDocumentDetail()));
					detailLotCollectionOld.set(pos, detailLotDefault);

					log.info(strLog + "DetailLot actualizado. initialStockLot: " + detailLotDefault.getInitialStockLot()
							+ ", quantity: " + detailLotDefault.getQuantity() + ", finalStockLot: "
							+ detailLotDefault.getFinalStockLot());

				}
			}
		} catch (Exception e) {
			log.error(strLog + "[Exception]" + e.getMessage());
			e.printStackTrace();
		}
	}

	/**
	 * Buscar los detailLot relacionados a un detalle
	 * 
	 * @param detail
	 * @return
	 */
	private List<DocumentDetailLot> getDetailLotsByDetail(DocumentDetail detail) {
		List<DocumentDetailLot> detailLots = new ArrayList<>();
		for (DocumentDetailLot detailLot : detailLotCollectionOld) {
			if (detail.getProduct().equals(detailLot.getDocumentDetail().getProduct())) {
				detailLots.add(detailLot);
			}
		}

		return detailLots;
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

					return Math.round(documentDetail.getTaxValue() * documentDetail.getQuantity());
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
					// pass the row/item that the user double clicked
					// to method doStuff.
					// doStuff(l.getItem());
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
		String strLog = "[selectPerson]";
		try {
			selectedPerson = person != null ? person : personLayout.getSelected();

			if (selectedPerson != null) {
				txtPerson.setValue(selectedPerson.getName() + " " + selectedPerson.getLastName());
				if (personSubwindow != null) {
					personSubwindow.close();
				}
			} else {
				ViewHelper.showNotification("Seleccione una persona", Notification.Type.WARNING_MESSAGE);
			}
		} catch (Exception e) {
			log.error(strLog + "[Exception]" + e.getMessage());
		}

	}

	/**
	 * Obtener el número máximo para la devolución
	 * 
	 * @param docType
	 */
	private void getNextReturnNumber(EDocumentType eDocType) {
		if (eDocType != null) {
			// Obtener el tipo de documento
			DocumentType docType = documentTypeBll.select(eDocType.getName());

			// Actualizar el consecutivo para el tipo de documento
			documentType = DocumentType.builder(docType).sequence(docType.getSequence() + 1).build();

			// Validar si la secuencia no está duplicada
			Document document = documentBll.select(String.valueOf(documentType.getSequence()), docType);
			if (document == null) {
				txtReturnNum.setValue(String.valueOf(documentType.getSequence()));
			} else {
				ViewHelper.showNotification("Consecutivo no válido. Por favor verifique",
						Notification.Type.ERROR_MESSAGE);
			}

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
	 * Metodo que construye la venta para buscar productos
	 */
	private void buildProductWindow(List<Product> productList) {
		selectedProduct = null;
		selectedLot = null;

		productSubwindow = ViewHelper.buildSubwindow("75%", null);
		productSubwindow.setCaption("Productos");

		VerticalLayout subContent = ViewHelper.buildVerticalLayout(true, true);

		try {
			productLayout = new ProductLayout(ELayoutMode.LIST, productList);

			productLayout.getProductGrid().addItemClickListener(listener -> {
				if (listener.getMouseEventDetails().isDoubleClick())
					// pass the row/item that the user double clicked
					// to method doStuff.
					// doStuff(l.getItem());
					selectProduct(listener.getItem());
			});
		} catch (IOException e) {
			log.error("Error al cargar lista de productos. Exception:" + e);
		}
		Panel productPanel = ViewHelper.buildPanel(null, productLayout);
		subContent.addComponents(productPanel);

		productSubwindow.setContent(subContent);
		getUI().addWindow(productSubwindow);

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
				pos = itemListOld.indexOf(CommonsConstants.CURRENT_DOCUMENT_DETAIL);
				DocumentDetail docDetail = DocumentDetail.builder(CommonsConstants.CURRENT_DOCUMENT_DETAIL)
						.product(product).build();
				CommonsConstants.CURRENT_DOCUMENT_DETAIL.setIndex(pos);
				detailGrid.getSelectionModel().select(docDetail);
				detailGrid.focus();

				if (itemListOld.contains(docDetail)) {
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
								Lot lastLot = lotBll.getOlderLotWithStockByProduct(product);
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
				// Setear el precio e impuesto
				MeasurementUnitProduct priceXMu = selectMuXProduct(docDetail.getMeasurementUnit(),
						docDetail.getProduct());
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
				itemListOld.set(pos, docDetail);
				fillDetailGridData(itemListOld);
				buildMeasurementUnitComponent();
				closeWindow(productSubwindow);
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
			// detailLotMap.put(docDetail, detailLot);
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
			Commons.LAYOUT_MODE = ELayoutMode.ALL;
			lotLayout = new LotLayout(product, productLayout);
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
					if (CommonsConstants.CURRENT_DOCUMENT_DETAIL.getProduct() != null) {
						CommonsConstants.CURRENT_DOCUMENT_DETAIL
								.setCode(CommonsConstants.CURRENT_DOCUMENT_DETAIL.getProduct().getCode());
						itemListOld.set(pos, CommonsConstants.CURRENT_DOCUMENT_DETAIL);
					} else {
						// itemsList.set(pos, null);
					}

					fillDetailGridData(itemListOld);
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
							detail.setQuantity(lot.getQuantity());
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
			itemListOld = detailList;
			dataProvider = new ListDataProvider<>(detailList);

			detailGrid.setDataProvider(dataProvider);

			dataProvider.addDataProviderListener(
					event -> header.getCell(columnSubtotal).setHtml(calculateTotal(dataProvider)));
			dataProvider.addDataProviderListener(
					event -> header.getCell(columnTax).setHtml(calculateTotalIVA(dataProvider)));

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
								buildCashChangeWindow();
							} else {
								saveInvoice(documentEntity);
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
		try {
			Document documentEntity = saveInvoiceHeader(document);
			log.info(strLog + "Document saved: " + documentEntity);

			if (documentEntity != null) {
				boolean hasErrors = false;
				// Por cada item del detalle de factura
				for (DocumentDetail detail : documentEntity.getDetails()) {
					try {

						log.info(strLog + "transactionType: " + transactionType);
						log.info(strLog + "product: " + detail.getProduct().getName());

						// Guardar inventario
						InventoryTransaction inventory = saveInventory(documentEntity, detail);
						if (inventory != null) {
							// Actualizar stock del producto
							Product product = saveProduct(inventory);
							if (product != null) {
								hasErrors = saveLot(documentEntity, detail);
								closeWindow(cashChangeWindow);

							}
						}

					} catch (Exception ex) {
						hasErrors = true;
						documentEntity = null;
						log.error(strLog + "[Exception]" + ex.getMessage());
						ex.printStackTrace();
						ViewHelper.showNotification(
								"Los datos no pudieron ser salvados, contacte al administrador del sistema",
								Notification.Type.ERROR_MESSAGE);
					}
				}

				if (!hasErrors) {
					documentTypeBll.commit();
					// Actualizar consecutivo de tipo de factura
					documentTypeBll.save(documentType);
					log.info(strLog + "documentType saved:" + documentType);
					log.info(strLog + "document saved:" + documentEntity);
					this.returnDocument = documentEntity;
					ViewHelper.showNotification("Factura guardada con exito", Notification.Type.WARNING_MESSAGE);
					// disableComponents(true);
					if (transactionType.equals(ETransactionType.SALIDA)) {
						printInvoice();
					}
				} else {
					documentBll.rollback();
					ViewHelper.showNotification(
							"Los datos no pudieron ser salvados, contacte al administrador del sistema",
							Notification.Type.ERROR_MESSAGE);
				}
			}
		} catch (Exception e) {
			documentBll.rollback();
			e.printStackTrace();
			ViewHelper.showNotification("Los datos no pudieron ser salvados, contacte al administrador del sistema",
					Notification.Type.ERROR_MESSAGE);
		}

	}

	/**
	 * Método para guardar el inventario actualizado por la transacción de venta o
	 * compra
	 * 
	 * @param detail
	 * @return
	 */

	private InventoryTransaction saveInventory(Document document, DocumentDetail detail) {
		String strLog = "[saveInventory] ";
		Double initialStock = 0.0;
		Double quantity = 0.0;
		Double finalStock = 0.0;
		Double finalStockMU = 0.0;

		InventoryTransaction inventoryTransaction = null;
		try {
			log.info(strLog + "[parameters] detail: " + detail);

			// Consultar UM x Product
			MeasurementUnitProduct muProduct = detail.getMeasurementUnitProduct();
			log.info(strLog + "muProduct. MU: " + muProduct.getMeasurementUnit());

			quantity = detail.getQuantity();
			log.info(strLog + "quantity ingresado: " + quantity);

			// Consultar stock para la UM escogida
			finalStockMU = muProduct.getStock();
			log.info(strLog + "Stock actual para la UM: " + finalStockMU);

			// El stock en la UM por defecto del producto
			initialStock = muProduct.getStock() != null ? muProduct.getStock() : 0;
			log.info(
					strLog + "initialStock total en UM pral. : " + muProduct.getMeasurementUnit() + " " + initialStock);

			if (transactionType.equals(ETransactionType.ENTRADA)) {
				// Para el inventario general
				finalStock = initialStock + quantity;// Se suma al inventario
				finalStockMU = finalStock; // UM

				log.info(strLog + "finalStock: " + finalStock);

				// Si la UM es diferente a la UM principal se debe convertir
				if (!detail.getMeasurementUnit().equals(detail.getProduct().getMeasurementUnit())) {
					quantity = convertStockXMU(quantity, detail.getMeasurementUnit(),
							detail.getProduct().getMeasurementUnit());
					log.info(strLog + "quantity convertido: " + quantity);
				}

			} else if (transactionType.equals(ETransactionType.SALIDA)) {
				finalStockMU = finalStockMU - quantity;// Se resta al inventario

				log.info(strLog + "finalStockMU: " + finalStockMU);

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

				finalStock = initialStock != 0 ? initialStock - quantity : quantity; // Se resta al inventario

			}

			// Crear objeto de inventario
			inventoryTransaction = InventoryTransaction.builder().product(detail.getProduct())
					.transactionType(transactionType).initialStock(initialStock).quantity(quantity)
					.finalStock(finalStock).document(document).build();

			// Guardar movimiento de inventario
			inventoryBll.save(inventoryTransaction, false);

			log.info(strLog + "inventoryTransaction saved: " + inventoryTransaction);

			// Actualizar el stock para la UM escogida
			muProduct.setStock(finalStockMU);

			// Actualizar precio del producto
			updatePrice(muProduct, detail);

			// Actualizar stock de cada UM asociada al producto
			updateStockByMU(detail.getProduct(), muProduct);

		} catch (ModelValidationException ex) {
			inventoryTransaction = null;
			log.error(strLog + "[ModelValidationException]" + ex);
		} catch (HibernateException ex) {
			inventoryTransaction = null;
			log.error(strLog + "[HibernateException]" + ex);
		} catch (Exception e) {
			log.error(strLog + "[Exception]" + e.getMessage());
		}

		return inventoryTransaction;
	}

	/**
	 * Metodo para actualizar el stock del producto de acuerdo a la tx de venta o
	 * compra
	 * 
	 * @param inventoryTransaction
	 */
	private Product saveProduct(InventoryTransaction inventoryTransaction) {
		String strLog = "[saveProduct] ";
		Product product = null;
		try {
			log.info(strLog + "[parameters] " + inventoryTransaction);
			product = productBll.select(inventoryTransaction.getProduct().getCode());
			product.setStock(inventoryTransaction.getFinalStock());
			product.setStockDate(new Date());

			// Actualizar stock producto
			productBll.save(product, false);
			log.info("product saved:" + product);

		} catch (Exception e) {
			product = null;
			inventoryBll.rollback();
			log.error(strLog + "[Exception]" + e.getMessage());
		}

		return product;

	}

	/**
	 * Actualizar el stock del lote de acuerdo a la tx venta o compra
	 * 
	 * @param document
	 * @param detail
	 */
	private boolean saveLot(Document document, DocumentDetail detail) {
		String strLog = "[saveLot] ";
		Lot lot = null;
		Double quantity = detail.getQuantity();
		Double initialStockLot = 0.0;
		Double quantityLot = 0.0;
		Double finalStockLot = 0.0;
		boolean hasErrors = false;
		try {

			// Buscar la lista de lotes asociados a un detail
			// DocumentDetail detailTmp = DocumentDetail.builder().document(null).build();

			// Detail sin relacion al documento
			DocumentDetail detailTmp = documentDetail(detail);

			List<DocumentDetailLot> detailLotList = getDetailLotsByDetail(detailTmp);

			for (DocumentDetailLot detailLot : detailLotList) {
				Double qty = detailLot.getQuantity();
				// Consultar lote
				lot = detailLot.getLot();
				log.info(strLog + "Lote a actualizar: " + lot);

				if (transactionType.equals(ETransactionType.ENTRADA)) {
					// Si la UM es diferente a la UM del lote se debe convertir
					if (!detail.getMeasurementUnit().equals(lot.getMeasurementUnit())) {
						qty = convertStockXMU(qty, detail.getMeasurementUnit(), lot.getMeasurementUnit());
						log.info(strLog + "quantity convertido: " + quantity);
					}

					// Para el lote
					initialStockLot = 0.0;
					finalStockLot = detailLot.getQuantity();
					quantityLot = qty;

				} else if (transactionType.equals(ETransactionType.SALIDA)) {
					initialStockLot = lot.getQuantity();// stock del lote

					if (!detail.getMeasurementUnit().equals(lot.getMeasurementUnit())) {
						// Si la UM es diferente a la UM principal se debe convertir
						// Convertir stock del lote
						initialStockLot = convertStockXMU(initialStockLot, detail.getMeasurementUnit(),
								lot.getMeasurementUnit());
						log.info(strLog + "initialStock del lote convertido: " + lot);

						// Convertir quantity de la tx
						quantity = convertStockXMU(qty, detail.getMeasurementUnit(), lot.getMeasurementUnit());
						log.info(strLog + "quantity convertido: " + quantity);
					}

					finalStockLot = initialStockLot - qty;
					quantityLot = qty;
				}

				log.info(strLog + "initialStockLot: " + initialStockLot);
				log.info(strLog + "quantity del movimiento Lot:" + quantityLot);
				log.info(strLog + "finalStockLot :" + finalStockLot);

				// Actualizar la cantidad del lote
				lot.setQuantity(finalStockLot);
				lot.setNew(false);

				lotBll.save(lot, false);
				log.info(strLog + "lot saved:" + lot);

				Lot lotEntity = lotBll.select(lot.getCode(), lot.getProduct());

				// Actualizar lot en el detail
				detail.setDocument(document);
				detailLot.setDocumentDetail(detail);
				detailLot = DocumentDetailLot.builder(detailLot).lot(lotEntity).documentDetail(detail).build();
				hasErrors = saveDetailLot(detailLot);

				if (hasErrors) {
					break;
				}

			}

		} catch (Exception e) {
			hasErrors = true;
			log.error(strLog + "[Exception]" + e.getMessage());
		}

		return hasErrors;
	}

	/**
	 * Guardar la relación de detalle de factura con lote
	 * 
	 * @param detailLot
	 */
	private boolean saveDetailLot(DocumentDetailLot detailLot) {
		String strLog = "[saveDetailLot] ";
		boolean hasErrors = false;
		try {
			log.info(strLog + "[parameters]" + detailLot);

			detailLotBll.save(detailLot, false);

			log.info(strLog + "detailLot saved:" + detailLot);

		} catch (Exception e) {
			hasErrors = true;
			detailLotBll.rollback();
			log.error(strLog + "[Exception]" + e.getMessage());
			e.printStackTrace();
		}
		return hasErrors;
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

			Double returnTotal = txtReturnTotal.getValue() != null ? Double.parseDouble(txtReturnTotal.getValue()) : 0;
			Double total = txtTotal.getValue() != null ? Double.parseDouble(txtTotal.getValue()) : 0;
			if (total >= invoiceTotal) {

				Double totalIVA = !StringUtil.isBlank(txtReturnTotalTax.getValue())
						? Double.parseDouble(txtReturnTotalTax.getValue())
						: 0.0;

				// Setear el detalle de items a la factura
				Set<DocumentDetail> details = detailGrid.getDataProvider().fetch(new Query<>())
						.collect(Collectors.toSet());

				Set<DocumentDetail> detailsTmp = new HashSet<>();
				for (DocumentDetail detail : details) {
					if (detail.getProduct() != null) {
						if (detail.getQuantity() == null || detail.getQuantity().equals(0.0)) {
							hasErrors = true;
							ViewHelper.showNotification("Cantidad no ingresada para producto: " + detail.getCode(),
									Notification.Type.ERROR_MESSAGE);
							break;
						} else {
							if (detail.getDiffQuantity() != null) {

								// Detail sin relacion al documento
								DocumentDetail detailTmp = documentDetail(documentEntity, detail);

								detailsTmp.add(detailTmp);
								itemList.add(detail);
							}
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

					Date docDate = DateUtil.localDateTimeToDate(dtfReturnDate.getValue());
					Date expirationDate = dtfExpirationDate.getValue() != null
							? DateUtil.localDateTimeToDate(dtfExpirationDate.getValue())
							: null;

					documentEntity = docBuilder.code(txtReturnNum.getValue())
							.reference(txtReference.getValue() != null ? txtReference.getValue() : "")
							.documentType(documentType).resolution(txtResolution.getValue()).person(selectedPerson)
							.documentDate(docDate).paymentMethod(cbPaymentMethod.getValue()).paymentType(paymentType)
							.paymentTerm(txtPaymentTerm.getValue()).expirationDate(expirationDate)
							.totalValue(returnTotal).totalValueNoTax(totalIVA).status(documentStatus)
							.salesman(user.getPerson()).payValue(payValue).details(detailsTmp).build();

					// Persistir documento
					documentBll.save(documentEntity, false);
					// Consultar el documento guardado
					documentSaved = documentBll.select(documentEntity.getCode(), documentEntity.getDocumentType());
				}
			} else {
				ViewHelper.showNotification("El valor total debe ser mayor o igual al de la factura inicial",
						Notification.Type.ERROR_MESSAGE);
			}
		} catch (

		ModelValidationException ex) {
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

	private DocumentDetail documentDetail(DocumentDetail detail) {
		DocumentDetail detailTmp = DocumentDetail.builder().product(detail.getProduct()).quantity(detail.getQuantity())
				.description(detail.getDescription()).subtotal(detail.getSubtotal())
				.measurementUnit(detail.getMeasurementUnit()).measurementUnitProduct(detail.getMeasurementUnitProduct())
				.price(detail.getPrice()).tax(detail.getTax()).discount(detail.getDiscount()).build();

		detailTmp.setDiffQuantity(detail.getDiffQuantity());
		detailTmp.setOldQuantity(detail.getOldQuantity());
		detailTmp.setTaxValue(detail.getTaxValue());
		detailTmp.setTotalTaxValue(detail.getTotalTaxValue());

		return detailTmp;

	}

	private DocumentDetail documentDetail(Document document, DocumentDetail detail) {
		DocumentDetail detailTmp = DocumentDetail.builder().product(detail.getProduct()).quantity(detail.getQuantity())
				.description(detail.getDescription()).subtotal(detail.getSubtotal())
				.measurementUnit(detail.getMeasurementUnit()).measurementUnitProduct(detail.getMeasurementUnitProduct())
				.price(detail.getPrice()).tax(detail.getTax()).discount(detail.getDiscount()).build();

		detailTmp.setDiffQuantity(detail.getDiffQuantity());
		detailTmp.setOldQuantity(detail.getOldQuantity());
		detailTmp.setTaxValue(detail.getTaxValue());
		detailTmp.setTotalTaxValue(detail.getTotalTaxValue());

		return detailTmp;

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

	/**
	 * Metodo para limpiar los campos de la pantalla
	 */
	private void cleanButtonAction() {
		String strLog = "[cleanButtonAction]";

		try {
			invoiceDocument = null;
			selectedPerson = null;
			selectedProduct = null;
			selectedLot = null;

			txtDocNumber.clear();
			txtDocNumFilter.clear();
			txtReturnNum.clear();
			txtReturnNum.setReadOnly(false);
			txtReference.clear();
			txtPerson.clear();
			txtPaymentTerm.clear();
			// cbDocumentType.clear();
			cbPaymentMethod.clear();
			// cbPaymentType.clear();
			cbDocumentStatus.setSelectedItem(docStatusBll.select("Nueva").get(0));
			itemListOld.clear();
			initializeGrid();
			detailGrid.getDataProvider().refreshAll();
			txtDocNumFilter.clear();
			// getNextReturnNumber(cbDocumentType.getSelectedItem().get());
			// disableComponents(false);
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
	private void searchInvoice(String documentNumber, EDocumentType eDocType) {
		String strLog = "[searchInvoice]";
		try {
			// cleanButtonAction();
			log.info(strLog + "[parameters] documentNumber: " + documentNumber + ", eDocType: " + eDocType.getName());
			DocumentType documentType = documentTypeBll.select(eDocType.getName());
			if (documentNumber != null && !documentNumber.isEmpty() && documentType != null) {
				invoiceDocument = documentBll.select(documentNumber, documentType);
				if (invoiceDocument != null) {
					cbDocumentType.setValue(invoiceDocument.getDocumentType());
					txtDocNumber.setValue(invoiceDocument.getCode());
					txtReference.setValue(invoiceDocument.getReference() != null ? invoiceDocument.getReference() : "");
					txtResolution
							.setValue(invoiceDocument.getResolution() != null ? invoiceDocument.getResolution() : "");
					dtfDocumentDate.setValue(DateUtil.dateToLocalDateTime(invoiceDocument.getDocumentDate()));

					// Llenar lista de tipos de pago
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

					PaymentDocumentType payDocType = paymentDocumentTypeBll.select(invoiceDocument.getDocumentType(),
							invoiceDocument.getPaymentType());
					cbPaymentType.setValue(payDocType);

					cbPaymentMethod.setValue(invoiceDocument.getPaymentMethod());
					txtPaymentTerm
							.setValue(invoiceDocument.getPaymentTerm() != null ? invoiceDocument.getPaymentTerm() : "");
					selectedPerson = invoiceDocument.getPerson();
					txtPerson.setValue(
							invoiceDocument.getPerson().getName() + " " + invoiceDocument.getPerson().getLastName());
					dtfExpirationDate.setValue(DateUtil.dateToLocalDateTime(invoiceDocument.getExpirationDate()));
					cbDocumentStatus.setValue(invoiceDocument.getStatus());

					// Cargar detalle de factura
					Set<DocumentDetail> detailSet = invoiceDocument.getDetails();
					itemListOld = new ArrayList<>();
					for (Iterator<DocumentDetail> iterator = detailSet.iterator(); iterator.hasNext();) {
						DocumentDetail detail = iterator.next();
						// Buscar los detailLot asociados al item
						List<DocumentDetailLot> detailLots = detailLotBll.select(detail);

						// Guardar temporalmente la cantidad inicial de cada item de factura
						detail.setOldQuantity(detail.getQuantity());

						for (DocumentDetailLot detailLot : detailLots) {
							detailLot.setDocumentDetail(detail); // Actualizar el item de DetailLot
							detailLotCollectionOld.add(detailLot);
						}

						itemListOld.add(detail);

					}
					fillDetailGridData(itemListOld);

					invoiceTotal = Double.parseDouble(txtTotal.getValue());

					if (eDocType.equals(EDocumentType.DV)) {
						if (role.getName().equals(ERole.SUDO.getName())
								|| role.getName().equals(ERole.MANAGER.getName())) {
							disableComponents(false);
						} else {
							disableComponents(true);
						}
					} else {
						disableHeaderComponents(true);
					}
				}
			}

		} catch (Exception e) {
			log.error(strLog + "[Exception]" + e.getMessage());
			e.printStackTrace();
			ViewHelper.showNotification("Se presentó un error al consultar la factura",
					Notification.Type.ERROR_MESSAGE);
		}
	}

	/**
	 * Método para inactivar todos los componentes
	 * 
	 * @param disable
	 */
	private void disableComponents(Boolean disable) {
		disableButtonComponents(disable);
		disableHeaderComponents(disable);
		disableDetailComponents(disable);
	}

	/**
	 * Método para inactivar botones
	 * 
	 * @param disable
	 */
	private void disableButtonComponents(Boolean disable) {
		searchPersonBtn.setEnabled(!disable);
		addProductBtn.setEnabled(!disable);
		deleteProductBtn.setEnabled(!disable);
	}

	/**
	 * Método para inactivar componentes del detalle
	 * 
	 * @param disable
	 */
	private void disableDetailComponents(Boolean disable) {
		searchPersonBtn.setEnabled(!disable);
		addProductBtn.setEnabled(!disable);
		deleteProductBtn.setEnabled(!disable);
	}

	/**
	 * Metodo para inactivar componentes del header
	 * 
	 * @param disable
	 */

	private void disableHeaderComponents(Boolean disable) {
		cbDocumentType.setReadOnly(disable);
		txtDocNumber.setReadOnly(disable);
		txtReference.setReadOnly(disable);
		txtResolution.setReadOnly(disable);
		dtfDocumentDate.setReadOnly(disable);
		cbPaymentType.setReadOnly(disable);
		cbPaymentMethod.setReadOnly(disable);
		txtPaymentTerm.setReadOnly(disable);
		txtPerson.setReadOnly(disable);
		searchPersonBtn.setEnabled(!disable);
		dtfExpirationDate.setReadOnly(disable);
		cbDocumentStatus.setReadOnly(disable);
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
				itemListOld.remove(detail);
				fillDetailGridData(itemListOld);
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
		if (invoiceDocument != null) {
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
			Document documentEntity = Document.builder(invoiceDocument).status(status).build();
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
			log.info(strLog + " document: " + invoiceDocument);
			if (invoiceDocument != null && invoiceDocument.getCode() != null) {
				if (pdfGenerator != null) {
					String filePath = pdfGenerator.generate(createReportParameters(), invoiceDocument.getDetails());

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
			if (returnDocument != null) {
				parameters.put(Commons.PARAM_INVOICE_NUMBER, returnDocument.getCode());
				parameters.put(Commons.PARAM_INVOICE_DATE, DateUtil.dateToString(invoiceDocument.getDocumentDate()));
				String invoiceType;

				if (returnDocument.getDocumentType().getTransactionType().equals(ETransactionType.ENTRADA)) {
					invoiceType = returnDocument.getDocumentType().getName();
				} else {
					PaymentType payType = returnDocument.getPaymentType();
					if (payType.getCode().equals(EPaymemtType.CREDIT.getName())) {
						invoiceType = "CREDITO";
					} else {
						invoiceType = "CONTADO";
					}
				}

				parameters.put(Commons.PARAM_INVOICE_TYPE, invoiceType);
				parameters.put(Commons.PARAM_SALESMAN,
						returnDocument.getSalesman().getName() + " " + returnDocument.getSalesman().getLastName());
				parameters.put(Commons.PARAM_CUSTOMER,
						returnDocument.getPerson().getName() + " " + returnDocument.getPerson().getLastName());

				parameters.put(Commons.PARAM_CUSTOMER_ID,
						returnDocument.getPerson().getDocumentNumber() != null
								? returnDocument.getPerson().getDocumentNumber()
								: "");
				parameters.put(Commons.PARAM_CUSTOMER_ADDRESS,
						returnDocument.getPerson().getAddress() != null ? returnDocument.getPerson().getAddress() : "");
				parameters.put(Commons.PARAM_CUSTOMER_PHONE,
						returnDocument.getPerson().getMobile() != null ? returnDocument.getPerson().getMobile() : "");
				parameters.put(Commons.PARAM_CASH,
						returnDocument.getPayValue() != null ? returnDocument.getPayValue() : 0.0);
				parameters.put(Commons.PARAM_CHANGE,
						returnDocument.getPayValue() != null
								? returnDocument.getPayValue() - returnDocument.getTotalValue()
								: 0.0);
			}
		} catch (Exception e) {

		}
		return parameters;
	}

	/**
	 * Metodo que construye la ventana para mostrar el cambio de dinero
	 */
	private boolean buildCashChangeWindow() {
		String strLog = "[buildCashChangeWindow]";

		cashChangeWindow = ViewHelper.buildSubwindow("50%", null);

		try {
			Double totalValue = txtReturnTotal.getValue() != null ? Double.parseDouble(txtReturnTotal.getValue()) : 0;
			cashChangeLayout = new CashChangeLayout(this, totalValue);
			cashChangeLayout.setMargin(false);
			cashChangeLayout.setSpacing(false);

			VerticalLayout subContent = ViewHelper.buildVerticalLayout(true, true);
			subContent.addComponents(cashChangeLayout);
			cashChangeWindow.setContent(subContent);

			getUI().addWindow(cashChangeWindow);
		} catch (Exception e) {
			log.error(strLog + "[Exception] " + e);
		}

		return true;

	}

	public Document getDocument() {
		return returnDocument;
	}

	public void setDocument(Document document) {
		this.returnDocument = document;
	}

	private void buildMeasurementUnitComponent() {
		Product product = selectedProduct != null ? selectedProduct
				: CommonsConstants.CURRENT_DOCUMENT_DETAIL.getProduct();
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

			setQuantityComponent(CommonsConstants.CURRENT_DOCUMENT_DETAIL.getQuantity(), e.getOldValue(), e.getValue());

		});

		Binder<DocumentDetail> binder = detailGrid.getEditor().getBinder();
		Binding<DocumentDetail, MeasurementUnit> binding = binder.bind(cbMeasurementUnit,
				DocumentDetail::getMeasurementUnit, DocumentDetail::setMeasurementUnit);
		columnUM.setEditorBinding(binding);

	}

	/**
	 * Metodo para actualizar el campo cantidad en el detalle de factura
	 * 
	 * @param value
	 * @param sourceMU
	 * @param targetMU
	 */
	private void setQuantityComponent(Double value, MeasurementUnit sourceMU, MeasurementUnit targetMU) {
		// Binding de cantidad

		NumberField txtQuantity = new NumberField();
		Double newQty = convertStockXMU(value, sourceMU, targetMU);
		txtQuantity.setValue(newQty);
		Binder<DocumentDetail> binder = detailGrid.getEditor().getBinder();
		Binding<DocumentDetail, String> quantityBinding = binder.bind(txtQuantity, DocumentDetail::getQuantityStr,
				DocumentDetail::setQuantityStr);
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

				CommonsConstants.MEASUREMENT_UNIT_PRODUCT = muProduct;
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
	 * Método para buscar una persona por código y/o nombre
	 * 
	 * @param code
	 * @param name
	 */
	private void searchPerson(String name) {
		String strLog = "[searchPerson] ";
		List<Person> persons = null;
		try {
			log.info(strLog + "[parameters] name: " + name);

			if (name != null && !name.isEmpty()) {
				persons = personBll.selectByName(name);
				int size = persons.size();
				log.info(strLog + "Cantidad de personas consultados: " + size);
				if (size == 1) {
					selectPerson(persons.get(0));
				} else {
					buildPersonWindow(persons);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			log.error(strLog + "[Exception]" + e.getMessage());
		}
	}

	/**
	 * Metodo para construir la venta modal de personas
	 * 
	 * @param personFiltter
	 */
	private void buildPersonWindow(List<Person> persons) {

		personSubwindow = ViewHelper.buildSubwindow("75%", null);
		personSubwindow.setCaption("Personas");

		VerticalLayout subContent = ViewHelper.buildVerticalLayout(true, true);

		try {

			if (transactionType.equals(ETransactionType.ENTRADA)) {
				Commons.PERSON_TYPE = PersonType.SUPPLIER.getName();
			}
			if (transactionType.equals(ETransactionType.SALIDA)) {
				Commons.PERSON_TYPE = PersonType.CUSTOMER.getName();
			}
			personLayout = new PersonLayout(ELayoutMode.LIST, persons);

			personLayout.getGrid().addItemClickListener(listener -> {
				if (listener.getMouseEventDetails().isDoubleClick())
					selectPerson(listener.getItem());
			});

			// Evento de enter para grid
			personLayout.getGrid()
					.addShortcutListener(new ShortcutListener("search person", ShortcutAction.KeyCode.ENTER, null) {
						private static final long serialVersionUID = 8741523733731956234L;

						@SuppressWarnings("unchecked")
						@Override
						public void handleAction(Object sender, Object target) {
							try {
								if (((Grid<Person>) target).equals(personLayout.getGrid())) {
									selectPerson(personLayout.getSelected());
								}

							} catch (Exception e) {
								log.error(
										"[search person][ShortcutListener][handleAction][Exception] " + e.getMessage());
							}
						}
					});

			Panel personPanel = ViewHelper.buildPanel(null, personLayout);
			subContent.addComponents(personPanel);

			personSubwindow.setContent(subContent);
			getUI().addWindow(personSubwindow);

		} catch (IOException e) {
			log.error("Error al cargar lista de personas. Exception:" + e);
		}

	}

	public Double getPayValue() {
		return payValue;
	}

	public void setPayValue(Double payValue) {
		this.payValue = payValue;
	}

}
