/* 
 * $Id: JGraphpadBrowserAction.java,v 1.1.1.1 2005/08/04 11:21:58 gaudenz Exp $
 * Copyright (c) 2001-2005, Gaudenz Alder
 * 
 * All rights reserved.
 * 
 * See LICENSE file for license details. If you are unable to locate
 * this file please contact info (at) jgraph (dot) com.
 */
package com.jgraph.browserplugin;

import java.awt.event.ActionEvent;
import java.io.IOException;

import com.jgraph.editor.JGraphEditorAction;
import com.jgraph.pad.dialog.JGraphpadDialogs;

/**
 * Implements all actions that require the browser launcher.
 */
public class JGraphpadBrowserAction extends JGraphEditorAction {

	/**
	 * Shortcut to the shared JGraphpad dialogs.
	 */
	protected static JGraphpadDialogs dlgs = JGraphpadDialogs
			.getSharedInstance();

	/**
	 * Specifies the name for the <code>homepage</code> action.
	 */
	public static final String NAME_HOMEPAGE = "homepage";

	/**
	 * Constructs a new Batik action for the specified name.
	 */
	public JGraphpadBrowserAction(String name) {
		super(name);
	}

	/**
	 * Executes the action based on the action name.
	 * 
	 * @param e
	 *            The object that describes the event.
	 */
	public void actionPerformed(ActionEvent e) {
		try {
			if (getName().equals(NAME_HOMEPAGE))
				JGraphpadBrowserLauncher.openURL("http://www.jgraph.com/");
		} catch (IOException e1) {
			dlgs
					.errorDialog(getPermanentFocusOwner(), e1
							.getLocalizedMessage());
		}
	}

	/**
	 * Bundle of all actions in this class.
	 */
	public static class AllActions implements Bundle {

		/**
		 * Holds the actions. The actions are constructed in the constructor as
		 * they require an editor instance.
		 */
		public JGraphEditorAction actionHomepage = new JGraphpadBrowserAction(
				NAME_HOMEPAGE);

		/*
		 * (non-Javadoc)
		 */
		public JGraphEditorAction[] getActions() {
			return new JGraphEditorAction[] { actionHomepage };
		}

		/*
		 * (non-Javadoc)
		 */
		public void update() {
			// always enabled
		}

	}

}