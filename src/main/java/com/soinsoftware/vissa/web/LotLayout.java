package com.soinsoftware.vissa.web;

import java.io.IOException;
import java.util.Set;

import org.apache.log4j.Logger;

import com.soinsoftware.vissa.bll.LotBll;
import com.soinsoftware.vissa.bll.WarehouseBll;
import com.soinsoftware.vissa.model.Lot;
import com.soinsoftware.vissa.model.Product;
import com.soinsoftware.vissa.model.Warehouse;
import com.soinsoftware.vissa.util.DateUtil;
import com.soinsoftware.vissa.util.ViewHelper;
import com.vaadin.data.provider.ConfigurableFilterDataProvider;
import com.vaadin.data.provider.ListDataProvider;
import com.vaadin.server.SerializablePredicate;
import com.vaadin.ui.AbstractOrderedLayout;
import com.vaadin.ui.Button;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.DateTimeField;
import com.vaadin.ui.FormLayout;
import com.vaadin.ui.Grid;
import com.vaadin.ui.Grid.SelectionMode;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Panel;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ValoTheme;

@SuppressWarnings("unchecked")
public class LotLayout extends AbstractEditableLayout<Lot> {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8375374548620313938L;
	protected static final Logger log = Logger.getLogger(LotLayout.class);

	private final LotBll lotBll;
	private final WarehouseBll warehouseBll;

	private Grid<Lot> lotGrid;

	private TextField txtCode;
	private TextField txtName;
	private DateTimeField dtfFabricationDate;
	private DateTimeField dtfExpirationDate;
	private TextField txtQuantity;
	private ComboBox<Warehouse> cbWarehouse;

	private Product product;
	private Warehouse warehouse;
	boolean listMode;

	private ConfigurableFilterDataProvider<Lot, Void, SerializablePredicate<Lot>> filterLotDataProvider;

	public LotLayout(Product product) throws IOException {
		super("Lotes");
		this.product = product;
		lotBll = LotBll.getInstance();
		warehouseBll = WarehouseBll.getInstance();
		addListTab();
	}

	public LotLayout(Warehouse warehouse) throws IOException {
		super("Lotes");
		this.warehouse = warehouse;
		lotBll = LotBll.getInstance();
		warehouseBll = WarehouseBll.getInstance();
		addListTab();
	}

	@Override
	public AbstractOrderedLayout buildListView() {
		VerticalLayout layout = ViewHelper.buildVerticalLayout(false, false);
		Panel buttonPanel = buildButtonPanelForLists();
		// Panel filterPanel = buildFilterPanel();
		Panel dataPanel = buildGridPanel();
		layout.addComponents(buttonPanel, dataPanel);
		return layout;
	}

	@Override
	protected AbstractOrderedLayout buildEditionView(Lot entity) {
		VerticalLayout layout = ViewHelper.buildVerticalLayout(false, false);
		Panel buttonPanel = buildButtonPanelForEdition(entity);
		Component dataPanel = buildEditionComponent(entity);
		layout.addComponents(buttonPanel, dataPanel);
		return layout;
	}

	@Override
	protected Panel buildGridPanel() {
		if (listMode) {
			
		}
		lotGrid = ViewHelper.buildGrid(SelectionMode.SINGLE);
		lotGrid.setSizeFull();
		lotGrid.addColumn(Lot::getCode).setCaption("Código");
		lotGrid.addColumn(lot -> {
			if (lot != null && lot.getLotDate() != null) {
				return DateUtil.dateToString(lot.getLotDate());
			} else {
				return null;
			}
		}).setCaption("Fecha de fabricación");
		lotGrid.addColumn(lot -> {
			if (lot != null && lot.getExpirationDate() != null) {
				return DateUtil.dateToString(lot.getExpirationDate());
			} else {
				return null;
			}
		}).setCaption("Fecha de vencimiento");
		lotGrid.addColumn(Lot::getQuantity).setCaption("Cantidad de productos");
		lotGrid.addColumn(lot -> {
			if (lot != null && lot.getWarehouse() != null) {
				return lot.getWarehouse().getName();
			} else {
				return null;
			}
		}).setCaption("Bodega");

		fillGridData();
		return ViewHelper.buildPanel(null, lotGrid);
	}

	protected Panel buildButtonPanelForLists() {
		HorizontalLayout layout = ViewHelper.buildHorizontalLayout(true, true);
		Button btNew = buildButtonForNewAction(ValoTheme.BUTTON_SMALL);
		Button btEdit = buildButtonForEditAction(ValoTheme.BUTTON_SMALL);
		Button btDelete = buildButtonForDeleteAction(ValoTheme.BUTTON_SMALL);
		layout.addComponents(btNew, btEdit, btDelete);
		return ViewHelper.buildPanel(null, layout);
	}

	protected Panel buildButtonPanelForEdition(Lot entity) {
		HorizontalLayout layout = ViewHelper.buildHorizontalLayout(true, true);
		Button btCancel = buildButtonForCancelAction(ValoTheme.BUTTON_SMALL);
		Button btSave = buildButtonForSaveAction(entity, ValoTheme.BUTTON_SMALL);
		layout.addComponents(btCancel, btSave);
		return ViewHelper.buildPanel(null, layout);
	}

	@Override
	protected Component buildEditionComponent(Lot entity) {
		VerticalLayout layout = ViewHelper.buildVerticalLayout(true, true);
		txtCode = new TextField("Código");
		txtCode.setStyleName(ValoTheme.TEXTAREA_TINY);
		txtCode.focus();
		txtCode.setValue(entity != null ? entity.getCode() : "");

		txtName = new TextField("Nombre");
		txtName.setStyleName(ValoTheme.TEXTAREA_TINY);
		txtName.setValue(entity != null ? entity.getName() : "");

		dtfFabricationDate = new DateTimeField("Fecha de fabricación");
		dtfFabricationDate.setStyleName(ValoTheme.DATEFIELD_TINY);
		dtfFabricationDate.setValue(entity != null ? DateUtil.dateToLocalDateTime(entity.getLotDate()) : null);

		dtfExpirationDate = new DateTimeField("Fecha de vencimiento");
		dtfExpirationDate.setStyleName(ValoTheme.DATEFIELD_TINY);
		dtfExpirationDate.setValue(entity != null ? DateUtil.dateToLocalDateTime(entity.getExpirationDate()) : null);

		txtQuantity = new TextField("Cantidad");
		txtQuantity.setStyleName(ValoTheme.TEXTAREA_TINY);
		txtQuantity.setValue(entity != null ? String.valueOf(entity.getQuantity()) : "");

		cbWarehouse = new ComboBox<>("Bodega");
		cbWarehouse.setEmptySelectionCaption("Seleccione");
		cbWarehouse.setStyleName(ValoTheme.COMBOBOX_TINY);
		ListDataProvider<Warehouse> countryDataProv = new ListDataProvider<>(warehouseBll.selectAll());
		cbWarehouse.setDataProvider(countryDataProv);
		cbWarehouse.setItemCaptionGenerator(Warehouse::getName);
		cbWarehouse.setValue(
				entity == null && warehouse != null ? warehouse : entity != null ? entity.getWarehouse() : null);
		if (warehouse != null) {
			cbWarehouse.setReadOnly(true);
		}

		FormLayout form = new FormLayout();
		form.setMargin(true);
		form.setCaption("Datos del lote");
		form.setCaptionAsHtml(true);
		form.setSizeFull();
		form.setWidth("50%");
		form.addStyleName(ValoTheme.FORMLAYOUT_LIGHT);

		form.addComponents(txtCode, txtName, dtfFabricationDate, dtfExpirationDate, txtQuantity, cbWarehouse);

		layout.addComponents(form);

		return layout;
	}

	@Override
	protected void fillGridData() {
		ListDataProvider<Lot> dataProvider = null;
		if (product != null) {
			dataProvider = new ListDataProvider<>(lotBll.select(product));
		} else if (warehouse != null) {
			dataProvider = new ListDataProvider<>(lotBll.select(warehouse));
		}
		if (dataProvider != null) {
			filterLotDataProvider = dataProvider.withConfigurableFilter();
			lotGrid.setDataProvider(filterLotDataProvider);
		}

	}

	@Override
	protected void saveButtonAction(Lot entity) {
		Lot.Builder lotBuilder = null;
		if (entity == null) {
			lotBuilder = Lot.builder();
		} else {
			lotBuilder = Lot.builder(entity);
		}

		Warehouse warehouse = cbWarehouse.getSelectedItem().isPresent() ? cbWarehouse.getSelectedItem().get() : null;
		entity = lotBuilder.code(txtCode.getValue()).name(txtName.getValue())
				.lotDate(DateUtil.localDateTimeToDate(dtfFabricationDate.getValue()))
				.expirationDate(DateUtil.localDateTimeToDate(dtfExpirationDate.getValue())).archived(false)
				.quantity(Integer.parseInt(txtQuantity.getValue())).product(product).warehouse(warehouse).build();
		save(lotBll, entity, "Lote guardado");

	}

	@Override
	protected Lot getSelected() {
		Lot lot = null;
		Set<Lot> lots = lotGrid.getSelectedItems();
		if (lots != null && !lots.isEmpty()) {
			lot = (Lot) lots.toArray()[0];
		}
		return lot;
	}

	@Override
	protected void delete(Lot entity) {
		entity = Lot.builder(entity).archived(true).build();
		save(lotBll, entity, "Lote borrado");

	}

}
