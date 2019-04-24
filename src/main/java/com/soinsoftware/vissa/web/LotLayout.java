package com.soinsoftware.vissa.web;

import static com.soinsoftware.vissa.web.VissaUI.KEY_LOTS;

import java.io.IOException;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.apache.log4j.Logger;
import org.jsoup.helper.StringUtil;
import org.springframework.scheduling.annotation.Async;
import org.vaadin.ui.NumberField;

import com.soinsoftware.vissa.bll.DocumentDetailLotBll;
import com.soinsoftware.vissa.bll.LotBll;
import com.soinsoftware.vissa.bll.MeasurementUnitLotBll;
import com.soinsoftware.vissa.bll.MeasurementUnitProductBll;
import com.soinsoftware.vissa.bll.MuEquivalenceBll;
import com.soinsoftware.vissa.bll.ProductBll;
import com.soinsoftware.vissa.bll.WarehouseBll;
import com.soinsoftware.vissa.common.CommonsConstants;
import com.soinsoftware.vissa.model.DocumentDetailLot;
import com.soinsoftware.vissa.model.Lot;
import com.soinsoftware.vissa.model.MeasurementUnit;
import com.soinsoftware.vissa.model.MeasurementUnitLot;
import com.soinsoftware.vissa.model.MeasurementUnitProduct;
import com.soinsoftware.vissa.model.MuEquivalence;
import com.soinsoftware.vissa.model.Product;
import com.soinsoftware.vissa.model.Warehouse;
import com.soinsoftware.vissa.util.Commons;
import com.soinsoftware.vissa.util.DateUtil;
import com.soinsoftware.vissa.util.ELayoutMode;
import com.soinsoftware.vissa.util.ViewHelper;
import com.vaadin.data.provider.ListDataProvider;
import com.vaadin.data.provider.Query;
import com.vaadin.ui.AbstractOrderedLayout;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.DateField;
import com.vaadin.ui.FormLayout;
import com.vaadin.ui.Grid;
import com.vaadin.ui.Grid.Column;
import com.vaadin.ui.Grid.SelectionMode;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Panel;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.components.grid.HeaderRow;
import com.vaadin.ui.renderers.ButtonRenderer;
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
	private final MeasurementUnitProductBll measurementUnitProductBll;
	private final MuEquivalenceBll muEquivalencesBll;
	private final DocumentDetailLotBll documentDetailLotBll;
	private final MeasurementUnitLotBll measurementUnitLotBll;

	public Grid<Lot> lotGrid;
	public Grid<DocumentDetailLot> detailLotGrid;

	private TextField txtCode;
	private TextField txtName;
	private DateField dtFabricationDate;
	private DateField dtExpirationDate;
	private NumberField txtQuantity;
	private ComboBox<Warehouse> cbWarehouse;
	private ComboBox<MeasurementUnit> cbMeasurementUnit;
	private TextField txtProductNameFilter;
	private ComboBox<Warehouse> txtWarehouseFilter;
	private CheckBox checkStockFilter;

	private Product product;
	private Warehouse warehouse;
	private Double totalStock;

	private ProductLayout productLayout;
	private InvoiceLayout invoiceLayout;
	private Column<?, ?> columnQuantity;
	private Column<?, ?> columnWarehouse;
	private HeaderRow footer;

	private Window muProductSubwindow;
	private MuProductLayout muProductLayout = null;

	private ListDataProvider<Lot> dataProvider = null;
	private ListDataProvider<MeasurementUnit> measurementDataProv;
	private ELayoutMode modeLayout;
	private Lot lot;

	private MuLotLayout muLotLayout;

	public LotLayout(Product product, ProductLayout productLayout) throws IOException {
		super("Lotes", KEY_LOTS);
		this.product = product;
		this.productLayout = productLayout;
		lotBll = LotBll.getInstance();
		productBll = ProductBll.getInstance();
		warehouseBll = WarehouseBll.getInstance();
		measurementUnitProductBll = MeasurementUnitProductBll.getInstance();
		muEquivalencesBll = MuEquivalenceBll.getInstance();
		documentDetailLotBll = DocumentDetailLotBll.getInstance();
		measurementUnitLotBll = MeasurementUnitLotBll.getInstance();
		modeLayout = Commons.LAYOUT_MODE;
		addListTab();
	}

	public LotLayout(Product product, InvoiceLayout invoiceLayout) throws IOException {
		super("Lotes", KEY_LOTS);
		this.product = product;
		this.invoiceLayout = invoiceLayout;
		lotBll = LotBll.getInstance();
		productBll = ProductBll.getInstance();
		warehouseBll = WarehouseBll.getInstance();
		measurementUnitProductBll = MeasurementUnitProductBll.getInstance();
		muEquivalencesBll = MuEquivalenceBll.getInstance();
		documentDetailLotBll = DocumentDetailLotBll.getInstance();
		measurementUnitLotBll = MeasurementUnitLotBll.getInstance();
		modeLayout = Commons.LAYOUT_MODE;
		if (modeLayout.equals(ELayoutMode.NEW)) {
			addListTab();
			newButtonAction();
		}
	}

	public LotLayout(Warehouse warehouse) throws IOException {
		super("Lotes", KEY_LOTS);
		this.warehouse = warehouse;
		lotBll = LotBll.getInstance();
		productBll = ProductBll.getInstance();
		warehouseBll = WarehouseBll.getInstance();
		measurementUnitProductBll = MeasurementUnitProductBll.getInstance();
		muEquivalencesBll = MuEquivalenceBll.getInstance();
		documentDetailLotBll = DocumentDetailLotBll.getInstance();
		measurementUnitLotBll = MeasurementUnitLotBll.getInstance();
		modeLayout = Commons.LAYOUT_MODE;
		addListTab();

	}

	public LotLayout() throws IOException {
		super("Lotes", KEY_LOTS);

		lotBll = LotBll.getInstance();
		productBll = ProductBll.getInstance();
		warehouseBll = WarehouseBll.getInstance();
		measurementUnitProductBll = MeasurementUnitProductBll.getInstance();
		muEquivalencesBll = MuEquivalenceBll.getInstance();
		documentDetailLotBll = DocumentDetailLotBll.getInstance();
		measurementUnitLotBll = MeasurementUnitLotBll.getInstance();
		modeLayout = ELayoutMode.REPORT;
		log.info("modeLayout:" + modeLayout);
	}

	@Override
	public AbstractOrderedLayout buildListView() {
		VerticalLayout layout = ViewHelper.buildVerticalLayout(false, false);
		if (modeLayout.equals(ELayoutMode.ALL)) {
			Panel buttonPanel = buildButtonPanelForLists();
			layout.addComponent(buttonPanel);
		}
		// if (modeLayout != null && modeLayout.equals(ELayoutMode.REPORT)) {
		Panel filterPanel = buildFilterPanel();

		// }
		layout.addComponent(filterPanel);
		Panel dataPanel = buildGridPanel();
		layout.addComponent(dataPanel);
		this.setSpacing(false);
		this.setMargin(false);
		return layout;
	}

	@Override
	protected AbstractOrderedLayout buildEditionView(Lot entity) {
		VerticalLayout layout = ViewHelper.buildVerticalLayout(false, false);
		if (!modeLayout.equals(ELayoutMode.REPORT)) {
			Panel buttonPanel = buildButtonPanelForEdition(entity);
			layout.addComponents(buttonPanel);
		}

		Component dataPanel = buildEditionComponent(entity);
		layout.addComponents(dataPanel);
		return layout;
	}

	@SuppressWarnings("rawtypes")
	@Override
	protected Panel buildGridPanel() {
		String strLog = "[buildGridPanel] ";
		try {
			lotGrid = ViewHelper.buildGrid(SelectionMode.SINGLE);

			if (modeLayout.equals(ELayoutMode.REPORT)) {
				lotGrid.addColumn(lot -> {
					if (lot != null && lot.getProduct() != null) {
						return lot.getProduct().getName();
					} else {
						return null;
					}
				}).setCaption("Producto");
			}

			lotGrid.addColumn(Lot::getCode).setCaption("Código Lote");

			columnWarehouse = lotGrid.addColumn(lot -> {
				if (lot != null && lot.getWarehouse() != null) {
					return lot.getWarehouse().getName();
				} else {
					return null;
				}
			}).setCaption("Bodega");
			columnQuantity = lotGrid.addColumn(Lot::getQuantity).setCaption("Stock");
			lotGrid.addColumn(lot -> {
				if (lot != null && lot.getMeasurementUnit() != null) {
					return lot.getMeasurementUnit().getName();
				} else {
					return null;
				}
			}).setCaption("Unidad de medida");
			lotGrid.addColumn(lot -> {
				if (lot != null && lot.getExpirationDate() != null) {
					return DateUtil.dateToString(lot.getExpirationDate());
				} else {
					return null;
				}
			}).setCaption("Fecha de vencimiento");

			lotGrid.addColumn(lot -> "Unidad de medida", new ButtonRenderer(clickEvent -> {
				buidMuLotWindow((Lot) clickEvent.getItem());
			})).setCaption("Ver unidad de medida");

			lotGrid.setStyleName(ValoTheme.TABLE_SMALL);
			lotGrid.setHeight("300px");

			footer = lotGrid.prependHeaderRow();
			footer.getCell(columnWarehouse).setHtml("<b>Totat stock:</b>");
			fillGridData();

			// refreshGrid();

			lotGrid.addItemClickListener(listener -> {
				if (listener.getMouseEventDetails().isDoubleClick()) {
					clickAction(listener.getItem());
				}
			});

		} catch (Exception e) {
			log.error(strLog + "[Exception]" + e.getMessage());
			e.printStackTrace();

		}
		return ViewHelper.buildPanel(null, lotGrid);
	}

	private void clickAction(Lot lot) {
		if (modeLayout.equals(ELayoutMode.NEW)) {
			if (invoiceLayout != null) {
				invoiceLayout.selectLot(CommonsConstants.CURRENT_DOCUMENT_DETAIL, lot);
			}
		}
		if (modeLayout.equals(ELayoutMode.REPORT)) {
			lotGrid.select(lot);
			editButtonAction("Movimientos del lote");
		}
	}

	/**
	 * Metodo para configurar el stock por UM del lote
	 * 
	 * @param lot
	 */
	private void buidMuLotWindow(Lot lot) {
		String strLog = "[buidMuLotWindow] ";
		try {
			log.info(strLog + "[parameters] lot: " + lot);
			this.lot = lot;
			VerticalLayout subContent = ViewHelper.buildVerticalLayout(true, true);
			muLotLayout = new MuLotLayout(this);
			muLotLayout.setCaption("Lotes");
			muLotLayout.setMargin(false);
			muLotLayout.setSpacing(true);
			subContent.addComponent(muLotLayout);
			Window muLotWindow = ViewHelper.buildSubwindow("50%", null);
			muLotWindow.setContent(subContent);
			getUI().addWindow(muLotWindow);
		} catch (IOException e) {
			log.error(strLog + "[IOException] " + e.getMessage());
			e.printStackTrace();
		}

	}

	protected Panel buildDetailLotGridPanel(Lot lot) {

		detailLotGrid = ViewHelper.buildGrid(SelectionMode.SINGLE);

		detailLotGrid.addColumn(detailLot -> {
			if (detailLot != null && detailLot.getDocumentDetail() != null) {
				return detailLot.getDocumentDetail().getDocument().getCode();
			} else {
				return "";
			}
		}).setCaption("Factura");

		detailLotGrid.addColumn(detailLot -> {
			if (detailLot != null && detailLot.getDocumentDetail() != null) {
				return DateUtil.dateToString(detailLot.getDocumentDetail().getDocument().getDocumentDate(),
						Commons.FORMAT_DATE_TIME);
			} else {
				return "";
			}
		}).setCaption("Fecha");

		detailLotGrid.addColumn(detailLot -> {
			if (detailLot != null && detailLot.getDocumentDetail() != null) {
				return detailLot.getDocumentDetail().getDocument().getDocumentType().getName();
			} else {
				return "";
			}
		}).setCaption("Factura");

		detailLotGrid.addColumn(DocumentDetailLot::getInitialStockLot).setCaption("Stock inicial");
		detailLotGrid.addColumn(DocumentDetailLot::getQuantity).setCaption("Cantidad");
		detailLotGrid.addColumn(DocumentDetailLot::getFinalStockLot).setCaption("Stock final");

		detailLotGrid.setStyleName(ValoTheme.TABLE_SMALL);

		fillDetailLotGridData(lot);

		return ViewHelper.buildPanel("Movimientos del lote", detailLotGrid);
	}

	private Panel buildFilterPanel() {
		HorizontalLayout layout = ViewHelper.buildHorizontalLayout(true, true);
		txtProductNameFilter = new TextField("Nombre producto");
		txtProductNameFilter.setStyleName(ValoTheme.TEXTFIELD_TINY);
		txtProductNameFilter.addValueChangeListener(e -> refreshGrid());

		txtWarehouseFilter = new ComboBox<Warehouse>("Bodega");
		txtWarehouseFilter.setEmptySelectionAllowed(true);
		txtWarehouseFilter.setEmptySelectionCaption("Seleccione");
		txtWarehouseFilter.setStyleName(ValoTheme.COMBOBOX_TINY);

		ListDataProvider<Warehouse> warehouseData = new ListDataProvider<>(warehouseBll.selectAll(false));
		txtWarehouseFilter.setDataProvider(warehouseData);
		txtWarehouseFilter.setItemCaptionGenerator(Warehouse::getName);
		txtWarehouseFilter.addValueChangeListener(e -> refreshGrid());

		checkStockFilter = new CheckBox("Stock en 0");
		checkStockFilter.setStyleName(ValoTheme.CHECKBOX_SMALL);
		checkStockFilter.addValueChangeListener(e -> refreshGrid());

		layout.addComponents(txtProductNameFilter, txtWarehouseFilter, checkStockFilter);
		layout.setComponentAlignment(checkStockFilter, Alignment.BOTTOM_CENTER);

		return ViewHelper.buildPanel("Filtrar por", layout);
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
		VerticalLayout layout = ViewHelper.buildVerticalLayout(false, false);

		if (modeLayout.equals(ELayoutMode.REPORT)) {
			layout.addComponents(buildDetailLotGridPanel(entity));
		} else {
			Integer sequence = getLotSequence();
			txtCode = new TextField("Código");
			txtCode.setStyleName(ValoTheme.TEXTAREA_TINY);
			txtCode.focus();
			txtCode.setValue(entity != null ? entity.getCode() : sequence != null ? String.valueOf(sequence) : "");
			txtCode.setRequiredIndicatorVisible(true);
			txtCode.setReadOnly(true);

			txtName = new TextField("Nombre");
			txtName.setStyleName(ValoTheme.TEXTAREA_TINY);
			txtName.setValue(entity != null ? entity.getName() : "");

			dtFabricationDate = new DateField("Fecha de fabricación");
			dtFabricationDate.setDateFormat(Commons.FORMAT_DATE);
			dtFabricationDate.setStyleName(ValoTheme.DATEFIELD_TINY);
			dtFabricationDate.setValue(entity != null ? DateUtil.dateToLocalDate(entity.getLotDate()) : null);

			dtExpirationDate = new DateField("Fecha de vencimiento");
			dtExpirationDate.setDateFormat(Commons.FORMAT_DATE);
			dtExpirationDate.setStyleName(ValoTheme.DATEFIELD_TINY);
			dtExpirationDate.setValue(entity != null ? DateUtil.dateToLocalDate(entity.getExpirationDate()) : null);

			txtQuantity = new NumberField("Cantidad");
			txtQuantity.setStyleName(ValoTheme.TEXTAREA_TINY);
			txtQuantity.setRequiredIndicatorVisible(true);
			txtQuantity.setValue(entity != null ? String.valueOf(entity.getQuantity()) : "");

			cbMeasurementUnit = new ComboBox<>("Unidad de medida");
			cbMeasurementUnit.setEmptySelectionCaption("Seleccione");
			cbMeasurementUnit.setStyleName(ValoTheme.COMBOBOX_TINY);
			cbMeasurementUnit.setEmptySelectionAllowed(false);
			cbMeasurementUnit.setRequiredIndicatorVisible(true);
			fillMeasurementUnit();
			cbMeasurementUnit.setItemCaptionGenerator(MeasurementUnit::getName);
			cbMeasurementUnit.setValue(entity != null ? entity.getMeasurementUnit() : null);

			Button muNewBtn = new Button("Nueva unidad de medida");
			muNewBtn.addStyleNames(ValoTheme.BUTTON_LINK, ValoTheme.BUTTON_TINY);
			muNewBtn.addClickListener(e -> buildMuProductWindow(product));

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

			form.addComponents(txtCode, txtName, dtFabricationDate, dtExpirationDate, txtQuantity, cbMeasurementUnit,
					muNewBtn, cbWarehouse);

			layout.addComponents(form);

		}
		return layout;
	}

	@Override
	protected void fillGridData() {
		String strLog = "[fillGridData] ";
		try {
			List<Lot> lots = null;
			if (product != null) {
				lots = lotBll.select(product);
			} else if (warehouse != null) {
				lots = lotBll.select(warehouse);
			} else if (productLayout != null) {
				lots = null;
			} else {
				lots = lotBll.selectAll(false);
			}

			if (lots != null) {
				Comparator<Lot> comparator = (h1, h2) -> new Integer((h1.getCode()))
						.compareTo(new Integer(h2.getCode()));
				lots.sort(comparator.reversed());

				dataProvider = new ListDataProvider<>(lots);
				if (dataProvider != null) {
					lotGrid.setDataProvider(dataProvider);
				}
				if (dataProvider != null) {
					dataProvider.addDataProviderListener(
							event -> footer.getCell(columnQuantity).setHtml(calculateTotalQuantity(dataProvider)));
					refreshGrid();
				}
			}

		} catch (Exception e) {
			log.error(strLog + "[Exception]" + e.getMessage());
			e.printStackTrace();
		}
	}

	protected void fillDetailLotGridData(Lot lot) {
		String strLog = "[fillDetailLotGridData]";
		try {

			ListDataProvider<DocumentDetailLot> dataProvider = new ListDataProvider<>(documentDetailLotBll.select(lot));
			detailLotGrid.setDataProvider(dataProvider);

		} catch (Exception e) {
			log.error(strLog + "[Exception]" + e.getMessage());
		}
	}

	private void fillMeasurementUnit() {
		measurementDataProv = new ListDataProvider<>(measurementUnitProductBll.selectMuByProduct(product));
		cbMeasurementUnit.setDataProvider(measurementDataProv);
	}

	@Override
	protected void saveButtonAction(Lot entity) {
		String strLog = "[saveButtonAction] ";
		String confirmMsg = null;
		boolean isNew = false;
		try {
			String message = validateRequiredFields();
			if (!message.isEmpty()) {
				ViewHelper.showNotification(message, Notification.Type.ERROR_MESSAGE);
			} else {
				Lot.Builder lotBuilder = null;
				if (entity == null) {
					lotBuilder = Lot.builder();
				} else {
					lotBuilder = Lot.builder(entity);
				}
				// Bandera para indicar que el lote es nuevo y se puede usar
				isNew = true;
				MeasurementUnit measurementUnit = cbMeasurementUnit.getSelectedItem().isPresent()
						? cbMeasurementUnit.getSelectedItem().get()
						: null;
				Warehouse warehouse = cbWarehouse.getSelectedItem().isPresent() ? cbWarehouse.getSelectedItem().get()
						: null;
				lot = lotBuilder.code(txtCode.getValue()).name(txtName.getValue())
						.lotDate(DateUtil.localDateToDate(dtFabricationDate.getValue()))
						.expirationDate(DateUtil.localDateToDate(dtExpirationDate.getValue())).archived(false)
						.quantity(Double.parseDouble(txtQuantity.getValue())).measurementUnit(measurementUnit)
						.product(product).warehouse(warehouse).isNew(isNew).build();

				if (invoiceLayout != null && modeLayout.equals(ELayoutMode.NEW)) {
					invoiceLayout.setSelectedLot(lot);
					invoiceLayout.selectLot(CommonsConstants.CURRENT_DOCUMENT_DETAIL, lot);
					invoiceLayout.getLotSubwindow().close();
					invoiceLayout.getDetailGrid().focus();
				} else {
					// Guardar el lote
					save(lotBll, lot, null);

					if (!hasError) {
						log.info(strLog + "Lote guardado: " + lot);
						// Actualizar stock del product
						product.setStock(totalStock);
						product.setStockDate(new Date());
						productBll.save(product);
						log.info(strLog + "Stock actualizado: " + totalStock);

						// Actualizar el stock por cada UM del producto
						updateStockByMU(measurementUnit);

						if (productLayout != null && !productLayout.isShowConfirmMessage()) {
							confirmMsg = "Producto guardado con éxito";
						} else {
							confirmMsg = "Lote guardado";
						}

						ViewHelper.showNotification(confirmMsg, Notification.Type.WARNING_MESSAGE);
						if (productLayout != null) {
							productLayout.updateProductLayout(product);
						}
					}

				}

			}

		} catch (Exception e) {
			log.error(strLog + "[Exception]" + e.getMessage());
			e.printStackTrace();
			ViewHelper.showNotification("Se generó un error al guardar el lote", Notification.Type.ERROR_MESSAGE);
		}
	}

	/**
	 * Actualizar equivalencia de stock por cada Unidad de Medida del producto
	 * 
	 * @param measurementUnit
	 */
	private void updateStockByMU(MeasurementUnit measurementUnit) {
		String strLog = "[updateStockByMU] ";
		try {
			List<MeasurementUnitProduct> muProductList = measurementUnitProductBll.select(product);
			for (MeasurementUnitProduct muProduct : muProductList) {
				if (!muProduct.getMeasurementUnit().equals(measurementUnit)) {
					Double newStock = convertMU(totalStock, measurementUnit, muProduct.getMeasurementUnit());
					muProduct.setStock(newStock);

				} else {
					muProduct.setStock(totalStock);
				}
				measurementUnitProductBll.save(muProduct);

				log.info(strLog + " stock actuaizado para UM: " + muProduct);
			}
		} catch (Exception e) {
			log.error(strLog + "[Exception]" + e.getMessage());
		}
	}

	/**
	 * Metodo asincrono que actualizar el stock por UM en el lote y producto
	 * 
	 * @param product
	 */
	@Async
	public void updateStock(Product product) {
		String strLog = "[updateStock] ";
		Map<MeasurementUnit, Double> muMap = new HashMap<MeasurementUnit, Double>();
		try {
			List<Lot> lots = lotBll.selecLotWithStock(product);
			for (Lot lot : lots) {
				List<MeasurementUnitLot> muLotList = measurementUnitLotBll.select(lot);
				for (MeasurementUnitLot muLot : muLotList) {
					MeasurementUnit mu = muLot.getMuProduct().getMeasurementUnit();
					Double cant = muMap.get(mu);
					Double stock = muMap.get(mu) + muLot.getStock();
					muMap.put(mu, stock);
				}
			}

		} catch (Exception e) {
			log.error(strLog + "[Exception] " + e.getMessage());
		}

	}

	private Double convertMU(Double quantity, MeasurementUnit muSource, MeasurementUnit muTarget) {
		String strLog = "[convertMu]";
		Double stock = 0.0;
		try {

			MuEquivalence muEquivalence = muEquivalencesBll.select(muSource, muTarget);
			if (muEquivalence != null) {
				Double sourceFactor = Double.parseDouble(muEquivalence.getMuSourceFactor());
				Double targetFactor = Double.parseDouble(muEquivalence.getMuTargetFactor());
				// Se calcula la equivalencia por la UM
				stock = (quantity * sourceFactor) * targetFactor;
			} else {
				String msg = " No hay equivalencias configuradas para las UM : " + muSource + " y " + muTarget;
				log.info(strLog + msg);
				ViewHelper.showNotification(msg, Notification.Type.ERROR_MESSAGE);
			}

		} catch (Exception e) {
			log.error(strLog + "[Exception]" + e.getMessage());
		}
		return stock;
	}

	private String validateRequiredFields() {
		String strLog = "[validateRequiredFields] ";
		String message = "";
		try {

			String character = "|";

			if (!cbMeasurementUnit.getSelectedItem().isPresent()) {
				if (!message.isEmpty()) {
					message = message.concat(character);
				}
				message = "La unidad de medida es obligatoria";
			}
			if (!cbWarehouse.getSelectedItem().isPresent()) {
				if (!message.isEmpty()) {
					message = message.concat(character);
				}
				message = message.concat("La bodega es obligatoria");
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
		String strLog = "[delete] ";
		try {
			entity = Lot.builder(entity).archived(true).build();
			save(lotBll, entity, "Lote borrado");
		} catch (Exception e) {
			log.error(strLog + "[Exception]" + e.getMessage());
			ViewHelper.showNotification("Se generó un error al eliminar el lote", Notification.Type.ERROR_MESSAGE);
		}
	}

	/**
	 * Metodo para recargar los registros de la grid
	 */
	private void refreshGrid() {
		if (dataProvider != null) {
			dataProvider.setFilter(lot -> filterGrid(lot));
		}
	}

	/**
	 * Metodo para filtrar los registros de la grid
	 * 
	 * @param lot
	 * @return
	 */
	private boolean filterGrid(Lot lot) {
		String strLog = "[filterGrid] ";
		boolean result = false;
		try {

			result = lot.getQuantity() > 0;

			boolean isZero = false;

			if (checkStockFilter != null) {
				isZero = checkStockFilter.getValue();
			}

			result = (isZero ? (lot.getQuantity().equals(0.0)) : lot.getQuantity() > 0);

			// Filtro por nombre del producto
			String productNameFilter = !StringUtil.isBlank(txtProductNameFilter.getValue())
					? txtProductNameFilter.getValue()
					: "";
			if (!StringUtil.isBlank(productNameFilter)) {
				result = result && lot.getProduct().getName().toUpperCase().contains(productNameFilter.toUpperCase());
			}

			// Filtro por bodega
			if (txtWarehouseFilter.getSelectedItem().isPresent()) {
				Warehouse warehouseFilter = txtWarehouseFilter.getSelectedItem().get();
				if (warehouseFilter != null) {
					result = result && lot.getWarehouse().equals(warehouseFilter);
				}
			}

		} catch (Exception e) {
			log.error(strLog + "[Exception]" + e.getMessage());
			e.printStackTrace();
		}
		return result;
	}

	/**
	 * Obtener el consecutivo para lotes
	 */
	public Integer getLotSequence() {
		String strLog = "[getLotSequence] ";
		Lot lastLot = null;
		Integer maxCode = 1;
		try {
			if (product == null) {
				if (productLayout != null) {
					productLayout.setShowConfirmMessage(false);
					productLayout.saveButtonAction(product);
					product = productLayout.getProduct();
				}
			} else {
				if (productLayout != null) {
					productLayout.setShowConfirmMessage(true);
				}
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

	/**
	 * Metodo que construye la ventana para buscar UM por producto
	 */
	private void buildMuProductWindow(Product product) {
		String strLog = "[buildMuProductWindow]";
		try {

			muProductSubwindow = ViewHelper.buildSubwindow("70%", "90%");
			muProductSubwindow
					.setCaption("Unidades de medida del producto " + product.getCode() + " - " + product.getName());

			VerticalLayout subContent = ViewHelper.buildVerticalLayout(true, true);

			muProductLayout = new MuProductLayout(this, product);

			muProductLayout.setMargin(false);
			muProductLayout.setSpacing(false);
			muProductLayout.getMuProductGrid().addItemClickListener(listener -> {
				if (listener.getMouseEventDetails().isDoubleClick()) {
					log.info("addItemClickListener");
					selectMuProduct(listener.getItem());
				}
			});
			subContent.addComponents(muProductLayout);

			muProductSubwindow.setContent(subContent);
			muProductSubwindow.addCloseListener(e -> {
				fillMeasurementUnit();
			});
			getUI().addWindow(muProductSubwindow);

		} catch (Exception e) {
			log.error(strLog + "[Exception]" + e.getMessage());
		}
	}

	private void selectMuProduct(MeasurementUnitProduct muProduct) {
		cbMeasurementUnit.setValue(muProduct.getMeasurementUnit());
	}

	public Double getTotalStock() {
		return totalStock;
	}

	public void setTotalStock(Double totalStock) {
		this.totalStock = totalStock;
	}

	public Grid<Lot> getLotGrid() {
		return lotGrid;
	}

	public void setLotGrid(Grid<Lot> lotGrid) {
		this.lotGrid = lotGrid;
	}

	public Window getMuProductSubwindow() {
		return muProductSubwindow;
	}

	public void setMuProductSubwindow(Window muProductSubwindow) {
		this.muProductSubwindow = muProductSubwindow;
	}

	public Lot getLot() {
		return lot;
	}

	public void setLot(Lot lot) {
		this.lot = lot;
	}

}