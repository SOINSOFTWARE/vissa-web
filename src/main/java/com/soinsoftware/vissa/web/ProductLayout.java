package com.soinsoftware.vissa.web;

import java.io.IOException;
import java.util.Set;

import com.soinsoftware.vissa.bll.MeasurementUnitBll;
import com.soinsoftware.vissa.bll.ProductBll;
import com.soinsoftware.vissa.bll.ProductCategoryBll;
import com.soinsoftware.vissa.bll.ProductTypeBll;
import com.soinsoftware.vissa.model.MeasurementUnit;
import com.soinsoftware.vissa.model.Person;
import com.soinsoftware.vissa.model.Product;
import com.soinsoftware.vissa.model.ProductCategory;
import com.soinsoftware.vissa.model.ProductType;
import com.soinsoftware.vissa.util.ViewHelper;
import com.vaadin.data.provider.ConfigurableFilterDataProvider;
import com.vaadin.data.provider.ListDataProvider;
import com.vaadin.server.SerializablePredicate;
import com.vaadin.ui.AbstractOrderedLayout;
import com.vaadin.ui.Button;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.FormLayout;
import com.vaadin.ui.Grid;
import com.vaadin.ui.Grid.SelectionMode;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Panel;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ValoTheme;

public class ProductLayout extends AbstractEditableLayout<Product> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5076502522106126046L;

	private final ProductBll productBll;
	private final ProductCategoryBll categoryBll;
	private final ProductTypeBll typeBll;
	private final MeasurementUnitBll measurementUnitBll;

	private Grid<Product> grid;

	private TextField txFilterByName;
	private TextField txtCode;
	private TextField txtName;
	private TextField txtDescription;
	private ComboBox<ProductCategory> cbCategory;
	private TextField txtEan;

	private ComboBox<ProductType> cbType;
	private ComboBox<MeasurementUnit> cbMeasurementUnit;
	private TextField txtSalePrice;
	private TextField txtPurchasePrice;
	private TextField txtSaleTax;
	private TextField txtPurchaseTax;

	Product product = new Product();
	private ConfigurableFilterDataProvider<Product, Void, SerializablePredicate<Product>> filterDataProvider;
	private ConfigurableFilterDataProvider<Product, Void, SerializablePredicate<Person>> filterProductDataProv;

	public ProductLayout() throws IOException {
		super("Productos");
		productBll = ProductBll.getInstance();
		categoryBll = ProductCategoryBll.getInstance();
		typeBll = ProductTypeBll.getInstance();
		measurementUnitBll = MeasurementUnitBll.getInstance();

	}

	@Override
	protected AbstractOrderedLayout buildListView() {
		VerticalLayout layout = ViewHelper.buildVerticalLayout(false, false);
		Panel buttonPanel = buildButtonPanelForLists(true);
		Panel filterPanel = buildFilterPanel();
		Panel dataPanel = buildGridPanel();
		layout.addComponents(buttonPanel, filterPanel, dataPanel);
		return layout;
	}

	@Override
	protected AbstractOrderedLayout buildEditionView(Product entity) {
		VerticalLayout layout = ViewHelper.buildVerticalLayout(false, false);
		Panel buttonPanel = buildButtonPanelForEdition(entity);
		Panel dataPanel = buildEditionPanel(entity);
		layout.addComponents(buttonPanel, dataPanel);
		return layout;
	}

	@Override
	protected Panel buildGridPanel() {
		grid = ViewHelper.buildGrid(SelectionMode.SINGLE);
		grid.addColumn(Product::getCode).setCaption("Codigo");
		grid.addColumn(Product::getName).setCaption("Nombre");
		grid.addColumn(Product::getDescription).setCaption("Descripción");
		grid.addColumn(Product::getSalePrice).setCaption("Precio de venta");
		fillGridData();
		return ViewHelper.buildPanel(null, grid);
	}

	@Override
	protected Panel buildEditionPanel(Product product) {
		/// 1. Informacion producto
		txtCode = new TextField("Código del producto");
		txtCode.setValue(product != null ? product.getCode() : "");
		txtName = new TextField("Nombre del producto");
		txtName.setValue(product != null ? product.getName() : "");
		txtDescription = new TextField("Descripción");
		System.out.println("desc="+product.getDescription());
		txtDescription.setValue(product != null && product.getDescription() != null ? product.getDescription() : "");

		cbCategory = new ComboBox<>("Categoría");
		cbCategory.setDescription("Categoría");
		cbCategory.setEmptySelectionAllowed(false);
		ListDataProvider<ProductCategory> categoryDataProv = new ListDataProvider<>(categoryBll.selectAll());
		cbCategory.setDataProvider(categoryDataProv);
		cbCategory.setItemCaptionGenerator(ProductCategory::getName);
		cbCategory.setValue(product != null ? product.getCategory() : null);

		cbType = new ComboBox<>("Tipo de producto");
		cbType.setDescription("Tipo de producto");
		cbType.setEmptySelectionAllowed(false);
		ListDataProvider<ProductType> typeDataProv = new ListDataProvider<>(typeBll.selectAll());
		cbType.setDataProvider(typeDataProv);
		cbType.setItemCaptionGenerator(ProductType::getName);
		cbType.setValue(product != null ? product.getType() : null);

		cbMeasurementUnit = new ComboBox<>("Unidad de medida");
		cbMeasurementUnit.setDescription("Unidad de medida");
		cbMeasurementUnit.setEmptySelectionAllowed(false);
		ListDataProvider<MeasurementUnit> measurementDataProv = new ListDataProvider<>(measurementUnitBll.selectAll());
		cbMeasurementUnit.setDataProvider(measurementDataProv);
		cbMeasurementUnit.setItemCaptionGenerator(MeasurementUnit::getName);
		cbMeasurementUnit.setValue(product != null ? product.getMeasurementUnit() : null);

		txtEan = new TextField("EAN");
		txtEan.setValue(product != null ? product.getEanCode() : "");

		txtSalePrice = new TextField("Precio de venta");
		String salePrice = product.getSalePrice() != null ? String.valueOf(product.getSalePrice()) : "";
		txtSalePrice.setValue(product != null ? salePrice : "");
		
		txtPurchasePrice = new TextField("Precio de compra");
		String purchasePrice = product.getPurchasePrice() != null ? String.valueOf(product.getPurchasePrice()) : "";
		txtPurchasePrice.setValue(product != null ? purchasePrice : "");
		
		txtSaleTax = new TextField("Impuesto de venta");
		String saleTax = product.getSaleTax() != null ? String.valueOf(product.getSaleTax()) : "";
		txtSaleTax.setValue(product != null ? saleTax : "");
		
		txtPurchaseTax = new TextField("Impuesto de compra");
		String purchaseTax = product.getPurchaseTax() != null ? String.valueOf(product.getPurchaseTax()) : "";
		txtPurchaseTax.setValue(product != null ? purchaseTax : "");

		// -------------------------------------------------------------------------

		final FormLayout form = new FormLayout();
		form.setMargin(false);
		form.setWidth("800px");
		form.addStyleName(ValoTheme.FORMLAYOUT_LIGHT);

		form.addComponents(txtCode, txtName, txtDescription, cbCategory, cbType, cbMeasurementUnit, txtSalePrice,
				txtPurchasePrice, txtSaleTax, txtPurchaseTax);

		VerticalLayout layout = ViewHelper.buildVerticalLayout(true, true);
		layout.setWidth("40%");
		layout.addComponent(form);
		return ViewHelper.buildPanel(null, layout);
	}

	@Override
	protected void fillGridData() {
		ListDataProvider<Product> dataProvider = new ListDataProvider<>(productBll.selectAll());
		filterDataProvider = dataProvider.withConfigurableFilter();
		grid.setDataProvider(filterDataProvider);

	}

	@Override
	protected void saveButtonAction(Product entity) {
		Product.Builder productBuilder = null;
		if (entity == null) {
		} else {
			productBuilder = Product.builder(entity);
		}
		Double salePrice = txtSalePrice.getValue() != null && txtSalePrice.getValue() != ""
				? Double.parseDouble(txtSalePrice.getValue())
				: null;
		Double purchasePrice = txtPurchasePrice.getValue() != null && txtPurchasePrice.getValue() != ""
				? Double.parseDouble(txtPurchasePrice.getValue())
				: null;
		Double saleTax = txtSaleTax.getValue() != null && txtSaleTax.getValue() != ""
				? Double.parseDouble(txtSaleTax.getValue())
				: null;
		Double purchaseTax = txtPurchaseTax.getValue() != null && txtPurchaseTax.getValue() != ""
				? Double.parseDouble(txtPurchaseTax.getValue())
				: null;
		entity = productBuilder.code(txtCode.getValue()).name(txtName.getValue()).description(txtDescription.getValue())
				.category(cbCategory.getSelectedItem().get()).type(cbType.getSelectedItem().get())
				.measurementUnit(cbMeasurementUnit.getSelectedItem().get()).eanCode(txtEan.getValue())
				.salePrice(salePrice).purchasePrice(purchasePrice).saleTax(saleTax).purchaseTax(purchaseTax)
				.archived(true).build();
		save(productBll, entity, "Producto guardado");
	}

	@Override
	protected Product getSelected() {
		Product product = null;
		Set<Product> products = grid.getSelectedItems();
		if (products != null && !products.isEmpty()) {
			product = (Product) products.toArray()[0];
		}
		return product;
	}

	@Override
	protected void delete(Product entity) {
		entity = Product.builder(entity).archived(false).build();
		save(productBll, entity, "Produto borrado");
	}

	protected Panel buildButtonPanelForLists(boolean validateCompany) {
		HorizontalLayout layout = ViewHelper.buildHorizontalLayout(true, true);
		Button btNew = buildButtonForNewAction();
		Button btEdit = buildButtonForEditAction(validateCompany);
		Button btDelete = buildButtonForDeleteAction(validateCompany);
		layout.addComponents(btNew, btEdit, btDelete);
		return ViewHelper.buildPanel(null, layout);
	}

	private Panel buildFilterPanel() {
		HorizontalLayout layout = ViewHelper.buildHorizontalLayout(true, true);
		txFilterByName = new TextField("Nombre");
		txFilterByName.addValueChangeListener(e -> refreshGrid());
		layout.addComponent(txFilterByName);
		return ViewHelper.buildPanel("Filtrar por", layout);
	}

	private void refreshGrid() {
		filterDataProvider.setFilter(filterGrid());
		grid.getDataProvider().refreshAll();
	}

	private SerializablePredicate<Product> filterGrid() {
		SerializablePredicate<Product> columnPredicate = null;
		columnPredicate = product -> (product.getName().toLowerCase().contains(txFilterByName.getValue().toLowerCase())
				|| txFilterByName.getValue().trim().isEmpty());
		return columnPredicate;
	}
}
