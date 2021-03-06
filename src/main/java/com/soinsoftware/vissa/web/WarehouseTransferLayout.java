package com.soinsoftware.vissa.web;

import static com.soinsoftware.vissa.web.VissaUI.KEY_WAREHOUSE_TRANSFER;

import java.io.IOException;
import java.util.Set;

import org.apache.log4j.Logger;
import org.jsoup.helper.StringUtil;
import org.vaadin.ui.NumberField;

import com.soinsoftware.vissa.bll.WarehouseBll;
import com.soinsoftware.vissa.bll.WarehouseTransferBll;
import com.soinsoftware.vissa.model.Warehouse;
import com.soinsoftware.vissa.model.WarehouseTransfer;
import com.soinsoftware.vissa.util.ViewHelper;
import com.vaadin.data.provider.ListDataProvider;
import com.vaadin.server.FontAwesome;
import com.vaadin.ui.AbstractOrderedLayout;
import com.vaadin.ui.Button;
import com.vaadin.ui.ComboBox;
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

@SuppressWarnings({ "unchecked", "deprecation" })
public class WarehouseTransferLayout extends AbstractEditableLayout<WarehouseTransfer> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5076502522106926046L;

	protected static final Logger log = Logger.getLogger(EgressTypeLayout.class);

	// Blls
	private final WarehouseTransferBll egressTypeBll;
	private final WarehouseBll warehouseBll;

	// Components
	private Grid<WarehouseTransfer> grid;
	private TextField txFilterByName;
	private TextField txFilterByCode;
	private TextField txtCode;
	private TextField txtName;
	private TextField txtProduct;
	private NumberField txtQuantity;
	private ComboBox<Warehouse> cbWarehouseSource;
	private ComboBox<Warehouse> cbWarehouseTarget;

	private ListDataProvider<WarehouseTransfer> dataProvider;

	public WarehouseTransferLayout() throws IOException {
		super("Translado de bodegas", KEY_WAREHOUSE_TRANSFER);
		egressTypeBll = WarehouseTransferBll.getInstance();
		warehouseBll = WarehouseBll.getInstance();
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
	protected AbstractOrderedLayout buildEditionView(WarehouseTransfer entity) {
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
		grid.addColumn(WarehouseTransfer::getCode).setCaption("Número");
		grid.addColumn(WarehouseTransfer::getTransferDate).setCaption("Fecha de traslado");
		grid.addColumn(WarehouseTransfer::getWarehouseSource).setCaption("Bodega origen");
		grid.addColumn(WarehouseTransfer::getWarehouseTarget).setCaption("Bodega destino");
		grid.addColumn(WarehouseTransfer::getLot).setCaption("Lote");
		grid.addColumn(WarehouseTransfer::getQuantity).setCaption("Cantidad");

		layout.addComponent(ViewHelper.buildPanel(null, grid));

		grid.addItemClickListener(listener -> {
			if (listener.getMouseEventDetails().isDoubleClick())
				grid.select(listener.getItem());
			showEditionTab(listener.getItem(), "Editar", FontAwesome.EDIT);
		});

		fillGridData();

		return ViewHelper.buildPanel(null, layout);
	}

	@Override
	protected Component buildEditionComponent(WarehouseTransfer entity) {

		VerticalLayout layout = ViewHelper.buildVerticalLayout(true, true);

		txtCode = new TextField("Número de traslado");
		txtCode.setStyleName(ValoTheme.TEXTAREA_TINY);
		txtCode.setRequiredIndicatorVisible(true);

		ListDataProvider<Warehouse> warehouseData = new ListDataProvider<>(warehouseBll.selectAll());

		cbWarehouseSource = new ComboBox<Warehouse>("Bodega origen");
		cbWarehouseSource.setEmptySelectionCaption("Seleccione");
		cbWarehouseSource.setEmptySelectionAllowed(false);
		cbWarehouseSource.setStyleName(ValoTheme.COMBOBOX_TINY);
		cbWarehouseSource.setRequiredIndicatorVisible(true);
		cbWarehouseSource.setDataProvider(warehouseData);
		cbWarehouseSource.setItemCaptionGenerator(Warehouse::getName);

		cbWarehouseTarget = new ComboBox<Warehouse>("Bodega destino");
		cbWarehouseTarget.setEmptySelectionCaption("Seleccione");
		cbWarehouseTarget.setEmptySelectionAllowed(false);
		cbWarehouseTarget.setStyleName(ValoTheme.COMBOBOX_TINY);
		cbWarehouseTarget.setRequiredIndicatorVisible(true);
		cbWarehouseTarget.setDataProvider(warehouseData);
		cbWarehouseTarget.setItemCaptionGenerator(Warehouse::getName);

		txtProduct = new TextField("Cantidad de ítems a transferir");
		
		txtQuantity = new NumberField("Cantidad de ítems a transferir");
		txtQuantity.setStyleName(ValoTheme.TEXTAREA_TINY);
		txtQuantity.setRequiredIndicatorVisible(true);

		// ----------------------------------------------------------------------------------

		final FormLayout form = new FormLayout();
		form.setMargin(true);
		form.setCaption("Datos del traslado de bodegas");
		form.setCaptionAsHtml(true);
		form.setSizeFull();
		form.setWidth("50%");
		form.addStyleName(ValoTheme.FORMLAYOUT_LIGHT);

		form.addComponents(txtCode, cbWarehouseSource, cbWarehouseTarget, txtQuantity);

		layout.addComponents(form);

		// Establece valores para los campos
		setFieldValues(entity);

		return layout;
	}

	/**
	 * Metodo para establecer los valores en los componentes
	 * 
	 * @param entity
	 */
	private void setFieldValues(WarehouseTransfer entity) {
		String strLog = "[setFieldValues] ";
		try {
			log.info(strLog + "[parameters] entity: " + entity);

			if (entity != null) {

			}
		} catch (Exception e) {
			log.error(strLog + "[Exception]" + e.getMessage());
			e.printStackTrace();
		}
	}

	protected Panel buildButtonPanelListMode() {
		HorizontalLayout layout = ViewHelper.buildHorizontalLayout(true, true);
		Button btNew = buildButtonForNewAction("");
		Button btEdit = buildButtonForEditAction("mystyle-btn");
		Button btDelete = buildButtonForDeleteAction("mystyle-btn");
		layout.addComponents(btNew, btEdit, btDelete);
		return ViewHelper.buildPanel(null, layout);
	}

	/**
	 * Metodo para cargar los datos en la grid
	 */
	@Override
	protected void fillGridData() {
		String strLog = "[fillGridData] ";
		try {
			dataProvider = new ListDataProvider<>(egressTypeBll.selectAll(false));
			grid.setDataProvider(dataProvider);
		} catch (Exception e) {
			log.error(strLog + "[Exception] " + e.getMessage());
			e.printStackTrace();
		}
	}

	/**
	 * Metodo con el evento del botón guardar
	 */
	@Override
	protected void saveButtonAction(WarehouseTransfer entity) {
		String strLog = "[saveButtonAction] ";
		try {
			log.info(strLog + "[parameters] entity: " + entity);

			String message = validateRequiredFields();
			if (!message.isEmpty()) {
				ViewHelper.showNotification(message, Notification.Type.WARNING_MESSAGE);
			} else {
				saveEntity(entity);
			}
		} catch (Exception e) {
			log.error(strLog + "[Exception] " + e.getMessage());
			e.printStackTrace();
		}
	}

	/**
	 * Metodo para guardar el tipo de egreso
	 * 
	 * @param entity
	 */
	private void saveEntity(WarehouseTransfer entity) {
		String strLog = "[saveEgressType] ";
		try {

			log.info(strLog + "[parameters] entity: " + entity);

			WarehouseTransfer.Builder builder = null;
			if (entity == null) {
				builder = WarehouseTransfer.builder();
			} else {
				builder = WarehouseTransfer.builder(entity);
			}

			/*
			 * entity =
			 * builder.code(txtCode.getValue().toUpperCase()).name(txtName.getValue().
			 * toUpperCase()) .archived(false).build();
			 */

			save(egressTypeBll, entity, "Tipo de egreso guardado");

		} catch (Exception e) {
			log.error(strLog + "[Exception] " + e.getMessage());
			e.printStackTrace();
		}
	}

	/**
	 * Metodo para validar los campos obligatorios para guardar el tipo de egreso
	 * 
	 * @return
	 */
	private String validateRequiredFields() {
		String message = "";
		String character = "|";

		if (StringUtil.isBlank(txtCode.getValue())) {
			if (!message.isEmpty()) {
				message = message.concat(character);
			}
			message = message.concat("El código es obligatorio");
		}
		if (StringUtil.isBlank(txtName.getValue())) {
			if (!message.isEmpty()) {
				message = message.concat(character);
			}
			message = message.concat("El nombre es obligatorio");
		}

		return message;
	}

	/**
	 * Metodo para obtener el registro seleccionado de la grid
	 */
	@Override
	public WarehouseTransfer getSelected() {
		String strLog = "[getSelected] ";
		WarehouseTransfer egressTypeObj = null;
		try {
			Set<WarehouseTransfer> egressTypes = grid.getSelectedItems();
			if (egressTypes != null && !egressTypes.isEmpty()) {
				egressTypeObj = (WarehouseTransfer) egressTypes.toArray()[0];
			}
		} catch (Exception e) {
			log.error(strLog + "" + e.getMessage());
			e.printStackTrace();
		}
		return egressTypeObj;
	}

	@Override
	protected void delete(WarehouseTransfer entity) {
		String strLog = "[delete] ";
		try {
			log.info(strLog + "[parameters] entity: " + entity);

			entity = WarehouseTransfer.builder(entity).archived(true).build();
			save(egressTypeBll, entity, "Tipo de egreso borrado");
		} catch (Exception e) {
			log.error(strLog + "[Exception] " + e.getMessage());
			e.printStackTrace();
		}
	}

	private Panel buildFilterPanel() {
		HorizontalLayout layout = ViewHelper.buildHorizontalLayout(true, true);

		txFilterByCode = new TextField("Código");
		txFilterByCode.setStyleName(ValoTheme.TEXTFIELD_TINY);
		txFilterByCode.addValueChangeListener(e -> refreshGrid());
		txFilterByName = new TextField("Nombre");

		txFilterByName.setStyleName(ValoTheme.TEXTFIELD_TINY);
		txFilterByName.addValueChangeListener(e -> refreshGrid());

		layout.addComponents(txFilterByCode, txFilterByName);

		return ViewHelper.buildPanel("Filtrar por", layout);
	}

	/**
	 * Metodo para recargar los registros de la grid
	 */
	private void refreshGrid() {
		if (dataProvider != null) {
			dataProvider.setFilter(egressType -> filterGrid(egressType));
		}
	}

	/**
	 * Metodo para filtrar los registros de la grid
	 * 
	 * @param entity
	 * @return
	 */
	private boolean filterGrid(WarehouseTransfer entity) {
		String strLog = "[filterGrid] ";
		boolean result = false;
		try {

			// Filtrar por el codigo del tipo de egreso
			/*
			 * String codeFilter = txFilterByCode.getValue(); if
			 * (!org.jsoup.helper.StringUtil.isBlank(codeFilter)) { result = result &&
			 * (entity.getCode().contains(codeFilter.toUpperCase())); }
			 * 
			 * // Filtrar por el nombre del tipo de egreso String nameFilter =
			 * txFilterByName.getValue(); if
			 * (!org.jsoup.helper.StringUtil.isBlank(nameFilter)) { result = result &&
			 * (entity.getCode().contains(nameFilter.toUpperCase())); }
			 */
		} catch (Exception e) {
			log.error(strLog + "[Exception] " + e.getMessage());
			e.printStackTrace();
			ViewHelper.showNotification(e.getMessage(), Notification.Type.WARNING_MESSAGE);
		}
		return result;

	}

}
