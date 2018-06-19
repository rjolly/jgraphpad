/* 
 * $Id: JGraphpadMouseAdapter.java,v 1.1.1.1 2005/08/04 11:21:58 gaudenz Exp $
 * Copyright (c) 2001-2005, Gaudenz Alder
 * 
 * All rights reserved.
 * 
 * See LICENSE file for license details. If you are unable to locate
 * this file please contact info (at) jgraph (dot) com.
 */
package com.jgraph.pad.util;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;

import org.w3c.dom.Node;

import com.jgraph.JGraphEditor;
import com.jgraph.JGraphpad;
import com.jgraph.editor.JGraphEditorSettings;

/**
 * Simpify the creation of popup menus.
 * 
 */
public class JGraphpadMouseAdapter extends MouseAdapter {

	/**
	 * References the enclosing editor.
	 */
	protected JGraphEditor editor;

	/**
	 * Holds the name of the configuration to be returned from
	 * {@link JGraphpad#NAME_UICONFIG}.
	 */
	protected String configName;

	/**
	 * Constructs a new mouse adapter for the specified enclosing editor and
	 * popup menu configuration name.
	 * 
	 * @param editor
	 *            The enclosing editor.
	 * @param configName
	 *            The name of the popup menu configuration.
	 */
	public JGraphpadMouseAdapter(JGraphEditor editor, String configName) {
		this.editor = editor;
		this.configName = configName;
	}

	/**
	 * Hook for subclassers to return a configuration for the popup menu.
	 * 
	 * @param event
	 *            The object that describes the event.
	 * @return Returns the configuration for the popup menu.
	 */
	public Node getPopupMenuConfiguration(MouseEvent event) {
		return JGraphEditorSettings.getNodeByName(editor.getSettings()
				.getDocument(JGraphpad.NAME_UICONFIG).getDocumentElement()
				.getChildNodes(), getConfigName());
	}

	/**
	 * Displays a popup menu.
	 * 
	 * @param event
	 *            The object that describes the event.
	 */
	public void mouseReleased(MouseEvent event) {
		if (SwingUtilities.isRightMouseButton(event)) {
			Node configuration = getPopupMenuConfiguration(event);
			if (configuration != null) {
				JPopupMenu popupMenu = editor.getFactory().createPopupMenu(
						configuration);
				popupMenu.setFocusable(false);
				popupMenu
						.show(event.getComponent(), event.getX(), event.getY());
				event.consume();
			}
		}
	}

	/**
	 * @return Returns the configName.
	 */
	public String getConfigName() {
		return configName;
	}

	/**
	 * @param configName
	 *            The configName to set.
	 */
	public void setConfigName(String configName) {
		this.configName = configName;
	}

}
