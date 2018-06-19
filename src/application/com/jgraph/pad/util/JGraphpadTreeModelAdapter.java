/* 
 * $Id: JGraphpadTreeModelAdapter.java,v 1.1.1.1 2005/08/04 11:21:58 gaudenz Exp $
 * Copyright (c) 2001-2005, Gaudenz Alder
 * 
 * All rights reserved.
 * 
 * See LICENSE file for license details. If you are unable to locate
 * this file please contact info (at) jgraph (dot) com.
 */
package com.jgraph.pad.util;

import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;

/**
 * Helper class for receiving events from a tree model. Provides empty
 * implementations for all methods of the {@link TreeModelListener} interface.
 */
public abstract class JGraphpadTreeModelAdapter implements TreeModelListener {

	/*
	 * (non-Javadoc)
	 */
	public void treeNodesChanged(TreeModelEvent arg0) {
		// empty
	}

	/*
	 * (non-Javadoc)
	 */
	public void treeNodesInserted(TreeModelEvent arg0) {
		// empty
	}

	/*
	 * (non-Javadoc)
	 */
	public void treeNodesRemoved(TreeModelEvent arg0) {
		// empty
	}

	/*
	 * (non-Javadoc)
	 */
	public void treeStructureChanged(TreeModelEvent arg0) {
		// empty
	}

}