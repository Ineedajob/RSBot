import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.EnumSet;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.LinkedList;
import java.util.Properties;

import org.rsbot.event.listeners.PaintListener;
import org.rsbot.script.Script;
import org.rsbot.script.ScriptManifest;
import org.rsbot.script.methods.Skills;
import org.rsbot.script.wrappers.RSArea;
import org.rsbot.script.wrappers.RSComponent;
import org.rsbot.script.wrappers.RSItem;
import org.rsbot.script.wrappers.RSObject;
import org.rsbot.script.wrappers.RSPath;
import org.rsbot.script.wrappers.RSPath.TraversalOption;
import org.rsbot.script.wrappers.RSTile;
import org.rsbot.script.wrappers.RSTilePath;
import org.rsbot.util.GlobalConfiguration;

@ScriptManifest(authors = "Insert Witty Name", name = "Fxjewellry", version = 0.1, description = "None")
public class Fxjewellry extends Script implements PaintListener {
	private int expGained = 0;
	private int expStart = 0;
	private GUI gui = null;
	private UtilThread utilThread = null;
	private int ifaceID = constants.ifaces.bracelets.sapphire;
	private int levelGained = 0;
	private int levelStart = 0;
	private int mouseLo = 2;
	private int mouseHi = 3;
	private int numDone = 0;
	private boolean running = true;
	private int secondaryID = constants.gems.sapphire;
	private Smelter smelter = null;
	private STATES state = STATES.IDLE;
	private final Timekeeper timekeeper = new Timekeeper();

	enum STATES {
		BANK_CLOSE("Closing bank"), BANK_OPEN("Opening bank"), CLICK_CONTINUE(
				"Clicking continue"), CLICK_FURNACE("Clicking furnace"), DEPOSIT(
				"Depositing items"), IDLE("Idling"), IFACE_NAVIGATE(
				"Navigating Iface"), RAGE_QUIT("Rage quitting"), RUN_ENABLE(
				"Turning run on"), WALK_BANK("Walking to bank"), WALK_FURNACE(
				"Walking to furnace"), WITHDRAW("Withdrawing items");
		private final String desc;

		STATES(String desc) {
			this.desc = desc;
		}

		String getDesc() {
			return this.desc;
		}
	}

	public static class constants {
		public static final int[] furnace = new int[] { 11666, 26814 };
		public static final int gold = 2357;

		public static class locations {
			public static final int alkaharid = 0;
			public static final int edgeville = 1;
			public static final int neitiznot = 2;
		}

		public static class ifaces {
			public static final int main = 446;

			public static class amulets {
				public static final int base = 53;
				public static final int gold = base;
				public static final int sapphire = base + 2;
				public static final int emerald = base + 4;
				public static final int ruby = base + 6;
				public static final int diamond = base + 8;
				public static final int dragonstone = base + 10;
				public static final int onyx = base + 12;
				public static final int[] combo = new int[] { gold, sapphire,
						emerald, ruby, diamond, dragonstone, onyx };
			}

			public static class bracelets {
				public static final int base = 32;
				public static final int gold = base;
				public static final int sapphire = base + 2;
				public static final int emerald = base + 4;
				public static final int ruby = base + 6;
				public static final int diamond = base + 8;
				public static final int dragonstone = base + 10;
				public static final int onyx = base + 12;
				public static final int[] combo = new int[] { gold, sapphire,
						emerald, ruby, diamond, dragonstone, onyx };
			}

			public static class necklaces {
				public static final int base = 68;
				public static final int gold = base;
				public static final int sapphire = base + 2;
				public static final int emerald = base + 4;
				public static final int ruby = base + 6;
				public static final int diamond = base + 8;
				public static final int dragonstone = base + 10;
				public static final int onyx = base + 12;
				public static final int[] combo = new int[] { gold, sapphire,
						emerald, ruby, diamond, dragonstone, onyx };
			}

			public static class rings {
				public static final int base = 82;
				public static final int gold = base;
				public static final int sapphire = base + 2;
				public static final int emerald = base + 4;
				public static final int ruby = base + 6;
				public static final int diamond = base + 8;
				public static final int dragonstone = base + 10;
				public static final int onyx = base + 12;
				public static final int[] combo = new int[] { gold, sapphire,
						emerald, ruby, diamond, dragonstone, onyx };
			}
		}

		public static class gems {
			public static final int gold = -1;
			public static final int sapphire = 1607;
			public static final int emerald = 1605;
			public static final int ruby = 1603;
			public static final int diamond = 1601;
			public static final int dragonstone = 1615;
			public static final int onyx = 6573;
			public static final int[] combo = new int[] { gold, sapphire,
					emerald, ruby, diamond, dragonstone, onyx };
		}

		public static class moulds {
			public static final int amulet = 1595;
			public static final int bracelet = 11065;
			public static final int[] moulds = new int[] { amulet, bracelet };
		}
	}

	public STATES getState() {
		if (inventory.contains(constants.gold)) {
			if (!walking.isRunEnabled() && walking.getEnergy() > 50)
				return STATES.RUN_ENABLE;
			if (bank.isOpen())
				return STATES.BANK_CLOSE;
			if (interfaces.canContinue())
				return STATES.CLICK_CONTINUE;
			else if (utilThread.getIdle() > 20) {
				if (ifaceIsUp(interfaces.getComponent(constants.ifaces.main, 0)))
					return STATES.IFACE_NAVIGATE;
				if (objects.getNearest(constants.furnace) != null
						&& objects.getNearest(constants.furnace).isOnScreen()
						&& smelter.getArea().contains(
								players.getMyPlayer().getLocation()))
					return STATES.CLICK_FURNACE;
				if (!smelter.getArea().contains(
						players.getMyPlayer().getLocation()))
					return STATES.WALK_FURNACE;
			}
		} else {
			if (bank.isOpen()) {
				if (inventory.getCount() > 1)
					return STATES.DEPOSIT;
				else if (bank.getCount(constants.gold) <= 0)
					return STATES.RAGE_QUIT;
				else
					return STATES.WITHDRAW;
			} else {
				if (smelter.getBankArea().contains(
						players.getMyPlayer().getLocation()))
					return STATES.BANK_OPEN;
				return STATES.WALK_BANK;
			}
		}
		return STATES.IDLE;
	}

	public int loop() {
		state = getState();
		switch (state) {
		case BANK_CLOSE: {
			bank.close();
			for (int i = 0; i < 20 && bank.isOpen(); i++)
				sleep(random(100, 200));
			sleep(500);
		}
			break;
		case BANK_OPEN: {
			bank.open();
			for (int i = 0; i < 10 && !bank.isOpen(); i++)
				sleep(random(100, 200));
		}
			break;
		case CLICK_CONTINUE: {
			interfaces.clickContinue();
		}
			break;
		case CLICK_FURNACE: {
			final RSObject furnaceObj = objects.getNearest(constants.furnace);
			if (ifaceIsUp(interfaces.getComponent(constants.ifaces.main, 0)))
				break;
			if (!inventory.isItemSelected())
				inventory.getItem(constants.gold).doAction("Use");
			if (ifaceIsUp(interfaces.getComponent(constants.ifaces.main, 0)))
				break;
			camera.turnToObject(furnaceObj);
			while (players.getMyPlayer().isMoving())
				sleep(random(100, 200));
			for (int i = 0; i < 10 && !inventory.isItemSelected(); i++)
				sleep(random(100, 200));
			if (ifaceIsUp(interfaces.getComponent(constants.ifaces.main, 0)))
				break;
			if (furnaceObj != null && inventory.isItemSelected()) {
				furnaceObj.doHover();
				log("Left-clicking furnace");
				mouse.click(false);
				for (int i = 0; i < 10 && !menu.isOpen(); i++)
					sleep(random(100, 200));
				log("Going through the menu options");
				int index = 0;
				for (final String s : menu.getItems()) {
					log(s);
					if (s.contains("Gold bar") && s.contains("Furnace"))
						break;
					index++;
				}
				if (index < menu.getItems().length)
					menu.clickIndex(index);
				else
					mouse.move(random(0, 10), random(0, 10));
			}
			// wait for furnace iface
			for (int i = 0; i < 10
					&& !ifaceIsUp(interfaces.getComponent(
							constants.ifaces.main, 0)); i++)
				sleep(random(100, 200));
		}
			break;
		case DEPOSIT: {
			// only cause of failure here so try->catch time!
			try {
				final int count = inventory.getCount();
				bank.depositAllExcept(constants.moulds.moulds);
				for (int i = 0; i < 10 && inventory.getCount() == count; i++)
					sleep(random(100, 200));
			} catch (final Exception e) {
				e.printStackTrace();
			}
		}
			break;
		case IFACE_NAVIGATE: {
			final RSComponent iface = interfaces.getComponent(
					constants.ifaces.main, ifaceID);
			if (!ifaceIsUp(iface))
				break;
			iface.doHover();
			mouse.click(false);
			for (int i = 0; i < 10 && !menu.isOpen(); i++)
				sleep(random(100, 200));
			if (!menu.isOpen())
				break;
			menu.doAction("Make All");
			utilThread.reset(0);
			for (int i = 0; i < 10
					&& ifaceIsUp(interfaces.getComponent(constants.ifaces.main,
							0)) && players.getMyPlayer().getAnimation() == -1; i++)
				sleep(random(100, 200));
		}
			break;
		case RAGE_QUIT: {
			running = false;
		}
			return -1;
		case RUN_ENABLE: {
			walking.setRun(true);
			for (int i = 0; i < 10 && !walking.isRunEnabled(); i++)
				sleep(random(100, 200));
		}
			break;
		case WALK_BANK: {
			smelter.walkToBank();
		}
			break;
		case WALK_FURNACE: {
			smelter.walkToFurnace();
		}
			break;
		case WITHDRAW: {
			if (secondaryID == -1) {
				withdrawBankItem(constants.gold, 0);
				for (int i = 0; i < 15 && !inventory.contains(constants.gold); i++)
					sleep(random(100, 200));
			} else {
				withdrawBankItem(constants.gold, 13);
				for (int i = 0; i < 15 && !inventory.contains(constants.gold); i++)
					sleep(random(100, 200));
				if (!inventory.contains(secondaryID)) {
					withdrawBankItem(secondaryID, 13);
					for (int i = 0; i < 15 && !inventory.contains(secondaryID); i++)
						sleep(random(100, 200));
				}
			}
		}
			break;
		case IDLE:
			break;
		}
		return 100;
	}

	public void onFinish() {
		log("Final status was " + state.getDesc());
		running = false;
	}

	public void onRepaint(Graphics g1) {
		expGained = skills.getCurrentExp(Skills.CRAFTING) - expStart;
		levelGained = skills.getCurrentLevel(Skills.CRAFTING) - levelStart;
		// START: Code generated using Enfilade's Easel
		final RenderingHints antialiasing = new RenderingHints(
				RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_ON);

		final Color color1 = new Color(0, 0, 255, 108);
		final Color color2 = new Color(0, 0, 0);
		final Color color3 = new Color(255, 255, 255);

		final BasicStroke stroke1 = new BasicStroke(1);

		final Font font1 = new Font("Calibri", 0, 15);
		final Font font2 = new Font("Calibri", 0, 9);
		Graphics2D g = (Graphics2D) g1;
		g.setRenderingHints(antialiasing);

		g.setColor(color1);
		g.fillRect(37, 35, 235, 124);
		g.setColor(color2);
		g.setStroke(stroke1);
		g.drawRect(37, 35, 235, 124);
		g.setFont(font1);
		g.setColor(color3);
		g.drawString("Fxjewellry by NoEffex", 74, 55);
		g.setFont(font2);
		g.drawString("Time Running", 74, 72);
		g.drawString(timekeeper.getRuntimeString(), 200, 72);
		g.drawString("Idle Time", 74, 84);
		g.drawString(Long.toString(utilThread.getIdle()), 200, 84);
		g.drawString("Items Done", 74, 96);
		g.drawString(Integer.toString(numDone), 200, 96);
		g.drawString("Items/Hr", 74, 108);
		g.drawString(Long.toString(timekeeper.calcPerHour(numDone)), 200, 108);
		g.drawString("Experience Gained", 74, 120);
		g.drawString(Integer.toString(expGained), 200, 120);
		g.drawString("Experience/Hr", 74, 132);
		g.drawString(Long.toString(timekeeper.calcPerHour(expGained)), 200, 132);
		g.drawString("Levels Gained", 74, 144);
		g.drawString(Integer.toString(levelGained), 200, 144);
		// END: Code generated using Enfilade's Easel
	}

	public boolean onStart() {
		SettingsManager sm = new SettingsManager(
				GlobalConfiguration.Paths.getSettingsDirectory()
						+ File.separator + "Fxjewellry.properties");
		gui = new GUI(sm);
		gui.setVisible(true);
		while (gui.isVisible()) {
			sleep(100);
		}
		sm.save();
		if (gui.isCancelled() == true)
			return false;
		// get ze type
		final int location = gui.getGoldLocation();
		switch (location) {
		case constants.locations.alkaharid:
			smelter = new AlKaharid();
			break;
		case constants.locations.edgeville:
			smelter = new Edgeville();
			break;
		default:
			return false;
		}
		final int jewel = gui.getJewel();
		final String type = gui.getGoldType();
		secondaryID = constants.gems.combo[jewel];
		if (type.equals("Bracelet"))
			ifaceID = constants.ifaces.bracelets.combo[jewel];
		else if (type.equals("Amulet"))
			ifaceID = constants.ifaces.amulets.combo[jewel];
		else if (type.equals("Necklace"))
			ifaceID = constants.ifaces.necklaces.combo[jewel];
		else if (type.equals("Ring"))
			ifaceID = constants.ifaces.rings.combo[jewel];
		mouseHi = gui.getMouseHi();
		mouseLo = gui.getMouseLo();
		utilThread = new UtilThread();
		new AntiBan();
		mouse.setSpeed(2);
		expStart = skills.getCurrentExp(Skills.CRAFTING);
		levelStart = skills.getCurrentLevel(Skills.CRAFTING);
		return true;
	}

	private boolean ifaceIsUp(RSComponent r) {
		if (r != null && r.isValid() && r.getLocation().x > 0
				&& r.getLocation().y > 0)
			return true;
		return false;
	}

	public boolean withdrawBankItem(final int itemID, final int num) {
		if (!bank.isOpen())
			return false;
		// bank.
		RSItem child = bank.getItem(itemID);
		if (child != null && child.getID() != itemID)
			return false;
		for (int i = 0; i < 40 && child.getID() != itemID; i++) {
			child = bank.getItem(itemID);
			sleep(random(100, 200));
		}
		if (child == null || !child.getComponent().isValid()
				|| child.getID() != itemID)
			return false;

		child.getComponent().doHover();
		mouse.click(false);
		sleep(random(200, 300));
		if (num == 0 || num >= (28 - inventory.getCount()))
			menu.doAction("Withdraw-All");
		else if (num == -1)
			menu.doAction("Withdraw-All but one");
		else {
			for (int i = 0; i < 40 && !menu.isOpen(); i++)
				sleep(random(100, 200));
			if (!menu.contains("Withdraw-" + Integer.toString(num))) {
				menu.doAction("Withdraw-X");
				for (int i = 0; i < 40
						&& !interfaces.getComponent(752, 4).isValid(); i++)
					sleep(random(100, 200));

				sleep(random(1000, 1500));
				if (interfaces.getComponent(752, 4).getText()
						.contains("amount")
						|| !interfaces.getComponent(752, 4).getText()
								.contains("purchase")) {
					keyboard.sendText(Integer.toString(num), true);
					sleep(2000);
				}
			} else {
				for (int i = 0; i < 40 && !menu.isOpen(); i++)
					sleep(random(100, 200));
				menu.doAction("Withdraw-" + Integer.toString(num));
			}
		}
		return true;
	}

	public abstract class Smelter {
		public abstract RSArea getArea();

		public abstract RSArea getBankArea();

		public abstract void walkToBank();

		public abstract void walkToFurnace();
	}

	public class AlKaharid extends Smelter {
		private final RSArea a = new RSArea(new RSTile(3271, 3183), new RSTile(
				3280, 3189));
		private final RSArea b = new RSArea(new RSTile(3264, 3160), new RSTile(
				3273, 3175));
		private final RSTile bankTile = new RSTile(3270, 3167);
		private final RSTile furnaceTile = new RSTile(3275, 3186);
		private RSPath bankToFurnace = null;
		private RSPath furnaceToBank = null;

		public RSArea getArea() {
			return a;
		}

		public RSArea getBankArea() {
			return b;
		}

		public void walkToBank() {
			if (furnaceToBank == null) {
				bankToFurnace = walking.getPath(bankTile);
			}
			furnaceToBank.traverse(EnumSet.of(TraversalOption.HANDLE_RUN, TraversalOption.SPACE_ACTIONS));
		}

		public void walkToFurnace() {
			if (bankToFurnace == null) {
				bankToFurnace = walking.getPath(furnaceTile);
			}
			bankToFurnace.traverse(EnumSet.of(TraversalOption.HANDLE_RUN, TraversalOption.SPACE_ACTIONS));
		}
	}

	public class Edgeville extends Smelter {
		private final RSArea a = new RSArea(new RSTile(3105, 3497), new RSTile(
				3111, 3503));
		private final RSArea b = new RSArea(new RSTile(3090, 3487), new RSTile(
				3099, 3500));
		private final RSTile[] bankToFurnacePath = new RSTile[] {
				new RSTile(3097, 3497), new RSTile(3103, 3499),
				new RSTile(3108, 3500) };
		private final RSTilePath bankToFurnace = walking
				.newTilePath(bankToFurnacePath);
		private boolean reversed = false;

		public RSArea getArea() {
			return a;
		}

		public RSArea getBankArea() {
			return b;
		}

		public void walkToBank() {
			if (!reversed) {
				bankToFurnace.reverse();
				reversed = true;
			}
			bankToFurnace.traverse(EnumSet.of(TraversalOption.HANDLE_RUN));
		}

		public void walkToFurnace() {
			if (reversed) {
				bankToFurnace.reverse();
				reversed = false;
			}
			bankToFurnace.traverse(EnumSet.of(TraversalOption.HANDLE_RUN));
		}

	}

	private class AntiBan implements Runnable {
		private void sleep(int t) {
			try {
				Thread.sleep(t);
			} catch (final Exception e) {
				e.printStackTrace();
			}
		}

		public AntiBan() {
			new Thread(this).start();
		}

		public void run() {
			while (running) {
				final int rand = random(0, 100);
				switch (rand) {
				case 0:
					mouse.setSpeed(random(mouseLo, mouseHi));
					break;
				}
				sleep(1000);
			}
		}
	}

	private class UtilThread implements Runnable {
		private long idleTime = 0;

		private void sleep(int t) {
			try {
				Thread.sleep(t);
			} catch (final Exception e) {
				e.printStackTrace();
			}
		}

		public long getIdle() {
			return idleTime;
		}

		public void reset(long t) {
			idleTime = t;
		}

		public UtilThread() {
			new Thread(this).start();
		}

		public void run() {
			int lastCount = 0;
			while (running) {
				sleep(100);
				if (isPaused()) {
					continue;
				}
				if (lastCount > inventory.getCount(constants.gold) && lastCount != 0) {
					numDone++;
				}
				lastCount = inventory.getCount(constants.gold);
				if (players.getMyPlayer().getAnimation() == -1) {
					idleTime++;
				} else {
					idleTime = 0;
				}
			}
		}
	}

	private class GUI extends JFrame {
		private static final long serialVersionUID = 570716943631400833L;

		private final String[] jewels = new String[] { "None (Gold)",
				"Sapphire", "Emerald", "Ruby", "Diamond", "Dragonstone", "Onyx" };
		private final String[] locations = new String[] { "Al Kaharid",
				"Edgeville", "Neitiznot (Not Implemented)" };
		private final String[] mouseString = new String[] { "0", "1", "2", "3",
				"4", "5", "6", "7", "8", "9" };
		private final String[] types = new String[] { "Ring", "Necklace",
				"Amulet", "Bracelet" };

		private boolean cancel = false;

		private JButton cancelButton = new JButton("Cancel");
		private JComboBox goldLocations = new JComboBox(locations);
		private JComboBox goldTypes = new JComboBox(types);
		private JComboBox jewellryTypes = new JComboBox(jewels);
		private JComboBox mouseHi = new JComboBox(mouseString);
		private JComboBox mouseLo = new JComboBox(mouseString);
		private JButton startButton = new JButton("Start");
		private JLabel title = new JLabel(
				"<html><body><b><font size=\"5\">Fxjewellry</font></b></body></html>");

		public boolean isCancelled() {
			return cancel;
		}

		public String getGoldType() {
			return (String) goldTypes.getSelectedItem();
		}

		public int getJewel() {
			return jewellryTypes.getSelectedIndex();
		}

		public int getGoldLocation() {
			return goldLocations.getSelectedIndex();
		}

		public int getMouseHi() {
			return mouseHi.getSelectedIndex();
		}

		public int getMouseLo() {
			return mouseLo.getSelectedIndex();
		}

		public GUI(SettingsManager sm) {
			sm.add("Gold Type", goldTypes);
			sm.add("Jewel Type", jewellryTypes);
			sm.add("Location", goldLocations);
			sm.add("Mouse Hi", mouseHi);
			sm.add("Mouse Lo", mouseLo);
			sm.load();
			setLayout(new BorderLayout());
			add(new NorthPanel(), BorderLayout.NORTH);
			add(new CenterPanel(), BorderLayout.CENTER);
			add(new SouthPanel(), BorderLayout.SOUTH);
			pack();
		}

		class GUIActionListener implements ActionListener {
			public void actionPerformed(ActionEvent event) {
				if (event.getActionCommand().equals("Cancel")) {
					cancel = true;
					setVisible(false);
				} else if (event.getActionCommand().equals("Start")) {
					setVisible(false);
				}
			}
		}

		class NorthPanel extends JPanel {
			private static final long serialVersionUID = -532956719518981384L;

			public NorthPanel() {
				setLayout(new GridLayout(1, 1));
				add(title);
			}
		}

		class CenterPanel extends JPanel {
			private static final long serialVersionUID = 7828700674189907012L;

			public CenterPanel() {
				mouseLo.setSelectedIndex(3);
				mouseHi.setSelectedIndex(6);
				setLayout(new GridLayout(0, 2));
				add(new JLabel("Type:"));
				add(goldTypes);
				add(new JLabel("Jewel:"));
				add(jewellryTypes);
				add(new JLabel("Location:"));
				add(goldLocations);
				add(new JLabel("Fastest Mouse Speed:"));
				add(mouseLo);
				add(new JLabel("Slowest Mouse Speed:"));
				add(mouseHi);
			}
		}

		class SouthPanel extends JPanel {
			private static final long serialVersionUID = -7538261008658025548L;

			public SouthPanel() {
				cancelButton.setActionCommand("Cancel");
				cancelButton.addActionListener(new GUIActionListener());
				startButton.setActionCommand("Start");
				startButton.addActionListener(new GUIActionListener());
				setLayout(new GridLayout(1, 2));
				add(cancelButton);
				add(startButton);
			}
		}
	}

	public class SettingsManager {
		private String name;
		private LinkedList<Pair> pairs = new LinkedList<Pair>();

		public SettingsManager(String name) {
			this.name = name;
		}

		public void add(String key, JComponent component) {
			pairs.add(new Pair(key, component));
		}

		public void load() {
			try {
				File file = new File(name);
				if (!file.exists()) {
					return;
				}
				FileReader rd = new FileReader(file);
				Properties prop = new Properties();
				prop.load(rd);
				for (Pair pair : pairs) {
					String value = prop.getProperty(pair.key);
					if (value == null) {
						continue;
					}
					if (pair.component instanceof JComboBox) {
						((JComboBox) pair.component)
								.setSelectedItem((Object) value);
					} else if (pair.component instanceof JCheckBox) {
						((JCheckBox) pair.component).setSelected(Boolean
								.parseBoolean(value));
					} else if (pair.component instanceof JTextField) {
						((JTextField) pair.component).setText(value);
					} else if (pair.component instanceof JTextArea) {
						((JTextArea) pair.component).setText(value);
					}
				}
				rd.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		public void save() {
			try {
				File file = new File(name);
				FileWriter wr = new FileWriter(file);
				Properties prop = new Properties();
				for (Pair pair : pairs) {
					String value = "null";
					if (pair.component instanceof JComboBox) {
						value = (String) ((JComboBox) pair.component)
								.getSelectedItem();
					} else if (pair.component instanceof JCheckBox) {
						value = Boolean.toString(((JCheckBox) pair.component)
								.isSelected());
					} else if (pair.component instanceof JTextField) {
						value = ((JTextField) pair.component).getText();
					} else if (pair.component instanceof JTextArea) {
						value = ((JTextArea) pair.component).getText();
					}
					prop.setProperty(pair.key, value);
				}
				prop.store(wr, "SettingsManager by NoEffex");
				wr.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		class Pair {
			String key;
			JComponent component;

			public Pair(String key, JComponent component) {
				this.key = key;
				this.component = component;
			}
		}
	}

	class Timekeeper {
		long startTime = 0;
		long pausedTime = 0;
		long pausedTemp = 0;
		long state = 0;

		public Timekeeper() {
			startTime = System.currentTimeMillis();
		}

		public long getMillis() {
			return System.currentTimeMillis() - startTime - pausedTime;
		}

		public long getSeconds() {
			return this.getMillis() / 1000;
		}

		public long getMinutes() {
			return this.getSeconds() / 60;
		}

		public long getHours() {
			return this.getMinutes() / 60;
		}

		public String getRuntimeString() {
			final long HoursRan = this.getHours();
			long MinutesRan = this.getMinutes();
			long SecondsRan = this.getSeconds();
			MinutesRan = MinutesRan % 60;
			SecondsRan = SecondsRan % 60;
			return HoursRan + ":" + MinutesRan + ":" + SecondsRan;
		}

		public void setPaused() {
			state = 1;
			pausedTemp = System.currentTimeMillis();
		}

		public long setResumed() {
			state = 0;
			return (pausedTime += (System.currentTimeMillis() - pausedTemp));
		}

		public long calcPerHour(final long i) {
			return calcPerHour((double) i);
		}

		public long calcPerHour(final double i) {
			final double elapsed_millis = this.getMillis();
			return (long) ((i / elapsed_millis) * 3600000);
		}

		public double calcPerSecond(final long i) {
			final double expToDouble = i;
			final double elapsed_millis = this.getMillis();
			return (expToDouble / elapsed_millis) * 1000;
		}
	}
}
