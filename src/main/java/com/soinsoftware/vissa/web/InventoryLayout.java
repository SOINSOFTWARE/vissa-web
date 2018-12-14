package com.soinsoftware.vissa.web;

import java.io.IOException;
import java.util.Arrays;
import java.util.Date;

import com.soinsoftware.vissa.bll.InventoryTransactionBll;
import com.soinsoftware.vissa.model.Document;
import com.soinsoftware.vissa.model.InventoryTransaction;
import com.soinsoftware.vissa.model.Product;
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

public class InventoryLayout extends AbstractEditableLayout<Product> {

	private final InventoryTransactionBll inventoryBll;
	
	InventoryTransaction invTransaction;
	private Grid<InventoryTransaction> inventoryGrid; 
	private TextField txtFilterByProdCode;
	private ConfigurableFilterDataProvider<InventoryTransaction, Void, SerializablePredicate<InventoryTransaction>> filterInvDataProvider;
	
	public InventoryLayout()  throws IOException {
		super("Transacciones de inventario");

		inventoryBll = InventoryTransactionBll.getInstance();
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = -8569932867844792189L;

	@Override
	protected AbstractOrderedLayout buildListView() {
		VerticalLayout layout = ViewHelper.buildVerticalLayout(false, false);
		Panel buttonPanel = buildButtonPanelForLists(true);
		Panel filterPanel = buildFilterPanel();
		Panel dataPanel = buildGridPanel();
		layout.addComponents( filterPanel, dataPanel);
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
		inventoryGrid = ViewHelper.buildGrid(SelectionMode.SINGLE);
		//InventoryTransaction inventoryTransaction = new InventoryTransaction();
		
		inventoryGrid.addColumn(invTransaction -> {
			if(invTransaction.getProduct() != null) {
				return invTransaction.getProduct().getCode();
			} else {
				return null;	
			}
		}).setCaption("Código producto");
		
		inventoryGrid.addColumn(invTransaction -> {
			if(invTransaction.getProduct() != null) {
				return invTransaction.getProduct().getName();
			} else {
				return null;
			}
		}).setCaption("Nombre producto");
		
		inventoryGrid.addColumn(InventoryTransaction::getTransactionType).setCaption("Tipo de transacción");
		
		inventoryGrid.addColumn(invTransaction -> {
			if(invTransaction.getDocument() != null) {
				return invTransaction.getDocument().getDocumentDate();
			} else {
				return null;
			}
		}).setCaption("Fecha transacción");
		
		inventoryGrid.addColumn(invTransaction -> {
			if(invTransaction.getDocument() != null) {
				return invTransaction.getDocument().getCode();
			} else {
				return null;
			}
		}).setCaption("Número de factura");
		
		inventoryGrid.addColumn(InventoryTransaction::getInitialStock).setCaption("Cantidad inicial");
		inventoryGrid.addColumn(InventoryTransaction::getQuantity).setCaption("Cantidad del movimiento");
		inventoryGrid.addColumn(InventoryTransaction::getInitialStock).setCaption("Cantidad final");
	
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
		Product  prod = new Product();
		Product.Builder builder =  Product.builder();
		prod = builder.code("REF1232").name("prodcto2").build();
		Document doc = new Document();
		Document.Builder builder2 =  Document.builder();
		doc = builder2.code("FV123").documentDate(new Date()).build();
		//InventoryTransaction invTx = new InventoryTransaction(prod, "E", doc, 50, 10, 40);
		ListDataProvider<InventoryTransaction> dataProvider = new ListDataProvider<>(inventoryBll.selectAll());
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
		columnPredicate = inventoryTransaction -> (inventoryTransaction.getProduct().getCode().toLowerCase().contains(txtFilterByProdCode.getValue().toLowerCase())
				|| txtFilterByProdCode.getValue().trim().isEmpty());
		return columnPredicate;
	}

}