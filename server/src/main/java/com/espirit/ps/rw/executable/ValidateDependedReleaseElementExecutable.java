package com.espirit.ps.rw.executable;


import com.espirit.moddev.components.annotations.PublicComponent;
import com.espirit.ps.rw.dependency.Action;
import com.espirit.ps.rw.workflow.ReportableWorkflow;
import de.espirit.firstspirit.access.BaseContext;
import de.espirit.firstspirit.access.store.StoreElement;

import java.util.Collections;
import java.util.List;

@PublicComponent(name = "reportable_workflow_validate_depended_release_element_executable", displayName = "ValidateDependedReleaseElementExecutable")
public class ValidateDependedReleaseElementExecutable extends AbstractExecutable {
	
	@Override
	public boolean execute(final BaseContext context, final StoreElement element) {
		return this.execute(context, Collections.singletonList(element));
	}
	
	
	@Override
	public boolean execute(final BaseContext context, final List<StoreElement> elements) {
		ReportableWorkflow.Builder builder = ReportableWorkflow.newBuilder();
		builder.withContext(context);
		builder.withElements(elements);
		builder.withOnlyValidate(true);
		builder.withDepended(true);
		builder.withAction(Action.RELEASE);
		
		ReportableWorkflow reportableWorkflow = builder.build();
		return reportableWorkflow.start();
	}
}