import java.awt.Point;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.swing.JOptionPane;

import org.rsbot.script.Script;
import org.rsbot.script.ScriptManifest;
import org.rsbot.script.methods.Game;
import org.rsbot.script.methods.Objects;
import org.rsbot.script.methods.Skills;
import org.rsbot.script.wrappers.RSComponent;
import org.rsbot.script.wrappers.RSGroundItem;
import org.rsbot.script.wrappers.RSItem;
import org.rsbot.script.wrappers.RSObject;
import org.rsbot.script.wrappers.RSTile;

@ScriptManifest(name = "NGHunter", authors = "Rakura", keywords = "Hunter", version = 1.0, description = "crim swifts, trop wags, <grey,red> chins")
public class NGHunter extends Script {

	private enum TileType {
		FALLEN, CAUGHT, FAILED, UP, EMPTY;
	}

	private class HunterTile extends RSTile implements Comparable<HunterTile> {
		boolean isOurTrap;
		TileType type;
		Object associated;

		HunterTile(int x, int y) {
			super(x, y);
		}

		@Override
		public int compareTo(HunterTile other) {
			return calc.distanceTo(this) - calc.distanceTo(other);
		}
	}

	private static class Constants {

		static final int BOX = 10008;
		static final int BOX_WAIT = 19187;
		static final int BOX_FAILED = 19192;
		static final int BOX_RED_CAUGHT = 19190;
		static final int BOX_GREY_CAUGHT = 19189;

		static final int SNARE = 10006;
		static final int SNARE_WAIT = 19175;
		static final int SNARE_FAILED = 19174;
		static final int TROPICAL_WAGTAIL_CAUGHT = 19178;
		static final int CRIMSON_SWIFT_CAUGHT = 19180;

		static final int BIRD_MEAT = 9978;
		static final int BONES = 526;

		final int level;

		final int trap;
		final int wait;
		final int failed;
		final int caught;

		final boolean bury;

		public Constants(int level) {
			this.level = level;
			if (level >= 53) {// we're goin boxtrappin
				trap = BOX;
				wait = BOX_WAIT;
				failed = BOX_FAILED;
				caught = level >= 63 ? BOX_RED_CAUGHT : BOX_GREY_CAUGHT;
				bury = false;// undef
			} else {// we're goin snarin
				trap = SNARE;
				wait = SNARE_WAIT;
				failed = SNARE_FAILED;
				caught = level >= 19 ? TROPICAL_WAGTAIL_CAUGHT
						: CRIMSON_SWIFT_CAUGHT;
				int selection = JOptionPane.showConfirmDialog(null,
						"Bury bones?", "NGHunter", JOptionPane.YES_NO_OPTION,
						JOptionPane.QUESTION_MESSAGE);
				bury = selection == JOptionPane.YES_OPTION;
			}
		}

	}

	private Constants consts;
	private HunterTile originalTile;
	private int nTraps;
	private final ArrayList<HunterTile> myTiles = new ArrayList<HunterTile>();

	private boolean waiting;
	private int phailCount;

	// private int counter;

	@Override
	public boolean onStart() {

		if (!walking.isRunEnabled())
			walking.setRun(true);

		int startLevel = skills.getCurrentLevel(Skills.HUNTER);
		consts = new Constants(startLevel);

		RSTile origTile = getMyPlayer().getLocation();
		originalTile = new HunterTile(origTile.getX(), origTile.getY());

		if (startLevel < 20) {
			nTraps = 1;
			myTiles.add(originalTile);
		} else if (startLevel < 40) {
			nTraps = 2;
			// quad 1
			myTiles.add(new HunterTile(originalTile.getX() + 1, originalTile
					.getY() + 1));
			// quad 2
			myTiles.add(new HunterTile(originalTile.getX() - 1, originalTile
					.getY() + 1));
		} else if (startLevel < 60) {
			nTraps = 3;
			// origin
			myTiles.add(originalTile);
			// quad 1
			myTiles.add(new HunterTile(originalTile.getX() + 1, originalTile
					.getY() + 1));
			// quad 2
			myTiles.add(new HunterTile(originalTile.getX() - 1, originalTile
					.getY() + 1));
		} else if (startLevel < 80) {
			nTraps = 4;
			// quad 1
			myTiles.add(new HunterTile(originalTile.getX() + 1, originalTile
					.getY() + 1));
			// quad 2
			myTiles.add(new HunterTile(originalTile.getX() - 1, originalTile
					.getY() + 1));
			// quad 3
			myTiles.add(new HunterTile(originalTile.getX() - 1, originalTile
					.getY() - 1));
			// quad 4
			myTiles.add(new HunterTile(originalTile.getX() + 1, originalTile
					.getY() - 1));
		} else { // startLevel >= 80
			nTraps = 5;
			// origin
			myTiles.add(originalTile);
			// quad 1
			myTiles.add(new HunterTile(originalTile.getX() + 1, originalTile
					.getY() + 1));
			// quad 2
			myTiles.add(new HunterTile(originalTile.getX() - 1, originalTile
					.getY() + 1));
			// quad 3
			myTiles.add(new HunterTile(originalTile.getX() - 1, originalTile
					.getY() - 1));
			// quad 4
			myTiles.add(new HunterTile(originalTile.getX() + 1, originalTile
					.getY() - 1));
		}

		return true;

	}

	private boolean needCleaning() {
		return inventory.getCount() >= 26;
	}

	private void cleanInventory() {
		RSItem[] meatNBones = inventory.getItems(Constants.BIRD_MEAT,
				Constants.BONES);
		List<RSItem> items = new ArrayList<RSItem>(Arrays.asList(meatNBones));
		Collections.shuffle(items);
		for (RSItem item : items) {
			if (item.getID() == Constants.BIRD_MEAT) {
				item.doAction("Drop");
			} else if (item.getID() == Constants.BONES) {
				if (consts.bury) {
					item.doClick(true);
				} else {
					item.doAction("Drop");
				}
			}
		}
	}

	@Override
	public int loop() {
		if (consts.level < 53) {
			if (waiting || needCleaning())
				cleanInventory();
		}
		return loopAction();
	}

	private int loopAction() {
		mouse.setSpeed(random(5, 8));
		verifyTiles();
		HunterTile currentTile = nextTile(null);
		if (currentTile != null) {
			boolean success;
			switch (currentTile.type) {
			case FALLEN:
				success = reset(currentTile);
				break;
			case CAUGHT:
			case FAILED:
				success = collect(currentTile);
				if (success) {
					success = setup(currentTile);
				}
				break;
			case EMPTY:
				if (inventory.containsOneOf(consts.trap)) {
					success = setup(currentTile);
				} else {
					myTiles.remove(currentTile);
					nTraps--;
					success = true;
				}
				break;
			default:
				// never happens. this is just to satisfy the compiler that
				// 'success' is initialized
				success = false;
			}
			if (success) {
				phailCount = 0;
			} else {
				phailCount++;
			}
		} // else if (!waiting) {
		// throw new AssertionError();
		// }
		if (phailCount > 10)
			stopScript();
		return 0;
	}

	private void verifyTiles() {
		HunterTile[] tiles = myTiles.toArray(new HunterTile[myTiles.size()]);
		for (int i = 0; i < tiles.length; i++) {
			HunterTile tile = tiles[i];
			if (!tile.isOurTrap && !isFree(tile)) {
				HunterTile nearestFreeTile = getOptimalFreeTile(4);
				if (nearestFreeTile != null) {
					myTiles.set(i, nearestFreeTile);
				} else {
					myTiles.remove(i);
					nTraps--;
				}
			}
		}
	}

	// priority:
	// 1. fallen
	// 2. nearest <caught, failed>
	// 3. empty
	private HunterTile nextTile(HunterTile exclude) {
		int waitingTraps = 0;
		Collections.sort(myTiles);
		for (HunterTile tile : myTiles) {
			RSGroundItem[] gritems = groundItems.getAllAt(tile);
			for (RSGroundItem gritem : gritems) {
				if (gritem.getItem().getID() == consts.trap) {
					waiting = false;
					tile.type = TileType.FALLEN;
					tile.associated = gritem;
					if (tile != exclude)
						return tile;
				}
			}
		}
		for (HunterTile tile : myTiles) {
			RSObject[] rsobjs = objects.getAt(tile, Objects.TYPE_INTERACTABLE);
			for (RSObject rsobj : rsobjs) {
				int id = rsobj.getID();
				if (id == consts.caught) {
					waiting = false;
					tile.type = TileType.CAUGHT;
					tile.associated = rsobj;
					if (tile != exclude)
						return tile;
				}
				if (id == consts.failed) {
					waiting = false;
					tile.type = TileType.FAILED;
					tile.associated = rsobj;
					if (tile != exclude)
						return tile;
				}
				if (id == consts.wait) {
					waitingTraps++;
					if (waitingTraps == nTraps) {
						waiting = true;
						return null;
					}
				}
			}
		}
		for (HunterTile tile : myTiles) {
			if (isFree(tile)) {
				waiting = false;
				tile.type = TileType.EMPTY;
				tile.associated = null;
				if (tile != exclude)
					return tile;
			}
		}
		return null;
	}

	private HunterTile getOptimalFreeTile(int maxOutwards) {
		int minX = originalTile.getX() - maxOutwards;
		int maxX = originalTile.getX() + maxOutwards;
		int minY = originalTile.getY() - maxOutwards;
		int maxY = originalTile.getY() + maxOutwards;
		List<HunterTile> available = new ArrayList<HunterTile>();
		for (int x = minX; x <= maxX; x++) {
			for (int y = minY; y <= maxY; y++) {
				HunterTile t = new HunterTile(x, y);
				if (isFree(t))
					available.add(t);
			}
		}
		return getNearest(originalTile, available);
	}

	private boolean isFree(HunterTile tile) {
		for (RSObject obj : objects.getAllAt(tile)) {
			RSObject.Type objType = obj.getType();
			if (objType == RSObject.Type.INTERACTABLE
					|| objType == RSObject.Type.BOUNDARY)
				return false;
		}
		return true;
	}

	private HunterTile getNearest(HunterTile locus, List<HunterTile> tiles) {
		HunterTile nearest = null;
		double min = Double.MAX_VALUE;
		for (HunterTile tile : tiles) {
			double dist = calc.distanceBetween(locus, tile);
			if (dist < min) {
				nearest = tile;
				min = dist;
			}
		}
		return nearest;
	}

	private boolean setup(HunterTile tile) {
		if (walk(tile)) {
			if (clickInventoryItem(consts.trap)) {
				return trapUp(tile);
			}
		}
		return false;
	}

	private boolean reset(HunterTile tile) {
		RSGroundItem trap = (RSGroundItem) tile.associated;
		if (trap.doAction("Lay")) {
			return trapUp(tile);
		}
		return false;
	}

	private boolean trapUp(HunterTile tile) {
		if (waitForAnim(3000)) {
			HunterTile nextTile = nextTile(tile);
			if (nextTile != null)
				hoverTile(nextTile);
			while (!getMyPlayer().isIdle()) {
				sleep(100);
			}
			// player shifts
			if (nextTile != null)
				hoverTile(nextTile);
			if (trapIsHere(tile)) {
				tile.isOurTrap = true;
				tile.type = TileType.UP;
				return true;
			}
		}
		return false;
	}

	private boolean collect(final HunterTile tile) {
		RSObject trap = (RSObject) tile.associated;
		trap.doClick();
		if (waitForAnim(3000)) {
			while (trapIsHere(tile)) {
				if (getMyPlayer().isIdle())
					break;
				sleep(100);
			}
			if (!trapIsHere(tile)) {
				tile.isOurTrap = false;
				tile.type = TileType.EMPTY;
				return true;
			}
		}
		return false;
	}

	private void hoverTile(RSTile tile) {
		if (!tile.equals(tiles.getTileUnderMouse())) {
			Point location = calc.tileToScreen(tile);
			mouse.move(location, 5, 5);
		}
	}

	private void atTile(RSTile tile) {
		hoverTile(tile);
		mouse.click(true);
	}

	private boolean trapIsHere(RSTile tile) {
		RSObject[] objs = objects.getAt(tile, Objects.TYPE_INTERACTABLE);
		for (RSObject obj : objs) {
			int id = obj.getID();
			if (id == consts.wait || id == consts.caught || id == consts.failed) {
				return true;
			}
		}
		return false;
	}

	private boolean walk(RSTile tile) {
		atTile(tile);
		waitForMove(3000);
		while (!onTile(tile)) {
			if (getMyPlayer().isIdle())
				break;
			moveToInventoryItem(consts.trap);
			sleep(100);
		}
		return onTile(tile);
	}

	private boolean onTile(RSTile tile) {
		return getMyPlayer().getLocation().equals(tile)
				&& getMyPlayer().isIdle();
	}

	private boolean clickInventoryItem(int itemID) {
		if (moveToInventoryItem(itemID)) {
			mouse.click(true);
			return true;
		}
		return false;
	}

	private boolean moveToInventoryItem(int itemID) {
		if (game.getCurrentTab() != Game.TAB_INVENTORY)
			game.openTab(Game.TAB_INVENTORY);
		RSItem item = inventory.getItem(itemID);
		RSComponent wrappedComp = item.getComponent();
		if (wrappedComp == null)
			return false;
		wrappedComp.doHover();
		return true;
	}

	private boolean waitForAnim(int maxMillis) {
		long start = System.currentTimeMillis();
		while (System.currentTimeMillis() - start < maxMillis) {
			if (getMyPlayer().getAnimation() != -1)
				return true;
		}
		return false;
	}

	private boolean waitForMove(int maxMillis) {
		long start = System.currentTimeMillis();
		while (System.currentTimeMillis() - start < maxMillis) {
			if (getMyPlayer().isMoving())
				return true;
		}
		return false;
	}

	// @Override
	// public void messageReceived(MessageEvent e) {
	// String s = e.getMessage().toLowerCase();
	// if (s.contains("caught"))
	// counter++;
	// }

}