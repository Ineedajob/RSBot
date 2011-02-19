import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.awt.geom.Ellipse2D;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
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
import javax.swing.JSlider;
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
import org.rsbot.script.methods.Magic;
import org.rsbot.script.methods.Skills;
import org.rsbot.script.util.Timer;
import org.rsbot.script.wrappers.RSComponent;
import org.rsbot.script.wrappers.RSInterface;
import org.rsbot.script.wrappers.RSItem;
import org.rsbot.script.wrappers.RSNPC;
import org.rsbot.script.wrappers.RSObject;
import org.rsbot.script.wrappers.RSPlayer;
import org.rsbot.script.wrappers.RSTile;
import org.rsbot.util.GlobalConfiguration;

/* @Updated 2.1.2011.
 * * * * *  Version 5.99  * * * *
 * 
 * - Final adjustments for the banking method.
 * 
 * The script is currently not the cleanest. I'll clean the code for 6.0.
 * 
 * * * * * * * * * * * * * * * * *
 *  
 */

@ScriptManifest(authors = { "Fallen" }, keywords = "Magic", name = "Fallen's Superheater", version = 5.99, description = "Superheats any kind of bars.")
public class FallenSuperheater extends Script implements PaintListener,
		MessageListener, MouseListener, MouseMotionListener {

	private enum State {
		OPENBANK, SUPERHEAT,

		DefaultBank, AdamantBank, CoalBagBank
	}

	// Mostly paint int's
	private boolean renew = true;
	private long startTime = 0;
	private final long scriptStartTime = 0;
	private long runTime = 0;
	private long millis = 0;
	private long seconds = 0;
	private long minutes = 0;
	private long hours = 0;
	private int BarCounter = 0;
	private int XPGained = 0, XPGainedSmithing = 0, XPGainedMagic = 0;
	private int startXPM;
	private int startXPS;
	private int startLevelM;
	private int levelsGainedM;
	private int startLevelS;
	private int levelsGainedS;
	private int currentLevelM;
	private int currentLevelS;
	private Image BKG;
	private int toNextLvlS;
	private int nextLvlS;
	private int XPToLevelS;
	private int toNextLvlM;
	private int nextLvlM;
	private int XPToLevelM;
	private long timerStart = 0;
	private long pauseTimer = 0;
	private long tempTimer = 0;
	private long tempTimer2 = 0;
	private boolean gathered = false;
	private boolean MTTNL = false;
	private boolean STTNL = false;
	private Point mouseSpot;
	private Timer expTimeOut = new Timer(1000 * 60 * 5);
	private int tempExp = 0;
	private int browser = 0;
	private boolean readyToCast = true;
	private int EXP1, EXP2;

	private Shape Circle;
	private Rectangle Rect;
	private Point P;
	private int R;
	private boolean DRAW = false;

	private double BarEXP = 0;

	private String BarType;
	private static int AmountOfCasts;
	private String Status;

	private String GUIString;
	private Object GUIString2;
	private Object GUIString3;
	private Object GUIString4;
	private boolean worked = false;

	// Some variables & anti-ban
	private Properties OPTION_FILE;
	private static int Percentage = 100;
	private static int AFK1, AFK2;
	private static int Mouse1 = 50;
	private int Mouse2 = 8;

	private final int cBag = 18339;
	private boolean coalBag = false, filled = false;
	private boolean bankTwice = false;
	private int OpenedBank = 0;
	private boolean Continue = false;
	private boolean BankTime = false;
	private boolean DirectHeat = false;
	private boolean TakeAShot = false;
	private boolean DoTheMath = false;
	private int Option = 1;
	private final boolean smartHeat = true;

	// int barType = 0;
	private int BarID = 0;
	private int Ore1 = 0;
	private int Ore2 = 0;
	private int Ore1WD = 0;
	private int Ore2WD = 0;
	private int Ore2PerSpell = 0;
	private int Ore1InvAm = 0;
	private int Ore2InvAm = 0;

	// Ore ID's
	private final int copperOre = 436;
	private final int tinOre = 438;
	private final int ironOre = 440;
	private final int silverOre = 442;
	private final int goldOre = 444;
	private final int coalOre = 453;
	private final int mithrilOre = 447;
	private final int adamantOre = 449;
	private final int runeOre = 451;
	// Bar ID's
	private final int bronzeBar = 2349;
	private final int ironBar = 2351;
	private final int steelBar = 2353;
	private final int silverBar = 2355;
	private final int goldBar = 2357;
	private final int mithrilBar = 2359;
	private final int adamantBar = 2361;
	private final int runeBar = 2363;

	private final int nature = 561;

	// Item prices from G.E
	private int BarPrice = 0;
	private int Ore1Price = 0;
	private int Ore2Price = 0;
	private int naturePrice = 0;

	private final int BOOK_KNOWLEDGE_ID = 11640;
	private final int LAMP_ID = 2528;
	private final int MYSTERY_BOX_ID = 6199;
	private final int BOX_ID = 14664;

	private final Rectangle testRec = null;

	/*-------------------------------------------------------------------
	 * ------------------   P   A   I   N   T   -------------------------
	 ------------------------------------------------------------------*/
	@Override
	public void onRepaint(Graphics g) {
		if (!game.isLoggedIn() || isPaused() || !isActive()) {
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

		runTime = System.currentTimeMillis() - scriptStartTime;
		seconds = runTime / 1000;
		if (startXPM == 0) {
			startXPM = skills.getCurrentExp(Skills.MAGIC);
		}
		if (startXPS == 0) {
			startXPS = skills.getCurrentExp(Skills.SMITHING);
		}
		if (startLevelM == 0) {
			startLevelM = skills.getCurrentLevel(Skills.MAGIC);
		}
		if (startLevelS == 0) {
			startLevelS = skills.getCurrentLevel(Skills.SMITHING);
		}
		XPGainedMagic = (skills.getCurrentExp(Skills.MAGIC) - startXPM);
		final int XPHRM = (int) ((XPGainedMagic) * 3600000D / (System
				.currentTimeMillis() - startTime - pauseTimer));
		XPGainedSmithing = (skills.getCurrentExp(Skills.SMITHING) - startXPS);
		BarCounter = (XPGainedMagic / 53);
		final int XPHRS = (int) ((XPGainedSmithing) * 3600000D / (System
				.currentTimeMillis() - startTime - pauseTimer));
		final int profit = ((BarPrice) - (Ore1Price) - (Ore2Price) - (naturePrice));
		final int ProfitHR = (int) ((profit * BarCounter) * 3600000D / (System
				.currentTimeMillis() - startTime - pauseTimer));
		levelsGainedM = skills.getCurrentLevel(Skills.MAGIC) - startLevelM;
		levelsGainedS = skills.getCurrentLevel(Skills.SMITHING) - startLevelS;
		currentLevelM = skills.getCurrentLevel(Skills.MAGIC);
		currentLevelS = skills.getCurrentLevel(Skills.SMITHING);
		millis = System.currentTimeMillis() - startTime - pauseTimer;
		hours = millis / (1000 * 60 * 60);
		millis -= hours * (1000 * 60 * 60);
		minutes = millis / (1000 * 60);
		millis -= minutes * (1000 * 60);
		seconds = millis / 1000;
		// DRAWINGS
		// GetNearest
		if (game.getCurrentTab() == Game.TAB_INVENTORY && DRAW) {
			g.setColor(Color.GREEN);
			g.drawRect(Rect.x, Rect.y, Rect.width, Rect.height);
			g.setColor(Color.CYAN);
			// Circle = new Ellipse2D.Double(((p.getX())-(i/2)),
			// ((p.getY())-(i/2)), i, i);
			g.drawOval((int) ((P.getX()) - (R / 2)),
					(int) ((P.getY()) - (R / 2)), R, R);
			g.setColor(Color.BLACK);
			g.fillOval((int) ((P.getX()) - (4 / 2)),
					(int) ((P.getY()) - (4 / 2)), 4, 4);
		}
		// Mouse in general.
		Point loc = mouse.getLocation();
		g.setColor(new Color(90, 90, 255, 180));
		g.fillRoundRect((int) loc.getX() - 10, (int) loc.getY() - 1, 21, 3, 3,
				3);
		g.fillRoundRect((int) loc.getX() - 1, (int) loc.getY() - 10, 3, 21, 3,
				3);
		// TestRec
		if (testRec != null) {
			g.setColor(Color.GREEN);
			g.drawRect(testRec.x, testRec.y, testRec.width, testRec.height);
		}
		// GENERAL
		g.setColor(new Color(0, 0, 200, 130));
		g.fillRect(497, 458, 15, 14);
		g.setColor(new Color(0, 0, 0, 255));
		g.drawRect(497, 458, 15, 14);
		g.setColor(new Color(255, 255, 255, 255));
		g.drawString("" + Option, 502, 470);
		toNextLvlS = skills.getPercentToNextLevel(Skills.SMITHING);
		XPToLevelS = skills.getExpToNextLevel(Skills.SMITHING);
		nextLvlS = skills.getCurrentLevel(Skills.SMITHING) + 1;
		toNextLvlM = skills.getPercentToNextLevel(Skills.MAGIC);
		XPToLevelM = skills.getExpToNextLevel(Skills.MAGIC);
		nextLvlM = skills.getCurrentLevel(Skills.MAGIC) + 1;
		// OPTION 1
		if (Option == 1) {
			// IMAGE
			g.drawImage(BKG, 7, 297, null);
			g.setFont(new Font("Verdana", 0, 9));
			g.setColor(Color.BLUE);
			g.drawString("v5.99", 443, 424);
			// TIME
			if (Status != "Loading...") {
				g.setFont(new Font("Verdana", 0, 13));
				g.setColor(Color.WHITE);
				g.drawString("" + hours + ":" + minutes + ":" + seconds + ".",
						80, 437);
			} else {
				g.setFont(new Font("Verdana", 0, 13));
				g.setColor(Color.WHITE);
				g.drawString("Loading...", 80, 437);
			}
			// Status
			g.setFont(new Font("Verdana", 0, 9));
			if (!game.isLoggedIn() || isPaused()) {
				Status = "Paused.";
			}
			g.drawString("" + Status, 40, 459);
			// Proggy bars & texts
			// Mage
			g.setFont(new Font("Verdana", 0, 9));
			g.setColor(new Color(0, 0, 0));
			g.fillRect(54, 359, 169, 16);
			g.drawRect(53, 358, 170, 17);
			g.setColor(new Color(51, 51, 51));
			g.fillRect(54, 359, 169, 8);
			g.setColor(new Color(90, 90, 255, 100));
			g.fillRect(56, 361, (int) (toNextLvlM * 165 / 100.0), 12);
			g.setColor(Color.WHITE);
			g.drawString("" + toNextLvlM + "% to " + nextLvlM + " Mage ("
					+ (XPToLevelM) / 1000 + "k Xp)", 70, 371);
			g.setColor(Color.CYAN);
			g.drawString("XP Gained: " + XPGainedMagic + " (" + XPHRM / 1000
					+ "k Xp/h)", 53, 391);
			g.drawString(
					"Level: " + currentLevelM + " (" + levelsGainedM + ")", 53,
					404);
			// Smith
			g.setFont(new Font("Verdana", 0, 9));
			g.setColor(new Color(0, 0, 0));
			g.fillRect(263, 359, 169, 16);
			g.drawRect(262, 358, 170, 17);
			g.setColor(new Color(51, 51, 51));
			g.fillRect(263, 359, 169, 8);
			g.setColor(new Color(250, 150, 70, 100));
			g.fillRect(265, 361, (int) (toNextLvlS * 165 / 100.0), 12);
			g.setColor(Color.WHITE);
			g.drawString("" + toNextLvlS + "% to " + nextLvlS + " Smith ("
					+ (XPToLevelS) / 1000 + "k Xp)", 279, 371);

			g.drawString("XP Gained: " + XPGainedSmithing + " (" + XPHRS / 1000
					+ "k Xp/h)", 262, 391);
			g.drawString(
					"Level: " + currentLevelS + " (" + levelsGainedS + ")",
					262, 404);
			// PROFIT
			g.setColor(Color.YELLOW);
			if (DoTheMath) {
				g.drawString("Profit: " + profit * BarCounter + " (" + ProfitHR
						/ 1000 + "k/h)", 262, 429);
				g.drawString("Profit/" + BarType + " Bar: " + profit, 262, 455);
			}
			g.drawString("Bars: " + BarCounter + " " + BarType + " Bars" + " ("
					+ XPHRM / 53 + "/h)", 262, 442);
			// Bars left
			if (AmountOfCasts > 0) {
				g.setFont(new Font("Verdana", 0, 9));
				g.setColor(Color.RED);
				g.drawString("Casts left: " + (AmountOfCasts - BarCounter),
						390, 15);
			}
		}
		// OPTION 2
		if (Option == 2) {
			// IMAGE
			g.drawImage(BKG, 16, 154, null);
			g.setFont(new Font("Verdana", 0, 9));
			g.setColor(Color.BLUE);
			g.drawString("v5.99", 452, 281);
			// TIME
			if (Status != "Loading...") {
				g.setFont(new Font("Verdana", 0, 13));
				g.setColor(Color.WHITE);
				g.drawString("" + hours + ":" + minutes + ":" + seconds + ".",
						89, 294);
			} else {
				g.setFont(new Font("Verdana", 0, 13));
				g.setColor(Color.WHITE);
				g.drawString("Loading...", 89, 294);
			}
			// Status
			g.setFont(new Font("Verdana", 0, 9));
			g.drawString("" + Status, 49, 316);
			// Proggy bars & texts
			// Mage
			g.setFont(new Font("Verdana", 0, 9));
			g.setColor(new Color(0, 0, 0));
			g.fillRect(63, 216, 169, 16);
			g.drawRect(62, 215, 170, 17);
			g.setColor(new Color(51, 51, 51));
			g.fillRect(63, 216, 169, 8);
			g.setColor(new Color(90, 90, 255, 100));
			g.fillRect(65, 218, (int) (toNextLvlM * 165 / 100.0), 12);
			g.setColor(Color.WHITE);
			g.drawString("" + toNextLvlM + "% to " + nextLvlM + " Mage ("
					+ (XPToLevelM) / 1000 + "k Xp)", 70, 228);
			g.setColor(Color.CYAN);
			g.drawString("XP Gained: " + XPGainedMagic + " (" + XPHRM / 1000
					+ "k Xp/h)", 62, 248);
			g.drawString(
					"Level: " + currentLevelM + " (" + levelsGainedM + ")", 62,
					261);
			// Smith
			g.setFont(new Font("Verdana", 0, 9));
			g.setColor(new Color(0, 0, 0));
			g.fillRect(272, 216, 169, 16);
			g.drawRect(271, 215, 170, 17);
			g.setColor(new Color(51, 51, 51));
			g.fillRect(272, 216, 169, 8);
			g.setColor(new Color(250, 150, 70, 100));
			g.fillRect(274, 218, (int) (toNextLvlS * 165 / 100.0), 12);
			g.setColor(Color.WHITE);
			g.drawString("" + toNextLvlS + "% to " + nextLvlS + " Smith ("
					+ (XPToLevelS) / 1000 + "k Xp)", 288, 228);

			g.drawString("XP Gained: " + XPGainedSmithing + " (" + XPHRS / 1000
					+ "k Xp/h)", 271, 248);
			g.drawString(
					"Level: " + currentLevelS + " (" + levelsGainedS + ")",
					271, 261);
			// PROFIT
			g.setColor(Color.YELLOW);
			if (DoTheMath) {
				g.drawString("Profit: " + profit * BarCounter + " (" + ProfitHR
						/ 1000 + "k/h)", 271, 286);
				g.drawString("Profit/" + BarType + " Bar: " + profit, 271, 312);
			}
			g.drawString("Bars: " + BarCounter + " " + BarType + " Bars" + " ("
					+ XPHRM / 53 + "/h)", 271, 299);
			// Bars left
			if (AmountOfCasts > 0) {
				g.setFont(new Font("Verdana", 0, 9));
				g.setColor(Color.RED);
				g.drawString("Casts left: " + (AmountOfCasts - BarCounter),
						390, 15);
			}
		}
		if (MTTNL) {
			int X = (int) mouseSpot.getX();
			int Y = (int) mouseSpot.getY();

			g.setColor(new Color(50, 50, 255, 230));
			g.fillRect(X, Y, 150, 25);// FRAME
			g.setColor(new Color(0, 0, 0));
			g.drawRect(X, Y, 150, 25);// Outline
			g.setFont(new Font("Verdana", 0, 9));
			g.setColor(Color.WHITE);
			try {
				if (XPHRM > 0) {
					long sTNL = (XPToLevelM) / (XPHRM / 3600);
					long hTNL = sTNL / (60 * 60);
					sTNL -= hTNL * (60 * 60);
					long mTNL = sTNL / 60;
					sTNL -= mTNL * 60;
					g.drawString("Next level in: " + hTNL + ":" + mTNL + ":"
							+ sTNL, X + 8, Y + 11);
				} else {
					g.drawString("Next level in: 0:0:0", X + 8, Y + 11);
				}
			} catch (Exception e) {
				g.drawString("Next level in: -1:-1:-1", X + 8, Y + 11);
			}
			g.drawString("Casts to next level: " + (XPToLevelM / 53), X + 8,
					Y + 22);
		}
		if (STTNL) {
			int X = (int) mouseSpot.getX();
			int Y = (int) mouseSpot.getY();
			g.setColor(new Color(210, 110, 50, 230));
			g.fillRect(X, Y, 150, 25);// FRAME
			g.setColor(new Color(0, 0, 0));
			g.drawRect(X, Y, 150, 25);// Outline
			g.setFont(new Font("Verdana", 0, 9));
			g.setColor(Color.WHITE);
			try {
				if (XPHRS > 0) {
					long sTNL = (XPToLevelS) / (XPHRS / 3600);
					long hTNL = sTNL / (60 * 60);
					sTNL -= hTNL * (60 * 60);
					long mTNL = sTNL / 60;
					sTNL -= mTNL * 60;
					g.drawString("Next level in: " + hTNL + ":" + mTNL + ":"
							+ sTNL, X + 8, Y + 11);
				} else {
					g.drawString("Next level in: 0:0:0", X + 8, Y + 11);
				}
			} catch (Exception e) {
				g.drawString("Next level in: -1:-1:-1", X + 8, Y + 11);
			}
			g.drawString("Casts to next level: " + (int) (XPToLevelS / BarEXP),
					X + 8, Y + 22);
		}
		EXP1 = XPGainedMagic;
		if (EXP1 > EXP2) {
			EXP2 = EXP1;
			readyToCast = true;
		}
	}

	/*------------------------------------------------------------
	 * ------------------  O N   S T A R T  ----------------------
	 -----------------------------------------------------------*/
	@Override
	public boolean onStart() {
		Status = "Loading...";
		try {
			BKG = ImageIO
					.read(new URL(
							"http://a.imageshack.us/img14/5392/superheater50paint.png"));
		} catch (final java.io.IOException e) {
			e.printStackTrace();
		}
		OPTION_FILE = new Properties();

		SuperheaterGUI = new GUI(this);
		SuperheaterGUI.setLocationRelativeTo(null);
		SuperheaterGUI.setVisible(true);

		while (WaitForStart) {
			sleep(20);
		}

		if (!worked)
			return false;
		if (DoTheMath) {
			log("Obtaining G.E prices...");
			BarPrice = grandExchange.lookup(BarID).getGuidePrice();
			Ore1Price = grandExchange.lookup(Ore1).getGuidePrice();
			if (bankTwice) {
				Ore2Price = grandExchange.lookup(Ore2).getGuidePrice()
						* Ore2PerSpell;
			}
			naturePrice = grandExchange.lookup(nature).getGuidePrice();
			;
			log("... prices retrieved!" + "  ||  Profit/Bar: " + (BarPrice)
					+ " - " + (Ore1Price) + " - " + (Ore2Price) + " - "
					+ (naturePrice) + " = "
					+ (BarPrice - Ore1Price - Ore2Price - naturePrice));
		}
		if (coalBag) {
			log("Magic XP/Cast: 53  ||  Smithing XP/" + BarType + " bar: "
					+ BarEXP + "  ||  Using coal bag!");
		} else {
			log("Magic XP/Cast: 53  ||  Smithing XP/" + BarType + " bar: "
					+ BarEXP + ".");
		}
		mouse.setSpeed(random(Mouse2 - 1, Mouse2 + 2));
		log(" ~ Anti-ban synchronized to " + Percentage
				+ "% efficiency! || Mousespeed set to " + Mouse1 + "%! ~");
		if (TakeAShot) {
			log("Will take a screenshot when finished, Fallen's Superheater is now running!");
		} else {
			log("Fallen's Superheater is now running!");
		}
		if (AmountOfCasts != 0) {
			log("Logging out after " + AmountOfCasts + " casts!");
		}
		game.openTab(Game.TAB_INVENTORY);
		if (!inventory.contains(nature)) {
			log.severe("Unable to find nature runes.");
			return false;
		}
		log("------------------------------------------------");
		startTime = System.currentTimeMillis();
		return true;
	}

	// ************************************************************************************************************
	private State getState() {
		if (!bank.isOpen()) {
			if (DirectHeat || Continue) {
				return State.SUPERHEAT;
			} else if (!Continue && BankTime) {
				return State.OPENBANK;
			} else if (!DirectHeat && !Continue) {
				int ore1Count = inventory.getCount(Ore1), ore2Count = inventory
						.getCount(Ore2);
				if (ore1Count < 1 || ore2Count < Ore2PerSpell
						|| ore1Count > Ore1InvAm || ore2Count > Ore2InvAm) {
					return State.OPENBANK;
				} else {
					return State.SUPERHEAT;
				}
			} else {
				return State.SUPERHEAT;
			}
		} else {
			return getBankState();
		}
	}

	// Gets the banking state.
	private State getBankState() {
		if (!coalBag) {
			if (BarType == "Adamant") {
				return State.AdamantBank;
			} else {
				return State.DefaultBank;
			}
		} else {
			return State.CoalBagBank;
		}
	}

	// ************************************************************************************************************
	@Override
	public int loop() {
		mouse.setSpeed(random(Mouse2 - 1, Mouse2 + 2));
		if (random(0, 75) == random(0, 75))
			mouse.moveRandomly(0, random(0, 75));
		if (failSafes())
			return 500;
		switch (getState()) { // Switches between these states based on getState
		case SUPERHEAT:
			Status = "Superheating.";
			DirectHeat = false;
			if (!isSuperheatSelected()) {
				if (game.getCurrentTab() != Game.TAB_MAGIC) {
					game.openTab(Game.TAB_MAGIC);
				}
				if (!bank.isOpen()) {
					try {
						if (magic.castSpell(Magic.SPELL_SUPERHEAT_ITEM)) {
							waitForTab(Game.TAB_INVENTORY, 2500);
							if (game.getCurrentTab() != Game.TAB_INVENTORY) {
								spellCheck();
							}
						} else {
							break;
						}
					} catch (Exception e) {
					}
				} else {
					bank.close();
					break;
				}
			}
			if (!bank.isOpen() && game.getCurrentTab() == Game.TAB_INVENTORY) {
				checkBooleans();
				RSItem closest = getClosestItem(Ore1, mouse.getLocation());
				if ((closest != null && closest.doAction("Cast"))
						|| atLastInventoryItem(Ore1, "Cast")) {
					spellAfterMath();
					break;
				}
			}
			break;
		case OPENBANK:
			Status = "Opening the bank.";
			if (magic.isSpellSelected()) {
				game.openTab(Game.TAB_INVENTORY);
			}
			if (OpenedBank < 2) {
				if (new FallenBank().open()) {
					OpenedBank = 0;
					BankTime = false;
				} else {
					if (new FallenBank().open()) {
						OpenedBank = 0;
						BankTime = false;
					} else {
						OpenedBank++;
					}
				}
				mouse.moveRandomly(50);
			} else {
				game.openTab(random(0, 17));
				sleep(random(300, 600));
				game.openTab(Game.TAB_INVENTORY);
				OpenedBank = 0;
			}
			break;
		case DefaultBank:
			defaultBank();
			break;
		case AdamantBank:
			adamantBank();
			break;
		case CoalBagBank:
			coalBagBank();
			break;
		}
		return random(10, 50);
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
	private boolean failSafes() {
		if (bank.isOpen()) { // Rewards box.
			if (inventory.containsOneOf(BOX_ID, BOOK_KNOWLEDGE_ID, LAMP_ID,
					MYSTERY_BOX_ID)) {
				bank.close();
				sleep(random(1000, 1200));
				log("Rewards-Box failsafed.");
				game.openTab(Game.TAB_INVENTORY);
				sleep(random(500, 1000));
				return true;
			}
		}
		if (!gainedExperience()) {
			Quit();
			return true;
		}
		try {
			if (getMyPlayer().getAnimation() == 9633) {
				log("What the heck, we're alching... Better quit now.");
				Quit();
				return true;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

	private boolean isSuperheatSelected() {
		return magic.isSpellSelected()
				&& game.getCurrentTab() == Game.TAB_INVENTORY;
	}

	private void spellCheck() {
		String[] actions = menu.getActions();
		boolean contained = false;
		for (String act : actions) {
			if (act.equals("Cast Superheat Item")) {
				contained = true;
			}
		}
		if (!contained) {
			if (browser > 4) {
				if (magic.getInterface()
						.getComponent(Magic.INTERFACE_SHOW_SKILL_SPELLS)
						.doClick()) {
					log("Show skill-spells enabled.");
					browser = 0;
					sleep(random(1500, 2000));
				}
				return;
			} else {
				browser++;
			}
		} else {
			browser = 0;
		}
	}

	/**
	 * Just a little something to make banking smoother. Probably could've done
	 * it simpler but who gives a shit.
	 */
	public class FallenBank extends Thread {
		public int BANK_IFACE = 762;
		public int[] BANKERS = { 44, 45, 494, 495, 499, 958, 1036, 2271, 2354,
				2355, 3824, 5488, 5901, 5912, 5913, 6362, 6532, 6533, 6534,
				6535, 7605, 8948, 9710, 14367 };
		public int[] BANK_BOOTHS = { 2213, 4483, 6084, 11402, 11758, 12759,
				14367, 19230, 24914, 25808, 26972, 27663, 29085, 34752, 35647,
				36786 };
		public int[] BANK_CHESTS = { 4483, 12308, 21301, 27663, 42192 };

		public boolean waitToOpen() {
			int timeOut = random(2000, 3000);
			long Start = System.currentTimeMillis();
			while (System.currentTimeMillis() - Start < timeOut) {
				if (interfaces.get(BANK_IFACE).isValid()) {
					return true;
				}
				try {
					sleep(random(50, 100));
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			return false;
		}

		public RSNPC nearest(int... ids) {
			RSNPC[] allNPC = npcs.getAll();
			int dist = 99;
			RSNPC nearest = null;
			for (RSNPC npc : allNPC) {
				for (int id : ids) {
					if (npc.getID() == id) {
						int tempDist = calc.distanceTo(npc.getLocation());
						if (tempDist < dist) {
							nearest = npc;
							dist = tempDist;
						}
					}
				}
			}
			return nearest;
		}

		public boolean click(RSTile t, int... IDS) {
			mouse.move(calc.tileToScreen(t, 0.5, 0.5, 50));
			try {
				sleep(random(0, 100));
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			for (int id : IDS) {
				if (objects.getTopAt(t).getID() == id) {
					mouse.click(true);
					return true;
				}
			}
			return false;
		}

		public final boolean open() {
			RSObject Chest = objects.getNearest(BANK_CHESTS);
			RSNPC Banker = nearest(BANKERS);
			RSObject Booth = objects.getNearest(BANK_BOOTHS);
			if (Chest != null) {
				if (Chest.isOnScreen()) {
					// if(Chest.doAction("Bank Bank Chest") ||
					// Chest.doAction("Use Bank Chest") ||
					// Chest.doAction("Bank Chest >")) {
					if (click(Chest.getLocation(), BANK_CHESTS)
							|| Chest.doAction("Bank Bank Chest")
							|| Chest.doAction("Use Bank Chest")
							|| Chest.doAction("Bank Chest >")) {
						if (waitToOpen()) {
							try {
								sleep(random(200, 300));
							} catch (InterruptedException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
							return true;
						}
					}
				} else {
					camera.turnToTile(Chest.getLocation());
				}
			} else if (Banker != null) {
				if (Banker.isOnScreen()) {
					if (Banker.doAction("Bank Banker")) {
						if (waitToOpen()) {
							try {
								sleep(random(200, 300));
							} catch (InterruptedException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
							return true;
						}
					}
				} else {
					camera.turnToTile(Banker.getLocation());
				}
			} else if (Booth != null) {
				if (Booth.isOnScreen()) {
					if (Booth.doAction("Use-quickly Bank booth")) {
						if (waitToOpen()) {
							try {
								sleep(random(200, 300));
							} catch (InterruptedException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
							return true;
						}
					}
				} else {
					camera.turnToTile(Booth.getLocation());
				}
			} else {
				return false;
			}
			return false;
		}
	}

	/**
	 * @returns A tile that's 'distance' away from you in the opposite direction
	 *          of RSTile 't'.
	 * 
	 * @param distance
	 *            - How far the returned tile should be from you.
	 * @param t
	 *            - Get's the opposite direction from this tile. (Could be e.g.
	 *            the location of a monster/object)
	 * 
	 * @author Fall3n
	 */
	/*
	 * private RSTile awayFrom(int distance, RSTile t) { RSTile destination =
	 * null; RSTile me = getMyPlayer().getLocation(); int DistX = t.getX() -
	 * me.getX(), DistY = t.getY() - me.getY(); float Dist = (float)
	 * Math.sqrt(Math.pow(DistX,2) + Math.pow(DistY,2)); //So in other words the
	 * RSTile t is now 'Dist' tiles away from you. float Scale = distance/Dist;
	 * return new RSTile((int)(me.getX() - (Scale * DistX)), (int)(me.getY() -
	 * (Scale * DistY))); }
	 */

	private boolean spellAfterMath() {
		if (random(0, 3) == random(0, 3)) {
			mouse.moveRandomly(3);
		}
		/*
		 * if(useFKeys) { sleep(random(100,200)); keyboard.pressKey((char)
		 * KeyEvent.VK_F4); sleep(random(50,150)); keyboard.releaseKey((char)
		 * KeyEvent.VK_F4); }
		 */
		AntiBan();
		waitForMageTabWO(2500, smartHeat);
		if ((AmountOfCasts) != 0) {
			if (BarCounter >= AmountOfCasts) {
				Quit();
				log("Goal achieved!");
				return false;
			}
		}
		return true;
	}

	/**
	 * Since I actually came up with this method, credits for it's usage would
	 * be appreciated.(Or at least not deleting this.)
	 * 
	 * @author Fall3n.
	 * 
	 * 
	 * @param ItemID
	 * @param p
	 *            - This is the point where from you want to find the closest
	 *            item.
	 * @return The closest item from <b>p</b> with the id <b>ItemID</b>.
	 */
	private RSItem getClosestItem(int ItemID, Point p) {
		for (int i = 1; i < 800; i += 2) {
			R = i;
			P = p;
			DRAW = true;
			Circle = new Ellipse2D.Double(((p.getX()) - (i / 2)),
					((p.getY()) - (i / 2)), i, i);
			RSComponent[] slots = inventory.getInterface().getComponents();
			for (RSComponent slot : slots) {
				if (Circle.contains(slot.getCenter())) {
					RSItem item = inventory.getItemAt(slot.getComponentIndex());
					if (item != null) {
						if (item.getID() == ItemID) {
							Rect = slot.getArea();
							return item;
						}
					}
				}
			}

		}
		return null;
	}

	private boolean gainedExperience() {
		if (!expTimeOut.isRunning()) {
			XPGained = XPGainedSmithing + XPGainedMagic;
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

	/**
	 * Checks if Continue & BankTime are true or false.
	 * 
	 * @param ore1Count
	 *            - The count of prime -ores.
	 * @param ore2Count
	 *            - The count of coal.
	 */
	private void checkBooleans() {
		if (game.getCurrentTab() != Game.TAB_INVENTORY) {
			game.openTab(Game.TAB_INVENTORY);
		}
		int ore1Count = inventory.getCount(Ore1), ore2Count = inventory
				.getCount(Ore2);
		if (ore1Count < 1) {
			ore1Count = inventory.getCount(Ore1);
		}
		if (ore2Count < 1) {
			ore2Count = inventory.getCount(Ore2);
		}
		filled = false;
		if (!coalBag) { // The default case.
			if (BarType != "Adamant") {
				if ((ore1Count > 1 && !bankTwice)
						|| (bankTwice && ore1Count > 1 && ore2Count > Ore2PerSpell)) {
					Continue = true;
					BankTime = false;
				} else {
					Continue = false;
					BankTime = true;
				}
			} else {
				if (ore1Count > 1 && ore2Count > Ore2PerSpell) {
					Continue = true;
					BankTime = false;
				} else {
					Continue = false;
					BankTime = true;
				}
			}
		} else { // Coal-bag cases.
			if (ore1Count > 1 && ore2Count > Ore2PerSpell) {
				Continue = true;
				BankTime = false;
			} else {
				Continue = false;
				BankTime = true;
			}
		}
	}

	private void coalBagBank() {
		Status = "Banking.";
		if (!bank.isOpen()) {
			return;
		}
		if (!inventory.contains(cBag)) {
			if (WD(cBag, 1)) {
				waitForWithdrawnItem(cBag, random(2000, 3000));
			}
			return;
		}
		OpenedBank = 0;
		int ore1Count = inventory.getCount(Ore1), ore2Count = inventory
				.getCount(Ore2);

		if (inventory.getCountExcept(nature, cBag, Ore1, Ore2) > 0) {
			try {
				depositAllExcept(nature, cBag, Ore1, Ore2);
			} catch (Exception e) {
			}
			return;
		}
		if (!filled) {
			if (ore2Count < 26) {
				if (inventory.getCountExcept(cBag, nature, Ore2) > 0) {
					try {
						depositAllExcept(nature, cBag, Ore2);
					} catch (Exception e) {
					}
					return;
				} else {
					if (WD(Ore2, Ore2WD)) {
						waitForWithdrawnItem(Ore2, random(2000, 3000));
					}
					return;
				}
			} else {
				RSItem bag = inventory.getItem(cBag);
				if (bag != null) {
					if (bag.doAction("Fill Coal bag")) {
						waitForDepositedItem(random(2000, 3000));
						sleep(random(100, 200));
					}
				}
				return;
			}
		}
		if (ore1Count == 0) {
			if (inventory.getCount() > 28 - Ore1InvAm) {
				try {
					depositAllExcept(nature, cBag);
				} catch (Exception e) {
				}
				return;
			}
			if (WD(Ore1, Ore1WD)) {
				waitForWithdrawnItem(Ore1, random(2000, 3000));
			}
			return;
		} else if (ore1Count > 0 && ore1Count != Ore1InvAm) {
			bank.deposit(Ore1, 0);
			waitForDepositedItem(random(2000, 3000));
			return;
		} else {
			if (!inventory.isFull()) {
				if (WD(Ore2, Ore2WD)) {
					waitForWithdrawnItem(Ore2, random(2000, 3000));
				}
				return;
			} else {
				if (game.getCurrentTab() == Game.TAB_MAGIC) {
					DirectHeat = true;
				}
				BankTime = false;
				bank.close();
				return;
			}
		}
	}

	private void adamantBank() {
		Status = "Banking.";
		if (!bank.isOpen()) {
			return;
		}
		OpenedBank = 0;
		int ore1Count = inventory.getCount(Ore1), ore2Count = inventory
				.getCount(Ore2), barCount = inventory.getCount(BarID);
		if (inventory.getCountExcept(Ore1, Ore2, nature, BarID) > 0) {
			try {
				depositAllExcept(Ore1, Ore2, nature, BarID);
			} catch (Exception e) {
			}
			return;
		}
		if (barCount >= 9) {
			try {
				depositAllExcept(nature);
			} catch (Exception e) {
			}
			return;
		} else if (barCount == 0) {
			if (ore1Count == 0) {
				if (WD(Ore1, Ore1WD)) {
					waitForWithdrawnItem(Ore1, random(2000, 3000));
				}
				return;
			} else if (ore1Count == 9) {
				if (ore2Count == 18) {
					if (game.getCurrentTab() == Game.TAB_MAGIC) {
						DirectHeat = true;
					}
					BankTime = false;
					bank.close();
					return;
				} else {
					if (WD(Ore2, Ore2WD)) {
						waitForWithdrawnItem(Ore2, random(2000, 3000));
					}
					return;
				}
			} else {
				bank.deposit(Ore1, 0);
				waitForDepositedItem(random(2000, 2500));
				return;
			}
		} else if (barCount > 0 && barCount < 9) {
			if (inventory.getCountExcept(nature, Ore1, Ore2, BarID) > 0) {
				try {
					depositAllExcept(nature, Ore1, Ore2, BarID);
				} catch (Exception e) {
				}
				return;
			}
			if (ore2Count < 18) {
				if (WD(Ore2, Ore2WD)) {
					waitForWithdrawnItem(Ore2, random(2000, 3000));
				}
				return;
			} else {
				if (game.getCurrentTab() == Game.TAB_MAGIC) {
					DirectHeat = true;
				}
				BankTime = false;
				bank.close();
				return;
			}
		} else {
			try {
				depositAllExcept(nature);
			} catch (Exception e) {
			}
			return;
		}
	}

	private void defaultBank() {
		Status = "Banking.";
		if (!bank.isOpen()) {
			return;
		}
		OpenedBank = 0;
		final int ore1Count2 = inventory.getCount(Ore1);
		final int ore2Count2 = inventory.getCount(Ore2);
		if (inventory.getCountExcept(nature, Ore1, Ore2) > 0) {
			try {
				depositAllExcept(nature, Ore1, Ore2);
			} catch (Exception e) {
			}
			return;
		}
		if (bankTwice) {
			if (ore1Count2 == 0) {
				if (WD(Ore1, Ore1WD)) {
					waitForWithdrawnItem(Ore1, random(2000, 3000));
				}
				return;
			} else if (ore1Count2 > 0 && ore1Count2 < Ore1InvAm) {
				try {
					if (ore2Count2 != Ore2InvAm) {
						bank.deposit(Ore1, 0);
						waitForDepositedItem(random(2000, 3000));
					} else {
						if (WD(Ore1, Ore1WD)) {
							waitForWithdrawnItem(Ore1, random(2000, 3000));
						}
					}

				} catch (Exception e) {
				}
				return;
			} else if (ore1Count2 > Ore1InvAm) {
				try {
					bank.deposit(Ore1, 0);
					waitForDepositedItem(random(2000, 3000));
				} catch (Exception e) {
				}
				return;
			} else {
				if (ore2Count2 == 0) {
					if (WD(Ore2, Ore2WD)) {
						waitForWithdrawnItem(Ore2, random(2000, 3000));
					}
					return;
				} else if (ore2Count2 > 0 && ore2Count2 < Ore2InvAm) {
					try {
						if (ore1Count2 != Ore1InvAm) {
							bank.deposit(Ore2, 0);
							waitForDepositedItem(random(2000, 3000));
						} else {
							if (WD(Ore2, Ore2WD)) {
								waitForWithdrawnItem(Ore2, random(2000, 3000));
							}
						}

					} catch (Exception e) {
					}
					return;
				} else if (ore2Count2 > Ore2InvAm) {
					try {
						bank.deposit(Ore2, 0);
						waitForDepositedItem(random(2000, 3000));
					} catch (Exception e) {
					}
					return;
				} else {
					if (game.getCurrentTab() == Game.TAB_MAGIC) {
						DirectHeat = true;
					}
					BankTime = false;
					bank.close();
					return;
				}
			}
		} else { // IF bankTwice == false...
			if (ore1Count2 == 0) {
				if (WD(Ore1, Ore1WD)) {
					waitForWithdrawnItem(Ore1, random(2000, 3000));
				}
				return;
			} else if (ore1Count2 > 0 && ore1Count2 < Ore1InvAm) {
				if (WD(Ore1, Ore1WD)) {
					waitForWithdrawnItem(Ore1, random(2000, 3000));
				}
				return;
			} else if (ore1Count2 > Ore1InvAm) {
				try {
					bank.deposit(Ore1, 0);
					waitForDepositedItem(random(2000, 3000));
				} catch (Exception e) {
				}
				return;
			} else {
				if (game.getCurrentTab() == Game.TAB_MAGIC) {
					DirectHeat = true;
				}
				BankTime = false;
				bank.close();
				return;
			}
		}
	}

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

	public boolean waitForMageTabWO(int timeout, boolean smartHeating) {
		long start = System.currentTimeMillis();
		int cap = random(600, 900);
		if (random(0, 33) == random(0, 33))
			mouse.moveRandomly(33);
		while (System.currentTimeMillis() - start < timeout) {
			if (BankTime && System.currentTimeMillis() - start > cap) {
				return true;
			}
			if (!smartHeating) {
				if (game.getCurrentTab() == Game.TAB_MAGIC) {
					return true;
				}
			} else {
				if (game.getCurrentTab() == Game.TAB_MAGIC && readyToCast) {
					readyToCast = false;
					sleep(random(0, random(0, 200)));
					return true;
				}
			}
			sleep(25);
		}
		return false;
	}

	public boolean waitForTab(int tab, int timeout) {
		long start = System.currentTimeMillis();
		while (System.currentTimeMillis() - start < timeout) {
			if (game.getCurrentTab() == tab) {
				return true;
			}
			sleep(100);
		}
		return false;
	}

	private boolean atLastInventoryItem(int itemID, String option) {
		try {
			if (game.getCurrentTab() != Game.TAB_INVENTORY
					&& !interfaces.get(Bank.INTERFACE_BANK).isValid()) {
				game.openTab(Game.TAB_INVENTORY);
			}
			RSComponent iinventory = inventory.getInterface();
			if (iinventory == null || iinventory.getComponents() == null)
				return false;

			java.util.List<RSComponent> possible = new ArrayList<RSComponent>();
			for (RSComponent item : iinventory.getComponents()) {
				if (item != null && item.getComponentID() == itemID) {
					possible.add(item);
				}
			}
			if (possible.size() == 0)
				return false;
			RSComponent item = possible.get(possible.size());
			return (item).doAction(option);
		} catch (Exception e) {
			log("atInventoryItem(int itemID, String option) Error: " + e);
			return false;
		}
	}

	/**
	 * Deposits all items in inventory except for the given IDs. Supports
	 * deposit boxes.
	 * 
	 * @param items
	 *            The items not to deposit.
	 * @return true on success.
	 */
	public boolean depositAllExcept(int... items) {
		if (bank.isOpen() || bank.isDepositOpen()) {
			boolean deposit = true;
			int invCount = bank.isOpen() ? inventory.getCount(true) : bank
					.getBoxCount();
			outer: for (int i = 0; i < 28; i++) {
				RSComponent item = bank.isOpen() ? inventory.getItemAt(i)
						.getComponent() : interfaces.get(11).getComponent(17)
						.getComponent(i);
				if (item != null && item.getComponentID() != -1) {
					for (int id : items) {
						if (item.getComponentID() == id) {
							continue outer;
						}
					}
					for (int tries = 0; tries < 5; tries++) {
						if (item.doAction(inventory.getCount(true,
								item.getComponentID()) > 1 ? "Deposit-All"
								: "Deposit")) {
							waitForDepositedItem(random(2000, 3000));
							int cInvCount = bank.isOpen() ? inventory
									.getCount(true) : bank.getBoxCount();
							if (cInvCount < invCount) {
								invCount = cInvCount;
								continue outer;
							}
						}
					}
					deposit = false;
				}
			}
			return deposit;
		}
		return false;
	}

	private boolean WD(int itemID, int count) {
		int tempCount = count;
		if (count == 0)
			tempCount = Ore2PerSpell;
		if (count < 0)
			throw new IllegalArgumentException("count < 0 (" + count + ")");
		if (!bank.isOpen())
			return false;
		final RSItem item = bank.getItem(itemID);
		if (item == null || !item.isComponentValid()
				|| bank.getCount(itemID) < tempCount) {
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
				if (menu.contains("Withdraw-" + count + " ")) {
					if (menu.doAction("Withdraw-" + count + " ")) {
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

	// ************************************************************************************************************

	public void Quit() {
		Status = "Quitting.";
		if (TakeAShot) {
			env.saveScreenshot(true);
			sleep(500);
		}
		stopScript();
	}

	/*
	 * --------------------------------------------------------------------------
	 * -------------------------
	 * ------------------------------------------------
	 * ---------------------------------------------------
	 * -------------------------------- A N T I - B A N
	 * ----------------------------------------------
	 * ----------------------------------------------------------- Standard &
	 * While banking --------------
	 * ----------------------------------------------
	 * ---------------------------------------------------
	 */
	boolean hoverPlayer() {
		RSPlayer player = null;
		RSPlayer[] validPlayers = players.getAll();

		player = validPlayers[random(0, validPlayers.length - 1)];
		if (player != null) {
			try {
				String playerName = player.getName();
				String myPlayerName = getMyPlayer().getName();
				if (playerName.equals(myPlayerName)) {
					return false;
				}
			} catch (NullPointerException e) {
			}
			try {
				RSTile targetLoc = player.getLocation();
				String name = player.getName();
				Point checkPlayer = calc.tileToScreen(targetLoc);
				if (calc.pointOnScreen(checkPlayer) && checkPlayer != null) {
					mouse.click(checkPlayer, 5, 5, false);
					log("ANTIBAN - Hover Player - Right click on " + name);
				} else {
					return false;
				}
				return true;
			} catch (Exception ignored) {
			}
		}
		return false;
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
					if (menu.doAction("Examine")) {
					} else {
					}
					sleep(random(100, 500));
				} catch (NullPointerException ignored) {
				}
			}
		}
		return null;
	}

	/*
	 * ---------------------------------- ------------ Standard ------------
	 * --------------------------------
	 */
	public void AntiBan() {
		try {
			int chckObj = random(1, (15000 / Percentage));
			int hover = random(1, (10000 / Percentage));
			int checkxp = random(1, (13000 / Percentage));
			int afk = random(1, (10000 / Percentage));
			int camerahh = random(1, (2000 / Percentage));
			int hoverObject = random(1, (8000 / Percentage));

			if (Percentage != 0) {
				if (chckObj == 5) {
					waitForMageTabWO(1500, false);
					Status = "ANTIBAN - Checking Stats.";
					game.openTab(Game.TAB_STATS);
					sleep(random(150, 250));
					if (game.getCurrentTab() != Game.TAB_STATS) {
						return;
					}
					sleep(random(400, 800));
					mouse.move(554, 213, 729 - 554, 428 - 213); // Random level.
					sleep(random(400, 800));
					mouse.move(554, 213, 729 - 554, 428 - 213); // Random level.
					sleep(random(400, 1000));
					mouse.moveRandomly(700);
					sleep(random(300, 500));
					game.openTab(Game.TAB_MAGIC);
				} else if (hover == 5) {
					Status = "ANTIBAN - Clicking a player.";
					hoverPlayer();
					sleep(random(1150, 2800));
					mouse.moveRandomly(750);
					sleep(random(400, 1000));
				} else if (hoverObject == 5) {
					Status = "ANTIBAN - Clicking an object.";
					hoverObject();
					sleep(random(1150, 2800));
					mouse.moveRandomly(750);
					sleep(random(400, 1000));
				} else if (checkxp == 5) {
					waitForMageTabWO(1500, false);
					Status = "ANTIBAN - XP Check.";
					final int GambleInt5 = random(0, 100);
					if (GambleInt5 > 50) {
						game.openTab(Game.TAB_STATS);
						sleep(random(150, 250));
						if (game.getCurrentTab() != Game.TAB_STATS) {
							return;
						}
						sleep(random(400, 800));
						mouse.move(584, 364, 20, 10); // Magic LvL
						sleep(random(800, 1200));
						mouse.move(584, 364, 20, 10); // Magic LvL
						sleep(random(900, 1750));
						mouse.moveRandomly(700);
						sleep(random(300, 800));
						game.openTab(Game.TAB_MAGIC);
					} else if (GambleInt5 < 51) {
						game.openTab(Game.TAB_STATS);
						sleep(random(150, 250));
						if (game.getCurrentTab() != Game.TAB_STATS) {
							return;
						}
						sleep(random(400, 800));
						mouse.move(707, 252, 20, 10); // Smithing LvL
						sleep(random(800, 1200));
						mouse.move(707, 252, 20, 10); // Smithing LvL
						sleep(random(900, 1750));
						mouse.moveRandomly(700);
						sleep(random(300, 800));
						game.openTab(Game.TAB_MAGIC);
					}
				} else if (afk == 5) {
					switch (random(1, 4)) {
					case 1:
						Status = "ANTIBAN - AFK'ing.";
						sleep(random(AFK1, AFK2));
						break;
					case 2:
						Status = "ANTIBAN - AFK'ing.";
						sleep(random(AFK1 / 4, AFK2 / 10));
						mouse.moveRandomly(750);
						sleep(random(AFK1, AFK2));
						break;
					case 3:
						Status = "ANTIBAN - AFK'ing.";
						sleep(random(0, 500));
						mouse.moveRandomly(1000);
						sleep(random(AFK1 / 4, AFK2 / 10));
						mouse.moveRandomly(1500);
						sleep(random(AFK1, AFK2));
						break;
					}
				} else if (camerahh == 5) {
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
						}
						if (random(0, 100) > random(0, 50)) {
							sleep(random(100, 1000));
							new CameraHeightThread().start();
						}
						break;
					}
				}
			}
		} catch (Exception e) {
			e.initCause(e);
		}
		return;
	}

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

	/*
	 * ---------------------------------- -------- While banking --------
	 * --------------------------------
	 */
	public void bankingAntiBan() {
		if ((random(0, (400 / Percentage)) == 2)) {
			int randomTurn = random(1, 4);
			switch (randomTurn) {
			case 1:
				new CameraRotateThread().start();
				break;
			case 2:
				new CameraHeightThread().start();
				break;
			case 3:
				int randomFormation = random(0, 2);
				if (randomFormation == 0) {
					new CameraRotateThread().start();
					new CameraHeightThread().start();
				} else {
					new CameraHeightThread().start();
					new CameraRotateThread().start();
				}
				break;
			}
		}
	}

	// ************************************************************************************************************
	@Override
	public void messageReceived(MessageEvent msg) {
		String message = msg.getMessage().toLowerCase();
		if (message.contains("have enough nat")) {
			log("Out of nature runes!");
			Quit();
		}
		if (message.contains("have enough fir")) {
			log("Out of fire runes!");
			log("Please equip a staff with unlimited fire runes.");
			Quit();
		}
		if (message.contains("coal bag is already")) {
			filled = true;
		}
		if (message.contains("add the coal to your")) {
			filled = true;
		}
	}

	// ************************************************************************************************************
	@Override
	public void onFinish() {
		log(+BarCounter + " " + BarType + " bars made in: " + hours + ":"
				+ minutes + ":" + seconds + ".");
		log("Thank you for using Fallen's Superheater.");
	}

	// ************ G U I
	// **************************************************************************************
	public boolean WaitForStart = true;
	GUI SuperheaterGUI;

	public class GUI extends JFrame implements WindowListener {

		private static final long serialVersionUID = -34342;
		private JPanel contentPane;
		private JPanel Panel1;
		private JPanel Panel2;
		private JPanel Panel3;
		private JTabbedPane TabbedPanel;
		private JButton startButton;
		private JButton exitButton;
		private JButton save;
		private JButton load;
		private JButton profitCheck;
		private JRadioButton Box1;
		private JRadioButton Box2;
		private JRadioButton Box3;
		private JRadioButton Box5; // Coal-bag
		private JComboBox Options;
		private JLabel OptionText;
		private JLabel BoxText;
		private JLabel Slider1Text;
		private JLabel Slider2Text;
		private JLabel FieldText1;
		private JLabel FieldText2;
		private JLabel FieldText3;
		private JFormattedTextField TextField;
		private JFormattedTextField TextField2;
		private JFormattedTextField TextField3;
		private JTextPane TextPanel;
		private JSlider Slider1;
		private JSlider Slider2;
		FallenSuperheater script;

		public GUI(final FallenSuperheater Superheater) {
			script = Superheater;
			initComponents();
		}

		private void initComponents() {

			addWindowListener(this);
			contentPane = (JPanel) getContentPane();
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
			TabbedPanel.addTab("Anti-Ban", Panel2);
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
			String[] types = { "Bronze", "Iron", "Steel", "Silver", "Gold",
					"Mithril", "Adamantite", "Runite" };
			Options = new JComboBox(types);
			OptionText = new JLabel();
			Options.setSelectedItem(types[2]);
			OptionText.setFont(new Font("Verdana", Font.PLAIN, 14));
			OptionText.setText("Which bars to make?");
			Options.setBorder(null);
			Options.setOpaque(false);
			Options.setFocusable(false);
			Options.addActionListener(new java.awt.event.ActionListener() {
				@Override
				public void actionPerformed(final java.awt.event.ActionEvent evt) {
					String item = Options.getSelectedItem().toString();
					if (item != null
							&& (item == "Steel" || item == "Mithril"
									|| item == "Adamantite" || item == "Runite")) {
						Box5.setEnabled(true);
					} else {
						Box5.setEnabled(false);
					}
				}
			});

			Box5 = new JRadioButton();
			Box5.setText("Use coal bag?");
			String item = Options.getSelectedItem().toString();
			if (item != null
					&& (item == "Steel" || item == "Mithril"
							|| item == "Adamantite" || item == "Runite")) {
				Box5.setEnabled(true);
			} else {
				Box5.setEnabled(false);
			}

			profitCheck = new JButton();
			profitCheck.setText("Check profit");
			profitCheck.addMouseListener(new java.awt.event.MouseListener() {
				@Override
				public void mouseClicked(MouseEvent e) {
				}

				@Override
				public void mouseEntered(MouseEvent e) {
				}

				@Override
				public void mouseExited(MouseEvent e) {
				}

				@Override
				public void mousePressed(MouseEvent e) {
					if (profitCheck.contains(e.getPoint())) {
						profitCheck.setText("Loading...");
						profitCheck.setEnabled(false);
					}
				}

				@Override
				public void mouseReleased(MouseEvent e) {
					calcProfit();
					profitCheck.setText("Check profit");
					profitCheck.setEnabled(true);
				}
			});

			TextField = new JFormattedTextField();
			TextField.setValue("5000");
			TextField.setEnabled(false);
			BoxText = new JLabel();
			BoxText.setText("Casts");
			BoxText.setEnabled(false);
			Box1 = new JRadioButton();
			Box1.setText("Log out after...");
			Box1.addActionListener(new java.awt.event.ActionListener() {
				@Override
				public void actionPerformed(final java.awt.event.ActionEvent evt) {
					Box1ButtonActionPerformed();
				}
			});
			Box2 = new JRadioButton();
			Box2.setText("Calculate prices?");
			Box2.setEnabled(true);
			Box2.setSelected(true);

			Box3 = new JRadioButton();
			Box3.setText("Take a screenshot when finished?");
			Box3.setEnabled(true);

			Slider1Text = new JLabel();
			Slider1Text.setFont(new Font("Verdana", Font.PLAIN, 12));
			Slider1Text.setText("Mouse Speed (%)");
			Slider1Text.setEnabled(true);

			Slider1 = new JSlider();
			Slider1.setMaximum(100);
			Slider1.setMinimum(10);
			Slider1.setBorder(null);
			Slider1.setOpaque(false);
			Slider1.setPaintTicks(true);
			Slider1.setPaintLabels(true);
			Slider1.setSnapToTicks(true);
			Slider1.setMajorTickSpacing(10);
			Slider1.setValue(60);
			Slider1.setFocusable(false);

			// ANTIBAN
			Slider2Text = new JLabel();
			Slider2Text.setFont(new Font("Verdana", Font.PLAIN, 12));
			Slider2Text.setText("Anti-Ban efficiency (%)");
			Slider2Text.setEnabled(true);

			Slider2 = new JSlider();
			Slider2.setMaximum(100);
			Slider2.setMinimum(10);
			Slider2.setBorder(null);
			Slider2.setOpaque(false);
			Slider2.setPaintTicks(true);
			Slider2.setPaintLabels(true);
			Slider2.setSnapToTicks(true);
			Slider2.setMajorTickSpacing(10);
			Slider2.setValue(50);
			Slider2.setFocusable(false);

			FieldText1 = new JLabel();
			FieldText1.setFont(new Font("Verdana", Font.PLAIN, 12));
			FieldText1.setText("AFK times (seconds): ");
			FieldText1.setEnabled(true);
			FieldText2 = new JLabel();
			FieldText2.setText("Min:");
			FieldText2.setEnabled(true);
			FieldText3 = new JLabel();
			FieldText3.setText("Max:");
			FieldText3.setEnabled(true);

			TextField2 = new JFormattedTextField();
			TextField2.setValue("2");
			TextField2.setEnabled(true);
			TextField3 = new JFormattedTextField();
			TextField3.setValue("10");
			TextField3.setEnabled(true);

			// INFO
			TextPanel = new JTextPane();
			TextPanel
					.setText("Fallen's Superheater AIO - v5.99\n\n Start the script at any bank. Have natures in your inventory and a staff with unlimited fire runes equipped.\n Place the ores in your bank so that they can be seen.");
			TextPanel.setEditable(false);
			TextPanel.setFont(new Font("Verdana", Font.PLAIN, 12));
			TextPanel.setBorder(new LineBorder(Color.black));

			// PLACEMENT
			setSize(400, 400);
			setTitle("Fallen's Superheater v5.99");
			addComponent(contentPane, startButton, 5, 270, 375, 55);
			addComponent(contentPane, exitButton, 140, 330, 100, 25);
			addComponent(contentPane, TabbedPanel, 5, 5, 375, 260);
			addComponent(contentPane, save, 5, 333, 60, 20);
			addComponent(contentPane, load, 315, 333, 60, 20);
			// PANEL 1
			addComponent(Panel1, OptionText, 113, 10, 250, 25);
			addComponent(Panel1, Options, 130, 42, 90, 26);
			addComponent(Panel1, Box5, 25, 45, 95, 20);
			addComponent(Panel1, profitCheck, 230, 45, 90, 20);

			addComponent(Panel1, Box1, 5, 105, 108, 20);
			addComponent(Panel1, TextField, 115, 104, 100, 20);
			addComponent(Panel1, BoxText, 225, 105, 150, 20);

			addComponent(Panel1, Box2, 5, 130, 200, 20);
			addComponent(Panel1, Box3, 5, 155, 220, 20);
			// PANEL 2
			addComponent(Panel2, Slider2Text, 115, 10, 200, 20);
			addComponent(Panel2, Slider2, 85, 25, 200, 50);
			addComponent(Panel2, Slider1Text, 128, 155, 200, 20);
			addComponent(Panel2, Slider1, 85, 170, 200, 50);

			addComponent(Panel2, FieldText1, 20, 85, 150, 50);
			addComponent(Panel2, FieldText2, 170, 100, 23, 20);
			addComponent(Panel2, TextField2, 196, 100, 30, 20);
			addComponent(Panel2, FieldText3, 240, 100, 27, 20);
			addComponent(Panel2, TextField3, 268, 100, 30, 20);
			// PANEL 3
			addComponent(Panel3, TextPanel, 10, 10, 350, 210);
		}

		private void addComponent(Container container, Component c, int x,
				int y, int width, int height) {
			c.setBounds(x, y, width, height);
			container.add(c);
		}

		private void startButtonActionPerformed() {

			FallenSuperheater.Mouse1 = Slider1.getValue();
			FallenSuperheater.Percentage = Slider2.getValue();
			GUIString = Options.getSelectedItem().toString();
			GUIString2 = TextField.getValue();
			GUIString3 = TextField2.getValue();
			GUIString4 = TextField3.getValue();
			FallenSuperheater.AFK1 = Integer.parseInt((String) GUIString3);
			FallenSuperheater.AFK2 = Integer.parseInt((String) GUIString4);
			if (GUIString.equals("Bronze")) {
				BarID = bronzeBar;
				Ore1 = copperOre;
				Ore2 = tinOre;
				Ore1WD = 13;
				Ore2WD = 13;
				bankTwice = true;
				BarType = "Bronze";
				BarEXP = 6.25;
				Ore2PerSpell = 1;
				Ore1InvAm = 13;
				Ore2InvAm = 13;
			} else if (GUIString.equals("Iron")) {
				BarID = ironBar;
				Ore1 = ironOre;
				// Ore2 = ironOre;
				Ore1WD = 0;
				// Ore2WD = 0;
				bankTwice = false;
				BarType = "Iron";
				BarEXP = 12.5;
				Ore2PerSpell = 0;
				Ore1InvAm = 27;
				Ore2InvAm = 0;
			} else if (GUIString.equals("Steel")) {
				BarID = steelBar;
				Ore1 = ironOre;
				Ore2 = coalOre;
				if (Box5.isSelected()) {
					Ore1WD = 17;
					Ore1InvAm = 17;
					Ore2InvAm = 9;
				} else {
					Ore1WD = 9;
					Ore1InvAm = 9;
					Ore2InvAm = 18;
				}
				Ore2WD = 0;
				bankTwice = true;
				BarType = "Steel";
				BarEXP = 17.5;
				Ore2PerSpell = 2;
			} else if (GUIString.equals("Silver")) {
				BarID = silverBar;
				Ore1 = silverOre;
				// Ore2 = silverOre;
				Ore1WD = 0;
				// Ore2WD = 0;
				bankTwice = false;
				BarType = "Silver";
				BarEXP = 13.7;
				Ore2PerSpell = 0;
				Ore1InvAm = 27;
				Ore2InvAm = 0;
			} else if (GUIString.equals("Gold")) {
				BarID = goldBar;
				Ore1 = goldOre;
				// Ore2 = goldOre;
				Ore1WD = 0;
				// Ore2WD = 0;
				bankTwice = false;
				BarType = "Gold";
				BarEXP = 56.2;
				Ore2PerSpell = 0;
				Ore1InvAm = 27;
				Ore2InvAm = 0;
			} else if (GUIString.equals("Mithril")) {
				BarID = mithrilBar;
				Ore1 = mithrilOre;
				Ore2 = coalOre;
				if (Box5.isSelected()) {
					Ore1WD = 10;
					Ore1InvAm = 10;
					Ore2InvAm = 16;
				} else {
					Ore1WD = 5;
					Ore1InvAm = 5;
					Ore2InvAm = 22;
				}
				Ore2WD = 0;
				bankTwice = true;
				BarType = "Mithril";
				BarEXP = 30;
				Ore2PerSpell = 4;

			} else if (GUIString.equals("Adamantite")) {
				BarID = adamantBar;
				Ore1 = adamantOre;
				Ore2 = coalOre;
				if (Box5.isSelected()) {
					Ore1WD = 7;
					Ore1InvAm = 7;
					Ore2InvAm = 19;
				} else {
					Ore1WD = 9;
					Ore1InvAm = 9;
					Ore2InvAm = 18;
				}
				Ore2WD = 0;
				bankTwice = true;
				BarType = "Adamant";
				BarEXP = 37.5;
				Ore2PerSpell = 6;
			} else if (GUIString.equals("Runite")) {
				BarID = runeBar;
				Ore1 = runeOre;
				Ore2 = coalOre;
				if (Box5.isSelected()) {
					Ore1WD = 5;
					Ore1InvAm = 5;
					Ore2InvAm = 21;
				} else {
					Ore1WD = 3;
					Ore1InvAm = 3;
					Ore2InvAm = 24;
				}
				Ore2WD = 0;
				bankTwice = true;
				BarType = "Runite";
				BarEXP = 50;
				Ore2PerSpell = 8;
			}
			// MOUSESPEED
			if (Mouse1 == 100) {
				Mouse2 = 3;
			} else if (Mouse1 == 90) {
				Mouse2 = 4;
			} else if (Mouse1 == 80) {
				Mouse2 = 5;
			} else if (Mouse1 == 70) {
				Mouse2 = 6;
			} else if (Mouse1 == 60) {
				Mouse2 = 7;
			} else if (Mouse1 == 50) {
				Mouse2 = 8;
			} else if (Mouse1 == 40) {
				Mouse2 = 9;
			} else if (Mouse1 == 30) {
				Mouse2 = 10;
			} else if (Mouse1 == 20) {
				Mouse2 = 11;
			} else if (Mouse1 == 10) {
				Mouse2 = 12;
			}
			if (Box2.isSelected()) {
				DoTheMath = true;
			}
			if (Box1.isSelected()) {
				FallenSuperheater.AmountOfCasts = Integer
						.parseInt((String) GUIString2);
			}
			if (Box3.isSelected()) {
				TakeAShot = true;
			}
			if (Box5.isEnabled()) {
				if (Box5.isSelected()) {
					coalBag = true;
				}
			}

			script.WaitForStart = false;
			worked = true;
			dispose();

		}

		private void exitButtonActionPerformed() {
			WaitForStart = false;
			worked = false;
			dispose();
		}

		private void Box1ButtonActionPerformed() {
			if (!BoxText.isEnabled()) {
				BoxText.setEnabled(true);
				TextField.setEnabled(true);
			} else {
				BoxText.setEnabled(false);
				TextField.setEnabled(false);
			}
		}

		private void calcProfit() {
			int value = 0;
			String name;
			Object item = Options.getSelectedItem().toString();
			if (item.equals("Bronze")) {
				BarID = bronzeBar;
				Ore1 = copperOre;
				Ore2 = tinOre;
				BarType = "Bronze";
				Ore2PerSpell = 1;
			}
			if (item.equals("Iron")) {
				BarID = ironBar;
				Ore1 = ironOre;
				Ore2 = 0;
				BarType = "Iron";
				Ore2PerSpell = 0;
			}
			if (item.equals("Steel")) {
				BarID = steelBar;
				Ore1 = ironOre;
				Ore2 = coalOre;
				BarType = "Steel";
				Ore2PerSpell = 2;
			}
			if (item.equals("Silver")) {
				BarID = silverBar;
				Ore1 = silverOre;
				Ore2 = 0;
				BarType = "Silver";
				Ore2PerSpell = 0;
			}
			if (item.equals("Gold")) {
				BarID = goldBar;
				Ore1 = goldOre;
				Ore2 = 0;
				BarType = "Gold";
				Ore2PerSpell = 0;
			}
			if (item.equals("Mithril")) {
				BarID = mithrilBar;
				Ore1 = mithrilOre;
				Ore2 = coalOre;
				BarType = "Mithril";
				Ore2PerSpell = 4;
			}
			if (item.equals("Adamantite")) {
				BarID = adamantBar;
				Ore1 = adamantOre;
				Ore2 = coalOre;
				BarType = "Adamant";
				Ore2PerSpell = 6;
			}
			if (item.equals("Runite")) {
				BarID = runeBar;
				Ore1 = runeOre;
				Ore2 = coalOre;
				BarType = "Runite";
				Ore2PerSpell = 8;
			}
			int Ore1P = grandExchange.lookup(Ore1).getGuidePrice();
			int Ore2P = grandExchange.lookup(Ore2).getGuidePrice()
					* Ore2PerSpell;
			int NatP = grandExchange.lookup(nature).getGuidePrice();
			int BarP = grandExchange.lookup(BarID).getGuidePrice();
			value = BarP - NatP - Ore1P - Ore2P;
			name = grandExchange.getItemName(BarID);
			JOptionPane.showMessageDialog(null, "Profit for each \"" + name
					+ "\" is currently " + value + " GP.", "Profit checker",
					JOptionPane.INFORMATION_MESSAGE, null);
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
			// Type [1 = Y| 2 = N]
			String Type = Options.getSelectedItem().toString();
			file.setProperty("Type", Type);
			// Coal bag?
			if (Box5.isSelected()) {
				file.setProperty("CoalBag", "1");
			} else {
				file.setProperty("CoalBag", "2");
			}
			// Logging out
			if (Box1.isSelected()) {
				file.setProperty("LogOut", "1");
				String Casts;
				try {
					Casts = TextField.getValue().toString();
				} catch (Exception e) {
					Casts = "5000";
				}
				file.setProperty("Casts", Casts);
			} else {
				file.setProperty("LogOut", "2");
				file.setProperty("Casts", "5000");
			}
			// Calculate prices?
			if (Box2.isSelected()) {
				file.setProperty("Calculate", "1");
			} else {
				file.setProperty("Calculate", "2");
			}
			// Screenshot?
			if (Box3.isSelected()) {
				file.setProperty("ScreenShot", "1");
			} else {
				file.setProperty("ScreenShot", "2");
			}
			// Anti-Ban -efficiency
			String AntiBan = String.valueOf(Slider2.getValue());
			file.setProperty("AntiBan", AntiBan);
			// AFK-times
			String min = "2";
			String max = "10";
			try {
				min = (String) TextField2.getValue();
				max = (String) TextField3.getValue();
			} catch (Exception e) {
				min = "2";
				max = "10";
			}
			file.setProperty("Min", min);
			file.setProperty("Max", max);
			// MouseSpeed
			String Mouse = String.valueOf(Slider1.getValue());
			file.setProperty("Mouse", Mouse);
			// Saving the file.
			try {
				file.store(
						new FileWriter(new File(GlobalConfiguration.Paths
								.getSettingsDirectory(),
								"FallenSuperheater.ini")),
						"Options for FallenSuperheater.");
				return true;
			} catch (IOException e) {
				return false;
			}
		}

		private boolean loadOptions(Properties file) {
			try {
				file.load(new FileInputStream(new File(
						GlobalConfiguration.Paths.getSettingsDirectory(),
						"FallenSuperheater.ini")));
			} catch (FileNotFoundException e) {
				return false;
			} catch (IOException e) {
				return false;
			}
			// Assuming we succeeded @ loading.
			if (file.getProperty("Type") != null) {
				Options.setSelectedItem(file.getProperty("Type"));
			}
			if (file.getProperty("CoalBag") != null) {
				if (file.getProperty("CoalBag").equals("1")) {
					Box5.setEnabled(true);
					Box5.setSelected(true);
				} else {
					Box5.setSelected(false);
				}
			}
			if (file.getProperty("LogOut") != null) {
				if (file.getProperty("LogOut").equals("1")) {
					Box1.setSelected(true);
					TextField.setEnabled(true);
					BoxText.setEnabled(true);
					if (file.getProperty("Casts") != null) {
						TextField.setValue(file.getProperty("Casts"));
					}
				} else {
					Box1.setSelected(false);
					TextField.setEnabled(false);
					BoxText.setEnabled(false);
				}
			}
			if (file.getProperty("Calculate") != null) {
				if (file.getProperty("Calculate").equals("1")) {
					Box2.setSelected(true);
				} else {
					Box2.setSelected(false);
				}
			}
			if (file.getProperty("ScreenShot") != null) {
				if (file.getProperty("ScreenShot").equals("1")) {
					Box3.setSelected(true);
				} else {
					Box3.setSelected(false);
				}
			}
			if (file.getProperty("AntiBan") != null) {
				Slider2.setValue(Integer.parseInt(file.getProperty("AntiBan")));
			}
			if (file.getProperty("Min") != null) {
				TextField2.setValue(file.getProperty("Min"));
			}
			if (file.getProperty("Max") != null) {
				TextField3.setValue(file.getProperty("Max"));
			}
			if (file.getProperty("Mouse") != null) {
				Slider1.setValue(Integer.parseInt(file.getProperty("Mouse")));
			}
			return true;
		}
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void mouseEntered(MouseEvent e) {

	}

	@Override
	public void mouseExited(MouseEvent e) {

	}

	@Override
	public void mousePressed(MouseEvent e) {
		Point p = new Point(e.getPoint());
		final Rectangle toggleRectangle = new Rectangle(497, 458, 15, 14);
		if (toggleRectangle.contains(p) && renew) {
			if (Option == 1) {
				Option = 2;
				renew = false;
			} else if (Option == 2) {
				Option = 3;
				renew = false;
			} else if (Option == 3) {
				Option = 1;
				renew = false;
			}
		}
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		renew = true;
	}

	@Override
	public void mouseDragged(MouseEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void mouseMoved(MouseEvent e) {
		Point p = new Point(e.getPoint());
		final Rectangle smithRectangle1 = new Rectangle(262, 358, 170, 17);
		final Rectangle smithRectangle2 = new Rectangle(271, 215, 170, 17);
		final Rectangle mageRectangle1 = new Rectangle(53, 358, 170, 17);
		final Rectangle mageRectangle2 = new Rectangle(62, 215, 170, 17);

		if (Option == 1) {
			if (smithRectangle1.contains(p)) {
				mouseSpot = new Point(e.getPoint());
				STTNL = true;
			} else {
				mouseSpot = new Point(e.getPoint());
				STTNL = false;
			}
			if (mageRectangle1.contains(p)) {
				mouseSpot = new Point(e.getPoint());
				MTTNL = true;
			} else {
				mouseSpot = new Point(e.getPoint());
				MTTNL = false;
			}
		}
		if (Option == 2) {
			if (smithRectangle2.contains(p)) {
				mouseSpot = new Point(e.getPoint());
				STTNL = true;
			} else {
				mouseSpot = new Point(e.getPoint());
				STTNL = false;
			}
			if (mageRectangle2.contains(p)) {
				mouseSpot = new Point(e.getPoint());
				MTTNL = true;
			} else {
				mouseSpot = new Point(e.getPoint());
				MTTNL = false;
			}
		}
	}
}
