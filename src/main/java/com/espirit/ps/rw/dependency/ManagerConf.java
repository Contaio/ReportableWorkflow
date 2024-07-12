package com.espirit.ps.rw.dependency;


import com.espirit.ps.rw.common.AdvancedLogger;
import com.espirit.ps.rw.resources.Resources;
import de.espirit.firstspirit.access.ModuleAgent;
import de.espirit.firstspirit.access.project.Project;
import de.espirit.firstspirit.agency.ModuleAdminAgent;
import de.espirit.firstspirit.agency.ProjectAgent;
import de.espirit.firstspirit.agency.SpecialistsBroker;
import de.espirit.firstspirit.io.FileSystem;
import de.espirit.firstspirit.module.ProjectApp;
import de.espirit.firstspirit.module.descriptor.ComponentDescriptor;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;


public class ManagerConf {
	
	private static final String CONFIG_PROPERTIES             = "configuration.properties";
	private static final String KEY_PREFIX                    = "projectapp.configuration";
	private static final String KEY_RELEASE_REPORT_ENABLED    = "release.report.enabled";
	private static final String KEY_HANDLERCONTROLLER_RELEASE = "handlercontroller.release";
	private static final String KEY_HANDLER_RELEASE           = "handler.release";
	private static final String KEY_DELETE_REPORT_ENABLED     = "delete.report.enabled";
	private static final String KEY_HANDLERCONTROLLER_DELETE  = "handlercontroller.delete";
	private static final String KEY_HANDLER_DELETE            = "handler.delete";
	private static final String DELIMITER                     = ";";
	
	private final Map<String, Object> values = new HashMap<>();
	
	
	public ManagerConf() {
		for (String key : getParameterNames()) {
			values.put(key, getDefaultProperty(key));
		}
	}
	
	
	public ManagerConf(final Properties properties) {
		this();
		
		
		for (String key : getParameterNames()) {
			Object value = properties.get(key);
			
			if (value != null) {
				values.put(key, value);
			}
		}
	}
	
	
	private String concatenateHandlerClassNames(final Set<String> classNames) {
		String value = "";
		
		for (String className : classNames) {
			if (!value.isEmpty()) {
				value += DELIMITER;
			}
			
			value += className;
		}
		
		return value;
	}
	
	
	private String getDefaultProperty(final String key) {
		return Resources.getConst(KEY_PREFIX + "." + key, getClass());
	}
	
	
	public Set<String> getDeleteHandlerClassNames() {
		String propertyValue = values.get(KEY_HANDLER_DELETE).toString();
		return getHandlerSet(propertyValue);
	}
	
	
	public String getDeleteHandlercontrollerClassName() {
		return values.get(KEY_HANDLERCONTROLLER_DELETE).toString();
	}
	
	
	private Set<String> getHandlerSet(final String propertyValue) {
		Set<String> handlers = new HashSet<>();
		
		for (String handlerName : propertyValue.split(DELIMITER)) {
			if (!handlerName.trim().isEmpty()) {
				handlers.add(handlerName.trim());
			}
		}
		
		return handlers;
	}
	
	
	public Object getParameter(final String key) {
		return values.get(key);
	}
	
	
	public Object getParameterOrDefault(final String key, final Object o) {
		return values.getOrDefault(key, o);
	}
	
	
	public Properties getProperties() {
		Properties properties = new Properties();
		
		for (String key : values.keySet()) {
			properties.put(key, values.get(key));
		}
		
		return properties;
	}
	
	
	public Set<String> getReleaseHandlerClassNames() {
		String propertyValue = values.get(KEY_HANDLER_RELEASE).toString();
		return getHandlerSet(propertyValue);
	}
	
	
	public String getReleaseHandlercontrollerClassName() {
		return values.get(KEY_HANDLERCONTROLLER_RELEASE).toString();
	}
	
	
	public boolean isDeleteReportEnabled() {
		return Boolean.valueOf(values.get(KEY_DELETE_REPORT_ENABLED).toString());
	}
	
	
	public boolean isEnabled(final Action action) {
		switch (action) {
			case RELEASE:
				return isReleaseReportEnabled();
			case DELETE:
				return isDeleteReportEnabled();
		}
		
		return false;
	}
	
	
	public boolean isReleaseReportEnabled() {
		return Boolean.valueOf(values.get(KEY_RELEASE_REPORT_ENABLED).toString());
	}
	
	
	public void saveConfiguration(final FileSystem<?> fileSystem, final Class<? extends ProjectApp> projectApp) {
		saveConfiguration(this, fileSystem, projectApp);
	}
	
	
	public void setDeleteHandlerClassNames(final Set<String> classNames) {
		values.put(KEY_HANDLER_DELETE, concatenateHandlerClassNames(classNames));
	}
	
	
	public void setDeleteHandlercontrollerClassName(final String className) {
		values.put(KEY_HANDLERCONTROLLER_DELETE, className);
	}
	
	
	public void setDeleteReportEnabled(final boolean enabled) {
		values.put(KEY_DELETE_REPORT_ENABLED, String.valueOf(enabled));
	}
	
	
	public void setParameter(final String key, final Object value) {
		values.put(key, value);
	}
	
	
	public void setReleaseHandlerClassNames(final Set<String> classNames) {
		values.put(KEY_HANDLER_RELEASE, concatenateHandlerClassNames(classNames));
	}
	
	
	public void setReleaseHandlercontrollerClassName(final String className) {
		values.put(KEY_HANDLERCONTROLLER_RELEASE, className);
	}
	
	
	public void setReleaseReportEnabled(final boolean enabled) {
		values.put(KEY_RELEASE_REPORT_ENABLED, String.valueOf(enabled));
	}
	
	
	public static Set<String> getParameterNames() {
		Set<String> keys = new HashSet<>();
		keys.add(KEY_RELEASE_REPORT_ENABLED);
		keys.add(KEY_HANDLERCONTROLLER_RELEASE);
		keys.add(KEY_HANDLER_RELEASE);
		keys.add(KEY_DELETE_REPORT_ENABLED);
		keys.add(KEY_HANDLERCONTROLLER_DELETE);
		keys.add(KEY_HANDLER_DELETE);
		
		return keys;
	}
	
	
	private static FileSystem<?> getProjectAppConfig(final SpecialistsBroker specialistsBroker, final Class<? extends ProjectApp> projectApp) {
		ModuleAgent      moduleAgent      = specialistsBroker.requestSpecialist(ModuleAgent.TYPE);
		ModuleAdminAgent moduleAdminAgent = specialistsBroker.requestSpecialist(ModuleAdminAgent.TYPE);
		ProjectAgent     projectAgent     = specialistsBroker.requireSpecialist(ProjectAgent.TYPE);
		
		Collection<ComponentDescriptor> components = moduleAgent.getComponents(projectApp);
		long                            projectId  = projectAgent.getId();
		
		for (ComponentDescriptor component : components) {
			Collection<Project> projects = moduleAdminAgent.getProjectAppUsages(component.getModuleName(), component.getName());
			Iterator<Project>   iterator = projects.iterator();
			
			while (iterator.hasNext()) {
				Project project = iterator.next();
				if (project.getId() == projectId) {
					return moduleAdminAgent.getProjectAppConfig(component.getModuleName(), component.getName(), project);
				}
			}
		}
		
		throw new IllegalStateException("No ProjectApp installed");
	}
	
	
	public static ManagerConf loadConfiguration(final SpecialistsBroker specialistsBroker, final Class<? extends ProjectApp> projectApp) {
		FileSystem<?> projectAppConfig = getProjectAppConfig(specialistsBroker, projectApp);
		
		return loadConfiguration(projectAppConfig, projectApp);
	}
	
	
	public static ManagerConf loadConfiguration(final FileSystem<?> fileSystem, final Class<? extends ProjectApp> projectApp) {
		try {
			InputStream inputStream = fileSystem.obtain(CONFIG_PROPERTIES).load();
			Properties  properties  = new Properties();
			properties.load(inputStream);
			
			return new ManagerConf(properties);
		} catch (IOException e) {
			AdvancedLogger.logError("Unable to read configuration.", projectApp);
			
			return new ManagerConf();
		}
	}
	
	
	public static ManagerConf saveConfiguration(final ManagerConf configuration, final SpecialistsBroker specialistsBroker, final Class<? extends ProjectApp> projectApp) {
		FileSystem<?> projectAppConfig = getProjectAppConfig(specialistsBroker, projectApp);
		saveConfiguration(configuration, projectAppConfig, projectApp);
		
		return configuration;
	}
	
	
	public static void saveConfiguration(final ManagerConf configuration, final FileSystem<?> fileSystem, final Class<? extends ProjectApp> projectApp) {
		try {
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			configuration.getProperties().store(out, "");
			fileSystem.obtain(CONFIG_PROPERTIES).save(new ByteArrayInputStream(out.toByteArray()));
		} catch (IOException e) {
			AdvancedLogger.logError("Unable to read configuration.", projectApp);
		}
	}
}
