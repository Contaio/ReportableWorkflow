package com.espirit.ps.rw.dependency;

import com.espirit.ps.rw.dependency.handler.*;

import java.util.LinkedList;
import java.util.List;

public interface Handler {
	
	void execute(final Handle handle, final Manager manager);
	
	enum Default {
		BROKEN_REFERENCE(DefaultBrokenReferenceHandler.class, Action.RELEASE, Action.DELETE),
		CONTENT2(DefaultContent2Handler.class, Action.RELEASE, Action.DELETE),
		DATASET(DefaultDataSetHandler.class, Action.RELEASE, Action.DELETE),
		EXTERNAL_REFERENCE(DefaultExternalReferenceHandler.class, Action.RELEASE, Action.DELETE),
		GCA_FOLDER(DefaultGCAFolderHandler.class, Action.RELEASE, Action.DELETE),
		GCA_PAGE(DefaultGCAPageHandler.class, Action.RELEASE, Action.DELETE),
		MEDIA(DefaultMediaHandler.class, Action.RELEASE, Action.DELETE),
		MEDIA_FOLDER(DefaultMediaFolderHandler.class, Action.RELEASE, Action.DELETE),
		PAGE(DefaultPageHandler.class, Action.RELEASE, Action.DELETE),
		PAGE_FOLDER(DefaultPageFolderHandler.class, Action.RELEASE, Action.DELETE),
		PAGEREF(DefaultPageRefHandler.class, Action.RELEASE, Action.DELETE),
		PAGEREF_FOLDER(DefaultPageRefFolderHandler.class, Action.RELEASE, Action.DELETE),
		SECTION(DefaultSectionHandler.class, Action.RELEASE, Action.DELETE),
		SECTION_REFERENCE(DefaultSectionReferenceHandler.class, Action.RELEASE, Action.DELETE),
		UNSUPPORTED_ELEMENT(DefaultUnsupportedElementHandler.class, Action.RELEASE, Action.DELETE),
		TEMPLATE(DefaultTemplateStoreElementHandler.class, Action.RELEASE, Action.DELETE),
		UNKNOWN_REFERENCE(DefaultUnknownReferenceHandler.class, Action.RELEASE, Action.DELETE);
		
		
		private final Action[]                 actions;
		private final Class<? extends Handler> handlerClass;
		
		
		Default(final Class<? extends Handler> handlerClass, final Action... action) {
			this.handlerClass = handlerClass;
			actions = action;
		}
		
		
		public Class<? extends Handler> getHandlerClass() {
			return handlerClass;
		}
		
		
		public static Default[] valuesByAction(final Action action) {
			List<Default> defaults = new LinkedList<>();
			
			for (Default d : values()) {
				for (int i = 0; i < d.actions.length; i++) {
					if (d.actions[i].equals(action)) {
						defaults.add(d);
					}
				}
			}
			
			return defaults.toArray(new Default[defaults.size()]);
		}
	}
}
