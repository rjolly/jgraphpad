/* 
 * $Id: JGraphpadVertexTool.java,v 1.9 2007/03/25 13:07:51 david Exp $
 * Copyright (c) 2001-2005, Gaudenz Alder
 * 
 * All rights reserved.
 * 
 * See LICENSE file for license details. If you are unable to locate
 * this file please contact info (at) jgraph (dot) com.
 */
package com.jgraph.pad.tool;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;

import org.jgraph.JGraph;
import org.jgraph.graph.CellView;
import org.jgraph.graph.DefaultGraphModel;
import org.jgraph.graph.GraphConstants;
import org.jgraph.graph.GraphLayoutCache;
import org.jgraph.graph.GraphModel;
import org.jgraph.graph.VertexView;
import org.jgraph.plaf.GraphUI;
import org.jgraph.plaf.basic.BasicGraphUI;

import com.jgraph.editor.JGraphEditorTool;

/**
 * Tool that inserts vertices based on a prototype.
 */
public class JGraphpadVertexTool extends JGraphEditorTool {

	/**
	 * Defines the default name for tools of this kind.
	 */
	public static final String NAME_VERTEXTOOL = "vertexTool";

	/**
	 * Holds the prototype to create new cells with.
	 */
	protected Object prototype;

	/**
	 * Defines the threshhold (minimum size) for a drag to be used for an
	 * insert. Default is 4.
	 */
	protected int threshold = 4;

	/**
	 * Defines the default size for vertices that are generated with a single
	 * click. A value of null ignores single clicks. Default is null.
	 */
	protected Dimension singleClickSize = null;

	/**
	 * Preview vertex view.
	 */
	protected transient CellView previewView;

	/**
	 * Specifies if the cellview should be previewed. Default is true.
	 */
	protected transient boolean previewEnabled = true;

	/**
	 * Constructs a new vertex tool for the specified prototype using
	 * {@link #NAME_VERTEXTOOL}.
	 * 
	 * @param prototype
	 *            The prototype cell to create new vertices with.
	 */
	public JGraphpadVertexTool(Object prototype) {
		this(NAME_VERTEXTOOL, prototype);
	}

	/**
	 * Constructs a new vertex tool for the specified name and prototype.
	 * 
	 * @param name
	 *            The name of the tool to be created.
	 * @param prototype
	 *            The prototype cell to create new vertices with.
	 */
	public JGraphpadVertexTool(String name, Object prototype) {
		super(name);
		this.prototype = prototype;
	}

	/**
	 * Returns the prototype used to create new cells.
	 * 
	 * @return Returns the prototype.
	 */
	public Object getPrototype() {
		return prototype;
	}

	/**
	 * Sets the prototype to be used to create new cells.
	 * 
	 * @param prototype
	 *            The prototype to set.
	 */
	public void setPrototype(Object prototype) {
		this.prototype = prototype;
	}

	/**
	 * Returns the single click size which should be used to insert cells with a
	 * single click.
	 * 
	 * @return Returns the singleClickSize.
	 */
	public Dimension getSingleClickSize() {
		return singleClickSize;
	}

	/**
	 * Sets the size of cells to be inserted with a single click.
	 * 
	 * @param singleClickSize
	 *            The singleClickSize to set.
	 */
	public void setSingleClickSize(Dimension singleClickSize) {
		this.singleClickSize = singleClickSize;
	}

	/**
	 * Returns the threshold for a drag to count as an insert.
	 * 
	 * @return Returns the threshold.
	 */
	public int getThreshold() {
		return threshold;
	}

	/**
	 * Sets the threshold for a drag to count as an insert.
	 * 
	 * @param threshold
	 *            The threshold to set.
	 */
	public void setThreshold(int threshold) {
		this.threshold = threshold;
	}

	/**
	 * Sets if the cellview should be previewed.
	 * 
	 * @return Returns the previewEnabled.
	 */
	public boolean isPreviewEnabled() {
		return previewEnabled;
	}

	/**
	 * Returns if the cellview should be previewed.
	 * 
	 * @param previewEnabled
	 *            The previewEnabled to set.
	 */
	public void setPreviewEnabled(boolean previewEnabled) {
		this.previewEnabled = previewEnabled;
	}

	/**
	 * Extends the parent's implementation to create a clone of the pototype
	 * using {@link #createCell(GraphModel)} and a new view using the layout
	 * cache's factory. The view is then configured and stored in
	 * {@link #previewView}.
	 * 
	 * @param event
	 *            The object that describes the event.
	 */
	public void mousePressed(MouseEvent event) {
		super.mousePressed(event);
		JGraph graph = getGraphForEvent(event);
		if (graph != null) {
			Object cell = createCell(graph.getModel());
			if (cell != null) {
				previewView = graph.getGraphLayoutCache().getFactory()
						.createView(graph.getModel(), cell);

				// Configures the previewView by setting its bounds
				// to the marquee bounds and calling refresh.
				if (previewView != null) {
					Rectangle2D rect = graph.fromScreen(graph
							.snap((Rectangle2D) marqueeBounds.clone()));
					previewView.getAttributes().applyValue(
							GraphConstants.BOUNDS, rect);
					previewView.refresh(graph.getGraphLayoutCache(), graph
							.getGraphLayoutCache(), false);
				}
			}
		}
	}

	/**
	 * Returns a deep clone of the cell prototype.
	 * 
	 * @param model
	 *            The model to use for cloning the prototype.
	 * @return Returns a clone of {@link #prototype}.
	 */
	protected Object createCell(GraphModel model) {
		return DefaultGraphModel.cloneCell(model, prototype);
	}

	/**
	 * Overrides the parent's implementation to update the preview bounds to the
	 * current {@link org.jgraph.graph.BasicMarqueeHandler#marqueeBounds}.
	 * 
	 * @param event
	 *            The object that describes the event.
	 */
	protected void processMouseDraggedEvent(MouseEvent event) {
		super.processMouseDraggedEvent(event);
		if (marqueeBounds != null && previewView instanceof VertexView) {
			JGraph graph = getGraphForEvent(event);

			// Special handling of constrained events or cells
			if (isConstrainedSizeEvent(event)
					|| GraphConstants.isConstrained(previewView
							.getAllAttributes())) {
				marqueeBounds.setFrame(startPoint.getX(), startPoint.getY(),
						marqueeBounds.getWidth(), marqueeBounds.getWidth());
			}

			// Updates the bounds of the previewView and updates
			Rectangle2D rect = graph.fromScreen(graph
					.snap((Rectangle2D) marqueeBounds.clone()));
			previewView.getAttributes().applyValue(GraphConstants.BOUNDS, rect);
			previewView.update(graph.getGraphLayoutCache());
		}
	}

	/**
	 * Hook for subclassers to define the key assignments for constrained
	 * inserts. This implementation redirects to
	 * {@link BasicGraphUI#isConstrainedMoveEvent(MouseEvent)}.
	 * 
	 * @param event
	 *            The object that describes the event.
	 */
	protected boolean isConstrainedSizeEvent(MouseEvent event) {
		JGraph graph = getGraphForEvent(event);
		GraphUI ui = graph.getUI();
		if (ui instanceof BasicGraphUI)
			return ((BasicGraphUI) ui).isConstrainedMoveEvent(event);
		return false;
	}

	/**
	 * Overrides the parent's implementation to check if the event triggers an
	 * insert (checks {@link #threshold} & {@link #singleClickSize}) and
	 * inserts the cell stored in {@link #previewView} using
	 * {@link #execute(GraphLayoutCache, Object)}.
	 * 
	 * @param event
	 *            The object that describes the event.
	 */
	public void mouseReleased(MouseEvent event) {
		try {
			JGraph graph = getGraphForEvent(event);
			if (graph != null) {
				if (previousCursor != null) {
					graph.setCursor(previousCursor);
				}
				if (marqueeBounds != null
						&& ((singleClickSize != null) || (marqueeBounds
								.getWidth() > threshold || marqueeBounds
								.getHeight() > threshold))) {
					if (previewView != null) {

						// Check if the minimum size requirement is met and do a
						// single-click insert with the specified size. This
						// takes the preview view and modifies the bounds
						// in place to be minimum of single click size.
						if (singleClickSize != null) {
							Rectangle2D bounds = GraphConstants
									.getBounds(previewView.getAllAttributes());
							if (bounds != null)
								bounds.setFrame(bounds.getX(), bounds.getY(),
										Math.max(singleClickSize.width, bounds
												.getWidth()), Math.max(
												singleClickSize.height, bounds
														.getHeight()));
						}

						// Prepares the cell for the execute call by moving all
						// attributes from the preview view to the cell, then
						// calls execute with that cell and the current cache.
						Object cell = previewView.getCell();
						graph.getModel().getAttributes(cell).applyMap(
								previewView.getAllAttributes());
						execute(graph, cell);
					}

					// Prevents further event processing
					event.consume();
				} else {

					// After an invalid insert the selection and the graph
					// canvas
					// is cleared and theh event is passed along the chain.
					graph.clearSelection();
					if (previewView != null) {
						Rectangle2D r = previewView.getBounds();
						graph.getGraphics().setClip((int) r.getX() - 1,
								(int) r.getY() - 1, (int) r.getWidth() + 2,
								(int) r.getHeight() + 2);
						graph.repaint();
					}
				}
			}
		} finally {
			previousCursor = null;
			marqueeBounds = null;
			currentPoint = null;
			previewView = null;
			startPoint = null;
		}
	}

	/**
	 * Provides a hook for subclassers to insert the specified cell into
	 * <code>cache</code>. This implementation passes the cell to the
	 * {@link GraphLayoutCache#insert(Object)}.
	 * 
	 * @param cache
	 *            The cache into which to insert the edge.
	 * @param cell
	 *            The cell to be inserted into <code>cache</code>.
	 */
	protected void execute(JGraph graph, Object cell) {
		graph.getGraphLayoutCache().insert(cell);
	}

	/**
	 * Extends the parent's implementation to draw the {@link #previewView}.
	 * 
	 * @param graph
	 *            The graph to paint in.
	 * @param g
	 *            The graphics to use for paiting.
	 * @param clear
	 *            Wether to clear the display.
	 */
	public void overlay(JGraph graph, Graphics g, boolean clear) {
		if (!previewEnabled) {
			super.overlay(graph, g, clear);
		} else if (previewView != null) {
			Rectangle2D bounds = previewView.getBounds();
			if (clear) {
				g.setPaintMode();
				graph.repaint((int) bounds.getX() - 1, (int) bounds.getY() - 1,
						(int) bounds.getWidth() + 2,
						(int) bounds.getHeight() + 2);
			} else {
				Graphics2D g2 = (Graphics2D) g;
				AffineTransform tmp = g2.getTransform();
				g2.scale(graph.getScale(), graph.getScale());
				graph.getUI().paintCell(g, previewView, bounds, true);
				g2.setTransform(tmp);
			}
		}
	}

}