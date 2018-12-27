package com.soinsoftware.vissa.web;

import java.io.IOException;
import java.math.BigInteger;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
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
import com.soinsoftware.vissa.bll.PaymentMethodBll;
import com.soinsoftware.vissa.bll.PaymentTypeBll;
import com.soinsoftware.vissa.bll.PersonBll;
import com.soinsoftware.vissa.bll.ProductBll;
import com.soinsoftware.vissa.bll.ProductStockBll;
import com.soinsoftware.vissa.bll.SupplierBll;
import com.soinsoftware.vissa.exception.ModelValidationException;
import com.soinsoftware.vissa.model.BankAccount;
import com.soinsoftware.vissa.model.Document;
import com.soinsoftware.vissa.model.DocumentDetail;
import com.soinsoftware.vissa.model.DocumentType;
import com.soinsoftware.vissa.model.InventoryTransaction;
import com.soinsoftware.vissa.model.InventoryTransactionType;
import com.soinsoftware.vissa.model.PaymentMethod;
import com.soinsoftware.vissa.model.PaymentType;
import com.soinsoftware.vissa.model.Person;
import com.soinsoftware.vissa.model.Product;
import com.soinsoftware.vissa.model.ProductStockBk;
import com.soinsoftware.vissa.model.Supplier;
import com.soinsoftware.vissa.util.ViewHelper;
import com.vaadin.data.Binder;
import com.vaadin.data.Binder.Binding;
import com.vaadin.data.provider.ConfigurableFilterDataProvider;
import com.vaadin.data.provider.DataProvider;
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
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.themes.ValoTheme;

public class SaleLayout extends VerticalLayout implements View {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5076502522106126046L;

	private final SupplierBll supplierBll;
	private final PersonBll personBll;
	private final ProductBll productBll;
	private final PaymentMethodBll payMethodBll;
	private final PaymentTypeBll payTypeBll;
	private final DocumentBll documentBll;
	private final DocumentDetailBll docDetailBll;
	private final InventoryTransactionBll inventoryBll;
	private final DocumentTypeBll docTypeBll;
	private final ProductStockBll stockBll;

	private TextField txtDocNumber;
	private TextField txtRefSupplier;
	private TextField txtSupplier;
	private DateField dtPurchaseDate;
	private ComboBox<PaymentType> cbPaymentType;
	private ComboBox<DocumentType> cbDocumentType;
	private TextField txtPaymentTerm;
	private DateField dtExpirationDate;
	private ComboBox<PaymentMethod> cbPaymentMethod;
	private ComboBox<BankAccount> cbCurrency;
	private Grid<Person> personGrid;
	private Grid<Product> productGrid;
	private Grid<DocumentDetail> docDetailGrid;
	Supplier supplier = new Supplier();
	private ConfigurableFilterDataProvider<Person, Void, SerializablePredicate<Person>> filterPersonDataProvider;
	private ConfigurableFilterDataProvider<Product, Void, SerializablePredicate<Product>> filterProductDataProvider;
	private ConfigurableFilterDataProvider<DocumentDetail, Void, SerializablePredicate<DocumentDetail>> filterDocDetailDataProvider;
	Set<Product> prodSet = null;
	Set<Person> personSet = null;
	Product productSelected = null;
	Person personSelected = null;
	Window productSubWindow = null;
	Window personSubWindow = null;
	Document document;
	Double subtotal;
	List<DocumentDetail> itemsList = null;
	protected static final Logger log = Logger.getLogger(AbstractEditableLayout.class);

	public SaleLayout() throws IOException {
		super();
		supplierBll = SupplierBll.getInstance();
		personBll = PersonBll.getInstance();
		productBll = ProductBll.getInstance();
		payMethodBll = PaymentMethodBll.getInstance();
		payTypeBll = PaymentTypeBll.getInstance();
		documentBll = DocumentBll.getInstance();
		docDetailBll = DocumentDetailBll.getInstance();
		inventoryBll = InventoryTransactionBll.getInstance();
		docTypeBll = DocumentTypeBll.getInstance();
		stockBll = ProductStockBll.getInstance();
		itemsList = new ArrayList<>();
		document = new Document();
	}

	@Override
	public void enter(ViewChangeEvent event) {

		View.super.enter(event);

		setMargin(true);
		Label tittle = new Label("Venta");
		tittle.addStyleName(ValoTheme.LABEL_H1);
		addComponent(tittle);

		// ***********Components***************+
		/// 1. Informacion encabezado facuta
		txtDocNumber = new TextField("Número de factura");
		txtDocNumber.setEnabled(false);
		txtDocNumber.setValue("FV-"+documentBll.selectMaxDoc());
		txtRefSupplier = new TextField("Referencia proveedor");
		txtSupplier = new TextField("Cliente");
		txtSupplier.setWidth("250px");

		Button searchSupplierButton = new Button("Buscar Cliente", FontAwesome.SEARCH);
		searchSupplierButton.addClickListener(e -> searchSupplier(""));
		searchSupplierButton.addStyleName(ValoTheme.BUTTON_ICON_ONLY);

		dtPurchaseDate = new DateField("Fecha");
		dtPurchaseDate.setValue(new Date().toInstant().atZone(ZoneId.systemDefault()).toLocalDate());

		cbPaymentType = new ComboBox<PaymentType>("Forma de pago");
		cbPaymentType.setWidth("250px");

		cbDocumentType = new ComboBox<DocumentType>("Tipo de pedido");
		cbDocumentType.setWidth("250px");
		ListDataProvider<DocumentType> dataProviderD = new ListDataProvider<>(docTypeBll.selectAll());
		cbDocumentType.setDataProvider(dataProviderD);
		cbDocumentType.setItemCaptionGenerator(DocumentType::getName);

		ListDataProvider<PaymentType> dataProvider3 = new ListDataProvider<>(payTypeBll.selectAll());
		cbPaymentType.setDataProvider(dataProvider3);
		cbPaymentType.setItemCaptionGenerator(PaymentType::getName);

		txtPaymentTerm = new TextField("Plazo");

		txtPaymentTerm.addValueChangeListener(e -> {
			fechaVencimiento(txtPaymentTerm.getValue());
		});

		dtExpirationDate = new DateField("Fecha de Vencimiento");
		dtExpirationDate.setEnabled(false);

		cbPaymentMethod = new ComboBox<PaymentMethod>("Método de pago");
		cbPaymentMethod.setWidth("250px");
		ListDataProvider<PaymentMethod> payMethoddataProvider = new ListDataProvider<>(payMethodBll.selectAll());
		cbPaymentMethod.setDataProvider(payMethoddataProvider);
		cbPaymentMethod.setItemCaptionGenerator(PaymentMethod::getName);

		cbCurrency = new ComboBox<>("Moneda");

		HorizontalLayout headerLayout = new HorizontalLayout();

		headerLayout.addComponents(txtDocNumber, txtSupplier, searchSupplierButton, dtPurchaseDate);

		HorizontalLayout headerLayout2 = new HorizontalLayout();
		headerLayout2.addComponents(cbPaymentType, txtPaymentTerm, dtExpirationDate, cbPaymentMethod);

		headerLayout.setComponentAlignment(searchSupplierButton, Alignment.BOTTOM_CENTER);

		// ***************************

		// ***Barra de botones

		Button saveButton = new Button("Guardar", FontAwesome.SAVE);
		saveButton.addStyleName(ValoTheme.BUTTON_PRIMARY);
		saveButton.addClickListener(e -> saveButtonAction(null));

		Button editButton = new Button("Edit", FontAwesome.EDIT);
		editButton.addStyleName(ValoTheme.BUTTON_FRIENDLY);
		editButton.addClickListener(e -> saveButtonAction(null));

		Button deleteButton = new Button("Delete", FontAwesome.ERASER);
		deleteButton.addStyleName(ValoTheme.BUTTON_DANGER);
		deleteButton.addClickListener(e -> saveButtonAction(document));
//
		HorizontalLayout buttonLayout = new HorizontalLayout();
		// buttonLayout.setSpacing(true);
		// buttonLayout.setMargin(true);
		buttonLayout.addComponents(saveButton, editButton, deleteButton);

		// ***************************
		TextField txtSearchBill = new TextField("Buscar factura");
		// txtSearchBill.setValue("Buscar factura");
		HorizontalLayout filterLayout = new HorizontalLayout();
		// filterLayout.setSpacing(true);
		// filterLayout.setMargin(true);
		filterLayout.addComponent(txtSearchBill);

		// ***************************
		// Filtros
		Button searchProduct = new Button("Buscar productos", FontAwesome.PLUS);
		searchProduct.addStyleName(ValoTheme.BUTTON_SMALL);
		searchProduct.addClickListener(e -> searchProducts());

		HorizontalLayout itemsButtonLayout = new HorizontalLayout();
		// itemsButtonLayout.setMargin(true);
		itemsButtonLayout.addComponent(searchProduct);

		// ************************************************************************************************************
		// Tabla de items
		docDetailGrid = new Grid<>("Productos");
		docDetailGrid.setSizeFull();

		DocumentDetail docDetail = new DocumentDetail();
		fillDocDetailGridData(itemsList);
		// grid.setItems(createItems(50));
		//

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

		docDetailGrid.setEnabled(true);

		TextField taskField = new TextField();

		Binder<DocumentDetail> binder = docDetailGrid.getEditor().getBinder();

		Binding<DocumentDetail, String> doneBinding = binder.bind(taskField, DocumentDetail::getDescription,
				DocumentDetail::setDescription);

		// Description
		docDetailGrid.addColumn(DocumentDetail::getDescription).setCaption("Cantidad").setEditorBinding(doneBinding);

		// docDetailGrid.addColumn(DocumentDetail::getQuantity).setCaption("Cantidad");

		// docDetailGrid.addColumn(DocumentDetail::getQuantity).setCaption("Cantidad").setEditorComponent(tf,DocumentDetail::setQuantity);

		taskField.addValueChangeListener(e -> {
			cantListener(taskField.getValue());
		});

		// .addComponentColumn(this::button);

		// docDetailGrid.addColumn(DocumentDetail::getSubtotal).setCaption("Subtotal");

		docDetailGrid.addColumn(documentDetail -> {
			if (documentDetail.getSubtotal() != 0) {
				return documentDetail.getSubtotal();
			} else {
				return subtotal;
			}
		}).setCaption("Subtotal");

		// grid2.setSelectionMode(SelectionMode.SINGLE);
		// grid2.setColumnReorderingAllowed(true);
		docDetailGrid.getEditor().setEnabled(true);

		ListDataProvider<DocumentDetail> dataProvider4 = new ListDataProvider<>(Arrays.asList(new DocumentDetail()));
		ConfigurableFilterDataProvider<DocumentDetail, Void, SerializablePredicate<DocumentDetail>> filterDataProvider3 = dataProvider4
				.withConfigurableFilter();
		docDetailGrid.setDataProvider(filterDataProvider3);
		//

		HorizontalLayout itemsLayout = new HorizontalLayout();
		// itemsLayout.setSpacing(true);
		// itemsLayout.setMargin(true);
		itemsLayout.setSizeFull();
		itemsLayout.addComponents(docDetailGrid);
		addComponents(buttonLayout, filterLayout, headerLayout, headerLayout2, itemsButtonLayout, itemsLayout);

	}

	private void cantListener(String val) {
		System.out.println("cantListener" + val);

		subtotal = productSelected.getSalePrice() * Integer.parseInt(val);
		docDetailGrid.getDataProvider().refreshAll();
		System.out.println("subtotal" + subtotal);

	}

	private void fechaVencimiento(String val) {
		System.out.println("fechaVencimiento" + val);
		LocalDate lDate = dtPurchaseDate.getValue();
		Date fecha = Date.from(lDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
		Date fecha2 = sumarDiasAFecha(fecha, Integer.parseInt(val));
		LocalDate lDate2 = fecha2.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
		dtExpirationDate.setValue(lDate2);

	}

	@Transactional(rollbackFor = Exception.class)
	private void saveButtonAction(Document entity) {
		Document.Builder docBuilder = null;
		if (entity == null) {
			docBuilder = Document.builder();
		} else {
			docBuilder = Document.builder(entity);
		}
		DocumentType.Builder docTypeBuilder = DocumentType.builder();
		docTypeBuilder.id(BigInteger.valueOf(4));
		docTypeBuilder.code("VF").name("Factura de Venta");
		Date docDate = Date.from(dtPurchaseDate.getValue().atStartOfDay(ZoneId.systemDefault()).toInstant());

		Set<DocumentDetail> details = new HashSet<>();

		DataProvider<DocumentDetail, ?> prov = docDetailGrid.getDataProvider();
		System.out.println("prov=" + prov);

		List<DocumentDetail> list = docDetailGrid.getDataProvider().fetch(new Query<>()).collect(Collectors.toList());

		System.out.println("prov=" + list.size() + list.get(0).getDescription());

		Set<DocumentDetail> set = new HashSet<DocumentDetail>(list);

		entity = docBuilder.code(txtDocNumber.getValue()).documentType(docTypeBuilder.build()).person(personSelected)
				.documentDate(docDate).paymentMethod(cbPaymentMethod.getValue()).paymentType(cbPaymentType.getValue())
				.paymentTerm(txtPaymentTerm.getValue()).build();

		// save(documentBll, entity, "Factura guardado");

		// Guardar document
		try {

			documentBll.save(entity);

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

		System.out.println("entity=" + entity.getId());

		// Guardar Detail
		for (DocumentDetail detObj : list) {
			DocumentDetail.Builder detailBuilder = DocumentDetail.builder();
			// Document.Builder docuBuilder = Document.builder();
			// Document doc = docuBuilder.id(entity.getId()).build();
			Integer cant = Integer.parseInt(detObj.getDescription());
			Product prod = detObj.getProduct();
			Double subtotal = cant * prod.getSalePrice();
			DocumentDetail detail = detailBuilder.product(prod).document(entity).quantity(cant+"")
					.description(detObj.getDescription()).subtotal(subtotal).build();

			// Guardar inventario
			InventoryTransaction.Builder invBuilder = InventoryTransaction.builder();
			Document.Builder docuBuilder = Document.builder();
			Document doc = docuBuilder.id(entity.getId()).documentDate(entity.getDocumentDate()).build();
			InventoryTransactionType txType = InventoryTransactionType.SALIDA;
			ProductStockBk ps = stockBll.select(detObj.getProduct());
			int initialStock = ps != null ? ps.getStock() : 0;
			System.out.println("initialStock=" + initialStock);
			System.out.println("cant=" + cant);
			int finalStock = initialStock != 0 ? initialStock - cant : cant;
			System.out.println("finalStock=" + finalStock);
			InventoryTransaction inv = invBuilder.product(detObj.getProduct()).transactionType(txType)
					.initialStock(initialStock).quantity(cant).finalStock(finalStock).document(entity).build();

			// Actualizar stock
			ProductStockBk stock = stockBll.select(detObj.getProduct());			
			stock.setStock(finalStock);
			stock.setStockDate(new Date());

			try {

				docDetailBll.save(detail);

				inventoryBll.save(inv);
				stockBll.save(stock);

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
		/*
		 * // Guardar Inventario for (DocumentDetail detObj : list) {
		 * InventoryTransaction.Builder invBuilder = InventoryTransaction.builder();
		 * Document.Builder docuBuilder = Document.builder(); Document doc =
		 * docuBuilder.id(entity.getId()).documentDate(entity.getDocumentDate()).build()
		 * ; InventoryTransactionType txType = InventoryTransactionType.ENTRADA;
		 * InventoryTransaction inv =
		 * invBuilder.product(detObj.getProduct()).transactionType(txType).initialStock(
		 * 50) .quantity(detObj.getQuantity()).finalStock(50 -
		 * detObj.getQuantity()).document(entity).build(); try {
		 * 
		 * inventoryBll.save(inv);
		 * 
		 * // afterSave(caption); } catch (ModelValidationException ex) { log.error(ex);
		 * ViewHelper.showNotification(ex.getMessage(),
		 * Notification.Type.ERROR_MESSAGE); } catch (HibernateException ex) {
		 * log.error(ex); // bll.rollback(); ViewHelper.showNotification(
		 * "Los datos no pudieron ser salvados, contacte al administrador (3007200405)",
		 * Notification.Type.ERROR_MESSAGE); }
		 * 
		 * }
		 */
		ViewHelper.showNotification("Factura guardada con exito", Notification.Type.ERROR_MESSAGE);

	}

	private void searchProducts() {
		System.out.println("searchProducts");

		productSubWindow = new Window();

		productSubWindow.setModal(true);
		productSubWindow.center();
		productSubWindow.setWidth("75%");
		VerticalLayout subContent = new VerticalLayout();
		subContent.setMargin(true);
		productSubWindow.setContent(subContent);

		Button backButton = new Button("Cancelar", FontAwesome.BACKWARD);
		backButton.addStyleName(ValoTheme.BUTTON_DANGER);
		Button selectButton = new Button("Seleccionar", FontAwesome.CHECK);
		selectButton.addStyleName(ValoTheme.BUTTON_PRIMARY);
		Button productButton = new Button("Crear producto", FontAwesome.PLUS);
		productButton.addStyleName(ValoTheme.BUTTON_FRIENDLY);

		HorizontalLayout buttonLayout = new HorizontalLayout();
		buttonLayout.setSpacing(true);
		buttonLayout.setMargin(true);

		buttonLayout.addComponents(backButton, selectButton, productButton);

		subContent.addComponent(buttonLayout);

		// Grid de productos
		productGrid = new Grid<>();
		fillProductGridData();
		productGrid.addColumn(Product::getCode).setCaption("Código");
		productGrid.addColumn(Product::getName).setCaption("Nombre");
		productGrid.addColumn(Product::getDescription).setCaption("Descripción");
		productGrid.addColumn(Product::getPurchasePrice).setCaption("Precio de compra");
		productGrid.addColumn(Product::getSalePrice).setCaption("Precio de venta");
		productGrid.setSelectionMode(SelectionMode.SINGLE);
		productGrid.setSizeFull();

		productGrid.addSelectionListener(event -> {
			prodSet = event.getAllSelectedItems();
			// Notification.show(selected.size() + " items selected");
		});
		// Set<Product> prodSet = productGrid.getAllSelectedItems();

		selectButton.addClickListener(e -> selectProduct());

		HorizontalLayout gridLayout = new HorizontalLayout();
		gridLayout.setSpacing(true);
		gridLayout.setMargin(true);
		gridLayout.setSizeFull();
		gridLayout.addComponent(productGrid);
		subContent.addComponent(gridLayout);

		getUI().addWindow(productSubWindow);

	}

	private void selectProduct() {
		System.out.println("selectProduct=");

		for (Iterator<Product> iterator = prodSet.iterator(); iterator.hasNext();) {
			productSelected = iterator.next();
			// Notification.show( "Producto seleccionado: "+productSelected.getCode());
		}

		DocumentDetail docDetail = new DocumentDetail();
		DocumentDetail.Builder docDetailBuilder = DocumentDetail.builder();
		docDetail = docDetailBuilder.product(productSelected).archived(false).build();
		itemsList.add(docDetail);
		fillDocDetailGridData(itemsList);
		productSubWindow.close();
	}

	private void fillProductGridData() {

		Product prod = Product.builder().code("prod1").name("producto 1").description("desc prod").build();
		System.out.println("prod=" + prod.getCode());
		ListDataProvider<Product> dataProvider = new ListDataProvider<>(productBll.selectAll());
		filterProductDataProvider = dataProvider.withConfigurableFilter();
		productGrid.setDataProvider(filterProductDataProvider);
	}

	private void fillDocDetailGridData(List<DocumentDetail> detailList) {

		ListDataProvider<DocumentDetail> dataProvider = new ListDataProvider<>(detailList);
		filterDocDetailDataProvider = dataProvider.withConfigurableFilter();
		docDetailGrid.setDataProvider(filterDocDetailDataProvider);
	}

	private void searchSupplier(String supplierFilter) {
		System.out.println("searchSupplier");
		personSubWindow = new Window();

		personSubWindow.setModal(true);
		personSubWindow.center();
		personSubWindow.setWidth("75%");
		VerticalLayout subContent = new VerticalLayout();
		subContent.setMargin(true);
		personSubWindow.setContent(subContent);

		Button backButton = new Button("Cancelar", FontAwesome.BACKWARD);
		backButton.addStyleName(ValoTheme.BUTTON_DANGER);
		Button selectButton = new Button("Seleccionar", FontAwesome.CHECK);
		selectButton.addStyleName(ValoTheme.BUTTON_PRIMARY);
		Button productButton = new Button("Crear cliente", FontAwesome.PLUS);
		productButton.addStyleName(ValoTheme.BUTTON_FRIENDLY);

		HorizontalLayout buttonLayout = new HorizontalLayout();
		buttonLayout.setSpacing(true);
		buttonLayout.setMargin(true);

		buttonLayout.addComponents(backButton, selectButton, productButton);

		subContent.addComponent(buttonLayout);

		// Grid de personas
		personGrid = new Grid<>();
		fillPersonGridData();
		personGrid.addColumn(Person::getDocumentNumber).setCaption("Número identificación");
		personGrid.addColumn(Person::getName).setCaption("Nombres");
		personGrid.addColumn(Person::getLastName).setCaption("Apellidos");

		personGrid.setSelectionMode(SelectionMode.SINGLE);
		personGrid.setSizeFull();

		personGrid.addSelectionListener(event -> {
			personSet = event.getAllSelectedItems();
			// Notification.show(selected.size() + " items selected");
		});
		// Set<Product> prodSet = productGrid.getAllSelectedItems();

		selectButton.addClickListener(e -> selectPerson());

		HorizontalLayout gridLayout = new HorizontalLayout();
		gridLayout.setSpacing(true);
		gridLayout.setMargin(true);
		gridLayout.setSizeFull();
		gridLayout.addComponent(personGrid);
		subContent.addComponent(gridLayout);

		getUI().addWindow(personSubWindow);

	}

	/**
	 * Metodo encargado de llenar la grid de personas (Proveedores)
	 */
	private void fillPersonGridData() {

		// bll2.selectAll();
		ListDataProvider<Person> dataProvider = new ListDataProvider<>(personBll.selectAll());
		filterPersonDataProvider = dataProvider.withConfigurableFilter();
		personGrid.setDataProvider(filterPersonDataProvider);
	}

	private void selectPerson() {
		System.out.println("selectPerson=");

		for (Iterator<Person> iterator = personSet.iterator(); iterator.hasNext();) {
			personSelected = iterator.next();
			// Notification.show( "Producto seleccionado: "+productSelected.getCode());
		}

		txtSupplier.setValue(personSelected.getName() + " " + personSelected.getLastName());
		personSubWindow.close();

	}

	public static Date sumarDiasAFecha(Date fecha, int dias) {
		if (dias == 0)
			return fecha;
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(fecha);
		calendar.add(Calendar.DAY_OF_YEAR, dias);
		return calendar.getTime();
	}
}
