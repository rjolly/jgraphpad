/* 
 * $Id: JGraphpadLayoutAction.java,v 1.2 2005/08/09 10:12:04 david Exp $
 * Copyright (c) 2001-2005, Gaudenz Alder
 * 
 * All rights reserved.
 * 
 * See LICENSE file for license details. If you are unable to locate
 * this file please contact info (at) jgraph (dot) com.
 */
package com.jgraph.layoutplugin;

import java.awt.Component;
import java.awt.event.ActionEvent;

import org.jgraph.JGraph;

import com.jgraph.algebra.JGraphAlgebra;
import com.jgraph.algebra.cost.JGraphDistanceCostFunction;
import com.jgraph.editor.JGraphEditorAction;
import com.jgraph.pad.action.JGraphpadFormatAction;
import com.jgraph.pad.dialog.JGraphpadDialogs;

/**
 * Implements all actions that require JGraph Layout Pro in the classpath.
 */
public class JGraphpadLayoutAction extends JGraphpadFormatAction {

	/**
	 * Specifies the name for the <code>layout</code> action.
	 */
	public static final String NAME_LAYOUT = "layout";

	/**
	 * Specifies the name for the <code>selectPath</code> action.
	 */
	public static final String NAME_SELECTPATH = "selectPath";

	/**
	 * Specifies the name for the <code>selectTree</code> action.
	 */
	public static final String NAME_SELECTTREE = "selectTree";

	/**
	 * Holds to the layout panel. The factory method that is in charge of
	 * creating the layout panel will search for this action and update the
	 * reference when it is executed.
	 */
	protected JGraphpadLayoutPanel layoutPanel;

	/**
	 * Constructs a new layout action for the specified name.
	 */
	public JGraphpadLayoutAction(String name) {
		super(name);
	}

	/**
	 * Returns the layout panel to be used to perform the layout.
	 * 
	 * @return Returns the layoutPanel.
	 */
	public JGraphpadLayoutPanel getLayoutPanel() {
		return layoutPanel;
	}

	/**
	 * Sets the layout panel to be used to perform the layout.
	 * 
	 * @param layoutPanel
	 *            The layoutPanel to set.
	 */
	public void setLayoutPanel(JGraphpadLayoutPanel layoutPanel) {
		this.layoutPanel = layoutPanel;
	}

	/**
	 * Executes the action based on the action name.
	 * 
	 * @param event
	 *            The object that describes the event.
	 */
	public void actionPerformed(ActionEvent event) {
		Component component = getPermanentFocusOwner();
		try {
			if (component instanceof JGraph) {
				JGraph graph = (JGraph) component;
				if (getName().equals(NAME_LAYOUT))
					doLayout();
				else if (getName().equals(NAME_SELECTTREE))
					doSelectMST(graph);
				else if (getName().equals(NAME_SELECTPATH))
					doSelectPath(graph, !JGraphpadDialogs.getSharedInstance()
							.confirmDialog(getPermanentFocusOwner(),
									getString("AllowUndirectedPath"), true,
									false));
			}
		} catch (Exception e) {
			JGraphpadDialogs.getSharedInstance().errorDialog(component,
					e.getMessage());
		}
	}

	/**
	 * Invokes {@link JGraphpadLayoutPanel#execute()} if the
	 * {@link #layoutPanel} has been assigned by the respective factory method.
	 */
	public void doLayout() {
		if (layoutPanel != null)
			layoutPanel.execute();
	}

	/**
	 * Selects the minimum spanning tree in the specified graph.
	 * 
	 * @param graph
	 *            The graph to perform the operation in.
	 */
	public void doSelectMST(JGraph graph) {
		Object[] v = graph.getGraphLayoutCache().getCells(false, true, false,
				false);
		Object[] e = graph.getGraphLayoutCache().getCells(false, false, false,
				true);
		Object[] mst = JGraphAlgebra.getSharedInstance()
				.getMinimumSpanningTree(
						graph.getModel(),
						v,
						e,
						new JGraphDistanceCostFunction(graph
								.getGraphLayoutCache()));
		graph.setSelectionCells(mst);
	}

	/**
	 * Selects the shortest (directed) path between two selected vertices in the
	 * specified graph.
	 * 
	 * @param graph
	 *            The graph to perform the operation in.
	 */
	public void doSelectPath(JGraph graph, boolean directed) {
		Object[] v = graph.getGraphLayoutCache().getCells(false, true, false,
				false);
		Object from = graph.getSelectionCell();
		Object to = graph.getSelectionCells()[1];
		Object[] path = JGraphAlgebra.getSharedInstance().getShortestPath(
				graph.getModel(), from, to,
				new JGraphDistanceCostFunction(graph.getGraphLayoutCache()),
				v.length, directed);
		if (path == null || path.length == 0)
			JGraphpadDialogs.getSharedInstance().errorDialog(
					getPermanentFocusOwner(), getString("PathNotFound"));
		else
			graph.setSelectionCells(path);
	}

	/**
	 * Bundle of all actions in this class.
	 */
	public static class AllActions implements Bundle {

		/**
		 * Holds the actions.
		 */
		public JGraphEditorAction actionLayout = new JGraphpadLayoutAction(
				NAME_LAYOUT), actionSelectTree = new JGraphpadLayoutAction(
				NAME_SELECTTREE), actionSelectPath = new JGraphpadLayoutAction(
				NAME_SELECTPATH);

		/*
		 * (non-Javadoc)
		 */
		public JGraphEditorAction[] getActions() {
			return new JGraphEditorAction[] { actionLayout, actionSelectTree,
					actionSelectPath };
		}

		/*
		 * (non-Javadoc)
		 */
		public void update() {
			Component component = getPermanentFocusOwner();
			boolean e = component instanceof JGraph;

			boolean vertices = false;
			int rootCount = 0;
			if (e) {
				JGraph graph = (JGraph) component;
				Object[] v = graph.getSelectionCells(graph
						.getGraphLayoutCache().getCells(false, true, false,
								false));
				vertices = v.length > 1
						&& v.length == graph.getSelectionCount();
				rootCount = graph.getModel().getRootCount();
			}
			actionLayout.setEnabled(rootCount > 0);
			actionSelectTree.setEnabled(rootCount > 0);
			actionSelectPath.setEnabled(vertices);
		}
	}

}