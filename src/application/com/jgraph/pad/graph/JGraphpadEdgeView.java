/* 
 * $Id: JGraphpadEdgeView.java,v 1.5 2007/03/25 13:10:43 david Exp $
 * Copyright (c) 2001-2005, Gaudenz Alder
 * 
 * All rights reserved.
 * 
 * See LICENSE file for license details. If you are unable to locate
 * this file please contact info (at) jgraph (dot) com.
 */
package com.jgraph.pad.graph;

import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.Map;

import javax.swing.tree.DefaultMutableTreeNode;

import org.jgraph.graph.CellHandle;
import org.jgraph.graph.CellView;
import org.jgraph.graph.CellViewRenderer;
import org.jgraph.graph.EdgeRenderer;
import org.jgraph.graph.EdgeView;
import org.jgraph.graph.GraphCellEditor;
import org.jgraph.graph.GraphConstants;
import org.jgraph.graph.GraphContext;

/**
 * Edge view that supports moveable ports. Note: The offset for the port depends
 * on the edge, and is therefore stored in the edge's attributes. It is not
 * required to provide a custom port view, as the port offset is returned from
 * within the overridden {@link #getNearestPoint(boolean)}.
 */
public class JGraphpadEdgeView extends EdgeView {
	
	/**
	 * Holds the static editor for views of this kind.
	 */
	public static JGraphpadRichTextEditor editor = new JGraphpadRichTextEditor();

	/**
	 * Holds the static editor for views of this kind.
	 */
	public static JGraphpadRedirectingEditor redirector = new JGraphpadRedirectingEditor();
	
	/**
	 * Holds the static renderer for views of this kind.
	 */
	public static EdgeRenderer renderer = new JGraphpadEdgeRenderer();


	/**
	 * Empty constructor for persistence.
	 */
	public JGraphpadEdgeView() {
		super();
	}

	/**
	 * Constructs a new edge view for the specified cell.
	 * 
	 * @param cell
	 *            The cell to constructs the edge view for.
	 */
	public JGraphpadEdgeView(Object cell) {
		super(cell);
	}

	/**
	 * Overrides the parent's implementation to return a custom handle.
	 * 
	 * @param ctx
	 *            The context to use for the handle.
	 * @return Returns a custom handle.
	 */
	public CellHandle getHandle(GraphContext ctx) {
		return new JGraphpadEdgeHandle(this, ctx);
	}

	/**
	 * Overrides the parent's implementation to return the moveable port
	 * position for this edge.
	 * 
	 * @param isSource
	 *            Whether to return the nearest point for the source or target.
	 * @return Returns the moveable port position for this edge.
	 */
	protected Point2D getNearestPoint(boolean isSource) {
		if ((isSource && getSource() != null)
				|| (!isSource && getTarget() != null)) {
			Point2D offset = (isSource) ? offset = JGraphpadGraphConstants
					.getSourcePortOffset(getAllAttributes())
					: JGraphpadGraphConstants
							.getTargetPortOffset(getAllAttributes());
			CellView parentView = (isSource) ? getSource().getParentView()
					: getTarget().getParentView();

			// Computes the absolute location of the relative offset
			// and the source or target parentview's bounds.
			if (offset != null && parentView != null) {
				Rectangle2D r = parentView.getBounds();
				double x = offset.getX() * (r.getWidth() - 1)
						/ GraphConstants.PERMILLE;
				double y = offset.getY() * (r.getHeight() - 1)
						/ GraphConstants.PERMILLE;
				Point2D pos = new Point2D.Double(r.getX() + x, r.getY() + y);
				return pos;
			}
		}
		return super.getNearestPoint(isSource);
	}
	
	/**
	 * Returns {@link #editor} if the user object of the cell is a rich text
	 * value or {@link #redirector} if the user object is a component.
	 * 
	 * @return Returns the editor for the cell view.
	 */
	public GraphCellEditor getEditor() {
		Object value = ((DefaultMutableTreeNode) getCell()).getUserObject();
		if (value instanceof JGraphpadBusinessObject) {
			JGraphpadBusinessObject obj = (JGraphpadBusinessObject) value;
			if (obj.isRichText())
				return editor;
			else if (obj.isComponent())
				return redirector;
		}
		return super.getEditor();
	}
	
	/**
	 * Returns the {@link #renderer}.
	 * 
	 * @return Returns the renderer for the cell view.
	 */
	public CellViewRenderer getRenderer() {
		return renderer;
	}

	/**
	 * Custom handle that implements moveable ports.
	 */
	public static class JGraphpadEdgeHandle extends EdgeHandle {

		/**
		 * Specifies if the offset should be reset if the mouse is released.
		 */
		protected boolean resetOffset = false;

		/**
		 * Constructs a new edge handle for the specified edge and context.
		 * 
		 * @param edge
		 *            The edge to create the handle for.
		 * @param ctx
		 *            The context to use for the handle.
		 */
		public JGraphpadEdgeHandle(EdgeView edge, GraphContext ctx) {
			super(edge, ctx);
		}

		/**
		 * Overrides the parent implementation to support the shift key to
		 * remove points from edges.
		 * 
		 * @param event
		 *            The object that describes the event.
		 * @return Returns true if the event is a remove point event.
		 */
		public boolean isRemovePointEvent(MouseEvent event) {
			return super.isRemovePointEvent(event) || event.isShiftDown();
		}

		/**
		 * Overrides the parent implementation to support the shift key to add
		 * points to edges.
		 * 
		 * @param event
		 *            The object that describes the event.
		 * @return Returns true if the event is a add point event.
		 */
		public boolean isAddPointEvent(MouseEvent event) {
			return super.isAddPointEvent(event) || event.isShiftDown();
		}

		/**
		 * Overrides the parent implementation to reset the port offset if a
		 * remove event has been performed on either port.
		 * 
		 * @param event
		 *            The object that describes the event.
		 */
		public void mousePressed(MouseEvent event) {
			super.mousePressed(event);
			if (isRemovePointEvent(event) && (source || target))
				resetOffset = true;
		}

		/**
		 * Overrides the parent implementation to set the port offset if the
		 * mousepointer is over the port's parent view but not over the port's
		 * non-floating location (eg center).
		 * 
		 * @param isSource
		 *            Whether to snap the source or target port.
		 * @param point
		 *            The point that should be used for snapping.
		 */
		protected boolean snap(boolean isSource, Point2D point) {

			// Resets the offsets in the preview edge
			if (isSource)
				edge.getAllAttributes().remove(
						JGraphpadGraphConstants.SOURCEPORTOFFSET);
			else
				edge.getAllAttributes().remove(
						JGraphpadGraphConstants.TARGETPORTOFFSET);

			// Gets the bounds of the parent view and of the non-floating port
			Rectangle2D parent = null;
			Rectangle2D port = null;
			if (isSource && edge.getSource() != null) {
				parent = edge.getSource().getParentView().getBounds();
				port = edge.getSource().getBounds();
			} else if (target && edge.getTarget() != null) {
				parent = edge.getTarget().getParentView().getBounds();
				port = edge.getTarget().getBounds();
			}

			// Sets the port offset if the mousepointer is over the
			// source or target parent view bounds, but not over the
			// port itself.
			int tol = graph.getTolerance();
			if (port != null && port.contains(point)) {
				overlay(graph.getGraphics());
				edge.update(graph.getGraphLayoutCache());
				overlay(graph.getGraphics());
			} else if (parent != null
					&& parent.intersects(new Rectangle2D.Double(point.getX()
							- tol, point.getY() - tol, 2 * tol, 2 * tol))) {

				// Makes sure the port moving does not trigger on reconnects,
				// but only if the port is moved over the original source or
				// target.
				if ((isSource && edge.getSource() == orig.getSource())
						|| (target && edge.getTarget() == orig.getTarget())) {
					Point2D offset = computeOffset(point);
					if (offset != null) {
						edgeModified = true;
						overlay(graph.getGraphics());
						if (isSource)
							JGraphpadGraphConstants.setSourcePortOffset(edge
									.getAllAttributes(), offset);
						else
							JGraphpadGraphConstants.setTargetPortOffset(edge
									.getAllAttributes(), offset);
						edge.update(graph.getGraphLayoutCache());
						overlay(graph.getGraphics());
						resetOffset = false;
					}
					return true; // exit
				}
			}

			// Else the offset is reset and the superclass is called.
			resetOffset = true;
			return super.snap(isSource, point);
		}

		/**
		 * Overrides the parent implementation to reset the port offset if the
		 * {@link #resetOffset} flag is set.
		 * 
		 * @param nested
		 *            The nested attribute map that is used to change the layout
		 *            cache.
		 * @param clone
		 *            Whether the control key was pressed.
		 */
		protected void processNestedMap(Map nested, boolean clone) {
			if (resetOffset) {
				Map attrs = (Map) nested.get(edge.getCell());
				if (attrs != null) {
					String[] removeAttributes = new String[] { (source) ? JGraphpadGraphConstants.SOURCEPORTOFFSET
							: JGraphpadGraphConstants.TARGETPORTOFFSET };
					GraphConstants.setRemoveAttributes(attrs, removeAttributes);
				}
			}
		}

		/**
		 * Returns the perimeter point as a relative vector in (where
		 * 100%=GraphConstants.PERMILLE) in the coordinate space of the parent
		 * view bounds for the source or target port.
		 * 
		 * @param pt
		 *            The point to compute the offset for.
		 * @return Point
		 */
		private Point2D computeOffset(Point2D pt) {
			CellView portView = (source) ? edge.getSource() : edge.getTarget();
			if (portView != null) {
				CellView vertex = portView.getParentView();
				pt = vertex.getPerimeterPoint(edge, getCenterPoint(vertex), pt);

				// Computes the relative vector for the perimeter point
				Rectangle2D rect = vertex.getBounds();
				pt = new Point2D.Double((pt.getX() - rect.getX())
						/ (rect.getWidth() - 1) * GraphConstants.PERMILLE, (pt
						.getY() - rect.getY())
						/ (rect.getHeight() - 1) * GraphConstants.PERMILLE);
			}
			return pt;
		}

	}

}
