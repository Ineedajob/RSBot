package org.rsbot.script.randoms;

import org.rsbot.script.Random;
import org.rsbot.script.ScriptManifest;
import org.rsbot.script.wrappers.RSNPC;
import org.rsbot.script.wrappers.RSObject;
import org.rsbot.script.wrappers.RSTile;

@ScriptManifest(authors = {"Taha"}, name = "FirstTimeDeath", version = 1.1)
public class FirstTimeDeath extends Random {
	private int step;
	private boolean exit;
	private RSNPC reaper;

	@Override
	public boolean activateCondition() {
		return ((reaper = npcs.getNearest(8869)) != null)
				|| ((reaper = npcs.getNearest(8870)) != null);
	}

	@Override
	public int loop() {
		if (!activateCondition()) {
			return -1;
		}
		camera.setPitch(true);
		if (interfaces.canContinue() && !exit) {
			if (interfaces.getComponent(241, 4).getText().contains("Yes?")) {
				step++;
				exit = true;
				return random(200, 400);
			} else if (interfaces.getComponent(242, 5).getText()
					.contains("Enjoy!")) {
				step++;
				exit = true;
			}
			interfaces.clickContinue();
			return random(200, 400);
		}
		switch (step) {
			case 0:
				RSObject reaperChair = objects.getNearest(45802);
				reaperChair.doAction("Talk-to");
				sleep(random(1000, 1200));
				if (!interfaces.canContinue()) {
					walking.walkTileOnScreen(new RSTile(
							reaper.getLocation().getX() + 2, reaper.getLocation()
									.getY() + 1));
					camera.turnTo(reaperChair);
				}
				break;

			case 1:
				int portalID = 45803;
				RSObject portal = objects.getNearest(portalID);
				RSTile loc = getMyPlayer().getLocation();
				portal.doAction("Enter");
				sleep(random(1000, 1200));
				if (calc.distanceTo(loc) < 10) {
					camera.turnTo(portal);
					if (!calc.tileOnScreen(portal.getLocation())) {
						walking.walkTileOnScreen(portal.getLocation());
					}
				}
				break;
		}
		return random(200, 400);
	}

	@Override
	public void onFinish() {
		step = -1;
		exit = false;
		reaper = null;
	}

}
