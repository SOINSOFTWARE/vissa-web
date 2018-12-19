package com.soinsoftware.vissa.web;

import java.io.IOException;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
import java.util.Set;

import org.apache.log4j.Logger;

import com.soinsoftware.vissa.bll.DocumentBll;
import com.soinsoftware.vissa.bll.DocumentDetailBll;
import com.soinsoftware.vissa.bll.DocumentTypeBll;
import com.soinsoftware.vissa.bll.InventoryTransactionBll;
import com.soinsoftware.vissa.bll.PaymentMethodBll;
import com.soinsoftware.vissa.bll.PaymentTypeBll;
import com.soinsoftware.vissa.bll.PersonBll;
import com.soinsoftware.vissa.bll.ProductBll;
import com.soinsoftware.vissa.bll.ProductStockBll;
import com.soinsoftware.vissa.model.DocumentDetail;
import com.soinsoftware.vissa.model.DocumentType;
import com.soinsoftware.vissa.model.PaymentMethod;
import com.soinsoftware.vissa.model.PaymentType;
import com.soinsoftware.vissa.model.Person;
import com.soinsoftware.vissa.model.PurchaseBean;
import com.soinsoftware.vissa.util.ViewHelper;
import com.vaadin.data.Binder;
import com.vaadin.data.Binder.Binding;
import com.vaadin.data.provider.ConfigurableFilterDataProvider;
import com.vaadin.data.provider.ListDataProvider;
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
public class PurchaLayout extends VerticalLayout implements View {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5187486966314714738L;
	protected static final Logger log = Logger.getLogger(PurchaLayout.class);

	// Bll
	private final PersonBll personBll;
	private final ProductBll productBll;
	private final PaymentMethodBll payMethodBll;
	private final PaymentTypeBll payTypeBll;
	private final DocumentBll documentBll;
	private final DocumentDetailBll docDetailBll;
	private final InventoryTransactionBll inventoryBll;
	private final DocumentTypeBll docTypeBll;
	private final ProductStockBll stockBll;

	PurchaseBean purchaseBean;
	// Components
	private TextField txtDocNumFilter;
	private TextField txtDocNumber;
	private TextField txtRefSupplier;
	private TextField txtSupplier;
	private DateField dtPurchaseDate;
	private ComboBox<PaymentType> cbPaymentType;
	private ComboBox<DocumentType> cbDocumentType;
	private TextField txtPaymentTerm;
	private DateField dtExpirationDate;
	private ComboBox<PaymentMethod> cbPaymentMethod;
	private Grid<DocumentDetail> docDetailGrid;
	private Window personSubwindow;
	private Set<Person> personSet = null;
	private Person personSelected = null;

	public PurchaLayout() throws IOException {
		super();
		personBll = PersonBll.getInstance();
		productBll = ProductBll.getInstance();
		payMethodBll = PaymentMethodBll.getInstance();
		payTypeBll = PaymentTypeBll.getInstance();
		documentBll = DocumentBll.getInstance();
		docDetailBll = DocumentDetailBll.getInstance();
		inventoryBll = InventoryTransactionBll.getInstance();
		docTypeBll = DocumentTypeBll.getInstance();
		stockBll = ProductStockBll.getInstance();
		purchaseBean = new PurchaseBean();

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
		Panel headerPanel = buildInvoiceHeaderPanel();

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
		System.out.println("buildButtonPanel");
		HorizontalLayout layout = ViewHelper.buildHorizontalLayout(true, true);
		Button newBtn = new Button("Nuevo", FontAwesome.SAVE);
		newBtn.addStyleName(ValoTheme.BUTTON_PRIMARY);
		// newBtn.addClickListener(e -> saveButtonAction(null));

		Button saveBtn = new Button("Guardar", FontAwesome.SAVE);
		saveBtn.addStyleName(ValoTheme.BUTTON_FRIENDLY);
		// saveBtn.addClickListener(e -> saveButtonAction(null));

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
	private Panel buildInvoiceHeaderPanel() {
		VerticalLayout layout = ViewHelper.buildVerticalLayout(true, true);

		txtDocNumber = new TextField("Número de factura");
		txtDocNumber.setEnabled(false);

		txtRefSupplier = new TextField("Referencia proveedor");

		txtSupplier = new TextField("Proveedor");
		txtSupplier.setWidth("250px");

		Button searchSupplierButton = new Button("Buscar proveedor", FontAwesome.SEARCH);
		searchSupplierButton.addClickListener(e -> buildSearchPersonWindow(txtSupplier.getValue()));
		searchSupplierButton.addStyleName(ValoTheme.BUTTON_ICON_ONLY);

		dtPurchaseDate = new DateField("Fecha");
		dtPurchaseDate.setValue(new Date().toInstant().atZone(ZoneId.systemDefault()).toLocalDate());

		cbPaymentType = new ComboBox<PaymentType>("Forma de pago");
		cbPaymentType.setWidth("250px");
		ListDataProvider<PaymentType> payTypeDataProv = new ListDataProvider<>(payTypeBll.selectAll());
		cbPaymentType.setDataProvider(payTypeDataProv);
		cbPaymentType.setItemCaptionGenerator(PaymentType::getName);

		cbDocumentType = new ComboBox<DocumentType>("Tipo de pedido");
		cbDocumentType.setWidth("250px");
		ListDataProvider<DocumentType> docTypeDataProv = new ListDataProvider<>(docTypeBll.selectAll());
		cbDocumentType.setDataProvider(docTypeDataProv);
		cbDocumentType.setItemCaptionGenerator(DocumentType::getName);
		// cbDocumentType.addValueChangeListener(e -> {
		// numDoc(cbDocumentType.getValue()); });

		txtPaymentTerm = new TextField("Plazo");

		txtPaymentTerm.addValueChangeListener(e -> {
			// fechaVencimiento(txtPaymentTerm.getValue());
		});

		dtExpirationDate = new DateField("Fecha de Vencimiento");
		dtExpirationDate.setEnabled(false);

		cbPaymentMethod = new ComboBox<PaymentMethod>("Método de pago");
		cbPaymentMethod.setWidth("250px");

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
		// searchProduct.addClickListener(e -> searchProducts());

		docDetailGrid = new Grid<>("Productos");
		docDetailGrid.setSizeFull();

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
				return documentDetail.getProduct().getMeasurementUnit();
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

		taskField.addValueChangeListener(e -> {
			// cantListener(taskField.getValue());
		});

		// .addComponentColumn(this::button);

		// docDetailGrid.addColumn(DocumentDetail::getSubtotal).setCaption("Subtotal");

		Double subtotal = null;
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
		itemsLayout.addComponents(docDetailGrid);

		layout.addComponents(searchProductBt, itemsLayout);

		return ViewHelper.buildPanel(null, layout);
	}

	/**
	 * Metodo para construir la venta modal de personas
	 * 
	 * @param personFiltter
	 */
	private void buildSearchPersonWindow(String personFiltter) {

		personSubwindow = ViewHelper.buildSubwindow();

		VerticalLayout subContent = ViewHelper.buildVerticalLayout(false, true);

		Button backBtn = new Button("Cancelar", FontAwesome.BACKWARD);
		backBtn.addStyleName(ValoTheme.BUTTON_DANGER);
		Button selectBtn = new Button("Seleccionar", FontAwesome.CHECK);
		selectBtn.addStyleName(ValoTheme.BUTTON_PRIMARY);
		Button newBtn = new Button("Crear proveedor", FontAwesome.PLUS);
		newBtn.addStyleName(ValoTheme.BUTTON_FRIENDLY);

		HorizontalLayout buttonLayout = ViewHelper.buildHorizontalLayout(false, false);
		buttonLayout.addComponents(backBtn, selectBtn, newBtn);

		// Grid de personas
		Grid<Person> personGrid = new Grid<>();		
		personGrid.setDataProvider(purchaseBean.listPersons());
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

		subContent.addComponents(buttonLayout, personGrid);

		personSubwindow.setContent(subContent);
		getUI().addWindow(personSubwindow);

	}

	/**
	 * Método para seleccionar proveedor o cliente
	 */
	private void selectPerson() {
		Person personSelected = null;
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
	 * 
	 */
	private void createPerson() {

	}

}
