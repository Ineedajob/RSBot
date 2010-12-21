import org.rsbot.event.listeners.PaintListener;
import org.rsbot.script.Script;
import org.rsbot.script.ScriptManifest;
import org.rsbot.script.util.Timer;
import org.rsbot.script.wrappers.RSArea;
import org.rsbot.script.wrappers.RSModel;
import org.rsbot.script.wrappers.RSNPC;
import org.rsbot.script.wrappers.RSObject;
import org.rsbot.script.wrappers.RSTile;

import java.awt.*;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Jacmob
 */
@ScriptManifest(name = "AutoEssence", authors = "Jacmob", keywords = "Mining", version = 1.3,
		description = "Varrock and Yanille essence miner.")
public class AutoEssence extends Script implements PaintListener {

	public static interface Constants {

		RSArea VARROCK_BANK_AREA = new RSArea(3250, 3419, 3257, 3423);

		RSArea YANILLE_BANK_AREA = new RSArea(2609, 3095, 2613, 3089);

		RSArea MINE_AREA = new RSArea(2870, 4790, 2950, 4870);

		RSArea AUBURY_AREA = new RSArea(new RSTile[]{
				new RSTile(3252, 3404),
				new RSTile(3253, 3404),
				new RSTile(3255, 3401),
				new RSTile(3253, 3399),
				new RSTile(3252, 3399)
		});

		RSArea WIZARD_TOWER_AREA = new RSArea(new RSTile[]{
				new RSTile(2597, 3089),
				new RSTile(2597, 3087),
				new RSTile(2592, 3082),
				new RSTile(2589, 3083),
				new RSTile(2585, 3086),
				new RSTile(2585, 3089),
				new RSTile(2589, 3093),
				new RSTile(2592, 3093)
		});

		RSTile VARROCK_DOOR_TILE = new RSTile(3253, 3398);

		int VARROCK_OPEN_DOOR = 24381;

		RSTile WIZARD_TOWER_DOOR_TILE = new RSTile(2598, 3087);

		int[] WIZARD_TOWER_DOORS = {1600, 1601};

		int AUBURY = 553;

		int DISTENTOR = 462;

		int ROCK = 2491;

		int PORTAL = 2492;

		int RUNE_ESSENCE = 1436;

		int PURE_ESSENCE = 7936;

		int[] ESSENCES = {RUNE_ESSENCE, PURE_ESSENCE};

		int[] PICKAXES = {1265, 1267, 1269, 1296, 1273, 1271, 1275, 15259};

		Color PLAYER_FILL_COLOR = new Color(0, 255, 0, 50);

		Color AREA_FILL_COLOR = new Color(0, 0, 255, 100);

		Color TEXT_COLOR = new Color(255, 255, 255, 255);

		Color MINE_TEXT_COLOR = new Color(0, 0, 0, 255);

	}

	public static abstract class Action {

		public abstract void process();

		public abstract boolean isValid();

		public abstract String getDesc();

		public void complete() {
		}

		public void paint(Graphics g) {
		}

	}

	public class Bank extends Action {

		private RSArea area;

		public Bank(RSArea area) {
			this.area = area;
		}

		public void process() {
			if (bank.isOpen()) {
				if (inventory.getCount(Constants.ESSENCES) == 28 ||
						!inventory.containsOneOf(Constants.PICKAXES)) {
					bank.depositAll();
				} else {
					if (pickaxe == 0) {
						log.info("Detected pickaxe in inventory. Script will withdraw if removed.");
					}
					pickaxe = inventory.getItem(Constants.PICKAXES).getID();
					bank.depositAllExcept(Constants.PICKAXES);
				}
				sleep(400);
				if (inventory.containsOneOf(Constants.ESSENCES)) {
					sleep(400);
				}
				if (pickaxe > 0 && !inventory.containsOneOf(Constants.PICKAXES)) {
					bank.withdraw(pickaxe, 1);
					sleep(2000);
					if (!inventory.containsOneOf(Constants.PICKAXES)) {
						log.warning("Unable to withdraw pickaxe.");
						stopScript(false);
					}
				}
			} else {
				bank.open();
			}
		}

		public boolean isValid() {
			return !canMine() && area.contains(getMyPlayer().getLocation());
		}

		public String getDesc() {
			return "Banking";
		}

	}

	public abstract class NPCAction extends Action {

		private int id;
		private String action;

		public NPCAction(int id, String action) {
			this.id = id;
			this.action = action;
		}

		public void process() {
			RSNPC npc = npcs.getNearest(id);
			if (npc != null) {
				if (!npc.isOnScreen()) {
					camera.turnToCharacter(npc, 10);
				} else if (npc.doAction(action)) {
					sleep(1000);
					if (!getMyPlayer().isIdle()) {
						sleep(2000);
					}
					sleep(1000);
				}
			}
		}

		public void paint(Graphics g) {
			RSNPC npc = npcs.getNearest(id);
			if (npc != null) {
				RSModel model = npc.getModel();
				if (model != null) {
					g.setColor(Constants.PLAYER_FILL_COLOR);
					for (Polygon p : model.getTriangles()) {
						g.fillPolygon(p);
					}
				}
			}
		}
	}

	public abstract class ObjectAction extends Action {

		private int id;
		private String action;
		private int fails;

		public ObjectAction(int id, String action) {
			this.id = id;
			this.action = action;
		}

		public void process() {
			RSObject obj = objects.getNearest(id);
			if (obj != null) {
				if (obj.isOnScreen()) {
					if (obj.doAction(action)) {
						sleep(1000);
						if (getMyPlayer().isMoving()) {
							sleep(2000);
						}
					} else if (fails > 5) {
						camera.turnToObject(obj, 10);
						fails = 0;
					} else {
						++fails;
					}
				} else if (!getMyPlayer().isMoving() && (walking.walkTo(obj.getLocation()) ||
						walking.walkTileMM(walking.getClosestTileOnMap(obj.getLocation())))) {
					sleep(random(1000, 1800));
				}
			}
		}

		public void complete() {
			fails = 0;
		}

		public void paint(Graphics g) {
			RSObject obj = objects.getNearest(id);
			if (obj != null) {
				RSModel model = obj.getModel();
				if (model != null) {
					g.setColor(Constants.PLAYER_FILL_COLOR);
					for (Polygon p : model.getTriangles()) {
						g.fillPolygon(p);
					}
				}
			}
		}
	}

	public abstract class WalkToArea extends Action {

		private RSArea dest;
		private String name;
		private RSTile last;

		public WalkToArea(RSArea dest, String name) {
			this.dest = dest;
			this.name = name;
		}

		protected abstract boolean isTargetValid();

		public void process() {
			if (!walking.isRunEnabled() && walking.getEnergy() > 20) {
				walking.setRun(true);
				sleep(500);
			}
			RSTile tile = dest.getCentralTile();
			if (last == null || getMyPlayer().isIdle() || (calc.distanceTo(last) < 10 && !dest.contains(last))) {
				if (calc.tileOnMap(tile)) {
					walking.walkTileMM(tile);
				} else if (!walking.walkTo(tile)) {
					walking.walkTileOnScreen(calc.getTileOnScreen(tile));
				}
				last = walking.getDestination();
				sleep(random(1000, 1800));
			} else {
				idle();
			}
		}

		public boolean isValid() {
			return isTargetValid() && !dest.contains(getMyPlayer().getLocation());
		}

		public void complete() {
			last = null;
		}

		public void paint(Graphics g) {
			g.setColor(Constants.AREA_FILL_COLOR);
			for (RSTile t : dest.getTileArray()) {
				Point pn = calc.tileToScreen(t, 0, 0, 0);
				Point px = calc.tileToScreen(t, 1, 0, 0);
				Point py = calc.tileToScreen(t, 0, 1, 0);
				Point pxy = calc.tileToScreen(t, 1, 1, 0);
				if (pn.x > -1 && px.x > -1 && py.x > -1 && pxy.x > -1) {
					g.fillPolygon(new int[]{py.x, pxy.x, px.x, pn.x},
							new int[]{py.y, pxy.y, px.y, pn.y}, 4);
				}
			}
		}

		public String getDesc() {
			return "Walking to " + name + ".";
		}

	}

	private Set<Action> actions;
	private long startTime;
	private Action action;
	private int pickaxe = 0;
	private int mined = 0;

	public boolean onStart() {
		actions = new HashSet<Action>();

		// Varrock

		actions.add(new WalkToArea(Constants.VARROCK_BANK_AREA, "Varrock east bank") {
			protected boolean isTargetValid() {
				return !canMine() && inVarrock();
			}

			public void process() {
				RSObject obj = objects.getNearest(Constants.VARROCK_OPEN_DOOR);
				if (obj != null && calc.distanceBetween(obj.getLocation(), Constants.VARROCK_DOOR_TILE) < 2) {
					if (obj.isOnScreen()) {
						if (obj.doAction("Open")) {
							sleep(random(1000, 2000));
						}
					} else if (!Constants.VARROCK_DOOR_TILE.equals(walking.getDestination())) {
						walking.walkTo(Constants.VARROCK_DOOR_TILE);
					}
				} else {
					super.process();
				}
			}
		});

		actions.add(new WalkToArea(Constants.AUBURY_AREA, "Aubury") {

			protected boolean isTargetValid() {
				return canMine() && inVarrock();
			}

			public void process() {
				RSObject obj = objects.getNearest(Constants.VARROCK_OPEN_DOOR);
				if (obj != null && calc.distanceBetween(obj.getLocation(), Constants.VARROCK_DOOR_TILE) < 2) {
					if (obj.isOnScreen()) {
						if (obj.doAction("Open")) {
							sleep(random(1000, 2000));
						}
					} else if (!Constants.VARROCK_DOOR_TILE.equals(walking.getDestination())) {
						walking.walkTo(Constants.VARROCK_DOOR_TILE);
					}
				} else {
					super.process();
				}
			}

		});

		actions.add(new NPCAction(Constants.AUBURY, "Teleport") {
			public String getDesc() {
				return "Teleporing to mine.";
			}

			public boolean isValid() {
				return canMine() && inShop();
			}
		});

		actions.add(new Bank(Constants.VARROCK_BANK_AREA));

		// Yanille

		actions.add(new WalkToArea(Constants.YANILLE_BANK_AREA, "Yanille bank") {
			protected boolean isTargetValid() {
				return !canMine() && inYanille();
			}

			public void process() {
				if (inTower()) {
					if (calc.distanceTo(Constants.WIZARD_TOWER_DOOR_TILE) > 5) {
						if (!getMyPlayer().isMoving()) {
							walking.walkTileOnScreen(Constants.WIZARD_TOWER_DOOR_TILE);
							sleep(500);
						}
					} else {
						RSObject door = objects.getNearest(Constants.WIZARD_TOWER_DOORS);
						if (door != null && calc.distanceTo(door.getLocation()) < 5) {
							if (door.isOnScreen() && door.doAction("Open")) {
								sleep(1000);
								if (inTower()) {
									sleep(1000);
								}
							} else {
								camera.turnToObject(door);
							}
						}
					}
				} else {
					super.process();
				}
			}
		});

		actions.add(new WalkToArea(Constants.WIZARD_TOWER_AREA, "Wizard's Tower") {

			protected boolean isTargetValid() {
				return canMine() && inYanille();
			}

			public void process() {
				if (calc.distanceTo(Constants.WIZARD_TOWER_DOOR_TILE) < 10) {
					RSObject obj = objects.getNearest(Constants.WIZARD_TOWER_DOORS);
					if (obj != null && calc.distanceTo(obj.getLocation()) < 10) {
						if (obj.isOnScreen() && obj.doAction("Open")) {
							sleep(1500);
							if (getMyPlayer().isMoving()) {
								sleep(1000);
							}
							if (!inTower()) {
								sleep(1000);
							}
						} else if (!getMyPlayer().isMoving()) {
							if (walking.getDestination() == null) {
								walking.walkTileOnScreen(obj.getLocation());
							} else {
								camera.turnToObject(obj);
							}
						}
					} else if (!getMyPlayer().isMoving()) {
						walking.walkTileMM(Constants.WIZARD_TOWER_DOOR_TILE);
					}
				} else if (!getMyPlayer().isMoving()) {
					walking.walkTileMM(Constants.WIZARD_TOWER_DOOR_TILE);
				}
			}

		});

		actions.add(new NPCAction(Constants.DISTENTOR, "Teleport") {
			public String getDesc() {
				return "Teleporing to mine.";
			}

			public boolean isValid() {
				return canMine() && inTower();
			}
		});

		actions.add(new Bank(Constants.YANILLE_BANK_AREA));

		// Global

		actions.add(new ObjectAction(Constants.PORTAL, "Enter") {
			public String getDesc() {
				return "Leaving mine.";
			}

			public boolean isValid() {
				return !canMine() && inMine();
			}
		});
		actions.add(new ObjectAction(Constants.ROCK, "Mine") {

			private int ticks = 0;
			private int last = 0;

			public String getDesc() {
				return "Mining rock.";
			}

			public void process() {
				int count = inventory.getCount(Constants.ESSENCES);
				mined += count - last;
				last = count;
				if (getMyPlayer().getAnimation() == -1) {
					if (ticks == 0) {
						super.process();
					} else {
						--ticks;
					}
				} else {
					if (ticks < 5) {
						++ticks;
					}
					idle();
				}
			}

			public void complete() {
				ticks = 0;
				last = 0;
			}

			public boolean isValid() {
				return canMine() && inMine();
			}

		});

		startTime = System.currentTimeMillis();
		return true;
	}

	public int loop() {
		mouse.setSpeed(random(6, 8));
		if (action != null) {
			if (action.isValid()) {
				action.process();
			} else {
				action.complete();
				action = null;
			}
		} else {
			for (Action a : actions) {
				if (a.isValid()) {
					action = a;
					break;
				}
			}
		}
		return random(300, 500);
	}

	public void onRepaint(Graphics g) {
		if (action != null) {
			action.paint(g);
			if (inMine()) {
				g.setColor(Constants.MINE_TEXT_COLOR);
			} else {
				g.setColor(Constants.TEXT_COLOR);
			}
			g.drawString("AutoEssence by Jacmob", 20, 55);
			g.drawString(Timer.format(System.currentTimeMillis() - startTime), 20, 75);
			g.drawString(action.getDesc(), 20, 95);
			g.drawString("Mined: " + mined, 20, 115);
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
		}
		if (random(0, 50) == 0) {
			Point curPos = mouse.getLocation();
			mouse.move(random(0, 750), random(0, 500), 20);
			sleep(random(100, 300));
			mouse.move(curPos, 20, 20);
		}
		if (random(0, 50) == 0) {
			int angle = camera.getAngle() + random(-40, 40);
			if (angle < 0) {
				angle += 359;
			}
			if (angle > 359) {
				angle -= 359;
			}
			camera.setAngle(angle);
		}
		if (random(0, 50) == 0) {
			if (random(0, 4) == 0) {
				camera.setPitch(random(50, 80));
			} else {
				camera.setPitch(true);
			}
		}
	}

	private boolean inVarrock() {
		return calc.distanceTo(Constants.VARROCK_BANK_AREA.getCentralTile()) < 200;
	}

	private boolean inYanille() {
		return calc.distanceTo(Constants.YANILLE_BANK_AREA.getCentralTile()) < 200;
	}

	private boolean inShop() {
		return Constants.AUBURY_AREA.contains(getMyPlayer().getLocation());
	}

	private boolean inTower() {
		return Constants.WIZARD_TOWER_AREA.contains(getMyPlayer().getLocation());
	}

	private boolean inMine() {
		return Constants.MINE_AREA.contains(getMyPlayer().getLocation());
	}

	private boolean canMine() {
		return !inventory.isFull();
	}

}
