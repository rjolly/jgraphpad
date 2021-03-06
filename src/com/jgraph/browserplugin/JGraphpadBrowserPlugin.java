/* 
 * $Id: JGraphpadBrowserPlugin.java,v 1.1.1.1 2005/08/04 11:21:58 gaudenz Exp $
 * Copyright (c) 2001-2005, Gaudenz Alder
 * 
 * All rights reserved.
 * 
 * See LICENSE file for license details. If you are unable to locate
 * this file please contact info (at) jgraph (dot) com.
 */
package com.jgraph.browserplugin;

import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import com.jgraph.JGraphEditor;
import com.jgraph.JGraphpad;
import com.jgraph.editor.JGraphEditorKit;
import com.jgraph.editor.JGraphEditorPlugin;
import com.jgraph.editor.JGraphEditorResources;
import com.jgraph.editor.JGraphEditorSettings;

/**
 * Plugin for using EPSGraphics in JGraphpad Pro. Adds an saveEPS EPS action.
 */
public class JGraphpadBrowserPlugin implements JGraphEditorPlugin {

	/**
	 * Defines the path to the UI configuration to be merged into the existing
	 * UI configuration.
	 */
	public static String PATH_UICONFIG = "/com/jgraph/browserplugin/resources/ui.xml";

	/**
	 * Adds resource bundles.
	 */
	static {
		JGraphEditorResources
				.addBundles(new String[] { "com.jgraph.browserplugin.resources.strings" });
	}

	/**
	 * Initializes the plugin by registering all action bundles and merging the
	 * UI configuration into the main configuration.
	 * 
	 * @param editor
	 *            The enclosing editor for the plugin.
	 * @param configuration
	 *            The object to configure the plugin with.
	 */
	public void initialize(JGraphEditor editor, Node configuration)
			throws ParserConfigurationException, SAXException, IOException {
		JGraphEditorKit kit = editor.getKit();
		kit.addBundle(new JGraphpadBrowserAction.AllActions());
		editor.getSettings().add(
				JGraphpad.NAME_UICONFIG,
				JGraphEditorSettings.parse(JGraphEditorResources
						.getInputStream(PATH_UICONFIG)));
	}

}
