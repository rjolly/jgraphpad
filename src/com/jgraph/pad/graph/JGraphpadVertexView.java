/* 
 * $Id: JGraphpadVertexView.java,v 1.8 2006/03/15 07:23:29 gaudenz Exp $
 * Copyright (c) 2001-2005, Gaudenz Alder
 * 
 * All rights reserved.
 * 
 * See LICENSE file for license details. If you are unable to locate
 * this file please contact info (at) jgraph (dot) com.
 */
package com.jgraph.pad.graph;

import javax.swing.tree.DefaultMutableTreeNode;

import org.jgraph.graph.CellViewRenderer;
import org.jgraph.graph.GraphCellEditor;
import org.jgraph.graph.VertexView;

/**
 * Vertex view that supports {@link JGraphpadBusinessObject} rendering and
 * in-place editing, that means it supports simple text, rich text and component
 * values.
 */
public class JGraphpadVertexView extends VertexView {

	/**
	 * Holds the static editor for views of this kind.
	 */
	public static JGraphpadRichTextEditor editor = new JGraphpadRichTextEditor();

	/**
	 * Holds the static editor for views of this kind.
	 */
	public static JGraphpadRedirectingEditor redirector = new JGraphpadRedirectingEditor();

	/**
	 * Holds the static renderer for views of this kind.
	 */
	public static JGraphpadVertexRenderer renderer = new JGraphpadVertexRenderer();

	/**
	 * Empty constructor for persistence.
	 */
	public JGraphpadVertexView() {
		super();
	}

	/**
	 * Constructs a new vertex view for the specified cell.
	 * 
	 * @param cell
	 *            The cell to construct the vertex view for.
	 */
	public JGraphpadVertexView(Object cell) {
		super(cell);
	}

	/**
	 * Returns {@link #editor} if the user object of the cell is a rich text
	 * value or {@link #redirector} if the user object is a component.
	 * 
	 * @return Returns the editor for the cell view.
	 */
	public GraphCellEditor getEditor() {
		Object value = ((DefaultMutableTreeNode) getCell()).getUserObject();
		if (value instanceof JGraphpadBusinessObject) {
			JGraphpadBusinessObject obj = (JGraphpadBusinessObject) value;
			if (obj.isRichText())
				return editor;
			else if (obj.isComponent())
				return redirector;
		}
		return super.getEditor();
	}

	/**
	 * Returns the {@link #renderer}.
	 * 
	 * @return Returns the renderer for the cell view.
	 */
	public CellViewRenderer getRenderer() {
		return renderer;
	}
}
