package com.espirit.ps.rw.dependency.handler;

import com.espirit.ps.rw.dependency.AbstractDefaultHandler;
import com.espirit.ps.rw.dependency.Handle;
import com.espirit.ps.rw.dependency.Manager;
import com.espirit.ps.rw.dependency.ReferencedEntryHandle;
import com.espirit.ps.rw.dependency.ValidationState;
import de.espirit.firstspirit.access.ReferenceEntry;

public final class DefaultUnknownReferenceHandler extends AbstractDefaultHandler {

	public DefaultUnknownReferenceHandler() {
		super(ReferenceEntry.class);
	}


	@Override
	public void execute(final Handle handle, final Manager manager) {
		if (handle instanceof ReferencedEntryHandle) {
			final ReferencedEntryHandle.Type referenceType = ((ReferencedEntryHandle) handle).getType();
			if (referenceType.equals(ReferencedEntryHandle.Type.UNKNOWN)
					|| referenceType.equals(ReferencedEntryHandle.Type.MISSING_PERMISSION)) {
				switch (manager.getAction()) {
					case RELEASE:
						if (ReferencedEntryHandle.Type.UNKNOWN.equals(referenceType)) {
							handle.addValidationState(ValidationState.createOther(handle, ((ReferencedEntryHandle) handle).getSourceElement(), "Unbekannte Referenz", ((ReferencedEntryHandle) handle).getReferenceEntry()));
						} else {
							handle.addValidationState(
									ValidationState.createOther(handle, ((ReferencedEntryHandle) handle).getSourceElement(),
											"Fehlende Rechte aus dem referenzierten Objekt",
											((ReferencedEntryHandle) handle).getReferenceEntry()));
						}

						break;

					case DELETE:
						break;
				}
			}
		}
	}
}
