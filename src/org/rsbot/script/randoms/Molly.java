package org.rsbot.script.randoms;

import org.rsbot.script.Random;
import org.rsbot.script.ScriptManifest;
import org.rsbot.script.wrappers.*;

import java.util.ArrayList;

//Checked 3/7/10

/**
 * Updated by aman (Nov 14, 09) Updated by Equilibrium (Dec 13, 09) Updated by
 * Fred (Dec 25, 09) Updated by Iscream (Jan 31, 10) Updated by Iscream (Feb 20,
 * 10) Updated by Jacmob (Oct 24, 10) Updated by Arbiter (Nov 11, 10)
 *
 * @author PwnZ
 */
@ScriptManifest(authors = {"PwnZ"}, name = "Molly", version = 1.9)
public class Molly extends Random {

	static final int CLAW_ID = 14976;
	static final int CONTROL_PANEL_ID = 14978;
	static final int DOOR_ID = 14982;
	static final int MOLLY_CHATBOX_INTERFACEGROUP = 228;
	static final int MOLLY_CHATBOX_NOTHANKS = 3;
	static final int MOLLY_CHATBOX_SKIP = 2;
	static final int CONTROL_INTERFACEGROUP = 240;
	static final int CONTROLS_GRAB = 28;
	static final int CONTROLS_UP = 29;
	static final int CONTROLS_DOWN = 30;
	static final int CONTROLS_LEFT = 31;
	static final int CONTROLS_RIGHT = 32;
	static final int CONTROLS_EXIT = 33;

	private RSNPC molly;
	private RSObject controlPanel;
	private int mollyID = -1;
	private boolean cameraSet;
	private boolean talkedToMolly;
	private boolean finished;
	private long delayTime;
	
	public void onFinish() {
		RSNPC molly = null;
		RSObject controlPanel = null;
		mollyID = -1;
		cameraSet = false;
		talkedToMolly = false;
		finished = false;
		delayTime = -1;
	}

	@Override
	public boolean activateCondition() {
		molly = npcs.getNearest("Molly");
		controlPanel = objects.getNearest(Molly.CONTROL_PANEL_ID);
		return (molly != null && molly.isInteractingWithLocalPlayer())
				|| (controlPanel != null);
	}

	private boolean inControlInterface() {
		final RSInterface i = interfaces.get(Molly.CONTROL_INTERFACEGROUP);
		return (i != null) && i.isValid();
	}

	private boolean inControlRoom() {
		final RSObject o = objects.getNearest(DOOR_ID);
		return (o != null)
				&& (getMyPlayer().getLocation().getX() > o.getLocation().getX());
	}

	@Override
	public int loop() {
		if (!activateCondition()) {
			log("Molly random finished!");
			sleep(500);
			if (!activateCondition()) {
				return -1;
			}
		}
		controlPanel = objects.getNearest(Molly.CONTROL_PANEL_ID);
		while (getMyPlayer().isMoving() || (getMyPlayer().getAnimation() != -1)) {
			return (random(800, 1300));
		}
		if (interfaces.canContinue()) {
			interfaces.clickContinue();
			return random(250, 750);
		}
		if (mollyID == -1) {
			mollyID = molly.getID();
			log("Molly ID: " + Integer.toString(mollyID));
			log("Evil Twin ID:" + Integer.toString(mollyID - 40));
		}
		if (interfaces.canContinue()) {
			setCamera();
			interfaces.clickContinue();
			return random(500, 800);
		}
		final RSComponent skipInterface = interfaces.get(
				Molly.MOLLY_CHATBOX_INTERFACEGROUP).getComponent(
				Molly.MOLLY_CHATBOX_SKIP);
		if ((skipInterface != null) && skipInterface.isValid()
				&& skipInterface.getAbsoluteY() > 5
				&& skipInterface.containsText("Yes, I")) {
			skipInterface.doClick();
			return random(600, 1000);
		}
		final RSComponent noThanksInterface = interfaces.get(
				Molly.MOLLY_CHATBOX_INTERFACEGROUP).getComponent(
				Molly.MOLLY_CHATBOX_NOTHANKS);
		if ((noThanksInterface != null) && noThanksInterface.isValid()
				&& noThanksInterface.getAbsoluteY() > 5) {
			setCamera();
			sleep(random(800, 1200));
			noThanksInterface.doClick();
			talkedToMolly = true;
			return random(600, 1000);
		}
		if (!cameraSet) {
			camera.setPitch(true);
			cameraSet = true;
			return (random(300, 500));
		}
		if (finished && !inControlRoom()) {
			if (!calc.tileOnScreen(molly.getLocation())) {
				walking.walkTileOnScreen(molly.getLocation());
				return random(1000, 2000);
			}
			molly.doAction("Talk");
			return (random(1000, 1200));
		}
		if (finished && inControlRoom()) {
			if (!openDoor()) {
				return (random(1000, 1500));
			}
			return (random(400, 600));
		}
		if (!inControlRoom()) {
			if (talkedToMolly
					&& !finished
					&& (interfaces.get(Molly.MOLLY_CHATBOX_INTERFACEGROUP) == null || interfaces
					.get(Molly.MOLLY_CHATBOX_INTERFACEGROUP)
					.getComponent(0).getAbsoluteY() < 2)
					&& (interfaces.get(Molly.MOLLY_CHATBOX_NOTHANKS) == null || interfaces
					.get(Molly.MOLLY_CHATBOX_NOTHANKS).getComponent(0)
					.getAbsoluteY() < 2)) {
				openDoor();
				sleep(random(800, 1200));
			} else {
				molly.doAction("Talk");
				talkedToMolly = true;
				return (random(1000, 2000));
			}
		} else {
			if (npcs.getNearest("Molly") != null) {
				finished = true;
				sleep(random(800, 1200));
			} else {
				if (!inControlInterface()) {
					if (calc.tileOnScreen(controlPanel.getLocation())) {
						controlPanel.doAction("Use");
						sleep(random(1200, 2000));
					} else {
						walking.walkTileOnScreen(controlPanel.getLocation());
						camera.setPitch(true);
						camera.turnTo(controlPanel);
					}
				} else {
					navigateClaw();
					delayTime = System.currentTimeMillis();
					while (!interfaces.canContinue()
							&& (System.currentTimeMillis() - delayTime < 15000)) {
					}
					if (interfaces.canContinue()) {
						interfaces.clickContinue();
					}
					sleep(random(300, 400));
				}
			}
		}
		return random(200, 400);
	}

	private void navigateClaw() {
		if (!inControlInterface() || (mollyID < 1)) {
			return;
		}
		RSObject claw;
		RSNPC suspect;
		while ((claw = objects.getNearest(Molly.CLAW_ID)) != null
				&& (suspect = npcs.getNearest(mollyID - 40)) != null) {
			final RSTile clawLoc = claw.getLocation();
			final RSTile susLoc = suspect.getLocation();
			final ArrayList<Integer> options = new ArrayList<Integer>();
			if (susLoc.getX() > clawLoc.getX()) {
				options.add(Molly.CONTROLS_LEFT);
			}
			if (susLoc.getX() < clawLoc.getX()) {
				options.add(Molly.CONTROLS_RIGHT);
			}
			if (susLoc.getY() > clawLoc.getY()) {
				options.add(Molly.CONTROLS_DOWN);
			}
			if (susLoc.getY() < clawLoc.getY()) {
				options.add(Molly.CONTROLS_UP);
			}
			if (options.isEmpty()) {
				options.add(Molly.CONTROLS_GRAB);
			}
			final RSInterface i = interfaces.get(Molly.CONTROL_INTERFACEGROUP);
			if ((i != null) && i.isValid()) {
				i.getComponent(options.get(random(0, options.size())))
						.doClick();
			}
			delayTime = System.currentTimeMillis();
			while (!hasClawMoved(clawLoc)
					&& (System.currentTimeMillis() - delayTime < 3500)) {
				sleep(10);
			}
		}
	}

	private boolean hasClawMoved(RSTile prevClawLoc) {
		RSObject claw = objects.getNearest(Molly.CLAW_ID);
		if (claw == null) {
			return false;
		}
		RSTile currentClawLoc = claw.getLocation();
		return (currentClawLoc.getX() - prevClawLoc.getX() != 0)
				|| (currentClawLoc.getY() - prevClawLoc.getY() != 0);
	}

	private boolean openDoor() {
		final RSObject door = objects.getNearest(Molly.DOOR_ID);
		if (door == null) {
			return false;
		}
		if (!calc.tileOnScreen(door.getLocation())) {
			walking.walkTileOnScreen(door.getLocation());
			sleep(1000, 2000);
			return false;
		}
		door.doAction("Open");
		return false;
	}

	private void setCamera() {
		if ((random(0, 6) == 3) && !cameraSet) {
			camera.setPitch(true);
			cameraSet = true;
		}
	}
}