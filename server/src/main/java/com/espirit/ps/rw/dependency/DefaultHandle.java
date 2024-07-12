package com.espirit.ps.rw.dependency;

import java.util.LinkedList;
import java.util.List;

public class DefaultHandle implements Handle {
	
	protected final Manager               manager;
	protected final Handle                previousHandle;
	protected final Object                handleObject;
	protected final List<ResolveObject>   nextHandleObjects = new LinkedList<>();
	protected final List<ValidationState> validationStates  = new LinkedList<>();
	
	
	public DefaultHandle(final Object o, final Handle previousHandle, final Manager manager) {
		handleObject = o;
		this.previousHandle = previousHandle;
		this.manager = manager;
	}
	
	
	@Override
	public final void addNextHandleObject(final ResolveObject resolveObject) {
		if (!nextHandleObjects.contains(resolveObject)) {
			nextHandleObjects.add(resolveObject);
		}
	}
	
	
	@Override
	public final void addValidationState(final ValidationState state) {
		if (manager.getAction().equals(state.getType().getAction())) {
			validationStates.add(state);
		} else {
			throw new IllegalArgumentException("Type of state is not supported for the manger: " + state.getType().getAction().name());
		}
	}
	
	
	@Override
	public final void addValidationStates(final List<ValidationState> validationStates) {
		for (ValidationState state : validationStates) {
			if (!manager.getAction().equals(state.getType().getAction())) {
				throw new IllegalArgumentException("Type of state is not supported for the manger: " + state.getType().getAction().name());
			}
		}
		
		this.validationStates.addAll(validationStates);
	}
	
	
	@Override
	public final Object getKeyObject() {
		return handleObject;
	}
	
	
	@Override
	public final List<ResolveObject> getNextHandleObjects() {
		return nextHandleObjects;
	}
	
	
	@Override
	public final Handle getPreviousHandle() {
		return previousHandle;
	}
	
	
	@Override
	public final List<ValidationState> getValidationStates(final ValidationState.Type type) {
		List<ValidationState> validationStates = new LinkedList<>();
		
		for (ValidationState state : this.validationStates) {
			if (state.getType().equals(type)) {
				validationStates.add(state);
			}
		}
		
		return validationStates;
	}
	
	
	@Override
	public final List<ValidationState> getValidationStates() {
		return validationStates;
	}
}
