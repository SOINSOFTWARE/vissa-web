package com.soinsoftware.vissa.web;

import static com.soinsoftware.vissa.web.VissaUI.KEY_PRODUCTS;

import java.io.IOException;
import java.math.BigInteger;
import java.util.Date;
import java.util.Set;

import org.apache.log4j.Logger;
import org.vaadin.dialogs.ConfirmDialog;
import org.vaadin.ui.NumberField;

import com.soinsoftware.vissa.bll.MeasurementUnitBll;
import com.soinsoftware.vissa.bll.ProductBll;
import com.soinsoftware.vissa.bll.ProductCategoryBll;
import com.soinsoftware.vissa.bll.ProductTypeBll;
import com.soinsoftware.vissa.bll.TableSequenceBll;
import com.soinsoftware.vissa.model.MeasurementUnit;
import com.soinsoftware.vissa.model.Product;
import com.soinsoftware.vissa.model.ProductCategory;
import com.soinsoftware.vissa.model.ProductType;
import com.soinsoftware.vissa.model.TableSequence;
import com.soinsoftware.vissa.util.DateUtil;
import com.soinsoftware.vissa.util.ViewHelper;
import com.vaadin.data.provider.ConfigurableFilterDataProvider;
import com.vaadin.data.provider.ListDataProvider;
import com.vaadin.server.Page;
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
public class ProductLayout extends AbstractEditableLayout<Product> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5076502522106126046L;

	protected static final Logger log = Logger.getLogger(ProductLayout.class);

	private final ProductBll productBll;
	private final ProductCategoryBll categoryBll;
	private final ProductTypeBll typeBll;
	private final MeasurementUnitBll measurementUnitBll;
	private final TableSequenceBll tableSequenceBll;

	private Grid<Product> productGrid;

	private TextField txFilterByName;
	private TextField txFilterByCode;
	private TextField txtCode;
	private TextField txtName;
	private TextField txtDescription;
	private ComboBox<ProductCategory> cbCategory;
	private TextField txtEan;

	private ComboBox<ProductType> cbType;
	private ComboBox<MeasurementUnit> cbMeasurementUnit;

	private TextField txtBrand;
	private NumberField txtSalePrice;
	private NumberField txtPurchasePrice;
	private NumberField txtSaleTax;
	private NumberField txtPurchaseTax;
	private NumberField txtUtility;
	private NumberField txtSalePriceWithTax;
	private NumberField txtStock;
	private TextField txtStockDate;

	private boolean listMode;
	private TableSequence tableSequence;

	private ConfigurableFilterDataProvider<Product, Void, SerializablePredicate<Product>> filterProductDataProvider;

	public ProductLayout(boolean list) throws IOException {
		super("Productos", KEY_PRODUCTS);
		listMode = list;
		productBll = ProductBll.getInstance();
		categoryBll = ProductCategoryBll.getInstance();
		typeBll = ProductTypeBll.getInstance();
		measurementUnitBll = MeasurementUnitBll.getInstance();
		tableSequenceBll = TableSequenceBll.getInstance();
		if (listMode) {
			addListTab();
		}
	}

	public ProductLayout() throws IOException {
		super("Productos", KEY_PRODUCTS);
		productBll = ProductBll.getInstance();
		categoryBll = ProductCategoryBll.getInstance();
		typeBll = ProductTypeBll.getInstance();
		measurementUnitBll = MeasurementUnitBll.getInstance();
		tableSequenceBll = TableSequenceBll.getInstance();
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
		this.setMargin(false);
		this.setSpacing(false);
		return layout;
	}

	@Override
	protected AbstractOrderedLayout buildEditionView(Product entity) {
		VerticalLayout layout = ViewHelper.buildVerticalLayout(false, false);
		Panel buttonPanel = buildButtonPanelForEdition(entity);
		Component dataPanel = buildEditionComponent(entity);
		layout.addComponents(buttonPanel, dataPanel);
		return layout;
	}

	@Override
	protected Panel buildGridPanel() {
		VerticalLayout layout = ViewHelper.buildVerticalLayout(true, true);
		productGrid = ViewHelper.buildGrid(SelectionMode.SINGLE);
		productGrid.addColumn(Product::getCode).setCaption("Código");
		productGrid.addColumn(Product::getName).setCaption("Nombre");
		productGrid.addColumn(Product::getSalePrice).setCaption("Precio de venta");
		productGrid.addColumn(Product::getStock).setCaption("Stock");

		layout.addComponent(ViewHelper.buildPanel(null, productGrid));
		fillGridData();
		return ViewHelper.buildPanel(null, layout);
	}

	@Override
	protected Component buildEditionComponent(Product product) {
		// Cosultar consecutivo de productos
		getProductSequence();
		VerticalLayout layout = ViewHelper.buildVerticalLayout(true, true);
		/// 1. Informacion producto
		txtCode = new TextField("Código del producto");
		txtCode.setWidth("50%");
		txtCode.setReadOnly(true);

		txtName = new TextField("Nombre del producto");
		txtName.setWidth("50%");
		txtName.focus();

		txtDescription = new TextField("Descripción");
		txtDescription.setWidth("50%");

		cbCategory = new ComboBox<>("Categoría");
		cbCategory.setEmptySelectionCaption("Seleccione");
		cbCategory.setWidth("50%");
		cbCategory.setEmptySelectionAllowed(true);
		ListDataProvider<ProductCategory> categoryDataProv = new ListDataProvider<>(categoryBll.selectAll());
		cbCategory.setDataProvider(categoryDataProv);
		cbCategory.setItemCaptionGenerator(ProductCategory::getName);

		cbType = new ComboBox<>("Tipo de producto");
		cbType.setEmptySelectionCaption("Seleccione");
		cbType.setWidth("50%");
		cbType.setEmptySelectionAllowed(true);
		ListDataProvider<ProductType> typeDataProv = new ListDataProvider<>(typeBll.selectAll());
		cbType.setDataProvider(typeDataProv);
		cbType.setItemCaptionGenerator(ProductType::getName);

		cbMeasurementUnit = new ComboBox<>("Unidad de medida");
		cbMeasurementUnit.setEmptySelectionCaption("Seleccione");
		cbMeasurementUnit.setWidth("50%");
		cbMeasurementUnit.setDescription("Unidad de medida");
		cbMeasurementUnit.setEmptySelectionAllowed(true);
		ListDataProvider<MeasurementUnit> measurementDataProv = new ListDataProvider<>(measurementUnitBll.selectAll());
		cbMeasurementUnit.setDataProvider(measurementDataProv);
		cbMeasurementUnit.setItemCaptionGenerator(MeasurementUnit::getName);

		txtBrand = new TextField("Marca");
		txtBrand.setWidth("50%");

		txtEan = new TextField("EAN");
		txtEan.setWidth("50%");

		txtSalePrice = new NumberField("Precio de venta");
		txtSalePrice.setWidth("50%");
		txtSalePrice.setReadOnly(true);

		txtPurchasePrice = new NumberField("Precio de compra");
		txtPurchasePrice.setWidth("50%");
		txtPurchasePrice.setRequiredIndicatorVisible(true);

		txtSaleTax = new NumberField("IVA");
		txtSaleTax.setWidth("50%");
		txtSaleTax.setRequiredIndicatorVisible(true);

		txtPurchaseTax = new NumberField("Impuesto de compra");
		txtPurchaseTax.setWidth("50%");

		txtUtility = new NumberField("Utilidad");
		txtUtility.setWidth("50%");
		txtUtility.setRequiredIndicatorVisible(true);

		txtSalePriceWithTax = new NumberField("Precio de venta con impuesto");
		txtSalePriceWithTax.setWidth("50%");
		txtSalePriceWithTax.setReadOnly(true);

		// Product Stock
		txtStock = new NumberField("Stock");
		txtStock.setWidth("50%");

		txtStockDate = new TextField("Fecha actualización Stock");
		txtStockDate.setWidth("55%");
		txtStockDate.setReadOnly(true);

		setFieldValues(product);

		txtPurchasePrice.addValueChangeListener(e -> {
			updateSalePrice();
		});

		txtUtility.addValueChangeListener(e -> {
			updateSalePrice();
		});

		txtSalePriceWithTax.addValueChangeListener(e -> {
			updateSalePriceWithTax();
		});

		txtSalePrice.addValueChangeListener(e -> {
			updateSalePriceWithTax();
		});
		txtStock.addValueChangeListener(e -> {
			updateStockDate(txtStock.getValue());
		});
		// ----------------------------------------------------------------------------------

		final FormLayout form = new FormLayout();
		form.setMargin(true);
		form.setCaption("Datos del producto");
		form.setCaptionAsHtml(true);
		form.setSizeFull();
		form.setWidth("50%");
		form.addStyleName(ValoTheme.FORMLAYOUT_LIGHT);

		form.addComponents(txtCode, txtName, txtDescription, cbCategory, cbType, cbMeasurementUnit, txtEan,
				txtPurchasePrice, txtPurchaseTax, txtUtility, txtSalePrice, txtSaleTax, txtSalePriceWithTax, txtStock,
				txtStockDate);

		// ---Panel de lotes
		LotLayout lotPanel = null;

		try {
			lotPanel = new LotLayout(product);
			lotPanel.setCaption("Lotes");
			lotPanel.setMargin(false);
			lotPanel.setSpacing(false);
		} catch (IOException e) {
			log.error("Error al cargar lotes del producto. Exception: " + e);

		}

		layout.addComponents(form, lotPanel);
		return layout;
	}

	/**
	 * Metodo para establecer valores a los campos del formulario de productos
	 * 
	 * @param product
	 */

	private void setFieldValues(Product product) {
		if (product != null) {
			txtCode.setValue(product.getCode() != null ? product.getCode()
					: tableSequence != null ? String.valueOf(tableSequence.getSequence()) : "");

			txtName.setValue(product.getName());
			txtDescription.setValue(product.getDescription() != null ? product.getDescription() : "");
			cbCategory.setValue(product.getCategory() != null ? product.getCategory() : null);
			cbType.setValue(product.getType() != null ? product.getType() : null);
			cbMeasurementUnit.setValue(product.getMeasurementUnit() != null ? product.getMeasurementUnit() : null);
			txtBrand.setValue(product.getBrand() != null ? product.getBrand() : "");
			txtEan.setValue(product.getEanCode() != null ? product.getEanCode() : "");
			txtPurchasePrice
					.setValue(product.getPurchasePrice() != null ? String.valueOf(product.getPurchasePrice()) : "");

			txtSalePrice.setValue(product.getSalePrice() != null ? String.valueOf(product.getSalePrice()) : "");

			txtSaleTax.setValue(product.getSaleTax() != null ? String.valueOf(product.getSaleTax()) : "0");

			txtPurchaseTax.setValue(product.getPurchaseTax() != null ? String.valueOf(product.getPurchaseTax()) : "0");

			txtUtility.setValue(product.getUtility() != null ? String.valueOf(product.getUtility()) : "0");
			txtStock.setValue(product.getStock() != null ? String.valueOf(product.getStock()) : "");

			txtStockDate.setValue(product.getStockDate() != null ? DateUtil.dateToString(product.getStockDate()) : "");
			updateSalePriceWithTax();
		} else {

			txtPurchaseTax.setValue("0");
			txtSaleTax.setValue("0");
			txtUtility.setValue("0");
			txtSalePrice.setValue("0");
			txtSalePriceWithTax.setValue("0");
		}
	}

	/**
	 * Metodo para actualizar la fecha de actualización del stock
	 * 
	 * @param val
	 */
	private void updateStockDate(String val) {
		log.info("updateStockDate" + val);
		if (val != null && !val.isEmpty()) {
			txtStockDate.setValue(DateUtil.dateToString(new Date()));
		}
	}

	/**
	 * Actualizar el valor de venta de acuerdo a la utilidad
	 */
	private void updateSalePrice() {
		String strLog = "[updateSalePrice] ";
		try {

			String utility = txtUtility.getValue();
			String purchasePrice = txtPurchasePrice.getValue();

			if ((utility != null) && (purchasePrice != null)) {
				Double utilityValue = Double.parseDouble(utility);
				Double purchaseValue = Double.parseDouble(purchasePrice);
				Double saleValue = purchaseValue + utilityValue;
				txtSalePrice.setValue(String.valueOf(saleValue));
				log.info(strLog + "salePrice: " + saleValue);
			}
		} catch (Exception e) {
			log.error(strLog + "[Exception]" + e.getMessage());
			ViewHelper.showNotification("Error en los precios. Por favor verifique", Notification.Type.ERROR_MESSAGE);
		}
	}

	/**
	 * Actualizar el valor de venta con impuesto
	 */
	private void updateSalePriceWithTax() {
		String strLog = "[updateSalePriceWithTax] ";

		try {
			Double saleWithTaxValue = 0.0;
			String saleTaxStr = txtSaleTax.getValue();
			String salePriceStr = txtSalePrice.getValue();

			if ((saleTaxStr != null) && (salePriceStr != null)) {
				Double saleTaxValue = Double.parseDouble(saleTaxStr) / 100;
				Double salePriceValue = Double.parseDouble(salePriceStr);
				saleWithTaxValue = salePriceValue;
				if (!saleTaxValue.equals(0.0)) {
					saleWithTaxValue = salePriceValue * saleTaxValue;
				}
				txtSalePriceWithTax.setValue(String.valueOf(saleWithTaxValue));
				log.info(strLog + "saleWithTaxValue: " + saleWithTaxValue);
			}
		} catch (Exception e) {
			log.error(strLog + "[Exception]" + e.getMessage());
			ViewHelper.showNotification("Error en los precios. Por favor verifique", Notification.Type.ERROR_MESSAGE);
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

	/**
	 * Metodo para llenar la grid de productos
	 */
	@Override
	protected void fillGridData() {
		ListDataProvider<Product> dataProvider = new ListDataProvider<>(productBll.selectAll(false));
		filterProductDataProvider = dataProvider.withConfigurableFilter();
		productGrid.setDataProvider(filterProductDataProvider);

	}

	/**
	 * Metodo con la acción de guardar producto
	 */
	@Override
	protected void saveButtonAction(Product entity) {
		String message = validateRequiredFields();
		if (!message.isEmpty()) {
			ViewHelper.showNotification(message, Notification.Type.ERROR_MESSAGE);
		} else {
			ConfirmDialog.show(Page.getCurrent().getUI(), "Confirmar", "Está seguro de guardar el producto", "Si", "No",
					e -> {
						if (e.isConfirmed()) {
							saveProduct(entity);
						}
					});
		}
	}

	/**
	 * Metodo para validar los campos obligatorios para guardar una factura
	 * 
	 * @return
	 */
	private String validateRequiredFields() {
		String message = "";
		String character = "|";

		if (txtName.getValue() == null || txtName.getValue().isEmpty()) {
			if (!message.isEmpty()) {
				message = message.concat(character);
			} else {
				message = message.concat("El nombre obligatorio");
			}
		}
		if (txtPurchasePrice.getValue() == null) {
			if (!message.isEmpty()) {
				message = message.concat(character);
			} else {
				message = message.concat("El precio de compra es obligatorio");
			}
		}

		if (txtUtility.getValue() == null) {
			if (!message.isEmpty()) {
				message = message.concat(character);
			} else {
				message = message.concat("La utilidad es obligatoria");
			}
		}

		return message;
	}

	/**
	 * Metodo para guardar un producto
	 * 
	 * @param entity
	 */
	private void saveProduct(Product entity) {
		String strLog = "[saveProduct] ";
		try {
			Product.Builder productBuilder = null;
			if (entity == null) {
				productBuilder = Product.builder();
			} else {
				productBuilder = Product.builder(entity);
			}

			ProductCategory category = cbCategory.getSelectedItem().isPresent() ? cbCategory.getSelectedItem().get()
					: null;
			MeasurementUnit measurementUnit = cbMeasurementUnit.getSelectedItem().isPresent()
					? cbMeasurementUnit.getSelectedItem().get()
					: null;
			ProductType type = cbType.getSelectedItem().isPresent() ? cbType.getSelectedItem().get() : null;

			Double salePrice = txtSalePrice.getValue() != null && !txtSalePrice.getValue().isEmpty()
					? Double.parseDouble(txtSalePrice.getValue())
					: null;
			Double purchasePrice = txtPurchasePrice.getValue() != null && !txtPurchasePrice.isEmpty()
					? Double.parseDouble(txtPurchasePrice.getValue())
					: null;
			Double saleTax = txtSaleTax.getValue() != null && !txtSaleTax.getValue().isEmpty()
					? Double.parseDouble(txtSaleTax.getValue())
					: null;
			Double purchaseTax = txtPurchaseTax.getValue() != null && !txtPurchaseTax.getValue().isEmpty()
					? Double.parseDouble(txtPurchaseTax.getValue())
					: null;
			Double utility = txtUtility.getValue() != null && !txtUtility.getValue().isEmpty()
					? Double.parseDouble(txtUtility.getValue())
					: null;
			Double stock = txtStock.getValue() != null && !txtStock.isEmpty() ? Double.parseDouble(txtStock.getValue())
					: null;
			entity = productBuilder.code(txtCode.getValue()).name(txtName.getValue())
					.description(txtDescription.getValue()).category(category).type(type)
					.measurementUnit(measurementUnit).eanCode(txtEan.getValue()).salePrice(salePrice)
					.purchasePrice(purchasePrice).saleTax(saleTax).purchaseTax(purchaseTax).stock(stock)
					.stockDate(DateUtil.stringToDate(txtStockDate.getValue())).archived(false).utility(utility).build();
			save(productBll, entity, "Producto guardado");
			tableSequenceBll.save(tableSequence);
		} catch (Exception e) {
			log.error(strLog + "[Exception]" + e.getMessage());
			ViewHelper.showNotification("Se generó un error al guardar el producto", Notification.Type.ERROR_MESSAGE);
		}

	}

	@Override
	public Product getSelected() {
		Product prod = null;
		Set<Product> products = productGrid.getSelectedItems();
		if (products != null && !products.isEmpty()) {
			prod = (Product) products.toArray()[0];
		}
		return prod;
	}

	@Override
	protected void delete(Product entity) {
		entity = Product.builder(entity).archived(true).build();
		save(productBll, entity, "Produto borrado");
	}

	private Panel buildFilterPanel() {
		HorizontalLayout layout = ViewHelper.buildHorizontalLayout(true, true);

		txFilterByCode = new TextField("Código");
		txFilterByCode.focus();
		txFilterByCode.addStyleName(ValoTheme.TEXTFIELD_TINY);
		txFilterByCode.addValueChangeListener(e -> refreshGrid());

		txFilterByName = new TextField("Nombre");
		txFilterByName.addStyleName(ValoTheme.TEXTFIELD_TINY);
		txFilterByName.addValueChangeListener(e -> refreshGrid());
		layout.addComponents(txFilterByCode, txFilterByName);
		return ViewHelper.buildPanel("Filtrar por", layout);
	}

	private void refreshGrid() {
		filterProductDataProvider.setFilter(filterGrid());
		productGrid.getDataProvider().refreshAll();
	}

	private SerializablePredicate<Product> filterGrid() {
		SerializablePredicate<Product> columnPredicate = null;
		String codeFilter = txFilterByCode.getValue().trim();
		String nameFilter = txFilterByName.getValue().trim();
		columnPredicate = product -> (product.getName().toLowerCase().contains(nameFilter.toLowerCase())
				&& product.getCode().toLowerCase().contains(codeFilter.toLowerCase()));
		return columnPredicate;
	}

	private void getProductSequence() {
		BigInteger seq = null;
		TableSequence tableSeqObj = tableSequenceBll.select(Product.class.getSimpleName());
		if (tableSeqObj != null) {
			seq = tableSeqObj.getSequence().add(BigInteger.valueOf(1L));
			TableSequence.Builder builder = TableSequence.builder(tableSeqObj);
			tableSequence = builder.sequence(seq).build();
		} else {
			ViewHelper.showNotification("No hay consecutivo configurado para los productos",
					Notification.Type.ERROR_MESSAGE);
		}
	}
}
