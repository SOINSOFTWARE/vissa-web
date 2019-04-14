package com.soinsoftware.vissa.web;

import java.io.IOException;

import org.apache.log4j.Logger;
import org.hibernate.HibernateException;
import org.springframework.transaction.annotation.Transactional;
import org.vaadin.ui.NumberField;

import com.soinsoftware.vissa.bll.UserBll;
import com.soinsoftware.vissa.exception.ModelValidationException;
import com.soinsoftware.vissa.model.User;
import com.soinsoftware.vissa.util.ViewHelper;
import com.vaadin.event.ShortcutAction;
import com.vaadin.event.ShortcutListener;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.server.FontAwesome;
import com.vaadin.ui.Button;
import com.vaadin.ui.FormLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Panel;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ValoTheme;

@SuppressWarnings("deprecation")
public class CashChangeLayout extends VerticalLayout implements View {
	/**
	 * 
	 */
	private static final long serialVersionUID = 5187486966314714738L;
	protected static final Logger log = Logger.getLogger(CashChangeLayout.class);

	// Bll
	private final UserBll userBll;

	// Components
	private NumberField txtTotalInvoice;
	private NumberField txtPaidAmount;
	private NumberField txtChange;
	private Double totalValueDocument;
	private InvoiceLayout invoiceLayout;
	private ReturnLayout returnLayout;

	private User user = null;

	public CashChangeLayout() throws IOException {
		super();
		userBll = UserBll.getInstance();
		buildComponents();

	}

	public CashChangeLayout(InvoiceLayout invoiceLayout, Double totalValueDocument) throws IOException {
		super();
		userBll = UserBll.getInstance();
		this.totalValueDocument = totalValueDocument;
		this.invoiceLayout = invoiceLayout;
		buildComponents();

	}

	public CashChangeLayout(ReturnLayout returnLayout, Double totalValueDocument) throws IOException {
		super();
		userBll = UserBll.getInstance();
		this.totalValueDocument = totalValueDocument;
		this.returnLayout = returnLayout;
		buildComponents();

	}

	@Override
	public void enter(ViewChangeEvent event) {

		View.super.enter(event);

		System.out.println("enter");
		VerticalLayout layout = ViewHelper.buildVerticalLayout(false, false);

		Label tittle = new Label("Cambio");
		tittle.addStyleName(ValoTheme.LABEL_H2);
		addComponent(tittle);

		// Panel de botones
		Panel buttonPanel = buildButtonPanel();

		// Panel de campos
		Panel editPanel = buildEditionPanel();

		layout.addComponents(buttonPanel, editPanel);
		addComponent(layout);
		this.setMargin(false);
		this.setSpacing(false);

	}

	public void buildComponents() {

		VerticalLayout layout = ViewHelper.buildVerticalLayout(false, false);

		Label tittle = new Label("Cambio");
		tittle.addStyleName(ValoTheme.LABEL_H3);
		addComponent(tittle);

		// Panel de botones
		Panel buttonPanel = buildButtonPanel();

		// Panel de campos
		Panel editPanel = buildEditionPanel();

		layout.addComponents(editPanel, buttonPanel);
		addComponent(layout);
		this.setMargin(false);
		this.setSpacing(false);

	}

	/**
	 * Construcción del panel de botones
	 * 
	 * @return
	 */

	private Panel buildButtonPanel() {

		HorizontalLayout layout = ViewHelper.buildHorizontalLayout(true, true);

		Button saveBtn = new Button("Guardar", FontAwesome.SAVE);
		saveBtn.addStyleName(ValoTheme.BUTTON_SMALL);
		saveBtn.addClickListener(e -> saveButtonAction());
		layout.addComponents(saveBtn);

		Button cancelBtn = new Button("Cancelar", FontAwesome.CLOSE);
		cancelBtn.addStyleName(ValoTheme.BUTTON_SMALL);
		cancelBtn.addClickListener(e -> closeWindow());
		layout.addComponents(cancelBtn);

		addComponent(layout);
		return ViewHelper.buildPanel(null, layout);
	}

	/*
	 * 
	 * Contrunscción de panel con campos de edición
	 */
	private Panel buildEditionPanel() {
		txtTotalInvoice = new NumberField("Total Factura");
		txtTotalInvoice.setReadOnly(true);
		txtTotalInvoice.setStyleName(ValoTheme.TEXTFIELD_TINY);
		txtTotalInvoice.setDecimalSeparator(',');

		txtPaidAmount = new NumberField("Valor pagado");
		txtPaidAmount.focus();
		txtPaidAmount.setStyleName(ValoTheme.TEXTFIELD_TINY);
		//txtPaidAmount.setDecimalSeparator(',');

		txtChange = new NumberField("Cambio");
		txtChange.setReadOnly(true);
		txtChange.setStyleName(ValoTheme.TEXTFIELD_TINY);
		txtChange.setDecimalSeparator(',');

		txtPaidAmount.addValueChangeListener(e -> setChangeValue());

		setFieldValues();
		FormLayout changeForm = ViewHelper.buildForm("Cambio", false, false);
		changeForm.addComponents(txtTotalInvoice, txtPaidAmount, txtChange);
		
		changeForm.addShortcutListener(new ShortcutListener("Enter for", ShortcutAction.KeyCode.ENTER, null) {
			private static final long serialVersionUID = 7441523733731956234L;
			@Override
			public void handleAction(Object sender, Object target) {
				try {
					if (((TextField) target).equals(txtPaidAmount)) {
						saveButtonAction();
					}
				} catch (Exception e) {
					log.error("[Form][ShortcutListener][handleAction][Exception] " + e.getMessage());
				}
			}
		});
		
		Panel userPanel = ViewHelper.buildPanel("", changeForm);

		return userPanel;

	}

	private void setFieldValues() {
		txtTotalInvoice.setValue(totalValueDocument);
		txtChange.setValue("0");
	}

	private void setChangeValue() {
		String strLog = "[setChangeValue]";
		try {
			Double totalValue = Double.valueOf(txtTotalInvoice.getValue());
			Double paidValue = Double.valueOf(txtPaidAmount.getValue());
			txtChange.setValue(String.valueOf(paidValue - totalValue));
		} catch (Exception e) {
			log.error(strLog + "[Exception]" + e.getMessage());
		}
	}

	@Transactional(rollbackFor = Exception.class)
	private void saveButtonAction() {
		String strLog = "[saveButtonAction]";
		log.info("saveButtonAction:" + user);

		// Guardar Usuario
		try {

			if (txtPaidAmount.getValue() != null && !txtPaidAmount.getValue().isEmpty()) {
				if (Double.parseDouble(txtPaidAmount.getValue()) < Double.parseDouble(txtTotalInvoice.getValue())) {
					ViewHelper.showNotification("El valor pagado debe ser mayor al total de la factura",
							Notification.Type.WARNING_MESSAGE);
				} else {
					if (invoiceLayout != null) {
						invoiceLayout.setPayValue(Double.valueOf(txtPaidAmount.getValue()));
						invoiceLayout.saveInvoice(invoiceLayout.getDocument());
					} else if (returnLayout != null) {
						returnLayout.setPayValue(Double.valueOf(txtPaidAmount.getValue()));
						returnLayout.saveInvoice(returnLayout.getDocument());
					}
				}

			} else {
				ViewHelper.showNotification("Ingrese el valor pagado", Notification.Type.WARNING_MESSAGE);
			}

		} catch (ModelValidationException ex) {
			log.error(strLog + "[ModelValidationException]" + ex);
			ViewHelper.showNotification(ex.getMessage(), Notification.Type.ERROR_MESSAGE);
		} catch (HibernateException ex) {
			log.error(strLog + "[HibernateException]" + ex);
			userBll.rollback();
			ViewHelper.showNotification("Los datos no pudieron ser salvados, contacte al administrador del sistema",
					Notification.Type.ERROR_MESSAGE);
		} catch (Exception ex) {
			log.error(strLog + "[Exception]" + ex);
			ViewHelper.showNotification("Se presentó un error, por favor contacte al adminisrador del sistema",
					Notification.Type.ERROR_MESSAGE);
		}
	}

	private void closeWindow() {
		getUI().close();
	}

}
