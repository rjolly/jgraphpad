/* 
 * $Id: JGraphEditorResources.java,v 1.2 2005/08/06 10:21:12 gaudenz Exp $
 * Copyright (c) 2001-2005, Gaudenz Alder
 * 
 * All rights reserved.
 * 
 * See LICENSE file for license details. If you are unable to locate
 * this file please contact info (at) jgraph (dot) com.
 */
package com.jgraph.editor;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.MissingResourceException;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;

import com.jgraph.JGraphEditor;

/**
 * Manages a set of resource bundles to retrieve keys and reads images and
 * streams from the classpath. In order to find a key, the class searches all
 * bundles in inverse insertion order (last inserted first).
 */
public class JGraphEditorResources {

	/**
	 * Ordered list of the inserted resource bundles.
	 */
	protected static LinkedList bundles = new LinkedList();

	/**
	 * Adds a resource bundle.
	 * 
	 * @param basename
	 *            The basename of the resource bundle to add.
	 */
	public static void addBundle(String basename) {
		bundles.addFirst(PropertyResourceBundle.getBundle(basename));
	}

	/**
	 * Adds an array of resource bundles using {@link #addBundle(String)}.
	 * 
	 * @param basenames
	 *            The array of basenames to add.
	 */
	public static void addBundles(String[] basenames) {
		if (basenames != null)
			for (int i = 0; i < basenames.length; i++)
				addBundle(basenames[i]);
	}

	/**
	 * Returns the value for <code>key</code> by searching the resource
	 * bundles in inverse order or <code>null</code> if no value can be found
	 * for <code>key</code>.
	 * 
	 * @param key
	 *            The key to be searched for.
	 * @return Returns the value for <code>key</code> or <code>null</code>.
	 * 
	 * @see ResourceBundle#getString(java.lang.String)
	 */
	public static String getString(String key) {
		Iterator it = bundles.iterator();
		while (it.hasNext()) {
			try {
				return ((PropertyResourceBundle) it.next()).getString(key);
			} catch (MissingResourceException mrex) {
				// continue
			}
		}
		return null;
	}

	/**
	 * Returns the value for <code>key</code> replacing every occurrence of
	 * <code>{0}</code> with <code>param</code>. This is a shortcut method
	 * for values with only one placeholder.
	 * 
	 * @return Returns the parametrized value for <code>key</code>.
	 * 
	 * @see #getString(String, Object[])
	 */
	public static String getString(String key, Object param) {
		return getString(key, new Object[] { param });
	}

	/**
	 * Returns the value for <code>key</code> replacing every occurrence of
	 * <code>{i}</code> with <code>params[i]</code> where i is an integer (i =
	 * 0, 1, ..., n).
	 * 
	 * @return Returns the parametrized value for <code>key</code>.
	 */
	public static String getString(String key, Object[] params) {
		String base = getString(key);
		if (base != null && params != null) {

			// Allocates space for the result string
			int len = base.length();
			for (int i = 0; i < params.length; i++)
				len += String.valueOf(params[i]).length();
			StringBuffer ret = new StringBuffer(len);

			// Parses the resource string and replaces placeholders
			StringBuffer indexString = null;
			for (int i = 0; i < base.length(); i++) {
				char c = base.charAt(i);

				// Starts reading the value index
				if (c == '{') {

					// Assumes an average index length of 1
					indexString = new StringBuffer(1);

				} else if (indexString != null && c == '}') {

					// Finishes reading and appends the value
					// Issues a warning if the index is wrong
					// and warnings are switched on.
					int index = Integer.parseInt(indexString.toString());
					if (index >= 0 && index < params.length)
						ret.append(params[index]);
					indexString = null;

				} else if (indexString != null) {

					// Reads the value index
					indexString.append(c);

				} else {
					ret.append(c);
				}
			}
			return ret.toString();
		}
		return base;
	}

	/**
	 * Returns the specified file as an image or <code>null</code> if there
	 * was an exception. Exceptions are silently ignored by this method. This
	 * implementation first tries to load the specified filename from the
	 * classpath. If the file cannot be found, it tries loading it as external
	 * file or URL.
	 * 
	 * @param uri
	 *            The URI to load the image from.
	 * @return Returns the image for <code>filename</code>.
	 * 
	 * @see ImageIO#read(java.net.URL)
	 * @see Class#getResource(java.lang.String)
	 */
	public static ImageIcon getImage(String uri) {
		try {
			return new ImageIcon(ImageIO.read(JGraphEditorResources.class
					.getResource(uri)));
		} catch (Exception e) {
			try {
				return new ImageIcon(ImageIO.read(new BufferedInputStream(
						JGraphEditor.isURL(uri) ? new URL(uri).openStream()
								: new FileInputStream(uri))));
			} catch (Exception e1) {
				// ignore
			}
		}
		return null;
	}

	/**
	 * Returns the specified file as a buffered input stream or
	 * <code>null</code> if there was an exception. Exceptions are silently
	 * ignored by this method. This implementation first tries to load the
	 * specified filename from the classpath. If the file cannot be found, it
	 * tries loading it as external file or URL.
	 * 
	 * @param uri
	 *            The URI to return the input stream for.
	 * @throws IOException
	 *             If the URI can not be read.
	 * @throws FileNotFoundException
	 *             If the URI can not be found.
	 * @throws MalformedURLException
	 *             If the URI is an invalid URL.
	 * @return Returns the input stream for <code>filename</code>.
	 * 
	 * @see Class#getResource(java.lang.String)
	 * @see URL#openStream()
	 * @see BufferedInputStream
	 */
	public static InputStream getInputStream(String uri)
			throws MalformedURLException, FileNotFoundException, IOException {
		URL url = JGraphEditorResources.class.getResource(uri);
		try {
			return new BufferedInputStream(url.openStream());
		} catch (Exception e) {
			return new BufferedInputStream(JGraphEditor.isURL(uri) ? new URL(
					uri).openStream() : new FileInputStream(uri));
		}
	}

	/**
	 * Returns a buffered output stream for the specified URI or null if there
	 * was an exception.
	 * 
	 * @param uri
	 *            The URI to return the output stream for.
	 * @return Returns an output stream for the specified URI.
	 * 
	 * @throws FileNotFoundException
	 *             If the specified URI can not be found.
	 */
	public static OutputStream getOutputStream(String uri)
			throws FileNotFoundException {
		OutputStream out = null;
		if (JGraphEditor.isURL(uri))
			out = new ByteArrayOutputStream();
		else
			out = new BufferedOutputStream(new FileOutputStream(uri));
		return out;
	}

	/**
	 * Returns the bundles.
	 * 
	 * @return Returns the bundles.
	 */
	public static LinkedList getBundles() {
		return bundles;
	}

	/**
	 * Sets the bundles.
	 * 
	 * @param bundles
	 *            The bundles to set.
	 */
	public static void setBundles(LinkedList bundles) {
		JGraphEditorResources.bundles = bundles;
	}

}