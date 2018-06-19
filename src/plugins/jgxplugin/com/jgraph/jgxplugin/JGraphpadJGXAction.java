/* 
 * $Id: JGraphpadJGXAction.java,v 1.4 2006/02/03 14:36:39 david Exp $
 * Copyright (c) 2001-2005, Gaudenz Alder
 * 
 * All rights reserved.
 * 
 * See LICENSE file for license details. If you are unable to locate
 * this file please contact info (at) jgraph (dot) com.
 */
package com.jgraph.jgxplugin;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.net.URLDecoder;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.StringTokenizer;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.border.Border;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.jgraph.JGraph;
import org.jgraph.graph.AttributeMap;
import org.jgraph.graph.ConnectionSet;
import org.jgraph.graph.DefaultGraphCell;
import org.jgraph.graph.DefaultGraphModel;
import org.jgraph.graph.DefaultPort;
import org.jgraph.graph.Edge;
import org.jgraph.graph.GraphConstants;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import com.jgraph.JGraphEditor;
import com.jgraph.JGraphpad;
import com.jgraph.editor.JGraphEditorAction;
import com.jgraph.pad.action.JGraphpadFileAction;
import com.jgraph.pad.graph.JGraphpadBusinessObject;
import com.jgraph.pad.graph.JGraphpadGraphConstants;
import com.jgraph.pad.graph.JGraphpadRichTextValue;
import com.jgraph.pad.graph.JGraphpadVertexRenderer;
import com.jgraph.pad.util.JGraphpadImageIcon;
import com.jgraph.pad.util.JGraphpadShadowBorder;

/**
 * JGX import for reading older JGraphpad files.
 */
public class JGraphpadJGXAction extends JGraphpadFileAction {

	public static final String TYPE_RECT = "rect", TYPE_TEXT = "text",
			TYPE_ELLIPSE = "ellipse", TYPE_DIAMOND = "diamond", TYPE_TRIANGLE = "triangle",
			TYPE_ROUNDRECT = "roundRect", TYPE_SWIMLANE = "swimlane",
			TYPE_IMAGE = "image", TYPE_PORT = "port", TYPE_EDGE = "edge";

	public static final String NAME_IMPORTJGX = "importJGX";

	public static final String EMPTY = new String("Empty");

	public static final String PARENT = new String("Parent");

	protected static Map cells = new Hashtable();

	protected static Map attrs = new Hashtable();

	protected static Map objs = new Hashtable();

	protected static List delayedAttributes;

	protected static List connectionSetIDs;

	protected Map cellMap = new Hashtable();

	protected AttributeCollection attrCol = new AttributeCollection();

	protected Map userObjectMap = new Hashtable();

	/**
	 * Constructs a new Batik action for the specified editor.
	 */
	protected JGraphpadJGXAction(JGraphEditor editor) {
		super(NAME_IMPORTJGX, editor);
	}

	/**
	 * Executes the action based on the action name.
	 * 
	 * @param event
	 *            The object that describes the event.
	 */
	public void actionPerformed(ActionEvent event) {
		Component component = getPermanentFocusOwner();
		try {
			if (component instanceof JGraph) {
				JGraph graph = (JGraph) component;
				if (getName().equals(NAME_IMPORTJGX))
					doImportJGX(graph, false);
			}
		} catch (Exception e) {
			dlgs.errorDialog(getPermanentFocusOwner(), e.getLocalizedMessage());
		}
	}

	public void doImportJGX(JGraph graph, boolean urlDialog) throws Exception {
		String filename = (urlDialog) ? dlgs.valueDialog(getString("EnterURL"),
				"") : dlgs.fileDialog(getPermanentFocusOwnerOrParent(),
				getString("OpenJGXFile"), true, ".jgx",
				getString("JGXFileDescription"), lastDirectory);
		if (filename != null) {
			InputStream in = editor.getModel().getInputStream(filename);
			read(in, graph);
			System.out.println("file " + filename + " read");
			in.close();
			lastDirectory = new File(filename).getParentFile();
		}
	}

	public DefaultGraphCell createCell(JGraph graph, Object type,
			Object userObject) {
		if (type.equals(TYPE_EDGE)) {
			Object prototype = getValue(KEY_EDGEPROTOTYPE);
			if (prototype instanceof DefaultGraphCell) {
				DefaultGraphCell edge = (DefaultGraphCell) DefaultGraphModel
						.cloneCell(graph.getModel(), prototype);
				graph.getModel().valueForCellChanged(edge, userObject);
				return edge;
			}
		} else if (type.equals(TYPE_PORT)) {
			return new DefaultPort(); // no port prototype
		} else {
			Object prototype = getValue(KEY_VERTEXPROTOTYPE);
			if (prototype instanceof DefaultGraphCell) {
				DefaultGraphCell vertex = (DefaultGraphCell) DefaultGraphModel
						.cloneCell(graph.getModel(), prototype);
				graph.getModel().valueForCellChanged(vertex, userObject);
				Map attrs = graph.getModel().getAttributes(vertex);
				if (type.equals(TYPE_ROUNDRECT))
					JGraphpadGraphConstants.setVertexShape(attrs,
							JGraphpadVertexRenderer.SHAPE_ROUNDED);
				else if (type.equals(TYPE_ELLIPSE))
					JGraphpadGraphConstants.setVertexShape(attrs,
							JGraphpadVertexRenderer.SHAPE_CIRCLE);
				else if (type.equals(TYPE_DIAMOND))
					JGraphpadGraphConstants.setVertexShape(attrs,
							JGraphpadVertexRenderer.SHAPE_DIAMOND);
				else if (type.equals(TYPE_TRIANGLE))
					JGraphpadGraphConstants.setVertexShape(attrs,
							JGraphpadVertexRenderer.SHAPE_TRIANGLE);
				else if (type.equals(TYPE_TEXT) && userObject instanceof Map) {
					Map user = (Map) userObject;
					Object text = user.get("value");
					if (text != null)
						graph.getModel().valueForCellChanged(vertex,
								new JGraphpadRichTextValue(text.toString()));
				}
				return vertex;
			}
		}
		return null;
	}

	/**
	 * returns <tt>pad_xml</tt>
	 */
	public String getFileExtension() {
		return "jgx";
	}

	/**
	 * Returns null
	 */
	public JComponent getReadAccessory() {
		return null;
	}

	/**
	 * Returns null
	 */
	public Hashtable getReadProperties(JComponent accessory) {
		return null;
	}

	//
	// Read
	//

	public void read(InputStream in, JGraph graph) throws Exception {
		// Create a DocumentBuilderFactory
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		// Create a DocumentBuilder
		DocumentBuilder db = dbf.newDocumentBuilder();
		// Parse the input file to get a Document object
		Document doc = db.parse(in);
		// Get the first child (the jgx-element)
		Node modelNode = null;
		Node objsNode = null;
		Node attrsNode = null;
		Node viewNode = null;

		delayedAttributes = new LinkedList();
		connectionSetIDs = new LinkedList();

		for (int i = 0; i < doc.getDocumentElement().getChildNodes()
				.getLength(); i++) {
			Node node = doc.getDocumentElement().getChildNodes().item(i);
			if (node.getNodeName().toLowerCase().equals("model")) {
				modelNode = node;
			} else if (node.getNodeName().toLowerCase().equals("user")) {
				objsNode = node;
			} else if (node.getNodeName().toLowerCase().equals("attrs")) {
				attrsNode = node;
			} else if (node.getNodeName().toLowerCase().equals("view")) {
				viewNode = node;
			}
		}
		objs = decodeUserObjects(objsNode);
		attrs = parseAttrs(attrsNode);
		attrs = augmentAttrs(attrs);
		Map settings = decodeMap(viewNode, false, false);
		ConnectionSet cs = new ConnectionSet();
		Hashtable cells = new Hashtable();
		DefaultGraphCell[] insert = parseChildren(graph, modelNode, cells, cs);

		// Create ConnectionSet
		Iterator it = connectionSetIDs.iterator();
		while (it.hasNext()) {
			ConnectionID cid = (ConnectionID) it.next();
			Object cell = cid.getCell();
			String tid = cid.getTargetID();
			if (tid != null) {
				Object port = cells.get(tid);
				if (port != null) {
					cs.connect(cell, port, cid.isSource());
				}
			}
		}

		// Create AttributeMap
		Map nested = new Hashtable();
		it = delayedAttributes.iterator();
		while (it.hasNext()) {
			DelayedAttributeID att = (DelayedAttributeID) it.next();
			Map attr = (Map) attrs.get(att.getMapID());
			if (attr == null)
				attr = new Hashtable();
			if (attr != null) {
				AttributeMap attr_temp = new AttributeMap(attr);
				attr = (Map) attr_temp.clone();
				if (att.getBounds() != null)
					GraphConstants.setBounds(attr, att.getBounds());
				if (att.getPoints() != null)
					GraphConstants.setPoints(attr, att.getPoints());
				nested.put(att.getCell(), attr);
			}
		}

		// Apply settings to graph
		applySettings(settings, graph);

		// Insert the cells (View stores attributes)
		graph.getGraphLayoutCache().insert(insert, nested, cs, null, null);
	}

	public void applySettings(Map s, JGraph graph) {
		Object tmp;

		tmp = s.get("editable");
		if (tmp != null)
			graph.setEditable(new Boolean(tmp.toString()).booleanValue());

		tmp = s.get("bendable");
		if (tmp != null)
			graph.setBendable(new Boolean(tmp.toString()).booleanValue());

		tmp = s.get("cloneable");
		if (tmp != null)
			graph.setCloneable(new Boolean(tmp.toString()).booleanValue());

		tmp = s.get("connectable");
		if (tmp != null)
			graph.setConnectable(new Boolean(tmp.toString()).booleanValue());

		tmp = s.get("disconnectable");
		if (tmp != null)
			graph.setDisconnectable(new Boolean(tmp.toString()).booleanValue());

		tmp = s.get("disconnectOnMove");
		if (tmp != null)
			graph.setDisconnectOnMove(new Boolean(tmp.toString())
					.booleanValue());

		tmp = s.get("doubleBuffered");
		if (tmp != null)
			graph.setDoubleBuffered(new Boolean(tmp.toString()).booleanValue());

		tmp = s.get("dragEnabled");
		if (tmp != null)
			graph.setDragEnabled(new Boolean(tmp.toString()).booleanValue());

		tmp = s.get("dropEnabled");
		if (tmp != null)
			graph.setDropEnabled(new Boolean(tmp.toString()).booleanValue());

		tmp = s.get("moveable");
		if (tmp != null)
			graph.setMoveable(new Boolean(tmp.toString()).booleanValue());

		tmp = s.get("sizeable");
		if (tmp != null)
			graph.setSizeable(new Boolean(tmp.toString()).booleanValue());

		tmp = s.get("gridVisible");
		if (tmp != null)
			graph.setGridVisible(new Boolean(tmp.toString()).booleanValue());

		tmp = s.get("gridEnabled");
		if (tmp != null)
			graph.setGridEnabled(new Boolean(tmp.toString()).booleanValue());

		tmp = s.get("gridSize");
		if (tmp != null)
			graph.setGridSize(Double.parseDouble(tmp.toString()));

		tmp = s.get("gridMode");
		if (tmp != null)
			graph.setGridMode(Integer.parseInt(tmp.toString()));

		tmp = s.get("scale");
		if (tmp != null)
			graph.setScale(Double.parseDouble(tmp.toString()));

		tmp = s.get("antiAlias");
		if (tmp != null)
			graph.setAntiAliased(new Boolean(tmp.toString()).booleanValue());

	}

	public Map augmentAttrs(Map attrs) {
		Map newAttrs = new Hashtable();
		Iterator it = attrs.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry entry = (Map.Entry) it.next();
			Object key = entry.getKey();
			Map map = (Map) entry.getValue();
			Stack s = new Stack();
			s.add(map);
			Object parentID = map.get(PARENT);
			Object hook = null;
			while (parentID != null) {
				hook = attrs.get(parentID);
				s.add(hook);
				parentID = ((Map) hook).get(PARENT);
			}
			Map newMap = new Hashtable();
			while (!s.isEmpty()) {
				newMap.putAll((Map) s.pop());
			}
			newMap.remove(PARENT);
			// Remove Empty values
			Iterator it2 = newMap.entrySet().iterator();
			while (it2.hasNext()) {
				entry = (Map.Entry) it2.next();
				if (entry.getValue() == EMPTY)
					it2.remove();
			}
			newAttrs.put(key, newMap);
		}
		return newAttrs;
	}

	public DefaultGraphCell parseCell(JGraph graph, Node node, Hashtable cells,
			ConnectionSet cs) {
		DefaultGraphCell cell = null;
		if (node.getNodeName().toLowerCase().equals("a")) {
			Node key = node.getAttributes().getNamedItem("id");
			Node type = node.getAttributes().getNamedItem("class");
			if (key != null && type != null) {
				Node value = node.getAttributes().getNamedItem("val");
				Object userObject = "";
				if (value != null)
					userObject = objs.get(value.getNodeValue());
				cell = createCell(graph, type.getNodeValue(), userObject);
				// attribute map

				if (cell != null) {
					cells.put(key.getNodeValue(), cell);

					DefaultGraphCell[] children = parseChildren(graph, node,
							cells, cs);
					for (int i = 0; i < children.length; i++)
						cell.add(children[i]);

					Node source = node.getAttributes().getNamedItem("src");
					Node target = node.getAttributes().getNamedItem("tgt");
					if (source != null) {
						ConnectionID cid = new ConnectionID(cell, source
								.getNodeValue(), true);
						connectionSetIDs.add(cid);
					}
					if (target != null) {
						ConnectionID cid = new ConnectionID(cell, target
								.getNodeValue(), false);
						connectionSetIDs.add(cid);
					}

					Node boundsNode = node.getAttributes().getNamedItem("rect");
					Rectangle2D bounds = null;
					if (boundsNode != null) {
						Object rectangle = decodeValue(Rectangle2D.class,
								boundsNode.getNodeValue());
						if (rectangle instanceof Rectangle2D)
							bounds = (Rectangle2D) rectangle;
					}

					Node pointsNode = node.getAttributes().getNamedItem("pts");
					List points = null;
					if (pointsNode != null) {
						Object pointList = decodeValue(List.class, pointsNode
								.getNodeValue());
						if (pointList instanceof List)
							points = (List) pointList;
					}

					Node attr = node.getAttributes().getNamedItem("attr");
					String mapID = null;
					if (attr != null)
						mapID = attr.getNodeValue();

					if (mapID != null)
						delayedAttributes.add(new DelayedAttributeID(cell,
								bounds, points, mapID));
				}
			}
		}
		return cell;
	}

	public DefaultGraphCell[] parseChildren(JGraph graph, Node node,
			Hashtable cells, ConnectionSet cs) {
		List list = new LinkedList();
		for (int i = 0; i < node.getChildNodes().getLength(); i++) {
			Node child = node.getChildNodes().item(i);
			DefaultGraphCell cell = parseCell(graph, child, cells, cs);
			if (cell != null)
				list.add(cell);
		}
		DefaultGraphCell[] dgc = new DefaultGraphCell[list.size()];
		list.toArray(dgc);
		return dgc;
	}

	public Map parseAttrs(Node node) {
		Hashtable map = new Hashtable();
		for (int i = 0; i < node.getChildNodes().getLength(); i++) {
			Node child = node.getChildNodes().item(i);
			if (child.getNodeName().toLowerCase().equals("map")) {
				Node key = child.getAttributes().getNamedItem("id");
				Node pid = child.getAttributes().getNamedItem("pid");
				Map attrs = decodeMap(child, true, false);
				if (key != null && attrs.size() > 0) {
					if (pid != null)
						attrs.put(PARENT, pid.getNodeValue());
					map.put(key.getNodeValue(), attrs);
				}
			}
		}
		return map;
	}

	/**
	 * Returns an attributeMap for the specified position and color.
	 */
	public Map createDefaultAttributes() {
		// Create an AttributeMap
		AttributeMap map = new AttributeMap();
		// Set a Black Line Border (the Border-Attribute must be Null!)
		GraphConstants.setBorderColor(map, Color.black);
		// Return the Map
		return map;
	}

	public static class DelayedAttributeID {

		protected Object cell;

		protected Rectangle2D bounds;

		protected List points;

		protected String mapID;

		public DelayedAttributeID(Object cell, Rectangle2D bounds, List points,
				String mapID) {
			this.cell = cell;
			this.bounds = bounds;
			this.points = points;
			this.mapID = mapID;
		}

		/*
		 * (non-Javadoc)
		 */
		public Object getCell() {
			return cell;
		}

		/*
		 * (non-Javadoc)
		 */
		public Rectangle2D getBounds() {
			return bounds;
		}

		/*
		 * (non-Javadoc)
		 */
		public String getMapID() {
			return mapID;
		}

		/*
		 * (non-Javadoc)
		 */
		public List getPoints() {
			return points;
		}

		/*
		 * (non-Javadoc)
		 */
		public void setBounds(Rectangle2D rectangle) {
			bounds = rectangle;
		}

		/*
		 * (non-Javadoc)
		 */
		public void setCell(Object object) {
			cell = object;
		}

		/**
		 * @param string
		 */
		public void setMapID(String string) {
			mapID = string;
		}

		/*
		 * (non-Javadoc)
		 */
		public void setPoints(List list) {
			points = list;
		}

	}

	public static class ConnectionID {

		protected Object cell;

		protected String targetID;

		protected boolean source;

		public ConnectionID(Object cell, String targetID, boolean source) {
			this.cell = cell;
			this.targetID = targetID;
			this.source = source;
		}

		/*
		 * (non-Javadoc)
		 */
		public Object getCell() {
			return cell;
		}

		/*
		 * (non-Javadoc)
		 */
		public boolean isSource() {
			return source;
		}

		/*
		 * (non-Javadoc)
		 */
		public String getTargetID() {
			return targetID;
		}

		/**
		 * @param object
		 */
		public void setCell(Object object) {
			cell = object;
		}

		/**
		 * @param b
		 */
		public void setSource(boolean b) {
			source = b;
		}

		/**
		 * @param string
		 */
		public void setTargetID(String string) {
			targetID = string;
		}

	}

	public int getUserObjectID(Object object) {
		Integer index = (Integer) userObjectMap.get(object);
		if (index != null)
			return index.intValue();
		index = new Integer(userObjectMap.size() + 1);
		userObjectMap.put(object, index);
		return index.intValue();
	}

	public int getID(Object object) {
		Integer index = (Integer) cellMap.get(object);
		if (index != null)
			return index.intValue();
		index = new Integer(cellMap.size() + 1);
		cellMap.put(object, index);
		return index.intValue();
	}

	public class AttributeCollection {

		public List maps = new LinkedList();

		public int addMap(Map attr) {
			Iterator it = maps.iterator();
			Map storeMap = new Hashtable(attr);
			Map hook = storeMap;
			while (it.hasNext()) {
				Map ref = (Map) it.next();
				Map diff = diffMap(ref, attr);
				if (diff.size() < storeMap.size()) {
					hook = ref;
					storeMap = diff;
				}
			}
			if (storeMap.size() == 0 && hook != storeMap)
				return maps.indexOf(hook);
			if (hook != storeMap)
				storeMap.put(PARENT, hook);
			maps.add(storeMap);
			return maps.indexOf(storeMap);
		}

		public void clear() {
			maps.clear();
		}

		/**
		 * Returns a new map that contains all (key, value)-pairs of
		 * <code>newState</code> where either key is not used or value is
		 * different for key in <code>oldState</code>. In other words, this
		 * method removes the common entries from oldState and newState, and
		 * returns the "difference" between the two.
		 * 
		 * This method never returns null.
		 */
		public Map diffMap(Map oldState, Map newState) {
			// Augment oldState
			Stack s = new Stack();
			s.add(oldState);
			Object hook = oldState.get(PARENT);
			while (hook instanceof Map) {
				s.add(hook);
				hook = ((Map) hook).get(PARENT);
			}
			oldState = new Hashtable();
			while (!s.isEmpty()) {
				oldState.putAll((Map) s.pop());
			}
			Map diff = new Hashtable();
			Iterator it = newState.entrySet().iterator();
			while (it.hasNext()) {
				Map.Entry entry = (Map.Entry) it.next();
				Object key = entry.getKey();
				Object oldValue = oldState.remove(key);
				if (key != PARENT) {
					Object newValue = entry.getValue();
					if (oldValue == null || !oldValue.equals(newValue))
						diff.put(key, newValue);
				}
			}
			it = oldState.keySet().iterator();
			while (it.hasNext()) {
				Object key = it.next();
				if (!oldState.get(key).equals(""))
					diff.put(key, "");
			}
			diff.remove(PARENT);
			return diff;
		}

	}

	//
	// Codec
	//

	protected String[] knownKeys = new String[] { GraphConstants.ABSOLUTEX,
			GraphConstants.ABSOLUTEY, GraphConstants.AUTOSIZE,
			GraphConstants.BACKGROUND, GraphConstants.GRADIENTCOLOR,
			GraphConstants.BEGINFILL, GraphConstants.BEGINSIZE,
			GraphConstants.BENDABLE, GraphConstants.BORDER,
			GraphConstants.BORDERCOLOR, GraphConstants.BOUNDS,
			GraphConstants.CHILDRENSELECTABLE, GraphConstants.CONNECTABLE,
			GraphConstants.CONSTRAINED, GraphConstants.DASHPATTERN,
			GraphConstants.DASHOFFSET, GraphConstants.DISCONNECTABLE,
			GraphConstants.EDITABLE, GraphConstants.ENDFILL,
			GraphConstants.ENDSIZE, GraphConstants.EXTRALABELS,
			GraphConstants.EXTRALABELPOSITIONS, GraphConstants.FONT,
			GraphConstants.FOREGROUND, GraphConstants.HORIZONTAL_ALIGNMENT,
			GraphConstants.VERTICAL_ALIGNMENT, GraphConstants.ICON,
			GraphConstants.INSET, GraphConstants.LABELALONGEDGE,
			GraphConstants.LABELPOSITION, GraphConstants.LINEBEGIN,
			GraphConstants.LINECOLOR, GraphConstants.LINEEND,
			GraphConstants.LINESTYLE, GraphConstants.LINEWIDTH,
			GraphConstants.MOVEABLE, GraphConstants.MOVEABLEAXIS,
			GraphConstants.MOVEHIDDENCHILDREN, GraphConstants.OFFSET,
			GraphConstants.OPAQUE, GraphConstants.POINTS,
			GraphConstants.RESIZE, GraphConstants.ROUTING,
			GraphConstants.SELECTABLE, GraphConstants.SIZE,
			GraphConstants.SIZEABLE, GraphConstants.SIZEABLEAXIS,
			GraphConstants.VALUE, GraphConstants.HORIZONTAL_TEXT_POSITION,
			GraphConstants.VERTICAL_TEXT_POSITION };

	protected Class[] keyTypes = new Class[] { Boolean.class, // ABSOLUTEX
			Boolean.class, Boolean.class, // ABSOLUTEY, AUTOSIZE
			Color.class, Color.class, // BACKGROUND, GRADIENTCOLOR,
			Boolean.class, Integer.class, // BEGINFILL, BEGINSIZE,
			Boolean.class, Border.class, // BENDABLE, BORDER,
			Color.class, Rectangle2D.class, // BORDERCOLOR, BOUNDS,
			Boolean.class, Boolean.class, // CHILDRENSELECTABLE, CONNECTABLE
			Boolean.class, float[].class, // CONSTRAINED, DASHPATTERN
			float.class, // DASHOFFSET
			Boolean.class, Boolean.class, // DISCONNECTABLE, EDITABLE,
			Boolean.class, Integer.class, // ENDFILL, ENDSIZE,
			Object[].class, Point[].class, // EXTRALABELS, EXTRALABELPOSITIONS
			Font.class, Color.class, // FONT, FOREGROUND,
			Integer.class, Integer.class, // HORIZONTAL_ALIGNMENT,
			// VERTICAL_ALIGNMENT
			Icon.class, Integer.class, // ICON, INSET,
			Boolean.class, Point.class, // LABELALONGEDGE, LABELPOSITION,
			Integer.class, Color.class, // LINEBEGIN, LINECOLOR,
			Integer.class, Integer.class, // LINEEND, LINESTYLE,
			Float.class, Boolean.class, // LINEWIDTH, MOVEABLE,
			Integer.class, Boolean.class, // MOVEABLEAXIS, MOVEHIDDENCHILDREN
			Point.class, Boolean.class, // OFFSET, OPAQUE,
			List.class, Boolean.class, Edge.Routing.class, // POINTS, RESIZE,
			// ROUTING,
			Boolean.class, // SELECTABLE
			Dimension.class, Boolean.class,// SIZE, SIZEABLE,
			Integer.class, // SIZEABLEAXIS
			Object.class, Integer.class, // VALUE, HORIZONTAL_TEXT_POSITION
			Integer.class }; // VERTICAL_TEXT_POSITION

	public Map decodeMap(Node node, boolean useKnownKeys,
			boolean URLdecodeValues) {
		Hashtable map = new Hashtable();
		for (int i = 0; i < node.getChildNodes().getLength(); i++) {
			Node child = node.getChildNodes().item(i);
			if (child.getNodeName().toLowerCase().equals("a")) {
				Node key = child.getAttributes().getNamedItem("key");
				Node value = child.getAttributes().getNamedItem("val");
				if (key != null && value != null) {
					String keyVal = key.getNodeValue().toString();
					Object valueS = value.getNodeValue().toString();
					if (useKnownKeys) {
						int index = -1;
						for (int j = 0; j < knownKeys.length; j++)
							if (keyVal.equals(knownKeys[j]))
								index = j;
						if (index != -1)
							valueS = decodeValue(keyTypes[index], valueS
									.toString());
					} else if (URLdecodeValues) {

						try {
							keyVal = URLDecoder.decode(keyVal.toString(),
									"UTF-8");
							valueS = URLDecoder.decode(valueS.toString(),
									"UTF-8");
						} catch (Exception e) {
							System.err.println(e.getMessage());
						}
					}
					if (valueS != null)
						map.put(keyVal, valueS);
				}
			}
		}
		return map;
	}

	public Map decodeUserObjects(Node node) {
		Hashtable map = new Hashtable();
		for (int i = 0; i < node.getChildNodes().getLength(); i++) {
			Node child = node.getChildNodes().item(i);
			if (child.getNodeName().toLowerCase().equals("a")) {
				Node key = child.getAttributes().getNamedItem("key");
				Node value = child.getAttributes().getNamedItem("val");
				if (key != null) {
					Map properties = decodeMap(child, false, true);
					String keyVal = key.getNodeValue().toString();
					String valueS = null;
					if (value != null) {
						valueS = value.getNodeValue().toString();
						if (valueS != null) {
							try {
								valueS = URLDecoder.decode(valueS.toString(),
										"UTF-8");
							} catch (Exception e) {
								System.err.println(e.getMessage());
							}
						}
					}
					if (valueS != null
							&& (properties == null || properties.isEmpty()))
						map.put(keyVal, valueS);
					else {
						if (valueS != null && valueS.toString().length() > 0)
							properties.put(JGraphpadBusinessObject.valueKey,
									valueS);
						if (properties != null)
							map.put(keyVal, properties);
					}
				}
			}
		}
		return map;
	}

	public static final String[] tokenize(String s, String token) {
		StringTokenizer tokenizer = new StringTokenizer(s, token);
		String[] tok = new String[tokenizer.countTokens()];
		int i = 0;
		while (tokenizer.hasMoreElements()) {
			tok[i++] = tokenizer.nextToken();
		}
		return tok;
	}

	public Object decodeValue(Class key, String value) {
		if (key != String.class && key != Object.class
				&& (value == null || value.equals("")))
			return EMPTY;
		if (key == Rectangle2D.class) {
			String[] tok = tokenize(value, ",");
			if (tok.length == 4) {
				double x = Double.parseDouble(tok[0]);
				double y = Double.parseDouble(tok[1]);
				double w = Double.parseDouble(tok[2]);
				double h = Double.parseDouble(tok[3]);
				return new Rectangle2D.Double(x, y, w, h);
			}
		} else if (key == List.class) { // FIX: Do not assume Points!
			List list = new LinkedList();
			String[] tok = tokenize(value, ",");
			for (int i = 0; i < tok.length; i = i + 2) {
				double x = Double.parseDouble(tok[i]);
				double y = Double.parseDouble(tok[i + 1]);
				AttributeMap dummyMap = new AttributeMap();
				Point2D point = dummyMap.createPoint(x, y);
				list.add(point);
			}
			return list;
		} else if (key == Font.class) {
			String[] tok = tokenize(value, ",");
			if (tok.length == 3) {
				String name = tok[0];
				int size = Integer.parseInt(tok[1]);
				int style = Integer.parseInt(tok[2]);
				return new Font(name, style, size);
			}
		} else if (key == Color.class) {
			String[] tok = tokenize(value, ",");
			if (tok.length == 3) {
				int r = Integer.parseInt(tok[0]);
				int g = Integer.parseInt(tok[1]);
				int b = Integer.parseInt(tok[2]);
				return new Color(r, g, b);
			}
			return new Color(Integer.parseInt(value));
		} else if (key == Point.class) {
			String[] tok = tokenize(value, ",");
			if (tok.length == 2) {
				int x = Integer.parseInt(tok[0]);
				int y = Integer.parseInt(tok[1]);
				return new Point(x, y);
			}
		} else if (key == float[].class) {
			String[] tok = tokenize(value, ",");
			float[] f = new float[tok.length];
			for (int i = 0; i < tok.length; i++)
				f[i] = Float.parseFloat(tok[i]);
			return f;
		} else if (key == Integer.class) {
			return new Integer(value);
		} else if (key == Border.class) {
			String[] tok = tokenize(value, ",");
			if (tok[0].equals("L")) { // LineBorder
				Color c = new Color(Integer.parseInt(tok[1]));
				int thickness = Integer.parseInt(tok[2]);
				return BorderFactory.createLineBorder(c, thickness);
			} else if (tok[0].equals("B")) { // BevelBorder
				int type = Integer.parseInt(tok[1]);
				return BorderFactory.createBevelBorder(type);
			} else if (tok[0].equals("S")) { // ShadowBorder
				return JGraphpadShadowBorder.sharedInstance;
			}
			return BorderFactory.createLineBorder(Color.black, 1);
		} else if (key == Boolean.class) {
			return new Boolean(value);
		} else if (key == Float.class) {
			return new Float(value);
		} else if (key == Icon.class) {
			try {
				return new JGraphpadImageIcon(new URL(value));
			} catch (Exception e) {
				System.err.println("Invalid URL: " + value);
				return new JGraphpadImageIcon(value);
			}
		} else if (key == Edge.Routing.class) {
			if (value.equals("simple"))
				return GraphConstants.ROUTING_SIMPLE;
		}
		return value;
	}

	/**
	 * Bundle of all actions in this class.
	 */
	public static class AllActions implements Bundle {

		/**
		 * Holds the actions. All actions require an editor reference and are
		 * therefore created at construction time.
		 */
		public JGraphEditorAction actionImportJGX;

		/**
		 * Constructs the action bundle for the enclosing class.
		 */
		public AllActions(JGraphEditor editor) {
			Object vertexPrototype = editor.getSettings().getObject(
					JGraphpad.KEY_VERTEXPROTOTYPE);
			Object edgePrototype = editor.getSettings().getObject(
					JGraphpad.KEY_EDGEPROTOTYPE);
			actionImportJGX = new JGraphpadJGXAction(editor);
			actionImportJGX.putValue(KEY_VERTEXPROTOTYPE, vertexPrototype);
			actionImportJGX.putValue(KEY_EDGEPROTOTYPE, edgePrototype);
		}

		/*
		 * (non-Javadoc)
		 */
		public JGraphEditorAction[] getActions() {
			return new JGraphEditorAction[] { actionImportJGX };
		}

		/*
		 * (non-Javadoc)
		 */
		public void update() {
			Component component = getPermanentFocusOwner();
			boolean e = component instanceof JGraph;
			actionImportJGX.setEnabled(e);
		}
	}

}
