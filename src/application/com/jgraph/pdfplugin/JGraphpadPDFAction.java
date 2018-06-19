/* 
 * $Id: JGraphpadPDFAction.java,v 1.2 2005/08/07 10:28:29 gaudenz Exp $
 * Copyright (c) 2001-2005, Gaudenz Alder
 * 
 * All rights reserved.
 * 
 * See LICENSE file for license details. If you are unable to locate
 * this file please contact info (at) jgraph (dot) com.
 */
package com.jgraph.pdfplugin;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.geom.Rectangle2D;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;

import javax.swing.RepaintManager;

import org.jgraph.JGraph;

import com.jgraph.JGraphEditor;
import com.jgraph.editor.JGraphEditorAction;
import com.jgraph.pad.action.JGraphpadFileAction;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfWriter;

/**
 * Implements all actions that require EPSGraphics in the classpath.
 */
public class JGraphpadPDFAction extends JGraphpadFileAction {

	/**
	 * Specifies the name for the <code>savePDF</code> action.
	 */
	public static final String NAME_SAVEPDF = "savePDF";

	/**
	 * Constructs a new Batik action for the specified name.
	 */
	public JGraphpadPDFAction(String name, JGraphEditor editor) {
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
				if (getName().equals(NAME_SAVEPDF))
					doSavePDF(graph, 5, dlgs.fileDialog(
							getPermanentFocusOwnerOrParent(),
							getString("SavePDFFile"), false, ".pdf",
							getString("PDFFileDescription"), lastDirectory));
			}
		} catch (Exception e1) {
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
	 * @throws DocumentException
	 */
	public void doSavePDF(JGraph graph, int inset, String filename)
			throws IOException, DocumentException {
		if (filename != null) {
			OutputStream out = editor.getModel().getOutputStream(filename);
			Object[] cells = graph.getDescendants(graph.getRoots());
			if (cells.length > 0) {
				Document document = new Document();
				PdfWriter writer = PdfWriter.getInstance(document, out);
				document.open();
				PdfContentByte cb = writer.getDirectContent();
				cb.saveState();
				cb.concatCTM(1, 0, 0, 1, 50, 400);
				Rectangle2D bounds = graph.toScreen(graph.getCellBounds(cells));
				Dimension d = bounds.getBounds().getSize();
				Graphics2D g2 = cb.createGraphics(d.width + 10, d.height + 10);
				g2.setColor(graph.getBackground());
				g2.fillRect(0, 0, d.width + 2 * inset, d.height + 2 * inset);
				g2.translate(-bounds.getX() + inset, -bounds.getY() + inset);

				// Paints the graph to the svg generator with no double
				// buffering enabled to make sure we get a vector image.
				RepaintManager currentManager = RepaintManager
						.currentManager(graph);
				currentManager.setDoubleBufferingEnabled(false);
				graph.paint(g2);
				currentManager.setDoubleBufferingEnabled(true);
				g2.dispose();
				cb.restoreState();
				document.close();
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
		public JGraphEditorAction actionSavePDF;

		/**
		 * Constructs the action bundle for the specified editor.
		 * 
		 * @param editor
		 *            The enclosing editor for this bundle.
		 */
		public AllActions(JGraphEditor editor) {
			actionSavePDF = new JGraphpadPDFAction(NAME_SAVEPDF, editor);
		}

		/*
		 * (non-Javadoc)
		 */
		public JGraphEditorAction[] getActions() {
			return new JGraphEditorAction[] { actionSavePDF };
		}

		/*
		 * (non-Javadoc)
		 */
		public void update() {
			Component component = getPermanentFocusOwner();
			boolean e = component instanceof JGraph;
			actionSavePDF.setEnabled(e);
		}

	}

}