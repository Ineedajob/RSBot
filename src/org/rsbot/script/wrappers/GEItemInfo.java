package org.rsbot.script.wrappers;

/**
 * Stores information about an item loaded from the Grand Exchange.
 *
 * @author Arbiter
 */
@Deprecated
public class GEItemInfo {
	/**
	 * The price change in 30 days.
	 */
	private final String change30;

	/**
	 * The price change in 90 days.
	 */
	private final String change90;

	/**
	 * The price change in 180 days.
	 */
	private final String change180;

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
	 * @param id          Item ID number
	 * @param minPrice    Current minimum price
	 * @param maxPrice    Current maximum price
	 * @param marketPrice Current market price
	 * @param change30    Change in the last thirty days
	 * @param change90    Change in the last ninety days
	 * @param change180   Change in the last 180 days
	 */
	public GEItemInfo(final int id, final int minPrice, final int maxPrice, final int marketPrice, final String change30, final String change90, final String change180) {
		this.minPrice = minPrice;
		this.maxPrice = maxPrice;
		this.marketPrice = marketPrice;
		this.change30 = change30;
		this.change90 = change90;
		this.change180 = change180;

	}

	/**
	 * @return Price change in the last 30 days.
	 */
	public String getChange30Days() {
		return change30;
	}

	/**
	 * @return Price change in the last 90 days.
	 */
	public String getChange90Days() {
		return change90;
	}

	/**
	 * @return Price change in the last 180 days.
	 */
	public String getChange180Days() {
		return change180;
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