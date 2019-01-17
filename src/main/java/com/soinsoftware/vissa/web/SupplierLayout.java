package com.soinsoftware.vissa.web;

import java.io.IOException;
import java.util.Arrays;
import java.util.Set;

import org.apache.log4j.Logger;
import org.springframework.transaction.annotation.Transactional;

import com.soinsoftware.vissa.bll.BankAccountBll;
import com.soinsoftware.vissa.bll.BankBll;
import com.soinsoftware.vissa.bll.CityBll;
import com.soinsoftware.vissa.bll.CountryBll;
import com.soinsoftware.vissa.bll.PaymentMethodBll;
import com.soinsoftware.vissa.bll.PaymentTypeBll;
import com.soinsoftware.vissa.bll.PersonBll;
import com.soinsoftware.vissa.bll.StateBll;
import com.soinsoftware.vissa.bll.SupplierBll;
import com.soinsoftware.vissa.model.Bank;
import com.soinsoftware.vissa.model.BankAccount;
import com.soinsoftware.vissa.model.BankAccountStatus;
import com.soinsoftware.vissa.model.BankAccountType;
import com.soinsoftware.vissa.model.City;
import com.soinsoftware.vissa.model.Country;
import com.soinsoftware.vissa.model.Customer;
import com.soinsoftware.vissa.model.DocumentIdType;
import com.soinsoftware.vissa.model.PaymentMethod;
import com.soinsoftware.vissa.model.PaymentType;
import com.soinsoftware.vissa.model.Person;
import com.soinsoftware.vissa.model.PersonType;
import com.soinsoftware.vissa.model.State;
import com.soinsoftware.vissa.model.Supplier;
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
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ValoTheme;

@SuppressWarnings("unchecked")
public class SupplierLayout extends AbstractEditableLayout<Supplier> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5076502522106126046L;

	protected static final Logger log = Logger.getLogger(SupplierLayout.class);

	private final SupplierBll supplierBll;
	private final PersonBll personBll;
	private final PaymentMethodBll payMethodBll;
	private final PaymentTypeBll payTypeBll;
	private final CountryBll countryBll;
	private final StateBll stateBll;
	private final CityBll cityBll;
	private final BankBll bankBll;
	private final BankAccountBll bankAccountBll;

	private TextField txFilterByName;
	private TextField txFilterByCode;
	private Grid<Supplier> grid;

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

	private boolean listMode;
	private PersonType personType;
	private Supplier supplier;
	private Customer customer;

	private ConfigurableFilterDataProvider<Supplier, Void, SerializablePredicate<Supplier>> filterDataProvider;

	public SupplierLayout(boolean list) throws IOException {

		super("");
		if (Commons.PERSON_TYPE.equals(PersonType.SUPPLIER.getName())) {
			personType = PersonType.SUPPLIER;
			this.pageTitle = "Proveedores";
		}
		if (Commons.PERSON_TYPE.equals(PersonType.CUSTOMER.getName())) {
			personType = PersonType.CUSTOMER;
			this.pageTitle = "Clientes";
		}

		listMode = list;
		supplierBll = SupplierBll.getInstance();
		payMethodBll = PaymentMethodBll.getInstance();
		payTypeBll = PaymentTypeBll.getInstance();
		personBll = PersonBll.getInstance();
		countryBll = CountryBll.getInstance();
		stateBll = StateBll.getInstance();
		cityBll = CityBll.getInstance();
		bankBll = BankBll.getInstance();
		bankAccountBll = BankAccountBll.getInstance();

		if (listMode) {
			addListTab();
		}
	}

	public SupplierLayout() throws IOException {
		super("");
		if (Commons.PERSON_TYPE.equals(PersonType.SUPPLIER.getName())) {
			personType = PersonType.SUPPLIER;
			this.pageTitle = "Proveedores";
		}
		if (Commons.PERSON_TYPE.equals(PersonType.CUSTOMER.getName())) {
			personType = PersonType.CUSTOMER;
			this.pageTitle = "Clientes";
		}

		supplierBll = SupplierBll.getInstance();
		personBll = PersonBll.getInstance();
		payMethodBll = PaymentMethodBll.getInstance();
		payTypeBll = PaymentTypeBll.getInstance();
		countryBll = CountryBll.getInstance();
		stateBll = StateBll.getInstance();
		cityBll = CityBll.getInstance();
		bankBll = BankBll.getInstance();
		bankAccountBll = BankAccountBll.getInstance();

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
	protected AbstractOrderedLayout buildEditionView(Supplier entity) {
		VerticalLayout layout = ViewHelper.buildVerticalLayout(false, false);
		Panel buttonPanel = buildButtonPanelForEdition(entity);
		Component dataPanel = buildEditionComponent(entity);
		Panel buttonPanel2 = buildButtonPanelForEdition(entity);
		layout.addComponents(buttonPanel, dataPanel, buttonPanel2);
		return layout;
	}

	@Override
	protected Panel buildGridPanel() {
		VerticalLayout layout = ViewHelper.buildVerticalLayout(true, true);
		grid = ViewHelper.buildGrid(SelectionMode.SINGLE);
		grid.addColumn(supplier -> {
			if (supplier.getPerson() != null) {
				return supplier.getPerson().getDocumentNumber();
			} else {
				return null;
			}
		}).setCaption("Número de documento");
		grid.addColumn(supplier -> {
			if (supplier.getPerson() != null) {
				return supplier.getPerson().getName();
			} else {
				return null;
			}
		}).setCaption("Nombres");
		grid.addColumn(supplier -> {
			if (supplier.getPerson() != null) {
				return supplier.getPerson().getLastName();
			} else {
				return null;
			}
		}).setCaption("Apellidos");

		layout.addComponent(ViewHelper.buildPanel(null, grid));
		fillGridData();
		return ViewHelper.buildPanel(null, layout);
	}

	@Override
	protected Component buildEditionComponent(Supplier supplier) {
		VerticalLayout layout = ViewHelper.buildVerticalLayout(false, false);

		/// 1. Informacion basica de la persona
		cbDocumentType = new ComboBox<>("Tipo de documento");
		cbDocumentType.setDescription("Tipo");
		cbDocumentType.setEmptySelectionAllowed(false);
		cbDocumentType.setRequiredIndicatorVisible(true);
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
		Panel basicPanel = ViewHelper.buildPanel("Datos basicos", basicForm);

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
		contactForm.addComponents(txtContactName, txtAddress, cbCountry, cbState, cbCity, txtNeighborhood, txtMobile, txtPhone, txtEmail,
				txtWebSite);

		Panel contactPanel = ViewHelper.buildPanel("Datos de contacto", contactForm);

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
		Panel paymentPanel = ViewHelper.buildPanel("Datos para pagos", paymentForm);
		

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
		Panel bankPanel = ViewHelper.buildPanel("Datos bancarios", bankForm);
		// ----------------------------------------------------------------------------------

		layout.addComponents(basicPanel, contactPanel, paymentPanel, bankPanel);
		setFieldValue(supplier);
		return layout;
	}

	private void setFieldValue(Supplier supplier) {
		// Establecer datos a los campos
		if (supplier != null) {
			cbPaymentType.setValue(supplier.getPaymentType());
			cbPaymentMethod.setValue(supplier.getPaymentMethod());
			txtPaymentTerm.setValue(supplier.getPaymentTerm() != null ? supplier.getPaymentTerm() : "");

			if (supplier.getPerson() != null) {
				Person person = supplier.getPerson();

				cbDocumentType.setValue(person.getDocumentType() != null ? person.getDocumentType() : null);
				txtDocumentId.setValue(person.getDocumentNumber() != null ? person.getDocumentNumber() : "");
				txtName.setValue(person.getName() != null ? person.getName() : "");
				txtLastName.setValue(person.getLastName() != null ? person.getLastName() : "");
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
					cbAccountType.setValue(supplier.getPerson().getBankAccount().getType());
					txtAccountNumber.setValue(supplier.getPerson().getBankAccount().getAccountNumber());
					cbBank.setValue(supplier.getPerson().getBankAccount().getBank());
					cbAccountStatus.setValue(supplier.getPerson().getBankAccount().getStatus());
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
		ListDataProvider<Supplier> dataProvider = new ListDataProvider<>(supplierBll.selectAll(false));
		filterDataProvider = dataProvider.withConfigurableFilter();
		grid.setDataProvider(filterDataProvider);

	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	protected void saveButtonAction(Supplier supplier) {
		try {
			String message = validateRequiredFields();
			if (!message.isEmpty()) {
				throw new Exception(message);
			}
			Person person = null;
			Person.Builder personBuilder = null;
			Supplier.Builder supplierBuilder = null;
			if (supplier == null) {
				supplierBuilder = Supplier.builder();
				personBuilder = Person.builder();
			} else {
				supplierBuilder = Supplier.builder(supplier);
				if (supplier.getPerson() == null) {
					personBuilder = Person.builder();
				} else {
					person = supplier.getPerson();
					personBuilder = Person.builder(person);
				}
			}

			String lastName = txtLastName.getValue() != null ? txtLastName.getValue() : null;

			City city = cbCity.getSelectedItem().isPresent() ? cbCity.getSelectedItem().get() : null;

			PaymentType paymentType = cbPaymentType.getSelectedItem().isPresent()
					? cbPaymentType.getSelectedItem().get()
					: null;
			PaymentMethod paymentMethod = cbPaymentMethod.getSelectedItem().isPresent()
					? cbPaymentMethod.getSelectedItem().get()
					: null;
			log.info("paymentMethod:" + paymentMethod);

			// Construir objeto con datos bancarios
			BankAccount bankAccount = null;

			BankAccount.Builder bankAccountBuilder = null;
			if (supplier != null && supplier.getPerson() != null && supplier.getPerson().getBankAccount() != null) {
				bankAccount = supplier.getPerson().getBankAccount();
				bankAccountBuilder = BankAccount.builder(bankAccount);
			} else {
				bankAccountBuilder = BankAccount.builder();
			}

			BankAccountType accountType = cbAccountType.getSelectedItem().isPresent()
					? cbAccountType.getSelectedItem().get()
					: null;
			Bank bank = cbBank.getSelectedItem().isPresent() ? cbBank.getSelectedItem().get() : null;
			BankAccountStatus accountStatus = cbAccountStatus.getSelectedItem().isPresent()
					? cbAccountStatus.getSelectedItem().get()
					: null;

			// objeto cuenta bancaria
			bankAccount = bankAccountBuilder.type(accountType).account(txtAccountNumber.getValue()).bank(bank)
					.status(accountStatus).build();

			if (bankAccount != null && bankAccount.getType() == null) {
				bankAccount = null;
			}

			// objeto persona
			DocumentIdType documentIdType = cbDocumentType.getSelectedItem().isPresent()
					? cbDocumentType.getSelectedItem().get()
					: null;
			String docId = txtDocumentId.getValue() != null ? txtDocumentId.getValue() : "";
			person = personBuilder.documentType(documentIdType).documentNumber(docId).name(txtName.getValue())
					.lastName(lastName).type(personType).contactName(txtContactName.getValue())
					.address(txtAddress.getValue()).city(city).neighborhood(txtNeighborhood.getValue())
					.mobile(txtMobile.getValue()).phone(txtPhone.getValue()).email(txtEmail.getValue())
					.webSite(txtWebSite.getValue()).bankAccount(bankAccount).build();

			// objeto proveedor
			supplier = supplierBuilder.person(person).paymentType(paymentType).paymentMethod(paymentMethod)
					.paymentTerm(txtPaymentTerm.getValue()).build();

			try {

				save(supplierBll, supplier, "Persona guardada");

			} catch (Exception e) {
				log.error("Error al guardar el tercero: Exception: " + e.getMessage());
				e.printStackTrace();
				ViewHelper.showNotification("Se presentó un error al guardar el tercero",
						Notification.Type.ERROR_MESSAGE);
			}
		} catch (Exception e) {
			e.printStackTrace();
			ViewHelper.showNotification(e.getMessage(), Notification.Type.ERROR_MESSAGE);
		}

	}

	private String validateRequiredFields() {
		String message = "";
		String character = "|";
		log.info("validateRequiredFields" + cbDocumentType.getSelectedItem().isPresent());
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
	public Supplier getSelected() {
		Supplier supplier = null;
		Set<Supplier> suppliers = grid.getSelectedItems();
		if (suppliers != null && !suppliers.isEmpty()) {
			supplier = (Supplier) suppliers.toArray()[0];
		}
		return supplier;
	}

	@Override
	protected void delete(Supplier entity) {
		entity = Supplier.builder(entity).archived(true).build();
		save(supplierBll, entity, "Proveedor borrado");
	}

	private Panel buildFilterPanel() {
		HorizontalLayout layout = ViewHelper.buildHorizontalLayout(true, true);
		txFilterByName = new TextField("Nombre");
		txFilterByName.addValueChangeListener(e -> refreshGrid());
		txFilterByName.setStyleName(ValoTheme.TEXTFIELD_TINY);
		txFilterByCode = new TextField("Número de documento");
		txFilterByCode.addValueChangeListener(e -> refreshGrid());
		txFilterByCode.setStyleName(ValoTheme.TEXTFIELD_TINY);
		layout.addComponents(txFilterByCode, txFilterByName);
		return ViewHelper.buildPanel("Buscar por", layout);
	}

	private void refreshGrid() {
		filterDataProvider.setFilter(filterGrid());
		grid.getDataProvider().refreshAll();
	}

	private SerializablePredicate<Supplier> filterGrid() {
		SerializablePredicate<Supplier> columnPredicate = null;
		String codeFilter = txFilterByCode.getValue().trim();
		String nameFilter = txFilterByName.getValue().trim();
		columnPredicate = supplier -> (supplier.getPerson().getName().toLowerCase().contains(nameFilter.toLowerCase())
				&& supplier.getPerson().getDocumentNumber().toLowerCase().contains(codeFilter.toLowerCase()));
		return columnPredicate;
	}

}
