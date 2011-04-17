package org.rsbot.script.methods;

import org.rsbot.script.util.Filter;
import org.rsbot.script.wrappers.RSPlayer;

import java.util.HashSet;
import java.util.Set;

/**
 * Player related operations.
 */
public class Players extends MethodProvider {

	/**
	 * A filter that accepts all matches.
	 */
	public static final Filter<RSPlayer> ALL_FILTER = new Filter<RSPlayer>() {
		public boolean accept(RSPlayer player) {
			return true;
		}
	};

	Players(final MethodContext ctx) {
		super(ctx);
	}

	/**
	 * Returns an <tt>RSPlayer</tt> object representing the current player.
	 *
	 * @return An <tt>RSPlayer</tt> object representing the player.
	 */
	public RSPlayer getMyPlayer() {
		return new RSPlayer(methods, methods.client.getMyRSPlayer());
	}

	/**
	 * Returns an array of all valid <tt>RSPlayer</tt>s.
	 *
	 * @return All valid RSPlayers.
	 */
	public RSPlayer[] getAll() {
		return getAll(Players.ALL_FILTER);
	}

	/**
	 * Returns an array of all valid <tt>RSPlayer</tt>s.
	 *
	 * @param filter Filters out unwanted matches.
	 * @return All valid RSPlayers.
	 */
	public RSPlayer[] getAll(final Filter<RSPlayer> filter) {
		int[] indices = methods.client.getRSPlayerIndexArray();
		org.rsbot.client.RSPlayer[] array = methods.client.getRSPlayerArray();
		Set<RSPlayer> players = new HashSet<RSPlayer>();
		for (int index : indices) {
			if (index != 0 && array[index] != null) {
				RSPlayer player = new RSPlayer(methods, array[index]);
				if (filter.accept(player)) {
					players.add(player);
				}
			}
		}
		return players.toArray(new RSPlayer[players.size()]);
	}

	/**
	 * Returns the <tt>RSPlayer</tt> that is nearest, out of all of the Players
	 * accepted by the provided filter.
	 *
	 * @param filter Filters unwanted matches.
	 * @return An <tt>RSPlayer</tt> object representing the nearest player that
	 *         was accepted by the provided Filter; or null if there are no
	 *         matching players in the current region.
	 */
	public RSPlayer getNearest(final Filter<RSPlayer> filter) {
		int min = 20;
		RSPlayer closest = null;
		org.rsbot.client.RSPlayer[] players = methods.client.getRSPlayerArray();
		int[] indices = methods.client.getRSPlayerIndexArray();
		for (int index : indices) {
			if (players[index] == null) {
				continue;
			}
			RSPlayer player = new RSPlayer(methods, players[index]);
			if (filter.accept(player)) {
				int distance = methods.calc.distanceTo(player);
				if (distance < min) {
					min = distance;
					closest = player;
				}
			}
		}
		return closest;
	}

	/**
	 * Returns the <tt>RSPlayer</tt> that is nearest, out of all of the Players
	 * with the provided name.
	 *
	 * @param name The name of the <tt>RSPlayer</tt> that you are searching for.
	 * @return An <tt>RSPlayer</tt> object representing the nearest player with
	 *         the provided name; or null if there are no matching players in
	 *         the current region.
	 */
	public RSPlayer getNearest(final String name) {
		return getNearest(new Filter<RSPlayer>() {
			public boolean accept(RSPlayer player) {
				return player.getName().equals(name);
			}
		});
	}

	/**
	 * Returns the <tt>RSPlayer</tt> that is nearest, out of all of the Players
	 * with the provided combat level.
	 *
	 * @param level The combat level of the <tt>RSPlayer</tt> that you are
	 *              searching for.
	 * @return An <tt>RSPlayer</tt> object representing the nearest player with
	 *         the provided combat level; or null if there are no matching
	 *         players in the current region.
	 */
	public RSPlayer getNearest(final int level) {
		return getNearest(new Filter<RSPlayer>() {
			public boolean accept(RSPlayer player) {
				return player.getCombatLevel() == level;
			}
		});
	}

}
