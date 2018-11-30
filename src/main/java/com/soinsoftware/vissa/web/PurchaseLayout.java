package com.soinsoftware.vissa.web;

import java.io.IOException;
import java.util.Arrays;
import java.util.Date;


import com.soinsoftware.vissa.bll.SupplierBll;
import com.soinsoftware.vissa.model.BankAccount;
import com.soinsoftware.vissa.model.BankAccountType;
import com.soinsoftware.vissa.model.DocumentType;
import com.soinsoftware.vissa.model.PaymentMethod;
import com.soinsoftware.vissa.model.PaymentType;
import com.soinsoftware.vissa.model.Person;
import com.soinsoftware.vissa.model.PersonType;
import com.soinsoftware.vissa.model.Supplier;
import com.vaadin.data.provider.ListDataProvider;
import com.vaadin.navigator.Navigator;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.server.FontAwesome;
import com.vaadin.ui.Button;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.ComponentContainer;
import com.vaadin.ui.DateField;
import com.vaadin.ui.FormLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ValoTheme;

public class PurchaseLayout extends VerticalLayout implements View {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5076502522106126046L;

	private final SupplierBll bll;

	private TextField txtBillNumber;
	private TextField txtSupplier;
	private DateField dtPurchaseDate;
	private ComboBox<PaymentType> cbPaymentType;
	private DateField dtExpirationDate;
	private ComboBox<BankAccount> cbCurrency;
	Supplier supplier = new Supplier();

	public PurchaseLayout() throws IOException {
		super();
		bll = SupplierBll.getInstance();
	}

	@Override
	public void enter(ViewChangeEvent event) {

		View.super.enter(event);

		setMargin(true);
		Label tittle = new Label("Factura de Compra");
		tittle.addStyleName(ValoTheme.LABEL_H1);
		addComponent(tittle);

		// ***********Components***************+
		/// 1. Informacion header
		txtBillNumber = new TextField("Número de factura");
		txtBillNumber.setEnabled(false);
		txtBillNumber.setValue("12313");
		txtSupplier = new TextField("Proveedor");

		Button searchSupplierButton = new Button("Buscar", FontAwesome.MAP_O);
		searchSupplierButton.addClickListener(e -> searchSupplier(""));
		
		dtPurchaseDate = new DateField("Fecha");

		cbPaymentType = new ComboBox<PaymentType>("Forma de pago");

		dtExpirationDate = new DateField("Fecha de Vencimiento");

		cbCurrency = new ComboBox<>("Moneda");

		// ***************************

		// ***
		Button saveButton = new Button("Guardar", FontAwesome.SAVE);
		saveButton.addStyleName("primary");

		saveButton.addClickListener(e -> save(supplier));

		HorizontalLayout buttonLayout = new HorizontalLayout();
		buttonLayout.setSpacing(true);
		buttonLayout.setMargin(true);
		buttonLayout.addComponents(saveButton);
		

		addComponent(buttonLayout);
		// Panel buttonPanel = new Panel(buttonLayout);
		// buttonPanel.addStyleName("well");
		// addComponent(buttonPanel);


		HorizontalLayout headerLayout = new HorizontalLayout();
		headerLayout.setSpacing(true);
		headerLayout.setMargin(true);
		headerLayout.addComponents(txtBillNumber, txtSupplier,searchSupplierButton, dtPurchaseDate, cbPaymentType, dtExpirationDate,
				cbCurrency);
		/*
		 * HorizontalLayout documentLayout = new HorizontalLayout();
		 * 
		 * documentLayout.addComponents(cbDocumentType,txtDocumentId); HorizontalLayout
		 * nameLayout = new HorizontalLayout();
		 * nameLayout.addComponents(txtName,txtLastName);
		 * 
		 * VerticalLayout basicLayout = new VerticalLayout();
		 * basicLayout.setSpacing(true); basicLayout.setMargin(true);
		 * basicLayout.addComponents(section, documentLayout, nameLayout);
		 * 
		 * 
		 * Panel supplierPanel = new Panel(basicLayout);
		 * supplierPanel.addStyleName("well");
		 *****/

		addComponent(headerLayout);
	}

	private void save(Supplier supplier) {
		System.out.println("save");

		bll.save(supplier);

	}

	private void searchSupplier(String supplierFilter) {
		System.out.println("searchSupplier");

		getUI().getNavigator().navigateTo("supplierList");

	}
}
