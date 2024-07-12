package com.espirit.ps.rw.dataaccess;

import com.espirit.ps.rw.dependency.DependencyUtil;
import com.espirit.ps.rw.dependency.ValidationState;
import com.espirit.ps.rw.resources.Resources;
import de.espirit.firstspirit.access.*;
import de.espirit.firstspirit.access.store.IDProvider;
import de.espirit.firstspirit.access.store.Store;
import de.espirit.firstspirit.access.store.contentstore.Dataset;
import de.espirit.firstspirit.access.store.pagestore.DataProvider;
import de.espirit.firstspirit.access.store.sitestore.PageRef;
import de.espirit.firstspirit.access.store.templatestore.TemplateStoreRoot;
import de.espirit.firstspirit.agency.*;
import de.espirit.firstspirit.ui.operations.DisplayElementOperation;
import de.espirit.firstspirit.ui.operations.OpenElementDataFormOperation;
import de.espirit.firstspirit.ui.operations.OpenElementMetaFormOperation;
import de.espirit.firstspirit.ui.operations.RequestOperation;
import de.espirit.firstspirit.webedit.server.ClientScriptOperation;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class DataAccessUtil {
	public static boolean isTemplateDeveloper(final BaseContext context) {
		User              user = context.requireSpecialist(UserAgent.TYPE).getUser();
		TemplateStoreRoot root = (TemplateStoreRoot) context.requireSpecialist(StoreAgent.TYPE).getStore(Store.Type.TEMPLATESTORE);
		return root.getPermission(user).canChangePermission();
	}
	
	
	public static boolean jumpToElement(final BaseContext context, final IDProvider element, @Nullable Language displayLanguage, @Nullable final String editorName) {
		Language metaLanguage  = context.requireSpecialist(LanguageAgent.TYPE).getMetaLanguage();
		boolean  isDisplayable = true;
		boolean  success       = false;
		
		if (displayLanguage == null) {
			displayLanguage = DependencyUtil.getDisplayLanguage(context);
		}
		
		if (metaLanguage.equals(displayLanguage)) {
			try {
				OpenElementMetaFormOperation dialogOperation = context.requireSpecialist(OperationAgent.TYPE).getOperation(OpenElementMetaFormOperation.TYPE);
				if (Objects.nonNull(editorName)) {
					dialogOperation.setField(editorName);
				}
				dialogOperation.setOpenEditable(context.is(BaseContext.Env.WEBEDIT));
				dialogOperation.perform(element);
			} catch (Exception e) {
				isDisplayable = false;
				context.logError("Error: " + e.getMessage());
			}
		} else if (element instanceof DataProvider) {
			try {
				OpenElementDataFormOperation dialogOperation = context.requireSpecialist(OperationAgent.TYPE).getOperation(OpenElementDataFormOperation.TYPE);
				if (Objects.nonNull(editorName)) {
					dialogOperation.setField(editorName);
				}
				dialogOperation.setOpenEditable(context.is(BaseContext.Env.WEBEDIT));
				dialogOperation.setLanguage(displayLanguage);
				dialogOperation.perform((DataProvider) element);
			} catch (Exception e) {
				isDisplayable = false;
				context.logError("Error: " + e.getMessage());
			}
		} else {
			try {
				DisplayElementOperation displayOperation = context.requireSpecialist(OperationAgent.TYPE).getOperation(DisplayElementOperation.TYPE);
				displayOperation.setLanguage(displayLanguage);
				displayOperation.perform(element);
				success = true;
			} catch (Exception e) {
				isDisplayable = false;
				context.logError("Error: " + e.getMessage());
			}
		}


		fallbackOpenInJavaClient(context, element, displayLanguage, isDisplayable);

		return success;
	}


	private static void fallbackOpenInJavaClient(final BaseContext context, final IDProvider element, final @Nullable Language displayLanguage, final boolean isDisplayable) {
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
	}


	/**
	 * Navigates inside the client to the specified element.
	 * For elements which are not displayable in the Web-Client a redirection to the Java-Client is performed.
	 * @param context the current context
	 * @param state the validation state that needs to be shown
	 * @return success state
	 */
	public static boolean jumpToElement(final BaseContext context, final ValidationState state) {
		IDProvider element    = state.getElement();
		Language   language   = state.getLanguage();
		String     editorName = state.getEditorName();
		
		if (element != null) {
			return jumpToElement(context, element, language, editorName);
		}
		
		return false;
	}
}
