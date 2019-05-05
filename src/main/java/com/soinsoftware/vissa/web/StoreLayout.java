package com.soinsoftware.vissa.web;

import static com.soinsoftware.vissa.web.VissaUI.KEY_EGRESS_TYPE;

import java.io.IOException;
import java.util.Set;

import org.apache.log4j.Logger;
import org.jsoup.helper.StringUtil;

import com.soinsoftware.vissa.bll.StoreBll;
import com.soinsoftware.vissa.model.Store;
import com.soinsoftware.vissa.util.ViewHelper;
import com.vaadin.data.provider.ListDataProvider;
import com.vaadin.server.FontAwesome;
import com.vaadin.ui.AbstractOrderedLayout;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.FormLayout;
import com.vaadin.ui.Grid;
import com.vaadin.ui.Grid.SelectionMode;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Panel;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ValoTheme;

@SuppressWarnings({ "unchecked", "deprecation" })
public class StoreLayout extends AbstractEditableLayout<Store> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5076502522106926046L;

	protected static final Logger log = Logger.getLogger(StoreLayout.class);

	// Blls
	private final StoreBll storeBll;

	// Components
	private Grid<Store> grid;
	private TextField txFilterByName;
	private TextField txFilterByCode;
	private TextField txtCode;
	private TextField txtName;
	private TextField txtAddress;
	private TextField txtPhone;
	private TextField txtMobile;
	private TextField txtEmail;

	private ListDataProvider<Store> dataProvider;

	public StoreLayout() throws IOException {
		super("Puntos de venta", KEY_EGRESS_TYPE);
		storeBll = StoreBll.getInstance();
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
	protected AbstractOrderedLayout buildEditionView(Store entity) {
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
		grid.addColumn(Store::getCode).setCaption("Código");
		grid.addColumn(Store::getName).setCaption("Nombre");
		grid.addColumn(Store::getAddress).setCaption("Dirección");
		grid.addColumn(Store::getMobile).setCaption("Celular");

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
	protected Component buildEditionComponent(Store warehouse) {

		VerticalLayout layout = ViewHelper.buildVerticalLayout(true, true);

		Label tittle = new Label("Datos del punto de venta");
		tittle.setStyleName(ValoTheme.LABEL_H4);

		txtCode = new TextField("Código");
		txtCode.setStyleName(ValoTheme.TEXTAREA_TINY);
		txtCode.setRequiredIndicatorVisible(true);

		txtName = new TextField("Nombre");
		txtName.setStyleName(ValoTheme.TEXTAREA_TINY);
		txtName.setRequiredIndicatorVisible(true);

		txtAddress = new TextField("Dirección");
		txtAddress.setStyleName(ValoTheme.TEXTAREA_TINY);

		txtPhone = new TextField("Teléfono");
		txtPhone.setStyleName(ValoTheme.TEXTAREA_TINY);

		txtMobile = new TextField("Celular");
		txtMobile.setStyleName(ValoTheme.TEXTAREA_TINY);

		txtEmail = new TextField("Correo electrónico");
		txtEmail.setStyleName(ValoTheme.TEXTAREA_TINY);

		// ----------------------------------------------------------------------------------

		final FormLayout form = new FormLayout();
		form.setMargin(true);
		form.setCaptionAsHtml(true);
		form.setSizeFull();
		form.setWidth("50%");
		form.addStyleName(ValoTheme.FORMLAYOUT_LIGHT);

		form.addComponents(tittle, txtCode, txtName, txtAddress, txtPhone, txtMobile, txtEmail);

		layout.addComponents(form);

		// Establece valores para los campos
		setFieldValues(warehouse);

		return layout;
	}

	/**
	 * Metodo para establecer los valores en los componentes
	 * 
	 * @param entity
	 */
	private void setFieldValues(Store entity) {
		String strLog = "[setFieldValues] ";
		try {
			log.info(strLog + "[parameters] entity: " + entity);

			if (entity != null) {
				txtCode.setValue(entity.getCode());
				txtName.setValue(entity.getName());
				txtAddress.setValue(entity.getAddress());
				txtPhone.setValue(entity.getPhone());
				txtMobile.setValue(entity.getMobile());
				txtEmail.setValue(entity.getEmail());
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
			dataProvider = new ListDataProvider<>(storeBll.selectAll(false));
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
	protected void saveButtonAction(Store entity) {
		String strLog = "[saveButtonAction] ";
		try {
			log.info(strLog + "[parameters] entity: " + entity);

			String message = validateRequiredFields();
			if (!message.isEmpty()) {
				ViewHelper.showNotification(message, Notification.Type.WARNING_MESSAGE);
			} else {
				saveStore(entity);
			}
		} catch (Exception e) {
			log.error(strLog + "[Exception] " + e.getMessage());
			e.printStackTrace();
		}
	}

	/**
	 * Metodo para guardar el punto de venta
	 * 
	 * @param entity
	 */
	private void saveStore(Store entity) {
		String strLog = "[saveStore] ";
		try {

			log.info(strLog + "[parameters] entity: " + entity);

			Store.Builder storeBuilder = null;
			if (entity == null) {
				storeBuilder = Store.builder();
			} else {
				storeBuilder = Store.builder(entity);
			}

			entity = storeBuilder.code(txtCode.getValue().toUpperCase()).name(txtName.getValue().toUpperCase())
					.address(txtAddress.getValue().toUpperCase()).phone(txtPhone.getValue().toUpperCase())
					.mobile(txtMobile.getValue().toUpperCase()).email(txtEmail.getValue().toUpperCase()).archived(false)
					.build();

			save(storeBll, entity, "Punto de venta guardado");

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
	public Store getSelected() {
		String strLog = "[getSelected] ";
		Store storeObj = null;
		try {
			Set<Store> stores = grid.getSelectedItems();
			if (stores != null && !stores.isEmpty()) {
				storeObj = (Store) stores.toArray()[0];
			}
		} catch (Exception e) {
			log.error(strLog + "" + e.getMessage());
			e.printStackTrace();
		}
		return storeObj;
	}

	@Override
	protected void delete(Store entity) {
		String strLog = "[delete] ";
		try {
			log.info(strLog + "[parameters] entity: " + entity);

			entity = Store.builder(entity).archived(true).build();
			save(storeBll, entity, "Punto de venta borrado");
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
	private boolean filterGrid(Store entity) {
		String strLog = "[filterGrid] ";
		boolean result = false;
		try {

			// Filtrar por el codigo del punto de venta
			String codeFilter = txFilterByCode.getValue();
			if (!org.jsoup.helper.StringUtil.isBlank(codeFilter)) {
				result = result && (entity.getCode().contains(codeFilter.toUpperCase()));
			}

			// Filtrar por el nombre del punto de venta
			String nameFilter = txFilterByName.getValue();
			if (!org.jsoup.helper.StringUtil.isBlank(nameFilter)) {
				result = result && (entity.getCode().contains(nameFilter.toUpperCase()));
			}
		} catch (Exception e) {
			log.error(strLog + "[Exception] " + e.getMessage());
			e.printStackTrace();
			ViewHelper.showNotification(e.getMessage(), Notification.Type.WARNING_MESSAGE);
		}
		return result;

	}

}
