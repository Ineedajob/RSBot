import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Point;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.rsbot.event.events.ServerMessageEvent;

import org.rsbot.event.listeners.PaintListener;
import org.rsbot.event.listeners.ServerMessageListener;

import org.rsbot.script.Script;
import org.rsbot.script.ScriptManifest;

import org.rsbot.script.methods.Game;
import org.rsbot.script.methods.Skills;

import org.rsbot.script.util.Filter;
import org.rsbot.script.util.Timer;

import org.rsbot.script.wrappers.RSArea;
import org.rsbot.script.wrappers.RSItem;
import org.rsbot.script.wrappers.RSNPC;
import org.rsbot.script.wrappers.RSPlayer;
import org.rsbot.script.wrappers.RSTile;

@ScriptManifest(authors = {"Aion"},
		name = "Aion's Karamja Fisher",
		version = 5.1,
		keywords = {"Fishing", "Karamja", "Stiles"},
		website = "http://www.powerbot.org/vb/showthread.php?t=371489",
		description = "The only Karamja fishing script you need!")
public class AionKaramjaFisher extends Script implements ActionListener,
		PaintListener, ServerMessageListener {

	private int lobs;
	private int tunas;
	private int swords;
	private int[] prices = {-1, -1, -1};

	private int START_EXP = -1;
	private int START_LVL = -1;

	private final int NPC_STILES = 11267;
	private final int NPC_FISHING_SPOT = 324;

	private final Filter<RSNPC> NPC_FILTER = new Filter<RSNPC>() {
		public boolean accept(RSNPC npc) {
			final int NPC_ID = npc.getID();
			return NPC_ID == NPC_STILES || NPC_ID == NPC_FISHING_SPOT;
		}
	};

	private final double VERSION = getClass().getAnnotation(
			ScriptManifest.class).version();

	private boolean fishLobs = true;
	private boolean keepTunas = true;
	private boolean showProfit = false;
	private boolean safeRoute = false;
	private boolean[] takeScreenshot = {true, false};

	private boolean pressedStart = false;
	private boolean pressedCancel = false;
	private static boolean windowClosed = false;

	private String status;

	private Angle angle;
	private Pitch pitch;

	private Timer elapsedTime;

	private final RSArea[] AREAS = {
			new RSArea(new RSTile(2918, 3173), new RSTile(2930, 3185)),
			new RSArea(new RSTile(2923, 3176), new RSTile(23926, 3180)),
			new RSArea(new RSTile(2840, 3135), new RSTile(2854, 3147))
	};

	private RSTile[] path;

	private final RSTile[][] PATHS = {
			{ // Normal path
					new RSTile(2919, 3176), new RSTile(2911, 3172),
					new RSTile(2898, 3170), new RSTile(2888, 3163),
					new RSTile(2880, 3156), new RSTile(2872, 3149),
					new RSTile(2862, 3147), new RSTile(2852, 3143)
			}, { // Safe path
					new RSTile(2919, 3176), new RSTile(2911, 3172),
					new RSTile(2899, 3165), new RSTile(2894, 3154),
					new RSTile(2885, 3146), new RSTile(2873, 3147),
					new RSTile(2861, 3146), new RSTile(2851, 3142)
			}
	};

	enum State {
		INIT("Initializing.."),
		WALK_TO_FISHING_SPOT("Heading to fishing spot"),
		ATTEMPT_TO_FISH("Attempting to fish"),
		FISHING("Fishing"),
		DROP("Dropping tunas"),
		WALK_TO_STILES("Heading to Stiles"),
		EXCHANGE("Exchanging fishes"),
		MOVE_MOUSE("(Antiban): Moving mouse"),
		LOOK_FISHING_SKILL("(Antiban): Looking at fishing skill"),
		AFK("(Antiban): Being 'AFK'");

		final private String DESC;

		State(final String desc) {
			this.DESC = desc;
		}

		public String getDescription() {
			return DESC;
		}
	}

	public void actionPerformed(ActionEvent e) {
		Object s = e.getSource();

		if (s instanceof JButton) {
			if (e.getActionCommand().equals("Start")) {
				pressedStart = true;
			} else {
				pressedCancel = true;
			}
		} else if (s instanceof JComboBox) {
			JPanel pane = (JPanel) ((Component) s).getParent();

			Object selItem = ((JComboBox) s).getSelectedItem();
			boolean enable = selItem.toString().contains("/ Sword");

			fishLobs = !enable;

			for (Component c : pane.getComponents()) {
				if (c instanceof JCheckBox) {
					JCheckBox jcb = (JCheckBox) c;
					if (jcb.getText().contains("rop tun")) {
						jcb.setEnabled(enable);
						if (!enable) {
							jcb.setSelected(false);
						}
					}
				}
			}
		} else if (s instanceof JCheckBox) {
			JCheckBox jcb = (JCheckBox) s;
			String text = jcb.getText();

			switch (text.charAt(0)) {
				case 'S':
					showProfit = jcb.isSelected();
					break;
				case 'U':
					safeRoute = jcb.isSelected();
					break;
				case 'D':
					keepTunas = !jcb.isSelected();
					break;
				case 'T':
					takeScreenshot[text.contains("upon") ? 0 : 1] = jcb.isSelected();
					break;
			}
		}
	}

	@Override
	public boolean onStart() {
		setStatus(State.INIT);

		GUISettings gui = new GUISettings();
		gui.setVisible(true);

		long time = System.currentTimeMillis();
		while (true) {
			if (AionKaramjaFisher.windowClosed || pressedCancel) {
				gui.dispose();
				return false;
			} else if ((System.currentTimeMillis() - time) >= 60000) {
				log("Initialization took too long");
				gui.dispose();
				return false;
			} else if (pressedStart) {
				gui.dispose();
				if (showProfit) {
					if (fishLobs) {
						prices[0] = grandExchange.getMarketPrice(377);
					} else {
						prices[2] = grandExchange.getMarketPrice(371);
						if (keepTunas)
							prices[1] = grandExchange.getMarketPrice(359);
					}
				}
				path = safeRoute ? PATHS[1] : PATHS[0];
				elapsedTime = new Timer(0);
				angle = new Angle();
				pitch = new Pitch();
				angle.start();
				pitch.start();
				return true;
			}
			sleep(5, 15);
		}
	}

	@Override
	public int loop() {
		if (game.getClientState() != 10) {
			if (!angle.pause) angle.pause = true;
			if (!pitch.pause) pitch.pause = true;
		}

		if (interfaces.get(741).isValid()) {
			interfaces.getComponent(741, 9).doClick();
			return random(300, 500);
		}

		if (interfaces.get(740).isValid()) {
			if (takeScreenshot[0]) {
				env.saveScreenshot(false);
			}
			interfaces.getComponent(740, 3).doClick();
			sleep(600, 700);
			if (game.getCurrentTab() != Game.TAB_STATS) {
				game.openTab(Game.TAB_STATS);
				sleep(800, 1200);
			}
			skills.doHover(Skills.INTERFACE_FISHING);
			sleep(150, 300);
			mouse.click(true);
			return random(1000, 2000);
		}

		State state = getState();
		switch (state) {
			case WALK_TO_FISHING_SPOT:
				setStatus(state);
				if (getMyPlayer().isMoving()) {
					if (calc.distanceTo(path[path.length - 1]) <= 20) {
						if (random(1, 7) == 6) {
							angle.pause = true;
							pitch.pause = true;
							RSNPC npc = getFishingSpot(true);
							if (npc != null) {
								camera.setAngle(camera.getTileAngle(npc.getLocation()));
								if (npc.isOnScreen()) {
									break;
								}
							}
							angle.pause = false;
							pitch.pause = false;
						}
					}
					if (calc.distanceTo(walking.getDestination()) > 7) break;
				}
				walkPathMM(walking.reversePath(path));
				break;
			case ATTEMPT_TO_FISH:
				setStatus(state);
				RSNPC fishingSpot = getFishingSpot(true);
				if (fishingSpot != null) {
					if (!fishingSpot.isOnScreen() && calc.distanceTo(fishingSpot) > 1) {
						walkToNPC(fishingSpot);
					}
					tiles.doAction(fishingSpot.getLocation(), getFishingAction());
					if (calc.distanceTo(fishingSpot) >= 2) {
						if (waitToMove(random(650, 900))) {
							waitToStop();
						}
					}
					waitForAnim(random(1500, 2000));
				}
				break;
			case FISHING:
				setStatus(state);
				antiban();
				break;
			case DROP:
				setStatus(state);
				for (RSItem item : inventory.getItems()) {
					if (item == null || item.getID() != 359) {
						continue;
					}
					Point iPoint = item.getComponent().getCenter();
					mouse.click(iPoint.x + random(-10, 10),
							iPoint.y + random(-10, 10), false);
					sleep(75, 125);
					menu.doAction("Drop");
					sleep(75, 125);
				}
				break;
			case WALK_TO_STILES:
				setStatus(state);
				if (getMyPlayer().isMoving()) {
					if (calc.distanceTo(walking.getDestination()) > 7) break;
				}
				walkPathMM(path);
				break;
			case EXCHANGE:
				setStatus(state);
				RSNPC stiles = npcs.getNearest(NPC_FILTER);
				if (stiles != null && stiles.getID() == NPC_STILES) {
					if (!stiles.isOnScreen() && calc.distanceTo(stiles) > 1) {
						walkToNPC(stiles);
					}
					stiles.doAction("Exchange");
					if (calc.distanceTo(stiles) >= 2) {
						if (waitToMove(random(650, 900))) {
							waitToStop();
						}
					}
					waitForIface(241, random(600, 800));
				}
				break;
		}
		return random(100, 300);
	}

	@Override
	public void onFinish() {
		angle.stop = true;
		pitch.stop = true;
		log("Thanks for using Aion's Karamja Fisher v" + getVersion());
	}

	public void onRepaint(Graphics g) {
		if (game.isLoggedIn() && !game.isLoginScreen()) {
			g.setColor(new Color(16, 16, 16, 123));
			g.fillRoundRect(8, 179, 243, 153, 15, 15);

			g.setColor(Color.RED);
			g.draw3DRect(13, 291, 231, 15, true);

			g.setColor(new Color(48, 225, 48, 170));
			g.fill3DRect(14, 292, (getPercentToLvl() * 229 / 100), 14, true);

			g.setColor(Color.WHITE);
			g.drawString("Aion's Karamja Fisher v" + getVersion(), 58, 192);
			g.drawString("Runtime: " + getRuntime(), 13, 206);

			if (showProfit) {
				g.drawString("Profit/hour: " + format(getProfitHour()), 140, 206);
			}

			g.drawString("Caught " + format(getFishCaught()) + " fish", 13, 225);
			g.drawString("Gained " + format(getExpGained()) + " exp", 13, 239);
			g.drawString("Catch/Hour: " + format(getFishHour()), 140, 225);
			g.drawString("Exp/Hour: " + format((int) getExpHour()), 140, 239);

			String t = (getLvlGained() == 0) ? "fishing" : "";
			g.drawString("Current " + t + " level: " + getFishingLvl(), 13, 258);

			if (getLvlGained() != 0) {
				String s = (getLvlGained() == 1) ? "" : "s";
				g.drawString("Gained " + getLvlGained() + " level" + s, 140, 258);
			}

			g.drawString("Exp left: " + format(getExpToLvl()), 13, 272);
			g.drawString("Catches left: " + format(getCatchesLeft()), 140, 272);
			g.drawString("Estimated time to level: " + getTimeToLvl(), 13, 286);
			g.drawString(getPercentToLvl() + "%", 113, 303);
			g.drawString("Status: " + status, 13, 325);
		}

		Point mPoint = mouse.getLocation();
		Point pPoint = mouse.getPressLocation();
		long mpt = System.currentTimeMillis() - mouse.getPressTime();

		if (mpt < 1000) {
			g.setColor(Color.red);
			g.drawOval(pPoint.x - 2, pPoint.y - 2, 4, 4);
			g.drawOval(pPoint.x - 9, pPoint.y - 9, 18, 18);
		}

		g.setColor(Color.YELLOW);
		g.drawOval(mPoint.x - 2, mPoint.y - 2, 4, 4);
		g.drawOval(mPoint.x - 9, mPoint.y - 9, 18, 18);
	}

	public void serverMessageRecieved(ServerMessageEvent e) {
		String msg = e.getMessage().toLowerCase();
		if (msg.contains("you catch")) {
			if (msg.contains("tuna")) {
				if (keepTunas) tunas++;
			} else if (msg.contains("lobster")) {
				lobs++;
			} else if (msg.contains("swordfish")) {
				swords++;
			}
		} else if (msg.contains("you've just")) {
			log(msg);
		}
	}

	private void antiban() {
		switch (random(1, 20)) {
			case 2:
				if (random(1, 5) != 1) break;
				setStatus(State.MOVE_MOUSE);
				mouse.moveSlightly();
				break;
			case 6:
				if (random(1, 18) != 7) break;
				setStatus(State.LOOK_FISHING_SKILL);
				if (game.getCurrentTab() != Game.TAB_STATS) {
					game.openTab(Game.TAB_STATS);
					sleep(500, 900);
				}
				skills.doHover(Skills.INTERFACE_FISHING);
				sleep(random(1400, 2000), 3000);
				if (random(0, 5) != 3) break;
				setStatus(State.MOVE_MOUSE);
				mouse.moveSlightly();
				break;
			case 16:
				if (random(1, 150) != random(15, 20)) break;
				angle.pause = true;
				pitch.pause = true;
				setStatus(State.AFK);
				sleep(random(10000, 180001));
				pitch.pause = false;
				angle.pause = false;
		}
	}

	private String format(int number) {
		return format(String.valueOf(number));
	}

	private String format(double number) {
		return format(String.valueOf((int) number));
	}

	private String format(String number) {
		if (number.length() < 4) {
			return number;
		}
		return format(number.substring(0, number.length() - 3)) + "," +
				number.substring(number.length() - 3, number.length());
	}

	private int getCatchesLeft() {
		return getExpToLvl() / 90;
	}

	private int getExpGained() {
		if (START_EXP == -1) {
			if (game.getClientState() == 10) {
				START_EXP = skills.getCurrentExp(Skills.FISHING);
			}
		}
		return skills.getCurrentExp(Skills.FISHING) - START_EXP;
	}

	private double getExpHour() {
		int xpGained = getExpGained();
		long start = System.currentTimeMillis() - elapsedTime.getElapsed();
		return xpGained * 3600000D / (System.currentTimeMillis() - start);
	}

	private int getExpToLvl() {
		return skills.getExpToNextLevel(Skills.FISHING);
	}

	private int getFishCaught() {
		return lobs + tunas + swords;
	}

	private double getFishHour() {
		int caught = getFishCaught();
		long start = System.currentTimeMillis() - elapsedTime.getElapsed();
		return caught * 3600000D / (System.currentTimeMillis() - start);
	}

	private int getFishingLvl() {
		return skills.getCurrentLevel(Skills.FISHING);
	}

	private String getFishingAction() {
		if (fishLobs) {
			return "Cage";
		}
		return "Harpoon";
	}

	private RSNPC getFishingSpot(boolean exclude) {
		for (RSNPC npc : npcs.getAll(NPC_FILTER)) {
			if (npc == null) continue;
			else if (npc.getID() != NPC_FISHING_SPOT) continue;
			else if (exclude && !isAtFishingQuay(npc)) continue;
			return npc;
		}
		return null;
	}

	private int getLvlGained() {
		if (START_LVL == -1) {
			if (game.getClientState() == 10) {
				START_LVL = skills.getCurrentLevel(Skills.FISHING);
				if (START_LVL < 40) {
					log("You need a fishing level of at least 40");
					stopScript(true);
				}
			}
		}
		return skills.getCurrentLevel(Skills.FISHING) - START_LVL;
	}

	private int getPercentToLvl() {
		return skills.getPercentToNextLevel(Skills.FISHING);
	}

	private int getProfitHour() {
		int profit = (fishLobs) ? prices[0] * lobs
				: prices[2] * swords + (keepTunas ? prices[1] * tunas : 0);
		long start = System.currentTimeMillis() - elapsedTime.getElapsed();
		return (int) (profit * 3600000D / (System.currentTimeMillis() - start));
	}

	private String getRuntime() {
		return elapsedTime.toElapsedString();
	}

	private State getState() {
		if (getMyPlayer().getAnimation() == -1) {
			if (interfaces.get(210).getComponent(1).containsText("carry any")) {
				if (!fishLobs && inventory.contains(359) && !keepTunas) {
					return State.DROP;
				}
				return State.WALK_TO_STILES;
			}
			if (inventory.isFull()) {
				if (!isAtStiles()) {
					if (!fishLobs && inventory.contains(359) && !keepTunas) {
						return State.DROP;
					}
					return State.WALK_TO_STILES;
				}
				return State.EXCHANGE;
			} else if (!isAtFishingSpot()) {
				if (isAtStiles()) {
					if (inventory.containsOneOf(359, 371, 377)) {
						return State.EXCHANGE;
					}
				}
				return State.WALK_TO_FISHING_SPOT;
			}
			return State.ATTEMPT_TO_FISH;
		}
		return State.FISHING;
	}

	private String getTimeFormat(long time) {
		return Timer.format(time);
	}

	private String getTimeToLvl() {
		return getTimeFormat((long) (getExpToLvl() / getExpHour() * 3600000D));
	}

	private double getVersion() {
		return VERSION;
	}

	private boolean isAtFishingQuay(final RSNPC npc) {
		return npc != null && AREAS[1].contains(npc.getLocation());
	}

	private boolean isAtFishingSpot() {
		RSPlayer me = getMyPlayer();
		return AREAS != null && me != null && AREAS[0].contains(me.getLocation());
	}

	private boolean isAtStiles() {
		RSPlayer me = getMyPlayer();
		return AREAS != null && me != null && AREAS[2].contains(me.getLocation());
	}

	private void setStatus(State state) {
		status = state.getDescription();
	}

	private int waitForAnim(int timeout) {
		long time = System.currentTimeMillis();
		int animation;
		while ((System.currentTimeMillis() - time) < timeout) {
			animation = getMyPlayer().getAnimation();
			if (animation != -1) {
				return animation;
			}
			sleep(5, 15);
		}
		return getMyPlayer().getAnimation();
	}

	private boolean waitForIface(int parent, int timeout) {
		long time = System.currentTimeMillis();
		while ((System.currentTimeMillis() - time) < timeout) {
			if (interfaces.get(parent).isValid()) {
				return true;
			}
			sleep(5, 15);
		}
		return interfaces.get(parent).isValid();
	}

	private boolean waitToMove(int timeout) {
		long time = System.currentTimeMillis();
		while ((System.currentTimeMillis() - time) < timeout) {
			if (getMyPlayer().isMoving()) {
				return true;
			}
			sleep(5, 15);
		}
		return getMyPlayer().isMoving();
	}

	private void walkPathMM(RSTile[] path) {
		if (path != null) {
			if (!walking.isRunEnabled()) {
				if (walking.getEnergy() >= random(25, 100))
					walking.setRun(true);
			}
			angle.pause = true;
			int mouse_speed = mouse.getSpeed();
			mouse.setSpeed(random(4, 7));
			if (!walking.walkPathMM(path, random(0, 4), random(0, 4))) {
				walking.walkPathMM(walking.findPath(path[path.length - 1]));
			}
			mouse.setSpeed(mouse_speed);
			angle.pause = false;
			waitToMove(random(600, 900));
		}
	}

	private void walkToNPC(RSNPC npc) {
		if (npc == null) return;
		walking.walkTileMM(npc.getLocation(), 1, 1);
		waitToMove(random(650, 900));
		while (getMyPlayer().isMoving()) {
			if (random(0, 20) == random(5, 8))
				camera.setAngle(camera.getTileAngle(npc.getLocation()));
			if (npc.isOnScreen()) break;
			sleep(5, 15);
		}
	}

	private void waitToStop() {
		do {
			sleep(5, 15);
		} while (getMyPlayer().isMoving());
	}

	class Angle extends Thread {

		private volatile boolean stop = false;
		private volatile boolean pause = false;

		@Override
		public void run() {
			try {
				while (!stop) {
					if (pause) {
						while (pause) {
							Thread.sleep(random(25, 150));
						}
					}
					if (random(1, 15000) == 342) {
						char key = KeyEvent.VK_RIGHT;
						if (random(1, 5) == 2) {
							key = KeyEvent.VK_LEFT;
						}
						long endTime = System.currentTimeMillis()
								+ random(250, 1200);
						keyboard.pressKey(key);
						while (System.currentTimeMillis() < endTime) {
							if (pause) break;
							Thread.sleep(random(5, 15));
						}
						keyboard.releaseKey(key);
						Thread.sleep(random(random(1000, 5000),
								random(11000, 17000)));
					}
				}
			} catch (Exception ignored) {
			}
		}
	}

	class Pitch extends Thread {

		private volatile boolean stop = false;
		private volatile boolean pause = false;

		@Override
		public void run() {
			try {
				while (!stop) {
					if (pause) {
						while (pause)
							Thread.sleep(random(25, 150));
					}
					if (random(1, 15000) == 342) {
						char key = KeyEvent.VK_UP;
						if (camera.getPitch() >= random(random(50, 86), 100))
							key = KeyEvent.VK_DOWN;
						keyboard.pressKey(key);
						long endTime = System.currentTimeMillis()
								+ random(random(50, 150), 500);
						while (System.currentTimeMillis() < endTime) {
							if (pause) break;
							else if (camera.getPitch() == 0) {
								if (key == KeyEvent.VK_DOWN) break;
							} else if (camera.getPitch() == 100) {
								if (key == KeyEvent.VK_UP) break;
							}
							Thread.sleep(random(5, 15));
						}
						keyboard.releaseKey(key);
						Thread.sleep(random(random(1000, 5000), 10001));
					}
				}
			} catch (Exception ignored) {
			}
		}
	}

	class GUISettings extends JFrame {

		private JLabel label1, label2;
		private JButton button1, button2;
		private JCheckBox checkBox1, checkBox2, checkBox3, checkBox4, checkBox5;
		private JComboBox comboBox1;

		public GUISettings() {
			initComponents();
		}

		private void initComponents() {
			label1 = new JLabel();
			label2 = new JLabel();
			button1 = new JButton();
			button2 = new JButton();
			checkBox1 = new JCheckBox();
			checkBox2 = new JCheckBox();
			checkBox3 = new JCheckBox();
			checkBox4 = new JCheckBox();
			checkBox5 = new JCheckBox();
			comboBox1 = new JComboBox();

			Container contentPane = getContentPane();
			contentPane.setLayout(new GridBagLayout());
			((GridBagLayout) contentPane.getLayout()).columnWidths = new int[]{0, 86, 0, 0, 0, 0};
			((GridBagLayout) contentPane.getLayout()).rowHeights = new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
			((GridBagLayout) contentPane.getLayout()).columnWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, 1.0E-4};
			((GridBagLayout) contentPane.getLayout()).rowWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0E-4};

			label1.setText("Aion's Karamja Fisher v5.1");
			label1.setFont(new Font("Palatino Linotype", Font.PLAIN, 20));
			contentPane.add(label1, new GridBagConstraints(0, 0, 2, 1, 0.0, 0.0,
					GridBagConstraints.CENTER, GridBagConstraints.NONE,
					new Insets(0, 0, 5, 5), 0, 0));

			checkBox2.setText("Use safe route");
			checkBox2.addActionListener(AionKaramjaFisher.this);
			checkBox2.setFont(new Font("Comic Sans MS", Font.PLAIN, 11));
			contentPane.add(checkBox2, new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0,
					GridBagConstraints.WEST, GridBagConstraints.NONE,
					new Insets(0, 0, 5, 5), 0, 0));

			checkBox5.setText("Show profits");
			checkBox5.addActionListener(AionKaramjaFisher.this);
			checkBox5.setFont(new Font("Comic Sans MS", Font.PLAIN, 11));
			contentPane.add(checkBox5, new GridBagConstraints(0, 2, 2, 1, 0.0, 0.0,
					GridBagConstraints.CENTER, GridBagConstraints.NONE,
					new Insets(0, 30, 5, 5), 0, 0));

			label2.setText("What would you like to fish?");
			label2.setFont(new Font("Comic Sans MS", Font.PLAIN, 12));
			contentPane.add(label2, new GridBagConstraints(0, 3, 1, 1, 0.0, 0.0,
					GridBagConstraints.CENTER, GridBagConstraints.BOTH,
					new Insets(0, 5, 5, 5), 0, 0));

			comboBox1.addItem("Lobsters");
			comboBox1.addItem("Tunas / Swordfishes");
			comboBox1.setFont(new Font("Comic Sans MS", Font.PLAIN, 11));
			comboBox1.addActionListener(AionKaramjaFisher.this);
			contentPane.add(comboBox1, new GridBagConstraints(0, 4, 1, 1, 0.0, 0.0,
					GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL,
					new Insets(0, 5, 5, 5), 0, 0));

			checkBox1.setText("Drop tunas");
			checkBox1.addActionListener(AionKaramjaFisher.this);
			checkBox1.setEnabled(false);
			checkBox1.setFont(new Font("Comic Sans MS", Font.PLAIN, 11));
			contentPane.add(checkBox1, new GridBagConstraints(1, 4, 1, 1, 0.0, 0.0,
					GridBagConstraints.CENTER, GridBagConstraints.NONE,
					new Insets(0, 0, 5, 5), 0, 0));

			checkBox3.setText("Take screenshot upon a new level");
			checkBox3.setSelected(true);
			checkBox3.addActionListener(AionKaramjaFisher.this);
			checkBox3.setFont(new Font("Comic Sans MS", Font.PLAIN, 11));
			contentPane.add(checkBox3, new GridBagConstraints(0, 5, 1, 1, 0.0, 0.0,
					GridBagConstraints.CENTER, GridBagConstraints.NONE,
					new Insets(0, 0, 5, 5), 0, 0));

			checkBox4.setText("Take screenshot when script ends");
			checkBox4.addActionListener(AionKaramjaFisher.this);
			checkBox4.setFont(new Font("Comic Sans MS", Font.PLAIN, 11));
			contentPane.add(checkBox4, new GridBagConstraints(0, 6, 1, 1, 0.0, 0.0,
					GridBagConstraints.CENTER, GridBagConstraints.NONE,
					new Insets(0, 0, 5, 5), 0, 0));

			button2.setText("Start");
			button2.addActionListener(AionKaramjaFisher.this);
			contentPane.add(button2, new GridBagConstraints(0, 7, 1, 1, 0.0, 0.0,
					GridBagConstraints.EAST, GridBagConstraints.NONE,
					new Insets(0, 0, 5, 5), 0, 0));

			button1.setText("Cancel");
			button1.addActionListener(AionKaramjaFisher.this);
			contentPane.add(button1, new GridBagConstraints(1, 7, 1, 1, 0.0, 0.0,
					GridBagConstraints.EAST, GridBagConstraints.VERTICAL,
					new Insets(0, 0, 5, 5), 0, 0));

			setTitle("GUI - Settings");
			setResizable(false);

			addWindowListener(new WindowAdapter() {
				@Override
				public void windowClosing(WindowEvent e) {
					AionKaramjaFisher.windowClosed = true;
				}
			});

			pack();
			setLocationRelativeTo(getOwner());
		}
	}
}