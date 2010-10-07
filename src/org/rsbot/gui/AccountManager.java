package org.rsbot.gui;

import java.awt.Component;
import java.awt.Container;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

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

import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Logger;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SpinnerNumberModel;

import org.rsbot.script.methods.Skills;
import org.rsbot.util.GlobalConfiguration;

/**
 *
 * @author Fusion89k
 * @author Aion
 */
public class AccountManager extends JDialog implements ActionListener {

    private static final String EMPTY = "No accounts were found!";
    private static final String FILE_NAME = GlobalConfiguration.Paths.getAccountsFile();

    private static Map<String, Map<String, String>> accounts;

    private static AccountManager instance;

    private static final Logger log = Logger.getLogger(AccountManager.class.getName());

	private JList nameList;

    static String key;

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

    private AccountManager() {
        super(Frame.getFrames()[0], "Account Manager", true);
        if (AccountManager.accounts.keySet().size() < 1) {
            AccountManager.accounts.put(EMPTY, null);
        }
    }

    public void actionPerformed(ActionEvent e) {
        JDialog parentFrame;
        Component comp = ((Component) e.getSource()).getParent();
        while (!(comp instanceof JDialog)) {
            comp = comp.getParent();
        }
        parentFrame = (JDialog) comp;

        if (e.getSource() instanceof JButton) {
            switch (((JButton) e.getSource()).getText().charAt(0)) {
                case 'A':
					final Component source = ((JButton) e.getSource()).getParent().getComponent(0);
                    if (!(source instanceof JTabbedPane)) {
                        AddAccountGUI gui = new AddAccountGUI();
                        gui.initComponents();

                        final JDialog frame = new JDialog(this, "New", true);
                        frame.add(gui.getContentPane());
                        frame.pack();
                        frame.setLocationRelativeTo(this);
                        frame.setVisible(true);
                    } else {
						final JTabbedPane tab = (JTabbedPane) source;
                        String name = null, pass = null, member = null, lamp = null, reward = null, pin = "";
                        for (Component compa : tab.getComponents()) {
                            final JPanel panel = (JPanel) compa;
                            for (Component c : panel.getComponents()) {
                                if (c instanceof JPasswordField) {
                                    pass = ((JTextField) c).getText();
                                } else if (c instanceof JTextField) {
                                    name = ((JTextField) c).getText();
                                } else if (c instanceof JSpinner) {
                                    pin += ((JSpinner) c).getValue();
                                } else if (c instanceof JCheckBox) {
                                    if (((JCheckBox) c).getText().equals("Member")) {
                                        member = ((JCheckBox) c).isSelected() ? "true" : "false";
                                    }
                                } else if (c instanceof JComboBox) {
                                    final String selItem = ((JComboBox) c).getSelectedItem().toString();
                                    if (lamp == null) {
                                        lamp = selItem;
                                    } else {
                                        reward = selItem;
                                    }
                                }
                            }
                        }
                        if (name.isEmpty() || pass.isEmpty()) {
                            JOptionPane.showMessageDialog(null, "Empty Fields");
                            return;
                        }
                        refreshList(name, pass, member, pin, reward, lamp);
                        nameList.validate();
                        parentFrame.dispose();
                    }
                    break;
                case 'D':
                    if (nameList.isSelectionEmpty()) {
                        break;
                    }
                    final String selName = nameList.getSelectedValue().toString();
                    if (!AccountManager.accounts.containsKey(selName)
                            || selName.endsWith(AccountManager.EMPTY)) {
                        break;
                    }
                    final int yes = JOptionPane.showConfirmDialog(this, "Are you sure you want to delete " + selName + "?", "Delete", JOptionPane.YES_NO_OPTION);
                    if (yes == 0) {
                        AccountManager.accounts.remove(selName);
                        refreshList();
                        nameList.validate();
                    }
                    break;
                case 'S':
                    AccountManager.saveAccounts();
                    break;
            }
        } else if (e.getSource() instanceof JCheckBox) {
            final JCheckBox jcb = (JCheckBox) e.getSource();
            final boolean enable = jcb.isSelected();
            final Container con = jcb.getParent();
            for (Component c : con.getComponents()) {
                if (c instanceof JSpinner) {
                    c.setEnabled(enable);
                }
            }
        }
    }

    /**
     * Encrypts/Decrypts a string using a SHA1 hash of <code>key</code>
     * - Jacmob
     *
     * @param start The input String
     * @param en    <tt>true</tt> to encrypt; <tt>false</tt> to decrypt
     * @return The crypted String
     */
    private static String crypt(final String start, final boolean en) {
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
        return name.replaceAll("\\s", "_");
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

    /**
     * Enables AccountManager to be a Singleton
     *
     * @return The instance of the AccountManager
     */
    public static AccountManager getInstance() {
        if (AccountManager.instance == null) {
            AccountManager.instance = new AccountManager();
        }
        return AccountManager.instance;
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
        return AccountManager.crypt(password, false);
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
     * Access the account skill to use a lamp on
     *
     * @param name The name of the account
     * @return The skill to use a lamp on
     */
    public static String getSkillLamp(final String name) {
        Map<String, String> values = AccountManager.accounts.get(name);
        String skill = values.get("lamp");
        if (skill == null) {
            return "attack";
        }
        return skill.toLowerCase();
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
        if (member == null) {
            member = "false";
        }
        return member.equalsIgnoreCase("true");
    }

    /**
     * Check if the string is a valid key
     *
     * @param key The key
     * @return true if the object is supported, false if it isn't
     */
    private static boolean isValidKey(final String key) {
        return key.matches("^(member|pin|password|(default)?reward|lamp)$");
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
        final TreeMap<String, Map<String, String>> names = new TreeMap<String, Map<String, String>>();
        TreeMap<String, String> keys = null;

        final File accountFile = new File(AccountManager.FILE_NAME);
        if (accountFile.exists()) {
            try {
                final BufferedReader br = new BufferedReader(new FileReader(accountFile));
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
                        final String[] split = line.trim().toLowerCase().split("=");
                        if (isValidKey(split[0])) {
                            String key2 = split[1];
                            if (split[0].equals("pin")) {
                                if (!isValidPin(key2)) {
                                    log.severe("Invalid Pin: " + key2 + " On account: " + name + "(pin was ignored)");
                                    key2 = "";
                                }
                            }
                            keys.put(split[0], key2);
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

    private void refreshList(String name, String pass, String member, String pin, String reward, String lamp) {
        final TreeMap<String, String> keys = new TreeMap<String, String>();
        keys.put("member", member);
        keys.put("password", AccountManager.crypt(pass, true));
        keys.put("pin", pin);
        keys.put("reward", reward);
        keys.put("lamp", lamp);
        accounts.put(name, keys);

        refreshList();
    }

    private void refreshList() {
        if (AccountManager.accounts.keySet().size() < 1) {
            AccountManager.accounts.put(EMPTY, null);
        } else {
            AccountManager.accounts.remove(EMPTY);
        }
        nameList.setListData(AccountManager.accounts.keySet().toArray(new String[AccountManager.accounts.size()]));
        nameList.getParent().validate();
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
                for (final String param : AccountManager.accounts.get(name).keySet()) {
                    if (param.isEmpty()) {
                        continue;
                    }
                    final String value = AccountManager.accounts.get(name).get(param);
                    bw.append(param).append("=").append(value);
                    bw.newLine();
                }
            }
            bw.close();
        } catch (Exception ignored) {
        }
    }

    /**
     * Creates and displays the main GUI
     * This GUI has the list and the main buttons
     */
    public void showGUI() {
        getContentPane().removeAll();
		GridBagConstraints gbc = new GridBagConstraints();

        final JPanel panel = new JPanel(new GridBagLayout());

        nameList = new JList(AccountManager.accounts.keySet().toArray(
                new String[AccountManager.accounts.size()]));
        nameList.setSelectedIndex(0);
        nameList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        nameList.addMouseListener(new MouseClick());

        final JScrollPane scroll = new JScrollPane(nameList);

        final JButton add = new JButton("Add");
        final JButton del = new JButton("Del");
        final JButton save = new JButton("Save");

        add.addActionListener(this);
        del.addActionListener(this);
        save.addActionListener(this);

        gbc.gridy = 0;
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.fill = GridBagConstraints.BOTH;
        panel.add(scroll, gbc);

        gbc.gridwidth = 1;
        gbc.gridy = 1;
        gbc.gridx = 0;
        gbc.fill = GridBagConstraints.NONE;
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.CENTER;
        panel.add(add, gbc);

        gbc.gridx = GridBagConstraints.RELATIVE;
        panel.add(del, gbc);
        panel.add(save, gbc);

        add(panel);
        pack();

        setLocationRelativeTo(getOwner());
        setVisible(true);
        setResizable(false);
    }

    private static byte[] SHA1(final String in) throws NoSuchAlgorithmException, UnsupportedEncodingException {
        MessageDigest md = MessageDigest.getInstance("SHA-1");
        md.update(in.getBytes("iso-8859-1"), 0, in.length());
        return md.digest();
    }

    /**
     * This class is a custom mouse listener
     */
    class MouseClick extends MouseAdapter {

        @Override
        public void mouseClicked(final MouseEvent e) {
            if (e.getClickCount() > 1 || e.getButton() == MouseEvent.BUTTON3) {
                final Object obj = ((JList) e.getComponent()).getSelectedValue();
                if (obj == null) {
                    return;
                }

                final String clickedName = obj.toString();
                if (clickedName.isEmpty()
                        || clickedName.equals(AccountManager.EMPTY)) {
                    return;
                }

                final String password = AccountManager.getPassword(clickedName);
                final String pin = AccountManager.getPin(clickedName);
                final boolean member = AccountManager.isMember(clickedName);
                final String reward = AccountManager.getReward(clickedName);
                final String lamp = fixName(AccountManager.getSkillLamp(clickedName));

                JOptionPane.showMessageDialog(null, "Password: " + password
                        + "\nPin: " + (pin.equals("-1") ? "None" : pin)
						+ "\nMember: " + (member ? "Yes" : "No")
                        + "\nReward: " + (reward.equals("XP Item") ? lamp : reward));
            }
        }
    }

    class AddAccountGUI extends JDialog {

        private final String[] REWARDS = {"Cash", "XP Item", "Runes", "Coal", "Essence", "Ore", "Bars", "Gems", "Herbs", "Seeds", "Charms", "Surprise", "Emote", "Costume"};

        private JTabbedPane tabbedPane1;
        private JPanel panel1;
        private JLabel label1;
        private JTextField textField1;
        private JCheckBox checkBox2;
        private JLabel label2;
        private JPasswordField passwordField1;
        private JCheckBox checkBox1;
        private JPanel panel2;
        private JLabel label3;
        private JComboBox comboBox1;
        private JLabel label4;
        private JComboBox comboBox2;
        private JButton button1;

        private void initComponents() {
            tabbedPane1 = new JTabbedPane();
            panel1 = new JPanel();
            label1 = new JLabel();
            textField1 = new JTextField();
            checkBox2 = new JCheckBox();
            label2 = new JLabel();
            passwordField1 = new JPasswordField();
            checkBox1 = new JCheckBox();
            panel2 = new JPanel();
            label3 = new JLabel();
            comboBox1 = new JComboBox();
            label4 = new JLabel();
            comboBox2 = new JComboBox();
            button1 = new JButton();

            Container contentPane = getContentPane();
            contentPane.setLayout(new GridBagLayout());
            ((GridBagLayout) contentPane.getLayout()).columnWidths = new int[]{87, 0};
            ((GridBagLayout) contentPane.getLayout()).rowHeights = new int[]{0, 0, 0, 0};
            ((GridBagLayout) contentPane.getLayout()).columnWeights = new double[]{0.0, 1.0E-4};
            ((GridBagLayout) contentPane.getLayout()).rowWeights = new double[]{0.0, 0.0, 0.0, 1.0E-4};

            panel1.setLayout(new GridBagLayout());
            ((GridBagLayout) panel1.getLayout()).columnWidths = new int[]{0, 0, 0, 0, 0, 39, 0};
            ((GridBagLayout) panel1.getLayout()).rowHeights = new int[]{0, 0, 0, 0};
            ((GridBagLayout) panel1.getLayout()).columnWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0E-4};
            ((GridBagLayout) panel1.getLayout()).rowWeights = new double[]{0.0, 0.0, 0.0, 1.0E-4};

            label1.setText("Username:");
            panel1.add(label1, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
                    GridBagConstraints.CENTER, GridBagConstraints.NONE,
                    new Insets(0, 0, 5, 5), 0, 0));
            panel1.add(textField1, new GridBagConstraints(1, 0, 4, 1, 0.0, 0.0,
                    GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL,
                    new Insets(0, 0, 5, 5), 0, 0));

            checkBox2.setText("Member");
            panel1.add(checkBox2, new GridBagConstraints(5, 0, 1, 1, 0.0, 0.0,
                    GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                    new Insets(0, 0, 5, 0), 0, 0));

            label2.setText("Password:");
            panel1.add(label2, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0,
                    GridBagConstraints.CENTER, GridBagConstraints.NONE,
                    new Insets(0, 0, 5, 5), 0, 0));
            panel1.add(passwordField1, new GridBagConstraints(1, 1, 4, 1, 0.0, 0.0,
                    GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL,
                    new Insets(0, 0, 5, 5), 0, 0));

            checkBox1.setText("Pin");
            checkBox1.addActionListener(AccountManager.this);
            panel1.add(checkBox1, new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0,
                    GridBagConstraints.CENTER, GridBagConstraints.NONE,
                    new Insets(0, 0, 0, 5), 0, 0));

            final JSpinner[] spins = new JSpinner[5];
            for (int i = 1; i < spins.length; i++) {
                spins[i] = new JSpinner(new SpinnerNumberModel(0, 0, 9, 1));
                spins[i].setEnabled(false);
                panel1.add(spins[i],
                        new GridBagConstraints(i, 2, 1, 1, 0.0, 0.0,
                        GridBagConstraints.CENTER, GridBagConstraints.NONE,
                        new Insets(0, 0, 0, 5), 0, 0));
            }

            tabbedPane1.addTab("Account", panel1);

            panel2.setLayout(new GridBagLayout());
            ((GridBagLayout) panel2.getLayout()).columnWidths = new int[]{0, 120, 0};
            ((GridBagLayout) panel2.getLayout()).rowHeights = new int[]{0, 0, 0, 0, 0};
            ((GridBagLayout) panel2.getLayout()).columnWeights = new double[]{0.0, 0.0, 1.0E-4};
            ((GridBagLayout) panel2.getLayout()).rowWeights = new double[]{0.0, 0.0, 0.0, 0.0, 1.0E-4};

            label4.setText("Reward:");
            panel2.add(label4, new GridBagConstraints(0, 0, 1, 2, 0.0, 0.0,
                    GridBagConstraints.CENTER, GridBagConstraints.NONE,
                    new Insets(0, 0, 5, 5), 0, 0));
            panel2.add(comboBox2, new GridBagConstraints(1, 0, 1, 2, 0.0, 0.0,
                    GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL,
                    new Insets(0, 0, 5, 0), 0, 0));

			for (String choice : REWARDS) {
				comboBox2.addItem(choice);
			}

            label3.setText("XP Lamp:");
            panel2.add(label3, new GridBagConstraints(0, 2, 1, 2, 0.0, 0.0,
                    GridBagConstraints.CENTER, GridBagConstraints.NONE,
                    new Insets(0, 0, 0, 5), 0, 0));
            panel2.add(comboBox1, new GridBagConstraints(1, 2, 1, 2, 0.0, 0.0,
                    GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL,
                    new Insets(0, 0, 0, 0), 0, 0));

            final String[] ss = Skills.SKILL_NAMES;
            for (int i = 0; i < ss.length - 1; i++) {
                comboBox1.addItem(fixName(ss[i]));
            }

            tabbedPane1.addTab("Reward", panel2);

            contentPane.add(tabbedPane1, new GridBagConstraints(0, 0, 1, 2, 0.0, 0.0,
                    GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                    new Insets(0, 0, 5, 0), 0, 0));

            button1.setText("Add");
            button1.addActionListener(AccountManager.this);
            contentPane.add(button1, new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0,
                    GridBagConstraints.CENTER, GridBagConstraints.NONE,
                    new Insets(0, 0, 0, 0), 0, 0));
            pack();
            setLocationRelativeTo(getOwner());
        }
    }
}