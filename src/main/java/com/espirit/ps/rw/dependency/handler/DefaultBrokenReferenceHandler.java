package com.espirit.ps.rw.dependency.handler;

import com.espirit.ps.rw.dependency.*;
import de.espirit.firstspirit.access.ReferenceEntry;

public final class DefaultBrokenReferenceHandler extends AbstractDefaultHandler {
	
	public DefaultBrokenReferenceHandler() {
		super(ReferenceEntry.class);
	}
	
	
	@Override
	public void execute(final Handle handle, final Manager manager) {
		if (handle instanceof ReferencedEntryHandle && ((ReferencedEntryHandle) handle).getType().equals(ReferencedEntryHandle.Type.BROKEN)) {
			handle.addValidationState(ValidationState.createBroken(handle, ((ReferencedEntryHandle) handle).getSourceElement(), ((ReferencedEntryHandle) handle).getReferenceEntry()));
		}
	}
}
