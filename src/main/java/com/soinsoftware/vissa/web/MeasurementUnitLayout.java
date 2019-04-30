package com.soinsoftware.vissa.web;

import static com.soinsoftware.vissa.web.VissaUI.KEY_MEASUREMENT_UNIT;

import java.io.IOException;
import java.util.Set;

import org.apache.log4j.Logger;

import com.soinsoftware.vissa.bll.MeasurementUnitBll;
import com.soinsoftware.vissa.model.MeasurementUnit;
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

public class MeasurementUnitLayout extends AbstractEditableLayout<MeasurementUnit> {

	private static final long serialVersionUID = 5076502522106126046L;

	protected static final Logger log = Logger.getLogger(MeasurementUnitLayout.class);

	private final MeasurementUnitBll measurementUnitBll;

	private Grid<MeasurementUnit> measurementUnitGrid;

	private TextField txFilterByName;
	private TextField txtName;

	private ConfigurableFilterDataProvider<MeasurementUnit, Void, SerializablePredicate<MeasurementUnit>> filterDataProvider;

	public MeasurementUnitLayout() throws IOException {
		super("Unidades de medida", KEY_MEASUREMENT_UNIT);
		measurementUnitBll = MeasurementUnitBll.getInstance();
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
	protected AbstractOrderedLayout buildEditionView(MeasurementUnit entity) {
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
		measurementUnitGrid = ViewHelper.buildGrid(SelectionMode.SINGLE);
		measurementUnitGrid.addColumn(MeasurementUnit::getName).setCaption("Nombre");

		layout.addComponent(ViewHelper.buildPanel(null, measurementUnitGrid));
		fillGridData();
		return ViewHelper.buildPanel(null, layout);
	}

	@Override
	protected Component buildEditionComponent(MeasurementUnit measurementUnit) {
		VerticalLayout layout = ViewHelper.buildVerticalLayout(true, true);

		txtName = new TextField("Nombre");
		txtName.setWidth("50%");
		txtName.setValue(measurementUnit != null ? measurementUnit.getName() : "");

		final FormLayout form = new FormLayout();
		form.setMargin(true);
		form.setCaption("Unidades de medida");
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
		ListDataProvider<MeasurementUnit> dataProvider = new ListDataProvider<>(measurementUnitBll.selectAll(false));
		filterDataProvider = dataProvider.withConfigurableFilter();
		measurementUnitGrid.setDataProvider(filterDataProvider);

	}

	@Override
	protected void saveButtonAction(MeasurementUnit entity) {
		MeasurementUnit.Builder builder = null;
		if (entity == null) {
			builder = MeasurementUnit.builder();
		} else {
			builder = MeasurementUnit.builder(entity);
		}

		entity = builder.name(txtName.getValue()).build();
		save(measurementUnitBll, entity, "Unidad de medida guardada");
	}

	@Override
	public MeasurementUnit getSelected() {
		MeasurementUnit measurementUnit = null;
		Set<MeasurementUnit> measurementUnits = measurementUnitGrid.getSelectedItems();
		if (measurementUnits != null && !measurementUnits.isEmpty()) {
			measurementUnit = (MeasurementUnit) measurementUnits.toArray()[0];
		}
		return measurementUnit;
	}

	@Override
	protected void delete(MeasurementUnit entity) {
		save(measurementUnitBll,  MeasurementUnit.builder(entity).archived(true).build(), "Unidad de medida borrada");
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
		measurementUnitGrid.getDataProvider().refreshAll();
	}

	private SerializablePredicate<MeasurementUnit> filterGrid() {
		SerializablePredicate<MeasurementUnit> columnPredicate = null;
		String nameFilter = txFilterByName.getValue().trim();
		columnPredicate = measurementUnit -> (measurementUnit.getName().toLowerCase().contains(nameFilter.toLowerCase()));
		return columnPredicate;
	}
}