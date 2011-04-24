package org.rsbot.script.methods;

import org.rsbot.client.RSInterface;

/**
 * For internal use to find GUI components.
 *
 * @author Qauters
 */
class GameGUI extends MethodProvider {

	private int ind_GUI;
	private int ind_Minimap;
	private int ind_Compass;
	private int[] ind_Tabs;

	public GameGUI(MethodContext ctx) {
		super(ctx);
		resetIDs();
	}

	/**
	 * If GUI is out of sync, resets GUI.
	 */
	private synchronized void checkGUI() {
		if (ind_GUI != methods.client.getGUIRSInterfaceIndex()) {
			resetIDs();
			ind_GUI = methods.client.getGUIRSInterfaceIndex();
		}
	}

	/**
	 * @return The compasses <tt>RSInterface</tt>;otherwise null.
	 */
	public synchronized RSInterface getCompass() {
		// Check for GUI changes
		checkGUI();

		// Get GUI interface
		final RSInterface[] gui = ind_GUI != -1 ? methods.client
				.getRSInterfaceCache()[ind_GUI] : null;
		if (gui == null) {
			return null;
		}

		// Check if we need to find a new compass index
		if (ind_Compass == -1) {
			for (int i = 0; i < gui.length; i++) {
				if ((gui[i] != null) && (gui[i].getActions() != null)
						&& (gui[i].getActions().length == 1)
						&& gui[i].getActions()[0].equals("Face North")) {
					ind_Compass = i;
					break;
				}
			}
		}

		// Return the compass interface
		if (ind_Compass != -1) {
			return gui[ind_Compass];
		}

		return null;
	}

	/**
	 * @return The minimaps <tt>RSInterface</tt>; otherwise null.
	 */
	public synchronized RSInterface getMinimapInterface() {
		// Check for GUI changes
		checkGUI();

		// Get the GUI interface
		final RSInterface[] gui = ind_GUI != -1 ? methods.client
				.getRSInterfaceCache()[ind_GUI] : null;
		if (gui == null) {
			return null;
		}

		// Check if we need to find the new minimap index
		if (ind_Minimap == -1) {
			for (int i = 0; i < gui.length; i++) {
				if ((gui[i] != null) && (gui[i].getSpecialType() == 1338)) {
					ind_Minimap = i;
					break;
				}
			}
		}

		// Return minimap interface
		if (ind_Minimap != -1) {
			return gui[ind_Minimap];
		}

		return null;
	}

	/**
	 * @param id The ID of the tab.
	 * @return The specified tab <tt>RSInterface</tt>; otherwise null.
	 */
	public synchronized RSInterface getTab(final int id) {
		// Check argument
		if ((id < 0) || (id >= ind_Tabs.length)) {
			return null;
		}

		// Check for GUI changes
		checkGUI();

		// Get GUI interface
		final RSInterface[] gui = ind_GUI != -1 ? methods.client
				.getRSInterfaceCache()[ind_GUI] : null;
		if (gui == null) {
			return null;
		}

		// Check if we need to find a new tab index
		if (ind_Tabs[id] == -1) {
			for (int i = 0; i < gui.length; i++) {
				if ((gui[i] != null) && (gui[i].getActions() != null)
						&& (gui[i].getActions().length > 0)
						&& gui[i].getActions()[0].equals(Game.TAB_NAMES[id])) {
					ind_Tabs[id] = i;
					break;
				}
			}
		}

		// Return the tab interface
		if (ind_Tabs[id] != -1) {
			return gui[ind_Tabs[id]];
		}

		return null;
	}

	/**
	 * Resets the GameGUI class IDs.
	 */
	private synchronized void resetIDs() {
		ind_GUI = -1;
		ind_Minimap = -1;
		ind_Compass = -1;

		ind_Tabs = new int[17];
		for (int i = 0; i < ind_Tabs.length; i++) {
			ind_Tabs[i] = -1;
		}
	}
}
