import java.awt.Color;
import java.awt.Graphics;

import org.rsbot.script.*;
import org.rsbot.script.methods.Skills;
import org.rsbot.script.wrappers.*;
import org.rsbot.event.listeners.PaintListener;
import org.rsbot.event.listeners.ServerMessageListener;
import org.rsbot.event.events.ServerMessageEvent;

@ScriptManifest(authors = {"Arbiter"}, keywords = "Woodcutting", name = "ArbiChop Draynor", version = 1.00, description = "Draynor willow chopper.")
public class ArbiChopLite extends Script implements PaintListener, ServerMessageListener {

	public static RSTile BANK_TILE = new RSTile(3092, 3244);
	public static RSTile TREE_TILE = new RSTile(3087, 3234);

	public static int[] WILLOWS = {5551, 5552, 5553};
	public static int[] HATCHETS = {6739, 1359, 1357, 1361, 1351, 1349, 1355, 1353};

	public RSObject lastTree;

	private int scriptStartXP = 0;
	private int treesCut = 0;
	private long scriptStartTime = 0;

	public boolean onStart() {
		return true;
	}

	public int loop() {
		mouse.setSpeed(random(5, 8));
		switch (getState()) {
			case walkToTree:
				if (walking.getDestination() != null && calc.distanceBetween(walking.getDestination(), TREE_TILE) < random(2, 3))
					return random(50, 100);
				if (calc.distanceTo(TREE_TILE) < random(2, 3))
					return random(50, 100);
				walking.walkTileMM(walking.getClosestTileOnMap(TREE_TILE), random(1, 3), random(1, 3));
				return random(50, 100);

			case walkToBank:
				if (walking.getDestination() != null && calc.distanceBetween(walking.getDestination(), BANK_TILE) < random(2, 3))
					return random(50, 100);
				if (calc.distanceTo(BANK_TILE) < random(2, 3))
					return random(50, 100);
				walking.walkTileMM(walking.getClosestTileOnMap(BANK_TILE), random(1, 3), random(1, 3));
				return random(50, 100);

			case chop:
				RSObject t = objects.getNearest(WILLOWS);
				if (t.doAction("Chop down Willow")) {
					sleep(random(1000, 2000));
					sleepForAnim(random(2000, 3000));
				}

			case bank:
				if (bank.isOpen() && interfaces.get(762).getComponent(9).getAbsoluteY() > 50) {
					if (!inventory.containsOneOf(HATCHETS))
						if (bank.depositAll())
							for (int i = 0; i < 20; i++) {
								sleep(50, 100);
								if (inventory.getCount() == inventory.getCount(HATCHETS))
									break;
							}
						else {
							for (int j = 0; j < 20; j++) {
								if (bank.depositAllExcept(HATCHETS))
									for (int i = 0; i < 20; i++) {
										sleep(50, 100);
										if (inventory.getCount() == inventory.getCount(HATCHETS) || !bank.isOpen())
											return (random(50, 100));
									}
								sleep(random(100, 200));
							}
						}
					return (random(50, 100));
				}
				RSNPC bankPerson = npcs.getNearest("Banker");
				if (bankPerson.doAction("Bank Banker")) {
					for (int i = 0; i < 100; i++) {
						if (bank.isOpen() && interfaces.get(762).getComponent(9).getAbsoluteY() > 50 && inventory.getCount() == 28)
							break;
						sleep(random(20, 30));
					}
					return 100;
				}
				return 100;
		}
		return 100;
	}

	private enum State {
		walkToTree, walkToBank, bank, chop, sleep
	}

	private State getState() {
		try {
			if (getMyPlayer().getAnimation() != -1 && lastTree != null && isTreeAlive() && inventory.getCount() < 28) {
				return State.sleep;
			} else if (inventory.getCount() < 28) {
				RSObject o = objects.getNearest(WILLOWS);
				if (o != null && o.getModel() != null && o.getModel().getPoint() != null && calc.pointOnScreen(o.getModel().getPoint())) {
					lastTree = objects.getNearest(WILLOWS);
					return State.chop;
				} else {
					return State.walkToTree;
				}
			} else {
				if (calc.tileOnScreen(npcs.getNearest("Banker").getLocation()))
					return State.bank;
				else
					return State.walkToBank;
			}
		} catch (Exception e) {
			return State.sleep;
		}
	}

	public boolean isTreeAlive() {
		int id = objects.getTopAt(lastTree.getLocation()).getID();
		for (int willow : WILLOWS) {
			if (id == willow)
				return true;
		}
		return false;
	}

	public int sleepForAnim(int timeout) {
		long start = System.currentTimeMillis();
		RSPlayer myPlayer = getMyPlayer();
		int anim = -1;

		while (System.currentTimeMillis() - start < timeout) {
			if ((anim = myPlayer.getAnimation()) != -1) {
				break;
			}
			sleep(random(5, 15));
		}
		return anim;
	}


	public void serverMessageRecieved(final ServerMessageEvent arg0) {
		final String serverString = arg0.getMessage();
		if (serverString.contains("get some"))
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

			final int x = 13;
			int y = 26;

			final int levelsGained = skills.getRealLevel(Skills.WOODCUTTING) - Skills.getLevelAt(scriptStartXP);
			g.setColor(BG);
			g.fill3DRect(x - 6, y, 211, 26, true);
			g.setColor(TEXT);
			g.drawString("ArbiCHOP Lite", x, y += 17);

			y += 20;
			g.setColor(BG);
			g.fill3DRect(x - 6, y, 211, 66, true);

			y -= 3;
			g.setColor(TEXT);
			g.drawString("Runtime: "
					+ getFormattedTime(System.currentTimeMillis()
					- scriptStartTime), x, y += 20);
			g.drawString("Chopped: " + treesCut + " Willows", x, y += 20);

			if (levelsGained < 0) {
				scriptStartXP = skills
						.getCurrentExp(Skills.WOODCUTTING);
			} else if (levelsGained == 1) {
				g
						.drawString(
								"Gained: "
										+ (skills
										.getCurrentExp(Skills.WOODCUTTING) - scriptStartXP)
										+ " XP (" + levelsGained + " lvl)", x,
								y += 20);
			} else {
				g
						.drawString(
								"Gained: "
										+ (skills
										.getCurrentExp(Skills.WOODCUTTING) - scriptStartXP)
										+ " XP (" + levelsGained + " lvls)", x,
								y += 20);
			}
		}
	}

	private String getFormattedTime(final long timeMillis) {
		long millis = timeMillis;
		final long seconds2 = millis / 1000;
		final long hours = millis / (1000 * 60 * 60);
		millis -= hours * 1000 * 60 * 60;
		final long minutes = millis / (1000 * 60);
		millis -= minutes * 1000 * 60;
		final long seconds = millis / 1000;
		String hoursString = "";
		String minutesString = "";
		String secondsString = seconds + "";
		String type = "seconds";

		if (minutes > 0) {
			minutesString = minutes + ":";
			type = "minutes";
		} else if (hours > 0 && seconds2 > 0) {
			minutesString = "0:";
		}
		if (hours > 0) {
			hoursString = hours + ":";
			type = "hours";
		}
		if (minutes < 10 && !type.equals("seconds")) {
			minutesString = "0" + minutesString;
		}
		if (hours < 10 && type.equals("hours")) {
			hoursString = "0" + hoursString;
		}
		if (seconds < 10 && !type.equals("seconds")) {
			secondsString = "0" + secondsString;
		}

		return hoursString + minutesString + secondsString + " " + type;
	}

	public void onFinish() {
		log("For universal woodcutting and more features purchase ArbiCHOP!");
	}

}