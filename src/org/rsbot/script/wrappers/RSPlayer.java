package org.rsbot.script.wrappers;

import org.rsbot.client.RSPlayerComposite;
import org.rsbot.script.methods.MethodContext;

import java.awt.*;
import java.lang.ref.SoftReference;

/**
 * Represents a player.
 */
public class RSPlayer extends RSCharacter {

	private final SoftReference<org.rsbot.client.RSPlayer> p;

	public RSPlayer(final MethodContext ctx, final org.rsbot.client.RSPlayer p) {
		super(ctx);
		this.p = new SoftReference<org.rsbot.client.RSPlayer>(p);
	}

	protected org.rsbot.client.RSCharacter getAccessor() {
		return p.get();
	}

	public int getCombatLevel() {
		return p.get().getLevel();
	}

	@Override
	public String getName() {
		return p.get().getName();
	}

	public int getTeam() {
		return p.get().getTeam();
	}

	public int getNPCID() {
		RSPlayerComposite comp = p.get().getComposite();
		if (comp != null) {
			return comp.getNPCID();
		}
		return -1;
	}

	public boolean isIdle() {
		return !isMoving() && (getAnimation() == -1) && !isInCombat();
	}

	@Override
	public boolean doAction(final String action) {
		return doAction(action, null);
	}

	@Override
	public boolean doAction(final String action, final String option) {
		final RSModel model = getModel();
		if (model != null && isValid()) {
			return model.doAction(action, option);
		}
		try {
			Point screenLoc;
			for (int i = 0; i < 20; i++) {
				screenLoc = getScreenLocation();
				if (!isValid() || !methods.calc.pointOnScreen(screenLoc)) {
					return false;
				}
				if (methods.mouse.getLocation().equals(screenLoc)) {
					break;
				}
				methods.mouse.move(screenLoc);
			}
			String[] items = methods.menu.getItems();
			if (items.length <= 1) {
				return false;
			}
			if (items[0].toLowerCase().contains(action.toLowerCase())) {
				methods.mouse.click(true);
				return true;
			} else {
				methods.mouse.click(false);
				return methods.menu.doAction(action, option);
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