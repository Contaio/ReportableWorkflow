package com.espirit.ps.rw.dependency.handler;

import com.espirit.ps.rw.common.ReportableWorkflowUtil;
import com.espirit.ps.rw.dependency.*;
import de.espirit.firstspirit.access.store.IDProvider;
import de.espirit.firstspirit.access.store.StoreElement;
import de.espirit.firstspirit.access.store.sitestore.PageRef;

import java.util.Set;

public final class DefaultPageRefHandler extends AbstractDefaultHandler {
	
	public DefaultPageRefHandler() {
		super(PageRef.class);
	}
	
	
	@Override
	@SuppressWarnings("unchecked")
	public void execute(final Handle handle, final Manager manager) {
		if (handle.getKeyObject() instanceof PageRef) {
			PageRef pageRef       = (PageRef) handle.getKeyObject();
			boolean addNextHandle = false;
			
			switch (manager.getAction()) {
				case RELEASE:
					if (manager.isInStartStoreElements(pageRef)) {
						super.validateRelease(handle, manager);
						super.validateData(handle, manager);
						super.checkOutgoingReferences(handle, manager);
						
						addNextHandle = true;
					} else {
						Set<StoreElement> ignoreSet = ReportableWorkflowUtil.buildIgnoreSet(manager);
						
						if (ignoreSet != null && ignoreSet.contains(handle.getKeyObject())) {
							handle.addValidationState(ValidationState.createIgnoredOnRelease(handle, (IDProvider) handle.getKeyObject()));
						} else if (pageRef.isInReleaseStore()) {
							handle.addValidationState(ValidationState.createReleasable(handle, pageRef));
						} else {
							handle.addValidationState(ValidationState.createUnreleased(handle, pageRef));
						}
					}
					
					break;
				
				case DELETE:
					if (super.checkIncomingReferences(handle, manager) && !manager.isInStartStoreElements(pageRef)) {
						handle.addValidationState(ValidationState.createDeletable(handle, pageRef));
					}
					
					addNextHandle = true;
					
					break;
			}

			// Adds the parent folder and the page of the pageref as further elements to validate.
			if (addNextHandle) {
				handle.addNextHandleObject(new ResolveObject(pageRef.getParentFolder(), handle));
				handle.addNextHandleObject(new ResolveObject(pageRef.getPage(), handle));
			}
		}
	}
}
