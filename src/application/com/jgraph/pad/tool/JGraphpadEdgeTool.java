/* 
 * $Id: JGraphpadEdgeTool.java,v 1.6 2007/08/30 10:55:12 david Exp $
 * Copyright (c) 2001-2005, Gaudenz Alder
 * 
 * All rights reserved.
 * 
 * See LICENSE file for license details. If you are unable to locate
 * this file please contact info (at) jgraph (dot) com.
 */
package com.jgraph.pad.tool;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;

import org.jgraph.JGraph;
import org.jgraph.graph.DefaultGraphCell;
import org.jgraph.graph.EdgeView;
import org.jgraph.graph.GraphConstants;
import org.jgraph.graph.GraphLayoutCache;
import org.jgraph.graph.PortView;
import org.jgraph.plaf.GraphUI;

/**
 * Tool that inserts edges based on a prototype.
 */
public class JGraphpadEdgeTool extends JGraphpadVertexTool {

	/**
	 * Defines the default name for tools of this kind.
	 */
	public static final String NAME_EDGETOOL = "edgeTool";

	/**
	 * Initial and current port view.
	 */
	protected PortView start, current;

	/**
	 * Constructs a new edge tool for the specified prototype using
	 * {@link #NAME_EDGETOOL}.
	 * 
	 * @param prototype
	 *            The prototype cell to create new edges with.
	 */
	public JGraphpadEdgeTool(Object prototype) {
		this(NAME_EDGETOOL, prototype);
	}

	/**
	 * Constructs a new edge tool for the specified name and prototype.
	 * Edgetools must be always active to be able to click on source/target
	 * vertices without triggering a cell move.
	 * 
	 * @param name
	 *            The name of the tool to be created.
	 * @param prototype
	 *            The prototype cell to create new edges with.
	 */
	public JGraphpadEdgeTool(String name, Object prototype) {
		super(name, prototype);
		setAlwaysActive(true);
	}

	/**
	 * Returns true if the in any case except the selection cell under the mouse
	 * pointer is an edge. This is used to fetch control when a source/target
	 * selection is made for connecting via a new edge.
	 * 
	 * @return Returns false if the selection cell under the mouse is an edge.
	 */
	public boolean isForceMarqueeEvent(MouseEvent event) {
		JGraph graph = getGraphForEvent(event);
		if (graph != null) {
			Object cell = graph.getSelectionCellAt(event.getPoint());
			if (graph.getModel().isEdge(cell))
				return false;
		}
		return true;
	}

	/**
	 * Extends the parent's implementation to find the port view at the mouse
	 * location and set the startPoint accordingly.
	 * 
	 * @param event
	 *            The object that describes the event.
	 */
	public void mousePressed(MouseEvent event) {
		super.mousePressed(event);
		JGraph graph = getGraphForEvent(event);
		if (graph != null) {
			PortView tmp = graph.getPortViewAt(event.getX(), event.getY());
			if (graph.getModel().acceptsSource(previewView.getCell(),
					(tmp != null) ? tmp.getCell() : null)) {
				start = tmp;
				if (start != null)
					startPoint = graph.fromScreen(graph.snap(start
							.getLocation()));
			}
		}
	}

	/**
	 * Overrides the parent's implementation to highlight ports which are under
	 * the mousepointer.
	 * 
	 * @param event
	 *            The object that describes the event.
	 */
	public void mouseMoved(MouseEvent event) {
		JGraph graph = getGraphForEvent(event);
		PortView newPort = graph.getPortViewAt(event.getX(), event.getY());
		if (this.current != newPort
				&& graph.getModel().acceptsSource(prototype,
						(newPort != null) ? newPort.getCell() : null)) {
			Graphics g = graph.getGraphics();

			// Sets the graphics for xor-painting the highlighted port
			// and clears the old graphics by repainting
			Color bg = graph.getBackground();
			Color fg = graph.getMarqueeColor();
			g.setColor(fg);
			g.setXORMode(bg);
			overlay(graph, g, true);

			// Updates the state of the tool and repaints
			this.current = newPort;
			g.setColor(bg);
			g.setXORMode(fg);
			overlay(graph, g, false);
		}
	}

	/**
	 * Overrides the parent's implementation to avoid flickering by checking if
	 * the state of the preview will change.
	 * 
	 * @param event
	 *            The object that describes the event.
	 */
	public void mouseDragged(MouseEvent event) {
		if (startPoint != null) {
			if (event.getSource() instanceof JGraph) {
				JGraph graph = (JGraph) event.getSource();
				PortView newPort = graph.getPortViewAt(event.getX(), event
						.getY());
				if (this.current != newPort || newPort == null) {
					super.mouseDragged(event);
				}
			}
		}
	}

	/**
	 * Overrides the parent's implementation to update the preview to connect to
	 * the port under the mouse or use the location of the mouse as a point.
	 * 
	 * @param event
	 *            The object that describes the event.
	 */
	protected void processMouseDraggedEvent(MouseEvent event) {
		super.processMouseDraggedEvent(event);
		JGraph graph = getGraphForEvent(event);
		if (graph != null) {

			// Checks if a port is at the mouse location and updates the
			// current variable and the current point.
			PortView tmp = graph.getPortViewAt(event.getX(), event.getY());
			if (graph.getModel().acceptsSource(previewView.getCell(),
					(tmp != null) ? tmp.getCell() : null)) {
				current = tmp;
				if (current != null)
					currentPoint = graph.toScreen(current.getLocation());
			}

			// Updates the preview to display the start and end point.
			// This uses the fact that the start and current point are
			// up-do-date.
			if (previewView instanceof EdgeView) {
				EdgeView edge = (EdgeView) previewView;
				Point2D scaledStart = graph.fromScreen(graph
						.snap((Point2D) startPoint.clone()));
				Point2D scaledCurrent = graph.fromScreen(graph
						.snap((Point2D) currentPoint.clone()));

				// Sets the points in-place. The update call makes sure
				// all routing is applied.
				edge.setPoint(0, scaledStart);
				edge.setPoint(edge.getPointCount() - 1, scaledCurrent);
				edge.setSource(start);
				edge.setTarget(current);
				edge.update(graph.getGraphLayoutCache());
			}
		}
	}

	/**
	 * Extends the parent's implementation to reset {@link #start} and
	 * {@link #current}.
	 * 
	 * @param event
	 *            The object that describes the event.
	 */
	public void mouseReleased(MouseEvent event) {
		super.mouseReleased(event);
		start = null;
		current = null;
	}

	/**
	 * Overrides the parent's implementation to insert the specified edge into
	 * <code>cache</code>.
	 * 
	 * @param cache
	 *            The cache into which to insert the edge.
	 * @param edge
	 *            The edge to be inserted into <code>cache</code>.
	 */
	protected void execute(JGraph graph, Object edge) {
		GraphLayoutCache cache = graph.getGraphLayoutCache();
		// Uses the cached points for the new edge
		if (previewView instanceof EdgeView) {
			// Only add the start and end points into the new edge points list
			// Otherwise, routed control points would be copied over
			List newPoints = new ArrayList();
			List previewPoints = ((EdgeView) previewView).getPoints();
			newPoints.add(previewPoints.get(0));
			newPoints.add(previewPoints.get(previewPoints.size()-1));
			GraphConstants.setPoints(cache.getModel().getAttributes(edge), newPoints);
		}
		Object source = (start != null) ? start.getCell() : null;
		Object target = (current != null) ? current.getCell() : null;
		cache.insertEdge(edge, source, target);
	}

	/**
	 * Extends the parent's implementation to draw the highlighted port using
	 * {@link #paintPort(JGraph, Graphics)}.
	 * 
	 * @param graph
	 *            The graph to paint in.
	 * @param g
	 *            The graphics to use for paiting.
	 * @param clear
	 *            Wether to clear the display.
	 */
	public void overlay(JGraph graph, Graphics g, boolean clear) {
		paintPort(graph, g);
		super.overlay(graph, g, clear);
	}

	/**
	 * Paints the {@link #current} port in highlighted state.
	 * 
	 * @param graph
	 *            The graph to paint the port in.
	 * @param g
	 *            The graphics to use for paiting.
	 */
	protected void paintPort(JGraph graph, Graphics g) {
		if (current != null && graph != null) {
			boolean offset = (GraphConstants.getOffset(current
					.getAllAttributes()) != null);
			Rectangle2D r = (offset) ? current.getBounds() : current
					.getParentView().getBounds();
			r = graph.toScreen((Rectangle2D) r.clone());
			int s = 3;
			r.setFrame(r.getX() - s, r.getY() - s, r.getWidth() + 2 * s, r
					.getHeight()
					+ 2 * s);
			GraphUI ui = (GraphUI) graph.getUI();
			ui.paintCell(g, current, r, true);
		}
	}

}