package org.rsbot.script.randoms;

import org.rsbot.event.events.MessageEvent;
import org.rsbot.event.listeners.MessageListener;
import org.rsbot.script.Random;
import org.rsbot.script.ScriptManifest;
import org.rsbot.script.methods.Equipment;
import org.rsbot.script.methods.Game;
import org.rsbot.script.wrappers.RSGroundItem;
import org.rsbot.script.wrappers.RSInterface;
import org.rsbot.script.wrappers.RSNPC;
import org.rsbot.script.wrappers.RSObject;
import org.rsbot.script.wrappers.RSTile;

/**
 * @version 2.4 - 6/12/10 Fix by Arbiter
 */
@ScriptManifest(authors = {"Pwnaz0r", "Taha", "zqqou", "Zach"}, name = "FreakyForester", version = 2.4)
public class FreakyForester extends Random implements MessageListener {

	private RSNPC forester;
	private static final int FORESTER_ID = 2458;
	private static final int SEARCH_INTERFACE_ID = 242;
	private static final int PORTAL_ID = 8972;
	private static final RSTile WALK_TO_TILE = new RSTile(2610, 4775);
	private boolean unequip = false;
	int phe = -1;

	boolean done = false;

	public boolean activateCondition() {
		if (!game.isLoggedIn())
			return false;

		forester = npcs.getNearest(FORESTER_ID);
		if (forester != null) {
			sleep(random(2000, 3000));
			if (npcs.getNearest(FORESTER_ID) != null) {
				RSObject portal = objects.getNearest(PORTAL_ID);
				return portal != null;
			}
		}
		return false;
	}

	public int getState() {
		if (done)
			return 3;
		else if (interfaces.canContinue())
			return 1;
		else if (phe == -1)
			return 0;
		else if (inventory.contains(6178))
			return 0;
		else if (phe != -1)
			return 2;
		else
			return 0;
	}

	@Override
	public int loop() {
		forester = npcs.getNearest(FORESTER_ID);
		if (forester == null)
			return -1;

		if (getMyPlayer().getAnimation() != -1)
			return random(3000, 5000);
		else if (getMyPlayer().isMoving())
			return random(200, 500);

		if (!done) {
			done = searchText(241, "Thank you") || interfaces.getComponent(242, 4).containsText("leave");
		}

		if (inventory.contains(6179)) {
			phe = -1;
			inventory.getItem(6179).doAction("Drop");
			return random(500, 900);
		}
		if (unequip && (inventory.getCount(false) != 28)) {
			if (game.getCurrentTab() != Game.TAB_EQUIPMENT) {
				game.openTab(Game.TAB_EQUIPMENT);
				sleep(random(1000, 1500));
				interfaces.get(Equipment.INTERFACE_EQUIPMENT).getComponent(17).doClick();
				return (random(1000, 1500));
			}
			return (random(100, 500));
		}

		if (bank.isDepositOpen() || (inventory.getCount(false) == 28) && !inventory.containsAll(6178)) {
			if (bank.isDepositOpen() && bank.getBoxCount() == 28) {
				interfaces.get(11).getComponent(17).getComponent(random(21, 27)).doAction("Deposit");
				return random(1000, 1500);
			} else if (bank.isDepositOpen()) {
				bank.close();
				return random(1000, 1500);
			}
			final RSObject box = objects.getNearest(32931);
			if ((!calc.tileOnScreen(box.getLocation()) && ((calc.distanceTo(walking.getDestination())) < 8)) || (calc.distanceTo(walking.getDestination()) > 40)) {
				if (!walking.walkTileMM(walking.randomize(box.getLocation(), 3, 3))) {
					walking.newTilePath(walking.findPath(walking.randomize(box.getLocation(), 3, 3))).traverse();
				}
				sleep(random(1200, 1400));
			}
			if (box.doAction("Deposit")) {
				return random(800, 1200);
			}
		}

		switch (getState()) {
			case 0: // Talk to forester
				if (calc.tileOnScreen(forester.getLocation()) && (calc.distanceTo(forester.getLocation()) <= 5)) {
					forester.doAction("Talk");
				} else if (calc.distanceTo(forester.getLocation()) >= 5) {
					walking.walkTileMM(walking.getClosestTileOnMap(walking.randomize(forester.getLocation(), 3, 3)));
					camera.turnToTile(walking.randomize(forester.getLocation(), 3, 3));
				}
				return random(500, 800);
			case 1: // Talking
				if (searchText(SEARCH_INTERFACE_ID, "one-")) {
					phe = 2459;
				} else if (searchText(SEARCH_INTERFACE_ID, "two-")) {
					phe = 2460;
				} else if (searchText(SEARCH_INTERFACE_ID, "three-")) {
					phe = 2461;
				}
				if (searchText(SEARCH_INTERFACE_ID, "four-")) {
					phe = 2462;
				}
				if (phe != -1) {
					log.info("Pheasant ID: " + phe);
				}
				if (myClickContinue())
					return random(500, 800);
				return random(200, 500);
			case 2: // Kill pheasant
				if (phe == -1)
					return random(200, 500);
				final RSNPC Pheasant = npcs.getNearest(phe);
				final RSGroundItem tile = groundItems.getNearest(6178);
				if (tile != null) {
					tiles.doAction(tile.getLocation(), "Take");
					return random(600, 900);
				} else if (Pheasant != null) {
					if (calc.tileOnScreen(Pheasant.getLocation()) && (calc.distanceTo(Pheasant.getLocation()) <= 5)) {
						Pheasant.doAction("Attack");
						return random(1000, 1500);
					} else if (calc.distanceTo(Pheasant.getLocation()) >= 5) {
						walking.walkTileMM(walking.getClosestTileOnMap(walking.randomize(Pheasant.getLocation(), 3, 3)));
						camera.turnToTile(walking.randomize(Pheasant.getLocation(), 3, 3));
					}
				} else
					return random(2000, 5000);
			case 3: // Get out
				if (!calc.tileOnScreen(WALK_TO_TILE)) {
					if (calc.tileOnMap(WALK_TO_TILE)) {
						walking.walkTileMM(WALK_TO_TILE);
					} else {
						walking.newTilePath(walking.findPath(walking.randomize(forester.getLocation(), 5, 5))).traverse();
					}
					return random(900, 1200);
				}

				final RSObject Portal = objects.getNearest(PORTAL_ID);

				if (Portal == null) {
					log.info("Could not find portal.");
					return random(800, 1200);
				}

				if (Portal.doAction("Enter"))
					return random(800, 1200);
				return random(200, 500);
		}
		return random(1000, 1500);
	}

	public boolean myClickContinue() {
		sleep(random(800, 1000));
		return interfaces.getComponent(243, 7).doClick() || interfaces.getComponent(241, 5).doClick() || interfaces.getComponent(242, 6).doClick() || interfaces.getComponent(244, 8).doClick() || interfaces.getComponent(64, 5).doClick();
	}

	public boolean searchText(final int interfac, final String text) {
		final RSInterface talkFace = interfaces.get(interfac);
		if (!talkFace.isValid())
			return false;
		for (int i = 0; i < talkFace.getChildCount(); i++) {
			if (talkFace.getComponent(i).containsText(text))
				return true;
		}

		return false;
	}

	public void messageReceived(final MessageEvent e) {
		final String serverString = e.getMessage();
		if (serverString.contains("no ammo left")) {
			unequip = true;
		}

	}

}