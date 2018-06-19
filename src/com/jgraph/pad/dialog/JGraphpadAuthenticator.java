/* 
 * $Id: JGraphpadAuthenticator.java,v 1.3 2005/08/07 14:24:57 gaudenz Exp $
 * Copyright (c) 2001-2005, Gaudenz Alder
 * 
 * All rights reserved.
 * 
 * See LICENSE file for license details. If you are unable to locate
 * this file please contact info (at) jgraph (dot) com.
 */
package com.jgraph.pad.dialog;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.net.Authenticator;
import java.net.PasswordAuthentication;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

import com.jgraph.JGraphpad;
import com.jgraph.editor.JGraphEditorResources;

/**
 * Simple authenticator for basic auth.
 */
public class JGraphpadAuthenticator extends Authenticator {

	// This method is called when a password-protected URL is accessed
	protected PasswordAuthentication getPasswordAuthentication() {
		// Get information about the request
		String title = getRequestingPrompt();
		String subtitle = String.valueOf(getRequestingSite());
		AuthDialog auth = new AuthDialog(title, subtitle);
		auth.setSize(320, 240);
		auth.setModal(true);
		JGraphpad.center(auth);
		auth.setVisible(true);

		// Return the information
		return new PasswordAuthentication(auth.getUsername(), auth
				.getPassword());
	}

	/**
	 * Implements the authentication dialog for network connections.
	 */
	public static class AuthDialog extends JGraphpadDialog {

		/**
		 * Holds the username field. Note: createContent is called before the
		 * instance initialization so the instance must be constructed there.
		 */
		protected JTextField username;

		/**
		 * Holds the password field. Note: createContent is called before the
		 * instance initialization so the instance must be constructed there.
		 */
		protected JPasswordField password;

		/**
		 * Constructs a new authentication dialog.
		 * 
		 * @param title
		 *            The title to be displayed.
		 * @param subtitle
		 *            The subtitle to be displayed.
		 */
		public AuthDialog(String title, String subtitle) {
			super(title, subtitle, JGraphEditorResources
					.getImage(JGraphEditorResources
							.getString("authDialog.icon")));

			// Adds OK button to close window
			JButton okButton = new JButton(JGraphEditorResources
					.getString("OK"));
			addButtons(new JButton[] { okButton });
			okButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					dispose();
				}
			});

			// Sets default button for enter key
			getRootPane().setDefaultButton(okButton);

			// Focused the username field when the window
			// becomes visible.
			addWindowListener(new WindowAdapter() {

				/* (non-Javadoc)
				 */
				public void windowOpened(WindowEvent e) {
					username.requestFocus();
				}
			});
		}

		/**
		 * Overrides {@link JGraphpadDialog#createContent()} to provide the
		 * actual content of the dialog.
		 * 
		 * @return Returns the content of the auth dialog.
		 */
		protected JComponent createContent() {
			username = new JTextField(16);
			password = new JPasswordField(16);

			JPanel panel = new JPanel();
			panel.setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));
			panel.setLayout(new GridBagLayout());

			GridBagConstraints c = new GridBagConstraints();
			c.anchor = GridBagConstraints.NORTHWEST;
			c.fill = GridBagConstraints.NONE;
			c.ipadx = 2;
			c.ipady = 2;
			c.weighty = 0.0;
			c.weightx = 1.0;
			c.gridwidth = 1;
			c.gridx = 0;
			c.gridy = 0;
			panel.add(new JLabel(JGraphEditorResources.getString("username")
					+ ":"), c);

			c.gridx = 1;
			c.gridy = 0;
			panel.add(username, c);

			c.gridx = 0;
			c.gridy = 1;
			panel.add(new JLabel(JGraphEditorResources.getString("password")
					+ ":"), c);

			c.gridx = 1;
			c.gridy = 1;
			panel.add(password, c);
			return panel;
		}

		/**
		 * Returns the username from the username field.
		 */
		public String getUsername() {
			return username.getText();
		}

		/**
		 * Returns the password from the password field.
		 */
		public char[] getPassword() {
			return password.getPassword();
		}

	}

}