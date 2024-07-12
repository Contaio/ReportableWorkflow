package com.espirit.ps.rw.dependency.handler;

import com.espirit.ps.rw.dependency.*;
import de.espirit.firstspirit.access.store.IDProvider;
import de.espirit.firstspirit.access.store.mediastore.Media;
import de.espirit.firstspirit.access.store.mediastore.MediaFolder;
import de.espirit.firstspirit.access.store.mediastore.MediaStoreRoot;

import java.util.LinkedList;
import java.util.List;

public final class DefaultMediaFolderHandler extends AbstractDefaultHandler {
	
	public DefaultMediaFolderHandler() {
		super(MediaFolder.class);
	}
	
	
	@Override
	public void execute(final Handle handle, final Manager manager) {
		if (handle.getKeyObject() instanceof MediaFolder) {
			boolean     addParentFolder = false;
			MediaFolder mediaFolder     = (MediaFolder) handle.getKeyObject();
			
			switch (manager.getAction()) {
				case RELEASE:
					super.validateRelease(handle, manager);
					super.validateData(handle, manager);
					super.checkOutgoingReferences(handle, manager);
					
					addParentFolder = true;
					
					break;
				
				case DELETE:
					if (!(mediaFolder instanceof MediaStoreRoot)) {
						List<IDProvider> idProviders = new LinkedList<>();
						idProviders.addAll(mediaFolder.getChildren(Media.class).toList());
						idProviders.addAll(mediaFolder.getChildren(MediaFolder.class).toList());
						
						if (handle.getPreviousHandle() != null) {
							idProviders.remove(handle.getPreviousHandle().getKeyObject());
						}
						
						if (super.checkIncomingReferences(handle, manager) && !(mediaFolder instanceof MediaStoreRoot) && idProviders.isEmpty()) {
							handle.addValidationState(ValidationState.createDeletable(handle, mediaFolder));
							addParentFolder = true;
						} else if (manager.isInStartStoreElements(mediaFolder) && !idProviders.isEmpty()) {
							handle.addValidationState(ValidationState.createIsNotEmpty(handle, mediaFolder));
						}
					}
					
					break;
			}
			
			if (addParentFolder && !(mediaFolder instanceof MediaStoreRoot)) {
				handle.addNextHandleObject(new ResolveObject(mediaFolder.getParent(), handle));
			}
		}
	}
}
