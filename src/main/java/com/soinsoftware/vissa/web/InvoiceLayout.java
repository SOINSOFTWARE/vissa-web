package com.soinsoftware.vissa.web;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.log4j.Logger;
import org.hibernate.HibernateException;
import org.springframework.transaction.annotation.Transactional;
import org.vaadin.dialogs.ConfirmDialog;

import com.mysql.cj.xdevapi.Table;
import com.soinsoftware.vissa.bll.DocumentBll;
import com.soinsoftware.vissa.bll.DocumentDetailBll;
import com.soinsoftware.vissa.bll.DocumentStatusBll;
import com.soinsoftware.vissa.bll.DocumentTypeBll;
import com.soinsoftware.vissa.bll.InventoryTransactionBll;
import com.soinsoftware.vissa.bll.LotBll;
import com.soinsoftware.vissa.bll.PaymentMethodBll;
import com.soinsoftware.vissa.bll.PaymentTypeBll;
import com.soinsoftware.vissa.bll.ProductBll;
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
import com.soinsoftware.vissa.util.Commons;
import com.soinsoftware.vissa.util.DateUtil;
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
	private List<DocumentDetailLot> detailLotList = new ArrayList<>();

	private TransactionType transactionType;
	
	private ListDataProvider<DocumentDetail> dataProvider;
	private ConfigurableFilterDataProvider<DocumentDetail, Void, SerializablePredicate<DocumentDetail>> filterDataProv;
	private Column<?, ?> columnQuantity;
	private Column<?, ?> columnTotal;
	private FooterRow footer;

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
		lotBll = LotBll.getInstance();
		document = new Document();
		itemsList = new ArrayList<>();
		transactionType = TransactionType.valueOf(Commons.TRANSACTION_TYPE);

	}

	@Override
	public void enter(ViewChangeEvent event) {

		View.super.enter(event);

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
		Button newBtn = new Button("Nuevo", FontAwesome.SAVE);
		newBtn.addStyleName("mystyle-btn");
		newBtn.addClickListener(e -> cleanButtonAction());

		Button saveBtn = new Button("Guardar", FontAwesome.SAVE);
		saveBtn.addStyleName("mystyle-btn");
		saveBtn.addClickListener(e -> saveButtonAction(null));

		Button editBtn = new Button("Edit", FontAwesome.EDIT);
		editBtn.addStyleName("mystyle-btn");
		// editBtn.addClickListener(e -> saveButtonAction(null));

		Button deleteBtn = new Button("Cancelar", FontAwesome.ERASER);
		deleteBtn.addStyleName("mystyle-btn");
		// deleteBtn.addClickListener(e -> saveButtonAction(document));

		layout.addComponents(newBtn, saveBtn, deleteBtn);
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
		txtPerson.setEnabled(false);
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

		txtTotal = new TextField("Total Factura");
		txtTotal.setEnabled(false);
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

		Button searchProductBt = new Button("Buscar productos", FontAwesome.PLUS);
		searchProductBt.addStyleName(ValoTheme.BUTTON_SMALL);
		searchProductBt.addClickListener(e -> buildProductWindow());

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
		TextField txtCant = new TextField();
		columnQuantity = detailGrid.addColumn(DocumentDetail::getQuantity).setCaption("Cantidad")
				.setEditorComponent(txtCant, DocumentDetail::setQuantity);

		columnTotal = detailGrid.addColumn(documentDetail -> {
			if (documentDetail.getSubtotal() != 0) {
				return documentDetail.getSubtotal();
			} else {
				return "";
			}
		}).setCaption("Subtotal");
		
		txtCant.addBlurListener(e -> dataProvider.refreshAll());
		footer = detailGrid.prependFooterRow();
		footer.getCell(columnQuantity).setHtml("<b>Total:</b>");

		/*
		 * TextField lSubtotal = new TextField(); lSubtotal.setEnabled(false);
		 * detailGrid.addColumn(DocumentDetail::getSubtotalStr).setCaption("Subtotal").
		 * setEditorComponent(lSubtotal, DocumentDetail::setSubtotalStr);
		 * lSubtotal.addValueChangeListener(e -> setTotalDocument());
		 */
		detailGrid.getEditor().setEnabled(true);

		HorizontalLayout itemsLayout = ViewHelper.buildHorizontalLayout(false, false);
		itemsLayout.setSizeFull();
		itemsLayout.addComponents(ViewHelper.buildPanel(null, detailGrid));

		layout.addComponents(searchProductBt, itemsLayout);

		return ViewHelper.buildPanel("Productos", layout);
	}

	private String calculateTotal(ListDataProvider<DocumentDetail> detailDataProv) {
		log.info("Calculando total");
		return "<b>"
				+ String.valueOf(detailDataProv.fetch(new Query<>()).mapToDouble(DocumentDetail::getSubtotal).sum())
				+ "</b>";

	}

	private void changeQuantity(Object obj) {
		Product prod = (Product) obj;
		log.info("prod:" + prod);
		Set<DocumentDetail> details = detailGrid.getSelectedItems();
		log.info("Details:" + details.size());
		if (!details.isEmpty()) {
			DocumentDetail detail = details.iterator().next();
			log.info("details: " + detail.getQuantity() + detail.getProduct().getName());
		}

	}

	private void setTotalDocument() {
		List<DocumentDetail> detailList = detailGrid.getDataProvider().fetch(new Query<>())
				.collect(Collectors.toList());

		log.info("detailList:" + detailList.size());
		Double total = 0.0;

		for (Iterator<DocumentDetail> iterator = detailList.iterator(); iterator.hasNext();) {
			total = total + iterator.next().getSubtotal();
		}

		log.info("Total:" + total);

		txtTotal.setValue(String.valueOf(total));

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
			ViewHelper.showNotification("El tipo de documento no tiene consecutivo configurado",
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

						buildLotWindow(selectedProduct);
						if (selectedLot != null) {
							itemsList.add(docDetail);
							DocumentDetailLot detailLot = DocumentDetailLot.builder().documentDetail(docDetail)
									.lot(selectedLot).initialStockLot(selectedLot.getQuantity()).build();
							detailLotList.add(detailLot);
							fillDetailGridData(itemsList);
							productSubwindow.close();
						}
					} catch (Exception e) {
						log.error("Error al cargar lotes del producto. Exception: " + e);
					}
				}
			}

		} else {
			ViewHelper.showNotification("No ha seleccionado un producto", Notification.Type.WARNING_MESSAGE);
		}

	}

	/**
	 * Metodo que construye la venta para buscar productos
	 */
	private void buildLotWindow(Product product) {

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
							closeWindow(lotSubwindow);
						}
					});
		}

	}

	private void addItemList() {
		DocumentDetail docDetail = DocumentDetail.builder().product(selectedProduct).build();
		itemsList.add(docDetail);
		fillDetailGridData(itemsList);
		productSubwindow.close();
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
		
		dataProvider.addDataProviderListener(event -> footer.getCell(columnTotal).setHtml(calculateTotal(dataProvider)));
		dataProvider.refreshAll();
	}

	@Transactional(rollbackFor = Exception.class)
	private void saveButtonAction(Document documentEntity) {
		Document.Builder docBuilder = null;
		if (documentEntity == null) {
			docBuilder = Document.builder();
		} else {
			docBuilder = Document.builder(documentEntity);
		}

		Date docDate = DateUtil.localDateTimeToDate(dtfDocumentDate.getValue());

		List<DocumentDetail> detailList = detailGrid.getDataProvider().fetch(new Query<>())
				.collect(Collectors.toList());
		log.info("personSelected:" + selectedPerson);

		DocumentStatus documentStatus = docStatusBll.select("Registrada").get(0);

		documentEntity = docBuilder.code(txtDocNumber.getValue())
				.reference(txtReference.getValue() != null ? txtReference.getValue() : "")
				.documentType(cbDocumentType.getValue()).person(selectedPerson).documentDate(docDate)
				.paymentMethod(cbPaymentMethod.getValue()).paymentType(cbPaymentType.getValue())
				.paymentTerm(txtPaymentTerm.getValue())
				.expirationDate(DateUtil.localDateTimeToDate(dtfExpirationDate.getValue())).status(documentStatus)
				.build();

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
		}

		// Guardar el detalle de la factura
		for (DocumentDetail detObj : detailList) {
			if (detObj.getQuantity() == null) {
				ViewHelper.showNotification("Cantidad no ingresada", Notification.Type.ERROR_MESSAGE);
			}
			// Guardar Detail
			DocumentDetail.Builder detailBuilder = DocumentDetail.builder();
			log.info("Cant=" + detObj.getQuantity());
			Integer cant = Integer.parseInt(detObj.getQuantity());
			Product prod = detObj.getProduct();
			Double subtotal = cant * prod.getSalePrice();
			DocumentDetail detail = detailBuilder.product(prod).document(documentEntity).quantity(cant + "")
					.description(detObj.getDescription()).subtotal(subtotal).build();

			// Guardar inventario
			InventoryTransaction.Builder invBuilder = InventoryTransaction.builder();

			Integer stock = detObj.getProduct().getStock();
			int initialStock = stock != null ? stock : 0;
			log.info("initialStock=" + initialStock);
			log.info("cant=" + cant);
			int finalStock = initialStock != 0 ? initialStock - cant : cant;
			log.info("finalStock=" + finalStock);
			InventoryTransaction inv = invBuilder.product(detObj.getProduct()).transactionType(transactionType)
					.initialStock(initialStock).quantity(cant).finalStock(finalStock).document(documentEntity).build();

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

				docDetailBll.save(detail);
				inventoryBll.save(inv);
				productBll.save(productObj);
				if (lotObj != null) {
					lotBll.save(lotObj);
				}

				// Actualizar consecutivo de tipo de documento
				documentTypeBll.save(documentType);

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

		ViewHelper.showNotification("Factura guardada con exito", Notification.Type.WARNING_MESSAGE);

	}

	private void cleanButtonAction() {
		log.info("Nuevo");
		txtDocNumber.clear();
		txtDocNumFilter.clear();
		txtReference.clear();
		txtPerson.clear();
		txtPaymentTerm.clear();
		cbDocumentType.clear();
		cbPaymentMethod.clear();
		cbPaymentType.clear();
		cbDocumentStatus.clear();
		itemsList.clear();
		detailGrid.getDataProvider().refreshAll();
		txtDocNumFilter.clear();

	}

	private void searchDocument(String documentNumber) {
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

}
