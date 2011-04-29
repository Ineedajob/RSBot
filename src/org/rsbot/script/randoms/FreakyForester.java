package org.rsbot.script.randoms;

import org.rsbot.event.events.MessageEvent;
import org.rsbot.event.listeners.MessageListener;
import org.rsbot.script.Random;
import org.rsbot.script.ScriptManifest;
import org.rsbot.script.methods.Equipment;
import org.rsbot.script.methods.Game;
import org.rsbot.script.util.Filter;
import org.rsbot.script.wrappers.*;

/**
 * @version 2.5 - 12/31/10 Fix by NoEffex (Models)
 */
@ScriptManifest(authors = {"Pwnaz0r", "Taha", "zqqou", "Zach"}, name = "FreakyForester", version = 2.5)
public class FreakyForester extends Random implements MessageListener {

	private RSNPC forester;
	private static final int FORESTER_ID = 2458;
	private static final int SEARCH_INTERFACE_ID = 242;
	private static final int PORTAL_ID = 15645;
	private static final RSTile WALK_TO_TILE = new RSTile(2610, 4775);
	private boolean unequip = false;
	short[] phe = {};
	final Filter<RSNPC> pheasantFilter = new Filter<RSNPC>() {
		public boolean accept(RSNPC npc) {
			// log("phe.length = " + phe.length);
			Filter<RSModel> modelFilter = RSModel.newVertexFilter(phe);
			return modelFilter.accept(npc.getModel());
		}
	};

	boolean done = false;

	static class Models {
		static final short[] oneTail = {0, 0, 0, 0, 0, 1, 1, 1, 1, 1, 2, 2, 2,
				3, 3, 3, 4, 4, 4, 4, 5, 5, 6, 6, 6, 6, 7, 8, 8, 8, 8, 9, 9, 10,
				10, 10, 10, 10, 11, 11, 11, 11, 11, 12, 12, 13, 13, 14, 14, 14,
				14, 15, 15, 15, 16, 16, 17, 17, 17, 18, 18, 18, 18, 18, 19, 19,
				34, 23, 23, 24, 25, 25, 25, 25, 25, 26, 26, 27, 27, 28, 28, 29,
				29, 29, 30, 30, 31, 31, 32, 32, 32, 32, 33, 33, 60, 60, 60, 63,
				63, 65, 65, 65, 60, 64, 64, 66, 66, 66, 66, 66, 70, 70, 70, 70,
				70, 70, 71, 71, 71, 71, 74, 74, 74, 74, 74, 74, 74, 73, 73, 61,
				59, 72, 69, 69, 69, 69, 57, 57, 50, 50, 50, 56, 57, 77, 79, 82,
				41, 41, 41, 41, 42, 42, 42, 42, 43, 43, 43, 43, 43, 44, 44, 44,
				95, 95, 95, 95, 95, 95, 85, 85, 86, 86, 86, 87, 87, 101, 101,
				101, 101, 101, 101, 102, 102, 102, 102, 103, 103, 103, 104,
				104, 104, 105, 105, 105, 105, 105, 105, 105, 105, 106, 106,
				107, 107, 108, 108, 108, 94, 35, 35, 36, 36, 36, 37, 37, 38,
				110, 111, 111, 111, 111, 111, 111, 112, 113, 99, 99, 99, 100,
				100, 100, 131, 97, 98, 129, 129, 129, 130, 130, 130, 130, 130,
				130, 130, 131, 131, 131, 131, 131, 131, 126, 126, 116, 146,
				118, 109, 120, 90, 136, 136, 143, 148, 139, 139, 149, 149, 149,
				153, 154, 154, 156, 157, 157, 150, 151, 151, 151, 152, 152,
				152, 155, 158, 159, 159, 159, 160, 160, 161, 161, 161, 164,
				164, 164, 164, 165, 165, 167, 167, 164, 164, 164, 164, 172,
				172, 172, 172, 172, 172, 171, 171,};
		static final short[] twoTail = {0, 0, 0, 0, 0, 1, 1, 1, 1, 1, 2, 2, 2,
				3, 3, 3, 4, 4, 4, 4, 5, 5, 6, 6, 6, 6, 7, 8, 8, 8, 8, 9, 9, 10,
				10, 10, 10, 10, 11, 11, 11, 11, 11, 12, 12, 13, 13, 14, 14, 14,
				14, 15, 15, 15, 16, 16, 17, 17, 17, 18, 18, 18, 18, 18, 19, 19,
				34, 23, 23, 24, 25, 25, 25, 25, 25, 26, 26, 27, 27, 28, 28, 29,
				29, 29, 30, 30, 31, 31, 32, 32, 32, 32, 33, 33, 60, 60, 60, 63,
				63, 65, 65, 65, 60, 64, 64, 66, 66, 66, 66, 66, 70, 70, 70, 70,
				70, 70, 71, 71, 71, 71, 74, 74, 74, 74, 74, 74, 74, 73, 73, 61,
				59, 72, 69, 69, 69, 69, 57, 57, 50, 50, 50, 56, 57, 77, 79, 82,
				41, 41, 41, 41, 42, 42, 42, 42, 43, 43, 43, 43, 43, 44, 44, 44,
				95, 95, 95, 95, 95, 95, 85, 85, 86, 86, 86, 87, 87, 101, 101,
				101, 101, 101, 101, 102, 102, 102, 102, 103, 103, 103, 104,
				104, 104, 105, 105, 105, 105, 105, 105, 105, 105, 106, 106,
				107, 107, 108, 108, 108, 94, 35, 35, 36, 36, 36, 37, 37, 38,
				110, 111, 111, 111, 111, 111, 111, 112, 113, 99, 99, 99, 100,
				100, 100, 131, 97, 98, 129, 129, 129, 130, 130, 130, 130, 130,
				130, 130, 131, 131, 131, 131, 131, 131, 126, 126, 116, 146,
				118, 109, 120, 90, 136, 136, 143, 148, 139, 139, 149, 149, 149,
				149, 149, 149, 150, 150, 150, 150, 151, 151, 151, 151, 152,
				152, 152, 152, 153, 153, 153, 154, 154, 154, 154, 155, 155,
				166, 166, 156, 165, 155, 155, 156, 156, 169, 169, 172, 172,
				172, 172, 172, 172, 161, 162, 162, 162, 163, 163, 163, 164,
				158, 158, 158, 175, 175, 175, 175, 176, 176, 176, 176, 184,
				184, 185, 185, 178, 182, 182, 182, 182, 177, 189, 189, 189,
				189, 190, 190, 188, 188, 179, 179, 179, 160,};
		static final short[] threeTail = {0, 0, 0, 0, 0, 1, 1, 1, 1, 1, 2, 2,
				2, 3, 3, 3, 4, 4, 4, 4, 5, 5, 6, 6, 6, 6, 7, 8, 8, 8, 8, 9, 9,
				10, 10, 10, 10, 10, 11, 11, 11, 11, 11, 12, 12, 13, 13, 14, 14,
				14, 14, 15, 15, 15, 16, 16, 17, 17, 17, 18, 18, 18, 18, 18, 19,
				19, 34, 23, 23, 24, 25, 25, 25, 25, 25, 26, 26, 27, 27, 28, 28,
				29, 29, 29, 30, 30, 31, 31, 32, 32, 32, 32, 33, 33, 60, 60, 60,
				63, 63, 65, 65, 65, 60, 64, 64, 66, 66, 66, 66, 66, 70, 70, 70,
				70, 70, 70, 71, 71, 71, 71, 74, 74, 74, 74, 74, 74, 74, 73, 73,
				61, 59, 72, 69, 69, 69, 69, 57, 57, 50, 50, 50, 56, 57, 77, 79,
				82, 41, 41, 41, 41, 42, 42, 42, 42, 43, 43, 43, 43, 43, 44, 44,
				44, 95, 95, 95, 95, 95, 95, 85, 85, 86, 86, 86, 87, 87, 101,
				101, 101, 101, 101, 101, 102, 102, 102, 102, 103, 103, 103,
				104, 104, 104, 105, 105, 105, 105, 105, 105, 105, 105, 106,
				106, 107, 107, 108, 108, 108, 94, 35, 35, 36, 36, 36, 37, 37,
				38, 110, 111, 111, 111, 111, 111, 111, 112, 113, 99, 99, 99,
				100, 100, 100, 131, 97, 98, 129, 129, 129, 130, 130, 130, 130,
				130, 130, 130, 131, 131, 131, 131, 131, 131, 126, 126, 116,
				146, 118, 109, 120, 90, 136, 136, 143, 148, 139, 139, 149, 149,
				149, 149, 149, 149, 150, 150, 150, 150, 151, 151, 151, 151,
				152, 152, 152, 152, 152, 153, 153, 153, 154, 154, 154, 154,
				155, 155, 167, 167, 156, 166, 155, 155, 156, 156, 170, 170,
				173, 173, 173, 173, 173, 173, 176, 176, 176, 176, 176, 176,
				163, 163, 164, 164, 164, 180, 180, 180, 180, 181, 181, 183,
				183, 181, 178, 180, 180, 180, 180, 188, 188, 188, 188, 188,
				188, 187, 187, 192, 192, 192, 158, 158, 158, 158, 159, 159,
				159, 160, 160, 160, 161, 200, 200, 200, 200, 199, 201, 201,
				201, 201, 203, 203, 203, 203, 197, 197, 197, 197, 202, 202,
				206, 206, 209, 209,};
		static final short[] fourTail = {0, 0, 0, 0, 0, 1, 1, 1, 1, 1, 2, 2,
				2, 3, 3, 3, 4, 4, 4, 4, 5, 5, 6, 6, 6, 6, 7, 8, 8, 8, 8, 9, 9,
				10, 10, 10, 10, 10, 11, 11, 11, 11, 11, 12, 12, 13, 13, 14, 14,
				14, 14, 15, 15, 15, 16, 16, 17, 17, 17, 18, 18, 18, 18, 18, 19,
				19, 34, 23, 23, 24, 25, 25, 25, 25, 25, 26, 26, 27, 27, 28, 28,
				29, 29, 29, 30, 30, 31, 31, 32, 32, 32, 32, 33, 33, 60, 60, 60,
				63, 63, 65, 65, 65, 60, 64, 64, 66, 66, 66, 66, 66, 70, 70, 70,
				70, 70, 70, 71, 71, 71, 71, 74, 74, 74, 74, 74, 74, 74, 73, 73,
				61, 59, 72, 69, 69, 69, 69, 57, 57, 50, 50, 50, 56, 57, 77, 79,
				82, 41, 41, 41, 41, 42, 42, 42, 42, 43, 43, 43, 43, 43, 44, 44,
				44, 95, 95, 95, 95, 95, 95, 85, 85, 86, 86, 86, 87, 87, 101,
				101, 101, 101, 101, 101, 102, 102, 102, 102, 103, 103, 103,
				104, 104, 104, 105, 105, 105, 105, 105, 105, 105, 105, 106,
				106, 107, 107, 108, 108, 108, 94, 35, 35, 36, 36, 36, 37, 37,
				38, 110, 111, 111, 111, 111, 111, 111, 112, 113, 99, 99, 99,
				100, 100, 100, 131, 97, 98, 129, 129, 129, 130, 130, 130, 130,
				130, 130, 130, 131, 131, 131, 131, 131, 131, 126, 126, 116,
				146, 118, 109, 120, 90, 136, 136, 143, 148, 139, 139, 149, 149,
				149, 149, 149, 149, 150, 150, 150, 150, 150, 150, 150, 151,
				151, 152, 152, 164, 164, 166, 167, 167, 169, 169, 153, 154,
				154, 154, 155, 155, 155, 155, 156, 156, 156, 156, 177, 177,
				177, 177, 163, 165, 165, 168, 168, 170, 171, 171, 171, 171,
				171, 181, 181, 181, 181, 172, 172, 173, 173, 184, 184, 173,
				180, 180, 180, 180, 180, 180, 180, 180, 161, 161, 162, 162,
				160, 190, 190, 190, 190, 190, 190, 190, 190, 195, 195, 195,
				195, 158, 158, 157, 176, 176, 176, 176, 157, 157, 198, 198,
				201, 201, 182, 182, 183, 183, 204, 204, 205, 205, 206, 206,
				189, 189, 189, 189, 208, 208, 210, 210, 208, 208, 192, 192,
				211, 211, 213, 213, 211, 211, 214, 214, 215, 215, 160, 160,
				160, 160, 193, 193, 218, 218, 219, 219, 157, 157,};
	}

	@Override
	public boolean activateCondition() {
		if (!game.isLoggedIn()) {
			return false;
		}

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
		if (done) {
			return 3;
		} else if (interfaces.canContinue()) {
			return 1;
		} else if (phe.length == 0) {
			return 0;
		} else if (inventory.contains(6178)) {
			return 0;
		} else if (phe.length > 0) {
			return 2;
		} else {
			return 0;
		}
	}

	@Override
	public int loop() {
		forester = npcs.getNearest(FORESTER_ID);
		if (forester == null) {
			return -1;
		}
		if (getMyPlayer().getAnimation() != -1) {
			return random(3000, 5000);
		} else if (getMyPlayer().isMoving()) {
			return random(200, 500);
		}
		if (!done) {
			done = searchText(241, "Thank you") || interfaces.getComponent(242, 4).containsText("leave");
		}
		/*
		if (inventory.contains(6179)) {
			phe = new short[]{};
			inventory.getItem(6179).doAction("Drop");
			return random(500, 900);
		}
		*/
		if (unequip && (inventory.getCount(false) != 28)) {
			if (game.getCurrentTab() != Game.TAB_EQUIPMENT) {
				game.openTab(Game.TAB_EQUIPMENT);
				sleep(random(1000, 1500));
				interfaces.get(Equipment.INTERFACE_EQUIPMENT).getComponent(17).doClick();
				return (random(1000, 1500));
			}
			return random(100, 500);
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
				if (!walking.walkTileMM(box.getLocation().randomize(3, 3))) {
					walking.getPath(box.getLocation().randomize(3, 3)).traverse();
				}
				sleep(random(1200, 1400));
			}
			if (box.doAction("Deposit")) {
				return random(800, 1200);
			}
		}
		switch (getState()) {
			case 0: // Talk to forester
				if (calc.tileOnScreen(forester.getLocation())
						&& (calc.distanceTo(forester.getLocation()) <= 5)) {
					forester.doAction("Talk");
				} else if (calc.distanceTo(forester.getLocation()) >= 5) {
					walking.walkTileMM(walking.getClosestTileOnMap(forester
							.getLocation().randomize(3, 3)));
					camera.turnTo(forester.getLocation().randomize(3, 3));
				}
				return random(500, 800);
			case 1: // Talking
				// log("Talking"); //debug REMOVEME
				if (searchText(SEARCH_INTERFACE_ID, " one")) {
					phe = Models.oneTail;
				} else if (searchText(SEARCH_INTERFACE_ID, " two")) {
					phe = Models.twoTail;
				} else if (searchText(SEARCH_INTERFACE_ID, " three")) {
					phe = Models.threeTail;
				}
				if (searchText(SEARCH_INTERFACE_ID, " four")) {
					phe = Models.fourTail;
				}
				if (interfaces.clickContinue()) {
					return random(500, 800);
				}
				return random(200, 500);
			case 2: // Kill pheasant
				if (phe.length == 0) {
					return random(200, 500);
				}
				final RSNPC pheasant = npcs.getNearest(pheasantFilter);
				final RSGroundItem tile = groundItems.getNearest(6178);
				if (tile != null) {
					tiles.doAction(tile.getLocation(), "Take");
					return random(600, 900);
				} else if (pheasant != null) {
					// log("Pheasant ID = " + pheasant.getID());
					if (calc.tileOnScreen(pheasant.getLocation()) && (calc.distanceTo(pheasant.getLocation()) <= 5)) {
						pheasant.doAction("Attack");
						return random(1000, 1500);
					} else if (calc.distanceTo(pheasant.getLocation()) >= 5) {
						walking.walkTileMM(walking.getClosestTileOnMap(pheasant.getLocation().randomize(3, 3)));
						camera.turnTo(pheasant.getLocation().randomize(3, 3));
					}
				} else {
					// log("Pheasant == NULL, sleeping");
					return random(2000, 5000);
				}
			case 3: // Get out
				if (!calc.tileOnScreen(WALK_TO_TILE)) {
					if (calc.tileOnMap(WALK_TO_TILE)) {
						walking.walkTileMM(WALK_TO_TILE);
					} else {
						walking.getPath(forester.getLocation().randomize(5, 5)).traverse();
					}
					return random(900, 1200);
				}
				final RSObject Portal = objects.getNearest(PORTAL_ID);
				if (Portal == null) {
					log.info("Could not find portal.");
					return random(800, 1200);
				}
				if (Portal.doAction("Enter")) {
					return random(800, 1200);
				}
				return random(200, 500);
		}
		return random(1000, 1500);
	}
	public boolean searchText(final int interfac, final String text) {
		final RSInterface talkFace = interfaces.get(interfac);
		if (!talkFace.isValid()) {
			return false;
		}
		for (int i = 0; i < talkFace.getChildCount(); i++) {
			if (talkFace.getComponent(i).containsText(text)) {
				return true;
			}
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