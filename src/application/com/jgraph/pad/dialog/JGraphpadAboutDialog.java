/* 
 * $Id: JGraphpadAboutDialog.java,v 1.4 2007/05/24 13:12:26 gaudenz Exp $
 * Copyright (c) 2001-2005, Gaudenz Alder
 * 
 * All rights reserved.
 * 
 * See LICENSE file for license details. If you are unable to locate
 * this file please contact info (at) jgraph (dot) com.
 */
package com.jgraph.pad.dialog;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.KeyStroke;

import org.jgraph.JGraph;

import com.jgraph.JGraphpad;
import com.jgraph.editor.JGraphEditorResources;

/**
 * About dialog for the JGraphpad application.
 */
public class JGraphpadAboutDialog extends JGraphpadDialog
{

	/**
	 * Defines the text used in the copyright tab.
	 */
	protected static final String COPYRIGHT = "JGraphpad Pro\n"
			+ "Copyright (C) 2001-2005 by Gaudenz Alder\n" + JGraphpad.VERSION
			+ "\n" + JGraph.VERSION;

	/**
	 * Defines the text used in the credits tab.
	 */
	protected static final String CREDITS = "Third-party libraries used:\n"
			+ "Batik\n" + "BeanShell\n" + "EPSGraphics\n" + "iText\n"
			+ "L2FProd\n" + "Looks\n";

	/**
	 * Constructs a new about dialog using the specified icon.
	 * 
	 * @param icon
	 *            The icon to display in the dialog header.
	 */
	public JGraphpadAboutDialog(ImageIcon icon)
	{
		super("About JGraphpad Pro",
				"For more information visit http://www.jgraph.com/", icon);
		setSize(640, 480);

		// Adds OK button to close window
		JButton okButton = new JButton(JGraphEditorResources.getString("OK"));
		addButtons(new JButton[] { okButton });
		okButton.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				setVisible(false);
			}
		});

		// Sets default button for enter key
		getRootPane().setDefaultButton(okButton);
	}

	/**
	 * Overrides {@link JGraphpadDialog#createContent()} to provide the actual
	 * content of the dialog.
	 * 
	 * @return Returns the content of the about dialog.
	 */
	protected JComponent createContent()
	{
		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout());
		panel.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
		JTabbedPane tabbedPane = new JTabbedPane();

		// Adds the copyright tab
		JTextArea copyright = new JTextArea();
		copyright.setOpaque(false);
		copyright.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
		copyright.setText(COPYRIGHT);
		tabbedPane.addTab(JGraphEditorResources.getString("Copyright"),
				copyright);

		// Adds the credits tab
		JTextArea credits = new JTextArea();
		credits.setOpaque(false);
		credits.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
		credits.setText(CREDITS);
		tabbedPane.addTab(JGraphEditorResources.getString("Credits"), credits);

		panel.add(tabbedPane, BorderLayout.CENTER);
		return panel;
	}

	/**
	 * Overrides {@link JDialog#createRootPane()} to return a root pane that
	 * hides the window when the user presses the ESCAPE key.O
	 */
	protected JRootPane createRootPane()
	{
		KeyStroke stroke = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);
		JRootPane rootPane = new JRootPane();
		rootPane.registerKeyboardAction(new ActionListener()
		{
			public void actionPerformed(ActionEvent actionEvent)
			{
				setVisible(false);
			}
		}, stroke, JComponent.WHEN_IN_FOCUSED_WINDOW);
		return rootPane;
	}

}