/* 
 * $Id: JGraphpadBshPlugin.java,v 1.1.1.1 2005/08/04 11:21:58 gaudenz Exp $
 * Copyright (c) 2001-2005, Gaudenz Alder
 * 
 * All rights reserved.
 * 
 * See LICENSE file for license details. If you are unable to locate
 * this file please contact info (at) jgraph (dot) com.
 */
package com.jgraph.bshplugin;

import java.awt.Component;

import javax.swing.JTabbedPane;

import org.w3c.dom.Node;

import bsh.EvalError;
import bsh.Interpreter;
import bsh.util.JConsole;

import com.jgraph.JGraphEditor;
import com.jgraph.editor.JGraphEditorPlugin;
import com.jgraph.editor.factory.JGraphEditorFactoryMethod;
import com.jgraph.pad.factory.JGraphpadPane;
import com.jgraph.pad.util.JGraphpadFocusManager;

/**
 * Plugin for using Bsh in JGraphpad Pro. Adds a shell console to the bottom
 * tabs in the main window.
 */
public class JGraphpadBshPlugin implements JGraphEditorPlugin {

	/**
	 * Initializes the plugin by registering all factory methods.
	 * 
	 * @param editor
	 *            The enclosing editor for the plugin.
	 * @param configuration
	 *            The object to configure the plugin with.
	 */
	public void initialize(JGraphEditor editor, Node configuration) {
		editor.getFactory().addMethod(new FactoryMethod(editor));
		editor.getFactory().addMethod(
				new ExtendedBottomTabFactoryMethod(editor));
	}

	/**
	 * Factory method to construct a bsh console.
	 */
	public static class FactoryMethod extends JGraphEditorFactoryMethod {

		/**
		 * Defines the default name for factory methods of this kind.
		 */
		public static String NAME = "createBshConsole";

		/**
		 * References the enclosing editor.
		 */
		protected JGraphEditor editor;

		/**
		 * Constructs a new factory method for the specified enclosing editor
		 * using {@link #NAME}.
		 * 
		 * @param editor
		 *            The editor that contains the factory method.
		 */
		public FactoryMethod(JGraphEditor editor) {
			super(NAME);
			this.editor = editor;
		}

		/*
		 * (non-Javadoc)
		 */
		public Component createInstance(Node configuration) {
			JConsole console = new JConsole();
			Interpreter interpreter = new Interpreter(console);
			try {

				// Assings the editor and mgr variable in the shell so the
				// editor and last focused graph are accessible.
				interpreter.set("editor", editor);
				interpreter.set("mgr", JGraphpadFocusManager
						.getCurrentGraphFocusManager());
			} catch (EvalError e) {
				// ignore
			}
			new Thread(interpreter).start();
			interpreter
					.print("Use the editor and mgr variables, eg. \"print(editor);\""
							+ " to access the enclosing application\n");
			return console;
		}
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

					Component bshPanel = editor.getFactory()
							.executeMethod(FactoryMethod.NAME);
					if (bshPanel != null)
						tabPane.addTab("Shell", bshPanel);

				}
				return component;
			}
			return null;
		}

	}

}
