package org.rsbot.script.methods;

import org.rsbot.client.MenuGroupNode;
import org.rsbot.client.MenuItemNode;
import org.rsbot.event.EventMulticaster;
import org.rsbot.event.listeners.PaintListener;
import org.rsbot.script.internal.wrappers.Deque;
import org.rsbot.script.internal.wrappers.Queue;

import java.awt.*;
import java.util.LinkedList;
import java.util.regex.Pattern;

/**
 * Context menu related operations.
 */
public class Menu extends MethodProvider {

	private static final Pattern HTML_TAG = Pattern.compile("(^[^<]+>|<[^>]+>|<[^>]+$)");

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
			if (idx == -1) {
				return false;
			}
			if (idx == 0) {
				methods.mouse.click(true);
				return true;
			}
			methods.mouse.click(false);
			return clickIndex(idx);
		} else if (idx == -1) {
			while (isOpen()) {
				methods.mouse.moveRandomly(750);
				sleep(random(100, 500));
			}
			return false;
		}
		return clickIndex(idx);
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
		if (!isOpen()) {
			return false;
		}
		String[] items = getItems();
		if (items.length <= i) {
			return false;
		}
		if (isCollapsed()) {
			Queue<MenuGroupNode> groups = new Queue<MenuGroupNode>(methods.client.getCollapsedMenuItems());
			int idx = 0, mainIdx = 0;
			for (MenuGroupNode g = groups.getHead(); g != null; g = groups.getNext(), ++mainIdx) {
				Queue subItems = new Queue(g.getItems());
				int subIdx = 0;
				for (MenuItemNode item = (MenuItemNode) subItems.getHead(); item != null; item = (MenuItemNode) subItems.getNext(), ++subIdx) {
					if (idx++ == i) {
						if (subItems.size() == 1) {
							return clickMain(items, mainIdx);
						} else {
							return clickSub(items, mainIdx, subIdx);
						}
					}
				}
			}
			return false;
		} else {
			return clickMain(items, i);
		}
	}

	private boolean clickMain(String[] items, int i) {
		Point menu = getLocation();
		int xOff = random(4, items[i].length() * 4);
		int yOff = 21 + 15 * i + random(3, 12);
		methods.mouse.move(menu.x + xOff, menu.y + yOff, 2, 2);
		if (isOpen()) {
			methods.mouse.click(true);
			return true;
		}
		return false;
	}

	private boolean clickSub(String[] items, int mIdx, int sIdx) {
		Point menuLoc = getLocation();
		int x = random(menuLoc.x + 4, menuLoc.x + 4 * items[mIdx].length() * 4);
		methods.mouse.move(x, menuLoc.y + 21 + 15 * mIdx + random(3, 12), 2, 2);
		sleep(random(0, 150));

		if (isOpen()) {
			Point subLoc = getSubMenuLocation();
			x = random(subLoc.x + 4, subLoc.x + 4 + items[sIdx].length() * 4);
			methods.mouse.move(x, methods.mouse.getLocation().y, 2, 0);
			sleep(random(50, 150));

			if (isOpen()) {
				int y = 21 + 15 * sIdx + random(3, 12);
				methods.mouse.move(x, y, 2, 2);
				sleep(random(25, 150));

				if (isOpen()) {
					methods.mouse.click(true);
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * Returns an array of the first parts of each item in the
	 * current menu context.
	 *
	 * @return The first half. "Walk here", "Trade with", "Follow".
	 */
	public String[] getActions() {
		return getMenuItemPart(true);
	}

	/**
	 * Returns the index in the menu for a given action. Starts at 0.
	 *
	 * @param action The action that you want the index of.
	 * @return The index of the given option in the context menu; otherwise -1.
	 */
	public int getIndex(String action) {
		action = action.toLowerCase();
		String[] items = getItems();
		for (int i = 0; i < items.length; i++) {
			// I believe contains fits more than startsWith
			if (items[i].toLowerCase().contains(action)) {
				return i;
			}
		}
		return -1;
	}

	/**
	 * Returns an array of each item in the current menu context.
	 *
	 * @return First half + second half. As displayed in RuneScape.
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
				output.add(action + ' ' + option);
			}
		}
		return output.toArray(new String[output.size()]);
	}

	/**
	 * Returns the menu's location.
	 *
	 * @return The screen space point if the menu is open; otherwise null.
	 */
	public Point getLocation() {
		if (isOpen()) {
			return new Point(methods.client.getMenuX() + 4, methods.client.getMenuY() + 4);
		}
		return null;
	}

	private String[] getMenuItemPart(boolean firstPart) {
		LinkedList<String> itemsList = new LinkedList<String>();
		if (isCollapsed()) {
			Queue<MenuGroupNode> menu = new Queue<MenuGroupNode>(methods.client.getCollapsedMenuItems());
			for (MenuGroupNode mgn = menu.getHead(); mgn != null; mgn = menu.getNext()) {
				Queue<MenuItemNode> submenu = new Queue<MenuItemNode>(mgn.getItems());
				for (MenuItemNode min = submenu.getHead(); min != null; min = submenu.getNext()) {
					itemsList.add(firstPart ? min.getAction() : min.getOption());
				}
			}
		} else {
			Deque<MenuItemNode> menu = new Deque<MenuItemNode>(methods.client.getMenuItems());
			for (MenuItemNode min = menu.getHead(); min != null; min = menu.getNext()) {
				itemsList.add(firstPart ? min.getAction() : min.getOption());
			}
		}
		String[] items = itemsList.toArray(new String[itemsList.size()]);
		LinkedList<String> output = new LinkedList<String>();
		if (isCollapsed()) {
			for (int i = 0; i < items.length; i++) {
				String item = items[i];
				output.add(item == null ? "" : stripFormatting(item));
			}
		} else {
			for (int i = items.length - 1; i >= 0; i--) {
				String item = items[i];
				output.add(item == null ? "" : stripFormatting(item));
			}
		}
		return output.toArray(new String[output.size()]);
	}

	/**
	 * Returns an array of the second parts of each item in the
	 * current menu context.
	 *
	 * @return The second half. "<user name>".
	 */
	public String[] getOptions() {
		return getMenuItemPart(false);
	}

	/**
	 * Returns the menu's item count.
	 *
	 * @return The menu size.
	 */
	public int getSize() {
		return getItems().length;
	}

	/**
	 * Returns the submenu's location.
	 *
	 * @return The screen space point of the submenu if the menu is collapsed; otherwise null.
	 */
	public Point getSubMenuLocation() {
		if (isCollapsed()) {
			return new Point(methods.client.getSubMenuX() + 4, methods.client.getSubMenuY() + 4);
		}
		return null;
	}

	/**
	 * Checks whether or not the menu is collapsed.
	 *
	 * @return <tt>true</tt> if the menu is collapsed; otherwise <tt>false</tt>.
	 */
	public boolean isCollapsed() {
		return methods.client.isMenuCollapsed();
	}

	/**
	 * Checks whether or not the menu is open.
	 *
	 * @return <tt>true</tt> if the menu is open; otherwise <tt>false</tt>.
	 */
	public boolean isOpen() {
		return methods.client.isMenuOpen();
	}

	/**
	 * For internal use only: sets up the menuListener.
	 */
	public void setupListener() {
		if (menuListenerStarted) {
			return;
		}
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
	 * Strips HTML tags.
	 *
	 * @param input The string you want to parse.
	 * @return The parsed {@code String}.
	 */
	private String stripFormatting(String input) {
		return HTML_TAG.matcher(input).replaceAll("");
	}
}