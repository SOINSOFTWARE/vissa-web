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
		return layout;
	}

	@Override
	protected AbstractOrderedLayout buildEditionView(Supplier entity) {
		VerticalLayout layout = ViewHelper.buildVerticalLayout(false, false);
		Panel buttonPanel = buildButtonPanelForEdition(entity);
		Component dataPanel = buildEditionComponent(entity);
		layout.addComponents(buttonPanel, dataPanel);
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
		ListDataProvider<DocumentIdType> docTypeDataProv = new ListDataProvider<>(
				Arrays.asList(DocumentIdType.values()));
		cbDocumentType.setDataProvider(docTypeDataProv);
		cbDocumentType.setItemCaptionGenerator(DocumentIdType::getDisplay);
		cbDocumentType.setValue(
				supplier != null && supplier.getPerson() != null ? supplier.getPerson().getDocumentType() : null);

		txtDocumentId = new TextField("Número de documento");
		txtDocumentId.setValue(
				supplier != null && supplier.getPerson() != null ? supplier.getPerson().getDocumentNumber() : "");

		txtName = new TextField("Nombres");
		txtName.setWidth("50%");
		txtName.setValue(supplier != null && supplier.getPerson() != null ? supplier.getPerson().getName() : "");

		txtLastName = new TextField("Apellidos");
		txtLastName.setWidth("50%");
		txtLastName
				.setValue(supplier != null && supplier.getPerson() != null ? supplier.getPerson().getLastName() : "");

		FormLayout basicForm = ViewHelper.buildForm("Datos basicos", true, false);
		basicForm.addComponents(cbDocumentType, txtDocumentId, txtName, txtLastName);
		Panel basicPanel = ViewHelper.buildPanel("Datos basicos", basicForm);

		// 2. Datos de contacto
		txtContactName = new TextField("Nombre de contacto");
		txtContactName.setValue(
				supplier != null && supplier.getPerson() != null && supplier.getPerson().getContactName() != null
						? supplier.getPerson().getContactName()
						: "");

		txtAddress = new TextField("Dirección");
		txtAddress.setValue(supplier != null && supplier.getPerson() != null ? supplier.getPerson().getAddress() : "");

		cbCountry = new ComboBox<>("País");
		cbCountry.setEmptySelectionCaption("Seleccione");
		ListDataProvider<Country> countryDataProv = new ListDataProvider<>(countryBll.selectAll());
		cbCountry.setDataProvider(countryDataProv);
		cbCountry.setItemCaptionGenerator(Country::getName);
		cbCountry.setValue(supplier != null && supplier.getPerson() != null
				? supplier.getPerson().getCity().getState().getCountry()
				: null);

		cbState = new ComboBox<>("Departamento");
		cbState.setEmptySelectionCaption("Seleccione");
		ListDataProvider<State> stateDataProv = new ListDataProvider<>(stateBll.selectAll());
		cbState.setDataProvider(stateDataProv);
		cbState.setItemCaptionGenerator(State::getName);
		cbState.setValue(
				supplier != null && supplier.getPerson() != null ? supplier.getPerson().getCity().getState() : null);

		cbCity = new ComboBox<>("Ciudad");
		cbCity.setEmptySelectionCaption("Seleccione");
		ListDataProvider<City> cityDataProv = new ListDataProvider<>(cityBll.selectAll());
		cbCity.setDataProvider(cityDataProv);
		cbCity.setItemCaptionGenerator(City::getName);
		cbCity.setValue(supplier != null && supplier.getPerson() != null ? supplier.getPerson().getCity() : null);

		cbCountry.addValueChangeListener(e -> {
			selectCountry();
		});

		cbState.addValueChangeListener(e -> {
			selectState();
		});

		cbCity.addValueChangeListener(e -> {
			selectCity();
		});

		txtMobile = new TextField("Teléfono móvil");
		txtMobile.setValue(supplier != null && supplier.getPerson() != null ? supplier.getPerson().getMobile() : "");

		txtPhone = new TextField("Teléfono fijo");
		txtPhone.setValue(supplier != null && supplier.getPerson() != null ? supplier.getPerson().getPhone() : "");

		txtEmail = new TextField("Correo electrónico");
		txtEmail.setValue(supplier != null && supplier.getPerson() != null ? supplier.getPerson().getEmail() : "");

		txtWebSite = new TextField("Sitio web");
		txtWebSite.setValue(supplier != null && supplier.getPerson() != null ? supplier.getPerson().getWebSite() : "");

		FormLayout contactForm = ViewHelper.buildForm("Datos de contacto", true, false);
		contactForm.addComponents(txtContactName, txtAddress, cbCountry, cbState, cbCity, txtMobile, txtPhone, txtEmail,
				txtWebSite);
		Panel contactPanel = ViewHelper.buildPanel("Datos de contacto", contactForm);

		// 3. Condiciones comerciales
		cbPaymentType = new ComboBox<>("Tipo de pago");
		cbPaymentType.setEmptySelectionCaption("Seleccione");
		ListDataProvider<PaymentType> payTypeDataProv = new ListDataProvider<>(payTypeBll.selectAll());
		cbPaymentType.setDataProvider(payTypeDataProv);
		cbPaymentType.setItemCaptionGenerator(PaymentType::getName);
		cbPaymentType.setValue(supplier != null ? supplier.getPaymentType() : null);

		cbPaymentMethod = new ComboBox<>("Forma de pago");
		cbPaymentMethod.setEmptySelectionCaption("Seleccione");
		ListDataProvider<PaymentMethod> payMetDataProv = new ListDataProvider<>(payMethodBll.selectAll());
		cbPaymentMethod.setDataProvider(payMetDataProv);
		cbPaymentMethod.setItemCaptionGenerator(PaymentMethod::getName);
		cbPaymentMethod.setValue(supplier != null ? supplier.getPaymentMethod() : null);

		txtPaymentTerm = new TextField("Plazo");
		txtPaymentTerm.setValue(supplier != null && supplier.getPaymentTerm() != null ? supplier.getPaymentTerm() : "");

		FormLayout paymentForm = ViewHelper.buildForm("Datos para pagos", true, false);
		paymentForm.addComponents(cbPaymentType, cbPaymentMethod, txtPaymentTerm);
		Panel paymentPanel = ViewHelper.buildPanel("Datos para pagos", paymentForm);

		// 3. Datos bancarios
		cbAccountType = new ComboBox<>("Tipo de cuenta");
		cbAccountType.setEmptySelectionCaption("Seleccione");
		ListDataProvider<BankAccountType> accTypeDataProv = new ListDataProvider<>(
				Arrays.asList(BankAccountType.values()));
		cbAccountType.setDataProvider(accTypeDataProv);
		cbAccountType.setItemCaptionGenerator(BankAccountType::getDisplay);

		txtAccountNumber = new TextField("Número de cuenta");

		cbBank = new ComboBox<>("Entidad financiera");
		cbBank.setEmptySelectionCaption("Seleccione");
		ListDataProvider<Bank> bankDataProv = new ListDataProvider<>(bankBll.selectAll());
		cbBank.setDataProvider(bankDataProv);
		cbBank.setItemCaptionGenerator(Bank::getName);

		cbAccountStatus = new ComboBox<>("Estado");
		cbAccountStatus.setEmptySelectionCaption("Seleccione");
		ListDataProvider<BankAccountStatus> accStatusDataProv = new ListDataProvider<>(
				Arrays.asList(BankAccountStatus.values()));
		cbAccountStatus.setDataProvider(accStatusDataProv);
		cbAccountStatus.setItemCaptionGenerator(BankAccountStatus::getDisplay);

		if (supplier != null && supplier.getPerson() != null && supplier.getPerson().getBankAccount() != null) {
			cbAccountType.setValue(supplier.getPerson().getBankAccount().getType());
			txtAccountNumber.setValue(supplier.getPerson().getBankAccount().getAccountNumber());
			cbBank.setValue(supplier.getPerson().getBankAccount().getBank());
			cbAccountStatus.setValue(supplier.getPerson().getBankAccount().getStatus());
		}

		FormLayout bankForm = ViewHelper.buildForm("Datos bancarios", false, false);
		bankForm.addComponents(cbAccountType, txtAccountNumber, cbBank, cbAccountStatus);
		Panel bankPanel = ViewHelper.buildPanel("Datos bancarios", bankForm);
		// ----------------------------------------------------------------------------------

		layout.addComponents(basicPanel, contactPanel, paymentPanel, bankPanel);
		return layout;
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
		Person person = null;
		Supplier.Builder supplierBuilder = null;
		if (supplier == null) {
			supplierBuilder = Supplier.builder();
		} else {
			supplierBuilder = Supplier.builder(supplier);
		}

		Person.Builder personBuilder = null;
		if (supplier.getPerson() == null) {
			personBuilder = Person.builder();
		} else {
			person = supplier.getPerson();
			personBuilder = Person.builder(person);
		}

		String lastName = txtLastName.getValue() != null ? txtLastName.getValue() : null;

		City city = cbCity.getSelectedItem().isPresent() ? cbCity.getSelectedItem().get() : null;

		PaymentType paymentType = cbPaymentType.getSelectedItem().isPresent() ? cbPaymentType.getSelectedItem().get()
				: null;
		PaymentMethod paymentMethod = cbPaymentMethod.getSelectedItem().isPresent()
				? cbPaymentMethod.getSelectedItem().get()
				: null;
		log.info("paymentMethod:" + paymentMethod);

		// Construir objeto con datos bancarios
		BankAccount bankAccount = null;

		BankAccount.Builder bankAccountBuilder = null;
		if (supplier.getPerson() != null && supplier.getPerson().getBankAccount() != null) {
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

		person = personBuilder.documentType(cbDocumentType.getValue()).documentNumber(txtDocumentId.getValue())
				.name(txtName.getValue()).lastName(lastName).type(personType).contactName(txtContactName.getValue())
				.address(txtAddress.getValue()).city(city).mobile(txtMobile.getValue()).phone(txtPhone.getValue())
				.email(txtEmail.getValue()).webSite(txtWebSite.getValue()).bankAccount(bankAccount).build();

		// objeto proveedor
		supplier = supplierBuilder.person(person).paymentType(paymentType).paymentMethod(paymentMethod)
				.paymentTerm(txtPaymentTerm.getValue()).build();

		try {

			save(supplierBll, supplier, "Persona guardada");

		} catch (Exception e) {
			log.error("Error al guardar el tercero: Exception: " + e.getMessage());
			e.printStackTrace();
			ViewHelper.showNotification("Se presentó un error al guardar el tercero", Notification.Type.ERROR_MESSAGE);
		}

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
		txFilterByCode = new TextField("Número de documento");
		txFilterByCode.addValueChangeListener(e -> refreshGrid());
		layout.addComponents(txFilterByCode, txFilterByName);
		return ViewHelper.buildPanel("Filtrar por", layout);
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
