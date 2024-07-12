package com.espirit.ps.rw.dependency.handler;

import com.espirit.ps.rw.dependency.*;
import de.espirit.firstspirit.access.store.IDProvider;
import de.espirit.firstspirit.access.store.pagestore.Page;
import de.espirit.firstspirit.access.store.pagestore.PageFolder;
import de.espirit.firstspirit.access.store.pagestore.PageStoreRoot;

import java.util.LinkedList;
import java.util.List;

public final class DefaultPageFolderHandler extends AbstractDefaultHandler {
	
	public DefaultPageFolderHandler() {
		super(PageFolder.class);
	}
	
	
	@Override
	public void execute(final Handle handle, final Manager manager) {
		if (handle.getKeyObject() instanceof PageFolder) {
			boolean    addParentFolder = false;
			PageFolder pageFolder      = (PageFolder) handle.getKeyObject();
			
			switch (manager.getAction()) {
				case RELEASE:
					super.validateRelease(handle, manager);
					super.validateFormData(handle, manager);
					super.checkOutgoingReferences(handle, manager);
					
					addParentFolder = true;
					
					break;
				
				case DELETE:
					if (!(pageFolder instanceof PageStoreRoot)) {
						List<IDProvider> idProviders = new LinkedList<>();
						idProviders.addAll(pageFolder.getChildren(Page.class).toList());
						idProviders.addAll(pageFolder.getChildren(PageFolder.class).toList());
						
						if (handle.getPreviousHandle() != null) {
							idProviders.remove(handle.getPreviousHandle().getKeyObject());
						}
						
						if (super.checkIncomingReferences(handle, manager) && idProviders.isEmpty()) {
							handle.addValidationState(ValidationState.createDeletable(handle, pageFolder));
							addParentFolder = true;
						} else if (manager.isInStartStoreElements(pageFolder) && !idProviders.isEmpty()) {
							handle.addValidationState(ValidationState.createIsNotEmpty(handle, pageFolder));
						}
					}
					
					break;
			}
			
			if (addParentFolder && !(pageFolder instanceof PageStoreRoot)) {
				handle.addNextHandleObject(new ResolveObject(pageFolder.getParent(), handle));
			}
		}
	}
}
