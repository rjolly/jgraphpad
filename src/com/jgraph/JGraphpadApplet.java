/* 
 * $Id: JGraphpadApplet.java,v 1.9 2007/10/18 09:21:42 david Exp $
 * Copyright (c) 2001-2005, Gaudenz Alder
 * 
 * All rights reserved.
 * 
 * See LICENSE file for license details. If you are unable to locate
 * this file please contact info (at) jgraph (dot) com.
 */
package com.jgraph;

import java.applet.Applet;
import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Hashtable;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JTextPane;
import javax.swing.text.rtf.RTFEditorKit;

import org.jgraph.graph.AbstractCellView;
import org.jgraph.graph.DefaultGraphCellEditor;
import org.jgraph.graph.EdgeRenderer;
import org.jgraph.graph.EdgeView;
import org.jgraph.graph.PortRenderer;
import org.jgraph.graph.PortView;
import org.jgraph.graph.VertexRenderer;
import org.jgraph.graph.VertexView;

import com.jgraph.pad.dialog.JGraphpadDialogs;
import com.jgraph.pad.graph.JGraphpadEdgeRenderer;
import com.jgraph.pad.graph.JGraphpadEdgeView;
import com.jgraph.pad.graph.JGraphpadRichTextEditor;
import com.jgraph.pad.graph.JGraphpadRichTextValue;
import com.jgraph.pad.graph.JGraphpadVertexRenderer;
import com.jgraph.pad.graph.JGraphpadVertexView;
import com.jgraph.pad.util.JGraphpadFocusManager;

/**
 * Displays a button to launch a new JGraphpad application.
 */
public class JGraphpadApplet extends Applet {

	/**
	 * Contains the arguments found in the init method.
	 */
	protected Map arguments;

	/**
	 * Constructs a new JGraphpad applet.
	 */
	public JGraphpadApplet() {
		appletStaticFixes();
		setLayout(new BorderLayout());

		// Adds the start button to the applet
		// and trigger class init
		JButton startButton = new JButton(JGraphpad.APPTITLE);

		// Starts a new application when the start button
		// is clicked. This overrides the exit method
		// of JGraphpad, not JGraphEditor.
		startButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					// Overrides the exit method to not
					// call System.exit, but stop the
					// applet instead.
					new JGraphpad() {
						protected void exit(int code) {
							// empty
						}
					}.createApplication(null, arguments);

				} catch (Exception e1) {
					JGraphpadDialogs.getSharedInstance().errorDialog(
							JGraphpadApplet.this, e1.getMessage());
					e1.printStackTrace();
				}
			}
		});
		add(startButton, BorderLayout.CENTER);
	}

	/**
	 * Fetches the parameters from the applet tag.
	 */
	public void init() {
		// Constructs the arguments from the applet
		// parameters. This requires the arguments
		// to be known by name, however, since most
		// applet pages are static this should be OK.
		// Override the hook to add more arguments.
		arguments = processArguments();
	}

	public void destroy() {
		super.destroy();
		PortView.renderer = new PortRenderer();
		EdgeView.renderer = new EdgeRenderer();
		AbstractCellView.cellEditor = new DefaultGraphCellEditor();
		VertexView.renderer = new VertexRenderer();
	}
	
	/**
	 * Hook for subclassers to add more arguments from the applet page. It is
	 * allowed to return null, eg:
	 * 
	 * <PRE>
	 * 
	 * String param = getParameter("myParameter"); if (param != null)
	 * arguments.put("myParameter", param);
	 * 
	 * </PRE>
	 */
	protected Map processArguments() {
		return new Hashtable();
	}

	/**
	 * Workaround for the statics not reloaded in applet problem.
	 * 
	 */
	public void appletStaticFixes() {
		// These cases fall foul of an applet re-load. The browser plugin
		// doesn't reload classes in this case which means statics are not
		// re-initialized.
		JGraphpadFocusManager.currentGraphFocusManager = new JGraphpadFocusManager();
		JGraphpadRichTextValue.editorKit = new RTFEditorKit();
		JGraphpadVertexRenderer.textPane = new JTextPane();
		JGraphpadVertexView.editor = new JGraphpadRichTextEditor();
		JGraphpadVertexView.renderer = new JGraphpadVertexRenderer();
		JGraphpadEdgeView.editor = new JGraphpadRichTextEditor();
		JGraphpadEdgeView.renderer = new JGraphpadEdgeRenderer();
	}

}
