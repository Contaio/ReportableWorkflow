package com.espirit.ps.rw.executable;


import com.espirit.ps.rw.client.ClientSession;
import com.espirit.ps.rw.common.AdvancedLogger;
import com.espirit.ps.rw.workflow.ReportableWorkflow;
import de.espirit.firstspirit.access.BaseContext;
import de.espirit.firstspirit.access.GuiScriptContext;
import de.espirit.firstspirit.access.script.Executable;
import de.espirit.firstspirit.access.store.IDProvider;
import de.espirit.firstspirit.access.store.StoreElement;
import de.espirit.firstspirit.access.store.templatestore.WorkflowScriptContext;
import de.espirit.firstspirit.webedit.WebeditUiAgent;
import de.espirit.firstspirit.workflow.model.Transition;

import java.io.Writer;
import java.util.List;
import java.util.Map;


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
		boolean success = execute(context, element);
		
		if (success) {
			advancedLogger.logInfo("Finished execution with success.");
		} else {
			advancedLogger.logInfo("Execution was NOT successfull.");
		}
		
		if (workflowScriptContext != null) {
			try {
				for (Transition transition : workflowScriptContext.getTransitions()) {
					if (success && transition.getUid().endsWith("_ok")) {
						ClientSession.removeItem(context, ReportableWorkflow.getIdentifier(element));
						workflowScriptContext.doTransition(transition);
						break;
					} else if (!success && transition.getUid().endsWith("_nok")) {
						workflowScriptContext.doTransition(transition);
						break;
					}
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
	
	
	abstract boolean execute(final BaseContext context, final StoreElement element);
	
	
	abstract boolean execute(final BaseContext context, final List<StoreElement> elements);
}
