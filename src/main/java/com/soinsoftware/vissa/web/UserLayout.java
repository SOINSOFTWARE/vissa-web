package com.soinsoftware.vissa.web;

import java.io.IOException;

import org.apache.log4j.Logger;
import org.hibernate.HibernateException;
import org.springframework.transaction.annotation.Transactional;

import com.soinsoftware.vissa.bll.UserBll;
import com.soinsoftware.vissa.exception.ModelValidationException;
import com.soinsoftware.vissa.model.User;
import com.soinsoftware.vissa.util.Commons;
import com.soinsoftware.vissa.util.ViewHelper;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.server.FontAwesome;
import com.vaadin.ui.Button;
import com.vaadin.ui.FormLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Panel;
import com.vaadin.ui.PasswordField;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ValoTheme;

@SuppressWarnings("deprecation")
public class UserLayout extends VerticalLayout implements View {
	/**
	 * 
	 */
	private static final long serialVersionUID = 5187486966314714738L;
	protected static final Logger log = Logger.getLogger(UserLayout.class);

	// Bll
	private final UserBll userBll;

	// Components
	private TextField txtPerson;
	private TextField txtLogin;
	private PasswordField txtPassword;

	private User user = null;

	public UserLayout() throws IOException {
		super();
		System.out.println("UserLayout");

		userBll = UserBll.getInstance();
	//	getUser();

	}

	private void getUser() {
		user = userBll.select(Commons.LOGIN);

	}

	@Override
	public void enter(ViewChangeEvent event) {

		View.super.enter(event);

		System.out.println("enter");
		VerticalLayout layout = ViewHelper.buildVerticalLayout(false, false);

		Label tittle = new Label("Cambiar contraseña");
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
		getUser();

	}

	/**
	 * Construcción del panel de botones
	 * 
	 * @return
	 */

	private Panel buildButtonPanel() {

		HorizontalLayout layout = ViewHelper.buildHorizontalLayout(true, true);

		Button saveBtn = new Button("Guardar", FontAwesome.SAVE);
		saveBtn.addStyleName("mystyle-btn");
		saveBtn.addClickListener(e -> saveButtonAction(null));
		layout.addComponents(saveBtn);

		Button cancelBtn = new Button("Cancelar", FontAwesome.SAVE);
		cancelBtn.addStyleName("mystyle-btn");
		// cancelBtn.addClickListener(e -> cleanButtonAction());
		layout.addComponents(cancelBtn);

		addComponent(layout);
		return ViewHelper.buildPanel(null, layout);
	}

	/*
	 * 
	 * Contrunscción de panel con campos de edición
	 */
	private Panel buildEditionPanel() {
		txtPerson = new TextField("Nombres y apellidos");
		txtPerson.setReadOnly(true);
		txtPerson.setStyleName(ValoTheme.TEXTFIELD_TINY);

		txtLogin = new TextField("Usuario");
		txtLogin.setReadOnly(true);
		txtPerson.setStyleName(ValoTheme.TEXTFIELD_TINY);

		txtPassword = new PasswordField("Contraseña");
		txtPassword.setStyleName(ValoTheme.TEXTFIELD_TINY);

		if (user != null) {
			txtPerson.setValue(user.getPerson().getName() + " " + user.getPerson().getLastName());
			txtLogin.setValue(user.getLogin());
			txtPassword.setValue(user.getPassword());
		}

		FormLayout userForm = ViewHelper.buildForm("Cambiar contraseña", false, false);
		userForm.addComponents(txtPerson, txtLogin, txtPassword);
		Panel userPanel = ViewHelper.buildPanel("Cambiar contra", userForm);

		return userPanel;

	}

	@Transactional(rollbackFor = Exception.class)
	private void saveButtonAction(User user) {
		User.Builder userBuilder = null;

		// Guardar Usuario
		try {

			if (user == null) {
				userBuilder = User.builder();
			} else {
				userBuilder = User.builder(user);
			}
			user = userBuilder.password(txtPassword.getValue()).build();

			userBll.save(user);

			ViewHelper.showNotification("Contraseña cambiada con éxito)", Notification.Type.WARNING_MESSAGE);

		} catch (ModelValidationException ex) {
			log.error(ex);
			ViewHelper.showNotification(ex.getMessage(), Notification.Type.ERROR_MESSAGE);
		} catch (HibernateException ex) {
			log.error(ex);
			userBll.rollback();
			ViewHelper.showNotification("Los datos no pudieron ser salvados, contacte al administrador (3007200405)",
					Notification.Type.ERROR_MESSAGE);
		} catch (Exception ex) {
			log.error(ex);
			ViewHelper.showNotification("Se presentó un error, por favor contacte al adminisrador",
					Notification.Type.ERROR_MESSAGE);
		}

	}

}
