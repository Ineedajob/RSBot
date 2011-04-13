package org.rsbot.gui;

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
 */
public class LoginDialog extends JDialog {

	/**
	 * Gui Dialog
	 */
	private static final long serialVersionUID = -7421702904004119500L;
	public static final ScriptBoxSource.Credentials CREDENTIALS = new ScriptBoxSource.Credentials();
	private JPanel a, b;
	private JTextField d;
	private JLabel c, e, g;
	private JPasswordField f;
	private JButton h;

	public LoginDialog(Frame parent) {
		super(parent, GlobalConfiguration.SITE_NAME + " Login");
		a = new JPanel();
		b = new JPanel();
		c = new JLabel();
		d = new JTextField();
		e = new JLabel();
		f = new JPasswordField();
		g = new JLabel();
		h = new JButton();
		setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		setResizable(false);
		setMinimumSize(new Dimension(200, 130));
		setAlwaysOnTop(true);
		Container localContainer = getContentPane();
		localContainer.setLayout(new BorderLayout(5, 0));
		a.setMaximumSize(new Dimension(170, 100));
		a.setMinimumSize(new Dimension(170, 100));
		a.setPreferredSize(new Dimension(170, 100));
		a.setLayout(new BorderLayout());
		localContainer.add(a);
		b.setMaximumSize(new Dimension(170, 70));
		b.setMinimumSize(new Dimension(170, 70));
		b.setPreferredSize(new Dimension(170, 70));
		b.setLayout(new GridBagLayout());
		c.setText("Username:");
		c.setLabelFor(d);
		c.setHorizontalAlignment(11);
		b.add(c, new GridBagConstraints(0, 0, 1, 1, 0.0D, 0.0D, 10, 1, new Insets(0, 0, 5, 5), 0, 0));
		d.setColumns(12);
		b.add(d, new GridBagConstraints(1, 0, 1, 1, 0.0D, 0.0D, 10, 1, new Insets(0, 0, 5, 0), 0, 0));
		e.setText("Password:");
		e.setLabelFor(f);
		e.setHorizontalAlignment(11);
		b.add(e, new GridBagConstraints(0, 1, 1, 1, 0.0D, 0.0D, 10, 1, new Insets(0, 0, 5, 5), 0, 0));
		f.setColumns(12);
		b.add(f, new GridBagConstraints(1, 1, 1, 1, 0.0D, 0.0D, 10, 1, new Insets(0, 0, 5, 0), 0, 0));
		g.setText("Register");
		g.setHorizontalTextPosition(0);
		g.setHorizontalAlignment(0);
		b.add(g, new GridBagConstraints(0, 2, 1, 1, 0.0D, 0.0D, 10, 1, new Insets(0, 0, 0, 5), 0, 0));
		h.setText("Login");
		b.add(h, new GridBagConstraints(1, 2, 1, 1, 0.0D, 0.0D, 10, 1, new Insets(0, 0, 0, 0), 0, 0));
		a.add(b);
		h.setFocusable(false);
		h.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				CREDENTIALS.username = d.getText();
				CREDENTIALS.password = new String(f.getPassword());
				dispose();
			}
		});
		f.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
			}
		});
		g.addMouseListener(new MouseAdapter() {
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
		setVisible(true);
		requestFocus();
	}

	public ScriptBoxSource.Credentials getCredentials() {
		return CREDENTIALS;
	}

}
