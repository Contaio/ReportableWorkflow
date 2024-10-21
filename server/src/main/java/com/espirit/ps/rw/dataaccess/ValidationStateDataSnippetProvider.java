package com.espirit.ps.rw.dataaccess;

import com.espirit.ps.rw.common.ReportableWorkflowUtil;
import com.espirit.ps.rw.dependency.ValidationState;
import com.espirit.ps.rw.resources.Resources;
import de.espirit.common.base.Logging;
import de.espirit.firstspirit.access.BaseContext;
import de.espirit.firstspirit.access.Language;
import de.espirit.firstspirit.access.ReferenceEntry;
import de.espirit.firstspirit.access.store.IDProvider;
import de.espirit.firstspirit.agency.Image;
import de.espirit.firstspirit.agency.SnippetAgent;
import de.espirit.firstspirit.client.plugin.dataaccess.DataSnippetProvider;

import java.util.HashMap;

public class ValidationStateDataSnippetProvider implements DataSnippetProvider<ValidationState> {
	
	private final BaseContext                             context;
	private final ValidationStateDataAccessSession        session;
	private final HashMap<ValidationState.Type, String[]> stateLabelMessageMap;
	private final Image<?>                                checkImageIcon;
	private final Image<?>                                unreleasableImageIcon;
	private final Image<?>                                ignoreImageIcon;
	
	
	public ValidationStateDataSnippetProvider(final BaseContext context, final ValidationStateDataAccessSession session) {
		this.context = context;
		this.session = session;
		stateLabelMessageMap = new HashMap<>();
		checkImageIcon = Resources.getImageIcon(this.context, "check.png", getClass());
		unreleasableImageIcon = Resources.getImageIcon(this.context, "unreleasable.png", getClass());
		ignoreImageIcon = Resources.getImageIcon(this.context, "ignore.png", getClass());
		
		for (ValidationState.Type type : ValidationState.Type.values()) {
			String[] values = new String[2];
			values[Field.LABEL.index] = Resources.getLabel("validation.state." + type.name().toLowerCase() + ".label", getClass()).trim();
			values[Field.MESSAGE.index] = Resources.getLabel("validation.state." + type.name().toLowerCase() + ".message", getClass()).trim();
			stateLabelMessageMap.put(type, values);
		}
	}
	
	
	@Override
	public String getExtract(final ValidationState state, Language language) {
		if (language == null) {
			language = ReportableWorkflowUtil.getDisplayLanguage(context);
		}

		if (state.getType() == ValidationState.Type.BROKEN && state.getObject() instanceof ReferenceEntry) {
			return getMessage(state, null) + " \"" + state.getElement().getDisplayName(language) + "\"";
		} else if (state.getType() == ValidationState.Type.DISPLAY_ONLY) {
			try {
				return context.requireSpecialist(SnippetAgent.TYPE).getSnippetProvider(state.getElement()).getExtract(language);
			} catch (Exception ignore) {
				return getMessage(state, null);
			}
		} else {
			return getMessage(state, null);
		}
	}
	
	
	@Override
	public String getHeader(final ValidationState state, Language language) {
		String header = "";
		String indent = "";
		
		if (language == null) {
			language = ReportableWorkflowUtil.getDisplayLanguage(context);
		}

		if (state.getType() == ValidationState.Type.BROKEN && state.getObject() instanceof ReferenceEntry) {
			ReferenceEntry entry = (ReferenceEntry) state.getObject();
			if (entry.getStoreType() == null) {
				header = getLabel(state, null) + ": " + " \"" + entry.getReferenceString() + "\"" ;
			} else {
				header = getLabel(state, null) + ": " + " \"" + entry.getReferenceString() + "\" [" + entry.getStoreType().name() + "]";
			}
		} else if (state.getType() == ValidationState.Type.DISPLAY_ONLY) {
			try {
				header = context.requireSpecialist(SnippetAgent.TYPE).getSnippetProvider(state.getElement()).getHeader(language);
			} catch (Exception ignore) {
				header = state.getElement().getDisplayName(language);
			}
		} else if (state.getType() == ValidationState.Type.INVALID_DATA) {
			header = state.getEditorLabel() + " [" + state.getLanguage().getAbbreviation() + "]";
		} else {
			header = state.getElement().getDisplayName(language);
		}
		
		for (int i = 0; i < session.getCallerIndex(state) + 1; i++) {
			indent += ">";
		}
		
		if (indent.isEmpty()) {
			return header;
		} else {
			return indent + " " + header;
		}
	}
	
	
	@Override
	public Image<?> getIcon(final ValidationState state) {
		Language language = ReportableWorkflowUtil.getDisplayLanguage(context);
		Image<?> imageIcon = null;
		
		if (state.getType() == ValidationState.Type.INVALID_DATA) {
			return Resources.getImageIcon(context, "validation-error.png", getClass());
		} else if (state.getType() == ValidationState.Type.DISPLAY_ONLY) {
			if (session.getStreamBuilder().getManager().isActionable()) {
				imageIcon = checkImageIcon;
			} else {
				imageIcon = unreleasableImageIcon;
			}
		} else {
			if (ValidationState.Type.IGNORED_ON_RELEASE.equals(state.getType())) {
				imageIcon = ignoreImageIcon;
			} else if (state.isPreventing()) {
				imageIcon = unreleasableImageIcon;
			} else {
				imageIcon = checkImageIcon;
			}
		}
		
		return imageIcon;
	}
	
	
	private String getLabel(final ValidationState state, final String fallback) {
		String label = stateLabelMessageMap.get(state.getType())[Field.LABEL.index];
		
		if (label.isEmpty()) {
			if (fallback != null && !fallback.isEmpty()) {
				label = fallback;
			} else {
				label = state.getType().name().toLowerCase();
			}
		}
		
		return label;
	}
	
	
	private String getMessage(final ValidationState state, final String fallback) {
		String message = state.getMessage();
		
		if (message == null || message.isEmpty()) {
			switch (state.getType()) {
				case UNRELEASED:
				case UNSUPPORTED_FOR_RELEASE:
					message = stateLabelMessageMap.get(state.getType())[Field.MESSAGE.index] + ": " + state.getElement().getElementType();
					break;
				default:
					message = stateLabelMessageMap.get(state.getType())[Field.MESSAGE.index];
			}
		}
		
		if ((message == null || message.isEmpty()) && fallback != null) {
			message = fallback;
		}
		
		if (message == null || message.isEmpty()) {
			message = state.getType().name().toLowerCase();
		}
		
		return message;
	}
	
	
	@Override
	public Image<?> getThumbnail(final ValidationState state, final Language language) {
		try {
			return context.requireSpecialist(SnippetAgent.TYPE).getSnippetProvider(state.getElement()).getThumbnail(language);
		} catch (Exception ignore) {
			return null;
		}
	}
	
	
	private enum Field {
		LABEL(0),
		MESSAGE(1);
		
		private final int index;
		
		
		Field(final int index) {
			this.index = index;
		}
	}
}