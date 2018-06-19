/* 
 * $Id: JGraphpadHelpAction.java,v 1.1.1.1 2005/08/04 11:21:58 gaudenz Exp $
 * Copyright (c) 2001-2005, Gaudenz Alder
 * 
 * All rights reserved.
 * 
 * See LICENSE file for license details. If you are unable to locate
 * this file please contact info (at) jgraph (dot) com.
 */
package com.jgraph.pad.action;

import java.awt.event.ActionEvent;

import com.jgraph.JGraphpad;
import com.jgraph.editor.JGraphEditorAction;
import com.jgraph.editor.JGraphEditorResources;
import com.jgraph.pad.dialog.JGraphpadAboutDialog;

/**
 * Implements all actions of the help menu.
 */
public class JGraphpadHelpAction extends JGraphEditorAction {

	/**
	 * Holds the about dialog. Lazy creation.
	 */
	protected JGraphpadAboutDialog aboutDialog;

	/**
	 * Specifies the name for the <code>about</code> action.
	 */
	public static final String NAME_ABOUT = "about";

	/**
	 * Constructs a new help action for the specified name.
	 * 
	 * @param name
	 *            The name of the action to be created.
	 */
	public JGraphpadHelpAction(String name) {
		super(name);
	}

	/**
	 * Executes the action based on the action name.
	 * 
	 * @param event
	 *            The object that describes the event.
	 */
	public void actionPerformed(ActionEvent event) {
		if (getName().equals(NAME_ABOUT))
			doAbout();
	}

	/**
	 * Displays the about dialog.
	 * 
	 * @see JGraphpadAboutDialog
	 */
	protected void doAbout() {
		if (aboutDialog == null)
			aboutDialog = new JGraphpadAboutDialog(JGraphEditorResources
					.getImage(JGraphEditorResources
							.getString("aboutDialog.icon")));
		JGraphpad.center(aboutDialog);
		aboutDialog.setModal(true);
		aboutDialog.setVisible(true);
	}

	/**
	 * Bundle of all actions in this class.
	 */
	public static class AllActions implements Bundle {

		/**
		 * Holds the actions.
		 */
		public JGraphEditorAction actionAbout = new JGraphpadHelpAction(
				NAME_ABOUT);

		/*
		 * (non-Javadoc)
		 */
		public JGraphEditorAction[] getActions() {
			return new JGraphEditorAction[] { actionAbout };
		}

		/*
		 * (non-Javadoc)
		 */
		public void update() {
			// Always enabled
		}

	}

}