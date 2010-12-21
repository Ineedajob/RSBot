package org.rsbot.script.methods;

import org.rsbot.script.wrappers.GEItemInfo;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;

/**
 * Obtains information on tradeable items from the Grand Exchange website.
 *
 * @author Arbiter
 */
public class GrandExchange extends MethodProvider {

	GrandExchange() {
		super(null);
	}

	/**
	 * Fetches item min price from GE. Faster return than GEItemInfo.
	 *
	 * @param itemID Item to load
	 * @return Min price for itemID.
	 */
	public int getMinPrice(int itemID) {
		int minPrice = 0;
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
						break;
					} else if (line.contains("m")) {
						line = line.replace(".", "");
						line = line.replace("m", "");
						line = line.trim();
						minPrice = Integer.parseInt(line) * 100000;
						break;
					} else {
						minPrice = Integer.parseInt(line);
						break;
					}
				}
			}
		} catch (final Exception ignored) {
		}
		return minPrice;
	}

	/**
	 * Fetches item market price from GE. Faster return than GEItemInfo.
	 *
	 * @param itemID Item to load
	 * @return Market price for itemID.
	 */
	public int getMarketPrice(int itemID) {
		int marketPrice = 0;
		try {
			final URL url = new URL("http://services.runescape.com/m=itemdb_rs/viewitem.ws?obj=" + itemID);
			final BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream()));

			String line;

			while ((line = reader.readLine()) != null) {
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
			}
		} catch (final Exception ignored) {
		}
		return marketPrice;
	}

	public int getMaxPrice(int itemID) {
		int maxPrice = 0;
		try {
			final URL url = new URL("http://services.runescape.com/m=itemdb_rs/viewitem.ws?obj=" + itemID);
			final BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream()));

			String line;

			while ((line = reader.readLine()) != null) {
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
			}
		} catch (final Exception ignored) {
		}
		return maxPrice;
	}

	public String getChange30Days(int itemID) {
		String change30 = "";
		try {
			final URL url = new URL("http://services.runescape.com/m=itemdb_rs/viewitem.ws?obj=" + itemID);
			final BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream()));

			String line;

			while ((line = reader.readLine()) != null) {
				if (line.contains("30 Days:")) {
					line = line.replace("<b>30 Days:</b> <span class=\"drop\">", "");
					line = line.replace("<b>30 Days:</b> <span class=\"stay\">", "");
					line = line.replace("<b>30 Days:</b> <span class=\"rise\">", "");
					line = line.replace("</span>", "");
					line = line.trim();
					change30 = line;
				}
			}
		} catch (final Exception ignored) {
		}
		return change30;
	}

	public String getChange90Days(int itemID) {
		String change90 = "";
		try {
			final URL url = new URL("http://services.runescape.com/m=itemdb_rs/viewitem.ws?obj=" + itemID);
			final BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream()));

			String line;

			while ((line = reader.readLine()) != null) {
				if (line.contains("90 Days:")) {
					line = line.replace("<b>90 Days:</b> <span class=\"drop\">", "");
					line = line.replace("<b>90 Days:</b> <span class=\"stay\">", "");
					line = line.replace("<b>90 Days:</b> <span class=\"rise\">", "");
					line = line.replace("</span>", "");
					line = line.trim();
					change90 = line;
				}
			}
		} catch (final Exception ignored) {
		}
		return change90;
	}

	public String getChange180Days(int itemID) {
		String change180 = "";
		try {
			final URL url = new URL("http://services.runescape.com/m=itemdb_rs/viewitem.ws?obj=" + itemID);
			final BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream()));

			String line;

			while ((line = reader.readLine()) != null) {
				if (line.contains("180 Days:")) {
					line = line.replace("<b>180 Days:</b> <span class=\"drop\">", "");
					line = line.replace("<b>180 Days:</b> <span class=\"stay\">", "");
					line = line.replace("<b>180 Days:</b> <span class=\"rise\">", "");
					line = line.replace("</span>", "");
					line = line.trim();
					change180 = line;
				}
			}
		} catch (final Exception ignored) {
		}
		return change180;
	}

	public int getLastChange(String itemName) {
		int change = 0;
		String suffix = itemName.replace(" ", "+");
		try {
			final URL url = new URL("http://services.runescape.com/m=itemdb_rs/results.ws?query=" + suffix);
			final BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream()));
			String line;
			while ((line = reader.readLine()) != null) {
				if (line.contains("> " + itemName + "</a>")) {
					line = reader.readLine();
					line = reader.readLine();
					if (line.contains("<span class=\"drop\">")) {
						line = line.replace("<td><span class=\"drop\">", "");
						line = line.replace("</span></td>", "");
						line = line.replace("+", "");
						change = Integer.parseInt(line);
					}
					if (line.contains("<span class=\"rise\">")) {
						line = line.replace("<td><span class=\"rise\">", "");
						line = line.replace("</span></td>", "");
						line = line.replace("+", "");
						change = Integer.parseInt(line);
					}
					if (line.contains("<span class=\"stay\">")) {
						line = line.replace("<td><span class=\"stay\">", "");
						line = line.replace("</span></td>", "");
						line = line.replace("+", "");
						change = Integer.parseInt(line);
					}
				}
				if (change != 0)
					break;
			}
		} catch (Exception e) {
		}
		return change;
	}

	/**
	 * This method loads a item's full info from the Grand Exchange website. Use only when requiring all info.
	 *
	 * @param itemID Item to load
	 * @return GEItemInfo containing item information
	 */
	public GEItemInfo loadItemInfo(final int itemID) {
		int minPrice = 0;
		int maxPrice = 0;
		int marketPrice = 0;
		String change30 = "";
		String change90 = "";
		String change180 = "";

		try {
			final URL url = new URL("http://services.runescape.com/m=itemdb_rs/viewitem.ws?obj=" + itemID);
			final BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream()));

			String line;

			while ((line = reader.readLine()) != null) {
				if (minPrice != 0 && maxPrice != 0 && marketPrice != 0 && change30 != "" && change90 != "" && change180 != "")
					break;
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
				if (line.contains("30 Days:")) {
					line = line.replace("<b>30 Days:</b> <span class=\"drop\">", "");
					line = line.replace("<b>30 Days:</b> <span class=\"stay\">", "");
					line = line.replace("<b>30 Days:</b> <span class=\"rise\">", "");
					line = line.replace("</span>", "");
					line = line.trim();
					change30 = line;
				}
				if (line.contains("90 Days:")) {
					line = line.replace("<b>90 Days:</b> <span class=\"drop\">", "");
					line = line.replace("<b>90 Days:</b> <span class=\"stay\">", "");
					line = line.replace("<b>90 Days:</b> <span class=\"rise\">", "");
					line = line.replace("</span>", "");
					line = line.trim();
					change90 = line;
				}
				if (line.contains("180 Days:")) {
					line = line.replace("<b>180 Days:</b> <span class=\"drop\">", "");
					line = line.replace("<b>180 Days:</b> <span class=\"stay\">", "");
					line = line.replace("<b>180 Days:</b> <span class=\"rise\">", "");
					line = line.replace("</span>", "");
					line = line.trim();
					change180 = line;
				}
			}
		} catch (final Exception ignored) {
		}

		return new GEItemInfo(itemID, minPrice, maxPrice, marketPrice, change30, change90, change180);
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
				while ((line = reader.readLine()) != null) {
					if (line.contains("<div class=\"brown_box main_ge_page vertically_spaced\">")) {
						line = reader.readLine();
						line = reader.readLine();
						return reader.readLine();
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