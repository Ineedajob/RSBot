package org.rsbot.script.methods;

import org.rsbot.script.wrappers.GEItemInfo;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;

/**
 * Obtains information on tradeable items from the Grand Exchange website.
 *
 * @author Aelin
 */
public class GrandExchange extends MethodProvider {

    GrandExchange() {
    	super(null);
    }

    /**
     * This method loads a item's info from the grand exchange website.
     *
     * @param itemID Item to load
     * @return GEItemInfo containing item information
     */
    public GEItemInfo loadItemInfo(final int itemID) {
        int minPrice = 0;
        int maxPrice = 0;
        int marketPrice = 0;
        String changeSeven = "";
        String changeThirty = "";

        try {
            final URL url = new URL("http://services.runescape.com/m=itemdb_rs/viewitem.ws?obj=" + itemID);
            final BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream()));

            String line;

            while ((line = reader.readLine()) != null) {
                if (line.contains("Minimum price:")) {
                    line = line.replace("<b>Minimum price:</b> ", "");
                    line = line.replace(",", "");
                    if (line.contains("k")) {
                        line = line.replace(".", "");
                        line = line.replace("k", "");
                        line = line.trim();
                        minPrice = Integer.parseInt(line) * 100;
                    } else if (line.contains("m")) {
                        line = line.replace(".", "");
                        line = line.replace("m", "");
                        line = line.trim();
                        minPrice = Integer.parseInt(line) * 100000;
                    } else {
                        minPrice = Integer.parseInt(line);
                    }
                }

                if (line.contains("Market price:")) {
                    line = line.replace("<b>Market price:</b> ", "");
                    line = line.replace(",", "");
                    if (line.contains("k")) {
                        line = line.replace(".", "");
                        line = line.replace("k", "");
                        line = line.trim();
                        marketPrice = Integer.parseInt(line) * 100;
                    } else if (line.contains("m")) {
                        line = line.replace(".", "");
                        line = line.replace("m", "");
                        line = line.trim();
                        marketPrice = Integer.parseInt(line) * 100000;
                    } else {
                        marketPrice = Integer.parseInt(line);
                    }
                }

                if (line.contains("Maximum price:")) {
                    line = line.replace("<b>Maximum price:</b> ", "");
                    line = line.replace(",", "");
                    if (line.contains("k")) {
                        line = line.replace(".", "");
                        line = line.replace("k", "");
                        line = line.trim();
                        maxPrice = Integer.parseInt(line) * 100;
                    } else if (line.contains("m")) {
                        line = line.replace(".", "");
                        line = line.replace("m", "");
                        line = line.trim();
                        maxPrice = Integer.parseInt(line) * 100000;
                    } else {
                        maxPrice = Integer.parseInt(line);
                    }
                }

                if (line.contains("7 Days:")) {
                    line = line.replace("<b>7 Days:</b> <span class=\"stay\">", "");
                    line = line.replace("<b>7 Days:</b> <span class=\"stay\">", "");
                    line = line.replace("</span>", "");
                    line = line.trim();
                    changeSeven = line;
                }

                if (line.contains("30 Days:")) {
                    line = line.replace("<b>30 Days:</b> <span class=\"stay\">", "");
                    line = line.replace("<b>30 Days:</b> <span class=\"stay\">", "");
                    line = line.replace("</span>", "");
                    line = line.trim();
                    changeThirty = line;
                }
            }
        } catch (final Exception ignored) {
        }

        return new GEItemInfo(itemID, minPrice, maxPrice, marketPrice, changeSeven, changeThirty);
	}
    
    /**
     * Retrieves the market price for the given item ID.
     *
     * @param id the id of the desired item
     * @return returns the market price of the given item.
     */
    public int getMarketPrice(final int id) {
        GEItemInfo i = loadItemInfo(id);
        return i.getMarketPrice();
    }

    /**
     * Gets the item name via the online Runescape item database.
     * 
     * @param ids the ids this method will look up
     * @return returns the name(s) of the item(s) that match with the
     *         given ID(s)
     */
    public String getItemName(int... ids) {
        try {
            for (int r : ids) {
                URL url = new URL("http://itemdb-rs.runescape.com/viewitem.ws?obj=" + r);
                BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream()));

                String line;
                int i = 0;
                while ((line = reader.readLine()) != null) {
                    if (line.equals("<div class=" + '"' + "subsectionHeader" + '"' + ">")) {
                        i++;
                        continue;
                    }
                    if (i == 1) {
                        reader.close();
                        return line;
                    }
                }
            }
        } catch (Exception e) {
        }
        return null;
    }
    
    /**
     * Gets the item id via the online Runescape item database.
     * 
     * @param name The name of the item.
     * @return <tt>int</tt> ID of the specified item name.
     */
    public int getItemID(String name) {
        int ID = 0;
        try {
            URL url = new URL("http://services.runescape.com/m=itemdb_rs/results.ws?query=" + name + "&price=all&members=");
            BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream()));

            String line;
            while ((line = reader.readLine()) != null) {
                if (line.contains('"' + name + '"') && line.contains("sprite.gif?")) {
                    String str = line;
                    str = str.substring(str.indexOf("id=") + 3, str.indexOf("\" alt=\""));
                    ID = Integer.parseInt(str);
                    reader.close();
                    return ID;
                }
            }
        } catch (Exception e) {
        }
        return ID;
    }
}
