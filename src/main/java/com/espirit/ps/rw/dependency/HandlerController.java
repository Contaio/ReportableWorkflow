package com.espirit.ps.rw.dependency;

import java.util.Iterator;
import java.util.List;

public interface HandlerController extends Iterator<Handler> {
	
	void addHandler(Handler handler);
	
	void breakExecutions();
	
	List<Action> getActions();
	
	List<Handler> getDefaultHandlerByStoreElementType(Class<?> clazz);

	List<Handler> getLoadedHandlers();

	void ignoreHandler(Handler handler, boolean permanently);
	
	void postExecution(Handle handle, Manager manager);
	
	void postIteration(Handle handle, Manager manager);
	
	void postResolve(Manager manager);
	
	void preExecution(Handle handle, Manager manager);
	
	void preIteration(Handle handle, Manager manager);
	
	void preResolve(Manager manager);
	
	void resetBeforeIteration();
	
	void skipNextHandler();
}
