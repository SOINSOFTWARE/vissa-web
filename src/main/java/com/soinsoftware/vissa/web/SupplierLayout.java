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
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.server.FontAwesome;
import com.vaadin.server.Page;
import com.vaadin.ui.Button;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.FormLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Panel;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ValoTheme;

public class SupplierLayout extends VerticalLayout implements View {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5076502522106126046L;

	private final SupplierBll bll;

	private TextField txtDocumentId;
	private ComboBox<DocumentType> cbDocumentType;
	private TextField txtName;
	private TextField txtLastName;
	private ComboBox<PaymentType> cbPaymentType;
	private ComboBox<PaymentMethod	> cbPaymentMethod;
	private TextField txtPaymentTerm;
	private TextField txtAccountNumber;
	private ComboBox<BankAccountType> cbaAccountType;
	private ComboBox<BankAccount> cbBank;
	private ComboBox<Object> cbAccountStatus;
	Supplier supplier = new Supplier();

	public SupplierLayout() throws IOException {
		super();
		bll = SupplierBll.getInstance();
	}

	@Override
	public void enter(ViewChangeEvent event) {

		View.super.enter(event);

		setMargin(true);
		Label tittle = new Label("Proveedores");
		tittle.addStyleName(ValoTheme.LABEL_H1);
		addComponent(tittle);

		// ***********Components***************+
		/// 1. Informacion basica
		txtDocumentId = new TextField("Número de documento");
		cbDocumentType = new ComboBox<>("Tipo de documento");
		cbDocumentType.setDescription("Tipo");
		cbDocumentType.setEmptySelectionAllowed(false);
		
		ListDataProvider<DocumentType> dataProvider = new ListDataProvider<>(
				Arrays.asList(DocumentType.CC, DocumentType.CE, DocumentType.NIT, DocumentType.PASSPORT));
		cbDocumentType.setDataProvider(dataProvider);
		cbDocumentType.setItemCaptionGenerator(DocumentType::getDisplay);
		
		txtName = new TextField("Nombres");
		txtLastName = new TextField("Apellidos");

		// 2. Condiciones comerciales
		cbPaymentType = new ComboBox<>("Tipo de pago");
		ListDataProvider<PaymentType> dataProvider3 = new ListDataProvider<>(
				Arrays.asList(PaymentType.PRE_PAID, PaymentType.POST_PAID,PaymentType.POST_PAID));
		cbPaymentType.setDataProvider(dataProvider3);
		cbPaymentType.setItemCaptionGenerator(PaymentType::getDisplay);
		
		cbPaymentMethod = new ComboBox<>("Forma de pago");
		ListDataProvider<PaymentMethod> dataProvider4 = new ListDataProvider<>(
				Arrays.asList(PaymentMethod.CASH, PaymentMethod.BANK_TRANSFER,PaymentMethod.BANK_DEPOSIT));
		cbPaymentMethod.setDataProvider(dataProvider4);
		cbPaymentMethod.setItemCaptionGenerator(PaymentMethod::getDisplay);
		
		txtPaymentTerm = new TextField("Plazo");

		// 3. Transferencia bancaria
		txtAccountNumber = new TextField("Número de cuenta");
		cbaAccountType = new ComboBox<>("Tipo de cuenta");
		ListDataProvider<BankAccountType> dataProvider2 = new ListDataProvider<>(
				Arrays.asList(BankAccountType.SAVING, BankAccountType.COMMON));
		cbaAccountType.setDataProvider(dataProvider2);
		cbaAccountType.setItemCaptionGenerator(BankAccountType::getDisplay);
		cbBank = new ComboBox<>("Entidad financiera");
		cbAccountStatus = new ComboBox<>("Estado");

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

		Label section = new Label("Información básica");
		section.addStyleName(ValoTheme.LABEL_H2);
		section.addStyleName(ValoTheme.LABEL_COLORED);

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

		final FormLayout form = new FormLayout();
		form.setMargin(false);
		form.setWidth("800px");
		form.addStyleName(ValoTheme.FORMLAYOUT_LIGHT);

		form.addComponents(section, cbDocumentType, txtDocumentId, txtName, txtLastName);

		section = new Label("Condiciones comerciales");
		section.addStyleName(ValoTheme.LABEL_H2);
		section.addStyleName(ValoTheme.LABEL_COLORED);

		form.addComponents(section, cbPaymentType, cbPaymentMethod ,txtPaymentTerm);

		section = new Label("Datos bancarios");
		section.addStyleName(ValoTheme.LABEL_H2);
		section.addStyleName(ValoTheme.LABEL_COLORED);

		form.addComponents(section, txtAccountNumber, cbaAccountType, cbBank, cbAccountStatus);

		addComponent(form);
	}

	private void save(Supplier supplier) {
		System.out.println("save");

		// ***
		DocumentType docType = cbDocumentType.getSelectedItem().get();
		System.out.println("docType=" + docType);

		String docId = txtDocumentId.getValue();
		String name = txtName.getValue();
		String lastName = txtLastName.getValue();

		Person person = new Person();

		person = Person.builder().documentType(docType).documentNumber(docId).name(name).lastName(lastName).type(PersonType.SUPPLIER).build();
		supplier = Supplier.builder().person(person).build();
		bll.save(supplier);

		new Notification("Proveedor guardado", Notification.Type.TRAY_NOTIFICATION).show(Page.getCurrent());
	}
}
