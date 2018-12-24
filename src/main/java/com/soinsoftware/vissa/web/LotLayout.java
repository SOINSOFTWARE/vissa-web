package com.soinsoftware.vissa.web;

import java.io.IOException;
import java.util.Set;

import org.apache.log4j.Logger;

import com.soinsoftware.vissa.bll.LotBll;
import com.soinsoftware.vissa.model.Lot;
import com.soinsoftware.vissa.model.Product;
import com.soinsoftware.vissa.util.DateUtil;
import com.soinsoftware.vissa.util.ViewHelper;
import com.vaadin.data.provider.ConfigurableFilterDataProvider;
import com.vaadin.data.provider.ListDataProvider;
import com.vaadin.server.SerializablePredicate;
import com.vaadin.ui.AbstractOrderedLayout;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.DateField;
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

	private Grid<Lot> lotGrid;

	private TextField txtCode;
	private TextField txtName;
	private DateField txtFabricationDate;
	private DateField txtExpirationDate;
	private TextField txtQuantity;

	private Product product;

	private ConfigurableFilterDataProvider<Lot, Void, SerializablePredicate<Lot>> filterLotDataProvider;

	public LotLayout(Product prod) throws IOException {
		super("Lotes");
		product = prod;
		lotBll = LotBll.getInstance();
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
		lotGrid = ViewHelper.buildGrid(SelectionMode.SINGLE);
		lotGrid.setSizeFull();
		lotGrid.addColumn(Lot::getCode).setCaption("Código");
		lotGrid.addColumn(Lot::getLotDate).setCaption("Fecha de fabricación");
		lotGrid.addColumn(Lot::getExpirationDate).setCaption("Fecha de vencimiento");
		lotGrid.addColumn(Lot::getQuantity).setCaption("Cantidad de productos");
		fillGridData();
		return ViewHelper.buildPanel(null, lotGrid);
	}

	protected Panel buildButtonPanelForLists() {
		HorizontalLayout layout = ViewHelper.buildHorizontalLayout(true, true);
		Button btNew = buildButtonForNewAction(ValoTheme.BUTTON_SMALL);
		Button btEdit = buildButtonForEditAction(ValoTheme.BUTTON_TINY);
		Button btDelete = buildButtonForDeleteAction(ValoTheme.BUTTON_TINY);
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
		txtCode.setValue(entity != null ? entity.getCode() : "");
		
		
		txtName = new TextField("Nombre");
		txtName.setValue(entity != null ? entity.getName() : "");

		txtFabricationDate = new DateField("Fecha de fabricación");
		txtFabricationDate.setValue(entity != null ? DateUtil.dateToLocalDate(entity.getLotDate()) : null);

		txtExpirationDate = new DateField("Fecha de vencimiento");
		txtExpirationDate.setValue(entity != null ? DateUtil.dateToLocalDate(entity.getExpirationDate()) : null);

		txtQuantity = new TextField("Cantidad");
		txtQuantity.setValue(entity != null ? String.valueOf(entity.getQuantity()) : "");

		FormLayout form = new FormLayout();
		form.setMargin(true);
		form.setCaption("Datos del producto");
		form.setCaptionAsHtml(true);
		form.setSizeFull();
		form.setWidth("50%");
		form.addStyleName(ValoTheme.FORMLAYOUT_LIGHT);

		form.addComponents(txtCode, txtName, txtFabricationDate, txtExpirationDate, txtQuantity);

		layout.addComponents(form);

		return layout;
	}

	@Override
	protected void fillGridData() {
		ListDataProvider<Lot> dataProvider = new ListDataProvider<>(lotBll.select(product));
		filterLotDataProvider = dataProvider.withConfigurableFilter();
		lotGrid.setDataProvider(filterLotDataProvider);

	}

	@Override
	protected void saveButtonAction(Lot entity) {
		Lot.Builder lotBuilder = null;
		if (entity == null) {
			lotBuilder = Lot.builder();
		} else {
			lotBuilder = Lot.builder(entity);
		}
		entity = lotBuilder.code(txtCode.getValue()).name(txtName.getValue())
				.lotDate(DateUtil.localDateToDate(txtFabricationDate.getValue()))
				.expirationDate(DateUtil.localDateToDate(txtExpirationDate.getValue())).archived(false)
				.quantity(Integer.parseInt(txtQuantity.getValue()))
				.product(product).build();
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

	private void refreshGrid() {
		lotGrid.getDataProvider().refreshAll();
	}

}
