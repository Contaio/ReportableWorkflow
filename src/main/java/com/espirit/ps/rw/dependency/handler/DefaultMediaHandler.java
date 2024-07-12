package com.espirit.ps.rw.dependency.handler;

import com.espirit.ps.rw.dependency.*;
import de.espirit.firstspirit.access.store.mediastore.Media;

public final class DefaultMediaHandler extends AbstractDefaultHandler {
	
	public DefaultMediaHandler() {
		super(Media.class);
	}
	
	
	@Override
	public void execute(final Handle handle, final Manager manager) {
		if (handle.getKeyObject() instanceof Media) {
			Media media = (Media) handle.getKeyObject();
			
			switch (manager.getAction()) {
				case RELEASE:
					super.validateRelease(handle, manager);
					super.validateFormData(handle, manager);
					super.checkOutgoingReferences(handle, manager);
					
					break;
				
				case DELETE:
					if (super.checkIncomingReferences(handle, manager)) {
						handle.addValidationState(ValidationState.createDeletable(handle, media));
					}
					
					break;
			}
			
			handle.addNextHandleObject(new ResolveObject(media.getParent(), handle));
		}
	}
}
