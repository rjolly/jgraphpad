/* 
 * $Id: JGraphpadGraph.java,v 1.1.1.1 2005/08/04 11:21:58 gaudenz Exp $
 * Copyright (c) 2001-2005, Gaudenz Alder
 * 
 * All rights reserved.
 * 
 * See LICENSE file for license details. If you are unable to locate
 * this file please contact info (at) jgraph (dot) com.
 */
package com.jgraph.pad.graph;

import java.awt.event.MouseEvent;

import org.jgraph.JGraph;
import org.jgraph.graph.DefaultGraphModel;
import org.jgraph.graph.GraphLayoutCache;

import com.jgraph.editor.JGraphEditorResources;

/**
 * Graph that provides tooltips.
 */
public class JGraphpadGraph extends JGraph {

	/**
	 * Constructs a new graph for the specified cache.
	 * 
	 * @param cache
	 *            The cache to construct the graph with.
	 */
	public JGraphpadGraph(GraphLayoutCache cache) {
		super(cache);
	}

	/**
	 * Overrides <code>JComponent</code> <code>getToolTipText</code> method
	 * in order to allow the graph to return a tooltip for the topmost cell
	 * under the mouse.
	 * 
	 * @param event
	 *            the <code>MouseEvent</code> that initiated the
	 *            <code>ToolTip</code> display
	 * @return a string containing the tooltip or <code>null</code> if
	 *         <code>event</code> is null
	 */
	public String getToolTipText(MouseEvent event) {
		if (event != null) {
			Object cell = getFirstCellForLocation(event.getX(), event.getY());
			if (cell != null)
				return "<html>" + getToolTipForCell(cell) + "</html>";
		}
		return null;
	}

	/**
	 * Returns a tooltip for the specified cell calling
	 * {@link JGraphpadBusinessObject#getTooltip()} on business objects and
	 * adding information about children, edges, source and target
	 * 
	 * @param cell
	 *            The cell to return the tooltip for.
	 * @return Returns the tooltip for the specified cell.
	 */
	protected String getToolTipForCell(Object cell) {
		String s = "";
		Object userObject = getModel().getValue(cell);
		if (userObject instanceof JGraphpadBusinessObject)
			s += ((JGraphpadBusinessObject) userObject).getTooltip();
		if (getModel().getChildCount(cell) > 0)
			s += JGraphEditorResources.getString("Children") + ": "
					+ getModel().getChildCount(cell) + "<br>";
		int n = DefaultGraphModel.getEdges(getModel(), new Object[] { cell })
				.size();
		if (n > 0)
			s += JGraphEditorResources.getString("Edges") + ": " + n + "<br>";
		if (getModel().getSource(cell) != null)
			s += JGraphEditorResources.getString("Source") + ": "
					+ DefaultGraphModel.getSourceVertex(graphModel, cell)
					+ "<br>";
		if (getModel().getSource(cell) != null)
			s += JGraphEditorResources.getString("Target") + ": "
					+ DefaultGraphModel.getTargetVertex(graphModel, cell)
					+ "<br>";
		return s;
	}

}
