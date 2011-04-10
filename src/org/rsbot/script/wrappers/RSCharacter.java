package org.rsbot.script.wrappers;

import org.rsbot.client.Model;
import org.rsbot.client.Node;
import org.rsbot.client.RSNPCNode;
import org.rsbot.script.methods.MethodContext;
import org.rsbot.script.methods.MethodProvider;

import java.awt.*;

public abstract class RSCharacter extends MethodProvider {

	public RSCharacter(MethodContext ctx) {
		super(ctx);
	}

	/**
	 * Retrieves a reference to the client accessor. For internal use. The
	 * reference should be stored in a SoftReference by subclasses to allow for
	 * garbage collection when appropriate.
	 *
	 * @return The client accessor.
	 */
	protected abstract org.rsbot.client.RSCharacter getAccessor();

	/**
	 * Clicks a humanoid character (tall and skinny).
	 *
	 * @param action The option to be clicked (if available).
	 * @return <tt>true</tt> if the option was found; otherwise <tt>false</tt>.
	 */
	public boolean doAction(final String action) {
		RSModel model = this.getModel();
		return model != null && this.isValid() && this.getModel().doAction(action);
	}

	public RSModel getModel() {
		org.rsbot.client.RSCharacter c = getAccessor();
		if (c != null) {
			Model model = c.getModel();
			if (model != null) {
				return new RSCharacterModel(methods, model, c);
			}
		}
		return null;
	}

	public int getAnimation() {
		return getAccessor().getAnimation();
	}

	public int getGraphic() {
		return getAccessor().getGraphicsData()[0].getID();
	}

	public int getHeight() {
		return getAccessor().getHeight();
	}

	/**
	 * @return The % of HP
	 */
	public int getHPPercent() {
		return isInCombat() ? getAccessor().getHPRatio() * 100 / 255 : 100;
	}

	public RSCharacter getInteracting() {
		final int interact = getAccessor().getInteracting();
		if (interact == -1) {
			return null;
		}
		if (interact < 32768) {
			Node node = methods.nodes.lookup(methods.client.getRSNPCNC(),
			                                 interact);
			if (node == null || !(node instanceof RSNPCNode)) {
				return null;
			}
			return new RSNPC(methods, ((RSNPCNode) node).getRSNPC());
		} else {
			int index = interact - 32768;
			if (index == methods.client.getSelfInteracting()) {
				index = 2047;
			}
			return new RSPlayer(methods,
			                    methods.client.getRSPlayerArray()[index]);
		}
	}

	public RSTile getLocation() {
		org.rsbot.client.RSCharacter c = getAccessor();
		if (c == null) {
			return new RSTile(-1, -1);
		}
		int x = methods.client.getBaseX() + (c.getX() >> 9);
		int y = methods.client.getBaseY() + (c.getY() >> 9);
		return new RSTile(x, y);
	}

	public String getMessage() {
		return getAccessor().getMessage();
	}

	/**
	 * Gets the minimap location, of the character. Note: This does work when
	 * it's walking!
	 *
	 * @return The location of the character on the minimap.
	 */
	public Point getMinimapLocation() {
		org.rsbot.client.RSCharacter c = getAccessor();
		int cX = methods.client.getBaseX() + (c.getX() / 32 - 2) / 4;
		int cY = methods.client.getBaseY() + (c.getY() / 32 - 2) / 4;
		return methods.calc.worldToMinimap(cX, cY);
	}

	public String getName() {
		return null; // should be overridden, obviously
	}

	public int getLevel() {
		return -1; // should be overridden too
	}

	public int getOrientation() {
		return (int) (270 - (getAccessor().getOrientation() & 0x3fff) / 45.51) % 360;
	}

	public Point getScreenLocation() {
		org.rsbot.client.RSCharacter c = getAccessor();
		RSModel model = getModel();
		if (model == null) {
			return methods.calc.groundToScreen(c.getX(), c.getY(),
			                                   c.getHeight() / 2);
		} else {
			return model.getPoint();
		}
	}

	/**
	 * Hovers this Player/NPC
	 */
	public void hover() {
		this.getModel().hover();
	}

	public boolean isInCombat() {
		return methods.game.isLoggedIn()
				&& methods.client.getLoopCycle() < getAccessor()
				.getLoopCycleStatus();
	}

	public boolean isInteractingWithLocalPlayer() {
		return getAccessor().getInteracting() - 32768 == methods.client
				.getSelfInteracting();
	}

	public boolean isMoving() {
		return getAccessor().isMoving() != 0;
	}

	public boolean isOnScreen() {
		RSModel model = getModel();
		if (model == null) {
			return methods.calc.tileOnScreen(getLocation());
		} else {
			return methods.calc.pointOnScreen(model.getPoint());
		}
	}

	public boolean isValid() {
		return getAccessor() != null;
	}

	@Override
	public int hashCode() {
		return System.identityHashCode(getAccessor());
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof RSCharacter) {
			RSCharacter cha = (org.rsbot.script.wrappers.RSCharacter) obj;
			return cha.getAccessor() == getAccessor();
		}
		return false;
	}

	@Override
	public String toString() {
		final RSCharacter inter = getInteracting();
		return "[anim="
				+ getAnimation()
				+ ",msg="
				+ getMessage()
				+ ",interact="
				+ (inter == null ? "null" : inter.isValid() ? inter
				.getMessage() : "Invalid") + "]";
	}

}