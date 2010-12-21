package org.rsbot.gui;

import org.rsbot.util.GlobalConfiguration;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableColumnModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Logger;

/**
 * @author Tekk
 * @author Jacmob
 * @author Aion
 */
public class AccountManager extends JDialog implements ActionListener {

	private static final String FILE_NAME = GlobalConfiguration.Paths.getAccountsFile();

	private static final String[] RANDOM_REWARDS = {"Cash", "Runes", "Coal",
			"Essence", "Ore", "Bars", "Gems", "Herbs", "Seeds", "Charms",
			"Surprise", "Emote", "Costume", "Attack", "Defence", "Strength",
			"Constitution", "Range", "Prayer", "Magic", "Cooking", "Woodcutting",
			"Fletching", "Fishing", "Firemaking", "Crafting", "Smithing", "Mining",
			"Herblore", "Agility", "Thieving", "Slayer", "Farming", "Runecrafting",
			"Hunter", "Construction", "Summoning", "Dungeoneering"};

	private static final String[] VALID_KEYS = {"password", "pin", "reward", "member", "take_breaks"};

	private static Map<String, Map<String, String>> accounts;

	private static final Logger log = Logger.getLogger(AccountManager.class.getName());

	private static String key;

	static {
		try {
			final InetAddress address = InetAddress.getLocalHost();
			final NetworkInterface ni = NetworkInterface.getByInetAddress(address);
			AccountManager.key = new String(ni.getHardwareAddress());
		} catch (final Exception e) {
			AccountManager.key = System.getProperty("user.name") + System.getProperty("user.language");
		}
		AccountManager.accounts = AccountManager.loadAccounts();
	}

	private static class RandomRewardEditor extends DefaultCellEditor {
		public RandomRewardEditor() {
			super(new JComboBox(RANDOM_REWARDS));
		}
	}

	private static class PasswordCellEditor extends DefaultCellEditor {
		public PasswordCellEditor() {
			super(new JPasswordField());
		}
	}

	private static class PasswordCellRenderer extends DefaultTableCellRenderer {
		protected void setValue(Object value) {
			if (value == null) {
				setText("<none>");
			} else {
				String str = value.toString();
				StringBuilder b = new StringBuilder();
				for (int i = 0; i < str.length(); ++i) {
					b.append("*");
				}
				setText(b.toString());
			}
		}
	}

	private class TableSelectionListener implements ListSelectionListener {
		public void valueChanged(ListSelectionEvent evt) {
			int row = table.getSelectedRow();
			if (!evt.getValueIsAdjusting()) {
				removeButton.setEnabled(row >= 0 && row < table.getRowCount());
			}
		}
	}

	private class AccountTableModel extends AbstractTableModel {

		public int getRowCount() {
			return accounts.size();
		}

		public int getColumnCount() {
			return VALID_KEYS.length + 1;
		}

		public Object getValueAt(int row, int column) {
			if (column == 0) {
				return userForRow(row);
			} else {
				Map<String, String> acc = accounts.get(userForRow(row));
				if (acc != null) {
					String str = acc.get(VALID_KEYS[column - 1]);
					if (str == null || str.isEmpty()) {
						return null;
					}
					if (getColumnClass(column) == Boolean.class)
						return Boolean.parseBoolean(str);
					else if (getColumnClass(column) == Integer.class)
						return Integer.parseInt(str);
					else return str;
				}
			}
			return null;
		}

		public String getColumnName(int column) {
			if (column == 0) return "Username";
			String str = VALID_KEYS[column - 1];
			StringBuilder b = new StringBuilder();
			boolean space = true;
			for (char c : str.toCharArray()) {
				if (c == '_') {
					c = ' ';
				}
				b.append(space ? Character.toUpperCase(c) : c);
				space = c == ' ';
			}
			return b.toString();
		}

		public Class getColumnClass(int column) {
			if (getColumnName(column).equals("Member"))
				return Boolean.class;
			if (getColumnName(column).equals("Take Breaks"))
				return Boolean.class;
			return Object.class;
		}

		public boolean isCellEditable(int row, int column) {
			return column > 0;
		}

		public void setValueAt(Object value, int row, int column) {
			Map<String, String> acc = accounts.get(userForRow(row));
			if (acc == null) return;
			acc.put(getColumnName(column).toLowerCase().replace(' ', '_'), String.valueOf(value));
			fireTableCellUpdated(row, column);
		}

		public String userForRow(int row) {
			Iterator<String> it = accounts.keySet().iterator();
			for (int k = 0; it.hasNext() && k < row; k++) {
				it.next();
			}
			if (it.hasNext())
				return it.next();
			return null;
		}

	}

	private JTable table;
	private JButton removeButton;

	private AccountManager() {
		super(Frame.getFrames()[0], "Account Manager", true);
	}

	public void actionPerformed(ActionEvent e) {
		if (e.getSource() instanceof JButton) {
			String label = ((JButton) e.getSource()).getText();
			if (label.equals("Done")) {
				saveAccounts();
				dispose();
			} else if (label.equals("Add")) {
				String str = JOptionPane.showInputDialog(getParent(),
						"Enter the account username.", "New Account", JOptionPane.QUESTION_MESSAGE);
				if (str == null || str.isEmpty()) return;
				accounts.put(str, new TreeMap<String, String>());
				accounts.get(str).put("reward", RANDOM_REWARDS[0]);
				int row = table.getRowCount();
				((AccountTableModel) table.getModel()).fireTableRowsInserted(row, row);
			} else if (label.equals("Remove")) {
				int row = table.getSelectedRow();
				String user = ((AccountTableModel) table.getModel()).userForRow(row);
				if (user != null) {
					accounts.remove(user);
					((AccountTableModel) table.getModel()).fireTableRowsDeleted(row, row);
				}
			}
		}
	}

	/**
	 * Creates and displays the main GUI
	 * This GUI has the list and the main buttons
	 */
	public void showGUI() {
		JScrollPane scrollPane = new JScrollPane();
		table = new JTable(new AccountTableModel());
		JPanel bar = new JPanel();
		removeButton = new JButton();
		JButton newButton = new JButton();
		JButton doneButton = new JButton();

		setTitle("Account Manager");
		Container contentPane = getContentPane();
		contentPane.setLayout(new BorderLayout(5, 5));

		table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		table.getSelectionModel().addListSelectionListener(new TableSelectionListener());

		TableColumnModel cm = table.getColumnModel();

		cm.getColumn(cm.getColumnIndex("Password")).setCellRenderer(new PasswordCellRenderer());
		cm.getColumn(cm.getColumnIndex("Password")).setCellEditor(new PasswordCellEditor());
		cm.getColumn(cm.getColumnIndex("Pin")).setCellRenderer(new PasswordCellRenderer());
		cm.getColumn(cm.getColumnIndex("Pin")).setCellEditor(new PasswordCellEditor());
		cm.getColumn(cm.getColumnIndex("Reward")).setCellEditor(new RandomRewardEditor());

		scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		scrollPane.setViewportView(table);

		contentPane.add(scrollPane, BorderLayout.CENTER);

		GridBagLayout gbl = new GridBagLayout();
		bar.setLayout(gbl);
		gbl.rowHeights = new int[]{0, 0};
		gbl.rowWeights = new double[]{0.0, 1.0E-4};

		newButton.setText("Add");
		bar.add(newButton, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
				GridBagConstraints.CENTER, GridBagConstraints.BOTH,
				new Insets(0, 0, 5, 5), 0, 0));

		removeButton.setText("Remove");
		bar.add(removeButton, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0,
				GridBagConstraints.CENTER, GridBagConstraints.BOTH,
				new Insets(0, 0, 5, 5), 0, 0));

		doneButton.setText("Done");
		bar.add(doneButton, new GridBagConstraints(2, 0, 1, 1, 0.0, 0.0,
				GridBagConstraints.CENTER, GridBagConstraints.BOTH,
				new Insets(0, 0, 5, 0), 0, 0));

		newButton.addActionListener(this);
		doneButton.addActionListener(this);
		removeButton.addActionListener(this);
		contentPane.add(bar, BorderLayout.SOUTH);

		int row = table.getSelectedRow();
		removeButton.setEnabled(row >= 0 && row < table.getRowCount());
		table.clearSelection();
		doneButton.requestFocus();

		setPreferredSize(new Dimension(600, 300));
		pack();
		setLocationRelativeTo(getOwner());
		setResizable(false);
		setVisible(true);
	}

	/**
	 * Encipher/decipher a string using a SHA1 hash of key.
	 *
	 * @param start The input String
	 * @param en	true to encrypt; false to decipher.
	 * @return The ciphered String.
	 */
	private static String cipher(final String start, final boolean en) {
		final String delim = "a";
		if (start == null) {
			return null;
		}
		int i;
		byte[] hashedKey, password;
		try {
			hashedKey = AccountManager.SHA1(AccountManager.key);
		} catch (final NoSuchAlgorithmException e) {
			e.printStackTrace();
			return start;
		} catch (final UnsupportedEncodingException e) {
			e.printStackTrace();
			return start;
		}
		if (en) {
			String end = "";
			password = start.getBytes();
			for (i = 0; i < hashedKey.length; i++) {
				if (i < start.length()) {
					end += hashedKey[i] + password[i] + delim;
				} else {
					end += hashedKey[i] + delim;
				}
			}
			return end.substring(0, end.length() - delim.length());
		}
		final String[] temp = start.split(delim);
		password = new byte[temp.length];
		for (i = 0; i < hashedKey.length; i++) {
			final int temp2 = Integer.parseInt(temp[i]);
			if (hashedKey[i] == temp2) {
				break;
			}
			password[i] = (byte) (temp2 - hashedKey[i]);
		}
		return new String(password, 0, i);
	}

	/**
	 * Capitalizes the first character and replaces spaces with underscores
	 * Purely aesthetic
	 *
	 * @param name The name of the account
	 * @return Fixed name
	 */
	private static String fixName(String name) {
		if (name.charAt(0) > 91) {
			name = (char) (name.charAt(0) - 32) + name.substring(1);
		}
		if (!name.contains("@")) {
			name = name.replaceAll("\\s", "_");
		}
		return name;
	}

	/**
	 * Access the list of names for loaded accounts
	 *
	 * @return Array of the names
	 */
	public static String[] getAccountNames() {
		return AccountManager.accounts.keySet().toArray(
				new String[AccountManager.accounts.size()]);
	}

	public static AccountManager getInstance() {
		return new AccountManager();
	}

	/**
	 * Access the account password of the given name
	 *
	 * @param name The name of the account
	 * @return Unencrypted password
	 */
	public static String getPassword(final String name) {
		Map<String, String> values = AccountManager.accounts.get(name);
		String password = values.get("password");
		if (password == null) {
			return "";
		}
		return password;
	}

	/**
	 * Access the account pin of the given string
	 *
	 * @param name The name of the account
	 * @return Pin or an empty string
	 */
	public static String getPin(final String name) {
		Map<String, String> values = AccountManager.accounts.get(name);
		String pin = values.get("pin");
		if (pin == null) {
			pin = "-1";
		}
		return pin;
	}

	/**
	 * Access the account desired reward of the given string
	 *
	 * @param name The name of the account
	 * @return The desired reward
	 */
	public static String getReward(final String name) {
		Map<String, String> values = AccountManager.accounts.get(name);
		String reward = values.get("reward");
		if (reward == null) {
			return "Cash";
		}
		return reward;
	}

	/**
	 * Access the account state of the given string
	 *
	 * @param name Name of the account
	 * @return true if the account is member, false if it isn't
	 */
	public static boolean isMember(final String name) {
		Map<String, String> values = AccountManager.accounts.get(name);
		String member = values.get("member");
		return member != null && member.equalsIgnoreCase("true");
	}

	/**
	 * Access the account state of the given string
	 *
	 * @param name Name of the account
	 * @return true if the account is member, false if it isn't
	 */
	public static boolean isTakingBreaks(final String name) {
		Map<String, String> values = AccountManager.accounts.get(name);
		String member = values.get("take_breaks");
		return member != null && member.equalsIgnoreCase("true");
	}

	/**
	 * Check if the string is a valid key
	 *
	 * @param key The key
	 * @return true if the object is supported, false if it isn't
	 */
	private static boolean isValidKey(final String key) {
		for (String check : VALID_KEYS)
			if (key.equalsIgnoreCase(check))
				return true;
		return false;
	}

	/**
	 * Checks if the given string is a valid pin
	 *
	 * @param pin The pin
	 * @return true if the pin is valid, false if it isn't
	 */
	private static boolean isValidPin(final String pin) {
		if (pin.length() == 4) {
			for (int i = 0; i < pin.length(); i++) {
				final char charAt = pin.charAt(i);
				if (charAt < '0' || charAt > '9') {
					return false;
				}
			}
			return true;
		}
		return false;
	}

	/**
	 * Loads the account from the account file
	 *
	 * @return A map of the accounts' information
	 */
	private static Map<String, Map<String, String>> loadAccounts() {
		Map<String, Map<String, String>> names = new TreeMap<String, Map<String, String>>();
		TreeMap<String, String> keys = null;

		File accountFile = new File(AccountManager.FILE_NAME);
		if (accountFile.exists()) {
			try {
				BufferedReader br = new BufferedReader(new FileReader(accountFile));
				String line;
				String name = "";
				while ((line = br.readLine()) != null) {
					if (line.startsWith("[") && line.endsWith("]")) {
						if (!name.isEmpty()) {
							names.put(AccountManager.fixName(name), keys);
						}
						name = line.trim().substring(1).substring(0, line.length() - 2);
						keys = new TreeMap<String, String>();
						continue;
					}
					if (keys != null && line.matches("^\\w+=.+$")) {
						if (name.isEmpty()) {
							continue;
						}
						String[] split = line.trim().split("=");
						if (isValidKey(split[0])) {
							String value = split[1];
							if (split[0].equals("pin")) {
								if (!isValidPin(value)) {
									log.warning("Invalid pin '" + value + "' on account: " + name + " (ignored)");
									value = null;
								}
							}
							if (split[0].equals("password")) {
								value = AccountManager.cipher(value, false);
							}
							keys.put(split[0], value);
						}
					}
				}
				if (!name.isEmpty()) {
					names.put(AccountManager.fixName(name), keys);
				}
				br.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return names;
	}

	/**
	 * Saves the account to the account file
	 */
	private static void saveAccounts() {
		final File accountFile = new File(FILE_NAME);
		try {
			final BufferedWriter bw = new BufferedWriter(new FileWriter(accountFile));
			for (final String name : AccountManager.accounts.keySet()) {
				if (name.isEmpty()) {
					continue;
				}
				bw.append("[").append(name).append("]");
				bw.newLine();
				for (final String key : AccountManager.accounts.get(name).keySet()) {
					if (key.isEmpty()) {
						continue;
					}
					String value = AccountManager.accounts.get(name).get(key);
					if (key.equals("password")) {
						value = cipher(value, true);
					} else if (key.equals("pin") && value != null && !isValidPin(value)) {
						if (!value.isEmpty()) {
							log.warning("Invalid pin '" + value + "' on account: " + name + " (ignored)");
						}
						AccountManager.accounts.get(name).remove(key);
					}
					bw.append(key).append("=").append(value);
					bw.newLine();
				}
			}
			bw.close();
		} catch (Exception ignored) {
		}
	}

	private static byte[] SHA1(final String in) throws NoSuchAlgorithmException, UnsupportedEncodingException {
		MessageDigest md = MessageDigest.getInstance("SHA-1");
		md.update(in.getBytes("iso-8859-1"), 0, in.length());
		return md.digest();
	}

}