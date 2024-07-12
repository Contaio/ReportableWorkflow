package com.espirit.ps.rw.dependency.handler;

import com.espirit.ps.rw.dependency.AbstractDefaultHandler;
import com.espirit.ps.rw.dependency.Handle;
import com.espirit.ps.rw.dependency.Manager;
import com.espirit.ps.rw.dependency.ValidationState;
import de.espirit.firstspirit.access.store.contentstore.Dataset;

public final class DefaultDataSetHandler extends AbstractDefaultHandler {

	public DefaultDataSetHandler() {
		super(Dataset.class);
	}


	@Override
	public void execute(final Handle handle, final Manager manager) {
		if (handle.getKeyObject() instanceof Dataset) {
			Dataset dataset = (Dataset) handle.getKeyObject();

			switch (manager.getAction()) {
				case RELEASE:
					super.validateRelease(handle, manager);
					super.validateFormData(handle, manager);
					super.checkOutgoingReferences(handle, manager);
					super.addRelatedDatasets(handle, manager);
					break;
				case DELETE:
					if (super.checkIncomingReferences(handle, manager)) {
						handle.addValidationState(ValidationState.createDeletable(handle, dataset));
					}
					break;
			}
		}
	}
}
