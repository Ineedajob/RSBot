package org.rsbot.gui;

import org.rsbot.service.LoginManager;
import org.rsbot.service.ScriptBoxSource;
import org.rsbot.util.GlobalConfiguration;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

/**
 * @author Timer
 */
public class LoginDialog extends JDialog {

	private static final long serialVersionUID = -7421702904004119500L;
	public static final ScriptBoxSource.Credentials CREDENTIALS = new ScriptBoxSource.Credentials();
	private JPanel masterPane, loginPane, infoPane;
	private JTextField usernameField;
	private JTextPane textPane;
	private JLabel usernameLabel, passwordLabel, registerLabel;
	private JPasswordField passwordField;
	private JButton loginButton;
	private String displayMessage = "Please enter your login details.";

	public LoginDialog(Frame parent) {
		super(parent, GlobalConfiguration.SITE_NAME + " Login");
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (final Exception ignored) {
		}
		setIconImage(GlobalConfiguration.getImage(
				GlobalConfiguration.Paths.Resources.ICON,
				GlobalConfiguration.Paths.ICON));
		masterPane = new JPanel();
		loginPane = new JPanel();
		usernameLabel = new JLabel();
		usernameField = new JTextField();
		passwordLabel = new JLabel();
		passwordField = new JPasswordField();
		registerLabel = new JLabel();
		loginButton = new JButton();
		infoPane = new JPanel();
		textPane = new JTextPane();
		setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				dispose();
				System.exit(0);
			}
		});
		setResizable(false);
		setMinimumSize(new Dimension(250, 200));
		setAlwaysOnTop(true);
		setLayout(new BorderLayout());
		masterPane.setMaximumSize(new Dimension(200, 70));
		masterPane.setMinimumSize(new Dimension(200, 70));
		masterPane.setPreferredSize(new Dimension(200, 70));
		masterPane.setLayout(new BorderLayout());
		add(masterPane, BorderLayout.WEST);
		loginPane.setMaximumSize(new Dimension(170, 70));
		loginPane.setMinimumSize(new Dimension(170, 70));
		loginPane.setPreferredSize(new Dimension(170, 70));
		loginPane.setLayout(new GridBagLayout());
		usernameLabel.setText("Username:");
		usernameLabel.setLabelFor(usernameField);
		usernameLabel.setHorizontalAlignment(11);
		loginPane.add(usernameLabel,
		              new GridBagConstraints(0, 0, 1, 1, 0.0D, 0.0D, 10, 1, new Insets(0, 0, 5, 5), 0, 0));
		usernameField.setColumns(12);
		loginPane.add(usernameField,
		              new GridBagConstraints(1, 0, 1, 1, 0.0D, 0.0D, 10, 1, new Insets(0, 0, 5, 0), 0, 0));
		passwordLabel.setText("Password:");
		passwordLabel.setLabelFor(passwordField);
		passwordLabel.setHorizontalAlignment(11);
		loginPane.add(passwordLabel,
		              new GridBagConstraints(0, 1, 1, 1, 0.0D, 0.0D, 10, 1, new Insets(0, 0, 5, 5), 0, 0));
		passwordField.setColumns(12);
		loginPane.add(passwordField,
		              new GridBagConstraints(1, 1, 1, 1, 0.0D, 0.0D, 10, 1, new Insets(0, 0, 5, 0), 0, 0));
		registerLabel.setText("<html><u>Register</u></html>");
		registerLabel.setHorizontalTextPosition(0);
		registerLabel.setHorizontalAlignment(0);
		registerLabel.setForeground(Color.blue);
		registerLabel.setToolTipText("Click here to register an account!");
		loginPane.add(registerLabel,
		              new GridBagConstraints(0, 2, 1, 1, 0.0D, 0.0D, 10, 1, new Insets(0, 0, 0, 5), 0, 0));
		loginButton.setText("Login");
		loginPane.add(loginButton, new GridBagConstraints(1, 2, 1, 1, 0.0D, 0.0D, 10, 1, new Insets(0, 0, 0, 0), 0, 0));
		masterPane.add(loginPane, BorderLayout.EAST);
		loginButton.setFocusable(false);
		infoPane.setMinimumSize(new Dimension(250, 100));
		infoPane.setMaximumSize(new Dimension(250, 200));
		infoPane.setPreferredSize(new Dimension(250, 100));
		infoPane.setLayout(new GridBagLayout());
		add(infoPane, BorderLayout.EAST);
		textPane.setText("Please login with your forum account to access the bot and decrypt your user accounts.  " +
				                 "The new encryption system uses your login to secure your accounts, you can trust your accounts in the " +
				                 "panel again!");
		textPane.setEditable(false);
		textPane.setMargin(new Insets(5, 5, 5, 5));
		textPane.setPreferredSize(new Dimension(230, 150));
		infoPane.add(textPane, new GridBagConstraints(0, 0, 1, 1, 0.0D, 0.0D, 10, 1, new Insets(0, 0, 5, 0), 0, 0));
		loginButton.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				CREDENTIALS.username = usernameField.getText();
				CREDENTIALS.password = new String(passwordField.getPassword());
				LoginManager lM = new LoginManager();
				if (lM.valid()) {
					dispose();
				} else {
					CREDENTIALS.username = "";
					passwordField.setText("");
					CREDENTIALS.password = "";
					displayMessage = lM.message();
				}
			}
		});
		passwordField.addKeyListener(new KeyAdapter() {
			public void keyReleased(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_ENTER) {
					CREDENTIALS.username = usernameField.getText();
					CREDENTIALS.password = new String(passwordField.getPassword());
					LoginManager lM = new LoginManager();
					if (lM.valid()) {
						dispose();
					} else {
						CREDENTIALS.username = "";
						passwordField.setText("");
						CREDENTIALS.password = "";
						displayMessage = lM.message();
					}
				}
			}
		});
		registerLabel.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseReleased(MouseEvent e) {
				try {
					Process p = Runtime.getRuntime().exec("cmd /c start http://www.powerbot.org/vb/register.php");
				} catch (Exception f) {
					f.printStackTrace();
				}
			}
		});
		pack();
	}

	public void setVisible() {
		setLocationRelativeTo(getOwner());
		setAlwaysOnTop(true);
		setVisible(true);
		requestFocus();
	}

	public ScriptBoxSource.Credentials getCredentials() {
		return CREDENTIALS;
	}

}