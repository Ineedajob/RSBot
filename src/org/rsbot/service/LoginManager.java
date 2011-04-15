package org.rsbot.service;

/**
 * @author Timer
 * @version 0.1
 */
public class LoginManager {

	private final ScriptBoxSource.Credentials cred;
	private String message = null;
	private static final String MESSAGE_GOOD_LOGIN = "Identified correctly.";

	/**
	 * Constructs a login manager.
	 *
	 * @param credentials Your login details.
	 */
	public LoginManager(final ScriptBoxSource.Credentials credentials) {
		cred = credentials;
	}

	/**
	 * Checks if your username and password is valid.
	 *
	 * @return <tt>true</tt> if correct; otherwise <tt>false</tt>.
	 */
	public boolean valid() {
		message = MESSAGE_GOOD_LOGIN;
		return true;//TODO .
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
