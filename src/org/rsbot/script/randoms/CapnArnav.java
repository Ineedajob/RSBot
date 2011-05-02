package org.rsbot.script.randoms;

import org.rsbot.script.Random;
import org.rsbot.script.ScriptManifest;
import org.rsbot.script.util.Timer;
import org.rsbot.script.wrappers.RSComponent;
import org.rsbot.script.wrappers.RSInterface;
import org.rsbot.script.wrappers.RSNPC;
import org.rsbot.script.wrappers.RSObject;

import java.awt.*;

@ScriptManifest(authors = "endoskeleton", name = "CapnArnav", version = 1)
public class CapnArnav extends Random {

	private static final int[] ARNAV_CHEST = {42337, 42338};
	private static final int ARNAV_ID = 2308;
	private static final int EXIT_PORTAL = 11369;
	private static final int[][] INTERFACE_SOLVE_IDS = {{7, 14, 21}, // BOWL
			{5, 12, 19}, // RING
			{6, 13, 20}, // COIN
			{8, 15, 22} // BAR
	};
	private static final int[][] ARROWS = {{2, 3}, {9, 10}, {16, 17}};
	private static final int TALK_INTERFACE = 228;
	private static final int CHEST_INTERFACE_PARENT = 185;
	private static final int CHEST_INTERFACE_UNLOCK = 28;
	private static final int CHEST_INTERFACE_CENTER = 23;

	private static enum STATE {
		OPEN_CHEST, SOLVE, TALK, EXIT
	}

	private int index = -1;
	
	@Override
	public boolean activateCondition() {
		final RSNPC captain = npcs.getNearest(ARNAV_ID);

		if (captain != null) {
			sleep(random(1500, 1600));
			RSObject portal = objects.getNearest(EXIT_PORTAL);

			return portal != null;

		}

		return false;
	}

	@Override
	public void onFinish() {
		index = -1;
	}

	private STATE getState() {
		if (objects.getNearest(ARNAV_CHEST[1]) != null) {
			return STATE.EXIT;
		} else if (interfaces.canContinue()
				|| interfaces.get(TALK_INTERFACE) != null
				&& interfaces.get(TALK_INTERFACE).isValid()) {
			return STATE.TALK;
		} else if (interfaces.get(CHEST_INTERFACE_PARENT) == null
				|| !interfaces.get(CHEST_INTERFACE_PARENT).isValid()) {
			return STATE.OPEN_CHEST;
		} else {
			return STATE.SOLVE;
		}
	}

	@Override
	public int loop() {
		if (bank.isDepositOpen() || bank.isOpen()) {
			bank.close();
		}

		if (!activateCondition()) {
			return -1;
		}

		if (getMyPlayer().isMoving()) {
			return random(1000, 2000);
		}

		switch (getState()) {
			case EXIT:
				RSObject portal = objects.getNearest(EXIT_PORTAL);
				if (portal != null) {
					if (!portal.isOnScreen()) {
						camera.turnTo(portal);
					}
					if (portal.doAction("Enter")) {
						return random(3000,3500);
					}
				}
				break;

			case OPEN_CHEST:
				RSObject chest = objects.getNearest(ARNAV_CHEST);
				if (chest != null) {
					if (chest.doClick()) {
						return random(1000, 1300);
					}
				}
				break;

			case TALK:
				if (interfaces.canContinue()) {
					interfaces.clickContinue();
					return random(1500, 2000);
				}
				RSComponent okay = interfaces.getComponent(TALK_INTERFACE, 3);
				if (okay != null && okay.isValid()) {
					okay.doClick();
				}
				return random(1500, 2000);

			case SOLVE:
				RSInterface solver = interfaces.get(CHEST_INTERFACE_PARENT);
				if (solver != null && solver.isValid()) {

					String s = solver.getComponent(32).getText();
					if (s.contains("Bowl")) {
						index = 0;
					} else if (s.contains("Ring")) {
						index = 1;
					} else if (s.contains("Coin")) {
						index = 2;
					} else if (s.contains("Bar")) {
						index = 3;
					}

					if (solved()) {
						solver.getComponent(CHEST_INTERFACE_UNLOCK).doClick();
						return random(600, 900);
					}

					RSComponent container = solver
							.getComponent(CHEST_INTERFACE_CENTER);
					for (int i = 0; i < 3; i++) {
						int rand = random(0, 100);
						if (rand < 50) {
							rand = 0;
						} else if (rand >= 50) {
							rand = 1;
						}
						RSComponent target = solver
								.getComponent(INTERFACE_SOLVE_IDS[index][i]);
						RSComponent arrow = solver.getComponent(ARROWS[i][rand]);
						while (container.isValid()
								&& target.isValid()
								&& !container.getArea().contains(
								new Point(target.getCenter().x + 15, target
										.getCenter().y)) && arrow.isValid()
								&& new Timer(10000).isRunning()) {
							arrow.doClick();
							sleep(random(1000, 1200));
						}
					}

				}
		}
		return random(500, 800);
	}

	private boolean solved() {
		if (index == -1) {
			return false;
		}
		RSInterface solver = interfaces.get(CHEST_INTERFACE_PARENT);
		if (solver != null && solver.isValid()) {
			RSComponent container = solver.getComponent(CHEST_INTERFACE_CENTER);

			Point p1 = solver.getComponent(INTERFACE_SOLVE_IDS[index][0])
					.getCenter();
			p1.setLocation(p1.x + 15, p1.y);
			Point p2 = solver.getComponent(INTERFACE_SOLVE_IDS[index][1])
					.getCenter();
			p2.setLocation(p2.x + 15, p1.y);
			Point p3 = solver.getComponent(INTERFACE_SOLVE_IDS[index][2])
					.getCenter();
			p3.setLocation(p3.x + 15, p1.y);
			return (container.getArea().contains(p1)
					&& container.getArea().contains(p2) && container.getArea()
					.contains(p3));
		}
		return false;
	}

}