package org.rsbot.gui;

import org.rsbot.bot.Bot;
import org.rsbot.event.impl.*;
import org.rsbot.event.listeners.PaintListener;
import org.rsbot.event.listeners.TextPaintListener;

import javax.swing.*;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

public class BotMenuBar extends JMenuBar {

	private static final long serialVersionUID = 971579975301998332L;

	public static final Map<String, Class<?>> DEBUG_MAP = new LinkedHashMap<String, Class<?>>();

	public static final String[] TITLES;
	public static final String[][] ELEMENTS;

	static {

		// Text
		DEBUG_MAP.put("Game State", TLoginIndex.class);
		DEBUG_MAP.put("Current Tab", TTab.class);
		DEBUG_MAP.put("Camera", TCamera.class);
		DEBUG_MAP.put("Animation", TAnimation.class);
		DEBUG_MAP.put("Floor Height", TFloorHeight.class);
		DEBUG_MAP.put("Player Position", TPlayerPosition.class);
		DEBUG_MAP.put("Mouse Position", TMousePosition.class);
		DEBUG_MAP.put("User Input Allowed", TUserInputAllowed.class);
		DEBUG_MAP.put("Menu Actions", TMenuActions.class);
		DEBUG_MAP.put("Menu", TMenu.class);
		DEBUG_MAP.put("FPS", TFPS.class);

		// Paint
		DEBUG_MAP.put("Players", DrawPlayers.class);
		DEBUG_MAP.put("NPCs", DrawNPCs.class);
		DEBUG_MAP.put("Objects", DrawObjects.class);
		DEBUG_MAP.put("Models", DrawModel.class);
		DEBUG_MAP.put("Mouse", DrawMouse.class);
		DEBUG_MAP.put("Inventory", DrawInventory.class);
		DEBUG_MAP.put("Ground Items", DrawItems.class);
		DEBUG_MAP.put("Calc Test", DrawBoundaries.class);
		DEBUG_MAP.put("Settings", DrawSettings.class);

		// Other
		DEBUG_MAP.put("Log Messages", MessageLogger.class);

		TITLES = new String[]{"File", "Edit", "View", "Help"};
		ELEMENTS = new String[][]{
				{"New Bot", "Close Bot", "-", "Run Script", "Stop Script", "Pause Script", "-", "Save Screenshot", "-", "Exit"},
				{"Accounts", "-", "ToggleF Force Input", "ToggleF Less CPU", "-", "ToggleF Disable Anti-Randoms", "ToggleF Disable Auto Login", "-", "ToggleF Christmas Mode"},
				constructDebugs(),
				{"Site", "Project", "About"}};
	}

	private static String[] constructDebugs() {
		List<String> debugItems = new ArrayList<String>();
		debugItems.add("Hide Toolbar");
		debugItems.add("Hide Log Window");
		debugItems.add("All Debugging");
		debugItems.add("-");
		for (String key : DEBUG_MAP.keySet()) {
			Class<?> el = DEBUG_MAP.get(key);
			if (PaintListener.class.isAssignableFrom(el)) {
				debugItems.add(key);
			}
		}
		debugItems.add("-");
		for (String key : DEBUG_MAP.keySet()) {
			final Class<?> el = DEBUG_MAP.get(key);
			if (TextPaintListener.class.isAssignableFrom(el)) {
				debugItems.add(key);
			}
		}
		debugItems.add("-");
		for (String key : DEBUG_MAP.keySet()) {
			Class<?> el = DEBUG_MAP.get(key);
			if (!(TextPaintListener.class.isAssignableFrom(el)) && !(PaintListener.class.isAssignableFrom(el))) {
				debugItems.add(key);
			}
		}
		for (ListIterator<String> it = debugItems.listIterator(); it.hasNext();) {
			String s = it.next();
			if (!s.equals("-")) {
				it.set("ToggleF " + s);
			}
		}
		return debugItems.toArray(new String[debugItems.size()]);
	}

	private Map<String, JCheckBoxMenuItem> eventCheckMap = new HashMap<String, JCheckBoxMenuItem>();
	private Map<String, JCheckBoxMenuItem> commandCheckMap = new HashMap<String, JCheckBoxMenuItem>();
	private Map<String, JMenuItem> commandMenuItem = new HashMap<String, JMenuItem>();
	private ActionListener listener;

	public BotMenuBar(ActionListener listener) {
		this.listener = listener;
		for (int i = 0; i < TITLES.length; i++) {
			String title = TITLES[i];
			String[] elems = ELEMENTS[i];
			add(constructMenu(title, elems));
		}
	}

	public void setOverrideInput(boolean force) {
		commandCheckMap.get("Force Input").setSelected(force);
	}

	public void setPauseScript(boolean pause) {
		commandMenuItem.get("Pause Script").setText(pause ? "Resume Script" : "Pause Script");
	}

	public void setBot(Bot bot) {
		if (bot == null) {
			commandMenuItem.get("Close Bot").setEnabled(false);
			commandMenuItem.get("Run Script").setEnabled(false);
			commandMenuItem.get("Stop Script").setEnabled(false);
			commandMenuItem.get("Pause Script").setEnabled(false);
			commandMenuItem.get("Save Screenshot").setEnabled(false);
			for (JCheckBoxMenuItem item : eventCheckMap.values()) {
				item.setSelected(false);
				item.setEnabled(false);
			}
			disable("All Debugging", "Force Input", "Less CPU",
					"Disable Anti-Randoms", "Disable Auto Login");
		} else {
			commandMenuItem.get("Close Bot").setEnabled(true);
			commandMenuItem.get("Run Script").setEnabled(true);
			commandMenuItem.get("Stop Script").setEnabled(true);
			commandMenuItem.get("Pause Script").setEnabled(true);
			commandMenuItem.get("Save Screenshot").setEnabled(true);
			int selections = 0;
			for (Map.Entry<String, JCheckBoxMenuItem> entry : eventCheckMap.entrySet()) {
				entry.getValue().setEnabled(true);
				boolean selected = bot.hasListener(DEBUG_MAP.get(entry.getKey()));
				entry.getValue().setSelected(selected);
				if (selected) {
					++selections;
				}
			}
			enable("All Debugging", selections == eventCheckMap.size());
			enable("Force Input", bot.overrideInput);
			enable("Less CPU", bot.disableRendering);
			enable("Disable Anti-Randoms", bot.disableRandoms);
			enable("Disable Auto Login", bot.disableAutoLogin);
		}
	}

	public JCheckBoxMenuItem getCheckBox(String key) {
		return commandCheckMap.get(key);
	}

	private void disable(String... items) {
		for (String item : items) {
			commandCheckMap.get(item).setSelected(false);
			commandCheckMap.get(item).setEnabled(false);
		}
	}

	private void enable(String item, boolean selected) {
		commandCheckMap.get(item).setSelected(selected);
		commandCheckMap.get(item).setEnabled(true);
	}

	private JMenu constructMenu(String title, String[] elems) {
		JMenu menu = new JMenu(title);
		for (String e : elems) {
			if (e.equals("-")) {
				menu.add(new JSeparator());
			} else {
				JMenuItem jmi;
				if (e.startsWith("Toggle")) {
					e = e.substring("Toggle".length());
					char state = e.charAt(0);
					e = e.substring(2);
					jmi = new JCheckBoxMenuItem(e);
					if ((state == 't') || (state == 'T')) {
						jmi.setSelected(true);
					}
					if (DEBUG_MAP.containsKey(e)) {
						eventCheckMap.put(e, (JCheckBoxMenuItem) jmi);
					}
					commandCheckMap.put(e, (JCheckBoxMenuItem) jmi);
				} else {
					jmi = new JMenuItem(e);
					commandMenuItem.put(e, jmi);
				}
				jmi.addActionListener(listener);
				jmi.setActionCommand(title + "." + e);
				menu.add(jmi);
			}
		}
		return menu;
	}

}
