package com.soinsoftware.vissa.web;

import static com.soinsoftware.vissa.web.VissaUI.KEY_PRODUCTS;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.springframework.scheduling.annotation.Async;
import org.vaadin.dialogs.ConfirmDialog;
import org.vaadin.ui.NumberField;

import com.soinsoftware.vissa.bll.LotBll;
import com.soinsoftware.vissa.bll.MeasurementUnitBll;
import com.soinsoftware.vissa.bll.MeasurementUnitLotBll;
import com.soinsoftware.vissa.bll.MeasurementUnitProductBll;
import com.soinsoftware.vissa.model.ETransactionType;
import com.soinsoftware.vissa.model.Lot;
import com.soinsoftware.vissa.model.MeasurementUnit;
import com.soinsoftware.vissa.model.MeasurementUnitLot;
import com.soinsoftware.vissa.model.MeasurementUnitProduct;
import com.soinsoftware.vissa.model.Product;
import com.soinsoftware.vissa.util.ViewHelper;
import com.vaadin.data.Binder;
import com.vaadin.data.Binder.Binding;
import com.vaadin.data.provider.ListDataProvider;
import com.vaadin.server.FontAwesome;
import com.vaadin.server.Page;
import com.vaadin.ui.AbstractOrderedLayout;
import com.vaadin.ui.Button;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.Grid;
import com.vaadin.ui.Grid.Column;
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
	private final MeasurementUnitLotBll measurementUnitLotBll;
	private final MeasurementUnitBll measurementUnitBll;
	private final LotBll lotBll;

	private Grid<MeasurementUnitProduct> muProductGrid;

	private ComboBox<MeasurementUnit> cbMeasurementUnit;

	private Product product;;

	private ETransactionType transactionType;

	private ListDataProvider<MeasurementUnitProduct> dataProvider;
	private List<MeasurementUnitProduct> muProductList;
	private ComboBox<MeasurementUnit> cbMUEquivalence;

	private LotLayout lotLayout;

	public MuProductLayout(LotLayout lotLayout, Product product) throws IOException {
		super("Unidades de Medida", KEY_PRODUCTS);
		this.product = product;
		this.lotLayout = lotLayout;
		measurementUnitProductBll = MeasurementUnitProductBll.getInstance();
		measurementUnitBll = MeasurementUnitBll.getInstance();
		measurementUnitLotBll = MeasurementUnitLotBll.getInstance();
		lotBll = LotBll.getInstance();
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
		// cbMeasurementUnit.addValueChangeListener(e ->
		// validateMeasurementUnit(e.getValue()));

		muProductGrid.addColumn(MeasurementUnitProduct::getMeasurementUnit).setCaption("Unidad de medida")
				.setEditorComponent(cbMeasurementUnit, MeasurementUnitProduct::setMeasurementUnit);

		/*
		 * NumberField txtStock = new NumberField(); txtStock.setReadOnly(true);
		 * txtStock.setStyleName(ValoTheme.TEXTFIELD_TINY);
		 * muProductGrid.addColumn(MeasurementUnitProduct::getStockStr).
		 * setCaption("Stock en UM") .setEditorComponent(txtStock,
		 * MeasurementUnitProduct::setStockStr);
		 */

		cbMUEquivalence = new ComboBox<>();
		cbMUEquivalence.setEmptySelectionCaption("Seleccione");
		cbMUEquivalence.setWidth("50%");
		cbMUEquivalence.setDescription("UM equiv");
		cbMUEquivalence.setEmptySelectionAllowed(true);
		ListDataProvider<MeasurementUnit> muEquivData = new ListDataProvider<>(measurementUnitBll.selectAll());
		cbMUEquivalence.setDataProvider(muEquivData);
		cbMUEquivalence.setItemCaptionGenerator(MeasurementUnit::getName);
		cbMUEquivalence.setStyleName(ValoTheme.COMBOBOX_TINY);

		muProductGrid.addColumn(MeasurementUnitProduct::getMuEquivalence).setCaption("Unidad de medida equivalente")
				.setEditorComponent(cbMUEquivalence, MeasurementUnitProduct::setMuEquivalence);

		NumberField txtQtyEquivalence = new NumberField();
		muProductGrid.addColumn(MeasurementUnitProduct::getQtyEquivalenceStr).setCaption("Cantidad UM equivalente")
				.setEditorComponent(txtQtyEquivalence, MeasurementUnitProduct::setQtyEquivalenceStr);

		CheckBox ckPrincipal = new CheckBox();
		Binder<MeasurementUnitProduct> binder = muProductGrid.getEditor().getBinder();
		Binding<MeasurementUnitProduct, Boolean> doneBinding = binder.bind(ckPrincipal,
				MeasurementUnitProduct::isPrincipal, MeasurementUnitProduct::setPrincipal);
		Column<MeasurementUnitProduct, String> column = muProductGrid
				.addColumn(muProduct -> String.valueOf(muProduct.isPrincipal())).setCaption("UM principal");
		column.setEditorBinding(doneBinding);

		muProductGrid.getEditor().setEnabled(true);

		// muProductGrid.getEditor().addSaveListener(e ->
		// validateMeasurementUnit(e.getBean()));

		layout.addComponents(buttonLayout, muProductGrid);

		fillGridData();
		Panel panel = ViewHelper.buildPanel("Unidades de medida", layout);
		panel.setSizeFull();
		return panel;
	}

	/**
	 * Metodo para agregar una línea a la grid de UM
	 */
	private void addItemGrid() {
		String strLog = "[addItemGrid] ";
		try {
			// Se obtiene la UM pral
			MeasurementUnitProduct umPral = new MeasurementUnitProduct();
			List<MeasurementUnitProduct> muProducts = measurementUnitProductBll.selectPrincipal(product);
			if (muProducts != null && !muProducts.isEmpty()) {
				umPral = muProducts.get(0);
			}
			MeasurementUnitProduct umNew = new MeasurementUnitProduct();

			// Se copian los valores de precios a la nueva UM
			umNew.setProduct(product);
			umNew.setPurchasePrice(umPral.getPurchasePrice());
			umNew.setPurchaseTax(umPral.getPurchaseTax());
			umNew.setUtility(umPral.getUtility());
			umNew.setUtilityPrc(umPral.getUtilityPrc());
			umNew.setSalePrice(umPral.getSalePrice());
			umNew.setSaleTax(umPral.getSaleTax());
			// Se copia el mismo stock para luego modificarlo de acuerdo a la equivalencia
			umNew.setStock(umPral.getStock());

			// Agregar a la lista que es data provider de la grid
			muProductList.add(umNew);

			// Agregar a la grid de UM X Producto
			muProductGrid.select(umNew);

			refreshGrid();
		} catch (Exception e) {
			log.error(strLog + "[Exception] " + e.getMessage());
			e.printStackTrace();
		}
	}

	/**
	 * Metodo para validar que la UM no esté duplicada en la grid
	 * 
	 * @param measurementUnit
	 */
	private void validateMeasurementUnit(MeasurementUnitProduct measurementUnitProduct) {
		if (muProductList.contains(measurementUnitProduct)) {
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
		String strLog = "[fillGridData] ";
		try {
			muProductList = measurementUnitProductBll.select(product);
			dataProvider = new ListDataProvider<>(muProductList);
			muProductGrid.setDataProvider(dataProvider);
		} catch (Exception e) {
			log.error(strLog + "[Exception] " + e.getMessage());
			e.printStackTrace();
		}
	}

	/**
	 * Metodo para el evento del boton guardar de todas las Unidades de medida del
	 * producto
	 */
	@Override
	protected void saveButtonAction(MeasurementUnitProduct entity) {
		String strLog = "[saveButtonAction] ";

		try {
			String message = validateRequiredFields();
			if (!message.isEmpty()) {
				ViewHelper.showNotification(message, Notification.Type.ERROR_MESSAGE);
			} else {
				for (MeasurementUnitProduct muProduct : muProductList) {
					muProduct = convertMuEquivalence(muProduct);
					saveMeasurementUnit(muProduct);
				}
				ViewHelper.showNotification("Unidades de medida actualizadas", Notification.Type.WARNING_MESSAGE);

				// Si se invoca desde la ventana de lotes, se refresca la grid de precios y se
				// cierra la ventana de UmXProduct
				if (lotLayout != null) {
					lotLayout.refreshMeasurementUnit();
					closeWindow(lotLayout.getMuProductSubwindow());
				}
			}
		} catch (Exception e) {
			log.error(strLog + "[Exception] " + e.getMessage());
			e.printStackTrace();

		}
	}

	/**
	 * Guardar una unidad de medida por producto
	 * 
	 * @param entity
	 */
	private void saveMeasurementUnit(MeasurementUnitProduct entity) {
		String strLog = "[saveMeasurementUnit] ";
		try {

			measurementUnitProductBll.save(entity);
			log.info(strLog + " MU product guardada: " + entity);

			// Se consulta la UM guardada al producto
			List<MeasurementUnitProduct> muList = measurementUnitProductBll.select(entity.getMeasurementUnit(),
					product);

			if (muList != null && !muList.isEmpty()) {
				MeasurementUnitProduct entitySaved = muList.get(0);

				// Actualizar UM por cada lote del producto
				saveMuLot(entitySaved);
			}
		} catch (Exception e) {
			measurementUnitProductBll.rollback();
			log.error(strLog + "[Exception] " + e.getMessage());
			e.printStackTrace();
			ViewHelper.showNotification("Se generó un error al guardar el MU por producto",
					Notification.Type.ERROR_MESSAGE);
		}
	}

	/**
	 * Metodo para actualizar las unidades de medida por lote
	 */
	@Async
	private void saveMuLot(MeasurementUnitProduct muProduct) {
		String strLog = "[saveMuLot] ";
		try {
			Product product = muProduct.getProduct();
			List<Lot> lots = lotBll.select(product);
			// Buscar los lotes del producto
			for (Lot lotTmp : lots) {
				// Buscar las um del lote
				List<MeasurementUnitLot> muLotList = measurementUnitLotBll.select(lotTmp);
				// Cargar las umProduct del lote en una nueva lista
				List<MeasurementUnitProduct> muProductList = new ArrayList<>();
				for (MeasurementUnitLot muLot : muLotList) {
					muProductList.add(muLot.getMuProduct());
				}

				// Si la UM no está agregada al lote se relaciona
				if (!muProductList.contains(muProduct)) {
					MeasurementUnitLot muLotEntity = MeasurementUnitLot.builder().muProduct(muProduct).lot(lotTmp)
							.build();
					measurementUnitLotBll.save(muLotEntity);
					log.info(strLog + "UM agregada al lote");
				}
			}
		} catch (Exception e) {
			log.error(strLog + "[Exception] " + e.getMessage());
			e.printStackTrace();
		}
	}

	/**
	 * Metodo para validar los campos obligatorios para guardar las UM
	 * 
	 * @return
	 */
	private String validateRequiredFields() {
		String message = "";
		String character = "|";

		int qtyPral = 0;
		for (MeasurementUnitProduct muProduct : muProductList) {
			// Eliminar los registros nulos
			if (muProduct.getMeasurementUnit() == null) {
				muProductList.remove(muProduct);
			} else {
				if (muProduct.isPrincipal()) {
					qtyPral++;
				}
			}
		}
		if (qtyPral <= 0) {
			if (!message.isEmpty()) {
				message = message.concat(character);
			}
			message = "Se debe escoger una UM como principal ";
		}

		if (qtyPral > 1) {
			if (!message.isEmpty()) {
				message = message.concat(character);
			}
			message = "Solo debe existir una UM principal ";
		}

		return message;
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

	/**
	 * Convertir valores de precios y stock de acuerdo a la UM equivalente
	 * 
	 * @param mu
	 */
	public MeasurementUnitProduct convertMuEquivalence(MeasurementUnitProduct muProduct) {
		String strLog = "[convertMu] ";
		try {
			// Se busca las UM donde la nueva UM es equivalencia
			List<MeasurementUnitProduct> muProducts = measurementUnitProductBll
					.selectMuEquivalence(muProduct.getMeasurementUnit(), muProduct.getProduct());

			if (muProducts != null && !muProducts.isEmpty()) {
				// Se toma el primero que se encuentre
				MeasurementUnitProduct muEquivalent = muProducts.get(0);

				// La cantidad es el factor de conversión
				Double qtyEquivalence = muEquivalent.getQtyEquivalence();

				// El precio se divide
				muProduct.setPurchasePrice((double) Math.round(muEquivalent.getPurchasePrice() / qtyEquivalence));

				// El stock se multiplica
				muProduct.setStock((double) (muEquivalent.getStock() * qtyEquivalence));
			}

		} catch (Exception e) {
			log.error(strLog + "[Exception] " + e.getMessage());
			e.printStackTrace();
		}

		return muProduct;
	}

	/**
	 * Metodo para eliminar una unidad de medida del producto
	 */
	@Override
	protected void delete(MeasurementUnitProduct muProduct) {
		String strLog = "[delete]";
		try {
			if (muProduct != null) {

				ConfirmDialog.show(Page.getCurrent().getUI(), "Confirmar",
						"Está seguro de eliminar la unidad de medida", "Si", "No", e -> {
							if (e.isConfirmed()) {
								MeasurementUnitProduct entity = MeasurementUnitProduct.builder(muProduct).archived(true)
										.build();
								measurementUnitProductBll.save(entity);
								fillGridData();
								if (lotLayout != null) {
									lotLayout.refreshMeasurementUnit();
								}
								ViewHelper.showNotification("Unidad de medida eliminada",
										Notification.Type.WARNING_MESSAGE);
							}
						});
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
