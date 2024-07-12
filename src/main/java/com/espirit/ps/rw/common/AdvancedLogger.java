package com.espirit.ps.rw.common;


import de.espirit.common.base.Logging;
import de.espirit.firstspirit.agency.SpecialistsBroker;


public class AdvancedLogger {
	
	private final Class<?> caller;
	private final boolean  advancedLoggingEnabled;
	
	
	public AdvancedLogger(final SpecialistsBroker broker, final Class<?> caller) {
		this.caller = caller;
		
		if (ReportableWorkflowProjectApp.isInstalled(broker)) {
				advancedLoggingEnabled = ReportableWorkflowProjectApp.getConfiguration(broker).isAdvancedLoggingEnabled();
		} else {
			advancedLoggingEnabled = false;
		}
	}
	
	
	public AdvancedLogger(final boolean advancedLoggingEnabled, final Class<?> caller) {
		this.caller = caller;
		this.advancedLoggingEnabled = advancedLoggingEnabled;
	}
	
	
	public void logDebug(final String message) {
		logDebug(message, false);
	}
	
	
	public void logDebug(final String message, final Throwable throwable) {
		logDebug(message, throwable, false);
	}
	
	
	public void logDebug(final String message, final boolean force) {
		if (force || advancedLoggingEnabled) {
			Logging.logDebug(message, caller);
		}
	}
	
	
	public void logDebug(final String message, final Throwable throwable, final boolean force) {
		if (force || advancedLoggingEnabled) {
			Logging.logDebug(message, throwable, caller);
		}
	}
	
	
	public void logError(final String message) {
		logError(message, false);
	}
	
	
	public void logError(final String message, final Throwable throwable) {
		logError(message, throwable, false);
	}
	
	
	public void logError(final String message, final boolean force) {
		if (force || advancedLoggingEnabled) {
			Logging.logError(message, caller);
		}
	}
	
	
	public void logError(final String message, final Throwable throwable, final boolean force) {
		if (force || advancedLoggingEnabled) {
			Logging.logError(message, throwable, caller);
		}
	}
	
	
	public void logInfo(final String message) {
		logInfo(message, false);
	}
	
	
	public void logInfo(final String message, final Throwable throwable) {
		logInfo(message, throwable, false);
	}
	
	
	public void logInfo(final String message, final boolean force) {
		if (force || advancedLoggingEnabled) {
			Logging.logInfo(message, caller);
		}
	}
	
	
	public void logInfo(final String message, final Throwable throwable, final boolean force) {
		if (force || advancedLoggingEnabled) {
			Logging.logInfo(message, throwable, caller);
		}
	}
	
	
	public void logWarning(final String message) {
		logWarning(message, false);
	}
	
	
	public void logWarning(final String message, final Throwable throwable) {
		logWarning(message, throwable, false);
	}
	
	
	public void logWarning(final String message, final boolean force) {
		if (force || advancedLoggingEnabled) {
			Logging.logWarning(message, caller);
		}
	}
	
	
	public void logWarning(final String message, final Throwable throwable, final boolean force) {
		if (force || advancedLoggingEnabled) {
			Logging.logWarning(message, throwable, caller);
		}
	}
	
	
	public static void logDebug(final String message, final Class<?> caller) {
		Logging.logDebug(message, caller);
	}
	
	
	public static void logDebug(final String message, final Throwable throwable, final Class<?> caller) {
		Logging.logDebug(message, throwable, caller);
	}
	
	
	public static void logDebug(final String message, final Class<?> caller, final SpecialistsBroker broker) {
		if (ReportableWorkflowProjectApp.getConfiguration(broker).isAdvancedLoggingEnabled()) {
			Logging.logDebug(message, caller);
		}
	}
	
	
	public static void logDebug(final String message, final Throwable throwable, final Class<?> caller, final SpecialistsBroker broker) {
		if (ReportableWorkflowProjectApp.getConfiguration(broker).isAdvancedLoggingEnabled()) {
			Logging.logDebug(message, throwable, caller);
		}
	}
	
	
	public static void logError(final String message, final Class<?> caller) {
		Logging.logError(message, caller);
	}
	
	
	public static void logError(final String message, final Throwable throwable, final Class<?> caller) {
		Logging.logError(message, throwable, caller);
	}
	
	
	public static void logError(final String message, final Class<?> caller, final SpecialistsBroker broker) {
		if (ReportableWorkflowProjectApp.getConfiguration(broker).isAdvancedLoggingEnabled()) {
			Logging.logError(message, caller);
		}
	}
	
	
	public static void logError(final String message, final Throwable throwable, final Class<?> caller, final SpecialistsBroker broker) {
		if (ReportableWorkflowProjectApp.getConfiguration(broker).isAdvancedLoggingEnabled()) {
			Logging.logError(message, throwable, caller);
		}
	}
	
	
	public static void logInfo(final String message, final Class<?> caller) {
		Logging.logInfo(message, caller);
	}
	
	
	public static void logInfo(final String message, final Throwable throwable, final Class<?> caller) {
		Logging.logInfo(message, throwable, caller);
	}
	
	
	public static void logInfo(final String message, final Class<?> caller, final SpecialistsBroker broker) {
		if (ReportableWorkflowProjectApp.getConfiguration(broker).isAdvancedLoggingEnabled()) {
			Logging.logInfo(message, caller);
		}
	}
	
	
	public static void logInfo(final String message, final Throwable throwable, final Class<?> caller, final SpecialistsBroker broker) {
		if (ReportableWorkflowProjectApp.getConfiguration(broker).isAdvancedLoggingEnabled()) {
			Logging.logInfo(message, throwable, caller);
		}
	}
	
	
	public static void logWarning(final String message, final Class<?> caller) {
		Logging.logWarning(message, caller);
	}
	
	
	public static void logWarning(final String message, final Throwable throwable, final Class<?> caller) {
		Logging.logWarning(message, throwable, caller);
	}
	
	
	public static void logWarning(final String message, final Class<?> caller, final SpecialistsBroker broker) {
		if (ReportableWorkflowProjectApp.getConfiguration(broker).isAdvancedLoggingEnabled()) {
			Logging.logWarning(message, caller);
		}
	}
	
	
	public static void logWarning(final String message, final Throwable throwable, final Class<?> caller, final SpecialistsBroker broker) {
		if (ReportableWorkflowProjectApp.getConfiguration(broker).isAdvancedLoggingEnabled()) {
			Logging.logWarning(message, throwable, caller);
		}
	}
}
