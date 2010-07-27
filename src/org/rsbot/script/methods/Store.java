package org.rsbot.script.methods;

import org.rsbot.script.wrappers.RSComponent;
import org.rsbot.script.wrappers.RSInterface;
import org.rsbot.script.wrappers.RSItem;

/**
 * Store related operations.
 */
public class Store extends MethodProvider {
	
    public static final int INTERFACE_STORE = 620;
    public static final int INTERFACE_STORE_BUTTON_CLOSE = 7;
    public static final int INTERFACE_STORE_BUTTON_PLAYERSTORE = 17;
    public static final int INTERFACE_STORE_BUTTON_MAINSTORE = 20;
    
    public static final int STOCK_MAIN = 24;
    public static final int STOCK_PLAYER = 26;
    
    private int stock = STOCK_MAIN;

    Store(final MethodContext ctx) {
        super(ctx);
    }

    /**
     * Tries to buy an item. 0 is All. 1, 5 and 10 use buy 1/5/10
     * while the other numbers use buy x.
     *
     * @param itemID The id of the item.
     * @param count  The number to buy.
     * @return <tt>true</tt> on success
     */
    public boolean buy(final int itemID, final int count) {
        if (count < 0)
            throw new IllegalArgumentException("count < 0 " + count);
        if (!isOpen())
            return false;
        final int inventoryCount = methods.inventory.getCount(true);
        RSItem item = getItem(itemID);
        if (item != null) {
        	for (int tries = 0; tries < 5; tries++) {
                switch (count) {
                    case 0: // Withdraw All
                    	item.doAction("Buy All");
                        break;
                    case 1: // Withdraw 1
                        item.doAction("Buy 1");
                        break;
                    case 5: // Withdraw 5
                        item.doAction("Buy 5");
                        break;
                    case 10: // Withdraw 10
                        item.doAction("Buy 10");
                        break;
                    case 50: // Withdraw 50
                        item.doAction("Buy 50");
                    default: // Withdraw x
                        item.doAction("Buy X");
                        sleep(random(900, 1100));
                        methods.inputManager.sendKeys("" + count, true);
                }
                sleep(random(500, 700));
                if (methods.inventory.getCount(true) > inventoryCount)
                    return true;
            }
        }
        return false;
    }

    /**
     * Closes the store interface.
     *
     * @return <tt>true</tt> if the interface is no longer open
     */
    public boolean close() {
        if (!isOpen())
            return true;
        methods.interfaces.getComponent(620, 18).doClick();
        sleep(random(500, 600));
        return !isOpen();
    }

    /**
     * Gets the store interface.
     *
     * @return the store <tt>RSInterface</tt>
     */
    public RSInterface getInterface() {
        return methods.interfaces.get(620);
    }

    /**
     * Gets the item at a given component index.
     * 
     * @param index The index of the component based off of the components in the Store interface.
     * @return <tt>RSComponent</tt> for the item at the given index; otherwise null.
     */
    public RSItem getItemAt(final int index) {
        final RSItem[] items = getItems();
        if (items != null) {
            for (final RSItem item : items) {
                if (item.getComponent().getComponentIndex() == index)
                    return item;
            }
        }

        return null;
    }

    /**
     * Gets the first item found with the given id.
     *
     * @param id ID of the item to get
     * @return The <tt>RSComponent</tt> of the item; otherwise null.
     */
    public RSItem getItem(final int id) {
        final RSItem[] items = getItems();
        if (items != null) {
            for (final RSItem item : items) {
                if (item.getID() == id)
                    return item;
            }
        }

        return null;
    }

    /**
     * Gets all the items in the store inventory.
     * 
     * @return An <tt>RSComponent</tt> array representing all of the components in the
     * stores <tt>RSInterface</tt>.
     */
    public RSItem[] getItems() {
        if ((getInterface() == null) || (getInterface().getComponent(stock) == null))
            return new RSItem[0];

        RSComponent[] components = getInterface().getComponent(stock).getComponents();
        RSItem[] items = new RSItem[components.length];
        for (int i = 0; i < components.length; ++i) {
        	items[i] = new RSItem(methods, components[i]);
        }
        return items;
    }

    /**
     * Returns whether or not the store interface is open.
     * 
     * @return <tt>true</tt> if the store interface is open, otherwise <tt>false</tt>.
     */
    public boolean isOpen() {
        return getInterface().isValid();
    }

    /**
     * Allows switching between main stock and player stock.
     *
     * @param mainStock <tt>true</tt> for MainStock; <tt>false</tt> for PlayerStock
     */
	public void setStock(final boolean mainStock) {
		stock = mainStock ? STOCK_MAIN : STOCK_PLAYER;
	}

}
