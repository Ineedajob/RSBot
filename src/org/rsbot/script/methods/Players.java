package org.rsbot.script.methods;

import java.util.LinkedList;
import java.util.List;

import org.rsbot.script.wrappers.RSPlayer;

/**
 * Player related operations.
 */
public class Players extends MethodProvider {

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
		int[] validPlayers = methods.client.getRSPlayerIndexArray();
		org.rsbot.client.RSPlayer[] array = methods.client.getRSPlayerArray();
		List<RSPlayer> players = new LinkedList<RSPlayer>();
		for (int index : validPlayers) {
			players.add(new RSPlayer(methods, array[index]));
		}
		return players.toArray(new RSPlayer[players.size()]);
	}

	/**
	 * Returns the <tt>RSPlayer</tt> that is nearest, out of all of the Players
	 * with the provided level. Can return null.
	 * 
	 * @param level The level of the <tt>RSPlayer</tt> that you are searching.
	 * @return An <tt>RSPlayer</tt> object representing the nearest player with
	 * one of the provided names; or null if there are no matching players in
	 * the current region.
	 */
	public RSPlayer getNearestByLevel(int level) {
		int Dist = 20;
		RSPlayer closest = null;
		int[] validPlayers = methods.client.getRSPlayerIndexArray();
		org.rsbot.client.RSPlayer[] players = methods.client
				.getRSPlayerArray();

		for (int element : validPlayers) {
			if (players[element] == null) {
				continue;
			}
			RSPlayer player = new RSPlayer(methods, players[element]);
			try {
				if (level != player.getCombatLevel()) {
					continue;
				}
				int distance = methods.calc.distanceTo(player);
				if (distance < Dist) {
					Dist = distance;
					closest = player;
				}
			} catch (Exception ignored) {
			}
		}
		return closest;
	}

	/**
	 * Returns the <tt>RSPlayer</tt> that is nearest, out of all of the Players
	 * with the provided minimum and maximum level. Can return null.
	 * 
	 * @param min The minimum level of the <tt>RSPlayer</tt> that you are searching.
	 * @param max The minimum level of the <tt>RSPlayer</tt> that you are searching.
	 * @return An <tt>RSPlayer</tt> object representing the nearest player within
	 * the level range; or null if there are no matching players in the current region.
	 */
	public RSPlayer getNearestByLevel(int min, int max) {
		int Dist = 20;
		RSPlayer closest = null;
		int[] validPlayers = methods.client.getRSPlayerIndexArray();
		org.rsbot.client.RSPlayer[] players = methods.client
				.getRSPlayerArray();

		for (int element : validPlayers) {
			if (players[element] == null) {
				continue;
			}
			RSPlayer player = new RSPlayer(methods, players[element]);
			try {
				if (player.getCombatLevel() < min
						&& player.getCombatLevel() > max) {
					continue;
				}
				int distance = methods.calc.distanceTo(player);
				if (distance < Dist) {
					Dist = distance;
					closest = player;
				}
			} catch (Exception ignored) {
			}
		}
		return closest;
	}

	/**
	 * Returns the <tt>RSPlayer</tt> that is nearest, out of all of the Players
	 * with the provided name. Can return null.
	 * 
	 * @param name The name of the <tt>RSPlayer</tt> that you are searching.
	 * @return An <tt>RSPlayer</tt> object representing the nearest player with
	 * the provided name; or null if there are no matching players in
	 * the current region.
	 */
	public RSPlayer getNearest(String name) {
		int Dist = 20;
		RSPlayer closest = null;
		int[] validPlayers = methods.client.getRSPlayerIndexArray();
		org.rsbot.client.RSPlayer[] players = methods.client
				.getRSPlayerArray();

		for (int element : validPlayers) {
			if (players[element] == null) {
				continue;
			}
			RSPlayer player = new RSPlayer(methods, players[element]);
			try {
				if (!name.equals(player.getName())) {
					continue;
				}
				int distance = methods.calc.distanceTo(player);
				if (distance < Dist) {
					Dist = distance;
					closest = player;
				}
			} catch (Exception ignored) {
			}
		}
		return closest;
	}

}
