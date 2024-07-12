package com.espirit.ps.rw.workflow;


import de.espirit.common.base.Logging;
import de.espirit.firstspirit.access.BaseContext;
import de.espirit.firstspirit.access.ServicesBroker;
import de.espirit.firstspirit.access.User;
import de.espirit.firstspirit.access.store.*;
import de.espirit.firstspirit.access.store.contentstore.Content2;
import de.espirit.firstspirit.access.store.contentstore.Dataset;
import de.espirit.firstspirit.access.store.pagestore.Page;
import de.espirit.firstspirit.access.store.templatestore.Template;
import de.espirit.firstspirit.agency.OperationAgent;
import de.espirit.firstspirit.agency.StoreElementAgent;
import de.espirit.firstspirit.agency.UserAgent;
import de.espirit.firstspirit.ui.operations.RequestOperation;
import de.espirit.or.Session;
import de.espirit.or.schema.Entity;

import com.espirit.ps.rw.client.ClientSession;
import com.espirit.ps.rw.common.AdvancedLogger;
import com.espirit.ps.rw.common.ReportableWorkflowUtil;
import com.espirit.ps.rw.dataaccess.delete.DeleteValidationStateDataAccessPlugin;
import com.espirit.ps.rw.dataaccess.release.ReleaseValidationStateDataAccessPlugin;
import com.espirit.ps.rw.dependency.Action;
import com.espirit.ps.rw.dependency.DependencyUtil;
import com.espirit.ps.rw.dependency.Manager;
import com.espirit.ps.rw.dependency.ValidationState;
import com.espirit.ps.rw.dependency.ValidationStateList;
import com.espirit.ps.rw.resources.Resources;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;


public class ReportableWorkflow {

	private final AdvancedLogger advancedLogger;
	private final BaseContext context;
	private final List<StoreElement> elements;
	private final boolean onlyValidate;
	private final boolean depended;
	private final Action action;
	private boolean clientContextAvailable;

	private ReportableWorkflow(final BaseContext context, final List<StoreElement> elements, final boolean onlyValidate, final boolean depended, final Action action) {
		this.context = context;
		this.elements = elements;
		this.onlyValidate = onlyValidate;
		this.depended = depended;
		this.action = action;
		this.advancedLogger = new AdvancedLogger(context, ReportableWorkflow.class);

		// ONLY LOGGING -->
		String message = "Initiate ReportableWorkflow with following settings:";
		message += "\n   context:       " + context.getClass() + ",";

		for (int i = 0; i < elements.size(); i++) {
			if (i == 0) {
				message += "\n   elements:      " + elements.get(i).toString();
			} else {
				message += ",\n                  " + elements.get(i).toString();
			}
		}

		message += "\n   onlyValidate:  " + onlyValidate + ",";
		message += "\n   depended:      " + depended + ",";
		message += "\n   action:        " + action.name() + ",";

		advancedLogger.logInfo(message);
		// <-- ONLY LOGGING

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


	public List<String> getIdentifiers() {
		return getIdentifiers(elements);
	}


	@SuppressWarnings("unchecked")
	public boolean start() {
		advancedLogger.logInfo("Start ReportableWorkflow...", true);

		List<String>      identifiers = new LinkedList<>();
		Set<StoreElement> ignoreSet   = new HashSet<>();

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

		Manager manager = new Manager(context, elements, depended, action);

		manager.resolve();

		ValidationStateList validationStates = manager.getInvalidValidationStates();


		List<IDProvider> validatedIDProviders = validationStates.stream().map(ValidationState::getElement).filter(Objects::nonNull).collect(Collectors.toList());

		boolean allInitialIDProvidersValidated = elements.stream().filter(element -> element instanceof IDProvider).allMatch(validatedIDProviders::contains);

		boolean allStatesNonPreventing = validationStates.isValid();

		if (allInitialIDProvidersValidated && allStatesNonPreventing) {
			if (onlyValidate) {
				return true;
			}

			User              user              = context.requireSpecialist(UserAgent.TYPE).getUser();
			StoreElementAgent storeElementAgent = context.requireSpecialist(StoreElementAgent.TYPE);
			// Actionables contains all elements which should be released or deleted.
			List<IDProvider> actionables = manager.getActionables();
			// lockedList collects all elements which have to be locked before they can be deleted or released. They
			// will be delocked in the finally block in every case.
			List<IDProvider> lockedList = new LinkedList<>();

			try {
				try {

					// pre-action
					preAction:
					for (IDProvider idProvider : actionables.stream().filter(e -> !(e instanceof Template)).collect(Collectors.toList())) {
						boolean executable = false;

						// check permission
						switch (action) {
							case RELEASE:
								if (idProvider.isReleased()) {
									continue preAction;
								}
								executable = idProvider.getPermission(user).canRelease();
								break;
							case DELETE:
								executable = idProvider.getPermission(user).canDelete();
								break;
						}

						if (!executable) {
							throw new ReportableWorkflowException(Resources.getLabel("workflow.release.executable.error.message.text.missing.permission", getClass()), idProvider);
						}

						// lock _element
						try {
							idProvider.refresh();
							idProvider.setLock(true, idProvider instanceof Page);
							lockedList.add(idProvider);
						} catch (LockException e) {
							throw new ReportableWorkflowException(Resources.getLabel("workflow.release.executable.error.message.text.locked.element", getClass()), idProvider, e);
						}
					}

					// action
					action:
					switch (action) {
						case RELEASE:
							for (IDProvider idProvider : actionables) {
								if (!idProvider.isReleased()) {
									if(!(idProvider instanceof StoreElementFolder)) {
										// Releases all children as well if the idProvider is no StoreElementFolder.
										idProvider.release(true);
									} else {
										// Releases only the idProvider if it is a StoreElementFolder.
										idProvider.release(false);
									}

								}
							}
							break;
						case DELETE:
							// TODO: Fix for PSCOMIN-13, waiting for CORE-12890
							// Delete all store elements, no folders
							for (IDProvider idProvider : actionables.stream().filter(e -> !e.isFolder()).collect(Collectors.toList())) {
								if (idProvider instanceof Dataset) {
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
								}
								Logging.logInfo("Deleting:" + idProvider.getId(), this.getClass());
								idProvider.delete();
							}
							// TODO: Fix for PSCOMIN-13, waiting for CORE-12890
							// Delete all folders
							for (final IDProvider idProvider : actionables.stream().filter(e -> !e.isDeleted() && e.isFolder()).collect(Collectors.toList())) {
								idProvider.delete();
							}


							break;
					}

				} catch (ReportableWorkflowException e) {
					throw e;
				} catch (Exception e) {
					throw new ReportableWorkflowException(e.getMessage(), null, e);
				} finally {
					for (IDProvider locked : lockedList) {
						try {
							locked.setLock(false, locked instanceof Page);
						} catch (Exception e) {
							if (!(e instanceof LockException)) {    // ignore LockExceptions
								advancedLogger.logError(e.getMessage(), e, true);
							}
						}
					}
				}

				return true;
			} catch (ReportableWorkflowException e) {
				if (clientContextAvailable) {
					e.fire(context);
				}
				return false;
			} catch (Exception e) {
				advancedLogger.logError(e.getMessage(), e, true);
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

			return false;
		} else {
			return false;
		}
	}


	public static String getIdentifier(final StoreElement element) {
		return ReportableWorkflowUtil.getIdentifier(element, ReportableWorkflow.class);
	}


	public static List<String> getIdentifiers(final List<StoreElement> elements) {
		return elements.stream().map(ReportableWorkflow::getIdentifier).collect(Collectors.toList());
	}


	public static Builder newBuilder() {
		AdvancedLogger.logInfo("Create ReportableWorkflow.Builder...", ReportableWorkflow.Builder.class);

		return new Builder();
	}

	public static final class Builder {

		private BaseContext context;

		private boolean onlyValidate;

		private boolean depended;

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

			return new ReportableWorkflow(context, elements, onlyValidate, depended, action);
		}


		public Builder withAction(final Action action) {
			AdvancedLogger.logInfo("Set action '" + action.name() + "' to ReportableWorkflow.Builder.", ReportableWorkflow.Builder.class);

			this.action = action;
			return this;
		}


		public Builder withContext(final BaseContext context) {
			AdvancedLogger.logInfo("Context for ReportableWorkflow.Builder: " + context.getClass(), ReportableWorkflow.Builder.class);

			this.context = context;
			return this;
		}


		public Builder withDepended(final boolean depended) {
			this.depended = depended;
			return this;
		}


		public Builder withElement(final StoreElement element) {
			this.elements.add(element);
			return this;
		}


		public Builder withElements(final List<StoreElement> elements) {
			assert elements != null;

			elements.forEach(element -> {
				AdvancedLogger.logInfo("Add element '" + element.toString() + "' to ReportableWorkflow.Builder.", ReportableWorkflow.Builder.class);
				this.elements.add(element);
			});

			return this;
		}


		public Builder withOnlyValidate(final boolean onlyValidate) {
			this.onlyValidate = onlyValidate;
			return this;
		}
	}

	private static class ReportableWorkflowException extends Exception {

		public static final long serialVersionUID = 1L;

		private final String     message;
		private final IDProvider element;


		private ReportableWorkflowException(final String message, @Nullable final IDProvider element) {
			super(message);
			this.message = message;
			this.element = element;
		}


		private ReportableWorkflowException(final String message, @Nullable final IDProvider element, final Throwable cause) {
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
