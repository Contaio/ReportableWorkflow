package com.espirit.ps.rw.dependency;

import com.espirit.ps.rw.client.ClientSession;
import de.espirit.firstspirit.access.BaseContext;
import de.espirit.firstspirit.access.ServicesBroker;
import de.espirit.firstspirit.access.store.StoreElement;
import de.espirit.firstspirit.access.store.contentstore.Dataset;
import de.espirit.firstspirit.agency.SpecialistsBroker;

import java.util.Set;

public abstract class AbstractDefaultHandler implements Handler {

	protected Class<?> elementType;
	//protected boolean  clientContextAvailable;


	public AbstractDefaultHandler(final Class<?> elementType) {
		this.elementType = elementType;
	}


	protected final void addRelatedDatasets(final Handle handle, final Manager manager) {
		if (Action.RELEASE.equals(manager.getAction()) && handle.getKeyObject() instanceof Dataset) {
			for (Dataset relatedDataset : DependencyUtil.resolveRelations(manager.getContext(), (Dataset) handle.getKeyObject())) {
				handle.addNextHandleObject(new ResolveObject(relatedDataset, handle));
			}
		}
	}


	/**
	 * This method checks whether the incoming references for the given handle are problematic by iterating
	 * over all incoming references and examing whether the handle is the original to be deleted element (startStoreElement).
	 *
	 * @param handle  the element to be validated
	 * @param manager the manager controlling the validation process
	 * @return result of the validation check for incoming references
	 */
	@SuppressWarnings("unchecked")
	protected final boolean checkIncomingReferences(final Handle handle, final Manager manager) {
		return ReferenceChecker.checkIncomingReferences(handle, manager);
	}


	protected final Set<StoreElement> checkOutgoingReferences(final Handle handle, final Manager manager) {
		return ReferenceChecker.checkOutgoingReferences(handle, manager);
	}


	@Override
	public void execute(final Handle handle, final Manager manager) {
		if (Action.RELEASE.equals(manager.getAction())) {
			validateRelease(handle, manager);
			validateFormData(handle, manager);
		}

		checkOutgoingReferences(handle, manager);
	}


	public final Class<?> getElementType() {
		return elementType;
	}


	protected final boolean isClientContextAvailable(final Manager manager) {
		SpecialistsBroker context = manager.getContext();

		try {
			ClientSession clientSession = context.requireSpecialist(ServicesBroker.TYPE).getService(ClientSession.class);
			return clientSession != null;
		} catch (Exception e) {
			return false;
		}
	}


	protected final void validateFormData(final Handle handle, final Manager manager) {
		handle.addValidationStates(manager.validateFormData(handle));
	}


	protected final void validateRelease(final Handle handle, final Manager manager) {
		handle.addValidationStates(manager.validateRelease(handle));
	}



}
