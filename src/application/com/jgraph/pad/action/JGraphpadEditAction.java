/* 
 * $Id: JGraphpadEditAction.java,v 1.2 2005/10/15 16:36:17 gaudenz Exp $
 * Copyright (c) 2001-2005, Gaudenz Alder
 * 
 * All rights reserved.
 * 
 * See LICENSE file for license details. If you are unable to locate
 * this file please contact info (at) jgraph (dot) com.
 */
package com.jgraph.pad.action;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import javax.swing.Action;
import javax.swing.TransferHandler;
import javax.swing.text.DefaultEditorKit;

import org.jgraph.JGraph;
import org.jgraph.graph.CellView;
import org.jgraph.graph.GraphLayoutCache;
import org.jgraph.graph.GraphUndoManager;

import com.jgraph.editor.JGraphEditorAction;
import com.jgraph.editor.JGraphEditorResources;
import com.jgraph.editor.factory.JGraphEditorDiagramPane;
import com.jgraph.pad.dialog.JGraphpadDialogs;
import com.jgraph.pad.factory.JGraphpadLibraryPane;

/**
 * Implements all actions of the edit menu. The selectPath and selectTree
 * actions are implemented by plugins.
 */
public class JGraphpadEditAction extends JGraphEditorAction {

	/**
	 * Holds the last search expression. Note: In a multi application
	 * environment you may have to put this into the application instance.
	 */
	protected static Pattern lastSearchPattern = null;

	/**
	 * Holds the last found cell for a search.
	 */
	protected static Object lastFoundCell = null;

	/**
	 * Specifies the name for the <code>cut</code> action.
	 */
	public static final String NAME_CUT = "cut";

	/**
	 * Specifies the name for the <code>copy</code> action.
	 */
	public static final String NAME_COPY = "copy";

	/**
	 * Specifies the name for the <code>paste</code> action.
	 */
	public static final String NAME_PASTE = "paste";

	/**
	 * Specifies the name for the <code>delete</code> action.
	 */
	public static final String NAME_DELETE = "delete";

	/**
	 * Specifies the name for the <code>edit</code> action.
	 */
	public static final String NAME_EDIT = "edit";

	/**
	 * Specifies the name for the <code>find</code> action.
	 */
	public static final String NAME_FIND = "find";

	/**
	 * Specifies the name for the <code>findAgain</code> action.
	 */
	public static final String NAME_FINDAGAIN = "findAgain";

	/**
	 * Specifies the name for the <code>undo</code> action.
	 */
	public static final String NAME_UNDO = "undo";

	/**
	 * Specifies the name for the <code>redo</code> action.
	 */
	public static final String NAME_REDO = "redo";

	/**
	 * Specifies the name for the <code>selectAll</code> action.
	 */
	public static final String NAME_SELECTALL = "selectAll";

	/**
	 * Specifies the name for the <code>clearSelection</code> action.
	 */
	public static final String NAME_CLEARSELECTION = "clearSelection";

	/**
	 * Specifies the name for the <code>selectVertices</code> action.
	 */
	public static final String NAME_SELECTVERTICES = "selectVertices";

	/**
	 * Specifies the name for the <code>selectEdges</code> action.
	 */
	public static final String NAME_SELECTEDGES = "selectEdges";

	/**
	 * Specifies the name for the <code>deselectVertices</code> action.
	 */
	public static final String NAME_DESELECTVERTICES = "deselectVertices";

	/**
	 * Specifies the name for the <code>deselectEdges</code> action.
	 */
	public static final String NAME_DESELECTEDGES = "deselectEdges";

	/**
	 * Specifies the name for the <code>invertSelection</code> action.
	 */
	public static final String NAME_INVERTSELECTION = "invertSelection";

	/**
	 * Fallback action if the focus-owner is not a graph. This is assigned
	 * internally for special action names, namely cut, copy, paste and delete
	 * for text components.
	 */
	protected Action fallbackAction = null;

	/**
	 * Constructs a new edit action for the specified name. This constructs the
	 * {@link #fallbackAction} for text components in case of supported actions.
	 * 
	 * @param name
	 *            The name of the action to be created.
	 */
	public JGraphpadEditAction(String name) {
		super(name);
		if (getName().equals(NAME_COPY))
			fallbackAction = new DefaultEditorKit.CopyAction();
		else if (getName().equals(NAME_PASTE))
			fallbackAction = new DefaultEditorKit.PasteAction();
		else if (getName().equals(NAME_CUT))
			fallbackAction = new DefaultEditorKit.CutAction();
		else if (getName().equals(NAME_DELETE))
			fallbackAction = new DefaultEditorKit.CutAction();
	}

	/**
	 * Executes the action based on the action name.
	 * 
	 * @param e
	 *            The object that describes the event.
	 */
	public void actionPerformed(ActionEvent e) {
		Component component = getPermanentFocusOwner();
		JGraphpadLibraryPane libraryPane = JGraphpadFileAction
				.getPermanentFocusOwnerLibraryPane();

		// Calls the fallback action if one has been defined
		// and the focus owner is not graph
		if (!(component instanceof JGraph) && libraryPane == null
				&& fallbackAction != null) {
			fallbackAction.actionPerformed(e);

			// Actions that require a focused graph
		} else if (component instanceof JGraph) {
			JGraph graph = (JGraph) component;

			// Creates a new event with the graph as the source.
			// This is required for the transfer handler to work
			// correctly when called from outside the graph ui.
			ActionEvent newEvent = new ActionEvent(graph, e.getID(), e
					.getActionCommand());
			if (getName().equals(NAME_COPY))
				TransferHandler.getCopyAction().actionPerformed(newEvent);
			else if (getName().equals(NAME_PASTE))
				TransferHandler.getPasteAction().actionPerformed(newEvent);
			else if (getName().equals(NAME_CUT))
				TransferHandler.getCutAction().actionPerformed(newEvent);
			else if (getName().equals(NAME_DELETE))
				graph.getGraphLayoutCache().remove(
						graph.getDescendants(graph.getSelectionCells()));
			else if (getName().equals(NAME_FIND)) {
				lastSearchPattern = null;
				lastFoundCell = null;
				doFindAgain(graph);
			} else if (getName().equals(NAME_FINDAGAIN))
				doFindAgain(graph);
			else if (getName().equals(NAME_EDIT))
				doEdit(graph);
			else if (getName().equals(NAME_SELECTALL))
				doSelect(graph, true, false, false);
			else if (getName().equals(NAME_CLEARSELECTION))
				graph.clearSelection();
			else if (getName().equals(NAME_SELECTVERTICES))
				doSelect(graph, false, false, false);
			else if (getName().equals(NAME_SELECTEDGES))
				doSelect(graph, false, true, false);
			else if (getName().equals(NAME_DESELECTVERTICES))
				doSelect(graph, false, false, true);
			else if (getName().equals(NAME_DESELECTEDGES))
				doSelect(graph, false, true, true);
			else if (getName().equals(NAME_INVERTSELECTION))
				doInvertSelection(graph);
		} else if (libraryPane != null && !libraryPane.isReadOnly()) {
			ActionEvent newEvent = new ActionEvent(libraryPane
					.getBackingGraph(), e.getID(), e.getActionCommand());

			if (getName().equals(NAME_COPY))
				TransferHandler.getCopyAction().actionPerformed(newEvent);
			else if (getName().equals(NAME_PASTE))
				TransferHandler.getPasteAction().actionPerformed(newEvent);
			else if (getName().equals(NAME_CUT))
				TransferHandler.getCutAction().actionPerformed(newEvent);
			else if (getName().equals(NAME_DELETE)
					&& JGraphpadDialogs.getSharedInstance().confirmDialog(
							libraryPane, getString("DeleteEntry"), true, false))
				libraryPane.removeEntry();
		}

		// Actions that require a focused diagram pane
		JGraphEditorDiagramPane diagramPane = getPermanentFocusOwnerDiagramPane();
		if (diagramPane != null) {
			if (getName().equals(NAME_UNDO))
				doUndo(diagramPane);
			else if (getName().equals(NAME_REDO))
				doRedo(diagramPane);
		}
	}

	/**
	 * Adds or removes the specified cells to/from the selection. If
	 * <code>all</code> is true then <code>edges</code> is ignored.
	 * 
	 * @param graph
	 *            The graph to perform the operation in.
	 * @param all
	 *            Whether all cells should be selected.
	 * @param edges
	 *            Whether edges or vertices should be selected.
	 * @param deselect
	 *            Whether to remove the cells from the selection.
	 */
	protected void doSelect(JGraph graph, boolean all, boolean edges,
			boolean deselect) {
		Object[] cells = (all) ? graph.getRoots() : graph.getGraphLayoutCache()
				.getCells(false, !edges, false, edges);
		if (deselect)
			graph.getSelectionModel().removeSelectionCells(cells);
		else
			graph.addSelectionCells(cells);
	}

	/**
	 * Inverts the selection in the specified graph by selecting all cells for
	 * which {@link #isParentSelected(JGraph, Object)} returns false.
	 * 
	 * @param graph
	 *            The graph to perform the operation in.
	 */
	public void doInvertSelection(JGraph graph) {
		GraphLayoutCache cache = graph.getGraphLayoutCache();
		CellView[] views = cache.getAllDescendants(cache.getRoots());
		List result = new ArrayList(views.length);
		for (int i = 0; i < views.length; i++)
			if (views[i].isLeaf()
					&& !graph.getModel().isPort(views[i].getCell())
					&& !isParentSelected(graph, views[i].getCell()))
				result.add(views[i].getCell());
		graph.setSelectionCells(result.toArray());
	}

	/**
	 * Helper method that returns true if either the cell or one of its parent
	 * is selected in <code>graph</code>.
	 * 
	 * @param graph
	 *            The graph to check if the cell is selected in.
	 * @param cell
	 *            The cell that is to be tested for selection.
	 * @return Returns true if cell or one of its ancestors is selected.
	 */
	protected boolean isParentSelected(JGraph graph, Object cell) {
		do {
			if (graph.isCellSelected(cell))
				return true;
			cell = graph.getModel().getParent(cell);
		} while (cell != null);
		return false;
	}

	/**
	 * Undos the last operation in the specified diagram pane.
	 * 
	 * @param diagramPane
	 *            The diagram pane to perform the operation in.
	 */
	protected void doUndo(JGraphEditorDiagramPane diagramPane) {
		GraphUndoManager manager = diagramPane.getGraphUndoManager();
		GraphLayoutCache cache = diagramPane.getGraph().getGraphLayoutCache();
		if (manager.canUndo(cache))
			manager.undo(cache);
	}

	/**
	 * Undos the last operation in the specified diagram pane.
	 * 
	 * @param diagramPane
	 *            The diagram pane to perform the operation in.
	 */
	protected void doRedo(JGraphEditorDiagramPane diagramPane) {
		GraphUndoManager manager = diagramPane.getGraphUndoManager();
		GraphLayoutCache cache = diagramPane.getGraph().getGraphLayoutCache();
		if (manager.canRedo(cache))
			manager.redo(cache);
	}

	/**
	 * Starts editing the selection cell in the specified graph.
	 * 
	 * @param graph
	 *            The graph to perform the operation in.
	 */
	protected void doEdit(JGraph graph) {
		if (!graph.isSelectionEmpty())
			graph.startEditingAtCell(graph.getSelectionCell());
	}

	/**
	 * Displays a dialog for {@link #lastSearchPattern} if it is null and
	 * performs a search using regular expression matching starting at
	 * {@link #lastFoundCell}. The found cell is selected and scrolled to. If
	 * no cell is found an error message is displayed.
	 * 
	 * @param graph
	 *            The graph to perform the operation in.
	 */
	public void doFindAgain(JGraph graph) {
		if (lastSearchPattern == null) {
			String exp = JGraphpadDialogs.getSharedInstance().valueDialog(
					getString("EnterSearchPattern"), "");
			if (exp != null && exp.length() > 0) {
				try {
					lastSearchPattern = Pattern.compile(".*" + exp + ".*");
				} catch (PatternSyntaxException e) {
					JGraphpadDialogs.getSharedInstance().errorDialog(graph,
							e.getLocalizedMessage());
				}
			}
		}
		if (lastSearchPattern != null) {
			Object[] cells = graph.getRoots();
			boolean active = (lastFoundCell == null);
			Object match = null;
			if (cells != null && cells.length > 0) {

				// Searches for the regular expression in cells
				for (int i = 0; i < cells.length; i++) {
					if (active || match == null) {
						String s = String.valueOf(
								graph.getModel().getValue(cells[i]))
								.replaceAll("\n", "");
						Matcher m = lastSearchPattern.matcher(s);
						if (m.matches()) {
							match = cells[i];
							if (active)
								break;
						}
					}
					active = active || cells[i] == lastFoundCell;
				}
			}
			lastFoundCell = match;
			if (lastFoundCell != null) {
				graph.scrollCellToVisible(lastFoundCell);
				graph.setSelectionCell(lastFoundCell);
			} else {
				JGraphpadDialogs.getSharedInstance().errorDialog(
						getActiveFrame(),
						JGraphEditorResources.getString("NotFound",
								lastSearchPattern.toString()));
			}
		}
	}

	/**
	 * Returns the fallback action.
	 * 
	 * @return Returns the fallbackAction.
	 */
	public Action getFallbackAction() {
		return fallbackAction;
	}

	/**
	 * Bundle of all actions in this class.
	 */
	public static class AllActions implements Bundle {

		/**
		 * Holds the actions.
		 */
		public JGraphEditorAction actionEdit = new JGraphpadEditAction(
				NAME_EDIT), actionSelectAll = new JGraphpadEditAction(
				NAME_SELECTALL),
				actionClearSelection = new JGraphpadEditAction(
						NAME_CLEARSELECTION),
				actionSelectVertices = new JGraphpadEditAction(
						NAME_SELECTVERTICES),
				actionSelectEdges = new JGraphpadEditAction(NAME_SELECTEDGES),
				actionDeselectVertices = new JGraphpadEditAction(
						NAME_DESELECTVERTICES),
				actionDeselectEdges = new JGraphpadEditAction(
						NAME_DESELECTEDGES),
				actionInvertSelection = new JGraphpadEditAction(
						NAME_INVERTSELECTION),
				actionFind = new JGraphpadEditAction(NAME_FIND),
				actionFindAgain = new JGraphpadEditAction(NAME_FINDAGAIN),
				actionUndo = new JGraphpadEditAction(NAME_UNDO),
				actionRedo = new JGraphpadEditAction(NAME_REDO),
				actionCut = new JGraphpadEditAction(NAME_CUT),
				actionCopy = new JGraphpadEditAction(NAME_COPY),
				actionPaste = new JGraphpadEditAction(NAME_PASTE),
				actionDelete = new JGraphpadEditAction(NAME_DELETE);

		/*
		 * (non-Javadoc)
		 */
		public JGraphEditorAction[] getActions() {
			return new JGraphEditorAction[] { actionEdit, actionSelectAll,
					actionClearSelection, actionSelectVertices,
					actionSelectEdges, actionDeselectVertices,
					actionDeselectEdges, actionInvertSelection, actionFind,
					actionUndo, actionRedo, actionFindAgain, actionCut,
					actionCopy, actionPaste, actionDelete };
		}

		/*
		 * (non-Javadoc)
		 */
		public void update() {
			JGraph graph = getPermanentFocusOwnerGraph();
			JGraphEditorDiagramPane diagramPane = getPermanentFocusOwnerDiagramPane();
			if (diagramPane != null) {
				GraphUndoManager manager = diagramPane.getGraphUndoManager();
				GraphLayoutCache glc = diagramPane.getGraph()
						.getGraphLayoutCache();
				actionUndo.setEnabled(manager.canUndo(glc));
				actionRedo.setEnabled(manager.canRedo(glc));
			} else {
				actionUndo.setEnabled(false);
				actionRedo.setEnabled(false);
			}
			boolean isGraphFocused = graph != null;
			boolean isGraphEditable = isGraphFocused && graph.isEditable();
			boolean isSelectionEmpty = !isGraphFocused
					|| graph.isSelectionEmpty();
			JGraphpadLibraryPane libraryPane = JGraphpadFileAction
					.getPermanentFocusOwnerLibraryPane();
			boolean isEntrySelected = libraryPane != null
					&& !libraryPane.isSelectionEmpty();

			actionEdit.setEnabled(isGraphEditable);
			actionFind.setEnabled(isGraphFocused);
			actionFindAgain.setEnabled(isGraphFocused
					&& lastSearchPattern != null);
			actionCut.setEnabled(!isSelectionEmpty || isEntrySelected);
			actionCopy.setEnabled(!isSelectionEmpty || isEntrySelected);
			actionPaste.setEnabled(isGraphFocused || libraryPane != null);
			actionDelete.setEnabled(!isSelectionEmpty || isEntrySelected);
		}

	}

};