/* 
 * $Id: JGraphpadWindowMenu.java,v 1.2 2005/10/15 16:35:35 gaudenz Exp $
 * Copyright (c) 2001-2005, Gaudenz Alder
 * 
 * All rights reserved.
 * 
 * See LICENSE file for license details. If you are unable to locate
 * this file please contact info (at) jgraph (dot) com.
 */
package com.jgraph.pad.factory;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ContainerEvent;
import java.awt.event.ContainerListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyVetoException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.ButtonGroup;
import javax.swing.JDesktopPane;
import javax.swing.JInternalFrame;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JRadioButtonMenuItem;

import org.w3c.dom.Node;

import com.jgraph.JGraphEditor;
import com.jgraph.editor.JGraphEditorResources;
import com.jgraph.editor.factory.JGraphEditorFactoryMethod;

/**
 * Window menu to activate internal frames in an editor pane.
 */
public class JGraphpadWindowMenu extends JMenu implements ContainerListener {

	/**
	 * Specifies the resource key for the menu label. Default is
	 * <code>windowMenu.label</code>.
	 */
	public static String KEY_MENULABEL = "windowMenu.label";

	/**
	 * Specifies the key under which to store the window menu for later
	 * reference.
	 */
	public static String KEY_WINDOWMENU = "windowMenu";

	/**
	 * References the desktop pane that this menu represents. This is assigned
	 * only after the desktop pane has been created by the respective factory
	 * method.
	 */
	protected JDesktopPane desktopPane;

	/**
	 * Holds all dynamically created items.
	 */
	protected List items = new ArrayList();

	/**
	 * Constructs a new window menu using {@link #KEY_MENULABEL}.
	 */
	public JGraphpadWindowMenu() {
		super(JGraphEditorResources.getString(KEY_MENULABEL));

		// Adds cascade all action
		add(new JMenuItem(new AbstractAction(JGraphEditorResources
				.getString("cascadeAll.label")) {

			/*
			 * (non-Javadoc)
			 */
			public void actionPerformed(ActionEvent e) {
				doCascadeAll();
			}
		}));

		// Adds maximize all action
		add(new JMenuItem(new AbstractAction(JGraphEditorResources
				.getString("maximizeAll.label")) {

			/*
			 * (non-Javadoc)
			 */
			public void actionPerformed(ActionEvent e) {
				doMaximizeAll();
			}
		}));

		// Adds minimize all action
		add(new JMenuItem(new AbstractAction(JGraphEditorResources
				.getString("minimizeAll.label")) {

			/*
			 * (non-Javadoc)
			 */
			public void actionPerformed(ActionEvent e) {
				doMinimizeAll();
			}
		}));

		addSeparator();
	}

	/**
	 * Returns the desktop pane.
	 * 
	 * @return Returns the desktopPane.
	 */
	public JDesktopPane getDesktopPane() {
		return desktopPane;
	}

	/**
	 * Sets the desktop pane and adds this as a container listener.
	 * 
	 * @param desktopPane
	 *            The desktopPane to set.
	 */
	public void setDesktopPane(JDesktopPane desktopPane) {
		this.desktopPane = desktopPane;
		desktopPane.addContainerListener(this);
	}

	/**
	 * Maximizes all internal frames in {@link #desktopPane}.
	 */
	public void doMaximizeAll() {
		if (desktopPane != null) {
			try {
				JInternalFrame selectedFrame = null;
				JInternalFrame[] frames = desktopPane.getAllFrames();
				for (int i = 0; i < frames.length; i++) {
					if (frames[i].isSelected())
						selectedFrame = frames[i];
					frames[i].setIcon(false);
					frames[i].setMaximum(true);
				}
				if (selectedFrame != null) {
					selectedFrame.toFront();
					selectedFrame.setSelected(true);
				}
			} catch (java.beans.PropertyVetoException pvex) {
				// empty
			}
		}
	}

	/**
	 * Minimizes all internal frames in {@link #desktopPane}.
	 */
	protected void doMinimizeAll() {
		if (desktopPane != null) {
			try {
				JInternalFrame[] frames = desktopPane.getAllFrames();
				for (int i = 0; i < frames.length; i++) {
					frames[i].setIcon(true);
					frames[i].setMaximum(false);
				}
			} catch (java.beans.PropertyVetoException pvex) {
				// empty
			}
		}
	}

	/**
	 * Cascades all internal frames in {@link #desktopPane}.
	 */
	protected void doCascadeAll() {
		if (desktopPane != null) {
			JInternalFrame[] frames = desktopPane.getAllFrames();

			int desktopX = frames[0].getX();
			int desktopY = frames[0].getY();
			int desktopWidth = frames[0].getWidth();
			int desktopHeight = frames[0].getHeight();
			int diffWidth = 20;
			int diffHeight = 20;

			for (int i = 0; i < frames.length; i++) {
				int frmWidth = desktopWidth - (frames.length - 1) * diffWidth;
				int frmHeight = desktopHeight - (frames.length - 1)
						* diffHeight;

				try {
					frames[i].setIcon(false);
					frames[i].setMaximum(false);
				} catch (java.beans.PropertyVetoException pvex) {
					// empty
				}

				frames[i].setLocation(desktopX, desktopY);
				frames[i].setSize(frmWidth, frmHeight);

				desktopX += diffWidth;
				desktopY += diffHeight;
			}
			desktopPane.getSelectedFrame().toFront();
		}
	}

	/**
	 * Reloads all dynamic items in the menu.
	 */
	protected void updateItems() {
		if (desktopPane != null) {
			Iterator it = items.iterator();

			// Removes all existing dynamic items
			while (it.hasNext())
				remove((JMenuItem) it.next());
			items.clear();

			// Adds a new item for each internal frame
			ButtonGroup group = new ButtonGroup();
			JInternalFrame[] frames = desktopPane.getAllFrames();
			if (frames.length == 0) {
				JMenuItem item = new JMenuItem(JGraphEditorResources
						.getString("NoDocument"));
				item.setEnabled(false);
				items.add(item);
				add(item);
			} else {
				for (int i = 0; i < frames.length; i++) {
					JInternalFrame frame = frames[i];
					JMenuItem item = createItem(frame);
					items.add(item);
					group.add(item);
					add(item);
				}
			}
			invalidate();
		}
	}

	/**
	 * Creates a new menuitem for the specified internal frame adding an action
	 * listener that brings the respective frame to front. The menu item is
	 * automatically updated if the frame's title changes or if the frame is
	 * selected.
	 * 
	 * @param frame
	 *            The internal frame that the item represents.
	 * @return Returns a new menu item for <code>frame</code>.
	 */
	protected JMenuItem createItem(final JInternalFrame frame) {
		final JRadioButtonMenuItem item = new JRadioButtonMenuItem(
				new AbstractAction(frame.getTitle()) {

					/*
					 * (non-Javadoc)
					 */
					public void actionPerformed(ActionEvent e) {
						try {
							frame.setIcon(false);
							frame.setSelected(true);
						} catch (PropertyVetoException e1) {
							// ignore
						}
						frame.toFront();
					}
				});

		// Adds a property change listener to the frame to keep the
		// menu item state up-to-date.
		frame.addPropertyChangeListener(new PropertyChangeListener() {

			/*
			 * (non-Javadoc)
			 */
			public void propertyChange(PropertyChangeEvent evt) {
				item.setText(frame.getTitle());
				item.setSelected(frame.isSelected());
				invalidate();
			}

		});
		return item;
	}

	/*
	 * (non-Javadoc)
	 */
	public void componentAdded(ContainerEvent e) {
		updateItems();
	}

	/*
	 * (non-Javadoc)
	 */
	public void componentRemoved(ContainerEvent e) {
		updateItems();
	}

	/**
	 * Provides a factory method to construct a window menu for an editor.
	 */
	public static class FactoryMethod extends JGraphEditorFactoryMethod {

		/**
		 * Defines the default name for factory methods of this kind.
		 */
		public static String NAME = "createWindowMenu";

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
			JMenu menu = new JGraphpadWindowMenu();
			editor.getSettings().putObject(KEY_WINDOWMENU, menu);
			return menu;
		}
	}

}