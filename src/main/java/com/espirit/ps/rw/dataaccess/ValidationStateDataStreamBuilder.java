package com.espirit.ps.rw.dataaccess;


import com.espirit.ps.rw.client.ClientSession;
import com.espirit.ps.rw.common.ReportableWorkflowProjectApp;
import com.espirit.ps.rw.common.ReportableWorkflowUtil;
import com.espirit.ps.rw.common.configuration.ProjectAppConfiguration;
import com.espirit.ps.rw.dependency.Action;
import com.espirit.ps.rw.dependency.LogHelper;
import com.espirit.ps.rw.dependency.Manager;
import com.espirit.ps.rw.dependency.ValidationState;
import com.espirit.ps.rw.resources.Resources;
import de.espirit.common.base.Logging;
import de.espirit.firstspirit.access.BaseContext;
import de.espirit.firstspirit.access.store.IDProvider;
import de.espirit.firstspirit.access.store.StoreElement;
import de.espirit.firstspirit.client.plugin.dataaccess.DataStream;
import de.espirit.firstspirit.client.plugin.dataaccess.DataStreamBuilder;
import de.espirit.firstspirit.client.plugin.dataaccess.aspects.Filterable;
import de.espirit.firstspirit.client.plugin.dataaccess.aspects.StreamBuilderAspectMap;
import de.espirit.firstspirit.client.plugin.dataaccess.aspects.StreamBuilderAspectType;
import de.espirit.firstspirit.client.plugin.report.Parameter;
import de.espirit.firstspirit.client.plugin.report.ParameterBoolean;
import de.espirit.firstspirit.client.plugin.report.ParameterMap;
import de.espirit.firstspirit.client.plugin.report.ParameterSelect;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;


public class ValidationStateDataStreamBuilder implements DataStreamBuilder<ValidationState> {

	public static final String CLIENT_SESSION_KEY_MANAGER = "lastDependencyManager";

	private final BaseContext                      context;
	private final StreamBuilderAspectMap           aspects;
	private final ValidationStateDataAccessSession session;

	private boolean showAllItems       = false;
	private boolean depended           = false;
	private boolean recursive          = false;
	private boolean justDependentMedia = false;
	private Manager manager;



	public ValidationStateDataStreamBuilder(final BaseContext context, final ValidationStateDataAccessSession session) {
		this.context = context;
		this.session = session;
		aspects = new StreamBuilderAspectMap();
		aspects.put(Filterable.TYPE, new AllItemsFilter(this, context));
	}


	@Override
	public DataStream<ValidationState> createDataStream() {
		IDProvider idProvider;
		session.getAdvancedLogger().logInfo("Creating DataStream");
		if (session.getCallerChain().isEmpty()) {
			session.getAdvancedLogger().logInfo("CallerChain Empty, Fetching Element from ClientSession");
			idProvider = ClientSession.getElement(context);
		} else {
			session.getAdvancedLogger().logInfo("CallerChain has Elements, fetching: " + LogHelper.getTextIdentification(session.getLastCaller().getElement()));
			idProvider = session.getLastCaller().getElement();
		}

		manager = (Manager) ClientSession.getAndDeleteItem(context, CLIENT_SESSION_KEY_MANAGER, null);

		if (manager == null) {
			List<StoreElement> storeElements;

			if (isRecursive()) {
				storeElements = ReportableWorkflowUtil.collectRecursiveElements(idProvider);
			} else {
				storeElements = Collections.singletonList(idProvider);
			}

			session.getAdvancedLogger().logInfo("Resolving with StoreElementList: " + storeElements.stream().map(o -> LogHelper.getTextIdentification(o) + ", ").collect(Collectors.joining()));
			Logging.logInfo(String.format("Dependency - Full: %s, Just Media: %s", depended, justDependentMedia), getClass());

			manager = new Manager(context, storeElements, depended, justDependentMedia, session.getSessionBuilder().getSession().getSessionBuilder().getPlugin().getAction());
			manager.resolve();
		}

		List<ValidationState> validationStates = manager.getReportRelevantElements(!isShowAllItems());
		session.addCaller(validationStates.remove(0));


		final List<ValidationState> callerChain = session.getCallerChain();
		if (manager.getAction().equals(Action.RELEASE)) {
//			for (int n = 0; n < session.getCallerChain().size(); n++) {
//				for (int m = 0; m < validationStates.size(); m++) {
//					if (Objects.nonNull(session.getCallerChain().get(n)) && Objects.nonNull(session.getCallerChain().get(n).getElement())) {
//						if (session.getCallerChain().get(n).getElement().equals(validationStates.get(m).getElement())) {
//							validationStates.remove(m);
//							break;
//						}
//					}
//				}
//			}
			validationStates.removeAll(callerChain);
		}

		List<ValidationState> resultList = new LinkedList<>();
		resultList.addAll(callerChain);
		resultList.addAll(validationStates);


		//store the first element
		List<ValidationState> subList;
		if (resultList.size() >= 3) {
			ValidationState firstElement = resultList.get(0);
			subList = resultList.subList(1, resultList.size() - 1);
			Collections.sort(subList);
			subList.add(0, firstElement);
		} else {
			subList = resultList;
		}

		session.storeCallerChain();

		return new ValidationStateDataStream(context, subList, this);
	}


	@Override
	public <A> A getAspect(final StreamBuilderAspectType<A> aspectType) {
		return aspects.get(aspectType);
	}


	public Manager getManager() {
		return manager;
	}


	public ValidationStateDataAccessSession getSession() {
		return session;
	}


	public boolean isDepended() {
		return depended;
	}


	public boolean isRecursive() {
		return recursive;
	}


	public boolean isShowAllItems() {
		return showAllItems;
	}


	public void setDepended(final boolean depended) {
		this.depended = depended;
	}


	private void setJustDependentMedia(final boolean justDependentMedia) {
		this.justDependentMedia = justDependentMedia;
	}


	public void setRecursive(final boolean recursive) {
		this.recursive = recursive;
	}


	public void setShowAllItems(final boolean show) {
		this.showAllItems = show;
	}


	public static class AllItemsFilter implements Filterable {

		private static final String                           RESOURCE_KEY_PREFIX  = "data.access.report.filter.";
		private static final String                           ALL_ITEMS            = "all_items";
		private static final String                           DEPENDED             = "depended";
		private static final String                           JUST_DEPENDENT_MEDIA = "justDependentMedia";
		private static final String                           RECURSIVE            = "recursive";
		private final        ProjectAppConfiguration          config;
		private              ParameterBoolean                 parameterShowAllItems;
		private              ParameterBoolean                 parameterRecursive;
		private              ParameterSelect                  parameterDependency;
		private              ValidationStateDataStreamBuilder builder;


		public AllItemsFilter(final ValidationStateDataStreamBuilder builder, final BaseContext context) {
			this.builder = builder;
			this.config = ReportableWorkflowProjectApp.getConfiguration(context);

//			if (DependencyUtil.isTemplateDeveloper(context)) {
//				parameterShowAllItems = Parameter.Factory.createBoolean(ALL_ITEMS, Resources.getLabel(RESOURCE_KEY_PREFIX + ALL_ITEMS, getClass()), false);
//			} else {
//				parameterShowAllItems = null;
//			}



			if (!config.isUseGlobalReportConfEnabled()) {
				parameterShowAllItems = Parameter.Factory.createBoolean(ALL_ITEMS, Resources.getLabel(RESOURCE_KEY_PREFIX + ALL_ITEMS, getClass()), false);
				List<ParameterSelect.SelectItem> itemList = new ArrayList<>();
				itemList.add(Parameter.Factory.createSelectItem(Resources.getLabel(RESOURCE_KEY_PREFIX + DEPENDED, getClass()), DEPENDED));
				itemList.add(Parameter.Factory.createSelectItem(Resources.getLabel(RESOURCE_KEY_PREFIX + JUST_DEPENDENT_MEDIA, getClass()), JUST_DEPENDENT_MEDIA));

				parameterDependency = Parameter.Factory.createSelect(DEPENDED, itemList, DEPENDED);
				parameterRecursive = Parameter.Factory.createBoolean(RECURSIVE, Resources.getLabel(RESOURCE_KEY_PREFIX + RECURSIVE, getClass()), false);
			} else {
				parameterShowAllItems = Parameter.Factory.createBoolean(ALL_ITEMS, Resources.getLabel(RESOURCE_KEY_PREFIX + ALL_ITEMS, getClass()), config.isShowAllItemsEnabled());
			}
		}


		@Override
		public List<Parameter<?>> getDefinedParameters() {
			List<Parameter<?>> parameters = new LinkedList<>();

			parameters.add(parameterShowAllItems);

			if (Objects.nonNull(parameterRecursive)) {
				parameters.add(parameterRecursive);
			}

			if (Objects.nonNull(parameterDependency)) {
				parameters.add(parameterDependency);
			}

			return parameters;
		}


		@Override
		public void setFilter(final ParameterMap parameterMap) {

			if (!config.isUseGlobalReportConfEnabled()) {

				if (parameterShowAllItems != null) {
					builder.setShowAllItems(Boolean.TRUE.equals(parameterMap.get(parameterShowAllItems)));
				}

				if (parameterDependency != null) {
					final String s = parameterMap.get(parameterDependency);
					if (JUST_DEPENDENT_MEDIA.equals(s)) {
						builder.setDepended(false);
						builder.setJustDependentMedia(true);
					} else if (DEPENDED.equals(s)) {
						builder.setDepended(true);
						builder.setJustDependentMedia(false);
					}

				}

				if (parameterRecursive != null) {
					builder.setRecursive(Boolean.TRUE.equals(parameterMap.get(parameterRecursive)));
				}
			} else {
				builder.setShowAllItems(Boolean.TRUE.equals(parameterMap.get(parameterShowAllItems)));
				builder.setRecursive(config.isRecursiveEnabled());
				builder.setJustDependentMedia(config.isJustDependedMediaEnabled());
				builder.setDepended(config.isDependedEnabled());
			}
		}
	}


}
