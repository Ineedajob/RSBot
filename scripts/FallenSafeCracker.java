import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Properties;

import javax.imageio.ImageIO;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFormattedTextField;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTabbedPane;
import javax.swing.JTextPane;
import javax.swing.border.LineBorder;

import org.rsbot.event.events.MessageEvent;
import org.rsbot.event.listeners.MessageListener;
import org.rsbot.event.listeners.PaintListener;
import org.rsbot.script.Script;
import org.rsbot.script.ScriptManifest;
import org.rsbot.script.methods.Bank;
import org.rsbot.script.methods.Game;
import org.rsbot.script.methods.Skills;
import org.rsbot.script.util.Timer;
import org.rsbot.script.wrappers.RSInterface;
import org.rsbot.script.wrappers.RSItem;
import org.rsbot.script.wrappers.RSModel;
import org.rsbot.script.wrappers.RSNPC;
import org.rsbot.script.wrappers.RSObject;
import org.rsbot.script.wrappers.RSPlayer;
import org.rsbot.script.wrappers.RSTile;
import org.rsbot.util.GlobalConfiguration;

/* @Updated 2.15.2011.
 * 
 * Updated the banking method.
 * 
 * Added a temp. fix for prices.
 */

@ScriptManifest(authors = { "Fallen" }, keywords = "Thieving", name = "Fallen's Safe Cracker", version = 4.3, description = "Cracks safes at Rogue Den's.")
public class FallenSafeCracker extends Script implements PaintListener,
		MessageListener, MouseListener, MouseMotionListener {

	private enum State {
		WALKTOSAFES, SMALLWALK, CRACK, WALKTOBANK, OPENBANK, BANK
	}

	// Mostly paint int's
	private long runTime = 0;
	private long startTime = 0;
	private long timeLeft = 0;
	private long seconds = 0;
	private long minutes = 0;
	private long millis = 0;
	private long hours = 0;
	private int startXP;
	private int startLevel;
	private int levelsGained;
	private int currentLevel;
	private Image title;
	private int GainedProfit;
	private int XPGained = 0;
	private int toNextLvl;
	private int nextLvl;
	private int XPToLevel;
	private double SuccessRate;
	private boolean setTime = false;
	private boolean paint = true;
	private boolean RT = false;
	private boolean extra = true;
	private boolean renew = true;
	private long timerStart = 0;
	private long pauseTimer = 0;
	private long tempTimer = 0;
	private long tempTimer2 = 0;
	private boolean defined = false, gathered = false, switching = false,
			waitingForMovement = false;
	private Timer switchTimer = new Timer(0);
	private int browseCounter = 0;
	private final boolean logIfFull = true;

	private boolean UseStethoscope;

	private boolean randomized = false;
	private boolean North = false;
	private boolean South = false;
	private boolean SouthLeft = false;
	private boolean SouthRight = false;
	private boolean NorthLeft = false;
	private boolean NorthRight = false;

	private int getX;
	private int getY;
	private final int Percentage = 60;

	private Timer expTimeOut = new Timer(1000 * 60 * 5);
	private int tempExp = 0;

	private int CrackState = 0;
	private int Trapped = 0;
	private int Cracked = 0;

	private int FoodID;
	private final int[] FoodIDS = { 1895, 1893, 1891, 4293, 2142, 291, 2140,
			3228, 9980, 7223, 6297, 6293, 6295, 6299, 7521, 9988, 7228, 2878,
			7568, 2343, 1861, 13433, 315, 325, 319, 3144, 347, 355, 333, 339,
			351, 329, 3381, 361, 10136, 5003, 379, 365, 373, 7946, 385, 397,
			391, 3369, 3371, 3373, 2309, 2325, 2333, 2327, 2331, 2323, 2335,
			7178, 7180, 7188, 7190, 7198, 7200, 7208, 7210, 7218, 7220, 2003,
			2011, 2289, 2291, 2293, 2295, 2297, 2299, 2301, 2303, 1891, 1893,
			1895, 1897, 1899, 1901, 7072, 7062, 7078, 7064, 7084, 7082, 7066,
			7068, 1942, 6701, 6703, 7054, 6705, 7056, 7060, 2130, 1985, 1993,
			1989, 1978, 5763, 5765, 1913, 5747, 1905, 5739, 1909, 5743, 1907,
			1911, 5745, 2955, 5749, 5751, 5753, 5755, 5757, 5759, 5761, 2084,
			2034, 2048, 2036, 2217, 2213, 2205, 2209, 2054, 2040, 2080, 2277,
			2225, 2255, 2221, 2253, 2219, 2281, 2227, 2223, 2191, 2233, 2092,
			2032, 2074, 2030, 2281, 2235, 2064, 2028, 2187, 2185, 2229, 6883,
			1971, 4608, 1883, 1885, 15272 };
	private static int FoodWDAmount;
	private static int EatingPoint;

	private final int Stethoscope = 5560;
	private final int Emerald = 1621;
	private final int Sapphire = 1623;
	private final int Ruby = 1619;
	private final int Diamond = 1617;

	private int Emeralds;
	private int Rubies;
	private int Sapphires;
	private int Diamonds;
	private int EmeraldsINV;
	private int RubiesINV;
	private int SapphiresINV;
	private int DiamondsINV;

	private final int wallsafe = 7236;
	private final int BankerID = 2271;

	// Item prices from G.E
	private int EmeraldPrice = 0;
	private int SapphirePrice = 0;
	private int RubyPrice = 0;
	private int DiamondPrice = 0;

	// Tiles
	private RSTile dest = new RSTile(3056, 4978);
	private final RSTile approxBank = new RSTile(3042, 4970);

	// GUI VARIABLES
	private Properties OPTION_FILE;
	private String GUIString;
	private Object GUIString2;
	private Object GUIString3;
	private Object GUIString4;
	private Object GUIString5;
	private Object GUIString6;
	private boolean TakeAShot = false;
	private boolean timeLimit = false;
	private boolean levelLimit = false;
	private boolean worked = false;
	private static int limitedHours;
	private static int limitedMinutes;
	private static int limitedLevel;
	private boolean change = true, advSwitch = true, clickFirst = false,
			up = false;

	private class Spot {
		public boolean free = false;
		public RSTile location = null;

		public Spot(RSTile t) {
			this.location = t;
		}

		public void setFree(boolean available) {
			this.free = available;
		}
	}

	public Spot NW = new Spot(new RSTile(3055, 4970)), NE = new Spot(
			new RSTile(3057, 4970)), SW = new Spot(new RSTile(3055, 4977)),
			SE = new Spot(new RSTile(3057, 4977));
	public ArrayList<Spot> spots = new ArrayList<Spot>();

	/*-------------------------------------------------------------------
	 * ------------------   P   A   I   N   T   -------------------------
	 ------------------------------------------------------------------*/
	@Override
	public void onRepaint(Graphics g) {
		if (!game.isLoggedIn() || this.isPaused() || !this.isActive()) {
			if (!gathered) {
				timerStart = System.currentTimeMillis();
				gathered = true;
			}
			tempTimer = System.currentTimeMillis() - timerStart - tempTimer2;
		} else {
			tempTimer = 0;
			tempTimer2 = 0;
			gathered = false;
		}
		pauseTimer += tempTimer;
		tempTimer2 += tempTimer;
		if (startXP == 0) {
			startXP = skills.getCurrentExp(Skills.THIEVING);
		}
		if (startLevel == 0) {
			startLevel = skills.getRealLevel(Skills.THIEVING);
		}
		if (RT == true) {
			runTime = System.currentTimeMillis() - startTime;
		} else {
			runTime = System.currentTimeMillis() - startTime - pauseTimer;
		}
		XPGained = (skills.getCurrentExp(Skills.THIEVING) - startXP);
		GainedProfit = ((Emeralds * EmeraldPrice) + (Rubies * RubyPrice)
				+ (Sapphires * SapphirePrice) + (Diamonds * DiamondPrice));
		final int XPHR = (int) ((XPGained) * 3600000D / (runTime));
		final int PROFITHR = (int) ((GainedProfit) * 3600000D / (runTime));
		int attempts = Cracked + Trapped;
		levelsGained = skills.getRealLevel(Skills.THIEVING) - startLevel;
		currentLevel = skills.getRealLevel(Skills.THIEVING);
		millis = runTime;
		hours = millis / (1000 * 60 * 60);
		millis -= hours * (1000 * 60 * 60);
		minutes = millis / (1000 * 60);
		millis -= minutes * (1000 * 60);
		seconds = millis / 1000;
		Point loc = mouse.getLocation();
		g.setColor(new Color(170, 10, 170, 180));
		g.fillRoundRect((int) loc.getX() - 10, (int) loc.getY() - 1, 21, 3, 3,
				3);
		g.fillRoundRect((int) loc.getX() - 1, (int) loc.getY() - 10, 3, 21, 3,
				3);
		if (waitingForMovement) {
			long secondsR = switchTimer.getRemaining() / 1000;
			long hoursR = secondsR / (60 * 60);
			secondsR -= hoursR * (60 * 60);
			long minutesR = secondsR / 60;
			secondsR -= minutesR * 60;
			g.setFont(new Font("Verdana", 0, 9));
			g.setColor(Color.RED);
			g.drawString("A player is under us, waiting - " + minutesR
					+ " minutes, " + secondsR + " seconds.", 140, 470);
		}
		if (switching) {
			g.setFont(new Font("Verdana", 0, 9));
			g.setColor(Color.RED);
			g.drawString("Switching location.", 220, 470);
		}
		// OPTION BOXES
		/*
		 * if(extra) { for(Spot spot : spots) { if(spot.free) { drawTile(g,
		 * spot.location, new Color(20, 180, 20, 100), false, ""); } else {
		 * if(!getMyPlayer().getLocation().equals(spot.location)) { drawTile(g,
		 * spot.location, new Color(180, 20, 20, 100), false, ""); } else {
		 * drawTile(g, spot.location, new Color(20, 20, 180, 100), false, ""); }
		 * } } }
		 */
		g.setFont(new Font("Verdana", 0, 10));
		// Paint
		if (paint) {
			g.setColor(new Color(20, 255, 20, 130));
		} else {
			g.setColor(new Color(255, 255, 255, 130));
		}
		g.fillRect(416, 458, 32, 15);
		g.setColor(new Color(0, 0, 0, 255));
		g.drawRect(416, 458, 32, 15);
		g.setColor(new Color(0, 0, 0, 255));
		g.drawString("Paint", 418, 470);
		// Real-Time
		g.setFont(new Font("Verdana", 0, 10));
		if (RT == true) {
			g.setColor(new Color(156, 20, 170, 130));
		} else {
			g.setColor(new Color(255, 255, 255, 130));
		}
		g.fillRect(448, 458, 32, 15);
		g.setColor(new Color(0, 0, 0, 255));
		g.drawRect(448, 458, 32, 15);
		g.setColor(new Color(0, 0, 0, 255));
		g.drawString("Time", 450, 470);
		// Extra
		g.setFont(new Font("Verdana", 0, 10));
		if (extra == true) {
			g.setColor(new Color(255, 90, 60, 130));
		} else {
			g.setColor(new Color(255, 255, 255, 130));
		}
		g.fillRect(480, 458, 32, 15);
		g.setColor(new Color(0, 0, 0, 255));
		g.drawRect(480, 458, 32, 15);
		g.setColor(new Color(0, 0, 0, 255));
		g.drawString("Extra", 482, 470);

		if (paint) {
			g.drawImage(title, 9, 30, null);
			// Version
			g.setFont(new Font("Arial", 0, 10));
			g.setColor(Color.MAGENTA);
			g.drawString("v 4.3", 171, 80);
			// Time
			g.setFont(new Font("Verdana", 0, 15));
			g.setColor(Color.WHITE);
			if (setTime == true) {
				g.drawString(" " + hours + ":" + minutes + ":" + seconds + ".",
						47, 315);
			} else {
				g.drawString("Loading...", 47, 315);
			}
			if (timeLimit == true && setTime == true) {
				g.setFont(new Font("Verdana", 0, 9));
				g.setColor(Color.RED);
				long secondsL = timeLeft - ((runTime) / 1000);
				long hoursL = secondsL / (60 * 60);
				secondsL -= hoursL * (60 * 60);
				long minutesL = secondsL / 60;
				secondsL -= minutesL * 60;
				g.drawString("Time left: " + hoursL + " hours, " + minutesL
						+ " minutes.", 355, 14);
			}
			// Hour rates
			g.setFont(new Font("Verdana", 0, 12));
			g.setColor(Color.MAGENTA);
			g.drawString("Exp/Hour: " + XPHR, 60, 115);
			// g.setColor(Color.MAGENTA);
			// g.drawString("Cracks/Hour: " + XPHR / 70, 60, 191);
			g.setColor(Color.MAGENTA);
			g.drawString("Profit/Hour: " + PROFITHR, 60, 212);
			// Thieving exp
			g.setColor(Color.WHITE);
			g.setFont(new Font("Verdana", 0, 11));
			// PROGRESS
			g.setFont(new Font("Verdana", 0, 9));
			toNextLvl = skills.getPercentToNextLevel(Skills.THIEVING);
			XPToLevel = skills.getExpToNextLevel(Skills.THIEVING);
			nextLvl = skills.getCurrentLevel(Skills.THIEVING) + 1;
			g.setColor(new Color(0, 0, 0));
			g.fillRect(19, 125, 180, 16);// Black box
			g.drawRect(18, 124, 181, 17); // FRAME
			g.setColor(new Color(51, 51, 51));
			g.fillRect(19, 125, 180, 8);// Gray box
			g.setColor(new Color(170, 10, 170, 130));
			g.fillRect(20, 126, (int) (toNextLvl * 178 / 100.0), 14);
			g.setColor(Color.WHITE);
			g.drawString("" + toNextLvl + "% to " + nextLvl + " Thiev" + " - "
					+ XPToLevel + " XP", 25, 137);
			// TTNL
			try {
				if (XPHR > 0) {
					long sTNL = (XPToLevel) / (XPHR / 3600);
					long hTNL = sTNL / (60 * 60);
					sTNL -= hTNL * (60 * 60);
					long mTNL = sTNL / 60;
					sTNL -= mTNL * 60;
					g.drawString("Next level in: " + hTNL + ":" + mTNL + ":"
							+ sTNL, 19, 154);
				} else {
					g.drawString("Next level in: 0:0:0", 19, 154);
				}
			} catch (Exception e) {
				g.drawString("Next level in: -1:-1:-1", 19, 154);
			}

			g.drawString("Thieving EXP gained: " + (XPGained), 19, 174);
			g.drawString("Thieving level: " + currentLevel + " ("
					+ levelsGained + ")", 19, 186);
			if (levelLimit == true) {
				g.setFont(new Font("Verdana", 0, 9));
				g.setColor(Color.RED);
				g.drawString(
						"Stopping at level " + limitedLevel + " Thieving.",
						363, 14);
			}
			// Profit
			g.drawString("Total profit: " + GainedProfit, 19, 234);
			g.setColor(Color.BLUE);
			g.drawString("Sapphires: " + Sapphires, 19, 246);
			g.setColor(Color.GREEN);
			g.drawString("Emeralds: " + Emeralds, 19, 258);
			g.setColor(Color.RED);
			g.drawString("Rubies: " + Rubies, 19, 270);
			g.setColor(Color.WHITE);
			g.drawString("Diamonds: " + Diamonds, 19, 282);
		}
		if (extra) {
			// Safes Cracked
			g.setFont(new Font("Verdana", 0, 9));
			g.setColor(Color.WHITE);
			g.drawString("Cracked: " + Cracked, 340, 310);
			// Traps Triggered
			g.drawString("Failed: " + Trapped, 340, 298);
			// Succeessss bar
			g.setFont(new Font("Verdana", 0, 9));
			g.setColor(new Color(0, 0, 0));
			g.fillRect(341, 316, 169, 16);
			g.drawRect(340, 315, 170, 17);
			g.setColor(new Color(51, 51, 51));
			g.fillRect(341, 316, 169, 8);
			g.setColor(new Color(200, 0, 0, 60));
			g.fillRect(343, 318, 165, 12);
			g.setColor(new Color(30, 255, 30, 80));
			if (SuccessRate != 0) {
				g.fillRect(343, 318, (int) (SuccessRate * 165 / 100.0), 12);
			}
			g.setColor(Color.WHITE);
			g.drawString("" + Math.round(SuccessRate) + " % Success", 390, 328);
			g.setColor(Color.WHITE);
			if (Cracked > 0) {
				SuccessRate = ((Cracked * 100.0) / (attempts));
			}

			// Small map
			g.setFont(new Font("Verdana", 0, 9));
			g.setColor(new Color(51, 51, 0));
			g.fillRoundRect(448, 257, 43, 26, 8, 8);
			if (South) {
				g.setColor(Color.GREEN);
				g.drawString("South", 455, 307);
				g.drawRoundRect(450, 270, 38, 27, 10, 10);
			} else {
				g.setColor(Color.RED);
				g.drawString("South", 455, 307);
				g.drawRoundRect(450, 270, 38, 27, 10, 10);
			}
			if (North) {
				g.setColor(Color.GREEN);
				g.drawString("North", 455, 237);
				g.drawRoundRect(450, 240, 38, 27, 10, 10);
			} else {
				g.setColor(Color.RED);
				g.drawString("North", 455, 237);
				g.drawRoundRect(450, 240, 38, 27, 10, 10);
			}
			// /////////////////////////////////////
			g.setFont(new Font("Verdana", 0, 12));
			// /////////////////////////////////////
			if (NorthLeft) {
				g.setColor(Color.GREEN);
				g.drawString("W", 432, 275);
				g.drawString("*", 459, 262);
			} else {
				g.setColor(Color.RED);
				g.drawString("W", 432, 275);
				g.drawString("*", 459, 262);
			}
			if (NorthRight) {
				g.setColor(Color.GREEN);
				g.drawString("E", 494, 275);
				g.drawString("*", 475, 262);
			} else {
				g.setColor(Color.RED);
				g.drawString("E", 494, 275);
				g.drawString("*", 475, 262);
			}
			if (SouthLeft) {
				g.setColor(Color.GREEN);
				g.drawString("W", 432, 275);
				g.drawString("*", 459, 290);
			} else {
				g.setColor(Color.RED);
				g.drawString("W", 432, 275);
				g.drawString("*", 459, 290);
			}
			if (SouthRight) {
				g.setColor(Color.GREEN);
				g.drawString("E", 494, 275);
				g.drawString("*", 475, 290);
			} else {
				g.setColor(Color.RED);
				g.drawString("E", 494, 275);
				g.drawString("*", 475, 290);
			}
		}
	}

	/**
	 * Draws a tile with the passed color on the passed instance of
	 * <code>Graphics</code>.
	 * 
	 * @param render
	 *            The instance of <code>Graphics</code> you want to draw on.
	 * @param tile
	 *            The instance of the tile you want to draw.
	 * @param color
	 *            The color you want the drawn tile to be.
	 * @param drawCardinalDirections
	 *            True if you want the cardinal directions to be drawn in each
	 *            corner.
	 * @author Gnarly
	 */
	public void drawTile(Graphics render, RSTile tile, Color color,
			boolean drawCardinalDirections, String s) {
		Point southwest = calc.tileToScreen(tile, 0, 0, 0);
		Point southeast = calc.tileToScreen(
				new RSTile(tile.getX() + 1, tile.getY()), 0, 0, 0);
		Point northwest = calc.tileToScreen(new RSTile(tile.getX(),
				tile.getY() + 1), 0, 0, 0);
		Point northeast = calc.tileToScreen(
				new RSTile(tile.getX() + 1, tile.getY() + 1), 0, 0, 0);

		if (calc.pointOnScreen(southwest) && calc.pointOnScreen(southeast)
				&& calc.pointOnScreen(northwest)
				&& calc.pointOnScreen(northeast)) {
			render.setColor(Color.BLACK);
			render.drawPolygon(new int[] { (int) northwest.getX(),
					(int) northeast.getX(), (int) southeast.getX(),
					(int) southwest.getX() },
					new int[] { (int) northwest.getY(), (int) northeast.getY(),
							(int) southeast.getY(), (int) southwest.getY() }, 4);
			render.setColor(color);
			render.fillPolygon(new int[] { (int) northwest.getX(),
					(int) northeast.getX(), (int) southeast.getX(),
					(int) southwest.getX() },
					new int[] { (int) northwest.getY(), (int) northeast.getY(),
							(int) southeast.getY(), (int) southwest.getY() }, 4);

			if (drawCardinalDirections) {
				render.setColor(Color.WHITE);
				render.drawString("" + s, southwest.x, southwest.y);
			}
		}
	}

	/*------------------------------------------------------------
	 * ------------------  O N   S T A R T  ----------------------
	 -----------------------------------------------------------*/
	@Override
	public boolean onStart() {
		try {
			title = ImageIO.read(new URL(
					"http://a.imageshack.us/img641/4190/paintv20.png"));
		} catch (final java.io.IOException e) {
			e.printStackTrace();
		}
		OPTION_FILE = new Properties();
		SafeCrackerGUI = new GUI(this);
		SafeCrackerGUI.setLocationRelativeTo(null);
		SafeCrackerGUI.setVisible(true);

		while (WaitForStart) {
			sleep(20);
		}
		if (worked == false)
			return false;
		spots.add(NW);
		spots.add(NE);
		spots.add(SW);
		spots.add(SE);
		log("Retrieving item prices from the Grand Exchange...");
		// SapphirePrice = grandExchange.lookup(Sapphire).getMinPrice();
		// EmeraldPrice = grandExchange.lookup(Emerald).getMinPrice();
		// RubyPrice = grandExchange.lookup(Ruby).getMinPrice();
		// DiamondPrice = grandExchange.lookup(Diamond).getMinPrice();
		SapphirePrice = getGuidePrice(Sapphire);
		EmeraldPrice = getGuidePrice(Emerald);
		RubyPrice = getGuidePrice(Ruby);
		DiamondPrice = getGuidePrice(Diamond);
		EmeraldsINV = inventory.getCount(Emerald);
		RubiesINV = inventory.getCount(Ruby);
		SapphiresINV = inventory.getCount(Sapphire);
		DiamondsINV = inventory.getCount(Diamond);
		log("... Prices retrieved!");
		log(" ~ Anti-ban synchronized! ~");
		log("------------------------------------------------");
		log("Fallen's Safe Cracker is now running!");
		startTime = System.currentTimeMillis();
		timeLeft = (((limitedHours * 60 * 60 * 1000) + (limitedMinutes * 60 * 1000)) / (1000));
		setTime = true;
		camera.setPitch(true);
		return true;
	}

	/*
	 * --------------------------------------------------------------------------
	 * ----------------------------------------------------
	 * ----------------------
	 * ----------------------------------------------------
	 * ----------------------------------------------------
	 * --------------------------------------- M E T H O D S
	 * ----------------------------------------------------
	 * ----------------------
	 * ----------------------------------------------------
	 * ----------------------------------------------------
	 * ----------------------
	 * ----------------------------------------------------
	 * ----------------------------------------------------
	 */
	/**
	 * These GE-methods aren't by me, credits to whoever made them.
	 */
	private int getGuidePrice(int itemID) {
		try {
			URL url = new URL(
					"http://services.runescape.com/m=itemdb_rs/viewitem.ws?obj="
							+ itemID);
			BufferedReader br = new BufferedReader(new InputStreamReader(
					url.openStream()));
			String line = null;

			while ((line = br.readLine()) != null) {
				if (line.contains("<b>Current guide price:</b>")) {
					line = line.replace("<b>Current guide price:</b>", "");
					return (int) parse(line);
				}
			}
		} catch (IOException e) {
		}
		return -1;
	}

	private double parse(String str) {
		if (str != null && !str.isEmpty()) {
			str = stripFormatting(str);
			str = str.substring(str.indexOf(58) + 2, str.length());
			str = str.replace(",", "");
			if (!str.endsWith("%")) {
				if (!str.endsWith("k") && !str.endsWith("m")) {
					return Double.parseDouble(str);
				}
				return Double.parseDouble(str.substring(0, str.length() - 1))
						* (str.endsWith("m") ? 1000000 : 1000);
			}
			int k = str.startsWith("+") ? 1 : -1;
			str = str.substring(1);
			return Double.parseDouble(str.substring(0, str.length() - 1)) * k;
		}
		return -1D;
	}

	private String stripFormatting(String str) {
		if (str != null && !str.isEmpty())
			return str.replaceAll("(^[^<]+>|<[^>]+>|<[^>]+$)", "");
		return "";
	}

	private boolean failSafes() {
		if (!gainedExperience()) {
			Quit();
			return true;
		}
		return false;
	}

	private boolean gainedExperience() {
		if (!expTimeOut.isRunning()) {
			if (XPGained > tempExp) {
				expTimeOut = new Timer(1000 * 60 * 5);
				tempExp = XPGained;
				return true;
			} else {
				log("No experience gained withtin the past 5 minutes!");
				return false;
			}
		}
		return true;
	}

	private void timeToQuit() {
		if (timeLimit == true) {
			if (limitedHours == hours && limitedMinutes <= minutes) {
				log("Time's up!");
				Quit();
			} else if (limitedHours < hours) {
				log("Time's up!");
				Quit();
			}
		}
		if (levelLimit == true) {
			if (currentLevel == limitedLevel) {
				log("Achieved level: " + currentLevel);
				Quit();
			}
		}
	}

	private boolean waitForIF(RSInterface iface, int timeout) {
		long start = System.currentTimeMillis();

		while (System.currentTimeMillis() - start < timeout) {
			if (iface.isValid()) {
				return true;
			}
			sleep(100);
		}
		return false;
	}

	private boolean inRectangle(int x1, int y1, int x2, int y2) {
		getX = getMyPlayer().getLocation().getX();
		getY = getMyPlayer().getLocation().getY();
		if (getX >= x1 && getX <= x2 && getY >= y1 && getY <= y2)
			return true;
		return false;
	}

	private boolean waitForWithdrawnItem(int item, int timeout) {
		int startCount = inventory.getCount(true, item);
		long start = System.currentTimeMillis();

		while (System.currentTimeMillis() - start < timeout) {
			if (inventory.getCount(item) > startCount) {
				return true;
			}
			sleep(100);
		}
		return false;
	}

	private boolean waitForDepositedItem(int timeout) {
		int startCount = inventory.getCount(true);
		long start = System.currentTimeMillis();

		while (System.currentTimeMillis() - start < timeout) {
			if (inventory.getCount(true) < startCount) {
				return true;
			}
			sleep(100);
		}
		return false;
	}

	private boolean waitForStateChange(int timeout) {
		long start = System.currentTimeMillis();

		while (System.currentTimeMillis() - start < timeout) {
			if (CrackState != 1
					|| safeToCrack(getMyPlayer().getLocation()) == null) {
				if (clickFirst && !inventory.isItemSelected()) {
					useStethoscope();
				}
				return true;
			}
			sleep(100);
			if (random(0, 100) > 70) {
				antiBan();
			}
			if (random(0, 20) == random(0, 20) && clickFirst
					&& !inventory.isItemSelected()) {
				useStethoscope();
			}
		}
		return false;
	}

	private boolean waitToStop(int timeout) {
		long start = System.currentTimeMillis();
		while (System.currentTimeMillis() - start < timeout) {
			if (!getMyPlayer().isMoving()) {
				return true;
			}
			sleep(50);
		}
		return false;
	}

	private boolean waitForAnim(int timeout) {
		long start = System.currentTimeMillis();
		while (System.currentTimeMillis() - start < timeout) {
			if (getMyPlayer().getAnimation() != -1) {
				return true;
			}
			sleep(50);
		}
		return false;
	}

	private boolean WD(int itemID, int count) {
		int tempCount = count;
		if (count == 0)
			tempCount = 1;
		if (count < 0)
			throw new IllegalArgumentException("count < 0 (" + count + ")");
		if (!bank.isOpen())
			return false;
		RSItem item = bank.getItem(itemID);
		if (item == null || !item.isComponentValid()
				|| bank.getCount(itemID) < tempCount) {
			if (bank.getCount(itemID) < tempCount) {
				log("Out of items - Check: 1/3.");
				sleep(random(400, 600));
				if (bank.getCount(itemID) < tempCount) {
					log("Out of items - Check: 2/3.");
					sleep(random(400, 600));
					if (bank.getCount(itemID) < tempCount && bank.isOpen()) {
						try {
							log("Out of: " + grandExchange.getItemName(itemID)
									+ " - Check 3/3.");
						} catch (Exception e) {
							e.printStackTrace();
							log("Out of: " + itemID);
						}
						Quit();
						return false;
					}
				}
			}
			return false;
		}
		switch (count) {
		case 0: // Withdraw All
			return item.doAction("Withdraw-All");
		case 1: // Withdraw 1
			return item.doClick(true);
		case 5: // Withdraw 5
		case 10: // Withdraw 10
			return item.doAction("Withdraw-" + count);
		default: // Withdraw x
			if (item.doClick(false)) {
				sleep(random(100, 300));
				if (menu.contains("Withdraw-" + count)) {
					if (menu.doAction("Withdraw-" + count)) {
						sleep(random(100, 200));
						return true;
					}
					return false;
				}
				if (item.doAction("Withdraw-X")) {
					sleep(random(1000, 1300));
					keyboard.sendText("" + count, true);
				}
				sleep(random(100, 200));
				return true;
			}
			break;
		}
		return false;
	}

	private boolean doActionAtModel(RSModel model, String action) {
		if (model != null) {
			int iters = random(3, 6);
			while (--iters > 0 && !menu.contains(action)) {
				try {
					mouse.move(model.getPoint());
					if (menu.contains(action)) {
						sleep(random(20, 100));
						if (menu.contains(action)) {
							break;
						}
					}
				} catch (Exception e) {
				}
			}
			if (menu.contains(action)) {
				return menu.doAction(action);
			} else {
				return false;
			}
		}
		return false;
	}

	private RSObject safeToCrack(RSTile t) {
		RSObject[] objs = objects.getAllAt(t);
		for (RSObject obj : objs) {
			if (obj.getID() == wallsafe) {
				return obj;
			}
		}
		return null;
	}

	private boolean waitForSafe(int timeout) {
		long start = System.currentTimeMillis();
		while (System.currentTimeMillis() - start < timeout) {
			if (safeToCrack(getMyPlayer().getLocation()) != null) {
				return true;
			}
			sleep(50);
		}
		return false;
	}

	private boolean useStethoscope() {
		RSItem Steth = inventory.getItem(Stethoscope);
		if (Steth != null) {
			return Steth.doClick(true);
		}
		return false;
	}

	private void crackSafe() {
		RSTile CurrentTile = getMyPlayer().getLocation();
		if (eat()) {
			return;
		}
		String s = "Crack";
		if (clickFirst) {
			s = "Use Stethoscope -> Wall safe";
			if (!inventory.isItemSelected()) {
				useStethoscope();
			}
		}
		RSObject SafeToCrack = safeToCrack(CurrentTile);
		RSModel SafeModel;
		if (SafeToCrack != null) {
			SafeModel = SafeToCrack.getModel();
		} else {
			return;
		}
		long start = System.currentTimeMillis();
		boolean clicked = false;
		while (!clicked && System.currentTimeMillis() - start < 5000) {
			if (doActionAtModel(SafeModel, s)) {
				clicked = true;
				if (random(0, 100) > 70) {
					mouse.moveRandomly(200);
				}
				CrackState = 1;
				return;
			}
			clicked = false;
			sleep(random(200, 600));
		}
		CrackState = 1;
	}

	/**
	 * Scans the area for your location & checks free locations.
	 */
	private void scanner() {
		if (inRectangle(3052, 4974, 3060, 4981)) {
			North = true;
			South = false;
		} else if (inRectangle(3052, 4966, 3060, 4973)) {
			North = false;
			South = true;
		} else {
			North = false;
			South = false;
		}
		// CRACK LOCATION CHECK [North(Left/Right) / South(Left/Right)]
		if (inRectangle(3055, 4970, 3055, 4970)) {
			SouthLeft = true;
			SouthRight = false;
			NorthLeft = false;
			NorthRight = false;
		} else if (inRectangle(3057, 4970, 3057, 4970)) {
			SouthLeft = false;
			SouthRight = true;
			NorthLeft = false;
			NorthRight = false;
		} else if (inRectangle(3055, 4977, 3055, 4977)) {
			SouthLeft = false;
			SouthRight = false;
			NorthLeft = true;
			NorthRight = false;
		} else if (inRectangle(3057, 4977, 3057, 4977)) {
			SouthLeft = false;
			SouthRight = false;
			NorthLeft = false;
			NorthRight = true;
		} else {
			SouthLeft = false;
			SouthRight = false;
			NorthLeft = false;
			NorthRight = false;
		}

		for (int N = 0; N < 4; N++) {
			spots.get(N).setFree(true);
		}
		RSPlayer[] plrs = players.getAll();
		for (RSPlayer plr : plrs) {
			if (plr != null) {
				RSTile plrL = plr.getLocation();
				for (int N = 0; N < 4; N++) {
					if (plrL.equals(spots.get(N).location)) {
						spots.get(N).setFree(false);
					}
				}
			}
		}
	}

	private boolean someoneBeneathMe() {
		RSTile spot = getMyPlayer().getLocation();
		RSPlayer[] validPlayers = players.getAll();

		for (RSPlayer player : validPlayers) {
			try {
				if (!player.equals(getMyPlayer())) {
					if (player.getLocation().equals(spot)) {
						return true;
					}
				}
			} catch (Exception ignored) {
			}
		}
		return false;
	}

	private RSTile nearestFreeSpot(boolean randomize) {
		ArrayList<RSTile> list = new ArrayList<RSTile>();
		int dist = 20;
		RSTile nearest = null;
		for (int N = 0; N < 4; N++) {
			if (spots.get(N).free) {
				list.add(spots.get(N).location);
				int tempDist = calc.distanceTo(spots.get(N).location);
				if (tempDist < dist) {
					nearest = spots.get(N).location;
					dist = tempDist;
				}
			}
		}
		if (randomize && random(0, 2) == random(0, 2)) {
			return list.get(random(0, list.size()));
		} else {
			return nearest;
		}
	}

	/**
	 * @Author Fall3n
	 * 
	 *         If moving, waits to arrive to a specified distance of your
	 *         destination.
	 * 
	 * @param dist
	 *            - waits until reached this distance. If < 0; waits until the
	 *            destination is visible on the screen.
	 * @param timeout
	 *            - the max amount of time to wait.
	 * @return True if reached the distance to your destination.
	 */
	private boolean waitToGetClose(int dist, int timeout) {
		long start = System.currentTimeMillis();
		if (!waitToMove(random(800, 1400))) {
			return false;
		}
		while (System.currentTimeMillis() - start < timeout) {
			if (dist >= 0) {
				if (walking.getDestination() != null) { // If destination ==
														// null; You're
														// there/It's
														// unreachable.
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
				if (walking.getDestination() != null) { // If destination ==
														// null; You're
														// there/It's
														// unreachable.
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

	private boolean waitToMove(int timeout) {
		long start = System.currentTimeMillis();
		while (System.currentTimeMillis() - start < timeout) {
			if (getMyPlayer().isMoving()) {
				return true;
			}
			sleep(50);
		}
		return false;
	}

	private void CameraAntiBan() {
		int randomTurn = random(1, 2);
		final int GambleInt4 = random(1, 1000);
		if (GambleInt4 >= 930) {
			switch (randomTurn) {
			case 1:
				new CameraRotateThread().start();
				break;
			case 2:
				int randomFormation = random(0, 2);
				if (randomFormation == 0) {
					new CameraRotateThread().start();
				} else {
					new CameraRotateThread().start();
					new CameraRotateThread().start();
				}
			}
		}
	}

	private void getInvCounts() {
		if (inventory.getCount(Emerald) > EmeraldsINV) {
			Emeralds++;
		}
		if (inventory.getCount(Sapphire) > SapphiresINV) {
			Sapphires++;
		}
		if (inventory.getCount(Ruby) > RubiesINV) {
			Rubies++;
		}
		if (inventory.getCount(Diamond) > DiamondsINV) {
			Diamonds++;
		}
		EmeraldsINV = inventory.getCount(Emerald);
		RubiesINV = inventory.getCount(Ruby);
		SapphiresINV = inventory.getCount(Sapphire);
		DiamondsINV = inventory.getCount(Diamond);
	}

	private boolean eat() {
		if (inventory.containsOneOf(FoodIDS)
				&& Integer.parseInt(interfaces.get(748).getComponent(8)
						.getText()) <= EatingPoint) {
			RSItem eatMe = inventory.getItem(FoodIDS);
			if (eatMe != null) {
				if (eatMe.doAction("eat")) {
					waitForAnim(1000);
					sleep(random(200, 400));
				}
			}
			if (inventory.containsOneOf(FoodIDS)
					&& Integer.parseInt(interfaces.get(748).getComponent(8)
							.getText()) <= (EatingPoint + skills
							.getRealLevel(Skills.CONSTITUTION))) {
				RSItem eatMe2 = inventory.getItem(FoodIDS);
				if (eatMe2 != null) {
					sleep(random(1000, 1300));
					if (eatMe2.doAction("eat")) {
						waitForAnim(1000);
						sleep(random(200, 400));
					}
				}
			}
			CrackState = 0;
			return true;
		} else {
			return false;
		}
	}

	private void Quit() {
		if (TakeAShot == true) {
			env.saveScreenshot(true);
			sleep(500);
		}
		stopScript();
	}

	/**
	 * @Author Fall3n
	 * 
	 *         Has to be looped.
	 * 
	 *         Walks directly to the specified locations. Customize the
	 *         variables inside the method if you wish.
	 * 
	 * @param destination
	 *            - the tile to walk to.
	 * @return True if reached the distance to the red flag on your minimap.
	 */
	private boolean directWalk(RSTile destination) {
		int distance = random(3, 5);
		int destX = destination.getX(), destY = destination.getY();
		int startX = getMyPlayer().getLocation().getX(), startY = getMyPlayer()
				.getLocation().getY();
		int distX = destX - startX, distY = destY - startY;
		double distToGoal = Math.sqrt(Math.pow(destX - startX, 2)
				+ Math.pow(distY, 2));
		double scale = random(12, 17) / distToGoal;
		int finalX = (int) (startX + (scale * distX)), finalY = (int) (startY + (scale * distY));
		RSTile walkTo = new RSTile(finalX, finalY);
		if (walkTo.getX() > 0 && walkTo.getY() > 0) {

		} else {
			log("directWalk() failed, error with the tile.");
			return false;
		}
		if (distToGoal < 10) {
			walkTo = destination;
		}
		if (!walking.walkTileMM(walkTo, 1, 1)) {
			log("Didn't even click the map.");
			return false;
		}
		long start = System.currentTimeMillis();

		while (System.currentTimeMillis() - start < random(800, 1200)) {
			if (getMyPlayer().isMoving()) {
				break;
			}
			sleep(50);
		}
		if (!getMyPlayer().isMoving())
			return false;

		if (random(0, 25) == random(0, 25)) {
			walking.walkTileMM(walkTo, 1, 1);
		}
		if (random(0, 10) == random(0, 10)) {
			mouse.moveRandomly(100);
		}
		if (random(0, 15) == random(0, 15)) {
			camera.setAngle(random(0, 360));
		}
		if (random(0, 15) == random(0, 15)) {
			game.openTab(random(0, 17));
			sleep(random(300, 1000));
			game.openTab(Game.TAB_INVENTORY);
		}

		while (System.currentTimeMillis() - start < random(3000, 4000)) {
			if (distance >= 0) {
				if (walking.getDestination() != null) { // If destination ==
														// null; You're
														// there/It's
														// unreachable.
					try {
						if (calc.distanceTo(walking.getDestination()) <= distance) {
							return true;
						}
					} catch (Exception e) {
						log.severe("Failed to get destination.");
					}
				} else {
					return true;
				}
			} else {
				if (walking.getDestination() != null) { // If destination ==
														// null; You're
														// there/It's
														// unreachable.
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

	private void smallWalk() {
		RSTile dest = nearestFreeSpot(false);
		if (dest == null)
			return;
		if (calc.tileOnScreen(dest)) {
			tiles.doAction(dest, "Walk here");
			waitToMove(1000);
			mouse.moveSlightly();
			waitToStop(3000);
		} else {
			directWalk(dest);
		}
	}

	/*
	 * public void smallWalk() { final int GambleInt2 = random(0, 100); if
	 * (North == true) { if((!NLCrowded && !NRCrowded) || (NRCrowded &&
	 * NLCrowded)) { if(GambleInt2 >= 49) { walking.walkTileOnScreen(NLeft);
	 * waitToMove(1000); camera.setAngle(random(130,230)); mouse.moveSlightly();
	 * waitToStop(3000); } else if(GambleInt2 <= 48) {
	 * walking.walkTileOnScreen(NRight); waitToMove(1000);
	 * camera.setAngle(random(130,230)); mouse.moveSlightly(); waitToStop(3000);
	 * } } else if(NRCrowded && !NLCrowded) { walking.walkTileOnScreen(NLeft);
	 * waitToMove(1000); camera.setAngle(random(130,230)); mouse.moveSlightly();
	 * waitToStop(3000); } else if(!NRCrowded && NLCrowded) {
	 * walking.walkTileOnScreen(NRight); waitToMove(1000);
	 * camera.setAngle(random(130,230)); mouse.moveSlightly(); waitToStop(3000);
	 * } } else if (South == true) { if((!SLCrowded && !SRCrowded) || (SRCrowded
	 * && SLCrowded)) { if(GambleInt2 >= 49) { walking.walkTileOnScreen(SLeft);
	 * waitToMove(1000); if(random(0,100) > 50) { camera.setAngle(random(0,50));
	 * } else { camera.setAngle(random(310,359)); } mouse.moveSlightly();
	 * waitToStop(3000); } else if(GambleInt2 <= 48) {
	 * walking.walkTileOnScreen(SRight); waitToMove(1000); if(random(0,100) >
	 * 50) { camera.setAngle(random(0,50)); } else {
	 * camera.setAngle(random(310,359)); } mouse.moveSlightly();
	 * waitToStop(3000); } } else if(SRCrowded && !SLCrowded) {
	 * walking.walkTileOnScreen(SLeft); waitToMove(1000); if(random(0,100) > 50)
	 * { camera.setAngle(random(0,50)); } else {
	 * camera.setAngle(random(310,359)); } mouse.moveSlightly();
	 * waitToStop(3000); } else if(!SRCrowded && SLCrowded) {
	 * walking.walkTileOnScreen(SRight); waitToMove(1000); if(random(0,100) >
	 * 50) { camera.setAngle(random(0,50)); } else {
	 * camera.setAngle(random(310,359)); } mouse.moveSlightly();
	 * waitToStop(3000); } } }
	 */

	private boolean atASafe() {
		if (inRectangle(3055, 4970, 3055, 4970)
				|| inRectangle(3057, 4970, 3057, 4970)
				|| inRectangle(3055, 4977, 3055, 4977)
				|| inRectangle(3057, 4977, 3057, 4977)) {
			return true;
		}
		return false;
	}

	private State getState() {
		RSNPC Banker = npcs.getNearest(BankerID);
		if (!inventory.containsOneOf(FoodIDS)
				|| (!inventory.contains(Stethoscope) && UseStethoscope == true)
				|| inventory.isFull()) {
			if (Banker != null && Banker.isOnScreen()) {
				if (bank.isOpen()) {
					return State.BANK;
				} else {
					return State.OPENBANK;
				}
			} else {
				return State.WALKTOBANK;
			}
		} else {
			if (North || South) {
				if (atASafe()) {
					return State.CRACK;
				} else {
					return State.SMALLWALK;
				}
			} else {
				return State.WALKTOSAFES;
			}
		}
	}

	@Override
	public void messageReceived(MessageEvent e) {
		if (e.getMessage().contains("cracking")) {
			CrackState = 1;
		} else if (e.getMessage().contains("trigger")) {
			CrackState = 2;
			Trapped++;
		} else if (e.getMessage().contains("Nothing")) {
			CrackState = 2;
		} else if (e.getMessage().contains("eat")) {
			CrackState = 0;
		} else if (e.getMessage().contains("loot")) {
			CrackState = 3;
			Cracked++;
		} else if (e.getMessage().contains("level 50")) {
			log("Level 50 Thieving is required to be able to use this script.");
			Quit();
		}
	}

	private boolean IsChangeRequired() {
		if (change) {
			if (someoneBeneathMe()
					&& (SouthLeft || SouthRight || NorthRight || NorthLeft)) { // If
																				// needed
				if (advSwitch) { // if advanced switching is enabled
					if (defined) { // if we've defined a counter
						if (!switchTimer.isRunning()) { // if the counter has
														// ran out
							defined = false;
							switching = true;
							log("Loc-Switch initiated.");
							waitingForMovement = false;
							return true;
						} else { // if the counter is still running
							waitingForMovement = true;
							switching = false;
							return false;
						}
					} else { // if we got no counter
						switchTimer = new Timer(random(1000, 3000) * 60);
						defined = true;
						return false;
					}
				} else { // IF not slowSwitch but got the need.
					switching = true;
					log("Loc-Switch initiated.");
					waitingForMovement = false;
					return true;
				}
			} else { // If no-one is under us.
				waitingForMovement = false;
				switching = false;
				if (switchTimer.isRunning()) {
					if (browseCounter > 9) {
						switchTimer.setEndIn(5);
						browseCounter = 0;
						return false;
					} else {
						browseCounter++;
						return false;
					}
				} else {
					browseCounter = 0;
					return false;
				}
			}
		} else { // if changing is disabled
			return false;
		}
	}

	private boolean SwitchPosition() {
		/*
		 * if(!spots[0].free && !spots[1].free && !spots[2].free &&
		 * !spots[3].free && logIfFull) {
		 * log("All spots were taken.. Logging out. :/"); Quit(); return false;
		 * }
		 */
		if (!NW.free && !NE.free && !SW.free && !SE.free && logIfFull) {
			log("All spots were taken.. Logging out. :/");
			Quit();
			return false;
		}
		long Start = System.currentTimeMillis();
		RSTile dest = nearestFreeSpot(true);
		while ((System.currentTimeMillis() - Start < random(15000, 20000))
				&& !(getMyPlayer().getLocation().equals(dest))) {
			if (!getMyPlayer().getLocation().equals(dest)) {
				if (calc.tileOnScreen(dest)) {
					walking.walkTileOnScreen(dest);
					waitToGetClose(0, random(2000, 3000));
				} else {
					walking.walkTileMM(dest);
					waitToGetClose(3, random(5000, 6000));
				}
			}
		}
		if (getMyPlayer().getLocation().equals(dest)) {
			log("Location-Switch completed.");
			return true;
		} else {
			log("Location-Switch failed.");
			return false;
		}
	}

	@Override
	public int loop() {
		mouse.setSpeed(random(5, 8));
		if (failSafes())
			return 500;
		scanner();
		if (IsChangeRequired()) {
			SwitchPosition();
			return 300;
		}
		switch (getState()) {
		case WALKTOSAFES:
			if (inventory.isItemSelected()) {
				inventory.clickSelectedItem(true);
			}
			if (camera.getPitch() < 80) {
				up = true;
				new CameraHeightThread().start();
			}
			if (!randomized) {
				int GambleInt = random(0, 100);
				if (GambleInt >= 49) {
					dest = new RSTile(3056, 4978);
				}
				if (GambleInt <= 48) {
					dest = new RSTile(3056, 4969);
				}
				randomized = true;
			}
			directWalk(dest);
			CrackState = 0;
			break;
		case SMALLWALK:
			if (inventory.isItemSelected()) {
				inventory.clickSelectedItem(true);
			}
			if (camera.getPitch() < 80) {
				up = true;
				new CameraHeightThread().start();
			}
			smallWalk();
			CrackState = 0;
			break;
		case CRACK:
			randomized = false;
			if (CrackState == 0) {
				getInvCounts();
				crackSafe();
				CrackState = 1;
			} else if (CrackState == 1) {
				waitForStateChange(random(6500, 7500));
				if (CrackState == 1) {
					CrackState = 2;
					break;
				}
			} else if (CrackState == 2) {
				sleep(random(300, 400));
				getInvCounts();
				if (!clickFirst) {
					sleep(random(100, 200));
				}
				crackSafe();
			} else {
				sleep(random(200, 300));
				getInvCounts();
				waitForSafe(random(1000, 2000));
				crackSafe();
			}
			break;
		case WALKTOBANK:
			if (inventory.isItemSelected()) {
				inventory.clickSelectedItem(true);
			}
			if (inventory.isFull()) {
				if (inventory.containsOneOf(FoodIDS)) {
					if (Integer.parseInt(interfaces.get(748).getComponent(8)
							.getText()) <= ((skills
							.getRealLevel(Skills.CONSTITUTION) * 10) - 100)) {
						inventory.getItem(FoodIDS).doAction("eat");
						waitForAnim(1000);
						sleep(random(200, 400));
						break;
					}
				}
			}
			if (camera.getPitch() < 80) {
				up = true;
				new CameraHeightThread().start();
			}
			RSNPC Banker = npcs.getNearest(BankerID);
			CameraAntiBan();
			if (Banker != null) {
				RSTile moveTo = npcs.getNearest(BankerID).getLocation();
				if (!Banker.isOnScreen()) {
					directWalk(moveTo);
				}
				break;
			}
			directWalk(approxBank);
			break;
		case OPENBANK:
			RSNPC Bankerr = npcs.getNearest(BankerID);
			if (Bankerr.doAction("Bank")) {
				if (waitForIF(interfaces.get(Bank.INTERFACE_BANK), 2000)) {
					sleep(random(200, 300));
				}
			}
			break;
		case BANK:
			if (!bank.isOpen())
				break;
			if (UseStethoscope) {
				if (inventory.getCountExcept(Stethoscope, FoodID) > 0) {
					try {
						bank.depositAll();
						waitForDepositedItem(random(2000, 3000));
					} catch (Exception e) {
					}
					CameraAntiBan();
					break;
				}
			}
			if (!UseStethoscope) {
				if (inventory.getCountExcept(FoodID) > 0) {
					try {
						bank.depositAll();
						waitForDepositedItem(random(2000, 3000));
					} catch (Exception e) {
					}
					CameraAntiBan();
					break;
				}
			}
			if (UseStethoscope && !inventory.contains(Stethoscope)) {
				if (WD(Stethoscope, 1)) {
					CameraAntiBan();
					waitForWithdrawnItem(Stethoscope, random(2000, 3000));
				}
				break;
			}
			if (WD(FoodID, FoodWDAmount)) {
				CameraAntiBan();
				waitForWithdrawnItem(FoodID, random(2000, 3000));
			}
			break;
		}
		timeToQuit();
		return random(10, 50);
	}

	// **************************** ANTIBAN ***********************************
	public class CameraRotateThread extends Thread {
		@Override
		public void run() {
			char LR = KeyEvent.VK_RIGHT;
			if (random(0, 2) == 0) {
				LR = KeyEvent.VK_LEFT;
			}
			keyboard.pressKey(LR);
			try {
				Thread.sleep(random(450, 2600));
			} catch (final Exception ignored) {
			}
			keyboard.releaseKey(LR);
		}
	}

	public class CameraHeightThread extends Thread {
		@Override
		public void run() {
			if (up) {
				char UD = KeyEvent.VK_UP;
				keyboard.pressKey(UD);
				try {
					Thread.sleep(random(450, 1700));
				} catch (final Exception ignored) {
				}
				keyboard.releaseKey(UD);
				up = false;
				return;
			}
			char UD = KeyEvent.VK_UP;
			if (random(0, 2) == 0) {
				UD = KeyEvent.VK_DOWN;
			}
			keyboard.pressKey(UD);
			try {
				Thread.sleep(random(450, 1700));
			} catch (final Exception ignored) {
			}
			keyboard.releaseKey(UD);
		}
	}

	public void hoverObject() {
		examineRandomObject(5);
		sleep(randGenerator(50, 1000));
		int mousemoveAfter2 = randGenerator(0, 4);
		sleep(randGenerator(100, 800));
		if (mousemoveAfter2 == 1 && mousemoveAfter2 == 2) {
			mouse.move(1, 1, 760, 500);
		}
	}

	int randGenerator(int min, int max) {
		return min + (int) (java.lang.Math.random() * (max - min));
	}

	public RSTile examineRandomObject(int scans) {
		RSTile start = getMyPlayer().getLocation();
		ArrayList<RSTile> possibleTiles = new ArrayList<RSTile>();
		for (int h = 1; h < scans * scans; h += 2) {
			for (int i = 0; i < h; i++) {
				for (int j = 0; j < h; j++) {
					int offset = (h + 1) / 2 - 1;
					if (i > 0 && i < h - 1) {
						j = h - 1;
					}
					RSTile tile = new RSTile(start.getX() - offset + i,
							start.getY() - offset + j);
					RSObject objectToList = objects.getTopAt(tile);
					if (objectToList != null
							&& calc.tileOnScreen(objectToList.getLocation())) {
						possibleTiles.add(objectToList.getLocation());
					}
				}
			}
		}
		if (possibleTiles.size() == 0) {
			return null;
		}
		if (possibleTiles.size() > 0 && possibleTiles != null) {
			final RSTile objectLoc = possibleTiles.get(randGenerator(0,
					possibleTiles.size()));
			Point objectPoint = calc.tileToScreen(objectLoc);
			if (objectPoint != null) {
				try {
					mouse.move(objectPoint);
					if (menu.doAction("xamine")) {
					} else {
					}
					sleep(random(100, 500));
				} catch (NullPointerException ignored) {
				}
			}
		}
		return null;
	}

	public void antiBan() {
		try {
			int statCheck = random(1, (150000 / Percentage));
			int cameraa = random(1, (8000 / Percentage));
			int mousee = random(1, (2000 / Percentage));
			int hoverObject = random(1, (50000 / Percentage));
			if (mousee == 5) {
				int randomFormation = random(1, 10);
				switch (randomFormation) {
				case 1:
					mouse.move(200, 300, 100, 100);
					sleep(random(100, 500));
					break;
				case 2:
					mouse.move(100, 700, 1000, 1000);
					break;
				case 3:
					mouse.move(340, 200, 300, 200);
					sleep(random(100, 500));
					break;
				case 4:
					mouse.move(122, 458, 50, 10);
					sleep(random(100, 500));
					mouse.moveSlightly();
					break;
				case 5:
					mouse.move(340, 200, 300, 200);
					sleep(random(100, 500));
					break;
				case 6:
					game.openTab(random(0, 17));
					sleep(random(300, 1000));
					game.openTab(Game.TAB_INVENTORY);
					break;
				case 7:
					mouse.move(80, 70, 400 - 80, 240 - 70);
					sleep(random(100, 500));
					break;
				case 8:
					mouse.move(80, 70, 400 - 80, 240 - 70);
					sleep(random(100, 500));
					break;
				case 9:
					mouse.move(80, 70, 400 - 80, 240 - 70);
					sleep(random(100, 500));
					break;
				}
			} else if (cameraa == 5) {
				int randomFormation = random(1, 6);
				switch (randomFormation) {
				case 1:
					new CameraRotateThread().start();
					break;
				case 2:
					new CameraHeightThread().start();
					break;
				case 3:
					new CameraRotateThread().start();
					if (random(0, 100) > random(0, 50)) {
						sleep(random(100, 2000));
						new CameraRotateThread().start();
					}
					break;
				case 4:
					new CameraHeightThread().start();
					if (random(0, 100) > random(0, 50)) {
						sleep(random(100, 2000));
						new CameraHeightThread().start();
					}
					break;
				case 5:
					new CameraRotateThread().start();
					new CameraHeightThread().start();
					if (random(0, 100) > random(0, 50)) {
						sleep(random(100, 2000));
						new CameraRotateThread().start();
						if (random(0, 100) > random(0, 50)) {
							sleep(random(100, 1000));
							new CameraHeightThread().start();
						}
					}
					break;
				}
			} else if (statCheck == 5) {
				game.openTab(Game.TAB_STATS);
				sleep(random(400, 800));
				mouse.move(554, 213, 729 - 554, 428 - 213); // Random level.
				sleep(random(400, 800));
				mouse.move(554, 213, 729 - 554, 428 - 213); // Random level.
				sleep(random(400, 1000));
				mouse.moveRandomly(700);
				sleep(random(300, 500));
				game.openTab(Game.TAB_INVENTORY);
			} else if (hoverObject == 5) {
				hoverObject();
				sleep(random(400, 800));
				mouse.moveRandomly(750);
				sleep(random(400, 800));
			}
		} catch (Exception e) {

		}
		return;
	}

	@Override
	public void onFinish() {
		log(+(XPGained) + " Thieving Exp Gained  ||  " + GainedProfit
				+ " Profit made in: " + hours + ":" + minutes + ":" + seconds
				+ "!");
		log("Thank you for using Fallen's Safe Cracker.");
	}

	// ************ G U I
	// **************************************************************************************
	public boolean WaitForStart = true;
	GUI SafeCrackerGUI;

	public class GUI extends JFrame implements WindowListener {

		private static final long serialVersionUID = -5781125843266714028L;
		private JPanel contentPane;
		private JPanel Panel1;
		private JPanel Panel2;
		private JPanel Panel3;
		private JTabbedPane TabbedPanel;
		private JButton startButton;
		private JButton exitButton;
		private JButton load;
		private JButton save;
		private JRadioButton Box1;
		private JRadioButton Box2;
		private JRadioButton Box3;
		private JRadioButton Box4;
		private JRadioButton Box5;
		private JRadioButton Box6;
		private JRadioButton Box7;
		private JComboBox Options;
		private JLabel OptionText;
		private JLabel BoxText;
		private JLabel FieldText1;
		private JLabel FieldText2;
		private JLabel FieldText3;
		private JFormattedTextField FormTextField1;
		private JFormattedTextField FormTextField2;
		private JFormattedTextField FormTextField3;
		private JFormattedTextField FormTextField4;
		private JFormattedTextField FormTextField5;
		private JTextPane TextPanel;
		FallenSafeCracker script;

		public GUI(final FallenSafeCracker SafeCracker) {
			script = SafeCracker;
			initComponents();
		}

		private void initComponents() {
			addWindowListener(this);
			contentPane = (JPanel) this.getContentPane();
			contentPane.setLayout(null);
			contentPane.setOpaque(false);

			TabbedPanel = new JTabbedPane();
			Panel1 = new JPanel();
			Panel2 = new JPanel();
			Panel3 = new JPanel();
			TabbedPanel.setFont(new Font("Verdana", Font.PLAIN, 12));
			TabbedPanel.setBorder(null);
			TabbedPanel.setFocusable(false);
			TabbedPanel.addTab("General", Panel1);
			TabbedPanel.addTab("Stopping", Panel2);
			TabbedPanel.addTab("Info", Panel3);
			Panel1.setFont(new Font("Verdana", Font.PLAIN, 12));
			Panel1.setFocusable(false);
			Panel1.setLayout(null);
			Panel2.setFont(new Font("Verdana", Font.PLAIN, 12));
			Panel2.setFocusable(false);
			Panel2.setLayout(null);
			Panel3.setFont(new Font("Verdana", Font.PLAIN, 12));
			Panel3.setFocusable(false);
			Panel3.setLayout(null);

			startButton = new JButton();
			startButton.setFont(new Font("Verdana", Font.PLAIN, 12));
			startButton.setText("Start");
			startButton.setEnabled(true);
			startButton.addActionListener(new java.awt.event.ActionListener() {
				@Override
				public void actionPerformed(final java.awt.event.ActionEvent evt) {
					startButtonActionPerformed();
				}
			});
			exitButton = new JButton();
			exitButton.setText("Cancel");
			exitButton.addActionListener(new java.awt.event.ActionListener() {
				@Override
				public void actionPerformed(final java.awt.event.ActionEvent evt) {
					exitButtonActionPerformed();
				}
			});

			load = new JButton();
			load.setText("Load");
			load.addActionListener(new java.awt.event.ActionListener() {
				@Override
				public void actionPerformed(final java.awt.event.ActionEvent evt) {
					if (loadOptions(OPTION_FILE)) {
						JOptionPane.showMessageDialog(null,
								"Options successfully loaded!",
								"Option Loader",
								JOptionPane.INFORMATION_MESSAGE, null);
					}
				}
			});

			save = new JButton();
			save.setText("Save");
			save.addActionListener(new java.awt.event.ActionListener() {
				@Override
				public void actionPerformed(final java.awt.event.ActionEvent evt) {
					if (saveOptions(OPTION_FILE)) {
						JOptionPane.showMessageDialog(null,
								"Options successfully saved!", "Option Saver",
								JOptionPane.INFORMATION_MESSAGE, null);
					}
				}
			});

			// GENERAL
			String[] types = { "Trout", "Salmon", "Tuna", "Cake", "Lobster",
					"Bass", "Swordfish", "Monkfish", "Shark", "Rocktail" };
			Options = new JComboBox(types);
			OptionText = new JLabel();
			OptionText.setFont(new Font("Verdana", Font.PLAIN, 14));
			OptionText.setText("Which food to use?");
			Options.setBorder(null);
			Options.setOpaque(false);
			Options.setFocusable(false);

			FormTextField1 = new JFormattedTextField();
			FormTextField1.setValue("10");
			FormTextField1.setEnabled(true);
			BoxText = new JLabel();
			BoxText.setText("Amount of food to withdraw:");
			BoxText.setEnabled(true);

			FieldText1 = new JLabel();
			FieldText1.setText("Eat when health drops below: ");
			FieldText1.setEnabled(true);
			FormTextField2 = new JFormattedTextField();
			FormTextField2.setValue("200");
			FormTextField2.setEnabled(true);

			Box7 = new JRadioButton();
			Box7.setText("Use location-switching?");
			Box7.setEnabled(true);
			Box7.setSelected(false);
			Box7.addActionListener(new java.awt.event.ActionListener() {
				@Override
				public void actionPerformed(final java.awt.event.ActionEvent evt) {
					if (Box7.isSelected()) {
						Box1.setEnabled(true);
					} else {
						Box1.setEnabled(false);
					}
				}
			});

			Box1 = new JRadioButton();
			Box1.setText("Advanced mode");
			if (Box7.isSelected()) {
				Box1.setEnabled(true);
			} else {
				Box1.setEnabled(false);
			}
			Box1.setSelected(false);

			Box2 = new JRadioButton();
			Box2.setText("Use Stethoscope?");
			Box2.setEnabled(true);
			Box2.setSelected(true);
			Box2.addActionListener(new java.awt.event.ActionListener() {
				@Override
				public void actionPerformed(final java.awt.event.ActionEvent evt) {
					if (Box2.isSelected()) {
						Box6.setEnabled(true);
					} else {
						Box6.setEnabled(false);
					}
				}
			});

			Box6 = new JRadioButton();
			Box6.setText("Click the scope?");
			if (Box2.isSelected()) {
				Box6.setEnabled(true);
			}
			Box6.setSelected(false);

			Box3 = new JRadioButton();
			Box3.setText("Take a screenshot when finished?");
			Box3.setEnabled(true);
			Box2.setSelected(true);

			// STOPPING
			// TIMER THINGIES
			Box4 = new JRadioButton();
			Box4.setText("Stop after:");
			Box4.setEnabled(true);
			Box4.setSelected(false);
			Box4.addActionListener(new java.awt.event.ActionListener() {
				@Override
				public void actionPerformed(final java.awt.event.ActionEvent evt) {
					Box4ButtonActionPerformed();
				}
			});
			FieldText2 = new JLabel();
			FieldText2.setText("Hours:");
			FieldText2.setEnabled(false);
			FormTextField3 = new JFormattedTextField();
			FormTextField3.setValue("2");
			FormTextField3.setEnabled(false);

			FieldText3 = new JLabel();
			FieldText3.setText("Minutes:");
			FieldText3.setEnabled(false);
			FormTextField4 = new JFormattedTextField();
			FormTextField4.setValue("30");
			FormTextField4.setEnabled(false);

			// LEVELER THINGIES
			Box5 = new JRadioButton();
			Box5.setText("Stop when reached level: ");
			Box5.setEnabled(true);
			Box5.setSelected(false);
			Box5.addActionListener(new java.awt.event.ActionListener() {
				@Override
				public void actionPerformed(final java.awt.event.ActionEvent evt) {
					Box5ButtonActionPerformed();
				}
			});
			FormTextField5 = new JFormattedTextField();
			FormTextField5.setValue("99");
			FormTextField5.setEnabled(false);

			// INFO
			TextPanel = new JTextPane();
			TextPanel
					.setText("Fallen's SafeCracker v4.3\n\n Start the script at Rogue Den's. Have food visible in your bank.\n\n "
							+ "Advanced location switching will wait 1-3 minutes to check if the "
							+ "player beneath us moves until switching location.");
			TextPanel.setEditable(false);
			TextPanel.setFont(new Font("Verdana", Font.PLAIN, 12));
			TextPanel.setBorder(new LineBorder(Color.black));

			// PLACEMENT
			setSize(400, 400);
			setTitle("Fallen's SafeCracker v4.3");
			addComponent(contentPane, startButton, 5, 270, 375, 55);
			addComponent(contentPane, exitButton, 140, 330, 100, 26);
			addComponent(contentPane, TabbedPanel, 5, 5, 375, 260);
			addComponent(contentPane, save, 5, 333, 60, 20);
			addComponent(contentPane, load, 315, 333, 60, 20);

			addComponent(Panel1, OptionText, 113, 10, 250, 25);
			addComponent(Panel1, Options, 145, 37, 80, 20);
			addComponent(Panel1, BoxText, 5, 70, 159, 20);
			addComponent(Panel1, FormTextField1, 180, 70, 50, 20);
			addComponent(Panel1, FieldText1, 5, 100, 170, 20);
			addComponent(Panel1, FormTextField2, 180, 100, 50, 20);
			addComponent(Panel1, Box7, 5, 130, 290, 20);
			addComponent(Panel1, Box1, 50, 150, 290, 20);
			addComponent(Panel1, Box3, 5, 170, 230, 20);
			addComponent(Panel1, Box2, 5, 190, 200, 20);
			addComponent(Panel1, Box6, 50, 210, 200, 20);

			addComponent(Panel2, Box4, 5, 60, 100, 20);
			addComponent(Panel2, FieldText2, 140, 60, 40, 20);
			addComponent(Panel2, FormTextField3, 179, 60, 30, 20);
			addComponent(Panel2, FieldText3, 230, 60, 50, 20);
			addComponent(Panel2, FormTextField4, 280, 60, 30, 20);
			addComponent(Panel2, Box5, 5, 150, 170, 20);
			addComponent(Panel2, FormTextField5, 179, 150, 30, 20);
			addComponent(Panel3, TextPanel, 10, 10, 350, 210);
		}

		private void addComponent(Container container, Component c, int x,
				int y, int width, int height) {
			c.setBounds(x, y, width, height);
			container.add(c);
		}

		private void startButtonActionPerformed() {

			GUIString = Options.getSelectedItem().toString();
			GUIString2 = FormTextField1.getValue();
			GUIString3 = FormTextField2.getValue();

			GUIString4 = FormTextField3.getValue();// TIME - HOURS
			GUIString5 = FormTextField4.getValue();// TIME - MINUTES

			GUIString6 = FormTextField5.getValue(); // LEVEL

			if (GUIString.equals("Trout")) {
				FoodID = 333;
			}
			if (GUIString.equals("Salmon")) {
				FoodID = 329;
			}
			if (GUIString.equals("Tuna")) {
				FoodID = 361;
			}
			if (GUIString.equals("Cake")) {
				FoodID = 1891;
			}
			if (GUIString.equals("Lobster")) {
				FoodID = 379;
			}
			if (GUIString.equals("Bass")) {
				FoodID = 365;
			}
			if (GUIString.equals("Swordfish")) {
				FoodID = 373;
			}
			if (GUIString.equals("Monkfish")) {
				FoodID = 7946;
			}
			if (GUIString.equals("Shark")) {
				FoodID = 385;
			}
			if (GUIString.equals("Rocktail")) {
				FoodID = 15272;
			}
			FallenSafeCracker.FoodWDAmount = Integer
					.parseInt((String) GUIString2);
			FallenSafeCracker.EatingPoint = Integer
					.parseInt((String) GUIString3);
			if (Box7.isSelected()) {
				change = true;
				if (Box1.isSelected()) {
					advSwitch = true;
				} else {
					advSwitch = false;
				}
			} else {
				change = false;
				advSwitch = false;
			}
			if (Box2.isSelected()) {
				UseStethoscope = true;
			} else {
				UseStethoscope = false;
			}
			if (Box3.isSelected()) {
				TakeAShot = true;
			}
			if (Box4.isSelected()) {
				timeLimit = true;
				FallenSafeCracker.limitedHours = Integer
						.parseInt((String) GUIString4);
				FallenSafeCracker.limitedMinutes = Integer
						.parseInt((String) GUIString5);
			}
			if (Box5.isSelected()) {
				levelLimit = true;
				FallenSafeCracker.limitedLevel = Integer
						.parseInt((String) GUIString6);
			}
			if (Box6.isSelected() && Box6.isEnabled()) {
				clickFirst = true;
			}

			script.WaitForStart = false;
			worked = true;
			dispose();

		}

		private void Box4ButtonActionPerformed() {
			if (Box4.isSelected()) {
				Box5.setSelected(false);
				// ENABLE HOURS AND MINUTES FORMTEXTFIELDS & TEXTS
				FieldText2.setEnabled(true);
				FormTextField3.setEnabled(true);
				FieldText3.setEnabled(true);
				FormTextField4.setEnabled(true);
				// DISABLE LEVEL THINGIES
				FormTextField5.setEnabled(false);
			} else if (!Box5.isSelected()) {
				// DISABLE HOURS AND MINUTES FORMTEXTFIELDS & TEXTS
				// DISABLE LEVEL THINGIES
				Box4.setSelected(false);
				Box5.setSelected(false);
				Box4.setSelected(false);
				Box5.setSelected(false);
				FieldText2.setEnabled(false);
				FormTextField3.setEnabled(false);
				FieldText3.setEnabled(false);
				FormTextField4.setEnabled(false);
				FormTextField5.setEnabled(false);
			}
		}

		private void Box5ButtonActionPerformed() {
			if (Box5.isSelected()) {
				Box4.setSelected(false);
				// DISABLE HOURS AND MINUTES FORMTEXTFIELDS & TEXTS
				FieldText2.setEnabled(false);
				FormTextField3.setEnabled(false);
				FieldText3.setEnabled(false);
				FormTextField4.setEnabled(false);
				// ENABLE LEVEL THINGIES
				FormTextField5.setEnabled(true);
			} else if (!Box4.isSelected()) {
				// DISABLE HOURS AND MINUTES FORMTEXTFIELDS & TEXTS
				// DISABLE LEVEL THINGIES
				Box4.setSelected(false);
				Box5.setSelected(false);
				FieldText2.setEnabled(false);
				FormTextField3.setEnabled(false);
				FieldText3.setEnabled(false);
				FormTextField4.setEnabled(false);
				FormTextField5.setEnabled(false);
			}
		}

		private void exitButtonActionPerformed() {
			WaitForStart = false;
			worked = false;
			dispose();
		}

		@Override
		public void windowClosing(final WindowEvent arg0) {
			WaitForStart = false;
			worked = false;
			dispose();
		}

		@Override
		public void windowActivated(final WindowEvent e) {
			toFront();
		}

		@Override
		public void windowClosed(WindowEvent arg0) {
		}

		@Override
		public void windowDeactivated(WindowEvent arg0) {
		}

		@Override
		public void windowDeiconified(WindowEvent arg0) {
		}

		@Override
		public void windowIconified(WindowEvent arg0) {
		}

		@Override
		public void windowOpened(WindowEvent arg0) {
		}

		// Option stuff
		private boolean saveOptions(Properties file) {
			// FOOD
			String Food = Options.getSelectedItem().toString();
			file.setProperty("Food", Food);
			// Withdrawal
			String WDAM;
			try {
				WDAM = FormTextField1.getValue().toString();
			} catch (Exception e) {
				WDAM = "10";
			}
			file.setProperty("WDAM", WDAM);
			// EatingPoint
			String EatLimit;
			try {
				EatLimit = FormTextField2.getValue().toString();
			} catch (Exception e) {
				EatLimit = "200";
			}
			file.setProperty("EatLimit", EatLimit);
			// Loc-switch?
			if (Box7.isSelected()) {
				file.setProperty("Change", "1");
				if (Box1.isSelected()) {
					file.setProperty("Advanced", "1");
				} else {
					file.setProperty("Advanced", "2");
				}
			} else {
				file.setProperty("Change", "2");
				file.setProperty("Advanced", "2");
			}
			// Screenshot?
			if (Box3.isSelected()) {
				file.setProperty("ScreenShot", "1");
			} else {
				file.setProperty("ScreenShot", "2");
			}
			// Stethoscope?
			if (Box2.isSelected()) {
				file.setProperty("Stethoscope", "1");
			} else {
				file.setProperty("Stethoscope", "2");
			}
			// Click first?
			if (Box6.isSelected()) {
				file.setProperty("ClickFirst", "1");
			} else {
				file.setProperty("ClickFirst", "2");
			}
			// Time limits
			String hours = "2";
			String minutes = "30";
			if (Box4.isSelected()) {
				file.setProperty("TimeLimit", "1");
				try {
					hours = (String) FormTextField3.getValue();
					minutes = (String) FormTextField4.getValue();
				} catch (Exception e) {
					hours = "2";
					minutes = "30";
				}
			} else {
				file.setProperty("TimeLimit", "2");
			}
			file.setProperty("Hours", hours);
			file.setProperty("Minutes", minutes);
			// Level limits
			String LevelLimit = "99";
			if (Box5.isSelected()) {
				file.setProperty("LevelLimit", "1");
				try {
					LevelLimit = (String) FormTextField5.getValue();
				} catch (Exception e) {
					LevelLimit = "99";
				}
			} else {
				file.setProperty("LevelLimit", "2");
			}
			file.setProperty("Level", LevelLimit);
			// Saving the file.
			try {
				file.store(
						new FileWriter(new File(GlobalConfiguration.Paths
								.getSettingsDirectory(),
								"FallenSafeCracker.ini")),
						"The options of FallenSafeCracker");
				return true;
			} catch (IOException e) {
				return false;
			}
		}

		private boolean loadOptions(Properties file) {
			try {
				file.load(new FileInputStream(new File(
						GlobalConfiguration.Paths.getSettingsDirectory(),
						"FallenSafeCracker.ini")));
			} catch (FileNotFoundException e) {
				return false;
			} catch (IOException e) {
				return false;
			}
			// Assuming we succeeded @ loading.
			if (file.getProperty("Food") != null) {
				Options.setSelectedItem(file.getProperty("Food"));
			}
			if (file.getProperty("WDAM") != null) {
				FormTextField1.setValue(file.getProperty("WDAM"));
			}
			if (file.getProperty("EatLimit") != null) {
				FormTextField2.setValue(file.getProperty("EatLimit"));
			}
			if (file.getProperty("Change") != null) {
				if (file.getProperty("Change").equals("1")) {
					Box7.setSelected(true);
					Box1.setEnabled(true);
					if (file.getProperty("Advanced") != null) {
						if (file.getProperty("Advanced").equals("1")) {
							Box1.setSelected(true);
						} else {
							Box1.setSelected(false);
						}
					}
				} else {
					Box1.setEnabled(false);
					Box7.setSelected(false);
					Box1.setSelected(false);
				}
			}
			if (file.getProperty("ScreenShot") != null) {
				if (file.getProperty("ScreenShot").equals("1")) {
					Box3.setSelected(true);
				} else {
					Box3.setSelected(false);
				}
			}
			if (file.getProperty("Stethoscope") != null) {
				if (file.getProperty("Stethoscope").equals("1")) {
					Box2.setSelected(true);
					Box6.setEnabled(true);
				} else {
					Box2.setSelected(false);
					Box6.setSelected(false);
					Box6.setEnabled(false);
				}
			}
			if (file.getProperty("ClickFirst") != null) {
				if (file.getProperty("ClickFirst").equals("1")) {
					Box6.setSelected(true);
				} else {
					Box6.setSelected(false);
				}
			}
			if (file.getProperty("TimeLimit") != null) {
				if (file.getProperty("TimeLimit").equals("1")) {
					Box4.setSelected(true);
					Box5.setSelected(false);
					FieldText2.setEnabled(true);
					FormTextField3.setEnabled(true);
					FieldText3.setEnabled(true);
					FormTextField4.setEnabled(true);
					// DISABLE LEVEL THINGIES
					FormTextField5.setEnabled(false);
					if (file.getProperty("Hours") != null) {
						FormTextField3.setValue(file.getProperty("Hours"));
					}
					if (file.getProperty("Minutes") != null) {
						FormTextField4.setValue(file.getProperty("Minutes"));
					}
				} else {
					Box4.setSelected(false);
					Box5.setSelected(true);
				}
			}
			if (file.getProperty("LevelLimit") != null) {
				if (file.getProperty("LevelLimit").equals("1")) {
					Box4.setSelected(false);
					Box5.setSelected(true);
					FieldText2.setEnabled(false);
					FormTextField3.setEnabled(false);
					FieldText3.setEnabled(false);
					FormTextField4.setEnabled(false);
					// ENABLE LEVEL THINGIES
					FormTextField5.setEnabled(true);
					if (file.getProperty("Level") != null) {
						FormTextField5.setValue(file.getProperty("Level"));
					}
				} else {
					Box4.setSelected(true);
					Box5.setSelected(false);
				}
			}
			return true;
		}

	}

	@Override
	public void mouseClicked(MouseEvent arg0) {

	}

	@Override
	public void mouseEntered(MouseEvent arg0) {

	}

	@Override
	public void mouseExited(MouseEvent arg0) {

	}

	@Override
	public void mousePressed(MouseEvent e) {
		Point p = new Point(e.getPoint());
		final Rectangle paintRec = new Rectangle(416, 458, 32, 15);
		final Rectangle RTRec = new Rectangle(448, 458, 32, 15);
		final Rectangle extraRec = new Rectangle(480, 458, 32, 15);

		if (paintRec.contains(p) && renew == true) {
			if (paint == true) {
				paint = false;
				renew = false;
			} else {
				paint = true;
				renew = false;
			}
		}
		if (RTRec.contains(p) && renew == true) {
			if (RT == true) {
				RT = false;
				renew = false;
			} else {
				RT = true;
				renew = false;
			}
		}
		if (extraRec.contains(p) && renew == true) {
			if (extra == true) {
				extra = false;
				renew = false;
			} else {
				extra = true;
				renew = false;
			}
		}
	}

	@Override
	public void mouseReleased(MouseEvent arg0) {
		renew = true;
	}

	@Override
	public void mouseDragged(MouseEvent e) {
	}

	@Override
	public void mouseMoved(MouseEvent e) {
	}
}
