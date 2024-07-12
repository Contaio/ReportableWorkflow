package com.espirit.ps.rw.dependency.handler;

import com.espirit.ps.rw.dependency.AbstractDefaultHandler;
import com.espirit.ps.rw.dependency.Handle;
import com.espirit.ps.rw.dependency.Manager;
import com.espirit.ps.rw.dependency.ValidationState;
import de.espirit.firstspirit.access.store.IDProvider;
import de.espirit.firstspirit.access.store.contentstore.Content2;
import de.espirit.firstspirit.access.store.contentstore.ContentStoreRoot;
import de.espirit.firstspirit.access.store.globalstore.GlobalContentArea;
import de.espirit.firstspirit.access.store.globalstore.ProjectProperties;
import de.espirit.firstspirit.access.store.globalstore.URLProperties;
import de.espirit.firstspirit.access.store.globalstore.UserProperties;
import de.espirit.firstspirit.access.store.mediastore.MediaStoreRoot;
import de.espirit.firstspirit.access.store.pagestore.PageStoreRoot;
import de.espirit.firstspirit.access.store.sitestore.SiteStoreRoot;
import de.espirit.firstspirit.access.store.templatestore.TemplateStoreRoot;

public final class DefaultUnsupportedElementHandler extends AbstractDefaultHandler {
	
	public DefaultUnsupportedElementHandler() {
		super(Content2.class);
	}
	
	
	@Override
	public void execute(final Handle handle, final Manager manager) {
		if (manager.isInStartStoreElements(handle.getKeyObject())) {
			boolean isPageStoreRoot     = handle.getKeyObject() instanceof PageStoreRoot;
			boolean isMediaStoreRoot    = handle.getKeyObject() instanceof MediaStoreRoot;
			boolean isContentStoreRoot  = handle.getKeyObject() instanceof ContentStoreRoot;
			boolean isSiteStoreRoot     = handle.getKeyObject() instanceof SiteStoreRoot;
			boolean isTemplateStoreRoot = handle.getKeyObject() instanceof TemplateStoreRoot;
			boolean isGlobalContentArea = handle.getKeyObject() instanceof GlobalContentArea;
			boolean isProjectProperties = handle.getKeyObject() instanceof ProjectProperties;
			boolean isURLProperties     = handle.getKeyObject() instanceof URLProperties;
			boolean isUserProperties    = handle.getKeyObject() instanceof UserProperties;
			boolean isRoot              = false;
			
			isRoot |= isPageStoreRoot;
			isRoot |= isMediaStoreRoot;
			isRoot |= isContentStoreRoot;
			isRoot |= isSiteStoreRoot;
			isRoot |= isTemplateStoreRoot;
			isRoot |= isGlobalContentArea;
			isRoot |= isProjectProperties;
			isRoot |= isURLProperties;
			isRoot |= isUserProperties;
			
			switch (manager.getAction()) {
				case RELEASE:
					break;
				
				case DELETE:
					if (isRoot) {
						handle.addValidationState(ValidationState.createDeleteUnsupported(handle, (IDProvider) handle.getKeyObject()));
					}
					
					break;
			}
		}
	}
}
