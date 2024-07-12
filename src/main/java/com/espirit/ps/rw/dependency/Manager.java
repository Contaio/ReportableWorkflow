package com.espirit.ps.rw.dependency;


import com.espirit.ps.rw.common.AdvancedLogger;
import com.espirit.ps.rw.common.ReportableWorkflowProjectApp;
import com.espirit.ps.rw.common.configuration.ProjectAppConfiguration;
import de.espirit.common.base.Logging;
import de.espirit.firstspirit.access.BaseContext;
import de.espirit.firstspirit.access.ModuleAgent;
import de.espirit.firstspirit.access.store.IDProvider;
import de.espirit.firstspirit.access.store.StoreElement;
import de.espirit.firstspirit.access.store.contentstore.Dataset;
import de.espirit.firstspirit.access.store.mediastore.MediaFolder;
import de.espirit.firstspirit.access.store.pagestore.Page;
import de.espirit.firstspirit.access.store.pagestore.PageFolder;
import de.espirit.firstspirit.access.store.sitestore.PageRef;
import de.espirit.firstspirit.access.store.sitestore.PageRefFolder;
import de.espirit.firstspirit.agency.SpecialistsBroker;
import de.espirit.firstspirit.module.descriptor.ComponentDescriptor;
import org.jetbrains.annotations.Nullable;

import javax.print.attribute.standard.Media;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;


public final class Manager {

	private final Map<String, Object> customObjectMap = new HashMap<>();

	private final Action                  action;
	private final AdvancedLogger          advancedLogger;
	private final ProjectAppConfiguration configuration;
	private final SpecialistsBroker       specialistsBroker;
	private final boolean                 dependentRelease;
	private final HandlerController       handlerController;
	private final List<ResolveObject>     resolveList;
	private final List<StoreElement>      startStoreElements;
	private final ValidationStateMap      validationStateMap;
	private final boolean                 justDependentMediaRelease;

	private boolean actionable = true;


	public Manager(final SpecialistsBroker context, final StoreElement startStoreElement, final Action action) {
		this(context, Collections.singletonList(startStoreElement), false, false, action);
	}


	public Manager(final SpecialistsBroker context, final StoreElement startStoreElement, final boolean dependentRelease, final boolean justDependentMediaRelease, final Action action) {
		this(context, Collections.singletonList(startStoreElement), dependentRelease, justDependentMediaRelease, action);
	}


	public Manager(final SpecialistsBroker context, final List<StoreElement> startStoreElements, final boolean dependentRelease, final boolean justDependentMediaRelease, final Action action) {
		this.specialistsBroker = context;
		this.startStoreElements = startStoreElements;
		this.dependentRelease = dependentRelease;
		this.justDependentMediaRelease = justDependentMediaRelease;
		this.action = action;
		this.validationStateMap = new ValidationStateMap(this);
		this.resolveList = new LinkedList<>();
		this.startStoreElements.forEach(element -> resolveList.add(new ResolveObject(element, null)));
		this.configuration = ReportableWorkflowProjectApp.getConfiguration(context);
		this.advancedLogger = new AdvancedLogger(context, Manager.class);

		advancedLogger.logInfo("Manager initiated with " + startStoreElements.size() + " start elements...", true);

		this.handlerController = loadHandlerController();

	}


	private void checkActionable(final Handle handle) {
		for (ValidationState state : handle.getValidationStates()) {
			if (actionable) {
				actionable &= !state.isPreventing();
			}
		}
	}


	public Action getAction() {
		return action;
	}


	public List<IDProvider> getActionableElements() {
		List<IDProvider> actionableElements;

		if (Action.RELEASE.equals(action)) {
			actionableElements = validationStateMap.getActionableElements(false);
		} else {
			actionableElements = validationStateMap.getActionableElements(true);

			for (StoreElement element : startStoreElements) {
				if (element instanceof IDProvider && !actionableElements.contains(element)) {
					// Prepend PageRefs to ensure Validity during deletions
					if (element instanceof PageRef) {
						actionableElements.add(0, (IDProvider) element);
					} else {
						actionableElements.add((IDProvider) element);
					}
				}
			}
		}

		return actionableElements;
	}


	public AdvancedLogger getAdvancedLogger() {
		return advancedLogger;
	}


	public ProjectAppConfiguration getConfiguration() {
		return configuration;
	}


	public SpecialistsBroker getContext() {
		return specialistsBroker;
	}


	public Object getCustomObject(final String key) {
		return getCustomObjectOrDefault(key, null);
	}


	public Object getCustomObjectOrDefault(final String key, final Object defaultValue) {
		return customObjectMap.getOrDefault(key, defaultValue);
	}


	public StoreElement getEqualStartStoreElement(Object handle) {
		return startStoreElements.stream().filter(e -> e.equals(handle)).findFirst().orElse(null);
	}


	@Nullable
	private Handle getHandle(final ResolveObject resolveObject) {
		if (resolveObject.getObject() instanceof Handle) {
			return (Handle) resolveObject.getObject();
		} else if (resolveObject.getObject() instanceof StoreElement) {
			return new DefaultHandle(resolveObject.getObject(), resolveObject.getPreviousHandle(), this);
		} else {
			return null;
		}
	}


	public HandlerController getHandlerController() {
		return handlerController;
	}


	public List<ValidationState> getInvalidElements() {
		List<ValidationState> states;
		if (Action.RELEASE.equals(action)) {
			states = validationStateMap.getAllValidationStates(ValidationState.Type.getAllPreventingTypes(Action.RELEASE));
		} else {
			states = validationStateMap.getAllValidationStates(ValidationState.Type.getAllPreventingTypes(Action.DELETE));
		}

		return states;
	}


	public List<ValidationState> getReportRelevantElements(final boolean invalidOnly) {
		List<ValidationState> states = new LinkedList<>();

		if (invalidOnly) {
			states = getInvalidElements();
		} else {
			states.addAll(getInvalidElements());
			states.addAll(getValidElements());
			states = states.stream().distinct().collect(Collectors.toList());
			//states = validationStateMap.getAllValidationStates();
		}

		final ArrayList<ValidationState> validationStates = new ArrayList<>();
		for (StoreElement element : startStoreElements) {
			if (element instanceof IDProvider) {
				validationStates.add(ValidationState.createDisplayOnly(null, (IDProvider) element));
			}
		}
		validationStates.addAll(states);
		return validationStates;
	}


	public List<StoreElement> getStartStoreElements() {
		return startStoreElements;
	}


	public List<ValidationState> getValidElements() {
		List<ValidationState> states;
		if (Action.RELEASE.equals(action)) {
			states = validationStateMap.getAllValidationStates(ValidationState.Type.getAllNonPreventingTypes(Action.RELEASE));
		} else {
			states = validationStateMap.getAllValidationStates(ValidationState.Type.getAllNonPreventingTypes(Action.DELETE));
		}
		return states.stream().filter(ValidationState::isNotIgnored).collect(Collectors.toList());
	}


	public List<ValidationState> getWorkflowRelevantElements() {
		List<ValidationState> states = new LinkedList<>();
		states.addAll(getValidElements());
		states.addAll(getInvalidElements());

		final ArrayList<ValidationState> validationStates = new ArrayList<>();
		for (StoreElement element : startStoreElements) {
			if (element instanceof IDProvider) {
				validationStates.add(ValidationState.createDisplayOnly(null, (IDProvider) element));
			}
		}
		validationStates.addAll(states);
		return validationStates;
	}


	public boolean isActionable() {
		return actionable;
	}


	public boolean isInStartStoreElements(Object keyObject) {
		return !isNotInStartStoreElements(keyObject);
	}


	public boolean isNotInStartStoreElements(Object keyObject) {
		return startStoreElements.stream().filter(e -> e.equals(keyObject)).findAny().isEmpty();
	}


	private HandlerController loadHandlerController() {
		HandlerController handlerController = DefaultHandlerController.getDefaultHandlerController();
		String            controllerClassName;
		Set<String>       handlerClassNames;

		if (Action.RELEASE.equals(action)) {
			controllerClassName = configuration.getReleaseHandlercontrollerClassName();
			handlerClassNames = configuration.getReleaseHandlerClassNames();
		} else {
			controllerClassName = configuration.getDeleteHandlercontrollerClassName();
			handlerClassNames = configuration.getDeleteHandlerClassNames();
		}

		if (!handlerController.getClass().getCanonicalName().equals(controllerClassName)) {
			boolean                         found       = false;
			List<AbstractHandlerController> controllers = DependencyUtil.getInstancesByType(specialistsBroker, AbstractHandlerController.class);

			if (!controllers.isEmpty()) {
				for (HandlerController controller : controllers) {
					if (controller.getActions().contains(action) && controller.getClass().getCanonicalName().equals(controllerClassName)) {
						handlerController = controller;
						found = true;

						advancedLogger.logInfo("Use HandlerController: " + handlerController.getClass().getCanonicalName());

						break;
					}
				}
			}

			if (!found) {
				advancedLogger.logWarning("Configured HandlerController '" + controllerClassName + "' not found for action " + action.name().toLowerCase() + ". Fallback to default.");
			}
		}

		Handler.Default[]               defaultHandlers = Handler.Default.valuesByAction(action);
		ModuleAgent                     moduleAgent     = specialistsBroker.requireSpecialist(ModuleAgent.TYPE);
		Collection<ComponentDescriptor> components      = moduleAgent.getComponents(AbstractDefaultHandler.class);
		boolean                         loadAll         = handlerClassNames.size() == 1 && handlerClassNames.iterator().next().equals("*");

		if (loadAll) {
			advancedLogger.logInfo("Load all handlers...");
			for (Handler.Default defaultHandler : defaultHandlers) {
				try {
					handlerController.addHandler(defaultHandler.getHandlerClass().getDeclaredConstructor().newInstance());
					advancedLogger.logInfo("Add default handler: " + defaultHandler.getHandlerClass());
				} catch (Exception e) {
					advancedLogger.logError("Unable to load handler: " + defaultHandler.getHandlerClass(), e);
				}
			}

			advancedLogger.logInfo("Found " + components.size() + " components for possible custom handler.");

			for (ComponentDescriptor component : components) {
				try {
					advancedLogger.logInfo("Try to load custom handler '" + component.getComponentClass() + "'...");
					Class<? extends AbstractDefaultHandler> handlerClass = moduleAgent.getTypeForName(component.getName(), AbstractDefaultHandler.class);
					advancedLogger.logInfo("Class '" + component.getComponentClass() + "' loaded...");
					Constructor<? extends AbstractDefaultHandler> handlerClassConstructor = handlerClass.getDeclaredConstructor();
					advancedLogger.logInfo("Use default constructor...");
					AbstractDefaultHandler handler = handlerClassConstructor.newInstance();
					advancedLogger.logInfo("Custom handler loaded!");
					handlerController.addHandler(handler);
					advancedLogger.logInfo("Add default handler: " + component.getComponentClass());
				} catch (Exception e) {
					advancedLogger.logError("Unable to load handler: " + component.getComponentClass(), e);
				}
			}
		} else {
			advancedLogger.logInfo("Load defined handlers...");

			handlerClassList:
			for (String className : handlerClassNames) {
				try {
					for (Handler.Default defaultHandler : defaultHandlers) {
						if (defaultHandler.getHandlerClass().getCanonicalName().equals(className)) {
							handlerController.addHandler(defaultHandler.getHandlerClass().getDeclaredConstructor().newInstance());
							advancedLogger.logInfo("Add default handler: " + className);
							continue handlerClassList;
						}
					}

					advancedLogger.logInfo(className + "is no default handler. Search for custom handler...");

					for (ComponentDescriptor component : components) {
						if (!component.getComponentClass().equals(className)) {
							continue;
						}

						advancedLogger.logInfo("Try to load custom handler '" + className + "'...");
						Class<? extends AbstractDefaultHandler> handlerClass = moduleAgent.getTypeForName(component.getName(), AbstractDefaultHandler.class);
						advancedLogger.logInfo("Class '" + className + "' loaded...");
						Constructor<? extends AbstractDefaultHandler> handlerClassConstructor = handlerClass.getDeclaredConstructor();
						advancedLogger.logInfo("Use default constructor...");
						AbstractDefaultHandler handler = handlerClassConstructor.newInstance();
						advancedLogger.logInfo("Custom handler loaded!");
						handlerController.addHandler(handler);
						continue handlerClassList;
					}

					advancedLogger.logWarning("Custom handler '" + className + "' not found!");
				} catch (Exception e) {
					advancedLogger.logError("Unable to load handler: " + className, e);
				}

			}
		}

		return handlerController;
	}


	/**
	 * Executes the full validation of the elements to delete/release.
	 * For this a resolveList is used and extended by every element which needs to be checked.
	 */
	public Manager resolve() {
		advancedLogger.logInfo("Call preResolve()...");
		handlerController.preResolve(this);
		advancedLogger.logInfo("...preResolve() called.");

		advancedLogger.logInfo("Iterate over resolveList...");

		while (!resolveList.isEmpty()) {
			advancedLogger.logInfo("Currently resolveList has size of " + resolveList.size() + ".");

			// Takes the first element of the resolution list to check.
			ResolveObject resolveObject = resolveList.remove(0);
			Handle        handle        = getHandle(resolveObject);

			if (Objects.nonNull(handle) && Objects.nonNull(handle.getKeyObject())) {
				if (!validationStateMap.contains(handle.getKeyObject())) {
					advancedLogger.logInfo("Check handle \n" + handle.getKeyObject().toString());

					HandlerController controller = getHandlerController();

					advancedLogger.logInfo("Call resetBeforeIteration()...");
					controller.resetBeforeIteration();
					advancedLogger.logInfo("...resetBeforeIteration() called.");

					advancedLogger.logInfo("Call preIteration()...");
					controller.preIteration(handle, this);
					advancedLogger.logInfo("...preIteration() called.");

					while (controller.hasNext()) {
						advancedLogger.logInfo("Current controller for handle: " + controller.getClass().getCanonicalName());

						advancedLogger.logInfo("Call preExecution()...");
						controller.preExecution(handle, this);
						advancedLogger.logInfo("...preExecution() called.");

						// At this point the different handler (DefaultPageHandler etc.) are called and validate the element.
						advancedLogger.logInfo("Call execute()...");
						controller.next().execute(handle, this);
						advancedLogger.logInfo("...execute() called.");

						advancedLogger.logInfo("Call postExecution()...");
						controller.postExecution(handle, this);
						advancedLogger.logInfo("...postExecution() called.");
					}

					advancedLogger.logInfo("Call postIteration()...");
					controller.postIteration(handle, this);
					advancedLogger.logInfo("...postIteration() called.");

					// If the validation of the elements detected more elements to be checked, they are added to the resolve list.
					List<ResolveObject> nextHandleObjects = handle.getNextHandleObjects();
					advancedLogger.logInfo("Add " + nextHandleObjects.size() + " new resolve objects to list.");
					resolveList.addAll(nextHandleObjects);

					if (Action.RELEASE.equals(action) && handle.getValidationStates().size() == 0 && handle.getKeyObject() instanceof IDProvider && isNotInStartStoreElements(handle)) {
						handle.addValidationState(ValidationState.createReleaseUnsupported(handle, (IDProvider) handle.getKeyObject()));
					}

					if (handle.getValidationStates().size() > 0 && actionable) {
						checkActionable(handle);
					}

					// Adds the current validation state of the element to the validationStateMap (possibly no state ist defined yet)
					validationStateMap.add(handle);
					advancedLogger.logInfo("Add handle to validationStateMap (Handles: " + validationStateMap.numberOfHandles() + " / States:" + validationStateMap.numberOfValidationStates() + ").");
				} else {
					advancedLogger.logInfo("Handle already resolved. continue...");
				}
			} else {
				advancedLogger.logInfo("No handle for this loop. continue...");
			}
		}

		handlerController.postResolve(this);
		return this;
	}


	public void setCustomObject(final String key, final Object o) {
		customObjectMap.put(key, o);
	}


	public List<ValidationState> validateFormData(final Handle handle) {
		List<ValidationState> resultList = new LinkedList<>();

		if (handle.getKeyObject() instanceof StoreElement) {
			resultList.addAll(ValidationUtil.validateFormData(ValidationUtil.createContext(specialistsBroker), handle));
		}

		return resultList;
	}


	/**
	 * This method validates the current object. It sets the ValidationState depending
	 * on the configuration parameters and the object itself.
	 *
	 * @param handle Contains the current object
	 * @return List of {@link ValidationState} objects
	 */
	@SuppressWarnings("unchecked")
	public List<ValidationState> validateRelease(final Handle handle) {
		final List<ValidationState> resultList = new LinkedList<>();

		if (handle.getKeyObject() instanceof IDProvider) {
			final IDProvider currentElement = (IDProvider) handle.getKeyObject();

			if (isNotInStartStoreElements(currentElement)) {
				// If the element is already released there is nothing to do.
				// However, if the IDProvider is not released we need to check if the release operation is supported
				// (Templates can not be released).
				if (currentElement.isReleaseSupported() && !currentElement.isReleased()) {
					// The next point to check is if the IDProvider is newly created and have never been released,
					// or is already in the release store.
					if (currentElement.isInReleaseStore()) {
						final String uid = currentElement instanceof Dataset ? "(Dataset fs_id:" + currentElement.getId() + " )" : currentElement.getUid();
						Logging.logInfo(String.format("%s is in release store", uid), getClass());
						if (justDependentMediaRelease) {
							if (isRelevantForJustDependentMediaRelease(handle)) {
								resultList.add(ValidationState.createDependedReleasable(handle, currentElement));
							} else {
								resultList.add(ValidationState.createInReleaseStore(handle, currentElement));
							}
						} else {
							// to achieve downwards compatibility
							resultList.add(ValidationState.createDependedReleasable(handle, currentElement));
						}
					} else {
						// Case: IDProvider is a new element and has never been released
						if (dependentRelease) {
							resultList.add(ValidationState.createDependedReleasable(handle, currentElement));
						} else if (justDependentMediaRelease) {
							if (isRelevantForJustDependentMediaRelease(handle)) {
								resultList.add(ValidationState.createDependedReleasable(handle, currentElement));
							} else {
								if (currentElement instanceof PageRef) {
									resultList.add(ValidationState.createReleasable(handle, currentElement));
								} else {
									resultList.add(ValidationState.createUnreleased(handle, currentElement));
								}
							}
						} else {
							resultList.add(ValidationState.createUnreleased(handle, currentElement));
						}
					}
				} else {
					// Object is already released, nothing to do
					resultList.add(ValidationState.createReleasable(handle, currentElement));
				}
			} else {
				// Elements from the Start List should be dependent released
				if (dependentRelease || justDependentMediaRelease) {
					resultList.add(ValidationState.createDependedReleasable(handle, currentElement));
				} else {
					resultList.add(ValidationState.createReleasable(handle, currentElement));
				}
			}
		}


		return resultList;
	}


	private static boolean isRelevantForJustDependentMediaRelease(final Handle handle) {
		return handle.getKeyObject() instanceof Media
				|| handle.getKeyObject() instanceof MediaFolder
				|| handle.getKeyObject() instanceof Page
				|| handle.getKeyObject() instanceof PageFolder
				|| handle.getKeyObject() instanceof PageRefFolder
				|| handle.getKeyObject() instanceof Dataset;
	}
}
