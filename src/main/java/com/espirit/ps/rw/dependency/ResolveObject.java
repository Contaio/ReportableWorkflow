package com.espirit.ps.rw.dependency;


public class ResolveObject {
	
	private final Object  object;
	private final Handle  previousHandle;
	private final boolean mandatory;
	
	
	public ResolveObject(final Object object, final Handle previousHandle) {
		this(object, previousHandle, true);
	}
	
	
	public ResolveObject(final Object object, final Handle previousHandle, final boolean mandatory) {
		this.object = object;
		this.previousHandle = previousHandle;
		this.mandatory = mandatory;
	}
	
	
	public Object getObject() {
		return object;
	}
	
	
	public Handle getPreviousHandle() {
		return previousHandle;
	}
	
	
	public boolean isMandatory() {
		return mandatory;
	}
	
	
	@Override
	public String toString() {
		return "ResolveObject{" +
				"_object=" + object +
				'}';
	}
}
