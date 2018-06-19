/* 
 * $Id: JGraphpadStatusBar.java,v 1.2 2005/08/05 19:56:23 gaudenz Exp $
 * Copyright (c) 2001-2005, Gaudenz Alder
 * 
 * All rights reserved.
 * 
 * See LICENSE file for license details. If you are unable to locate
 * this file please contact info (at) jgraph (dot) com.
 */
package com.jgraph.pad.factory;

import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.NumberFormat;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.event.TreeModelEvent;

import org.jgraph.JGraph;
import org.w3c.dom.Node;

import com.jgraph.JGraphEditor;
import com.jgraph.editor.JGraphEditorFile;
import com.jgraph.editor.JGraphEditorModel;
import com.jgraph.editor.JGraphEditorResources;
import com.jgraph.editor.factory.JGraphEditorDiagramPane;
import com.jgraph.editor.factory.JGraphEditorFactoryMethod;
import com.jgraph.pad.util.JGraphpadFocusManager;
import com.jgraph.pad.util.JGraphpadTreeModelAdapter;

/**
 * Status bar to display general information about the focused graph.
 */
public class JGraphpadStatusBar extends JPanel {

	/**
	 * Holds the shared number formatter.
	 * 
	 * @see NumberFormat#getInstance()
	 */
	public static final NumberFormat numberFormat = NumberFormat.getInstance();

	/**
	 * Holds the labels contained in the status bar.
	 */
	protected JLabel infoLabel = new JLabel(getString("NoDocument")),
			modifiedLabel = new JLabel(" "), editableLabel = new JLabel(" "),
			zoomLabel = new JLabel(" "), mouseLabel = new JLabel(" "),
			spareLabel = new JLabel(" ");

	/**
	 * Constructs a new repository panel for the specified editor.
	 */
	public JGraphpadStatusBar(JGraphEditor editor) {
		setBorder(BorderFactory.createEmptyBorder(4, 5, 5, 4));
		setLayout(new GridBagLayout());
		numberFormat.setMaximumFractionDigits(0);

		GridBagConstraints c = new GridBagConstraints();
		c.anchor = GridBagConstraints.NORTHWEST;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.ipadx = 4;
		c.ipady = 2;
		c.weighty = 0.0;
		c.weightx = 1.0;
		c.gridwidth = 1;
		c.gridx = 0;
		c.gridy = 0;
		c.weightx = 4;
		add(infoLabel, c);

		c.gridx = 1;
		c.weightx = 1;
		add(modifiedLabel, c);

		c.gridx = 2;
		c.weightx = 1;
		add(editableLabel, c);

		c.gridx = 3;
		c.weightx = 1;
		add(zoomLabel, c);

		c.gridx = 4;
		c.weightx = 1;
		add(mouseLabel, c);

		c.gridx = 5;
		c.weightx = 3;
		add(spareLabel, c);

		// Installs a listener to update the mouse location
		JGraphpadFocusManager.getCurrentGraphFocusManager()
				.addMouseMotionListener(new MouseMotionListener() {

					/*
					 * (non-Javadoc)
					 */
					public void mouseDragged(MouseEvent e) {
						mouseLabel.setText(e.getX() + " : " + e.getY());
					}

					/*
					 * (non-Javadoc)
					 */
					public void mouseMoved(MouseEvent e) {
						mouseLabel.setText(e.getX() + " : " + e.getY());
					}
				});

		// Installs a listener to listen to the focused graph and react
		// to all kinds of changes, such as selection, model, cache etc.
		JGraphpadFocusManager.getCurrentGraphFocusManager()
				.addPropertyChangeListener(new PropertyChangeListener() {

					/*
					 * (non-Javadoc)
					 */
					public void propertyChange(PropertyChangeEvent e) {
						updateLabels();
					}
				});

		// Installs a listener to update the file state
		editor.getModel().addTreeModelListener(new JGraphpadTreeModelAdapter() {

			/*
			 * (non-Javadoc)
			 */
			public void treeNodesInserted(TreeModelEvent e) {
				treeNodesChanged(e);
			}

			/*
			 * (non-Javadoc)
			 */
			public void treeNodesRemoved(TreeModelEvent e) {
				treeNodesChanged(e);
			}

			/*
			 * (non-Javadoc)
			 */
			public void treeNodesChanged(TreeModelEvent e) {
				updateLabels();
			}
		});
	}

	/**
	 * Shortcut method to {@link JGraphEditorResources#getString(String)}.
	 * 
	 * @param key
	 *            The key to return the resource string for.
	 */
	public static String getString(String key) {
		return JGraphEditorResources.getString(key);
	}

	/**
	 * Invoked by the various listeners to update the labels. This
	 * implementation does not update the mouse label as it is updated by other
	 * means.
	 */
	protected void updateLabels() {
		JGraphpadFocusManager focusedGraph = JGraphpadFocusManager
				.getCurrentGraphFocusManager();

		// Fetches the focused graph by first trying the permanent focus
		// owner and if that is null, the last focused graph.
		JGraph graph = focusedGraph.getFocusedGraph();
		if (graph != null) {

			// Updates the various graph-related labels
			// Shows selection or graph details in the info label
			if (graph.isSelectionEmpty())
				infoLabel.setText(graph.getModel().getRootCount() + " "
						+ getString("Roots"));
			else
				infoLabel.setText(graph.getSelectionCount() + " "
						+ getString("Selected"));

			// Shows the editable state and zoom level in the respective labels
			editableLabel.setText((graph.isEditable()) ? getString("Editable")
					: getString("ReadOnly"));
			zoomLabel.setText((int) (graph.getScale() * 100) + "%");

			// Updates the modified label (file state)
			JGraphEditorDiagramPane diagramPane = JGraphEditorDiagramPane
					.getParentDiagramPane(graph);
			if (diagramPane != null) {
				JGraphEditorFile file = JGraphEditorModel
						.getParentFile(diagramPane.getDiagram());
				if (file != null) { // may be removed from file
					modifiedLabel
							.setText((file.isModified()) ? getString("Modified")
									: (file.isNew()) ? getString("New")
											: getString("Saved"));
				}
			}
		}

		// Shows some memory usage details in the spare label
		Runtime runtime = Runtime.getRuntime();
		double freeMemory = runtime.freeMemory() / 1024;
		double totalMemory = runtime.totalMemory() / 1024;
		double usedMemory = totalMemory - freeMemory;

		spareLabel.setText(numberFormat.format(usedMemory)
				+ getString("KBFree") + " / "
				+ numberFormat.format(totalMemory) + getString("KBTotal"));
	}

	/**
	 * Provides a factory method to construct a status bar for an editor.
	 */
	public static class FactoryMethod extends JGraphEditorFactoryMethod {

		/**
		 * Defines the default name for factory methods of this kind.
		 */
		public static String NAME = "createStatusBar";

		/**
		 * References the enclosing editor.
		 */
		protected JGraphEditor editor;

		/**
		 * Constructs a new factory method for the specified enclosing editor
		 * using {@link #NAME}.
		 * 
		 * @param editor
		 *            The editor that contains the factory method.
		 */
		public FactoryMethod(JGraphEditor editor) {
			super(NAME);
			this.editor = editor;
		}

		/*
		 * (non-Javadoc)
		 */
		public Component createInstance(Node configuration) {
			return new JGraphpadStatusBar(editor);
		}

	}

}