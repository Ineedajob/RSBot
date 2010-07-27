package org.rsbot.script.wrappers;

import java.awt.*;

import org.rsbot.script.methods.MethodContext;

/**
 * Represents a player.
 */
public class RSPlayer extends RSCharacter {

    private org.rsbot.accessors.RSPlayer p;

    public RSPlayer(final MethodContext ctx, final org.rsbot.accessors.RSPlayer p) {
        super(ctx, p);
        this.p = p;
    }

    public int getCombatLevel() {
        return p.getLevel();
    }

    @Override
    public String getName() {
        return p.getName();
    }

    public int getTeam() {
        return p.getTeam();
    }

    public boolean isIdle() {
        return !isMoving() && (getAnimation() == -1) && !isInCombat();
    }

    public boolean doAction(String action) {
        try {
            Point screenLoc;
            for (int i = 0; i < 20; i++) {
                screenLoc = getScreenLocation();
                if (!isValid() || !methods.calc.pointOnScreen(screenLoc)) {
                    return false;
                }
                if (methods.mouse.getClientLocation().equals(screenLoc)) {
                    break;
                }
                methods.mouse.move(screenLoc);
            }
            screenLoc = getScreenLocation();
            if (!methods.mouse.getClientLocation().equals(screenLoc))
                return false;
            String[] items = methods.menu.getItems();
            if (items.length <= 1)
                return false;
            if (items[0].toLowerCase().contains(action.toLowerCase())) {
                methods.mouse.click(screenLoc, true);
                return true;
            } else {
                methods.mouse.click(screenLoc, false);
                return methods.menu.doAction(action);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public String toString() {
        return "Player[" + getName() + "]" + super.toString();
    }
}