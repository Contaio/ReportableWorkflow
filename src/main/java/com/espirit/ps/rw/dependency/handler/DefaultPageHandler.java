package com.espirit.ps.rw.dependency.handler;


import de.espirit.common.base.Logging;
import de.espirit.firstspirit.access.store.globalstore.GCAPage;
import de.espirit.firstspirit.access.store.pagestore.Page;
import de.espirit.firstspirit.access.store.pagestore.Section;
import com.espirit.ps.rw.dependency.*;

public final class DefaultPageHandler extends AbstractDefaultHandler {
	
	public DefaultPageHandler() {
		super(Page.class);
	}
	
	
	@Override
	public void execute(final Handle handle, final Manager manager) {
		if (handle.getKeyObject() instanceof Page && !(handle.getKeyObject() instanceof GCAPage)) {
			Page page = (Page) handle.getKeyObject();
			
			switch (manager.getAction()) {
				case RELEASE:
					super.validateRelease(handle, manager);
					super.validateFormData(handle, manager);
					super.checkOutgoingReferences(handle, manager);
					
					break;
				
				case DELETE:
					// If the page is referenced by other pagerefs than the one to be deleted, it mustn't be deleted.

					if (page.getIncomingReferences().length > 1) {
						Logging.logInfo("Page is referenced by more than one Pageref, must not be deleted!.", this.getClass());
						handle.addValidationState(ValidationState.createIgnoredOnDelete(handle, page));

					} else if (super.checkIncomingReferences(handle, manager)) {
						handle.addValidationState(ValidationState.createDeletable(handle, page));
					}
					
					break;
			}
			
			for (Section<?> section : page.getChildren(Section.class, true)) {
				handle.addNextHandleObject(new ResolveObject(section, handle));
			}
			
			handle.addNextHandleObject(new ResolveObject(page.getParent(), handle));
		}
	}
}
