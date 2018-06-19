/* 
 * $Id: JGraphpadCellViewFactory.java,v 1.1.1.1 2005/08/04 11:21:58 gaudenz Exp $
 * Copyright (c) 2001-2005, Gaudenz Alder
 * 
 * All rights reserved.
 * 
 * See LICENSE file for license details. If you are unable to locate
 * this file please contact info (at) jgraph (dot) com.
 */
package com.jgraph.pad.graph;

import org.jgraph.graph.DefaultCellViewFactory;
import org.jgraph.graph.EdgeView;
import org.jgraph.graph.PortView;
import org.jgraph.graph.VertexView;

/**
 * Cell view factory for all graph layout caches (libraries and diagrams).
 * 
 * @see JGraphpadGraphLayoutCache
 */
public class JGraphpadCellViewFactory extends DefaultCellViewFactory {

	/*
	 * (non-Javadoc)
	 */
	protected EdgeView createEdgeView(Object cell) {
		return new JGraphpadEdgeView(cell);
	}

	/*
	 * (non-Javadoc)
	 */
	protected PortView createPortView(Object cell) {
		return new JGraphpadPortView(cell);
	}

	/*
	 * (non-Javadoc)
	 */
	protected VertexView createVertexView(Object cell) {
		return new JGraphpadVertexView(cell);
	}

}
