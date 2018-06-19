/* 
 * $Id: JGraphpadFocusManager.java,v 1.4 2006/01/30 15:33:27 gaudenz Exp $
 * Copyright (c) 2001-2005, Gaudenz Alder
 * 
 * All rights reserved.
 * 
 * See LICENSE file for license details. If you are unable to locate
 * this file please contact info (at) jgraph (dot) com.
 */
package com.jgraph.pad.util;

import java.awt.AWTEventMulticaster;
import java.awt.Component;
import java.awt.KeyboardFocusManager;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.EventListener;
import java.util.Observable;

import javax.swing.event.EventListenerList;
import javax.swing.event.SwingPropertyChangeSupport;
import javax.swing.event.UndoableEditEvent;
import javax.swing.event.UndoableEditListener;

import org.jgraph.JGraph;
import org.jgraph.event.GraphLayoutCacheEvent;
import org.jgraph.event.GraphLayoutCacheListener;
import org.jgraph.event.GraphModelEvent;
import org.jgraph.event.GraphModelListener;
import org.jgraph.event.GraphSelectionEvent;
import org.jgraph.event.GraphSelectionListener;

/**
 * Indirection to dispatch events from the permanent focus owner graph to the
 * registered listeners. For redispatching mouse events the built-in
 * multicasters are used.
 * 
 * @see AWTEventMulticaster
 */
public class JGraphpadFocusManager extends Observable implements
		GraphLayoutCacheListener, GraphModelListener, GraphSelectionListener,
		MouseListener, MouseMotionListener, UndoableEditListener,
		PropertyChangeListener {

	/**
	 * Bound property name for <code>focusedGraph</code>.
	 */
	public final static String FOCUSED_GRAPH_PROPERTY = "focusedGraph";

	/**
	 * Bound property name for model changes. This is fired whenever a graph
	 * model event is received from the focused graph.
	 */
	public final static String MODEL_CHANGE_NOTIFICATION = "modelChange";

	/**
	 * Bound property name for graph layout changes. This is fired whenever a
	 * graph layout event is received from the focused graph.
	 */
	public final static String GRAPHLAYOUT_CHANGE_NOTIFICATION = "graphLayoutChange";

	/**
	 * Bound property name for graph selection changes. This is fired whenever a
	 * graph selection event is received from the focused graph.
	 */
	public final static String SELECTION_CHANGE_NOTIFICATION = "selectionChange";

	/**
	 * Bound property name for graph undoable changes. This is fired whenever a
	 * graph undoable event is received from the focused graph.
	 */
	public final static String UNDOABLE_CHANGE_NOTIFICATION = "undoableChange";

	/**
	 * Shared singleton instance.
	 */
	public static JGraphpadFocusManager currentGraphFocusManager = new JGraphpadFocusManager();

	/**
	 * Listeners to all graph-specific events (the other use event
	 * multicasters).
	 */
	protected transient EventListenerList listenerList = new EventListenerList();

	/**
	 * Property change support for event notification.
	 */
	protected SwingPropertyChangeSupport changeSupport = new SwingPropertyChangeSupport(
			this);

	/**
	 * Used to manage mouse listeners as an event multicaster.
	 */
	protected MouseListener mouseListener;

	/**
	 * Used to manage mouse motion listeners as an event multicaster.
	 */
	protected MouseMotionListener mouseMotionListener;

	/**
	 * Reference to the current and last focused graph.
	 */
	protected JGraph focusedGraph;

	/**
	 * Constructs a new focus manager.
	 */
	public JGraphpadFocusManager() {
		KeyboardFocusManager focusManager = KeyboardFocusManager
				.getCurrentKeyboardFocusManager();
		focusManager.addPropertyChangeListener(new PropertyChangeListener() {

			/*
			 * (non-Javadoc)
			 */
			public void propertyChange(PropertyChangeEvent e) {
				String prop = e.getPropertyName();
				if (("permanentFocusOwner".equals(prop))
						&& (e.getNewValue() != null)
						&& ((e.getNewValue()) instanceof Component)) {
					if (e.getNewValue() instanceof JGraph)
						setFocusedGraph((JGraph) e.getNewValue());
					else
						setFocusedGraph(null);
				}
			}
		});
	}

	/**
	 * Returns the shared graph focused manager.
	 * 
	 * @return Returns the shared graph focus manager.
	 */
	public static JGraphpadFocusManager getCurrentGraphFocusManager() {
		return currentGraphFocusManager;
	}

	/**
	 * Returns the focused graph.
	 * 
	 * @return Returns the focused graph.
	 */
	public JGraph getFocusedGraph() {
		return focusedGraph;
	}

	/**
	 * Sets the focused graph to the specified value. If the current focused
	 * graph points to a different instance then <code>newGraph</code> then
	 * this implementation updates the last focused graph with the current
	 * focused graph and removes all listeners from either the current graph or
	 * the last focused graph if the current graph is null. This method fires a
	 * property change event for {@link #FOCUSED_GRAPH_PROPERTY}.
	 * 
	 * @param newGraph
	 *            The new focused graph.
	 */
	public void setFocusedGraph(JGraph newGraph) {
		JGraph oldValue = getFocusedGraph();
		if (oldValue != newGraph) {

			// Uninstalls the listeners from the previous graph
			if (oldValue != null) {
				uninstallListeners(oldValue);
			}

			// Installs the listeners into the new graph and fires a property
			// change event
			this.focusedGraph = newGraph;
			installListeners(newGraph);
			changeSupport.firePropertyChange(FOCUSED_GRAPH_PROPERTY, oldValue,
					newGraph);
		}
	}

	/**
	 * Installs all listeners in the specified graph.
	 * 
	 * @param graph
	 *            The graph to install the listeners to.
	 */
	protected void installListeners(JGraph graph) {
		if (graph != null) {
			graph.getModel().addGraphModelListener(this);
			graph.getModel().addUndoableEditListener(this);
			graph.addGraphSelectionListener(this);
			graph.getGraphLayoutCache().addGraphLayoutCacheListener(this);
			graph.addMouseListener(this);
			graph.addMouseMotionListener(this);
			graph.addPropertyChangeListener(this);
		}
	}

	/**
	 * Uninstalls all listeners previously registered using
	 * {@link #installListeners(JGraph)} from the specified graph.
	 * 
	 * @param graph
	 *            The graph to uninstall the listeners from.
	 */
	protected void uninstallListeners(JGraph graph) {
		if (graph != null) {
			graph.getModel().removeGraphModelListener(this);
			graph.getModel().removeUndoableEditListener(this);
			graph.removeGraphSelectionListener(this);
			graph.getGraphLayoutCache().removeGraphLayoutCacheListener(this);
			graph.removeMouseListener(this);
			graph.removeMouseMotionListener(this);
			graph.removePropertyChangeListener(this);
		}
	}

	//
	// PropertyChangeListener
	//

	/**
	 * Adds a PropertyChangeListener to the listener list. The listener is
	 * registered for all properties.
	 * 
	 * @param listener
	 *            the PropertyChangeListener to be added
	 */
	public synchronized void addPropertyChangeListener(
			PropertyChangeListener listener) {
		changeSupport.addPropertyChangeListener(listener);
	}

	/**
	 * Removes a PropertyChangeListener from the listener list. This removes a
	 * PropertyChangeListener that was registered for all properties.
	 * 
	 * @param listener
	 *            the PropertyChangeListener to be removed
	 */
	public synchronized void removePropertyChangeListener(
			PropertyChangeListener listener) {
		changeSupport.removePropertyChangeListener(listener);
	}

	/**
	 * Redispatches the property change event using the {@link #changeSupport}.
	 */
	public void propertyChange(PropertyChangeEvent evt) {
		changeSupport.firePropertyChange(evt);
	}

	//
	// UndoableEditListener
	//

	/**
	 * Fires a property change for <code>UNDOABLE_CHANGE_NOTIFICATION</code>.
	 * 
	 * @see javax.swing.event.UndoableEditListener#undoableEditHappened(javax.swing.event.UndoableEditEvent)
	 */
	public void undoableEditHappened(UndoableEditEvent e) {
		changeSupport.firePropertyChange(UNDOABLE_CHANGE_NOTIFICATION,
				focusedGraph, e);
	}

	//
	// GraphLayoutCacheListener
	//

	/**
	 * Redispatches the graph layout cache event and fires a property change for
	 * <code>UNDOABLE_CHANGE_NOTIFICATION</code>.
	 * 
	 * @see javax.swing.event.UndoableEditListener#undoableEditHappened(javax.swing.event.UndoableEditEvent)
	 */
	public void graphLayoutCacheChanged(GraphLayoutCacheEvent e) {
		setChanged();
		fireGraphLayoutCacheChanged(e);
		changeSupport.firePropertyChange(GRAPHLAYOUT_CHANGE_NOTIFICATION,
				focusedGraph, e);
	}

	/**
	 * Adds a listener for the GraphLayoutCacheEvent posted after the graph
	 * layout cache changes.
	 * 
	 * @see #removeGraphLayoutCacheListener(GraphLayoutCacheListener)
	 * @param l
	 *            the listener to add
	 */
	public void addGraphLayoutCacheListener(GraphLayoutCacheListener l) {
		listenerList.add(GraphLayoutCacheListener.class, l);
	}

	/**
	 * Removes a listener previously added with <B>addGraphModelListener() </B>.
	 * 
	 * @see #addGraphLayoutCacheListener(GraphLayoutCacheListener)
	 * @param l
	 *            the listener to remove
	 */
	public void removeGraphLayoutCacheListener(GraphLayoutCacheListener l) {
		listenerList.remove(GraphLayoutCacheListener.class, l);
	}

	/**
	 * Notifies all listeners that have registered interest for notification on
	 * this event type. The event instance is lazily created using the
	 * parameters passed into the fire method.
	 * 
	 * @see EventListenerList
	 */
	protected void fireGraphLayoutCacheChanged(GraphLayoutCacheEvent e) {
		Object[] listeners = listenerList.getListenerList();
		for (int i = listeners.length - 2; i >= 0; i -= 2) {
			if (listeners[i] == GraphLayoutCacheListener.class) {
				((GraphLayoutCacheListener) listeners[i + 1])
						.graphLayoutCacheChanged(e);
			}
		}
	}

	/**
	 * Returns an array of all GraphModelListeners that were added to this
	 * model.
	 */
	public GraphLayoutCacheListener[] getGraphLayoutCacheListeners() {
		return (GraphLayoutCacheListener[]) listenerList
				.getListeners(GraphLayoutCacheListener.class);
	}

	//
	// GraphModelListener
	//

	/**
	 * Redispatches the graph model event and fires a property change for
	 * <code>MODEL_CHANGE_NOTIFICATION</code>.
	 * 
	 * @see org.jgraph.event.GraphModelListener#graphChanged(org.jgraph.event.GraphModelEvent)
	 */
	public void graphChanged(GraphModelEvent e) {
		fireGraphChanged(e);
		changeSupport.firePropertyChange(MODEL_CHANGE_NOTIFICATION,
				focusedGraph, e);
	}

	/**
	 * Adds a listener for the GraphModelEvent posted after the graph changes.
	 * 
	 * @see #removeGraphModelListener(GraphModelListener)
	 * @param l
	 *            the listener to add
	 */
	public void addGraphModelListener(GraphModelListener l) {
		listenerList.add(GraphModelListener.class, l);
	}

	/**
	 * Removes a listener previously added with <B>addGraphModelListener() </B>.
	 * 
	 * @see #addGraphModelListener(GraphModelListener)
	 * @param l
	 *            the listener to remove
	 */
	public void removeGraphModelListener(GraphModelListener l) {
		listenerList.remove(GraphModelListener.class, l);
	}

	/**
	 * Notify all listeners that have registered interest for notification on
	 * this event type. The event instance is lazily created using the
	 * parameters passed into the fire method.
	 * 
	 * @see EventListenerList
	 */
	protected void fireGraphChanged(GraphModelEvent e) {
		Object[] listeners = listenerList.getListenerList();
		for (int i = listeners.length - 2; i >= 0; i -= 2)
			if (listeners[i] == GraphModelListener.class)
				((GraphModelListener) listeners[i + 1]).graphChanged(e);
	}

	/**
	 * Return an array of all graph model listeners.
	 */
	public GraphModelListener[] getGraphModelListeners() {
		return (GraphModelListener[]) listenerList
				.getListeners(GraphModelListener.class);
	}

	//
	// GraphSelectionListener
	//

	/**
	 * Redirects the value change event and fires a property change for
	 * <code>SELECTION_CHANGE_NOTIFICATION</code>.
	 * 
	 * @see org.jgraph.event.GraphSelectionListener#valueChanged(org.jgraph.event.GraphSelectionEvent)
	 */
	public void valueChanged(GraphSelectionEvent e) {
		fireValueChanged(e);
		changeSupport.firePropertyChange(SELECTION_CHANGE_NOTIFICATION,
				focusedGraph, e);
	}

	/**
	 * Adds <code>x</code> to the list of listeners that are notified each
	 * time the set of selected cells changes.
	 * 
	 * @param x
	 *            the new listener to be added
	 */
	public void addGraphSelectionListener(GraphSelectionListener x) {
		listenerList.add(GraphSelectionListener.class, x);
	}

	/**
	 * Removes <code>x</code> from the list of listeners that are notified
	 * each time the set of selected cells changes.
	 * 
	 * @param x
	 *            the listener to remove
	 */
	public void removeGraphSelectionListener(GraphSelectionListener x) {
		listenerList.remove(GraphSelectionListener.class, x);
	}

	/**
	 * Notifies all listeners that are registered for graph selection events on
	 * this object.
	 * 
	 * @see #addGraphSelectionListener(GraphSelectionListener)
	 * @see EventListenerList
	 */
	protected void fireValueChanged(GraphSelectionEvent e) {
		Object[] listeners = listenerList.getListenerList();
		for (int i = listeners.length - 2; i >= 0; i -= 2)
			if (listeners[i] == GraphSelectionListener.class)
				((GraphSelectionListener) listeners[i + 1]).valueChanged(e);
	}

	/**
	 * Returns an array of all the listeners of the given type that were added
	 * to this manager.
	 * 
	 * @return all of the objects receiving <em>listenerType</em>
	 *         notifications from this model
	 */
	public EventListener[] getListeners(Class listenerType) {
		return listenerList.getListeners(listenerType);
	}

	//
	// MouseListeners
	//

	/**
	 * Adds a listener for the MouseEvent.
	 * 
	 * @see #removeMouseListener(MouseListener)
	 * @param l
	 *            the listener to add
	 */
	public synchronized void addMouseListener(MouseListener l) {
		mouseListener = AWTEventMulticaster.add(mouseListener, l);
	}

	/**
	 * Removes a listener previously added with <B>addMouseListener() </B>.
	 * 
	 * @see #addMouseListener(MouseListener)
	 * @param l
	 *            the listener to remove
	 */
	public synchronized void removeMouseListener(MouseListener l) {
		mouseListener = AWTEventMulticaster.remove(mouseListener, l);
	}

	/**
	 * Adds a listener for the MouseMotionEvent.
	 * 
	 * @see #removeMouseMotionListener(MouseMotionListener)
	 * @param l
	 *            the listener to add
	 */
	public synchronized void addMouseMotionListener(MouseMotionListener l) {
		mouseMotionListener = AWTEventMulticaster.add(mouseMotionListener, l);
	}

	/**
	 * Removes a listener previously added with
	 * <B>addMouseMotionListeneraddMouseMotionListener() </B>.
	 * 
	 * @see #addMouseListener(MouseListener)
	 * @param l
	 *            the listener to remove
	 */
	public synchronized void removeMouseMotionListener(MouseMotionListener l) {
		mouseMotionListener = AWTEventMulticaster
				.remove(mouseMotionListener, l);
	}

	/**
	 * Redirects the mouse event to the registered listeners.
	 * 
	 * @see java.awt.event.MouseListener#mouseClicked(java.awt.event.MouseEvent)
	 */
	public void mouseClicked(MouseEvent arg0) {
		MouseListener listener = mouseListener;
		if (listener != null) {
			listener.mouseClicked(arg0);
		}
	}

	/**
	 * Redirects the mouse event to the registered listeners.
	 * 
	 * @see java.awt.event.MouseListener#mousePressed(java.awt.event.MouseEvent)
	 */
	public void mousePressed(MouseEvent arg0) {
		MouseListener listener = mouseListener;
		if (listener != null) {
			listener.mousePressed(arg0);
		}
	}

	/**
	 * Redirects the mouse event to the registered listeners.
	 * 
	 * @see java.awt.event.MouseListener#mouseReleased(java.awt.event.MouseEvent)
	 */
	public void mouseReleased(MouseEvent arg0) {
		MouseListener listener = mouseListener;
		if (listener != null) {
			listener.mouseReleased(arg0);
		}
	}

	/**
	 * Redirects the mouse event to the registered listeners.
	 * 
	 * @see java.awt.event.MouseListener#mouseEntered(java.awt.event.MouseEvent)
	 */
	public void mouseEntered(MouseEvent arg0) {
		MouseListener listener = mouseListener;
		if (listener != null) {
			listener.mouseEntered(arg0);
		}
	}

	/**
	 * Redirects the mouse event to the registered listeners.
	 * 
	 * @see java.awt.event.MouseListener#mouseExited(java.awt.event.MouseEvent)
	 */
	public void mouseExited(MouseEvent arg0) {
		MouseListener listener = mouseListener;
		if (listener != null) {
			listener.mouseExited(arg0);
		}
	}

	/**
	 * Redirects the mouse event to the registered listeners.
	 * 
	 * @see java.awt.event.MouseMotionListener#mouseDragged(java.awt.event.MouseEvent)
	 */
	public void mouseDragged(MouseEvent arg0) {
		MouseMotionListener listener = mouseMotionListener;
		if (listener != null) {
			listener.mouseDragged(arg0);
		}
	}

	/**
	 * Redirects the mouse event to the registered listeners.
	 * 
	 * @see java.awt.event.MouseMotionListener#mouseMoved(java.awt.event.MouseEvent)
	 */
	public void mouseMoved(MouseEvent arg0) {
		MouseMotionListener listener = mouseMotionListener;
		if (listener != null) {
			listener.mouseMoved(arg0);
		}
	}

}