package com.soinsoftware.vissa.web;

import com.soinsoftware.vissa.model.DocumentDetail;
import com.soinsoftware.vissa.model.InventoryTransaction;
import com.soinsoftware.vissa.model.Product;
import com.soinsoftware.vissa.util.ViewHelper;
import com.vaadin.ui.AbstractOrderedLayout;
import com.vaadin.ui.Component;
import com.vaadin.ui.Grid;
import com.vaadin.ui.Panel;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Grid.SelectionMode;

public class InventoryLayout extends AbstractEditableLayout<Product> {

	
	InventoryTransaction invTransaction;
	private Grid<InventoryTransaction> inventoryGrid; 
	
	public InventoryLayout() {
		super("Transacciones de inventario");
		// TODO Auto-generated constructor stub
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = -8569932867844792189L;

	@Override
	protected AbstractOrderedLayout buildListView() {
		VerticalLayout layout = ViewHelper.buildVerticalLayout(false, false);
		Panel buttonPanel = buildButtonPanelForLists(true);
	//	Panel filterPanel = buildFilterPanel();
		Panel dataPanel = buildGridPanel();
		//layout.addComponents(buttonPanel, filterPanel, dataPanel);
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
		InventoryTransaction inventoryTransaction = new InventoryTransaction();
		
		inventoryGrid.addColumn(invTransaction -> {
			if(invTransaction.getProduct() != null) {
				return invTransaction.getProduct().getCode();
			} else {
				return null;
			}
		}).setCaption("Código");
		
		inventoryGrid.addColumn(invTransaction -> {
			if(invTransaction.getProduct() != null) {
				return invTransaction.getProduct().getName();
			} else {
				return null;
			}
		}).setCaption("Código");
		
		inventoryGrid.addColumn(InventoryTransaction::getInventoryTransactionType).setCaption("Tipo de transacción");
		
		inventoryGrid.addColumn(invTransaction -> {
			if(invTransaction.getDocument() != null) {
				return invTransaction.getDocument().getDocumentDate();
			} else {
				return null;
			}
		}).setCaption("Fecha transacción");
	
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
		// TODO Auto-generated method stub
		
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

}
