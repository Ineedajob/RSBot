package org.rsbot.script.methods;

import java.awt.event.KeyEvent;

import org.rsbot.script.wrappers.RSComponent;
import org.rsbot.script.wrappers.RSInterface;
import org.rsbot.script.wrappers.RSTile;

/**
 * Game state and GUI operations.
 */
public class Game extends MethodProvider {
	
	public enum CHAT_MODE {
		VIEW, ON, FRIENDS, OFF, HIDE
	}

	public static final int INDEX_LOGIN_SCREEN = 3;
	public static final int INDEX_LOBBY_SCREEN = 7;
	public static final int[] INDEX_LOGGED_IN = { 10, 11 };
	public static final int INDEX_FIXED = 746;

	public static final int[] TAB_FUNCTION_KEYS = { KeyEvent.VK_F5, // Attack
			0, // Achievements
			0, // Stats
			0, // Quests
			KeyEvent.VK_F1, // Inventory
			KeyEvent.VK_F2, // Equipment
			KeyEvent.VK_F3, // Prayer
			KeyEvent.VK_F4, // Magic
			0, // Summoning
			0, // Friends
			0, // Ignore
			0, // Clan
			0, // Options
			0, // Controls
			0, // Music
			0, // Notes
			0, // Logout
	};
	public static final int TAB_ATTACK = 0;
	public static final int TAB_ACHIEVEMENTS = 1;
	public static final int TAB_STATS = 2;
	public static final int TAB_QUESTS = 3;
	public static final int TAB_INVENTORY = 4;
	public static final int TAB_EQUIPMENT = 5;
	public static final int TAB_PRAYER = 6;
	public static final int TAB_MAGIC = 7;
	public static final int TAB_SUMMONING = 8;
	public static final int TAB_FRIENDS = 9;
	public static final int TAB_IGNORE = 10;
	public static final int TAB_CLAN = 11;
	public static final int TAB_OPTIONS = 12;
	public static final int TAB_CONTROLS = 13;
	public static final int TAB_MUSIC = 14;
	public static final int TAB_NOTES = 15;
	public static final int TAB_LOGOUT = 16;

	public static final int CHAT_OPTION = 751;
	public static final int CHAT_OPTION_ALL = 2;
	public static final int CHAT_OPTION_GAME = 3;
	public static final int CHAT_OPTION_PUBLIC = 4;
	public static final int CHAT_OPTION_PRIVATE = 5;
	public static final int CHAT_OPTION_CLAN = 6;
	public static final int CHAT_OPTION_TRADE = 7;
	public static final int CHAT_OPTION_ASSIST = 8;

	public static final int INTERFACE_CHAT_BOX = 137;
	public static final int INTERFACE_GAME_SCREEN = 548;
	public static final int INTERFACE_LEVEL_UP = 740;
	public static final int INTERFACE_LOGOUT = 182;
	public static final int INTERFACE_LOGOUT_LOBBY = 1;
	public static final int INTERFACE_LOGOUT_COMPLETE = 6;
	public static final int INTERFACE_LOGOUT_BUTTON_FIXED = 181;
	public static final int INTERFACE_LOGOUT_BUTTON_RESIZED = 172;
	public static final int INTERFACE_WELCOME_SCREEN = 907;
	public static final int INTERFACE_WELCOME_SCREEN_CHILD = 150;
	public static final int INTERFACE_WELCOME_SCREEN_PLAY = 18;

	public static final int INTERFACE_HP_ORB = 748;
	public static final int INTERFACE_PRAYER_ORB = 749;

	public static final int[] INTERFACE_TALKS = new int[] { 211, 241, 251, 101,
			242, 102, 161, 249, 243, 64, 65, 244, 255, 249, 230, 372, 421 };
	public static final int[] INTERFACE_OPTIONS = new int[] { 230, 228 };

	public static final String[] TAB_NAMES = new String[] { "Combat Styles",
			"Stats", "Quest List", "Achievements", "Inventory",
			"Worn Equipment", "Prayer List", "Magic Spellbook", "Objectives",
			"Friends List", "Ignore List", "Clan Chat", "Options", "Emotes",
			"Music Player", "Notes", "Exit" };

	Game(final MethodContext ctx) {
		super(ctx);
	}

	/**
	 * Sets the trade accept mode
	 * 
	 * @param mode
	 *            The <tt>CHAT_MODE</tt> to set the trade button to.
	 * @return <tt>true</tt> if item in menu was clicked; otherwise
	 *         <tt>false</tt>.
	 * @see #setChatOption(int, CHAT_MODE)
	 */
	@Deprecated
	public boolean setTradeMode(Game.CHAT_MODE mode) {
		if (mode.equals(Game.CHAT_MODE.HIDE))
			throw new IllegalArgumentException("Bad mode: HIDE");
		mouseChatButton(CHAT_OPTION_TRADE, false);
		return methods.menu.doAction(mode.toString());
	}

	/**
	 * Left clicks the first chat button at the bottom of the screen to turn on
	 * the showing of all chat messages.
	 * 
	 * @see #setChatOption(int, CHAT_MODE)
	 */
	@Deprecated
	public void showAllChatMessages() {
		mouseChatButton(CHAT_OPTION_ALL, true);
	}

	/**
	 * Left clicks the game chat messages button at the bottom of the screen to
	 * enable the showing of all game messages.
	 * 
	 * @see #setChatOption(int, CHAT_MODE)
	 */
	@Deprecated
	public void showGameChatMessages() {
		mouseChatButton(CHAT_OPTION_GAME, true);
	}

	/**
	 * Set private chat mode.
	 * 
	 * @param mode
	 *            The <tt>CHAT_MODE</tt> to set the private chat button to.
	 * @return <tt>true</tt> if item in menu was clicked; otherwise
	 *         <tt>false</tt>.
	 * @see #setChatOption(int, CHAT_MODE)
	 */
	@Deprecated
	public boolean setPrivateChat(Game.CHAT_MODE mode) {
		if (mode.equals(Game.CHAT_MODE.HIDE))
			throw new IllegalArgumentException("Bad mode: HIDE");
		mouseChatButton(CHAT_OPTION_PRIVATE, false);
		return methods.menu.doAction(mode.toString());
	}

	/**
	 * Sets the public chat mode.
	 * 
	 * @param mode
	 *            The <tt>CHAT_MODE</tt> to set the public chat button to.
	 * @return <tt>true</tt> if item in menu was clicked; otherwise
	 *         <tt>false</tt>.
	 * @see #setChatOption(int, CHAT_MODE)
	 */
	@Deprecated
	public boolean setPublicChat(Game.CHAT_MODE mode) {
		mouseChatButton(CHAT_OPTION_PUBLIC, false);
		return methods.menu.doAction(mode.toString());
	}

	/**
	 * Sets the request assistance mode.
	 * 
	 * @param mode
	 *            The <tt>CHAT_MODE</tt> to set the assist button to.
	 * @return <tt>true</tt> if item in menu was clicked; otherwise
	 *         <tt>false</tt>.
	 * @see #setChatOption(int, CHAT_MODE)
	 */
	@Deprecated
	public boolean setAssistMode(Game.CHAT_MODE mode) {
		if (mode.equals(Game.CHAT_MODE.HIDE))
			throw new IllegalArgumentException("Bad mode: HIDE");
		mouseChatButton(CHAT_OPTION_ASSIST, false);
		return methods.menu.doAction(mode.toString());
	}

	/**
	 * Sets the clan chat mode
	 * 
	 * @param mode
	 *            The <tt>CHAT_MODE</tt> to set the clan button to.
	 * @return <tt>true</tt> if item in menu was clicked; otherwise
	 *         <tt>false</tt>.
	 * @see #setChatOption(int, CHAT_MODE)
	 */
	@Deprecated
	public boolean setClanMode(Game.CHAT_MODE mode) {
		if (mode.equals(Game.CHAT_MODE.HIDE))
			throw new IllegalArgumentException("Bad mode: HIDE");
		mouseChatButton(CHAT_OPTION_CLAN, false);
		return methods.menu.doAction(mode.toString());
	}

	/**
	 * Set the specified chat mode
	 * 
	 * @param chatOption
	 *            one of CHAT_OPTION_
	 * @param mode
	 *            one of CHAT_MODE
	 * @return <tt>true</tt> if item was clicked correctly; otherwise
	 *         <tt>false</tt>
	 */
	public boolean setChatOption(int chatOption, CHAT_MODE mode) {
		mouseChatButton(chatOption, false);
		return methods.menu.doAction(mode.toString());
	}

	/**
	 * Access the last message spoken by a player.
	 * 
	 * @return The last message spoken by a player or "" if none
	 */
	public String getLastMessage() {
		RSInterface chatBox = methods.interfaces.get(INTERFACE_CHAT_BOX);
		for (int i = 279; i >= 180; i--) {// Valid text is from 180 to 279, was
											// 58-157
			String text = chatBox.getComponent(i).getText();
			if (!text.isEmpty() && text.contains("<"))
				return text;
		}
		return "";
	}

	/**
	 * Opens the specified tab at the specified index.
	 * 
	 * @param tab
	 *            The tab to open.
	 * @return <tt>true</tt> if tab successfully selected; otherwise
	 *         <tt>false</tt>.
	 * @see #openTab(int tab, boolean functionKey)
	 */
	public boolean openTab(int tab) {
		return openTab(tab, false);
	}

	/**
	 * Opens the specified tab at the specified index.
	 * 
	 * @param tab
	 *            The tab to open, functionKey if wanting to use function keys
	 *            to switch.
	 * @return <tt>true</tt> if tab successfully selected; otherwise
	 *         <tt>false</tt>.
	 */
	public boolean openTab(int tab, boolean functionKey) {
		// Check current tab
		if (tab == getCurrentTab())
			return true;

		if (functionKey) {
			if (tab >= TAB_FUNCTION_KEYS.length || TAB_FUNCTION_KEYS[tab] == 0)
				return false;// no function key for specified tab

			methods.keyboard.pressKey((char) TAB_FUNCTION_KEYS[tab]);
			sleep(random(80, 200));
			methods.keyboard.releaseKey((char) TAB_FUNCTION_KEYS[tab]);
		} else {
			org.rsbot.client.RSInterface iTab = methods.gui.getTab(tab);
			if (iTab == null) {
				return false;
			}
			methods.interfaces.getComponent(iTab.getID()).doClick();
		}

		sleep(random(400, 600));
		return tab == getCurrentTab();
	}

	/**
	 * Closes the currently open tab if in resizable mode.
	 */
	public void closeTab() {
		int tab = getCurrentTab();
		if (isFixed() || tab == TAB_LOGOUT) {
			return;
		}
		org.rsbot.client.RSInterface iTab = methods.gui.getTab(tab);
		if (iTab != null) {
			methods.interfaces.getComponent(iTab.getID()).doClick();
		}
	}

	/**
	 * Click chat button.
	 * 
	 * @param button
	 *            Which button? One of CHAT_OPTION
	 * @param left
	 *            Left or right button? Left = true. Right = false.
	 */
	public boolean mouseChatButton(int button, boolean left) {
		RSComponent chatButton = methods.interfaces.get(CHAT_OPTION)
				.getComponent(button);
		if (!chatButton.isValid()) {
			return false;
		}
		return chatButton.doClick(left);
	}

	/**
	 * Gets the currently open tab.
	 * 
	 * @return The currently open tab or the logout tab by default.
	 */
	public int getCurrentTab() {
		for (int i = 0; i < TAB_NAMES.length; i++) {

			// Get tab
			org.rsbot.client.RSInterface tab = methods.gui.getTab(i);
			if (tab == null) {
				continue;
			}

			// Check if tab is selected
			if (tab.getTextureID() != -1)
				return i;
		}

		return -1; // no selected ones. (never happens, always return
					// TAB_LOGOUT)
	}

	/**
	 * Gets the current run energy.
	 * <p/>
	 * Deprecated : use walking.getEnergy()
	 * 
	 * @return An <tt>int</tt> representation of the players current energy.
	 */
	@Deprecated
	public int getEnergy() {
		return methods.walking.getEnergy();
	}

	/**
	 * Returns the valid chat component.
	 * 
	 * @return <tt>RSInterfaceChild</tt> of the current valid talk interface;
	 *         otherwise null.
	 * @see #INTERFACE_TALKS
	 */
	public RSComponent getTalkInterface() {
		for (int talk : INTERFACE_TALKS) {
			RSComponent child = methods.interfaces.getComponent(talk, 0);
			if (child.isValid())
				return child;
		}
		return null;
	}

	/**
	 * Switches to a given world.
	 * 
	 * @param world
	 *            the world to switch to, must be valid.
	 */
	public boolean switchWorld(int world) {
		if (isLoggedIn())
			logout(true);

		if (getClientState() != INDEX_LOBBY_SCREEN)
			return false;

		RSComponent worldSelect = methods.interfaces.getComponent(906, 196);
		if (worldSelect.getBackgroundColor() != 2630) {
			if (worldSelect.doClick()) {
				for (int i = 0; worldSelect.getBackgroundColor() != 2630; i++) {
					if (i == 10)
						return false;

					sleep(random(100, 200));
				}
			}
		}

		RSComponent worldComp = null;
		for (RSComponent comp : methods.interfaces.getComponent(910, 68)
				.getComponents()) {
			if (Integer.parseInt(comp.getText()) == world) {
				worldComp = comp;
				break;
			}
		}

		if (worldComp == null)
			return false;

		for (int i = 0; !methods.interfaces.scrollTo(worldComp,
				(910 << 16) + 85); i++) {
			if (i == 3)
				return false;

			sleep(random(200, 400));
		}

		String players = methods.interfaces.getComponent(910, 70)
				.getComponents()[worldComp.getComponentIndex()].getText();
		if (players.equals("0") || players.equals("OFFLINE")
				|| players.equals("FULL"))
			return false;

		if (methods.interfaces.getComponent(910, 76).getComponents()[worldComp
				.getComponentIndex()].doClick()) {
			if (methods.interfaces.getComponent(906, 154).doClick())
				return true;
		}

		return false;
	}

	/**
	 * Checks whether or not the logout tab is selected.
	 * 
	 * @return <tt>true</tt> if on the logout tab.
	 */
	public boolean isOnLogoutTab() {
		for (int i = 0; i < Game.TAB_NAMES.length; i++) {
			org.rsbot.client.RSInterface tab = methods.gui.getTab(i);
			if (tab == null)
				continue;
			int id = tab.getTextureID();
			if (id > -1 && id < 2201)
				return false;
		}
		return true;
	}

	/**
	 * Closes the bank if it is open and logs out.
	 * 
	 * @param lobby
	 *            <tt>true</tt> if player should be logged out to the lobby
	 * @return <tt>true</tt> if the player was logged out.
	 */
	public boolean logout(boolean lobby) {
		if (methods.bank.isOpen()) {
			methods.bank.close();
			sleep(random(200, 400));
		}
		if (methods.bank.isOpen()) {
			return false;
		}
		if (methods.client.isSpellSelected()
				|| methods.inventory.isItemSelected()) {
			int currentTab = methods.game.getCurrentTab();
			int randomTab = random(1, 6);
			while (randomTab == currentTab) {
				randomTab = random(1, 6);
			}
			methods.game.openTab(randomTab);
			sleep(random(400, 800));
		}
		if (methods.client.isSpellSelected()
				|| methods.inventory.isItemSelected()) {
			return false;
		}
		if (!isOnLogoutTab()) {
			int idx = methods.client.getGUIRSInterfaceIndex();
			// Logout button in the top right hand corner
			methods.interfaces.getComponent(idx, isFixed() ? 181 : 172)
					.doClick();
			int timesToWait = 0;
			while (!isOnLogoutTab() && timesToWait < 5) {
				sleep(random(200, 400));
				timesToWait++;
			}
		}
		methods.interfaces.getComponent(182, lobby ? 1 : 6).doClick();
		// Final logout button in the logout tab
		sleep(random(1500, 2000));
		return !isLoggedIn();
	}

	/**
	 * Runs the LoginBot random.
	 * 
	 * @return <tt>true</tt> if random was run; otherwise <tt>false</tt>.
	 */
	public boolean login() {
		return new org.rsbot.script.randoms.LoginBot().activateCondition();
	}

	/**
	 * Determines whether or no the client is currently in the fixed display
	 * mode.
	 * 
	 * @return <tt>true</tt> if in fixed mode; otherwise <tt>false</tt>.
	 */
	public boolean isFixed() {
		return methods.client.getGUIRSInterfaceIndex() != INDEX_FIXED;
	}

	/**
	 * Determines whether or not the client is currently logged in to an
	 * account.
	 * 
	 * @return <tt>true</tt> if logged in; otherwise <tt>false</tt>.
	 */
	public boolean isLoggedIn() {
		org.rsbot.client.Client client = methods.client;
		int index = client == null ? -1 : client.getLoginIndex();
		for (int idx : INDEX_LOGGED_IN) {
			if (index == idx) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Determines whether or not the client is showing the login screen.
	 * 
	 * @return <tt>true</tt> if the client is showing the login screen;
	 *         otherwise <tt>false</tt>.
	 */
	public boolean isLoginScreen() {
		return methods.client.getLoginIndex() == INDEX_LOGIN_SCREEN;
	}

	/**
	 * Determines whether or not the welcome screen is open.
	 * 
	 * @return <tt>true</tt> if the client is showing the welcome screen;
	 *         otherwise <tt>false</tt>.
	 */
	public boolean isWelcomeScreen() {
		return methods.interfaces.get(INTERFACE_WELCOME_SCREEN)
				.getComponent(INTERFACE_WELCOME_SCREEN_CHILD).getAbsoluteY() > 2;
	}

	/**
	 * Gets the game state.
	 * 
	 * @return The game state.
	 */
	public int getClientState() {
		return methods.client.getLoginIndex();
	}

	/**
	 * Gets the plane we are currently on. Typically 0 (ground level), but will
	 * increase when going up ladders. You cannot be on a negative plane. Most
	 * dungeons/basements are on plane 0 elsewhere on the world map.
	 * 
	 * @return The current plane.
	 */
	public int getPlane() {
		return methods.client.getPlane();
	}

	/**
	 * Gets the x coordinate of the loaded map area (far west).
	 * 
	 * @return The region base x.
	 */
	public int getBaseX() {
		return methods.client.getBaseX();
	}

	/**
	 * Gets the y coordinate of the loaded map area (far south).
	 * 
	 * @return The region base y.
	 */
	public int getBaseY() {
		return methods.client.getBaseY();
	}

	/**
	 * Gets the (x, y) coordinate pair of the south-western tile at the base of
	 * the loaded map area.
	 * 
	 * @return The region base tile.
	 */
	public RSTile getMapBase() {
		return new RSTile(methods.client.getBaseX(), methods.client.getBaseY());
	}

	/**
	 * Gets the canvas height.
	 * 
	 * @return The canvas' width.
	 */
	public int getWidth() {
		return methods.bot.getCanvas().getWidth();
	}

	/**
	 * Gets the canvas height.
	 * 
	 * @return The canvas' height.
	 */
	public int getHeight() {
		return methods.bot.getCanvas().getHeight();
	}

}