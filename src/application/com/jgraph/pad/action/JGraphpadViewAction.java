/* 
 * $Id: JGraphpadViewAction.java,v 1.5 2005/08/07 10:28:29 gaudenz Exp $
 * Copyright (c) 2001-2005, Gaudenz Alder
 * 
 * All rights reserved.
 * 
 * See LICENSE file for license details. If you are unable to locate
 * this file please contact info (at) jgraph (dot) com.
 */
package com.jgraph.pad.action;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;

import org.jgraph.JGraph;
import org.jgraph.graph.GraphLayoutCache;
import org.jgraph.graph.PortView;

import com.jgraph.JGraphEditor;
import com.jgraph.editor.JGraphEditorAction;
import com.jgraph.editor.factory.JGraphEditorDiagramPane;
import com.jgraph.pad.dialog.JGraphpadDialogs;
import com.jgraph.pad.factory.JGraphpadLibraryPane;
import com.jgraph.pad.graph.JGraphpadVertexRenderer;
import com.jgraph.pad.util.JGraphpadImageIcon;

/**
 * Implements all actions of the view menu. The layout item is added by a the
 * layout plugin.
 */
public class JGraphpadViewAction extends JGraphEditorAction {

	/**
	 * Shortcut to the shared JGraphpad dialogs.
	 */
	private static JGraphpadDialogs dlgs = JGraphpadDialogs.getSharedInstance();

	/**
	 * Specifies the name for the <code>togglerRulers</code> action.
	 */
	public static final String NAME_TOGGLERULERS = "toggleRulers";

	/**
	 * Specifies the name for the <code>togglePorts</code> action.
	 */
	public static final String NAME_TOGGLEPORTS = "togglePorts";

	/**
	 * Specifies the name for the <code>handleColor</code> action.
	 */
	public static final String NAME_HANDLECOLOR = "handleColor";

	/**
	 * Specifies the name for the <code>handleSize</code> action.
	 */
	public static final String NAME_LOCKEDHANDLECOLOR = "lockedHandleColor";

	/**
	 * Specifies the name for the <code>handleColor</code> action.
	 */
	public static final String NAME_MARQUEECOLOR = "marqueeColor";

	/**
	 * Specifies the name for the <code>handleSize</code> action.
	 */
	public static final String NAME_HANDLESIZE = "handleSize";

	/**
	 * Specifies the name for the <code>toggleGrid</code> action.
	 */
	public static final String NAME_TOGGLEGRID = "toggleGrid";

	/**
	 * Specifies the name for the <code>gridColor</code> action.
	 */
	public static final String NAME_GRIDCOLOR = "gridColor";

	/**
	 * Specifies the name for the <code>switchDotGrid</code> action.
	 */
	public static final String NAME_SWITCHDOTGRID = "switchDotGrid";

	/**
	 * Specifies the name for the <code>switchDotGrid</code> action.
	 */
	public static final String NAME_SWITCHLINEGRID = "switchLineGrid";

	/**
	 * Specifies the name for the <code>switchDotGrid</code> action.
	 */
	public static final String NAME_SWITCHCROSSGRID = "switchCrossGrid";

	/**
	 * Specifies the name for the <code>toggleAntiAlias</code> action.
	 */
	public static final String NAME_TOGGLEANTIALIAS = "toggleAntiAlias";

	/**
	 * Specifies the name for the <code>toggleRootHandles</code> action.
	 */
	public static final String NAME_TOGGLEROOTHANDLES = "toggleRootHandles";

	/**
	 * Specifies the name for the <code>toggleDragEnabled</code> action.
	 */
	public static final String NAME_TOGGLEDRAGENABLED = "toggleDragEnabled";

	/**
	 * Specifies the name for the <code>fitNone</code> action.
	 */
	public static final String NAME_FITNONE = "fitNone";

	/**
	 * Specifies the name for the <code>fitPage</code> action.
	 */
	public static final String NAME_FITPAGE = "fitPage";

	/**
	 * Specifies the name for the <code>fitWindow</code> action.
	 */
	public static final String NAME_FITWINDOW = "fitWindow";

	/**
	 * Specifies the name for the <code>fitWidth</code> action.
	 */
	public static final String NAME_FITWIDTH = "fitWidth";

	/**
	 * Specifies the name for the <code>togglePage</code> action.
	 */
	public static final String NAME_TOGGLEPAGE = "togglePage";

	/**
	 * Specifies the name for the <code>background</code> action.
	 */
	public static final String NAME_BACKGROUND = "background";

	/**
	 * Specifies the name for the <code>backgroundImage</code> action.
	 */
	public static final String NAME_BACKGROUNDIMAGE = "backgroundImage";

	/**
	 * Specifies the name for the <code>backgroundImageURL</code> action.
	 */
	public static final String NAME_BACKGROUNDIMAGEURL = "backgroundImageURL";

	/**
	 * Specifies the name for the <code>clearBackground</code> action.
	 */
	public static final String NAME_CLEARBACKGROUND = "clearBackground";

	/**
	 * Specifies the name for the <code>zoomActual</code> action.
	 */
	public static final String NAME_ZOOMACTUAL = "zoomActual";

	/**
	 * Specifies the name for the <code>zoomCustom</code> action.
	 */
	public static final String NAME_ZOOMCUSTOM = "zoomCustom";

	/**
	 * Specifies the name for the <code>zoomIn</code> action.
	 */
	public static final String NAME_ZOOMIN = "zoomIn";

	/**
	 * Specifies the name for the <code>zoomOut</code> action.
	 */
	public static final String NAME_ZOOMOUT = "zoomOut";

	/**
	 * Specifies the name for the <code>tolerance</code> action.
	 */
	public static final String NAME_TOLERANCE = "tolerance";

	/**
	 * Specifies the name for the <code>portSize</code> action.
	 */
	public static final String NAME_PORTSIZE = "portSize";

	/**
	 * Specifies the name for the <code>gridSize</code> action.
	 */
	public static final String NAME_GRIDSIZE = "gridSize";

	/**
	 * Specifies the name for the <code>toggleMetric</code> action.
	 */
	public static final String NAME_TOGGLEMETRIC = "toggleMetric";

	/**
	 * Specifies the name for the <code>toggleEditable</code> action.
	 */
	public static final String NAME_TOGGLEEDITABLE = "toggleEditable";

	/**
	 * Specifies the name for the <code>editClickCount</code> action.
	 */
	public static final String NAME_EDITCLICKCOUNT = "editClickCount";

	/**
	 * Specifies the name for the <code>toggleDisconnectOnMove</code> action.
	 */
	public static final String NAME_TOGGLEDISCONNECTONMOVE = "toggleDisconnectOnMove";

	/**
	 * Specifies the name for the <code>toggleMoveBelowZero</code> action.
	 */
	public static final String NAME_TOGGLEMOVEBELOWZERO = "toggleMoveBelowZero";

	/**
	 * Specifies the name for the <code>toggleGraphCloneable</code> action.
	 */
	public static final String NAME_TOGGLEGRAPHCLONEABLE = "toggleGraphCloneable";

	/**
	 * Specifies the name for the <code>toggleGraphMoveable</code> action.
	 */
	public static final String NAME_TOGGLEGRAPHMOVEABLE = "toggleGraphMoveable";

	/**
	 * Specifies the name for the <code>toggleGraphSizeable</code> action.
	 */
	public static final String NAME_TOGGLEGRAPHSIZEABLE = "toggleGraphSizeable";

	/**
	 * Specifies the name for the <code>toggleGraphBendable</code> action.
	 */
	public static final String NAME_TOGGLEGRAPHBENDABLE = "toggleGraphBendable";

	/**
	 * Specifies the name for the <code>toggleGraphConnectable</code> action.
	 */
	public static final String NAME_TOGGLEGRAPHCONNECTABLE = "toggleGraphConnectable";

	/**
	 * Specifies the name for the <code>toggleGraphDisconnectable</code>
	 * action.
	 */
	public static final String NAME_TOGGLEGRAPHDISCONNECTABLE = "toggleGraphDisconnectable";

	/**
	 * Specifies the name for the <code>toggleSelectsLocalInsertedCells</code>
	 * action.
	 */
	public static final String NAME_TOGGLESELECTSLOCALINSERTEDCELLS = "toggleSelectsLocalInsertedCells";

	/**
	 * Specifies the name for the <code>toggleSelectsAllInsertedCells</code>
	 * action.
	 */
	public static final String NAME_TOGGLESELECTSALLINSERTEDCELLS = "toggleSelectsAllInsertedCells";

	/**
	 * Specifies the name for the <code>toggleShowsExistingConnections</code>
	 * action.
	 */
	public static final String NAME_TOGGLESHOWSEXISTINGCONNECTIONS = "toggleShowsExistingConnections";

	/**
	 * Specifies the name for the <code>toggleShowsInsertedConnections</code>
	 * action.
	 */
	public static final String NAME_TOGGLESHOWSINSERTEDCONNECTIONS = "toggleShowsInsertedConnections";

	/**
	 * Specifies the name for the <code>toggleShowsChangedConnections</code>
	 * action.
	 */
	public static final String NAME_TOGGLESHOWSCHANGEDCONNECTIONS = "toggleShowsChangedConnections";

	/**
	 * Specifies the name for the <code>toggleHidesExistingConnections</code>
	 * action.
	 */
	public static final String NAME_TOGGLEHIDESEXISTINGCONNECTIONS = "toggleHidesExistingConnections";

	/**
	 * Specifies the name for the <code>toggleHidesDanglingConnections</code>
	 * action.
	 */
	public static final String NAME_TOGGLEHIDESDANGLINGCONNECTIONS = "toggleHidesDanglingConnections";

	/**
	 * Specifies the name for the <code>toggleRemembersCellViews</code>
	 * action.
	 */
	public static final String NAME_TOGGLEREMEMBERSCELLVIEWS = "toggleRemembersCellViews";

	/**
	 * Specifies the name for the <code>toggleDoubleBuffered</code> action.
	 */
	public static final String NAME_TOGGLEDOUBLEBUFFERED = "toggleDoubleBuffered";

	/**
	 * Specifies the name for the <code>togglePortsScaled</code> action.
	 */
	public static final String NAME_TOGGLEPORTSSCALED = "togglePortsScaled";

	/**
	 * Specifies the name for the <code>toggleJumpsToDefaultPort</code>
	 * action.
	 */
	public static final String NAME_TOGGLEJUMPSTODEFAULTPORT = "toggleJumpsToDefaultPort";

	/**
	 * Specifies the name for the <code>toggleMovesIntoGroups</code> action.
	 */
	public static final String NAME_TOGGLEMOVESINTOGROUPS = "toggleMovesIntoGroups";

	/**
	 * Specifies the name for the <code>toggleMovesOutOfGroups</code> action.
	 */
	public static final String NAME_TOGGLEMOVESOUTOFGROUPS = "toggleMovesOutOfGroups";

	/**
	 * Specifies the name for the <code>libraryLarger</code> action.
	 */
	public static final String NAME_LIBRARYLARGER = "libraryLarger";

	/**
	 * Specifies the name for the <code>librarySmaller</code> action.
	 */
	public static final String NAME_LIBRARYSMALLER = "librarySmaller";

	/**
	 * Holds the last directory for file operations.
	 */
	protected File lastDirectory = null;

	/**
	 * Constructs a new view action for the specified name. If the action name
	 * starts with <code>toggle</code>, <code>fit</code> or
	 * <code>switch</code> then the action is configured to be a toggle
	 * action.
	 * 
	 * @param name
	 *            The name of the action to be created.
	 */
	public JGraphpadViewAction(String name) {
		super(name);
		setToggleAction(name.startsWith("toggle") || name.startsWith("fit")
				|| name.startsWith("switch"));
	}

	/**
	 * Executes the action based on the action name.
	 * 
	 * @param event
	 *            The object that describes the event.
	 */
	public void actionPerformed(ActionEvent event) {
		try {

			// Actions that require a focused graph
			JGraph graph = getPermanentFocusOwnerGraph();
			if (graph != null) {
				GraphLayoutCache cache = graph.getGraphLayoutCache();
				if (getName().equals(NAME_TOGGLEDRAGENABLED))
					graph.setDragEnabled(!graph.isDragEnabled());
				else if (getName().equals(NAME_TOGGLEPORTS))
					graph.setPortsVisible(!graph.isPortsVisible());
				else if (getName().equals(NAME_TOGGLEANTIALIAS))
					graph.setAntiAliased(!graph.isAntiAliased());
				else if (getName().equals(NAME_TOGGLEROOTHANDLES)) {

					// ToggleRootHandles is implemented as a client property
					// stored in JGraph and used in JGraphpadVertexRenderer.
					String key = JGraphpadVertexRenderer.CLIENTPROPERTY_SHOWFOLDINGICONS;
					Boolean b = (Boolean) graph.getClientProperty(key);
					if (b == null)
						b = new Boolean(true);
					graph
							.putClientProperty(key, new Boolean(!b
									.booleanValue()));
					graph.repaint();
				} else if (getName().equals(NAME_TOGGLEEDITABLE))
					graph.setEditable(!graph.isEditable());
				else if (getName().equals(NAME_TOGGLEDOUBLEBUFFERED))
					graph.setDoubleBuffered(!graph.isDoubleBuffered());
				else if (getName().equals(NAME_TOGGLEDISCONNECTONMOVE))
					graph.setDisconnectOnMove(!graph.isDisconnectOnMove());
				else if (getName().equals(NAME_TOGGLEMOVEBELOWZERO))
					graph.setMoveBelowZero(!graph.isMoveBelowZero());
				else if (getName().equals(NAME_TOGGLEGRAPHCLONEABLE))
					graph.setCloneable(!graph.isCloneable());
				else if (getName().equals(NAME_TOGGLEGRAPHMOVEABLE))
					graph.setMoveable(!graph.isMoveable());
				else if (getName().equals(NAME_TOGGLEGRAPHSIZEABLE))
					graph.setSizeable(!graph.isSizeable());
				else if (getName().equals(NAME_TOGGLEGRAPHBENDABLE))
					graph.setBendable(!graph.isBendable());
				else if (getName().equals(NAME_TOGGLEGRAPHCONNECTABLE))
					graph.setConnectable(!graph.isConnectable());
				else if (getName().equals(NAME_TOGGLEGRAPHDISCONNECTABLE))
					graph.setDisconnectable(!graph.isDisconnectable());
				else if (getName().equals(NAME_TOGGLESELECTSLOCALINSERTEDCELLS))
					cache.setSelectsLocalInsertedCells(!cache
							.isSelectsLocalInsertedCells());
				else if (getName().equals(NAME_TOGGLESELECTSALLINSERTEDCELLS))
					cache.setSelectsAllInsertedCells(!cache
							.isSelectsAllInsertedCells());
				else if (getName().equals(NAME_TOGGLESHOWSEXISTINGCONNECTIONS))
					cache.setShowsExistingConnections(!cache
							.isShowsExistingConnections());
				else if (getName().equals(NAME_TOGGLESHOWSINSERTEDCONNECTIONS))
					cache.setShowsInsertedConnections(!cache
							.isShowsInsertedConnections());
				else if (getName().equals(NAME_TOGGLESHOWSCHANGEDCONNECTIONS))
					cache.setShowsChangedConnections(!cache
							.isShowsChangedConnections());
				else if (getName().equals(NAME_TOGGLEHIDESEXISTINGCONNECTIONS))
					cache.setHidesExistingConnections(!cache
							.isHidesExistingConnections());
				else if (getName().equals(NAME_TOGGLEHIDESDANGLINGCONNECTIONS))
					cache.setHidesDanglingConnections(!cache
							.isHidesDanglingConnections());
				else if (getName().equals(NAME_TOGGLEREMEMBERSCELLVIEWS))
					cache.setRemembersCellViews(!cache.isRemembersCellViews());
				else if (getName().equals(NAME_TOGGLEJUMPSTODEFAULTPORT))
					graph.setJumpToDefaultPort(!graph.isJumpToDefaultPort());
				else if (getName().equals(NAME_TOGGLEPORTSSCALED))
					graph.setPortsScaled(!graph.isPortsScaled());
				else if (getName().equals(NAME_TOGGLEMOVESINTOGROUPS))
					graph.setMoveIntoGroups(!graph.isMoveIntoGroups());
				else if (getName().equals(NAME_TOGGLEMOVESOUTOFGROUPS))
					graph.setMoveOutOfGroups(!graph.isMoveOutOfGroups());
				else if (getName().equals(NAME_TOGGLEGRID)) {
					graph.setGridEnabled(!graph.isGridEnabled());
					graph.setGridVisible(graph.isGridEnabled());
				} else if (getName().equals(NAME_ZOOMACTUAL))
					doZoom(graph, false, false, false);
				else if (getName().equals(NAME_ZOOMIN))
					doZoom(graph, true, false, false);
				else if (getName().equals(NAME_ZOOMOUT))
					doZoom(graph, false, true, false);
				else if (getName().equals(NAME_ZOOMCUSTOM))
					doZoom(graph, false, false, true);
				else if (getName().equals(NAME_TOLERANCE))
					graph.setTolerance(dlgs.intDialog(
							getString("EnterTolerance"), graph.getTolerance(),
							false, true));
				else if (getName().equals(NAME_PORTSIZE)) {
					PortView.SIZE = dlgs.intDialog(getString("EnterPortSize"),
							PortView.SIZE, false, true);
					graph.repaint();
				} else if (getName().equals(NAME_GRIDSIZE))
					graph.setGridSize(dlgs.doubleDialog(
							getString("EnterGridSize"), graph.getGridSize(),
							false, false));
				else if (getName().equals(NAME_GRIDCOLOR))
					graph.setGridColor(dlgs.colorDialog(graph,
							getString("SelectColor"), graph.getGridColor()));
				else if (getName().equals(NAME_HANDLECOLOR))
					graph.setHandleColor(dlgs.colorDialog(graph,
							getString("SelectColor"), graph.getHandleColor()));
				else if (getName().equals(NAME_HANDLESIZE))
					graph.setHandleSize(dlgs.intDialog(
							getString("EnterNumber"), graph.getHandleSize(),
							false, false));
				else if (getName().equals(NAME_LOCKEDHANDLECOLOR))
					graph.setLockedHandleColor(dlgs.colorDialog(graph,
							getString("SelectColor"), graph
									.getLockedHandleColor()));
				else if (getName().equals(NAME_MARQUEECOLOR))
					graph.setMarqueeColor(dlgs.colorDialog(graph,
							getString("SelectColor"), graph.getMarqueeColor()));
				else if (getName().equals(NAME_EDITCLICKCOUNT))
					graph.setEditClickCount(dlgs.intDialog(
							getString("EnterClickCount"), graph
									.getEditClickCount(), false, false));
				else if (getName().equals(NAME_SWITCHDOTGRID))
					graph.setGridMode(JGraph.DOT_GRID_MODE);
				else if (getName().equals(NAME_SWITCHLINEGRID))
					graph.setGridMode(JGraph.LINE_GRID_MODE);
				else if (getName().equals(NAME_SWITCHCROSSGRID))
					graph.setGridMode(JGraph.CROSS_GRID_MODE);
			}

			// Actions that require a focused diagram pane
			JGraphEditorDiagramPane diagramPane = getPermanentFocusOwnerDiagramPane();
			if (diagramPane != null) {
				if (getName().equals(NAME_TOGGLERULERS))
					diagramPane
							.setRulersVisible(!diagramPane.isRulersVisible());
				else if (getName().equals(NAME_TOGGLEPAGE))
					diagramPane.setPageVisible(!diagramPane.isPageVisible());
				else if (getName().equals(NAME_TOGGLEMETRIC))
					diagramPane.setMetric(!diagramPane.isMetric());
				else if (getName().equals(NAME_BACKGROUND))
					doBackground(diagramPane);
				else if (getName().equals(NAME_BACKGROUNDIMAGE))
					doBackgroundImage(diagramPane, dlgs.imageFileDialog(
							diagramPane, getString("SelectImage"), true,
							lastDirectory));
				else if (getName().equals(NAME_BACKGROUNDIMAGEURL))
					doBackgroundImage(diagramPane, dlgs
							.valueDialog(getString("EnterURL")));
				else if (getName().equals(NAME_CLEARBACKGROUND)) {
					diagramPane.setBackgroundImage(null);
					diagramPane.repaint();
				} else if (getName().equals(NAME_FITNONE))
					diagramPane
							.setAutoScalePolicy(JGraphEditorDiagramPane.AUTOSCALE_POLICY_NONE);
				else if (getName().equals(NAME_FITPAGE))
					diagramPane
							.setAutoScalePolicy(JGraphEditorDiagramPane.AUTOSCALE_POLICY_PAGE);
				else if (getName().equals(NAME_FITWIDTH))
					diagramPane
							.setAutoScalePolicy(JGraphEditorDiagramPane.AUTOSCALE_POLICY_PAGEWIDTH);
				else if (getName().equals(NAME_FITWINDOW))
					diagramPane
							.setAutoScalePolicy(JGraphEditorDiagramPane.AUTOSCALE_POLICY_WINDOW);
			}

			// Actions that require a focused library
			JGraphpadLibraryPane libraryPane = JGraphpadFileAction
					.getPermanentFocusOwnerLibraryPane();
			if (libraryPane != null) {
				if (getName().equals(NAME_LIBRARYLARGER))
					doScaleLibraryEntries(libraryPane, true);
				else if (getName().equals(NAME_LIBRARYSMALLER))
					doScaleLibraryEntries(libraryPane, false);
			}
		} catch (JGraphpadDialogs.CancelException e) {
			// ignore
		} catch (Exception e) {
			dlgs.errorDialog(getPermanentFocusOwner(), e.getMessage());
		}
	}

	/**
	 * Doubles or halves the current size of the entries in the specified
	 * library pane based on <code>larger</code>.
	 * 
	 * @param pane
	 *            The layout pane to change the entries of.
	 * @param larger
	 *            Whether to make the entries larger.
	 */
	protected void doScaleLibraryEntries(JGraphpadLibraryPane pane,
			boolean larger) {
		int w = pane.getEntrywidth();
		int h = pane.getEntryheight();
		if (larger && w < 800) {
			w *= 2;
			h *= 2;
		} else if (!larger && w > 4) {
			w /= 2;
			h /= 2;
		}
		pane.setEntrywidth(w);
		pane.setEntryheight(h);
		pane.invalidate();
		pane.repaint();
	}

	/**
	 * Sets the zoom on the specified graph depending on the paramters, which
	 * are interpreted to be exlusive. If <code>custom</code> is true this
	 * method displays a dialog to enter the custom zoom and sets the zoom to
	 * the specified value. If custom is false then the method will double or
	 * half the current zoom of <code>graph</code> depending on the value of
	 * <code>in</code> and <code>out</code>. If all values are false then
	 * the zoom is reset to 1.<br>
	 * This implementation makes sure the zoom is between 0.01 and 300 and
	 * scrolls to the selection cell if the selection is not empty.
	 * 
	 * @param graph
	 *            The graph to perform the operation in.
	 * @param in
	 *            Whether to zoom in if no custom dialog is used.
	 * @param out
	 *            Whether to zoom out if no custom dialog is used.
	 * @param custom
	 *            Whether to display a dialog to ask for a custom zoom.
	 */
	protected void doZoom(JGraph graph, boolean in, boolean out, boolean custom) {
		double scale = 1;
		if (custom) {
			String value = dlgs.valueDialog(getString("EnterZoom"), String
					.valueOf(graph.getScale() * 100));
			if (value != null && value.length() > 0) {
				if (value.endsWith("%"))
					value = value.substring(0, value.length() - 1);
				scale = Double.parseDouble(value) / 100;
			} else
				scale = -1;
		} else if (in)
			scale = graph.getScale() * 2;
		else if (out)
			scale = graph.getScale() / 2;
		if (scale > 0.01 && scale < 300) {
			graph.setScale(scale);
			if (graph.getSelectionCell() != null)
				graph.scrollCellToVisible(graph.getSelectionCell());
		}
	}

	/**
	 * Displays a color dialog and sets the background color on the specified
	 * diagram pane.
	 * 
	 * @param diagramPane
	 *            The diagramPane to perform the operation in.
	 */
	protected void doBackground(JGraphEditorDiagramPane diagramPane) {
		Color color = dlgs.colorDialog(diagramPane, getString("SelectColor"),
				diagramPane.getGraph().getBackground());
		if (color != null)
			diagramPane.getGraph().setBackground(color);
	}

	/**
	 * Displays a file- or URL-dialog and uses the filename to set the
	 * background image on the specified diagram pane.
	 * 
	 * @param diagramPane
	 *            The diagramPane to perform the operation in.
	 * @param filename
	 *            The filename or URL to load the image from.
	 */
	protected void doBackgroundImage(JGraphEditorDiagramPane diagramPane,
			String filename) throws MalformedURLException,
			FileNotFoundException, IOException {
		if (filename != null) {
			JGraphpadImageIcon icon = (JGraphEditor.isURL(filename)) ? new JGraphpadImageIcon(
					JGraphEditor.toURL(filename))
					: new JGraphpadImageIcon(filename);
			if (icon != null)
				diagramPane.setBackgroundImage(icon);
			diagramPane.repaint();
			if (!JGraphEditor.isURL(filename))
				lastDirectory = new File(filename).getParentFile();
		}
	}

	/**
	 * Bundle of all actions in this class.
	 */
	public static class AllActions implements Bundle {

		/**
		 * Holds the actions.
		 */
		public JGraphEditorAction actionToggleRulers = new JGraphpadViewAction(
				NAME_TOGGLERULERS),
				actionTogglePorts = new JGraphpadViewAction(NAME_TOGGLEPORTS),
				actionToggleGrid = new JGraphpadViewAction(NAME_TOGGLEGRID),
				actionToggleAntiAlias = new JGraphpadViewAction(
						NAME_TOGGLEANTIALIAS),
				actionToggleRootHandles = new JGraphpadViewAction(
						NAME_TOGGLEROOTHANDLES),
				actionToggleDragEnabled = new JGraphpadViewAction(
						NAME_TOGGLEDRAGENABLED),
				actionToggleDoubleBuffered = new JGraphpadViewAction(
						NAME_TOGGLEDOUBLEBUFFERED),
				actionTogglePage = new JGraphpadViewAction(NAME_TOGGLEPAGE),
				actionBackgroundImage = new JGraphpadViewAction(
						NAME_BACKGROUNDIMAGE),
				actionBackgroundImageURL = new JGraphpadViewAction(
						NAME_BACKGROUNDIMAGEURL),
				actionBackground = new JGraphpadViewAction(NAME_BACKGROUND),
				actionClearBackground = new JGraphpadViewAction(
						NAME_CLEARBACKGROUND),
				actionZoomActual = new JGraphpadViewAction(NAME_ZOOMACTUAL),
				actionZoomIn = new JGraphpadViewAction(NAME_ZOOMIN),
				actionZoomOut = new JGraphpadViewAction(NAME_ZOOMOUT),
				actionZoomCustom = new JGraphpadViewAction(NAME_ZOOMCUSTOM),
				actionFitNone = new JGraphpadViewAction(NAME_FITNONE),
				actionFitWindow = new JGraphpadViewAction(NAME_FITWINDOW),
				actionFitWidth = new JGraphpadViewAction(NAME_FITWIDTH),
				actionFitPage = new JGraphpadViewAction(NAME_FITPAGE),
				actionTolerance = new JGraphpadViewAction(NAME_TOLERANCE),
				actionPortSize = new JGraphpadViewAction(NAME_PORTSIZE),
				actionGridSize = new JGraphpadViewAction(NAME_GRIDSIZE),
				actionGridColor = new JGraphpadViewAction(NAME_GRIDCOLOR),
				actionToggleMetric = new JGraphpadViewAction(NAME_TOGGLEMETRIC),
				actionToggleEditable = new JGraphpadViewAction(
						NAME_TOGGLEEDITABLE),
				actionEditClickCount = new JGraphpadViewAction(
						NAME_EDITCLICKCOUNT),
				actionToggleDisconnectOnMove = new JGraphpadViewAction(
						NAME_TOGGLEDISCONNECTONMOVE),
				actionToggleMoveBelowZero = new JGraphpadViewAction(
						NAME_TOGGLEMOVEBELOWZERO),
				actionToggleGraphCloneable = new JGraphpadViewAction(
						NAME_TOGGLEGRAPHCLONEABLE),
				actionToggleGraphMoveable = new JGraphpadViewAction(
						NAME_TOGGLEGRAPHMOVEABLE),
				actionToggleGraphSizeable = new JGraphpadViewAction(
						NAME_TOGGLEGRAPHSIZEABLE),
				actionToggleGraphBendable = new JGraphpadViewAction(
						NAME_TOGGLEGRAPHBENDABLE),
				actionToggleGraphConnectable = new JGraphpadViewAction(
						NAME_TOGGLEGRAPHCONNECTABLE),
				actionToggleGraphDisconnectable = new JGraphpadViewAction(
						NAME_TOGGLEGRAPHDISCONNECTABLE),
				actionToggleSelectsLocalInsertedCells = new JGraphpadViewAction(
						NAME_TOGGLESELECTSLOCALINSERTEDCELLS),
				actionToggleSelectsAllInsertedCells = new JGraphpadViewAction(
						NAME_TOGGLESELECTSALLINSERTEDCELLS),
				actionToggleShowsExistingConnections = new JGraphpadViewAction(
						NAME_TOGGLESHOWSEXISTINGCONNECTIONS),
				actionToggleShowsInsertedConnections = new JGraphpadViewAction(
						NAME_TOGGLESHOWSINSERTEDCONNECTIONS),
				actionToggleShowsChangedConnections = new JGraphpadViewAction(
						NAME_TOGGLESHOWSCHANGEDCONNECTIONS),
				actionToggleHidesExistingConnections = new JGraphpadViewAction(
						NAME_TOGGLEHIDESEXISTINGCONNECTIONS),
				actionToggleHidesDanglingConnections = new JGraphpadViewAction(
						NAME_TOGGLEHIDESDANGLINGCONNECTIONS),
				actionToggleRemembersCellViews = new JGraphpadViewAction(
						NAME_TOGGLEREMEMBERSCELLVIEWS),
				actionToggleJumpsToDefaultPort = new JGraphpadViewAction(
						NAME_TOGGLEJUMPSTODEFAULTPORT),
				actionTogglePortsScaled = new JGraphpadViewAction(
						NAME_TOGGLEPORTSSCALED),
				actionToggleMovesIntoGroups = new JGraphpadViewAction(
						NAME_TOGGLEMOVESINTOGROUPS),
				actionToggleMovesOutOfGroups = new JGraphpadViewAction(
						NAME_TOGGLEMOVESOUTOFGROUPS),
				actionSwitchDotGrid = new JGraphpadViewAction(
						NAME_SWITCHDOTGRID),
				actionSwitchLineGrid = new JGraphpadViewAction(
						NAME_SWITCHLINEGRID),
				actionSwitchCrossGrid = new JGraphpadViewAction(
						NAME_SWITCHCROSSGRID),
				actionHandleColor = new JGraphpadViewAction(NAME_HANDLECOLOR),
				actionLockedHandleColor = new JGraphpadViewAction(
						NAME_LOCKEDHANDLECOLOR),
				actionHandleSize = new JGraphpadViewAction(NAME_HANDLESIZE),
				actionMarqueeColor = new JGraphpadViewAction(NAME_MARQUEECOLOR),
				actionLibraryLarger = new JGraphpadViewAction(
						NAME_LIBRARYLARGER),
				actionLibrarySmaller = new JGraphpadViewAction(
						NAME_LIBRARYSMALLER);

		/*
		 * (non-Javadoc)
		 */
		public JGraphEditorAction[] getActions() {
			return new JGraphEditorAction[] { actionToggleRulers,
					actionTogglePorts, actionToggleGrid, actionToggleAntiAlias,
					actionToggleRootHandles, actionToggleDragEnabled,
					actionToggleDoubleBuffered, actionTogglePage,
					actionBackgroundImage, actionBackgroundImageURL,
					actionBackground, actionClearBackground, actionZoomActual,
					actionZoomIn, actionZoomOut, actionZoomCustom,
					actionFitNone, actionFitWindow, actionFitWidth,
					actionFitPage, actionTolerance, actionPortSize,
					actionGridSize, actionGridColor, actionToggleMetric,
					actionToggleEditable, actionEditClickCount,
					actionToggleDisconnectOnMove, actionToggleMoveBelowZero,
					actionToggleGraphMoveable, actionToggleGraphSizeable,
					actionToggleGraphBendable, actionToggleGraphConnectable,
					actionToggleGraphDisconnectable,
					actionToggleSelectsLocalInsertedCells,
					actionToggleSelectsAllInsertedCells,
					actionToggleShowsExistingConnections,
					actionToggleShowsInsertedConnections,
					actionToggleShowsChangedConnections,
					actionToggleHidesExistingConnections,
					actionToggleHidesDanglingConnections,
					actionToggleRemembersCellViews,
					actionToggleJumpsToDefaultPort, actionTogglePortsScaled,
					actionToggleMovesIntoGroups, actionToggleMovesOutOfGroups,
					actionSwitchDotGrid, actionSwitchLineGrid,
					actionSwitchCrossGrid, actionHandleColor,
					actionLockedHandleColor, actionHandleSize,
					actionMarqueeColor, actionLibraryLarger,
					actionLibrarySmaller };
		}

		/*
		 * (non-Javadoc)
		 */
		public void update() {
			Component component = getPermanentFocusOwner();
			boolean isGraphFocused = component instanceof JGraph;
			boolean isLibraryFocused = JGraphpadFileAction
					.getPermanentFocusOwnerLibraryPane() != null;

			// Enabling actions
			actionLibraryLarger.setEnabled(isLibraryFocused);
			actionLibrarySmaller.setEnabled(isLibraryFocused);

			actionTogglePorts.setEnabled(isGraphFocused);
			actionToggleGrid.setEnabled(isGraphFocused);
			actionToggleAntiAlias.setEnabled(isGraphFocused);
			actionToggleRootHandles.setEnabled(isGraphFocused);
			actionToggleDragEnabled.setEnabled(isGraphFocused);
			actionToggleDoubleBuffered.setEnabled(isGraphFocused);
			actionBackground.setEnabled(isGraphFocused);
			actionZoomActual.setEnabled(isGraphFocused);
			actionZoomIn.setEnabled(isGraphFocused);
			actionZoomOut.setEnabled(isGraphFocused);
			actionZoomCustom.setEnabled(isGraphFocused);
			actionTolerance.setEnabled(isGraphFocused);
			actionPortSize.setEnabled(isGraphFocused);
			actionGridSize.setEnabled(isGraphFocused);
			actionGridColor.setEnabled(isGraphFocused);
			actionToggleEditable.setEnabled(isGraphFocused);
			actionEditClickCount.setEnabled(isGraphFocused);
			actionToggleDisconnectOnMove.setEnabled(isGraphFocused);
			actionToggleMoveBelowZero.setEnabled(isGraphFocused);
			actionToggleGraphMoveable.setEnabled(isGraphFocused);
			actionToggleGraphSizeable.setEnabled(isGraphFocused);
			actionToggleGraphBendable.setEnabled(isGraphFocused);
			actionToggleGraphConnectable.setEnabled(isGraphFocused);
			actionToggleGraphDisconnectable.setEnabled(isGraphFocused);
			actionToggleSelectsLocalInsertedCells.setEnabled(isGraphFocused);
			actionToggleSelectsAllInsertedCells.setEnabled(isGraphFocused);
			actionToggleShowsExistingConnections.setEnabled(isGraphFocused);
			actionToggleShowsInsertedConnections.setEnabled(isGraphFocused);
			actionToggleShowsChangedConnections.setEnabled(isGraphFocused);
			actionToggleHidesExistingConnections.setEnabled(isGraphFocused);
			actionToggleHidesDanglingConnections.setEnabled(isGraphFocused);
			actionToggleRemembersCellViews.setEnabled(isGraphFocused);
			actionToggleJumpsToDefaultPort.setEnabled(isGraphFocused);
			actionTogglePortsScaled.setEnabled(isGraphFocused);
			actionToggleMovesIntoGroups.setEnabled(isGraphFocused);
			actionToggleMovesOutOfGroups.setEnabled(isGraphFocused);
			actionSwitchDotGrid.setEnabled(isGraphFocused);
			actionSwitchLineGrid.setEnabled(isGraphFocused);
			actionSwitchCrossGrid.setEnabled(isGraphFocused);
			actionHandleColor.setEnabled(isGraphFocused);
			actionLockedHandleColor.setEnabled(isGraphFocused);
			actionHandleSize.setEnabled(isGraphFocused);
			actionMarqueeColor.setEnabled(isGraphFocused);

			// Sets the states of the toggleable actions that require
			// a focused graph.
			JGraph graph = getPermanentFocusOwnerGraph();
			if (graph != null) {
				GraphLayoutCache cache = graph.getGraphLayoutCache();
				actionTogglePorts.setSelected(graph.isPortsVisible());
				actionToggleGrid.setSelected(graph.isGridVisible());
				actionToggleAntiAlias.setSelected(graph.isAntiAliased());
				actionToggleDragEnabled.setSelected(graph.isDragEnabled());
				actionToggleDoubleBuffered
						.setSelected(graph.isDoubleBuffered());
				actionToggleEditable.setSelected(graph.isEditable());
				actionToggleDisconnectOnMove.setSelected(graph
						.isDisconnectOnMove());
				actionToggleMoveBelowZero.setSelected(graph.isMoveBelowZero());
				actionToggleGraphMoveable.setSelected(graph.isMoveable());
				actionToggleGraphSizeable.setSelected(graph.isSizeable());
				actionToggleGraphBendable.setSelected(graph.isBendable());
				actionToggleGraphConnectable.setSelected(graph.isConnectable());
				actionToggleGraphDisconnectable.setSelected(graph
						.isDisconnectable());
				actionToggleSelectsLocalInsertedCells.setSelected(cache
						.isSelectsLocalInsertedCells());
				actionToggleSelectsAllInsertedCells.setSelected(cache
						.isSelectsAllInsertedCells());
				actionToggleShowsExistingConnections.setSelected(cache
						.isShowsExistingConnections());
				actionToggleShowsInsertedConnections.setSelected(cache
						.isShowsInsertedConnections());
				actionToggleShowsChangedConnections.setSelected(cache
						.isShowsChangedConnections());
				actionToggleHidesExistingConnections.setSelected(cache
						.isHidesExistingConnections());
				actionToggleHidesDanglingConnections.setSelected(cache
						.isHidesDanglingConnections());
				actionToggleRemembersCellViews.setSelected(cache
						.isRemembersCellViews());
				actionToggleJumpsToDefaultPort.setSelected(graph
						.isJumpToDefaultPort());
				actionTogglePortsScaled.setSelected(graph.isPortsScaled());
				actionToggleMovesIntoGroups.setSelected(graph
						.isMoveIntoGroups());
				actionToggleMovesOutOfGroups.setSelected(graph
						.isMoveOutOfGroups());

				// ToggleRootHandles is implemented as a client property
				Boolean b = (Boolean) graph
						.getClientProperty(JGraphpadVertexRenderer.CLIENTPROPERTY_SHOWFOLDINGICONS);
				actionToggleRootHandles.setSelected((b != null) ? b
						.booleanValue() : true);

				int gridMode = graph.getGridMode();
				actionSwitchDotGrid
						.setSelected(gridMode == JGraph.DOT_GRID_MODE);
				actionSwitchLineGrid
						.setSelected(gridMode == JGraph.LINE_GRID_MODE);
				actionSwitchCrossGrid
						.setSelected(gridMode == JGraph.CROSS_GRID_MODE);
			}

			JGraphEditorDiagramPane diagramPane = JGraphEditorDiagramPane
					.getParentDiagramPane(component);
			boolean isDiagramFocused = diagramPane != null;

			actionToggleRulers.setEnabled(isDiagramFocused);
			actionTogglePage.setEnabled(isDiagramFocused);
			actionBackgroundImage.setEnabled(isDiagramFocused);
			actionBackgroundImageURL.setEnabled(isDiagramFocused);
			actionFitNone.setEnabled(isDiagramFocused);
			actionFitWindow.setEnabled(isDiagramFocused);
			actionFitWidth.setEnabled(isDiagramFocused);
			actionFitPage.setEnabled(isDiagramFocused);
			actionClearBackground.setEnabled(isDiagramFocused);
			actionToggleMetric.setEnabled(isDiagramFocused);

			// Sets the states of the toggleable actions that require
			// a focused diagram pane.
			actionFitNone.setSelected(true);
			if (diagramPane != null) {
				actionToggleRulers.setSelected(diagramPane.isRulersVisible());
				actionTogglePage.setSelected(diagramPane.isPageVisible());
				actionToggleMetric.setSelected(diagramPane.isMetric());
				actionFitNone
						.setSelected(diagramPane.getAutoScalePolicy() == JGraphEditorDiagramPane.AUTOSCALE_POLICY_NONE);
				actionFitWindow
						.setSelected(diagramPane.getAutoScalePolicy() == JGraphEditorDiagramPane.AUTOSCALE_POLICY_WINDOW);
				actionFitPage
						.setSelected(diagramPane.getAutoScalePolicy() == JGraphEditorDiagramPane.AUTOSCALE_POLICY_PAGE);
				actionFitWidth
						.setSelected(diagramPane.getAutoScalePolicy() == JGraphEditorDiagramPane.AUTOSCALE_POLICY_PAGEWIDTH);
			}
		}

	}

}