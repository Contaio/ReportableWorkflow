package com.espirit.ps.rw.client;


import de.espirit.firstspirit.access.BaseContext;
import de.espirit.firstspirit.access.ServicesBroker;
import de.espirit.firstspirit.access.store.IDProvider;
import de.espirit.firstspirit.agency.ClientServiceRegistryAgent;
import de.espirit.firstspirit.client.gui.model.ModelListener;
import de.espirit.firstspirit.client.gui.model.ModelService;
import de.espirit.firstspirit.client.gui.model.selection.SelectionModel;
import de.espirit.firstspirit.client.plugin.JavaClientPermanentPlugin;


public class JavaClientSession implements JavaClientPermanentPlugin {
	
	private ClientServiceRegistryAgent clientServiceRegistry;
	private ClientSession              clientSession;
	private ModelService               modelService;
	private EditorialModelService      editorialModelService;
	
	
	@Override
	public void setUp(final BaseContext context) {
		editorialModelService = new EditorialModelService();
		modelService = context.requireSpecialist(ServicesBroker.TYPE).getService(ModelService.class);
		modelService.addModelListener(SelectionModel.EDITORIAL, editorialModelService);
		
		clientServiceRegistry = context.requireSpecialist(ClientServiceRegistryAgent.TYPE);
		clientSession = new ClientSession(editorialModelService);
		clientServiceRegistry.registerClientService(ClientSession.class, clientSession);
	}
	
	
	@Override
	public void tearDown() {
		if (clientServiceRegistry != null && clientSession != null) {
			clientServiceRegistry.unregisterClientService(clientSession);
		}
		if (modelService != null && editorialModelService != null) {
			modelService.removeModelListener(SelectionModel.EDITORIAL, editorialModelService);
		}
	}
	
	
	private static class EditorialModelService implements ModelListener<SelectionModel>, ClientSession.Observer {
		
		private IDProvider element;
		
		
		@Override
		public IDProvider getElement() {
			return element;
		}
		
		
		@Override
		public void modelChanged(final SelectionModel model) {
			if (model.getElement() != null) {
				element = model.getElement();
			}
		}
	}
}
