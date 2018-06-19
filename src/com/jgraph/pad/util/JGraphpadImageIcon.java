/*
 * $Id: JGraphpadImageIcon.java,v 1.2 2005/08/17 21:20:29 gaudenz Exp $
 * Copyright (c) 2001-2005, Gaudenz Alder
 * 
 * All rights reserved. 
 * 
 * This file is licensed under the JGraph software license, a copy of which
 * will have been provided to you in the file LICENSE at the root of your
 * installation directory. If you are unable to locate this file please
 * contact JGraph sales for another copy.
 */
package com.jgraph.pad.util;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

import javax.swing.ImageIcon;

import com.jgraph.editor.JGraphEditorResources;

/**
 * This class transform a <CODE>ImageIcon</CODE> into a bean, allowing for
 * encoding and decoding in XML using <CODE>XMLEncoder</CODE> and <CODE>
 * XMLDecoder</CODE>.
 */
public class JGraphpadImageIcon extends ImageIcon {

	private String filename = null;

	/**
	 * Creates an uninitialized image icon.
	 */
	public JGraphpadImageIcon() {
		super();
	}

	/**
	 * Creates an image icon from the specified file. The image will be
	 * preloaded by using <CODE>MediaTracker</CODE> to monitor the loading
	 * answer of the image. The specified <CODE>String</CODE> can be a file
	 * name or a file path. When specifying a path, use the Internet-standard
	 * forward-slash ("/") as a separator. (The string is converted to an <CODE>
	 * URL</CODE>, so the forward-slash works on all systems.) For example,
	 * specify:
	 * 
	 * <PRE>
	 * 
	 * new JGraphpadImageIcon("images/myImage.gif")
	 * 
	 * </PRE>
	 * 
	 * The <CODE>description</CODE> is initialized to the filename string.
	 * 
	 * @param filename
	 *            A <CODE>String</CODE> specifying a filename or path.
	 */
	public JGraphpadImageIcon(String filename) {
		setFileName(filename);
	}

	/**
	 * Creates an image icon from the specified file. The image will be
	 * preloaded by using <CODE>MediaTracker</CODE> to monitor the loading
	 * answer of the image.
	 * 
	 * @param filename
	 *            The name of the file containing the image.
	 * @param description
	 *            A brief textual description of the image.
	 */
	public JGraphpadImageIcon(String filename, String description) {
		setDescription(description);
		setFileName(filename);
	}

	/**
	 * Creates an image icon from the specified <CODE>URL</CODE>. The image
	 * will be preloaded by using <CODE>MediaTracker</CODE> to monitor the
	 * loaded answer of the image. The icon's <CODE>description</CODE> is
	 * initialized to be a string representation of the URL.
	 * 
	 * @param location
	 *            The <CODE>URL</CODE> for the image.
	 */
	public JGraphpadImageIcon(URL location) {
		setFileName(location.toExternalForm());
	}

	/**
	 * Creates an image icon from the specified <CODE>URL</CODE>. The image
	 * will be preloaded by using <CODE>MediaTracker</CODE> to monitor the
	 * loaded answer of the image.
	 * 
	 * @param location
	 *            The URL for the image.
	 * @param description
	 *            A brief textual description of the image.
	 */
	public JGraphpadImageIcon(URL location, String description) {
		setDescription(description);
		setFileName(location.toExternalForm());
	}

	/**
	 * Returns the file name used to initialize the image.
	 */
	public String getFileName() {
		return filename;
	}

	/**
	 * Initializes this image icon from the specified file. The image will be
	 * preloaded by using <CODE>MediaTracker</CODE> to monitor the loading
	 * answer of the image. The specified <CODE>String</CODE> can be a file
	 * name or a file path. When specifying a path, use the Internet-standard
	 * forward-slash ("/") as a separator. (The string is converted to an <CODE>
	 * URL</CODE>, so the forward-slash works on all systems.) For example,
	 * specify:
	 * 
	 * <PRE>
	 * 
	 * new BeanifiedIcon().setFileName("images/myImage.gif")
	 * 
	 * </PRE>
	 * 
	 * @param filename
	 *            A <CODE>String</CODE> specifying a filename or path.
	 */
	public void setFileName(String filename) {

		// Try classpath first
		ImageIcon icon = JGraphEditorResources.getImage(filename);
		if (icon != null) {
			this.filename = filename;
			setImage(icon.getImage());
		} else {

			URL loadableName;

			try {
				try {
					loadableName = new URL(filename);
				} catch (MalformedURLException ex) {
					loadableName = new File(filename).toURL();
				}
				this.filename = loadableName.toExternalForm();
			} catch (Exception ex) {
				return;
			}

			if (getDescription() == null)
				setDescription(filename);

			icon = JGraphEditorResources.getImage(this.filename);
			if (icon != null)
				setImage(icon.getImage());

		}

	}

}
