/* 
 * $Id: JGraphpadLayoutPlugin.java,v 1.1.1.1 2005/08/04 11:21:58 gaudenz Exp $
 * Copyright (c) 2001-2005, Gaudenz Alder
 * 
 * All rights reserved.
 * 
 * See LICENSE file for license details. If you are unable to locate
 * this file please contact info (at) jgraph (dot) com.
 */
package com.jgraph.layoutplugin;

import java.awt.Component;
import java.io.IOException;

import javax.swing.JTabbedPane;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import com.jgraph.JGraphEditor;
import com.jgraph.JGraphpad;
import com.jgraph.editor.JGraphEditorPlugin;
import com.jgraph.editor.JGraphEditorResources;
import com.jgraph.editor.JGraphEditorSettings;
import com.jgraph.editor.factory.JGraphEditorFactoryMethod;
import com.jgraph.pad.factory.JGraphpadPane;

/**
 * Plugin for using JGraph Layout Pro in JGraphpad Pro. Adds a layout panel to
 * the bottom tabs in the main window and layout, select path and select tree
 * actions in the application's edit menu.
 */
public class JGraphpadLayoutPlugin implements JGraphEditorPlugin {

	/**
	 * Defines the path to the UI configuration to be merged into the existing
	 * UI configuration.
	 */
	public static String PATH_UICONFIG = "/com/jgraph/layoutplugin/resources/ui.xml";

	/**
	 * Adds resource bundles.
	 */
	static {
		JGraphEditorResources
				.addBundles(new String[] { "com.jgraph.layoutplugin.resources.strings" });
	}

	/**
	 * Initializes the plugin by registering all factory methods and action
	 * bundles and merging the UI configuration into the main configuration.
	 * 
	 * @param editor
	 *            The enclosing editor for the plugin.
	 * @param configuration
	 *            The object to configure the plugin with.
	 */
	public void initialize(JGraphEditor editor, Node configuration)
			throws ParserConfigurationException, SAXException, IOException {
		editor.getFactory().addMethod(
				new JGraphpadLayoutPanel.FactoryMethod(editor));
		editor.getFactory().addMethod(
				new ExtendedBottomTabFactoryMethod(editor));
		editor.getKit().addBundle(new JGraphpadLayoutAction.AllActions());
		editor.getSettings().add(
				JGraphpad.NAME_UICONFIG,
				JGraphEditorSettings.parse(JGraphEditorResources
						.getInputStream(PATH_UICONFIG)));
	}

	/**
	 * Utility class to add the layout panel to the bottom tab of the main
	 * application window. This factory method replaces the original factory
	 * method and keeps a reference to it. When it is invoked it uses the
	 * original factory method to create the original object and then adds its
	 * own tabs to that object before returning it.
	 */
	public static class ExtendedBottomTabFactoryMethod extends
			JGraphEditorFactoryMethod {

		/**
		 * Reference to factory method that was replaced.
		 */
		protected JGraphEditorFactoryMethod previous = null;

		/**
		 * References the enclosing editor.
		 */
		protected JGraphEditor editor;

		/**
		 * Constructs a new factory method for the specified enclosing editor
		 * using JGraphpadPane.BottomTabFactoryMethod.NAME.
		 * 
		 * @param editor
		 *            The editor that contains the factory method.
		 */
		public ExtendedBottomTabFactoryMethod(JGraphEditor editor) {
			super(JGraphpadPane.BottomTabFactoryMethod.NAME);
			previous = editor.getFactory().getMethod(
					JGraphpadPane.BottomTabFactoryMethod.NAME);
			this.editor = editor;
		}

		/*
		 * (non-Javadoc)
		 */
		public Component createInstance(Node configuration) {
			if (previous != null) {
				Component component = previous.createInstance(configuration);
				if (component instanceof JTabbedPane) {
					JTabbedPane tabPane = (JTabbedPane) component;
					Component layoutPanel = editor.getFactory()
							.executeMethod(
									JGraphpadLayoutPanel.FactoryMethod.NAME,
									configuration);
					if (layoutPanel != null)
						tabPane.addTab(JGraphEditorResources
								.getString("Layout"), layoutPanel);
				}
				return component;
			}
			return null;
		}

	}

}
