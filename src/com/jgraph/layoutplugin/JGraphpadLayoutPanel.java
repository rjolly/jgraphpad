/* 
 * $Id: JGraphpadLayoutPanel.java,v 1.9 2009/02/12 23:42:08 david Exp $
 * Copyright (c) 2001-2005, Gaudenz Alder
 * 
 * All rights reserved.
 * 
 * See LICENSE file for license details. If you are unable to locate
 * this file please contact info (at) jgraph (dot) com.
 */
package com.jgraph.layoutplugin;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.ProgressMonitor;

import org.jgraph.JGraph;
import org.w3c.dom.Node;

import com.jgraph.JGraphEditor;
import com.jgraph.editor.JGraphEditorAction;
import com.jgraph.editor.JGraphEditorResources;
import com.jgraph.editor.factory.JGraphEditorFactoryMethod;
import com.jgraph.layout.JGraphCompoundLayout;
import com.jgraph.layout.JGraphFacade;
import com.jgraph.layout.JGraphLayout;
import com.jgraph.layout.JGraphLayoutProgress;
import com.jgraph.layout.graph.JGraphSimpleLayout;
import com.jgraph.layout.hierarchical.JGraphHierarchicalLayout;
import com.jgraph.layout.organic.JGraphFastOrganicLayout;
import com.jgraph.layout.organic.JGraphOrganicLayout;
import com.jgraph.layout.organic.JGraphSelfOrganizingOrganicLayout;
import com.jgraph.layout.tree.JGraphCompactTreeLayout;
import com.jgraph.layout.tree.JGraphRadialTreeLayout;
import com.jgraph.layout.tree.JGraphTreeLayout;
import com.jgraph.pad.dialog.JGraphpadDialogs;
import com.jgraph.pad.util.JGraphpadFocusManager;
import com.jgraph.pad.util.JGraphpadMorphingManager;
import com.l2fprod.common.propertysheet.PropertySheetPanel;

/**
 * Layout panel to configure and execute a set of layouts from JGraph Layout
 * Pro.
 */
public class JGraphpadLayoutPanel extends JPanel
{

	/**
	 * Enclosing editor that contains the panel.
	 */
	protected JGraphEditor editor;

	/**
	 * Array of layouts to choose from. Created using
	 * {@link #createChoosableLayouts()}.
	 */
	protected JGraphLayout[] layouts = createChoosableLayouts();

	/**
	 * Combo box for selecting the current layout.
	 */
	protected JComboBox layoutCombo = new JComboBox(layouts);

	/**
	 * Holds the morphing manager.
	 */
	protected JGraphpadMorphingManager morpher = new JGraphpadMorphingManager();

	/**
	 * Checkboxes to control the global settings.
	 */
	protected JCheckBox applyGridCheckBox, flushOriginCheckBox,
			ignoreDirectionsCheckBox, ignoreHiddenCellsCheckBox,
			ignoreUnconnectedNodesCheckBox, ignoreCellsInsideGroupsCheckBox,
			lockSelectionCellsCheckBox;

	/**
	 * Holds the execute button.
	 */
	protected JButton executeButton;

	/**
	 * Constructs a new layout panel for the specified editor.
	 * 
	 * @param editor
	 *            The enclosing editor for the panel.
	 */
	public JGraphpadLayoutPanel(JGraphEditor editor)
	{
		this.editor = editor;
		setLayout(new BorderLayout());
		setBorder(null);
		layoutCombo.setFocusable(false);
		add(createCurrentLayoutPanel(), BorderLayout.NORTH);

		// Adds the general and layout settings tabs
		JTabbedPane tabPane = editor.getFactory().createTabbedPane(
				JTabbedPane.TOP);
		tabPane.addTab(JGraphEditorResources.getString("General"), editor
				.getFactory().createScrollPane(createGeneralPanel()));
		tabPane.addTab(JGraphEditorResources.getString("Layout"),
				createLayoutSettingsPanel());

		add(tabPane, BorderLayout.CENTER);
	}

	/**
	 * Hook for subclassers to create or augment the array of choosable layouts.
	 * 
	 * @return Returns the array of choosable layouts.
	 */
	protected JGraphLayout[] createChoosableLayouts()
	{
		// Creates a sample compound layout
		JGraphFastOrganicLayout organicLayout = new JGraphFastOrganicLayout();
		organicLayout.setForceConstant(100);
		JGraphCompoundLayout compound = new JGraphCompoundLayout(
				new JGraphLayout[] { organicLayout, new JGraphOrganicLayout() })
		{
			public String toString()
			{
				return "Organic";
			}
		};

		// Returns all standard layouts plus the custom compound layout
		return new JGraphLayout[] {
				new JGraphCompactTreeLayout(),
				new JGraphTreeLayout(),
				new JGraphRadialTreeLayout(),
				new JGraphHierarchicalLayout(),
				new JGraphFastOrganicLayout(),
				new JGraphSelfOrganizingOrganicLayout(),
				new JGraphOrganicLayout(),
				new JGraphSimpleLayout(JGraphSimpleLayout.TYPE_CIRCLE),
				new JGraphSimpleLayout(JGraphSimpleLayout.TYPE_TILT, 40, 40),
				new JGraphSimpleLayout(JGraphSimpleLayout.TYPE_RANDOM, 640, 480),
				compound };
	}

	/**
	 * Returns the selection layout from the combo box.
	 * 
	 * @return The selection layout.
	 */
	protected JGraphLayout getSelectionLayout()
	{
		return (JGraphLayout) layoutCombo.getSelectedItem();
	}

	/**
	 * Returns the current focused graph from the focus manager.
	 * 
	 * @return The graph to execute the layout in.
	 */
	protected JGraph getFocusedGraph()
	{
		JGraphpadFocusManager manager = JGraphpadFocusManager
				.getCurrentGraphFocusManager();
		return manager.getFocusedGraph();
	}

	/**
	 * Executes the layout returned by {@link #getSelectionLayout()} on the
	 * graph returned by {@link #getFocusedGraph()} by creating a facade and
	 * progress monitor for the layout and invoking it's run method in a
	 * separate thread so this method call returns immediately. To display the
	 * result of the layout algorithm a {@link JGraphpadMorphingManager} is
	 * used.
	 */
	public void execute()
	{
		final JGraphLayout layout = getSelectionLayout();
		final JGraph graph = getFocusedGraph();

		if (graph != null && graph.isEnabled() && graph.isMoveable()
				&& layout != null)
		{
			final JGraphFacade facade = createFacade(graph, layout);

			final ProgressMonitor progressMonitor = (layout instanceof JGraphLayout.Stoppable) ? createProgressMonitor(
					graph, (JGraphLayout.Stoppable) layout)
					: null;
			new Thread()
			{
				public void run()
				{
					synchronized (this)
					{
						try
						{
							// Executes the layout and checks if the user has
							// clicked
							// on cancel during the layout run. If no progress
							// monitor
							// has been displayed or cancel has not been pressed
							// then
							// the result of the layout algorithm is processed.
							layout.run(facade);
							boolean ignoreResult = false;
							if (progressMonitor != null)
							{
								ignoreResult = progressMonitor.isCanceled();
								progressMonitor.close();
							}
							if (!ignoreResult)
							{

								// Processes the result of the layout algorithm
								// by creating a nested map based on the global
								// settings and passing the map to a morpher
								// for the graph that should be changed.
								// The morpher will animate the change and then
								// invoke the edit method on the graph layout
								// cache.
								Map map = facade
										.createNestedMap(!applyGridCheckBox
												.isSelected(),
												(flushOriginCheckBox
														.isSelected()) ? facade
														.getGraphOrigin()
														: null);

								morpher.morph(graph, map);
								graph.requestFocus();
							}
						}
						catch (Exception e)
						{
							JGraphpadDialogs.getSharedInstance()
									.errorDialog(
											JGraphEditorAction
													.getPermanentFocusOwner(),
											e.getMessage());
						}
					}
				}
			}.start(); // fork
		}
	}

	/**
	 * Creates a {@link LockedSelectionFacade} and makes sure it contains a
	 * valid set of root cells if the specified layout is a tree layout. A root
	 * cell in this context is one that has no incoming edges.
	 * 
	 * @param graph
	 *            The graph to use for the facade.
	 * @param layout
	 *            The layout to create the facade for.
	 * @return Returns a new facade for the specified layout and graph.
	 */
	protected JGraphFacade createFacade(JGraph graph, JGraphLayout layout)
	{
		// Creates and configures the facade using the global switches
		JGraphFacade facade = new LockedSelectionFacade(graph, graph
				.getSelectionCells());
		facade.setIgnoresUnconnectedCells(ignoreUnconnectedNodesCheckBox
				.isSelected());
		facade.setIgnoresCellsInGroups(ignoreCellsInsideGroupsCheckBox
				.isSelected());
		facade.setIgnoresHiddenCells(ignoreHiddenCellsCheckBox.isSelected());
		facade.setDirected(!ignoreDirectionsCheckBox.isSelected());

		// Note: The enclosing class is not available at construction time,
		// so this call *must* go here:
		facade.resetControlPoints();
		return facade;
	}

	/**
	 * Creates a {@link LayoutProgressMonitor} for the specified layout.
	 * 
	 * @param graph
	 *            The graph to use as the parent component.
	 * @param layout
	 *            The layout to create the progress monitor for.
	 * @return Returns a new progress monitor.
	 */
	protected ProgressMonitor createProgressMonitor(JGraph graph,
			JGraphLayout.Stoppable layout)
	{
		ProgressMonitor monitor = new LayoutProgressMonitor(graph,
				((JGraphLayout.Stoppable) layout).getProgress(),
				JGraphEditorResources.getString("PerformingLayout"));
		monitor.setMillisToDecideToPopup(100);
		monitor.setMillisToPopup(500);
		return monitor;
	}

	/**
	 * Returns a panel for selecting and executing the current layout.
	 */
	protected JPanel createCurrentLayoutPanel()
	{
		JPanel panel = new JPanel();

		panel.setLayout(new GridBagLayout());
		panel.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));

		// Adds a title and the inner panel
		JLabel currentLayoutLabel = new JLabel(JGraphEditorResources
				.getString("CurrentLayout")
				+ ":");
		currentLayoutLabel.setFont(currentLayoutLabel.getFont().deriveFont(
				Font.BOLD));
		currentLayoutLabel.setBorder(BorderFactory
				.createEmptyBorder(2, 2, 4, 2));

		// Adds current layout title
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
		panel.add(currentLayoutLabel, c);

		// Adds layout combo
		c.gridx = 0;
		c.gridy = 1;
		c.weightx = 0.9;
		panel.add(layoutCombo, c);

		// Adds execute button
		c.gridx = 1;
		c.gridy = 1;
		c.weightx = 0.1;
		c.insets = new Insets(0, 10, 0, 0);
		c.ipadx = 1;
		executeButton = new JButton(JGraphEditorResources.getString("Execute"));
		executeButton.setFocusable(false);
		executeButton.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				execute();
			}
		});
		panel.add(executeButton, c);

		return panel;
	}

	/**
	 * Returns a panel for configuring global layout switches.
	 */
	protected JPanel createGeneralPanel()
	{
		JPanel panel = new JPanel();
		panel.setLayout(new GridBagLayout());
		panel.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));

		GridBagConstraints c = new GridBagConstraints();
		c.anchor = GridBagConstraints.NORTHWEST;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.ipadx = 4;
		c.ipady = 2;
		c.weighty = 0.0;
		c.weightx = 1.0;
		c.gridwidth = 1;
		c.gridx = 0;

		c.ipady = 4;
		c.gridy = 0;
		c.gridx = 0;
		JLabel settingsLabel = new JLabel(JGraphEditorResources
				.getString("GlobalSettings")
				+ ":");
		settingsLabel.setFont(settingsLabel.getFont().deriveFont(Font.BOLD));
		panel.add(settingsLabel, c);

		c.gridy = 0;
		c.gridx = 1;
		JLabel ignoreCellsLabel = new JLabel(JGraphEditorResources
				.getString("IgnoreCells")
				+ ":");
		ignoreCellsLabel.setFont(settingsLabel.getFont().deriveFont(Font.BOLD));
		panel.add(ignoreCellsLabel, c);

		c.gridy = 1;
		c.gridx = 0;
		flushOriginCheckBox = new JCheckBox(JGraphEditorResources
				.getString("MoveResultToOrigin"), true);
		flushOriginCheckBox.setFocusable(false);
		panel.add(flushOriginCheckBox, c);

		c.gridy = 2;
		c.gridx = 0;
		applyGridCheckBox = new JCheckBox(JGraphEditorResources
				.getString("ApplyGridToResult"));
		applyGridCheckBox.setFocusable(false);
		panel.add(applyGridCheckBox, c);

		c.gridy = 3;
		c.gridx = 0;
		ignoreDirectionsCheckBox = new JCheckBox(JGraphEditorResources
				.getString("IgnoreEdgeDirections"));
		ignoreDirectionsCheckBox.setFocusable(false);
		panel.add(ignoreDirectionsCheckBox, c);

		c.gridy = 4;
		c.gridx = 0;
		c.weighty = 1.0;
		lockSelectionCellsCheckBox = new JCheckBox(JGraphEditorResources
				.getString("LockSelectionCells"));
		lockSelectionCellsCheckBox.setFocusable(false);
		panel.add(lockSelectionCellsCheckBox, c);

		c.ipady = 2;
		c.gridy = 1;
		c.gridx = 1;
		c.weighty = 0.0;
		ignoreUnconnectedNodesCheckBox = new JCheckBox(JGraphEditorResources
				.getString("UnconnectedNodes"), true);
		ignoreUnconnectedNodesCheckBox.setFocusable(false);
		panel.add(ignoreUnconnectedNodesCheckBox, c);

		c.gridy = 2;
		c.gridx = 1;
		ignoreHiddenCellsCheckBox = new JCheckBox(JGraphEditorResources
				.getString("HiddenCells"), true);
		ignoreHiddenCellsCheckBox.setFocusable(false);
		panel.add(ignoreHiddenCellsCheckBox, c);

		c.gridy = 3;
		c.gridx = 1;
		c.weighty = 0.0;
		ignoreCellsInsideGroupsCheckBox = new JCheckBox(JGraphEditorResources
				.getString("CellsInsideGroups"));
		ignoreCellsInsideGroupsCheckBox.setFocusable(false);
		panel.add(ignoreCellsInsideGroupsCheckBox, c);

		return panel;
	}

	/**
	 * Returns a panel for configuring the selection layout.
	 */
	protected JPanel createLayoutSettingsPanel()
	{
		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout());
		panel.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));

		// Adds property sheet
		final PropertySheetPanel propertySheet = new PropertySheetPanel();
		propertySheet.setSortingProperties(true);
		propertySheet.setBorder(null);
		propertySheet.setMinimumSize(new Dimension(0, 0));
		propertySheet.setPreferredSize(new Dimension(0, 0));
		panel.add(editor.getFactory().createScrollPane(propertySheet),
				BorderLayout.CENTER);

		// Creates the listener that is going to be used
		// to wait for updates on the property sheet.
		final PropertyChangeListener propertySheetListener = new PropertyChangeListener()
		{
			public void propertyChange(PropertyChangeEvent evt)
			{
				JGraphLayout layout = (JGraphLayout) layoutCombo
						.getSelectedItem();
				propertySheet.writeToObject(layout);
			}
		};

		// Listens to selections in the layout combo
		layoutCombo.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				JGraphLayout layout = (JGraphLayout) layoutCombo
						.getSelectedItem();
				try
				{

					BeanInfo info = Introspector.getBeanInfo(layout.getClass());
					propertySheet
							.removePropertySheetChangeListener(propertySheetListener);
					propertySheet.setBeanInfo(info);
					propertySheet.readFromObject(layout);
					propertySheet.invalidate();

				}
				catch (IntrospectionException e1)
				{
					// ignore
				}

				// Makes sure the property sheet has a listener
				finally
				{
					propertySheet
							.addPropertySheetChangeListener(propertySheetListener);
				}
			}
		});

		// Select the first item by default
		layoutCombo.setSelectedIndex(0);

		return panel;
	}

	/**
	 * Utility facade to lock selection cells if the
	 * {@link JGraphpadLayoutPanel#lockSelectionCellsCheckBox} is selected.
	 */
	public class LockedSelectionFacade extends JGraphFacade
	{

		/**
		 * Holds the graph reference.
		 */
		protected JGraph graph;

		/**
		 * Constructs a locked selection facade for the specified graph and
		 * roots.
		 * 
		 * @param graph
		 *            The graph to create the facade for.
		 * @param roots
		 *            The array of roots to use in the facade.
		 */
		public LockedSelectionFacade(JGraph graph, Object[] roots)
		{
			super(graph, roots);
			this.graph = graph;
		}

		/**
		 * Returns false if the
		 * {@link JGraphpadLayoutPanel#lockSelectionCellsCheckBox} and the cell
		 * are selected.
		 * 
		 * @param cell
		 *            The cell to be checked.
		 * @return Returns true if <code>cell</code> is moveable.
		 */
		public boolean isMoveable(Object cell)
		{
			return (!lockSelectionCellsCheckBox.isSelected() || !graph
					.isCellSelected(cell))
					&& super.isMoveable(cell);
		}

	}

	/**
	 * Utility progress monitor for a layout progress object. Implements a
	 * property change listener to update itself based on the running layout.
	 * The listener is added to the progress object in the constructor and
	 * removed from it when the close method is called.
	 * 
	 */
	public static class LayoutProgressMonitor extends ProgressMonitor implements
			PropertyChangeListener
	{

		/**
		 * References the progress being monitored.
		 */
		protected JGraphLayoutProgress progress;

		/**
		 * Constructs a new progress monitor for the specified progress object.
		 * 
		 * @param component
		 *            The parent component to use for the dialog.
		 * @param progress
		 *            The progress object to be monitored.
		 * @param message
		 *            The message to display.
		 */
		public LayoutProgressMonitor(Component component,
				JGraphLayoutProgress progress, String message)
		{
			super(component, message, "", 0, 100);
			this.progress = progress;
			progress.addPropertyChangeListener(this);
		}

		/*
		 * (non-Javadoc)
		 */
		public void propertyChange(PropertyChangeEvent evt)
		{
			if (evt.getPropertyName().equals(
					JGraphLayoutProgress.PROGRESS_PROPERTY))
			{
				int newValue = Integer.parseInt(String.valueOf(evt
						.getNewValue()));
				setProgress(newValue);
			}

			// Updates the maximum property. This is set after the layout
			// run method has been called.
			else if (evt.getPropertyName().equals(
					JGraphLayoutProgress.MAXIMUM_PROPERTY))
			{
				int newValue = Integer.parseInt(String.valueOf(evt
						.getNewValue()));
				setMaximum(newValue);
			}

			// Checks isCancelled and stops the layout if it has been pressed
			if (isCanceled())
				progress.setStopped(true);
		}

		/**
		 * Overrides the parent's implementation to remove the property change
		 * listener from the progress.
		 */
		public void close()
		{
			progress.removePropertyChangeListener(this);
			super.close();
		}

	}

	/**
	 * Provides a factory method to construct a layout panel for an editor.
	 */
	public static class FactoryMethod extends JGraphEditorFactoryMethod
	{

		/**
		 * Defines the default name for factory methods of this kind.
		 */
		public static String NAME = "createLayoutPanel";

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
		public FactoryMethod(JGraphEditor editor)
		{
			super(NAME);
			this.editor = editor;
		}

		/*
		 * (non-Javadoc)
		 */
		public Component createInstance(Node configuration)
		{

			JGraphpadLayoutPanel panel = null;

			try
			{
				panel = new JGraphpadLayoutPanel(editor);
				JGraphEditorAction action = editor.getKit().getAction(
						JGraphpadLayoutAction.NAME_LAYOUT);

				// Links the existing action with the panel
				if (action instanceof JGraphpadLayoutAction)
					((JGraphpadLayoutAction) action).setLayoutPanel(panel);
			}
			catch (Throwable e)
			{
				// ignore
			}

			return panel;
		}

	}

}
