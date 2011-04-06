package org.rsbot.gui;

import org.rsbot.service.ScriptBoxSource;
import org.rsbot.util.GlobalConfiguration;

import javax.swing.*;
import java.awt.*;

/**
 * @author Jacmob
 */
public class LoginDialog extends JDialog {

	/**
	 * Gui Dialog
	 */
	private static final long serialVersionUID = -7421702904004119500L;
	static final ScriptBoxSource.Credentials CREDENTIALS = new ScriptBoxSource.Credentials();

	public LoginDialog(Frame parent) {
		super(parent, GlobalConfiguration.SITE_NAME + " Login");
	}

}
