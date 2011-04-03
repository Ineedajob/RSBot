package org.rsbot.gui;

import java.awt.Frame;

import javax.swing.JDialog;

import org.rsbot.service.ScriptBoxSource;
import org.rsbot.util.GlobalConfiguration;

/**
 * @author Jacmob
 */
@SuppressWarnings("serial")
public class LoginDialog extends JDialog {

	static final ScriptBoxSource.Credentials CREDENTIALS = new ScriptBoxSource.Credentials();

	public LoginDialog(Frame parent) {
		super(parent, GlobalConfiguration.SITE_NAME + " Login");
	}

}
