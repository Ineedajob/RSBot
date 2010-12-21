import org.rsbot.event.events.MessageEvent;
import org.rsbot.event.listeners.MessageListener;
import org.rsbot.event.listeners.PaintListener;
import org.rsbot.script.Script;
import org.rsbot.script.ScriptManifest;
import org.rsbot.script.methods.Skills;
import org.rsbot.script.util.Timer;
import org.rsbot.script.wrappers.RSNPC;
import org.rsbot.script.wrappers.RSObject;
import org.rsbot.script.wrappers.RSPlayer;
import org.rsbot.script.wrappers.RSTile;

import java.awt.*;
import java.text.DecimalFormat;

@ScriptManifest(authors = "Arbiter", keywords = "Woodcutting", name = "ArbiChop Lite", version = 1.3, description = "Start at Draynor. Auto-detecting Regular Tree and Willow Chopper.")
public class ArbiChopLite extends Script implements PaintListener, MessageListener {

	private enum State {
		DROP, WALK_TO_TREE, WALK_TO_BANK, BANK, CHOP, SLEEP
	}

	private static final RSTile BANK_TILE = new RSTile(3092, 3244);
	private static final RSTile TREE_TILE = new RSTile(3087, 3234);

	private static final int[] WILLOWS = {5551, 5552, 5553};
	private static final int[] REGULAR = {1276, 1278};
	private static final int[] HATCHETS = {6739, 1359, 1357, 1361, 1351, 1349, 1355, 1353};

	private static final DecimalFormat XP_FORMAT = new DecimalFormat("###,###,###");

	public RSObject lastTree;

	private int scriptStartXP = 0;
	private int treesCut = 0;
	private long scriptStartTime = 0;
	private int random1 = random(25, 75);
	private int[] trees;

	private boolean power, inventoryContains;

	public int loop() {
		try {
			antiBan();
			mouse.setSpeed(random(6, 8));
			if (skills.getCurrentLevel(Skills.WOODCUTTING) >= 30) {
				trees = WILLOWS;
				power = false;
			} else {
				trees = REGULAR;
				power = true;
			}
			if (walking.getEnergy() > random(50, 100) && !walking.isRunEnabled()) {
				walking.setRun(true);
				return random(1000, 2000);
			}
			switch (getState()) {
				case WALK_TO_TREE:
					RSObject tree = objects.getNearest(trees);
					if (trees != WILLOWS && tree != null) {
						RSTile loc = walking.getClosestTileOnMap(tree.getLocation());
						if (walking.getDestination() != null && calc.distanceBetween(walking.getDestination(), loc) < random(2, 3))
							return random(50, 100);
						if (calc.distanceTo(loc) < random(2, 3))
							return random(50, 100);
						walking.walkTileMM(loc, random(1, 3), random(1, 3));
						return random(50, 100);
					}
					RSTile loc = walking.getClosestTileOnMap(TREE_TILE);
					if (walking.getDestination() != null && calc.distanceBetween(walking.getDestination(), loc) < random(2, 3))
						return random(50, 100);
					if (calc.distanceTo(loc) < random(2, 3))
						return random(50, 100);
					walking.walkTileMM(walking.getClosestTileOnMap(loc), random(1, 3), random(1, 3));
					return random(50, 100);
				case WALK_TO_BANK:
					loc = walking.getClosestTileOnMap(BANK_TILE);
					if (walking.getDestination() != null && calc.distanceBetween(walking.getDestination(), loc) < random(2, 3))
						return random(50, 100);
					if (calc.distanceTo(loc) < random(2, 3))
						return random(50, 100);
					walking.walkTileMM(walking.getClosestTileOnMap(loc), random(1, 3), random(1, 3));
					return random(50, 100);
				case CHOP:
					if (bank.isOpen()) {
						bank.close();
						return random(500, 1000);
					}
					RSObject t = objects.getNearest(trees);
					if (!power && t.doAction("Chop down Willow")) {
						sleep(random(1000, 2000));
						sleepForAnim(random(2000, 3000));
					} else if (power && t.doAction("Chop down Tree")) {
						sleep(random(1000, 2000));
						sleepForAnim(random(2000, 3000));
					}
					return random(50, 100);
				case BANK:
					if (bank.isOpen() && interfaces.get(762).getComponent(9).getAbsoluteY() > 50) {
						if (!inventory.containsOneOf(HATCHETS)) {
							if (bank.depositAll()) {
								for (int i = 0; i < 20; i++) {
									sleep(50, 100);
									if (inventory.getCount() == 0) {
										return random(50, 100);
									}
								}
							}
						} else {
							for (int j = 0; j < 20; j++) {
								if (bank.depositAllExcept(HATCHETS)) {
									for (int i = 0; i < 20; i++) {
										sleep(50, 100);
										if (inventory.getCount() == inventory.getCount(HATCHETS) || !bank.isOpen()) {
											return (random(50, 100));
										}
									}
								}
								sleep(random(100, 200));
							}
						}
					}
					RSNPC bankPerson = npcs.getNearest("Banker");
					if (bankPerson.doAction("Bank Banker")) {
						for (int i = 0; i < 50; i++) {
							while (getMyPlayer().isMoving())
								sleep(random(1, 50));
							if (bank.isOpen() && interfaces.get(762).getComponent(9).getAbsoluteY() > 50 && inventory.getCount() == 28)
								return random(50, 100);
							sleep(random(20, 30));
						}
					}
					return random(50, 100);
				case DROP:
					if (inventory.getCount(HATCHETS) == inventory.getCount()) {
						inventoryContains = false;
						return random(1, 10);
					}
					if (interfaces.get(94).getComponent(7).isValid() && interfaces.get(94).getComponent(7).getAbsoluteY() > 50) {
						interfaces.get(94).getComponent(3).doAction("Continue");
						return random(250, 500);
					}
					if (inventory.contains(15289)) {
						inventory.getItem(15289).doAction("Destroy");
						for (int i = 0; i < 100; i++) {
							if (interfaces.get(94).getComponent(7).isValid() && interfaces.get(94).getComponent(7).getAbsoluteY() > 50)
								return random(50, 100);
							sleep(10, 20);
						}
						return random(50, 100);
					}
					if (inventory.contains(15290)) {
						inventory.getItem(15290).doAction("Destroy");
						for (int i = 0; i < 100; i++) {
							if (interfaces.get(94).getComponent(7).isValid() && interfaces.get(94).getComponent(7).getAbsoluteY() > 50)
								return random(50, 100);
							sleep(10, 20);
						}
						return random(50, 100);
					}
					boolean leftToRight = true;
					if (random(0, 100) > random1)
						leftToRight = false;
					if (!leftToRight) {
						for (int c = 0; c < 4; c++) {
							for (int r = 0; r < 7; r++) {
								boolean found = false;
								for (int i = 0; i < HATCHETS.length && !found; i++) {
									found = HATCHETS[i] == inventory.getItems()[c + r * 4].getID();
								}
								if (!found) {
									inventory.dropItem(c, r);
								}
							}
						}
					} else {
						for (int r = 0; r < 7; r++) {
							for (int c = 0; c < 4; c++) {
								boolean found = false;
								for (int i = 0; i < HATCHETS.length && !found; i++) {
									found = HATCHETS[i] == inventory.getItems()[c + r * 4].getID();
								}
								if (!found) {
									inventory.dropItem(c, r);
								}
							}
						}
					}
					for (int i = 0; i < 100; i++) {
						if (inventory.getCount(HATCHETS) == inventory.getCount())
							return random(50, 100);
						sleep(10, 15);
					}
					return (random(50, 100));
			}
		} catch (Exception e) {
		}
		return random(50, 100);
	}

	private State getState() {
		try {
			if (inventoryContains) {
				return State.DROP;
			} else if (getMyPlayer().getAnimation() != -1 && lastTree != null && isTreeAlive() && inventory.getCount() < 28) {
				return State.SLEEP;
			} else if (inventory.getCount() < 28) {
				RSObject o = objects.getNearest(trees);
				if (o != null && o.getModel() != null && o.getModel().getPoint() != null && calc.pointOnScreen(o.getModel().getPoint())) {
					lastTree = objects.getNearest(trees);
					return State.CHOP;
				} else {
					return State.WALK_TO_TREE;
				}
			} else if (power) {
				inventoryContains = true;
				return State.DROP;
			} else {
				if (calc.tileOnScreen(npcs.getNearest("Banker").getLocation()))
					return State.BANK;
				else
					return State.WALK_TO_BANK;
			}
		} catch (Exception e) {
			return State.SLEEP;
		}
	}

	public boolean isTreeAlive() {
		int id = objects.getTopAt(lastTree.getLocation()).getID();
		for (int tree : trees) {
			if (id == tree)
				return true;
		}
		return false;
	}

	public int sleepForAnim(int timeout) {
		long start = System.currentTimeMillis();
		RSPlayer myPlayer = getMyPlayer();
		int anim = -1;

		while (System.currentTimeMillis() - start < timeout) {
			if ((anim = myPlayer.getAnimation()) != -1 || !isTreeAlive()) {
				return random(50, 100);
			}
			sleep(random(5, 15));
		}
		return anim;
	}


	public void messageReceived(final MessageEvent evt) {
		final String serverString = evt.getMessage();
		if (evt.getID() == MessageEvent.MESSAGE_ACTION && serverString.contains("get some"))
			treesCut++;
	}

	public void onRepaint(final Graphics g) {
		if (game.isLoggedIn()
				&& skills.getCurrentLevel(Skills.WOODCUTTING) > 1) {
			if (scriptStartTime == 0) {
				scriptStartTime = System.currentTimeMillis();
				scriptStartXP = skills
						.getCurrentExp(Skills.WOODCUTTING);
			}

			final Color BG = new Color(50, 50, 50, 150);
			final Color TEXT = new Color(200, 255, 0, 255);

			int x = 13;
			int y = 26;

			final int levelsGained = skills.getRealLevel(Skills.WOODCUTTING) - Skills.getLevelAt(scriptStartXP);
			g.setColor(BG);
			g.fill3DRect(x - 6, y, 211, 26, true);
			g.setColor(TEXT);
			g.drawString("ArbiCHOP Lite", x, y += 17);

			y += 20;
			g.setColor(BG);
			if (levelsGained == 0)
				g.fill3DRect(x - 6, y, 211, 66, true);
			else
				g.fill3DRect(x - 6, y, 211, 86, true);

			y -= 3;
			g.setColor(TEXT);
			g.drawString("Runtime: " + Timer.format(System.currentTimeMillis() - scriptStartTime), x, y += 20);
			g.drawString("Chopped: " + treesCut + " Logs", x, y += 20);

			long hourly = 0;
			if (System.currentTimeMillis() - scriptStartTime != 0)
				hourly = (((long) skills.getCurrentExp(Skills.WOODCUTTING) - (long) scriptStartXP) * 3600 * 1000 / (System.currentTimeMillis() - scriptStartTime));
			g.drawString("Gained: " + XP_FORMAT.format(skills.getCurrentExp(Skills.WOODCUTTING) - scriptStartXP) + " XP [ " + XP_FORMAT.format(hourly) + "/HR ]", x, y += 20);

			if (levelsGained == 1)
				g.drawString("1 Level Gained!", x, y += 20);
			else if (levelsGained > 1)
				g.drawString(levelsGained + " Levels Gained!", x, y += 20);
		}
	}

	private void antiBan() {
		int random = random(1, 10);
		switch (random) {
			case 1:
				if (random(1, 10) != 1)
					return;
				mouse.move(random(10, 750), random(10, 495));
				break;
			case 2:
				if (random(1, 20) != 1)
					return;
				int angle = camera.getAngle() + random(-90, 90);
				if (angle < 0) {
					angle = random(0, 10);
				}
				if (angle > 359) {
					angle = random(0, 10);
				}
				camera.setAngle(angle);
				break;
			case 3:
				if (random(1, 5) == 1)
					mouse.moveSlightly();
		}
	}

	public void onFinish() {
		log("For universal woodcutting and more features purchase ArbiCHOP!");
		log("www.ArbiBots.com");
	}

}