package org.rsbot.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.swing.Box;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;

import org.rsbot.bot.Bot;
import org.rsbot.script.Script;
import org.rsbot.script.internal.ScriptHandler;
import org.rsbot.script.internal.event.ScriptListener;
import org.rsbot.service.FileScriptSource;
import org.rsbot.service.ScriptBoxSource;
import org.rsbot.service.ScriptDefinition;
import org.rsbot.service.ScriptSource;
import org.rsbot.service.ServiceException;
import org.rsbot.util.GlobalConfiguration;

/**
 * @author Jacmob
 */
public class ScriptSelector extends JDialog implements ScriptListener {

	public static void main(String[] args) {
		new ScriptSelector(null, null).setVisible(true);
	}

	private static final long serialVersionUID = 5475451138208522511L;

	private static final String[] COLUMN_NAMES = new String[] { "", "Name",
			"Version", "Author", "Description" };

	private static final ScriptSource SRC_SOURCES;
	private static final ScriptSource SRC_PRECOMPILED;
	private static final ScriptSource SRC_BUNDLED;
	private static final ScriptSource SRC_DRM;

	static {
		SRC_SOURCES = new FileScriptSource(new File(
				GlobalConfiguration.Paths.getScriptsSourcesDirectory()));
		SRC_PRECOMPILED = new FileScriptSource(new File(
				GlobalConfiguration.Paths.getScriptsPrecompiledDirectory()));
		if (GlobalConfiguration.RUNNING_FROM_JAR) {
			SRC_BUNDLED = new FileScriptSource(new File(
					GlobalConfiguration.Paths.getScriptsExtractedCache()));
		} else {
			SRC_BUNDLED = new FileScriptSource(new File("." + File.separator
					+ GlobalConfiguration.Paths.SCRIPTS_NAME_SRC));
		}
		SRC_DRM = new ScriptBoxSource(LoginDialog.CREDENTIALS);
	}

	private final Bot bot;
	private JTable table;
	private JTextField search;
	private JComboBox accounts;
	private final ScriptTableModel model;
	private final List<ScriptDefinition> scripts;
	private JButton submit;

	public ScriptSelector(Frame frame, Bot bot) {
		super(frame, "Script Selector");
		this.bot = bot;
		this.scripts = new ArrayList<ScriptDefinition>();
		this.model = new ScriptTableModel(this.scripts);
	}

	public void showGUI() {
		init();
		update();
		setVisible(true);
		load();
	}

	public void update() {
		boolean available = bot.getScriptHandler().getRunningScripts().size() == 0;
		submit.setEnabled(available && table.getSelectedRow() != -1);
		table.setEnabled(available);
		search.setEnabled(available);
		accounts.setEnabled(available);
		table.clearSelection();
	}

	private void load() {
		scripts.clear();
		scripts.addAll(SRC_DRM.list());
		scripts.addAll(SRC_BUNDLED.list());
		scripts.addAll(SRC_PRECOMPILED.list());
		scripts.addAll(SRC_SOURCES.list());
		model.search(search.getText());
	}

	@SuppressWarnings("serial")
	private void init() {
		setLayout(new BorderLayout());
		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);

		bot.getScriptHandler().addScriptListener(ScriptSelector.this);
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(final WindowEvent e) {
				bot.getScriptHandler()
						.removeScriptListener(ScriptSelector.this);
				dispose();
			}
		});

		table = new JTable(model) {
			@Override
			public String getToolTipText(MouseEvent e) {
				int row = rowAtPoint(e.getPoint());
				ScriptDefinition def = model.getDefinition(row);
				if (def != null) {
					StringBuilder b = new StringBuilder();
					if (def.authors.length > 1) {
						b.append("Authors: ");
					} else {
						b.append("Author: ");
					}
					boolean prefix = false;
					for (String author : def.authors) {
						if (prefix) {
							b.append(", ");
						} else {
							prefix = true;
						}
						b.append(author);
					}
					return b.toString();
				}
				return super.getToolTipText(e);
			}
		};
		table.setRowHeight(20);
		table.setIntercellSpacing(new Dimension(1, 1));
		table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		table.getSelectionModel().addListSelectionListener(
				new TableSelectionListener());
		setColumnWidths(table, 30, 175, 50, 100);

		JToolBar toolBar = new JToolBar();
		toolBar.setMargin(new Insets(1, 1, 1, 1));
		toolBar.setFloatable(false);

		search = new JTextField();
		search.addFocusListener(new FocusAdapter() {
			@Override
			public void focusGained(FocusEvent e) {
				table.clearSelection();
			}
		});
		search.addKeyListener(new KeyAdapter() {
			@Override
			public void keyTyped(KeyEvent e) {
				model.search(search.getText());
				table.revalidate();
			}
		});
		submit = new JButton("Start Script", new ImageIcon(
				GlobalConfiguration.getImage(
						GlobalConfiguration.Paths.Resources.ICON_START,
						GlobalConfiguration.Paths.ICON_START)));
		JButton connect = new JButton(new ImageIcon(
				GlobalConfiguration.getImage(
						GlobalConfiguration.Paths.Resources.ICON_DISCONNECT,
						GlobalConfiguration.Paths.ICON_DISCONNECT)));
		submit.setEnabled(false);
		submit.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent evt) {
				ScriptDefinition def = model.getDefinition(table
						.getSelectedRow());
				try {
					bot.setAccount((String) accounts.getSelectedItem());
					bot.getScriptHandler().runScript(def.source.load(def));
					bot.getScriptHandler().removeScriptListener(
							ScriptSelector.this);
					dispose();
				} catch (ServiceException e) {
					e.printStackTrace();
				}
			}
		});

		connect.setEnabled(false);
		connect.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent evt) {

			}
		});

		accounts = new JComboBox(AccountManager.getAccountNames());
		accounts.setMinimumSize(new Dimension(200, 20));
		accounts.setPreferredSize(new Dimension(200, 20));

		toolBar.add(search);
		toolBar.add(Box.createHorizontalStrut(5));
		toolBar.add(accounts);
		toolBar.add(Box.createHorizontalStrut(5));
		toolBar.add(connect);
		toolBar.add(Box.createHorizontalStrut(5));
		toolBar.add(submit);

		JPanel center = new JPanel();
		center.setLayout(new BorderLayout());
		JScrollPane pane = new JScrollPane(table,
				JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
				JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		center.add(pane, BorderLayout.CENTER);

		add(center, BorderLayout.CENTER);
		add(toolBar, BorderLayout.SOUTH);

		setSize(750, 400);
		setMinimumSize(getSize());
		setLocationRelativeTo(getParent());
		search.requestFocus();
	}

	private void setColumnWidths(JTable table, int... widths) {
		for (int i = 0; i < widths.length; ++i) {
			table.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
			table.getColumnModel().getColumn(i).setMinWidth(widths[i]);
			table.getColumnModel().getColumn(i).setMaxWidth(widths[i]);
		}
	}

	@Override
	public void scriptStarted(ScriptHandler handler, Script script) {
		update();
	}

	@Override
	public void scriptStopped(ScriptHandler handler, Script script) {
		update();
	}

	@Override
	public void scriptResumed(ScriptHandler handler, Script script) {

	}

	@Override
	public void scriptPaused(ScriptHandler handler, Script script) {

	}

	@Override
	public void inputChanged(Bot bot, int mask) {

	}

	private class TableSelectionListener implements ListSelectionListener {

		@Override
		public void valueChanged(ListSelectionEvent evt) {
			if (!evt.getValueIsAdjusting()) {
				submit.setEnabled(table.getSelectedRow() != -1);
			}
		}

	}

	private static class ScriptTableModel extends AbstractTableModel {

		private static final long serialVersionUID = 1L;

		public static final ImageIcon ICON_SCRIPT_SRC = new ImageIcon(
				GlobalConfiguration.getImage(
						GlobalConfiguration.Paths.Resources.ICON_SCRIPT_SRC,
						GlobalConfiguration.Paths.ICON_SCRIPT_SRC));

		public static final ImageIcon ICON_SCRIPT_PRE = new ImageIcon(
				GlobalConfiguration.getImage(
						GlobalConfiguration.Paths.Resources.ICON_SCRIPT_PRE,
						GlobalConfiguration.Paths.ICON_SCRIPT_PRE));

		public static final ImageIcon ICON_SCRIPT_DRM = new ImageIcon(
				GlobalConfiguration.getImage(
						GlobalConfiguration.Paths.Resources.ICON_SCRIPT_DRM,
						GlobalConfiguration.Paths.ICON_SCRIPT_DRM));

		public static final ImageIcon ICON_SCRIPT_BDL = new ImageIcon(
				GlobalConfiguration.getImage(
						GlobalConfiguration.Paths.Resources.ICON_SCRIPT_BDL,
						GlobalConfiguration.Paths.ICON_SCRIPT_BDL));

		private final List<ScriptDefinition> scripts;
		private final List<ScriptDefinition> matches;

		public ScriptTableModel(List<ScriptDefinition> scripts) {
			this.scripts = scripts;
			this.matches = new ArrayList<ScriptDefinition>();
		}

		public void search(String substr) {
			matches.clear();
			if (substr.length() == 0) {
				matches.addAll(scripts);
			} else {
				substr = substr.toLowerCase();
				for (ScriptDefinition def : scripts) {
					if (def.name.toLowerCase().contains(substr)) {
						matches.add(def);
					} else {
						for (String keyword : def.keywords) {
							if (keyword.toLowerCase().contains(substr)) {
								matches.add(def);
								break;
							}
						}
					}
				}
			}
			fireTableDataChanged();
		}

		public ScriptDefinition getDefinition(int rowIndex) {
			return matches.get(rowIndex);
		}

		@Override
		public int getRowCount() {
			return matches.size();
		}

		@Override
		public int getColumnCount() {
			return COLUMN_NAMES.length;
		}

		@Override
		public Object getValueAt(int rowIndex, int columnIndex) {
			if (rowIndex >= 0 && rowIndex < matches.size()) {
				ScriptDefinition def = matches.get(rowIndex);
				if (columnIndex == 0) {
					if (def.source == SRC_SOURCES) {
						return ICON_SCRIPT_SRC;
					}
					if (def.source == SRC_PRECOMPILED) {
						return ICON_SCRIPT_PRE;
					}
					if (def.source == SRC_BUNDLED) {
						return ICON_SCRIPT_BDL;
					}
					return ICON_SCRIPT_DRM;
				}
				if (columnIndex == 1) {
					return def.name;
				}
				if (columnIndex == 2) {
					return def.version;
				}
				if (columnIndex == 3) {
					StringBuilder b = new StringBuilder();
					for (String author : def.authors) {
						b.append(author).append(", ");
					}
					return b.replace(b.length() - 2, b.length(), "");
				}
				if (columnIndex == 4) {
					return def.description;
				}
			}
			return null;
		}

		@Override
		public Class<?> getColumnClass(int col) {
			if (col == 0) {
				return ImageIcon.class;
			}
			return String.class;
		}

		@Override
		public String getColumnName(int col) {
			return COLUMN_NAMES[col];
		}

	}

}