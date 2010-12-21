import org.rsbot.event.listeners.PaintListener;
import org.rsbot.script.Script;
import org.rsbot.script.ScriptManifest;
import org.rsbot.script.methods.Game;
import org.rsbot.script.methods.Skills;
import org.rsbot.script.util.Timer;
import org.rsbot.script.wrappers.GEItemInfo;
import org.rsbot.script.wrappers.RSComponent;
import org.rsbot.script.wrappers.RSTile;

import java.awt.*;

@ScriptManifest(authors = "Jacmob", keywords = {"Crafting", "Money Making"}, name = "AutoSpinner", version = 2.1, description = "Lumbridge castle; flax at top of bank.")
public class AutoSpinner extends Script implements PaintListener {

	private enum State {
		WALKTOBANK, WALKTOSPIN, OPENBANK, OPENSPIN, CLIMBUP, BANK, SPIN
	}

	public final int EMOTE_ID = 1563;
	public final int FLAX_ID = 1779;
	public final int BOW_STRING_ID = 1777;
	public final int[] BANK_AREA = {3207, 3210, 3217, 3220};
	public final RSTile BANK_TILE = new RSTile(3208, 3221);
	public final RSTile SPINNER_TILE = new RSTile(3209, 3212);
	public final RSTile BANK_WALK_TILE = new RSTile(3208, 3219);
	public final RSTile STAIRCASE_TILE = new RSTile(3205, 3208);
	public final RSTile STAIRCASE_GROUND_TILE = new RSTile(3204, 3208);
	public final RSTile STAIRCASE_GROUND_WALK_TILE = new RSTile(3207, 3210);

	private int scriptStartXP = 0;
	private int nextMinRunEnergy = random(20, 50);
	private int flaxSpun = 0;
	private int flaxPrice = 0;
	private int stringPrice = 0;
	private long scriptStartTime = 0;

	private void antiBan(final int upperBound) {
		final int rand = random(0, upperBound);
		if (rand == 69) {
			if (game.getCurrentTab() == Game.TAB_STATS) {
				game.openTab(Game.TAB_INVENTORY);
				sleep(random(50, 1000));
			}
			final Point screenLoc = calc.tileToScreen(getMyPlayer()
					.getLocation());
			mouse.move(screenLoc, 3, 3, 5);
			sleep(random(50, 300));
			mouse.click(false);
			sleep(random(500, 2500));
			while (menu.isOpen()) {
				mouse.moveRandomly(700);
				sleep(random(100, 500));
			}
		} else if (rand == 68) {
			if (game.getCurrentTab() != Game.TAB_STATS) {
				game.openTab(Game.TAB_STATS);
				sleep(random(200, 400));
				interfaces.getComponent(Skills.INTERFACE_TAB_STATS, Skills.INTERFACE_CRAFTING).doHover();
				sleep(random(800, 1500));
				if (random(0, 2) == 0) {
					moveMouseAway(10);
				}
				sleep(random(200, 400));
			} else if (game.getCurrentTab() == Game.TAB_STATS) {
				game.openTab(Game.TAB_INVENTORY);
				sleep(random(800, 1200));
			}
		} else if (rand == 67) {
			final int rand2 = random(1, 3);
			for (int i = 0; i < rand2; i++) {
				mouse.move(random(100, 700), random(100, 500));
				sleep(random(200, 700));
			}
			mouse.move(random(0, 800), 647, 50, 100);
			sleep(random(100, 1500));
			mouse.move(random(75, 400), random(75, 400), 30);
		} else if (rand == 0) {
			rotateCamera();
		} else if (rand < 4) {
			waveMouse();
		}
	}

	private State getState() {
		if (game.getPlane() == 0) {
			return State.CLIMBUP;
		} else if (inventory.contains(FLAX_ID)) {
			if (game.getPlane() == 2) {
				return State.WALKTOSPIN;
			} else if (getSpinComponent().isValid()) {
				return State.SPIN;
			} else {
				return State.OPENSPIN;
			}
		} else {
			if (game.getPlane() == 1) {
				return State.WALKTOBANK;
			} else if (bank.isOpen()) {
				return State.BANK;
			} else {
				return State.OPENBANK;
			}
		}
	}

	@Override
	public int loop() {
		mouse.setSpeed(random(5, 8));
		final State state = getState();
		int tries = 0;
		antiBan(65);
		switch (state) {
			case WALKTOSPIN:
				if (walking.getEnergy() > nextMinRunEnergy) {
					walking.setRun(true);
					nextMinRunEnergy = random(20, 50);
				}
				if (calc.tileOnScreen(STAIRCASE_TILE)) {
					if (bank.isOpen()) {
						bank.close();
					}
					if (!tiles.doAction(STAIRCASE_TILE, "Climb-down")
							&& !getMyPlayer().isMoving()) {
						rotateCamera();
					}
					while (game.getPlane() != 1 && tries < 10) {
						tries++;
						sleep(random(100, 200));
					}
				} else {
					walkTileSmart(STAIRCASE_TILE);
					while (calc.distanceTo(STAIRCASE_TILE) > 4 && tries < 10) {
						tries++;
						sleep(random(400, 500));
					}
				}
				break;
			case WALKTOBANK:
				if (getSpinComponent().isValid()) {
					mouse.click(random(480, 485), random(42, 48), true);
				}
				if (calc.tileOnScreen(STAIRCASE_TILE)) {
					if (!tiles.doAction(STAIRCASE_TILE, "Climb-up")
							&& !getMyPlayer().isMoving()) {
						rotateCamera();
						break;
					} else {
						if (random(0, 5) != 1) {
							mouse.move(random(608, 640), random(50, 90), 5);
							if (random(0, 5) != 1) {
								moveMouseAway(5);
							}
						}
					}
					while (game.getPlane() != 2 && tries < 15) {
						tries++;
						sleep(random(200, 400));
					}
				} else {
					walkTileSmart(STAIRCASE_TILE);
					while (calc.distanceTo(STAIRCASE_TILE) > 4 && tries < 10) {
						tries++;
						sleep(random(400, 500));
					}
				}
				break;
			case OPENBANK:
				if (playerIsInArea(BANK_AREA)) {
					tiles.doAction(BANK_TILE, "Use-quickly");
					while (!bank.isOpen() && tries < 5) {
						tries++;
						sleep(random(400, 600));
					}
				} else {
					walkTileSmart(BANK_WALK_TILE);
					sleep(random(200, 700));
					while (!playerIsInArea(BANK_AREA) && tries < 10) {
						tries++;
						sleep(random(400, 600));
					}
				}
				break;
			case OPENSPIN:
				if (calc.tileOnScreen(SPINNER_TILE)) {
					if (tiles.doAction(SPINNER_TILE, "Spin")) {
						moveMouseAway(50);
					} else {
						break;
					}
					while (!getSpinComponent().isValid() && tries < 10) {
						if (getMyPlayer().isMoving()) {
							tries = 2;
						}
						tries++;
						sleep(random(400, 600));
						antiBan(65);
					}
				} else {
					camera.turnToTile(SPINNER_TILE, 20);
				}
				break;
			case SPIN:
				int stringsPreviouslyHeld = inventory.getCount(BOW_STRING_ID);
				if (getSpinComponent().doAction("Make All")) {
					if (random(0, 2) == 1) {
						moveMouseAway(50);
					}
					sleep(random(1000, 1200));
					if (getSpinComponent().isValid()) {
						break;
					}
				} else {
					break;
				}
				while (tries < 15 && inventory.contains(FLAX_ID)) {
					if (getMyPlayer().getAnimation() == EMOTE_ID) {
						tries = 0;
					} else {
						tries++;
					}
					sleep(random(300, 500));
					antiBan(80);
					flaxSpun += inventory.getCount(BOW_STRING_ID)
							- stringsPreviouslyHeld;
					stringsPreviouslyHeld = inventory.getCount(BOW_STRING_ID);
				}
				break;
			case BANK:
				if (inventory.getCount() > 0) {
					bank.depositAll();
					if (random(0, 5) == 0) {
						moveMouseAway(7);
					}
					return random(400, 600);
				}
				if (inventory.getCount(FLAX_ID) == 0) {
					if (bank.getItem(FLAX_ID) != null) {
						bank.getItem(FLAX_ID).doAction("Withdraw-All");
						sleep(random(50, 100));
					}
					if (random(0, 5) == 0) {
						moveMouseAway(7);
						sleep(random(1000, 1200));
					} else {
						sleep(random(1000, 1200));
						if (random(0, 5) == 0) {
							moveMouseAway(7);
						}
					}
					if (inventory.getCount(FLAX_ID) == 0 && bank.getItem(FLAX_ID) == null) {
						if (bank.isOpen()) {
							bank.close();
							sleep(random(200, 500));
						}
						log.info("No Flax Found");
						stopScript();
						break;
					}
				}
				sleep(random(150, 400));
				if (inventory.getCount(FLAX_ID) > 0 && random(0, 3) == 0) {
					bank.close();
				}
			case CLIMBUP:
				if (calc.tileOnScreen(STAIRCASE_TILE)) {
					if (!tiles.doAction(STAIRCASE_GROUND_TILE, "Climb-up")
							&& !getMyPlayer().isMoving()) {
						rotateCamera();
					}
					sleep(random(400, 600));
				} else {
					walking.walkTileMM(walking.getClosestTileOnMap(STAIRCASE_GROUND_WALK_TILE), 1, 1);
					sleep(random(1000, 2000));
				}
				break;
			default:
				break;
		}
		return random(400, 700);
	}

	private void moveMouseAway(final int moveDist) {
		final Point pos = mouse.getLocation();
		mouse.move(pos.x - moveDist, pos.y - moveDist, moveDist * 2,
				moveDist * 2);
	}

	@Override
	public boolean onStart() {
		final GEItemInfo stringGE = grandExchange.loadItemInfo(BOW_STRING_ID);
		stringPrice = stringGE.getMarketPrice();
		log.info("Each bow string will be valued at the current GE market price of " + stringPrice + " coins.");
		final GEItemInfo flaxGE = grandExchange.loadItemInfo(FLAX_ID);
		flaxPrice = flaxGE.getMarketPrice();
		log.info("Each piece of flax will be valued at the current GE market price of " + flaxPrice + " coins.");
		if (flaxPrice == 0 || stringPrice == 0) {
			log.info("Grand Exchange prices could not be loaded - some features of the paint will be disabled.");
		}
		return true;
	}

	@Override
	public void onFinish() {
		log.info(flaxSpun + " flax spun in " +
				Timer.format(System.currentTimeMillis() - scriptStartTime) + ".");
	}

	public void onRepaint(final Graphics g) {
		if (game.isLoggedIn()
				&& skills.getCurrentLevel(Skills.CRAFTING) > 1) {
			if (scriptStartTime == 0) {
				scriptStartTime = System.currentTimeMillis();
				scriptStartXP = skills
						.getCurrentExp(Skills.CRAFTING);
			}

			final Color BG = new Color(50, 50, 50, 150);
			final Color TEXT = new Color(200, 255, 0, 255);

			final int x = 13;
			int y = 26;

			final int levelsGained = skills
					.getRealLevel(Skills.CRAFTING)
					- Skills.getLevelAt(scriptStartXP);
			final long runSeconds = (System.currentTimeMillis() - scriptStartTime) / 1000;
			g.setColor(BG);
			g.fill3DRect(x - 6, y, 211, 26, true);
			g.setColor(TEXT);
			g.drawString("AutoSpinner v2.1 by Jacmob", x, y += 17);

			y += 20;
			g.setColor(BG);
			g.fill3DRect(x - 6, y, 211, 86, true);

			y -= 3;
			g.setColor(TEXT);
			g.drawString("Runtime: "
					+ Timer.format(System.currentTimeMillis()
					- scriptStartTime), x, y += 20);
			g.drawString("Spun: " + flaxSpun + " Flax", x, y += 20);

			if (levelsGained < 0) {
				scriptStartXP = skills
						.getCurrentExp(Skills.CRAFTING);
			} else if (levelsGained == 1) {
				g
						.drawString(
								"Gained: "
										+ (skills
										.getCurrentExp(Skills.CRAFTING) - scriptStartXP)
										+ " XP (" + levelsGained + " lvl)", x,
								y += 20);
			} else {
				g
						.drawString(
								"Gained: "
										+ (skills
										.getCurrentExp(Skills.CRAFTING) - scriptStartXP)
										+ " XP (" + levelsGained + " lvls)", x,
								y += 20);
			}

			if (runSeconds > 10 && flaxSpun > 0) {
				g.drawString("Averaging: " +
						(skills.getCurrentExp(Skills.CRAFTING) - scriptStartXP)
								* 3600 / runSeconds + " XP/hr", x, y += 20);
				if (flaxPrice != 0 && stringPrice != 0) {
					y += 20;
					g.setColor(BG);
					g.fill3DRect(x - 6, y, 211, 66, true);
					y -= 3;
					g.setColor(TEXT);
					final int profit = flaxSpun * (stringPrice - flaxPrice);
					g.drawString("Gained: " + profit + " GP", x, y += 20);
					g.drawString("Averaging: " + flaxSpun * 3600 / runSeconds
							+ " spins/hr", x, y += 20);
					g.drawString("Averaging: " + profit * 3600 / runSeconds
							+ " GP/hr", x, y += 20);
				}
			} else {
				g.drawString("Gathering Data...", x, y += 20);
			}
		}
	}

	private RSComponent getSpinComponent() {
		return interfaces.get(905).getComponent(16);
	}

	private boolean playerIsInArea(final int[] bounds) {
		final RSTile pos = getMyPlayer().getLocation();
		return pos.getX() >= bounds[0] && pos.getX() <= bounds[1]
				&& pos.getY() >= bounds[2] && pos.getY() <= bounds[3];
	}

	private void rotateCamera() {
		int angle = camera.getAngle() + random(-40, 40);
		if (angle < 0) {
			angle += 359;
		}
		if (angle > 359) {
			angle -= 359;
		}

		camera.setAngle(angle);
	}

	private boolean walkTileSmart(final RSTile t) {
		if (calc.tileOnScreen(t)) {
			return tiles.doAction(t, "Walk");
		}
		return walking.walkTo(t);
	}

	private void waveMouse() {
		final Point curPos = mouse.getLocation();
		mouse.move(random(0, 750), random(0, 500), 20);
		sleep(random(100, 300));
		mouse.move(curPos, 20, 20);
	}
}