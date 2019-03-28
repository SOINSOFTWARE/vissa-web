package com.soinsoftware.vissa.web;

import static com.soinsoftware.vissa.web.VissaUI.KEY_PRODUCTS;

import java.io.IOException;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.vaadin.ui.NumberField;

import com.soinsoftware.vissa.bll.MeasurementUnitBll;
import com.soinsoftware.vissa.bll.MeasurementUnitProductBll;
import com.soinsoftware.vissa.model.ETransactionType;
import com.soinsoftware.vissa.model.MeasurementUnit;
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
public class MuProductLayout extends AbstractEditableLayout<MeasurementUnitProduct> {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8375374548620313938L;
	protected static final Logger log = Logger.getLogger(MuProductLayout.class);

	private final MeasurementUnitProductBll measurementUnitProductBll;
	private final MeasurementUnitBll measurementUnitBll;

	private Grid<MeasurementUnitProduct> muProductGrid;

	private ComboBox<MeasurementUnit> cbMeasurementUnit;

	private Product product;;

	private ETransactionType transactionType;

	private ListDataProvider<MeasurementUnitProduct> dataProvider;
	private List<MeasurementUnitProduct> muProductList;

	private LotLayout lotLayout;

	public MuProductLayout(LotLayout lotLayout, Product product) throws IOException {
		super("Unidades de Medida", KEY_PRODUCTS);
		this.product = product;
		this.lotLayout = lotLayout;
		measurementUnitProductBll = MeasurementUnitProductBll.getInstance();
		measurementUnitBll = MeasurementUnitBll.getInstance();
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
	protected AbstractOrderedLayout buildEditionView(MeasurementUnitProduct entity) {
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

		muProductGrid = ViewHelper.buildGrid(SelectionMode.SINGLE);
		muProductGrid.setHeight("160px");

		cbMeasurementUnit = new ComboBox<>("Unidad de medida");
		cbMeasurementUnit.setEmptySelectionCaption("Seleccione");
		cbMeasurementUnit.setWidth("50%");
		cbMeasurementUnit.setDescription("Unidad de medida");
		cbMeasurementUnit.setEmptySelectionAllowed(true);
		ListDataProvider<MeasurementUnit> measurementDataProv = new ListDataProvider<>(measurementUnitBll.selectAll());
		cbMeasurementUnit.setDataProvider(measurementDataProv);
		cbMeasurementUnit.setItemCaptionGenerator(MeasurementUnit::getName);
		cbMeasurementUnit.addValueChangeListener(e -> validateMeasurementUnit(
				cbMeasurementUnit.getSelectedItem().isPresent() ? cbMeasurementUnit.getSelectedItem().get() : null));

		muProductGrid.addColumn(MeasurementUnitProduct::getMeasurementUnit).setCaption("Unidad de medida")
				.setEditorComponent(cbMeasurementUnit, MeasurementUnitProduct::setMeasurementUnit);

		NumberField txtPurchasePrice = new NumberField();
		muProductGrid.addColumn(MeasurementUnitProduct::getPurchasePriceStr).setCaption("Precio de compra")
				.setEditorComponent(txtPurchasePrice, MeasurementUnitProduct::setPurchasePriceStr);
		NumberField txtPurchaseTax = new NumberField();
		muProductGrid.addColumn(MeasurementUnitProduct::getPurchaseTaxStr).setCaption("% Impuesto compra")
				.setEditorComponent(txtPurchaseTax, MeasurementUnitProduct::setPurchaseTaxStr);

		NumberField txtUtility = new NumberField();
		muProductGrid.addColumn(MeasurementUnitProduct::getUtilityStr).setCaption("Utilidad ($)")
				.setEditorComponent(txtUtility, MeasurementUnitProduct::setUtilityStr);

		muProductGrid.addColumn(MeasurementUnitProduct::getSalePrice).setCaption("Precio de venta");
		NumberField txtSaleTax = new NumberField();
		muProductGrid.addColumn(MeasurementUnitProduct::getSaleTaxStr).setCaption("% IVA")
				.setEditorComponent(txtSaleTax, MeasurementUnitProduct::setSaleTaxStr);

		muProductGrid.addColumn(MeasurementUnitProduct::getFinalPrice).setCaption("Precio final");

		muProductGrid.getEditor().setEnabled(true);

		layout.addComponents(buttonLayout, muProductGrid);

		fillGridData();
		Panel panel = ViewHelper.buildPanel("Unidades de medida y precios", layout);
		panel.setSizeFull();
		return panel;
	}

	/**
	 * Metodo para agregar una línea a la grid de UM
	 */
	private void addItemGrid() {
		MeasurementUnitProduct muProduct = new MeasurementUnitProduct();
		muProduct.setProduct(product);
		muProductList.add(muProduct);

		muProductGrid.select(muProduct);

		refreshGrid();
	}

	/**
	 * Metodo para validar que la UM no esté duplicada
	 * 
	 * @param measurementUnit
	 */
	private void validateMeasurementUnit(MeasurementUnit measurementUnit) {
		MeasurementUnitProduct muProduct = MeasurementUnitProduct.builder().measurementUnit(measurementUnit).build();
		if (muProductList.contains(muProduct)) {
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

	protected Panel buildButtonPanelForEdition(MeasurementUnitProduct entity) {
		HorizontalLayout layout = ViewHelper.buildHorizontalLayout(true, true);
		Button btCancel = buildButtonForCancelAction(ValoTheme.BUTTON_SMALL);
		Button btSave = buildButtonForSaveAction(entity, ValoTheme.BUTTON_SMALL);
		layout.addComponents(btCancel, btSave);
		return ViewHelper.buildPanel(null, layout);
	}

	@Override
	protected Component buildEditionComponent(MeasurementUnitProduct entity) {
		VerticalLayout layout = ViewHelper.buildVerticalLayout(true, true);

		return layout;
	}

	@Override
	protected void fillGridData() {

		String strLog = "[fillGridData]";

		try {
			muProductList = measurementUnitProductBll.select(product);
			dataProvider = new ListDataProvider<>(muProductList);
			muProductGrid.setDataProvider(dataProvider);
		} catch (Exception e) {
			log.error(strLog + "[Exception]" + e.getMessage());
		}
	}

	@Override
	protected void saveButtonAction(MeasurementUnitProduct entity) {
		String strLog = "[saveButtonAction] ";

		try {
			for (MeasurementUnitProduct muProduct : muProductList) {
				measurementUnitProductBll.save(muProduct);
				log.info(strLog + " MU product saved: " + muProduct);
			}
			ViewHelper.showNotification("Unidades de medida actualizadas", Notification.Type.WARNING_MESSAGE);
			closeWindow(lotLayout.getMuProductSubwindow());
		} catch (Exception e) {
			measurementUnitProductBll.rollback();
			log.error(strLog + "[Exception]" + e.getMessage());
			ViewHelper.showNotification("Se generó un error al guardar el MU por producto",
					Notification.Type.ERROR_MESSAGE);
		}
	}

	@Override
	protected MeasurementUnitProduct getSelected() {
		MeasurementUnitProduct muProduct = null;
		Set<MeasurementUnitProduct> muProducts = muProductGrid.getSelectedItems();
		if (muProducts != null && !muProducts.isEmpty()) {
			muProduct = (MeasurementUnitProduct) muProducts.toArray()[0];
		}
		return muProduct;
	}

	@Override
	protected void delete(MeasurementUnitProduct entity) {
		String strLog = "[delete]";
		try {
			if (entity != null) {
				entity = MeasurementUnitProduct.builder(entity).archived(true).build();
				measurementUnitProductBll.save(entity);
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
		dataProvider.setFilter(muProduct -> filterGrid(muProduct));
	}

	private boolean filterGrid(MeasurementUnitProduct muProduct) {
		String strLog = "[filterGrid] ";
		boolean result = true;
		try {

		} catch (Exception e) {
			log.error(strLog + "[Exception]" + e.getMessage());
		}
		return result;
	}

	public Grid<MeasurementUnitProduct> getMuProductGrid() {
		return muProductGrid;
	}

	public void setMuProductGrid(Grid<MeasurementUnitProduct> muProductGrid) {
		this.muProductGrid = muProductGrid;
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
