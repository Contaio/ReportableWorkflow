package com.espirit.ps.rw.dependency;


import com.espirit.ps.rw.common.AdvancedLogger;
import com.espirit.ps.rw.common.ReportableWorkflowUtil;
import com.espirit.ps.rw.resources.Resources;
import de.espirit.common.base.Logging;
import de.espirit.firstspirit.access.AdminService;
import de.espirit.firstspirit.access.BaseContext;
import de.espirit.firstspirit.access.Language;
import de.espirit.firstspirit.access.ModuleAgent;
import de.espirit.firstspirit.access.ServicesBroker;
import de.espirit.firstspirit.access.User;
import de.espirit.firstspirit.access.editor.value.DatasetContainer;
import de.espirit.firstspirit.access.store.IDProvider;
import de.espirit.firstspirit.access.store.Store;
import de.espirit.firstspirit.access.store.contentstore.Dataset;
import de.espirit.firstspirit.access.store.pagestore.DataProvider;
import de.espirit.firstspirit.access.store.sitestore.PageRef;
import de.espirit.firstspirit.access.store.templatestore.TemplateStoreRoot;
import de.espirit.firstspirit.access.store.templatestore.gom.GomFormElement;
import de.espirit.firstspirit.access.store.templatestore.gom.lists.GomIndex;
import de.espirit.firstspirit.agency.ClientUrlAgent;
import de.espirit.firstspirit.agency.LanguageAgent;
import de.espirit.firstspirit.agency.OperationAgent;
import de.espirit.firstspirit.agency.SpecialistsBroker;
import de.espirit.firstspirit.agency.StoreAgent;
import de.espirit.firstspirit.agency.UserAgent;
import de.espirit.firstspirit.client.access.editor.lists.Index;
import de.espirit.firstspirit.client.plugin.dataaccess.DataAccessSession;
import de.espirit.firstspirit.forms.FormData;
import de.espirit.firstspirit.module.descriptor.ComponentDescriptor;
import de.espirit.firstspirit.ui.operations.DisplayElementOperation;
import de.espirit.firstspirit.ui.operations.OpenElementDataFormOperation;
import de.espirit.firstspirit.ui.operations.OpenElementMetaFormOperation;
import de.espirit.firstspirit.ui.operations.RequestOperation;
import de.espirit.firstspirit.webedit.server.ClientScriptOperation;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Set;


public class DependencyUtil {



	public static Collection<ComponentDescriptor> getComponentsByType(final SpecialistsBroker broker, final Class<?> type) {
		List<ComponentDescriptor> instances   = new LinkedList<>();
		ModuleAgent               moduleAgent = broker.requireSpecialist(ModuleAgent.TYPE);

		return moduleAgent.getComponents(type);
	}


	public static Language getDisplayLanguage(final BaseContext context) {
		return ReportableWorkflowUtil.getDisplayLanguage(context);
	}


	public static <T> List<T> getInstancesByType(final SpecialistsBroker context, final Class<T> type) {
		List<T>     instances   = new LinkedList<>();
		ModuleAgent moduleAgent = context.requireSpecialist(ModuleAgent.TYPE);

		for (ComponentDescriptor component : moduleAgent.getComponents(type)) {
			Class<? extends T> clazz = null;

			try {
				AdvancedLogger.logInfo("Try to load class '" + component.getComponentClass() + "' form component '" + component.getName() + "'.", DependencyUtil.class);
				clazz = moduleAgent.getTypeForName(component.getName(), type);
				instances.add(clazz.getDeclaredConstructor().newInstance());
			} catch (Exception e) {
				if (clazz == null) {
					AdvancedLogger.logError("Unable to load definition of class '" + component.getComponentClass() + "'.", e, DependencyUtil.class);
				} else {
					AdvancedLogger.logError("Unable to instantiate '" + clazz + "'.", e, DependencyUtil.class);
				}
			}
		}

		return instances;
	}


	private static boolean hasUserPermissions(final SpecialistsBroker context, final IDProvider idProvider) {
		User user = context.requireSpecialist(UserAgent.TYPE).getUser();
		return idProvider.getPermission(user).canRead();
	}


	public static boolean isTemplateDeveloper(final BaseContext context) {
		User              user = context.requireSpecialist(UserAgent.TYPE).getUser();
		TemplateStoreRoot root = (TemplateStoreRoot) context.requireSpecialist(StoreAgent.TYPE).getStore(Store.Type.TEMPLATESTORE);
		return root.getPermission(user).canChange();
	}


	public static boolean jumpToElement(final BaseContext context, final ValidationState state) {
		IDProvider element    = state.getElement();
		Language   language   = state.getLanguage();
		String     editorName = state.getEditorName();

		if (element != null) {
			return jumpToElement(context, element, language, editorName);
		}

		return false;
	}


	public static boolean jumpToElement(final BaseContext context, final IDProvider element, @Nullable Language displayLanguage, @Nullable final String editorName) {
		Language metaLanguage  = context.requireSpecialist(LanguageAgent.TYPE).getMetaLanguage();
		boolean  isDisplayable = true;
		boolean  success       = false;

		if (displayLanguage == null) {
			displayLanguage = getDisplayLanguage(context);
		}

		if (metaLanguage.equals(displayLanguage)) {
			try {
				OpenElementMetaFormOperation dialogOperation = context.requireSpecialist(OperationAgent.TYPE).getOperation(OpenElementMetaFormOperation.TYPE);
				if (editorName != null) {
					dialogOperation.setField(editorName);
				}
				if (context.is(BaseContext.Env.WEBEDIT)) {
					dialogOperation.setOpenEditable(true);
				}
				dialogOperation.perform(element);
			} catch (Exception e) {
				isDisplayable = false;
			}
		} else if (element instanceof DataProvider) {
			try {
				OpenElementDataFormOperation dialogOperation = context.requireSpecialist(OperationAgent.TYPE).getOperation(OpenElementDataFormOperation.TYPE);
				if (editorName != null) {
					dialogOperation.setField(editorName);
				}
				if (context.is(BaseContext.Env.WEBEDIT)) {
					dialogOperation.setOpenEditable(true);
				}
				dialogOperation.setLanguage(displayLanguage);
				dialogOperation.perform((DataProvider) element);
			} catch (Exception e) {
				isDisplayable = false;
			}
		} else if (!context.is(BaseContext.Env.WEBEDIT) || element instanceof PageRef || element instanceof Dataset) {
			try {
				DisplayElementOperation displayOperation = context.requireSpecialist(OperationAgent.TYPE).getOperation(DisplayElementOperation.TYPE);
				displayOperation.setLanguage(displayLanguage);
				displayOperation.perform(element);
				success = true;
			} catch (Exception e) {
				isDisplayable = false;
			}
		} else {
			isDisplayable = false;
		}

		if (!isDisplayable && context.is(BaseContext.Env.WEBEDIT)) {
			RequestOperation requestOperation = context.requireSpecialist(OperationAgent.TYPE).getOperation(RequestOperation.TYPE);
			requestOperation.setKind(RequestOperation.Kind.QUESTION);

			ClientUrlAgent.JavaClientUrlBuilder javaClientUrlBuilder = context.requireSpecialist(ClientUrlAgent.TYPE).getBuilder(ClientUrlAgent.ClientType.JAVACLIENT);
			String                              javaClientUrl        = javaClientUrlBuilder.element(element).language(displayLanguage).createUrl();

			requestOperation.setTitle(Resources.getLabel("webedit.dialog.jump.to.element.title", DependencyUtil.class));
			requestOperation.addOk();

			RequestOperation.Answer openInJavaClient = requestOperation.addAnswer(Resources.getLabel("webedit.dialog.jump.to.element.button.open.in.javaclient", DependencyUtil.class));
			RequestOperation.Answer answer           = requestOperation.perform(Resources.getLabel("webedit.dialog.jump.to.element.message", DependencyUtil.class, javaClientUrl));

			if (answer.equals(openInJavaClient)) {
				String loginTicket = context.requireSpecialist(ServicesBroker.TYPE).getService(AdminService.class).getConnection().createTicket();
				javaClientUrl += "&login.ticket=" + loginTicket;

				ClientScriptOperation scriptOperation = context.requireSpecialist(OperationAgent.TYPE).getOperation(ClientScriptOperation.TYPE);
				scriptOperation.perform(String.format("(function(url){ window.open(url); return 1; })('%s')", javaClientUrl), false);
			}
		}

		return success;
	}


	public static List<Dataset> resolveRelations(final SpecialistsBroker context, final Dataset dataset) {

		if (hasUserPermissions(context, dataset)) {
			Set<Dataset>         relations      = new HashSet<>();
			FormData             formData       = dataset.getFormData();
			final List<Language> languages      = dataset.getProject().getLanguages();
			final Language       masterLanguage = dataset.getProject().getMasterLanguage();

			for (GomFormElement gomEl : formData.getForm().forms()) {
				for (Language language : gomEl.usesLanguages() ? languages : Collections.singletonList(masterLanguage)) {
					if (!gomEl.usesLanguages()) {
						if ("FS_DATASET".equals(gomEl.getGomElementTag()) && !formData.get(language, gomEl.name()).isEmpty()) {
							relations.add(((DatasetContainer) formData.get(language, gomEl.name()).get()).getDataset());
						} else if ("FS_INDEX".equals(gomEl.getGomElementTag()) && "DatasetDataAccessPlugin".equals(((GomIndex) gomEl).source().name()) && !formData.get(null, gomEl.name()).isEmpty()) {

							DataAccessSession<Dataset> session = ((GomIndex) gomEl).source().createSession(context, false);
							final Object               index   = formData.get(language, gomEl.name()).get();
							if (Objects.nonNull(index)) {
								for (Index.Record record : (Index) index) {
									try {
										relations.add(session.getData(record.getIdentifier()));
									} catch (NoSuchElementException e) {
										Logging.logWarning(String.format("User has not enough permission for dependent Dataset [%s]", record.getIdentifier()), DependencyUtil.class);
									}
								}
							}
						}    // TODO other GomForms (like FS_LIST)
					}
				}
			}
			return new ArrayList<>(relations);
		} else {
			return Collections.emptyList();
		}
	}
}
