package com.espirit.ps.rw.webapp;

import com.espirit.moddev.components.annotations.WebAppComponent;
import com.espirit.moddev.components.annotations.WebResource;
import de.espirit.firstspirit.module.WebApp;
import de.espirit.firstspirit.module.WebEnvironment;
import de.espirit.firstspirit.module.descriptor.WebAppDescriptor;

@WebAppComponent(name = "reportable_workflow_web_app",
		displayName = "Reportable Workflow - WebApp",
		webXml = "webedit/reportable_workflow/web.xml",
		scope = { WebAppDescriptor.WebAppScope.GLOBAL,WebAppDescriptor.WebAppScope.PROJECT },
		webResources = {@WebResource(name= "com.espirit.ps.rw:webfiles",
									version = "0.1", targetPath = "/", path = "webedit/")}
)
public class ReportableWorkflowDummyWebApp implements WebApp {
	
	@Override
	public void createWar() {
	
	}
	
	
	@Override
	public void init(final WebAppDescriptor webAppDescriptor, final WebEnvironment webEnvironment) {
	
	}
	
	
	@Override
	public void installed() {
	
	}
	
	
	@Override
	public void uninstalling() {
	
	}
	
	
	@Override
	public void updated(final String s) {
	
	}
}
