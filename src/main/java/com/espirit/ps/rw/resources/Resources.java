package com.espirit.ps.rw.resources;


import com.espirit.ps.rw.common.AdvancedLogger;
import de.espirit.firstspirit.access.BaseContext;
import de.espirit.firstspirit.agency.Image;
import de.espirit.firstspirit.agency.ImageAgent;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.Scanner;


public abstract class Resources {
	
	private static final String PROJECT_PREFIX       = "reportable_workflow";
	private static final String PROJECT_PACKAGE_PATH = "rw.";
	private static final String PREFIX_JAVACLIENT    = "javaclient";
	
	
	public static String getConst(final String key, final Class<?> clazz) {
		// javaclient.reportable_workflow.properties.consts
		String constance = getString(PREFIX_JAVACLIENT + "." + PROJECT_PREFIX + ".properties.consts", key, clazz);
		
		if (constance == null || constance.isEmpty()) {
			return "(MISSING KEY: " + key + ")";
		} else {
			return constance;
		}
	}
	
	
	public static String getFilePath(final String fileName, final Class<?> clazz) {
		return PROJECT_PREFIX + "/files/" + fileName;
	}
	
	
	@Nullable
	public static Icon getIcon(final String fileName, final Class<?> clazz) {
		try {
			// /javaclient/reportable_workflow/icons/...
			return new ImageIcon(clazz.getResource("/" + PREFIX_JAVACLIENT + "/" + PROJECT_PREFIX + "/icons/" + fileName));
		} catch (Exception e) {
			AdvancedLogger.logWarning(e.getMessage(), e, clazz);
			return null;
		}
	}
	
	
	public static String getIconPath(final String fileName, final Class<?> clazz) {
		return PROJECT_PREFIX + "/icons/" + fileName;
	}
	
	
	@Nullable
	public static Image<?> getImageIcon(final BaseContext context, final String fileName, final Class<?> clazz) {
		try {
			if (context.is(BaseContext.Env.WEBEDIT)) {
				// webedit/reportable_workflow/icons/...
				return context.requireSpecialist(ImageAgent.TYPE).getImageFromUrl(PROJECT_PREFIX + "/icons/" + fileName);
			} else {
				return context.requireSpecialist(ImageAgent.TYPE).getImageFromIcon(getIcon(fileName, clazz));
			}
		} catch (Exception e) {
			AdvancedLogger.logWarning(e.getMessage(), e, clazz);
			return null;
		}
	}
	
	
	public static String getLabel(final String key, final Class<?> clazz, final Object... args) {
		String label = String.format(getLabel(key, clazz), args);
		
		if (label == null || label.isEmpty()) {
			return "(MISSING KEY: " + key + ")";
		} else {
			return label;
		}
	}
	
	
	public static String getLabel(final String key, final Class<?> clazz) {
		// javaclient.reportable_workflow.properties.labels
		String label = getString(PREFIX_JAVACLIENT + "." + PROJECT_PREFIX + ".properties.labels", key, clazz);
		
		if (label == null || label.isEmpty()) {
			return "(MISSING KEY: " + key + ")";
		} else {
			return label;
		}
	}
	
	
	private static String getString(final String bundleName, final String key, final Class<?> clazz) {
		try {
			ResourceBundle resourceBundle = ResourceBundle.getBundle(bundleName, Locale.getDefault(), clazz.getClassLoader());
			
			/*
			// edit by Mario Vaccarisi, 27.11.2020
			// remove defined encoding, used without any charset parameter!
			return new String(resourceBundle.getString(key).getBytes(StandardCharsets.ISO_8859_1), StandardCharsets.UTF_8);
			 */
			return new String(resourceBundle.getString(key).getBytes());//StandardCharsets.ISO_8859_1));//, StandardCharsets.UTF_8);
		} catch (final Exception e) {
			AdvancedLogger.logWarning(e.getMessage(), e, clazz);
			return "";
		}
	}
	
	
	public static String readFile(final String fileName, final Class<?> clazz) {
		try {
			// /javaclient/reportable_workflow/files/...
			return new Scanner(clazz.getResourceAsStream("/" + PREFIX_JAVACLIENT + "/" + PROJECT_PREFIX + "/files/" + fileName), "UTF-8").useDelimiter("\\A").next();
		} catch (Exception e) {
			AdvancedLogger.logWarning(e.getMessage(), e, clazz);
			return "";
		}
	}
}
