package org.rsbot.script.methods;

import org.rsbot.script.wrappers.RSComponent;
import org.rsbot.script.wrappers.RSItem;
import org.rsbot.script.wrappers.RSItemDef;
import org.rsbot.script.wrappers.RSObject;

import java.awt.*;
import java.util.LinkedList;

/**
 * Inventory related operations.
 */
public class Inventory extends MethodProvider {

	public static final int INTERFACE_INVENTORY = 149;
	public static final int INTERFACE_INVENTORY_BANK = 763;

	Inventory(MethodContext ctx) {
		super(ctx);
	}

	/**
	 * Gets the inventory interface.
	 *
	 * @return the inventory interface
	 */
	public RSComponent getInterface() {
		if (methods.interfaces.get(INTERFACE_INVENTORY_BANK).isValid()) {
			RSComponent bankInv = methods.interfaces.getComponent(INTERFACE_INVENTORY_BANK, 0);
			if (bankInv != null && bankInv.getAbsoluteX() > 50) {
				return bankInv;
			}
		}

		// Tab needs to be open for it to update it's content -.-
		if (methods.game.getCurrentTab() != Game.TAB_INVENTORY) {
			methods.game.openTab(Game.TAB_INVENTORY);
			sleep(random(400, 900));
		}

		return methods.interfaces.getComponent(INTERFACE_INVENTORY, 0);
	}

	/**
	 * Drops all items with the same specified id.
	 *
	 * @param leftToRight <tt>true</tt> to drop items from left to right.
	 * @param items	   The item IDs to drop
	 */
	public void dropAllExcept(boolean leftToRight, int... items) {
		if (getCountExcept(items) != 0) {
			if (!leftToRight) {
				for (int c = 0; c < 4; c++) {
					for (int r = 0; r < 7; r++) {
						boolean found = false;
						for (int i = 0; i < items.length && !found; ++i) {
							found = items[i] == getItems()[c + r * 4].getID();
						}
						if (!found) {
							dropItem(c, r);
						}
					}
				}
			} else {
				for (int r = 0; r < 7; r++) {
					for (int c = 0; c < 4; c++) {
						boolean found = false;
						for (int i = 0; i < items.length && !found; ++i) {
							found = items[i] == getItems()[c + r * 4].getID();
						}
						if (!found) {
							dropItem(c, r);
						}
					}
				}
			}
			sleep(random(500, 800));
		}
	}

	/**
	 * Drops all items with the same specified id. This method drops items vertically going down the inventory.
	 *
	 * @param items The item IDs to drop.
	 * @return <tt>true</tt> at all times.
	 * @see #dropAllExcept(boolean, int...)
	 */
	public boolean dropAllExcept(int... items) {
		dropAllExcept(false, items);
		return true;
	}

	/**
	 * Drops the item in the specified column and row.
	 *
	 * @param col The column the item is in.
	 * @param row The row the item is in.
	 */
	public void dropItem(int col, int row) {
		if (methods.interfaces.canContinue()) {
			methods.interfaces.clickContinue();
			sleep(random(800, 1300));
		}
		if (methods.game.getCurrentTab() != Game.TAB_INVENTORY
				&& !methods.interfaces.get(Bank.INTERFACE_BANK).isValid()
				&& !methods.interfaces.get(Store.INTERFACE_STORE).isValid()) {
			methods.game.openTab(Game.TAB_INVENTORY);
		}
		if (col < 0 || col > 3 || row < 0 || row > 6)
			return;
		if (getItems()[col + row * 4].getID() == -1)
			return;
		Point p;
		p = methods.mouse.getLocation();
		if (p.x < 563 + col * 42 || p.x >= 563 + col * 42 + 32
				|| p.y < 213 + row * 36 || p.y >= 213 + row * 36 + 32) {
			methods.mouse.move(getInterface()
					.getComponents()[row * 4 + col].getCenter(), 10, 10);
		}
		methods.mouse.click(false);
		sleep(random(10, 25));
		methods.menu.doAction("drop");
		sleep(random(25, 50));
	}

	/**
	 * Checks whether or not your inventory contains the provided item ID.
	 *
	 * @param itemID The item(s) you wish to evaluate.
	 * @return <tt>true</tt> if your inventory contains an item with the
	 *         ID provided; otherwise <tt>false</tt>.
	 * @see #containsOneOf(int...)
	 * @see #containsAll(int...)
	 */
	public boolean contains(int itemID) {
		return getItem(itemID) != null;
	}

	/**
	 * Checks whether or not your inventory contains all of the provided
	 * item IDs.
	 *
	 * @param itemID The item(s) you wish to evaluate.
	 * @return <tt>true</tt> if your inventory contains at least one of all of
	 *         the item IDs provided; otherwise <tt>false</tt>.
	 * @see #containsOneOf(int...)
	 */
	public boolean containsAll(int... itemID) {
		for (int i : itemID) {
			if (getItem(i) == null)
				return false;
		}
		return true;
	}

	/**
	 * Checks whether or not your inventory contains at least one of
	 * the provided item IDs.
	 *
	 * @param itemID The item ID to check for.
	 * @return <tt>true</tt> if inventory contains one of the specified items;
	 *         otherwise <tt>false</tt>.
	 * @see #containsAll(int...)
	 */
	public boolean containsOneOf(int... itemID) {
		RSItem[] items = getItems();
		for (RSItem item : items) {
			for (int i : itemID) {
				if (item.getID() == i)
					return true;
			}
		}
		return false;
	}

	/**
	 * Checks whether or not your inventory is full.
	 *
	 * @return <tt>true</tt> if your inventory contains 28 items; otherwise
	 *         <tt>false</tt>.
	 */
	public boolean isFull() {
		return getCount() == 28;
	}

	/**
	 * Checks whether or not an inventory item is selected.
	 *
	 * @return <tt>true</tt> if an item in your inventory is selected; otherwise <tt>false</tt>.
	 */
	public boolean isItemSelected() {
		return getSelectedItemIndex() != -1;
	}

	/**
	 * Uses two items together.
	 *
	 * @param item	   The item to use on another item.
	 * @param targetItem The item you want the first parameter to be used on.
	 * @return <tt>true</tt> if the "use" action had been used on both items; otherwise <tt>false</tt>.
	 */
	public boolean useItem(RSItem item, RSItem targetItem) {
		if (methods.game.getCurrentTab() != Game.TAB_INVENTORY) {
			methods.game.openTab(Game.TAB_INVENTORY);
		}
		return item.doAction("Use") && targetItem.doAction("Use");
	}

	/**
	 * Uses an item on an object.
	 *
	 * @param item		 The item to use on another item.
	 * @param targetObject The RSObject you want the first parameter to be used on.
	 * @return <tt>true</tt> if the "use" action had been used on both the RSItem and RSObject; otherwise <tt>false</tt>.
	 */
	public boolean useItem(RSItem item, RSObject targetObject) {
		if (methods.game.getCurrentTab() != Game.TAB_INVENTORY) {
			methods.game.openTab(Game.TAB_INVENTORY);
		}
		return item.doAction("Use")
				&& targetObject.doAction("Use");
	}

	/**
	 * Randomizes a point.
	 *
	 * @param inventoryPoint The inventory point to be randomized.
	 * @return A randomized <tt>Point</tt> from the center of the given <tt>Point</tt>.
	 */
	public Point randomizeItemPoint(Point inventoryPoint) {
		return new Point(inventoryPoint.x + random(-10, 10),
				inventoryPoint.y + random(-10, 10));
	}

	/**
	 * Gets the selected item name.
	 *
	 * @return The name of the current selected item,
	 *         or null if none is selected.
	 */
	public String getSelectedItemName() {
		String name = methods.client.getSelectedItemName();
		return !isItemSelected() || name == null ? null : name.replaceAll("<[\\w\\d]+=[\\w\\d]+>", "");
	}

	/**
	 * Gets the selected item index.
	 *
	 * @return The index of current selected item,
	 *         or -1 if none is selected.
	 */
	public int getSelectedItemIndex() {
		RSComponent[] comps = getInterface().getComponents();
		for (int i = 0; i < Math.min(28, comps.length); ++i) {
			if (comps[i].getBorderThickness() == 2)
				return i;
		}
		return -1;
	}

	/**
	 * Gets the selected inventory item.
	 *
	 * @return The current selected item,
	 *         or <tt>null</tt> if none is selected.
	 */
	public RSItem getSelectedItem() {
		int index = getSelectedItemIndex();
		return index == -1 ? null : getItemAt(index);
	}

	/**
	 * Clicks selected inventory item, if it's selected.
	 *
	 * @param leftClick <tt>true</tt> for left button click,
	 *                  <tt>false</tt> for right button.
	 * @return <tt>true</tt> if item was selected, <tt>false</tt> if not.
	 */
	public boolean clickSelectedItem(boolean leftClick) {
		RSItem item = getSelectedItem();
		return item != null && item.doClick(true);
	}

	/**
	 * Left-clicks on the selected item.
	 *
	 * @return <tt>true</tt> if item was selected, </tt>false</tt> if not.
	 * @see #clickSelectedItem(boolean)
	 */
	public boolean clickSelectedItem() {
		return clickSelectedItem(true);
	}

	/**
	 * Gets inventory item at specified index.
	 *
	 * @param index The index of inventory item.
	 * @return The item, or <tt>null</tt> if not found.
	 */
	public RSItem getItemAt(int index) {
		RSComponent comp = getInterface().getComponent(index);
		return 0 <= index && index < 28 && comp != null ?
				new RSItem(methods, comp) : null;
	}

	/**
	 * Gets all the items in the inventory.
	 *
	 * @return <tt>RSItem</tt> array of the current inventory items or new <tt>RSItem[0]</tt>.
	 */
	public RSItem[] getItems() {
		RSComponent invIface = getInterface();
		if (invIface != null) {
			if (invIface.getComponents().length > 0) {
				int len = 0;
				RSComponent[] comps = invIface.getComponents();
				for (RSComponent com : comps) {
					if (com.getType() == 5) {
						++len;
					}
				}
				RSItem[] inv = new RSItem[len];
				for (int i = 0; i < len; ++i) {
					RSComponent item = comps[i];
					int idx = item.getComponentIndex();
					if (idx >= 0) {
						inv[idx] = new RSItem(methods, item);
					} else {
						return new RSItem[0];
					}
				}
				return inv;
			}
		}

		return new RSItem[0];
	}

	/**
	 * Gets all the items in the inventory matching any of the provided IDs.
	 *
	 * @param ids Valid IDs.
	 * @return <tt>RSItem</tt> array of the matching inventory items.
	 */
	public RSItem[] getItems(int... ids) {
		LinkedList<RSItem> items = new LinkedList<RSItem>();
		for (RSItem item : getItems()) {
			for (int i : ids) {
				if (item.getID() == i) {
					items.add(item);
					break;
				}
			}
		}
		return items.toArray(new RSItem[items.size()]);
	}

	/**
	 * Gets all the items in the inventory. If the tab is not
	 * currently open, it does not open it and returns the last
	 * known array of items in the tab.
	 *
	 * @return <tt>RSItem</tt> array of the cached inventory items or new <tt>RSItem[0]</tt>.
	 */
	public RSItem[] getCachedItems() {
		RSComponent invIface = methods.interfaces.getComponent(INTERFACE_INVENTORY, 0);
		if (invIface != null) {
			if (invIface.getComponents().length > 0) {
				int len = 0;
				for (RSComponent com : invIface.getComponents()) {
					if (com.getType() == 5) {
						++len;
					}
				}

				RSItem[] inv = new RSItem[len];
				for (int i = 0; i < len; ++i) {
					RSComponent item = invIface.getComponents()[i];
					int idx = item.getComponentIndex();
					if (idx >= 0) {
						inv[idx] = new RSItem(methods, item);
					} else {
						return new RSItem[0];
					}
				}

				return inv;
			}
		}

		return new RSItem[0];
	}

	/**
	 * Gets the ID of an item in the inventory with a given name.
	 *
	 * @param name The name of the item you wish to find.
	 * @return The ID of the item or -1 if not in inventory.
	 */
	public int getItemID(String name) {
		RSItem[] items = getItems();
		int slot = -1;
		for (RSItem item : items) {
			RSItemDef def = item.getDefinition();
			if (def != null && def.getName().contains(name)) {
				slot = item.getID();
				break;
			}
		}
		return slot;
	}

	/**
	 * Gets the first item in the inventory with any of the
	 * provided IDs.
	 *
	 * @param ids The IDs of the item to find.
	 * @return The first <tt>RSItem</tt> for the given IDs; otherwise null.
	 */
	public RSItem getItem(int... ids) {
		RSItem[] items = getItems();
		for (RSItem item : items) {
			for (int id : ids) {
				if (item.getID() == id)
					return item;
			}
		}
		return null;
	}

	/**
	 * Gets the count of all the items in the inventory without
	 * any of the provided IDs. This ignores stack sizes.
	 *
	 * @param ids The item IDs to exclude.
	 * @return The count.
	 */
	public int getCountExcept(int... ids) {
		return getCountExcept(false, ids);
	}

	/**
	 * Gets the count of all the items in the inventory without
	 * any of the provided IDs.
	 *
	 * @param includeStacks <tt>true</tt> to count the stack
	 *                      sizes of each item; <tt>false</tt> to count a stack as
	 *                      a single item.
	 * @param ids		   The item IDs to exclude.
	 * @return The count.
	 */
	public int getCountExcept(boolean includeStacks, int... ids) {
		RSItem[] items = getItems();
		int count = 0;
		for (RSItem i : items) {
			if (i.getID() == -1) {
				continue;
			}
			boolean skip = false;
			for (int id : ids) {
				if (i.getID() == id) {
					skip = true;
					break;
				}
			}
			if (!skip) {
				count += includeStacks ? i.getStackSize() : 1;
			}
		}
		return count;
	}

	/**
	 * Gets the count of all the items in the inventory with
	 * the any of the specified IDs. This ignores stack sizes.
	 *
	 * @param itemIDs the item IDs to include
	 * @return The count.
	 */
	public int getCount(int... itemIDs) {
		return getCount(false, itemIDs);
	}

	/**
	 * Gets the count of all the items in the inventory with
	 * the any of the specified IDs.
	 *
	 * @param includeStacks <tt>true</tt> to count the stack
	 *                      sizes of each item; <tt>false</tt> to count a stack as
	 *                      a single item.
	 * @param itemIDs	   the item IDs to include
	 * @return The count.
	 */
	public int getCount(boolean includeStacks, int... itemIDs) {
		int total = 0;

		for (RSItem item : getItems()) {
			if (item == null) {
				continue;
			}

			for (int ID : itemIDs) {
				if (item.getID() == ID) {
					total += includeStacks ? item.getStackSize() : 1;
				}
			}
		}

		return total;
	}

	/**
	 * Gets the count of all items in your inventory
	 * ignoring stack sizes.
	 *
	 * @return The count.
	 */
	public int getCount() {
		return getCount(false);
	}

	/**
	 * Gets the count of all items in your inventory.
	 *
	 * @param includeStacks <tt>false</tt> if stacked items should
	 *                      be counted as a single item; otherwise <tt>true</tt>.
	 * @return The count.
	 */
	public int getCount(boolean includeStacks) {
		int count = 0;
		RSItem[] items = getItems();
		for (RSItem item : items) {
			int iid = item.getID();
			if (iid != -1) {
				if (includeStacks) {
					count += item.getStackSize();
				} else {
					++count;
				}
			}
		}

		return count;
	}

}
