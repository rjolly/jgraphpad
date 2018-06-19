/* 
 * $Id: JGraphpadLibrary.java,v 1.2 2005/10/15 16:36:17 gaudenz Exp $
 * Copyright (c) 2001-2005, Gaudenz Alder
 * 
 * All rights reserved.
 * 
 * See LICENSE file for license details. If you are unable to locate
 * this file please contact info (at) jgraph (dot) com.
 */
package com.jgraph.pad;

import java.util.Map;

import org.jgraph.graph.GraphLayoutCache;
import org.jgraph.graph.GraphModel;

import com.jgraph.editor.JGraphEditorDiagram;
import com.jgraph.pad.graph.JGraphpadGraphLayoutCache;

/**
 * Represents a library in the JGraphpad editor. A library is a file and at the
 * same time a diagram which contains a set of groups to be used as template
 * cells.
 */
public class JGraphpadLibrary extends JGraphpadFile implements
		JGraphEditorDiagram {

	/**
	 * Specifies if the library can be changed. Default is false.
	 * Note: There is no way to change this setting via the UI.
	 * Please use a file edit to change the setting within the
	 * XML file that represents this library.
	 */
	protected boolean isReadOnly = false;
	
	/**
	 * Holds the graph layout cache that contains the cells.
	 */
	protected GraphLayoutCache graphLayoutCache;

	/**
	 * Constructs a new library.
	 */
	public JGraphpadLibrary() {
		this(null);
	}

	/**
	 * Constructs a new file using the filename as the user object and a
	 * {@link JGraphpadGraphLayoutCache} to hold the cells.
	 * 
	 * @param filename
	 *            The user object of the parent object.
	 */
	public JGraphpadLibrary(String filename) {
		this(filename, new JGraphpadGraphLayoutCache());
	}

	/**
	 * Constructs a new library with the specified filename as the user object
	 * and the specified graph layout cache to hold the cells.
	 * 
	 * @param filename
	 *            The user object of the parent object.
	 * @param graphLayoutCache
	 *            The graph layout cache that contains the cells.
	 */
	public JGraphpadLibrary(String filename, GraphLayoutCache graphLayoutCache) {
		super(filename);
		this.graphLayoutCache = graphLayoutCache;
	}

	/*
	 * (non-Javadoc)
	 */
	public void setGraphLayoutCache(GraphLayoutCache graphLayoutCache) {
		this.graphLayoutCache = graphLayoutCache;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.jgraph.editor.core.JGraphEditorDiagram#getGraphLayoutCache()
	 */
	public GraphLayoutCache getGraphLayoutCache() {
		return graphLayoutCache;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.jgraph.editor.core.JGraphEditorDiagram#getGraphLayoutCache()
	 */
	public GraphModel getModel() {
		return graphLayoutCache.getModel();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.jgraph.editor.core.JGraphEditorDiagram#setName(java.lang.String)
	 */
	public void setName(String name) {
		setFilename(name);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.jgraph.editor.core.JGraphEditorDiagram#getName()
	 */
	public String getName() {
		return toString();
	}

	/**
	 * @return Returns the isReadOnly.
	 */
	public boolean isReadOnly() {
		return isReadOnly;
	}

	/**
	 * @param isReadOnly The isReadOnly to set.
	 */
	public void setReadOnly(boolean isReadOnly) {
		this.isReadOnly = isReadOnly;
	}

	/*
	 * (non-Javadoc)
	 */
	public Map getProperties() {
		return null;
	}

}
