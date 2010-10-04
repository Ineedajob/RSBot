package org.rsbot.script.wrappers;

import org.rsbot.client.LDModel;
import org.rsbot.client.Model;
import org.rsbot.client.Node;
import org.rsbot.client.RSNPCNode;
import org.rsbot.script.methods.MethodContext;
import org.rsbot.script.methods.MethodProvider;

import java.awt.Point;

/**
 * Represents a character.
 */
public class RSCharacter extends MethodProvider {

    protected org.rsbot.client.RSCharacter c;

    public RSCharacter(final MethodContext ctx, final org.rsbot.client.RSCharacter c) {
    	super(ctx);
        this.c = c;
    }

    /**
     * Clicks a humanoid character (tall and skinny).
     *
     * @param action The option to be clicked (if available).
     * @return <tt>true</tt> if the option was found; otherwise <tt>false</tt>.
     */
    public boolean doAction(String action) {
        for (int i = 0; i < 20; i++) {
            if (c == null || !methods.calc.pointOnScreen(getScreenLocation()) || !methods.calc.tileOnScreen(getLocation()))
                return false;
            methods.mouse.move(new Point((int) Math.round(getScreenLocation().getX()) + random(-5, 5), (int) Math.round(getScreenLocation().getY()) + random(-5, 5)));
            if (methods.menu.getItems()[0].toLowerCase().contains(action.toLowerCase())) {
                methods.mouse.click(true);
                return true;
            } else {
                String[] menuItems = methods.menu.getItems();
                for (String item : menuItems) {
                    if (item.toLowerCase().contains(action.toLowerCase())) {
                        methods.mouse.click(false);
                        return methods.menu.doAction(action);
                    }
                }
            }
        }
        return false;
    }
    
    public RSModel getModel() {
    	if (c != null) {
    		Model model = c.getModel();
    		if (model != null) {
    			return new RSCharacterModel(methods, (LDModel) model, c);
    		}
    	}
		return null;
    }

    public int getAnimation() {
        return c.getAnimation();
    }

    public int getHeight() {
        return c.getHeight();
    }

    /**
     * @return The % of HP; 100 if not in combat.
     */
    public int getHPPercent() {
        return isInCombat() ? c.getHPRatio() * 100 / 255 : 100;
    }

    public RSCharacter getInteracting() {
        final int interact = c.getInteracting();
        if (interact == -1) {
            return null;
        }
        if (interact < 32768) {
            Node node = methods.nodes.lookup(methods.client.getRSNPCNC(), interact);
            if (node == null || !(node instanceof RSNPCNode)) {
                return null;
            }
            return new RSNPC(methods, ((RSNPCNode) node).getRSNPC());
        } else {
            int index = interact - 32768;
            if (index == methods.client.getSelfInteracting()) {
                index = 2047;
            }
            return new RSPlayer(methods, methods.client.getRSPlayerArray()[index]);
        }
    }

    public RSTile getLocation() {
        if (c == null) {
            return new RSTile(-1, -1);
        }
        final int x = methods.client.getBaseX() + (c.getX() >> 9);
        final int y = methods.client.getBaseY() + (c.getY() >> 9);
        return new RSTile(x, y);
    }

    public String getMessage() {
        return c.getMessage();
    }

    /**
     * Gets the minimap location, of the character.
	 * Note: This does work when it's walking!
     *
     * @return The location of the character on the minimap.
     */
    public Point getMinimapLocation() {
        final int cX = methods.client.getBaseX() + (c.getX() / 32 - 2) / 4;
        final int cY = methods.client.getBaseY() + (c.getY() / 32 - 2) / 4;
        return methods.calc.worldToMinimap(cX, cY);
    }

    public String getName() {
        return null; // should be overridden, obviously
    }

    public int getLevel() {
        return -1; // should be overridden too
    }

    public Point getScreenLocation() {
        return methods.calc.groundToScreen(c.getX(), c.getY(), -c.getHeight() / 2);
    }

    public boolean isInCombat() {
        return methods.game.isLoggedIn() && methods.client.getLoopCycle() < c.getLoopCycleStatus();
    }

    public boolean isInteractingWithLocalPlayer() {
        return c.getInteracting() - 32768 == methods.client.getSelfInteracting();
    }

    public boolean isMoving() {
        return c.isMoving() != 0;
    }

    public boolean isOnScreen() {
        return methods.calc.tileOnScreen(getLocation());
    }

    public boolean isValid() {
        return c != null;
    }

    @Override
    public int hashCode() {
        return System.identityHashCode(c);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof RSCharacter) {
            RSCharacter cha = (org.rsbot.script.wrappers.RSCharacter) obj;
            return cha.c == c;
        }
        return false;
    }

    @Override
    public String toString() {
        final RSCharacter inter = getInteracting();
        return "[anim=" + getAnimation() + ",msg=" + getMessage() + ",interact=" + (inter == null ? "null" : inter.isValid() ? inter.getMessage() : "Invalid") + "]";
    }
}
