package org.rsbot.script.randoms;

import org.rsbot.script.Random;
import org.rsbot.script.ScriptManifest;
import org.rsbot.script.wrappers.RSInterface;
import org.rsbot.script.wrappers.RSNPC;
import org.rsbot.script.wrappers.RSObject;

/*
 * Updated by TwistedMind (Feb 8, '10) ~ It wasn't exiting...
 * Updated by Iscream (Feb 8, 10) Fixed some loop issues.
 * Updated by Iscream (Feb 09,10)
 * Updated by Iscream (Feb 17,10) Fixed Exiting Issues
 * Updated by Arbiter (Oct 27,10)
 * Updated by NoEffex (Nov 29,10) Updated to interface coords rather than fixed coords
 */

@ScriptManifest(authors = {"Keilgo", "Taha", "Equilibrium", "Twistedmind"}, name = "CapnArnav", version = 0.9)
public class CapnArnav extends Random {

	public int startValue3;
	public int startValue2;
	public int startValue1;

	public int currValue3;
	public int currValue2;
	public int currValue1;

	private boolean thirdColFound = false;
	private boolean secondColFound = false;
	private boolean firstColFound = false;

	private boolean reel1done = false;
	private boolean reel2done = false;
	private boolean reel3done = false;

	private boolean talkedto = false;
	private static final int CAPTAIN_ID = 2308;
	private static final int PORTAL_ID = 11369;


	private boolean done = false;

	static class Ifaces {
		public static final int PARENT = 185;
		public static final int FIRST_SET_UP = 3;
		public static final int FIRST_SET_DOWN = 2;
		public static final int SECOND_SET_UP = 10;
		public static final int SECOND_SET_DOWN = 9;
		public static final int THIRD_SET_UP = 17;
		public static final int THIRD_SET_DOWN = 16;
		public static final int CONFIRM_BUTTON = 28;
	}

	public boolean activateCondition() {
		final RSNPC captain = npcs.getNearest(CAPTAIN_ID);

		if (captain != null) {
			sleep(random(1500, 1600));
			RSObject portal = objects.getNearest(PORTAL_ID);

			return portal != null;

		}

		return false;
	}

	public int loop() {
		if (bank.isDepositOpen() || bank.isOpen())
			bank.close();

		final RSNPC captain = npcs.getNearest(CAPTAIN_ID);

		if (!activateCondition()) {
			return -1;
		}
		if (getMyPlayer().isMoving() || (getMyPlayer().getAnimation() != -1)) {
			return random(1200, 1500);
		}
		if (searchText(241, "yer foot")) {
			final RSObject Chest = objects.getNearest(42337);
			talkedto = true;
			Chest.doAction("Open");
			//try and wait a few (3-6) seconds for the interface to pop up
			for (int i = 0; i < 30 && !interfaces.get(Ifaces.PARENT).isValid(); i++) {
				sleep(100, 200);
			}
			return random(800, 1200);
		}
		if (interfaces.get(228).getComponent(3).isValid() && interfaces.get(228).getComponent(3).getText().contains("Okay")) {
			interfaces.get(228).getComponent(3).doClick();
			for (int i = 0; i < 30 && interfaces.get(228).getComponent(3).isValid()
					&& interfaces.get(228).getComponent(3).getText().contains("Okay"); i++)
				sleep(100, 200);
		}
		if (interfaces.get(241).isValid() && interfaces.get(241).getComponent(0).getAbsoluteY() > 2) {
			if (interfaces.get(241).containsText("haul") || interfaces.get(241).containsText("Just hop")) {
				done = true;
				log("Finished CapnArnav Random ~ Exiting");
				if (interfaces.canContinue()) {
					interfaces.clickContinue();
					return random(600, 700);
				}
				return random(500, 700);
			}
		}
		if (done) {
			final RSObject Portal = objects.getNearest(11369);
			if (Portal == null) {
				log("Can't find portal!");
				camera.turnToObject(Portal);
				return random(800, 1200);
			}
			if (Portal.doAction("Enter")) {
				return random(4500, 4900);
			} else {
				camera.turnToObject(Portal);
				return random(600, 700);
			}
		}
		if (interfaces.get(Ifaces.PARENT).isValid()) {
			log("Setting default position. Coins Coins Coins!");
			for (int i = 0; i < 100 && !firstColFound; i++) {
				for (int j = 0; j < 100 && !thirdColFound; j++) {
					startValue3 = settings.getSetting(809);
					sleep(random(500, 700));
					interfaces.getComponent(Ifaces.PARENT, Ifaces.THIRD_SET_UP).doClick(); // third set up
					sleep(random(800, 1000));
					currValue3 = settings.getSetting(809);
					if (currValue3 < startValue3) {
						thirdColFound = true;
					}
				}

				for (int j = 0; j < 100 && !secondColFound; j++) {
					startValue2 = settings.getSetting(809);
					sleep(random(500, 700));
					interfaces.getComponent(Ifaces.PARENT, Ifaces.SECOND_SET_UP).doClick(); // second set up
					sleep(random(800, 1000));
					currValue2 = settings.getSetting(809);
					if (currValue2 < startValue2) {
						secondColFound = true;
					}
				}

				for (int j = 0; j < 100 && !firstColFound; j++) {
					startValue1 = settings.getSetting(809);
					sleep(random(500, 700));
					interfaces.getComponent(Ifaces.PARENT, Ifaces.FIRST_SET_UP).doClick(); // first set up
					sleep(random(800, 1000));
					currValue1 = settings.getSetting(809);
					if (currValue1 < startValue1) {
						firstColFound = true;
					}
				}
			}
		}

		if (interfaces.get(Ifaces.PARENT).isValid()) {
			if (searchText(Ifaces.PARENT, "Bar")) {
				for (int i = 0; i < 100 && !reel1done; i++) {
					interfaces.getComponent(Ifaces.PARENT, Ifaces.FIRST_SET_UP).doClick();
					sleep(random(800, 1000));
					interfaces.getComponent(Ifaces.PARENT, Ifaces.FIRST_SET_UP).doClick();
					sleep(random(800, 1000));
					log("Reel 1 Bar Found!");
					reel1done = true;
				}

				for (int i = 0; i < 100 && !reel2done; i++) {
					interfaces.getComponent(Ifaces.PARENT, Ifaces.SECOND_SET_UP).doClick();
					sleep(random(800, 1000));
					interfaces.getComponent(Ifaces.PARENT, Ifaces.SECOND_SET_UP).doClick();
					sleep(random(800, 1000));
					log("Reel 2 Bar Found!");
					reel2done = true;
				}

				for (int i = 0; i < 100 && !reel3done; i++) {
					interfaces.getComponent(Ifaces.PARENT, Ifaces.THIRD_SET_UP).doClick();
					sleep(random(800, 1000));
					interfaces.getComponent(Ifaces.PARENT, Ifaces.THIRD_SET_UP).doClick();
					sleep(random(800, 1000));
					log("Reel 3 Bar Found!");
					reel3done = true;
				}

				if (interfaces.get(Ifaces.PARENT).isValid()) {
					interfaces.get(Ifaces.PARENT).getComponent(Ifaces.CONFIRM_BUTTON).doClick();
					sleep(random(700, 1000));
				}
			}
		}

		if (interfaces.get(Ifaces.PARENT).isValid()) {
			if (searchText(Ifaces.PARENT, "Coins")) {
				if (!reel1done) {
					log("Reel 1 Coins Found!");
					reel1done = true;
				}

				if (!reel2done) {
					log("Reel 2 Coins Found!");
					reel2done = true;
				}

				if (!reel3done) {
					log("Reel 3 Coins Found!");
					reel3done = true;
				}

				if (interfaces.get(Ifaces.PARENT).isValid()) {
					interfaces.get(Ifaces.PARENT).getComponent(Ifaces.CONFIRM_BUTTON).doClick();
					sleep(random(700, 1000));
				}
			}
		}

		if (interfaces.get(Ifaces.PARENT).isValid()) {
			if (searchText(Ifaces.PARENT, "Bowl")) {
				for (int i = 0; i < 100 && !reel1done; i++) {
					interfaces.getComponent(Ifaces.PARENT, Ifaces.FIRST_SET_UP).doClick();
					sleep(random(800, 1000));
					log("Reel 1 Bowl Found!");
					reel1done = true;
				}

				for (int i = 0; i < 100 && !reel2done; i++) {
					interfaces.getComponent(Ifaces.PARENT, Ifaces.SECOND_SET_UP).doClick();
					sleep(random(800, 1000));
					log("Reel 2 Bowl Found!");
					reel2done = true;
				}

				for (int i = 0; i < 100 && !reel3done; i++) {
					interfaces.getComponent(Ifaces.PARENT, Ifaces.THIRD_SET_UP).doClick();
					sleep(random(800, 1000));
					log("Reel 3 Bowl Found!");
					reel3done = true;
				}

				if (interfaces.get(Ifaces.PARENT).isValid()) {
					interfaces.get(Ifaces.PARENT).getComponent(Ifaces.CONFIRM_BUTTON).doClick(); //click confirm
					sleep(random(700, 1000));
				}
			}
		}

		if (interfaces.get(Ifaces.PARENT).isValid()) { //scroll down
			if (searchText(Ifaces.PARENT, "Ring")) {
				for (int i = 0; i < 100 && !reel1done; i++) { //first set
					interfaces.getComponent(Ifaces.PARENT, Ifaces.FIRST_SET_DOWN).doClick();
					sleep(random(800, 1000));
					log("Reel 1 Ring Found!");
					reel1done = true;
				}

				for (int i = 0; i < 100 && !reel2done; i++) {
					interfaces.getComponent(Ifaces.PARENT, Ifaces.SECOND_SET_DOWN).doClick();
					sleep(random(800, 1000));
					log("Reel 2 Ring Found!");
					reel2done = true;
				}

				for (int i = 0; i < 100 && !reel3done; i++) {
					interfaces.getComponent(Ifaces.PARENT, Ifaces.THIRD_SET_DOWN).doClick();
					sleep(random(800, 1000));
					log("Reel 3 Ring Found!");
					reel3done = true;
				}

				if (interfaces.get(Ifaces.PARENT).isValid()) {
					interfaces.get(Ifaces.PARENT).getComponent(Ifaces.CONFIRM_BUTTON).doClick();
					sleep(random(700, 1000));
				}
			}
		}

		if (interfaces.get(228).isValid() && interfaces.get(228).getComponent(0).getAbsoluteY() > 2) {
			// final int x = random(220, 310), y = random(427, 437);
			// mouse.click(x, y, true);
			interfaces.clickContinue();
		}
		if (!myClickContinue() && !talkedto && !interfaces.canContinue()) {
			captain.doAction("Talk-to");
			return random(500, 700);
		}
		if (interfaces.canContinue()) {
			interfaces.clickContinue();
			return random(1000, 1200);
		}
		if (!done && talkedto && !interfaces.get(Ifaces.PARENT).isValid() && !interfaces.get(241).isValid() && !interfaces.canContinue() && !getMyPlayer().isInteractingWithLocalPlayer()) {
			captain.doAction("Talk-to");
			return random(500, 700);
		}
		return random(1000, 1500);
	}

	public boolean myClickContinue() {
		sleep(random(800, 1000));
		if ((interfaces.getComponent(243, 7).isValid() && interfaces.getComponent(243, 7).getAbsoluteY() < 5) || (interfaces.getComponent(241, 5).isValid() && interfaces.getComponent(241, 5).getAbsoluteY() < 5) || (interfaces.getComponent(242, 6).isValid() && interfaces.getComponent(242, 6).getAbsoluteY() < 5) || (interfaces.getComponent(244, 8).isValid() && interfaces.getComponent(244, 8).getAbsoluteY() < 5) || (interfaces.getComponent(64, 5).isValid() && interfaces.getComponent(64, 5).getAbsoluteY() < 5) || (interfaces.getComponent(236, 1).isValid() && interfaces.getComponent(236, 1).getAbsoluteY() < 5) || (interfaces.getComponent(230, 4).isValid() && interfaces.getComponent(230, 4).getAbsoluteY() < 5) || (interfaces.getComponent(228, 3).isValid() && interfaces.getComponent(228, 3).getAbsoluteY() < 5))
			return false;
		return interfaces.getComponent(243, 7).doClick() || interfaces.getComponent(241, 5).doClick() || interfaces.getComponent(242, 6).doClick() || interfaces.getComponent(244, 8).doClick() || interfaces.getComponent(64, 5).doClick() || interfaces.getComponent(236, 1).doClick() || interfaces.getComponent(230, 4).doClick() || interfaces.getComponent(228, 3).doClick();
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
}
