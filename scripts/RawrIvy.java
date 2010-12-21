import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.net.URL;
import java.util.ArrayList;
import javax.imageio.ImageIO;
import javax.swing.JOptionPane;

import org.rsbot.event.events.MessageEvent;
import org.rsbot.event.listeners.PaintListener;
import org.rsbot.event.listeners.MessageListener;
import org.rsbot.script.Script;
import org.rsbot.script.ScriptManifest;
import org.rsbot.script.methods.*;
import org.rsbot.script.wrappers.*;

@ScriptManifest(authors = "Rawr", name = "Rawr Ivy Pro", keywords = "Woodcutting", version = 1.1, 
		description = "The best Ivy Chopper out there.")
public class RawrIvy extends Script implements PaintListener, MessageListener {
	antiban AntiBan;
	public String STATE = "Loading";
	public int AMOUNT_CHOPPED = 0, NESTS_COLLECTED = 0;
	public int[] NEST_ID = {5070, 5071, 5072, 5073, 5074, 5075, 5076, 7413, 11966};
	public int[] IVY_ID = {46318, 46320, 46322, 46324};
	public int[] HATCHETS = {1349, 1351, 1353, 1355, 1357, 1359, 1361, 6739};
	public RSObject LAST_IVY, CURRENT_IVY;
	public RSArea BANK_AREA;
	public RSArea VP_AREA = new RSArea(new RSTile(3161, 3486), new RSTile(3168, 3493)),
			VW_AREA = new RSArea(new RSTile(3250, 3418), new RSTile(3257, 3423)),
			NF_AREA = new RSArea(new RSTile(2944, 3366), new RSTile(2950, 3371)),
			SF_AREA = new RSArea(new RSTile(3090, 3240), new RSTile(3095, 3246)),
			ARD_AREA = new RSArea(new RSTile(2649, 3280), new RSTile(2657, 3287)),
			YAN_AREA = new RSArea(new RSTile(2609, 3090), new RSTile(2615, 3095)),
			CW_AREA = new RSArea(new RSTile(2440, 3082), new RSTile(2445, 3087));
	public RSTile IVY_TILE;
	public RSTile VP_IVY = new RSTile(3218, 3498),
			VW_IVY = new RSTile(3233, 3459),
			NF_IVY = new RSTile(3015, 3392),
			SF_IVY = new RSTile(3048, 3328),
			ARD_IVY = new RSTile(2622, 3307),
			YAN_IVY = new RSTile(2594, 3111),
			CW_IVY = new RSTile(2426, 3068);
	public RSTile[] IVY_TO_BANK;
	public RSTile[] VP_IVY_TO_BANK = {new RSTile(3217, 3500), new RSTile(3204, 3502), new RSTile(3195, 3494),
			new RSTile(3183, 3490), new RSTile(3173, 3491), new RSTile(3167, 3489)},
			VW_IVY_TO_BANK = {new RSTile(3232, 3459), new RSTile(3240, 3458), new RSTile(3246, 3449),
					new RSTile(3246, 3436), new RSTile(3253, 3428), new RSTile(3253, 3420)},
			NF_IVY_TO_BANK = {new RSTile(3015, 3395), new RSTile(3004, 3400), new RSTile(2992, 3398),
					new RSTile(2978, 3398), new RSTile(2965, 3391), new RSTile(2954, 3382), new RSTile(2946, 3371)},
			SF_IVY_TO_BANK = {new RSTile(3052, 3325), new RSTile(3064, 3323), new RSTile(3076, 3315),
					new RSTile(3072, 3301), new RSTile(3076, 3288), new RSTile(3075, 3277), new RSTile(3079, 3263),
					new RSTile(3085, 3250), new RSTile(3092, 3243)},
			ARD_IVY_TO_BANK = {new RSTile(2626, 3304), new RSTile(2632, 3292), new RSTile(2642, 3284),
					new RSTile(2654, 3283)},
			YAN_IVY_TO_BANK = {new RSTile(2595, 3111), new RSTile(2605, 3102), new RSTile(2613, 3092)},
			CW_IVY_TO_BANK = {new RSTile(2427, 3067), new RSTile(2434, 3067), new RSTile(2445, 3070),
					new RSTile(2444, 3083)};
	public long startTime = System.currentTimeMillis(), activityTimer = System.currentTimeMillis(),
		antiban_timer = System.currentTimeMillis();
	public int START_XP, START_LVL;
	public Image BKG;
	public boolean renew = true;
	public int mode = 1;
	public int CURR_LVL = 0;
	public int NXT_LVL = 0;
	public int percent;
	BufferedImage normal = null;
	BufferedImage clicked = null;
	
	public String[] locations = { "Varrock Palace", "Varrock Wall", "N Fally", "S Fally"
			, "Ardougne", "Yanille", "CWars" };
	public String LOCATION;
	
	public void selectLocation() {
		LOCATION = (String) JOptionPane.showInputDialog(null, "Select Chopping Location...",
	    		"Select Chopping Location...", JOptionPane.QUESTION_MESSAGE, null,
	    		locations, locations[0]);
		if (LOCATION.equals("Varrock Palace")) {
			BANK_AREA = VP_AREA;
			IVY_TO_BANK = VP_IVY_TO_BANK;
			IVY_TILE = VP_IVY;
		}
		if (LOCATION.equals("Varrock Wall")) {
			BANK_AREA = VW_AREA;
			IVY_TO_BANK = VW_IVY_TO_BANK;
			IVY_TILE = VW_IVY;
		}
		if (LOCATION.equals("N Fally")) {
			BANK_AREA = NF_AREA;
			IVY_TO_BANK = NF_IVY_TO_BANK;
			IVY_TILE = NF_IVY;
		}
		if (LOCATION.equals("S Fally")) {
			BANK_AREA = SF_AREA;
			IVY_TO_BANK = SF_IVY_TO_BANK;
			IVY_TILE = SF_IVY;
		}
		if (LOCATION.equals("Ardougne")) {
			BANK_AREA = ARD_AREA;
			IVY_TO_BANK = ARD_IVY_TO_BANK;
			IVY_TILE = ARD_IVY;
		}
		if (LOCATION.equals("Yanille")) {
			BANK_AREA = YAN_AREA;
			IVY_TO_BANK = YAN_IVY_TO_BANK;
			IVY_TILE = YAN_IVY;
		}
		if (LOCATION.equals("CWars")) {
			BANK_AREA = CW_AREA;
			IVY_TO_BANK = CW_IVY_TO_BANK;
			IVY_TILE = CW_IVY;
		}
	}

	public boolean onStart() {
		try {
			BKG = ImageIO.read(new URL("http://a.imageshack.us/img816/9064/ivychopperpaintfinal.png"));
			normal = ImageIO.read(new URL("http://imgur.com/i7nMG.png"));
			clicked = ImageIO.read(new URL("http://imgur.com/8k9op.png"));
		} catch (final java.io.IOException e) {
			e.printStackTrace();
		}
		AntiBan = new antiban();
		selectLocation();
		startTime = System.currentTimeMillis();
		camera.setPitch(true);
		return true;
	}

	public void onFinish() {
		env.saveScreenshot(true);
		long millis = System.currentTimeMillis() - startTime;
		long hours = millis / (1000 * 60 * 60);
		millis -= hours * (1000 * 60 * 60);
		long minutes = millis / (1000 * 60);
		millis -= minutes * (1000 * 60);
		long seconds = millis / 1000;
		log.info("Progress Report...");
		log.info("Script ran for : " + hours + ":" + minutes + ":" + seconds + ".");
		log.info("Ivy chopped : " + AMOUNT_CHOPPED + ".");
		log.info("Nests collected : " + NESTS_COLLECTED + ".");
		log.info("Ending WC Level : " + skills.getCurrentLevel(Skills.WOODCUTTING) + ".");
	}

	public RSObject getIvy() {
		ArrayList<RSObject> ivys = new ArrayList<RSObject>();
		for (int x = -11; x < 12; x++) {
			for (int y = -11; y < 12; y++) {
				RSObject[] all = objects.getAllAt(new RSTile(players.getMyPlayer().getLocation().getX() + x, getMyPlayer().getLocation().getY() + y));
				outer:
				for (RSObject obj : all) {
					if (obj != null) {
						int id = obj.getID();
						for (int ivyID : IVY_ID) {
							if (id == ivyID) {
								ivys.add(obj);
								continue outer;
							}
						}
					}
				}
			}
		}
		return ivys.size() > 0 ? ivys.get(random(0, ivys.size() - 1)) : null;
	}

	public boolean isIvy(RSObject oldIvy) {
		if (oldIvy == null) {
			return false;
		}
		RSObject[] all = objects.getAllAt(oldIvy.getLocation());
		for (RSObject obj : all) {
			if (obj != null) {
				int id = obj.getID();
				for (int ivyID : IVY_ID) {
					if (id == ivyID) {
						return true;
					}
				}
			}
		}
		return false;
	}

	public void cameraControl() {
		//NORTH
		if (LOCATION.equals("S Fally") || LOCATION.equals("CWars")) {
			camera.setAngle(random(345, 360));
		}
		//SOUTH
		if (LOCATION.equals("Varrock Palace") || LOCATION.equals("N Fally") || LOCATION.equals("Yanille")) {
			camera.setAngle(random(170, 185));
		}
		//EAST
		if (LOCATION.equals("Varrock Wall") || LOCATION.equals("Taverly")) {
			camera.setAngle(random(260, 275));
		}
		//WEST
		if (LOCATION.equals("Ardougne")) {
			camera.setAngle(random(80, 95));
		}
	}

	public void openBank() {
		if (objects.getNearest(Bank.BANK_BOOTHS) != null) {
			if (objects.getNearest(Bank.BANK_BOOTHS).doAction("Use-quickly"))
				waitForIF(interfaces.get(Bank.INTERFACE_BANK), 2000);
		}
		if (objects.getNearest(Bank.BANK_CHESTS) != null) {
			if (objects.getNearest(Bank.BANK_CHESTS).doAction("Use"))
				waitForIF(interfaces.get(Bank.INTERFACE_BANK), 2000);
		}
		if (npcs.getNearest(Bank.BANKERS) != null) {
			RSNPC Banker = npcs.getNearest(Bank.BANKERS);
			RSTile BankerTile = Banker.getLocation();
			if (tiles.doAction(BankerTile, "Bank"))
				waitForIF(interfaces.get(Bank.INTERFACE_BANK), 2000);
		}
	}
	
	public boolean busy() {
		if(System.currentTimeMillis() - activityTimer < random(2000, 2200)) {
			return true;
		}
		return false;
	}

	@Override
	public int loop() {
		mouse.setSpeed(random(7, 9));
		RSObject IVY = CURRENT_IVY != null && isIvy(CURRENT_IVY) ? CURRENT_IVY : (CURRENT_IVY = getIvy());
		RSGroundItem NEST = groundItems.getNearest(NEST_ID);
		//COLLECTING NEST
		if (NEST != null && !inventory.isFull()) {
			STATE = "Collecting nest";
			if (NEST.doAction("Take"))
				sleep(1000, 1500);
		}
		//CLICKING IVY
		if (!inventory.isFull()) {
			if (IVY != null && !busy()) {
				cameraControl();
				if (IVY.doAction("Chop Ivy")) {
					STATE = "Clicking the Ivy";
					LAST_IVY = IVY;
					sleep(2000, 3000);
				} else {
					return 1000;
				}
			} else if (calc.distanceTo(IVY_TILE) >= 7) {
				STATE = "Walking back to Ivy";
				walking.walkPathMM(walking.reversePath(IVY_TO_BANK), 1, 1);
				waitToGetClose(random(3, 4), random(2500, 3000));
			}
		}
		//BANKING
		if (inventory.isFull()) {
			if (LOCATION.equals("Taverly")) {
				log.info("Sorry, your inventory is full... Stopping script.");
				sleep(1500, 2500);
				stopScript();
			} else {
				if (!BANK_AREA.contains(players.getMyPlayer().getLocation())) {
					STATE = "Walking to Bank";
					walking.walkPathMM(IVY_TO_BANK, 1, 1);
					waitToGetClose(random(3, 4), random(2500, 3000));
				} else if (BANK_AREA.contains(players.getMyPlayer().getLocation())) {
					if (!bank.isOpen()) {
						STATE = "Opening bank";
						openBank();
					} else if (bank.isOpen()) {
						int RANDOM = random(1, 10);
						if (inventory.getCount() > 1) {
							STATE = "Depositing inventory";
							if (bank.depositAllExcept(HATCHETS))
								sleep(600, 800);
							if (RANDOM == 5) {
								bank.close();
							}
						}
					}
				}
			}
		}
		//CHOPPING IVY
		if (IVY != null && players.getMyPlayer().getAnimation() != -1) {
			STATE = "Chopping the Ivy";
			activityTimer = System.currentTimeMillis();
		}
		
		if (STATE.equalsIgnoreCase("Chopping the Ivy")) {
			AntiBan.run();
		}
		return 100;
	}
	//WAITING METHODS
	public boolean waitForIF(RSInterface iface, int timeout) {
		long start = System.currentTimeMillis();

		while (System.currentTimeMillis() - start < timeout) {
			if (iface.isValid()) {
					return true;
			}
			sleep(100);
		}
		return false;
	}
	
	public boolean waitToGetClose(int dist, int timeout) {
		long start = System.currentTimeMillis();
		if(!waitToMove(random(800,1400))) {
			return false;
		}
		while (System.currentTimeMillis() - start < timeout) {
			if(dist >= 0) {
				if(walking.getDestination() != null) {
					try {
						if (calc.distanceTo(walking.getDestination()) <= dist) {
								return true;
						}
					} catch (Exception e) {
						log.severe("Failed to get destination.");
					}
				} else {
					return true;
				}
			} else {
				if(walking.getDestination() != null) {
					try {
						if (calc.tileOnScreen(walking.getDestination())) {
							return true;
						}
					} catch (Exception e) {
						log.severe("Failed to get destination.");
					}
				} else {
					return true;
				}
			}
			sleep(100);
		}		
		return false;
	}
	
	public boolean waitToMove(int timeout) {
		long start = System.currentTimeMillis();	
		while (System.currentTimeMillis() - start < timeout) {
			if (getMyPlayer().isMoving()) {
					return true;
			}
			sleep(50);
		}
		return false;
	}

	///////////////////////////////
	////////ANTI-BAN METHODS///////
	///////////////////////////////
	private class antiban extends Thread {
		final char[] upDownRandom = new char[] { KeyEvent.VK_DOWN, KeyEvent.VK_UP };
		final char[] leftRightRandom = new char[] { KeyEvent.VK_LEFT,KeyEvent.VK_RIGHT };
		final char[] allKeys = new char[] { KeyEvent.VK_LEFT,KeyEvent.VK_RIGHT, KeyEvent.VK_UP,KeyEvent.VK_UP };
		final int random1 = random(0, 2);
		final int random2 = random(0, 2);
		final int random3 = random(0, 4);
		int antiBanRandomValue = random(0, 15);
		private void mouseMovementAntiBan() {
			int mouseRandomValue = random(1, 2);
			try {
				if (mouseRandomValue == 1) {
					mouse.moveRandomly(200);
				} else if (mouseRandomValue == 2) {
					mouse.moveOffScreen();
				}
			} catch (Exception e) { }
		}

		private void cameraMovementAntiBan() {
			try {
				if (random(0, 2) <= 1) {
					keyboard.pressKey(upDownRandom[random1]);
					sleep(600, 800);
		            keyboard.pressKey(leftRightRandom[random2]);
		            sleep(600, 800);
					keyboard.releaseKey(leftRightRandom[random2]);
					sleep(100, 200);
					keyboard.releaseKey(upDownRandom[random1]);
				} else {
					keyboard.pressKey(allKeys[random3]);
					sleep(800, 1000);
					keyboard.releaseKey(allKeys[random3]);
				}
			} catch (Exception e) { }
		}

		private void checkSkillAntiBan() {
			try {
				if (random(5, 7) == 6 && players.getMyPlayer().getAnimation() != -1) {
					if (game.getCurrentTab() != Game.TAB_STATS) {
						game.openTab(Game.TAB_STATS);
						sleep(600, 800);
						skills.doHover(Skills.INTERFACE_WOODCUTTING);
						sleep(1000, 2000);
					}
				}
			} catch (Exception e) { }
		}

		private void clickPlayerAntiBan() {
			if (random(1, 3) == 2) {
				try {
					RSPlayer player = players.getNearest(Players.ALL_FILTER);
					if (player != null) {
						mouse.move(player.getScreenLocation(), 5, 5);
						sleep(400, 500);
						mouse.click(false);
						sleep(750, 800);
						mouse.move(random(10, 450), random(10, 495));
					}
				} catch (Exception e) { }
			}
		}

		private void checkFriendsAntiBan() {
			try {
				if (game.getCurrentTab() != Game.TAB_FRIENDS) {
					if (random(0, 2) == 1) {
						game.openTab(Game.TAB_FRIENDS);
						sleep(1000, 2000);
					}
				}
			} catch (Exception e) { }
		}

        @Override
        public void run() {
			try {
				if ((System.currentTimeMillis() - antiban_timer) >= (random(30, 90) * random(800, 1200)) 
						&& !players.getMyPlayer().isMoving() && !players.getMyPlayer().isInCombat()) {
					findAntiBan();
					antiban_timer = System.currentTimeMillis();
            	}
			} catch (Exception e) { }
		}

        private void findAntiBan() {
			antiBanRandomValue = random(0, 10);
			if (antiBanRandomValue < 5) {
				mouseMovementAntiBan();
			} else if (antiBanRandomValue == 6) {
				cameraMovementAntiBan();	
			} else if (antiBanRandomValue == 7) {
				checkSkillAntiBan();
			} else if (antiBanRandomValue == 8) {
				clickPlayerAntiBan();
			} else if (antiBanRandomValue == 9) {
				checkFriendsAntiBan();
			}
        }
    }

	public void drawMouse(final Graphics g) {
		if (normal != null) {
			final double mouseX = mouse.getLocation().getX() - 8,
					mouseY = mouse.getLocation().getY() - 8;
			final double mousePressX = mouse.getPressLocation().getX() - 8,
					mousePressY = mouse.getPressLocation().getY() - 8;
			if (System.currentTimeMillis() - mouse.getPressTime() < 400) {
				g.drawImage(clicked, (int) mousePressX, (int) mousePressY, null);
			}
			g.drawImage(normal, (int) mouseX, (int) mouseY, null);
		}
	}

	public void onRepaint(Graphics g) {
		//MOUSE
		drawMouse(g);
		//TIME STUFF
		long millis = System.currentTimeMillis() - startTime;
		long hours = millis / (1000 * 60 * 60);
		millis -= hours * (1000 * 60 * 60);
		long minutes = millis / (1000 * 60);
		millis -= minutes * (1000 * 60);
		long seconds = millis / 1000;
		// XP / LEVELS
		int XP_GAINED;
		int LVLS_GAINED;
		if (START_XP == 0) {
			START_XP = skills.getCurrentExp(Skills.WOODCUTTING);
		}
		XP_GAINED = skills.getCurrentExp(Skills.WOODCUTTING) - START_XP;
		if (START_LVL == 0) {
			START_LVL = skills.getRealLevel(Skills.WOODCUTTING);
		}
		CURR_LVL = skills.getRealLevel(Skills.WOODCUTTING);
		NXT_LVL = skills.getRealLevel(Skills.WOODCUTTING) + 1;
		LVLS_GAINED = skills.getRealLevel(Skills.WOODCUTTING) - START_LVL;
		int XP_TNL = skills.getExpToNextLevel(Skills.WOODCUTTING);
		int XP_HOUR = (int) ((XP_GAINED) * 3600000D / (System.currentTimeMillis() - startTime));
		percent = skills.getPercentToNextLevel(Skills.WOODCUTTING);
		g.drawImage(BKG, 13, 4, null);
		g.setColor(Color.WHITE);
		g.setFont(new Font("Verdana", 0, 13));
		g.drawString("" + hours + ":" + minutes + ":" + seconds + ".", 311, 82);
		g.setFont(new Font("Verdana", 0, 9));
		g.drawString("Currently : " + STATE, 282, 105);
		g.drawString("Collected : " + NESTS_COLLECTED + ".", 311, 129);
		g.setColor(new Color(0, 0, 0));
		g.fillRect(96, 61, 180, 16);//Black box
		g.drawRect(95, 60, 181, 17); //FRAME
		g.setColor(new Color(51, 51, 51));
		g.fillRect(96, 61, 180, 8);//Gray box
		g.setColor(new Color(10, 150, 10, 130));
		g.fillRect(97, 62, (int) (percent * 178 / 100.0), 14);
		g.setColor(Color.WHITE);
		g.drawString("" + percent + "% to " + NXT_LVL + " WC" + " - " + XP_TNL / 1000 + "k XP", 124, 73);
		try {
			if (XP_HOUR > 0) {
				long sTNL = (XP_TNL) / (XP_HOUR / 3600);
				long hTNL = sTNL / (60 * 60);
				sTNL -= hTNL * (60 * 60);
				long mTNL = sTNL / 60;
				sTNL -= mTNL * 60;
				g.drawString("Next level in: " + hTNL + ":" + mTNL + ":" + sTNL, 95, 89);
			} else {
				g.drawString("Next level in: 0:0:0", 95, 89);
			}
		} catch (Exception e) {
			g.drawString("Next level in: -1:-1:-1", 95, 89);
		}
		g.setColor(Color.ORANGE);
		g.drawString("Chopped: " + AMOUNT_CHOPPED + " Ivy.", 95, 108);
		g.setColor(Color.WHITE);
		g.drawString("XP: " + XP_GAINED + ".  (" + XP_HOUR / 1000 + "k XP/H)", 95, 120);
		g.setColor(Color.GREEN);
		g.drawString("WC Level : " + CURR_LVL + " [ " + LVLS_GAINED + " ].", 95, 132);
	}
	
	public void messageReceived(final MessageEvent e) {
		final String message = e.getMessage();
		if (message.contains("chop away some")) AMOUNT_CHOPPED++;
		if (message.contains("nest falls out")) NESTS_COLLECTED++;
	}
}
