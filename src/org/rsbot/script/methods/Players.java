package org.rsbot.script.methods;

import java.util.HashSet;
import java.util.Set;

import org.rsbot.script.wrappers.RSPlayer;

/**
 * Player related operations.
 */
public class Players extends MethodProvider {

	/**
	 * Used to filter the players found by methods in the Players class.
	 */
	public static interface Qualifier {
		public boolean qualifies(RSPlayer player);
	}

	/**
	 * A qualifier that accepts all matches.
	 */
	public static final Qualifier PREDICATE_QUALIFIER = new Qualifier() {
		public boolean qualifies(RSPlayer player) {
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
		return getAll(Players.PREDICATE_QUALIFIER);
	}

	/**
	 * Returns an array of all valid <tt>RSPlayer</tt>s.
	 *
	 * @param qualifier Filters unwanted matches.
	 * @return All valid RSPlayers.
	 */
	public RSPlayer[] getAll(Qualifier qualifier) {
		int[] indices = methods.client.getRSPlayerIndexArray();
		org.rsbot.client.RSPlayer[] array = methods.client.getRSPlayerArray();
		Set<RSPlayer> players = new HashSet<RSPlayer>();
		for (int index : indices) {
			if (index != 0) {
				RSPlayer player = new RSPlayer(methods, array[index]);
				if (qualifier.qualifies(player)) {
					players.add(player);
				}
			}
		}
		return players.toArray(new RSPlayer[players.size()]);
	}

	/**
	 * Returns the <tt>RSPlayer</tt> that is nearest, out of all of the Players
	 * with the provided name. Can return null.
	 *
	 * @param qualifier Filters unwanted matches.
	 * @return An <tt>RSPlayer</tt> object representing the nearest player with
	 * the provided name; or null if there are no matching players in
	 * the current region.
	 */
	public RSPlayer getNearest(Qualifier qualifier) {
		int min = 20;
		RSPlayer closest = null;
		org.rsbot.client.RSPlayer[] players = methods.client.getRSPlayerArray();
		int[] indices = methods.client.getRSPlayerIndexArray();
		for (int index : indices) {
			if (players[index] == null) {
				continue;
			}
			RSPlayer player = new RSPlayer(methods, players[index]);
			if (qualifier.qualifies(player)) {
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
	 * with the provided name. Can return null.
	 *
	 * @param name The name of the <tt>RSPlayer</tt> that you are searching for.
	 * @return An <tt>RSPlayer</tt> object representing the nearest player with
	 * the provided name; or null if there are no matching players in
	 * the current region.
	 */
	public RSPlayer getNearest(final String name) {
		return getNearest(new Qualifier() {
			public boolean qualifies(RSPlayer player) {
				return player.getName().equals(name);
			}
		});
	}

	/**
	 * Returns the <tt>RSPlayer</tt> that is nearest, out of all of the Players
	 * with the provided combat level. Can return null.
	 *
	 * @param level The combat level of the <tt>RSPlayer</tt> that you are searching for.
	 * @return An <tt>RSPlayer</tt> object representing the nearest player with
	 * the provided combat evel; or null if there are no matching players in
	 * the current region.
	 */
	public RSPlayer getNearest(final int level) {
		return getNearest(new Qualifier() {
			public boolean qualifies(RSPlayer player) {
				return player.getCombatLevel() == level;
			}
		});
	}

}
