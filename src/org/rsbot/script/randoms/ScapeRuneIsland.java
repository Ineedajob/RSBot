package org.rsbot.script.randoms;

import org.rsbot.script.Random;
import org.rsbot.script.ScriptManifest;
import org.rsbot.script.wrappers.*;

/*
 * Updated by Arbiter (Oct 19, 2010)
 */
@ScriptManifest(authors = "Arbiter", name = "ScapeRuneIsland", version = 2.0)
public class ScapeRuneIsland extends Random {

    public RSObject statue1, statue2, statue3, statue4, direction;
    public int[] statueid = {8992, 8993, 8990, 8991};
    public RSNPC servant;
    public boolean finished;
    public boolean fishing;
    public RSTile center = new RSTile(3421,4777);

    public boolean activateCondition() {
        servant = npcs.getNearest(2481);
		return servant != null || objects.getNearest(8986) != null;
	}

    public int loop() {
        if (!activateCondition()) {
            return -1;
        }
        if (statue1 == null) {
            statue1 = objects.getNearest(statueid[0]);
            statue2 = objects.getNearest(statueid[1]);
            statue3 = objects.getNearest(statueid[2]);
            statue4 = objects.getNearest(statueid[3]);
            log("Setting statues");
        }
        if (getMyPlayer().isMoving() || getMyPlayer().getAnimation() == 620) {
            return random(550, 700);
        }
        if (interfaces.get(241).getComponent(4).getText().contains("catnap"))
            finished = true;
        if (clickContinue())
            return random(500,1000);
        if (finished) {
            RSObject portal = objects.getNearest(8987);
            if (portal != null) {
                if (!calc.tileOnScreen(portal.getLocation())) {
                    walking.walkTileMM(walking.getClosestTileOnMap(portal.getLocation()));
                    return random(500,1000);
                }
                else {
                    if (portal.doAction("Enter"))
                        return random(3000,5000);
                    return random(500,1000);
                }
            }
            else {
                walking.walkTileMM(walking.getClosestTileOnMap(center));
            }
        }
        if (bank.isDepositOpen() && bank.getBoxCount() - bank.getBoxCount(6209, 6202, 6200) >= 27) {
            RSComponent randomItem = interfaces.get(11).getComponent(17).getComponent(random(16,26));
            int randomID = randomItem.getComponentID();
            if (randomID < 0)
                return random(50,100);
            log("Item with ID " + randomID + " was deposited.");
            if (interfaces.get(11).getComponent(17).getComponent(random(16,26)).doAction("Dep"))
                return random(500, 1000);
            return random(50,100);
        }
        if (bank.isDepositOpen() && bank.getBoxCount() - bank.getBoxCount(6209, 6202, 6200) < 27) {
            bank.close();
            return random(500,1000);
        }
        if (inventory.getCountExcept(6209, 6202, 6200) >= 27) {
            RSObject box = objects.getNearest(32930);
            if (!calc.tileOnScreen(box.getLocation())) {
                walking.walkTileMM(walking.getClosestTileOnMap(box.getLocation()));
                return random(1000,2000);
            }
            else {
                log("Depositing item(s) to make room.");
                box.doAction("Deposit");
                return random(500,1000);
            }
        }
        if (inventory.getCount(6202) > 0) {
            final RSObject pot = objects.getNearest(8985);
            if (pot != null) {
                if (!calc.tileOnScreen(pot.getLocation())) {
                    walking.walkTileMM(walking.getClosestTileOnMap(pot.getLocation()));
                    return random(400, 800);
                }
                inventory.getItem(6202).doAction("Use");
                sleep(random(800, 1000));
                pot.doAction("Use");
                return random(2000, 2400);
            }
            else {
                walking.walkTileMM(walking.getClosestTileOnMap(center));
            }
        }
        if (fishing && inventory.getCount(6209) == 0) {
            final RSGroundItem net = groundItems.getNearest(6209);
            if (net != null) {
                if (!calc.tileOnScreen(net.getLocation())) {
                    walking.walkTileMM(walking.getClosestTileOnMap(net.getLocation()));
                    return random(800, 1000);
                } else {
                    tiles.doAction(net.getLocation(), "Take");
                    return random(800, 1000);
                }
            }
            else {
                walking.walkTileMM(walking.getClosestTileOnMap(center));
            }
        }

        if (interfaces.get(246).getComponent(5).containsText("contains")
                && settings.getSetting(334) == 1 && direction == null) {
            sleep(2000);
            if (calc.tileOnScreen(statue1.getLocation())) {
                direction = statue1;
                fishing = true;
            }
            if (calc.tileOnScreen(statue2.getLocation())) {
                direction = statue2;
                fishing = true;
            }
            if (calc.tileOnScreen(statue3.getLocation())) {
                direction = statue3;
                fishing = true;
            }
            if (calc.tileOnScreen(statue4.getLocation())) {
                direction = statue4;
                fishing = true;
            }
            log("Checking direction");
            return random(2000,3000);
        }

        if (direction != null && inventory.getCount(6200) < 1) {
            sleep(random(1000,1200));
            if (!calc.tileOnScreen(direction.getLocation())) {
                walking.walkTileMM(walking.getClosestTileOnMap(direction.getLocation()));
                return random(400, 600);
            }
            final RSObject spot = objects.getNearest(8986);
            if (spot != null) {
                if (!calc.tileOnScreen(spot.getLocation())) {
                    camera.turnToTile(spot.getLocation());
                }
                tiles.doAction(spot.getLocation(), "Net");
                return random(1000, 1200);
            }
            else {
                walking.walkTileMM(walking.getClosestTileOnMap(center));
            }
        }

        if (inventory.getCount(6200) > 0 && !clickContinue()) {
            final RSNPC cat = npcs.getNearest(2479);
            if (cat != null) {
                if (!calc.tileOnScreen(cat.getLocation())) {
                    walking.walkTileMM(walking.getClosestTileOnMap(cat.getLocation()));
                }
                inventory.getItem(6200).doAction("Use");
                sleep(random(500, 1000));
                cat.doAction("Use Raw fish-like thing -> Evil bob");
            }
            else {
                walking.walkTileMM(walking.getClosestTileOnMap(center));
            }
            return random(1900, 2200);
        }
        if (servant != null && direction == null && settings.getSetting(344) == 0
                && !clickContinue()) {
            if (!calc.tileOnScreen(servant.getLocation())) {
                walking.walkTileMM(walking.getClosestTileOnMap(servant.getLocation()));
                return 700;
            }
            servant.doAction("Talk-to");
            return random(1000, 2000);
        }
        if (servant == null) {
            servant = npcs.getNearest(2481);
            if (servant == null) {
                walking.walkTileMM(walking.getClosestTileOnMap(center));
                return random(1000,2000);
            }
            return random(50,100);
        }
        log("Setting 344: " + settings.getSetting(344));
        return random(800, 1200);
    }

    public boolean clickContinue() {
        RSInterface[] valid = interfaces.getAll();
        for (RSInterface iface : valid) {
            if (iface.getIndex() != 137) {
                int len = iface.getChildCount();
                for (int i = 0; i < len; i++) {
                    RSComponent child = iface.getComponent(i);
                    if (child.containsText("Click here to continue") && child.getAbsoluteY() > 5) {
                        return child.doAction("Continue");
					}
                }
            }
        }
        return false;
    }
}