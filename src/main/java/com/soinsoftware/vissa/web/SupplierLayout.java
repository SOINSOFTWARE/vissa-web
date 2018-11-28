package com.soinsoftware.vissa.web;

import com.soinsoftware.vissa.model.DocumentType;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.FormLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ValoTheme;

public class SupplierLayout extends VerticalLayout implements View{

	/**
	 * 
	 */
	private static final long serialVersionUID = 5076502522106126046L;
	
	
	@Override
	public void enter(ViewChangeEvent event) {
		
		View.super.enter(event);
		
		setMargin(true);
		Label tittle = new Label("Proveedores");
		tittle.addStyleName(ValoTheme.LABEL_H1);
		addComponent(tittle);
		
		//***********Components***************+
		///1. Informacion basica
		TextField txtDocumentId =  new TextField("Número de documento");		
		ComboBox<Object> cbDocumentType = new ComboBox<>("Tipo de documento"); 		
		TextField txtName =  new TextField("Nombres");
		TextField txtLastName =  new TextField("Apellidos");
		
		//2. Condiciones comerciales
		ComboBox<Object> cbPaymentType = new ComboBox<>("Forma de pago");
		TextField txtPaymentTerm = new TextField("Plazo"); 
		
		//3. Transferencia bancaria
		TextField txtAccountNumber = new TextField("Número de cuenta");
		ComboBox<Object> cbaaccountType = new ComboBox<>("Tipo de cuenta");
		ComboBox<Object> cbBank = new ComboBox<>("Entidad financiera");
		ComboBox<Object> cbAccountStatus = new ComboBox<>("Estado");
		
		//***************************
		
		
		Label section = new Label("Información básica");
		section.addStyleName(ValoTheme.LABEL_H2);
		section.addStyleName(ValoTheme.LABEL_COLORED);	
		
	/*	HorizontalLayout documentLayout = new HorizontalLayout();	
		
		documentLayout.addComponents(cbDocumentType,txtDocumentId);		
		HorizontalLayout nameLayout = new HorizontalLayout();		
		nameLayout.addComponents(txtName,txtLastName);
		
		VerticalLayout basicLayout = new VerticalLayout();
		basicLayout.setSpacing(true);
		basicLayout.setMargin(true);
		basicLayout.addComponents(section, documentLayout, nameLayout);
		
		
		Panel supplierPanel = new Panel(basicLayout);
		supplierPanel.addStyleName("well");
		*****/
		
		final FormLayout form = new FormLayout();
        form.setMargin(false);
        form.setWidth("800px");
        form.addStyleName(ValoTheme.FORMLAYOUT_LIGHT);        
		addComponent(form);	
		
		form.addComponents(section, cbDocumentType, txtDocumentId, txtName, txtLastName);
		
		section = new Label("Condiciones comerciales");
		section.addStyleName(ValoTheme.LABEL_H2);
		section.addStyleName(ValoTheme.LABEL_COLORED);	
		
		form.addComponents(section, cbPaymentType, txtPaymentTerm);
		
		section = new Label("Datos bancarios para transferencias");
		section.addStyleName(ValoTheme.LABEL_H2);
		section.addStyleName(ValoTheme.LABEL_COLORED);
		
		form.addComponents(section, txtAccountNumber, cbaaccountType, cbBank, cbAccountStatus);
	
		
		
		
		
		
	
		
		
		
		
	}

}
