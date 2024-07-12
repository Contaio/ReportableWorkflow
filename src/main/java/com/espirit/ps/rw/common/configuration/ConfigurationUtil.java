package com.espirit.ps.rw.common.configuration;

import de.espirit.firstspirit.access.ModuleAgent;
import de.espirit.firstspirit.agency.SpecialistsBroker;
import de.espirit.firstspirit.module.descriptor.ComponentDescriptor;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

public class ConfigurationUtil {
	public static Collection<ComponentDescriptor> getComponentsByType(final SpecialistsBroker broker, final Class<?> type) {
		List<ComponentDescriptor> instances   = new LinkedList<>();
		ModuleAgent               moduleAgent = broker.requireSpecialist(ModuleAgent.TYPE);
		
		return moduleAgent.getComponents(type);
	}
}
