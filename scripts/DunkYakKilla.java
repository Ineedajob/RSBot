import org.rsbot.event.listeners.PaintListener;
import org.rsbot.script.Script;
import org.rsbot.script.ScriptManifest;
import org.rsbot.script.methods.Skills;
import org.rsbot.script.util.Filter;
import org.rsbot.script.wrappers.RSCharacter;
import org.rsbot.script.wrappers.RSNPC;
import org.rsbot.script.wrappers.RSTile;
import org.rsbot.util.GlobalConfiguration;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Properties;

@ScriptManifest(authors = "Dunnkers", name = "DunkYakKilla", keywords = "Combat", version = 1.4, description = "Kills yaks on Neitiznot.")
public class DunkYakKilla extends Script implements PaintListener {

	/* MISC */
	public double VERSION = 1;
	public String NAME = "";
	public long startTime = System.currentTimeMillis();
	public String status = "";
	public int kills = 0;
	public int MS = 4;
	public SkillInfo infoSkill;
	public int curAFKT;
	public long curAFKST;
	public boolean curAFK = false, run = false;
	private Properties settingsFile;
	public boolean killCounted = false;
	boolean menuContained = false;

	/* IDS */
	public int YAKDEADANIMATIONID = 5784;
	public String YAKNAME = "Yak";
	public int SKILLINTERFACE = 320;
	public int[] ATTACK = {125, 123, 121, 2428}, STRENGTH = {119, 117, 115,
			113}, DEFENCE = {137, 135, 133, 2432}, SUPERATTACK = {149, 147,
			145, 2436}, SUPERSTRENGTH = {161, 159, 157, 2440},
			SUPERDEFENCE = {167, 165, 163, 2442}, RANGED = {173, 171, 169,
			2444};

	/* SETTINGS */
	public int pouchID = 12029, pouchCost = 10, foodID = 7946, eatPercent = 50,
			foodHeal = 16;
	public boolean useAttack = false, useStrength = false, useDefence = false,
			superAttack = false, superStrength = false, superDefence = false,
			useRanged = false;
	public boolean mouseFollow = true;

	public boolean onStart() {
		VERSION = getClass().getAnnotation(ScriptManifest.class).version();
		NAME = getClass().getAnnotation(ScriptManifest.class).name();
		settingsFile = new Properties();
		final DunkYakKillaGUI GUI = new DunkYakKillaGUI();
		GUI.setVisible(true);
		while (GUI.isVisible()) {
			sleep(10);
		}
		if (superAttack)
			ATTACK = SUPERATTACK;
		if (superStrength)
			STRENGTH = SUPERSTRENGTH;
		if (superDefence)
			DEFENCE = SUPERDEFENCE;
		infoSkill = new SkillInfo();
		mouse.setSpeed(MS);
		return run;
	}

	private enum state {
		EAT, POTIONS, FAMILIAR, ATTACK, FIND, NONE
	}

	public state getState() {
		if (getHPPercent() < eatPercent) {
			if (inventory.contains(foodID)) {
				return state.EAT;
			} else if (getHPPercent() > 0) {
				log("We ran out of food and we're low on hp, stopping script.");
				stopScript();
			}
		}
		if (useAttack && inventory.containsOneOf(ATTACK)
				&& !isSkillBoosted(Skills.ATTACK) || useStrength
				&& inventory.containsOneOf(STRENGTH)
				&& !isSkillBoosted(Skills.STRENGTH) || useDefence
				&& inventory.containsOneOf(DEFENCE)
				&& !isSkillBoosted(Skills.DEFENSE) || useRanged
				&& !isSkillBoosted(Skills.RANGE)
				&& inventory.containsOneOf(RANGED)) {
			return state.POTIONS;
		}
		if (pouchID != 0 && inventory.contains(pouchID)
				&& !summoning.isFamiliarSummoned()
				&& summoning.getSummoningPoints() >= pouchCost) {
			return state.FAMILIAR;
		}
		if (needAttack()) {
			return state.ATTACK;
		}
		if (players.getMyPlayer().getInteracting() != null) {
			if (players.getMyPlayer().getInteracting().getHPPercent() < 30) {
				return state.FIND;
			}
		}
		return state.NONE;
	}

	public int loop() {
		switch (getState()) {
			case EAT:
				status = "Eating food...";
				int eatN = getFoodToHeal();
				log("[eat] eating " + eatN + " food with id: (" + foodID + ")");
				for (int i = 0; i < eatN; i++) {
					inventory.getItem(foodID).doAction("Eat");
					sleep(random(1500, 2000));
				}
				break;
			case POTIONS:
				status = "Drinking potions...";
				int[] potions = {};
				if (useAttack && !isSkillBoosted(Skills.ATTACK))
					potions = ATTACK;
				if (useStrength && !isSkillBoosted(Skills.STRENGTH))
					potions = STRENGTH;
				if (useDefence && !isSkillBoosted(Skills.DEFENSE))
					potions = DEFENCE;
				if (useRanged && !isSkillBoosted(Skills.RANGE))
					potions = RANGED;
				int id = 0;
				for (int potion : potions) {
					if (inventory.contains(potion)) {
						id = potion;
						break;
					}
				}
				if (id != 0) {
					log("[potions] drinking potion with id (" + id + ")");
					try {
						inventory.getItem(id).doAction("Drink");
					} catch (Exception e) {
						e.printStackTrace();
					}
					sleep(random(1000, 1250));
				}
			case FAMILIAR:
				status = "Summoning familiar...";
				log("[familiar] summoning familiar with pouch id: " + pouchID);
				try {
					inventory.getItem(pouchID).doAction("Summon");
				} catch (Exception e) {
				}
				sleep(random(1000, 2000));
				break;
			case ATTACK:
				status = "Attacking...";
				if (menuContains("Attack Yak")) {
					mouse.click(true);
					sleep(random(500, 1000));
				} else {
					RSNPC toAttack = getNPCToAttack();
					if (toAttack != null) {
						if (toAttack.isOnScreen()) {
							toAttack.doAction("Attack");
							sleep(random(500, 1000));
						} else {
							new turnToYak().start();
							if (calc.distanceTo(toAttack) > 5) {
								walking.walkTileMM(toAttack.getLocation());
							}
						}
					} else {
						if (random(0, 3) == 2) {
							RSTile[] area = {new RSTile(2327, 3792),
									new RSTile(2323, 3795), new RSTile(2320, 3794),
									new RSTile(2323, 3792), new RSTile(2323, 3795)};
							walking.walkTileMM(area[random(0, area.length)]);
							sleep(random(1000, 2000));
						}
					}
				}
				break;
			case FIND:
				status = "Finding new opponent...";
				RSNPC nextOpponent = getNPCToAttack();
				if (nextOpponent != null) {
					if (!nextOpponent.isOnScreen()) {
						camera.turnToCharacter(nextOpponent);
					} else if (mouseFollow) {
						if (!menuContains("Attack")) {
							Point p = nextOpponent.getScreenLocation();
							if (calc.pointOnScreen(p)) {
								mouse.move(p.x, p.y);
							}
						}
					}
				}
				break;
			case NONE:
				status = "Waiting...";
				antiban();
				break;
		}
		return 1;
	}

	public void antiban() {
		int t = random(0, 100);
		switch (t) {
			case 1:
				if (random(0, 10) == 5) {
					log("[antiban] move mouse");
					mouse.moveSlightly();
				}
				break;
			case 2:
				if (random(0, 25) == 5) {
					log("[antiban] check combat skill");
					int[] childs = {1, 2, 4, 22, 46, 87};
					game.openTab(1);
					interfaces.getComponent(SKILLINTERFACE,
							childs[random(0, childs.length)]).doHover();
					sleep(random(1000, 2000));
				}
				break;
			case 3:
				if (random(0, 30) == 5) {
					log("[antiban] open random tab and move mouse within");
					game.openTab(random(0, 15));
					mouse.move(random(550, 735), random(210, 465));
					sleep(random(1000, 2000));
				}
				break;
			case 4:
				if (random(0, 30) == 5) {
					log("[antiban] open random tab");
					game.openTab(random(0, 15));
					sleep(random(500, random(1000, 2000)));
				}
				break;
			case 5:
				if (random(0, 40) == 5) {
					log("[antiban] check objective");
					game.openTab(8);
					interfaces.getComponent(891, 10).doHover();
					sleep(random(1000, 2000));
				}
				break;
			case 6:
				if (random(0, 70) == 5) {
					log("[antiban] away from keyboard");
					mouse.moveOffScreen();
					curAFKST = System.currentTimeMillis();
					curAFKT = random(0, random(5000, random(10000, random(15000,
							20000))));
					log("[antiban] afk for " + curAFKT);
					curAFK = true;
					sleep(curAFKT);
					curAFK = false;
				}
				break;
			default:
				break;
		}
	}

	public void onFinish() {
	}

	public void onRepaint(final Graphics g) {
		((Graphics2D) g).setRenderingHint(RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_ON);
		g.setFont(new Font("Tahoma", Font.PLAIN, 13));
		g.setColor(Color.WHITE);
		String[] list = {
				NAME + " v" + VERSION,
				"Runtime: "
						+ timeFormat((int) (System.currentTimeMillis() - startTime)),
				"Status: " + status, "Kills: " + numberFormat(kills)};
		drawColumn(g, list, 7, 337, -3, false, true);
		ArrayList<String> infoSkillsRaw = new ArrayList<String>();
		for (int i = 0; i < infoSkill.skillAmount; i++) {
			if (infoSkill.getExpGained(i) > 0) {
				infoSkillsRaw.add("");
				infoSkillsRaw.add(setFirstUppercase(infoSkill.getSkillName(i))
						+ " exp gained: "
						+ numberFormat(infoSkill.getExpGained(i)));
				infoSkillsRaw.add("  Exp per hour: "
						+ numberFormat((int) getPerHour(infoSkill
						.getExpGained(i), startTime)));
			}
		}
		String[] infoSkills = toArray(infoSkillsRaw);
		g.setFont(new Font("Tahoma", Font.PLAIN, 12));
		drawColumn(g, infoSkills, 515, 337, -3, true, true);
		g.setColor(setAlpha(Color.GREEN, 150));
		RSNPC[] a = npcs.getAll(new Filter<RSNPC>() {
			public boolean accept(RSNPC v) {
				return isValidToAttack(v);
			}
		});
		for (RSNPC b : a) {
			Point l = calc.tileToScreen(b.getLocation(), b.getHeight() / 2);
			if (!calc.pointOnScreen(l))
				continue;
			g.drawOval(l.x - 5, l.y - 5, 10, 10);
			g.setColor(setAlpha(Color.GREEN, 150));
			// drawModel(g, b);
		}
		g.setColor(Color.YELLOW);
		if (curAFK) {
			drawNote(g, true, new String[]{
					"Away from keyboard for " + secondFormat(curAFKT)
							+ " seconds...",
					"Time left: "
							+ secondFormat((int) (curAFKT - (System
							.currentTimeMillis() - curAFKST)))});
		}
		RSCharacter opponent = players.getMyPlayer().getInteracting();
		if (opponent != null) {
			if (opponent.getAnimation() == YAKDEADANIMATIONID && !killCounted) {
				kills++;
				killCounted = true;
			}
		}
		if (opponent == null || opponent.getHPPercent() > 1) {
			killCounted = false;
		}
	}

	/* VOIDS */
	public void drawColumn(Graphics g, String[] column, int x, int y,
						   int offset, boolean reverseHorizontal, boolean reverseVertical) {
		int h = g.getFontMetrics().getHeight(), xp = x;
		h += offset;
		if (reverseVertical) {
			y -= (column.length - 1) * h;
		} else {
			y += h;
		}
		for (int i = 0; i < column.length; i++) {
			x = xp;
			if (reverseHorizontal) {
				x += -g.getFontMetrics().stringWidth(column[i]);
			}
			g.drawString(column[i], x, y + (h * i));
		}
	}

	public void drawNote(Graphics g, boolean dark, String[] strA) {
		if (dark) {
			g.setColor(setAlpha(Color.BLACK, 125));
			g.fillRect(0, 0, game.getWidth(), game.getHeight());
		}
		int MW = getMostWidth(g, strA), w = MW + (((MW / 2) / 2) / 2), oY = g
				.getFontMetrics().getHeight(), x = (game.getWidth() / 2)
				- (MW / 2), y = (game.getHeight() / 2)
				- ((oY * strA.length) / 2), h = oY * strA.length + 5;
		g.setColor(setAlpha(Color.WHITE, 200));
		g.fill3DRect(x, y, w, h, true);
		g.setColor(Color.BLACK);
		for (int i = 0; i < strA.length; i++) {
			g.drawString(strA[i],
					x
							+ ((w / 2) - (g.getFontMetrics().stringWidth(
							strA[i]) / 2)), y + oY * (i + 1));
		}
	}

	public void drawModel(Graphics g, RSNPC o) {
		Polygon[] model = o.getModel().getTriangles();
		for (Polygon p : model) {
			g.drawPolygon(p);
		}
	}

	/* BOOLEANS */
	public boolean needAttack() {
		RSCharacter opponent = players.getMyPlayer().getInteracting();
		return opponent == null
				|| opponent.getAnimation() == YAKDEADANIMATIONID;
	}

	public boolean isValidToAttack(RSNPC v) {
		return v.getName().equals(YAKNAME) && v.getInteracting() == null
				&& !v.isInCombat();
	}

	public boolean isSkillBoosted(int skill) {
		return skills.getRealLevel(skill) != skills.getCurrentLevel(skill);
	}

	public boolean menuContains(String action) {
		try {
			String[] actions = menu.getItems();
			for (String a : actions) {
				if (a.contains(action)) {
					return true;
				}
			}
		} catch (Exception e) {
		}
		return false;
	}

	/* INTS */
	public int getFoodToHeal() {
		return (getMaxHP() - getHP()) / (foodHeal * 10);
	}

	public int getHPPercent() {
		if (game.isLoggedIn()) {
			try {
				float currentHP = Integer.parseInt(interfaces.getComponent(748,
						8).getText());
				float maxHP = Integer.parseInt(interfaces
						.getComponent(320, 190).getText()) * 10;
				float HPPercent = (currentHP / maxHP) * 100;
				if (HPPercent > 100) {
					game.openTab(1);
				}
				return (int) ((currentHP / maxHP) * 100);
			} catch (NumberFormatException e) {
				return 100;
			}
		} else {
			return 100;
		}
	}

	public int getHP() {
		try {
			return Integer.parseInt(interfaces.getComponent(748, 8).getText());
		} catch (NumberFormatException e) {
			return -1;
		}
	}

	public int getMaxHP() {
		try {
			return Integer
					.parseInt(interfaces.getComponent(320, 190).getText()) * 10;
		} catch (NumberFormatException e) {
			return -1;
		}
	}

	public int getMostWidth(Graphics g, String[] array) {
		int o = 0;
		for (String str : array) {
			int d = g.getFontMetrics().stringWidth(str);
			if (d > o) {
				o = d;
			}
		}
		return o;
	}

	/* STRINGS */
	public String timeFormat(final int currentTime) {
		NumberFormat nf = NumberFormat.getInstance();
		nf.setMinimumIntegerDigits(2);
		final long millisecondsL = currentTime / 10;
		final long secondsL = currentTime / 1000;
		final long minutesL = secondsL / 60;
		final long hoursL = minutesL / 60;
		long milliseconds = (int) (millisecondsL % 60);
		final int seconds = (int) (secondsL % 60);
		final int minutes = (int) (minutesL % 60);
		final int hours = (int) (hoursL % 60);
		final String h = nf.format(hours), m = nf.format(minutes), s = nf
				.format(seconds);
		nf.setMinimumIntegerDigits(1);
		milliseconds = milliseconds / 10;
		final String ms = nf.format(milliseconds);
		return (h + ":" + m + ":" + s + "." + ms);
	}

	String secondFormat(final int currentTime) {
		NumberFormat nf = NumberFormat.getInstance();
		nf.setMinimumIntegerDigits(1);
		String ms = nf.format(((currentTime / 10) % 60) / 10);
		String s = nf.format((currentTime / 1000) % 60);
		return (s + "." + ms);
	}

	public String numberFormat(final int number) {
		NumberFormat nf = NumberFormat.getInstance(Locale.UK);
		return (nf.format(number));
	}

	public String setFirstUppercase(String str) {
		return Character.toUpperCase(str.charAt(0))
				+ str.substring(1).toLowerCase();
	}

	public String[] toArray(ArrayList<String> a) {
		String[] result = new String[a.size()];
		for (int i = 0; i < a.size(); i++) {
			result[i] = a.get(i);
		}
		return result;
	}

	/* OTHER */
	public RSNPC getNPCToAttack() {
		return npcs.getNearest(new Filter<RSNPC>() {
			public boolean accept(RSNPC v) {
				return isValidToAttack(v);
			}
		});
	}

	public Color setAlpha(Color o, int alpha) {
		return new Color(o.getRed(), o.getGreen(), o.getBlue(), alpha);
	}

	public float getPerMili(final int amount, long startTime) {
		return ((float) amount)
				/ ((float) (System.currentTimeMillis() - startTime));
	}

	public float getPerSec(final int amount, long startTime) {
		return getPerMili(amount, startTime) * 1000;
	}

	public float getPerMin(final int amount, long startTime) {
		return getPerSec(amount, startTime) * 60;
	}

	public float getPerHour(final int amount, long startTime) {
		return getPerMin(amount, startTime) * 60;
	}

	public class SkillInfo {

		public int skillAmount = Skills.SKILL_NAMES.length - 1;
		public int[] expStarts = new int[skillAmount];
		public int[] lvlStarts = new int[skillAmount];
		public boolean initialized = false;

		public int getExpGained(int skill) {
			if (!initialized) {
				if (game.isLoggedIn() && skillsInitialized()) {
					initialize();
				}
				return 0;
			}
			return skills.getCurrentExp(skill) - expStarts[skill];
		}

		public String getSkillName(int skill) {
			return Skills.SKILL_NAMES[skill];
		}

		public void initialize() {
			for (int i = 0; i < skillAmount; i++) {
				expStarts[i] = skills.getCurrentExp(i);
				lvlStarts[i] = skills.getRealLevel(i);
			}
			initialized = true;
		}

		public boolean skillsInitialized() {
			return skills.getCurrentExp(Skills.CONSTITUTION) > 5;
		}
	}

	public class turnToYak extends Thread {

		public void run() {
			camera.turnToCharacter(getNPCToAttack());
		}
	}

	public class DunkYakKillaGUI extends JFrame {

		private static final long serialVersionUID = 1L;
		private JPanel dialogPane;
		private JPanel contentPanel;
		private JLabel title;
		private JLabel label1;
		private JLabel label2;
		private JLabel label3;
		private JLabel label4;
		private JLabel label5;
		private JLabel label6;
		private JLabel label7;
		private JComboBox attack;
		private JComboBox strength;
		private JComboBox defence;
		private JTextField food;
		private JTextField pouch;
		private JSlider mousespeed;
		private JLabel label8;
		private JLabel label9;
		private JLabel label10;
		private JCheckBox mousefollow;
		private JLabel label11;
		private JComboBox ranged;
		private JPanel buttonBar;
		private JButton okButton;
		private JButton cancelButton;

		public DunkYakKillaGUI() {
			initComponents();
		}

		private void okButtonActionPerformed() {
			saveSettings();
			saveSettingsFile(settingsFile);
			run = true;
			dispose();
		}

		private void cancelButtonActionPerformed() {
			dispose();
		}

		private void loadSettings() {
			if (loadSettingsFile(settingsFile)) {
				if (settingsFile.getProperty("attack") != null) {
					attack.setSelectedItem(settingsFile.getProperty("attack"));
				}
				if (settingsFile.getProperty("strength") != null) {
					strength.setSelectedItem(settingsFile
							.getProperty("strength"));
				}
				if (settingsFile.getProperty("defence") != null) {
					defence
							.setSelectedItem(settingsFile
									.getProperty("defence"));
				}
				if (settingsFile.getProperty("pouchID") != null) {
					pouch.setText(settingsFile.getProperty("pouchID"));
				}
				if (settingsFile.getProperty("foodID") != null) {
					food.setText(settingsFile.getProperty("foodID"));
				}
				if (settingsFile.getProperty("mouseSpeed") != null) {
					mousespeed.setValue(Integer.parseInt(settingsFile
							.getProperty("mouseSpeed")));
				}
				if (settingsFile.getProperty("mouseFollow") != null) {
					mousefollow.setSelected(settingsFile.getProperty(
							"mouseFollow").equals("true"));
				}
				if (settingsFile.getProperty("ranged") != null) {
					ranged.setSelectedItem(settingsFile.getProperty("ranged"));
				}
			}
		}

		private void saveSettings() {
			String att = attack.getSelectedItem().toString();
			settingsFile.setProperty("attack", att);
			if (!att.equals("Dont use")) {
				useAttack = true;
				if (att.equals("Super")) {
					superAttack = true;
				}
			}
			String str = strength.getSelectedItem().toString();
			settingsFile.setProperty("strength", str);
			if (!str.equals("Dont use")) {
				useStrength = true;
				if (str.equals("Super")) {
					superStrength = true;
				}
			}
			String def = defence.getSelectedItem().toString();
			settingsFile.setProperty("defence", def);
			if (!def.equals("Dont use")) {
				useDefence = true;
				if (def.equals("Super")) {
					superDefence = true;
				}
			}
			try {
				foodID = Integer.parseInt(food.getText());
				settingsFile.setProperty("foodID", food.getText());
			} catch (NumberFormatException ignored) {
				foodID = 0;
				settingsFile.remove("foodID");
			}
			try {
				pouchID = Integer.parseInt(pouch.getText());
				settingsFile.setProperty("pouchID", pouch.getText());
			} catch (NumberFormatException ignored) {
				pouchID = 0;
				settingsFile.remove("pouchID");
			}
			MS = mousespeed.getValue();
			settingsFile.setProperty("mouseSpeed", "" + MS);
			mouseFollow = mousefollow.isSelected();
			settingsFile.setProperty("mouseFollow", "" + mouseFollow);
			String range = ranged.getSelectedItem().toString();
			settingsFile.setProperty("ranged", range);
			if (!range.equals("Dont use")) {
				useRanged = true;
			}
		}

		public boolean loadSettingsFile(Properties f) {
			try {
				f.load(new FileInputStream(new File(GlobalConfiguration.Paths
						.getSettingsDirectory(), NAME + "Settings.ini")));
				return true;
			} catch (FileNotFoundException e) {
				return false;
			} catch (IOException e) {
				return false;
			}
		}

		public boolean saveSettingsFile(Properties f) {
			try {
				f.store(new FileWriter(new File(GlobalConfiguration.Paths
						.getSettingsDirectory(), NAME + "Settings.ini")),
						"The settings of " + NAME);
				return true;
			} catch (IOException e) {
				return false;
			}
		}

		private void initComponents() {
			dialogPane = new JPanel();
			contentPanel = new JPanel();
			title = new JLabel();
			label1 = new JLabel();
			label2 = new JLabel();
			label3 = new JLabel();
			label4 = new JLabel();
			label5 = new JLabel();
			label6 = new JLabel();
			label7 = new JLabel();
			attack = new JComboBox();
			strength = new JComboBox();
			defence = new JComboBox();
			food = new JTextField();
			pouch = new JTextField();
			mousespeed = new JSlider();
			label8 = new JLabel();
			label9 = new JLabel();
			label10 = new JLabel();
			mousefollow = new JCheckBox();
			label11 = new JLabel();
			ranged = new JComboBox();
			buttonBar = new JPanel();
			okButton = new JButton();
			cancelButton = new JButton();

			// ======== this ========
			setResizable(false);
			setTitle("DunkYakKilla");
			setBackground(Color.white);
			Container contentPane = getContentPane();
			contentPane.setLayout(new BorderLayout());

			// ======== dialogPane ========
			{
				dialogPane.setBorder(new EmptyBorder(12, 12, 12, 12));
				dialogPane.setBackground(Color.white);
				dialogPane.setLayout(new BorderLayout());

				// ======== contentPanel ========
				{
					contentPanel.setInheritsPopupMenu(true);
					contentPanel.setBackground(Color.white);
					contentPanel.setPreferredSize(new Dimension(190, 300));

					// ---- title ----
					title.setText("DunkYakKilla");
					title.setFont(new Font("Tahoma", Font.BOLD, 16));
					title.setHorizontalAlignment(SwingConstants.CENTER);

					// ---- label1 ----
					label1.setText("Attack potion: ");

					// ---- label2 ----
					label2.setText("Strength potion:");

					// ---- label3 ----
					label3.setText("Defence potion:");

					// ---- label4 ----
					label4.setText("Familiar pouch ID:");

					// ---- label5 ----
					label5.setText("Food ID:");

					// ---- label6 ----
					label6.setText("Leave a textfield blank to disable");

					// ---- label7 ----
					label7.setText("All settings are auto saved");
					label7.setFont(new Font("Tahoma", Font.BOLD, 11));
					label7.setHorizontalAlignment(SwingConstants.CENTER);

					String[] model = new String[]{"Dont use", "Normal",
							"Super"};

					// ---- attack ----
					attack.setModel(new DefaultComboBoxModel(model));

					// ---- strength ----
					strength.setModel(new DefaultComboBoxModel(model));

					// ---- defence ----
					defence.setModel(new DefaultComboBoxModel(model));

					// ---- mousespeed ----
					mousespeed.setBackground(Color.white);
					mousespeed.setMaximum(8);
					mousespeed.setValue(4);
					mousespeed.setPaintLabels(true);
					mousespeed.setSnapToTicks(true);
					mousespeed.setMajorTickSpacing(1);
					mousespeed.setMinimum(2);

					// ---- label8 ----
					label8.setText("Mouse speed");
					label8.setHorizontalAlignment(SwingConstants.CENTER);

					// ---- label9 ----
					label9.setText("Fast");

					// ---- label10 ----
					label10.setText("Slow");

					// ---- mousefollow ----
					mousefollow.setText("Follow next target with mouse");
					mousefollow.setSelected(true);

					// ---- label11 ----
					label11.setText("Ranged potion:");

					// ---- ranged ----
					ranged.setModel(new DefaultComboBoxModel(new String[]{
							"Dont use", "Normal"}));

					GroupLayout contentPanelLayout = new GroupLayout(
							contentPanel);
					contentPanel.setLayout(contentPanelLayout);
					contentPanelLayout
							.setHorizontalGroup(contentPanelLayout
									.createParallelGroup()
									.addGroup(
											contentPanelLayout
													.createSequentialGroup()
													.addContainerGap()
													.addGroup(
															contentPanelLayout
																	.createParallelGroup()
																	.addGroup(
																			contentPanelLayout
																					.createSequentialGroup()
																					.addGroup(
																							contentPanelLayout
																									.createParallelGroup()
																									.addGroup(
																											contentPanelLayout
																													.createSequentialGroup()
																													.addComponent(
																															label2,
																															GroupLayout.PREFERRED_SIZE,
																															81,
																															GroupLayout.PREFERRED_SIZE)
																													.addGap(
																															18,
																															18,
																															18)
																													.addComponent(
																															strength,
																															0,
																															71,
																															Short.MAX_VALUE))
																									.addGroup(
																											contentPanelLayout
																													.createSequentialGroup()
																													.addComponent(
																															label1,
																															GroupLayout.PREFERRED_SIZE,
																															81,
																															GroupLayout.PREFERRED_SIZE)
																													.addGap(
																															18,
																															18,
																															18)
																													.addComponent(
																															attack,
																															0,
																															71,
																															Short.MAX_VALUE))
																									.addComponent(
																											title,
																											GroupLayout.Alignment.TRAILING,
																											GroupLayout.DEFAULT_SIZE,
																											170,
																											Short.MAX_VALUE)
																									.addGroup(
																											contentPanelLayout
																													.createSequentialGroup()
																													.addComponent(
																															label3,
																															GroupLayout.PREFERRED_SIZE,
																															81,
																															GroupLayout.PREFERRED_SIZE)
																													.addGap(
																															18,
																															18,
																															18)
																													.addGroup(
																															contentPanelLayout
																																	.createParallelGroup()
																																	.addComponent(
																																			ranged,
																																			0,
																																			71,
																																			Short.MAX_VALUE)
																																	.addComponent(
																																			defence,
																																			0,
																																			71,
																																			Short.MAX_VALUE))))
																					.addGap(
																							10,
																							10,
																							10))
																	.addGroup(
																			contentPanelLayout
																					.createSequentialGroup()
																					.addComponent(
																							label11)
																					.addContainerGap(
																							106,
																							Short.MAX_VALUE))
																	.addGroup(
																			contentPanelLayout
																					.createSequentialGroup()
																					.addComponent(
																							label6)
																					.addContainerGap(
																							22,
																							Short.MAX_VALUE))
																	.addGroup(
																			contentPanelLayout
																					.createSequentialGroup()
																					.addGroup(
																							contentPanelLayout
																									.createParallelGroup()
																									.addComponent(
																											label5,
																											GroupLayout.PREFERRED_SIZE,
																											55,
																											GroupLayout.PREFERRED_SIZE)
																									.addComponent(
																											label4))
																					.addPreferredGap(
																							LayoutStyle.ComponentPlacement.RELATED,
																							13,
																							Short.MAX_VALUE)
																					.addGroup(
																							contentPanelLayout
																									.createParallelGroup(
																											GroupLayout.Alignment.LEADING,
																											false)
																									.addComponent(
																											pouch)
																									.addComponent(
																											food,
																											GroupLayout.PREFERRED_SIZE,
																											71,
																											GroupLayout.PREFERRED_SIZE))
																					.addContainerGap())
																	.addGroup(
																			contentPanelLayout
																					.createSequentialGroup()
																					.addComponent(
																							label9)
																					.addPreferredGap(
																							LayoutStyle.ComponentPlacement.RELATED)
																					.addComponent(
																							label8,
																							GroupLayout.DEFAULT_SIZE,
																							118,
																							Short.MAX_VALUE)
																					.addPreferredGap(
																							LayoutStyle.ComponentPlacement.RELATED)
																					.addComponent(
																							label10)
																					.addContainerGap())
																	.addGroup(
																			contentPanelLayout
																					.createSequentialGroup()
																					.addComponent(
																							mousespeed,
																							GroupLayout.DEFAULT_SIZE,
																							170,
																							Short.MAX_VALUE)
																					.addContainerGap())
																	.addGroup(
																			contentPanelLayout
																					.createSequentialGroup()
																					.addComponent(
																							mousefollow)
																					.addContainerGap(
																							9,
																							Short.MAX_VALUE))
																	.addGroup(
																			contentPanelLayout
																					.createSequentialGroup()
																					.addComponent(
																							label7,
																							GroupLayout.DEFAULT_SIZE,
																							170,
																							Short.MAX_VALUE)
																					.addContainerGap()))));
					contentPanelLayout
							.setVerticalGroup(contentPanelLayout
									.createParallelGroup()
									.addGroup(
											contentPanelLayout
													.createSequentialGroup()
													.addComponent(title)
													.addPreferredGap(
															LayoutStyle.ComponentPlacement.RELATED)
													.addGroup(
															contentPanelLayout
																	.createParallelGroup(
																			GroupLayout.Alignment.BASELINE)
																	.addComponent(
																			attack,
																			GroupLayout.PREFERRED_SIZE,
																			GroupLayout.DEFAULT_SIZE,
																			GroupLayout.PREFERRED_SIZE)
																	.addComponent(
																			label1))
													.addPreferredGap(
															LayoutStyle.ComponentPlacement.RELATED)
													.addGroup(
															contentPanelLayout
																	.createParallelGroup(
																			GroupLayout.Alignment.BASELINE)
																	.addComponent(
																			label2)
																	.addComponent(
																			strength,
																			GroupLayout.PREFERRED_SIZE,
																			GroupLayout.DEFAULT_SIZE,
																			GroupLayout.PREFERRED_SIZE))
													.addPreferredGap(
															LayoutStyle.ComponentPlacement.RELATED)
													.addGroup(
															contentPanelLayout
																	.createParallelGroup(
																			GroupLayout.Alignment.BASELINE)
																	.addComponent(
																			label3)
																	.addComponent(
																			defence,
																			GroupLayout.PREFERRED_SIZE,
																			GroupLayout.DEFAULT_SIZE,
																			GroupLayout.PREFERRED_SIZE))
													.addPreferredGap(
															LayoutStyle.ComponentPlacement.RELATED)
													.addGroup(
															contentPanelLayout
																	.createParallelGroup(
																			GroupLayout.Alignment.BASELINE)
																	.addComponent(
																			label11)
																	.addComponent(
																			ranged,
																			GroupLayout.PREFERRED_SIZE,
																			GroupLayout.DEFAULT_SIZE,
																			GroupLayout.PREFERRED_SIZE))
													.addPreferredGap(
															LayoutStyle.ComponentPlacement.RELATED)
													.addComponent(label6)
													.addPreferredGap(
															LayoutStyle.ComponentPlacement.RELATED)
													.addGroup(
															contentPanelLayout
																	.createParallelGroup(
																			GroupLayout.Alignment.BASELINE)
																	.addComponent(
																			label5)
																	.addComponent(
																			food,
																			GroupLayout.PREFERRED_SIZE,
																			GroupLayout.DEFAULT_SIZE,
																			GroupLayout.PREFERRED_SIZE))
													.addPreferredGap(
															LayoutStyle.ComponentPlacement.RELATED)
													.addGroup(
															contentPanelLayout
																	.createParallelGroup(
																			GroupLayout.Alignment.BASELINE)
																	.addComponent(
																			label4)
																	.addComponent(
																			pouch,
																			GroupLayout.PREFERRED_SIZE,
																			GroupLayout.DEFAULT_SIZE,
																			GroupLayout.PREFERRED_SIZE))
													.addPreferredGap(
															LayoutStyle.ComponentPlacement.RELATED)
													.addGroup(
															contentPanelLayout
																	.createParallelGroup(
																			GroupLayout.Alignment.BASELINE)
																	.addComponent(
																			label9)
																	.addComponent(
																			label10)
																	.addComponent(
																			label8))
													.addPreferredGap(
															LayoutStyle.ComponentPlacement.RELATED)
													.addComponent(
															mousespeed,
															GroupLayout.PREFERRED_SIZE,
															GroupLayout.DEFAULT_SIZE,
															GroupLayout.PREFERRED_SIZE)
													.addPreferredGap(
															LayoutStyle.ComponentPlacement.RELATED)
													.addComponent(mousefollow)
													.addPreferredGap(
															LayoutStyle.ComponentPlacement.RELATED)
													.addComponent(label7)
													.addContainerGap(
															GroupLayout.DEFAULT_SIZE,
															Short.MAX_VALUE)));
				}
				dialogPane.add(contentPanel, BorderLayout.NORTH);

				// ======== buttonBar ========
				{
					buttonBar.setBorder(new EmptyBorder(12, 0, 0, 0));
					buttonBar.setBackground(Color.white);
					buttonBar.setLayout(new GridBagLayout());
					((GridBagLayout) buttonBar.getLayout()).columnWidths = new int[]{
							0, 85, 80};
					((GridBagLayout) buttonBar.getLayout()).columnWeights = new double[]{
							1.0, 0.0, 0.0};

					// ---- okButton ----
					okButton.setText("OK");
					okButton.addActionListener(new ActionListener() {
						public void actionPerformed(ActionEvent e) {
							okButtonActionPerformed();
						}
					});
					buttonBar.add(okButton, new GridBagConstraints(1, 0, 1, 1,
							0.0, 0.0, GridBagConstraints.CENTER,
							GridBagConstraints.BOTH, new Insets(0, 0, 0, 5), 0,
							0));

					// ---- cancelButton ----
					cancelButton.setText("Cancel");
					cancelButton.addActionListener(new ActionListener() {
						public void actionPerformed(ActionEvent e) {
							cancelButtonActionPerformed();
						}
					});
					buttonBar.add(cancelButton, new GridBagConstraints(2, 0, 1,
							1, 0.0, 0.0, GridBagConstraints.CENTER,
							GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0,
							0));
				}
				dialogPane.add(buttonBar, BorderLayout.SOUTH);
			}
			contentPane.add(dialogPane, BorderLayout.CENTER);
			pack();
			setLocationRelativeTo(getOwner());
			loadSettings();
		}
	}
}