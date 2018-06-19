/* 
 * $Id: JGraphpadL2FAction.java,v 1.1.1.1 2005/08/04 11:21:58 gaudenz Exp $
 * Copyright (c) 2001-2005, Gaudenz Alder
 * 
 * All rights reserved.
 * 
 * See LICENSE file for license details. If you are unable to locate
 * this file please contact info (at) jgraph (dot) com.
 */
package com.jgraph.l2fplugin;

import java.awt.Component;
import java.awt.Font;

import org.jgraph.JGraph;

import com.jgraph.editor.JGraphEditorAction;
import com.jgraph.pad.action.JGraphpadFormatAction;
import com.l2fprod.common.swing.JFontChooser;

/**
 * Implements all actions that require L2FProd in the classpath.
 */
public class JGraphpadL2FAction extends JGraphpadFormatAction {

	/**
	 * Constructs a new L2F action for the specified name.
	 */
	public JGraphpadL2FAction(String name) {
		super(name);
	}

	/**
	 * Overrides the parent implementation to use
	 * {@link JFontChooser#showDialog(java.awt.Component, java.lang.String, java.awt.Font)}.
	 * 
	 * @param component
	 *            The parent component for the dialog to be displayed.
	 * @param title
	 *            The title of the dialog to be displayed.
	 * @param font
	 *            The default font to use in the dialog.
	 * @return Returns the selected font.
	 */
	public Font fontDialog(Component component, String title, Font font) {
		return JFontChooser.showDialog(component, title, font);
	}

	/**
	 * Bundle of all actions in this class.
	 */
	public static class AllActions implements Bundle {

		/**
		 * Holds the actions.
		 */
		public JGraphEditorAction actionFont = new JGraphpadL2FAction(NAME_FONT);

		/*
		 * (non-Javadoc)
		 */
		public JGraphEditorAction[] getActions() {
			return new JGraphEditorAction[] { actionFont };
		}

		/*
		 * (non-Javadoc)
		 */
		public void update() {
			JGraph graph = getPermanentFocusOwnerGraph();
			boolean isCellsSelected = graph != null
					&& !graph.isSelectionEmpty();

			actionFont.setEnabled(isCellsSelected);
		}

	}

}