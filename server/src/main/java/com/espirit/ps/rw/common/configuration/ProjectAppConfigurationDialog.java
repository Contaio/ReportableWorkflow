package com.espirit.ps.rw.common.configuration;


import de.espirit.firstspirit.module.Configuration;
import de.espirit.firstspirit.module.ProjectEnvironment;

import javax.swing.*;
import java.awt.*;
import java.util.Set;


public class ProjectAppConfigurationDialog<E extends ProjectEnvironment> implements Configuration<E> {
	
	private E                       env;
	private String                  moduleName;
	private ProjectAppConfiguration configuration;
	private Panel                   panel;
	
	
	@Override
	public E getEnvironment() {
		return env;
	}
	
	
	@Override
	public JComponent getGui(final Frame frame) {
		panel = new Panel(this);
		
		setValuesToGui(configuration, panel);
		
		return panel;
	}
	
	
	public String getModuleName() {
		return moduleName;
	}
	
	
	@Override
	public String getParameter(final String s) {
		return configuration.getParameterOrDefault(s, "").toString();
	}
	
	
	@Override
	public Set<String> getParameterNames() {
		return ProjectAppConfiguration.getParameterNames();
	}
	
	
	@Override
	public boolean hasGui() {
		return true;
	}
	
	
	@Override
	public void init(final String moduleName, final String componentName, final E env) {
		this.moduleName = moduleName;
		this.env = env;
	}
	
	
	@Override
	public void load() {
		configuration = ProjectAppConfiguration.loadConfiguration(env.getConfDir());
	}
	
	
	@Override
	public void store() {
		getValueFromGui(configuration, panel);
		configuration.saveConfiguration(env.getConfDir());
	}
	
	
	private static void getValueFromGui(final ProjectAppConfiguration configuration, final Panel panel) {
		configuration.setTPPSupport(panel.isTPPSupported().getValue());
		configuration.setAdvancedLoggingEnabled(panel.isLoggingEnabled().getValue());
		
		// release
		configuration.setReleaseReportEnabled(panel.getReleaseReportEnabled().getValue());
		configuration.setReleaseHandlercontrollerClassName(panel.getReleaseHandlerController().getValue());
		configuration.setReleaseHandlerClassNames(panel.getReleaseHandlerTable().getValue());
		
		// delete
		configuration.setDeleteReportEnabled(panel.getDeleteReportEnabled().getValue());
		configuration.setDeleteHandlercontrollerClassName(panel.getDeleteHandlerController().getValue());
		configuration.setDeleteHandlerClassNames(panel.getDeleteHandlerTable().getValue());
	}
	
	
	private static void setValuesToGui(final ProjectAppConfiguration configuration, final Panel panel) {
		panel.isTPPSupported().setValue(configuration.isTPPSupported());
		panel.isLoggingEnabled().setValue(configuration.isAdvancedLoggingEnabled());
		
		// release
		panel.getReleaseReportEnabled().setValue(configuration.isReleaseReportEnabled());
		panel.getReleaseHandlerController().setValue(configuration.getReleaseHandlercontrollerClassName());
		panel.getReleaseHandlerTable().setValue(configuration.getReleaseHandlerClassNames());
		
		// delete
		panel.getDeleteReportEnabled().setValue(configuration.isDeleteReportEnabled());
		panel.getDeleteHandlerController().setValue(configuration.getDeleteHandlercontrollerClassName());
		panel.getDeleteHandlerTable().setValue(configuration.getDeleteHandlerClassNames());
	}
}