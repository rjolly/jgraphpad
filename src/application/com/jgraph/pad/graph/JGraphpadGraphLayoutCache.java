/* 
 * $Id: JGraphpadGraphLayoutCache.java,v 1.1.1.1 2005/08/04 11:21:58 gaudenz Exp $
 * Copyright (c) 2001-2005, Gaudenz Alder
 * 
 * All rights reserved.
 * 
 * See LICENSE file for license details. If you are unable to locate
 * this file please contact info (at) jgraph (dot) com.
 */
package com.jgraph.pad.graph;

import java.util.Set;

import org.jgraph.graph.CellView;
import org.jgraph.graph.CellViewFactory;
import org.jgraph.graph.GraphLayoutCache;
import org.jgraph.graph.GraphModel;

/**
 * GraphLayoutCache to be used in JGraphpad files. Generally, due to the
 * collapse/expand feature present in JGraphpad, all layout caches are partial.
 */
public class JGraphpadGraphLayoutCache extends GraphLayoutCache {

	/**
	 * Constructs a new graph layout cache with a {@link JGraphpadGraphModel}
	 * and partial set to true.
	 */
	public JGraphpadGraphLayoutCache() {
		this(new JGraphpadGraphModel(), true);
	}

	/**
	 * Constructs a new graph layout cache for the specified model.
	 * 
	 * @param model
	 *            The model to contruct the graph layout cache for.
	 * @param partial
	 *            Whether the graph layout cache should be partial.
	 */
	public JGraphpadGraphLayoutCache(GraphModel model, boolean partial) {
		this(model, null, partial);
	}

	/**
	 * Constructs a new graph layout cache which has all its state stored in the
	 * visible set, eg if the cache is partial, but does not contain view-local
	 * attributes.
	 * 
	 * @param model
	 *            The model to constructs the graph layout cache for.
	 * @param visibleSet
	 *            The set of the visible cells.
	 * @param partial
	 *            Whether the graph layout cache should be partial.
	 */
	public JGraphpadGraphLayoutCache(GraphModel model, Set visibleSet,
			boolean partial) {
		super(model, new JGraphpadCellViewFactory(), partial);
		if (visibleSet != null && !visibleSet.isEmpty()) {
			setVisibleImpl(visibleSet.toArray(), true);
			reloadRoots();
			updatePorts();
		}
	}

	/**
	 * Constructs a new graph layout cache for the specified parameters.
	 */
	public JGraphpadGraphLayoutCache(GraphModel model, CellViewFactory factory,
			CellView[] cellViews, CellView[] hiddenCellViews, boolean partial) {
		super(model, factory, cellViews, hiddenCellViews, partial);
	}

	/**
	 * Workaround for the XMLEncoder. The XMLEncoder, for some strange reason,
	 * does not call "isPartial" for this property.
	 */
	public boolean getPartial() {
		return isPartial();
	}

}
