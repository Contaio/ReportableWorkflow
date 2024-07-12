package com.espirit.ps.rw.dependency;

import de.espirit.firstspirit.access.BaseContext;
import de.espirit.firstspirit.access.Language;
import de.espirit.firstspirit.agency.*;
import de.espirit.firstspirit.webedit.WebeditUiAgent;

import java.util.Arrays;
import java.util.List;

public final class ValidationContext implements SpecialistsBroker {
	
	private final BaseContext                           context;
	private final Language                              metaLanguage;
	private final List<ValidationAgent.ValidationScope> validationScopes;
	private final ValidationAgent                       validationAgent;
	private       Language                              displayLanguage;
	
	
	public ValidationContext(final BaseContext context) {
		this.context = context;
		
		LanguageAgent languageAgent = this.context.requireSpecialist(LanguageAgent.TYPE);
		metaLanguage = languageAgent.getMetaLanguage();
		validationScopes = Arrays.asList(ValidationAgent.ValidationScope.SAVE, ValidationAgent.ValidationScope.RELEASE);
		validationAgent = this.context.requireSpecialist(ValidationAgent.TYPE);
		
		if (context.is(BaseContext.Env.WEBEDIT)) {
			WebeditUiAgent webeditUiAgent = context.requestSpecialist(WebeditUiAgent.TYPE);
			if (webeditUiAgent != null && webeditUiAgent.getDisplayLanguage() != null) {
				displayLanguage = webeditUiAgent.getDisplayLanguage();
			}
		} else {
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
