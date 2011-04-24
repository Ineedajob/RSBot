package org.rsbot.script.randoms;

import org.rsbot.script.Random;
import org.rsbot.script.ScriptManifest;
import org.rsbot.script.wrappers.RSComponent;
import org.rsbot.script.wrappers.RSModel;
import org.rsbot.script.wrappers.RSObject;

import java.awt.*;


@ScriptManifest(authors = {"Iscream", "Aelin", "LM3", "IceCandle", "Taha"}, name = "Pinball", version = 2.7)
public class Pinball extends Random {

	private static final int[] OBJ_PILLARS = {15000, 15002, 15004, 15006, 15008};

	private static final int[] OBJ_ACTIVATE = {15000, 15002, 15004, 15006, 15007, 15008};

	private static final int INTERFACE_PINBALL = 263;

	public boolean activateCondition() {
		return game.isLoggedIn() && objects.getNearest(OBJ_ACTIVATE) != null;
	}

	private int getScore() {
		RSComponent score = interfaces.get(INTERFACE_PINBALL).getComponent(1);
		try {
			return Integer.parseInt(score.getText().split(" ")[1]);
		} catch (java.lang.ArrayIndexOutOfBoundsException t) {
			return 10;
		}
	}

	public int loop() {
		if (!activateCondition()) {
			return -1;
		}
		if (getMyPlayer().isMoving() || getMyPlayer().getAnimation() != -1) {
			return random(300, 500);
		}
		if (getScore() >= 10) {
			int OBJ_EXIT = 15010;
			RSObject exit = objects.getNearest(OBJ_EXIT);
			if (exit != null) {
				if (calc.tileOnScreen(exit.getLocation()) && exit.doAction("Exit")) {
					sleep(random(2000, 2200));
					exit.doAction("Exit");
					return random(2000, 2100);
				} else {
					camera.setCompass('s');
					walking.walkTileOnScreen(exit.getLocation());
					return random(1400, 1500);
				}

			}
		}
		RSObject pillar = objects.getNearest(OBJ_PILLARS);
		if (pillar != null) {
			if (calc.distanceTo(pillar) > 2 && !pillar.isOnScreen()) {
				walking.walkTileOnScreen(pillar.getLocation());
				return random(500, 600);
			}
			if (pillar != null) {
				doClick(pillar);
			}
			int before = getScore();
			for (int i = 0; i < 50; i++) {
				if (getScore() > before) {
					return random(50, 100);
				}
				sleep(100, 200);
			}
		}
		return random(50, 100);
	}

	private void doClick(RSObject pillar) {
		RSModel model = pillar.getModel();
		if (model != null) {
			Point central = model.getCentralPoint();
			mouse.click(central.x, central.y, 4, 4, true);
			return;
		} else {
			Point p = calc.tileToScreen(pillar.getLocation());
			if (calc.pointOnScreen(p)) {
				mouse.click(p.x, p.y, 4, 20, true);
			}
			return;
		}
	}


}