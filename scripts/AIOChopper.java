import org.rsbot.event.listeners.PaintListener;
import org.rsbot.script.Script;
import org.rsbot.script.ScriptManifest;
import org.rsbot.script.methods.Objects;
import org.rsbot.script.methods.Skills;
import org.rsbot.script.util.Filter;
import org.rsbot.script.util.Timer;
import org.rsbot.script.wrappers.RSArea;
import org.rsbot.script.wrappers.RSGroundItem;
import org.rsbot.script.wrappers.RSModel;
import org.rsbot.script.wrappers.RSObject;
import org.rsbot.script.wrappers.RSPlayer;
import org.rsbot.script.wrappers.RSTile;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * @author Jacmob
 */
@ScriptManifest(name = "AIO Chopper", authors = {"Jacmob"}, keywords = "Woodcutting", version = 1.1,
		description = "Select options in the GUI")
public class AIOChopper extends Script implements PaintListener {

	/* Constants */

	private static enum Action {
		WALK_TO_TREES, WALK_TO_BANK, CHOP, DROP, BANK
	}

	private static enum TreeType {

		TREE(1, new int[]{1276, 1278}, new int[]{1342}),

		OAK(15, new int[]{1281}, new int[]{1356}),

		WILLOW(30, new int[]{5551, 5552, 5553}, new int[]{5554}),

		MAPLE(45, new int[]{1307}, new int[]{7400}),

		YEW(60, new int[]{1309}, new int[]{7402}),

		MAGIC(85, new int[]{1306}, new int[]{7401});

		TreeType(int level, int[] ids, int[] stumps) {
			this.level = level;
			this.ids = ids;
			this.stumps = stumps;
		}

		public final int level;
		public final int[] ids;
		public final int[] stumps;
	}

	public final int[] AXE_IDS = {1351, 1349, 1353, 1361, 1355, 1357, 1359, 4031, 6739, 13470, 14108};
	public final int[] NEST_IDS = {5070, 5071, 5072, 5073, 5074, 5075, 5076, 7413, 11966};

	public final TreeType[] AUTO_QUEUE = {TreeType.WILLOW, TreeType.OAK, TreeType.TREE};

	public final Color BG = new Color(0, 100, 0, 150);
	public final Color DROP = new Color(0, 20, 0, 255);
	public final Color TEXT = new Color(0, 255, 0, 255);
	public final Color FOCUS_PRIMARY = new Color(0, 255, 0, 50);
	public final Color FOCUS_SECONDARY = new Color(0, 0, 255, 50);

	/* Properties */

	private String title;

	/* Script State */

	private Action action;
	private int nextMinRunEnergy;
	private boolean checkLocation;
	private Location currentLocation;
	private Tree[] currentTrees;
	private Tree currentTree;
	private Tree nextTree;
	private boolean approached;
	private int hatchet;

	private int nestsCollected;
	private int startExperience;
	private long startTime;
	private Set<Particle> particles;

	private boolean hoveredNext;
	private boolean targetedCurrent;

	/* User Selected Settings */

	private Location[] selectedLocs;
	private boolean powerChop;
	private boolean paintEffects;

	/* Overriding Methods */

	@Override
	public boolean onStart() {
		ScriptManifest properties = getClass().getAnnotation(ScriptManifest.class);
		SetupFrame frame = new SetupFrame(properties.version());
		while (frame.isVisible()) {
			sleep(500);
		}
		selectedLocs = frame.getSelectedLocations();

		if (selectedLocs == null)
			return false;

		powerChop = frame.isPowerChopSelected();
		paintEffects = frame.isPaintEffectsSelected();
		title = new StringBuilder().
				append(properties.name()).append(" by Jacmob v").
				append(properties.version()).toString();
		startExperience = 0;

		return true;
	}

	@Override
	public void onFinish() {

	}

	/* Implementing Methods */

	public int loop() {
		if (startTime == 0) {
			if (skills.getCurrentLevel(Skills.CONSTITUTION) > 1) {
				initializeState();
			} else {
				return 500;
			}
		}
		if (checkLocation) {
			if (skills.getCurrentLevel(Skills.CONSTITUTION) > 1) {
				loadLocation();
				checkLocation = false;
			} else {
				return 500;
			}
		}
		switch (action = getAction()) {
			case WALK_TO_TREES:
				walkTo(currentLocation.treeArea);
				break;
			case WALK_TO_BANK:
				walkTo(currentLocation.bankArea);
				break;
			case CHOP:
				if (currentTrees == null) {
					if (currentLocation == null) {
						currentTrees = getLocalTrees(AUTO_QUEUE);
					} else {
						currentTrees = getLocalTrees(currentLocation.treeType);
					}
				} else {
					updateTrees();
				}

				if (nextTree == null || !nextTree.isStanding()) {
					loadNextTree();
				}
				boolean newTree = false;
				if (currentTree == null || !currentTree.isStanding() && (
						nextTree.isStanding() || !approached)) {
					currentTree = nextTree;
					loadNextTree();
					newTree = true;
					approached = true;
				}
				if (!powerChop) {
					nest();
				}
				if (currentTree != null && (newTree || !isChopping())) {
					RSObject obj = getObject(currentTree);
					if (obj == null) {
						idle();
					} else if (currentTree.isStanding()) {
						approached = false;
						if (obj.isOnScreen()) {
							if (chopTree(obj)) {
								hoveredNext = false;
								targetedCurrent = false;
								sleep(1000);
							} else if (random(0, 5) == 0) {
								camera.turnToObject(obj, 10);
							}
						} else if (calc.distanceTo(obj.getLocation()) > 5) {
							if (!walking.walkTo(obj.getLocation()) && !getMyPlayer().isMoving()) {
								walking.walkTileMM(walking.getClosestTileOnMap(obj.getLocation()));
							}
							sleep(500);
						} else if (!targetedCurrent) {
							camera.turnToObject(obj, 10);
							targetedCurrent = true;
						} else {
							walking.walkTileOnScreen(obj.getLocation());
							sleep(500);
						}
					} else if (!targetedCurrent) {
						camera.turnToObject(obj, 10);
						targetedCurrent = true;
					} else if (calc.distanceTo(obj.getLocation()) > 4) {
						walking.walkTileOnScreen(obj.getLocation());
						sleep(500);
					} else {
						idle();
					}
				} else {
					idle();
				}
				if (currentTree == null && nextTree == null && currentLocation == null) {
					currentTrees = null;
				}
				if (!hoveredNext && random(0, 4) == 0 && nextTree != null) {
					RSObject obj = getObject(nextTree);
					if (obj != null) {
						if (!obj.isOnScreen()) {
							camera.turnToObject(obj, 10);
						} else if (random(0, 4) != 0) {
							obj.doHover();
						}
						hoveredNext = true;
					}
				}
				break;
			case DROP:
				if (currentLocation == null || currentLocation.treeArea == null) {
					currentTrees = null;
				}
				inventory.dropAllExcept(random(0, 2) == 0, AXE_IDS);
				break;
			case BANK:
				if (bank.isOpen()) {
					if (hatchet > 0 && !inventory.containsOneOf(AXE_IDS)) {
						bank.withdraw(hatchet, 1);
						sleep(2000);
						if (!inventory.containsOneOf(AXE_IDS)) {
							log.warning("Unable to withdraw hatchet.");
							stopScript(false);
						}
					}
					if (!inventory.containsOneOf(AXE_IDS)) {
						bank.depositAll();
					} else {
						if (hatchet == 0) {
							log.info("Detected hatchet in inventory. Script will withdraw if removed.");
						}
						hatchet = inventory.getItem(AXE_IDS).getID();
						bank.depositAllExcept(AXE_IDS);
					}
				} else if (bank.open()) {
					sleep(500);
				}
				break;
		}
		return random(500, 1000);
	}

	public void onRepaint(Graphics g) {
		if (startTime == 0) {
			return;
		}

		if (paintEffects) {

			// Scene

			if (action == Action.CHOP) {

				if (currentTree != null) {
					RSObject obj = objects.getTopAt(currentTree.location);
					RSModel model = obj == null ? null : obj.getModel();
					if (model != null) {
						g.setColor(FOCUS_PRIMARY);
						for (Polygon p : model.getTriangles()) {
							g.fillPolygon(p);
						}
						Point pt = model.getPoint();
						Particle pl = Particle.newParticle(pt.x, pt.y);
						pl.g = random(200, 255);
						particles.add(pl);
					}
				}

				if (nextTree != null) {
					RSObject obj = objects.getTopAt(nextTree.location);
					RSModel model = obj == null ? null : obj.getModel();
					if (model != null) {
						g.setColor(FOCUS_SECONDARY);
						for (Polygon p : model.getTriangles()) {
							g.fillPolygon(p);
						}
						Point pt = model.getPoint();
						Particle pl = Particle.newParticle(pt.x, pt.y);
						pl.b = random(200, 255);
						particles.add(pl);
					}
				}

				Iterator<Particle> i = particles.iterator();
				while (i.hasNext()) {
					Particle p = i.next();
					if (p.a <= 0 || p.x < 0) {
						i.remove();
					} else {
						g.setColor(new Color(p.r, p.g, p.b, p.a));
						g.fillOval(p.x - p.rad, p.y - p.rad, p.rad * 2, p.rad * 2);
						p.a -= 10;
						p.y += 1;
						p.x += p.v;
					}
				}

			}

		}

		// Text

		int x = 14, y = 26, vspace = 18;

		g.setColor(BG);
		g.fill3DRect(x - 7, y, 211, 25, true);

		g.setColor(DROP);
		g.drawString(title, x + 1, y += vspace);
		g.setColor(TEXT);
		g.drawString(title, x, y -= 1);

		g.setColor(BG);
		g.fill3DRect(x - 7, y += vspace, 211, 62, true);

		g.setColor(TEXT);
		g.drawString(Timer.format(System.currentTimeMillis() - startTime) + " - " + format(action), x, y += vspace);
		g.drawString("XP Gained: " + (skills.getCurrentExp(Skills.WOODCUTTING) - startExperience), x, y += vspace);
		g.drawString(powerChop ? "Dropping Logs" : ("Nests Collected: " + nestsCollected), x, y += vspace);
	}

	/* Defined Methods */

	private void walkTo(RSArea area) {
		if (area.contains(getMyPlayer().getLocation())) {
			return;
		}
		if (!walking.isRunEnabled() && walking.getEnergy() > nextMinRunEnergy) {
			nextMinRunEnergy = random(20, 50);
			walking.setRun(true);
		}
		RSTile dest = walking.getDestination();
		if (dest != null && getMyPlayer().isMoving() && calc.distanceTo(dest) > 8) {
			return;
		}
		if (walking.walkTileMM(walking.getClosestTileOnMap(area.getCentralTile()))) {
			sleep(500);
		}
	}

	private void nest() {
		RSGroundItem nest = groundItems.getNearest(NEST_IDS);
		int initialCount = inventory.getCount();
		if (nest != null && initialCount < 28) {
			if (!nest.isOnScreen())
				camera.turnToTile(nest.getLocation());
			if (nest.doAction("Take")) {
				for (int i = 0; i < 15; ++i) {
					sleep(100);
					if (getMyPlayer().isMoving()) {
						i = 0;
					}
					if (inventory.getCount() > initialCount) {
						nestsCollected++;
						return;
					}
				}
			}
		}
	}

	private void idle() {
		if (random(0, 50) == 0) {
			int rand2 = random(1, 3);
			for (int i = 0; i < rand2; i++) {
				mouse.move(random(100, 700), random(100, 500));
				sleep(random(200, 700));
			}
			mouse.move(random(0, 800), 647, 50, 100);
			sleep(random(100, 1500));
			mouse.move(random(75, 400), random(75, 400), 30);
			hoveredNext = false;
		}
		if (random(0, 50) == 0) {
			Point curPos = mouse.getLocation();
			mouse.move(random(0, 750), random(0, 500), 20);
			sleep(random(100, 300));
			mouse.move(curPos, 20, 20);
		}
		if (random(0, 50) == 0) {
			camera.setAngle(camera.getAngle() + random(-40, 40));
			hoveredNext = false;
		}
		if (random(0, 50) == 0) {
			if (random(0, 4) == 0) {
				camera.setPitch(random(50, 80));
			} else {
				camera.setPitch(true);
			}
			hoveredNext = false;
		}
	}

	private boolean chopTree(RSObject obj) {
		if (getMyPlayer().isMoving()) {
			for (int i = 0, len = random(2, 5); i < len; ++i) {
				mouse.move(obj.getModel().getPoint());
				sleep(random(20, 100));
			}
			return menu.doAction("Chop");
		} else {
			return obj.doAction("Chop");
		}
	}

	private boolean isChopping() {
		RSPlayer me = getMyPlayer();
		for (int i = 10; i > 0; --i) {
			if (me.getAnimation() != -1) {
				return true;
			}
			sleep(10);
		}
		return false;
	}

	private Action getAction() {
		if (inventory.isFull()) {
			if (powerChop)
				return Action.DROP;
			else if (inArea(currentLocation.bankArea))
				return Action.BANK;
			else
				return Action.WALK_TO_BANK;
		} else {
			if (currentLocation != null && !inArea(currentLocation.treeArea))
				return Action.WALK_TO_TREES;
			else
				return Action.CHOP;
		}
	}

	private boolean inArea(RSArea area) {
		if (currentLocation != null && area != null &&
				!area.contains(getMyPlayer().getLocation())) {
			RSTile dest = walking.getDestination();
			return dest != null && getMyPlayer().isMoving() &&
					area.contains(dest) && calc.distanceTo(dest) < 8;
		}
		return true;
	}

	private void initializeState() {
		nextMinRunEnergy = 20;
		checkLocation = true;
		nestsCollected = 0;
		hatchet = 0;
		startExperience = skills.getCurrentExp(Skills.WOODCUTTING);
		startTime = System.currentTimeMillis();
		particles = new HashSet<Particle>();
	}

	private void loadLocation() {
		if (selectedLocs.length == 0) {
			currentLocation = null;
		} else if (selectedLocs.length == 1) {
			currentLocation = selectedLocs[0];
		} else {
			for (int i = selectedLocs.length - 1; i >= 0; --i) {
				if (skills.getCurrentLevel(Skills.WOODCUTTING) >= selectedLocs[i].treeType.level &&
						(selectedLocs[i].bankArea != null || getLocalTrees(selectedLocs[i].treeType).length > 0)) {
					currentLocation = selectedLocs[i];
					break;
				}
			}
		}
	}

	private void loadNextTree() {
		Arrays.sort(currentTrees, new TreeComparator(getMyPlayer().getLocation()));
		if (currentTrees.length >= 2 && currentTrees[0].equals(currentTree)) {
			nextTree = currentTrees[1];
		} else if (currentTrees.length >= 1 && !currentTrees[0].equals(currentTree)) {
			nextTree = currentTrees[0];
		} else {
			nextTree = null;
		}
	}

	private void updateTrees() {
		for (Tree tree : currentTrees) {
			if (tree.isStanding() == tree.fallen) {
				if (tree.fallen) {
					tree.fallen = false;
				} else {
					tree.fallen = true;
					tree.fallTime = System.currentTimeMillis();
				}
			}
		}
	}

	private RSObject getObject(Tree tree) {
		return objects.getTopAt(tree.location, Objects.TYPE_INTERACTABLE);
	}

	private Tree[] getLocalTrees(TreeType[] types) {
		for (TreeType type : types) {
			if (skills.getCurrentLevel(Skills.WOODCUTTING) >= type.level) {
				Tree[] local = getLocalTrees(type);
				if (local.length > 0) {
					return local;
				}
			}
		}
		return null;
	}

	private Tree[] getLocalTrees(final TreeType type) {
		RSObject[] obj = objects.getAll(new Filter<RSObject>() {
			public boolean accept(RSObject o) {
				if (currentLocation == null || currentLocation.treeArea == null ||
						currentLocation.treeArea.contains(o.getLocation())) {
					int oid = o.getID();
					for (int id : type.ids) {
						if (oid == id) {
							return true;
						}
					}
					for (int id : type.stumps) {
						if (oid == id) {
							return true;
						}
					}
				}
				return false;
			}
		});
		int len = obj.length;
		Tree[] trees = new Tree[len];
		for (int i = 0; i < len; ++i) {
			trees[i] = new Tree(type, obj[i].getLocation());
		}
		return trees;
	}

	private String format(Object o) {
		String s = o.toString();
		s = s.trim().toLowerCase().replace('_', ' ');
		int i = -1;
		do {
			s = s.substring(0, i + 1) + s.substring(i + 1, i + 2).toUpperCase() + s.substring(i + 2);
			i++;
		} while ((i = s.indexOf(' ', i)) != -1);
		return s;
	}

	/* Classes */

	private class Tree {

		private final TreeType type;
		private final RSTile location;

		private long fallTime;
		private boolean fallen;

		public Tree(TreeType type, RSTile location) {
			this.type = type;
			this.location = location;
			this.fallTime = Long.MAX_VALUE - random(0, 1000);
			this.fallen = false;
		}

		public boolean isStanding() {
			RSObject obj = objects.getTopAt(location, Objects.TYPE_INTERACTABLE);
			if (obj != null) {
				int oid = obj.getID();
				for (int id : type.ids) {
					if (id == oid) {
						return true;
					}
				}
			}
			return false;
		}

		public String toString() {
			return type + "@" + location;
		}

	}

	private static class Particle {

		private int r, g, b, a, x, y, v, rad;

		public static Particle newParticle(int x, int y) {
			Particle p = new Particle();
			int random = (int) (Math.random() * 0xffffff);
			p.rad = ((random >> 1) & 1) + 1;
			p.v = (random & 3) - 1;
			p.x = x;
			p.y = y;
			p.r = random & 0xff;
			p.g = (random >> 8) & 0xff;
			p.b = (random >> 16) & 0xff;
			p.a = 200;
			return p;
		}

	}

	private class TreeComparator implements Comparator<Tree> {

		private RSTile loc;

		public TreeComparator(RSTile loc) {
			this.loc = loc;
		}

		public int compare(Tree first, Tree second) {
			if (first.isStanding() != second.isStanding()) {
				return first.isStanding() ? -1 : 1;
			}
			if (first.isStanding()) {
				return dist(loc, first.location) - dist(loc, second.location);
			} else {
				return (int) (first.fallTime - second.fallTime);
			}
		}

		private int dist(RSTile curr, RSTile dest) {
			return (curr.getX() - dest.getX()) * (curr.getX() - dest.getX()) + (curr.getY() - dest.getY()) * (curr.getY() - dest.getY());
		}

	}

	private static class Location {

		public final RSArea bankArea;
		public final RSArea treeArea;
		public final TreeType treeType;

		public Location(RSArea bankA, RSArea treeA, TreeType treeT) {
			this.bankArea = bankA;
			this.treeArea = treeA;
			this.treeType = treeT;
		}

	}

	private static class SetupFrame extends JFrame {

		public final String OPTION_OTHER = "Other...";
		public final String OPTION_AUTO_CHOP = "Auto Select";
		public final double SCRIPT_VERSION;

		private Location[] selectedLocs;

		private JLabel description;
		private JComboBox locationBox;
		private JComboBox typeBox;
		private JCheckBox powerChop;
		private JCheckBox paintEffects;
		private String[] typesModel;

		/**
		 * Each SelectableLocation represents an entry in the Locations
		 * combo box. The TreeArea array passed to the constructor will be
		 * loaded into the second ComboBox when the location is selected
		 * in the Locations combo box.
		 * <p/>
		 * If you want to add a location with more than one area with the
		 * same TreeType (e.g. Yews, Maples), then use the second constructor
		 * of TreeType to provide different names for the second combo box.
		 * <p/>
		 * RSArea(RSTile sw, RSTile ne) // South-west & north-east tiles.
		 * TreeArea(TreeType, area) // Default name such as 'Yews' used.
		 * TreeArea(TreeType, name, area) // name used instead of default.
		 * SelectableLocation(name, bankArea, treeArea[])
		 */
		private final SelectableLocation[] locations = new SelectableLocation[]{

				// Catherby
				new SelectableLocation("Catherby", new RSArea(new RSTile(2807, 3438), new RSTile(2811, 3441)), new TreeArea[]{
						new TreeArea(TreeType.YEW, new RSArea(new RSTile(2752, 3425), new RSTile(2768, 3435)))
				}),

				// Draynor Village
				new SelectableLocation("Draynor", new RSArea(new RSTile(3092, 3242), new RSTile(3094, 3245)), new TreeArea[]{
						new TreeArea(TreeType.WILLOW, new RSArea(new RSTile(3081, 3225), new RSTile(3091, 3239)))
				}),

				// Grand Exchange
				new SelectableLocation("Grand Exchange", new RSArea(new RSTile(3160, 3485), new RSTile(3169, 3494)), new TreeArea[]{
						new TreeArea(TreeType.YEW, new RSArea(new RSTile(3203, 3501), new RSTile(3224, 3505)))
				}),

				// Seers' Village
				new SelectableLocation("Seers' Village", new RSArea(new RSTile(2722, 3490), new RSTile(2729, 3493)), new TreeArea[]{
						new TreeArea(TreeType.MAPLE, new RSArea(new RSTile(2720, 3498), new RSTile(2734, 3503))),
						new TreeArea(TreeType.MAGIC, new RSArea(new RSTile(2689, 3422), new RSTile(2698, 3428)))
				}),

				// Power Chop
				new SelectableLocation(OPTION_OTHER, null, null)

		};

		public SetupFrame(double scriptVersion) {
			super("AIO Chopper");
			this.SCRIPT_VERSION = scriptVersion;
			setMinimumSize(new Dimension(400, 250));
			setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
			getContentPane().setLayout(new BorderLayout());
			createChildren();
			pack();
			setLocationRelativeTo(getOwner());
			setVisible(true);
		}

		public Location[] getSelectedLocations() {
			return selectedLocs;
		}

		public boolean isPowerChopSelected() {
			return powerChop.isSelected();
		}

		public boolean isPaintEffectsSelected() {
			return paintEffects.isSelected();
		}

		private void createChildren() {
			Container content = getContentPane();

			// ----- JFrame (this) ----- //

			/*JLabel titleLabel = new JLabel("AIO Chopper", SwingConstants.CENTER);
						titleLabel.setFont(titleLabel.getFont().deriveFont(titleLabel.getFont().getStyle() | Font.BOLD, 20f));
						content.add(titleLabel, BorderLayout.NORTH);*/

			JPanel buttonBar = new JPanel(new GridBagLayout());
			buttonBar.setBorder(new EmptyBorder(0, 12, 5, 0));
			((GridBagLayout) buttonBar.getLayout()).columnWidths = new int[]{0, 85, 80};
			((GridBagLayout) buttonBar.getLayout()).columnWeights = new double[]{1.0, 0.0, 0.0};

			description = new JLabel();
			description.setFont(new Font("Arial", Font.BOLD, 10));
			description.setForeground(new Color(100, 100, 100));
			buttonBar.add(description, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
					GridBagConstraints.CENTER, GridBagConstraints.BOTH,
					new Insets(0, 0, 0, 0), 0, 0));

			JButton okButton = new JButton("Start");
			buttonBar.add(okButton, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0,
					GridBagConstraints.CENTER, GridBagConstraints.BOTH,
					new Insets(0, 0, 0, 0), 0, 0));

			JButton cancelButton = new JButton("Cancel");
			buttonBar.add(cancelButton, new GridBagConstraints(2, 0, 1, 1, 0.0, 0.0,
					GridBagConstraints.CENTER, GridBagConstraints.BOTH,
					new Insets(0, 0, 0, 0), 0, 0));

			okButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					readSettings();
					dispose();
				}
			});

			cancelButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					dispose();
				}
			});

			content.add(buttonBar, BorderLayout.SOUTH);

			// ----- JTabbedPane (Tabs) ----- //

			JTabbedPane tabPane = new JTabbedPane();

			content.add(tabPane);

			// ----- JPanel (settings) ----- //

			GridBagLayout settingsLayout = new GridBagLayout();
			settingsLayout.columnWidths = new int[]{0, 130};

			JPanel settingsPanel = new JPanel(settingsLayout);

			typesModel = getTypesModel(locations[0]);

			JLabel typeLabel = new JLabel("Tree Type:", JLabel.RIGHT);
			typeBox = new JComboBox(new DefaultComboBoxModel(typesModel));

			JLabel locLabel = new JLabel("Location:", JLabel.RIGHT);
			locationBox = new JComboBox(new DefaultComboBoxModel(locations));

			settingsPanel.add(locLabel, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
					GridBagConstraints.CENTER, GridBagConstraints.BOTH,
					new Insets(0, 0, 5, 5), 0, 0));
			settingsPanel.add(locationBox, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0,
					GridBagConstraints.CENTER, GridBagConstraints.BOTH,
					new Insets(0, 0, 5, 0), 0, 0));
			settingsPanel.add(typeLabel, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0,
					GridBagConstraints.CENTER, GridBagConstraints.BOTH,
					new Insets(0, 0, 5, 5), 0, 0));
			settingsPanel.add(typeBox, new GridBagConstraints(1, 1, 1, 1, 0.0, 0.0,
					GridBagConstraints.CENTER, GridBagConstraints.BOTH,
					new Insets(0, 0, 5, 0), 0, 0));

			powerChop = new JCheckBox("Power Chop");

			settingsPanel.add(powerChop, new GridBagConstraints(1, 2, 1, 1, 0.0, 0.0,
					GridBagConstraints.CENTER, GridBagConstraints.BOTH,
					new Insets(0, 0, 0, 0), 0, 0));

			paintEffects = new JCheckBox("Paint Effects");

			settingsPanel.add(paintEffects, new GridBagConstraints(1, 3, 1, 1, 0.0, 0.0,
					GridBagConstraints.CENTER, GridBagConstraints.BOTH,
					new Insets(0, 0, 0, 0), 0, 0));

			locationBox.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					if (locationBox.getSelectedItem().toString().equals(OPTION_OTHER)) {
						TreeType[] types = TreeType.values();
						typesModel = new String[types.length + 1];
						for (int i = 0; i < types.length; ++i)
							typesModel[i] = formatString(types[i].toString());
						typesModel[typesModel.length - 1] = OPTION_AUTO_CHOP;
						powerChop.setEnabled(false);
						powerChop.setSelected(true);
					} else {
						typesModel = getTypesModel(locations[locationBox.getSelectedIndex()]);
						if (!powerChop.isEnabled()) {
							powerChop.setEnabled(true);
							powerChop.setSelected(false);
						}
					}
					typeBox.setModel(new DefaultComboBoxModel(typesModel));
					updateDescription();
				}
			});


			typeBox.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					updateDescription();
				}
			});

			tabPane.add("Settings", settingsPanel);

			// ----- JPanel (updates) ----- //

			JPanel updatesPanel = new JPanel(new BorderLayout());

			JToolBar updatesBar = new JToolBar();
			updatesBar.setFloatable(false);
			updatesBar.setEnabled(false);

			final JLabel updateLabel = new JLabel("Version: " + SCRIPT_VERSION);
			final JButton updateButton = new JButton("No Update Available");
			final JTextPane updatesPane = new JTextPane();

			updateButton.setFont(updateButton.getFont().deriveFont(updateButton.getFont().getStyle() | Font.BOLD));
			updatesPane.setEditable(false);
			updatesPane.setContentType("text/html");
			updatesPane.setText("<html><body style='padding:5px; font-family: Arial; font-size: 9px;'>" +
					"<strong>v1.1</strong><br />Paint updated; effects optional.<br />Inventory hatchet support.<br />Preemptive movement updated." +
					"<br /><strong>v1.0</strong><br />More features and locations will come with bot updates.</body></html>");

			updatesBar.add(updateLabel);
			updatesBar.addSeparator();
			updatesBar.add(updateButton);

			new Thread(new Runnable() {
				public void run() {
					double onlineVersion = SCRIPT_VERSION;
					if (onlineVersion > SCRIPT_VERSION) {
						updateButton.setText("Update To Version " + onlineVersion);
						updateLabel.setForeground(new Color(150, 0, 0));
					} else {
						updateLabel.setForeground(new Color(0, 150, 0));
						updateButton.setEnabled(false);
					}
				}
			}).start();

			updatesPanel.add(updatesBar, BorderLayout.NORTH);
			updatesPanel.add(updatesPane, BorderLayout.CENTER);

			tabPane.add("Updates", updatesPanel);

		}

		private void readSettings() {
			int sType = typeBox.getSelectedIndex();
			SelectableLocation sLoc = (SelectableLocation) locationBox.getSelectedItem();

			if (sLoc.treeAreas == null) {
				TreeType[] types = TreeType.values();
				if (sType == types.length) {
					selectedLocs = new Location[0];
				} else {
					selectedLocs = new Location[]{new Location(null, null, types[sType])};
				}
			} else if (sType == sLoc.treeAreas.length) {
				selectedLocs = new Location[sType];
				for (int i = 0; i < selectedLocs.length; ++i)
					selectedLocs[i] = new Location(sLoc.bankArea, sLoc.treeAreas[i].area, sLoc.treeAreas[i].type);
			} else {
				selectedLocs = new Location[]{new Location(sLoc.bankArea, sLoc.treeAreas[sType].area, sLoc.treeAreas[sType].type)};
			}

		}

		private void updateDescription() {
			String sType = typeBox.getSelectedItem().toString();
			if (locationBox.getSelectedItem().toString().equals(OPTION_OTHER)) {
				if (sType.equals(OPTION_AUTO_CHOP)) {
					description.setText("Chop the best nearby trees for your level (up to willow).");
				} else {
					description.setText("Power chop nearby trees.");
				}
			} else if (sType.equals(OPTION_AUTO_CHOP)) {
				description.setText("Chop the best tree type for your level.");
			} else {
				description.setText("");
			}
		}

		private String[] getTypesModel(SelectableLocation location) {
			String[] types;
			if (location.treeAreas.length > 1) {
				types = new String[location.treeAreas.length + 1];
				for (int i = 0; i < location.treeAreas.length; ++i)
					types[i] = location.treeAreas[i].treeTypeName;
				types[types.length - 1] = OPTION_AUTO_CHOP;
			} else {
				types = new String[location.treeAreas.length];
				for (int i = 0; i < location.treeAreas.length; ++i)
					types[i] = location.treeAreas[i].treeTypeName;
			}
			return types;
		}

		private class SelectableLocation {

			public final String name;
			public final RSArea bankArea;
			public final TreeArea[] treeAreas;

			public SelectableLocation(String name, RSArea bankArea, TreeArea[] treeAreas) {
				this.name = name;
				this.bankArea = bankArea;
				this.treeAreas = treeAreas;
			}

			@Override
			public String toString() {
				return name;
			}

		}

		private class TreeArea {

			public final RSArea area;
			public final TreeType type;
			public final String treeTypeName;

			public TreeArea(TreeType type, RSArea area) {
				this(type, formatString(type.toString()), area);
			}

			public TreeArea(TreeType type, String name, RSArea area) {
				this.area = area;
				this.type = type;
				this.treeTypeName = name;
			}

		}


		private String formatString(String s) {
			s = s.trim().toLowerCase().replace('_', ' ');
			int i = -1;
			do {
				s = s.substring(0, i + 1) + s.substring(i + 1, i + 2).toUpperCase() + s.substring(i + 2);
				i++;
			} while ((i = s.indexOf(' ', i)) != -1);
			return s.concat("s");
		}

	}

}