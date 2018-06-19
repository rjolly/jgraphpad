/* 
 * $Id: JGraphEditorFactoryMethod.java,v 1.1.1.1 2005/08/04 11:21:58 gaudenz Exp $
 * Copyright (c) 2001-2005, Gaudenz Alder
 * 
 * All rights reserved.
 * 
 * See LICENSE file for license details. If you are unable to locate
 * this file please contact info (at) jgraph (dot) com.
 */
package com.jgraph.editor.factory;

import java.awt.Component;

import org.w3c.dom.Node;

/**
 * Defines the basic implementation of a factory method.
 */
public abstract class JGraphEditorFactoryMethod {

	/**
	 * Holds the name.
	 */
	protected String name = null;

	/**
	 * Constructs a new factory method using the specified name.
	 * 
	 * @param name
	 *            The name of the factory method to construct.
	 */
	public JGraphEditorFactoryMethod(String name) {
		this.name = name;
	}

	/**
	 * Returns a new component for the specified configuration.
	 * 
	 * @param configuration
	 *            The configuration to create the component with.
	 * @return Returns a new component.
	 */
	public abstract Component createInstance(Node configuration);

	/**
	 * Returns the name of the factory method.
	 * 
	 * @return Returns the name.
	 */
	public String getName() {
		return name;
	}

	/**
	 * Sets the name of the factory method.
	 * 
	 * @param name
	 *            The name to set.
	 */
	public void setName(String name) {
		this.name = name;
	}

}
