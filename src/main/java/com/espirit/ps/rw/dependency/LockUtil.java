package com.espirit.ps.rw.dependency;

import com.espirit.ps.rw.resources.Resources;
import com.espirit.ps.rw.workflow.ReportableWorkflow;
import de.espirit.common.base.Logging;
import de.espirit.firstspirit.access.store.ElementDeletedException;
import de.espirit.firstspirit.access.store.IDProvider;
import de.espirit.firstspirit.access.store.LockException;
import de.espirit.firstspirit.access.store.pagestore.Body;
import de.espirit.firstspirit.access.store.pagestore.Page;
import de.espirit.firstspirit.access.store.pagestore.Section;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Objects;
import java.util.function.Consumer;

public class LockUtil {

	public static void lock(final IDProvider idProvider, final LockableAction action) throws Exception {
		IDProvider element = idProvider;
		if (idProvider instanceof Body) {
			element = idProvider.getParent();
		}
		if (idProvider instanceof Section) {
			element = idProvider.getParent();
			if (Objects.nonNull(element)) {
				element = element.getParent();
			}
		}


		try {
			if (Objects.nonNull(element)) {
				if (!element.isLocked() || !element.isLockedOnServer(true)) {
					element.setLock(true, element instanceof Page);
				}
				action.execute();
			}
		} catch (LockException lockException) {
			throw new ReportableWorkflow.ReportableWorkflowException(Resources.getLabel("workflow.release.executable.error.message.text.locked.element", ReportableWorkflow.class), element, lockException);
		} catch (Exception ex) {
			throw ex;
		} finally {
			try {
				if (Objects.nonNull(element) && !element.isDeleted() && element.isLocked()) {
					element.setLock(false, element instanceof Page);
				}
			} catch (LockException lockException) {
				throw new ReportableWorkflow.ReportableWorkflowException(Resources.getLabel("workflow.release.executable.error.message.text.locked.element", ReportableWorkflow.class), element, lockException);
			} catch (ElementDeletedException e) {
				throw e;
			}
		}
	}


	public static Consumer<? super IDProvider> setLock() {
		return idProvider -> {
			try {
				if (idProvider != null && !idProvider.isDeleted() && idProvider.isLocked()) {
					idProvider.setLock(true, idProvider instanceof Page);
				}
			} catch (LockException | ElementDeletedException lockException) {
				StringWriter sw = new StringWriter();
				PrintWriter  pw = new PrintWriter(sw);
				lockException.printStackTrace(pw);
				Logging.logError(sw.toString(), LockUtil.class);
			}
		};
	}


	public static void unlock(final IDProvider idProvider) throws Exception {
		try {
			if (idProvider != null && !idProvider.isDeleted() && idProvider.isLocked()) {
				idProvider.setLock(false, idProvider instanceof Page);
			}
		} catch (LockException lockException) {
			throw new ReportableWorkflow.ReportableWorkflowException(Resources.getLabel("workflow.release.executable.error.message.text.locked.element", ReportableWorkflow.class), idProvider, lockException);
		} catch (ElementDeletedException e) {
			throw e;
		}
	}


	public interface LockableAction {

		void execute() throws Exception;
	}
}