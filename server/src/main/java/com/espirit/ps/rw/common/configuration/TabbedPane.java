package com.espirit.ps.rw.common.configuration;

import javax.swing.*;

public class TabbedPane {
	
	private final JTabbedPane tabbedPane;
	
	
	public TabbedPane(final Tab release, final Tab delete) {
		tabbedPane = new JTabbedPane(JTabbedPane.TOP);
		tabbedPane.addTab(release.getName(), release.getComponent());
		tabbedPane.addTab(delete.getName(), delete.getComponent());
	}
	
	
	public JComponent getComponent() {
		return tabbedPane;
	}
}
