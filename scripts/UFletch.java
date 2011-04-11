import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import java.io.*;
import java.net.*;
import java.sql.Timestamp;
import java.util.*;
import java.util.logging.*;
import java.util.regex.*;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.text.*;

import org.rsbot.event.events.MessageEvent;
import org.rsbot.event.listeners.*;
import org.rsbot.util.GlobalConfiguration;
import org.rsbot.script.*;
import org.rsbot.script.methods.*;
import org.rsbot.script.wrappers.*;

@ScriptManifest(authors = { "Fletch To 99" }, keywords = "Fletching", name = "UFletch", version = 2.21, description = "The best fletcher!")
/**
 * All-in-One Fletching script for RSBot 2.XX
 * @author Fletch To 99
 */
public class UFletch extends Script implements PaintListener, MouseListener,
		MouseMotionListener, MessageListener {

	private static interface constants {
		String[] optionMethod = { "Fletch", "String", "Fletch&String",
				"Chop-Fletch-Drop/Shaft" };
		String[] optionLog = { "Normal", "Oak", "Willow", "Maple", "Yew",
				"Magic" };
		String[] optionBow = { "Short", "Long", "Shafts", "Stocks", "N/A" };
		String[] optionKnife = { "Normal", "clay", "N/A" };
		String[] optionAxe = { "Bronze", "Iron", "Black", "Mith", "Addy",
				"Rune", "Dragon", "N/A" };
		String[] optionColor = { "Black", "Red", "Orange", "Blue", "Green",
				"Yellow", "Pink", "White", "Tan" };
		final String UPDATER_FILE_NAME = "UFletch.java";
		final String UPDATER_URL = "http://dl.dropbox.com/u/23938245/Scripts/UFletch/Script/UFletch.java";
		final Pattern UPDATER_VERSION_PATTERN = Pattern
				.compile("version\\s*=\\s*([0-9.]+)");
		final Color TAN = new Color(220, 202, 169);
		MenuItem item1 = new MenuItem("Stop");
		MenuItem item2 = new MenuItem("Pause");
		MenuItem item3 = new MenuItem("Resume");
		MenuItem item4 = new MenuItem("Open Gui");
		MenuItem item5 = new MenuItem("Help");
		MenuItem item6 = new MenuItem("IRC");
		static final String server = "irc.rizon.net";
		static final String channel = "#ufletch";
	}

	private int amount = 0;
	private int startXP = 0;
	private int fletched = 0;
	private int strung = 0;
	private int xpIsClose = 13020000;
	private int currentexp = 0;
	private int Mouse1 = 50;
	private int Mouse2 = 8;
	private int xpGained = 0;
	private int xpToLevel = 0;
	private int hoursTNL = 0;
	private int minsTNL = 0;
	private int fail = 0;
	private int full = 0;

	private long startTime = System.currentTimeMillis();

	private Point p = null;
	private Point p2 = null;
	private Point z = null;

	private Image invPaint = null;
	private Image paint = null;
	private Image hide = null;
	private Image show = null;
	private Image guiButton = null;
	private Image watermark = null;
	private Image icon = null;

	private double version = 0.0;
	private double newver = 0.0;

	private boolean has99 = false;
	private boolean fullPaint = true;
	private boolean isClicking = false;
	private boolean fletchAndString = false;
	private boolean pause = false;

	private String status = "";
	private String name = null;

	private gui gui;
	private trayInfo trayInfo;
	private beeper beep;
	private Thread b;

	private Irc irc;
	private Thread i;
	private IRCGui IRCgui;
	private ircNameGUI nameGUI;
	private Document doc;
	private Document users;
	private RSTile[] path;

	private final LinkedList<MousePathPoint> mousePath = new LinkedList<MousePathPoint>();
	private final LinkedList<MousePathPoint2> mousePath2 = new LinkedList<MousePathPoint2>();
	private final LinkedList<MouseCirclePathPoint> mouseCirclePath = new LinkedList<MouseCirclePathPoint>();
	private final LinkedList<MouseCirclePathPoint2> mouseCirclePath2 = new LinkedList<MouseCirclePathPoint2>();

	private Color getColorText() {
		if (gui.comboBox12.getSelectedIndex() == 0) {
			return Color.BLACK;
		} else if (gui.comboBox12.getSelectedIndex() == 1) {
			return Color.RED;
		} else if (gui.comboBox12.getSelectedIndex() == 2) {
			return Color.ORANGE;
		} else if (gui.comboBox12.getSelectedIndex() == 3) {
			return Color.BLUE;
		} else if (gui.comboBox12.getSelectedIndex() == 4) {
			return Color.GREEN;
		} else if (gui.comboBox12.getSelectedIndex() == 5) {
			return Color.YELLOW;
		} else if (gui.comboBox12.getSelectedIndex() == 6) {
			return Color.PINK;
		} else if (gui.comboBox12.getSelectedIndex() == 7) {
			return Color.WHITE;
		} else if (gui.comboBox12.getSelectedIndex() == 8) {
			return constants.TAN;
		}
		return Color.BLACK;
	}

	private Color getColorPaint() {
		if (gui.comboBox13.getSelectedIndex() == 0) {
			return Color.BLACK;
		} else if (gui.comboBox13.getSelectedIndex() == 1) {
			return Color.RED;
		} else if (gui.comboBox13.getSelectedIndex() == 2) {
			return Color.ORANGE;
		} else if (gui.comboBox13.getSelectedIndex() == 3) {
			return Color.BLUE;
		} else if (gui.comboBox13.getSelectedIndex() == 4) {
			return Color.GREEN;
		} else if (gui.comboBox13.getSelectedIndex() == 5) {
			return Color.YELLOW;
		} else if (gui.comboBox13.getSelectedIndex() == 6) {
			return Color.PINK;
		} else if (gui.comboBox13.getSelectedIndex() == 7) {
			return Color.WHITE;
		} else if (gui.comboBox13.getSelectedIndex() == 8) {
			return constants.TAN;
		}
		return Color.BLACK;
	}

	private Color getColorProgressBarBelow() {
		if (gui.comboBox8.getSelectedIndex() == 0) {
			return Color.BLACK;
		} else if (gui.comboBox8.getSelectedIndex() == 1) {
			return Color.RED;
		} else if (gui.comboBox8.getSelectedIndex() == 2) {
			return Color.ORANGE;
		} else if (gui.comboBox8.getSelectedIndex() == 3) {
			return Color.BLUE;
		} else if (gui.comboBox8.getSelectedIndex() == 4) {
			return Color.GREEN;
		} else if (gui.comboBox8.getSelectedIndex() == 5) {
			return Color.YELLOW;
		} else if (gui.comboBox8.getSelectedIndex() == 6) {
			return Color.PINK;
		} else if (gui.comboBox8.getSelectedIndex() == 7) {
			return Color.WHITE;
		} else if (gui.comboBox8.getSelectedIndex() == 8) {
			return constants.TAN;
		}
		return Color.BLACK;
	}

	private Color getColorProgressBarOnTop() {
		if (gui.comboBox9.getSelectedIndex() == 0) {
			return Color.BLACK;
		} else if (gui.comboBox9.getSelectedIndex() == 1) {
			return Color.RED;
		} else if (gui.comboBox9.getSelectedIndex() == 2) {
			return Color.ORANGE;
		} else if (gui.comboBox9.getSelectedIndex() == 3) {
			return Color.BLUE;
		} else if (gui.comboBox9.getSelectedIndex() == 4) {
			return Color.GREEN;
		} else if (gui.comboBox9.getSelectedIndex() == 5) {
			return Color.YELLOW;
		} else if (gui.comboBox9.getSelectedIndex() == 6) {
			return Color.PINK;
		} else if (gui.comboBox9.getSelectedIndex() == 7) {
			return Color.WHITE;
		} else if (gui.comboBox9.getSelectedIndex() == 8) {
			return constants.TAN;
		}
		return Color.BLACK;
	}

	private Color getColorRSBotLine() {
		if (gui.comboBox10.getSelectedIndex() == 0) {
			return Color.BLACK;
		} else if (gui.comboBox10.getSelectedIndex() == 1) {
			return Color.RED;
		} else if (gui.comboBox10.getSelectedIndex() == 2) {
			return Color.ORANGE;
		} else if (gui.comboBox10.getSelectedIndex() == 3) {
			return Color.BLUE;
		} else if (gui.comboBox10.getSelectedIndex() == 4) {
			return Color.GREEN;
		} else if (gui.comboBox10.getSelectedIndex() == 5) {
			return Color.YELLOW;
		} else if (gui.comboBox10.getSelectedIndex() == 6) {
			return Color.PINK;
		} else if (gui.comboBox10.getSelectedIndex() == 7) {
			return Color.WHITE;
		} else if (gui.comboBox10.getSelectedIndex() == 8) {
			return constants.TAN;
		}
		return Color.BLACK;
	}

	private Color getColorRSBotCrosshair() {
		if (gui.comboBox11.getSelectedIndex() == 0) {
			return Color.BLACK;
		} else if (gui.comboBox11.getSelectedIndex() == 1) {
			return Color.RED;
		} else if (gui.comboBox11.getSelectedIndex() == 2) {
			return Color.ORANGE;
		} else if (gui.comboBox11.getSelectedIndex() == 3) {
			return Color.BLUE;
		} else if (gui.comboBox11.getSelectedIndex() == 4) {
			return Color.GREEN;
		} else if (gui.comboBox11.getSelectedIndex() == 5) {
			return Color.YELLOW;
		} else if (gui.comboBox11.getSelectedIndex() == 6) {
			return Color.PINK;
		} else if (gui.comboBox11.getSelectedIndex() == 7) {
			return Color.WHITE;
		} else if (gui.comboBox11.getSelectedIndex() == 8) {
			return constants.TAN;
		}
		return Color.BLACK;
	}

	private Color getColorUserLine() {
		if (gui.comboBox14.getSelectedIndex() == 0) {
			return Color.BLACK;
		} else if (gui.comboBox14.getSelectedIndex() == 1) {
			return Color.RED;
		} else if (gui.comboBox14.getSelectedIndex() == 2) {
			return Color.ORANGE;
		} else if (gui.comboBox14.getSelectedIndex() == 3) {
			return Color.BLUE;
		} else if (gui.comboBox14.getSelectedIndex() == 4) {
			return Color.GREEN;
		} else if (gui.comboBox14.getSelectedIndex() == 5) {
			return Color.YELLOW;
		} else if (gui.comboBox14.getSelectedIndex() == 6) {
			return Color.PINK;
		} else if (gui.comboBox14.getSelectedIndex() == 7) {
			return Color.WHITE;
		} else if (gui.comboBox14.getSelectedIndex() == 8) {
			return constants.TAN;
		}
		return Color.BLACK;
	}

	private Color getColorUserCrosshair() {
		if (gui.comboBox15.getSelectedIndex() == 0) {
			return Color.BLACK;
		} else if (gui.comboBox15.getSelectedIndex() == 1) {
			return Color.RED;
		} else if (gui.comboBox15.getSelectedIndex() == 2) {
			return Color.ORANGE;
		} else if (gui.comboBox15.getSelectedIndex() == 3) {
			return Color.BLUE;
		} else if (gui.comboBox15.getSelectedIndex() == 4) {
			return Color.GREEN;
		} else if (gui.comboBox15.getSelectedIndex() == 5) {
			return Color.YELLOW;
		} else if (gui.comboBox15.getSelectedIndex() == 6) {
			return Color.PINK;
		} else if (gui.comboBox15.getSelectedIndex() == 7) {
			return Color.WHITE;
		} else if (gui.comboBox15.getSelectedIndex() == 8) {
			return constants.TAN;
		}
		return Color.BLACK;
	}

	private int getMethod() {
		if (gui.comboBox1.getSelectedIndex() == 0) {
			return 1;
		} else if (gui.comboBox1.getSelectedIndex() == 1) {
			return 2;
		} else if (gui.comboBox1.getSelectedIndex() == 2) {
			return 3;
		} else if (gui.comboBox1.getSelectedIndex() == 3) {
			return 4;
		}
		return -1;
	}

	private int getLogId() {
		if (gui.comboBox2.getSelectedIndex() == 0) {
			return 1511;
		} else if (gui.comboBox2.getSelectedIndex() == 1) {
			return 1521;
		} else if (gui.comboBox2.getSelectedIndex() == 2) {
			return 1519;
		} else if (gui.comboBox2.getSelectedIndex() == 3) {
			return 1517;
		} else if (gui.comboBox2.getSelectedIndex() == 4) {
			return 1515;
		} else if (gui.comboBox2.getSelectedIndex() == 5) {
			return 1513;
		}
		return -1;
	}

	private int[] getTreeId() {
		if (gui.comboBox2.getSelectedIndex() == 0) {
			return new int[] { 1278, 1276, 38787, 38760, 38788, 38784, 38783,
					38782 };
		} else if (gui.comboBox2.getSelectedIndex() == 1) {
			return new int[] { 1281, 38731 };
		} else if (gui.comboBox2.getSelectedIndex() == 2) {
			return new int[] { 5551, 5552, 5553, 1308, 38616, 38617, 38627 };
		} else if (gui.comboBox2.getSelectedIndex() == 3) {
			return new int[] { 1307 };
		} else if (gui.comboBox2.getSelectedIndex() == 4) {
			return new int[] { 1309, 38755 };
		} else if (gui.comboBox2.getSelectedIndex() == 5) {
			return new int[] { 1306 };
		}
		return null;
	}

	private int getUnstrungId() {
		if (getBowType() == 1) { // 1 = Shortbows, 2 = Longbows
			if (gui.comboBox2.getSelectedIndex() == 0) {
				return 50;
			} else if (gui.comboBox2.getSelectedIndex() == 1) {
				return 54;
			} else if (gui.comboBox2.getSelectedIndex() == 2) {
				return 60;
			} else if (gui.comboBox2.getSelectedIndex() == 3) {
				return 64;
			} else if (gui.comboBox2.getSelectedIndex() == 4) {
				return 68;
			} else if (gui.comboBox2.getSelectedIndex() == 5) {
				return 72;
			}
		} else {
			if (gui.comboBox2.getSelectedIndex() == 0) {
				return 48;
			} else if (gui.comboBox2.getSelectedIndex() == 1) {
				return 56;
			} else if (gui.comboBox2.getSelectedIndex() == 2) {
				return 58;
			} else if (gui.comboBox2.getSelectedIndex() == 3) {
				return 62;
			} else if (gui.comboBox2.getSelectedIndex() == 4) {
				return 66;
			} else if (gui.comboBox2.getSelectedIndex() == 5) {
				return 70;
			}
		}
		return -1;
	}

	private int getBowType() {
		if (gui.comboBox3.getSelectedIndex() == 0) {
			return 1;
		} else if (gui.comboBox3.getSelectedIndex() == 1) {
			return 2;
		} else if (gui.comboBox3.getSelectedIndex() == 2) {
			return 3;
		} else if (gui.comboBox3.getSelectedIndex() == 3) {
			return 4;
		}
		return -1;
	}

	private int getKnifeId() {
		if (gui.comboBox4.getSelectedIndex() == 0) {
			return 946;
		} else if (gui.comboBox4.getSelectedIndex() == 1) {
			return 14111;
		}
		return -1;
	}

	private int getAxeId() {
		if (gui.comboBox5.getSelectedIndex() == 0) {
			return 1351;
		} else if (gui.comboBox5.getSelectedIndex() == 1) {
			return 1349;
		} else if (gui.comboBox5.getSelectedIndex() == 2) {
			return 1361;
		} else if (gui.comboBox5.getSelectedIndex() == 3) {
			return 1355;
		} else if (gui.comboBox5.getSelectedIndex() == 4) {
			return 1357;
		} else if (gui.comboBox5.getSelectedIndex() == 5) {
			return 1359;
		} else if (gui.comboBox5.getSelectedIndex() == 6) {
			return 6739;
		}
		return -1;
	}

	private boolean isBusy() {
		if (getMethod() == 2) {
			if (getMyPlayer().getAnimation() == -1) {
				for (int i = 0; i < 50; i++) {
					sleep(50);
					if (getMyPlayer().getAnimation() != -1
							|| inventory.getCount() == 28
							|| inventory.getCount() == 0) {
						break;
					}
				}
			}
		}
		sleep(25);
		return (getMyPlayer().getAnimation() != -1);
	}

	private boolean openBank() {
		return bank.open();
	}

	private boolean closeSWIFace() {
		if (interfaces.get(276).isValid()) {
			sleep(random(100, 200));
			interfaces.get(276).getComponent(76).doClick(true);
			sleep(random(300, 400));
		}
		return !interfaces.get(276).isValid();
	}

	public int loop() {
		if (getMethod() == 1) {
			fletch();
			sleep(random(200, 250));
		} else if (getMethod() == 2) {
			string();
			sleep(random(200, 250));
		} else if (getMethod() == 3) {
			fletchAndString();
			sleep(random(200, 250));
		} else if (getMethod() == 4) {
			cfd();
			sleep(random(200, 250));
		}
		closeSWIFace();
		pauseScript();
		return random(200, 300);
	}

	private void fletch() {
		amount = Integer.parseInt(gui.textField1.getText());
		if (!inventory.contains(getKnifeId()) && amount == 0 && !isBusy()
				&& !interfaces.get(905).isValid()) {
			withdrawKnife();
			sleep(random(200, 250));
		}
		if (!inventory.contains(getKnifeId()) && fletched <= amount
				&& !isBusy() && !interfaces.get(905).isValid()) {
			withdrawKnife();
			sleep(random(200, 250));
		}
		if (!inventory.contains(getLogId()) && amount == 0 && !isBusy()
				&& !interfaces.get(905).isValid()) {
			if (getBowType() == 1 || getBowType() == 2) {
				withdrawLogs();
			} else if (getBowType() == 3) {
				withdrawShafts();
			} else if (getBowType() == 4) {
				withdrawStocks();
			}
			sleep(random(200, 250));
		} else if (!inventory.contains(getLogId()) && fletched <= amount
				&& !isBusy() && !interfaces.get(905).isValid()) {
			if (getBowType() == 1 || getBowType() == 2) {
				withdrawLogs();
			} else if (getBowType() == 3) {
				withdrawShafts();
			} else if (getBowType() == 4) {
				withdrawStocks();
			}
			sleep(random(200, 250));
		} else if (fletchAndString && !isBusy() && fletched >= amount
				&& amount > 0) {
			gui.comboBox1.setSelectedItem("String");
		} else if (fletchAndString && !isBusy() && amount == 0
				&& bank.getItem(getLogId()) == null
				&& !interfaces.get(905).isValid()) {
			sleep(random(50, 100));
			if (fletchAndString && !isBusy() && amount == 0
					&& bank.getItem(getLogId()) == null
					&& !interfaces.get(905).isValid()
					&& inventory.getCount() < 1) {
				gui.comboBox1.setSelectedItem("String");
			}
		} else if (fletched >= amount && amount != 0 && !fletchAndString) {
			log("Fletched amount logging out!");
			stopScript();
		}
		if (inventory.contains(getLogId())
				&& inventory.containsOneOf(getKnifeId()) && amount == 0
				&& !isBusy()) {
			if (getBowType() == 1 || getBowType() == 2) {
				fletchLogs();
			} else if (getBowType() == 3) {
				fletchShafts();
			} else if (getBowType() == 4) {
				fletchStocks();
			}
			sleep(random(200, 250));
		} else if (inventory.contains(getLogId())
				&& inventory.containsOneOf(getKnifeId()) && fletched <= amount
				&& !isBusy()) {
			if (getBowType() == 1 || getBowType() == 2) {
				fletchLogs();
			} else if (getBowType() == 3) {
				fletchShafts();
			} else if (getBowType() == 4) {
				fletchStocks();
			}
			sleep(random(100, 250));
		} else if (fletchAndString && !isBusy() && fletched >= amount
				&& amount > 0) {
			gui.comboBox1.setSelectedItem("String");
		} else if (fletchAndString && !isBusy() && amount == 0
				&& bank.getItem(getLogId()) == null
				&& !interfaces.get(905).isValid()) {
			sleep(random(50, 100));
			if (fletchAndString && !isBusy() && amount == 0
					&& bank.getItem(getLogId()) == null
					&& !interfaces.get(905).isValid()
					&& inventory.getCount() < 1) {
				gui.comboBox1.setSelectedItem("String");
			}
		} else if (fletched >= amount && amount != 0 && !isBusy()
				&& !fletchAndString) {
			log("Fletched amount logging out!");
			stopScript();
		}
		if (isBusy() && !interfaces.get(740).isValid()) {
			antiban();
			sleep(random(200, 250));
		}
		clickContinue();
		if (gui.checkBox5.isSelected()) {
			checkfor99();
		}
	}

	private void string() {
		amount = Integer.parseInt(gui.textField1.getText());
		if (!inventory.contains(getUnstrungId()) || !inventory.contains(1777)
				&& amount == 0 && !isBusy() && !interfaces.get(905).isValid()) {
			withdrawStrings();
			sleep(random(200, 250));
		} else if (!inventory.contains(getUnstrungId())
				|| !inventory.contains(1777) && strung <= amount && !isBusy()
				&& !interfaces.get(905).isValid()) {
			withdrawStrings();
			sleep(random(200, 250));
		} else if (strung >= amount && amount != 0) {
			log("strung the chosen amount of bows!");
			stopScript();
		}
		if (inventory.contains(getUnstrungId()) && inventory.contains(1777)
				&& amount == 0 && !isBusy()) {
			stringBows();
			sleep(random(200, 250));
		} else if (inventory.contains(getUnstrungId())
				&& inventory.contains(1777) && strung <= amount && !isBusy()) {
			stringBows();
			sleep(random(200, 250));
		} else if (strung >= amount && amount != 0) {
			log("strung the chosen amount of bows!");
			stopScript();
		}

		if (isBusy() && !interfaces.get(740).isValid()
				&& inventory.contains(1777)
				&& inventory.contains(getUnstrungId())) {
			antiban();
			sleep(random(400, 500));
		}
		clickContinue();
		if (gui.checkBox5.isSelected()) {
			checkfor99();
		}
	}

	private void fletchAndString() {
		fletchAndString = true;
		gui.comboBox1.setSelectedItem("Fletch");
	}

	private void cfd() {
		amount = Integer.parseInt(gui.textField1.getText());
		if (!inventory.contains(getAxeId())
				|| !inventory.contains(getKnifeId())) {
			log("Get a axe and knife before starting");
			log("If you have the supplys...");
			log("select the right item in the gui!");
			log("Script stopping");
			sleep(2000);
			stopScript(false);
			sleep(500);
		}
		if (amount == 0 && !isBusy() && !interfaces.get(905).isValid()) {
			chopLogs();
		} else if (fletched <= amount && !isBusy()
				&& !interfaces.get(905).isValid()) {
			chopLogs();
		} else if (fletched >= amount && amount != 0) {
			log("Done the amount required!");
			stopScript();
		}

		if (inventory.contains(getLogId())
				&& inventory.containsOneOf(getKnifeId()) && amount == 0
				&& !isBusy() && inventory.isFull()) {
			if (getBowType() == 1 || getBowType() == 2) {
				fletchLogs();
				full = 0;
			} else if (getBowType() == 3) {
				fletchShafts();
				full = 0;
			} else if (getBowType() == 4) {
				fletchStocks();
				full = 0;
			}
		} else if (inventory.contains(getLogId()) && fletched <= amount
				&& !isBusy() && inventory.isFull()) {
			if (getBowType() == 1 || getBowType() == 2) {
				fletchLogs();
				full = 0;
			} else if (getBowType() == 3) {
				fletchShafts();
				full = 0;
			} else if (getBowType() == 4) {
				fletchStocks();
				full = 0;
			}
		} else if (fletched >= amount && amount != 0) {
			log("Done the amount required!");
			stopScript();
		}

		if (inventory.contains(getUnstrungId())
				&& inventory.containsOneOf(getKnifeId()) && amount == 0
				&& !isBusy() && inventory.isFull() && getBowType() != 3) {
			drop();
		} else if (inventory.contains(getUnstrungId()) && fletched <= amount
				&& !isBusy() && inventory.isFull()) {
			drop();
		} else if (fletched >= amount && amount != 0 && getBowType() != 3) {
			log("Done the amount required!");
			stopScript();
		}
		while (isBusy() && !interfaces.get(740).isValid()) {
			antiban();
			sleep(random(200, 250));
		}
		clickContinue();
		if (gui.checkBox5.isSelected()) {
			checkfor99();
		}
	}

	private void clickContinue() {
		if (interfaces.get(740).isValid()) {
			status = "Level up: Clicking Continue";
			sleep(50, 75);
			if (gui.checkBox2.isSelected()) {
				env.saveScreenshot(true);
			}
			if (gui.checkBox3.isSelected()
					&& skills.getRealLevel(Skills.FLETCHING) == 99 && !has99) {
				log("If you have 99 already, Disable at 99 for screenshots!");
				env.saveScreenshot(true);
				has99 = true;
			}
			trayInfo.systray.displayMessage("Level UP", "You are now level: "
					+ skills.getCurrentLevel(Skills.FLETCHING),
					TrayIcon.MessageType.INFO);
			sleep(150, 1500);
			interfaces.get(740).getComponent(3).doClick(true);
			sleep(150, 400);
		}
	}

	private void checkfor99() {
		currentexp = skills.getCurrentExp(Skills.FLETCHING);
		if (currentexp >= xpIsClose) {
			status = "Check 99: Logging out";
			if (bank.isOpen()) {
				bank.close();
			}
			stopScript(true);
		}
	}

	private void withdrawKnife() {
		status = "Banking: Knife";
		try {
			sleep(10, 20);
			openBank();
			sleep(200, 400);
			if (bank.isOpen()) {
				sleep(100, 250);
				if (!inventory.contains(getKnifeId())) {
					if (inventory.getCount() > 0)
						bank.depositAll();
					sleep(100, 150);
					if (getMethod() != 3) {
						if (bank.getItem(getKnifeId()) == null) {
							log("could not find a knife, logging out!");
							stopScript();
						}
					}
					bank.withdraw(getKnifeId(), 1);
					sleep(50, 100);
				}
			}
		} catch (Exception e) {
		}
	}

	private void withdrawLogs() {
		status = "Banking: Logs";
		try {
			sleep(10, 20);
			if (openBank()) {
				if (bank.isOpen()) {
					sleep(200, 400);
					if (bank.depositAllExcept(getKnifeId())) {
						for (int i = 0; i < 10; i++) {
							sleep(30);
							if (inventory.getCount() == 0) {
								break;
							}
						}
					}
					if (bank.getItem(getLogId()) == null) {
						if (fletchAndString) {
							gui.comboBox1.setSelectedItem("String");
						} else {
							log("could not find any Logs, logging out!");
							stopScript();
						}
					}
					bank.withdraw(getLogId(), 0);
					sleep(50, 100);
					for (int i = 0; i < 25; i++) {
						sleep(50);
						if (inventory.contains(getLogId())) {
							break;
						}
					}
				}
			}
		} catch (Exception e) {
		}
	}

	private void withdrawStrings() {
		status = "Banking: Stringing";
		try {
			if (!inventory.contains(1777)
					|| !inventory.contains(getUnstrungId())) {
				openBank();
				if (bank.isOpen()) {
					if (inventory.getCount() > 0) {
						bank.depositAll();
						sleep(50);
						for (int i = 0; i < 20; i++) {
							sleep(25);
							if (inventory.getCount() == 0) {
								break;
							}
						}
					}
					if (!inventory.contains(getUnstrungId())) {
						if (bank.getCount(getUnstrungId()) > 0) {
							bank.withdraw(getUnstrungId(), 14);
							sleep(100);
							for (int i = 0; i < 50; i++) {
								sleep(25);
								if (inventory.contains(getUnstrungId())) {
									break;
								}
							}
						} else {
							log("No more bows (u) in bank.");
							stopScript(true);
						}
					}
					if (!inventory.contains(getUnstrungId())) {
						withdrawStrings();
					}
					if (!inventory.contains(1777)) {
						if (bank.getCount(1777) > 0) {
							bank.withdraw(1777, 0);
							sleep(100);
							for (int i = 0; i < 50; i++) {
								sleep(25);
								if (inventory.contains(1777)) {
									break;
								}
							}
						} else {
							log("No more bowstring in bank.");
							stopScript(true);
						}
					}
					if (inventory.getCount(1777) > 14
							|| inventory.getCount(getUnstrungId()) > 14) {
						withdrawStrings();
					}
					if (inventory.getCount(1777) < 14
							|| inventory.getCount(getUnstrungId()) < 14) {
						withdrawStrings();
					}
				}
			}
		} catch (Exception e) {
		}
	}

	private void withdrawShafts() {
		status = "Banking: Shafts";
		try {
			sleep(10, 20);
			if (openBank()) {
				if (getLogId() != 1511) {
					log("Please select normal logs!");
					stopScript();
				} else if (getLogId() == 1511 && bank.isOpen()) {
					sleep(200, 400);
					bank.depositAllExcept(getKnifeId());
					sleep(100, 150);
					if (bank.getItem(getLogId()) == null) {
						log("Out of logs, Logging out!");
						stopScript();
					}
					bank.withdraw(getLogId(), 0);
					for (int i = 0; i < 200; i++) {
						sleep(50);
						if (inventory.contains(getLogId())) {
							bank.close();
							break;
						}
					}
					sleep(30, 50);
				}
			}
		} catch (Exception e) {
		}
	}

	private void withdrawStocks() {
		status = "Banking: Stocks";
		try {
			sleep(10, 20);
			if (openBank()) {
				if (getLogId() == 1513) {
					log("Please select a different log!");
					stopScript();
				} else if (getLogId() != 1513 && bank.isOpen()) {
					sleep(200, 400);
					bank.depositAllExcept(getKnifeId());
					sleep(100, 150);
					if (bank.getItem(getLogId()) == null) {
						log("Out of logs, Logging out!");
						stopScript();
					}
					bank.withdraw(getLogId(), 0);
					for (int i = 0; i < 200; i++) {
						sleep(50);
						if (inventory.contains(getLogId())) {
							bank.close();
							break;
						}
					}
					sleep(30, 50);
				}
			}
		} catch (Exception e) {
		}
	}

	private void fletchLogs() {
		status = "Fletching: UBows";
		try {
			if (bank.isOpen()) {
				bank.close();
			}
			sleep(50, 100);
			if (!interfaces.get(905).isValid() && !isBusy()
					&& inventory.containsOneOf(getKnifeId())) {
				if (random(1, 2) == 1) {
					inventory.getItem(getLogId()).doClick(true);
					sleep(200, 400);
					inventory.getItem(getKnifeId()).doClick(true);
				} else {
					inventory.getItem(getKnifeId()).doClick(true);
					sleep(200, 400);
					inventory.getItem(getLogId()).doClick(true);
				}
			}
			sleep(50, 100);
			mouse.move(random(35, 448), random(500, 355));
			sleep(400, 450);
			if (interfaces.get(905).isValid()) {
				if (getBowType() == 1) {
					status = "Fletching: short";
					if (getLogId() == 1511) {
						sleep(200, 250);
						interfaces.get(905).getComponent(15)
								.doAction("Make All");
					} else {
						sleep(200, 250);
						interfaces.get(905).getComponent(14)
								.doAction("Make All");
					}
				}
				if (getBowType() == 2) {
					status = "Fletching: long";
					if (getLogId() == 1511) {
						sleep(200, 250);
						interfaces.get(905).getComponent(16)
								.doAction("Make All");
					} else {
						sleep(200, 250);
						interfaces.get(905).getComponent(15)
								.doAction("Make All");
					}
				}
			}
			sleep(50, 200);
		} catch (Exception e) {
		}
	}

	private void stringBows() {
		status = "Stringing: Bows";
		try {
			if (bank.isOpen()) {
				bank.close();
			}
			if (!interfaces.get(905).isValid() && !isBusy()
					&& inventory.contains(1777)
					&& inventory.contains(getUnstrungId())) {
				if (random(1, 2) == 1) {
					inventory.getItem(getUnstrungId()).doClick(true);
					sleep(200, 400);
					inventory.getItem(1777).doClick(true);
					sleep(random(200, 400));
				} else {
					inventory.getItem(1777).doClick(true);
					sleep(200, 400);
					inventory.getItem(getUnstrungId()).doClick(true);
					sleep(random(200, 400));
				}
			}
			sleep(50, 100);
			mouse.moveRandomly(150, 500);
			sleep(400, 450);
			if (interfaces.get(905).isValid()) {
				sleep(200, 250);
				interfaces.get(905).getComponent(14).doAction("Make All");
				sleep(50, 200);
			}
		} catch (Exception e) {
		}
	}

	private void fletchShafts() {
		status = "Fletching: Shafts";
		try {
			if (bank.isOpen())
				bank.close();
			sleep(50, 100);
			if (!interfaces.get(905).isValid() && !isBusy()) {
				if (random(1, 2) == 1) {
					inventory.getItem(getLogId()).doClick(true);
					sleep(200, 400);
					inventory.getItem(getKnifeId()).doClick(true);
				} else {
					inventory.getItem(getKnifeId()).doClick(true);
					sleep(200, 400);
					inventory.getItem(getLogId()).doClick(true);
				}
			}
			sleep(50, 100);
			mouse.moveRandomly(150, 500);
			sleep(400, 450);
			if (interfaces.get(905).isValid()) {
				if (getLogId() == 1511) {
					sleep(200, 250);
					interfaces.get(905).getComponent(14).doClick(true);
				} else if (getLogId() != 1511) {
					log("Please select normal logs!");
					stopScript();
				}
			}
			sleep(50, 200);
		} catch (Exception e) {
		}
	}

	private void fletchStocks() {
		status = "Fletching: Stocks";
		try {
			if (bank.isOpen())
				bank.close();
			sleep(50, 100);
			if (!interfaces.get(905).isValid() && !isBusy()) {
				if (random(1, 2) == 1) {
					inventory.getItem(getLogId()).doClick(true);
					sleep(200, 400);
					inventory.getItem(getKnifeId()).doClick(true);
				} else {
					inventory.getItem(getKnifeId()).doClick(true);
					sleep(200, 400);
					inventory.getItem(getLogId()).doClick(true);
				}
			}
			sleep(50, 100);
			mouse.moveRandomly(150, 500);
			sleep(400, 450);
			if (interfaces.get(905).isValid()) {
				if (getLogId() == 1513) {
					log("Please slect a different log!");
					stopScript();
				} else if (getLogId() != 1513) {
					if (getLogId() == 1511) {
						sleep(200, 250);
						interfaces.get(905).getComponent(17).doClick(true);
					} else {
						sleep(200, 250);
						interfaces.get(905).getComponent(17).doClick(true);
					}
				}
			}
			sleep(50, 200);
		} catch (Exception e) {
		}
	}

	private void chopLogs() {
		walk();
		status = "Chop: Logs";
		if (objects.getNearest(getTreeId()) != null
				&& getMyPlayer().getAnimation() == -1
				&& !getMyPlayer().isMoving()) {
			if (objects.getNearest(getTreeId()) != null
					&& !isBusy()
					&& calc.tileOnScreen(objects.getNearest(getTreeId())
							.getLocation())) {
				objects.getNearest(getTreeId()).doAction("Chop");
				sleep(random(1000, 1250));
				if (full > 5) {
					log("Inventory was to full, error!");
					log("Clearing out inventory!");
					full = 0;
					drop();
				}
				if (fail > 3) {
					status = "Fail: getting new tree.";
					walking.walkTileMM(getMyPlayer().getLocation().randomize(
							10, 10));
					walk();
					fail = 0;
				}
				inventory.dropAllExcept(getAxeId(), getKnifeId(), 52,
						getLogId(), 15544, 15545);
			}
		}
	}

	private void drop() {
		status = "Drop: Fletched items";
		inventory.dropAllExcept(getAxeId(), getKnifeId(), getLogId(), 15544,
				15545, 52);
	}

	@SuppressWarnings("deprecation")
	public void walk() {
		status = "Walking";
		if (objects.getNearest(getTreeId()) != null
				&& getMyPlayer().getAnimation() == -1
				&& !getMyPlayer().isMoving()) {
			if (objects.getNearest(getTreeId()) != null && !isBusy()) {
				camera.setPitch(random(90, 100));
				path = walking.findPath(objects.getNearest(getTreeId())
						.getLocation().randomize(3, 3));
				walking.newTilePath(path).traverse();
				sleep(random(600, 650));
				while (getMyPlayer().isMoving()) {
					sleep(random(150, 250));
				}
			}
		} else if (objects.getNearest(getTreeId()) == null) {
			log("Tree out of reach, please start closer to the tree!");
			stopScript();
		}
	}

	@Override
	public void messageReceived(MessageEvent e) {
		try {
			String m = e.getMessage().toLowerCase();
			int person = e.getID();
			if (m.contains("you carefully cut")
					&& person == MessageEvent.MESSAGE_ACTION) {
				fletched++;
			}
			if (m.contains("you add a string to the bow")
					&& person == MessageEvent.MESSAGE_ACTION) {
				strung++;
			}
			if (m.contains("you can't reach that")
					&& person == MessageEvent.MESSAGE_ACTION) {
				fail++;
			}
			if (m.contains("your inventory is too full")
					&& person == MessageEvent.MESSAGE_ACTION) {
				fail++;
				full++;
			}
			if (m.contains("you need a")
					&& person == MessageEvent.MESSAGE_ACTION) {
				log("not high enough level! Stopping!");
				stopScript(true);
			}
			if (gui.checkBox15.isSelected()) {
				if (person == MessageEvent.MESSAGE_CHAT
						|| person == MessageEvent.MESSAGE_CLAN_CHAT
						|| person == MessageEvent.MESSAGE_PRIVATE_IN) {
					trayInfo.systray.displayMessage(e.getSender() + ":",
							e.getMessage(), TrayIcon.MessageType.WARNING);
				}
			}
		} catch (Exception e1) {
		}
	}

	private void antiban() {
		status = "Antiban:";
		int r = random(1, 200);
		if (r == 1) {
			status = "Antiban: Mouse";
			mouse.moveRandomly(100, 200);
			sleep(random(2000, 2500));
		}
		if (r == 6) {
			status = "Antiban: Mouse";
			mouse.moveRandomly(25, 150);
			sleep(random(1000, 2500));
		}
		if (r == 12) {
			status = "Antiban: Stats";
			if (game.getCurrentTab() != 1) {
				game.openTab(1);
				sleep(350, 500);
				mouse.move(random(615, 665), random(350, 375));
				sleep(1000, 1200);
				if (game.getCurrentTab() != 4) {
					game.openTab(4);
					sleep(random(100, 200));
				}
			}
		}
		if (r == 19) {
			status = "Antiban: AFK";
			sleep(random(2000, 2500));
		}
		if (r == 26) {
			status = "Antiban: Camera";

			camera.setAngle(random(0, 300));
			camera.setPitch(random(35, 85));
			sleep(random(1750, 1950));
		}
	}

	private boolean checkForUpdates() {
		try {
			JOptionPane.showMessageDialog(null, "Checking for updates!");
			double currentVer = UFletch.class.getAnnotation(
					ScriptManifest.class).version();
			double newVer = -1;
			URL url = new URL(constants.UPDATER_URL);
			BufferedReader in = new BufferedReader(new InputStreamReader(
					url.openStream()));
			String line, lines = "";
			Matcher m;
			while ((line = in.readLine()) != null) {
				lines += line + "\n";
				if ((m = constants.UPDATER_VERSION_PATTERN.matcher(line))
						.find()) {
					newVer = Double.parseDouble(m.group(1));
					break;
				}
			}
			if (newVer < 0) {
				in.close();
				log("Unable to find the new version number. Update failed");
				return false;
			}
			if (currentVer >= newVer) {
				in.close();
				log("You already have the latest version of the script.");
				JOptionPane.showMessageDialog(null,
						"You already have the latest version of the script.");
				return false;
			}
			String pick = JOptionPane.showInputDialog(null,
					"Update found! Type yes to download!");
			if (pick.contains("yes")) {
				log("Update found! Downloading version " + newVer);
				String scriptFilePath = GlobalConfiguration.Paths
						.getScriptsSourcesDirectory()
						+ "\\"
						+ constants.UPDATER_FILE_NAME;
				PrintWriter out = new PrintWriter(scriptFilePath);
				out.print(lines);
				while ((line = in.readLine()) != null)
					out.println(line);
				out.close();
				in.close();
				JOptionPane
						.showMessageDialog(null,
								"Update Downloaded successfully! Attempting to compile!");
				log("Successfully saved "
						+ constants.UPDATER_FILE_NAME
						+ " to "
						+ GlobalConfiguration.Paths
								.getScriptsSourcesDirectory());
				log("Compiling...");
				BufferedReader pathfile = new BufferedReader(new FileReader(
						GlobalConfiguration.Paths.getSettingsDirectory() + "\\"
								+ "path.txt"));
				String path = pathfile.readLine();
				try {
					Runtime.getRuntime()
							.exec(new String[] { "javac", "-cp", path,
									scriptFilePath });
				} catch (Exception ex) {
					JOptionPane.showMessageDialog(null,
							"Error Compiling, please manually compile!");
					log("Could not compile the script. Please manually compile to finish the update.");
					stopScript();
					return false;
				}
				log("Update successful!");
				log("The new version will appear near the bottom of the script selector.");
				log("Stopping the script. restart to run the newer version.");
				stopScript();
				return true;
			}
		} catch (IOException e) {
			log(e.toString());
			log("Update failed.");
		}
		return false;
	}

	String urversion() {
		return Double.toString(version);
	}

	String newversion() {
		return Double.toString(newver);
	}

	private void getVersionNumbers() {
		try {
			version = getClass().getAnnotation(ScriptManifest.class).version();
			URL url = new URL(constants.UPDATER_URL);
			BufferedReader in = new BufferedReader(new InputStreamReader(
					url.openStream()));
			String line, lines = "";
			Matcher m;
			while ((line = in.readLine()) != null) {
				lines += line + "\n";
				if ((m = constants.UPDATER_VERSION_PATTERN.matcher(line))
						.find()) {
					newver = Double.parseDouble(m.group(1));
					break;
				}
			}
		} catch (Exception e) {
		}
	}

	private void pauseScript() {
		if (pause) {
			log("Pausing...");
			status = "Paused";
			while (pause) {
				sleep(400, 600);
			}
		}
	}

	public boolean onStart() {
		for (int i = 0; i < 80; i++) {
			sleep(80);
			if (game.isLoggedIn()) {
				break;
			}
		}
		sleep(random(400, 425));
		if (!game.isLoggedIn()) {
			JOptionPane.showMessageDialog(null, "Please completely login!");
			return false;
		}
		JOptionPane.showMessageDialog(null, "Please wait while gui loads.");
		getVersionNumbers();
		nameGUI = new ircNameGUI();
		gui = new gui();
		gui.setVisible(true);
		loadSettings();
		gui.checkBox16.setSelected(false);
		if (gui.checkBox7.isSelected()) {
			checkForUpdates();
		}
		if (gui.textField2.getText().equals("All")) {
			gui.textField2.setEnabled(true);
			gui.button4.setEnabled(true);
		} else {
			gui.textField2.setEnabled(false);
			gui.button4.setEnabled(false);
		}
		name = gui.textField2.getText();
		gui.label1
				.setText("<html><img src =http://universalscripts.org/UFletch_generate.php?user="
						+ name + "> </html>");
		while (gui.isVisible()) {
			sleep(random(200, 400));
		}
		nameGUI.setVisible(true);
		while (nameGUI.isVisible()) {
			sleep(random(200, 400));
		}
		if (nameGUI.checkBox1.isSelected()) {
			IRCgui = new IRCGui();
			doc = IRCgui.textArea1.getDocument();
			users = IRCgui.textArea2.getDocument();
			irc = new Irc();
			i = new Thread(irc);
			i.start();
			IRCgui.setVisible(true);
		}
		if (gui.checkBox16.isSelected()) {
			beep = new beeper();
			b = new Thread(beep);
			b.start();
		}
		getExtraInfo();
		trayInfo = new trayInfo();
		gui.checkBox16.setEnabled(false);
		sleep(random(50, 75));
		return true;
	}

	private void getExtraInfo() {
		invPaint = Images.getImage("ufletchpaint2.png");
		paint = Images.getImage("ufletchpaint.png");
		hide = Images.getImage("hidepaint.png");
		show = Images.getImage("showpaint.png");
		guiButton = Images.getImage("button.png");
		watermark = Images.getImage("watermark.png");
		icon = Images.getImage("icon.png");
		sleep(random(400, 500));
		startXP = skills.getCurrentExp(Skills.FLETCHING);
		sleep(random(100, 250));
	}

	private void createSignature() {
		try {
			URL url;
			URLConnection urlConn;
			url = new URL("http://www.universalscripts.org/UFletch_submit.php");
			urlConn = url.openConnection();
			urlConn.setRequestProperty("User-Agent", "hax");
			urlConn.setDoInput(true);
			urlConn.setDoOutput(true);
			urlConn.setUseCaches(false);
			urlConn.setRequestProperty("Content-Type",
					"application/x-www-form-urlencoded");
			String content = "";
			String[] stats = { "auth", "secs", "mins", "hours", "fletched",
					"strung", "expgained" };
			Object[] data = { gui.textField2.getText(), 0, 0, 0, 0, 0, 0 };
			for (int i = 0; i < stats.length; i++) {
				content += stats[i] + "=" + data[i] + "&";
			}
			content = content.substring(0, content.length() - 1);
			OutputStreamWriter wr = new OutputStreamWriter(
					urlConn.getOutputStream());
			wr.write(content);
			wr.flush();
			BufferedReader rd = new BufferedReader(new InputStreamReader(
					urlConn.getInputStream()));
			String line;
			while ((line = rd.readLine()) != null) {
				if (line.contains("success")) {
					log(line);
				}
			}
			wr.close();
			rd.close();
		} catch (Exception e) {
		}
	}

	private void updateSignature() {
		try {
			long xpGained = skills.getCurrentExp(Skills.FLETCHING) - startXP;
			long millis = System.currentTimeMillis() - startTime;
			long hours = millis / (1000 * 60 * 60);
			millis -= hours * (1000 * 60 * 60);
			long minutes = millis / (1000 * 60);
			millis -= minutes * (1000 * 60);
			long seconds = millis / 1000;
			URL url;
			URLConnection urlConn;
			url = new URL("http://www.universalscripts.org/UFletch_submit.php");
			urlConn = url.openConnection();
			urlConn.setRequestProperty("User-Agent", "hax");
			urlConn.setDoInput(true);
			urlConn.setDoOutput(true);
			urlConn.setUseCaches(false);
			urlConn.setRequestProperty("Content-Type",
					"application/x-www-form-urlencoded");
			String content = "";
			String[] stats = { "auth", "secs", "mins", "hours", "fletched",
					"strung", "expgained" };
			Object[] data = { gui.textField2.getText(), seconds, minutes,
					hours, fletched, strung, xpGained };
			for (int i = 0; i < stats.length; i++) {
				content += stats[i] + "=" + data[i] + "&";
			}
			content = content.substring(0, content.length() - 1);
			OutputStreamWriter wr = new OutputStreamWriter(
					urlConn.getOutputStream());
			wr.write(content);
			wr.flush();
			BufferedReader rd = new BufferedReader(new InputStreamReader(
					urlConn.getInputStream()));
			String line;
			while ((line = rd.readLine()) != null) {
				if (line.contains("success")) {
					log(line);
				}
			}
			wr.close();
			rd.close();
		} catch (Exception e) {
		}
	}

	@SuppressWarnings("deprecation")
	public void onFinish() {
		updateSignature();
		log.info("Thanks for using UFletch. Have a good one ;)");
		if (gui.checkBox1.isSelected()) {
			env.saveScreenshot(true);
		}
		SystemTray.getSystemTray().remove(trayInfo.systray);
		if (nameGUI.checkBox1.isSelected()) {
			irc.leave();
			i.interrupt();
			i.suspend();
			IRCgui.dispose();
		}
		if (gui.checkBox16.isSelected()) {
			b.interrupt();
			b.suspend();
		}
	}

	private static class Images {
		private static Logger log = Logger.getLogger(Images.class.getName());

		public static Image getImage(String fileName) {
			try {
				File loc = new File(
						GlobalConfiguration.Paths.getScriptsDirectory()
								+ "/Paint_Images/");
				File f = new File(
						GlobalConfiguration.Paths.getScriptsDirectory()
								+ "/Paint_Images/" + fileName);
				BufferedImage img = ImageIO.read(new URL(
						"http://dl.dropbox.com/u/23938245/Scripts/UFletch/Images/"
								+ fileName));
				if (!loc.exists()) {
					loc.mkdir();
				}
				if (loc.exists()) {
					if (f.exists()) {
						log.info("Successfully loaded " + fileName
								+ " from scripts folder.");
						return ImageIO.read(f.toURI().toURL());
					}
				}
				if (loc.exists()) {
					if (img != null) {
						log.info("Downlaoding images...");
						ImageIO.write((RenderedImage) img, "PNG", f);
						log.info("Saved " + fileName
								+ " to Scripts folder successfully.");
						return img;
					}
				}
			} catch (IOException e) {
				log.info("No Internet Connection or Broken Image Link, Check for Script Update.");
			}
			return null;
		}

	}

	@SuppressWarnings("serial")
	private class MousePathPoint extends Point {
		private int toColor(double d) {
			return Math.min(255, Math.max(0, (int) d));
		}

		private long finishTime;
		private double lastingTime;

		public MousePathPoint(int x, int y, int lastingTime) {
			super(x, y);
			this.lastingTime = lastingTime;
			finishTime = System.currentTimeMillis() + lastingTime;
		}

		public boolean isUp() {
			return System.currentTimeMillis() > finishTime;
		}

		public Color getColor() {
			return new Color(
					getColorRSBotLine().getRed(),
					getColorRSBotLine().getGreen(),
					getColorRSBotLine().getBlue(),
					toColor(256 * ((finishTime - System.currentTimeMillis()) / lastingTime)));
		}
	}

	private class MousePathPoint2 extends Point {
		/**
		 * 
		 */
		private static final long serialVersionUID = 3567008140194371837L;

		private int toColor(double d) {
			return Math.min(255, Math.max(0, (int) d));
		}

		private long finishTime;
		private double lastingTime;

		public MousePathPoint2(int x, int y, int lastingTime) {
			super(z.x, z.y);
			this.lastingTime = lastingTime;
			finishTime = System.currentTimeMillis() + lastingTime;
		}

		public boolean isUp2() {
			return System.currentTimeMillis() > finishTime;
		}

		public Color getColor2() {
			return new Color(
					getColorUserLine().getRed(),
					getColorUserLine().getGreen(),
					getColorUserLine().getBlue(),
					toColor(256 * ((finishTime - System.currentTimeMillis()) / lastingTime)));
		}
	}

	private class MouseCirclePathPoint extends Point {
		private static final long serialVersionUID = 1L;

		private int toColor(double d) {
			return Math.min(255, Math.max(0, (int) d));
		}

		private long finishTime;
		private double lastingTime;

		public MouseCirclePathPoint(int x, int y, int lastingTime) {
			super(x, y);
			this.lastingTime = lastingTime;
			finishTime = System.currentTimeMillis() + lastingTime;
		}

		public boolean isUp() {
			return System.currentTimeMillis() > finishTime;
		}
	}

	private class MouseCirclePathPoint2 extends Point {
		private static final long serialVersionUID = 1L;

		private int toColor(double d) {
			return Math.min(255, Math.max(0, (int) d));
		}

		private long finishTime;
		private double lastingTime;

		public MouseCirclePathPoint2(int x, int y, int lastingTime) {
			super(x, y);
			this.lastingTime = lastingTime;
			finishTime = System.currentTimeMillis() + lastingTime;
		}

		public boolean isUp() {
			return System.currentTimeMillis() > finishTime;
		}
	}

	private final RenderingHints rh = new RenderingHints(
			RenderingHints.KEY_TEXT_ANTIALIASING,
			RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

	public void onRepaint(Graphics g) {
		if (gui.checkBox4.isSelected()) {
			Graphics2D g2 = (Graphics2D) g;
			g2.setRenderingHints(rh);
			long millis = System.currentTimeMillis() - startTime;
			long hours = millis / (1000 * 60 * 60);
			millis -= hours * (1000 * 60 * 60);
			long minutes = millis / (1000 * 60);
			millis -= minutes * (1000 * 60);
			long seconds = millis / 1000;
			xpGained = skills.getCurrentExp(Skills.FLETCHING) - startXP;
			xpToLevel = skills.getExpToNextLevel(Skills.FLETCHING);
			float xpsec = ((float) xpGained)
					/ (float) (seconds + (minutes * 60) + (hours * 60 * 60));
			float xpmin = xpsec * 60;
			float xphour = xpmin * 60;
			if (xpGained > 0) {
				hoursTNL = (int) Math.floor(xpToLevel / xphour);
				minsTNL = (int) Math
						.floor(((xpToLevel / xphour) - hoursTNL) * 60);
			}

			// =========> Chat Paint <=========
			if (fullPaint && !isClicking && game.isLoggedIn()
					&& gui.checkBox8.isSelected()) {
				g2.setColor(getColorPaint());
				g2.fillRect(6, 344, 507, 129);
				g2.drawImage(paint, 6, 344, null);
				g2.drawImage(hide, 9, 347, null);
				g2.drawImage(guiButton, 360, 440, null);
				g2.drawImage(watermark, 305, 315, null);
				g2.setFont(new Font("Arial", 1, 15));
				g2.setColor(getColorText());
				g2.drawString("Time Running: " + getRuntime(), 60, 372);
				g2.drawString(
						"Exp Gained: "
								+ (skills.getCurrentExp(Skills.FLETCHING) - startXP),
						60, 391);
				g2.drawString(
						"Exp/H: "
								+ getHourly(skills
										.getCurrentExp(Skills.FLETCHING)
										- startXP), 60, 409);
				if (getMethod() != 2) {
					g2.drawString("Fletched/Strung:  " + fletched, 60, 426);
					g2.drawString(
							"Fletched/Hr:  "
									+ ((int) (new Double(fletched)
											/ new Double(System
													.currentTimeMillis()
													- startTime) * new Double(
											60 * 60 * 1000))), 60, 444);
				} else if (getMethod() == 2) {
					g2.drawString("Fletched/Strung:  " + strung, 60, 426);
					g2.drawString(
							"Fletched/Hr:  "
									+ ((int) (new Double(strung)
											/ new Double(System
													.currentTimeMillis()
													- startTime) * new Double(
											60 * 60 * 1000))), 60, 444);
				}
				g2.drawString("Status:  " + status, 60, 466);
				g2.setFont(new Font("Arial", 1, 10));
				g2.drawString("Hide Permanitly (Click to Fade)", 30, 355);
				g2.setFont(new Font("Arial", 1, 15));
			} else if (!fullPaint && isClicking && game.isLoggedIn()
					&& gui.checkBox8.isSelected()) {
				g2.drawImage(show, 9, 347, null);
				g2.drawImage(watermark, 305, 315, null);
			} else if (fullPaint && isClicking && gui.checkBox8.isSelected()) {
				g2.setColor(new Color(getColorPaint().getRed(), getColorPaint()
						.getGreen(), getColorPaint().getBlue(), 127));
				g2.fillRect(6, 344, 507, 129);
				g2.drawImage(paint, 6, 344, null);
				g2.drawImage(hide, 9, 347, null);
				g2.drawImage(guiButton, 360, 440, null);
				g2.drawImage(watermark, 305, 315, null);
				g2.setFont(new Font("Arial", 1, 15));
				g2.setColor(new Color(getColorText().getRed(), getColorText()
						.getGreen(), getColorText().getBlue(), 127));
				g2.drawString("Time Running: " + getRuntime(), 60, 372);
				g2.drawString(
						"Exp Gained: "
								+ (skills.getCurrentExp(Skills.FLETCHING) - startXP),
						60, 391);
				g2.drawString(
						"Exp/H: "
								+ getHourly(skills
										.getCurrentExp(Skills.FLETCHING)
										- startXP), 60, 409);
				g2.drawString("Fletched/Strung:  " + fletched, 60, 426);
				g2.drawString(
						"Fletched/Hr:  "
								+ ((int) (new Double(fletched)
										/ new Double(System.currentTimeMillis()
												- startTime) * new Double(
										60 * 60 * 1000))), 60, 444);
				g2.drawString("Status:  " + status, 60, 466);
				g2.setFont(new Font("Arial", 1, 10));
				g2.drawString("Hide Permanitly (Click to Fade)", 30, 355);
				g2.setFont(new Font("Arial", 1, 15));
			} else if (gui.checkBox8.isSelected()) {
				g2.drawImage(show, 9, 347, null);
				g2.drawImage(watermark, 305, 315, null);
			}

			// ============> Inv Paint <============
			if (fullPaint && !isClicking && game.isLoggedIn()
					&& gui.checkBox9.isSelected()) {
				g2.setColor(getColorPaint());
				g2.fillRoundRect(547, 203, 189, 264, 16, 16);
				g2.setColor(Color.BLACK);
				g2.drawRoundRect(547, 203, 189, 264, 16, 16);
				g2.drawImage(invPaint, 549, 206, null);
				g2.drawImage(guiButton, 606, 283, null);
				g2.drawImage(hide, 719, 451, null);
				g2.drawImage(watermark, 305, 315, null);
				g2.setColor(getColorText());
				g2.setFont(new Font("Arial", 1, 13));
				g2.drawString("Time Running: " + getRuntime(), 560, 330);
				g2.drawString(
						"Exp Gained: "
								+ (skills.getCurrentExp(Skills.FLETCHING) - startXP),
						560, 350);
				g2.drawString(
						"Exp/H: "
								+ getHourly(skills
										.getCurrentExp(Skills.FLETCHING)
										- startXP), 560, 370);
				g2.drawString("Fletched/Strung:  " + fletched, 560, 390);
				g2.drawString(
						"Fletched/Hr:  "
								+ ((int) (new Double(fletched)
										/ new Double(System.currentTimeMillis()
												- startTime) * new Double(
										60 * 60 * 1000))), 560, 410);
				g2.drawString("Status:  " + status, 560, 430);
				g2.setFont(new Font("Arial", 1, 10));
				g2.drawString("(Click to Fade) Hide Perminatly:", 560, 460);
				g2.setFont(new Font("Arial", 1, 13));
			} else if (!fullPaint && isClicking && game.isLoggedIn()
					&& gui.checkBox9.isSelected()) {
				g2.drawImage(show, 719, 451, null);
				g2.drawImage(watermark, 305, 315, null);
			} else if (fullPaint && isClicking && gui.checkBox9.isSelected()) {
				g2.setColor(new Color(getColorPaint().getRed(), getColorPaint()
						.getGreen(), getColorPaint().getBlue(), 127));
				g2.fillRoundRect(547, 203, 189, 264, 16, 16);
				g2.setColor(Color.BLACK);
				g2.drawRoundRect(547, 203, 189, 264, 16, 16);
				g2.drawImage(invPaint, 549, 206, null);
				g2.drawImage(invPaint, 549, 206, null);
				g2.drawImage(guiButton, 606, 283, null);
				g2.drawImage(watermark, 305, 315, null);
				g2.setColor(new Color(getColorText().getRed(), getColorText()
						.getGreen(), getColorText().getBlue(), 127));
				g2.setFont(new Font("Arial", 1, 13));
				g2.drawString("Time Running: " + getRuntime(), 560, 330);
				g2.drawString(
						"Exp Gained: "
								+ (skills.getCurrentExp(Skills.FLETCHING) - startXP),
						560, 350);
				g2.drawString(
						"Exp/H: "
								+ getHourly(skills
										.getCurrentExp(Skills.FLETCHING)
										- startXP), 560, 370);
				g2.drawString("Fletched/Strung:  " + fletched, 560, 390);
				g2.drawString(
						"Fletched/Hr:  "
								+ ((int) (new Double(fletched)
										/ new Double(System.currentTimeMillis()
												- startTime) * new Double(
										60 * 60 * 1000))), 560, 410);
				g2.drawString("Status:  " + status, 560, 430);
				g2.setFont(new Font("Arial", 1, 10));
				g2.drawString("(Click to Fade) Hide Perminatly:", 560, 461);
				g2.setFont(new Font("Arial", 1, 13));
			} else if (gui.checkBox9.isSelected()) {
				g2.drawImage(show, 719, 451, null);
				g2.drawImage(watermark, 305, 315, null);
			}

			if (game.isLoggedIn() && !isClicking && fullPaint
					&& gui.checkBox10.isSelected()) {
				// =========> PROGRESS <=========
				double percent = 512 * skills
						.getPercentToNextLevel(Skills.FLETCHING) / 100.0;
				GradientPaint base = new GradientPaint(4, 3, new Color(255,
						255, 255, 200), 4, 3 + 22 + 3,
						getColorProgressBarBelow());
				GradientPaint overlay = new GradientPaint(4, 3, new Color(255,
						255, 255, 200), 4, 3 + 22 + 3,
						getColorProgressBarOnTop());
				g2.setPaint(base);
				g2.fillRect(4, 3, 512, 22);
				g2.setPaint(overlay);
				g2.fillRect(4, 3, (int) percent, 22);
				g2.setColor(Color.black);
				g2.drawRect(4, 3, 512, 22);
				g2.setFont(new Font("Arial", 0, 13));
				String progress = skills
						.getPercentToNextLevel(Skills.FLETCHING)
						+ "% to "
						+ (skills.getCurrentLevel(Skills.FLETCHING) + 1)
						+ " Fletching | "
						+ skills.getExpToNextLevel(Skills.FLETCHING)
						+ "XP Until level | "
						+ hoursTNL
						+ " Hours, "
						+ minsTNL
						+ " Mins Until level";
				g2.setColor(getColorText());
				g2.drawString(progress, 12, 19);

			} else if (game.isLoggedIn() && isClicking && fullPaint
					&& gui.checkBox10.isSelected()) {
				double percent = 512 * skills
						.getPercentToNextLevel(Skills.FLETCHING) / 100.0;
				GradientPaint base = new GradientPaint(4, 3, new Color(200,
						200, 200, 100), 4, 3 + 22 + 3,
						getColorProgressBarBelow());
				GradientPaint overlay = new GradientPaint(4, 3, new Color(200,
						200, 200, 100), 4, 3 + 22 + 3,
						getColorProgressBarOnTop());
				g2.setPaint(base);
				g2.fillRect(4, 3, 512, 22);
				g2.setPaint(overlay);
				g2.fillRect(4, 3, (int) percent, 22);
				g2.setColor(Color.black);
				g2.drawRect(4, 3, 512, 22);
				g2.setFont(new Font("Arial", 0, 13));
				String progress = skills
						.getPercentToNextLevel(Skills.FLETCHING)
						+ "% to "
						+ (skills.getCurrentLevel(Skills.FLETCHING) + 1)
						+ " Fletching | "
						+ skills.getExpToNextLevel(Skills.FLETCHING)
						+ "XP Until level | "
						+ hoursTNL
						+ " Hours, "
						+ minsTNL
						+ " Mins Until level";
				g2.setColor(getColorText());
				g2.drawString(progress, 12, 19);
			}

			// ==========> MOUSE! <==========
			Point m = mouse.getLocation();
			g2.setColor(getColorText());
			g2.fillRect(m.x - 5, m.y, 12, 2);
			g2.fillRect(m.x, m.y - 5, 2, 12);
			if (gui.checkBox11.isSelected() && !gui.checkBox17.isSelected()) {
				while (!mousePath.isEmpty() && mousePath.peek().isUp())
					mousePath.remove();
				Point clientCursor = mouse.getLocation();
				MousePathPoint mpp = new MousePathPoint(clientCursor.x,
						clientCursor.y, 3000);
				if (mousePath.isEmpty() || !mousePath.getLast().equals(mpp))
					mousePath.add(mpp);
				MousePathPoint lastPoint = null;
				for (MousePathPoint a : mousePath) {
					if (lastPoint != null) {
						g2.setColor(a.getColor());
						g2.drawLine(a.x, a.y, lastPoint.x, lastPoint.y);
					}
					lastPoint = a;
				}
			} else if (gui.checkBox11.isSelected()
					&& gui.checkBox17.isSelected()) {
				while (!mouseCirclePath.isEmpty()
						&& mouseCirclePath.peek().isUp())
					mouseCirclePath.remove();
				MouseCirclePathPoint mp = new MouseCirclePathPoint(m.x, m.y,
						3000);
				if (mouseCirclePath.isEmpty()
						|| !mouseCirclePath.getLast().equals(mp))
					mouseCirclePath.add(mp);
				MouseCirclePathPoint lastPoint = null;
				for (MouseCirclePathPoint a : mouseCirclePath) {
					if (lastPoint != null) {
						g2.setColor(new Color(getColorRSBotLine().getRed(),
								getColorRSBotLine().getGreen(),
								getColorRSBotLine().getBlue(),
								a.toColor(156 * ((a.finishTime - System
										.currentTimeMillis()) / a.lastingTime))));
						g2.fillOval(
								a.x
										- a.toColor(15 * ((a.finishTime - System
												.currentTimeMillis()) / (a.lastingTime)))
										/ 2,
								a.y
										- a.toColor(15 * ((a.finishTime - System
												.currentTimeMillis()) / (a.lastingTime)))
										/ 2,
								a.toColor(15 * ((a.finishTime - System
										.currentTimeMillis()) / (a.lastingTime))),
								a.toColor(15 * ((a.finishTime - System
										.currentTimeMillis()) / (a.lastingTime))));
						g2.setColor(new Color(0, 0, 0, a
								.toColor(156 * ((a.finishTime - System
										.currentTimeMillis()) / a.lastingTime))));
						g2.drawOval(
								a.x
										- a.toColor(15 * ((a.finishTime - System
												.currentTimeMillis()) / (a.lastingTime)))
										/ 2,
								a.y
										- a.toColor(15 * ((a.finishTime - System
												.currentTimeMillis()) / (a.lastingTime)))
										/ 2,
								a.toColor(15 * ((a.finishTime - System
										.currentTimeMillis()) / (a.lastingTime))),
								a.toColor(15 * ((a.finishTime - System
										.currentTimeMillis()) / (a.lastingTime))));
					}
					lastPoint = a;
				}
			}

			if (gui.checkBox13.isSelected() && !gui.checkBox18.isSelected()) {
				while (!mousePath2.isEmpty() && mousePath2.peek().isUp2())
					mousePath2.remove();
				MousePathPoint2 mpp = new MousePathPoint2(z.x, z.y, 3000);
				if (mousePath2.isEmpty() || !mousePath2.getLast().equals(mpp))
					mousePath2.add(mpp);
				MousePathPoint2 lastPoint = null;
				for (MousePathPoint2 z : mousePath2) {
					if (lastPoint != null) {
						g2.setColor(z.getColor2());
						g2.drawLine(z.x, z.y, lastPoint.x, lastPoint.y);
					}
					lastPoint = z;
				}
			} else if (gui.checkBox13.isSelected()
					&& gui.checkBox18.isSelected()) {
				while (!mouseCirclePath2.isEmpty()
						&& mouseCirclePath2.peek().isUp())
					mouseCirclePath2.remove();
				MouseCirclePathPoint2 mp = new MouseCirclePathPoint2(z.x, z.y,
						3000);
				if (mouseCirclePath2.isEmpty()
						|| !mouseCirclePath2.getLast().equals(mp))
					mouseCirclePath2.add(mp);
				MouseCirclePathPoint2 lastPoint = null;
				for (MouseCirclePathPoint2 a : mouseCirclePath2) {
					if (lastPoint != null) {
						g2.setColor(new Color(getColorUserLine().getRed(),
								getColorUserLine().getGreen(),
								getColorUserLine().getBlue(),
								a.toColor(156 * ((a.finishTime - System
										.currentTimeMillis()) / a.lastingTime))));
						g2.fillOval(
								a.x
										- a.toColor(15 * ((a.finishTime - System
												.currentTimeMillis()) / (a.lastingTime)))
										/ 2,
								a.y
										- a.toColor(15 * ((a.finishTime - System
												.currentTimeMillis()) / (a.lastingTime)))
										/ 2,
								a.toColor(15 * ((a.finishTime - System
										.currentTimeMillis()) / (a.lastingTime))),
								a.toColor(15 * ((a.finishTime - System
										.currentTimeMillis()) / (a.lastingTime))));
						g2.setColor(new Color(0, 0, 0, a
								.toColor(156 * ((a.finishTime - System
										.currentTimeMillis()) / a.lastingTime))));
						g2.drawOval(
								a.x
										- a.toColor(15 * ((a.finishTime - System
												.currentTimeMillis()) / (a.lastingTime)))
										/ 2,
								a.y
										- a.toColor(15 * ((a.finishTime - System
												.currentTimeMillis()) / (a.lastingTime)))
										/ 2,
								a.toColor(15 * ((a.finishTime - System
										.currentTimeMillis()) / (a.lastingTime))),
								a.toColor(15 * ((a.finishTime - System
										.currentTimeMillis()) / (a.lastingTime))));
					}
					lastPoint = a;
				}
			}
			if (gui.checkBox12.isSelected()) {
				int gW = game.getWidth();
				int gH = game.getHeight();
				Point localPoint = mouse.getLocation();
				g2.setColor(getColorRSBotCrosshair());
				g2.drawLine(0, localPoint.y, gW, localPoint.y);
				g2.drawLine(localPoint.x, 0, localPoint.x, gH);
			}
			if (gui.checkBox14.isSelected()) {
				int gW = game.getWidth();
				int gH = game.getHeight();
				g2.setColor(getColorUserCrosshair());
				g2.drawLine(0, z.y, gW, z.y);
				g2.drawLine(z.x, 0, z.x, gH);
			}
		}
	}

	private int getHourly(final int input) {
		double millis = System.currentTimeMillis() - startTime;
		return (int) ((input / millis) * 3600000);
	}

	private String getRuntime() {
		try {
			long millis = System.currentTimeMillis() - startTime;
			long hours = millis / (1000 * 60 * 60);
			millis -= hours * (1000 * 60 * 60);
			long minutes = millis / (1000 * 60);
			millis -= minutes * (1000 * 60);
			long seconds = millis / 1000;
			return ("" + (hours < 10 ? "0" : "") + hours + ":"
					+ (minutes < 10 ? "0" : "") + minutes + ":"
					+ (seconds < 10 ? "0" : "") + seconds + "");
		} catch (Exception e) {
			return "";
		}
	}

	private int getValue(boolean b) {
		if (b)
			return 1;
		return 0;
	}

	private void loadSettings() {
		if (!new File(GlobalConfiguration.Paths.getHomeDirectory()
				+ File.separator + "Settings" + File.separator + "UFletch.ini")
				.exists()) {
			return;
		}
		try {
			DataInputStream in = new DataInputStream(new FileInputStream(
					GlobalConfiguration.Paths.getHomeDirectory()
							+ File.separator + "Settings" + File.separator
							+ "UFletch.ini"));
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			String line;
			while ((line = br.readLine()) != null) {
				try {
					boolean checkWhat = false;
					int w = -1;
					String isDoing = "";
					for (char c : line.toCharArray()) {
						if (c == '<' || checkWhat) {
							if (c == '<') {
								isDoing = "";
							}
							checkWhat = true;
							isDoing += c;
							if (c == '>') {
								if (isDoing.contains("<Method>")) {
									w = 1;
								} else if (isDoing.contains("<LogType>")) {
									w = 2;
								} else if (isDoing.contains("<BowType>")) {
									w = 3;
								} else if (isDoing.contains("<Knife>")) {
									w = 4;
								} else if (isDoing.contains("<AxeType>")) {
									w = 5;
								} else if (isDoing.contains("<Color1>")) {
									w = 6;
								} else if (isDoing.contains("<Color2>")) {
									w = 7;
								} else if (isDoing.contains("<Color3>")) {
									w = 8;
								} else if (isDoing.contains("<Color4>")) {
									w = 9;
								} else if (isDoing.contains("<Color5>")) {
									w = 10;
								} else if (isDoing.contains("<Color6>")) {
									w = 11;
								} else if (isDoing.contains("<Color7>")) {
									w = 12;
								} else if (isDoing.contains("<Color8>")) {
									w = 13;
								} else if (isDoing.contains("<Amount>")) {
									w = 14;
								} else if (isDoing.contains("<Name>")) {
									w = 15;
								} else if (isDoing.contains("<WhenDone>")) {
									w = 16;
								} else if (isDoing.contains("<UponLvl>")) {
									w = 17;
								} else if (isDoing.contains("<Hourly>")) {
									w = 18;
								} else if (isDoing.contains("<Before99>")) {
									w = 19;
								} else if (isDoing.contains("<Save>")) {
									w = 20;
								} else if (isDoing.contains("<Update>")) {
									w = 21;
								} else if (isDoing.contains("<Paint>")) {
									w = 22;
								} else if (isDoing.contains("<Chat>")) {
									w = 23;
								} else if (isDoing.contains("<Inventory>")) {
									w = 24;
								} else if (isDoing.contains("<Bar>")) {
									w = 25;
								} else if (isDoing.contains("<BotLine>")) {
									w = 26;
								} else if (isDoing.contains("<BotCross>")) {
									w = 27;
								} else if (isDoing.contains("<UserLine>")) {
									w = 28;
								} else if (isDoing.contains("<UserCross>")) {
									w = 29;
								} else if (isDoing.contains("<BotCircle>")) {
									w = 30;
								} else if (isDoing.contains("<UserCircle>")) {
									w = 31;
								} else if (isDoing.contains("<Message>")) {
									w = 32;
								} else if (isDoing.contains("<Beep>")) {
									w = 33;
								} else if (isDoing.contains("<IRCname>")) {
									w = 34;
								} else if (isDoing.contains("<Speed>")) {
									w = 35;
								}
								checkWhat = false;
								isDoing = "";
							}
							continue;
						} else if (w == 1 || w == 2 || w == 3 || w == 4
								|| w == 5 || w == 6 || w == 7 || w == 8
								|| w == 9 || w == 10 || w == 11 || w == 12
								|| w == 13 || w == 15 || w == 34) {
							if (c == '(') {
								isDoing = "";
							} else if (c == ',' || c == ')') {
								if (w == 1) {
									gui.comboBox1.setSelectedItem(isDoing);
								} else if (w == 2) {
									gui.comboBox2.setSelectedItem(isDoing);
								} else if (w == 3) {
									gui.comboBox3.setSelectedItem(isDoing);
								} else if (w == 4) {
									gui.comboBox4.setSelectedItem(isDoing);
								} else if (w == 5) {
									gui.comboBox5.setSelectedItem(isDoing);
								} else if (w == 6) {
									gui.comboBox12.setSelectedItem(isDoing);
								} else if (w == 7) {
									gui.comboBox13.setSelectedItem(isDoing);
								} else if (w == 8) {
									gui.comboBox8.setSelectedItem(isDoing);
								} else if (w == 9) {
									gui.comboBox9.setSelectedItem(isDoing);
								} else if (w == 10) {
									gui.comboBox10.setSelectedItem(isDoing);
								} else if (w == 11) {
									gui.comboBox11.setSelectedItem(isDoing);
								} else if (w == 12) {
									gui.comboBox14.setSelectedItem(isDoing);
								} else if (w == 13) {
									gui.comboBox15.setSelectedItem(isDoing);
								} else if (w == 15) {
									gui.textField2.setText(isDoing);
								} else if (w == 34) {
									nameGUI.textField1.setText(isDoing);
								}
							} else {
								isDoing += c;
							}
						} else {
							if (c == '(') {
								isDoing = "";
							} else if (c == ',' || c == ')') {
								int tempID = Integer.parseInt(isDoing);
								boolean val = false;
								if (tempID == 1) {
									val = true;
								}
								if (w == 14) {
									gui.textField1.setText(isDoing);
								} else if (w == 16) {
									gui.checkBox1.setSelected(val);
								} else if (w == 17) {
									gui.checkBox2.setSelected(val);
								} else if (w == 18) {
									gui.checkBox3.setSelected(val);
								} else if (w == 19) {
									gui.checkBox5.setSelected(val);
								} else if (w == 20) {
									gui.checkBox6.setSelected(val);
								} else if (w == 21) {
									gui.checkBox7.setSelected(val);
								} else if (w == 22) {
									gui.checkBox4.setSelected(val);
								} else if (w == 23) {
									gui.checkBox8.setSelected(val);
								} else if (w == 24) {
									gui.checkBox9.setSelected(val);
								} else if (w == 25) {
									gui.checkBox10.setSelected(val);
								} else if (w == 26) {
									gui.checkBox11.setSelected(val);
								} else if (w == 27) {
									gui.checkBox12.setSelected(val);
								} else if (w == 28) {
									gui.checkBox13.setSelected(val);
								} else if (w == 29) {
									gui.checkBox14.setSelected(val);
								} else if (w == 30) {
									gui.checkBox17.setSelected(val);
								} else if (w == 31) {
									gui.checkBox18.setSelected(val);
								} else if (w == 32) {
									gui.checkBox15.setSelected(val);
								} else if (w == 33) {
									gui.checkBox16.setSelected(val);
								} else if (w == 35) {
									gui.slider1.setValue(tempID);
								}
								isDoing = "";
							} else if (c == '0' || c == '1' || c == '2'
									|| c == '3' || c == '4' || c == '5'
									|| c == '6' || c == '7' || c == '8'
									|| c == '9') {
								isDoing += c;
							}
						}
					}
				} catch (Exception e) {
				}
			}
			in.close();
		} catch (Exception e) {
			return;
		}
	}

	private void saveSettings() {
		try {
			ArrayList<String> s = new ArrayList<String>();
			s.add("<Method>(" + (String) gui.comboBox1.getSelectedItem() + ")");
			s.add("<LogType>(" + (String) gui.comboBox2.getSelectedItem() + ")");
			s.add("<BowType>(" + (String) gui.comboBox3.getSelectedItem() + ")");
			s.add("<Knife>(" + (String) gui.comboBox4.getSelectedItem() + ")");
			s.add("<AxeType>(" + (String) gui.comboBox5.getSelectedItem() + ")");
			s.add("<Color1>(" + (String) gui.comboBox12.getSelectedItem() + ")");
			s.add("<Color2>(" + (String) gui.comboBox13.getSelectedItem() + ")");
			s.add("<Color3>(" + (String) gui.comboBox8.getSelectedItem() + ")");
			s.add("<Color4>(" + (String) gui.comboBox9.getSelectedItem() + ")");
			s.add("<Color5>(" + (String) gui.comboBox10.getSelectedItem() + ")");
			s.add("<Color6>(" + (String) gui.comboBox11.getSelectedItem() + ")");
			s.add("<Color7>(" + (String) gui.comboBox14.getSelectedItem() + ")");
			s.add("<Color8>(" + (String) gui.comboBox15.getSelectedItem() + ")");
			s.add("<Amount>(" + (String) gui.textField1.getText() + ")");
			s.add("<Name>(" + (String) gui.textField2.getText() + ")");
			s.add("<WhenDone>(" + getValue(gui.checkBox1.isSelected()) + ")");
			s.add("<UponLvl>(" + getValue(gui.checkBox2.isSelected()) + ")");
			s.add("<Hourly>(" + getValue(gui.checkBox3.isSelected()) + ")");
			s.add("<Before99>(" + getValue(gui.checkBox5.isSelected()) + ")");
			s.add("<Save>(" + getValue(gui.checkBox6.isSelected()) + ")");
			s.add("<Update>(" + getValue(gui.checkBox7.isSelected()) + ")");
			s.add("<Paint>(" + getValue(gui.checkBox4.isSelected()) + ")");
			s.add("<Chat>(" + getValue(gui.checkBox8.isSelected()) + ")");
			s.add("<Inventory>(" + getValue(gui.checkBox9.isSelected()) + ")");
			s.add("<Bar>(" + getValue(gui.checkBox10.isSelected()) + ")");
			s.add("<BotLine>(" + getValue(gui.checkBox11.isSelected()) + ")");
			s.add("<BotCross>(" + getValue(gui.checkBox12.isSelected()) + ")");
			s.add("<UserLine>(" + getValue(gui.checkBox13.isSelected()) + ")");
			s.add("<UserCross>(" + getValue(gui.checkBox14.isSelected()) + ")");
			s.add("<BotCircle>(" + getValue(gui.checkBox17.isSelected()) + ")");
			s.add("<UserCircle>(" + getValue(gui.checkBox18.isSelected()) + ")");
			s.add("<Message>(" + getValue(gui.checkBox15.isSelected()) + ")");
			s.add("<Beep>(" + getValue(gui.checkBox16.isSelected()) + ")");
			s.add("<IRCname>(" + (String) nameGUI.textField1.getText() + ")");
			s.add("<Speed>(" + (int) gui.slider1.getValue() + ")");

			final BufferedWriter writer = new BufferedWriter(new FileWriter(
					GlobalConfiguration.Paths.getHomeDirectory()
							+ File.separator + "Settings" + File.separator
							+ "UFletch.ini"));
			for (String str : s) {
				writer.write(str);
				writer.newLine();
			}
			writer.close();
		} catch (Exception e) {

		}
	}

	public class trayInfo extends MenuItem {
		private static final long serialVersionUID = 1L;
		private PopupMenu menu = new PopupMenu();
		public TrayIcon systray;

		public trayInfo() {
			initComponents();
		}

		private void item1ActionPerformed(ActionEvent e) {
			stopScript(false);
		}

		private void item2ActionPerformed(ActionEvent e) {
			env.setUserInput(Environment.INPUT_KEYBOARD
					| Environment.INPUT_MOUSE);
			pause = true;
		}

		private void item3ActionPerformed(ActionEvent e) {
			pause = false;
			env.setUserInput(Environment.INPUT_KEYBOARD);
			log("Resuming..");
		}

		private void item4ActionPerformed(ActionEvent e) {
			gui.button1.setText("Update");
			gui.setVisible(true);
		}

		private void item5ActionPerformed(ActionEvent e) {
			gui.tabbedPane1.setSelectedIndex(4);
			gui.button1.setText("Update");
			gui.setVisible(true);
		}

		private void item6ActionPerformed(ActionEvent e) {
			if (nameGUI.checkBox1.isSelected()) {
				irc.connect();
				IRCgui.setVisible(true);
			}
		}

		private void initComponents() {
			if (!SystemTray.isSupported()) {
				JOptionPane.showMessageDialog(null, "SystemTray not supported");
			} else {
				menu.add(constants.item1);
				constants.item1.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						item1ActionPerformed(e);
					}
				});
				menu.add(constants.item2);
				constants.item2.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						item2ActionPerformed(e);
					}
				});
				menu.add(constants.item3);
				constants.item3.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						item3ActionPerformed(e);
					}
				});
				menu.add(constants.item4);
				constants.item4.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						item4ActionPerformed(e);
					}
				});
				menu.add(constants.item5);
				constants.item5.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						item5ActionPerformed(e);
					}
				});
				if (nameGUI.checkBox1.isSelected()) {
					menu.add(constants.item6);
				}
				constants.item6.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						item6ActionPerformed(e);
					}
				});
				try {
					systray = new TrayIcon(
							icon.getScaledInstance(SystemTray.getSystemTray()
									.getTrayIconSize().width, SystemTray
									.getSystemTray().getTrayIconSize().height,
									0), "UFletch", menu);
					SystemTray.getSystemTray().add(systray);
				} catch (Exception e) {
					log("Error setting up system tray!");
				}
			}
		}
	}

	public class gui extends JFrame {
		private static final long serialVersionUID = 1L;

		public gui() {
			initComponents();
		}

		private String getMessage() {

			URLConnection url = null;
			BufferedReader in = null;
			try {
				url = new URL(
						"http://dl.dropbox.com/u/23938245/Scripts/UFletch/Other/message.txt")
						.openConnection();
				in = new BufferedReader(new InputStreamReader(
						url.getInputStream()));
				return in.readLine();
			} catch (MalformedURLException e) {
			} catch (IOException e) {
			}
			return "nothing new.";
		}

		private void button3ActionPerformed(ActionEvent e) {
			try {
				Desktop.getDesktop().browse(
						new URL("http://www.universalscripts.org").toURI());
			} catch (MalformedURLException e1) {
			} catch (IOException e1) {
			} catch (URISyntaxException e1) {
			}
		}

		private void label1MouseClicked(MouseEvent e) {
			try {
				Desktop.getDesktop().browse(
						new URL(
								"http://www.universalscripts.org/UFletch_generate.php?user="
										+ gui.textField2.getText()).toURI());
			} catch (MalformedURLException e1) {
			} catch (IOException e1) {
			} catch (URISyntaxException e1) {
			}
		}

		private void button4ActionPerformed(ActionEvent e) {
			createSignature();
			name = textField2.getText();
			label1.setText("<html><img src =http://universalscripts.org/UFletch_generate.php?user="
					+ name + "> </html>");
		}

		private void button5ActionPerformed(ActionEvent e) {
			try {
				Desktop.getDesktop()
						.browse(new URL(
								"https://www.paypal.com/cgi-bin/webscr?cmd=_s-xclick&hosted_button_id=WCYVXS8Z7X63C")
								.toURI());
			} catch (MalformedURLException e1) {
			} catch (IOException e1) {
			} catch (URISyntaxException e1) {
			}
		}

		private void button2ActionPerformed(ActionEvent e) {
			checkForUpdates();
		}

		private void button1ActionPerformed(ActionEvent e) {
			setVisible(false);
			log("Task: "
					+ constants.optionLog[gui.comboBox2.getSelectedIndex()]
					+ " "
					+ constants.optionBow[gui.comboBox3.getSelectedIndex()]
					+ " "
					+ constants.optionMethod[gui.comboBox1.getSelectedIndex()]);
			Mouse1 = (int) slider1.getValue();
			if (Mouse1 == 100) {
				Mouse2 = random(1, 2);
			} else if (Mouse1 == 90) {
				Mouse2 = random(2, 4);
			} else if (Mouse1 == 80) {
				Mouse2 = random(3, 5);
			} else if (Mouse1 == 70) {
				Mouse2 = random(4, 6);
			} else if (Mouse1 == 60) {
				Mouse2 = random(5, 7);
			} else if (Mouse1 == 50) {
				Mouse2 = random(6, 8);
			} else if (Mouse1 == 40) {
				Mouse2 = random(7, 9);
			} else if (Mouse1 == 30) {
				Mouse2 = random(8, 10);
			} else if (Mouse1 == 20) {
				Mouse2 = random(10, 12);
			} else if (Mouse1 == 10) {
				Mouse2 = random(12, 14);
			}
			mouse.setSpeed(Mouse2);
			button4.setEnabled(false);
			button3.setEnabled(false);
			button2.setEnabled(false);
			textField2.setEnabled(false);
		}

		private void initComponents() {
			// JFormDesigner - Component initialization - DO NOT MODIFY
			// //GEN-BEGIN:initComponents
			tabbedPane1 = new JTabbedPane();
			panel4 = new JPanel();
			label24 = new JLabel();
			label25 = new JLabel();
			label2 = new JLabel();
			comboBox2 = new JComboBox(constants.optionLog);
			label3 = new JLabel();
			comboBox3 = new JComboBox(constants.optionBow);
			label4 = new JLabel();
			label5 = new JLabel();
			comboBox4 = new JComboBox(constants.optionKnife);
			comboBox5 = new JComboBox(constants.optionAxe);
			label6 = new JLabel();
			label7 = new JLabel();
			comboBox1 = new JComboBox(constants.optionMethod);
			label8 = new JLabel();
			label9 = new JLabel();
			label10 = new JLabel();
			label11 = new JLabel();
			label14 = new JLabel();
			label12 = new JLabel();
			textField1 = new JTextField();
			label13 = new JLabel();
			panel2 = new JPanel();
			label31 = new JLabel();
			label32 = new JLabel();
			label33 = new JLabel();
			label34 = new JLabel();
			label35 = new JLabel();
			label36 = new JLabel();
			label37 = new JLabel();
			label38 = new JLabel();
			button3 = new JButton();
			label39 = new JLabel();
			label40 = new JLabel();
			checkBox4 = new JCheckBox();
			checkBox8 = new JCheckBox();
			checkBox9 = new JCheckBox();
			checkBox10 = new JCheckBox();
			checkBox11 = new JCheckBox();
			checkBox12 = new JCheckBox();
			checkBox13 = new JCheckBox();
			checkBox14 = new JCheckBox();
			label41 = new JLabel();
			comboBox8 = new JComboBox(constants.optionColor);
			label42 = new JLabel();
			comboBox9 = new JComboBox(constants.optionColor);
			label43 = new JLabel();
			label44 = new JLabel();
			label45 = new JLabel();
			label46 = new JLabel();
			comboBox10 = new JComboBox(constants.optionColor);
			comboBox11 = new JComboBox(constants.optionColor);
			comboBox12 = new JComboBox(constants.optionColor);
			comboBox13 = new JComboBox(constants.optionColor);
			comboBox14 = new JComboBox(constants.optionColor);
			comboBox15 = new JComboBox(constants.optionColor);
			label47 = new JLabel();
			label64 = new JLabel();
			checkBox17 = new JCheckBox();
			label65 = new JLabel();
			checkBox18 = new JCheckBox();
			panel1 = new JPanel();
			label17 = new JLabel();
			textField2 = new JTextField();
			label18 = new JLabel();
			label1 = new JLabel();
			button4 = new JButton();
			panel3 = new JPanel();
			slider1 = new JSlider();
			label15 = new JLabel();
			label26 = new JLabel();
			checkBox7 = new JCheckBox();
			label16 = new JLabel();
			checkBox6 = new JCheckBox();
			label19 = new JLabel();
			label20 = new JLabel();
			label21 = new JLabel();
			label22 = new JLabel();
			label23 = new JLabel();
			checkBox5 = new JCheckBox();
			label27 = new JLabel();
			checkBox1 = new JCheckBox();
			checkBox2 = new JCheckBox();
			button5 = new JButton();
			button2 = new JButton();
			label28 = new JLabel();
			checkBox3 = new JCheckBox();
			label29 = new JLabel();
			label30 = new JLabel();
			label48 = new JLabel();
			checkBox15 = new JCheckBox();
			label49 = new JLabel();
			checkBox16 = new JCheckBox();

			panel5 = new JPanel();
			label50 = new JLabel();
			label51 = new JLabel();
			label52 = new JLabel();
			label53 = new JLabel();
			label54 = new JLabel();
			label55 = new JLabel();
			label56 = new JLabel();
			label57 = new JLabel();
			label58 = new JLabel();
			label59 = new JLabel();
			label60 = new JLabel();
			label61 = new JLabel();
			label62 = new JLabel();
			label63 = new JLabel();
			button1 = new JButton();

			// ======== this ========
			setTitle("UFletch - GUI");
			Container contentPane = getContentPane();
			contentPane.setLayout(null);

			// ======== tabbedPane1 ========
			{
				tabbedPane1.setTabPlacement(SwingConstants.LEFT);

				// ======== panel4 ========
				{
					panel4.setLayout(null);

					// ---- label24 ----
					label24.setText("Message:");
					label24.setFont(new Font("Tahoma", Font.PLAIN, 17));
					label24.setForeground(Color.red);
					panel4.add(label24);
					label24.setBounds(0, 265, 75, 25);

					// ---- label25 ----
					label25.setText(getMessage());
					label25.setFont(new Font("Tahoma", Font.PLAIN, 17));
					label25.setForeground(Color.blue);
					panel4.add(label25);
					label25.setBounds(80, 265, 340, 25);

					// ---- label2 ----
					label2.setText("<html> <img src = http://dl.dropbox.com/u/23938245/Scripts/UFletch/Images/logs.png> </html>");
					panel4.add(label2);
					label2.setBounds(new Rectangle(new Point(5, 35), label2
							.getPreferredSize()));

					// ---- comboBox2 ----
					comboBox2.setForeground(Color.cyan);
					panel4.add(comboBox2);
					comboBox2.setBounds(50, 35, 160, 30);

					// ---- label3 ----
					label3.setText("<html> <img src = http://dl.dropbox.com/u/23938245/Scripts/UFletch/Images/bow.png> </html>");
					panel4.add(label3);
					label3.setBounds(new Rectangle(new Point(5, 110), label3
							.getPreferredSize()));

					// ---- comboBox3 ----
					comboBox3.setForeground(Color.blue);
					panel4.add(comboBox3);
					comboBox3.setBounds(50, 110, 160, 30);

					// ---- label4 ----
					label4.setText("<html> <img src = http://dl.dropbox.com/u/23938245/Scripts/UFletch/Images/knife.png> </html>");
					panel4.add(label4);
					label4.setBounds(220, 110, 25,
							label4.getPreferredSize().height);

					// ---- label5 ----
					label5.setText("<html> <img src = http://dl.dropbox.com/u/23938245/Scripts/UFletch/Images/axe.png> </html>");
					panel4.add(label5);
					label5.setBounds(220, 35, label5.getPreferredSize().width,
							30);

					// ---- comboBox4 ----
					comboBox4.setForeground(Color.green);
					panel4.add(comboBox4);
					comboBox4.setBounds(255, 110, 170, 30);

					// ---- comboBox5 ----
					comboBox5.setForeground(Color.red);
					panel4.add(comboBox5);
					comboBox5.setBounds(255, 35, 170, 30);

					// ---- label6 ----
					label6.setText("<html> <img src = http://dl.dropbox.com/u/23938245/Scripts/UFletch/Images/settings.png> </html>");
					panel4.add(label6);
					label6.setBounds(new Rectangle(new Point(5, 180), label6
							.getPreferredSize()));

					// ---- label7 ----
					label7.setText("<html> <img src = http://dl.dropbox.com/u/23938245/Scripts/UFletch/Images/settings.png> </html>");
					panel4.add(label7);
					label7.setBounds(375, 180, 47, 50);

					// ---- comboBox1 ----
					comboBox1.setForeground(Color.magenta);
					panel4.add(comboBox1);
					comboBox1.setBounds(60, 180, 310, 50);

					// ---- label8 ----
					label8.setText("Method To Perform");
					label8.setFont(label8.getFont().deriveFont(Font.PLAIN,
							label8.getFont().getSize() + 17f));
					panel4.add(label8);
					label8.setBounds(95, 145, 240, 40);

					// ---- label9 ----
					label9.setText("Bow Type");
					label9.setFont(label9.getFont().deriveFont(
							label9.getFont().getSize() + 17f));
					panel4.add(label9);
					label9.setBounds(65, 75, 125, 35);

					// ---- label10 ----
					label10.setText("Log Type");
					label10.setFont(label10.getFont().deriveFont(
							label10.getFont().getSize() + 17f));
					panel4.add(label10);
					label10.setBounds(new Rectangle(new Point(70, 0), label10
							.getPreferredSize()));

					// ---- label11 ----
					label11.setText("Axe Type");
					label11.setFont(label11.getFont().deriveFont(
							label11.getFont().getSize() + 17f));
					panel4.add(label11);
					label11.setBounds(new Rectangle(new Point(280, 0), label11
							.getPreferredSize()));

					// ---- label14 ----
					label14.setText("Knife Type");
					label14.setFont(label14.getFont().deriveFont(
							label14.getFont().getSize() + 17f));
					panel4.add(label14);
					label14.setBounds(new Rectangle(new Point(275, 75), label14
							.getPreferredSize()));

					// ---- label12 ----
					label12.setText("Amount to Fletch:");
					label12.setFont(new Font("Tahoma", Font.PLAIN, 16));
					panel4.add(label12);
					label12.setBounds(0, 235, 135, 25);

					// ---- textField1 ----
					textField1.setText("0");
					panel4.add(textField1);
					textField1.setBounds(135, 235, 90, 25);

					// ---- label13 ----
					label13.setText("0 for Unilimted Fletching!");
					label13.setFont(new Font("Tahoma", Font.PLAIN, 16));
					panel4.add(label13);
					label13.setBounds(230, 235, 190, 25);
				}
				tabbedPane1.addTab("Main Settings", panel4);

				// ======== panel2 ========
				{
					panel2.setLayout(null);

					// ---- label31 ----
					label31.setText("Enable Paint:");
					label31.setFont(new Font("Tahoma", Font.PLAIN, 16));
					panel2.add(label31);
					label31.setBounds(new Rectangle(new Point(5, 0), label31
							.getPreferredSize()));

					// ---- label32 ----
					label32.setText("Over Chatbox:");
					label32.setFont(new Font("Tahoma", Font.PLAIN, 16));
					panel2.add(label32);
					label32.setBounds(new Rectangle(new Point(5, 65), label32
							.getPreferredSize()));

					// ---- label33 ----
					label33.setText("Over Inventory:");
					label33.setFont(new Font("Tahoma", Font.PLAIN, 16));
					panel2.add(label33);
					label33.setBounds(new Rectangle(new Point(5, 85), label33
							.getPreferredSize()));

					// ---- label34 ----
					label34.setText("Progress bar:");
					label34.setFont(new Font("Tahoma", Font.PLAIN, 16));
					panel2.add(label34);
					label34.setBounds(new Rectangle(new Point(5, 105), label34
							.getPreferredSize()));

					// ---- label35 ----
					label35.setText("RSBot Mouse Lines:");
					label35.setFont(new Font("Tahoma", Font.PLAIN, 16));
					panel2.add(label35);
					label35.setBounds(new Rectangle(new Point(5, 125), label35
							.getPreferredSize()));

					// ---- label36 ----
					label36.setText("RSBot Mouse Crosshair:");
					label36.setFont(new Font("Tahoma", Font.PLAIN, 16));
					panel2.add(label36);
					label36.setBounds(new Rectangle(new Point(5, 145), label36
							.getPreferredSize()));

					// ---- label37 ----
					label37.setText("Your Mouse Lines:");
					label37.setFont(new Font("Tahoma", Font.PLAIN, 16));
					panel2.add(label37);
					label37.setBounds(new Rectangle(new Point(5, 165), label37
							.getPreferredSize()));

					// ---- label38 ----
					label38.setText("Your Mouse Crosshair:");
					label38.setFont(new Font("Tahoma", Font.PLAIN, 16));
					panel2.add(label38);
					label38.setBounds(5, 185, 165,
							label38.getPreferredSize().height);

					// ---- button3 ----
					button3.setText("Visit Universalscripts.org! :D");
					button3.setFont(button3.getFont().deriveFont(
							button3.getFont().getSize() + 19f));
					button3.setForeground(Color.red);
					button3.setBackground(new Color(255, 0, 51));
					button3.addActionListener(new ActionListener() {
						public void actionPerformed(ActionEvent e) {
							button3ActionPerformed(e);
						}
					});
					panel2.add(button3);
					button3.setBounds(0, 210, 425, 80);

					// ---- label39 ----
					label39.setText("Text Color:");
					label39.setFont(new Font("Tahoma", Font.PLAIN, 16));
					panel2.add(label39);
					label39.setBounds(new Rectangle(new Point(5, 25), label39
							.getPreferredSize()));

					// ---- label40 ----
					label40.setText("Paint Main Color:");
					label40.setFont(new Font("Tahoma", Font.PLAIN, 16));
					panel2.add(label40);
					label40.setBounds(new Rectangle(new Point(5, 45), label40
							.getPreferredSize()));

					// ---- checkBox4 ----
					checkBox4.setFont(new Font("Tahoma", Font.PLAIN, 16));
					checkBox4.setSelected(true);
					panel2.add(checkBox4);
					checkBox4.setBounds(new Rectangle(new Point(100, 0),
							checkBox4.getPreferredSize()));

					// ---- checkBox8 ----
					checkBox8.setFont(new Font("Tahoma", Font.PLAIN, 16));
					checkBox8.setSelected(true);
					panel2.add(checkBox8);
					checkBox8.setBounds(new Rectangle(new Point(110, 65),
							checkBox8.getPreferredSize()));

					// ---- checkBox9 ----
					checkBox9.setFont(new Font("Tahoma", Font.PLAIN, 16));
					panel2.add(checkBox9);
					checkBox9.setBounds(new Rectangle(new Point(120, 85),
							checkBox9.getPreferredSize()));

					// ---- checkBox10 ----
					checkBox10.setFont(new Font("Tahoma", Font.PLAIN, 16));
					checkBox10.setSelected(true);
					panel2.add(checkBox10);
					checkBox10.setBounds(new Rectangle(new Point(100, 105),
							checkBox10.getPreferredSize()));

					// ---- checkBox11 ----
					checkBox11.setFont(new Font("Tahoma", Font.PLAIN, 16));
					checkBox11.setSelected(true);
					panel2.add(checkBox11);
					checkBox11.setBounds(new Rectangle(new Point(145, 125),
							checkBox11.getPreferredSize()));

					// ---- checkBox12 ----
					checkBox12.setFont(new Font("Tahoma", Font.PLAIN, 16));
					checkBox12.setSelected(true);
					panel2.add(checkBox12);
					checkBox12.setBounds(new Rectangle(new Point(175, 145),
							checkBox12.getPreferredSize()));

					// ---- checkBox13 ----
					checkBox13.setFont(new Font("Tahoma", Font.PLAIN, 16));
					checkBox13.setSelected(true);
					panel2.add(checkBox13);
					checkBox13.setBounds(new Rectangle(new Point(135, 165),
							checkBox13.getPreferredSize()));

					// ---- checkBox14 ----
					checkBox14.setFont(new Font("Tahoma", Font.PLAIN, 16));
					checkBox14.setSelected(true);
					panel2.add(checkBox14);
					checkBox14.setBounds(new Rectangle(new Point(165, 185),
							checkBox14.getPreferredSize()));

					// ---- label41 ----
					label41.setText("Color Below:");
					label41.setFont(new Font("Tahoma", Font.PLAIN, 16));
					panel2.add(label41);
					label41.setBounds(new Rectangle(new Point(120, 105),
							label41.getPreferredSize()));
					panel2.add(comboBox8);
					comboBox8.setBounds(215, 105, 70,
							comboBox8.getPreferredSize().height);

					// ---- label42 ----
					label42.setText("On Top:");
					label42.setFont(new Font("Tahoma", Font.PLAIN, 16));
					panel2.add(label42);
					label42.setBounds(new Rectangle(new Point(290, 105),
							label42.getPreferredSize()));
					panel2.add(comboBox9);
					comboBox9.setBounds(355, 105, 70,
							comboBox9.getPreferredSize().height);

					// ---- label43 ----
					label43.setText("Color:");
					label43.setFont(new Font("Tahoma", Font.PLAIN, 16));
					panel2.add(label43);
					label43.setBounds(new Rectangle(new Point(240, 125),
							label43.getPreferredSize()));

					// ---- label44 ----
					label44.setText("Color:");
					label44.setFont(new Font("Tahoma", Font.PLAIN, 16));
					panel2.add(label44);
					label44.setBounds(new Rectangle(new Point(195, 145),
							label44.getPreferredSize()));

					// ---- label45 ----
					label45.setText("Color:");
					label45.setFont(new Font("Tahoma", Font.PLAIN, 16));
					panel2.add(label45);
					label45.setBounds(235, 165, 45,
							label45.getPreferredSize().height);

					// ---- label46 ----
					label46.setText("Color:");
					label46.setFont(new Font("Tahoma", Font.PLAIN, 16));
					panel2.add(label46);
					label46.setBounds(new Rectangle(new Point(185, 185),
							label46.getPreferredSize()));
					panel2.add(comboBox10);
					comboBox10.setBounds(285, 125, 70,
							comboBox10.getPreferredSize().height);
					panel2.add(comboBox11);
					comboBox11.setBounds(240, 145, 70,
							comboBox11.getPreferredSize().height);
					panel2.add(comboBox12);
					comboBox12.setBounds(90, 25, 70,
							comboBox12.getPreferredSize().height);
					panel2.add(comboBox13);
					comboBox13.setBounds(130, 45, 70,
							comboBox13.getPreferredSize().height);
					panel2.add(comboBox14);
					comboBox14.setBounds(280, 165, 70,
							comboBox14.getPreferredSize().height);
					panel2.add(comboBox15);
					comboBox15.setBounds(230, 185, 65,
							comboBox15.getPreferredSize().height);

					// ---- label47 ----
					label47.setText("<html> <img src= http://dl.dropbox.com/u/23938245/Scripts/UFletch/Images/paint.png> </html>");
					panel2.add(label47);
					label47.setBounds(265, -5, 125, 115);

					// ---- label64 ----
					label64.setText("Circles:");
					label64.setFont(new Font("Tahoma", Font.PLAIN, 16));
					panel2.add(label64);
					label64.setBounds(new Rectangle(new Point(165, 125),
							label64.getPreferredSize()));

					// ---- checkBox17 ----
					checkBox17.setFont(new Font("Tahoma", Font.PLAIN, 16));
					checkBox17.setSelected(true);
					panel2.add(checkBox17);
					checkBox17.setBounds(new Rectangle(new Point(220, 125),
							checkBox17.getPreferredSize()));

					// ---- label65 ----
					label65.setText("Cricles:");
					label65.setFont(new Font("Tahoma", Font.PLAIN, 16));
					panel2.add(label65);
					label65.setBounds(new Rectangle(new Point(155, 165),
							label65.getPreferredSize()));

					// ---- checkBox18 ----
					checkBox18.setFont(new Font("Tahoma", Font.PLAIN, 16));
					panel2.add(checkBox18);
					checkBox18.setBounds(new Rectangle(new Point(210, 165),
							checkBox18.getPreferredSize()));
				}
				tabbedPane1.addTab("Paint Settings", panel2);

				// ======== panel1 ========
				{
					panel1.setLayout(null);

					// ---- label17 ----
					label17.setText("Signature name:");
					label17.setFont(label17.getFont().deriveFont(
							label17.getFont().getStyle() & ~Font.BOLD));
					panel1.add(label17);
					label17.setBounds(0, 5, 80, 25);

					// ---- textField2 ----
					textField2.setText("All");
					panel1.add(textField2);
					textField2.setBounds(80, 5, 95, 25);

					// ---- label18 ----
					label18.setText("No Spaces Please!");
					panel1.add(label18);
					label18.setBounds(180, 5, 88, 25);

					// ---- label1 ----
					label1.setText("test");
					label1.addMouseListener(new MouseAdapter() {
						@Override
						public void mouseClicked(MouseEvent e) {
							label1MouseClicked(e);
						}
					});
					panel1.add(label1);
					label1.setBounds(0, 25, 425, 265);

					// ---- button4 ----
					button4.setText("Generate Signature");
					button4.setForeground(new Color(0, 204, 0));
					button4.setBackground(Color.white);
					button4.addActionListener(new ActionListener() {
						public void actionPerformed(ActionEvent e) {
							button4ActionPerformed(e);
						}
					});
					panel1.add(button4);
					button4.setBounds(275, 5, 155, 25);
				}
				tabbedPane1.addTab("Signature", panel1);

				// ======== panel3 ========
				{
					panel3.setLayout(null);

					// ---- slider1 ----
					slider1.setSnapToTicks(true);
					slider1.setPaintTicks(true);
					slider1.setMajorTickSpacing(10);
					panel3.add(slider1);
					slider1.setBounds(105, 130, 315, 30);

					// ---- label15 ----
					label15.setText("Mousespeed:");
					label15.setFont(new Font("Tahoma", Font.PLAIN, 16));
					panel3.add(label15);
					label15.setBounds(5, 130, 100, 30);

					// ---- label26 ----
					label26.setText("Auto Update:");
					label26.setFont(new Font("Tahoma", Font.PLAIN, 16));
					panel3.add(label26);
					label26.setBounds(125, 195, 100, 20);

					// ---- checkBox7 ----
					checkBox7.setText("(Highly Reccomended)");
					checkBox7.setSelected(true);
					checkBox7.setFont(new Font("Tahoma", Font.PLAIN, 16));
					panel3.add(checkBox7);
					checkBox7.setBounds(220, 195, 190, 20);

					// ---- label16 ----
					label16.setText("Save Settings:");
					label16.setFont(new Font("Tahoma", Font.PLAIN, 16));
					panel3.add(label16);
					label16.setBounds(0, 195, 110, 20);

					// ---- checkBox6 ----
					checkBox6.setSelected(true);
					checkBox6.setFont(new Font("Tahoma", Font.PLAIN, 16));
					panel3.add(checkBox6);
					checkBox6.setBounds(100, 195, 21, 21);

					// ---- label19 ----
					label19.setText("Version checker:  Latest Version:");
					label19.setFont(new Font("Tahoma", Font.PLAIN, 16));
					label19.setForeground(Color.red);
					panel3.add(label19);
					label19.setBounds(0, 220, 235, 20);

					// ---- label20 ----
					label20.setText(newversion());
					label20.setFont(new Font("Tahoma", Font.PLAIN, 16));
					label20.setForeground(Color.blue);
					panel3.add(label20);
					label20.setBounds(235, 220, 40, 20);

					// ---- label21 ----
					label21.setText("Your Version:");
					label21.setFont(new Font("Tahoma", Font.PLAIN, 16));
					label21.setForeground(Color.red);
					panel3.add(label21);
					label21.setBounds(280, 220, 100, 20);

					// ---- label22 ----
					label22.setText(urversion());
					label22.setFont(new Font("Tahoma", Font.PLAIN, 16));
					label22.setForeground(Color.blue);
					panel3.add(label22);
					label22.setBounds(380, 220, 45, 20);

					// ---- label23 ----
					label23.setText("logout 20k experince before 99 fletching:");
					label23.setFont(new Font("Tahoma", Font.PLAIN, 16));
					panel3.add(label23);
					label23.setBounds(5, 85, 300, 20);

					// ---- checkBox5 ----
					checkBox5.setSelected(true);
					panel3.add(checkBox5);
					checkBox5.setBounds(300, 85, 21, 21);

					// ---- label27 ----
					label27.setText("when done:");
					label27.setFont(new Font("Tahoma", Font.PLAIN, 16));
					panel3.add(label27);
					label27.setBounds(35, 165, 90, 25);

					// ---- checkBox1 ----
					checkBox1.setText(" Upon Level:");
					checkBox1.setSelected(true);
					checkBox1.setFont(new Font("Tahoma", Font.PLAIN, 16));
					panel3.add(checkBox1);
					checkBox1.setBounds(125, 165, 115, 25);

					// ---- checkBox2 ----
					checkBox2.setSelected(true);
					checkBox2.setFont(new Font("Tahoma", Font.PLAIN, 16));
					checkBox2.setText("Getting 99:");
					panel3.add(checkBox2);
					checkBox2.setBounds(240, 165, 110, 25);

					// ---- button5 ----
					button5.setText("Donate");
					button5.setForeground(Color.red);
					button5.setBackground(Color.red);
					button5.addActionListener(new ActionListener() {
						public void actionPerformed(ActionEvent e) {
							button5ActionPerformed(e);
							button5ActionPerformed(e);
						}
					});
					panel3.add(button5);
					button5.setBounds(0, 240, 210, 50);

					// ---- button2 ----
					button2.setText("Check For Updates");
					button2.setForeground(Color.red);
					button2.setBackground(Color.red);
					button2.addActionListener(new ActionListener() {
						public void actionPerformed(ActionEvent e) {
							button2ActionPerformed(e);
							button2ActionPerformed(e);
						}
					});
					panel3.add(button2);
					button2.setBounds(210, 240, 215, 50);

					// ---- label28 ----
					label28.setText("<html> <img src =http://dl.dropbox.com/u/23938245/Scripts/UFletch/Images/camera.png> </html>");
					panel3.add(label28);
					label28.setBounds(new Rectangle(new Point(0, 165), label28
							.getPreferredSize()));

					// ---- checkBox3 ----
					checkBox3.setFont(new Font("Tahoma", Font.PLAIN, 16));
					checkBox3.setSelected(true);
					panel3.add(checkBox3);
					checkBox3.setBounds(350, 165,
							checkBox3.getPreferredSize().width, 26);

					// ---- label29 ----
					label29.setText("Developed by: Fletch to 99");
					label29.setForeground(Color.blue);
					label29.setFont(label29.getFont().deriveFont(Font.BOLD,
							label29.getFont().getSize() + 20f));
					panel3.add(label29);
					label29.setBounds(5, 35, 415, 55);

					// ---- label30 ----
					label30.setText("UFletch: FREE, AIO, FLAWLESS!");
					label30.setForeground(Color.green);
					label30.setFont(label30.getFont().deriveFont(
							label30.getFont().getStyle() | Font.BOLD,
							label30.getFont().getSize() + 15f));
					panel3.add(label30);
					label30.setBounds(new Rectangle(new Point(5, 5), label30
							.getPreferredSize()));

					// ---- label48 ----
					label48.setText("Message Notification:");
					label48.setFont(new Font("Tahoma", Font.PLAIN, 16));
					panel3.add(label48);
					label48.setBounds(new Rectangle(new Point(5, 105), label48
							.getPreferredSize()));

					// ---- checkBox15 ----
					checkBox15.setFont(new Font("Tahoma", Font.PLAIN, 16));
					checkBox15.setSelected(true);
					panel3.add(checkBox15);
					checkBox15.setBounds(new Rectangle(new Point(160, 105),
							checkBox15.getPreferredSize()));

					// ---- label49 ----
					label49.setText("Message Beep:");
					label49.setFont(new Font("Tahoma", Font.PLAIN, 16));
					panel3.add(label49);
					label49.setBounds(new Rectangle(new Point(185, 105),
							label49.getPreferredSize()));

					// ---- checkBox16 ----
					checkBox16.setFont(new Font("Tahoma", Font.PLAIN, 16));
					checkBox16.setSelected(true);
					panel3.add(checkBox16);
					checkBox16.setBounds(new Rectangle(new Point(290, 105),
							checkBox16.getPreferredSize()));

					{ // compute preferred size
						Dimension preferredSize = new Dimension();
						for (int i = 0; i < panel3.getComponentCount(); i++) {
							Rectangle bounds = panel3.getComponent(i)
									.getBounds();
							preferredSize.width = Math.max(bounds.x
									+ bounds.width, preferredSize.width);
							preferredSize.height = Math.max(bounds.y
									+ bounds.height, preferredSize.height);
						}
						Insets insets = panel3.getInsets();
						preferredSize.width += insets.right;
						preferredSize.height += insets.bottom;
						panel3.setMinimumSize(preferredSize);
						panel3.setPreferredSize(preferredSize);
					}
				}
				tabbedPane1.addTab("Other Settings", panel3);

				// ======== panel5 ========
				{
					panel5.setLayout(null);

					// ---- label50 ----
					label50.setText("Log Type: The type of log you wish to fletch or chop.");
					label50.setFont(new Font("Tahoma", Font.PLAIN, 14));
					panel5.add(label50);
					label50.setBounds(new Rectangle(new Point(0, 5), label50
							.getPreferredSize()));

					// ---- label51 ----
					label51.setText("Axe Type: The axe to use while chopping logs.");
					label51.setFont(new Font("Tahoma", Font.PLAIN, 14));
					panel5.add(label51);
					label51.setBounds(new Rectangle(new Point(0, 25), label51
							.getPreferredSize()));

					// ---- label52 ----
					label52.setText("Knife Type: The Knife to use while fletching.");
					label52.setFont(new Font("Tahoma", Font.PLAIN, 14));
					panel5.add(label52);
					label52.setBounds(new Rectangle(new Point(0, 45), label52
							.getPreferredSize()));

					// ---- label53 ----
					label53.setText("Bow Type: The type of bow you wish to fletch.");
					label53.setFont(new Font("Tahoma", Font.PLAIN, 14));
					panel5.add(label53);
					label53.setBounds(new Rectangle(new Point(0, 65), label53
							.getPreferredSize()));

					// ---- label54 ----
					label54.setText("Method: How you want to fletch.");
					label54.setFont(new Font("Tahoma", Font.PLAIN, 14));
					panel5.add(label54);
					label54.setBounds(new Rectangle(new Point(0, 85), label54
							.getPreferredSize()));

					// ---- label55 ----
					label55.setText("Colors: The color for the checked item.");
					label55.setFont(new Font("Tahoma", Font.PLAIN, 14));
					panel5.add(label55);
					label55.setBounds(new Rectangle(new Point(0, 105), label55
							.getPreferredSize()));

					// ---- label56 ----
					label56.setText("Signature Name: The name you want on your dynamic signature.");
					label56.setFont(new Font("Tahoma", Font.PLAIN, 14));
					panel5.add(label56);
					label56.setBounds(new Rectangle(new Point(0, 125), label56
							.getPreferredSize()));

					// ---- label57 ----
					label57.setText("20k before 99: Logs you out 20k experince before 99 fletching.");
					label57.setFont(new Font("Tahoma", Font.PLAIN, 14));
					panel5.add(label57);
					label57.setBounds(new Rectangle(new Point(0, 145), label57
							.getPreferredSize()));

					// ---- label58 ----
					label58.setText("Message Notification: Notifiys you when someone talks.");
					label58.setFont(new Font("Tahoma", Font.PLAIN, 14));
					panel5.add(label58);
					label58.setBounds(0, 165, 340,
							label58.getPreferredSize().height);

					// ---- label59 ----
					label59.setText("Message Beep: Makes a beeping sound twice when someone talks.");
					label59.setFont(new Font("Tahoma", Font.PLAIN, 14));
					panel5.add(label59);
					label59.setBounds(new Rectangle(new Point(0, 185), label59
							.getPreferredSize()));

					// ---- label60 ----
					label60.setText("Mouse Speed: How fast the bots mouse should move.");
					label60.setFont(new Font("Tahoma", Font.PLAIN, 14));
					panel5.add(label60);
					label60.setBounds(new Rectangle(new Point(0, 205), label60
							.getPreferredSize()));

					// ---- label61 ----
					label61.setText("Screenshots: Takes a screenshot at the selected times.");
					label61.setFont(new Font("Tahoma", Font.PLAIN, 14));
					panel5.add(label61);
					label61.setBounds(new Rectangle(new Point(0, 225), label61
							.getPreferredSize()));

					// ---- label62 ----
					label62.setText("Save Settings: Saves the settings to be loaded next time.");
					label62.setFont(new Font("Tahoma", Font.PLAIN, 14));
					panel5.add(label62);
					label62.setBounds(new Rectangle(new Point(0, 245), label62
							.getPreferredSize()));

					// ---- label63 ----
					label63.setText("Auto-Update: Tells you when the bot is outdated and updates it.");
					label63.setFont(new Font("Tahoma", Font.PLAIN, 14));
					panel5.add(label63);
					label63.setBounds(new Rectangle(new Point(0, 265), label63
							.getPreferredSize()));
				}
				tabbedPane1.addTab("About", panel5);
			}
			contentPane.add(tabbedPane1);
			tabbedPane1.setBounds(5, 0, 515, 295);

			// ---- button1 ----
			button1.setText("Start UFletch!");
			button1.setForeground(Color.blue);
			button1.setFont(new Font("Tahoma", Font.PLAIN, 26));
			button1.setBackground(new Color(51, 51, 255));
			button1.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					button1ActionPerformed(e);
				}
			});
			contentPane.add(button1);
			button1.setBounds(0, 295, 520, 45);

			comboBox12.setSelectedIndex(0);
			comboBox13.setSelectedIndex(8);
			comboBox8.setSelectedIndex(1);
			comboBox9.setSelectedIndex(3);
			comboBox10.setSelectedIndex(7);
			comboBox11.setSelectedIndex(6);
			comboBox14.setSelectedIndex(5);
			comboBox15.setSelectedIndex(2);

			{ // compute preferred size
				Dimension preferredSize = new Dimension();
				for (int i = 0; i < contentPane.getComponentCount(); i++) {
					Rectangle bounds = contentPane.getComponent(i).getBounds();
					preferredSize.width = Math.max(bounds.x + bounds.width,
							preferredSize.width);
					preferredSize.height = Math.max(bounds.y + bounds.height,
							preferredSize.height);
				}
				Insets insets = contentPane.getInsets();
				preferredSize.width += insets.right;
				preferredSize.height += insets.bottom;
				contentPane.setMinimumSize(preferredSize);
				contentPane.setPreferredSize(preferredSize);
			}
			pack();
			setLocationRelativeTo(getOwner());
			// JFormDesigner - End of component initialization
			// //GEN-END:initComponents
		}

		// JFormDesigner - Variables declaration - DO NOT MODIFY
		// //GEN-BEGIN:variables
		private JTabbedPane tabbedPane1;
		private JPanel panel4;
		private JLabel label24;
		private JLabel label25;
		private JLabel label2;
		private JComboBox comboBox2;
		private JLabel label3;
		private JComboBox comboBox3;
		private JLabel label4;
		private JLabel label5;
		private JComboBox comboBox4;
		private JComboBox comboBox5;
		private JLabel label6;
		private JLabel label7;
		private JComboBox comboBox1;
		private JLabel label8;
		private JLabel label9;
		private JLabel label10;
		private JLabel label11;
		private JLabel label14;
		private JLabel label12;
		private JTextField textField1;
		private JLabel label13;
		private JPanel panel2;
		private JLabel label31;
		private JLabel label32;
		private JLabel label33;
		private JLabel label34;
		private JLabel label35;
		private JLabel label36;
		private JLabel label37;
		private JLabel label38;
		private JButton button3;
		private JLabel label39;
		private JLabel label40;
		private JCheckBox checkBox4;
		private JCheckBox checkBox8;
		private JCheckBox checkBox9;
		private JCheckBox checkBox10;
		private JCheckBox checkBox11;
		private JCheckBox checkBox12;
		private JCheckBox checkBox13;
		private JCheckBox checkBox14;
		private JLabel label41;
		private JComboBox comboBox8;
		private JLabel label42;
		private JComboBox comboBox9;
		private JLabel label43;
		private JLabel label44;
		private JLabel label45;
		private JLabel label46;
		private JComboBox comboBox10;
		private JComboBox comboBox11;
		private JComboBox comboBox12;
		private JComboBox comboBox13;
		private JComboBox comboBox14;
		private JComboBox comboBox15;
		private JLabel label47;
		private JLabel label64;
		private JCheckBox checkBox17;
		private JLabel label65;
		private JCheckBox checkBox18;
		private JPanel panel1;
		private JLabel label17;
		private JTextField textField2;
		private JLabel label18;
		private JLabel label1;
		private JButton button4;
		private JPanel panel3;
		private JSlider slider1;
		private JLabel label15;
		private JLabel label26;
		private JCheckBox checkBox7;
		private JLabel label16;
		private JCheckBox checkBox6;
		private JLabel label19;
		private JLabel label20;
		private JLabel label21;
		private JLabel label22;
		private JLabel label23;
		private JCheckBox checkBox5;
		private JLabel label27;
		private JCheckBox checkBox1;
		private JCheckBox checkBox2;
		private JButton button5;
		private JButton button2;
		private JLabel label28;
		private JCheckBox checkBox3;
		private JLabel label29;
		private JLabel label30;
		private JLabel label48;
		private JCheckBox checkBox15;
		private JLabel label49;
		private JCheckBox checkBox16;
		private JPanel panel5;
		private JLabel label50;
		private JLabel label51;
		private JLabel label52;
		private JLabel label53;
		private JLabel label54;
		private JLabel label55;
		private JLabel label56;
		private JLabel label57;
		private JLabel label58;
		private JLabel label59;
		private JLabel label60;
		private JLabel label61;
		private JLabel label62;
		private JLabel label63;
		private JButton button1;
		// JFormDesigner - End of variables declaration //GEN-END:variables
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		p = e.getPoint();
		if (p.x >= 9 && p.x <= 36 && p.y >= 347 && p.y <= 372 || p.x >= 716
				&& p.x <= 733 && p.y >= 451 && p.y <= 466) {
			if (fullPaint) {
				fullPaint = false;
			} else if (!fullPaint) {
				fullPaint = true;
			}
		}

		if (p.x >= 360 && p.x <= 460 && p.y >= 440 && p.y <= 464 || p.x >= 603
				&& p.x <= 704 && p.y >= 282 && p.y <= 307) {
			gui.button1.setText("Update!");
			gui.setVisible(true);
		}
	}

	public void mouseEntered(MouseEvent e) {
	}

	public void mouseExited(MouseEvent e) {
	}

	public void mousePressed(MouseEvent e) {
		p2 = e.getPoint();
		if (p2.x >= 4 && p2.x <= 514 && p2.y >= 345 && p2.y <= 473
				|| p2.x >= 548 && p2.x <= 736 && p2.y >= 205 && p2.y <= 464) {
			isClicking = true;
		}
	}

	public void mouseReleased(MouseEvent e) {
		p2 = e.getPoint();
		if (p2.x >= 4 && p2.x <= 514 && p2.y >= 345 && p2.y <= 473
				|| p2.x >= 548 && p2.x <= 736 && p2.y >= 205 && p2.y <= 464) {
			isClicking = false;
		}
	}

	public void mouseDragged(MouseEvent e) {
	}

	public void mouseMoved(MouseEvent e) {
		z = e.getPoint();
	}

	public class beeper implements Runnable {
		private String firstMessage = "";

		public void text() {
			if (!m().toLowerCase().isEmpty()
					&& !m().toLowerCase().equals(firstMessage)) {
				beep();
				firstMessage = m().toLowerCase();
			}
		}

		public void beep() {
			try {
				for (int i = 0; i < 3; i++) {
					java.awt.Toolkit.getDefaultToolkit().beep();
					Thread.sleep(250);
				}
				Thread.sleep(random(100, 500));
			} catch (Exception e) {
			}
			return;
		}

		@Override
		public void run() {
			while (!b.isInterrupted()) {
				text();
				try {
					Thread.sleep(random(50, 150));
				} catch (InterruptedException e) {
				}
			}
		}

		public String m() {
			RSInterface chatBox = interfaces.get(137);
			for (int i = 281; i >= 180; i--) {// Valid text is from 180 to 281
				String text = chatBox.getComponent(i).getText();
				if (!text.isEmpty() && text.contains("<")) {
					return text;
				}
			}
			return "";
		}
	}

	public class Irc extends Thread implements Runnable {
		BufferedWriter writer = null;
		BufferedReader reader = null;
		Socket socket = null;

		@Override
		public void run() {
			checkForWords();
			return;
		}

		public Irc() {
			connect();
		}

		public void sendMessage(String message) {
			this.sendRaw("PRIVMSG " + constants.channel + " :" + message);
		}

		public void sendRaw(String line) {
			try {
				writer.write(line + "\n");
				writer.flush();
			} catch (IOException ioe) {
			}
		}

		public void leave() {
			sendRaw("QUIT " + constants.channel + "\n");
			try {
				socket.close();
			} catch (IOException e) {
			}
		}

		private void listAllNick(String message) {
			StringTokenizer st = new StringTokenizer(message);
			while (st.hasMoreTokens()) {
				try {
					users.insertString(users.getLength(),
							"\n" + st.nextToken(), new SimpleAttributeSet());
				} catch (BadLocationException e) {
				}
			}
		}

		public String IRCMessage(String IRCLine) {
			int index = 0;
			IRCLine.trim();
			index = IRCLine.indexOf(":", 2);
			if (index != -1) {
				return IRCLine.substring(index + 1);
			}
			return IRCLine;
		}

		public void checkForWords() {
			String line = null;
			String text = null;
			try {
				while ((line = reader.readLine()) != null) {
					if (line.contains("PING")) {
						sendRaw("PONG " + line.substring(4) + "\r\n");
					} else if (line.contains("JOIN")
							&& !IRCgui.textArea2.getText().contains(
									getIRCUserName(line))) {
						users.insertString(users.getLength(), "\n"
								+ getIRCUserName(line),
								new SimpleAttributeSet());
					} else if (line.contains("PART")
							|| line.contains("QUIT")
							&& IRCgui.textArea2.getText().contains(
									getIRCUserName(line))) {
						users.remove(
								IRCgui.textArea2.getText().indexOf(
										getIRCUserName(line).trim()) - 1,
								getIRCUserName(line).length() + 1);
					} else if (line.contains("KICK")
							&& getIRCUserName(line).equals(
									nameGUI.textField1.getText())) {
						nameGUI.setVisible(false);
						irc.leave();
						JOptionPane.showMessageDialog(null,
								"You have been kicked.");
					} else if (!line.contains("PING") && !line.contains("QUIT")) {
						text = IRCMessage(line);
						doc.insertString(0, "\n(" + getTimeStamp() + ")<"
								+ getIRCUserName(line) + ">: " + text,
								new SimpleAttributeSet());
					}
				}
			} catch (IOException e) {
			} catch (BadLocationException e) {
			}
		}

		public String getTimeStamp() {
			Timestamp st = new Timestamp(System.currentTimeMillis());
			String time = st.toString();
			return time.substring(0, time.indexOf("."));
		}

		protected String getIRCUserName(String line) {
			Pattern userPattern = Pattern.compile("^:\\w*!",
					Pattern.CASE_INSENSITIVE);
			Matcher user = userPattern.matcher(line);
			String username = null;
			if (user.find()) {
				username = line.substring(user.start() + 1, user.end() - 1);
				return username;
			}
			return "";
		}

		public void connect() {
			String line = null;
			try {
				socket = new Socket(constants.server, 6667);
				writer = new BufferedWriter(new OutputStreamWriter(
						socket.getOutputStream()));
				reader = new BufferedReader(new InputStreamReader(
						socket.getInputStream()));
				sendRaw("USER " + nameGUI.textField1.getText() + " 8 * :"
						+ nameGUI.textField1.getText() + "\n");
				sendRaw("NICK " + nameGUI.textField1.getText() + "\r\n");
				while ((line = reader.readLine()) != null) {
					if (line.indexOf("433") >= 0) {
						log("Nickname is already in use.");
						JOptionPane.showMessageDialog(null,
								"Name taken please change your name!");
						nameGUI.setVisible(true);
						while (nameGUI.isVisible()) {
							sleep(random(200, 400));
						}
						sendRaw("USER " + nameGUI.textField1.getText()
								+ " 8 * :" + nameGUI.textField1.getText()
								+ "\n");
						sendRaw("NICK " + nameGUI.textField1.getText() + "\r\n");
					}
					if (line.indexOf("376") >= 0) {
						sendRaw("JOIN " + constants.channel + "\r\n");
						break;
					}
					if (line.indexOf("353") >= 0) {

					}
				}
				while ((line = reader.readLine()) != null) {
					if (line.indexOf("353") >= 0) {
						listAllNick(IRCMessage(line));
						Thread.sleep(100);
						break;
					}
				}
				while ((line = reader.readLine()) != null) {
					if (line.indexOf("366") >= 0) {
						break;
					}
				}
			} catch (IOException e) {
			} catch (InterruptedException e) {
			}
		}
	}

	public class IRCGui extends JFrame {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		public IRCGui() {
			initComponents();
			textArea1.setCaretPosition(textArea1.getDocument().getLength());
		}

		private void button1ActionPerformed(ActionEvent e) {
			performAction();
		}

		private void button2ActionPerformed(ActionEvent e) {
			clearScreen();
		}

		public void clearScreen() {
			try {
				doc.remove(0, doc.getLength());
			} catch (BadLocationException e) {
			}
		}

		private void performAction() {
			if (IRCgui.textField1.getText().equals("/clear")) {
				clearScreen();
			} else if (!IRCgui.textField1.getText().equals("/clear")) {
				irc.sendMessage(IRCgui.textField1.getText());
				try {
					doc.insertString(0, "\n(" + irc.getTimeStamp() + ")<"
							+ nameGUI.textField1.getText() + ">: "
							+ IRCgui.textField1.getText(),
							new SimpleAttributeSet());
				} catch (BadLocationException e1) {
				}
			}
			textField1.setText("");
		}

		private void initComponents() {
			// JFormDesigner - Component initialization - DO NOT MODIFY
			// //GEN-BEGIN:initComponents
			textField1 = new JTextField();
			button1 = new JButton();
			label1 = new JLabel();
			label2 = new JLabel();
			scrollPane2 = new JScrollPane();
			textArea1 = new JTextArea();
			scrollPane3 = new JScrollPane();
			textArea2 = new JTextArea();
			button2 = new JButton();

			// ======== this ========
			setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
			setTitle("UFletch - IRC");
			setIconImage(null);
			setResizable(false);
			addWindowListener(new WindowListener() {
				public void windowClosed(WindowEvent arg0) {
					irc.leave();
				}

				public void windowActivated(WindowEvent arg0) {
				}

				public void windowClosing(WindowEvent arg0) {
				}

				public void windowDeactivated(WindowEvent arg0) {
				}

				public void windowDeiconified(WindowEvent arg0) {
				}

				public void windowIconified(WindowEvent arg0) {
				}

				public void windowOpened(WindowEvent arg0) {
				}
			});
			Container contentPane = getContentPane();
			contentPane.setLayout(null);
			contentPane.add(textField1);
			textField1.setBounds(0, 295, 555, 35);
			textField1.addKeyListener(new KeyListener() {
				@Override
				public void keyPressed(KeyEvent e) {
					if (e.getKeyCode() == KeyEvent.VK_ENTER) {
						performAction();
					}
				}

				@Override
				public void keyReleased(KeyEvent arg0) {
				}

				@Override
				public void keyTyped(KeyEvent arg0) {
				}
			});

			// ---- button1 ----
			button1.setText("Send");
			button1.setFont(button1.getFont().deriveFont(
					button1.getFont().getSize() + 5f));
			button1.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					button1ActionPerformed(e);
				}
			});
			contentPane.add(button1);
			button1.setBounds(555, 295, 75, 35);

			// ---- label1 ----
			label1.setText("Users");
			label1.setFont(label1.getFont().deriveFont(
					label1.getFont().getSize() + 5f));
			contentPane.add(label1);
			label1.setBounds(605, 5, 50, 20);

			// ---- label2 ----
			label2.setText("Chat Area");
			label2.setFont(label2.getFont().deriveFont(
					label2.getFont().getSize() + 5f));
			contentPane.add(label2);
			label2.setBounds(230, 5, 85, 20);

			// ======== scrollPane2 ========
			{

				// ---- textArea1 ----
				textArea1.setEditable(false);
				textArea1.setFont(new Font("Tahoma", Font.PLAIN, 11));
				scrollPane2.setViewportView(textArea1);
			}
			contentPane.add(scrollPane2);
			scrollPane2.setBounds(0, 30, 555, 265);

			// ======== scrollPane3 ========
			{

				// ---- textArea2 ----
				textArea2.setEditable(false);
				textArea2.setFont(new Font("Tahoma", Font.PLAIN, 11));
				scrollPane3.setViewportView(textArea2);
			}
			contentPane.add(scrollPane3);
			scrollPane3.setBounds(560, 30, 140, 265);

			// ---- button2 ----
			button2.setText("Clear");
			button2.setFont(button2.getFont().deriveFont(
					button2.getFont().getSize() + 5f));
			contentPane.add(button2);
			button2.setBounds(630, 295, 70, 35);
			button2.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					button2ActionPerformed(e);
				}
			});

			{ // compute preferred size
				Dimension preferredSize = new Dimension();
				for (int i = 0; i < contentPane.getComponentCount(); i++) {
					Rectangle bounds = contentPane.getComponent(i).getBounds();
					preferredSize.width = Math.max(bounds.x + bounds.width,
							preferredSize.width);
					preferredSize.height = Math.max(bounds.y + bounds.height,
							preferredSize.height);
				}
				Insets insets = contentPane.getInsets();
				preferredSize.width += insets.right;
				preferredSize.height += insets.bottom;
				contentPane.setMinimumSize(preferredSize);
				contentPane.setPreferredSize(preferredSize);
			}
			setSize(710, 355);
			setLocationRelativeTo(getOwner());
			textArea1.setAutoscrolls(true);
			// JFormDesigner - End of component initialization
			// //GEN-END:initComponents
		}

		// JFormDesigner - Variables declaration - DO NOT MODIFY
		// //GEN-BEGIN:variables
		private JTextField textField1;
		private JButton button1;
		private JLabel label1;
		private JLabel label2;
		private JScrollPane scrollPane2;
		private JTextArea textArea1;
		private JScrollPane scrollPane3;
		private JTextArea textArea2;
		private JButton button2;
		// JFormDesigner - End of variables declaration //GEN-END:variables
	}

	public class ircNameGUI extends JFrame {
		private static final long serialVersionUID = 1L;

		public ircNameGUI() {
			initComponents();
		}

		private void initComponents() {
			// JFormDesigner - Component initialization - DO NOT MODIFY
			// //GEN-BEGIN:initComponents
			checkBox1 = new JCheckBox();
			label1 = new JLabel();
			button1 = new JButton();
			textField1 = new JTextField();

			// ======== this ========
			Container contentPane = getContentPane();
			contentPane.setLayout(null);

			// ---- checkBox1 ----
			checkBox1.setText("Use Irc Chat system");
			checkBox1.setFont(new Font("Tahoma", Font.PLAIN, 16));
			contentPane.add(checkBox1);
			checkBox1.setBounds(new Rectangle(new Point(5, 25), checkBox1
					.getPreferredSize()));

			// ---- label1 ----
			label1.setText("Name:");
			label1.setFont(new Font("Tahoma", Font.PLAIN, 16));
			contentPane.add(label1);
			label1.setBounds(new Rectangle(new Point(5, 5), label1
					.getPreferredSize()));

			// ---- button1 ----
			button1.setText("Start");
			button1.setFont(new Font("Tahoma", Font.PLAIN, 16));
			button1.setForeground(Color.blue);
			contentPane.add(button1);
			button1.setBounds(5, 55, 165, 35);
			contentPane.add(textField1);
			textField1.setBounds(55, 5, 110,
					textField1.getPreferredSize().height);
			button1.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					button1ActionPerformed(e);
				}

				private void button1ActionPerformed(ActionEvent e) {
					setVisible(false);
					if (gui.checkBox6.isSelected()) {
						saveSettings();
					}
				}
			});

			{ // compute preferred size
				Dimension preferredSize = new Dimension();
				for (int i = 0; i < contentPane.getComponentCount(); i++) {
					Rectangle bounds = contentPane.getComponent(i).getBounds();
					preferredSize.width = Math.max(bounds.x + bounds.width,
							preferredSize.width);
					preferredSize.height = Math.max(bounds.y + bounds.height,
							preferredSize.height);
				}
				Insets insets = contentPane.getInsets();
				preferredSize.width += insets.right;
				preferredSize.height += insets.bottom;
				contentPane.setMinimumSize(preferredSize);
				contentPane.setPreferredSize(preferredSize);
			}
			pack();
			setLocationRelativeTo(getOwner());
			// JFormDesigner - End of component initialization
			// //GEN-END:initComponents
		}

		// JFormDesigner - Variables declaration - DO NOT MODIFY
		// //GEN-BEGIN:variables
		private JCheckBox checkBox1;
		private JLabel label1;
		private JButton button1;
		private JTextField textField1;
		// JFormDesigner - End of variables declaration //GEN-END:variables
	}

}
