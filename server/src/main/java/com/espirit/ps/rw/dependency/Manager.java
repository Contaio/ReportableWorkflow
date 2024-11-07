package com.espirit.ps.rw.dependency;


import de.espirit.firstspirit.access.store.sitestore.PageRef;
import com.espirit.ps.rw.common.AdvancedLogger;
import com.espirit.ps.rw.common.ReportableWorkflowProjectApp;
import com.espirit.ps.rw.common.configuration.ProjectAppConfiguration;
import de.espirit.firstspirit.access.BaseContext;
import de.espirit.firstspirit.access.ModuleAgent;
import de.espirit.firstspirit.access.store.IDProvider;
import de.espirit.firstspirit.access.store.StoreElement;
import de.espirit.firstspirit.module.descriptor.ComponentDescriptor;

import java.lang.reflect.Constructor;
import java.util.*;


public final class Manager {
	
	private final Action                  action;
	private final AdvancedLogger          advancedLogger;
	private final ProjectAppConfiguration configuration;
	private final BaseContext             context;
	private final boolean                 depended;
	private final HandlerController       handlerController;
	private final List<ResolveObject>     resolveList;
	private final List<StoreElement>      startStoreElements;
	private       boolean                 actionable = true;
	private       ValidationStateMap      validationStateMap;
	
	
	public Manager(final BaseContext context, final StoreElement startStoreElement, final Action action) {
		this(context, Collections.singletonList(startStoreElement), false, action);
	}
	
	
	public Manager(final BaseContext context, final StoreElement startStoreElement, final boolean depended, final Action action) {
		this(context, Collections.singletonList(startStoreElement), depended, action);
	}
	
	
	public Manager(final BaseContext context, final List<StoreElement> startStoreElements, final boolean depended, final Action action) {
		this.context = context;
		this.startStoreElements = startStoreElements;
		this.depended = depended;
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
	
	
	public List<IDProvider> getActionables() {
		List<IDProvider> actionables;
		
		if (Action.RELEASE.equals(action)) {
			actionables = validationStateMap.getActionables(false);
		} else {
			actionables = validationStateMap.getActionables(true);
			
			for (StoreElement element : startStoreElements) {
				if (element instanceof IDProvider && !actionables.contains(element)) {
					// Prepend PageRefs to ensure Validity during deletions
					if(element instanceof PageRef) {
						actionables.add(0, (IDProvider) element);
					} else {
						actionables.add((IDProvider) element);
					}
				}
			}
		}
		
		return actionables;
	}
	
	
	public AdvancedLogger getAdvancedLogger() {
		return advancedLogger;
	}
	
	
	public ProjectAppConfiguration getConfiguration() {
		return configuration;
	}
	
	
	public BaseContext getContext() {
		return context;
	}
	
	
	public StoreElement getEqualStartStoreElement(Object handle) {
		return startStoreElements.stream().filter(e -> e.equals(handle)).findFirst().orElse(null);
	}
	
	
	public HandlerController getHandlerController() {
		return handlerController;
	}
	
	
	public ValidationStateList getInvalidValidationStates() {
		ValidationStateList states;
		
		if (Action.RELEASE.equals(action)) {
			states = validationStateMap.getAllValidationStates(ValidationState.Type.RELEASABLE, ValidationState.Type.IGNORED_ON_RELEASE, ValidationState.Type.DEPENDED_RELEASABLE);
		} else {
			states = validationStateMap.getAllValidationStates(ValidationState.Type.DELETABLE, ValidationState.Type.IGNORED_ON_DELETE);
		}
		
		for (StoreElement element : startStoreElements) {
			if (element instanceof IDProvider) {
				Handle handle;
				if (states.size() > 0 ) {
					handle = new DefaultHandle(element, states.get(0).getHandle(), this);
				} else {
					handle = new DefaultHandle(element, null, this);
				}
				states.add(0, ValidationState.createDisplayOnly(handle, (IDProvider) element));
			}
		}
		
		return states;
	}
	
	
	public List<StoreElement> getStartStoreElements() {
		return startStoreElements;
	}
	
	
	public List<ValidationState> getValidationStatesForDisplay(final boolean invalidOnly) {
		List<ValidationState> states;
		
		if (invalidOnly) {
			if (Action.RELEASE.equals(action)) {
				states = validationStateMap.getAllValidationStates(ValidationState.Type.RELEASABLE);
			} else {
				states = validationStateMap.getAllValidationStates(ValidationState.Type.DELETABLE);
			}
		} else {
			states = validationStateMap.getAllValidationStates();
		}
		
		for (StoreElement element : startStoreElements) {
			if (element instanceof IDProvider) {
				Handle handle;
				if (states.size() > 0 ) {
					handle = new DefaultHandle(element, states.get(0).getHandle(), this);
				} else {
					handle = new DefaultHandle(element, null, this);
				}
				states.add(0, ValidationState.createDisplayOnly(handle, (IDProvider) element));
			}
		}
		
		
		return states;
	}
	
	
	public boolean isActionable() {
		return actionable;
	}
	
	
	public boolean isInStartStoreElements(Object handle) {
		return startStoreElements.stream().anyMatch(e -> e.equals(handle));
	}
	
	
	public boolean isNotInStartStoreElements(Object handle) {
		return startStoreElements.stream().noneMatch(e -> e.equals(handle));
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
			List<AbstractHandlerController> controllers = DependencyUtil.getInstancesByType(context, AbstractHandlerController.class);
			
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
		ModuleAgent                     moduleAgent     = context.requireSpecialist(ModuleAgent.TYPE);
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
					defaultHandlers:
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
	public void resolve() {
		advancedLogger.logInfo("Call preResolve()...");
		handlerController.preResolve(this);
		advancedLogger.logInfo("...preResolve() called.");
		
		advancedLogger.logInfo("Iterate over resolveList...");
		
		while (!resolveList.isEmpty()) {
			advancedLogger.logInfo("Currently resolveList has size of " + resolveList.size() + ".");

			// Takes the first element of the resolution list to check.
			ResolveObject resolveObject = resolveList.remove(0);
			Handle        handle        = null;
			
			if (resolveObject.getObject() instanceof Handle) {
				handle = (Handle) resolveObject.getObject();
			} else if (resolveObject.getObject() instanceof StoreElement) {
				handle = new DefaultHandle(resolveObject.getObject(), resolveObject.getPreviousHandle(), this);
			}
			
			if (handle != null && handle.getKeyObject() != null) {
				if (!validationStateMap.contains(handle.getKeyObject())) {
					advancedLogger.logInfo("Check handle " + handle.getKeyObject().toString());
					
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
	}
	
	
	public List<ValidationState> validateData(final Handle handle) {
		List<ValidationState> resultList = new LinkedList<>();
		
		if (handle.getKeyObject() instanceof StoreElement) {
			resultList.addAll(ValidationUtil.validateData(ValidationUtil.createContext(context), handle));
		}
		
		return resultList;
	}
	
	
	@SuppressWarnings("unchecked")
	public List<ValidationState> validateReleased(final Handle handle) {
		List<ValidationState> resultList = new LinkedList<>();
		
		if (isNotInStartStoreElements(handle) && handle.getKeyObject() instanceof IDProvider) {
			if (((IDProvider) handle.getKeyObject()).isReleaseSupported() && !((IDProvider) handle.getKeyObject()).isReleased() && false) {
				if (depended) {
					resultList.add(ValidationState.createDependedReleasable(handle, (IDProvider) handle.getKeyObject()));
				} else {
					resultList.add(ValidationState.createUnreleased(handle, (IDProvider) handle.getKeyObject()));
				}
			} else {
				resultList.add(ValidationState.createReleasable(handle, (IDProvider) handle.getKeyObject()));
			}
		}
		
		return resultList;
	}
	
}
