package org.rsbot.gui;

import org.rsbot.service.LoginManager;
import org.rsbot.service.ScriptBoxSource;
import org.rsbot.util.GlobalConfiguration;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

/**
 * @author Timer
 * @author Aut0r
 */
public class LoginDialog extends JDialog {

	private static final long serialVersionUID = -7421702904004119500L;
	public static final ScriptBoxSource.Credentials CREDENTIALS = new ScriptBoxSource.Credentials();
	private final JPanel masterPane, loginPane, infoPane, subinfoPane;
	private final JTextField usernameField;
	private final JLabel usernameLabel, passwordLabel, registerLabel, infoLabel;
	private final JPasswordField passwordField;
	private final JButton loginButton;
	private String displayMessage = "Please enter your login details.";

	public LoginDialog(final Frame parent) {
		super(parent, GlobalConfiguration.SITE_NAME + " Login");
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (final Exception ignored) {
		}
		setIconImage(GlobalConfiguration.getImage(
				GlobalConfiguration.Paths.Resources.ICON,
				GlobalConfiguration.Paths.ICON));
		masterPane = new JPanel();
		infoPane = new JPanel();
		subinfoPane = new JPanel();
		loginPane = new JPanel();
		usernameLabel = new JLabel();
		usernameField = new JTextField();
		passwordLabel = new JLabel();
		infoLabel = new JLabel();
		passwordField = new JPasswordField();
		registerLabel = new JLabel();
		loginButton = new JButton();
		setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				dispose();
				System.exit(0);
			}
		});
		setResizable(false);
		setMinimumSize(new Dimension(170, 140));
		setMaximumSize(new Dimension(170, 140));
		setPreferredSize(new Dimension(170, 140));
		setAlwaysOnTop(true);
		setLayout(new BorderLayout());
		masterPane.setMaximumSize(new Dimension(170, 80));
		masterPane.setMinimumSize(new Dimension(170, 80));
		masterPane.setPreferredSize(new Dimension(170, 80));
		masterPane.setLayout(new BorderLayout());
		add(masterPane, BorderLayout.NORTH);
		loginPane.setMaximumSize(new Dimension(170, 78));
		loginPane.setMinimumSize(new Dimension(170, 78));
		loginPane.setPreferredSize(new Dimension(170, 78));
		loginPane.setLayout(new GridBagLayout());
		infoPane.setMinimumSize(new Dimension(170, 20));
		infoPane.setMinimumSize(new Dimension(170, 20));
		infoPane.setPreferredSize(new Dimension(170, 20));
		infoPane.setLayout(new BorderLayout());
		add(infoPane, BorderLayout.SOUTH);
		subinfoPane.setMinimumSize(new Dimension(170, 12));
		subinfoPane.setMaximumSize(new Dimension(170, 12));
		subinfoPane.setPreferredSize(new Dimension(170, 12));
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
		masterPane.add(loginPane, BorderLayout.NORTH);
		loginButton.setFocusable(false);
		subinfoPane.setLayout(new GridBagLayout());
		infoLabel.setText("<html><u>Help</u></html>");
		infoLabel.setToolTipText("Click here for help!");
		subinfoPane.add(infoLabel, new GridBagConstraints(0, 2, 1, 1, 0.0D, 0.0D, 10, 1, new Insets(0, 0, 0, 5), 0, 0));
		infoPane.add(subinfoPane, BorderLayout.NORTH);
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
					Runtime.getRuntime().exec("cmd /c start http://www.powerbot.org/vb/register.php");
				} catch (Exception f) {
					f.printStackTrace();
				}
			}
		});
		infoLabel.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseReleased(MouseEvent e) {
				setVisible(false);
				JOptionPane.showMessageDialog(parent,
				                              "You must enter your forum login to enable the bot\nand decrypt your " +
						                              "user accounts.  The new system encrypts your accounts file\n" +
						                              "using your login, thus making your accounts virtually\n" +
						                              "un-hackable.  Soon, this information will also be used for\n" +
						                              "script box upon release, please keep in mind if you enter " +
						                              "your\ninformation wrong your account info will be cleared " +
						                              "(until\nthe information is checked to the server).",
				                              "Information", JOptionPane.OK_OPTION);
				setVisible();
			}
		});
		pack();
	}

	public void setVisible() {
		setLocationRelativeTo(getOwner());
		setVisible(true);
		setAlwaysOnTop(true);
		requestFocus();
	}

	public ScriptBoxSource.Credentials getCredentials() {
		return CREDENTIALS;
	}

}