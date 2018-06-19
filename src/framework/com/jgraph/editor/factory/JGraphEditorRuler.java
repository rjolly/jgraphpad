/* 
 * $Id: JGraphEditorRuler.java,v 1.1.1.1 2005/08/04 11:21:58 gaudenz Exp $
 * Copyright (c) 2001-2005, Gaudenz Alder
 * 
 * All rights reserved.
 * 
 * See LICENSE file for license details. If you are unable to locate
 * this file please contact info (at) jgraph (dot) com.
 */
package com.jgraph.editor.factory;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.geom.Point2D;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.NumberFormat;

import javax.swing.JComponent;

import org.jgraph.JGraph;

/**
 * Component that displays a ruler for a JGraph component.
 */
public class JGraphEditorRuler extends JComponent implements
		MouseMotionListener, PropertyChangeListener {

	/**
	 * Defines the constants for horizontal and vertical orientation.
	 */
	public static int ORIENTATION_HORIZONTAL = 0, ORIENTATION_VERTICAL = 1;

	/**
	 * Internal constant used to describe the screen resolution (DPI). Default
	 * is 72.
	 */
	protected static int INCH = 72;

	/**
	 * Holds the shared number formatter.
	 * 
	 * @see NumberFormat#getInstance()
	 */
	public static final NumberFormat numberFormat = NumberFormat.getInstance();

	/**
	 * Configuers the number format.
	 */
	static {
		numberFormat.setMaximumFractionDigits(2);
	}

	/**
	 * Defines the inactive background border. Default is a not-so-dark gray.
	 */
	protected Color inactiveBackground = new Color(170, 170, 170);

	/**
	 * Specifies the orientation.
	 */
	protected int orientation = ORIENTATION_HORIZONTAL;

	/**
	 * Specified that start and length of the active region, ie the region to
	 * paint with the background border. This is used for example to indicate
	 * the printable region of a graph.
	 */
	protected int activeoffset, activelength;

	/**
	 * Specifies the scale for the metrics. Default is
	 * {@link JGraphEditorDiagramPane#DEFAULT_PAGESCALE}.
	 */
	protected double scale = JGraphEditorDiagramPane.DEFAULT_PAGESCALE;

	/**
	 * Specifies the unit system. Default is
	 * {@link JGraphEditorDiagramPane#DEFAULT_ISMETRIC}.
	 */
	protected boolean isMetric = JGraphEditorDiagramPane.DEFAULT_ISMETRIC;

	/**
	 * Specifies height or width of the ruler. Default is 15 pixels.
	 */
	protected int rulerSize = 15;

	/**
	 * Reference to the attached graph.
	 */
	protected JGraph graph;

	/**
	 * Holds the current and first mouse point.
	 */
	protected Point drag, mouse = new Point();

	/**
	 * Parameters to control the display.
	 */
	protected double increment, units;

	/**
	 * Constructs a new ruler for the specified graph and orientation.
	 * 
	 * @param graph
	 *            The graph to create the ruler for.
	 * @param orientation
	 *            The orientation to use for the ruler.
	 */
	public JGraphEditorRuler(JGraph graph, int orientation) {
		this.orientation = orientation;
		this.graph = graph;
		updateIncrementAndUnits();
		graph.addMouseMotionListener(this);
		graph.addPropertyChangeListener(this);
	}

	/**
	 * Sets the start of the active region in pixels.
	 * 
	 * @param offset
	 *            The start of the active region.
	 */
	public void setActiveOffset(int offset) {
		activeoffset = (int) (offset * scale);
	}

	/**
	 * Sets the length of the active region in pixels.
	 * 
	 * @param length
	 *            The length of the active region.
	 */
	public void setActiveLength(int length) {
		activelength = (int) (length * scale);
	}

	/**
	 * Returns true if the ruler uses metric units.
	 * 
	 * @return Returns if the ruler is metric.
	 */
	public boolean isMetric() {
		return isMetric;
	}

	/**
	 * Sets if the ruler uses metric units.
	 * 
	 * @param isMetric
	 *            Whether to use metric units.
	 */
	public void setMetric(boolean isMetric) {
		this.isMetric = isMetric;
		updateIncrementAndUnits();
		repaint();
	}

	/**
	 * Returns the ruler's horizontal or vertical size.
	 * 
	 * @return Returns the rulerSize.
	 */
	public int getRulerSize() {
		return rulerSize;
	}

	/**
	 * Sets the ruler's horizontal or vertical size.
	 * 
	 * @param rulerSize
	 *            The rulerSize to set.
	 */
	public void setRulerSize(int rulerSize) {
		this.rulerSize = rulerSize;
	}

	/**
	 * Returns the preferred size by replacing the respective component of the
	 * graph's preferred size with {@link #rulerSize}.
	 * 
	 * @return Returns the preferred size for the ruler.
	 */
	public Dimension getPreferredSize() {
		Dimension dim = graph.getPreferredSize();
		if (orientation == ORIENTATION_VERTICAL)
			dim.width = rulerSize;
		else
			dim.height = rulerSize;
		return dim;
	}

	/*
	 * (non-Javadoc)
	 */
	public void mouseMoved(MouseEvent e) {
		if (drag != null) {
			Point old = drag;
			drag = null;
			repaint(old.x, old.y);
		}
		Point old = mouse;
		mouse = e.getPoint();
		repaint(old.x, old.y);
		repaint(mouse.x, mouse.y);
	}

	/*
	 * (non-Javadoc)
	 */
	public void mouseDragged(MouseEvent e) {
		Point old = drag;
		drag = e.getPoint();
		if (old != null)
			repaint(old.x, old.y);
		repaint(drag.x, drag.y);
	}

	/*
	 * (non-Javadoc)
	 */
	public void propertyChange(PropertyChangeEvent event) {
		String changeName = event.getPropertyName();
		if (changeName.equals(JGraph.SCALE_PROPERTY))
			repaint();
	}

	/**
	 * Updates the local variables used for painting based on the current scale
	 * and unit system.
	 */
	protected void updateIncrementAndUnits() {
		if (isMetric) {
			units = INCH / 2.54; // 2.54 dots per centimeter
			units *= graph.getScale() * scale;
			increment = units;
		} else {
			units = INCH;
			units *= graph.getScale() * scale;
			increment = units / 2;
		}
	}

	/**
	 * Repaints the ruler between the specified 0 and x or y depending on the
	 * orientation.
	 * 
	 * @param x
	 *            The endpoint for repainting a horizontal ruler.
	 * @param y
	 *            The endpoint for repainting a vertical ruler.
	 */
	public void repaint(int x, int y) {
		if (orientation == ORIENTATION_VERTICAL)
			repaint(0, y, rulerSize, 1);
		else
			repaint(x, 0, 1, rulerSize);
	}

	/**
	 * Paints the ruler.
	 * 
	 * @param g
	 *            The graphics to paint the ruler to.
	 */
	public void paintComponent(Graphics g) {
		revalidate();
		updateIncrementAndUnits();
		Rectangle drawHere = g.getClipBounds();

		// Fills clipping area with background.
		if (activelength > 0 && inactiveBackground != null)
			g.setColor(inactiveBackground);
		else
			g.setColor(getBackground());
		g.fillRect(drawHere.x, drawHere.y, drawHere.width, drawHere.height);

		// Draws the active region.
		Point2D p = graph.toScreen(new Point2D.Double(activeoffset,
				activelength));
		g.setColor(getBackground());
		if (orientation == ORIENTATION_VERTICAL)
			g.fillRect(drawHere.x, (int) p.getX(), drawHere.width, (int) p
					.getY());
		else
			g.fillRect((int) p.getX(), drawHere.y, (int) p.getY(),
					drawHere.height);

		// Do the ruler labels in a small font that'buttonSelect black.
		g.setFont(new Font("SansSerif", Font.PLAIN, 8));
		g.setColor(Color.black);

		// Some vars we need.
		double end = 0;
		double start = 0;
		int tickLength = 0;
		String text = null;

		// Uses clipping bounds to calculate first tick and last tick
		// location.
		if (orientation == ORIENTATION_VERTICAL) {
			start = Math.floor(drawHere.y / increment) * increment;
			end = Math.ceil((drawHere.y + drawHere.height) / increment)
					* increment;
		} else {
			start = Math.floor(drawHere.x / increment) * increment;
			end = Math.ceil((drawHere.x + drawHere.width) / increment)
					* increment;
		}

		// Makes a special case of 0 to display the number
		// within the rule and draw a units label.
		if (start == 0) {
			text = Integer.toString(0) + (isMetric ? " cm" : " in");
			tickLength = 10;
			if (orientation == ORIENTATION_VERTICAL) {
				g.drawLine(rulerSize - 1, 0, rulerSize - tickLength - 1, 0);
				g.drawString(text, 1, 11);
			} else {
				g.drawLine(0, rulerSize - 1, 0, rulerSize - tickLength - 1);
				g.drawString(text, 2, 11);
			}
			text = null;
			start = increment;
		}

		// Ticks and labels
		for (double i = start; i < end; i += increment) {
			if (units == 0)
				units = 1;
			tickLength = 10;
			// VW make relative to scaling factor
			text = numberFormat.format(i / units);
			if (tickLength != 0) {
				if (orientation == ORIENTATION_VERTICAL) {
					g.drawLine(rulerSize - 1, (int) i, rulerSize - tickLength
							- 1, (int) i);
					if (text != null)
						g.drawString(text, 0, (int) i + 9);
				} else {
					g.drawLine((int) i, rulerSize - 1, (int) i, rulerSize
							- tickLength - 1);
					if (text != null)
						g.drawString(text, (int) i + 2, 11);
				}
			}
		}

		// Draw Mouseposition
		g.setColor(Color.green);
		if (orientation == ORIENTATION_VERTICAL)
			g.drawLine(rulerSize - 1, mouse.y, rulerSize - tickLength - 1,
					mouse.y);
		else
			g.drawLine(mouse.x, rulerSize - 1, mouse.x, rulerSize - tickLength
					- 1);
		if (drag != null)
			if (orientation == ORIENTATION_VERTICAL)
				g.drawLine(rulerSize - 1, drag.y, rulerSize - tickLength - 1,
						drag.y);
			else
				g.drawLine(drag.x, rulerSize - 1, drag.x, rulerSize
						- tickLength - 1);
	}

}