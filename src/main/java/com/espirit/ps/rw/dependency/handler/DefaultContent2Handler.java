package com.espirit.ps.rw.dependency.handler;

import com.espirit.ps.rw.dependency.*;
import de.espirit.firstspirit.access.store.IDProvider;
import de.espirit.firstspirit.access.store.contentstore.Content2;
import de.espirit.firstspirit.access.store.contentstore.ContentFolder;
import de.espirit.firstspirit.access.store.contentstore.ContentStoreRoot;

import java.util.LinkedList;
import java.util.List;

public final class DefaultContent2Handler extends AbstractDefaultHandler {
	
	public DefaultContent2Handler() {
		super(Content2.class);
	}
	
	
	@Override
	public void execute(final Handle handle, final Manager manager) {
		if (handle.getKeyObject() instanceof Content2) {
			boolean  addParentFolder = false;
			Content2 content2        = (Content2) handle.getKeyObject();
			
			switch (manager.getAction()) {
				case RELEASE:
					handle.addValidationState(ValidationState.createReleasable(handle, content2));
					break;
				
				case DELETE:
					if (!(content2 instanceof ContentStoreRoot)) {
						List<IDProvider> idProviders = new LinkedList<>();
						idProviders.addAll(content2.getChildren(Content2.class).toList());
						idProviders.addAll(content2.getChildren(ContentFolder.class).toList());
						
						if (handle.getPreviousHandle() != null) {
							idProviders.remove(handle.getPreviousHandle().getKeyObject());
						}
						
						if (super.checkIncomingReferences(handle, manager) && idProviders.isEmpty()) {
							handle.addValidationState(ValidationState.createDeletable(handle, content2));
							addParentFolder = true;
						} else if (manager.isInStartStoreElements(content2) && !idProviders.isEmpty()) {
							handle.addValidationState(ValidationState.createIsNotEmpty(handle, content2));
						}
					}
					
					break;
			}
			
			if (addParentFolder && !(content2 instanceof ContentStoreRoot)) {
				handle.addNextHandleObject(new ResolveObject(content2.getParent(), handle));
			}
		}
	}
}
