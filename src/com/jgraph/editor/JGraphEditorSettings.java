/* 
 * $Id: JGraphEditorSettings.java,v 1.1.1.1 2005/08/04 11:21:58 gaudenz Exp $
 * Copyright (c) 2001-2005, Gaudenz Alder
 * 
 * All rights reserved.
 * 
 * See LICENSE file for license details. If you are unable to locate
 * this file please contact info (at) jgraph (dot) com.
 */
package com.jgraph.editor;

import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.Window;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.swing.JSplitPane;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * Manages configuration files for a JGraph editor, namely XML documents and
 * properties files and holds references to configured objects.
 */
public class JGraphEditorSettings {

	/**
	 * Document builder factory for parsing XML files.
	 * 
	 * @see javax.xml.parsers.DocumentBuilderFactory#newInstance()
	 */
	public static DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory
			.newInstance();

	/**
	 * XML attribute name for the key attribute.
	 */
	public static final String ATTRIBUTENAME_KEY = "key";

	/**
	 * XML attribute name for the before attribute.
	 */
	public static final String ATTRIBUTENAME_BEFORE = "before";

	/**
	 * List of hooks that are to be called on shutdown.
	 */
	protected List shutdownHooks = new ArrayList();

	/**
	 * Holds maps of (document, name), (properties, name) and (key, Object)
	 * pairs respectively.
	 */
	protected Map documents = new HashMap(), properties = new HashMap(),
			objects;

	/**
	 * Constructs new settings using an empty object map.
	 */
	public JGraphEditorSettings() {
		this(new HashMap());
	}

	/**
	 * Constructs new settings using the passed-in object map.
	 * 
	 * @param objects
	 *            The map to use as the initial objects map.
	 */
	public JGraphEditorSettings(Map objects) {
		this.objects = objects;
	}

	/**
	 * Returns the document for the specified name or <code>null</code> if no
	 * such document exists.
	 * 
	 * @param name
	 *            The name of the document to be returned.
	 * @return Returns the backing document.
	 */
	public Document getDocument(String name) {
		return (Document) documents.get(name);
	}

	/**
	 * Adds the document under the specified name or merges <code>doc</code>
	 * into an existing document for <code>name</code>.
	 * 
	 * @param name
	 *            The name under which the document should be added.
	 * @param doc
	 *            The document to add.
	 */
	public void add(String name, Document doc) {
		Document document = getDocument(name);
		if (document == null)
			documents.put(name, doc);
		else
			merge(document, document.getDocumentElement(), doc
					.getDocumentElement().getChildNodes(), true);
	}

	/**
	 * Recursively replaces or appends <code>children</code> in
	 * <code>parent</code> for equal keys ({@link #ATTRIBUTENAME_KEY} or node
	 * names, depending on <code>useNames</code>. If the node to be added
	 * provides a before attribute and the node for the specified key exists in
	 * the parent's node list then the new node is inserted before the
	 * referenced node.
	 * 
	 * @param document
	 *            The document to import the nodes into.
	 * @param parent
	 *            The parent node to replace or add ports.
	 * @param children
	 *            The children to add or replace.
	 * @param useNames
	 *            If names or keys should be used to check for node equality.
	 */
	protected void merge(Document document, Node parent, NodeList children,
			boolean useNames) {
		for (int i = 0; i < children.getLength(); i++) {
			Node childNode = children.item(i);

			// Searches for existing node (by key or node name)
			Node existingNode = (useNames) ? getNodeByName(parent
					.getChildNodes(), childNode.getNodeName())
					: getNodeByAttribute(parent.getChildNodes(),
							ATTRIBUTENAME_KEY, getAttributeValue(childNode,
									ATTRIBUTENAME_KEY));
			if (existingNode != null) {

				// Merges recursively
				merge(document, existingNode, childNode.getChildNodes(), false);
			} else {

				if (existingNode != null) {

					// Removes from parent
					existingNode.getParentNode().removeChild(existingNode);
				}

				// Appends new child or inserts before referenced node
				String beforeKey = getAttributeValue(childNode,
						ATTRIBUTENAME_BEFORE);
				Node refNode = getNodeByAttribute(parent.getChildNodes(),
						ATTRIBUTENAME_KEY, beforeKey);
				Node newNode = document.importNode(childNode, true);
				if (refNode != null)
					parent.insertBefore(newNode, refNode);
				else
					parent.appendChild(newNode);
			}
		}
	}

	/**
	 * Returns the first node for <code>name</code> in the document registered
	 * under <code>documentName</code>.
	 * 
	 * @param documentName
	 *            The string that identifies the document to be used.
	 * @param name
	 *            The name of the node to be returned.
	 * @return Returns the first node for <code>name</code> in
	 *         <code>documentName</code>.
	 */
	public Node getNodeByName(String documentName, String name) {
		return getNodeByName(getDocument(documentName).getDocumentElement()
				.getChildNodes(), name);
	}

	/**
	 * Constructs a document object for an XML input stream using the
	 * {@link #documentBuilderFactory}.
	 * 
	 * @param in
	 *            The input stream that represents the XML data.
	 * @return Return the parsed input stream as an XML document.
	 */
	public static Document parse(InputStream in)
			throws ParserConfigurationException, SAXException, IOException {
		DocumentBuilder db;
		synchronized (documentBuilderFactory) {
			db = documentBuilderFactory.newDocumentBuilder();
		}
		return db.parse(in);
	}

	/**
	 * Returns the first node in <code>nodeList</code> whos name equals
	 * <code>name</code> or <code>null</code> if no such node exists.
	 * 
	 * @param nodeList
	 *            The list of nodes to scan for the name.
	 * @param name
	 *            The name of the node to search for.
	 * @return Returns the first node for <code>name</code> in <code>nodeList
	 *         </code>.
	 * 
	 * @see Node#getNodeName()
	 */
	public static Node getNodeByName(NodeList nodeList, String name) {
		for (int i = 0; i < nodeList.getLength(); i++) {
			Node node = nodeList.item(i);
			if (node.getNodeName().equals(name))
				return node;
		}
		return null;
	}

	/**
	 * Returns the node in <code>nodeList</code> whos attribute named
	 * <code>attributeName</code> equals <code>value</code> or
	 * <code>null</code> if no such node exists.
	 * 
	 * @param nodeList
	 *            The nodes to scan for the attribute value.
	 * @param attributeName
	 *            The name of the attribute to scan for.
	 * @param value
	 *            The value of the attribute to scan for.
	 * @return Returns the first node that matches the search criteria.
	 * 
	 * @see #getAttributeValue(Node, String)
	 */
	public static Node getNodeByAttribute(NodeList nodeList,
			String attributeName, String value) {
		if (value != null && attributeName != null) {
			for (int i = 0; i < nodeList.getLength(); i++) {
				Node childNode = nodeList.item(i);
				String attributeValue = getAttributeValue(childNode,
						attributeName);
				if (attributeValue != null && attributeValue.equals(value))
					return childNode;
			}
		}
		return null;
	}

	/**
	 * Returns the textual representation of the attribute in <code>node</code>
	 * whos name is <code>attributeName</code> or <code>null</code> if no
	 * such attribute exists.
	 * 
	 * @param node
	 *            The node whos attribute should be returned.
	 * @param attributeName
	 *            The name of the attribute whos value should be returned.
	 * @return Returns the value of <code>attributeName</code> in
	 *         <code>node</code>.
	 * 
	 * @see Node#getAttributes()
	 * @see NamedNodeMap#getNamedItem(java.lang.String)
	 * @see Node#getNodeValue()
	 */
	public static String getAttributeValue(Node node, String attributeName) {
		if (node != null && node.hasAttributes()) {
			Node attribute = node.getAttributes().getNamedItem(attributeName);
			if (attribute != null)
				return String.valueOf(attribute.getNodeValue());
		}
		return null;
	}

	/**
	 * Returns the textual representation of the attribute in <code>node</code>
	 * whos name is equal to {@link #ATTRIBUTENAME_KEY}or <code>null</code>
	 * if no such node exists.
	 * 
	 * @param node
	 *            The node whos attribute should be returned.
	 * @return Returns the value of {@link #ATTRIBUTENAME_KEY}in
	 *         <code>node</code>.
	 * 
	 * @see #getAttributeValue(Node, String)
	 */
	public static String getKeyAttributeValue(Node node) {
		return getAttributeValue(node, ATTRIBUTENAME_KEY);
	}

	/**
	 * Returns the properties registered under the specified name or
	 * <code>null</code> if no such properties exist.
	 * 
	 * @param name
	 *            The name of the properties to return.
	 * @return Returns the properties for <code>name</code>.
	 */
	public Properties getProperties(String name) {
		return (Properties) properties.get(name);
	}

	/**
	 * Registers the specified properties under the specified name.
	 * 
	 * @param name
	 *            The name to register the properties under.
	 * @param props
	 *            The properties to register.
	 */
	public void add(String name, Properties props) {
		properties.put(name, props);
	}

	/**
	 * Adds the properties under a specified name from an input stream. This
	 * method creates the properties using
	 * {@link Properties#load(java.io.InputStream)}and adds the properties
	 * using {@link #add(String, Properties)}.
	 * 
	 * @param name
	 *            The name to register the properties under.
	 * @param s
	 *            The input stream to read the properties from.
	 * @throws IOException
	 *             If the input stream cannot be read.
	 */
	public void add(String name, InputStream s) throws IOException {
		Properties p = new Properties();
		p.load(s);
		add(name, p);
	}

	/**
	 * Adds the specified object under <code>key</code>.
	 * 
	 * @param key
	 *            The key to register the object under.
	 * @param obj
	 *            The object to register.
	 */
	public void putObject(String key, Object obj) {
		objects.put(key, obj);
	}

	/**
	 * Returns the object for the specified key.
	 * 
	 * @param key
	 *            The key to return the object for.
	 * @return Returns the object for <code>key</code>.
	 */
	public Object getObject(String key) {
		return objects.get(key);
	}

	/**
	 * Restores the window bounds for the window found in objects under
	 * <code>key</code> from the rectangle stored in the properties registered
	 * under <code>name</code> for the <code>key</code> property. This
	 * implementation assumes that the <code>key</code> property is a
	 * rectangle, ie. consists of 4 entries in the file.
	 * 
	 * @param name
	 *            The name of the properties to use.
	 * @param key
	 *            The key of the object and rectangle property.
	 * 
	 * @see #getRectangleProperty(String, String)
	 */
	public void restoreWindow(String name, String key) {
		Object obj = getObject(key);
		if (obj instanceof Window && getProperties(name) != null) {
			Window wnd = (Window) obj;
			try {
				Rectangle rect = getRectangleProperty(name, key);
				if (rect != null) {
					Dimension screen = Toolkit.getDefaultToolkit()
							.getScreenSize();
					if (rect.getX() > screen.getWidth())
						rect.x = 0;
					if (rect.getY() > screen.getHeight())
						rect.y = 0;
					rect.width = Math.min(screen.width, rect.width);
					rect.height = Math.min(screen.height, rect.height);
					wnd.setBounds(rect);
				}
			} catch (Exception e) {
				// ignore
			}
		}
	}

	/**
	 * Restores the divider location for the split pane found in objects under
	 * <code>key</code> from the integer stored in the properties registered
	 * under <code>name</code> for the <code>key</code> property.
	 * 
	 * @param name
	 *            The name of the properties to use.
	 * @param key
	 *            The key of the object and integer property.
	 * 
	 * @see #getRectangleProperty(String, String)
	 */
	public void restoreSplitPane(String name, String key) {
		Properties props = getProperties(name);
		Object obj = getObject(key);
		if (obj instanceof JSplitPane && props != null) {
			JSplitPane split = (JSplitPane) obj;
			try {
				Integer value = new Integer(props.getProperty(key));
				split.setDividerLocation(value.intValue());
			} catch (Exception e) {
				// ignore
			}
		}
	}

	/**
	 * Stores the bounds of the window found in objects under <code>key</code>
	 * in the properties called <code>name</code> as a rectangle property
	 * under <code>key</code>.
	 * 
	 * @param name
	 *            The name of the properties to store the bounds.
	 * @param key
	 *            The name of the key to store the rectangle property.
	 */
	public void storeWindow(String name, String key) {
		Window wnd = (Window) getObject(key);
		putRectangleProperty(name, key, wnd.getBounds());
	}

	/**
	 * Stores the dividerlocation of the splitpane found in objects under
	 * <code>key</code> in the properties called <code>name</code> as a int
	 * property under <code>key</code>.
	 * 
	 * @param name
	 *            The name of the properties to store the divider location.
	 * @param key
	 *            The name of the key to store the divider location.
	 */
	public void storeSplitPane(String name, String key) {
		Properties props = getProperties(name);
		JSplitPane split = (JSplitPane) getObject(key);
		if (props != null && split != null)
			props.put(key, String.valueOf(split.getDividerLocation()));
	}

	/**
	 * Pushes a new entry into the list for <code>key</code> in the properties
	 * called <code>name</code> with <code>value</code>, making sure the
	 * list has no more than <code>maxCount</code> entries. If the list is
	 * longer than <code>maxCount</code>, then the oldest entry will be
	 * removed.
	 * 
	 * @param name
	 *            The name of the properties to store the list.
	 * @param key
	 *            The name of the key to store the list entries.
	 * @param value
	 *            The value of the new list entry.
	 * @param maxCount
	 *            The maximum number of elemnts in the list.
	 */
	public void pushListEntryProperty(String name, String key, String value,
			int maxCount) {
		Properties props = getProperties(name);
		if (props != null) {
			List values = new ArrayList(maxCount);
			int i = 0;
			String tmp = props.getProperty(key + i++);
			while (tmp != null) {
				values.add(tmp);
				tmp = props.getProperty(key + i++);
			}
			if (value != null && !values.contains(value)) {
				values.add(value);
				if (values.size() > maxCount)
					values.remove(0);
				i = 0;
				Iterator it = values.iterator();
				while (it.hasNext())
					props.setProperty(key + i++, String.valueOf(it.next()));
			}
		}
	}

	/**
	 * Puts the rectangle as 4 entries for the <code>key</code> -property plus
	 * a suffix of .x, .y, .width and .height into the properties called
	 * <code>name</code>.
	 * 
	 * @param name
	 *            The name of the properties to store the rectangle.
	 * @param key
	 *            The name of the key to store the rectangle size and location.
	 * @param rect
	 *            The rectangle to put into the properties.
	 */
	public void putRectangleProperty(String name, String key, Rectangle rect) {
		Properties props = getProperties(name);
		if (props != null && rect != null) {
			props.put(key + ".x", String.valueOf(rect.x));
			props.put(key + ".y", String.valueOf(rect.y));
			props.put(key + ".width", String.valueOf(rect.width));
			props.put(key + ".height", String.valueOf(rect.height));
		}
	}

	/**
	 * Gets the rectangle made up by 4 entries for the <code>key</code>
	 * -property from the properties called <code>name</code>.
	 * 
	 * @param name
	 *            The name of the properties to get the rectangle from.
	 * @param key
	 *            The key to retrieve the rectangle values.
	 * @return Returns the rectangle for <code>key</code> in <code>name</code>.
	 * 
	 * @see #putRectangleProperty(String, String, Rectangle)
	 */
	public Rectangle getRectangleProperty(String name, String key) {
		Properties props = getProperties(name);
		if (props != null) {
			int x = Integer.parseInt(props.getProperty(key + ".x"));
			int y = Integer.parseInt(props.getProperty(key + ".y"));
			int w = Integer.parseInt(props.getProperty(key + ".width"));
			int h = Integer.parseInt(props.getProperty(key + ".height"));
			return new Rectangle(x, y, w, h);
		}
		return null;
	}

	/**
	 * Invokes all hooks that have previously been added using addShutdownHook
	 * in reverse order (last added first).
	 */
	public void shutdown() {
		for (int i = shutdownHooks.size() - 1; i >= 0; i--)
			((ShutdownHook) shutdownHooks.get(i)).shutdown();
	}

	/**
	 * Adds a shutdown hook which is called from {@link #shutdown()} normally
	 * when the program terminates.
	 * 
	 * @param hook
	 *            The shutdown hook to be added.
	 * 
	 */
	public void addShutdownHook(ShutdownHook hook) {
		shutdownHooks.add(hook);
	}

	/**
	 * Defines the requiements for a class that may act as a shutdown hook, ie.
	 * it is invoked shortly when the program terminates.
	 */
	public interface ShutdownHook {

		public void shutdown();

	}
}