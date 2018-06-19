/* 
 * $Id: JGraphpadPortView.java,v 1.1.1.1 2005/08/04 11:21:58 gaudenz Exp $
 * Copyright (c) 2001-2005, Gaudenz Alder
 * 
 * All rights reserved.
 * 
 * See LICENSE file for license details. If you are unable to locate
 * this file please contact info (at) jgraph (dot) com.
 */
package com.jgraph.pad.graph;

import org.jgraph.graph.EdgeView;
import org.jgraph.graph.PortView;

/**
 * Port view that blocks port magic (optimized locations) for non-rectangular
 * parents
 */
public class JGraphpadPortView extends PortView {

	/**
	 * Empty constructor for persistence.
	 */
	public JGraphpadPortView() {
		super();
	}

	/**
	 * Constructs a new port view for the specified cell.
	 * 
	 * @param cell
	 *            The cell to create the port view for.
	 */
	public JGraphpadPortView(Object cell) {
		super(cell);
	}

	/**
	 * Avoids port magic for non-rectangular shapes.
	 */
	protected boolean shouldInvokePortMagic(EdgeView edge) {
		int shape = JGraphpadGraphConstants.getVertexShape(getParentView()
				.getAllAttributes());
		if (shape != JGraphpadVertexRenderer.SHAPE_RECTANGLE
				&& shape != JGraphpadVertexRenderer.SHAPE_ROUNDED
				&& shape != JGraphpadVertexRenderer.SHAPE_CYLINDER)
			return false;
		return super.shouldInvokePortMagic(edge);
	}

}
