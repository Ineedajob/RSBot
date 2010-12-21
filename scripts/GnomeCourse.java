import org.rsbot.event.listeners.PaintListener;
import org.rsbot.script.Script;
import org.rsbot.script.ScriptManifest;
import org.rsbot.script.methods.Skills;
import org.rsbot.script.util.Timer;
import org.rsbot.script.wrappers.RSModel;
import org.rsbot.script.wrappers.RSObject;
import org.rsbot.script.wrappers.RSTile;

import java.awt.*;
import java.awt.event.KeyEvent;

@ScriptManifest(authors = "Jacmob", keywords = "Agility", name = "Gnome Course", version = 2.2, description = "Standard gnome course. Eats common food.")
public class GnomeCourse extends Script implements PaintListener {

	public static final int[] FOOD = new int[]{
			1895, 1893, 1891, 4293, 2142, 291, 2140, 3228, 9980,
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
	public static final int[] ENERGY_POTIONS = new int[]{
			3014, 3012, 3010, 3008, 3022, 3020, 3018, 3016};

	public static final Color BG = new Color(123, 123, 123, 100);
	public static final Color GREEN = new Color(0, 220, 0, 255);
	public static final Color GREENBAR = new Color(0, 255, 0, 150);
	public static final Color RED = new Color(255, 0, 0, 150);

	public static final RSArea AREA_ON_LOG = new RSArea(new RSTile(2474, 3430), new RSTile(2474, 3435), 0);
	public static final RSArea AREA_NET = new RSArea(new RSTile(2470, 3425), new RSTile(2478, 3429), 0);
	public static final RSArea AREA_BRANCH = new RSArea(new RSTile(2471, 3422), new RSTile(2476, 3424), 1);
	public static final RSArea AREA_ROPE = new RSArea(new RSTile(2472, 3418), new RSTile(2477, 3421), 2);
	public static final RSArea AREA_BRANCH_DOWN = new RSArea(new RSTile(2483, 3418), new RSTile(2488, 3421), 2);
	public static final RSArea AREA_NET_END = new RSArea(new RSTile(2481, 3418), new RSTile(2490, 3426), 0);
	public static final RSArea AREA_PIPE = new RSArea(new RSTile(2481, 3427), new RSTile(2489, 3431), 0);

	public static final Obstacle OBSTACLE_LOG = new Obstacle(2474, 3435, 10, "Walk-across", new Obstacle.PassedListener() {
		public void onPassed(GnomeCourse ctx) {
			ctx.camera.turnToTile(OBSTACLE_NET);
		}
	});
	public static final Obstacle OBSTACLE_NET = new Obstacle(2474, 3425, -50, "Climb-over");
	public static final Obstacle OBSTACLE_BRANCH = new Obstacle(2473, 3422, 120, "Climb", new Obstacle.PassedListener() {
		public void onPassed(GnomeCourse ctx) {
			ctx.turner.setTarget(OBSTACLE_ROPE);
		}
	});
	public static final Obstacle OBSTACLE_ROPE = new Obstacle(2478, 3420, 0, "Walk-on", new Obstacle.PassedListener() {
		public void onPassed(GnomeCourse ctx) {
			if (ctx.random(0, 10) != 0) {
				ctx.camera.setPitch(false);
				ctx.sleep(ctx.random(10, 100));
			}
			if (ctx.random(0, 5) != 0) {
				ctx.turner.setTarget(OBSTACLE_BRANCH_DOWN);
			}
		}
	});
	public static final Obstacle OBSTACLE_BRANCH_DOWN = new Obstacle(2486, 3419, -60, "Climb-down", new Obstacle.PassedListener() {
		public void onPassed(GnomeCourse ctx) {
			ctx.turner.setTarget(OBSTACLE_NET_END);
		}
	});
	public static final Obstacle OBSTACLE_NET_END = new Obstacle(2486, 3426, -100, "Climb-over", new Obstacle.PassedListener() {
		public void onPassed(GnomeCourse ctx) {
			ctx.turner.setTarget(OBSTACLE_PIPE);
		}
	});
	public static final Obstacle OBSTACLE_PIPE = new Obstacle(2483, 3431, -60, "Squeeze-through", new Obstacle.PassedListener() {
		public void onPassed(GnomeCourse ctx) {
			ctx.turner.setTarget(OBSTACLE_LOG);
		}
	});

	private CameraTurner turner = new CameraTurner();
	private int eatingHealth = random(10, 20);
	private int drinkingEnergy = random(20, 40);
	private int laps;
	private int tries;
	private int startXp = -1;
	private long startTime;
	private Obstacle last;
	private RSModel model;
	private Color color;

	private void eat() {
		if (inventory.getCount(GnomeCourse.FOOD) >= 1
				&& walking.getEnergy() <= eatingHealth) {
			eatingHealth = random(10, 20);
			for (int element : GnomeCourse.FOOD) {
				if (inventory.getCount(element) == 0) {
					continue;
				}
				log.info("Eating.");
				inventory.getItem(element).doAction("Eat");
				sleep(random(500, 800));
				break;
			}
		}
	}

	private void drink() {
		if (inventory.getCount(GnomeCourse.ENERGY_POTIONS) >= 1
				&& walking.getEnergy() <= drinkingEnergy) {
			drinkingEnergy = random(20, 40);
			for (int element : GnomeCourse.ENERGY_POTIONS) {
				if (inventory.getCount(element) == 0) {
					continue;
				}
				log.info("Drinking energy potion.");
				inventory.getItem(element).doAction("Drink");
				sleep(random(500, 800));
				break;
			}
		}
	}

	@Override
	public int loop() {
		mouse.setSpeed(random(6, 8));
		if (random(0, 100) == 0) {
			camera.setPitch(true);
		}
		if (startXp == -1) {
			if (game.isLoggedIn() && skills.getRealLevel(Skills.AGILITY) > 1) {
				setInitialState();
			}
			return 100;
		}
		if (getMyPlayer().getAnimation() == -1) {
			eat();
			drink();
			Obstacle obstacle = getObstacle();
			if (obstacle != null) {
				if (obstacle == last) {
					++tries;
				} else {
					tries = 0;
					last = obstacle;
				}
				if (obstacle.doAction(this)) {
					obstacle.onPassed(this);
					waitForChange(obstacle, 2000);
				} else if (!obstacle.isOnScreen(this) || tries > 3) {
					if (calc.distanceTo(obstacle) > 5) {
						walking.walkTileOnScreen(obstacle);
						sleep(500);
					} else {
						turner.setTarget(obstacle);
					}
				}
			}
		}
		return random(100, 200);
	}

	public void waitForChange(Obstacle current, int timeout) {
		long end = System.currentTimeMillis() + timeout;
		while (current.equals(getObstacle()) &&
				System.currentTimeMillis() < end) {
			if (!getMyPlayer().isIdle()) {
				end += 60;
			}
			sleep(100);
		}
	}

	public Obstacle getObstacle() {
		RSTile loc = getMyPlayer().getLocation();
		int plane = game.getPlane();
		if (AREA_NET.contains(loc, plane)) {
			return OBSTACLE_NET;
		} else if (AREA_BRANCH.contains(loc, plane)) {
			return OBSTACLE_BRANCH;
		} else if (AREA_ROPE.contains(loc, plane)) {
			return OBSTACLE_ROPE;
		} else if (AREA_BRANCH_DOWN.contains(loc, plane)) {
			return OBSTACLE_BRANCH_DOWN;
		} else if (AREA_NET_END.contains(loc, plane)) {
			return OBSTACLE_NET_END;
		} else if (AREA_PIPE.contains(loc, plane)) {
			return OBSTACLE_PIPE;
		} else if (plane == 0 && !AREA_ON_LOG.contains(loc, plane)) {
			return OBSTACLE_LOG;
		}
		return null;
	}

	public void setInitialState() {
		laps = 0;
		startXp = skills.getCurrentExp(Skills.AGILITY);
		startTime = System.currentTimeMillis();
	}

	public void onRepaint(final Graphics g) {
		if (game.isLoggedIn() && skills.getRealLevel(Skills.AGILITY) > 1) {
			int x = 13;
			int y = 21;

			int levelsGained = skills.getRealLevel(Skills.AGILITY)
					- Skills.getLevelAt(startXp);
			long runSeconds = (System.currentTimeMillis() - startTime) / 1000;

			RSModel model = this.model;
			if (model != null) {
				g.setColor(color);
				for (Polygon polygon : model.getTriangles()) {
					g.drawPolygon(polygon);
				}
			}

			g.setColor(BG);
			if (runSeconds != 0) {
				g.fill3DRect(8, 25, 210, 164, true);
			} else {
				g.fill3DRect(8, 25, 210, 123, true);
			}

			g.setColor(GREEN);
			g.drawString("GnomeCourse v2.2", x, y += 20);
			g.drawString("GnomeCourse v2.2", x, y);
			g.drawString("Runtime: " + Timer.format(
					System.currentTimeMillis() - startTime) + ".", x, y += 20);

			if (levelsGained == 1) {
				g.drawString("Gained: " + (skills.getCurrentExp(
						Skills.AGILITY) - startXp)
						+ " XP (" + levelsGained + " lvl)", x, y += 20);
			} else {
				g.drawString("Gained: " + (skills.getCurrentExp(
						Skills.AGILITY) - startXp)
						+ " XP (" + levelsGained + " lvls)", x, y += 20);
			}

			if (runSeconds > 0) {
				g.drawString("Averaging: " + (skills.getCurrentExp(
						Skills.AGILITY) - startXp)
						* 3600 / runSeconds + " XP/hr", x, y += 20);
			}

			g.drawString("Laps done: " + laps, x, y += 20);
			g.drawString("Current level: "
					+ skills.getRealLevel(Skills.AGILITY), x,
					y += 20);
			g.drawString("Next level: "
					+ skills.getExpToNextLevel(Skills.AGILITY) + " XP",
					x, y += 20);
			if (runSeconds != 0) {
				g.setColor(RED);
				g.fill3DRect(x, y += 9, 200, 13, true);
				g.setColor(GREENBAR);
				g.fill3DRect(x, y, skills
						.getPercentToNextLevel(Skills.AGILITY) * 2, 13, true);
			}
		}
	}

	@Override
	public boolean onStart() {
		new Thread(turner).start();
		return true;
	}

	@Override
	public void onFinish() {
		turner.stop();
		log("Gained " + (skills.getCurrentExp(Skills.AGILITY) - startXp)
				+ " XP (" + (skills.getRealLevel(Skills.AGILITY) -
				Skills.getLevelAt(startXp)) + " levels) in "
				+ Timer.format(System.currentTimeMillis() - startTime) + ".");
	}

	class CameraTurner implements Runnable {

		private final Object targetLock = new Object();

		private boolean running;
		private volatile Obstacle target;

		public void run() {
			running = true;
			while (running) {
				synchronized (targetLock) {
					if (target != null) {
						char key = KeyEvent.VK_RIGHT;
						keyboard.pressKey(key);
						int i = 60;
						while (!target.isOnScreen(GnomeCourse.this) && --i >= 0) {
							GnomeCourse.this.sleep(50);
						}
						GnomeCourse.this.sleep(random(250, 350));
						keyboard.releaseKey(key);
						if (i >= 0 && !target.isOnScreen(GnomeCourse.this)) {
							key = KeyEvent.VK_RIGHT;
							i = 20;
							while (!target.isOnScreen(GnomeCourse.this) && --i >= 0) {
								GnomeCourse.this.sleep(50);
							}
							keyboard.releaseKey(key);
						}
					}
					target = null;
				}
				GnomeCourse.this.sleep(100);
			}
		}

		public void setTarget(Obstacle target) {
			synchronized (targetLock) {
				this.target = target;
			}
		}

		public Obstacle getTarget() {
			return target;
		}

		public void stop() {
			running = false;
		}

	}

	static class RSArea {

		private final int x, y, width, height, plane;

		public RSArea(RSTile sw, RSTile ne, int plane) {
			this.x = sw.getX();
			this.y = sw.getY();
			this.width = ne.getX() - sw.getX();
			this.height = ne.getY() - sw.getY();
			this.plane = plane;
		}

		public boolean contains(int x, int y, int plane) {
			return this.plane == plane &&
					(x >= this.x) &&
					(x <= this.x + this.width) &&
					(y >= this.y) &&
					(y <= this.y + this.height);
		}

		public boolean contains(RSTile tile, int plane) {
			return contains(tile.getX(), tile.getY(), plane);
		}

	}

	static class Obstacle extends RSTile {

		private int clickHeight;
		private String action;
		private PassedListener listener;

		public Obstacle(int x, int y, int clickHeight, String action) {
			this(x, y, clickHeight, action, null);
		}

		public Obstacle(int x, int y, int clickHeight,
						String action, PassedListener listener) {
			super(x, y);
			this.clickHeight = clickHeight;
			this.action = action;
			this.listener = listener;
		}

		public boolean doAction(GnomeCourse ctx) {
			while (ctx.turner.getTarget() != null) {
				ctx.sleep(ctx.random(50, 100));
			}
			RSObject o = ctx.objects.getTopAt(this);
			if (o != null && o.getModel() != null) {
				ctx.model = o.getModel();
				ctx.color = new Color(ctx.random(100, 255),
						ctx.random(100, 255),
						ctx.random(100, 255), 123);
				ctx.mouse.move(o.getModel().getPoint());
				ctx.sleep(ctx.random(5, 50));
				if (ctx.menu.contains(action) && (!ctx.getMyPlayer().isIdle() || ctx.turner.getTarget() != null)) {
					ctx.mouse.click(false);
					ctx.sleep(ctx.random(5, 50));
				}
				return ctx.menu.doAction(action);
			}
			ctx.model = null;
			Point p = ctx.calc.tileToScreen(this, clickHeight);
			if (p.x != -1) {
				ctx.mouse.move(p, 5, 5);
				ctx.game.sleep(ctx.game.random(50, 300));
				return ctx.menu.doAction(action);
			}
			return false;
		}

		public boolean isOnScreen(GnomeCourse ctx) {
			RSObject o = ctx.objects.getTopAt(this);
			if (o != null) {
				return o.isOnScreen();
			}
			return ctx.calc.tileToScreen(this).x >= 0;
		}

		public void onPassed(GnomeCourse ctx) {
			if (listener != null) {
				listener.onPassed(ctx);
			}
		}

		public static interface PassedListener {
			public void onPassed(GnomeCourse ctx);
		}

	}

}