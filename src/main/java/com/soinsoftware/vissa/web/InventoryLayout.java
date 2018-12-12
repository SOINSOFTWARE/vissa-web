package com.soinsoftware.vissa.web;

import com.soinsoftware.vissa.model.Product;
import com.vaadin.ui.AbstractOrderedLayout;
import com.vaadin.ui.Component;
import com.vaadin.ui.Panel;

public class InventoryLayout extends AbstractEditableLayout<Product> {

	public InventoryLayout(String pageTitle) {
		super(pageTitle);
		// TODO Auto-generated constructor stub
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = -8569932867844792189L;

	@Override
	protected AbstractOrderedLayout buildListView() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected AbstractOrderedLayout buildEditionView(Product entity) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected Panel buildGridPanel() {
		// TODO Auto-generated method stub
		return null;
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
