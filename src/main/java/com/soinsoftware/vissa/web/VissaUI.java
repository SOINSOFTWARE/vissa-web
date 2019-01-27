package com.soinsoftware.vissa.web;

import java.io.File;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

import javax.servlet.annotation.WebServlet;

import org.apache.log4j.Logger;

import com.soinsoftware.vissa.bll.UserBll;
import com.soinsoftware.vissa.model.Person;
import com.soinsoftware.vissa.model.PersonType;
import com.soinsoftware.vissa.model.TransactionType;
import com.soinsoftware.vissa.model.User;
import com.soinsoftware.vissa.util.Commons;
import com.soinsoftware.vissa.util.PermissionUtil;
import com.soinsoftware.vissa.util.ViewHelper;
import com.vaadin.annotations.Theme;
import com.vaadin.annotations.VaadinServletConfiguration;
import com.vaadin.data.TreeData;
import com.vaadin.data.provider.TreeDataProvider;
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
	protected static final String KEY_COMPANY_DATA = "companyData";
	protected static final String KEY_INVENTORY = "Inventario";
	protected static final String KEY_INVENTORY_MOV = "Movimientos";
	protected static final String KEY_PURCHASES = "Compras";
	protected static final String KEY_WAREHOUSE = "Bodegas";
	protected static final String KEY_SALES = "Ventas";
	protected static final String KEY_SALES_REPORT = "Reporte de Ventas";
	protected static final String KEY_PURCHASES_REPORT = "Reporte de Compras";
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

	private PermissionUtil permissionUtil;

	TreeDataProvider<String> dataProvider;
	TreeData<String> treeData;
	Tree<String> tree;
	ValoMenuLayout root;
	Navigator navigator;

	@Override
	protected void init(VaadinRequest vaadinRequest) {
		getPage().setTitle("Vissa ERP");
		addStyleName(ValoTheme.UI_WITH_MENU);
		Responsive.makeResponsive(this);
		buildUI(getUser());
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
		panel.setContent(root);
		setContent(root);
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
		if (permissionUtil.canView(KEY_INVENTORY)) {
			treeData.addItem(null, KEY_INVENTORY);
			if (permissionUtil.canView(KEY_PRODUCTS)) {
				treeData.addItem(KEY_INVENTORY, KEY_PRODUCTS);
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
			treeData.addItem(KEY_REPORTS, "test");
		}

		if (permissionUtil.canView(KEY_ADMINISTRATION)) {
			treeData.addItem(null, KEY_ADMINISTRATION);
			if (permissionUtil.canView(KEY_USERS)) {
				treeData.addItem(KEY_ADMINISTRATION, KEY_USERS);
			}
		}
	}

	private CssLayout buildMenu(ValoMenuLayout root) {
		CssLayout menu = new CssLayout();

		Component menuItemsLayout = buildMenuItemsLayout2(root);
		// Tree menuItemsLayout = buildMenuItemsLayout2();
		menu.addComponent(buildTopLayout());
		menu.addComponent(buildShowMenuButton(menu));
		menu.addComponent(buildMenuBar());
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

	private Component buildMenuItemsLayout2(ValoMenuLayout root) {
		VerticalLayout layout = ViewHelper.buildVerticalLayout(false, false);

		tree = new Tree<>();
		treeData = new TreeData<>();

		buildMenuItems();

		buildNavigator();
		dataProvider = new TreeDataProvider<>(treeData);
		tree.setDataProvider(dataProvider);
		tree.setStyleName("valo-menuitems");
		tree.setWidth("100%");
		if (permissionUtil.canView(KEY_PURCHASES)) {
			tree.expand(KEY_PURCHASES);
		}
		if (permissionUtil.canView(KEY_SALES)) {
			tree.expand(KEY_SALES);
		}
		if (permissionUtil.canView(KEY_INVENTORY)) {
			tree.expand(KEY_INVENTORY);
		}
		if (permissionUtil.canView(KEY_REPORTS)) {
			tree.expand(KEY_REPORTS);
		}
		if (permissionUtil.canView(KEY_ADMINISTRATION)) {
			tree.expand(KEY_ADMINISTRATION);
		}

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
			Commons.TRANSACTION_TYPE = TransactionType.SALIDA.getName();
		}
		if (item.equals(KEY_PURCHASE_INVOICES) || item.equals(KEY_PURCHASES_REPORT)) {
			Commons.TRANSACTION_TYPE = TransactionType.ENTRADA.getName();
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
		navigator.addView(KEY_WAREHOUSE, WarehouseLayout.class);
		navigator.addView(KEY_SALES_REPORT, InvoiceListLayout.class);
		navigator.addView(KEY_PURCHASES_REPORT, InvoiceListLayout.class);
		navigator.addView(KEY_USERS, PersonLayout.class);
		navigator.addView("test", ReportLayout.class);
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

		Panel loginPanel = new Panel("<center><h1>Inicio de sesión - Vissa</h1></center>");
		loginPanel.addStyleName("well");
		loginPanel.setContent(contentLayout);
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
		Window userWindow = ViewHelper.buildSubwindow("50%");
		UserLayout userLayout;
		log.info("login=" + login);
		try {
			if (login != null) {
				Commons.LOGIN = login;
				ComponentContainer viewContainer = root.getContentContainer();
				Navigator navigator = new Navigator(this, viewContainer);
				navigator.addView(KEY_SUPPLIER, UserLayout.class);
				
				userLayout = new UserLayout();
				userLayout.setCaption("Lotes");
				userLayout.setMargin(false);
				userLayout.setSpacing(false);
				
				VerticalLayout subContent = ViewHelper.buildVerticalLayout(true, true);
				subContent.addComponents(userLayout);

				userWindow.setContent(subContent);
				getUI().addWindow(userWindow);
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
