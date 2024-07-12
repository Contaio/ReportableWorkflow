package com.espirit.ps.rw.common.configuration;

import javax.swing.*;

public class Tab {
	
	private final JPanel   parent;
	private final Combobox controllerSelection;
	private final Label    infoMessage;
	private final Table    handlerTable;
	private final String   name;
	private final Checkbox enabled;
	
	
	public Tab(final String name, final Checkbox enabled, final Combobox controllerSelection, final Label infoMessage, final Table handlerTable) {
		this.enabled = enabled;
		this.controllerSelection = controllerSelection;
		this.infoMessage = infoMessage;
		this.handlerTable = handlerTable;
		this.name = name;
		
		parent = new JPanel();
		parent.setLayout(Panel.getPanelLayout(parent, enabled, controllerSelection, infoMessage, handlerTable));
	}
	
	
	public JComponent getComponent() {
		return parent;
	}
	
	
	public String getName() {
		return name;
	}
}