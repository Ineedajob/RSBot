/**
 * @author White Bear
 * @copyright (C)2010-2011 White Bear
 * 			No one except White Bear has the right to modify this script!
 */

import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.Rectangle2D;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Properties;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFormattedTextField;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;

import org.rsbot.event.events.MessageEvent;
import org.rsbot.event.listeners.MessageListener;
import org.rsbot.event.listeners.PaintListener;
import org.rsbot.script.Script;
import org.rsbot.script.ScriptManifest;
import org.rsbot.script.methods.Bank;
import org.rsbot.script.methods.Game;
import org.rsbot.script.methods.GrandExchange.GEItem;
import org.rsbot.script.methods.Players;
import org.rsbot.script.methods.Skills;
import org.rsbot.script.wrappers.RSComponent;
import org.rsbot.script.wrappers.RSGroundItem;
import org.rsbot.script.wrappers.RSItem;
import org.rsbot.script.wrappers.RSNPC;
import org.rsbot.script.wrappers.RSObject;
import org.rsbot.script.wrappers.RSPlayer;
import org.rsbot.script.wrappers.RSTile;
import org.rsbot.util.GlobalConfiguration;

@ScriptManifest(authors = { "WhiteBear" }, keywords = "Magic All in One", name = "White Bear AIO Magic", version = 2.11, description = "Flawless All-in-One script for training magic!", website = "http://whitebearrs.orgfree.com")
public class WhiteBearAIOMagic extends Script implements PaintListener,
		MessageListener, MouseListener, MouseMotionListener {

	// ------------VARIABLES--------------\\
	final ScriptManifest properties = getClass().getAnnotation(
			ScriptManifest.class);
	WhiteBearPaint thePainter = new WhiteBearPaint();
	ChatResponder chatRes;
	Antiban antiban = new Antiban();
	private final Properties WBini = new Properties();

	private int lvlAmt = 0, minMS = 4, maxMS = 6;
	private int priceGuide = 1, bankCount = 0;
	private int exitStage = 0, camTurned = 0, resCount = 0;
	private int midTime = 90, randTime = 120, midLength = 10, randLength = 20;
	private int counter = 0, relogAfter = -1, itemID = -1, npcID = -1,
			bankMode = 0;
	private int doWhat = -1, component = -1, pastExp = 0, count1 = 0,
			curseComponent = -1;
	private int barID = 0, ore1ID = 0, ore2ID = 0, ore1Amt = 0, ore2Amt = 0,
			foodItem = 0, bolteId = -1, boltComponent = -1, floor = 0;
	private double totalExp = 0, exp = 0, expGained = 0;
	private final double curseExpGained = 0;
	private long stopTime = -1, nextBreak = System.currentTimeMillis(),
			nextLength = 60000;
	private long endTime = System.currentTimeMillis(), nextRun = System
			.currentTimeMillis(), curseWait = System.currentTimeMillis();
	private boolean unactivated = true, foundType = false, breakLogout = false;
	private boolean logOutInfo = false, tradeResponse = false,
			doBanking = false, invFull = false;
	private boolean useChatRes = true, useFkeys = true, guiStart = false,
			chatResGUI = false;
	private boolean useBreaking = false, randomBreaking = false,
			checkUpdates = false, currentlyBreaking = false;
	private boolean useRemote = false, doingRemote = false, doRelog = false,
			logOutR = false, clicked = false;
	private String totalTime = "00:00:00", itemtype = "Unknown",
			spellName = "", barName = "null";
	private String remoteName = "", remoteMsg = "", remoteReply = "",
			status = "Loading...", colour, lastMessage = null;
	private RSTile startLoc = null;

	private enum State {
		Antiban, Teleport, Alchemy, Curse, Superheat, Bones, Enchant, AlchNCurse, Telekinetic, Bolts
	}

	// ------------MAIN LOOP--------------\\
	@Override
	public int loop() {
		try {
			mouse.setSpeed(random(minMS, maxMS));
			if (unactivated) {
				activate();
				unactivated = false;
			}
			if (game.getClientState() != 10) {
				interfaces.getComponent(976, 6).doClick();
				env.enableRandom("Login");
				return random(50, 100);
			}
			env.disableRandom("Login");
			if (counter == 398) {
				env.saveScreenshot(false);
				counter = 397;
			}
			if (logOutR || exitStage == 2) {
				doLogOut(false, true);
				return 1000;
			}
			if (stopTime > 0 && System.currentTimeMillis() > stopTime) {
				log("Stop Time Reached. Logging off in 5 seconds.");
				sleep(random(4950, 5600));
				doLogOut(false, true);
			}
			if (doingRemote) {
				if (doRelog) {
					endTime = System.currentTimeMillis()
							+ (relogAfter * 60 * 1000);
					doLogOut(false, false);
					while (endTime > System.currentTimeMillis()) {
						sleep(100);
					}
					doingRemote = false;
				} else {
					doLogOut(false, true);
					return 100;
				}
			}
			if (antiban.breakingCheck() && useBreaking) {
				long endTime = System.currentTimeMillis() + nextLength;
				currentlyBreaking = true;
				if (breakLogout) {
					doLogOut(true, false);
				}
				log("Taking a break for "
						+ thePainter.formatTime((int) nextLength));
				while (System.currentTimeMillis() < endTime
						&& currentlyBreaking == true) {
					sleep(1000);
				}
				currentlyBreaking = false;
				antiban.breakingNew();
				return 10;
			}

			if (!thePainter.savedStats) {
				startLoc = getMyPlayer().getLocation();
				camera.setPitch(true);
				thePainter.saveStats();
				return 100;
			}

			// interface checks
			interfaces.getComponent(741, 9).doClick();
			interfaces.getComponent(620, 18).doClick();
			interfaces.getComponent(109, 13).doClick();
			interfaces.getComponent(335, 19).doClick();

			if (!foundType) {
				floor = game.getPlane();
				thePainter.savedStats = true;
				startLoc = getMyPlayer().getLocation();
				if (doWhat == 0) {
					GEItem i = grandExchange.lookup(563);
					if (i != null) {
						priceGuide = i.getGuidePrice();
					} else {
						priceGuide = 0;
					}
					itemtype = "Law Rune";
				} else if (doWhat == 5) {
					GEItem i = grandExchange.lookup(barID);
					if (i != null) {
						priceGuide = i.getGuidePrice();
					} else {
						priceGuide = 0;
					}
					itemtype = barName;
				} else if (doWhat == 8) {
					GEItem i = grandExchange.lookup(bolteId);
					if (i != null) {
						priceGuide = i.getGuidePrice();
					} else {
						priceGuide = 0;
					}
				} else {
					GEItem i = grandExchange.lookup(itemID);
					if (i != null) {
						priceGuide = i.getGuidePrice();
					} else {
						priceGuide = 0;
					}
					itemtype = "Item " + itemID;
				}
				foundType = true;
			}
			try {
				RSPlayer modC = antiban.getNearbyMod();
				if (modC != null) {
					if (System.currentTimeMillis() < chatRes.nextModAlert) {
						chatRes.nextModAlert += 150000;
						log.warning("[MOD] There is a Moderator nearby! Name: "
								+ modC.getName());
					}
				}
			} catch (Exception e) {
			}

			return doAction();
		} catch (java.lang.Throwable t) {
			return 100;
		}
	}

	private int doAction() {
		switch (getState()) {
		case Bolts:
			status = "Enchanting Bolts";
			if (useFkeys && game.getCurrentTab() != Game.TAB_MAGIC) {
				keyboard.pressKey((char) KeyEvent.VK_F4);
				sleep(random(50, 110));
				keyboard.releaseKey((char) KeyEvent.VK_F4);
				sleep(random(160, 220));
			}
			if (!interfaces.get(432).isValid()) {
				magic.castSpell(component);
				sleep(random(400, 600));
			}
			if (interfaces.get(432).isValid()) {
				if (!interfaces.getComponent(432, boltComponent).doAction(
						"Make 10")) {
					sleep(random(300, 400));
					if (!interfaces.getComponent(432, boltComponent).doAction(
							"Make 10")) {
						sleep(random(300, 400));
						if (interfaces.get(432).isValid()
								&& !interfaces.getComponent(432, boltComponent)
										.doAction("Make 10")) {
							sleep(random(300, 400));
							if (!interfaces.getComponent(432, boltComponent)
									.doAction("Make 10")) {
								sleep(random(300, 400));
								interfaces.getComponent(432, 12).doClick();
								log.warning("Unable to cast Enchant Crossbow Bolt spell for 4 tries, logging out.");
								logOutR = true;
								return random(200, 300);
							}
						}
					}
				}
			}
			if (random(0, 4) == 0) {
				if (antiban.lookAway()) {
					return random(50, 100);
				}
			}
			if (random(0, 3) == 0) {
				antiban.main(true);
				sleep(random(190, 300));
			} else {
				sleep(random(460, 550));
			}
			int mx = 0;
			while (valid() && getMyPlayer().getAnimation() == -1 && mx < 26) {
				mx += 1;
				sleep(random(49, 52));
			}
			mx = 0;
			while (valid() && getMyPlayer().getAnimation() != -1 && mx < 46) {
				mx += 1;
				sleep(random(49, 52));
				if (getMyPlayer().getAnimation() > 4000
						&& getMyPlayer().getAnimation() < 5000)
					mx = 20;
			}
			return random(20, 50);
		case Teleport:
			status = "Cast " + spellName;
			if (useFkeys && game.getCurrentTab() != Game.TAB_MAGIC) {
				keyboard.pressKey((char) KeyEvent.VK_F4);
				sleep(random(50, 110));
				keyboard.releaseKey((char) KeyEvent.VK_F4);
				sleep(random(160, 220));
			}
			magic.castSpell(component);
			if (random(0, 4) == 0) {
				if (antiban.lookAway()) {
					return random(50, 100);
				}
			}
			if (random(0, 3) == 0) {
				antiban.main(true);
				sleep(random(190, 300));
			} else {
				sleep(random(460, 550));
			}
			int m = 0;
			while (valid() && getMyPlayer().getAnimation() == 8939 && m < 46) {
				m += 1;
				sleep(random(49, 52));
			}
			return 20;
		case Alchemy:
			status = "Cast " + spellName;
			if (clicked || !magic.isSpellSelected()) {
				if (useFkeys && game.getCurrentTab() != Game.TAB_MAGIC) {
					keyboard.pressKey((char) KeyEvent.VK_F4);
					sleep(random(50, 110));
					keyboard.releaseKey((char) KeyEvent.VK_F4);
					sleep(random(160, 220));
				}
				magic.castSpell(component);
				clicked = false;
			}
			if (magic.isSpellSelected()) {
				if (useFkeys && game.getCurrentTab() != Game.TAB_INVENTORY) {
					keyboard.pressKey((char) KeyEvent.VK_F1);
					sleep(random(50, 110));
					keyboard.releaseKey((char) KeyEvent.VK_F1);
					sleep(random(160, 220));
				}
				for (int i = 0; i < 11; i++) {
					sleep(50);
					if (game.getCurrentTab() == Game.TAB_INVENTORY)
						break;
				}
				if (inventory.getItem(itemID) == null) {
					sleep(random(150, 250));
					if (inventory.getItem(itemID) == null) {
						sleep(random(150, 250));
						if (inventory.getItem(itemID) == null) {
							sleep(random(800, 1100));
							if (inventory.getItem(itemID) == null) {
								RSItem[] items = equipment.getItems();
								for (RSItem i : items) {
									if (i.getID() == itemID) {
										i.doAction("Remove");
										return 100;
									}
								}
								log.warning("You have run out of the selected item ("
										+ itemID + ")! Logging out!");
								doLogOut(false, true);
								return 100;
							}
						}
					}
				}
				if (inventory.getItem(itemID).doAction("Cast"))
					clicked = true;
				if (random(0, 4) == 0) {
					if (antiban.lookAway()) {
						return random(50, 100);
					}
				}
				if (random(0, 3) == 0) {
					antiban.main(true);
					sleep(random(190, 300));
				} else {
					sleep(random(460, 550));
				}
				sleep(500);
				int maxA = 0;
				while (valid() && game.getCurrentTab() != 7 && maxA <= 23) {
					maxA += 1;
					sleep(random(99, 102));
				}
			}
			return 10;
		case Curse:
			if (curseWait < System.currentTimeMillis()) {
				status = "Cast " + spellName;
				if (!magic.isSpellSelected()) {
					if (useFkeys && game.getCurrentTab() != Game.TAB_MAGIC) {
						keyboard.pressKey((char) KeyEvent.VK_F4);
						sleep(random(50, 110));
						keyboard.releaseKey((char) KeyEvent.VK_F4);
						sleep(random(160, 220));
					}
					magic.castSpell(component);
				}
				if (magic.isSpellSelected()) {
					RSNPC target = npcs.getNearest(npcID);
					if (target != null && target.isOnScreen()) {
						if (!target.doAction("Cast")) {
							antiban.main(true);
						} else {
							sleep(500);
						}
						int maxCr = 0;
						while (valid() && getMyPlayer().getAnimation() != -1
								&& maxCr <= 20) {
							maxCr += 1;
							sleep(random(99, 102));
						}
					} else {
						status = "Waiting";
						if (random(0, 4) == 0) {
							if (antiban.curseLA()) {
								return random(50, 100);
							}
						}
						if (random(0, 3) == 0) {
							antiban.main(true);
							sleep(random(190, 300));
						} else {
							sleep(random(460, 550));
						}
					}
				}
			} else {
				status = "Waiting";
				if (random(0, 4) == 0) {
					if (antiban.curseLA()) {
						return random(50, 100);
					}
				}
				if (random(0, 3) == 0) {
					antiban.main(true);
					sleep(random(190, 300));
				} else {
					sleep(random(460, 550));
				}
			}
			return 200;
		case Superheat:
			if (doBanking) {
				status = "Banking";
				openBank();
				sleep(random(400, 450));
				if (bank.isOpen()) {
					if (bank.getItem(ore1ID) == null
							|| bank.getItem(ore2ID) == null) {
						sleep(random(400, 600));
						if (bank.getItem(995) == null) {
							bank.close();
							return random(600, 800);
						}
						if (bank.getItem(ore1ID) == null
								|| bank.getItem(ore2ID) == null) {
							sleep(random(600, 800));
							if (bank.getItem(ore1ID) == null
									|| bank.getItem(ore2ID) == null) {
								log.warning("You have run out of the ores required! Logging out!");
								doLogOut(false, true);
								return 100;
							}
						}
					}
					if (inventory.contains(barID) && !bank.deposit(barID, 0)) {
						return 100;
					}
					sleep(random(500, 550));
					int c1 = inventory.getCount(ore1ID), c2 = inventory
							.getCount(ore2ID);
					if (ore2ID == -1) { // silver / gold / iron
						bank.withdraw(ore1ID, 0);
					} else { // needs 2 ores
						if (c1 > 0 && c1 != ore1Amt)
							bank.deposit(ore1ID, 0);
						sleep(random(90, 150));
						if (c2 > 0 && c2 != ore2Amt)
							bank.deposit(ore2ID, 0);
						sleep(random(300, 600));
						bank.withdraw(ore2ID, ore2Amt);
						sleep(random(90, 150));
						bank.withdraw(ore1ID, ore1Amt);
					}
					doBanking = false;
					bankCount = bank.getCount(ore1ID);
					sleep(random(450, 600));
					bank.close();
					return 10;
				}
			} else {
				status = "Casting Superheat";
				if (bank.isOpen()) {
					bank.close();
				}
				if (useFkeys && game.getCurrentTab() != Game.TAB_MAGIC) {
					keyboard.pressKey((char) KeyEvent.VK_F4);
					sleep(random(50, 110));
					keyboard.releaseKey((char) KeyEvent.VK_F4);
					sleep(random(160, 220));
				}
				if (!magic.castSpell(component)) {
					return random(200, 500);
				}
				int m2 = 0;
				while (valid() && m2 < 24
						&& game.getCurrentTab() != Game.TAB_INVENTORY) {
					m2 += 1;
					sleep(50);
				}
				if (!findOre()) {
					doBanking = true;
					return random(100, 200);
				} else {
					if (!getLastOre().doAction("Cast Superheat")) {
						return random(400, 600);
					}
					if (random(0, 4) == 0) {
						if (antiban.lookAway())
							return random(50, 100);
					}
					if (random(0, 3) == 0)
						antiban.main(true);
					sleep(random(350, 550));
					if (useFkeys && game.getCurrentTab() != Game.TAB_MAGIC) {
						keyboard.pressKey((char) KeyEvent.VK_F4);
						sleep(random(50, 110));
						keyboard.releaseKey((char) KeyEvent.VK_F4);
						sleep(random(160, 220));
					}
					int m3 = 0;
					while (valid()
							&& game.getCurrentTab() == Game.TAB_INVENTORY
							&& m3 < 24) {
						m3 += 1;
						sleep(50);
					}
				}
			}
			return 200;
		case Bones:
			if (doBanking) {
				status = "Banking";
				openBank();
				sleep(random(400, 450));
				if (bank.isOpen()) {
					if (bank.getItem(itemID) == null) {
						sleep(random(400, 600));
						if (bank.getItem(995) == null
								&& bank.getItem(foodItem) == null) {
							bank.close();
							return random(600, 800);
						}
						if (bank.getItem(itemID) == null) {
							sleep(random(600, 800));
							if (bank.getItem(itemID) == null) {
								log.warning("You have run out of bones! Logging out!");
								doLogOut(false, true);
								return 100;
							}
						}
					}
					if (inventory.isFull() && inventory.contains(itemID)) {
						doBanking = false;
						return random(100, 300);
					}
					if (inventory.contains(foodItem)) {
						if (bank.deposit(foodItem, 0)) {
							sleep(random(500, 600));
							if (bank.withdraw(itemID, 0)) {
								doBanking = false;
								bankCount = bank.getCount(itemID);
								sleep(random(150, 250));
								bank.close();
							}
						}
					} else if (bank.withdraw(itemID, 0)) {
						doBanking = false;
						bankCount = bank.getCount(itemID);
						sleep(random(150, 250));
						bank.close();
					}
				}
				return 10;
			} else {
				if (bank.isOpen()) {
					bank.close();
				}
				status = "Casting " + spellName;
				if (useFkeys && game.getCurrentTab() != Game.TAB_MAGIC) {
					keyboard.pressKey((char) KeyEvent.VK_F4);
					sleep(random(50, 110));
					keyboard.releaseKey((char) KeyEvent.VK_F4);
					sleep(random(160, 220));
				}
				if (!magic.castSpell(component)) {
					return random(500, 600);
				}
				if (random(0, 4) == 0) {
					if (antiban.lookAway()) {
						return random(50, 100);
					}
				}
				if (random(0, 3) == 0) {
					antiban.main(true);
					sleep(random(150, 250));
				} else {
					sleep(random(350, 400));
				}
				int maxB = 0;
				while (valid() && maxB < 11) {
					maxB += 1;
					sleep(50);
					if (doBanking)
						return 1;
				}
			}
			return random(140, 230);
		case Enchant:
			int[] runes = { 561, 554, 564, 558, 555, 557, 556, itemID };
			if (doBanking) {
				status = "Banking";
				openBank();
				if (bank.isOpen()) {
					sleep(random(500, 750));
					bankCount = bank.getCount(itemID);
					if (inventory.isFull() && inventory.contains(itemID)) {
						doBanking = false;
						return random(100, 300);
					}
					if (!bank.depositAllExcept(runes)) {
						return random(500, 600);
					}
					sleep(random(500, 600));
					if (bank.getItem(itemID) == null) {
						sleep(random(400, 600));
						if (bank.getItem(995) == null) {
							bank.close();
							return random(600, 800);
						}
						if (bank.getItem(itemID) == null) {
							sleep(random(600, 800));
							if (bank.getItem(itemID) == null) {
								log.warning("You have run out of the selected item ("
										+ itemID + ")! Logging out!");
								doLogOut(false, true);
								return 100;
							}
						}
					}
					if (bank.withdraw(itemID, 0)) {
						sleep(random(100, 200));
						bankCount = bank.getCount(itemID);
						doBanking = false;
						bank.close();
					}
				}
				return 10;
			} else {
				bank.close();
				status = "Casting Lvl-" + spellName;
				if (!magic.isSpellSelected()) {
					if (useFkeys && game.getCurrentTab() != Game.TAB_MAGIC) {
						keyboard.pressKey((char) KeyEvent.VK_F4);
						sleep(random(50, 110));
						keyboard.releaseKey((char) KeyEvent.VK_F4);
						sleep(random(160, 220));
					}
					if (magic.castSpell(component)) {
						int maxEE = 0;
						while (valid()
								&& game.getCurrentTab() != Game.TAB_INVENTORY
								&& maxEE < 20) {
							maxEE += 1;
							sleep(50);
						}
					}
				}
				if (magic.isSpellSelected()) {
					if (useFkeys && game.getCurrentTab() != Game.TAB_INVENTORY) {
						keyboard.pressKey((char) KeyEvent.VK_F1);
						sleep(random(50, 110));
						keyboard.releaseKey((char) KeyEvent.VK_F1);
						sleep(random(160, 220));
					}
					if (inventory.getItem(itemID) == null) {
						doBanking = true;
						return 10;
					} else {
						if (!inventory.getItem(itemID).doAction(
								"Cast Lvl-" + spellName)) {
							return random(300, 500);
						}
						if (random(0, 4) == 0) {
							if (antiban.lookAway()) {
								return random(50, 100);
							}
						}
						if (random(0, 3) == 0) {
							antiban.main(true);
							sleep(random(150, 250));
						} else {
							sleep(random(350, 400));
						}
						int maxES = 0;
						while (valid()
								&& game.getCurrentTab() == Game.TAB_INVENTORY
								&& maxES < 20) {
							maxES += 1;
							sleep(50);
						}
						if (inventory.getItem(itemID) == null) {
							doBanking = true;
							return 10;
						}
					}
				}
			}
			return random(10, 90);
		case AlchNCurse:
			status = "Doing Alch + Curse";
			if (useFkeys && game.getCurrentTab() != Game.TAB_MAGIC) {
				keyboard.pressKey((char) KeyEvent.VK_F4);
				sleep(random(50, 110));
				keyboard.releaseKey((char) KeyEvent.VK_F4);
				sleep(random(160, 220));
			}
			RSNPC target = npcs.getNearest(npcID);
			if (target != null && target.isOnScreen()) {
				magic.castSpell(curseComponent);
				sleep(random(130, 200));
				if (magic.isSpellSelected()) {
					if (!target.doAction("Cast")) {
						antiban.main(true);
					}
				}
			} else {
				if (antiban.curseLA()) {
					return random(50, 100);
				}
			}
			magic.castSpell(component);
			sleep(random(150, 280));
			if (magic.isSpellSelected()) {
				if (inventory.getItem(itemID) == null) {
					sleep(random(300, 500));
					if (inventory.getItem(itemID) == null) {
						sleep(random(400, 600));
						if (inventory.getItem(itemID) == null) {
							log.warning("You have run out of the selected item ("
									+ itemID + ")! Logging out!");
							doLogOut(false, true);
							return 100;
						}
					}
				}
				if (inventory.getItem(itemID).doAction("Cast")) {
					if (random(0, 4) == 0) {
						antiban.main(true);
						sleep(random(150, 250));
					} else {
						sleep(random(350, 400));
					}
					int maxA = 0;
					while (valid()
							&& game.getCurrentTab() == Game.TAB_INVENTORY
							&& maxA <= 30) {
						maxA += 1;
						sleep(random(49, 52));
					}
				}
			}
			return random(100, 230);
		case Telekinetic:
			if (invFull) {
				if (useFkeys && game.getCurrentTab() != Game.TAB_INVENTORY) {
					keyboard.pressKey((char) KeyEvent.VK_F1);
					sleep(random(50, 110));
					keyboard.releaseKey((char) KeyEvent.VK_F1);
					sleep(random(160, 220));
				}
				for (int i = 0; i < 28; i++) {
					if (interfaces.getComponent(149, 0).getComponent(i)
							.getComponentID() == itemID) {
						if (interfaces.getComponent(149, 0).getComponent(i)
								.doAction("Drop")) {
							sleep(random(300, 500));
						}
					}
					if (!valid())
						break;
				}
			}
			RSGroundItem item = groundItems.getNearest(itemID);
			if (item == null || item.isOnScreen() == false) {
				status = "Waiting";
				if (!antiban.telekineticLA()) {
					antiban.main(true);
				}
				return random(56, 80);
			}
			if (useFkeys && game.getCurrentTab() != Game.TAB_MAGIC) {
				keyboard.pressKey((char) KeyEvent.VK_F4);
				sleep(random(50, 110));
				keyboard.releaseKey((char) KeyEvent.VK_F4);
				sleep(random(160, 220));
			}
			status = "Telegrabing";
			magic.castSpell(component);
			item.doAction("Cast");
			sleep(random(500, 600));
			int maxTel = 0;
			while (valid() && getMyPlayer().getAnimation() != -1
					&& maxTel <= 30) {
				maxTel += 1;
				sleep(random(49, 52));
			}
			return 10;
		case Antiban:
			antiban.main(true);
			return 100;
		}
		return 100;
	}

	private State getState() {
		if (doWhat != 0 && calc.distanceTo(startLoc) > 2) {
			if (!onTile(startLoc, "Walk here", 0.5, 0.5, 0)) {
				walking.walkTileMM(startLoc);
			}
			sleep(random(800, 1200));
		}
		if ((doWhat == 2 || doWhat == 3) && game.getPlane() != floor) {
			log.warning("Player is no longer on plane " + floor
					+ "! Logging out");
			logOutR = true;
			return State.Antiban;
		}
		switch (doWhat) {
		case 0:
			return State.Teleport;
		case 1:
			return State.Alchemy;
		case 2:
			return State.Curse;
		case 3:
			return State.AlchNCurse;
		case 4:
			return State.Enchant;
		case 5:
			return State.Superheat;
		case 6:
			return State.Bones;
		case 7:
			return State.Telekinetic;
		case 8:
			return State.Bolts;
		}
		status = "Error!";
		return State.Antiban;
	}

	// --------------ON START--------------\\
	@Override
	public boolean onStart() {
		chatRes = new ChatResponder();
		chatRes.start();
		env.disableRandom("Login");
		return true;
	}

	// --------------ON FINISH-------------\\
	@Override
	public void onFinish() {
		counter = 405;
		chatRes.run = false;
		if (game.isLoggedIn() && exitStage < 2 && thePainter.runTime >= 3600000) {
			env.saveScreenshot(false);
		}
		log.info("In just " + totalTime + ", you gained " + totalExp
				+ " magic exp.");
		log.info("Thank you for using White Bear AIO Magic!");
	}

	// --------------ON REPAINT------------\\
	@Override
	public void onRepaint(final Graphics g) {
		// **************Casts Count*************\\
		if (foundType) {
			if (pastExp != skills.getCurrentExp(Skills.MAGIC)) {
				count1 += 1;
				expGained = count1 * exp;
			}
		}
		pastExp = skills.getCurrentExp(Skills.MAGIC);
		// ***************************************\\

		try {
			if (game.isLoggedIn()) {
				final Rectangle nameBlock = new Rectangle(interfaces.get(137)
						.getComponent(54).getAbsoluteX(), interfaces.get(137)
						.getComponent(54).getAbsoluteY() + 2, 89, 13);
				g.setColor(new Color(211, 192, 155, 253));
				try {
					g.fillRect(nameBlock.x, nameBlock.y, nameBlock.width,
							nameBlock.height);
				} catch (Exception e) {
				}
			}
			g.setFont(new Font("sansserif", Font.PLAIN, 12));
			if (thePainter.antialias == true) {
				((Graphics2D) g).setRenderingHint(
						RenderingHints.KEY_ANTIALIASING,
						RenderingHints.VALUE_ANTIALIAS_ON);
				((Graphics2D) g).setRenderingHint(
						RenderingHints.KEY_COLOR_RENDERING,
						RenderingHints.VALUE_COLOR_RENDER_QUALITY);
				((Graphics2D) g).setRenderingHint(RenderingHints.KEY_RENDERING,
						RenderingHints.VALUE_RENDER_QUALITY);
			}
			if (currentlyBreaking) {
				g.setColor(new Color(0, 0, 0));
				g.setColor(Color.BLACK);
				g.setFont(new Font("sansserif", Font.BOLD, 14));
				g.drawString("Currently taking a break!", 10, 30);
			}

			// Paints the Mouse Location
			Point mousey = mouse.getLocation();
			int x = (int) mousey.getX();
			int y = (int) mousey.getY();
			if (System.currentTimeMillis() - mouse.getPressTime() < 900)
				g.setColor(new Color(0, 0, 255, 170));
			else
				g.setColor(new Color(0, 0, 255, 75));
			g.drawLine(x, 0, x, game.getHeight());
			g.drawLine(0, y, game.getWidth(), y);
			g.fillRect(x - 1, y - 1, 3, 3);
		} catch (java.lang.Throwable t) {
		}
		try {
			if (thePainter.savedStats == true && game.getClientState() == 10) {
				thePainter.paint(g);
			}
		} catch (Exception e) {
		}
	}

	private class WhiteBearPaint {
		Rectangle clr1 = new Rectangle(210, 43, 15, 15);
		Rectangle clr2 = new Rectangle(227, 43, 15, 15);
		Rectangle clr3 = new Rectangle(244, 43, 15, 15);
		Rectangle clr4 = new Rectangle(261, 43, 15, 15);
		Rectangle clr5 = new Rectangle(278, 43, 15, 15);
		Rectangle clr6 = new Rectangle(295, 43, 15, 15);
		Rectangle cr1 = new Rectangle(210, 61, 15, 15);
		Rectangle logOut = new Rectangle(295, 79, 55, 15);
		Rectangle logOut2 = new Rectangle(320, 220, 200, 70);
		Rectangle logOutYes = new Rectangle(338, 255, 80, 20);
		Rectangle logOutNo = new Rectangle(423, 255, 80, 20);

		Rectangle r = new Rectangle(7, 345, 408, 114);
		Rectangle r1 = new Rectangle(420, 345, 77, 20);
		Rectangle r2 = new Rectangle(420, 369, 77, 20);
		Rectangle r3 = new Rectangle(420, 392, 77, 20);
		Rectangle r4 = new Rectangle(420, 415, 77, 20);
		Rectangle r5 = new Rectangle(420, 439, 77, 20);
		Rectangle r6 = new Rectangle(420, 439, 77, 20);
		Rectangle r2c = new Rectangle(415, 369, 5, 20);
		Rectangle r3c = new Rectangle(415, 392, 5, 20);
		Rectangle r4c = new Rectangle(415, 415, 5, 20);
		Rectangle r5c = new Rectangle(415, 439, 5, 20);
		Rectangle r6c = new Rectangle(415, 439, 5, 20);

		Rectangle sb1 = new Rectangle(12, 370, 398, 16);
		boolean savedStats = false, antialias = false;
		int currentTab = 0, lastTab = 0;
		int start_exp = 0, start_lvl = 0;
		int gained_exp = 0, gained_lvl = 0;
		int paintX = 7, paintY = 344;
		Point p = new Point(0, 0);
		Color fonts, normalBack, hiddenPaint, lines;
		String font = "sansserif";

		Thread mouseWatcher = new Thread();
		final NumberFormat nf = NumberFormat.getInstance();

		long time_ScriptStart = System.currentTimeMillis();
		long runTime = System.currentTimeMillis() - time_ScriptStart;

		public void proggiePaint(final Graphics g) {
			final String DATE_FORMAT_NOW = "yyyy-MM-dd HH:mm:ss";
			Calendar cal = Calendar.getInstance();
			SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT_NOW);
			String date = sdf.format(cal.getTime());
			if (paintX <= 140 && paintY <= 86) {
				// If main paint is on original location of proggiepaint
				g.setFont(new Font(font, Font.PLAIN, 12));
				g.setColor(normalBack);
				g.fillRect(7, 235, 133, 76);
				g.setColor(fonts);
				g.drawString(date, 17, 257);
				g.drawString(
						"Version " + Double.toString(properties.version()), 17,
						279);
				g.drawString(
						"Magic Level: " + skills.getCurrentLevel(Skills.MAGIC),
						17, 301);
			} else {
				g.setFont(new Font(font, Font.PLAIN, 12));
				g.setColor(normalBack);
				g.fillRect(7, 10, 133, 76);
				g.setColor(fonts);
				g.drawString(date, 17, 32);
				g.drawString(
						"Version " + Double.toString(properties.version()), 17,
						54);
				g.drawString(
						"Magic Level: " + skills.getCurrentLevel(Skills.MAGIC),
						17, 76);
			}
		}

		public void paint(final Graphics g) {
			// Redefine locations of all rectangles //
			r = new Rectangle(paintX, paintY, 408, 114);
			r1 = new Rectangle(paintX + 393, paintY + 117, 15, 18);
			r2 = new Rectangle(paintX + 181, paintY + 117, 40, 18);
			r3 = new Rectangle(paintX + 225, paintY + 117, 35, 18);
			r4 = new Rectangle(paintX + 264, paintY + 117, 40, 18);
			r5 = new Rectangle(paintX + 308, paintY + 117, 30, 18);
			r6 = new Rectangle(paintX + 343, paintY + 117, 45, 18);
			r2c = new Rectangle(paintX + 181, paintY + 114, 40, 3);
			r3c = new Rectangle(paintX + 225, paintY + 114, 35, 3);
			r4c = new Rectangle(paintX + 264, paintY + 114, 40, 3);
			r5c = new Rectangle(paintX + 308, paintY + 114, 30, 3);
			r6c = new Rectangle(paintX + 343, paintY + 114, 45, 3);

			sb1 = new Rectangle(paintX + 5, paintY + 25, 398, 16);
			clr1 = new Rectangle(paintX + 15, paintY + 43, 15, 15);
			clr2 = new Rectangle(paintX + 32, paintY + 43, 15, 15);
			clr3 = new Rectangle(paintX + 49, paintY + 43, 15, 15);
			clr4 = new Rectangle(paintX + 66, paintY + 43, 15, 15);
			clr5 = new Rectangle(paintX + 83, paintY + 43, 15, 15);
			clr6 = new Rectangle(paintX + 100, paintY + 43, 15, 15);
			cr1 = new Rectangle(paintX + 15, paintY + 61, 15, 15);
			logOut = new Rectangle(paintX + 100, paintY + 79, 55, 15);
			// =========================================================//
			g.setFont(new Font(font, Font.PLAIN, 12));
			if (exitStage == 1) {
				g.setColor(new Color(0, 0, 0, 100));
				g.fillRect(logOut2.x, logOut2.y, logOut2.width, logOut2.height);
				g.setColor(Color.WHITE);
				g.drawString("Logout: Are you sure?", logOut2.x + 10,
						logOut2.y + 22);
				g.setColor(Color.RED);
				g.fillRect(logOutYes.x, logOutYes.y, logOutYes.width,
						logOutYes.height);
				g.setColor(Color.GREEN);
				g.fillRect(logOutNo.x, logOutNo.y, logOutNo.width,
						logOutNo.height);
				g.setColor(Color.BLACK);
				g.drawString("YES", logOutYes.x + 28, logOutYes.y + 14);
				g.drawString("NO", logOutNo.x + 29, logOutNo.y + 14);
			}

			runTime = System.currentTimeMillis() - time_ScriptStart;
			totalTime = formatTime((int) runTime);

			currentTab = paintTab();

			NumberFormat formatter = new DecimalFormat("#,###,###");

			if (game.getClientState() == 10) {
				totalExp = skills.getCurrentExp(Skills.MAGIC) - start_exp;
			}

			switch (currentTab) {
			case -1: // PAINT OFF
				g.setColor(hiddenPaint);
				g.fillRect(r1.x, r1.y, r1.width, r1.height);
				g.setColor(fonts);
				drawString(g, "O", r1, 5);
				break;
			case 0: // DEFAULT TAB - MAIN
				drawPaint(g, r2c);
				g.setColor(lines);
				g.drawLine(r.x + 204, r.y + 22, r.x + 204, r.y + 109);
				g.setColor(fonts);
				g.setFont(new Font(font, Font.BOLD, 14));
				drawString(g, properties.name(), r, -40);
				g.setFont(new Font(font, Font.PLAIN, 12));
				drawStringMain(g, "Runtime: ", totalTime, r, 20, 35, 0, true);
				drawStringMain(g, "", status, r, 20, 35, 0, false);
				int castPerHour = 0;
				int xpPerHour = 0;
				totalExp = expGained + curseExpGained;
				if ((runTime / 1000) > 0) {
					castPerHour = (int) ((3600000.0 / runTime) * (count1));
					xpPerHour = (int) ((3600000.0 / runTime) * (expGained + curseExpGained));
				}
				drawStringMain(g, "Casts ", formatter.format((count1)), r, 20,
						35, 2, true);
				drawStringMain(g, "Casts / Hour: ",
						formatter.format((castPerHour)), r, 20, 35, 3, true);

				drawStringMain(g, "EXP Gained: ",
						formatter.format((long) (expGained + curseExpGained)),
						r, 20, 35, 2, false);
				drawStringMain(g, "EXP / Hour: ",
						formatter.format((xpPerHour)), r, 20, 35, 3, false);
				break;
			case 1: // INFO
				drawPaint(g, r3c);
				g.setColor(lines);
				g.drawLine(r.x + 204, r.y + 22, r.x + 204, r.y + 109);
				g.setColor(fonts);
				g.setFont(new Font(font, Font.BOLD, 14));
				drawString(g, properties.name(), r, -40);
				g.setFont(new Font(font, Font.PLAIN, 12));
				drawStringMain(g, "Version: ",
						Double.toString(properties.version()), r, 20, 35, 0,
						true);
				if (foundType == true) {
					if (doWhat != 8) {
						drawStringMain(g, "Amt of " + itemtype + " in Bank:",
								"", r, 20, 35, 2, true);
						drawStringMain(g, "", formatter.format((bankCount)), r,
								20, 35, 3, true);
						drawStringMain(g, "Worth:",
								formatter.format((bankCount * priceGuide)), r,
								20, 35, 4, true);
					}
					drawStringMain(g, itemtype + " Prices", "", r, 20, 35, 0,
							false);
					drawStringMain(g, "Price Guide:",
							Integer.toString(priceGuide) + " coins", r, 20, 35,
							2, false);
				} else {
					drawStringMain(g, "Prices not loaded!", "", r, 20, 35, 0,
							false);
				}
				break;
			case 2: // STATS
				drawPaint(g, r4c);
				g.setColor(lines);
				g.drawLine(r.x + 204, r.y + 43, r.x + 204, r.y + 109);
				drawStats(g);
				g.setColor(fonts);
				g.setFont(new Font(font, Font.BOLD, 14));
				drawString(g, properties.name(), r, -40);
				g.setFont(new Font(font, Font.PLAIN, 12));
				final int xpTL = skills.getExpToNextLevel(Skills.MAGIC);
				final int xpHour = ((int) ((3600000.0 / runTime) * gained_exp));
				final int TTL = (int) (((double) xpTL / (double) xpHour) * 3600000);
				drawStringMain(g, "Current Level:",
						skills.getCurrentLevel(Skills.MAGIC) + "", r, 20, 35,
						2, true);
				drawStringMain(g, "Level Gained:", gained_lvl + " lvl", r, 20,
						35, 3, true);
				drawStringMain(g, "Time to Lvl:", formatTime(TTL), r, 20, 35,
						4, true);

				drawStringMain(g, "XP Gained:", formatter.format(gained_exp)
						+ "xp", r, 20, 35, 2, false);
				drawStringMain(g, "XP / Hour:",
						formatter.format(xpHour) + "xp", r, 20, 35, 3, false);
				drawStringMain(g, "XP to Lvl:", formatter.format(xpTL) + "xp",
						r, 20, 35, 4, false);
				break;
			case 3: // ETC
				drawPaint(g, r5c);
				g.setColor(lines);
				g.drawLine(r.x + 204, r.y + 22, r.x + 204, r.y + 109);
				g.setColor(fonts);
				g.setFont(new Font(font, Font.BOLD, 14));
				drawString(g, properties.name(), r, -40);
				g.setFont(new Font(font, Font.PLAIN, 12));
				if (useBreaking == true) {
					if (randomBreaking == true) {
						drawStringMain(g, "Break Distance:", "Random", r, 20,
								35, 0, true);
						drawStringMain(g, "Break Length:", "Random", r, 20, 35,
								1, true);
					} else {
						drawStringMain(
								g,
								"Break Distance:",
								Integer.toString(midTime) + " \u00B1"
										+ Integer.toString(randTime), r, 20,
								35, 0, true);
						drawStringMain(g, "Break Length:",
								Integer.toString(midLength) + " \u00B1"
										+ Integer.toString(randLength), r, 20,
								35, 1, true);
					}
					drawStringMain(g, "Next Break:",
							formatTime((int) (nextBreak - System
									.currentTimeMillis())), r, 20, 35, 3, true);
					drawStringMain(g, "Break Length:",
							formatTime((int) nextLength), r, 20, 35, 4, true);
				} else {
					drawStringMain(g, "Breaking is disabled!", "", r, 20, 35,
							0, true);
				}
				drawStringMain(g, "Camera Turns:", Integer.toString(camTurned),
						r, 20, 35, 0, false);
				if (useChatRes) {
					drawStringMain(g, "Chat Response:",
							Integer.toString(resCount), r, 20, 35, 3, false);
				} else {
					drawStringMain(g, "Chat Responder is disabled!", "", r, 20,
							35, 3, false);
				}
				if (useRemote) {
					drawStringMain(g, "Remote Control:", "Enabled", r, 20, 35,
							4, false);
				} else {
					drawStringMain(g, "Remote Control is disabled!", "", r, 20,
							35, 4, false);
				}
				break;
			case 4:
				drawPaint(g, r6c);
				g.setColor(lines);
				g.drawLine(r.x + 204, r.y + 22, r.x + 204, r.y + 109);
				g.setColor(fonts);
				g.setFont(new Font(font, Font.BOLD, 14));
				drawString(g, properties.name(), r, -40);
				g.setFont(new Font(font, Font.PLAIN, 12));
				g.setColor(Color.WHITE);
				g.drawString("Settings", paintX + 15, paintY + 31);
				if (useChatRes == true) {
					g.setColor(Color.GREEN);
					g.drawString("Chat Responder ON", cr1.x + 19, cr1.y + 13);
				} else {
					g.setColor(Color.RED);
					g.drawString("Chat Responder OFF", cr1.x + 19, cr1.y + 13);
				}
				g.setColor(new Color(0, 0, 0, 190));
				g.fillRect(clr1.x, clr1.y, clr1.width, clr1.height);
				g.fillRect(cr1.x, cr1.y, cr1.width, cr1.height);
				g.setColor(new Color(0, 0, 70, 190));
				g.fillRect(clr2.x, clr2.y, clr2.width, clr2.height);
				g.setColor(new Color(0, 70, 0, 190));
				g.fillRect(clr3.x, clr3.y, clr3.width, clr3.height);
				g.setColor(new Color(65, 0, 0, 190));
				g.fillRect(clr4.x, clr4.y, clr4.width, clr4.height);
				g.setColor(new Color(65, 0, 65, 190));
				g.fillRect(clr5.x, clr5.y, clr5.width, clr5.height);
				g.setColor(new Color(82, 41, 0, 190));
				g.fillRect(clr6.x, clr6.y, clr6.width, clr6.height);
				g.setColor(Color.WHITE);
				g.drawString("T", cr1.x + 4, cr1.y + 12);
				if (exitStage == 0) {
					g.setColor(new Color(0, 0, 0, 160));
					g.fillRect(logOut.x, logOut.y, logOut.width, logOut.height);
					g.setColor(Color.YELLOW);
					g.drawString("Log Out", logOut.x + 6, logOut.y + 12);
				}
				if (counter < 1) {
					g.setColor(new Color(0, 0, 0, 160));
					g.fillRect(logOut.x + 125, logOut.y, logOut.width + 53,
							logOut.height);
					g.setColor(Color.YELLOW);
					g.drawString("Take Screenshot", logOut.x + 131,
							logOut.y + 12);
				}
				break;
			}
			if (counter > 1) {
				proggiePaint(g);
			}
			if (counter == 400) {
				counter = 398;
			}
			if (counter < 398 && counter > 0) {
				counter -= 1;
			}
		}

		public void saveStats() {
			if (skills.getCurrentLevel(Skills.MAGIC) != 0 && game.isLoggedIn()) {
				nf.setMinimumIntegerDigits(2);
				final int stats = Skills.MAGIC;
				start_exp = skills.getCurrentExp(stats);
				start_lvl = skills.getCurrentLevel(stats);
				savedStats = true;
			}
		}

		public int paintTab() {
			final Point mouse1 = new Point(p);
			if (mouseWatcher.isAlive())
				return currentTab;
			if (thePainter.currentTab == 4 && game.isLoggedIn() == true) {
				if (clr1.contains(mouse1)) {
					colour = "Black";
					setColour();
				}
				if (clr2.contains(mouse1)) {
					colour = "Blue";
					setColour();
				}
				if (clr3.contains(mouse1)) {
					colour = "Green";
					setColour();
				}
				if (clr4.contains(mouse1)) {
					colour = "Red";
					setColour();
				}
				if (clr5.contains(mouse1)) {
					colour = "Purple";
					setColour();
				}
				if (clr6.contains(mouse1)) {
					colour = "Brown";
					setColour();
				}
				if (cr1.contains(mouse1)) {
					mouseWatcher = new Thread(new MouseWatcher(cr1));
					mouseWatcher.start();
					if (useChatRes == true) {
						useChatRes = false;
					} else {
						useChatRes = true;
					}
				}
			}
			if (r1.contains(mouse1)) {
				mouseWatcher = new Thread(new MouseWatcher(r1));
				mouseWatcher.start();
				if (currentTab == -1) {
					return lastTab;
				} else {
					lastTab = currentTab;
					return -1;
				}
			}
			if (currentTab == -1)
				return currentTab;
			if (r2.contains(mouse1))
				return 0;
			if (r3.contains(mouse1))
				return 1;
			if (r4.contains(mouse1))
				return 2;
			if (r5.contains(mouse1))
				return 3;
			if (r6.contains(mouse1))
				return 4;
			return currentTab;
		}

		public void drawPaint(final Graphics g, final Rectangle rect) {
			g.setColor(normalBack);
			g.fillRect(r1.x, r1.y, r1.width, r1.height);
			g.fillRect(r2.x, r2.y, r2.width, r2.height);
			g.fillRect(r3.x, r3.y, r3.width, r3.height);
			g.fillRect(r4.x, r4.y, r4.width, r4.height);
			g.fillRect(r5.x, r5.y, r5.width, r5.height);
			g.fillRect(r6.x, r6.y, r6.width, r6.height);
			g.fillRect(rect.x, rect.y, rect.width, rect.height);
			g.fillRect(r.x, r.y, r.width, r.height);
			g.setColor(fonts);
			g.setFont(new Font(font, Font.PLAIN, 10));
			drawString(g, "X", r1, 4);
			drawString(g, "Main", r2, 4);
			drawString(g, "Info", r3, 4);
			drawString(g, "Stats", r4, 4);
			drawString(g, "Etc", r5, 4);
			drawString(g, "Setting", r6, 4);
			g.setColor(normalBack);
		}

		public void drawStat(final Graphics g, final int index, final int count) {
			g.setFont(new Font(font, Font.PLAIN, 11));
			g.setColor(new Color(97, 97, 97, 185));
			int half = sb1.height / 2;
			g.fillRect(sb1.x, sb1.y, sb1.width, half);
			g.setColor(new Color(60, 60, 60, 185));
			g.fillRect(sb1.x, sb1.y + half, sb1.width, half);
			final int percent = skills.getPercentToNextLevel(Skills.MAGIC);
			g.setColor(new Color(255 - 2 * percent, (int) (1.7 * percent), 0,
					150));
			g.fillRect(sb1.x + 2, sb1.y + 2,
					(int) (((sb1.width - 4) / 100.0) * percent), sb1.height - 4);
			g.setColor(Color.WHITE);
			g.drawString("Magic", sb1.x + 3, sb1.y + 12);
			drawStringEnd(g, percent + "%", sb1, -3, 4);
		}

		public void drawStats(final Graphics g) {
			if (savedStats == true) {
				final int stats = Skills.MAGIC;
				gained_exp = skills.getCurrentExp(stats) - start_exp;
				gained_lvl = skills.getCurrentLevel(stats) - start_lvl;
				drawStat(g, 1, 1);
			}
		}

		public void setColour() {
			if (colour.equals("Blue")) {
				fonts = Color.YELLOW;
				normalBack = new Color(0, 0, 70, 230);
				hiddenPaint = new Color(0, 0, 70, 120);
				lines = new Color(19, 51, 200, 200);
			} else if (colour.equals("Green")) {
				fonts = Color.YELLOW;
				normalBack = new Color(0, 70, 0, 230);
				hiddenPaint = new Color(0, 70, 0, 120);
				lines = new Color(19, 200, 51, 200);
			} else if (colour.equals("Red")) {
				fonts = Color.YELLOW;
				normalBack = new Color(65, 0, 0, 230);
				hiddenPaint = new Color(65, 0, 0, 120);
				lines = new Color(205, 0, 0, 200);
			} else if (colour.equals("Purple")) {
				fonts = new Color(255, 122, 224, 250);
				normalBack = new Color(65, 0, 65, 230);
				hiddenPaint = new Color(65, 0, 65, 120);
				lines = new Color(180, 0, 180, 200);
			} else if (colour.equals("Brown")) {
				fonts = new Color(51, 204, 0, 250);
				normalBack = new Color(82, 41, 0, 230);
				hiddenPaint = new Color(82, 41, 0, 120);
				lines = new Color(142, 91, 0, 200);
			} else {
				fonts = Color.WHITE;
				normalBack = new Color(0, 0, 0, 230);
				hiddenPaint = new Color(0, 0, 0, 130);
				lines = new Color(100, 100, 100, 200);
			}
		}

		public void drawString(final Graphics g, final String str,
				final Rectangle rect, final int offset) {
			final FontMetrics font = g.getFontMetrics();
			final Rectangle2D bounds = font.getStringBounds(str, g);
			final int width = (int) bounds.getWidth();
			g.drawString(str, rect.x + ((rect.width - width) / 2), rect.y
					+ ((rect.height / 2) + offset));
		}

		public void drawStringEnd(final Graphics g, final String str,
				final Rectangle rect, final int xOffset, final int yOffset) {
			final FontMetrics font = g.getFontMetrics();
			final Rectangle2D bounds = font.getStringBounds(str, g);
			final int width = (int) bounds.getWidth();
			g.drawString(str, (rect.x + rect.width) - width + xOffset, rect.y
					+ ((rect.height / 2) + yOffset));
		}

		public void drawStringMain(final Graphics g, final String str,
				final String val, final Rectangle rect, final int xOffset,
				final int yOffset, final int index, final boolean leftSide) {
			final FontMetrics font = g.getFontMetrics();
			final Rectangle2D bounds = font.getStringBounds(val, g);
			final int indexMult = 17;
			final int width = (int) bounds.getWidth();
			if (leftSide) {
				g.drawString(str, rect.x + xOffset, rect.y + yOffset
						+ (index * indexMult));
				g.drawString(val, rect.x + (rect.width / 2) - width - xOffset,
						rect.y + yOffset + (index * indexMult));
			} else {
				g.drawString(str, rect.x + (rect.width / 2) + xOffset, rect.y
						+ yOffset + (index * indexMult));
				g.drawString(val, rect.x + rect.width - width - xOffset, rect.y
						+ yOffset + (index * indexMult));
			}
		}

		public String formatTime(final long milliseconds) {
			final long t_seconds = milliseconds / 1000;
			final long t_minutes = t_seconds / 60;
			final long t_hours = t_minutes / 60;
			final long seconds = t_seconds % 60;
			final long minutes = t_minutes % 60;
			final long hours = t_hours;
			return (nf.format(hours) + ":" + nf.format(minutes) + ":" + nf
					.format(seconds));
		}

		public class MouseWatcher implements Runnable {

			Rectangle rect = null;

			MouseWatcher(final Rectangle rect) {
				this.rect = rect;
			}

			@Override
			public void run() {
				Point mouse1 = new Point(p);
				while (rect.contains(mouse1)) {
					try {
						mouse1 = new Point(p);
						Thread.sleep(50);
					} catch (Exception e) {
					}
				}
			}
		}
	}

	// --------------SERVER MSG------------\\
	@Override
	public void messageReceived(MessageEvent arg0) {
		try {
			String serverString = arg0.getMessage();
			if (arg0.getID() > 0 && arg0.getID() < 10)
				return;
			if (serverString.contains("You've just advanced")) {
				if (lvlAmt == 0) {
					log("[Alert] You have just leveled, thanks to White Bear AIO Magic!");
				} else {
					if (random(1, 3) == 1) {
						log("[Alert] Another level by White Bear AIO Magic!");
					} else {
						log("[Alert] Congratulations! You have just leveled!");
					}
				}
				lvlAmt += 1;
			}
			if (serverString.contains("Oh dear")) {
				log.severe("[Alert] You were killed! Aborting script!");
				logOutR = true;
			}
			if (serverString.contains("wishes to trade with you")) {
				tradeResponse = true;
			}
			if (serverString.contains("System update in")
					&& !serverString.contains(":")) {
				log.warning("There will be a system update soon, so we logged out");
				logOutR = true;
			}
			if (serverString.contains("holding any bones")
					&& !serverString.contains(":")) {
				doBanking = true;
			}
			if (serverString.contains("inventory space to hold that item")
					&& !serverString.contains(":")) {
				invFull = true;
			}
			if (serverString.contains("You do not have enough")
					&& !serverString.contains(":")
					&& serverString.contains("cast this spell")) {
				log.severe("[Alert] You ran out of runes! Logging out!");
				logOutR = true;
			}
			if (serverString.contains("don't have enough runes for that")) {
				log.severe("[Alert] You ran out of runes! Logging out!");
				logOutR = true;
			}
			if (serverString.contains("Your Magic level is not high enough")
					&& !serverString.contains(":")) {
				log.severe("[Alert] You cannot use that spell! Logging out!");
				logOutR = true;
			}
			if (serverString.contains("has already been")
					&& serverString.contains("Your foe")
					&& !serverString.contains(":")) {
				curseWait = System.currentTimeMillis() + 5000;
			}
			if (serverString.contains("currently immune to that spell")
					&& !serverString.contains(":")) {
				curseWait = System.currentTimeMillis() + 5000;
			}
		} catch (java.lang.Throwable t) {
		}
	}

	// ---------------ANTIBAN--------------\\
	private class Antiban {
		int moveMouseB = 75, allRand = 21, cam = 27, skill = 45, player = 18,
				friend = 61;
		boolean checkFriend = false, checkExperience = true,
				screenLookaway = false;

		// Antiban timeouts and next times
		long timeOutA1 = 600000, timeOutA2 = 800000, lengthA1 = 1000,
				lengthA2 = 3000;
		long timeFriend = System.currentTimeMillis(), timeExp = System
				.currentTimeMillis();
		long timeLook = System.currentTimeMillis();
		long timeOutFriend = 20000, timeOutExp = 20000;

		private void main(boolean extras) {
			mouse.setSpeed(random(minMS + 1, maxMS + 1));
			if (!chatRes.typing) {
				if (nextRun < System.currentTimeMillis()
						&& walking.getEnergy() >= random(79, 90)) {
					nextRun = System.currentTimeMillis() + 7000;
					walking.setRun(true);
					sleep(100);
				}
				int random = random(1, allRand);
				if (random == 1) {
					if (random(1, 3) == 1) {
						chatRes.wait = true;
						mouse.move(random(5, game.getWidth()),
								random(5, game.getHeight()));
						chatRes.wait = false;
					}
				}
				if (random == 2) {
					int randCamera = random(1, cam);
					if (randCamera <= 4) {
						camTurned += 1;
						chatRes.wait = true;
						turnCamera();
						chatRes.wait = false;
					}
				}
				if (checkExperience && random == 6) {
					if (System.currentTimeMillis() > timeExp
							&& random(1, skill) == 1
							&& getMyPlayer().getAnimation() != -1) {
						if (game.getCurrentTab() != 1) {
							chatRes.wait = true;
							game.openTab(1);
							Point stats = new Point(interfaces.get(320)
									.getComponent(87).getAbsoluteX() + 20,
									interfaces.get(320).getComponent(87)
											.getAbsoluteY() + 10);
							mouse.move(stats, 5, 5);
							sleepCR(random(28, 31));
							timeExp = System.currentTimeMillis()
									+ (long) random(timeOutExp - 1500,
											timeOutExp + 1500);
							chatRes.wait = false;
						}
					}
				}
				if (random == 7) {
					if (random(0, 2) == 0) {
						if (checkFriend
								&& System.currentTimeMillis() > timeFriend
								&& random(1, friend) == 1) {
							if (getMyPlayer().getAnimation() != -1
									|| (getMyPlayer().isMoving() && calc
											.distanceTo(walking
													.getDestination()) > 5)) {
								chatRes.wait = true;
								game.openTab(9);
								sleepCR(random(18, 25));
								timeFriend = System.currentTimeMillis()
										+ (long) random(timeOutFriend - 1500,
												timeOutFriend + 1500);
								chatRes.wait = false;
							}
						}
					}
				}
				if (random == 8) {
					if (extras == true) {
						final int chance2 = random(1, player);
						if (chance2 == 1) {
							RSPlayer player = players
									.getNearest(Players.ALL_FILTER);
							if (player != null && calc.distanceTo(player) != 0) {
								chatRes.wait = true;
								mouse.move(player.getScreenLocation(), 5, 5);
								sleepCR(random(6, 9));
								mouse.click(false);
								sleepCR(random(15, 17));
								mouse.move(random(10, 450), random(10, 495));
								chatRes.wait = false;
							}
						}
					}
				}
			}
			mouse.setSpeed(random(minMS, maxMS));
		}

		private void turnCamera() {
			if (doWhat == 2 || doWhat == 3 || doWhat == 7)
				return;
			final char[] LR = new char[] { KeyEvent.VK_LEFT, KeyEvent.VK_RIGHT };
			final char[] UD = new char[] { KeyEvent.VK_DOWN, KeyEvent.VK_UP };
			final char[] LRUD = new char[] { KeyEvent.VK_LEFT,
					KeyEvent.VK_RIGHT, KeyEvent.VK_UP, KeyEvent.VK_UP };
			final int randomLR = random(0, 2);
			final int randomUD = random(0, 2);
			final int randomAll = random(0, 4);

			if (random(0, 3) == 0) {
				keyboard.pressKey(LR[randomLR]);
				sleepCR(random(2, 9));
				keyboard.pressKey(UD[randomUD]);
				sleepCR(random(6, 10));
				keyboard.releaseKey(UD[randomUD]);
				sleepCR(random(2, 7));
				keyboard.releaseKey(LR[randomLR]);
			} else {
				keyboard.pressKey(LRUD[randomAll]);
				if (randomAll > 1) {
					sleepCR(random(6, 11));
				} else {
					sleepCR(random(9, 12));
				}
				keyboard.releaseKey(LRUD[randomAll]);
			}
		}

		private boolean lookAway() {
			if (!chatRes.typing && screenLookaway
					&& timeLook < System.currentTimeMillis()
					&& random(0, 111) == 0) {
				chatRes.pause = true;
				status = "Antiban";
				if (random(0, moveMouseB) <= 50) {
					mouse.setSpeed(random(3, 5));
					mouse.move(random(40, game.getWidth() - 50),
							game.getHeight());
					mouse.setSpeed(random(minMS, maxMS));
				}
				int r1 = random(0, 101);
				if (!foundType)
					r1 = 1;
				int to = 0;
				int m = 0;
				int a = (int) (lengthA1 / 100), b = (int) (lengthA2 / 100);
				while (valid() && m < random(a, b)) {
					m++;
					sleep(100);
				}
				if (r1 > 57) {
					to = random(50, 200);
					sleep(to);
				}
				log.info("[Antiban] Simulated looking away from screen, waited an extra "
						+ to + " ms.");
				timeLook = (long) (System.currentTimeMillis() + random(
						timeOutA1, timeOutA2));
				chatRes.pause = false;
				return true;
			}
			return false;
		}

		private boolean telekineticLA() {
			if (!chatRes.typing && screenLookaway
					&& timeLook < System.currentTimeMillis()
					&& random(0, 111) == 0) {
				chatRes.pause = true;
				status = "Look Away";
				if (random(0, moveMouseB) <= 50) {
					mouse.setSpeed(random(3, 5));
					mouse.move(random(40, game.getWidth() - 50),
							game.getHeight());
					mouse.setSpeed(random(minMS, maxMS));
				}
				int r1 = random(0, 101);
				if (!foundType)
					r1 = 1;
				int to = 0;
				int m = 0;
				int max = random(750, 830);
				while (valid() && m < max) {
					RSGroundItem item = groundItems.getNearest(itemID);
					if (item != null && calc.tileOnScreen(item.getLocation())) {
						break;
					}
					m++;
					sleep(100);
				}
				if (r1 > 57) {
					to = random(500, 3500);
					sleep(to);
				}
				log.info("[Antiban] Simulated looking away from screen, waited an extra "
						+ to + " ms.");
				timeLook = (long) (System.currentTimeMillis() + random(
						timeOutA1, timeOutA2));
				chatRes.pause = false;
				return true;
			}
			return false;
		}

		private boolean curseLA() {
			if (!chatRes.typing && screenLookaway
					&& timeLook < System.currentTimeMillis()
					&& random(0, 111) == 0) {
				chatRes.pause = true;
				status = "Look Away";
				if (random(0, moveMouseB) <= 50) {
					mouse.setSpeed(random(3, 5));
					mouse.move(random(40, game.getWidth() - 50),
							game.getHeight());
					mouse.setSpeed(random(minMS, maxMS));
				}
				int r1 = random(0, 101);
				if (!foundType)
					r1 = 1;
				int to = 0;
				int m = 0;
				int max = random(750, 830);
				while (valid() && m < max) {
					RSNPC i = npcs.getNearest(npcID);
					if (i != null && calc.tileOnScreen(i.getLocation())) {
						break;
					}
					m++;
					sleep(100);
				}
				if (r1 > 57) {
					to = random(500, 3500);
					sleep(to);
				}
				log.info("[Antiban] Simulated looking away from screen, waited an extra "
						+ to + " ms.");
				timeLook = (long) (System.currentTimeMillis() + random(
						timeOutA1, timeOutA2));
				chatRes.pause = false;
				return true;
			}
			return false;

		}

		private RSPlayer getNearbyMod() {
			RSPlayer[] modCheck = players.getAll();
			int Dist = 18;
			RSPlayer closest = null;
			int element = 0;
			int size = modCheck.length;
			while (element < size) {
				if (modCheck[element] != null) {
					try {
						if (modCheck[element].getName().startsWith("Mod")) {
							int distance = calc.distanceTo(modCheck[element]);
							if (distance < Dist) {
								Dist = distance;
								closest = modCheck[element];
							}
						}
					} catch (Exception ignored) {
					}
				}
				element += 1;
			}
			return closest;
		}

		private void breakingNew() {
			if (randomBreaking) {
				long varTime = random(7200000, 18000000);
				nextBreak = System.currentTimeMillis() + varTime;
				long varLength = random(120000, 600000);
				nextLength = varLength;
			} else {
				int diff = randTime * 1000 * 60;
				long varTime = random((midTime * 1000 * 60) - diff,
						(midTime * 1000 * 60) + diff);
				nextBreak = System.currentTimeMillis() + varTime;
				int diff2 = randLength * 1000 * 60;
				long varLength = random((midLength * 1000 * 60) - diff2,
						(midLength * 1000 * 60) + diff2);
				nextLength = varLength;
			}
		}

		private boolean breakingCheck() {
			if (nextBreak <= System.currentTimeMillis()) {
				return true;
			}
			return false;
		}

		private boolean personalize() {
			try {
				WBini.load(new FileInputStream(new File(
						GlobalConfiguration.Paths.getSettingsDirectory(),
						"WhiteBearAIOMagicV2.ini")));
			} catch (java.lang.Exception e) {
			}
			if (WBini.getProperty("ABallRand") == null)
				WBini.setProperty("ABallRand", Integer.toString(random(20, 23)));
			if (WBini.getProperty("ABcam") == null)
				WBini.setProperty("ABcam", Integer.toString(random(26, 29)));
			if (WBini.getProperty("ABskill") == null)
				WBini.setProperty("ABskill", Integer.toString(random(44, 49)));
			if (WBini.getProperty("ABplayer") == null)
				WBini.setProperty("ABplayer", Integer.toString(random(19, 23)));
			if (WBini.getProperty("ABfriend") == null)
				WBini.setProperty("ABfriend", Integer.toString(random(59, 69)));

			if (WBini.getProperty("ABtimeOutFriend") == null)
				WBini.setProperty("ABtimeOutFriend",
						Integer.toString(random(33000, 60000)));
			if (WBini.getProperty("ABtimeOutExp") == null)
				WBini.setProperty("ABtimeOutExp",
						Integer.toString(random(25000, 50000)));

			if (WBini.getProperty("ABmoveMouseB") == null)
				WBini.setProperty("ABmoveMouseB",
						Integer.toString(random(55, 105)));
			try {
				WBini.store(
						new FileWriter(new File(GlobalConfiguration.Paths
								.getSettingsDirectory(),
								"WhiteBearAIOMagicV2.ini")),
						"The GUI Settings for White Bear AIO Magic (Version: "
								+ Double.toString(properties.version()) + ")");
			} catch (java.lang.Exception e) {
				log.severe("[ERROR] Could not save settings file!");
				return false;
			}
			boolean load = antiban.load();
			while (!load) {
				load = antiban.load();
			}
			return true;
		}

		private boolean load() {
			try {
				WBini.load(new FileInputStream(new File(
						GlobalConfiguration.Paths.getSettingsDirectory(),
						"WhiteBearAIOMagicV2.ini")));
			} catch (java.lang.Exception e) {
				log.severe("[ERROR] Could not load settings file!");
				return false;
			}
			if (WBini.getProperty("ABallRand") != null)
				allRand = Integer.parseInt(WBini.getProperty("ABallRand"));
			if (WBini.getProperty("ABcam") != null)
				cam = Integer.parseInt(WBini.getProperty("ABcam"));
			if (WBini.getProperty("ABskill") != null)
				skill = Integer.parseInt(WBini.getProperty("ABskill"));
			if (WBini.getProperty("ABplayer") != null)
				player = Integer.parseInt(WBini.getProperty("ABplayer"));
			if (WBini.getProperty("ABfriend") != null)
				friend = Integer.parseInt(WBini.getProperty("ABfriend"));

			if (WBini.getProperty("ABtimeOutFriend") != null)
				timeOutFriend = Integer.parseInt(WBini
						.getProperty("ABtimeOutFriend"));
			if (WBini.getProperty("ABtimeOutExp") != null)
				timeOutExp = Integer
						.parseInt(WBini.getProperty("ABtimeOutExp"));
			if (WBini.getProperty("ABmoveMouseB") != null)
				moveMouseB = Integer
						.parseInt(WBini.getProperty("ABmoveMouseB"));
			return true;
		}

		private boolean sleepCR(int amtOfHalfSecs) {
			for (int x = 0; x < (amtOfHalfSecs + 1); x++) {
				sleep(random(48, 53));
				if (chatRes.typing) {
					return false;
				}
			}
			return true;
		}
	}

	// --------------METHODS---------------\\
	private void activate() {
		if (!game.isFixed()) {
			log.warning("Your screen size is not Fixed!");
			log.warning("The script will encounter problems if you don't change it to fixed!");
		}
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				WhiteBearGUI gui = new WhiteBearGUI();
				gui.WhiteBearGUI.setVisible(true);
			}
		});
		while (!guiStart) {
			sleep(100);
		}
		thePainter.setColour();
		if (stopTime > 0) {
			log.info("Script will stop after "
					+ thePainter.formatTime((int) stopTime));
			long stoppingTime = stopTime + System.currentTimeMillis();
			stopTime = stoppingTime;
		}
		if (checkUpdates) {
			URLConnection url = null;
			BufferedReader in = null;
			try {
				url = new URL(
						"http://whitebearrs.orgfree.com/content/updater.php?script=magic&type=all")
						.openConnection();
				in = new BufferedReader(new InputStreamReader(
						url.getInputStream()));
				String ver = in.readLine();
				String link = in.readLine();
				String update = in.readLine();
				if (Double.parseDouble(ver) > properties.version()) {
					log.warning("Latest version: " + ver
							+ "! Please update the script!");
					log.info("In this update: " + update);
					log.info("Download from: " + link);
				} else if (Double.parseDouble(ver) < properties.version()) {
					log.info("You are using a beta version of this script!");
				} else {
					log.info("You are using the latest version of this script!");
				}
				if (in != null)
					in.close();
			} catch (java.lang.Exception e) {
				log.warning("An error occurred while checking for update!");
			}
		}
		antiban.breakingNew();
		boolean per = antiban.personalize();
		while (!per) {
			per = antiban.personalize();
		}
		if (!game.isLoggedIn()) {
			log.warning("You should start the script logged in!");
		}
		thePainter.time_ScriptStart = System.currentTimeMillis();
	}

	private boolean openBank() {
		int failCount = 0;
		try {
			if (!bank.isOpen()) {
				if (bankMode == 1) {
					RSNPC banker = npcs.getNearest(Bank.BANKERS);
					if (onTile(banker.getLocation(), "Bank Banker", 0.5, 0.5, 0)) {
						sleep(800);
						while (!bank.isOpen() && failCount < 7) {
							sleep(random(90, 110));
							if (getMyPlayer().isMoving())
								failCount = 0;
							failCount++;
						}
					} else {
						return false;
					}
				} else if (bankMode == 2) {
					RSObject booth = objects.getNearest(Bank.BANK_BOOTHS);
					if (onTile(booth.getLocation(), "Use-quickly", 0.5, 0.5, 0)) {
						sleep(800);
						while (!bank.isOpen() && failCount < 7) {
							sleep(random(90, 110));
							if (getMyPlayer().isMoving())
								failCount = 0;
							failCount++;
						}
					} else {
						return false;
					}
				} else {
					RSObject chest = objects.getNearest(Bank.BANK_CHESTS);
					if (chest.doAction((bankMode == 3) ? "Bank" : "Use")) {
						sleep(800);
						while (!bank.isOpen() && failCount < 7) {
							sleep(random(90, 110));
							if (getMyPlayer().isMoving())
								failCount = 0;
							failCount++;
						}
					} else {
						return false;
					}
				}
			}
			return true;
		} catch (final Exception e) {
			return false;
		}
	}

	private boolean doLogOut(boolean toLobby, boolean stopScript) {
		status = "Logging out";
		while (bank.isOpen()) {
			bank.close();
			mouse.move(random(10, 430), random(10, 465));
			sleep(random(200, 400));
		}
		while (!game.isOnLogoutTab()) {
			mouse.move(random(game.getWidth() - 15, game.getWidth() - 5),
					random(5, 16));
			mouse.click(true);
			if (bank.isOpen()) {
				bank.close();
			}
			int timesToWait = 0;
			while (!game.isOnLogoutTab() && timesToWait < 5) {
				sleep(random(200, 400));
				timesToWait++;
			}
		}
		int maximum = 0;
		while (game.isLoggedIn() == true && maximum < 20) {
			if (toLobby) {
				interfaces.get(182).getComponent(2).doClick();
			} else {
				interfaces.get(182).getComponent(6).doClick();
			}
			sleep(1000);
		}
		if (!toLobby && stopScript) {
			stopScript(false);
		}
		return true;
	}

	private boolean findOre() {
		if (ore2ID > 0) {
			return (inventory.contains(ore1ID) && inventory.contains(ore2ID));
		}
		return inventory.contains(ore1ID);
	}

	private RSComponent getLastOre() {
		for (int i = 27; i >= 0; i--) {
			if (interfaces.getComponent(149, 0).getComponent(i)
					.getComponentID() == ore1ID) {
				return interfaces.getComponent(149, 0).getComponent(i);
			}
		}
		return null;
	}

	private boolean onTile(final RSTile tile, final String action,
			final double dx, final double dy, final int height) {
		Point checkScreen;
		try {
			checkScreen = calc.tileToScreen(tile, dx, dy, height);
			if (!calc.pointOnScreen(checkScreen)) {
				if (calc.distanceTo(tile) <= 8) {
					if (getMyPlayer().isMoving()) {
						return false;
					}
					walking.walkTileMM(tile);
					walking.sleep(1000);
					return false;
				}
				return false;
			}
		} catch (final Exception e) {
		}
		try {
			boolean stop = false;
			for (int i = 0; i <= 50; i++) {
				checkScreen = calc.tileToScreen(tile, dx, dy, height);
				if (!calc.pointOnScreen(checkScreen)) {
					return false;
				}
				mouse.move(checkScreen);
				final Object[] menuItems = menu.getItems();
				for (int a = 0; a < menuItems.length; a++) {
					if (menuItems[a].toString().toLowerCase()
							.contains(action.toLowerCase())) {
						stop = true;
						break;
					}
				}
				if (stop) {
					break;
				}
			}
		} catch (final Exception e) {
		}
		try {
			return menu.doAction(action);
		} catch (final Exception e) {
		}
		return false;
	}

	private boolean valid() {
		return game.isLoggedIn();
	}

	// -----------MOUSE LISTENER-----------\\
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
	public void mouseReleased(MouseEvent arg0) {
	}

	@Override
	public void mouseMoved(MouseEvent e) {
		thePainter.p = e.getPoint();
	}

	@Override
	public void mouseDragged(MouseEvent arg0) {
		Point p = arg0.getPoint();
		processPaint(p);
	}

	@Override
	public void mousePressed(MouseEvent e) {
		Point p = e.getPoint();
		processPaint(p);

		if (thePainter.logOutYes.contains(p) && exitStage == 1) {
			exitStage = 2;
			if (logOutInfo == false) {
				log("You will be logged out when the current loop ends (i.e. in a while)");
				logOutInfo = true;
			}
		}
		if (thePainter.logOutNo.contains(p) && exitStage == 1) {
			exitStage = 0;
		}
		if (thePainter.logOut.contains(p) && exitStage == 0
				&& thePainter.currentTab == 4) {
			thePainter.currentTab = 0;
			exitStage = 1;
		}
		if (thePainter.currentTab == 4
				&& counter == 0
				&& new Rectangle(thePainter.logOut.x + 125,
						thePainter.logOut.y, thePainter.logOut.width + 53,
						thePainter.logOut.height).contains(p)) {
			thePainter.currentTab = 0;
			counter = 400;
		}
	}

	private void processPaint(Point mouse) {
		Point p = mouse;
		int totalWidth = 408, totalHeight = 135, moveHeight = 114;
		int mouseX = p.x;
		int mouseY = p.y;
		if (game.getClientState() == 10
				&& (mouseX >= thePainter.paintX
						&& mouseX <= (thePainter.paintX + totalWidth)
						&& mouseY >= thePainter.paintY && mouseY <= (thePainter.paintY + moveHeight)))
			if (thePainter.currentTab != -1 && thePainter.currentTab != 4) {
				thePainter.paintX = mouseX - (totalWidth / 2);
				thePainter.paintY = mouseY - (totalHeight / 2);
			}
		if (thePainter.paintX < 4)
			thePainter.paintX = 4;
		if (thePainter.paintY < 4)
			thePainter.paintY = 4;
		if ((thePainter.paintX + totalWidth) > 761)
			thePainter.paintX = 761 - totalWidth;
		if ((thePainter.paintY + totalHeight) > 494)
			thePainter.paintY = 494 - totalHeight;
	}

	// -----------CHAT RESPONDER-----------\\
	private class ChatResponder extends Thread {
		long lastSaidHi = System.currentTimeMillis() - 110000,
				lastDenyBot = System.currentTimeMillis() - 110000;
		long lastLevelUp = System.currentTimeMillis() - 300000,
				nextCustom = System.currentTimeMillis() - 1000000;
		long lastSaidLevel = System.currentTimeMillis() - 110000,
				nextModAlert = System.currentTimeMillis(), sayNo = System
						.currentTimeMillis();
		int level = 0; // records MAGIC level
		boolean run = true, doLevelRes = false, doCustomRes = false;
		boolean typing = false; // read by antiban (true = suppress antiban)
		boolean wait = false; // written by antiban (true = chat responder will
								// wait)
		boolean pause = false; // true if look away from screen is active

		// Chat Responder Customization
		String[] tradeRes = { "No thanks", "No thx", "Nope", "Im fine" },
				greetingRes = { "hi!", "hi.", "hi", "hello", "hello!",
						"hello.", "hello..", "yo", "yo!", "yes?", "what",
						"what?", "hey!" }, botterRes = { "huh", "zzz", "...",
						"???", "?????", "what", "what?", "no", "nop", "nope" },
				levelRes = { "yay", "haha", ":)", "yay!", "yay!!!",
						"finally..." }, customDetect = {}, customRes = {};
		double customTO = 160000, customTOR = 30000;

		@Override
		public void run() {
			while (!thePainter.savedStats || getChatMessage() == null) {
				sleepNE(200);
			}
			while (run) {
				try {
					if (game.getClientState() == 10 && !pause) {
						if (useChatRes && tradeResponse) {
							if (sayNo < System.currentTimeMillis()) {
								tradeResponse = false;
								int timeOut = random(110000, 130000);
								sayNo = System.currentTimeMillis() + timeOut;
								sleepNE(random(300, 700));
								String[] res = tradeRes;
								int rand = random(0, res.length);
								sendText(res[rand]);
								log("[Response] Said No to a Trade Request. Timeout: "
										+ timeOut / 1000 + " sec");
							}
						}
						String m = getChatMessage().toLowerCase();
						if (m != null
								&& !m.equals(lastMessage)
								&& (m.contains(getMyPlayer().getName()
										.toLowerCase() + ": <") != true)) {
							remoteControl(m);
							if (useChatRes) {
								response(m);
							} else {
								sleepNE(random(700, 850));
							}
							lastMessage = m;
						} else {
							sleepNE(random(600, 700));
						}
					} else {
						sleepNE(random(300, 400));
					}
				} catch (java.lang.Throwable t) {
				}
			}
		}

		private boolean findText(String t, String[] check) {
			String[] m = check;
			for (int i = 0; i < m.length; i++) {
				if (t.contains(m[i])) {
					return true;
				}
			}
			return false;
		}

		private void remoteControl(String m) {
			if (useRemote) {
				try {
					String[] m2 = m.split("</col>");
					if (m2[2].toLowerCase().contains(remoteName)) {
						// character talked to you!
						if (m2[3].toLowerCase().contains(remoteMsg)) {
							log.warning("Remote Control password detected! Logging out soon.");
							String ans = "/" + remoteReply;
							sendText(ans);
							doingRemote = true;
						} else {
							log.warning("Your Remote Control Character talked to you!");
						}
					}
				} catch (Exception e) {
				}
			}
		}

		private void response(String m) {
			if (doLevelRes) {
				if (level > 0 && skills.getCurrentLevel(Skills.MAGIC) > level
						&& (System.currentTimeMillis() - 200000) >= lastLevelUp) {
					lastLevelUp = System.currentTimeMillis();
					if (random(0, 11) <= 7
							&& calc.distanceTo(players
									.getNearest(Players.ALL_FILTER)) < 10) {
						resCount++;
						sleepNE(random(200, 600));
						String[] r = levelRes;
						final int ra = random(0, r.length);
						sendText(r[ra]);
						log("[Response] Level Up Response: " + r[ra]);
						sleepNE(random(150, 250));
					}
					level = skills.getCurrentLevel(Skills.MAGIC);
					return;
				}
				level = skills.getCurrentLevel(Skills.MAGIC);
			}
			if ((System.currentTimeMillis() - 150000) >= lastSaidLevel) {
				if (findText(m, new String[] { "magic", "mage", "maging",
						"magik", "maggic" })
						&& findText(m, new String[] { "level", "levl", "lvel",
								"lvl" })) {
					lastSaidLevel = System.currentTimeMillis();
					resCount++;
					sleepNE(random(600, 2000));
					final int random = random(1, 11);
					if (random == 1) {
						sendText("magic lvl "
								+ skills.getCurrentLevel(Skills.MAGIC));
					} else if (random == 2) {
						sendText("level: "
								+ skills.getCurrentLevel(Skills.MAGIC));
					} else if (random == 3) {
						sendText("" + skills.getCurrentLevel(Skills.MAGIC));
					} else if (random == 4) {
						sendText("mines "
								+ skills.getCurrentLevel(Skills.MAGIC));
					} else if (random == 5) {
						sendText("lv " + skills.getCurrentLevel(Skills.MAGIC));
					} else if (random == 6) {
						sendText(Integer.toString(skills
								.getCurrentLevel(Skills.MAGIC)));
					} else if (random > 6) {
						sleepNE(random(100, 200));
						keyboard.sendKey((char) KeyEvent.VK_ENTER);
						sleepNE(random(800, 1300));
						keyboard.sendKey('S');
						sleepNE(random(800, 1300));
						keyboard.sendKey('M');
						sleepNE(random(800, 1300));
						keyboard.sendKey('2');
					}
					log("[Response] Answered to Level Question: '" + m + "'");
					sleepNE(random(200, 300));
					return;
				}
			}
			if (findText(m, new String[] { "bottin", "botin", "botttin",
					"botter", "bottter", "boter", "bootin", "boottin",
					"booter", "bootter" })) {
				if (m.contains("?")
						|| m.contains(getMyPlayer().getName().toLowerCase())
						|| m.contains("!")) {
					if ((System.currentTimeMillis() - 130000) >= lastDenyBot) {
						lastDenyBot = System.currentTimeMillis();
						resCount++;
						sleepNE(random(600, 2000));
						String[] bot = botterRes;
						final int random3 = random(0, bot.length);
						sendText(bot[random3]);
						log("[Response] Answered to Botting Message: '" + m
								+ "'");
						sleepNE(random(150, 250));
						return;
					}
				}
			}
			if (findText(m, new String[] { "hi ", "hello", "hi<", "hey", "hi!",
					"hi.", "yo!", "yo.", "yo<" })) {
				if ((System.currentTimeMillis() - 130000) >= lastSaidHi) {
					lastSaidHi = System.currentTimeMillis();
					resCount++;
					sleepNE(random(600, 1600));
					String[] hi = greetingRes;
					final int random2 = random(0, hi.length);
					sendText(hi[random2]);
					log("[Response] Answered to Greeting: '" + m + "'");
					sleepNE(random(150, 250));
					return;
				}
			}
			if (doCustomRes && findText(m, customDetect)
					&& System.currentTimeMillis() > nextCustom) {
				nextCustom = (long) (System.currentTimeMillis() + random(
						customTO - customTOR, customTO + customTOR));
				resCount++;
				sleepNE(random(500, 1400));
				final int r = random(0, customRes.length);
				sendText(customRes[r]);
				log("[Response] Custom Response: '" + m + "'");
				sleepNE(random(150, 250));
				return;
			}
			sleepNE(random(650, 750));
		}

		private void sendText(final String text) {
			final char[] chs = text.toCharArray();
			typing = true;
			if (wait) {
				for (int i = 0; i < 21; i++) {
					sleepNE(10);
					if (!wait) {
						i = 21;
					}
				}
			}
			for (final char element : chs) {
				keyboard.sendKey(element);
				sleepNE(random(280, 550));
			}
			keyboard.sendKey((char) KeyEvent.VK_ENTER);
			typing = false;
		}

		private String getChatMessage() {
			try {
				String text = null;
				for (int x = 280; x >= 180; x--) {
					if (interfaces.get(137).getComponent(x).getText() != null) {
						if (interfaces.get(137).getComponent(x).getText()
								.contains("<col=")) {
							text = interfaces.get(137).getComponent(x)
									.getText();
							break;
						}
					}
				}
				return text;
			} catch (Exception e) {
			}
			return null;
		}

		private void sleepNE(int ms) {
			try {
				Thread.sleep(ms);
			} catch (Exception e) {
			}
		}
	}

	// -----------------GUI----------------\\
	private class WhiteBearGUI {
		private static final long serialVersionUID = 1L;
		public boolean first = false, useSetting = true;

		public boolean loadSettings() {
			try {
				WBini.load(new FileInputStream(new File(
						GlobalConfiguration.Paths.getSettingsDirectory(),
						"WhiteBearAIOMagicV2.ini")));
			} catch (FileNotFoundException e) {
				log.warning("[GUI] Settings file was not found!");
				first = true;
				return false;
			} catch (IOException e) {
				log.warning("[GUI] Error occurred when loading settings!");
				return false;
			}
			try {
				if (WBini.getProperty("UseSetting") != null)
					useSetting = Boolean.parseBoolean(WBini
							.getProperty("UseSetting"));
			} catch (java.lang.Exception e) {
			}
			if (useSetting) {
				try {
					if (WBini.getProperty("ItemID") != null)
						tfItemID.setText(WBini.getProperty("ItemID"));
					if (WBini.getProperty("NpcID") != null)
						tfNpcID.setText(WBini.getProperty("NpcID"));

					if (WBini.getProperty("SpellTypeCombo") != null)
						spellTypeCombo.setSelectedIndex(Integer.parseInt(WBini
								.getProperty("SpellTypeCombo")));
					if (WBini.getProperty("SuperheatCombo") != null)
						superheatCombo.setSelectedIndex(Integer.parseInt(WBini
								.getProperty("SuperheatCombo")));
					if (WBini.getProperty("BankCombo") != null)
						bankCombo.setSelectedIndex(Integer.parseInt(WBini
								.getProperty("BankCombo")));
					if (WBini.getProperty("AlchemyCombo") != null)
						alchemyCombo.setSelectedIndex(Integer.parseInt(WBini
								.getProperty("AlchemyCombo")));
					if (WBini.getProperty("BoltCombo") != null)
						boltCombo.setSelectedIndex(Integer.parseInt(WBini
								.getProperty("BoltCombo")));
					if (WBini.getProperty("EnchantCombo") != null)
						enchantCombo.setSelectedIndex(Integer.parseInt(WBini
								.getProperty("EnchantCombo")));
					if (WBini.getProperty("CurseCombo") != null)
						curseCombo.setSelectedIndex(Integer.parseInt(WBini
								.getProperty("CurseCombo")));
					if (WBini.getProperty("OthersCombo") != null)
						othersCombo.setSelectedIndex(Integer.parseInt(WBini
								.getProperty("OthersCombo")));
					if (WBini.getProperty("TeleportCombo") != null)
						teleportCombo.setSelectedIndex(Integer.parseInt(WBini
								.getProperty("TeleportCombo")));

					if (WBini.getProperty("UseChatRes") != null)
						radioButton12.setSelected(Boolean.parseBoolean(WBini
								.getProperty("UseChatRes")));
					if (WBini.getProperty("CheckUpdate") != null)
						radioButton25.setSelected(Boolean.parseBoolean(WBini
								.getProperty("CheckUpdate")));
					if (WBini.getProperty("PaintColour") != null)
						clrSelected.setSelectedIndex(Integer.valueOf(WBini
								.getProperty("PaintColour")));
					if (WBini.getProperty("PaintFont") != null)
						tfTextFont.setText(WBini.getProperty("PaintFont"));
					if (WBini.getProperty("Fkeys") != null)
						radioButton23.setSelected(Boolean.parseBoolean(WBini
								.getProperty("Fkeys")));
					if (WBini.getProperty("MinMouseSpeed") != null)
						jTextField.setText(WBini.getProperty("MinMouseSpeed"));
					if (WBini.getProperty("MaxMouseSpeed") != null)
						jTextField2.setText(WBini.getProperty("MaxMouseSpeed"));
					if (WBini.getProperty("Antialias") != null)
						radioButton22.setSelected(Boolean.parseBoolean(WBini
								.getProperty("Antialias")));
					if (WBini.getProperty("Breaking") != null)
						radioButton1.setSelected(Boolean.parseBoolean(WBini
								.getProperty("Breaking")));
					if (WBini.getProperty("RandomBreak") != null)
						radioButton2.setSelected(Boolean.parseBoolean(WBini
								.getProperty("RandomBreak")));
					if (WBini.getProperty("CheckFriend") != null)
						check2.setSelected(Boolean.parseBoolean(WBini
								.getProperty("CheckFriend")));
					if (WBini.getProperty("CheckExperience") != null)
						check3.setSelected(Boolean.parseBoolean(WBini
								.getProperty("CheckExperience")));
					if (WBini.getProperty("ScreenLookaway") != null)
						check4.setSelected(Boolean.parseBoolean(WBini
								.getProperty("ScreenLookaway")));
					if (WBini.getProperty("TimeoutA1") != null)
						jTextField3.setText(WBini.getProperty("TimeoutA1"));
					if (WBini.getProperty("TimeoutA2") != null)
						jTextField4.setText(WBini.getProperty("TimeoutA2"));
					if (WBini.getProperty("TimeoutB1") != null)
						jTextField6.setText(WBini.getProperty("TimeoutB1"));
					if (WBini.getProperty("TimeoutB2") != null)
						jTextField5.setText(WBini.getProperty("TimeoutB2"));
					if (WBini.getProperty("BreakLogout") != null)
						radioButton3.setSelected(Boolean.parseBoolean(WBini
								.getProperty("BreakLogout")));
					if (WBini.getProperty("MidTime") != null)
						formattedTextField1.setText(WBini
								.getProperty("MidTime"));
					if (WBini.getProperty("RandTime") != null)
						formattedTextField3.setText(WBini
								.getProperty("RandTime"));
					if (WBini.getProperty("MidLength") != null)
						formattedTextField2.setText(WBini
								.getProperty("MidLength"));
					if (WBini.getProperty("RandLength") != null)
						formattedTextField4.setText(WBini
								.getProperty("RandLength"));
					if (WBini.getProperty("AutoStopH") != null)
						textHour.setText(WBini.getProperty("AutoStopH"));
					if (WBini.getProperty("AutoStopM") != null)
						textMinute.setText(WBini.getProperty("AutoStopM"));
					if (WBini.getProperty("AutoStopS") != null)
						textSecond.setText(WBini.getProperty("AutoStopS"));
					if (WBini.getProperty("Remote") != null)
						radioButton11.setSelected(Boolean.parseBoolean(WBini
								.getProperty("Remote")));
					if (WBini.getProperty("RemoteName") != null)
						formattedTextField11.setText(WBini
								.getProperty("RemoteName"));
					if (WBini.getProperty("RemoteText") != null)
						formattedTextField21.setText(WBini
								.getProperty("RemoteText"));
					if (WBini.getProperty("RemoteReply") != null)
						formattedTextField22.setText(WBini
								.getProperty("RemoteReply"));
					if (WBini.getProperty("Relog") != null)
						radioButton26.setSelected(Boolean.parseBoolean(WBini
								.getProperty("Relog")));
					if (WBini.getProperty("RelogTime") != null)
						formattedTextField31.setText(WBini
								.getProperty("RelogTime"));

					if (WBini.getProperty("CRuseLevelRes") != null)
						chatRes.doLevelRes = Boolean.parseBoolean(WBini
								.getProperty("CRuseLevelRes"));
					if (WBini.getProperty("CRuseCustomRes") != null)
						chatRes.doCustomRes = Boolean.parseBoolean(WBini
								.getProperty("CRuseCustomRes"));
					if (WBini.getProperty("CRtradeRes") != null)
						chatRes.tradeRes = WBini.getProperty("CRtradeRes")
								.toLowerCase().split("/");
					if (WBini.getProperty("CRgreetingRes") != null)
						chatRes.greetingRes = WBini
								.getProperty("CRgreetingRes").toLowerCase()
								.split("/");
					if (WBini.getProperty("CRbotterRes") != null)
						chatRes.botterRes = WBini.getProperty("CRbotterRes")
								.toLowerCase().split("/");
					if (WBini.getProperty("CRlevelRes") != null)
						chatRes.levelRes = WBini.getProperty("CRlevelRes")
								.toLowerCase().split("/");
					if (WBini.getProperty("CRdetection") != null)
						chatRes.customDetect = WBini.getProperty("CRdetection")
								.toLowerCase().split("/");
					if (WBini.getProperty("CRresponse") != null)
						chatRes.customRes = WBini.getProperty("CRresponse")
								.toLowerCase().split("/");
					if (WBini.getProperty("CRcustomTO") != null)
						chatRes.customTO = Integer.parseInt(WBini
								.getProperty("CRcustomTO"));
					if (WBini.getProperty("CRcustomTOR") != null)
						chatRes.customTOR = Integer.parseInt(WBini
								.getProperty("CRcustomTOR"));
				} catch (java.lang.Exception e) {
					log.warning("[GUI] Settings file is corrupt, using default settings!");
				}
			}
			return true;
		}

		private WhiteBearGUI() {
			initComponents();
		}

		private void spellTypeComboItemStateChanged(ItemEvent e) {
			int i = spellTypeCombo.getSelectedIndex();
			label36.setVisible(false);
			label35.setVisible(false);
			label34.setVisible(false);
			label33.setVisible(false);
			label32.setVisible(false);
			label31.setVisible(false);
			tfNpcID.setVisible(false);
			tfItemID.setVisible(false);
			superheatCombo.setVisible(false);
			alchemyCombo.setVisible(false);
			curseCombo.setVisible(false);
			enchantCombo.setVisible(false);
			othersCombo.setVisible(false);
			bankCombo.setVisible(false);
			teleportCombo.setVisible(false);
			boltCombo.setVisible(false);
			switch (i) {
			case 0: // Teleports
				teleportCombo.setVisible(true);
				label32.setVisible(true);
				break;
			case 1: // Alchemy
				alchemyCombo.setVisible(true);
				label32.setVisible(true);
				label31.setVisible(true);
				tfItemID.setVisible(true);
				break;
			case 2: // Curses
				curseCombo.setVisible(true);
				label33.setVisible(true);
				label34.setVisible(true);
				tfNpcID.setVisible(true);
				break;
			case 3: // Alch + Curse
				alchemyCombo.setVisible(true);
				label32.setVisible(true);
				label31.setVisible(true);
				tfItemID.setVisible(true);
				curseCombo.setVisible(true);
				label33.setVisible(true);
				label34.setVisible(true);
				tfNpcID.setVisible(true);
				break;
			case 4: // Enchants
				enchantCombo.setVisible(true);
				label32.setVisible(true);
				label31.setVisible(true);
				tfItemID.setVisible(true);
				bankCombo.setVisible(true);
				label35.setVisible(true);
				break;
			case 5: // Superheat
				superheatCombo.setVisible(true);
				label36.setVisible(true);
				bankCombo.setVisible(true);
				label35.setVisible(true);
				break;
			case 6: // Bolt Enchant
				boltCombo.setVisible(true);
				label32.setVisible(true);
				break;
			case 7: // Others
				othersCombo.setVisible(true);
				label32.setVisible(true);
				label31.setVisible(true);
				tfItemID.setVisible(true);
				bankCombo.setVisible(true);
				label35.setVisible(true);
				break;
			}
		}

		private void button1ActionPerformed(ActionEvent e) {
			if (chatResGUI) {
				log.severe("Chat Responder GUI is still active!");
			} else {
				String b = (String) bankCombo.getSelectedItem();
				if (b.contains("Bankers")) {
					bankMode = 1;
				} else if (b.contains("Bank Booths")) {
					bankMode = 2;
				} else if (b.contains("Soul Wars")) {
					bankMode = 4;
				} else {
					bankMode = 3;
				}

				String t = (String) spellTypeCombo.getSelectedItem();
				if (t.contains("Teleports")) {
					doWhat = 0;
				} else if (t.contains("Alchemy")) {
					doWhat = 1;
				} else if (t.contains("Curses")) {
					doWhat = 2;
				} else if (t.contains("Alch + Curse")) {
					doWhat = 3;
				} else if (t.contains("Enchants")) {
					doWhat = 4;
				} else if (t.contains("Superheat")) {
					doWhat = 5;
				} else if (t.contains("Bolt Enchant")) {
					doWhat = 8;
				} else if (t.contains("Others")) {
					doWhat = 99;
				}
				// *****************TELEPORTS********************************\\
				if (doWhat == 0) {
					String tempTele = (String) teleportCombo.getSelectedItem();
					if (tempTele.contains("Varrock Teleport")) {
						component = 40;
						exp = 35;
						spellName = "Varrock Teleport";
					} else if (tempTele.contains("Lumbridge Teleport")) {
						component = 43;
						exp = 41;
						spellName = "Lumbridge Teleport";
					} else if (tempTele.contains("Falador Teleport")) {
						component = 46;
						exp = 47;
						spellName = "Falador Teleport";
					} else if (tempTele.contains("Camelot Teleport")) {
						component = 51;
						exp = 55.5;
						spellName = "Camelot Teleport";
					} else if (tempTele.contains("Ardougne Teleport")) {
						component = 57;
						exp = 61;
						spellName = "Ardougne Teleport";
					} else if (tempTele.contains("Watchtower Teleport")) {
						component = 62;
						exp = 68;
						spellName = "Watchtower Teleport";
					} else if (tempTele.contains("Trollheim Teleport")) {
						component = 69;
						exp = 68;
						spellName = "Trollheim Teleport";
					} else if (tempTele.contains("Teleport to Ape Atoll")) {
						component = 72;
						exp = 74;
						spellName = "Teleport to Ape Atoll";
					} else {
						log.severe("An error occured!");
					}
					// *****************ALCHEMY********************************\\
				} else if (doWhat == 1) {
					String tempAlch = (String) alchemyCombo.getSelectedItem();
					if (tempAlch.contains("Low Level Alchemy")) {
						component = 38;
						exp = 31;
						spellName = "Low Level Alchemy";
					} else if (tempAlch.contains("High Level Alchemy")) {
						component = 59;
						exp = 65;
						spellName = "High Level Alchemy";
					} else {
						log.severe("An error occured!");
					}
					// *****************CURSES*********************************\\
				} else if (doWhat == 2) {
					String tempCurse = (String) curseCombo.getSelectedItem();
					if (tempCurse.contains("Confuse")) {
						component = 26;
						exp = 13;
						spellName = "Confuse";
					} else if (tempCurse.contains("Weaken")) {
						component = 31;
						exp = 21;
						spellName = "Weaken";
					} else if (tempCurse.contains("Curse")) {
						component = 35;
						exp = 29;
						spellName = "Curse";
					} else if (tempCurse.contains("Bind")) {
						component = 36;
						exp = 30;
						spellName = "Bind";
					} else if (tempCurse.contains("Snare")) {
						component = 55;
						exp = 60;
						spellName = "Snare";
					} else if (tempCurse.contains("Vulnerability")) {
						component = 75;
						exp = 76;
						spellName = "Vulnerability";
					} else if (tempCurse.contains("Enfeeble")) {
						component = 78;
						exp = 83;
						spellName = "Enfeeble";
					} else if (tempCurse.contains("Entangle")) {
						component = 81;
						exp = 89;
						spellName = "Entangle";
					} else if (tempCurse.contains("Stun")) {
						component = 82;
						exp = 90;
						spellName = "Stun";
					} else {
						log.severe("An error occured!");
					}
					// *****************ALCH +
					// CURSE****************************\\
				} else if (doWhat == 3) {
					String tempAlch = (String) alchemyCombo.getSelectedItem();
					if (tempAlch.contains("Low Level Alchemy")) {
						component = 38;
						exp = 31;
						spellName = "Low Level Alchemy";
					} else if (tempAlch.contains("High Level Alchemy")) {
						component = 59;
						exp = 65;
						spellName = "High Level Alchemy";
					} else {
						log.severe("An error occured!");
					}
					String tempCurse = (String) curseCombo.getSelectedItem();
					if (tempCurse.contains("Confuse")) {
						curseComponent = 26;
					} else if (tempCurse.contains("Weaken")) {
						curseComponent = 31;
					} else if (tempCurse.contains("Curse")) {
						curseComponent = 35;
					} else if (tempCurse.contains("Bind")) {
						curseComponent = 36;
					} else if (tempCurse.contains("Snare")) {
						curseComponent = 55;
					} else if (tempCurse.contains("Vulnerability")) {
						curseComponent = 75;
					} else if (tempCurse.contains("Enfeeble")) {
						curseComponent = 78;
					} else if (tempCurse.contains("Entangle")) {
						curseComponent = 81;
					} else if (tempCurse.contains("Stun")) {
						curseComponent = 82;
					} else {
						log.severe("An error occured!");
					}
					// *****************ENCHANT********************************\\
				} else if (doWhat == 4) {
					String tempAlch = (String) enchantCombo.getSelectedItem();
					if (tempAlch.contains("Level 1")) {
						component = 29;
						exp = 17.5;
						spellName = "1 Enchant";
					} else if (tempAlch.contains("Level 2")) {
						component = 41;
						exp = 37;
						spellName = "2 Enchant";
					} else if (tempAlch.contains("Level 3")) {
						component = 53;
						exp = 59;
						spellName = "3 Enchant";
					} else if (tempAlch.contains("Level 4")) {
						component = 61;
						exp = 67;
						spellName = "4 Enchant";
					} else if (tempAlch.contains("Level 5")) {
						component = 76;
						exp = 78;
						spellName = "5 Enchant";
					} else if (tempAlch.contains("Level 6")) {
						component = 88;
						exp = 97;
						spellName = "6 Enchant";
					} else {
						log.severe("An error occured!");
					}
					// *****************SUPERHEAT******************************\\
				} else if (doWhat == 5) {
					component = 50;
					exp = 53;
					spellName = "Superheat Item";
					String tempBar = (String) superheatCombo.getSelectedItem();
					if (tempBar.contains("Bronze")) {
						barID = 2349;
						ore1ID = 436;
						ore1Amt = 13;
						ore2ID = 438;
						ore2Amt = 13;
						barName = "Bronze Bar";
					} else if (tempBar.contains("Iron")) {
						barID = 2351;
						ore1ID = 440;
						ore2ID = -1;
						barName = "Iron Bar";
					} else if (tempBar.contains("Steel")) {
						barID = 2353;
						ore1ID = 440;
						ore1Amt = 9;
						ore2ID = 453;
						ore2Amt = 18;
						barName = "Steel Bar";
					} else if (tempBar.contains("Silver")) {
						barID = 2355;
						ore1ID = 442;
						ore2ID = -1;
						barName = "Silver Bar";
					} else if (tempBar.contains("Gold")) {
						barID = 2357;
						ore1ID = 444;
						ore2ID = -1;
						barName = "Gold Bar";
					} else if (tempBar.contains("Mithril")) {
						barID = 2359;
						ore1ID = 447;
						ore1Amt = 5;
						ore2ID = 453;
						ore2Amt = 20;
						barName = "Mithril Bar";
					} else if (tempBar.contains("Adamant")) {
						barID = 2361;
						ore1ID = 449;
						ore1Amt = 3;
						ore2ID = 453;
						ore2Amt = 18;
						barName = "Adamant Bar";
					} else if (tempBar.contains("Rune")) {
						barID = 2363;
						ore1ID = 451;
						ore1Amt = 3;
						ore2ID = 453;
						ore2Amt = 27;
						barName = "Rune Bar";
					} else {
						log.severe("An error occured! [Superheating]");
					}
					// *****************BOLTS**********************************\\
				} else if (doWhat == 8) {
					component = 27;
					spellName = "Enchant Crossbow Bolt";
					String tempBolt = (String) boltCombo.getSelectedItem();
					if (tempBolt.contains("Sapphire")) {
						exp = 17;
						bolteId = 9240;
						boltComponent = 29;
						itemtype = "Sapphire Bolt";
					} else if (tempBolt.contains("Emerald")) {
						exp = 37;
						bolteId = 9241;
						boltComponent = 32;
						itemtype = "Emerald Bolt";
					} else if (tempBolt.contains("Ruby")) {
						exp = 59;
						bolteId = 9242;
						boltComponent = 35;
						itemtype = "Ruby Bolt";
					} else if (tempBolt.contains("Diamond")) {
						exp = 67;
						bolteId = 9243;
						boltComponent = 38;
						itemtype = "Diamond Bolt";
					} else if (tempBolt.contains("Dragon")) {
						exp = 78;
						bolteId = 9244;
						boltComponent = 41;
						itemtype = "Dragon Bolt";
					} else if (tempBolt.contains("Onyx")) {
						exp = 97;
						bolteId = 9245;
						boltComponent = 44;
						itemtype = "Onyx Bolt";
					} else {
						log.severe("An error occured! [Enchant Crossbow Bolt]");
					}
					// *****************OTHERS*********************************\\
				} else if (doWhat == 99) {
					String tempAlch = (String) othersCombo.getSelectedItem();
					if (tempAlch.contains("Bones to Banana")) {
						doWhat = 6;
						component = 33;
						exp = 25;
						spellName = "Bones to Bananas";
						foodItem = 1963;
					} else if (tempAlch.contains("Bones to Peaches")) {
						doWhat = 6;
						component = 65;
						exp = 65;
						spellName = "Bones to Peaches";
						foodItem = 6883;
					} else if (tempAlch.contains("Telekinetic Grab")) {
						doWhat = 7;
						component = 44;
						exp = 43;
						spellName = "Telekinetic Grab";
					} else {
						log.severe("An error occured!");
					}
				}

				npcID = Integer.parseInt(tfNpcID.getText());
				itemID = Integer.parseInt(tfItemID.getText());
				colour = (String) clrSelected.getSelectedItem();
				useBreaking = radioButton1.isSelected();
				randomBreaking = radioButton2.isSelected();
				thePainter.antialias = !radioButton22.isSelected();
				thePainter.font = tfTextFont.getText();
				checkUpdates = radioButton25.isSelected();
				useFkeys = !radioButton23.isSelected();
				useChatRes = radioButton12.isSelected();
				antiban.checkFriend = check2.isSelected();
				antiban.checkExperience = check3.isSelected();
				antiban.screenLookaway = check4.isSelected();
				antiban.timeOutA1 = Integer.parseInt(jTextField6.getText()) * 1000;
				antiban.timeOutA2 = Integer.parseInt(jTextField5.getText()) * 1000;
				antiban.lengthA1 = Integer.parseInt(jTextField6.getText()) * 1000;
				antiban.lengthA2 = Integer.parseInt(jTextField5.getText()) * 1000;
				breakLogout = radioButton3.isSelected();
				minMS = Integer.parseInt(jTextField.getText());
				maxMS = Integer.parseInt(jTextField2.getText());
				if (minMS >= maxMS) {
					maxMS = minMS + 1;
				}
				midTime = Integer.parseInt(formattedTextField1.getText());
				randTime = Integer.parseInt(formattedTextField3.getText());
				midLength = Integer.parseInt(formattedTextField2.getText());
				randLength = Integer.parseInt(formattedTextField4.getText());
				if (midTime < 10) {
					midTime = 10;
				} else if (midTime >= 50001) {
					midTime = 50000;
				}
				if (randTime < 3) {
					randTime = 3;
				} else if (randTime >= 20001) {
					randTime = 20000;
				}
				if (randTime > midTime) {
					randTime = midTime - 1;
				}

				if (midLength < 2) {
					midLength = 2;
				} else if (midLength >= 35001) {
					midLength = 35000;
				}
				if (randLength < 1) {
					randLength = 1;
				} else if (randLength >= 15001) {
					randLength = 15000;
				}
				if (randLength > midLength) {
					randLength = midLength - 1;
				}
				long hour = Long.parseLong(textHour.getText());
				long minute = Long.parseLong(textMinute.getText());
				long second = Long.parseLong(textSecond.getText());
				if (hour <= 0 && minute <= 0 && second <= 0) {
					stopTime = -1;
				} else {
					long tempTime = 0;
					if (hour > 1) {
						long tempHr = tempTime;
						tempTime = tempHr + hour * 3600000;
					}
					if (minute > 1) {
						long tempMin = tempTime;
						tempTime = tempMin + minute * 60000;
					}
					if (second > 1) {
						long tempSec = tempTime;
						tempTime = tempSec + second * 1000;
					}
					stopTime = hour * 3600000 + minute * 60000 + second * 1000;
				}
				useRemote = radioButton11.isSelected();
				doRelog = radioButton26.isSelected();
				try {
					remoteName = formattedTextField11.getText().toLowerCase();
				} catch (Exception ee) {
					remoteName = "";
				}
				try {
					remoteMsg = formattedTextField21.getText().toLowerCase();
				} catch (Exception eee) {
					remoteMsg = "";
				}
				try {
					remoteReply = formattedTextField22.getText().toLowerCase();
				} catch (Exception eeep) {
					remoteMsg = "";
				}
				try {
					relogAfter = Integer.parseInt(formattedTextField31
							.getText());
				} catch (Exception eeee) {
					relogAfter = -1;
				}
				WBini.setProperty("UseSetting",
						String.valueOf(useSetting ? true : false));
				if (useSetting) {
					WBini.setProperty("NpcID", tfNpcID.getText());
					WBini.setProperty("ItemID", tfItemID.getText());

					WBini.setProperty("SpellTypeCombo",
							Integer.toString(spellTypeCombo.getSelectedIndex()));
					WBini.setProperty("SuperheatCombo",
							Integer.toString(superheatCombo.getSelectedIndex()));
					WBini.setProperty("BankCombo",
							Integer.toString(bankCombo.getSelectedIndex()));
					WBini.setProperty("AlchemyCombo",
							Integer.toString(alchemyCombo.getSelectedIndex()));
					WBini.setProperty("BoltCombo",
							Integer.toString(boltCombo.getSelectedIndex()));
					WBini.setProperty("EnchantCombo",
							Integer.toString(enchantCombo.getSelectedIndex()));
					WBini.setProperty("CurseCombo",
							Integer.toString(curseCombo.getSelectedIndex()));
					WBini.setProperty("OthersCombo",
							Integer.toString(othersCombo.getSelectedIndex()));
					WBini.setProperty("TeleportCombo",
							Integer.toString(teleportCombo.getSelectedIndex()));

					WBini.setProperty("UseChatRes", String
							.valueOf(radioButton12.isSelected() ? true : false));
					WBini.setProperty("CheckUpdate", String
							.valueOf(radioButton25.isSelected() ? true : false));
					WBini.setProperty("PaintColour",
							String.valueOf(clrSelected.getSelectedIndex()));
					WBini.setProperty("PaintFont", tfTextFont.getText());
					WBini.setProperty("Fkeys", String.valueOf(radioButton23
							.isSelected() ? true : false));
					WBini.setProperty("MinMouseSpeed", jTextField.getText());
					WBini.setProperty("MaxMouseSpeed", jTextField2.getText());
					WBini.setProperty("Antialias", String.valueOf(radioButton22
							.isSelected() ? true : false));
					WBini.setProperty("Breaking", String.valueOf(radioButton1
							.isSelected() ? true : false));
					WBini.setProperty("RandomBreak", String
							.valueOf(radioButton2.isSelected() ? true : false));
					WBini.setProperty("CheckFriend",
							String.valueOf(check2.isSelected() ? true : false));
					WBini.setProperty("CheckExperience",
							String.valueOf(check3.isSelected() ? true : false));
					WBini.setProperty("ScreenLookaway",
							String.valueOf(check4.isSelected() ? true : false));
					WBini.setProperty("TimeoutA1", jTextField3.getText());
					WBini.setProperty("TimeoutA2", jTextField4.getText());
					WBini.setProperty("LengthA1", jTextField6.getText());
					WBini.setProperty("LengthA2", jTextField5.getText());
					WBini.setProperty("BreakLogout", String
							.valueOf(radioButton3.isSelected() ? true : false));
					WBini.setProperty("MidTime", formattedTextField1.getText());
					WBini.setProperty("RandTime", formattedTextField3.getText());
					WBini.setProperty("MidLength",
							formattedTextField2.getText());
					WBini.setProperty("RandLength",
							formattedTextField4.getText());
					WBini.setProperty("AutoStopH", textHour.getText());
					WBini.setProperty("AutoStopM", textMinute.getText());
					WBini.setProperty("AutoStopS", textSecond.getText());
					WBini.setProperty("Remote", String.valueOf(radioButton11
							.isSelected() ? true : false));
					WBini.setProperty("RemoteName",
							formattedTextField11.getText());
					WBini.setProperty("RemoteText",
							formattedTextField21.getText());
					WBini.setProperty("RemoteReply",
							formattedTextField22.getText());
					WBini.setProperty("Relog", String.valueOf(radioButton26
							.isSelected() ? true : false));
					WBini.setProperty("RelogTime",
							formattedTextField31.getText());
				}
				try {
					WBini.store(new FileWriter(new File(
							GlobalConfiguration.Paths.getSettingsDirectory(),
							"WhiteBearAIOMagicV2.ini")),
							"The GUI Settings for White Bear AIO Magic (Version: "
									+ Double.toString(properties.version())
									+ ")");
					if (first)
						log("[GUI] Created a settings file!");
				} catch (IOException ioe) {
					log.warning("[GUI] Error occurred when saving GUI settings!");
				}

				guiStart = true;
				WhiteBearGUI.dispose();
			}
		}

		private void button2ActionPerformed(ActionEvent e) {
			if (!chatResGUI) {
				chatResGUI = true;
				SwingUtilities.invokeLater(new Runnable() {
					@Override
					public void run() {
						ChatResponderGUI chatGUI = new ChatResponderGUI();
						chatGUI.WhiteBearGUI.setVisible(true);
					}
				});
			}
		}

		private void radioButton2ActionPerformed(ActionEvent e) {
			randomBreaking = radioButton2.isSelected();
			if (randomBreaking == true) {
				formattedTextField1.setEnabled(false);
				formattedTextField2.setEnabled(false);
				formattedTextField3.setEnabled(false);
				formattedTextField4.setEnabled(false);
			} else {
				formattedTextField1.setEnabled(true);
				formattedTextField2.setEnabled(true);
				formattedTextField3.setEnabled(true);
				formattedTextField4.setEnabled(true);
			}
		}

		private void initComponents() {
			WhiteBearGUI = new JFrame();
			panel1 = new JPanel();
			tabbedPane1 = new JTabbedPane();
			panel6 = new JPanel();
			radioButton12 = new JCheckBox();
			teleportCombo = new JComboBox();
			radioButton25 = new JCheckBox();
			button2 = new JButton();
			label7 = new JLabel();
			spellTypeCombo = new JComboBox();
			label31 = new JLabel();
			tfItemID = new JFormattedTextField();
			alchemyCombo = new JComboBox();
			boltCombo = new JComboBox();
			label32 = new JLabel();
			label33 = new JLabel();
			curseCombo = new JComboBox();
			label34 = new JLabel();
			tfNpcID = new JFormattedTextField();
			enchantCombo = new JComboBox();
			othersCombo = new JComboBox();
			label35 = new JLabel();
			bankCombo = new JComboBox();
			label36 = new JLabel();
			superheatCombo = new JComboBox();
			panel2 = new JPanel();
			jTextField = new JFormattedTextField();
			radioButton22 = new JCheckBox();
			label4 = new JLabel();
			clrSelected = new JComboBox();
			label5 = new JLabel();
			label6 = new JLabel();
			label16 = new JLabel();
			textSecond = new JFormattedTextField();
			label15 = new JLabel();
			textMinute = new JFormattedTextField();
			label14 = new JLabel();
			textHour = new JFormattedTextField();
			label13 = new JLabel();
			jTextField2 = new JFormattedTextField();
			label21 = new JLabel();
			radioButton23 = new JCheckBox();
			label30 = new JLabel();
			tfTextFont = new JFormattedTextField();
			panel3 = new JPanel();
			radioButton1 = new JCheckBox();
			radioButton2 = new JCheckBox();
			label8 = new JLabel();
			formattedTextField1 = new JFormattedTextField();
			label9 = new JLabel();
			label10 = new JLabel();
			formattedTextField3 = new JFormattedTextField();
			label11 = new JLabel();
			formattedTextField2 = new JFormattedTextField();
			label12 = new JLabel();
			formattedTextField4 = new JFormattedTextField();
			radioButton3 = new JCheckBox();
			panel5 = new JPanel();
			check3 = new JCheckBox();
			check2 = new JCheckBox();
			check4 = new JCheckBox();
			label22 = new JLabel();
			jTextField3 = new JFormattedTextField();
			label23 = new JLabel();
			jTextField4 = new JFormattedTextField();
			label24 = new JLabel();
			label25 = new JLabel();
			label26 = new JLabel();
			label27 = new JLabel();
			label28 = new JLabel();
			jTextField5 = new JFormattedTextField();
			label29 = new JLabel();
			jTextField6 = new JFormattedTextField();
			panel4 = new JPanel();
			textArea3 = new JTextArea();
			radioButton11 = new JCheckBox();
			label17 = new JLabel();
			formattedTextField11 = new JFormattedTextField();
			label18 = new JLabel();
			formattedTextField21 = new JFormattedTextField();
			radioButton26 = new JCheckBox();
			formattedTextField31 = new JFormattedTextField();
			label19 = new JLabel();
			label20 = new JLabel();
			formattedTextField22 = new JFormattedTextField();
			button1 = new JButton();
			label1 = new JLabel();
			label2 = new JLabel();

			// ======== WhiteBearGUI ========
			{
				WhiteBearGUI.setAlwaysOnTop(true);
				WhiteBearGUI.setBackground(Color.black);
				WhiteBearGUI.setResizable(false);
				WhiteBearGUI.setMinimumSize(new Dimension(405, 405));
				WhiteBearGUI.setTitle("White Bear AIO Magic");
				WhiteBearGUI
						.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
				WhiteBearGUI
						.setFont(new Font("Century Gothic", Font.PLAIN, 12));
				Container WhiteBearGUIContentPane = WhiteBearGUI
						.getContentPane();
				WhiteBearGUIContentPane.setLayout(null);

				// ======== panel1 ========
				{
					panel1.setBackground(Color.black);
					panel1.setForeground(Color.green);
					panel1.setMinimumSize(new Dimension(100, 200));
					panel1.setLayout(null);

					// ======== tabbedPane1 ========
					{
						tabbedPane1.setFont(new Font("Century Gothic",
								Font.PLAIN, 12));
						tabbedPane1.setForeground(new Color(0, 153, 0));

						// ======== panel6 ========
						{
							panel6.setBackground(Color.black);
							panel6.setLayout(null);

							// ---- radioButton12 ----
							radioButton12.setText("Use Chat Responder");
							radioButton12.setBackground(Color.black);
							radioButton12.setForeground(Color.yellow);
							radioButton12.setFont(new Font("Century Gothic",
									Font.BOLD, 13));
							radioButton12.setSelected(true);
							panel6.add(radioButton12);
							radioButton12.setBounds(new Rectangle(new Point(17,
									155), radioButton12.getPreferredSize()));

							// ---- teleportCombo ----
							teleportCombo.setBackground(Color.black);
							teleportCombo.setForeground(new Color(51, 51, 51));
							teleportCombo.setBorder(null);
							teleportCombo.setFont(new Font("Century Gothic",
									Font.BOLD, 12));
							teleportCombo.setModel(new DefaultComboBoxModel(
									new String[] { "Varrock Teleport",
											"Lumbridge Teleport",
											"Falador Teleport",
											"Camelot Teleport",
											"Ardougne Teleport",
											"Watchtower Teleport",
											"Trollheim Teleport",
											"Teleport to Ape Atoll" }));
							teleportCombo.setSelectedIndex(0);
							panel6.add(teleportCombo);
							teleportCombo.setBounds(75, 64, 155, 25);

							// ---- radioButton25 ----
							radioButton25.setText("Check for Updates");
							radioButton25.setBackground(Color.black);
							radioButton25.setForeground(Color.yellow);
							radioButton25.setFont(new Font("Century Gothic",
									Font.BOLD, 13));
							radioButton25.setSelected(true);
							panel6.add(radioButton25);
							radioButton25.setBounds(17, 191, 150, 25);

							// ---- button2 ----
							button2.setText("Customize");
							button2.setBackground(Color.black);
							button2.setFont(new Font("Century Gothic",
									Font.BOLD, 12));
							button2.setForeground(new Color(0, 102, 51));
							button2.addActionListener(new ActionListener() {
								@Override
								public void actionPerformed(ActionEvent e) {
									button2ActionPerformed(e);
								}
							});
							panel6.add(button2);
							button2.setBounds(183, 157, 95, 23);

							// ---- label7 ----
							label7.setText("Spell Type:");
							label7.setBackground(new Color(51, 51, 51));
							label7.setForeground(new Color(255, 204, 0));
							label7.setFont(new Font("Century Gothic",
									Font.BOLD, 14));
							panel6.add(label7);
							label7.setBounds(20, 20, 85, 20);

							// ---- spellTypeCombo ----
							spellTypeCombo.setBackground(Color.black);
							spellTypeCombo.setForeground(new Color(51, 51, 51));
							spellTypeCombo.setBorder(null);
							spellTypeCombo.setFont(new Font("Century Gothic",
									Font.BOLD, 12));
							spellTypeCombo.setModel(new DefaultComboBoxModel(
									new String[] { "Teleports", "Alchemy",
											"Curses", "Alch + Curse",
											"Enchants", "Superheat",
											"Bolt Enchant", "Others" }));
							spellTypeCombo.addItemListener(new ItemListener() {
								@Override
								public void itemStateChanged(ItemEvent e) {
									spellTypeComboItemStateChanged(e);
								}
							});
							panel6.add(spellTypeCombo);
							spellTypeCombo.setBounds(110, 20, 145, 25);
							spellTypeCombo.setSelectedIndex(1);
							spellTypeCombo.setSelectedIndex(0);

							// ---- label31 ----
							label31.setText("Item ID:");
							label31.setBackground(new Color(51, 51, 51));
							label31.setForeground(new Color(255, 255, 102));
							label31.setFont(new Font("Century Gothic",
									Font.BOLD, 14));
							panel6.add(label31);
							label31.setBounds(240, 65, 60, 20);

							// ---- tfItemID ----
							tfItemID.setFont(new Font("Century Gothic",
									Font.PLAIN, 12));
							tfItemID.setForeground(new Color(255, 255, 204));
							tfItemID.setBackground(Color.gray);
							tfItemID.setText("0");
							panel6.add(tfItemID);
							tfItemID.setBounds(300, 65, 50, 23);

							// ---- alchemyCombo ----
							alchemyCombo.setBackground(Color.black);
							alchemyCombo.setForeground(new Color(51, 51, 51));
							alchemyCombo.setBorder(null);
							alchemyCombo.setFont(new Font("Century Gothic",
									Font.BOLD, 12));
							alchemyCombo.setModel(new DefaultComboBoxModel(
									new String[] { "Low Level Alchemy",
											"High Level Alchemy" }));
							alchemyCombo.setSelectedIndex(0);
							panel6.add(alchemyCombo);
							alchemyCombo.setBounds(75, 64, 155, 25);

							// ---- boltCombo ----
							boltCombo.setBackground(Color.black);
							boltCombo.setForeground(new Color(51, 51, 51));
							boltCombo.setBorder(null);
							boltCombo.setFont(new Font("Century Gothic",
									Font.BOLD, 12));
							boltCombo
									.setModel(new DefaultComboBoxModel(
											new String[] { "Sapphire",
													"Emerald", "Ruby",
													"Diamond", "Dragon", "Onyx" }));
							boltCombo.setSelectedIndex(0);
							panel6.add(boltCombo);
							boltCombo.setBounds(75, 64, 155, 25);

							// ---- label32 ----
							label32.setText("Spell:");
							label32.setBackground(new Color(51, 51, 51));
							label32.setForeground(new Color(255, 255, 102));
							label32.setFont(new Font("Century Gothic",
									Font.BOLD, 14));
							panel6.add(label32);
							label32.setBounds(20, 65, 50, 20);

							// ---- label33 ----
							label33.setText("Spell:");
							label33.setBackground(new Color(51, 51, 51));
							label33.setForeground(new Color(255, 255, 102));
							label33.setFont(new Font("Century Gothic",
									Font.BOLD, 14));
							panel6.add(label33);
							label33.setBounds(20, 107, 50, 20);

							// ---- curseCombo ----
							curseCombo.setBackground(Color.black);
							curseCombo.setForeground(new Color(51, 51, 51));
							curseCombo.setBorder(null);
							curseCombo.setFont(new Font("Century Gothic",
									Font.BOLD, 12));
							curseCombo.setModel(new DefaultComboBoxModel(
									new String[] { "Confuse", "Weaken",
											"Curse", "Bind", "Snare",
											"Vulnerability", "Enfeeble",
											"Entangle", "Stun" }));
							curseCombo.setSelectedIndex(0);
							panel6.add(curseCombo);
							curseCombo.setBounds(75, 106, 155, 25);

							// ---- label34 ----
							label34.setText("NPC ID:");
							label34.setBackground(new Color(51, 51, 51));
							label34.setForeground(new Color(255, 255, 102));
							label34.setFont(new Font("Century Gothic",
									Font.BOLD, 14));
							panel6.add(label34);
							label34.setBounds(240, 107, 60, 20);

							// ---- tfNpcID ----
							tfNpcID.setFont(new Font("Century Gothic",
									Font.PLAIN, 12));
							tfNpcID.setForeground(new Color(255, 255, 204));
							tfNpcID.setBackground(Color.gray);
							tfNpcID.setText("0");
							panel6.add(tfNpcID);
							tfNpcID.setBounds(300, 107, 50, 23);

							// ---- enchantCombo ----
							enchantCombo.setBackground(Color.black);
							enchantCombo.setForeground(new Color(51, 51, 51));
							enchantCombo.setBorder(null);
							enchantCombo.setFont(new Font("Century Gothic",
									Font.BOLD, 12));
							enchantCombo.setModel(new DefaultComboBoxModel(
									new String[] { "Enchant Level 1",
											"Enchant Level 2",
											"Enchant Level 3",
											"Enchant Level 4",
											"Enchant Level 5",
											"Enchant Level 6" }));
							enchantCombo.setSelectedIndex(0);
							panel6.add(enchantCombo);
							enchantCombo.setBounds(75, 65, 155, 25);

							// ---- othersCombo ----
							othersCombo.setBackground(Color.black);
							othersCombo.setForeground(new Color(51, 51, 51));
							othersCombo.setBorder(null);
							othersCombo.setFont(new Font("Century Gothic",
									Font.BOLD, 12));
							othersCombo.setModel(new DefaultComboBoxModel(
									new String[] { "Bones to Banana",
											"Bones to Peaches",
											"Telekinetic Grab" }));
							othersCombo.setSelectedIndex(0);
							panel6.add(othersCombo);
							othersCombo.setBounds(75, 65, 155, 25);

							// ---- label35 ----
							label35.setText("Bank using:");
							label35.setBackground(new Color(51, 51, 51));
							label35.setForeground(new Color(255, 255, 102));
							label35.setFont(new Font("Century Gothic",
									Font.BOLD, 14));
							panel6.add(label35);
							label35.setBounds(20, 107, 80, 20);

							// ---- bankCombo ----
							bankCombo.setBackground(Color.black);
							bankCombo.setForeground(new Color(51, 51, 51));
							bankCombo.setBorder(null);
							bankCombo.setFont(new Font("Century Gothic",
									Font.BOLD, 12));
							bankCombo.setModel(new DefaultComboBoxModel(
									new String[] { "Bankers", "Bank Booth",
											"Chests", "Chest (Soul Wars)" }));
							bankCombo.setSelectedIndex(0);
							panel6.add(bankCombo);
							bankCombo.setBounds(105, 107, 125, 25);

							// ---- label36 ----
							label36.setText("Superheat:");
							label36.setBackground(new Color(51, 51, 51));
							label36.setForeground(new Color(255, 255, 102));
							label36.setFont(new Font("Century Gothic",
									Font.BOLD, 14));
							panel6.add(label36);
							label36.setBounds(20, 65, 75, 20);

							// ---- superheatCombo ----
							superheatCombo.setBackground(Color.black);
							superheatCombo.setForeground(new Color(51, 51, 51));
							superheatCombo.setBorder(null);
							superheatCombo.setFont(new Font("Century Gothic",
									Font.BOLD, 12));
							superheatCombo.setModel(new DefaultComboBoxModel(
									new String[] { "Bronze", "Iron", "Steel",
											"Silver", "Gold", "Mithril",
											"Adamant", "Rune" }));
							superheatCombo.setSelectedIndex(0);
							panel6.add(superheatCombo);
							superheatCombo.setBounds(100, 64, 95, 25);

							{ // compute preferred size
								Dimension preferredSize = new Dimension();
								for (int i = 0; i < panel6.getComponentCount(); i++) {
									Rectangle bounds = panel6.getComponent(i)
											.getBounds();
									preferredSize.width = Math
											.max(bounds.x + bounds.width,
													preferredSize.width);
									preferredSize.height = Math.max(bounds.y
											+ bounds.height,
											preferredSize.height);
								}
								Insets insets = panel6.getInsets();
								preferredSize.width += insets.right;
								preferredSize.height += insets.bottom;
								panel6.setMinimumSize(preferredSize);
								panel6.setPreferredSize(preferredSize);
							}
						}
						tabbedPane1.addTab("Info", panel6);

						// ======== panel2 ========
						{
							panel2.setBackground(Color.black);
							panel2.setLayout(null);

							// ---- jTextField ----
							jTextField.setFont(new Font("Century Gothic",
									Font.PLAIN, 12));
							jTextField.setForeground(new Color(255, 255, 204));
							jTextField.setBackground(Color.gray);
							jTextField.setText("4");
							panel2.add(jTextField);
							jTextField.setBounds(120, 93, 35, 23);

							// ---- radioButton22 ----
							radioButton22.setText("Disable Paint Antialias");
							radioButton22.setBackground(Color.black);
							radioButton22.setForeground(Color.yellow);
							radioButton22.setFont(new Font("Century Gothic",
									Font.BOLD, 13));
							panel2.add(radioButton22);
							radioButton22.setBounds(17, 130, 175, 25);

							// ---- label4 ----
							label4.setText("Paint Colour");
							label4.setBackground(new Color(51, 51, 51));
							label4.setForeground(new Color(255, 255, 102));
							label4.setFont(new Font("Century Gothic",
									Font.BOLD, 14));
							panel2.add(label4);
							label4.setBounds(20, 17, 90, 20);

							// ---- clrSelected ----
							clrSelected.setBackground(Color.black);
							clrSelected.setForeground(new Color(51, 51, 51));
							clrSelected.setBorder(null);
							clrSelected.setFont(new Font("Century Gothic",
									Font.BOLD, 12));
							clrSelected.setModel(new DefaultComboBoxModel(
									new String[] { "Black", "Blue", "Green",
											"Red", "Purple", "Brown" }));
							clrSelected.setSelectedIndex(0);
							panel2.add(clrSelected);
							clrSelected.setBounds(118, 16, 110, 25);

							// ---- label5 ----
							label5.setText("Mouse Speed");
							label5.setBackground(new Color(51, 51, 51));
							label5.setForeground(new Color(255, 255, 102));
							label5.setFont(new Font("Century Gothic",
									Font.BOLD, 14));
							panel2.add(label5);
							label5.setBounds(20, 93, 100, 20);

							// ---- label6 ----
							label6.setText("(higher = slower)");
							label6.setBackground(new Color(51, 51, 51));
							label6.setForeground(new Color(255, 255, 102));
							label6.setFont(new Font("Century Gothic",
									Font.BOLD, 12));
							panel2.add(label6);
							label6.setBounds(225, 93, 105, 20);

							// ---- label16 ----
							label16.setText("(hr:min:sec)");
							label16.setBackground(new Color(51, 51, 51));
							label16.setForeground(new Color(255, 255, 102));
							label16.setFont(new Font("Century Gothic",
									Font.BOLD, 12));
							panel2.add(label16);
							label16.setBounds(220, 206, 80, 20);

							// ---- textSecond ----
							textSecond.setFont(new Font("Century Gothic",
									Font.PLAIN, 12));
							textSecond.setForeground(new Color(255, 255, 204));
							textSecond.setBackground(Color.gray);
							textSecond.setText("0");
							panel2.add(textSecond);
							textSecond.setBounds(185, 206, 30, 23);

							// ---- label15 ----
							label15.setText(":");
							label15.setBackground(new Color(51, 51, 51));
							label15.setForeground(new Color(255, 255, 102));
							label15.setFont(new Font("Century Gothic",
									Font.BOLD, 12));
							panel2.add(label15);
							label15.setBounds(175, 206, 10, 20);

							// ---- textMinute ----
							textMinute.setFont(new Font("Century Gothic",
									Font.PLAIN, 12));
							textMinute.setForeground(new Color(255, 255, 204));
							textMinute.setBackground(Color.gray);
							textMinute.setText("0");
							panel2.add(textMinute);
							textMinute.setBounds(140, 206, 30, 23);

							// ---- label14 ----
							label14.setText(":");
							label14.setBackground(new Color(51, 51, 51));
							label14.setForeground(new Color(255, 255, 102));
							label14.setFont(new Font("Century Gothic",
									Font.BOLD, 12));
							panel2.add(label14);
							label14.setBounds(135, 206, 10, 20);

							// ---- textHour ----
							textHour.setFont(new Font("Century Gothic",
									Font.PLAIN, 12));
							textHour.setForeground(new Color(255, 255, 204));
							textHour.setBackground(Color.gray);
							textHour.setText("0");
							panel2.add(textHour);
							textHour.setBounds(100, 206, 30, 23);

							// ---- label13 ----
							label13.setText("Auto Stop:");
							label13.setBackground(new Color(51, 51, 51));
							label13.setForeground(new Color(255, 255, 102));
							label13.setFont(new Font("Century Gothic",
									Font.BOLD, 14));
							panel2.add(label13);
							label13.setBounds(20, 206, 75, 20);

							// ---- jTextField2 ----
							jTextField2.setFont(new Font("Century Gothic",
									Font.PLAIN, 12));
							jTextField2.setForeground(new Color(255, 255, 204));
							jTextField2.setBackground(Color.gray);
							jTextField2.setText("6");
							panel2.add(jTextField2);
							jTextField2.setBounds(180, 93, 35, 23);

							// ---- label21 ----
							label21.setText("to");
							label21.setBackground(new Color(51, 51, 51));
							label21.setForeground(new Color(255, 255, 102));
							label21.setFont(new Font("Century Gothic",
									Font.BOLD, 12));
							panel2.add(label21);
							label21.setBounds(162, 93, 15, 20);

							// ---- radioButton23 ----
							radioButton23.setText("Disable F-keys");
							radioButton23.setBackground(Color.black);
							radioButton23.setForeground(Color.yellow);
							radioButton23.setFont(new Font("Century Gothic",
									Font.BOLD, 13));
							panel2.add(radioButton23);
							radioButton23.setBounds(17, 167, 133, 25);

							// ---- label30 ----
							label30.setText("Paint Font");
							label30.setBackground(new Color(51, 51, 51));
							label30.setForeground(new Color(255, 255, 102));
							label30.setFont(new Font("Century Gothic",
									Font.BOLD, 14));
							panel2.add(label30);
							label30.setBounds(20, 56, 90, 20);

							// ---- tfTextFont ----
							tfTextFont.setFont(new Font("Century Gothic",
									Font.PLAIN, 12));
							tfTextFont.setForeground(new Color(255, 255, 204));
							tfTextFont.setBackground(Color.gray);
							tfTextFont.setText("sansserif");
							panel2.add(tfTextFont);
							tfTextFont.setBounds(119, 56, 108, 23);

							{ // compute preferred size
								Dimension preferredSize = new Dimension();
								for (int i = 0; i < panel2.getComponentCount(); i++) {
									Rectangle bounds = panel2.getComponent(i)
											.getBounds();
									preferredSize.width = Math
											.max(bounds.x + bounds.width,
													preferredSize.width);
									preferredSize.height = Math.max(bounds.y
											+ bounds.height,
											preferredSize.height);
								}
								Insets insets = panel2.getInsets();
								preferredSize.width += insets.right;
								preferredSize.height += insets.bottom;
								panel2.setMinimumSize(preferredSize);
								panel2.setPreferredSize(preferredSize);
							}
						}
						tabbedPane1.addTab("Option", panel2);

						// ======== panel3 ========
						{
							panel3.setBackground(Color.black);
							panel3.setLayout(null);

							// ---- radioButton1 ----
							radioButton1.setText("Use Breaking");
							radioButton1.setBackground(Color.black);
							radioButton1.setForeground(Color.yellow);
							radioButton1.setFont(new Font("Century Gothic",
									Font.BOLD, 13));
							panel3.add(radioButton1);
							radioButton1.setBounds(17, 20, 120, 25);

							// ---- radioButton2 ----
							radioButton2.setText("Completely Random");
							radioButton2.setBackground(Color.black);
							radioButton2.setForeground(Color.yellow);
							radioButton2.setFont(new Font("Century Gothic",
									Font.BOLD, 13));
							radioButton2
									.addActionListener(new ActionListener() {
										@Override
										public void actionPerformed(
												ActionEvent e) {
											radioButton2ActionPerformed(e);
										}
									});
							panel3.add(radioButton2);
							radioButton2.setBounds(175, 20, 170, 25);

							// ---- label8 ----
							label8.setText("Time between breaks:");
							label8.setBackground(new Color(51, 51, 51));
							label8.setForeground(new Color(255, 255, 102));
							label8.setFont(new Font("Century Gothic",
									Font.BOLD, 12));
							panel3.add(label8);
							label8.setBounds(20, 65, 140, 20);

							// ---- formattedTextField1 ----
							formattedTextField1.setFont(new Font(
									"Century Gothic", Font.PLAIN, 12));
							formattedTextField1.setForeground(new Color(255,
									255, 204));
							formattedTextField1.setBackground(Color.gray);
							formattedTextField1.setText("90");
							panel3.add(formattedTextField1);
							formattedTextField1.setBounds(160, 65, 45, 23);

							// ---- label9 ----
							label9.setText("\u00b1");
							label9.setBackground(new Color(51, 51, 51));
							label9.setForeground(new Color(255, 255, 102));
							label9.setFont(new Font("Century Gothic",
									Font.BOLD, 12));
							panel3.add(label9);
							label9.setBounds(225, 65, 15, 20);

							// ---- label10 ----
							label10.setText("(time unit: minutes)");
							label10.setBackground(new Color(51, 51, 51));
							label10.setForeground(new Color(255, 255, 102));
							label10.setFont(new Font("Century Gothic",
									Font.BOLD, 12));
							panel3.add(label10);
							label10.setBounds(240, 135, 110, 20);

							// ---- formattedTextField3 ----
							formattedTextField3.setFont(new Font(
									"Century Gothic", Font.PLAIN, 12));
							formattedTextField3.setForeground(new Color(255,
									255, 204));
							formattedTextField3.setBackground(Color.gray);
							formattedTextField3.setText("90");
							panel3.add(formattedTextField3);
							formattedTextField3.setBounds(240, 65, 45, 23);

							// ---- label11 ----
							label11.setText("Length of breaks:");
							label11.setBackground(new Color(51, 51, 51));
							label11.setForeground(new Color(255, 255, 102));
							label11.setFont(new Font("Century Gothic",
									Font.BOLD, 12));
							panel3.add(label11);
							label11.setBounds(20, 110, 110, 20);

							// ---- formattedTextField2 ----
							formattedTextField2.setFont(new Font(
									"Century Gothic", Font.PLAIN, 12));
							formattedTextField2.setForeground(new Color(255,
									255, 204));
							formattedTextField2.setBackground(Color.gray);
							formattedTextField2.setText("8");
							panel3.add(formattedTextField2);
							formattedTextField2.setBounds(160, 110, 45, 23);

							// ---- label12 ----
							label12.setText("\u00b1");
							label12.setBackground(new Color(51, 51, 51));
							label12.setForeground(new Color(255, 255, 102));
							label12.setFont(new Font("Century Gothic",
									Font.BOLD, 12));
							panel3.add(label12);
							label12.setBounds(225, 110, 15, 20);

							// ---- formattedTextField4 ----
							formattedTextField4.setFont(new Font(
									"Century Gothic", Font.PLAIN, 12));
							formattedTextField4.setForeground(new Color(255,
									255, 204));
							formattedTextField4.setBackground(Color.gray);
							formattedTextField4.setText("2");
							panel3.add(formattedTextField4);
							formattedTextField4.setBounds(240, 110, 45, 23);

							// ---- radioButton3 ----
							radioButton3.setText("Logout before break starts");
							radioButton3.setBackground(Color.black);
							radioButton3.setForeground(Color.yellow);
							radioButton3.setFont(new Font("Century Gothic",
									Font.BOLD, 13));
							panel3.add(radioButton3);
							radioButton3.setBounds(17, 170, 208, 25);

							{ // compute preferred size
								Dimension preferredSize = new Dimension();
								for (int i = 0; i < panel3.getComponentCount(); i++) {
									Rectangle bounds = panel3.getComponent(i)
											.getBounds();
									preferredSize.width = Math
											.max(bounds.x + bounds.width,
													preferredSize.width);
									preferredSize.height = Math.max(bounds.y
											+ bounds.height,
											preferredSize.height);
								}
								Insets insets = panel3.getInsets();
								preferredSize.width += insets.right;
								preferredSize.height += insets.bottom;
								panel3.setMinimumSize(preferredSize);
								panel3.setPreferredSize(preferredSize);
							}
						}
						tabbedPane1.addTab("Breaking", panel3);

						// ======== panel5 ========
						{
							panel5.setBackground(Color.black);
							panel5.setLayout(null);

							// ---- check3 ----
							check3.setText("Check magic exp");
							check3.setBackground(Color.black);
							check3.setForeground(Color.yellow);
							check3.setFont(new Font("Century Gothic",
									Font.BOLD, 13));
							check3.setSelected(true);
							panel5.add(check3);
							check3.setBounds(17, 15, 145, 25);

							// ---- check2 ----
							check2.setText("Check friends");
							check2.setBackground(Color.black);
							check2.setForeground(Color.yellow);
							check2.setFont(new Font("Century Gothic",
									Font.BOLD, 13));
							panel5.add(check2);
							check2.setBounds(175, 15, 145, 25);

							// ---- check4 ----
							check4.setText("Take short breaks between casts");
							check4.setBackground(Color.black);
							check4.setForeground(Color.yellow);
							check4.setFont(new Font("Century Gothic",
									Font.BOLD, 13));
							panel5.add(check4);
							check4.setBounds(17, 66, 338, 25);

							// ---- label22 ----
							label22.setText("Timeout");
							label22.setBackground(new Color(51, 51, 51));
							label22.setForeground(new Color(255, 255, 102));
							label22.setFont(new Font("Century Gothic",
									Font.BOLD, 14));
							panel5.add(label22);
							label22.setBounds(20, 101, 65, 20);

							// ---- jTextField3 ----
							jTextField3.setFont(new Font("Century Gothic",
									Font.PLAIN, 12));
							jTextField3.setForeground(new Color(255, 255, 204));
							jTextField3.setBackground(Color.gray);
							jTextField3.setText("30");
							panel5.add(jTextField3);
							jTextField3.setBounds(119, 101, 40, 23);

							// ---- label23 ----
							label23.setText("Max:");
							label23.setBackground(new Color(51, 51, 51));
							label23.setForeground(new Color(255, 255, 102));
							label23.setFont(new Font("Century Gothic",
									Font.BOLD, 12));
							panel5.add(label23);
							label23.setBounds(167, 101, 38, 20);

							// ---- jTextField4 ----
							jTextField4.setFont(new Font("Century Gothic",
									Font.PLAIN, 12));
							jTextField4.setForeground(new Color(255, 255, 204));
							jTextField4.setBackground(Color.gray);
							jTextField4.setText("80");
							panel5.add(jTextField4);
							jTextField4.setBounds(203, 101, 40, 23);

							// ---- label24 ----
							label24.setText("(in seconds)");
							label24.setBackground(new Color(51, 51, 51));
							label24.setForeground(new Color(255, 255, 102));
							label24.setFont(new Font("Century Gothic",
									Font.BOLD, 12));
							panel5.add(label24);
							label24.setBounds(251, 101, 83, 20);

							// ---- label25 ----
							label25.setText("Min:");
							label25.setBackground(new Color(51, 51, 51));
							label25.setForeground(new Color(255, 255, 102));
							label25.setFont(new Font("Century Gothic",
									Font.BOLD, 12));
							panel5.add(label25);
							label25.setBounds(86, 101, 36, 20);

							// ---- label26 ----
							label26.setText("Length");
							label26.setBackground(new Color(51, 51, 51));
							label26.setForeground(new Color(255, 255, 102));
							label26.setFont(new Font("Century Gothic",
									Font.BOLD, 14));
							panel5.add(label26);
							label26.setBounds(20, 136, 65, 20);

							// ---- label27 ----
							label27.setText("Min:");
							label27.setBackground(new Color(51, 51, 51));
							label27.setForeground(new Color(255, 255, 102));
							label27.setFont(new Font("Century Gothic",
									Font.BOLD, 12));
							panel5.add(label27);
							label27.setBounds(90, 136, 36, 20);

							// ---- label28 ----
							label28.setText("(in seconds)");
							label28.setBackground(new Color(51, 51, 51));
							label28.setForeground(new Color(255, 255, 102));
							label28.setFont(new Font("Century Gothic",
									Font.BOLD, 12));
							panel5.add(label28);
							label28.setBounds(255, 136, 83, 20);

							// ---- jTextField5 ----
							jTextField5.setFont(new Font("Century Gothic",
									Font.PLAIN, 12));
							jTextField5.setForeground(new Color(255, 255, 204));
							jTextField5.setBackground(Color.gray);
							jTextField5.setText("3");
							panel5.add(jTextField5);
							jTextField5.setBounds(205, 136, 40, 23);

							// ---- label29 ----
							label29.setText("Max:");
							label29.setBackground(new Color(51, 51, 51));
							label29.setForeground(new Color(255, 255, 102));
							label29.setFont(new Font("Century Gothic",
									Font.BOLD, 12));
							panel5.add(label29);
							label29.setBounds(170, 136, 38, 20);

							// ---- jTextField6 ----
							jTextField6.setFont(new Font("Century Gothic",
									Font.PLAIN, 12));
							jTextField6.setForeground(new Color(255, 255, 204));
							jTextField6.setBackground(Color.gray);
							jTextField6.setText("1");
							panel5.add(jTextField6);
							jTextField6.setBounds(120, 136, 40, 23);

							{ // compute preferred size
								Dimension preferredSize = new Dimension();
								for (int i = 0; i < panel5.getComponentCount(); i++) {
									Rectangle bounds = panel5.getComponent(i)
											.getBounds();
									preferredSize.width = Math
											.max(bounds.x + bounds.width,
													preferredSize.width);
									preferredSize.height = Math.max(bounds.y
											+ bounds.height,
											preferredSize.height);
								}
								Insets insets = panel5.getInsets();
								preferredSize.width += insets.right;
								preferredSize.height += insets.bottom;
								panel5.setMinimumSize(preferredSize);
								panel5.setPreferredSize(preferredSize);
							}
						}
						tabbedPane1.addTab("Antiban", panel5);

						// ======== panel4 ========
						{
							panel4.setBackground(Color.black);
							panel4.setLayout(null);

							// ---- textArea3 ----
							textArea3
									.setText("Allows remote stopping of script. When the character says the pass to you in clan chat, you will be logged out, thus \nallowing you to login to your account.");
							textArea3.setLineWrap(true);
							textArea3.setFont(new Font("Century Gothic",
									Font.PLAIN, 12));
							textArea3.setTabSize(0);
							textArea3.setBackground(Color.gray);
							textArea3.setForeground(new Color(255, 255, 104));
							textArea3.setEditable(false);
							textArea3.setBorder(null);
							panel4.add(textArea3);
							textArea3.setBounds(5, 5, 355, 52);

							// ---- radioButton11 ----
							radioButton11.setText("Enable Remote Control");
							radioButton11.setBackground(Color.black);
							radioButton11.setForeground(Color.yellow);
							radioButton11.setFont(new Font("Century Gothic",
									Font.BOLD, 13));
							panel4.add(radioButton11);
							radioButton11.setBounds(17, 65, 180, 25);

							// ---- label17 ----
							label17.setText("Character name");
							label17.setBackground(new Color(51, 51, 51));
							label17.setForeground(new Color(255, 255, 102));
							label17.setFont(new Font("Century Gothic",
									Font.BOLD, 12));
							panel4.add(label17);
							label17.setBounds(20, 100, 110, 20);

							// ---- formattedTextField11 ----
							formattedTextField11.setFont(new Font(
									"Century Gothic", Font.PLAIN, 12));
							formattedTextField11.setForeground(new Color(255,
									255, 204));
							formattedTextField11.setBackground(Color.gray);
							formattedTextField11.setText("Zezima");
							panel4.add(formattedTextField11);
							formattedTextField11.setBounds(135, 100, 120, 23);

							// ---- label18 ----
							label18.setText("Pass");
							label18.setBackground(new Color(51, 51, 51));
							label18.setForeground(new Color(255, 255, 102));
							label18.setFont(new Font("Century Gothic",
									Font.BOLD, 12));
							panel4.add(label18);
							label18.setBounds(20, 135, 35, 20);

							// ---- formattedTextField21 ----
							formattedTextField21.setFont(new Font(
									"Century Gothic", Font.PLAIN, 12));
							formattedTextField21.setForeground(new Color(255,
									255, 204));
							formattedTextField21.setBackground(Color.gray);
							formattedTextField21.setText("Lets go shopping");
							panel4.add(formattedTextField21);
							formattedTextField21.setBounds(65, 135, 190, 23);

							// ---- radioButton26 ----
							radioButton26.setText("Relog after");
							radioButton26.setBackground(Color.black);
							radioButton26.setForeground(Color.yellow);
							radioButton26.setFont(new Font("Century Gothic",
									Font.BOLD, 13));
							panel4.add(radioButton26);
							radioButton26.setBounds(17, 208, 103, 25);

							// ---- formattedTextField31 ----
							formattedTextField31.setFont(new Font(
									"Century Gothic", Font.PLAIN, 12));
							formattedTextField31.setForeground(new Color(255,
									255, 204));
							formattedTextField31.setBackground(Color.gray);
							formattedTextField31.setText("15");
							panel4.add(formattedTextField31);
							formattedTextField31.setBounds(125, 208, 45, 23);

							// ---- label19 ----
							label19.setText("minutes");
							label19.setBackground(new Color(51, 51, 51));
							label19.setForeground(new Color(255, 255, 102));
							label19.setFont(new Font("Century Gothic",
									Font.BOLD, 12));
							panel4.add(label19);
							label19.setBounds(175, 209, 60, 20);

							// ---- label20 ----
							label20.setText("Reply");
							label20.setBackground(new Color(51, 51, 51));
							label20.setForeground(new Color(255, 255, 102));
							label20.setFont(new Font("Century Gothic",
									Font.BOLD, 12));
							panel4.add(label20);
							label20.setBounds(20, 170, 40, 20);

							// ---- formattedTextField22 ----
							formattedTextField22.setFont(new Font(
									"Century Gothic", Font.PLAIN, 12));
							formattedTextField22.setForeground(new Color(255,
									255, 204));
							formattedTextField22.setBackground(Color.gray);
							formattedTextField22.setText("Sure!");
							panel4.add(formattedTextField22);
							formattedTextField22.setBounds(75, 170, 180, 23);

							{ // compute preferred size
								Dimension preferredSize = new Dimension();
								for (int i = 0; i < panel4.getComponentCount(); i++) {
									Rectangle bounds = panel4.getComponent(i)
											.getBounds();
									preferredSize.width = Math
											.max(bounds.x + bounds.width,
													preferredSize.width);
									preferredSize.height = Math.max(bounds.y
											+ bounds.height,
											preferredSize.height);
								}
								Insets insets = panel4.getInsets();
								preferredSize.width += insets.right;
								preferredSize.height += insets.bottom;
								panel4.setMinimumSize(preferredSize);
								panel4.setPreferredSize(preferredSize);
							}
						}
						tabbedPane1.addTab("Remote", panel4);

					}
					panel1.add(tabbedPane1);
					tabbedPane1.setBounds(15, 55, 370, 275);

					// ---- button1 ----
					button1.setText("Start Training Magic!");
					button1.setBackground(Color.black);
					button1.setFont(new Font("Century Gothic", Font.BOLD, 18));
					button1.setForeground(new Color(0, 102, 51));
					button1.addActionListener(new ActionListener() {
						@Override
						public void actionPerformed(ActionEvent e) {
							button1ActionPerformed(e);
						}
					});
					panel1.add(button1);
					button1.setBounds(25, 335, 350, 55);

					// ---- label1 ----
					label1.setText("White Bear AIO Magic");
					label1.setBackground(new Color(51, 51, 51));
					label1.setForeground(new Color(153, 255, 153));
					label1.setFont(new Font("Century Gothic", Font.BOLD, 24));
					label1.setHorizontalAlignment(SwingConstants.CENTER);
					panel1.add(label1);
					label1.setBounds(40, 5, 315, 50);

					// ---- label2 ----
					label2.setText("Version: 2.01");
					label2.setBackground(new Color(51, 51, 51));
					label2.setForeground(new Color(204, 255, 0));
					label2.setFont(new Font("Century Gothic", Font.PLAIN, 12));
					panel1.add(label2);
					label2.setBounds(300, 51, 83, 20);

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
				WhiteBearGUIContentPane.add(panel1);
				panel1.setBounds(0, 0, 400, 405);

				WhiteBearGUIContentPane
						.setPreferredSize(new Dimension(405, 425));
				WhiteBearGUI.setSize(405, 425);
				WhiteBearGUI.setLocationRelativeTo(WhiteBearGUI.getOwner());
				loadSettings();
			}
		}

		private JFrame WhiteBearGUI;
		private JPanel panel1;
		private JTabbedPane tabbedPane1;
		private JPanel panel6;
		private JCheckBox radioButton12;
		private JComboBox teleportCombo;
		private JCheckBox radioButton25;
		private JButton button2;
		private JLabel label7;
		private JComboBox spellTypeCombo;
		private JLabel label31;
		private JFormattedTextField tfItemID;
		private JComboBox alchemyCombo;
		private JComboBox boltCombo;
		private JLabel label32;
		private JLabel label33;
		private JComboBox curseCombo;
		private JLabel label34;
		private JFormattedTextField tfNpcID;
		private JComboBox enchantCombo;
		private JComboBox othersCombo;
		private JLabel label35;
		private JComboBox bankCombo;
		private JLabel label36;
		private JComboBox superheatCombo;
		private JPanel panel2;
		private JFormattedTextField jTextField;
		private JCheckBox radioButton22;
		private JLabel label4;
		private JComboBox clrSelected;
		private JLabel label5;
		private JLabel label6;
		private JLabel label16;
		private JFormattedTextField textSecond;
		private JLabel label15;
		private JFormattedTextField textMinute;
		private JLabel label14;
		private JFormattedTextField textHour;
		private JLabel label13;
		private JFormattedTextField jTextField2;
		private JLabel label21;
		private JCheckBox radioButton23;
		private JLabel label30;
		private JFormattedTextField tfTextFont;
		private JPanel panel3;
		private JCheckBox radioButton1;
		private JCheckBox radioButton2;
		private JLabel label8;
		private JFormattedTextField formattedTextField1;
		private JLabel label9;
		private JLabel label10;
		private JFormattedTextField formattedTextField3;
		private JLabel label11;
		private JFormattedTextField formattedTextField2;
		private JLabel label12;
		private JFormattedTextField formattedTextField4;
		private JCheckBox radioButton3;
		private JPanel panel5;
		private JCheckBox check3;
		private JCheckBox check2;
		private JCheckBox check4;
		private JLabel label22;
		private JFormattedTextField jTextField3;
		private JLabel label23;
		private JFormattedTextField jTextField4;
		private JLabel label24;
		private JLabel label25;
		private JLabel label26;
		private JLabel label27;
		private JLabel label28;
		private JFormattedTextField jTextField5;
		private JLabel label29;
		private JFormattedTextField jTextField6;
		private JPanel panel4;
		private JTextArea textArea3;
		private JCheckBox radioButton11;
		private JLabel label17;
		private JFormattedTextField formattedTextField11;
		private JLabel label18;
		private JFormattedTextField formattedTextField21;
		private JCheckBox radioButton26;
		private JFormattedTextField formattedTextField31;
		private JLabel label19;
		private JLabel label20;
		private JFormattedTextField formattedTextField22;
		private JButton button1;
		private JLabel label1;
		private JLabel label2;
	}

	private class ChatResponderGUI {
		private static final long serialVersionUID = 1L;

		public boolean loadSettings() {
			try {
				WBini.load(new FileInputStream(new File(
						GlobalConfiguration.Paths.getSettingsDirectory(),
						"WhiteBearAIOMagicV2.ini")));
			} catch (FileNotFoundException e) {
				return false;
			} catch (IOException e) {
				log.warning("[GUI] Error occurred when loading settings!");
				return false;
			}
			try {
				if (WBini.getProperty("CRuseLevelRes") != null)
					radioButton2.setSelected(Boolean.parseBoolean(WBini
							.getProperty("CRuseLevelRes")));
				if (WBini.getProperty("CRuseCustomRes") != null)
					radioButton1.setSelected(Boolean.parseBoolean(WBini
							.getProperty("CRuseCustomRes")));
				if (WBini.getProperty("CRtradeRes") != null)
					textArea3.setText(WBini.getProperty("CRtradeRes"));
				if (WBini.getProperty("CRgreetingRes") != null)
					textArea7.setText(WBini.getProperty("CRgreetingRes"));
				if (WBini.getProperty("CRbotterRes") != null)
					textArea8.setText(WBini.getProperty("CRbotterRes"));
				if (WBini.getProperty("CRlevelRes") != null)
					textArea9.setText(WBini.getProperty("CRlevelRes"));
				if (WBini.getProperty("CRdetection") != null)
					textArea10.setText(WBini.getProperty("CRdetection"));
				if (WBini.getProperty("CRresponse") != null)
					textArea11.setText(WBini.getProperty("CRresponse"));

				if (WBini.getProperty("CRcustomTO") != null)
					formattedTextField1
							.setText(WBini.getProperty("CRcustomTO"));
				if (WBini.getProperty("CRcustomTOR") != null)
					formattedTextField3.setText(WBini
							.getProperty("CRcustomTOR"));
			} catch (java.lang.Exception e) {
				log.warning("[GUI] Settings file is corrupt, using default settings!");
			}
			return true;
		}

		private ChatResponderGUI() {
			initComponentx();
		}

		private void button1ActionPerformed(ActionEvent e) {
			try {
				chatRes.tradeRes = textArea3.getText().toLowerCase().split("/");
				chatRes.greetingRes = textArea7.getText().toLowerCase()
						.split("/");
				chatRes.botterRes = textArea8.getText().toLowerCase()
						.split("/");
				chatRes.levelRes = textArea9.getText().toLowerCase().split("/");
				chatRes.customDetect = textArea10.getText().toLowerCase()
						.split("/");
				chatRes.customRes = textArea11.getText().toLowerCase()
						.split("/");
				chatRes.doLevelRes = radioButton2.isSelected();
				chatRes.doCustomRes = radioButton1.isSelected();
				chatRes.customTO = Integer.parseInt(formattedTextField1
						.getText());
				chatRes.customTOR = Integer.parseInt(formattedTextField3
						.getText());

				WBini.setProperty("CRuseLevelRes", String.valueOf(radioButton2
						.isSelected() ? true : false));
				WBini.setProperty("CRuseCustomRes", String.valueOf(radioButton1
						.isSelected() ? true : false));
				WBini.setProperty("CRtradeRes", textArea3.getText());
				WBini.setProperty("CRgreetingRes", textArea7.getText());
				WBini.setProperty("CRbotterRes", textArea8.getText());
				WBini.setProperty("CRlevelRes", textArea9.getText());
				WBini.setProperty("CRdetection", textArea10.getText());
				WBini.setProperty("CRresponse", textArea11.getText());
				WBini.setProperty("CRcustomTO", formattedTextField1.getText());
				WBini.setProperty("CRcustomTOR", formattedTextField3.getText());
				try {
					WBini.store(new FileWriter(new File(
							GlobalConfiguration.Paths.getSettingsDirectory(),
							"WhiteBearAIOMagicV2.ini")),
							"The GUI Settings for White Bear AIO Magic (Version: "
									+ Double.toString(properties.version())
									+ ")");
				} catch (IOException ioe) {
					log.warning("[GUI] Error occurred when saving GUI settings!");
				}
				chatResGUI = false;
				WhiteBearGUI.dispose();
			} catch (java.lang.Exception ex) {
				log.severe("Error occurred when saving GUI options.");
			}
		}

		private void button2ActionPerformed(ActionEvent e) {
			tabbedPane1.setSelectedIndex(1);
		}

		private void button3ActionPerformed(ActionEvent e) {
			tabbedPane1.setSelectedIndex(2);
		}

		private void back1ActionPerformed(ActionEvent e) {
			tabbedPane1.setSelectedIndex(0);
		}

		private void back2ActionPerformed(ActionEvent e) {
			tabbedPane1.setSelectedIndex(0);
		}

		private void initComponentx() {
			WhiteBearGUI = new JFrame();
			panel1 = new JPanel();
			tabbedPane1 = new JTabbedPane();
			panel6 = new JPanel();
			textArea1 = new JTextArea();
			textArea2 = new JTextArea();
			button2 = new JButton();
			textArea4 = new JTextArea();
			textArea5 = new JTextArea();
			button3 = new JButton();
			textArea6 = new JTextArea();
			panel4 = new JPanel();
			label17 = new JLabel();
			label18 = new JLabel();
			label20 = new JLabel();
			label30 = new JLabel();
			textArea3 = new JTextArea();
			textArea7 = new JTextArea();
			textArea8 = new JTextArea();
			textArea9 = new JTextArea();
			label19 = new JLabel();
			button4 = new JButton();
			radioButton2 = new JCheckBox();
			panel3 = new JPanel();
			radioButton1 = new JCheckBox();
			label8 = new JLabel();
			formattedTextField1 = new JFormattedTextField();
			label9 = new JLabel();
			formattedTextField3 = new JFormattedTextField();
			button5 = new JButton();
			label21 = new JLabel();
			textArea10 = new JTextArea();
			textArea11 = new JTextArea();
			label22 = new JLabel();
			button1 = new JButton();
			label1 = new JLabel();

			// ======== WhiteBearGUI ========
			{
				WhiteBearGUI.setAlwaysOnTop(true);
				WhiteBearGUI.setBackground(Color.black);
				WhiteBearGUI.setResizable(false);
				WhiteBearGUI.setMinimumSize(new Dimension(405, 405));
				WhiteBearGUI.setTitle("White Bear AIO Magic");
				WhiteBearGUI
						.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
				WhiteBearGUI
						.setFont(new Font("Century Gothic", Font.PLAIN, 12));
				Container WhiteBearGUIContentPane = WhiteBearGUI
						.getContentPane();
				WhiteBearGUIContentPane.setLayout(null);

				// ======== panel1 ========
				{
					panel1.setBackground(Color.black);
					panel1.setForeground(Color.green);
					panel1.setMinimumSize(new Dimension(100, 200));
					panel1.setLayout(null);

					// ======== tabbedPane1 ========
					{
						tabbedPane1.setFont(new Font("Century Gothic",
								Font.PLAIN, 12));
						tabbedPane1.setForeground(new Color(0, 153, 0));
						tabbedPane1.setEnabled(false);

						// ======== panel6 ========
						{
							panel6.setBackground(Color.black);
							panel6.setLayout(null);

							// ---- textArea1 ----
							textArea1
									.setText(" This GUI allows you to change Chat Responder\nsettings. Just click start if you do not know what\nto do.");
							textArea1.setLineWrap(true);
							textArea1.setFont(new Font("Century Gothic",
									Font.PLAIN, 14));
							textArea1.setTabSize(0);
							textArea1.setBackground(Color.black);
							textArea1.setForeground(new Color(204, 255, 0));
							textArea1.setEditable(false);
							textArea1.setBorder(null);
							textArea1.setOpaque(false);
							textArea1.setRequestFocusEnabled(false);
							textArea1.setFocusable(false);
							panel6.add(textArea1);
							textArea1.setBounds(20, 10, 330, 60);

							// ---- textArea2 ----
							textArea2
									.setText(" For responses, separate each response with /\nE.g. For hi/hello/yes?, the possible responses\nare hi, hello and yes?. When the bot needs to\nrespond, it will randomly pick one response");
							textArea2.setLineWrap(true);
							textArea2.setFont(new Font("Century Gothic",
									Font.PLAIN, 14));
							textArea2.setTabSize(0);
							textArea2.setBackground(Color.black);
							textArea2.setForeground(new Color(204, 255, 0));
							textArea2.setEditable(false);
							textArea2.setBorder(null);
							textArea2.setOpaque(false);
							textArea2.setRequestFocusEnabled(false);
							textArea2.setFocusable(false);
							panel6.add(textArea2);
							textArea2.setBounds(20, 80, 330, 78);

							// ---- button2 ----
							button2.setText("Customize Responses");
							button2.setBackground(Color.black);
							button2.setFont(new Font("Century Gothic",
									Font.BOLD, 12));
							button2.setForeground(new Color(0, 102, 51));
							button2.addActionListener(new ActionListener() {
								@Override
								public void actionPerformed(ActionEvent e) {
									button2ActionPerformed(e);
								}
							});
							panel6.add(button2);
							button2.setBounds(190, 157, 160, 23);

							// ---- textArea4 ----
							textArea4.setText("and use it.");
							textArea4.setLineWrap(true);
							textArea4.setFont(new Font("Century Gothic",
									Font.PLAIN, 14));
							textArea4.setTabSize(0);
							textArea4.setBackground(Color.black);
							textArea4.setForeground(new Color(204, 255, 0));
							textArea4.setEditable(false);
							textArea4.setBorder(null);
							textArea4.setOpaque(false);
							textArea4.setRequestFocusEnabled(false);
							textArea4.setFocusable(false);
							panel6.add(textArea4);
							textArea4.setBounds(20, 156, 85, 20);

							// ---- textArea5 ----
							textArea5
									.setText(" You can also set a custom detection, reply");
							textArea5.setLineWrap(true);
							textArea5.setFont(new Font("Century Gothic",
									Font.PLAIN, 14));
							textArea5.setTabSize(0);
							textArea5.setBackground(Color.black);
							textArea5.setForeground(new Color(204, 255, 0));
							textArea5.setEditable(false);
							textArea5.setBorder(null);
							textArea5.setOpaque(false);
							textArea5.setRequestFocusEnabled(false);
							textArea5.setFocusable(false);
							panel6.add(textArea5);
							textArea5.setBounds(20, 190, 330, 20);

							// ---- button3 ----
							button3.setText("Custom detection");
							button3.setBackground(Color.black);
							button3.setFont(new Font("Century Gothic",
									Font.BOLD, 12));
							button3.setForeground(new Color(0, 102, 51));
							button3.addActionListener(new ActionListener() {
								@Override
								public void actionPerformed(ActionEvent e) {
									button3ActionPerformed(e);
								}
							});
							panel6.add(button3);
							button3.setBounds(210, 207, 140, 23);

							// ---- textArea6 ----
							textArea6.setText("and timeout.");
							textArea6.setLineWrap(true);
							textArea6.setFont(new Font("Century Gothic",
									Font.PLAIN, 14));
							textArea6.setTabSize(0);
							textArea6.setBackground(Color.black);
							textArea6.setForeground(new Color(204, 255, 0));
							textArea6.setEditable(false);
							textArea6.setBorder(null);
							textArea6.setOpaque(false);
							textArea6.setRequestFocusEnabled(false);
							textArea6.setFocusable(false);
							panel6.add(textArea6);
							textArea6.setBounds(20, 209, 100, 20);

							{ // compute preferred size
								Dimension preferredSize = new Dimension();
								for (int i = 0; i < panel6.getComponentCount(); i++) {
									Rectangle bounds = panel6.getComponent(i)
											.getBounds();
									preferredSize.width = Math
											.max(bounds.x + bounds.width,
													preferredSize.width);
									preferredSize.height = Math.max(bounds.y
											+ bounds.height,
											preferredSize.height);
								}
								Insets insets = panel6.getInsets();
								preferredSize.width += insets.right;
								preferredSize.height += insets.bottom;
								panel6.setMinimumSize(preferredSize);
								panel6.setPreferredSize(preferredSize);
							}
						}
						tabbedPane1.addTab("Info", panel6);

						// ======== panel4 ========
						{
							panel4.setBackground(Color.black);
							panel4.setLayout(null);

							// ---- label17 ----
							label17.setText("Trade Response");
							label17.setBackground(new Color(51, 51, 51));
							label17.setForeground(new Color(255, 255, 102));
							label17.setFont(new Font("Century Gothic",
									Font.BOLD, 12));
							label17.setHorizontalAlignment(SwingConstants.LEFT);
							panel4.add(label17);
							label17.setBounds(5, 15, 110, 20);

							// ---- label18 ----
							label18.setText("Greeting Response");
							label18.setBackground(new Color(51, 51, 51));
							label18.setForeground(new Color(255, 255, 102));
							label18.setFont(new Font("Century Gothic",
									Font.BOLD, 12));
							label18.setHorizontalAlignment(SwingConstants.LEFT);
							panel4.add(label18);
							label18.setBounds(5, 65, 120, 20);

							// ---- label20 ----
							label20.setText("Botter! Response");
							label20.setBackground(new Color(51, 51, 51));
							label20.setForeground(new Color(255, 255, 102));
							label20.setFont(new Font("Century Gothic",
									Font.BOLD, 12));
							panel4.add(label20);
							label20.setBounds(5, 115, 115, 20);

							// ---- label30 ----
							label30.setText("Level up (yourself)");
							label30.setBackground(new Color(51, 51, 51));
							label30.setForeground(new Color(255, 255, 102));
							label30.setFont(new Font("Century Gothic",
									Font.BOLD, 12));
							panel4.add(label30);
							label30.setBounds(5, 165, 115, 20);

							// ---- textArea3 ----
							textArea3.setForeground(new Color(255, 255, 204));
							textArea3.setBackground(Color.gray);
							textArea3.setText("no thanks/no thx/nope/im fine");
							textArea3.setFont(new Font("Century Gothic",
									Font.PLAIN, 12));
							textArea3.setLineWrap(true);
							panel4.add(textArea3);
							textArea3.setBounds(130, 15, 225, 37);

							// ---- textArea7 ----
							textArea7.setForeground(new Color(255, 255, 204));
							textArea7.setBackground(Color.gray);
							textArea7
									.setText("hi!/hi./hi/hello/hello!/hello./hello../yo/yo!/yes?/what/what?/hey!");
							textArea7.setFont(new Font("Century Gothic",
									Font.PLAIN, 12));
							textArea7.setLineWrap(true);
							panel4.add(textArea7);
							textArea7.setBounds(130, 65, 225, 37);

							// ---- textArea8 ----
							textArea8.setForeground(new Color(255, 255, 204));
							textArea8.setBackground(Color.gray);
							textArea8
									.setText("huh/zzz/.../???/?????/what/what?/no/nop/nope");
							textArea8.setFont(new Font("Century Gothic",
									Font.PLAIN, 12));
							textArea8.setLineWrap(true);
							panel4.add(textArea8);
							textArea8.setBounds(130, 115, 225, 37);

							// ---- textArea9 ----
							textArea9.setForeground(new Color(255, 255, 204));
							textArea9.setBackground(Color.gray);
							textArea9
									.setText("yay/haha/:)/yay!/yay!!!/finally...");
							textArea9.setFont(new Font("Century Gothic",
									Font.PLAIN, 12));
							textArea9.setLineWrap(true);
							panel4.add(textArea9);
							textArea9.setBounds(130, 165, 225, 37);

							// ---- label19 ----
							label19.setText("(70% chance to talk)");
							label19.setBackground(new Color(51, 51, 51));
							label19.setForeground(new Color(255, 255, 102));
							label19.setFont(new Font("Century Gothic",
									Font.BOLD, 12));
							panel4.add(label19);
							label19.setBounds(4, 182, 130, 20);

							// ---- button4 ----
							button4.setText("Back");
							button4.setBackground(Color.black);
							button4.setFont(new Font("Century Gothic",
									Font.BOLD, 12));
							button4.setForeground(new Color(0, 102, 51));
							button4.addActionListener(new ActionListener() {
								@Override
								public void actionPerformed(ActionEvent e) {
									back1ActionPerformed(e);
								}
							});
							panel4.add(button4);
							button4.setBounds(285, 215, 70, 23);

							// ---- radioButton2 ----
							radioButton2.setText("Use Level up Response");
							radioButton2.setBackground(Color.black);
							radioButton2.setForeground(Color.yellow);
							radioButton2.setFont(new Font("Century Gothic",
									Font.BOLD, 13));
							panel4.add(radioButton2);
							radioButton2.setBounds(10, 210, 183, 25);

							{ // compute preferred size
								Dimension preferredSize = new Dimension();
								for (int i = 0; i < panel4.getComponentCount(); i++) {
									Rectangle bounds = panel4.getComponent(i)
											.getBounds();
									preferredSize.width = Math
											.max(bounds.x + bounds.width,
													preferredSize.width);
									preferredSize.height = Math.max(bounds.y
											+ bounds.height,
											preferredSize.height);
								}
								Insets insets = panel4.getInsets();
								preferredSize.width += insets.right;
								preferredSize.height += insets.bottom;
								panel4.setMinimumSize(preferredSize);
								panel4.setPreferredSize(preferredSize);
							}
						}
						tabbedPane1.addTab("Responses", panel4);

						// ======== panel3 ========
						{
							panel3.setBackground(Color.black);
							panel3.setLayout(null);

							// ---- radioButton1 ----
							radioButton1.setText("Use Custom Detection");
							radioButton1.setBackground(Color.black);
							radioButton1.setForeground(Color.yellow);
							radioButton1.setFont(new Font("Century Gothic",
									Font.BOLD, 13));
							panel3.add(radioButton1);
							radioButton1.setBounds(17, 15, 183, 25);

							// ---- label8 ----
							label8.setText("Timeout (seconds):");
							label8.setBackground(new Color(51, 51, 51));
							label8.setForeground(new Color(255, 255, 102));
							label8.setFont(new Font("Century Gothic",
									Font.BOLD, 12));
							panel3.add(label8);
							label8.setBounds(25, 160, 125, 20);

							// ---- formattedTextField1 ----
							formattedTextField1.setFont(new Font(
									"Century Gothic", Font.PLAIN, 12));
							formattedTextField1.setForeground(new Color(255,
									255, 204));
							formattedTextField1.setBackground(Color.gray);
							formattedTextField1.setText("160");
							panel3.add(formattedTextField1);
							formattedTextField1.setBounds(155, 160, 70, 23);

							// ---- label9 ----
							label9.setText("\u00b1");
							label9.setBackground(new Color(51, 51, 51));
							label9.setForeground(new Color(255, 255, 102));
							label9.setFont(new Font("Century Gothic",
									Font.BOLD, 12));
							panel3.add(label9);
							label9.setBounds(230, 160, 15, 20);

							// ---- formattedTextField3 ----
							formattedTextField3.setFont(new Font(
									"Century Gothic", Font.PLAIN, 12));
							formattedTextField3.setForeground(new Color(255,
									255, 204));
							formattedTextField3.setBackground(Color.gray);
							formattedTextField3.setText("30");
							panel3.add(formattedTextField3);
							formattedTextField3.setBounds(245, 160, 59, 23);

							// ---- button5 ----
							button5.setText("Back");
							button5.setBackground(Color.black);
							button5.setFont(new Font("Century Gothic",
									Font.BOLD, 12));
							button5.setForeground(new Color(0, 102, 51));
							button5.addActionListener(new ActionListener() {
								@Override
								public void actionPerformed(ActionEvent e) {
									back2ActionPerformed(e);
								}
							});
							panel3.add(button5);
							button5.setBounds(285, 215, 70, 23);

							// ---- label21 ----
							label21.setText("Detect:");
							label21.setBackground(new Color(51, 51, 51));
							label21.setForeground(new Color(255, 255, 102));
							label21.setFont(new Font("Century Gothic",
									Font.BOLD, 12));
							label21.setHorizontalAlignment(SwingConstants.LEFT);
							panel3.add(label21);
							label21.setBounds(25, 50, 55, 20);

							// ---- textArea10 ----
							textArea10.setForeground(new Color(255, 255, 204));
							textArea10.setBackground(Color.gray);
							textArea10
									.setText("i love u/i luv u/i love you/i luv you/i lov u/i love you");
							textArea10.setFont(new Font("Century Gothic",
									Font.PLAIN, 12));
							textArea10.setLineWrap(true);
							panel3.add(textArea10);
							textArea10.setBounds(95, 50, 235, 37);

							// ---- textArea11 ----
							textArea11.setForeground(new Color(255, 255, 204));
							textArea11.setBackground(Color.gray);
							textArea11
									.setText("yuck/yuk/gross/eww/zzz/.../zzzz/....");
							textArea11.setFont(new Font("Century Gothic",
									Font.PLAIN, 12));
							textArea11.setLineWrap(true);
							panel3.add(textArea11);
							textArea11.setBounds(95, 100, 235, 37);

							// ---- label22 ----
							label22.setText("Respond:");
							label22.setBackground(new Color(51, 51, 51));
							label22.setForeground(new Color(255, 255, 102));
							label22.setFont(new Font("Century Gothic",
									Font.BOLD, 12));
							label22.setHorizontalAlignment(SwingConstants.LEFT);
							panel3.add(label22);
							label22.setBounds(25, 100, 65, 20);

							{ // compute preferred size
								Dimension preferredSize = new Dimension();
								for (int i = 0; i < panel3.getComponentCount(); i++) {
									Rectangle bounds = panel3.getComponent(i)
											.getBounds();
									preferredSize.width = Math
											.max(bounds.x + bounds.width,
													preferredSize.width);
									preferredSize.height = Math.max(bounds.y
											+ bounds.height,
											preferredSize.height);
								}
								Insets insets = panel3.getInsets();
								preferredSize.width += insets.right;
								preferredSize.height += insets.bottom;
								panel3.setMinimumSize(preferredSize);
								panel3.setPreferredSize(preferredSize);
							}
						}
						tabbedPane1.addTab("Custom", panel3);

					}
					panel1.add(tabbedPane1);
					tabbedPane1.setBounds(15, 45, 370, 275);

					// ---- button1 ----
					button1.setText("Complete Customization");
					button1.setBackground(Color.black);
					button1.setFont(new Font("Century Gothic", Font.BOLD, 18));
					button1.setForeground(new Color(0, 102, 51));
					button1.addActionListener(new ActionListener() {
						@Override
						public void actionPerformed(ActionEvent e) {
							button1ActionPerformed(e);
						}
					});
					panel1.add(button1);
					button1.setBounds(25, 325, 350, 55);

					// ---- label1 ----
					label1.setText("Chat Responder Customization");
					label1.setBackground(new Color(51, 51, 51));
					label1.setForeground(new Color(153, 255, 153));
					label1.setFont(new Font("Century Gothic", Font.BOLD, 20));
					panel1.add(label1);
					label1.setBounds(50, 5, 315, 40);

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
				WhiteBearGUIContentPane.add(panel1);
				panel1.setBounds(0, 0, 400, 395);

				WhiteBearGUIContentPane
						.setPreferredSize(new Dimension(405, 420));
				WhiteBearGUI.setSize(405, 420);
				WhiteBearGUI.setLocationRelativeTo(WhiteBearGUI.getOwner());
				loadSettings();
			}
		}

		private JFrame WhiteBearGUI;
		private JPanel panel1;
		private JTabbedPane tabbedPane1;
		private JPanel panel6;
		private JTextArea textArea1;
		private JTextArea textArea2;
		private JButton button2;
		private JTextArea textArea4;
		private JTextArea textArea5;
		private JButton button3;
		private JTextArea textArea6;
		private JPanel panel4;
		private JLabel label17;
		private JLabel label18;
		private JLabel label20;
		private JLabel label30;
		private JTextArea textArea3;
		private JTextArea textArea7;
		private JTextArea textArea8;
		private JTextArea textArea9;
		private JLabel label19;
		private JButton button4;
		private JCheckBox radioButton2;
		private JPanel panel3;
		private JCheckBox radioButton1;
		private JLabel label8;
		private JFormattedTextField formattedTextField1;
		private JLabel label9;
		private JFormattedTextField formattedTextField3;
		private JButton button5;
		private JLabel label21;
		private JTextArea textArea10;
		private JTextArea textArea11;
		private JLabel label22;
		private JButton button1;
		private JLabel label1;
	}
}
