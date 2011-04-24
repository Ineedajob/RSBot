package org.rsbot.script.randoms;

import org.rsbot.gui.AccountManager;
import org.rsbot.script.Random;
import org.rsbot.script.ScriptManifest;
import org.rsbot.script.wrappers.RSComponent;
import org.rsbot.script.wrappers.RSItem;

import java.awt.*;

@ScriptManifest(authors = {"Fred", "Arbiter"}, name = "Improved Rewards Box", version = 1.3)
public class ImprovedRewardsBox extends Random {

	private static final int BOOK_KNOWLEDGE_ID = 11640;
	private static final int LAMP_ID = 2528;
	private static final int MYSTERY_BOX_ID = 6199;
	private static final int BOX_ID = 14664;
	private static final int BOX_IF = 202;
	private static final int BOX_CONFIRM_IF = 28;
	private static final int BOX_SELECTION_IF = 15;
	private static final int BOX_SCROLLBAR_IF = 24;
	private static final int XP_IF = 134;

	private static final int ATT_ID = 4;
	private static final int AGILITY_ID = 5;
	private static final int HERBLORE_ID = 6;
	private static final int FISHING_ID = 7;
	private static final int THIEVING_ID = 8;
	private static final int RUNECRAFTING_ID = 9;
	private static final int SLAYER_ID = 10;
	private static final int FARMING_ID = 11;
	private static final int MINING_ID = 12;
	private static final int SMITHING_ID = 13;
	private static final int HUNTER_ID = 14;
	private static final int COOKING_ID = 15;
	private static final int FIREMAKING_ID = 16;
	private static final int WOODCUTTING_ID = 17;
	private static final int FLETCHING_ID = 18;
	private static final int CONSTRUCTION_ID = 19;
	private static final int SUMMONING_ID = 20;
	private static final int STRENGTH_ID = 21;
	private static final int RANGED_ID = 22;
	private static final int MAGIC_ID = 23;
	private static final int DEFENCE_ID = 24;
	private static final int HITPOINTS_ID = 25;
	private static final int CRAFTING_ID = 26;
	private static final int PRAYER_ID = 27;
	private static final int DUNGEONEERING_ID = 28;
	private static final int CONFIRM_ID = 2;

	private int scrollbarTopLength;
	private int scrollbarTotalLength;
	private int hiddenScreenHeight;
	private int viewableScreenHeight;
	private int endofselection = 0;
	private int XPSelection;

	public Random Rand;

	public boolean activateCondition() {
		return game.isLoggedIn() && !getMyPlayer().isInCombat() && !bank.isOpen()
				&& cachedInventoryContainedOneOf(BOX_ID, BOOK_KNOWLEDGE_ID, LAMP_ID, MYSTERY_BOX_ID);
	}

	private boolean cachedInventoryContainedOneOf(int... ids) {
		for (RSItem item : inventory.getCachedItems()) {
			for (int id : ids) {
				if (item.getID() == id) {
					return true;
				}
			}
		}
		return false;
	}

	public int getActualY(final RSComponent Component) {
		int boxYPos;
		final RSComponent[] selection = interfaces.get(202).getComponent(15)
				.getComponents();
		for (int end = 0; end < selection.length; end++) {
			if (selection[end].containsText(":")) {
				endofselection = (end - 6);
			}
			if (selection[end].containsText("emote")) {
				endofselection = (end - 6);
			}
			if (selection[end].containsText("costume")) {
				endofselection = (end - 6);
			}
		}
		viewableScreenHeight = (interfaces.get(202).getComponent(15).getHeight() - 11);
		int totalScreenHeight = (selection[endofselection].getAbsoluteY()
				+ selection[endofselection].getHeight() - selection[0]
				.getAbsoluteY());
		hiddenScreenHeight = (totalScreenHeight - viewableScreenHeight);
		if (hiddenScreenHeight > 0) {
			final RSComponent[] scrollbar = interfaces.get(202)
					.getComponent(24).getComponents();
			scrollbarTopLength = (scrollbar[1].getAbsoluteY() - scrollbar[0]
					.getAbsoluteY());
			int scrollbarBottomLength = (scrollbar[5].getAbsoluteY()
					- scrollbar[3].getAbsoluteY() + scrollbar[3].getHeight() - 6);
			scrollbarTotalLength = scrollbarTopLength + scrollbarBottomLength;
			double difference = (Double.parseDouble(Integer.toString(scrollbarTopLength))
					/ Double.parseDouble(Integer.toString(scrollbarTotalLength)) * Double
					.parseDouble(Integer.toString(hiddenScreenHeight)));
			boxYPos = (Component.getAbsoluteY() - (int) difference);
		} else {
			boxYPos = Component.getAbsoluteY();
		}
		return boxYPos;
	}

	public Rectangle getBoxArea(final RSComponent Component) {
		return new Rectangle(Component.getAbsoluteX(), getActualY(Component),
				Component.getWidth(), Component.getHeight());
	}

	public int loop() {
		if (getMyPlayer().isInCombat()) {
			return -1;
		}
		String[] choices = getChoices();
		if (interfaces.get(BOX_IF).isValid()) {
			for (RSComponent child : interfaces.get(137).getComponents()) {
				if (choices[choices.length - 1].equals("Emote")) {
					break;
				}
				if (child.containsText("You've already unlocked")
						&& child.containsText("emotes")
						&& !child.containsText("<col=0000ff>")) {
					for (int i = 0; i < choices.length; i++) {
						if (choices[i].contains("Emote")) {
							System.arraycopy(choices, i + 1, choices, i, choices.length - 1 - i);
							choices[choices.length - 1] = "Emote";
							break;
						}
					}
				}
			}
			RSComponent[] selection = interfaces.get(BOX_IF).getComponent(BOX_SELECTION_IF).getComponents();
			int optionSelected = 999;
			for (final String choice : choices) {
				for (int i = 0; i < selection.length; i++) {
					if (selection[i].getText().toLowerCase()
							.contains(choice.toLowerCase())) {
						optionSelected = i - 6;
						break;
					}
				}
				if (optionSelected != 999) {
					break;
				}
			}
			if (optionSelected == 999) {
				optionSelected = 0;
			}
			RSComponent[] scrollbar = interfaces.get(BOX_IF).getComponent(BOX_SCROLLBAR_IF).getComponents();
			if (scrollbarTopLength > 0) {
				mouse.move(scrollbar[1].getAbsoluteX() + random(1, 7),
						scrollbar[1].getAbsoluteY() + random(0, 20));
				mouse.drag((int) mouse.getLocation().getX(),
						(int) mouse.getLocation().getY() - scrollbarTopLength);
			}
			if (getBoxArea(selection[optionSelected]).y > 278) {
				mouse.move(scrollbar[1].getAbsoluteX() + random(1, 7),
						scrollbar[1].getAbsoluteY() + random(20, 30));
				int toDragtoY = (int) (mouse.getLocation().getY() + (Double
						.parseDouble(Integer
								.toString((getBoxArea(selection[optionSelected]).y
										+ getBoxArea(selection[optionSelected]).height
										- selection[0].getAbsoluteY() - viewableScreenHeight)))
						/ Double.parseDouble(Integer
						.toString(hiddenScreenHeight)) * Double
						.parseDouble(Integer.toString(scrollbarTotalLength))));
				if ((toDragtoY - (int) mouse.getLocation().getY()) > (scrollbar[5]
						.getAbsoluteY()
						- scrollbar[3].getAbsoluteY()
						+ scrollbar[3].getHeight() - 6)) {
					toDragtoY = (int) mouse.getLocation().getY()
							+ (scrollbar[5].getAbsoluteY()
							- scrollbar[3].getAbsoluteY()
							+ scrollbar[3].getHeight() - 6);
				}
				mouse.drag((int) mouse.getLocation().getX(), toDragtoY);
			}
			sleep(random(3000, 4000));
			selection = interfaces.get(BOX_IF).getComponent(BOX_SELECTION_IF).getComponents();
			if (selection.length > optionSelected) {
				int boxX = getBoxArea(selection[optionSelected]).x + 15;
				int boxY = getBoxArea(selection[optionSelected]).y + 15;
				int boxWidth = getBoxArea(selection[optionSelected]).width - 30;
				int boxHeight = getBoxArea(selection[optionSelected]).height - 30;
				mouse.move(random(boxX, boxX + boxWidth),
						random(boxY, boxY + boxHeight));
				mouse.click(true);
				interfaces.get(BOX_IF).getComponent(BOX_CONFIRM_IF).doClick();
			}
			return random(3000, 4000);
		}
		if (interfaces.get(XP_IF).isValid()) {
			interfaces.get(XP_IF).getComponent(XPSelection).doClick();
			interfaces.get(XP_IF).getComponent(CONFIRM_ID).doClick();
			return random(3000, 4000);
		}
		if (inventory.contains(BOX_ID)) {
			inventory.getItem(BOX_ID).doAction("Open");
			return random(3000, 4000);
		}
		if (inventory.contains(BOOK_KNOWLEDGE_ID)) {
			inventory.getItem(BOOK_KNOWLEDGE_ID).doAction("Read");
			return random(3000, 4000);
		}
		if (inventory.contains(LAMP_ID)) {
			inventory.getItem(LAMP_ID).doAction("Rub");
			return random(3000, 4000);
		}
		if (inventory.contains(MYSTERY_BOX_ID)) {
			inventory.getItem(MYSTERY_BOX_ID).doAction("Open");
			return random(3000, 4000);
		}
		return -1;
	}

	private String[] getChoices() {
		String[] choices = new String[2];
		choices[0] = "XP Item";
		choices[1] = "Cash";

		String a = account.getName() == null ? null : AccountManager.getReward(account.getName());
		if (a.equals("Attack")) {
			XPSelection = ATT_ID;
		} else if (a.equals("Strength")) {
			XPSelection = STRENGTH_ID;
		} else if (a.equals("Defence")) {
			XPSelection = DEFENCE_ID;
		} else if (a.equals("Range")) {
			XPSelection = RANGED_ID;
		} else if (a.equals("Prayer")) {
			XPSelection = PRAYER_ID;
		} else if (a.equals("Magic")) {
			XPSelection = MAGIC_ID;
		} else if (a.equals("Runecrafting")) {
			XPSelection = RUNECRAFTING_ID;
		} else if (a.equals("Construction")) {
			XPSelection = CONSTRUCTION_ID;
		} else if (a.equals("Hitpoints")) {
			XPSelection = HITPOINTS_ID;
		} else if (a.equals("Agility")) {
			XPSelection = AGILITY_ID;
		} else if (a.equals("Herblore")) {
			XPSelection = HERBLORE_ID;
		} else if (a.equals("Thieving")) {
			XPSelection = THIEVING_ID;
		} else if (a.equals("Crafting")) {
			XPSelection = CRAFTING_ID;
		} else if (a.equals("Fletching")) {
			XPSelection = FLETCHING_ID;
		} else if (a.equals("Slayer")) {
			XPSelection = SLAYER_ID;
		} else if (a.equals("Hunter")) {
			XPSelection = HUNTER_ID;
		} else if (a.equals("Mining")) {
			XPSelection = MINING_ID;
		} else if (a.equals("Smithing")) {
			XPSelection = SMITHING_ID;
		} else if (a.equals("Fishing")) {
			XPSelection = FISHING_ID;
		} else if (a.equals("Cooking")) {
			XPSelection = COOKING_ID;
		} else if (a.equals("Firemaking")) {
			XPSelection = FIREMAKING_ID;
		} else if (a.equals("Woodcutting")) {
			XPSelection = WOODCUTTING_ID;
		} else if (a.equals("Farming")) {
			XPSelection = FARMING_ID;
		} else if (a.equals("Summoning")) {
			XPSelection = SUMMONING_ID;
		} else if (a.equals("Dungeoneering")) {
			XPSelection = DUNGEONEERING_ID;
		} else {
			XPSelection = WOODCUTTING_ID;
			choices[0] = account.getName() == null ? null : AccountManager.getReward(account.getName());
		}
		return choices;
	}

}