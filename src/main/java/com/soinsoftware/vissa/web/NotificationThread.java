package com.soinsoftware.vissa.web;

import org.apache.log4j.Logger;

import com.soinsoftware.vissa.util.Commons;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;

public class NotificationThread implements Runnable {

	protected static final Logger log = Logger.getLogger(NotificationThread.class);
	String nameThread = "Notification";
	private boolean continuar = true;
	InvoiceLayout layout=null;
	
	public NotificationThread(InvoiceLayout layout) {
		this.layout = layout;
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		try {
			while (continuar) {
				Thread.sleep(6000);
				log.info("HILO notification");
				notificationWindow(layout);
			}

		} catch (InterruptedException exc) {
			System.out.println("Hilo principal interrumpido.");
		}
	}

	// metodo para poner el boolean a false.
	public void stop() {
		continuar = false;
	}

	private void notificationWindow(InvoiceLayout layout) {
		layout.notificationWindow();
	/*	VerticalLayout popupContent = new VerticalLayout();
		// popupContent.addComponent(new TextField("Textfield"));
		// popupContent.addComponent(new Button("Button"));
		// PopupView popup = new PopupView("Pop it up", popupContent);
		// popup.setPopupVisible(true);
		Window w = new Window("Recordatorios");
		w.setContent(popupContent);
		w.setPosition(1050, 3);
		w.setWidth("20%");
		w.setHeight("25%");
		layout.getUI().addWindow(w);
		*/
	}

}
