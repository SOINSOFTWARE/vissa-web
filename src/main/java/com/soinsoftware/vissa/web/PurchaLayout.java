package com.soinsoftware.vissa.web;

import java.io.IOException;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.Date;

import org.apache.log4j.Logger;

import com.soinsoftware.vissa.model.DocumentDetail;
import com.soinsoftware.vissa.model.DocumentType;
import com.soinsoftware.vissa.model.PaymentMethod;
import com.soinsoftware.vissa.model.PaymentType;
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
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ValoTheme;

@SuppressWarnings("deprecation")
public class PurchaLayout extends VerticalLayout implements View {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5187486966314714738L;
	protected static final Logger log = Logger.getLogger(PurchaLayout.class);

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

	public PurchaLayout() throws IOException {
		super();

	}

	@Override
	public void enter(ViewChangeEvent event) {

		View.super.enter(event);

		//setMargin(true);
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
		// searchSupplierButton.addClickListener(e -> searchSupplier(""));
		searchSupplierButton.addStyleName(ValoTheme.BUTTON_ICON_ONLY);

		dtPurchaseDate = new DateField("Fecha");
		dtPurchaseDate.setValue(new Date().toInstant().atZone(ZoneId.systemDefault()).toLocalDate());

		cbPaymentType = new ComboBox<PaymentType>("Forma de pago");
		cbPaymentType.setWidth("250px");

		cbDocumentType = new ComboBox<DocumentType>("Tipo de pedido");
		cbDocumentType.setWidth("250px");
		/*
		 * ListDataProvider<DocumentType> dataProviderD = new
		 * ListDataProvider<>(docTypeBll.selectAll());
		 * cbDocumentType.setDataProvider(dataProviderD);
		 * cbDocumentType.setItemCaptionGenerator(DocumentType::getName);
		 * cbDocumentType.addValueChangeListener(e -> {
		 * numDoc(cbDocumentType.getValue()); });
		 */

		/*
		 * ListDataProvider<PaymentType> dataProvider3 = new
		 * ListDataProvider<>(payTypeBll.selectAll());
		 * cbPaymentType.setDataProvider(dataProvider3);
		 * cbPaymentType.setItemCaptionGenerator(PaymentType::getName);
		 */

		txtPaymentTerm = new TextField("Plazo");

		txtPaymentTerm.addValueChangeListener(e -> {
			// fechaVencimiento(txtPaymentTerm.getValue());
		});

		dtExpirationDate = new DateField("Fecha de Vencimiento");
		dtExpirationDate.setEnabled(false);

		cbPaymentMethod = new ComboBox<PaymentMethod>("Método de pago");
		cbPaymentMethod.setWidth("250px");
		/*
		 * ListDataProvider<PaymentMethod> payMethoddataProvider = new
		 * ListDataProvider<>(payMethodBll.selectAll());
		 * cbPaymentMethod.setDataProvider(payMethoddataProvider);
		 * cbPaymentMethod.setItemCaptionGenerator(PaymentMethod::getName);
		 */

		HorizontalLayout headerLayout1 = new HorizontalLayout();

		headerLayout1.addComponents(cbDocumentType, txtDocNumber, txtRefSupplier, txtSupplier, searchSupplierButton,
				dtPurchaseDate);
		headerLayout1.setComponentAlignment(searchSupplierButton, Alignment.BOTTOM_CENTER);

		HorizontalLayout headerLayout2 = new HorizontalLayout();
		headerLayout2.addComponents(cbPaymentType, txtPaymentTerm, dtExpirationDate, cbPaymentMethod);

		
		layout.addComponents(headerLayout1, headerLayout2);

		return ViewHelper.buildPanel(null, layout);
	}
	
	/**
	 * Construcción del panel  con grid de items
	 * 
	 * @return
	 */
	private Panel buildDetailPanel() {
		VerticalLayout layout = ViewHelper.buildVerticalLayout(true, true);

		
		Button searchProductBt = new Button("Buscar productos", FontAwesome.PLUS);
		searchProductBt.addStyleName(ValoTheme.BUTTON_SMALL);
		//searchProduct.addClickListener(e -> searchProducts());
		
		
		docDetailGrid = new Grid<>("Productos");
		docDetailGrid.setSizeFull();
		
		
		//columns
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
			//cantListener(taskField.getValue());
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
		
		HorizontalLayout itemsLayout = new HorizontalLayout();
		// itemsLayout.setSpacing(true);
		// itemsLayout.setMargin(true);
		itemsLayout.setSizeFull();
		itemsLayout.addComponents(docDetailGrid);
		
		layout.addComponents(searchProductBt, docDetailGrid);

		return ViewHelper.buildPanel(null, layout);
	}

}
