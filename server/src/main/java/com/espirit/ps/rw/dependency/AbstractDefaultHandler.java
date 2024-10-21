package com.espirit.ps.rw.dependency;

import com.espirit.ps.rw.client.ClientSession;
import com.espirit.ps.rw.workflow.ReportableWorkflow;
import de.espirit.common.base.Logging;
import de.espirit.firstspirit.access.BaseContext;
import de.espirit.firstspirit.access.ReferenceEntry;
import de.espirit.firstspirit.access.ServicesBroker;
import de.espirit.firstspirit.access.store.IDProvider;
import de.espirit.firstspirit.access.store.StoreElement;
import de.espirit.firstspirit.access.store.contentstore.Content2;
import de.espirit.firstspirit.access.store.contentstore.Dataset;
import de.espirit.firstspirit.access.store.sitestore.PageRef;
import de.espirit.firstspirit.access.store.templatestore.MasterTemplate;
import de.espirit.firstspirit.agency.OperationAgent;
import de.espirit.firstspirit.ui.operations.RequestOperation;
import de.espirit.or.schema.Entity;

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
		boolean result = true;

		if (Action.DELETE.equals(manager.getAction()) && handle.getKeyObject() instanceof IDProvider) {

			for (ReferenceEntry referenceEntry : ((StoreElement) handle.getKeyObject()).getIncomingReferences()) {
				String handleName = ((IDProvider) handle.getKeyObject()).hasUid() ? ((IDProvider) handle.getKeyObject()).getUid() : ((IDProvider) handle.getKeyObject()).getName();
				Logging.logInfo(String.format("Handle (%s) has %d incoming references.", handleName, ((IDProvider) handle.getKeyObject()).getIncomingReferences().length), getClass());
				if (isActionableReferenceEntry(referenceEntry)) {
					IDProvider idProvider = getReferencedIdProvider(referenceEntry);
					// If the reference is the previous handle (possible parent object), it gets ignored.
					if (handle.getPreviousHandle() != null && referenceEntry.getReferencedObject().equals(handle.getPreviousHandle().getKeyObject())) {
						Logging.logInfo("the reference is the previous handle (possible parent object), it gets ignored.", getClass());
						continue;
					}

					// Checks whether the element is (one of) the initial element(s) to be deleted.
					Logging.logInfo("Check whether the element is (one of) the initial element(s) to be deleted.", getClass());
					StoreElement startStoreElement = manager.getEqualStartStoreElement(handle.getKeyObject());
					if (startStoreElement != null) {
						Set<StoreElement> ignoreSet = (Set<StoreElement>) ClientSession.getItem(manager.getContext(), ReportableWorkflow.getIdentifier(startStoreElement), null);

						if (ignoreSet != null && ignoreSet.contains(handle.getKeyObject())) {
							handle.addValidationState(ValidationState.createIgnoredOnDelete(handle, idProvider));
						} else {
							handle.addValidationState(ValidationState.createIsStillReferenced(handle, idProvider));
						}

						result = false;
					} else {
						Logging.logInfo("Element is not one of the initial elements to be deleted.", getClass());
						manager.getStartStoreElements().forEach(el -> {
							Logging.logInfo("\t - " + el.getName(), getClass());
						});
					}
				}
			}
		}

		return result;
	}


	protected final void checkOutgoingReferences(final Handle handle, final Manager manager) {
		if (Action.RELEASE.equals(manager.getAction()) && handle.getKeyObject() instanceof StoreElement) {
			for (ReferenceEntry referenceEntry : ((StoreElement) handle.getKeyObject()).getOutgoingReferences()) {
				if (!referenceEntry.getRelease()) {
					if (referenceEntry.isBroken()) {
						ResolveObject resolveObject = new ResolveObject(new ReferencedEntryHandle(((StoreElement) handle.getKeyObject()), handle, manager, referenceEntry, ReferencedEntryHandle.Type.BROKEN), handle);
						handle.addNextHandleObject(resolveObject);
					} else if (referenceEntry.isType(ReferenceEntry.EXTERNAL_REFERENCE)) {
						ResolveObject resolveObject = new ResolveObject(new ReferencedEntryHandle(((StoreElement) handle.getKeyObject()), handle, manager, referenceEntry, ReferencedEntryHandle.Type.EXTERNAL), handle);
						handle.addNextHandleObject(resolveObject);
					} else {
						StoreElement storeElement = null;

						if (referenceEntry.isType(ReferenceEntry.CONTENT_REFERENCE)) {
							if (referenceEntry.getReferencedElement() instanceof Content2 && referenceEntry.getReferencedObject() instanceof Entity) {
								storeElement = ((Content2) referenceEntry.getReferencedElement()).getDataset((Entity) referenceEntry.getReferencedObject());
							}
						} else if (referenceEntry.isType(ReferenceEntry.STORE_ELEMENT_REFERENCE)) {
							storeElement = referenceEntry.getReferencedElement();
						}

						if (storeElement == null) {
							ResolveObject resolveObject = new ResolveObject(new ReferencedEntryHandle(((StoreElement) handle.getKeyObject()), handle, manager, referenceEntry, ReferencedEntryHandle.Type.UNKNOWN), handle);
							handle.addNextHandleObject(resolveObject);
						} else {
							boolean addStoreElement = true;

							if (storeElement instanceof PageRef && handle.getKeyObject() instanceof MasterTemplate) {
								PageRef previewPageRef = ((MasterTemplate) handle.getKeyObject()).getPreviewPageRef();
								if (storeElement.equals(previewPageRef)) {
									addStoreElement = false;
								}
							}

							if (addStoreElement) {
								ResolveObject resolveObject = new ResolveObject(storeElement, handle);
								handle.addNextHandleObject(resolveObject);
							}
						}
					}
				}
			}
		}
	}


	@Override
	public void execute(final Handle handle, final Manager manager) {
		if (Action.RELEASE.equals(manager.getAction())) {
			validateRelease(handle, manager);
			validateData(handle, manager);
		}

		checkOutgoingReferences(handle, manager);
	}


	public final Class<?> getElementType() {
		return elementType;
	}


	private IDProvider getReferencedIdProvider(final ReferenceEntry referenceEntry) {
		if (referenceEntry.getReferencedObject() instanceof Entity) {
			final Entity   entity   = (Entity) referenceEntry.getReferencedObject();
			final Content2 content2 = (Content2) referenceEntry.getReferencedElement();
			return content2.getDataset(entity);
		}
		return (IDProvider) referenceEntry.getReferencedObject();
	}


	protected final boolean isClientContextAvailable(final Manager manager) {
		BaseContext context;
		context = manager.getContext();

		try {
			ClientSession clientSession = context.requireSpecialist(ServicesBroker.TYPE).getService(ClientSession.class);
			return clientSession != null;
		} catch (Exception e) {
			return false;
		}
	}


	protected final void validateData(final Handle handle, final Manager manager) {
		handle.addValidationStates(manager.validateData(handle));
	}


	protected final void validateRelease(final Handle handle, final Manager manager) {
		handle.addValidationStates(manager.validateReleased(handle));
	}


	private static boolean isActionableReferenceEntry(final ReferenceEntry referenceEntry) {
		return referenceEntry.getReferencedObject() instanceof IDProvider || referenceEntry.getReferencedObject() instanceof Entity;
	}
}
