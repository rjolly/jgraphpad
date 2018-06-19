/* 
 * $Id: JGraphpadDiagram.java,v 1.1.1.1 2005/08/04 11:21:58 gaudenz Exp $
 * Copyright (c) 2001-2005, Gaudenz Alder
 * 
 * All rights reserved.
 * 
 * See LICENSE file for license details. If you are unable to locate
 * this file please contact info (at) jgraph (dot) com.
 */
package com.jgraph.pad;

import java.util.Hashtable;
import java.util.Map;

import javax.swing.tree.DefaultMutableTreeNode;

import org.jgraph.graph.GraphLayoutCache;

import com.jgraph.editor.JGraphEditorDiagram;
import com.jgraph.pad.graph.JGraphpadGraphLayoutCache;

/**
 * Represents a diagram to be contained in a {@link JGraphpadFile}. The diagram
 * has a name, default settings for the graph that displays the diagram and a
 * graph layout cache that contains the actual graph data.
 */
public class JGraphpadDiagram extends DefaultMutableTreeNode implements
		JGraphEditorDiagram {

	/**
	 * Holds the graph layout cache that defines the diagram.
	 */
	protected GraphLayoutCache graphLayoutCache;

	/**
	 * Holds the diagram properties.
	 */
	protected Map properties;

	/**
	 * Constructs a new diagram with the specified name and a
	 * {@link JGraphpadGraphLayoutCache} to hold the diagram.
	 * 
	 * @param name
	 *            The name of the new diagram.
	 */
	public JGraphpadDiagram(String name) {
		this(name, new JGraphpadGraphLayoutCache());
	}

	/**
	 * Constructs a new diagram with the specified name using
	 * <code>graphLayoutCache</code> to hold the diagram.
	 * 
	 * @param name
	 *            The name of the diagram.
	 * @param graphLayoutCache
	 *            The graph layout cache that makes up the diagram.
	 */
	public JGraphpadDiagram(String name, GraphLayoutCache graphLayoutCache) {
		this(name, graphLayoutCache, new Hashtable());
	}

	/**
	 * Constructs a new diagram with the specified name using
	 * <code>graphLayoutCache</code> to hold the diagram.
	 * 
	 * @param name
	 *            The name of the diagram.
	 * @param graphLayoutCache
	 *            The graph layout cache that makes up the diagram.
	 * @param properties
	 *            The properties to use for the diagram.
	 */
	public JGraphpadDiagram(String name, GraphLayoutCache graphLayoutCache,
			Map properties) {
		super(name);
		this.graphLayoutCache = graphLayoutCache;
		this.properties = properties;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.jgraph.editor.core.JGraphEditorDiagram#setGraphLayoutCache(org.jgraph.graph.GraphLayoutCache)
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
	 * @see com.jgraph.editor.core.JGraphEditorDiagram#setName(java.lang.String)
	 */
	public void setName(String name) {
		setUserObject(name);
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
	 * Returns the diagram properties.
	 * 
	 * @return Returns the properties.
	 */
	public Map getProperties() {
		return properties;
	}

	/**
	 * Sets the diagram properties.
	 * 
	 * @param properties
	 *            The properties to set.
	 */
	public void setProperties(Map properties) {
		this.properties = properties;
	}

}
