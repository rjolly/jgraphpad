/* 
 * $Id: JGraphpadCellAction.java,v 1.4 2005/10/15 16:36:17 gaudenz Exp $
 * Copyright (c) 2001-2005, Gaudenz Alder
 * 
 * All rights reserved.
 * 
 * See LICENSE file for license details. If you are unable to locate
 * this file please contact info (at) jgraph (dot) com.
 */
package com.jgraph.pad.action;

import java.awt.event.ActionEvent;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.swing.SwingConstants;

import org.jgraph.JGraph;
import org.jgraph.graph.AttributeMap;
import org.jgraph.graph.CellView;
import org.jgraph.graph.ConnectionSet;
import org.jgraph.graph.DefaultGraphModel;
import org.jgraph.graph.GraphConstants;
import org.jgraph.graph.GraphLayoutCache;
import org.jgraph.graph.GraphModel;
import org.jgraph.graph.ParentMap;
import org.jgraph.graph.PortView;

import com.jgraph.JGraphEditor;
import com.jgraph.JGraphpad;
import com.jgraph.editor.JGraphEditorAction;
import com.jgraph.pad.dialog.JGraphpadDialogs;
import com.jgraph.pad.factory.JGraphpadLibraryPane;
import com.jgraph.pad.graph.JGraphpadBusinessObject;
import com.jgraph.pad.graph.JGraphpadGraphConstants;
import com.jgraph.pad.graph.JGraphpadGraphModel;

/**
 * Implements all actions of the cell menu. Extends format action to use some of
 * the helper methods.
 */
public class JGraphpadCellAction extends JGraphpadFormatAction {

	/**
	 * Defines the constants to be passed to {@link #doAlignCells(JGraph, int)}.
	 */
	public static final int ALIGN_LEFT = SwingConstants.LEFT,
			ALIGN_RIGHT = SwingConstants.RIGHT, ALIGN_TOP = SwingConstants.TOP,
			ALIGN_BOTTOM = SwingConstants.BOTTOM,
			ALIGN_CENTER = SwingConstants.CENTER,
			ALIGN_MIDDLE = SwingConstants.NEXT;

	/**
	 * Key under which the cell prototype is stored in the actions values. This
	 * is used to store the prototype for connect and group actions, which
	 * require to create new edges and groups respectively.
	 */
	public static final String KEY_PROTOTYPE = "prototype";

	/**
	 * Specifies the name for the <code>cloneValue</code> action.
	 */
	public static final String NAME_CLONEVALUE = "cloneValue";

	/**
	 * Specifies the name for the <code>cloneSize</code> action.
	 */
	public static final String NAME_CLONESIZE = "cloneSize";

	/**
	 * Specifies the name for the <code>cloneAttributes</code> action.
	 */
	public static final String NAME_CLONEATTRIBUTES = "cloneAttributes";

	/**
	 * Specifies the name for the <code>cellsAlignTop</code> action.
	 */
	public static final String NAME_CELLSALIGNTOP = "cellsAlignTop";

	/**
	 * Specifies the name for the <code>cellsAlignMiddle</code> action.
	 */
	public static final String NAME_CELLSALIGNMIDDLE = "cellsAlignMiddle";

	/**
	 * Specifies the name for the <code>cellsAlignBottom</code> action.
	 */
	public static final String NAME_CELLSALIGNBOTTOM = "cellsAlignBottom";

	/**
	 * Specifies the name for the <code>cellsAlignLeft</code> action.
	 */
	public static final String NAME_CELLSALIGNLEFT = "cellsAlignLeft";

	/**
	 * Specifies the name for the <code>cellsAlignCenter</code> action.
	 */
	public static final String NAME_CELLSALIGNCENTER = "cellsAlignCenter";

	/**
	 * Specifies the name for the <code>cellsAlignRight</code> action.
	 */
	public static final String NAME_CELLSALIGNRIGHT = "cellsAlignRight";

	/**
	 * Specifies the name for the <code>toggleSelectable</code> action.
	 */
	public static final String NAME_TOGGLESELECTABLE = "toggleSelectable";

	/**
	 * Specifies the name for the <code>allSelectable</code> action.
	 */
	public static final String NAME_ALLSELECTABLE = "allSelectable";

	/**
	 * Specifies the name for the <code>toggleChildrenSelectable</code>
	 * action.
	 */
	public static final String NAME_TOGGLECHILDRENSELECTABLE = "toggleChildrenSelectable";

	/**
	 * Specifies the name for the <code>collapse</code> action.
	 */
	public static final String NAME_COLLAPSE = "collapse";

	/**
	 * Specifies the name for the <code>collapse</code> action.
	 */
	public static final String NAME_TOGGLECOLLAPSED = "toggleCollapsed";

	/**
	 * Specifies the name for the <code>expand</code> action.
	 */
	public static final String NAME_EXPAND = "expand";

	/**
	 * Specifies the name for the <code>expandAll</code> action.
	 */
	public static final String NAME_EXPANDALL = "expandAll";

	/**
	 * Specifies the name for the <code>toBack</code> action.
	 */
	public static final String NAME_TOBACK = "toBack";

	/**
	 * Specifies the name for the <code>toFront</code> action.
	 */
	public static final String NAME_TOFRONT = "toFront";

	/**
	 * Specifies the name for the <code>group</code> action.
	 */
	public static final String NAME_GROUP = "group";

	/**
	 * Specifies the name for the <code>groupAsEdge</code> action.
	 */
	public static final String NAME_GROUPASEDGE = "groupAsEdge";

	/**
	 * Specifies the name for the <code>ungroup</code> action.
	 */
	public static final String NAME_UNGROUP = "ungroup";

	/**
	 * Specifies the name for the <code>removeFromGroup</code> action.
	 */
	public static final String NAME_REMOVEFROMGROUP = "removeFromGroup";

	/**
	 * Specifies the name for the <code>connect</code> action.
	 */
	public static final String NAME_CONNECT = "connect";

	/**
	 * Specifies the name for the <code>disconnect</code> action.
	 */
	public static final String NAME_DISCONNECT = "disconnect";

	/**
	 * Specifies the name for the <code>addProperty</code> action.
	 */
	public static final String NAME_ADDPROPERTY = "addProperty";

	/**
	 * Specifies the name for the <code>removeProperty</code> action.
	 */
	public static final String NAME_REMOVEPROPERTY = "removeProperty";

	/**
	 * Specifies the name for the <code>invert</code> action.
	 */
	public static final String NAME_INVERT = "invert";

	/**
	 * Constructs a new cell action for the specified name. If the action name
	 * starts with <code>toggle</code> or <code>switch</code> then the
	 * action is configured to be a toggle action.
	 * 
	 * @param name
	 *            The name of the action to be created.
	 */
	public JGraphpadCellAction(String name) {
		super(name);
		setToggleAction(name.startsWith("toggle") || name.startsWith("switch")
				&& name.equals(NAME_TOGGLECOLLAPSED));
	}

	/**
	 * Executes the action based on the action name.
	 * 
	 * @param e
	 *            The object that describes the event.
	 */
	public void actionPerformed(ActionEvent e) {

		// Fetches the focus owner before showing dialogs
		JGraph graph = getPermanentFocusOwnerGraph();
		if (graph != null) {
			GraphLayoutCache cache = graph.getGraphLayoutCache();
			if (getName().equals(NAME_CLONEVALUE))
				doClone(graph, true, false);
			else if (getName().equals(NAME_CLONESIZE))
				doClone(graph, false, true);
			else if (getName().equals(NAME_CLONEATTRIBUTES))
				doClone(graph, false, false);
			else if (getName().equals(NAME_CELLSALIGNTOP))
				doAlignCells(graph, ALIGN_TOP);
			else if (getName().equals(NAME_CELLSALIGNMIDDLE))
				doAlignCells(graph, ALIGN_MIDDLE);
			else if (getName().equals(NAME_CELLSALIGNBOTTOM))
				doAlignCells(graph, ALIGN_BOTTOM);
			else if (getName().equals(NAME_CELLSALIGNLEFT))
				doAlignCells(graph, ALIGN_LEFT);
			else if (getName().equals(NAME_CELLSALIGNCENTER))
				doAlignCells(graph, ALIGN_CENTER);
			else if (getName().equals(NAME_CELLSALIGNRIGHT))
				doAlignCells(graph, ALIGN_RIGHT);
			else if (getName().equals(NAME_TOGGLESELECTABLE))
				doToggleAttribute(graph, GraphConstants.SELECTABLE, true);
			else if (getName().equals(NAME_TOGGLECHILDRENSELECTABLE))
				doToggleAttribute(graph, GraphConstants.CHILDRENSELECTABLE,
						true);
			else if (getName().equals(NAME_ALLSELECTABLE))
				setAttributes(graph,
						new String[] { GraphConstants.SELECTABLE },
						new Object[] { null }, true);
			else if (getName().equals(NAME_TOGGLECOLLAPSED))
				cache.toggleCollapsedState(graph.getSelectionCells(), false,
						false);
			else if (getName().equals(NAME_COLLAPSE))
				cache.toggleCollapsedState(graph.getSelectionCells(), true,
						false);
			else if (getName().equals(NAME_EXPAND))
				cache.toggleCollapsedState(graph.getSelectionCells(), false,
						true);
			else if (getName().equals(NAME_EXPANDALL))
				cache.toggleCollapsedState(graph.getDescendants(graph
						.getSelectionCells()), false, true);
			else if (getName().equals(NAME_TOBACK))
				graph.getGraphLayoutCache().toBack(graph.getSelectionCells());
			else if (getName().equals(NAME_TOFRONT))
				graph.getGraphLayoutCache().toFront(graph.getSelectionCells());
			else if (getName().equals(NAME_GROUP))
				doGroup(graph, getValue(KEY_PROTOTYPE));
			else if (getName().equals(NAME_GROUPASEDGE))
				doGroupAsEdge(graph, getValue(KEY_PROTOTYPE));
			else if (getName().equals(NAME_REMOVEFROMGROUP))
				graph.getGraphLayoutCache().edit(null, null,
						new ParentMap(graph.getSelectionCells(), null), null);
			else if (getName().equals(NAME_UNGROUP))
				graph.setSelectionCells(graph.getGraphLayoutCache().ungroup(
						graph.getSelectionCells()));
			else if (getName().equals(NAME_CONNECT))
				doConnect(graph, getValue(KEY_PROTOTYPE));
			else if (getName().equals(NAME_DISCONNECT))
				doDisconnect(graph);
			else if (getName().equals(NAME_ADDPROPERTY))
				doCellProperty(graph, true);
			else if (getName().equals(NAME_REMOVEPROPERTY))
				doCellProperty(graph, false);
			else if (getName().equals(NAME_INVERT))
				doInvert(graph);
		}

		// Fetches the focus owner before showing dialogs
		JGraphpadLibraryPane libraryPane = JGraphpadFileAction
				.getPermanentFocusOwnerLibraryPane();
		if (libraryPane != null && !libraryPane.isReadOnly()) {
			if (getName().equals(NAME_TOBACK))
				libraryPane.sendEntryToBack();
			else if (getName().equals(NAME_TOFRONT))
				libraryPane.bringEntryToFront();
		}
	}

	/**
	 * Inverts all selected cells by swapping the source and target of edges and
	 * inverting all control points, or by swapping width and height of
	 * vertices.
	 * 
	 * @param graph
	 *            The graph to perform the operation in.
	 */
	protected void doInvert(JGraph graph) {
		if (!graph.isSelectionEmpty()) {
			CellView[] views = graph.getGraphLayoutCache().getMapping(
					graph.getSelectionCells());
			ConnectionSet cs = new ConnectionSet();
			Map nested = new Hashtable();
			GraphModel model = graph.getModel();
			for (int i = 0; i < views.length; i++) {
				Object cell = views[i].getCell();
				Map change = new Hashtable();
				Rectangle2D bounds = GraphConstants.getBounds(views[i]
						.getAllAttributes());
				if (model.isEdge(cell)) {

					// Swaps source and target port
					Object source = model.getSource(cell);
					Object target = model.getTarget(cell);
					if (source != null)
						cs.connect(cell, source, false);
					if (target != null)
						cs.connect(cell, target, true);

					// Inverts the control points
					List pts = GraphConstants.getPoints(views[i]
							.getAllAttributes());
					if (pts != null) {
						LinkedList inverted = new LinkedList();
						Iterator it = pts.iterator();
						while (it.hasNext())
							inverted.addFirst(it.next());
						GraphConstants.setPoints(change, inverted);
					}
				}

				// Inverts the bounds
				else if (bounds != null) {
					bounds = new Rectangle2D.Double(bounds.getX(), bounds
							.getY(), bounds.getHeight(), bounds.getWidth());
					GraphConstants.setBounds(change, bounds);
				}
				if (!change.isEmpty())
					nested.put(cell, change);
			}
			graph.getGraphLayoutCache().edit(nested, cs, null, null);
		}
	}

	/**
	 * Displays a value dialog and adds or removes the entered value as a
	 * property to/from the user objects of the selection cells.
	 * 
	 * @param graph
	 *            The graph to perform the operation in.
	 * @param add
	 *            Whether the property should be added or removed.
	 */
	protected void doCellProperty(JGraph graph, boolean add) {
		if (!graph.isSelectionEmpty()) {
			String property = JGraphpadDialogs.getSharedInstance().valueDialog(
					getString("EnterPropertyName"));
			if (property != null) {
				Object[] cells = graph.getSelectionCells();

				// Contains the shared change description
				Map change = new Hashtable();
				change.put(property, (add) ? ""
						: JGraphpadGraphModel.VALUE_EMPTY);

				// Creates a nested map for all business objects
				Map nested = new Hashtable();
				for (int i = 0; i < cells.length; i++) {
					Object obj = graph.getModel().getValue(cells[i]);
					if (obj instanceof JGraphpadBusinessObject)
						nested.put(obj, change);
				}

				// Calls the model instead of the layout cache
				// to update the business objects
				if (nested != null)
					graph.getModel().edit(nested, null, null, null);
			}
		}
	}

	/**
	 * Clones the value, size or attributes of the first selection cell to the
	 * other selection cells. If value is true then the value is cloned, if size
	 * if true then the size is cloned. If both are false, then the attributes
	 * (except the bounds and points) are cloned. <br>
	 * Note: If value and size are true at the same time, then the value is
	 * cloned.
	 * 
	 * @param graph
	 *            The graph to perform the operation in.
	 * @param value
	 *            Whether to clone the value.
	 * @param size
	 *            Whether to clone the size.
	 */
	protected void doClone(JGraph graph, boolean value, boolean size) {
		GraphLayoutCache cache = graph.getGraphLayoutCache();
		CellView master = cache.getMapping(graph.getSelectionCell(), false);
		if (master != null && graph.getSelectionCount() > 1) {
			if (value) {

				// Clone the value directly by doing a cast.
				// An alternative would be to use the model's
				// clone method on the cell and get the cloned
				// user object from there.
				Map nested = new Hashtable();
				Object userObject = graph.getModel().getValue(master.getCell());
				Object[] cells = graph.getSelectionCells();
				for (int i = 0; i < cells.length; i++) {
					Map change = new Hashtable();
					if (userObject instanceof JGraphpadBusinessObject)
						userObject = ((JGraphpadBusinessObject) userObject)
								.clone();
					GraphConstants.setValue(change, userObject);
					nested.put(cells[i], change);
				}
				cache.edit(nested);
			} else if (size) {
				Rectangle2D bounds = master.getBounds();
				Map nested = new Hashtable();
				Object[] cells = graph.getSelectionCells();

				// Updates the size for all bounds-attributes
				for (int i = 1; i < cells.length; i++) {
					CellView view = cache.getMapping(cells[i], false);
					Rectangle2D tmp = GraphConstants.getBounds(view
							.getAllAttributes());

					// Ignores the cell if it has no bounds
					if (tmp != null) {
						Map change = new Hashtable();
						GraphConstants.setBounds(change,
								new Rectangle2D.Double(tmp.getX(), tmp.getY(),
										bounds.getWidth(), bounds.getHeight()));
						nested.put(view.getCell(), change);
					}
				}
				cache.edit(nested);
			} else {
				Map change = new Hashtable(master.getAllAttributes());
				change.remove(GraphConstants.BOUNDS);
				change.remove(GraphConstants.POINTS);
				cache.edit(graph.getSelectionCells(), change);
			}

		}
	}

	/**
	 * Connects the selection vertices using clones of <code>prototype</code>
	 * as edges. {@link JGraph#getDefaultPortForCell(Object)} is used to find
	 * the connection points of the vertices.
	 * 
	 * @param graph
	 *            The graph to perform the operation in.
	 * @param prototype
	 *            The cell to be cloned for creating edges.
	 * 
	 * @see GraphLayoutCache#getCells(boolean, boolean, boolean, boolean)
	 * @see DefaultGraphModel#cloneCell(GraphModel, Object)
	 * @see DefaultGraphModel#containsEdgeBetween(GraphModel, Object, Object)
	 */
	protected void doConnect(JGraph graph, Object prototype) {
		if (prototype != null) {
			Object[] v = graph.getSelectionCells(graph.getGraphLayoutCache()
					.getCells(false, true, false, false));

			// Replaces all vertices with their default ports
			PortView[] pv = new PortView[v.length];
			for (int i = 0; i < v.length; i++)
				pv[i] = graph.getDefaultPortForCell(v[i]);

			// Constructs the edges and connection set
			GraphModel model = graph.getModel();
			ConnectionSet cs = new ConnectionSet();
			for (int i = 0; i < v.length; i++) {
				for (int j = i + 1; j < v.length; j++) {

					// Checks if not already connected
					if (!DefaultGraphModel.containsEdgeBetween(model, v[i],
							v[j])) {

						// Creates a new edge and connection
						Object edge = DefaultGraphModel.cloneCell(model,
								prototype);
						cs.connect(edge, pv[i].getCell(), pv[j].getCell());
					}
				}
			}

			// Inserts the edges if any connections have been made
			if (!cs.isEmpty())
				graph.getGraphLayoutCache().insert(
						cs.getChangedEdges().toArray(), null, cs, null, null);
		}
	}

	/**
	 * Disconnects the selection vertices by removing all edges between them.
	 * 
	 * @param graph
	 *            The graph to perform the operation in.
	 * 
	 * @see GraphLayoutCache#getCells(boolean, boolean, boolean, boolean)
	 * @see DefaultGraphModel#getEdgesBetween(GraphModel, Object, Object,
	 *      boolean)
	 */
	protected void doDisconnect(JGraph graph) {
		Object[] v = graph.getSelectionCells(graph.getGraphLayoutCache()
				.getCells(false, true, false, false));
		if (v != null && v.length > 0) {
			HashSet result = new HashSet();
			GraphModel model = graph.getModel();

			// Gets the edges (undirected) for all vertex pairs
			for (int i = 0; i < v.length; i++) {
				for (int j = i + 1; j < v.length; j++) {
					Object[] e = DefaultGraphModel.getEdgesBetween(model, v[i],
							v[j], false);
					for (int k = 0; k < e.length; k++)
						result.add(e[k]);
				}
			}
			if (result.size() > 0)
				graph.getGraphLayoutCache().remove(result.toArray());
		}
	}

	/**
	 * Creates a new group cell that contains the selection cells as children
	 * using a clone of <code>prototype</code> as the group cell.
	 * 
	 * @param graph
	 *            The graph to perform the operation in.
	 * @param prototype
	 *            The cell to be cloned for creating groups.
	 * 
	 * @see DefaultGraphModel#cloneCell(GraphModel, Object)
	 * @see JGraph#order(Object[])
	 * @see GraphLayoutCache#insertGroup(Object, Object[])
	 */
	protected void doGroup(JGraph graph, Object prototype) {
		if (prototype != null) {

			// Reorders the selection according to the layering
			Object[] cells = graph.order(graph.getSelectionCells());
			if (cells != null && cells.length > 0) {

				// Gets a clone of the prototype group cell
				Object group = DefaultGraphModel.cloneCell(graph.getModel(),
						prototype);
				graph.getGraphLayoutCache().insertGroup(group, cells);
			}
		}
	}

	/**
	 * Creates a new group cell that contains the selection cells as children
	 * using a clone of <code>prototype</code> as the group cell. The
	 * prototype is assumed to be an edge.
	 * 
	 * @param graph
	 *            The graph to perform the operation in.
	 * @param prototype
	 *            The cell to be cloned for creating groups.
	 */
	protected void doGroupAsEdge(JGraph graph, Object prototype) {
		if (prototype != null) {
			prototype = DefaultGraphModel
					.cloneCell(graph.getModel(), prototype);
			Rectangle2D bounds = graph.getCellBounds(graph.getSelectionCells());
			if (bounds != null) {
				Map change = new Hashtable();
				List pts = new ArrayList();
				pts.add(new Point2D.Double(bounds.getX(), bounds.getY()));
				pts.add(new Point2D.Double(bounds.getX() + bounds.getWidth(),
						bounds.getY() + bounds.getHeight()));
				GraphConstants.setPoints(change, pts);
				graph.getModel().getAttributes(prototype).applyMap(change);
				doGroup(graph, prototype);
			}
		}
	}

	/**
	 * Aligns the selection vertices according to <code>constraint</code>.
	 * Valid constraints are: {@link #ALIGN_TOP}, {@link #ALIGN_MIDDLE},
	 * {@link #ALIGN_BOTTOM}, {@link #ALIGN_LEFT}, {@link #ALIGN_CENTER} and
	 * {@link #ALIGN_RIGHT}.
	 * 
	 * @param graph
	 *            The graph to perform the operation in.
	 * @param constraint
	 *            The constraint that describes the alignment.
	 * @see #alignRectangle2D(Rectangle2D, Rectangle2D, int)
	 * @see GraphLayoutCache#getCells(boolean, boolean, boolean, boolean)
	 */
	protected void doAlignCells(JGraph graph, int constraint) {
		Object[] cells = graph.getSelectionCells(graph.getGraphLayoutCache()
				.getCells(false, true, false, false));
		Rectangle2D bounds = graph.getCellBounds(cells);
		if (bounds != null) {
			GraphLayoutCache cache = graph.getGraphLayoutCache();
			Map nested = new Hashtable();

			// Aligns all cells that have a bounds property
			for (int i = 0; i < cells.length; i++) {
				CellView cellView = cache.getMapping(cells[i], false);
				if (cellView != null) {
					Rectangle2D cellBounds = GraphConstants.getBounds(cellView
							.getAllAttributes());
					if (cellBounds != null) {
						Map attrs = new Hashtable();
						Rectangle2D newBounds = (Rectangle2D) cellBounds
								.clone();
						alignRectangle2D(newBounds, bounds, constraint);
						GraphConstants.setBounds(attrs, newBounds);
						nested.put(cellView.getCell(), attrs);
					}
				}
			}
			if (!nested.isEmpty())
				cache.edit(nested, null, null, null);
		}
	}

	/**
	 * Helper methods that aligns <code>cellBounds</code> inside
	 * <code>outerBounds</code> according to <code>constraint</code>.
	 */
	protected void alignRectangle2D(Rectangle2D cellBounds,
			Rectangle2D outerBounds, int constraint) {
		switch (constraint) {
		case ALIGN_LEFT:
			cellBounds.setFrame(outerBounds.getX(), cellBounds.getY(),
					cellBounds.getWidth(), cellBounds.getHeight());
			break;
		case ALIGN_TOP:
			cellBounds.setFrame(cellBounds.getX(), outerBounds.getY(),
					cellBounds.getWidth(), cellBounds.getHeight());
			break;
		case ALIGN_RIGHT:
			cellBounds.setFrame(outerBounds.getX() + outerBounds.getWidth()
					- cellBounds.getWidth(), cellBounds.getY(), cellBounds
					.getWidth(), cellBounds.getHeight());
			break;
		case ALIGN_BOTTOM:
			cellBounds.setFrame(cellBounds.getX(), outerBounds.getY()
					+ outerBounds.getHeight() - cellBounds.getHeight(),
					cellBounds.getWidth(), cellBounds.getHeight());
			break;
		case ALIGN_CENTER:
			double cx = outerBounds.getWidth() / 2;
			cellBounds.setFrame(outerBounds.getX() + cx - cellBounds.getWidth()
					/ 2, cellBounds.getY(), cellBounds.getWidth(), cellBounds
					.getHeight());
			break;
		case ALIGN_MIDDLE:
			double cy = outerBounds.getHeight() / 2;
			cellBounds.setFrame(cellBounds.getX(), outerBounds.getY() + cy
					- cellBounds.getHeight() / 2, cellBounds.getWidth(),
					cellBounds.getHeight());
			break;
		}
	}

	/**
	 * Bundle of all actions in this class.
	 */
	public static class AllActions implements Bundle {

		/**
		 * Holds the actions. The actionGroup and actionConnect are assigned the
		 * prototypes at construction time.
		 */
		public JGraphEditorAction actionCloneValue = new JGraphpadCellAction(
				NAME_CLONEVALUE), actionCloneSize = new JGraphpadCellAction(
				NAME_CLONESIZE),
				actionCloneAttributes = new JGraphpadCellAction(
						NAME_CLONEATTRIBUTES),
				actionCellsAlignTop = new JGraphpadCellAction(
						NAME_CELLSALIGNTOP),
				actionCellsAlignMiddle = new JGraphpadCellAction(
						NAME_CELLSALIGNMIDDLE),
				actionCellsAlignBottom = new JGraphpadCellAction(
						NAME_CELLSALIGNBOTTOM),
				actionCellsAlignLeft = new JGraphpadCellAction(
						NAME_CELLSALIGNLEFT),
				actionCellsAlignCenter = new JGraphpadCellAction(
						NAME_CELLSALIGNCENTER),
				actionCellsAlignRight = new JGraphpadCellAction(
						NAME_CELLSALIGNRIGHT),
				actionToggleSelectable = new JGraphpadCellAction(
						NAME_TOGGLESELECTABLE),
				actionToggleChildrenSelectable = new JGraphpadCellAction(
						NAME_TOGGLECHILDRENSELECTABLE),
				actionAllSelectable = new JGraphpadCellAction(
						NAME_ALLSELECTABLE),
				actionCollapse = new JGraphpadCellAction(NAME_COLLAPSE),
				actionToggleCollapsed = new JGraphpadCellAction(
						NAME_TOGGLECOLLAPSED),
				actionExpand = new JGraphpadCellAction(NAME_EXPAND),
				actionExpandAll = new JGraphpadCellAction(NAME_EXPANDALL),
				actionToBack = new JGraphpadCellAction(NAME_TOBACK),
				actionToFront = new JGraphpadCellAction(NAME_TOFRONT),
				actionGroup = new JGraphpadCellAction(NAME_GROUP),
				actionGroupAsEdge = new JGraphpadCellAction(NAME_GROUPASEDGE),
				actionUngroup = new JGraphpadCellAction(NAME_UNGROUP),
				actionRemoveFromGroup = new JGraphpadCellAction(
						NAME_REMOVEFROMGROUP),
				actionConnect = new JGraphpadCellAction(NAME_CONNECT),
				actionDisconnect = new JGraphpadCellAction(NAME_DISCONNECT),
				actionAddProperty = new JGraphpadCellAction(NAME_ADDPROPERTY),
				actionRemoveProperty = new JGraphpadCellAction(
						NAME_REMOVEPROPERTY),
				actionInvert = new JGraphpadCellAction(NAME_INVERT);

		/**
		 * Constructs the action bundle for the enclosing class.
		 * 
		 * @param editor
		 *            The enclosing editor.
		 */
		public AllActions(JGraphEditor editor) {
			Object groupPrototype = editor.getSettings().getObject(
					JGraphpad.KEY_GROUPPROTOTYPE);
			Object edgePrototype = editor.getSettings().getObject(
					JGraphpad.KEY_EDGEPROTOTYPE);
			actionGroup.putValue(KEY_PROTOTYPE, groupPrototype);
			actionGroupAsEdge.putValue(KEY_PROTOTYPE, edgePrototype);
			actionConnect.putValue(KEY_PROTOTYPE, edgePrototype);
		}

		/*
		 * (non-Javadoc)
		 */
		public JGraphEditorAction[] getActions() {
			return new JGraphEditorAction[] { actionCloneValue,
					actionCloneSize, actionCloneAttributes,
					actionCellsAlignTop, actionCellsAlignMiddle,
					actionCellsAlignBottom, actionCellsAlignLeft,
					actionCellsAlignCenter, actionCellsAlignRight,
					actionToggleSelectable, actionToggleChildrenSelectable,
					actionAllSelectable, actionCollapse, actionToggleCollapsed,
					actionExpand, actionExpandAll, actionToBack, actionToFront,
					actionGroup, actionGroupAsEdge, actionUngroup,
					actionRemoveFromGroup, actionConnect, actionDisconnect,
					actionAddProperty, actionRemoveProperty, actionInvert };
		}

		/*
		 * (non-Javadoc)
		 */
		public void update() {
			JGraph graph = getPermanentFocusOwnerGraph();
			boolean isCellSelected = graph != null && !graph.isSelectionEmpty();
			JGraphpadLibraryPane libraryPane = JGraphpadFileAction
					.getPermanentFocusOwnerLibraryPane();
			boolean isEntrySelected = libraryPane != null
					&& !libraryPane.isSelectionEmpty();

			actionToBack.setEnabled(isCellSelected || isEntrySelected);
			actionToFront.setEnabled(isCellSelected || isEntrySelected);

			actionCloneValue.setEnabled(isCellSelected);
			actionCloneSize.setEnabled(isCellSelected);
			actionCloneAttributes.setEnabled(isCellSelected);
			actionCellsAlignTop.setEnabled(isCellSelected);
			actionCellsAlignMiddle.setEnabled(isCellSelected);
			actionCellsAlignBottom.setEnabled(isCellSelected);
			actionCellsAlignLeft.setEnabled(isCellSelected);
			actionCellsAlignCenter.setEnabled(isCellSelected);
			actionCellsAlignRight.setEnabled(isCellSelected);
			actionToggleSelectable.setEnabled(isCellSelected);
			actionAllSelectable.setEnabled(graph != null);
			actionToggleChildrenSelectable.setEnabled(isCellSelected);
			actionCollapse.setEnabled(isCellSelected);
			actionToggleCollapsed.setEnabled(isCellSelected);
			actionExpand.setEnabled(isCellSelected);
			actionExpandAll.setEnabled(isCellSelected);
			actionGroup.setEnabled(isCellSelected);
			actionGroupAsEdge.setEnabled(isCellSelected);
			actionUngroup.setEnabled(isCellSelected);
			actionRemoveFromGroup.setEnabled(isCellSelected);
			actionConnect.setEnabled(isCellSelected);
			actionDisconnect.setEnabled(isCellSelected);
			actionAddProperty.setEnabled(isCellSelected);
			actionRemoveProperty.setEnabled(isCellSelected);
			actionInvert.setEnabled(isCellSelected);

			// Update toggleable action states
			if (graph != null) {
				actionToggleSelectable.setSelected(getBooleanAttribute(graph,
						GraphConstants.SELECTABLE, true));
				actionToggleChildrenSelectable.setSelected(getBooleanAttribute(
						graph, GraphConstants.CHILDRENSELECTABLE, true));
			}
		}

	}

}