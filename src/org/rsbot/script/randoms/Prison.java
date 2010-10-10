package org.rsbot.script.randoms;

import org.rsbot.script.Random;
import org.rsbot.script.ScriptManifest;
import org.rsbot.script.methods.Bank;
import org.rsbot.script.wrappers.*;
//228 child is 2 or 3

/*
 * Written by Iscream(Feb 4, 2010)
 * Updated by Iscream(Feb 17, 2010)
 * Updated by zzSleepzz(Mar 1, 2010 to remove false positives)
 * Updated by Iscream(Apr 15, 2010)
 * Updated by Iscream(Apr 23, 2010)
 */
@ScriptManifest(authors = {"Iscream"}, name = "PrisonPete", version = 1.5)
public class Prison extends Random {

    private static final int PRISON_MATE = 3118, LEVER_ID = 10817, DOOR_KEY = 6966;
    private int unlocked, state = 0;
    private RSNPC balloonToPop;
    private RSNPC pete;
    private boolean talkedtopete = false;
    private boolean key = false;
    private boolean lucky = false;

    @Override
    public boolean activateCondition() {
		if (game.isLoggedIn()) {
			pete = npcs.getNearest("Prison Pete");
			if (pete != null) {
				return objects.getNearest(LEVER_ID) != null;
			}
		}
        return false;
    }

    @Override
    public int loop() {
        if (npcs.getNearest("Prison Pete") == null) {
            return -1;
        }
        if (!talkedtopete) {
            camera.setAltitude(true);
            if ((camera.getAngle() < 175) || (camera.getAngle() > 185)) {
                camera.setAngle(random(175, 185));
                return random(500, 750);
            }
        }
        switch (state) {
            case 0:
                pete = npcs.getNearest("Prison Pete");
                if (interfaceContains("Lucky you!")) {
                    if (IScanContinue()) {
                        interfaces.clickContinue();
                    }
                    state = 4;
                    lucky = true;
                    return random(500, 600);
                }
                if (interfaceContains("should leave")) {
                    if (IScanContinue()) {
                        interfaces.clickContinue();
                    }
                    state = 4;
                    unlocked = 10;
                    return random(500, 600);
                }
                if ((inventory.getCount(false) == 28)
                        && !inventory.containsAll(DOOR_KEY)) {
                    log("Not enough space for this random. Depositing an Item");
                    final RSObject depo = objects.getNearest(32924);
                    if (depo != null) {
                        if (!calc.tileOnScreen(depo.getLocation())) {
                            if (!walking.walkTileMM(walking.randomizeTile(depo.getLocation(), 3, 3))) {
                                walking.walkPathMM(walking.findPath(walking.randomizeTile(depo.getLocation(), 3, 3)));
                                return random(500, 700);
                            }
                            return random(1000, 1500);
                        }
                        camera.turnToObject(depo, 20);
                        if (depo.doAction("Deposit")) {
                            sleep(random(1800, 2000));
                            if (getMyPlayer().isMoving()) {
                                sleep(random(200, 500));
                            }
                            if (interfaces.get(Bank.INTERFACE_DEPOSIT_BOX).isValid()) {
                                sleep(random(700, 1200));
                                interfaces.get(11).getComponent(17).getComponent(random(16, 17)).doAction("Dep");
                                sleep(random(700, 1200));
                                interfaces.getComponent(11, 15).doClick();
                            }
                            return random(400, 500);
                        }
                        return random(500, 800);
                    }
                    return random(500, 600);
                }

                if (getMyPlayer().isMoving()) {
                    return random(250, 500);
                }
                if (interfaceContains("minute")) {
                    talkedtopete = true;
                    if (IScanContinue()) {
                        interfaces.clickContinue();
                        return random(500, 600);
                    }
                    return random(500, 600);
                }

                if (interfaces.get(228).isValid()
                        && interfaces.get(228).containsText("How do")) {
                    interfaces.get(228).getComponent(3).doClick();
                    return random(500, 600);
                }
                if (IScanContinue()) {
                    interfaces.clickContinue();
                    return random(1000, 1200);
                }
                if (!talkedtopete && pete != null
                        && !(interfaces.get(228).isValid()) && !IScanContinue()) {
                    if (!calc.tileOnScreen(pete.getLocation())) {
                        walking.walkTileMM(pete.getLocation());
                        return random(1000, 1400);
                    }
                    if (pete.doAction("talk")) {
                        return random(1500, 1600);
                    } else {
                        camera.turnToTile(pete.getLocation());
                        return random(500, 600);
                    }
                }
                if (unlocked == 3) {
                    state = 4;
                    return random(250, 500);
                }
                if (unlocked <= 2 && talkedtopete) {
                    state = 1;
                    return random(500, 600);
                }
                return random(350, 400);

            case 1:
                // Figures out the balloon
            	if (interfaceContains("Lucky you!")) {
                    if (IScanContinue()) {
                        interfaces.clickContinue();
                    }
                    state = 4;
                    lucky = true;
                    return random(500, 600);
                }
                if (interfaceContains("should leave")) {
                    if (IScanContinue()) {
                        interfaces.clickContinue();
                    }
                    state = 4;
                    unlocked = 10;
                    return random(500, 600);
                }
            	if (interfaces.get(273).getComponent(3).isValid()) {
            		if (atLever()) {
            			if (balloonToPop != null && interfaces.get(273).getComponent(4).doAction("Close")) {
                            state = 2;
                            return random(800, 900);
                        }
                        return random(500, 700);
                    }
            	}
                final RSObject lever = objects.getNearest(LEVER_ID);
                if ((lever != null) && talkedtopete) {
                    if (!calc.tileOnScreen(lever.getLocation())) {
                        walking.walkTileMM(lever.getLocation());
                        return random(1000, 1200);
                    }
                    if (!getMyPlayer().isMoving()
                            && calc.tileOnScreen(lever.getLocation())) {
                        //if (tiles.doAction(lever.getLocation(), 0.5, 0.5, 170, "Pull")) {
                    	if (lever.doAction("Pull")) {
                            sleep(random(1400, 1600));
                            if (atLever()) {
                                if (balloonToPop != null && interfaces.get(273).getComponent(4).doAction("Close")) {
                                    state = 2;
                                    return random(800, 900);
                                }
                                return random(500, 700);
                            }
                            return random(500, 600);
                        } else {
                            camera.turnToTile(lever.getLocation());
                            return random(500, 600);
                        }
                    }
                }
                if (!talkedtopete) {
                    state = 0;
                    return random(500, 600);
                }
                return random(500, 600);
            case 2:
                // Finds animal and pops it
            	if (interfaceContains("Lucky you!")) {
                    if (IScanContinue()) {
                        interfaces.clickContinue();
                    }
                    state = 4;
                    lucky = true;
                    return random(500, 600);
                }
                if (interfaceContains("should leave")) {
                    if (IScanContinue()) {
                        interfaces.clickContinue();
                    }
                    state = 4;
                    unlocked = 10;
                    return random(500, 600);
                }
                if (getMyPlayer().isMoving()) {
                    return random(250, 500);
                }
                if (balloonToPop == null && unlocked <= 2) {
                    state = 1;
                    return random(500, 700);
                }
                if (unlocked == 3) {
                    state = 4;
                }

                if (!inventory.containsAll(DOOR_KEY)) {
                    if (calc.tileOnScreen(balloonToPop.getLocation())) {
                        balloonToPop.doAction("Pop");
                        return random(1200, 1400);
                    } else {
                        if (!getMyPlayer().isMoving()) {
                            walking.walkTileMM(walking.randomizeTile(balloonToPop.getLocation(), 2,
                                    2));
                            return random(500, 750);
                        }
                        return random(500, 750);
                    }
                }
                if (inventory.containsAll(DOOR_KEY)) {
                    key = false;
                    state = 3;
                    return random(500, 700);
                }
                return random(350, 400);

            case 3:
                // Goes to pete
                pete = npcs.getNearest("Prison Pete");
                if (getMyPlayer().isMoving()) {
                    return random(250, 500);
                }
                if (interfaceContains("Lucky you!")) {
                    if (IScanContinue()) {
                        interfaces.clickContinue();
                    }
                    state = 4;
                    lucky = true;
                    return random(500, 600);
                }
                if (interfaceContains("should leave")) {
                    if (IScanContinue()) {
                        interfaces.clickContinue();
                    }
                    state = 4;
                    unlocked = 10;
                    return random(500, 600);
                }
                if (interfaceContains("you got all the keys")) {
                    key = true;
                    unlocked = 5;
                    state = 4;
                    balloonToPop = null;
                    if (IScanContinue()) {
                        interfaces.clickContinue();
                        return random(500, 600);
                    }
                    return random(250, 500);
                }
                if (interfaceContains("Hooray")) {
                    key = true;
                    if (IScanContinue()) {
                        interfaces.clickContinue();
                        return random(500, 600);
                    }
                }
                if (IScanContinue()) {
                    interfaces.clickContinue();
                    return random(500, 600);
                }
                if (pete != null && !calc.tileOnScreen(pete.getLocation()) && !(interfaces.get(243).isValid())) {
                    walking.walkTileMM(pete.getLocation());
                    return random(400, 600);
                }
                if (!inventory.containsAll(DOOR_KEY)
                        && (npcs.getNearest(PRISON_MATE) != null)
                        && (unlocked <= 2) && key) {
                    unlocked++;
                    state = 0;
                    balloonToPop = null;
                    return random(350, 400);
                }

                if (inventory.containsAll(DOOR_KEY) && !getMyPlayer().isMoving()) {
                    inventory.getItem(DOOR_KEY).doAction("Return");
                    return random(1000, 2000);
                }
                if (!inventory.containsAll(DOOR_KEY)
                        && (npcs.getNearest(PRISON_MATE) != null)
                        && (unlocked <= 2) && !key) {
                    state = 0;
                    balloonToPop = null;
                    return random(350, 400);
                }

                return random(350, 400);
            case 4:
                // exits
                RSTile doorTile = new RSTile(2086, 4459);
                if (unlocked <= 2 && !lucky) {
                    state = 0;
                    return random(500, 600);
                }
                if (!calc.tileOnScreen(doorTile)) {
                    walking.walkTileMM(doorTile);
                    return random(400, 500);
                }
                if (calc.tileOnScreen(doorTile)) {
                	RSObject gate = objects.getNearest(11177,11178);
                	if (gate != null)
                		gate.doAction("Open");
                    //tiles.doAction(new RSTile(2085, 4459), 1, 0, 30, "Open");
                    return random(500, 600);
                }
                return random(200, 400);
        }
        return random(200, 400);
    }

    @Override
    public void onFinish() {
    	if (lucky) {
    		log.info("Failed to complete Prison Pete. Stopping now.");
    		sleep(5000,10000);
    		stopScript(false);
    	}
    }

    public int setItemIDs(final int b2p) {
        // sets the proper balloon id
        switch (b2p) {
            case 10749:
                return 3119;
            case 10750:
                return 3120;
            case 10751:
                return 3121;
            case 10752:
                return 3122;
        }
        return 0;
    }

    public boolean IScanContinue() {
        return ISgetContinueChildInterface() != null;
    }

    public RSComponent ISgetContinueChildInterface() {
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

    public int ISgetIface(final String a) {
        final RSInterface[] valid = interfaces.getAll();
        for (final RSInterface iface : valid) {
            if (iface.containsText(a)) {
                log("Interface Found");
                return iface.getIndex();
            }
        }
        return 0;
    }
    
    public boolean interfaceContains(String s) {
    	RSInterface[] all = interfaces.getAll();
    	for (int a = 0; a < all.length; a++) {
	    	RSInterface iface = all[a];
	    	if (iface != null) {
		    	int count = iface.getComponents().length;
		    	for (int i = 0; i < count; i++) {
		    		if (iface.getComponent(i).getText() != null && iface.getComponent(i).getText().contains(s))
		    		{
		    			//System.out.println("Return true");
		    			return true;
		    		}
		    	}
	    	}
    	}
    	return false;
    }

    public boolean atLever() {
        if (interfaces.get(273).getComponent(3).isValid()) {
            balloonToPop = npcs.getNearest(setItemIDs(interfaces.get(273).getComponent(3).getModelID()));
            if (balloonToPop != null) {
                return true;
            }
        }
        return false;
    }

}