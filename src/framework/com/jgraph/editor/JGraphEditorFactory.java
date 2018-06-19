/* 
 * $Id: JGraphEditorFactory.java,v 1.3 2006/02/02 14:13:07 gaudenz Exp $
 * Copyright (c) 2001-2005, Gaudenz Alder
 * 
 * All rights reserved.
 * 
 * See LICENSE file for license details. If you are unable to locate
 * this file please contact info (at) jgraph (dot) com.
 */
package com.jgraph.editor;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Hashtable;
import java.util.Map;

import javax.accessibility.AccessibleContext;
import javax.accessibility.AccessibleState;
import javax.swing.AbstractButton;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;

import org.jgraph.JGraph;
import org.jgraph.graph.GraphLayoutCache;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.jgraph.editor.factory.JGraphEditorDiagramPane;
import com.jgraph.editor.factory.JGraphEditorFactoryMethod;
import com.jgraph.editor.factory.JGraphEditorToolbox;

/**
 * Class that creates the UI elements of a JGraph editor. It uses the actions
 * and tools from the {@link #kit} to create menu bars, toolbars, toolboxes and
 * popup menus.
 */
public class JGraphEditorFactory {

	/**
	 * Defines the nodename for separators.
	 */
	public static final String NODENAME_SEPARATOR = "separator";

	/**
	 * Defines the nodename for items.
	 */
	public static final String NODENAME_ITEM = "item";

	/**
	 * Defines the nodename for menus.
	 */
	public static final String NODENAME_MENU = "menu";

	/**
	 * Defines the nodename for groups.
	 */
	public static final String NODENAME_GROUP = "group";

	/**
	 * Defines the suffix for actionname resources. This is used to replace the
	 * default action name, which is its key, eg.
	 * <code>openFile.label=Open...</code>.
	 */
	public static final String SUFFIX_ACTION = ".action";

	/**
	 * Defines the suffix for toolname resources. This is used to replace the
	 * default tool name, which is its key, eg.
	 * <code>openFile.label=Open...</code>.
	 */
	public static final String SUFFIX_TOOL = ".tool";

	/**
	 * Defines the suffix for label resources, eg.
	 * <code>open.label=Open...</code>.
	 */
	public static final String SUFFIX_LABEL = ".label";

	/**
	 * Defines the suffix for icon resources, eg.
	 * <code>open.icon=/com/jgraph/pad/images/open.gif</code>.
	 */
	public static final String SUFFIX_ICON = ".icon";

	/**
	 * Defines the suffix for mnemonic resources, eg.
	 * <code>open.mnemonic=o</code>.
	 */
	public static final String SUFFIX_MNEMONIC = ".mnemonic";

	/**
	 * Defines the suffix for shortcut resources, eg.
	 * <code>open.shortcut=control O</code>.
	 */
	public static final String SUFFIX_SHORTCUT = ".shortcut";

	/**
	 * Defines the suffix for tooltip resources, eg.
	 * <code>open.tooltip=Open a file</code>.
	 */
	public static final String SUFFIX_TOOLTIP = ".tooltip";

	/**
	 * Constant for menubar creation.
	 */
	protected static final int ITEMTYPE_MENUBAR = 0;

	/**
	 * Constant for toolbar creation.
	 */
	protected static final int ITEMTYPE_TOOLBAR = 1;

	/**
	 * Shared separator instance.
	 */
	protected static final Component SEPARATOR = new JButton();

	/**
	 * Holds the (name, factory method) pairs
	 */
	protected Map factoryMethods = new Hashtable();

	/**
	 * References the editor kit.
	 */
	protected JGraphEditorKit kit;

	/**
	 * Constructs an empty factory.
	 */
	public JGraphEditorFactory() {
		// empty
	}

	/**
	 * Constructs a factory for the specified kit.
	 */
	public JGraphEditorFactory(JGraphEditorKit kit) {
		setKit(kit);
	}

	/**
	 * Returns the editor kit.
	 * 
	 * @return Returns the kit.
	 */
	public JGraphEditorKit getKit() {
		return kit;
	}

	/**
	 * Sets the editor kit to provide actions and tools.
	 * 
	 * @param kit
	 *            The kit to set.
	 */
	public void setKit(JGraphEditorKit kit) {
		this.kit = kit;
	}

	/**
	 * Adds the specified factory method.
	 * 
	 * @param method
	 *            The factory method to add.
	 * @return Returns the previous factory method for <code>name</code>.
	 */
	public Object addMethod(JGraphEditorFactoryMethod method) {
		if (method != null)
			return factoryMethods.put(method.getName(), method);
		return null;
	}

	/**
	 * Returns the factory method for the specified name or <code>null</code>
	 * if no such factory method can be found.
	 * 
	 * @param name
	 *            The name that identifies the factory method.
	 * @return Returns the factory method under <code>name</code> or
	 *         <code>null</code>.
	 */
	public JGraphEditorFactoryMethod getMethod(String name) {
		if (name != null)
			return (JGraphEditorFactoryMethod) factoryMethods.get(name);
		return null;
	}

	/**
	 * Shortcut method to {@link #executeMethod(String, Node)} with a null
	 * configuration.
	 * 
	 * @param factoryMethod
	 *            The name of the factory method to executed.
	 * @return Returns the return value of
	 *         {@link JGraphEditorFactoryMethod#createInstance(Node)} on
	 *         <code>factoryMethod</code>.
	 */
	public Component executeMethod(String factoryMethod) {
		return executeMethod(factoryMethod, null);
	}

	/**
	 * Executes {@link JGraphEditorFactoryMethod#createInstance(Node)} on the
	 * factory method under <code>factoryMethod</code> passing
	 * <code>configuration</code> along as an argument and returns the return
	 * value of the invoked method.
	 * 
	 * @param factoryMethod
	 *            The name of the factory method to executed.
	 * @param configuration
	 *            The configuration to pass to
	 *            {@link JGraphEditorFactoryMethod#createInstance(Node)}.
	 * @return Returns the return value of
	 *         {@link JGraphEditorFactoryMethod#createInstance(Node)} on
	 *         <code>factoryMethod</code>.
	 * 
	 * @see #getMethod(String)
	 */
	public Component executeMethod(String factoryMethod, Node configuration) {
		JGraphEditorFactoryMethod method = getMethod(factoryMethod);
		if (method != null)
			return method.createInstance(configuration);
		return null;
	}

	/**
	 * Shortcut method to {@link JGraphEditorResources#getString(String)}.
	 * 
	 * @return Returns the resource string for <code>key</code>.
	 */
	protected String getString(String key) {
		return JGraphEditorResources.getString(key);
	}

	/**
	 * Shortcut method to
	 * {@link JGraphEditorSettings#getKeyAttributeValue(Node)}.
	 */
	protected String getKey(Node node) {
		return JGraphEditorSettings.getKeyAttributeValue(node);
	}

	/**
	 * Returns the action for the resource under <code>key+SUFFIX_ACTION</code>
	 * from the editor kit. If no such resource exists then the action for
	 * <code>key</code> is returned or <code>null</code> if no action can be
	 * found.
	 * 
	 * @return Returns the action for the <code>key+SUFFIX_ACTION</code>
	 *         resource or the action for <code>key</code>.
	 * 
	 * @see #getString(String)
	 * @see JGraphEditorKit#getAction(String)
	 */
	public JGraphEditorAction getAction(String key) {
		String tmp = getString(key + SUFFIX_ACTION);
		if (tmp != null)
			key = tmp;
		return getKit().getAction(key);
	}

	/**
	 * Returns the tool for the resource under <code>key+SUFFIX_TOOL</code>
	 * from the editor kit. If no such resource exists then the tool for
	 * <code>key</code> is returned or <code>null</code> if no tool can be
	 * found.
	 * 
	 * @return Returns the tool for the <code>key+SUFFIX_TOOL</code> resource
	 *         or the tool for <code>key</code>.
	 * 
	 * @see #getString(String)
	 * @see JGraphEditorKit#getTool(String)
	 */
	public JGraphEditorTool getTool(String key) {
		String tmp = getString(key + SUFFIX_TOOL);
		if (tmp != null)
			key = tmp;
		return getKit().getTool(key);
	}

	//
	// General Items
	//

	/**
	 * Returns a new diagram pane for the specified diagram by creating a new
	 * graph using the {@link #createGraph(GraphLayoutCache)} method and
	 * wrapping it up in a {@link JGraphEditorDiagramPane}.
	 * 
	 * @param diagram
	 *            The diagram that contains the graph to be used.
	 * @return Returns a new diagram pane for <code>diagram</code>.
	 */
	public JGraphEditorDiagramPane createDiagramPane(JGraphEditorDiagram diagram) {
		JGraph graph = createGraph(diagram.getGraphLayoutCache());
		JGraphEditorDiagramPane pane = new JGraphEditorDiagramPane(diagram,
				graph);
		return pane;
	}

	/**
	 * Returns a new graph for the specified <code>cache</code>. This
	 * implementation creates a new graph using
	 * {@link JGraph#JGraph(GraphLayoutCache)}.
	 * 
	 * @param cache
	 *            The layout cache that defines the graph.
	 * @return Returns a new graph for <code>cache</code>.
	 */
	public JGraph createGraph(GraphLayoutCache cache) {
		return new JGraph(cache);
	}

	/**
	 * Returns a new {@link JScrollPane} containing the specified component.
	 * 
	 * @return Returns a new scrollpane containing the specified component.
	 */
	public JScrollPane createScrollPane(Component component) {
		JScrollPane scrollPane = new JScrollPane(component);
		scrollPane.setFocusable(false);
		scrollPane.getHorizontalScrollBar().setFocusable(false);
		scrollPane.getVerticalScrollBar().setFocusable(false);
		return scrollPane;
	}

	/**
	 * Returns a new {@link JSplitPane} containing the specified component with
	 * the specified orientation.
	 * 
	 * @return Returns a new splitpane containing the the specified components.
	 */
	public JSplitPane createSplitPane(Component first, Component second,
			int orientation) {
		JSplitPane splitPane = new JSplitPane(orientation, first, second);
		splitPane.setBorder(null);
		splitPane.setFocusable(false);
		return splitPane;
	}

	/**
	 * Returns a new empty {@link JTabbedPane}.
	 * 
	 * @return Returns a new tabbed pane.
	 */
	public JTabbedPane createTabbedPane(int tabPlacement) {
		JTabbedPane tabPane = new JTabbedPane(tabPlacement);
		tabPane.setFocusable(false);
		return tabPane;
	}

	//
	// Toolbox
	//

	/**
	 * Returns a new {@link JGraphEditorToolbox} configured using
	 * {@link #configureToolbox(JGraphEditorToolbox, Node)}.
	 * 
	 * @param configuration
	 *            The configuration to create the toolbox with.
	 * @return Returns a new toolbox.
	 */
	public JGraphEditorToolbox createToolbox(Node configuration) {
		JGraphEditorToolbox toolbox = new JGraphEditorToolbox();
		configureToolbox(toolbox, configuration);
		return toolbox;
	}

	/**
	 * Hook for subclassers to configure a new toolbox based on
	 * <code>configuration</code>.
	 * 
	 * @param toolbox
	 *            The toolbox to be configured.
	 * @param configuration
	 *            The configuration to configure the toolbox with.
	 * 
	 * @see #createToolboxButton(JGraphEditorTool)
	 */
	protected void configureToolbox(final JGraphEditorToolbox toolbox,
			Node configuration) {
		if (configuration != null) {
			ButtonGroup group = new ButtonGroup();
			NodeList nodes = configuration.getChildNodes();
			for (int i = 0; i < nodes.getLength(); i++) {
				Node child = nodes.item(i);
				String key = getKey(child);

				final JGraphEditorTool tool = getTool(key);
				if (tool != null) {
					AbstractButton button = createToolboxButton(tool);
					group.add(button);
					toolbox.add(button);

					// Selects the first button in the toolbox
					if (toolbox.getComponentCount() == 1) {
						button.setSelected(true);
						toolbox.setSelectionTool(tool);
						toolbox.setDefaultButton(button);
					}

					// Update the selection tool in the enclosing
					// toolbox when the button is selected.
					button.getAccessibleContext().addPropertyChangeListener(
							new PropertyChangeListener() {
								public void propertyChange(
										PropertyChangeEvent evt) {
									if (evt
											.getPropertyName()
											.equals(
													AccessibleContext.ACCESSIBLE_STATE_PROPERTY)
											&& evt.getNewValue() != null
											&& evt.getNewValue().equals(
													AccessibleState.SELECTED)) {
										toolbox.setSelectionTool(tool);
									}
								}
							});

				} else if (key != null) {

					// Tries to execute factory method and add result.
					Component c = executeMethod(key, configuration);
					if (c != null) {
						toolbox.add(c);
						toolbox.addSeparator(new Dimension(3, 0));
					}
				} else if (child.getNodeName().equals(NODENAME_SEPARATOR)) {
					toolbox.addSeparator();
				}
			}
		}
	}

	/**
	 * Returns a new {@link JToggleButton} button configured for
	 * <code>tool</code> by calling
	 * {@link #configureAbstractButton(AbstractButton, String)}.
	 * 
	 * @param tool
	 *            The tool to create the toolbox button for.
	 * @return Returns a new toolbox.
	 */
	protected AbstractButton createToolboxButton(JGraphEditorTool tool) {
		AbstractButton button = new JToggleButton();
		configureAbstractButton(button, tool.getName());
		return button;
	}

	/**
	 * Hook for subclassers to configure a toolbox button for <code>tool</code>.
	 * 
	 * @param button
	 *            The button to be configured.
	 * @param name
	 *            The name of the tool or action to configure the button for.
	 */
	protected void configureAbstractButton(AbstractButton button, String name) {
		button.setFocusable(false);
		button.setText("");

		// Configures the tooltip
		String tip = getString(name + SUFFIX_TOOLTIP);
		if (tip == null)
			tip = getString(name + SUFFIX_LABEL);
		button.setToolTipText((tip != null) ? tip : name);

		// Configures the icon and size
		ImageIcon icon = JGraphEditorResources.getImage(getString(name
				+ SUFFIX_ICON));
		if (icon != null) {
			button.setIcon(icon);
			Dimension d = new Dimension(icon.getIconWidth() + 8, icon
					.getIconHeight() + 10);
			button.setMaximumSize(d);
			button.setPreferredSize(d);
		}
	}

	//
	// Menus
	//

	/**
	 * Returns a new {@link JMenuBar} configured using
	 * {@link #configureMenuBar(Container, Node)}.
	 * 
	 * @param configuration
	 *            The configuration to create the menubar with.
	 * @return Returns a new toolbox.
	 */
	public JMenuBar createMenuBar(Node configuration) {
		JMenuBar menuBar = new JMenuBar();
		configureMenuBar(menuBar, configuration);
		return menuBar;
	}

	/**
	 * Returns a new {@link JPopupMenu} configured using
	 * {@link #configureMenuBar(Container, Node)}.
	 * 
	 * @param configuration
	 *            The configuration to create the popup menu with.
	 * @return Returns a new toolbox.
	 */
	public JPopupMenu createPopupMenu(Node configuration) {
		JPopupMenu menuBar = new JPopupMenu();
		configureMenuBar(menuBar, configuration);
		return menuBar;
	}

	/**
	 * Hook for subclassers to configure a new menu based on
	 * <code>configuration</code>. This is used for menubars and submenus.
	 * 
	 * @param menu
	 *            The menu to be configured.
	 * @param configuration
	 *            The configuration to configure the menubar with.
	 * 
	 * @see #createMenuItem(Node, boolean)
	 */
	protected void configureMenuBar(Container menu, Node configuration) {
		if (configuration != null) {
			String key = getKey(configuration);
			if (menu instanceof JMenu) {
				JMenu tmp = (JMenu) menu;
				tmp.setText(getString(key + SUFFIX_LABEL));

				// Configures the mnemonic
				String mnemonic = getString(key + SUFFIX_MNEMONIC);
				if (mnemonic != null && mnemonic.length() > 0)
					tmp.setMnemonic(mnemonic.toCharArray()[0]);
			}

			NodeList nodes = configuration.getChildNodes();
			for (int i = 0; i < nodes.getLength(); i++) {
				Node child = nodes.item(i);
				String name = child.getNodeName();

				Component c = executeMethod(getKey(child), child);
				if (c != null) {
					menu.add(c);
				} else if (name.equals(NODENAME_MENU)) {

					// Creates and adds a submenu
					Container subMenu = createMenu(child);
					configureMenuBar(subMenu, child); // recurse
					menu.add(subMenu);

				} else if (name.equals(NODENAME_GROUP)) {

					// Creates a button group
					ButtonGroup group = new ButtonGroup();
					NodeList groupNodes = child.getChildNodes();
					for (int j = 0; j < groupNodes.getLength(); j++) {
						Node groupChild = groupNodes.item(j);
						if (groupChild.getNodeName().equals(NODENAME_ITEM)) {
							AbstractButton button = createMenuItem(groupChild,
									true);
							if (button != null) {
								menu.add(button);
								group.add(button);
							}
						}
					}

					// Create Item
				} else if (name.equals(NODENAME_ITEM)) {
					AbstractButton item = createMenuItem(child, false);
					if (item != null)
						menu.add(item);
				} else if (name.equals(NODENAME_SEPARATOR)
						&& menu instanceof JMenu) {
					((JMenu) menu).addSeparator();
				} else if (name.equals(NODENAME_SEPARATOR)
						&& menu instanceof JPopupMenu) {
					((JPopupMenu) menu).addSeparator();
				}
			}
		}
	}

	/**
	 * Hook for subclassers to create a new menu. This implementation returns a
	 * new instance of {@link JMenuBar}.
	 * 
	 * @param configuration
	 *            The configuration to create the menu with.
	 * @return Returns a new menubar.
	 */
	protected Container createMenu(Node configuration) {
		return new JMenu();
	}

	/**
	 * Returns a new {@link JCheckBoxMenuItem} or {@link JMenuItem} configured
	 * using {@link #configureActionItem(AbstractButton, JGraphEditorAction)}.
	 * 
	 * @param configuration
	 *            The configuration to create the menu item with.
	 * @param radio
	 *            Whether the created item should be a
	 *            {@link JRadioButtonMenuItem}.
	 * @return Returns a new menu item.
	 */
	public AbstractButton createMenuItem(Node configuration, boolean radio) {
		JGraphEditorAction action = getAction(getKey(configuration));
		if (action != null) {
			AbstractButton item = (radio) ? new JRadioButtonMenuItem()
					: (action.isToggleAction()) ? new JCheckBoxMenuItem()
							: new JMenuItem();
			configureActionItem(item, action);
			return item;
		}
		return null;
	}

	/**
	 * Hook for subclassers to configure an action item for <code>action</code>.
	 * Valid action items are toolbar buttons and menu items, but not toolbox
	 * buttons.
	 * 
	 * @param button
	 *            The button to be configured.
	 * @param action
	 *            The action to configure the button for.
	 */
	public void configureActionItem(AbstractButton button,
			JGraphEditorAction action) {
		String name = action.getName();
		button.setFocusable(false);
		button.setAction(action);
		button.setEnabled(action.isEnabled());
		button.setSelected(action.isSelected());

		// Listens to changes of the action state and upates the button
		action.addPropertyChangeListener(createActionChangeListener(button));

		// Configures the label
		String label = getString(name + SUFFIX_LABEL);
		button.setText((label != null && label.length() > 0) ? label : name);

		// Configures the icon
		ImageIcon icon = JGraphEditorResources.getImage(getString(name
				+ SUFFIX_ICON));
		if (icon != null)
			button.setIcon(icon);

		// Configures the mnemonic
		String mnemonic = getString(name + SUFFIX_MNEMONIC);
		if (mnemonic != null && mnemonic.length() > 0)
			button.setMnemonic(mnemonic.toCharArray()[0]);

		// Configures the tooltip
		String tooltip = getString(name + SUFFIX_TOOLTIP);
		if (tooltip != null)
			button.setToolTipText(tooltip);

		// Configures the shortcut aka. accelerator
		String shortcut = getString(name + SUFFIX_SHORTCUT);
		if (shortcut != null && button instanceof JMenuItem)
			((JMenuItem) button).setAccelerator(KeyStroke
					.getKeyStroke(shortcut));
	}

	//
	// Toolbar
	//

	/**
	 * Returns a new {@link JToolBar} configured using
	 * {@link #configureToolBar(Container, Node)}.
	 * 
	 * @param configuration
	 *            The configuration to create the toolbar with.
	 * @return Returns a new toolbar.
	 */
	public JToolBar createToolBar(Node configuration) {
		JToolBar toolBar = new JToolBar();
		configureToolBar(toolBar, configuration);
		return toolBar;

	}

	/**
	 * Hook for subclassers to configure a toolbar with
	 * <code>configuration</code>.
	 * 
	 * @param toolBar
	 *            The toolBar to be configured.
	 * @param configuration
	 *            The configuration to configure the toolbar with.
	 */
	protected void configureToolBar(Container toolBar, Node configuration) {
		if (configuration != null) {
			NodeList nodes = configuration.getChildNodes();
			for (int i = 0; i < nodes.getLength(); i++) {
				Node child = nodes.item(i);
				String name = child.getNodeName();

				Component c = executeMethod(getKey(child), child);
				if (c != null) {
					toolBar.add(c);
				} else if (name.equals(NODENAME_ITEM)) {
					AbstractButton button = createToolBarButton(child);
					if (button != null)
						toolBar.add(button);
				} else if (name.equals(NODENAME_SEPARATOR)
						&& toolBar instanceof JToolBar) {
					((JToolBar) toolBar).addSeparator();
				}
			}
		}
	}

	/**
	 * Returns a new {@link JToggleButton} or {@link JButton} configured using
	 * {@link #configureActionItem(AbstractButton, JGraphEditorAction)} and
	 * {@link #configureAbstractButton(AbstractButton, String)} (in this order).
	 * 
	 * @param configuration
	 *            The configuration to create the toolbar with.
	 * @return Returns a new toolbar.
	 */
	protected AbstractButton createToolBarButton(Node configuration) {
		JGraphEditorAction action = getAction(getKey(configuration));
		if (action != null) {
			AbstractButton button = null;
			if (action.isToggleAction())
				button = new JToggleButton();
			else
				button = new JButton();
			configureActionItem(button, action);
			configureAbstractButton(button, action.getName());
			return button;
		}
		return null;
	}

	/**
	 * Returns a new property change listener that updates <code>button</code>
	 * according to property change events.
	 * 
	 * @param button
	 *            The button to create the listener for.
	 * @return Returns a new property change listener for actions.
	 */
	public PropertyChangeListener createActionChangeListener(
			AbstractButton button) {
		return new ActionChangedListener(button);
	}

	/**
	 * Updates <code>button</code> based on property change events.
	 */
	protected class ActionChangedListener implements PropertyChangeListener {

		/**
		 * References the button that is to be updated.
		 */
		AbstractButton button;

		/**
		 * Constructs a action changed listener for the specified button.
		 * 
		 * @param button
		 *            The button to create the listener for.
		 */
		ActionChangedListener(AbstractButton button) {
			this.button = button;
		}

		/**
		 * Updates the button state based on changes of the action state.
		 */
		public void propertyChange(PropertyChangeEvent e) {
			String propertyName = e.getPropertyName();
			if (propertyName.equals("enabled")) {
				Boolean enabledState = (Boolean) e.getNewValue();
				button.setEnabled(enabledState.booleanValue());
			} else if (propertyName
					.equals(JGraphEditorAction.PROPERTY_ISSELECTED)) {
				Boolean selectedState = (Boolean) e.getNewValue();
				if (selectedState != null) {
					boolean selected = selectedState.booleanValue();
					if (button instanceof JCheckBoxMenuItem)
						((JCheckBoxMenuItem) button).setState(selected);
					else if (button instanceof JRadioButtonMenuItem)
						((JRadioButtonMenuItem) button).setSelected(selected);
					else if (button instanceof JToggleButton)
						((JToggleButton) button).setSelected(selected);
				}
			}
		}
	}

}
