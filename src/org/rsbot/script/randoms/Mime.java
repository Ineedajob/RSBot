package org.rsbot.script.randoms;

import org.rsbot.script.Random;
import org.rsbot.script.ScriptManifest;
import org.rsbot.script.wrappers.RSNPC;
import org.rsbot.script.wrappers.RSTile;

@ScriptManifest(authors = {"PwnZ", "Taha", "Zenzie"}, name = "Mime", version = 1.3)
public class Mime extends Random {

	private enum Stage {
		click, findMime, findAnimation, clickAnimation, wait
	}

	private int animation;
	private RSNPC mime;

	public void onFinish() {
		mime = null;
		animation = -1;
	}
	@Override
	public boolean activateCondition() {
		final RSNPC mime = npcs.getNearest(1056);
		return (mime != null) && (calc.distanceTo(mime.getLocation()) < 15);
	}

	private boolean clickAnimation(final String find) {
		if (!interfaces.get(188).isValid()) {
			return false;
		}
		for (int a = 0; a < interfaces.get(188).getChildCount(); a++) {
			if (interfaces.get(188).getComponent(a).getText().contains(find)) {
				log("Clicked on: " + find);
				sleep(random(500,1000));
				interfaces.get(188).getComponent(a).doClick();
				sleep(random(1000,1200));
				return true; 
			}
		}
		return false;
	}

	private RSNPC getNPCAt(final RSTile t) {
		for (RSNPC npc : npcs.getAll()) {
			if (npc.getLocation().equals(t)) {
				return npc;
			}
		}
		return null;
	}

	private Stage getStage() {
		if (interfaces.canContinue() && getMyPlayer().getLocation().equals(new RSTile(2008, 4764))) {
			return Stage.click;
		} else if (mime == null) {
			return Stage.findMime;
		} else if ((interfaces.get(372).getComponent(2).getText().contains("Watch") || interfaces.get(372).getComponent(
				3).getText().contains("Watch")) && (mime.getAnimation() != -1) && (mime.getAnimation() != 858)) {
			return Stage.findAnimation;
		} else if (interfaces.get(188).isValid()) {
			return Stage.clickAnimation;
		} else {
			return Stage.wait;
		}
	}

	@Override
	public int loop() {
		if (!activateCondition()) {
			return -1;
		}
		switch (getStage()) {
			case click:
				interfaces.clickContinue();
				sleep(random(1500, 2000));
				return random(200, 400);

			case findMime:
				if (((mime = npcs.getNearest(1056)) == null) && ((mime = getNPCAt(new RSTile(2011, 4762))) == null)) {
					log.warning("ERROR: Mime not found!");
					return -1;
				}
				return random(200, 400);

			case findAnimation:
				animation = mime.getAnimation();
				log.info("Found Mime animation: " + animation);
				sleep(1000);
				if (interfaces.get(188).isValid()) {
					return random(400, 800);
				}
				final long start = System.currentTimeMillis();
				while (System.currentTimeMillis() - start >= 5000) {
					if (interfaces.get(188).isValid()) {
						return random(1000, 1600);
					}
					sleep(random(1000, 1500));
				}
				return random(200, 400);

			case clickAnimation:
				log.info("Clicking text according to animation: " + animation);
				if ((animation != -1) && (animation != 858)) {
					switch (animation) {
						case 857:
							clickAnimation("Think");
							break;
						case 860:
							clickAnimation("Cry");
							break;
						case 861:
							clickAnimation("Laugh");
							break;
						case 866:
							clickAnimation("Dance");
							break;
						case 1128:
							clickAnimation("Glass Wall");
							break;
						case 1129:
							clickAnimation("Lean");
							break;
						case 1130:
							clickAnimation("Rope");
							break;
						case 1131:
							clickAnimation("Glass Box");
							break;
						default:
							log.info("Unknown Animation: " + animation + " Please inform a developer at RSBot.org!");
							return random(2000, 3000);
					}
				}
			case wait:
				return random(200, 400);
		}
		return random(200, 400);
	}
}