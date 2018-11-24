package com.soinsoftware.vissa.web;

import com.vaadin.navigator.View;
import com.vaadin.ui.VerticalLayout;

public class DefaultView extends VerticalLayout implements View {
	
	private static final long serialVersionUID = -4823544347906605726L;

	public DefaultView() {
		addComponent(new VerticalLayout());
    }
}