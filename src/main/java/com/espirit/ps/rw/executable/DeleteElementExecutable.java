package com.espirit.ps.rw.executable;


import com.espirit.ps.rw.dependency.Action;
import com.espirit.ps.rw.workflow.ReportableWorkflow;
import de.espirit.firstspirit.access.store.StoreElement;
import de.espirit.firstspirit.agency.SpecialistsBroker;

import java.util.Collections;
import java.util.List;

public class DeleteElementExecutable extends AbstractExecutable {
	
	@Override
	public boolean execute(final SpecialistsBroker broker, final StoreElement element) {
		return this.execute(broker, Collections.singletonList(element));
	}

	/**
	 * Execute method inherited by the FirstSpirit Executable Interface.
	 *
	 * @param broker   the calling context
	 * @param elements the elements on which the workflow (release oder delete) has been called
	 * @return
	 */
	@Override
	public boolean execute(final SpecialistsBroker broker, final List<StoreElement> elements) {
		ReportableWorkflow.Builder builder = ReportableWorkflow.newBuilder();
		builder.withContext(broker);
		builder.withElements(elements);
		builder.withAction(Action.DELETE);
		
		ReportableWorkflow reportableWorkflow = builder.build();
		return reportableWorkflow.start();
	}
}