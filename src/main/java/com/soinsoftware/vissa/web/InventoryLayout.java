package com.soinsoftware.vissa.web;

import static com.soinsoftware.vissa.web.VissaUI.KEY_INVENTORY;

import java.io.IOException;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import com.soinsoftware.vissa.bll.InventoryTransactionBll;
import com.soinsoftware.vissa.model.Collection;
import com.soinsoftware.vissa.model.InventoryTransaction;
import com.soinsoftware.vissa.model.Product;
import com.soinsoftware.vissa.util.DateUtil;
import com.soinsoftware.vissa.util.ViewHelper;
import com.vaadin.data.provider.ConfigurableFilterDataProvider;
import com.vaadin.data.provider.ListDataProvider;
import com.vaadin.server.SerializablePredicate;
import com.vaadin.ui.AbstractOrderedLayout;
import com.vaadin.ui.Component;
import com.vaadin.ui.Grid;
import com.vaadin.ui.Grid.SelectionMode;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Panel;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ValoTheme;

public class InventoryLayout extends AbstractEditableLayout<Product> {

	private final InventoryTransactionBll inventoryBll;

	InventoryTransaction invTransaction;
	private Grid<InventoryTransaction> inventoryGrid;
	private TextField txtFilterByProdCode;
	private ConfigurableFilterDataProvider<InventoryTransaction, Void, SerializablePredicate<InventoryTransaction>> filterInvDataProvider;

	public InventoryLayout() throws IOException {
		super("Transacciones de inventario", KEY_INVENTORY);

		inventoryBll = InventoryTransactionBll.getInstance();
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = -8569932867844792189L;

	@Override
	protected AbstractOrderedLayout buildListView() {
		VerticalLayout layout = ViewHelper.buildVerticalLayout(false, false);
		Panel buttonPanel = buildButtonPanelForLists();
		Panel filterPanel = buildFilterPanel();
		Panel dataPanel = buildGridPanel();
		layout.addComponents(filterPanel, dataPanel);
		this.setSpacing(false);
		this.setMargin(false);
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

	@SuppressWarnings("unchecked")
	@Override
	protected Panel buildGridPanel() {
		inventoryGrid = ViewHelper.buildGrid(SelectionMode.SINGLE);
		// InventoryTransaction inventoryTransaction = new InventoryTransaction();

		inventoryGrid.addColumn(invTransaction -> {
			if (invTransaction.getProduct() != null) {
				return invTransaction.getProduct().getCode();
			} else {
				return null;
			}
		}).setCaption("Código producto");

		inventoryGrid.addColumn(invTransaction -> {
			if (invTransaction.getProduct() != null) {
				return invTransaction.getProduct().getName();
			} else {
				return null;
			}
		}).setCaption("Nombre producto");

		inventoryGrid.addColumn(InventoryTransaction::getTransactionType).setCaption("Tipo de transacción");

		inventoryGrid.addColumn(invTransaction -> {
			if (invTransaction.getDocument() != null) {
				return DateUtil.dateToString(invTransaction.getDocument().getDocumentDate());
			} else {
				return null;
			}
		}).setCaption("Fecha transacción");

		inventoryGrid.addColumn(invTransaction -> {
			if (invTransaction.getDocument() != null) {
				return invTransaction.getDocument().getCode();
			} else {
				return null;
			}
		}).setCaption("Número de factura");

		inventoryGrid.addColumn(InventoryTransaction::getInitialStock).setCaption("Cantidad inicial");
		inventoryGrid.addColumn(InventoryTransaction::getQuantity).setCaption("Cantidad del movimiento");
		inventoryGrid.addColumn(InventoryTransaction::getFinalStock).setCaption("Cantidad final");

		fillGridData();
		return ViewHelper.buildPanel(null, inventoryGrid);
	}

	@Override
	protected Component buildEditionComponent(Product entity) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected void fillGridData() {
		List<InventoryTransaction> inventory = inventoryBll.selectAll();

		Comparator<InventoryTransaction> comparator = (h1, h2) -> h1.getDocument().getDocumentDate()
				.compareTo(h2.getDocument().getDocumentDate());

		inventory.sort(comparator.reversed());

		ListDataProvider<InventoryTransaction> dataProvider = new ListDataProvider<>(inventory);
		filterInvDataProvider = dataProvider.withConfigurableFilter();
		inventoryGrid.setDataProvider(filterInvDataProvider);

	}

	@Override
	protected void saveButtonAction(Product entity) {
		// TODO Auto-generated method stub

	}

	@Override
	protected Product getSelected() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected void delete(Product entity) {
		// TODO Auto-generated method stub

	}

	private Panel buildFilterPanel() {
		HorizontalLayout layout = ViewHelper.buildHorizontalLayout(true, true);
		txtFilterByProdCode = new TextField("Codigo Producto");
		txtFilterByProdCode.addStyleName(ValoTheme.TEXTFIELD_TINY);
		txtFilterByProdCode.addValueChangeListener(e -> refreshGrid());
		layout.addComponent(txtFilterByProdCode);
		return ViewHelper.buildPanel("Filtrar por", layout);
	}

	private void refreshGrid() {
		filterInvDataProvider.setFilter(filterGrid());
		inventoryGrid.getDataProvider().refreshAll();
	}

	private SerializablePredicate<InventoryTransaction> filterGrid() {
		SerializablePredicate<InventoryTransaction> columnPredicate = null;
		columnPredicate = inventoryTransaction -> (inventoryTransaction.getProduct().getCode().toLowerCase().contains(
				txtFilterByProdCode.getValue().toLowerCase()) || txtFilterByProdCode.getValue().trim().isEmpty());
		return columnPredicate;
	}

}
