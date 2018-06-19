/* 
 * $Id: JGraphpadMarqueeHandler.java,v 1.4 2005/09/18 08:39:51 gaudenz Exp $
 * Copyright (c) 2001-2005, Gaudenz Alder
 * 
 * All rights reserved.
 * 
 * See LICENSE file for license details. If you are unable to locate
 * this file please contact info (at) jgraph (dot) com.
 */
package com.jgraph.pad.graph;

import java.awt.Component;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;

import org.jgraph.JGraph;
import org.jgraph.graph.BasicMarqueeHandler;
import org.jgraph.graph.CellView;
import org.jgraph.graph.DefaultGraphModel;
import org.w3c.dom.Node;

import com.jgraph.JGraphEditor;
import com.jgraph.JGraphpad;
import com.jgraph.editor.JGraphEditorSettings;

/**
 * Marquee handler that implements popup menus and folding (by icon). This
 * implements the event processing order for all graphs in the following way:
 * <ul>
 * <li>If a folding icon is under the mouse pointer all processing is blocked
 * until the mouse is released. On release, the group's collapsed state is
 * toggled and the group is selected.</li>
 * <li>If the right mouse button is used without holding down the shift key
 * then a popup menu is displayed. If there is a cell under the mouse pointer
 * which is not selected, then the cell is selected on mouse down. If the mouse
 * is right-clicked on the background then the selection is cleared before the
 * popup is displayed.</li>
 * </li>
 * </ul>
 */
public class JGraphpadMarqueeHandler extends BasicMarqueeHandler {

	/**
	 * Defines the nodename used to configure the graphpopupmenu.
	 */
	public static String NODENAME_GRAPHPOPUPMENU = "graphpopupmenu";

	/**
	 * Defines the nodename used to configure the cellpopupmenu.
	 */
	public static String NODENAME_CELLPOPUPMENU = "cellpopupmenu";

	/**
	 * References the enclosing editor. The editor is used to configure and
	 * create the popup menus.
	 */
	protected JGraphEditor editor = null;

	/**
	 * Holds the group view if the interaction started on a folding icon.
	 */
	protected CellView groupView = null;

	/**
	 * Constructs a new editor using the specified editor to configure and
	 * create popup menus.
	 * 
	 * @param editor
	 *            The enclosing editor.
	 */
	public JGraphpadMarqueeHandler(JGraphEditor editor) {
		this.editor = editor;
	}

	/**
	 * Extends the parent's implementation to implement the event processing
	 * order.
	 * 
	 * @param event
	 *            The object that describes the event.
	 */
	public boolean isForceMarqueeEvent(MouseEvent event) {
		JGraph graph = getGraphForEvent(event);

		// Checks if the popup menu should be triggered and fetches
		// control for further event processing.
		if (SwingUtilities.isRightMouseButton(event) && !event.isShiftDown())
			return true;

		// Checks if a folding icon is under the mouse pointer
		// and stores the respective view in group view to block
		// further event processing until mouseReleased is reached.
		groupView = getGroupByFoldingHandle(graph, graph
				.fromScreen((Point2D) event.getPoint().clone()));
		if (groupView != null)
			return true;

		return super.isForceMarqueeEvent(event);
	}

	/**
	 * Overrides the parent's implementation to either block processing if a
	 * group view is scheduled to be collapsed/expanded or immediately select
	 * the cell under the mouse pointer if it is not selected.
	 * 
	 * @param event
	 *            The object that describes the event.
	 */
	public void mousePressed(MouseEvent event) {
		JGraph graph = getGraphForEvent(event);

		// Blocks processing if a folding icon was clicked
		if (groupView != null)
			event.consume();

		// Immediately selects unselected cells under the mouse
		else if (graph.getSelectionCellAt(event.getPoint()) == null) {
			Point2D pt = graph.fromScreen((Point2D) event.getPoint().clone());
			CellView view = graph.getTopmostViewAt(pt.getX(), pt.getY(), false,
					false);
			if (view != null)
				graph.getUI().selectCellsForEvent(graph,
						new Object[] { view.getCell() }, event);
			else
				graph.clearSelection();
		}
	}

	/**
	 * Overrides the parent's implementation to display a popup menu using
	 * {@link #getPopupMenuConfiguration(MouseEvent)} to obtain the
	 * configuration or toggle the collapsed state of {@link #groupView} and
	 * select the respective cell. The state of the handler is reset after event
	 * processing.
	 * 
	 * @param e
	 *            The object that describes the event.
	 */
	public void mouseReleased(MouseEvent e) {
		if (SwingUtilities.isRightMouseButton(e)) {

			// Displays a popup menu by obtaining the menu configuration
			// for the event. The menu configuration is created based
			// on the current selection in the event source.
			Node configuration = getPopupMenuConfiguration(e);
			if (configuration != null) {
				JPopupMenu popupMenu = editor.getFactory().createPopupMenu(
						configuration);
				popupMenu.setFocusable(false);
				popupMenu.show(e.getComponent(), e.getX(), e.getY());
				e.consume();
			}
		}

		// Toggles the collapsed state of the group view, selects
		// the group cell and resets the state of the handle.
		else if (groupView != null) {
			JGraph graph = getGraphForEvent(e);
			graph.getGraphLayoutCache().toggleCollapsedState(
					new Object[] { groupView.getCell() }, false, false);
			graph.setSelectionCell(groupView.getCell());
			groupView = null;
			e.consume();
		}

		// Else call parent's implementation
		else
			super.mouseReleased(e);
	}

	/**
	 * Returns the popup menu configuration for the specified event. This
	 * implementation looks at the event source and returns a
	 * {@link #NODENAME_CELLPOPUPMENU} or {@link #NODENAME_GRAPHPOPUPMENU}
	 * configuration based on the selection state of the graph.
	 * 
	 * @param event
	 *            The event that triggered the display of the popup menu.
	 * @return Returns the popup menu configuration for <code>event</code>.
	 */
	protected Node getPopupMenuConfiguration(MouseEvent event) {
		JGraph graph = getGraphForEvent(event);
		return JGraphEditorSettings.getNodeByName(editor.getSettings()
				.getDocument(JGraphpad.NAME_UICONFIG).getDocumentElement()
				.getChildNodes(),
				(graph.isSelectionEmpty()) ? NODENAME_GRAPHPOPUPMENU
						: NODENAME_CELLPOPUPMENU);
	}

	/**
	 * Returns the cell view at the specified location if the location is over
	 * the cell view's folding icon.
	 * 
	 * @param graph
	 *            The graph to get the cell views from.
	 * @param pt
	 *            The location to check for a folding icon.
	 * @return Returns the topmost cell view who's folding icon is under the
	 *         mouse pointer.
	 */
	protected CellView getGroupByFoldingHandle(JGraph graph, Point2D pt) {
		CellView[] views = graph.getGraphLayoutCache().getCellViews();
		for (int i = 0; i < views.length; i++) {
			if (views[i].getBounds().contains(pt.getX(), pt.getY())) {
				Rectangle2D rectBounds = views[i].getBounds();
				Point2D containerPoint = (Point2D) pt.clone();
				containerPoint.setLocation(containerPoint.getX()
						- rectBounds.getX(), containerPoint.getY()
						- rectBounds.getY());
				Component renderer = views[i].getRendererComponent(graph,
						false, false, false);
				if (renderer instanceof JGraphpadVertexRenderer
						&& DefaultGraphModel.isGroup(graph.getModel(), views[i]
								.getCell())) {
					JGraphpadVertexRenderer group = (JGraphpadVertexRenderer) renderer;
					if (group.inHitRegion(containerPoint)) {
						return views[i];
					}
				}
			}
		}
		return null;
	}
}
