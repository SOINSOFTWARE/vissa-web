package com.soinsoftware.vissa.web;

import static com.soinsoftware.vissa.web.VissaUI.KEY_PRODUCTS;

import java.io.IOException;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.vaadin.ui.NumberField;

import com.soinsoftware.vissa.bll.MeasurementUnitBll;
import com.soinsoftware.vissa.bll.MeasurementUnitLotBll;
import com.soinsoftware.vissa.bll.MeasurementUnitProductBll;
import com.soinsoftware.vissa.common.CommonsConstants;
import com.soinsoftware.vissa.model.ETransactionType;
import com.soinsoftware.vissa.model.Lot;
import com.soinsoftware.vissa.model.MeasurementUnit;
import com.soinsoftware.vissa.model.MeasurementUnitLot;
import com.soinsoftware.vissa.model.MeasurementUnitProduct;
import com.soinsoftware.vissa.model.Product;
import com.soinsoftware.vissa.util.ViewHelper;
import com.vaadin.data.provider.ListDataProvider;
import com.vaadin.server.FontAwesome;
import com.vaadin.ui.AbstractOrderedLayout;
import com.vaadin.ui.Button;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.Grid;
import com.vaadin.ui.Grid.SelectionMode;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Panel;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.themes.ValoTheme;

@SuppressWarnings({ "unchecked", "deprecation" })
public class MuLotLayout extends AbstractEditableLayout<MeasurementUnitLot> {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8375374548623313938L;
	protected static final Logger log = Logger.getLogger(MuLotLayout.class);

	private final MeasurementUnitLotBll measurementUnitLotBll;
	private final MeasurementUnitProductBll measurementUnitProducBll;
	private final MeasurementUnitBll measurementUnitBll;

	private Grid<MeasurementUnitLot> muLotGrid;

	private ComboBox<MeasurementUnit> cbMeasurementUnit;

	private Product product;

	private Lot lot;

	private ETransactionType transactionType;

	private ListDataProvider<MeasurementUnitLot> dataProvider;
	private List<MeasurementUnitLot> muLotList;

	private LotLayout lotLayout;

	public MuLotLayout(LotLayout lotLayout) throws IOException {
		super("Unidades de Medida x lote", KEY_PRODUCTS);
		measurementUnitLotBll = MeasurementUnitLotBll.getInstance();
		measurementUnitProducBll = MeasurementUnitProductBll.getInstance();
		measurementUnitBll = MeasurementUnitBll.getInstance();
		this.lotLayout = lotLayout;
		this.lot = lotLayout.getLot();
		this.product = lot.getProduct();
		addComponent(buildGridPanel());
	}

	@Override
	public AbstractOrderedLayout buildListView() {
		VerticalLayout layout = ViewHelper.buildVerticalLayout(false, false);
		if (transactionType != null && transactionType.equals(ETransactionType.ENTRADA)) {
			Panel buttonPanel = buildButtonPanelForLists();
			layout.addComponent(buttonPanel);
		}
		// Panel filterPanel = buildFilterPanel();
		Panel dataPanel = buildGridPanel();
		layout.addComponent(dataPanel);
		return layout;
	}

	@Override
	protected AbstractOrderedLayout buildEditionView(MeasurementUnitLot entity) {
		VerticalLayout layout = ViewHelper.buildVerticalLayout(false, false);
		Panel buttonPanel = buildButtonPanelForEdition(entity);
		Component dataPanel = buildEditionComponent(entity);
		layout.addComponents(buttonPanel, dataPanel);
		return layout;
	}

	@Override
	protected Panel buildGridPanel() {
		VerticalLayout layout = ViewHelper.buildVerticalLayout(false, true);

		HorizontalLayout buttonLayout = ViewHelper.buildHorizontalLayout(true, false);

		Button newMuTbn = new Button("Nueva", FontAwesome.PLUS);
		newMuTbn.setStyleName(ValoTheme.BUTTON_TINY);
		newMuTbn.addClickListener(e -> addItemGrid());

		Button saveMuBtn = new Button("Guardar", FontAwesome.SAVE);
		saveMuBtn.setStyleName(ValoTheme.BUTTON_TINY);
		saveMuBtn.addClickListener(e -> saveButtonAction(null));

		Button deleteMuBtn = new Button("Eliminar", FontAwesome.ERASER);
		deleteMuBtn.setStyleName(ValoTheme.BUTTON_TINY);
		deleteMuBtn.addClickListener(e -> {
			delete(getSelected());
		});

		buttonLayout.addComponents(newMuTbn, saveMuBtn, deleteMuBtn);

		muLotGrid = ViewHelper.buildGrid(SelectionMode.SINGLE);
		muLotGrid.setHeight("160px");

		cbMeasurementUnit = new ComboBox<>("Unidad de medida");
		cbMeasurementUnit.setEmptySelectionCaption("Seleccione");
		cbMeasurementUnit.setWidth("50%");
		cbMeasurementUnit.setDescription("Unidad de medida");
		cbMeasurementUnit.setEmptySelectionAllowed(true);
		ListDataProvider<MeasurementUnit> measurementDataProv = new ListDataProvider<>(
				measurementUnitBll.selectAll(false));
		cbMeasurementUnit.setDataProvider(measurementDataProv);
		cbMeasurementUnit.setItemCaptionGenerator(MeasurementUnit::getName);
		/*
		 * cbMeasurementUnit.addValueChangeListener(e -> validateMeasurementUnit(
		 * cbMeasurementUnit.getSelectedItem().isPresent() ?
		 * cbMeasurementUnit.getSelectedItem().get() : null));
		 */

		muLotGrid.addColumn(MeasurementUnitLot::getMeasureUnit).setCaption("Unidad de medida")
				.setEditorComponent(cbMeasurementUnit, MeasurementUnitLot::setMeasureUnit);

		NumberField txtStock = new NumberField();
		muLotGrid.addColumn(MeasurementUnitLot::getStockStr).setCaption("Stock").setEditorComponent(txtStock,
				MeasurementUnitLot::setStockStr);

		muLotGrid.getEditor().setEnabled(true);

		muLotGrid.getEditor().addSaveListener(e -> {
			muLotGrid.getDataProvider().refreshAll();
		});
		layout.addComponents(buttonLayout, muLotGrid);

		fillGridData();
		Panel panel = ViewHelper.buildPanel("Stock por Unidad de Medida", layout);
		panel.setSizeFull();
		return panel;
	}

	/**
	 * Metodo para agregar una línea a la grid de UM x Lote
	 */
	private void addItemGrid() {
		MeasurementUnitLot muProductLot = new MeasurementUnitLot();
		muProductLot.setLot(lot);
		muLotList.add(muProductLot);
		muLotGrid.focus();
		refreshGrid();
	}

	/**
	 * Metodo para validar que la UM no esté duplicada
	 * 
	 * @param measurementUnit
	 */
	private void validateMeasurementUnit(MeasurementUnit measurementUnit) {
		MeasurementUnitProduct muProduct = MeasurementUnitProduct.builder().measurementUnit(measurementUnit).build();
		if (muLotList.contains(muProduct)) {
			ViewHelper.showNotification("Esta unidad de medida ya está asociada al producto",
					Notification.Type.ERROR_MESSAGE);
		}
	}

	protected Panel buildButtonPanelForLists() {
		HorizontalLayout layout = ViewHelper.buildHorizontalLayout(true, true);
		Button btNew = buildButtonForNewAction(ValoTheme.BUTTON_SMALL);
		Button btEdit = buildButtonForEditAction(ValoTheme.BUTTON_SMALL);
		Button btDelete = buildButtonForDeleteAction(ValoTheme.BUTTON_SMALL);
		layout.addComponents(btNew, btEdit, btDelete);
		return ViewHelper.buildPanel(null, layout);
	}

	protected Panel buildButtonPanelForEdition(MeasurementUnitLot entity) {
		HorizontalLayout layout = ViewHelper.buildHorizontalLayout(true, true);
		Button btCancel = buildButtonForCancelAction(ValoTheme.BUTTON_SMALL);
		Button btSave = buildButtonForSaveAction(entity, ValoTheme.BUTTON_SMALL);
		layout.addComponents(btCancel, btSave);
		return ViewHelper.buildPanel(null, layout);
	}

	@Override
	protected Component buildEditionComponent(MeasurementUnitLot entity) {
		VerticalLayout layout = ViewHelper.buildVerticalLayout(true, true);

		return layout;
	}

	@Override
	protected void fillGridData() {

		String strLog = "[fillGridData] ";

		try {
			muLotList = measurementUnitLotBll.select(lot);

			if (muLotList == null || muLotList.isEmpty()) {
				List<MeasurementUnitProduct> muProductList = measurementUnitProducBll.select(product);
				for (MeasurementUnitProduct muProduct : muProductList) {
					MeasurementUnitLot muLot = new MeasurementUnitLot();
					muLot.setMuProduct(muProduct);
					muLot.setMeasureUnit(muProduct.getMeasurementUnit());
					muLot.setLot(lot);
					muLotList.add(muLot);
				}

			}

			if (muLotList == null || muLotList.isEmpty()) {
				ViewHelper.showNotification("No hay UM confirguradas para el producto",
						Notification.Type.ERROR_MESSAGE);
			} else {
				dataProvider = new ListDataProvider<>(muLotList);
				muLotGrid.setDataProvider(dataProvider);
			}
		} catch (Exception e) {
			log.error(strLog + "[Exception] " + e.getMessage());
		}
	}

	@Override
	protected void saveButtonAction(MeasurementUnitLot entity) {
		String strLog = "[saveButtonAction] ";

		try {
			for (MeasurementUnitLot muLot : muLotList) {
				measurementUnitLotBll.save(muLot);
				log.info(strLog + " MU x lote guardado: " + muLot);

				// Actualizar stock total por UM
				lotLayout.updateStock(product);
			}
			ViewHelper.showNotification("Unidades de medida actualizadas para el lote",
					Notification.Type.WARNING_MESSAGE);
			closeWindow(lotLayout.getMuProductSubwindow());
		} catch (Exception e) {
			measurementUnitLotBll.rollback();
			log.error(strLog + "[Exception]" + e.getMessage());
			ViewHelper.showNotification("Se generó un error al guardar el MU por lote",
					Notification.Type.ERROR_MESSAGE);
		}
	}

	@Override
	protected MeasurementUnitLot getSelected() {
		MeasurementUnitLot muProduct = null;
		Set<MeasurementUnitLot> muProducts = muLotGrid.getSelectedItems();
		if (muProducts != null && !muProducts.isEmpty()) {
			muProduct = (MeasurementUnitLot) muProducts.toArray()[0];
		}
		return muProduct;
	}

	@Override
	protected void delete(MeasurementUnitLot entity) {
		String strLog = "[delete]";
		try {
			if (entity != null) {
				entity = MeasurementUnitLot.builder(entity).archived(true).build();
				measurementUnitLotBll.save(entity);
				fillGridData();
				ViewHelper.showNotification("Unidad de medida eliminada", Notification.Type.WARNING_MESSAGE);
			} else {
				ViewHelper.showNotification("No ha seleccionado un registro", Notification.Type.WARNING_MESSAGE);
			}
		} catch (Exception e) {
			log.error(strLog + "[Exception]" + e.getMessage());
			ViewHelper.showNotification("Se generó un error al eliminar la UM", Notification.Type.ERROR_MESSAGE);
		}
	}

	private void refreshGrid() {
		dataProvider.refreshAll();
	}

	private boolean filterGrid(MeasurementUnitLot muProduct) {
		String strLog = "[filterGrid] ";
		boolean result = true;
		try {

		} catch (Exception e) {
			log.error(strLog + "[Exception]" + e.getMessage());
		}
		return result;
	}

	public Grid<MeasurementUnitLot> getMuProductGrid() {
		return muLotGrid;
	}

	public void setMuProductGrid(Grid<MeasurementUnitLot> muProductGrid) {
		this.muLotGrid = muProductGrid;
	}

	/**
	 * Metodo para cerrar un componente Window
	 * 
	 * @param w
	 */
	private void closeWindow(Window w) {
		if (w != null) {
			w.close();
		}
	}
}
