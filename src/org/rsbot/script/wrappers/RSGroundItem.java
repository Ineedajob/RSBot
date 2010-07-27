package org.rsbot.script.wrappers;

import org.rsbot.script.methods.MethodContext;
import org.rsbot.script.methods.MethodProvider;

/**
 * Represents an item on a tile.
 * 
 * @author Jacmob
 */
public class RSGroundItem extends MethodProvider {
	
    private RSItem groundItem;
    private RSTile location;

    public RSGroundItem(final MethodContext ctx, final RSTile location, final RSItem groundItem) {
        super(ctx);
        this.location = location;
        this.groundItem = groundItem;
    }

    public boolean doAction(String action) {
        double d = random(0.45,0.55);
        return methods.tiles.doAction(getLocation(), d, d, 0, action);
    }

    public RSItem getItem() {
        return groundItem;
    }

    public RSTile getLocation() {
        return location;
    }
    
    public boolean isOnScreen() {
    	return methods.calc.tileOnScreen(location);
    }

}
