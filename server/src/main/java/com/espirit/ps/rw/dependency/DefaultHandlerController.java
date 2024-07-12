package com.espirit.ps.rw.dependency;


public final class DefaultHandlerController extends AbstractHandlerController {
	
	private static DefaultHandlerController defaultHandlerController = null;
	
	public static DefaultHandlerController getDefaultHandlerController() {
		if (defaultHandlerController == null) {
			defaultHandlerController = new DefaultHandlerController();
		}
		
		return defaultHandlerController;
	}
}
