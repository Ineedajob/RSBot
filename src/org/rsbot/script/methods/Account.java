package org.rsbot.script.methods;

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

}
