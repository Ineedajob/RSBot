package org.rsbot.gui;

import java.awt.Component;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Logger;

import javax.naming.NamingException;
import javax.swing.*;

import org.rsbot.util.GlobalConfiguration;

/**
 * This will handle the management of Accounts for the Bot.<br>
 * Information will be stored in a file specified by FILE_NAME.<br>
 * Format for accounts should be:<br>
 * &nbsp;&nbsp; Name:Password[:Pin] <br>
 * Pin may be omitted if no Pin on Account
 *
 * @author Fusion89k
 */
// The brackets [] are there to denote optional.
public class AccountManager extends JDialog implements ActionListener {

    private static final long serialVersionUID = 6401178388485322197L;
    private static final String SEP_CHAR = ":";
    private static final String FILE_NAME = GlobalConfiguration.Paths.getAccountsFile();
    private static final Logger log = Logger.getLogger(AccountManager.class.getName());

    private static Map<String, String> accounts;
    private static AccountManager instance;

    private final String emptyName = "No Accounts Found";

    private JList names;
    private GridBagConstraints c;

    static String key;

    static {
        try {
            final InetAddress address = InetAddress.getLocalHost();
            final NetworkInterface ni = NetworkInterface.getByInetAddress(address);
            AccountManager.key = new String(ni.getHardwareAddress());
        } catch (final Exception e) {
            AccountManager.key = System.getProperty("user.name") + System.getProperty("user.language");
        }
        try {
            AccountManager.accounts = AccountManager.loadAccounts();
        } catch (final NamingException e) {
            log.warning("There was an error loading account data.");
            if (new File(FILE_NAME).delete()) {
                log.warning("Corrupt account file deleted.");
            } else {
                log.warning("Unable to delete corrupt account file.");
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
        if (start == null)
            return null;
        byte[] hashedkey;
        byte[] password;
        int i;
        try {
            hashedkey = AccountManager.SHA1(AccountManager.key);
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
            for (i = 0; i < hashedkey.length; i++) {
                if (i < start.length()) {
                    end += hashedkey[i] + password[i] + delim;
                } else {
                    end += hashedkey[i] + delim;
                }
            }
            return end.substring(0, end.length() - delim.length());
        } else {
            final String[] temp = start.split(delim);
            password = new byte[temp.length];
            for (i = 0; i < hashedkey.length; i++) {
                final int temp2 = Integer.parseInt(temp[i]);
                if (hashedkey[i] == temp2) {
                    break;
                } else {
                    password[i] = (byte) (temp2 - hashedkey[i]);
                }
            }
            return new String(password, 0, i);
        }
    }

    /**
     * Capitalizes the first character and replaces spaces with underscores
     * Purely aesthetic
     *
     * @param name Name to fix
     * @return Fixed name
     */
    private static String fixName(String name) {
        if (name.charAt(0) > 91) {
            name = (char) (name.charAt(0) - 32) + name.substring(1);
        }
        while (name.contains(" ")) {
            name = name.substring(0, name.indexOf(" ")) + "_" + name.substring(name.indexOf(" ") + 1);
        }
        return name;
    }

    /**
     * Access the list of names for loaded accounts
     *
     * @return Array of the Names
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
        if (AccountManager.instance == null)
            return AccountManager.instance = new AccountManager();
        else
            return AccountManager.instance;
    }

    /**
     * Access the password of the given account
     *
     * @param name Name of account to access password of
     * @return Unencrypted Password
     */
    public static String getPassword(final String name) {
        String password = AccountManager.accounts.get(name);
        if ((password != null) && password.contains(AccountManager.SEP_CHAR)) {
            password = password.split(AccountManager.SEP_CHAR, 2)[0];
        }
        return AccountManager.crypt(password, false);
    }

    /**
     * Access the pin for the given account
     *
     * @param name Name of the account
     * @return Pin or -1 if no pin
     */
    public static String getPin(final String name) {
        final String all = AccountManager.accounts.get(name);
        if (all.contains(AccountManager.SEP_CHAR))
            return all.split(AccountManager.SEP_CHAR, 2)[1];
        return "-1";
    }

    /**
     * Loads the accounts from the file
     *
     * @return A map of user names to passwords
     * @throws javax.naming.NamingException Invalid storage
     */
    private static Map<String, String> loadAccounts() throws NamingException {
        final TreeMap<String, String> accounts = new TreeMap<String, String>();
        final File accountFile = new File(AccountManager.FILE_NAME);
        if (accountFile.exists()) {
            try {
                final BufferedReader in = new BufferedReader(new FileReader(accountFile));
                String line;// Used to store the lines from the file
                while ((line = in.readLine()) != null) {
                    if (line.isEmpty()) {
                        continue;
                    }
                    // Means that it is in old format
                    if (line.contains("USERNAME")) {
                        if (!accountFile.delete()) {
                            log.warning("Failed to delete old account file.");
                        }
                        return new HashMap<String, String>();
                    }
                    // If for some reason the format is not as expected
                    if (!line.contains(AccountManager.SEP_CHAR))
                        throw new NamingException("Invalid Storage of: " + line);
                    final String[] parts = line.split(AccountManager.SEP_CHAR, 2);
                    for (final String s : parts) {
                        if (s.isEmpty())
                            throw new NamingException("Invalid Storage of: " + line);
                    }
                    // Formats the name
                    parts[0] = AccountManager.fixName(parts[0]);
                    // Checks Pin Validity
                    try {
                        if ((parts.length > 2) && ((parts[2].length() != 4) || (Integer.parseInt(parts[2]) >= 0)))
                            throw new NamingException("Invalid Pin: " + parts[2] + " On Account: " + parts[0]);
                    } catch (final NumberFormatException e) {
                        throw new NamingException("Invalid Pin: " + parts[2] + " On Account: " + parts[0]);
                    }
                    accounts.put(parts[0], parts[1]);
                }
                in.close();
            } catch (final FileNotFoundException e) {// From File Loading
                e.printStackTrace();
            } catch (final IOException e) {// From Reader traversal
                e.printStackTrace();
            }
        }
        return accounts;
    }

    /**
     * Writes the accounts to the file
     */
    private static void saveAccounts() {
        final File accountFile = new File(AccountManager.FILE_NAME);
        try {
            final BufferedWriter out = new BufferedWriter(new FileWriter(accountFile));
            for (final String s : AccountManager.accounts.keySet()) {
                if (AccountManager.accounts.get(s).isEmpty()) {
                    continue;
                }
                out.append(s).append(AccountManager.SEP_CHAR).append(AccountManager.accounts.get(s));
                out.newLine();
            }
            out.close();
        } catch (final IOException e) {
            e.printStackTrace();
        }
    }

    /*
      * Returns SHA1 hash of a String.
      */

    private static byte[] SHA1(final String in) throws NoSuchAlgorithmException, UnsupportedEncodingException {
        MessageDigest md = MessageDigest.getInstance("SHA-1");
        md.update(in.getBytes("iso-8859-1"), 0, in.length());
        return md.digest();
    }

    private AccountManager() {
        super(Frame.getFrames()[0], "Account Manager", true);
        if (AccountManager.accounts.size() < 1) {
            AccountManager.accounts.put(emptyName, "");
        }
    }

    /**
     * Controls all of the button and checkBox actions of the program
     */
    public void actionPerformed(final ActionEvent event) {
        JDialog parentFrame;
        Component comp = ((Component) event.getSource()).getParent();
        while (!(comp instanceof JDialog)) {
            comp = comp.getParent();
        }
        parentFrame = (JDialog) comp;
        if (event.getSource() instanceof JButton) {
            switch (((JButton) event.getSource()).getText().charAt(0)) {
                case 'A':// Add Accounts
                    if (event.getActionCommand().contains("dd")) {
                        addAccount();
                    } else {// Update the List with the new Account
                        final JPanel pane = (JPanel) ((JButton) event.getSource()).getParent();
                        String name = null, pass = null, pin = "";
                        for (final Component c : pane.getComponents()) {
                            if (c instanceof JPasswordField) {
                                pass = ((JTextField) c).getText();
                            } else if (c instanceof JTextField) {
                                name = ((JTextField) c).getText();
                            } else if (c instanceof JSpinner) {
                                pin += ((JSpinner) c).getValue();
                            }
                        }
                        if (name.isEmpty() || pass.isEmpty()) {
                            JOptionPane.showMessageDialog(null, "Empty Fields");
                            return;
                        }
                        AccountManager.accounts.put(AccountManager.fixName(name), AccountManager.crypt(pass, true) + (pin.isEmpty() ? "" : AccountManager.SEP_CHAR + pin));
                        refreshList();
                        names.validate();
                        parentFrame.dispose();
                    }
                    break;
                case 'D':// Delete Accounts
                    if (AccountManager.accounts.get(names.getSelectedValue()).isEmpty())
                        return;
                    final int yes = JOptionPane.showConfirmDialog(this, "Are you sure you want to delete " + names.getSelectedValue() + "?", "Delete", JOptionPane.YES_NO_OPTION);
                    if (yes == 0) {
                        AccountManager.accounts.remove(names.getSelectedValue());
                        refreshList();
                        names.validate();
                    }
                    break;
                case 'S':// Save Accounts
                    AccountManager.saveAccounts();
                    break;
            }
        } else if (event.getSource() instanceof JCheckBox) {
            // CheckBox for Pins add and remove JSpinners
            final JCheckBox jcb = (JCheckBox) event.getSource();
            if (jcb.isSelected()) {
                // Add Spinners
                c = new GridBagConstraints();
                c.gridy = 2;
                c.gridx = 1;
                c.weightx = 1;
                c.insets = new Insets(0, 0, 5, 5);
                final JSpinner[] spins = new JSpinner[4];
                for (int i = 0; i < spins.length; i++) {
                    spins[i] = new JSpinner(new SpinnerNumberModel(0, 0, 9, 1));
                    jcb.getParent().add(spins[i], c);
                    c.gridx = GridBagConstraints.RELATIVE;
                }
                parentFrame.pack();
            } else {
                // Remove Spinners
                for (final Component c : jcb.getParent().getComponents()) {
                    if (c instanceof JSpinner) {
                        jcb.getParent().remove(c);
                    }
                }
                parentFrame.validate();
            }
        }
    }

    /**
     * Gets the necessary information from the user to make a new account
     */
    private void addAccount() {
        final JDialog addFrame = new JDialog(this, "New", true);
        final JPanel pane = new JPanel(new GridBagLayout());
        c = new GridBagConstraints();
        final JTextField userName = new JTextField(8);
        final JPasswordField userPass = new JPasswordField(8);
        userPass.setEchoChar('*');
        c.gridwidth = 2;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new Insets(5, 5, 0, 7);
        pane.add(new JLabel("User Name: ", SwingConstants.CENTER), c);
        c.gridx = GridBagConstraints.RELATIVE;
        c.gridwidth = 3;
        c.insets = new Insets(5, 0, 0, 5);
        pane.add(userName, c);
        c.insets = new Insets(5, 5, 5, 7);
        c.gridwidth = 2;
        c.gridx = 0;
        c.gridy = 1;
        pane.add(new JLabel("Password: ", SwingConstants.CENTER), c);
        c.insets = new Insets(5, 0, 5, 5);
        c.gridwidth = 3;
        c.gridx = GridBagConstraints.RELATIVE;
        pane.add(userPass, c);
        final JCheckBox pinCheck = new JCheckBox("Pin");
        pinCheck.addActionListener(this);
        c = new GridBagConstraints();
        c.gridy = 2;
        c.insets = new Insets(0, 5, 5, 5);
        pane.add(pinCheck, c);

        final JButton save = new JButton("Add");
        save.setActionCommand("Update");
        save.addActionListener(this);
        c.gridy = 3;
        c.gridwidth = GridBagConstraints.REMAINDER;
        pane.add(save, c);

        addFrame.setResizable(false);
        addFrame.add(pane);
        addFrame.pack();
        addFrame.setLocationRelativeTo(this);
        addFrame.setVisible(true);
    }

    /**
     * Resets the Model for the List to reflect changes made to the accounts Map
     */
    private void refreshList() {
        if ((AccountManager.accounts.keySet().size() > 1) && AccountManager.accounts.keySet().contains(emptyName)) {
            AccountManager.accounts.remove(emptyName);
        } else if (AccountManager.accounts.keySet().size() < 1) {
            AccountManager.accounts.put(emptyName, "");
        }
        names.setListData(AccountManager.accounts.keySet().toArray(new String[AccountManager.accounts.size()]));
        names.getParent().validate();
    }

    /**
     * Creates and Displays the main GUI
     * <p/>
     * This GUI has the list and the main buttons
     */
    public void showGUI() {
        getContentPane().removeAll();
        c = new GridBagConstraints();
        // Main Panel with everything
        final JPanel pane = new JPanel(new GridBagLayout());
        // Makes the List
        names = new JList(AccountManager.accounts.keySet().toArray(new String[AccountManager.accounts.size()]));
        // Only one selection at a time
        names.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        // Will display the password and pin when double clicked or right
        // clicked
        names.addMouseListener(new MouseListener() {
            public void mouseClicked(final MouseEvent click) {
                if ((click.getClickCount() > 1) || (click.getButton() == MouseEvent.BUTTON3)) {
                    final String clicked = AccountManager.accounts.get(((JList) click.getComponent()).getSelectedValue());
                    if (clicked.isEmpty())
                        return;
                    String pass = clicked.contains(AccountManager.SEP_CHAR) ? clicked.substring(0, clicked.indexOf(AccountManager.SEP_CHAR)) : clicked;
                    pass = AccountManager.crypt(pass, false);
                    String info = "Password: " + pass;
                    if (clicked.contains(AccountManager.SEP_CHAR)) {
                        info += "\nPin: " + clicked.substring(clicked.indexOf(AccountManager.SEP_CHAR) + 1, clicked.length());
                    }
                    JOptionPane.showMessageDialog(null, info);
                }
            }

            public void mouseEntered(final MouseEvent click) {
            }

            public void mouseExited(final MouseEvent click) {
            }

            public void mousePressed(final MouseEvent click) {
            }

            public void mouseReleased(final MouseEvent click) {
            }
        });

        // Enables scrolling through the List
        final JScrollPane scroll = new JScrollPane(names);

        final JButton add = new JButton("Add");// Button 1
        final JButton del = new JButton("Delete");// Button 2
        final JButton save = new JButton("Save");// Button 3

        add.addActionListener(this);
        del.addActionListener(this);
        save.addActionListener(this);
        // Positions Everything Correctly on the panel
        c.gridy = 0;
        c.gridwidth = GridBagConstraints.REMAINDER;
        c.fill = GridBagConstraints.BOTH;
        pane.add(scroll, c);
        c.gridwidth = 1;
        c.gridy = 1;
        c.gridx = 0;
        c.fill = GridBagConstraints.NONE;
        c.insets = new Insets(5, 5, 5, 5);
        c.anchor = GridBagConstraints.CENTER;
        pane.add(add, c);
        c.gridx = GridBagConstraints.RELATIVE;
        pane.add(del, c);
        pane.add(save, c);
        names.setSelectedIndex(0);
        add(pane);
        pack();
        setLocationRelativeTo(getOwner());
        setVisible(true);
        setResizable(false);
	}
}