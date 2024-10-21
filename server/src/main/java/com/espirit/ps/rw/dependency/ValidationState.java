package com.espirit.ps.rw.dependency;


import com.espirit.ps.rw.resources.Resources;
import de.espirit.common.base.Logging;
import de.espirit.firstspirit.access.Language;
import de.espirit.firstspirit.access.ReferenceEntry;
import de.espirit.firstspirit.access.store.IDProvider;
import de.espirit.firstspirit.access.store.StoreElement;
import de.espirit.firstspirit.access.store.sitestore.SiteStoreFolder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;


public final class ValidationState implements Comparable<ValidationState> {
	
	public static final String KEY_OBJECT       = "object";
	public static final String KEY_EDITOR_NAME  = "editor_name";
	public static final String KEY_EDITOR_LABEL = "editor_label";
	public static final String KEY_LANGUAGE     = "language";
	
	private final Handle       handle;
	private final StoreElement element;
	private final String       message;
	
	private Map<String, Object> values = new HashMap<>();
	
	private Type    type;
	private boolean isPreventing;
	
	
	public ValidationState(final Handle handle, final Type type, final StoreElement element, final String message) {
		this.handle = handle;
		this.element = element;
		this.type = type;
		this.message = message;
		isPreventing = type.isPreventing();
	}
	
	
	@Override
	public int compareTo(@NotNull final ValidationState o) {
		if (this.type.isPreventing && o.type.isPreventing || !this.type.isPreventing && !o.type.isPreventing) {
			return 0;
		} else if (this.type.isPreventing){
			return -1;
		} else {
			return 1;
		}
	}
	
	
	@Nullable
	@SuppressWarnings("unchecked")
	private <C> C get(final String key, final Class<C> type) {
		Object o = values.get(key);
		
		if (type.isInstance(o)) {
			return (C) o;
		} else {
			return null;
		}
	}
	
	
	@Nullable
	public String getEditorLabel() {
		return get(KEY_EDITOR_LABEL, String.class);
	}
	
	
	@Nullable
	public String getEditorName() {
		return get(KEY_EDITOR_NAME, String.class);
	}
	
	
	@Nullable
	public IDProvider getElement() {
		if (element instanceof IDProvider) {
			return (IDProvider) element;
		} else {
			return null;
		}
	}
	
	
	public Handle getHandle() {
		return handle;
	}
	
	
	@Nullable
	public Language getLanguage() {
		return get(KEY_LANGUAGE, Language.class);
	}
	
	
	@Nullable
	public String getMessage() {
		return message;
	}
	
	
	public Object getObject() {
		if (values.containsKey(KEY_OBJECT)) {
			return values.get(KEY_OBJECT);
		} else {
			return element;
		}
	}
	
	
	public List<Object> getReferenceChain() {
		List<Object> chain = new LinkedList<>();
		
		Handle currentHandle = handle;
		
		while (currentHandle != null && currentHandle.getKeyObject() != null) {
			chain.add(0, currentHandle.getKeyObject());
			currentHandle = currentHandle.getPreviousHandle();
		}
		
		return chain;
	}
	
	
	public String getReferenceChainString() {
		String chain = "";
		
		for (Object o : getReferenceChain()) {
			chain += "\n";
			
			if (o instanceof IDProvider) {
				String key = "first_spirit.element_type." + ((IDProvider) o).getElementType().toLowerCase();
				key = key.replaceAll("\\[.*\\]", "");
				
				chain += Resources.getLabel(key, getClass()) + ": ";
				if (((IDProvider) o).hasUid()) {
					chain += ((IDProvider) o).getUid();
				} else {
					chain += ((IDProvider) o).getElementType() + " #" + ((IDProvider) o).getId();
				}
			} else if (o instanceof StoreElement) {
				chain += ((StoreElement) o).getName();
			} else {
				chain += o.toString();
			}
		}
		
		return chain.trim();
	}
	
	
	public Type getType() {
		return type;
	}
	
	
	public boolean isPreventing() {
		return isPreventing;
	}
	
	
	public void setType(final Type type) {
		this.type = type;
	}
	
	
	public String toString() {
		return type + "[" + element.toString() + "]";
	}
	
	
	public void transformState(final Type type) {
		this.type = type;
		isPreventing = this.type.isPreventing();
	}
	
	
	public static ValidationState createBroken(final Handle handle, final StoreElement element, final ReferenceEntry referenceEntry) {
		ValidationState state = new ValidationState(handle, Type.BROKEN, element, null);
		state.values.put(KEY_OBJECT, referenceEntry);
		
		return state;
	}
	
	
	public static ValidationState createDeletable(final Handle handle, final IDProvider element) {
		return new ValidationState(handle, Type.DELETABLE, element, null);
	}
	
	
	public static ValidationState createDeleteUnsupported(final Handle handle, final IDProvider element) {
		return new ValidationState(handle, Type.UNSUPPORTED_FOR_DELETE, element, null);
	}
	
	
	public static ValidationState createDependedReleasable(final Handle handle, final StoreElement element) {
		return new ValidationState(handle, Type.DEPENDED_RELEASABLE, element, null);
	}
	
	
	public static ValidationState createDisplayOnly(final Handle handle, final IDProvider element) {
		return new ValidationState(handle, Type.DISPLAY_ONLY, element, null);
	}
	
	
	public static ValidationState createIgnoredOnDelete(final Handle handle, final IDProvider element) {
		return new ValidationState(handle, Type.IGNORED_ON_DELETE, element, null);
	}
	
	
	public static ValidationState createIgnoredOnRelease(final Handle handle, final IDProvider element) {
		Logging.logError("Hier sind wir ", ValidationState.class);
		return new ValidationState(handle, Type.IGNORED_ON_RELEASE, element, null);
	}
	
	
	public static ValidationState createInvalidData(final Handle handle, final IDProvider element, final String editorName, final String editorLabel, final Language language, final String message) {
		ValidationState state = new ValidationState(handle, Type.INVALID_DATA, element, message);
		state.values.put(KEY_EDITOR_LABEL, editorLabel);
		state.values.put(KEY_EDITOR_NAME, editorName);
		state.values.put(KEY_LANGUAGE, language);
		
		return state;
	}
	
	
	public static ValidationState createIsNotEmpty(final Handle handle, final IDProvider element) {
		return new ValidationState(handle, Type.IS_NOT_EMPTY, element, null);
	}
	
	
	public static ValidationState createIsStillReferenced(final Handle handle, final IDProvider element) {
		return new ValidationState(handle, Type.IS_STILL_REFERENCED, element, null);
	}
	
	
	public static ValidationState createMissingStartNode(final Handle handle, final SiteStoreFolder element) {
		return new ValidationState(handle, Type.MISSING_STARTNODE, element, null);
	}
	
	
	public static ValidationState createOther(final Handle handle, final StoreElement element, final String message, final Object o) {
		ValidationState state = new ValidationState(handle, Type.OTHER, element, message);
		state.values.put(KEY_OBJECT, o);
		
		return state;
	}
	
	
	public static ValidationState createReleasable(final Handle handle, final StoreElement element) {
		return new ValidationState(handle, Type.RELEASABLE, element, null);
	}
	
	
	public static ValidationState createReleaseUnsupported(final Handle handle, final IDProvider element) {
		return new ValidationState(handle, Type.UNSUPPORTED_FOR_RELEASE, element, null);
	}
	
	
	public static ValidationState createUnreleased(final Handle handle, final IDProvider element) {
		return new ValidationState(handle, Type.UNRELEASED, element, null);
	}
	
	
	public enum Type {
		BROKEN(Action.RELEASE, true),
		DISPLAY_ONLY(Action.RELEASE, false),
		INVALID_DATA(Action.RELEASE, true),
		MISSING_STARTNODE(Action.RELEASE, true),
		RELEASABLE(Action.RELEASE, false),
		DEPENDED_RELEASABLE(Action.RELEASE, false),
		OTHER(Action.RELEASE, true),
		UNRELEASED(Action.RELEASE, true),
		UNSUPPORTED_FOR_RELEASE(Action.RELEASE, false),
		IGNORED_ON_RELEASE(Action.RELEASE, false),

		IS_STILL_REFERENCED(Action.DELETE, true),
		IS_NOT_EMPTY(Action.DELETE, true),
		UNSUPPORTED_FOR_DELETE(Action.DELETE, true),
		DELETABLE(Action.DELETE, false),
		IGNORED_ON_DELETE(Action.DELETE, false);

		private Action  action;
		private boolean isPreventing;
		
		
		Type(final Action action, final boolean isPreventing) {
			this.action = action;
			this.isPreventing = isPreventing;
		}
		
		
		public Action getAction() {
			return action;
		}
		
		
		public boolean isPreventing() {
			return isPreventing;
		}
	}
}
