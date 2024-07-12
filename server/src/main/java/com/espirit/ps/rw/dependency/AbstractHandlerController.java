package com.espirit.ps.rw.dependency;


import org.jetbrains.annotations.Nullable;

import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;


public abstract class AbstractHandlerController implements HandlerController {
	
	private List<Handler> allHandlers               = new UniqueHandlerList();
	private List<Handler> currentlyIgnoreHandlers   = new UniqueHandlerList();
	private List<Handler> permanentlyIgnoreHandlers = new UniqueHandlerList();
	private List<Handler> loopList                  = new UniqueHandlerList();
	private boolean       breakExecution            = false;
	private boolean       skip                      = false;
	private int           loopIndex                 = 0;
	
	
	@Override
	public final void addHandler(final Handler handler) {
		for (Handler customizedHandler : allHandlers) {
			if (customizedHandler.getClass().equals(handler.getClass())) {
				return;
			}
		}
		
		allHandlers.add(handler);
	}
	
	
	@Override
	public final void breakExecutions() {
		breakExecution = true;
	}
	
	
	@Override
	public final void forEachRemaining(final Consumer<? super Handler> action) {
	}
	
	
	@Override
	public List<Action> getActions() {
		List<Action> actions = new LinkedList<>();
		
		actions.add(Action.RELEASE);
		actions.add(Action.DELETE);
		
		return actions;
	}
	
	
	@Override
	public final List<Handler> getDefaultHandlerByStoreElementType(final Class<?> clazz) {
		List<Handler> resultList = new LinkedList<>();
		
		for (Handler handler : allHandlers) {
			if (((AbstractDefaultHandler) handler).getElementType().equals(clazz)) {
				resultList.add(handler);
			}
		}
		
		return resultList;
	}
	
	
	@Override
	public final boolean hasNext() {
		boolean result = true;
		
		if (skip) {
			loopIndex++;
			skip = false;
			
			return hasNext();
		}
		
		if (breakExecution) {
			resetBeforeIteration();
			result = false;
		} else {
			result &= loopIndex < loopList.size();
		}
		
		return result;
	}
	
	
	@Override
	public final void ignoreHandler(final Handler handler, final boolean permanently) {
		if (permanently) {
			permanentlyIgnoreHandlers.add(handler);
		} else {
			currentlyIgnoreHandlers.add(handler);
		}
	}
	
	
	@Nullable
	@Override
	public final Handler next() {
		if (loopIndex < loopList.size()) {
			return loopList.get(loopIndex++);
		} else {
			return null;
		}
	}
	
	
	@Override
	public void postExecution(final Handle handle, final Manager manager) {
	}
	
	
	@Override
	public void postIteration(final Handle handle, final Manager manager) {
	}
	
	
	@Override
	public void postResolve(final Manager manager) {
	}
	
	
	@Override
	public void preExecution(final Handle handle, final Manager manager) {
	}
	
	
	@Override
	public void preIteration(final Handle handle, final Manager manager) {
	}
	
	
	@Override
	public void preResolve(final Manager manager) {
	}
	
	
	@Override
	public final void remove() {
	}
	
	
	@Override
	public final void resetBeforeIteration() {
		currentlyIgnoreHandlers.clear();
		loopList.clear();
		loopIndex = 0;
		breakExecution = false;
		skip = false;
		
		for (Handler handler : allHandlers) {
			if (!permanentlyIgnoreHandlers.contains(handler) && !currentlyIgnoreHandlers.contains(handler)) {
				loopList.add(handler);
			}
		}
	}
	
	
	@Override
	public final void skipNextHandler() {
		skip = true;
	}
	
	
	private class UniqueHandlerList extends LinkedList<Handler> {
		
		public static final long serialVersionUID = 1L;
		
		
		@Override
		public boolean add(final Handler handler) {
			for (Handler h : this) {
				if (h.getClass().equals(handler.getClass())) {
					return false;
				}
			}
			
			return super.add(handler);
		}
	}
}
