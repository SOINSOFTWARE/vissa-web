package com.soinsoftware.vissa.web;

import java.io.IOException;
import java.util.Arrays;
import java.util.Date;


import com.soinsoftware.vissa.bll.PersonBll;
import com.soinsoftware.vissa.bll.SupplierBll;
import com.soinsoftware.vissa.model.BankAccount;
import com.soinsoftware.vissa.model.BankAccountType;
import com.soinsoftware.vissa.model.DocumentType;
import com.soinsoftware.vissa.model.PaymentMethod;
import com.soinsoftware.vissa.model.PaymentType;
import com.soinsoftware.vissa.model.Person;
import com.soinsoftware.vissa.model.PersonType;
import com.soinsoftware.vissa.model.Supplier;
import com.vaadin.data.provider.ConfigurableFilterDataProvider;
import com.vaadin.data.provider.ListDataProvider;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.server.FontAwesome;
import com.vaadin.server.Page;
import com.vaadin.server.SerializablePredicate;
import com.vaadin.ui.Button;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.FormLayout;
import com.vaadin.ui.Grid;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Panel;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Grid.SelectionMode;
import com.vaadin.ui.themes.ValoTheme;

public class SupplierListLayout extends VerticalLayout implements View {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5076502522106126046L;

	private final PersonBll bll;

	private Grid<Person> grid;
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
	private ConfigurableFilterDataProvider<Person, Void, SerializablePredicate<Person>> filterDataProvider;

	public SupplierListLayout() throws IOException {
		super();
		bll = PersonBll.getInstance();
	}

	
	public void enter(ViewChangeEvent event) {
		
		View.super.enter(event);
		System.out.println("supplierListLayout");

		setMargin(true);
		Label tittle = new Label("Proveedores");
		tittle.addStyleName(ValoTheme.LABEL_H1);
		addComponent(tittle);

		Button backButton = new Button("Regresar", FontAwesome.BACKWARD);
		HorizontalLayout buttonLayout = new HorizontalLayout();
		buttonLayout.setSpacing(true);
		buttonLayout.setMargin(true);
		buttonLayout.addComponents(backButton);
		grid = new Grid<>();
		fillGridData();
		grid.addColumn(Person::getDocumentType).setCaption("Tipo de documento");
		grid.addColumn(Person::getDocumentNumber).setCaption("Documento");
		grid.addColumn(Person::getName).setCaption("Nombre");
		grid.setSelectionMode(SelectionMode.SINGLE);
		

		HorizontalLayout gridLayout = new HorizontalLayout();
		gridLayout.setSpacing(true);
		gridLayout.setMargin(true);
		gridLayout.addComponents(backButton);
		
		//Panel dataPanel = new Panel(grid);
	//	dataPanel.addStyleName("well");
		addComponents(buttonLayout,gridLayout);
	}

	
	private void fillGridData() {
		
		bll.selectAll();
		ListDataProvider<Person> dataProvider = new ListDataProvider<>(bll.selectAll());
		filterDataProvider = dataProvider.withConfigurableFilter();
		grid.setDataProvider(filterDataProvider);
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


		new Notification("Proveedor guardado", Notification.Type.TRAY_NOTIFICATION).show(Page.getCurrent());
	}
}
