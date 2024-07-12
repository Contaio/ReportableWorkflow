package com.espirit.ps.rw.dependency;


import de.espirit.common.tools.Strings;
import de.espirit.firstspirit.access.BaseContext;
import de.espirit.firstspirit.access.Language;
import de.espirit.firstspirit.access.store.IDProvider;
import de.espirit.firstspirit.access.store.pagestore.DataProvider;
import de.espirit.firstspirit.access.store.templatestore.gom.GomFormElement;
import de.espirit.firstspirit.agency.FormValidationReport;
import de.espirit.firstspirit.agency.MultiFormValidationReport;
import de.espirit.firstspirit.agency.SpecialistsBroker;
import de.espirit.firstspirit.agency.ValidationAgent;
import de.espirit.firstspirit.forms.FormData;

import java.util.LinkedList;
import java.util.List;


public class ValidationUtil {
	
	public static ValidationContext createContext(final SpecialistsBroker broker) {
		return new ValidationContext(broker);
	}
	
	
	private static List<ValidationState> handleFormValidationReport(final ValidationContext context, final IDProvider element, final Language language, final FormValidationReport report, final Handle handle) {
		List<ValidationState> invalidDataList = new LinkedList<>();
		
		if (report != null) {
			for (String gadget : report.getGadgets()) {
				String         editorName  = null;
				String         editorLabel = null;
				FormData       formData    = language.equals(context.getMetaLanguage()) ? element.getMetaFormData() : ((DataProvider) element).getFormData();
				GomFormElement editor      = formData.getForm().findEditor(gadget);
				
				if (editor != null) {
					editorLabel = editor.label(context.getDisplayLanguage().getAbbreviation());
				}
				if (Strings.isEmpty(editorLabel)) {
					editorLabel = "[" + gadget + "]";
				} else {
					editorName = gadget;
				}
				
				for (String message : report.getMessages(gadget, context.getDisplayLanguage())) {
					if (message.startsWith("[")) {
						message = message.substring(1);
					}
					if (message.endsWith("]")) {
						message = message.substring(0, message.length() - 2);
					}
					
					invalidDataList.add(ValidationState.createInvalidData(handle, element, editorName, editorLabel, language, message));
				}
			}
		}
		
		return invalidDataList;
	}


	/**
	 * This Method validates against the FormData Ruleset.
	 * @param context
	 * @param handle
	 * @return
	 */
	public static List<ValidationState> validateFormData(final ValidationContext context, final Handle handle) {
		List<ValidationState> validationStates = new LinkedList<>();
		
		if (handle.getKeyObject() instanceof DataProvider || (handle.getKeyObject() instanceof IDProvider && ((IDProvider) handle.getKeyObject()).hasMeta())) {
			IDProvider element = (IDProvider) handle.getKeyObject();
			
			for (ValidationAgent.ValidationScope validationScope : context.getValidationScopes()) {
				MultiFormValidationReport validationReport = context.getValidationAgent().validate(element, validationScope);
				if (!validationReport.isValid()) {
					
					if (validationReport.getProblemsForMetaData() != null) {
						validationStates.addAll(handleFormValidationReport(context, element, context.getMetaLanguage(), validationReport.getProblemsForMetaData(), handle));
					}
					
					if (element instanceof DataProvider) {
						for (Language language : validationReport.getLanguages(context)) {
							validationStates.addAll(handleFormValidationReport(context, element, language, validationReport.getProblems(language), handle));
						}
					}
					
				}
			}
		}
		
		return validationStates;
	}
}