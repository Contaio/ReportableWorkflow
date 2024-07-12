package com.espirit.ps.rw.executable;


import com.espirit.ps.rw.common.AdvancedLogger;
import com.espirit.ps.rw.common.ReportableWorkflowProjectApp;
import com.espirit.ps.rw.common.configuration.ProjectAppConfiguration;
import de.espirit.common.base.Logging;
import de.espirit.firstspirit.access.BaseContext;
import de.espirit.firstspirit.access.Connection;
import de.espirit.firstspirit.access.ConnectionManager;
import de.espirit.firstspirit.access.GuiScriptContext;
import de.espirit.firstspirit.access.User;
import de.espirit.firstspirit.access.script.Executable;
import de.espirit.firstspirit.access.store.IDProvider;
import de.espirit.firstspirit.access.store.StoreElement;
import de.espirit.firstspirit.access.store.templatestore.WorkflowScriptContext;
import de.espirit.firstspirit.agency.BrokerAgent;
import de.espirit.firstspirit.agency.SpecialistsBroker;
import de.espirit.firstspirit.common.MaximumNumberOfSessionsExceededException;
import de.espirit.firstspirit.server.authentication.AuthenticationException;
import de.espirit.firstspirit.webedit.WebeditUiAgent;
import de.espirit.firstspirit.workflow.model.Transition;

import java.io.IOException;
import java.io.Writer;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;


public abstract class AbstractExecutable implements Executable {

	protected BaseContext           context;
	protected StoreElement          element;
	protected WorkflowScriptContext workflowScriptContext;
	protected AdvancedLogger        advancedLogger;


	@Override
	public Object execute(Map<String, Object> parameter) {
		context = (BaseContext) parameter.get("context");
		workflowScriptContext = null;
		element = null;
		advancedLogger = new AdvancedLogger(context, getClass());

		if (parameter.get("context") instanceof WorkflowScriptContext) {
			workflowScriptContext = (WorkflowScriptContext) parameter.get("context");
			element = workflowScriptContext.getElement();
		} else if (parameter.get("context") instanceof GuiScriptContext) {
			element = ((GuiScriptContext) parameter.get("context")).getElement();
		} else if (parameter.containsKey("element") && parameter.get("element") instanceof StoreElement) {
			element = (IDProvider) parameter.get("element");
		} else if (context.is(BaseContext.Env.WEBEDIT)) {
			element = context.requireSpecialist(WebeditUiAgent.TYPE).getPreviewElement();
		}


		advancedLogger.logInfo("Start execution on element.");
		boolean success = false;
		if (element.getProject().getUserService().getUser().getLoginName().equalsIgnoreCase("SYSTEM")) {
			final ProjectAppConfiguration configuration = ReportableWorkflowProjectApp.getConfiguration(context);

			if (Objects.nonNull(configuration.getUsername()) && Objects.nonNull(configuration.getPassword())) {
				final Connection connection    = element.getProject().getUserService().getConnection();
				final Connection newConnection = ConnectionManager.getConnection(connection.getHost(), connection.getPort(), connection.getMode(), configuration.getUsername(), configuration.getPassword());
				try {
					newConnection.connect();
					final User        user   = newConnection.getUser();
					SpecialistsBroker broker = newConnection.getBroker().requireSpecialist(BrokerAgent.TYPE).getBrokerByProjectId(element.getProject().getId());
					success = execute(broker, element);
					newConnection.disconnect();
					newConnection.close();

				} catch (IOException e) {
					throw new RuntimeException(e);
				} catch (AuthenticationException e) {
					throw new RuntimeException(e);
				} catch (MaximumNumberOfSessionsExceededException e) {
					throw new RuntimeException(e);
				}

			}

		} else {
			success = execute(workflowScriptContext, element);
		}

		if (success) {
			Logging.logInfo("Finished execution with success.", AbstractExecutable.class);
		} else {
			Logging.logInfo("Execution was NOT successfully.", AbstractExecutable.class);
		}

		String transitionSuffix = success ? "_ok" : "_nok";
		if (workflowScriptContext != null) {
			try {
				Logging.logInfo("Reportable Workflow return code:  " + success, AbstractExecutable.class);
				final Optional<Transition> transition = Arrays.stream(workflowScriptContext.getTransitions())
						.filter(t -> t.getUid().endsWith(transitionSuffix))
						.findFirst();
				if (transition.isPresent()) {
					try {
						workflowScriptContext.doTransition(transition.get());
					} catch (IllegalAccessException e) {
						throw new RuntimeException(e);
					}
				} else {
					throw new IllegalArgumentException("Transition is not present!");
				}

			} catch (Exception e) {
				AdvancedLogger.logError(e.getMessage(), e, AbstractExecutable.class);
			}
		}

		return success;
	}


	@Override
	public Object execute(final Map<String, Object> map, final Writer writer, final Writer writer1) {
		return execute(map);
	}


	abstract boolean execute(final SpecialistsBroker broker, final StoreElement element);


	abstract boolean execute(final SpecialistsBroker broker, final List<StoreElement> elements);
}
