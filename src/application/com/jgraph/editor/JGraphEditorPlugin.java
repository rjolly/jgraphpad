/* 
 * $Id: JGraphEditorPlugin.java,v 1.1.1.1 2005/08/04 11:21:58 gaudenz Exp $
 * Copyright (c) 2001-2005, Gaudenz Alder
 * 
 * All rights reserved.
 * 
 * See LICENSE file for license details. If you are unable to locate
 * this file please contact info (at) jgraph (dot) com.
 */
package com.jgraph.editor;

import org.w3c.dom.Node;

import com.jgraph.JGraphEditor;

/**
 * Defines the requirements for a class that may be used define addon
 * functionality for a JGraph editor.
 */
public interface JGraphEditorPlugin {

	/**
	 * Initializes the plugin for the specified editor using
	 * <code>configuration</code>
	 * 
	 * @param editor
	 *            The editor to initialize the plugin for.
	 * @param configuration
	 *            The configuration for the plugin.
	 * 
	 * @throws Exception
	 *             To indicate that the plugin could not be initialized.
	 */
	public void initialize(JGraphEditor editor, Node configuration)
			throws Exception;

}
