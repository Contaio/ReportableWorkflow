package com.espirit.ps.rw.dataaccess;


import com.espirit.ps.rw.common.AdvancedLogger;
import com.espirit.ps.rw.common.ReportableWorkflowProjectApp;
import com.espirit.ps.rw.dependency.Action;
import com.espirit.ps.rw.dependency.ValidationState;
import com.espirit.ps.rw.resources.Resources;
import de.espirit.firstspirit.access.BaseContext;
import de.espirit.firstspirit.agency.Image;
import de.espirit.firstspirit.client.plugin.dataaccess.DataAccessPlugin;
import de.espirit.firstspirit.client.plugin.dataaccess.DataAccessSessionBuilder;
import de.espirit.firstspirit.client.plugin.dataaccess.aspects.DataAccessAspectMap;
import de.espirit.firstspirit.client.plugin.dataaccess.aspects.DataAccessAspectType;
import de.espirit.firstspirit.client.plugin.dataaccess.aspects.ReportItemsProviding;
import de.espirit.firstspirit.client.plugin.dataaccess.aspects.Reporting;


public abstract class AbstractValidationStateDataAccessPlugin implements DataAccessPlugin<ValidationState> {
	
	protected final DataAccessAspectMap aspects = new DataAccessAspectMap();
	protected final Action              action;
	
	protected ValidationStateDataAccessSessionBuilder sessionBuilder;
	protected BaseContext                             context;
	protected AdvancedLogger                          advancedLogger;
	
	
	protected AbstractValidationStateDataAccessPlugin(final Action action) {
		this.action = action;
	}
	
	
	@Override
	public DataAccessSessionBuilder<ValidationState> createSessionBuilder() {
		return sessionBuilder = new ValidationStateDataAccessSessionBuilder(this);
	}
	
	
	public Action getAction() {
		return action;
	}
	
	
	public AdvancedLogger getAdvancedLogger() {
		return advancedLogger;
	}
	
	
	@Override
	public <A> A getAspect(final DataAccessAspectType<A> aspectType) {
		return aspects.get(aspectType);
	}
	
	
	@Override
	public Image<?> getIcon() {
		return null;
	}
	
	
	@Override
	public String getLabel() {
		return Resources.getLabel("data.access.label." + action.name().toLowerCase(), getClass());
	}
	
	
	public ValidationStateDataAccessSessionBuilder getSessionBuilder() {
		return sessionBuilder;
	}
	
	
	@Override
	public void setUp(final BaseContext context) {
		this.context = context;
		advancedLogger = new AdvancedLogger(context, getClass());
		
		if (ReportableWorkflowProjectApp.isInstalled(this.context) && ReportableWorkflowProjectApp.getConfiguration(this.context).isEnabled(action)) {
			aspects.put(Reporting.TYPE, new DataAccessReportingAspect(this.context, "dependency-report", action, this));
			aspects.put(ReportItemsProviding.TYPE, new ValidationStateReportItems(this.context, this));
		}
	}
	
	
	@Override
	public void tearDown() {
	}
}