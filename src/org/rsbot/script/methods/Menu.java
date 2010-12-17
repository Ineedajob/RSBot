package org.rsbot.script.methods;

import org.rsbot.client.MenuItemNode;
import org.rsbot.event.EventMulticaster;
import org.rsbot.event.listeners.PaintListener;
import org.rsbot.script.internal.wrappers.Deque;

import java.awt.*;
import java.util.LinkedList;
import java.util.regex.Pattern;

/**
 * Context menu related operations.
 */
public class Menu extends MethodProvider {

	// Menu items are cached after each frame
	private static final Pattern stripFormatting = Pattern.compile("\\<.+?\\>");
	private final Object menuCacheLock = new Object();
	private String[] menuOptionsCache = new String[0];
	private String[] menuActionsCache = new String[0];
	private boolean menuListenerStarted = false;

	Menu(final MethodContext ctx) {
		super(ctx);
	}

	/**
	 * Clicks the menu option. Will left-click if the menu item is the first,
	 * otherwise open menu and click the option.
	 *
	 * @param action The action (or action substring) to click.
	 * @return <tt>true</tt> if the menu item was clicked; otherwise <tt>false</tt>.
	 */
	public boolean doAction(String action) {
		int idx = getIndex(action);
		if (!isOpen()) {
			if (idx == -1)
				return false;
			if (idx == 0) {
				methods.mouse.click(true);
				return true;
			} else {
				methods.mouse.click(false);
				idx = getIndex(action);
				return clickIndex(idx);
			}
		} else {
			if (idx == -1) {
				while (isOpen()) {
					methods.mouse.moveRandomly(750);
					sleep(random(100, 500));
				}
				return false;
			} else {
				return clickIndex(idx);
			}
		}
	}

	/**
	 * Checks whether or not a given action (or action substring) is present
	 * in the menu.
	 *
	 * @param action The action or action substring.
	 * @return <tt>true</tt> if present, otherwise <tt>false</tt>.
	 */
	public boolean contains(String action) {
		return getIndex(action) != -1;
	}

	/**
	 * Left clicks at the given index.
	 *
	 * @param i The index of the item.
	 * @return <tt>true</tt> if the mouse was clicked; otherwise <tt>false</tt>.
	 */
	public boolean clickIndex(int i) {
		if (!isOpen())
			return false;
		try {
			String[] items = getItems();
			if (items.length <= i)
				return false;
			Point menu = getLocation();
			int xOff = random(4, items[i].length() * 4);
			int yOff = 21 + 15 * i + random(3, 12);
			methods.mouse.move(menu.x + xOff, menu.y + yOff, 2, 2);
			if (!isOpen())
				return false;
			methods.mouse.click(true);
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	/**
	 * Returns an array of the first parts of each item in the current
	 * context menu actions.
	 *
	 * @return The first half, for example "Walk here" "Follow".
	 */
	public String[] getActions() {
		LinkedList<String> actionsList = new LinkedList<String>();

		if (methods.client.isMenuCollapsed()) {
			/*Queue groups = new Queue(methods.client.getCollapsedMenuItems());

			for (MenuGroupNode g = (MenuGroupNode) groups.getHead(); g != null; g = (MenuGroupNode) groups.getNext()) {
				Queue items = new Queue(g.getItems());

				for (MenuItemNode item = (MenuItemNode) items.getHead(); item != null; item = (MenuItemNode) items.getNext()) {
					actionsList.add(item.getAction());
				}
			}*/
			return new String[] {"Cancel"};
		} else {
			Deque items = new Deque(methods.client.getMenuItems());

			for (MenuItemNode item = (MenuItemNode) items.getHead(); item != null; item = (MenuItemNode) items.getNext()) {
				actionsList.add(item.getAction());
			}
		}

		String[] actions = actionsList.toArray(new String[actionsList.size()]);
		LinkedList<String> output = new LinkedList<String>();
		// Don't remove the commented line, Jagex switches every few updates, so
		// it's there for quick fixes.
		for (int i = actions.length - 1; i >= 0; --i) {
			//for (int i = 0; i < actions.length; ++i) {
			String action = actions[i];
			if (action != null) {
				String text = stripFomatting(action);
				output.add(text);
			} else {
				output.add("");
			}
		}

		return output.toArray(new String[output.size()]);
	}

	/**
	 * Returns the index (starts at 0) in the menu for a given action. -1 when invalid.
	 *
	 * @param action The String or a beginning of the String that you want the index of.
	 * @return The index of the given option in the context menu; or -1 if the
	 *         option was not found.
	 */
	public int getIndex(String action) {
		action = action.toLowerCase();
		String[] actions = getItems();
		for (int i = 0; i < actions.length; i++) {
			String a = actions[i];
			if (a.toLowerCase().startsWith(action))
				return i;
		}
		return -1;
	}

	/**
	 * Returns an array of each item in the current context menu actions.
	 *
	 * @return First half + second half. As displayed in rs.
	 */
	public String[] getItems() {
		String[] options;
		String[] actions;
		synchronized (menuCacheLock) {
			options = menuOptionsCache;
			actions = menuActionsCache;
		}
		LinkedList<String> output = new LinkedList<String>();
		//for (int i = Math.min(options.length, actions.length) - 1; i >= 0; --i) {
		for (int i = 0; i < Math.min(options.length, actions.length); ++i) {
			String option = options[i];
			String action = actions[i];
			if (option != null && action != null) {
				String text = stripFomatting(action) + ' ' + stripFomatting(option);
				output.add(text);
			}
		}
		return output.toArray(new String[output.size()]);
	}

	/**
	 * Returns the location of the menu. Returns null if not open.
	 *
	 * @return The screen space point if open; otherwise null.
	 */
	public Point getLocation() {
		if (!isOpen())
			return null;
		int x = methods.client.getMenuX();
		int y = methods.client.getMenuY();
		return new Point(x + 4, y + 4);
	}

	/**
	 * Returns a list of the second parts of each item in the current
	 * context menu actions.
	 *
	 * @return The second half. "<user name>".
	 */
	public String[] getOptions() {
		LinkedList<String> optionsList = new LinkedList<String>();

		if (methods.client.isMenuCollapsed()) {
			//Queue menu = new Queue(methods.client.getCollapsedMenuItems());
		} else {
			Deque menu = new Deque(methods.client.getMenuItems());

			for (MenuItemNode adn = (MenuItemNode) menu.getHead(); adn != null; adn = (MenuItemNode) menu.getNext()) {
				optionsList.add(adn.getOption());
			}
		}

		String[] options = optionsList.toArray(new String[optionsList.size()]);
		LinkedList<String> output = new LinkedList<String>();
		// Don't remove the commented line, Jagex switches every few updates, so
		// it's there for quick fixes.
		for (int i = options.length - 1; i >= 0; --i) {
			//for (int i = 0; i < options.length; ++i) {
			String option = options[i];
			if (option != null) {
				String text = stripFomatting(option);
				output.add(text);
			} else {
				output.add("");
			}
		}
		return output.toArray(new String[output.size()]);
	}

	/**
	 * Check whether menu is open, and returns true if opened, and false if otherwise
	 *
	 * @return true if menu is open, false otherwise
	 */
	public boolean isOpen() {
		return methods.client.isMenuOpen();
	}

	/**
	 * For internal use only: Sets up the menuListener.
	 */
	public void setupListener() {
		if (menuListenerStarted)
			return;

		menuListenerStarted = true;
		methods.bot.getEventManager().addListener(new PaintListener() {
			public void onRepaint(Graphics g) {
				synchronized (menuCacheLock) {
					menuOptionsCache = getOptions();
					menuActionsCache = getActions();
				}
			}
		}, EventMulticaster.PAINT_EVENT);
	}

	/**
	 * Removes HTML tags.
	 *
	 * @param input The string you want to parse.
	 * @return The parsed {@code String}.
	 */
	private String stripFomatting(String input) {
		return stripFormatting.matcher(input).replaceAll("");
	}

}
