package com.espirit.ps.rw.dependency;

import java.util.List;

public interface Handle {
	
	
	void addNextHandleObject(final ResolveObject o);
	
	
	void addValidationState(final ValidationState state);
	
	
	void addValidationStates(final List<ValidationState> validationStates);
	
	
	Object getKeyObject();
	
	
	List<ResolveObject> getNextHandleObjects();
	
	
	Handle getPreviousHandle();
	
	
	List<ValidationState> getValidationStates(final ValidationState.Type type);
	
	
	List<ValidationState> getValidationStates();
}
