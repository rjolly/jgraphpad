/* 
 * $Id: JGraphpadOpenRecentMenu.java,v 1.4 2006/01/31 15:33:11 gaudenz Exp $
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
import java.awt.event.ActionListener;
import java.io.File;
import java.util.Properties;

import javax.swing.JMenu;
import javax.swing.JMenuItem;

import org.w3c.dom.Node;

import com.jgraph.JGraphEditor;
import com.jgraph.JGraphpad;
import com.jgraph.editor.JGraphEditorAction;
import com.jgraph.editor.JGraphEditorModel;
import com.jgraph.editor.JGraphEditorResources;
import com.jgraph.editor.factory.JGraphEditorFactoryMethod;
import com.jgraph.pad.dialog.JGraphpadDialogs;

/**
 * Menu to open the files stored under {@link JGraphpad#KEY_RECENTFILES} in the
 * editor settings. This implementation is updated once on application start. It
 * requires its list of entries in the settings to be updated by the respective
 * file actions, namely open and save.
 */
public class JGraphpadOpenRecentMenu extends JGraphEditorFactoryMethod {

	/**
	 * Defines the default name for factory methods of this kind.
	 */
	public static String NAME = "createOpenRecentMenu";

	/**
	 * Specifies the maximum length of the filename to show. Default is 30.
	 */
	public static int MAX_DISPLAYLENGTH = 30;

	/**
	 * Specifies the resource key for the menu label. Default is
	 * <code>openRecentMenu.label</code>.
	 */
	public static String KEY_MENULABEL = "openRecentMenu.label";

	/**
	 * References the enclosing editor.
	 */
	protected JGraphEditor editor;

	/**
	 * Constructs a new factory method for the specified enclosing editor using
	 * {@link #NAME}.
	 * 
	 * @param editor
	 *            The editor that contains the factory method.
	 */
	public JGraphpadOpenRecentMenu(JGraphEditor editor) {
		super(NAME);
		this.editor = editor;
	}

	/**
	 * Constructs a new menu using the entries in the recent files list. Adds an
	 * action listener to each entry that invokes
	 * {@link JGraphEditorModel#addFile(String)} with the respective filename.
	 * The configuration parameter is currently ignored.
	 * 
	 * @param configuration
	 *            The configuration to configure the menu with.
	 */
	public Component createInstance(Node configuration) {
		JMenu menu = new JMenu(JGraphEditorResources.getString(KEY_MENULABEL));
		Properties props = editor.getSettings().getProperties(
				JGraphpad.NAME_USERSETTINGS);
		int i = 0;
		if (props != null) {

			// Finds all entries in the list by appending an incrementing
			// value to the resource key. If no value is found the end
			// of the list is assumed.
			String tmp = props.getProperty(JGraphpad.KEY_RECENTFILES + i++);
			while (tmp != null) {
				final String filename = tmp;
				if (JGraphEditor.isURL(filename) || new File(tmp).canRead()) {
					int len = tmp.length();
					if (len > MAX_DISPLAYLENGTH)
						tmp = "..."
								+ tmp.substring(len - MAX_DISPLAYLENGTH, len);

					// Creates a menu item using the short name as a label
					// and adds an action listener that opens the file.
					JMenuItem item = new JMenuItem(tmp);
					item.addActionListener(new ActionListener() {
						public void actionPerformed(ActionEvent e) {
							try {
								editor.getModel().addFile(filename);
							} catch (Exception ex) {
								// ex.printStackTrace();
								JGraphpadDialogs
										.getSharedInstance()
										.errorDialog(
												JGraphEditorAction
														.getPermanentFocusOwner(),
												ex.getMessage());
							}
						}
					});
					menu.add(item);
				}
				tmp = props.getProperty(JGraphpad.KEY_RECENTFILES + i++);
			}
		}
		return menu;
	}

}