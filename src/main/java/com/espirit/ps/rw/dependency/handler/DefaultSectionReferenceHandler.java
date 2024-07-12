package com.espirit.ps.rw.dependency.handler;

import com.espirit.ps.rw.dependency.AbstractDefaultHandler;
import com.espirit.ps.rw.dependency.Handle;
import com.espirit.ps.rw.dependency.Manager;
import com.espirit.ps.rw.dependency.ResolveObject;
import de.espirit.firstspirit.access.store.pagestore.SectionReference;

public final class DefaultSectionReferenceHandler extends AbstractDefaultHandler {
	
	public DefaultSectionReferenceHandler() {
		super(SectionReference.class);
	}
	
	
	@Override
	public void execute(final Handle handle, final Manager manager) {
		if (handle.getKeyObject() instanceof SectionReference) {
			switch (manager.getAction()) {
				case RELEASE:
					handle.addNextHandleObject(new ResolveObject(((SectionReference<?>) handle.getKeyObject()).getReference(), handle));
					super.checkOutgoingReferences(handle, manager);
					break;
				
				case DELETE:
					break;
			}
		}
	}
}
