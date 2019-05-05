package com.soinsoftware.vissa.web;

import static com.soinsoftware.vissa.web.VissaUI.KEY_MENUS;

import java.io.IOException;
import java.util.Set;

import org.apache.log4j.Logger;

import com.soinsoftware.vissa.bll.MenuBll;
import com.soinsoftware.vissa.model.Menu;
import com.soinsoftware.vissa.util.ViewHelper;
import com.vaadin.data.provider.ListDataProvider;
import com.vaadin.ui.AbstractOrderedLayout;
import com.vaadin.ui.Button;
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
public class MenuLayout extends AbstractEditableLayout<Menu> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5076502522106126046L;

	protected static final Logger log = Logger.getLogger(MenuLayout.class);

	// Bll
	private final MenuBll menuBll;

	// Component
	private Grid<Menu> grid;

	private TextField txFilterByName;
	private TextField txtName;

	// Entities

	private ListDataProvider<Menu> dataProvider;

	public MenuLayout() throws IOException {
		super("Menus", KEY_MENUS);
		menuBll = MenuBll.getInstance();

	}

	@Override
	protected AbstractOrderedLayout buildListView() {
		VerticalLayout layout = ViewHelper.buildVerticalLayout(false, false);
		Panel buttonPanel = null;
		buttonPanel = buildButtonPanelListMode();
		Panel filterPanel = buildFilterPanel();
		Panel dataPanel = buildGridPanel();
		layout.addComponents(buttonPanel, filterPanel, dataPanel);
		this.setMargin(false);
		this.setSpacing(false);
		return layout;
	}

	@Override
	protected AbstractOrderedLayout buildEditionView(Menu entity) {
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
		grid.addColumn(Menu::getName).setCaption("Nombre");

		layout.addComponent(ViewHelper.buildPanel(null, grid));
		fillGridData();
		return ViewHelper.buildPanel(null, layout);
	}

	@Override
	protected Component buildEditionComponent(Menu menu) {

		VerticalLayout layout = ViewHelper.buildVerticalLayout(true, true);
		txtName = new TextField("Nombre");
		txtName.setStyleName(ValoTheme.TEXTFIELD_SMALL);
		txtName.setRequiredIndicatorVisible(true);
		txtName.setValue(menu != null ? menu.getName() : "");

		FormLayout companyForm = ViewHelper.buildForm("Menu", false, false);
		companyForm.addComponent(txtName);
		layout.addComponent(companyForm);

		return layout;
	}

	protected Panel buildButtonPanelListMode() {
		HorizontalLayout layout = ViewHelper.buildHorizontalLayout(true, true);
		Button btNew = buildButtonForNewAction("mystyle-btn");
		Button btEdit = buildButtonForEditAction("mystyle-btn");
		Button btDelete = buildButtonForDeleteAction("mystyle-btn");
		layout.addComponents(btNew, btEdit, btDelete);
		return ViewHelper.buildPanel(null, layout);
	}

	@Override
	protected void fillGridData() {
		dataProvider = new ListDataProvider<>(menuBll.selectAll(false));
		grid.setDataProvider(dataProvider);
	}

	@Override
	protected void saveButtonAction(Menu entity) {
		Menu.Builder menuBuilder = null;
		if (entity == null) {
			menuBuilder = Menu.builder();
		} else {
			menuBuilder = Menu.builder(entity);
		}

		entity = menuBuilder.name(txtName.getValue()).archived(false).build();
		save(menuBll, entity, "Menu guardado");

	}

	@Override
	public Menu getSelected() {
		Menu menuObj = null;
		Set<Menu> menus = grid.getSelectedItems();
		if (menus != null && !menus.isEmpty()) {
			menuObj = (Menu) menus.toArray()[0];
		}
		return menuObj;
	}

	@Override
	protected void delete(Menu entity) {
		entity = Menu.builder(entity).archived(true).build();
		save(menuBll, entity, "Menu borrado");
	}

	/**
	 * Metodo para construir panel con campos para filtrar registros de la grid
	 * 
	 * @return
	 */
	private Panel buildFilterPanel() {
		HorizontalLayout layout = ViewHelper.buildHorizontalLayout(true, true);
		txFilterByName = new TextField("Nombre");
		txFilterByName.setStyleName(ValoTheme.TEXTFIELD_TINY);
		txFilterByName.addValueChangeListener(e -> refreshGrid());

		layout.addComponents(txFilterByName);
		return ViewHelper.buildPanel("Filtrar por", layout);
	}

	/**
	 * Metodo para refrescar registros de la grid
	 */
	private void refreshGrid() {
		if (dataProvider != null) {
			dataProvider.setFilter(menu -> filterGrid(menu));
		}
	}

	/**
	 * Metodo para filtrar registros de la grid
	 * 
	 * @param menu
	 * @return
	 */
	private boolean filterGrid(Menu menu) {

		boolean result = false;
		try {

			String nameFilter = txFilterByName != null ? txFilterByName.getValue() : "";

			result = menu.getName().toUpperCase().contains(nameFilter.toUpperCase());

		} catch (Exception e) {
			ViewHelper.showNotification(e.getMessage(), Notification.Type.WARNING_MESSAGE);
			e.printStackTrace();
		}
		return result;

	}

}
