package com.espirit.ps.rw.dependency;

import de.espirit.firstspirit.access.ReferenceEntry;
import de.espirit.firstspirit.access.store.StoreElement;

public final class ReferencedEntryHandle extends DefaultHandle {
	
	private final Type         type;
	private final StoreElement sourceElement;
	
	
	public ReferencedEntryHandle(final StoreElement element, final Handle previousHandle, final Manager manager, final ReferenceEntry referenceEntry, final Type type) {
		super(referenceEntry, previousHandle, manager);
		this.type = type;
		sourceElement = element;
	}
	
	
	public ReferenceEntry getReferenceEntry() {
		return (ReferenceEntry) getKeyObject();
	}
	
	
	public StoreElement getSourceElement() {
		return sourceElement;
	}
	
	
	public Type getType() {
		return type;
	}
	
	
	public enum Type {
		BROKEN,
		EXTERNAL,
		UNKNOWN
	}
}
