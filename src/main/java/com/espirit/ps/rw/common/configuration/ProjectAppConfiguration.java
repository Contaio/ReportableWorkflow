package com.espirit.ps.rw.common.configuration;


import com.espirit.ps.rw.common.AdvancedLogger;
import com.espirit.ps.rw.common.ReportableWorkflowProjectApp;
import com.espirit.ps.rw.dependency.Action;
import com.espirit.ps.rw.resources.Resources;
import de.espirit.firstspirit.access.ModuleAgent;
import de.espirit.firstspirit.access.project.Project;
import de.espirit.firstspirit.agency.ModuleAdminAgent;
import de.espirit.firstspirit.agency.ProjectAgent;
import de.espirit.firstspirit.agency.SpecialistsBroker;
import de.espirit.firstspirit.io.FileSystem;
import de.espirit.firstspirit.module.descriptor.ComponentDescriptor;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.Set;


public class ProjectAppConfiguration {

	private static final String KEY_PREFIX                    = "projectapp.configuration";
	private static final String KEY_TPP_SUPPORTED             = "tpp.supported";
	private static final String KEY_ADVANCED_LOGGING_ENABLED  = "advancedlogging.enabled";
	private static final String KEY_RELEASE_REPORT_ENABLED    = "release.report.enabled";
	private static final String KEY_HANDLERCONTROLLER_RELEASE = "handlercontroller.release";
	private static final String KEY_HANDLER_RELEASE           = "handler.release";
	private static final String KEY_DELETE_REPORT_ENABLED     = "delete.report.enabled";
	private static final String KEY_HANDLERCONTROLLER_DELETE  = "handlercontroller.delete";
	private static final String KEY_HANDLER_DELETE            = "handler.delete";

	private static final String KEY_SHOW_ALL_ITEMS      = "report.show.items.all";
	private static final String KEY_DEPENDED            = "report.show.items.depended";
	private static final String KEY_RECURSIVE           = "report.show.items.recursive";
	private static final String KEY_JUST_DEPENDED_MEDIA = "report.show.items.depended.media";
	private static final String AUTH_USERNAME           = "auth.username";
	private static final String AUTH_PASSWORD           = "auth.password";

	private static final String DELIMITER               = ";";
	private static final String KEY_USE_GLOBAL_SETTINGS = "report.use.global.settings";

	private final Map<String, Object> values = new HashMap<>();



	public ProjectAppConfiguration() {
		for (String key : getParameterNames()) {
			values.put(key, getDefaultProperty(key));
		}
	}


	public ProjectAppConfiguration(final Properties properties) {
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


	public String getPassword() {
		return values.get(AUTH_PASSWORD).toString();
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


	public String getUsername() {
		return values.get(AUTH_USERNAME).toString();
	}


	public Boolean isAdvancedLoggingEnabled() {
		//to avoid null
		return Boolean.valueOf(values.get(KEY_ADVANCED_LOGGING_ENABLED).toString()) == true;
	}


	public boolean isDeleteReportEnabled() {
		return Boolean.valueOf(values.get(KEY_DELETE_REPORT_ENABLED).toString());
	}


	public boolean isDependedEnabled() {
		return Boolean.parseBoolean(values.get(KEY_DEPENDED).toString());
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


	public boolean isJustDependedMediaEnabled() {
		return Boolean.parseBoolean(values.get(KEY_JUST_DEPENDED_MEDIA).toString());
	}


	public boolean isRecursiveEnabled() {
		return Boolean.parseBoolean(values.get(KEY_RECURSIVE).toString());
	}


	public boolean isReleaseReportEnabled() {
		return Boolean.parseBoolean(values.get(KEY_RELEASE_REPORT_ENABLED).toString());
	}


	public boolean isShowAllItemsEnabled() {
		return Boolean.parseBoolean(values.get(KEY_SHOW_ALL_ITEMS).toString());
	}


	public Boolean isTPPSupported() {
		//to avoid null
		if (values.get(KEY_TPP_SUPPORTED) != null) {
			return Boolean.parseBoolean(values.get(KEY_TPP_SUPPORTED).toString());
		}

		return false;
	}


	public boolean isUseGlobalReportConfEnabled() {
		return Boolean.parseBoolean(values.get(KEY_USE_GLOBAL_SETTINGS).toString());
	}


	public void saveConfiguration(final FileSystem<?> fileSystem) {
		saveConfiguration(this, fileSystem);
	}


	public void setAdvancedLoggingEnabled(final boolean enable) {
		values.put(KEY_ADVANCED_LOGGING_ENABLED, String.valueOf(enable));
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


	public void setDepended(boolean value) {
		values.put(KEY_DEPENDED, String.valueOf(value));
	}


	public void setJustDependedMedia(boolean value) {
		values.put(KEY_JUST_DEPENDED_MEDIA, String.valueOf(value));
	}


	public void setParameter(final String key, final Object value) {
		values.put(key, value);
	}


	public void setPassword(final String password) {
		values.put(AUTH_PASSWORD, password);
	}


	public void setRecursive(boolean value) {
		values.put(KEY_RECURSIVE, String.valueOf(value));
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


	public void setShowAllItems(boolean value) {
		values.put(KEY_SHOW_ALL_ITEMS, String.valueOf(value));
	}


	public void setTPPSupport(final boolean supported) {
		values.put(KEY_TPP_SUPPORTED, String.valueOf(supported));
	}


	public void setUseGlobalSettings(boolean value) {
		values.put(KEY_USE_GLOBAL_SETTINGS, String.valueOf(value));
	}


	public void setUsername(final String username) {
		values.put(AUTH_USERNAME, username);
	}


	public static Set<String> getParameterNames() {
		Set<String> keys = new HashSet<>();
		keys.add(KEY_TPP_SUPPORTED);
		keys.add(KEY_ADVANCED_LOGGING_ENABLED);
		keys.add(KEY_RELEASE_REPORT_ENABLED);
		keys.add(KEY_HANDLERCONTROLLER_RELEASE);
		keys.add(KEY_HANDLER_RELEASE);
		keys.add(KEY_DELETE_REPORT_ENABLED);
		keys.add(KEY_HANDLERCONTROLLER_DELETE);
		keys.add(KEY_HANDLER_DELETE);
		keys.add(KEY_USE_GLOBAL_SETTINGS);
		keys.add(KEY_SHOW_ALL_ITEMS);
		keys.add(KEY_DEPENDED);
		keys.add(KEY_RECURSIVE);
		keys.add(KEY_JUST_DEPENDED_MEDIA);
		keys.add(AUTH_USERNAME);
		keys.add(AUTH_PASSWORD);

		return keys;
	}


	private static FileSystem<?> getProjectAppConfig(final SpecialistsBroker specialistsBroker) {
		ModuleAgent      moduleAgent      = specialistsBroker.requestSpecialist(ModuleAgent.TYPE);
		ModuleAdminAgent moduleAdminAgent = specialistsBroker.requestSpecialist(ModuleAdminAgent.TYPE);
		ProjectAgent     projectAgent     = specialistsBroker.requireSpecialist(ProjectAgent.TYPE);

		Collection<ComponentDescriptor> components = moduleAgent.getComponents(ReportableWorkflowProjectApp.class);
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


	public static ProjectAppConfiguration loadConfiguration(final SpecialistsBroker specialistsBroker) {
		FileSystem<?> projectAppConfig = getProjectAppConfig(specialistsBroker);

		return loadConfiguration(projectAppConfig);
	}


	public static ProjectAppConfiguration loadConfiguration(final FileSystem<?> fileSystem) {
		try {
			InputStream inputStream = fileSystem.obtain(ReportableWorkflowProjectApp.CONFIG_PROPERTIES).load();
			Properties  properties  = new Properties();
			properties.load(inputStream);

			return new ProjectAppConfiguration(properties);
		} catch (IOException e) {
			AdvancedLogger.logError("Unable to read configuration.", e, ReportableWorkflowProjectApp.class);

			return new ProjectAppConfiguration();
		}
	}


	public static ProjectAppConfiguration saveConfiguration(final ProjectAppConfiguration configuration, final SpecialistsBroker specialistsBroker) {
		FileSystem<?> projectAppConfig = getProjectAppConfig(specialistsBroker);
		saveConfiguration(configuration, projectAppConfig);

		return configuration;
	}


	public static void saveConfiguration(final ProjectAppConfiguration configuration, final FileSystem<?> fileSystem) {
		try {
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			configuration.getProperties().store(out, "");
			fileSystem.obtain(ReportableWorkflowProjectApp.CONFIG_PROPERTIES).save(new ByteArrayInputStream(out.toByteArray()));
		} catch (IOException e) {
			AdvancedLogger.logError("Unable to read configuration.", e, ReportableWorkflowProjectApp.class);
		}
	}
}
