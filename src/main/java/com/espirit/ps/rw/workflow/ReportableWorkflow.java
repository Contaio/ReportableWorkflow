package com.espirit.ps.rw.workflow;


import com.espirit.ps.rw.client.ClientSession;
import com.espirit.ps.rw.common.AdvancedLogger;
import com.espirit.ps.rw.common.ReportableWorkflowUtil;
import com.espirit.ps.rw.dataaccess.delete.DeleteValidationStateDataAccessPlugin;
import com.espirit.ps.rw.dataaccess.release.ReleaseValidationStateDataAccessPlugin;
import com.espirit.ps.rw.dependency.*;
import com.espirit.ps.rw.resources.Resources;
import de.espirit.common.base.Logging;
import de.espirit.firstspirit.access.BaseContext;
import de.espirit.firstspirit.access.ServicesBroker;
import de.espirit.firstspirit.access.User;
import de.espirit.firstspirit.access.store.IDProvider;
import de.espirit.firstspirit.access.store.StoreElement;
import de.espirit.firstspirit.access.store.contentstore.Content2;
import de.espirit.firstspirit.access.store.contentstore.Dataset;
import de.espirit.firstspirit.agency.OperationAgent;
import de.espirit.firstspirit.agency.SpecialistsBroker;
import de.espirit.firstspirit.agency.StoreElementAgent;
import de.espirit.firstspirit.agency.UserAgent;
import de.espirit.firstspirit.store.operations.DeleteOperation;
import de.espirit.firstspirit.store.operations.ReleaseOperation;
import de.espirit.firstspirit.ui.operations.RequestOperation;
import de.espirit.or.Session;
import de.espirit.or.schema.Entity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Collectors;


public class ReportableWorkflow {

    private final AdvancedLogger advancedLogger;
    private final SpecialistsBroker context;
    private final List<StoreElement> elements;
    private final boolean onlyValidate;
    private final boolean dependentRelease;
    private final Action action;
    private boolean clientContextAvailable;
    private boolean justDependentMediaRelease;


    private ReportableWorkflow(final SpecialistsBroker context, final List<StoreElement> elements,
                               final boolean onlyValidate,
                               final boolean dependentRelease,
                               final boolean justDependentMediaRelease,
                               final Action action) {
        this.context = context;
        this.elements = elements;
        this.onlyValidate = onlyValidate;
        this.dependentRelease = dependentRelease;
        this.justDependentMediaRelease = justDependentMediaRelease;
        this.action = action;
        this.advancedLogger = new AdvancedLogger(context, ReportableWorkflow.class);


        doInitialLogging(context, elements, onlyValidate, dependentRelease, justDependentMediaRelease, action);

        try {
            ClientSession clientSession = context.requireSpecialist(ServicesBroker.TYPE).getService(ClientSession.class);
            clientContextAvailable = clientSession != null;
        } catch (Exception e) {
            clientContextAvailable = false;
        } finally {
            if (clientContextAvailable) {
                advancedLogger.logInfo("Client context available.", true);
            } else {
                advancedLogger.logInfo("No ClientSession / ClientService available, GUI actions will be skipped.", true);
            }
        }
    }

    public static String getIdentifier(final StoreElement element) {
        return ReportableWorkflowUtil.getIdentifier(element, ReportableWorkflow.class);
    }

    public static List<String> getIdentifiers(final List<StoreElement> elements) {
        return elements.stream().map(ReportableWorkflow::getIdentifier).collect(Collectors.toList());
    }

    @NotNull
    private static ReleaseOperation getReleaseOperation(final SpecialistsBroker broker) {
        return broker.requireSpecialist(OperationAgent.TYPE)
                .getOperation(ReleaseOperation.TYPE)
                .dependentReleaseType(IDProvider.DependentReleaseType.DEPENDENT_RELEASE_NEW_AND_CHANGED)
                .ensureAccessibility(true);
    }

    @NotNull
    private static Set<IDProvider> getValidatedIDProviders(final List<ValidationState> validationStates) {
        return validationStates.stream().map(ValidationState::getElement).filter(Objects::nonNull).collect(Collectors.toSet());
    }

    public static Builder newBuilder() {
        AdvancedLogger.logInfo("Create ReportableWorkflow.Builder...", ReportableWorkflow.Builder.class);
        return new Builder();
    }

    private void doInitialLogging(final SpecialistsBroker context, final List<StoreElement> elements, final boolean onlyValidate, final boolean dependentRelease, final boolean justDependentMediaRelease, final Action action) {
        // ONLY LOGGING -->
        StringBuilder message = new StringBuilder();
        message.append("\n-------------------------------------------------------------------------\n");
        message.append("Initiate ReportableWorkflow with following settings:\n");
        message.append("context:\n");
        message.append("\t" + context.getClass()).append("\n");
        message.append("elements:\n");
        for (int i = 0; i < elements.size(); i++) {
            message.append("\t").append(elements.get(i).toString()).append("\n");
        }

        message.append("Parameters:\n");
        message.append("\tonlyValidate:                ").append(onlyValidate).append("\n");
        message.append("\tdepended:                    ").append(dependentRelease).append("\n");
        message.append("\tjustDependentMediaRelease:   ").append(justDependentMediaRelease).append("\n");
        message.append("\taction:                      ").append(action.name()).append("\n");
        message.append("-------------------------------------------------------------------------");
        Logging.logInfo(message.toString(), getClass());
        // <-- ONLY LOGGING
    }

    private void executeAction(final StoreElementAgent storeElementAgent, final List<IDProvider> listOfActionableElements) throws Exception {
        switch (action) {
            case RELEASE:
                // get the unreleased elements
                List<IDProvider> idProviderList = listOfActionableElements.stream()
                        .filter(IDProvider::isReleaseSupported)
                        .filter(idProvider -> !idProvider.isReleased())
                        .collect(Collectors.toList());

                if (!idProviderList.isEmpty()) {
                    ReleaseOperation releaseOperation = getReleaseOperation(context);

                    // collect the already locked elements
                    // in most cases this should contain only one element, which is the current element
                    // of the workflow task
                    Set<IDProvider> locked = new HashSet<>();
                    for (final IDProvider idProvider : idProviderList) {
                        Logging.logInfo("Name: " + idProvider.getName() + ", Type: " + LogHelper.getTextIdentification(idProvider), ReportableWorkflow.class);
                        if (idProvider.isLocked()) {
                            locked.add(idProvider);
                            LockUtil.unlock(idProvider);
                        }
                    }

                    // Release all unreleased elements
                    releaseOperation.perform(idProviderList);

                    // refresh all elements ...
                    idProviderList.forEach(IDProvider::refresh);

                    // set the lock
                    locked.forEach(LockUtil.setLock());
                    Logging.logInfo("Finished with releasing Operation.", ReportableWorkflow.class);
                } else {
                    Logging.logInfo("Nothing to release.", ReportableWorkflow.class);
                }


                break;

            case DELETE:
                Logging.logInfo("Start deleting Operation.", ReportableWorkflow.class);
                // TODO: Fix for PSCOMIN-13, waiting for CORE-12890
                DeleteOperation deleteOperation = getDeleteOperation(context);

                // collect the already locked elements
                // in most cases this should contain only one element, which is the current element
                // of the workflow task
                Set<IDProvider> locked = new HashSet<>();


                // Delete all store elements, no folders
                for (IDProvider idProvider : listOfActionableElements.stream().filter(e -> !e.isFolder()).collect(Collectors.toList())) {
                    Logging.logInfo("Current element which should be deleted: " + idProvider.getName(), ReportableWorkflow.class);

                    Logging.logInfo("Name: " + idProvider.getName() + ", Type: " + LogHelper.getTextIdentification(idProvider), ReportableWorkflow.class);
                    if (idProvider.isLocked()) {
                        locked.add(idProvider);
                        LockUtil.unlock(idProvider);
                    }

                    if (idProvider instanceof Dataset) {
                        LockUtil.lock(idProvider, () -> {
                            Dataset dataset = (Dataset) idProvider;
                            Session releaseSession = dataset.getTableTemplate().getSchema().getSession(true);
                            String uid = dataset.getParent().getUid();
                            Content2 releasedContent2 = (Content2) storeElementAgent.loadStoreElement(uid, Content2.UID_TYPE, true);
                            Entity releasedEntity = releaseSession.find(dataset.getEntity().getKeyValue());

                            if (releasedEntity != null) {
                                if (!releasedContent2.isLocked()) {
                                    releasedContent2.setLock(true, false);
                                }

                                // Deletes the entity (or better the whole dataset)
                                dataset.delete();
                                releasedContent2.save();

                                releaseSession.rollback();
                                releasedEntity.refresh();

                                // Deletes the entity also from the list of release entities.
                                releaseSession.delete(releasedEntity);
                                releaseSession.commit();

                                releasedContent2.setLock(false, false);
                            }
                        });
                    }

                    Logging.logInfo("Deleting: " + idProvider.getId(), this.getClass());
                    LockUtil.lock(idProvider, idProvider::delete);
                }

                // set the lock
                locked.forEach(LockUtil.setLock());

                // TODO: Fix for PSCOMIN-13, waiting for CORE-12890
                // Delete all folders
                Logging.logInfo("Start deleting folders.", ReportableWorkflow.class);
                final IDProvider[] deletableFolders = listOfActionableElements.stream().filter(e -> !e.isDeleted() && e.isFolder()).toArray(IDProvider[]::new);
                for (IDProvider deletableFolder : deletableFolders) {
                    Logging.logInfo("Deleting folder: " + deletableFolder.getName(), ReportableWorkflow.class);
                    deleteOperation.perform(deletableFolder);
                    if (!deletableFolder.isDeleted()) {
                        deletableFolder.delete();
                    }
                }
                break;
        }
    }

    @Nullable
    private DeleteOperation getDeleteOperation(final SpecialistsBroker broker) {
        return broker.requireSpecialist(OperationAgent.TYPE).getOperation(DeleteOperation.TYPE);
    }

    public List<String> getIdentifiers() {
        return getIdentifiers(elements);
    }

    private boolean hasUserPermissions(final User user, final IDProvider idProvider) {
        if (user.getLoginName().equalsIgnoreCase("SYSTEM")) {
            return true;
        }

        // check permission
        switch (action) {
            case RELEASE:
                return idProvider.getPermission(user).canRelease();
            case DELETE:
                return idProvider.getPermission(user).canDelete();
            default:
                return false;
        }
    }

    private List<String> persistIgnoreSetIntoClientSession() {
        List<String> identifiers = new LinkedList<>();
        Set<StoreElement> ignoreSet = new HashSet<>();

        for (StoreElement element : elements) {
            String identifier = ReportableWorkflowUtil.getIdentifier(element, getClass());
            identifiers.add(identifier);
            advancedLogger.logInfo("Add identifier for " + element.toString() + ": " + identifier);

            if (clientContextAvailable) {
                advancedLogger.logInfo("Try to add items from client session...");
                ignoreSet.addAll((Set<StoreElement>) ClientSession.getAndDeleteItem(context, identifier, new HashSet<>()));
                ClientSession.setItem(context, identifier, ignoreSet);
                advancedLogger.logInfo("Items set to client session.");
            }
        }

        return identifiers;
    }

    private void showFailedMessage() {
        final OperationAgent operationAgent = context.requestSpecialist(OperationAgent.TYPE);

        if (Objects.nonNull(operationAgent)) {
            final RequestOperation operation = operationAgent.getOperation(RequestOperation.TYPE);
            if (Objects.nonNull(operation)) {
                operation.setKind(RequestOperation.Kind.ERROR);
                operation.perform(Resources.getLabel("webedit.dialog.open.validation.error", getClass()));
            }
        }
    }

    @SuppressWarnings("unchecked")
    public boolean start() {
        advancedLogger.logInfo("Start ReportableWorkflow...", true);
        List<String> identifiers = persistIgnoreSetIntoClientSession();

        // create instance of Manager
        Manager manager = new Manager(context, elements, dependentRelease, justDependentMediaRelease, action).resolve();

        // get all elements which are relevant for the current workflow
        List<ValidationState> allWorkflowRelevantValidationStatesList = manager.getWorkflowRelevantElements();

        // Set of all through the reportable workflow validated IDProviders
        Set<IDProvider> validatedIDProviders = getValidatedIDProviders(allWorkflowRelevantValidationStatesList);

        // check if all initial elements are validated
        boolean allInitialIDProvidersValidated = elements.stream()
                .filter(element -> element instanceof IDProvider).distinct()
                .map(element -> (IDProvider) element)
                .map(validatedIDProviders::contains)
                .reduce((result, currentVal) -> result && currentVal)
                .orElse(false);

        // combine all validation results
        boolean isValid = allWorkflowRelevantValidationStatesList.stream()
                .filter(Objects::nonNull)
                .map(ValidationState::isPreventing)
                .map(b -> !b)
                .reduce((result, currentElement) -> result && currentElement)
                .orElse(true);


        if (allInitialIDProvidersValidated && isValid) {
            if (onlyValidate) {
                return true;
            }

            User user = context.requireSpecialist(UserAgent.TYPE).getUser();
            StoreElementAgent storeElementAgent = context.requireSpecialist(StoreElementAgent.TYPE);

            // Actionable contains all elements which should be released or deleted.
            List<IDProvider> listOfActionableElements = allWorkflowRelevantValidationStatesList.stream().map(ValidationState::getElement).collect(Collectors.toList());

            // add the initial elements to the list of elements to release
            elements.stream().map(e -> (IDProvider) e).filter(e -> !listOfActionableElements.contains(e)).distinct().forEach(listOfActionableElements::add);

            final List<IDProvider> actionableElements = listOfActionableElements.stream().distinct().collect(Collectors.toList());

            try {
                // pre-action
                List<IDProvider> processableElements = new LinkedList<>();

                preAction:
                for (IDProvider idProvider : actionableElements) {
                    Logging.logInfo(LogHelper.getTextIdentification(idProvider) + " - " + idProvider.getName(), ReportableWorkflow.class);
                    if (hasUserPermissions(user, idProvider)) {
                        processableElements.add(idProvider);
                    } else {
                        if (idProvider.isReleased() || (!idProvider.isReleased() && idProvider.isInReleaseStore())) {
                            continue preAction;
                        }
                        throw new ReportableWorkflowException(Resources.getLabel("workflow.release.executable.error.message.text.missing.permission", getClass()), idProvider);
                    }
                }

                executeAction(storeElementAgent, processableElements);
                return true;
            } catch (ReportableWorkflowException e) {
                if (clientContextAvailable && context instanceof BaseContext) {
                    e.fire((BaseContext) context);
                }
                showFailedMessage();
                return false;
            } catch (Exception e) {
                advancedLogger.logError(e.getMessage(), e, true);
                showFailedMessage();
                return false;
            } finally {
                if (clientContextAvailable) {
                    ClientSession.removeItems(context, identifiers);
                }
            }
        } else if (clientContextAvailable) {
            ClientSession.setItem(context, manager.getClass().getName(), manager);

            switch (action) {
                case RELEASE:
                    ReleaseValidationStateDataAccessPlugin.showReport(context);
                    break;
                case DELETE:
                    DeleteValidationStateDataAccessPlugin.showReport(context);
                    break;
            }
            showFailedMessage();
            return false;
        } else {
            showFailedMessage();
            return false;
        }
    }

    public static final class Builder {

        private SpecialistsBroker context;

        private boolean onlyValidate = false;

        private boolean dependentRelease;
        private boolean justDependentMediaRelease = false;

        private Action action;

        private List<StoreElement> elements;


        private Builder() {
            this.elements = new LinkedList<>();
        }


        public ReportableWorkflow build() {
            if (context == null) {
                throw new IllegalArgumentException("Need context to build instance of ReportableWorkflow.");
            }

            if (action == null) {
                throw new IllegalArgumentException("Need action to build instance of ReportableWorkflow.");
            }

            return new ReportableWorkflow(context, elements, onlyValidate, dependentRelease, justDependentMediaRelease, action);
        }


        public Builder withAction(final Action action) {
            AdvancedLogger.logInfo("Set action '" + action.name() + "' to ReportableWorkflow.Builder.", ReportableWorkflow.Builder.class);

            this.action = action;
            return this;
        }


        public Builder withContext(final SpecialistsBroker broker) {
            AdvancedLogger.logInfo("Context for ReportableWorkflow.Builder: " + broker.getClass(), ReportableWorkflow.Builder.class);

            this.context = broker;
            return this;
        }


        public Builder withDepended(final boolean depended) {
            this.dependentRelease = depended;
            return this;
        }


        public Builder withElement(final StoreElement element) {
            this.elements.add(element);
            return this;
        }


        public Builder withElements(final List<StoreElement> elements) {
            assert elements != null;

            elements.forEach(element -> {
                AdvancedLogger.logInfo("\nAdd following element to ReportableWorkflow.Builder:\n" + element.toString(), ReportableWorkflow.Builder.class);
                this.elements.add(element);
            });

            return this;
        }


        public Builder withJustDependentMedia(final boolean justDependentMediaRelease) {
            this.justDependentMediaRelease = justDependentMediaRelease;
            return this;
        }


        public Builder withOnlyValidate(final boolean onlyValidate) {
            this.onlyValidate = onlyValidate;
            return this;
        }
    }


    public static class ReportableWorkflowException extends Exception {

        public static final long serialVersionUID = 1L;

        private final String message;
        private final IDProvider element;


        private ReportableWorkflowException(final String message, @Nullable final IDProvider element) {
            super(message);
            this.message = message;
            this.element = element;
        }


        public ReportableWorkflowException(final String message, @Nullable final IDProvider element, final Throwable cause) {
            super(message, cause);
            this.message = message;
            this.element = element;
        }


        private void fire(final BaseContext context) {
            RequestOperation message = context.requireSpecialist(OperationAgent.TYPE).getOperation(RequestOperation.TYPE);
            message.setKind(RequestOperation.Kind.ERROR);
            message.setTitle(Resources.getLabel("workflow.release.executable.error.message.title", getClass()));

            message.addOk();
            RequestOperation.Answer jumpAnswer = null;
            if (this.element != null) {
                jumpAnswer = message.addAnswer(Resources.getLabel("workflow.release.executable.error.message.answer.jump.to.element", getClass(), ReportableWorkflowUtil.getDisplayName(context, this.element)));
            }

            RequestOperation.Answer answer = message.perform(this.message);
            if (answer != null && answer.equals(jumpAnswer)) {
                DependencyUtil.jumpToElement(context, this.element, null, null);
            }
        }
    }
}
