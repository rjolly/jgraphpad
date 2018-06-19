/* 
 * $Id: JGraphEditorComboBox.java,v 1.2 2007/03/25 13:13:16 gaudenz Exp $
 * Copyright (c) 2001-2005, Gaudenz Alder
 * 
 * All rights reserved.
 * 
 * See LICENSE file for license details. If you are unable to locate
 * this file please contact info (at) jgraph (dot) com.
 */
package com.jgraph.editor.factory;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Rectangle2D;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;
import javax.swing.border.Border;

import org.jgraph.JGraph;
import org.jgraph.graph.AttributeMap;
import org.jgraph.graph.CellView;
import org.jgraph.graph.CellViewRenderer;
import org.jgraph.graph.DefaultGraphModel;
import org.jgraph.graph.EdgeRenderer;
import org.jgraph.graph.EdgeView;
import org.jgraph.graph.GraphConstants;
import org.jgraph.graph.VertexView;
import org.w3c.dom.Node;

import com.jgraph.editor.JGraphEditorAction;

/**
 * Combo box for selecting cell styles. The class provides a series of factory
 * methods to be added to an editor factory. The following methods are provided:
 * {@link BorderComboFactoryMethod}, {@link LineDecorationComboFactoryMethod}
 * and {@link LineWidthComboFactoryMethod}.
 */
public class JGraphEditorComboBox extends JComboBox {

	/**
	 * Default array of borders.
	 */
	public static Border[] defaultBorders = new Border[] {
			BorderFactory.createRaisedBevelBorder(),
			BorderFactory.createLoweredBevelBorder(),
			BorderFactory.createEtchedBorder(),
			BorderFactory.createLineBorder(Color.black) };

	/**
	 * Default array of widths.
	 */
	public static float[] defaultWidths = new float[] { 1, 2, 3, 4, 5, 6, 8 };

	/**
	 * Default array of patterns.
	 */
	protected static float[][] defaultPatterns = new float[][] {
			new float[] { 1, 1 }, new float[] { 2, 2 }, new float[] { 4, 2 },
			new float[] { 4, 4 }, new float[] { 8, 2 }, new float[] { 8, 4 },
			new float[] { 4, 4, 16, 4 } };

	/**
	 * Default array of fillable arrows.
	 */
	public static int[] defaultFillableDecorations = new int[] {
			GraphConstants.ARROW_CLASSIC, GraphConstants.ARROW_TECHNICAL,
			GraphConstants.ARROW_DIAMOND, GraphConstants.ARROW_CIRCLE };

	/**
	 * Default array or arrows.
	 */
	public static int[] defaultDecorations = new int[] {
			GraphConstants.ARROW_CLASSIC, GraphConstants.ARROW_SIMPLE,
			GraphConstants.ARROW_TECHNICAL, GraphConstants.ARROW_DIAMOND,
			GraphConstants.ARROW_CIRCLE, GraphConstants.ARROW_LINE,
			GraphConstants.ARROW_DOUBLELINE };

	/**
	 * Shared graph instance for rendering.
	 */
	protected static JGraph backingGraph = new JGraph(new DefaultGraphModel());

	/**
	 * Initial view attributes for restoring.
	 */
	protected AttributeMap initialAttributes;

	/**
	 * Constructs a new combo box for the specified attribute maps. Uses an edge
	 * view for preview of <code>edgePreview</code> is true, otherwise a
	 * vertex view is used.
	 * 
	 * @param attributes
	 *            The array of attributes for the combo box entries.
	 * @param edgePreview
	 *            Whether to use an edge for preview.
	 */
	public JGraphEditorComboBox(Map[] attributes, boolean edgePreview) {
		this(attributes, null, edgePreview);
	}

	/**
	 * Constructs a new combo box for the specified attribute maps. Uses an edge
	 * view for preview if <code>edgePreview</code> is true, otherwise a
	 * vertex view is used.
	 * 
	 * @param attributes
	 *            The array of attributes for the combo box entries.
	 * @param view
	 *            The cell view to use for previewing the attributes.
	 * @param edgePreview
	 *            Whether to use an edge for preview.
	 */
	public JGraphEditorComboBox(Map[] attributes, CellView view,
			boolean edgePreview) {
		super(attributes);
		setOpaque(false);
		if (view == null)
			view = (edgePreview) ? createEdgeView() : createVertexView();
		initialAttributes = new AttributeMap(view.getAttributes());
		GraphConstants.setRemoveAll(initialAttributes, true);
		setRenderer(new CellViewRendererBridge(view));
		setMinimumSize(new Dimension(44, 20));
		setPreferredSize(new Dimension(44, 20));
		setMaximumSize(new Dimension(44, 20));
	}

	/**
	 * Returns a new vertex to be used for preview. This implementation returns
	 * a {@link VertexView} with a string user object.
	 * 
	 * @return Returns a new vertex view for preview.
	 */
	protected CellView createVertexView() {
		VertexView view = new VertexView("");
		Dimension dim = getVertexViewSize();
		Rectangle2D bounds = new Rectangle2D.Double(0, 0, dim.width, dim.height);
		GraphConstants.setBounds(view.getAttributes(), bounds);
		return view;
	}

	/**
	 * Hook for subclassers to modify the size of the vertex view.
	 * 
	 * @return Returns the size of the vertex view.
	 */
	protected Dimension getVertexViewSize() {
		return new Dimension(18, 18);
	}

	/**
	 * Returns a new edge to be used for preview. This implementation returns a
	 * {@link JGraphComboEdgeView} with a string user object and a set of
	 * default points.
	 * 
	 * @return Returns a new edge view for preview.
	 */
	protected CellView createEdgeView() {
		EdgeView view = new JGraphComboEdgeView(" ");
		List points = new LinkedList();
		points.add(new Point(2, 6));
		points.add(new Point(14, 6));
		GraphConstants.setPoints(view.getAttributes(), points);
		GraphConstants.setBeginSize(view.getAttributes(), 8);
		GraphConstants.setEndSize(view.getAttributes(), 8);
		return view;
	}

	/**
	 * Returns the backing graph which is used for rendering previews.
	 * 
	 * @return Returns the backing graph.
	 */
	public static JGraph getBackingGraph() {
		return backingGraph;
	}

	/**
	 * Sets the backing graph which should be used for rendering previews.
	 * 
	 * @param backingGraph
	 *            The backing graph to set.
	 */
	public static void setBackingGraph(JGraph backingGraph) {
		JGraphEditorComboBox.backingGraph = backingGraph;
	}

	/**
	 * This class returns the renderer of the view configured for the selected
	 * combo box item. Note: If the map contains an icon then this renderer (a
	 * JLabel) with the icon is returned.
	 */
	public class CellViewRendererBridge extends JLabel implements
			ListCellRenderer {

		/**
		 * Reference to the view this renderer bridge uses for rendering.
		 */
		protected CellView view = null;

		/**
		 * Constructs a new renderer bridge using <code>view</code> to render
		 * the list entries.
		 * 
		 * @param view
		 *            The view to renderer the list entries with.
		 */
		public CellViewRendererBridge(CellView view) {
			setHorizontalAlignment(CENTER);
			setVerticalAlignment(CENTER);
			this.view = view;
		}

		/**
		 * Returns a configured renderer for the specified value.
		 * 
		 * @param list
		 *            The list that contains the entry.
		 * @param value
		 *            The value to be rendered.
		 * @param index
		 *            The index of the value.
		 * @param isSelected
		 *            Whether the value should be rendered as selected.
		 * @param hasFocus
		 *            Whether the value should be renderer as focused.
		 * @return Returns a renderer for the specified value.
		 */
		public Component getListCellRendererComponent(JList list, Object value,
				int index, boolean isSelected, boolean hasFocus) {
			Icon icon = null;
			if (value instanceof Map) {
				Map map = (Map) value;
				icon = GraphConstants.getIcon(map);
				if (icon == null) {
					view.getAllAttributes().clear();
					view.changeAttributes(backingGraph.getGraphLayoutCache(), initialAttributes);
					view.changeAttributes(backingGraph.getGraphLayoutCache(), map);
				}
			}
			if (icon != null) {
				setIcon(icon);
				return this;
			} else {
				Component r = view.getRendererComponent(backingGraph,
						isSelected, hasFocus, false);
				if (r instanceof JComponent) {
					final JComponent c = (JComponent) r;
					final Dimension d = new Dimension((int) view.getBounds()
							.getWidth(), (int) view.getBounds().getHeight());

					// Returns an outer renderer that may be freely sized by the
					// combo rendering process. The component paints the cell
					// view renderer at the size of the contained cell view.
					JComponent wrapper = new JComponent() {

						/**
						 * Paints the cell view renderer at the correct size.
						 * 
						 * @param g
						 *            The graphics to paint the inner renderer
						 *            on.
						 */
						public void paint(Graphics g) {
							c.setBounds(0, 0, d.width, d.height);
							c.paint(g);
						}

					};
					wrapper.setMinimumSize(d);
					wrapper.setPreferredSize(d);
					return wrapper;
				}
				return r;
			}
		}
	}

	/**
	 * This class provides a an edge view with a special renderer.
	 */
	protected static class JGraphComboEdgeView extends EdgeView {

		/**
		 * Holds the custom renderer.
		 */
		protected static CellViewRenderer renderer = new JGraphComboEdgeRenderer();

		/**
		 * Constructs an empty vertex view.
		 */
		public JGraphComboEdgeView() {
			super();
		}

		/**
		 * Constructs a new combo edge view for the specified cell.
		 * 
		 * @param cell
		 *            The cell to create the edge view for.
		 */
		public JGraphComboEdgeView(Object cell) {
			super(cell);
		}

		/**
		 * Returns the custom renderer for this view.
		 * 
		 * @return Returns the custom renderer.
		 */
		public CellViewRenderer getRenderer() {
			return renderer;
		}

	}

	/**
	 * This class removes the translate call in the paint method. This is
	 * required to render a list cell.
	 */
	protected static class JGraphComboEdgeRenderer extends EdgeRenderer {

		/**
		 * Overrides parent method to avoid translating the graphics before
		 * painting.
		 * 
		 * @param g
		 *            The graphics object to avoid translation for.
		 */
		protected void translateGraphics(Graphics g) {
			// do not translate
		}
	}

	/**
	 * Provides a factory method to construct a border combo box.
	 */
	public static class BorderComboFactoryMethod extends
			JGraphEditorFactoryMethod {

		/**
		 * Defines the default name for factory methods of this kind.
		 */
		public static String NAME = "createBorderCombo";

		/**
		 * Constructs a new border combo factory method using {@link #NAME}.
		 */
		public BorderComboFactoryMethod() {
			super(NAME);
		}

		/**
		 * Returns a new border combo box for <code>configuration</code>.
		 * 
		 * @param configuration
		 *            The configuration to use for creating the combo box.
		 */
		public Component createInstance(Node configuration) {
			Map[] attrs = new Hashtable[defaultBorders.length + 1];
			attrs[0] = new Hashtable();
			GraphConstants.setRemoveAttributes(attrs[0], new Object[] {
					GraphConstants.BORDER, GraphConstants.BORDERCOLOR });
			for (int i = 0; i < defaultBorders.length; i++) {
				attrs[i + 1] = new Hashtable(2);
				GraphConstants.setBorder(attrs[i + 1], defaultBorders[i]);
			}
			JGraphEditorComboBox comboBox = new JGraphEditorComboBox(attrs,
					false);
			comboBox.addActionListener(new ComboBoxListener());
			comboBox.setFocusable(false);
			return comboBox;
		}

	}

	/**
	 * Provides a factory method to construct a linewidth combo box.
	 */
	public static class LineWidthComboFactoryMethod extends
			JGraphEditorFactoryMethod {

		/**
		 * Defines the default name for factory methods of this kind.
		 */
		public static String NAME = "createLineWidthCombo";

		/**
		 * Constructs a new line width combo factory method using {@link #NAME}.
		 */
		public LineWidthComboFactoryMethod() {
			super(NAME);
		}

		/**
		 * Returns a new linewidth combo box for <code>configuration</code>.
		 * 
		 * @param configuration
		 *            The configuration to use for creating the combo box.
		 */
		public Component createInstance(Node configuration) {
			Map[] attrs = new Hashtable[defaultWidths.length];
			for (int i = 0; i < defaultWidths.length; i++) {
				attrs[i] = new Hashtable(2);
				GraphConstants.setLineWidth(attrs[i], defaultWidths[i]);
			}
			JGraphEditorComboBox comboBox = new JGraphEditorComboBox(attrs,
					true);
			comboBox.addActionListener(new ComboBoxListener());
			comboBox.setFocusable(false);
			return comboBox;
		}

	}

	/**
	 * Provides a factory method to construct a dashpattern combo box.
	 */
	public static class DashPatternComboFactoryMethod extends
			JGraphEditorFactoryMethod {

		/**
		 * Defines the default name for factory methods of this kind.
		 */
		public static String NAME = "createDashPatternCombo";

		/**
		 * Constructs a new dash pattern combo factory method using
		 * {@link #NAME}.
		 */
		public DashPatternComboFactoryMethod() {
			super(NAME);
		}

		/**
		 * Returns a new dashpattern combo box for <code>configuration</code>.
		 * 
		 * @param configuration
		 *            The configuration to use for creating the combo box.
		 */
		public Component createInstance(Node configuration) {
			Map[] attrs = new Hashtable[defaultPatterns.length + 1];
			attrs[0] = new Hashtable();
			GraphConstants.setRemoveAttributes(attrs[0],
					new Object[] { GraphConstants.DASHPATTERN });
			for (int i = 0; i < defaultPatterns.length; i++) {
				attrs[i + 1] = new Hashtable(2);
				GraphConstants.setDashPattern(attrs[i + 1], defaultPatterns[i]);
			}
			JGraphEditorComboBox comboBox = new JGraphEditorComboBox(attrs,
					true);
			comboBox.addActionListener(new ComboBoxListener());
			comboBox.setFocusable(false);
			return comboBox;
		}

	}

	/**
	 * Provides a factory method to construct a line decoration combo box.
	 */
	public static class LineDecorationComboFactoryMethod extends
			JGraphEditorFactoryMethod {

		/**
		 * Defines the default name for factory methods of this kind.
		 */
		public static String NAME = "createLineDecorationCombo";

		/**
		 * Constructs a new line decoration combo factory method using
		 * {@link #NAME}.
		 */
		public LineDecorationComboFactoryMethod() {
			super(NAME);
		}

		/**
		 * Returns a new line decoration combo box for
		 * <code>configuration</code>.
		 * 
		 * @param configuration
		 *            The configuration to use for creating the combo box.
		 */
		public Component createInstance(Node configuration) {
			int all = defaultDecorations.length
					+ defaultFillableDecorations.length;
			Map[] attrs = new Hashtable[2 * all + 1];
			attrs[0] = new Hashtable();
			GraphConstants.setRemoveAttributes(attrs[0], new Object[] {
					GraphConstants.LINEBEGIN, GraphConstants.LINEEND,
					GraphConstants.BEGINFILL, GraphConstants.ENDFILL });

			// Adds end decorations, not-filled and filled
			for (int i = 0; i < defaultPatterns.length; i++) {
				Map m = attrs[i + 1] = new Hashtable(2);
				GraphConstants.setLineEnd(m, defaultDecorations[i]);
				GraphConstants.setEndFill(m, false);
			}
			for (int i = 0; i < defaultFillableDecorations.length; i++) {
				Map m = attrs[defaultDecorations.length + i + 1] = new Hashtable(
						2);
				GraphConstants.setLineEnd(m, defaultFillableDecorations[i]);
				GraphConstants.setEndFill(m, true);
			}

			// Adds begin decorations, not-filled and filled
			for (int i = 0; i < defaultPatterns.length; i++) {
				Map m = attrs[all + i + 1] = new Hashtable(2);
				GraphConstants.setLineBegin(m, defaultDecorations[i]);
				GraphConstants.setBeginFill(m, false);
			}
			for (int i = 0; i < defaultFillableDecorations.length; i++) {
				Map m = attrs[defaultDecorations.length + all + i + 1] = new Hashtable(
						2);
				GraphConstants.setLineBegin(m, defaultFillableDecorations[i]);
				GraphConstants.setBeginFill(m, true);
			}

			// Constructs and returns the combo box
			JGraphEditorComboBox comboBox = new JGraphEditorComboBox(attrs,
					true);
			comboBox.addActionListener(new ComboBoxListener());
			comboBox.setFocusable(false);
			return comboBox;
		}

	}

	/**
	 * This class edits the selection of the focused graph according to the
	 * selected combo box item.
	 */
	public static class ComboBoxListener implements ActionListener {

		/**
		 * Redirects selection of a combo box item to changing the respective
		 * attrbiutes on the focused graph.
		 * 
		 * @param e
		 *            The object that describes the event.
		 */
		public void actionPerformed(ActionEvent e) {
			JComboBox sender = (JComboBox) e.getSource();

			// Gets focused component before showing dialogs.
			Component component = JGraphEditorAction.getPermanentFocusOwner();
			Object userObject = getSelection(sender);
			if (userObject instanceof Map) {

				// Edits cells in the focused graph
				if (component instanceof JGraph) {
					JGraph graph = (JGraph) component;
					graph.getGraphLayoutCache().edit(graph.getSelectionCells(),
							new Hashtable((Map) userObject));
				}
			}
		}

		/**
		 * Hook for subclassers to process the selection and return and map for
		 * the default implementation to process.
		 * 
		 * @param box
		 *            The combo box to return the selected item for.
		 * @return Returns the selected item in <code>box</code>.
		 */
		protected Object getSelection(JComboBox box) {
			return box.getSelectedItem();
		}

	}

}