package org.rsbot.script.randoms;

import org.rsbot.script.Random;
import org.rsbot.script.ScriptManifest;
import org.rsbot.script.methods.Bank;
import org.rsbot.script.wrappers.*;

import java.awt.*;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * <p>
 * This short-sighted gravedigger has managed to put five coffins in the wrong
 * graves. <br />
 * If he'd looked more closely at the headstones, he might have known where each
 * one was supposed to go! <br />
 * Help him by matching the contents of each coffin with the headstones in the
 * graveyard. Easy, huh?
 * </p>
 * <p/>
 * Last Update: 1.6 09/05/10 Jacmob.
 *
 * @author Qauters
 */
@ScriptManifest(authors = {"Qauters"}, name = "GraveDigger", version = 1.6)
public class GraveDigger extends Random {

	class Group {
		// IDs used later
		int coffinID = -1;
		int graveID = -1;

		// General group data
		final int graveStoneModelID;
		final int[] coffinModelIDs;

		public Group(final int graveStoneModelID, final int[] coffinModelIDs) {
			this.graveStoneModelID = graveStoneModelID;
			this.coffinModelIDs = coffinModelIDs;
		}

		public boolean isGroup(final int graveStoneModelID) {
			return this.graveStoneModelID == graveStoneModelID;
		}

		public boolean isGroup(final int[] coffinModelIDs) {
			for (final int modelID : this.coffinModelIDs) {
				boolean found = false;
				for (final int coffinModelID : coffinModelIDs) {
					if (modelID == coffinModelID) {
						found = true;
					}
				}
				if (!found) {
					return false;
				}
			}
			return true;
		}
	}

	private static final int[] coffinIDs = {7587, 7588, 7589, 7590, 7591};
	private static final int[] graveStoneIDs = {12716, 12717, 12718, 12719,
			12720};
	private static final int[] filledGraveIDs = {12721, 12722, 12723, 12724,
			12725};
	private static final int[] emptyGraveIDs = {12726, 12727, 12728, 12729,
			12730};

	private static final int INTERFACE_READ_GRAVESTONE = 143;
	private static final int INTERFACE_READ_GRAVESTONE_MODEL = 2;
	private static final int INTERFACE_READ_GRAVESTONE_CLOSE = 3;
	private static final int INTERFACE_CHECK_COFFIN = 141;
	private static final int INTERFACE_CHECK_COFFIN_CLOSE = 12;
	private static final int[] INTERFACE_CHECK_COFFIN_ITEMS = {3, 4, 5, 6, 7,
			8, 9, 10, 11};

	@SuppressWarnings("unused")
	private static final int[] NOT_TO_DEPOSIT = {1351, 1349, 1353, 1361, 1355,
			1357, 1359, 4031, 6739, 13470, 14108, 1265, 1267, 1269, 1296, 1273,
			1271, 1275, 15259, 303, 305, 307, 309, 311, 10129, 301, 13431, 313,
			314, 2347, 995, 10006, 10031, 10008, 10012, 11260, 10150, 10010,
			556, 558, 555, 557, 554, 559, 562, 560, 565, 8013, 4251, 8011,
			8010, 8009, 8008, 8007};

	private final ArrayList<Group> groups = new ArrayList<Group>();

	private int tmpID = -1, tmpStatus = -1; // used to store some data across
	// loops

	public GraveDigger() {
		groups.add(new Group(7614, new int[]{7603, 7605, 7612}));
		groups.add(new Group(7615, new int[]{7600, 7601, 7604}));
		groups.add(new Group(7616, new int[]{7597, 7606, 7607}));
		groups.add(new Group(7617, new int[]{7602, 7609, 7610}));
		groups.add(new Group(7618, new int[]{7599, 7608, 7613}));
	}

	@Override
	public boolean activateCondition() {
		if ((settings.getSetting(696) != 0) && (objects.getNearest(12731) != null)) {
			tmpID = tmpStatus = -1;
			return true;
		}
		return false;
	}

	@Override
	public int loop() {
		if (npcs.getNearest("Leo") == null) {
			return -1;
		}
		if (inventory.getCountExcept(GraveDigger.coffinIDs) > 23) {
			if (interfaces.canContinue()) {
				interfaces.clickContinue();
				sleep(random(1500, 2000));
			}
			RSObject depo = objects.getNearest(12731);
			if (depo != null) {
				if (!calc.tileOnScreen(depo.getLocation())) {
					walking.getPath(depo.getLocation()).traverse();
					camera.turnTo(depo);
				} else {
					depo.doAction("Deposit");
				}
			}
			if (interfaces.get(Bank.INTERFACE_DEPOSIT_BOX).isValid()) {
				sleep(random(700, 1200));
				interfaces.get(11).getComponent(17).getComponent(27).doAction("Dep");
				sleep(random(700, 1200));
				interfaces.get(11).getComponent(17).getComponent(26).doAction("Dep");
				sleep(random(700, 1200));
				interfaces.get(11).getComponent(17).getComponent(25).doAction("Dep");
				sleep(random(700, 1200));
				interfaces.get(11).getComponent(17).getComponent(24).doAction("Dep");
				sleep(random(700, 1200));
				interfaces.get(11).getComponent(17).getComponent(23).doAction("Dep");
				sleep(random(700, 1200));
				interfaces.getComponent(11, 15).doClick();
				return random(500, 700);
			}
			return (random(2000, 3000));
		}
		if (getMyPlayer().isMoving()) {
		} else if (getMyPlayer().getAnimation() == 827) {
		} else if (interfaces.get(242).isValid()) {
			// Check if we finished before
			if (interfaces.get(242).containsText("ready to leave")) {
				tmpStatus++;
			}
			interfaces.getComponent(242, 6).doClick();
		} else if (interfaces.get(64).isValid()) {
			interfaces.getComponent(64, 5).doClick();
		} else if (interfaces.get(241).isValid()) {
			interfaces.getComponent(241, 5).doClick();
		} else if (interfaces.get(243).isValid()) {
			interfaces.getComponent(243, 7).doClick();
		} else if (interfaces.get(220).isValid()) {
			interfaces.getComponent(220, 16).doClick();
		} else if (interfaces.get(236).isValid()) {
			if (interfaces.get(236).containsText("ready to leave")) {
				interfaces.getComponent(236, 1).doClick();
			} else {
				interfaces.getComponent(236, 2).doClick();
			}
		} else if (interfaces.get(GraveDigger.INTERFACE_CHECK_COFFIN).isValid()) {
			if (tmpID >= 0) {
				final int[] items = new int[GraveDigger.INTERFACE_CHECK_COFFIN_ITEMS.length];
				RSInterface inters = interfaces.get(GraveDigger.INTERFACE_CHECK_COFFIN);
				for (int i = 0; i < GraveDigger.INTERFACE_CHECK_COFFIN_ITEMS.length; i++) {
					items[i] = inters.getComponent(GraveDigger.INTERFACE_CHECK_COFFIN_ITEMS[i]).getComponentID();
				}
				for (final Iterator<Group> it = groups.iterator(); it.hasNext() && (tmpID >= 0);) {
					final Group g = it.next();
					if (g.isGroup(items)) {
						g.coffinID = tmpID;
						tmpID = -1;
					}
				}
			}
			atCloseInterface(GraveDigger.INTERFACE_CHECK_COFFIN, GraveDigger.INTERFACE_CHECK_COFFIN_CLOSE);
		} else if (interfaces.get(GraveDigger.INTERFACE_READ_GRAVESTONE).isValid()) {
			final int modelID = interfaces.get(GraveDigger.INTERFACE_READ_GRAVESTONE).getComponent(GraveDigger.INTERFACE_READ_GRAVESTONE_MODEL).getComponentID();
			for (final Group g : groups) {
				if (g.isGroup(modelID)) {
					g.graveID = tmpID;
				}
			}
			atCloseInterface(GraveDigger.INTERFACE_READ_GRAVESTONE, GraveDigger.INTERFACE_READ_GRAVESTONE_CLOSE);
		} else if ((tmpStatus == 0) && (tmpID != -1)) {
			for (final Group g : groups) {
				if (g.graveID == tmpID) {
					final RSObject obj = objects.getNearest(g.graveID);
					if ((obj == null) || !setObjectInScreen(obj)) {
						log.info("Couldn't find grave, shutting down.");
						game.logout(false);
						return -1;
					}
					// if (isItemSelected() > 0) {
					// inventory.atItem(GraveDigger.coffinIDs[g.coffinID],
					// "Cancel");
					// }

					inventory.useItem(inventory.getItem(GraveDigger.coffinIDs[g.coffinID]), obj);

					// Wait for about 10s to finish
					final long cTime = System.currentTimeMillis();
					while (System.currentTimeMillis() - cTime < 10000) {
						if (inventory.getItem(GraveDigger.coffinIDs[g.coffinID]) == null) {
							break;
						}
						sleep(random(400, 700));
					}
					break;
				}
			}
			tmpID = -1;
		} else if ((tmpStatus == -1) && (objects.getNearest(GraveDigger.filledGraveIDs) != null)) {
			final RSObject obj = objects.getNearest(GraveDigger.filledGraveIDs);
			if ((obj == null) || !setObjectInScreen(obj)) {
				log.severe("Couldn't find grave, shutting down.");
				game.logout(false);
				return -1;
			}
			obj.doAction("Take-coffin");
		} else if ((tmpStatus == 0) && (objects.getNearest(GraveDigger.emptyGraveIDs) != null)) {
			final RSObject obj = objects.getNearest(GraveDigger.emptyGraveIDs);
			final int id = obj.getID();
			for (int i = 0; i < GraveDigger.emptyGraveIDs.length; i++) {
				if (GraveDigger.emptyGraveIDs[i] == id) {
					final RSObject objGS = objects.getNearest(GraveDigger.graveStoneIDs[i]);
					if ((objGS == null) || !setObjectInScreen(objGS)) {
						log.severe("Couldn't find grave stone, shutting down.");
						game.logout(false);
						return -1;
					}
					tmpID = obj.getID();
					// if (Bot.getClient().isItemSelected() == 1) {
					// objects.atObject(objGS, "Use");
					// }
					objGS.doAction("Read");
				}
			}
		} else if (tmpStatus == -1) {
			final ArrayList<Integer> agc = new ArrayList<Integer>();
			for (int i = 0; i < GraveDigger.coffinIDs.length; i++) {
				agc.add(i);
			}
			for (final Group g : groups) {
				if (g.coffinID != -1) {
					agc.remove(new Integer(g.coffinID));
				}
			}
			if ((tmpStatus == -1) && (agc.size() == 0)) {
				tmpStatus++;
			}
			while (tmpStatus == -1) {
				final int i = random(0, agc.size());
				if (inventory.getCount(GraveDigger.coffinIDs[agc.get(i)]) > 0) {
					tmpID = agc.get(i);
					inventory.getItem(GraveDigger.coffinIDs[agc.get(i)]).doAction("Check");

					return random(1800, 2400); // We are looking at the model
				}
			}
		} else if (tmpStatus == 0) {
			// Done
			final RSNPC leo = npcs.getNearest("Leo");
			if ((leo == null) || !setCharacterInScreen(leo)) {
				log.severe("Couldn't find Leo, shutting down.");
				game.logout(false);
				return -1;
			}
			//Teleport Ani - 8939
			if (getMyPlayer().getAnimation() == -1) {
				leo.doAction("Talk-to");
			}
		}
		return random(1400, 1800);
	}

	public boolean atCloseInterface(int parent, int child) {
		RSComponent i = interfaces.getComponent(parent, child);
		if (!i.isValid()) {
			return false;
		}
		Rectangle pos = i.getArea();
		if (pos.x == -1 || pos.y == -1 || pos.width == -1 || pos.height == -1) {
			return false;
		}
		int dx = (int) (pos.getWidth() - 4) / 2;
		int dy = (int) (pos.getHeight() - 4) / 2;
		int midx = (int) (pos.getMinX() + pos.getWidth() / 2);
		int midy = (int) (pos.getMinY() + pos.getHeight() / 2);
		mouse.click(midx + random(-dx, dx) - 5, midy + random(-dy, dy), true);
		return true;
	}

	public boolean setCharacterInScreen(final RSCharacter ch) {
		// Check if it's on screen, if not make it on screen.
		for (int i = 0; i < 3; i++) {
			final Point screenLocation = ch.getScreenLocation();
			if (!calc.pointOnScreen(screenLocation)) {
				switch (i) {
					case 0:
						camera.turnTo(ch);
						sleep(random(200, 500));
						break;
					case 1:
						walking.walkTileMM(walking.getClosestTileOnMap(ch.getLocation().randomize(2, 2)));
						sleep(random(1800, 2000));
						while (getMyPlayer().isMoving()) {
							sleep(random(200, 500));
						}
						break;
					default:
						return false;
				}
			}
		}
		return true;
	}

	public boolean setObjectInScreen(final RSObject obj) {
		// Check if it's on screen, if not make it on screen.
		for (int i = 0; i < 3; i++) {
			final Point screenLocation = calc.tileToScreen(obj.getLocation());
			if (!calc.pointOnScreen(screenLocation)) {
				switch (i) {
					case 0:
						camera.turnTo(obj);
						sleep(random(200, 500));
						break;
					case 1:
						walking.walkTileMM(walking.getClosestTileOnMap(obj.getLocation().randomize(2, 2)));
						sleep(random(1800, 2000));
						while (getMyPlayer().isMoving()) {
							sleep(random(200, 500));
						}
						break;
					default:
						return false;
				}
			}
		}
		return true;
	}
}