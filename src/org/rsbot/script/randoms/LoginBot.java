package org.rsbot.script.randoms;

import org.rsbot.gui.AccountManager;
import org.rsbot.script.Random;
import org.rsbot.script.ScriptManifest;
import org.rsbot.script.wrappers.RSComponent;
import org.rsbot.script.wrappers.RSInterface;

import java.awt.*;
import java.awt.event.KeyEvent;

/**
 * @author Iscream, Aut0r, Doout, Pervy
 */
@ScriptManifest(authors = {"Iscream", "Pervy Shuya", "Aut0r"}, name = "Login", version = 2.0)
public class LoginBot extends Random {

	private static final int INTERFACE_MAIN = 905;
	private static final int INTERFACE_MAIN_CHILD = 59;
	private static final int INTERFACE_MAIN_CHILD_COMPONENT_ID = 4;
	private static final int INTERFACE_LOGIN_SCREEN = 596;
	private static final int INTERFACE_USERNAME = 65;
	private static final int INTERFACE_USERNAME_WINDOW = 37;
	private static final int INTERFACE_PASSWORD = 71;
	private static final int INTERFACE_PASSWORD_WINDOW = 39;
	private static final int INTERFACE_BUTTON_LOGIN = 42;
	private static final int INTERFACE_TEXT_RETURN = 11;
	private static final int INTERFACE_BUTTON_BACK = 60;
	private static final int INTERFACE_WELCOME_SCREEN = 906;
	private static final int INTERFACE_WELCOME_SCREEN_BUTTON_PLAY_1 = 160;
	private static final int INTERFACE_WELCOME_SCREEN_BUTTON_PLAY_2 = 171;
	//private static final int INTERFACE_WELCOME_SCREEN_BUTTON_LOGOUT = 193;
	private static final int INTERFACE_WELCOME_SCREEN_TEXT_RETURN = 221;
	private static final int INTERFACE_WELCOME_SCREEN_BUTTON_BACK = 218;
	private static final int INTERFACE_WELCOME_SCREEN_HIGH_RISK_WORLD_TEXT = 86;
	private static final int INTERFACE_WELCOME_SCREEN_HIGH_RISK_WORLD_LOGIN_BUTTON = 93;
	private static final int INTERFACE_GRAPHICS_NOTICE = 976;
	private static final int INTERFACE_GRAPHICS_LEAVE_ALONE = 6;

	private static final int INDEX_LOGGED_OUT = 3;
	private static final int INDEX_LOBBY = 7;

	private int invalidCount, worldFullCount;

	public Random Rand;

	@Override
	public boolean activateCondition() {
		int idx = game.getClientState();
		return (idx == INDEX_LOGGED_OUT || idx == INDEX_LOBBY)
				&& !switchingWorlds() && account.getName() != null;
	}

	@Override
	public int loop() {
		String username = account.getName().toLowerCase().trim();
		String returnText = interfaces.get(INTERFACE_LOGIN_SCREEN)
				.getComponent(INTERFACE_TEXT_RETURN).getText().toLowerCase();
		int textlength;
		if (game.getClientState() != INDEX_LOGGED_OUT) {
			if (!game.isWelcomeScreen()) {
				sleep(random(1000, 2000));
			}
			if (game.getClientState() == INDEX_LOBBY) {
				RSInterface welcome_screen = interfaces
						.get(INTERFACE_WELCOME_SCREEN);
				RSComponent welcome_screen_button_play_1 = welcome_screen
						.getComponent(INTERFACE_WELCOME_SCREEN_BUTTON_PLAY_1);
				RSComponent welcome_screen_button_play_2 = welcome_screen
						.getComponent(INTERFACE_WELCOME_SCREEN_BUTTON_PLAY_2);

				mouse.click(welcome_screen_button_play_1.getAbsoluteX(),
						welcome_screen_button_play_1.getAbsoluteY(),
						welcome_screen_button_play_2.getAbsoluteX()
								+ welcome_screen_button_play_2.getWidth()
								- welcome_screen_button_play_1.getAbsoluteX(),
						welcome_screen_button_play_1.getHeight(), true);

				for (int i = 0; i < 4 && game.getClientState() == 6; i++) {
					sleep(500);
				}
				returnText = interfaces.get(INTERFACE_WELCOME_SCREEN)
						.getComponent(INTERFACE_WELCOME_SCREEN_TEXT_RETURN)
						.getText().toLowerCase();

				if (returnText.contains("total skill level of")
						&& !AccountManager.isMember(account.getName())) {
					log("Log back in when you total level of 1000+");
					interfaces.getComponent(INTERFACE_WELCOME_SCREEN,
							INTERFACE_WELCOME_SCREEN_BUTTON_BACK).doClick();
					stopScript(false);
				} else if (returnText.contains("total skill level of")
						&& AccountManager.isMember(account.getName())) {
					log("Log back in when you total level of 1500+");
					interfaces.getComponent(INTERFACE_WELCOME_SCREEN,
							INTERFACE_WELCOME_SCREEN_BUTTON_BACK).doClick();
					stopScript(false);
				}
		        if (interfaces.get(INTERFACE_WELCOME_SCREEN).getComponent(INTERFACE_WELCOME_SCREEN_BUTTON_BACK).isValid()) {
		        	         interfaces.get(INTERFACE_WELCOME_SCREEN).getComponent(INTERFACE_WELCOME_SCREEN_BUTTON_BACK).doClick();
		        } 

				if (returnText.contains("login limit exceeded")) {
					if (interfaces.getComponent(INTERFACE_WELCOME_SCREEN_BUTTON_BACK).isValid()) {
						interfaces.getComponent(INTERFACE_WELCOME_SCREEN_BUTTON_BACK).doClick();
					}
				}

				if (returnText.contains("your account has not logged out")) {
				interfaces.getComponent(INTERFACE_WELCOME_SCREEN,
						INTERFACE_WELCOME_SCREEN_BUTTON_BACK).doClick();
					if (invalidCount > 10) {
						log.warning("Unable to login after 10 attempts. Stopping script.");
						log.severe("It seems you are actually already logged in?");
						stopScript(false);
					}
					invalidCount++;
					log.severe("Waiting for logout..");
					sleep(5000, 15000);
				}

				if (returnText.contains("member")) {
					log("Unable to login to a members world. Stopping script.");
					RSComponent back_button1 = interfaces.get(
							INTERFACE_WELCOME_SCREEN).getComponent(228);
					RSComponent back_button2 = interfaces.get(
							INTERFACE_WELCOME_SCREEN).getComponent(231);
					mouse.click(
							back_button1.getAbsoluteX(),
							back_button1.getAbsoluteY(),
							back_button2.getAbsoluteX()
									+ back_button2.getWidth()
									- back_button1.getAbsoluteX(),
							back_button1.getHeight(), true);
					interfaces.get(INTERFACE_WELCOME_SCREEN).getComponent(203)
							.doClick();
					stopScript(false);
				}

				if (interfaces
						.get(INTERFACE_WELCOME_SCREEN)
						.getComponent(
								INTERFACE_WELCOME_SCREEN_HIGH_RISK_WORLD_TEXT)
						.getText().toLowerCase().trim()
						.contains("high-risk wilderness world")) {
					interfaces
							.get(INTERFACE_WELCOME_SCREEN)
							.getComponent(
									INTERFACE_WELCOME_SCREEN_HIGH_RISK_WORLD_LOGIN_BUTTON)
							.doClick();
				}
			}
			return -1;
		}
		if (!game.isLoggedIn()) {
			if (interfaces.get(INTERFACE_LOGIN_SCREEN).getComponent(INTERFACE_BUTTON_BACK).isValid()) {
				interfaces.get(INTERFACE_LOGIN_SCREEN).getComponent(INTERFACE_BUTTON_BACK).doClick();
			}
			if (returnText.contains("no reply from login server")) {
				if (invalidCount > 10) {
					log.warning("Unable to login after 10 attempts. Stopping script.");
					log.severe("It seems the login server is down.");
					stopScript(false);
				}
				invalidCount++;
				return random(500, 2000);
			}
			if (returnText.contains("update")) {
				log("Runescape has been updated, please reload RSBot.");
				stopScript(false);
			}
			if (returnText.contains("disable")) {
				log.severe("Your account is banned/disabled.");
				stopScript(false);
			}
			if (returnText.contains("your account has not logged out")) {
			  interfaces.get(INTERFACE_LOGIN_SCREEN).getComponent(INTERFACE_BUTTON_BACK).doClick(); 
				if (invalidCount > 10) {
					log.warning("Unable to login after 10 attempts. Stopping script.");
					log.severe("It seems you are actually already logged in?");
					stopScript(false);
				}
				invalidCount++;
				log.severe("Waiting for logout..");
				sleep(5000, 15000);
			}
			if (returnText.contains("incorrect")) {
				log.warning("Failed to login five times in a row. Stopping script.");
				stopScript(false);
			}
			if (returnText.contains("invalid")) {
				if (invalidCount > 6) {
					log.warning("Unable to login after 6 attempts. Stopping script.");
					log("Please verify that your RSBot account profile is correct.");
					stopScript(false);
				}
				invalidCount++;
				return random(500, 2000);
			}
			if (returnText.contains("error connecting")) {
				interfaces.get(INTERFACE_LOGIN_SCREEN)
						.getComponent(INTERFACE_BUTTON_BACK).doClick();
				stopScript(false);
				return random(500, 2000);
			}
			if (returnText.contains("full")) {
				if (worldFullCount > 30) {
					log("World Is Full. Waiting for 15 seconds.");
					sleep(random(10000, 15000));
					worldFullCount = 0;
				}
				sleep(random(1000, 1200));
				worldFullCount++;
			}
			if (returnText.contains("login limit exceeded")) {
				if (invalidCount > 10) {
					log.warning("Unable to login after 10 attempts. Stopping script.");
					log.severe("It seems you are actually already logged in?");
					stopScript(false);
				}
				invalidCount++;
				sleep(5000, 15000);
			}
			if (returnText.contains("world")) {
				return random(1500, 2000);
			}
			if (returnText.contains("performing login")) {
				return random(1500, 2000);
			}
		}
		if (game.getClientState() == INDEX_LOGGED_OUT) {
			if (interfaces.getComponent(INTERFACE_GRAPHICS_NOTICE,
					INTERFACE_GRAPHICS_LEAVE_ALONE).isValid()) {
				interfaces.getComponent(INTERFACE_GRAPHICS_NOTICE,
						INTERFACE_GRAPHICS_LEAVE_ALONE).doClick();
				if (interfaces.getComponent(INTERFACE_BUTTON_BACK).isValid()) {
					interfaces.getComponent(INTERFACE_BUTTON_BACK).doClick();
				}
				return random(500, 600);
			}
			if (!atLoginScreen()) {
				interfaces.getComponent(INTERFACE_MAIN, INTERFACE_MAIN_CHILD)
						.getComponent(INTERFACE_MAIN_CHILD_COMPONENT_ID)
						.doAction("");
				return random(500, 600);
			}
			if (isUsernameFilled() && isPasswordFilled()) {
				if (random(0, 2) == 0) {
					keyboard.pressKey((char) KeyEvent.VK_ENTER);
				} else {
					interfaces.get(INTERFACE_LOGIN_SCREEN)
							.getComponent(INTERFACE_BUTTON_LOGIN).doClick();
				}
				return random(500, 600);
			}
			if (!isUsernameFilled()) {
				atLoginInterface(interfaces.get(INTERFACE_LOGIN_SCREEN)
						.getComponent(INTERFACE_USERNAME_WINDOW));
				sleep(random(500, 700));
				textlength = interfaces.get(INTERFACE_LOGIN_SCREEN)
						.getComponent(INTERFACE_USERNAME).getText().length()
						+ random(3, 5);
				for (int i = 0; i <= textlength + random(1, 5); i++) {
					keyboard.sendText("\b", false);
					if (random(0, 2) == 1) {
						sleep(random(25, 100));
					}
				}
				keyboard.sendText(username, false);
				return random(500, 600);
			}
			if (isUsernameFilled() && !isPasswordFilled()) {
				atLoginInterface(interfaces.get(INTERFACE_LOGIN_SCREEN)
						.getComponent(INTERFACE_PASSWORD_WINDOW));
				sleep(random(500, 700));
				textlength = interfaces.get(INTERFACE_LOGIN_SCREEN)
						.getComponent(INTERFACE_PASSWORD).getText().length()
						+ random(3, 5);
				for (int i = 0; i <= textlength + random(1, 5); i++) {
					keyboard.sendText("\b", false);
					if (random(0, 2) == 1) {
						sleep(random(25, 100));
					}
				}
				keyboard.sendText(AccountManager.getPassword(account.getName()), false);
			}
		}
		return random(500, 2000);
	}

	private boolean switchingWorlds() {
		return interfaces.get(INTERFACE_WELCOME_SCREEN)
				.getComponent(INTERFACE_WELCOME_SCREEN_TEXT_RETURN).isValid()
				&& interfaces.get(INTERFACE_WELCOME_SCREEN)
				.getComponent(INTERFACE_WELCOME_SCREEN_TEXT_RETURN)
				.containsText("just left another world");
	}

	// Clicks past all of the letters
	private boolean atLoginInterface(RSComponent i) {
		if (!i.isValid()) {
			return false;
		}
		Rectangle pos = i.getArea();
		if (pos.x == -1 || pos.y == -1 || pos.width == -1 || pos.height == -1) {
			return false;
		}
		int dy = (int) (pos.getHeight() - 4) / 2;
		int maxRandomX = (int) (pos.getMaxX() - pos.getCenterX());
		int midx = (int) (pos.getCenterX());
		int midy = (int) (pos.getMinY() + pos.getHeight() / 2);
		if (i.getIndex() == INTERFACE_PASSWORD_WINDOW) {
			mouse.click(minX(i), midy + random(-dy, dy), true);
		} else {
			mouse.click(midx + random(1, maxRandomX), midy + random(-dy, dy),
					true);
		}
		return true;
	}

	/*
		  * Returns x int based on the letters in a Child Only the password text is
		  * needed as the username text cannot reach past the middle of the interface
		  */
	private int minX(RSComponent a) {
		int x = 0;
		Rectangle pos = a.getArea();
		int dx = (int) (pos.getWidth() - 4) / 2;
		int midx = (int) (pos.getMinX() + pos.getWidth() / 2);
		if (pos.x == -1 || pos.y == -1 || pos.width == -1 || pos.height == -1) {
			return 0;
		}
		for (int i = 0; i < interfaces.get(INTERFACE_LOGIN_SCREEN)
				.getComponent(INTERFACE_PASSWORD).getText().length(); i++) {
			x += 11;
		}
		if (x > 44) {
			return (int) (pos.getMinX() + x + 15);
		} else {
			return midx + random(-dx, dx);
		}
	}

	private boolean atLoginScreen() {
		return interfaces.get(596).isValid();
	}

	private boolean isUsernameFilled() {
		String username = account.getName().toLowerCase().trim();
		return interfaces.get(INTERFACE_LOGIN_SCREEN)
				.getComponent(INTERFACE_USERNAME).getText().toLowerCase()
				.equalsIgnoreCase(username);
	}

	private boolean isPasswordFilled() {
		String passWord = AccountManager.getPassword(account.getName());
		return interfaces.get(INTERFACE_LOGIN_SCREEN)
				.getComponent(INTERFACE_PASSWORD).getText().toLowerCase()
				.length() == (passWord == null ? 0 : passWord.length());
	}
}
