package org.rsbot.gui;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;

import javax.swing.ButtonGroup;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextPane;
import javax.swing.JTree;
import javax.swing.WindowConstants;
import javax.swing.JToggleButton.ToggleButtonModel;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.Element;
import javax.swing.text.PlainDocument;
import javax.swing.text.StyleConstants;
import javax.swing.text.html.HTML;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import org.rsbot.script.Script;
import org.rsbot.script.ScriptHandler;
import org.rsbot.script.ScriptManifest;
import org.rsbot.util.GlobalConfiguration;
import org.rsbot.util.GlobalFile;
import org.rsbot.util.ScriptClassLoader;

/**
 * This will handle the selection process of scripts.<br>
 * Fully implemented searching using MergeSort and various parameters.<br>
 *
 * @author Fusion89k
 */
public class ScriptSelector extends JDialog implements ActionListener, TreeSelectionListener {

    /**
     * The different parameters to search by. Information loaded from
     * ScriptManifest
     */
    private enum TYPE {
        AUTHOR, CATEGORY, VERSION, NAME
    }

    private static final long serialVersionUID = 5475451138208522510L;

    /**
     * Enables singleton
     *
     * @param gui BotGUI from which to run the script
     * @return Single instance of ScriptSelector
     */
    public static ScriptSelector getInstance(final BotGUI gui) {
        if (ScriptSelector.instance == null) {
            ScriptSelector.instance = new ScriptSelector(gui);
        }
        return ScriptSelector.instance;
    }

    /**
     * Loads all of the scripts along with their manifests
     *
     * @return a mapping of the script names and manifests
     */
    public static LinkedHashMap<String, ScriptManifest> loadScripts() {
        final LinkedHashMap<String, ScriptManifest> scripts = new LinkedHashMap<String, ScriptManifest>();
        final ArrayList<String> paths = new ArrayList<String>(2);
        if (!GlobalConfiguration.RUNNING_FROM_JAR) {
            final String rel = "." + File.separator + GlobalConfiguration.Paths.SCRIPTS_NAME_SRC;
            paths.add(rel);
        } else {
            // Generate the path of the scripts folder in the jar
            final URL version = GlobalConfiguration.class.getClassLoader().getResource(GlobalConfiguration.Paths.Resources.VERSION);
            String p = version.toString().replace("jar:file:", "").replace(GlobalConfiguration.Paths.Resources.VERSION, GlobalConfiguration.Paths.Resources.SCRIPTS);
            try {
                p = URLDecoder.decode(p, "UTF-8");
            } catch (final UnsupportedEncodingException ignored) {
            }
            paths.add(p);
        }

        // Add documents script directory
        paths.add(GlobalConfiguration.Paths.getScriptsDirectory());

        // Add documents precompiled script directory
        paths.add(GlobalConfiguration.Paths.getScriptsPrecompiledDirectory());

        // Add all jar files in the precompiled scripts directory
        final File psdir = new GlobalFile(GlobalConfiguration.Paths.getScriptsPrecompiledDirectory());
        if (psdir.exists()) {
            for (final File file : psdir.listFiles()) {
                if (file.getName().endsWith(".jar!")) {
                    paths.add(file.getPath());
                }
            }
        }

        // Loop through paths to find scripts
        for (final String path : paths) {
            final GlobalFile dir = new GlobalFile(path);
            if (!dir.exists() || !dir.isDirectory()) {
                continue;
            }
            
            String url;
            try {
                url = dir.toURI().toURL().toString();
            } catch (final MalformedURLException e) {
                continue;
            }
        	ScriptClassLoader loader = new ScriptClassLoader(url, ScriptClassLoader.class.getClassLoader());
        	load(null, loader, scripts, dir);
        }
        return scripts;
    }
    
    private static void load(String prefix, ScriptClassLoader loader, LinkedHashMap<String, ScriptManifest> scripts, File file) {
    	if (file.isDirectory()) {
    		if (prefix == null) {
				for (File f : file.listFiles()) {
					load("", loader, scripts, f);
		    	}
			} else if (!file.getName().equals("Precompiled")
					&& !file.getName().endsWith(".jar!")
					&& !file.getName().startsWith(".")) {
				System.out.println(file.getName());
				for (File f : file.listFiles()) {
					load(prefix + file.getName() + ".", loader, scripts, f);
	    		}
			}
    	} else {
    		String name = prefix + file.getName();
	        final String ext = ".class";
	        if (name.endsWith(ext) && !scripts.containsKey(name) && !name.startsWith(".") && !name.contains("!") && !name.contains("$")) {
	            try {
	                name = name.substring(0, name.length() - ext.length());
	                final Class<?> cls = loader.loadClass(name);
	                if (cls.isAnnotationPresent(ScriptManifest.class)) {
	                    final ScriptManifest os = scripts.get(name);
	                    final ScriptManifest ns = cls.getAnnotation(ScriptManifest.class);
	                    if ((os != null) && (ns != null) && (os.version() >= ns.version())) {
	                        return;
	                    }

	                    scripts.put(name, cls.getAnnotation(ScriptManifest.class));
	                }
	            } catch (final Exception e) {
	                e.printStackTrace();
	            }
	        }
    	}
    }

    public static void main(final String[] args) {
        new ScriptSelector(null).showSelector();
    }

    private final String defaultText = "<html><body style='padding: 10px; text-align: center;'>" + "This Script Does Not Have A Description</body></html>";
    private LinkedHashMap<String, ScriptManifest> scriptList;
    private JTree tree;
    private final BotGUI gui;
    private JPanel rightPane;
    private JButton okay;
    private JComboBox accounts;
    private JCheckBox searchOpt;
    private JTextPane description;
    private JScrollPane scrollDescription;

    private JScrollPane scroll;

    private JSplitPane splits;

    private static ScriptSelector instance;

    /**
     * Private allows for singleton
     *
     * @param parent BotGUI from which to run the script.
     */
    private ScriptSelector(final BotGUI parent) {
        super(parent, "Script Selector", true);
        gui = parent;
    }

    /**
     * Handles all of the actions that take place.
     */
    public void actionPerformed(final ActionEvent event) {
        if (event.getSource() instanceof JCheckBox) {// Search CheckBox
            if (((JCheckBox) event.getSource()).isSelected()) {
                tree.clearSelection();
                resetRightSide(true, false, null);
            } else {
                tree.setSelectionRow(0);
                resetRightSide(false, false, null);
            }
        } else if (event.getSource() instanceof JRadioButton) {// Sort Parameters
            final JPanel pane = (JPanel) ((JRadioButton) event.getSource()).getParent();
            TYPE t = null;
            boolean asc = false;
            JComboBox box = null;
            Object index = null;
            for (final Component c : pane.getComponents()) {
                if ((c instanceof JRadioButton) && (t == null)) {
                    final JRadioButton r = (JRadioButton) c;
                    if (r.getText().equals("Ascending")) {
                        asc = r.isSelected();
                    } else if (!r.getText().contains("ing") && r.isSelected()) {
                        t = TYPE.valueOf(r.getText().toUpperCase());
                    }
                } else if (c instanceof JComboBox) {
                    box = (JComboBox) c;
                    pane.remove(c);
                }
            }
            final String[] scripts = sort(getScriptsNames(), t, asc);
            if (t.equals(TYPE.CATEGORY) || t.equals(TYPE.AUTHOR)) {
                if (box != null) {
                    index = box.getSelectedItem();
                }
                final String[] types = getMappingOfType(scripts, t).keySet().toArray(new String[0]);
                final String[] all = new String[types.length + 1];
                all[0] = "All";
                System.arraycopy(types, 0, all, 1, types.length);
                box = new JComboBox(all);
                box.setName(t.toString() + (asc ? "A" : "D"));
                box.setSelectedIndex(0);
                box.addActionListener(this);
                if (index == null) {
                    index = box.getSelectedItem();
                }
            }
            if (index == null) {
                remakeTree(scripts, t, asc);
                resetTree();
                resetRightSide(true, false, null);
            } else {
                box.setSelectedItem(index);
                resetRightSide(true, true, box);
            }

        } else if (event.getSource() instanceof JComboBox) {// Filter Parameters
            final JComboBox box = (JComboBox) event.getSource();
            final String name = box.getName();
            final TYPE t = TYPE.valueOf(name.substring(0, name.length() - 1));
            final boolean asc = name.charAt(name.length() - 1) == 'A';
            String[] scripts = sort(getScriptsNames(), t, asc);
            final LinkedHashMap<String, ArrayList<String>> map = getMappingOfType(scripts, t);
            for (final String s : map.keySet()) {
                if (s.equals(box.getSelectedItem())) {
                    scripts = map.get(s).toArray(new String[0]);
                }
            }
            remakeTree(scripts, t, asc);
        } else if (event.getSource() instanceof JButton) {// OK button
            if (getScript() == null) {
                JOptionPane.showMessageDialog(this, "Please select a script!", "Error!", 2);
            } else {
                gui.runScript(accounts.getSelectedItem().toString(), getScript(), getArguments());
                dispose();
            }
        }
    }

    /**
     * Compare function used when sorting
     */
    private int compare(final String one, final String two, final TYPE t) {
        switch (t) {
            case NAME:
                return scriptList.get(one).name().toLowerCase().compareTo(scriptList.get(two).name().toLowerCase());
            case CATEGORY:
                return scriptList.get(one).category().compareTo(scriptList.get(two).category());
            case VERSION:
                return new Double(scriptList.get(one).version()).compareTo(scriptList.get(two).version());
            default:
                return scriptList.get(one).authors()[0].toLowerCase().compareTo(scriptList.get(two).authors()[0].toLowerCase());
        }
    }

    /**
     * Returns the arguments in a HashMap
     *
     * @return HashMap of the arguments
     */
    private Map<String, String> getArguments() {
        final Document doc = description.getDocument();
        final Map<String, String> args = new HashMap<String, String>();
        for (final Element elem : doc.getRootElements()) {
            getArguments(args, elem);
        }
        return args;
    }

    /**
     * Helper method for getArguments()
     */
    private void getArguments(final Map<String, String> args, final Element elem) {
        final int len = elem.getElementCount();
        if (elem.getName().equalsIgnoreCase("input") || elem.getName().equalsIgnoreCase("select")) {
            final AttributeSet as = elem.getAttributes();
            final Object model = as.getAttribute(StyleConstants.ModelAttribute);
            final String name = as.getAttribute(HTML.Attribute.NAME).toString();
            if (model instanceof PlainDocument) {
                final PlainDocument pd = (PlainDocument) model;
                String value = null;
                try {
                    value = pd.getText(0, pd.getLength());
                } catch (final BadLocationException e) {
                    e.printStackTrace();
                }
                args.put(name, value);
            } else if (model instanceof ToggleButtonModel) {
                final ToggleButtonModel buttonModel = (ToggleButtonModel) model;
                if (!args.containsKey(name)) {
                    args.put(name, null);
                }
                if (buttonModel.isSelected()) {
                    args.put(name, as.getAttribute(HTML.Attribute.VALUE).toString());
                }
            } else if (model instanceof DefaultComboBoxModel) {
                args.put(name, ((DefaultComboBoxModel) model).getSelectedItem().toString());
            } else
                throw new Error("Unknown model [" + model.getClass().getName() + "]");
        }
        for (int i = 0; i < len; i++) {
            final Element e = elem.getElement(i);
            getArguments(args, e);
        }
    }

    /**
     * Access the different types of scripts based on the parameters
     *
     * @param scripts Script names array
     * @param t       TYPE to distinguish by
     * @return A mapping of the types and the scripts that are of that type
     */
    private LinkedHashMap<String, ArrayList<String>> getMappingOfType(final String[] scripts, final TYPE t) {
        final LinkedHashMap<String, ArrayList<String>> map = new LinkedHashMap<String, ArrayList<String>>();
        for (String s : scripts) {
            String key = s;
            s = getScriptForName(s);
            switch (t) {
                case CATEGORY:
                    key = scriptList.get(s).category();
                    break;
                case VERSION:
                    key = scriptList.get(s).version() + "";
                    break;
                case AUTHOR:
                    key = scriptList.get(s).authors()[0];
                    break;
            }
            ArrayList<String> temp = map.get(key);
            if (temp != null) {
                temp.add(s);
            } else {
                temp = new ArrayList<String>();
                temp.add(s);
            }
            map.put(key, temp);
        }
        return map;
    }

    /**
     * Will load the and return the selected Script
     *
     * @return Script that is selected
     */
    private Script getScript() {
        final TreePath selectPath = tree.getSelectionPath();
        if (selectPath == null) {
            return null;
        }
        final String name = getScriptForName(selectPath.getLastPathComponent().toString());
        if (scriptList.get(name) == null)
            return null;

        // just try all the loaders
        // TODO store which loader was used to load the script info
        Class<?> newest = null;
        for (final ScriptClassLoader loader : ScriptHandler.getLoaders()) {
            try {
                final Class<?> cls = loader.loadClass(name);
                if (cls.isAnnotationPresent(ScriptManifest.class) && (newest != null)) {
                    final ScriptManifest os = newest.getAnnotation(ScriptManifest.class);
                    final ScriptManifest ns = cls.getAnnotation(ScriptManifest.class);
                    if ((os != null) && (ns != null) && (os.version() >= ns.version())) {
                        continue;
                    }
                }

                newest = cls;
            } catch (final Exception ignored) {
            }
        }

        try {
            if (newest != null) {
                return newest.asSubclass(Script.class).newInstance();
            }
        } catch (final Exception ex) {
        	JOptionPane.showMessageDialog(this, "Script initialization failed.", "Error!", 2);
        	ex.printStackTrace();
        }

        return null;
    }

    /**
     * Will access the ClassFile name of the given annotation name
     *
     * @param name Name as it appears on the ScriptManifest
     * @return Name of the ClassFile
     */
    private String getScriptForName(final String name) {
        if (scriptList.get(name) != null)
            return name;
        for (final String s : scriptList.keySet()) {
            if (scriptList.get(s).name().equals(name))
                return s;
        }
        return null;
    }

    /**
     * Will access the name of the scripts from the ScriptManifest
     *
     * @return Array of all the script Names
     */
    private String[] getScriptsNames() {
        final LinkedList<String> names = new LinkedList<String>();
        for (final String s : scriptList.keySet()) {
            names.add(scriptList.get(s).name());
        }
        return names.toArray(new String[0]);
    }

    /**
     * Helper method for sort().
     */
    private String[] merge(final String[] left, final String[] right, final TYPE t, final boolean asc) {
        final String[] result = new String[left.length + right.length];
        int leftIndex = 0, rightIndex = 0, resultIndex = 0;
        while ((leftIndex < left.length) && (rightIndex < right.length)) {
            if (compare(getScriptForName(left[leftIndex]), getScriptForName(right[rightIndex]), t) * (asc ? 1 : -1) < 0) {
                result[resultIndex] = left[leftIndex];
                leftIndex++;
            } else {
                result[resultIndex] = right[rightIndex];
                rightIndex++;
            }
            resultIndex++;
        }
        String[] rest;
        int restIndex;
        if (leftIndex >= left.length) {
            rest = right;
            restIndex = rightIndex;
        } else {
            rest = left;
            restIndex = leftIndex;
        }
        for (int i = restIndex; i < rest.length; i++) {
            result[resultIndex] = rest[i];
            resultIndex++;
        }
        return result;
    }

    /**
     * Update the JTree information
     */
    private void remakeTree(final String[] scripts, final TYPE t, final boolean asc) {
        final LinkedHashMap<String, ArrayList<String>> list = getMappingOfType(scripts, t);
        if (!t.equals(TYPE.NAME)) {
            final DefaultMutableTreeNode root = new DefaultMutableTreeNode("Root");
            for (final String s : list.keySet()) {
                final DefaultMutableTreeNode top = new DefaultMutableTreeNode(s);
                if (!asc) {
                    for (int i = list.get(s).size() - 1; i >= 0; i--) {
                        top.add(new DefaultMutableTreeNode(scriptList.get(list.get(s).get(i)).name()));
                    }
                } else {
                    for (final String name : list.get(s)) {
                        top.add(new DefaultMutableTreeNode(scriptList.get(name).name()));
                    }
                }
                root.add(top);
            }
            tree = new JTree(root);
        } else {
            tree = new JTree(sort(getScriptsNames(), t, asc));
        }
        tree.addTreeSelectionListener(this);
        resetTree();
    }

    /**
     * Will redisplay the right side of the splitPane. Used when switching
     * between search and display
     *
     * @param buttons True if you want buttons on the pane
     * @param box     True if you want the comboBox on the pane
     * @param cbox    The comboBox to put on the pane or null if no box.
     */
    private void resetRightSide(final boolean buttons, boolean box, JComboBox cbox) {
        final GridBagConstraints c = new GridBagConstraints();
        c.weighty = 1;
        c.weightx = 1;
        c.gridwidth = 3;
        c.fill = GridBagConstraints.BOTH;
        JPanel mainPane = null;
        boolean hasButtons = false, hasBox = false;
        // Determines if we need to reset the buttons or not.
        if (buttons) {
            for (final Component c1 : rightPane.getComponents()) {
                if (c1 instanceof JPanel) {
                    mainPane = (JPanel) c1;
                    for (final Component comp : ((JPanel) c1).getComponents()) {
                        if (comp instanceof JRadioButton) {
                            hasButtons = true;
                        } else if (comp instanceof JComboBox) {
                            hasBox = true;
                        }
                    }
                    break;
                }
            }
        }
        // Doesn't reset the buttons if not needed
        if (hasButtons && buttons) {
            if ((!box && !hasBox) || (box && hasBox))
                return;
        }
        if (buttons) {
            final GridBagConstraints c2 = new GridBagConstraints();
            final JPanel pane = new JPanel(new GridBagLayout());
            if (!hasButtons) {
                // Reset the Buttons
                rightPane.removeAll();
                final JRadioButton asc = new JRadioButton("Ascending"), des = new JRadioButton("Descending"), name = new JRadioButton("Name"), category = new JRadioButton("Category"), version = new JRadioButton("Version"), author = new JRadioButton("Author");
                asc.addActionListener(this);
                des.addActionListener(this);
                name.addActionListener(this);
                category.addActionListener(this);
                version.addActionListener(this);
                author.addActionListener(this);
                final ButtonGroup group1 = new ButtonGroup();
                group1.add(asc);
                group1.add(des);
                final ButtonGroup group2 = new ButtonGroup();
                group2.add(name);
                group2.add(version);
                group2.add(category);
                group2.add(author);
                c2.gridwidth = 2;
                pane.add(asc, c2);
                c2.gridx = GridBagConstraints.RELATIVE;
                pane.add(des, c2);
                c2.gridwidth = 1;
                c2.gridx = 0;
                c2.gridy = 1;
                c2.gridx = GridBagConstraints.RELATIVE;
                pane.add(name, c2);
                pane.add(version, c2);
                pane.add(category, c2);
                pane.add(author, c2);
                asc.setSelected(true);
                category.setSelected(true);
                box = true;
                final String[] most = getMappingOfType(getScriptsNames(), TYPE.CATEGORY).keySet().toArray(new String[0]);
                final String[] all = new String[most.length + 1];
                System.arraycopy(most, 0, all, 1, most.length);
                all[0] = "All";
                cbox = new JComboBox(all);
            }
            if (box && !hasBox) {
                // Add the ComboBox
                // For looks and resizing we use the same panel instead of
                // creating a new one if the buttons are already there
                c2.gridwidth = 2;
                c2.gridx = 1;
                c2.gridy = 2;
                if (mainPane == null) {
                    pane.add(cbox, c2);
                } else {
                    mainPane.add(cbox, c2);
                }
            }
            if (mainPane != null) {
                rightPane.remove(mainPane);
                c.ipady = 230;
                rightPane.add(mainPane, c);
            } else {
                c.ipady = box ? 230 : 255;
                rightPane.add(pane, c);
            }
        } else {
            rightPane.removeAll();
            rightPane.add(scrollDescription, c);
        }
        // Add the CheckBox, ComboBox, and Button at the bottom
        c.fill = GridBagConstraints.NONE;
        c.ipady = 0;
        c.gridwidth = 1;
        c.gridy = 2;
        c.gridx = GridBagConstraints.RELATIVE;
        c.insets = new Insets(5, 5, 5, 5);
        rightPane.add(searchOpt, c);
        c.ipadx = 50;
        rightPane.add(accounts, c);
        c.ipadx = 0;
        c.anchor = GridBagConstraints.EAST;
        c.insets = new Insets(5, 20, 5, 5);
        rightPane.add(okay, c);
        rightPane.validate();
        rightPane.repaint();
    }

    /**
     * Redraws the tree and applies the correct settings. Made method to reduce
     * repetitive code
     */
    private void resetTree() {
        final DefaultTreeCellRenderer render = new DefaultTreeCellRenderer();
        // Requested that we keep the icons
        // render.setLeafIcon(null); //Gay little grey dots
        // render.setClosedIcon(null); //Gay Folders
        // render.setOpenIcon(null);
        tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        tree.setCellRenderer(render);
        tree.putClientProperty("JTree.lineStyle", "None");
        tree.setShowsRootHandles(false);
        tree.setRootVisible(false);
        for (int row = 0; row < tree.getRowCount(); row++) {
            tree.expandPath(tree.getPathForRow(row));
        }
        scroll.setViewportView(tree);
        splits.validate();
    }

    /**
     * Created and displays the ScriptSelector
     */
    public void showSelector() {
        scriptList = ScriptSelector.loadScripts();
        tree = new JTree(getScriptsNames());
        getContentPane().removeAll();
        final JPanel pane = new JPanel(new GridBagLayout());
        pane.setBorder(null);
        rightPane = new JPanel(new GridBagLayout());
        rightPane.setBorder(null);
        final GridBagConstraints c = new GridBagConstraints();
        accounts = new JComboBox(AccountManager.getAccountNames());
        okay = new JButton("OK");
        okay.addActionListener(this);
        searchOpt = new JCheckBox("Search");
        searchOpt.addActionListener(this);
        scroll = new JScrollPane(tree);
        scroll.setBorder(null);
        description = new JTextPane();
        description.setBorder(null);
        description.setEditable(false);
        description.setPreferredSize(new Dimension(350, 300));
        scrollDescription = new JScrollPane(description);
        scrollDescription.setBorder(null);
        resetRightSide(false, false, null);
        splits = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, scroll, rightPane);
        splits.setBorder(null);
        splits.setEnabled(false);
        resetTree();
        tree.setSelectionRow(0);
        tree.requestFocusInWindow();
        tree.addTreeSelectionListener(this);
        pane.add(splits, c);
        add(pane);
        setResizable(false);
        final String[] names = sort(getScriptsNames(), TYPE.CATEGORY, true);
        final LinkedHashMap<String, ScriptManifest> ans = new LinkedHashMap<String, ScriptManifest>();
        for (String s : names) {
            s = getScriptForName(s);
            ans.put(s, scriptList.get(s));
        }
        scriptList = ans;
        remakeTree(names, TYPE.CATEGORY, false);
        pack();
        scroll.setPreferredSize(scroll.getSize());
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(getParent());
        setVisible(true);
        setResizable(false);
        splits.setBorder(null);
        description.setBorder(null);
    }

    /**
     * Sorts the Scripts based on the information sent. Implements MergeSort
     *
     * @param all List of the scripts to sort
     * @param t   TYPE to sort by
     * @param asc True if ascending order.
     * @return Sorted list of Scripts
     */
    private String[] sort(final String[] all, final TYPE t, final boolean asc) {
        if (all.length <= 1)
            return all;
        final String[] left = new String[all.length / 2], right = new String[all.length - left.length];
        System.arraycopy(all, 0, left, 0, left.length);
        System.arraycopy(all, left.length, right, 0, right.length);
        return merge(sort(left, t, asc), sort(right, t, asc), t, asc);
    }

    /**
     * Handles the different selections of the JTree.
     */
    public void valueChanged(final TreeSelectionEvent event) {
        if (((JTree) event.getSource()).getSelectionPath() == null)
            return;
        if (searchOpt.isSelected()) {
            searchOpt.doClick();
        }
        description.setContentType("text/plain");
        description.setContentType("text/html");
        final ScriptManifest info = scriptList.get(getScriptForName(((JTree) event.getSource()).getSelectionPath().getLastPathComponent().toString()));
        if (info == null) {
            description.setText("");
        } else if ((info.description() == null) || info.description().equals("")) {
            description.setText(defaultText);
		} else {
			description.setText(info.description());
		}
		description.validate();
		description.setCaretPosition(0);
	}
}