package com.soinsoftware.vissa.web;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.swing.DefaultCellEditor;
import javax.swing.JTextField;

import com.soinsoftware.vissa.bll.PaymentMethodBll;
import com.soinsoftware.vissa.bll.PaymentTypeBll;
import com.soinsoftware.vissa.bll.PersonBll;
import com.soinsoftware.vissa.bll.ProductBll;
import com.soinsoftware.vissa.bll.SupplierBll;
import com.soinsoftware.vissa.model.BankAccount;
import com.soinsoftware.vissa.model.DocumentDetail;
import com.soinsoftware.vissa.model.PaymentMethod;
import com.soinsoftware.vissa.model.PaymentType;
import com.soinsoftware.vissa.model.Person;
import com.soinsoftware.vissa.model.Product;
import com.soinsoftware.vissa.model.Supplier;
import com.vaadin.data.provider.ConfigurableFilterDataProvider;
import com.vaadin.data.provider.ListDataProvider;
import com.vaadin.icons.VaadinIcons;
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
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.themes.ValoTheme;

public class PurchaseLayout extends VerticalLayout implements View {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5076502522106126046L;

	private final SupplierBll supplierBll;
	private final PersonBll personBll;
	private final ProductBll productBll;
	private final PaymentMethodBll payMethodBll;
	private final PaymentTypeBll payTypeBll;

	private TextField txtDocNumber;
	private TextField txtRefSupplier;
	private TextField txtSupplier;
	private DateField dtPurchaseDate;
	private ComboBox<PaymentType> cbPaymentType;
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
	List<DocumentDetail> itemsList = null;

	public PurchaseLayout() throws IOException {
		super();
		supplierBll = SupplierBll.getInstance();
		personBll = PersonBll.getInstance();
		productBll = ProductBll.getInstance();
		payMethodBll = PaymentMethodBll.getInstance();
		payTypeBll = PaymentTypeBll.getInstance();
		itemsList  = new ArrayList<>(); 
	}

	@Override
	public void enter(ViewChangeEvent event) {

		View.super.enter(event);

		setMargin(true);
		Label tittle = new Label("Pedido");
		tittle.addStyleName(ValoTheme.LABEL_H1);
		addComponent(tittle);

		// ***********Components***************+
		/// 1. Informacion encabezado facuta
		txtDocNumber = new TextField("Número de factura");
		txtDocNumber.setEnabled(false);
		txtDocNumber.setValue("12313");
		txtRefSupplier = new TextField("Referencia proveedor");
		txtSupplier = new TextField("Proveedor");
		txtSupplier.setWidth("250px");

		Button searchSupplierButton = new Button("Buscar proveedor", FontAwesome.SEARCH);
		searchSupplierButton.addClickListener(e -> searchSupplier(""));
		searchSupplierButton.addStyleName(ValoTheme.BUTTON_ICON_ONLY);

		dtPurchaseDate = new DateField("Fecha");

		cbPaymentType = new ComboBox<PaymentType>("Forma de pago");
		cbPaymentType.setWidth("250px");
		ListDataProvider<PaymentType> dataProvider3 = new ListDataProvider<>(
				payTypeBll.selectAll());
		cbPaymentType.setDataProvider(dataProvider3);
		cbPaymentType.setItemCaptionGenerator(PaymentType::getName);

		txtPaymentTerm = new TextField("Plazo");

		dtExpirationDate = new DateField("Fecha de Vencimiento");

		cbPaymentMethod = new ComboBox<PaymentMethod>("Método de pago");
		cbPaymentMethod.setWidth("250px");
		ListDataProvider<PaymentMethod> payMethoddataProvider = new ListDataProvider<>(payMethodBll.selectAll());
		cbPaymentMethod.setDataProvider(payMethoddataProvider);
		cbPaymentMethod.setItemCaptionGenerator(PaymentMethod::getName);

		cbCurrency = new ComboBox<>("Moneda");

		HorizontalLayout headerLayout = new HorizontalLayout();
	
		headerLayout.addComponents(txtDocNumber, txtRefSupplier, txtSupplier, searchSupplierButton, dtPurchaseDate);
		
		
		HorizontalLayout headerLayout2 = new HorizontalLayout();
		headerLayout2.addComponents(
				cbPaymentType, txtPaymentTerm,  dtExpirationDate, cbPaymentMethod);

		headerLayout.setComponentAlignment(searchSupplierButton, Alignment.BOTTOM_CENTER);

		// ***************************

		// ***Barra de botones

		Button saveButton = new Button("Guardar", FontAwesome.SAVE);
		saveButton.addStyleName(ValoTheme.BUTTON_PRIMARY);
		saveButton.addClickListener(e -> save(supplier));

		Button editButton = new Button("Edit", FontAwesome.EDIT);
		editButton.addStyleName(ValoTheme.BUTTON_FRIENDLY);
		editButton.addClickListener(e -> save(supplier));

		Button deleteButton = new Button("Delete", FontAwesome.ERASER);
		deleteButton.addStyleName(ValoTheme.BUTTON_DANGER);
		deleteButton.addClickListener(e -> save(supplier));
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

		// Tabla de items

		docDetailGrid = new Grid<>("Productos");
		docDetailGrid.setSizeFull();

		DocumentDetail docDetail = new DocumentDetail();
		fillDocDetailGridData(itemsList);
		// grid.setItems(createItems(50));
		//

		docDetailGrid.addColumn(documentDetail -> {
			if(documentDetail.getProduct() != null) {
				return documentDetail.getProduct().getCode();
			} else {
				return null;
			}
		}).setCaption("Código");
		docDetailGrid.addColumn(documentDetail -> {
			if(documentDetail.getProduct() != null) {
				return documentDetail.getProduct().getName();
			} else {
				return null;
			}
		}).setCaption("Nombre");
		docDetailGrid.addColumn(documentDetail -> {
			if(documentDetail.getProduct() != null) {
				return documentDetail.getProduct().getMeasurementUnit();
			} else {
				return null;
			}
		}).setCaption("Unidad de medida");
		docDetailGrid.addColumn(documentDetail -> {
			if(documentDetail.getProduct() != null) {
				return documentDetail.getProduct().getSalePrice();
			} else {
				return null;
			}
		}).setCaption("Precio");
		
		docDetailGrid.addColumn(DocumentDetail::getDescription).setCaption("Descripcion");
		
		JTextField field = new JTextField();
		 DefaultCellEditor editor1 = new DefaultCellEditor(field);
	        editor1.setClickCountToStart(1);
	        TextField tf = new TextField();
	     
	//	docDetailGrid.addColumn(DocumentDetail::getQuantity).setCaption("Cantidad").setEditorComponent(tf,DocumentDetail::setQuantity);
	
		
		
	        Button button = new Button(VaadinIcons.CLOSE);
	        button.addStyleName(ValoTheme.BUTTON_SMALL);
	        
	      //.addComponentColumn(this::button);

		
		docDetailGrid.addColumn(DocumentDetail::getSubtotal).setCaption("Subtotal");

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

	private void save(Supplier supplier) {
		System.out.println("save");

		supplierBll.save(supplier);

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

		DocumentDetail  docDetail = new DocumentDetail();
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
		Button productButton = new Button("Crear proveedor", FontAwesome.PLUS);
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
		
		txtSupplier.setValue (personSelected.getName() +  " " + personSelected.getLastName());
		personSubWindow.close();

		
	}
}
