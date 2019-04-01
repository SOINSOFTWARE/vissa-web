package com.soinsoftware.vissa.util;

import com.soinsoftware.vissa.web.InvoiceLayout;
import com.soinsoftware.vissa.web.NotificationThread;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;

public class NotificationUtil {
	
	
	public  static void showNotification(InvoiceLayout layout) {
		NotificationThread nt =new NotificationThread(layout);		
		Thread t=new Thread(nt);
		t.start();
	}
	
	private void notificationWindow() {
		VerticalLayout popupContent = new VerticalLayout();
		// popupContent.addComponent(new TextField("Textfield"));
		// popupContent.addComponent(new Button("Button"));
		// PopupView popup = new PopupView("Pop it up", popupContent);
		// popup.setPopupVisible(true);
		Window w = new Window("Recordatorios");
		w.setContent(popupContent);
		w.setPosition(1050, 3);
		w.setWidth("20%");
		w.setHeight("25%");
		Commons.appWindow.addWindow(w);
	}

}
