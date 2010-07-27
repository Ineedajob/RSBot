package org.rsbot.script.wrappers;

/**
 * Stores information about an item loaded from the Grand Exchange.
 * 
 * @author Aelin
 */
public class GEItemInfo {
    /**
     * The price change in 7 days.
     */
    private String changeSeven;

    /**
     * The price change in 30 days.
     */
    private final String changeThirty;

    /**
     * Current market price.
     */
    private final int marketPrice;

    /**
     * Maximum price.
     */
    private final int maxPrice;

    /**
     * Minimum price.
     */
    private final int minPrice;

    /**
     * Default constructor.
     *
     * @param id           Item ID number
     * @param minPrice     Current minimum price
     * @param maxPrice     Current maximum price
     * @param marketPrice  Current market price
     * @param changeSeven  Change in the last seven days
     * @param changeThirty Change in the last thirty days
     */
    public GEItemInfo(final int id, final int minPrice, final int maxPrice, final int marketPrice, final String changeSeven, final String changeThirty) {
        this.minPrice = minPrice;
        this.maxPrice = maxPrice;
        this.marketPrice = marketPrice;
        this.changeSeven = changeSeven;
        this.changeThirty = changeThirty;

    }

    /**
     * @return Price change in the last seven days.
     */
    public String getChangeSevenDays() {
        return changeSeven;
    }

    /**
     * @return Price change in the last thirty days.
     */
    public String getChangeThirtyDays() {
        return changeThirty;
    }

    /**
     * @return Current market price.
     */
    public int getMarketPrice() {
        return marketPrice;
    }

    /**
     * @return Current maximum price.
     */
    public int getMaxPrice() {
        return maxPrice;
    }

    /**
     * @return Current minimum price
     */
    public int getMinPrice() {
		return minPrice;
	}
}
