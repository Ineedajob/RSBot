package org.rsbot.script.methods;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Obtains information on tradeable items from the Grand Exchange website.
 *
 * @author Aion
 */
@SuppressWarnings("deprecation")
public class GrandExchange extends MethodProvider {

	private static final String HOST = "http://services.runescape.com";
	private static final String GET = "/m=itemdb_rs/viewitem.ws?obj=";

	private static final Pattern PATTERN = Pattern.compile(
			"(?i)<td><img src=\".+obj_sprite\\.gif\\?id=(\\d+)\" alt=\"(.+)\"");

	GrandExchange() {
		super(null);
	}

	/**
	 * Gets the name of the given item ID. Should not be used.
	 *
	 * @param itemID The item ID to look for.
	 * @return The name of the given item ID or an empty String if unavailable.
	 * @see GrandExchange#lookup(int)
	 */
	public String getItemName(int itemID) {
		GEItem geItem = lookup(itemID);
		if (geItem != null) {
			return geItem.getName();
		}
		return "";
	}

	/**
	 * Gets the ID of the given item name. Should not be used.
	 *
	 * @param itemName The name of the item to look for.
	 * @return The ID of the given item name or -1 if unavailable.
	 * @see GrandExchange#lookup(java.lang.String)
	 */
	public int getItemID(String itemName) {
		GEItem geItem = lookup(itemName);
		if (geItem != null) {
			return geItem.getID();
		}
		return -1;
	}

	/**
	 * This method loads a item's full info from the Grand Exchange website. Use only when requiring all info.
	 *
	 * @param itemID Item to load
	 * @return GEItemInfo containing item information
	 * @see #lookup(int)
	 */
	@Deprecated
	public org.rsbot.script.wrappers.GEItemInfo loadItemInfo(int itemID) {
		GEItem item = lookup(itemID);
		if (item == null) {
			return null;
		}
		return new org.rsbot.script.wrappers.GEItemInfo(itemID, item.getMinPrice(), item.getMaxPrice(), item.getMarketPrice(), Double.toString(item.getChange30Days()), Double.toString(item.getChange90Days()), Double.toString(item.getChange180Days()));
	}

	/**
	 * Fetches the max price from the grand exchange
	 *
	 * @param itemID Item to load
	 * @return Max price
	 * @see #lookup(int)
	 */
	@Deprecated
	public int getMaxPrice(int itemID) {
		GEItem item = lookup(itemID);
		if (item != null) {
			return item.getMaxPrice();
		}
		return -1;
	}

	/**
	 * Fetches the market price from the grand exchange
	 *
	 * @param itemID Item to load
	 * @return Market price
	 * @see #lookup(int)
	 */
	@Deprecated
	public int getMarketPrice(int itemID) {
		GEItem item = lookup(itemID);
		if (item != null) {
			return item.getMarketPrice();
		}
		return -1;
	}

	/**
	 * Fetches the min price from the grand exchange
	 *
	 * @param itemID Item to load
	 * @return Min price
	 * @see #lookup(int)
	 */
	@Deprecated
	public int getMinPrice(int itemID) {
		GEItem item = lookup(itemID);
		if (item != null) {
			return item.getMinPrice();
		}
		return -1;
	}

	/**
	 * Collects data for a given item ID from the Grand Exchange website.
	 *
	 * @param itemID The item ID.
	 * @return An instance of GrandExchange.GEItem; <code>null</code> if unable to fetch data.
	 */
	public GEItem lookup(int itemID) {
		try {
			URL url = new URL(GrandExchange.HOST + GrandExchange.GET + itemID);
			BufferedReader br = new BufferedReader(new InputStreamReader(url.openStream()));
			String input;
			boolean exists = false;
			int i = 0;
			double[] values = new double[6];
			String name = "", examine = "";
			while ((input = br.readLine()) != null) {
				if (input.contains("<div class=\"brown_box main_ge_page") && !exists) {
					if (!input.contains("vertically_spaced")) {
						return null;
					}
					exists = true;
					br.readLine();
					br.readLine();
					name = br.readLine();
				} else if (input.contains("<img id=\"item_image\" src=\"")) {
					examine = br.readLine();
				} else if (input.matches("(?i).+ (price|days):</b> .+")) {
					values[i] = parse(input);
					i++;
				} else if (input.matches("<div id=\"legend\">"))
					break;
			}
			return new GEItem(name, examine, itemID, values);
		} catch (IOException ignore) {
		}
		return null;
	}

	/**
	 * Collects data for a given item name from the Grand Exchange website.
	 *
	 * @param itemName The name of the item.
	 * @return An instance of GrandExchange.GEItem; <code>null</code> if unable to fetch data.
	 */
	public GEItem lookup(String itemName) {
		try {
			URL url = new URL(GrandExchange.HOST + "/m=itemdb_rs/results.ws?query="
					+ itemName + "&price=all&members=");
			BufferedReader br = new BufferedReader(new InputStreamReader(url.openStream()));
			String input;
			while ((input = br.readLine()) != null) {
				if (input.contains("<div id=\"search_results_text\">")) {
					input = br.readLine();
					if (input.contains("Your search for")) {
						return null;
					}
				} else if (input.startsWith("<td><img src=")) {
					Matcher matcher = GrandExchange.PATTERN.matcher(input);
					if (matcher.find()) {
						if (matcher.group(2).contains(itemName)) {
							return lookup(Integer.parseInt(matcher.group(1)));
						}
					}
				}
			}
		} catch (IOException ignored) {
		}
		return null;
	}

	private double parse(String str) {
		if (str != null && !str.isEmpty()) {
			str = stripFormatting(str);
			str = str.substring(str.indexOf(58) + 2, str.length());
			str = str.replace(",", "");
			if (!str.endsWith("%")) {
				if (!str.endsWith("k") && !str.endsWith("m")) {
					return Double.parseDouble(str);
				}
				return Double.parseDouble(str.substring(0, str.length() - 1))
						* (str.endsWith("m") ? 1000000 : 1000);
			}
			int k = str.startsWith("+") ? 1 : -1;
			str = str.substring(1);
			return Double.parseDouble(str.substring(0, str.length() - 1)) * k;
		}
		return -1D;
	}

	private String stripFormatting(String str) {
		if (str != null && !str.isEmpty())
			return str.replaceAll("(^[^<]+>|<[^>]+>|<[^>]+$)", "");
		return "";
	}

	public static class GEItem {

		private String name;
		private String examine;

		private int id;

		private int minPrice;
		private int marketPrice;
		private int maxPrice;

		private double change30;
		private double change90;
		private double change180;

		GEItem(String name, String examine, int id, double[] values) {
			this.name = name;
			this.examine = examine;
			this.id = id;
			minPrice = (int) values[0];
			marketPrice = (int) values[1];
			maxPrice = (int) values[2];
			change30 = values[3];
			change90 = values[4];
			change180 = values[5];
		}

		/**
		 * Gets the change in price for the last 30 days of this item.
		 *
		 * @return The change in price for the last 30 days of this item.
		 */
		public double getChange30Days() {
			return change30;
		}

		/**
		 * Gets the change in price for the last 90 days of this item.
		 *
		 * @return The change in price for the last 90 days of this item.
		 */
		public double getChange90Days() {
			return change90;
		}

		/**
		 * Gets the change in price for the last 180 days of this item.
		 *
		 * @return The change in price for the last 180 days of this item.
		 */
		public double getChange180Days() {
			return change180;
		}

		/**
		 * Gets the ID of this item.
		 *
		 * @return The ID of this item.
		 */
		public int getID() {
			return id;
		}

		/**
		 * Gets the market price of this item.
		 *
		 * @return The market price of this item.
		 */
		public int getMarketPrice() {
			return marketPrice;
		}

		/**
		 * Gets the maximum market price of this item.
		 *
		 * @return The maximum market price of this item.
		 */
		public int getMaxPrice() {
			return maxPrice;
		}

		/**
		 * Gets the minimum market price of this item.
		 *
		 * @return The minimum market price of this item.
		 */
		public int getMinPrice() {
			return minPrice;
		}

		/**
		 * Gets the name of this item.
		 *
		 * @return The name of this item.
		 */
		public String getName() {
			return name;
		}

		/**
		 * Gets the description of this item.
		 *
		 * @return The description of this item.
		 */
		public String getDescription() {
			return examine;
		}
	}
}