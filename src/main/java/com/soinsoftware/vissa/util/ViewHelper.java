// Soin Software 2018
package com.soinsoftware.vissa.util;

import com.vaadin.server.Page;
import com.vaadin.server.Resource;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.Grid;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Panel;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.Grid.SelectionMode;

/**
 * @author Carlos Rodriguez
 * @since 06/12/2018
 *
 */
public class ViewHelper {
	
	public static Button buildButton(String caption, Resource icon, String style) {
		Button button = new Button(caption, icon);
		button.addStyleName(style);
		return button;
	}
	
	@SuppressWarnings("rawtypes")
	public static Grid buildGrid(SelectionMode selectionMode) {
		Grid grid = new Grid<>();
		grid.setSelectionMode(selectionMode);
		grid.setSizeFull();
		return grid;
	}
	
	public static HorizontalLayout buildHorizontalLayout(boolean spacing, boolean margin) {
		HorizontalLayout layout = new HorizontalLayout();
		layout.setSpacing(spacing);
		layout.setMargin(margin);
		return layout;
	}
	
	public static VerticalLayout buildVerticalLayout(boolean spacing, boolean margin) {
		VerticalLayout layout = new VerticalLayout();
		layout.setSpacing(spacing);
		layout.setMargin(margin);
		return layout;
	}
	
	public static Panel buildPanel(String caption, Component content) {
		Panel panel = (caption != null) ? new Panel(caption) : new Panel();
		panel.setContent(content);
		panel.addStyleName("well");
		return panel;
	}
	
	public static void showNotification(String caption, Notification.Type type) {
		new Notification(caption, type).show(Page.getCurrent());
	}
	
	public static Window  buildSubwindow(String width) {		
		Window subdwindow = new Window();
		subdwindow.setModal(true);
		subdwindow.center();
		subdwindow.setWidth(width);	
		return subdwindow;
	}
}