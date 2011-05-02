package org.rsbot.script.randoms;

import org.rsbot.script.Random;
import org.rsbot.script.ScriptManifest;
import org.rsbot.script.util.Filter;
import org.rsbot.script.wrappers.RSComponent;
import org.rsbot.script.wrappers.RSNPC;

/**
 * Updated by Iscream Feb 22,10 Updated by Parameter Jan 1, 11
 */
@ScriptManifest(authors = {"Nightmares18", "joku.rules", "Taha", "Fred"}, name = "FrogCave", version = 2.3)
public class FrogCave extends Random {

	private RSNPC frog;
	private boolean talkedToHerald, talkedToFrog;
	private int tries;

	@Override
	public boolean activateCondition() {
		if (!game.isLoggedIn()) {
			return false;
		} else if ((npcs.getNearest("Frog Herald") != null)
				&& (objects.getNearest(5917) != null)) {
			sleep(random(2000, 3000));
			return (npcs.getNearest("Frog Herald") != null)
					&& (objects.getNearest(5917) != null);
		}
		return false;
	}

	private RSNPC findFrog() {
		return npcs.getNearest(new Filter<RSNPC>() {
			@Override
			public boolean accept(RSNPC npc) {
				return !npc.isMoving() && npc.getHeight() == -278;
			}
		});
	}

	private boolean canContinue() {
		return interfaces.canContinue()
				|| interfaces.getComponent(65, 6).isValid();
	}
	
	public void onFinish() {
		talkedToHerald = false;
		frog = null;
		tries = 0;
	}

	@Override
	public int loop() {
		try {
			if (!activateCondition()) {
				return -1;
			}
			if (canContinue()) {
				// log("can continue...");
				if (!talkedToHerald) {
					final RSComponent heraldTalkComp = interfaces.getComponent(
							242, 4);
					talkedToHerald = heraldTalkComp.isValid()
							&& (heraldTalkComp.containsText("crown") || heraldTalkComp
							.containsText("is still waiting"));
				}
				if (!interfaces.clickContinue()) {
					interfaces.getComponent(65, 6).doClick();
				}
				return random(600, 800);
			}
			if (getMyPlayer().isMoving()) {
				return random(600, 800);
			}
			if (!talkedToHerald) {
				final RSNPC herald = npcs.getNearest("Frog Herald");
				if (calc.distanceTo(herald) < 5) {
					if (!calc.tileOnScreen(herald.getLocation())) {
						camera.turnTo(herald);
					}
					herald.doAction("Talk-to");
					return random(500, 1000);
				} else {
					walking.walkTileMM(herald.getLocation());
					return random(500, 700);
				}
			}
			if (frog == null) {
				frog = findFrog();
				if (frog != null) {
					log("Princess found! ID: " + frog.getID());
				}
			}
			if (frog != null && frog.getLocation() != null
					&& (!talkedToFrog || !canContinue())) {
				if (calc.distanceTo(frog) < 5) {
					if (!calc.tileOnScreen(frog.getLocation())) {
						camera.turnTo(frog);
					}
					if (frog.doAction("Talk-to Frog")) {
						sleep(750, 1250);
						talkedToFrog = canContinue();
					}
					return random(900, 1000);
				} else {
					walking.walkTileMM(frog.getLocation());
					return random(500, 700);
				}
			} else {
				tries++;
				if (tries > 200) {
					// log("tries > 200");
					tries = 0;
					talkedToHerald = false;
				}
				return random(200, 400);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return random(200, 400);
	}
}