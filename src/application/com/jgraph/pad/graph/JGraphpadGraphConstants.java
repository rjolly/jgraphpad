/* 
 * $Id: JGraphpadGraphConstants.java,v 1.2 2007/08/30 11:04:27 david Exp $
 * Copyright (c) 2001-2005, Gaudenz Alder
 * 
 * All rights reserved.
 * 
 * See LICENSE file for license details. If you are unable to locate
 * this file please contact info (at) jgraph (dot) com.
 */
package com.jgraph.pad.graph;

import java.awt.geom.Point2D;
import java.util.Map;

import org.jgraph.graph.Edge;
import org.jgraph.graph.GraphConstants;
import org.jgraph.util.ParallelEdgeRouter;

import com.jgraph.pad.util.JGraphpadParallelSplineRouter;

/**
 * JGraphpad graph constants. Contains special constants supported by the
 * renderers or other functionality.
 */
public class JGraphpadGraphConstants extends GraphConstants {

	/**
	 * Shared routing instance for parallel routing.
	 */
	public final static Edge.Routing ROUTING_PARALLEL = ParallelEdgeRouter.getSharedInstance();

	/**
	 * Shared routing instance for parallel spline routing.
	 */
	public final static Edge.Routing ROUTING_PARALLELSPLINE = JGraphpadParallelSplineRouter.sharedInstance;

	/**
	 * Key for the <code>stretchImage</code> attribute. This special attribute
	 * contains a Boolean instance indicating whether the background image
	 * should be stretched.
	 */
	public final static String STRETCHIMAGE = "stretchImage";

	/**
	 * Key for the <code>groupResize</code> attribute. This special attribute
	 * contains a Boolean instance indicating if the group should be resized
	 * when it is collapsed. This is usually set to true before the first
	 * collapse and then removed.
	 */
	public final static String GROUPRESIZE = "groupResize";

	/**
	 * Key for the <code>groupReposition</code> attribute. This special
	 * attribute contains a Boolean instance indicating if the collapsed group
	 * should be moved to the top left corner of it's child area when it is
	 * collapsed.
	 */
	public final static String GROUPREPOSITION = "groupReposition";

	/**
	 * Key for the <code>vertexShape</code> attribute. This special attribute
	 * contains an Integer instance indicating which shape should be drawn by
	 * the renderer.
	 */
	public final static String VERTEXSHAPE = "vertexShape";

	/**
	 * Key for the <code>sourcePortOffset</code> attribute. This special
	 * attribute contains a Point2D instance indicating the relative position of
	 * a port in its parents coordinate space seen from a specific edge.
	 */
	public final static String SOURCEPORTOFFSET = "sourcePortOffset";

	/**
	 * Key for the <code>targetPortOffset</code> attribute. This special
	 * attribute contains a Point2D instance indicating the relative position of
	 * a port in its parents coordinate space seen from a specific edge.
	 */
	public final static String TARGETPORTOFFSET = "targetPortOffset";

	/**
	 * Returns true if stretchImage in this map is true. Default is false.
	 */
	public static final boolean isStretchImage(Map map) {
		Boolean boolObj = (Boolean) map.get(STRETCHIMAGE);
		if (boolObj != null)
			return boolObj.booleanValue();
		return false;
	}

	/**
	 * Sets stretchImage in the specified map to the specified value.
	 */
	public static final void setStretchImage(Map map, boolean stretchImage) {
		map.put(STRETCHIMAGE, new Boolean(stretchImage));
	}

	/**
	 * Returns true if groupResize in this map is true. Default is false.
	 */
	public static final boolean isGroupResize(Map map) {
		Boolean boolObj = (Boolean) map.get(GROUPRESIZE);
		if (boolObj != null)
			return boolObj.booleanValue();
		return false;
	}

	/**
	 * Sets groupResize in the specified map to the specified value.
	 */
	public static final void setGroupResize(Map map, boolean stretchImage) {
		map.put(GROUPRESIZE, new Boolean(stretchImage));
	}

	/**
	 * Returns true if groupReposition in this map is true. Default is true.
	 */
	public static final boolean isGroupReposition(Map map) {
		Boolean boolObj = (Boolean) map.get(GROUPREPOSITION);
		if (boolObj != null)
			return boolObj.booleanValue();
		return true;
	}

	/**
	 * Sets groupReposition in the specified map to the specified value.
	 */
	public static final void setGroupReposition(Map map, boolean stretchImage) {
		map.put(GROUPREPOSITION, new Boolean(stretchImage));
	}

	/**
	 * Sets vertexShape in the specified map to the specified value.
	 */
	public static final void setVertexShape(Map map, int shape) {
		map.put(VERTEXSHAPE, new Integer(shape));
	}

	/**
	 * Returns vertexShape from the specified map.
	 */
	public static final int getVertexShape(Map map) {
		Integer intObj = (Integer) map.get(VERTEXSHAPE);
		if (intObj != null)
			return intObj.intValue();
		return 0;
	}

	/**
	 * Sets sourcePortOffset in the specified map to the specified value.
	 */
	public static final void setSourcePortOffset(Map map, Point2D offset) {
		map.put(SOURCEPORTOFFSET, offset);
	}

	/**
	 * Returns sourcePortOffset from the specified map.
	 */
	public static final Point2D getSourcePortOffset(Map map) {
		return (Point2D) map.get(SOURCEPORTOFFSET);
	}

	/**
	 * Sets targetPortOffset in the specified map to the specified value.
	 */
	public static final void setTargetPortOffset(Map map, Point2D offset) {
		map.put(TARGETPORTOFFSET, offset);
	}

	/**
	 * Returns targetPortOffset from the specified map.
	 */
	public static final Point2D getTargetPortOffset(Map map) {
		return (Point2D) map.get(TARGETPORTOFFSET);
	}

	/**
	 * Returns the shared instance of the parallel routing.
	 */
	public static Edge.Routing getParallelEdgeRouting() {
		return ROUTING_PARALLEL;
	}

	/**
	 * Returns the shared instance of the parallel spline routing.
	 */
	public static Edge.Routing getParallelSplineRouting() {
		return ROUTING_PARALLELSPLINE;
	}

}