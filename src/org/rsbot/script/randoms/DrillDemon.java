package org.rsbot.script.randoms;

import org.rsbot.script.Random;
import org.rsbot.script.ScriptManifest;
import org.rsbot.script.wrappers.RSNPC;
import org.rsbot.script.wrappers.RSObject;
import org.rsbot.script.wrappers.RSTile;

@ScriptManifest(authors = {"Keilgo"}, name = "DrillDemon", version = 0.2)
public class DrillDemon extends Random {

	public final int demonID = 2790;
	public int sign1;
	public int sign2;
	public int sign3;
	public int sign4;

	@Override
	public boolean activateCondition() {
		return playerInArea(3167, 4822, 3159, 4818);
	}

	@SuppressWarnings("deprecation")
	@Override
	public int loop() {
		camera.setPitch(true);
		camera.setCompass('N');

		if (getMyPlayer().isMoving() || (getMyPlayer().getAnimation() != -1)) {
			return random(1900, 2400);
		}

		final RSNPC demon = npcs.getNearest(demonID);
		final RSObject mat1 = objects.getNearest(10076);
		final RSObject mat2 = objects.getNearest(10077);
		final RSObject mat3 = objects.getNearest(10078);
		final RSObject mat4 = objects.getNearest(10079);

		if (demon == null) {
			return -1;
		}

		myClickContinue();
		sleep(random(750, 1000));

		if (interfaces.get(148).isValid()) {
			switch (settings.getSetting(531)) {
				case 668:
					sign1 = 1;
					sign2 = 2;
					sign3 = 3;
					sign4 = 4;
					break;
				case 675:
					sign1 = 2;
					sign2 = 1;
					sign3 = 3;
					sign4 = 4;
					break;
				case 724:
					sign1 = 1;
					sign2 = 3;
					sign3 = 2;
					sign4 = 4;
					break;
				case 738:
					sign1 = 3;
					sign2 = 1;
					sign3 = 2;
					sign4 = 4;
					break;
				case 787:
					sign1 = 2;
					sign2 = 3;
					sign3 = 1;
					sign4 = 4;
					break;
				case 794:
					sign1 = 3;
					sign2 = 2;
					sign3 = 1;
					sign4 = 4;
					break;
				case 1116:
					sign1 = 1;
					sign2 = 2;
					sign3 = 4;
					sign4 = 3;
					break;
				case 1123:
					sign1 = 2;
					sign2 = 1;
					sign3 = 4;
					sign4 = 3;
					break;
				case 1228:
					sign1 = 1;
					sign2 = 4;
					sign3 = 2;
					sign4 = 3;
					break;
				case 1249:
					sign1 = 4;
					sign2 = 1;
					sign3 = 2;
					sign4 = 3;
					break;
				case 1291:
					sign1 = 2;
					sign2 = 4;
					sign3 = 1;
					sign4 = 3;
					break;
				case 1305:
					sign1 = 4;
					sign2 = 2;
					sign3 = 1;
					sign4 = 3;
					break;
				case 1620:
					sign1 = 1;
					sign2 = 3;
					sign3 = 4;
					sign4 = 2;
					break;
				case 1634:
					sign1 = 3;
					sign2 = 1;
					sign3 = 4;
					sign4 = 2;
					break;
				case 1676:
					sign1 = 1;
					sign2 = 4;
					sign3 = 3;
					sign4 = 2;
					break;
				case 1697:
					sign1 = 4;
					sign2 = 1;
					sign3 = 3;
					sign4 = 2;
					break;
				case 1802:
					sign1 = 3;
					sign2 = 4;
					sign3 = 1;
					sign4 = 2;
					break;
				case 1809:
					sign1 = 4;
					sign2 = 3;
					sign3 = 1;
					sign4 = 2;
					break;
				case 2131:
					sign1 = 2;
					sign2 = 3;
					sign3 = 4;
					sign4 = 1;
					break;
				case 2138:
					sign1 = 3;
					sign2 = 2;
					sign3 = 4;
					sign4 = 1;
					break;
				case 2187:
					sign1 = 2;
					sign2 = 4;
					sign3 = 3;
					sign4 = 1;
					break;
				case 2201:
					sign1 = 4;
					sign2 = 2;
					sign3 = 3;
					sign4 = 1;
					break;
				case 2250:
					sign1 = 3;
					sign2 = 4;
					sign3 = 2;
					sign4 = 1;
					break;
				case 2257:
					sign1 = 4;
					sign2 = 3;
					sign3 = 2;
					sign4 = 1;
					break;
			}
		}

		if (interfaces.getComponent(148, 1).getText().contains("jumps")) {
			if (sign1 == 1) {
				if (calc.distanceTo(new RSTile(3167, 4820)) < 2) {
					walking.walkTileMM(walking.randomize(
							new RSTile(3160, 4820), 0, 0));
					mat1.doAction("Use");
				} else {
					mat1.doAction("Use");
				}
				return random(2000, 2500);
			} else if (sign2 == 1) {
				mat2.doAction("Use");
				return random(2000, 2500);
			} else if (sign3 == 1) {
				mat3.doAction("Use");
				return random(2000, 2500);
			} else if (sign4 == 1) {
				if (calc.distanceTo(new RSTile(3159, 4820)) < 2) {
					walking.walkTileMM(walking.randomize(
							new RSTile(3166, 4820), 0, 0));
					mat4.doAction("Use");
				} else {
					mat4.doAction("Use");
				}
				return random(2000, 2500);
			}
		} else if (interfaces.getComponent(148, 1).getText()
				.contains("push ups")) {
			if (sign1 == 2) {
				if (calc.distanceTo(new RSTile(3167, 4820)) < 2) {
					walking.walkTileMM(walking.randomize(
							new RSTile(3160, 4820), 0, 0));
					mat1.doAction("Use");
				} else {
					mat1.doAction("Use");
				}
				return random(2000, 2500);
			} else if (sign2 == 2) {
				mat2.doAction("Use");
				return random(2000, 2500);
			} else if (sign3 == 2) {
				mat3.doAction("Use");
				return random(2000, 2500);
			} else if (sign4 == 2) {
				if (calc.distanceTo(new RSTile(3159, 4820)) < 2) {
					walking.walkTileMM(walking.randomize(
							new RSTile(3166, 4820), 0, 0));
					mat4.doAction("Use");
				} else {
					mat4.doAction("Use");
				}
				return random(2000, 2500);
			}
		} else if (interfaces.getComponent(148, 1).getText()
				.contains("sit ups")) {
			if (sign1 == 3) {
				if (calc.distanceTo(new RSTile(3167, 4820)) < 2) {
					walking.walkTileMM(walking.randomize(
							new RSTile(3160, 4820), 0, 0));
					mat1.doAction("Use");
				} else {
					mat1.doAction("Use");
				}
				return random(1000, 1500);
			} else if (sign2 == 3) {
				mat2.doAction("Use");
				return random(2000, 2500);
			} else if (sign3 == 3) {
				mat3.doAction("Use");
				return random(2000, 2500);
			} else if (sign4 == 3) {
				if (calc.distanceTo(new RSTile(3159, 4820)) < 2) {
					walking.walkTileMM(walking.randomize(
							new RSTile(3166, 4820), 0, 0));
					mat4.doAction("Use");
				} else {
					mat4.doAction("Use");
				}
				return random(2000, 2500);
			}
		} else if (interfaces.getComponent(148, 1).getText().contains("jog on")) {
			if (sign1 == 4) {
				if (calc.distanceTo(new RSTile(3167, 4820)) < 2) {
					walking.walkTileMM(walking.randomize(
							new RSTile(3160, 4820), 0, 0));
					mat1.doAction("Use");
				} else {
					mat1.doAction("Use");
				}
				return random(2000, 2500);
			} else if (sign2 == 4) {
				mat2.doAction("Use");
				return random(2000, 2500);
			} else if (sign3 == 4) {
				mat3.doAction("Use");
				return random(2000, 2500);
			} else if (sign4 == 4) {
				if (calc.distanceTo(new RSTile(3159, 4820)) < 2) {
					walking.walkTileMM(walking.randomize(
							new RSTile(3166, 4820), 0, 0));
					mat4.doAction("Use");
				} else {
					mat4.doAction("Use");
				}
				return random(2000, 2500);
			}
		}

		if (!myClickContinue() && getMyPlayer().getAnimation() == -1) {
			demon.doAction("Talk-to");
		}

		return random(2000, 2500);
	}

	public boolean myClickContinue() {
		sleep(random(800, 1000));
		return interfaces.getComponent(243, 7).doClick()
				|| interfaces.getComponent(241, 5).doClick()
				|| interfaces.getComponent(242, 6).doClick()
				|| interfaces.getComponent(244, 8).doClick()
				|| interfaces.getComponent(64, 5).doClick();
	}

	public boolean playerInArea(final int maxX, final int maxY, final int minX,
	                            final int minY) {
		final int x = getMyPlayer().getLocation().getX();
		final int y = getMyPlayer().getLocation().getY();
		return (x >= minX) && (x <= maxX) && (y >= minY) && (y <= maxY);
	}
}