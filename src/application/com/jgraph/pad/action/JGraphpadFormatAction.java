/* 
 * $Id: JGraphpadFormatAction.java,v 1.10 2006/03/15 07:30:06 gaudenz Exp $
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
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import javax.swing.JLabel;
import javax.swing.JTextPane;
import javax.swing.SwingConstants;
import javax.swing.text.BadLocationException;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

import org.jgraph.JGraph;
import org.jgraph.graph.CellView;
import org.jgraph.graph.Edge;
import org.jgraph.graph.GraphConstants;
import org.jgraph.graph.GraphModel;

import com.jgraph.JGraphEditor;
import com.jgraph.editor.JGraphEditorAction;
import com.jgraph.pad.dialog.JGraphpadDialogs;
import com.jgraph.pad.graph.JGraphpadBusinessObject;
import com.jgraph.pad.graph.JGraphpadGraphConstants;
import com.jgraph.pad.graph.JGraphpadRichTextValue;
import com.jgraph.pad.graph.JGraphpadVertexRenderer;
import com.jgraph.pad.util.JGraphpadImageIcon;

/**
 * Implements all actions of the format menu.
 */
public class JGraphpadFormatAction extends JGraphEditorAction {

	/**
	 * Shortcut to the shared JGraphpad dialogs.
	 */
	private static JGraphpadDialogs dlgs = JGraphpadDialogs.getSharedInstance();

	/**
	 * Defines the underline font style. This font style is only available for
	 * rich text values.
	 */
	protected static int FONT_UNDERLINE = -1;

	/**
	 * Specifies the name for the <code>toggleCellMoveable</code> action.
	 */
	public static final String NAME_TOGGLECELLMOVEABLE = "toggleCellMoveable";

	/**
	 * Specifies the name for the <code>toggleCellMoveable</code> action.
	 */
	public static final String NAME_TOGGLEGROUPREPOSITION = "toggleGroupReposition";

	/**
	 * Specifies the name for the <code>switchLockX</code> action.
	 */
	public static final String NAME_SWITCHLOCKX = "switchLockX";

	/**
	 * Specifies the name for the <code>switchLockX</code> action.
	 */
	public static final String NAME_SWITCHLOCKY = "switchLockY";

	/**
	 * Specifies the name for the <code>resize</code> action.
	 */
	public static final String NAME_RESIZE = "resize";

	/**
	 * Specifies the name for the <code>toggleAutoSize</code> action.
	 */
	public static final String NAME_TOGGLEAUTOSIZE = "toggleAutoSize";

	/**
	 * Specifies the name for the <code>toggleCellSizeable</code> action.
	 */
	public static final String NAME_TOGGLECELLSIZEABLE = "toggleCellSizeable";

	/**
	 * Specifies the name for the <code>toggleConstrained</code> action.
	 */
	public static final String NAME_TOGGLECONSTRAINED = "toggleConstrained";

	/**
	 * Specifies the name for the <code>switchLockWidth</code> action.
	 */
	public static final String NAME_SWITCHLOCKWIDTH = "switchLockWidth";

	/**
	 * Specifies the name for the <code>switchLockHeight</code> action.
	 */
	public static final String NAME_SWITCHLOCKHEIGHT = "switchLockHeight";

	/**
	 * Specifies the name for the <code>switchShapeRectangle</code> action.
	 */
	public static final String NAME_SWITCHSHAPERECTANGLE = "switchShapeRectangle";

	/**
	 * Specifies the name for the <code>switchShapeRounded</code> action.
	 */
	public static final String NAME_SWITCHSHAPEROUNDED = "switchShapeRounded";

	/**
	 * Specifies the name for the <code>switchShapeCircle</code> action.
	 */
	public static final String NAME_SWITCHSHAPECIRCLE = "switchShapeCircle";

	/**
	 * Specifies the name for the <code>switchShapeDiamond</code> action.
	 */
	public static final String NAME_SWITCHSHAPEDIAMOND = "switchShapeDiamond";

	/**
	 * Specifies the name for the <code>switchShapeTriangle</code> action.
	 */
	public static final String NAME_SWITCHSHAPETRIANGLE = "switchShapeTriangle";

	/**
	 * Specifies the name for the <code>switchShapeCylinder</code> action.
	 */
	public static final String NAME_SWITCHSHAPECYLINDER = "switchShapeCylinder";

	/**
	 * Specifies the name for the <code>cellImage</code> action.
	 */
	public static final String NAME_CELLIMAGE = "cellImage";

	/**
	 * Specifies the name for the <code>cellImageURL</code> action.
	 */
	public static final String NAME_CELLIMAGEURL = "cellImageURL";

	/**
	 * Specifies the name for the <code>clearCellImage</code> action.
	 */
	public static final String NAME_CLEARCELLIMAGE = "clearCellImage";

	/**
	 * Specifies the name for the <code>toggleStretchCellImage</code> action.
	 */
	public static final String NAME_TOGGLESTRETCHCELLIMAGE = "toggleStretchCellImage";

	/**
	 * Specifies the name for the <code>cellBackgroundColor</code> action.
	 */
	public static final String NAME_CELLBACKGROUNDCOLOR = "cellBackgroundColor";

	/**
	 * Specifies the name for the <code>cellGradientColor</code> action.
	 */
	public static final String NAME_CELLGRADIENTCOLOR = "cellGradientColor";

	/**
	 * Specifies the name for the <code>toggleCellOpaque</code> action.
	 */
	public static final String NAME_TOGGLECELLOPAQUE = "toggleCellOpaque";

	/**
	 * Specifies the name for the <code>toggleGroupOpaque</code> action.
	 */
	public static final String NAME_TOGGLEGROUPOPAQUE = "toggleGroupOpaque";

	/**
	 * Specifies the name for the <code>cellInset</code> action.
	 */
	public static final String NAME_CELLINSET = "cellInset";

	/**
	 * Specifies the name for the <code>cellBorderColor</code> action.
	 */
	public static final String NAME_CELLBORDERCOLOR = "cellBorderColor";

	/**
	 * Specifies the name for the <code>cellBorderWidth</code> action.
	 */
	public static final String NAME_CELLBORDERWIDTH = "cellBorderWidth";

	/**
	 * Specifies the name for the <code>clearCellBorder</code> action.
	 */
	public static final String NAME_CLEARCELLBORDER = "clearCellBorder";

	/**
	 * Specifies the name for the <code>font</code> action.
	 */
	public static final String NAME_FONT = "font";

	/**
	 * Specifies the name for the <code>fontColor</code> action.
	 */
	public static final String NAME_FONTCOLOR = "fontColor";

	/**
	 * Specifies the name for the <code>fontSize</code> action.
	 */
	public static final String NAME_FONTSIZE = "fontSize";

	/**
	 * Specifies the name for the <code>fontPlain</code> action.
	 */
	public static final String NAME_FONTPLAIN = "fontPlain";

	/**
	 * Specifies the name for the <code>fontBold</code> action.
	 */
	public static final String NAME_FONTBOLD = "fontBold";

	/**
	 * Specifies the name for the <code>fontItalic</code> action.
	 */
	public static final String NAME_FONTITALIC = "fontItalic";

	/**
	 * Specifies the name for the <code>fontUnderline</code> action.
	 */
	public static final String NAME_FONTUNDERLINE = "fontUnderline";

	/**
	 * Specifies the name for the <code>toggleCellEditable</code> action.
	 */
	public static final String NAME_TOGGLECELLEDITABLE = "toggleCellEditable";

	/**
	 * Specifies the name for the <code>switchLabelTop</code> action.
	 */
	public static final String NAME_SWITCHLABELTOP = "switchLabelTop";

	/**
	 * Specifies the name for the <code>switchLabelMiddle</code> action.
	 */
	public static final String NAME_SWITCHLABELMIDDLE = "switchLabelMiddle";

	/**
	 * Specifies the name for the <code>switchLabelBottom</code> action.
	 */
	public static final String NAME_SWITCHLABELBOTTOM = "switchLabelBottom";

	/**
	 * Specifies the name for the <code>switchLabelLeft</code> action.
	 */
	public static final String NAME_SWITCHLABELLEFT = "switchLabelLeft";

	/**
	 * Specifies the name for the <code>switchLabelCenter</code> action.
	 */
	public static final String NAME_SWITCHLABELCENTER = "switchLabelCenter";

	/**
	 * Specifies the name for the <code>switchLabelRight</code> action.
	 */
	public static final String NAME_SWITCHLABELRIGHT = "switchLabelRight";

	/**
	 * Specifies the name for the <code>switchAlignTop</code> action.
	 */
	public static final String NAME_SWITCHALIGNTOP = "switchAlignTop";

	/**
	 * Specifies the name for the <code>switchAlignMiddle</code> action.
	 */
	public static final String NAME_SWITCHALIGNMIDDLE = "switchAlignMiddle";

	/**
	 * Specifies the name for the <code>switchAlignBottom</code> action.
	 */
	public static final String NAME_SWITCHALIGNBOTTOM = "switchAlignBottom";

	/**
	 * Specifies the name for the <code>switchAlignLeft</code> action.
	 */
	public static final String NAME_SWITCHALIGNLEFT = "switchAlignLeft";

	/**
	 * Specifies the name for the <code>switchAlignCenter</code> action.
	 */
	public static final String NAME_SWITCHALIGNCENTER = "switchAlignCenter";

	/**
	 * Specifies the name for the <code>switchAlignRight</code> action.
	 */
	public static final String NAME_SWITCHALIGNRIGHT = "switchAlignRight";

	/**
	 * Specifies the name for the <code>lineWidth</code> action.
	 */
	public static final String NAME_LINEWIDTH = "lineWidth";

	/**
	 * Specifies the name for the <code>lineColor</code> action.
	 */
	public static final String NAME_LINECOLOR = "lineColor";

	/**
	 * Specifies the name for the <code>dashPattern</code> action.
	 */
	public static final String NAME_DASHPATTERN = "dashPattern";

	/**
	 * Specifies the name for the <code>dashOffset</code> action.
	 */
	public static final String NAME_DASHOFFSET = "dashOffset";

	/**
	 * Specifies the name for the <code>switchStyleOrthogonal</code> action.
	 */
	public static final String NAME_SWITCHSTYLEORTHOGONAL = "switchStyleOrthogonal";

	/**
	 * Specifies the name for the <code>switchStyleSpline</code> action.
	 */
	public static final String NAME_SWITCHSTYLESPLINE = "switchStyleSpline";

	/**
	 * Specifies the name for the <code>switchStyleBezier</code> action.
	 */
	public static final String NAME_SWITCHSTYLEBEZIER = "switchStyleBezier";

	/**
	 * Specifies the name for the <code>toggleEdgeBendable</code> action.
	 */
	public static final String NAME_TOGGLEEDGEBENDABLE = "toggleEdgeBendable";

	/**
	 * Specifies the name for the <code>toggleLabelAlongEdge</code> action.
	 */
	public static final String NAME_TOGGLELABELALONGEDGE = "toggleLabelAlongEdge";

	/**
	 * Specifies the name for the <code>switchRoutingNone</code> action.
	 */
	public static final String NAME_SWITCHROUTINGNONE = "switchRoutingNone";

	/**
	 * Specifies the name for the <code>switchRoutingSimple</code> action.
	 */
	public static final String NAME_SWITCHROUTINGSIMPLE = "switchRoutingSimple";

	/**
	 * Specifies the name for the <code>switchRoutingParallel</code> action.
	 */
	public static final String NAME_SWITCHROUTINGPARALLEL = "switchRoutingParallel";

	/**
	 * Specifies the name for the <code>switchRoutingParallelSpline</code>
	 * action.
	 */
	public static final String NAME_SWITCHROUTINGPARALLELSPLINE = "switchRoutingParallelSpline";

	/**
	 * Specifies the name for the <code>beginSize</code> action.
	 */
	public static final String NAME_BEGINSIZE = "beginSize";

	/**
	 * Specifies the name for the <code>clearBegin</code> action.
	 */
	public static final String NAME_CLEARBEGIN = "clearBegin";

	/**
	 * Specifies the name for the <code>endSize</code> action.
	 */
	public static final String NAME_ENDSIZE = "endSize";

	/**
	 * Specifies the name for the <code>clearEnd</code> action.
	 */
	public static final String NAME_CLEAREND = "clearEnd";

	/**
	 * Specifies the name for the <code>toggleConnectable</code> action.
	 */
	public static final String NAME_TOGGLECONNECTABLE = "toggleConnectable";

	/**
	 * Specifies the name for the <code>toggleDisconnectable</code> action.
	 */
	public static final String NAME_TOGGLEDISCONNECTABLE = "toggleDisconnectable";

	/**
	 * Holds the last directory for file operations.
	 */
	protected File lastDirectory = null;

	/**
	 * Constructs a new format action for the specified name. If the action name
	 * starts with <code>toggle</code> or <code>switch</code> then the
	 * action is configured to be a toggle action.
	 * 
	 * @param name
	 *            The name of the action to be created.
	 */
	public JGraphpadFormatAction(String name) {
		super(name);
		setToggleAction(name.startsWith("toggle") || name.startsWith("switch"));
	}

	/**
	 * Executes the action based on the action name.
	 * 
	 * @param event
	 *            The object that describes the event.
	 */
	public void actionPerformed(ActionEvent event) {
		// Fetch the focus owner before showing any dialogs
		// as showing dialogs transfers the focus.
		Component component = getPermanentFocusOwner();

		try {

			// Actions that work on multiple components
			if (getName().equals(NAME_FONT))
				doSetFont(component,
						fontDialog(component, "Choose Font", null), false, 0, 0);
			else if (getName().equals(NAME_FONTCOLOR))
				doSetFont(component, dlgs.colorDialog(component,
						getString("SelectColor"), null), false, 0, 0);
			else if (getName().equals(NAME_FONTSIZE))
				doSetFont(component, null, false, 0, dlgs.intDialog(
						getString("EnterNumber"), 0, false, false));
			else if (getName().equals(NAME_FONTPLAIN))
				doSetFont(component, null, true, Font.PLAIN, 0);
			else if (getName().equals(NAME_FONTBOLD))
				doSetFont(component, null, false, Font.BOLD, 0);
			else if (getName().equals(NAME_FONTITALIC))
				doSetFont(component, null, false, Font.ITALIC, 0);
			else if (getName().equals(NAME_FONTUNDERLINE))
				doSetFont(component, null, false, FONT_UNDERLINE, 0);

			// Actions that require a focused graph
			JGraph graph = getPermanentFocusOwnerGraph();
			if (graph != null) {
				if (getName().equals(NAME_TOGGLECELLEDITABLE))
					doToggleAttribute(graph, GraphConstants.EDITABLE, false);

				// Location
				else if (getName().equals(NAME_TOGGLECELLMOVEABLE))
					doToggleAttribute(graph, GraphConstants.MOVEABLE, false);
				else if (getName().equals(NAME_TOGGLEGROUPREPOSITION))
					doToggleAttribute(graph,
							JGraphpadGraphConstants.GROUPREPOSITION, false);
				else if (getName().equals(NAME_SWITCHLOCKX))
					setAttribute(graph, GraphConstants.MOVEABLEAXIS,
							new Integer(GraphConstants.Y_AXIS));
				else if (getName().equals(NAME_SWITCHLOCKY))
					setAttribute(graph, GraphConstants.MOVEABLEAXIS,
							new Integer(GraphConstants.X_AXIS));

				// Size
				else if (getName().equals(NAME_RESIZE))
					setAttribute(graph, GraphConstants.RESIZE,
							new Boolean(true));
				else if (getName().equals(NAME_TOGGLEAUTOSIZE))
					doToggleAttribute(graph, GraphConstants.AUTOSIZE, true);
				else if (getName().equals(NAME_TOGGLECELLSIZEABLE))
					doToggleAttribute(graph, GraphConstants.SIZEABLE, false);
				else if (getName().equals(NAME_TOGGLECONSTRAINED))
					doToggleAttribute(graph, GraphConstants.CONSTRAINED, true);
				else if (getName().equals(NAME_SWITCHLOCKWIDTH))
					setAttribute(graph, GraphConstants.SIZEABLEAXIS,
							new Integer(GraphConstants.Y_AXIS));
				else if (getName().equals(NAME_SWITCHLOCKHEIGHT))
					setAttribute(graph, GraphConstants.SIZEABLEAXIS,
							new Integer(GraphConstants.X_AXIS));

				// Shape
				else if (getName().equals(NAME_SWITCHSHAPERECTANGLE))
					setAttribute(
							graph,
							JGraphpadGraphConstants.VERTEXSHAPE,
							new Integer(JGraphpadVertexRenderer.SHAPE_RECTANGLE));
				else if (getName().equals(NAME_SWITCHSHAPEROUNDED))
					setAttribute(graph, JGraphpadGraphConstants.VERTEXSHAPE,
							new Integer(JGraphpadVertexRenderer.SHAPE_ROUNDED));
				else if (getName().equals(NAME_SWITCHSHAPECIRCLE))
					setAttribute(graph, JGraphpadGraphConstants.VERTEXSHAPE,
							new Integer(JGraphpadVertexRenderer.SHAPE_CIRCLE));
				else if (getName().equals(NAME_SWITCHSHAPEDIAMOND))
					setAttribute(graph, JGraphpadGraphConstants.VERTEXSHAPE,
							new Integer(JGraphpadVertexRenderer.SHAPE_DIAMOND));
				else if (getName().equals(NAME_SWITCHSHAPETRIANGLE))
					setAttribute(graph, JGraphpadGraphConstants.VERTEXSHAPE,
							new Integer(JGraphpadVertexRenderer.SHAPE_TRIANGLE));
				else if (getName().equals(NAME_SWITCHSHAPECYLINDER))
					setAttribute(graph, JGraphpadGraphConstants.VERTEXSHAPE,
							new Integer(JGraphpadVertexRenderer.SHAPE_CYLINDER));

				// Image
				else if (getName().equals(NAME_CELLIMAGE))
					doAskImageAttribute(graph, GraphConstants.ICON, false);
				else if (getName().equals(NAME_CELLIMAGEURL))
					doAskImageAttribute(graph, GraphConstants.ICON, true);
				else if (getName().equals(NAME_CLEARCELLIMAGE))
					setAttribute(graph, GraphConstants.ICON, null);
				else if (getName().equals(NAME_TOGGLESTRETCHCELLIMAGE))
					doToggleAttribute(graph,
							JGraphpadGraphConstants.STRETCHIMAGE, false);

				// Background
				else if (getName().equals(NAME_CELLBACKGROUNDCOLOR))
					doAskColorAttribute(graph, GraphConstants.BACKGROUND, true);
				else if (getName().equals(NAME_CELLGRADIENTCOLOR))
					doAskColorAttribute(graph, GraphConstants.GRADIENTCOLOR,
							true);
				else if (getName().equals(NAME_TOGGLECELLOPAQUE))
					doToggleAttribute(graph, GraphConstants.OPAQUE, false);
				else if (getName().equals(NAME_TOGGLEGROUPOPAQUE))
					doToggleAttribute(graph, GraphConstants.GROUPOPAQUE, false);

				// Border
				else if (getName().equals(NAME_CELLINSET))
					doAskIntAttribute(graph, GraphConstants.INSET,
							GraphConstants.DEFAULTINSET);
				else if (getName().equals(NAME_CELLBORDERCOLOR))
					doAskColorAttribute(graph, GraphConstants.BORDERCOLOR);
				else if (getName().equals(NAME_CELLBORDERWIDTH)
						|| getName().equals(NAME_LINEWIDTH))
					doAskFloatAttribute(graph, GraphConstants.LINEWIDTH, 1);
				else if (getName().equals(NAME_CLEARCELLBORDER))
					setAttribute(graph, GraphConstants.BORDERCOLOR, null);

				// Vertical label position
				else if (getName().equals(NAME_SWITCHLABELTOP))
					setAttribute(graph, GraphConstants.VERTICAL_TEXT_POSITION,
							new Integer(JLabel.TOP));
				else if (getName().equals(NAME_SWITCHLABELMIDDLE))
					setAttribute(graph, GraphConstants.VERTICAL_TEXT_POSITION,
							new Integer(JLabel.CENTER));
				else if (getName().equals(NAME_SWITCHLABELBOTTOM))
					setAttribute(graph, GraphConstants.VERTICAL_TEXT_POSITION,
							new Integer(JLabel.BOTTOM));
				else if (getName().equals(NAME_SWITCHLABELLEFT))
					setAttribute(graph,
							GraphConstants.HORIZONTAL_TEXT_POSITION,
							new Integer(JLabel.LEFT));
				else if (getName().equals(NAME_SWITCHLABELCENTER))
					setAttribute(graph,
							GraphConstants.HORIZONTAL_TEXT_POSITION,
							new Integer(JLabel.CENTER));
				else if (getName().equals(NAME_SWITCHLABELRIGHT))
					setAttribute(graph,
							GraphConstants.HORIZONTAL_TEXT_POSITION,
							new Integer(JLabel.RIGHT));

				// Alignment
				else if (getName().equals(NAME_SWITCHALIGNTOP))
					setAttribute(graph, GraphConstants.VERTICAL_ALIGNMENT,
							new Integer(JLabel.TOP));
				else if (getName().equals(NAME_SWITCHALIGNMIDDLE))
					setAttribute(graph, GraphConstants.VERTICAL_ALIGNMENT,
							new Integer(JLabel.CENTER));
				else if (getName().equals(NAME_SWITCHALIGNBOTTOM))
					setAttribute(graph, GraphConstants.VERTICAL_ALIGNMENT,
							new Integer(JLabel.BOTTOM));
				else if (getName().equals(NAME_SWITCHALIGNLEFT))
					setAttribute(graph, GraphConstants.HORIZONTAL_ALIGNMENT,
							new Integer(JLabel.LEFT));
				else if (getName().equals(NAME_SWITCHALIGNCENTER))
					setAttribute(graph, GraphConstants.HORIZONTAL_ALIGNMENT,
							new Integer(JLabel.CENTER));
				else if (getName().equals(NAME_SWITCHALIGNRIGHT))
					setAttribute(graph, GraphConstants.HORIZONTAL_ALIGNMENT,
							new Integer(JLabel.RIGHT));

				// Linestyle
				else if (getName().equals(NAME_LINECOLOR))
					doAskColorAttribute(graph, GraphConstants.LINECOLOR);
				else if (getName().equals(NAME_DASHPATTERN))
					doSetDashPattern(graph);
				else if (getName().equals(NAME_DASHOFFSET))
					doAskFloatAttribute(graph, GraphConstants.DASHOFFSET, 0);
				else if (getName().equals(NAME_SWITCHSTYLEORTHOGONAL))
					setAttribute(graph, GraphConstants.LINESTYLE, new Integer(
							GraphConstants.STYLE_ORTHOGONAL));
				else if (getName().equals(NAME_SWITCHSTYLESPLINE))
					setAttribute(graph, GraphConstants.LINESTYLE, new Integer(
							GraphConstants.STYLE_SPLINE));
				else if (getName().equals(NAME_SWITCHSTYLEBEZIER))
					setAttribute(graph, GraphConstants.LINESTYLE, new Integer(
							GraphConstants.STYLE_BEZIER));
				else if (getName().equals(NAME_TOGGLEEDGEBENDABLE))
					doToggleAttribute(graph, GraphConstants.BENDABLE, true);
				else if (getName().equals(NAME_TOGGLELABELALONGEDGE))
					doToggleAttribute(graph, GraphConstants.LABELALONGEDGE,
							true);
				else if (getName().equals(NAME_SWITCHROUTINGNONE))
					setRouting(graph, null);
				else if (getName().equals(NAME_SWITCHROUTINGSIMPLE))
					setRouting(graph, GraphConstants.ROUTING_SIMPLE);
				else if (getName().equals(NAME_SWITCHROUTINGPARALLEL))
					setRouting(graph, JGraphpadGraphConstants.ROUTING_PARALLEL);
				else if (getName().equals(NAME_SWITCHROUTINGPARALLELSPLINE))
					setRouting(graph,
							JGraphpadGraphConstants.ROUTING_PARALLELSPLINE);
				else if (getName().equals(NAME_BEGINSIZE))
					doAskIntAttribute(graph, GraphConstants.BEGINSIZE,
							GraphConstants.DEFAULTDECORATIONSIZE);
				else if (getName().equals(NAME_CLEARBEGIN))
					setAttribute(graph, GraphConstants.LINEBEGIN, null);
				else if (getName().equals(NAME_ENDSIZE))
					doAskIntAttribute(graph, GraphConstants.ENDSIZE,
							GraphConstants.DEFAULTDECORATIONSIZE);
				else if (getName().equals(NAME_CLEAREND))
					setAttribute(graph, GraphConstants.LINEEND, null);
				else if (getName().equals(NAME_TOGGLECONNECTABLE))
					doToggleAttribute(graph, GraphConstants.CONNECTABLE, false);
				else if (getName().equals(NAME_TOGGLEDISCONNECTABLE))
					doToggleAttribute(graph, GraphConstants.DISCONNECTABLE,
							false);
			}
		} catch (JGraphpadDialogs.CancelException e) {
			// ignore
		} catch (Exception e) {
			dlgs.errorDialog(component, e.getMessage());
		}
	}

	/**
	 * Displays a font dialog using
	 * {@link JGraphpadDialogs#fontDialog(Component, String)}. This hook is
	 * overridden in the l2f plugin to provide a better dialog.
	 * 
	 * @param component
	 *            The parent component for the dialog to be displayed.
	 * @param title
	 *            The title of the dialog to be displayed.
	 * @param font
	 *            The default font to use in the dialog.
	 * @return Returns the selected font.
	 */
	public Font fontDialog(Component component, String title, Font font) {
		return JGraphpadDialogs.getSharedInstance()
				.fontDialog(component, title);
	}

	/**
	 * Changes the font style on the specified component to <code>style</code>,
	 * which may be one of {@link Font#BOLD}, {@link Font#ITALIC} or
	 * {@link #FONT_UNDERLINE}. This implementation changes the style of either
	 * the selection cells in {@link JGraph} or the selection text in
	 * {@link JTextPane}.
	 * 
	 * @param graph
	 *            The graph to change the font in.
	 * @param routing
	 *            The routing to use.
	 */
	protected void setRouting(JGraph graph, Edge.Routing routing) {
		Object[] cells = graph.getSelectionCells();

		// Uses the font for each cell to derive a nested map
		Map nested = new Hashtable();
		for (int i = 0; i < cells.length; i++) {
			CellView view = graph.getGraphLayoutCache().getMapping(cells[i],
					false);
			if (view != null && graph.getModel().isEdge(view.getCell())) {
				Map attr = new Hashtable();
				if (routing == null)
					GraphConstants.setRemoveAttributes(attr,
							new Object[] { GraphConstants.ROUTING });
				else
					GraphConstants.setRouting(attr, routing);
				nested.put(cells[i], attr);
			}
		}
		graph.getGraphLayoutCache().edit(nested, null, null, null);
	}

	/**
	 * Changes the dash pattern in the selection edges of the specified graph by
	 * displaying a dialog to enter a dash sequence of the form
	 * <code>[n[,n[,n]*]]</code> where n is a number.
	 * 
	 * @param graph
	 *            The graph to change the dash pattern in.
	 */
	protected void doSetDashPattern(JGraph graph) {
		if (!graph.isSelectionEmpty()) {
			String pattern = dlgs.valueDialog(getString("EnterDashPattern"));
			if (pattern != null) {
				String[] tokens = pattern.split(",");
				float[] f = new float[tokens.length];
				for (int i = 0; i < tokens.length; i++)
					f[i] = Float.parseFloat(tokens[i]);
				if (f != null) {
					Map map = new Hashtable();
					GraphConstants.setDashPattern(map, f);
					graph.getGraphLayoutCache().edit(graph.getSelectionCells(),
							map);
				}
			}
		}
	}

	/**
	 * Changes the font on the specified component to <code>change</code>,
	 * which may be either a {@link Font} or {@link Color} object, changing the
	 * respective font property of either the selection cells in {@link JGraph}
	 * or the selection text in {@link JTextPane}.
	 * 
	 * @param component
	 *            The component to change the font in.
	 * @param change
	 *            The value to change the font to.
	 * @throws BadLocationException
	 */
	protected void doSetFont(Component component, Object change, boolean plain,
			int style, float size) throws BadLocationException {
		SimpleAttributeSet sas = new SimpleAttributeSet();
		if (change instanceof Font) {
			Font font = (Font) change;
			StyleConstants.setBold(sas, true);
			StyleConstants.setFontFamily(sas, font.getFamily());
			StyleConstants.setFontSize(sas, font.getSize());
			StyleConstants.setBold(sas, font.isBold());
			StyleConstants.setItalic(sas, font.isItalic());
		} else if (change instanceof Color) {
			StyleConstants.setForeground(sas, (Color) change);
		} else {
			if (style == Font.BOLD)
				StyleConstants.setBold(sas, true);
			else if (style == Font.ITALIC)
				StyleConstants.setItalic(sas, true);
			else if (style == FONT_UNDERLINE)
				StyleConstants.setUnderline(sas, true);
			else if (plain) {
				StyleConstants.setItalic(sas, false);
				StyleConstants.setBold(sas, false);
				StyleConstants.setUnderline(sas, false);
			}
			if (size > 0)
				StyleConstants.setFontSize(sas, (int) size);
		}

		// Changes the selection text' font in a text pane
		if (component instanceof JTextPane) {
			JTextPane textPane = (JTextPane) component;
			StyledDocument doc = (StyledDocument) textPane.getDocument();
			int start = textPane.getSelectionStart();
			int len = textPane.getSelectionEnd() - start;
			doc.setCharacterAttributes(start, len, sas, false);
		}

		// Changes the selection cells' font in a graph. Rich text cells
		// are a special case where the value of the user object needs
		// to be changed.
		else if (component instanceof JGraph) {
			JGraph graph = (JGraph) component;
			GraphModel model = graph.getModel();
			if (!graph.isSelectionEmpty()) {
				StyledDocument doc = (StyledDocument) JGraphpadRichTextValue
						.createDefaultDocument("");
				Hashtable attrs = new Hashtable();
				if (change instanceof Font)
					GraphConstants.setFont(attrs, (Font) change);
				else if (change instanceof Color)
					GraphConstants.setForeground(attrs, (Color) change);

				Hashtable trx = new Hashtable();
				Object[] cells = graph.getSelectionCells();
				for (int i = 0; i < cells.length; i++) {
					Object value = model.getValue(cells[i]);
					if (value instanceof JGraphpadBusinessObject
							&& ((JGraphpadBusinessObject) value).isRichText()) {
						JGraphpadBusinessObject obj = (JGraphpadBusinessObject) value;
						JGraphpadRichTextValue text = (JGraphpadRichTextValue) obj
								.getValue();
						if (text.toString().length() > 0) {
							text.insertInto(doc);
							doc.setCharacterAttributes(0, doc.getLength(), sas,
									false);
							// Removes the trailing newline that has
							// been added for some strange reason
							doc.remove(doc.getLength() - 1, 1);
							Map map = new Hashtable();
							GraphConstants.setValue(map,
									new JGraphpadRichTextValue(doc));
							trx.put(cells[i], map);
						}
					} else {
						if (attrs.isEmpty()) {
							CellView view = graph.getGraphLayoutCache()
									.getMapping(cells[i], false);
							if (view != null) {
								Font font = GraphConstants.getFont(view
										.getAllAttributes());
								Map newMap = new Hashtable();
								int tmp = (style == Font.PLAIN) ? (plain) ? 0
										: font.getStyle() : font.getStyle()
										| style;
								GraphConstants.setFont(newMap, font.deriveFont(
										tmp, (size == 0) ? font.getSize2D()
												: size));
								trx.put(cells[i], newMap);
							}
						} else {
							trx.put(cells[i], attrs);
						}
					}
				}
				graph.getGraphLayoutCache().edit(trx);
			}
		}
	}

	/**
	 * Changes the horizontal alignment on the specified component to
	 * <code>align</code>, which may be one of {@link SwingConstants#LEFT},
	 * {@link SwingConstants#CENTER} or {@link SwingConstants#RIGHT}. This
	 * implementation changes the horizontal aligment of either the selection
	 * cells in {@link JGraph} or the selection text in {@link JTextPane},
	 * however, the alignment in textpanes is not persisted due to a bug in
	 * Swing (so end users should align cells, not selection rich text).
	 * 
	 * @param component
	 *            The component to change the font in.
	 * @param align
	 *            The horizontal alignment to use.
	 */
	protected void doSetHorizontalAlignment(Component component, int align) {

		// Changes the selection text' alignment in text panes
		if (component instanceof JTextPane) {
			JTextPane textPane = (JTextPane) component;
			StyledDocument doc = (StyledDocument) textPane.getDocument();
			SimpleAttributeSet sas = new SimpleAttributeSet();
			align = (align == JLabel.CENTER) ? StyleConstants.ALIGN_CENTER
					: (align == JLabel.RIGHT) ? StyleConstants.ALIGN_RIGHT
							: StyleConstants.ALIGN_LEFT;
			StyleConstants.setAlignment(sas, align);
			int start = textPane.getSelectionStart();
			int len = textPane.getSelectionEnd() - start;
			doc.setParagraphAttributes(start, len, sas, true);
		}

		// Changes the selection cells' alignment in graphs
		else if (component instanceof JGraph) {
			setAttribute((JGraph) component,
					GraphConstants.HORIZONTAL_ALIGNMENT, new Integer(align));
		}
	}

	/**
	 * Displays a dialog to ask for a number and uses the number to set the
	 * attribute under <code>key</code> for the selected cells in
	 * <code>graph</code>. The default value is used in the dialog.
	 * 
	 * @param graph
	 *            The graph to perform the operation in.
	 * @param key
	 *            The key of the attribute to be changed.
	 * @param defaultValue
	 *            The default value for the number dialog.
	 */
	protected void doAskIntAttribute(JGraph graph, String key, int defaultValue) {
		Integer tmp = (Integer) getAttribute(graph, key);
		int value = (tmp != null) ? tmp.intValue() : defaultValue;
		value = dlgs.intDialog(getString("EnterNumber"), value, false, true);
		setAttribute(graph, key, new Integer(value));
	}

	/**
	 * Displays a dialog to ask for a number and uses the number to set the
	 * attribute under <code>key</code> for the selected cells in
	 * <code>graph</code>. The default value is used in the dialog.
	 * 
	 * @param graph
	 *            The graph to perform the operation in.
	 * @param key
	 *            The key of the attribute to be changed.
	 * @param defaultValue
	 *            The default value for the number dialog.
	 */
	protected void doAskFloatAttribute(JGraph graph, String key,
			float defaultValue) {
		Float tmp = (Float) getAttribute(graph, key);
		float value = (tmp != null) ? tmp.intValue() : defaultValue;
		value = dlgs.floatDialog(getString("EnterNumber"), value, false, true);
		setAttribute(graph, key, new Float(value));
	}

	/**
	 * Shortcut method that invokes
	 * {@link #doAskColorAttribute(JGraph, String, boolean)} with setOpaque =
	 * false.
	 * 
	 * @param graph
	 *            The graph to perform the operation in.
	 * @param key
	 *            The key of the attribute to be changed.
	 */
	protected void doAskColorAttribute(JGraph graph, String key) {
		doAskColorAttribute(graph, key, false);
	}

	/**
	 * Displays a dialog to ask for a color and uses the color to set the
	 * attribute under <code>key</code> for the selected cells in
	 * <code>graph</code>. If <code>setOpaque</code> is <code>true</code>
	 * then the opaque attribute will be set to true.
	 * 
	 * @param graph
	 *            The graph to perform the operation in.
	 * @param key
	 *            The key of the attribute to be changed.
	 * @param setOpaque
	 *            Whether to make the affected cells opaque.
	 */
	protected void doAskColorAttribute(JGraph graph, String key,
			boolean setOpaque) {
		Color color = dlgs.colorDialog(graph, getString("SelectColor"),
				(Color) getAttribute(graph, key));
		if (color != null) {
			if (setOpaque)
				setAttributes(graph,
						new String[] { key, GraphConstants.OPAQUE },
						new Object[] { color, new Boolean(true) }, false);
			else
				setAttribute(graph, key, color);
		}
	}

	/**
	 * Displays a dialog to ask for a file or URL depending on
	 * <code>urlDialog</code> and uses the filename to set the image attribute
	 * under <code>key</code> for the selected cells in <code>graph</code>.
	 * 
	 * @param graph
	 *            The graph to perform the operation in.
	 * @param key
	 *            The key of the attribute to be changed.
	 * @param urlDialog
	 *            Whether to ask for an URL instead of a filename.
	 */
	protected void doAskImageAttribute(JGraph graph, String key,
			boolean urlDialog) throws MalformedURLException,
			FileNotFoundException, IOException {
		String filename = (urlDialog) ? dlgs.valueDialog(getString("EnterURL"))
				: dlgs.imageFileDialog(getPermanentFocusOwnerOrParent(),
						getString("SelectImage"), true, lastDirectory);
		if (filename != null) {
			JGraphpadImageIcon icon = (JGraphEditor.isURL(filename)) ? new JGraphpadImageIcon(
					JGraphEditor.toURL(filename))
					: new JGraphpadImageIcon(filename);
			setAttribute(graph, key, icon);
			if (!JGraphEditor.isURL(filename))
				lastDirectory = new File(filename).getParentFile();
		}
	}

	/**
	 * Displays a dialog to ask for a file or URL depending on
	 * <code>urlDialog</code> and uses the filename to set the image attribute
	 * under <code>key</code> for the selected cells in <code>graph</code>.
	 * 
	 * @param graph
	 *            The graph to perform the operation in.
	 * @param key
	 *            The key of the attribute to be changed.
	 * @param defaultValue
	 *            The default value to use.
	 */
	protected void doToggleAttribute(JGraph graph, String key,
			boolean defaultValue) {
		setAttribute(graph, key, new Boolean(!getBooleanAttribute(graph, key,
				!defaultValue)));
	}

	/**
	 * Sets the value of the attribute under <code>key</code> to
	 * <code>value</code> for the selection cells in <code>graph</code>.
	 * 
	 * @param graph
	 *            The graph to perform the operation in.
	 * @param key
	 *            The name of the attribute to set.
	 * @param value
	 *            The value of the attribute to set.
	 */
	protected void setAttribute(JGraph graph, String key, Object value) {
		setAttributes(graph, new String[] { key }, new Object[] { value },
				false);
	}

	/**
	 * Sets the value of the attributes under <code>keys</code> to
	 * <code>values</code> for the selection or all cells in
	 * <code>graph</code> depending on <code>all</code>. For each key[i]
	 * the corresponding value[i] is assigned. If value[i] is null then this
	 * will add a {@link GraphConstants#REMOVEATTRIBUTES} entry to the nested
	 * map and remove the corresponding key. Note: Use only one value of null
	 * per array of values, as previous {@link GraphConstants#REMOVEATTRIBUTES}
	 * will be overwritten by later ones.
	 * 
	 * @param graph
	 *            The graph to perform the operation in.
	 * @param keys
	 *            The names of the attributes to set.
	 * @param values
	 *            The values of the attributes to set.
	 * @param all
	 *            Whether to apply the change to all or only the selection
	 *            cells.
	 */
	protected void setAttributes(JGraph graph, String[] keys, Object[] values,
			boolean all) {
		Map change = new Hashtable(keys.length);
		for (int i = 0; i < keys.length; i++) {
			if (values[i] != null)
				change.put(keys[i], values[i]);
			else
				GraphConstants.setRemoveAttributes(change,
						new Object[] { keys[i] });
		}
		Object[] cells = (all) ? graph.getDescendants(graph.getRoots()) : graph
				.getSelectionCells();
		graph.getGraphLayoutCache().edit(cells, change);
	}

	/**
	 * Returns the value of the attribute under <code>key</code> for the first
	 * selected cell in the specified graph as a boolean or
	 * <code>defaultValue</code> if no such attribute can be found.
	 * 
	 * @param graph
	 *            The graph to perform the operation in.
	 * @param key
	 *            The name of the attribute to look for.
	 * @param defaultValue
	 *            The default value to use.
	 * @return Returns the value of the <code>key</code> attribute.
	 */
	public static boolean getBooleanAttribute(JGraph graph, String key,
			boolean defaultValue) {
		Object value = getAttribute(graph, key);
		if (value instanceof Boolean)
			defaultValue = ((Boolean) value).booleanValue();
		return defaultValue;
	}

	/**
	 * Returns the value of the attribute under <code>key</code> for the first
	 * selected cell in the specified graph as an int or
	 * <code>defaultValue</code> if no such attribute can be found.
	 * 
	 * @param graph
	 *            The graph to perform the operation in.
	 * @param key
	 *            The name of the attribute to look for.
	 * @param defaultValue
	 *            The default value to use.
	 * @return Returns the value of the <code>key</code> attribute.
	 */
	public static int getIntAttribute(JGraph graph, String key, int defaultValue) {
		Object value = getAttribute(graph, key);
		if (value instanceof Integer)
			defaultValue = ((Integer) value).intValue();
		return defaultValue;
	}

	/**
	 * Returns the value of the attribute under <code>key</code> for the first
	 * selected cell in the specified graph as an Object or null if no such
	 * attribute exists.
	 * 
	 * @param graph
	 *            The graph to perform the operation in.
	 * @param key
	 *            The name of the attribute to look for.
	 * @return Returns the value of the <code>key</code> attribute.
	 */
	public static Object getAttribute(JGraph graph, String key) {
		Object cell = graph.getSelectionCell();
		Map attributes = graph.getModel().getAttributes(cell);
		return (attributes != null) ? attributes.get(key) : null;
	}

	/**
	 * Bundle of all actions in this class.
	 */
	public static class AllActions implements Bundle {

		/**
		 * Holds the actions.
		 */
		public JGraphEditorAction actionToggleCellMoveable = new JGraphpadFormatAction(
				NAME_TOGGLECELLMOVEABLE),
				actionToggleGroupReposition = new JGraphpadFormatAction(
						NAME_TOGGLEGROUPREPOSITION),
				actionSwitchLockX = new JGraphpadFormatAction(NAME_SWITCHLOCKX),
				actionSwitchLockY = new JGraphpadFormatAction(NAME_SWITCHLOCKY),
				actionResize = new JGraphpadFormatAction(NAME_RESIZE),
				actionToggleAutoSize = new JGraphpadFormatAction(
						NAME_TOGGLEAUTOSIZE),
				actionToggleCellSizeable = new JGraphpadFormatAction(
						NAME_TOGGLECELLSIZEABLE),
				actionToggleConstrained = new JGraphpadFormatAction(
						NAME_TOGGLECONSTRAINED),
				actionSwitchLockWidth = new JGraphpadFormatAction(
						NAME_SWITCHLOCKWIDTH),
				actionSwitchLockHeight = new JGraphpadFormatAction(
						NAME_SWITCHLOCKHEIGHT),
				actionSwitchShapeRectangle = new JGraphpadFormatAction(
						NAME_SWITCHSHAPERECTANGLE),
				actionSwitchShapeRounded = new JGraphpadFormatAction(
						NAME_SWITCHSHAPEROUNDED),
				actionSwitchShapeCircle = new JGraphpadFormatAction(
						NAME_SWITCHSHAPECIRCLE),
				actionSwitchShapeDiamond = new JGraphpadFormatAction(
						NAME_SWITCHSHAPEDIAMOND),
				actionSwitchShapeTriangle = new JGraphpadFormatAction(
						NAME_SWITCHSHAPETRIANGLE),
				actionSwitchShapeCylinder = new JGraphpadFormatAction(
						NAME_SWITCHSHAPECYLINDER),
				actionCellImage = new JGraphpadFormatAction(NAME_CELLIMAGE),
				actionCellImageURL = new JGraphpadFormatAction(
						NAME_CELLIMAGEURL),
				actionClearCellImage = new JGraphpadFormatAction(
						NAME_CLEARCELLIMAGE),
				actionToggleStretchCellImage = new JGraphpadFormatAction(
						NAME_TOGGLESTRETCHCELLIMAGE),
				actionCellBackgroundColor = new JGraphpadFormatAction(
						NAME_CELLBACKGROUNDCOLOR),
				actionCellGradientColor = new JGraphpadFormatAction(
						NAME_CELLGRADIENTCOLOR),
				actionToggleCellOpaque = new JGraphpadFormatAction(
						NAME_TOGGLECELLOPAQUE),
				actionToggleGroupOpaque = new JGraphpadFormatAction(
						NAME_TOGGLEGROUPOPAQUE),
				actionCellInset = new JGraphpadFormatAction(NAME_CELLINSET),
				actionCellBorderColor = new JGraphpadFormatAction(
						NAME_CELLBORDERCOLOR),
				actionCellBorderWidth = new JGraphpadFormatAction(
						NAME_CELLBORDERWIDTH),
				actionClearCellBorder = new JGraphpadFormatAction(
						NAME_CLEARCELLBORDER),
				actionFont = new JGraphpadFormatAction(NAME_FONT),
				actionFontColor = new JGraphpadFormatAction(NAME_FONTCOLOR),
				actionFontSize = new JGraphpadFormatAction(NAME_FONTSIZE),
				actionFontPlain = new JGraphpadFormatAction(NAME_FONTPLAIN),
				actionFontBold = new JGraphpadFormatAction(NAME_FONTBOLD),
				actionFontItalic = new JGraphpadFormatAction(NAME_FONTITALIC),
				actionFontUnderline = new JGraphpadFormatAction(
						NAME_FONTUNDERLINE),
				actionSwitchLabelTop = new JGraphpadFormatAction(
						NAME_SWITCHLABELTOP),
				actionSwitchLabelMiddle = new JGraphpadFormatAction(
						NAME_SWITCHLABELMIDDLE),
				actionSwitchLabelBottom = new JGraphpadFormatAction(
						NAME_SWITCHLABELBOTTOM),
				actionSwitchLabelLeft = new JGraphpadFormatAction(
						NAME_SWITCHLABELLEFT),
				actionSwitchLabelCenter = new JGraphpadFormatAction(
						NAME_SWITCHLABELCENTER),
				actionSwitchLabelRight = new JGraphpadFormatAction(
						NAME_SWITCHLABELRIGHT),
				actionToggleCellEditable = new JGraphpadFormatAction(
						NAME_TOGGLECELLEDITABLE),
				actionSwitchAlignTop = new JGraphpadFormatAction(
						NAME_SWITCHALIGNTOP),
				actionSwitchAlignMiddle = new JGraphpadFormatAction(
						NAME_SWITCHALIGNMIDDLE),
				actionSwitchAlignBottom = new JGraphpadFormatAction(
						NAME_SWITCHALIGNBOTTOM),
				actionSwitchAlignLeft = new JGraphpadFormatAction(
						NAME_SWITCHALIGNLEFT),
				actionSwitchAlignCenter = new JGraphpadFormatAction(
						NAME_SWITCHALIGNCENTER),
				actionSwitchAlignRight = new JGraphpadFormatAction(
						NAME_SWITCHALIGNRIGHT),
				actionLineWidth = new JGraphpadFormatAction(NAME_LINEWIDTH),
				actionLineColor = new JGraphpadFormatAction(NAME_LINECOLOR),
				actionDashPattern = new JGraphpadFormatAction(NAME_DASHPATTERN),
				actionDashOffset = new JGraphpadFormatAction(NAME_DASHOFFSET),
				actionSwitchStyleOrthogonal = new JGraphpadFormatAction(
						NAME_SWITCHSTYLEORTHOGONAL),
				actionSwitchStyleSpline = new JGraphpadFormatAction(
						NAME_SWITCHSTYLESPLINE),
				actionSwitchStyleBezier = new JGraphpadFormatAction(
						NAME_SWITCHSTYLEBEZIER),
				actionToggleEdgeBendable = new JGraphpadFormatAction(
						NAME_TOGGLEEDGEBENDABLE),
				actionToggleLabelAlongEdge = new JGraphpadFormatAction(
						NAME_TOGGLELABELALONGEDGE),
				actionSwitchRoutingNone = new JGraphpadFormatAction(
						NAME_SWITCHROUTINGNONE),
				actionSwitchRoutingSimple = new JGraphpadFormatAction(
						NAME_SWITCHROUTINGSIMPLE),
				actionSwitchRoutingParallel = new JGraphpadFormatAction(
						NAME_SWITCHROUTINGPARALLEL),
				actionSwitchRoutingParallelSpline = new JGraphpadFormatAction(
						NAME_SWITCHROUTINGPARALLELSPLINE),
				actionBeginSize = new JGraphpadFormatAction(NAME_BEGINSIZE),
				actionClearBegin = new JGraphpadFormatAction(NAME_CLEARBEGIN),
				actionEndSize = new JGraphpadFormatAction(NAME_ENDSIZE),
				actionClearEnd = new JGraphpadFormatAction(NAME_CLEAREND),
				actionToggleConnectable = new JGraphpadFormatAction(
						NAME_TOGGLECONNECTABLE),
				actionToggleDisconnectable = new JGraphpadFormatAction(
						NAME_TOGGLEDISCONNECTABLE);

		/*
		 * (non-Javadoc)
		 */
		public JGraphEditorAction[] getActions() {
			return new JGraphEditorAction[] { actionToggleCellMoveable,
					actionToggleGroupReposition, actionSwitchLockX,
					actionSwitchLockY, actionResize, actionToggleAutoSize,
					actionToggleCellSizeable, actionToggleConstrained,
					actionSwitchLockWidth, actionSwitchLockHeight,
					actionSwitchShapeRectangle, actionSwitchShapeRounded,
					actionSwitchShapeCircle, actionSwitchShapeDiamond,
					actionSwitchShapeCylinder, actionCellImage,
					actionCellImageURL, actionClearCellImage,
					actionToggleStretchCellImage, actionCellBackgroundColor,
					actionCellGradientColor, actionToggleCellOpaque,
					actionToggleGroupOpaque, actionCellInset,
					actionCellBorderColor, actionCellBorderWidth,
					actionClearCellBorder, actionFont, actionFontColor,
					actionFontSize, actionFontPlain, actionFontBold,
					actionFontItalic, actionFontUnderline,
					actionSwitchLabelTop, actionSwitchLabelMiddle,
					actionSwitchLabelBottom, actionSwitchLabelLeft,
					actionSwitchLabelCenter, actionSwitchLabelRight,
					actionToggleCellEditable, actionSwitchAlignTop,
					actionSwitchAlignMiddle, actionSwitchAlignBottom,
					actionSwitchAlignLeft, actionSwitchAlignCenter,
					actionSwitchAlignRight, actionLineWidth, actionLineColor,
					actionDashPattern, actionDashOffset,
					actionSwitchStyleOrthogonal, actionSwitchStyleSpline,
					actionSwitchStyleBezier, actionToggleEdgeBendable,
					actionToggleLabelAlongEdge, actionSwitchRoutingNone,
					actionSwitchRoutingSimple, actionSwitchRoutingParallel,
					actionSwitchRoutingParallelSpline, actionBeginSize,
					actionClearBegin, actionEndSize, actionClearEnd,
					actionToggleConnectable, actionToggleDisconnectable };
		}

		/*
		 * (non-Javadoc)
		 */
		public void update() {
			JGraph graph = getPermanentFocusOwnerGraph();
			boolean isCellsSelected = graph != null
					&& !graph.isSelectionEmpty();

			// Checks if there are any cells selected. This implements
			// a poor man's check for attribute compatiblity with the
			// selected cells, ie. this edge / rich text attributes
			// if no respective cells are selected.
			boolean isEdgesSelected = false;
			if (isCellsSelected && graph != null) {
				GraphModel model = graph.getModel();
				Object[] cells = graph.getSelectionCells();
				for (int i = 0; i < cells.length; i++)
					isEdgesSelected = isEdgesSelected || model.isEdge(cells[i]);
			}

			actionToggleCellMoveable.setEnabled(isCellsSelected);
			actionToggleGroupReposition.setEnabled(isCellsSelected);
			actionSwitchLockX.setEnabled(isCellsSelected);
			actionSwitchLockY.setEnabled(isCellsSelected);
			actionResize.setEnabled(isCellsSelected);
			actionToggleAutoSize.setEnabled(isCellsSelected);
			actionToggleCellSizeable.setEnabled(isCellsSelected);
			actionToggleConstrained.setEnabled(isCellsSelected);
			actionSwitchLockWidth.setEnabled(isCellsSelected);
			actionSwitchLockHeight.setEnabled(isCellsSelected);
			actionSwitchShapeRectangle.setEnabled(isCellsSelected);
			actionSwitchShapeRounded.setEnabled(isCellsSelected);
			actionSwitchShapeCircle.setEnabled(isCellsSelected);
			actionSwitchShapeDiamond.setEnabled(isCellsSelected);
			actionSwitchShapeCylinder.setEnabled(isCellsSelected);
			actionCellImage.setEnabled(isCellsSelected);
			actionCellImageURL.setEnabled(isCellsSelected);
			actionClearCellImage.setEnabled(isCellsSelected);
			actionToggleStretchCellImage.setEnabled(isCellsSelected);
			actionCellBackgroundColor.setEnabled(isCellsSelected);
			actionCellGradientColor.setEnabled(isCellsSelected);
			actionToggleCellOpaque.setEnabled(isCellsSelected);
			actionToggleGroupOpaque.setEnabled(isCellsSelected);
			actionCellInset.setEnabled(isCellsSelected);
			actionCellBorderColor.setEnabled(isCellsSelected);
			actionCellBorderWidth.setEnabled(isCellsSelected);
			actionClearCellBorder.setEnabled(isCellsSelected);

			actionFont.setEnabled(isCellsSelected);
			actionFontColor.setEnabled(isCellsSelected);
			actionFontSize.setEnabled(isCellsSelected);
			actionFontPlain.setEnabled(isCellsSelected);
			actionFontBold.setEnabled(isCellsSelected);
			actionFontItalic.setEnabled(isCellsSelected);
			actionFontUnderline.setEnabled(isCellsSelected);

			actionSwitchLabelTop.setEnabled(isCellsSelected);
			actionSwitchLabelMiddle.setEnabled(isCellsSelected);
			actionSwitchLabelBottom.setEnabled(isCellsSelected);
			actionSwitchLabelLeft.setEnabled(isCellsSelected);
			actionSwitchLabelCenter.setEnabled(isCellsSelected);
			actionSwitchLabelRight.setEnabled(isCellsSelected);
			actionToggleCellEditable.setEnabled(isCellsSelected);
			actionSwitchAlignTop.setEnabled(isCellsSelected);
			actionSwitchAlignMiddle.setEnabled(isCellsSelected);
			actionSwitchAlignBottom.setEnabled(isCellsSelected);
			actionSwitchAlignLeft.setEnabled(isCellsSelected);
			actionSwitchAlignCenter.setEnabled(isCellsSelected);
			actionSwitchAlignRight.setEnabled(isCellsSelected);
			actionToggleConnectable.setEnabled(isCellsSelected);
			actionToggleDisconnectable.setEnabled(isCellsSelected);

			actionLineWidth.setEnabled(isEdgesSelected);
			actionLineColor.setEnabled(isEdgesSelected);
			actionDashPattern.setEnabled(isEdgesSelected);
			actionDashOffset.setEnabled(isEdgesSelected);
			actionSwitchStyleOrthogonal.setEnabled(isEdgesSelected);
			actionSwitchStyleSpline.setEnabled(isEdgesSelected);
			actionSwitchStyleBezier.setEnabled(isEdgesSelected);
			actionToggleEdgeBendable.setEnabled(isEdgesSelected);
			actionToggleLabelAlongEdge.setEnabled(isEdgesSelected);
			actionSwitchRoutingNone.setEnabled(isEdgesSelected);
			actionSwitchRoutingSimple.setEnabled(isEdgesSelected);
			actionSwitchRoutingParallel.setEnabled(isEdgesSelected);
			actionSwitchRoutingParallelSpline.setEnabled(isEdgesSelected);
			actionBeginSize.setEnabled(isEdgesSelected);
			actionClearBegin.setEnabled(isEdgesSelected);
			actionEndSize.setEnabled(isEdgesSelected);
			actionClearEnd.setEnabled(isEdgesSelected);

			// Sets the toggle states of the toggleable actions
			if (isCellsSelected) {
				actionToggleCellMoveable.setSelected(getBooleanAttribute(graph,
						GraphConstants.MOVEABLE, true));
				actionToggleGroupReposition.setSelected(getBooleanAttribute(
						graph, JGraphpadGraphConstants.GROUPREPOSITION, true));

				int moveableAxis = getIntAttribute(graph,
						GraphConstants.MOVEABLEAXIS, 0);
				actionSwitchLockX
						.setSelected(moveableAxis == GraphConstants.Y_AXIS);
				actionSwitchLockY
						.setSelected(moveableAxis == GraphConstants.X_AXIS);

				actionToggleAutoSize.setSelected(getBooleanAttribute(graph,
						GraphConstants.AUTOSIZE, false));
				actionToggleCellSizeable.setSelected(getBooleanAttribute(graph,
						GraphConstants.SIZEABLE, true));
				actionToggleConstrained.setSelected(getBooleanAttribute(graph,
						GraphConstants.CONSTRAINED, false));

				int sizeableAxis = getIntAttribute(graph,
						GraphConstants.SIZEABLEAXIS, 0);
				actionSwitchLockWidth
						.setSelected(sizeableAxis == GraphConstants.Y_AXIS);
				actionSwitchLockHeight
						.setSelected(sizeableAxis == GraphConstants.X_AXIS);

				int vertexShape = getIntAttribute(graph,
						JGraphpadGraphConstants.VERTEXSHAPE,
						JGraphpadVertexRenderer.SHAPE_RECTANGLE);
				actionSwitchShapeRectangle
						.setSelected(vertexShape == JGraphpadVertexRenderer.SHAPE_RECTANGLE);
				actionSwitchShapeRounded
						.setSelected(vertexShape == JGraphpadVertexRenderer.SHAPE_ROUNDED);
				actionSwitchShapeCircle
						.setSelected(vertexShape == JGraphpadVertexRenderer.SHAPE_CIRCLE);
				actionSwitchShapeDiamond
						.setSelected(vertexShape == JGraphpadVertexRenderer.SHAPE_DIAMOND);
				actionSwitchShapeDiamond
						.setSelected(vertexShape == JGraphpadVertexRenderer.SHAPE_TRIANGLE);
				actionSwitchShapeDiamond
						.setSelected(vertexShape == JGraphpadVertexRenderer.SHAPE_CYLINDER);

				actionToggleCellOpaque.setSelected(getBooleanAttribute(graph,
						GraphConstants.OPAQUE, true));
				actionToggleGroupOpaque.setSelected(getBooleanAttribute(graph,
						GraphConstants.GROUPOPAQUE, true));
				actionToggleStretchCellImage.setSelected(getBooleanAttribute(
						graph, JGraphpadGraphConstants.STRETCHIMAGE, false));

				int verticalTextPosition = getIntAttribute(graph,
						GraphConstants.VERTICAL_TEXT_POSITION, JLabel.CENTER);
				actionSwitchLabelTop
						.setSelected(verticalTextPosition == JLabel.TOP);
				actionSwitchLabelMiddle
						.setSelected(verticalTextPosition == JLabel.CENTER);
				actionSwitchLabelBottom
						.setSelected(verticalTextPosition == JLabel.BOTTOM);
				actionSwitchLabelLeft
						.setSelected(verticalTextPosition == JLabel.LEFT);
				actionSwitchLabelCenter
						.setSelected(verticalTextPosition == JLabel.CENTER);
				actionSwitchLabelRight
						.setSelected(verticalTextPosition == JLabel.RIGHT);

				actionToggleCellEditable.setSelected(getBooleanAttribute(graph,
						GraphConstants.EDITABLE, true));

				int verticalAlignment = getIntAttribute(graph,
						GraphConstants.VERTICAL_ALIGNMENT, JLabel.CENTER);
				actionSwitchAlignTop
						.setSelected(verticalAlignment == JLabel.TOP);
				actionSwitchAlignMiddle
						.setSelected(verticalAlignment == JLabel.CENTER);
				actionSwitchAlignBottom
						.setSelected(verticalAlignment == JLabel.BOTTOM);

				int horizontalAlignment = getIntAttribute(graph,
						GraphConstants.HORIZONTAL_ALIGNMENT, JLabel.CENTER);
				actionSwitchAlignLeft
						.setSelected(horizontalAlignment == JLabel.LEFT);
				actionSwitchAlignCenter
						.setSelected(horizontalAlignment == JLabel.CENTER);
				actionSwitchAlignRight
						.setSelected(horizontalAlignment == JLabel.RIGHT);

				int lineStyle = getIntAttribute(graph,
						GraphConstants.LINESTYLE,
						GraphConstants.STYLE_ORTHOGONAL);
				actionSwitchStyleOrthogonal
						.setSelected(lineStyle == GraphConstants.STYLE_ORTHOGONAL);
				actionSwitchStyleSpline
						.setSelected(lineStyle == GraphConstants.STYLE_SPLINE);
				actionSwitchStyleBezier
						.setSelected(lineStyle == GraphConstants.STYLE_BEZIER);

				actionToggleEdgeBendable.setSelected(getBooleanAttribute(graph,
						GraphConstants.BENDABLE, true));
				actionToggleConnectable.setSelected(getBooleanAttribute(graph,
						GraphConstants.CONNECTABLE, true));
				actionToggleDisconnectable.setSelected(getBooleanAttribute(
						graph, GraphConstants.DISCONNECTABLE, true));
				actionToggleLabelAlongEdge.setSelected(getBooleanAttribute(
						graph, GraphConstants.LABELALONGEDGE, false));

			}
		}

	}

}