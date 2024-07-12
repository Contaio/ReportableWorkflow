package com.espirit.ps.rw.dataaccess;


import com.espirit.ps.rw.client.ClientSession;
import com.espirit.ps.rw.common.ReportableWorkflowUtil;
import com.espirit.ps.rw.dependency.*;
import com.espirit.ps.rw.resources.Resources;
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

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;


public class ValidationStateDataStreamBuilder implements DataStreamBuilder<ValidationState> {
	
	public static final String CLIENT_SESSION_KEY_MANAGER = "lastDependencyManager";
	
	private final BaseContext                      context;
	private final StreamBuilderAspectMap           aspects;
	private final ValidationStateDataAccessSession session;
	
	private boolean showAllItems = false;
	private boolean depended     = false;
	private boolean recursive    = false;
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
			session.getAdvancedLogger().logInfo("CallerChain has Elemnts, fetching: "
														+ LogHelper.getTextIdentification(session.getLastCaller().getElement()));
			idProvider = session.getLastCaller().getElement();
		}
		
		manager = (Manager) ClientSession.getAndDeleteItem(context, CLIENT_SESSION_KEY_MANAGER, null);
		
		if (manager == null) {
			List<StoreElement> storeElements;
			
			if (recursive) {
				storeElements = ReportableWorkflowUtil.collectRecursiveElements(idProvider);
			} else {
				storeElements = Collections.singletonList(idProvider);
			}
			session.getAdvancedLogger().logInfo("Resolving with StoreElementList: " + storeElements.stream().map(o -> LogHelper.getTextIdentification(o) + ", ").collect(Collectors.joining()));
			manager = new Manager(context, storeElements, depended, session.getSessionBuilder().getSession().getSessionBuilder().getPlugin().getAction());
			manager.resolve();
		}
		
		List<ValidationState> validationStates = manager.getValidationStatesForDisplay(!showAllItems);
		session.addCaller(validationStates.remove(0));
		
		if (manager.getAction().equals(Action.RELEASE)) {
			for (int n = 0; n < session.getCallerChain().size(); n++) {
				for (int m = 0; m < validationStates.size(); m++) {
					if (session.getCallerChain().get(n).getElement().equals(validationStates.get(m).getElement())) {
						validationStates.remove(m);
						break;
					}
				}
			}
		}
		
		List<ValidationState> resultList = new LinkedList<>();
		resultList.addAll(session.getCallerChain());
		resultList.addAll(validationStates);
		
		//store the first element
		List<ValidationState> subList;
		if (resultList.size() >= 3) {
			ValidationState       firstElement = resultList.get(0);
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
	
	
	public void setRecursive(final boolean recursive) {
		this.recursive = recursive;
	}
	
	
	public void setShowAllItems(final boolean show) {
		showAllItems = show;
	}
	
	public static class AllItemsFilter implements Filterable {
		
		private static final String RESOURCE_KEY_PREFIX = "data.access.report.filter.";
		private static final String ALL_ITEMS           = "all_items";
		private static final String DEPENDED            = "depended";
		private static final String RECURSIVE           = "recursive";
		
		private final ParameterBoolean                 parameterShowAllItems;
		private final ParameterBoolean                 parameterRecursive;
		private final ParameterBoolean                 parameterDepended;
		private final ValidationStateDataStreamBuilder builder;
		
		
		public AllItemsFilter(final ValidationStateDataStreamBuilder builder, final BaseContext context) {
			this.builder = builder;
			
			if (DependencyUtil.isTemplateDeveloper(context)) {
				parameterShowAllItems = Parameter.Factory.createBoolean(ALL_ITEMS, Resources.getLabel(RESOURCE_KEY_PREFIX + ALL_ITEMS, getClass()), false);
			} else {
				parameterShowAllItems = null;
			}
			
			parameterDepended = Parameter.Factory.createBoolean(DEPENDED, Resources.getLabel(RESOURCE_KEY_PREFIX + DEPENDED, getClass()), false);
			parameterRecursive = Parameter.Factory.createBoolean(RECURSIVE, Resources.getLabel(RESOURCE_KEY_PREFIX + RECURSIVE, getClass()), false);
		}
		
		
		@Override
		public List<Parameter<?>> getDefinedParameters() {
			List<Parameter<?>> parameters = new LinkedList<>();
			
			if (parameterShowAllItems != null) {
				parameters.add(parameterShowAllItems);
			}
			
			parameters.add(parameterRecursive);
			parameters.add(parameterDepended);
			
			return parameters;
		}
		
		
		@Override
		public void setFilter(final ParameterMap parameterMap) {
			if (parameterShowAllItems != null) {
				builder.setShowAllItems(parameterMap.get(parameterShowAllItems));
			}
			
			if (parameterDepended != null) {
				builder.setDepended(parameterMap.get(parameterDepended));
			}
			
			if (parameterRecursive != null) {
				builder.setRecursive(parameterMap.get(parameterRecursive));
			}
		}
	}
}
