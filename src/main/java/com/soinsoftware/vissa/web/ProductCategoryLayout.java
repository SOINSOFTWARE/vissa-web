package com.soinsoftware.vissa.web;

import static com.soinsoftware.vissa.web.VissaUI.KEY_PRODUCT_CATEGORY;

import java.io.IOException;
import java.util.Set;

import org.apache.log4j.Logger;

import com.soinsoftware.vissa.bll.ProductCategoryBll;
import com.soinsoftware.vissa.model.ProductCategory;
import com.soinsoftware.vissa.util.ViewHelper;
import com.vaadin.data.provider.ConfigurableFilterDataProvider;
import com.vaadin.data.provider.ListDataProvider;
import com.vaadin.server.SerializablePredicate;
import com.vaadin.ui.AbstractOrderedLayout;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.FormLayout;
import com.vaadin.ui.Grid;
import com.vaadin.ui.Grid.SelectionMode;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Panel;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ValoTheme;

public class ProductCategoryLayout extends AbstractEditableLayout<ProductCategory> {

	private static final long serialVersionUID = -9123527053553224244L;

	protected static final Logger log = Logger.getLogger(ProductCategoryLayout.class);

	private final ProductCategoryBll productCategoryBll;

	private Grid<ProductCategory> productCategoryGrid;

	private TextField txFilterByName;
	private TextField txtName;
	private TextArea txaDescription;

	private ConfigurableFilterDataProvider<ProductCategory, Void, SerializablePredicate<ProductCategory>> filterDataProvider;

	public ProductCategoryLayout() throws IOException {
		super("Dependencias", KEY_PRODUCT_CATEGORY);
		productCategoryBll = ProductCategoryBll.getInstance();
	}

	@Override
	protected AbstractOrderedLayout buildListView() {
		VerticalLayout layout = ViewHelper.buildVerticalLayout(false, false);
		Panel buttonPanel = buildButtonPanelForLists();
		Panel filterPanel = buildFilterPanel();
		Panel dataPanel = buildGridPanel();
		layout.addComponents(buttonPanel, filterPanel, dataPanel);
		this.setMargin(false);
		this.setSpacing(false);
		return layout;
	}

	@Override
	protected AbstractOrderedLayout buildEditionView(ProductCategory entity) {
		VerticalLayout layout = ViewHelper.buildVerticalLayout(false, false);
		Panel buttonPanel = buildButtonPanelForEdition(entity);
		Component dataPanel = buildEditionComponent(entity);
		layout.addComponents(buttonPanel, dataPanel);
		return layout;
	}

	@SuppressWarnings("unchecked")
	@Override
	protected Panel buildGridPanel() {
		VerticalLayout layout = ViewHelper.buildVerticalLayout(true, true);
		productCategoryGrid = ViewHelper.buildGrid(SelectionMode.SINGLE);
		productCategoryGrid.addColumn(ProductCategory::getName).setCaption("Nombre");
		productCategoryGrid.addColumn(ProductCategory::getDescription).setCaption("Descripción");

		layout.addComponent(ViewHelper.buildPanel(null, productCategoryGrid));
		fillGridData();
		return ViewHelper.buildPanel(null, layout);
	}

	@Override
	protected Component buildEditionComponent(ProductCategory productCategory) {
		VerticalLayout layout = ViewHelper.buildVerticalLayout(true, true);

		txtName = new TextField("Nombre");
		txtName.setWidth("50%");
		txtName.setValue(productCategory != null ? productCategory.getName() : "");
		
		txaDescription = new TextArea("Descripción");
		txaDescription.setWidth("50%");
		txaDescription.setValue(productCategory != null ? productCategory.getDescription() : "");

		final FormLayout form = new FormLayout();
		form.setMargin(true);
		form.setCaption("Categorias");
		form.setCaptionAsHtml(true);
		form.setSizeFull();
		form.setWidth("50%");
		form.addStyleName(ValoTheme.FORMLAYOUT_LIGHT);

		form.addComponents(txtName, txaDescription);

		layout.addComponents(form);
		return layout;
	}

	protected Panel buildButtonPanelListMode() {
		HorizontalLayout layout = ViewHelper.buildHorizontalLayout(true, true);
		Button btNew = buildButtonForNewAction("");
		Button btEdit = buildButtonForEditAction("mystyle-btn");
		Button btDelete = buildButtonForDeleteAction("mystyle-btn");
		layout.addComponents(btNew, btEdit, btDelete);
		return ViewHelper.buildPanel(null, layout);
	}

	@Override
	protected void fillGridData() {
		ListDataProvider<ProductCategory> dataProvider = new ListDataProvider<>(productCategoryBll.selectAll(false));
		filterDataProvider = dataProvider.withConfigurableFilter();
		productCategoryGrid.setDataProvider(filterDataProvider);

	}

	@Override
	protected void saveButtonAction(ProductCategory entity) {
		ProductCategory.Builder builder = null;
		if (entity == null) {
			builder = ProductCategory.builder();
		} else {
			builder = ProductCategory.builder(entity);
		}

		entity = builder.name(txtName.getValue()).description(txaDescription.getValue()).build();
		save(productCategoryBll, entity, "Dependencia guardada");
	}

	@Override
	public ProductCategory getSelected() {
		ProductCategory productCategory = null;
		Set<ProductCategory> productCategories = productCategoryGrid.getSelectedItems();
		if (productCategories != null && !productCategories.isEmpty()) {
			productCategory = (ProductCategory) productCategories.toArray()[0];
		}
		return productCategory;
	}

	@Override
	protected void delete(ProductCategory entity) {
		save(productCategoryBll,  ProductCategory.builder(entity).archived(true).build(), "Dependencia borrada");
	}

	private Panel buildFilterPanel() {
		HorizontalLayout layout = ViewHelper.buildHorizontalLayout(true, true);
		txFilterByName = new TextField("Nombre");
		txFilterByName.addValueChangeListener(e -> refreshGrid());
		layout.addComponents(txFilterByName);
		return ViewHelper.buildPanel("Filtrar por", layout);
	}

	private void refreshGrid() {
		filterDataProvider.setFilter(filterGrid());
		productCategoryGrid.getDataProvider().refreshAll();
	}

	private SerializablePredicate<ProductCategory> filterGrid() {
		SerializablePredicate<ProductCategory> columnPredicate = null;
		String nameFilter = txFilterByName.getValue().trim();
		columnPredicate = productCategory -> (productCategory.getName().toLowerCase().contains(nameFilter.toLowerCase()));
		return columnPredicate;
	}
}