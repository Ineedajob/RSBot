import org.rsbot.event.listeners.PaintListener;
import org.rsbot.script.Script;
import org.rsbot.script.ScriptManifest;
import org.rsbot.script.methods.Bank;
import org.rsbot.script.methods.Game;
import org.rsbot.script.methods.Skills;
import org.rsbot.script.util.Filter;
import org.rsbot.script.util.Timer;
import org.rsbot.script.wrappers.RSComponent;
import org.rsbot.script.wrappers.RSItem;
import org.rsbot.script.wrappers.RSNPC;
import org.rsbot.script.wrappers.RSObject;
import org.rsbot.script.wrappers.RSPlayer;
import org.rsbot.script.wrappers.RSTile;
import org.rsbot.util.GlobalConfiguration;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * RSBot AIO Firemaker
 *
 * @author Jacmob
 * @version 2.0
 */
@ScriptManifest(authors = {"Jacmob"}, keywords = "Firemaking", name = "AIO Firemaker", version = 2.0, description = "Place your logs at top of the bank.")
public class AIOFiremaker extends Script implements PaintListener {

	private enum State {
		FIREMAKE, OPEN_BANK, BANK
	}

	class AntiBan implements Runnable {

		public void run() {
			int camAlt = camera.getZ();
			char LR = KeyEvent.VK_LEFT;
			char UD;
			if (camAlt > -1600) {
				UD = KeyEvent.VK_UP;
			} else if (camAlt < -2215 || random(0, 2) == 0) {
				UD = KeyEvent.VK_DOWN;
			} else {
				UD = KeyEvent.VK_UP;
			}
			if (random(0, 2) == 0) {
				LR = KeyEvent.VK_RIGHT;
			}

			keyboard.pressKey(LR);
			try {
				Thread.sleep(random(50, 400));
			} catch (Exception ignored) {
			}
			keyboard.pressKey(UD);
			try {
				Thread.sleep(random(300, 700));
			} catch (Exception ignored) {
			}
			keyboard.releaseKey(UD);
			try {
				Thread.sleep(random(100, 400));
			} catch (Exception ignored) {
			}
			keyboard.releaseKey(LR);
		}

	}

	public static final int TINDERBOX = 590;
	public static final int FIRE_RING = 13659;
	public static final int FLAME_GLOVES = 13660;
	public static final int[] FireObjects = {2732, 2982, 2983, 2984, 2985, 2986, 1189};

	public static final Color BG = new Color(100, 0, 0, 150);
	public static final Color DROP = new Color(20, 0, 0, 255);
	public static final Color TEXT = new Color(200, 255, 0, 255);

	private int logId = 0;
	private String logName = "";
	private String eqString = "";
	private double xpPerFire = 0;
	private double xpMultiplier = 1;
	private FMLocation location;

	private int nextMinRunEnergy = random(20, 50);
	private int currentZone = 0;
	private int sine = 0;
	private int sineM = 1;
	private int scriptStartXP = 0;
	private long scriptStartTime = 0;
	private long lastXPCheckTime = 0;
	private RSTile nextTile = null;
	private RSTile blackListedTile = null;

	private final AStar pathFinder = new AStar();
	private final ExecutorService antiBanExecutor = Executors.newSingleThreadExecutor();

	private RSTile checkTile(RSTile tile) {
		if (calc.distanceTo(tile) < 17) {
			return tile;
		}
		RSTile loc = getMyPlayer().getLocation();
		RSTile walk = new RSTile((loc.getX() + tile.getX()) / 2,
				(loc.getY() + tile.getY()) / 2);
		return calc.distanceTo(walk) < 17 ? walk : checkTile(walk);
	}

	private void checkXP() {
		if (System.currentTimeMillis() - lastXPCheckTime < 300000) {
			return;
		}
		lastXPCheckTime = System.currentTimeMillis();
		game.openTab(Game.TAB_STATS);
		sleep(random(50, 500));
		if (random(0, 5) != 1) {
			if (random(0, 2) == 1) {
				mouse.move(random(575, 695), random(240, 435), 10);
			}
			interfaces.getComponent(320, 85).doHover();
			sleep(random(800, 3400));
		}
		if (random(0, 2) == 0) {
			game.openTab(Game.TAB_INVENTORY);
		}
	}

	private RSTile getBestFreeTile() {
		RSTile nextT = getProceedingTile(getMyPlayer().getLocation());
		if (nextT != null) {
			RSTile nextT2 = getProceedingTile(nextT);
			if (nextT2 != null && isFreeTile(nextT) && isFreeTile(nextT2)
					&& !isPlayerAt(nextT) && !isPlayerAt(nextT2)) {
				return nextT;
			}
		}
		int start = 0, longest = 0, length = 0;
		RSTile[] bestRow = location.zones[currentZone].rows[0].tiles;
		outer:
		for (int i = 0; i < location.zones[currentZone].rows.length; i++) {
			RSTile[] tiles = location.zones[currentZone].rows[i].tiles;
			// log.info("Traversing Row " + i);
			for (int j = 0; j < tiles.length; j++) {
				if (tiles[j] == blackListedTile) {
					blackListedTile = null;
					continue outer;
				}
				if (isFireAt(tiles[j])) {
					if (j - start > length
							|| (j - start >= inventory.getCount(logId) || j
							- start == length)
							&& calc.distanceTo(tiles[start]) < calc.distanceTo(bestRow[longest])) {
						length = j - start;
						longest = start;
						bestRow = tiles;
						// log.info("  Set Best - " + i + ", " + start + " (" +
						// length + "/" + tiles.length + ")");
					}
					start = j + 1;
				} else if (j == tiles.length - 1) {
					if (j - start > length - 1
							|| (j - start + 1 >= inventory.getCount(logId) || j
							- start == length - 1)
							&& calc.distanceTo(tiles[start]) < calc.distanceTo(bestRow[longest])) {
						length = j - start + 1;
						longest = start;
						bestRow = tiles;
						// log.info("  Set Best - " + i + ", " + start + " (" +
						// length + "/" + tiles.length + ")");
					}
				}
			}
			start = 0;
		}

		// log("Calculated Best Tile: " + bestRow[longest].getX() + "," +
		// bestRow[longest].getY());
		if (length == 0 && start != 0) {
			return null;
		} else {
			return bestRow[longest];
		}
	}

	private RSTile getClosestTileInRegion(RSTile tile) {
		RSTile loc = getMyPlayer().getLocation();
		for (int i = 0; i < 1000; ++i) {
			if (tileInRegion(tile)) {
				return tile;
			}
			tile = new RSTile((loc.getX() + tile.getX()) / 2,
					(loc.getY() + tile.getY()) / 2);
		}
		return null;
	}

	private RSTile getProceedingTile(RSTile location) {
		if (location == null) {
			return null;
		}
		FMRow[] rows = this.location.zones[currentZone].rows;
		int x = location.getX(), y = location.getY();
		for (FMRow row : rows) {
			int start = Math.min(row.start, row.end);
			int end = Math.max(row.start, row.end);
			if (this.location.zones[currentZone].horizontal && y == row.pos
					&& x >= start && x <= end) {
				if (row.start < row.end && x + 1 <= row.end) {
					return new RSTile(x + 1, y);
				} else if (row.start > row.end && x - 1 >= row.end) {
					return new RSTile(x - 1, y);
				}
				return null;
			} else if (!this.location.zones[currentZone].horizontal && x == row.pos
					&& y >= start && y <= end) {
				if (row.start < row.end && y + 1 <= row.end) {
					return new RSTile(x, y + 1);
				} else if (row.start > row.end && y - 1 >= row.end) {
					return new RSTile(x, y - 1);
				}
				return null;
			}
		}
		return null;
	}

	private State getState() {
		if (!inventory.contains(logId) || !inventory.contains(TINDERBOX)) {
			if (bank.isOpen()) {
				return State.BANK;
			} else {
				return State.OPEN_BANK;
			}
		} else {
			return State.FIREMAKE;
		}
	}

	private void highlightTile(Graphics g, RSTile t, Color outline, Color fill) {
		Point pn = calc.tileToScreen(t, 0, 0, 0);
		Point px = calc.tileToScreen(t, 1, 0, 0);
		Point py = calc.tileToScreen(t, 0, 1, 0);
		Point pxy = calc.tileToScreen(t, 1, 1, 0);
		if (py.x == -1 || pxy.x == -1 || px.x == -1 || pn.x == -1) {
			return;
		}
		g.setColor(outline);
		g.drawPolygon(new int[]{py.x, pxy.x, px.x, pn.x},
				new int[]{py.y, pxy.y, px.y, pn.y}, 4);
		g.setColor(fill);
		g.fillPolygon(new int[]{py.x, pxy.x, px.x, pn.x},
				new int[]{py.y, pxy.y, px.y, pn.y}, 4);
	}

	private boolean isFireAt(RSTile location) {
		RSObject obj = objects.getTopAt(location);
		if (obj == null) {
			return false;
		}
		int objID = obj.getID();
		for (int i : FireObjects) {
			if (objID == i) {
				return true;
			}
		}
		return false;
	}

	private boolean isFreeTile(RSTile location) {
		return isInRow(location) && !isFireAt(location);
	}

	private boolean isInRow(RSTile location) {
		FMRow[] rows = this.location.zones[currentZone].rows;
		int x = location.getX(), y = location.getY();
		for (FMRow row : rows) {
			int start = Math.min(row.start, row.end);
			int end = Math.max(row.start, row.end);
			if (this.location.zones[currentZone].horizontal && y == row.pos
					&& x >= start && x <= end
					|| !this.location.zones[currentZone].horizontal && x == row.pos
					&& y >= start && y <= end) {
				return true;
			}
		}
		return false;
	}

	private boolean isPlayerAt(final RSTile tile) {
		return players.getAll(new Filter<RSPlayer>() {
			public boolean accept(RSPlayer player) {
				return player.getLocation().equals(tile);
			}
		}).length > 0;
	}

	@Override
	public int loop() {
		if (scriptStartTime == -1 && skills.getRealLevel(Skills.FIREMAKING) > 1) {
			int fireLevel = skills.getRealLevel(Skills.FIREMAKING);
			scriptStartTime = System.currentTimeMillis();
			scriptStartXP = skills.getCurrentExp(Skills.FIREMAKING);

			if (equipment.containsAll(FIRE_RING, FLAME_GLOVES)) {
				log.info("Your ring and gloves will grant you 5% extra XP per log.");
				xpMultiplier = 1.05;
				eqString = " (+5%)";
			} else if (equipment.containsAll(FIRE_RING)) {
				log.info("Your Ring of Fire will grant you 2% extra XP per log.");
				if (fireLevel >= 79) {
					log.info("Flame Gloves with your ring would grant you 5% extra XP per log.");
				}
				xpMultiplier = 1.02;
				eqString = " (+2%)";
			} else if (equipment.containsAll(FLAME_GLOVES)) {
				log.info("A Ring of Fire with your gloves would grant you 5% extra XP per log.");
				xpMultiplier = 1.02;
				eqString = " (+2%)";
			} else if (fireLevel >= 79) {
				log.info("A Ring of Fire and Flame Gloves would grant you 5% extra XP per log.");
			} else if (fireLevel >= 62) {
				log.info("A Ring of Fire from 'All Fired Up' would grant you 2% extra XP per log.");
			}
		} else {
			mouse.setSpeed(random(6, 9));
		}
		State state = getState();
		switch (state) {
			case FIREMAKE:
				if (!isFreeTile(getMyPlayer().getLocation())) {
					nextTile = getBestFreeTile();
					if (calc.distanceTo(nextTile) == 0) {
						return random(100, 200);
					}
					if (nextTile != null) {
						int iters = 0;
						while (calc.distanceTo(nextTile) > 0) {
							if (isFireAt(nextTile)) {
								blackListedTile = nextTile;
								return random(100, 300);
							}
							if (calc.tileOnScreen(nextTile) && !bank.isOpen()) {
								if (inventory.isItemSelected()) {
									unUse();
								}
								Point location = calc.tileToScreen(nextTile);
								if (location.x == -1 || location.y == -1) {
									break;
								} else {
									mouse.move(location, 5, 5);
								}
								tiles.doAction(nextTile, "Walk here");
								sleep(random(200, 400));
								if (random(0, 3) != 0) {
									RSItem item = inventory.getItem(TINDERBOX);
									if (item != null) {
										item.getComponent().doHover();
									}
								}
								sleep(random(50, 100));
							} else {
								if (walking.getEnergy() > nextMinRunEnergy) {
									walking.setRun(true);
									nextMinRunEnergy = random(20, 50);
								}
								if (calc.distanceTo(nextTile) < 16) {
									walkTo(nextTile);
									sleep(random(200, 500));
								} else {
									walkTo(nextTile);
								}
							}
							int tries = 0;
							while (tries < 10 && calc.distanceTo(nextTile) > 0
									&& getMyPlayer().isMoving()) {
								tries++;
								sleep(random(300, 500));
							}
							sleep(random(50, 80));
							RSTile proceeding = getProceedingTile(nextTile);
							if (proceeding != null && calc.distanceTo(proceeding) == 0
									&& isFreeTile(proceeding) || iters > 50
									|| calc.distanceTo(nextTile) > 100) {
								break;
							}
							iters++;
						}
					}
				} else if (bank.isOpen()) {
					bank.close();
				} else if (isTinderboxSelected()) {
					RSTile currTile = getMyPlayer().getLocation();
					if (currTile.equals(nextTile)) {
						nextTile = getProceedingTile(currTile);
						if (nextTile != null && !isFreeTile(nextTile)) {
							nextTile = null;
						}
					}
					RSTile secondTile = nextTile == null ? null : getProceedingTile(nextTile);
					if (secondTile != null && !isFreeTile(secondTile)) {
						secondTile = null;
					}
					boolean oneLog = inventory.getCount(logId) == 1;
					RSItem item = inventory.getItem(logId);
					if (item != null) {
						item.getComponent().doClick();
					}
					if (random(0, 40) == 0) {
						antiBanExecutor.submit(new AntiBan());
					}
					if (random(0, 15) != 0 && secondTile != null && !oneLog) {
						sleep(random(150, 400));
						item = inventory.getItem(TINDERBOX);
						if (item != null) {
							item.doClick(true);
						}
						if (random(0, 5) != 0) {
							sleep(random(50, 200));
							item = inventory.getItem(logId);
							if (item != null) {
								mouse.move(item.getComponent().getCenter(), 30, 30);
								if (mouse.getLocation().distance(item.getComponent().getCenter()) > 10) {
									sleep(random(20, 100));
									mouse.move(inventory.getItem(logId).getComponent().getCenter(), 8, 8);
								}
							}
						}
					} else if (random(0, 5) == 0) {
						mouse.move(random(75, 700), random(750, 400), 50);
						sleep(random(100, 2500));
						if (random(0, 2) == 1) {
							mouse.move(random(75, 600), random(75, 400), 30);
						}
					} else if (random(0, 5) != 0 && !oneLog) {
						item = inventory.getItem(TINDERBOX);
						if (item != null) {
							item.getComponent().doHover();
						}
					}
					if (random(0, 40) == 0) {
						antiBanExecutor.submit(new AntiBan());
					}
					while (getMyPlayer().getAnimation() != -1
							|| getMyPlayer().isMoving()) {
						sleep(random(70, 120));
					}
					if (random(0, 200) == 0) {
						checkXP();
					}
					if (nextTile != null) {
						int tries = 0;
						while (tries < 5 && calc.distanceTo(nextTile) != 0 ||
								calc.distanceTo(currTile) == 0) {
							sleep(random(100, 150));
							tries++;
						}
					}
				} else {
					if (random(0, 200) == 0) {
						checkXP();
					}
					if (!inventory.isItemSelected()) {
						if (getMyPlayer().isMoving()) {
							sleep(random(10, 20));
						}
						RSItem item = inventory.getItem(TINDERBOX);
						if (item != null) {
							item.doClick(true);
						}
						sleep(random(10, 100));
					}
					if (random(0, 40) == 0) {
						antiBanExecutor.submit(new AntiBan());
					}
				}
				break;
			case OPEN_BANK:
				RSTile bankLoc = nearestBank();
				nextTile = bankLoc;
				if (walking.getEnergy() > nextMinRunEnergy) {
					walking.setRun(true);
					nextMinRunEnergy = random(20, 50);
				}
				if (inventory.isItemSelected()) {
					unUse();
				}
				if (bankLoc == null) {
					walkTo(location.bank);
				} else if (calc.distanceTo(bankLoc) < 7) {
					if (bank.open()) {
						sleep(700);
						if (getMyPlayer().isMoving()) {
							sleep(1000);
						}
					} else {
						sleep(random(200, 400));
						if (!bank.open()) {
							rotateCamera();
						}
					}
				} else if (location.randomness == -1) {
					walkTo(bankLoc);
				} else {
					walkTo(new RSTile(location.bank.getX()
							+ random(0, location.randomness + 1),
							location.bank.getY()
									+ random(0, location.randomness + 1)));
				}
				break;
			case BANK:
				bank.depositAllExcept(logId, TINDERBOX);
				if (!inventory.contains(TINDERBOX)) {
					withdraw(TINDERBOX, "Tinderbox", false);
					sleep(1000);
					if (!inventory.contains(TINDERBOX)) {
						sleep(1000);
					}
				} else if (!inventory.contains(logId)) {
					withdraw(logId, logName, true);
					sleep(random(500, 800));
					if (!inventory.contains(logId)) {
						sleep(random(500, 700));
					}
				}
				if (inventory.contains(logId) && random(0, 2) == 0) {
					bank.close();
				}
				currentZone = random(0, location.zones.length);
				break;
			default:
				break;
		}
		return random(500, 1000);
	}

	private RSTile nearestBank() {
		RSObject booth = objects.getNearest(Bank.BANK_BOOTHS);
		RSNPC BankerNPC = npcs.getNearest(Bank.BANKERS);
		RSObject BankChest = objects.getNearest(Bank.BANK_CHESTS);
		int minDist = 30;
		if (BankChest != null) {
			minDist = calc.distanceTo(BankChest);
		}
		if (BankerNPC != null && calc.distanceTo(BankerNPC) < minDist) {
			minDist = calc.distanceTo(BankerNPC);
			BankChest = null;
		}
		if (booth != null && calc.distanceTo(booth) < minDist) {
			BankerNPC = null;
		}
		if (BankerNPC != null) {
			return BankerNPC.getLocation();
		}
		if (BankChest != null) {
			return BankChest.getLocation();
		}
		if (booth != null) {
			return booth.getLocation();
		}
		return null;
	}

	@Override
	public void onFinish() {
		log.info("AIO Firemaker Stopped. You gained "
				+ (skills.getCurrentExp(Skills.FIREMAKING) - scriptStartXP)
				+ " XP in "
				+ Timer.format(System.currentTimeMillis() - scriptStartTime) + ".");
	}

	public void onRepaint(Graphics g) {
		if (game.isLoggedIn()) {
			if (bank.isOpen()) {
				RSItem logIF = bank.getItem(logId);
				RSItem tbIF = bank.getItem(TINDERBOX);
				g.setColor(Color.green);
				if (logIF != null && logIF.getComponent().getAbsoluteY() < 270) {
					g.drawRect(logIF.getComponent().getAbsoluteX() - 1,
							logIF.getComponent().getAbsoluteY() - 1,
							logIF.getComponent().getWidth() + 2,
							logIF.getComponent().getHeight() + 2);
				}
				g.setColor(Color.blue);
				if (tbIF != null && tbIF.getComponent().getAbsoluteY() < 270) {
					g.drawRect(tbIF.getComponent().getAbsoluteX() - 1,
							tbIF.getComponent().getAbsoluteY() - 1,
							tbIF.getComponent().getWidth() + 2,
							tbIF.getComponent().getHeight() + 2);
				}
			} else if (nextTile != null) {
				highlightTile(g, nextTile, new Color(255, 0, 0, 20), new Color(255, 255, 0, 20));
			}

			if (scriptStartTime == -1) {
				return;
			}

			String title = "AIOFiremaker "
					+ getClass().getAnnotation(ScriptManifest.class).version()
					+ " by Jacmob";
			int x = 13;
			int y = 26;

			if (sine >= 84) {
				sine = 84;
				sineM *= -1;
			} else if (sine <= 1) {
				sine = 1;
				sineM *= -1;
			}

			sine += sineM;

			g.setColor(BG);
			g.fill3DRect(x - 6, y, 211, 25, true);

			g.setColor(DROP);
			g.drawString(title, x + 1, y += 18);
			g.setColor(TEXT);
			g.drawString(title, x, y -= 1);

			if (xpPerFire == 0) {
				return;
			}

			int fireLevel = skills.getRealLevel(Skills.FIREMAKING);
			int levelsGained = fireLevel - Skills.getLevelAt(scriptStartXP);
			int XPGained = skills.getCurrentExp(Skills.FIREMAKING) - scriptStartXP;
			int lvlPerc = skills.getPercentToNextLevel(Skills.FIREMAKING);
			int XPToLevel = skills.getExpToNextLevel(Skills.FIREMAKING);
			long runMillis = System.currentTimeMillis() - scriptStartTime;

			int LogsBurned = (int) Math.round(XPGained
					/ (xpPerFire * xpMultiplier));
			String lvlStr = levelsGained + " lvls";
			if (levelsGained == 1) {
				lvlStr = "1 lvl";
			}

			g.setColor(BG);
			g.fill3DRect(x - 6, y += 11, 211, 112, true);

			g.setColor(TEXT);
			if (scriptStartTime == 0) {
				g.drawString("Waiting For > Lvl 1...", x, y += 17);
			} else {
				g.drawString("Runtime: " + Timer.format(runMillis), x, y += 17);
			}
			g.drawString("Gained: " + XPGained + " XP (" + lvlStr + ")", x, y += 17);
			g.drawString("Burned: " + LogsBurned + " Logs" + eqString, x, y += 17);
			g.drawString("Next Level: " + (fireLevel + 1) + " ("
					+ (int) Math.ceil(XPToLevel / (xpPerFire * xpMultiplier))
					+ " Fires)", x, y += 17);
			g.drawString("Next Level: " + XPToLevel + " XP", x, y += 17);

			g.setColor(new Color(0, 0, 0, 150));
			g.fill3DRect(x, y += 8, 199, 12, true);

			g.setColor(new Color(255 - 2 * lvlPerc,
					(int) (1.7 * lvlPerc + sine), 0, 150));
			g.fillRect(x + 1, y += 1, (int) (1.97 * lvlPerc), 10);
			g.setColor(new Color(255, 255, 255, 50));
			g.fillRect(x + 1, y, (int) (1.97 * lvlPerc), 5);

			g.setColor(BG);

			if (XPGained >= 1000) {
				int XPPerSecond = (int) (XPGained * 1000 / runMillis);
				int SecsToLevel = XPToLevel / XPPerSecond;

				g.fill3DRect(x - 6, y += 21, 211, 59, true);
				g.setColor(TEXT);
				g.drawString("Next Level: "
						+ Timer.format(SecsToLevel * 1000), x, y += 17);
				g.drawString("Averaging: " + XPPerSecond * 3600 + " XP/hr", x,
						y += 17);
				g.drawString("Averaging: "
						+ (int) Math.ceil(LogsBurned * 360000D / runMillis)
						+ "0 Logs/hr", x, y += 17);
			} else {
				g.fill3DRect(x - 6, y += 21, 211, 25, true);
				g.setColor(TEXT);
				g.drawString("Calculating...", x, y += 17);
			}

		}
	}

	@Override
	public boolean onStart() {
		log.info("Waiting for options to be set...");
		FMFrame gui = new FMFrame("AIO Firemaker");
		while (gui.isVisible()) {
			sleep(500);
		}
		location = gui.getSelectedLocation();
		if (location == null) {
			return false;
		}
		logId = (int) gui.getSelectedLogInfo()[0];
		xpPerFire = gui.getSelectedLogInfo()[1];
		logName = gui.getSelectedLogName();
		scriptStartTime = -1;
		log.info("Options Loaded - Burning " + logName + "...");
		return true;
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

	private boolean tileInRegion(RSTile tile) {
		int tileX = tile.getX() - game.getBaseX(), tileY = tile.getY() - game.getBaseY();
		return !(tileX < 0 || tileY < 0 || tileX > 103 || tileY > 103);
	}

	private void unUse() {
		int rand = random(0, 2);
		if (game.getCurrentTab() != Game.TAB_INVENTORY) {
			game.openTab(Game.TAB_INVENTORY);
		} else if (rand == 0) {
			game.openTab(Game.TAB_FRIENDS);
			sleep(random(200, 500));
			if (random(0, 3) != 0) {
				mouse.move(random(575, 695), random(238, 418));
			}
			game.openTab(Game.TAB_INVENTORY);
		} else {
			checkXP();
		}
	}

	private boolean isTinderboxSelected() {
		RSItem item = inventory.getSelectedItem();
		return item != null && item.getID() == TINDERBOX;
	}

	public void walkTo(RSTile tile) {
		RSTile dest = getMyPlayer().getLocation();
		RSTile[] path = null;
		if (calc.distanceBetween(tile, dest) > 1) {
			dest = getClosestTileInRegion(tile);
			if (dest != null) {
				path = pathFinder.findPath(getMyPlayer().getLocation(), dest);
			}
		}
		if (path == null) {
			walking.walkTileMM(checkTile(tile));
			return;
		}
		for (int i = path.length - 1; i >= 0; i--) {
			if (calc.distanceTo(path[i]) < 17) {
				RSTile currDest = walking.getDestination();
				if (currDest != null) {
					if (calc.distanceBetween(currDest, path[i]) <= 3) {
						break;
					}
				}
				walking.walkTileMM(checkTile(path[i]));
				sleep(random(200, 400));
				RSTile cdest = walking.getDestination();
				if (cdest != null && calc.distanceTo(cdest) > 6) {
					sleep(random(300, 500));
				}
				break;
			}
		}
	}

	private void withdraw(int ID, String name, boolean all) {
		if (!bank.isOpen()) {
			return;
		}
		RSComponent[] scrollbox = interfaces.getComponent(
				Bank.INTERFACE_BANK, Bank.INTERFACE_BANK_SCROLLBAR)
				.getComponents();
		if (scrollbox.length > 1) {
			int scrollTop = scrollbox[0].getAbsoluteY();
			if (scrollbox[1].getAbsoluteY() > scrollTop + 1) {
				mouse.click(scrollbox[0].getAbsoluteX() + random(2, 4),
						scrollTop + random(2, 4), true);
			}
		}
		RSItem bankItem = bank.getItem(ID);
		if (bankItem == null) {
			sleep(2000);
			if (bank.getItem(ID) == null) {
				log.severe("Unable to withraw " + name
						+ ". You will be logged out in ten seconds.");
				sleep(random(9000, 11000));
				bank.close();
				stopScript();
			}
		} else if (bankItem.getComponent().getAbsoluteY() > 270
				&& interfaces.getComponent(Bank.INTERFACE_BANK,
				Bank.INTERFACE_BANK_BUTTON_SEARCH).doAction("Search")) {
			sleep(random(1200, 1500));
			int rand = random(0, Math.min(4, name.length() - 2));
			keyboard.sendText(name.toLowerCase().substring(0, name.length() - rand),
					false);
			sleep(random(900, 1200));
			bankItem = bank.getItem(ID);
			if (bankItem == null
					|| bankItem.getComponent().getAbsoluteY() > 270
					|| !bankItem.doAction(all ? "Withdraw-All " + name : "Withdraw-1 " + name)
					&& !bankItem.doAction(all ? "Withdraw-All " + name : "Withdraw-1 " + name)) {
				log.severe("Unable to withraw " + name
						+ ". You will be logged out in ten seconds.");
				sleep(random(9000, 11000));
				if (bank.isOpen()) {
					bank.close();
				}
				stopScript();
			} else {
				interfaces.getComponent(Bank.INTERFACE_BANK, Bank.INTERFACE_BANK_TAB[0]).doClick();
			}
		} else {
			bankItem.doAction(all ? "Withdraw-All " + name : "Withdraw-1 " + name);
		}
	}

	class AStar {

		private class Node {

			public int x, y;
			public Node parent;
			public double g, f;

			public Node(int x, int y) {
				this.x = x;
				this.y = y;
				g = f = 0;
			}

			public boolean isAt(Node another) {
				return x == another.x && y == another.y;
			}

			public RSTile toRSTile(int baseX, int baseY) {
				return new RSTile(x + baseX, y + baseY);
			}

		}

		private int[][] blocks;

		public AStar() {

		}

		private Node cheapestNode(ArrayList<Node> open) {
			Node c = null;
			for (Node t : open) {
				if (c == null || t.f < c.f) {
					c = t;
				}
			}
			return c;
		}

		private double diagonalHeuristic(Node current, Node end) {
			double dx = Math.abs(current.x - end.x);
			double dy = Math.abs(current.y - end.y);
			double diag = Math.min(dx, dy);
			double straight = dx + dy;
			return Math.sqrt(2.0) * diag + straight - 2 * diag;
		}

		public RSTile[] findPath(RSTile cur, RSTile dest) {
			int baseX = game.getBaseX(), baseY = game.getBaseY();
			int currX = cur.getX() - baseX, currY = cur.getY() - baseY;
			int destX = dest.getX() - baseX, destY = dest.getY() - baseY;
			if (currX < 0 || currY < 0 || currX > 103 || currY > 103 || destX < 0
					|| destY < 0 || destX > 103 || destY > 103) {
				return null;
			}
			ArrayList<Node> closed = new ArrayList<Node>(), open = new ArrayList<Node>();
			blocks = walking.getCollisionFlags(game.getPlane());
			Node current = new Node(currX, currY);
			Node destination = new Node(destX, destY);
			open.add(current);
			while (open.size() > 0) {
				current = cheapestNode(open);
				closed.add(current);
				open.remove(open.indexOf(current));
				for (Node n : getSurroundingWalkableNodes(current)) {
					if (!isIn(closed, n)) {
						if (!isIn(open, n)) {
							n.parent = current;
							n.g = current.g + getAdditionalCost(n, current);
							n.f = n.g + diagonalHeuristic(n, destination);
							open.add(n);
						} else {
							Node old = getNode(open, n);
							if (current.g + getAdditionalCost(old, current) < old.g) {
								old.parent = current;
								old.g = current.g + getAdditionalCost(old, current);
								old.f = old.g + diagonalHeuristic(old, destination);
							}
						}
					}
				}
				if (isIn(closed, destination)) {
					return getPath(closed.get(closed.size() - 1), baseX, baseY);
				}
			}
			return null;
		}

		private double getAdditionalCost(Node start, Node end) {
			double cost = 1.0;
			if (!(start.x == end.y) || start.x == end.y) {
				cost = Math.sqrt(2.0);
			}
			return cost;
		}

		private Node getNode(ArrayList<Node> nodes, Node key) {
			for (Node n : nodes) {
				if (n.isAt(key)) {
					return n;
				}
			}
			return null;
		}

		private RSTile[] getPath(Node endNode, int baseX,
								 int baseY) {
			ArrayList<RSTile> reversePath = new ArrayList<RSTile>();
			Node p = endNode;
			while (p.parent != null) {
				reversePath.add(p.toRSTile(baseX, baseY));
				int next = (int) (Math.random() * 4 + 5);
				for (int i = 0; i < next && p.parent != null; i++) {
					p = p.parent;
				}
			}
			RSTile[] fixedPath = new RSTile[reversePath.size()];
			for (int i = 0; i < fixedPath.length; i++) {
				fixedPath[i] = reversePath.get(fixedPath.length - 1 - i);
			}
			return fixedPath;
		}

		private ArrayList<Node> getSurroundingWalkableNodes(Node t) {
			ArrayList<Node> tiles = new ArrayList<Node>();
			int curX = t.x, curY = t.y;
			if (curX > 0 && curY < 103
					&& (blocks[curX - 1][curY + 1] & 0x1280138) == 0
					&& (blocks[curX - 1][curY] & 0x1280108) == 0
					&& (blocks[curX][curY + 1] & 0x1280120) == 0) {
				tiles.add(new Node(curX - 1, curY + 1));
			}
			if (curY < 103 && (blocks[curX][curY + 1] & 0x1280120) == 0) {
				tiles.add(new Node(curX, curY + 1));
			}
			if (curX > 0 && curY < 103
					&& (blocks[curX - 1][curY + 1] & 0x1280138) == 0
					&& (blocks[curX - 1][curY] & 0x1280108) == 0
					&& (blocks[curX][curY + 1] & 0x1280120) == 0) {
				tiles.add(new Node(curX + 1, curY + 1));
			}
			if (curX > 0 && (blocks[curX - 1][curY] & 0x1280108) == 0) {
				tiles.add(new Node(curX - 1, curY));
			}
			if (curX < 103 && (blocks[curX + 1][curY] & 0x1280180) == 0) {
				tiles.add(new Node(curX + 1, curY));
			}
			if (curX > 0 && curY > 0
					&& (blocks[curX - 1][curY - 1] & 0x128010e) == 0
					&& (blocks[curX - 1][curY] & 0x1280108) == 0
					&& (blocks[curX][curY - 1] & 0x1280102) == 0) {
				tiles.add(new Node(curX - 1, curY - 1));
			}
			if (curY > 0 && (blocks[curX][curY - 1] & 0x1280102) == 0) {
				tiles.add(new Node(curX, curY - 1));
			}
			if (curX < 103 && curY > 0
					&& (blocks[curX + 1][curY - 1] & 0x1280183) == 0
					&& (blocks[curX + 1][curY] & 0x1280180) == 0
					&& (blocks[curX][curY - 1] & 0x1280102) == 0) {
				tiles.add(new Node(curX + 1, curY - 1));
			}
			return tiles;
		}

		private boolean isIn(ArrayList<Node> nodes, Node key) {
			return getNode(nodes, key) != null;
		}
	}

}

class FMFrame extends JFrame {

	/**
	 * AIO Firemaker Swing GUI Class
	 */
	private static long serialVersionUID = -6177554547983230084L;
	private int currentLogs;
	private String currentLogName;
	private JComboBox logsBox;
	private JComboBox locationsBox;
	private FMLocation currentLocation;
	private HashMap<Integer, Double> XPMap;
	private HashMap<String, Integer> logMap;
	private HashMap<String, FMLocation> locationMap;

	public FMLocation[] LOCATIONS;
	public File SETTINGS_FILE = new File(new File(
			GlobalConfiguration.Paths.getSettingsDirectory()), "AIOFM.txt");

	public FMFrame(String title) {
		super(title);

		/*
		 * LOCATIONS ARRAY
		 *
		 * FMLocation("Name", FMZone[], (RSTile)bankLocation, (int)randomness)
		 * FMZone(FMRow[], (boolean)horizontal) FMRow((int)startpos,
		 * (int)endpos, (int)otheraxispos)
		 *
		 * If a zone is horizontal, starpos and endpos for all rows will refer
		 * to the x axis and otheraxispos will refer to the y position. As far
		 * as I have seen all firemaking runs from east to west, so horizontal =
		 * true && endpos < startpos The randomness for each location determins
		 * how it will walk to the bank tile. If -1 is specified, it will walk
		 * directly to the found bank if possible, otherwise the script will
		 * always walk to bankLocation, with the randomness specified. 0 = no
		 * randomness, 1 = one tile randomness etc.
		 */
		LOCATIONS = new FMLocation[]{

				new FMLocation("Grand Exchange", new FMZone[]{
						new FMZone(new FMRow[]{new FMRow(3172, 3157, 3484),
								new FMRow(3178, 3151, 3483),
								new FMRow(3178, 3151, 3482),
								new FMRow(3169, 3161, 3481)}, true),
						new FMZone(new FMRow[]{new FMRow(3173, 3156, 3494),
								new FMRow(3172, 3157, 3495),
								new FMRow(3178, 3151, 3496),
								new FMRow(3178, 3151, 3497),
								new FMRow(3168, 3161, 3498)}, true)},
						new RSTile(3162, 3490), 1),

				new FMLocation("Draynor Village", new FMZone[]{new FMZone(
						new FMRow[]{new FMRow(3097, 3077, 3249),
								new FMRow(3098, 3072, 3248),
								new FMRow(3095, 3081, 3247)}, true)},
						new RSTile(3093, 3244), 0),

				new FMLocation("Fist of Guthix", new FMZone[]{new FMZone(
						new FMRow[]{new FMRow(1717, 1676, 5601),
								new FMRow(1718, 1676, 5600),
								new FMRow(1718, 1676, 5599),
								new FMRow(1718, 1676, 5598),
								new FMRow(1718, 1676, 5597)}, true)},
						new RSTile(1703, 5599), -1),

				new FMLocation("Varrock West", new FMZone[]{new FMZone(
						new FMRow[]{new FMRow(3199, 3175, 3431),
								new FMRow(3199, 3168, 3430),
								new FMRow(3199, 3168, 3429),
								new FMRow(3199, 3168, 3428)}, true)},
						new RSTile(3183, 3435), 0),

				new FMLocation("Varrock East", new FMZone[]{new FMZone(
						new FMRow[]{new FMRow(3265, 3241, 3429),
								new FMRow(3265, 3241, 3428),
								new FMRow(3257, 3255, 3427),
								new FMRow(3252, 3250, 3427)}, true)},
						new RSTile(3253, 3421), 0),

				new FMLocation("Falador East", new FMZone[]{new FMZone(
						new FMRow[]{new FMRow(3032, 3005, 3359),
								new FMRow(3032, 3005, 3360),
								new FMRow(3032, 3001, 3361),
								new FMRow(3032, 3001, 3362),
								new FMRow(3032, 3001, 3363)}, true)},
						new RSTile(3012, 3356), 0),

				new FMLocation("Yanille", new FMZone[]{new FMZone(
						new FMRow[]{new FMRow(2606, 2577, 3099),
								new FMRow(2606, 2577, 3098),
								new FMRow(2606, 2578, 3097)}, true)},
						new RSTile(2612, 3092), 0)

		};

		currentLocation = null;
		logsBox = new JComboBox();
		locationsBox = new JComboBox();
		XPMap = new HashMap<Integer, Double>();
		logMap = new HashMap<String, Integer>();
		locationMap = new HashMap<String, FMLocation>();
		setupFrame();
		setVisible(true);
	}

	public FMLocation getSelectedLocation() {
		return currentLocation;
	}

	public double[] getSelectedLogInfo() {
		return new double[]{currentLogs, XPMap.get(currentLogs)};
	}

	public String getSelectedLogName() {
		return currentLogName;
	}

	private void setupFrame() {

		// FRAME

		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		setResizable(false);
		setAlwaysOnTop(true);
		setLocationRelativeTo(null);
		setSize(200, 120);

		// START BUTTON

		JButton startButton = new JButton("Start");
		add(startButton, BorderLayout.SOUTH);
		startButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {

				currentLocation = locationMap.get(locationsBox
						.getSelectedItem().toString());
				currentLogs = logMap.get(logsBox.getSelectedItem().toString());
				currentLogName = logsBox.getSelectedItem().toString();

				// WRITE TO SETTINGS FILE

				try {
					BufferedWriter out = new BufferedWriter(
							new FileWriter(SETTINGS_FILE));
					out.write(currentLogName + ":"
							+ locationsBox.getSelectedItem().toString());
					out.close();
				} catch (Exception ignored) {
				}

				// DISPOSE

				setVisible(false);
				dispose();
			}
		});

		// COMBO BOXES

		String[] locations = new String[LOCATIONS.length];

		/*
		 * LOG ARRAYS
		 *
		 * The following three arrays must be the same length, with
		 * <code>logs</code> referring to the log name, <code>logIDs</code>
		 * referring to the log ID, and <code>logXPs</code> referring to the
		 * firemaking XP gained each time a log of that kind is burned.
		 *
		 * Each string in (String[] logs) must be the correct name of the item
		 * specified in logIDs or the script will be unable to withdraw the logs
		 * from the bank.
		 */
		String[] logs = {"Logs", "Oak logs", "Willow logs",
				"Maple logs", "Yew logs", "Magic logs"};
		int[] logIDs = {1511, 1521, 1519, 1517, 1515, 1513};
		double[] logXPs = {40.0, 60.0, 90.0, 135.0, 202.5, 303.8};

		for (int i = 0; i < logs.length; i++) {
			logMap.put(logs[i], logIDs[i]);
			XPMap.put(logIDs[i], logXPs[i]);
		}

		for (int i = 0; i < locations.length; i++) {
			locations[i] = LOCATIONS[i].name;
			locationMap.put(locations[i], LOCATIONS[i]);
		}

		locationsBox.setModel(new DefaultComboBoxModel(locations));
		add(locationsBox, BorderLayout.CENTER);

		logsBox.setModel(new DefaultComboBoxModel(logs));
		add(logsBox, BorderLayout.NORTH);

		// LOAD SAVED SELECTIONS FROM SETTINGS FILE

		try {

			BufferedReader in = new BufferedReader(new FileReader(
					SETTINGS_FILE));
			String line;
			String[] opts = {};

			while ((line = in.readLine()) != null) {
				if (line.contains(":")) {
					opts = line.split(":");
				}
			}
			in.close();
			if (opts.length == 2) {
				logsBox.setSelectedItem(opts[0]);
				locationsBox.setSelectedItem(opts[1]);
			}
		} catch (IOException ignored) {
		}

	}

}

class FMLocation {
	public String name;
	public FMZone[] zones;
	public RSTile bank;
	public int randomness;

	public FMLocation(String name, FMZone[] zones,
					  RSTile bank, int randomness) {
		this.name = name;
		this.zones = zones;
		this.bank = bank;
		this.randomness = randomness;
	}
}

class FMRow {
	public RSTile[] tiles;
	public int start, end, pos;

	public FMRow(int start, int end, int pos) {
		this.start = start;
		this.end = end;
		this.pos = pos;
	}

	public void generateTiles(boolean horizontal) {
		int length = Math.abs(end - start) + 1;
		tiles = new RSTile[length];
		if (end > start) {
			if (horizontal) {
				for (int i = 0; i < length; i++) {
					tiles[i] = new RSTile(start + i, pos);
				}
			} else {
				for (int i = 0; i < length; i++) {
					tiles[i] = new RSTile(pos, start + i);
				}
			}
		} else {
			if (horizontal) {
				for (int i = 0; i < length; i++) {
					tiles[i] = new RSTile(start - i, pos);
				}
			} else {
				for (int i = 0; i < length; i++) {
					tiles[i] = new RSTile(pos, start - i);
				}
			}
		}
	}

}

class FMZone {
	public FMRow[] rows;
	public boolean horizontal;

	public FMZone(FMRow[] fmRows, boolean horizontal) {
		rows = fmRows;
		this.horizontal = horizontal;
		for (FMRow r : rows) {
			r.generateTiles(horizontal);
		}
	}

}