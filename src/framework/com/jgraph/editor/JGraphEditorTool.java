/* 
 * $Id: JGraphEditorTool.java,v 1.2 2005/10/15 10:28:23 gaudenz Exp $
 * Copyright (c) 2001-2005, Gaudenz Alder
 * 
 * All rights reserved.
 * 
 * See LICENSE file for license details. If you are unable to locate
 * this file please contact info (at) jgraph (dot) com.
 */
package com.jgraph.editor;

import java.awt.event.MouseEvent;

import org.jgraph.graph.BasicMarqueeHandler;

/**
 * The base class for all tools in a JGraph editor kit. Tools are used to
 * temporary set a graphs marquee handler, ie. to take over all interactions on
 * a graph. This is typically used to insert new cells in marquee-style (as
 * opposed to dnd-style used in the library).
 */
public class JGraphEditorTool extends BasicMarqueeHandler {

	/**
	 * Holds the name.
	 */
	protected String name;

	/**
	 * Specifies whether this tool is always activated. Default is false. A
	 * value of true means the tool will always be called by the toolbox
	 * redirector if the tool is selected.
	 */
	protected boolean isAlwaysActive = true;

	/**
	 * Constructs a tool with the specified name.
	 * 
	 * @param name
	 *            The name of the tool to be created.
	 */
	public JGraphEditorTool(String name) {
		this(name, true);
	}

	/**
	 * Constructs a tool with the specified name.
	 * 
	 * @param name
	 *            The name of the tool to be created.
	 */
	public JGraphEditorTool(String name, boolean isAlwaysActive) {
		setName(name);
		this.isAlwaysActive = isAlwaysActive;
	}

	/**
	 * Returns the name of the tool.
	 * 
	 * @return Returns the name.
	 */
	public String getName() {
		return name;
	}

	/**
	 * Sets the name of the tool.
	 * 
	 * @param name
	 *            The name to set.
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Returns true if this tool is always active, eg if it should return true
	 * whenever {@link #isForceMarqueeEvent(MouseEvent)} is called.
	 * 
	 * @return Returns the isAlwaysActive.
	 */
	public boolean isAlwaysActive() {
		return isAlwaysActive;
	}

	/**
	 * Sets whether the tool is always active.
	 * 
	 * @param isAlwaysActive
	 *            The isAlwaysActive to set.
	 */
	public void setAlwaysActive(boolean isAlwaysActive) {
		this.isAlwaysActive = isAlwaysActive;
	}

}