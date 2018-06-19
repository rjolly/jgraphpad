/* 
 * $Id: JGraphpad.java,v 1.24 2007/07/28 09:41:33 gaudenz Exp $
 * Copyright (c) 2001-2005, Gaudenz Alder
 * 
 * All rights reserved.
 * 
 * See LICENSE file for license details. If you are unable to locate
 * this file please contact info (at) jgraph (dot) com.
 */
package com.jgraph;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.KeyboardFocusManager;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.beans.DefaultPersistenceDelegate;
import java.beans.Encoder;
import java.beans.Expression;
import java.beans.PersistenceDelegate;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Authenticator;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JDesktopPane;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JWindow;
import javax.swing.ToolTipManager;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.tree.MutableTreeNode;
import javax.xml.parsers.ParserConfigurationException;

import org.jgraph.JGraph;
import org.jgraph.event.GraphSelectionEvent;
import org.jgraph.event.GraphSelectionListener;
import org.jgraph.graph.AttributeMap;
import org.jgraph.graph.ConnectionSet;
import org.jgraph.graph.DefaultEdge;
import org.jgraph.graph.DefaultGraphCell;
import org.jgraph.graph.DefaultPort;
import org.jgraph.graph.Edge;
import org.jgraph.graph.GraphCell;
import org.jgraph.graph.GraphConstants;
import org.jgraph.graph.GraphLayoutCache;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import com.jgraph.editor.JGraphEditorAction;
import com.jgraph.editor.JGraphEditorFactory;
import com.jgraph.editor.JGraphEditorKit;
import com.jgraph.editor.JGraphEditorModel;
import com.jgraph.editor.JGraphEditorPlugin;
import com.jgraph.editor.JGraphEditorResources;
import com.jgraph.editor.JGraphEditorSettings;
import com.jgraph.editor.JGraphEditorTool;
import com.jgraph.editor.factory.JGraphEditorComboBox;
import com.jgraph.editor.factory.JGraphEditorNavigator;
import com.jgraph.pad.JGraphpadDiagram;
import com.jgraph.pad.JGraphpadFile;
import com.jgraph.pad.JGraphpadLibrary;
import com.jgraph.pad.action.JGraphpadCellAction;
import com.jgraph.pad.action.JGraphpadEditAction;
import com.jgraph.pad.action.JGraphpadFileAction;
import com.jgraph.pad.action.JGraphpadFormatAction;
import com.jgraph.pad.action.JGraphpadHelpAction;
import com.jgraph.pad.action.JGraphpadViewAction;
import com.jgraph.pad.dialog.JGraphpadAuthenticator;
import com.jgraph.pad.factory.JGraphpadComboBox;
import com.jgraph.pad.factory.JGraphpadConsole;
import com.jgraph.pad.factory.JGraphpadLibraryPane;
import com.jgraph.pad.factory.JGraphpadOpenRecentMenu;
import com.jgraph.pad.factory.JGraphpadPane;
import com.jgraph.pad.factory.JGraphpadStatusBar;
import com.jgraph.pad.factory.JGraphpadWindowMenu;
import com.jgraph.pad.graph.JGraphpadBusinessObject;
import com.jgraph.pad.graph.JGraphpadEdgeView;
import com.jgraph.pad.graph.JGraphpadGraph;
import com.jgraph.pad.graph.JGraphpadGraphConstants;
import com.jgraph.pad.graph.JGraphpadGraphLayoutCache;
import com.jgraph.pad.graph.JGraphpadGraphModel;
import com.jgraph.pad.graph.JGraphpadHeavyweightRenderer;
import com.jgraph.pad.graph.JGraphpadMarqueeHandler;
import com.jgraph.pad.graph.JGraphpadPortView;
import com.jgraph.pad.graph.JGraphpadRichTextValue;
import com.jgraph.pad.graph.JGraphpadTransferHandler;
import com.jgraph.pad.graph.JGraphpadVertexRenderer;
import com.jgraph.pad.graph.JGraphpadVertexView;
import com.jgraph.pad.tool.JGraphpadEdgeTool;
import com.jgraph.pad.tool.JGraphpadVertexTool;
import com.jgraph.pad.util.JGraphpadFocusManager;
import com.jgraph.pad.util.JGraphpadImageIcon;
import com.jgraph.pad.util.JGraphpadParallelEdgeRouter;
import com.jgraph.pad.util.JGraphpadParallelSplineRouter;
import com.jgraph.pad.util.JGraphpadShadowBorder;

/**
 * Class that constructs a new editor by creating a custom document model, kit
 * and factory. The document model defines the persistence delegates for xml
 * encoding, the kit and factory contain tools, actions and factory methods
 * respectively. The class also constructs all plugins and provides the methods
 * for creating the custom graph, graph cells and user objects to be used in
 * this editor and to exit the application. For this purpose it provides two
 * inner anonymous classes which override the respective methods, namely
 * {@link JGraphEditor#exit(int)} and
 * {@link JGraphEditorFactory#createGraph(GraphLayoutCache)} editor's exit
 * method and the factory's createGraph method.
 */
public class JGraphpad {

	/**
	 * Global static product identifier.
	 */
	public static final String VERSION_NUMBER = "6.0.4.1";

	/**
	 * Holds the application title for dialogs.
	 */
	public static String APPTITLE = "JGraphpad Pro";

	/**
	 * Global static product identifier.
	 */
	public static final String VERSION = APPTITLE + " (v" + VERSION_NUMBER
			+ ")";

	/**
	 * Specifies if libraries should reside inside documents. If this flag is true, then
	 * the navigator and libraries are inside the internal frames for documents. The
	 * libraries will be treated as part of their enclosing files. Default is false.
	 */
	public static boolean INNER_LIBRARIES = false;

	/**
	 * Defines the look and feel argument name.
	 */
	public static String ARG_SYSTEMLOOKANDFEEL = "S";

	/**
	 * Defines the look and feel argument name.
	 */
	public static String ARG_JGOODIESLOOKANDFEEL = "J";

	/**
	 * Defines the look and feel argument name.
	 */
	public static String ARG_VERSION = "V";

	/**
	 * Defines the path to the UI config file.
	 */
	public static String PATH_UICONFIG = "/com/jgraph/pad/resources/ui.xml";

	/**
	 * Defines the path to the splash image file.
	 */
	public static String PATH_SPLASHIMAGE = "/com/jgraph/pad/images/splash.jpg";

	/**
	 * Defines the path to the UI config file.
	 */
	public static String PATH_DEFAULTLIBRARY = "/com/jgraph/pad/resources/default.xml";

	/**
	 * Defines the path the the user settings file. This should also work with
	 * URLs (untested).
	 */
	public static String PATH_DEFAULTSETTINGS = "/com/jgraph/pad/resources/default.ini";

	/**
	 * Defines the path the the user settings file. This should also work with
	 * URLs (untested).
	 */
	public static String PATH_USERSETTINGS;

	/**
	 * Defines the filename for the settings file. Default is .jgraphpad.ini
	 */
	public static String NAME_SETTINGSFILE = ".jgraphpad.ini";

	/**
	 * Defines the name for the ui XML document in the editor settings.
	 */
	public static String NAME_UICONFIG = "ui";

	/**
	 * Defines the name for the user properties in the editor settings.
	 */
	public static String NAME_USERSETTINGS = "user";

	/**
	 * Defines the key used to identify the main window settings.
	 */
	public static String KEY_MAINWINDOW = "mainWindow";

	/**
	 * Defines the key used to identify the recent files settings.
	 */
	public static String KEY_RECENTFILES = "recentFiles";

	/**
	 * Defines the key used to identify the group prototype settings.
	 */
	public static String KEY_GROUPPROTOTYPE = "groupPrototype";

	/**
	 * Defines the key used to identify the vertex prototype settings.
	 */
	public static String KEY_VERTEXPROTOTYPE = "vertexPrototype";

	/**
	 * Defines the key used to identify the edge prototype settings.
	 */
	public static String KEY_EDGEPROTOTYPE = "edgePrototype";

	/**
	 * Defines the name for the selectTool.
	 */
	public static final String NAME_SELECTTOOL = "selectTool";

	/**
	 * Defines the name for the textTool.
	 */
	public static final String NAME_TEXTTOOL = "textTool";

	/**
	 * Defines the name for the vertexTool.
	 */
	public static final String NAME_VERTEXTOOL = JGraphpadVertexTool.NAME_VERTEXTOOL;

	/**
	 * Defines the name for the roundedTool.
	 */
	public static final String NAME_ROUNDEDTOOL = "roundedTool";

	/**
	 * Defines the name for the circleTool.
	 */
	public static final String NAME_CIRCLETOOL = "circleTool";

	/**
	 * Defines the name for the diamondTool.
	 */
	public static final String NAME_DIAMONDTOOL = "diamondTool";

	/**
	 * Defines the name for the triangleTool.
	 */
	public static final String NAME_TRIANGLETOOL = "triangleTool";

	/**
	 * Defines the name for the diamondTool.
	 */
	public static final String NAME_CYLINDERTOOL = "cylinderTool";

	/**
	 * Defines the name for the imageTool.
	 */
	public static final String NAME_IMAGETOOL = "imageTool";

	/**
	 * Defines the name for the imageTool.
	 */
	public static final String NAME_HEAVYTOOL = "heavyTool";

	/**
	 * Defines the name for the edgeTool.
	 */
	public static final String NAME_EDGETOOL = JGraphpadEdgeTool.NAME_EDGETOOL;

	/**
	 * Defines the name for the orthogonalEdgeTool.
	 */
	public static final String NAME_ORTHOGONALEDGETOOL = "orthogonalEdgeTool";

	/**
	 * Defines the name of the createShapeCombo factory method.
	 */
	public static final String METHOD_CREATESHAPECOMBO = "createShapeCombo";

	/**
	 * Defines the name of the createGradientCombo factory method.
	 */
	public static final String METHOD_CREATEGRADIENTCOMBO = "createGradientCombo";

	/**
	 * Defines the name of the createGradientCombo factory method.
	 */
	public static final String METHOD_CREATELINECOLORCOMBO = "createLineColorCombo";

	/**
	 * Adds resources bundles and prepares classes for xml encoding.
	 * 
	 * @see JGraphEditorResources#addBundles
	 * @see JGraphEditorModel#makeCellViewFieldsTransient(Class)
	 * @see JGraphEditorModel#makeTransient
	 */
	static {

		// Adds resource bundles
		JGraphEditorResources.addBundles(new String[] {
				"com.jgraph.pad.resources.actions",
				"com.jgraph.pad.resources.menus",
				"com.jgraph.pad.resources.strings",
				"com.jgraph.pad.resources.tools" });

		// Prepares cell views for xml encoding (recommended for all cell views)
		JGraphEditorModel
				.makeCellViewFieldsTransient(JGraphpadVertexView.class);
		JGraphEditorModel.makeCellViewFieldsTransient(JGraphpadPortView.class);
		JGraphEditorModel.makeCellViewFieldsTransient(JGraphpadEdgeView.class);

		// Prepares class bean infos for xml encoding
		JGraphEditorModel.makeTransient(DefaultPort.class, "edges");
		JGraphEditorModel.makeTransient(DefaultEdge.class, "source");
		JGraphEditorModel.makeTransient(DefaultEdge.class, "target");
		JGraphEditorModel.makeTransient(GraphLayoutCache.class, "visibleSet");
		JGraphEditorModel.makeTransient(JGraphpadFile.class, "modified");
		JGraphEditorModel.makeTransient(JGraphpadFile.class, "userObject");
		JGraphEditorModel.makeTransient(JGraphpadFile.class, "filename");
		JGraphEditorModel.makeTransient(JGraphpadFile.class, "new");
		JGraphEditorModel.makeTransient(JGraphpadLibrary.class, "modified");
		JGraphEditorModel.makeTransient(JGraphpadLibrary.class, "userObject");
		JGraphEditorModel.makeTransient(JGraphpadLibrary.class, "filename");
		JGraphEditorModel.makeTransient(JGraphpadLibrary.class, "new");
		JGraphEditorModel.makeTransient(ConnectionSet.class, "edges");

		// Installs an additional border in the border combo box
		JGraphEditorComboBox.defaultBorders = new Border[] {
				BorderFactory.createRaisedBevelBorder(),
				BorderFactory.createLoweredBevelBorder(),
				BorderFactory.createEtchedBorder(),
				BorderFactory.createLineBorder(Color.black),
				JGraphpadShadowBorder.sharedInstance };

		// Installs basic authentication dialog
		try {
			Authenticator.setDefault(new JGraphpadAuthenticator());
		} catch (Exception e) {
			// ignore
		}
	}

	/**
	 * The class names for the defaut plugins.
	 */
	public String[] defaultPlugins = new String[] {
			"com.jgraph.l2fplugin.JGraphpadL2FPlugin",
			"com.jgraph.layoutplugin.JGraphpadLayoutPlugin",
			"com.jgraph.svgplugin.JGraphpadSVGPlugin",
			"com.jgraph.bshplugin.JGraphpadBshPlugin",
			"com.jgraph.jgxplugin.JGraphpadJGXPlugin",
			"com.jgraph.codecplugin.JGraphpadCodecPlugin",
			"com.jgraph.epsplugin.JGraphpadEPSPlugin",
			"com.jgraph.pdfplugin.JGraphpadPDFPlugin",
			"com.jgraph.browserplugin.JGraphpadBrowserPlugin",
			"com.jgraph.twikiplugin.JGraphpadTWikiPlugin" };

	/**
	 * Defines the default port locations.
	 */
	public Point2D[] defaultPortLocations = new Point2D[] { null };

	/**
	 * Defines the default vertex bounds.
	 */
	public Rectangle2D defaultBounds = new Rectangle2D.Double(0, 0, 20, 20);

	/**
	 * Defines the default border color.
	 */
	public Color defaultBorderColor = Color.BLACK;

	/**
	 * Defines the default edge font.
	 */
	public Font defaultEdgeFont = GraphConstants.DEFAULTFONT.deriveFont(10);

	/**
	 * Defines the default end and begin decorations for edges.
	 */
	public int defaultEndDecoration = GraphConstants.ARROW_TECHNICAL,
			defaultBeginDecoration = GraphConstants.ARROW_NONE;

	/**
	 * Constructs JGraphpad as an applet.
	 */
	public JGraphpad() {
		// Some general variable assignments that require control logic.
		// This one is likely to throw a security exception in an applet.
		// In applications it uses the user's homedir to save settings.
		try {
			PATH_USERSETTINGS = System.getProperty("user.home")
					+ File.separator + NAME_SETTINGSFILE;
		} catch (SecurityException e) {
			// ignore
		}
	}

	/**
	 * Hook for subclassers to implement the application exit method. This
	 * implementation calls System.exit with the specified code.
	 */
	protected void exit(int code) {
		System.exit(code);
	}

	/**
	 * Constructs a new editor application and returns its main window. Shows a
	 * splash window while the application is being constructed and returns the
	 * main window in visible state. <br>
	 * The parameters are obtained by parsing the arguments passed to the main
	 * method as follows. For all arguments -A B the value B is stored in the
	 * arguments under the A, for other arguments, eg. C D E the values C, D and
	 * E and passed in as elements of the list.
	 * 
	 * @param files
	 *            The list of filenames passed to the Java command.
	 * @param args
	 *            The arguments to use for constructing the editor settings.
	 * @return Returns a new application main window.
	 */
	public Window createApplication(List files, Map args)
			throws ParserConfigurationException, SAXException, IOException {

		// Checks the lookandfeel argument
		if (args != null && args.remove(ARG_SYSTEMLOOKANDFEEL) != null) {
			try {
				UIManager.setLookAndFeel(UIManager
						.getSystemLookAndFeelClassName());
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else if (args != null && args.remove(ARG_JGOODIESLOOKANDFEEL) != null) {
			try {
				UIManager
						.setLookAndFeel("com.jgoodies.looks.plastic.Plastic3DLookAndFeel");
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		// Constructs and displays a splash window. Also checks to
		// see if it's the user's first session and opens an
		// initial new document if it is.
		Window splashWindow = createSplashWindow();
		splashWindow.setVisible(true);

		boolean firstTime = (PATH_USERSETTINGS != null) ? !(new File(
				PATH_USERSETTINGS).exists()) : false;

		// Constructs the editor
		final JGraphEditor editor = createEditor(args);

		// Adds the plugins settings, actions, tools and methods
		// and initializes the plugins.
		createPlugins(editor);

		// Constructs the main window and keep a reference (restore bounds)
		// for storage of the bounds when the program terminates (hook).
		Window mainWindow = createMainWindow(editor,
				JGraphpadPane.FactoryMethod.NAME);

		// ** Application construction complete **

		// Puts the main window into the settings map and
		// restores its bounds from the user settings.
		editor.getSettings().putObject(KEY_MAINWINDOW, mainWindow);
		editor.getSettings().restoreWindow(NAME_USERSETTINGS, KEY_MAINWINDOW);

		// Installs a shutdown hook to store the bounds if the
		// main window in the properties (in memory) for later
		// storing to disk.
		editor.getSettings().addShutdownHook(
				new JGraphEditorSettings.ShutdownHook() {

					// Takes the window bounds and stores the into the in-core
					// user configuration, which is later saved to disk.
					public void shutdown() {
						editor.getSettings().storeWindow(NAME_USERSETTINGS,
								KEY_MAINWINDOW);
					}
				});

		// This is how we link two factory methods, in this case the
		// createFrame and the createWindowMenu, which needs to be updated
		// when internal frames are added to or removed from the desktop
		// pane: Both factoryMethods store a reference to the desktop pane
		// and the menu and the code below fetches the references and wires
		// them up properly, eg. adds the menu as a listener to the desktop
		// pane. This needs to be done after createMainWindow was called.
		JDesktopPane desktopPane = (JDesktopPane) editor.getSettings()
				.getObject(JGraphpadPane.KEY_DESKTOPPANE);
		JGraphpadWindowMenu windowMenu = (JGraphpadWindowMenu) editor
				.getSettings().getObject(JGraphpadWindowMenu.KEY_WINDOWMENU);
		// Wires the two references up by setting the desktop in the menu
		if (desktopPane != null && windowMenu != null) {
			windowMenu.setDesktopPane(desktopPane);
		}

		// Opens the built-in library and remove the filename
		// as it has been loaded from within the jar file to
		// where it can not be saved back. Mark it new afterwards.
		try {
			JGraphpadLibrary library = (JGraphpadLibrary) editor.getModel()
					.addFile(PATH_DEFAULTLIBRARY);
			if (library != null) {
				editor.getModel().setFilename(library,
						JGraphEditorResources.getString("DefaultLibrary"));
				library.setNew(true);
			}
		} catch (Exception e) {
			// ignore
		}

		// Disposes the splash window
		splashWindow.dispose();

		// Restores the divider locations
		// We must return a visible main window to be able to
		// restore the splitpane divider locations, which require
		// the split panes to be visible (Swing Bug).
		mainWindow.setVisible(true);
		editor.getSettings().restoreSplitPane(NAME_USERSETTINGS,
				JGraphpadPane.KEY_NAVIGATORSPLIT);
		editor.getSettings().restoreSplitPane(NAME_USERSETTINGS,
				JGraphpadPane.KEY_LEFTSPLIT);
		editor.getSettings().restoreSplitPane(NAME_USERSETTINGS,
				JGraphpadPane.KEY_RIGHTSPLIT);

		// Opens the specified documents

		// Removes file actions from the kit for the demo version.
		if (files != null && !files.isEmpty()) {
			JGraphEditorModel model = editor.getModel();
			Iterator it = files.iterator();
			while (it.hasNext()) {
				String filename = String.valueOf(it.next());
				try {
					editor.getModel().addFile(filename);
				} catch (Exception e) {
					JGraphpadFile file = new JGraphpadFile(filename);
					JGraphpadDiagram newDiagram = new JGraphpadDiagram(
							JGraphEditorResources.getString("Diagram") + "1");
					model.addRoot(file);
					model.addChild(newDiagram, file);
				}
			}
		}

		// Opens a document if it's the users first session
		else if (firstTime)
			editor.getKit().getAction(JGraphpadFileAction.NAME_NEWDOCUMENT)
					.actionPerformed(null);

		return mainWindow;
	}

	/**
	 * Helper method that invokes the specified factory method and configures
	 * the main window by setting its bounds and installing window listeners.
	 * This implementation invokes the exit action in the editor kit if the
	 * window is closed by the user.
	 * 
	 * @param editor
	 *            The editor to create the main window for.
	 * @param factoryMethod
	 *            The name of the factory method to invoke.
	 * @return Returns a new main window for the specified editor.
	 * 
	 * @see JGraphEditorFactory#executeMethod(String, Node)
	 * @see JGraphEditorKit#getAction(String)
	 * @see #center(Window)
	 */
	protected Window createMainWindow(final JGraphEditor editor,
			String factoryMethod) {

		// Invokes the specified factory method and passes
		// along the ui configuration.
		Window wnd = (Window) editor.getFactory().executeMethod(
				factoryMethod,
				editor.getSettings().getDocument(NAME_UICONFIG)
						.getDocumentElement());

		// Positions the window, sets the default size and
		// installs a window listener.
		wnd.setSize(800, 640);
		center(wnd);

		// Makes sure the window is only closed by means of the exit
		// operation, which shuts down the VM.
		if (wnd instanceof JFrame)
			((JFrame) wnd).setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		wnd.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				editor.getKit().getAction("exit").actionPerformed(null);
			}
		});
		return wnd;
	}

	/**
	 * Constructs a splash window to be displayed during the construction of the
	 * application.
	 * 
	 * @return Returns a reference to the displaying splash window.
	 */
	protected Window createSplashWindow() {
		JWindow splashWindow = new JWindow();

		// Loads the splash image from the resources into a JLabel
		// using the instance variable for the image path. Then adds
		// a raised bevel border an adds the label to the window.
		JLabel image = new JLabel(JGraphEditorResources
				.getImage(PATH_SPLASHIMAGE));
		image.setBorder(BorderFactory.createRaisedBevelBorder());
		splashWindow.getContentPane().add(image, BorderLayout.CENTER);
		JLabel label = new JLabel("v" + VERSION_NUMBER);
		image.setLayout(null);
		image.add(label);
		label.setFont(label.getFont().deriveFont(Font.BOLD));
		label.setForeground(Color.GRAY);
		label.setBounds(202, 118, 200, 30);

		// Sets size and position and displays the window
		splashWindow.pack();
		center(splashWindow);

		return splashWindow;
	}

	/**
	 * Constructs a new {@link JGraphEditor} using the specified settings and
	 * document model model and calls
	 * {@link #configureEditor(JGraphEditor, Map)} on the new instance. The
	 * returned instance has an overridden exit hook which disposes the main
	 * window and terminates the VM in the proper way for how the application
	 * was started.
	 * 
	 * @param args
	 *            The arguments passed to createApplication.
	 * 
	 * @return Returns a new configured editor.
	 * @throws IOException
	 * @throws SAXException
	 * @throws ParserConfigurationException
	 */
	protected JGraphEditor createEditor(Map args)
			throws ParserConfigurationException, SAXException, IOException {

		// Overrides the global application exit hook to dispose the
		// window. Note: Calls System.exit if thisis not an applet. The exit
		// method is called after all user interface interaction has
		// terminated from within the exit method implemented in
		// JGraphpadFileExit.
		JGraphEditor editor = new JGraphEditor() {

			/**
			 * Fetches the main window from the settings and disposes it. This
			 * will terminate the VM if there are no more open windows left.
			 */
			public void exit(int code) {
				super.exit(code);
				Window wnd = (Window) getSettings().getObject(KEY_MAINWINDOW);
				if (wnd != null) {
					wnd.setVisible(false);
					wnd.dispose();
				}
				JGraphpad.this.exit(code);
			}
		};
		// Configures the editor kit, factory and constructs plugins
		configureEditor(editor, args);

		return editor;
	}

	/**
	 * Hook for subclassers to configure new editors. This implementation
	 * configures the editor kit and -factory and invokes
	 * {@link #createPlugins(JGraphEditor)}for the editor.
	 * 
	 * @param editor
	 *            The editor to be configured.
	 * @param args
	 *            The arguments passed to createApplication.
	 * @throws IOException
	 * @throws SAXException
	 * @throws ParserConfigurationException
	 * 
	 * @see #createKit(JGraphEditor)
	 * @see #createFactory(JGraphEditor)
	 */
	protected void configureEditor(JGraphEditor editor, Map args)
			throws ParserConfigurationException, SAXException, IOException {
		editor.setSettings(createSettings(args));
		editor.setModel(createModel());
		editor.setKit(createKit(editor));
		editor.setFactory(createFactory(editor));
	}

	/**
	 * Constructs the editor settings. This implementation constructs a new
	 * instance of JGraphEditorSettings using args as the initial object map and
	 * passes it to the {@link #configureSettings(JGraphEditorSettings)}method.
	 * 
	 * @param args
	 *            The arguments passed to the command line.
	 * @return Returns a configured editor settings object.
	 */
	protected JGraphEditorSettings createSettings(Map args)
			throws ParserConfigurationException, SAXException, IOException {
		JGraphEditorSettings settings = new JGraphEditorSettings(args);
		configureSettings(settings);
		return settings;
	}

	/**
	 * Hook for subclassers to configure new editor settings. This
	 * implementation adds the ui configuration ({@link #PATH_UICONFIG}) to
	 * the
	 * 
	 * {@link #NAME_UICONFIG}and user settings ({@link #PATH_USERSETTINGS})
	 * to the {@link #NAME_USERSETTINGS}.
	 * 
	 * @param settings
	 *            The editor settings to be configured.
	 * @throws IOException
	 * @throws SAXException
	 * @throws ParserConfigurationException
	 * @throws FileNotFoundException
	 * @throws MalformedURLException
	 * 
	 * @see JGraphEditorSettings#parse(InputStream)
	 * @see JGraphEditorSettings#add(String, InputStream)
	 */
	protected void configureSettings(JGraphEditorSettings settings)
			throws MalformedURLException, FileNotFoundException,
			ParserConfigurationException, SAXException, IOException {

		// Parses the ui configuration file. We have to use the resources'
		// getInputStream because we have no model yet. This means the
		// user configuration must not be compressed, which is acceptable.
		settings.add(NAME_UICONFIG, JGraphEditorSettings
				.parse(JGraphEditorResources.getInputStream(PATH_UICONFIG)));

		// Reads the user settings if they exist. Note: User settings do
		// never exist on the first run of this program. They are created
		// or updated when the program exits.
		if (PATH_USERSETTINGS != null && new File(PATH_USERSETTINGS).canRead())
			settings.add(NAME_USERSETTINGS, JGraphEditorResources
					.getInputStream(PATH_USERSETTINGS));
		else
			settings.add(NAME_USERSETTINGS, JGraphEditorResources
					.getInputStream(PATH_DEFAULTSETTINGS));

		// Adds a shutdown hook to save the user settings when
		// the program terminates. This shutdown hook is added
		// at creation time to make sure it is the last hook
		// invoked in the shutdown method of the settings.
		final Properties userSettings = settings
				.getProperties(JGraphpad.NAME_USERSETTINGS);
		settings.addShutdownHook(new JGraphEditorSettings.ShutdownHook() {
			public void shutdown() {
				if (userSettings != null && PATH_USERSETTINGS != null) {
					try {
						OutputStream out = JGraphEditorResources
								.getOutputStream(JGraphpad.PATH_USERSETTINGS);
						userSettings.store(out, "");
						out.flush();
						out.close();

						// Allows PATH_USERSETTINGS to be a URL
						if (JGraphEditor.isURL(JGraphpad.PATH_USERSETTINGS)) {
							URL url = new URL(JGraphpad.PATH_USERSETTINGS);
							JGraphpadFileAction.postPlain(url, url.getFile(),
									out);
						}
					} catch (Exception e) {
						// ignore
					}
				}
			}
		});

		// Adds prototypes for groups, vertices and edges to the
		// settings for later use by plugins and other interested
		// parties.
		settings.putObject(KEY_GROUPPROTOTYPE, createGroup());
		settings.putObject(KEY_VERTEXPROTOTYPE, createVertex());
		settings.putObject(KEY_EDGEPROTOTYPE, createEdge());
	}

	/**
	 * Constructs a document model for new editors. This implementation
	 * constructs a new instance of {@link JGraphEditorModel}and passes it to
	 * the {@link #configureModel(JGraphEditorModel)}method.
	 * 
	 * @return Returns a configured document model for an editor.
	 */
	protected JGraphEditorModel createModel() {
		JGraphEditorModel model = new JGraphEditorModel();
		configureModel(model);
		return model;
	}

	/**
	 * Hook for subclassers to configure new document models. This
	 * implementation adds persistence delegates for the following classes:
	 * {@link JGraphpadDiagram},{@link JGraphpadGraphModel},
	 * com.jgraph.graph.ConnectionSet, {@link JGraphpadGraphLayoutCache},
	 * com.jgraph.graph.DefaultGraphCell, com.jgraph.graph.DefaultEdge,
	 * com.jgraph.graph.DefaultPort,{@link JGraphpadBusinessObject},
	 * {@link JGraphpadRichTextValue}and {@link JGraphpadShadowBorder}.<br>
	 * <b>JGraphpadDiagram </b> <br>
	 * Constructs a persistence delegate for the diagram class. This uses the
	 * fact that all information is stored in the model, not the layout cache by
	 * ignoring the layout cache and accessing the model stored in the layout
	 * cache directly through the model bean property. Note: To allow this kind
	 * of encoding the diagram class offers a special constructor that takes a
	 * model and constructs a new graph layout cache for it. <br>
	 * <b>JGraphpadGraphModel </b> <br>
	 * To encode graph models we do not want the files to contain redundant
	 * connectivity information in the ports.edges and edges.source and target
	 * fields, so we add a method to the graph model that returns a connection
	 * set which describes the connections without redundancy. (Note: In the
	 * static initializer of this class we make sure that the edges, source and
	 * target of the respective classes or not encoded.) <br>
	 * <b>ConnectionSet </b> <br>
	 * The complete information of a connection set is stored in the actual
	 * connections, thus we only store the connections and use special
	 * constructor to restore the state of the complete object when de- coding.
	 * (Note: For connection sets this will update the edges field.) <br>
	 * <b>JGraphpadGraphLayoutCache </b> <br>
	 * The graph layout cache is encoded by encoding the various member fields,
	 * using a special constructor to restore the state of the layout cache upon
	 * decoding. Note that this is currently not used. <br>
	 * <b>DefaultGraphCell, DefaultEdge, DefaultPort </b> <br>
	 * Makes sure the cells are only encoded along with their user objects, the
	 * attributes, connections and tree-structure is stored in other objects and
	 * does not need to be encoded here. <br>
	 * <b>JGraphpadBusinessObject, JGraphpadRichTextData </b> <br>
	 * Allows to encode custom business objects used in JGraphpad. Since this
	 * object implements the bean interface we do only require a default
	 * persistence delegates with no special constructor calls to decode the
	 * object. Same holds for the rich text data object, which is a special
	 * value that can hold text formatting information. <br>
	 * <b>JGraphShadowBorder </b> <br>
	 * Since the shadow border is a singleton we must tell the decoder which
	 * method to use in order to find the shared instance of the class.
	 * 
	 * @param model
	 *            The document model to be configured.
	 */
	protected void configureModel(JGraphEditorModel model) {
		model.addPersistenceDelegate(JGraphpadDiagram.class,
				new DefaultPersistenceDelegate(new String[] { "name",
						"graphLayoutCache", "properties" }));
		model.addPersistenceDelegate(JGraphpadLibrary.class,
				new DefaultPersistenceDelegate(new String[] { "filename",
						"graphLayoutCache" }));
		model.addPersistenceDelegate(JGraphpadGraphModel.class,
				new DefaultPersistenceDelegate(new String[] { "roots",
						"attributes", "connectionSet" }));
		model.addPersistenceDelegate(ConnectionSet.class,
				new DefaultPersistenceDelegate(new String[] { "connections" }));

		model.addPersistenceDelegate(JGraphpadGraphLayoutCache.class,
				new DefaultPersistenceDelegate(new String[] { "model",
						"visibleSet", "partial" }));

		model.addPersistenceDelegate(DefaultGraphCell.class,
				new DefaultPersistenceDelegate(new String[] { "userObject" }));
		model.addPersistenceDelegate(DefaultEdge.class,
				new DefaultPersistenceDelegate(new String[] { "userObject" }));
		model.addPersistenceDelegate(DefaultPort.class,
				new DefaultPersistenceDelegate(new String[] { "userObject" }));

		model.addPersistenceDelegate(JGraphpadBusinessObject.class,
				new DefaultPersistenceDelegate());
		model.addPersistenceDelegate(JGraphpadRichTextValue.class,
				new DefaultPersistenceDelegate());

		// Shadow Border instance
		model.addPersistenceDelegate(JGraphpadShadowBorder.class,
				new PersistenceDelegate() {
					protected Expression instantiate(Object oldInstance,
							Encoder out) {
						return new Expression(oldInstance,
								JGraphpadShadowBorder.class,
								"getSharedInstance", null);
					}
				});

		// Two shared routing instances
		model.addPersistenceDelegate(JGraphpadParallelEdgeRouter.class,
				new PersistenceDelegate() {
					protected Expression instantiate(Object oldInstance,
							Encoder out) {
						return new Expression(oldInstance,
								JGraphpadGraphConstants.class,
								"getParallelEdgeRouting", null);
					}
				});
		model.addPersistenceDelegate(JGraphpadParallelSplineRouter.class,
				new PersistenceDelegate() {
					protected Expression instantiate(Object oldInstance,
							Encoder out) {
						return new Expression(oldInstance,
								JGraphpadGraphConstants.class,
								"getParallelSplineRouting", null);
					}
				});
	}

	/**
	 * Constructs a default kit for new editors. This implementation constructs
	 * a new instance of {@link JGraphEditorKit}and passes it to
	 * {@link #configureKit(JGraphEditor, JGraphEditorKit)}.
	 * 
	 * @param editor
	 *            The editor for which to create an editor kit.
	 * @return Returns a configured editor kit for the specified editor.
	 */
	protected JGraphEditorKit createKit(JGraphEditor editor) {
		JGraphEditorKit kit = new JGraphEditorKit();
		configureKit(editor, kit);
		return kit;
	}

	/**
	 * Boilerplate method for configuring new editor kits. This implementation
	 * calls {@link #addActions(JGraphEditor, JGraphEditorKit)},
	 * {@link #addTools(JGraphEditor, JGraphEditorKit)}and registers the kit
	 * with the listeners required to update the state. This method is called
	 * from {@link #createKit(JGraphEditor)}.
	 * 
	 * @param editor
	 *            The editor for which to configure the editor kit.
	 * @param kit
	 *            The new editor kit to be configured.
	 */
	protected void configureKit(JGraphEditor editor, final JGraphEditorKit kit) {
		addActions(editor, kit);
		addTools(editor, kit);

		// A selection listener for library pane's backing graph that
		// needs to be listened to for updating the actions in the kit.
		final GraphSelectionListener selectionListener = new GraphSelectionListener() {

			/**
			 * Updates the actions in the factory kit when the selection of a
			 * library pane (ie of its backing graph) changes.
			 */
			public void valueChanged(GraphSelectionEvent e) {
				kit.update();
			}

		};

		KeyboardFocusManager focusManager = KeyboardFocusManager
				.getCurrentKeyboardFocusManager();
		focusManager.addPropertyChangeListener(new PropertyChangeListener() {

			/*
			 * (non-Javadoc)
			 */
			public void propertyChange(PropertyChangeEvent e) {
				if (e.getPropertyName().equals("permanentFocusOwner"))
					kit.update();

				// Keeps the selection listener installed with the library
				// pane's backing graph. Travels along with the focus.
				if (e.getOldValue() instanceof JGraphpadLibraryPane)
					((JGraphpadLibraryPane) e.getOldValue()).getBackingGraph()
							.getSelectionModel().removeGraphSelectionListener(
									selectionListener);
				if (e.getNewValue() instanceof JGraphpadLibraryPane)
					((JGraphpadLibraryPane) e.getNewValue()).getBackingGraph()
							.getSelectionModel().addGraphSelectionListener(
									selectionListener);
			}
		});

		JGraphpadFocusManager.getCurrentGraphFocusManager()
				.addPropertyChangeListener(new PropertyChangeListener() {

					/*
					 * (non-Javadoc)
					 */
					public void propertyChange(PropertyChangeEvent e) {
						kit.update();
					}
				});
		kit.update();
	}

	//
	// Actions
	//

	/**
	 * Adds the action bundles for {@link JGraphpadEditAction},
	 * {@link JGraphpadFileAction},{@link JGraphpadViewAction},
	 * {@link JGraphpadFormatAction}and {@link JGraphpadCellAction}. Uses the
	 * createVertex and createEdge method to construct prototypes for
	 * {@link JGraphpadCellAction#NAME_GROUP}and
	 * {@link JGraphpadCellAction#NAME_CONNECT}. This method is called from
	 * configureEditorKit.
	 * 
	 * @param editor
	 *            The editor for which to create the actions.
	 * @param kit
	 *            The editor kit to add the actions to.
	 * 
	 * @see #configureKit(JGraphEditor, JGraphEditorKit)
	 * @see JGraphEditorKit#addBundle(JGraphEditorAction.Bundle)
	 * @see #createVertex()
	 * @see #createEdge()
	 */
	protected void addActions(JGraphEditor editor, JGraphEditorKit kit) {

		// Adds action bundles for each of the menus. To simplify
		// searching for specific actions all actions reside in
		// the classes corresponding to the menu they are in.
		kit.addBundle(new JGraphpadEditAction.AllActions());
		kit.addBundle(new JGraphpadViewAction.AllActions());
		kit.addBundle(new JGraphpadFormatAction.AllActions());
		kit.addBundle(new JGraphpadHelpAction.AllActions());

		// Adds actions that require prototype cells
		kit.addBundle(new JGraphpadFileAction.AllActions(editor));
		kit.addBundle(new JGraphpadCellAction.AllActions(editor));
	}

	//
	// Editor Tools
	//

	/**
	 * Adds the following tools to <code>kit</code>:{@link #NAME_SELECTTOOL},
	 * {@link #NAME_VERTEXTOOL},{@link #NAME_ROUNDEDTOOL},
	 * {@link #NAME_CIRCLETOOL},{@link #NAME_DIAMONDTOOL},{@link #NAME_TRIANGLETOOL},
	 * {@link #NAME_IMAGETOOL},{@link #NAME_HEAVYTOOL},{@link #NAME_EDGETOOL}and
	 * {@link #NAME_ORTHOGONALEDGETOOL}. This method is called from
	 * configureEditorKit.
	 * 
	 * @param editor
	 *            The editor for which to create the tools.
	 * @param kit
	 *            The editor kit to add the tools to.
	 * 
	 * @see #configureKit(JGraphEditor, JGraphEditorKit)
	 * @see JGraphEditorKit#addTool(JGraphEditorTool)
	 * @see #createVertexTool(String, Object, int, ImageIcon)
	 * @see #createEdgeTool(String, String, Edge.Routing)
	 */
	protected void addTools(JGraphEditor editor, JGraphEditorKit kit) {
		kit.addTool(new JGraphEditorTool(NAME_SELECTTOOL, false));
		JGraphpadVertexTool tool = createVertexTool(NAME_TEXTTOOL,
				new JGraphpadRichTextValue(""), -1, null, true);
		tool.setPreviewEnabled(false);
		kit.addTool(tool);
		kit.addTool(createVertexTool(NAME_VERTEXTOOL,
				new JGraphpadRichTextValue(""),
				JGraphpadVertexRenderer.SHAPE_RECTANGLE, null));
		JComponent c = new JGraphpadHeavyweightRenderer();
		kit.addTool(createVertexTool(NAME_HEAVYTOOL, c,
				JGraphpadVertexRenderer.SHAPE_RECTANGLE, null));
		kit.addTool(createVertexTool(NAME_ROUNDEDTOOL,
				new JGraphpadRichTextValue(""),
				JGraphpadVertexRenderer.SHAPE_ROUNDED, null));
		kit.addTool(createVertexTool(NAME_CIRCLETOOL,
				new JGraphpadRichTextValue(""),
				JGraphpadVertexRenderer.SHAPE_CIRCLE, null));
		kit.addTool(createVertexTool(NAME_DIAMONDTOOL,
				new JGraphpadRichTextValue(""),
				JGraphpadVertexRenderer.SHAPE_DIAMOND, null));
		kit.addTool(createVertexTool(NAME_TRIANGLETOOL,
				new JGraphpadRichTextValue(""),
				JGraphpadVertexRenderer.SHAPE_TRIANGLE, null));
		kit.addTool(createVertexTool(NAME_CYLINDERTOOL,
				new JGraphpadRichTextValue(""),
				JGraphpadVertexRenderer.SHAPE_CYLINDER, null));
		// Uses default image resource from classpath
		JGraphpadImageIcon icon = new JGraphpadImageIcon(
				"/com/jgraph/pad/images/noimage.gif");
		kit.addTool(createVertexTool(NAME_IMAGETOOL, "",
				JGraphpadVertexRenderer.SHAPE_RECTANGLE, icon));
		kit.addTool(createEdgeTool(NAME_EDGETOOL, new JGraphpadRichTextValue(""), null));
		kit.addTool(createEdgeTool(NAME_ORTHOGONALEDGETOOL, new JGraphpadRichTextValue(""),
				GraphConstants.ROUTING_SIMPLE));
	}

	/**
	 * Invokes
	 * {@link #createVertexTool(String, Object, int, ImageIcon, boolean)} with
	 * post editing set to false.
	 */
	protected JGraphpadVertexTool createVertexTool(String name,
			Object defaultValue, int shape, ImageIcon icon) {
		return createVertexTool(name, defaultValue, shape, icon, false);
	}

	/**
	 * Helper method to create and return a new vertex tool. This uses the
	 * createVertexUserObject or creates a new {@link JGraphpadRichTextValue}
	 * object based on <code>isRichText</code> as the user object, which it
	 * passes to createVertex to create the graph cell. The method sets the
	 * shape on the created graph cell, and returns a new JGraphpadVertexTool
	 * with the specified name.
	 * 
	 * @param name
	 *            The name of the tool to be created.
	 * @param defaultValue
	 *            The defaultValue for the vertices that this tool creates.
	 * @param shape
	 *            The shape for the vertices that this tool creates. Use -1 for
	 *            no border.
	 * @param icon
	 *            The icon for the vertices that this tool creates.
	 * @param postEdit
	 *            If in-place editing should be triggered after inserting the
	 *            cell.
	 * @return Returns a new vertex tool.
	 * 
	 * @see JGraphpadRichTextValue
	 * @see #createVertexUserObject(Object)
	 * @see #createVertex(Object)
	 * @see JGraphpadGraphConstants#setVertexShape(Map, int)
	 * @see JGraphpadVertexTool
	 */
	protected JGraphpadVertexTool createVertexTool(String name,
			Object defaultValue, int shape, ImageIcon icon, boolean postEdit) {
		Object userObject = createVertexUserObject(defaultValue);
		GraphCell vertex = createVertex(userObject);
		if (shape >= 0) {
			JGraphpadGraphConstants.setVertexShape(vertex.getAttributes(),
					shape);
		} else {
			vertex.getAttributes().remove(GraphConstants.BORDERCOLOR);
			vertex.getAttributes().remove(GraphConstants.BORDER);
		}
		if (icon != null) {
			GraphConstants.setIcon(vertex.getAttributes(), icon);
		}
		if (postEdit) {
			return new JGraphpadVertexTool(name, vertex) {
				protected void execute(JGraph graph, Object cell) {
					super.execute(graph, cell);
					graph.startEditingAtCell(cell);
				}
			};
		} else {
			return new JGraphpadVertexTool(name, vertex);
		}
	}

	/**
	 * Helper method to create and return a new edge tool. This passes the
	 * return value of createEdgeUserObject to createEdge to create the graph
	 * cell. The method sets the routing on the created graph cell if it is not
	 * <code>null</code>, and returns a new JGraphpadEdgeTool with the
	 * specified name.
	 * 
	 * @param name
	 *            The name of the tool to be created.
	 * @param defaultValue
	 *            The defaultValue for the edges that this tool creates.
	 * @param routing
	 *            The routing for the edges that this tool creates.
	 * @return Returns a new edge tool.
	 * 
	 * @see #createEdgeUserObject(Object)
	 * @see #createEdge(Object)
	 * @see GraphConstants#setRouting(Map, Edge.Routing)
	 * @see JGraphpadEdgeTool
	 */
	protected JGraphpadEdgeTool createEdgeTool(String name,
			Object defaultValue, Edge.Routing routing) {
		DefaultEdge edge = createEdge(createEdgeUserObject(defaultValue));
		if (routing != null)
			GraphConstants.setRouting(edge.getAttributes(), routing);
		return new JGraphpadEdgeTool(name, edge);
	}

	/**
	 * Constructs a default factory for new editors. This implementation
	 * constructs a new instance of JGraphEditorFactory and overrides
	 * {@link JGraphEditorFactory#createGraph(GraphLayoutCache)} to call
	 * {@link #createGraph(JGraphEditor, GraphLayoutCache)} and configures the
	 * factory using
	 * {@link #configureFactory(JGraphEditor, JGraphEditorFactory)}.
	 * 
	 * @param editor
	 *            The editor for which to create an editor factory.
	 * @return Returns a configured editor factory for the specified editor.
	 */
	protected JGraphEditorFactory createFactory(final JGraphEditor editor) {
		JGraphEditorFactory factory = new JGraphEditorFactory(editor.getKit()) {
			public JGraph createGraph(GraphLayoutCache graphLayoutCache) {
				return JGraphpad.this.createGraph(editor, graphLayoutCache);
			}
		};
		configureFactory(editor, factory);
		return factory;
	}

	/**
	 * Hook for subclassers to configure new editor factories. This
	 * implementation adds various following factory methods.
	 * 
	 * @param editor
	 *            The editor to create the factory methods for.
	 * @param factory
	 *            The factory to be configured.
	 */
	protected void configureFactory(JGraphEditor editor,
			JGraphEditorFactory factory) {

		// Adds the factory methods from the Editor Framework
		factory.addMethod(new JGraphEditorNavigator.FactoryMethod(editor));
		factory.addMethod(new JGraphEditorComboBox.BorderComboFactoryMethod());
		factory
				.addMethod(new JGraphEditorComboBox.LineDecorationComboFactoryMethod());
		factory
				.addMethod(new JGraphEditorComboBox.LineWidthComboFactoryMethod());
		factory
				.addMethod(new JGraphEditorComboBox.DashPatternComboFactoryMethod());

		// Adds custom factory methods
		factory.addMethod(new JGraphpadPane.FactoryMethod(editor));
		factory.addMethod(new JGraphpadPane.LeftTabFactoryMethod(editor));
		factory.addMethod(new JGraphpadPane.BottomTabFactoryMethod(editor));
		factory
				.addMethod(new JGraphpadComboBox.VertexShapeComboFactoryMethod());
		factory.addMethod(new JGraphpadComboBox.ColorComboFactoryMethod());
		factory.addMethod(new JGraphpadComboBox.ColorComboFactoryMethod(
				METHOD_CREATELINECOLORCOMBO, JGraphpadComboBox.TYPE_LINECOLOR));
		factory.addMethod(new JGraphpadComboBox.ColorComboFactoryMethod(
				METHOD_CREATEGRADIENTCOMBO, JGraphpadComboBox.TYPE_GRADIENT));
		factory.addMethod(new JGraphpadLibraryPane.FactoryMethod(editor));
		factory.addMethod(new JGraphpadWindowMenu.FactoryMethod(editor));
		factory.addMethod(new JGraphpadOpenRecentMenu(editor));
		factory.addMethod(new JGraphpadConsole.FactoryMethod());
		factory.addMethod(new JGraphpadStatusBar.FactoryMethod(editor));
	}

	/**
	 * Hook for subclassers to create plugins for a new editor. This
	 * implementation creates the {@link #defaultPlugins}. This method is
	 * called from {@link #configureEditor(JGraphEditor, Map)}.
	 * 
	 * @param editor
	 *            The editor for which to create the plugins.
	 */
	protected void createPlugins(JGraphEditor editor) {
		if (defaultPlugins != null) {
			for (int i = 0; i < defaultPlugins.length; i++) {
				try {
					System.out.print("Loading " + defaultPlugins[i] + "... ");
					JGraphEditorPlugin plugin = (JGraphEditorPlugin) Class
							.forName(defaultPlugins[i]).newInstance();
					plugin.initialize(editor, null);
					System.out.println("Done");
				} catch (Throwable e) {
					System.out.println("Not Available");
				}
			}
		}
	}

	//
	// Custom graph and factory methods
	//

	/**
	 * Hook for subclassers to provide a custom graph for the user interface.
	 * This method is invoked by the default custom factory returned by the
	 * createEditorFactory method. It invokes
	 * {@link #configureGraph(JGraphEditor, JGraph)} to configure the new graph
	 * instance.
	 * 
	 * @see #createFactory(JGraphEditor)
	 */
	protected JGraph createGraph(JGraphEditor editor,
			GraphLayoutCache graphLayoutCache) {
		JGraph graph = new JGraphpadGraph(graphLayoutCache);
		configureGraph(editor, graph);
		return graph;
	}

	/**
	 * Hook for subclassers to configure a new graph. This implementation adds a
	 * {@link JGraphpadTransferHandler} and {@link JGraphpadMarqueeHandler} to
	 * the instance. (Note: {@link #createVertex()} is used to create the
	 * prototype cell for the transfer handler.)
	 * 
	 * @param graph
	 *            The graph to be configured
	 */
	protected void configureGraph(JGraphEditor editor, JGraph graph) {
		ToolTipManager.sharedInstance().registerComponent(graph);
		graph.setTransferHandler(new JGraphpadTransferHandler(createVertex()));
		graph.setMarqueeHandler(new JGraphpadMarqueeHandler(editor));
		graph.setInvokesStopCellEditing(true);
		graph.setJumpToDefaultPort(true);
		graph.setMoveOutOfGroups(true);
		graph.setMoveIntoGroups(true);
		graph.setDragEnabled(true);
		graph.setCloneable(true);
		graph.setOpaque(false);
	}

	//
	// Custom Cells (Vertices, Groups, Edges & Ports) and User Objects
	//

	/**
	 * Hook for subclassers to construct vertices with default user objects.
	 * This implementation invokes {@link #createVertexUserObject(Object)}and
	 * passes the return value to {@link #createVertex(Object)}.
	 * 
	 * @return Returns a new vertex with a default user object.
	 */
	public GraphCell createVertex() {
		return createVertex(createVertexUserObject(null));
	}

	/**
	 * Returns a new DefaultGraphCell containing the specified user object. This
	 * implementation uses {@link #createAttributeMap()}to create the map that
	 * holds the attributes for the new vertex, and
	 * {@link #configureVertex(GraphCell)}to configure the vertex.
	 * 
	 * @param userObj
	 *            The user object that the vertex should contain.
	 * @return Returns a new vertex.
	 */
	public GraphCell createVertex(Object userObj) {
		DefaultGraphCell vertex = new DefaultGraphCell(userObj,
				createAttributeMap());
		configureVertex(vertex);
		return vertex;
	}

	/**
	 * Hook for subclassers to configure the specified vertex. This
	 * implementation sets the {@link #defaultBorderColor}and adds ports if the
	 * vertex implements the {@link MutableTreeNode}interface.
	 * 
	 * @param vertex
	 *            The vertex to be configured.
	 * 
	 * @see #addPorts(MutableTreeNode, Point2D[])
	 */
	protected void configureVertex(GraphCell vertex) {
		AttributeMap attributes = vertex.getAttributes();
		if (defaultBorderColor != null) {
			GraphConstants.setOpaque(attributes, false);
			GraphConstants.setBorderColor(attributes, defaultBorderColor);
			GraphConstants.setGroupOpaque(attributes, false);
			JGraphpadGraphConstants.setGroupResize(attributes, true);
			JGraphpadGraphConstants.setInset(attributes, 4);
		}
		if (vertex instanceof MutableTreeNode)
			addPorts((MutableTreeNode) vertex, defaultPortLocations);
	}

	/**
	 * Hook for subclassers to construct groups with default user objects. This
	 * implementation invokes {@link #createVertexUserObject(Object)}and passes
	 * the return value to {@link #createGroup(Object)}.
	 * 
	 * @return Returns a new group with a default user object.
	 */
	public GraphCell createGroup() {
		return createVertex(createVertexUserObject(null));
	}

	/**
	 * Returns a new DefaultGraphCell containing the specified user object. This
	 * implementation uses {@link #createAttributeMap()}to create the map that
	 * holds the attributes for the new vertex, and
	 * {@link #configureVertex(GraphCell)}to configure the vertex.
	 * 
	 * @param userObj
	 *            The user object that the group should contain.
	 * @return Returns a new group.
	 */
	public GraphCell createGroup(Object userObj) {
		DefaultGraphCell vertex = new DefaultGraphCell(userObj,
				createAttributeMap());
		configureVertex(vertex);
		return vertex;
	}

	/**
	 * Adds ports to <code>parent</code> using <code>offsets</code> as the
	 * port relative offsets. The method uses
	 * {@link #createPortUserObject(Object)},
	 * {@link #createPort(MutableTreeNode, Object)}and
	 * {@link #configurePort(GraphCell, Point2D)}to create the ports and their
	 * user objects, configure them and add them to the parent.
	 * 
	 * @param parent
	 *            The parent to add the ports to.
	 * @param offsets
	 *            The points defining the port locations.
	 */
	protected void addPorts(MutableTreeNode parent, Point2D[] offsets) {
		for (int i = 0; i < offsets.length; i++) {
			GraphCell port = createPort(parent, createPortUserObject(null));
			configurePort(port, offsets[i]);
		}
	}

	/**
	 * Creates a port containing the specified user object and adds it to
	 * <code>parent</code>.
	 * 
	 * @param userObject
	 *            The user object that the port should contain.
	 * @return Returns a new port.
	 */
	public GraphCell createPort(MutableTreeNode parent, Object userObject) {
		DefaultGraphCell port = new DefaultPort(userObject);
		parent.insert(port, parent.getChildCount());
		port.setParent(parent);
		return port;
	}

	/**
	 * Hook for subclassers to configure the specified port using the
	 * <code>offset</code> as the relative location.
	 * 
	 * @param port
	 *            The port to be configured.
	 * @param offset
	 *            The relative offset of the port.
	 */
	public void configurePort(GraphCell port, Point2D offset) {
		AttributeMap map = port.getAttributes();
		if (offset != null)
			GraphConstants.setOffset(map, offset);
	}

	/**
	 * Hook for subclassers to construct edgges with default user objects. This
	 * implementation invokes {@link #createEdgeUserObject(Object)}and passes
	 * the return value to {@link #createEdge(Object)}.
	 * 
	 * @return Returns a new edge with a default user object.
	 */
	public DefaultEdge createEdge() {
		return createEdge(createEdgeUserObject(null));
	}

	/**
	 * Returns a new DefaultEdge containing the specified user object. This
	 * implementation uses {@link #createAttributeMap()}to create the map that
	 * holds the attributes for the new edge and
	 * {@link #configureEdge(GraphCell)}to configure the edge.
	 * 
	 * @param userObj
	 *            The user object that the edge should contain.
	 * @return Returns a new edge.
	 */
	public DefaultEdge createEdge(Object userObj) {
		DefaultEdge edge = new DefaultEdge(userObj, createAttributeMap());
		configureEdge(edge);
		return edge;
	}

	/**
	 * Hook for subclassers to configure the specified edge. This implementation
	 * sets the {@link #defaultEdgeFont},{@link #defaultEndDecoration}and
	 * {@link #defaultBeginDecoration}.
	 * 
	 * @param edge
	 *            The edge to be configured.
	 */
	protected void configureEdge(GraphCell edge) {
		AttributeMap attributes = edge.getAttributes();
		if (defaultEdgeFont != null)
			GraphConstants.setFont(attributes, defaultEdgeFont);
		if (defaultEndDecoration != GraphConstants.ARROW_NONE)
			GraphConstants.setLineEnd(attributes, defaultEndDecoration);
		if (defaultBeginDecoration != GraphConstants.ARROW_NONE)
			GraphConstants.setLineBegin(attributes, defaultBeginDecoration);
		if (edge instanceof MutableTreeNode)
			addPorts((MutableTreeNode) edge, defaultPortLocations);
	}

	/**
	 * Hook for subclassers to create a user object for edges that contains the
	 * specified value. This implementation calls
	 * {@link #createVertexUserObject(Object)}.
	 * 
	 * @param value
	 *            The value that the user object should contain.
	 * @return Returns a new user object containing <code>value</code>.
	 */
	protected Object createEdgeUserObject(Object value) {
		return createVertexUserObject(value);
	}

	/**
	 * Hook for subclassers to create a user object for ports that contains the
	 * specified value. This implementation calls
	 * {@link #createVertexUserObject(Object)}.
	 * 
	 * @param value
	 *            The value that the user object should contain.
	 * @return Returns a new user object containing <code>value</code>.
	 */
	protected Object createPortUserObject(Object value) {
		return createVertexUserObject(value);
	}

	/**
	 * Returns a new {@link JGraphpadBusinessObject}for the specified value.
	 * This implementation replaces all <code>null</code> values with an empty
	 * {@link JGraphpadRichTextValue}.
	 * 
	 * @param value
	 *            The value that the user object should contain.
	 * @return Returns a new user object containing <code>value</code>.
	 * 
	 * @see JGraphpadBusinessObject
	 */
	protected Object createVertexUserObject(Object value) {
		return new JGraphpadBusinessObject((value != null) ? value
				: new JGraphpadRichTextValue(""));
	}

	/**
	 * Hook for subclassers to construct attribute map for cells. This
	 * implementation returns a new instance of {@link AttributeMap}.
	 * 
	 * @return Returns a new attribute map.
	 */
	protected AttributeMap createAttributeMap() {
		return new AttributeMap();
	}

	/**
	 * Returns true if the specified filename has an image extension, namely one
	 * in {@link javax.imageio.ImageIO#getReaderFormatNames()}.
	 * 
	 * @param filename
	 *            The filename to be checked.
	 * @return Returns true if the filename is an image file.
	 */
	public static boolean isImage(String filename) {
		String[] formats = ImageIO.getReaderFormatNames();
		filename = filename.toLowerCase();
		for (int j = 0; j < formats.length; j++) {
			if (filename.endsWith(formats[j].toLowerCase()))
				return true;
		}
		return false;
	}

	/**
	 * Centers the specified window on the screen, taking into account the
	 * current size of the window.
	 * 
	 * @param wnd
	 *            The window to be centered.
	 */
	public static void center(Window wnd) {
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		Dimension frameSize = wnd.getSize();
		wnd.setLocation(screenSize.width / 2 - (frameSize.width / 2),
				screenSize.height / 2 - (frameSize.height / 2));
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
			Map arguments = new Hashtable();
			List files = new LinkedList();
			if (args != null) {

				String key = null;
				String arg = null;
				for (int i = 0; i < args.length; i++) {
					arg = args[i];
					if (arg.startsWith("-")) {
						key = arg.substring(1);
						arguments.put(key, "");
						if (key.equals(ARG_VERSION))
							key = null;
					} else if (key != null) {
						arguments.put(key, arg);
						key = null;
					} else if (arg.length() > 0) {
						files.add(arg);
					}
				}
			}
			if (arguments.containsKey("?") || arguments.containsKey("help")
					|| arguments.containsKey("-?")
					|| arguments.containsKey("-help")) {
				System.out.println(USAGE);
			} else if (arguments.containsKey(ARG_VERSION)) {
				System.out.println(JGraphpad.VERSION + "\n" + JGraph.VERSION);
			} else {
				// in plugin init
				new JGraphpad().createApplication(files, arguments);
			}
		} catch (Exception e) {
			e.printStackTrace();

			// Terminates abnormally
			System.exit(1);
		}
	}

	/**
	 * Defines the usage information (use --help), see
	 * http://java.sun.com/docs/books/tutorial/uiswing/misc/plaf.html#programmatic
	 * on setting the look and feel.
	 */
	public static final String USAGE = "Usage: java com.jgraph.JGraphpad [OPTION]...\n"
			+ "  -"
			+ ARG_SYSTEMLOOKANDFEEL
			+ "                               use system look and feel\n"
			+ ARG_JGOODIESLOOKANDFEEL
			+ "                               jgoodies look and feel\n"
			+ "  -"
			+ ARG_VERSION
			+ "                               print version\n"
			+ "\nTip: java -Dswing.defaultlaf=com.jgoodies.looks.plastic.Plastic3DLookAndFeel\n"
			+ "Exit status is 0 if OK, 1 if minor problems, 2 if serious trouble.\n"
			+ "\nReport bugs via http://www.jgraph.com/tracker\n\n";

}
