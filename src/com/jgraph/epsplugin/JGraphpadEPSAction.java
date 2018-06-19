/* 
 * $Id: JGraphpadEPSAction.java,v 1.2 2005/08/07 10:28:29 gaudenz Exp $
 * Copyright (c) 2001-2005, Gaudenz Alder
 * 
 * All rights reserved.
 * 
 * See LICENSE file for license details. If you are unable to locate
 * this file please contact info (at) jgraph (dot) com.
 */
package com.jgraph.epsplugin;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.geom.Rectangle2D;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;

import javax.swing.RepaintManager;

import org.jgraph.JGraph;
import org.jibble.epsgraphics.EpsGraphics2D;

import com.jgraph.JGraphEditor;
import com.jgraph.editor.JGraphEditorAction;
import com.jgraph.pad.action.JGraphpadFileAction;

/**
 * Implements all actions that require EPSGraphics in the classpath.
 */
public class JGraphpadEPSAction extends JGraphpadFileAction {

	/**
	 * Specifies the name for the <code>saveEPS</code> action.
	 */
	public static final String NAME_SAVEEPS = "saveEPS";

	/**
	 * Constructs a new Batik action for the specified name.
	 */
	public JGraphpadEPSAction(String name, JGraphEditor editor) {
		super(name, editor);
	}

	/**
	 * Executes the action based on the action name.
	 * 
	 * @param e
	 *            The object that describes the event.
	 */
	public void actionPerformed(ActionEvent e) {
		Component component = getPermanentFocusOwner();
		try {
			if (component instanceof JGraph) {
				JGraph graph = (JGraph) component;
				if (getName().equals(NAME_SAVEEPS))
					doSaveEPS(graph, 5, dlgs.fileDialog(
							getPermanentFocusOwnerOrParent(),
							getString("SaveEPSFile"), false, ".eps",
							getString("EPSFileDescription"), lastDirectory));
			}
		} catch (IOException e1) {
			dlgs
					.errorDialog(getPermanentFocusOwner(), e1
							.getLocalizedMessage());
		}
	}

	/**
	 * Saves the specified graph as an EPS vector graphics file.
	 * 
	 * @param graph
	 *            The graph to write as an EPS file.
	 * @param inset
	 *            The inset to use for the EPS graphics.
	 * @param filename
	 *            The filename to write the EPS.
	 */
	public void doSaveEPS(JGraph graph, int inset, String filename)
			throws IOException {
		if (filename != null) {
			OutputStream out = editor.getModel().getOutputStream(filename);
			Object[] cells = graph.getDescendants(graph.getRoots());
			if (cells.length > 0) {
				EpsGraphics2D graphics = new EpsGraphics2D();
				graphics.setColor(graph.getBackground());
				Rectangle2D bounds = graph.toScreen(graph.getCellBounds(cells));
				Dimension d = bounds.getBounds().getSize();
				graphics.fillRect(0, 0, d.width + 2 * inset, d.height + 2
						* inset);
				graphics.translate(-bounds.getX() + inset, -bounds.getY()
						+ inset);

				// Paints the graph to the svg generator with no double
				// buffering enabled to make sure we get a vector image.
				RepaintManager currentManager = RepaintManager
						.currentManager(graph);
				currentManager.setDoubleBufferingEnabled(false);
				graph.paint(graphics);
				out.write(graphics.toString().getBytes());
				currentManager.setDoubleBufferingEnabled(true);
			}

			out.close();
			if (JGraphEditor.isURL(filename)) {
				URL url = new URL(filename);
				post(url, url.getFile(), MIME_HTML, out);
			} else
				lastDirectory = new File(filename).getParentFile();
		}
	}

	/**
	 * Bundle of all actions in this class.
	 */
	public static class AllActions implements Bundle {

		/**
		 * Holds the actions. The actions are constructed in the constructor as
		 * they require an editor instance.
		 */
		public JGraphEditorAction actionSaveEPS;

		/**
		 * Constructs the action bundle for the specified editor.
		 * 
		 * @param editor
		 *            The enclosing editor for this bundle.
		 */
		public AllActions(JGraphEditor editor) {
			actionSaveEPS = new JGraphpadEPSAction(NAME_SAVEEPS, editor);
		}

		/*
		 * (non-Javadoc)
		 */
		public JGraphEditorAction[] getActions() {
			return new JGraphEditorAction[] { actionSaveEPS };
		}

		/*
		 * (non-Javadoc)
		 */
		public void update() {
			Component component = getPermanentFocusOwner();
			boolean e = component instanceof JGraph;
			actionSaveEPS.setEnabled(e);
		}

	}

}