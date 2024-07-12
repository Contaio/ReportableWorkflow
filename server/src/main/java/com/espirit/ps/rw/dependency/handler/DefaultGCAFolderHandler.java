package com.espirit.ps.rw.dependency.handler;

import com.espirit.ps.rw.dependency.*;
import de.espirit.firstspirit.access.store.IDProvider;
import de.espirit.firstspirit.access.store.globalstore.GCAFolder;
import de.espirit.firstspirit.access.store.globalstore.GCAPage;
import de.espirit.firstspirit.access.store.globalstore.GlobalContentArea;

import java.util.LinkedList;
import java.util.List;

public final class DefaultGCAFolderHandler extends AbstractDefaultHandler {
	
	public DefaultGCAFolderHandler() {
		super(GCAFolder.class);
	}
	
	
	@Override
	public void execute(final Handle handle, final Manager manager) {
		if (handle.getKeyObject() instanceof GCAFolder) {
			boolean   addParentFolder = false;
			GCAFolder gcaFolder       = (GCAFolder) handle.getKeyObject();
			
			switch (manager.getAction()) {
				case RELEASE:
					super.validateRelease(handle, manager);
					super.validateData(handle, manager);
					super.checkOutgoingReferences(handle, manager);
					
					addParentFolder = true;
					
					break;
				
				case DELETE:
					if (!(gcaFolder instanceof GlobalContentArea)) {
						List<IDProvider> idProviders = new LinkedList<>();
						idProviders.addAll(gcaFolder.getChildren(GCAPage.class).toList());
						idProviders.addAll(gcaFolder.getChildren(GCAFolder.class).toList());
						
						if (handle.getPreviousHandle() != null) {
							idProviders.remove(handle.getPreviousHandle().getKeyObject());
						}
						
						if (super.checkIncomingReferences(handle, manager) && !(gcaFolder instanceof GlobalContentArea) && idProviders.isEmpty()) {
							handle.addValidationState(ValidationState.createDeletable(handle, gcaFolder));
							addParentFolder = true;
						} else if (manager.isInStartStoreElements(gcaFolder) && !idProviders.isEmpty()) {
							handle.addValidationState(ValidationState.createIsNotEmpty(handle, gcaFolder));
						}
					}
					
					break;
			}
			
			if (addParentFolder && !(handle.getKeyObject() instanceof GlobalContentArea)) {
				handle.addNextHandleObject(new ResolveObject(gcaFolder.getParent(), handle));
			}
		}
	}
}
