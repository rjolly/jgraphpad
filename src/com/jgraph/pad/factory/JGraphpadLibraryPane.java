/* 
 * $Id: JGraphpadLibraryPane.java,v 1.9 2007/08/29 09:30:49 gaudenz Exp $
 * Copyright (c) 2001-2005, Gaudenz Alder
 * 
 * All rights reserved.
 * 
 * See LICENSE file for license details. If you are unable to locate
 * this file please contact info (at) jgraph (dot) com.
 */
package com.jgraph.pad.factory;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.event.MouseEvent;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import javax.swing.CellRendererPane;
import javax.swing.JComponent;
import javax.swing.JInternalFrame;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;
import javax.swing.TransferHandler;
import javax.swing.event.TreeModelEvent;

import org.jgraph.JGraph;
import org.jgraph.event.GraphModelEvent;
import org.jgraph.event.GraphModelListener;
import org.jgraph.event.GraphSelectionEvent;
import org.jgraph.event.GraphSelectionListener;
import org.jgraph.graph.CellView;
import org.jgraph.graph.ConnectionSet;
import org.jgraph.graph.DefaultGraphCell;
import org.jgraph.graph.GraphModel;
import org.jgraph.graph.GraphTransferHandler;
import org.jgraph.graph.ParentMap;
import org.w3c.dom.Node;

import com.jgraph.JGraphEditor;
import com.jgraph.JGraphpad;
import com.jgraph.editor.JGraphEditorAction;
import com.jgraph.editor.JGraphEditorFactory;
import com.jgraph.editor.factory.JGraphEditorFactoryMethod;
import com.jgraph.pad.JGraphpadLibrary;
import com.jgraph.pad.graph.JGraphpadVertexRenderer;
import com.jgraph.pad.util.JGraphpadMouseAdapter;
import com.jgraph.pad.util.JGraphpadTreeModelAdapter;

/**
 * Displays groups in a {@link JGraphpadLibrary} as a list of entries. Allows to
 * drag and drop entries to/from {@link JGraph}.
 */
public class JGraphpadLibraryPane extends JComponent {

	/**
	 * Node name for the library popup menu configuration.
	 */
	public static final String NODENAME_LIBRARYPOPUPMENU = "librarypopupmenu";

	/**
	 * Node name for the library popup menu configuration.
	 */
	public static final String NODENAME_ENTRYPOPUPMENU = "entrypopupmenu";

	/**
	 * Defines the preferred width which is used to find the best number of
	 * columns.
	 */
	public static int PREFERRED_WIDTH = 100;

	/**
	 * References the library.
	 */
	protected JGraphpadLibrary library;

	/**
	 * References the enclosing editor.
	 */
	protected JGraphEditor editor;

	/**
	 * Defines geometry and spacing.
	 */
	protected int entrywidth = 60, entryheight = 40, hgap = 10, vgap = 10;

	protected CellRendererPane rendererPane = new CellRendererPane();

	/**
	 * Holds the backing graph for rendering. Makes sure the cell renderer pane
	 * is actually inserted into a valid component.
	 */
	protected JGraph backingGraph = new JGraph();

	/**
	 * Automatically groups cells on drop and ungroups cells on drag. Default is
	 * true.
	 */
	protected boolean autoBoxing = true;

	/**
	 * Specifies whether to use antialiasing to render the entries.
	 */
	protected boolean antiAliased = true;

	/**
	 * Specifies whether the library can be changed.
	 */
	protected boolean isReadOnly = false;

	/**
	 * Internal variable to block drag and drop if the operation was initiated
	 * from here. This variable is true during drag operations that started
	 * here.
	 */
	protected transient boolean dragging = false;

	/**
	 * Constructs a new repository pane for the specified library.
	 * 
	 * @param library
	 *            The library that contains the cells.
	 */
	public JGraphpadLibraryPane(final JGraphEditor editor,
			JGraphpadLibrary library) {
		setTransferHandler(new LibraryTransferHandler());
		this.editor = editor;
		this.library = library;
		this.isReadOnly = library.isReadOnly();

		// Disables the folding icon in the backing graph
		backingGraph.putClientProperty(
				JGraphpadVertexRenderer.CLIENTPROPERTY_SHOWFOLDINGICONS,
				new Boolean(false));

		// Configures the backing graph
		GraphTransferHandler transferHandler = new LibraryGraphTransferHandler();
		transferHandler.setAlwaysReceiveAsCopyAction(true);
		backingGraph.setTransferHandler(transferHandler);
		backingGraph.setGraphLayoutCache(library.getGraphLayoutCache());
		backingGraph.setDragEnabled(true);
		backingGraph.setDoubleBuffered(false);

		// Starts dragging the entry under the mouse pointer
		addMouseListener(new JGraphpadMouseAdapter(editor,
				NODENAME_LIBRARYPOPUPMENU) {

			/**
			 * Selects the entry under the mouse pointer.
			 */
			public void mousePressed(MouseEvent event) {
				int index = getIndexAt(event.getX(), event.getY());

				// Selects cell at index and starts dragging
				if (index >= 0) {
					getBackingGraph().setSelectionCell(
							getBackingGraph().getModel().getRootAt(index));
					if (!SwingUtilities.isRightMouseButton(event))
						getTransferHandler().exportAsDrag(getBackingGraph(),
								event, TransferHandler.COPY);
				}

				// Clear selection if no cell found
				else {
					getBackingGraph().clearSelection();
				}
				requestFocus();
			}

			/**
			 * Overrides the parent implementation to return a different config
			 * if the selection is not empty.
			 */
			public String getConfigName() {
				return (!isSelectionEmpty()) ? NODENAME_ENTRYPOPUPMENU : super
						.getConfigName();
			}

		});

		// Repaints and revalidates on model changes
		backingGraph.getModel().addGraphModelListener(new GraphModelListener() {
			public void graphChanged(GraphModelEvent e) {
				revalidate();
				repaint();
			}
		});

		// Repaints on selection changes
		backingGraph.getSelectionModel().addGraphSelectionListener(
				new GraphSelectionListener() {
					public void valueChanged(GraphSelectionEvent e) {
						repaint();
					}
				});

		add(rendererPane);
	}

	/**
	 * Returns true if the library contains no entries.
	 * 
	 * @return Returns true if the library is empty.
	 */
	public boolean isEmpty() {
		return backingGraph.getModel().getRootCount() == 0;
	}

	/**
	 * Returns true if the library contains no selectio entries.
	 * 
	 * @return Returns true if the selection is empty.
	 */
	public boolean isSelectionEmpty() {
		return backingGraph.isSelectionEmpty();
	}

	/**
	 * Removes the selection entry from the library.
	 */
	public void removeEntry() {
		if (!backingGraph.isSelectionEmpty()) {
			Object[] cells = backingGraph.getDescendants(backingGraph
					.getSelectionCells());
			backingGraph.getModel().remove(cells);
		}
	}

	/**
	 * Brings the selection entry to front (start of list).
	 */
	public void bringEntryToFront() {
		if (!backingGraph.isSelectionEmpty())
			backingGraph.getModel().toFront(backingGraph.getSelectionCells());
	}

	/**
	 * Sends the selection entry to back (end of list).
	 */
	public void sendEntryToBack() {
		if (!backingGraph.isSelectionEmpty())
			backingGraph.getModel().toBack(backingGraph.getSelectionCells());
	}

	/**
	 * Returns the bounds of the entry at <code>index</code>. This returns a
	 * value regardless of whether an entry at index actually exists.
	 * 
	 * @param index
	 *            The index that specifies the entry.
	 * @return Returns the bounds for the entry at <code>index</code>.
	 */
	public Rectangle getBounds(int index) {
		Rectangle outer = getBounds();
		int cols = Math.max(outer.width / (entrywidth + hgap), 1);
		int col = index % cols;
		int row = index / cols;
		int x = hgap + col * (entrywidth + hgap);
		int y = vgap + row * (entryheight + vgap);
		return new Rectangle(x, y, entrywidth, entryheight);
	}

	/**
	 * Returns the index of the entry at the specified location. If no entry
	 * exists at the specified location then -1 is returned.
	 * 
	 * @param x
	 *            The x position.
	 * @param y
	 *            The y position.
	 * @return Returns the index for the specified location or -1.
	 */
	public int getIndexAt(int x, int y) {
		Rectangle outer = getBounds();
		int cols = Math.max(outer.width / (entrywidth + hgap), 1);
		int col = x / (entrywidth + hgap);
		int row = y / (entryheight + vgap);
		int index = row * cols + col;
		if (index >= 0 && index < backingGraph.getModel().getRootCount())
			return index;
		return -1;

	}

	/**
	 * Paints the library pane.
	 * 
	 * @param g
	 *            The graphics to paint the library pane to.
	 */
	public void paint(Graphics g) {
		super.paint(g);
		Graphics2D g2 = (Graphics2D) g;
		if (antiAliased)
			g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
					RenderingHints.VALUE_ANTIALIAS_ON);
		Shape clip = g.getClip();
		CellView[] entries = backingGraph.getGraphLayoutCache().getRoots();
		for (int i = 0; i < entries.length; i++) {

			// Computes the scale to draw the entry width
			Rectangle2D bounds = entries[i].getBounds();
			double scale = Math.min(entrywidth / bounds.getWidth(), entryheight
					/ bounds.getHeight());
			Rectangle rect = getBounds(i);
			Rectangle frame = new Rectangle((int) rect.getX() - 2, (int) rect
					.getY() - 2, entrywidth + 4, entryheight + 4);

			// Translates and renders the entry using the backing graph
			if (clip.intersects(frame)) {
				g2.scale(scale, scale);
				g.translate((int) (rect.getX() / scale - bounds.getX()),
						(int) (rect.getY() / scale - bounds.getY()));

				// Since the backing graph has not been added to a component
				// hierarchy we have to use our own cell renderer pane here.
				paintView(g, entries[i]);
				g.translate(-(int) (rect.getX() / scale - bounds.getX()),
						-(int) (rect.getY() / scale - bounds.getY()));
				g2.scale(1 / scale, 1 / scale);
				if (getBackingGraph().isCellSelected(entries[i].getCell())) {
					g.setColor(backingGraph.getHandleColor());
					g.drawRect(frame.x, frame.y, frame.width, frame.height);
				}
			}
		}
	}

	/**
	 * Paints the specified cell view on the local cell renderer pane.
	 * 
	 * @param g
	 * @param view
	 */
	protected void paintView(Graphics g, CellView view) {
		// Paint parent
		Component component = view.getRendererComponent(backingGraph, false,
				false, false);
		Rectangle2D bounds = view.getBounds();
		component.setBounds(0, 0, (int) bounds.getWidth(), (int) bounds
				.getHeight());
		rendererPane.paintComponent(g, component, this, (int) bounds.getX(),
				(int) bounds.getY(), (int) bounds.getWidth(), (int) bounds
						.getHeight(), true);

		// Paint children recursively
		CellView[] children = view.getChildViews();
		for (int i = 0; i < children.length; i++) {
			paintView(g, children[i]); // recurse
		}
	}

	/**
	 * Overrides the parent method to contain all entries in a matrix with as
	 * many columns as fit into {@link #PREFERRED_WIDTH} with the current
	 * {@link #entrywidth}.
	 * 
	 * @return Returns the preferred size.
	 */
	public Dimension preferredSize() {
		int rootCount = getBackingGraph().getModel().getRootCount();
		int cols = Math.max(1, PREFERRED_WIDTH / (entrywidth + vgap));
		return new Dimension(cols * (entrywidth + vgap), (Math.max(
				(rootCount + 1) / cols, 1) * (entryheight + hgap)));
	}

	/**
	 * Overrides the parent method to contain a single entry.
	 * 
	 * @return Returns the minimum size.
	 */
	public Dimension getMinimumSize() {
		return new Dimension(entrywidth + vgap, entryheight + hgap);
	}

	/**
	 * Returns the backing graph used for rendering entries.
	 * 
	 * @return Returns the backing graph.
	 */
	public JGraph getBackingGraph() {
		return backingGraph;
	}

	/**
	 * Sets the backing graph to be used to render entries.
	 * 
	 * @param backingGraph
	 *            The backing graph to set.
	 */
	public void setBackingGraph(JGraph backingGraph) {
		this.backingGraph = backingGraph;
	}

	/**
	 * Returns the library associated with the library pane.
	 * 
	 * @return Returns the library.
	 */
	public JGraphpadLibrary getLibrary() {
		return library;
	}

	/**
	 * Sets the library associated with the library pane.
	 * 
	 * @param library
	 *            The library to set.
	 */
	public void setLibrary(JGraphpadLibrary library) {
		this.library = library;
	}

	/**
	 * Returns true if the library uses autoboxing.
	 * 
	 * @return Returns the autoBoxing.
	 */
	public boolean isAutoBoxing() {
		return autoBoxing;
	}

	/**
	 * Sets if the library should use autoboxing.
	 * 
	 * @param autoBoxing
	 *            The autoBoxing to set.
	 */
	public void setAutoBoxing(boolean autoBoxing) {
		this.autoBoxing = autoBoxing;
	}

	/**
	 * Returns the height to draw entries.
	 * 
	 * @return Returns the entryheight.
	 */
	public int getEntryheight() {
		return entryheight;
	}

	/**
	 * Sets the height to draw entries.
	 * 
	 * @param entryheight
	 *            The entryheight to set.
	 */
	public void setEntryheight(int entryheight) {
		this.entryheight = entryheight;
	}

	/**
	 * Returns the width to draw entries.
	 * 
	 * @return Returns the entrywidth.
	 */
	public int getEntrywidth() {
		return entrywidth;
	}

	/**
	 * Sets the width to draw entries.
	 * 
	 * @param entrywidth
	 *            The entrywidth to set.
	 */
	public void setEntrywidth(int entrywidth) {
		this.entrywidth = entrywidth;
	}

	/**
	 * Returns the horizontal gap between entries.
	 * 
	 * @return Returns the hgap.
	 */
	public int getHgap() {
		return hgap;
	}

	/**
	 * Sets the horizontal gap between entries.
	 * 
	 * @param hgap
	 *            The hgap to set.
	 */
	public void setHgap(int hgap) {
		this.hgap = hgap;
	}

	/**
	 * Returns the vertical gap between entries.
	 * 
	 * @return Returns the vgap.
	 */
	public int getVgap() {
		return vgap;
	}

	/**
	 * Sets the vertical gap between entries.
	 * 
	 * @param vgap
	 *            The vgap to set.
	 */
	public void setVgap(int vgap) {
		this.vgap = vgap;
	}

	/**
	 * Returns true if rendering should be antialiased.
	 * 
	 * @return Returns the antiAliased.
	 */
	public boolean isAntiAliased() {
		return antiAliased;
	}

	/**
	 * Sets if the rendering should be antialiased.
	 * 
	 * @param antiAliased
	 *            The antiAliased to set.
	 */
	public void setAntiAliased(boolean antiAliased) {
		this.antiAliased = antiAliased;
	}

	/**
	 * @return Returns the readOnly.
	 */
	public boolean isReadOnly() {
		return isReadOnly;
	}

	/**
	 * @param readOnly
	 *            The readOnly to set.
	 */
	public void setReadOnly(boolean readOnly) {
		this.isReadOnly = readOnly;
	}

	/**
	 * Returns the parent library pane of the specified component or the
	 * component itself if it is a library pane.
	 * 
	 * @return Returns the parent library pane.
	 */
	public static JGraphpadLibraryPane getParentLibraryPane(Component component) {
		while (component != null) {
			if (component instanceof JGraphpadLibraryPane)
				return (JGraphpadLibraryPane) component;
			component = component.getParent();
		}
		return null;
	}

	/**
	 * Utility class to implement autoboxing and to set the {@link #dragging}
	 * flag.
	 */
	public class LibraryGraphTransferHandler extends GraphTransferHandler {

		/**
		 * Only allows importing data if the enclosing library is not read-only.
		 */
		public boolean canImport(JComponent component, DataFlavor[] flavors) {
			return !isReadOnly;
		}

		/**
		 * Overrides the parent method to set the dragging flag and replaces the
		 * selection group with its children if autoboxing is turned on.
		 * 
		 * @param c
		 *            The component to perform the opeation in.
		 */
		protected Transferable createTransferable(JComponent c) {
			dragging = true;

			// Creates a transferable that contains the children
			// of the autobox cell (selection cell) and returns them.
			if (c instanceof JGraph) {
				JGraph graph = (JGraph) c;
				if (!graph.isSelectionEmpty()) {
					Object cell = graph.getSelectionCell();
					GraphModel model = graph.getModel();
					if (cell instanceof AutoBoxCell) {
						int childCount = model.getChildCount(cell);
						List children = new ArrayList(childCount);
						for (int i = 0; i < childCount; i++)
							children.add(model.getChild(cell, i));
						Object[] cells = graph.getDescendants(graph
								.order(children.toArray()));
						return createTransferable(graph, cells); // exit
					}
				}
			}
			return super.createTransferable(c);
		}

		/**
		 * Overrides the parent method to reset the dragging flag in order to
		 * accept external drops.
		 */
		protected void exportDone(JComponent source, Transferable data,
				int action) {
			dragging = false;
			super.exportDone(source, data, action);
		}

		/**
		 * Overrides the parent method to add a group to the dropped cells if
		 * autoboxing is turned on.
		 * 
		 * @param graph
		 *            The graph to perform the operation in.
		 * @param cells
		 *            The cells to be inserted into the library.
		 * @param nested
		 *            The attributes to be assigned to the cells.
		 * @param cs
		 *            The connections to be established between the cells.
		 * @param pm
		 *            The parent map that describes the parent-child relations.
		 * @param dx
		 *            The x-offset to translate the cells with.
		 * @param dy
		 *            The y-offset to translate the cells with.
		 */
		protected void handleExternalDrop(JGraph graph, Object[] cells,
				Map nested, ConnectionSet cs, ParentMap pm, double dx, double dy) {
			if (autoBoxing) {
				// Adds a new autoboxcell as the parent cell to all inserted
				// cells which have no parent in the transfer data.
				DefaultGraphCell parent = new AutoBoxCell();
				List tmp = new ArrayList(cells.length + 1);
				tmp.add(parent);
				for (int i = 0; i < cells.length; i++) {
					if (!(cells[i] instanceof AutoBoxCell)) {
						tmp.add(cells[i]);
						Object oldParent = graph.getModel().getParent(cells[i]);
						if (oldParent == null
								|| !pm.getChangedNodes().contains(oldParent))
							pm.addEntry(cells[i], parent);
					}
				}
				cells = tmp.toArray();
			}
			super.handleExternalDrop(graph, cells, nested, cs, pm, dx, dy);
		}

	}

	/**
	 * Utility class to redirect transfer events from the library pane to the
	 * backing graph if the dragging flag is not set.
	 * 
	 */
	public class LibraryTransferHandler extends TransferHandler {

		/*
		 * (non-Javadoc)
		 */
		public int getSourceActions(JComponent c) {
			return COPY;
		}

		/*
		 * (non-Javadoc)
		 */
		public boolean canImport(JComponent comp, DataFlavor[] flavors) {
			return !dragging
					&& getBackingGraph().getTransferHandler().canImport(comp,
							flavors);
		}

		/*
		 * (non-Javadoc)
		 */
		public boolean importData(JComponent comp, Transferable t) {
			return getBackingGraph().getTransferHandler().importData(
					getBackingGraph(), t);
		}

	}

	/**
	 * Utility class to establish a listener in a editor's document model and
	 * update the library panes in a tabbed pane.
	 */
	public static class LibraryTracker extends JGraphpadTreeModelAdapter {

		/**
		 * References the enclosing editor.
		 */
		protected JGraphEditor editor;

		/**
		 * References the tabbed pane to be updated.
		 */
		protected JTabbedPane tabPane;

		/**
		 * Holds library, component pairs to find the respective tabs.
		 */
		protected Map tabs = new Hashtable();

		/**
		 * Constructs a new library tracker for updating the specified tabPane
		 * using factory to create required components. The library tracker must
		 * be added as a tree model listener to an editor's document model.
		 * 
		 * @param tabPane
		 *            The pane to be updated on document model changes.
		 * @param editor
		 *            The enclosing editor.
		 */
		public LibraryTracker(JTabbedPane tabPane, JGraphEditor editor) {
			this.tabPane = tabPane;
			this.editor = editor;
		}

		/**
		 * Creates a new {@link JGraphpadLibraryPane}, wraps it up in a scroll
		 * pane using {@link JGraphEditorFactory#createScrollPane(Component)}
		 * and adds it as a tab to {@link #tabPane} using the library's toString
		 * method to set the tab's title.
		 * 
		 * @param arg0
		 *            The object that describes the event.
		 */
		public void treeNodesInserted(TreeModelEvent arg0) {
			JGraphpadPane padPane = JGraphEditorAction.getJGraphpadPane();

			Object[] children = arg0.getChildren();
			for (int i = 0; i < children.length; i++) {

				if (children[i] instanceof JGraphpadLibrary) {
					JGraphpadLibrary library = (JGraphpadLibrary) children[i];

					JInternalFrame frame = null;

					if (padPane != null) {
						frame = padPane.getInternalFrame(library.getParent());
					}
					
					if ((!JGraphpad.INNER_LIBRARIES) ||
						(frame != null && frame.isAncestorOf(tabPane))) {
						final JGraphpadLibraryPane libraryPane = new JGraphpadLibraryPane(
								editor, library);
						Component pane = editor.getFactory().createScrollPane(
								libraryPane);
						tabPane.addTab(getTitle(library), pane);
						tabPane.setSelectedComponent(pane);
						tabs.put(children[i], pane);

						// Transfers the focus to the new library pane
						// after the component hierarchy has been revalidated.
						SwingUtilities.invokeLater(new Runnable() {
							public void run() {
								libraryPane.requestFocus();
							}
						});
					}
				}
			}
		}

		/**
		 * Removes the tabs for the removed libraries from the tab pane.
		 */
		public void treeNodesRemoved(TreeModelEvent arg0) {
			Object[] children = arg0.getChildren();
			for (int i = 0; i < children.length; i++) {
				if (children[i] instanceof JGraphpadLibrary) {

					// Finds the tab with the scrollpane that contains
					// the library pane for the removed library and
					// removes it.
					Object tab = tabs.remove(children[i]);
					if (tab instanceof Component)
						tabPane.remove((Component) tab);
				}
			}
		}

		/**
		 * Calls {@link #updateTabTitle(JGraphpadLibrary)} for all libraries
		 * that have changed in the tree.
		 * 
		 * @param arg0
		 *            The object that describes the event.
		 */
		public void treeNodesChanged(TreeModelEvent arg0) {
			Object[] children = arg0.getChildren();
			for (int i = 0; i < children.length; i++)
				if (children[i] instanceof JGraphpadLibrary)
					updateTabTitle((JGraphpadLibrary) children[i]);
		}

		/**
		 * Invoked to update the title for the tab of the specified library.
		 * 
		 * @param library
		 *            The library who's title needs to be updated.
		 */
		protected void updateTabTitle(JGraphpadLibrary library) {
			Object tab = tabs.get(library);
			if (tab instanceof Component) {
				String title = getTitle(library);
				int index = tabPane.indexOfComponent((Component) tab);
				tabPane.setTitleAt(index, title);
			}
		}

		/**
		 * Hook for subclassers to return the tab title to be used for the
		 * specified library. This implementation returns the filename-part of a
		 * path.
		 * 
		 * @param library
		 *            The library to return the title for.
		 * @return Returns a title for <code>library</code>.
		 */
		protected String getTitle(JGraphpadLibrary library) {
			String state = (library.isModified()) ? " *" : "";
			return String.valueOf(library) + state;
		}

	}

	/**
	 * Utility class to identify autoboxing cells.
	 */
	public static class AutoBoxCell extends DefaultGraphCell {
		// empty
	}

	/**
	 * Provides a factory method to construct a library pane.
	 */
	public static class FactoryMethod extends JGraphEditorFactoryMethod {

		/**
		 * Defines the default name for factory methods of this kind.
		 */
		public static String NAME = "createLibraryPane";

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
			JTabbedPane tabPane = editor.getFactory().createTabbedPane(
					JTabbedPane.BOTTOM);
			tabPane.addMouseListener(new JGraphpadMouseAdapter(editor,
					JGraphpadPane.NODENAME_DESKTOPPOPUPMENU));
			LibraryTracker tracker = new LibraryTracker(tabPane, editor);
			editor.getModel().addTreeModelListener(tracker);
			return tabPane;
		}

	}

}