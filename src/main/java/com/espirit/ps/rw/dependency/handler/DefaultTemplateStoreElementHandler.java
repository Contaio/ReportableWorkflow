package com.espirit.ps.rw.dependency.handler;

import com.espirit.ps.rw.dependency.AbstractDefaultHandler;
import com.espirit.ps.rw.dependency.Handle;
import com.espirit.ps.rw.dependency.Manager;
import com.espirit.ps.rw.dependency.ValidationState;
import de.espirit.firstspirit.access.store.IDProvider;
import de.espirit.firstspirit.access.store.Store;
import de.espirit.firstspirit.access.store.StoreElement;
import de.espirit.firstspirit.access.store.templatestore.Schema;
import de.espirit.firstspirit.access.store.templatestore.TemplateFolder;
import de.espirit.firstspirit.access.store.templatestore.TemplateStoreElement;

import java.util.List;

public final class DefaultTemplateStoreElementHandler extends AbstractDefaultHandler {
	
	public DefaultTemplateStoreElementHandler() {
		super(TemplateStoreElement.class);
	}
	
	
	@Override
	public void execute(final Handle handle, final Manager manager) {
		if (handle.getKeyObject() instanceof TemplateStoreElement) {
			TemplateStoreElement templateStoreElement = (TemplateStoreElement) handle.getKeyObject();
			
			switch (manager.getAction()) {
				case RELEASE:
					super.validateRelease(handle, manager);
					super.checkOutgoingReferences(handle, manager);
					
					break;
				
				case DELETE:
					if (manager.isInStartStoreElements(templateStoreElement)) {
						boolean deletable = true;
						
						if (templateStoreElement instanceof TemplateFolder || templateStoreElement instanceof Schema) {
							List<IDProvider> idProviders = templateStoreElement.getChildren(IDProvider.class).toList();
							
							if (!idProviders.isEmpty()) {
								handle.addValidationState(ValidationState.createIsNotEmpty(handle, templateStoreElement));
								deletable = false;
							}
						}
						
						deletable &= super.checkIncomingReferences(handle, manager);
						
						if (deletable) {
							handle.addValidationState(ValidationState.createDeletable(handle, templateStoreElement));
						}
					} else {
						handle.addValidationState(ValidationState.createDeleteUnsupported(handle, templateStoreElement));
					}
					
					break;
			}
		} else if (handle.getKeyObject() instanceof StoreElement
				&& ((StoreElement) handle.getKeyObject()).getStore().getType().equals(Store.Type.TEMPLATESTORE)) {
			switch (manager.getAction()) {
				case RELEASE:
					break;
				
				case DELETE:
					if (manager.isInStartStoreElements(handle.getKeyObject())) {
						handle.addValidationState(ValidationState.createDeleteUnsupported(handle, (IDProvider) handle.getKeyObject()));
					}
					break;
			}
		}
	}
}
