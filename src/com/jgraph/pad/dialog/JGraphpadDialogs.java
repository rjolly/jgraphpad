/* 
 * $Id: JGraphpadDialogs.java,v 1.6 2007/08/24 10:54:15 gaudenz Exp $
 * Copyright (c) 2001-2005, Gaudenz Alder
 * 
 * All rights reserved.
 * 
 * See LICENSE file for license details. If you are unable to locate
 * this file please contact info (at) jgraph (dot) com.
 */
package com.jgraph.pad.dialog;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.security.AccessControlException;

import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.filechooser.FileFilter;

import com.jgraph.JGraphEditor;
import com.jgraph.JGraphpad;
import com.jgraph.editor.JGraphEditorResources;
import com.jgraph.pad.util.JGraphpadFileFilter;
import com.jgraph.pad.util.JGraphpadFileFilter.EditorFileFilter;

/**
 * Singleton class that provides a set of standard dialogs.
 */
public class JGraphpadDialogs {

	/**
	 * Shared singleton instance.
	 */
	protected static JGraphpadDialogs sharedInstance = new JGraphpadDialogs();

	/**
	 * Shared simple font dialog.
	 */
	protected static FontDialog fontDialog = new FontDialog();

	/**
	 * Holds the various file choosers to preserve their states between uses.
	 */
	protected JFileChooser saveEditorChooser, openEditorChooser,
			saveImageChooser, openImageChooser;

	/**
	 * Singleton constructor.
	 */
	protected JGraphpadDialogs() {
		// Defines the filters for editor files
		try {
			FileFilter allEditorFilter = new EditorFileFilter(
					JGraphEditorResources
							.getString("AllJGraphpadFilesDescription"));
			FileFilter compressedEditorFilter = new JGraphpadFileFilter(
					".xml.gz", JGraphEditorResources
							.getString("JGraphpadCompressedFileDescription"));
			FileFilter uncompressedEdiorFilter = new JGraphpadFileFilter(
					".xml", JGraphEditorResources
							.getString("JGraphpadFileDescription"));

			// Constructs the save editor file chooser
			saveEditorChooser = new JFileChooser();
			saveEditorChooser.addChoosableFileFilter(compressedEditorFilter);
			saveEditorChooser.addChoosableFileFilter(uncompressedEdiorFilter);
			saveEditorChooser.setFileFilter(compressedEditorFilter);

			// Constructs the open editor file chooser
			openEditorChooser = new JFileChooser();
			openEditorChooser.addChoosableFileFilter(allEditorFilter);
			openEditorChooser.addChoosableFileFilter(compressedEditorFilter);
			openEditorChooser.addChoosableFileFilter(uncompressedEdiorFilter);
			openEditorChooser.setFileFilter(allEditorFilter);

			// Defines the filter for image files
			FileFilter allImageFilter = new JGraphpadFileFilter.ImageFileFilter(
					JGraphEditorResources.getString("AllImagesDescription"));
			FileFilter pngFilter = new JGraphpadFileFilter(".png", "PNG "
					+ JGraphEditorResources.getString("File") + " (.png)");
			FileFilter jpgFilter = new JGraphpadFileFilter(".jpg", "JPG "
					+ JGraphEditorResources.getString("File") + " (.jpg)");
			FileFilter gifFilter = new JGraphpadFileFilter(".gif", "GIF "
					+ JGraphEditorResources.getString("File") + " (.gif)");
			FileFilter bmpFilter = new JGraphpadFileFilter(".bmp", "BMP "
					+ JGraphEditorResources.getString("File") + " (.bmp)");

			// Constructs the save image file chooser
			saveImageChooser = new JFileChooser();
			saveImageChooser.addChoosableFileFilter(pngFilter);
			saveImageChooser.addChoosableFileFilter(jpgFilter);
			saveImageChooser.addChoosableFileFilter(gifFilter);
			saveImageChooser.addChoosableFileFilter(bmpFilter);
			saveImageChooser.setFileFilter(pngFilter);

			// Constructs the open image file chooser
			openImageChooser = new JFileChooser();
			openImageChooser.addChoosableFileFilter(allImageFilter);
			openImageChooser.addChoosableFileFilter(jpgFilter);
			openImageChooser.addChoosableFileFilter(pngFilter);
			openImageChooser.addChoosableFileFilter(gifFilter);
			openImageChooser.addChoosableFileFilter(bmpFilter);
			openImageChooser.setFileFilter(allImageFilter);
		} catch (Throwable e) {
			// ignore
		}
	}

	/**
	 * Returns the singleton instance.
	 * 
	 * @return Returns {@link #sharedInstance}.
	 */
	public static JGraphpadDialogs getSharedInstance() {
		return sharedInstance;
	}

	/**
	 * Displays a color dialog using
	 * {@link JColorChooser#showDialog(java.awt.Component, java.lang.String, java.awt.Color)}.
	 * 
	 * @param component
	 *            The parent component for the dialog to be displayed.
	 * @param title
	 *            The title of the dialog to be displayed.
	 * @param color
	 *            The default color to use in the dialog.
	 * @return Returns the selected color.
	 */
	public Color colorDialog(Component component, String title, Color color) {
		return JColorChooser.showDialog(component, title, color);
	}

	/**
	 * Displays a simple font dialog using {@link FontDialog}.
	 * 
	 * @param component
	 *            The parent component for the dialog to be displayed.
	 * @param title
	 *            The title of the dialog to be displayed.
	 * @return Returns the selected font.
	 */
	public Font fontDialog(Component component, String title) {
		fontDialog.setVisible(true);
		return fontDialog.getFont();
	}

	/**
	 * Shortcut method to {@link #valueDialog(String, String)} with an empty
	 * initial value.
	 * 
	 * @param title
	 *            The title of the dialog to be displayed.
	 * @return Returns the user input.
	 */
	public String valueDialog(String title) {
		return valueDialog(title, "");
	}

	/**
	 * Displays a value dialog using
	 * {@link JOptionPane#showInputDialog(java.awt.Component, java.lang.Object)}.
	 * 
	 * @param title
	 *            The title of the dialog to be displayed.
	 * @param initialValue
	 *            The intitial value to be displayed.
	 * @return Returns the user input.
	 */
	public String valueDialog(String title, String initialValue) {
		return JOptionPane.showInputDialog(title, initialValue);
	}

	/**
	 * Shortcut method to {@link #valueDialog(String, String)} that returns the
	 * user input as an int.
	 * 
	 * @param title
	 *            The title of the dialog to be displayed.
	 * @param initialValue
	 *            The initial value to be displayed.
	 * @param allowNegative
	 *            Specifies whether negative values are allowed as input.
	 * @param allowZero
	 *            Specifies whether zero is a valid input.
	 * @return Returns the user input as an int.
	 * @throws IllegalArgumentException
	 *             If a value <= 0 is entered.
	 * @throws CancelException
	 *             if the user clicks cancel
	 */
	public int intDialog(String title, int initialValue, boolean allowNegative,
			boolean allowZero) {
		String value = valueDialog(title, String.valueOf(initialValue));
		if (value != null && value.length() > 0) {
			int tmp = Integer.parseInt(value);
			if ((!allowNegative && tmp < 0) || (!allowZero && tmp == 0))
				throw new IllegalArgumentException(JGraphEditorResources
						.getString("InvalidValue"));
			initialValue = tmp;
		} else if (value == null)
			throw new CancelException();
		return initialValue;
	}

	/**
	 * Shortcut method to {@link #valueDialog(String, String)} that returns the
	 * user input as a float.
	 * 
	 * @param title
	 *            The title of the dialog to be displayed.
	 * @param initialValue
	 *            The initial value to be displayed.
	 * @param allowNegative
	 *            Specifies whether negative values are allowed as input.
	 * @param allowZero
	 *            Specifies whether zero is a valid input.
	 * @return Returns the user input as a double.
	 * @throws IllegalArgumentException
	 *             If a value <= 0 is entered.
	 * @throws CancelException
	 *             if the user clicks cancel
	 */
	public float floatDialog(String title, float initialValue,
			boolean allowNegative, boolean allowZero) {
		String value = valueDialog(title, String.valueOf(initialValue));
		if (value != null && value.length() > 0) {
			float tmp = Float.parseFloat(value);
			if ((!allowNegative && tmp < 0) || (!allowZero && tmp == 0))
				throw new IllegalArgumentException(JGraphEditorResources
						.getString("InvalidValue"));
			initialValue = tmp;
		} else if (value == null)
			throw new CancelException();
		return initialValue;
	}

	/**
	 * Shortcut method to {@link #valueDialog(String, String)} that returns the
	 * user input as a double.
	 * 
	 * @param title
	 *            The title of the dialog to be displayed.
	 * @param initialValue
	 *            The initial value to be displayed.
	 * @return Returns the user input as a double.
	 * @throws IllegalArgumentException
	 *             If a value <= 0 is entered.
	 * @throws CancelException
	 *             if the user clicks cancel
	 */
	public double doubleDialog(String title, double initialValue,
			boolean allowNegative, boolean allowZero) {
		String value = valueDialog(title, String.valueOf(initialValue));
		if (value != null && value.length() > 0) {
			double tmp = Double.parseDouble(value);
			if ((!allowNegative && tmp < 0) || (!allowZero && tmp == 0))
				throw new IllegalArgumentException(JGraphEditorResources
						.getString("InvalidValue"));
			initialValue = tmp;
		} else if (value == null)
			throw new CancelException();
		return initialValue;
	}

	/**
	 * Displays a confirmation dialog using
	 * {@link JOptionPane#showConfirmDialog(java.awt.Component, java.lang.Object, java.lang.String, int)}
	 * and {@link JGraphpad#APPTITLE} for the title.
	 * 
	 * @param component
	 *            The parent component for the dialog to be displayed.
	 * @param message
	 *            The message to be confirmed.
	 * @param yesNo
	 *            Whether to display yes/no or ok/cancel options.
	 * @param cancel
	 *            Whether to display a cancel option for yes/no dialogs.
	 * @return Returns true if the message is confirmed.
	 * @throws CancelException
	 *             If the user clicks cancel.
	 */
	public boolean confirmDialog(Component component, String message,
			boolean yesNo, boolean cancel) throws CancelException {
		int optionType = (yesNo) ? (cancel) ? JOptionPane.YES_NO_CANCEL_OPTION
				: JOptionPane.YES_NO_OPTION : JOptionPane.OK_CANCEL_OPTION;
		int confirm = (yesNo) ? JOptionPane.YES_OPTION : JOptionPane.OK_OPTION;
		int result = JOptionPane.showConfirmDialog(component, message,
				JGraphpad.APPTITLE, optionType);
		if (result == JOptionPane.CANCEL_OPTION)
			throw new CancelException();
		return result == confirm;
	}

	/**
	 * Shortcut method to {@link #messageDialog(Component, String, int)} that
	 * display a dialog of type {@link JOptionPane#INFORMATION_MESSAGE}.
	 * 
	 * @param component
	 *            The parent component for the dialog to be displayed.
	 * @param message
	 *            The message to be displayed.
	 */
	public void informationDialog(Component component, String message) {
		messageDialog(component, message, JOptionPane.INFORMATION_MESSAGE);
	}

	/**
	 * Shortcut method to {@link #messageDialog(Component, String, int)} that
	 * display a dialog of type {@link JOptionPane#ERROR_MESSAGE}.
	 * 
	 * @param component
	 *            The parent component for the dialog to be displayed.
	 * @param message
	 *            The message to be displayed.
	 */
	public void errorDialog(Component component, String message) {
		messageDialog(component, message, JOptionPane.ERROR_MESSAGE);
	}

	/**
	 * Displays a message dialog using
	 * {@link JOptionPane#showMessageDialog(java.awt.Component, java.lang.Object, java.lang.String, int)}
	 * and {@link JGraphpad#APPTITLE} for the title.
	 * 
	 * @param component
	 *            The parent component for the dialog to be displayed.
	 * @param message
	 *            The message to be confirmed.
	 * @param type
	 *            The type of message dialog to be displayed.
	 */
	public void messageDialog(Component component, String message, int type) {
		JOptionPane.showMessageDialog(component, message, JGraphpad.APPTITLE,
				type);
	}

	/**
	 * Displays a {@link JFileChooser} using
	 * {@link #showFileChooser(Component, JFileChooser, boolean)} for files with
	 * the specified extension. The dialog will show <code>description</code>
	 * for file of this type. The full extension including the dot must be
	 * specified. If the selected filename does not end with
	 * <code>extension</code> then the extension is appended to the filename.
	 * 
	 * @param component
	 *            The parent component for the dialog to be displayed.
	 * @param title
	 *            The title of the dialog to be displayed.
	 * @param open
	 *            Whether to display an open or save dialog.
	 * @param extension
	 *            The extension to be used for filtering files.
	 * @param desc
	 *            The description of the file format.
	 * @param directory
	 *            The default directory to use for the dialog.
	 * @return Returns the selected filename.
	 */
	public String fileDialog(Component component, String title, boolean open,
			String extension, String desc, File directory) {
		JFileChooser fc = new JFileChooser(directory);
		fc.setDialogTitle(title);
		if (extension != null)
			fc.setFileFilter(new JGraphpadFileFilter(extension.toLowerCase(),
					desc));
		return showFileChooser(component, fc, open);
	}

	/**
	 * Displays a {@link JFileChooser} using
	 * {@link #showFileChooser(Component, JFileChooser, boolean)}. This
	 * implementation adds two file filters to the chooser, one for .xml files
	 * and the other for .xml.gz files.
	 * 
	 * @param component
	 *            The parent component for the dialog to be displayed.
	 * @param title
	 *            The title of the dialog to be displayed.
	 * @param open
	 *            Whether to display an open or save dialog.
	 * @param directory
	 *            The default directory to use for the dialog.
	 * @return Returns the selected filename.
	 */
	public String editorFileDialog(Component component, String title,
			String filename, boolean open, File directory) {
		JFileChooser chooser = (open) ? openEditorChooser : saveEditorChooser;
		if (chooser != null) {
			chooser.setDialogTitle(title);
			// Uses the passed-in directory or filename to set current dir
			if (filename != null && !JGraphEditor.isURL(filename)) {
				File file = new File(filename);
				directory = file.getParentFile();
				chooser.setSelectedFile(file);
			}
			chooser.setCurrentDirectory(directory);
			return showFileChooser(component, chooser, open);
		}
		return null;
	}

	/**
	 * Displays a {@link JFileChooser} using
	 * {@link #showFileChooser(Component, JFileChooser, boolean)}. This
	 * implementation adds two file filters to the chooser, one for .jpg files
	 * and the other for .png files.
	 * 
	 * @param component
	 *            The parent component for the dialog to be displayed.
	 * @param title
	 *            The title of the dialog to be displayed.
	 * @param open
	 *            Whether to display an open or save dialog.
	 * @param directory
	 *            The default directory to use for the dialog.
	 * @return Returns the selected filename.
	 */
	public String imageFileDialog(Component component, String title,
			boolean open, File directory) {
		JFileChooser chooser = (open) ? openImageChooser : saveImageChooser;
		if (chooser != null) {
			chooser.setDialogTitle(title);
			if (directory != null)
				chooser.setCurrentDirectory(directory);
			return showFileChooser(component, chooser, open);
		}
		return null;
	}

	/**
	 * Helper method to display the specified chooser using
	 * {@link JFileChooser#showOpenDialog(java.awt.Component)} or
	 * {@link JFileChooser#showSaveDialog(java.awt.Component)} making sure that
	 * the returned file has the extension of the selected filter. This
	 * implementation displays a confirmation dialog if a file will be
	 * overwritten and returns null if the user chooses not to overwrite it.
	 * 
	 * @param component
	 *            The parent component for the dialog to be displayed.
	 * @param chooser
	 *            The dialog to be displayed. Whether to display an open or save
	 *            dialog.
	 * @param open
	 *            Whether to display an open or save dialog.
	 * @return Returns the selected filename.
	 */
	protected String showFileChooser(Component component, JFileChooser chooser,
			boolean open) {
		int returnValue = JFileChooser.CANCEL_OPTION;
		if (open)
			returnValue = chooser.showOpenDialog(component);
		else
			returnValue = chooser.showSaveDialog(component);
		if (returnValue == JFileChooser.APPROVE_OPTION
				&& chooser.getSelectedFile() != null) {
			String filename = chooser.getSelectedFile().getAbsolutePath();
			FileFilter tmp = chooser.getFileFilter();
			if (tmp instanceof JGraphpadFileFilter) {
				JGraphpadFileFilter filter = (JGraphpadFileFilter) tmp;
				String ext = filter.getExtension().toLowerCase();
				if (!open && !filename.toLowerCase().endsWith(ext))
					filename += ext;
			}
			if (open
					|| !new File(filename).exists()
					|| confirmDialog(component, JGraphEditorResources
							.getString("OverwriteExistingFile"), true, false))
				return filename;
		}
		return null;
	}

	public static class CancelException extends RuntimeException {
	}

	/**
	 * A simple font dialog.
	 */
	public static class FontDialog extends JGraphpadDialog {

		/**
		 * Holds the selection font.
		 */
		protected Font font;

		/**
		 * Constructs a new font panel.
		 */
		public FontDialog() {
			super(JGraphEditorResources.getString("SelectFont"));
			setModal(true);
			pack();
			JGraphpad.center(this);
		}

		/**
		 * Creates the content panel.
		 */
		protected JComponent createContent() {
			JPanel panel = new JPanel(new FlowLayout());
			GraphicsEnvironment gEnv = GraphicsEnvironment
					.getLocalGraphicsEnvironment();
			String[] fontNames = gEnv.getAvailableFontFamilyNames();

			final JTextField sizeField = new JTextField("12");
			Dimension dim = sizeField.getPreferredSize();
			dim.width = 30;
			sizeField.setMinimumSize(dim);
			sizeField.setPreferredSize(dim);
			final JComboBox combo = new JComboBox(fontNames);

			final ActionListener listener = new ActionListener() {

				/**
				 * Updates the font field on selection.
				 */
				public void actionPerformed(ActionEvent e) {
					if (sizeField.getText().length() > 0) {
						try {
							setFont(new Font(String.valueOf(combo
									.getSelectedItem()), 0, Integer
									.parseInt(sizeField.getText())));
						} catch (Exception ex) {
							sizeField.setText("");
						}
					}
				}
			};

			// Keeps font variable up-to-date
			combo.addActionListener(listener);
			sizeField.addActionListener(listener);

			// Adds components to panel
			panel.add(combo);
			panel.add(sizeField);

			// Adds OK button to close window
			JButton okButton = new JButton(JGraphEditorResources
					.getString("OK"));
			getRootPane().setDefaultButton(okButton);

			addButtons(new JButton[] { okButton });
			okButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					listener.actionPerformed(e);
					setVisible(false);
				}
			});

			return panel;
		}

		/**
		 * @return Returns the font.
		 */
		public Font getFont() {
			return font;
		}

		/**
		 * @param font
		 *            The font to set.
		 */
		public void setFont(Font font) {
			this.font = font;
		}

	}

}
