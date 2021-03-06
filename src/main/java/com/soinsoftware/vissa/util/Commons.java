package com.soinsoftware.vissa.util;

import com.vaadin.ui.UI;

public class Commons {

//	public static String TRANSACTION_TYPE;
	public static String PERSON_TYPE;
	public static final String FORMAT_DATE_TIME = "dd-MM-yyyy HH:mm:ss";
	public static final String FORMAT_DATE = "dd-MM-yyyy";
	public static final String DECIMAL_FORMAT = "###,###,###.##";
	public static String MENU_NAME;
	public static String LOGIN;
	public static String ROLE;
	public static ELayoutMode LAYOUT_MODE;
	public static UI appWindow;

	public static final String PARAM_COMPANY = "P_COMPANY";
	public static final String PARAM_INVOICE_NUMBER = "P_INVOICE_NUMBER";
	public static final String PARAM_CUSTOMER = "P_CUSTOMER";
	public static final String PARAM_INVOICE_DATE = "P_INVOICE_DATE";
	public static final String PARAM_INVOICE_TYPE = "P_INVOICE_TYPE";
	public static final String PARAM_LOGO = "P_LOGO_PATH";
	public static final String PARAM_SALESMAN = "P_CASHIER";
	public static final String PARAM_RESOLUTION = "P_RESOLUTION";
	public static final String PARAM_REGIMEN = "P_REGIMEN";
	public static final String PARAM_NIT = "P_NIT";
	public static final String PARAM_ADDRESS = "P_ADDRESS";
	public static final String PARAM_PHONE = "P_PHONE";
	public static final String PARAM_MOBILE = "P_MOBILE";
	public static final String PARAM_CUSTOMER_ID = "P_CUSTOMER_ID";
	public static final String PARAM_CUSTOMER_ADDRESS = "P_CUSTOMER_ADDRESS";
	public static final String PARAM_CUSTOMER_PHONE = "P_CUSTOMER_PHONE";
	public static final String PARAM_CASH = "P_CASH";
	public static final String PARAM_TOTAL_IVA = "P_TOTAL_IVA";
	public static final String PARAM_CHANGE = "P_CHANGE";
	public static final Double PARAM_SALESMAN_CASH_BASE = 100000.0;

	public static final String SALE_REPORT_NAME = "/WEB-INF/reports/saleInvoicePOS.jrxml";
	public static final String PURCHASE_REPORT_NAME = "/WEB-INF/reports/purchaseInvoicePOS.jrxml";

}
