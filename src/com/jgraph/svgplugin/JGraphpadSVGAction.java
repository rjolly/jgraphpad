/* 
 * $Id: JGraphpadSVGAction.java,v 1.2 2005/08/07 10:28:29 gaudenz Exp $
 * Copyright (c) 2001-2005, Gaudenz Alder
 * 
 * All rights reserved.
 * 
 * See LICENSE file for license details. If you are unable to locate
 * this file please contact info (at) jgraph (dot) com.
 */
package com.jgraph.svgplugin;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.geom.Rectangle2D;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.net.URL;

import javax.swing.RepaintManager;

import org.apache.batik.dom.GenericDOMImplementation;
import org.apache.batik.svggen.SVGGraphics2D;
import org.apache.batik.svggen.SVGGraphics2DIOException;
import org.jgraph.JGraph;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;

import com.jgraph.JGraphEditor;
import com.jgraph.editor.JGraphEditorAction;
import com.jgraph.editor.JGraphEditorResources;
import com.jgraph.pad.action.JGraphpadFileAction;

/**
 * Implements all actions that require Batik in the classpath.
 */
public class JGraphpadSVGAction extends JGraphpadFileAction {

	/**
	 * Defines the mime type for SVG content (image/svg+xml).
	 */
	public static final String MIME_SVG = "image/svg+xml";

	/**
	 * Specifies the name for the <code>saveSVG</code> action.
	 */
	public static final String NAME_SAVESVG = "saveSVG";

	/**
	 * Specifies the name for the <code>svgServer</code> action.
	 */
	public static final String NAME_SVGSERVER = "svgServer";

	/**
	 * Holds the running server instance.
	 */
	protected JGraphpadSVGServer server = null;

	/**
	 * Constructs a new Batik action for the specified name.
	 */
	public JGraphpadSVGAction(String name, JGraphEditor editor) {
		super(name, editor);
		if (name.equals(NAME_SVGSERVER))
			setToggleAction(true);
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
			if (getName().equals(NAME_SVGSERVER))
				doSVGServer();
			if (component instanceof JGraph) {
				JGraph graph = (JGraph) component;
				if (getName().equals(NAME_SAVESVG))
					doSaveSVG(graph, 0, dlgs.fileDialog(
							getPermanentFocusOwnerOrParent(),
							getString("SaveSVGFile"), false, ".svg",
							getString("SVGFileDescription"), lastDirectory));
			}
		} catch (IOException e1) {
			dlgs
					.errorDialog(getPermanentFocusOwner(), e1
							.getLocalizedMessage());
		}
	}

	/**
	 * Starts or stops the {@link #server}.
	 */
	protected void doSVGServer() throws IOException {
		setSelected(false);
		if (server == null) {
			String port = dlgs.valueDialog(getString("EnterPortNumber"));
			if (port != null) {
				server = new JGraphpadSVGServer(editor, Integer.parseInt(port));
				dlgs.informationDialog(getPermanentFocusOwner(),
						JGraphEditorResources.getString("ServerRunningOnPort",
								String.valueOf(port)));
			}
			setSelected(true);
		} else {
			server.getServerSocket().close();
			server = null;
		}
	}

	/**
	 * Saves the specified graph as an SVG vector graphics file.
	 * 
	 * @param graph
	 *            The graph to write as an SVG file.
	 * @param inset
	 *            The inset to use for the SVG graphics.
	 * @param filename
	 *            The filename to write the SVG.
	 */
	public void doSaveSVG(JGraph graph, int inset, String filename)
			throws IOException {
		if (filename != null) {
			OutputStream out = editor.getModel().getOutputStream(filename);
			writeSVG(graph, out, inset);
			out.close();
			if (JGraphEditor.isURL(filename)) {
				URL url = new URL(filename);
				post(url, url.getFile(), MIME_SVG, out);
			} else
				lastDirectory = new File(filename).getParentFile();
		}
	}

	/**
	 * Writes the specified graph as an SVG stream to the specified output
	 * stream.
	 * 
	 * @param graph
	 *            The graph to be converted into an SVG stream.
	 * @param out
	 *            The output stream to use for writing out the SVG stream.
	 * @param inset
	 *            The inset of the SVG graphics.
	 */
	public static void writeSVG(JGraph graph, OutputStream out, int inset)
			throws UnsupportedEncodingException, SVGGraphics2DIOException {
		Object[] cells = graph.getRoots();
		Rectangle2D bounds = graph.toScreen(graph.getCellBounds(cells));
		if (bounds != null) {
			// Constructs the svg generator used for painting the graph to
			DOMImplementation domImpl = GenericDOMImplementation
					.getDOMImplementation();
			Document document = domImpl.createDocument(null, "svg", null);
			SVGGraphics2D svgGenerator = new SVGGraphics2D(document);
			svgGenerator.translate(-bounds.getX() + inset, -bounds.getY()
					+ inset);

			// Paints the graph to the svg generator with no double
			// buffering enabled to make sure we get a vector image.
			RepaintManager currentManager = RepaintManager
					.currentManager(graph);
			currentManager.setDoubleBufferingEnabled(false);
			graph.paint(svgGenerator);

			// Writes the graph to the specified file as an SVG stream
			Writer writer = new OutputStreamWriter(out, "UTF-8");
			svgGenerator.stream(writer, false);

			currentManager.setDoubleBufferingEnabled(true);
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
		public JGraphEditorAction actionSaveSVG, actionSVGServer;

		/**
		 * Constructs the action bundle for the specified editor.
		 * 
		 * @param editor
		 *            The enclosing editor for this bundle.
		 */
		public AllActions(JGraphEditor editor) {
			actionSaveSVG = new JGraphpadSVGAction(NAME_SAVESVG, editor);
			actionSVGServer = new JGraphpadSVGAction(NAME_SVGSERVER, editor);
		}

		/*
		 * (non-Javadoc)
		 */
		public JGraphEditorAction[] getActions() {
			return new JGraphEditorAction[] { actionSaveSVG, actionSVGServer };
		}

		/*
		 * (non-Javadoc)
		 */
		public void update() {
			Component component = getPermanentFocusOwner();
			boolean e = component instanceof JGraph;
			actionSaveSVG.setEnabled(e);
		}

	}

}