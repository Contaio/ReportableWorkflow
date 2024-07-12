package com.espirit.ps.rw.dependency.handler;

import com.espirit.ps.rw.dependency.*;
import de.espirit.firstspirit.access.ReferenceEntry;


public final class DefaultExternalReferenceHandler extends AbstractDefaultHandler {
	
	public DefaultExternalReferenceHandler() {
		super(ReferenceEntry.class);
	}
	
	
	@Override
	public void execute(final Handle handle, final Manager manager) {
		if (handle instanceof ReferencedEntryHandle && ((ReferencedEntryHandle) handle).getType().equals(ReferencedEntryHandle.Type.EXTERNAL)) {
			// How to handle the release and delete action, depending on the http status code
			switch (manager.getAction()) {
				case RELEASE:
					handle.addValidationState(ValidationState.createReleasable(handle, ((ReferencedEntryHandle) handle).getSourceElement()));
					break;
				case DELETE:
					break;
			}
		}
	}
}