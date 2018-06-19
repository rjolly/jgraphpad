/*
 * $Id: JGraphpadDemo.java,v 1.1 2006/01/31 15:33:25 gaudenz Exp $
 * Copyright (c) 2001-2005, Gaudenz Alder
 * 
 * All rights reserved.
 * 
 * See LICENSE file for license details. If you are unable to locate
 * this file please contact info (at) jgraph (dot) com.
 */
package com.jgraph;

import java.awt.Window;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import com.jgraph.editor.JGraphEditorFile;
import com.jgraph.editor.JGraphEditorKit;
import com.jgraph.editor.JGraphEditorModel;
import com.jgraph.pad.action.JGraphpadFileAction;

/**
 * @author Administrator
 * 
 */
public class JGraphpadDemo extends JGraphpad {

	/**
	 * 
	 */
	private static final String MESSAGE = "This demo does not allow to open or save files.";

	/**
	 * Issues a warning message in the online demo.
	 */
	public Window createApplication(List files, Map args)
			throws ParserConfigurationException, SAXException, IOException {
		args.put(ARG_JGOODIESLOOKANDFEEL, "");
		Window wnd = super.createApplication(null, args);
		System.err.println(MESSAGE);
		return wnd;
	}

	/**
	 * Creates a model that does not allow to add files.
	 */
	protected JGraphEditorModel createModel() {
		JGraphEditorModel model = new JGraphEditorModel() {
			public Object addFile(String uri) {
				throw new RuntimeException(MESSAGE);
			}
		};
		configureModel(model);
		return model;
	}

	/**
	 * Replaces some actions with empty implementations.
	 */
	protected void addActions(JGraphEditor editor, JGraphEditorKit kit) {
		super.addActions(editor, kit);
		kit.addBundle(new AllActions(editor));
	}

	/**
	 * Replaces file open / save action with different actions.
	 * 
	 */
	public static class JGraphpadDemoFileAction extends JGraphpadFileAction {

		/**
		 * @param name
		 * @param editor
		 */
		public JGraphpadDemoFileAction(String name, JGraphEditor editor) {
			super(name, editor);
		}

		/**
		 * Replaces the saveFile action with an error message.
		 */
		protected void doSaveFile(JGraphEditorFile file,
				boolean forceFilenameDialog, boolean urlDialog)
				throws IOException {
			throw new RuntimeException(MESSAGE);
		}

		/**
		 * Replaces the open action with an error message.
		 */
		public void doSave(String filename, byte[] data) throws Exception {
			throw new RuntimeException(MESSAGE);
		}

		/**
		 * Replaces the saveFile action with an error message.
		 */
		protected void doOpenFile(String filename)
				throws MalformedURLException, IOException {
			throw new RuntimeException(MESSAGE);
		}

	}

	/**
	 * Bundle of all actions in this class.
	 */
	public static class AllActions extends JGraphpadFileAction.AllActions {

		/**
		 * Constructs the action bundle for the specified editor.
		 * 
		 * @param editor
		 *            The enclosing editor for this bundle.
		 */
		public AllActions(JGraphEditor editor) {
			super(editor);
			actionClose = new JGraphpadDemoFileAction(
					JGraphpadFileAction.NAME_CLOSE, editor);
			actionCloseAll = new JGraphpadDemoFileAction(
					JGraphpadFileAction.NAME_CLOSE, editor);
			actionOpen = new JGraphpadDemoFileAction(
					JGraphpadFileAction.NAME_OPEN, editor);
			actionSave = new JGraphpadDemoFileAction(
					JGraphpadFileAction.NAME_SAVE, editor);
			actionSaveAs = new JGraphpadDemoFileAction(
					JGraphpadFileAction.NAME_SAVEAS, editor);
			actionSaveAll = new JGraphpadDemoFileAction(
					JGraphpadFileAction.NAME_SAVEALL, editor);
		}

		/*
		 * (non-Javadoc)
		 */
		public void update() {
			actionOpen.setEnabled(false);
			actionSave.setEnabled(false);
			actionSaveAs.setEnabled(false);
			actionSaveAll.setEnabled(false);
			actionUploadAs.setEnabled(false);
		}

	}

	//
	// Main
	//

	/**
	 * Constructs and displays a new application window.
	 * 
	 * @param args
	 *            The command line arguments to pass to the application.
	 */
	public static void main(String[] args) {
		try {
			new JGraphpadDemo().createApplication(null, new Hashtable());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
