/* 
 * $Id: JGraphEditorModel.java,v 1.8 2007/08/29 09:30:49 gaudenz Exp $
 * Copyright (c) 2001-2005, Gaudenz Alder
 * 
 * All rights reserved.
 * 
 * See LICENSE file for license details. If you are unable to locate
 * this file please contact info (at) jgraph (dot) com.
 */
package com.jgraph.editor;

import java.beans.BeanInfo;
import java.beans.DefaultPersistenceDelegate;
import java.beans.Encoder;
import java.beans.ExceptionListener;
import java.beans.Expression;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PersistenceDelegate;
import java.beans.PropertyDescriptor;
import java.beans.XMLDecoder;
import java.beans.XMLEncoder;
import java.io.BufferedInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

import org.jgraph.event.GraphLayoutCacheEvent;
import org.jgraph.event.GraphLayoutCacheListener;
import org.jgraph.event.GraphModelEvent;
import org.jgraph.event.GraphModelListener;
import org.jgraph.graph.DefaultEdge;
import org.jgraph.graph.DefaultGraphModel;
import org.jgraph.graph.EdgeView;
import org.jgraph.graph.GraphConstants;
import org.jgraph.graph.GraphLayoutCache;
import org.jgraph.graph.PortView;
import org.jgraph.graph.VertexView;

import com.jgraph.JGraphpad;

/**
 * Default document model for JGraph editors. This class is in charge of
 * preparing the bean info classes and has a map of persistence delegate for XML
 * encoding. You should use the writeObject and readObject method of this class
 * for file I/O to properly use the registered peristence delegates. <br>
 * Note: The API refers to the children of the {@link #rootNode}as roots.
 */
public class JGraphEditorModel extends DefaultTreeModel {

	/**
	 * Prepares class bean infos for XML encoding. Note: To encode cell views
	 * they must have an empty constructor and all fields (except cell and
	 * attributes) must be marked transient in the BeanInfo. To do this for a
	 * new cell view, use {@link #makeCellViewFieldsTransient(Class)}.
	 */
	static {
		makeCellViewFieldsTransient(PortView.class);
		makeCellViewFieldsTransient(VertexView.class);
		makeCellViewFieldsTransient(EdgeView.class);
	}

	/**
	 * Holds the (class, persistence delegate) pairs.
	 */
	protected Map persistenceDelegates = new Hashtable();

	/**
	 * Reference to the mutable root node.
	 */
	protected DefaultMutableTreeNode rootNode;

	/**
	 * Constructs a new JGraph editor model adding persistence delegates for the
	 * DefaultGraphModel, GraphLayoutCache and DefaultEdge.DefaultRouting
	 * classes. This also adds a tree model listener to this model to update the
	 * modified state of parent files for child changes.
	 */
	public JGraphEditorModel() {
		super(new DefaultMutableTreeNode());
		rootNode = (DefaultMutableTreeNode) getRoot();

		// Adds a listener to update parent file states
		// on child changes.
		addTreeModelListener(new TreeModelListener() {

			public void treeNodesChanged(TreeModelEvent e) {
				Object[] children = e.getChildren();
				for (int i = 0; i < children.length; i++) {
					JGraphEditorFile file = getParentFile((TreeNode) children[i]);
					
					if (JGraphpad.INNER_LIBRARIES &&
						file.getParent() instanceof JGraphEditorFile)
					{
						file = (JGraphEditorFile) file.getParent();
					}
					
					if (file != children[i])
						setModified(file, true);
				}
			}

			public void treeNodesInserted(TreeModelEvent e) {
				Object[] children = e.getChildren();
				for (int i = 0; i < children.length; i++) {
					JGraphEditorFile file = getParentFile((TreeNode) children[i]);
					
					if (JGraphpad.INNER_LIBRARIES &&
						file.getParent() instanceof JGraphEditorFile)
					{
						file = (JGraphEditorFile) file.getParent();
					}
					
					if (file != children[i])
						setModified(file, true);
				}
			}

			public void treeNodesRemoved(TreeModelEvent e) { // empty
			}

			public void treeStructureChanged(TreeModelEvent e) { // empty
			}

		});

		// Adds default persistence delegates
		addPersistenceDelegate(DefaultGraphModel.class,
				new DefaultPersistenceDelegate(new String[] { "roots",
						"attributes" }));
		addPersistenceDelegate(GraphLayoutCache.class,
				new DefaultPersistenceDelegate(new String[] { "model",
						"factory", "cellViews", "hiddenCellViews", "partial" }));

		// DefaultEdge.DefaultRouting has a shared instance which may
		// be retrieved using GraphConstants.getROUTING_SIMPLE().
		addPersistenceDelegate(DefaultEdge.DefaultRouting.class,
				new PersistenceDelegate() {
					protected Expression instantiate(Object oldInstance,
							Encoder out) {
						return new Expression(oldInstance,
								GraphConstants.class, "getROUTING_SIMPLE", null);
					}
				});

		addPersistenceDelegate(DefaultEdge.LoopRouting.class,
				new PersistenceDelegate() {
					protected Expression instantiate(Object oldInstance,
							Encoder out) {
						return new Expression(oldInstance,
								GraphConstants.class, "getROUTING_DEFAULT",
								null);
					}
				});
	}

	/**
	 * Returns the first generation of childs, aka roots. This usually returns
	 * the documents and repositories, eg files that are currently open.
	 * 
	 * @return Returns the children of {@link #rootNode}.
	 */
	public Enumeration roots() {
		return rootNode.children();
	}

	/**
	 * Associates the specified persistence delegate with <code>clazz</code>
	 * for XML encoding.
	 * 
	 * @param clazz
	 *            The class to associate the delegate with.
	 * @return Returns the previous delegate for <code>clazz</code>.
	 */
	public Object addPersistenceDelegate(Class clazz,
			PersistenceDelegate delegate) {
		if (delegate != null)
			return persistenceDelegates.put(clazz, delegate);
		return null;
	}

	/**
	 * Returns the associated persistence delegate for <code>clazz</code> or
	 * <code>null</code> if no association exists.
	 * 
	 * @param clazz
	 *            The clazz to return the delegate for.
	 * @return Returns the persistence delegate for <code>clazz</code> or
	 *         <code>null</code>.
	 */
	public PersistenceDelegate getPersistenceDelegate(Class clazz) {
		if (clazz != null)
			return (PersistenceDelegate) persistenceDelegates.get(clazz);
		return null;
	}

	/**
	 * Adds the specified root as a child to {@link #rootNode}. Calls
	 * {@link #installListeners(TreeNode)}on the node.
	 * 
	 * @param node
	 *            The node to add to {@link #rootNode}.
	 * @return Returns the node that has been added.
	 */
	public MutableTreeNode addRoot(MutableTreeNode node) {
		insertNodeInto(node, rootNode, getChildCount(getRoot()));
		installListeners(node);
		return node;
	}

	/**
	 * Adds the specified child to <code>parent</code>. Calls
	 * {@link #installListeners(TreeNode)}on the child.
	 * 
	 * @param child
	 *            The node to add to <code>parent</code>.
	 * @param parent
	 *            The parent to add <code>child</code> to.
	 * @return Returns the child that has been added.
	 */
	public MutableTreeNode addChild(MutableTreeNode child,
			MutableTreeNode parent) {
		insertNodeInto(child, parent, getChildCount(parent));
		installListeners(child);
		return child;
	}


	/**
	 * Reads the specified URI and returns the deserialized object.
	 * 
	 * @param uri
	 *            The URI to read the object from.
	 * @return Returns the object stat was added.
	 */
	public Object readFile(String uri) throws MalformedURLException, IOException {
		if (uri != null) {
			Object file = getFileByFilename(uri);
			if (file == null) {
				InputStream in = getInputStream(uri);
				file = readObject(in);
				in.close();
				return file;
			}
		}
		return null;
	}
	
	/**
	 * Reads the specified URI and adds it as a root.
	 * 
	 * @param uri
	 *            The URI to read the object from.
	 * @return Returns the object stat was added.
	 */
	public Object addFile(String uri) throws MalformedURLException, IOException {
		if (uri != null) {
			Object file = readFile(uri);
			if (file instanceof JGraphEditorFile) {
				((JGraphEditorFile) file).setFilename(uri);
				addRoot((JGraphEditorFile) file);
			}
			return file;
		}
		return null;
	}

	/**
	 * Hook for subclassers to install the required listeners in new tree nodes.
	 * This is invoked recursively for tree nodes and calls
	 * {@link #installDiagramListeners(JGraphEditorDiagram)}on all diagrams
	 * that are found along the invocation chain.
	 * 
	 * @param node
	 *            The node to scan for diagrams.
	 */
	protected void installListeners(TreeNode node) {
		if (node instanceof TreeNode) {
			for (int i = 0; i < getChildCount(node); i++) {
				Object child = getChild(node, i);
				if (child instanceof TreeNode)
					installListeners((TreeNode) child);
			}
		}
		if (node instanceof JGraphEditorDiagram) {
			installDiagramListeners((JGraphEditorDiagram) node);
		}
	}

	/**
	 * Installs the listeners required to update the modified state of the
	 * parent file node to <code>diagram</code>. This implementation adds a
	 * graph layout cache listener and a graph model listener.
	 * 
	 * @param diagram
	 *            The diagram to install the listeners to.
	 */
	protected void installDiagramListeners(final JGraphEditorDiagram diagram) {
		diagram.getGraphLayoutCache().addGraphLayoutCacheListener(
				new GraphLayoutCacheListener() {

					public void graphLayoutCacheChanged(GraphLayoutCacheEvent e) {
						JGraphEditorFile file = getParentFile(diagram);
						if (JGraphpad.INNER_LIBRARIES &&
							file.getParent() instanceof JGraphEditorFile)
						{
							file = (JGraphEditorFile) file.getParent();
						}
						setModified(file, true);
					}

				});
		diagram.getGraphLayoutCache().getModel().addGraphModelListener(
				new GraphModelListener() {

					public void graphChanged(GraphModelEvent e) {
						JGraphEditorFile file = getParentFile(diagram);
						if (JGraphpad.INNER_LIBRARIES &&
							file.getParent() instanceof JGraphEditorFile)
						{
							file = (JGraphEditorFile) file.getParent();
						}
						setModified(file, true);
					}
				});
	}

	/**
	 * Sets the user object of the specified node and dispatches a notification
	 * event.
	 * 
	 * @param node
	 *            The node to change the user object for.
	 * @param userObject
	 *            The new user object.
	 */
	public void setUserObject(TreeNode node, Object userObject) {
		TreePath path = new TreePath(getPathToRoot(node));
		valueForPathChanged(path, userObject);
	}

	/**
	 * Sets the filename of the specified file and dispatches a notification
	 * event.
	 * 
	 * @param file
	 *            The file to change the filename for.
	 * @param filename
	 *            The new filename.
	 */
	public void setFilename(JGraphEditorFile file, String filename) {
		file.setFilename(filename);
		nodeChanged(file);
	}

	/**
	 * Sets the name of the specified diagram and dispatches a notification
	 * event.
	 * 
	 * @param diagram
	 *            The diagram to change the name for.
	 * @param name
	 *            The new name.
	 */
	public void setName(JGraphEditorDiagram diagram, String name) {
		diagram.setName(name);
		nodeChanged(diagram);
	}

	/**
	 * Sets the modified state of the specified file and dispatches a
	 * notification event.
	 * 
	 * @param file
	 *            The file to change the modified state for.
	 * @param modified
	 *            The new modified state.
	 */
	public void setModified(JGraphEditorFile file, boolean modified) {
		if (file != null) {
			file.setModified(modified);
			nodeChanged(file);
		}
	}

	/**
	 * Returns the file for the specified filename if it is in the model or
	 * <code>null</code> if no such file exists.
	 * 
	 * @param filename
	 *            The filename to return the file for.
	 * @return Returns the file for <code>filename</code> or <code>null</code>.
	 */
	public JGraphEditorFile getFileByFilename(String filename) {
		int childCount = getChildCount(rootNode);
		for (int i = 0; i < childCount; i++) {
			Object child = getChild(rootNode, i);
			if (child instanceof JGraphEditorFile) {
				JGraphEditorFile file = (JGraphEditorFile) child;
				if (file.getFilename() != null
						&& file.getFilename().equals(filename)) {
					return file;
				}
			}
		}
		return null;
	}

	/**
	 * Writes the specified object to the output stream using an xml encoder
	 * which was configured using {@link #configureEncoder(XMLEncoder)}. The
	 * exceptions that are thrown during encoding are caught by a local handler
	 * and passed to the caller as a RuntimeException with description of the
	 * encoding problems. <br>
	 * Note: You should use this method as a global hook to write all XML files.
	 * 
	 * @param object
	 *            The object to be written.
	 * @param out
	 *            The output strem to write to.
	 * 
	 * @throws RuntimeException
	 *             If there are problems during encoding.
	 */
	public void writeObject(Object object, OutputStream out) {
		final List problems = new LinkedList();
		if (object != null) {
			XMLEncoder enc = new XMLEncoder(out);
			enc.setExceptionListener(new ExceptionListener() {
				public void exceptionThrown(Exception e) {
					// Uncomment this line for debugging
					// XML encoding:
					e.printStackTrace();
					problems.add(e);
				}
			});
			configureEncoder(enc);
			enc.writeObject(object);
			enc.close();
		}
		if (!problems.isEmpty())
			throw new RuntimeException(problems.size()
					+ " errors while writing " + object + " ("
					+ problems.get(0) + ")");
	}

	/**
	 * Hook for subclassers to configure a new XML encoder for writing an
	 * object. This implementation sets all registered persistence delegates and
	 * installs default mappings for classes (eg. it assigns the list
	 * persistence delegates to array lists).
	 * 
	 * @param enc
	 *            The encoder to be configured.
	 */
	protected void configureEncoder(XMLEncoder enc) {
		Iterator it = persistenceDelegates.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry entry = (Map.Entry) it.next();
			enc.setPersistenceDelegate((Class) entry.getKey(),
					(PersistenceDelegate) entry.getValue());
		}
		enc.setPersistenceDelegate(ArrayList.class, enc
				.getPersistenceDelegate(List.class));
	}

	/**
	 * Hook for subclassers to create an input stream for the specified URI.
	 * This implementation creates an input stream using
	 * {@link JGraphEditorResources#getInputStream(String)} and wraps it in a
	 * {@link GZIPInputStream} if the URI ends with <code>.gz</code>.
	 * 
	 * @param uri
	 *            The URI to return the input stream for.
	 * @return Return an input stream for the specified URI.
	 */
	public InputStream getInputStream(String uri) throws MalformedURLException,
			FileNotFoundException, IOException {
		InputStream in = JGraphEditorResources.getInputStream(uri);
		if (uri.toLowerCase().endsWith(".gz"))
			in = new GZIPInputStream(in);
		return new BufferedInputStream(in);
	}

	/**
	 * Hook for subclassers to create an output stream for the specified URI.
	 * This implementation creates an output stream using
	 * {@link JGraphEditorResources#getOutputStream(String)} and wraps it in a
	 * {@link java.util.zip.GZIPOutputStream} if the URI ends with
	 * <code>.gz</code>.
	 * 
	 * @param uri
	 *            The URI to return the output stream for.
	 * @return Returns an output stream for the specified URI.
	 */
	public OutputStream getOutputStream(String uri) throws IOException {
		OutputStream out = JGraphEditorResources.getOutputStream(uri);
		if (uri.toLowerCase().endsWith(".gz"))
			out = new GZIPOutputStream(out);
		return out;
	}

	/**
	 * Returns a new object from the specified stream using a new XML decoder.
	 * This method does nothing special. Subclassers can override this method if
	 * they need to do anything special with opened files. <br>
	 * Note: You should use this method as a global hook to read all XML files.
	 * 
	 * @return Returns a new object from the specified stream.
	 */
	public Object readObject(InputStream in) {
		XMLDecoder dec = new XMLDecoder(in);
		if (dec != null) {
			Object obj = dec.readObject();
			dec.close();
			return obj;
		}
		return null;
	}

	/**
	 * Makes the specified field transient in the bean info of
	 * <code>clazz</code>.
	 * 
	 * @param clazz
	 *            The class whos field should be made transient.
	 * @param field
	 *            The name of the field that should be made transient.
	 */
	public static void makeTransient(Class clazz, String field) {
		try {
			BeanInfo info = Introspector.getBeanInfo(clazz);
			PropertyDescriptor[] propertyDescriptors = info
					.getPropertyDescriptors();
			for (int i = 0; i < propertyDescriptors.length; ++i) {
				PropertyDescriptor pd = propertyDescriptors[i];
				if (pd.getName().equals(field)) {
					pd.setValue("transient", Boolean.TRUE);
				}
			}
		} catch (IntrospectionException e) {
			// ignore
		}
	}

	/**
	 * Makes all fields but <code>cell</code> and <code>attributes</code>
	 * transient in the bean info of <code>clazz</code>.
	 * 
	 * @param clazz
	 *            The cell view class who fields should be made transient.
	 */
	public static void makeCellViewFieldsTransient(Class clazz) {
		try {
			BeanInfo info = Introspector.getBeanInfo(clazz);
			PropertyDescriptor[] propertyDescriptors = info
					.getPropertyDescriptors();
			for (int i = 0; i < propertyDescriptors.length; ++i) {
				PropertyDescriptor pd = propertyDescriptors[i];
				if (!pd.getName().equals("cell")
						&& !pd.getName().equals("attributes")) {
					pd.setValue("transient", Boolean.TRUE);
				}
			}
		} catch (IntrospectionException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Returns the parent file for <code>node</code> or the node itself, if it
	 * is a file. This method returns <code>null</code> if no parent file is
	 * found for <code>node</code>.
	 * 
	 * @param node
	 *            The node to find the parent file for.
	 * @return Returns the parent file for node, the node itself or
	 *         <code>null</code>.
	 */
	public static JGraphEditorFile getParentFile(TreeNode node) {
		while (node != null) {
			if (node instanceof JGraphEditorFile)
				return (JGraphEditorFile) node;
			node = node.getParent();
		}
		return null;
	}

}
