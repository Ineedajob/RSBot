package org.rsbot.script.methods;

import org.rsbot.script.wrappers.RSComponent;

import java.util.ArrayList;

/**
 * Clan chat related operations.
 *
 * @author Debauchery
 */
public class ClanChat extends MethodProvider {

	public static final int INTERFACE_CLAN_CHAT = 1110;
	public static final int INTERFACE_CLAN_CHAT_CHECK = 55;
	public static final int INTERFACE_CLAN_CHAT_USERS_LIST = 9;
	public static final int INTERFACE_CLAN_CHAT_INFO_BUTTON = 0;
	public static final int INTERFACE_CLAN_CHAT_SETTINGS_BUTTON = 0;
	//
	public static final int INTERFACE_CLAN_CHAT_INFO = 1107;
	public static final int INTERFACE_CLAN_CHAT_INFO_CHANNEL_NAME = 172;
	public static final int INTERFACE_CLAN_CHAT_INFO_CHANNEL_OWNER = 35;
	public static final int INTERFACE_CLAN_CHAT_INFO_CLOSE_BUTTON = 174;
	//
	public static final int INTERFACE_CLAN_CHAT_SETTINGS = 1096;
	public static final int INTERFACE_CLAN_CHAT_SETTINGS_LEAVE = 281;
	public static final int INTERFACE_CLAN_CHAT_SETTINGS_CLOSE_BUTTON = 341;

	ClanChat(final MethodContext ctx) {
		super(ctx);
	}

	/**
	 * Gets the owner of the channel.
	 *
	 * @return The owner of the channel or null if unavailable
	 */
	public String getChannelOwner() {
		String temp = null;
		if (isInformationOpen()) {
			temp = methods.interfaces.getComponent(INTERFACE_CLAN_CHAT_INFO,
			                                       INTERFACE_CLAN_CHAT_INFO_CHANNEL_OWNER).getText();
		} else {
			if (methods.game.getCurrentTab() != Game.TAB_CLAN_CHAT) {
				methods.game.openTab(Game.TAB_CLAN_CHAT);
			}
			if (methods.game.getCurrentTab() == Game.TAB_CLAN_CHAT) {
				if (openInformation()) {
					if (isInformationOpen()) {
						temp = methods.interfaces.getComponent(INTERFACE_CLAN_CHAT_INFO,
						                                       INTERFACE_CLAN_CHAT_INFO_CHANNEL_OWNER).getText();
					}
				}
			}
		}
		if (isInformationOpen()) {
			closeInformation();
		}
		return temp.trim();
	}

	/**
	 * Gets the name of the channel.
	 *
	 * @return The name of the channel or null if none
	 */
	public String getChannelName() {
		String temp = null;
		if (isInformationOpen()) {
			temp = methods.interfaces.getComponent(INTERFACE_CLAN_CHAT_INFO,
			                                       INTERFACE_CLAN_CHAT_INFO_CHANNEL_NAME).getText();
		} else {
			if (methods.game.getCurrentTab() != Game.TAB_CLAN_CHAT) {
				methods.game.openTab(Game.TAB_CLAN_CHAT);
			}
			if (methods.game.getCurrentTab() == Game.TAB_CLAN_CHAT) {
				if (openInformation()) {
					if (isInformationOpen()) {
						temp = methods.interfaces.getComponent(INTERFACE_CLAN_CHAT_INFO,
						                                       INTERFACE_CLAN_CHAT_INFO_CHANNEL_NAME).getText();
					}
				}
			}
		}
		if (isInformationOpen()) {
			closeInformation();
		}
		return temp.trim();
	}

	/**
	 * Gets the users in the channel.
	 *
	 * @return The users in the channel or null if unavailable
	 */
	public String[] getChannelUsers() {
		String[] temp = null;
		ArrayList<String> tempList = new ArrayList<String>();
		if (methods.game.getCurrentTab() != Game.TAB_CLAN_CHAT) {
			methods.game.openTab(Game.TAB_CLAN_CHAT);
		}
		if (methods.game.getCurrentTab() == Game.TAB_CLAN_CHAT) {
			if (methods.interfaces.getComponent(INTERFACE_CLAN_CHAT, INTERFACE_CLAN_CHAT_USERS_LIST) != null) {
				for (RSComponent comp : methods.interfaces.getComponent(INTERFACE_CLAN_CHAT,
				                                                        INTERFACE_CLAN_CHAT_USERS_LIST).getComponents()) {
					if (comp.getText() != null) {
						tempList.add(comp.getText().trim());
					} else {
						break;
					}
				}
			}
		}
		tempList.toArray(temp);
		return temp;
	}

	/**
	 * Returns whether or not we're in a channel.
	 *
	 * @return <tt>true</tt> if in a channel; otherwise <tt>false</tt>
	 */
	public boolean isInChannel() {
		if (methods.game.getCurrentTab() != Game.TAB_CLAN_CHAT) {
			methods.game.openTab(Game.TAB_CLAN_CHAT);
		}
		if (methods.game.getCurrentTab() == Game.TAB_CLAN_CHAT) {
			return methods.interfaces.getComponent(INTERFACE_CLAN_CHAT, INTERFACE_CLAN_CHAT_CHECK).containsText(
					"If you");

		} else {
			return false;
		}
	}

	/**
	 * Opens clan information interface.
	 *
	 * @return <tt>true</tt> if open/has been opened; otherwise <tt>false</tt>
	 */
	public boolean openInformation() {
		if (!isInformationOpen()) {
			if (methods.game.getCurrentTab() != Game.TAB_CLAN_CHAT) {
				methods.game.openTab(Game.TAB_CLAN_CHAT);
			}
			if (methods.game.getCurrentTab() == Game.TAB_CLAN_CHAT) {
				if (isInChannel()) {
					methods.interfaces.getComponent(INTERFACE_CLAN_CHAT_INFO,
					                                INTERFACE_CLAN_CHAT_INFO_BUTTON).doClick();
				} else {
					return false;
				}
			}
		}
		return isInformationOpen();
	}

	/**
	 * Closes clan information interface.
	 *
	 * @return <tt>true</tt> if closed/has been closed; otherwise <tt>false</tt>
	 */
	public boolean closeInformation() {
		if (isInformationOpen()) {
			methods.interfaces.getComponent(INTERFACE_CLAN_CHAT_INFO, INTERFACE_CLAN_CHAT_INFO_CLOSE_BUTTON).doClick();
		}
		return !isInformationOpen();
	}

	/**
	 * Checks to see if the information interface is open/valid.
	 *
	 * @return <tt>true</tt> if open; otherwise <tt>false</tt>
	 */
	public boolean isInformationOpen() {
		return methods.interfaces.get(INTERFACE_CLAN_CHAT_INFO).isValid();
	}

	/**
	 * Opens clan Settings interface.
	 *
	 * @return <tt>true</tt> if open/has been opened; otherwise <tt>false</tt>
	 */
	public boolean openSettings() {
		if (!isSettingsOpen()) {
			if (methods.game.getCurrentTab() != Game.TAB_CLAN_CHAT) {
				methods.game.openTab(Game.TAB_CLAN_CHAT);
			}
			if (methods.game.getCurrentTab() == Game.TAB_CLAN_CHAT) {
				if (isInChannel()) {
					methods.interfaces.getComponent(INTERFACE_CLAN_CHAT_SETTINGS,
					                                INTERFACE_CLAN_CHAT_SETTINGS_BUTTON).doClick();
				} else {
					return false;
				}
			}
		}
		return isSettingsOpen();
	}

	/**
	 * Closes clan Settings interface.
	 *
	 * @return <tt>true</tt> if closed/has been closed; otherwise <tt>false</tt>
	 */
	public boolean closeSettings() {
		if (isSettingsOpen()) {
			methods.interfaces.getComponent(INTERFACE_CLAN_CHAT_SETTINGS,
			                                INTERFACE_CLAN_CHAT_SETTINGS_CLOSE_BUTTON).doClick();
		}
		return !isSettingsOpen();
	}

	/**
	 * Checks to see if the Settings interface is open/valid.
	 *
	 * @return <tt>true</tt> if open; otherwise <tt>false</tt>
	 */
	public boolean isSettingsOpen() {
		return methods.interfaces.get(INTERFACE_CLAN_CHAT_SETTINGS).isValid();
	}
}