package com.soinsoftware.vissa.web;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.persistence.PersistenceException;

import org.apache.log4j.Logger;
import org.hibernate.HibernateException;
import org.springframework.transaction.annotation.Transactional;
import org.vaadin.dialogs.ConfirmDialog;

import com.soinsoftware.vissa.bll.DocumentBll;
import com.soinsoftware.vissa.bll.DocumentDetailBll;
import com.soinsoftware.vissa.bll.DocumentDetailLotBll;
import com.soinsoftware.vissa.bll.DocumentStatusBll;
import com.soinsoftware.vissa.bll.DocumentTypeBll;
import com.soinsoftware.vissa.bll.InventoryTransactionBll;
import com.soinsoftware.vissa.bll.LotBll;
import com.soinsoftware.vissa.bll.PaymentMethodBll;
import com.soinsoftware.vissa.bll.PaymentTypeBll;
import com.soinsoftware.vissa.bll.ProductBll;
import com.soinsoftware.vissa.common.CommonsUtil;
import com.soinsoftware.vissa.exception.ModelValidationException;
import com.soinsoftware.vissa.model.Document;
import com.soinsoftware.vissa.model.DocumentDetail;
import com.soinsoftware.vissa.model.DocumentDetailLot;
import com.soinsoftware.vissa.model.DocumentStatus;
import com.soinsoftware.vissa.model.DocumentType;
import com.soinsoftware.vissa.model.InventoryTransaction;
import com.soinsoftware.vissa.model.Lot;
import com.soinsoftware.vissa.model.PaymentMethod;
import com.soinsoftware.vissa.model.PaymentType;
import com.soinsoftware.vissa.model.Person;
import com.soinsoftware.vissa.model.PersonType;
import com.soinsoftware.vissa.model.Product;
import com.soinsoftware.vissa.model.TransactionType;
import com.soinsoftware.vissa.model.User;
import com.soinsoftware.vissa.util.Commons;
import com.soinsoftware.vissa.util.DateUtil;
import com.soinsoftware.vissa.util.PermissionUtil;
import com.soinsoftware.vissa.util.ViewHelper;
import com.vaadin.data.provider.ConfigurableFilterDataProvider;
import com.vaadin.data.provider.ListDataProvider;
import com.vaadin.data.provider.Query;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.server.FontAwesome;
import com.vaadin.server.Page;
import com.vaadin.server.SerializablePredicate;
import com.vaadin.shared.ui.datefield.DateTimeResolution;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.DateTimeField;
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
public class InvoiceLayout extends VerticalLayout implements View {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5187486966314714738L;
	protected static final Logger log = Logger.getLogger(InvoiceLayout.class);

	// Bll

	private final ProductBll productBll;
	private final PaymentMethodBll payMethodBll;
	private final PaymentTypeBll payTypeBll;
	private final DocumentBll documentBll;
	private final DocumentDetailBll docDetailBll;
	private final InventoryTransactionBll inventoryBll;
	private final DocumentTypeBll documentTypeBll;
	private final DocumentStatusBll docStatusBll;
	private final LotBll lotBll;
	private final DocumentDetailLotBll detailLotBll;

	// Components
	private TextField txtDocNumFilter;
	private TextField txtDocNumber;
	private TextField txtResolution;
	private TextField txtReference;
	private TextField txtPerson;
	private DateTimeField dtfDocumentDate;
	private ComboBox<PaymentType> cbPaymentType;
	private ComboBox<DocumentType> cbDocumentType;
	private TextField txtPaymentTerm;
	private DateTimeField dtfExpirationDate;
	private TextField txtTotal;
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

	private HashMap<DocumentDetail, DocumentDetailLot> detailLotMap = new HashMap<DocumentDetail, DocumentDetailLot>();

	private TransactionType transactionType;

	private ListDataProvider<DocumentDetail> dataProvider;
	private ConfigurableFilterDataProvider<DocumentDetail, Void, SerializablePredicate<DocumentDetail>> filterDataProv;
	private Column<?, ?> columnQuantity;
	private Column<?, ?> columnTotal;
	private FooterRow footer;

	private boolean withoutLot;

	private PermissionUtil permissionUtil;

	private User user;

	public InvoiceLayout() throws IOException {
		super();

		productBll = ProductBll.getInstance();
		payMethodBll = PaymentMethodBll.getInstance();
		payTypeBll = PaymentTypeBll.getInstance();
		documentBll = DocumentBll.getInstance();
		docDetailBll = DocumentDetailBll.getInstance();
		inventoryBll = InventoryTransactionBll.getInstance();
		documentTypeBll = DocumentTypeBll.getInstance();
		docStatusBll = DocumentStatusBll.getInstance();
		detailLotBll = DocumentDetailLotBll.getInstance();
		lotBll = LotBll.getInstance();
		document = new Document();
		itemsList = new ArrayList<>();
		transactionType = TransactionType.valueOf(Commons.TRANSACTION_TYPE);

	}

	@Override
	public void enter(ViewChangeEvent event) {

		View.super.enter(event);

		this.user = getSession().getAttribute(User.class);
		this.permissionUtil = new PermissionUtil(user.getRole().getPermissions());
		// setMargin(true);
		String title = "";
		if (transactionType.equals(TransactionType.ENTRADA)) {
			title = "Pedido";
		} else {
			title = "Venta";
		}
		Label tittle = new Label(title);
		tittle.addStyleName(ValoTheme.LABEL_H2);
		addComponent(tittle);

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
			saveBtn.addClickListener(e -> saveButtonAction(null));
			layout.addComponents(saveBtn);

			Button editBtn = new Button("Edit", FontAwesome.EDIT);
			editBtn.addStyleName("mystyle-btn");
			// editBtn.addClickListener(e -> saveButtonAction(null));
			// layout.addComponents(editBtn);
		}

		if (permissionUtil.canDelete(Commons.MENU_NAME)) {
			Button deleteBtn = new Button("Cancelar", FontAwesome.ERASER);
			deleteBtn.addStyleName("mystyle-btn");
			deleteBtn.addClickListener(e -> deleteButtonAction());
			layout.addComponents(deleteBtn);
		}

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

		cbDocumentType = new ComboBox<DocumentType>("Tipo de pedido");
		cbDocumentType.focus();
		// cbDocumentType.setWidth("40%");

		ListDataProvider<DocumentType> docTypeDataProv = new ListDataProvider<>(
				documentTypeBll.select(transactionType));

		cbDocumentType.setDataProvider(docTypeDataProv);
		cbDocumentType.setItemCaptionGenerator(DocumentType::getName);
		cbDocumentType.addValueChangeListener(e -> {
			getNextDocumentNumber(cbDocumentType.getValue());
		});
		cbDocumentType.setEmptySelectionAllowed(false);
		cbDocumentType.setEmptySelectionCaption("Seleccione");
		cbDocumentType.setStyleName(ValoTheme.COMBOBOX_TINY);

		txtDocNumber = new TextField("Número de factura");
		txtDocNumber.setEnabled(false);
		// txtDocNumber.setWidth("40%");
		txtDocNumber.setStyleName(ValoTheme.TEXTFIELD_TINY);

		txtResolution = new TextField("Resolución de factura");
		txtResolution.setStyleName(ValoTheme.TEXTFIELD_TINY);

		txtReference = new TextField("Referencia");
		txtReference.setStyleName(ValoTheme.TEXTFIELD_TINY);
		// txtReference.setWidth("40%");

		String title = "";
		if (transactionType.equals(TransactionType.ENTRADA)) {
			title = "Proveedor";
		} else {
			title = "Cliente";
		}
		txtPerson = new TextField(title);
		// txtPerson.setWidth("28%");
		txtPerson.setReadOnly(true);
		txtPerson.setRequiredIndicatorVisible(true);
		txtPerson.setStyleName(ValoTheme.TEXTFIELD_TINY);
		Button searchSupplierButton = new Button("Buscar proveedor", FontAwesome.SEARCH);
		searchSupplierButton.addClickListener(e -> buildPersonWindow(txtPerson.getValue()));
		searchSupplierButton.setStyleName("icon-only");

		dtfDocumentDate = new DateTimeField("Fecha");
		dtfDocumentDate.setResolution(DateTimeResolution.SECOND);
		dtfDocumentDate.setValue(LocalDateTime.now());
		dtfDocumentDate.setDateFormat(Commons.FORMAT_DATE);
		dtfDocumentDate.setStyleName(ValoTheme.DATEFIELD_TINY);
		dtfDocumentDate.setWidth("184px");

		cbPaymentType = new ComboBox<PaymentType>("Forma de pago");

		cbPaymentType.setEmptySelectionAllowed(false);
		cbPaymentType.setEmptySelectionCaption("Seleccione");
		ListDataProvider<PaymentType> payTypeDataProv = new ListDataProvider<>(payTypeBll.selectAll());
		cbPaymentType.setDataProvider(payTypeDataProv);
		cbPaymentType.setItemCaptionGenerator(PaymentType::getName);
		cbPaymentType.setStyleName(ValoTheme.COMBOBOX_TINY);

		txtPaymentTerm = new TextField("Plazo");
		txtPaymentTerm.setStyleName(ValoTheme.TEXTFIELD_TINY);

		dtfExpirationDate = new DateTimeField("Fecha de Vencimiento");
		dtfExpirationDate.setEnabled(false);
		dtfExpirationDate.setDateFormat(Commons.FORMAT_DATE);
		dtfExpirationDate.setStyleName(ValoTheme.DATEFIELD_TINY);
		dtfExpirationDate.setWidth("184px");

		txtPaymentTerm.addValueChangeListener(e -> {
			setExpirationDate(txtPaymentTerm.getValue());
		});

		cbPaymentMethod = new ComboBox<PaymentMethod>("Método de pago");
		// cbPaymentMethod.setWidth("20%");
		cbPaymentMethod.setEmptySelectionAllowed(false);
		cbPaymentMethod.setEmptySelectionCaption("Seleccione");
		ListDataProvider<PaymentMethod> payMetDataProv = new ListDataProvider<>(payMethodBll.selectAll());
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

		HorizontalLayout headerLayout1 = ViewHelper.buildHorizontalLayout(false, false);

		headerLayout1.addComponents(cbDocumentType, txtDocNumber, txtResolution, txtReference, txtPerson,
				searchSupplierButton);

		HorizontalLayout headerLayout2 = ViewHelper.buildHorizontalLayout(false, false);
		headerLayout2.addComponents(dtfDocumentDate, cbPaymentType, txtPaymentTerm, dtfExpirationDate, cbPaymentMethod);

		headerLayout1.setComponentAlignment(searchSupplierButton, Alignment.BOTTOM_CENTER);

		HorizontalLayout headerLayout3 = ViewHelper.buildHorizontalLayout(false, false);
		headerLayout3.addComponents(cbDocumentStatus, txtTotal);

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
		Button addProductBt = new Button("Agregar producto", FontAwesome.PLUS);
		addProductBt.addStyleName(ValoTheme.BUTTON_TINY);
		addProductBt.addClickListener(e -> buildProductWindow());

		Button deleteProductBt = new Button("Eliminar producto", FontAwesome.ERASER);
		deleteProductBt.addStyleName(ValoTheme.BUTTON_TINY);
		deleteProductBt.addClickListener(e -> deleteProductGrid());

		buttonlayout.addComponents(addProductBt, deleteProductBt);

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
		detailGrid.addColumn(documentDetail -> {
			if (documentDetail.getProduct() != null && documentDetail.getProduct().getMeasurementUnit() != null) {
				return documentDetail.getProduct().getMeasurementUnit().getName();
			} else {
				return null;
			}
		}).setCaption("Unidad de medida");
		detailGrid.addColumn(documentDetail -> {
			if (documentDetail.getProduct() != null && documentDetail.getProduct().getSalePrice() != null) {
				return documentDetail.getProduct().getSalePrice();
			} else {
				return null;
			}
		}).setCaption("Precio");

		// Columna cantidad editable
		TextField txtQuantity = new TextField();
		columnQuantity = detailGrid.addColumn(DocumentDetail::getQuantity).setCaption("Cantidad")
				.setEditorComponent(txtQuantity, DocumentDetail::setQuantity);

		columnTotal = detailGrid.addColumn(documentDetail -> {
			if (documentDetail.getSubtotal() != 0) {
				return documentDetail.getSubtotal();
			} else {
				return "";
			}
		}).setCaption("Subtotal");

		txtQuantity.setMaxLength(100);
		txtQuantity.setRequiredIndicatorVisible(true);

		txtQuantity.addBlurListener(e -> changeQuantity(txtQuantity.getValue()));
		footer = detailGrid.prependFooterRow();
		footer.getCell(columnQuantity).setHtml("<b>Total:</b>");

		detailGrid.getEditor().setEnabled(true);

		HorizontalLayout itemsLayout = ViewHelper.buildHorizontalLayout(false, false);
		itemsLayout.setSizeFull();
		itemsLayout.addComponents(ViewHelper.buildPanel(null, detailGrid));

		layout.addComponents(buttonlayout, itemsLayout);

		return ViewHelper.buildPanel("Productos", layout);
	}

	private void changeQuantity(String quantity) {
		dataProvider.refreshAll();
		String message = "";
		boolean correct = false;
		Integer qty;
		try {
			qty = Integer.parseInt(quantity);

			if (qty > 0) {
				if (!withoutLot) {
					DocumentDetail currentDetail = CommonsUtil.currentDocumentDetail;
					log.info("currentDetail:" + currentDetail);
					DocumentDetailLot detailLot = detailLotMap.get(currentDetail);
					log.info("detailLot:" + detailLot);
					if (detailLot != null) {
						if (qty > detailLot.getInitialStockLot()) {
							message = "Cantidad del lote menor a la cantidad solicitada";
							throw new Exception(message);
						} else {
							Integer finalStock = detailLot.getInitialStockLot() - qty;
							DocumentDetailLot detailLotTmp = DocumentDetailLot.builder(detailLot).quantity(qty)
									.finalStockLot(finalStock).build();
							log.info("currentDetail again:" + currentDetail);
							log.info("detailLotTmp:" + detailLotTmp);
							detailLotMap.put(currentDetail, detailLotTmp);
						}
					}
				}
				correct = true;

			} else {
				message = "La cantidad debe ser mayor a 0";
				throw new Exception(message);
			}

		} catch (NumberFormatException nfe) {
			log.error(nfe.getMessage());
			message = "Formato de cantidad no valido";
		} catch (Exception e) {
			log.error(e.getMessage());
			message = e.getMessage();
		} finally {
			log.info("Correct: " + correct);
			if (!correct && message != null) {
				ViewHelper.showNotification(message, Notification.Type.ERROR_MESSAGE);
			}

		}

	}

	private String calculateTotal(ListDataProvider<DocumentDetail> detailDataProv) {
		log.info("Calculando total");
		String total = String
				.valueOf(detailDataProv.fetch(new Query<>()).mapToDouble(DocumentDetail::getSubtotal).sum());
		txtTotal.setValue(total);
		return "<b>" + total + "</b>";

	}

	/**
	 * Metodo para construir la venta modal de personas
	 * 
	 * @param personFiltter
	 */
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

			if (transactionType.equals(TransactionType.ENTRADA)) {
				Commons.PERSON_TYPE = PersonType.SUPPLIER.getName();
			}
			if (transactionType.equals(TransactionType.SALIDA)) {
				Commons.PERSON_TYPE = PersonType.CUSTOMER.getName();
			}
			personLayout = new PersonLayout(true);

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
	private void selectPerson() {
		selectedPerson = personLayout.getSelected();

		if (selectedPerson != null) {
			txtPerson.setValue(selectedPerson.getName() + " " + selectedPerson.getLastName());
			personSubwindow.close();
		} else {
			ViewHelper.showNotification("Seleccione un proveedor", Notification.TYPE_WARNING_MESSAGE);
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

		productSubwindow = ViewHelper.buildSubwindow("75%");
		productSubwindow.setCaption("Productos");

		VerticalLayout subContent = ViewHelper.buildVerticalLayout(true, true);

		// Panel de botones
		Button backBtn = new Button("Cancelar", FontAwesome.BACKWARD);
		backBtn.addStyleName("mystyle-btn");
		backBtn.addClickListener(e -> closeWindow(productSubwindow));

		Button selectBtn = new Button("Seleccionar", FontAwesome.CHECK);
		selectBtn.addStyleName("mystyle-btn");
		selectBtn.addClickListener(e -> selectProduct());

		HorizontalLayout buttonLayout = ViewHelper.buildHorizontalLayout(true, true);
		buttonLayout.addComponents(backBtn, selectBtn);
		Panel buttonPanel = ViewHelper.buildPanel(null, buttonLayout);

		try {
			productLayout = new ProductLayout(true);

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
	private void selectProduct() {

		selectedProduct = productLayout.getSelected();

		if (selectedProduct != null) {
			DocumentDetail docDetail = DocumentDetail.builder().product(selectedProduct).build();
			if (itemsList.contains(docDetail)) {
				ViewHelper.showNotification("Este producto ya está agregado a la factura",
						Notification.Type.WARNING_MESSAGE);
			} else {
				if (selectedProduct.getSalePrice() == null) {
					ViewHelper.showNotification("El producto no tiene precio configurado",
							Notification.Type.WARNING_MESSAGE);
				} else if (selectedProduct.getStock() == null || selectedProduct.getStock() == 0) {
					ViewHelper.showNotification("El producto no tiene stock disponible",
							Notification.Type.WARNING_MESSAGE);
				} else {

					// ---Panel de lotes
					try {
						log.info("withoutLot:" + withoutLot);
						if (selectedLot == null && !withoutLot) {
							buildLotWindow(selectedProduct);
						} else if (selectedLot != null || withoutLot) {
							itemsList.add(docDetail);
							fillDetailGridData(itemsList);
							// Si hay lote, se asocia al registro de detail
							if (selectedLot != null) {
								DocumentDetailLot detailLot = DocumentDetailLot.builder().documentDetail(docDetail)
										.lot(selectedLot).initialStockLot(selectedLot.getQuantity()).build();
								detailLotMap.put(docDetail, detailLot);
							}
							productSubwindow.close();
						}
					} catch (Exception e) {
						log.error("Error al cargar lotes del producto. Exception: " + e);
					}
				}
			}

		} else

		{
			ViewHelper.showNotification("No ha seleccionado un producto", Notification.Type.WARNING_MESSAGE);
		}

	}

	/**
	 * Metodo que construye la venta para buscar productos
	 */
	private void buildLotWindow(Product product) {
		withoutLot = false;

		lotSubwindow = ViewHelper.buildSubwindow("70%");
		lotSubwindow.setCaption("Lotes del producto " + product.getName());

		VerticalLayout subContent = ViewHelper.buildVerticalLayout(true, true);

		// Panel de botones
		Button backBtn = new Button("Cancelar", FontAwesome.BACKWARD);
		backBtn.addStyleName("mystyle-btn");
		backBtn.addClickListener(e -> selectLot());

		Button selectBtn = new Button("Seleccionar", FontAwesome.CHECK);
		selectBtn.addStyleName("mystyle-btn");
		selectBtn.addClickListener(e -> selectLot());

		HorizontalLayout buttonLayout = ViewHelper.buildHorizontalLayout(true, true);
		buttonLayout.addComponents(backBtn, selectBtn);
		Panel buttonPanel = ViewHelper.buildPanel(null, buttonLayout);

		try {
			lotLayout = new LotLayout(selectedProduct);
			lotLayout.setCaption("Lotes");
			lotLayout.setMargin(false);
			lotLayout.setSpacing(false);
		} catch (IOException e) {
			log.error("Error al cargar lista de lotes. Exception:" + e);
		}
		subContent.addComponents(buttonPanel, lotLayout);

		lotSubwindow.setContent(subContent);
		getUI().addWindow(lotSubwindow);

	}

	/**
	 * Metodo para escoger lotes para tomar los productos
	 */
	private void selectLot() {

		selectedLot = lotLayout.getSelected();

		log.info("selectedLot:" + selectedLot);

		if (selectedLot != null) {

			if (selectedLot.getQuantity() <= 0) {
				ViewHelper.showNotification("El lote no tiene un stock productos: " + selectedLot.getQuantity(),
						Notification.Type.ERROR_MESSAGE);
			} else {
				lotSubwindow.close();
			}
		} else {
			ConfirmDialog.show(Page.getCurrent().getUI(), "Confirmar", "No ha seleccionado un lote. Desea continuar",
					"Si", "No", e -> {
						if (e.isConfirmed()) {
							withoutLot = true;
							closeWindow(lotSubwindow);

						}
					});
		}

	}

	/**
	 * Metodo para llenar la data de la grid de detalle de la factura
	 * 
	 * @param detailList
	 */
	private void fillDetailGridData(List<DocumentDetail> detailList) {

		dataProvider = new ListDataProvider<>(detailList);
		filterDataProv = dataProvider.withConfigurableFilter();
		detailGrid.setDataProvider(filterDataProv);

		dataProvider
				.addDataProviderListener(event -> footer.getCell(columnTotal).setHtml(calculateTotal(dataProvider)));
		dataProvider.refreshAll();
	}

	@Transactional(rollbackFor = Exception.class)
	private void saveButtonAction(Document documentEntity) {
		ConfirmDialog.show(Page.getCurrent().getUI(), "Confirmar", "Está seguro de guardar la factura", "Si", "No",
				e -> {
					if (e.isConfirmed()) {
						saveDocument(documentEntity);
					}
				});
	}

	@Transactional(rollbackFor = Exception.class)
	private void saveDocument(Document document) {
		Document documentEntity = saveDocumentEntity(document);
		log.info("Document saved:" + documentEntity);

		if (documentEntity != null) {

			// Guardar el detalle de la factura
			List<DocumentDetail> detailList = detailGrid.getDataProvider().fetch(new Query<>())
					.collect(Collectors.toList());

			for (DocumentDetail detObj : detailList) {
				if (detObj.getQuantity() == null) {
					ViewHelper.showNotification("Cantidad no ingresada", Notification.Type.ERROR_MESSAGE);
					documentBll.rollback();
					break;
				} else {
					// Guardar Detail
					DocumentDetail.Builder detailBuilder = DocumentDetail.builder();

					Integer cant = Integer.parseInt(detObj.getQuantity());
					Product prod = detObj.getProduct();
					Double subtotal = cant * prod.getSalePrice();

					// Detail sin relacion al documento
					DocumentDetail detail = detailBuilder.product(prod).quantity(cant + "")
							.description(detObj.getDescription()).subtotal(subtotal).build();

					// Relacion detail con lote
					DocumentDetailLot detailLot = detailLotMap.get(detail);
					log.info("DetailLot a guardar:" + detailLot);

					// Detail con relacion al documento
					detail = detailBuilder.document(documentEntity).build();
					log.info("Detail a guardar:" + detail);

					detailLot = DocumentDetailLot.builder(detailLot).documentDetail(detail).build();

					// Guardar inventario
					InventoryTransaction.Builder invBuilder = InventoryTransaction.builder();

					Integer stock = detObj.getProduct().getStock();
					int initialStock = stock != null ? stock : 0;
					log.info("initialStock:" + initialStock);
					log.info("quantity:" + cant);
					int finalStock = 0;

					if (transactionType.equals(TransactionType.ENTRADA)) {
						finalStock = initialStock != 0 ? initialStock + cant : cant;
					} else if (transactionType.equals(TransactionType.SALIDA)) {
						finalStock = initialStock != 0 ? initialStock - cant : cant;
					}
					log.info("finalStock:" + finalStock);
					InventoryTransaction inventoryObj = invBuilder.product(detObj.getProduct())
							.transactionType(transactionType).initialStock(initialStock).quantity(cant)
							.finalStock(finalStock).document(documentEntity).build();

					// Actualizar stock total del producto
					Product productObj = productBll.select(detObj.getProduct().getCode());
					productObj.setStock(finalStock);
					productObj.setStockDate(new Date());

					// Actualizar lotes del producto
					List<Lot> lotList = lotBll.select(detObj.getProduct());
					Lot lotObj = null;
					if (lotList.size() > 0) {
						Lot lot = lotList.get(0);
						log.info("lote más pronto a vencer:" + lot.getExpirationDate());

						lotObj = lotBll.select(lot.getCode());
						int newLotStock = lotObj.getQuantity() - cant;
						lotObj.setQuantity(newLotStock);
					}

					try {

						// Guardar detalle de la factura
						docDetailBll.save(detail);
						log.info("Detail lot a guardar 2:" + detailLot);
						// Guardar relación item factura con lote
						if (detailLot != null) {
							detailLotBll.save(detailLot);
						}

						// Guardar movimiento de inventario
						inventoryBll.save(inventoryObj);

						// Actualizar stock producto
						productBll.save(productObj);

						// Actualizar stock de lote
						if (lotObj != null) {
							lotBll.save(lotObj);
						}

						// afterSave(caption);
					} catch (ModelValidationException ex) {
						log.error(ex);
						ViewHelper.showNotification(ex.getMessage(), Notification.Type.ERROR_MESSAGE);
					} catch (HibernateException ex) {
						log.error(ex);
						// bll.rollback();
						ViewHelper.showNotification(
								"Los datos no pudieron ser salvados, contacte al administrador (3007200405)",
								Notification.Type.ERROR_MESSAGE);
					}

				}

			}

			// Actualizar consecutivo de tipo de documento
			documentTypeBll.save(documentType);

			ViewHelper.showNotification("Factura guardada con exito", Notification.Type.WARNING_MESSAGE);
		}
	}

	private Document saveDocumentEntity(Document documentEntity) {
		Document.Builder docBuilder = null;
		DocumentStatus documentStatus = null;
		if (documentEntity == null) {
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

		documentEntity = docBuilder.code(txtDocNumber.getValue())
				.reference(txtReference.getValue() != null ? txtReference.getValue() : "")
				.documentType(cbDocumentType.getValue()).person(selectedPerson).documentDate(docDate)
				.paymentMethod(cbPaymentMethod.getValue()).paymentType(cbPaymentType.getValue())
				.paymentTerm(txtPaymentTerm.getValue()).expirationDate(expirationDate).totalValue(total)
				.status(documentStatus).salesman(user.getPerson()).build();

		// Guardar documento
		try {

			documentBll.save(documentEntity);

			// afterSave("");
		} catch (ModelValidationException ex) {
			log.error(ex);
			ViewHelper.showNotification(ex.getMessage(), Notification.Type.ERROR_MESSAGE);
		} catch (HibernateException ex) {
			log.error(ex);
			documentBll.rollback();
			ViewHelper.showNotification("Los datos no pudieron ser salvados, contacte al administrador (3007200405)",
					Notification.Type.ERROR_MESSAGE);
		} catch (PersistenceException ex) {
			log.error(ex);
			documentBll.rollback();
			ViewHelper.showNotification("Se presentó un error, por favor contacte al adminisrador (3007200405)",
					Notification.Type.ERROR_MESSAGE);
		} catch (Exception ex) {
			log.error(ex);
			documentBll.rollback();
			ViewHelper.showNotification("Se presentó un error, por favor contacte al adminisrador (3007200405)",
					Notification.Type.ERROR_MESSAGE);
		}

		Document documentSaved = documentBll.select(documentEntity.getCode(), documentEntity.getDocumentType());

		return documentSaved;

	}

	private void cleanButtonAction() {
		log.info("Nuevo");
		txtDocNumber.clear();
		txtDocNumFilter.clear();
		txtReference.clear();
		txtPerson.clear();
		txtPaymentTerm.clear();
		// cbDocumentType.clear();
		cbPaymentMethod.clear();
		cbPaymentType.clear();
		cbDocumentStatus.clear();
		itemsList.clear();
		detailGrid.getDataProvider().refreshAll();
		txtDocNumFilter.clear();

	}

	private void searchDocument(String documentNumber) {
		log.info("searchDocument");
		if (documentNumber != null && !documentNumber.isEmpty()) {
			document = documentBll.select(documentNumber);
			if (document != null) {
				cbDocumentType.setValue(document.getDocumentType());
				txtDocNumber.setValue(document.getCode());
				txtReference.setValue(document.getReference() != null ? document.getReference() : "");
				dtfDocumentDate.setValue(DateUtil.dateToLocalDateTime(document.getDocumentDate()));
				cbPaymentType.setValue(document.getPaymentType());
				cbPaymentMethod.setValue(document.getPaymentMethod());
				txtPaymentTerm.setValue(document.getPaymentTerm() != null ? document.getPaymentTerm() : "");
				selectedPerson = document.getPerson();
				txtPerson.setValue(document.getPerson().getName() + " " + document.getPerson().getLastName());
				dtfExpirationDate.setValue(DateUtil.dateToLocalDateTime(document.getExpirationDate()));
				Set<DocumentDetail> detailSet = document.getDetails();

				itemsList = new ArrayList<>();
				for (Iterator<DocumentDetail> iterator = detailSet.iterator(); iterator.hasNext();) {
					itemsList.add(iterator.next());
				}
				fillDetailGridData(itemsList);
			}
		}

	}

	private void closeWindow(Window w) {
		w.close();
	}

	private void deleteProductGrid() {
		itemsList.remove(getSelectedDetail());
		fillDetailGridData(itemsList);
	}

	private DocumentDetail getSelectedDetail() {
		Set<DocumentDetail> detailSet = detailGrid.getSelectedItems();
		return detailSet.iterator().next();
	}

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

	private void deleteDocument() {

		DocumentStatus status = docStatusBll.select("Cancelada").get(0);
		Document documentEntity = Document.builder(document).status(status).build();
		saveButtonAction(documentEntity);
		;

	}

}
