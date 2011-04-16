package org.rsbot.gui;

import org.rsbot.service.LoginManager;
import org.rsbot.service.ScriptBoxSource;
import org.rsbot.util.GlobalConfiguration;

import javax.swing.*;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * @author Timer
 * @author Aut0r
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
	private float version = (float) GlobalConfiguration.getVersion();

	public LoginDialog(Frame parent) {
		super(parent, GlobalConfiguration.SITE_NAME + " Login");
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (final Exception ignored) {
		}
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
		setResizable(false);
		setMinimumSize(new Dimension(450, 225));
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
		loginPane.add(usernameLabel, new GridBagConstraints(0, 0, 1, 1, 0.0D,
				0.0D, 10, 1, new Insets(0, 0, 5, 5), 0, 0));
		usernameField.setColumns(12);
		loginPane.add(usernameField, new GridBagConstraints(1, 0, 1, 1, 0.0D,
				0.0D, 10, 1, new Insets(0, 0, 5, 0), 0, 0));
		passwordLabel.setText("Password:");
		passwordLabel.setLabelFor(passwordField);
		passwordLabel.setHorizontalAlignment(11);
		loginPane.add(passwordLabel, new GridBagConstraints(0, 1, 1, 1, 0.0D,
				0.0D, 10, 1, new Insets(0, 0, 5, 5), 0, 0));
		passwordField.setColumns(12);
		loginPane.add(passwordField, new GridBagConstraints(1, 1, 1, 1, 0.0D,
				0.0D, 10, 1, new Insets(0, 0, 5, 0), 0, 0));
		// registerLabel.setText("Register");
		// registerLabel.setHorizontalTextPosition(0);
		// registerLabel.setHorizontalAlignment(0);
		// loginPane.add(registerLabel,
		// new GridBagConstraints(0, 2, 1, 1, 0.0D, 0.0D, 10, 1, new Insets(0,
		// 0, 0, 5), 0, 0));
		loginButton.setText("Login");
		loginPane.add(loginButton, new GridBagConstraints(1, 2, 1, 1, 0.0D,
				0.0D, 10, 1, new Insets(0, 0, 0, 0), 0, 0));
		masterPane.add(loginPane, BorderLayout.EAST);
		loginButton.setFocusable(false);
		infoPane.setMinimumSize(new Dimension(250, 100));
		infoPane.setMaximumSize(new Dimension(250, 200));
		infoPane.setPreferredSize(new Dimension(250, 100));
		infoPane.setLayout(new GridBagLayout());
		add(infoPane, BorderLayout.EAST);
		textPane.setText("RSBot v"
				+ version
				/ 100
				+ "\n  This is a local user/pass combination that encrypts your account manager file and its contents.  If you forget this user/pass combination you will not be able to access your accounts from account manager it will be wiped and you must re-enter the data.  Without this combination no one can decrypt your account manager. \n                           RSBot Dev Team");
		textPane.setEditable(false);
		textPane.setMargin(new Insets(5, 5, 5, 5));
		textPane.setPreferredSize(new Dimension(230, 160));
		infoPane.add(textPane, new GridBagConstraints(0, 0, 1, 1, 0.0D, 0.0D,
				10, 1, new Insets(0, 0, 5, 0), 0, 0));
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
		passwordField.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
			}
		});
		// registerLabel.addMouseListener(new MouseAdapter() {
		// @Override
		// public void mouseReleased(MouseEvent e) {
		// try {
		// Process p =
		// Runtime.getRuntime().exec("cmd /c start http://www.powerbot.org/vb/register.php");
		// } catch (Exception f) {
		// f.printStackTrace();
		// }
		// }
		// });
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