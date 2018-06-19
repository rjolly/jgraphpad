/* 
 * $Id: JGraphpadL2FPlugin.java,v 1.1.1.1 2005/08/04 11:21:58 gaudenz Exp $
 * Copyright (c) 2001-2005, Gaudenz Alder
 * 
 * All rights reserved.
 * 
 * See LICENSE file for license details. If you are unable to locate
 * this file please contact info (at) jgraph (dot) com.
 */
package com.jgraph.l2fplugin;

import java.awt.Component;

import javax.swing.JTabbedPane;

import org.w3c.dom.Node;

import com.jgraph.JGraphEditor;
import com.jgraph.editor.JGraphEditorFactory;
import com.jgraph.editor.JGraphEditorKit;
import com.jgraph.editor.JGraphEditorPlugin;
import com.jgraph.editor.JGraphEditorResources;
import com.jgraph.editor.factory.JGraphEditorFactoryMethod;
import com.jgraph.pad.factory.JGraphpadPane;

/**
 * Plugin for using L2FProd in JGraphpad Pro. Adds a property sheet to the
 * bottom tabs in the main window and a font action to the format menu.
 */
public class JGraphpadL2FPlugin implements JGraphEditorPlugin {

	/**
	 * Adds resource bundles.
	 */
	static {
		JGraphEditorResources
				.addBundles(new String[] { "com.jgraph.l2fplugin.resources.strings" });
	}

	/**
	 * Initializes the plugin by registering all factory methods and action
	 * bundles.
	 * 
	 * @param editor
	 *            The enclosing editor for the plugin.
	 * @param configuration
	 *            The object to configure the plugin with.
	 */
	public void initialize(JGraphEditor editor, Node configuration) {
		JGraphEditorKit kit = editor.getKit();
		kit.addBundle(new JGraphpadL2FAction.AllActions());
		JGraphEditorFactory factory = editor.getFactory();
		factory.addMethod(new JGraphpadL2FLibraryPane(editor));
		factory.addMethod(new JGraphpadL2FPropertySheet.FactoryMethod());
		editor.getFactory().addMethod(
				new ExtendedBottomTabFactoryMethod(editor));
	}

	/**
	 * Utility class to add the property sheet to the bottom tab of the main
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

					Component properties = editor
							.getFactory()
							.executeMethod(
									JGraphpadL2FPropertySheet.FactoryMethod.NAME);
					if (properties != null)
						tabPane.addTab(JGraphEditorResources
								.getString("Properties"), properties);
				}
				return component;
			}
			return null;
		}

	}

}
