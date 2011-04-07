package org.rsbot.script.wrappers;

import java.util.ArrayList;
import java.util.List;

/**
 * The web map data.
 *
 * @author Timer
 */

public class WebMap {
	/**
	 * The tile array.
	 */
	private final WebTile[] tiles;

	public WebMap(final WebTile[] tiles) {
		this.tiles = tiles;
	}

	/**
	 * Gets a web tile by index.
	 *
	 * @param index The index of the tile in the map.
	 * @return The tile.
	 */
	public WebTile getWebTile(final int index) {
		try {
			return tiles[index];
		} catch (Exception e) {
			return null;
		}
	}

	/**
	 * The surrounding indices of a web tile.
	 *
	 * @param tile The web tile you wish to access.
	 * @return The indices.
	 */
	public int[] getSurroundingOf(final WebTile tile) {
		List<Integer> goodL = new ArrayList<Integer>();
		int[] neighbors = tile.connectingIndex();
		for (int in : neighbors) {
			WebTile ctile = getWebTile(in);
			if (ctile != null) {
				if (ctile.req != null) {
					if (ctile.req.canDo()) {
						goodL.add(in);
					}
				} else {
					goodL.add(in);
				}
			}
		}
		int[] good = new int[goodL.size()];
		if (goodL.size() > 0) {
			for (int i = 0; i < goodL.size(); i++) {
				good[i] = goodL.get(i);
			}
		}
		return good;
	}

	/**
	 * The nearest web tile.
	 *
	 * @param tile The web tile from a tile.
	 * @return The resulting web tile.
	 */
	public WebTile getWebTile(final RSTile tile) {
		double maxDist = 999999.0;
		WebTile webTile = null;
		for (WebTile ctrl : tiles) {
			if (maxDist == 0.0) {
				break;
			}
			double sqrt = Math.sqrt((ctrl.getX() - tile.getX())
					* (ctrl.getX() - tile.getX())
					+ (ctrl.getY() - tile.getY())
					* (ctrl.getY() - tile.getY()));
			if (sqrt < maxDist) {
				webTile = ctrl;
				maxDist = sqrt;
			}
		}
		return webTile;
	}

	/**
	 * Finds the heuristic.
	 *
	 * @param start The starting tile.
	 * @param end   The ending tile.
	 * @return The heuristic.
	 */
	public double heuristic(WebTile start, WebTile end) {
		double dx = start.getX() - end.getX();
		double dy = start.getY() - end.getY();
		if (dx < 0)
			dx = -dx;
		if (dy < 0)
			dy = -dy;
		return dx < dy ? dy : dx;
	}

	/**
	 * Gets the dist.
	 *
	 * @param start Starting web tile.
	 * @param end   Ending web tile.
	 * @return The dist.
	 */
	public double dist(WebTile start, WebTile end) {
		if (start.getX() != end.getX() && start.getY() != end.getY()) {
			RSTile curr = start.tile();
			RSTile dest = end.tile();
			return Math.sqrt((curr.getX() - dest.getX())
					* (curr.getX() - dest.getX())
					+ (curr.getY() - dest.getY())
					* (curr.getY() - dest.getY()));
		} else {
			return 1.0;
		}
	}
}
