/* 
 * $Id: JGraphpadTWikiAction.java,v 1.7 2005/10/09 12:00:04 gaudenz Exp $
 * Copyright (c) 2001-2005, Gaudenz Alder
 * 
 * All rights reserved.
 * 
 * See LICENSE file for license details. If you are unable to locate
 * this file please contact info (at) jgraph (dot) com.
 */
package com.jgraph.twikiplugin;

import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;
import java.util.zip.GZIPOutputStream;

import javax.imageio.ImageIO;

import org.jgraph.JGraph;

import com.jgraph.JGraphEditor;
import com.jgraph.codecplugin.JGraphpadCodecAction;
import com.jgraph.editor.JGraphEditorAction;
import com.jgraph.editor.JGraphEditorFile;
import com.jgraph.editor.JGraphEditorResources;
import com.jgraph.editor.factory.JGraphEditorDiagramPane;

/**
 * Implements all actions that require the browser launcher.
 */
public class JGraphpadTWikiAction extends JGraphpadCodecAction {

	/**
	 * Specifies the name for the <code>uploadToTWiki</code> action.
	 */
	public static final String NAME_UPLOADTOTWIKI = "uploadToTWiki";

	/**
	 * Specifies the name for the <code>uploadAndExit</code> action.
	 */
	public static final String NAME_UPLOADANDEXIT = "uploadAndExit";

	/**
	 * Constructs a new file action for the specified name and editor.
	 * 
	 * @param name
	 *            The name of the action to be created.
	 * @param editor
	 *            The enclosing editor for the action.
	 */
	public JGraphpadTWikiAction(String name, JGraphEditor editor) {
		super(name, editor);
	}

	/**
	 * Executes the action based on the action name.
	 * 
	 * @param e
	 *            The object that describes the event.
	 */
	public void actionPerformed(ActionEvent e) {
		try {
			JGraphEditorFile file = getPermanentFocusOwnerFile();
			JGraph graph = getPermanentFocusOwnerGraph();
			if (file != null && graph != null) {
				if (getName().equals(NAME_UPLOADTOTWIKI)) {
					doUploadToTWiki(file, graph, 0);
				} else if (getName().equals(NAME_UPLOADANDEXIT)) {
					doUploadToTWiki(file, graph, 0);
					doExit();
				}
			}
		} catch (Exception e1) {
			e1.printStackTrace();
			dlgs
					.errorDialog(getPermanentFocusOwner(), e1
							.getLocalizedMessage());
		}
	}

	protected void doUploadToTWiki(JGraphEditorFile file, JGraph graph,
			int inset) throws IOException {
		String uploadURL = (String) editor.getSettings().getObject("upload");
		if (uploadURL != null) {
			// Contains Full URL
			String filepath = file.getFilename();
			int lastSlash = filepath.lastIndexOf("/");
			// Contains basedir URL
			String baseurl = filepath.substring(0, lastSlash);
			// Contains filename with extension
			String filename = filepath.substring(lastSlash + 1);

			// Contains filename without extension
			String basename = filename;
			if (basename.toLowerCase().endsWith(".gz"))
				basename = basename.substring(0, basename.length() - 4);
			if (basename.toLowerCase().endsWith(".xml"))
				basename = basename.substring(0, basename.length() - 4);

			// Writes diagram file. Do not use
			// editor.getModel().getOutputStream()
			// for the file is we need to keep a reference to the innermost
			// bytearrayoutputstream for passing to the post method!
			OutputStream out = JGraphEditorResources.getOutputStream(filepath);
			OutputStream inner = out;
			if (filename.toLowerCase().endsWith((".gz")))
				out = new GZIPOutputStream(out);
			editor.getModel().writeObject(file, out);
			out.flush();
			out.close();

			// Posts inner output stream
			post(new URL(uploadURL), filename, (filename.toLowerCase()
					.endsWith(".gz")) ? "application/octet-stream"
					: MIME_PLAINTEXT, inner);

			// Writes map file
			String mapFilename = baseurl + "/" + basename + ".map";
			out = editor.getModel().getOutputStream(mapFilename);
			int written = writeMap(graph, basename, inset, out);
			out.flush();
			out.close();
			if (written > 0) {
				post(new URL(uploadURL), mapFilename, MIME_PLAINTEXT, out);
			}

			// Writes PNG image
			String imgFilename = baseurl + "/" + basename + ".png";
			out = editor.getModel().getOutputStream(imgFilename);
			JGraphEditorDiagramPane diagramPane = JGraphEditorDiagramPane
					.getParentDiagramPane(graph);
			BufferedImage img = diagramPane.getImage(null, inset);

			// Note: Use JGraphpadImageEncoder.writeGIF(img, out) for GIF files,
			// this is OK for all built-in types (eg. JPG, BMP and PNG).
			ImageIO.write(img, "png", out);
			out.flush();
			out.close();
			post(new URL(uploadURL), imgFilename, "image/png", out);

			file.setNew(false);
			file.setModified(false);
		}
	}

	/**
	 * Bundle of all actions in this class.
	 */
	public static class AllActions implements Bundle {

		/**
		 * Holds the actions. The actions are constructed in the constructor as
		 * they require an editor instance.
		 */
		public JGraphEditorAction actionUploadToTWiki, actionUploadAndExit;

		/**
		 * Constructs the action bundle for the specified editor.
		 * 
		 * @param editor
		 *            The enclosing editor for this bundle.
		 */
		public AllActions(JGraphEditor editor) {
			actionUploadToTWiki = new JGraphpadTWikiAction(NAME_UPLOADTOTWIKI,
					editor);
			actionUploadAndExit = new JGraphpadTWikiAction(NAME_UPLOADANDEXIT,
					editor);
		}

		/*
		 * (non-Javadoc)
		 */
		public JGraphEditorAction[] getActions() {
			return new JGraphEditorAction[] { actionUploadToTWiki,
					actionUploadAndExit };
		}

		/*
		 * (non-Javadoc)
		 */
		public void update() {
			// always enabled
		}

	}

}