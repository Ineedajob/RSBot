package org.rsbot.script.wrappers;

import org.rsbot.script.methods.MethodContext;

import java.util.EnumSet;

/**
 * A wrapper for traversing on a web path.
 *
 * @author Timer
 */

public class WebPath extends WebSkeleton {
	/**
	 * The web tile array.
	 */
	private final WebTile[] web;

	public WebPath(final MethodContext ctx, final WebTile[] tiles) {
		super(ctx);
		this.web = tiles;
	}

	/**
	 * Traverses on the path.
	 *
	 * @param options The flags to take into account while traversing the path.
	 * @return <tt>True</tt> if walked, otherwise false.
	 */
	public boolean traverse(EnumSet<TraversalOption> options) {
		if (options.contains(TraversalOption.HANDLE_RUN) && !methods.walking.isRunEnabled() && methods.walking.getEnergy() > 40) {
			methods.walking.setRun(true);
		}
		if (options.contains(TraversalOption.SPACE_ACTIONS) && methods.walking.getDestination() != null && methods.calc.distanceTo(methods.walking.getDestination()) > random(5, 12)) {
			return true;
		}
		RSTile nextTile = getNext();
		if (nextTile != null) {
			int nextTileIndex = getNextIndex();
			if (nextTileIndex < web.length) {
				RSTile offTile = web[nextTileIndex];
				if (methods.walking.isLocal(offTile)) {
					nextTile = offTile;
				}
			}
			RSLocalPath path = new RSLocalPath(methods, nextTile);
			path.random(2, 2);
			path.traverse(null);
		}
		return false;
	}

	/**
	 * Gets the tiles of the web skeleton.
	 *
	 * @return The tiles in a <tt>RSTile[]</tt>.
	 */
	public RSTile[] getTiles() {
		return web;
	}

	/**
	 * Get next tile.
	 *
	 * @return The next tile in the skeleton.
	 */
	public RSTile getNext() {
		RSTile finalTile = null;
		for (RSTile tile : web) {
			if (methods.calc.tileOnMap(tile)) {
				finalTile = tile;
			}
		}
		return finalTile;
	}

	/**
	 * Get next tile index off screen.
	 *
	 * @return The next tile in the skeleton.
	 */
	public int getNextIndex() {
		int finalIndex = -1;
		int i = 0;
		for (RSTile tile : web) {
			if (methods.calc.tileOnMap(tile)) {
				finalIndex = i;
			}
			i++;
		}
		for (int ii = i; ii < web.length; ii++) {
			if (!methods.calc.tileOnMap(web[ii])) {
				return ii;
			}
		}
		return finalIndex;
	}

	/**
	 * Gets the next start tile.
	 *
	 * @return The start tile.
	 */
	public RSTile getStart() {
		return web != null && web.length > 0 ? web[0] : null;
	}

	/**
	 * The end tile.
	 *
	 * @return The end tile.
	 */
	public RSTile getEnd() {
		return web != null && web.length > 0 ? web[web.length - 1] : null;
	}

	/**
	 * Returns if you're at your destination.
	 *
	 * @return <tt>true</tt> if at destination, otherwise false.
	 */
	public boolean atDestination() {
		return getEnd() != null ? methods.calc.distanceTo(getEnd()) < 8 : false;
	}
}
