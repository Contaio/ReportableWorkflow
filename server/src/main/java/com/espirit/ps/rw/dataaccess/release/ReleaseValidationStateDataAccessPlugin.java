package com.espirit.ps.rw.dataaccess.release;


import com.espirit.moddev.components.annotations.PublicComponent;
import com.espirit.ps.rw.dataaccess.AbstractValidationStateDataAccessPlugin;
import com.espirit.ps.rw.dependency.Action;
import de.espirit.firstspirit.agency.OperationAgent;
import de.espirit.firstspirit.agency.SpecialistsBroker;
import de.espirit.firstspirit.ui.operations.ShowReportOperation;

@PublicComponent(name = "reportable_workflow_release_validation_state_data_access_plugin", displayName = "ReleaseValidationStateDAP")
public class ReleaseValidationStateDataAccessPlugin extends AbstractValidationStateDataAccessPlugin {
	
	
	public ReleaseValidationStateDataAccessPlugin() {
		super(Action.RELEASE);
	}
	
	
	public static void showReport(final SpecialistsBroker broker) {
		ShowReportOperation operation = broker.requireSpecialist(OperationAgent.TYPE).getOperation(ShowReportOperation.TYPE);
		operation.perform(ReleaseValidationStateDataAccessPlugin.class);
	}
}