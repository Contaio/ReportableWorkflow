package com.espirit.ps.rw.client;


import com.espirit.moddev.components.annotations.PublicComponent;
import com.espirit.ps.rw.common.AdvancedLogger;
import com.espirit.ps.rw.common.configuration.ProjectAppConfiguration;
import de.espirit.firstspirit.access.BaseContext;
import de.espirit.firstspirit.access.store.IDProvider;
import de.espirit.firstspirit.access.store.Store;
import de.espirit.firstspirit.access.store.contentstore.Content2;
import de.espirit.firstspirit.access.store.contentstore.Dataset;
import de.espirit.firstspirit.agency.ClientServiceRegistryAgent;
import de.espirit.firstspirit.agency.OperationAgent;
import de.espirit.firstspirit.agency.QueryAgent;
import de.espirit.firstspirit.agency.StoreAgent;
import de.espirit.firstspirit.client.plugin.WebeditPermanentPlugin;
import de.espirit.firstspirit.client.plugin.toolbar.ToolbarContext;
import de.espirit.firstspirit.webedit.plugin.WebeditToolbarActionsItemsPlugin;
import de.espirit.firstspirit.webedit.plugin.toolbar.ClientScriptProvidingToolbarActionsItem;
import de.espirit.firstspirit.webedit.plugin.toolbar.WebeditToolbarItem;
import de.espirit.firstspirit.webedit.server.ClientScriptOperation;
import de.espirit.or.EntityList;
import de.espirit.or.Session;
import de.espirit.or.query.Equal;
import de.espirit.or.query.Select;

import java.io.Serializable;
import java.util.*;


/**
 * This class is used to evaluate the current element in the context of the ContentCreator.
 * <p>
 * The interfaces WebeditToolbarActionsItemsPlugin, ClientScriptProvidingToolbarActionsItem are implemented as a workaround.
 * The BaseContext which is given in the <code>setUp()</code> method can not be used to execute a ClientScriptOperation.
 * To fix this problem, we used the ToolbarContext to execute the ClientScript operation in which the Javascript (WE_API)
 * returns the current element to us.
 *
 * @author awolf@e-spirit.com, vaccarisi@e-spirit.com
 */
@PublicComponent(name = "reportable_workflow_web_edit_session", displayName = "WebEditSession")
public class WebEditSession implements WebeditPermanentPlugin, WebeditToolbarActionsItemsPlugin, ClientScriptProvidingToolbarActionsItem {
	
	private ClientServiceRegistryAgent clientServiceRegistry;
	private ClientSession              clientSession;
	private EditorialWebObserver       observer;
	
	
	@Override
	public String getIconPath(final ToolbarContext toolbarContext) {
		return null;
	}
	
	
	@Override
	public Collection<? extends WebeditToolbarItem> getItems() {
		return Collections.singletonList(this);
	}
	
	
	@Override
	public String getLabel(final ToolbarContext toolbarContext) {
		return null;
	}
	
	
	@Override
	public String getScript(final ToolbarContext toolbarContext) {
		return null;
	}
	
	
	@Override
	public boolean isEnabled(final ToolbarContext toolbarContext) {
		if (observer != null) {
			observer.setElement(toolbarContext);
		}
		
		return false;
	}
	
	
	@Override
	public boolean isVisible(final ToolbarContext toolbarContext) {
		return isEnabled(toolbarContext);
	}
	
	
	@Override
	public void setUp(final BaseContext context) {
		if (ProjectAppConfiguration.loadConfiguration(context).isTPPSupported()) {
			observer = new EditorialWebObserver();
		}
		
		clientServiceRegistry = context.requireSpecialist(ClientServiceRegistryAgent.TYPE);
		clientSession = new ClientSession(observer);
		clientServiceRegistry.registerClientService(ClientSession.class, clientSession);
	}
	
	
	@Override
	public void tearDown() {
		if (clientServiceRegistry != null && clientSession != null) {
			clientServiceRegistry.unregisterClientService(clientSession);
		}
	}
	
	private static class EditorialWebObserver implements ClientSession.Observer {
		
		private IDProvider          element;
		private Map<Long, Content2> content2Map = new HashMap<>();
		
		
		private Content2 getContent2(final Long id, final BaseContext context) {
			if (!content2Map.containsKey(id)) {
				Store content2Store = context.requestSpecialist(StoreAgent.TYPE).getStore(Store.Type.CONTENTSTORE);
				content2Store.getChildren(Content2.class, true).forEach(content2 -> content2Map.put(content2.getId(), content2));
			}
			
			return content2Map.get(id);
		}
		
		
		@Override
		public IDProvider getElement() {
			return element;
		}
		
		
		public void setElement(final BaseContext context) {
			ClientScriptOperation operation = context.requestSpecialist(OperationAgent.TYPE).getOperation(ClientScriptOperation.TYPE);
			String                script    = "function() { \n" + "\n" + "\tvar storeType = top.WE_API.Common.getPreviewElement().getStoreType();\n" + "\tvar jsonResult;\n" + "\tif (storeType === \"sitestore\") {\n" + "\t\tjsonResult = { type : \"sitestore\", id : top.WE_API.Common.getPreviewElement().getId()};\n" + "\t} else {\n" + "\t\tjsonResult = { type : \"contentstore\", id : top.WE_API.Common.getPreviewElement().getId(), content2 : top.WE_API.Common.getPreviewElement().getContent2()};\n" + "\t}\n" + "\n" + "console.log(jsonResult);" + "\treturn(jsonResult);\n" + "}";
			Serializable          perform   = operation.perform(script, false);
			if (perform instanceof HashMap) {
				
				@SuppressWarnings("unchecked")
				HashMap<String, Serializable> jsonResult = ((HashMap<String, Serializable>) perform);
				QueryAgent queryAgent = context.requireSpecialist(QueryAgent.TYPE);
				
				if (jsonResult.get("type").equals("sitestore")) {
					Long id = Double.valueOf(jsonResult.get("id").toString()).longValue();
					AdvancedLogger.logInfo(String.format("ID from CC %s", id), getClass());
					Iterator<IDProvider> iterator = queryAgent.answer(String.format("fs.id=%s", id)).iterator();
					if (iterator.hasNext()) {
						element = iterator.next();
						AdvancedLogger.logInfo(String.format("Set the current element as PageRef: %s", element.getUid()), getClass());
					} else {
						AdvancedLogger.logWarning(String.format("Could not find any Element with id: %s", id), getClass());
					}
				} else {
					Long fsId       = Double.valueOf(jsonResult.get("id").toString()).longValue();
					Long content2Id = Double.valueOf(jsonResult.get("content2").toString()).longValue();
					AdvancedLogger.logInfo(String.format("fs_id: %s  and content2Id: %s from CC.", fsId, content2Id), getClass());
					Content2 content2 = getContent2(content2Id, context);
					Session  session  = content2.getSchema().getSession(false);
					Select   select   = session.createSelect(content2.getEntityType().getName());
					select.setConstraint(new Equal("fs_id", fsId));
					EntityList entities = session.executeQuery(select);
					
					if (entities.size() == 1) {
						element = content2.getDataset(entities.get(0));
						AdvancedLogger.logInfo(String.format("Set the current element as Dataset: %s", ((Dataset) element).getEntity().get("fs_id")), getClass());
					} else {
						throw new IllegalArgumentException("Unable to get dataset for fs_id " + fsId);
					}
				}
			}
		}
	}
}
