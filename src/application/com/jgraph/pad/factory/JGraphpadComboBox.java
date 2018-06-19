/* 
 * $Id: JGraphpadComboBox.java,v 1.4 2006/02/03 14:32:46 david Exp $
 * Copyright (c) 2001-2005, Gaudenz Alder
 * 
 * All rights reserved.
 * 
 * See LICENSE file for license details. If you are unable to locate
 * this file please contact info (at) jgraph (dot) com.
 */
package com.jgraph.pad.factory;

import java.awt.Color;
import java.awt.Component;
import java.awt.geom.Rectangle2D;
import java.util.Hashtable;
import java.util.Map;

import javax.swing.JComboBox;

import org.jgraph.graph.AbstractCellView;
import org.jgraph.graph.GraphConstants;
import org.w3c.dom.Node;

import com.jgraph.editor.JGraphEditorResources;
import com.jgraph.editor.factory.JGraphEditorComboBox;
import com.jgraph.editor.factory.JGraphEditorFactoryMethod;
import com.jgraph.editor.factory.JGraphEditorComboBox.ComboBoxListener;
import com.jgraph.pad.dialog.JGraphpadDialogs;
import com.jgraph.pad.graph.JGraphpadGraphConstants;
import com.jgraph.pad.graph.JGraphpadVertexRenderer;
import com.jgraph.pad.graph.JGraphpadVertexView;

/**
 * Combo box factory methods with special dialogs and custom cell views for
 * specific attributes (shape).
 */
public class JGraphpadComboBox {

	/**
	 * Defines the combo box types for {@link ColorComboFactoryMethod}.
	 */
	public static final int TYPE_BACKGROUND = 0, TYPE_GRADIENT = 1,
			TYPE_LINECOLOR = 2;

	/**
	 * Default array of colors.
	 */
	public static Color[] defaultColors = new Color[] { Color.black,
			Color.blue, Color.cyan, Color.darkGray, Color.gray, Color.green,
			Color.lightGray, Color.magenta, Color.orange, Color.pink,
			Color.red, Color.white, Color.yellow };

	/**
	 * Default array of shapes.
	 */
	public static int[] defaultShapes = new int[] {
			JGraphpadVertexRenderer.SHAPE_RECTANGLE,
			JGraphpadVertexRenderer.SHAPE_ROUNDED,
			JGraphpadVertexRenderer.SHAPE_CIRCLE,
			JGraphpadVertexRenderer.SHAPE_DIAMOND,
			JGraphpadVertexRenderer.SHAPE_TRIANGLE,
			JGraphpadVertexRenderer.SHAPE_CYLINDER };

	/**
	 * Provides a factory method to construct a color combo box.
	 */
	public static class ColorComboFactoryMethod extends
			JGraphEditorFactoryMethod {

		/**
		 * Defines the default name for factory methods of this kind.
		 */
		public static String NAME = "createBackgroundCombo";

		/**
		 * Specifies the type of the color combo box.
		 */
		protected int type = 0;

		/**
		 * Constructs a color combo box of type {@link #TYPE_BACKGROUND} using
		 * {@link #NAME}.
		 */
		public ColorComboFactoryMethod() {
			this(NAME, TYPE_BACKGROUND);
		}

		/**
		 * Constructs a color combo box of the specified type.
		 * 
		 * @param type
		 *            The type of the combo box to be created.
		 */
		public ColorComboFactoryMethod(String name, int type) {
			super(name);
			this.type = type;
		}

		/*
		 * (non-Javadoc)
		 */
		public Component createInstance(Node configuration) {
			Map[] attrs = new Hashtable[defaultColors.length + 2];

			// Adds a special entry that displays a color bucket
			// icon. Below a special listener is installed to handle
			// clicks on this special entry. The renderer bridge
			// takes care of displaying the icon.
			attrs[0] = new Hashtable();
			GraphConstants.setIcon(attrs[0], JGraphEditorResources
					.getImage("/com/jgraph/pad/images/color.gif"));

			// Adds special entries to remove the color properties
			// from the selection cells.
			attrs[1] = new Hashtable();
			if (type == TYPE_BACKGROUND)
				GraphConstants.setRemoveAttributes(attrs[1], new Object[] {
						GraphConstants.BACKGROUND, GraphConstants.OPAQUE });
			else if (type == TYPE_GRADIENT)
				GraphConstants.setRemoveAttributes(attrs[1], new Object[] {
						GraphConstants.GRADIENTCOLOR, GraphConstants.OPAQUE });
			else
				GraphConstants.setRemoveAttributes(attrs[1],
						new Object[] { GraphConstants.LINECOLOR });
			GraphConstants.setIcon(attrs[1], JGraphEditorResources
					.getImage("/com/jgraph/pad/images/delete.gif"));

			// Adds the standard entries for each color
			for (int i = 0; i < defaultColors.length; i++) {
				Map m = attrs[i + 2] = new Hashtable(4);
				setColor(m, defaultColors[i]);
			}

			// Overrides the default action listener's getSelection hook
			// to return a specially created map which uses the color
			// value from a dialog. This behaviour is for the first
			// entry, which is displayed with a color bucket icon.
			JGraphEditorComboBox comboBox = new JGraphEditorComboBox(attrs,
					type == TYPE_LINECOLOR);
			comboBox.addActionListener(new ComboBoxListener() {
				protected Object getSelection(JComboBox box) {
					if (box.getSelectedIndex() == 0) {
						Color color = JGraphpadDialogs
								.getSharedInstance()
								.colorDialog(
										box,
										JGraphEditorResources
												.getString("SelectColor"), null);
						if (color != null) {
							Map map = new Hashtable(2);
							setColor(map, color);
							return map;
						} else {
							return null;
						}
					} else if (box.getSelectedIndex() == 1) {
						Object obj = super.getSelection(box);
						if (obj instanceof Map) {
							Map map = new Hashtable((Map) obj);
							map.remove(GraphConstants.ICON);
							return map;
						} else {
							return obj;
						}
					}
					return super.getSelection(box);
				}
			});
			comboBox.setFocusable(false);
			return comboBox;
		}

		/**
		 * Sets the specified color in <code>map</code> according to the type
		 * of the combo box.
		 * 
		 * @param m
		 *            The map to set the color in.
		 * @param color
		 *            The color to be set.
		 */
		protected void setColor(Map m, Color color) {
			if (type == TYPE_GRADIENT) {
				GraphConstants.setGradientColor(m, color);
				GraphConstants.setOpaque(m, true);
			} else if (type == TYPE_LINECOLOR) {
				GraphConstants.setLineColor(m, color);
			} else {
				GraphConstants.setBackground(m, color);
				GraphConstants.setOpaque(m, true);
			}
		}

	}

	/**
	 * Provides a factory method to construct a shape combo box.
	 */
	public static class VertexShapeComboFactoryMethod extends
			JGraphEditorFactoryMethod {

		/**
		 * Defines the default name for factory methods of this kind.
		 */
		public static String NAME = "createVertexShapeCombo";

		/**
		 * Constructs a new border combo factory method using {@link #NAME}.
		 */
		public VertexShapeComboFactoryMethod() {
			super(NAME);
		}

		/*
		 * (non-Javadoc)
		 */
		public Component createInstance(Node configuration) {
			Map[] attrs = new Hashtable[defaultShapes.length];
			for (int i = 0; i < defaultShapes.length; i++) {
				attrs[i] = new Hashtable(2);
				JGraphpadGraphConstants.setVertexShape(attrs[i],
						defaultShapes[i]);
			}
			AbstractCellView view = new JGraphpadVertexView("");
			GraphConstants.setBorderColor(view.getAttributes(), Color.BLACK);
			Rectangle2D bounds = new Rectangle2D.Double(0, 0, 14, 14);
			GraphConstants.setBounds(view.getAttributes(), bounds);
			JGraphEditorComboBox comboBox = new JGraphEditorComboBox(attrs,
					view, false);
			comboBox
					.addActionListener(new JGraphEditorComboBox.ComboBoxListener());
			comboBox.setFocusable(false);
			return comboBox;
		}

	}

}
