/* 
 * $Id: JGraphEditorFile.java,v 1.1.1.1 2005/08/04 11:21:58 gaudenz Exp $
 * Copyright (c) 2001-2005, Gaudenz Alder
 * 
 * All rights reserved.
 * 
 * See LICENSE file for license details. If you are unable to locate
 * this file please contact info (at) jgraph (dot) com.
 */
package com.jgraph.editor;

import javax.swing.tree.MutableTreeNode;

/**
 * Defines the basic requirements for a file in a JGraph editor document model.
 * A file contains an arbitrary set of children. The properties of document
 * model elements shoudl be changed via the document model in order to update
 * all attached listeners.
 */
public interface JGraphEditorFile extends MutableTreeNode {

	/**
	 * Sets the modified state.
	 * 
	 * @param modified
	 *            The modified state to set.
	 */
	public void setModified(boolean modified);

	/**
	 * Returns <code>true</code> if the file was modified since the last save.
	 * 
	 * @return Returns the modified state.
	 */
	public boolean isModified();

	/**
	 * Sets the isNew state.
	 * 
	 * @param isNew
	 *            The isNew state to set.
	 */
	public void setNew(boolean isNew);

	/**
	 * Returns <code>true</code> if the file has never been saved since its
	 * creation.
	 * 
	 * @return Returns the isNew state.
	 */
	public boolean isNew();

	/**
	 * Sets the filename.
	 * 
	 * @param filename
	 *            The filename to set.
	 */
	public void setFilename(String filename);

	/**
	 * Returns the filename.
	 * 
	 * @return Returns the filename.
	 */
	public String getFilename();

}
