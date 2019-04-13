package com.soinsoftware.vissa.web;

import static com.soinsoftware.vissa.web.VissaUI.KEY_INVENTORY;

import java.io.IOException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import org.jsoup.helper.StringUtil;

import com.soinsoftware.vissa.bll.InventoryTransactionBll;
import com.soinsoftware.vissa.model.ETransactionType;
import com.soinsoftware.vissa.model.InventoryTransaction;
import com.soinsoftware.vissa.model.Product;
import com.soinsoftware.vissa.util.Commons;
import com.soinsoftware.vissa.util.DateUtil;
import com.soinsoftware.vissa.util.ViewHelper;
import com.vaadin.data.provider.ListDataProvider;
import com.vaadin.shared.ui.datefield.DateTimeResolution;
import com.vaadin.ui.AbstractOrderedLayout;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.DateTimeField;
import com.vaadin.ui.Grid;
import com.vaadin.ui.Grid.SelectionMode;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Panel;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ValoTheme;

public class InventoryLayout extends AbstractEditableLayout<Product> {

	private final InventoryTransactionBll inventoryBll;

	InventoryTransaction invTransaction;
	private Grid<InventoryTransaction> inventoryGrid;
	private TextField txtFilterCode;
	private TextField txtFilterName;
	private DateTimeField dtfFilterIniDate;
	private DateTimeField dtfFilterEndDate;
	private ComboBox<ETransactionType> cbTransactionTypeFilter;

	private ListDataProvider<InventoryTransaction> dataProvider;

	public InventoryLayout() throws IOException {
		super("Transacciones de inventario", KEY_INVENTORY);
		inventoryBll = InventoryTransactionBll.getInstance();
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = -8569932867844792189L;

	@Override
	protected AbstractOrderedLayout buildListView() {
		VerticalLayout layout = ViewHelper.buildVerticalLayout(false, false);
		// Panel buttonPanel = buildButtonPanelForLists();
		Panel filterPanel = buildFilterPanel();
		Panel dataPanel = buildGridPanel();
		layout.addComponents(filterPanel, dataPanel);
		this.setSpacing(false);
		this.setMargin(false);
		return layout;
	}

	@Override
	protected AbstractOrderedLayout buildEditionView(Product entity) {
		VerticalLayout layout = ViewHelper.buildVerticalLayout(false, false);
		Panel buttonPanel = buildButtonPanelForEdition(entity);
		Component dataPanel = buildEditionComponent(entity);
		layout.addComponents(buttonPanel, dataPanel);
		return layout;
	}

	@SuppressWarnings("unchecked")
	@Override
	protected Panel buildGridPanel() {
		inventoryGrid = ViewHelper.buildGrid(SelectionMode.SINGLE);
		// InventoryTransaction inventoryTransaction = new InventoryTransaction();

		inventoryGrid.addColumn(invTransaction -> {
			if (invTransaction.getProduct() != null) {
				return invTransaction.getProduct().getCode();
			} else {
				return null;
			}
		}).setCaption("Código producto");

		inventoryGrid.addColumn(invTransaction -> {
			if (invTransaction.getProduct() != null) {
				return invTransaction.getProduct().getName();
			} else {
				return null;
			}
		}).setCaption("Nombre producto");

		inventoryGrid.addColumn(InventoryTransaction::getTransactionType).setCaption("Tipo de transacción");

		inventoryGrid.addColumn(invTransaction -> {
			if (invTransaction.getDocument() != null) {
				return DateUtil.dateToString(invTransaction.getDocument().getDocumentDate());
			} else {
				return null;
			}
		}).setCaption("Fecha transacción");

		inventoryGrid.addColumn(invTransaction -> {
			if (invTransaction.getDocument() != null) {
				return invTransaction.getDocument().getCode();
			} else {
				return null;
			}
		}).setCaption("Número de factura");

		inventoryGrid.addColumn(InventoryTransaction::getInitialStock).setCaption("Cantidad inicial");
		inventoryGrid.addColumn(InventoryTransaction::getQuantity).setCaption("Cantidad del movimiento");
		inventoryGrid.addColumn(InventoryTransaction::getFinalStock).setCaption("Cantidad final");

		fillGridData();
		return ViewHelper.buildPanel(null, inventoryGrid);
	}

	@Override
	protected Component buildEditionComponent(Product entity) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected void fillGridData() {
		List<InventoryTransaction> inventory = inventoryBll.selectAll();

		Comparator<InventoryTransaction> comparator = (h1, h2) -> h1.getDocument().getDocumentDate()
				.compareTo(h2.getDocument().getDocumentDate());

		inventory.sort(comparator.reversed());

		dataProvider = new ListDataProvider<>(inventory);

		inventoryGrid.setDataProvider(dataProvider);

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

	private Panel buildFilterPanel() {
		HorizontalLayout layout = ViewHelper.buildHorizontalLayout(true, true);
		txtFilterCode = new TextField("Codigo producto");
		txtFilterCode.addStyleName(ValoTheme.TEXTFIELD_TINY);
		txtFilterCode.addValueChangeListener(e -> refreshGrid());

		txtFilterName = new TextField("Nombre Producto");
		txtFilterName.addStyleName(ValoTheme.TEXTFIELD_TINY);
		txtFilterName.addValueChangeListener(e -> refreshGrid());

		dtfFilterIniDate = new DateTimeField("Fecha inicial");
		dtfFilterIniDate.setResolution(DateTimeResolution.SECOND);
		dtfFilterIniDate.setValue(DateUtil.getDefaultIniMonthDateTime());
		dtfFilterIniDate.setDateFormat(Commons.FORMAT_DATE_TIME);
		dtfFilterIniDate.setStyleName(ValoTheme.DATEFIELD_TINY);
		dtfFilterIniDate.setWidth("184px");
		dtfFilterIniDate.setRequiredIndicatorVisible(true);
		dtfFilterIniDate.addValueChangeListener(e -> refreshGrid());

		dtfFilterEndDate = new DateTimeField("Fecha final");
		dtfFilterEndDate.setResolution(DateTimeResolution.SECOND);
		dtfFilterEndDate.setValue(DateUtil.getDefaultEndDateTime());
		dtfFilterEndDate.setDateFormat(Commons.FORMAT_DATE_TIME);
		dtfFilterEndDate.setStyleName(ValoTheme.DATEFIELD_TINY);
		dtfFilterEndDate.setWidth("184px");
		dtfFilterEndDate.setRequiredIndicatorVisible(true);
		dtfFilterEndDate.addValueChangeListener(e -> refreshGrid());

		cbTransactionTypeFilter = new ComboBox<>("Tipo de transacción");
		cbTransactionTypeFilter.setEmptySelectionAllowed(true);
		cbTransactionTypeFilter.setEmptySelectionCaption("Seleccione");
		cbTransactionTypeFilter.setStyleName(ValoTheme.COMBOBOX_TINY);
		ListDataProvider<ETransactionType> transactionType = new ListDataProvider<>(
				Arrays.asList(ETransactionType.values()));
		cbTransactionTypeFilter.setDataProvider(transactionType);
		cbTransactionTypeFilter.setItemCaptionGenerator(ETransactionType::getName);
		cbTransactionTypeFilter.addValueChangeListener(e -> refreshGrid());

		layout.addComponents(txtFilterCode, txtFilterName, dtfFilterIniDate, dtfFilterEndDate, cbTransactionTypeFilter);
		return ViewHelper.buildPanel("Filtrar por", layout);
	}

	/**
	 * Metodo para refrescar los registros de la grid
	 */
	private void refreshGrid() {
		if (dataProvider != null) {
			dataProvider.setFilter(inventory -> filterGrid(inventory));
		}
	}

	/**
	 * Metodo para filtrar los registros de la grid
	 * 
	 * @param inventory
	 * @return
	 */
	private boolean filterGrid(InventoryTransaction inventory) {

		boolean result = false;
		try {

			Date iniDateFilter = dtfFilterIniDate.getValue() != null
					? DateUtil.localDateTimeToDate(dtfFilterIniDate.getValue())
					: DateUtil.localDateToDate(DateUtil.getDefaultIniMonthDate());

			Date endDateFilter = dtfFilterEndDate.getValue() != null
					? DateUtil.localDateTimeToDate(dtfFilterEndDate.getValue())
					: DateUtil.getDefaultEndDate();

			if (endDateFilter.before(iniDateFilter)) {
				throw new Exception("La fecha final debe ser mayor que la inicial");
			}

			// Filtro por defecto con fechas
			result = inventory.getDocument().getDocumentDate().before(endDateFilter)
					&& inventory.getDocument().getDocumentDate().after(iniDateFilter);

			// Filtrar por código del producto
			String codeFilter = txtFilterCode.getValue().toUpperCase();
			if (!StringUtil.isBlank(codeFilter)) {
				result = result && inventory.getProduct().getName().contains(codeFilter);
			}

			// Filtrar por nombre del producto
			String nameFilter = txtFilterName.getValue().toUpperCase();
			if (!StringUtil.isBlank(nameFilter)) {
				result = result && inventory.getProduct().getName().contains(nameFilter);
			}

			// Filtrar por el tipo de transacción
			ETransactionType transactionFilter = null;
			if (cbTransactionTypeFilter.getSelectedItem().isPresent()) {
				transactionFilter = cbTransactionTypeFilter.getSelectedItem().get();
				if (transactionFilter != null) {
					result = result && inventory.getTransactionType().equals(transactionFilter);
				}
			}

		} catch (Exception e) {
			ViewHelper.showNotification(e.getMessage(), Notification.Type.WARNING_MESSAGE);
			e.printStackTrace();
		}
		return result;

	}

}
