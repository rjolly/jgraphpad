/* 
 * $Id: JGraphEditorDiagramPane.java,v 1.7 2005/08/07 13:25:43 gaudenz Exp $
 * Copyright (c) 2001-2005, Gaudenz Alder
 * 
 * All rights reserved.
 * 
 * See LICENSE file for license details. If you are unable to locate
 * this file please contact info (at) jgraph (dot) com.
 */
package com.jgraph.editor.factory;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.Stroke;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.ImageIcon;
import javax.swing.JScrollPane;
import javax.swing.JViewport;
import javax.swing.RepaintManager;

import org.jgraph.JGraph;
import org.jgraph.event.GraphLayoutCacheEvent;
import org.jgraph.event.GraphLayoutCacheListener;
import org.jgraph.event.GraphModelEvent;
import org.jgraph.event.GraphModelListener;
import org.jgraph.graph.GraphUndoManager;

import com.jgraph.editor.JGraphEditorDiagram;

/**
 * Wrapper panel for a diagram/JGraph-pair that implements automatic sizing,
 * backgrounds, printing and undo support. When wrapped in a scrollpane this
 * panel adds rulers to the enclosing scrollpane. Furthermore, it automatically
 * sets the minimum size and scale of the graph based on its settings.
 */
public class JGraphEditorDiagramPane extends JScrollPane implements Printable {

	/**
	 * Specifies the default page scale. Default is 1.5
	 */
	public static final double DEFAULT_PAGESCALE = 1.5;

	/**
	 * Specifies the size of the undo history. Default is 100.
	 */
	public static final int DEFAULT_HISTORYSIZE = 100;

	/**
	 * Specifies the default unit system. Default is metric.
	 */
	public static final boolean DEFAULT_ISMETRIC = true;

	/**
	 * Defines the no autoscaling policy.
	 */
	public static final int AUTOSCALE_POLICY_NONE = 0;

	/**
	 * Defines the window-size autoscaling policy.
	 */
	public static final int AUTOSCALE_POLICY_WINDOW = 1;

	/**
	 * Defines the page autoscaling policy.
	 */
	public static final int AUTOSCALE_POLICY_PAGE = 2;

	/**
	 * Defines the pagewidth autoscaling policy.
	 */
	public static final int AUTOSCALE_POLICY_PAGEWIDTH = 3;

	/**
	 * References the diagram this pane represents.
	 */
	protected JGraphEditorDiagram diagram;

	/**
	 * Background page format.
	 */
	protected PageFormat pageFormat = new PageFormat();

	/**
	 * Defines the scaling for the background page metrics. Default is
	 * {@link #DEFAULT_PAGESCALE}.
	 */
	protected double pageScale = DEFAULT_PAGESCALE;

	/**
	 * Holds the rulers to be used in a parent scrollpane.
	 */
	protected JGraphEditorRuler verticalRuler, horizontalRuler;

	/**
	 * Specifies if the rules should be visible. Default is true.
	 */
	protected boolean isRulersVisible = false;

	/**
	 * Specifies if the rulers should use metric units. Default is true.
	 */
	protected boolean isMetric = true;

	/**
	 * Specifies if the background page is visible. Default is true.
	 */
	protected boolean isPageVisible = true;

	/**
	 * Specified the autoscaling policy. Default is
	 * {@link #AUTOSCALE_POLICY_NONE}.
	 */
	protected int autoScalePolicy = AUTOSCALE_POLICY_NONE;

	/**
	 * Holds the background image.
	 */
	protected ImageIcon backgroundImage;

	/**
	 * Holds the undo manager for the graph.
	 */
	protected transient GraphUndoManager undoManager = new GraphUndoManager();

	/**
	 * References the inner graph.
	 */
	protected JGraph graph;

	/**
	 * Holds the autoscale reset listener to be used with the graph. This
	 * listener is used to reset the autoscale policy to none if the scale of
	 * the graph is changed manually, ie. <i>not</i> by means of automatic
	 * scaling.
	 */
	protected PropertyChangeListener autoScaleResetListener = createAutoScaleResetListener();

	/**
	 * Bound property names for the respective properties.
	 */
	public static String PROPERTY_METRIC = "metric",
			PROPERTY_PAGEVISIBLE = "pageVisible",
			PROPERTY_BACKGROUNDIMAGE = "backgroundImage",
			PROPERTY_RULERSVISIBLE = "rulersVisible",
			PROPERTY_PAGEFORMAT = "pageFormat",
			PROPERTY_AUTOSCALEPOLICY = "autoScalePolicy",
			PROPERTY_PAGESCALE = "pageScale";

	/**
	 * Constructs a new graph pane for the specified diagram and graph.
	 */
	public JGraphEditorDiagramPane(JGraphEditorDiagram diagram, JGraph graph) {
		super();
		this.diagram = diagram;
		this.graph = graph;
		setViewport(createViewport());
		getViewport().add(graph);
		setFocusable(false);
		undoManager.setLimit(DEFAULT_HISTORYSIZE);

		// Creates and initializes the rulers.
		createRulers();
		// In case the default changes to true
		setRulersVisible(isRulersVisible());

		// Installs undo support. Default history size is 100.
		graph.getModel().addUndoableEditListener(getGraphUndoManager());

		// Resets the autoscale policy if the is manually changed.
		graph.addPropertyChangeListener(autoScaleResetListener);

		// Install listener for adjusting min size and autoscaling
		graph.getModel().addGraphModelListener(new GraphModelListener() {
			public void graphChanged(GraphModelEvent e) {
				updateMinimumSize();
				updateScale();
			}
		});

		// Same for the layout cache
		graph.getGraphLayoutCache().addGraphLayoutCacheListener(
				new GraphLayoutCacheListener() {
					public void graphLayoutCacheChanged(GraphLayoutCacheEvent e) {
						updateMinimumSize();
						updateScale();
					}
				});

		// Installs listener for autoscaling.
		getViewport().addComponentListener(new ComponentAdapter() {
			public void componentResized(ComponentEvent arg0) {
				updateScale();
			}
		});

		// Initializes controlled graph properties.
		updateMinimumSize();
		updateScale();
	}

	/**
	 * Hook for subclassers to create the autoscale reset listener.
	 * 
	 * @return Returns a new autoscale reset listener.
	 */
	public PropertyChangeListener createAutoScaleResetListener() {
		return new PropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent evt) {
				if (evt.getPropertyName().equals(JGraph.SCALE_PROPERTY)
						&& getAutoScalePolicy() != AUTOSCALE_POLICY_NONE) {
					setAutoScalePolicy(AUTOSCALE_POLICY_NONE);
				}
			}
		};
	}

	/**
	 * Hook for subclassers to create the viewport.
	 * 
	 * @return Returns a new viewport to be used in the panel.
	 */
	protected JViewport createViewport() {
		return new Viewport();
	}

	/**
	 * Hook for subclassers to create the rulers. This implementation sets the
	 * {@link #horizontalRuler} and {@link #verticalRuler}.
	 */
	protected void createRulers() {
		horizontalRuler = new JGraphEditorRuler(graph,
				JGraphEditorRuler.ORIENTATION_HORIZONTAL);
		verticalRuler = new JGraphEditorRuler(graph,
				JGraphEditorRuler.ORIENTATION_VERTICAL);
	}

	/**
	 * Returns a {@link BufferedImage} for the graph using inset as an empty
	 * border around the cells of the graph. If bg is null then a transparent
	 * background is applied to the image, else the background is filled with
	 * the bg color. Therefore, one should only use a null background if the
	 * fileformat support transparency, eg. GIF and PNG. For JPG, you can use
	 * <code>Color.WHITE</code> for example. This implementation also takes
	 * into account potential background images.
	 * 
	 * @return Returns an image of the graph.
	 */
	public BufferedImage getImage(Color bg, int inset) {
		Object[] cells = graph.getRoots();
		Rectangle2D bounds = graph.getCellBounds(cells);
		if (bounds != null) {
			graph.toScreen(bounds);
			BufferedImage img = new BufferedImage((int) bounds.getWidth() + 2
					* inset, (int) bounds.getHeight() + 2 * inset,
					(bg != null) ? BufferedImage.TYPE_INT_RGB
							: BufferedImage.TYPE_INT_ARGB);
			Graphics2D graphics = img.createGraphics();
			if (backgroundImage != null) {
				graphics.drawImage(backgroundImage.getImage(), 0, 0, this);
			} else if (bg != null) {
				graphics.setColor(bg);
				graphics.fillRect(0, 0, img.getWidth(), img.getHeight());
			} else {
				graphics.setComposite(AlphaComposite.getInstance(
						AlphaComposite.CLEAR, 0.0f));
				graphics.fillRect(0, 0, img.getWidth(), img.getHeight());
				graphics.setComposite(AlphaComposite.SrcOver);
			}
			graphics.translate((int) (-bounds.getX() + inset), (int) (-bounds
					.getY() + inset));
			boolean tmp = isDoubleBuffered();
			RepaintManager currentManager = RepaintManager.currentManager(this);
			currentManager.setDoubleBufferingEnabled(false);
			graph.paint(graphics);
			currentManager.setDoubleBufferingEnabled(tmp);
			return img;
		}
		return null;
	}

	/**
	 * Returns the graph undo manager.
	 * 
	 * @return Returns the graphUndoManager.
	 */
	public GraphUndoManager getGraphUndoManager() {
		return undoManager;
	}

	/**
	 * Sets the graph undo manager.
	 * 
	 * @param undoManager
	 *            The graphUndoManager to set.
	 */
	public void setGraphUndoManager(GraphUndoManager undoManager) {
		this.undoManager = undoManager;
	}

	/**
	 * Returns the inner graph.
	 * 
	 * @return Returns the graph.
	 */
	public JGraph getGraph() {
		return graph;
	}

	/**
	 * Returns the diagram.
	 * 
	 * @return Returns the cacheNode.
	 */
	public JGraphEditorDiagram getDiagram() {
		return diagram;
	}

	/**
	 * Returns true if the rulers use metric units.
	 * 
	 * @return Returns the isMetric.
	 */
	public boolean isMetric() {
		return isMetric;
	}

	/**
	 * Specifies if the rulers should use metric units. Fires a property change
	 * event for {@link #PROPERTY_METRIC}.
	 * 
	 * @param isMetric
	 *            The isMetric to set.
	 */
	public void setMetric(boolean isMetric) {
		boolean oldValue = this.isMetric;
		this.isMetric = isMetric;
		getVerticalRuler().setMetric(isMetric);
		getHorizontalRuler().setMetric(isMetric);
		firePropertyChange(PROPERTY_METRIC, oldValue, isMetric);
	}

	/**
	 * Returns true if the rulers are to be displayed.
	 * 
	 * @return Returns the isRulersVisible.
	 */
	public boolean isRulersVisible() {
		return isRulersVisible;
	}

	/**
	 * Sets if the rulers are to be displayed. Fires a property change event for
	 * {@link #PROPERTY_RULERSVISIBLE}.
	 * 
	 * @param isRulersVisible
	 *            The isRulersVisible to set.
	 */
	public void setRulersVisible(boolean isRulersVisible) {
		boolean oldValue = this.isRulersVisible;
		this.isRulersVisible = isRulersVisible;
		if (isRulersVisible()) {
			setColumnHeaderView(getHorizontalRuler());
			setRowHeaderView(getVerticalRuler());
		} else {
			setColumnHeaderView(null);
			setRowHeaderView(null);
		}
		firePropertyChange(PROPERTY_RULERSVISIBLE, oldValue, isRulersVisible);
	}

	/**
	 * Returns the vertical ruler.
	 * 
	 * @return Returns the verticalRuler.
	 */
	public JGraphEditorRuler getVerticalRuler() {
		return verticalRuler;
	}

	/**
	 * Returns the horizontal ruler.
	 * 
	 * @return Returns the horizontalRuler.
	 */
	public JGraphEditorRuler getHorizontalRuler() {
		return horizontalRuler;
	}

	/**
	 * Returns the background image.
	 * 
	 * @return Returns the backgroundImage.
	 */
	public ImageIcon getBackgroundImage() {
		return backgroundImage;
	}

	/**
	 * Sets the background image. Fires a property change event for
	 * {@link #PROPERTY_BACKGROUNDIMAGE}.
	 * 
	 * @param backgroundImage
	 *            The backgroundImage to set.
	 */
	public void setBackgroundImage(ImageIcon backgroundImage) {
		ImageIcon oldValue = this.backgroundImage;
		this.backgroundImage = backgroundImage;
		firePropertyChange(PROPERTY_BACKGROUNDIMAGE, oldValue, backgroundImage);
	}

	/**
	 * Returns the autoscale policy.
	 * 
	 * @return Returns the autoScalePolicy.
	 */
	public int getAutoScalePolicy() {
		return autoScalePolicy;
	}

	/**
	 * Sets the autoscale policy. Possible values is one of:
	 * {@link #AUTOSCALE_POLICY_NONE},{@link #AUTOSCALE_POLICY_PAGE},
	 * {@link #AUTOSCALE_POLICY_PAGEWIDTH} or {@link #AUTOSCALE_POLICY_WINDOW}.
	 * Fires a property change event for {@link #PROPERTY_AUTOSCALEPOLICY}.
	 * 
	 * @param autoScalePolicy
	 *            The autoScalePolicy to set.
	 */
	public void setAutoScalePolicy(int autoScalePolicy) {
		int oldValue = this.autoScalePolicy;
		this.autoScalePolicy = autoScalePolicy;
		if (autoScalePolicy == AUTOSCALE_POLICY_NONE)
			getGraph().setScale(1.0);
		updateScale();
		firePropertyChange(PROPERTY_AUTOSCALEPOLICY, oldValue, autoScalePolicy);
	}

	/**
	 * Returns true if the background page is visible.
	 * 
	 * @return Returns the isPageVisible.
	 */
	public boolean isPageVisible() {
		return isPageVisible;
	}

	/**
	 * Sets if the background page should be visible.Fires a property change
	 * event for {@link #PROPERTY_PAGEVISIBLE}.
	 * 
	 * @param isPageVisible
	 *            The isPageVisible to set.
	 */
	public void setPageVisible(boolean isPageVisible) {
		boolean oldValue = this.isPageVisible;
		this.isPageVisible = isPageVisible;
		updateMinimumSize();
		updateScale();
		firePropertyChange(PROPERTY_PAGEVISIBLE, oldValue, isPageVisible);
	}

	/**
	 * Returns the page format of the background page.
	 * 
	 * @return Returns the pageFormat.
	 */
	public PageFormat getPageFormat() {
		return pageFormat;
	}

	/**
	 * Sets the page format of the background page.Fires a property change event
	 * for {@link #PROPERTY_PAGEFORMAT}.
	 * 
	 * @param pageFormat
	 *            The pageFormat to set.
	 */
	public void setPageFormat(PageFormat pageFormat) {
		Object oldValue = this.pageFormat;
		this.pageFormat = pageFormat;
		updateMinimumSize();
		firePropertyChange(PROPERTY_PAGEFORMAT, oldValue, pageFormat);
	}

	/**
	 * Returns the scale of the page metrics.
	 * 
	 * @return Returns the pageScale.
	 */
	public double getPageScale() {
		return pageScale;
	}

	/**
	 * Sets the scale of the page metrics.Fires a property change event for
	 * {@link #PROPERTY_PAGESCALE}.
	 * 
	 * @param pageScale
	 *            The pageScale to set.
	 */
	public void setPageScale(double pageScale) {
		double oldValue = this.pageScale;
		this.pageScale = pageScale;
		firePropertyChange(PROPERTY_PAGESCALE, oldValue, pageScale);
	}

	/**
	 * Updates the minimum size of the graph according to the current state of
	 * the background page: if the page is not visible then the minimum size is
	 * set to <code>null</code>, otherwise the minimum size is set to the
	 * smallest area of pages containing the graph.
	 */
	protected void updateMinimumSize() {
		if (isPageVisible() && pageFormat != null) {
			Rectangle2D bounds = graph.getCellBounds(graph.getRoots());
			Dimension size = (bounds != null) ? new Dimension((int) (bounds
					.getX() + bounds.getWidth()), (int) (bounds.getY() + bounds
					.getHeight())) : new Dimension(1, 1);
			int w = (int) (pageFormat.getWidth() * pageScale);
			int h = (int) (pageFormat.getHeight() * pageScale);
			int cols = (int) Math.ceil((double) (size.width - 5) / (double) w);
			int rows = (int) Math.ceil((double) (size.height - 5) / (double) h);
			size = new Dimension(Math.max(cols, 1) * w + 5, Math.max(rows, 1)
					* h + 5);
			graph.setMinimumSize(size);

			// Updates the active region in the rulers.
			horizontalRuler.setActiveOffset((int) (pageFormat.getImageableX()));
			verticalRuler.setActiveOffset((int) (pageFormat.getImageableY()));
			horizontalRuler.setActiveLength((int) (pageFormat
					.getImageableWidth()));
			verticalRuler.setActiveLength((int) (pageFormat
					.getImageableHeight()));
		} else {
			graph.setMinimumSize(null);
		}
		graph.revalidate();
	}

	/**
	 * Updates the scale based on the autoscale policy. This implementation
	 * makes sure the {@link #autoScaleResetListener} does not trigger an
	 * autoscale policy reset be temporary removing the listener from the graph
	 * while the scale is updated. The scale is computed using one of
	 * {@link #computeWindowScale(int)}, {@link #computePageScale()} and
	 * {@link #computePageWidthScale(int)}.
	 */
	protected void updateScale() {
		double scale = 0;
		if (getAutoScalePolicy() == AUTOSCALE_POLICY_WINDOW)
			scale = computeWindowScale(10);
		else if (getAutoScalePolicy() == AUTOSCALE_POLICY_PAGE)
			scale = computePageScale();
		else if (getAutoScalePolicy() == AUTOSCALE_POLICY_PAGEWIDTH)
			scale = computePageWidthScale(20);
		if (scale > 0) {
			graph.removePropertyChangeListener(autoScaleResetListener);
			graph.setScale(Math.max(Math.min(scale, 128), .01));
			graph.addPropertyChangeListener(autoScaleResetListener);
		}
	}

	/**
	 * Computes the scale for the window autoscale policy.
	 * 
	 * @param border
	 *            The border to use.
	 * @return Returns the scale to use for the graph.
	 */
	protected double computeWindowScale(int border) {
		Dimension size = getViewport().getExtentSize();
		Rectangle2D p = getGraph().getCellBounds(getGraph().getRoots());
		if (p != null) {
			return Math.min((double) size.getWidth()
					/ (p.getX() + p.getWidth() + border), (double) size
					.getHeight()
					/ (p.getY() + p.getHeight() + border));
		}
		return 0;
	}

	/**
	 * Computes the scale for the page autoscale policy.
	 * 
	 * @return Returns the scale to use for the graph.
	 */
	protected double computePageScale() {
		Dimension size = getViewport().getExtentSize();
		Dimension p = getGraph().getMinimumSize();
		if (p != null && (p.getWidth() != 0 || p.getHeight() != 0)) {
			return Math.min((double) size.getWidth() / (double) p.getWidth(),
					(double) size.getHeight() / (double) p.getHeight());
		}
		return 0;
	}

	/**
	 * Computes the scale for the pagewidth autoscale policy.
	 * 
	 * @param border
	 *            The border to use.
	 * @return Returns the scale to use for the graph.
	 */
	protected double computePageWidthScale(int border) {
		Dimension size = getViewport().getExtentSize();
		Dimension p = getGraph().getMinimumSize();
		if (p != null && (p.getWidth() != 0 || p.getHeight() != 0)) {
			size.width = size.width - border;
			return (double) size.getWidth() / (double) p.getWidth();
		}
		return 0;
	}

	/**
	 * Prints the specified page on the specified graphics using
	 * <code>pageForm</code> for the page format.
	 * 
	 * @param g
	 *            The graphics to paint the graph on.
	 * @param printFormat
	 *            The page format to use for printing.
	 * @param page
	 *            The page to print
	 * @return Returns {@link Printable#PAGE_EXISTS} or
	 *         {@link Printable#NO_SUCH_PAGE}.
	 */
	public int print(Graphics g, PageFormat printFormat, int page) {
		Dimension pSize = graph.getPreferredSize();
		int w = (int) (printFormat.getWidth() * pageScale);
		int h = (int) (printFormat.getHeight() * pageScale);
		int cols = (int) Math.max(Math.ceil((double) (pSize.width - 5)
				/ (double) w), 1);
		int rows = (int) Math.max(Math.ceil((double) (pSize.height - 5)
				/ (double) h), 1);
		if (page < cols * rows) {

			// Configures graph for printing
			getGraph().removePropertyChangeListener(autoScaleResetListener);
			RepaintManager currentManager = RepaintManager.currentManager(this);
			currentManager.setDoubleBufferingEnabled(false);
			double oldScale = getGraph().getScale();
			getGraph().setScale(1 / pageScale);
			int dx = (int) ((page % cols) * printFormat.getWidth());
			int dy = (int) ((page % rows) * printFormat.getHeight());
			g.translate(-dx, -dy);
			g.setClip(dx, dy, (int) (dx + printFormat.getWidth()),
					(int) (dy + printFormat.getHeight()));

			// Prints the graph on the graphics.
			getGraph().paint(g);

			// Restores graph
			g.translate(dx, dy);
			graph.setScale(oldScale);
			getGraph().addPropertyChangeListener(autoScaleResetListener);
			currentManager.setDoubleBufferingEnabled(true);
			return PAGE_EXISTS;
		} else {
			return NO_SUCH_PAGE;
		}
	}

	/**
	 * Viewport for diagram panes that is in charge of painting the background
	 * image or page.
	 */
	public class Viewport extends JViewport {

		/**
		 * Paints the background.
		 * 
		 * @param g
		 *            The graphics object to paint the background on.
		 */
		public void paint(Graphics g) {
			if (isPageVisible())
				paintBackgroundPages((Graphics2D) g);
			else
				setBackground(graph.getBackground());
			if (getBackgroundImage() != null)
				paintBackgroundImage((Graphics2D) g);
			setOpaque(!isPageVisible() && getBackgroundImage() == null);
			super.paint(g);
			setOpaque(true);
		}

		/**
		 * Hook for subclassers to paint the background image.
		 * 
		 * @param g2
		 *            The graphics object to paint the image on.
		 */
		protected void paintBackgroundImage(Graphics2D g2) {
			// Clears the background
			if (!isPageVisible()) {
				g2.setColor(graph.getBackground());
				g2.fillRect(0, 0, graph.getWidth(), graph.getHeight());
			}
			// Paints the image
			AffineTransform tmp = g2.getTransform();
			Point offset = getViewPosition();
			g2.translate(-offset.x, -offset.y);
			g2.scale(graph.getScale(), graph.getScale());
			Image img = getBackgroundImage().getImage();
			g2.drawImage(img, 0, 0, graph);
			g2.setTransform(tmp);
		}

		/**
		 * Hook for subclassers to paint the background page(s).
		 * 
		 * @param g2
		 *            The graphics object to paint the background page(s) on.
		 */
		protected void paintBackgroundPages(Graphics2D g2) {
			Point2D p = graph.toScreen(new Point2D.Double(
					pageFormat.getWidth(), pageFormat.getHeight()));
			Dimension pSize = graph.getPreferredSize();
			int w = (int) (p.getX() * pageScale);
			int h = (int) (p.getY() * pageScale);
			int cols = (int) Math.max(Math.ceil((double) (pSize.width - 5)
					/ (double) w), 1);
			int rows = (int) Math.max(Math.ceil((double) (pSize.height - 5)
					/ (double) h), 1);
			g2.setColor(graph.getHandleColor());

			// Draws the pages.
			Point offset = getViewPosition();
			g2.translate(-offset.x, -offset.y);
			g2.fillRect(0, 0, graph.getWidth(), graph.getHeight());
			g2.setColor(Color.darkGray);
			g2.fillRect(3, 3, cols * w, rows * h);
			g2.setColor(getGraph().getBackground());
			g2.fillRect(1, 1, cols * w - 1, rows * h - 1);

			// Draws the pagebreaks.
			Stroke previousStroke = g2.getStroke();
			g2.setStroke(new BasicStroke(1, BasicStroke.CAP_BUTT,
					BasicStroke.JOIN_MITER, 10.0f, new float[] { 1, 2 }, 0));
			g2.setColor(Color.darkGray);
			for (int i = 1; i < cols; i++)
				g2.drawLine(i * w, 1, i * w, rows * h - 1);
			for (int i = 1; i < rows; i++)
				g2.drawLine(1, i * h, cols * w - 1, i * h);

			// Restores the graphics.
			g2.setStroke(previousStroke);
			g2.translate(offset.x, offset.y);
			g2.clipRect(0, 0, cols * w - 1 - offset.x, rows * h - 1 - offset.y);
		}

	}

	/**
	 * Returns the parent diagram pane of the specified component, or the
	 * component itself if it is a editor diagram pane.
	 * 
	 * @return Returns the parent editor diagram pane of <code>component</code>.
	 */
	public static JGraphEditorDiagramPane getParentDiagramPane(
			Component component) {
		while (component != null) {
			if (component instanceof JGraphEditorDiagramPane)
				return (JGraphEditorDiagramPane) component;
			component = component.getParent();
		}
		return null;
	}

}