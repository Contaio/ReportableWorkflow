package com.espirit.ps.rw.common;

import com.espirit.moddev.components.annotations.ProjectAppComponent;
import com.espirit.ps.rw.common.configuration.ProjectAppConfiguration;
import com.espirit.ps.rw.common.configuration.ProjectAppConfigurationDialog;
import de.espirit.firstspirit.access.ModuleAgent;
import de.espirit.firstspirit.access.project.Project;
import de.espirit.firstspirit.agency.ModuleAdminAgent;
import de.espirit.firstspirit.agency.ProjectAgent;
import de.espirit.firstspirit.agency.SpecialistsBroker;
import de.espirit.firstspirit.io.FileHandle;
import de.espirit.firstspirit.io.FileSystem;
import de.espirit.firstspirit.module.ProjectApp;
import de.espirit.firstspirit.module.ProjectEnvironment;
import de.espirit.firstspirit.module.descriptor.ComponentDescriptor;
import de.espirit.firstspirit.module.descriptor.ProjectAppDescriptor;

import java.util.Collection;
import java.util.Iterator;

@ProjectAppComponent(name = "reportable_workflow_projectapp", displayName = "Reportable Workflow - ProjectApp", configurable = ProjectAppConfigurationDialog.class)
public class ReportableWorkflowProjectApp implements ProjectApp {
	
	public static final String CONFIG_PROPERTIES = "configuration.properties";
	
	private ProjectEnvironment projectEnvironment;
	
	
	public ReportableWorkflowProjectApp() {
	}
	
	
	@Override
	public void init(final ProjectAppDescriptor descriptor, final ProjectEnvironment env) {
		projectEnvironment = env;
	}
	
	
	@Override
	public void installed() {
		FileSystem<? extends FileHandle> fileSystem = projectEnvironment.getConfDir();
		ProjectAppConfiguration.saveConfiguration(new ProjectAppConfiguration(), fileSystem);
	}
	
	
	@Override
	public void uninstalling() {
	}
	
	
	@Override
	public void updated(final String oldVersion) {
		FileSystem<? extends FileHandle> fileSystem    = projectEnvironment.getConfDir();
		ProjectAppConfiguration          configuration = ProjectAppConfiguration.loadConfiguration(fileSystem);
		ProjectAppConfiguration.saveConfiguration(configuration, fileSystem);
	}
	
	
	public static ProjectAppConfiguration getConfiguration(final SpecialistsBroker broker) {
		return ProjectAppConfiguration.loadConfiguration(broker);
	}
	
	
	public static boolean isInstalled(final SpecialistsBroker specialistsBroker) {
		ModuleAgent      moduleAgent      = specialistsBroker.requestSpecialist(ModuleAgent.TYPE);
		ModuleAdminAgent moduleAdminAgent = specialistsBroker.requestSpecialist(ModuleAdminAgent.TYPE);
		ProjectAgent     projectAgent     = specialistsBroker.requireSpecialist(ProjectAgent.TYPE);
		
		Collection<ComponentDescriptor> components = moduleAgent.getComponents(ReportableWorkflowProjectApp.class);
		long                            projectId  = projectAgent.getId();
		
		for (ComponentDescriptor component : components) {
			Collection<Project> projects = moduleAdminAgent.getProjectAppUsages(component.getModuleName(), component.getName());
			Iterator<Project>   iterator = projects.iterator();
			
			while (iterator.hasNext()) {
				if (iterator.next().getId() == projectId) {
					return true;
				}
			}
		}
		
		return false;
	}
}
