package org.rsbot.script.randoms;

import org.rsbot.event.events.ServerMessageEvent;
import org.rsbot.event.listeners.ServerMessageListener;
import org.rsbot.script.Random;
import org.rsbot.script.ScriptManifest;
import org.rsbot.script.methods.Equipment;
import org.rsbot.script.methods.Game;
import org.rsbot.script.wrappers.*;

/**
 * @version 2.3 - 15/4/10 Fix by Iscream
 */
@ScriptManifest(authors = {"Pwnaz0r", "Taha", "zqqou", "Zach"}, name = "FreakyForester", version = 2.3)
public class FreakyForester extends Random implements ServerMessageListener {

    private RSNPC forester;
    private static final int FORESTER_ID = 2458;
    private static final int SEARCH_INTERFACE_ID = 242;
    private static final int PORTAL_ID = 8972;
    private static final RSTile WALK_TO_TILE = new RSTile(2610, 4775);
    private boolean unequip = false;
    int phe = -1;

    boolean done = false;

    @Override
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
        else if (IScanContinue())
            return 1;
        else if (phe == -1)
            return 0;
        else if (inventory.containsAll(6178))
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

        if (inventory.containsAll(6179)) {
            phe = -1;
            inventory.getItem(6179).doAction("rop");
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

        if ((inventory.getCount(false) == 28) && !inventory.containsAll(6178)) {
            final RSObject Deposit = objects.getNearest(32931);
            if ((!calc.tileOnScreen(Deposit.getLocation()) && ((calc.distanceTo(walking.getDestination())) < 8)) || (calc.distanceTo(walking.getDestination()) > 40)) {
                if (!walking.walkTileMM(walking.randomizeTile(Deposit.getLocation(), 3, 3))) {
                    walking.walkPathMM(walking.findPath(walking.randomizeTile(Deposit.getLocation(), 3, 3)));
                }
                sleep(random(1200, 1400));
            }
            if (Deposit.doAction("Deposit")) {
                sleep(random(1800, 2000));
                mouse.click(410 + random(2, 4), 235 + random(2, 1), false);
                menu.doAction("Deposit-1");
                sleep(random(1200, 1400));
                mouse.click(435 + random(2, 4), 40 + random(2, 1), true);
                return random(800, 1200);
            }
        }

        switch (getState()) {
            case 0: // Talk to forester
                if (calc.tileOnScreen(forester.getLocation()) && (calc.distanceTo(forester.getLocation()) <= 5)) {
                    forester.doAction("Talk");
                } else if (calc.distanceTo(forester.getLocation()) >= 5) {
                    walking.walkTileMM(walking.getClosestTileOnMap(walking.randomizeTile(forester.getLocation(), 3, 3)));
                    camera.turnToTile(walking.randomizeTile(forester.getLocation(), 3, 3));
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
                final RSNPC Pheasant = npcs.getNearestFree(phe);
                final RSGroundItem tile = groundItems.get(6178);
                if (tile != null) {
                    tiles.doAction(tile.getLocation(), "Take");
                    return random(600, 900);
                } else if (Pheasant != null) {
                    if (calc.tileOnScreen(Pheasant.getLocation()) && (calc.distanceTo(Pheasant.getLocation()) <= 5)) {
                        Pheasant.doAction("ttack");
                        return random(1000, 1500);
                    } else if (calc.distanceTo(Pheasant.getLocation()) >= 5) {
                        walking.walkTileMM(walking.getClosestTileOnMap(walking.randomizeTile(Pheasant.getLocation(), 3, 3)));
                        camera.turnToTile(walking.randomizeTile(Pheasant.getLocation(), 3, 3));
                    }
                } else
                    return random(2000, 5000);
            case 3: // Get out
                if (!calc.tileOnScreen(WALK_TO_TILE)) {
                    if (calc.tileOnMap(WALK_TO_TILE)) {
                        walking.walkTileMM(WALK_TO_TILE);
                    } else {
                        walking.walkPathMM(walking.findPath(walking.randomizeTile(forester.getLocation(), 5, 5)));
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

    public void serverMessageRecieved(final ServerMessageEvent e) {
        final String serverString = e.getMessage();
        if (serverString.contains("no ammo left")) {
            unequip = true;
        }

    }

    private boolean IScanContinue() {
        return ISgetContinueChildInterface() != null;
    }

    private RSComponent ISgetContinueChildInterface() {
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