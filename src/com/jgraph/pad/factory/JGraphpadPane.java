/* 
 * $Id: JGraphpadPane.java,v 1.11 2008/11/18 15:24:50 david Exp $
 * Copyright (c) 2001-2005, Gaudenz Alder
 * 
 * All rights reserved.
 * 
 * See LICENSE file for license details. If you are unable to locate
 * this file please contact info (at) jgraph (dot) com.
 */
package com.jgraph.pad.factory;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.GridLayout;
import java.awt.KeyboardFocusManager;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;

import javax.swing.ImageIcon;
import javax.swing.JDesktopPane;
import javax.swing.JFrame;
import javax.swing.JInternalFrame;
import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JToolBar;
import javax.swing.SwingUtilities;
import javax.swing.event.InternalFrameAdapter;
import javax.swing.event.InternalFrameEvent;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreeNode;

import org.jgraph.JGraph;
import org.w3c.dom.Node;

import com.jgraph.JGraphEditor;
import com.jgraph.JGraphpad;
import com.jgraph.editor.JGraphEditorAction;
import com.jgraph.editor.JGraphEditorDiagram;
import com.jgraph.editor.JGraphEditorFactory;
import com.jgraph.editor.JGraphEditorFile;
import com.jgraph.editor.JGraphEditorModel;
import com.jgraph.editor.JGraphEditorResources;
import com.jgraph.editor.JGraphEditorSettings;
import com.jgraph.editor.factory.JGraphEditorDiagramPane;
import com.jgraph.editor.factory.JGraphEditorFactoryMethod;
import com.jgraph.editor.factory.JGraphEditorNavigator;
import com.jgraph.editor.factory.JGraphEditorToolbox;
import com.jgraph.pad.JGraphpadFile;
import com.jgraph.pad.JGraphpadLibrary;
import com.jgraph.pad.action.JGraphpadFileAction;
import com.jgraph.pad.util.JGraphpadFocusManager;
import com.jgraph.pad.util.JGraphpadImageIcon;
import com.jgraph.pad.util.JGraphpadMouseAdapter;
import com.jgraph.pad.util.JGraphpadTreeModelAdapter;

/**
 * Main application panel consisting of a menubar and toolbar, two tabbed panes,
 * one on the left and one on the bottom, a desktop pane in the center, and a
 * status bar.
 */
public class JGraphpadPane extends JPanel {

	/**
	 * Nodename to be used for the desktop popup menu configuration.
	 */
	public static String NODENAME_DESKTOPPOPUPMENU = "desktoppopupmenu";

	/**
	 * Nodename to be used for the desktop popup menu configuration.
	 */
	public static String NODENAME_INTERNALFRAMEPOPUPMENU = "internalframepopupmenu";

	/**
	 * Defines the key used to identify the navigator split divider location.
	 */
	public static String KEY_DESKTOPPANE = "desktopPane";

	/**
	 * Defines the key used to identify the left split divider location.
	 */
	public static String KEY_LEFTSPLIT = "leftSplit";

	/**
	 * Defines the key used to identify the right split divider location.
	 * setting.
	 */
	public static String KEY_RIGHTSPLIT = "rightSplit";

	/**
	 * Defines the key used to identify the right split divider location.
	 * setting.
	 */
	public static String KEY_NAVIGATORSPLIT = "navigatorSplit";

	/**
	 * Holds the desktop pane.
	 */
	protected JDesktopPane desktopPane = new JDesktopPane();

	/**
	 * Maps from diagrams to internal frames.
	 */
	protected Map internalFrames = new HashMap();

	/**
	 * References the enclosing editor.
	 */
	protected JGraphEditor editor;

	/**
	 * Constructs a new editor pane for the specified enclosing editor.
	 * 
	 * @param editor
	 *            The editor that contains the pane.
	 */
	public JGraphpadPane(JGraphEditor editor) {
		setLayout(new BorderLayout());
		this.editor = editor;
		desktopPane.addMouseListener(new JGraphpadMouseAdapter(editor,
				NODENAME_DESKTOPPOPUPMENU));

		// Stores a reference to the desktop pane in the editor
		// settings for later wiring by the createApplication code.
		editor.getSettings().putObject(KEY_DESKTOPPANE, desktopPane);

		// Creates the left and bottom tab components using the respective
		// factory methods. This way, plugins can add new tabs to the main
		// window by replacing/overriding these factory methods.
		Component bottomTab = editor.getFactory().executeMethod(
				BottomTabFactoryMethod.NAME);

		// Constructs the split panes and registers them for restoring
		// the divider locations. The restoring of the locations
		// is only possible when the split panes are visible.
		JSplitPane rightSplit = editor.getFactory().createSplitPane(
				desktopPane, bottomTab, JSplitPane.VERTICAL_SPLIT);
		rightSplit.setOneTouchExpandable(true);
		rightSplit.setResizeWeight(1.0);
		editor.getSettings().putObject(KEY_RIGHTSPLIT, rightSplit);

		// Constructs the left split pane and adds the left tab created
		// with the factory method and the right (inner) split with
		// the bottom tab and desktop pane.
		if (JGraphpad.INNER_LIBRARIES) {
			add(rightSplit, BorderLayout.CENTER);
		} else {
			Component leftTab = editor.getFactory().executeMethod(
					LeftTabFactoryMethod.NAME);
			JSplitPane leftSplit = editor.getFactory().createSplitPane(leftTab,
					rightSplit, JSplitPane.HORIZONTAL_SPLIT);
			leftSplit.setOneTouchExpandable(true);
			editor.getSettings().putObject(KEY_LEFTSPLIT, leftSplit);
			add(leftSplit, BorderLayout.CENTER);
		}

		// Adds a shutdown hook to the settings to store the divider
		// locations when the program terminates.
		editor.getSettings().addShutdownHook(
				new JGraphEditorSettings.ShutdownHook() {

					// Takes the window bounds and stores the into the in-core
					// user configuration, which is later saved to disk.
					public void shutdown() {
						JGraphpadPane.this.editor.getSettings().storeSplitPane(
								JGraphpad.NAME_USERSETTINGS, KEY_LEFTSPLIT);
						JGraphpadPane.this.editor.getSettings().storeSplitPane(
								JGraphpad.NAME_USERSETTINGS, KEY_RIGHTSPLIT);
					}
				});
	}

	/**
	 * Adds an internal frame for the specified file to the desktop pane. The
	 * {@link #getFileTitle(JGraphEditorFile)} method is used to determine the
	 * title for the internal frame.
	 * 
	 * @param file
	 *            The file to be added.
	 */
	public void addFile(JGraphEditorFile file) {
		JInternalFrame internalFrame = (JInternalFrame) internalFrames
				.get(file);

		// Creates the internal frame for the file if the frame does not yet
		// exist. Else this method does nothing.
		if (internalFrame == null) {
			internalFrame = createInternalFrame(getFileTitle(file));

			// Associates the internal frame with the file and
			// adds it to the desktop pane.
			internalFrames.put(file, internalFrame);
			desktopPane.add(internalFrame);
			
			// Adds a tabbed pane to hold the diagrams in the file
			final JTabbedPane tabbedPane = editor.getFactory()
					.createTabbedPane(JTabbedPane.BOTTOM);

			// Adds the libraries and navigator into the document
			if (JGraphpad.INNER_LIBRARIES) {
				JTabbedPane leftTab = (JTabbedPane) editor.getFactory()
						.executeMethod(LeftTabFactoryMethod.NAME);

				final JGraphEditorNavigator navigator = (JGraphEditorNavigator) ((Container) leftTab
						.getComponent(0)).getComponent(0);
				final JInternalFrame frame = internalFrame;

				// Updates the current graph in the navigator based on the
				// global focus traversal.
				JGraphpadFocusManager focusedGraph = JGraphpadFocusManager
						.getCurrentGraphFocusManager();
				focusedGraph
						.addPropertyChangeListener(new PropertyChangeListener() {
							public void propertyChange(PropertyChangeEvent e) {
								String prop = e.getPropertyName();
								if (JGraphpadFocusManager.FOCUSED_GRAPH_PROPERTY
										.equals(prop)) {

									JGraph graph = (JGraph) e.getNewValue();

									if (frame.isAncestorOf(graph)) {
										navigator.setCurrentGraph((JGraph) e
												.getNewValue());
									}
								}
							}
						});

				JSplitPane leftSplit = editor.getFactory().createSplitPane(
						leftTab, tabbedPane, JSplitPane.HORIZONTAL_SPLIT);
				leftSplit.setOneTouchExpandable(true);

				internalFrame.getContentPane().add(leftSplit,
						BorderLayout.CENTER);
				
				// Adds child libs
				JTabbedPane libraryTabs = (JTabbedPane) ((Container) leftTab
						.getComponent(0)).getComponent(1);

				int childCount = editor.getModel().getChildCount(file);
				for (int i=0; i<childCount; i++)
				{
					Object child = editor.getModel().getChild(file, i);
					if (child instanceof JGraphpadLibrary)
					{
						JGraphpadLibrary library = (JGraphpadLibrary) child;
						final JGraphpadLibraryPane libraryPane = new JGraphpadLibraryPane(
								editor, library);
						Component pane = editor.getFactory().createScrollPane(
								libraryPane);
						libraryTabs.addTab(library.toString(), pane);
						libraryTabs.setSelectedComponent(pane);
					}
				}
			} else {
				internalFrame.getContentPane().add(tabbedPane,
						BorderLayout.CENTER);
			}

			tabbedPane.addMouseListener(new JGraphpadMouseAdapter(editor,
					NODENAME_INTERNALFRAMEPOPUPMENU));

			// Installs a listener to react to clicks on the close icon.
			// The close action will be executed and the internal frame
			// will be removed if the document is removed from the model.
			internalFrame.addInternalFrameListener(new InternalFrameAdapter() {
				public void internalFrameClosing(InternalFrameEvent e) {
					JInternalFrame frame = e.getInternalFrame();
					frame
							.setDefaultCloseOperation(JInternalFrame.DO_NOTHING_ON_CLOSE);
					try {
						JGraphEditorAction closeAction = editor.getKit()
								.getAction(JGraphpadFileAction.NAME_CLOSE);
						if (closeAction != null) {

							// Requests the focus to the graph to make sure
							// the correct file is closed by the action. This
							// is because the action finds the current file
							// based on the focus.
							((JGraphEditorDiagramPane) tabbedPane
									.getSelectedComponent()).getGraph()
									.requestFocus();
							SwingUtilities.invokeLater(new Runnable() {
								public void run() {
									editor.getKit().getAction("close")
											.actionPerformed(null);
								}
							});
						}
					} catch (Exception ex) {
						// don't dispose
					}
				}
			});
		}
	}

	/**
	 * Hook method to create an internal frame
	 * @param title the title of the frame
	 * @return the internal frame created
	 */
	public JInternalFrame createInternalFrame(String title) {
		JInternalFrame internalFrame = new JInternalFrame(title, true, true,
				true, true);

		internalFrame.setBounds(0, 0, 600, 280);
		internalFrame.setVisible(true);
		internalFrame.setFocusable(false);
		internalFrame.getContentPane().setLayout(new BorderLayout());
		return internalFrame;
	}
	
	public JInternalFrame getInternalFrame(Object file) {
		return (JInternalFrame) internalFrames.get(file);
	}

	/**
	 * Removes the internal frame for the specified file from the desktop pane.
	 * 
	 * @param file
	 *            The file to be removed.
	 */
	public void removeFile(JGraphEditorFile file) {
		JInternalFrame internalFrame = (JInternalFrame) internalFrames
				.remove(file);
		if (internalFrame != null) {
			desktopPane.remove(internalFrame);
			internalFrame.dispose();
			desktopPane.repaint();
		}
	}

	/**
	 * Updates the internal frame title for the specified file. The
	 * {@link #getFileTitle(JGraphEditorFile)} method is used to determine the
	 * title for the internal frame.
	 * 
	 * @param file
	 *            The file who's internal frame should be updated.
	 */
	public void updateFileTitle(JGraphEditorFile file) {
		JInternalFrame internalFrame = (JInternalFrame) internalFrames
				.get(file);
		if (internalFrame != null)
			internalFrame.setTitle(getFileTitle(file));
	}
	
	protected JTabbedPane getDiagramContainer(JInternalFrame frame)
	{
		JTabbedPane tabbedPane = null;
		if (JGraphpad.INNER_LIBRARIES) {
			tabbedPane = (JTabbedPane) ((Container) frame
					.getContentPane().getComponent(0)).getComponent(1);
		} else {
			tabbedPane = (JTabbedPane) frame.getContentPane()
					.getComponent(0);
		}
		return tabbedPane;
	}

	/**
	 * Creates a diagram pane using
	 * {@link JGraphEditorFactory#createDiagramPane(JGraphEditorDiagram)},
	 * configures it using
	 * {@link #configureDiagramPane(JGraphEditorDiagramPane, JGraphEditorDiagram)}
	 * and adds it the the tabbed pane of the internal frame for the parent
	 * file. The {@link JGraphEditorDiagram#getName()} method is used to set the
	 * tab's title.
	 * 
	 * @param diagram
	 *            The diagram to be added.
	 */
	public void addDiagram(JGraphEditorDiagram diagram) {
		JGraphEditorFile file = JGraphEditorModel.getParentFile(diagram);
		JInternalFrame internalFrame = (JInternalFrame) internalFrames
				.get(file);
		if (internalFrame != null) {
			final JGraphEditorDiagramPane diagramPane = editor.getFactory()
					.createDiagramPane(diagram);
			configureDiagramPane(diagramPane, diagram);

			// Adds the new diagram pane to the tabbed pane of
			// the parent's internal frame and selects the new tab.
			JTabbedPane tabbedPane = getDiagramContainer(internalFrame);
			tabbedPane.addTab(diagram.getName(), diagramPane);
			tabbedPane.setSelectedComponent(diagramPane);

			// Transfers the focus to the new graph in the diagram pane
			// after the component hierarchy has been revalidated.
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					diagramPane.getGraph().requestFocus();
				}
			});
		}
	}

	/**
	 * Adds the specified library to the library pane.
	 * 
	 * @param library
	 *            The library to be added.
	 */
	public void addLibrary(JGraphpadLibrary library) {
		// TODO
	}

	/**
	 * Configures the newly created diagram pane to reflect the properties of
	 * the specified diagram. This also installs a listener to keep the
	 * properties of the diagram up-to-date with the graph for the next creation
	 * after persistence.
	 * 
	 * @param diagramPane
	 *            The diagram pane to be configured.
	 * @param diagram
	 *            The diagram to be configured.
	 */
	protected void configureDiagramPane(JGraphEditorDiagramPane diagramPane,
			final JGraphEditorDiagram diagram) {

		// Listens to JGraph properties
		diagramPane.getGraph().addPropertyChangeListener(
				new PropertyChangeListener() {

					/*
					 * (non-Javadoc)
					 */
					public void propertyChange(PropertyChangeEvent event) {

						// Checks if it's an interesting property and stores
						// the interesting ones back in the diagram.
						String name = event.getPropertyName();
						if (name.equals(JGraph.ANTIALIASED_PROPERTY)
								|| name.equals(JGraph.EDITABLE_PROPERTY)
								|| name.equals(JGraph.GRID_COLOR_PROPERTY)
								|| name.equals(JGraph.GRID_SIZE_PROPERTY)
								|| name.equals(JGraph.GRID_VISIBLE_PROPERTY)
								|| name.equals(JGraph.HANDLE_COLOR_PROPERTY)
								|| name.equals(JGraph.HANDLE_SIZE_PROPERTY)
								|| name
										.equals(JGraph.LOCKED_HANDLE_COLOR_PROPERTY)
								|| name.equals(JGraph.PORTS_VISIBLE_PROPERTY)
								|| name.equals(JGraph.PORTS_SCALED_PROPERTY)
								|| name.equals(JGraph.SCALE_PROPERTY))
							if (event.getNewValue() == null)
								diagram.getProperties().remove("graph." + name);
							else
								diagram.getProperties().put("graph." + name,
										event.getNewValue());
					}
				});

		// Listens to diagram pane properties
		diagramPane.addPropertyChangeListener(new PropertyChangeListener() {

			/*
			 * (non-Javadoc)
			 */
			public void propertyChange(PropertyChangeEvent event) {

				// Checks if it's an interesting property and stores
				// the interesting ones back in the diagram.
				String name = event.getPropertyName();
				if (name
						.equals(JGraphEditorDiagramPane.PROPERTY_AUTOSCALEPOLICY)
						|| name
								.equals(JGraphEditorDiagramPane.PROPERTY_BACKGROUNDIMAGE)
						|| name.equals(JGraphEditorDiagramPane.PROPERTY_METRIC)
						|| name
								.equals(JGraphEditorDiagramPane.PROPERTY_PAGEFORMAT)
						|| name
								.equals(JGraphEditorDiagramPane.PROPERTY_PAGESCALE)
						|| name
								.equals(JGraphEditorDiagramPane.PROPERTY_PAGEVISIBLE)
						|| name
								.equals(JGraphEditorDiagramPane.PROPERTY_RULERSVISIBLE))
					if (event.getNewValue() == null)
						diagram.getProperties().remove("diagramPane." + name);
					else
						diagram.getProperties().put("diagramPane." + name,
								event.getNewValue());
			}
		});

		// Tries to set all current properties from the diagram to the graph.
		// Note: A hashtable contains the object to resolve the prefixes.
		Map objects = new Hashtable();
		objects.put("diagramPane", diagramPane);
		objects.put("graph", diagramPane.getGraph());

		Iterator it = diagram.getProperties().entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry entry = (Map.Entry) it.next();
			setProperty(objects, String.valueOf(entry.getKey()), entry
					.getValue());
		}
	}

	/**
	 * Utility method to set the property of an object using reflection.
	 * 
	 * @param objects
	 *            Maps from prefixes to objects.
	 * @param property
	 *            The name of the property to be changed.
	 * @param value
	 *            The value of the property to be set.
	 */
	public static void setProperty(Map objects, String property, Object value) {
		// Analyze prefix
		int delim = property.indexOf('.');
		if (delim > 0) {
			String prefix = property.substring(0, delim);
			property = property.substring(delim + 1);
			Object obj = objects.get(prefix);
			if (obj != null) {
				// Does type conversion to basic types
				Class clazz = value.getClass();
				if (clazz == Boolean.class)
					clazz = boolean.class;
				else if (clazz == Integer.class)
					clazz = int.class;
				else if (clazz == Long.class)
					clazz = long.class;
				else if (clazz == Float.class)
					clazz = float.class;
				else if (clazz == Double.class)
					clazz = double.class;
				else if (clazz == JGraphpadImageIcon.class)
					clazz = ImageIcon.class;
				String name = String.valueOf(property);
				name = name.substring(0, 1).toUpperCase() + name.substring(1);
				try {
					Method setter = obj.getClass().getMethod("set" + name,
							new Class[] { clazz });
					setter.invoke(obj, new Object[] { value });
				} catch (Exception e) {
					// ignore
				}
			}
		}
	}

	/**
	 * Removes the diagram pane for the specified diagram from the tabbed pane
	 * in the previous parent's internal frame.
	 * 
	 * @param previousParent
	 *            The previous parent file of the removed diagram.
	 * @param diagram
	 *            The diagram to be removed.
	 */
	public void removeDiagram(JGraphEditorFile previousParent,
			JGraphEditorDiagram diagram) {
		JInternalFrame internalFrame = (JInternalFrame) internalFrames
				.get(previousParent);
		if (internalFrame != null) {
			JTabbedPane tabbedPane = getDiagramContainer(internalFrame);

			// Loops through all tabs of the tabbed pane to find the
			// correct tab. This assumes the diagram pane was added
			// as a tab directly, with no decorator components.
			for (int i = 0; i < tabbedPane.getTabCount(); i++) {
				Component tab = tabbedPane.getComponentAt(i);
				if (tab instanceof JGraphEditorDiagramPane) {
					JGraphEditorDiagramPane pane = (JGraphEditorDiagramPane) tab;
					if (pane.getDiagram() == diagram) {
						tabbedPane.removeTabAt(i);
						continue; // exit for
					}
				}
			}
			// Transfers the focus to the graph in the selected diagram
			// pane after the component hierarchy has been revalidated.
			Component tab = tabbedPane.getSelectedComponent();
			if (tab instanceof JGraphEditorDiagramPane) {
				final JGraphEditorDiagramPane pane = (JGraphEditorDiagramPane) tab;
				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						pane.getGraph().requestFocusInWindow();
					}
				});
			}
		}
	}

	/**
	 * Updates the tab title for the specified diagram in the tabbed pane of the
	 * parent's internal frame. The {@link JGraphEditorDiagram#getName()} method
	 * is used to set the tab's title.
	 * 
	 * @param diagram
	 *            The diagram who's tab should be updated.
	 */
	public void updateDiagramTitle(JGraphEditorDiagram diagram) {
		JGraphEditorFile file = JGraphEditorModel.getParentFile(diagram);
		JInternalFrame internalFrame = (JInternalFrame) internalFrames
				.get(file);
		if (internalFrame != null) {
			JTabbedPane tabbedPane = getDiagramContainer(internalFrame);

			// Loops through all tabs of the tabbed pane to find the
			// correct tab. This assumes the diagram pane was added
			// as a tab directly, with no decorator components.
			for (int i = 0; i < tabbedPane.getTabCount(); i++) {
				Component tab = tabbedPane.getComponentAt(i);
				if (tab instanceof JGraphEditorDiagramPane) {
					JGraphEditorDiagramPane pane = (JGraphEditorDiagramPane) tab;
					if (pane.getDiagram() == diagram) {
						tabbedPane.setTitleAt(i, diagram.getName());
						continue; // exit for
					}
				}
			}
		}
	}

	/**
	 * Hook for subclassers to return the internal frame title for a file. This
	 * implementation returns <code>String.valueOf(file)</code> appending an
	 * asterisk (*) if {@link JGraphEditorFile#isModified()} returns true.
	 * 
	 * @param file
	 *            The file to return the title for.
	 * @return Return the title for <code>file</code>.
	 */
	protected String getFileTitle(JGraphEditorFile file) {
		String title = String.valueOf(file);
		if (file.isModified())
			title += " *";
		return title;
	}

	/**
	 * Utility class to establish a listener in a editor's document model and
	 * update an editor pane.
	 */
	public static class DocumentTracker extends JGraphpadTreeModelAdapter {

		/**
		 * References the editor pane to be updated.
		 */
		protected JGraphpadPane pane;

		/**
		 * Constructs a new diagram tracker for updating the specified pane. The
		 * diagram tracker must be added as a tree model listener to an editor's
		 * document model.
		 * 
		 * @param pane
		 *            The pane to be updated on document model changes.
		 */
		public DocumentTracker(JGraphpadPane pane) {
			this.pane = pane;
		}

		/**
		 * Calls {@link #treeNodeInserted(TreeModel, Object)} with the last path
		 * component of {@link TreeModelEvent#getTreePath()} as the root of the
		 * resursion.
		 * 
		 * @param arg0
		 *            The object that describes the event.
		 */
		public void treeNodesInserted(TreeModelEvent arg0) {
			TreeModel source = (TreeModel) arg0.getSource();
			Object[] children = arg0.getChildren();
			for (int i = 0; i < children.length; i++)
				treeNodeInserted(source, children[i]);
		}

		/**
		 * Calls {@link JGraphpadPane#addFile(JGraphEditorFile)} or
		 * {@link JGraphpadPane#addDiagram(JGraphEditorDiagram)} recursively on
		 * all inserted nodes of the respective type.
		 * 
		 * @param source
		 *            The source tree model.
		 * @param object
		 *            The node that has been inserted.
		 */
		public void treeNodeInserted(TreeModel source, Object object) {
			if (object instanceof JGraphEditorDiagram)
				pane.addDiagram((JGraphEditorDiagram) object);
			else if (object instanceof JGraphpadFile)
				pane.addFile((JGraphpadFile) object);

			// Inserts all children recursively
			if (object != null) {
				int count = source.getChildCount(object);
				for (int i = 0; i < count; i++)
					treeNodeInserted(source, source.getChild(object, i));
			}
		}

		/**
		 * Calls {@link JGraphpadPane#updateFileTitle(JGraphEditorFile)} or
		 * {@link JGraphpadPane#updateDiagramTitle(JGraphEditorDiagram)} on all
		 * changed nodes of the respective type.
		 * 
		 * @param event
		 *            The object that describes the event.
		 */
		public void treeNodesChanged(TreeModelEvent event) {
			Object[] children = event.getChildren();
			for (int i = 0; i < children.length; i++) {
				if (children[i] instanceof JGraphEditorDiagram)
					pane.updateDiagramTitle((JGraphEditorDiagram) children[i]);
				else if (children[i] instanceof JGraphpadFile)
					pane.updateFileTitle((JGraphpadFile) children[i]);
			}
		}

		/**
		 * Calls {@link JGraphpadPane#removeFile(JGraphEditorFile)} or
		 * {@link JGraphpadPane#removeDiagram(JGraphEditorFile, JGraphEditorDiagram)}
		 * on all removed nodes of the respective type.
		 * 
		 * @param arg0
		 *            The object that describes the event.
		 */
		public void treeNodesRemoved(TreeModelEvent arg0) {
			Object[] children = arg0.getChildren();
			for (int i = 0; i < children.length; i++) {
				if (children[i] instanceof JGraphEditorFile) {
					pane.removeFile((JGraphEditorFile) children[i]);
				} else if (children[i] instanceof JGraphEditorDiagram
						&& arg0.getTreePath().getLastPathComponent() instanceof TreeNode) {
					pane.removeDiagram(JGraphEditorModel
							.getParentFile((TreeNode) arg0.getTreePath()
									.getLastPathComponent()),
							(JGraphEditorDiagram) children[i]);
				}
			}
		}

	}

	/**
	 * Provides a factory method to construct an editor pane.
	 */
	public static class FactoryMethod extends JGraphEditorFactoryMethod {

		/**
		 * Defines the default name for factory methods of this kind.
		 */
		public static String NAME = "createFrame";

		/**
		 * Node name used for menu configurations.
		 */
		public static final String NODENAME_MENUBAR = "menubar";

		/**
		 * Node name used for toolbar configurations.
		 */
		public static final String NODENAME_TOOLBAR = "toolbar";

		/**
		 * Node name used for toolbox configurations.
		 */
		public static final String NODENAME_TOOLBOX = "toolbox";

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
			final JFrame frame = new JFrame();
			frame.setIconImage(JGraphEditorResources.getImage(
					JGraphEditorResources.getString("logo.icon")).getImage());
			frame.getContentPane().setLayout(new BorderLayout());

			// Fetches the menu bar configuration and constructs
			// the menubar for the frame.
			Node menuBarConfiguration = JGraphEditorSettings.getNodeByName(
					configuration.getChildNodes(), NODENAME_MENUBAR);
			frame.setJMenuBar((JMenuBar) editor.getFactory().createMenuBar(
					menuBarConfiguration));

			// Creates container for multiple toolbars and
			// adds it to the main window.
			JPanel toolbars = new JPanel(new GridLayout(2, 1));
			frame.getContentPane().add(toolbars, BorderLayout.NORTH);

			// Fetches the toolbar configuration and create the toolbar
			Node toolbarConfiguration = JGraphEditorSettings.getNodeByName(
					configuration.getChildNodes(), NODENAME_TOOLBAR);
			JToolBar toolbar = editor.getFactory().createToolBar(
					toolbarConfiguration);
			toolbars.add(toolbar);

			// Fetches the toolbox configuration and creates the toolbox
			Node toolboxConfiguration = JGraphEditorSettings.getNodeByName(
					configuration.getChildNodes(), NODENAME_TOOLBOX);
			final JGraphEditorToolbox toolbox = editor.getFactory()
					.createToolbox(toolboxConfiguration);
			toolbars.add(toolbox);

			// Creates and adds the editor pane and adds a diagram tracker to
			// listen to the model and update the internal frames and tabs in
			// the editor pane.
			final JGraphpadPane editorPane = new JGraphpadPane(editor);
			frame.getContentPane().add(editorPane, BorderLayout.CENTER);
			editor.getModel().addTreeModelListener(
					new DocumentTracker(editorPane));

			// Adds the status bar using its factory method
			Component statusBar = editor.getFactory().executeMethod(
					JGraphpadStatusBar.FactoryMethod.NAME);
			if (statusBar != null)
				frame.getContentPane().add(statusBar, BorderLayout.SOUTH);

			// Updates the frame title on focus traversal and various
			// other changes (selection, model, cache, properties...)
			// and keeps the installed graph in the toolbox up-to-date.
			JGraphpadFocusManager focusedGraph = JGraphpadFocusManager
					.getCurrentGraphFocusManager();
			focusedGraph
					.addPropertyChangeListener(new PropertyChangeListener() {
						public void propertyChange(PropertyChangeEvent e) {
							frame.setTitle(getWindowTitle(editorPane));

							// Updates the installed graph in the toolbox
							String prop = e.getPropertyName();
							if (prop
									.equals(JGraphpadFocusManager.FOCUSED_GRAPH_PROPERTY)
									&& e.getNewValue() instanceof JGraph) {
								toolbox.setGraph((JGraph) e.getNewValue());
							}
						}
					});

			// Additionally updates the window title on all changes
			// to the global focus owner.
			KeyboardFocusManager focusManager = KeyboardFocusManager
					.getCurrentKeyboardFocusManager();
			focusManager
					.addPropertyChangeListener(new PropertyChangeListener() {

						/*
						 * (non-Javadoc)
						 */
						public void propertyChange(PropertyChangeEvent e) {
							String prop = e.getPropertyName();
							if (prop.equals("permanentFocusOwner"))
								frame.setTitle(getWindowTitle(editorPane));
						}
					});

			// On an any document model changes
			editor.getModel().addTreeModelListener(new TreeModelListener() {

				/*
				 * (non-Javadoc)
				 */
				public void treeNodesChanged(TreeModelEvent e) {
					frame.setTitle(getWindowTitle(editorPane));
				}

				/*
				 * (non-Javadoc)
				 */
				public void treeNodesInserted(TreeModelEvent e) {
					frame.setTitle(getWindowTitle(editorPane));
				}

				/*
				 * (non-Javadoc)
				 */
				public void treeNodesRemoved(TreeModelEvent e) {
					frame.setTitle(getWindowTitle(editorPane));
				}

				/*
				 * (non-Javadoc)
				 */
				public void treeStructureChanged(TreeModelEvent e) {
					frame.setTitle(getWindowTitle(editorPane));
				}

			});

			return frame;
		}

		/**
		 * Hook for subclassers to return the window title for the specified
		 * editor pane. This implementation appens the parent file title for the
		 * focused diagram or library using
		 * {@link JGraphpadFileAction#getPermanentFocusOwnerFile()} and
		 * {@link JGraphpadPane#getFileTitle(JGraphEditorFile)} to
		 * {@link JGraphpad#APPTITLE}.
		 * 
		 * @param pane
		 *            The editor pane to create the title for.
		 * @return Returns the window title.
		 */
		protected String getWindowTitle(JGraphpadPane pane) {
			JGraphEditorFile file = JGraphpadFileAction
					.getPermanentFocusOwnerFile();
			String title = "";
			if (file != null)
				title = " - " + pane.getFileTitle(file);
			return JGraphpad.APPTITLE + title;
		}

	}

	/**
	 * Provides a factory method to construct the left tab of an editor pane.
	 */
	public static class LeftTabFactoryMethod extends JGraphEditorFactoryMethod {

		/**
		 * Defines the default name for factory methods of this kind.
		 */
		public static String NAME = "createLeftTab";

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
		public LeftTabFactoryMethod(JGraphEditor editor) {
			super(NAME);
			this.editor = editor;
		}

		/*
		 * (non-Javadoc)
		 */
		public Component createInstance(Node configuration) {
			JTabbedPane tabPane = editor.getFactory().createTabbedPane(
					JTabbedPane.TOP);

			final JGraphEditorNavigator navigator = (JGraphEditorNavigator) editor
					.getFactory().executeMethod(
							JGraphEditorNavigator.FactoryMethod.NAME);

			// Updates the current graph in the navigator based on the
			// global focus traversal.
			if (!JGraphpad.INNER_LIBRARIES) {
				JGraphpadFocusManager focusedGraph = JGraphpadFocusManager
						.getCurrentGraphFocusManager();
				focusedGraph
						.addPropertyChangeListener(new PropertyChangeListener() {
							public void propertyChange(PropertyChangeEvent e) {
								String prop = e.getPropertyName();
								if (JGraphpadFocusManager.FOCUSED_GRAPH_PROPERTY
										.equals(prop)) {
									navigator.setCurrentGraph((JGraph) e
											.getNewValue());
								}
							}
						});
			}

			// Executes the factory method to create the repository. The factory
			// method has been previously registered by the default factory.
			Component libraryPane = editor.getFactory().executeMethod(
					JGraphpadLibraryPane.FactoryMethod.NAME);

			JSplitPane navigatorSplit = editor.getFactory().createSplitPane(
					navigator, libraryPane, JSplitPane.VERTICAL_SPLIT);
			editor.getSettings().putObject(KEY_NAVIGATORSPLIT, navigatorSplit);

			// Adds a shutdown hook to the settings to store the divider
			// locations when the program terminates.
			editor.getSettings().addShutdownHook(
					new JGraphEditorSettings.ShutdownHook() {

						// Takes the window bounds and stores the into the
						// in-core
						// user configuration, which is later saved to disk.
						public void shutdown() {
							editor.getSettings().storeSplitPane(
									JGraphpad.NAME_USERSETTINGS,
									KEY_NAVIGATORSPLIT);
						}
					});

			tabPane.addTab(JGraphEditorResources.getString("Navigator"),
					navigatorSplit);

			return tabPane;
		}

	}

	/**
	 * Provides a factory method to construct the bottom tab of an editor pane.
	 */
	public static class BottomTabFactoryMethod extends
			JGraphEditorFactoryMethod {

		/**
		 * Defines the default name for factory methods of this kind.
		 */
		public static String NAME = "createBottomTab";

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
		public BottomTabFactoryMethod(JGraphEditor editor) {
			super(NAME);
			this.editor = editor;
		}

		/*
		 * (non-Javadoc)
		 */
		public Component createInstance(Node configuration) {
			JTabbedPane tabPane = editor.getFactory().createTabbedPane(
					JTabbedPane.TOP);

			// Adds a console tab at the bottom
			Component console = editor.getFactory().executeMethod(
					JGraphpadConsole.FactoryMethod.NAME);
			if (console != null)
				tabPane.addTab(JGraphEditorResources.getString("Errors"),
						editor.getFactory().createScrollPane(console));

			return tabPane;
		}

	}

}
