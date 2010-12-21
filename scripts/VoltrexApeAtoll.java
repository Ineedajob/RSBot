import org.rsbot.event.listeners.PaintListener;
import org.rsbot.script.Script;
import org.rsbot.script.ScriptManifest;
import org.rsbot.script.methods.Skills;
import org.rsbot.script.util.Timer;
import org.rsbot.script.wrappers.RSArea;
import org.rsbot.script.wrappers.RSObject;
import org.rsbot.script.wrappers.RSPlayer;
import org.rsbot.script.wrappers.RSTile;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;


@ScriptManifest(authors = {"Voltrex"}, name = "Voltrex Ape Atoll Agility", version = 1.35,
		description = "Trains at Ape Atoll; eats all food, and cuts pineapples if you have a knife.")
public class VoltrexApeAtoll extends Script implements PaintListener {

	private enum State {
		recover1, stone, tree1, bars, rocks, rope, tree2, tostart, getpineapple, cutpineapple, error
	}

	private final ScriptManifest properties = getClass().getAnnotation(ScriptManifest.class);
	private final int pineapple = 2114;
	private final int knife = 946;
	private final int[] dropItems = {2313, 1923};
	private final int[] food = {1895, 1893, 1891, 4293, 2142, 291, 2140, 3228, 9980,
			7223, 6297, 6293, 6295, 6299, 7521, 9988, 7228, 2878, 7568, 2343,
			1861, 13433, 315, 325, 319, 3144, 347, 355, 333, 339, 351, 329,
			3381, 361, 10136, 5003, 379, 365, 373, 7946, 385, 397, 391, 3369,
			3371, 3373, 2309, 2325, 2333, 2327, 2331, 2323, 2335, 7178, 7180,
			7188, 7190, 7198, 7200, 7208, 7210, 7218, 7220, 2003, 2011, 2289,
			2291, 2293, 2295, 2297, 2299, 2301, 2303, 1891, 1893, 1895, 1897,
			1899, 1901, 7072, 7062, 7078, 7064, 7084, 7082, 7066, 7068, 1942,
			6701, 6703, 7054, 6705, 7056, 7060, 2130, 1985, 1993, 1989, 1978,
			5763, 5765, 1913, 5747, 1905, 5739, 1909, 5743, 1907, 1911, 5745,
			2955, 5749, 5751, 5753, 5755, 5757, 5759, 5761, 2084, 2034, 2048,
			2036, 2217, 2213, 2205, 2209, 2054, 2040, 2080, 2277, 2225, 2255,
			2221, 2253, 2219, 2281, 2227, 2223, 2191, 2233, 2092, 2032, 2074,
			2030, 2281, 2235, 2064, 2028, 2187, 2185, 2229, 6883, 1971, 4608,
			1883, 1885, 15272, 2118, 2116};

	private long scriptStartTime = 0;
	private int runEnergy = random(40, 95);
	private boolean setAltitude = true;
	private final Antibans antibans = new Antibans();

	// Paint
	private int startXP = 0;
	private int startLvl = 0;
	private int laps = 0;
	private boolean lapStarted;
	private String status = "";
	private Image BKG;
	private BufferedImage normal = null;
	private BufferedImage clicked = null;

	// GUI
	private boolean waitGUI = true;
	ApeAtollGUI gui;
	private boolean safeLogOut;
	private boolean pickFruits;
	private boolean checkUpdates;
	private int MouseSpeed;

	private State getState() {
		if (inventory.contains(knife) && inventory.contains(pineapple) && inventory.getCount() <= 24) {
			return State.cutpineapple;
		}
		if (pickFruits && inventory.getCount() < 28 && inventory.contains(knife) && inventory.getCount(food) < 4
				&& (new RSArea(new RSTile(2764, 2737), new RSTile(2779, 2752)).contains(players.getMyPlayer().getLocation())
				|| new RSArea(new RSTile(2755, 2742), new RSTile(2768, 2756)).contains(players.getMyPlayer().getLocation()))
				&& game.getPlane() != 2) {
			return State.getpineapple;
		}
		if (safeLogOut && combat.getLifePoints() < 150 && !inventory.containsOneOf(food)) {
			log.info("Health is too low and out of food...");
			log.info("Health (" + combat.getLifePoints() + " hp) percentage remaining: " + combat.getHealth() + "%. Logged out to prevent dieing.");
			return State.error;
		}
		if (players.getMyPlayer().getLocation().equals(new RSTile(2755, 2742)) || players.getMyPlayer().getLocation().equals(new RSTile(2756, 2742)))
			return State.stone;
		if (players.getMyPlayer().getLocation().equals(new RSTile(2753, 2742)) && game.getPlane() != 2)
			return State.tree1;
		if (new RSArea(new RSTile(2752, 2741), new RSTile(2754, 2742)).contains(players.getMyPlayer().getLocation()))
			return State.bars;
		if (players.getMyPlayer().getLocation().equals(new RSTile(2747, 2741)))
			return State.rocks;
		if (players.getMyPlayer().getLocation().equals(new RSTile(2742, 2741)) || new RSArea(new RSTile(2747, 2729), new RSTile(2752, 2736)).contains(players.getMyPlayer().getLocation()))
			return State.rope;
		if (new RSArea(new RSTile(2756, 2730), new RSTile(2759, 2737)).contains(players.getMyPlayer().getLocation()))
			return State.tree2;
		if (calc.distanceTo(new RSTile(2770, 2747)) > 3)
			return State.tostart;
		return State.recover1;
	}


	//*******************************************************//
	// ON START
	//*******************************************************//
	public boolean onStart() {
		log("Starting up, this may take a minute...");


		// GUI settings:
		gui = new ApeAtollGUI();
		gui.setVisible(true);
		while (waitGUI) {
			sleep(100);
		}
		// END: GUI settigns


		// START: auto update
		/** MAJOR credits to OneThatWalks for this AMAZING auto updater.
		 *
		 */
		URLConnection url = null;
		BufferedReader in = null;
		BufferedWriter out = null;
		// Ask the user if they'd like to check for an update...

		if (checkUpdates) {
			try {
				// Open the version text file
				url = new URL(
						"http://www.voltrex.be/rsbot/VoltrexApeAtollVERSION.txt")
						.openConnection();
				// Create an input stream for it
				in = new BufferedReader(new InputStreamReader(url
						.getInputStream()));
				// Check if the current version is outdated
				if (Double.parseDouble(in.readLine()) > properties.version()) {
					// If it is, check if the user would like to update.
					if (JOptionPane.showConfirmDialog(null,
							"Update found. Do you want to update?") == 0) {
						// If so, allow the user to choose the file to be
						// updated.
						JOptionPane
								.showMessageDialog(null,
										"Please choose 'VoltrexApeAtoll.java' in your scripts/sources folder.");
						JFileChooser fc = new JFileChooser();
						// Make sure "Open" was clicked.
						if (fc.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
							// If so, set up the URL for the .java file and set
							// up the IO.
							url = new URL(
									"http://www.voltrex.be/rsbot/VoltrexApeAtoll.java")
									.openConnection();
							in = new BufferedReader(new InputStreamReader(url
									.getInputStream()));
							out = new BufferedWriter(new FileWriter(fc
									.getSelectedFile().getPath()));
							String inp;
							/*
															 * Until we reach the end of the file, write the
															 * next line in the file and add a new line. Then
															 * flush the buffer to ensure we lose no data in the
															 * process.
															 */
							while ((inp = in.readLine()) != null) {
								out.write(inp);
								out.newLine();
								out.flush();
							}
							// Notify the user that the script has been updated,
							// and a recompile and reload is needed.
							log("Script successfully downloaded. Please recompile.");
							return false;
						} else
							log("Update canceled");
					} else
						log("Update canceled");
				} else
					// JOptionPane.showMessageDialog(null, "You have the latest version.");
					log("You have the latest version.");
				// User has the
				// latest
				// version. Tell
				// them!
				if (in != null)
					in.close();
				if (out != null)
					out.close();
			} catch (IOException e) {
				log("Problem getting version. Please report this bug!");
			}

		} // end: check updates
		// END: auto update


		try {
			BKG = ImageIO.read(new URL("http://i54.tinypic.com/2egcfaw.jpg"));
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
		scriptStartTime = System.currentTimeMillis();
		mouse.setSpeed(MouseSpeed);
		camera.setPitch(true);

		log("You are using Voltrex Ape Atoll agility course.");

		return true;
	}

	//*******************************************************//
	// MAIN LOOP
	//*******************************************************//
	public int loop() {
		if (!game.isLoggedIn()) {
			sleep(1400);
			logIn();
			return random(100, 200);
		}

		if (startLvl == 0) {
			startXP = skills.getCurrentExp(Skills.AGILITY);
			startLvl = skills.getCurrentLevel(Skills.AGILITY);
			return 50;
		}

		if (setAltitude) {
			camera.setPitch(true);
			sleep(random(250, 500));
			setAltitude = false;
			return 50;
		}
		camera.setPitch(true);

		eat();

		startRunning(runEnergy);

		if (inventory.containsOneOf(dropItems)) {
			while (inventory.containsOneOf(dropItems)) {
				inventory.getItem(dropItems).doAction("Drop");
			}
		}

		if (players.getMyPlayer().getLocation().equals(new RSTile(2752, 2736))) {
			walkTile(new RSTile(2751, 2731));
		}

		switch (getState()) {
			case stone:
				doStone();
				return 50;
			case tree1:
				doTree1();
				return 50;
			case bars:
				doBars();
				return 50;
			case rocks:
				doRocks();
				return 50;
			case rope:
				doRope();
				return 50;
			case tree2:
				doTree2();
				return 50;
			case tostart:
				doTostart();
				return 50;
			case recover1:
				doRecover1();
				return 50;
			case getpineapple:
				doGetpineapple();
				return 50;
			case cutpineapple:
				doCutpineapple();
				return 50;
			case error:
				return -1;
		}

		return 50;
	}


	private void doStone() {
		status = "Jumping on stone...";

		lapStarted = true;

		final RSTile stone = new RSTile(2754, 2742);

		if (calc.tileOnScreen(stone)) {
			if (onTile(stone, "Stepping stone", "Jump-to", 0.5, 0.4, 0)) {
				antibans.perform(new int[]{Antibans.HOVER_PLAYER}, random(11, 18));
				sleep(random(500, 700));
				if (!players.getMyPlayer().getLocation().equals(new RSTile(2755, 2742))) {
					sleep(random(700, 1500));
				}
			}
		}

		while (players.getMyPlayer().getAnimation() == 3481 || players.getMyPlayer().isMoving())
			sleep(100);
	}

	private void doTree1() {
		status = "Climbing tree...";
		final RSTile tree = new RSTile(2752, 2741); // or 2743
		if (onTile(tree, "Tropical tree", "Climb", 0.5, 0.4, 0))
			sleep(random(100, 200));
		camera.setAngle(random(1, 360));
		sleep(random(100, 300));
		mouse.move(random(50, 700), random(50, 450), 2, 2);
		sleep(random(200, 700));
		mouse.move(random(50, 700), random(50, 450), 2, 2);
		sleep(random(1800, 2100));
		while (players.getMyPlayer().isMoving() || players.getMyPlayer().getAnimation() == 3487)
			sleep(100);
	}

	private void doBars() {
		status = "Crossing bars...";
		final RSObject mbars = objects.getNearest(12573);

		if (mbars != null) {
			mbars.doAction("Swing Across");
			sleep(random(1000, 1500));
		} else {
		}

		while (players.getMyPlayer().getAnimation() == 3484 || players.getMyPlayer().isMoving())
			sleep(100);
	}

	private void doRocks() {
		status = "Climbing rocks...";
		camera.setPitch(true);

		final RSObject rocks = objects.getNearest(12576);
		final RSTile startTile = new RSTile(2747, 2741);

		if (rocks != null) {
			// rocks.doAction("Climb-up");
			while (!rocks.doAction("Climb-up")) {
				rocks.doAction("Climb-up");
				sleep(random(200, 400));
			}
			sleep(random(500, 700));

			if (players.getMyPlayer().getLocation().equals(startTile)) {
				camera.setAngle(random(1, 360));
			} else {
				int rand = random(1, 2);
				if (rand == 1) {
					mouse.move(random(50, 700), random(50, 450), 2, 2);
				} else {
					camera.setAngle(random(1, 360));
				}
				sleep(random(1400, 1700));
			}

		}

		while (players.getMyPlayer().getAnimation() == 3484 || players.getMyPlayer().isMoving())
			sleep(100);
	}

	private void doRope() {
		final RSTile rope = new RSTile(2752, 2731);

		final RSTile walkHere = new RSTile(2751, 2731);

		if (players.getMyPlayer().getAnimation() != 3488) {
			if (!new RSArea(new RSTile(2749, 2730), new RSTile(2751, 2733)).contains(players.getMyPlayer().getLocation())) {
				status = "Walking to rope...";
				walkTile(walkHere);
				mouse.move(random(220, 340), random(130, 200), 2, 2);
				antibans.perform(new int[]{Antibans.CAMERA_MOVE_SLIGHTLY}, random(13, 20));
				sleep(random(200, 400));
			} else {
				if (calc.tileOnScreen(rope)) {
					if (onTile(rope, "Rope", "Swing", 0.5, 0.6, 0)) {
						status = "Swinging rope...";
						sleep(random(50, 200));
						camera.setAngle(random(1, 70));
						mouse.move(random(330, 422), random(95, 122), 2, 2);
						sleep(random(100, 400));
					} else {
						camera.setAngle(camera.getAngle() + random(-70, 70));
						return;
					}
				} else {
					camera.setAngle(camera.getAngle() + random(-50, 50));
				}
			}
		}

		while (players.getMyPlayer().isMoving() || players.getMyPlayer().getAnimation() == 3488)
			sleep(100);
	}

	private void doTree2() {
		status = "Climbing down tree...";

		// final RSObject tree = objects.getNearest(12618);
		final RSTile treeTile = new RSTile(2757, 2734);

		if (game.getPlane() != 1) {
			if (calc.tileOnScreen(treeTile)) {
				if (onTile(treeTile, "Tropical tree", "Climb-down", 0.5, 0.4, 0)) {
					if (lapStarted) {
						laps++;
						lapStarted = false;
					}
					antibans.perform(new int[]{Antibans.SKILLS_HOVER_AGILITY}, random(11, 17));
					sleep(random(300, 500));
					mouse.move(random(550, 670), random(20, 120), 2, 2);
					sleep(random(800, 1500));
				} else {
					camera.setAngle(random(1, 70));
				}
			} else {
				camera.setAngle(camera.getAngle() + random(-70, 70));
			}
		} else {
			antibans.perform(new int[]{Antibans.SKILLS_HOVER_AGILITY}, random(11, 17));
			sleep(random(300, 500));
			camera.setAngle(camera.getAngle() + random(-70, 70));
		}

		while (players.getMyPlayer().isMoving())
			sleep(100);
	}

	private void doRecover1() {
		status = "Walking to start...";

		final RSTile[] walkHere = new RSTile[]{new RSTile(2768, 2747), new RSTile(2755, 2742)};

		if (players.getMyPlayer().getLocation() != walkHere[1]) {
			walking.walkPathMM(walkHere, 1, 1);

			sleep(random(500, 750));
		}

		while (players.getMyPlayer().isMoving())
			sleep(100);
	}

	private void doTostart() {
		status = "Walking to start... (2)";

		final RSTile[] walkHere = new RSTile[]{new RSTile(2768, 2747), new RSTile(2755, 2742)};

		if (players.getMyPlayer().getLocation().equals(new RSTile(2756, 2743))) {
			walkTile(new RSTile(2755, 2742));
			sleep(random(500, 750));
		} else if (players.getMyPlayer().getLocation() != walkHere[1]) {
			walking.walkPathMM(walkHere, 1, 1);

			sleep(random(500, 750));
		}

		while (players.getMyPlayer().isMoving())
			sleep(100);
	}

	private void doCutpineapple() {
		status = "Cutting pineapples... (1)";

		status = "Cutting pineapples... (2)";
		org.rsbot.script.wrappers.RSItem item = inventory.getItem(knife);
		org.rsbot.script.wrappers.RSItem item2 = inventory.getItem(pineapple);
		inventory.useItem(item, item2);
		sleep(random(500, 600));

		if (interfaces.getComponent(905, 14).isValid()) {
			status = "Cutting pineapples... (3)";
			interfaces.getComponent(905, 14).doClick();
			sleep(random(800, 1000));
		}
		// eat:
		eat();
	}

	private void doGetpineapple() {
		final RSTile walkHere = new RSTile(2774, 2748);
		final RSTile walkHereB4 = new RSTile(2768, 2747);

		if (calc.distanceTo(walkHere) > 3 && calc.tileOnMap(walkHere)) {
			status = "Walking to plant...";
			walkTile(walkHere);
			sleep(random(500, 750));
		} else if (!calc.tileOnMap(walkHere)) {
			status = "Walking to pineapples...";
			walkTile(walkHereB4);
			sleep(random(500, 750));
		} else {
			status = "Collecting pineapple...";
			final RSObject plant = objects.getNearest(4827);

			if (plant != null) {
				plant.doAction("Pick");
				sleep(random(800, 1200));
			} else {
				status = "Waiting for pineapple...";
			}
		}

		while (players.getMyPlayer().isMoving())
			sleep(100);
	}

	private void startRunning(final int energy) {
		if (walking.getEnergy() >= energy && !walking.isRunEnabled()) {
			runEnergy = random(40, 95);
			walking.setRun(true);
			sleep(random(500, 750));
		}
	}

	public boolean eat() {
		int getHP = combat.getLifePoints();
		int RealHP = skills.getRealLevel(3) * 10;
		if (inventory.containsOneOf(food)) {
			if (getHP <= random(RealHP / 2.0, RealHP / 2.5)) {
				status = "Eating...";
				inventory.getItem(food).doAction("Eat");
				sleep(300, 500);
			}
		}
		return false;
	}

	public boolean onTile(RSTile tile, String search, String action, double dx, double dy, int height) {
		Point checkScreen;
		checkScreen = calc.tileToScreen(tile, dx, dy, height);
		if (!calc.pointOnScreen(checkScreen)) {
			walkTile(tile);
			sleep(random(340, 1310));
		}

		try {
			Point screenLoc;
			for (int i = 0; i < 30; i++) {
				screenLoc = calc.tileToScreen(tile, dx, dy, height);
				if (!calc.pointOnScreen(screenLoc)) {
					return false;
				}
				if (menu.getItems().toString().toLowerCase().contains(search.toLowerCase())) {
					break;
				}
				if (mouse.getLocation().equals(screenLoc)) {
					break;
				}
				mouse.move(screenLoc);
			}
			if (menu.getItems().length <= 1) {
				return false;
			}
			sleep(random(100, 200));
			if (menu.getItems().toString().toLowerCase().contains(action.toLowerCase())) {
				mouse.click(true);
				return true;
			} else {
				return menu.doAction(action);
			}
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	private void walkTile(final RSTile tile) {
		if (!(calc.distanceTo(walking.getDestination()) <= random(4, 7))) {
			if (players.getMyPlayer().isMoving())
				return;
		}
		Point screen = calc.tileToScreen(tile);
		if (calc.pointOnScreen(screen)) {
			if (players.getMyPlayer().isMoving())
				return;
			mouse.move(screen, random(-3, 4), random(-3, 4));
			walking.walkTileOnScreen(tile);
			sleep(random(500, 750));
		} else {
			walking.walkTileMM(tile);
			sleep(random(500, 750));
		}
	}

	public void onFinish() {
		log("Thanks for using Voltrex Ape Atoll!");
	}

	public boolean logIn() {
		if (game.isWelcomeScreen()) {
			log("On Welcome Screen, logging in...");
			sleep(random(2000, 3500));
			interfaces.getComponent(game.INTERFACE_WELCOME_SCREEN_PLAY).doClick();
			log("Clicked on login... Now waiting...");
			sleep(random(5000, 7000));
			return true;
		} else if (interfaces.getComponent(game.INTERFACE_WELCOME_SCREEN).isValid()) {
			log("On Welcome Screen, logging in...");
			sleep(random(2000, 3500));
			interfaces.getComponent(game.INTERFACE_WELCOME_SCREEN_PLAY).doClick();
			log("Clicked on login... Now waiting...");
			sleep(random(5000, 7000));
			return true;
		} else {
			log("Not on welcome screen... Can't login from here...");
		}
		return false;
	}

	/**
	 * Inspired on VoluntaryThieve
	 */
	private final class Antibans {

		private static final int MOUSE_MOVE_RANDOMLY = 0;
		private static final int CAMERA_MOVE_SLIGHTLY = 1;
		private static final int SKILLS_HOVER_AGILITY = 2;
		private static final int TABS_SELECT_RANDOM = 3;
		private static final int HOVER_PLAYER = 4;
		private static final int ALL_ANTIBANS = 5;

		private int currentAntiban;
		private final Timer timer = new Timer(0);
		private int counter;

		/**
		 * Generates a random number, and if it's the <tt>1/probability</tt>,
		 * it performs a randomly selected antiban from the <tt>selection</tt>.
		 *
		 * @param selection   The identifiers of the possible antibans to perform. To include
		 *                    all antibans, use only the value of <tt>ALL_ANTIBANS</tt>.
		 * @param probability The probability for an antiban to be performed, read as
		 *                    "1 in probability", where probability is the specified value.
		 *                    The minimum allowed probability is 1.
		 * @return <tt>true</tt> if an antiban was, and still is, being performed;
		 *         otherwise <tt>false</tt>.
		 * @throws IllegalArgumentException If the selection is null or if the probability is below one.
		 *                                  Also if an invalid antiban was found in the selection.
		 */
		private boolean perform(final int[] selection, final int probability) throws IllegalArgumentException {

			if (currentAntiban == 0) {
				if (selection == null) throw new IllegalArgumentException("The selection of antibans is null.");
				if (probability < 1) throw new IllegalArgumentException(
						"The probability is below one: " + probability);

				if (selection.length == 0 || random(0, probability) != 0)
					return false;

				currentAntiban = (selection.length == 1 && selection[0] == ALL_ANTIBANS) ?
						random(0, ALL_ANTIBANS) : selection[random(0, selection.length)];
				if (0 > currentAntiban || currentAntiban >= ALL_ANTIBANS)
					throw new IllegalArgumentException("Invalid antiban in selection: " + currentAntiban);
				timer.setEndIn(counter = 0);
			}

			final int mouseSpeed = mouse.getSpeed();
			mouse.setSpeed(random(5, 10));

			switch (currentAntiban) {
				case MOUSE_MOVE_RANDOMLY:
					if (timer.isRunning()) break;
					timer.setEndIn(random(755, 2345));

					if (++counter < random(2, 5))
						mouse.move(random(5, game.getWidth() - 253), random(5, game.getHeight() - 169));
					else currentAntiban = 0;
					break;
				case CAMERA_MOVE_SLIGHTLY:
					camera.setAngle(camera.getAngle() + random(-80, 80));
					currentAntiban = 0;
					break;
				case SKILLS_HOVER_AGILITY:
					if (timer.isRunning()) break;
					timer.setEndIn(random(755, 2345));
					if (counter == 0) {
						skills.doHover(Skills.INTERFACE_AGILITY);
						timer.setEndIn(random(1735, 2865));
						counter++;
					} else currentAntiban = 0;
					break;
				case TABS_SELECT_RANDOM:
					if (timer.isRunning()) break;
					timer.setEndIn(random(755, 2345));
					final int[] tabs = {game.TAB_ACHIEVEMENTS, game.TAB_ATTACK, game.TAB_CLAN, game.TAB_CONTROLS,
							game.TAB_EQUIPMENT, game.TAB_FRIENDS, game.TAB_IGNORE, game.TAB_INVENTORY, game.TAB_MAGIC,
							game.TAB_MUSIC, game.TAB_NOTES, game.TAB_OPTIONS, game.TAB_PRAYER, game.TAB_QUESTS,
							game.TAB_STATS, game.TAB_SUMMONING};

					game.openTab(tabs[random(0, tabs.length)]);
					currentAntiban = 0;
					break;
				case HOVER_PLAYER:
					if (timer.isRunning()) break;
					timer.setEndIn(random(755, 2345));
					RSPlayer player = null;
					RSPlayer[] validPlayers = players.getAll();

					player = validPlayers[random(0, validPlayers.length - 1)];
					if (player != null) {
						try {
							String playerName = player.getName();
							String myPlayerName = getMyPlayer().getName();
							if (playerName.equals(myPlayerName)) {
								break;
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
								break;
							}
						} catch (Exception ignored) {
						}
					}
					currentAntiban = 0;
					break;
				default:
					throw new AssertionError("Unsupported antiban in selection: " + currentAntiban);
			}

			mouse.setSpeed(mouseSpeed);
			return (currentAntiban != 0);
		}
	}


	//*******************************************************//
	// PAINT SCREEN
	//*******************************************************//
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
		long runTime;
		long seconds;
		long minutes = 0;
		long hours = 0;
		int percent;
		int currentXP;
		int currentLVL;
		int gainedXP;
		int gainedLVL;
		int lapsPerHour;
		int expPerHour;

		runTime = System.currentTimeMillis() - scriptStartTime;
		seconds = runTime / 1000;
		if (seconds >= 60) {
			minutes = seconds / 60;
			seconds -= (minutes * 60);
		}
		if (minutes >= 60) {
			hours = minutes / 60;
			minutes -= (hours * 60);
		}


		/* ************************ NEW PAINT ******************* */
		percent = skills.getPercentToNextLevel(Skills.AGILITY);
		currentXP = skills.getCurrentExp(Skills.AGILITY);
		currentLVL = skills.getCurrentLevel(Skills.AGILITY);
		gainedXP = currentXP - startXP;
		gainedLVL = currentLVL - startLvl;
		lapsPerHour = (int) ((3600000.0 / (double) runTime) * laps);
		expPerHour = (int) (3600000.0 / (double) runTime * gainedXP);
		final int fillBar = (int) (4.7 * (double) percent);
		drawMouse(g);


		g.setColor(new Color(32, 0, 0, 175)); // border-top-right
		g.fillRect(23, 387, 470, 26); // border-top-right
		g.setColor(new Color(108, 108, 108, 200)); // border-bottom-left
		g.fillRect(25, 389, 470, 26); // border-bottom-left


		g.setColor(new Color(0, 0, 0, 175));
		g.fillRoundRect(7, 345, 506, 129, 7, 7);

		g.drawImage(BKG, 90, 10, null);
		g.setColor(new Color(70, 50, 10, 255));
		g.drawString("v" + properties.version(), 321, 40);


		g.setColor(new Color(139, 0, 0, 175)); // red1
		g.fillRect(24, 388, 470, 26); // red1
		g.setColor(new Color(0, 166, 0, 200)); // green1
		g.fillRect(24, 388, fillBar, 26); // green1

		g.setColor(new Color(207, 58, 58, 175)); // red2
		g.fillRect(24, 401, 470, 13); // red2
		g.setColor(new Color(128, 219, 128, 200)); // green2
		g.fillRect(24, 401, fillBar, 13); // green2

		g.setFont(new Font("Verdana", 0, 14));
		g.setColor(new Color(0, 0, 0, 255));
		g.drawString(percent + "% - " + skills.getExpToNextLevel(Skills.AGILITY) + "xp remaining", 43, 406);

		g.setFont(new Font("Verdana", 0, 12));
		g.setColor(new Color(225, 225, 225, 175));

		// left side
		g.drawString("Total Laps: " + laps + " (" + lapsPerHour + "/hr)", 24, 430);
		g.drawString("Current Lvl: " + currentLVL + " (+ " + gainedLVL + ")", 24, 445);
		g.drawString("Exp Gained: " + gainedXP + " (" + expPerHour + "/hr)", 24, 460);

		// right side
		g.drawString("Status: " + status, 250, 430);
		g.drawString("Run Time: " + hours + ":" + minutes + ":" + seconds, 250, 445);
		try {
			if (expPerHour > 0) {
				long sTNL = (skills.getExpToNextLevel(Skills.AGILITY)) / (expPerHour / 3600);
				long hTNL = sTNL / (60 * 60);
				sTNL -= hTNL * (60 * 60);
				long mTNL = sTNL / 60;
				sTNL -= mTNL * 60;
				g.drawString("Level in: " + hTNL + ":" + mTNL + ":" + sTNL + " (" + percent + "%)", 250, 460);
			} else {
				g.drawString("Level in: 0:0:0 (" + percent + "%)", 250, 460);
			}
		} catch (Exception e) {
			g.drawString("Level in: -1:-1:-1 (" + percent + "%)", 250, 460);
		}

	}


	//*******************************************************//
	// GUI
	//*******************************************************//
	public class ApeAtollGUI extends JFrame {
		public ApeAtollGUI() {
			initComponents();
		}

		private void button1ActionPerformed(ActionEvent e) {
			safeLogOut = checkBox1.isSelected();
			pickFruits = checkBox2.isSelected();
			checkUpdates = checkBox3.isSelected();
			int mouseSpeedTemp = (int) slider1.getValue();

			if (mouseSpeedTemp == 100) {
				MouseSpeed = 2;
			} else if (mouseSpeedTemp == 90) {
				MouseSpeed = 3;
			} else if (mouseSpeedTemp == 80) {
				MouseSpeed = 4;
			} else if (mouseSpeedTemp == 70) {
				MouseSpeed = 5;
			} else if (mouseSpeedTemp == 60) {
				MouseSpeed = 6;
			} else if (mouseSpeedTemp == 50) {
				MouseSpeed = 7;
			} else if (mouseSpeedTemp == 40) {
				MouseSpeed = 8;
			} else if (mouseSpeedTemp == 30) {
				MouseSpeed = 9;
			} else if (mouseSpeedTemp == 20) {
				MouseSpeed = 10;
			} else if (mouseSpeedTemp == 10) {
				MouseSpeed = 12;
			}

			waitGUI = false;
			dispose();
		}

		private void initComponents() {
			// JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
			// Generated using JFormDesigner Evaluation license - Jhon Nyboy
			tabbedPane1 = new JTabbedPane();
			panel1 = new JPanel();
			checkBox1 = new JCheckBox();
			checkBox2 = new JCheckBox();
			separator1 = new JSeparator();
			checkBox3 = new JCheckBox();
			slider1 = new JSlider();
			label4 = new JLabel();
			label5 = new JLabel();
			label6 = new JLabel();
			scrollPane1 = new JScrollPane();
			editorPane1 = new JEditorPane();
			label1 = new JLabel();
			label2 = new JLabel();
			label3 = new JLabel();
			button1 = new JButton();
			scrollPane2 = new JScrollPane();
			editorPane2 = new JEditorPane();

			//======== this ========
			Container contentPane = getContentPane();
			contentPane.setLayout(null);

			//======== tabbedPane1 ========
			{

				//======== panel1 ========
				{


					panel1.setLayout(null);

					//---- checkBox1 ----
					checkBox1.setText("Log out when low HP and out of food");
					checkBox1.setSelected(true);
					panel1.add(checkBox1);
					checkBox1.setBounds(new Rectangle(new Point(5, 5), checkBox1.getPreferredSize()));

					//---- checkBox2 ----
					checkBox2.setText("Pick pineapples (have a knife in inventory)");
					checkBox2.setSelected(true);
					panel1.add(checkBox2);
					checkBox2.setBounds(new Rectangle(new Point(5, 30), checkBox2.getPreferredSize()));
					panel1.add(separator1);
					separator1.setBounds(20, 60, 225, 2);

					//---- checkBox3 ----
					checkBox3.setText("Check for updates on startup");
					checkBox3.setSelected(true);
					panel1.add(checkBox3);
					checkBox3.setBounds(new Rectangle(new Point(5, 70), checkBox3.getPreferredSize()));

					//---- slider1 ----
					slider1.setSnapToTicks(true);
					slider1.setPaintTicks(true);
					slider1.setPaintLabels(true);
					slider1.setMinimum(10);
					slider1.setMajorTickSpacing(10);
					slider1.setValue(60);
					panel1.add(slider1);
					slider1.setBounds(10, 170, 240, slider1.getPreferredSize().height);

					//---- label4 ----
					label4.setText("Mouse speed:");
					label4.setFont(label4.getFont().deriveFont(label4.getFont().getStyle() | Font.BOLD));
					panel1.add(label4);
					label4.setBounds(new Rectangle(new Point(5, 135), label4.getPreferredSize()));

					//---- label5 ----
					label5.setText("Slow");
					panel1.add(label5);
					label5.setBounds(new Rectangle(new Point(5, 155), label5.getPreferredSize()));

					//---- label6 ----
					label6.setText("Fast");
					panel1.add(label6);
					label6.setBounds(new Rectangle(new Point(230, 155), label6.getPreferredSize()));

					{ // compute preferred size
						Dimension preferredSize = new Dimension();
						for (int i = 0; i < panel1.getComponentCount(); i++) {
							Rectangle bounds = panel1.getComponent(i).getBounds();
							preferredSize.width = Math.max(bounds.x + bounds.width, preferredSize.width);
							preferredSize.height = Math.max(bounds.y + bounds.height, preferredSize.height);
						}
						Insets insets = panel1.getInsets();
						preferredSize.width += insets.right;
						preferredSize.height += insets.bottom;
						panel1.setMinimumSize(preferredSize);
						panel1.setPreferredSize(preferredSize);
					}
				}
				tabbedPane1.addTab("Options", panel1);


				//======== scrollPane1 ========
				{

					//---- editorPane1 ----
					editorPane1.setEditable(false);
					editorPane1.setText("Start AT the course with a Monkey Greegree\nequipted(must be a ninja greegree). If you enabled the option 'Pick pineapples', then you must have a\nknife in your inventory.");
					scrollPane1.setViewportView(editorPane1);
				}
				tabbedPane1.addTab("Instructions", scrollPane1);

			}
			contentPane.add(tabbedPane1);
			tabbedPane1.setBounds(0, 0, 270, 265);

			//---- label1 ----
			label1.setText("Ape Atoll course");
			label1.setFont(label1.getFont().deriveFont(label1.getFont().getStyle() | Font.BOLD, label1.getFont().getSize() + 2f));
			contentPane.add(label1);
			label1.setBounds(new Rectangle(new Point(275, 20), label1.getPreferredSize()));

			//---- label2 ----
			label2.setText("by Voltrex");
			contentPane.add(label2);
			label2.setBounds(new Rectangle(new Point(275, 35), label2.getPreferredSize()));

			//---- label3 ----
			label3.setText("Version " + properties.version());
			contentPane.add(label3);
			label3.setBounds(new Rectangle(new Point(275, 60), label3.getPreferredSize()));

			//---- button1 ----
			button1.setText("Start Script");
			button1.setFont(button1.getFont().deriveFont(button1.getFont().getStyle() | Font.BOLD));
			button1.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					button1ActionPerformed(e);
				}
			});
			contentPane.add(button1);
			button1.setBounds(new Rectangle(new Point(280, 235), button1.getPreferredSize()));

			//======== scrollPane2 ========
			{

				//---- editorPane2 ----
				editorPane2.setEditable(false);
				editorPane2.setText("1.30: New GUI");
				scrollPane2.setViewportView(editorPane2);
			}
			contentPane.add(scrollPane2);
			scrollPane2.setBounds(275, 75, 110, 155);

			{ // compute preferred size
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
			// JFormDesigner - End of component initialization  //GEN-END:initComponents
		}

		// JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables
		// Generated using JFormDesigner Evaluation license - Jhon Nyboy
		private JTabbedPane tabbedPane1;
		private JPanel panel1;
		private JCheckBox checkBox1;
		private JCheckBox checkBox2;
		private JSeparator separator1;
		private JCheckBox checkBox3;
		private JSlider slider1;
		private JLabel label4;
		private JLabel label5;
		private JLabel label6;
		private JScrollPane scrollPane1;
		private JEditorPane editorPane1;
		private JLabel label1;
		private JLabel label2;
		private JLabel label3;
		private JButton button1;
		private JScrollPane scrollPane2;
		private JEditorPane editorPane2;
		// JFormDesigner - End of variables declaration  //GEN-END:variables
	}

}
