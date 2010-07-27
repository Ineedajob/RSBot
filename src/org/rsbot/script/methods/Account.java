package org.rsbot.script.methods;

import org.rsbot.gui.AccountManager;

/**
 * Selected account information.
 */
public class Account extends MethodProvider {

	public Account(MethodContext ctx) {
		super(ctx);
	}
	
    /**
     * @return The currently selected account's name.
     */
	public String getName() {
		return methods.bot.getAccountName();
	}
	
	/**
	 * @return The currently selected account's password.
	 */
	public String getPassword() {
		return getName() == null ? null : AccountManager.getPassword(getName());
	}
	
	/**
	 * @return The currently selected account's pin,
	 * or <code>null</code>.
	 */
	public String getPin() {
		return getName() == null ? null : AccountManager.getPin(getName());
	}
	
}
