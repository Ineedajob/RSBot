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
	 * @return The currently selected account's pin, or <code>null</code>.
	 */
	public String getPin() {
		return getName() == null ? null : AccountManager.getPin(getName());
	}

	/**
	 * @return <tt>true</tt> if the currently selected account is a member.
	 */
	public boolean isMember() {
		return AccountManager.isMember(this.getName());
	}

	/**
	 * @return <tt>true</tt> if the currently selected account may take breaks.
	 */
	public boolean isTakingBreaks() {
		return AccountManager.isTakingBreaks(getName());
	}

	/**
	 * @return The currently selected account's reward choice, or
	 *         <code>null</code>.
	 */
	public String getPreferredReward() {
		return getName() == null ? null : AccountManager.getReward(getName());
	}

}
