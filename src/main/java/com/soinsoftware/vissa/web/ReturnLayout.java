package com.soinsoftware.vissa.web;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.persistence.PersistenceException;

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
import com.soinsoftware.vissa.util.EModeLayout;
import com.soinsoftware.vissa.util.PermissionUtil;
import com.soinsoftware.vissa.util.ViewHelper;
import com.vaadin.data.Binder;
import com.vaadin.data.Binder.Binding;
import com.vaadin.data.provider.ConfigurableFilterDataProvider;
import com.vaadin.data.provider.ListDataProvider;
import com.vaadin.data.provider.Query;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.server.FileResource;
import com.vaadin.server.FontAwesome;
import com.vaadin.server.Page;
import com.vaadin.server.SerializablePredicate;
import com.vaadin.server.VaadinService;
import com.vaadin.shared.ui.datefield.DateTimeResolution;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.DateTimeField;
import com.vaadin.ui.Embedded;
import com.vaadin.ui.Grid;
import com.vaadin.ui.Grid.Column;
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
	private final DocumentDetailLotBll docDetailLotBll;
	private final DocumentDetailLotBll detailLotBll;
	private final CompanyBll companyBll;
	private final PaymentDocumentTypeBll paymentDocumentTypeBll;
	private final MeasurementUnitProductBll measurementUnitProductBll;

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
	private ConfigurableFilterDataProvider<DocumentDetail, Void, SerializablePredicate<DocumentDetail>> filterDataProv;
	private Column<?, ?> columnQuantity;
	private Column<?, ?> columnSubtotal;
	private Column<DocumentDetail, String> columnTax;
	private Column<DocumentDetail, String> columnPrice;
	private Column<DocumentDetail, String> columnDiscount;
	private Column<DocumentDetail, MeasurementUnit> columnUM;
	private FooterRow footer;

	private boolean withoutLot;

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

	public ReturnLayout() throws IOException {
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
		docDetailLotBll = DocumentDetailLotBll.getInstance();
		lotBll = LotBll.getInstance();
		document = new Document();
		itemsList = new ArrayList<>();
		transactionType = ETransactionType.valueOf(CommonsUtil.TRANSACTION_TYPE);
		company = companyBll.selectAll().get(0);

	}

	@Override
	public void enter(ViewChangeEvent event) {

		View.super.enter(event);

		this.user = getSession().getAttribute(User.class);
		this.role = user.getRole();
		this.permissionUtil = new PermissionUtil(user.getRole().getPermissions());

		String title = "Devoluciones";
		Label tittle = new Label(title);
		tittle.addStyleName(ValoTheme.LABEL_H1);
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

		txtDocNumFilter = new TextField("Número de factura");

		txtDocNumFilter.addValueChangeListener(e -> searchDocument(txtDocNumFilter.getValue()));
		txtDocNumFilter.setStyleName(ValoTheme.TEXTFIELD_TINY);

		layout.addComponents(txtDocNumFilter);

		return ViewHelper.buildPanel(null, layout);
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

		txtDocNumber = new TextField("Número de factura");
		txtDocNumber.setReadOnly(true);
		txtDocNumber.setStyleName(ValoTheme.TEXTFIELD_TINY);

		txtResolution = new TextField("Resolución de factura");
		txtResolution.setStyleName(ValoTheme.TEXTFIELD_TINY);
		txtResolution.setReadOnly(true);
		txtResolution.setValue(company.getInvoiceResolution());

		txtReference = new TextField("Referencia");
		txtReference.setStyleName(ValoTheme.TEXTFIELD_TINY);
		// txtReference.setWidth("40%");

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

		cbPaymentType = new ComboBox<>("Forma de pago");
		cbPaymentType.setEmptySelectionAllowed(false);
		cbPaymentType.setEmptySelectionCaption("Seleccione");
		cbPaymentType.setStyleName(ValoTheme.COMBOBOX_TINY);
		cbPaymentType.setRequiredIndicatorVisible(true);

		txtPaymentTerm = new TextField("Plazo en días");
		txtPaymentTerm.setStyleName(ValoTheme.TEXTFIELD_TINY);
		txtPaymentTerm.setReadOnly(true);

		cbPaymentType.addValueChangeListener(e -> {
			if (cbPaymentType.getSelectedItem().get().getPaymentType().getCode()
					.equals(EPaymemtType.CREDIT.getName())) {
				txtPaymentTerm.setReadOnly(false);
			} else {
				txtPaymentTerm.setReadOnly(true);
			}
		});

		dtfExpirationDate = new DateTimeField("Fecha de Vencimiento");
		dtfExpirationDate.setReadOnly(true);
		dtfExpirationDate.setDateFormat(Commons.FORMAT_DATE_TIME);
		dtfExpirationDate.setStyleName(ValoTheme.DATEFIELD_TINY);
		dtfExpirationDate.setWidth("184px");

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

		cbDocumentStatus = new ComboBox<DocumentStatus>("Estado de la factura");
		cbDocumentStatus.setEmptySelectionAllowed(false);
		cbDocumentStatus.setEmptySelectionCaption("Seleccione");
		ListDataProvider<DocumentStatus> docStatusDataProv = new ListDataProvider<>(docStatusBll.selectAll());
		cbDocumentStatus.setDataProvider(docStatusDataProv);
		cbDocumentStatus.setItemCaptionGenerator(DocumentStatus::getName);
		cbDocumentStatus.setSelectedItem(docStatusBll.select("Nueva").get(0));
		cbDocumentStatus.setStyleName(ValoTheme.COMBOBOX_TINY);
		cbDocumentStatus.setReadOnly(true);

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
		}

		if (transactionType.equals(ETransactionType.SALIDA)) {
			DocumentType docType = docTypeDataProv.getItems().iterator().next();
			cbDocumentType.setValue(docType);
			getNextDocumentNumber(docType);
		}

		headerLayout1.addComponents(txtPerson, searchPersonBtn);

		HorizontalLayout headerLayout2 = ViewHelper.buildHorizontalLayout(false, false);
		headerLayout2.addComponents(dtfDocumentDate, cbPaymentType, txtPaymentTerm, dtfExpirationDate, cbPaymentMethod);

		headerLayout1.setComponentAlignment(searchPersonBtn, Alignment.BOTTOM_CENTER);

		HorizontalLayout headerLayout3 = ViewHelper.buildHorizontalLayout(false, false);
		headerLayout3.addComponents(cbDocumentStatus, txtTotal, txtTotalTax);

		verticalLayout.addComponents(headerLayout1, headerLayout2, headerLayout3);
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
		addProductBtn = new Button("Agregar producto", FontAwesome.PLUS);
		addProductBtn.addStyleName(ValoTheme.BUTTON_TINY);
		addProductBtn.addClickListener(e -> buildProductWindow());

		deleteProductBtn = new Button("Eliminar producto", FontAwesome.ERASER);
		deleteProductBtn.addStyleName(ValoTheme.BUTTON_TINY);
		deleteProductBtn.addClickListener(e -> deleteItemDetail());

		buttonlayout.addComponents(addProductBtn, deleteProductBtn);

		detailGrid = new Grid<>();
		detailGrid.setSizeFull();
		detailGrid.setEnabled(true);

		// columns
		detailGrid.addColumn(documentDetail -> {
			if (documentDetail.getProduct() != null) {
				return documentDetail.getProduct().getCode();
			} else {
				return null;
			}
		}).setCaption("Código");
		detailGrid.addColumn(documentDetail -> {
			if (documentDetail.getProduct() != null) {
				return documentDetail.getProduct().getName();
			} else {
				return null;
			}
		}).setCaption("Nombre");

		// Columna Unidad de medida editable
		columnUM = detailGrid.addColumn(DocumentDetail::getMeasurementUnit).setCaption("Unidad de medida");

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

		txtQuantity.setMaxLength(100);
		txtQuantity.setRequiredIndicatorVisible(true);

		txtQuantity.addBlurListener(e -> changeQuantity(txtQuantity.getValue()));
		footer = detailGrid.prependFooterRow();
		if (columnDiscount != null) {
			footer.getCell(columnDiscount).setHtml("<b>Total IVA:</b>");
		} else {
			footer.getCell(columnPrice).setHtml("<b>Total IVA:</b>");
		}
		footer.getCell(columnQuantity).setHtml("<b>Total:</b>");

		detailGrid.getEditor().setEnabled(true);
		detailGrid.getEditor().addSaveListener(e -> changeQuantity(txtQuantity.getValue()));

		HorizontalLayout itemsLayout = ViewHelper.buildHorizontalLayout(false, false);
		itemsLayout.setSizeFull();
		itemsLayout.addComponents(ViewHelper.buildPanel(null, detailGrid));

		layout.addComponents(buttonlayout, itemsLayout);

		return ViewHelper.buildPanel("Productos", layout);
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

				if (qty > 0) {
					currentDetail = CommonsUtil.CURRENT_DOCUMENT_DETAIL;
					log.info(strLog + "currentDetail:" + currentDetail);
					// if (!withoutLot) {
					DocumentDetailLot detailLot = detailLotMap.get(currentDetail);
					log.info(strLog + "detailLot:" + detailLot);
					if (detailLot != null) {
						if (transactionType.equals(ETransactionType.SALIDA) && qty > detailLot.getInitialStockLot()) {
							message = "Cantidad ingresada es mayor al stock del lote producto";
							throw new Exception(message);
						} else {
							Double finalStock = 0.0;
							Double diffQty = 0.0;
							Double oldQty = Double.parseDouble(currentDetail.getOldQuantity());
							if (qty > oldQty) {
								diffQty = (qty - oldQty);
								currentDetail.setTransactionType(ETransactionType.SALIDA);
							} else if (qty < oldQty) {
								diffQty = oldQty - qty;
								currentDetail.setTransactionType(ETransactionType.ENTRADA);
							}
							currentDetail.setDiffQuantity(String.valueOf(diffQty));

							log.info(strLog + "initialStock:" + detailLot.getInitialStockLot());

							log.info(strLog + "Diferencia de cantidad inicia con la final:" + diffQty);

							if (currentDetail.getTransactionType().equals(ETransactionType.ENTRADA)) {
								finalStock = detailLot.getInitialStockLot() + diffQty;
							} else if (currentDetail.getTransactionType().equals(ETransactionType.SALIDA)) {
								finalStock = detailLot.getInitialStockLot() - diffQty;
							}

							log.info(strLog + "finalStock:" + finalStock);
							DocumentDetailLot detailLotTmp = DocumentDetailLot.builder(detailLot).quantity(diffQty)
									.finalStockLot(finalStock).build();

							log.info(strLog + "currentDetail again:" + currentDetail);
							log.info(strLog + "detailLotTmp:" + detailLotTmp);
							detailLotMap.put(currentDetail, detailLotTmp);
						}
					}

					CommonsUtil.CURRENT_DOCUMENT_DETAIL = currentDetail;

					/*
					 * } else { if (transactionType.equals(ETransactionType.SALIDA) && qty >
					 * currentDetail.getProduct().getStock()) { ViewHelper.
					 * showNotification("Cantidad ingresada es mayor al stock del producto",
					 * Notification.Type.ERROR_MESSAGE); } }
					 */
					correct = true;

				} else {
					message = "La cantidad debe ser mayor a 0";
					throw new Exception(message);
				}
			}

		} catch (NumberFormatException nfe) {
			log.error(strLog + "[NumberFormatException]" + nfe.getMessage());
			message = "Formato de cantidad no valido";
		} catch (Exception e) {
			log.error(strLog + "[Exception]" + e.getMessage());
			message = e.getMessage();
		} finally {
			log.info("Correct: " + correct);
			if (!correct && (message != null && !message.isEmpty())) {
				ViewHelper.showNotification(message, Notification.Type.ERROR_MESSAGE);
			}
		}
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
					return documentDetail.getTax();
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
		Panel buttonPanel = ViewHelper.buildPanel(null, buttonLayout);

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
		subContent.addComponents(buttonPanel, personPanel);

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
			ViewHelper.showNotification("Campo plazo inválido", Notification.TYPE_ERROR_MESSAGE);
		}

	}

	/**
	 * Metodo que construye la venta para buscar productos
	 */
	private void buildProductWindow() {
		selectedProduct = null;
		selectedLot = null;
		withoutLot = false;

		productSubwindow = ViewHelper.buildSubwindow("75%", null);
		productSubwindow.setCaption("Productos");

		VerticalLayout subContent = ViewHelper.buildVerticalLayout(true, true);

		// Panel de botones
		Button backBtn = new Button("Cancelar", FontAwesome.BACKWARD);
		backBtn.addStyleName("mystyle-btn");
		backBtn.addClickListener(e -> closeWindow(productSubwindow));

		Button selectBtn = new Button("Seleccionar", FontAwesome.CHECK);
		selectBtn.addStyleName("mystyle-btn");
		selectBtn.addClickListener(e -> selectProduct(null));

		HorizontalLayout buttonLayout = ViewHelper.buildHorizontalLayout(true, true);
		buttonLayout.addComponents(backBtn, selectBtn);
		Panel buttonPanel = ViewHelper.buildPanel(null, buttonLayout);

		try {
			productLayout = new ProductLayout(EModeLayout.LIST, null);

			productLayout.productGrid.addItemClickListener(listener -> {
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
		subContent.addComponents(buttonPanel, productPanel);

		productSubwindow.setContent(subContent);
		getUI().addWindow(productSubwindow);

	}

	/**
	 * Metodo para escoger productos a agregar a la factura
	 */
	private void selectProduct(Product product) {
		String strLog = "[selectProduct]";
		try {
			selectedProduct = product != null ? product : productLayout.getSelected();

			if (selectedProduct != null) {

				DocumentDetail docDetail = DocumentDetail.builder().product(selectedProduct).build();

				if (itemsList.contains(docDetail)) {
					ViewHelper.showNotification("Este producto ya está agregado a la factura",
							Notification.Type.WARNING_MESSAGE);
				} else {
					if (selectedProduct.getSalePrice() == null || selectedProduct.getSalePrice().equals("0")) {
						ViewHelper.showNotification("El producto no tiene precio configurado",
								Notification.Type.WARNING_MESSAGE);
					} else if (transactionType.equals(ETransactionType.SALIDA)
							&& (selectedProduct.getStock() == null || selectedProduct.getStock() == 0)) {
						ViewHelper.showNotification("El producto no tiene stock disponible",
								Notification.Type.WARNING_MESSAGE);
					} else {
						try {

							// ---Panel de lotes
							log.info("selectedLot:" + selectedLot + ", withoutLot:" + withoutLot);
							// if (selectedLot == null && !withoutLot) {
							buildLotWindow(docDetail);
							// }
						} catch (Exception e) {
							log.error(strLog + "[Exception]" + "Error al cargar lotes del producto. Exception: "
									+ e.getMessage());
							e.printStackTrace();
						}
					}
				}

			} else

			{
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
				docDetail.setMeasurementUnitList(muList);
				MeasurementUnit mu = muList.get(0);
				docDetail.setMeasurementUnit(mu);
				// Setear el precio e impuesto
				MeasurementUnitProduct priceXMu = selectPriceXMU(mu, selectedProduct);
				docDetail.setMeasurementUnitProduct(priceXMu);

				if (transactionType.equals(ETransactionType.ENTRADA)) {
					docDetail.setPrice(priceXMu.getPurchasePrice());
					docDetail.setTax(priceXMu.getPurchaseTax());
				} else if (transactionType.equals(ETransactionType.SALIDA)) {
					docDetail.setPrice(priceXMu.getSalePrice());
					docDetail.setTax(priceXMu.getSaleTax());
				}

				itemsList.add(docDetail);
				fillDetailGridData(itemsList);
				setMeasurementUnitComponent();
				productSubwindow.close();
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
		// Si hay lote, se asocia al registro de detail
		if (selectedLot != null) {
			DocumentDetailLot detailLot = DocumentDetailLot.builder().documentDetail(docDetail).lot(selectedLot)
					.initialStockLot(selectedLot.getQuantity()).build();
			detailLotMap.put(docDetail, detailLot);
		}
	}

	/**
	 * Metodo que construye la ventana para buscar lores
	 */
	private void buildLotWindow(DocumentDetail detail) {
		String strLog = "[buildLotWindow]";
		try {
			withoutLot = false;

			Product product = detail.getProduct();
			lotSubwindow = ViewHelper.buildSubwindow("70%", "90%");
			lotSubwindow.setCaption("Lotes del producto " + product.getName());

			VerticalLayout subContent = ViewHelper.buildVerticalLayout(true, true);

			// Panel de botones
			Button backBtn = new Button("Cancelar", FontAwesome.BACKWARD);
			backBtn.addStyleName("mystyle-btn");
			backBtn.addClickListener(e -> selectLot(detail, null));

			Button selectBtn = new Button("Seleccionar", FontAwesome.CHECK);
			selectBtn.addStyleName("mystyle-btn");
			selectBtn.addClickListener(e -> selectLot(detail, null));

			HorizontalLayout buttonLayout = ViewHelper.buildHorizontalLayout(true, true);
			buttonLayout.addComponents(backBtn, selectBtn);
			Panel buttonPanel = ViewHelper.buildPanel(null, buttonLayout);

			lotLayout = new LotLayout(selectedProduct, productLayout, transactionType);
			lotLayout.setCaption("Lotes");
			lotLayout.setMargin(false);
			lotLayout.setSpacing(false);
			lotLayout.lotGrid.addItemClickListener(listener -> {
				if (listener.getMouseEventDetails().isDoubleClick())
					// pass the row/item that the user double clicked
					// to method doStuff.
					// doStuff(l.getItem());
					selectLot(detail, listener.getItem());
			});
			subContent.addComponents(buttonPanel, lotLayout);

			lotSubwindow.setContent(subContent);
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

			log.info(strLog + "[parameters]" + detail);

			// Lote seleccionado en la grid
			selectedLot = lot != null ? lot : lotLayout.getSelected();

			log.info(strLog + "selectedLot:" + selectedLot);

			if (selectedLot != null) {

				// Se agrega el producto al detail de la factura
				addItemToDetail(detail);
				if (selectedLot.getQuantity() <= 0) {
					ViewHelper.showNotification("El lote no tiene un stock productos: " + selectedLot.getQuantity(),
							Notification.Type.ERROR_MESSAGE);
				} else {
					// Se asocia el lote al registro del detail
					addLotToDetail(detail);
					closeWindow(lotSubwindow);
				}
			} else {
				ViewHelper.showNotification("No ha seleccionado lote", Notification.Type.ERROR_MESSAGE);
				/*
				 * ConfirmDialog.show(Page.getCurrent().getUI(), "Confirmar",
				 * "No ha seleccionado un lote. Desea continuar", "Si", "No", e -> { if
				 * (e.isConfirmed()) { selectedLot = null; withoutLot = true;
				 * closeWindow(lotSubwindow);
				 * 
				 * } });
				 */
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
			dataProvider = new ListDataProvider<>(detailList);
			filterDataProv = dataProvider.withConfigurableFilter();
			detailGrid.setDataProvider(filterDataProv);

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
								buildCashChangeWindow();
							} else {
								saveInvoiceDetail(documentEntity);
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
	public void saveInvoiceDetail(Document document) {
		String strLog = "[saveInvoiceDetail] ";
		Document documentEntity = saveInvoiceHeader(document);
		log.info(strLog + "Document saved:" + documentEntity);

		if (documentEntity != null) {
			boolean hasErrors = false;
			for (DocumentDetail detail : documentEntity.getDetails()) {
				if (detail.getQuantity() == null || detail.getQuantity().isEmpty()) {
					ViewHelper.showNotification("Cantidad no ingresada", Notification.Type.ERROR_MESSAGE);
					hasErrors = true;
					documentBll.rollback();
					break;
				} else {
					if (detail.getQuantity().equals(detail.getOldQuantity())) {
						log.info(strLog + "Item no tiene cambios: " + detail.getProduct().getName());

					} else {
						// Guardar Detail
						DocumentDetail.Builder detailBuilder = DocumentDetail.builder();

						// Detail sin relacion al documento
						DocumentDetail detailTmp = detailBuilder.product(detail.getProduct())
								.quantity(detail.getDiffQuantity()).description(detail.getDescription())
								.subtotal(detail.getSubtotal()).measurementUnit(detail.getMeasurementUnit())
								.price(detail.getPrice()).tax(detail.getTax()).discount(detail.getDiscount()).build();

						// Relacion detail con lote. Se busca DetailLot a partir del Detail
						DocumentDetailLot detailLot = detailLotMap.get(detailTmp);

						// Detail con relacion al documento
						detailTmp = detailBuilder.document(documentEntity).build();

						// Se actualiza el detail del objeto DetailLot
						if (detailLot != null) {
							detailLot = DocumentDetailLot.builder(detailLot).documentDetail(detail).build();
						}

						// Guardar inventario
						InventoryTransaction.Builder invBuilder = InventoryTransaction.builder();

						// Se actualiza stock en el movimiento de inventario
						Double stock = lotLayout.getTotalStock();
						Double initialStock = stock != null ? stock : 0;
						log.info(strLog + "initialStock:" + initialStock);
						Double finalStock = 0.0;
						Double quantity = Double.parseDouble(detail.getDiffQuantity());
						log.info(strLog + "quantity:" + quantity);

						if (transactionType.equals(ETransactionType.ENTRADA)) {
							finalStock = initialStock != 0 ? initialStock + quantity : quantity;
						} else if (transactionType.equals(ETransactionType.SALIDA)) {
							finalStock = initialStock != 0 ? initialStock - quantity : quantity;
						}
						log.info(strLog + "finalStock:" + finalStock);
						InventoryTransaction inventoryTransaction = invBuilder.product(detail.getProduct())
								.transactionType(detail.getTransactionType()).initialStock(initialStock)
								.quantity(quantity).finalStock(finalStock).document(documentEntity).build();

						// Actualizar stock total del producto
						Product product = productBll.select(detail.getProduct().getCode());
						product.setStock(finalStock);
						product.setStockDate(new Date());

						// Actualizar lotes del producto
						/*
						 * List<Lot> lotList = lotBll.select(detail.getProduct()); Lot lot = null; if
						 * (lotList.size() > 0) { lot = lotList.get(0); log.info(strLog +
						 * "Lote más pronto a vencer:" + lot.getExpirationDate());
						 * 
						 * Double newLotStock = 0.0; if
						 * (transactionType.equals(ETransactionType.ENTRADA)) { newLotStock =
						 * lot.getQuantity() + quantity; } else if
						 * (transactionType.equals(ETransactionType.SALIDA)) { newLotStock =
						 * lot.getQuantity() - quantity; } lot.setQuantity(newLotStock); }
						 */

						log.info(strLog + "Lote a actualizar stock:" + selectedLot);

						Double newLotStock = 0.0;
						Lot lotTmp = selectedLot;
						if (detail.getTransactionType().equals(ETransactionType.ENTRADA)) {
							newLotStock = lotTmp.getQuantity() + quantity;
						} else if (detail.getTransactionType().equals(ETransactionType.SALIDA)) {
							newLotStock = lotTmp.getQuantity() - quantity;
						}
						log.info(strLog + "newLotStock: " + newLotStock);
						lotTmp.setQuantity(newLotStock);

						try {
							// Guardar relación item factura con lote
							if (detailLot != null) {
								detailLotBll.save(detailLot, false);
							}
							log.info(strLog + "detailLot saved:" + detailLot);

							// Guardar movimiento de inventario
							inventoryBll.save(inventoryTransaction, false);
							log.info(strLog + "inventoryTransaction saved:" + inventoryTransaction);

							// Actualizar stock producto
							productBll.save(product, false);
							log.info("product saved:" + product);

							// Actualizar stock de lote
							if (lotTmp != null) {
								lotBll.save(lotTmp, false);
							}
							log.info(strLog + "lot saved:" + product);

							// Actualizar precio del producto
							// updatePrice(detailTmp.getMeasurementUnitProduct(), detailTmp.getPrice());

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

		try {
			log.info(strLog + "[parameters] documentEntity: " + documentEntity);
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

			// Setear el detalle de items a la factura
			Set<DocumentDetail> details = detailGrid.getDataProvider().fetch(new Query<>()).collect(Collectors.toSet());

			documentEntity = docBuilder.code(txtDocNumber.getValue())
					.reference(txtReference.getValue() != null ? txtReference.getValue() : "")
					.documentType(cbDocumentType.getValue()).resolution(txtResolution.getValue()).person(selectedPerson)
					.documentDate(docDate).paymentMethod(cbPaymentMethod.getValue()).paymentType(paymentType)
					.paymentTerm(txtPaymentTerm.getValue()).expirationDate(expirationDate).totalValue(total)
					.totalValueNoTax(totalIVA).status(documentStatus).salesman(user.getPerson()).payValue(payValue)
					.details(details).build();

			// persistir documento
			documentBll.save(documentEntity, false);
			documentSaved = documentBll.select(documentEntity.getCode(), documentEntity.getDocumentType());

			// afterSave("");
		} catch (ModelValidationException ex) {
			log.error(strLog + "[ModelValidationException]" + ex);
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
			// cbDocumentType.clear();
			cbPaymentMethod.clear();
			// cbPaymentType.clear();
			cbDocumentStatus.setSelectedItem(docStatusBll.select("Nueva").get(0));
			itemsList.clear();
			detailGrid.getDataProvider().refreshAll();
			txtDocNumFilter.clear();
			detailLotMap.clear();
			getNextDocumentNumber(cbDocumentType.getSelectedItem().get());

			disableComponents(false);
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
					cbPaymentType
							.setValue(PaymentDocumentType.builder().paymentType(document.getPaymentType()).build());
					cbPaymentMethod.setValue(document.getPaymentMethod());
					txtPaymentTerm.setValue(document.getPaymentTerm() != null ? document.getPaymentTerm() : "");
					selectedPerson = document.getPerson();
					txtPerson.setValue(document.getPerson().getName() + " " + document.getPerson().getLastName());
					dtfExpirationDate.setValue(DateUtil.dateToLocalDateTime(document.getExpirationDate()));
					cbDocumentStatus.setValue(document.getStatus());
					Set<DocumentDetail> detailSet = document.getDetails();

					itemsList = new ArrayList<>();
					for (Iterator<DocumentDetail> iterator = detailSet.iterator(); iterator.hasNext();) {
						DocumentDetail docDetail = iterator.next();
						docDetail.setOldQuantity(docDetail.getQuantity());
						itemsList.add(docDetail);
						DocumentDetailLot detailLot = docDetailLotBll.select(docDetail);
						detailLot.setInitialStockLot(detailLot.getLot().getQuantity());
						detailLotMap.put(docDetail, detailLot);
					}
					fillDetailGridData(itemsList);
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
			cashChangeLayout = null;// new CashChangeLayout(this, totalValue);
			cashChangeLayout.setMargin(false);
			cashChangeLayout.setSpacing(false);

			VerticalLayout subContent = ViewHelper.buildVerticalLayout(true, true);
			subContent.addComponents(cashChangeLayout);
			cashChangeWindow.setContent(subContent);

			getUI().addWindow(cashChangeWindow);
		} catch (Exception e) {
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

	private void setMeasurementUnitComponent() {
		Product product = selectedProduct != null ? selectedProduct : CommonsUtil.CURRENT_DOCUMENT_DETAIL.getProduct();
		ComboBox<MeasurementUnit> cbMeasurementUnit = new ComboBox<>();

		cbMeasurementUnit.setEmptySelectionAllowed(false);
		List<MeasurementUnit> muList = measurementUnitProductBll.selectMuByProduct(product);
		ListDataProvider<MeasurementUnit> measurementDataProv = new ListDataProvider<>(muList);
		cbMeasurementUnit.setDataProvider(measurementDataProv);
		cbMeasurementUnit.setItemCaptionGenerator(MeasurementUnit::getName);
		cbMeasurementUnit
				.addValueChangeListener(e -> selectPriceXMU(cbMeasurementUnit.getSelectedItem().get(), product));

		Binder<DocumentDetail> binder = detailGrid.getEditor().getBinder();
		Binding<DocumentDetail, MeasurementUnit> binding = binder.bind(cbMeasurementUnit,
				DocumentDetail::getMeasurementUnit, DocumentDetail::setMeasurementUnit);
		columnUM.setEditorBinding(binding);

	}

	/**
	 * Metodo para seleccionar el precio para una UM
	 * 
	 * @param mus
	 * @param product
	 * @return
	 */
	private MeasurementUnitProduct selectPriceXMU(MeasurementUnit mu, Product product) {
		MeasurementUnitProduct pricexMU = null;
		String strLog = "";
		try {
			Binder<DocumentDetail> binder = detailGrid.getEditor().getBinder();
			NumberField txtPrice = new NumberField();
			NumberField txtTax = new NumberField();
			txtTax.setReadOnly(true);
			List<MeasurementUnitProduct> priceList = measurementUnitProductBll.select(mu, product);
			if (priceList.size() > 0) {
				pricexMU = priceList.get(0);
				CommonsUtil.MEASUREMENT_UNIT_PRODUCT = pricexMU;
				if (transactionType.equals(ETransactionType.ENTRADA)) {
					txtPrice.setValue(pricexMU.getPurchasePrice());
					txtTax.setValue(pricexMU.getPurchaseTax());
					txtPrice.setReadOnly(false);
				} else if (transactionType.equals(ETransactionType.SALIDA)) {
					txtPrice.setValue(pricexMU.getSalePrice());
					txtTax.setValue(pricexMU.getSaleTax());
					txtPrice.setReadOnly(true);
				}
			}

			// Binding de precio
			Binding<DocumentDetail, String> priceBinding = binder.bind(txtPrice, DocumentDetail::getPriceStr,
					DocumentDetail::setPriceStr);
			columnPrice.setEditorBinding(priceBinding);

			// Binding de impuesto
			Binding<DocumentDetail, String> taxBinding = binder.bind(txtTax, DocumentDetail::getTaxStr,
					DocumentDetail::setTaxStr);
			columnTax.setEditorBinding(taxBinding);

		} catch (Exception e) {
			log.error(strLog + "[Exception]" + e.getMessage());

		}
		return pricexMU;
	}

	/**
	 * Actualizar el precio del producto x UM ingresado en la grid
	 * 
	 * @param muProduct
	 * @param price
	 */
	private void updatePrice(MeasurementUnitProduct muProduct, Double price) {
		String strLog = "[setPrice] ";
		try {
			if (price != null && !price.equals(0.0) && muProduct != null) {
				MeasurementUnitProduct entity = null;
				MeasurementUnitProduct.Builder muProductBuilder = MeasurementUnitProduct.builder(muProduct);
				if (transactionType.equals(ETransactionType.ENTRADA)) {
					entity = muProductBuilder.purchasePrice(price).build();
				} else if (transactionType.equals(ETransactionType.SALIDA)) {
					entity = muProductBuilder.salePrice(price).build();
				}
				measurementUnitProductBll.save(entity, false);
				log.info(strLog + "Precio actualizado");
			}
		} catch (Exception e) {
			log.error(strLog + "[Exception]" + e.getMessage());
		}
	}

	public Double getPayValue() {
		return payValue;
	}

	public void setPayValue(Double payValue) {
		this.payValue = payValue;
	}

}
