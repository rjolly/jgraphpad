/* 
 * $Id: JGraphpadTransferHandler.java,v 1.1.1.1 2005/08/04 11:21:58 gaudenz Exp $
 * Copyright (c) 2001-2005, Gaudenz Alder
 * 
 * All rights reserved.
 * 
 * See LICENSE file for license details. If you are unable to locate
 * this file please contact info (at) jgraph (dot) com.
 */
package com.jgraph.pad.graph;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.io.Reader;
import java.util.List;
import java.util.Map;

import javax.swing.ImageIcon;
import javax.swing.JComponent;

import org.jgraph.JGraph;
import org.jgraph.graph.DefaultGraphModel;
import org.jgraph.graph.GraphConstants;
import org.jgraph.graph.GraphModel;
import org.jgraph.graph.GraphTransferHandler;
import org.jgraph.plaf.basic.BasicGraphUI;

import com.jgraph.JGraphEditor;
import com.jgraph.JGraphpad;
import com.jgraph.pad.util.JGraphpadImageIcon;

/**
 * Transfer handler for graphs that accepts external files and text.
 */
public class JGraphpadTransferHandler extends GraphTransferHandler {

	/**
	 * Holds the prototype which is used to create new vertices.
	 */
	protected transient Object prototype;

	/**
	 * Constructs a new transfer handler for the specified prototype.
	 * 
	 * @param prototype
	 *            The prototype to be used to create new cells.
	 */
	public JGraphpadTransferHandler(Object prototype) {
		this.prototype = prototype;
	}

	/**
	 * Constructs a new cell from {@link #prototype} using the specified model
	 * and assigns it the new user object.
	 * 
	 * @param model
	 *            The model to use for cloning the prototype.
	 * @param userObject
	 *            The user object to be assigned to the clone.
	 * @return Returns a prototype clone with the specified user object.
	 */
	protected Object createCell(GraphModel model, Object userObject) {
		Object cell = DefaultGraphModel.cloneCell(model, prototype);
		model.valueForCellChanged(cell, userObject);
		return cell;
	}

	/**
	 * Extends the parent implementation to accept text and
	 * {@link DataFlavor#javaFileListFlavor} flavors.
	 * 
	 * @param comp
	 *            The component to drop the transferable to.
	 * @param flavors
	 *            The supported data flavors of the transferable.
	 * @return Returns true if the flavor is accepted.
	 */
	public boolean canImport(JComponent comp, DataFlavor[] flavors) {
		if (flavors != null) {
			if (DataFlavor.selectBestTextFlavor(flavors) != null)
				return true;
			for (int i = 0; i < flavors.length; i++)
				if (flavors[i].equals(DataFlavor.javaFileListFlavor))
					return true;
		}
		return super.canImport(comp, flavors);
	}

	/**
	 * Overrides the parent implementation to import transferable data. Note: It
	 * is a special rule in JGraph not to override the
	 * {@link GraphTransferHandler#importData(JComponent, Transferable)} method
	 * because the return value has a different semantic. This implementation
	 * silently ignores all exceptions.
	 * 
	 * @param comp
	 *            The component to drop the transferable to.
	 * @param t
	 *            The transferable data to be inserted into the component.
	 * @return Returns true if the transferable data was inserted.
	 */
	public boolean importDataImpl(JComponent comp, Transferable t) {
		if (super.importDataImpl(comp, t))
			return true; // exit
		else if (comp instanceof JGraph && ((JGraph) comp).isDropEnabled()) {
			try {
				JGraph graph = (JGraph) comp;
				Point2D p = getInsertionLocation(graph);

				// Drops the java file list as a sequence of cells
				if (t.isDataFlavorSupported(DataFlavor.javaFileListFlavor))
					insertValues(graph, p, ((List) t
							.getTransferData(DataFlavor.javaFileListFlavor))
							.toArray());

				// Drops as single text cell
				else {
					DataFlavor bestFlavor = DataFlavor.selectBestTextFlavor(t
							.getTransferDataFlavors());
					if (bestFlavor != null) {

						// Constructs a string using the reader and passes
						// the text to the insertText method.
						Reader reader = bestFlavor.getReaderForText(t);
						insertValues(graph, p,
								new Object[] { getString(reader) });
					}
				}
			} catch (Exception exception) {
				// ignore
			}
		}
		return false;
	}

	/**
	 * Helper method to return the insertion location for a drop operation in
	 * the specified graph. This implementation returns a scaled clone of
	 * {@link BasicGraphUI#getInsertionLocation()} or a dummy point if the
	 * insertion location is not available in the graph ui.
	 * 
	 * @param graph
	 *            The graph to determine the insertion location for.
	 * @return Returns the insertion location for the specified graph.
	 */
	protected Point2D getInsertionLocation(JGraph graph) {
		Point2D p = graph.getUI().getInsertionLocation();
		if (p != null)
			p = graph.fromScreen(graph.snap((Point2D) p.clone()));

		// Returns a dummy point at (gridSize, gridSize) in case
		// the insertion location is not available from the ui.
		if (p == null) {
			double gs = graph.getGridSize();
			p = new Point((int) gs, (int) gs);
		}
		return p;
	}

	/**
	 * Helper method to read the specified reader into a string.
	 * 
	 * @param reader
	 *            The reader to read into the string.
	 * @return Returns the string stored in the reader.
	 */
	protected String getString(Reader reader) throws IOException {
		StringBuffer s = new StringBuffer();
		char[] c = new char[1];
		while (reader.read(c) != -1)
			s.append(c);
		return s.toString();
	}

	/**
	 * Inserts the specified list of values into the specified graph. If a known
	 * image extension is seen then the icon attribute is setup accordingly.
	 * 
	 * @param graph
	 *            The graph to insert the cells into.
	 * @param p
	 *            The point at which to insert the cells.
	 * @param values
	 *            The list of values to be inserted.
	 */
	protected void insertValues(JGraph graph, Point2D p, Object[] values) {
		double gs = graph.getGridSize();
		Dimension d = new Dimension((int) gs, (int) (2 * gs));
		Object[] cells = new Object[values.length];

		// Loops through the files in the list and creates
		// a cell for each one, using the file as an icon
		// if it has a n known extension.
		for (int i = 0; i < values.length; i++) {
			cells[i] = createCell(graph.getModel(), values[i]);
			Map attrs = graph.getModel().getAttributes(cells[i]);

			// Uses the file as an icon if it has a known extension
			String url = String.valueOf(cells[i]).toLowerCase();
			if (JGraphpad.isImage(url)) {
				ImageIcon icon = (JGraphEditor.isURL(url)) ? new JGraphpadImageIcon(
						JGraphEditor.toURL(url))
						: new JGraphpadImageIcon(url);
				if (icon != null)
					GraphConstants.setIcon(attrs, icon);
			}

			// Sets the default bounds of the cell forcing a resize
			GraphConstants.setResize(attrs, true);
			GraphConstants.setBounds(attrs, new Rectangle2D.Double(p.getX(), p
					.getY(), d.getWidth(), d.getHeight()));

			// Shifts the point for the next cell to be inserted
			p.setLocation(p.getX(), p.getY() + d.getHeight() + 1.5 * gs);
			graph.snap(p);
		}

		// Inserts the cells into the graph
		graph.getGraphLayoutCache().insert(cells);
	}

}