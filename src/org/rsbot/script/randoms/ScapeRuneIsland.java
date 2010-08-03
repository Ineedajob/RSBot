package org.rsbot.script.randoms;

import org.rsbot.script.Random;
import org.rsbot.script.ScriptManifest;
import org.rsbot.script.wrappers.*;

/*
 * Updated by Iscream(Feb 17, 2010)
 * Updated by Iscream(Mar 01, 2010)
 * Updated by Iscream(Apr 15, 2010)
 * Updated by Iscream(May 11, 2010)
 */
@ScriptManifest(authors = {"endoskeleton", "Johnnei"}, name = "ScapeRuneIsland", version = 1.27)
public class ScapeRuneIsland extends Random {

    public RSObject statue1, statue2, statue3, statue4, direction;
    public int[] statueid = {8992, 8993, 8990, 8991};
    public RSNPC servant;
    public boolean finished;
    RSObject a;

    public boolean activateCondition() {
        return (servant = npcs.getNearest(2481)) != null;
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
        if (canContinueIS()) {
            if (interfaces.getComponent(241, 4).containsText("catnap")) {
                finished = true;
            }
            interfaces.clickContinue();
            return random(1000, 1200);
        }
        if (finished) {
            final RSObject exit = objects.getNearest(8987);
            if (exit != null) {
                if (calc.distanceTo(exit.getLocation()) > 4) {
                    walking.walkTo(exit.getLocation());
                    return random(400, 800);
                }
                if (!calc.tileOnScreen(exit.getLocation())) {
                    walking.walkTo(exit.getLocation());
                    return random(400, 800);
                }
                tiles.doAction(exit.getLocation(), "Enter");
                return random(5500, 6000);
            }
        }
        if (inventory.getCount(6202) > 0) {
            final RSObject pot = objects.getNearest(8985);
            if (pot != null) {
                if (calc.distanceTo(pot.getLocation()) > 4) {
                    walking.walkTo(pot.getLocation());
                    return random(400, 800);
                }
                inventory.getItem(6202).doAction("Use");
                sleep(random(800, 1000));
                tiles.doAction(pot.getLocation(), "Use");
                return random(2000, 2400);
            }
        }
        if (inventory.getCountExcept(6209, 6202, 6200) >= 27) {
            log("Not enough space for this random. Depositing 2 Items");
            final RSObject depo = objects.getNearest(32930);
            if (!calc.tileOnScreen(depo.getLocation())) {
                if (!walking.walkTileMM(walking.randomizeTile(depo.getLocation(), 3, 3))) {
                    walking.walkTo(walking.randomizeTile(depo.getLocation(), 3, 3));
                }
                sleep(random(1000, 1500));
            }
            if (!interfaces.get(11).isValid()) {
                depo.doAction("Deposit");
                sleep(random(2000, 2500));
            }
            if (interfaces.get(11).isValid()) {
                interfaces.get(11).getComponent(17).getComponent(25).doAction("Dep");
                sleep(random(1000, 1200));
                interfaces.get(11).getComponent(17).getComponent(26).doAction("Dep");
                sleep(random(1000, 1500));
                interfaces.getComponent(11, 15).doClick();
                sleep(random(1000, 1500));
            }
            return random(400, 1200);
        }
        if (inventory.getCount(6209) == 0) {
            final RSGroundItem net = groundItems.get(6209);
            if (net != null) {
                if (calc.distanceTo(net.getLocation()) > 5) {
                    walking.walkTo(net.getLocation());
                    return random(800, 1000);
                } else {
                    tiles.doAction(net.getLocation(), "Take");
                    return random(800, 1000);
                }
            }
        }

        if (interfaces.getComponent(246, 5).containsText("contains")
                && settings.getSetting(334) == 1 && direction == null) {
            sleep(2000);
            if (calc.tileOnScreen(statue1.getLocation())) {
                direction = statue1;
            }
            if (calc.tileOnScreen(statue2.getLocation())) {
                direction = statue2;
            }
            if (calc.tileOnScreen(statue3.getLocation())) {
                direction = statue3;
            }
            if (calc.tileOnScreen(statue4.getLocation())) {
                direction = statue4;
            }
            log("Checking direction");
            return 3000;
        }

        if (direction != null && inventory.getCount(6200) < 1) {
            // 6206, 6202
            // (want), 6200
            // (cooked) anim
            // 620
            sleep(1200);
            if (calc.distanceTo(direction.getLocation()) > 4) {
                walking.walkTo(direction.getLocation());
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
        }

        if (inventory.getCount(6200) > 0 && !canContinueIS()) {
            final RSNPC cat = npcs.getNearest(2479);
            if (cat != null) {
                if (!calc.tileOnScreen(cat.getLocation())) {
                    camera.turnToTile(cat.getLocation());
                    walking.walkTo(cat.getLocation());
                }
                inventory.getItem(6200).doAction("Use");
                sleep(random(500, 1000));
                tiles.doAction(cat.getLocation(), "Use Raw fish-like thing -> Evil bob");
            }
            return random(1900, 2200);
        }
        if (servant != null && direction == null && settings.getSetting(344) == 0
                && !canContinueIS()) {
            if (!calc.tileOnScreen(servant.getLocation())) {
                walking.walkTo(servant.getLocation());
                return 700;
            }
            servant.doAction("Talk-to");
            return random(2000, 2100);
        }
        log("Setting 344: " + settings.getSetting(344));
        return random(800, 1200);
    }

    private boolean canContinueIS() {
        return getContinueChildInterfaceIS() != null;
    }

    private RSComponent getContinueChildInterfaceIS() {
        RSInterface[] valid = interfaces.getAll();
        for (RSInterface iface : valid) {
            if (iface.getIndex() != 137) {
                int len = iface.getChildCount();
                for (int i = 0; i < len; i++) {
                    RSComponent child = iface.getComponent(i);
                    if (child.containsText("Click here to continue"))
                        return child;
                }
            }
        }
        return null;
    }
}