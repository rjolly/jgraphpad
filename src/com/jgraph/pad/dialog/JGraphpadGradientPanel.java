/* 
 * $Id: JGraphpadGradientPanel.java,v 1.1.1.1 2005/08/04 11:21:58 gaudenz Exp $
 * Copyright (c) 2001-2005, Gaudenz Alder
 * 
 * All rights reserved.
 * 
 * See LICENSE file for license details. If you are unable to locate
 * this file please contact info (at) jgraph (dot) com.
 */
package com.jgraph.pad.dialog;

import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;

import javax.swing.JPanel;

/**
 * Panel with a gradient background.
 */
public class JGraphpadGradientPanel extends JPanel {

	/**
	 * Start- and endcolor of the gradient fill.
	 */
	protected Color startColor, endColor;

	/**
	 * Constructs a new gradient panel with no gradient.
	 */
	public JGraphpadGradientPanel() {
		this(null);
	}

	/**
	 * Constructs a new gradient panel with the specified start- and no end
	 * color (background is used).
	 * 
	 * @param startColor
	 *            The start color to use for the gradient.
	 */
	public JGraphpadGradientPanel(Color startColor) {
		this(startColor, null);
	}

	/**
	 * Constructs a new gradient panel with the specified start color and end
	 * colors. If start or end color is <code>null</code>, then the
	 * component's background color is used.
	 * 
	 * @param startColor
	 *            The start color to use for the gradient.
	 * @param endColor
	 *            The end color to use for the gradient.
	 */
	public JGraphpadGradientPanel(Color startColor, Color endColor) {
		setOpaque(false);
		if (startColor == null)
			startColor = getBackground();
		if (endColor == null)
			endColor = getBackground();
		this.startColor = startColor;
		this.endColor = endColor;
	}

	/**
	 * Paints a gradient background on <code>g</code>.
	 */
	public void paint(Graphics g) {
		Graphics2D g2d = (Graphics2D) g;
		g2d.setPaint(new GradientPaint(0, 0, getStartColor(), getWidth(),
				getHeight(), getEndColor(), true));
		g2d.fillRect(0, 0, getWidth(), getHeight());
		super.paint(g);
	}

	/**
	 * Returns the endcolor for the gradient fill.
	 * 
	 * @return Returns the end color.
	 */
	public Color getEndColor() {
		return endColor;
	}

	/**
	 * Sets the endcolor for the gradient fill.
	 * 
	 * @param endColor
	 *            The end color of the gradient.
	 */
	public void setEndColor(Color endColor) {
		this.endColor = endColor;
	}

	/**
	 * Returns the startcolor for the gradient fill.
	 * 
	 * @return Returns the start color.
	 */
	public Color getStartColor() {
		return startColor;
	}

	/**
	 * Sets the startcolor for the gradient fill.
	 * 
	 * @param startColor
	 *            The start color of the gradient.
	 */
	public void setStartColor(Color startColor) {
		this.startColor = startColor;
	}

}
