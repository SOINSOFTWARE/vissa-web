// Soin Software 2018
package com.soinsoftware.vissa.web;

import org.apache.log4j.Logger;
import org.hibernate.HibernateException;
import org.vaadin.dialogs.ConfirmDialog;

import com.soinsoftware.vissa.bll.AbstractBll;
import com.soinsoftware.vissa.exception.ModelValidationException;
import com.soinsoftware.vissa.model.User;
import com.soinsoftware.vissa.util.PermissionUtil;
import com.soinsoftware.vissa.util.ViewHelper;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.server.FontAwesome;
import com.vaadin.server.Page;
import com.vaadin.server.Resource;
import com.vaadin.ui.AbstractOrderedLayout;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Panel;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.TabSheet.Tab;
import com.vaadin.ui.VerticalLayout;

@SuppressWarnings("deprecation")
public abstract class AbstractEditableLayout<E> extends VerticalLayout implements View {

	//
	private static final long serialVersionUID = -7958396636831213220L;
	protected static final Logger log = Logger.getLogger(AbstractEditableLayout.class);

	private final String menuName;
	private TabSheet tabSheet;
	protected String pageTitle;
	private PermissionUtil permissionUtil;
	protected boolean hasError = false;

	public AbstractEditableLayout(String pageTitle, String menuName) {
		super();
		this.pageTitle = pageTitle;
		this.menuName = menuName;
	}

	@Override
	public void enter(ViewChangeEvent event) {
		View.super.enter(event);
		this.permissionUtil = new PermissionUtil(getSession().getAttribute(User.class).getRole().getPermissions());
		setMargin(true);
		addPageTitle(pageTitle);
		addListTab();
	}

	protected void addPageTitle(String title) {
		Label h1 = new Label(title);
		h1.addStyleName("h1");
		addComponent(h1);
	}

	protected void addListTab() {
		AbstractOrderedLayout layout = buildListView();
		tabSheet = new TabSheet();
		tabSheet.addStyleName("framed");
		Tab tab = tabSheet.addTab(layout, "Listado");
		tab.setIcon(FontAwesome.LIST);
		addComponent(tabSheet);
	}

	protected Panel buildButtonPanelForLists() {
		HorizontalLayout layout = ViewHelper.buildHorizontalLayout(true, true);
		if (permissionUtil.canEdit(menuName)) {
			Button btNew = buildButtonForNewAction("mystyle-btn");
			Button btEdit = buildButtonForEditAction("mystyle-btn");
			layout.addComponents(btNew, btEdit);
		}
		if (permissionUtil.canDelete(menuName)) {
			Button btDelete = buildButtonForDeleteAction("mystyle-btn");
			layout.addComponent(btDelete);
		}
		return ViewHelper.buildPanel(null, layout);
	}

	protected Panel buildButtonPanelForEdition(E entity) {
		HorizontalLayout layout = ViewHelper.buildHorizontalLayout(true, true);
		Button btCancel = buildButtonForCancelAction("mystyle-btn");
		Button btSave = buildButtonForSaveAction(entity, "mystyle-btn");
		layout.addComponents(btCancel, btSave);
		return ViewHelper.buildPanel(null, layout);
	}

	protected Button buildButtonForNewAction(String style) {
		Button button = ViewHelper.buildButton("Nuevo", FontAwesome.PLUS, style);
		button.addClickListener(e -> newButtonAction());
		return button;
	}

	protected Button buildButtonForEditAction(String style) {
		Button button = ViewHelper.buildButton("Editar", FontAwesome.EDIT, style);
		button.addClickListener(e -> editButtonAction());
		return button;
	}

	protected Button buildButtonForDeleteAction(String style) {
		Button button = ViewHelper.buildButton("Borrar", FontAwesome.ERASER, style);
		button.addClickListener(e -> deleteButtonAction());
		return button;
	}

	protected Button buildButtonForCancelAction(String style) {
		Button button = ViewHelper.buildButton("Cancelar", FontAwesome.CLOSE, style);
		button.addClickListener(e -> cancelButtonAction());
		return button;
	}

	protected Button buildButtonForSaveAction(E entity, String style) {
		Button button = ViewHelper.buildButton("Guardar", FontAwesome.SAVE, style);
		button.addClickListener(e -> saveButtonAction(entity));
		return button;
	}

	protected void newButtonAction() {
		showEditionTab(null, "Nueva", FontAwesome.PLUS);
	}

	protected void editButtonAction() {
		E entity = getSelected();
		if (entity != null) {
			showEditionTab(entity, "Editar", FontAwesome.EDIT);
		} else {
			ViewHelper.showNotification("No has seleccionado ningún registro", Notification.Type.WARNING_MESSAGE);
		}
	}

	protected void deleteButtonAction() {
		E entity = getSelected();
		if (entity != null) {

			showDeleteConfirmationDialog(entity);
		} else {
			ViewHelper.showNotification("No has seleccionado ningún registro", Notification.Type.WARNING_MESSAGE);
		}
	}

	protected void cancelButtonAction() {
		Tab tab = tabSheet.getTab(1);
		if (tab != null) {
			tabSheet.removeTab(tab);
		}
	}

	protected void showEditionTab(E entity, String caption, Resource icon) {
		AbstractOrderedLayout layout = buildEditionView(entity);
		addEditionTab(layout, caption, icon);
	}

	protected void addEditionTab(AbstractOrderedLayout layout, String caption, Resource icon) {
		cancelButtonAction();
		Tab tab = tabSheet.addTab(layout, caption);
		tab.setIcon(icon);
		tabSheet.setSelectedTab(1);
	}

	protected void save(AbstractBll<E, ?> bll, E entity, String caption) {
		hasError = false;
		try {
			bll.save(entity);
			afterSave(caption);
		} catch (ModelValidationException ex) {
			hasError = true;
			log.error(ex);
			bll.rollback();
			ViewHelper.showNotification(ex.getMessage(), Notification.Type.ERROR_MESSAGE);
		} catch (HibernateException ex) {
			hasError = true;
			log.error(ex);
			bll.rollback();
			ViewHelper.showNotification("Los datos no pudieron ser salvados, contacte al administrador del sistema",
					Notification.Type.ERROR_MESSAGE);
		} catch (Exception ex) {
			hasError = true;
			log.error(ex);
			bll.rollback();
			ViewHelper.showNotification("Los datos no pudieron ser salvados, contacte al administrador del sistema",
					Notification.Type.ERROR_MESSAGE);
		}
	}

	protected void afterSave(String caption) {
		fillGridData();
		cancelButtonAction();

		if (caption != null && caption.isEmpty()) {
			ViewHelper.showNotification(caption, Notification.Type.WARNING_MESSAGE);
		}
	}

	private void showDeleteConfirmationDialog(E entity) {
		ConfirmDialog.show(Page.getCurrent().getUI(), "Confirmar", "Está seguro que desea eliminar el registro?",
				"Aceptar", "Cancelar", e -> {
					if (e.isConfirmed()) {
						delete(entity);
					}
				});
	}

	protected abstract AbstractOrderedLayout buildListView();

	protected abstract AbstractOrderedLayout buildEditionView(E entity);

	protected abstract Panel buildGridPanel();

	protected abstract Component buildEditionComponent(E entity);

	protected abstract void fillGridData();

	protected abstract void saveButtonAction(E entity);

	protected abstract E getSelected();

	protected abstract void delete(E entity);
}