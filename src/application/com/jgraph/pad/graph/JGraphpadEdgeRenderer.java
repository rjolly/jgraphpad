/*
 * $Id: JGraphpadEdgeRenderer.java,v 1.5 2006/03/21 10:18:19 gaudenz Exp $
 * Copyright (c) 2001-2005, Gaudenz Alder
 * 
 * All rights reserved. 
 * 
 * This file is licensed under the JGraph software license, a copy of which
 * will have been provided to you in the file LICENSE at the root of your
 * installation directory. If you are unable to locate this file please
 * contact JGraph sales for another copy.
 */
package com.jgraph.pad.graph;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.Point2D;
import java.util.Map;

import javax.accessibility.AccessibleContext;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JTextPane;
import javax.swing.SwingConstants;
import javax.swing.border.Border;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

import org.jgraph.JGraph;
import org.jgraph.graph.CellView;
import org.jgraph.graph.EdgeRenderer;
import org.jgraph.graph.EdgeView;
import org.jgraph.graph.GraphConstants;

public class JGraphpadEdgeRenderer extends EdgeRenderer {

	/**
	 * Holds the text pane to be used for rich text rendering.
	 */
	public static JTextPane textPane = new JTextPane();

	/**
	 * An angular tolerance (actually a proportionality scalar) below which the
	 * edge label isn't rotated so that the font looks better
	 */
	private static int angleTol = 10;

	/**
	 * 
	 */
	protected boolean isRichText = false;

	/**
	 * 
	 */
	private String text = ""; // "" rather than null, for BeanBox

	/**
	 * 
	 */
	private double x_buff, y_buff;

	/**
	 * 
	 */
	private int verticalAlignment = 0;

	/**
	 * Defines the default inset to render rich text.
	 */
	public static int INSET = 4;

	/**
	 * Holds the user object of the current cell.
	 */
	protected Object userObject = null;

	public JGraphpadEdgeRenderer() {
		super();
	}

	/**
	 * Utility method to paint the rich text content for rich text values. This
	 * implementation simulates rich text vertical alignment by translating the
	 * graphics before painting the textPane.
	 * 
	 * @param g
	 *            The graphics to paint the rich text content to.
	 */
	protected void paintRichText(Graphics g, int x, int y) {
		g.translate(-x, -y);
		textPane.paint(g);
		g.translate(x, y);
	}

	/**
	 * Paint the specified label for the current edgeview.
	 */
	protected void paintLabel(Graphics g, String label, Point2D p,
			boolean mainLabel) {
		if (!mainLabel) {
			super.paintLabel(g, label, p, mainLabel);
			return;
		}

		if (p != null && label != null && label.toString().length() > 0
				&& metrics != null) {
			textPane.setSize(JGraphpadVertexRenderer.ZERO_DIMENSION);
			Dimension d = textPane.getPreferredSize();
			textPane.setSize(d.width, d.height - 14);
			int sw = textPane.getWidth();
			int sh = textPane.getHeight();
			Graphics2D g2 = (Graphics2D) g;
			boolean applyTransform = isLabelTransform(label);
			double angle = 0;
			int dx = -sw / 2;
			int offset = isMoveBelowZero || applyTransform ? 0 : Math.min(0,
					(int) (dx + p.getX()));

			g2.translate(p.getX() - offset, p.getY());
			if (applyTransform) {
				angle = getLabelAngle2(label);
				g2.rotate(angle);
			}

			int dy = (verticalAlignment == SwingConstants.TOP) ? sh
					: ((verticalAlignment == SwingConstants.BOTTOM) ? 0
							: sh / 2);
			g.setColor(fontColor);
			paintRichText(g, sw / 2, dy);
			if (applyTransform) {
				// Undo the transform
				g2.rotate(-angle);
			}
			g2.translate(-p.getX() + offset, -p.getY());
		}
	}

	/**
	 * Calculates the angle at which graphics should be rotated to paint label
	 * along the edge. Before calling this method always check that transform
	 * should be applied using {@linkisLabelTransform}
	 * 
	 * @return the value of the angle, 0 if the angle is zero or can't be
	 *         calculated
	 */
	private double getLabelAngle2(String label) {
		Point2D p = getLabelPosition(view);
		double angle = 0;
		if (p != null && label != null && label.length() > 0) {
			int sw = (int) textPane.getPreferredSize().getWidth();
			// Note: For control points you may want to choose other
			// points depending on the segment the label is in.
			Point2D p1 = view.getPoint(0);
			Point2D p2 = view.getPoint(view.getPointCount() - 1);
			// Length of the edge
			double length = Math.sqrt(x_buff + y_buff);
			if (!(length <= Double.NaN || length < sw)) { // Label fits into
				// edge's length

				// To calculate projections of edge
				double cos = (p2.getX() - p1.getX()) / length;
				double sin = (p2.getY() - p1.getY()) / length;

				// Determine angle
				angle = Math.acos(cos);
				if (sin < 0) { // Second half
					angle = 2 * Math.PI - angle;
				}
			}
			if (angle > Math.PI / 2 && angle <= Math.PI * 3 / 2) {
				angle -= Math.PI;
			}
		}
		return angle;
	}

	/**
	 * Estimates whether the transform for label should be applied. With the
	 * transform, the label will be painted along the edge. To apply transform,
	 * rotate graphics by the angle returned from {@link #getLabelAngle}
	 * 
	 * @return true, if transform can be applied, false otherwise
	 */
	private boolean isLabelTransform(String label) {
		if (!labelTransformEnabled) {
			return false;
		}
		Point2D p = getLabelPosition(view);
		if (p != null && label != null && label.length() > 0) {
			int sw = (int) textPane.getPreferredSize().getWidth();
			Point2D p1 = view.getPoint(0);
			Point2D p2 = view.getPoint(view.getPointCount() - 1);
			x_buff = p2.getX() - p1.getX();
			y_buff = p2.getY() - p1.getY();
			x_buff = x_buff * x_buff;
			y_buff = y_buff * y_buff;
			double length = Math.sqrt(x_buff + y_buff);
			if (y_buff * angleTol < x_buff)
				return false;
			if (!(length <= Double.NaN || length < sw)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Returns the label size of the specified view in the given graph.
	 */
	public Dimension getLabelSize(EdgeView view, String label) {
		Dimension d = super.getLabelSize(view, label);
		if (isRichText) {
			d = textPane.getPreferredSize();
			d.height *= 2; // worst case verical alignment
		}
		return d;
	}

	/**
	 * Defines the single line of text this component will display. If the value
	 * of text is null or empty string, nothing is displayed.
	 * <p>
	 * The default value of this property is null.
	 * <p>
	 * This is a JavaBeans bound property.
	 * 
	 * @see #setVerticalTextPosition
	 * @see #setHorizontalTextPosition
	 * @see #setIcon
	 * @beaninfo preferred: true bound: true attribute: visualUpdate true
	 *           description: Defines the single line of text this component
	 *           will display.
	 */
	public void setText(String text) {

		String oldAccessibleName = null;
		if (accessibleContext != null) {
			oldAccessibleName = accessibleContext.getAccessibleName();
		}

		String oldValue = this.text;
		this.text = text;
		firePropertyChange("text", oldValue, text);

		if ((accessibleContext != null)
				&& (accessibleContext.getAccessibleName() != oldAccessibleName)) {
			accessibleContext.firePropertyChange(
					AccessibleContext.ACCESSIBLE_VISIBLE_DATA_PROPERTY,
					oldAccessibleName, accessibleContext.getAccessibleName());
		}
		if (text == null || oldValue == null || !text.equals(oldValue)) {
			revalidate();
			repaint();
		}
	}

	/**
	 * Extends the parent's method to configure the renderer for displaying the
	 * specified view.
	 * 
	 * @param view
	 *            The view to configure the renderer for.
	 */
	public void installAttributes(CellView view) {
		super.installAttributes(view);

		// workaround for missing graph reference at the first call before any
		// edge will
		// need to render rich text
		if (graph == null)
			return;
		JGraph graph = (JGraph) this.graph.get();

		// Configures the rich text or component value
		userObject = graph.getModel().getValue(view.getCell());
		if (userObject instanceof JGraphpadBusinessObject) {
			JGraphpadBusinessObject obj = (JGraphpadBusinessObject) userObject;
			isRichText = obj.isRichText();
		} else {
			isRichText = false;
		}
		verticalAlignment = getVerticalAlignment(view.getAllAttributes());
		// Configures the rich text box for rendering the rich text
		if (isRichText) {
			StyledDocument document = (StyledDocument) textPane.getDocument();
			((JGraphpadRichTextValue) ((JGraphpadBusinessObject) userObject)
					.getValue()).insertInto(document);

			textPane.setBorder(GraphConstants
					.getBorder(view.getAllAttributes()));
			Color bordercolor = GraphConstants.getBorderColor(view
					.getAllAttributes());
			int borderWidth = Math.max(1, Math.round(GraphConstants
					.getLineWidth(view.getAllAttributes())));
			if (textPane.getBorder() == null && bordercolor != null) {
				textPane.setBorder(BorderFactory.createLineBorder(bordercolor,
						borderWidth));
			}
			Border insetBorder = BorderFactory.createEmptyBorder(INSET, INSET,
					INSET, INSET);
			textPane.setBorder(BorderFactory.createCompoundBorder(textPane
					.getBorder(), insetBorder));
			Color background = GraphConstants.getBackground(view
					.getAllAttributes());
			if (background != null) {
				textPane.setBackground(background);
				textPane.setOpaque(true);
			} else {
				textPane.setOpaque(false);
			}

			// Uses the label's alignment and sets it on the text pane to work
			// around the problem of the text pane alignments not being stored.
			// Note: As a consequence a text pane can only have one alignment
			// for all text it contains. It is not possible to align the
			// paragraphs individually.
			int align = GraphConstants.getHorizontalAlignment(view
					.getAllAttributes());
			SimpleAttributeSet sas = new SimpleAttributeSet();
			align = (align == JLabel.CENTER) ? StyleConstants.ALIGN_CENTER
					: (align == JLabel.RIGHT) ? StyleConstants.ALIGN_RIGHT
							: StyleConstants.ALIGN_LEFT;
			StyleConstants.setAlignment(sas, align);
			document.setParagraphAttributes(0, document.getLength(), sas, true);
		}
	}

	public int getVerticalAlignment(Map map) {
		Integer intObj = (Integer) map.get(GraphConstants.VERTICAL_ALIGNMENT);
		if (intObj != null)
			return intObj.intValue();
		return JLabel.TOP;
	}

	public static int getAngleTol() {
		return angleTol;
	}

}
