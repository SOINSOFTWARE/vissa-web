package com.soinsoftware.vissa.web;

import java.io.File;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map.Entry;
import java.util.Set;

import javax.servlet.annotation.WebServlet;

import org.apache.log4j.Logger;

import com.soinsoftware.vissa.bll.UserBll;
import com.soinsoftware.vissa.model.Person;
import com.soinsoftware.vissa.model.TransactionType;
import com.soinsoftware.vissa.model.User;
import com.soinsoftware.vissa.util.Commons;
import com.soinsoftware.vissa.util.ViewHelper;
import com.vaadin.annotations.Theme;
import com.vaadin.annotations.VaadinServletConfiguration;
import com.vaadin.client.metadata.Property;
import com.vaadin.data.TreeData;
import com.vaadin.data.provider.TreeDataProvider;
import com.vaadin.navigator.Navigator;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener;
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
import com.vaadin.ui.Tree.TreeContextClickEvent;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
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
	private static final String KEY_PRODUCTS = "Productos";
	private static final String KEY_COMPANY_DATA = "companyData";
	private static final String KEY_INVENTORY = "Inventario";
	private static final String KEY_INVENTORY_MOV = "Movimientos";
	private static final String KEY_PURCHASES = "Compras";
	private static final String KEY_FOOD_BRAND = "foodBrand";
	private static final String KEY_SALES = "Ventas";
	private static final String KEY_SUPPLIER = "Proveedores";
	private static final String KEY_CUSTOMERS = "Clientes";
	private static final String KEY_SALE_INVOICES = "Facturas de Venta";
	private static final String KEY_PURCHASE_INVOICES = "Facturas de Compra";
	private static final String KEY_SUPPLIER_LIST = "supplierList";

	private LinkedHashMap<String, String> menuItems = new LinkedHashMap<String, String>();
	private LinkedHashMap<String, FontAwesome> menuIconItems = new LinkedHashMap<String, FontAwesome>();

	TreeDataProvider dataProvider;
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
		setContent(root);
	}

	private void buildMenuItems2() {

		// Parents items
		treeData.addItem(null, KEY_PURCHASES);
		treeData.addItem(null, KEY_SALES);
		treeData.addItem(null, KEY_INVENTORY);
		

		// Couple of childless root items
		treeData.addItem(KEY_PURCHASES, KEY_PURCHASE_INVOICES);
		treeData.addItem(KEY_PURCHASES, KEY_SUPPLIER);

		treeData.addItem(KEY_SALES, KEY_SALE_INVOICES);
		treeData.addItem(KEY_SALES, KEY_CUSTOMERS);

		treeData.addItem(KEY_INVENTORY, KEY_PRODUCTS);
		
		treeData.addItem(KEY_INVENTORY, KEY_INVENTORY_MOV);
		
	}

	private void buildMenuIconItems() {
		menuIconItems.put(KEY_PURCHASES, FontAwesome.LIST);
		menuIconItems.put(KEY_SALES, FontAwesome.LIST);
		menuIconItems.put(KEY_INVENTORY, FontAwesome.PRODUCT_HUNT);
		menuIconItems.put(KEY_COMPANY_DATA, FontAwesome.BOOK);
		menuIconItems.put(KEY_SUPPLIER, FontAwesome.BOOKMARK);
		menuIconItems.put(KEY_PRODUCTS, FontAwesome.TAGS);
		menuIconItems.put(KEY_FOOD_BRAND, FontAwesome.NEWSPAPER_O);

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

	@SuppressWarnings("unchecked")
	private Component buildMenuItemsLayout2(ValoMenuLayout root) {
		VerticalLayout layout = ViewHelper.buildVerticalLayout(false, false);

		tree = new Tree<>();
		treeData = new TreeData<>();

		buildMenuItems2();

		buildNavigator();
		dataProvider = new TreeDataProvider<>(treeData);
		tree.setDataProvider(dataProvider);
		tree.setStyleName("valo-menuitems");
		tree.setWidth("100%");
		tree.expand(KEY_PURCHASES);
		tree.expand(KEY_SALES);
		tree.expand(KEY_INVENTORY);

		tree.addItemClickListener(e -> selectItem(e));
		layout.addComponent(tree);
		return tree;

	}

	private void selectItem(Tree.ItemClick<String> event) {
		String item = event.getItem();
		if (item.equals(KEY_SALE_INVOICES)) {
			Commons.DOCUMENT_TYPE = TransactionType.SALIDA.getName();
		}
		if (item.equals(KEY_PURCHASE_INVOICES)) {
			Commons.DOCUMENT_TYPE = TransactionType.ENTRADA.getName();
		}
		UI.getCurrent().getNavigator().navigateTo(item);
	}

	private void buildNavigator() {

		ComponentContainer viewContainer = root.getContentContainer();
		Navigator navigator = new Navigator(this, viewContainer);
		navigator.addView(KEY_SUPPLIER, SupplierLayout.class);
		navigator.addView(KEY_PRODUCTS, ProductLayout.class);
		navigator.addView(KEY_SUPPLIER_LIST, SupplierListLayout.class);
		navigator.addView(KEY_PURCHASE_INVOICES, PurchaseLayout.class);
		navigator.addView(KEY_SALE_INVOICES, SaleLayout.class);
		navigator.addView(KEY_INVENTORY_MOV, InventoryLayout.class);
		navigator.setErrorView(DefaultView.class);
		UI.getCurrent().setNavigator(navigator);

	}

	private void buildUI(User user) {
		getSession().setAttribute(User.class, user);
		if (user == null) {
			buildLoginForm();
		} else {
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

	@WebServlet(urlPatterns = "/*", name = "MyUIServlet", asyncSupported = true)
	@VaadinServletConfiguration(ui = VissaUI.class, productionMode = false)
	public static class MyUIServlet extends VaadinServlet {
		private static final long serialVersionUID = -2743165194104880059L;
	}
}
