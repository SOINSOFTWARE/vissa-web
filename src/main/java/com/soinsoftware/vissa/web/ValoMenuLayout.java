package com.soinsoftware.vissa.web;

import com.vaadin.ui.Component;
import com.vaadin.ui.ComponentContainer;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.HorizontalLayout;

public class ValoMenuLayout extends HorizontalLayout { 
	 
    private static final long serialVersionUID = -2997472862155084141L;

	CssLayout contentArea = new CssLayout(); 
 
    CssLayout menuArea = new CssLayout(); 
 
    public ValoMenuLayout() { 
        setSizeFull(); 
 
        menuArea.setPrimaryStyleName("valo-menu"); 
 
        contentArea.setPrimaryStyleName("valo-content"); 
        contentArea.addStyleName("v-scrollable"); 
        contentArea.setSizeFull(); 
 
        addComponents(menuArea, contentArea); 
        setExpandRatio(contentArea, 1); 
    } 
 
    public ComponentContainer getContentContainer() { 
        return contentArea; 
    } 
 
    public void addMenu(Component menu) { 
        menu.addStyleName("valo-menu-part"); 
        menuArea.addComponent(menu); 
    }
}