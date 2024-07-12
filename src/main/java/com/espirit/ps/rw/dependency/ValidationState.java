package com.espirit.ps.rw.dependency;


import com.espirit.ps.rw.resources.Resources;
import de.espirit.firstspirit.access.Language;
import de.espirit.firstspirit.access.ReferenceEntry;
import de.espirit.firstspirit.access.store.IDProvider;
import de.espirit.firstspirit.access.store.StoreElement;
import de.espirit.firstspirit.access.store.sitestore.SiteStoreFolder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;


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
		this.isPreventing = type.isPreventing();
	}


	@Override
	public int compareTo(@NotNull final ValidationState o) {
		if (this.type.isPreventing && o.type.isPreventing || !this.type.isPreventing && !o.type.isPreventing) {
			return 0;
		} else if (this.type.isPreventing) {
			return -1;
		} else {
			return 1;
		}
	}


	@Override
	public boolean equals(final Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		final ValidationState that = (ValidationState) o;

		if (isPreventing != that.isPreventing) return false;
		if (!Objects.equals(element, that.element)) return false;
		if (!Objects.equals(message, that.message)) return false;
		return type == that.type;
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
		return values.getOrDefault(KEY_OBJECT, element);
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
		StringBuilder chain = new StringBuilder();

		for (Object o : getReferenceChain()) {
			chain.append("\n");

			if (o instanceof IDProvider) {
				String key = "first_spirit.element_type." + ((IDProvider) o).getElementType().toLowerCase();
				key = key.replaceAll("\\[.*\\]", "");

				chain.append(Resources.getLabel(key, getClass())).append(": ");
				if (((IDProvider) o).hasUid()) {
					chain.append(((IDProvider) o).getUid());
				} else {
					chain.append(((IDProvider) o).getElementType()).append(" #").append(((IDProvider) o).getId());
				}
			} else if (o instanceof StoreElement) {
				chain.append(((StoreElement) o).getName());
			} else {
				chain.append(o.toString());
			}
		}

		return chain.toString().trim();
	}


	public Type getType() {
		return type;
	}


	@Override
	public int hashCode() {
		int result = element != null ? element.hashCode() : 0;
		result = 31 * result + (message != null ? message.hashCode() : 0);
		result = 31 * result + (type != null ? type.hashCode() : 0);
		result = 31 * result + (isPreventing ? 1 : 0);
		return result;
	}


	public boolean isNotIgnored() {
		return !this.type.ignored;
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
		return new ValidationState(handle, Type.IGNORED_ON_RELEASE, element, null);
	}


	public static ValidationState createInReleaseStore(final Handle handle, final StoreElement element) {
		return new ValidationState(handle, Type.IN_RELEASE_STORE, element, null);
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
		IN_RELEASE_STORE(Action.RELEASE, false, true),
		BROKEN(Action.RELEASE, true, false),
		DISPLAY_ONLY(Action.RELEASE, false, true),
		INVALID_DATA(Action.RELEASE, true, false),
		MISSING_STARTNODE(Action.RELEASE, true, false),
		RELEASABLE(Action.RELEASE, false, false),
		DEPENDED_RELEASABLE(Action.RELEASE, false, false),
		OTHER(Action.RELEASE, true, false),
		UNRELEASED(Action.RELEASE, true, false),
		UNSUPPORTED_FOR_RELEASE(Action.RELEASE, false, true),
		IGNORED_ON_RELEASE(Action.RELEASE, false, true),

		IS_STILL_REFERENCED(Action.DELETE, true, false),
		IS_NOT_EMPTY(Action.DELETE, true, false),
		UNSUPPORTED_FOR_DELETE(Action.DELETE, true, false),
		DELETABLE(Action.DELETE, false, false),
		IGNORED_ON_DELETE(Action.DELETE, false, true);

		private Action  action;
		private boolean isPreventing;
		private boolean ignored;


		Type(final Action action, final boolean isPreventing, boolean ignored) {
			this.action = action;
			this.isPreventing = isPreventing;
			this.ignored = ignored;
		}


		public Action getAction() {
			return action;
		}


		public boolean isPreventing() {
			return isPreventing;
		}


		public static List<Type> getAllNonPreventingTypes(Action action) {
			return Arrays.stream(Type.values())
					.filter(t -> !t.isPreventing())
					.filter(t -> t.getAction().equals(action))
					.collect(Collectors.toList());
		}


		public static List<Type> getAllPreventingTypes(Action action) {
			return Arrays.stream(Type.values())
					.filter(Type::isPreventing)
					.filter(t -> t.getAction().equals(action))
					.collect(Collectors.toList());
		}


	}
}
