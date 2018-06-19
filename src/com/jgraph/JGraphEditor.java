/* 
 * $Id: JGraphEditor.java,v 1.1.1.1 2005/08/04 11:21:58 gaudenz Exp $
 * Copyright (c) 2001-2005, Gaudenz Alder
 * 
 * All rights reserved.
 * 
 * See LICENSE file for license details. If you are unable to locate
 * this file please contact info (at) jgraph (dot) com.
 */
package com.jgraph;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

import com.jgraph.editor.JGraphEditorFactory;
import com.jgraph.editor.JGraphEditorKit;
import com.jgraph.editor.JGraphEditorModel;
import com.jgraph.editor.JGraphEditorSettings;

/**
 * Defines the structure of an JGraph editor.
 */
public class JGraphEditor {
	
	/**
	 * Global static product identifier.
	 */
	public static final String VERSION = "JGraphEditor (v6.0.7.4)";
	
	/**
	 * Holds the settings.
	 */
	protected JGraphEditorSettings settings;

	/**
	 * Holds the document model.
	 */
	protected JGraphEditorModel model;

	/**
	 * Holds the editor kit.
	 */
	protected JGraphEditorKit kit;

	/**
	 * Holds the UI factory.
	 */
	protected JGraphEditorFactory factory;

	/**
	 * Constructs an empty editor.
	 */
	public JGraphEditor() {
		this(null);
	}

	/**
	 * Constructs a new editor for the specified settings and model.
	 * 
	 * @param settings
	 *            The settings to use for the editor.
	 */
	public JGraphEditor(JGraphEditorSettings settings) {
		this(settings, null, null, null);
	}

	/**
	 * Constructs a new editor for the specified settings and model.
	 * 
	 * @param settings
	 *            The settings to use for the editor.
	 * @param model
	 *            The model to use as the document model.
	 * @param kit
	 *            The kit to be used to store the actions and tools.
	 * @param factory
	 *            The factory to be used to creat the user interface.
	 */
	public JGraphEditor(JGraphEditorSettings settings, JGraphEditorModel model,
			JGraphEditorKit kit, JGraphEditorFactory factory) {
		this.settings = settings;
		this.model = model;
		this.kit = kit;
		this.factory = factory;
	}

	/**
	 * Implements an application exit. This implementation invokes
	 * {@link JGraphEditorSettings#shutdown()}. Subclassers should call the
	 * superclass method as the first step. Note: This method is never called
	 * from within the framework.
	 */
	public void exit(int code) {
		getSettings().shutdown();
	}

	/**
	 * @return Returns the settings.
	 */
	public JGraphEditorSettings getSettings() {
		return settings;
	}

	/**
	 * @param settings
	 *            The settings to set.
	 */
	public void setSettings(JGraphEditorSettings settings) {
		this.settings = settings;
	}

	/**
	 * @return Returns the factory.
	 */
	public JGraphEditorFactory getFactory() {
		return factory;
	}

	/**
	 * @param factory
	 *            The factory to set.
	 */
	public void setFactory(JGraphEditorFactory factory) {
		this.factory = factory;
	}

	/**
	 * @return Returns the kit.
	 */
	public JGraphEditorKit getKit() {
		return kit;
	}

	/**
	 * @param kit
	 *            The kit to set.
	 */
	public void setKit(JGraphEditorKit kit) {
		this.kit = kit;
	}

	/**
	 * @return Returns the model.
	 */
	public JGraphEditorModel getModel() {
		return model;
	}

	/**
	 * @param model
	 *            The model to set.
	 */
	public void setModel(JGraphEditorModel model) {
		this.model = model;
	}

	/**
	 * Returns true if the specified value is a URL, that is, if it starts with
	 * one of http://, mailto:, ftp://, file:, https://, webdav:// or
	 * webdavs://.
	 * 
	 * @param value
	 *            The value that represents a potential URL.
	 * @return Returns true if <code>value</code> is a URL.
	 */
	public static boolean isURL(Object value) {
		return (value != null && (value.toString().startsWith("http://")
				|| value.toString().startsWith("mailto:")
				|| value.toString().startsWith("ftp://")
				|| value.toString().startsWith("file:")
				|| value.toString().startsWith("https://")
				|| value.toString().startsWith("webdav://") || value.toString()
				.startsWith("webdavs://")));
	}

	/**
	 * Returns a URL representation for the specified file or an empty string if
	 * there was an exception. This method silently ignores exceptions.
	 * 
	 * @param file
	 *            The file to return the URL for.
	 * @return Returns the URL representation of <code>file</code> or an empty
	 *         string.
	 */
	public static String toURL(File file) {
		String result = "";
		try {
			result = file.toURL().toString();
		} catch (MalformedURLException e) {
			// ignore
		}
		return result;
	}

	/**
	 * Returns a URL representation for the specified string or null if there
	 * was an exception. This method silently ignores exceptions.
	 * 
	 * @param s
	 *            The string to return the URL for.
	 * @return Returns the URL representation of <code>string</code> or null.
	 */
	public static URL toURL(String s) {
		try {
			return new URL(String.valueOf(s));
		} catch (MalformedURLException e) {
			// ignore
		}
		return null;
	}
	
	public static void main(String[] args) {
		System.out.println(VERSION);
	}

}
