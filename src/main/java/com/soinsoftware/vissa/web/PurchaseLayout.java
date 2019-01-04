package com.soinsoftware.vissa.web;

import java.io.IOException;
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
import com.soinsoftware.vissa.bll.DocumentStatusBll;
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
import com.soinsoftware.vissa.model.DocumentStatus;
import com.soinsoftware.vissa.model.DocumentType;
import com.soinsoftware.vissa.model.InventoryTransaction;
import com.soinsoftware.vissa.model.Lot;
import com.soinsoftware.vissa.model.PaymentMethod;
import com.soinsoftware.vissa.model.PaymentType;
import com.soinsoftware.vissa.model.Person;
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
	private final DocumentStatusBll docStatusBll;
	private final LotBll lotBll;

	// Components
	private TextField txtDocNumFilter;
	private TextField txtDocNumber;
	private TextField txtReference;
	private TextField txtPerson;
	private Grid<Person> personGrid;
	private DateField dtDocumentDate;
	private ComboBox<PaymentType> cbPaymentType;
	private ComboBox<DocumentType> cbDocumentType;
	private TextField txtPaymentTerm;
	private DateField dtExpirationDate;
	private ComboBox<PaymentMethod> cbPaymentMethod;
	private ComboBox<DocumentStatus> cbDocumentStatus;
	private Grid<DocumentDetail> detailGrid;
	private Grid<Product> productGrid;
	private Window personSubwindow;
	private Window productSubwindow;
	private Set<Product> productSet = null;
	private Set<Person> personSet = null;
	private Person personSelected = null;
	private Product productSelected = null;
	private Document document;
	private List<DocumentDetail> itemsList = null;
	private ProductLayout productLayout = null;
	private TransactionType transactionType;

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
		docStatusBll = DocumentStatusBll.getInstance();
		lotBll = LotBll.getInstance();
		document = new Document();
		itemsList = new ArrayList<>();
		transactionType = TransactionType.valueOf(Commons.DOCUMENT_TYPE);

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

		Button deleteBtn = new Button("Cancelar", FontAwesome.ERASER);
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

		txtDocNumFilter.addValueChangeListener(e -> searchDocument(txtDocNumFilter.getValue()));

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

		ListDataProvider<DocumentType> docTypeDataProv = new ListDataProvider<>(docTypeBll.select(transactionType));

		cbDocumentType.setDataProvider(docTypeDataProv);
		cbDocumentType.setItemCaptionGenerator(DocumentType::getName);
		cbDocumentType.addValueChangeListener(e -> {
			getNextDocumentNumber(cbDocumentType.getValue());
		});
		cbDocumentType.setEmptySelectionAllowed(false);
		cbDocumentType.setEmptySelectionCaption("Seleccione");

		txtDocNumber = new TextField("Número de factura");
		txtDocNumber.setEnabled(false);

		txtReference = new TextField("Referencia proveedor");

		txtPerson = new TextField("Proveedor");
		txtPerson.setWidth("250px");
		txtPerson.setEnabled(false);

		Button searchSupplierButton = new Button("Buscar proveedor", FontAwesome.SEARCH);
		searchSupplierButton.addClickListener(e -> buildPersonWindow(txtPerson.getValue()));
		searchSupplierButton.addStyleName(ValoTheme.BUTTON_ICON_ONLY);

		dtDocumentDate = new DateField("Fecha");
		dtDocumentDate.setValue(LocalDate.now());
		dtDocumentDate.setDateFormat(Commons.FORMAT_DATE);

		cbPaymentType = new ComboBox<PaymentType>("Forma de pago");
		cbPaymentType.setWidth("250px");
		cbPaymentType.setEmptySelectionAllowed(false);
		cbPaymentType.setEmptySelectionCaption("Seleccione");
		ListDataProvider<PaymentType> payTypeDataProv = new ListDataProvider<>(payTypeBll.selectAll());
		cbPaymentType.setDataProvider(payTypeDataProv);
		cbPaymentType.setItemCaptionGenerator(PaymentType::getName);

		txtPaymentTerm = new TextField("Plazo");

		dtExpirationDate = new DateField("Fecha de Vencimiento");
		dtExpirationDate.setEnabled(false);
		dtExpirationDate.setDateFormat(Commons.FORMAT_DATE);

		txtPaymentTerm.addValueChangeListener(e -> {
			setExpirationDate(txtPaymentTerm.getValue());
		});

		cbPaymentMethod = new ComboBox<PaymentMethod>("Método de pago");
		cbPaymentMethod.setWidth("250px");
		cbPaymentMethod.setEmptySelectionAllowed(false);
		cbPaymentMethod.setEmptySelectionCaption("Seleccione");
		ListDataProvider<PaymentMethod> payMetDataProv = new ListDataProvider<>(payMethodBll.selectAll());
		cbPaymentMethod.setDataProvider(payMetDataProv);
		cbPaymentMethod.setItemCaptionGenerator(PaymentMethod::getName);

		cbDocumentStatus = new ComboBox<DocumentStatus>("Estado de la factura");
		cbDocumentStatus.setWidth("250px");
		cbDocumentStatus.setEmptySelectionAllowed(false);
		cbDocumentStatus.setEmptySelectionCaption("Seleccione");
		ListDataProvider<DocumentStatus> docStatusDataProv = new ListDataProvider<>(docStatusBll.selectAll());
		cbDocumentStatus.setDataProvider(docStatusDataProv);
		cbDocumentStatus.setItemCaptionGenerator(DocumentStatus::getName);
		cbDocumentStatus.setSelectedItem(docStatusBll.select("Nueva").get(0));

		HorizontalLayout headerLayout1 = ViewHelper.buildHorizontalLayout(false, false);

		headerLayout1.addComponents(cbDocumentType, txtDocNumber, txtReference, txtPerson, searchSupplierButton,
				dtDocumentDate);
		headerLayout1.setComponentAlignment(searchSupplierButton, Alignment.BOTTOM_CENTER);

		HorizontalLayout headerLayout2 = ViewHelper.buildHorizontalLayout(false, false);
		headerLayout2.addComponents(cbPaymentType, txtPaymentTerm, dtExpirationDate, cbPaymentMethod, cbDocumentStatus);

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

		detailGrid.addColumn(DocumentDetail::getQuantity).setCaption("Cantidad").setEditorComponent(new TextField(),
				DocumentDetail::setQuantity);

		detailGrid.addColumn(documentDetail -> {
			if (documentDetail.getSubtotal() != 0) {
				return documentDetail.getSubtotal();
			} else {
				return "";
			}
		}).setCaption("Subtotal");

		detailGrid.getEditor().setEnabled(true);

		ListDataProvider<DocumentDetail> detailDataProv = new ListDataProvider<>(Arrays.asList(new DocumentDetail()));
		ConfigurableFilterDataProvider<DocumentDetail, Void, SerializablePredicate<DocumentDetail>> filterDataProvider = detailDataProv
				.withConfigurableFilter();
		detailGrid.setDataProvider(filterDataProvider);

		HorizontalLayout itemsLayout = ViewHelper.buildHorizontalLayout(false, false);
		itemsLayout.setSizeFull();
		itemsLayout.addComponents(ViewHelper.buildPanel(null, detailGrid));

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
				txtPerson.setValue(personSelected.getName() + " " + personSelected.getLastName());
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
	 * @param docType
	 */
	private void getNextDocumentNumber(DocumentType docType) {
		if (docType != null && docType.getCode() != null && !docType.getCode().isEmpty())
			txtDocNumber.setValue(docType.getCode() + "-" + documentBll.selectNextDocumentNumber());
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
				LocalDate docDate = dtDocumentDate.getValue();
				if (docDate != null) {
					Date iniDate = DateUtil.localDateToDate(docDate);
					Date endDate = DateUtil.addDaysToDate(iniDate, paymentTerm);
					LocalDate expDate = DateUtil.dateToLocalDate(endDate);
					dtExpirationDate.setValue(expDate);
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
		backBtn.addStyleName(ValoTheme.BUTTON_DANGER);
		backBtn.addClickListener(e -> closeWindow(productSubwindow));

		Button selectBtn = new Button("Seleccionar", FontAwesome.CHECK);
		selectBtn.addStyleName(ValoTheme.BUTTON_PRIMARY);
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

		productSelected = productLayout.getSelected();

		// ---Panel de lotes
		/*
		 * try { buildLotWindow(productSelected); } catch (Exception e) {
		 * log.error("Error al cargar lotes del producto. Exception: " + e); }
		 */

		if (productSelected != null) {
			DocumentDetail docDetail = DocumentDetail.builder().product(productSelected).build();
			if (itemsList.contains(docDetail)) {
				ViewHelper.showNotification("Este producto ya está agregado a la factura",
						Notification.TYPE_WARNING_MESSAGE);
			} else {
				if (productSelected.getSalePrice() == null) {
					ViewHelper.showNotification("El producto no tiene precio configurado",
							Notification.TYPE_WARNING_MESSAGE);
				} else if (productSelected.getStock() == null || productSelected.getStock() == 0) {
					ViewHelper.showNotification("El producto no tiene stock disponible",
							Notification.TYPE_WARNING_MESSAGE);
				} else {
					itemsList.add(docDetail);
					fillDetailGridData(itemsList);
					productSubwindow.close();
				}
			}

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
	private void fillDetailGridData(List<DocumentDetail> detailList) {

		ListDataProvider<DocumentDetail> dataProvider = new ListDataProvider<>(detailList);
		ConfigurableFilterDataProvider<DocumentDetail, Void, SerializablePredicate<DocumentDetail>> filterDataProv = dataProvider
				.withConfigurableFilter();
		detailGrid.setDataProvider(filterDataProv);
	}

	@Transactional(rollbackFor = Exception.class)
	private void saveButtonAction(Document documentEntity) {
		Document.Builder docBuilder = null;
		if (documentEntity == null) {
			docBuilder = Document.builder();
		} else {
			docBuilder = Document.builder(documentEntity);
		}

		Date docDate = DateUtil.localDateToDate(dtDocumentDate.getValue());

		List<DocumentDetail> detailList = detailGrid.getDataProvider().fetch(new Query<>())
				.collect(Collectors.toList());
		log.info("personSelected:" + personSelected);

		documentEntity = docBuilder.code(txtDocNumber.getValue()).documentType(cbDocumentType.getValue())
				.person(personSelected).documentDate(docDate).paymentMethod(cbPaymentMethod.getValue())
				.paymentType(cbPaymentType.getValue()).paymentTerm(txtPaymentTerm.getValue())
				.status(cbDocumentStatus.getValue()).build();

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
				txtReference.setValue(document.getReference());
				dtDocumentDate.setValue(DateUtil.dateToLocalDate(document.getDocumentDate()));
				cbPaymentType.setValue(document.getPaymentType());
				cbPaymentMethod.setValue(document.getPaymentMethod());
				txtPaymentTerm.setValue(document.getPaymentTerm() != null ? document.getPaymentTerm() : "");
				txtPerson.setValue(document.getPerson().getName() + " " + document.getPerson().getLastName());

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
