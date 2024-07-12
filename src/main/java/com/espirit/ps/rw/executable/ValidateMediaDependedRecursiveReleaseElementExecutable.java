package com.espirit.ps.rw.executable;

import com.espirit.ps.rw.common.ReportableWorkflowUtil;
import com.espirit.ps.rw.dependency.Action;
import com.espirit.ps.rw.workflow.ReportableWorkflow;
import de.espirit.firstspirit.access.store.StoreElement;
import de.espirit.firstspirit.agency.SpecialistsBroker;

import java.util.Collections;
import java.util.List;

public class ValidateMediaDependedRecursiveReleaseElementExecutable extends AbstractExecutable {
	
	@Override
	public boolean execute(final SpecialistsBroker broker, final StoreElement element) {
		return execute(broker, Collections.singletonList(element));
	}
	
	
	@Override
	public boolean execute(final SpecialistsBroker broker, final List<StoreElement> elements) {
		ReportableWorkflow.Builder builder = ReportableWorkflow.newBuilder();
		builder.withContext(broker);
		builder.withDepended(false);
		builder.withJustDependentMedia(true);
		builder.withOnlyValidate(true);
		builder.withElements(ReportableWorkflowUtil.collectRecursiveElements(elements));
		builder.withAction(Action.RELEASE);
		
		ReportableWorkflow reportableWorkflow = builder.build();
		return reportableWorkflow.start();
	}
}