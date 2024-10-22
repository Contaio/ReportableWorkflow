package com.espirit.ps.rw.dependency.handler;
import com.espirit.ps.rw.common.ReportableWorkflowUtil;
import com.espirit.ps.rw.dependency.*;

import de.espirit.common.base.Logging;
import de.espirit.firstspirit.access.ReferenceEntry;
import de.espirit.firstspirit.access.store.IDProvider;
import de.espirit.firstspirit.access.store.StoreElement;
import de.espirit.firstspirit.server.reference.ExternalReferenceEntry;

import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Set;

public final class DefaultExternalReferenceHandler extends AbstractDefaultHandler {
	
	public DefaultExternalReferenceHandler() {
		super(ReferenceEntry.class);
	}

	@Override
	public void execute(final Handle handle, final Manager manager) {

		if (handle instanceof ReferencedEntryHandle && ((ReferencedEntryHandle) handle).getType().equals(ReferencedEntryHandle.Type.EXTERNAL)) {
			switch (manager.getAction()) {
				case RELEASE:
					final ExternalReferenceEntry keyObject = (ExternalReferenceEntry) handle.getKeyObject();
					final IDProvider idProvider = (IDProvider) ((ReferencedEntryHandle) handle).getSourceElement();

					final URL url;
					try {
						url = new URL(keyObject.getReferenceString());
					} catch (MalformedURLException ignore) {
						Logging.logError("previous Handle " + handle.getPreviousHandle(), getClass());
						handleIgnoreSet(manager, handle, idProvider);
						break;

					}

					final Boolean checkResult = checkHttpsStatusCode(url);
					if (checkResult != null && checkResult) {
						handle.addValidationState(ValidationState.createReleasable(handle,idProvider));
					} else {
						handleIgnoreSet(manager, handle, idProvider);
					}
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
		} catch (Exception ignore) {
			return null;
		}
	}

	public void handleIgnoreSet(final Manager manager, final Handle handle, final IDProvider idProvider) {
		final Set<StoreElement> ignoreSet = ReportableWorkflowUtil.buildIgnoreSet(manager);
		if (ignoreSet != null && ignoreSet.contains(idProvider)) {
			handle.addValidationState(ValidationState.createIgnoredOnRelease(handle, idProvider));
		} else {
			handle.addValidationState(ValidationState.createUnreleased(handle, idProvider));
		}
	}
}