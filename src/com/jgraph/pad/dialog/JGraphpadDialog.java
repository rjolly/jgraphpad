/* 
 * $Id: JGraphpadDialog.java,v 1.1.1.1 2005/08/04 11:21:58 gaudenz Exp $
 * Copyright (c) 2001-2005, Gaudenz Alder
 * 
 * All rights reserved.
 * 
 * See LICENSE file for license details. If you are unable to locate
 * this file please contact info (at) jgraph (dot) com.
 */
package com.jgraph.pad.dialog;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JPanel;

/**
 * Basic dialog class for JGraphpad consisting of the header with a title,
 * subtitle and icon, the content, and the button panel with a set of buttons.
 */
public class JGraphpadDialog extends JDialog {

	/**
	 * Holds the button panel.
	 */
	protected JPanel buttonPanel;

	/**
	 * Constructs a new dialog using the specified title.
	 * 
	 * @param title
	 *            The title to use for the dialog and header.
	 */
	public JGraphpadDialog(String title) {
		this(title, null, null);

	}

	/**
	 * Constructs a new dialog using the specified title. The title is used in
	 * the dialog and also in the header. The header is only displayed if either
	 * an icon or subtitle is specified.
	 * 
	 * @param title
	 *            The title to use for the dialog and header.
	 * @param subtitle
	 *            The subtitle to display in the header.
	 * @param icon
	 *            The icon to display in the header.
	 */
	public JGraphpadDialog(String title, String subtitle, ImageIcon icon) {
		getContentPane().setLayout(new BorderLayout());
		setTitle(title);

		// Adds the header panel
		if (subtitle != null || icon != null) {
			JGraphpadHeaderPanel header = new JGraphpadHeaderPanel(icon, title,
					subtitle);
			getContentPane().add(header, BorderLayout.NORTH);
		}

		// Adds the button panel
		buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		buttonPanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory
				.createMatteBorder(1, 0, 0, 0, Color.GRAY), BorderFactory
				.createEmptyBorder(16, 8, 8, 8)));
		getContentPane().add(buttonPanel, BorderLayout.SOUTH);
		setContent(createContent());
	}

	/**
	 * Hook for subclassers to create the content of the dialog. The value
	 * returned by this method is passed to {@link #setContent(Component)}
	 * within the constructor.
	 * 
	 * @return Returns the content of the dialog.
	 */
	protected JComponent createContent() {
		return null;
	}

	/**
	 * Adds the specified component to the component hierarchy of the dialog.
	 * 
	 * @param content
	 *            The component to add to the hierarchy.
	 */
	public void setContent(Component content) {
		if (content != null)
			getContentPane().add(content, BorderLayout.CENTER);
	}

	/**
	 * Adds the specified buttons to the button panel.
	 * 
	 * @param buttons
	 *            The buttons to be added to the button panel.
	 */
	public void addButtons(JButton[] buttons) {
		for (int i = 0; i < buttons.length; i++)
			buttonPanel.add(buttons[i]);
		buttonPanel.invalidate();
	}

}