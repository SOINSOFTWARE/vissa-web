package com.soinsoftware.vissa.web;

import static com.soinsoftware.vissa.web.VissaUI.KEY_EGRESS_TYPE;

import java.io.IOException;
import java.util.Set;

import org.apache.log4j.Logger;
import org.jsoup.helper.StringUtil;

import com.soinsoftware.vissa.bll.EgressTypeBll;
import com.soinsoftware.vissa.model.EgressType;
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
import com.vaadin.ui.Notification;
import com.vaadin.ui.Panel;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ValoTheme;

@SuppressWarnings({ "unchecked", "deprecation" })
public class EgressTypeLayout extends AbstractEditableLayout<EgressType> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5076502522106926046L;

	protected static final Logger log = Logger.getLogger(EgressTypeLayout.class);

	// Blls
	private final EgressTypeBll egressTypeBll;

	// Components
	private Grid<EgressType> grid;
	private TextField txFilterByName;
	private TextField txFilterByCode;
	private TextField txtCode;
	private TextField txtName;

	private ListDataProvider<EgressType> dataProvider;

	public EgressTypeLayout() throws IOException {
		super("Conceptos de egresos", KEY_EGRESS_TYPE);
		egressTypeBll = EgressTypeBll.getInstance();
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
	protected AbstractOrderedLayout buildEditionView(EgressType entity) {
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
		grid.addColumn(EgressType::getCode).setCaption("Código");
		grid.addColumn(EgressType::getName).setCaption("Nombre");

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
	protected Component buildEditionComponent(EgressType entity) {

		VerticalLayout layout = ViewHelper.buildVerticalLayout(true, true);

		txtCode = new TextField("Código de egreso");
		txtCode.setStyleName(ValoTheme.TEXTAREA_TINY);
		txtCode.setRequiredIndicatorVisible(true);

		txtName = new TextField("Nombre de egreso");
		txtName.setStyleName(ValoTheme.TEXTAREA_TINY);
		txtName.setRequiredIndicatorVisible(true);

		// ----------------------------------------------------------------------------------

		final FormLayout form = new FormLayout();
		form.setMargin(true);
		form.setCaption("Datos del tipo de egreso");
		form.setCaptionAsHtml(true);
		form.setSizeFull();
		form.setWidth("50%");
		form.addStyleName(ValoTheme.FORMLAYOUT_LIGHT);

		form.addComponents(txtCode, txtName);

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
	private void setFieldValues(EgressType entity) {
		String strLog = "[setFieldValues] ";
		try {
			log.info(strLog + "[parameters] entity: " + entity);

			if (entity != null) {
				txtCode.setValue(entity.getCode());
				txtName.setValue(entity.getName());
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
	protected void saveButtonAction(EgressType entity) {
		String strLog = "[saveButtonAction] ";
		try {
			log.info(strLog + "[parameters] entity: " + entity);

			String message = validateRequiredFields();
			if (!message.isEmpty()) {
				ViewHelper.showNotification(message, Notification.Type.WARNING_MESSAGE);
			} else {
				saveEgressType(entity);
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
	private void saveEgressType(EgressType entity) {
		String strLog = "[saveEgressType] ";
		try {

			log.info(strLog + "[parameters] entity: " + entity);

			EgressType.Builder builder = null;
			if (entity == null) {
				builder = EgressType.builder();
			} else {
				builder = EgressType.builder(entity);
			}

			entity = builder.code(txtCode.getValue().toUpperCase()).name(txtName.getValue().toUpperCase())
					.archived(false).build();

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
	public EgressType getSelected() {
		String strLog = "[getSelected] ";
		EgressType egressTypeObj = null;
		try {
			Set<EgressType> egressTypes = grid.getSelectedItems();
			if (egressTypes != null && !egressTypes.isEmpty()) {
				egressTypeObj = (EgressType) egressTypes.toArray()[0];
			}
		} catch (Exception e) {
			log.error(strLog + "" + e.getMessage());
			e.printStackTrace();
		}
		return egressTypeObj;
	}

	@Override
	protected void delete(EgressType entity) {
		String strLog = "[delete] ";
		try {
			log.info(strLog + "[parameters] entity: " + entity);

			entity = EgressType.builder(entity).archived(true).build();
			save(egressTypeBll, entity, "Tipo de egreso borrado");
		} catch (Exception e) {
			log.error(strLog + "[Exception] " + e.getMessage());
			e.printStackTrace();
		}
	}

	private Panel buildFilterPanel() {
		HorizontalLayout layout = ViewHelper.buildHorizontalLayout(true, true);
		txFilterByName = new TextField("Nombre");
		txFilterByName.setStyleName(ValoTheme.TEXTFIELD_TINY);
		txFilterByName.addValueChangeListener(e -> refreshGrid());

		txFilterByCode = new TextField("Código");
		txFilterByCode.setStyleName(ValoTheme.TEXTFIELD_TINY);
		txFilterByCode.addValueChangeListener(e -> refreshGrid());
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
	private boolean filterGrid(EgressType entity) {
		String strLog = "[filterGrid] ";
		boolean result = false;
		try {

			// Filtrar por el codigo del tipo de egreso
			String codeFilter = txFilterByCode.getValue();
			if (!org.jsoup.helper.StringUtil.isBlank(codeFilter)) {
				result = result && (entity.getCode().contains(codeFilter.toUpperCase()));
			}

			// Filtrar por el nombre del tipo de egreso
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
