package com.espirit.ps.rw.common.configuration;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class Label {
	
	private final JLabel label;
	
	
	public Label(final String label) {
		this.label = new JLabel(label);
		this.label.setHorizontalAlignment(SwingConstants.CENTER);
	}
	
	
	public Label(final String label, final Color foreground) {
		this(label);
		
		this.label.setForeground(foreground);
	}
	
	
	public Label(final String label, final Color foreground, final JComponent component) {
		this(label, foreground);
		
		if (component instanceof JComboBox<?>) {
			((JComboBox<?>) component).addActionListener(new ActionListener() {
				
				@Override
				public void actionPerformed(final ActionEvent e) {
					Label.this.label.setVisible(!(((JComboBox<?>) e.getSource()).getSelectedIndex() == 0));
				}
			});
		}
	}
	
	
	public JComponent getComponent() {
		return label;
	}
}
