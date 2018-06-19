/* 
 * $Id: JGraphpadFileAction.java,v 1.16 2007/08/29 09:30:49 gaudenz Exp $
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
import java.awt.KeyboardFocusManager;
import java.awt.event.ActionEvent;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.print.PageFormat;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.UnknownServiceException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;

import javax.imageio.ImageIO;
import javax.swing.Action;
import javax.swing.tree.TreeModel;

import org.jgraph.JGraph;
import org.jgraph.graph.ConnectionSet;
import org.jgraph.graph.DefaultGraphModel;
import org.jgraph.graph.GraphConstants;
import org.jgraph.graph.GraphLayoutCache;
import org.jgraph.graph.GraphModel;

import com.jgraph.JGraphEditor;
import com.jgraph.JGraphpad;
import com.jgraph.editor.JGraphEditorAction;
import com.jgraph.editor.JGraphEditorDiagram;
import com.jgraph.editor.JGraphEditorFile;
import com.jgraph.editor.JGraphEditorModel;
import com.jgraph.editor.JGraphEditorResources;
import com.jgraph.editor.factory.JGraphEditorDiagramPane;
import com.jgraph.editor.factory.JGraphEditorNavigator;
import com.jgraph.pad.JGraphpadDiagram;
import com.jgraph.pad.JGraphpadFile;
import com.jgraph.pad.JGraphpadLibrary;
import com.jgraph.pad.dialog.JGraphpadDialogs;
import com.jgraph.pad.factory.JGraphpadLibraryPane;
import com.jgraph.pad.factory.JGraphpadOpenRecentMenu;
import com.jgraph.pad.graph.JGraphpadRichTextValue;
import com.jgraph.pad.util.JGraphpadImageEncoder;

/**
 * Implements all actions of the file menu. The openRecent menu is implemented
 * using the {@link JGraphpadOpenRecentMenu} class, and the import/export
 * actions are added to the file menu by plugins, so look for their
 * implementations there.
 */
public class JGraphpadFileAction extends JGraphEditorAction {

	/**
	 * Defines the text/plain mime-type.
	 */
	public static final String MIME_PLAINTEXT = "text/plain",
			MIME_HTML = "text/html";

	/**
	 * Defines the (double) newline character as used in mime responses.
	 */
	static String NL = "\r\n", NLNL = NL + NL;

	/**
	 * Shortcut to the shared JGraphpad dialogs.
	 */
	protected static JGraphpadDialogs dlgs = JGraphpadDialogs
			.getSharedInstance();

	/**
	 * Import actions require the action value for this key to be assigned. The
	 * object under this key will be used to create new vertices for imports.
	 * 
	 * @see Action#putValue(java.lang.String, java.lang.Object)
	 */
	public static final String KEY_VERTEXPROTOTYPE = "vertexPrototype";

	/**
	 * Import actions require the action values for this key to be assigned. The
	 * object under this key will be used to create the edges for imports.
	 * 
	 * @see Action#putValue(java.lang.String, java.lang.Object)
	 */
	public static final String KEY_EDGEPROTOTYPE = "edgePrototype";

	/**
	 * Defines the key for the main windows object. This key is used to store a
	 * reference in the editor settings for the bounds of the application
	 * window. This class is in charge of storing the bounds in
	 * {@link JGraphpad#PATH_USERSETTINGS} on program termination.
	 */
	public static final String KEY_MAINWINDOW = JGraphpad.KEY_MAINWINDOW;

	/**
	 * Defines the key for the recent files user settings. This class is in
	 * charge of updating the list and storing it in
	 * {@link JGraphpad#PATH_USERSETTINGS} on program termination.
	 */
	public static final String KEY_RECENTFILES = "recentFiles";

	/**
	 * Defines the maximum number of recent files to store under the
	 * {@link #KEY_RECENTFILES} in the user settings file. Default is 6.
	 */
	public static int MAX_RECENTFILES = 6;

	/**
	 * Specifies the name for the <code>newDocument</code> action.
	 */
	public static final String NAME_NEWDOCUMENT = "newDocument";

	/**
	 * Specifies the name for the <code>newDiagram</code> action.
	 */
	public static final String NAME_NEWDIAGRAM = "newDiagram";

	/**
	 * Specifies the name for the <code>renameDiagram</code> action.
	 */
	public static final String NAME_RENAMEDIAGRAM = "renameDiagram";

	/**
	 * Specifies the name for the <code>removeDiagram</code> action.
	 */
	public static final String NAME_REMOVEDIAGRAM = "removeDiagram";

	/**
	 * Specifies the name for the <code>newLibrary</code> action.
	 */
	public static final String NAME_NEWLIBRARY = "newLibrary";

	/**
	 * Specifies the name for the <code>open</code> action.
	 */
	public static final String NAME_OPEN = "open";

	/**
	 * Specifies the name for the <code>download</code> action.
	 */
	public static final String NAME_DOWNLOAD = "download";

	/**
	 * Specifies the name for the <code>close</code> action.
	 */
	public static final String NAME_CLOSE = "close";

	/**
	 * Specifies the name for the <code>closeAll</code> action.
	 */
	public static final String NAME_CLOSEALL = "closeAll";

	/**
	 * Specifies the name for the <code>save</code> action.
	 */
	public static final String NAME_SAVE = "save";

	/**
	 * Specifies the name for the <code>saveAs</code> action.
	 */
	public static final String NAME_SAVEAS = "saveAs";

	/**
	 * Specifies the name for the <code>uploadAs</code> action.
	 */
	public static final String NAME_UPLOADAS = "uploadAs";

	/**
	 * Specifies the name for the <code>saveAll</code> action.
	 */
	public static final String NAME_SAVEALL = "saveAll";

	/**
	 * Specifies the name for the <code>saveImage</code> action.
	 */
	public static final String NAME_SAVEIMAGE = "saveImage";

	/**
	 * Specifies the name for the <code>importCSV</code> action.
	 */
	public static final String NAME_IMPORTCSV = "importCSV";

	/**
	 * Specifies the name for the <code>pageSetup</code> action.
	 */
	public static final String NAME_PAGESETUP = "pageSetup";

	/**
	 * Specifies the name for the <code>print</code> action.
	 */
	public static final String NAME_PRINT = "print";

	/**
	 * Specifies the name for the <code>exit</code> action.
	 */
	public static final String NAME_EXIT = "exit";

	/**
	 * References the enclosing editor.
	 */
	protected JGraphEditor editor;

	/**
	 * Holds the last directory for file operations.
	 */
	protected File lastDirectory = null;

	/**
	 * Constructs a new file action for the specified name and editor.
	 * 
	 * @param name
	 *            The name of the action to be created.
	 * @param editor
	 *            The enclosing editor for the action.
	 */
	public JGraphpadFileAction(String name, JGraphEditor editor) {
		super(name);
		this.editor = editor;
	}

	/**
	 * Executes the action based on the action name.
	 * 
	 * @param event
	 *            The object that describes the event.
	 */
	public void actionPerformed(ActionEvent event) {
		JGraph graph = getPermanentFocusOwnerGraph();
		try {
			if (getName().equals(NAME_NEWDOCUMENT))
				doNewDocument();
			else if (getName().equals(NAME_NEWLIBRARY))
				doNewLibrary();
			else if (getName().equals(NAME_SAVEALL))
				doSaveAll();
			else if (getName().equals(NAME_CLOSEALL))
				doCloseAll();
			else if (getName().equals(NAME_EXIT))
				doExit();

			// Actions that require a focused graph
			if (graph != null) {
				if (getName().equals(NAME_IMPORTCSV))
					doImportCSV(graph.getGraphLayoutCache(), dlgs.fileDialog(
							getPermanentFocusOwnerOrParent(),
							getString("OpenCSVFile"), true, ".csv",
							getString("CommaSeparatedFileDescription"),
							lastDirectory));
			}

			// Actions that require a focused file
			JGraphEditorFile file = getPermanentFocusOwnerFile();
			
			if (file != null) {
				if (getName().equals(NAME_NEWDIAGRAM))
					doNewDiagram(file);
				else if (getName().equals(NAME_SAVE))
					doSaveFile(file, false, false);
				else if (getName().equals(NAME_SAVEAS))
					doSaveFile(file, true, false);
				else if (getName().equals(NAME_UPLOADAS))
					doSaveFile(file, true, true);
				else if (getName().equals(NAME_CLOSE))
					doCloseFile(file, true);
			}
			
			if (getName().equals(NAME_OPEN))
				doOpenFile(dlgs.editorFileDialog(getActiveFrame(),
						getString("OpenJGraphpadFile"), null, true,
						lastDirectory), file);

			// Actions that require a focused diagram
			JGraphEditorDiagram diagram = getPermanentFocusOwnerDiagram();
			if (diagram != null) {
				if (getName().equals(NAME_REMOVEDIAGRAM))
					doRemoveDiagram(diagram);
				if (getName().equals(NAME_RENAMEDIAGRAM))
					doRenameDiagram(diagram);
			}

			// Actions that require a focused diagram pane
			JGraphEditorDiagramPane diagramPane = getPermanentFocusOwnerDiagramPane();
			if (diagramPane != null) {
				if (getName().equals(NAME_SAVEIMAGE))
					doSaveImage(diagramPane, 5, dlgs.imageFileDialog(
							diagramPane, getString("SaveImage"), false,
							lastDirectory));
				else if (getName().equals(NAME_PRINT))
					doPrintDiagramPane(diagramPane);
				else if (getName().equals(NAME_PAGESETUP))
					doPageSetup(diagramPane);

			}
		} catch (JGraphpadDialogs.CancelException e) {
			// ignore
		} catch (Exception e) {
			e.printStackTrace();
			dlgs.errorDialog(getPermanentFocusOwner(), e.getMessage());
		}
	}

	/**
	 * Saves all open files using
	 * {@link #doSaveFile(JGraphEditorFile, boolean, boolean)} showing file
	 * dialogs for files whose filename has not been assigned.
	 * 
	 * @throws IOException
	 *             If there was an error saving the files.
	 */
	protected void doSaveAll() throws IOException {
		Enumeration files = editor.getModel().roots();
		while (files.hasMoreElements()) {
			Object file = files.nextElement();
			if (file instanceof JGraphEditorFile)
				doSaveFile((JGraphEditorFile) file, false, false);
		}
	}

	/**
	 * Saves the specified file displaying a filename dialog if the filename is
	 * not set or if <code>forceFilenameDialog</code> is true. If
	 * <code>urlDialog</code> is true, then the dialog will ask for an URL
	 * instead of a local file.<br>
	 * This implementation does the following additional checks:
	 * <ul>
	 * <li>If the file already exists it asks whether it should be overwritten.</li>
	 * <li>It rejects to assign filenames of files which are already open.</li>
	 * <li>If filename is an URL it tries to upload the data to that URL.</li>
	 * </ul>
	 * Furthermore, this implementation updates the isNew and modified state of
	 * the file if it has successfully been saved.
	 * 
	 * @param file
	 *            The file to be saved.
	 * @param forceFilenameDialog
	 *            Whether to display a dialog regardless of the filename.
	 * @param urlDialog
	 *            Whether to display an URL dialog to specifiy the filename.
	 * @throws IOException
	 *             If the file cannot be saved.
	 * 
	 * @see JGraphEditorModel#getOutputStream(String)
	 * @see #postPlain(URL, String, OutputStream)
	 */
	protected void doSaveFile(JGraphEditorFile file,
			boolean forceFilenameDialog, boolean urlDialog) throws IOException {
		JGraphEditorModel model = editor.getModel();

		// Asks for a filename, using the file's current location
		// as the current directory on the dialog.
		String filename = file.getFilename();
		if (file.isNew() || forceFilenameDialog) {
			if (urlDialog) {
				String defaultValue = JGraphEditor.isURL(file.getFilename()) ? file
						.getFilename()
						: "http://";
				filename = dlgs
						.valueDialog(getString("EnterURL"), defaultValue);
			} else {
				// Strip extension as it will be added by the dialog
				// based on the chooseable filter.
				if (filename.toLowerCase().endsWith(".xml.gz"))
					filename = filename.substring(0, filename.length() - 7);
				else if (filename.toLowerCase().endsWith(".xml"))
					filename = filename.substring(0, filename.length() - 4);

				filename = dlgs.editorFileDialog(
						getPermanentFocusOwnerOrParent(),
						getString("SaveJGraphpadFile"), filename, false,
						lastDirectory);
			}
		}

		// Stores the file under the given filename or URL
		if (filename != null) {
			Object check = model.getFileByFilename(filename);
			if (check != null && check != file) {
				dlgs.errorDialog(getPermanentFocusOwner(),
						getString("FileAlreadyOpen"));
			} else {
				OutputStream out = editor.getModel().getOutputStream(filename);
				model.writeObject(file, out);
				out.flush();
				out.close();
				if (JGraphEditor.isURL(filename)) {
					URL url = new URL(filename);
					postPlain(url, url.getFile(), out);
				} else
					lastDirectory = new File(filename).getParentFile();
				if (file.getFilename() == null
						|| !file.getFilename().equals(filename))
					model.setFilename(file, filename);
				file.setNew(false);
				model.setModified(file, false);
				editor.getSettings().pushListEntryProperty(
						JGraphpad.NAME_USERSETTINGS, KEY_RECENTFILES, filename,
						MAX_RECENTFILES);
			}
		}
	}

	/**
	 * Saves the specified graph as an image using <code>inset</code> as the
	 * size of the empty border around the image. This implementation displays a
	 * dialog to ask for transparency-support if the chosen fileformat supports
	 * it (eg. PNG, GIF). If filename is an URL it tries to upload the data to
	 * that URL.
	 * 
	 * @param diagramPane
	 *            The diagram pane to save to the image for.
	 * @param inset
	 *            The size of the empty border around the image.
	 * @param filename
	 *            The filename to save or upload the image to.
	 * @throws IOException
	 *             If the image cannot be saved.
	 * @throws IllegalArgumentException
	 *             If the graph contains no cells.
	 * 
	 * @see JGraph#getImage(Color, int)
	 * @see ImageIO#write(java.awt.image.RenderedImage, java.lang.String,
	 *      java.io.OutputStream)
	 * @see JGraphEditorModel#getOutputStream(String)
	 * @see #post(URL, String, String, OutputStream)
	 */
	protected void doSaveImage(JGraphEditorDiagramPane diagramPane, int inset,
			String filename) throws IOException {
		if (filename != null) {
			JGraph graph = diagramPane.getGraph();
			if (graph != null && graph.getModel().getRootCount() > 0) {
				String ext = filename.substring(filename.lastIndexOf(".") + 1)
						.toLowerCase();
				Color bg = null;
				if (!(ext.equals("png") || ext.equals("gif"))
						|| !dlgs.confirmDialog(JGraphEditorNavigator
								.getParentScrollPane(graph),
								getString("MakeTransparent"), true, false))
					bg = graph.getBackground();
				BufferedImage img = diagramPane.getImage(bg, inset);
				OutputStream out = editor.getModel().getOutputStream(filename);
				if (ext.equals("gif"))
					JGraphpadImageEncoder.writeGIF(img, out);
				else
					ImageIO.write(img, ext, out);
				out.flush();
				out.close();
				if (JGraphEditor.isURL(filename)) {
					URL url = new URL(filename);
					if (ext.equals("jpg"))
						ext = "jpeg"; // image/jpeg
					post(url, url.getFile(), "image/" + ext, out);
				} else
					lastDirectory = new File(filename).getParentFile();
			} else {
				throw new IllegalArgumentException(getString("DiagramIsEmpty"));
			}
		}
	}

	/**
	 * Saves the specified byte array to the specified file.
	 * 
	 * @param filename
	 *            The filename of the file to be written.
	 * @param data
	 *            The array of bytes to write to the file.
	 */
	public void doSave(String filename, byte[] data) throws Exception {
		if (filename != null) {
			OutputStream out = editor.getModel().getOutputStream(filename);
			out.write(data);
			out.close();
			if (JGraphEditor.isURL(filename)) {
				URL url = new URL(filename);
				post(url, url.getFile(), MIME_HTML, out);
			} else
				lastDirectory = new File(filename).getParentFile();
		}
	}

	/**
	 * Displays a file- or URL-dialog and loads the selected file or URL into
	 * the specified diagram as a comma separated value file (CSV) using
	 * {@link #importCSVFile(GraphLayoutCache, InputStream, String, Object, Object, String)}.
	 * 
	 * @param cache
	 *            The graph layout cache to import into.
	 * @param filename
	 *            The filename to import from.
	 * @throws IOException
	 *             If the file cannot be read.
	 * 
	 * @see JGraphEditorModel#getInputStream(String)
	 */
	protected void doImportCSV(GraphLayoutCache cache, String filename)
			throws IOException {
		if (filename != null) {
			InputStream in = editor.getModel().getInputStream(filename);
			Object vertexPrototype = getValue(KEY_VERTEXPROTOTYPE);
			Object edgePrototype = getValue(KEY_EDGEPROTOTYPE);
			importCSVFile(cache, in, ",", vertexPrototype, edgePrototype, "");
			in.close();
			lastDirectory = new File(filename).getParentFile();
		}
	}

	/**
	 * Displays a system print dialog and prints the specified diagram pane.
	 * 
	 * @param diagramPane
	 *            The diagram pane to be printed.
	 * @throws PrinterException
	 *             If the document can not be printed.
	 */
	protected void doPrintDiagramPane(JGraphEditorDiagramPane diagramPane)
			throws PrinterException {
		PrinterJob printJob = PrinterJob.getPrinterJob();
		printJob.setPrintable(diagramPane);
		if (printJob.printDialog())
			printJob.print();
	}

	/**
	 * Displays the system page format dialog and updates the pageformat on the
	 * specified diagram pane.
	 * 
	 * @param diagramPane
	 *            The diagram pane to set the page format.
	 */
	protected void doPageSetup(JGraphEditorDiagramPane diagramPane) {
		PageFormat format = PrinterJob.getPrinterJob().pageDialog(
				diagramPane.getPageFormat());
		if (format != null)
			diagramPane.setPageFormat(format);
	}

	/**
	 * Opens the specified filename using
	 * {@link JGraphEditorModel#addFile(String)} if a file for the specified
	 * name is not already open.
	 * 
	 * @param filename
	 *            The name of the file to be opened.
	 * @throws IOException
	 *             If there was an error reading the file.
	 * @throws MalformedURLException
	 *             If the filename is an URL but the URL is invalid.
	 */
	protected void doOpenFile(String filename, JGraphEditorFile file) throws MalformedURLException,
			IOException {
		if (filename != null) {
			JGraphEditorModel model = editor.getModel();
			if (model.getFileByFilename(filename) == null) {
				if (JGraphpad.INNER_LIBRARIES)
				{
					if (file instanceof JGraphpadLibrary)
					{
						if (file.getParent() instanceof JGraphEditorFile)
						{
							file = (JGraphEditorFile) file.getParent();
						}
					}
					
					Object newFile = model.readFile(filename);
					
					if (file != null &&
						newFile instanceof JGraphpadLibrary)
					{
						model.addChild((JGraphpadLibrary) newFile, file);
					}
					else if (newFile instanceof JGraphEditorFile &&
						!(newFile instanceof JGraphpadLibrary))
					{
						model.addRoot((JGraphEditorFile) newFile);
					}
				}
				else
				{
					model.addFile(filename);
				}
				editor.getSettings().pushListEntryProperty(
						JGraphpad.NAME_USERSETTINGS, KEY_RECENTFILES, filename,
						MAX_RECENTFILES);
				if (!JGraphEditor.isURL(filename))
					lastDirectory = new File(filename).getParentFile();
			} else
				dlgs.informationDialog(getPermanentFocusOwner(),
						getString("FileAlreadyOpen"));
		}
	}

	/**
	 * Closes all open files using {@link #doCloseAll()}, saves the user
	 * settings and terminates the program using {@link System#exit(int)}. This
	 * implementation allows the {@link JGraphpad#PATH_USERSETTINGS} to be a
	 * URL.
	 * 
	 * @throws IOException
	 *             If there was an error saving unsaved changes.
	 */
	protected void doExit() throws IOException {
		Enumeration files = editor.getModel().roots();
		while (files.hasMoreElements()) {
			Object obj = files.nextElement();
			if (obj instanceof JGraphEditorFile)
				doCloseFile((JGraphEditorFile) obj, false);
		}
		editor.exit(1);
	}

	/**
	 * Closes all open files using
	 * {@link #doCloseFile(JGraphEditorFile, boolean)} giving the user a chance
	 * to save unsaved changes before closing each file.
	 * 
	 * @throws IOException
	 *             If there was an error saving unsaved changes.
	 */
	protected void doCloseAll() throws IOException {
		// Closes all files in a two step process to
		// avoid concurrent modifications and simplify
		// things. First gets all "roots" from the model.
		JGraphEditorModel model = editor.getModel();
		Object[] files = new Object[model.getChildCount(model.getRoot())];
		for (int i = 0; i < files.length; i++)
			files[i] = model.getChild(model.getRoot(), i);

		// Then closes all files, thereby removing them
		// from the childset of the root.
		for (int i = 0; i < files.length; i++)
			if (files[i] instanceof JGraphEditorFile)
				doCloseFile((JGraphEditorFile) files[i], true);
	}

	/**
	 * Closes the specified file by removing it from the parent. This
	 * implementation displays a dialog if there are unsaved changes and calls
	 * {@link #doSaveFile(JGraphEditorFile, boolean, boolean)} if the user
	 * chooses to save the changes.
	 * 
	 * @param file
	 *            The file to be closed.
	 * @param remove
	 *            Whether the file should be removed from the model.
	 * @throws IOException
	 *             If the unsaved changes can not be saved.
	 */
	protected void doCloseFile(JGraphEditorFile file, boolean remove)
			throws IOException {
		if (file.isModified()
				&& dlgs.confirmDialog(getPermanentFocusOwnerOrParent(),
						JGraphEditorResources.getString("SaveChanges", file),
						true, true)) {
			doSaveFile(file, false, false);
		}
		if (remove)
			editor.getModel().removeNodeFromParent(file);
	}

	/**
	 * Inserts a new empty document into the document model. The name is
	 * assigned automatically using the Document resource string and the number
	 * returned by {@link #getFileCount(boolean)}.
	 */
	protected void doNewDocument() {
		JGraphEditorModel model = editor.getModel();
		int number = getFileCount(false) + 1;
		JGraphpadFile file = new JGraphpadFile(getString("Document") + number);
		model.addRoot(file);
		doNewDiagram(file);
	}

	/**
	 * Inserts a new empty diagram into the specified file. The name is assigned
	 * automatically using the Diagram resource string and the number returned
	 * by {@link TreeModel#getChildCount(java.lang.Object)} for the file.
	 * 
	 * @param file
	 *            The file to add the diagram to.
	 */
	protected void doNewDiagram(JGraphEditorFile file) {
		if (!isLibrary(file)) {
			JGraphEditorModel model = editor.getModel();
			int number = model.getChildCount(file) + 1;
			JGraphpadDiagram newDiagram = new JGraphpadDiagram(
					getString("Diagram") + number);
			model.addChild(newDiagram, (JGraphpadFile) file);
		}
	}

	/**
	 * Inserts a new library into the given file. The name is assigned
	 * automatically using the Library resource string and the number returned
	 * by {@link #getFileCount(boolean)}.
	 */
	protected void doNewLibrary() {
		JGraphEditorModel model = editor.getModel();
		
		JGraphEditorFile file = null;
		if (JGraphpad.INNER_LIBRARIES)
		{
			file = getPermanentFocusOwnerFile();
			if (file instanceof JGraphpadLibrary)
			{
				if (file.getParent() instanceof JGraphEditorFile)
				{
					file = (JGraphEditorFile) file.getParent();
				}
			}
		}
		
		int number = getFileCount(file, true) + 1;
		JGraphpadLibrary library = new JGraphpadLibrary(getString("Library")
				+ number);
		
		if (JGraphpad.INNER_LIBRARIES)
		{
			if (file != null)
			{
				model.addChild(library, file);
			}
		}
		else
		{
			model.addRoot(library);
		}
	}
	
	/**
	 * Removes the specified diagram from the parent file if the parent file
	 * contains at least one diagram after the removal. Otherwise the diagram is
	 * not removed. This implementation displays a confirmation dialog before
	 * actually removing the diagram.
	 * 
	 * @param diagram
	 *            The diagram to be removed from its parent file.
	 * 
	 * @see JGraphpadDialogs#confirmDialog(Component, String, boolean, boolean)
	 */
	protected void doRemoveDiagram(JGraphEditorDiagram diagram) {
		JGraphEditorFile file = JGraphEditorModel.getParentFile(diagram);

		// A file needs at least one diagram to be able to add new
		// diagrams to it. This is because in order to find the
		// "focused" file the system tries to first find the focused
		// diagram pane, and goes to the file from there. Hence, if
		// there is no diagram pane the focused file cannot be found.
		if (file.getChildCount() > 1
				&& dlgs.confirmDialog(getPermanentFocusOwnerOrParent(),
						JGraphEditorResources.getString("RemoveDiagram",
								diagram), true, false)) {
			editor.getModel().removeNodeFromParent(diagram);
		}
	}

	/**
	 * Displays a dialog to enter the new name for the specified diagram and
	 * updates the name of the diagram using
	 * {@link JGraphEditorModel#setName(JGraphEditorDiagram, String)}.
	 * 
	 * @param diagram
	 *            The diagram to be renamed.
	 * 
	 * @see JGraphpadDialogs#valueDialog(String, String)
	 */
	protected void doRenameDiagram(JGraphEditorDiagram diagram) {
		String newValue = dlgs.valueDialog(getString("EnterName"), diagram
				.getName());
		if (newValue != null)
			editor.getModel().setName(diagram, newValue);
	}

	/**
	 * Uses {@link #isLibrary(JGraphEditorFile)} to find the number of roots
	 * that are instances of {@link JGraphEditorFile}. If
	 * <code>countLibraries</code> is true, then the number of libraries is
	 * returned. <br>
	 * The following always holds:
	 * <code>getFileCount(false) + getFileCount(true) ==
	 * getChildCount(getRoot())</code>
	 * 
	 * @param countLibraries
	 *            Whether libraries or non-libraries should be counted.
	 * @return Returns the number of libraries or non-libraries.
	 */
	protected int getFileCount(boolean countLibraries) {
		return getFileCount(null, countLibraries);
	}

	/**
	 * Uses {@link #isLibrary(JGraphEditorFile)} to find the number of roots
	 * that are instances of {@link JGraphEditorFile}. If
	 * <code>countLibraries</code> is true, then the number of libraries is
	 * returned. <br>
	 * The following always holds:
	 * <code>getFileCount(false) + getFileCount(true) ==
	 * getChildCount(getRoot())</code>
	 * 
	 * @param countLibraries
	 *            Whether libraries or non-libraries should be counted.
	 * @return Returns the number of libraries or non-libraries.
	 */
	protected int getFileCount(Object root, boolean countLibraries) {
		JGraphEditorModel model = editor.getModel();

		if (root == null)
		{
			root = model.getRoot();
		}
		
		int result = 0;
		int childCount = model.getChildCount(root);
		for (int i = 0; i < childCount; i++) {
			Object child = model.getChild(root, i);
			if (child instanceof JGraphEditorFile) {
				JGraphEditorFile file = (JGraphEditorFile) child;
				if (countLibraries == isLibrary(file))
					result++;
			}
		}
		return result;
	}
	
	/**
	 * Returns true if the specified file is a library. This implementation
	 * returns true if file is an instance of {@link JGraphpadLibrary}.
	 * 
	 * @param file
	 *            The file to be checked.
	 * @return Returns true if <code>file</code> is a library.
	 */
	protected boolean isLibrary(JGraphEditorFile file) {
		return file instanceof JGraphpadLibrary;
	}

	/**
	 * Returns the diagram for the permanent focus owner diagram pane.
	 */
	public static JGraphEditorFile getPermanentFocusOwnerFile() {
		JGraphEditorDiagram diagram = getPermanentFocusOwnerDiagram();
		if (diagram != null)
			return JGraphEditorModel.getParentFile(diagram);
		JGraphpadLibraryPane libraryPane = getPermanentFocusOwnerLibraryPane();
		if (libraryPane != null)
			return libraryPane.getLibrary();
		return null;
	}

	/**
	 * Reads the specified input stream as a comma-delimeted file, using the
	 * following format: a,b[,c] where a and b are vertices and c is the label
	 * of the edge to be inserted between a and b. For example, to create a
	 * triangle, use:<br>
	 * a,b,ab<br>
	 * b,c,bc<br>
	 * c,a,ca<br>
	 * 
	 * @param cache
	 *            The layout cache to import the file into.
	 * @param fstream
	 *            The stream to import the cells from.
	 * @param delim
	 *            The delimeter to parse the tokens.
	 * @param vertexPrototype
	 *            The prototype to create new vertices with.
	 * @param edgePrototype
	 *            The prototype to create new edges with.
	 * @param defaultEdgeLabel
	 *            The default label to use for edges if none is specified.
	 * @throws IOException
	 */
	public static void importCSVFile(GraphLayoutCache cache,
			InputStream fstream, String delim, Object vertexPrototype,
			Object edgePrototype, String defaultEdgeLabel) throws IOException {
		GraphModel model = cache.getModel();

		// Convert our input stream to a
		// DataInputStream
		DataInputStream in = new DataInputStream(fstream);

		// Continue to read lines while
		// there are still some left to read
		// Map from keys to vertices
		Hashtable map = new Hashtable();

		// Adds the existing vertices from the graph layout cache
		// into the vertex map for reference by the keys in the file.
		Object[] items = DefaultGraphModel.getAll(model);
		if (items != null) {
			for (int i = 0; i < items.length; i++)
				if (items[i] != null && items[i].toString() != null
						&& !model.isPort(items[i]) && !model.isEdge(items[i])) {
					map.put(items[i].toString(), items[i]);
				}
		}

		// Geometry of the import matrix
		int cols = 8;
		int offset = 40;
		int w = 100;
		int h = 100;

		// Vertices and Edges to insert
		Hashtable adj = new Hashtable();
		// Make space for a minimum of 4 bytes per entry
		List insert = new ArrayList(in.available() / 4);
		ConnectionSet cs = new ConnectionSet();
		while (in.available() != 0) {
			String s = in.readLine();
			StringTokenizer st = new StringTokenizer(s, delim);
			if (st.hasMoreTokens()) {
				String srckey = st.nextToken().trim();

				// Get or create source vertex
				Object source = getCellForKey(model, vertexPrototype, map,
						srckey, cols, w, h, offset, false);
				if (!model.contains(source) && !insert.contains(source))
					insert.add(source);
				if (st.hasMoreTokens()) {
					String tgtkey = st.nextToken().trim();

					// Get or create source vertex
					Object target = getCellForKey(model, vertexPrototype, map,
							tgtkey, cols, w, h, offset, false);
					if (!model.contains(target) && !insert.contains(target))
						insert.add(target);

					// Create and insert Edge
					Set neighbours = (Set) adj.get(srckey);
					if (neighbours == null) {
						neighbours = new HashSet();
						adj.put(srckey, neighbours);
					}
					String label = (st.hasMoreTokens()) ? st.nextToken().trim()
							: defaultEdgeLabel;
					if (!(neighbours.contains(tgtkey))) {
						Object edge = DefaultGraphModel.cloneCell(model,
								edgePrototype);
						model.valueForCellChanged(edge, label);
						Object sourcePort = model.getChild(source, 0);
						Object targetPort = model.getChild(target, 0);
						if (sourcePort != null && targetPort != null) {
							cs.connect(edge, sourcePort, targetPort);
							insert.add(edge);
							neighbours.add(tgtkey);
						}
					}
				}
			}
		}
		in.close();
		cache.insert(insert.toArray(), null, cs, null, null);
	}

	/**
	 * Utility method to return the cell stored under key in the specified map
	 * or create the cell using the specified prototype and model and put it
	 * into the map under key. The cells will be positioned into a matrix with
	 * <code>cols</code> columns and entries of size (w,h).
	 * 
	 * @param model
	 *            The model to use for cloning the prototype.
	 * @param prototype
	 *            The prototype to use for creating new cells.
	 * @param map
	 *            The map to check whether the cell exists for key.
	 * @param key
	 *            The key to return the cell for.
	 * @param cols
	 *            The number of columns for the matrix.
	 * @param w
	 *            The width of the entries.
	 * @param h
	 *            The height of the entries.
	 * @param offset
	 *            The offset from the top left.
	 * @param image
	 *            Whether to insert image or text cells.
	 * @return Returns the cell for the specified key.
	 */
	public static Object getCellForKey(GraphModel model, Object prototype,
			Hashtable map, String key, int cols, int w, int h, int offset,
			boolean image) {
		Object cell = map.get(key);
		if (cell == null) {
			cell = DefaultGraphModel.cloneCell(model, prototype);
			if (image)
				model.valueForCellChanged(cell, key);
			else
				model
						.valueForCellChanged(cell, new JGraphpadRichTextValue(
								key));

			GraphConstants.setResize(model.getAttributes(cell), true);

			// Set initial Location
			int col = map.size() / cols;
			int row = map.size() % cols;
			Rectangle2D bounds = new Rectangle2D.Double(row * w + offset, col
					* h + offset, 10, 10);
			GraphConstants.setBounds(model.getAttributes(cell), bounds);
			map.put(key, cell);
		}
		return cell;
	}

	/**
	 * Posts the data to the specified url using <code>path</code> to specify
	 * the filename in the mime response using for type {@link #MIME_PLAINTEXT}.
	 * 
	 * @param url
	 *            The url to post the mime response to.
	 * @param path
	 *            The filename to use in the mime response.
	 * @param data
	 *            The binary data to send with the mime response.
	 * @return Returns true if the data was successfuly posted.
	 * @throws IOException
	 */
	public static boolean postPlain(URL url, String path, OutputStream data)
			throws IOException {
		return post(url, path, data.toString(), MIME_PLAINTEXT);
	}

	/**
	 * Posts the data to the specified url using <code>path</code> to specify
	 * the filename in the mime response for the specified mime type.
	 * 
	 * @param url
	 *            The url to post the mime response to.
	 * @param path
	 *            The filename to use in the mime response.
	 * @param mime
	 *            The mime type to use for the response.
	 * @param data
	 *            The binary data to send with the mime response.
	 * @return Returns true if the data was successfuly posted.
	 * @throws IOException
	 */
	public static boolean post(URL url, String path, String mime,
			OutputStream data) throws IOException {
		return post(url, path, mime, convert(data, mime));
	}

	/**
	 * Converts the specified data stream into a string assuming the data stream
	 * is of the specified mime type. This performs a byte to char conversion on
	 * all mime types other than {@link #MIME_PLAINTEXT}.
	 * 
	 * @param data
	 *            The data to be converted.
	 * @param mime
	 *            The mime type to assume for the data.
	 * @return Returns a string representation of the data in the stream.
	 */
	public static String convert(OutputStream data, String mime) {
		String text = null;
		if (data instanceof ByteArrayOutputStream) {
			byte[] aByte = ((ByteArrayOutputStream) data).toByteArray();
			int size = aByte.length;
			char[] aChar = new char[size];
			for (int i = 0; i < size; i++) {
				aChar[i] = (char) aByte[i];
			}
			text = String.valueOf(aChar, 0, aChar.length);
		} else
			text = data.toString();
		return text;
	}

	/**
	 * Posts the data to the specified url using <code>path</code> to specify
	 * the filename in the mime response for the specified mime type.
	 * 
	 * @param url
	 *            The url to post the mime response to.
	 * @param path
	 *            The filename to use in the mime response.
	 * @param mime
	 *            The mime type to use for the response.
	 * @param data
	 *            The binary data to send with the mime response.
	 * @return Returns true if the data was successfuly posted.
	 * @throws IOException
	 */
	public static boolean post(URL url, String path, String mime, String data)
			throws IOException {
		String sep = "89692781418184";
		while (data.indexOf(sep) != -1)
			sep += "x";
		String message = makeMimeForm("", mime, path, data, "", sep);

		// Ask for parameters
		URLConnection connection = url.openConnection();
		connection.setAllowUserInteraction(false);
		connection.setDoOutput(true);
		connection.setUseCaches(false);
		connection.setRequestProperty("Content-type",
				"multipart/form-data; boundary=" + sep);
		connection.setRequestProperty("Content-length", Integer
				.toString(message.length()));

		String replyString = null;
		try {
			DataOutputStream out = new DataOutputStream(connection
					.getOutputStream());
			out.writeBytes(message);
			out.close();
			try {
				BufferedReader in = new BufferedReader(new InputStreamReader(
						connection.getInputStream()));
				String reply = null;
				while ((reply = in.readLine()) != null) {
					if (reply.startsWith("ERROR ")) {
						replyString = reply.substring("ERROR ".length());
					}
				}
				in.close();
			} catch (IOException ioe) {
				replyString = ioe.toString();
			}
		} catch (UnknownServiceException use) {
			replyString = use.getMessage();
		}
		if (replyString != null) {
			return false;
		} else {
			return true;
		}
	}

	/**
	 * Returns a mime form using the specified parameters.
	 */
	public static String makeMimeForm(String fileName, String type,
			String path, String content, String comment, String sep) {
		String binary = "";
		if (type.startsWith("image/") || type.startsWith("application")) {
			binary = "Content-Transfer-Encoding: binary" + NL;
		}
		String mime_sep = NL + "--" + sep + NL;
		return "--" + sep + "\r\n"
				+ "Content-Disposition: form-data; name=\"filename\"" + NLNL
				+ fileName + mime_sep
				+ "Content-Disposition: form-data; name=\"noredirect\"" + NLNL
				+ 1 + mime_sep
				+ "Content-Disposition: form-data; name=\"filepath\"; "
				+ "filename=\"" + path + "\"" + NL + "Content-Type: " + type
				+ NL + binary + NL + content + mime_sep
				+ "Content-Disposition: form-data; name=\"filecomment\"" + NLNL
				+ comment + NL + "--" + sep + "--" + NL;
	}

	/**
	 * Returns the permanent focus owner library pane.
	 */
	public static JGraphpadLibraryPane getPermanentFocusOwnerLibraryPane() {
		Component component = KeyboardFocusManager
				.getCurrentKeyboardFocusManager().getPermanentFocusOwner();
		return JGraphpadLibraryPane.getParentLibraryPane(component);
	}

	/**
	 * Bundle of all actions in this class.
	 */
	public static class AllActions implements Bundle {

		/**
		 * Holds the actions. All actions require an editor reference and are
		 * therefore created at construction time.
		 */
		public JGraphEditorAction actionNewDocument, actionNewDiagram,
				actionNewLibrary, actionOpen, actionDownload, actionSave,
				actionSaveAs, actionUploadAs, actionSaveAll,
				actionRenameDiagram, actionRemoveDiagram, actionClose,
				actionCloseAll, actionSaveImage, actionImportCSV, actionPrint,
				actionPageSetup, actionExit;

		/**
		 * Constructs the action bundle for the specified editor.
		 * 
		 * @param editor
		 *            The enclosing editor for this bundle.
		 */
		public AllActions(JGraphEditor editor) {
			Object vertexPrototype = editor.getSettings().getObject(
					JGraphpad.KEY_VERTEXPROTOTYPE);
			Object edgePrototype = editor.getSettings().getObject(
					JGraphpad.KEY_EDGEPROTOTYPE);
			actionNewDocument = new JGraphpadFileAction(NAME_NEWDOCUMENT,
					editor);
			actionNewDiagram = new JGraphpadFileAction(NAME_NEWDIAGRAM, editor);
			actionNewLibrary = new JGraphpadFileAction(NAME_NEWLIBRARY, editor);
			actionOpen = new JGraphpadFileAction(NAME_OPEN, editor);
			actionDownload = new JGraphpadFileAction(NAME_DOWNLOAD, editor);
			actionSave = new JGraphpadFileAction(NAME_SAVE, editor);
			actionSaveAs = new JGraphpadFileAction(NAME_SAVEAS, editor);
			actionUploadAs = new JGraphpadFileAction(NAME_UPLOADAS, editor);
			actionSaveAll = new JGraphpadFileAction(NAME_SAVEALL, editor);
			actionRemoveDiagram = new JGraphpadFileAction(NAME_REMOVEDIAGRAM,
					editor);
			actionRenameDiagram = new JGraphpadFileAction(NAME_RENAMEDIAGRAM,
					editor);
			actionPrint = new JGraphpadFileAction(NAME_PRINT, editor);
			actionPageSetup = new JGraphpadFileAction(NAME_PAGESETUP, editor);
			actionClose = new JGraphpadFileAction(NAME_CLOSE, editor);
			actionCloseAll = new JGraphpadFileAction(NAME_CLOSEALL, editor);
			actionSaveImage = new JGraphpadFileAction(NAME_SAVEIMAGE, editor);
			actionExit = new JGraphpadFileAction(NAME_EXIT, editor);
			actionImportCSV = new JGraphpadFileAction(NAME_IMPORTCSV, editor);
			actionImportCSV.putValue(KEY_VERTEXPROTOTYPE, vertexPrototype);
			actionImportCSV.putValue(KEY_EDGEPROTOTYPE, edgePrototype);
		}

		/*
		 * (non-Javadoc)
		 */
		public JGraphEditorAction[] getActions() {
			return new JGraphEditorAction[] { actionNewDocument,
					actionNewDiagram, actionNewLibrary, actionOpen,
					actionDownload, actionSave, actionSaveAs, actionUploadAs,
					actionSaveAll, actionRemoveDiagram, actionRenameDiagram,
					actionPrint, actionPageSetup, actionClose, actionCloseAll,
					actionSaveImage, actionImportCSV, actionExit };
		}

		/*
		 * (non-Javadoc)
		 */
		public void update() {
			JGraphEditorDiagram diagram = getPermanentFocusOwnerDiagram();
			JGraphEditorFile file = getPermanentFocusOwnerFile();
			boolean isDiagramFocused = (diagram != null);
			boolean isFileModified = (file != null && (file.isModified() || file
					.isNew()));
			actionOpen.setEnabled(true);
			actionRemoveDiagram.setEnabled(isDiagramFocused);
			actionRenameDiagram.setEnabled(isDiagramFocused);
			actionNewDiagram.setEnabled(isDiagramFocused);
			actionNewLibrary.setEnabled(!JGraphpad.INNER_LIBRARIES || (file != null));
			actionImportCSV.setEnabled(isDiagramFocused);
			actionSaveImage.setEnabled(isDiagramFocused);
			actionClose.setEnabled(file != null);
			actionCloseAll.setEnabled(file != null);
			actionSave.setEnabled(isFileModified);
			actionSaveAs.setEnabled(file != null);
			actionSaveAll.setEnabled(file != null);
			actionUploadAs.setEnabled(file != null);
		}

	}

};