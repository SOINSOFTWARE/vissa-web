package com.soinsoftware.vissa.web;

import static com.soinsoftware.vissa.web.VissaUI.KEY_INVOICES;

import java.io.IOException;
import java.util.Date;
import java.util.Set;

import org.apache.log4j.Logger;
import org.springframework.transaction.annotation.Transactional;

import com.soinsoftware.vissa.bll.DocumentDetailBll;
import com.soinsoftware.vissa.bll.ProductCategoryBll;
import com.soinsoftware.vissa.model.Document;
import com.soinsoftware.vissa.model.DocumentDetail;
import com.soinsoftware.vissa.model.ProductCategory;
import com.soinsoftware.vissa.util.Commons;
import com.soinsoftware.vissa.util.DateUtil;
import com.soinsoftware.vissa.util.ViewHelper;
import com.vaadin.data.provider.ConfigurableFilterDataProvider;
import com.vaadin.data.provider.ListDataProvider;
import com.vaadin.data.provider.Query;
import com.vaadin.server.SerializablePredicate;
import com.vaadin.shared.ui.datefield.DateTimeResolution;
import com.vaadin.ui.AbstractOrderedLayout;
import com.vaadin.ui.Button;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.DateTimeField;
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
public class ProductReportLayout extends AbstractEditableLayout<DocumentDetail> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5076502522106126046L;

	protected static final Logger log = Logger.getLogger(ProductReportLayout.class);

	private final DocumentDetailBll documentDetailBll;
	private final ProductCategoryBll productCategoryBll;
	private TextField txtFilterByProductCode;
	private ComboBox<ProductCategory> cbFilterByCategory;
	private DateTimeField dtfFilterIniDate;
	private DateTimeField dtfFilterEndDate;
	private TextField txtTotal;
	private TextField txtQuantity;
	private Grid<DocumentDetail> grid;
	private FooterRow footer;
	private Column<?, ?> subtotalColumn;
	private Column<?, ?> quantityColumn;

	private ConfigurableFilterDataProvider<DocumentDetail, Void, SerializablePredicate<DocumentDetail>> filterDataProvider;
	private ListDataProvider<DocumentDetail> dataProvider;

	public ProductReportLayout() throws IOException {
		super("", KEY_INVOICES);

		documentDetailBll = DocumentDetailBll.getInstance();
		productCategoryBll = ProductCategoryBll.getInstance();

	}

	@Override
	protected AbstractOrderedLayout buildListView() {
		VerticalLayout layout = ViewHelper.buildVerticalLayout(false, false);
		Panel buttonPanel = buildButtonPanelForLists();

		Panel filterPanel = buildFilterPanel();
		Panel totalPanel = builTotalPanel();
		Panel dataPanel = buildGridPanel();
		layout.addComponents(filterPanel, totalPanel, dataPanel);
		this.setSpacing(false);
		this.setMargin(false);
		return layout;
	}

	@Override
	protected AbstractOrderedLayout buildEditionView(DocumentDetail entity) {
		VerticalLayout layout = ViewHelper.buildVerticalLayout(false, false);
		Panel buttonPanel = buildButtonPanelForEdition(entity);
		Component dataPanel = buildEditionComponent(entity);
		Panel buttonPanel2 = buildButtonPanelForEdition(entity);
		layout.addComponents(buttonPanel, dataPanel, buttonPanel2);
		return layout;
	}

	@Override
	protected Panel buildGridPanel() {
		VerticalLayout layout = ViewHelper.buildVerticalLayout(true, true);
		grid = ViewHelper.buildGrid(SelectionMode.SINGLE);
		grid.addColumn(documentDetail -> {
			if (documentDetail.getDocument() != null) {
				return documentDetail.getDocument().getDocumentDate();
			} else {
				return "";
			}
		}).setCaption("Fecha");
		grid.addColumn(documentDetail -> {
			if (documentDetail.getProduct() != null) {
				return documentDetail.getProduct().getCode();
			} else {
				return "";
			}
		}).setCaption("Código producto");
		grid.addColumn(document -> {
			if (document.getProduct() != null) {
				return document.getProduct().getName();
			} else {
				return "";
			}
		}).setCaption("Nombre producto");
		quantityColumn = grid.addColumn(DocumentDetail::getQuantity).setCaption("Cantidad");
		subtotalColumn = grid.addColumn(DocumentDetail::getSubtotal).setCaption("Subtotal");
		footer = grid.prependFooterRow();
		footer.getCell(quantityColumn).setHtml("<b>Total:</b>");

		layout.addComponent(ViewHelper.buildPanel(null, grid));
		fillGridData();

		return ViewHelper.buildPanel(null, layout);
	}

	@Override
	protected Component buildEditionComponent(DocumentDetail person) {
		VerticalLayout layout = ViewHelper.buildVerticalLayout(false, false);

		return layout;
	}

	protected Panel buildButtonPanelListMode() {
		HorizontalLayout layout = ViewHelper.buildHorizontalLayout(true, true);
		// Button btNew = buildButtonForNewAction(ValoTheme.BUTTON_TINY);
		Button btEdit = buildButtonForEditAction("mystyle-btn");
		// Button btDelete = buildButtonForDeleteAction(ValoTheme.BUTTON_TINY);
		layout.addComponents(btEdit);
		return ViewHelper.buildPanel(null, layout);
	}

	protected Panel buildButtonPanelForLists() {
		HorizontalLayout layout = ViewHelper.buildHorizontalLayout(true, true);
		Button btEdit = buildButtonForEditAction("mystyle-btn");

		btEdit.setCaption("Detalle");
		// Button btDelete = buildButtonForDeleteAction("mystyle-btn");
		layout.addComponents(btEdit);
		return ViewHelper.buildPanel(null, layout);
	}

	@Override
	protected void fillGridData() {

		dataProvider = new ListDataProvider<>(documentDetailBll.selectAll());

		grid.setDataProvider(dataProvider);
		dataProvider
				.addDataProviderListener(event -> footer.getCell(subtotalColumn).setHtml(calculateTotal(dataProvider)));
	}

	private String calculateTotal(ListDataProvider<DocumentDetail> detailDataProv) {
		log.info("Calculando total");
		String total = String
				.valueOf(detailDataProv.fetch(new Query<>()).mapToDouble(DocumentDetail::getSubtotal).sum());
		String quantity = String.valueOf(detailDataProv.size(new Query<>()));
		txtQuantity.setValue(quantity);
		txtTotal.setValue(total);
		return "<b>" + total + "</b>";

	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	protected void saveButtonAction(DocumentDetail documentDetail) {

	}

	@Override
	public DocumentDetail getSelected() {
		DocumentDetail documentDetail = null;
		Set<DocumentDetail> details = grid.getSelectedItems();
		if (details != null && !details.isEmpty()) {
			documentDetail = (DocumentDetail) details.toArray()[0];
		}
		return documentDetail;
	}

	@Override
	protected void delete(DocumentDetail entity) {
		entity = DocumentDetail.builder(entity).archived(true).build();
		save(documentDetailBll, entity, "Item borrado");
	}

	private Panel buildFilterPanel() {
		HorizontalLayout layout = ViewHelper.buildHorizontalLayout(true, true);

		txtFilterByProductCode = new TextField("Código del producto");
		txtFilterByProductCode.addValueChangeListener(e -> refreshGrid());
		txtFilterByProductCode.setStyleName(ValoTheme.TEXTFIELD_TINY);

		cbFilterByCategory = new ComboBox<>("Categoría del producto");
		cbFilterByCategory.setStyleName(ValoTheme.TEXTFIELD_TINY);
		ListDataProvider<ProductCategory> docTypeDataProv = new ListDataProvider<>(productCategoryBll.selectAll());
		cbFilterByCategory.setDataProvider(docTypeDataProv);
		cbFilterByCategory.setItemCaptionGenerator(ProductCategory::getName);
		cbFilterByCategory.addValueChangeListener(e -> refreshGrid());

		dtfFilterIniDate = new DateTimeField("Fecha inicial");
		dtfFilterIniDate.setResolution(DateTimeResolution.SECOND);
		dtfFilterIniDate.setValue(DateUtil.getDefaultIniDate());
		dtfFilterIniDate.setDateFormat(Commons.FORMAT_DATE_TIME);
		dtfFilterIniDate.setStyleName(ValoTheme.DATEFIELD_TINY);
		dtfFilterIniDate.setRequiredIndicatorVisible(true);
		dtfFilterIniDate.addValueChangeListener(e -> refreshGrid());

		dtfFilterEndDate = new DateTimeField("Fecha final");
		dtfFilterEndDate.setResolution(DateTimeResolution.SECOND); //
		dtfFilterEndDate.setValue(DateUtil.getDefaultEndDate());
		dtfFilterEndDate.setDateFormat(Commons.FORMAT_DATE_TIME);
		dtfFilterEndDate.setStyleName(ValoTheme.DATEFIELD_TINY);
		dtfFilterEndDate.setRequiredIndicatorVisible(true);
		dtfFilterEndDate.addValueChangeListener(e -> refreshGrid());

		layout.addComponents(txtFilterByProductCode, dtfFilterIniDate, dtfFilterEndDate);

		return ViewHelper.buildPanel("Buscar por", layout);
	}

	

	private Panel builTotalPanel() {
		HorizontalLayout layout = ViewHelper.buildHorizontalLayout(true, true);

		txtQuantity = new TextField("Cantidad:");
		txtQuantity.setReadOnly(true);
		txtQuantity.setStyleName(ValoTheme.TEXTFIELD_TINY);

		txtTotal = new TextField("Total:");
		txtTotal.setReadOnly(true);
		txtTotal.setStyleName(ValoTheme.TEXTFIELD_TINY);

		layout.addComponents(txtQuantity, txtTotal);
		return ViewHelper.buildPanel(null, layout);
	}

	private void refreshGrid() {
		dataProvider.setFilter(documentDetail -> filterGrid2(documentDetail));
	}

	private SerializablePredicate<DocumentDetail> filterGrid() {
		SerializablePredicate<DocumentDetail> columnPredicate = null;

		try {

			String productCodeFilter = txtFilterByProductCode.getValue().trim();

			String categoryFilter = cbFilterByCategory.getSelectedItem().isPresent()
					? cbFilterByCategory.getSelectedItem().get().getName()
					: "";
			Date iniDateFilter = dtfFilterIniDate.getValue() != null
					? DateUtil.localDateTimeToDate(dtfFilterIniDate.getValue())
					: DateUtil.stringToDate("01-01-2000 00:00:00");

			Date endDateFilter = dtfFilterEndDate.getValue() != null
					? DateUtil.localDateTimeToDate(dtfFilterEndDate.getValue())
					: new Date();

			if (endDateFilter.before(iniDateFilter)) {
				throw new Exception("La fecha final debe ser mayor que la inicial");
			} else {

			}

			columnPredicate = documentDetail -> (productCodeFilter != null
					? documentDetail.getProduct().getCode().equals(productCodeFilter)
					: true && documentDetail.getDocument().getDocumentDate().before(endDateFilter)
							&& documentDetail.getDocument().getDocumentDate().after(iniDateFilter));
		} catch (Exception e) {
			ViewHelper.showNotification(e.getMessage(), Notification.Type.ERROR_MESSAGE);
		}
		return columnPredicate;

	}

	private boolean filterGrid2(DocumentDetail documentDetail) {
		SerializablePredicate<Document> columnPredicate = null;
		boolean result = false;
		try {

			String productCodeFilter = txtFilterByProductCode.getValue().trim();

			String categoryFilter = cbFilterByCategory.getSelectedItem().isPresent()
					? cbFilterByCategory.getSelectedItem().get().getName()
					: "";
			Date iniDateFilter = dtfFilterIniDate.getValue() != null
					? DateUtil.localDateTimeToDate(dtfFilterIniDate.getValue())
					: DateUtil.stringToDate("01-01-2000 00:00:00");

			Date endDateFilter = dtfFilterEndDate.getValue() != null
					? DateUtil.localDateTimeToDate(dtfFilterEndDate.getValue())
					: new Date();

			if (endDateFilter.before(iniDateFilter)) {
				throw new Exception("La fecha final debe ser mayor que la inicial");
			} else {

			}

			result = productCodeFilter != null ? documentDetail.getProduct().getCode().equals(productCodeFilter)
					: true && documentDetail.getDocument().getDocumentDate().before(endDateFilter)
							&& documentDetail.getDocument().getDocumentDate().after(iniDateFilter);
		} catch (Exception e) {
			ViewHelper.showNotification(e.getMessage(), Notification.Type.ERROR_MESSAGE);
		}
		return result;

	}

}
