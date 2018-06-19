/* 
 * $Id: JGraphpadTWikiPlugin.java,v 1.6 2005/10/09 12:00:04 gaudenz Exp $
 * Copyright (c) 2001-2005, Gaudenz Alder
 * 
 * All rights reserved.
 * 
 * See LICENSE file for license details. If you are unable to locate
 * this file please contact info (at) jgraph (dot) com.
 */
package com.jgraph.twikiplugin;

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
 * Action to embed JGraphpad Pro into a TWiki (or other Webapplication). The
 * upload action requires a -upload URL option and filename to be passed to the
 * editor at startup, the latter also being a URL.<br>
 * If the filename is not found for the URL, a new file is created using the URL
 * as the filename by JGraphpad. <br>
 * The URL option is used to upload 3 files, using the basename of the diagram
 * filename. For example, a diagram filename of
 * http://www.example.com/diagrams/test.xml.gz will result in a basename of
 * test being used to upload test.xml.gz, test.map and test.png. Note that the
 * map file is only uploaded if the image map is not empty, ie. if the diagram's
 * business objects contain at least one property for the name <code>url</code>.
 */
public class JGraphpadTWikiPlugin implements JGraphEditorPlugin {

	/**
	 * Defines the path to the UI configuration to be merged into the existing
	 * UI configuration.
	 */
	public static String PATH_UICONFIG = "/com/jgraph/twikiplugin/resources/ui.xml";

	/**
	 * Adds resource bundles.
	 */
	static {
		JGraphEditorResources
				.addBundles(new String[] { "com.jgraph.twikiplugin.resources.strings" });
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
		kit.addBundle(new JGraphpadTWikiAction.AllActions(editor));
		editor.getSettings().add(
				JGraphpad.NAME_UICONFIG,
				JGraphEditorSettings.parse(JGraphEditorResources
						.getInputStream(PATH_UICONFIG)));

		// Removes some unwanted actions
		// editor.getKit().getActions().remove(JGraphpadFileAction.NAME_SAVE);
		// editor.getKit().getActions()
		// .remove(JGraphpadFileAction.NAME_NEWDIAGRAM);
	}

}
