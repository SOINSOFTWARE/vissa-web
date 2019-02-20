package com.soinsoftware.vissa.web;

import static com.soinsoftware.vissa.web.VissaUI.KEY_PRODUCTS;

import java.io.IOException;
import java.util.Date;
import java.util.Set;

import org.apache.log4j.Logger;
import org.vaadin.ui.NumberField;

import com.soinsoftware.vissa.bll.LotBll;
import com.soinsoftware.vissa.bll.ProductBll;
import com.soinsoftware.vissa.bll.WarehouseBll;
import com.soinsoftware.vissa.model.ETransactionType;
import com.soinsoftware.vissa.model.Lot;
import com.soinsoftware.vissa.model.Product;
import com.soinsoftware.vissa.model.Warehouse;
import com.soinsoftware.vissa.util.Commons;
import com.soinsoftware.vissa.util.DateUtil;
import com.soinsoftware.vissa.util.ViewHelper;
import com.vaadin.data.provider.ConfigurableFilterDataProvider;
import com.vaadin.data.provider.ListDataProvider;
import com.vaadin.data.provider.Query;
import com.vaadin.server.SerializablePredicate;
import com.vaadin.ui.AbstractOrderedLayout;
import com.vaadin.ui.Button;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.DateTimeField;
import com.vaadin.ui.FormLayout;
import com.vaadin.ui.Grid;
import com.vaadin.ui.Grid.Column;
import com.vaadin.ui.Grid.SelectionMode;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Panel;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.components.grid.FooterRow;
import com.vaadin.ui.themes.ValoTheme;

@SuppressWarnings("unchecked")
public class LotLayout extends AbstractEditableLayout<Lot> {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8375374548620313938L;
	protected static final Logger log = Logger.getLogger(LotLayout.class);

	private final LotBll lotBll;
	private final ProductBll productBll;
	private final WarehouseBll warehouseBll;

	public Grid<Lot> lotGrid;

	private TextField txtCode;
	private TextField txtName;
	private DateTimeField dtfFabricationDate;
	private DateTimeField dtfExpirationDate;
	private NumberField txtQuantity;
	private ComboBox<Warehouse> cbWarehouse;

	private Product product;
	private Warehouse warehouse;
	private boolean listMode;
	private Double totalStock;
	private ETransactionType transactionType;

	private ProductLayout productLayout;
	private Column<?, ?> columnQuantity;
	private Column<?, ?> columnWarehouse;
	private FooterRow footer;

	private ConfigurableFilterDataProvider<Lot, Void, SerializablePredicate<Lot>> filterLotDataProvider;
	private ListDataProvider<Lot> dataProvider = null;

	public LotLayout(Product product, ProductLayout productLayout, ETransactionType transactionType)
			throws IOException {
		super("Lotes", KEY_PRODUCTS);
		this.product = product;
		this.productLayout = productLayout;
		this.transactionType = transactionType;
		lotBll = LotBll.getInstance();
		productBll = ProductBll.getInstance();
		warehouseBll = WarehouseBll.getInstance();
		addListTab();
	}

	public LotLayout(Warehouse warehouse) throws IOException {
		super("Lotes", KEY_PRODUCTS);
		this.warehouse = warehouse;
		lotBll = LotBll.getInstance();
		productBll = ProductBll.getInstance();
		warehouseBll = WarehouseBll.getInstance();
		addListTab();
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
		lotGrid.setHeight("200px");
		lotGrid.addColumn(Lot::getCode).setCaption("Código");
		columnWarehouse = lotGrid.addColumn(lot -> {
			if (lot != null && lot.getWarehouse() != null) {
				return lot.getWarehouse().getName();
			} else {
				return null;
			}
		}).setCaption("Bodega");
		columnQuantity = lotGrid.addColumn(Lot::getQuantity).setCaption("Cantidad de productos");
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

		footer = lotGrid.prependFooterRow();
		footer.getCell(columnWarehouse).setHtml("<b>Totat cantidad:</b>");
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

		Integer sequence = getLotSequence();
		txtCode = new TextField("Código");
		txtCode.setStyleName(ValoTheme.TEXTAREA_TINY);
		txtCode.focus();
		txtCode.setValue(entity != null ? entity.getCode() : sequence != null ? String.valueOf(sequence) : "");
		txtCode.setRequiredIndicatorVisible(true);

		txtName = new TextField("Nombre");
		txtName.setStyleName(ValoTheme.TEXTAREA_TINY);
		txtName.setValue(entity != null ? entity.getName() : "");

		dtfFabricationDate = new DateTimeField("Fecha de fabricación");
		dtfFabricationDate.setDateFormat(Commons.FORMAT_DATE_TIME);
		dtfFabricationDate.setStyleName(ValoTheme.DATEFIELD_TINY);
		dtfFabricationDate.setValue(entity != null ? DateUtil.dateToLocalDateTime(entity.getLotDate()) : null);

		dtfExpirationDate = new DateTimeField("Fecha de vencimiento");
		dtfExpirationDate.setDateFormat(Commons.FORMAT_DATE_TIME);
		dtfExpirationDate.setStyleName(ValoTheme.DATEFIELD_TINY);
		dtfExpirationDate.setValue(entity != null ? DateUtil.dateToLocalDateTime(entity.getExpirationDate()) : null);

		txtQuantity = new NumberField("Cantidad");
		txtQuantity.setStyleName(ValoTheme.TEXTAREA_TINY);
		txtQuantity.setRequiredIndicatorVisible(true);
		txtQuantity.setValue(entity != null ? String.valueOf(entity.getQuantity()) : "");

		cbWarehouse = new ComboBox<>("Bodega");
		cbWarehouse.setEmptySelectionCaption("Seleccione");
		cbWarehouse.setStyleName(ValoTheme.COMBOBOX_TINY);
		cbWarehouse.setRequiredIndicatorVisible(true);
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

		if (product != null) {
			dataProvider = new ListDataProvider<>(lotBll.select(product));
		} else if (warehouse != null) {
			dataProvider = new ListDataProvider<>(lotBll.select(warehouse));
		}
		if (dataProvider != null) {
			filterLotDataProvider = dataProvider.withConfigurableFilter();
			lotGrid.setDataProvider(filterLotDataProvider);
		}
		dataProvider.addDataProviderListener(
				event -> footer.getCell(columnQuantity).setHtml(calculateTotalQuantity(dataProvider)));
		dataProvider.refreshAll();
	}

	@Override
	protected void saveButtonAction(Lot entity) {
		String strLog = "[saveButtonAction]";
		String confirmMsg = null;
		try {
			String message = validateRequiredFields();
			if (!message.isEmpty()) {
				throw new Exception(message);
			} else {
				Lot.Builder lotBuilder = null;
				if (entity == null) {
					lotBuilder = Lot.builder();
				} else {
					lotBuilder = Lot.builder(entity);
				}

				Warehouse warehouse = cbWarehouse.getSelectedItem().isPresent() ? cbWarehouse.getSelectedItem().get()
						: null;
				entity = lotBuilder.code(txtCode.getValue()).name(txtName.getValue())
						.lotDate(DateUtil.localDateTimeToDate(dtfFabricationDate.getValue()))
						.expirationDate(DateUtil.localDateTimeToDate(dtfExpirationDate.getValue())).archived(false)
						.quantity(Double.parseDouble(txtQuantity.getValue())).product(product).warehouse(warehouse)
						.build();
				// Guardar el lote
				save(lotBll, entity, null);
				log.info("Lote guardado: " + entity);

				// Actualizar stock del product
				product.setStock(totalStock);
				product.setStockDate(new Date());
				productBll.save(product);
				log.info("Stock actualizado: " + totalStock);

				if (!productLayout.isShowConfirmMessage()) {
					confirmMsg = "Producto guardado con éxito";
				} else {
					confirmMsg = "Lote guardado";
				}

				ViewHelper.showNotification(confirmMsg, Notification.Type.WARNING_MESSAGE);

			}

		} catch (Exception e) {
			log.error(strLog + "[Exception]" + e.getMessage());
			ViewHelper.showNotification("Se generó un error al guardar el lote", Notification.Type.ERROR_MESSAGE);

		}

	}

	private String validateRequiredFields() {
		String strLog = "[validateRequiredFields] ";
		String message = "";
		try {

			String character = "|";

			if (!cbWarehouse.getSelectedItem().isPresent()) {
				message = "La bodega es obligatoria";
			}
			if (txtQuantity.getValue() == null || txtQuantity.getValue().isEmpty()) {
				if (!message.isEmpty()) {
					message = message.concat(character);
				}
				message = message.concat("La cantidad del lote es obligatoria");
			}

		} catch (Exception e) {
			log.error(strLog + "[Exception]" + e.getMessage());
		}
		return message;

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
		String strLog = "[delete]";
		try {
			entity = Lot.builder(entity).archived(true).build();
			save(lotBll, entity, "Lote borrado");
		} catch (Exception e) {
			log.error(strLog + "[Exception]" + e.getMessage());
			ViewHelper.showNotification("Se generó un error al eliminar el lote", Notification.Type.ERROR_MESSAGE);
		}

	}

	/**
	 * Obtener el consecutivo para lotes
	 */
	private Integer getLotSequence() {
		String strLog = "[getLotSequence] ";
		Lot lastLot = null;
		Integer maxCode = 1;
		try {
			if (product == null) {
				productLayout.setShowConfirmMessage(false);
				productLayout.saveButtonAction(product);
				product = productLayout.getProduct();
			} else {
				productLayout.setShowConfirmMessage(true);
			}

			if (product != null) {
				lastLot = lotBll.getLastLotByProduct(product);
				if (lastLot != null) {
					maxCode = Integer.parseInt(lastLot.getCode()) + 1;
				}
			}
		} catch (Exception e) {
			log.error(strLog + "[Exception]" + e.getMessage());
		}
		return maxCode;
	}

	private String calculateTotalQuantity(ListDataProvider<Lot> dataProvider) {
		String strLog = "[calculateTotalQuantity]";

		try {
			log.info(strLog + "[parameters]" + dataProvider);
			totalStock = dataProvider.fetch(new Query<>()).mapToDouble(lot -> {
				return lot.getQuantity();
			}).sum();

			return "<b>" + totalStock + "</b>";
		} catch (Exception e) {
			log.error(strLog + "[Exception]" + e.getMessage());
		}

		log.info("total->" + totalStock);

		return null;
	}

	public Double getTotalStock() {
		return totalStock;
	}

	public void setTotalStock(Double totalStock) {
		this.totalStock = totalStock;
	}

}
