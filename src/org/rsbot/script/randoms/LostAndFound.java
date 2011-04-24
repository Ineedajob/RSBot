package org.rsbot.script.randoms;

import org.rsbot.script.Random;
import org.rsbot.script.ScriptManifest;
import org.rsbot.script.wrappers.RSObject;
import org.rsbot.script.wrappers.RSTile;

/**
 * Updated by Arbiter 9/22/10: Replaced tile clicking with model clicking. :)
 */
@ScriptManifest(authors = {"Garrett"}, name = "LostAndFound", version = 1.1)
public class LostAndFound extends Random {

	final int appendN = 8995;
	final int appendE = 8994;
	final int appendS = 8997;
	final int appendW = 8996;

	final int answerN[] = {32, 64, 135236, 67778, 135332, 34017, 202982, 101443, 101603, 236743, 33793, 67682, 135172,
			236743, 169093, 33889, 202982, 67714, 101539};
	final int answerE[] = {4, 6, 101474, 101473, 169124, 169123, 67648, 135301, 135298, 67651, 169121, 33827, 67652,
			236774, 101479, 33824, 202951};
	final int answerS[] = {4228, 32768, 68707, 167011, 38053, 230433, 164897, 131072, 168068, 65536, 35939, 103589,
			235718, 204007, 100418, 133186, 99361, 136357, 1057, 232547};
	final int answerW[] = {105571, 37921, 131204, 235751, 1024, 165029, 168101, 68674, 203974, 2048, 100451, 6144,
			39969, 69698, 32801, 136324};

	final int setting = 531;

	public boolean activateCondition() {
		return game.isLoggedIn() && objects.getNearest(appendN) != null;
	}

	public RSObject getFarthestObjectByID(final int... ids) {
		RSObject cur = null;
		double dist = -1;
		for (int x = 0; x < 104; x++) {
			for (int y = 0; y < 104; y++) {
				final RSObject[] objs = objects.getAllAt(new RSTile(x + game.getBaseX(), y + game.getBaseY()));
				if (objs.length > 0) {
					RSObject o = objs[0];
					boolean isObject = false;
					for (final int id : ids) {
						if (o.getID() == id) {
							isObject = true;
							break;
						}
					}
					if (isObject) {
						final double distTmp = calc.distanceBetween(getMyPlayer().getLocation(), o.getLocation());
						if (cur == null) {
							dist = distTmp;
							cur = o;
						} else if (distTmp > dist) {
							cur = o;
							dist = distTmp;
						}
					}
				}
			}
		}
		return cur;
	}

	private int getOddAppendage() {
		final int[] settings = this.settings.getSettingArray();
		try {
			for (final int element : answerN) {
				if (settings[setting] == element) {
					return appendN;
				}
			}
			for (final int element : answerE) {
				if (settings[setting] == element) {
					return appendE;
				}
			}
			for (final int element : answerS) {
				if (settings[setting] == element) {
					return appendS;
				}
			}
			for (final int element : answerW) {
				if (settings[setting] == element) {
					return appendW;
				}
			}
		} catch (final Exception ignored) {
		}
		return random(8994, 8998);
	}

	public int loop() {
		if (interfaces.canContinue()) {
			interfaces.clickContinue();
		}

		if (objects.getNearest(appendN) == null) {
			return -1;
		}

		final int appendage = getOddAppendage();

		try {
			final RSObject obj = getFarthestObjectByID(appendage);
			final RSTile tile = obj.getLocation();
			if (!calc.tileOnScreen(tile)) {
				walking.getPath(tile).traverse();
				sleep(random(700, 900));
				while (getMyPlayer().isMoving()) {
					sleep(100);
				}
			}
			if (obj.doAction("Operate")) {
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