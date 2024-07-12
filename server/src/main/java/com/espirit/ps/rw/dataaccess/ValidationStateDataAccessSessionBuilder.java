package com.espirit.ps.rw.dataaccess;


import com.espirit.ps.rw.common.AdvancedLogger;
import com.espirit.ps.rw.dependency.ValidationState;
import de.espirit.firstspirit.access.BaseContext;
import de.espirit.firstspirit.client.plugin.dataaccess.DataAccessSession;
import de.espirit.firstspirit.client.plugin.dataaccess.DataAccessSessionBuilder;
import de.espirit.firstspirit.client.plugin.dataaccess.aspects.SessionBuilderAspectMap;
import de.espirit.firstspirit.client.plugin.dataaccess.aspects.SessionBuilderAspectType;


public class ValidationStateDataAccessSessionBuilder implements DataAccessSessionBuilder<ValidationState> {
	
	
	private final AbstractValidationStateDataAccessPlugin plugin;
	private final SessionBuilderAspectMap                 aspects;
	private       ValidationStateDataAccessSession        session;
	
	
	public ValidationStateDataAccessSessionBuilder(final AbstractValidationStateDataAccessPlugin plugin) {
		this.plugin = plugin;
		this.aspects = new SessionBuilderAspectMap();
	}
	
	
	@Override
	public DataAccessSession<ValidationState> createSession(final BaseContext context) {
		return session = new ValidationStateDataAccessSession(context, this);
	}
	
	
	public AdvancedLogger getAdvancedLogger() {
		return plugin.getAdvancedLogger();
	}
	
	
	@Override
	public <A> A getAspect(final SessionBuilderAspectType<A> aspectType) {
		return aspects.get(aspectType);
	}
	
	
	public AbstractValidationStateDataAccessPlugin getPlugin() {
		return plugin;
	}
	
	
	public ValidationStateDataAccessSession getSession() {
		return session;
	}
}