package com.espirit.ps.rw.common.configuration;

import com.espirit.ps.rw.dependency.Action;

import javax.swing.*;
import java.awt.*;

public class Panel extends JPanel {

	private static final long serialVersionUID = 1L;

	private final Checkbox          releaseReportEnabled;
	private final Combobox          releaseHandlerController;
	private final Table             releaseHandlerTable;
	private final Checkbox          deleteReportEnabled;
	private final Combobox          deleteHandlerController;
	private final Table             deleteHandlerTable;
	private final Checkbox          supportTPP;
	private final Checkbox          enableAdvancedLogging;
	private final AuthenticationTab authenticationTab;
	private       Checkbox          isShowAllItemsEnabled;
	private       Checkbox          enableRecursive;
	private       Combobox          dependencyOption;
	private       Checkbox          useGlobalSettings;


	public Panel(final ProjectAppConfigurationDialog<?> dialog) {
		// labels
		String labelEnabledReport         = "enable report";
		String labelUsedHandlerController = "used HandlerController:";
		String infoMessage                = "If you do not use the default controller, the use of the handlers specified here is not guaranteed.";

		// table data
		String[]   headers             = new String[]{"Module", "Component", "Class", "enabled"};
		Object[][] releaseTableContent = Table.createContent(com.espirit.ps.rw.dependency.Action.RELEASE, dialog);
		Object[][] deleteTableContent  = Table.createContent(Action.DELETE, dialog);

		// components
		com.espirit.ps.rw.common.configuration.Label welcomeMessage = new com.espirit.ps.rw.common.configuration.Label("Use this dialog to configure the specific properties of the reportable workflow.");
		releaseReportEnabled = new Checkbox(labelEnabledReport);
		releaseHandlerController = new Combobox(labelUsedHandlerController, dialog);
		com.espirit.ps.rw.common.configuration.Label releaseTabInfoMessage = new com.espirit.ps.rw.common.configuration.Label(infoMessage, Color.RED, releaseHandlerController.getComponent());
		releaseHandlerTable = new Table(headers, releaseTableContent);
		deleteReportEnabled = new Checkbox(labelEnabledReport);
		deleteHandlerController = new Combobox(labelUsedHandlerController, dialog);
		com.espirit.ps.rw.common.configuration.Label deleteTabInfoMessage = new com.espirit.ps.rw.common.configuration.Label(infoMessage, Color.RED, deleteHandlerController.getComponent());
		deleteHandlerTable = new Table(headers, deleteTableContent);

		Tab releaseTab = new Tab("release", releaseReportEnabled, releaseHandlerController, releaseTabInfoMessage, releaseHandlerTable);
		Tab deleteTab  = new Tab("delete", deleteReportEnabled, deleteHandlerController, deleteTabInfoMessage, deleteHandlerTable);
		this.authenticationTab = new AuthenticationTab();
		TabbedPane tabbedPane = new TabbedPane(releaseTab, deleteTab, authenticationTab);

		supportTPP = new Checkbox("Activate TPP support");
		enableAdvancedLogging = new Checkbox("Activate advanced logging");

		// Reports
		this.isShowAllItemsEnabled = new Checkbox("Show all items, not just the operation preventing ones");
		this.enableRecursive = new Checkbox("Evaluate the dependencies recursively");
		Combobox.Entry nothing           = new Combobox.Entry("notDependent", "Don't collect any dependent elements");
		Combobox.Entry fullyDependent    = new Combobox.Entry("depended", "Collect all dependent elements");
		Combobox.Entry justMediaAndPages = new Combobox.Entry("justDependedMedia", "Collect only media");
		this.dependencyOption = new Combobox("Behaviour when viewing the dependencies", new Combobox.Entry[]{nothing, fullyDependent, justMediaAndPages});
		this.useGlobalSettings = new Checkbox("Use the following settings for the Reports. Hide parametrization in the clients");

		// properties
		setPreferredSize(new Dimension(781, 800));
		setMinimumSize(new Dimension(800, 800));
		setLayout(getMainLayout(this, welcomeMessage, tabbedPane,
				supportTPP, enableAdvancedLogging, useGlobalSettings,
				isShowAllItemsEnabled, enableRecursive, dependencyOption));
	}


	public Combobox getDeleteHandlerController() {
		return deleteHandlerController;
	}


	public Table getDeleteHandlerTable() {
		return deleteHandlerTable;
	}


	public Checkbox getDeleteReportEnabled() {
		return deleteReportEnabled;
	}


	public Combobox getDependencyOption() {
		return dependencyOption;
	}


	public String getPassword() {
		return this.authenticationTab.getPassword();
	}


	public Combobox getReleaseHandlerController() {
		return releaseHandlerController;
	}


	public Table getReleaseHandlerTable() {
		return releaseHandlerTable;
	}


	public Checkbox getReleaseReportEnabled() {
		return releaseReportEnabled;
	}


	public String getUsername() {
		return this.authenticationTab.getUsername();
	}


	public Checkbox isLoggingEnabled() {
		return enableAdvancedLogging;
	}


	public Checkbox isRecursiveEnabled() {
		return enableRecursive;
	}


	public Checkbox isShowAllItemsEnabled() {
		return isShowAllItemsEnabled;
	}


	public Checkbox isTPPSupported() {
		return supportTPP;
	}


	public Checkbox isUSeGlobalSettingsEnabled() {
		return this.useGlobalSettings;
	}


	public void setPassword(final String password) {
		this.authenticationTab.setPassword(password);
	}


	public void setUsername(final String username) {
		this.authenticationTab.setUsername(username);
	}


	public static GroupLayout getMainLayout(final JPanel parent,
											final com.espirit.ps.rw.common.configuration.Label welcomeMessage,
											final TabbedPane tabbedPane,
											final Checkbox supportTPP,
											final Checkbox enableLogging,
											final Checkbox useGlobalSettings,
											final Checkbox isShowAllItemsEnabled,
											final Checkbox isRecursiveEnabled,
											final Combobox dependencyOption
	) {
		final JSeparator firstSeparator  = new JSeparator(SwingConstants.HORIZONTAL);
		final JSeparator secondSeparator = new JSeparator(SwingConstants.HORIZONTAL);
		GroupLayout      groupLayout     = new GroupLayout(parent);
		groupLayout.setHorizontalGroup(
				groupLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
						.addGroup(GroupLayout.Alignment.TRAILING, groupLayout.createSequentialGroup()
								.addContainerGap()
								.addGroup(groupLayout.createParallelGroup(GroupLayout.Alignment.TRAILING)
										.addComponent(welcomeMessage.getComponent(), GroupLayout.Alignment.LEADING, GroupLayout.DEFAULT_SIZE, 788, Short.MAX_VALUE)
										.addComponent(tabbedPane.getComponent(), GroupLayout.Alignment.LEADING, GroupLayout.DEFAULT_SIZE, 788, Short.MAX_VALUE)
										.addGroup(groupLayout.createSequentialGroup()
												.addComponent(firstSeparator))
										.addGroup(GroupLayout.Alignment.LEADING, groupLayout.createSequentialGroup()
												.addComponent(supportTPP.getLabel())
												.addGap(10)
												.addComponent(supportTPP.getComponent()))
										.addGroup(GroupLayout.Alignment.LEADING, groupLayout.createSequentialGroup()
												.addComponent(enableLogging.getLabel())
												.addGap(10)
												.addComponent(enableLogging.getComponent()))
										.addGroup(groupLayout.createSequentialGroup()
												.addComponent(secondSeparator))
										.addGroup(GroupLayout.Alignment.LEADING, groupLayout.createSequentialGroup()
												.addComponent(useGlobalSettings.getLabel())
												.addGap(10)
												.addComponent(useGlobalSettings.getComponent()))
										.addGroup(GroupLayout.Alignment.LEADING, groupLayout.createSequentialGroup()
												.addComponent(isShowAllItemsEnabled.getLabel())
												.addGap(10)
												.addComponent(isShowAllItemsEnabled.getComponent()))
										.addGroup(GroupLayout.Alignment.LEADING, groupLayout.createSequentialGroup()
												.addComponent(isRecursiveEnabled.getLabel())
												.addGap(10)
												.addComponent(isRecursiveEnabled.getComponent()))
										.addGroup(GroupLayout.Alignment.LEADING, groupLayout.createSequentialGroup()
												.addComponent(dependencyOption.getLabel())
												.addGap(10)
												.addComponent(dependencyOption.getComponent()))
								)

								.addContainerGap())
		);
		groupLayout.setVerticalGroup(
				groupLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
						.addGroup(groupLayout.createSequentialGroup()
								.addContainerGap()
								.addComponent(welcomeMessage.getComponent())
								.addGap(20)
								.addComponent(tabbedPane.getComponent(), GroupLayout.DEFAULT_SIZE, 554, Short.MAX_VALUE)
								.addGap(20)
								.addGroup(groupLayout.createSequentialGroup()
										.addComponent(firstSeparator))
								.addGap(20)
								.addGroup(groupLayout.createParallelGroup(GroupLayout.Alignment.CENTER)
										.addComponent(supportTPP.getLabel())
										.addComponent(supportTPP.getComponent(), GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
								.addGap(5)
								.addGroup(groupLayout.createParallelGroup(GroupLayout.Alignment.CENTER)
										.addComponent(enableLogging.getLabel())
										.addComponent(enableLogging.getComponent(), GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
								.addGap(20)
								.addGroup(groupLayout.createSequentialGroup()
										.addComponent(secondSeparator))
								.addGap(20)
								.addGroup(groupLayout.createParallelGroup(GroupLayout.Alignment.CENTER)
										.addComponent(useGlobalSettings.getLabel())
										.addComponent(useGlobalSettings.getComponent(), GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
								.addGap(5)
								.addGroup(groupLayout.createParallelGroup(GroupLayout.Alignment.CENTER)
										.addComponent(isShowAllItemsEnabled.getLabel())
										.addComponent(isShowAllItemsEnabled.getComponent(), GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
								.addGap(5)
								.addGroup(groupLayout.createParallelGroup(GroupLayout.Alignment.CENTER)
										.addComponent(isRecursiveEnabled.getLabel())
										.addComponent(isRecursiveEnabled.getComponent(), GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
								.addGap(5)
								.addGroup(groupLayout.createParallelGroup(GroupLayout.Alignment.CENTER)
										.addComponent(dependencyOption.getLabel())
										.addComponent(dependencyOption.getComponent(), GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
								.addGap(20)
								.addContainerGap())
		);

		return groupLayout;
	}


	public static GroupLayout getPanelLayout(final JPanel parent, final Checkbox enabled, final Combobox controlerSelection, final Label infoMessage, final Table handlerTable) {
		GroupLayout groupLayout = new GroupLayout(parent);
		groupLayout.setHorizontalGroup(
				groupLayout.createParallelGroup(GroupLayout.Alignment.TRAILING)
						.addGroup(groupLayout.createSequentialGroup()
								.addContainerGap()
								.addGroup(groupLayout.createParallelGroup(GroupLayout.Alignment.TRAILING)
										.addComponent(handlerTable.getComponent(), GroupLayout.Alignment.LEADING, GroupLayout.DEFAULT_SIZE, 1124, Short.MAX_VALUE)
										.addComponent(infoMessage.getComponent(), GroupLayout.Alignment.LEADING, GroupLayout.DEFAULT_SIZE, 1124, Short.MAX_VALUE)
										.addGroup(GroupLayout.Alignment.LEADING, groupLayout.createSequentialGroup()
												.addGroup(groupLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
														.addComponent(enabled.getLabel())
														.addComponent(controlerSelection.getLabel()))
												.addGap(18)
												.addGroup(groupLayout.createParallelGroup(GroupLayout.Alignment.TRAILING)
														.addComponent(enabled.getComponent(), 0, 925, Short.MAX_VALUE)
														.addComponent(controlerSelection.getComponent(), GroupLayout.Alignment.LEADING, GroupLayout.DEFAULT_SIZE, 925, Short.MAX_VALUE))))
								.addContainerGap())
		);
		groupLayout.setVerticalGroup(
				groupLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
						.addGroup(groupLayout.createSequentialGroup()
								.addContainerGap()
								.addGroup(groupLayout.createParallelGroup(GroupLayout.Alignment.CENTER)
										.addComponent(enabled.getComponent())
										.addComponent(enabled.getLabel()))
								.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
								.addGroup(groupLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
										.addComponent(controlerSelection.getLabel())
										.addComponent(controlerSelection.getComponent(), GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
								.addGap(18)
								.addComponent(infoMessage.getComponent())
								.addGap(18)
								.addComponent(handlerTable.getComponent(), GroupLayout.DEFAULT_SIZE, 369, Short.MAX_VALUE)
								.addContainerGap())
		);

		return groupLayout;
	}
}
