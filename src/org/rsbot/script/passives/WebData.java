package org.rsbot.script.passives;

import org.rsbot.script.PassiveScript;
import org.rsbot.script.PassiveScriptManifest;
import org.rsbot.script.internal.wrappers.TileFlags;
import org.rsbot.script.methods.Web;
import org.rsbot.script.wrappers.RSTile;
import org.rsbot.service.WebQueue;

import java.util.HashMap;

@PassiveScriptManifest(name = "Web Data Collector", authors = {"Timer"})
public class WebData extends PassiveScript {
	private RSTile lb = null;
	public final HashMap<RSTile, TileFlags> rs_map = new HashMap<RSTile, TileFlags>();

	@Override
	public boolean activateCondition() {
		final RSTile curr_base = game.getMapBase();
		return game.isLoggedIn() && (lb == null || !lb.equals(curr_base));
	}

	public int loop() {
		try {
			final RSTile curr_base = game.getMapBase();
			if (lb != null && lb.equals(curr_base)) {
				return -1;
			}
			sleep(5000);
			lb = curr_base;
			Node t;
			log("Analysing new region into your local web.");
			int plane = game.getPlane();
			final int flags[][] = walking.getCollisionFlags(plane);
			for (int i = 0; i < 104; i++) {
				for (int j = 0; j < 104; j++) {
					RSTile start = new RSTile(curr_base.getX() + i, curr_base.getY() + j, plane);
					int base_x = game.getBaseX(), base_y = game.getBaseY();
					int curr_x = start.getX() - base_x, curr_y = start.getY() - base_y;
					t = new Node(curr_x, curr_y);
					RSTile offset = walking.getCollisionOffset(plane);
					int off_x = offset.getX();
					int off_y = offset.getY();
					int x = t.x, y = t.y;
					int f_x = x - off_x, f_y = y - off_y;
					int here = flags[f_x][f_y];
					TileFlags tI = new TileFlags(start);
					if ((here & TileFlags.Flags.WALL_EAST) != 0) {
						tI.addKey(TileFlags.Keys.WALL_EAST);
					}
					if ((here & TileFlags.Flags.WALL_WEST) != 0) {
						tI.addKey(TileFlags.Keys.WALL_WEST);
					}
					if ((here & TileFlags.Flags.WALL_NORTH) != 0) {
						tI.addKey(TileFlags.Keys.WALL_NORTH);
					}
					if ((here & TileFlags.Flags.WALL_SOUTH) != 0) {
						tI.addKey(TileFlags.Keys.WALL_SOUTH);
					}
					if ((here & TileFlags.Flags.WALL_NORTH_EAST) != 0) {
						tI.addKey(TileFlags.Keys.WALL_NORTH_EAST);
					}
					if ((here & TileFlags.Flags.WALL_NORTH_WEST) != 0) {
						tI.addKey(TileFlags.Keys.WALL_NORTH_WEST);
					}
					if ((here & TileFlags.Flags.WALL_SOUTH_EAST) != 0) {
						tI.addKey(TileFlags.Keys.WALL_SOUTH_EAST);
					}
					if ((here & TileFlags.Flags.WALL_SOUTH_WEST) != 0) {
						tI.addKey(TileFlags.Keys.WALL_SOUTH_WEST);
					}
					if ((here & TileFlags.Flags.BLOCKED) != 0) {
						tI.addKey(TileFlags.Keys.BLOCKED);
					} else {
						if ((here & TileFlags.Flags.WATER) != 0) {
							tI.addKey(TileFlags.Keys.TILE_WATER);
						}
					}
					if (!tI.isQuestionable()) {
						tI.addKey(TileFlags.Keys.TILE_CLEAR);
					}
					if (!Web.map.containsKey(start) && f_y > 0 && f_x < 103) {
						rs_map.put(start, tI);
					} else {
						try {
							if (f_y > 0 && f_x < 103 && !Web.map.get(start).equals(tI)) {
								WebQueue.Remove(start);
							}
						} catch (NullPointerException ignored) {
						}
					}
				}
			}
			WebQueue.Add(rs_map);
			return -1;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return -1;
	}

	@Override
	public int iterationSleep() {
		return 1000;
	}

	private class Node {
		public int x, y;

		public Node(int x, int y) {
			this.x = x;
			this.y = y;
		}
	}
}
