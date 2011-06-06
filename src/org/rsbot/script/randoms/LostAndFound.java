package org.rsbot.script.randoms;

import org.rsbot.script.Random;
import org.rsbot.script.ScriptManifest;
import org.rsbot.script.util.Filter;
import org.rsbot.script.wrappers.RSObject;
import org.rsbot.script.wrappers.RSTile;

/**
* Updated by Arbiter 9/22/10: Replaced tile clicking with model clicking. :)
* Updated by b0xb0x 6/5/11: Random updated to work; fake analyzing.
*/
@ScriptManifest(authors = {"Garrett"}, name = "LostAndFound", version = 1.2)
public class LostAndFound extends Random {

	final int append = 33254;
	
	final int answerN[] = {32, 64, 135236, 67778, 135332, 34017, 202982, 101443, 101603, 236743, 33793, 67682, 135172,
			236743, 169093, 33889, 202982, 67714, 101539};
	final int answerE[] = {4, 6, 101474, 101473, 169124, 169123, 67648, 135301, 135298, 67651, 169121, 33827, 67652,
			236774, 101479, 33824, 202951};
	final int answerS[] = {4228, 32768, 68707, 167011, 38053, 230433, 164897, 131072, 168068, 65536, 35939, 103589,
			235718, 204007, 100418, 133186, 99361, 136357, 1057, 232547};
	final int answerW[] = {105571, 37921, 131204, 235751, 1024, 165029, 168101, 68674, 203974, 2048, 100451, 6144,
			39969, 69698, 32801, 136324};
	
	final int setting = 531;

	boolean analyzed = false;
	
	@Override
	public boolean activateCondition() {
		return game.isLoggedIn() && objects.getNearest(append) != null;
	}

	private void fakeAnalyze() {
		sleep(random(1000, 1750));
		final int pitch = camera.getPitch();
		final int angle = camera.getAngle();
		final int loop = random(2, 4);
		for (int i = 0; i < loop; i++) {
			camera.setPitch(random(20, 80));
			camera.setAngle(random(0, 359));
			sleep(random(1000, 2500));
		}
		camera.setPitch(pitch);
		camera.setAngle(angle);
	}

	// N=0, E=2, S=4, W=6
	private RSObject getFurthestAtDir(final int direction, final int range, final int...ids) {
		if (direction < 0 || direction > 6) {
			return null;
		}
		RSObject cur = null;
		int xDist = -1;
		int yDist = -1;
		Top: for (RSObject o : getLoadedInRange(range)) {
			if (o == null) {
				continue;
			}
			for (int id : ids) {
				if (o.getID() != id)
					continue Top;
			}
			RSTile loc = o.getLocation();
			RSTile me = players.getMyPlayer().getLocation();
			if (direction == 0 || direction == 4) {
				final int yTemp = Math.abs(me.getY() - loc.getY());
				if (cur == null) {
					yDist = yTemp;
					cur = o;
				} else if (direction == 0 && yTemp < yDist) {
					cur = o;
					yDist = yTemp;
				} else if (direction == 4 && yTemp > yDist) {
					cur = o;
					yDist = yTemp;
				}
			} else {
				final int xTemp = Math.abs(me.getX() - loc.getX());
				if (cur == null) {
					xDist = xTemp;
					cur = o;
				} else if (direction == 2 && xTemp < xDist) {
					cur = o;
					xDist = xTemp;
				} else if (direction == 6 && xTemp > xDist) {
					cur = o;
					xDist = xTemp;
				}
			}
		}
		return cur;
	}
	
	private RSObject[] getLoadedInRange(final int range) {
		return objects.getAll(new Filter<RSObject>() {
			public boolean accept(RSObject o) {
				if (o != null && calc.distanceTo(o) <= range) {
					return true;
				}
				return false;
			}
		});
	}
	
	private int getAppendageDirection() {
		final int[] settings = this.settings.getSettingArray();
		try {
			for (final int element : answerN) {
				if (settings[setting] == element) {
					return 0;
				}
			}
			for (final int element : answerE) {
				if (settings[setting] == element) {
					return 2;
				}
			}
			for (final int element : answerS) {
				if (settings[setting] == element) {
					return 4;
				}
			}
			for (final int element : answerW) {
				if (settings[setting] == element) {
					return 6;
				}
			}
		} catch (final Exception ignored) {
		}
		return random(0, 3) * 2;
	}
	
	@Override
	public int loop() {
		if (!analyzed) {
			fakeAnalyze();
			analyzed = true;
		}
		if (interfaces.canContinue()) {
			interfaces.clickContinue();
		}
		if (objects.getNearest(append) == null) {
			return -1;
		}
		final int direction = getAppendageDirection();
		try {
			final RSObject obj = getFurthestAtDir(direction, 8, append);
			final RSTile tile = obj.getLocation();
			if (!calc.tileOnScreen(tile)) {
				walking.getPath(tile).traverse();
				sleep(random(700, 900));
				while (getMyPlayer().isMoving()) {
					sleep(100);
				}
			}
			if (obj.interact("Operate")) {
				sleep(random(1000, 1500));
				while (getMyPlayer().isMoving()) {
					sleep(100);
				}
			}
		} catch (final Exception ignored) {
		}
		return random(1000, 2000);
	}

}