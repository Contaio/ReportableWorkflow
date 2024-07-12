package com.espirit.ps.rw.dataaccess;


import com.espirit.ps.rw.dependency.Action;
import com.espirit.ps.rw.resources.Resources;
import de.espirit.firstspirit.access.BaseContext;
import de.espirit.firstspirit.agency.Image;
import de.espirit.firstspirit.client.plugin.dataaccess.aspects.Reporting;
import org.jetbrains.annotations.Nullable;


public class DataAccessReportingAspect implements Reporting {
	
	private final BaseContext                             context;
	private final String                                  fileNamePrefix;
	private final AbstractValidationStateDataAccessPlugin plugin;
	private final Action                                  action;
	
	
	public DataAccessReportingAspect(final BaseContext context, @Nullable final String fileNamePrefix, final Action action, final AbstractValidationStateDataAccessPlugin plugin) {
		this.context = context;
		this.fileNamePrefix = fileNamePrefix;
		this.action = action;
		this.plugin = plugin;
	}
	
	
	@Override
	public Image<?> getReportIcon(final boolean active) {
		if (fileNamePrefix != null) {
			String filename = "";
			
			filename += fileNamePrefix;
			filename += "-" + action.name().toLowerCase();
			
			if (context.is(BaseContext.Env.WEBEDIT)) {
				filename += active ? "_normal" : "_colored";
			} else {
				filename += active ? "_colored" : "_normal";
			}
			
			filename += ".png";
			
			return Resources.getImageIcon(context, filename, plugin.getClass());
		} else {
			return null;
		}
	}
}