package com.espirit.ps.rw.dependency.handler;
import com.espirit.ps.rw.dependency.*;

import de.espirit.common.base.Logging;

import de.espirit.firstspirit.access.ReferenceEntry;
import de.espirit.firstspirit.access.store.IDProvider;
import de.espirit.firstspirit.server.reference.ExternalReferenceEntry;

import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public final class DefaultExternalReferenceHandler extends AbstractDefaultHandler {
	
	public DefaultExternalReferenceHandler() {
		super(ReferenceEntry.class);
	}
	
	
	@Override
	public void execute(final Handle handle, final Manager manager) {

		if (handle instanceof ReferencedEntryHandle && ((ReferencedEntryHandle) handle).getType().equals(ReferencedEntryHandle.Type.EXTERNAL)) {
			// How to handle the release and delete action, depending on the http status code
			switch (manager.getAction()) {
				case RELEASE:
					final ExternalReferenceEntry keyObject = (ExternalReferenceEntry) handle.getKeyObject();
					final IDProvider idProvider = (IDProvider) ((ReferencedEntryHandle) handle).getSourceElement();
					handle.addValidationState(ValidationState.createIgnoredOnRelease(handle, idProvider));

//					final URL url;
//					try {
//						url = new URL(keyObject.getReferenceString());
//					} catch (MalformedURLException e) {
//						Logging.logError(e.getMessage(), getClass());
//						handle.addValidationState(ValidationState.createBroken(handle, idProvider,((ReferencedEntryHandle) handle).getReferenceEntry()));
//						break;
//					}
//
//					final Boolean checkResult = checkHttpsStatusCode(url);
//
//					if (checkResult == null) {
//						// TODO: Es passiert halt nur der log aber kein EIntrag mehr?
//
//						handle.addValidationState(ValidationState.createIgnoredOnRelease(handle, idProvider));
//						Logging.logError("Aber ich bin jetzt hier, oder? ", getClass());
//						// handle.addValidationState(ValidationState.createBroken(handle, idProvider,((ReferencedEntryHandle) handle).getReferenceEntry()));
//					} else if (checkResult) {
//						handle.addValidationState(ValidationState.createReleasable(handle,idProvider));
//					} else {
//						handle.addValidationState(ValidationState.createIgnoredOnRelease(handle, idProvider));
//					}
					break;
				case DELETE:
					break;
			}
		}
	}

	public Boolean checkHttpsStatusCode(URL url) {
		try {
			final HttpURLConnection openedConnection = (HttpURLConnection) url.openConnection();
			openedConnection.setRequestMethod("GET");
			openedConnection.connect();
			final int code = openedConnection.getResponseCode();
			return code >= 200 && code <= 399;
		} catch (Exception e) {
			Logging.logError("Request fehlgeschlagen ", e, getClass());
			return null;
		}
	}
}