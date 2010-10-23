import org.rsbot.event.listeners.PaintListener;
import org.rsbot.script.Script;
import org.rsbot.script.ScriptManifest;
import org.rsbot.script.wrappers.*;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Polygon;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Jacmob
 */
@ScriptManifest(name = "AutoEssence", authors = "Jacmob", keywords = "Mining", version = 1.0,
	description = "Varrock essence miner.")
public class AutoEssence extends Script implements PaintListener {

	public static interface Constants {

		RSArea BANK_AREA = new RSArea(3250, 3419, 3257, 3423);

		RSArea MINE_AREA = new RSArea(2870, 4790, 2950, 4870);

		RSArea AUBURY_AREA = new RSArea(new RSTile[] {
				new RSTile(3252, 3404),
				new RSTile(3253, 3404),
				new RSTile(3255, 3401),
				new RSTile(3253, 3399),
				new RSTile(3252, 3399)
		});

		RSTile DOOR_TILE = new RSTile(3253, 3398);

		int ROCK = 2491;

		int PORTAL = 2492;

		int OPEN_DOOR = 24381;

		int AUBURY = 553;

		int RUNE_ESSENCE = 1436;

		int PURE_ESSENCE = 7936;

		int[] ESSENCES = {RUNE_ESSENCE, PURE_ESSENCE};

		int[] PICKAXES = {1265, 1267, 1269, 1296, 1273, 1271, 1275, 15259} ;

		Color PLAYER_FILL_COLOR = new Color(0, 255, 0, 50);

		Color AREA_FILL_COLOR = new Color(0, 0, 255, 100);

		Color TEXT_COLOR = new Color(255, 255, 255, 255);

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
				if (bank.getCount(Constants.ESSENCES) == 28) {
					bank.depositAll();
				} else {
					bank.depositAllExcept(Constants.PICKAXES);
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
			if (npc != null && npc.doAction(action)) {
				sleep(random(2500, 3300));
				if (!getMyPlayer().isIdle()) {
					sleep(500);
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
						sleep(random(2000, 3000));
						if (getMyPlayer().isMoving()) {
							sleep(1000);
						}
					} else if (fails > 5) {
						camera.turnToObject(obj, 10);
						fails = 0;
					} else {
						++fails;
					}
				} else if (!getMyPlayer().isMoving() && walking.walkTo(obj.getLocation())) {
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
			RSTile tile = dest.getCentralTile();
			if (last == null || getMyPlayer().isIdle() || (calc.distanceTo(last) < 10 && !dest.contains(last))) {
				if (walking.walkTo(tile)) {
					last = walking.getDestination();
					sleep(random(1000, 1800));
				}
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
					g.fillPolygon(new int[] { py.x, pxy.x, px.x, pn.x },
							new int[] { py.y, pxy.y, px.y, pn.y }, 4);
				}
			}
		}

		public String getDesc() {
			return "Walking to " + name + ".";
		}

	}

	private Set<Action> actions;
	private Action action;
	private boolean worn;
	private int mined;

	public boolean onStart() {
		actions = new HashSet<Action>();

		actions.add(new WalkToArea(Constants.BANK_AREA, "bank") {
			protected boolean isTargetValid() {
				return !canMine() && inVarrock() && !inBank();
			}

			public void process() {
				RSObject obj = objects.getNearest(Constants.OPEN_DOOR);
				if (obj != null && calc.distanceBetween(obj.getLocation(), Constants.DOOR_TILE) < 2
						&& obj.doAction("Open")) {
					sleep(random(1000, 2000));
				} else {
					super.process();
				}
			}
		});
		actions.add(new WalkToArea(Constants.AUBURY_AREA, "Aubury") {

			protected boolean isTargetValid() {
				return canMine() && inVarrock() && !inShop();
			}

			public void process() {
				RSObject obj = objects.getNearest(Constants.OPEN_DOOR);
				if (obj != null && calc.distanceBetween(obj.getLocation(), Constants.DOOR_TILE) < 2
						&& obj.doAction("Open")) {
					sleep(random(1000, 2000));
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
		actions.add(new Bank(Constants.BANK_AREA));

		return true;
	}

	public int loop() {
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
		return random(300, 600);
	}

	public void onRepaint(Graphics g) {
		if (action != null) {
			action.paint(g);
			g.setColor(Constants.TEXT_COLOR);
			g.drawString(action.getDesc(), 20, 55);
			g.drawString("Mined: " + mined, 20, 75);
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
	}

	private boolean inVarrock() {
		return calc.distanceTo(Constants.BANK_AREA.getCentralTile()) < 200;
	}

	private boolean inShop() {
		return Constants.AUBURY_AREA.contains(getMyPlayer().getLocation());
	}

	private boolean inBank() {
		return Constants.BANK_AREA.contains(getMyPlayer().getLocation());
	}

	private boolean inMine() {
		return Constants.MINE_AREA.contains(getMyPlayer().getLocation());
	}

	private boolean canMine() {
		return !inventory.isFull();
	}

}
