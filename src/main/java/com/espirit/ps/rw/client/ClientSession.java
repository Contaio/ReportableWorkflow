package com.espirit.ps.rw.client;


import com.espirit.ps.rw.common.AdvancedLogger;
import com.espirit.ps.rw.common.configuration.ProjectAppConfiguration;
import com.espirit.ps.rw.dependency.LogHelper;
import de.espirit.common.base.Logging;
import de.espirit.firstspirit.access.ServiceNotFoundException;
import de.espirit.firstspirit.access.ServicesBroker;
import de.espirit.firstspirit.access.store.IDProvider;
import de.espirit.firstspirit.agency.SpecialistsBroker;
import de.espirit.firstspirit.webedit.WebeditUiAgent;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class ClientSession {

	private final Observer            observer;
	private       Map<String, Object> storage    = new HashMap<>();
	private       Boolean             tppSupport = null;


	public ClientSession() {
		observer = null;
	}


	public ClientSession(final Observer observer) {
		this.observer = observer;
	}


	private IDProvider getElement() {
		return observer == null ? null : observer.getElement();
	}


	private Object getItem(final String key) {
		return storage.get(key);
	}


	private boolean hasItem(final String key) {
		return storage.containsKey(key);
	}


	private void removeItem(final String key) {
		storage.remove(key);
	}


	private void setItem(final String key, final Object value) {
		storage.put(key, value);
	}


	public static Object getAndDeleteItem(final SpecialistsBroker broker, final String name, final Object fallback) {
		ClientSession clientSession = getClientSession(broker);

		if (clientSession == null) {
			AdvancedLogger.logInfo("No client session exists.", ClientSession.class);
			return fallback;
		} else if (!clientSession.hasItem(name)) {
			AdvancedLogger.logInfo("Client session has no items.", ClientSession.class);
			return fallback;
		} else {
			Object item = clientSession.getItem(name);
			clientSession.removeItem(name);

			return item;
		}
	}


	@Nullable
	private static ClientSession getClientSession(final SpecialistsBroker broker) {
		try {
			return broker.requireSpecialist(ServicesBroker.TYPE).getService(ClientSession.class);
		} catch (ServiceNotFoundException e) {
			Logging.logDebug("Service could not be found. ", e, ClientSession.class);
			return null;
		}
	}


	public static IDProvider getElement(final SpecialistsBroker broker) {
		AdvancedLogger.logDebug("Fetching Element", ClientSession.class);
		ClientSession  clientSession  = getClientSession(broker);
		WebeditUiAgent webeditUiAgent = broker.requestSpecialist(WebeditUiAgent.TYPE);

		if (webeditUiAgent == null || isTPPSupported(broker)) {
			AdvancedLogger.logDebug("Fetching by clientSession: " + LogHelper.getTextIdentification(clientSession != null ? clientSession.getElement() : null),
					ClientSession.class);
			return clientSession != null ? clientSession.getElement() : null;
		} else {
			AdvancedLogger.logDebug("Fetching by webeditUiAgent: " + LogHelper.getTextIdentification(webeditUiAgent.getPreviewElement()),
					ClientSession.class);
			return webeditUiAgent.getPreviewElement();
		}
	}


	public static Object getItem(final SpecialistsBroker broker, final String name, final Object fallback) {
		AdvancedLogger.logDebug("Fetching item", ClientSession.class);
		ClientSession clientSession = getClientSession(broker);
		if (clientSession != null) {
			AdvancedLogger.logDebug(clientSession.hasItem(name) ? ("Retrieveing:" + LogHelper.getTextIdentification(clientSession.getItem(name)))
					: ("Fallback: " + fallback), ClientSession.class);
			return clientSession.hasItem(name) ? clientSession.getItem(name) : fallback;
		} else {
			AdvancedLogger.logDebug("Client Session null. Fallback:" + LogHelper.getTextIdentification(fallback), ClientSession.class);
			return fallback;
		}
	}


	public static boolean isTPPSupported(final SpecialistsBroker broker) {
		ClientSession clientSession = getClientSession(broker);

		if (clientSession == null) {
			Logging.logWarning("ClientSession was null. Returned TPP Support as false.", ClientSession.class);
			return false;
		} else if (clientSession.tppSupport == null) {
			clientSession.tppSupport = ProjectAppConfiguration.loadConfiguration(broker).isTPPSupported();
		}

		return clientSession.tppSupport;
	}


	public static void removeItem(final SpecialistsBroker broker, final String name) {
		ClientSession cientSession = getClientSession(broker);
		if (cientSession != null) {
			cientSession.removeItem(name);
		}
	}


	public static void removeItems(final SpecialistsBroker broker, final List<String> names) {
		ClientSession clientSession = getClientSession(broker);
		if (clientSession != null) {
			for (String name : names) {
				clientSession.removeItem(name);
			}
		}
	}


	public static void setItem(final SpecialistsBroker broker, final String name, final Object value) {
		ClientSession cientSession = getClientSession(broker);
		if (cientSession != null) {
			cientSession.setItem(name, value);
		}
	}


	public interface Observer {

		IDProvider getElement();
	}
}
