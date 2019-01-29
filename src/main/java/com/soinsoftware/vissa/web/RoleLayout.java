package com.soinsoftware.vissa.web;

import static com.soinsoftware.vissa.web.VissaUI.KEY_WAREHOUSE;

import java.io.IOException;
import java.util.Set;

import org.apache.log4j.Logger;

import com.soinsoftware.vissa.bll.RoleBll;
import com.soinsoftware.vissa.bll.TableSequenceBll;
import com.soinsoftware.vissa.model.Role;
import com.soinsoftware.vissa.model.TableSequence;
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
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ValoTheme;

@SuppressWarnings("unchecked")
public class RoleLayout extends AbstractEditableLayout<Role> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5076502522106126046L;

	protected static final Logger log = Logger.getLogger(RoleLayout.class);

	private final RoleBll roleBll;

	private final TableSequenceBll tableSequenceBll;

	private Grid<Role> roleGrid;

	private TextField txFilterByName;
	private TextField txFilterByCode;
	private TextField txtCode;
	private TextField txtName;

	private boolean listMode;
	private TableSequence tableSequence;

	private ConfigurableFilterDataProvider<Role, Void, SerializablePredicate<Role>> filterProductDataProvider;

	public RoleLayout(boolean list) throws IOException {
		super("Roles", KEY_WAREHOUSE);
		listMode = list;
		roleBll = RoleBll.getInstance();
		tableSequenceBll = TableSequenceBll.getInstance();
		if (listMode) {
			addListTab();
		}
	}

	public RoleLayout() throws IOException {
		super("Roles", KEY_WAREHOUSE);
		roleBll = RoleBll.getInstance();
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
		// Panel filterPanel = buildFilterPanel();
		Panel dataPanel = buildGridPanel();
		layout.addComponents(buttonPanel, dataPanel);
		this.setMargin(false);
		this.setSpacing(false);
		return layout;
	}

	@Override
	protected AbstractOrderedLayout buildEditionView(Role entity) {
		VerticalLayout layout = ViewHelper.buildVerticalLayout(false, false);
		Panel buttonPanel = buildButtonPanelForEdition(entity);
		Component dataPanel = buildEditionComponent(entity);
		layout.addComponents(buttonPanel, dataPanel);
		return layout;
	}

	@Override
	protected Panel buildGridPanel() {
		VerticalLayout layout = ViewHelper.buildVerticalLayout(true, true);
		roleGrid = ViewHelper.buildGrid(SelectionMode.SINGLE);
		roleGrid.addColumn(Role::getName).setCaption("Nombre");

		layout.addComponent(ViewHelper.buildPanel(null, roleGrid));
		fillGridData();
		return ViewHelper.buildPanel(null, layout);
	}

	@Override
	protected Component buildEditionComponent(Role role) {

		VerticalLayout layout = ViewHelper.buildVerticalLayout(true, true);
		/// 1. Informacion rol
		txtName = new TextField("Nombre");
		txtName.setWidth("50%");
		txtName.focus();
		txtName.setValue(role != null ? role.getName() : "");

		// ----------------------------------------------------------------------------------

		final FormLayout form = new FormLayout();
		form.setMargin(true);
		form.setCaption("Datos del rol");
		form.setCaptionAsHtml(true);
		form.setSizeFull();
		form.setWidth("50%");
		form.addStyleName(ValoTheme.FORMLAYOUT_LIGHT);

		form.addComponents(txtName);

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
		ListDataProvider<Role> dataProvider = new ListDataProvider<>(roleBll.selectAll(false));
		filterProductDataProvider = dataProvider.withConfigurableFilter();
		roleGrid.setDataProvider(filterProductDataProvider);

	}

	@Override
	protected void saveButtonAction(Role entity) {
		Role.Builder warehouseBuilder = null;
		if (entity == null) {
			warehouseBuilder = Role.builder();
		} else {
			warehouseBuilder = Role.builder(entity);
		}

		entity = warehouseBuilder.name(txtName.getValue()).archived(false).build();
		save(roleBll, entity, "Rol guardado");
		tableSequenceBll.save(tableSequence);

	}

	@Override
	public Role getSelected() {
		Role roleObj = null;
		Set<Role> roles = roleGrid.getSelectedItems();
		if (roles != null && !roles.isEmpty()) {
			roleObj = (Role) roles.toArray()[0];
		}
		return roleObj;
	}

	@Override
	protected void delete(Role entity) {
		entity = Role.builder(entity).archived(true).build();
		save(roleBll, entity, "Rol borrado");
	}

	private Panel buildFilterPanel() {
		HorizontalLayout layout = ViewHelper.buildHorizontalLayout(true, true);
		txFilterByName = new TextField("Nombre");
		txFilterByName.addValueChangeListener(e -> refreshGrid());
		txFilterByCode = new TextField("Código");
		txFilterByCode.addValueChangeListener(e -> refreshGrid());
		layout.addComponents(txFilterByName);
		return ViewHelper.buildPanel("Filtrar por", layout);
	}

	private void refreshGrid() {
		filterProductDataProvider.setFilter(filterGrid());
		roleGrid.getDataProvider().refreshAll();
	}

	private SerializablePredicate<Role> filterGrid() {
		SerializablePredicate<Role> columnPredicate = null;
		String nameFilter = txFilterByName.getValue().trim();
		columnPredicate = role -> (role.getName().toLowerCase().contains(nameFilter.toLowerCase()));
		return columnPredicate;
	}

}
