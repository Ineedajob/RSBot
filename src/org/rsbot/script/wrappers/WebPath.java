package org.rsbot.script.wrappers;

import org.rsbot.script.methods.MethodContext;

import java.util.EnumSet;
import java.util.logging.Logger;

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

	private final Logger log = Logger.getLogger(WebPath.class.getName());

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
		if (options.contains(TraversalOption.SPACE_ACTIONS) && methods.walking.getDestination() != null && methods.calc.distanceTo(methods.walking.getDestination()) > random(2, 8)) {
			return true;
		}
		RSTile nextTile = getNext();
		if (nextTile != null) {
			int nextTileIndex = getNextIndex();
			if (nextTileIndex < web.length - 1 && methods.walking.isLocal(web[nextTileIndex])) {
				nextTileIndex++;
				nextTile = web[nextTileIndex];
			}
			log.severe(nextTile.toString());
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
		for (int i = web.length - 1; i > -1; i--) {
			if (methods.calc.tileOnMap(web[i])) {
				finalTile = web[i];
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
		int i = web.length;
		for (i = web.length - 1; i > -1; i--) {
			if (methods.calc.tileOnMap(web[i])) {
				finalIndex = i;
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
