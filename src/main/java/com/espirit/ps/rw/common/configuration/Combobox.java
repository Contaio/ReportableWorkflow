package com.espirit.ps.rw.common.configuration;

import com.espirit.ps.rw.dependency.AbstractHandlerController;
import com.espirit.ps.rw.dependency.DefaultHandlerController;
import com.espirit.ps.rw.dependency.DependencyUtil;
import com.espirit.ps.rw.dependency.HandlerController;
import de.espirit.firstspirit.module.descriptor.ComponentDescriptor;

import javax.swing.*;
import java.util.*;

public class Combobox implements ValueHolder<String> {
	
	
	private final Label            label;
	private final JComboBox<Entry> combobox;
	
	
	public Combobox(final String label, final ProjectAppConfigurationDialog<?> dialog) {
		this.label = new Label(label);
		combobox = new JComboBox<Entry>(createEntries(dialog));
	}

	public Combobox(final String label, final Entry[] entries) {
		this.label = new Label(label);
		combobox = new JComboBox<Entry>(entries);
	}
	
	
	private Entry[] createEntries(final ProjectAppConfigurationDialog<?> dialog) {
		List<Entry> entryList = new LinkedList<>();
		
		Set<ComponentDescriptor> controllers = new HashSet<>();
		controllers.addAll(DependencyUtil.getComponentsByType(dialog.getEnvironment().getBroker(), HandlerController.class));
		controllers.addAll(DependencyUtil.getComponentsByType(dialog.getEnvironment().getBroker(), AbstractHandlerController.class));
		
		for (ComponentDescriptor componentDescriptor : controllers) {
			if (!componentDescriptor.getModuleName().equals(dialog.getModuleName())) {
				Entry entry = new Entry(componentDescriptor.getComponentClass(), componentDescriptor.getName());
				
				if (!entryList.contains(entry)) {
					entryList.add(entry);
				}
			}
		}
		
		entryList.sort(new Comparator<Entry>() {
			@Override
			public int compare(final Entry e1, final Entry e2) {
				return e1.getLabel().compareTo(e2.getLabel());
			}
		});
		
		entryList.add(0, new Entry(DefaultHandlerController.class.getCanonicalName(), DefaultHandlerController.class.getSimpleName()));
		
		return entryList.toArray(new Entry[entryList.size()]);
	}
	
	
	public JComponent getComponent() {
		return combobox;
	}
	
	
	public JComponent getLabel() {
		return label.getComponent();
	}
	
	
	@Override
	public String getValue() {
		return ((Entry) combobox.getSelectedItem()).getValue();
	}
	
	
	@Override
	public void setValue(final String value) {
		for (int i = 0; i < combobox.getItemCount(); i++) {
			if (combobox.getItemAt(i).getValue().equals(value)) {
				combobox.setSelectedIndex(i);
				combobox.repaint();
			}
		}
	}
	
	
	public static class Entry {
		
		private final String value;
		private final String label;
		
		
		public Entry(final String value) {
			this.value = value;
			label = value;
		}
		
		
		public Entry(final String value, final String label) {
			this.value = value;
			this.label = label;
		}
		
		
		@Override
		public boolean equals(final Object obj) {
			if (obj instanceof Entry) {
				return value.equals(((Entry) obj).value);
			}
			
			return false;
		}
		
		
		public String getLabel() {
			return label;
		}
		
		
		public String getValue() {
			return value;
		}
		
		
		@Override
		public int hashCode() {
			return value.hashCode();
		}
		
		
		@Override
		public String toString() {
			return label;
		}
	}
}
