import org.rsbot.event.events.MessageEvent;
import org.rsbot.event.listeners.MessageListener;
import org.rsbot.event.listeners.PaintListener;
import org.rsbot.script.Script;
import org.rsbot.script.ScriptManifest;
import org.rsbot.script.methods.Game;
import org.rsbot.script.methods.Skills;
import org.rsbot.script.wrappers.RSObject;
import org.rsbot.script.wrappers.RSTile;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

@ScriptManifest(authors = "Bloddyharry", name = "Master Farmer Stealer", keywords = "Thieving", version = 2.4, description = "Made by BloddyHarry. Settings in GUI.")
public class BloddyMasterFarmerStealer extends Script implements PaintListener,
		MessageListener {

	final ScriptManifest properties = getClass().getAnnotation(
			ScriptManifest.class);

	BufferedImage normal = null;
	BufferedImage clicked = null;
	public int farmerID = 2234;
	public int bBoothID = 2213;
	public int addFail = 0;
	private boolean doDropSeeds = true;
	public boolean showInventory = false;
	private boolean withdraw10 = false;
	private boolean highLevelMode = false;
	private boolean canSay = true;
	public boolean logOut = false, failed = false;
	public int wantedHours, wantedMinutes, wantedSeconds, wantedLevel;
	public int gainedLvls;
	public boolean finishAt, finishWhenLevel;
	private int startXP = 0;
	public int[] seedID = { 5304, 5296, 5300, 5295, 5303, 5302, 5100, 5323,
			5299, 5301, 5298, 5320, 5321, 1161, 1965, 1969, 1967, 1895, 1893,
			1891, 1971, 4293, 2142, 4291, 2140, 3228, 9980, 7223, 6297, 6293,
			6295, 6299, 7521, 9988, 7228, 2878, 7568, 2343, 1861, 13433, 315,
			325, 319, 3144, 347, 355, 333, 339, 351, 329, 3381, 361, 10136,
			5003, 379, 365, 373, 7946, 385, 397, 391, 3369, 3371, 3373, 2309,
			2325, 2333, 2327, 2331, 2323, 2335, 7178, 7180, 7188, 7190, 7198,
			7200, 7208, 7210, 7218, 7220, 2003, 2011, 2289, 2291, 2293, 2295,
			2297, 2299, 2301, 2303, 1891, 1893, 1895, 1897, 1899, 1901, 7072,
			7062, 7078, 7064, 7084, 7082, 7066, 7068, 1942, 6701, 6703, 7054,
			6705, 7056, 7060, 2130, 1985, 1993, 1989, 1978, 5763, 5765, 1913,
			5747, 1905, 5739, 1909, 5743, 1907, 1911, 5745, 2955, 5749, 5751,
			5753, 5755, 5757, 5759, 5761, 2084, 2034, 2048, 2036, 2217, 2213,
			2205, 2209, 2054, 2040, 2080, 2277, 2225, 2255, 2221, 2253, 2219,
			2281, 2227, 2223, 2191, 2233, 2092, 2032, 2074, 2030, 2281, 2235,
			2064, 2028, 2187, 2185, 2229 };
	public int[] junkSeedID = { 5319, 5307, 5305, 5322, 5099, 5310, 5308, 5102,
			5101, 5096, 5324, 5306, 5291, 5103, 5292, 5097, 5281, 5098, 5294,
			5105, 5106, 5280, 5297, 5311, 5104, 5293, 5318, 5282, 5309 };
	private int FOODID;
	private int ANIMATIONID = 11974;
	RSTile[] farmerToBank = { new RSTile(3081, 3250), new RSTile(3092, 3244) };
	RSTile farmerTile = new RSTile(3081, 3250);
	RSTile bankTile = new RSTile(3092, 3244);
	String status = "";
	private int HP;
	private int startLvl;
	public int foodEated = 0;
	public int pickpockets = 0;
	public int failPickpocket = 0;
	public int somethingID = 5295;
	public long startTime = System.currentTimeMillis();
	public int[] pickpocketing = { 11942, 378 };
	final private cameraHeight camHeight = new cameraHeight();
	final private cameraRotate camRotate = new cameraRotate();
	private boolean guiWait = true, guiExit;
	BloddyMasterFarmerGUI gui;

	public boolean onStart() {
		try {
			final URL cursorURL = new URL("http://i48.tinypic.com/313623n.png");
			final URL cursor80URL = new URL("http://i46.tinypic.com/9prjnt.png");
			normal = ImageIO.read(cursorURL);
			clicked = ImageIO.read(cursor80URL);
		} catch (MalformedURLException e) {
			log("Unable to buffer cursor.");
		} catch (IOException e) {
			log("Unable to open cursor image.");
		}
		gui = new BloddyMasterFarmerGUI();
		gui.setVisible(true);
		while (guiWait) {
			sleep(100);
		}
		startTime = System.currentTimeMillis();
		startXP = skills.getCurrentExp(Skills.THIEVING);
		startLvl = skills.getCurrentLevel(Skills.THIEVING);
		mouse.setSpeed(6);
		camRotate.isActive = true;
		camRotate.start();
		camHeight.isActive = true;
		camHeight.start();
		log("Antiban initialized!");
		return !guiExit;
	}

	public void onFinish() {
		mouse.moveOffScreen();
		camHeight.isActive = false;
		camRotate.isActive = false;
	}

	public static void main(final String[] args) {
		new BloddyMasterFarmerStealer();
	}

	private int getCurrentLifepoint() {
		if (interfaces.get(748).getComponent(8).isValid()) {
			if (interfaces.get(748).getComponent(8).getText() != null) {
				HP = Integer.parseInt(interfaces.get(748).getComponent(8)
						.getText());
			} else {
				log.severe("Getting lifepoints Error");
			}
		} else {
			log.warning("HP Interface is not valid");
		}

		return HP;
	}

	private int checkEat() {
		int CurrHP = getCurrentLifepoint() / 10;
		int RealHP = skills.getRealLevel(Skills.CONSTITUTION);
		if (CurrHP <= random(RealHP / 2, RealHP / 1.5)) {
			if (!highLevelMode) {
				status = "Eating Food";
				if (inventory.contains(FOODID)) {
					inventory.getItem(FOODID).doAction("Eat");
					log("ate food..");
				}
			} else {
				if (getMyPlayer().getHPPercent() <= 10) {
					log.warning("Low HP! loggin out..");
					game.logout(true);
					stopScript(true);
				}
			}
		}
		return 0;
	}

	public int checkInventoryFull() {
		if (!highLevelMode) {
			if (bank.isOpen() && bank.getCount(FOODID) == 0
					&& !inventory.contains(FOODID) && game.isLoggedIn()) {
				log("Out of food! logging out..");
				sleep(random(4000, 5000));
				bank.close();
				logOut();
			}
		}
		if (inventory.isFull()) {
			if (!highLevelMode) {
				if (!atBank()) {
					status = "Walking to Bank";
					if (walking.getEnergy() == random(60, 100)) {
						// setRun(true);
					}
					if (calc.distanceTo(walking.getDestination()) < random(5,
							12)
							|| calc.distanceTo(walking.getDestination()) > 40) {
						if (!walking.walkPathMM(farmerToBank)) {
							walking.walkTo(walking.randomize(bankTile, 2, 2));
							return random(1400, 1600);
						}
					}
				} else if (atBank()) {
					openBank();
					bank();
				}
			}
		} else if (!inventory.contains(FOODID)) {
			status = "Getting food";
			if (atBank()) {
				openBank();
				bank();
			} else if (!atBank()) {
				if (calc.distanceTo(walking.getDestination()) < random(5, 12)
						|| calc.distanceTo(walking.getDestination()) > 40) {
					if (!walking.walkPathMM(farmerToBank)) {
						walking.walkTo(walking.randomize(bankTile, 2, 2));
						return random(1400, 1600);
					}
				}
			}
		} else {
			if (inventory.containsOneOf(junkSeedID)) {
				try {
					inventory.dropAllExcept(seedID);
				} catch (Exception e) {

				}
			}
		}
		return 0;
	}

	public void checkFail() {
		if (addFail == 3) {
			log("out of food! logging out!");
			bank.close();
			logOut();
			stopScript();
		}
	}

	private void logOut() {
		mouse.move(754, 10, 10, 10);
		mouse.click(true);
		mouse.move(642, 378, 100, 20);
		mouse.click(true);
		sleep(random(2000, 3000));
		stopScript();
	}

	@Override
	public int loop() {
		if (logOut == false) {
			checkFail();
			checkEat();
			checkInventoryFull();
			if (failed) {
				stunned();
			}
			if (!inventory.isFull()) {
				if (!highLevelMode) {
					if (inventory.contains(FOODID)) {
						if (calc.distanceTo(npcs.getNearest("Master Farmer")) <= 5) {
							pickPocket();
						} else if (calc.distanceTo(npcs
								.getNearest("Master Farmer")) >= 6) {
							status = "walking to Farmer";
							walking.walkTo((walking.randomize(
									npcs.getNearest("Master Farmer")
											.getLocation(), 1, 1)));
							return random(1500, 1700);
						}
					}
				} else {
					if (calc.distanceTo(npcs.getNearest("Master Farmer")) <= 5) {
						pickPocket();
					} else if (calc
							.distanceTo(npcs.getNearest("Master Farmer")) >= 6) {
						status = "walking to Farmer";
						walking.walkTo((walking.randomize(
								npcs.getNearest("Master Farmer").getLocation(),
								1, 1)));
						return random(1600, 1700);
					}
				}
			}
		} else {
			logOut();
		}
		return random(100, 200);
	}

	private boolean atBank() {
		return calc.distanceTo(bankTile) <= 5;
	}

	public int openBank() {
		final RSObject bankBooth = objects.getNearest(bBoothID);
		if (!(bank.isOpen())) {
			if (bankBooth != null) {
				bankBooth.doAction("Use-Quickly");
				sleep(random(200, 300));
			}
			if (bankBooth == null) {
				log("cant find bank :/");
				return random(150, 350);
			}
		}
		return random(150, 350);
	}

	public int bank() {
		status = "Banking";
		try {
			if (bank.isOpen()) {
				if (inventory.getCount() >= 1) {
					bank.depositAll();
					sleep(random(300, 400));
				}
				if (!withdraw10) {
					bank.getItem(FOODID).doAction("Withdraw-5");
					sleep(random(500, 600));
				} else {
					bank.getItem(FOODID).doAction("Withdraw-10");
					sleep(random(500, 600));
				}
			}
		} catch (Exception e) {
			log("Crash Prevented isbankopen=" + bank.isOpen());
			return 10;
		}
		return 0;
	}

	public boolean antiBan2() {
		int randomNumber = random(1, 45);
		if (randomNumber <= 9) {
			if (randomNumber == 1) {
			}
			if (randomNumber == 2) {
				mouse.move(random(50, 700), random(50, 450), 2, 2);
				sleep(random(200, 300));
				mouse.move(random(50, 700), random(50, 450), 2, 2);
			}
			if (randomNumber == 3) {
			}
			if (randomNumber == 4) {
				camera.setAngle(random(1, 360));
				mouse.move(random(50, 700), random(50, 450), 2, 2);
			}
			if (randomNumber == 6) {
				mouse.move(random(50, 700), random(50, 450), 2, 2);
			}
			if (randomNumber == 7) {
				mouse.move(random(50, 700), random(50, 450), 2, 2);
			}
			if (randomNumber == 8) {
				camera.setAngle(random(1, 360));
			}
			if (randomNumber == 9) {
			}
		}
		return true;
	}

	private boolean stunned() {
		final int random = random(1, 5);
		status = "failed";
		checkEat();
		if (!highLevelMode) {
			if (doDropSeeds) {
				if (inventory.containsOneOf(junkSeedID)) {
					try {
						inventory.dropAllExcept(seedID);
					} catch (Exception e) {

					}
				} else if (!inventory.containsOneOf(junkSeedID)) {
					antiBan();
				}
				if (random == 2) {
					antiBan();
				}
			} else {
				antiBan();
			}
			failed = false;
		} else {
			antiBan2();
			sleep(random(1400, 1700));
			failed = false;
		}
		return true;
	}

	private boolean pickPocket() {
		if (failed) {
			stunned();
		} else if (npcs.getNearest(farmerID) != null
				&& getMyPlayer().getAnimation() != ANIMATIONID
				&& getMyPlayer().isInCombat() == false
				&& !getMyPlayer().isMoving()) {
			status = "pickpocketing";
			doSomethingNPC(farmerID, "Pickpocket");
			antiBan2();
			sleep(random(400, 600));
		}
		if (failed) {
			stunned();
		}
		return true;

	}

	public boolean doSomethingNPC(int id, String action) {
		org.rsbot.script.wrappers.RSNPC NPC = npcs.getNearest(id);
		if (NPC == null) {
			return antiBan();
		}
		if (NPC.isOnScreen()) {
			NPC.doAction(action);
			return true;
		} else {
			walking.walkTo(NPC.getLocation());
			NPC.doAction(action);
			return true;
		}
	}

	public boolean antiBan() {
		int randomNumber = random(1, 16);
		if (randomNumber <= 16) {
			if (randomNumber == 1) {
				openRandomTab();
				sleep(random(100, 500));
				mouse.move(631, 254, 50, 100);
				sleep(random(2200, 2700));
			}
			if (randomNumber == 2) {
				mouse.move(random(50, 700), random(50, 450), 2, 2);
				sleep(random(200, 400));
				mouse.move(random(50, 700), random(50, 450), 2, 2);
			}
			if (randomNumber == 3) {
				camera.setAngle(random(1, 360));
				mouse.move(random(50, 700), random(50, 450), 2, 2);
			}
			if (randomNumber == 4) {
				sleep(random(100, 200));
				mouse.move(random(50, 700), random(50, 450), 2, 2);
				camera.setAngle(random(1, 360));
				mouse.move(random(50, 700), random(50, 450), 2, 2);
			}
			if (randomNumber == 6) {
				camera.setAngle(random(1, 360));
			}
			if (randomNumber == 7) {
				mouse.move(random(50, 700), random(50, 450), 2, 2);
			}
			if (randomNumber == 8) {
				sleep(random(100, 200));
				mouse.move(631, 278);
				mouse.move(random(50, 700), random(50, 450), 2, 2);
				sleep(random(200, 500));
				if (randomNumber == 9) {
					sleep(random(100, 200));
					mouse.move(random(50, 700), random(50, 450), 2, 2);
					if (randomNumber == 10) {
						mouse.move(random(50, 700), random(50, 450), 2, 2);
					}
					if (randomNumber == 11) {
						camera.setAngle(random(1, 360));
						mouse.move(random(50, 700), random(50, 450), 2, 2);
					}
					if (randomNumber == 12) {
						game.openTab(Game.TAB_STATS);
						sleep(random(1000, 2000));
					}
					if (randomNumber == 13) {
						mouse.move(random(50, 700), random(50, 450), 2, 2);
						camera.setAngle(random(1, 360));
					}

				}
			}
		}
		return true;
	}

	private void openRandomTab() {
		int randomNumber = random(1, 14);
		if (randomNumber <= 14) {
			if (randomNumber == 1) {
				game.openTab(Game.TAB_STATS);
			}
			if (randomNumber == 2) {
				game.openTab(Game.TAB_ATTACK);
			}
			if (randomNumber == 3) {
				game.openTab(Game.TAB_EQUIPMENT);
			}
			if (randomNumber == 4) {
				game.openTab(Game.TAB_FRIENDS);
			}
			if (randomNumber == 6) {
				game.openTab(Game.TAB_MAGIC);
			}
			if (randomNumber == 7) {
				game.openTab(Game.TAB_NOTES);
			}
		}
	}

	public void onRepaint(Graphics g) {
		long runTime = 0;
		long seconds = 0;
		long minutes = 0;
		long hours = 0;
		int pickpocketsHour = 0;
		int currentXP = 0;
		int currentLVL = 0;
		int gainedXP = 0;
		int gainedLVL = 0;
		int xpPerHour = 0;
		runTime = System.currentTimeMillis() - startTime;
		seconds = runTime / 1000;
		if (seconds >= 60) {
			minutes = seconds / 60;
			seconds -= (minutes * 60);
		}
		if (minutes >= 60) {
			hours = minutes / 60;
			minutes -= (hours * 60);
		}

		currentXP = skills.getCurrentExp(Skills.THIEVING);
		currentLVL = skills.getCurrentLevel(Skills.THIEVING);
		gainedXP = currentXP - startXP;
		gainedLVL = currentLVL - startLvl;
		xpPerHour = (int) ((3600000.0 / (double) runTime) * gainedXP);
		pickpocketsHour = (int) ((3600000.0 / (double) runTime) * pickpockets);

		if (game.getCurrentTab() == Game.TAB_INVENTORY) {
			if (showInventory == false) {
				g.setColor(new Color(0, 0, 0, 190));
				g.fillRoundRect(555, 210, 175, 250, 0, 0);

				g.setColor(Color.RED);
				g.draw3DRect(555, 210, 175, 250, true);

				g.setColor(Color.WHITE);
				int[] coords = new int[] { 225, 240, 255, 270, 285, 300, 315,
						330, 345, 360, 375, 390, 405, 420, 435, 450, 465, 480 };
				g.setColor(Color.RED);
				g.setFont(new Font("Segoe Print", Font.BOLD, 14));
				g.drawString("Master Farmer Stealer", 561, coords[0]);
				g.drawString("Version: " + properties.version(), 561, coords[1]);
				g.setFont(new Font("Lucida Calligraphy", Font.PLAIN, 12));
				g.setColor(Color.LIGHT_GRAY);
				g.drawString("Run Time: " + hours + ":" + minutes + ":"
						+ seconds, 561, coords[2]);
				g.setColor(Color.RED);
				g.drawString(pickpockets + " pickpockets", 561, coords[4]);
				g.setColor(Color.LIGHT_GRAY);
				g.drawString("pickpockets/hour: " + pickpocketsHour, 561,
						coords[5]);
				g.setColor(Color.RED);
				g.drawString("XP Gained: " + gainedXP, 561, coords[6]);
				g.setColor(Color.LIGHT_GRAY);
				g.drawString("XP/Hour: " + xpPerHour, 561, coords[7]);
				g.setColor(Color.RED);
				g.drawString("Your level is " + currentLVL, 561, coords[8]);
				g.setColor(Color.LIGHT_GRAY);
				g.drawString("Lvls Gained: " + gainedLVL, 561, coords[9]);
				g.setColor(Color.RED);
				g.drawString("failed " + failPickpocket + " times", 561,
						coords[10]);
				g.drawString(
						"XP To Next Level: "
								+ skills.getExpToNextLevel(Skills.THIEVING),
						561, coords[12]);
				g.setColor(Color.LIGHT_GRAY);
				g.drawString(
						"% To Next Level: "
								+ skills.getPercentToNextLevel(Skills.THIEVING),
						561, coords[13]);
				g.setColor(Color.RED);
				g.drawString("Status: " + status, 561, coords[14]);
				g.setColor(Color.LIGHT_GRAY);
				g.drawString("By Bloddyharry", 561, coords[15]);
			}
			g.setFont(new Font("Lucida Calligraphy", Font.PLAIN, 12));
			g.setColor(new Color(0, 0, 0, 195));
			g.fillRoundRect(6, 315, 120, 20, 0, 0);
			g.setColor(Color.red);
			g.draw3DRect(6, 315, 120, 20, true);
			g.setColor(Color.white);
			g.drawString("See inventory", 10, 330);

			Point m = mouse.getLocation();
			if (m.x >= 6 && m.x < 6 + 120 && m.y >= 315 && m.y < 315 + 30) {
				showInventory = true;
			} else {
				showInventory = false;
			}
		}
		if (hours == wantedHours && minutes == wantedMinutes
				&& seconds == wantedSeconds && finishAt) {
			logOut = true;
			if (canSay) {
				log(wantedHours + " hours " + wantedMinutes + " minutes "
						+ wantedSeconds + " seconds past, stopping script.");
				canSay = false;
			}
		}
		if (normal != null) {
			final int mouse_x = mouse.getLocation().x;
			final int mouse_y = mouse.getLocation().y;
			final int mouse_x2 = mouse.getPressLocation().x;
			final int mouse_y2 = mouse.getPressLocation().y;
			final long mpt = System.currentTimeMillis() - mouse.getPressTime();
			if (mouse.getPressTime() == -1 || mpt >= 1000) {
				g.drawImage(normal, mouse_x - 8, mouse_y - 8, null);
			}
			if (mpt < 1000) {
				g.drawImage(clicked, mouse_x2 - 8, mouse_y2 - 8, null);
				g.drawImage(normal, mouse_x - 8, mouse_y - 8, null);
			}
		}
	}

	public void messageReceived(MessageEvent e) {
		if (e.getID() != MessageEvent.MESSAGE_ACTION)
			return;
		final String serverString = e.getMessage();
		if (serverString.contains("You pick the")) {
			pickpockets++;
		}
		if (serverString.contains("You can't do that")) {
			walking.walkTileMM(bankTile);
			sleep(random(1500, 2000));
		}
		if (serverString.contains("You fail")) {
			failPickpocket++;
			failed = true;
		}
		if (serverString.contains("You eat")) {
			foodEated++;
		}
		if (serverString.contains("You've just advanced")) {
			log("Congrats on level up, Screenshot taken!");
			sleep(random(1500, 2500));
			if (interfaces.canContinue()) {
				interfaces.clickContinue();
			}
			gainedLvls++;
			if (skills.getCurrentLevel(Skills.THIEVING) == wantedLevel
					&& finishWhenLevel) {
				logOut = true;
				if (canSay) {
					log("Reached level "
							+ skills.getCurrentLevel(Skills.THIEVING)
							+ " in thieving, stopping script");
					canSay = false;
				}
			}
		}

	}

	public class cameraHeight extends Thread {

		private boolean isActive = false;
		private boolean pause = false;

		@Override
		public void run() {
			while (this.isActive) {
				if (random(1, 25000) == 342) {
					try {
						if (this.pause) {
							while (this.pause) {
								Thread.sleep(3000, 7000);
							}
						}
						char key = KeyEvent.VK_UP;
						if (camera.getPitch() >= random(35, 100)) {
							key = KeyEvent.VK_DOWN;
						}
						keyboard.pressKey(key);
						Thread.sleep(random(75, 500));
						keyboard.releaseKey(key);
					} catch (Exception e) {
						camera.setPitch(random(20, 100));
					}
					try {
						Thread.sleep(random(random(1000, 5000),
								random(11000, 17000)));
					} catch (Exception e) {
					}
				}
			}
		}
	}

	public class cameraRotate extends Thread {

		private boolean isActive = false;
		private boolean pause = false;

		@Override
		public void run() {
			while (this.isActive) {
				if (random(1, 25000) == 342) {
					try {
						if (this.pause) {
							while (this.pause) {
								Thread.sleep(3000, 7000);
							}
						}
						char key = KeyEvent.VK_RIGHT;
						if (random(1, 5) == 2) {
							key = KeyEvent.VK_LEFT;
						}
						keyboard.pressKey(key);
						Thread.sleep(random(350, 1300));
						keyboard.releaseKey(key);
					} catch (Exception e) {
						camera.setAngle(random(0, 360));
					}
					try {
						Thread.sleep(random(random(1000, 5000),
								random(11000, 17000)));
					} catch (Exception e) {
					}
				}
			}
		}
	}

	public class BloddyMasterFarmerGUI extends JFrame {
		/**
		 *
		 */
		private static final long serialVersionUID = 1L;

		public BloddyMasterFarmerGUI() {
			initComponents();
		}

		private void button2ActionPerformed(ActionEvent e) {
			guiWait = false;
			guiExit = true;
			dispose();
		}

		private void button1ActionPerformed(ActionEvent e) {
			FOODID = Integer.parseInt(textField1.getText());
			finishAt = checkBox3.isSelected();
			wantedHours = Integer.parseInt(textField3.getText());
			wantedMinutes = Integer.parseInt(textField4.getText());
			wantedSeconds = Integer.parseInt(textField5.getText());
			finishWhenLevel = checkBox1.isSelected();
			wantedLevel = Integer.parseInt(textField2.getText());
			guiWait = false;
			dispose();
		}

		private void checkBox2ActionPerformed(ActionEvent e) {
			if (checkBox2.isSelected()) {
				doDropSeeds = true;
			} else {
				doDropSeeds = false;
			}
		}

		private void checkBox1ActionPerformed(ActionEvent e) {
			if (checkBox1.isSelected()) {
				textField2.setEnabled(true);
			} else {
				textField2.setEnabled(false);
			}
		}

		private void checkBox3ActionPerformed(ActionEvent e) {
			if (checkBox3.isSelected()) {
				label7.setEnabled(true);
				label8.setEnabled(true);
				label9.setEnabled(true);
				textField3.setEnabled(true);
				textField4.setEnabled(true);
				textField5.setEnabled(true);
			} else {
				label7.setEnabled(false);
				label8.setEnabled(false);
				label9.setEnabled(false);
				textField3.setEnabled(false);
				textField4.setEnabled(false);
				textField5.setEnabled(false);
			}
		}

		private void comboBox1ActionPerformed(ActionEvent e) {
			if (comboBox1.getSelectedItem() == "Withdraw-10") {
				withdraw10 = true;
			}
		}

		private void initComponents() {
			// GEN-BEGIN:initComponents
			label1 = new JLabel();
			button2 = new JButton();
			button1 = new JButton();
			tabbedPane1 = new JTabbedPane();
			panel1 = new JPanel();
			label2 = new JLabel();
			label5 = new JLabel();
			label6 = new JLabel();
			checkBox2 = new JCheckBox();
			label4 = new JLabel();
			textField1 = new JTextField();
			label3 = new JLabel();
			label10 = new JLabel();
			comboBox1 = new JComboBox();
			panel2 = new JPanel();
			checkBox1 = new JCheckBox();
			textField2 = new JTextField();
			checkBox3 = new JCheckBox();
			label7 = new JLabel();
			label8 = new JLabel();
			label9 = new JLabel();
			textField3 = new JTextField();
			textField4 = new JTextField();
			textField5 = new JTextField();

			// ======== this ========
			setResizable(false);
			setTitle("Bloddy Master Farmer Stealer GUI");
			Container contentPane = getContentPane();
			contentPane.setLayout(null);

			// ---- label1 ----
			label1.setText("Bloddy Master Farmer Stealer "
					+ properties.version());
			label1.setFont(new Font("Lucida Calligraphy", Font.BOLD, 13));
			label1.setForeground(Color.red);
			contentPane.add(label1);
			label1.setBounds(new Rectangle(new Point(5, 10), label1
					.getPreferredSize()));

			// ---- button2 ----
			button2.setText("Cancel");
			button2.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					button2ActionPerformed(e);
				}
			});
			contentPane.add(button2);
			button2.setBounds(new Rectangle(new Point(140, 210), button2
					.getPreferredSize()));

			// ---- button1 ----
			button1.setText("Start");
			button1.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					button1ActionPerformed(e);
					button1ActionPerformed(e);
				}
			});
			contentPane.add(button1);
			button1.setBounds(210, 210, 58, button1.getPreferredSize().height);

			// ======== tabbedPane1 ========
			{

				// ======== panel1 ========
				{
					panel1.setLayout(null);

					// ---- label2 ----
					label2.setText("Start in the draynor bank with all your seeds \n");
					label2.setFont(new Font("Comic Sans MS", Font.PLAIN, 11));
					panel1.add(label2);
					label2.setBounds(new Rectangle(new Point(5, 5), label2
							.getPreferredSize()));

					// ---- label5 ----
					label5.setText("and food in one tab.");
					label5.setFont(new Font("Comic Sans MS", Font.PLAIN, 11));
					panel1.add(label5);
					label5.setBounds(new Rectangle(new Point(5, 25), label5
							.getPreferredSize()));

					// ---- label6 ----
					label6.setText("Also make sure you fill in the right food ID.");
					label6.setFont(new Font("Comic Sans MS", Font.PLAIN, 11));
					panel1.add(label6);
					label6.setBounds(new Rectangle(new Point(5, 45), label6
							.getPreferredSize()));

					// ---- checkBox2 ----
					checkBox2.setText("Drop junkseeds");
					checkBox2
							.setFont(new Font("Comic Sans MS", Font.PLAIN, 11));
					checkBox2.setSelected(true);
					checkBox2.addActionListener(new ActionListener() {
						public void actionPerformed(ActionEvent e) {
							checkBox2ActionPerformed(e);
						}
					});
					panel1.add(checkBox2);
					checkBox2.setBounds(new Rectangle(new Point(3, 60),
							checkBox2.getPreferredSize()));

					// ---- label4 ----
					label4.setText("Food ID*:");
					label4.setFont(new Font("Comic Sans MS", Font.PLAIN, 11));
					panel1.add(label4);
					label4.setBounds(5, 100, 265, 18);

					// ---- textField1 ----
					textField1.setText("7946");
					panel1.add(textField1);
					textField1.setBounds(140, 100, 90,
							textField1.getPreferredSize().height);

					// ---- label3 ----
					label3.setText("*In RSBot client go to: view > inventory");
					label3.setFont(label3.getFont().deriveFont(
							label3.getFont().getStyle() | Font.ITALIC,
							label3.getFont().getSize() - 2f));
					panel1.add(label3);
					label3.setBounds(new Rectangle(new Point(10, 125), label3
							.getPreferredSize()));

					// ---- label10 ----
					label10.setText("Withdraw food:");
					label10.setFont(new Font("Comic Sans MS", Font.PLAIN, 11));
					panel1.add(label10);
					label10.setBounds(new Rectangle(new Point(4, 85), label10
							.getPreferredSize()));

					// ---- comboBox1 ----
					comboBox1.setModel(new DefaultComboBoxModel(new String[] {
							"Withdraw-5", "Withdraw-10" }));
					comboBox1.addActionListener(new ActionListener() {
						public void actionPerformed(ActionEvent e) {
							comboBox1ActionPerformed(e);
						}
					});
					panel1.add(comboBox1);
					comboBox1.setBounds(140, 80, 90,
							comboBox1.getPreferredSize().height);

					{ // compute preferred size
						Dimension preferredSize = new Dimension();
						for (int i = 0; i < panel1.getComponentCount(); i++) {
							Rectangle bounds = panel1.getComponent(i)
									.getBounds();
							preferredSize.width = Math.max(bounds.x
									+ bounds.width, preferredSize.width);
							preferredSize.height = Math.max(bounds.y
									+ bounds.height, preferredSize.height);
						}
						Insets insets = panel1.getInsets();
						preferredSize.width += insets.right;
						preferredSize.height += insets.bottom;
						panel1.setMinimumSize(preferredSize);
						panel1.setPreferredSize(preferredSize);
					}
				}
				tabbedPane1.addTab("Main", panel1);

				// ======== panel2 ========
				{
					panel2.setLayout(null);

					// ---- checkBox1 ----
					checkBox1.setText("When reached level:");
					checkBox1.addActionListener(new ActionListener() {
						public void actionPerformed(ActionEvent e) {
							checkBox1ActionPerformed(e);
						}
					});
					panel2.add(checkBox1);
					checkBox1.setBounds(new Rectangle(new Point(5, 10),
							checkBox1.getPreferredSize()));

					// ---- textField2 ----
					textField2.setText("0");
					textField2.setEnabled(false);
					panel2.add(textField2);
					textField2.setBounds(140, 12, 40,
							textField2.getPreferredSize().height);

					// ---- checkBox3 ----
					checkBox3.setText("After:");
					checkBox3.addActionListener(new ActionListener() {
						public void actionPerformed(ActionEvent e) {
							checkBox3ActionPerformed(e);
						}
					});
					panel2.add(checkBox3);
					checkBox3.setBounds(new Rectangle(new Point(5, 45),
							checkBox3.getPreferredSize()));

					// ---- label7 ----
					label7.setText("H:");
					label7.setEnabled(false);
					panel2.add(label7);
					label7.setBounds(new Rectangle(new Point(30, 75), label7
							.getPreferredSize()));

					// ---- label8 ----
					label8.setText("M:");
					label8.setEnabled(false);
					panel2.add(label8);
					label8.setBounds(new Rectangle(new Point(95, 75), label8
							.getPreferredSize()));

					// ---- label9 ----
					label9.setText("S:");
					label9.setEnabled(false);
					panel2.add(label9);
					label9.setBounds(new Rectangle(new Point(165, 75), label9
							.getPreferredSize()));

					// ---- textField3 ----
					textField3.setEnabled(false);
					textField3.setText("0");
					panel2.add(textField3);
					textField3.setBounds(50, 74, 30,
							textField3.getPreferredSize().height);

					// ---- textField4 ----
					textField4.setEnabled(false);
					textField4.setText("0");
					panel2.add(textField4);
					textField4.setBounds(115, 74, 30, 20);

					// ---- textField5 ----
					textField5.setEnabled(false);
					textField5.setText("0");
					panel2.add(textField5);
					textField5.setBounds(185, 74, 30, 20);

					{ // compute preferred size
						Dimension preferredSize = new Dimension();
						for (int i = 0; i < panel2.getComponentCount(); i++) {
							Rectangle bounds = panel2.getComponent(i)
									.getBounds();
							preferredSize.width = Math.max(bounds.x
									+ bounds.width, preferredSize.width);
							preferredSize.height = Math.max(bounds.y
									+ bounds.height, preferredSize.height);
						}
						Insets insets = panel2.getInsets();
						preferredSize.width += insets.right;
						preferredSize.height += insets.bottom;
						panel2.setMinimumSize(preferredSize);
						panel2.setPreferredSize(preferredSize);
					}
				}
				tabbedPane1.addTab("On Finish", panel2);

			}
			contentPane.add(tabbedPane1);
			tabbedPane1.setBounds(5, 35, 265, 170);

			contentPane.setPreferredSize(new Dimension(290, 270));
			setSize(290, 270);
			setLocationRelativeTo(getOwner());
			// GEN-END:initComponents
		}

		// GEN-BEGIN:variables
		private JLabel label1;
		private JButton button2;
		private JButton button1;
		private JTabbedPane tabbedPane1;
		private JPanel panel1;
		private JLabel label2;
		private JLabel label5;
		private JLabel label6;
		private JCheckBox checkBox2;
		private JLabel label4;
		private JTextField textField1;
		private JLabel label3;
		private JPanel panel2;
		private JLabel label10;
		private JComboBox comboBox1;
		private JCheckBox checkBox1;
		private JTextField textField2;
		private JCheckBox checkBox3;
		private JLabel label7;
		private JLabel label8;
		private JLabel label9;
		private JTextField textField3;
		private JTextField textField4;
		private JTextField textField5;
		// GEN-END:variables
	}
}