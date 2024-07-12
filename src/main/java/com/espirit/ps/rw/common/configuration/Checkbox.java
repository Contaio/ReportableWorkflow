package com.espirit.ps.rw.common.configuration;

import javax.swing.*;

public class Checkbox implements ValueHolder<Boolean> {
	
	private final Label     label;
	private final JCheckBox checkbox;
	
	
	public Checkbox(final String firstLabel) {
		this(firstLabel, null);
	}
	
	
	public Checkbox(final String firstLabel, final String secondLabel) {
		label = new Label(firstLabel);
		
		if (secondLabel == null || secondLabel.isEmpty()) {
			checkbox = new JCheckBox();
		} else {
			checkbox = new JCheckBox(secondLabel);
		}
	}
	
	
	public JComponent getComponent() {
		return checkbox;
	}
	
	
	public JComponent getLabel() {
		return label.getComponent();
	}
	
	
	@Override
	public Boolean getValue() {
		return checkbox.isSelected();
	}
	
	
	@Override
	public void setValue(final Boolean value) {
		if (value != null) {
			checkbox.setSelected(value);
			checkbox.repaint();
		}
	}
}
