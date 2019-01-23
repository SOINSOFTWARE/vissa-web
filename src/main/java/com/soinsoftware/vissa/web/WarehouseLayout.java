package com.soinsoftware.vissa.web;

import static com.soinsoftware.vissa.web.VissaUI.KEY_WAREHOUSE;

import java.io.IOException;
import java.math.BigInteger;
import java.util.Set;

import org.apache.log4j.Logger;

import com.soinsoftware.vissa.bll.TableSequenceBll;
import com.soinsoftware.vissa.bll.WarehouseBll;
import com.soinsoftware.vissa.model.TableSequence;
import com.soinsoftware.vissa.model.Warehouse;
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
import com.vaadin.ui.Notification;
import com.vaadin.ui.Panel;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ValoTheme;

@SuppressWarnings("unchecked")
public class WarehouseLayout extends AbstractEditableLayout<Warehouse> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5076502522106126046L;

	protected static final Logger log = Logger.getLogger(WarehouseLayout.class);

	private final WarehouseBll warehouseBll;

	private final TableSequenceBll tableSequenceBll;

	private Grid<Warehouse> warehouseGrid;

	private TextField txFilterByName;
	private TextField txFilterByCode;
	private TextField txtCode;
	private TextField txtName;

	private boolean listMode;
	private TableSequence tableSequence;

	private ConfigurableFilterDataProvider<Warehouse, Void, SerializablePredicate<Warehouse>> filterProductDataProvider;

	public WarehouseLayout(boolean list) throws IOException {
		super("Bodegas", KEY_WAREHOUSE);
		listMode = list;
		warehouseBll = WarehouseBll.getInstance();
		tableSequenceBll = TableSequenceBll.getInstance();
		if (listMode) {
			addListTab();
		}
	}

	public WarehouseLayout() throws IOException {
		super("Bodegas", KEY_WAREHOUSE);
		warehouseBll = WarehouseBll.getInstance();
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
	protected AbstractOrderedLayout buildEditionView(Warehouse entity) {
		VerticalLayout layout = ViewHelper.buildVerticalLayout(false, false);
		Panel buttonPanel = buildButtonPanelForEdition(entity);
		Component dataPanel = buildEditionComponent(entity);
		layout.addComponents(buttonPanel, dataPanel);
		return layout;
	}

	@Override
	protected Panel buildGridPanel() {
		VerticalLayout layout = ViewHelper.buildVerticalLayout(true, true);
		warehouseGrid = ViewHelper.buildGrid(SelectionMode.SINGLE);
		warehouseGrid.addColumn(Warehouse::getCode).setCaption("Código");
		warehouseGrid.addColumn(Warehouse::getName).setCaption("Nombre");

		layout.addComponent(ViewHelper.buildPanel(null, warehouseGrid));
		fillGridData();
		return ViewHelper.buildPanel(null, layout);
	}

	@Override
	protected Component buildEditionComponent(Warehouse warehouse) {
		// Cosultar consecutivo de productos
		getSequence();
		VerticalLayout layout = ViewHelper.buildVerticalLayout(true, true);
		/// 1. Informacion producto
		txtCode = new TextField("Código de bodega");
		txtCode.setWidth("50%");
		txtCode.setEnabled(false);

		txtCode.setValue(warehouse != null ? warehouse.getCode()
				: tableSequence != null ? String.valueOf(tableSequence.getSequence()) : "");

		txtName = new TextField("Nombre de bodega");
		txtName.setWidth("50%");
		txtName.focus();
		txtName.setValue(warehouse != null ? warehouse.getName() : "");

		// ----------------------------------------------------------------------------------

		final FormLayout form = new FormLayout();
		form.setMargin(true);
		form.setCaption("Datos del producto");
		form.setCaptionAsHtml(true);
		form.setSizeFull();
		form.setWidth("50%");
		form.addStyleName(ValoTheme.FORMLAYOUT_LIGHT);

		form.addComponents(txtCode, txtName);
		// ---Panel de lotes
		LotLayout lotPanel = null;

		try {
			lotPanel = new LotLayout(warehouse);
			lotPanel.setCaption("Lotes");
			lotPanel.setMargin(false);
			lotPanel.setSpacing(false);
		} catch (IOException e) {
			log.error("Error al cargar lotes del producto. Exception: " + e);
		}

		layout.addComponents(form, lotPanel);
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
		ListDataProvider<Warehouse> dataProvider = new ListDataProvider<>(warehouseBll.selectAll(false));
		filterProductDataProvider = dataProvider.withConfigurableFilter();
		warehouseGrid.setDataProvider(filterProductDataProvider);
	}

	@Override
	protected void saveButtonAction(Warehouse entity) {
		Warehouse.Builder warehouseBuilder = null;
		if (entity == null) {
			warehouseBuilder = Warehouse.builder();
		} else {
			warehouseBuilder = Warehouse.builder(entity);
		}

		entity = warehouseBuilder.code(txtCode.getValue()).name(txtName.getValue()).archived(false).build();
		save(warehouseBll, entity, "Bodega guardada");
		tableSequenceBll.save(tableSequence);
	}

	@Override
	public Warehouse getSelected() {
		Warehouse warehouseObj = null;
		Set<Warehouse> warehouses = warehouseGrid.getSelectedItems();
		if (warehouses != null && !warehouses.isEmpty()) {
			warehouseObj = (Warehouse) warehouses.toArray()[0];
		}
		return warehouseObj;
	}

	@Override
	protected void delete(Warehouse entity) {
		entity = Warehouse.builder(entity).archived(true).build();
		save(warehouseBll, entity, "Bodega borrada");
	}

	private Panel buildFilterPanel() {
		HorizontalLayout layout = ViewHelper.buildHorizontalLayout(true, true);
		txFilterByName = new TextField("Nombre");
		txFilterByName.addValueChangeListener(e -> refreshGrid());
		txFilterByCode = new TextField("Código");
		txFilterByCode.addValueChangeListener(e -> refreshGrid());
		layout.addComponents(txFilterByCode, txFilterByName);
		return ViewHelper.buildPanel("Filtrar por", layout);
	}

	private void refreshGrid() {
		filterProductDataProvider.setFilter(filterGrid());
		warehouseGrid.getDataProvider().refreshAll();
	}

	private SerializablePredicate<Warehouse> filterGrid() {
		SerializablePredicate<Warehouse> columnPredicate = null;
		String codeFilter = txFilterByCode.getValue().trim();
		String nameFilter = txFilterByName.getValue().trim();
		columnPredicate = warehouse -> (warehouse.getName().toLowerCase().contains(nameFilter.toLowerCase())
				&& warehouse.getCode().toLowerCase().contains(codeFilter.toLowerCase()));
		return columnPredicate;
	}

	private void getSequence() {
		BigInteger seq = null;
		TableSequence tableSeqObj = tableSequenceBll.select(Warehouse.class.getSimpleName());
		if (tableSeqObj != null) {
			seq = tableSeqObj.getSequence().add(BigInteger.valueOf(1L));
			TableSequence.Builder builder = TableSequence.builder(tableSeqObj);
			tableSequence = builder.sequence(seq).build();
		} else {
			ViewHelper.showNotification("No hay consecutivo configurado para bodegas", Notification.Type.ERROR_MESSAGE);
		}
	}
}
