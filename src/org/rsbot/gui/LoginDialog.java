package org.rsbot.gui;

import org.rsbot.service.LoginManager;
import org.rsbot.service.ScriptBoxSource;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * @author Aut0r
 * @author fletch to 99
 */
public class LoginDialog extends JDialog {

	/**
	 * Gui Dialog
	 */
	private static final long serialVersionUID = -7421702904004119500L;
	public static final ScriptBoxSource.Credentials CREDENTIALS = new ScriptBoxSource.Credentials();
	private JLabel label1;
	private JLabel label2;
	private JPasswordField passwordField1;
	private JTextField textField1;
	private JButton button1;
	private JLabel label3;
	private JTextArea textArea1;
	private String displayMessage = "Please enter your login details.";

	public LoginDialog(Frame parent) {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (final Exception ignored) {
		}

		label1 = new JLabel();
		label2 = new JLabel();
		passwordField1 = new JPasswordField();
		textField1 = new JTextField();
		button1 = new JButton();
		label3 = new JLabel();
		textArea1 = new JTextArea();

		setTitle("Account Manager-Encrypter");
		setResizable(false);
		Container contentPane = getContentPane();
		contentPane.setLayout(null);

		label1.setText("Username:");
		label1.setFont(new Font("Tahoma", Font.PLAIN, 12));
		contentPane.add(label1);
		label1.setBounds(5, 25, label1.getPreferredSize().width, 20);

		label2.setText("Password:");
		label2.setFont(new Font("Tahoma", Font.PLAIN, 12));
		contentPane.add(label2);
		label2.setBounds(5, 50, label2.getPreferredSize().width, 20);
		contentPane.add(passwordField1);
		passwordField1.setBounds(65, 50, 75,
				passwordField1.getPreferredSize().height);
		contentPane.add(textField1);
		textField1.setBounds(65, 25, 75, textField1.getPreferredSize().height);

		button1.setText("Login");
		contentPane.add(button1);
		button1.setBounds(5, 75, 135, 25);

		label3.setText("Please Login to your Powerbot Account");
		label3.setFont(new Font("Tahoma", Font.BOLD, 14));
		label3.setHorizontalAlignment(SwingConstants.CENTER);
		contentPane.add(label3);
		label3.setBounds(0, 0, 465, 20);

		textArea1.setBackground(Color.lightGray);
		textArea1
				.setText("This username and password encrypts your account usersnames/passwords/pins so that your Runescape accounts are protected from being decrypted.  This username and password combination must be correct everytime you load RSBot.  If it isn't it will overwrite the existing Account Manager.");
		textArea1.setEditable(false);
		textArea1.setLineWrap(true);
		textArea1.setFont(new Font("Tahoma", Font.PLAIN, 11));
		textArea1.setWrapStyleWord(true);
		contentPane.add(textArea1);
		textArea1.setBounds(145, 25, 315, 75);

		contentPane.setPreferredSize(new Dimension(470, 130));
		setSize(470, 130);
		setLocationRelativeTo(getOwner());
		
		button1.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				CREDENTIALS.username = textField1.getText();
				CREDENTIALS.password = new String(passwordField1.getPassword());
				LoginManager lM = new LoginManager();
				if (lM.valid()) {
					dispose();
				} else {
					CREDENTIALS.username = "";
					passwordField1.setText("");
					CREDENTIALS.password = "";
					displayMessage = lM.message();
				}
			}
		});
		passwordField1.addActionListener(new ActionListener() {
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
