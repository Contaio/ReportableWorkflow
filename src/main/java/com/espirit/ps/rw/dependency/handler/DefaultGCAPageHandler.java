package com.espirit.ps.rw.dependency.handler;

import com.espirit.ps.rw.dependency.*;
import de.espirit.firstspirit.access.store.globalstore.GCAPage;

public final class DefaultGCAPageHandler extends AbstractDefaultHandler {
	
	public DefaultGCAPageHandler() {
		super(GCAPage.class);
	}
	
	
	@Override
	public void execute(final Handle handle, final Manager manager) {
		if (handle.getKeyObject() instanceof GCAPage) {
			GCAPage gcaPage = (GCAPage) handle.getKeyObject();
			
			switch (manager.getAction()) {
				case RELEASE:
					super.validateRelease(handle, manager);
					super.validateFormData(handle, manager);
					super.checkOutgoingReferences(handle, manager);
					break;
				
				case DELETE:
					if (super.checkIncomingReferences(handle, manager)) {
						handle.addValidationState(ValidationState.createDeletable(handle, gcaPage));
					}
					
					break;
			}
			
			handle.addNextHandleObject(new ResolveObject(gcaPage.getParent(), handle));
		}
	}
}
