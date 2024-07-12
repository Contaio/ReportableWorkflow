package com.espirit.ps.rw.common.configuration;

import com.espirit.ps.rw.dependency.AbstractDefaultHandler;
import com.espirit.ps.rw.dependency.Action;
import com.espirit.ps.rw.dependency.DependencyUtil;
import com.espirit.ps.rw.dependency.Handler;
import de.espirit.firstspirit.module.descriptor.ComponentDescriptor;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import java.util.*;
import java.util.stream.Collectors;

public class Table implements ValueHolder<Set<String>> {
	
	private final JTable      table;
	private final JScrollPane scrollPane;
	
	
	public Table(final Object[] headers, final Object[][] data) {
		TableModel model = new TableModel(headers, data, this);
		table = new JTable(model);
		scrollPane = new JScrollPane();
		scrollPane.setColumnHeaderView(table.getTableHeader());
		scrollPane.setViewportView(table);
		
		table.getColumnModel().getColumn(TableModel.MODULE_NAME).setPreferredWidth(75);
		table.getColumnModel().getColumn(TableModel.COMPONENT_NAME).setPreferredWidth(250);
		table.getColumnModel().getColumn(TableModel.CLASS_NAME).setPreferredWidth(290);
		table.getColumnModel().getColumn(TableModel.VALUE).setPreferredWidth(50);
		table.getColumnModel().getColumn(TableModel.VALUE).setResizable(false);
	}
	
	
	public JComponent getComponent() {
		return scrollPane;
	}
	
	
	private JTable getTable() {
		return table;
	}
	
	
	@Override
	public Set<String> getValue() {
		Set<String> result = new HashSet<>();
		
		for (int r = 0; r < table.getRowCount(); r++) {
			if ((Boolean) table.getValueAt(r, TableModel.VALUE)) {
				result.add((String) table.getValueAt(r, TableModel.CLASS_NAME));
			}
		}
		
		return result;
	}
	
	
	@Override
	public void setValue(final Set<String> values) {
		for (int r = 0; r < table.getRowCount(); r++) {
			table.setValueAt(values.contains(table.getValueAt(r, TableModel.CLASS_NAME)), r, TableModel.VALUE);
		}
		
		table.repaint();
	}
	
	
	public static Object[][] createContent(final Action action, final ProjectAppConfigurationDialog<?> dialog) {
		java.util.List<Object[]> resultList = new LinkedList<>();
		java.util.List<Object[]> defaults   = new LinkedList<>();
		java.util.List<Object[]> customs    = new LinkedList<>();
		
		resultList.add(new Object[]{"<installed>", "", "*", true});
		
		for (Handler.Default defaultHandler : Handler.Default.valuesByAction(action)) {
			defaults.add(new Object[]{"<default>", defaultHandler.getHandlerClass().getSimpleName(), defaultHandler.getHandlerClass().getCanonicalName(), false});
		}
		
		resultList.addAll(sort(defaults));
		
		Set<ComponentDescriptor> handlers = new HashSet<>();
		handlers.addAll(DependencyUtil.getComponentsByType(dialog.getEnvironment().getBroker(), AbstractDefaultHandler.class));
		
		//avoid duplicates 'AbstractDefaultHandler' <=> 'Handler'
		List<String> handlerClassNames = handlers.stream().map(componentDescriptor -> componentDescriptor.getComponentClass()).collect(Collectors.toList());
		
		handlers.addAll(DependencyUtil.getComponentsByType(dialog.getEnvironment().getBroker(), Handler.class).stream().filter(componentDescriptor -> !handlerClassNames.contains(componentDescriptor.getComponentClass())).collect(Collectors.toList()));
		
		for (ComponentDescriptor componentDescriptor : handlers) {
			if (!componentDescriptor.getModuleName().equals(dialog.getModuleName())) {
				customs.add(new Object[]{componentDescriptor.getModuleName(), componentDescriptor.getName(), componentDescriptor.getComponentClass(), false});
			}
		}
		
		resultList.addAll(sort(customs));
		
		Object[][] resultArray = new Object[defaults.size() + customs.size()][4];
		
		return resultList.toArray(resultArray);
	}
	
	
	private static java.util.List<Object[]> sort(final java.util.List<Object[]> entries) {
		entries.sort(new Comparator<Object[]>() {
			@Override
			public int compare(final Object[] o1, final Object[] o2) {
				int result = ((String) o1[0]).compareTo(((String) o2[0]));
				
				if (result == 0) {
					return ((String) o1[1]).compareTo(((String) o2[1]));
				} else {
					return result;
				}
			}
		});
		
		return entries;
	}
	
	
	private static class TableModel extends AbstractTableModel {
		
		private static final long serialVersionUID = 1L;
		private static final int  MODULE_NAME      = 0;
		private static final int  COMPONENT_NAME   = 1;
		private static final int  CLASS_NAME       = 2;
		private static final int  VALUE            = 3;
		
		private final Object[]   headers;
		private final Object[][] data;
		private final Table      table;
		
		
		public TableModel(final Object[] headers, final Object[][] data, final Table table) {
			this.headers = headers;
			this.data = data;
			this.table = table;
		}
		
		
		public Class<?> getColumnClass(final int column) {
			return (getValueAt(0, column).getClass());
		}
		
		
		@Override
		public int getColumnCount() {
			return headers.length;
		}
		
		
		@Override
		public String getColumnName(final int column) {
			return headers[column].toString();
		}
		
		
		@Override
		public int getRowCount() {
			return data.length;
		}
		
		
		@Override
		public Object getValueAt(final int rowIndex, final int columnIndex) {
			return data[rowIndex][columnIndex];
		}
		
		
		public boolean isCellEditable(final int row, final int column) {
			return column == VALUE;
		}
		
		
		@Override
		public void setValueAt(final Object value, final int row, final int column) {
			if (column == VALUE && value instanceof Boolean) {
				if (row == 0) {
					data[0][VALUE] = true;
					
					if ((boolean) value) {
						for (int r = 1; r < data.length; r++) {
							data[r][VALUE] = false;
						}
					}
				} else {
					data[row][VALUE] = value;
					
					boolean onlyFirstValue = true;
					
					for (int r = 1; r < data.length; r++) {
						onlyFirstValue &= !(boolean) data[r][VALUE];
					}
					
					data[0][VALUE] = onlyFirstValue;
				}
				
				table.getTable().repaint();
			}
		}
	}
}
