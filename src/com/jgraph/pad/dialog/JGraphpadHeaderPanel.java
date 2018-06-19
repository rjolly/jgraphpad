/* 
 * $Id: JGraphpadHeaderPanel.java,v 1.1.1.1 2005/08/04 11:21:58 gaudenz Exp $
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
import java.awt.Font;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;

/**
 * Panel with a right-aligned icon, title and optional subtitle with a gradient
 * background.
 */
public class JGraphpadHeaderPanel extends JGraphpadGradientPanel {

	/**
	 * Constructs a new header panel with no subtitle.
	 * 
	 * @param icon
	 *            The icon to be displayed.
	 * @param title
	 *            The title to be displayed.
	 */
	public JGraphpadHeaderPanel(ImageIcon icon, String title) {
		this(icon, title, null);
	}

	/**
	 * Constructs a new header panel. If substitle is null then the no text area
	 * will be added. Using the textarea allows the subtitle to contain special
	 * characters, such as newlines (\n) and tabs (\t).
	 * 
	 * @param icon
	 *            The icon to be displayed.
	 * @param title
	 *            The title to be displayed.
	 * @param subtitle
	 *            The subtitle to be displayed.
	 */
	public JGraphpadHeaderPanel(ImageIcon icon, String title, String subtitle) {
		super(Color.white);
		setBorder(BorderFactory.createCompoundBorder(BorderFactory
				.createMatteBorder(0, 0, 1, 0, Color.GRAY), BorderFactory
				.createEmptyBorder(8, 8, 8, 8)));
		setLayout(new BorderLayout());
		setOpaque(false);

		// Adds icon
		JLabel iconLabel = new JLabel(icon);
		iconLabel.setOpaque(false);
		add(iconLabel, BorderLayout.EAST);

		// Adds title/subtitle panel
		JPanel textPanel = new JPanel(new BorderLayout());
		textPanel.setOpaque(false);
		add(textPanel, BorderLayout.CENTER);

		// Adds title
		JLabel titleLabel = new JLabel(title);
		titleLabel.setBorder(BorderFactory.createEmptyBorder(4, 0, 0, 0));
		titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD));
		titleLabel.setOpaque(false);
		textPanel.add(titleLabel, BorderLayout.NORTH);

		// Adds optional subtitle
		if (subtitle != null) {
			JTextArea textArea = new JTextArea(subtitle);
			textArea.setOpaque(false);
			textArea.setBorder(BorderFactory.createEmptyBorder(4, 18, 0, 0));
			textArea.setEditable(false);
			textPanel.add(textArea, BorderLayout.CENTER);
		}

	}

}