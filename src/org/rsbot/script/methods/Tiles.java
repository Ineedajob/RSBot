package org.rsbot.script.methods;

import org.rsbot.script.wrappers.RSTile;

import java.awt.*;

/**
 * Tile related operations.
 */
public class Tiles extends MethodProvider {

	Tiles(final MethodContext ctx) {
		super(ctx);
	}

	/**
	 * Clicks a tile if it is on screen with given offsets in 3D space.
	 *
	 * @param tile   The <code>RSTile</code> to do the action at.
	 * @param xd	 Distance from bottom left of the tile to bottom right. Ranges from 0-1.
	 * @param yd	 Distance from bottom left of the tile to top left. Ranges from 0-1.
	 * @param h	  Height to click the <code>RSTile</code> at. Use 1 for tables, 0 by default.
	 * @param action The action to perform at the given <code>RSTile</code>.
	 * @return <tt>true</tt> if no exceptions were thrown; otherwise <tt>false</tt>.
	 */
	public boolean doAction(RSTile tile, double xd, double yd, int h, String action) {
		Point location = methods.calc.tileToScreen(tile, xd, yd, h);
		if (location.x != -1 && location.y != -1) {
			methods.mouse.move(location, 3, 3);
			sleep(random(20, 100));
			return methods.menu.doAction(action);
		}
		return false;
	}

	/**
	 * Clicks a tile if it is on screen. It will left-click if the action is
	 * available as the default option, otherwise it will right-click and check
	 * for the action in the context methods.menu.
	 *
	 * @param tile   The RSTile that you want to click.
	 * @param action Action command to use on the Character (e.g "Attack" or
	 *               "Trade").
	 * @return <tt>true</tt> if the Character was clicked; otherwise
	 *         <tt>false</tt>.
	 */
	public boolean doAction(RSTile tile, String action) {
		int counter = 0;
		try {
			Point location = methods.calc.tileToScreen(tile);
			if (location.x == -1 || location.y == -1) {
				return false;
			}
			methods.mouse.move(location, 5, 5);
			while (!methods.menu.getItems()[0].toLowerCase().contains(action.toLowerCase()) && counter < 5) {
				location = methods.calc.tileToScreen(tile);
				methods.mouse.move(location, 5, 5);
				counter++;
			}
			if (methods.menu.getItems()[0].toLowerCase().contains(action.toLowerCase())) {
				methods.mouse.click(true);
			} else {
				methods.mouse.click(false);
				methods.menu.doAction(action);
			}
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	/**
	 * Returns the RSTile under the mouse.
	 *
	 * @return The <code>RSTile</code> under the mouse, or null
	 *         if the mouse is not over the viewport.
	 */
	public RSTile getTileUnderMouse() {
		Point p = methods.mouse.getLocation();
		if (!methods.calc.pointOnScreen(p)) {
			return null;
		}
		RSTile close = null;
		for (int x = 0; x < 104; x++) {
			for (int y = 0; y < 104; y++) {
				RSTile t = new RSTile(x + methods.client.getBaseX(), y + methods.client.getBaseY());
				Point s = methods.calc.tileToScreen(t);
				if (s.x != -1 && s.y != -1) {
					if (close == null) {
						close = t;
					}
					if (methods.calc.tileToScreen(close).distance(p) > methods.calc.tileToScreen(t).distance(p)) {
						close = t;
					}
				}
			}
		}
		return close;
	}
}
