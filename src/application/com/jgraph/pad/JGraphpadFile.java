/* 
 * $Id: JGraphpadFile.java,v 1.2 2005/08/06 22:24:16 gaudenz Exp $
 * Copyright (c) 2001-2005, Gaudenz Alder
 * 
 * All rights reserved.
 * 
 * See LICENSE file for license details. If you are unable to locate
 * this file please contact info (at) jgraph (dot) com.
 */
package com.jgraph.pad;

import javax.swing.tree.DefaultMutableTreeNode;

import com.jgraph.editor.JGraphEditorFile;

/**
 * Represents a file in the JGraphpad editor. A file may contain any number of
 * children which are themselfes XML encodable.
 */
public class JGraphpadFile extends DefaultMutableTreeNode implements
		JGraphEditorFile {

	/**
	 * Specifies if the file has been modified since the last save.
	 */
	protected transient boolean modified;

	/**
	 * Specifies if the file has been saved since its creation.
	 */
	protected transient boolean isNew = false;

	/**
	 * Constructs a new file.
	 */
	public JGraphpadFile() {
		this(null);
	}

	/**
	 * Constructs a new file using the filename as the user object.
	 * 
	 * @param filename
	 *            The user object of the parent object.
	 */
	public JGraphpadFile(String filename) {
		super(filename);
		isNew = (filename != null);
	}
	
	/*
	 * (non-Javadoc)
	 */
	public void setNew(boolean isNew) {
		this.isNew = isNew;
	}

	/*
	 * (non-Javadoc)
	 */
	public boolean isNew() {
		return isNew;
	}

	/*
	 * (non-Javadoc)
	 */
	public void setModified(boolean modified) {
		this.modified = modified;
	}

	/*
	 * (non-Javadoc)
	 */
	public boolean isModified() {
		return modified;
	}

	/*
	 * (non-Javadoc)
	 */
	public void setFilename(String filename) {
		setUserObject(filename);
	}

	/*
	 * (non-Javadoc)
	 */
	public String getFilename() {
		Object obj = getUserObject();
		if (obj != null)
			return obj.toString();
		return null;
	}

}