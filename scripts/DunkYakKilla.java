import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.RenderingHints;
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

import javax.swing.DefaultComboBoxModel;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.LayoutStyle;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;

import org.rsbot.event.listeners.PaintListener;
import org.rsbot.script.Script;
import org.rsbot.script.ScriptManifest;
import org.rsbot.script.methods.Skills;
import org.rsbot.script.util.Filter;
import org.rsbot.script.wrappers.RSCharacter;
import org.rsbot.script.wrappers.RSNPC;
import org.rsbot.script.wrappers.RSTile;
import org.rsbot.util.GlobalConfiguration;

@ScriptManifest(authors = "Dunnkers", name = "DunkYakKilla", keywords = "Combat", version = 1.1, description = "Kills yaks on Neitiznot.")
public class DunkYakKilla extends Script implements PaintListener {

	/* MISC */
	public double VERSION = 1;
	public String NAME = "";
	public long startTime = System.currentTimeMillis();
	public String status = "";
	public int kills = 0;
	public skillInfo infoSkill;
	public int curAFKT;
	public long curAFKST;
	public boolean curAFK = false, run = false;
	private Properties settingsFile;

	/* IDS */
	public int /*YAKID = 5529,*/ YAKDEADANIMATIONID = 5784;
	//public int YAKHIDE = 10818;
	public String YAKNAME = "Yak";
	//public int BANKID = 21301;
	public int SKILLINTERFACE = 320;
	public int[] ATTACK = {125, 123, 121, 2428}, STRENGTH = {119, 117, 115,
			113}, DEFENCE = {137, 135, 133, 2432}, SUPERATTACK = {149, 147,
			145, 2436}, SUPERSTRENGTH = {161, 159, 157, 2440},
			SUPERDEFENCE = {167, 165, 163, 2442};

	/* SETTINGS */
	public int pouchID = 12029, pouchCost = 10, foodID = 7946, eatPercent = 50,
			foodHeal = 16;
	public boolean /*useFamiliar = false,*/ useAttack = false, useStrength = false,
			useDefence = false, superAttack = false, superStrength = false,
			superDefence = false;

	public boolean onStart() {
		/*
		 * TODO LIST - MAKE METHOD getPotionDrinkArray() - SUPPORT BANKING
		 */
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
		infoSkill = new skillInfo();
		mouse.setSpeed(5);
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
				&& !isSkillBoosted(Skills.DEFENSE)) {
			return state.POTIONS;
		}
		if (inventory.contains(pouchID) && !summoning.isFamiliarSummoned()
				&& summoning.getSummoningPoints() >= pouchCost) {
			return state.FAMILIAR;
		}
		if (needAttack()) {
			return state.ATTACK;
		}
		if (getMyPlayer().getInteracting() != null) {
			if (getMyPlayer().getInteracting().getHPPercent() < 10) {
				return state.FIND;
			}
		}
		return state.NONE;
	}

	public int loop() {
		if (getMyPlayer().getInteracting() != null) {
			if (getMyPlayer().getInteracting().getAnimation() == YAKDEADANIMATIONID) {
				kills++;
			}
		}
		switch (getState()) {
			case EAT:
				status = "Eating food...";
				int eatN = getFoodToHeal();
				log("[eat] eating " + eatN + " food to heal. food: (" + foodID
						+ ")");
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
				try {
					inventory.getItem(pouchID).doAction("Summon");
				} catch (Exception e) {
					e.printStackTrace();
				}
				sleep(random(1000, 2000));
				break;
			case ATTACK:
				status = "Attacking...";
				RSNPC toAttack = getNPCToAttack();
				if (toAttack != null) {
					if (toAttack.isOnScreen()) {
						toAttack.doAction("Attack");
					} else {
						if (calc.distanceTo(toAttack) > 5) {
							walking.walkTileMM(toAttack.getLocation());
						}
						camera.turnToCharacter(toAttack);
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
				sleep(random(100, 1000));
				break;
			case FIND:
				status = "Finding new opponent...";
				RSNPC nextOpponent = getNPCToAttack();
				if (nextOpponent != null) {
					if (!nextOpponent.isOnScreen()) {
						camera.turnToCharacter(nextOpponent);
					} else {
						Point p = nextOpponent.getScreenLocation();
						if (calc.pointOnScreen(p)) {
							mouse
									.move(p.x - random(-10, 10), p.y
											- random(-10, 10));
						}
					}
					sleep(random(1000, 2000));
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
				"Runtime: " + timeFormat((int) (System.currentTimeMillis() - startTime)),
				"Status: " + status, "Kills: " + numberFormat(kills)};
		drawColumn(g, list, 7, 337 - 4 - (list.length * (g.getFontMetrics()
				.getHeight() - 3)), -3, false);
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
		drawColumn(g, infoSkills, 515, 337 - 4 - (infoSkills.length * (g
				.getFontMetrics().getHeight() - 3)), -3, true);
		g.setColor(setAlpha(Color.GREEN, 150));
		RSNPC[] a = npcs.getAll(new Filter<RSNPC>() {
			public boolean accept(RSNPC v) {
				return isValidToAttack(v);
			}
		});
		for (RSNPC b : a) {
			Point l = b.getScreenLocation();
			g.drawOval(l.x - 5, l.y - 5, 10, 10);
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
	}

	/* VOIDS */
	public void drawColumn(Graphics g, String[] column, int x, int y,
						   int offset, boolean alignRight) {
		int h = g.getFontMetrics().getHeight(), xp = x;
		y += h;
		for (int i = 0; i < column.length; i++) {
			x = xp;
			if (alignRight)
				x += -g.getFontMetrics().stringWidth(column[i]);
			y += offset;
			g.drawString(column[i], x, (y + h * i));
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

	/* BOOLEANS */
	public boolean needAttack() {
		RSCharacter opponent = getMyPlayer().getInteracting();
		return opponent == null || opponent.getAnimation() == YAKDEADANIMATIONID;
	}

	public boolean isValidToAttack(RSNPC v) {
		return v.getName().equals(YAKNAME) && v.getInteracting() == null
				&& !v.isInCombat();
	}

	public boolean isSkillBoosted(int skill) {
		return skills.getRealLevel(skill) != skills.getCurrentLevel(skill);
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

	public class skillInfo {

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
			foodID = Integer.parseInt(food.getText());
			try {
				settingsFile.setProperty("foodID", food.getText());
			} catch (NumberFormatException ignored) {
			}
			pouchID = Integer.parseInt(pouch.getText());
			try {
				settingsFile.setProperty("pouchID", pouch.getText());
			} catch (NumberFormatException ignored) {
			}
			log(settingsFile.toString());
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
															GroupLayout.Alignment.TRAILING,
															contentPanelLayout
																	.createSequentialGroup()
																	.addGroup(
																			contentPanelLayout
																					.createParallelGroup(
																							GroupLayout.Alignment.TRAILING)
																					.addGroup(
																							GroupLayout.Alignment.LEADING,
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
																									.addComponent(
																									defence,
																									GroupLayout.PREFERRED_SIZE,
																									GroupLayout.DEFAULT_SIZE,
																									GroupLayout.PREFERRED_SIZE))
																					.addGroup(
																							GroupLayout.Alignment.LEADING,
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
																									GroupLayout.PREFERRED_SIZE,
																									GroupLayout.DEFAULT_SIZE,
																									GroupLayout.PREFERRED_SIZE))
																					.addGroup(
																					GroupLayout.Alignment.LEADING,
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
																							GroupLayout.PREFERRED_SIZE,
																							GroupLayout.DEFAULT_SIZE,
																							GroupLayout.PREFERRED_SIZE)))
																	.addContainerGap(
																	13,
																	Short.MAX_VALUE))
													.addGroup(
															contentPanelLayout
																	.createSequentialGroup()
																	.addGroup(
																			contentPanelLayout
																					.createParallelGroup(
																							GroupLayout.Alignment.TRAILING)
																					.addComponent(
																							label6,
																							GroupLayout.Alignment.LEADING)
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
																									GroupLayout.DEFAULT_SIZE,
																									71,
																									Short.MAX_VALUE))))
																	.addContainerGap())
													.addGroup(
															GroupLayout.Alignment.TRAILING,
															contentPanelLayout
																	.createSequentialGroup()
																	.addComponent(
																			title,
																			GroupLayout.DEFAULT_SIZE,
																			170,
																			Short.MAX_VALUE)
																	.addContainerGap())
													.addGroup(
													contentPanelLayout
															.createSequentialGroup()
															.addComponent(
																	label7,
																	GroupLayout.DEFAULT_SIZE,
																	160,
																	Short.MAX_VALUE)
															.addGap(
															20,
															20,
															20)))));
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
													LayoutStyle.ComponentPlacement.RELATED,
													GroupLayout.DEFAULT_SIZE,
													Short.MAX_VALUE)
											.addComponent(label7)
											.addContainerGap()));
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