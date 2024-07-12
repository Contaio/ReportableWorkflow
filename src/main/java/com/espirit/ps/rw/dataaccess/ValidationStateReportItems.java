package com.espirit.ps.rw.dataaccess;

import com.espirit.ps.rw.client.ClientSession;
import com.espirit.ps.rw.common.AdvancedLogger;
import com.espirit.ps.rw.common.ReportableWorkflowUtil;
import com.espirit.ps.rw.dataaccess.delete.DeleteValidationStateDataAccessPlugin;
import com.espirit.ps.rw.dataaccess.release.ReleaseValidationStateDataAccessPlugin;
import com.espirit.ps.rw.dependency.Action;
import com.espirit.ps.rw.dependency.DependencyUtil;
import com.espirit.ps.rw.dependency.Manager;
import com.espirit.ps.rw.dependency.ValidationState;
import com.espirit.ps.rw.resources.Resources;
import com.espirit.ps.rw.workflow.ReportableWorkflow;
import de.espirit.firstspirit.access.BaseContext;
import de.espirit.firstspirit.access.store.StoreElement;
import de.espirit.firstspirit.agency.OperationAgent;
import de.espirit.firstspirit.client.plugin.dataaccess.aspects.ReportItemsProviding;
import de.espirit.firstspirit.client.plugin.report.JavaClientExecutableReportItem;
import de.espirit.firstspirit.client.plugin.report.ReportContext;
import de.espirit.firstspirit.client.plugin.report.ReportItem;
import de.espirit.firstspirit.ui.operations.RequestOperation;
import de.espirit.firstspirit.webedit.plugin.report.WebeditExecutableReportItem;

import javax.swing.*;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public class ValidationStateReportItems implements ReportItemsProviding<ValidationState> {

	private final AbstractValidationStateDataAccessPlugin plugin;
	private final List<ReportItem<ValidationState>>       items;


	public ValidationStateReportItems(final BaseContext context, final AbstractValidationStateDataAccessPlugin plugin) {
		this.plugin = plugin;
		items = new LinkedList<>();

		if (this.plugin.getAction().equals(Action.RELEASE)) {
			items.add(new IgnoreItem());
		}

		if (DependencyUtil.isTemplateDeveloper(context)) {
			items.add(new ShowElementReferenceChain());
		}
	}


	@Override
	public ReportItem<ValidationState> getClickItem() {
		return new HighlightElement();
	}


	@Override
	public Collection<? extends ReportItem<ValidationState>> getItems() {
		return items;
	}


	private void showReport(final ReportContext<ValidationState> context) {
		if (Action.RELEASE.equals(plugin.getAction())) {
			ReleaseValidationStateDataAccessPlugin.showReport(context);
		} else {
			DeleteValidationStateDataAccessPlugin.showReport(context);
		}
	}


	public class HighlightElement implements JavaClientExecutableReportItem<ValidationState>, WebeditExecutableReportItem<ValidationState> {

		@Override
		public void execute(final ReportContext<ValidationState> context) {
			if (DependencyUtil.jumpToElement(context, context.getObject())) {
				plugin.getSessionBuilder().getSession().restoreCallerChain();
				plugin.getSessionBuilder().getSession().addOrRestoreCaller(context.getObject());

				ClientSession.setItem(context, ValidationStateDataStreamBuilder.CLIENT_SESSION_KEY_MANAGER, plugin.getSessionBuilder().getSession().getStreamBuilder().getManager());

				showReport(context);
			}
		}


		@Override
		public Icon getIcon(final ReportContext<ValidationState> context) {
			return null;
		}


		@Override
		public String getIconPath(final ReportContext<ValidationState> context) {
			return null;
		}


		@Override
		public String getLabel(final ReportContext<ValidationState> context) {
			return "";
		}


		@Override
		public boolean isEnabled(final ReportContext<ValidationState> context) {
			return true;
		}


		@Override
		public boolean isVisible(final ReportContext<ValidationState> context) {
			return true;
		}
	}


	public class IgnoreItem implements JavaClientExecutableReportItem<ValidationState>, WebeditExecutableReportItem<ValidationState> {

		private boolean checkType(final ReportContext<ValidationState> context) {
			return true;
		}


		@Override
		public void execute(final ReportContext<ValidationState> context) {
			Manager           manager   = plugin.getSessionBuilder().getSession().getStreamBuilder().getManager();
			Set<StoreElement> ignoreSet = ReportableWorkflowUtil.buildIgnoreSet(manager);


			if (context.getObject().getObject() instanceof StoreElement) {
				StoreElement storeElement = (StoreElement) context.getObject().getObject();
				switch (context.getObject().getType()) {
					case UNRELEASED:
						ignoreSet.add(storeElement);
						break;
					case IGNORED_ON_DELETE:
					case IGNORED_ON_RELEASE:
						ignoreSet.remove(storeElement);
						break;
					default:
						// Nothing to do
				}
				for (final String startStoreIdentifier : ReportableWorkflow.getIdentifiers(manager.getStartStoreElements())) {
					ClientSession.setItem(context, startStoreIdentifier, ignoreSet);
				}
				showReport(context);
			}
		}


		@Override
		public Icon getIcon(final ReportContext<ValidationState> context) {
			switch (context.getObject().getType()) {
				case UNRELEASED:
					return Resources.getIcon("ignore.png", getClass());
				case IGNORED_ON_DELETE:
				case IGNORED_ON_RELEASE:
					return Resources.getIcon("unreleasable.png", getClass());
				default:
					return null;
			}
		}


		@Override
		public String getIconPath(final ReportContext<ValidationState> context) {
			switch (context.getObject().getType()) {
				case UNRELEASED:
					return Resources.getIconPath("ignore.png", getClass());
				case IGNORED_ON_DELETE:
				case IGNORED_ON_RELEASE:
					return Resources.getIconPath("unreleasable.png", getClass());
				default:
					return null;
			}
		}


		@Override
		public String getLabel(final ReportContext<ValidationState> context) {
			return Resources.getLabel("data.access.report.item.ignore", getClass());
		}


		@Override
		public boolean isEnabled(final ReportContext<ValidationState> context) {
			return isVisible(context);
		}


		@Override
		public boolean isVisible(final ReportContext<ValidationState> context) {
			Manager manager = plugin.getSessionBuilder().getSession().getStreamBuilder().getManager();
			if (manager == null || manager.getStartStoreElements() == null) {
				AdvancedLogger.logInfo(String.format("Ignore manager is null: %s. Start store elemente are null: %s", manager == null, (manager != null && manager.getStartStoreElements() == null)), getClass());
				return false;
			}
			List<String> identifiers = ReportableWorkflow.getIdentifiers(manager.getStartStoreElements());

			boolean isStoreElement = context.getObject().getObject() instanceof StoreElement;
			boolean isWorkflow     = false;
			for (String identifier : identifiers) {
				isWorkflow |= ClientSession.getItem(context, identifier, null) != null;
			}
			boolean isActionType;

			switch (context.getObject().getType()) {
				case UNRELEASED:
				case IGNORED_ON_DELETE:
				case IGNORED_ON_RELEASE:
					isActionType = true;
					break;
				default:
					isActionType = false;
			}

			return isActionType && isStoreElement && isWorkflow;
		}
	}


	public class ShowElementReferenceChain implements JavaClientExecutableReportItem<ValidationState>, WebeditExecutableReportItem<ValidationState> {

		@Override
		public void execute(final ReportContext<ValidationState> context) {
			String message = context.getObject().getReferenceChainString();

			if (message.isEmpty()) {
				message = Resources.getLabel("data.access.report.item.reference_chain.empty", getClass());
			}

			context.requireSpecialist(OperationAgent.TYPE).getOperation(RequestOperation.TYPE).perform(message);
		}


		@Override
		public Icon getIcon(final ReportContext<ValidationState> context) {
			return Resources.getIcon("chain.png", getClass());
		}


		@Override
		public String getIconPath(final ReportContext<ValidationState> context) {
			return Resources.getIconPath("chain.png", getClass());
		}


		@Override
		public String getLabel(final ReportContext<ValidationState> context) {
			return Resources.getLabel("data.access.report.item.reference_chain.show", getClass());
		}


		@Override
		public boolean isEnabled(final ReportContext<ValidationState> context) {
			return true;
		}


		@Override
		public boolean isVisible(final ReportContext<ValidationState> context) {
			return true;
		}
	}
}
