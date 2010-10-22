import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

import javax.imageio.ImageIO;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.WindowConstants;

import org.rsbot.event.events.ServerMessageEvent;
import org.rsbot.event.listeners.PaintListener;
import org.rsbot.event.listeners.ServerMessageListener;
import org.rsbot.script.Script;
import org.rsbot.script.ScriptManifest;
import org.rsbot.script.methods.*;
import org.rsbot.script.wrappers.*;

@ScriptManifest(authors = "RawR", name = "Rawr Ivy Pro", version = 1.0, description = "The best Ivy Chopper out there.")
public class RawrIvy extends Script implements PaintListener, ServerMessageListener {

	public String LOCATION = "", STATE = "Loading.";
	public int AMOUNT_CHOPPED = 0, NESTS_COLLECTED = 0;
	public int[] NEST_ID = {5070, 5071, 5072, 5073, 5074, 5075, 5076, 7413, 11966};
	public int[] IVY_ID = {46318, 46320, 46322, 46324};
	public int[] HATCHETS = {1349, 1351, 1353, 1355, 1357, 1359, 1361, 6739};
	public int[] BANK_BOOTHS = {2213, 4483, 6084, 11402, 11758,
			12759, 14367, 19230, 24914, 25808, 26972, 27663, 29085, 34752, 35647};
	public int[] BANK_CHESTS = {4483, 12308, 21301, 27663, 42192};
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
	public long startTime = System.currentTimeMillis(), waitTimer = System.currentTimeMillis();
	public int START_XP, START_LVL;
	public Image BKG;
	public boolean renew = true;
	public int mode = 1;
	public int CURR_LVL = 0;
	public int NXT_LVL = 0;
	public int percent;
	BufferedImage normal = null;
	BufferedImage clicked = null;
	RawrGUI gui;

	public boolean onStart() {
		log.info(":-:-: Loading data for Rawr Ivy :-:-:");
		log.info("Please wait...");
		try {
			BKG = ImageIO.read(new URL("http://a.imageshack.us/img816/9064/ivychopperpaintfinal.png"));
		} catch (final java.io.IOException e) {
			e.printStackTrace();
		}
		try {
			final URL cursorURL = new URL("http://imgur.com/i7nMG.png");
			final URL cursor80URL = new URL("http://imgur.com/8k9op.png");
			normal = ImageIO.read(cursorURL);
			clicked = ImageIO.read(cursor80URL);
		} catch (MalformedURLException e) {
			log.info("Unable to buffer cursor.");
		} catch (IOException e) {
			log.info("Unable to open cursor image.");
		}
		sleep(random(500, 1000));
		startTime = System.currentTimeMillis();
		waitTimer = System.currentTimeMillis();
		gui = new RawrGUI(this);
		gui.setVisible(true);
		while (!gui.okButtonPressed) {
			log("Waiting for user to finish filling out the GUI...");
			sleep(1000);
		}
		log.info("Starting WC Level: " + skills.getRealLevel(Skills.WOODCUTTING) + ".");
		log.info("=================================");
		log.info("Setting up Camera & Mouse Speed.");
		mouse.setSpeed(random(8, 10));
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

	public void serverMessageRecieved(final ServerMessageEvent e) {
		final String message = e.getMessage();
		if (message.contains("chop away some")) AMOUNT_CHOPPED++;
	}

	public RSObject getIvy() {
		ArrayList<RSObject> ivys = new ArrayList<RSObject>();
		for (int x = -11; x < 12; x++) {
			for (int y = -11; y < 12; y++) {
				RSObject[] all = objects.getAllAt(new RSTile(getMyPlayer().getLocation().getX() + x, getMyPlayer().getLocation().getY() + y));
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

	public boolean clickObject(RSObject obj, String action) {
		if (obj == null) {
			return false;
		}
		RSModel m = obj.getModel();
		for (int i = 0; i < 5; i++) {
			mouse.move(m.getPoint());
			sleep(random(40, 100));
			if (menu.doAction(action)) {
				return true;
			}
		}
		return false;
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
		RSObject bankBooth = objects.getNearest(BANK_BOOTHS),
				bankChest = objects.getNearest(BANK_CHESTS);
		RSNPC banker = npcs.getNearest(6533, 6535);
		if (bankBooth != null) {
			clickObject(bankBooth, "Use-quickly");
			sleep(random(800, 1000));
		} else if (bankChest != null) {
			clickObject(bankChest, "Use");
			sleep(random(800, 1000));
		} else if (banker != null) {
			bank.open();
			sleep(random(800, 1000));
		}
	}

	public void doBank() {
		if (!bank.isOpen()) {
			STATE = "Opening bank.";
			openBank();
		} else if (bank.isOpen()) {
			int RANDOM = random(1, 10);
			if (inventory.getCount() > 1) {
				STATE = "Depositing nests.";
				bank.depositAllExcept(HATCHETS);
				sleep(random(600, 800));
				if (RANDOM == 5) {
					bank.close();
				}
			}
		}
	}

	public void CheckLocation() {
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

	@Override
	public int loop() {
		CheckLocation();
		RSObject IVY = CURRENT_IVY != null && isIvy(CURRENT_IVY) ? CURRENT_IVY : (CURRENT_IVY = getIvy());
		RSGroundItem NEST = groundItems.getNearest(NEST_ID);
		//COLLECTING NEST
		if (NEST != null && !inventory.isFull()) {
			STATE = "Collecting nest.";
			NEST.doAction("Take");
			sleep(random(1000, 1500));
			NESTS_COLLECTED++;
		}
		//CLICKING IVY
		if (!inventory.isFull()) {
			if (IVY != null && (System.currentTimeMillis() - waitTimer) > random(1850, 1950)) {
				if (IVY.isOnScreen()) {
					cameraControl();
					if (clickObject(IVY, "Chop Ivy")) {
						STATE = "Clicking the Ivy.";
						LAST_IVY = IVY;
						sleep(random(2000, 3000));
					} else {
						return 1000;
					}
				}
				//calc.distanceTo(IVY_TILE) >= 8
			} else if (calc.distanceTo(IVY_TILE) >= 7) {
				STATE = "Walking back to Ivy.";
				walking.walkPathMM(walking.reversePath(IVY_TO_BANK), 1, 1);
				while (players.getMyPlayer().isMoving()) {
					sleep(random(200, 500));
				}
			}
		}
		//BANKING
		if (inventory.isFull()) {
			if (LOCATION.equals("Taverly")) {
				log.info("Sorry, your inventory is full... Stopping script.");
				sleep(random(1500, 2500));
				stopScript();
			} else {
				if (!BANK_AREA.contains(players.getMyPlayer().getLocation())) {
					STATE = "Walking to Bank.";
					walking.walkPathMM(IVY_TO_BANK, 1, 1);
					while (players.getMyPlayer().isMoving()) {
						sleep(random(200, 500));
					}
				} else if (BANK_AREA.contains(players.getMyPlayer().getLocation())) {
					doBank();
				}
			}
		}
		//CHOPPING IVY
		if (IVY != null && players.getMyPlayer().getAnimation() != -1) {
			STATE = "Chopping the Ivy.";
			waitTimer = System.currentTimeMillis();
			antiban();
		}
		return 100;
	}

	public void antiban() {
		int randomNum = random(1, 40);
		int r = random(1, 45);
		if (randomNum == 6) {
			if (r == 2) {
				log.info("[RAWR ANTIBAN] Opening random tab.");
				game.openTab(random(1, 14));
			}
			if (r == 3) {
				log.info("[RAWR ANTIBAN] Moving mouse.");
				mouse.moveRandomly(50, 300);
			}
			if (r == 4) {
				log.info("[RAWR ANTIBAN] Moving mouse.");
				mouse.moveRandomly(70, 380);
			}
			if (r == 5) {
				log.info("[RAWR ANTIBAN] Moving mouse off screen.");
				mouse.moveOffScreen();
			}
			if (r == 6) {
				log.info("[RAWR ANTIBAN] Moving mouse off screen.");
				mouse.moveOffScreen();
			}
			if (r == 7) {
				log.info("[RAWR ANTIBAN] Turning camera slightly.");
				camera.setAngle(random(100, 120));
			}
			if (r == 8) {
				log.info("[RAWR ANTIBAN] Turning camera slightly.");
				camera.setAngle(random(190, 230));
			}
			if (r == 9) {
				log.info("[RAWR ANTIBAN] Turning camera slightly.");
				camera.setAngle(random(150, 180));
			}
			if (r == 10) {
				log.info("[RAWR ANTIBAN] Turning camera slightly.");
				camera.setAngle(random(250, 260));
			}
			if (r == 11) {
				log.info("[RAWR ANTIBAN] Checking inventory.");
				if (game.getCurrentTab() != 4) {
					game.openTab(4);
				} else return;
			}
			if (r == 35) {
				log.info("[RAWR ANTIBAN] Checking Woodcutting XP.");
				if (game.getCurrentTab() != 1) {
					game.openTab(1);
					sleep(random(500, 700));
					mouse.move(random(680, 730), random(355, 370));
					sleep(random(1500, 5000));
				}
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
		final Point Mouse = new Point(mouse.getLocation().x, mouse.getLocation().y);
		final Rectangle toggleRectangle = new Rectangle(497, 458, 15, 14);
		if (renew && toggleRectangle.contains(Mouse) && (mouse.isPressed())) {
			if (mode == 1) {
				mode = 2;
				renew = false;
			} else if (mode == 2) {
				mode = 3;
				renew = false;
			} else if (mode == 3) {
				mode = 1;
				renew = false;
			}
		}
		if (!mouse.isPressed()) {
			renew = true;
		}
		g.setColor(new Color(50, 250, 50, 130));
		g.fillRect(497, 458, 15, 14);
		g.setColor(new Color(0, 0, 0, 255));
		g.drawRect(497, 458, 15, 14);
		g.setColor(new Color(0, 0, 0, 255));
		g.drawString("" + mode, 502, 470);
		//Mode 1
		if (mode == 1) {
			//BKG
			g.drawImage(BKG, 13, 4, null);
			//Clock
			g.setColor(Color.WHITE);
			g.setFont(new Font("Verdana", 0, 13));
			g.drawString("" + hours + ":" + minutes + ":" + seconds + ".", 311, 82);
			g.setFont(new Font("Verdana", 0, 9));
			g.drawString("Currently : " + STATE, 282, 105);
			//Nests
			g.drawString("Collected : " + NESTS_COLLECTED + ".", 311, 129);
			//WC'ing Info
			//Proggy bar
			g.setColor(new Color(0, 0, 0));
			g.fillRect(96, 61, 180, 16);//Black box
			g.drawRect(95, 60, 181, 17); //FRAME
			g.setColor(new Color(51, 51, 51));
			g.fillRect(96, 61, 180, 8);//Gray box
			g.setColor(new Color(10, 150, 10, 130));
			g.fillRect(97, 62, (int) (percent * 178 / 100.0), 14);
			g.setColor(Color.WHITE);
			g.drawString("" + percent + "% to " + NXT_LVL + " WC" + " - " + XP_TNL / 1000 + "k XP", 124, 73);
			//TNL
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
			//General
			g.setColor(Color.ORANGE);
			g.drawString("Chopped: " + AMOUNT_CHOPPED + " Ivy.", 95, 108);
			g.setColor(Color.WHITE);
			g.drawString("XP: " + XP_GAINED + ".  (" + XP_HOUR / 1000 + "k XP/H)", 95, 120);
			g.setColor(Color.GREEN);
			g.drawString("WC Level : " + CURR_LVL + " [ " + LVLS_GAINED + " ].", 95, 132);
		}
		//Mode 2
		if (mode == 2) {
			final int Y = 190;
			//BKG
			g.drawImage(BKG, 13, 4 + Y, null);
			//Clock
			g.setColor(Color.WHITE);
			g.setFont(new Font("Verdana", 0, 13));
			g.drawString("" + hours + ":" + minutes + ":" + seconds + ".", 311, 82 + Y);
			g.setFont(new Font("Verdana", 0, 9));
			g.drawString("Currently : " + STATE, 282, 105 + Y);
			//Nests
			g.drawString("Collected : " + NESTS_COLLECTED + ".", 311, 129 + Y);
			//WC'ing Info
			//Proggy bar
			g.setColor(new Color(0, 0, 0));
			g.fillRect(96, 61 + Y, 180, 16);//Black box
			g.drawRect(95, 60 + Y, 181, 17); //FRAME
			g.setColor(new Color(51, 51, 51));
			g.fillRect(96, 61 + Y, 180, 8);//Gray box
			g.setColor(new Color(10, 150, 10, 130));
			g.fillRect(97, 62 + Y, (int) (percent * 178 / 100.0), 14);
			g.setColor(Color.WHITE);
			g.drawString("" + percent + "% to " + NXT_LVL + " WC" + " - " + XP_TNL / 1000 + "k XP", 124, 73 + Y);
			//TNL
			try {
				if (XP_HOUR > 0) {
					long sTNL = (XP_TNL) / (XP_HOUR / 3600);
					long hTNL = sTNL / (60 * 60);
					sTNL -= hTNL * (60 * 60);
					long mTNL = sTNL / 60;
					sTNL -= mTNL * 60;
					g.drawString("Next level in: " + hTNL + ":" + mTNL + ":" + sTNL, 95, 89 + Y);
				} else {
					g.drawString("Next level in: 0:0:0", 95, 89 + Y);
				}
			} catch (Exception e) {
				g.drawString("Next level in: -1:-1:-1", 95, 89 + Y);
			}
			//General
			g.setColor(Color.ORANGE);
			g.drawString("Chopped: " + AMOUNT_CHOPPED + " Ivy.", 95, 108 + Y);
			g.setColor(Color.WHITE);
			g.drawString("XP: " + XP_GAINED + ".  (" + XP_HOUR / 1000 + "k XP/H)", 95, 120 + Y);
			g.setColor(Color.GREEN);
			g.drawString("WC Level : " + CURR_LVL + " [ " + LVLS_GAINED + " ].", 95, 132 + Y);

		}
		//Mode 3
		if (mode == 3) {
			g.setFont(new Font("Verdana", 0, 11));
			g.setColor(Color.GREEN);
			g.drawString("----->", 450, 469);
		}
	}

	public class RawrGUI extends JFrame {
		public static final long serialVersionUID = 31653L;
		public boolean okButtonPressed = false;
		public RawrIvy bbb;

		public RawrGUI(RawrIvy bbb) {
			this.bbb = bbb;
			initComponents();
		}

		private void Cancel(ActionEvent e) {
			setVisible(false);
			okButtonPressed = true;
		}

		private void Start(ActionEvent e) {
			setVisible(false);
			okButtonPressed = true;

		}

		private void initComponents() {
			label = new JLabel();
			button1 = new JButton();
			button2 = new JButton();
			loc = new JComboBox();
			setBackground(Color.GREEN);
			setAlwaysOnTop(false);
			setTitle("Rawr Ivy");
			setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
			Container contentPane = getContentPane();
			contentPane.setLayout(null);
			//TITLE
			label.setText("Rawr Ivy");
			label.setFont(new Font("Trebuchet MS", Font.BOLD, 22));
			contentPane.add(label);
			label.setBounds(new Rectangle(new Point(15, 5), label.getPreferredSize()));
			//LOCATION
			loc.setModel(new DefaultComboBoxModel(new String[]{
					"Varrock Wall",
					"Varrock Palace",
					"N Fally",
					"S Fally",
					"Ardougne",
					"Yanille",
					"CWars"
			}));
			contentPane.add(loc);
			loc.setBounds(new Rectangle(new Point(15, 30), loc.getPreferredSize()));
			//START BUTTON
			button1.setText("Start Script");
			button1.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					LOCATION = (String) loc.getSelectedItem();
					Start(e);
				}
			});
			contentPane.add(button1);
			button1.setBounds(new Rectangle(new Point(15, 50), button1.getPreferredSize()));
			//CANCEL BUTTON
			button2.setText("Cancel");
			button2.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					Cancel(e);
				}
			});
			contentPane.add(button2);
			button2.setBounds(new Rectangle(new Point(15, 70), button2.getPreferredSize()));
			{
				Dimension preferredSize = new Dimension();
				for (int i = 0; i < contentPane.getComponentCount(); i++) {
					Rectangle bounds = contentPane.getComponent(i).getBounds();
					preferredSize.width = Math.max(bounds.x + bounds.width, preferredSize.width);
					preferredSize.height = Math.max(bounds.y + bounds.height, preferredSize.height);
				}
				Insets insets = contentPane.getInsets();
				preferredSize.width += insets.right;
				preferredSize.height += insets.bottom;
				contentPane.setMinimumSize(preferredSize);
				contentPane.setPreferredSize(preferredSize);
			}
			pack();
			setLocationRelativeTo(getOwner());
		}

		private JLabel label;
		private JButton button1;
		private JButton button2;
		private JComboBox loc;
	}
}