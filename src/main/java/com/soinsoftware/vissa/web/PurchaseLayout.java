package com.soinsoftware.vissa.web;

import java.io.IOException;
import java.math.BigInteger;
import java.time.LocalDate;
import java.time.ZoneId;
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

import com.soinsoftware.vissa.bll.DocumentBll;
import com.soinsoftware.vissa.bll.DocumentDetailBll;
import com.soinsoftware.vissa.bll.DocumentTypeBll;
import com.soinsoftware.vissa.bll.InventoryTransactionBll;
import com.soinsoftware.vissa.bll.LotBll;
import com.soinsoftware.vissa.bll.PaymentMethodBll;
import com.soinsoftware.vissa.bll.PaymentTypeBll;
import com.soinsoftware.vissa.bll.PersonBll;
import com.soinsoftware.vissa.bll.ProductBll;
import com.soinsoftware.vissa.exception.ModelValidationException;
import com.soinsoftware.vissa.model.Document;
import com.soinsoftware.vissa.model.DocumentDetail;
import com.soinsoftware.vissa.model.DocumentType;
import com.soinsoftware.vissa.model.InventoryTransaction;
import com.soinsoftware.vissa.model.InventoryTransactionType;
import com.soinsoftware.vissa.model.Lot;
import com.soinsoftware.vissa.model.PaymentMethod;
import com.soinsoftware.vissa.model.PaymentType;
import com.soinsoftware.vissa.model.Person;
import com.soinsoftware.vissa.model.Product;
import com.soinsoftware.vissa.model.PurchaseBean;
import com.soinsoftware.vissa.util.DateUtil;
import com.soinsoftware.vissa.util.ViewHelper;
import com.vaadin.data.Binder;
import com.vaadin.data.Binder.Binding;
import com.vaadin.data.provider.ConfigurableFilterDataProvider;
import com.vaadin.data.provider.ListDataProvider;
import com.vaadin.data.provider.Query;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.server.FontAwesome;
import com.vaadin.server.SerializablePredicate;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.DateField;
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

@SuppressWarnings("deprecation")
public class PurchaseLayout extends VerticalLayout implements View {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5187486966314714738L;
	protected static final Logger log = Logger.getLogger(PurchaseLayout.class);

	// Bll
	private final PersonBll personBll;
	private final ProductBll productBll;
	private final PaymentMethodBll payMethodBll;
	private final PaymentTypeBll payTypeBll;
	private final DocumentBll documentBll;
	private final DocumentDetailBll docDetailBll;
	private final InventoryTransactionBll inventoryBll;
	private final DocumentTypeBll docTypeBll;
	private final LotBll lotBll;

	PurchaseBean purchaseBean;
	// Components
	private TextField txtDocNumFilter;
	private TextField txtDocNumber;
	private TextField txtRefSupplier;
	private TextField txtSupplier;
	private Grid<Person> personGrid;
	private DateField dtPurchaseDate;
	private ComboBox<PaymentType> cbPaymentType;
	private ComboBox<DocumentType> cbDocumentType;
	private TextField txtPaymentTerm;
	private DateField dtExpirationDate;
	private ComboBox<PaymentMethod> cbPaymentMethod;
	private Grid<DocumentDetail> docDetailGrid;
	private Grid<Product> productGrid;
	private Window personSubwindow;
	private Window productSubwindow;
	private Set<Product> productSet = null;
	private Set<Person> personSet = null;
	private Person personSelected = null;
	private Product productSelected = null;
	private Double subtotal;
	private Document document;
	private List<DocumentDetail> itemsList = null;
	ProductLayout productLayout = null;

	public PurchaseLayout() throws IOException {
		super();
		personBll = PersonBll.getInstance();
		productBll = ProductBll.getInstance();
		payMethodBll = PaymentMethodBll.getInstance();
		payTypeBll = PaymentTypeBll.getInstance();
		documentBll = DocumentBll.getInstance();
		docDetailBll = DocumentDetailBll.getInstance();
		inventoryBll = InventoryTransactionBll.getInstance();
		docTypeBll = DocumentTypeBll.getInstance();
		lotBll = LotBll.getInstance();
		purchaseBean = new PurchaseBean();
		document = new Document();
		itemsList = new ArrayList<>();

	}

	@Override
	public void enter(ViewChangeEvent event) {

		View.super.enter(event);

		// setMargin(true);
		Label tittle = new Label("Pedido");
		tittle.addStyleName(ValoTheme.LABEL_H1);
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
	}

	/**
	 * Construcción del panel de botones
	 * 
	 * @return
	 */

	private Panel buildButtonPanel() {

		HorizontalLayout layout = ViewHelper.buildHorizontalLayout(true, true);
		Button newBtn = new Button("Nuevo", FontAwesome.SAVE);
		newBtn.addStyleName(ValoTheme.BUTTON_PRIMARY);
		newBtn.addClickListener(e -> cleanButtonAction());

		Button saveBtn = new Button("Guardar", FontAwesome.SAVE);
		saveBtn.addStyleName(ValoTheme.BUTTON_FRIENDLY);
		saveBtn.addClickListener(e -> saveButtonAction(null));

		Button editBtn = new Button("Edit", FontAwesome.EDIT);
		editBtn.addStyleName(ValoTheme.BUTTON_FRIENDLY);
		// editBtn.addClickListener(e -> saveButtonAction(null));

		Button deleteBtn = new Button("Delete", FontAwesome.ERASER);
		deleteBtn.addStyleName(ValoTheme.BUTTON_DANGER);
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

		layout.addComponents(txtDocNumFilter);

		return ViewHelper.buildPanel(null, layout);
	}

	/**
	 * Construcción del panel de encabezado factura
	 * 
	 * @return
	 */
	private Panel buildHeaderPanel() {
		VerticalLayout layout = ViewHelper.buildVerticalLayout(true, true);

		cbDocumentType = new ComboBox<DocumentType>("Tipo de pedido");
		cbDocumentType.setWidth("250px");
		ListDataProvider<DocumentType> docTypeDataProv = new ListDataProvider<>(docTypeBll.selectAll());
		cbDocumentType.setDataProvider(docTypeDataProv);
		cbDocumentType.setItemCaptionGenerator(DocumentType::getName);
		cbDocumentType.addValueChangeListener(e -> {
			getMaxDocNumber(cbDocumentType.getValue());
		});
		cbDocumentType.setEmptySelectionAllowed(false);
		cbDocumentType.setEmptySelectionCaption("Seleccione");

		txtDocNumber = new TextField("Número de factura");
		txtDocNumber.setEnabled(false);

		txtRefSupplier = new TextField("Referencia proveedor");

		txtSupplier = new TextField("Proveedor");
		txtSupplier.setWidth("250px");
		txtSupplier.setEnabled(false);

		Button searchSupplierButton = new Button("Buscar proveedor", FontAwesome.SEARCH);
		searchSupplierButton.addClickListener(e -> buildPersonWindow(txtSupplier.getValue()));
		searchSupplierButton.addStyleName(ValoTheme.BUTTON_ICON_ONLY);

		dtPurchaseDate = new DateField("Fecha");
		dtPurchaseDate.setValue(new Date().toInstant().atZone(ZoneId.systemDefault()).toLocalDate());

		cbPaymentType = new ComboBox<PaymentType>("Forma de pago");
		cbPaymentType.setWidth("250px");
		cbPaymentType.setEmptySelectionAllowed(false);
		cbPaymentType.setEmptySelectionCaption("Seleccione");
		ListDataProvider<PaymentType> payTypeDataProv = new ListDataProvider<>(payTypeBll.selectAll());
		cbPaymentType.setDataProvider(payTypeDataProv);
		cbPaymentType.setItemCaptionGenerator(PaymentType::getName);

		txtPaymentTerm = new TextField("Plazo");

		txtPaymentTerm.addValueChangeListener(e -> {
			setExpirationDate(txtPaymentTerm.getValue());
		});

		dtExpirationDate = new DateField("Fecha de Vencimiento");
		dtExpirationDate.setEnabled(false);

		cbPaymentMethod = new ComboBox<PaymentMethod>("Método de pago");
		cbPaymentMethod.setWidth("250px");
		cbPaymentMethod.setEmptySelectionAllowed(false);
		cbPaymentMethod.setEmptySelectionCaption("Seleccione");
		ListDataProvider<PaymentMethod> payMetDataProv = new ListDataProvider<>(payMethodBll.selectAll());
		cbPaymentMethod.setDataProvider(payMetDataProv);
		cbPaymentMethod.setItemCaptionGenerator(PaymentMethod::getName);

		HorizontalLayout headerLayout1 = ViewHelper.buildHorizontalLayout(false, false);

		headerLayout1.addComponents(cbDocumentType, txtDocNumber, txtRefSupplier, txtSupplier, searchSupplierButton,
				dtPurchaseDate);
		headerLayout1.setComponentAlignment(searchSupplierButton, Alignment.BOTTOM_CENTER);

		HorizontalLayout headerLayout2 = ViewHelper.buildHorizontalLayout(false, false);
		headerLayout2.addComponents(cbPaymentType, txtPaymentTerm, dtExpirationDate, cbPaymentMethod);

		layout.addComponents(headerLayout1, headerLayout2);

		return ViewHelper.buildPanel(null, layout);
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

		docDetailGrid = new Grid<>();
		docDetailGrid.setSizeFull();
		docDetailGrid.setEnabled(true);

		// columns
		docDetailGrid.addColumn(documentDetail -> {
			if (documentDetail.getProduct() != null) {
				return documentDetail.getProduct().getCode();
			} else {
				return null;
			}
		}).setCaption("Código");
		docDetailGrid.addColumn(documentDetail -> {
			if (documentDetail.getProduct() != null) {
				return documentDetail.getProduct().getName();
			} else {
				return null;
			}
		}).setCaption("Nombre");
		docDetailGrid.addColumn(documentDetail -> {
			if (documentDetail.getProduct() != null) {
				return documentDetail.getProduct().getMeasurementUnit().getName();
			} else {
				return null;
			}
		}).setCaption("Unidad de medida");
		docDetailGrid.addColumn(documentDetail -> {
			if (documentDetail.getProduct() != null) {
				return documentDetail.getProduct().getSalePrice();
			} else {
				return null;
			}
		}).setCaption("Precio");

		// Columna cantidad editable
		TextField quantityField = new TextField();

		Binder<DocumentDetail> binder = docDetailGrid.getEditor().getBinder();

		Binding<DocumentDetail, String> doneBinding = binder.bind(quantityField, DocumentDetail::getQuantity,
				DocumentDetail::setQuantity);

		docDetailGrid.addColumn(DocumentDetail::getQuantity).setCaption("Cantidad").setEditorBinding(doneBinding);

		quantityField.addValueChangeListener(e -> {
			setSubtotal(quantityField.getValue());
		});

		docDetailGrid.addColumn(documentDetail -> {
			if (documentDetail.getSubtotal() != 0) {
				return documentDetail.getSubtotal();
			} else {
				return subtotal;
			}
		}).setCaption("Subtotal");

		docDetailGrid.getEditor().setEnabled(true);

		ListDataProvider<DocumentDetail> detailDataProv = new ListDataProvider<>(Arrays.asList(new DocumentDetail()));
		ConfigurableFilterDataProvider<DocumentDetail, Void, SerializablePredicate<DocumentDetail>> filterDataProvider = detailDataProv
				.withConfigurableFilter();
		docDetailGrid.setDataProvider(filterDataProvider);

		HorizontalLayout itemsLayout = ViewHelper.buildHorizontalLayout(false, false);
		itemsLayout.setSizeFull();
		itemsLayout.addComponents(ViewHelper.buildPanel(null, docDetailGrid));

		layout.addComponents(searchProductBt, itemsLayout);

		return ViewHelper.buildPanel("Productos", layout);
	}

	/**
	 * Metodo para construir la venta modal de personas
	 * 
	 * @param personFiltter
	 */
	private void buildPersonWindow(String personFiltter) {

		personSubwindow = ViewHelper.buildSubwindow("75%");

		VerticalLayout subContent = ViewHelper.buildVerticalLayout(true, true);

		Button backBtn = new Button("Cancelar", FontAwesome.BACKWARD);
		backBtn.addStyleName(ValoTheme.BUTTON_DANGER);
		Button selectBtn = new Button("Seleccionar", FontAwesome.CHECK);
		selectBtn.addStyleName(ValoTheme.BUTTON_PRIMARY);
		Button newBtn = new Button("Crear proveedor", FontAwesome.PLUS);
		newBtn.addStyleName(ValoTheme.BUTTON_FRIENDLY);

		HorizontalLayout buttonLayout = ViewHelper.buildHorizontalLayout(true, true);
		buttonLayout.addComponents(backBtn, selectBtn, newBtn);

		// Grid de personas
		personGrid = new Grid<>();
		fillPersonDataGrid();
		personGrid.addColumn(Person::getDocumentNumber).setCaption("Número identificación");
		personGrid.addColumn(Person::getName).setCaption("Nombres");
		personGrid.addColumn(Person::getLastName).setCaption("Apellidos");

		personGrid.setSelectionMode(SelectionMode.SINGLE);
		personGrid.setSizeFull();

		personGrid.addSelectionListener(event -> {
			personSet = event.getAllSelectedItems();

		});

		selectBtn.addClickListener(e -> selectPerson());
		newBtn.addClickListener(e -> createPerson());

		HorizontalLayout gridLayout = ViewHelper.buildHorizontalLayout(true, true);
		gridLayout.setSizeFull();
		gridLayout.addComponent(personGrid);

		subContent.addComponents(buttonLayout, gridLayout);

		personSubwindow.setContent(subContent);
		getUI().addWindow(personSubwindow);

	}

	/**
	 * Método para seleccionar proveedor o cliente
	 */
	private void selectPerson() {
		if (personSet != null) {
			for (Iterator<Person> iterator = personSet.iterator(); iterator.hasNext();) {
				personSelected = iterator.next();
			}
			if (personSelected != null) {
				txtSupplier.setValue(personSelected.getName() + " " + personSelected.getLastName());
				personSubwindow.close();
			} else {
				ViewHelper.showNotification("Seleccione un proveedor", Notification.TYPE_WARNING_MESSAGE);
			}
		}
	}

	/**
	 * Metodo para crear nueva persona
	 */
	private void createPerson() {

	}

	/**
	 * Metodo encargado de llenar la grid de personas (Proveedores, Clientes)
	 */
	private void fillPersonDataGrid() {
		ListDataProvider<Person> dataProvider = new ListDataProvider<>(personBll.selectAll());
		ConfigurableFilterDataProvider<Person, Void, SerializablePredicate<Person>> filterPersonDataProv = dataProvider
				.withConfigurableFilter();
		personGrid.setDataProvider(filterPersonDataProv);
	}

	/**
	 * Obtener el número máximo del documento: venta, compra, remision
	 * 
	 * @param val
	 */
	private void getMaxDocNumber(DocumentType val) {
		txtDocNumber.setValue(val.getCode() + "-" + documentBll.selectMaxDoc());
	}

	/**
	 * Setear la fecha de vencimiento de acuerdo al plazo
	 * 
	 * @param val
	 */
	private void setExpirationDate(String val) {

		LocalDate lDate = dtPurchaseDate.getValue();
		Date fecha = Date.from(lDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
		Date fecha2 = DateUtil.addDaysToDate(fecha, Integer.parseInt(val));
		LocalDate lDate2 = fecha2.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
		dtExpirationDate.setValue(lDate2);

	}

	/**
	 * Evento para calcular el subtotal
	 * 
	 * @param val
	 */
	private void setSubtotal(String val) {
		log.info("setSubtotal:" + val);
		/*
		 * Set<DocumentDetail> detailSet = docDetailGrid.getSelectedItems();
		 * DocumentDetail itemSelected = null; DocumentDetail itemSelected2 = null; for
		 * (Iterator<DocumentDetail> iterator = detailSet.iterator();
		 * iterator.hasNext();) { itemSelected = iterator.next(); itemSelected2 =
		 * itemSelected; } log.info("itemSelected:" + itemSelected); double subt =
		 * itemSelected.getProduct().getSalePrice() * Integer.parseInt(val);
		 * log.info("subt:" + subt); itemSelected2.setSubtotal(subt); int pos =
		 * itemsList.indexOf(itemSelected); log.info("pos:" + pos); itemsList.set(pos,
		 * itemSelected2);
		 */
		subtotal = productSelected.getSalePrice() * Integer.parseInt(val);
		docDetailGrid.getDataProvider().refreshAll();
		System.out.println("subtotal" + subtotal);

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
		backBtn.addStyleName(ValoTheme.BUTTON_DANGER);
		Button selectBtn = new Button("Seleccionar", FontAwesome.CHECK);
		selectBtn.addStyleName(ValoTheme.BUTTON_PRIMARY);
		Button newBtn = new Button("Crear producto", FontAwesome.PLUS);
		newBtn.addStyleName(ValoTheme.BUTTON_FRIENDLY);

		HorizontalLayout buttonLayout = ViewHelper.buildHorizontalLayout(true, true);
		buttonLayout.addComponents(backBtn, selectBtn);
		Panel buttonPanel = ViewHelper.buildPanel(null, buttonLayout);

		newBtn.addClickListener(e -> createProduct());
		selectBtn.addClickListener(e -> selectProduct());

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
	 * Metodo para crear nuevo producto
	 */
	private void createProduct() {

	}

	/**
	 * Metodo para escoger productos a agregar a la factura
	 */
	private void selectProduct() {

		productSelected = productLayout.getSelected();
		// ---Panel de lotes

		/*
		 * try { buildLotWindow(productSelected); } catch (Exception e) {
		 * log.error("Error al cargar lotes del producto. Exception: " + e);
		 * 
		 * }
		 */

		if (productSelected != null) {
			DocumentDetail docDetail = new DocumentDetail();
			DocumentDetail.Builder docDetailBuilder = DocumentDetail.builder();
			docDetail = docDetailBuilder.product(productSelected).archived(false).build();
			itemsList.add(docDetail);
			fillDocDetailGridData(itemsList);
			productSubwindow.close();
		} else {
			ViewHelper.showNotification("No ha seleccionado un producto", Notification.TYPE_WARNING_MESSAGE);
		}

	}

	/**
	 * Metodo que construye la venta para buscar productos
	 */
	private void buildLotWindow(Product product) {

		Window lotSubwindow = ViewHelper.buildSubwindow("50%");
		lotSubwindow.setCaption("Lotes");

		VerticalLayout subContent = ViewHelper.buildVerticalLayout(true, true);

		// Panel de botones
		Button backBtn = new Button("Cancelar", FontAwesome.BACKWARD);
		backBtn.addStyleName(ValoTheme.BUTTON_DANGER);
		Button selectBtn = new Button("Seleccionar", FontAwesome.CHECK);
		selectBtn.addStyleName(ValoTheme.BUTTON_PRIMARY);

		HorizontalLayout buttonLayout = ViewHelper.buildHorizontalLayout(true, true);
		buttonLayout.addComponents(backBtn, selectBtn);

		LotLayout lotPanel = null;
		try {
			lotPanel = new LotLayout(productSelected);
			lotPanel.setCaption("Lotes");
			lotPanel.setMargin(false);
			lotPanel.setSpacing(false);
		} catch (IOException e) {
			log.error("Error al cargar lista de lotes. Exception:" + e);
		}
		subContent.addComponents(buttonLayout, lotPanel);

		lotSubwindow.setContent(subContent);
		getUI().addWindow(lotSubwindow);

	}

	/**
	 * Metodo para llenar la data de la grid de detalle de la factura
	 * 
	 * @param detailList
	 */
	private void fillDocDetailGridData(List<DocumentDetail> detailList) {

		ListDataProvider<DocumentDetail> dataProvider = new ListDataProvider<>(detailList);
		ConfigurableFilterDataProvider<DocumentDetail, Void, SerializablePredicate<DocumentDetail>> filterDataProv = dataProvider
				.withConfigurableFilter();
		docDetailGrid.setDataProvider(filterDataProv);
	}

	@Transactional(rollbackFor = Exception.class)
	private void saveButtonAction(Document documentEntity) {
		Document.Builder docBuilder = null;
		if (documentEntity == null) {
			docBuilder = Document.builder();
		} else {
			docBuilder = Document.builder(documentEntity);
		}
		DocumentType.Builder docTypeBuilder = DocumentType.builder();
		docTypeBuilder.id(BigInteger.valueOf(1));
		docTypeBuilder.code("CO").name("Factura de Venta");
		Date docDate = DateUtil.localDateToDate(dtPurchaseDate.getValue());

		List<DocumentDetail> detailList = docDetailGrid.getDataProvider().fetch(new Query<>())
				.collect(Collectors.toList());
		log.info("personSelected:" + personSelected);

		documentEntity = docBuilder.code(txtDocNumber.getValue()).documentType(cbDocumentType.getValue())
				.person(personSelected).documentDate(docDate).paymentMethod(cbPaymentMethod.getValue())
				.paymentType(cbPaymentType.getValue()).paymentTerm(txtPaymentTerm.getValue()).build();

		// Guardar documento
		try {

			documentBll.save(documentEntity);

			// afterSave(caption);
		} catch (ModelValidationException ex) {
			log.error(ex);
			ViewHelper.showNotification(ex.getMessage(), Notification.Type.ERROR_MESSAGE);
		} catch (HibernateException ex) {
			log.error(ex);
			// bll.rollback();
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
			InventoryTransactionType txType = InventoryTransactionType.ENTRADA;
			Integer stock = detObj.getProduct().getStock();
			int initialStock = stock != null ? stock : 0;
			log.info("initialStock=" + initialStock);
			log.info("cant=" + cant);
			int finalStock = initialStock != 0 ? initialStock - cant : cant;
			log.info("finalStock=" + finalStock);
			InventoryTransaction inv = invBuilder.product(detObj.getProduct()).transactionType(txType)
					.initialStock(initialStock).quantity(cant).finalStock(finalStock).document(documentEntity).build();

			// Actualizar stock total del producto
			Product productObj = productBll.select(detObj.getProduct().getCode());
			productObj.setStock(finalStock);
			productObj.setStockDate(new Date());

			// Actualizar lotes del producto
			List<Lot> lotList = lotBll.select(detObj.getProduct());
			Lot lot = lotList.get(0);
			log.info("lote más pronto a vencer:" + lot.getExpirationDate());

			Lot lotObj = lotBll.select(lot.getCode());
			int newLotStock = lotObj.getQuantity() - cant;
			lotObj.setQuantity(newLotStock);

			try {

				docDetailBll.save(detail);
				inventoryBll.save(inv);
				productBll.save(productObj);
				lotBll.save(lotObj);

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

		ViewHelper.showNotification("Factura guardada con exito", Notification.Type.ERROR_MESSAGE);

	}

	private void cleanButtonAction() {
		buildHeaderPanel();

	}

}
