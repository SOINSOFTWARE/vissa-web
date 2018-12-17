package com.soinsoftware.vissa.web;

import java.io.IOException;

import org.apache.log4j.Logger;

import com.soinsoftware.vissa.util.ViewHelper;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.server.FontAwesome;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ValoTheme;


public class PurchaLayout extends VerticalLayout implements View {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5187486966314714738L;
	protected static final Logger log = Logger.getLogger(PurchaLayout.class);
	
	

	public PurchaLayout() throws IOException {
		super();
		
		

	}

	@Override
	public void enter(ViewChangeEvent event) {

		View.super.enter(event);
		
		setMargin(true);
		Label tittle = new Label("Pedido");
		tittle.addStyleName(ValoTheme.LABEL_H1);
		addComponent(tittle);
		
		VerticalLayout layout = ViewHelper.buildVerticalLayout(false, false);
		
		//Panel de botones
		Panel buttonPanel = buildButtonPanel();
		
		layout.addComponents(buttonPanel);
	}
	
	private Panel buildButtonPanel() {
		HorizontalLayout layout = ViewHelper.buildHorizontalLayout(true, true);
		Button newBtn = new Button("Guardar", FontAwesome.SAVE);
		newBtn.addStyleName(ValoTheme.BUTTON_PRIMARY);
	//	newBtn.addClickListener(e -> saveButtonAction(null));
		
		Button saveBtn = new Button("Guardar", FontAwesome.SAVE);
		saveBtn.addStyleName(ValoTheme.BUTTON_PRIMARY);
	//	saveBtn.addClickListener(e -> saveButtonAction(null));

		Button editBtn = new Button("Edit", FontAwesome.EDIT);
		editBtn.addStyleName(ValoTheme.BUTTON_FRIENDLY);
	//	editBtn.addClickListener(e -> saveButtonAction(null));

		Button deleteBtn = new Button("Delete", FontAwesome.ERASER);
		deleteBtn.addStyleName(ValoTheme.BUTTON_DANGER);
	//	deleteBtn.addClickListener(e -> saveButtonAction(document));
		
		layout.addComponents(newBtn, saveBtn, editBtn, deleteBtn);
		return ViewHelper.buildPanel(null, layout);
	}
}
