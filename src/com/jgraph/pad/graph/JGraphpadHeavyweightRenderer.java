/*
 * $Id: JGraphpadHeavyweightRenderer.java,v 1.4 2006/08/10 08:59:57 gaudenz Exp $
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
import java.awt.Component;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.border.LineBorder;

import org.jgraph.JGraph;

public class JGraphpadHeavyweightRenderer extends JPanel implements Cloneable {

	protected JTextField text = new JTextField(10);

	protected JCheckBox checkbox = new JCheckBox("Check");

	protected JSpinner spinner = new JSpinner();

	protected JComboBox combo = new JComboBox(new Object[] { "Red", "Green",
			"Blue" });

	protected JToggleButton toggle = new JToggleButton("Toggle");

	public JGraphpadHeavyweightRenderer() {
		super(new GridBagLayout());
		setBorder(new ScaledLineBorder(Color.BLACK, 1));
		GridBagConstraints c = new GridBagConstraints();
		c.anchor = GridBagConstraints.NORTHWEST;
		c.fill = GridBagConstraints.BOTH;
		c.ipadx = 4;
		c.ipady = 2;
		c.weighty = 0.0;
		c.weightx = 1.0;
		c.gridwidth = 1;
		c.gridheight = 1;
		c.gridx = 0;

		c.ipady = 4;
		c.gridy = 0;
		c.gridx = 0;
		c.weightx = 0.3;
		JLabel label = new JLabel("Text:");
		add(label, c);

		c.gridy = 0;
		c.gridx = 1;
		c.weightx = 0.7;
		c.gridwidth = 2;
		add(text, c);

		c.gridy = 1;
		c.gridx = 0;
		c.gridwidth = 3;
		c.weighty = 1.0;
		c.weightx = 1.0;
		JGraph graph = new JGraph();
		JScrollPane scrollPane = new JScrollPane(graph);
		add(scrollPane, c);

		c.weighty = 0.0;
		c.gridy = 2;
		c.gridx = 0;
		c.gridwidth = 1;
		checkbox.setOpaque(false);
		add(checkbox, c);

		c.gridx = 1;
		c.gridwidth = 2;
		combo.setOpaque(false);
		add(combo, c);

		c.gridwidth = 1;
		c.gridy = 3;
		c.gridx = 0;
		spinner.setOpaque(false);
		add(spinner, c);

		c.gridx = 1;
		JButton button = new JButton("Button");
		button.setOpaque(false);
		add(button, c);

		c.gridx = 2;
		toggle.setOpaque(false);
		add(toggle, c);
	}

	/**
	 * Workaround for the line border in Swing that has bad offsets and
	 * asymetric line widths when zoomed.
	 */
	public class ScaledLineBorder extends LineBorder {

		public ScaledLineBorder(Color color) {
			this(color, 1);
		}
		
		public ScaledLineBorder(Color color, int thickness) {
			super(color, thickness+2, false);
		}

		public void paintBorder(Component c, Graphics g, int x, int y,
				int width, int height) {
			Color oldColor = g.getColor();
			int i;

			g.setColor(lineColor);
			int thickness = this.thickness - 2;
			for (i = 0; i < thickness; i++) {
				g.drawRect(x + i + 1, y + i + 1,
						width - i - i - 2, height - i - i - 2);
			}
			g.setColor(oldColor);
		}

	}

	public Object clone() {
		JGraphpadHeavyweightRenderer clone = new JGraphpadHeavyweightRenderer();
		clone.checkbox.setSelected(checkbox.isSelected());
		clone.text.setText(text.getText());
		clone.spinner.setValue(spinner.getValue());
		clone.combo.setSelectedIndex(combo.getSelectedIndex());
		clone.toggle.setSelected(toggle.isSelected());
		return clone;
	}

}
