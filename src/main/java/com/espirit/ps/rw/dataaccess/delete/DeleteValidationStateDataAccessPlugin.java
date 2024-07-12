package com.espirit.ps.rw.dataaccess.delete;


import com.espirit.ps.rw.dataaccess.AbstractValidationStateDataAccessPlugin;
import com.espirit.ps.rw.dependency.Action;
import de.espirit.firstspirit.agency.OperationAgent;
import de.espirit.firstspirit.agency.SpecialistsBroker;
import de.espirit.firstspirit.ui.operations.ShowReportOperation;


public class DeleteValidationStateDataAccessPlugin extends AbstractValidationStateDataAccessPlugin {
	
	
	public DeleteValidationStateDataAccessPlugin() {
		super(Action.DELETE);
	}
	
	
	public static void showReport(final SpecialistsBroker broker) {
		ShowReportOperation operation = broker.requireSpecialist(OperationAgent.TYPE).getOperation(ShowReportOperation.TYPE);
		operation.perform(DeleteValidationStateDataAccessPlugin.class);
	}
}