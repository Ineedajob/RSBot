package org.rsbot.service;

/**
 * @author Timer
 */
public class LoginManager {

	private String message = null;
	private static final String MESSAGE_GOOD_LOGIN = "Identified correctly.";

	/**
	 * Checks if your username and password is valid.
	 *
	 * @return <tt>true</tt> if correct; otherwise <tt>false</tt>.
	 */
	public boolean valid() {
		message = MESSAGE_GOOD_LOGIN;
		return true;
	}

	/**
	 * Return message.
	 *
	 * @return The login message.
	 */
	public String message() {
		return message;
	}

}
