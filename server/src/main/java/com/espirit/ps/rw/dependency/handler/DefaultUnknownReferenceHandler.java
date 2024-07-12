package com.espirit.ps.rw.dependency.handler;

import com.espirit.ps.rw.dependency.*;
import de.espirit.firstspirit.access.ReferenceEntry;

public final class DefaultUnknownReferenceHandler extends AbstractDefaultHandler {
	
	public DefaultUnknownReferenceHandler() {
		super(ReferenceEntry.class);
	}
	
	
	@Override
	public void execute(final Handle handle, final Manager manager) {
		if (handle instanceof ReferencedEntryHandle && ((ReferencedEntryHandle) handle).getType().equals(ReferencedEntryHandle.Type.UNKNOWN)) {
			switch (manager.getAction()) {
				case RELEASE:
					handle.addValidationState(ValidationState.createOther(handle, ((ReferencedEntryHandle) handle).getSourceElement(), "UNKNOWN REFERENCE", ((ReferencedEntryHandle) handle).getReferenceEntry()));
					break;
				
				case DELETE:
					break;
			}
		}
	}
}
