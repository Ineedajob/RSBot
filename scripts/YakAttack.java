import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.geom.Rectangle2D;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Random;

import javax.swing.AbstractListModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JSlider;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;

import org.rsbot.event.events.MessageEvent;
import org.rsbot.event.listeners.MessageListener;
import org.rsbot.event.listeners.PaintListener;
import org.rsbot.script.Script;
import org.rsbot.script.ScriptManifest;
import org.rsbot.script.methods.Game;
import org.rsbot.script.methods.Skills;
import org.rsbot.script.util.Filter;
import org.rsbot.script.util.Timer;
import org.rsbot.script.wrappers.RSGroundItem;
import org.rsbot.script.wrappers.RSItem;
import org.rsbot.script.wrappers.RSModel;
import org.rsbot.script.wrappers.RSNPC;
import org.rsbot.script.wrappers.RSTile;

@ScriptManifest(authors = { "Pervy Shuya" }, keywords = "Combat", name = "YakAttack PRo", version = 1.4, description = "Settings in GUI")
public class YakAttack extends Script implements PaintListener, MessageListener {
	private final int KILLYAKS = 0, KILLSCRIPT = 1, SPECIAL = 2;

	final ScriptManifest properties = super.getClass().getAnnotation(
			ScriptManifest.class);
	private final int yakID = 5529;

	private final int[] Junk = { 10814, 10816, 10818, 526 };
	private final int[] superStrength = { 2440, 157, 159, 161 };
	private final int[] superDefense = { 2442, 163, 165, 167 };
	private final int[] superAttack = { 2436, 145, 147, 149 };
	private final int[] normalStrength = { 113, 115, 117, 119 };
	private final int[] normalDefense = { 2432, 133, 135, 137 };
	private final int[] normalAttack = { 2428, 121, 123, 125 };
	private final int[] normalRange = { 2444, 169, 171, 173 };
	private final int[] combatPots = { 9739, 9741, 9743, 9745 };
	private final int[] potionSet = { 2440, 157, 159, 161, 2442, 163, 165, 167,
			2436, 145, 147, 149, 113, 115, 117, 119, 2432, 133, 135, 137, 2428,
			121, 123, 125, 2444, 169, 171, 173, 9739, 9741, 9743, 9745 };

	private final String[] potionsToDrink = { "Super Attack", "Super Strength",
			"Super Defense", "Combat potion", "Normal Range", "Normal Attack",
			"Normal Strength", "Normal Defense" };
	private int speed, hp2EatAt;

	YakAttackProGUI gui;
	Thread t = new Thread(new YakAttackAntiBan());

	private int CURR_WEP_ID, SPEC_WEP_ID, yaksKilled, yaksPerHour,
			arrowID = -1;

	RSItem foodInBag;

	private final int bronzeArrow = 882, ironArrow = 884, steelArrow = 886,
			mithrilArrow = 888, adamantArrow = 890, runeArrow = 892,
			bronzeBolt = 877, boneBolt = 8882, blueriteBolt = 9139,
			ironBolt = 9140, steelBolt = 9141, blackBolt = 13083,
			mithrilBolt = 9142, adamantBolt = 9143, runeBolt = 9144,
			broadBolt = 13280, bronzeKnife = 864, ironKnife = 863,
			steelKnife = 865, blackKnife = 869, mithrilKnife = 866,
			adamantKnife = 867, runeKnife = 868;

	private int specialCost = 0, Food;

	private final int bronzeDart = 806, ironDart = 807, steelDart = 808,
			blackDart = 3093, mithrilDart = 809, adamantDart = 810,
			runeDart = 811;
	private int rndSpec = 1000, rndSpecCtr;

	private final Timer timeRan = new Timer(0L);

	private String Status = "Starting", arrowName;
	private final RSTile yakTile = new RSTile(2324, 3792);
	private boolean wants2Eat, doSpec, guiWait = true, guiExit, Wait;
	private final int[] foodID = { 1895, 1893, 1891, 4293, 2142, 291, 2140,
			3228, 9980, 7223, 6297, 6293, 6295, 6299, 7521, 9988, 7228, 2878,
			7568, 2343, 1861, 13433, 315, 325, 319, 3144, 347, 355, 333, 339,
			351, 329, 3381, 361, 10136, 5003, 379, 365, 373, 7946, 385, 397,
			391, 3369, 3371, 3373, 2309, 2325, 2333, 2327, 2331, 2323, 2335,
			7178, 7180, 7188, 7190, 7198, 7200, 7208, 7210, 7218, 7220, 2003,
			2011, 2289, 2291, 2293, 2295, 2297, 2299, 2301, 2303, 1891, 1893,
			1895, 1897, 1899, 1901, 7072, 7062, 7078, 7064, 7084, 7082, 7066,
			7068, 1942, 6701, 6703, 7054, 6705, 7056, 7060, 2130, 1985, 1993,
			1989, 1978, 5763, 5765, 1913, 5747, 1905, 5739, 1909, 5743, 1907,
			1911, 5745, 2955, 5749, 5751, 5753, 5755, 5757, 5759, 5761, 2084,
			2034, 2048, 2036, 2217, 2213, 2205, 2209, 2054, 2040, 2080, 2277,
			2225, 2255, 2221, 2253, 2219, 2281, 2227, 2223, 2191, 2233, 2092,
			2032, 2074, 2030, 2281, 2235, 2064, 2028, 2187, 2185, 2229, 6883,
			1971, 4608, 1883, 1885 }, arrowPack = { 882, 884, 886, 888, 890,
			892, 877, 8882, 9139, 9140, 9141, 13083, 9142, 9143, 9144, 13280,
			864, 863, 865, 869, 866, 867, 868, 806, 807, 808, 3093, 809, 810,
			811 };

	private final int[] exptolevel = { 0, 83, 174, 276, 388, 512, 650, 801,
			969, 1154, 1358, 1584, 1833, 2107, 2411, 2746, 3115, 3523, 3973,
			4470, 5018, 5624, 6291, 7028, 7842, 8740, 9730, 10824, 12031,
			13363, 14833, 16456, 18247, 20224, 22406, 24815, 27473, 30408,
			33648, 37224, 41171, 45529, 50339, 55649, 61512, 67983, 75127,
			83014, 91721, 101333, 111945, 123660, 136594, 150872, 166636,
			184040, 203254, 224466, 247886, 273742, 302288, 333804, 368599,
			407015, 449428, 496254, 547953, 605032, 668051, 737627, 814445,
			899257, 992895, 1096278, 1210421, 1336443, 1475581, 1629200,
			1798808, 1986068, 2192818, 2421087, 2673114, 2951373, 3258594,
			3597792, 3972294, 4385776, 4842295, 5346332, 5902831, 6517253,
			7195629, 7944614, 8771558, 9684577, 10692629, 11805606, 13034431,
			200000001 };
	private final int[] startinglvl = new int[8];
	private final int[] currentlvl = new int[8];
	private final int[] startingexp = new int[8];
	private final int[] currentexp = new int[8];
	private final int[] diffexp = new int[8];
	private final String[] statnames = { "MAGIC", "ATTACK", "DEFENSE",
			"CONSTITUTION", "PRAYER", "RANGE", "SLAYER", "STRENGTH" };

	private boolean pickupArrows(int[] id, String itemName) {
		boolean back = false;
		try {
			RSGroundItem loots = groundItems.getNearest(id);
			Point toscreen = calc.tileToScreen(loots.getLocation());
			if (loots != null && !getMyPlayer().isMoving()) {
				back = true;
				if (calc.pointOnScreen(toscreen)) {
					mouse.move(toscreen, toscreen.x, toscreen.y);
					sleep(random(100, 200));
					if (menu.getItems().length > 1) {
						if (listContainsString(menu.getItems(), itemName)) {
							if (menu.getItems()[0].contains(itemName)) {
								mouse.click(true);
								sleep(random(750, 1000));
							} else {
								mouse.click(false);
								sleep(random(500, 750));
								menu.doAction(itemName);
								sleep(random(750, 1000));
							}
						}
					}
				}
			}
		} catch (Exception ignored) {
		}
		return back;
	}

	private boolean listContainsString(String[] list, String string) {
		try {
			int a;
			for (a = list.length - 1; a-- >= 0;) {
				if (list[a].contains(string))
					return true;
			}
		} catch (Exception ignored) {
		}
		return false;
	}

	private boolean handleArrows() {
		RSGroundItem loots = groundItems.getNearest(arrowPack);
		if (arrowID != -1 && getMyPlayer().getInteracting() == null) {
			Status = "Picking up Arrows";
			if (tiles.doAction(loots.getLocation(), 0.5, 0.5, 0, arrowName)) {
				return true;
			} else if (!tiles.doAction(loots.getLocation(), 0.5, 0.5, 0,
					arrowName)) {
				pickupArrows(arrowPack, "Take " + arrowName);
				sleep(random(400, 600));
				return true;
			}
		}
		return false;
	}

	private void drinkPot(String type) {
		if (type.equals("Super Attack")) {
			for (final int id : superAttack) {
				if (skills.getCurrentLevel(Skills.ATTACK) <= skills
						.getRealLevel(Skills.ATTACK) + random(2, 4)) {
					if (inventory.getCount(id) > 0) {
						inventory.getItem(superAttack).doAction("Drink");
						while (getMyPlayer().getAnimation() != -1) {
							sleep(random(300, 600));
						}
					}
				}
			}
		}
		if (type.equals("Super Strength")) {
			for (final int id : superStrength) {
				if (skills.getCurrentLevel(Skills.STRENGTH) <= skills
						.getRealLevel(Skills.STRENGTH) + random(2, 4)) {
					if (inventory.getCount(id) > 0) {
						inventory.getItem(superStrength).doAction("Drink");
						while (getMyPlayer().getAnimation() != -1) {
							sleep(random(300, 600));
						}
					}
				}
			}
		}
		if (type.equals("Super Defense")) {
			for (final int id : superDefense) {
				if (skills.getCurrentLevel(Skills.DEFENSE) <= skills
						.getRealLevel(Skills.DEFENSE) + random(2, 4)) {
					if (inventory.getCount(id) > 0) {
						inventory.getItem(superDefense).doAction("Drink");
						while (getMyPlayer().getAnimation() != -1) {
							sleep(random(300, 600));
						}
					}
				}
			}
		}
		if (type.equals("Normal Range")) {
			for (final int id : normalRange) {
				if (skills.getCurrentLevel(Skills.RANGE) <= skills
						.getRealLevel(Skills.RANGE) + random(2, 4)) {
					if (inventory.getCount(id) > 0) {
						inventory.getItem(normalRange).doAction("Drink");
						while (getMyPlayer().getAnimation() != -1) {
							sleep(random(300, 600));
						}
					}
				}
			}
		}
		if (type.equals("Normal Attack")) {
			for (final int id : normalAttack) {
				if (skills.getCurrentLevel(Skills.ATTACK) <= skills
						.getRealLevel(Skills.ATTACK) + random(2, 4)) {
					if (inventory.getCount(id) > 0) {
						inventory.getItem(normalAttack).doAction("Drink");
						while (getMyPlayer().getAnimation() != -1) {
							sleep(random(300, 600));
						}
					}
				}
			}
		}
		if (type.equals("Normal Strength")) {
			for (final int id : normalStrength) {
				if (skills.getCurrentLevel(Skills.STRENGTH) <= skills
						.getRealLevel(Skills.STRENGTH) + random(2, 4)) {
					if (inventory.getCount(id) > 0) {
						inventory.getItem(normalStrength).doAction("Drink");
						while (getMyPlayer().getAnimation() != -1) {
							sleep(random(300, 600));
						}
					}
				}
			}
		}
		if (type.equals("Normal Defense")) {
			for (final int id : normalDefense) {
				if (skills.getCurrentLevel(Skills.DEFENSE) <= skills
						.getRealLevel(Skills.DEFENSE) + random(2, 4)) {
					if (inventory.getCount(id) > 0) {
						inventory.getItem(normalDefense).doAction("Drink");
						while (getMyPlayer().getAnimation() != -1) {
							sleep(random(300, 600));
						}
					}
				}
			}
		}
		if (type.equals("Combat potion")) {
			for (final int id : combatPots) {
				if (skills.getCurrentLevel(Skills.STRENGTH) <= skills
						.getRealLevel(Skills.STRENGTH) + random(2, 4)
						|| skills.getCurrentLevel(Skills.ATTACK) <= skills
								.getRealLevel(Skills.ATTACK) + random(2, 4)) {
					if (inventory.getCount(id) > 0) {
						inventory.getItem(combatPots).doAction("Drink");
						while (getMyPlayer().getAnimation() != -1) {
							sleep(random(300, 600));
						}
					}
				}
			}
		}
	}

	private String buildString(int paramInt) {
		if (paramInt >= 100000)
			return new StringBuilder().append(paramInt / 1000).append("K")
					.toString();
		double d;
		if (paramInt >= 10000) {
			d = round(paramInt / 1000.0D, 1);
			return new StringBuilder().append(d).append("K").toString();
		}
		if (paramInt >= 1000) {
			d = round(paramInt / 1000.0D, 2);
			return new StringBuilder().append(d).append("K").toString();
		}
		return new StringBuilder().append(paramInt).append("").toString();
	}

	private static double round(double paramDouble, int paramInt) {
		BigDecimal localBigDecimal = new BigDecimal(
				Double.toString(paramDouble));
		localBigDecimal = localBigDecimal.setScale(paramInt, 4);
		return localBigDecimal.doubleValue();
	}

	private boolean shouldDrinkPot() {
		return skills.getCurrentLevel(Skills.STRENGTH) <= skills
				.getRealLevel(Skills.STRENGTH) + random(3, 5)
				|| skills.getCurrentLevel(Skills.ATTACK) <= skills
						.getRealLevel(Skills.ATTACK) + random(3, 5)
				|| skills.getCurrentLevel(Skills.DEFENSE) <= skills
						.getRealLevel(Skills.DEFENSE) + random(3, 5)
				|| skills.getCurrentLevel(Skills.RANGE) <= skills
						.getRealLevel(Skills.RANGE) + random(3, 5);
	}

	/**
	 * Returns the RSNPC that is nearest, out of all of the RSPNCs with the
	 * provided name(s), that is not currently in combat. Can return null.
	 * 
	 * @param names
	 *            The name(s) of the NPCs that you are searching.
	 * @return An RSNPC object representing the nearest RSNPC with one of the
	 *         provided names that is not in combat; or null if there are no
	 *         mathching NPCs in the current region.
	 * @see #getNearest(int...)
	 * @see #getNearestFree(int...)
	 * @see #getNearestToAttack(int...)
	 * @see #getNearestFreeToAttack(int...)
	 */
	@SuppressWarnings("unused")
	private RSNPC getNearestFree(int... ids) {
		int Dist = 20;
		RSNPC closest = null;
		RSNPC[] validNPCs = npcs.getAll();

		RSNPC Monster = validNPCs[random(0, validNPCs.length)];
		try {
			for (int id : ids) {
				if (id != Monster.getID() || Monster.isInCombat()) {
					continue;
				}
				int distance = calc.distanceTo(Monster);
				if (distance < Dist) {
					Dist = distance;
					closest = Monster;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return closest;
	}

	private void getRndSpec() {
		rndSpec = random(specialCost, 1000);
		log("Will next Spec at " + rndSpec / 10 + "%");
	}

	private void drawRect(Graphics g, Rectangle rect, Color col) {
		g.setColor(col);
		g.drawRect(rect.x, rect.y, rect.width, rect.height);
	}

	private void drawNiceBox(Graphics g, String text, int x, int y, Color col1,
			Color col2, Color col3, Boolean important) {
		int widthInPixels = 0;
		int height = 0;
		Rectangle2D bounds;
		if (important) {
			Font font = new Font("Arial", Font.BOLD, 16);
			FontMetrics metrics = new FontMetrics(font) {

				/**
				 * 
				 */
				private static final long serialVersionUID = 1L;
			};
			bounds = metrics.getStringBounds(text, null);
			widthInPixels = (int) bounds.getWidth();
			g.setFont(font);
		} else {
			Font font = new Font("Arial", Font.PLAIN, 12);
			FontMetrics metrics = new FontMetrics(font) {

				/**
				 * 
				 */
				private static final long serialVersionUID = 1L;
			};
			bounds = metrics.getStringBounds(text, null);
			widthInPixels = (int) bounds.getWidth();
			g.setFont(font);
		}
		height = (int) bounds.getHeight() + 6;
		g.setColor(col1);
		g.fill3DRect(x, y, widthInPixels + 10, height, true);
		g.setColor(col2);
		g.drawRect(x - 1, y - 1, widthInPixels + 11, height + 1);
		g.setColor(new Color(col3.getRed(), col3.getGreen(), col3.getBlue(), 30));
		g.drawString(text, x + 4, y + height - 5);
		g.setColor(col3);
		g.drawString(text, x + 5, y + height - 6);
	}

	private void drawItem(Graphics g, RSItem item, Boolean food)
			throws InterruptedException {
		if (food) {
			g.setColor(Color.WHITE);
			drawRect(g, item.getComponent().getArea(), Color.WHITE);
			drawNiceBox(
					g,
					"Eating: "
							+ item.getName()
									.substring(
											item.getName().indexOf(
													"<col=ff9040>") + 12), item
							.getComponent().getArea().x - 80, item
							.getComponent().getArea().y
							+ item.getComponent().getArea().height, new Color(
							255, 255, 255, 100), new Color(0, 0, 0, 150),
					Color.BLACK, false);
		} else {
			g.setColor(Color.GREEN);
			drawRect(g, item.getComponent().getArea(), Color.GREEN);
			drawNiceBox(g, "Using: " + item.getName(), item.getComponent()
					.getArea().x - 80, item.getComponent().getArea().y
					+ item.getComponent().getArea().height, new Color(0, 255,
					0, 100), new Color(0, 0, 0, 150), Color.BLACK, false);
		}
	}

	private int statInfo(int i) {
		switch (i) {
		case 0:
			return Skills.MAGIC;
		case 1:
			return Skills.ATTACK;
		case 2:
			return Skills.DEFENSE;
		case 3:
			return Skills.CONSTITUTION;
		case 4:
			return Skills.PRAYER;
		case 5:
			return Skills.RANGE;
		case 6:
			return Skills.SLAYER;
		case 7:
			return Skills.STRENGTH;
		}
		return i;
	}

	@SuppressWarnings("unused")
	private int getAngleToCoord(RSTile loc) {
		int x1 = getMyPlayer().getLocation().getX();
		int y1 = getMyPlayer().getLocation().getY();
		int x = x1 - loc.getX();
		int y = y1 - loc.getY();
		double angle = Math.toDegrees(Math.atan2(y, x));
		return (int) angle;
	}

	private Point centerPoint(RSModel o, int level) {
		if (o == null)
			return new Point(-1, -1);
		int xTotal = 0;
		int yTotal = 0;
		Point[] points = new Point[level];
		for (int i = 0; i < level; i++) {
			points[i] = o.getPoint();
			xTotal += points[i].getX();
			yTotal += points[i].getY();
		}
		return new Point(xTotal / level, yTotal / level);
	}

	private boolean clickNPC(final RSNPC npc, final String action) {
		for (int i = 0; i < 10; i++) {
			if (isPartiallyOnScreen(npc.getModel())) {
				Point p;
				if (npc.isOnScreen()) {
					p = npc.getModel().getCentralPoint();
				} else {
					p = getPointOnScreen(npc.getModel(), false);
				}
				if (p == null || !calc.pointOnScreen(p)) {
					continue;
				}
				mouse.move(p, 3, 3);
				String[] items = menu.getItems();
				if (items.length > 0 && items[0].contains(action)) {
					mouse.click(true);
					return true;
				} else if (menu.contains(action)) {
					mouse.click(false);
					sleep(random(100, 200));
					for (int x = 0; x < 4; x++) {
						if (!menu.contains(action)) {
							break;
						}
						if (menu.doAction(action)) {
							return true;
						}
					}
				}
			} else {
				if (npc != null && !npc.isOnScreen()) {
					Status = "Setting view to yaks";
					int angle = camera.getCharacterAngle(npc);
					if (calc.distanceTo(npc) < 10
							&& Math.abs(angle - camera.getAngle()) > 20) {
						camera.turnToCharacter(npc);
					}
				} else {
					walking.walkTileMM(closerTile(npc.getLocation(), 1), 2, 2);
					return false;
				}
			}
		}
		return false;
	}

	private boolean isPartiallyOnScreen(RSModel m) {
		return getPointOnScreen(m, true) != null;
	}

	private Point getPointOnScreen(RSModel m, boolean first) {
		if (m == null)
			return null;
		ArrayList<Point> list = new ArrayList<Point>();
		try {
			Polygon[] tris = m.getTriangles();
			for (int i = 0; i < tris.length; i++) {
				Polygon p = tris[i];
				for (int j = 0; j < p.xpoints.length; j++) {
					Point pt = new Point(p.xpoints[j], p.ypoints[j]);
					if (calc.pointOnScreen(pt)) {
						if (first)
							return pt;
						list.add(pt);
					}
				}
			}
		} catch (Exception ignored) {
		}
		return list.size() > 0 ? list.get(random(0, list.size())) : null;
	}

	private RSTile closerTile(RSTile t, int dist) {
		RSTile loc = getMyPlayer().getLocation();
		int newX = t.getX(), newY = t.getY();
		for (int i = 1; i < dist; i++) {
			newX = t.getX() != loc.getX() ? (t.getX() < loc.getX() ? newX--
					: newX++) : newX;
			newY = t.getY() != loc.getY() ? (t.getY() < loc.getY() ? newY--
					: newY++) : newY;
		}
		return new RSTile(newX, newY);
	}

	private int getAction() {
		if (game.isLoggedIn()) {
			if (getMyPlayer().getInteracting() != null
					|| getMyPlayer().isInCombat()) {
				if (doSpec && settings.getSetting(300) >= rndSpec) {
					rndSpecCtr = 0;
					return SPECIAL;
				}
			}
			if (calc.distanceTo(yakTile) < 50)
				return KILLYAKS;
			else
				return KILLSCRIPT;
		}
		return random(100, 300);
	}

	@Override
	public int loop() {
		foodInBag = inventory.getItem(foodID);
		RSNPC yak = npcs.getNearest(new Filter<RSNPC>() {
			@Override
			public boolean accept(RSNPC npc) {
				if (npc.getID() == yakID) {
					if (npc.getInteracting() != null || npc.isInCombat())
						return false;

					return true;
				}

				return false;
			}
		});

		if (wants2Eat) {
			if (combat.getLifePoints() <= random(hp2EatAt - 30, hp2EatAt + 30)) {
				Status = "Eating Food";
				if (inventory.contains(Food)) {
					inventory.getItem(Food).doAction("Eat");
					return random(800, 1400);
				} else if (!inventory.contains(Food)
						&& inventory.contains(Food)) {
					inventory.getItem(foodID).doAction("Eat");
					return random(800, 1400);
				} else {
					Status = "Out of food! shutting down";
					log("We are out of food! logging out");
					if (getMyPlayer().isInCombat()) {
						sleep(random(10000, 11000));
					}
					stopScript(true);
				}
			}
		}

		if (inventory.getCount(Junk) >= 1) {
			Status = "Dropping junk";
			inventory.getItem(Junk).doAction("Drop");
		}

		if (getMyPlayer().getInteracting() != null) {
			Status = "Engaged in Combat";
		} else {
			Status = "We are idle";
		}

		if (Wait) {
			while (getMyPlayer().getInteracting() instanceof RSNPC
					|| getMyPlayer().isInCombat()) {
			}
		}
		if (arrowID != -1 && getMyPlayer().getInteracting() == null
				&& groundItems.getNearest(arrowPack) != null) {
			handleArrows();
		} else {
			if (yak != null) {
				Point nextNPC = yak.getScreenLocation();
				if (nextNPC != null && getMyPlayer().isInCombat()) {
					Status = "Hovering Yaks";
					mouse.move(new Point(centerPoint(yak.getModel(),
							random(3, 5))));
				}
			}
		}

		if (doSpec) {
			if (rndSpecCtr == 0 && settings.getSetting(300) >= specialCost) {
				if (settings.getSetting(301) != 1) {
					rndSpec = settings.getSetting(300);
				} else if (settings.getSetting(301) == 1) {
					int tempRndSpec = (settings.getSetting(300)) - specialCost;
					if (tempRndSpec >= specialCost) {
						rndSpec = tempRndSpec;
					} else {
						getRndSpec();
					}
				}
				rndSpecCtr = 1;
			} else {
				if (settings.getSetting(300) < specialCost) {
					if (inventory.containsOneOf(CURR_WEP_ID)) {
						inventory.getItem(CURR_WEP_ID).doAction("Wield");
						sleep(random(1000, 1100));
						mouse.moveSlightly();
					}
					if (rndSpecCtr == 0) {
						getRndSpec();
						rndSpecCtr = 1;
					}
				}
			}
		}

		switch (getAction()) {
		case SPECIAL:
			if (inventory.containsOneOf(SPEC_WEP_ID)) {
				inventory.getItem(SPEC_WEP_ID).doAction("Wield");
				sleep(random(1000, 1100));
			}
			while (getMyPlayer().getInteracting() != null
					&& settings.getSetting(300) >= specialCost) {
				if (getMyPlayer().getInteracting() == null) {
					break;
				}
				if (game.getCurrentTab() != Game.TAB_ATTACK) {
					game.openTab(Game.TAB_ATTACK);
					sleep(random(400, 600));
				}
				if (settings.getSetting(301) != 1) {
					interfaces.get(884).getComponent(4).doClick();
					sleep(random(900, 1000));
				} else {
					sleep(random(100, 300));
				}
			}
			break;

		case KILLYAKS:
			runControl();

			RSGroundItem rangeStuff = groundItems.getNearest(arrowID);
			if (rangeStuff != null && arrowID != -1) {
				if (inventory.getCount(arrowID) == random(50, 100)) {
					if (game.getCurrentTab() != 4) {
						game.openTab(4);
					}
					Status = "Equiping Arrows";
					inventory.getItem(arrowID).doAction("Wield");
					return random(15000, 30000);
				}
			} else {
				rangeStuff = null;
			}
			if (inventory.getCount(potionSet) >= 1 && shouldDrinkPot()) {
				for (String aPotion : potionsToDrink) {
					drinkPot(aPotion);
				}
			}

			if (yak != null && getMyPlayer().getInteracting() == null) {
				Status = "Attacking Yaks";
				if (clickNPC(yak, "Attack"))
					mouse.moveOffScreen();
				return random(400, 600);
			}
			break;
		case KILLSCRIPT:
			log.warning("Stopping script get to the Yak Pen on Neitiznot.");
			stopScript(true);
			return random(100, 200);
		}

		return random(200, 400);
	}

	@Override
	public boolean onStart() {
		gui = new YakAttackProGUI();
		gui.setVisible(true);
		while (guiWait) {
			if (!gui.isVisible())
				return false;
			sleep(100);
		}
		gui.setVisible(false);
		if (guiExit)
			return false;

		for (int i = 0; i < startingexp.length; i++) {
			if (startinglvl[i] >= 1)
				continue;
			startingexp[i] = skills.getCurrentExp(statInfo(i));
		}
		for (int i = 0; i < startinglvl.length; i++) {
			if (startinglvl[i] >= 1)
				continue;
			startinglvl[i] = skills.getRealLevel(statInfo(i));
		}

		log("************************************************");
		log("********YakAttack V" + properties.version()
				+ " started!**************");
		log("*********Let's get some Experience**************");
		log("************************************************");
		mouse.setSpeed(speed);
		camera.setPitch(true);
		if (!t.isAlive()) {
			t.start();
			log("Built-in camera initialized!");
		}
		return true;
	}

	private void runControl() {
		if (!walking.isRunEnabled() && walking.getEnergy() > random(20, 30)) {
			walking.setRun(true);
		}
	}

	@Override
	public void messageReceived(MessageEvent e) {
		String serverString = e.getMessage();

		if (serverString.contains("System update in")) {
			log.warning("There will be a system update soon, so we logged out");
			stopScript(true);
		}
		if (serverString.contains("Oh dear, you are dead!")) {
			Status = "Dead";
			log.warning("We somehow died :S, shutting down");
			stopScript(true);
		}
		if (serverString.contains("already under attack")) {
			sleep(random(2500, 4000));
		}
		if (serverString.contains("There is no ammo left in your quiver.")) {
			log.warning("We have no arrows left, shutting down!");
			stopScript(true);
		}

		if (serverString
				.contentEquals("You can't log out until 10 seconds after the end of combat.")) {
			log("Waiting 10 seconds before logging out");
			sleep(random(10100, 11000));
			stopScript(true);
		}
		if (serverString.contains("You've just advanced an")) {
			log("Congrats on level up, Screenshot taken!");
			env.saveScreenshot(true);
			interfaces.clickContinue();
		}
	}

	private String locToString() {
		return "(X: " + getMyPlayer().getLocation().getX() + ", Y:"
				+ getMyPlayer().getLocation().getY() + ")";
	}

	@Override
	public void onRepaint(Graphics g) {
		((Graphics2D) g).setRenderingHint(RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_ON);
		int mouse_x = mouse.getLocation().x;
		int mouse_y = mouse.getLocation().y;
		int mouse_press_x = mouse.getPressLocation().x;
		int mouse_press_y = mouse.getPressLocation().y;
		long mouse_press_time = mouse.getPressTime();
		if (game.isLoggedIn() && !guiWait)
			if (System.currentTimeMillis() - mouse_press_time < 100L) {
				g.setColor(new Color(70, 130, 180, 250));
				g.drawString("C", mouse_press_x, mouse_press_y);
			} else if (System.currentTimeMillis() - mouse_press_time < 200L
					&& System.currentTimeMillis() - mouse_press_time > 99L) {
				g.setColor(new Color(70, 130, 180, 225));
				g.drawString("Cl", mouse_press_x, mouse_press_y);
			} else if (System.currentTimeMillis() - mouse_press_time < 300L
					&& System.currentTimeMillis() - mouse_press_time > 199L) {
				g.setColor(new Color(70, 130, 180, 200));
				g.drawString("Cli", mouse_press_x, mouse_press_y);
			} else if (System.currentTimeMillis() - mouse_press_time < 400L
					&& System.currentTimeMillis() - mouse_press_time > 299L) {
				g.setColor(new Color(70, 130, 180, 175));
				g.drawString("Clic", mouse_press_x, mouse_press_y);
			} else if (System.currentTimeMillis() - mouse_press_time < 500L
					&& System.currentTimeMillis() - mouse_press_time > 399L) {
				g.setColor(new Color(70, 130, 180, 150));
				g.drawString("Click", mouse_press_x, mouse_press_y);
			} else if (System.currentTimeMillis() - mouse_press_time < 600L
					&& System.currentTimeMillis() - mouse_press_time > 499L) {
				g.setColor(new Color(70, 130, 180, 125));
				g.drawString("Click", mouse_press_x, mouse_press_y);
			} else if (System.currentTimeMillis() - mouse_press_time < 700L
					&& System.currentTimeMillis() - mouse_press_time > 599L) {
				g.setColor(new Color(70, 130, 180, 100));
				g.drawString("Click", mouse_press_x, mouse_press_y);
			} else if (System.currentTimeMillis() - mouse_press_time < 800L
					&& System.currentTimeMillis() - mouse_press_time > 699L) {
				g.setColor(new Color(70, 130, 180, 75));
				g.drawString("Click", mouse_press_x, mouse_press_y);
			} else if (System.currentTimeMillis() - mouse_press_time < 900L
					&& System.currentTimeMillis() - mouse_press_time > 799L) {
				g.setColor(new Color(70, 130, 180, 50));
				g.drawString("Click", mouse_press_x, mouse_press_y);
			} else if (System.currentTimeMillis() - mouse_press_time < 1000L
					&& System.currentTimeMillis() - mouse_press_time > 899L) {
				g.setColor(new Color(70, 130, 180, 25));
				g.drawString("Click", mouse_press_x, mouse_press_y);
			}
		Polygon po = new Polygon();
		po.addPoint(mouse_x, mouse_y);
		po.addPoint(mouse_x, mouse_y + 15);
		po.addPoint(mouse_x + 10, mouse_y + 10);
		g.setColor(new Color(70, 130, 180, 125));
		g.fillPolygon(po);
		g.drawPolygon(po);

		try {
			final Point Mouse = new Point(mouse.getLocation().x,
					mouse.getLocation().y);
			if (foodInBag != null && foodInBag.getName() != null) {
				if (Status.equals("Eating food")
						&& inventory.getInterface().getArea().contains(Mouse))
					try {
						drawItem(g, foodInBag, true);
					} catch (Exception ignored) {
					}
			} else {
				foodInBag = null;
			}
		} catch (Exception ignored) {
		}

		for (int i = 0; i < currentlvl.length; i++) {
			currentlvl[i] = skills.getRealLevel(statInfo(i));
			currentexp[i] = skills.getCurrentExp(statInfo(i));
			diffexp[i] = (currentexp[i] - startingexp[i]);
		}

		StringBuilder localStringBuilder = new StringBuilder();
		long l1 = timeRan.getElapsed();
		long l2 = l1 / 1000L;
		long l3 = l2 / 60L;
		long l4 = l3 / 60L;
		long l5 = l4 / 24L;
		int j = (int) l2 % 60;
		int k = (int) l3 % 60;
		int m = (int) l4 % 60;
		int n = (int) l5;
		if (n > 3) {
			localStringBuilder.append(" ");
			localStringBuilder.append(buildString(n));
			localStringBuilder.append(" Days");
		} else {
			if (m < 10)
				localStringBuilder.append("0");
			localStringBuilder.append(m);
			localStringBuilder.append(" : ");
			if (k < 10)
				localStringBuilder.append("0");
			localStringBuilder.append(k);
			localStringBuilder.append(" : ");
			if (j < 10)
				localStringBuilder.append("0");
			localStringBuilder.append(j);
		}
		int i1 = 0;
		int i2 = 0;
		String firstSet = "";
		StringBuilder localStringBuilderNew = new StringBuilder();

		if (l2 > 0L) {
			for (int i3 = 0; i3 < diffexp.length; i3++) {
				if (diffexp[i3] > 0) {
					int i4 = diffexp[i3] * 60 * 60 / (int) l2;
					int i5 = exptolevel[currentlvl[i3]] - currentexp[i3];
					StringBuilder localStringBuilder3 = new StringBuilder();
					long l6 = i5 * timeRan.getElapsed() / diffexp[i3];

					long l7 = l6 / 1000L;
					long l8 = l7 / 60L;
					long l9 = l8 / 60L;
					long l10 = l9 / 24L;
					int i17 = (int) l7 % 60;
					int i18 = (int) l8 % 60;
					int i19 = (int) l9 % 60;
					int i20 = (int) l10;
					if (i20 > 3) {
						localStringBuilder3.append(" ");
						localStringBuilder3.append(buildString(i20));
						localStringBuilder3.append(" Days");
					} else {
						if (i19 < 10)
							localStringBuilder3.append("0");
						localStringBuilder3.append(i19);
						localStringBuilder3.append(" : ");
						if (i18 < 10)
							localStringBuilder3.append("0");
						localStringBuilder3.append(i18);
						localStringBuilder3.append(" : ");
						if (i17 < 10)
							localStringBuilder3.append("0");
						localStringBuilder3.append(i17);
					}

					firstSet = new StringBuilder()
							.append(firstSet)
							.append((currentexp[i3] - exptolevel[(currentlvl[i3] - 1)])
									* 100
									/ (exptolevel[currentlvl[i3]] - exptolevel[(currentlvl[i3] - 1)]))
							.append("qaswed").toString();

					localStringBuilderNew.append(new StringBuilder()
							.append(statnames[i3]).append(" ")
							.append(currentlvl[i3]).append('/')
							.append(startinglvl[i3]).append("  |  EXP-TL: ")
							.append(buildString(i5)).append("  |  Gain: ")
							.append(buildString(diffexp[i3]))
							.append("  |  EXP/HR: ").append(buildString(i4))
							.append("  |  TTL: ").append(localStringBuilder3)
							.append("qaswed").toString());

					i1 += i4;
					i2 += diffexp[i3];
				}

			}

		}

		String str2 = new StringBuilder().append("Time ran: ")
				.append(localStringBuilder).append("  |  Total Gain: ")
				.append(buildString(i2)).append("  |  Total EXP/HR: ")
				.append(buildString(i1)).append("  |  Location: ")
				.append(locToString()).append("qaswed")
				.append(localStringBuilderNew.toString()).toString();

		String[] arrayOfStringOne = str2.split("qaswed");
		String[] arrayOfStringTwo = new StringBuilder().append("100qaswed")
				.append(firstSet).toString().split("qaswed");

		int i6 = 120;
		int i7 = 90;
		int i8 = 30;

		int i9 = 4;
		int i10 = 3;

		int i11 = arrayOfStringOne.length;
		int i12 = 9;
		int i13 = 346;
		int i14 = 485;
		int i15 = 17;
		int i16 = i15 * i11;
		g.setColor(new Color(0, 0, 0, 130));
		g.fillRoundRect(i12 - 2, i13 - 2, i14 + 5, 5 + i16, 10, 10);

		for (int i17 = 0; i17 < i11; i17++) {
			g.setColor(new Color(i6, i7, i8, 190));
			g.drawRoundRect(i12, i13 + 1 + i17 * i15, i14, i15 - 2, 10, 10);

			g.fillRoundRect(i12, i13 + 1 + i17 * i15, i14, i15 - 2, 10, 10);

			g.setColor(new Color(i6 * 2 / 3, i7 * 2 / 3, i8 * 2 / 3, 190));

			g.fillRoundRect(i12, i13 + 1 + i17 * i15,
					i14 * Integer.parseInt(arrayOfStringTwo[i17]) / 100,
					i15 - 2, 10, 10);

			g.setColor(Color.BLACK);
			g.drawString(arrayOfStringOne[i17], i12 + 5, i13 - i10 + (i17 + 1)
					* i15);

			g.setColor(Color.WHITE);
			g.drawString(arrayOfStringOne[i17], i12 + 5, i13 - i9 + (i17 + 1)
					* i15);
		}

		int xpHour = (int) (3600000.0D / timeRan.getElapsed() * i2);
		yaksKilled = (i2 / 200);
		yaksPerHour = (xpHour / 200);
		String[] arrayOfStringThree = {
				new StringBuilder().append(properties.name()).append(" v")
						.append(properties.version()).toString(),
				new StringBuilder().append("Status: ").append(Status)
						.toString(),
				new StringBuilder().append("Yaks Killed: ").append(yaksKilled)
						.append("  |  YaksKilled/HR: ").append(yaksPerHour)
						.toString(), "www.powerbot.org" };

		i12 = 5;
		i13 = 30;
		i14 = 210;
		i15 = 16;
		i11 = arrayOfStringThree.length;
		i16 = i15 * i11;

		g.setColor(new Color(0, 0, 0, 130));
		g.fillRoundRect(i12 - 2, i13 - 2, i14 + 5, 5 + i16, 10, 10);

		g.setColor(new Color(i6, i7, i8, 120));
		g.drawRoundRect(i12, i13 + 1 + 0 * i15, i14, i15 - 2, 10, 10);

		g.fillRoundRect(i12, i13 + 1 + 0 * i15, i14, i15 - 2, 10, 10);

		for (int i18 = 0; i18 < i11; i18++) {
			g.setColor(Color.BLACK);
			g.drawString(arrayOfStringThree[i18], i12 + 5, i13 - i10
					+ (i18 + 1) * i15);

			g.setColor(Color.WHITE);
			g.drawString(arrayOfStringThree[i18], i12 + 5, i13 - i9 + (i18 + 1)
					* i15);
		}
	}

	@Override
	public void onFinish() {
		env.saveScreenshot(true);
		log.info("Ran for " + timeRan.toElapsedString());
		if (!players.getMyPlayer().isInCombat()) {
			stopScript(false);
		}
	}

	private class YakAttackAntiBan implements Runnable {

		@Override
		public void run() {
			Random random = new Random();
			while (isActive()) {
				try {
					if (random.nextInt(Math.abs(15 - 0)) == 0) {
						final char[] LR = new char[] { KeyEvent.VK_LEFT,
								KeyEvent.VK_RIGHT };
						final char[] UD = new char[] { KeyEvent.VK_DOWN,
								KeyEvent.VK_UP };
						final char[] LRUD = new char[] { KeyEvent.VK_LEFT,
								KeyEvent.VK_RIGHT, KeyEvent.VK_UP,
								KeyEvent.VK_UP };
						final int random2 = random.nextInt(Math.abs(2 - 0));
						final int random1 = random.nextInt(Math.abs(2 - 0));
						final int random4 = random.nextInt(Math.abs(4 - 0));

						if (random.nextInt(Math.abs(3 - 0)) == 0) {
							keyboard.pressKey(LR[random1]);
							Thread.sleep(random.nextInt(Math.abs(400 - 100)));
							keyboard.pressKey(UD[random2]);
							Thread.sleep(random.nextInt(Math.abs(600 - 300)));
							keyboard.releaseKey(UD[random2]);
							Thread.sleep(random.nextInt(Math.abs(400 - 100)));
							keyboard.releaseKey(LR[random1]);
						} else {
							keyboard.pressKey(LRUD[random4]);
							if (random4 > 1) {
								Thread.sleep(random.nextInt(Math.abs(600 - 300)));
							} else {
								Thread.sleep(random.nextInt(Math.abs(900 - 500)));
							}
							keyboard.releaseKey(LRUD[random4]);
						}
					} else {
						Thread.sleep(random.nextInt(Math.abs(2000 - 200)));
					}
				} catch (Exception e) {
					System.out.println("AntiBan error detected!");
				}
			}
		}
	}

	@SuppressWarnings("serial")
	public class YakAttackProGUI extends JFrame {
		public YakAttackProGUI() {
			initComponents();
		}

		private void StartActionPerformed(ActionEvent e) {
			Status = "Setting up Gui";

			wants2Eat = checkBox8.isSelected();
			doSpec = checkBox7.isSelected();
			Wait = checkBox6.isSelected();
			speed = mouseSpeedSlider.getValue();

			log("Getting MouseSpeed at " + speed);

			if (wants2Eat) {
				hp2EatAt = Integer.parseInt(textField1.getText());

				if (list2.getSelectedValue().equals("None")) {
					wants2Eat = false;
				} else if (list2.getSelectedValue().equals("Cake")) {
					Food = 1891;
					Status = "Eating Cake";
				} else if (list2.getSelectedValue().equals("Chocolate cake")) {
					Food = 1897;
					Status = "Eating Chocolate cake";
				} else if (list2.getSelectedValue().equals("Plain pizza")) {
					Food = 2289;
					Status = "Eating Plain pizza";
				} else if (list2.getSelectedValue().equals("Pineapple pizza")) {
					Food = 2301;
					Status = "Eating Pineapple pizza";
				} else if (list2.getSelectedValue().equals("Meat pizza")) {
					Food = 2293;
					Status = "Eating Meat pizza";
				} else if (list2.getSelectedValue().equals("Lobster")) {
					Food = 379;
					Status = "Eating Lobster";
				} else if (list2.getSelectedValue().equals("Cavefish")) {
					Food = 15266;
					Status = "Eating Cavefish";
				} else if (list2.getSelectedValue().equals("Salmon")) {
					Food = 329;
					Status = "Eating Salmon";
				} else if (list2.getSelectedValue().equals("Tuna")) {
					Food = 361;
					Status = "Eating Tuna";
				} else if (list2.getSelectedValue().equals("Trout")) {
					Food = 333;
					Status = "Eating Trout";
				} else if (list2.getSelectedValue().equals("Sharks")) {
					Food = 385;
					Status = "Eating Sharks";
				} else if (list2.getSelectedValue().equals("Monkfish")) {
					Food = 7946;
					Status = "Eating Monkfish";
				} else if (list2.getSelectedValue().equals("Manta ray")) {
					Food = 391;
					Status = "Eating Manta ray";
				} else if (list2.getSelectedValue().equals("Sea turtle")) {
					Food = 397;
					Status = "Eating Sea turtle";
				} else if (list2.getSelectedValue().equals("Swordfish")) {
					Food = 373;
					Status = "Eating Swordfish";
				} else if (list2.getSelectedValue().equals("Rocktail")) {
					Food = 15272;
					Status = "Eating Rocktail";
				}
			}

			if (doSpec) {
				log("Special Attacks Enabled");

				if (specComboBox.getSelectedItem()
						.equals("Dragon dagger (p++)")) {
					SPEC_WEP_ID = 5698;
					specialCost = 250;
				} else if (specComboBox.getSelectedItem().equals(
						"Dragon longsword")) {
					SPEC_WEP_ID = 1305;
					specialCost = 250;
				} else if (specComboBox.getSelectedItem().equals("Dragon mace")) {
					SPEC_WEP_ID = 1434;
					specialCost = 250;
				} else if (specComboBox.getSelectedItem().equals(
						"Dragon battleaxe")) {
					SPEC_WEP_ID = 1377;
					specialCost = 1000;
				} else if (specComboBox.getSelectedItem().equals(
						"Dragon halberd")) {
					SPEC_WEP_ID = 3204;
					specialCost = 300;
				} else if (specComboBox.getSelectedItem().equals(
						"Dragon scimitar")) {
					SPEC_WEP_ID = 4587;
					specialCost = 550;
				} else if (specComboBox.getSelectedItem()
						.equals("Dragon claws")) {
					SPEC_WEP_ID = 14484;
					specialCost = 500;
				} else if (specComboBox.getSelectedItem().equals(
						"Dragon 2h sword")) {
					SPEC_WEP_ID = 7158;
					specialCost = 550;
				} else if (specComboBox.getSelectedItem().equals(
						"Dorgeshuun c'bow")) {
					SPEC_WEP_ID = 8880;
					specialCost = 900;
				} else if (specComboBox.getSelectedItem().equals(
						"Magic shortbow")) {
					SPEC_WEP_ID = 861;
					specialCost = 550;
				} else if (specComboBox.getSelectedItem().equals("Seercull")) {
					SPEC_WEP_ID = 6724;
					specialCost = 1000;
				} else if (specComboBox.getSelectedItem().equals("Dark bow")) {
					SPEC_WEP_ID = 11235;
					specialCost = 650;
				} else if (specComboBox.getSelectedItem()
						.equals("Granite maul")) {
					SPEC_WEP_ID = 4153;
					specialCost = 500;
				} else if (specComboBox.getSelectedItem()
						.equals("Granite mace")) {
					SPEC_WEP_ID = 14679;
					specialCost = 500;
				} else if (specComboBox.getSelectedItem()
						.equals("Abyssal whip")) {
					SPEC_WEP_ID = 4151;
					specialCost = 500;
				} else if (specComboBox.getSelectedItem()
						.equals("Ancient mace")) {
					SPEC_WEP_ID = 11061;
					specialCost = 1000;
				} else if (specComboBox.getSelectedItem().equals(
						"Saradomin sword")) {
					SPEC_WEP_ID = 11730;
					specialCost = 250;
				} else if (specComboBox.getSelectedItem().equals(
						"Armadyl godsword")) {
					SPEC_WEP_ID = 11694;
					specialCost = 500;
				} else if (specComboBox.getSelectedItem().equals(
						"Bandos godsword")) {
					SPEC_WEP_ID = 11696;
					specialCost = 1000;
				} else if (specComboBox.getSelectedItem().equals(
						"Saradomin godsword")) {
					SPEC_WEP_ID = 11698;
					specialCost = 500;
				} else if (specComboBox.getSelectedItem().equals(
						"Zamorak godsword")) {
					SPEC_WEP_ID = 11700;
					specialCost = 600;
				}
				if (!orginalWepTextField.getText().isEmpty()) {
					try {
						CURR_WEP_ID = Integer.parseInt(orginalWepTextField
								.getText());
					} catch (NumberFormatException ignored) {
					}
				} else {
					CURR_WEP_ID = specComboBox.getSelectedIndex();
				}
			} else {
				log("Not doing Spec");
			}

			if (checkBox8.isSelected()) {
				if (list3.getSelectedItem().equals("Bronze arrow")) {
					arrowID = bronzeArrow;
					arrowName = "Bronze arrow";
				} else if (list3.getSelectedItem().equals("Iron arrow")) {
					arrowID = ironArrow;
					arrowName = "Iron arrow";
				} else if (list3.getSelectedItem().equals("Steel arrow")) {
					arrowID = steelArrow;
					arrowName = "Steel arrow";
				} else if (list3.getSelectedItem().equals("Mithril arrow")) {
					arrowID = mithrilArrow;
					arrowName = "Mithril arrow";
				} else if (list3.getSelectedItem().equals("Adamant arrow")) {
					arrowID = adamantArrow;
					arrowName = "Adamant arrow";
				} else if (list3.getSelectedItem().equals("Rune arrow")) {
					arrowID = runeArrow;
					arrowName = "Rune arrow";
				} else if (list3.getSelectedItem().equals("Bronze bolts")) {
					arrowID = bronzeBolt;
					arrowName = "Bronze bolts";
				} else if (list3.getSelectedItem().equals("Bluerite bolts")) {
					arrowID = blueriteBolt;
					arrowName = "Bluerite bolts";
				} else if (list3.getSelectedItem().equals("Bone bolts")) {
					arrowID = boneBolt;
					arrowName = "Bone bolts";
				} else if (list3.getSelectedItem().equals("Iron bolts")) {
					arrowID = ironBolt;
					arrowName = "Iron bolts";
				} else if (list3.getSelectedItem().equals("Steel bolts")) {
					arrowID = steelBolt;
					arrowName = "Steel bolts";
				} else if (list3.getSelectedItem().equals("Black bolts")) {
					arrowID = blackBolt;
					arrowName = "Bronze bolts";
				} else if (list3.getSelectedItem().equals("Mithril bolts")) {
					arrowID = mithrilBolt;
					arrowName = "Mithril bolts";
				} else if (list3.getSelectedItem().equals("Adamant bolts")) {
					arrowID = adamantBolt;
					arrowName = "Adamant bolts";
				} else if (list3.getSelectedItem().equals("Rune bolts")) {
					arrowID = runeBolt;
					arrowName = "Rune bolts";
				} else if (list3.getSelectedItem().equals("Broad bolts")) {
					arrowID = broadBolt;
					arrowName = "Broad bolts";
				} else if (list3.getSelectedItem().equals("Bronze knife")) {
					arrowID = bronzeKnife;
					arrowName = "Bronze knife";
				} else if (list3.getSelectedItem().equals("Iron knife")) {
					arrowID = ironKnife;
					arrowName = "Iron knife";
				} else if (list3.getSelectedItem().equals("Steel knife")) {
					arrowID = steelKnife;
					arrowName = "Steel knife";
				} else if (list3.getSelectedItem().equals("Black knife")) {
					arrowID = blackKnife;
					arrowName = "Black knife";
				} else if (list3.getSelectedItem().equals("Mithril knife")) {
					arrowID = mithrilKnife;
					arrowName = "Mithril knife";
				} else if (list3.getSelectedItem().equals("Adamant knife")) {
					arrowID = adamantKnife;
					arrowName = "Adamant knife";
				} else if (list3.getSelectedItem().equals("Rune knife")) {
					arrowID = runeKnife;
					arrowName = "Rune knife";
				} else if (list3.getSelectedItem().equals("Bronze dart")) {
					arrowID = bronzeDart;
					arrowName = "Bronze dart";
				} else if (list3.getSelectedItem().equals("Iron dart")) {
					arrowID = ironDart;
					arrowName = "Iron dart";
				} else if (list3.getSelectedItem().equals("Steel dart")) {
					arrowID = steelDart;
					arrowName = "Steel dart";
				} else if (list3.getSelectedItem().equals("Black dart")) {
					arrowID = blackDart;
					arrowName = "Black dart";
				} else if (list3.getSelectedItem().equals("Mithril dart")) {
					arrowID = mithrilDart;
					arrowName = "Mithril dart";
				} else if (list3.getSelectedItem().equals("Adamant dart")) {
					arrowID = adamantDart;
					arrowName = "Adamant dart";
				} else if (list3.getSelectedItem().equals("Rune dart")) {
					arrowID = runeDart;
					arrowName = "Rune dart";
				}
			}
			guiWait = false;
		}

		private void ExitActionPerformed(ActionEvent e) {
			guiWait = false;
			guiExit = true;
			dispose();
		}

		private void initComponents() {
			// GEN-BEGIN:initComponents
			panel1 = new JPanel();
			label1 = new JLabel();
			separator1 = new JSeparator();
			label3 = new JLabel();
			label4 = new JLabel();
			label5 = new JLabel();
			label2 = new JLabel();
			separator2 = new JSeparator();
			separator3 = new JSeparator();
			separator4 = new JSeparator();
			label6 = new JLabel();
			label7 = new JLabel();
			tabbedPane1 = new JTabbedPane();
			panel2 = new JPanel();
			label8 = new JLabel();
			label10 = new JLabel();
			list3 = new JComboBox();
			separator5 = new JSeparator();
			DMLine1 = new JLabel();
			DMLine2 = new JLabel();
			DMLine3 = new JLabel();
			separator8 = new JSeparator();
			label15 = new JLabel();
			checkBox7 = new JCheckBox();
			checkBox6 = new JCheckBox();
			orginalWepLabel = new JLabel();
			orginalWepTextField = new JTextField();
			specComboBox = new JComboBox();
			panel3 = new JPanel();
			label9 = new JLabel();
			scrollPane2 = new JScrollPane();
			list2 = new JList();
			label17 = new JLabel();
			textField1 = new JTextField();
			checkBox8 = new JCheckBox();
			separator10 = new JSeparator();
			mouseSpeedLabel = new JLabel();
			mouseSpeedSlider = new JSlider();
			Start = new JButton();
			Exit = new JButton();
			label20 = new JLabel();

			// ======== this ========
			setTitle("YakAttack PR0");
			Container contentPane = getContentPane();
			contentPane.setLayout(null);

			// ======== panel1 ========
			{
				panel1.setBackground(new Color(204, 204, 204));
				panel1.setLayout(null);

				// ---- label1 ----
				label1.setText("YakAttack PR0");
				label1.setFont(new Font("Times New Roman", Font.PLAIN, 40));
				panel1.add(label1);
				label1.setBounds(130, 5, 260, 56);
				panel1.add(separator1);
				separator1.setBounds(0, 65, 550, 10);

				// ---- label3 ----
				label3.setText("Ranged");
				label3.setFont(label3.getFont().deriveFont(Font.ITALIC,
						label3.getFont().getSize() + 2f));
				panel1.add(label3);
				label3.setBounds(465, 45, 70, 16);

				// ---- label4 ----
				label4.setText("Melee");
				label4.setFont(label4.getFont().deriveFont(Font.ITALIC,
						label4.getFont().getSize() + 2f));
				panel1.add(label4);
				label4.setBounds(35, 40, 60, 21);

				// ---- label5 ----
				label5.setText("Fast");
				label5.setFont(label5.getFont().deriveFont(Font.ITALIC,
						label5.getFont().getSize() + 2f));
				panel1.add(label5);
				label5.setBounds(10, 10, 70, 16);

				// ---- label2 ----
				label2.setText("Special Attacks");
				label2.setFont(label2.getFont().deriveFont(Font.ITALIC,
						label2.getFont().getSize() + 2f));
				panel1.add(label2);
				label2.setBounds(440, 5, 120, 16);

				// ---- separator2 ----
				separator2.setOrientation(SwingConstants.VERTICAL);
				panel1.add(separator2);
				separator2.setBounds(100, 0, 15, 65);

				// ---- separator3 ----
				separator3.setOrientation(SwingConstants.VERTICAL);
				panel1.add(separator3);
				separator3.setBounds(435, 0, 80, 65);
				panel1.add(separator4);
				separator4.setBounds(0, 310, 550, 10);

				// ---- label6 ----
				label6.setText("Version: " + properties.version());
				label6.setBackground(new Color(51, 204, 0));
				label6.setForeground(new Color(51, 102, 0));
				panel1.add(label6);
				label6.setBounds(10, 315, 58, 14);

				// ---- label7 ----
				label7.setText("Author: Pervy Shuya");
				label7.setFont(label7.getFont().deriveFont(
						label7.getFont().getStyle() | Font.BOLD));
				panel1.add(label7);
				label7.setBounds(425, 315, 116, 14);

				// ======== tabbedPane1 ========
				{
					tabbedPane1.setBackground(new Color(204, 204, 204));

					// ======== panel2 ========
					{
						panel2.setBackground(Color.white);
						panel2.setLayout(null);

						// ---- label8 ----
						label8.setText("Spec weapon");
						label8.setFont(new Font("MV Boli", Font.BOLD, 15));
						panel2.add(label8);
						label8.setBounds(15, 5, 110,
								label8.getPreferredSize().height);

						// ---- label10 ----
						label10.setText("Pick Up Arrows/Bolts?");
						label10.setFont(new Font("MV Boli", Font.BOLD, 15));
						panel2.add(label10);
						label10.setBounds(145, 5, 180, 26);

						// ---- list3 ----
						list3.setModel(new DefaultComboBoxModel(new String[] {
								"None", "Bronze arrow", "Iron arrow",
								"Mithril arrow", "Adamant arrow", "Rune arrow",
								"Bronze bolts", "Bluerite bolts", "Iron bolts",
								"Steel bolts", "Black bolts", "Mithril bolts",
								"Adamant bolts", "Rune bolts", "Broad bolts",
								"Bronze knife", "Iron knife", "Steel knife",
								"Black knife", "Mithril knife",
								"Adamant knife", "Rune knife", "Bronze dart",
								"Iron dart", "Steel dart", "Black dart",
								"Mithril dart", "Adamant dart", "Rune dart" }));
						panel2.add(list3);
						list3.setBounds(185, 40, 120, 25);
						panel2.add(separator5);
						separator5.setBounds(0, 145, 145, 10);
						panel2.add(DMLine1);
						DMLine1.setBounds(5, 140,
								DMLine1.getPreferredSize().width, 15);
						panel2.add(DMLine2);
						DMLine2.setBounds(new Rectangle(new Point(5, 155),
								DMLine2.getPreferredSize()));
						panel2.add(DMLine3);
						DMLine3.setBounds(new Rectangle(new Point(5, 170),
								DMLine3.getPreferredSize()));
						panel2.add(separator8);
						separator8.setBounds(145, 145, 190, 10);

						// ---- label15 ----
						label15.setText("GUI Made By Pervy");
						label15.setFont(label15.getFont().deriveFont(
								label15.getFont().getStyle() | Font.BOLD,
								label15.getFont().getSize() - 2f));
						label15.setEnabled(false);
						panel2.add(label15);
						label15.setBounds(new Rectangle(new Point(220, 150),
								label15.getPreferredSize()));

						// ---- checkBox7 ----
						checkBox7.setText("Use Special Attacks");
						checkBox7.setBackground(Color.white);
						panel2.add(checkBox7);
						checkBox7.setBounds(new Rectangle(new Point(145, 105),
								checkBox7.getPreferredSize()));

						// ---- checkBox6 ----
						checkBox6.setText("Would you like to wait for arrows?");
						checkBox6.setBackground(Color.white);
						panel2.add(checkBox6);
						checkBox6.setBounds(145, 70, 189,
								checkBox6.getPreferredSize().height);

						// ---- orginalWepLabel ----
						orginalWepLabel.setText("Orginal Weapon ID:");
						orginalWepLabel.setFont(new Font("Script MT Bold",
								Font.BOLD | Font.ITALIC, 12));
						panel2.add(orginalWepLabel);
						orginalWepLabel.setBounds(5, 70, 130, 20);

						// ---- orginalWepTextField ----
						orginalWepTextField
								.setHorizontalAlignment(SwingConstants.CENTER);
						panel2.add(orginalWepTextField);
						orginalWepTextField.setBounds(25, 105, 77,
								orginalWepTextField.getPreferredSize().height);

						// ---- specComboBox ----
						specComboBox.setModel(new DefaultComboBoxModel(
								new String[] { "Dragon dagger (p++)",
										"Dragon longsword", "Dragon mace",
										"Dragon battleaxe", "Dragon halberd",
										"Dragon scimitar", "Dragon claws",
										"Dragon 2h sword", "Dorgeshuun c'bow",
										"Magic shortbow", "Seercull",
										"Dark bow", "Granite maul",
										"Granite mace", "Abyssal whip",
										"Ancient mace", "Saradomin sword",
										"Armadyl godsword", "Bandos godsword",
										"Saradomin godsword",
										"Zamorak godsword" }));
						panel2.add(specComboBox);
						specComboBox.setBounds(new Rectangle(new Point(5, 30),
								specComboBox.getPreferredSize()));

						{ // compute preferred size
							Dimension preferredSize = new Dimension();
							for (int i = 0; i < panel2.getComponentCount(); i++) {
								Rectangle bounds = panel2.getComponent(i)
										.getBounds();
								preferredSize.width = Math.max(bounds.x
										+ bounds.width, preferredSize.width);
								preferredSize.height = Math.max(bounds.y
										+ bounds.height, preferredSize.height);
							}
							Insets insets = panel2.getInsets();
							preferredSize.width += insets.right;
							preferredSize.height += insets.bottom;
							panel2.setMinimumSize(preferredSize);
							panel2.setPreferredSize(preferredSize);
						}
					}
					tabbedPane1.addTab("Combat Options", panel2);

					// ======== panel3 ========
					{
						panel3.setBackground(Color.white);
						panel3.setLayout(null);

						// ---- label9 ----
						label9.setText("What To Eat?");
						label9.setFont(new Font("MV Boli", Font.BOLD, 15));
						panel3.add(label9);
						label9.setBounds(10, 5, 125, 27);

						// ======== scrollPane2 ========
						{

							// ---- list2 ----
							list2.setModel(new AbstractListModel() {
								String[] values = { "None", "Lobster",
										"Salmon", "Tuna", "Trout", "Sharks",
										"Monkfish", "Manta ray", "Sea turtle",
										"Cake", "Chocolate cake",
										"Plain pizza", "Pineapple pizza",
										"Meat pizza", "Rocktail", "Cavefish",
										"Swordfish" };

								@Override
								public int getSize() {
									return values.length;
								}

								@Override
								public Object getElementAt(int i) {
									return values[i];
								}
							});
							list2.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
							scrollPane2.setViewportView(list2);
						}
						panel3.add(scrollPane2);
						scrollPane2.setBounds(5, 35, 105, 135);

						// ---- label17 ----
						label17.setText("What HP To Eat At");
						panel3.add(label17);
						label17.setBounds(new Rectangle(new Point(130, 50),
								label17.getPreferredSize()));

						// ---- textField1 ----
						textField1.setText("500");
						panel3.add(textField1);
						textField1.setBounds(230, 45, 100,
								textField1.getPreferredSize().height);

						// ---- checkBox8 ----
						checkBox8.setText("Would like to eat?");
						checkBox8.setBackground(Color.white);
						panel3.add(checkBox8);
						checkBox8.setBounds(170, 10, 115, 23);
						panel3.add(separator10);
						separator10.setBounds(115, 70, 220, 10);

						// ---- mouseSpeedLabel ----
						mouseSpeedLabel.setText("Adjust Mouse Speed:");
						mouseSpeedLabel.setFont(new Font("Engravers MT",
								Font.PLAIN, 12));
						panel3.add(mouseSpeedLabel);
						mouseSpeedLabel.setBounds(130, 80,
								mouseSpeedLabel.getPreferredSize().width, 20);

						// ---- mouseSpeedSlider ----
						mouseSpeedSlider.setMinimum(1);
						mouseSpeedSlider.setMaximum(10);
						mouseSpeedSlider.setPaintTicks(true);
						mouseSpeedSlider.setPaintLabels(true);
						mouseSpeedSlider.setMajorTickSpacing(1);
						mouseSpeedSlider.setSnapToTicks(true);
						mouseSpeedSlider.setValue(7);
						mouseSpeedSlider.setBackground(Color.lightGray);
						panel3.add(mouseSpeedSlider);
						mouseSpeedSlider.setBounds(115, 105, 220, 65);

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
					tabbedPane1.addTab("Food And Other Settings", panel3);

				}
				panel1.add(tabbedPane1);
				tabbedPane1.setBounds(100, 65, 340, 195);

				// ---- Start ----
				Start.setText("Start Script");
				Start.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						StartActionPerformed(e);
					}
				});
				panel1.add(Start);
				Start.setBounds(155, 275, 90, 25);

				// ---- Exit ----
				Exit.setText("Exit GUI");
				Exit.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						ExitActionPerformed(e);
					}
				});
				panel1.add(Exit);
				Exit.setBounds(305, 275, 90, 25);

				// ---- label20 ----
				label20.setText("text");
				panel1.add(label20);
				label20.setBounds(385, 480, 20, 14);
			}
			contentPane.add(panel1);
			panel1.setBounds(0, 0, 550, 340);

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

		// GEN-BEGIN:variables
		private JPanel panel1;
		private JLabel label1;
		private JSeparator separator1;
		private JLabel label3;
		private JLabel label4;
		private JLabel label5;
		private JLabel label2;
		private JSeparator separator2;
		private JSeparator separator3;
		private JSeparator separator4;
		private JLabel label6;
		private JLabel label7;
		private JTabbedPane tabbedPane1;
		private JPanel panel2;
		private JLabel label8;
		private JLabel label10;
		private JComboBox list3;
		private JSeparator separator5;
		private JLabel DMLine1;
		private JLabel DMLine2;
		private JLabel DMLine3;
		private JSeparator separator8;
		private JLabel label15;
		private JCheckBox checkBox7;
		private JCheckBox checkBox6;
		private JLabel orginalWepLabel;
		private JTextField orginalWepTextField;
		private JComboBox specComboBox;
		private JPanel panel3;
		private JLabel label9;
		private JScrollPane scrollPane2;
		private JList list2;
		private JLabel label17;
		private JTextField textField1;
		private JCheckBox checkBox8;
		private JSeparator separator10;
		private JLabel mouseSpeedLabel;
		private JSlider mouseSpeedSlider;
		private JButton Start;
		private JButton Exit;
		private JLabel label20;
		// GEN-END:variables
	}

}