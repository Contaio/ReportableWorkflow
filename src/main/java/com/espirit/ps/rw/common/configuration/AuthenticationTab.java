package com.espirit.ps.rw.common.configuration;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class AuthenticationTab {

	private final JTextField     usernameField;
	private final JPasswordField passwordField;
	private       JPanel         panel;


	public AuthenticationTab() {
		this.panel = new JPanel(new BorderLayout());
		this.usernameField = new JTextField();
		this.passwordField = new JPasswordField();

		JLabel hintLabel = new JLabel(getHint());
		hintLabel.setBorder(new EmptyBorder(10, 10, 10, 10));

		JPanel innerPanel = new JPanel();
		innerPanel.setLayout(new BoxLayout(innerPanel, BoxLayout.Y_AXIS));

		final JLabel usernameLabel      = new JLabel("<html><h3>Username:</h3></html>");
		JPanel       usernameLabelPanel = new JPanel();
		usernameLabelPanel.setOpaque(false);
		usernameLabelPanel.setLayout(new BorderLayout(0, 0));
		usernameLabelPanel.add(usernameLabel, BorderLayout.CENTER);
		usernameLabelPanel.setBorder(new EmptyBorder(10, 10, 0, 10));
		innerPanel.add(usernameLabelPanel);

		final JPanel userPanel = new JPanel(new BorderLayout());
		userPanel.setBorder(new EmptyBorder(0, 10, 0, 10));
		userPanel.add(usernameField, BorderLayout.NORTH);
		innerPanel.add(userPanel);

		final JLabel passwordLabel         = new JLabel("<html><h3>Password:</h3></html>");
		JPanel       usernamePasswordLabel = new JPanel();
		usernamePasswordLabel.setOpaque(false);
		usernamePasswordLabel.setLayout(new BorderLayout(0, 0));
		usernamePasswordLabel.add(passwordLabel, BorderLayout.CENTER);
		usernamePasswordLabel.setBorder(new EmptyBorder(0, 10, 0, 10));
		innerPanel.add(usernamePasswordLabel);


		final JPanel passwordPanel = new JPanel(new BorderLayout());
		passwordPanel.setBorder(new EmptyBorder(0, 10, 10, 10));
		passwordPanel.add(passwordField, BorderLayout.NORTH);
		innerPanel.add(passwordPanel);

		this.panel.add(hintLabel, BorderLayout.NORTH);
		this.panel.add(innerPanel, BorderLayout.CENTER);
	}


	public Component getComponent() {
		return this.panel;
	}


	private String getHint() {
		return "<html><h1>Hinweis:</h1><br><p>Die folgenden Felder sind optionale Konfigurationen. Wird der Reportable Workflow durch den SYSTEM-User ausgeführt, kann es zu Problemen bei der Freigabe kommen. Aus diesem Grund können hier Zugangsdaten für einen technischen Benutzer hinterlegt werden.</p></html>";
	}


	public String getName() {
		return "Authentifizierung";
	}


	public String getPassword() {
		return new String(passwordField.getPassword());
	}


	public String getUsername() {
		return usernameField.getText();
	}


	public void setPassword(final String password) {
		passwordField.setText(password);
	}


	public void setUsername(final String username) {
		usernameField.setText(username);
	}
}
