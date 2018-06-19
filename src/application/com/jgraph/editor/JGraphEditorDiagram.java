/* 
 * $Id: JGraphEditorDiagram.java,v 1.1.1.1 2005/08/04 11:21:58 gaudenz Exp $
 * Copyright (c) 2001-2005, Gaudenz Alder
 * 
 * All rights reserved.
 * 
 * See LICENSE file for license details. If you are unable to locate
 * this file please contact info (at) jgraph (dot) com.
 */
package com.jgraph.editor;

import java.util.Map;

import javax.swing.tree.MutableTreeNode;

import org.jgraph.graph.GraphLayoutCache;

/**
 * Defines the basic requirements for a named diagram in a JGraph editor
 * document model. A diagram contains a (stateful) layout cache with a graph
 * model. The properties of document model elements should be changed via the
 * document model in order to update all attached listeners.
 */
public interface JGraphEditorDiagram extends MutableTreeNode {

	/**
	 * Sets the name.
	 * 
	 * @param name
	 *            The name to set.
	 */
	public void setName(String name);

	/**
	 * Returns the name.
	 * 
	 * @return Returns the name.
	 */
	public String getName();

	/**
	 * @param cache
	 *            The cache to set.
	 */
	public void setGraphLayoutCache(GraphLayoutCache cache);

	/**
	 * @return Returns the cache.
	 */
	public GraphLayoutCache getGraphLayoutCache();

	/**
	 * Returns the properties of the diagram.
	 * 
	 * @return Returns all properties.
	 */
	public Map getProperties();

}
