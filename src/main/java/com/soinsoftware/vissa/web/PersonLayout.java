package com.soinsoftware.vissa.web;

import static com.soinsoftware.vissa.web.VissaUI.KEY_PERSON;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.Arrays;
import java.util.Set;

import org.apache.log4j.Logger;
import org.hibernate.HibernateException;
import org.springframework.transaction.annotation.Transactional;

import com.soinsoftware.vissa.bll.BankBll;
import com.soinsoftware.vissa.bll.CityBll;
import com.soinsoftware.vissa.bll.CountryBll;
import com.soinsoftware.vissa.bll.PaymentMethodBll;
import com.soinsoftware.vissa.bll.PaymentTypeBll;
import com.soinsoftware.vissa.bll.PersonBll;
import com.soinsoftware.vissa.bll.RoleBll;
import com.soinsoftware.vissa.bll.StateBll;
import com.soinsoftware.vissa.bll.SupplierBll;
import com.soinsoftware.vissa.bll.UserBll;
import com.soinsoftware.vissa.exception.ModelValidationException;
import com.soinsoftware.vissa.model.Bank;
import com.soinsoftware.vissa.model.BankAccount;
import com.soinsoftware.vissa.model.BankAccountStatus;
import com.soinsoftware.vissa.model.BankAccountType;
import com.soinsoftware.vissa.model.City;
import com.soinsoftware.vissa.model.Country;
import com.soinsoftware.vissa.model.DocumentIdType;
import com.soinsoftware.vissa.model.PaymentMethod;
import com.soinsoftware.vissa.model.PaymentType;
import com.soinsoftware.vissa.model.Person;
import com.soinsoftware.vissa.model.PersonType;
import com.soinsoftware.vissa.model.Role;
import com.soinsoftware.vissa.model.State;
import com.soinsoftware.vissa.model.Supplier;
import com.soinsoftware.vissa.model.User;
import com.soinsoftware.vissa.util.Commons;
import com.soinsoftware.vissa.util.ViewHelper;
import com.vaadin.data.provider.ConfigurableFilterDataProvider;
import com.vaadin.data.provider.ListDataProvider;
import com.vaadin.server.SerializablePredicate;
import com.vaadin.ui.AbstractOrderedLayout;
import com.vaadin.ui.Button;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.FormLayout;
import com.vaadin.ui.Grid;
import com.vaadin.ui.Grid.SelectionMode;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Panel;
import com.vaadin.ui.PasswordField;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ValoTheme;

@SuppressWarnings("unchecked")
public class PersonLayout extends AbstractEditableLayout<Person> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5076502522106126046L;

	protected static final Logger log = Logger.getLogger(PersonLayout.class);

	private final SupplierBll supplierBll;
	private final PersonBll personBll;
	private final UserBll userBll;
	private final PaymentMethodBll payMethodBll;
	private final PaymentTypeBll payTypeBll;
	private final CountryBll countryBll;
	private final StateBll stateBll;
	private final CityBll cityBll;
	private final BankBll bankBll;
	private final RoleBll roleBll;

	private TextField txFilterByName;
	private TextField txFilterByCode;
	private TextField txFilterByLastName;
	public Grid<Person> grid;

	private TextField txtDocumentId;
	private ComboBox<DocumentIdType> cbDocumentType;
	private TextField txtName;
	private TextField txtLastName;
	private TextField txtContactName;
	private TextField txtAddress;
	private ComboBox<Country> cbCountry;
	private ComboBox<State> cbState;
	private ComboBox<City> cbCity;
	private TextField txtNeighborhood;
	private TextField txtMobile;
	private TextField txtPhone;
	private TextField txtEmail;
	private TextField txtWebSite;

	private ComboBox<PaymentType> cbPaymentType;
	private ComboBox<PaymentMethod> cbPaymentMethod;
	private TextField txtPaymentTerm;
	private TextField txtAccountNumber;
	private ComboBox<BankAccountType> cbAccountType;
	private ComboBox<Bank> cbBank;
	private ComboBox<BankAccountStatus> cbAccountStatus;
	private TextField txtLogin;
	private PasswordField txtPassword;
	private ComboBox<Role> cbRole;

	private boolean listMode;
	private PersonType personType;

	private ConfigurableFilterDataProvider<Person, Void, SerializablePredicate<Person>> filterDataProvider;

	public PersonLayout(boolean list) throws IOException {

		super("", KEY_PERSON);
		if (Commons.PERSON_TYPE.equals(PersonType.SUPPLIER.getName())) {
			personType = PersonType.SUPPLIER;
			this.pageTitle = "Proveedores";
		}
		if (Commons.PERSON_TYPE.equals(PersonType.CUSTOMER.getName())) {
			personType = PersonType.CUSTOMER;
			this.pageTitle = "Clientes";
		}
		
		if (Commons.PERSON_TYPE.equals(PersonType.USER.getName())) {
			personType = PersonType.USER;
			this.pageTitle = "Usuarios";
		}

		listMode = list;
		supplierBll = SupplierBll.getInstance();
		payMethodBll = PaymentMethodBll.getInstance();
		payTypeBll = PaymentTypeBll.getInstance();
		personBll = PersonBll.getInstance();
		userBll = UserBll.getInstance();
		countryBll = CountryBll.getInstance();
		stateBll = StateBll.getInstance();
		cityBll = CityBll.getInstance();
		bankBll = BankBll.getInstance();
		roleBll = RoleBll.getInstance();

		if (listMode) {
			addListTab();
		}
	}

	public PersonLayout() throws IOException {
		super("", KEY_PERSON);
		if (Commons.PERSON_TYPE.equals(PersonType.SUPPLIER.getName())) {
			personType = PersonType.SUPPLIER;
			this.pageTitle = "Proveedores";
		}
		if (Commons.PERSON_TYPE.equals(PersonType.CUSTOMER.getName())) {
			personType = PersonType.CUSTOMER;
			this.pageTitle = "Clientes";
		}
		if (Commons.PERSON_TYPE.equals(PersonType.USER.getName())) {
			personType = PersonType.USER;
			this.pageTitle = "Usuarios";
		}

		supplierBll = SupplierBll.getInstance();
		personBll = PersonBll.getInstance();
		userBll = UserBll.getInstance();
		payMethodBll = PaymentMethodBll.getInstance();
		payTypeBll = PaymentTypeBll.getInstance();
		countryBll = CountryBll.getInstance();
		stateBll = StateBll.getInstance();
		cityBll = CityBll.getInstance();
		bankBll = BankBll.getInstance();
		roleBll = RoleBll.getInstance();

		if (listMode) {
			addListTab();
		}
		listMode = false;

	}

	@Override
	protected AbstractOrderedLayout buildListView() {
		VerticalLayout layout = ViewHelper.buildVerticalLayout(false, false);
		Panel buttonPanel = null;
		if (listMode) {
			buttonPanel = buildButtonPanelListMode();
		} else {
			buttonPanel = buildButtonPanelForLists();
		}
		Panel filterPanel = buildFilterPanel();
		Panel dataPanel = buildGridPanel();
		layout.addComponents(buttonPanel, filterPanel, dataPanel);
		this.setSpacing(false);
		this.setMargin(false);
		return layout;
	}

	@Override
	protected AbstractOrderedLayout buildEditionView(Person entity) {
		VerticalLayout layout = ViewHelper.buildVerticalLayout(false, false);
		Panel buttonPanelUpper = buildButtonPanelForEdition(entity);
		Component dataPanel = buildEditionComponent(entity);
		Panel buttonPanelLower = buildButtonPanelForEdition(entity);
		layout.addComponents(buttonPanelUpper, dataPanel, buttonPanelLower);
		return layout;
	}

	@Override
	protected Panel buildGridPanel() {
		VerticalLayout layout = ViewHelper.buildVerticalLayout(true, true);
		grid = ViewHelper.buildGrid(SelectionMode.SINGLE);
		grid.addColumn(Person::getDocumentNumber).setCaption("Número de documento");
		grid.addColumn(person -> {
			if (person != null) {
				return person.getName() + " " + person.getLastName();
			} else {
				return null;
			}
		}).setCaption("Nombre y apellidos");

		layout.addComponent(ViewHelper.buildPanel(null, grid));
		fillGridData();
		return ViewHelper.buildPanel(null, layout);
	}

	@Override
	protected Component buildEditionComponent(Person person) {
		VerticalLayout layout = ViewHelper.buildVerticalLayout(false, false);

		Panel basicPanel;
		Panel contactPanel;
		Panel paymentPanel;
		Panel bankPanel;
		Panel userPanel;
		/// 1. Informacion basica de la persona
		cbDocumentType = new ComboBox<>("Tipo de documento");
		cbDocumentType.setDescription("Tipo");
		cbDocumentType.setEmptySelectionAllowed(false);
		cbDocumentType.setRequiredIndicatorVisible(true);
		cbDocumentType.focus();
		cbDocumentType.setStyleName(ValoTheme.COMBOBOX_TINY);
		ListDataProvider<DocumentIdType> docTypeDataProv = new ListDataProvider<>(
				Arrays.asList(DocumentIdType.values()));
		cbDocumentType.setDataProvider(docTypeDataProv);
		cbDocumentType.setItemCaptionGenerator(DocumentIdType::getDisplay);

		txtDocumentId = new TextField("Número de documento");
		txtDocumentId.setRequiredIndicatorVisible(true);
		txtDocumentId.setStyleName(ValoTheme.TEXTFIELD_TINY);

		txtName = new TextField("Nombres");
		txtName.setWidth("50%");
		txtName.setRequiredIndicatorVisible(true);
		txtName.setStyleName(ValoTheme.TEXTFIELD_TINY);

		txtLastName = new TextField("Apellidos");
		txtLastName.setWidth("50%");
		txtLastName.setStyleName(ValoTheme.TEXTFIELD_TINY);

		FormLayout basicForm = ViewHelper.buildForm("Datos basicos", true, false);
		basicForm.addComponents(cbDocumentType, txtDocumentId, txtName, txtLastName);
		basicPanel = ViewHelper.buildPanel("Datos basicos", basicForm);
		layout.addComponents(basicPanel);

		if (personType.equals(PersonType.USER)) {
			userPanel = (Panel) buildUserPanel(person);
			layout.addComponents(userPanel);
		} else {

			// 2. Datos de contacto
			txtContactName = new TextField("Nombre de contacto");
			txtContactName.setStyleName(ValoTheme.TEXTFIELD_TINY);

			txtAddress = new TextField("Dirección");
			txtAddress.setStyleName(ValoTheme.TEXTFIELD_TINY);

			cbCountry = new ComboBox<>("País");
			cbCountry.setEmptySelectionCaption("Seleccione");
			cbCountry.setStyleName(ValoTheme.COMBOBOX_TINY);
			ListDataProvider<Country> countryDataProv = new ListDataProvider<>(countryBll.selectAll());
			cbCountry.setDataProvider(countryDataProv);
			cbCountry.setItemCaptionGenerator(Country::getName);

			cbState = new ComboBox<>("Departamento");
			cbState.setEmptySelectionCaption("Seleccione");
			cbState.setStyleName(ValoTheme.COMBOBOX_TINY);
			ListDataProvider<State> stateDataProv = new ListDataProvider<>(stateBll.selectAll());
			cbState.setDataProvider(stateDataProv);
			cbState.setItemCaptionGenerator(State::getName);

			cbCity = new ComboBox<>("Ciudad");
			cbCity.setEmptySelectionCaption("Seleccione");
			cbCity.setStyleName(ValoTheme.COMBOBOX_TINY);
			ListDataProvider<City> cityDataProv = new ListDataProvider<>(cityBll.selectAll());
			cbCity.setDataProvider(cityDataProv);
			cbCity.setItemCaptionGenerator(City::getName);

			txtNeighborhood = new TextField("Barrio");
			txtNeighborhood.setStyleName(ValoTheme.TEXTFIELD_TINY);

			txtMobile = new TextField("Teléfono móvil");
			txtMobile.setStyleName(ValoTheme.TEXTFIELD_TINY);

			txtPhone = new TextField("Teléfono fijo");
			txtPhone.setStyleName(ValoTheme.TEXTFIELD_TINY);

			txtEmail = new TextField("Correo electrónico");
			txtEmail.setStyleName(ValoTheme.TEXTFIELD_TINY);

			txtWebSite = new TextField("Sitio web");
			txtWebSite.setStyleName(ValoTheme.TEXTFIELD_TINY);

			// Establecer valores de persona

			// Eventos
			cbCountry.addValueChangeListener(e -> {
				selectCountry();
			});

			cbState.addValueChangeListener(e -> {
				selectState();
			});

			cbCity.addValueChangeListener(e -> {
				selectCity();
			});

			FormLayout contactForm = ViewHelper.buildForm("Datos de contacto", true, false);
			contactForm.addComponents(txtContactName, txtAddress, cbCountry, cbState, cbCity, txtNeighborhood,
					txtMobile, txtPhone, txtEmail, txtWebSite);

			contactPanel = ViewHelper.buildPanel("Datos de contacto", contactForm);

			// 3. Condiciones comerciales
			cbPaymentType = new ComboBox<>("Tipo de pago");
			cbPaymentType.setEmptySelectionCaption("Seleccione");
			cbPaymentType.setStyleName(ValoTheme.COMBOBOX_TINY);
			ListDataProvider<PaymentType> payTypeDataProv = new ListDataProvider<>(payTypeBll.selectAll());
			cbPaymentType.setDataProvider(payTypeDataProv);
			cbPaymentType.setItemCaptionGenerator(PaymentType::getName);

			cbPaymentMethod = new ComboBox<>("Forma de pago");
			cbPaymentMethod.setEmptySelectionCaption("Seleccione");
			cbPaymentMethod.setStyleName(ValoTheme.COMBOBOX_TINY);
			ListDataProvider<PaymentMethod> payMetDataProv = new ListDataProvider<>(payMethodBll.selectAll());
			cbPaymentMethod.setDataProvider(payMetDataProv);
			cbPaymentMethod.setItemCaptionGenerator(PaymentMethod::getName);

			txtPaymentTerm = new TextField("Plazo");
			txtPaymentTerm.setStyleName(ValoTheme.TEXTFIELD_TINY);

			FormLayout paymentForm = ViewHelper.buildForm("Datos para pagos", true, false);
			paymentForm.addComponents(cbPaymentType, cbPaymentMethod, txtPaymentTerm);
			paymentPanel = ViewHelper.buildPanel("Datos para pagos", paymentForm);

			// 3. Datos bancarios
			cbAccountType = new ComboBox<>("Tipo de cuenta");
			cbAccountType.setEmptySelectionCaption("Seleccione");
			cbAccountType.setStyleName(ValoTheme.COMBOBOX_TINY);
			ListDataProvider<BankAccountType> accTypeDataProv = new ListDataProvider<>(
					Arrays.asList(BankAccountType.values()));
			cbAccountType.setDataProvider(accTypeDataProv);
			cbAccountType.setItemCaptionGenerator(BankAccountType::getDisplay);

			txtAccountNumber = new TextField("Número de cuenta");
			txtAccountNumber.setStyleName(ValoTheme.TEXTFIELD_TINY);

			cbBank = new ComboBox<>("Entidad financiera");
			cbBank.setEmptySelectionCaption("Seleccione");
			cbBank.setStyleName(ValoTheme.COMBOBOX_TINY);
			ListDataProvider<Bank> bankDataProv = new ListDataProvider<>(bankBll.selectAll());
			cbBank.setDataProvider(bankDataProv);
			cbBank.setItemCaptionGenerator(Bank::getName);

			cbAccountStatus = new ComboBox<>("Estado");
			cbAccountStatus.setEmptySelectionCaption("Seleccione");
			cbAccountStatus.setStyleName(ValoTheme.COMBOBOX_TINY);
			ListDataProvider<BankAccountStatus> accStatusDataProv = new ListDataProvider<>(
					Arrays.asList(BankAccountStatus.values()));
			cbAccountStatus.setDataProvider(accStatusDataProv);
			cbAccountStatus.setItemCaptionGenerator(BankAccountStatus::getDisplay);

			FormLayout bankForm = ViewHelper.buildForm("Datos bancarios", false, false);
			bankForm.addComponents(cbAccountType, txtAccountNumber, cbBank, cbAccountStatus);
			bankPanel = ViewHelper.buildPanel("Datos bancarios", bankForm);
			// ----------------------------------------------------------------------------------
			layout.addComponents(contactPanel, paymentPanel, bankPanel);
		}

		setFieldValues(person);
		return layout;
	}

	private Component buildUserPanel(Person person) {
		User user = null;
		if (person != null) {
			user = userBll.select(person);
		}
		txtLogin = new TextField("Nombre de usuario");
		txtLogin.setRequiredIndicatorVisible(true);
		txtLogin.setStyleName(ValoTheme.TEXTFIELD_TINY);

		txtPassword = new PasswordField("Contraseña");
		txtPassword.setRequiredIndicatorVisible(true);
		txtPassword.setStyleName(ValoTheme.TEXTFIELD_TINY);

		cbRole = new ComboBox<>("Rol");
		cbRole.setEmptySelectionCaption("Seleccione");
		cbRole.setStyleName(ValoTheme.COMBOBOX_TINY);
		cbRole.setRequiredIndicatorVisible(true);
		ListDataProvider<Role> roleDataProv = new ListDataProvider<>(roleBll.selectAll());
		cbRole.setDataProvider(roleDataProv);
		cbRole.setItemCaptionGenerator(Role::getName);

		if (user != null) {
			txtLogin.setValue(user.getLogin());
			txtPassword.setValue(user.getPassword());
			cbRole.setValue(user.getRole());
		}

		FormLayout authForm = ViewHelper.buildForm("Datos de autenticación", false, false);
		authForm.addComponents(txtLogin, txtPassword);
		Panel authPanel = ViewHelper.buildPanel("Datos de autenticación", authForm);

		return authPanel;

	}

	private void setFieldValues(Person person) {
		// Establecer datos a los campos
		if (person != null) {
			cbDocumentType.setValue(person.getDocumentType() != null ? person.getDocumentType() : null);
			txtDocumentId.setValue(person.getDocumentNumber() != null ? person.getDocumentNumber() : "");
			txtName.setValue(person.getName() != null ? person.getName() : "");
			txtLastName.setValue(person.getLastName() != null ? person.getLastName() : "");
			if (!personType.equals(PersonType.USER)) {
				txtContactName.setValue(person.getContactName() != null ? person.getContactName() : "");
				txtAddress.setValue(person.getAddress() != null ? person.getAddress() : "");

				cbCountry.setValue(person.getCity() != null && person.getCity().getState() != null
						? person.getCity().getState().getCountry()
						: null);
				cbState.setValue(
						person.getCity() != null && person.getCity().getState() != null ? person.getCity().getState()
								: null);
				cbCity.setValue(person.getCity() != null ? person.getCity() : null);

				txtNeighborhood.setValue(person.getNeighborhood() != null ? person.getNeighborhood() : "");
				txtMobile.setValue(person.getMobile() != null ? person.getMobile() : "");
				txtPhone.setValue(person.getPhone() != null ? person.getPhone() : "");
				txtEmail.setValue(person.getEmail() != null ? person.getEmail() : "");
				txtWebSite.setValue(person.getWebSite() != null ? person.getWebSite() : "");

				if (person.getBankAccount() != null) {
					cbAccountType.setValue(person.getBankAccount().getType());
					txtAccountNumber.setValue(person.getBankAccount().getAccountNumber());
					cbBank.setValue(person.getBankAccount().getBank());
					cbAccountStatus.setValue(person.getBankAccount().getStatus());
				}

				Supplier supplier = supplierBll.select(person.getDocumentNumber());
				if (supplier != null) {
					cbPaymentType.setValue(supplier.getPaymentType());
					cbPaymentMethod.setValue(supplier.getPaymentMethod());
					txtPaymentTerm.setValue(supplier.getPaymentTerm() != null ? supplier.getPaymentTerm() : "");
				}
			}
		}

	}

	private void selectCountry() {
		Country country = cbCountry.getSelectedItem().isPresent() ? cbCountry.getSelectedItem().get() : null;
		if (country != null) {
			ListDataProvider<State> stateDataProv = new ListDataProvider<>(stateBll.select(country));
			cbState.setDataProvider(stateDataProv);
			cbState.setItemCaptionGenerator(State::getName);
		}

	}

	private void selectState() {
		if (cbCountry.getSelectedItem().isPresent() && cbState.getSelectedItem().isPresent()) {
			State state = cbState.getSelectedItem().get();
			ListDataProvider<City> cityDataProv = new ListDataProvider<>(cityBll.select(state));
			cbCity.setDataProvider(cityDataProv);
			cbCity.setItemCaptionGenerator(City::getName);
		} else if (!cbCountry.getSelectedItem().isPresent() && cbState.getSelectedItem().isPresent()) {
			ViewHelper.showNotification("Debe seleccionar un pais", Notification.Type.ERROR_MESSAGE);
		}

	}

	private void selectCity() {

		if (!cbState.getSelectedItem().isPresent() && cbCity.getSelectedItem().isPresent()) {
			ViewHelper.showNotification("Debe seleccionar un departamento", Notification.Type.ERROR_MESSAGE);
		}

	}

	protected Panel buildButtonPanelListMode() {
		HorizontalLayout layout = ViewHelper.buildHorizontalLayout(true, true);
		Button btNew = buildButtonForNewAction(ValoTheme.BUTTON_TINY);
		Button btEdit = buildButtonForEditAction(ValoTheme.BUTTON_TINY);
		Button btDelete = buildButtonForDeleteAction(ValoTheme.BUTTON_TINY);
		layout.addComponents(btNew, btEdit, btDelete);
		return ViewHelper.buildPanel(null, layout);
	}

	@Override
	protected void fillGridData() {
		ListDataProvider<Person> dataProvider = new ListDataProvider<>(personBll.select(personType));
		filterDataProvider = dataProvider.withConfigurableFilter();
		grid.setDataProvider(filterDataProvider);

	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	protected void saveButtonAction(Person person) {
		try {
			String message = validateRequiredFields();
			if (!message.isEmpty()) {
				throw new Exception(message);
			}

			Person personSaved = null;
			Supplier supplierSaved = null;
			User userSaved = null;

			try {
				personSaved = savePerson(person);
				if (personSaved != null) {
					if (personType.equals(PersonType.SUPPLIER) || personType.equals(PersonType.CUSTOMER)) {
						supplierSaved = saveThird(personSaved);
					} else {
						userSaved = saveUser(personSaved);
					}
				}

				if (supplierSaved != null || userSaved != null) {
					afterSave("Persona guardada");
					ViewHelper.showNotification("Persona guardada", Notification.Type.WARNING_MESSAGE);
				}
			} catch (ModelValidationException ex) {
				log.error(ex);
				ViewHelper.showNotification(ex.getMessage(), Notification.Type.ERROR_MESSAGE);
			} catch (HibernateException ex) {
				log.error(ex);
				personBll.rollback();
				ViewHelper.showNotification(
						"Los datos de la persona no pudieron ser guardados, contacte al administrador (3002007694)",
						Notification.Type.ERROR_MESSAGE);
			}

		} catch (Exception e) {
			e.printStackTrace();
			ViewHelper.showNotification(e.getMessage(), Notification.Type.ERROR_MESSAGE);
		}

	}

	// Guardar datos básicos de persona
	private Person savePerson(Person person) {
		Person.Builder personBuilder = null;
		Person personSaved = null;
		if (person == null) {
			personBuilder = Person.builder();
		} else {
			personBuilder = Person.builder(person);
		}

		// Objeto persona
		String lastName = txtLastName.getValue() != null ? txtLastName.getValue() : null;
		DocumentIdType documentIdType = cbDocumentType.getSelectedItem().isPresent()
				? cbDocumentType.getSelectedItem().get()
				: null;
		String docId = txtDocumentId.getValue() != null ? txtDocumentId.getValue() : "";

		City city = cbCity != null ? cbCity.getSelectedItem().isPresent() ? cbCity.getSelectedItem().get() : null
				: null;

		// Construir objeto con datos bancarios
		BankAccount bankAccount = null;
		BankAccount.Builder bankAccountBuilder = null;
		if (person != null && person.getBankAccount() != null) {
			bankAccount = person.getBankAccount();
			bankAccountBuilder = BankAccount.builder(bankAccount);
		} else {
			bankAccountBuilder = BankAccount.builder();
		}

		BankAccountType accountType = cbAccountType != null
				? cbAccountType.getSelectedItem().isPresent() ? cbAccountType.getSelectedItem().get() : null
				: null;

		Bank bank = cbBank != null ? cbBank.getSelectedItem().isPresent() ? cbBank.getSelectedItem().get() : null
				: null;

		BankAccountStatus accountStatus = cbAccountStatus != null
				? cbAccountStatus.getSelectedItem().isPresent() ? cbAccountStatus.getSelectedItem().get() : null
				: null;

		// objeto cuenta bancaria
		bankAccount = bankAccountBuilder.type(accountType)
				.account(txtAccountNumber != null ? txtAccountNumber.getValue() : null).bank(bank).status(accountStatus)
				.build();

		if (bankAccount != null && bankAccount.getType() == null) {
			bankAccount = null;
		}

		person = personBuilder.documentType(documentIdType).documentNumber(docId).name(txtName.getValue())
				.lastName(lastName).type(personType)
				.contactName(txtContactName != null ? txtContactName.getValue() : null)
				.address(txtAddress != null ? txtAddress.getValue() : null).city(city)
				.neighborhood(txtNeighborhood != null ? txtNeighborhood.getValue() : null)
				.mobile(txtMobile != null ? txtMobile.getValue() : null)
				.phone(txtPhone != null ? txtPhone.getValue() : null)
				.email(txtEmail != null ? txtEmail.getValue() : null)
				.webSite(txtWebSite != null ? txtWebSite.getValue() : null).bankAccount(bankAccount).build();

		log.info("person:" + person.toString());

		try {
			personBll.save(person);
			// afterSave("Persona guardada");

			// Consultar el objeto guardado
			personSaved = personBll.select(person.getDocumentNumber());

		} catch (ModelValidationException ex) {
			person = null;
			log.error(ex);
			ViewHelper.showNotification(ex.getMessage(), Notification.Type.ERROR_MESSAGE);
		} catch (HibernateException ex) {
			person = null;
			log.error(ex);
			personBll.rollback();
			ViewHelper.showNotification(
					"Los datos de la persona no pudieron ser guardados, contacte al administrador (3002007694)",
					Notification.Type.ERROR_MESSAGE);
		}

		return personSaved;
	}

	// Guardar un cliente o proveedor
	private Supplier saveThird(Person person) {
		Supplier supplier = null;
		Supplier.Builder supplierBuilder = null;

		try {
			Person personObj = personBll.select(person.getDocumentNumber());
			supplier = supplierBll.select(personObj.getDocumentNumber());

			if (supplier == null) {
				supplierBuilder = Supplier.builder();
			} else {
				supplierBuilder = Supplier.builder(supplier);
			}

			PaymentType paymentType = cbPaymentType.getSelectedItem().isPresent()
					? cbPaymentType.getSelectedItem().get()
					: null;
			PaymentMethod paymentMethod = cbPaymentMethod.getSelectedItem().isPresent()
					? cbPaymentMethod.getSelectedItem().get()
					: null;

			// Objeto proveedor
			supplier = supplierBuilder.person(personObj).paymentType(paymentType).paymentMethod(paymentMethod)
					.paymentTerm(txtPaymentTerm.getValue()).build();

			log.info("tercero:" + supplier.toString());

			supplierBll.save(supplier);
			// afterSave("Persona guardada");

		} catch (ModelValidationException ex) {
			supplier = null;
			log.error(ex);
			ViewHelper.showNotification(ex.getMessage(), Notification.Type.ERROR_MESSAGE);
		} catch (HibernateException ex) {
			supplier = null;
			log.error(ex);
			personBll.rollback();
			ViewHelper.showNotification(
					"Los datos de la persona no pudieron ser guardados, contacte al administrador (3002007694)",
					Notification.Type.ERROR_MESSAGE);
		}
		return supplier;
	}

	// Guardar un usuario
	private User saveUser(Person person) {
		User user = null;
		User.Builder userBuilder = null;

		try {
			user = userBll.select(person);

			if (user == null) {
				userBuilder = User.builder();
			} else {
				userBuilder = User.builder(user);
			}

			Role role = cbRole.getSelectedItem().isPresent() ? cbRole.getSelectedItem().get() : null;

			// Objeto usuario
			user = userBuilder.person(person).login(txtLogin.getValue()).password(txtPassword.getValue()).role(role)
					.build();

			log.info("usuario:" + user.toString());

			userBll.save(user);
			// afterSave("Persona guardada");

		} catch (ModelValidationException ex) {
			user = null;
			log.error(ex);
			ViewHelper.showNotification(ex.getMessage(), Notification.Type.ERROR_MESSAGE);
		} catch (HibernateException ex) {
			user = null;
			log.error(ex);
			personBll.rollback();
			ViewHelper.showNotification(
					"Los datos de la persona no pudieron ser guardados, contacte al administrador (3002007694)",
					Notification.Type.ERROR_MESSAGE);

		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvalidKeySpecException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return user;
	}

	private String validateRequiredFields() {
		String message = "";
		String character = "|";

		if (!cbDocumentType.getSelectedItem().isPresent()) {
			message = "El tipo de documento es obligatorio";
		}
		if (txtDocumentId.getValue() == null || txtDocumentId.getValue().isEmpty()) {
			if (!message.isEmpty()) {
				message = message.concat(character);
			}
			message = message.concat("El número de documento es obligatorio");
		}
		if (txtName.getValue() == null || txtName.getValue().isEmpty()) {
			if (!message.isEmpty()) {
				message = message.concat(character);
			}
			message = message.concat("El nombre es obligatorio");
		}

		return message;

	}

	@Override
	public Person getSelected() {
		Person person = null;
		Set<Person> persons = grid.getSelectedItems();
		if (persons != null && !persons.isEmpty()) {
			person = (Person) persons.toArray()[0];
		}
		return person;
	}

	@Override
	protected void delete(Person entity) {
		entity = Person.builder(entity).archived(true).build();
		save(personBll, entity, "Proveedor borrado");
	}

	private Panel buildFilterPanel() {
		HorizontalLayout layout = ViewHelper.buildHorizontalLayout(true, true);

		txFilterByCode = new TextField("Número de documento");
		txFilterByCode.addValueChangeListener(e -> refreshGrid());
		txFilterByCode.setStyleName(ValoTheme.TEXTFIELD_TINY);

		txFilterByName = new TextField("Nombres");
		txFilterByName.addValueChangeListener(e -> refreshGrid());
		txFilterByName.setStyleName(ValoTheme.TEXTFIELD_TINY);

		txFilterByLastName = new TextField("Apellidos");
		txFilterByLastName.addValueChangeListener(e -> refreshGrid());
		txFilterByLastName.setStyleName(ValoTheme.TEXTFIELD_TINY);

		layout.addComponents(txFilterByCode, txFilterByName, txFilterByLastName);
		return ViewHelper.buildPanel("Buscar por", layout);
	}

	private void refreshGrid() {
		filterDataProvider.setFilter(filterGrid());
		grid.getDataProvider().refreshAll();
	}

	private SerializablePredicate<Person> filterGrid() {
		SerializablePredicate<Person> columnPredicate = null;
		String codeFilter = txFilterByCode.getValue().trim();
		String nameFilter = txFilterByName.getValue().trim();
		String lastNameFilter = txFilterByLastName.getValue().trim();
		columnPredicate = person -> (person.getName().toLowerCase().contains(nameFilter.toLowerCase())
				&& person.getLastName().toLowerCase().contains(lastNameFilter.toLowerCase())
				&& person.getDocumentNumber().toLowerCase().contains(codeFilter.toLowerCase()));
		return columnPredicate;
	}

}
