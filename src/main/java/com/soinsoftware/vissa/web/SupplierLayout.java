package com.soinsoftware.vissa.web;

import java.io.IOException;
import java.util.Arrays;
import java.util.Date;
import java.util.Set;

import org.apache.log4j.Logger;

import com.soinsoftware.vissa.bll.PaymentMethodBll;
import com.soinsoftware.vissa.bll.PaymentTypeBll;
import com.soinsoftware.vissa.bll.PersonBll;
import com.soinsoftware.vissa.bll.SupplierBll;
import com.soinsoftware.vissa.model.BankAccount;
import com.soinsoftware.vissa.model.BankAccountType;
import com.soinsoftware.vissa.model.DocumentIdType;
import com.soinsoftware.vissa.model.MeasurementUnit;
import com.soinsoftware.vissa.model.PaymentMethod;
import com.soinsoftware.vissa.model.PaymentType;
import com.soinsoftware.vissa.model.Person;
import com.soinsoftware.vissa.model.Product;
import com.soinsoftware.vissa.model.ProductCategory;
import com.soinsoftware.vissa.model.ProductType;
import com.soinsoftware.vissa.model.Supplier;
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

	private TextField txFilterByName;
	private TextField txFilterByCode;
	private Grid<Supplier> grid;

	private TextField txtDocumentId;
	private ComboBox<DocumentIdType> cbDocumentType;
	private TextField txtName;
	private TextField txtLastName;
	private ComboBox<PaymentType> cbPaymentType;
	private ComboBox<PaymentMethod> cbPaymentMethod;
	private TextField txtPaymentTerm;
	private TextField txtAccountNumber;
	private ComboBox<BankAccountType> cbAccountType;
	private ComboBox<BankAccount> cbBank;
	private ComboBox<Object> cbAccountStatus;

	private boolean listMode;
	private String personType;

	private ConfigurableFilterDataProvider<Supplier, Void, SerializablePredicate<Supplier>> filterDataProvider;

	public SupplierLayout(boolean list, String type) throws IOException {
		super("Proveedores");
		listMode = list;
		supplierBll = SupplierBll.getInstance();
		payMethodBll = PaymentMethodBll.getInstance();
		payTypeBll = PaymentTypeBll.getInstance();
		personBll = PersonBll.getInstance();
		personType = type;
		if (listMode) {
			addListTab();
		}
	}

	public SupplierLayout() throws IOException {
		super("Proveedores");
		supplierBll = SupplierBll.getInstance();
		personBll = PersonBll.getInstance();
		payMethodBll = PaymentMethodBll.getInstance();
		payTypeBll = PaymentTypeBll.getInstance();
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

		cbPaymentType = new ComboBox<>("Tipo de pago");
		ListDataProvider<PaymentType> payTypeDataProv = new ListDataProvider<>(payTypeBll.selectAll());
		cbPaymentType.setDataProvider(payTypeDataProv);
		cbPaymentType.setItemCaptionGenerator(PaymentType::getName);
		cbPaymentType.setValue(supplier != null ? supplier.getPaymentType() : null);

		cbPaymentMethod = new ComboBox<>("Forma de pago");
		ListDataProvider<PaymentMethod> payMetDataProv = new ListDataProvider<>(payMethodBll.selectAll());
		cbPaymentMethod.setDataProvider(payMetDataProv);
		cbPaymentMethod.setItemCaptionGenerator(PaymentMethod::getName);
		cbPaymentMethod.setValue(supplier != null ? supplier.getPaymentMethod() : null);

		txtPaymentTerm = new TextField("Plazo");
		txtPaymentTerm.setValue(supplier != null ? supplier.getPaymentTerm() : "");

		FormLayout paymentForm = ViewHelper.buildForm("Datos para pagos", true, false);
		paymentForm.addComponents(cbPaymentType, cbPaymentMethod, txtPaymentTerm);
		Panel paymentPanel = ViewHelper.buildPanel("Datos para pagos", paymentForm);

		// 3. Transferencia bancaria

		cbAccountType = new ComboBox<>("Tipo de cuenta");
		ListDataProvider<BankAccountType> accTypeDataProv = new ListDataProvider<>(
				Arrays.asList(BankAccountType.values()));
		cbAccountType.setDataProvider(accTypeDataProv);
		cbAccountType.setItemCaptionGenerator(BankAccountType::getDisplay);

		txtAccountNumber = new TextField("Número de cuenta");
		// txtAccountNumber.setValue(supplier != null ? supplier.get() : "");

		cbBank = new ComboBox<>("Entidad financiera");

		cbAccountStatus = new ComboBox<>("Estado");

		FormLayout bankForm = ViewHelper.buildForm("Datos bancarios", false, false);
		bankForm.addComponents(cbPaymentType, txtAccountNumber, cbBank, cbAccountStatus);
		Panel bankPanel = ViewHelper.buildPanel("Datos bancarios", bankForm);
		// ----------------------------------------------------------------------------------

		layout.addComponents(basicPanel, paymentPanel, bankPanel);
		return layout;
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

		PaymentType paymentType = cbPaymentType.getSelectedItem().isPresent() ? cbPaymentType.getSelectedItem().get()
				: null;
		PaymentMethod paymentMethod = cbPaymentMethod.getSelectedItem().isPresent()
				? cbPaymentMethod.getSelectedItem().get()
				: null;

		BankAccountType accountType = cbAccountType.getSelectedItem().isPresent()
				? cbAccountType.getSelectedItem().get()
				: null;

		person = personBuilder.documentType(cbDocumentType.getValue()).documentNumber(txtDocumentId.getValue())
				.name(txtName.getValue()).lastName(lastName).build();
		supplier = supplierBuilder.person(person).paymentType(paymentType).paymentMethod(paymentMethod)
				.paymentTerm(txtPaymentTerm.getValue()).build();
		save(supplierBll, supplier, "Persona guardada");
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
