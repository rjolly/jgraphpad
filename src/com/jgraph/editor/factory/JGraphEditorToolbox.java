/* 
 * $Id: JGraphEditorToolbox.java,v 1.5 2006/02/03 17:31:13 gaudenz Exp $
 * Copyright (c) 2001-2005, Gaudenz Alder
 * 
 * All rights reserved.
 * 
 * See LICENSE file for license details. If you are unable to locate
 * this file please contact info (at) jgraph (dot) com.
 */
package com.jgraph.editor.factory;

import java.awt.Graphics;
import java.awt.event.MouseEvent;

import javax.swing.AbstractButton;
import javax.swing.JToolBar;

import org.jgraph.JGraph;
import org.jgraph.graph.BasicMarqueeHandler;
import org.w3c.dom.Node;

import com.jgraph.editor.JGraphEditorFactory;
import com.jgraph.editor.JGraphEditorTool;

/**
 * A toolbox that contains a set of tools to be installed as marquee handlers in
 * a graph. Toolboxes are created using the built-in
 * {@link JGraphEditorFactory#createToolbox(Node)}.
 */
public class JGraphEditorToolbox extends JToolBar {

	/**
	 * Specifies whether the toolbox should select the default button after a
	 * gesture with another tool if the default button exists. By default, the
	 * default button is assigned to be the first inserted button (see
	 * JGraphEditorFactory.configureToolbox) and the default for this value is
	 * false.
	 */
	public static boolean SWITCH_BACK = false;

	/**
	 * Holds the marquee redirector.
	 */
	protected BasicMarqueeHandler redirector = new MarqueeRedirector();

	/**
	 * References the previous marquee handler.
	 */
	protected BasicMarqueeHandler previousMarqueeHandler = null;

	/**
	 * References the tool that is currently selected, ie the tool that is used
	 * to interact with the graph.
	 */
	protected JGraphEditorTool selectionTool;

	/**
	 * References the default tool button which is activated after the use of
	 * any other tool.
	 */
	protected AbstractButton defaultButton;

	/**
	 * References the graph that is currently installed, ie the graph that is
	 * used to interact with the selection tool.
	 */
	protected JGraph graph;

	/**
	 * Constructs a new toolbox.
	 */
	public JGraphEditorToolbox() {
		super();
	}

	/**
	 * Returns the installed graph.
	 * 
	 * @return Returns the installed graph.
	 */
	public JGraph getGraph() {
		return graph;
	}

	/**
	 * Sets the installed graph and restores the marquee handler on the
	 * previously installed graph.
	 * 
	 * @param newGraph
	 *            The graph to be installed.
	 */
	public void setGraph(JGraph newGraph) {
		JGraph oldGraph = getGraph();
		if (oldGraph != newGraph) {
			if (oldGraph != null)
				oldGraph.setMarqueeHandler(previousMarqueeHandler);
			previousMarqueeHandler = newGraph.getMarqueeHandler();
			newGraph.setMarqueeHandler(redirector);
			this.graph = newGraph;
		}
	}

	/**
	 * Sets the default tool, which is activated after single use of any other
	 * tool.
	 * 
	 * @param defaultTool
	 *            The default tool to be used.
	 */
	public void setDefaultButton(AbstractButton defaultButton) {
		this.defaultButton = defaultButton;
	}

	/**
	 * Returns the selected tool.
	 * 
	 * @return Returns the selected tool.
	 */
	public JGraphEditorTool getSelectionTool() {
		return selectionTool;
	}

	/**
	 * Sets the selected tool.
	 * 
	 * @param selectionTool
	 *            The tool to be selected.
	 */
	public void setSelectionTool(JGraphEditorTool selectionTool) {
		this.selectionTool = selectionTool;
	}

	/**
	 * A class that redirects marquee events to the marquee handler it replaces
	 * or to the selection tool of the enclosing toolbox depending on the return
	 * value of {@link BasicMarqueeHandler#isForceMarqueeEvent(MouseEvent)} of
	 * the {@link JGraphEditorToolbox#previousMarqueeHandler}.
	 */
	public class MarqueeRedirector extends BasicMarqueeHandler {

		/**
		 * Indicates whether the initial isForceMarqueeEvent returned true.
		 */
		protected boolean redirect = false;

		/**
		 * Returns true if the tool wants to take control of an interaction, ie.
		 * if it handles the sequence of events, or if the previous marquee
		 * handler wants to do so.
		 * 
		 * @param event
		 *            The object that describes the event.
		 * @return Returns the true if the event is handled.
		 */
		public boolean isForceMarqueeEvent(MouseEvent event) {
			if (previousMarqueeHandler != null
					&& previousMarqueeHandler.isForceMarqueeEvent(event)) {
				redirect = true;
				return true;
			} else if (selectionTool != null)
				return selectionTool.isAlwaysActive()
						|| selectionTool.isForceMarqueeEvent(event);
			return false;
		}

		/**
		 * Overrides the basic marquee handler by redirecting to the previous
		 * marquee handler or selection tool's mousePressed method.
		 * 
		 * @param event
		 *            The object that describes the event.
		 */
		public void mousePressed(MouseEvent event) {
			if (redirect || selectionTool == null)
				previousMarqueeHandler.mousePressed(event);
			else
				selectionTool.mousePressed(event);
		}

		/**
		 * Overrides the basic marquee handler by redirecting to the previous
		 * marquee handler or selection tool's mouseDragged method.
		 * 
		 * @param event
		 *            The object that describes the event.
		 */
		public void mouseDragged(MouseEvent event) {
			if (redirect || selectionTool == null)
				previousMarqueeHandler.mouseDragged(event);
			else
				selectionTool.mouseDragged(event);
		}

		/**
		 * Overrides the basic marquee handler by redirecting to the previous
		 * marquee handler or selection tool's mouseReleased method.
		 * 
		 * @param event
		 *            The object that describes the event.
		 */
		public void mouseReleased(MouseEvent event) {
			if (redirect || selectionTool == null) {
				redirect = false;
				previousMarqueeHandler.mouseReleased(event);
			} else
				selectionTool.mouseReleased(event);

			// Switches back to the default button
			if (SWITCH_BACK && defaultButton != null
					&& !defaultButton.isSelected()) {
				defaultButton.setSelected(true);
			}
		}

		/**
		 * Overrides the basic marquee handler to display the selection tool
		 * cursor and redirect to the selection tool.
		 * 
		 * @param event
		 *            The object that describes the event.
		 */
		public void mouseMoved(MouseEvent event) {
			if (selectionTool == null || previousMarqueeHandler != null
					&& previousMarqueeHandler.isForceMarqueeEvent(event))
				previousMarqueeHandler.mouseMoved(event);
			else
				selectionTool.mouseMoved(event);
		}

		/**
		 * Overrides the paint method to redirect to the selection tool.
		 * 
		 * @param graph
		 *            The graph to perform the preview in.
		 * @param g
		 *            The graphics object to be used for painting.
		 */
		public void paint(JGraph graph, Graphics g) {
			if (redirect || selectionTool == null)
				previousMarqueeHandler.paint(graph, g);
			else
				selectionTool.paint(graph, g);
		}

		/**
		 * Overrides the overlay method to redirect to the selection tool.
		 * 
		 * @param graph
		 *            The graph to perform the preview in.
		 * @param g
		 *            The graphics object to be used for painting.
		 * @param clear
		 *            Specifies if the canvas should be cleared.
		 */
		public void overlay(JGraph graph, Graphics g, boolean clear) {
			if (redirect || selectionTool == null)
				previousMarqueeHandler.overlay(graph, g, clear);
			else
				selectionTool.overlay(graph, g, clear);
		}

	}

}
