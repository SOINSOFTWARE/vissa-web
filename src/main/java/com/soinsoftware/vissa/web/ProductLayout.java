package com.soinsoftware.vissa.web;

import java.io.IOException;
import java.util.Date;
import java.util.Set;

import org.apache.log4j.Logger;
import org.hibernate.HibernateException;

import com.soinsoftware.vissa.bll.MeasurementUnitBll;
import com.soinsoftware.vissa.bll.ProductBll;
import com.soinsoftware.vissa.bll.ProductCategoryBll;
import com.soinsoftware.vissa.bll.ProductStockBll;
import com.soinsoftware.vissa.bll.ProductTypeBll;
import com.soinsoftware.vissa.exception.ModelValidationException;
import com.soinsoftware.vissa.model.MeasurementUnit;
import com.soinsoftware.vissa.model.Product;
import com.soinsoftware.vissa.model.ProductCategory;
import com.soinsoftware.vissa.model.ProductStock;
import com.soinsoftware.vissa.model.ProductType;
import com.soinsoftware.vissa.util.ViewHelper;
import com.vaadin.data.provider.ConfigurableFilterDataProvider;
import com.vaadin.data.provider.ListDataProvider;
import com.vaadin.server.SerializablePredicate;
import com.vaadin.ui.AbstractOrderedLayout;
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
	private final ProductStockBll productStockBll;
	private final ProductCategoryBll categoryBll;
	private final ProductTypeBll typeBll;
	private final MeasurementUnitBll measurementUnitBll;

	private Grid<Product> productGrid;

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
	private TextField txtStock;
	private TextField txtStockDate;

	private ConfigurableFilterDataProvider<Product, Void, SerializablePredicate<Product>> filterProductDataProvider;

	public ProductLayout() throws IOException {
		super("Productos");
		productBll = ProductBll.getInstance();
		productStockBll = ProductStockBll.getInstance();
		categoryBll = ProductCategoryBll.getInstance();
		typeBll = ProductTypeBll.getInstance();
		measurementUnitBll = MeasurementUnitBll.getInstance();
	}

	@Override
	protected AbstractOrderedLayout buildListView() {
		VerticalLayout layout = ViewHelper.buildVerticalLayout(false, false);
		Panel buttonPanel = buildButtonPanelForLists();
		Panel filterPanel = buildFilterPanel();
		Panel dataPanel = buildGridPanel();
		layout.addComponents(buttonPanel, filterPanel, dataPanel);
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
		productGrid = ViewHelper.buildGrid(SelectionMode.SINGLE);
		productGrid.addColumn(Product::getCode).setCaption("Codigo");
		productGrid.addColumn(Product::getName).setCaption("Nombre");
		productGrid.addColumn(Product::getDescription).setCaption("Descripción");
		productGrid.addColumn(Product::getSalePrice).setCaption("Precio de venta");
		fillGridData();
		return ViewHelper.buildPanel(null, productGrid);
	}

	@Override
	protected Component buildEditionComponent(Product product) {
		VerticalLayout layout = ViewHelper.buildVerticalLayout(true, true);
		/// 1. Informacion producto
		txtCode = new TextField("Código del producto");
		txtCode.setWidth("50%");
		txtCode.setEnabled(false);
		txtCode.setValue(product != null ? product.getCode() : "");

		txtName = new TextField("Nombre del producto");
		txtName.setWidth("50%");
		txtName.setValue(product != null ? product.getName() : "");

		txtDescription = new TextField("Descripción");
		txtDescription.setWidth("50%");
		txtDescription.setValue(product != null && product.getDescription() != null ? product.getDescription() : "");

		cbCategory = new ComboBox<>("Categoría");
		cbCategory.setEmptySelectionCaption("Seleccione");
		cbCategory.setWidth("50%");
		cbCategory.setEmptySelectionAllowed(false);
		ListDataProvider<ProductCategory> categoryDataProv = new ListDataProvider<>(categoryBll.selectAll());
		cbCategory.setDataProvider(categoryDataProv);
		cbCategory.setItemCaptionGenerator(ProductCategory::getName);
		cbCategory.setValue(product != null ? product.getCategory() : null);

		cbType = new ComboBox<>("Tipo de producto");
		cbType.setWidth("50%");
		cbType.setEmptySelectionAllowed(false);
		ListDataProvider<ProductType> typeDataProv = new ListDataProvider<>(typeBll.selectAll());
		cbType.setDataProvider(typeDataProv);
		cbType.setItemCaptionGenerator(ProductType::getName);
		cbType.setValue(product != null ? product.getType() : null);

		cbMeasurementUnit = new ComboBox<>("Unidad de medida");
		cbMeasurementUnit.setWidth("50%");
		cbMeasurementUnit.setDescription("Unidad de medida");
		cbMeasurementUnit.setEmptySelectionAllowed(false);
		ListDataProvider<MeasurementUnit> measurementDataProv = new ListDataProvider<>(measurementUnitBll.selectAll());
		cbMeasurementUnit.setDataProvider(measurementDataProv);
		cbMeasurementUnit.setItemCaptionGenerator(MeasurementUnit::getName);
		cbMeasurementUnit.setValue(product != null ? product.getMeasurementUnit() : null);

		txtEan = new TextField("EAN");
		txtEan.setWidth("50%");
		txtEan.setValue(product != null && product.getEanCode() != null ? product.getEanCode() : "");

		txtSalePrice = new TextField("Precio de venta");
		txtSalePrice.setWidth("50%");
		txtSalePrice.setValue(
				product != null && product.getSalePrice() != null ? String.valueOf(product.getSalePrice()) : "");

		txtPurchasePrice = new TextField("Precio de compra");
		txtPurchasePrice.setWidth("50%");
		txtPurchasePrice.setValue(
				product != null && product.getPurchasePrice() != null ? String.valueOf(product.getPurchasePrice())
						: "");

		txtSaleTax = new TextField("Impuesto de venta");
		txtSaleTax.setWidth("50%");
		txtSaleTax
				.setValue(product != null && product.getSaleTax() != null ? String.valueOf(product.getSaleTax()) : "");

		txtPurchaseTax = new TextField("Impuesto de compra");
		txtPurchaseTax.setWidth("50%");
		txtPurchaseTax.setValue(
				product != null && product.getPurchaseTax() != null ? String.valueOf(product.getPurchaseTax()) : "");

		// Product Stock
		txtStock = new TextField("Stock");
		txtStock.setWidth("50%");
		ProductStock prodStock = null;
		if (product != null) {
			prodStock = productStockBll.select(product);
		}
		txtStock.setValue(
				prodStock != null && prodStock.getStock() != null ? String.valueOf(prodStock.getStock()) : "");

		txtStockDate = new TextField("Fecha actualización Stock");
		txtStockDate.setWidth("50%");
		txtStockDate.setEnabled(false);
		txtStockDate.setValue(
				prodStock != null && prodStock.getStock() != null ? String.valueOf(prodStock.getStockDate()) : "");

		// ----------------------------------------------------------------------------------

		final FormLayout form = new FormLayout();
		form.setMargin(true);
		form.setCaption("Datos del producto");
		form.setCaptionAsHtml(true);
		form.setSizeFull();
		form.setWidth("50%");
		form.addStyleName(ValoTheme.FORMLAYOUT_LIGHT);

		form.addComponents(txtCode, txtName, txtDescription, cbCategory, cbType, cbMeasurementUnit, txtSalePrice,
				txtPurchasePrice, txtSaleTax, txtPurchaseTax, txtStock, txtStockDate);

		// ---Panel de lotes
		LotLayout lotPanel = null;

		try {
			lotPanel = new LotLayout(product);
			lotPanel.setCaption("Lotes");
			lotPanel.setMargin(false);
			lotPanel.setSpacing(false);
		} catch (IOException e) {
			e.printStackTrace();
		}

		layout.addComponents(form, lotPanel);
		return layout;
	}

	@Override
	protected void fillGridData() {
		ListDataProvider<Product> dataProvider = new ListDataProvider<>(productBll.selectAll());
		filterProductDataProvider = dataProvider.withConfigurableFilter();
		productGrid.setDataProvider(filterProductDataProvider);

	}

	@Override
	protected void saveButtonAction(Product entity) {
		Product.Builder productBuilder = null;
		if (entity == null) {
			productBuilder = Product.builder();
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
				.archived(false).build();
		save(productBll, entity, "Producto guardado");
		
		//Actualizar stock de producto
		ProductStock stock = productStockBll.select(entity);
		saveStock(stock);
	}

	protected void saveStock(ProductStock entity) {
		ProductStock.Builder stockBuiler = null;
		if (entity == null) {
			stockBuiler = ProductStock.builder();
		} else {
			stockBuiler = ProductStock.builder(entity);
		}

		entity = stockBuiler.stock(Integer.parseInt(txtStock.getValue())).stockDate(new Date()).archived(false).build();

		try {
			productStockBll.save(entity);

		} catch (ModelValidationException ex) {
			log.error(ex);
			ViewHelper.showNotification(ex.getMessage(), Notification.Type.ERROR_MESSAGE);
		} catch (HibernateException ex) {
			log.error(ex);
			// bll.rollback();
			ViewHelper.showNotification("Los datos no pudieron ser salvados, contacte al administrador (3007200405)",
					Notification.Type.ERROR_MESSAGE);
		}

	}

	@Override
	protected Product getSelected() {
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
		txFilterByName = new TextField("Nombre");
		txFilterByName.addValueChangeListener(e -> refreshGrid());
		layout.addComponent(txFilterByName);
		return ViewHelper.buildPanel("Filtrar por", layout);
	}

	private void refreshGrid() {
		filterProductDataProvider.setFilter(filterGrid());
		productGrid.getDataProvider().refreshAll();
	}

	private SerializablePredicate<Product> filterGrid() {
		SerializablePredicate<Product> columnPredicate = null;
		columnPredicate = product -> (product.getName().toLowerCase().contains(txFilterByName.getValue().toLowerCase())
				|| txFilterByName.getValue().trim().isEmpty());
		return columnPredicate;
	}

}
