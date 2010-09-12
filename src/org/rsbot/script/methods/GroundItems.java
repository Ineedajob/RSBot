package org.rsbot.script.methods;

import org.rsbot.script.internal.wrappers.Deque;
import org.rsbot.script.wrappers.RSArea;
import org.rsbot.script.wrappers.RSItem;
import org.rsbot.script.wrappers.RSGroundItem;
import org.rsbot.script.wrappers.RSTile;

import java.util.ArrayList;
import java.util.List;

/**
 * Provides access to ground items.
 */
public class GroundItems extends MethodProvider {

    GroundItems(final MethodContext ctx) {
        super(ctx);
    }

    /**
     * Returns the first (but not the closest) item found in a square within
     * (range) away from you.
     *
     * @param range The maximum distance.
     * @return The first ground item found; or null if none were found.
     */
    public RSGroundItem getFirst(int range) {
        int pX = methods.players.getMyPlayer().getLocation().getX();
        int pY = methods.players.getMyPlayer().getLocation().getY();
        int minX = pX - range;
        int minY = pY - range;
        int maxX = pX + range;
        int maxY = pY + range;
        for (int x = minX; x < maxX; x++) {
            for (int y = minY; y < maxY; y++) {
                RSGroundItem[] items = getAllAt(x, y);
                if (items.length > 0)
                    return items[0];
            }
        }
        return null;
    }

    /**
     * 
     * @param range The range to check items for.
     * @return <tt>RSGroundItem</tt> array containing all of the items in range.
     */
    public RSGroundItem[] getAll(int range) {
        ArrayList<RSGroundItem> temp = new ArrayList<RSGroundItem>();
        int pX = methods.players.getMyPlayer().getLocation().getX();
        int pY = methods.players.getMyPlayer().getLocation().getY();
        int minX = pX - range;
        int minY = pY - range;
        int maxX = pX + range;
        int maxY = pY + range;
        for (int x = minX; x < maxX; x++) {
            for (int y = minY; y < maxY; y++) {
                RSGroundItem[] items = getAllAt(x, y);
                if (items.length > 0) {
                    temp.add(items[0]);
                }
            }
        }
        if (temp.size() < 1)
            return null;
        RSGroundItem[] array = new RSGroundItem[temp.size()];
        for (int i = 0; i < temp.size(); i++) {
            array[i] = temp.get(i);
        }
        return array;
    }

    /**
     * Returns the first (but not the closest) item with a specified id in the
     * playable(visible) area.
     *
     * @param ids The IDs of the items to look for.
     * @return The first matching ground item found; or null if none were found.
     */
    public RSGroundItem get(int... ids) {
        return get(52, ids);
    }

    /**
     * Returns the first (but not the closest) item with any of the specified
     * IDs in a square within (range) away from you.
     *
     * @param range The maximum distance.
     * @param ids   The IDs of the items to look for.
     * @return The first matching ground item found; or null if none were found.
     */
    public RSGroundItem get(int range, int[] ids) {
        int pX = methods.players.getMyPlayer().getLocation().getX();
        int pY = methods.players.getMyPlayer().getLocation().getY();
        int minX = pX - range;
        int minY = pY - range;
        int maxX = pX + range;
        int maxY = pY + range;
        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                RSGroundItem[] items = getAllAt(x, y);
                for (RSGroundItem item : items) {
                    int iId = item.getItem().getID();
                    for (int id : ids) {
                        if (iId == id)
                            return item;
                    }
                }
            }
        }
        return null;
    }

    /**
     * Returns all the ground items at a tile on the current plane.
     *
     * @param x The x position of the tile in the world.
     * @param y The y position of the tile in the world.
     * @return An array of the ground items on the specified tile.
     */
    public RSGroundItem[] getAllAt(int x, int y) {
        if (!methods.game.isLoggedIn())
            return new RSGroundItem[0];

        List<RSGroundItem> list = new ArrayList<RSGroundItem>();

        org.rsbot.client.NodeCache itemNC = methods.client.getRSItemNodeCache();
        int id = x | y << 14 | methods.client.getPlane() << 28;

        org.rsbot.client.NodeListCache itemNLC = (org.rsbot.client.NodeListCache) methods.nodes.lookup(itemNC, id);

        if (itemNLC == null)
            return new RSGroundItem[0];

        Deque itemNL = new Deque(itemNLC.getNodeList());
        for (org.rsbot.client.RSItem item = (org.rsbot.client.RSItem) itemNL.getHead(); item != null; item = (org.rsbot.client.RSItem) itemNL.getNext()) {
            list.add(new RSGroundItem(methods, new RSTile(x, y), new RSItem(methods, item)));
        }

        return list.toArray(new RSGroundItem[list.size()]);
    }

    /**
     * Returns all the ground items at a tile on the current plane.
     *
     * @param t The tile.
     * @return An array of the ground items on the specified tile.
     */
    public RSGroundItem[] getAllAt(RSTile t) {
        return getAllAt(t.getX(), t.getY());
    }

    /**
     * Returns an RSItemTile representing the nearest item on the ground with an
     * ID that matches any of the IDS provided. Can return null. RSItemTile is a
     * subclass of RSTile.
     *
     * @param ids The IDs to look for.
     * @return RSItemTile of the nearest item with the an ID that matches any in
     * the array of IDs provided; or null if no matching ground items were found.
     */
    public RSGroundItem getNearest(int... ids) {
        int dist = 9999999;
        int pX = methods.players.getMyPlayer().getLocation().getX();
        int pY = methods.players.getMyPlayer().getLocation().getY();
        int minX = pX - 52;
        int minY = pY - 52;
        int maxX = pX + 52;
        int maxY = pY + 52;
        RSGroundItem itm = null;
        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                RSGroundItem[] items = getAllAt(x, y);
                for (RSGroundItem item : items) {
                    int iId = item.getItem().getID();
                    for (int id : ids) {
                        if (iId == id && methods.calc.distanceTo(item.getLocation()) < dist) {
                            dist = methods.calc.distanceTo(item.getLocation());
                            itm = item;
                        }
                    }
                }
            }
        }
        return itm;
    }

    /**
     * Searches for an item on the ground within the specified area.
     * 
     * @param toSearch The area to search within.
     * @return RSItemTile of the nearest item with the an ID that matches any in
     * the array of IDs provided; or null if no matching ground items were found.
     */
    public RSGroundItem getNearest(RSArea toSearch, int... ids) {
        int dist = 9999999;
        RSTile[][] areaTile = toSearch.getTiles();
        RSGroundItem itm = null;
        for (RSTile[] element : areaTile) {
            for (int y = 0; y < element.length; y++) {
                RSGroundItem[] items = getAllAt(element[y]);
                for (RSGroundItem item : items) {
                    int iId = item.getItem().getID();
                    for (int id : ids) {
                        if (iId == id && methods.calc.distanceTo(item.getLocation()) < dist) {
                            dist = methods.calc.distanceTo(item.getLocation());
                            itm = item;
                        }
                    }
                }
            }
        }
        return itm;
    }
}
