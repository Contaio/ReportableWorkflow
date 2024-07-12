package com.espirit.ps.rw.dataaccess;


import com.espirit.ps.rw.common.AdvancedLogger;
import com.espirit.ps.rw.dependency.ValidationState;
import de.espirit.firstspirit.access.BaseContext;
import de.espirit.firstspirit.client.plugin.dataaccess.DataAccessSession;
import de.espirit.firstspirit.client.plugin.dataaccess.DataSnippetProvider;
import de.espirit.firstspirit.client.plugin.dataaccess.DataStreamBuilder;
import de.espirit.firstspirit.client.plugin.dataaccess.aspects.SessionAspectMap;
import de.espirit.firstspirit.client.plugin.dataaccess.aspects.SessionAspectType;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;


public class ValidationStateDataAccessSession implements DataAccessSession<ValidationState> {
	
	private final BaseContext                             context;
	private final ValidationStateDataAccessSessionBuilder sessionBuilder;
	private final SessionAspectMap                        aspects;
	private final List<ValidationState>                   validationStates;
	private final List<ValidationState>                   callerChain;
	private final List<ValidationState>                   storedCallerChain;
	
	private ValidationStateDataStreamBuilder   streamBuilder;
	private ValidationStateDataSnippetProvider snippetProvider;
	
	
	public ValidationStateDataAccessSession(final BaseContext context, final ValidationStateDataAccessSessionBuilder builder) {
		this.context = context;
		sessionBuilder = builder;
		aspects = new SessionAspectMap();
		validationStates = new LinkedList<>();
		callerChain = new LinkedList<>();
		storedCallerChain = new LinkedList<>();
	}
	
	
	public void addCaller(final ValidationState state) {
		boolean add = true;
		
		for (ValidationState callerState : callerChain) {
			if (callerState.getElement().equals(state.getElement())) {
				add = false;
				break;
			}
		}
		
		if (add) {
			callerChain.add(state);
		}
	}
	
	
	public void addOrRestoreCaller(final ValidationState state) {
		int index = -1;
		
		for (int i = 0; i < callerChain.size(); i++) {
			ValidationState callerState = callerChain.get(i);
			if (callerState.getElement().equals(state.getElement())) {
				index = i;
				break;
			}
		}
		
		if (index == -1) {
			callerChain.add(state);
		} else {
			while (callerChain.size() > index + 1) {
				callerChain.remove(callerChain.size() - 1);
			}
		}
	}
	
	
	@Override
	public DataSnippetProvider<ValidationState> createDataSnippetProvider() {
		return snippetProvider = new ValidationStateDataSnippetProvider(context, this);
	}
	
	
	@Override
	public DataStreamBuilder<ValidationState> createDataStreamBuilder() {
		return streamBuilder = new ValidationStateDataStreamBuilder(context, this);
	}
	
	
	public AdvancedLogger getAdvancedLogger() {
		return sessionBuilder.getAdvancedLogger();
	}
	
	
	@Override
	public <A> A getAspect(final SessionAspectType<A> aspectType) {
		return aspects.get(aspectType);
	}
	
	
	public List<ValidationState> getCallerChain() {
		return callerChain;
	}
	
	
	public int getCallerIndex(final ValidationState state) {
		int index = -1;
		
		if (!callerChain.isEmpty()) {
			index = callerChain.indexOf(state);
		} else if (!storedCallerChain.isEmpty()) {
			index = storedCallerChain.indexOf(state);
		}
		
		return index;
	}
	
	
	@Override
	public ValidationState getData(final String identifier) throws NoSuchElementException {
		ValidationState state = validationStates.get(Integer.valueOf(identifier));
		if (state == null) {
			new NoSuchElementException(String.format("ValidationStates doesn't contains identifier %s", identifier));
		}
		return state;
	}
	
	
	@Override
	public List<ValidationState> getData(final Collection<String> identifierList) {
		List<ValidationState> validationStates = new LinkedList<>();
		for (String identifier : identifierList) {
			try {
				validationStates.add(getData(identifier));
			} catch (NoSuchElementException e) {
				sessionBuilder.getAdvancedLogger().logError("Element not found!", e);
			}
		}
		return validationStates;
	}
	
	
	@Override
	public String getIdentifier(final ValidationState state) throws NoSuchElementException {
		if (!validationStates.contains(state)) {
			sessionBuilder.getAdvancedLogger().logInfo(String.format("ValidationStates doesn't contains %s", state.toString()));
			validationStates.add(state);
		}
		return String.valueOf(validationStates.indexOf(state));
	}
	
	
	public ValidationState getLastCaller() {
		return callerChain.get(callerChain.size() - 1);
	}
	
	
	public ValidationStateDataAccessSessionBuilder getSessionBuilder() {
		return sessionBuilder;
	}
	
	
	public ValidationStateDataStreamBuilder getStreamBuilder() {
		return streamBuilder;
	}
	
	
	public boolean isCaller(final ValidationState state) {
		if (!callerChain.isEmpty()) {
			return callerChain.contains(state);
		} else if (!storedCallerChain.isEmpty()) {
			return storedCallerChain.contains(state);
		}
		
		return false;
	}
	
	
	public void restoreCallerChain() {
		callerChain.clear();
		callerChain.addAll(storedCallerChain);
		storedCallerChain.clear();
	}
	
	
	public void storeCallerChain() {
		storedCallerChain.clear();
		storedCallerChain.addAll(callerChain);
		callerChain.clear();
	}
}