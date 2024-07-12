package com.espirit.ps.rw.dependency;

import de.espirit.firstspirit.access.BaseContext;
import de.espirit.firstspirit.access.Language;
import de.espirit.firstspirit.agency.LanguageAgent;
import de.espirit.firstspirit.agency.SpecialistType;
import de.espirit.firstspirit.agency.SpecialistsBroker;
import de.espirit.firstspirit.agency.UIAgent;
import de.espirit.firstspirit.agency.ValidationAgent;
import de.espirit.firstspirit.webedit.WebeditUiAgent;

import java.util.Arrays;
import java.util.List;

public final class ValidationContext implements SpecialistsBroker {

	private final SpecialistsBroker                     context;
	private final Language                              metaLanguage;
	private final List<ValidationAgent.ValidationScope> validationScopes;
	private final ValidationAgent                       validationAgent;
	private       Language                              displayLanguage;


	public ValidationContext(final SpecialistsBroker broker) {
		this.context = broker;

		LanguageAgent languageAgent = this.context.requireSpecialist(LanguageAgent.TYPE);
		metaLanguage = languageAgent.getMetaLanguage();
		validationScopes = Arrays.asList(ValidationAgent.ValidationScope.SAVE, ValidationAgent.ValidationScope.RELEASE);
		validationAgent = this.context.requireSpecialist(ValidationAgent.TYPE);

		if (context instanceof BaseContext && ((BaseContext) context).is(BaseContext.Env.WEBEDIT)) {
			WebeditUiAgent webeditUiAgent = context.requestSpecialist(WebeditUiAgent.TYPE);
			if (webeditUiAgent != null && webeditUiAgent.getDisplayLanguage() != null) {
				displayLanguage = webeditUiAgent.getDisplayLanguage();
			}
		} else if (context instanceof BaseContext && ((BaseContext) context).is(BaseContext.Env.ARCHITECT)) {
			UIAgent uiAgent = context.requestSpecialist(UIAgent.TYPE);
			if (uiAgent != null && uiAgent.getDisplayLanguage() != null) {
				displayLanguage = uiAgent.getDisplayLanguage();
			}
		}

		if (displayLanguage == null) {
			displayLanguage = languageAgent.getMasterLanguage();
		}
	}


	public Language getDisplayLanguage() {
		return displayLanguage;
	}


	public Language getMetaLanguage() {
		return metaLanguage;
	}


	public ValidationAgent getValidationAgent() {
		return validationAgent;
	}


	public List<ValidationAgent.ValidationScope> getValidationScopes() {
		return validationScopes;
	}


	@Override
	public <S> S requestSpecialist(final SpecialistType<S> specialistType) {
		return context.requestSpecialist(specialistType);
	}


	@Override
	public <S> S requireSpecialist(final SpecialistType<S> specialistType) throws IllegalStateException {
		return context.requireSpecialist(specialistType);
	}
}
