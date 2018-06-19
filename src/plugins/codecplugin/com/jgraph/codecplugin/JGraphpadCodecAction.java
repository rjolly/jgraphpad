/* 
 * $Id: JGraphpadCodecAction.java,v 1.5 2005/08/07 14:24:57 gaudenz Exp $
 * Copyright (c) 2001-2005, Gaudenz Alder
 * 
 * All rights reserved.
 * 
 * See LICENSE file for license details. If you are unable to locate
 * this file please contact info (at) jgraph (dot) com.
 */
package com.jgraph.codecplugin;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.geom.Rectangle2D;
import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.URL;

import org.jgraph.JGraph;
import org.jgraph.graph.CellView;
import org.jgraph.graph.DefaultGraphModel;
import org.jgraph.graph.GraphLayoutCache;

import com.jgraph.JGraphEditor;
import com.jgraph.JGraphpad;
import com.jgraph.editor.JGraphEditorAction;
import com.jgraph.editor.factory.JGraphEditorDiagramPane;
import com.jgraph.pad.action.JGraphpadFileAction;
import com.jgraph.pad.graph.JGraphpadBusinessObject;

/**
 * Export actions for HTML image maps, gxl and graphviz.
 */
public class JGraphpadCodecAction extends JGraphpadFileAction {

	/**
	 * Specifies the name for the <code>saveImageMap</code> action.
	 */
	public static final String NAME_SAVEIMAGEMAP = "saveImageMap";

	/**
	 * Specifies the name for the <code>saveGXL</code> action.
	 */
	public static final String NAME_SAVEGXL = "saveGXL";

	/**
	 * Specifies the name for the <code>importGXL</code> action.
	 */
	public static final String NAME_IMPORTGXL = "importGXL";

	/**
	 * Specifies the name for the <code>saveGraphviz</code> action.
	 */
	public static final String NAME_SAVEGRAPHVIZ = "saveGraphviz";

	/**
	 * Constructs a new export action for the specified name.
	 */
	protected JGraphpadCodecAction(String name, JGraphEditor editor) {
		super(name, editor);
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
				if (getName().equals(NAME_SAVEIMAGEMAP))
					doSaveImageMap(graph, dlgs.imageFileDialog(
							getPermanentFocusOwnerOrParent(),
							getString("SaveImage"), false, lastDirectory), 0);
				else if (getName().equals(NAME_SAVEGRAPHVIZ))
					doSave(dlgs
							.fileDialog(getPermanentFocusOwnerOrParent(),
									getString("SaveGraphvizFile"), false,
									".dot",
									getString("GraphvizFileDescription"),
									lastDirectory), JGraphpadGraphvizCodec
							.encode(
									graph,
									(graph.isSelectionEmpty()) ? graph
											.getRoots() : graph
											.getSelectionCells()).getBytes());
				else if (getName().equals(NAME_SAVEGXL))
					doSave(dlgs.fileDialog(getPermanentFocusOwnerOrParent(),
							getString("SaveGXLFile"), false, ".xml",
							getString("GXLFileDescription"), lastDirectory),
							JGraphpadGXLCodec.encode(
									graph,
									(graph.isSelectionEmpty()) ? graph
											.getRoots() : graph
											.getSelectionCells()).getBytes());
				else if (getName().equals(NAME_IMPORTGXL))
					doImportGXL(graph.getGraphLayoutCache(), dlgs.fileDialog(
							getPermanentFocusOwnerOrParent(), getString("OpenGXLFile"),
							true, ".xml", getString("GXLFileDescription"),
							lastDirectory));
			}
		} catch (Exception e) {
			dlgs.errorDialog(getPermanentFocusOwner(), e.getLocalizedMessage());
		}
	}

	/**
	 * Imports the specified GXL file into the specified graph.
	 * 
	 * @param cache
	 *            The graph layout cache to import into.
	 * @param filename
	 *            The filename to import from.
	 * @throws Exception
	 */
	protected void doImportGXL(GraphLayoutCache cache, String filename)
			throws Exception {
		if (filename != null) {
			InputStream in = editor.getModel().getInputStream(filename);
			Object vertexPrototype = getValue(KEY_VERTEXPROTOTYPE);
			Object edgePrototype = getValue(KEY_EDGEPROTOTYPE);
			JGraphpadGXLCodec.decode(in, cache, vertexPrototype, edgePrototype);
			in.close();
			lastDirectory = new File(filename).getParentFile();
		}
	}

	/**
	 * Exports the specified graph as an image map. The filename and map name
	 * are constructed out of the specified image filename.
	 * 
	 * @param graph
	 *            The graph to be exported as an image map.
	 * @param filename
	 *            The filename of the image to export.
	 * @param inset
	 *            The inset to apply to the image and coordinates.
	 */
	protected void doSaveImageMap(JGraph graph, String filename, int inset)
			throws Exception {
		super.doSaveImage(JGraphEditorDiagramPane.getParentDiagramPane(graph),
				inset, filename);
		String basename = filename.substring(0, filename.lastIndexOf("."));
		String htmlFile = basename + ".html";
		if (htmlFile != null) {
			OutputStream out = editor.getModel().getOutputStream(htmlFile);
			String mapName = new File(basename).getName();
			out.write(new String("<IMG SRC=\""
					+ JGraphEditor.toURL(new File(filename))
					+ "\" BORDER=\"0\" ISMAP USEMAP=\"#" + mapName + "\">")
					.getBytes());
			writeMap(graph, mapName, inset, out);
			out.flush();
			out.close();
			if (JGraphEditor.isURL(filename)) {
				URL url = new URL(filename);
				post(url, url.getFile(), MIME_HTML, out);
			} else
				lastDirectory = new File(filename).getParentFile();
		}
	}

	/**
	 * Writes a HTML image map to the specified stream applying inset to the
	 * coordinates of all cells. To find the URL and alt tag value of each cell,
	 * the {@link #getURL(JGraph, Object)} and {@link #getAlt(JGraph, Object)}
	 * methods are used respectively.
	 * 
	 * @param graph
	 *            The graph to create the image map for.
	 * @param mapName
	 *            The name of the image map in the HTML code.
	 * @return Returns the number of entries in the map.
	 */
	protected int writeMap(JGraph graph, String mapName, int inset,
			OutputStream out) {
		PrintWriter writer = new PrintWriter(out);
		int written = 0;
		if (graph.getModel().getRootCount() > 0) {
			writer.println("<MAP NAME=\"" + mapName + "\">");
			Rectangle2D bounds = graph.getCellBounds(graph.getRoots());
			Object[] vertices = DefaultGraphModel.getAll(graph.getModel());
			for (int i = 0; i < vertices.length; i++) {
				String alt = getAlt(graph, vertices[i]);
				String href = getURL(graph, vertices[i]);
				CellView view = graph.getGraphLayoutCache().getMapping(
						vertices[i], false);
				if (view != null && href != null && href.length() > 0) {
					written++;
					Rectangle2D b = (Rectangle2D) graph
							.toScreen((Rectangle2D) ((Rectangle2D) view
									.getBounds()).clone());
					b.setFrame(b.getX() - bounds.getX() + inset, b.getY()
							- bounds.getY() + inset, b.getWidth(), b
							.getHeight());
					String rect = (int) b.getX() + "," + (int) b.getY() + ","
							+ (int) (b.getX() + b.getWidth()) + ","
							+ (int) (b.getY() + b.getHeight());
					String shape = "RECT";
					writer.println("<AREA SHAPE=" + shape + " COORDS=\"" + rect
							+ "\" HREF=\"" + href + "\" ALT=\"" + alt + "\">");
				}
			}
			writer.println("</MAP>");
			writer.flush();
			writer.close();
		}
		return written;
	}

	/**
	 * Returns the URL to be used in the image map of <code>graph</code> for
	 * the specified cell.
	 * 
	 * @param graph
	 *            The graph for which the image map is being created for.
	 * @param cell
	 *            The cell to return the URL for.
	 * @return Returns a URL for <code>cell</code>.
	 */
	protected String getURL(JGraph graph, Object cell) {
		String url = getAlt(graph, cell);
		if (JGraphEditor.isURL(url))
			return url;
		Object value = graph.getModel().getValue(cell);
		if (JGraphEditor.isURL(value))
			return String.valueOf(value);
		if (value instanceof JGraphpadBusinessObject) {
			JGraphpadBusinessObject obj = (JGraphpadBusinessObject) value;
			Object property = obj.getProperty("url");
			if (JGraphEditor.isURL(property))
				return String.valueOf(property);
		}
		return "";
	}

	/**
	 * Returns the value for the alt tag to be used in the image map of
	 * <code>graph</code> for the specified cell.
	 * 
	 * @param graph
	 *            The graph for which the image map is being created for.
	 * @param cell
	 *            The cell to return the value of the alt tag for.
	 * @return Returns a alt tag for <code>cell</code>.
	 */
	protected String getAlt(JGraph graph, Object cell) {
		return graph.convertValueToString(cell);
	}

	/**
	 * Bundle of all actions in this class.
	 */
	public static class AllActions implements Bundle {

		/**
		 * Holds the actions. All actions require an editor reference and are
		 * therefore created at construction time.
		 */
		public JGraphEditorAction actionSaveImageMap, actionSaveGraphViz,
				actionSaveGXL, actionImportGXL;

		/**
		 * Constructs the action bundle for the enclosing class.
		 */
		public AllActions(JGraphEditor editor) {
			actionSaveImageMap = new JGraphpadCodecAction(NAME_SAVEIMAGEMAP,
					editor);
			actionSaveGraphViz = new JGraphpadCodecAction(NAME_SAVEGRAPHVIZ,
					editor);
			actionSaveGXL = new JGraphpadCodecAction(NAME_SAVEGXL, editor);
			actionImportGXL = new JGraphpadCodecAction(NAME_IMPORTGXL, editor);
			Object vertexPrototype = editor.getSettings().getObject(
					JGraphpad.KEY_VERTEXPROTOTYPE);
			Object edgePrototype = editor.getSettings().getObject(
					JGraphpad.KEY_EDGEPROTOTYPE);
			actionImportGXL.putValue(KEY_VERTEXPROTOTYPE, vertexPrototype);
			actionImportGXL.putValue(KEY_EDGEPROTOTYPE, edgePrototype);
		}

		/*
		 * (non-Javadoc)
		 */
		public JGraphEditorAction[] getActions() {
			return new JGraphEditorAction[] { actionSaveImageMap,
					actionSaveGraphViz, actionSaveGXL, actionImportGXL };
		}

		/*
		 * (non-Javadoc)
		 */
		public void update() {
			Component component = getPermanentFocusOwner();
			boolean e = component instanceof JGraph;
			actionSaveImageMap.setEnabled(e);
			actionSaveGraphViz.setEnabled(e);
			actionSaveGXL.setEnabled(e);
			actionImportGXL.setEnabled(e);
		}
	}

}
