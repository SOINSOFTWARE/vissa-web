package com.soinsoftware.vissa.web;

import java.io.IOException;
import java.util.List;

import org.apache.log4j.Logger;
import org.jsoup.helper.StringUtil;

import com.soinsoftware.vissa.bll.CompanyBll;
import com.soinsoftware.vissa.model.Company;
import com.soinsoftware.vissa.model.User;
import com.soinsoftware.vissa.util.Commons;
import com.soinsoftware.vissa.util.PermissionUtil;
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
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ValoTheme;

@SuppressWarnings("deprecation")
public class CompanyLayout extends VerticalLayout implements View {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1009491925177013905L;
	protected static final Logger log = Logger.getLogger(CompanyLayout.class);

	// Bll
	private final CompanyBll companyBll;

	// Components
	private TextField txtNit;
	private TextField txtName;
	private TextField txtInvoiceResolution;
	private TextField txtRegimeType;
	private TextField txtAddress;
	private TextField txtPhone;
	private TextField txtMobile;
	private TextField txtEmail;
	private TextField txtWebsite;

	// Entities
	private Company company;
	private PermissionUtil permissionUtil;
	private User user;

	public CompanyLayout() throws IOException {
		super();
		companyBll = CompanyBll.getInstance();

	}

	@Override
	public void enter(ViewChangeEvent event) {
		VerticalLayout layout = ViewHelper.buildVerticalLayout(false, false);

		this.user = getSession().getAttribute(User.class);
		this.permissionUtil = new PermissionUtil(user.getRole().getPermissions());

		// Titulo de la pagina
		Label tittle = new Label("Datos de la empresa");
		tittle.addStyleName(ValoTheme.LABEL_H3);

		addComponent(tittle);

		// Paneles
		Panel buttonPanel = buildButtonPanel();
		Panel editionPanel = buildEditionPanel();

		layout.addComponents(buttonPanel, editionPanel);
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
		if (permissionUtil.canEdit(Commons.MENU_NAME)) {
			Button newBtn = new Button("Limpiar", FontAwesome.ERASER);
			newBtn.addStyleName("mystyle-btn");
			newBtn.addClickListener(e -> cleanButtonAction());
			layout.addComponents(newBtn);

			Button saveBtn = new Button("Guardar", FontAwesome.SAVE);
			saveBtn.addStyleName("mystyle-btn");
			saveBtn.addClickListener(e -> saveButtonAction(company));
			layout.addComponents(saveBtn);

			Button editBtn = new Button("Cancelar", FontAwesome.CLOSE);
			editBtn.addStyleName("mystyle-btn");
			editBtn.addClickListener(e -> setFieldValues());
			layout.addComponents(editBtn);
		}

		addComponent(layout);
		return ViewHelper.buildPanel(null, layout);
	}

	/**
	 * Metodo para construir el panel con los campos de edición
	 * 
	 * @return
	 */
	private Panel buildEditionPanel() {
		VerticalLayout verticalLayout = ViewHelper.buildVerticalLayout(true, true);

		FormLayout companyForm = ViewHelper.buildForm(null, false, false);

		txtNit = new TextField("Nit");
		txtNit.setStyleName(ValoTheme.TEXTFIELD_TINY);
		txtNit.setRequiredIndicatorVisible(true);

		txtName = new TextField("Nombre");
		txtName.setStyleName(ValoTheme.TEXTFIELD_TINY);
		txtName.setRequiredIndicatorVisible(true);

		txtInvoiceResolution = new TextField("Resolución de facturación");
		txtInvoiceResolution.setStyleName(ValoTheme.TEXTFIELD_TINY);
		txtInvoiceResolution.setRequiredIndicatorVisible(true);

		txtRegimeType = new TextField("Régimen");
		txtRegimeType.setStyleName(ValoTheme.TEXTFIELD_TINY);
		txtRegimeType.setRequiredIndicatorVisible(true);

		txtAddress = new TextField("Dirección");
		txtAddress.setStyleName(ValoTheme.TEXTFIELD_TINY);
		txtAddress.setRequiredIndicatorVisible(true);

		txtPhone = new TextField("Teléfono");
		txtPhone.setStyleName(ValoTheme.TEXTFIELD_TINY);

		txtMobile = new TextField("Celular");
		txtMobile.setStyleName(ValoTheme.TEXTFIELD_TINY);
		txtMobile.setRequiredIndicatorVisible(true);

		txtEmail = new TextField("Correo electrónico");
		txtEmail.setStyleName(ValoTheme.TEXTFIELD_TINY);

		txtWebsite = new TextField("Sitio web");
		txtWebsite.setStyleName(ValoTheme.TEXTFIELD_TINY);

		companyForm.addComponents(txtNit, txtName, txtInvoiceResolution, txtRegimeType, txtAddress, txtPhone, txtMobile,
				txtEmail, txtWebsite);

		setFieldValues();
		verticalLayout.addComponent(companyForm);
		return ViewHelper.buildPanel(null, verticalLayout);
	}

	/**
	 * Metodo para cargar los datos de la companía en los respectivos campos
	 */
	private void setFieldValues() {
		String strLog = "[setFieldValues] ";

		try {
			// Por defecto se obtiene el primer registro
			List<Company> companies = companyBll.selectAll(false);
			if (companies != null && !companies.isEmpty()) {
				company = companyBll.selectAll(false).get(0);
				txtNit.setValue(company.getNit());
				txtName.setValue(company.getName());
				txtInvoiceResolution.setValue(company.getInvoiceResolution());
				txtRegimeType.setValue(company.getRegimeType());
				txtAddress.setValue(company.getAddress());
				txtPhone.setValue(company.getPhone());
				txtMobile.setValue(company.getMobile());
				txtEmail.setValue(company.getEmail());
				txtWebsite.setValue(company.getWebsite());
			}
		} catch (Exception e) {
			log.error(strLog + "[Exception] " + e.getMessage());
		}
	}

	/**
	 * Metodo para la acción del botón guardar
	 */
	private void saveButtonAction(Company entity) {
		String strLog = "[saveButtonAction] ";
		try {
			String message = validateRequiredFields();
			if (!message.isEmpty()) {
				ViewHelper.showNotification(message, Notification.Type.WARNING_MESSAGE);
			} else {
				saveCompany(company);
			}
		} catch (Exception e) {
			log.error(strLog + "[Exception] " + e.getMessage());
		}
	}

	/**
	 * Metodo para guardar los datos de la empresa
	 * 
	 * @param entity
	 */
	private void saveCompany(Company entity) {
		String strLog = "[saveCompany] ";
		try {

			Company.Builder companyBuilder = null;
			if (entity == null) {
				companyBuilder = Company.builder();
			} else {
				companyBuilder = Company.builder(entity);
			}

			entity = companyBuilder.nit(txtNit.getValue()).name(txtName.getValue())
					.invoiceResolution(txtInvoiceResolution.getValue()).regimeType(txtRegimeType.getValue())
					.address(txtAddress.getValue()).phone(txtPhone.getValue()).mobile(txtMobile.getValue())
					.email(txtEmail.getValue()).website(txtWebsite.getValue()).archived(false).build();

			companyBll.save(entity);

			ViewHelper.showNotification("Datos de la companía guardados", Notification.Type.WARNING_MESSAGE);

		} catch (Exception e) {
			log.error(strLog + "[Exception] " + e.getMessage());
			e.printStackTrace();
		}
	}

	/**
	 * Metodo para validar los campos obligatorios para guardar la empresa
	 * 
	 * @return
	 */
	private String validateRequiredFields() {
		String message = "";
		String character = "|";

		if (StringUtil.isBlank(txtNit.getValue())) {
			if (!message.isEmpty()) {
				message = message.concat(character);
			}
			message = message.concat("El nit es obligatorio");
		}
		if (StringUtil.isBlank(txtName.getValue())) {
			if (!message.isEmpty()) {
				message = message.concat(character);
			}
			message = message.concat("El nombre es obligatorio");
		}

		if (StringUtil.isBlank(txtInvoiceResolution.getValue())) {
			if (!message.isEmpty()) {
				message = message.concat(character);
			}
			message = message.concat("La resolución de factura es obligatoria");
		}

		if (StringUtil.isBlank(txtRegimeType.getValue())) {
			if (!message.isEmpty()) {
				message = message.concat(character);
			}
			message = message.concat("La regimén es obligatorio");
		}

		if (StringUtil.isBlank(txtAddress.getValue())) {
			if (!message.isEmpty()) {
				message = message.concat(character);
			}
			message = message.concat("La dirección es obligatoria");
		}

		if (StringUtil.isBlank(txtMobile.getValue())) {
			if (!message.isEmpty()) {
				message = message.concat(character);
			}
			message = message.concat("El número de celular es obligatorio");
		}

		return message;
	}

	/**
	 * Metodo para limpiar los campos del panel de edición
	 *
	 */
	private void cleanButtonAction() {
		txtNit.clear();
		txtName.clear();
		txtInvoiceResolution.clear();
		txtRegimeType.clear();
		txtAddress.clear();
		txtPhone.clear();
		txtMobile.clear();
		txtEmail.clear();
		txtWebsite.clear();
	}

}
