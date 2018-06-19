/* 
 * $Id: JGraphpadL2FPropertySheet.java,v 1.4 2005/08/28 21:28:32 gaudenz Exp $
 * Copyright (c) 2001-2005, Gaudenz Alder
 * 
 * All rights reserved.
 * 
 * See LICENSE file for license details. If you are unable to locate
 * this file please contact info (at) jgraph (dot) com.
 */
package com.jgraph.l2fplugin;

import java.awt.Component;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.jgraph.JGraph;
import org.jgraph.graph.GraphConstants;
import org.jgraph.graph.PortView;
import org.w3c.dom.Node;

import com.jgraph.editor.JGraphEditorResources;
import com.jgraph.editor.factory.JGraphEditorFactoryMethod;
import com.jgraph.pad.graph.JGraphpadBusinessObject;
import com.jgraph.pad.util.JGraphpadFocusManager;
import com.l2fprod.common.propertysheet.DefaultProperty;
import com.l2fprod.common.propertysheet.Property;
import com.l2fprod.common.propertysheet.PropertySheetPanel;

/**
 * Property sheet to display cell and user object properties.
 */
public class JGraphpadL2FPropertySheet extends PropertySheetPanel {

	/**
	 * Constructs a new property sheet.
	 */
	public JGraphpadL2FPropertySheet() {
		setSortingProperties(true);
		setBorder(null);
	}

	/**
	 * Updates the display properties to show the properties of the selection
	 * cell in the specified graph.
	 * 
	 * @param graph
	 *            The graph to display the selection cell properties for.
	 */
	public void update(JGraph graph) {
		if (graph != null && !graph.isSelectionEmpty()) {
			Object cell = graph.getSelectionCell();
			Map attrs = graph.getModel().getAttributes(cell);
			Property[] properties = createProperties(JGraphEditorResources
					.getString("Graph"), attrs, createPropertyChangeListener(
					graph, cell));

			// Adds the business object properties to the array
			Object obj = graph.getModel().getValue(cell);
			if (obj instanceof JGraphpadBusinessObject) {
				Property[] tmp1 = createProperties(JGraphEditorResources
						.getString("Business"), ((JGraphpadBusinessObject) obj)
						.getProperties(), createPropertyChangeListener(graph,
						obj));
				Property[] tmp2 = properties;
				properties = new Property[tmp1.length + tmp2.length];
				System.arraycopy(tmp1, 0, properties, 0, tmp1.length);
				System.arraycopy(tmp2, 0, properties, tmp1.length, tmp2.length);
			}
			setProperties(properties);
		} else {
			// Resets properties
			//setProperties(new Property[0]);
		}
	}

	/**
	 * Creates an array of properties out of the key, value pairs in the
	 * specified map using the specified category.
	 * 
	 * @param map
	 *            The map that contains the key, value pairs to be added.
	 * @param category
	 *            The category of the new properties.
	 * @return Returns an array of properties for <code>map</code>.
	 */
	protected Property[] createProperties(String category, Map map,
			PropertyChangeListener listener) {
		Property[] properties = new Property[map.size()];
		Iterator it = map.entrySet().iterator();
		int i = 0;
		while (it.hasNext()) {
			Map.Entry entry = (Map.Entry) it.next();
			properties[i++] = createProperty(category, String.valueOf(entry
					.getKey()), entry.getValue(), listener);
		}
		return properties;
	}

	/**
	 * Creates a new property using the specified name, value and category.
	 * 
	 * @param name
	 *            The name of the property to be created.
	 * @param value
	 *            The value of the property to be created.
	 * @param category
	 *            The category of the new property.
	 * @return Returns a new property.
	 */
	protected Property createProperty(String category, String name,
			Object value, PropertyChangeListener listener) {
		DefaultProperty property = new DefaultProperty();
		property.setDisplayName(name);
		property.setShortDescription(name);
		property.setName(name);
		property.setType(value.getClass());
		property.setValue(convertValue(value));
		property.setEditable(listener != null);
		property.setCategory(category);
		if (listener != null)
			property.addPropertyChangeListener(listener);
		return property;
	}

	/**
	 * Converts the value to a string representation if it is not supported as
	 * an editable value by the property sheet.
	 */
	public Object convertValue(Object value) {
		if (value instanceof Rectangle2D) {
			Rectangle2D rect = (Rectangle2D) value;
			value = "[x=" + rect.getX() + ", y=" + rect.getY() + ", w="
					+ rect.getWidth() + ", h=" + rect.getHeight() + "]";
		} else if (value instanceof PortView) {
			PortView pv = (PortView) value;
			Point2D p = pv.getLocation();
			value = "port[null]";
			if (p != null)
				value = "port[x=" + p.getX() + ", y=" + p.getY() + "]";
		} else if (value instanceof Point2D) {
			Point2D p = (Point2D) value;
			value = "[x=" + p.getX() + ", y=" + p.getY() + "]";
		} else if (value instanceof List) {
			String entries = "";
			Iterator it = ((List) value).iterator();
			if (it.hasNext())
				entries = String.valueOf(convertValue(it.next()));
			while (it.hasNext())
				entries += ", " + String.valueOf(convertValue(it.next()));
			value = entries;
		}
		return value;
	}

	/**
	 * Creates a property change listener that uses the specified keyCell to add
	 * an entry to the nested map for the changed attributes.
	 * 
	 * @param graph
	 *            The graph to be used for changing the key cell.
	 * @param keyCell
	 *            The object to be used as a key in the nested map.
	 */
	protected PropertyChangeListener createPropertyChangeListener(
			final JGraph graph, final Object keyCell) {
		return new PropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent arg0) {
				Property source = (Property) arg0.getSource();
				Map nested = GraphConstants.createAttributes(keyCell, source
						.getName(), arg0.getNewValue());

				// Checks if we're dealing with a business object and
				// redirect the edit call to the model if we do.
				if (!graph.getModel().contains(keyCell))
					graph.getModel().edit(nested, null, null, null);
				else
					graph.getGraphLayoutCache().edit(nested, null, null, null);
			}
		};
	}

	public static class FactoryMethod extends JGraphEditorFactoryMethod {

		/**
		 * Defines the default name for factory methods of this kind.
		 */
		public static String NAME = "createPropertySheet";

		/**
		 * Constructs a new factory method using {@link #NAME}.
		 */
		public FactoryMethod() {
			super(NAME);
		}

		/*
		 * (non-Javadoc)
		 */
		public Component createInstance(Node configuration) {
			final JGraphpadL2FPropertySheet propertySheet = new JGraphpadL2FPropertySheet();
			propertySheet.setSortingCategories(true);
			propertySheet.setFocusable(false);
			// Updates the property sheet on selection, model and layout cache
			// changes.
			final JGraphpadFocusManager focusedGraph = JGraphpadFocusManager
					.getCurrentGraphFocusManager();
			focusedGraph
					.addPropertyChangeListener(new PropertyChangeListener() {
						public void propertyChange(PropertyChangeEvent e) {
							String prop = e.getPropertyName();
							JGraph graph = focusedGraph.getFocusedGraph();
							if (JGraphpadFocusManager.SELECTION_CHANGE_NOTIFICATION
									.equals(prop)
									|| JGraphpadFocusManager.MODEL_CHANGE_NOTIFICATION
											.equals(prop)
									|| JGraphpadFocusManager.UNDOABLE_CHANGE_NOTIFICATION
											.equals(prop)
									|| JGraphpadFocusManager.FOCUSED_GRAPH_PROPERTY
											.equals(prop)) {

								// This is fired when switching from the graph
								// to the property panel for example and should
								// be ignored.
								if (prop
										.equals(JGraphpadFocusManager.FOCUSED_GRAPH_PROPERTY)
										&& e.getNewValue() == null)
									return;
								propertySheet.update(graph);
							}
						}
					});

			return propertySheet;
		}

	}

}