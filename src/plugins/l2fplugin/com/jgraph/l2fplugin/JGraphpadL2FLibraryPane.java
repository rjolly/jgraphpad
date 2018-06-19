/* 
 * $Id: JGraphpadL2FLibraryPane.java,v 1.4 2006/01/11 10:35:31 gaudenz Exp $
 * Copyright (c) 2001-2005, Gaudenz Alder
 * 
 * All rights reserved.
 * 
 * See LICENSE file for license details. If you are unable to locate
 * this file please contact info (at) jgraph (dot) com.
 */
package com.jgraph.l2fplugin;

import java.awt.Component;

import org.w3c.dom.Node;

import com.jgraph.JGraphEditor;
import com.jgraph.pad.factory.JGraphpadLibraryPane;
import com.jgraph.pad.factory.JGraphpadPane;
import com.jgraph.pad.factory.JGraphpadLibraryPane.LibraryTracker;
import com.jgraph.pad.util.JGraphpadMouseAdapter;
import com.l2fprod.common.swing.JOutlookBar;

/**
 * Factory method to replace the tabbed pane with a {@link JOutlookBar} in the
 * default factory method of {@link com.jgraph.pad.factory.JGraphpadLibraryPane}.
 */
public class JGraphpadL2FLibraryPane extends JGraphpadLibraryPane.FactoryMethod {

	/**
	 * Constructs a new factory method for the specified enclosing editor using
	 * JGraphpadLibraryPane.FactoryMethod.NAME.
	 * 
	 * @param editor
	 *            The editor that contains the factory method.
	 */
	public JGraphpadL2FLibraryPane(JGraphEditor editor) {
		super(editor);
	}

	/*
	 * (non-Javadoc)
	 */
	public Component createInstance(Node configuration) {
		JOutlookBar tabPane = new JOutlookBar();
		tabPane.addMouseListener(new JGraphpadMouseAdapter(editor,
				JGraphpadPane.NODENAME_DESKTOPPOPUPMENU));
		LibraryTracker tracker = new LibraryTracker(tabPane, editor);
		editor.getModel().addTreeModelListener(tracker);
		return tabPane;
	}

}
