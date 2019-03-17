package com.soinsoftware.vissa.web;

import static com.soinsoftware.vissa.web.VissaUI.KEY_EGRESS;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang.time.DateUtils;
import org.apache.log4j.Logger;
import org.vaadin.ui.NumberField;

import com.soinsoftware.vissa.bll.EgressBll;
import com.soinsoftware.vissa.bll.EgressTypeBll;
import com.soinsoftware.vissa.model.Collection;
import com.soinsoftware.vissa.model.Document;
import com.soinsoftware.vissa.model.ERole;
import com.soinsoftware.vissa.model.Egress;
import com.soinsoftware.vissa.model.EgressType;
import com.soinsoftware.vissa.model.PaymentType;
import com.soinsoftware.vissa.model.Person;
import com.soinsoftware.vissa.model.Role;
import com.soinsoftware.vissa.model.User;
import com.soinsoftware.vissa.util.Commons;
import com.soinsoftware.vissa.util.DateUtil;
import com.soinsoftware.vissa.util.NumericUtil;
import com.soinsoftware.vissa.util.ViewHelper;
import com.vaadin.data.provider.ConfigurableFilterDataProvider;
import com.vaadin.data.provider.ListDataProvider;
import com.vaadin.server.SerializablePredicate;
import com.vaadin.ui.AbstractOrderedLayout;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.DateTimeField;
import com.vaadin.ui.FormLayout;
import com.vaadin.ui.Grid;
import com.vaadin.ui.Grid.SelectionMode;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Panel;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ValoTheme;

@SuppressWarnings("unchecked")
public class EgressLayout extends AbstractEditableLayout<Egress> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5076502522106126046L;

	protected static final Logger log = Logger.getLogger(EgressLayout.class);

	private final EgressBll egressBll;

	private final EgressTypeBll egresstypeBll;

	private Grid<Egress> egressGrid;

	private TextField txFilterByName;
	private TextField txFilterByCode;

	private TextField txtEgressName;
	private TextArea taEgressDescription;
	private TextField txtPerson;
	private DateTimeField dtfEgressDate;
	private NumberField txtEgressValue;
	private ComboBox<EgressType> cbEgressType;

	private Person selectedPerson = null;

	private User user;
	private Role role;

	private ListDataProvider<Egress> dataProvider;
	private ConfigurableFilterDataProvider<Egress, Void, SerializablePredicate<Egress>> filterProductDataProvider;

	public EgressLayout() throws IOException {
		super("Egresos", KEY_EGRESS);
		egressBll = EgressBll.getInstance();

		egresstypeBll = EgressTypeBll.getInstance();

	}

	@Override
	protected AbstractOrderedLayout buildListView() {

		this.user = getSession().getAttribute(User.class);
		this.role = user.getRole();
		selectedPerson = user.getPerson();

		VerticalLayout layout = ViewHelper.buildVerticalLayout(false, false);
		Panel buttonPanel = buildButtonPanelForLists();
		Panel filterPanel = buildFilterPanel();
		Panel dataPanel = buildGridPanel();
		layout.addComponents(buttonPanel, dataPanel);
		this.setMargin(false);
		this.setSpacing(false);
		return layout;
	}

	@Override
	protected AbstractOrderedLayout buildEditionView(Egress entity) {
		VerticalLayout layout = ViewHelper.buildVerticalLayout(false, false);
		Panel buttonPanel = buildButtonPanelForEdition(entity);
		Component dataPanel = buildEditionComponent(entity);
		layout.addComponents(buttonPanel, dataPanel);
		return layout;
	}

	@Override
	protected Panel buildGridPanel() {
		VerticalLayout layout = ViewHelper.buildVerticalLayout(true, true);
		egressGrid = ViewHelper.buildGrid(SelectionMode.SINGLE);

		egressGrid.addColumn(egress -> {
			if (egress.getEgressDate() != null) {
				return DateUtil.dateToString(egress.getEgressDate());
			} else {
				return "";
			}
		}).setCaption("Fecha");

		egressGrid.addColumn(egress -> {
			if (egress.getPerson() != null) {
				return egress.getPerson().getName() + " " + egress.getPerson().getLastName();
			} else {
				return "";
			}
		}).setCaption("Persona");
		egressGrid.addColumn(Egress::getName).setCaption("Nombre");
		egressGrid.addColumn(Egress::getValue).setCaption("Valor");

		egressGrid.addColumn(egress -> {
			if (egress.getType() != null) {
				return egress.getType().getName();
			} else {
				return "";
			}
		}).setCaption("Tipo");

		layout.addComponent(ViewHelper.buildPanel(null, egressGrid));
		fillGridData();
		return ViewHelper.buildPanel(null, layout);
	}

	@Override
	protected Component buildEditionComponent(Egress collection) {

		VerticalLayout layout = ViewHelper.buildVerticalLayout(false, false);

		dtfEgressDate = new DateTimeField("Fecha");
		dtfEgressDate.setStyleName(ValoTheme.DATEFIELD_TINY);
		dtfEgressDate.setDateFormat(Commons.FORMAT_DATE_TIME);
		dtfEgressDate.setRequiredIndicatorVisible(true);
		dtfEgressDate.setValue(LocalDateTime.now());
		dtfEgressDate.setWidth("50%");
		dtfEgressDate.setRequiredIndicatorVisible(true);

		cbEgressType = new ComboBox<>("Tipo de egreso");
		cbEgressType.setEmptySelectionCaption("Seleccione");
		cbEgressType.setStyleName(ValoTheme.COMBOBOX_TINY);
		cbEgressType.setRequiredIndicatorVisible(true);
		ListDataProvider<EgressType> egressTypeDataProv = new ListDataProvider<>(egresstypeBll.selectAll());
		cbEgressType.setDataProvider(egressTypeDataProv);
		cbEgressType.setItemCaptionGenerator(EgressType::getName);

		txtEgressName = new TextField("Nombre");
		txtEgressName.setStyleName(ValoTheme.TEXTFIELD_TINY);
		txtEgressName.setRequiredIndicatorVisible(true);
		txtEgressName.setWidth("50%");

		taEgressDescription = new TextArea("Descripción");
		taEgressDescription.setStyleName(ValoTheme.TEXTAREA_TINY);
		taEgressDescription.setWidth("50%");

		txtPerson = new TextField("Persona");
		txtPerson.setStyleName(ValoTheme.TEXTFIELD_TINY);
		txtPerson.setReadOnly(true);
		txtPerson.setWidth("50%");

		txtEgressValue = new NumberField("Valor egreso");
		txtEgressValue.setStyleName(ValoTheme.TEXTFIELD_TINY);
		txtEgressValue.setRequiredIndicatorVisible(true);
		txtEgressValue.setWidth("50%");

		FormLayout egressForm = ViewHelper.buildForm("", false, false);
		egressForm.addComponents(txtPerson, dtfEgressDate, cbEgressType, txtEgressName, taEgressDescription,
				txtEgressValue);
		Panel salePanel = ViewHelper.buildPanel("Egreso", egressForm);
		layout.addComponents(salePanel);

		// -------------------------------------------------------------------------

		setFieldValues(collection);
		// ----------------------------------------------------------------------------------

		return layout;
	}

	private void setFieldValues(Egress egress) {

		if (egress != null) {
			// selectedegress = egress.getDocument();
			txtEgressName.setValue(egress.getName());
			taEgressDescription.setValue(egress.getDescription());
			dtfEgressDate.setValue(DateUtil.dateToLocalDateTime(egress.getEgressDate()));
			txtPerson.setValue(egress.getPerson().getName() + " " + egress.getPerson().getLastName());
			cbEgressType.setValue(egress.getType());
			txtEgressValue.setValue(String.valueOf(egress.getValue()));
		} else {// Valores por defecto
			txtPerson.setValue(selectedPerson.getName() + " " + selectedPerson.getLastName());
		}

	}

	@Override
	protected void fillGridData() {
		List<Egress> egressList = null;
		if (role.getName().equals(ERole.SUDO.getName()) || role.getName().equals(ERole.MANAGER.getName())) {
			egressList = egressBll.selectAll();
		} else {
			egressList = egressBll.select(selectedPerson);
		}
		
		egressList = egressList.stream().sorted(Comparator.comparing(Egress::getEgressDate).reversed())
				.collect(Collectors.toList());
		
		dataProvider = new ListDataProvider<>(egressList);
		filterProductDataProvider = dataProvider.withConfigurableFilter();
		egressGrid.setDataProvider(filterProductDataProvider);
	}

	@Override
	protected void saveButtonAction(Egress entity) {
		String message = validateRequiredFields();
		if (!message.isEmpty()) {
			ViewHelper.showNotification(message, Notification.Type.ERROR_MESSAGE);
		} else {
			// Guardar egreso (gasto)
			saveEgress(entity);
			// Actualizar conciliación (cuadre de caja) por día y empleado
			saveConciliation();
		}

	}

	private void saveEgress(Egress entity) {
		String strLog = "[saveEgress]";
		try {
			Egress.Builder egressBuilder = null;
			if (entity == null) {
				egressBuilder = Egress.builder();
			} else {
				egressBuilder = Egress.builder(entity);
			}

			Date egressDate = DateUtil.localDateTimeToDate(dtfEgressDate.getValue());

			entity = egressBuilder.egressDate(egressDate).type(cbEgressType.getSelectedItem().get())
					.name(txtEgressName.getValue()).description(taEgressDescription.getValue())
					.value(NumericUtil.stringToBigDecimal(txtEgressValue.getValue())).person(selectedPerson)
					.archived(false).build();

			save(egressBll, entity, "Egreso guardado");
		} catch (Exception e) {
			log.error(strLog + "[Exception]" + e.getMessage());
			e.printStackTrace();
			ViewHelper.showNotification("Se generó un error al guardar el egreso", Notification.Type.ERROR_MESSAGE);
		}

	}

	private String validateRequiredFields() {
		String message = "";
		String character = "|";

		if (txtEgressName.getValue() == null || txtEgressName.getValue().isEmpty()) {
			if (!message.isEmpty()) {
				message = message.concat(character);
			}
			message = message.concat("La factura es obligatoria");
		}
		if (dtfEgressDate.getValue() == null) {
			if (!message.isEmpty()) {
				message = message.concat(character);
			}
			message = message.concat("La fecha  esobligatoria");
		}

		if (selectedPerson == null) {
			if (!message.isEmpty()) {
				message = message.concat(character);
			}
			message = message.concat("La persona es obligatoria");
		}

		if (txtEgressValue.getValue() == null) {
			if (!message.isEmpty()) {
				message = message.concat(character);
			}
			message = message.concat("El valor del gasto es obligatorio");
		}

		return message;

	}

	@Override
	public Egress getSelected() {
		Egress egressObj = null;
		Set<Egress> egressSet = egressGrid.getSelectedItems();
		if (egressSet != null && !egressSet.isEmpty()) {
			egressObj = (Egress) egressSet.toArray()[0];
		}
		return egressObj;
	}

	@Override
	protected void delete(Egress entity) {

	}

	private Panel buildFilterPanel() {
		HorizontalLayout layout = ViewHelper.buildHorizontalLayout(true, true);
		txFilterByName = new TextField("Nombre");
		txFilterByName.addValueChangeListener(e -> refreshGrid());
		txFilterByCode = new TextField("Código");
		txFilterByCode.addValueChangeListener(e -> refreshGrid());
		layout.addComponents(txFilterByCode, txFilterByName);
		return ViewHelper.buildPanel("Filtrar por", layout);
	}

	private void refreshGrid() {
		filterProductDataProvider.setFilter(filterGrid());
		egressGrid.getDataProvider().refreshAll();
	}

	private SerializablePredicate<Egress> filterGrid() {
		SerializablePredicate<Egress> columnPredicate = null;
		// String codeFilter = txFilterByCode.getValue().trim();
		// String nameFilter = txFilterByName.getValue().trim();
		/*
		 * columnPredicate = warehouse ->
		 * (warehouse.getName().toLowerCase().contains(nameFilter.toLowerCase()) &&
		 * warehouse.getCode().toLowerCase().contains(codeFilter.toLowerCase()));
		 **********/
		return columnPredicate;
	}

	private boolean filterDocumentByDate(Document document, PaymentType paymentType) {
		String strLog = "[filterDocumentByDate]";
		boolean result = false;
		try {
			Date iniDateFilter = DateUtil.localDateTimeToDate(DateUtil.getDefaultIniDate());
			Date endDateFilter = DateUtil.localDateTimeToDate(DateUtil.getDefaultEndDateTime());

			log.info(strLog + " iniDateFilter: " + iniDateFilter + ", endDateFilter:" + endDateFilter);

			result = document.getDocumentDate().before(endDateFilter)
					&& document.getDocumentDate().after(iniDateFilter);

			if (paymentType != null) {
				result = result && document.getPaymentType().equals(paymentType);
			}
		} catch (Exception e) {
			log.error(strLog + e.getMessage());
		}

		log.info(strLog + " result filterDocumentByDate: " + result);
		return result;
	}

	/*
	 * 
	 * Actualizar conciliación (cuadre de caja) por día y empleado
	 */
	private void saveConciliation() {
		String strLog = "[saveConciliation] ";
		try {
			Date conciliationDate = DateUtil.localDateTimeToDate(dtfEgressDate.getValue());
			conciliationDate = DateUtils.truncate(conciliationDate, Calendar.DATE);
			new CashConciliationLayout().saveDailyConciliation(user, conciliationDate);
		} catch (IOException e) {
			log.error(strLog + "Error al actualizar conciliación: " + e.getMessage());
			e.printStackTrace();
		}
	}

}
