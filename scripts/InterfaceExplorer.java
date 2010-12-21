import org.rsbot.event.listeners.PaintListener;
import org.rsbot.script.Script;
import org.rsbot.script.ScriptManifest;
import org.rsbot.script.wrappers.RSComponent;
import org.rsbot.script.wrappers.RSInterface;

import javax.swing.*;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Arrays;

@ScriptManifest(authors = {"joku.rules"}, keywords = "Development", name = "Interface Explorer", version = 0.3, description = "Fetches various interface data for developers.")
public class InterfaceExplorer extends Script implements PaintListener {

	private class InterfaceTreeModel implements TreeModel {
		private final Object root = new Object();
		private final ArrayList<TreeModelListener> treeModelListeners = new ArrayList<TreeModelListener>();

		// only call getAllInterfaces() once per GUI update, because
		// otherwise closed interfaces might mess up the indexes
		private final ArrayList<RSInterfaceWrap> interfaceWraps = new ArrayList<RSInterfaceWrap>();

		public void addTreeModelListener(final TreeModelListener l) {
			treeModelListeners.add(l);
		}

		private void fireTreeStructureChanged(final Object oldRoot) {
			treeModelListeners.size();
			final TreeModelEvent e = new TreeModelEvent(this,
					new Object[]{oldRoot});
			for (final TreeModelListener tml : treeModelListeners) {
				tml.treeStructureChanged(e);
			}
		}

		public Object getChild(final Object parent, final int index) {
			if (parent == root) {
				return interfaceWraps.get(index);
			} else if (parent instanceof RSInterfaceWrap) {
				return new RSComponentWrap(
						((RSInterfaceWrap) parent).wrapped.getComponents()[index]);
			} else if (parent instanceof RSComponentWrap) {
				return new RSComponentWrap(
						((RSComponentWrap) parent).wrapped.getComponents()[index]);
			}
			return null;
		}

		public int getChildCount(final Object parent) {
			if (parent == root) {
				return interfaceWraps.size();
			} else if (parent instanceof RSInterfaceWrap) {
				return ((RSInterfaceWrap) parent).wrapped.getComponents().length;
			} else if (parent instanceof RSComponentWrap) {
				return ((RSComponentWrap) parent).wrapped.getComponents().length;
			}
			return 0;
		}

		public int getIndexOfChild(final Object parent, final Object child) {
			if (parent == root) {
				return interfaceWraps.indexOf(child);
			} else if (parent instanceof RSInterfaceWrap) {
				return Arrays.asList(
						((RSInterfaceWrap) parent).wrapped.getComponents())
						.indexOf(((RSComponentWrap) child).wrapped);
			} else if (parent instanceof RSComponentWrap) {
				return Arrays
						.asList(
								((RSComponentWrap) parent).wrapped
										.getComponents()).indexOf(
								((RSComponentWrap) child).wrapped);
			}
			return -1;
		}

		public Object getRoot() {
			return root;
		}

		public boolean isLeaf(final Object o) {
			return o instanceof RSComponentWrap && ((RSComponentWrap) o).wrapped.getComponents().length == 0;
		}

		public void removeTreeModelListener(final TreeModelListener l) {
			treeModelListeners.remove(l);
		}

		public boolean searchMatches(final RSComponent iface,
									 final String contains) {
			return iface.getText().toLowerCase().contains(
					contains.toLowerCase());
		}

		public void update(final String search) {
			interfaceWraps.clear();

			for (final RSInterface iface : interfaces.getAll()) {
				toBreak:
				for (final RSComponent child : iface
						.getComponents()) {
					if (searchMatches(child, search)) {
						interfaceWraps.add(new RSInterfaceWrap(iface));
						break;
					}

					for (final RSComponent component : child.getComponents()) {
						if (searchMatches(component, search)) {
							interfaceWraps.add(new RSInterfaceWrap(iface));
							break toBreak;
						}
					}
				}
			}
			fireTreeStructureChanged(root);
		}

		public void valueForPathChanged(final TreePath path,
										final Object newValue) {
			// tree represented by this model isn't editable
		}
	}

	private class RSComponentWrap {
		public RSComponent wrapped;

		public RSComponentWrap(final RSComponent wrapped) {
			this.wrapped = wrapped;
		}

		@Override
		public boolean equals(final Object o) {
			return o instanceof RSComponentWrap && wrapped == ((RSComponentWrap) o).wrapped;
		}

		@Override
		public String toString() {
			return "Component " + wrapped.getIndex();
		}
	}

	// these wrappers just add toString() methods
	private class RSInterfaceWrap {
		public RSInterface wrapped;

		public RSInterfaceWrap(final RSInterface wrapped) {
			this.wrapped = wrapped;
		}

		@Override
		public boolean equals(final Object o) {
			return o instanceof RSInterfaceWrap && wrapped == ((RSInterfaceWrap) o).wrapped;
		}

		@Override
		public String toString() {
			return "Interface " + wrapped.getIndex();
		}
	}

	private JFrame window;
	private JTree tree;

	private InterfaceTreeModel treeModel;

	private JPanel infoArea;

	private JTextField searchBox;

	private Rectangle highlightArea = null;

	@Override
	public int loop() {
		if (window.isVisible()) {
			return 1000;
		}
		return -1;
	}

	public void onRepaint(final Graphics g) {
		if (highlightArea != null) {
			g.setColor(Color.ORANGE);
			g.drawRect(highlightArea.x, highlightArea.y, highlightArea.width,
					highlightArea.height);
		}
	}

	public boolean onStart() {
		window = new JFrame("Interface Explorer");

		treeModel = new InterfaceTreeModel();
		treeModel.update("");
		tree = new JTree(treeModel);
		tree.setRootVisible(false);
		tree.setEditable(false);
		tree.getSelectionModel().setSelectionMode(
				TreeSelectionModel.SINGLE_TREE_SELECTION);
		tree.addTreeSelectionListener(new TreeSelectionListener() {
			private void addInfo(final String key, final String value) {
				final JPanel row = new JPanel();
				row.setAlignmentX(Component.LEFT_ALIGNMENT);
				row.setLayout(new BoxLayout(row, BoxLayout.X_AXIS));

				for (final String data : new String[]{key, value}) {
					final JLabel label = new JLabel(data);
					label.setAlignmentY(Component.TOP_ALIGNMENT);
					row.add(label);
				}
				infoArea.add(row);
			}

			public void valueChanged(final TreeSelectionEvent e) {
				final Object node = tree.getLastSelectedPathComponent();
				if (node == null || node instanceof RSInterfaceWrap) {
					return;
				}
				// at this point the node can only be an instace of
				// RSInterfaceChildWrap
				// or of RSInterfaceComponentWrap

				infoArea.removeAll();
				RSComponent iface = null;
				if (node instanceof RSComponentWrap) {
					highlightArea = ((RSComponentWrap) node).wrapped
							.getArea();
					iface = ((RSComponentWrap) node).wrapped;
				}
				if (iface == null) {
					return;
				}
				addInfo("Action type: ", "-1" /* + iface.getActionType() */);
				addInfo("Type: ", "" + iface.getType());
				addInfo("SpecialType: ", "" + iface.getSpecialType());
				addInfo("Bounds Index: ", "" + iface.getBoundsArrayIndex());
				addInfo("Model ID: ", "" + iface.getModelID());
				addInfo("Texture ID: ", "" + iface.getBackgroundColor());
				addInfo("Parent ID: ", "" + iface.getParentID());
				addInfo("Text: ", "" + iface.getText());
				addInfo("Tooltip: ", "" + iface.getTooltip());
				addInfo("SelActionName: ", "" + iface.getSelectedActionName());
				if (iface.getActions() != null) {
					String actions = "";
					for (final String action : iface.getActions()) {
						if (!actions.equals("")) {
							actions += "\n";
						}
						actions += action;
					}
					addInfo("Actions: ", actions);
				}
				addInfo("Component ID: ", "" + iface.getComponentID());
				addInfo("Component Stack Size: ", "" + iface.getComponentStackSize());
				addInfo("Relative Location: ", "(" + iface.getRelativeX() + "," + iface.getRelativeY() + ")");
				addInfo("Absolute Location: ", "(" + iface.getAbsoluteX() + "," + iface.getAbsoluteY() + ")");

				infoArea.validate();
				infoArea.repaint();
			}
		});

		JScrollPane scrollPane = new JScrollPane(tree);
		scrollPane.setPreferredSize(new Dimension(250, 500));
		window.add(scrollPane, BorderLayout.WEST);

		infoArea = new JPanel();
		infoArea.setLayout(new BoxLayout(infoArea, BoxLayout.Y_AXIS));
		scrollPane = new JScrollPane(infoArea);
		scrollPane.setPreferredSize(new Dimension(250, 500));
		window.add(scrollPane, BorderLayout.CENTER);

		final ActionListener actionListener = new ActionListener() {
			public void actionPerformed(final ActionEvent e) {
				treeModel.update(searchBox.getText());
				infoArea.removeAll();
				infoArea.validate();
				infoArea.repaint();
			}
		};

		final JPanel toolArea = new JPanel();
		toolArea.setLayout(new FlowLayout(FlowLayout.LEFT));
		toolArea.add(new JLabel("Filter:"));

		searchBox = new JTextField(20);
		searchBox.addActionListener(actionListener);
		toolArea.add(searchBox);

		final JButton updateButton = new JButton("Update");
		updateButton.addActionListener(actionListener);
		toolArea.add(updateButton);
		window.add(toolArea, BorderLayout.NORTH);

		window.pack();
		window.setVisible(true);
		return true;
	}
}
