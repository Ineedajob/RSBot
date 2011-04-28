package org.rsbot.script.wrappers;

import org.rsbot.client.RSGroundEntity;
import org.rsbot.client.RSGroundObject;
import org.rsbot.script.methods.MethodContext;
import org.rsbot.script.methods.MethodProvider;

/**
 * Represents an item on a tile.
 *
 * @author Jacmob
 */
public class RSGroundItem extends MethodProvider {

	private final RSItem groundItem;
	private final RSTile location;

	public RSGroundItem(final MethodContext ctx, final RSTile location, final RSItem groundItem) {
		super(ctx);
		this.location = location;
		this.groundItem = groundItem;
	}

	/**
	 * Gets the top model on the tile of this ground item.
	 *
	 * @return The top model on the tile of this ground item.
	 */
	public RSModel getModel() {
		int x = location.getX() - methods.game.getBaseX();
		int y = location.getY() - methods.game.getBaseY();
		int plane = methods.client.getPlane();
		org.rsbot.client.RSGround rsGround = methods.client.getRSGroundArray()[plane][x][y];

		if (rsGround != null) {
			RSGroundEntity obj = rsGround.getGroundObject();
			if (obj != null) {
				org.rsbot.client.Model model = ((RSGroundObject) rsGround.getGroundObject()).getModel();
				if (model != null) {
					return new RSAnimableModel(methods, model, obj);
				}
			}
		}
		return null;
	}

	/**
	 * Performs the given action on this RSGroundItem.
	 *
	 * @param action The menu action to click.
	 * @return <tt>true</tt> if the action was clicked; otherwise <tt>false</tt>.
	 */
	public boolean doAction(final String action) {
		return doAction(action, null);
	}

	/**
	 * Performs the given action on this RSGroundItem.
	 *
	 * @param action The menu action to click.
	 * @param option The option of the menu action to click.
	 * @return <tt>true</tt> if the action was clicked; otherwise <tt>false</tt>.
	 */
	public boolean doAction(final String action, final String option) {
		RSModel model = getModel();
		if (model != null) {
			return model.doAction(action, option);
		}
		return methods.tiles.doAction(getLocation(), random(0.45, 0.55), random(0.45, 0.55), 0,
				action, option);
	}

	public RSItem getItem() {
		return groundItem;
	}

	public RSTile getLocation() {
		return location;
	}

	public boolean isOnScreen() {
		RSModel model = getModel();
		if (model == null) {
			return methods.calc.tileOnScreen(location);
		} else {
			return methods.calc.pointOnScreen(model.getPoint());
		}
	}

}
