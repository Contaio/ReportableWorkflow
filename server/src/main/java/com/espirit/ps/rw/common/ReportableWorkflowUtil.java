package com.espirit.ps.rw.common;


import com.espirit.ps.rw.client.ClientSession;
import com.espirit.ps.rw.dependency.Manager;
import com.espirit.ps.rw.workflow.ReportableWorkflow;
import de.espirit.firstspirit.access.BaseContext;
import de.espirit.firstspirit.access.Language;
import de.espirit.firstspirit.access.store.IDProvider;
import de.espirit.firstspirit.access.store.StoreElement;
import de.espirit.firstspirit.agency.LanguageAgent;
import de.espirit.firstspirit.agency.UIAgent;
import de.espirit.firstspirit.webedit.WebeditUiAgent;

import java.util.*;


public class ReportableWorkflowUtil {

	private ReportableWorkflowUtil() {
		// Just to hide
	}


	/**
	 * Build ignore set based on given manager instance.
	 *
	 * @param manager
	 * @return Set of store elements that are given in Manager's startStoreElements.
	 */
	@SuppressWarnings("unchecked")
	public static Set<StoreElement> buildIgnoreSet(final Manager manager) {
		List<String> identifiers = ReportableWorkflow.getIdentifiers(manager.getStartStoreElements());
		
		Set<StoreElement> ignoreSet = new HashSet<>();
		for (String identifier : identifiers) {
			Set<StoreElement> result = (Set<StoreElement>) ClientSession.getItem(manager.getContext(), identifier, new HashSet<>());
			ignoreSet.addAll(result);
		}
		
		return ignoreSet;
	}
	
	
	/**
	 * Collect all child elements for given list of store elements.
	 *
	 * @param storeElements List of store elements.
	 * @return Given store elements and their children (and grandchildren, ...) in flatten list.
	 */
	public static List<StoreElement> collectRecursiveElements(Iterable<? extends StoreElement> storeElements) {
		List<StoreElement> results = new LinkedList<>();
		
		for (StoreElement storeElement : storeElements) {
			if (storeElement.isFolder()) {
				results.add(storeElement);
				results.addAll(collectRecursiveElements(storeElement.getChildren(IDProvider.class, false)));
			} else {
				results.add(storeElement);
			}
		}
		
		return results;
	}
	
	
	/**
	 * Collect all child elements for given list of store elements.
	 *
	 * @param storeElement store elements.
	 * @return Given store elements and their children (and grandchildren, ...) in flatten list.
	 */
	public static List<StoreElement> collectRecursiveElements(StoreElement storeElement) {
		return collectRecursiveElements(Collections.singletonList(storeElement));
	}
	
	
	public static Language getDisplayLanguage(final BaseContext context) {
		if (context.is(BaseContext.Env.WEBEDIT)) {
			WebeditUiAgent webeditUiAgent = context.requestSpecialist(WebeditUiAgent.TYPE);
			if (webeditUiAgent != null && webeditUiAgent.getDisplayLanguage() != null) {
				return webeditUiAgent.getDisplayLanguage();
			}
		} else {
			UIAgent uiAgent = context.requestSpecialist(UIAgent.TYPE);
			if (uiAgent != null && uiAgent.getDisplayLanguage() != null) {
				return uiAgent.getDisplayLanguage();
			}
		}
		
		return context.requireSpecialist(LanguageAgent.TYPE).getMasterLanguage();
	}
	
	
	public static String getDisplayName(final BaseContext context, final IDProvider element) {
		return element.getDisplayName(getDisplayLanguage(context));
	}
	
	
	public static String getIdentifier(final StoreElement element, final Class<?> c) {
		String identifier = c.getCanonicalName() + "@";
		identifier += element.getElementType() + "$";
		
		if (element instanceof IDProvider) {
			if (((IDProvider) element).hasUid()) {
				identifier += ((IDProvider) element).getUid();
			} else {
				identifier += element.getName() + "#" + ((IDProvider) element).getId();
			}
		} else {
			identifier += element.getName();
		}
		
		return identifier;
	}
}
