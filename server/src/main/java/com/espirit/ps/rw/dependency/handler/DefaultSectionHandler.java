package com.espirit.ps.rw.dependency.handler;

import com.espirit.ps.rw.dependency.AbstractDefaultHandler;
import com.espirit.ps.rw.dependency.Handle;
import com.espirit.ps.rw.dependency.Manager;
import com.espirit.ps.rw.dependency.ValidationState;
import de.espirit.firstspirit.access.store.globalstore.GCASection;
import de.espirit.firstspirit.access.store.pagestore.Section;

public final class DefaultSectionHandler extends AbstractDefaultHandler {
	
	public DefaultSectionHandler() {
		super(Section.class);
	}
	
	
	@Override
	public void execute(final Handle handle, final Manager manager) {
		if (handle.getKeyObject() instanceof Section<?> && !(handle.getKeyObject() instanceof GCASection)) {
			Section<?> section = (Section<?>) handle.getKeyObject();
			
			switch (manager.getAction()) {
				case RELEASE:
					super.validateRelease(handle, manager);
					super.validateData(handle, manager);
					super.checkOutgoingReferences(handle, manager);
					break;
				
				case DELETE:
					if (super.checkIncomingReferences(handle, manager)) {
						handle.addValidationState(ValidationState.createDeletable(handle, section));
					}
					break;
			}
		}
	}
}
