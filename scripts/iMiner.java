/*
 * Copyright (c) 2011 TehGamer 
 */
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.SystemColor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import javax.imageio.ImageIO;
import javax.swing.DefaultComboBoxModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JTextPane;

import org.rsbot.event.events.ServerMessageEvent;
import org.rsbot.event.listeners.PaintListener;
import org.rsbot.event.listeners.ServerMessageListener;
import org.rsbot.script.Script;
import org.rsbot.script.ScriptManifest;
import org.rsbot.script.util.Timer;
import org.rsbot.script.wrappers.RSArea;
import org.rsbot.script.wrappers.RSObject;
import org.rsbot.script.wrappers.RSTile;

@SuppressWarnings("deprecation")
@ScriptManifest(authors = { "TehGamer" }, keywords = "Mining", name = "iMiner", version = 2.3, description = "Ultimate Iron Miner!")
public class iMiner extends Script implements PaintListener,
		ServerMessageListener {

	iMinerGUI gui;
	public boolean guiWait = true, guiExit;

	private enum State {
		WhileMining, Mine, Drop, Banking, BoxBank, WalkToBank, WalkToMine, WalkToRock, Sleep
	}

	boolean ScriptRunning;
	public int mined;
	public int gained;
	public int[] RockID;
	public int[] NonDropIDs = { 1265, 1267, 1269, 1296, 1273, 1271, 1275,
			15259, 15532, 15533 };
	public int[] PickIDs = { 1265, 1267, 1269, 1296, 1273, 1271, 1275, 15259 };
	public int[] BankBoothID = { 11402, 2213, 2215 };
	public int[] BankBoxID = { 36788 };
	public String Location;
	public String Mode;
	public RSTile[] toBank;
	public RSTile[] toMine;
	public int gem;
	RSArea Mine;
	RSArea Bank;
	RSTile RockLoc;
	public ArrayList<Integer> dead = new ArrayList<Integer>();

	// RsObjects
	RSObject Rock;
	RSObject BankBooth;
	RSObject BankBox;

	// Threads
	public Thread Antiban = new Thread(new AntiBan());
	public int IronPrice = 0;

	// Paint
	private Timer BotTime = new Timer(0);
	public int MoneyGained;
	public int oldmineExp;
	public int startmineExp;
	public int startmineLevel;
	public int oldmineLevel;
	public int minegain;
	public int currentlevel;
	public int mineexp;
	public int minenext;
	public int nextlevel;
	public int mineExp;
	public int percenttolevel;
	public int startLevel = 0;
	public int startXP = 0;
	public long starttime;
	public long startTime = System.currentTimeMillis();
	String status;
	public RSObject lastclicked;
	public BufferedImage paintPic = null;

	private void CheckVersion() {
		URLConnection url = null;
		BufferedReader in = null;
		try {
			url = new URL("http://tehgamer.info/AutoIron/Version.txt")
					.openConnection();
			in = new BufferedReader(new InputStreamReader(url.getInputStream()));
			double newVer = Double.parseDouble(in.readLine());
			double currentVer = iMiner.class
					.getAnnotation(ScriptManifest.class).version();
			if (newVer > currentVer) {
				log("[UPDATE]iMiner v" + newVer + " is available!");
				log("[UPDATE]Please check the thread for the updated version.");
			}
		} catch (IOException e) {
			log("Unable to check for update.");
		}
	}

	public boolean onStart() {
		ScriptRunning = true;
		log.info("Loading Resources for iMiner. This may take moment.");
		CheckVersion();
		IronPrice = grandExchange.lookup(440).getGuidePrice();
		try {
			// Image Loader
			final URL url = new URL("http://tehgamer.info/AutoIron/Paint.png");
			paintPic = ImageIO.read(url);
		} catch (final IOException e) {
			log("Failed to upload Pic for paint.");
			e.printStackTrace();
		}
		gui = new iMinerGUI();
		gui.setVisible(true);
		log.info("[GUI Started]Please enter you settings.");
		while (guiWait) {
			sleep(100);
		}
		// Array
		dead.add(9723);
		dead.add(9724);
		dead.add(9725);
		dead.add(11533);
		dead.add(11552);
		dead.add(11553);
		dead.add(11554);
		dead.add(11555);
		dead.add(11556);
		dead.add(11557);
		if (Location.equals("East Varrock")) {
			Bank = new RSArea(new RSTile(3250, 3420), new RSTile(3257, 3423));
			Mine = new RSArea(new RSTile(3283, 3366), new RSTile(3289, 3373));
			RockID = new int[] { 11956, 11954, 11955 };
			toMine = new RSTile[] { new RSTile(3253, 3421),
					new RSTile(3254, 3428), new RSTile(3264, 3428),
					new RSTile(3277, 3428), new RSTile(3285, 3426),
					new RSTile(3288, 3417), new RSTile(3291, 3409),
					new RSTile(3292, 3401), new RSTile(3291, 3391),
					new RSTile(3292, 3383), new RSTile(3292, 3377),
					new RSTile(3287, 3373), new RSTile(3285, 3370) };
			toBank = walking.reversePath(toMine);
		}
		if (Location.equals("West Varrock")) {
			Bank = new RSArea(new RSTile(3182, 3433), new RSTile(3189, 3446));
			Mine = new RSArea(new RSTile(3173, 3364), new RSTile(3178, 3370));
			RockID = new int[] { 11956, 11955 };
			toMine = new RSTile[] { new RSTile(3183, 3436),
					new RSTile(3184, 3430), new RSTile(3176, 3428),
					new RSTile(3172, 3426), new RSTile(3172, 3419),
					new RSTile(3172, 3414), new RSTile(3172, 3407),
					new RSTile(3172, 3398), new RSTile(3176, 3392),
					new RSTile(3178, 3383), new RSTile(3183, 3375),
					new RSTile(3179, 3368), new RSTile(3175, 3367) };
			toBank = walking.reversePath(toMine);
		}
		if (Location.equals("Rimmington")) {
			Bank = new RSArea(new RSTile(3045, 3268), new RSTile(3049, 3234));
			Mine = new RSArea(new RSTile(2966, 3235), new RSTile(2973, 3244));
			RockID = new int[] { 9717, 9718, 9719 };
			toMine = new RSTile[] { new RSTile(3046, 3236),
					new RSTile(3035, 3236), new RSTile(3026, 3242),
					new RSTile(3014, 3242), new RSTile(3001, 3238),
					new RSTile(2990, 3232), new RSTile(2979, 3234),
					new RSTile(2971, 3240) };

			toBank = walking.reversePath(toMine);
		}
		if (Location.equals("Yanille (North)")) {
			camera.setPitch(random(2007, 3007));
			Bank = new RSArea(new RSTile(2609, 3090), new RSTile(2613, 3096));
			Mine = new RSArea(new RSTile(2622, 3147), new RSTile(2627, 3153));
			RockID = new int[] { 37308, 37309 };
			toMine = new RSTile[] { new RSTile(2611, 3093),
					new RSTile(2613, 3103), new RSTile(2619, 3113),
					new RSTile(2625, 3122), new RSTile(2623, 3133),
					new RSTile(2623, 3143), new RSTile(2624, 3148) };

			toBank = walking.reversePath(toMine);
		}
		if (Location.equals("Yanille (South)")) {
			Bank = new RSArea(new RSTile(2612, 3092), new RSTile(2613, 3096));
			Mine = new RSArea(new RSTile(2625, 3138), new RSTile(2629, 3144));
			RockID = new int[] { 37308, 37309 };
			toMine = new RSTile[] { new RSTile(2611, 3093),
					new RSTile(2613, 3103), new RSTile(2619, 3113),
					new RSTile(2625, 3122), new RSTile(2623, 3133),
					new RSTile(2626, 3140) };

			toBank = walking.reversePath(toMine);
		}
		if (Location.equals("World Wide")) {
			// TODO Get all iron rock id's
			RockID = new int[] { 37307, 37308, 37309, 11954, 11955, 11956,
					9717, 9718, 9719, 5773, 5774, 5775, 2092, 2093 };
		}
		BotTime.reset();
		return !guiExit;
	}

	public void onFinish() {
		ScriptRunning = false;
		log.info("Thank you for using iMiner!");
		if (mined > 0) {
			log.info("-Your Report-");
			log.info("Total Run Time: " + BotTime.toElapsedString());
			log.info("Iron Mined: " + mined);
			log.info("XP Gained: " + mineexp);
			log.info("Level's Gained: " + gained);
		}
	}

	// *******************************************************//
	// LOOP
	// *******************************************************//
	@Override
	public int loop() {
		if (!Antiban.isAlive()) {
			Antiban.start();
		}
		mouse.setSpeed(random(5, 7));
		if (getMyPlayer().getAnimation() == -1)
			lastclicked = null;
		switch (getState()) {
		case WhileMining:
			status = "Checking";
			if (Antideadrock() && getMyPlayer().getAnimation() != -1) {
				status = "Dead rock detected";
				Mine();
			}
			break;
		case Mine:
			Rock = objects.getNearest(RockID);
			if (Rock != null && MineLocation()
					&& getMyPlayer().getAnimation() == -1) {
				Mine();
			}
			break;
		case Drop:
			mouse.setSpeed(random(8, 9));
			inventory.dropAllExcept(NonDropIDs);
			break;
		case Banking:
			BankBooth = objects.getNearest(BankBoothID);
			mouse.setSpeed(random(8, 9));
			if (BankBooth == null)
				;
			if (Bank.contains(getMyPlayer().getLocation())
					&& inventory.isFull() && !BankBooth.isOnScreen()
					&& BankBooth != null)
				BoothAngle();
			if (!bank.isOpen() && BankBooth != null) {
				if (BankBooth == null)
					;
				status = "Banking";
				BankBooth.doAction("use-quickly");
				sleep(random(1500, 1700));
				if (!bank.isOpen())
					sleep(random(1000, 1500));
			} else {
				if (bank.isOpen()) {
					bank.depositAllExcept(PickIDs);
					bank.depositAllExcept(PickIDs);
					bank.depositAllExcept(PickIDs);
				} else {
					if (bank.isOpen() && inventory.getCount() <= 1)
						;
					bank.close();
				}
			}
			break;
		case BoxBank:
			RSObject BankBox = objects.getNearest(BankBoxID);
			mouse.setSpeed(random(8, 9));
			if (BankBox == null)
				;
			if (Bank.contains(getMyPlayer().getLocation())
					&& inventory.isFull() && !BankBox.isOnScreen()
					&& BankBox != null)
				BoxAngle();
			if (!bank.isDepositOpen() && BankBox != null) {
				if (BankBox == null)
					;
				status = "Banking";
				bank.openDepositBox();
				sleep(random(1500, 1700));
				if (!bank.isDepositOpen())
					sleep(random(1000, 1500));
			} else {
				if (bank.isDepositOpen()) {
					bank.depositAllExcept(PickIDs);
					bank.depositAllExcept(PickIDs);
				} else {
					if (bank.isDepositOpen() && bank.getBoxCount() <= 1) {
						bank.close();
					}
				}
			}
			break;
		case WalkToRock:
			Rock = objects.getNearest(RockID);
			if (Rock == null)
				;
			RockLoc = Rock.getLocation();
			if (RockLoc == null)
				;
			if (calc.distanceTo(Rock) > 5 && Rock != null && !Rock.isOnScreen()
					&& !inventory.isFull() && CorrectRock()) {
				if (calc.tileOnMap(RockLoc)) {
					walking.walkTileMM(RockLoc, 1, 1);
				} else {
					walking.walkTo(RockLoc);
				}
			}
			Mine();
			break;
		case WalkToBank:
			walk(toBank);
			sleep(random(400, 600));
			break;
		case WalkToMine:
			walk(toMine);
			sleep(random(400, 600));
			break;
		}
		return 0;
	}

	// *******************************************************//
	// GET STATE
	// *******************************************************//
	private State getState() {
		// TODO Improve Box Banking Loop
		if (Location.equals("Rimmington")) {
			if (bank.isDepositOpen() && bank.getBoxCount() < 2)
				return State.WalkToMine;
			if (bank.isOpen() && inventory.getCount() < 2)
				return State.WalkToMine;
			if (bank.isDepositOpen() && bank.getBoxCount() > 1
					|| inventory.isFull()) {
				if (Mode.equals("Bank")) {
					if (Bank.contains(getMyPlayer().getLocation())) {
						return State.BoxBank;
					}
					return State.WalkToBank;
				}
				return State.Drop;
			} else if (Mine.contains(getMyPlayer().getLocation())) {
				if (getMyPlayer().getAnimation() != -1 && lastclicked != null) {
					return State.WhileMining;
				}
				Rock = objects.getNearest(RockID);
				if (getMyPlayer().getAnimation() == -1 && Rock != null
						|| Antideadrock() && Rock != null) {
					if (!Rock.isOnScreen() && Rock.getLocation() != null)
						return State.WalkToRock;
					return State.Mine;
				}
			} else if (!Mine.contains(getMyPlayer().getLocation())) {
				return State.WalkToMine;
			}
			return State.Sleep;
		} else {
			// Normal Loop
			if (inventory.isFull()) {
				if (Mode.equals("Bank")) {
					if (Bank.contains(getMyPlayer().getLocation())) {
						return State.Banking;
					}
					return State.WalkToBank;
				}
				return State.Drop;
			} else if (MineLocation()) {
				if (getMyPlayer().getAnimation() != -1 && lastclicked != null) {
					return State.WhileMining;
				}
				Rock = objects.getNearest(RockID);
				if (getMyPlayer().getAnimation() == -1 && Rock != null
						|| Antideadrock() && Rock != null) {
					if (!Rock.isOnScreen() && Rock.getLocation() != null)
						return State.WalkToRock;
					return State.Mine;
				}
			} else if (!MineLocation()) {
				return State.WalkToMine;
			}
			return State.Sleep;
		}
	}

	// *******************************************************//
	// METHODS
	// *******************************************************//

	public boolean MineLocation() {
		Rock = objects.getNearest(RockID);
		if (Rock == null)
			;
		if (!Location.equals("World Wide")) {
			if (Mine.contains(getMyPlayer().getLocation()))
				return true;
		}
		if (Location.equals("World Wide")) {
			if (Rock != null && calc.distanceTo(Rock) < 7)
				return true;
		}
		return false;
	}

	public boolean CorrectRock() {
		Rock = objects.getNearest(RockID);
		if (Rock == null)
			;
		if (!Location.equals("World Wide")) {
			if (Mine.contains(getMyPlayer().getLocation())
					&& Mine.contains(Rock.getLocation()))
				return true;
		}
		if (Location.equals("World Wide")) {
			if (Rock != null && calc.distanceTo(Rock) < 7)
				return true;
		}
		return false;
	}

	public boolean BoothAngle() {
		BankBooth = objects.getNearest(BankBoothID);
		if (Bank.contains(getMyPlayer().getLocation()) && inventory.isFull()
				&& !BankBooth.isOnScreen()) {
			int BoothAngle = camera.getObjectAngle(BankBooth);
			camera.setAngle(BoothAngle);
			if (!BankBooth.isOnScreen())
				return false;
		}
		return true;
	}

	public boolean BoxAngle() {
		RSObject BankBox = objects.getNearest(BankBoxID);
		if (Bank.contains(getMyPlayer().getLocation()) && inventory.isFull()
				&& !BankBox.isOnScreen()) {
			int BoxAngle = camera.getObjectAngle(BankBox);
			camera.setAngle(BoxAngle);
			if (!BankBox.isOnScreen())
				return false;
		}
		return true;
	}

	@SuppressWarnings("static-access")
	public boolean Antideadrock() {
		if (lastclicked != null) {
			RSObject[] clickedrock = objects.getAt(lastclicked.getLocation(),
					objects.TYPE_INTERACTABLE);
			for (RSObject clickedrock2 : clickedrock)
				if (dead.contains(clickedrock2.getID()))
					return true;
		}
		return false;
	}

	private boolean walk(RSTile[] path) {
		status = "Walking";
		if (calc.distanceTo(walking.getDestination()) < random(6, 8)) {
			if (walking.walkPathMM(path, 2, 2)) {
			}
		}
		return true;
	}

	public boolean Mine() {
		Rock = objects.getNearest(RockID);
		if (Rock == null) {
			return false;
		} else if (Rock != null && !inventory.isFull() && MineLocation()) {
			double location = random(0.4, 0.7);
			double location2 = random(0.4, 0.7);
			tiles.doAction(Rock.getLocation(), location, location2, 0, "Mine");
			lastclicked = Rock;
			status = "Mining";
			sleep(random(1700, 1900));
			if (getMyPlayer().isMoving())
				sleep(random(1000, 1500));
			if (getMyPlayer().isMoving())
				sleep(random(1000, 1500));
			return true;
		}
		return false;
	}

	private boolean logout() {
		mouse.move(754, 10, 4, 4);
		sleep(random(100, 300));
		mouse.click(true);
		sleep(random(100, 300));
		mouse.move(641, 373, 15, 4);
		sleep(random(100, 300));
		mouse.click(true);
		sleep(random(100, 300));
		return true;
	}

	// Makes sure camera view isn't blocked by wall
	public boolean AntiWall() {
		int angle = camera.getAngle();
		if (Location.equals("East Varrock")) {
			if (angle > 156 && angle < 215
					&& Mine.contains(getMyPlayer().getLocation())) {
				RotateCamera();
			}
		}
		return true;
	}

	private int RotateCamera() {
		int angle = camera.getAngle() + random(-90, 90);
		camera.setAngle(angle);
		return 0;
	}

	public boolean Failsafes() {
		// Checks for no pick axe
		if (game.isLoggedIn() && !inventory.containsOneOf(PickIDs)
				&& !equipment.containsOneOf(PickIDs)) {
			sleep(1000, 2000);
			if (game.isLoggedIn() && !inventory.containsOneOf(PickIDs)
					&& !equipment.containsOneOf(PickIDs)) {
				log.warning("You haven't got a Pickaxe.");
				logout();
				stopScript();
				return false;
			}
		}
		// Checks for wrong mining level
		if (game.isLoggedIn() && currentlevel < 15) {
			sleep(1000, 2000);
			if (game.isLoggedIn() && currentlevel < 15) {
				log.warning("You haven't got a high enough Mining level.");
				logout();
				stopScript();
				return false;
			}
		}
		return true;
	}

	// *******************************************************//
	// Thread's
	// *******************************************************//

	// TODO Update AntiBan
	// AntiBan Thread
	public class AntiBan implements Runnable {
		public void run() {
			while (ScriptRunning) {
				AntiWall();
				// Run
				if (walking.getEnergy() >= (random(20, 40))
						&& !walking.isRunEnabled()) {
					walking.setRun(true);
				}
				// Update coming soon
				int Random = random(0, 125);
				switch (Random) {
				case 1:
					int angle = camera.getAngle() + random(-90, 90);
					log("Antiban: Rotating camera to " + angle + " degrees.");
					camera.setAngle(angle);
					break;
				}
				sleep(random(5000, 7000));
			}
		}
	}

	// *******************************************************//
	// Server Message's
	// *******************************************************//
	public void serverMessageRecieved(final ServerMessageEvent arg0) {
		final String serverString = arg0.getMessage();
		if (serverString.contains("You manage to mine some iron")) {
			mined++;
		}
		if (serverString.contains("You just found a")) {
			gem++;
		}
		if (serverString.contains("You've just advanced")) {
			log("Congratulations on level up!");
			gained++;
		}
	}

	// *******************************************************//
	// GUI
	// *******************************************************//
	// Version 2.0
	public class iMinerGUI extends JFrame {
		private static final long serialVersionUID = 1L;

		public iMinerGUI() {
			initComponents();
		}

		private void comboBox1ActionPerformed(ActionEvent e) {
			Location = comboBox1.getSelectedItem().toString();
			if (Location.equals("World Wide")) {
				comboBox2.setModel(new DefaultComboBoxModel(
						new String[] { "PowerMine" }));
			} else {
				comboBox2.setModel(new DefaultComboBoxModel(new String[] {
						"Bank", "PowerMine" }));
			}
		}

		private void button1ActionPerformed(ActionEvent e) {
			Location = comboBox1.getSelectedItem().toString();
			Mode = comboBox2.getSelectedItem().toString();
			guiWait = false;
			dispose();
		}

		private void initComponents() {
			label3 = new JLabel();
			tabbedPane6 = new JTabbedPane();
			textPane2 = new JTextPane();
			panel3 = new JPanel();
			label2 = new JLabel();
			comboBox1 = new JComboBox();
			comboBox2 = new JComboBox();
			label10 = new JLabel();
			label1 = new JLabel();
			button1 = new JButton();

			// ======== this ========
			setTitle("AutoIron");
			setAlwaysOnTop(true);
			setResizable(false);
			Container contentPane = getContentPane();
			contentPane.setLayout(null);

			// ---- label3 ----
			label3.setEnabled(false);
			contentPane.add(label3);
			label3.setBounds(0, 235, 65, 45);

			// ======== tabbedPane6 ========
			{
				tabbedPane6.setBorder(null);

				// ---- textPane2 ----
				textPane2.setBackground(SystemColor.menu);
				textPane2.setEditable(false);
				textPane2
						.setText("I'm iMiner one of the most advanced Rsbot Miners in the World. I will get you the Mining levels you have always wanted. Use the settings tab to set your settings. Then watch me train for you!");
				tabbedPane6.addTab("Intro", textPane2);

				// ======== panel3 ========
				{
					panel3.setBorder(null);
					panel3.setLayout(null);

					// ---- label2 ----
					label2.setFont(new Font("Tahoma", Font.PLAIN, 14));
					label2.setText("Location:");
					panel3.add(label2);
					label2.setBounds(5, 10, 90, 25);

					// ---- comboBox1 ----
					comboBox1.setMaximumRowCount(10);
					comboBox1.setModel(new DefaultComboBoxModel(
							new String[] { "East Varrock", "West Varrock",
									"Rimmington", "Yanille (North)",
									"Yanille (South)", "World Wide" }));
					comboBox1.addActionListener(new ActionListener() {
						public void actionPerformed(ActionEvent e) {
							comboBox1ActionPerformed(e);
						}
					});
					panel3.add(comboBox1);
					comboBox1.setBounds(80, 10, 155, 25);

					// ---- comboBox2 ----
					comboBox2.setModel(new DefaultComboBoxModel(new String[] {
							"Bank", "PowerMine" }));
					panel3.add(comboBox2);
					comboBox2.setBounds(115, 50, 118, 25);

					// ---- label10 ----
					label10.setText("Mode:");
					label10.setFont(new Font("Tahoma", Font.PLAIN, 14));
					panel3.add(label10);
					label10.setBounds(5, 50, label10.getPreferredSize().width,
							25);

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
				tabbedPane6.addTab("Settings", panel3);

			}
			contentPane.add(tabbedPane6);
			tabbedPane6.setBounds(0, 120, 245, 115);

			// ---- label1 ----
			try {
				label1.setIcon(new ImageIcon(new URL(
						"http://tehgamer.info/AutoIron/Banner.png")));
			} catch (final IOException e) {
				log("Failed to upload Pic for GUI.");
				e.printStackTrace();
			}
			contentPane.add(label1);
			label1.setBounds(0, -5, 245, 135);

			// ---- button1 ----
			button1.setText("Start");
			button1.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					button1ActionPerformed(e);
				}
			});
			contentPane.add(button1);
			button1.setBounds(5, 240, 235, 35);

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
			// GEN-END:initComponents
		}

		private JLabel label3;
		private JTabbedPane tabbedPane6;
		private JTextPane textPane2;
		private JPanel panel3;
		private JLabel label2;
		private JComboBox comboBox1;
		private JComboBox comboBox2;
		private JLabel label10;
		private JLabel label1;
		private JButton button1;
		// GEN-END:variables
	}

	// *******************************************************//
	// PAINT SCREEN
	// *******************************************************//
	public void onRepaint(final Graphics g) {
		final Point loc = mouse.getLocation();
		MoneyGained = (IronPrice * mined);
		mineexp = skills.getCurrentExp(14) - startmineExp;
		minenext = skills.getExpToNextLevel(14);
		currentlevel = skills.getCurrentLevel(14);
		percenttolevel = skills.getPercentToNextLevel(14);
		nextlevel = currentlevel + 1;
		if (startmineExp == 0) {
			startmineExp = skills.getCurrentExp(14);
			oldmineExp = 0;
		}
		if (startmineExp == 0) {
			startmineExp = skills.getCurrentExp(14);
			oldmineExp = 0;
		}
		final int xpHr = (int) ((mineexp) * 3600000D / (System
				.currentTimeMillis() - startTime));
		final int oreHr = (int) ((mined) * 3600000D / (System
				.currentTimeMillis() - startTime));
		// Mouse lines
		g.setColor(new Color(0, 0, 0, 170));
		g.drawLine(0, loc.y, 766, loc.y);
		g.drawLine(loc.x, 0, loc.x, 505);
		// paint
		int row1 = 255;
		int row2 = 380;
		g.setColor(Color.white);
		g.drawImage(paintPic, 4, 211, null);
		g.setFont(new Font("Arial", Font.PLAIN, 10));
		g.drawString("Version: " + "2.3", row1, 255);
		g.drawString("Run Time: " + BotTime.toElapsedString(), row1, 270);
		g.drawString("Mine: " + Location, row1, 285);
		g.drawString("Mode: " + Mode, row1, 300);
		g.drawString("Iron Mined: " + mined, row1, 315);
		g.drawString("Iron/Hour: " + oreHr, row1, 330);
		g.drawString("XP Gained: " + mineexp, row2, 255);
		g.drawString("XP/Hour: " + xpHr, row2, 270);
		g.drawString("Xp To Level: " + minenext, row2, 285);
		g.drawString("Levels Gained: " + gained, row2, 300);
		g.drawString("Gems Found: " + gem, row2, 315);
		if (Mode.equals("Bank"))
			g.drawString("Money Earned: " + MoneyGained, row2, 330);
	}
}
