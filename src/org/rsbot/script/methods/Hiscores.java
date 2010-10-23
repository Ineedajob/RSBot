package org.rsbot.script.methods;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import java.net.URL;

import java.util.HashMap;

/**
 * This class is used to fetch the stats of another player.
 * <p/>
 * Example:
 * Hiscores.Stats stats = hiscores.lookup("username");
 * int attack = stats.getCurrentLevel(Skills.ATTACK);
 *
 * @author Aion
 */
public class Hiscores extends MethodProvider {

	private static final String HOST = "http://hiscore.runescape.com";
	private static final String GET = "/index_lite.ws?player=";

	Hiscores() {
		super(null);
	}

	/**
	 * Collects data for a given player from the hiscore website.
	 *
	 * @param username The username
	 * @return An instance of Hiscores.Stats; <code>null</code> if unable to fetch data.
	 */
	public Stats lookup(String username) {
		String overall = null;
		HashMap<Integer, String> stats = new HashMap<Integer, String>();
		try {
			URL url = new URL(Hiscores.HOST + Hiscores.GET + username);
			BufferedReader br = new BufferedReader(new InputStreamReader(
					url.openStream()));
			overall = br.readLine();
			String line;
			for (int i = 0; i < 25; i++) {
				line = br.readLine();
				stats.put(i, line);
			}
		} catch (IOException ignored) {
		}
		if (!stats.isEmpty()) {
			return new Stats(username, overall, stats);
		}
		return null;
	}

	public static class Stats {

		private String username;
		private String overall;
		private HashMap<Integer, String> stats;

		Stats(String username, String overall, HashMap<Integer, String> hm) {
			this.username = username;
			this.overall = overall;
			this.stats = hm;
		}

		/**
		 * Gets the experience from the given skill index
		 *
		 * @param index The index of the skill
		 * @return The experience or -1
		 */
		public int getCurrentExp(int index) {
			if (stats != null) {
				if (stats.containsKey(index)) {
					return Integer.parseInt(stats.get(index).split(",")[2]);
				}
			}
			return -1;
		}

		/**
		 * Gets the level of the given skill index
		 *
		 * @param index The index of the skill
		 * @return The level or -1
		 */
		public int getCurrentLevel(int index) {
			if (stats != null) {
				if (stats.containsKey(index)) {
					return Integer.parseInt(stats.get(index).split(",")[1]);
				}
			}
			return -1;
		}

		/**
		 * Gets the rank for the given skill index
		 *
		 * @param index The index of the skill
		 * @return The rank or -1
		 */
		public int getCurrentRank(int index) {
			if (stats != null) {
				if (stats.containsKey(index)) {
					return Integer.parseInt(stats.get(index).split(",")[0]);
				}
			}
			return -1;
		}

		/**
		 * Gets the overall experience of the looked up player
		 *
		 * @return The overall experience or -1
		 */
		public int getOverallExp() {
			if (overall != null) {
				return Integer.parseInt(overall.split(",")[2]);
			}
			return -1;
		}

		/**
		 * Gets the overall rank of the looked up player
		 *
		 * @return The overall rank or -1
		 */
		public int getOverallRank() {
			if (overall != null) {
				return Integer.parseInt(overall.split(",")[0]);
			}
			return -1;
		}

		/**
		 * Gets the total level of the looked up player
		 *
		 * @return The total level or -1
		 */
		public int getTotalLevel() {
			if (overall != null) {
				return Integer.parseInt(overall.split(",")[1]);
			}
			return -1;
		}

		/**
		 * Gets the username of the looked up player
		 *
		 * @return The username
		 */
		public String getUsername() {
			return username;
		}
	}
}