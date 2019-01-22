package com.soinsoftware.vissa.web;

import java.io.IOException;
import java.math.BigInteger;
import java.util.Date;
import java.util.Set;

import org.apache.log4j.Logger;

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

	private TextField txtSalePrice;
	private TextField txtPurchasePrice;
	private TextField txtSaleTax;
	private TextField txtPurchaseTax;
	private TextField txtStock;
	private TextField txtStockDate;
	private boolean listMode;
	private TableSequence tableSequence;

	private ConfigurableFilterDataProvider<Product, Void, SerializablePredicate<Product>> filterProductDataProvider;

	public ProductLayout(boolean list) throws IOException {
		super("Productos");
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
		super("Productos");
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
		txtCode.setEnabled(false);
		txtCode.setValue(product != null ? product.getCode()
				: tableSequence != null ? String.valueOf(tableSequence.getSequence()) : "");

		txtName = new TextField("Nombre del producto");
		txtName.setWidth("50%");
		txtName.focus();
		txtName.setValue(product != null ? product.getName() : "");		

		txtDescription = new TextField("Descripción");
		txtDescription.setWidth("50%");
		txtDescription.setValue(product != null && product.getDescription() != null ? product.getDescription() : "");

		cbCategory = new ComboBox<>("Categoría");
		cbCategory.setEmptySelectionCaption("Seleccione");
		cbCategory.setWidth("50%");
		cbCategory.setEmptySelectionAllowed(true);
		ListDataProvider<ProductCategory> categoryDataProv = new ListDataProvider<>(categoryBll.selectAll());
		cbCategory.setDataProvider(categoryDataProv);
		cbCategory.setItemCaptionGenerator(ProductCategory::getName);
		cbCategory.setValue(product != null ? product.getCategory() : null);

		cbType = new ComboBox<>("Tipo de producto");
		cbType.setEmptySelectionCaption("Seleccione");
		cbType.setWidth("50%");
		cbType.setEmptySelectionAllowed(true);
		ListDataProvider<ProductType> typeDataProv = new ListDataProvider<>(typeBll.selectAll());
		cbType.setDataProvider(typeDataProv);
		cbType.setItemCaptionGenerator(ProductType::getName);
		cbType.setValue(product != null ? product.getType() : null);

		cbMeasurementUnit = new ComboBox<>("Unidad de medida");
		cbMeasurementUnit.setEmptySelectionCaption("Seleccione");
		cbMeasurementUnit.setWidth("50%");
		cbMeasurementUnit.setDescription("Unidad de medida");
		cbMeasurementUnit.setEmptySelectionAllowed(true);
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
		txtStock.setValue(product != null && product.getStock() != null ? String.valueOf(product.getStock()) : "");

		txtStockDate = new TextField("Fecha actualización Stock");
		txtStockDate.setWidth("50%");
		txtStockDate.setEnabled(false);
		
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
				txtSalePrice, txtPurchasePrice, txtSaleTax, txtPurchaseTax, txtStock, txtStockDate);

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

	private void updateStockDate(String val) {
		log.info("updateStockDate" + val);
		if (val != null && !val.isEmpty()) {
			txtStockDate.setValue(DateUtil.dateToString(new Date()));
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
		ListDataProvider<Product> dataProvider = new ListDataProvider<>(productBll.selectAll(false));
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

		ProductCategory category = cbCategory.getSelectedItem().isPresent() ? cbCategory.getSelectedItem().get() : null;
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
		Integer stock = txtStock.getValue() != null && !txtStock.isEmpty() ? Integer.parseInt(txtStock.getValue())
				: null;
		entity = productBuilder.code(txtCode.getValue()).name(txtName.getValue()).description(txtDescription.getValue())
				.category(category).type(type).measurementUnit(measurementUnit).eanCode(txtEan.getValue())
				.salePrice(salePrice).purchasePrice(purchasePrice).saleTax(saleTax).purchaseTax(purchaseTax)
				.stock(stock).stockDate(DateUtil.stringToDate(txtStockDate.getValue())).archived(false).build();
		save(productBll, entity, "Producto guardado");
		tableSequenceBll.save(tableSequence);

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
		txFilterByCode.addValueChangeListener(e -> refreshGrid());
		
		txFilterByName = new TextField("Nombre");
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
