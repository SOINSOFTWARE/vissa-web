package com.soinsoftware.vissa.web;

import java.io.File;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

import javax.servlet.annotation.WebServlet;

import org.apache.log4j.Logger;

import com.soinsoftware.vissa.bll.UserBll;
import com.soinsoftware.vissa.common.CommonsConstants;
import com.soinsoftware.vissa.model.ERole;
import com.soinsoftware.vissa.model.ETransactionType;
import com.soinsoftware.vissa.model.Person;
import com.soinsoftware.vissa.model.PersonType;
import com.soinsoftware.vissa.model.User;
import com.soinsoftware.vissa.util.Commons;
import com.soinsoftware.vissa.util.ELayoutMode;
import com.soinsoftware.vissa.util.PermissionUtil;
import com.soinsoftware.vissa.util.ViewHelper;
import com.vaadin.annotations.Theme;
import com.vaadin.annotations.VaadinServletConfiguration;
import com.vaadin.data.TreeData;
import com.vaadin.data.provider.TreeDataProvider;
import com.vaadin.event.ShortcutAction;
import com.vaadin.event.ShortcutListener;
import com.vaadin.navigator.Navigator;
import com.vaadin.server.FileResource;
import com.vaadin.server.FontAwesome;
import com.vaadin.server.Responsive;
import com.vaadin.server.VaadinRequest;
import com.vaadin.server.VaadinService;
import com.vaadin.server.VaadinServlet;
import com.vaadin.shared.ui.ContentMode;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.ComponentContainer;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.MenuBar;
import com.vaadin.ui.MenuBar.MenuItem;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Panel;
import com.vaadin.ui.PasswordField;
import com.vaadin.ui.TextField;
import com.vaadin.ui.Tree;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.themes.ValoTheme;

/**
 * This UI is the application entry point. A UI may either represent a browser
 * window (or tab) or some part of an HTML page where a Vaadin application is
 * embedded.
 * <p>
 * The UI is initialized using {@link #init(VaadinRequest)}. This method is
 * intended to be overridden to add component to the user interface and
 * initialize non-component functionality.
 */
@SuppressWarnings("deprecation")

@Theme("mytheme")
public class VissaUI extends UI {

	private static final long serialVersionUID = 7412593442523938389L;
	private static final Logger log = Logger.getLogger(VissaUI.class);
	protected static final String KEY_PRODUCTS = "Productos";
	protected static final String KEY_LOTS = "Lotes";
	protected static final String KEY_COMPANY_DATA = "companyData";
	protected static final String KEY_INVENTORY = "Inventario";
	protected static final String KEY_INVENTORY_MOV = "Movimientos";
	protected static final String KEY_PURCHASES = "Compras";
	protected static final String KEY_RETURNS = "Devoluciones";
	protected static final String KEY_WAREHOUSE = "Bodegas";
	protected static final String KEY_SALES = "Ventas";
	protected static final String KEY_SALES_REPORT = "Reporte de Ventas";
	protected static final String KEY_PURCHASES_REPORT = "Reporte de Compras";
	protected static final String KEY_PRODUCTS_REPORT = "Reporte de Productos";
	protected static final String KEY_SUPPLIER = "Proveedores";
	protected static final String KEY_CUSTOMER = "Clientes";
	protected static final String KEY_SALE_INVOICES = "Facturas de Venta";
	protected static final String KEY_PURCHASE_INVOICES = "Facturas de Compra";
	protected static final String KEY_SUPPLIER_LIST = "supplierList";
	protected static final String KEY_REPORTS = "Reportes";
	protected static final String KEY_PERSON = "Personas";
	protected static final String KEY_INVOICES = "Facturas";
	protected static final String KEY_ADMINISTRATION = "Administración";
	protected static final String KEY_USERS = "Usuarios";
	protected static final String KEY_ROLES = "Roles";
	protected static final String KEY_MENUS = "Menus";
	protected static final String KEY_CONCILIATION = "Conciliaciones";
	protected static final String KEY_SALESMAN_CONCILIATION = "Cuadre vendedor";
	protected static final String KEY_ADMIN_CONCILIATION = "Cuadre administrador";
	protected static final String KEY_COLLECTION = "Recaudos";
	protected static final String KEY_SUPPLIER_PAYMENTS = "Pago a proveedores";
	protected static final String KEY_EGRESS = "Egresos";
	protected static final String KEY_COMPANY = "Compañía";
	protected static final String KEY_MENU = "Menus";
	protected static final String KEY_NOTIFICATION = "Notificaciones";
	protected static final String KEY_MEASUREMENT_UNIT = "Unidades de medida";
	protected static final String KEY_PRODUCT_CATEGORY = "Dependencias";

	private PermissionUtil permissionUtil;

	TreeDataProvider<String> dataProvider;
	TreeData<String> treeData;
	Tree<String> tree;
	ValoMenuLayout root;
	Navigator navigator;

	@Override
	protected void init(VaadinRequest vaadinRequest) {
		getPage().setTitle("Vissa");
		addStyleName(ValoTheme.UI_WITH_MENU);
		Responsive.makeResponsive(this);
		buildUI(getUser());
		Commons.appWindow = this;
	}

	private void buildValoMenuLayout() {
		root = new ValoMenuLayout();
		CssLayout menu = buildMenu(root);
		if (getPage().getWebBrowser().isIE() && getPage().getWebBrowser().getBrowserMajorVersion() == 9) {
			menu.setWidth("320px");
		}
		root.addMenu(menu);
		root.setWidth("100%");
		Panel panel = new Panel();
		panel.setStyleName("well");
		try {
			root.addComponent(new NotificationLayout());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		panel.setContent(root);
		setContent(root);
		UI.getCurrent().getNavigator().navigateTo(KEY_NOTIFICATION);
	}

	private void buildMenuItems() {
		if (permissionUtil.canView(KEY_PURCHASES)) {
			treeData.addItem(null, KEY_PURCHASES);
			if (permissionUtil.canView(KEY_PURCHASE_INVOICES)) {
				treeData.addItem(KEY_PURCHASES, KEY_PURCHASE_INVOICES);
			}
			if (permissionUtil.canView(KEY_SUPPLIER)) {
				treeData.addItem(KEY_PURCHASES, KEY_SUPPLIER);
			}
		}
		if (permissionUtil.canView(KEY_SALES)) {
			treeData.addItem(null, KEY_SALES);
			if (permissionUtil.canView(KEY_SALE_INVOICES)) {
				treeData.addItem(KEY_SALES, KEY_SALE_INVOICES);
			}
			if (permissionUtil.canView(KEY_CUSTOMER)) {
				treeData.addItem(KEY_SALES, KEY_CUSTOMER);
			}
		}
		if (permissionUtil.canView(KEY_RETURNS)) {
			treeData.addItem(null, KEY_RETURNS);
		}

		if (permissionUtil.canView(KEY_INVENTORY)) {
			treeData.addItem(null, KEY_INVENTORY);
			if (permissionUtil.canView(KEY_PRODUCTS)) {
				treeData.addItem(KEY_INVENTORY, KEY_PRODUCTS);
			}
			if (permissionUtil.canView(KEY_LOTS)) {
				treeData.addItem(KEY_INVENTORY, KEY_LOTS);
			}
			if (permissionUtil.canView(KEY_WAREHOUSE)) {
				treeData.addItem(KEY_INVENTORY, KEY_WAREHOUSE);
			}
			if (permissionUtil.canView(KEY_INVENTORY_MOV)) {
				treeData.addItem(KEY_INVENTORY, KEY_INVENTORY_MOV);
			}
		}
		if (permissionUtil.canView(KEY_REPORTS)) {
			treeData.addItem(null, KEY_REPORTS);
			if (permissionUtil.canView(KEY_SALES_REPORT)) {
				treeData.addItem(KEY_REPORTS, KEY_SALES_REPORT);
			}
			if (permissionUtil.canView(KEY_PURCHASES_REPORT)) {
				treeData.addItem(KEY_REPORTS, KEY_PURCHASES_REPORT);
			}
			if (permissionUtil.canView(KEY_PRODUCTS_REPORT)) {
				treeData.addItem(KEY_REPORTS, KEY_PRODUCTS_REPORT);
			}

		}

		if (permissionUtil.canView(KEY_CONCILIATION)) {
			treeData.addItem(null, KEY_CONCILIATION);
			if (permissionUtil.canView(KEY_SALESMAN_CONCILIATION)) {
				treeData.addItem(KEY_CONCILIATION, KEY_SALESMAN_CONCILIATION);
			}
			if (permissionUtil.canView(KEY_ADMIN_CONCILIATION)) {
				treeData.addItem(KEY_CONCILIATION, KEY_ADMIN_CONCILIATION);
			}
		}

		if (permissionUtil.canView(KEY_COLLECTION)) {
			treeData.addItem(null, KEY_COLLECTION);
		}

		if (permissionUtil.canView(KEY_SUPPLIER_PAYMENTS)) {
			treeData.addItem(null, KEY_SUPPLIER_PAYMENTS);
		}

		if (permissionUtil.canView(KEY_EGRESS)) {
			treeData.addItem(null, KEY_EGRESS);
		}

		if (permissionUtil.canView(KEY_ADMINISTRATION)) {
			treeData.addItem(null, KEY_ADMINISTRATION);

			if (permissionUtil.canView(KEY_USERS)) {
				treeData.addItem(KEY_ADMINISTRATION, KEY_USERS);
			}
			if (permissionUtil.canView(KEY_ROLES)) {
				treeData.addItem(KEY_ADMINISTRATION, KEY_ROLES);
			}

			if (permissionUtil.canView(KEY_MENUS)) {
				treeData.addItem(KEY_ADMINISTRATION, KEY_MENUS);
			}

			if (permissionUtil.canView(KEY_COMPANY)) {
				treeData.addItem(KEY_ADMINISTRATION, KEY_COMPANY);
			}
			if (permissionUtil.canView(KEY_MEASUREMENT_UNIT)) {
				treeData.addItem(KEY_ADMINISTRATION, KEY_MEASUREMENT_UNIT);
			}
			if (permissionUtil.canView(KEY_PRODUCT_CATEGORY)) {
				treeData.addItem(KEY_ADMINISTRATION, KEY_PRODUCT_CATEGORY);
			}
		}

	}

	private CssLayout buildMenu(ValoMenuLayout root) {
		CssLayout menu = new CssLayout();

		Component menuItemsLayout = buildMenuItemsLayout(root);
		menu.addComponent(buildTopLayout());
		menu.addComponent(buildShowMenuButton(menu));
		
		menu.addComponent(buildMenuBar());		
		
		TextField txtSalePoint = new TextField();		
		txtSalePoint.setValue("PUNTO DE VENTA 1");
		txtSalePoint.setReadOnly(true);
		txtSalePoint.addStyleNames(ValoTheme.TEXTFIELD_BORDERLESS, ValoTheme.TEXTFIELD_TINY, ValoTheme.TEXTFIELD_ALIGN_CENTER);		
		menu.addComponent(txtSalePoint);
		
		menu.addComponent(menuItemsLayout);

		menu.setWidth("100%");
		menu.setSizeFull();

		// buildNavigator(root, menu, menuItemsLayout);
		return menu;
	}

	private HorizontalLayout buildTopLayout() {
		HorizontalLayout top = new HorizontalLayout();
		top.setWidth("100%");
		top.setDefaultComponentAlignment(Alignment.MIDDLE_LEFT);
		top.addStyleName(ValoTheme.MENU_TITLE);

		Label title = new Label("<h3><strong>Vissa</strong></h3>", ContentMode.HTML);
		title.setSizeUndefined();
		top.addComponent(title);
		top.setExpandRatio(title, 1);
		return top;
	}

	private Button buildShowMenuButton(CssLayout menu) {
		Button showMenu = new Button("Menu", e -> {
			if (menu.getStyleName().contains("valo-menu-visible")) {
				menu.removeStyleName("valo-menu-visible");
			} else {
				menu.addStyleName("valo-menu-visible");
			}
		});
		showMenu.addStyleName(ValoTheme.BUTTON_PRIMARY);
		showMenu.addStyleName(ValoTheme.BUTTON_SMALL);
		showMenu.addStyleName("valo-menu-toggle");
		showMenu.setIcon(FontAwesome.LIST);
		return showMenu;
	}

	private MenuBar buildMenuBar() {
		Person person = getUser().getPerson();
		MenuBar settings = new MenuBar();
		settings.addStyleName("user-menu");
		String basepath = VaadinService.getCurrent().getBaseDirectory().getAbsolutePath();
		FileResource resource = new FileResource(new File(basepath + "/WEB-INF/logoKisam.png"));
		MenuItem settingsItem = settings.addItem(person.getName() + " " + person.getLastName(), resource, null);
		settingsItem.addItem("Cerrar session", e -> buildUI(null));
		return settings;
	}

	private Component buildMenuItemsLayout(ValoMenuLayout root) {
		VerticalLayout layout = ViewHelper.buildVerticalLayout(false, false);

		tree = new Tree<>();
		treeData = new TreeData<>();

		buildMenuItems();

		buildNavigator();
		dataProvider = new TreeDataProvider<>(treeData);
		tree.setDataProvider(dataProvider);
		tree.setStyleName("valo-menuitems");
		tree.setWidth("100%");
		/*
		 * if (permissionUtil.canView(KEY_PURCHASES)) { tree.expand(KEY_PURCHASES); } if
		 * (permissionUtil.canView(KEY_SALES)) { tree.expand(KEY_SALES); } if
		 * (permissionUtil.canView(KEY_INVENTORY)) { tree.expand(KEY_INVENTORY); } if
		 * (permissionUtil.canView(KEY_REPORTS)) { tree.expand(KEY_REPORTS); } if
		 * (permissionUtil.canView(KEY_ADMINISTRATION)) {
		 * tree.expand(KEY_ADMINISTRATION); } if
		 * (permissionUtil.canView(KEY_CONCILIATION)) { tree.expand(KEY_CONCILIATION); }
		 */

		tree.addItemClickListener(e -> selectItem(e));
		layout.addComponent(tree);

		Panel treePanel = new Panel();
		treePanel.addStyleName("well");
		treePanel.setContent(tree);
		// setContent(treePanel);

		return tree;

	}

	private void selectItem(Tree.ItemClick<String> event) {
		String item = event.getItem();
		if (item.equals(KEY_SALE_INVOICES) || item.equals(KEY_SALES_REPORT)) {
			CommonsConstants.TRANSACTION_TYPE = ETransactionType.SALIDA.getName();
		}
		if (item.equals(KEY_PURCHASE_INVOICES) || item.equals(KEY_PURCHASES_REPORT)) {
			CommonsConstants.TRANSACTION_TYPE = ETransactionType.ENTRADA.getName();
		}
		if (item.equals(KEY_RETURNS)) {
			CommonsConstants.TRANSACTION_TYPE = ETransactionType.SALIDA.getName();
		}
		if (item.equals(KEY_SUPPLIER)) {
			Commons.PERSON_TYPE = PersonType.SUPPLIER.getName();
		}
		if (item.equals(KEY_CUSTOMER)) {
			Commons.PERSON_TYPE = PersonType.CUSTOMER.getName();
		}
		if (item.equals(KEY_USERS)) {
			Commons.PERSON_TYPE = PersonType.USER.getName();
		}
		if (item.equals(KEY_SALESMAN_CONCILIATION)) {
			Commons.ROLE = ERole.SALESMAN.getName();
		}

		if (item.equals(KEY_ADMIN_CONCILIATION)) {
			Commons.ROLE = ERole.ADMINISTRATOR.getName();
		}

		if (item.equals(KEY_LOTS)) {
			Commons.LAYOUT_MODE = ELayoutMode.REPORT;
		}

		if (item.equals(KEY_SUPPLIER_PAYMENTS)) {
			CommonsConstants.TRANSACTION_TYPE = ETransactionType.ENTRADA.getName();
		}

		if (item.equals(KEY_COLLECTION)) {
			CommonsConstants.TRANSACTION_TYPE = ETransactionType.SALIDA.getName();
		}

		if (item.equals(KEY_PRODUCTS)) {
			Commons.LAYOUT_MODE = ELayoutMode.ALL;
		}
		Commons.MENU_NAME = item;
		UI.getCurrent().getNavigator().navigateTo(item);
	}

	private void buildNavigator() {

		ComponentContainer viewContainer = root.getContentContainer();
		Navigator navigator = new Navigator(this, viewContainer);
		navigator.addView(KEY_SUPPLIER, PersonLayout.class);
		navigator.addView(KEY_PRODUCTS, ProductLayout.class);
		navigator.addView(KEY_SUPPLIER_LIST, SupplierListLayout.class);
		navigator.addView(KEY_PURCHASE_INVOICES, InvoiceLayout.class);
		navigator.addView(KEY_SALE_INVOICES, InvoiceLayout.class);
		navigator.addView(KEY_CUSTOMER, PersonLayout.class);
		navigator.addView(KEY_INVENTORY_MOV, InventoryLayout.class);
		navigator.addView(KEY_LOTS, LotLayout.class);
		navigator.addView(KEY_WAREHOUSE, WarehouseLayout.class);
		navigator.addView(KEY_SALES_REPORT, InvoiceReportLayout.class);
		navigator.addView(KEY_PURCHASES_REPORT, InvoiceReportLayout.class);
		navigator.addView(KEY_PRODUCTS_REPORT, ProductReportLayout.class);
		navigator.addView(KEY_USERS, PersonLayout.class);
		navigator.addView(KEY_ROLES, RoleLayout.class);
		navigator.addView(KEY_SALESMAN_CONCILIATION, CashConciliationLayout.class);
		navigator.addView(KEY_ADMIN_CONCILIATION, CashConciliationLayout.class);
		navigator.addView(KEY_COLLECTION, CollectionLayout.class);
		navigator.addView(KEY_SUPPLIER_PAYMENTS, CollectionLayout.class);
		navigator.addView(KEY_EGRESS, EgressLayout.class);
		navigator.addView(KEY_RETURNS, ReturnLayout.class);
		navigator.addView(KEY_COMPANY, CompanyLayout.class);
		navigator.addView(KEY_MENUS, MenuLayout.class);
		navigator.addView(KEY_NOTIFICATION, NotificationLayout.class);
		navigator.addView(KEY_MEASUREMENT_UNIT, MeasurementUnitLayout.class);
		navigator.addView(KEY_PRODUCT_CATEGORY, ProductCategoryLayout.class);
		navigator.setErrorView(DefaultView.class);
		UI.getCurrent().setNavigator(navigator);

	}

	private void buildUI(User user) {
		getSession().setAttribute(User.class, user);
		if (user == null) {
			buildLoginForm();
		} else {
			permissionUtil = new PermissionUtil(user.getRole().getPermissions());
			buildValoMenuLayout();
		}
	}

	private User getUser() {
		return getSession().getAttribute(User.class);
	}

	private void buildLoginForm() {
		VerticalLayout contentLayout = new VerticalLayout();
		contentLayout.setSizeFull();

		VerticalLayout layout = new VerticalLayout();
		layout.setWidth("400px");

		TextField txtUsername = new TextField("Usuario");
		txtUsername.setSizeFull();
		layout.addComponent(txtUsername);

		PasswordField txtPassword = new PasswordField("Contraseña");
		txtPassword.setSizeFull();
		layout.addComponent(txtPassword);

		Button loginButton = new Button("Ingresar", e -> authenticate(txtUsername.getValue(), txtPassword.getValue()));
		loginButton.setSizeFull();
		loginButton.addStyleName("mystyle");
		layout.addComponent(loginButton);

		Button changePasswordBtn = new Button("Cambiar contraseña");
		changePasswordBtn.setStyleName(ValoTheme.BUTTON_LINK);
		changePasswordBtn.addClickListener(e -> buildChangePasswordWindow(txtUsername.getValue()));
		layout.addComponent(changePasswordBtn);

		contentLayout.addComponent(layout);
		contentLayout.setComponentAlignment(layout, Alignment.TOP_CENTER);

		Panel loginPanel = new Panel("<center><h1>Vissa</h1></center>");
		loginPanel.addStyleName("well");
		loginPanel.setContent(contentLayout);

		txtPassword.addShortcutListener(new ShortcutListener("Enter form", ShortcutAction.KeyCode.ENTER, null) {
			private static final long serialVersionUID = 7141523733731956234L;

			@Override
			public void handleAction(Object sender, Object target) {
				try {

					if (((PasswordField) target).equals(txtPassword)) {
						authenticate(txtUsername.getValue(), txtPassword.getValue());
					}
					if (((Panel) target).equals(loginPanel)) {
						authenticate(txtUsername.getValue(), txtPassword.getValue());
					}

				} catch (Exception e) {
					log.error("[FormLogin][Enter][Exception] " + e.getMessage());
				}
			}
		});

		loginPanel.addShortcutListener(new ShortcutListener("Enter form", ShortcutAction.KeyCode.ENTER, null) {
			private static final long serialVersionUID = 7141523733631956234L;

			@Override
			public void handleAction(Object sender, Object target) {
				try {
					if (((Panel) target).equals(loginPanel)) {
						authenticate(txtUsername.getValue(), txtPassword.getValue());
					}

					if (((PasswordField) target).equals(txtPassword)) {
						authenticate(txtUsername.getValue(), txtPassword.getValue());
					}

				} catch (Exception e) {
					log.error("[FormLogin][Enter][Exception] " + e.getMessage());
				}
			}
		});

		setContent(loginPanel);
	}

	private void authenticate(String username, String password) {
		try {
			User user = UserBll.getInstance().select(User.builder().login(username).password(password).build());
			if (user == null) {
				ViewHelper.showNotification("Usuario o contraseña inválidos.", Notification.Type.ERROR_MESSAGE);
			} else {
				buildUI(user);
			}
		} catch (NoSuchAlgorithmException | InvalidKeySpecException | IOException ex) {
			log.error(ex);
			ViewHelper.showNotification("Contacte al administrador (3002007694)", Notification.Type.ERROR_MESSAGE);
		}
	}

	private void buildChangePasswordWindow(String login) {
		Window subWindow = ViewHelper.buildSubwindow("50%", null);
		UserLayout userLayout;
		log.info("login=" + login);
		try {
			if (login != null) {
				Commons.LOGIN = login;
				userLayout = new UserLayout();
				userLayout.setMargin(false);
				userLayout.setSpacing(false);

				VerticalLayout subContent = ViewHelper.buildVerticalLayout(true, true);
				subContent.addComponents(userLayout);
				subWindow.setContent(subContent);

				addWindow(subWindow);
			} else {
				ViewHelper.showNotification("Ingrese su usuario", Notification.Type.WARNING_MESSAGE);
			}

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	@WebServlet(urlPatterns = "/*", name = "MyUIServlet", asyncSupported = true)
	@VaadinServletConfiguration(ui = VissaUI.class, productionMode = false)
	public static class MyUIServlet extends VaadinServlet {
		private static final long serialVersionUID = -2743165194104880059L;
	}
}
