/* 
 * $Id: JGraphpadConsole.java,v 1.1.1.1 2005/08/04 11:21:58 gaudenz Exp $
 * Copyright (c) 2001-2005, Gaudenz Alder
 * 
 * All rights reserved.
 * 
 * See LICENSE file for license details. If you are unable to locate
 * this file please contact info (at) jgraph (dot) com.
 */
package com.jgraph.pad.factory;

import java.awt.Component;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import javax.swing.JTextArea;

import org.w3c.dom.Node;

import com.jgraph.editor.JGraphEditorResources;
import com.jgraph.editor.factory.JGraphEditorFactoryMethod;

/**
 * Console to display a print stream.
 */
public class JGraphpadConsole extends JTextArea {

	/**
	 * Holds the print stream that acts as a data source.
	 */
	protected PrintStream stream;

	/**
	 * Constructs a console for the specified stream. If stream is one of
	 * System.out or System.err then the respective stream will be redirected to
	 * the constructed console.
	 * 
	 * @param stream
	 *            The stream that is to be displayed.
	 */
	public JGraphpadConsole(PrintStream stream) {
		this.stream = stream;
		PrintStream textStream = new JTextAreaOutputStream(this, stream, true);
		try {
			if (stream == System.out)
				System.setOut(textStream);
			else if (stream == System.err)
				System.setErr(textStream);
		} catch (SecurityException e) {
			textStream.println(JGraphEditorResources
					.getString("SecurityRestrictions"));
		}
	}

	/**
	 * @return Returns the stream.
	 */
	public PrintStream getStream() {
		return stream;
	}

	/**
	 * @param stream
	 *            The stream to set.
	 */
	public void setStream(PrintStream stream) {
		this.stream = stream;
	}

	/**
	 * A PrintStream for the text area output.
	 */
	class JTextAreaOutputStream extends PrintStream {

		/**
		 * The target for this printstream.
		 */
		private JTextArea target = null;

		/**
		 * The original PrintStream to forward this stream to the original
		 * stream.
		 */
		private PrintStream orig = null;

		/**
		 * Flag is true if the stream should forward the output to the original
		 * stream.
		 */
		private boolean showOrig = false;

		/**
		 * Constructs a new output stream for the specified stream and text
		 * area.
		 */
		public JTextAreaOutputStream(JTextArea t, PrintStream orig,
				boolean showOrig) {
			super(new ByteArrayOutputStream());
			target = t;

			this.showOrig = showOrig;
			this.orig = orig;
		}

		/**
		 * Writes the value to the target
		 */
		public void print(boolean b) {
			if (showOrig)
				orig.print(b);
			if (b)
				target.append("true");
			else
				target.append("false");
			target.setCaretPosition(target.getText().length());
		}

		/**
		 * Writes the value to the target
		 */
		public void println(boolean b) {
			if (showOrig)
				orig.println(b);

			if (b)
				target.append("true\n");
			else
				target.append("false\n");
			target.setCaretPosition(target.getText().length());
		}

		/**
		 * Writes the value to the target
		 */
		public void print(char c) {
			if (showOrig)
				orig.print(c);

			char[] tmp = new char[1];
			tmp[0] = c;
			target.append(new String(tmp));
			target.setCaretPosition(target.getText().length());
		}

		/**
		 * Writes the value to the target
		 */
		public void println(char c) {
			if (showOrig)
				orig.println(c);

			char[] tmp = new char[2];
			tmp[0] = c;
			tmp[1] = '\n';
			target.append(new String(tmp));
			target.setCaretPosition(target.getText().length());
		}

		/**
		 * Writes the value to the target
		 */
		public void print(char[] s) {
			if (showOrig)
				orig.print(s);

			target.append(new String(s));
			target.setCaretPosition(target.getText().length());
		}

		/**
		 * Writes the value to the target
		 */
		public void println(char[] s) {
			if (showOrig)
				orig.println(s);

			target.append(new String(s) + "\n");
			target.setCaretPosition(target.getText().length());
		}

		/**
		 * Writes the value to the target
		 */
		public void print(double d) {
			if (showOrig)
				orig.print(d);

			target.append(Double.toString(d));
			target.setCaretPosition(target.getText().length());
		}

		/**
		 * Writes the value to the target
		 */
		public void println(double d) {
			if (showOrig)
				orig.println(d);

			target.append(Double.toString(d) + "\n");
			target.setCaretPosition(target.getText().length());
		}

		/**
		 * Writes the value to the target
		 */
		public void print(float f) {
			if (showOrig)
				orig.print(f);

			target.append(Float.toString(f));
			target.setCaretPosition(target.getText().length());
		}

		/**
		 * Writes the value to the target
		 */
		public void println(float f) {
			if (showOrig)
				orig.println(f);

			target.append(Float.toString(f) + "\n");
			target.setCaretPosition(target.getText().length());
		}

		/**
		 * Writes the value to the target
		 */
		public void print(int i) {
			if (showOrig)
				orig.print(i);

			target.append(Integer.toString(i));
			target.setCaretPosition(target.getText().length());
		}

		/**
		 * Writes the value to the target
		 */
		public void println(int i) {
			if (showOrig)
				orig.println(i);

			target.append(Integer.toString(i) + "\n");
			target.setCaretPosition(target.getText().length());
		}

		/**
		 * Writes the value to the target
		 */
		public void print(long l) {
			if (showOrig)
				orig.print(l);

			target.append(Long.toString(l));
			target.setCaretPosition(target.getText().length());
		}

		/**
		 * Writes the value to the target
		 */
		public void println(long l) {
			if (showOrig)
				orig.println(l);

			target.append(Long.toString(l) + "\n");
			target.setCaretPosition(target.getText().length());
		}

		/**
		 * Writes the value to the target
		 */
		public void print(Object o) {
			if (showOrig)
				orig.print(o);

			target.append(o.toString());
			target.setCaretPosition(target.getText().length());
		}

		/**
		 * Writes the value to the target
		 */
		public void println(Object o) {
			if (showOrig)
				orig.println(o);

			target.append(o.toString() + "\n");
			target.setCaretPosition(target.getText().length());
		}

		/**
		 * Writes the value to the target
		 */
		public void print(String s) {
			if (showOrig)
				orig.print(s);

			target.append(s);
			target.setCaretPosition(target.getText().length());
		}

		/**
		 * Writes the value to the target
		 */
		public void println(String s) {
			if (showOrig)
				orig.println(s);

			target.append(s + "\n");
			target.setCaretPosition(target.getText().length());
		}

		/**
		 * Writes the value to the target
		 */
		public void println() {
			if (showOrig)
				orig.println();

			target.append(new String("\n"));
			target.setCaretPosition(target.getText().length());
		}
	}

	/**
	 * Provides a factory method to construct a console for System.err.
	 */
	public static class FactoryMethod extends JGraphEditorFactoryMethod {

		/**
		 * Defines the default name for factory methods of this kind.
		 */
		public static String NAME = "createConsole";

		/**
		 * Constructs a new console factory method using {@link #NAME}.
		 */
		public FactoryMethod() {
			super(NAME);
		}

		/*
		 * (non-Javadoc)
		 */
		public Component createInstance(Node configuration) {
			JGraphpadConsole console = new JGraphpadConsole(System.err);
			console.setEditable(false);
			return console;
		}

	}

}