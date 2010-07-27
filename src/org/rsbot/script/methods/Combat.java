package org.rsbot.script.methods;

import org.rsbot.script.wrappers.RSComponent;

/**
 * Combat related operations.
 */
public class Combat extends MethodProvider {

	public Combat(MethodContext ctx) {
		super(ctx);
	}

	/**
     * Turns auto-retaliate on or off in the combat tab.
     *
     * @param enable <tt>true</tt> to enable; <tt>false</tt> to disable.
     */
	public void setAutoRetaliate(final boolean enable) {
		final RSComponent autoRetal = methods.interfaces.getComponent(884, 15);
		if (isAutoRetaliateEnabled() != enable) {
			if (methods.game.getCurrentTab() != Game.TAB_ATTACK) {
				methods.game.openTab(Game.TAB_ATTACK);
			}
			if (methods.game.getCurrentTab() == Game.TAB_ATTACK
					&& autoRetal != null) {
				autoRetal.doClick();
			}
		}
	}

	/**
	 * Returns whether or not the auto-retaliate option is enabled.
	 * 
	 * @return <tt>true</tt> if retaliate is enabled; otherwise <tt>false</tt>.
	 */
	public boolean isAutoRetaliateEnabled() {
		return methods.settings.getSetting(172) == 0;
	}


	/**
	 * Gets the attack mode.
	 * 
	 * @return The current fight mode setting.
	 */
	public int getFightMode() {
		return methods.settings.getSetting(Settings.SETTING_COMBAT_STYLE);
	}

	/**
	 * Sets the attack mode.
	 * 
	 * @param fightMode The fight mode to set it to. From 0-3 corresponding to the 
	 * 4 attacking modes.
	 * @see #getFightMode()
	 */
	public void setFightMode(int fightMode) {
		if (fightMode != getFightMode()) {
			methods.game.openTab(Game.TAB_ATTACK);
			if (fightMode == 0) {
				methods.mouse.click(577, 253, 55, 35, true);
			} else if (fightMode == 1) {
				methods.mouse.click(661, 253, 55, 35, true);
			} else if (fightMode == 2) {
				methods.mouse.click(576, 306, 55, 35, true);
			} else if (fightMode == 3) {
				methods.mouse.click(662, 308, 55, 35, true);
			}
		}
	}

	/**
	 * Gets the current Wilderness Level. Written by Speed.
	 *
	 * @return The current wilderness level otherwise, 0.
	 */
	public int getWildernessLevel() {
		return methods.interfaces.get(381).getComponent(1).isValid() ?
				Integer.parseInt(methods.interfaces.get(381).getComponent(1).getText().replace("Level: ", "").trim()) : 0;
	}

	/**
	 * Gets the current player's life points.
	 * 
	 * @return The current life points if the interface
	 * is valid; otherwise 0.
	 */
	public int getLifePoints() {
		try {
			return Integer.parseInt(methods.interfaces.get(748).getComponent(8).getText());
		} catch (NumberFormatException ex) {
			return 0;
		}
	}

	/**
	 * Returns true if designated prayer is turned on. Written by Iscream.
	 *
	 * @param index The prayer to check.
	 */
	public boolean isPrayerOn(int index) {
		RSComponent[] prayers = methods.interfaces.getComponent(271, 7).getComponents();
		for (RSComponent prayer : prayers) {
			if (prayer.getComponentIndex() == index && prayer.getBackgroundColor() != -1)
				return true;
		}
		return false;
	}

}
