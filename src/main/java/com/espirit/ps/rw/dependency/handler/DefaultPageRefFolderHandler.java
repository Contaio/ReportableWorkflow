package com.espirit.ps.rw.dependency.handler;

import com.espirit.ps.rw.dependency.*;
import de.espirit.firstspirit.access.store.IDProvider;
import de.espirit.firstspirit.access.store.sitestore.PageRef;
import de.espirit.firstspirit.access.store.sitestore.PageRefFolder;
import de.espirit.firstspirit.access.store.sitestore.SiteStoreFolder;
import de.espirit.firstspirit.access.store.sitestore.SiteStoreRoot;

import java.util.LinkedList;
import java.util.List;

public final class DefaultPageRefFolderHandler extends AbstractDefaultHandler {
	
	public DefaultPageRefFolderHandler() {
		super(SiteStoreFolder.class);
	}
	
	
	@Override
	public void execute(final Handle handle, final Manager manager) {
		if (handle.getKeyObject() instanceof SiteStoreFolder) {
			boolean         addParentFolder = false;
			SiteStoreFolder siteStoreFolder = (SiteStoreFolder) handle.getKeyObject();
			
			switch (manager.getAction()) {
				case RELEASE:
					super.validateRelease(handle, manager);
					super.validateFormData(handle, manager);
					super.checkOutgoingReferences(handle, manager);
					
					if (siteStoreFolder.getStartNode() == null) {
						handle.addValidationState(ValidationState.createMissingStartNode(handle, siteStoreFolder));
					} else {
						handle.addNextHandleObject(new ResolveObject(siteStoreFolder.getStartNode(), handle));
					}
					
					addParentFolder = true;
					
					break;
				
				case DELETE:
					if (!(siteStoreFolder instanceof SiteStoreRoot)) {
						List<IDProvider> idProviders = new LinkedList<>();
						idProviders.addAll(siteStoreFolder.getChildren(PageRef.class).toList());
						idProviders.addAll(siteStoreFolder.getChildren(PageRefFolder.class).toList());
						
						if (handle.getPreviousHandle() != null) {
							idProviders.remove(handle.getPreviousHandle().getKeyObject());
						}
						
						if (super.checkIncomingReferences(handle, manager) && idProviders.isEmpty()) {
							handle.addValidationState(ValidationState.createDeletable(handle, siteStoreFolder));
							addParentFolder = true;
						} else if (manager.isInStartStoreElements(siteStoreFolder) && !idProviders.isEmpty()) {
							handle.addValidationState(ValidationState.createIsNotEmpty(handle, siteStoreFolder));
						}
					}
					
					break;
			}
			
			if (addParentFolder && !(siteStoreFolder instanceof SiteStoreRoot)) {
				handle.addNextHandleObject(new ResolveObject(siteStoreFolder.getParentFolder(), handle));
			}
		}
	}
}
