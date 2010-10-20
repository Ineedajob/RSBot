import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.event.KeyEvent;

import org.rsbot.event.listeners.PaintListener;
import org.rsbot.script.Script;
import org.rsbot.script.ScriptManifest;
import org.rsbot.script.methods.Skills;
import org.rsbot.script.wrappers.RSTile;

@ScriptManifest(authors = {"Jacmob", "Arbiter"}, keywords = "Agility", name = "Gnome Course", version = 2.0, description = "Standard gnome course. Eats common food.")
public class GnomeCourse extends Script implements PaintListener {

	public static final int[] FOOD = new int[]{
			333, 385, 379, 285, 373, 365, 7946, 361, 397, 391, 1963, 329, 2118};
	public static final int[] ENERGY_POTIONS = new int[]{
			3014, 3012, 3010, 3008, 3022, 3020, 3018, 3016};

	public static final Color BG = new Color(123, 123, 123, 100);
	public static final Color GREEN = new Color(0, 200, 0, 255);
	public static final Color GREENBAR = new Color(0, 255, 0, 150);
	public static final Color RED = new Color(255, 0, 0, 150);

	public static final RSArea AREA_ON_LOG = new RSArea(new RSTile(2474, 3430), new RSTile(2474, 3435), 0);
	public static final RSArea AREA_NET = new RSArea(new RSTile(2470, 3425), new RSTile(2478, 3429), 0);
	public static final RSArea AREA_BRANCH = new RSArea(new RSTile(2471, 3422), new RSTile(2476, 3424), 1);
	public static final RSArea AREA_ROPE = new RSArea(new RSTile(2472, 3418), new RSTile(2477, 3421), 2);
	public static final RSArea AREA_BRANCH_DOWN = new RSArea(new RSTile(2483, 3418), new RSTile(2488, 3421), 2);
	public static final RSArea AREA_NET_END = new RSArea(new RSTile(2481, 3418), new RSTile(2490, 3426), 0);
	public static final RSArea AREA_PIPE = new RSArea(new RSTile(2481, 3427), new RSTile(2489, 3431), 0);

	public static final Obstacle OBSTACLE_LOG = new Obstacle(2474, 3435, -10, "Walk-across", new Obstacle.PassedListener() {
		public void onPassed(GnomeCourse ctx) {
			ctx.camera.turnToTile(OBSTACLE_NET);
		}
	});
	public static final Obstacle OBSTACLE_NET = new Obstacle(2474, 3425, 50, "Climb-over");
	public static final Obstacle OBSTACLE_BRANCH = new Obstacle(2473, 3422, 120, "Climb");
	public static final Obstacle OBSTACLE_ROPE = new Obstacle(2478, 3420, 0, "Walk-on");
	public static final Obstacle OBSTACLE_BRANCH_DOWN = new Obstacle(2486, 3419, 60, "Climb-down", new Obstacle.PassedListener() {
		public void onPassed(GnomeCourse ctx) {
			ctx.turner.setTarget(OBSTACLE_NET_END);
		}
	});
	public static final Obstacle OBSTACLE_NET_END = new Obstacle(2486, 3426, 100, "Climb-over");
	public static final Obstacle OBSTACLE_PIPE = new Obstacle(2483, 3431, 60, "Squeeze-through", new Obstacle.PassedListener() {
		public void onPassed(GnomeCourse ctx) {
			ctx.turner.setTarget(OBSTACLE_LOG);
		}
	});

	private CameraTurner turner = new CameraTurner();
	private int eatingHealth = random(10, 20);
	private int drinkingEnergy = random(20, 40);
	private int laps;
	private int startXp = -1;
	private long startTime;

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
				if (obstacle.doAction(this)) {
					obstacle.onPassed(this);
					waitForChange(obstacle, 5000);
				} else if (!obstacle.isOnScreen(this)) {
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

			g.setColor(BG);
			if (runSeconds != 0) {
				g.fill3DRect(8, 25, 210, 164, true);
			} else {
				g.fill3DRect(8, 25, 210, 123, true);
			}

			g.setColor(GREEN);
			g.drawString("GnomeCourse v2.0", x, y += 20);
			g.drawString("GnomeCourse v2.0", x, y);
			g.drawString("Runtime: " + getFormattedTime(
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
		return true;
	}

	@Override
	public void onFinish() {
		turner.stop();
		log("Gained " + (skills.getCurrentExp(Skills.AGILITY) - startXp)
				+ " XP (" + (skills.getRealLevel(Skills.AGILITY) -
				Skills.getLevelAt(startXp)) + " levels) in "
				+ getFormattedTime(System.currentTimeMillis() - startTime) + ".");
	}

	private String getFormattedTime(final long timeMillis) {
		long millis = timeMillis;
		final long seconds2 = millis / 1000;
		final long hours = millis / (1000 * 60 * 60);
		millis -= hours * 1000 * 60 * 60;
		final long minutes = millis / (1000 * 60);
		millis -= minutes * 1000 * 60;
		final long seconds = millis / 1000;
		String hoursString = "";
		String minutesString = "";
		String secondsString = seconds + "";
		String type = "seconds";

		if (minutes > 0) {
			minutesString = minutes + ":";
			type = "minutes";
		} else if (hours > 0 && seconds2 > 0) {
			minutesString = "0:";
		}
		if (hours > 0) {
			hoursString = hours + ":";
			type = "hours";
		}
		if (minutes < 10 && !type.equals("seconds")) {
			minutesString = "0" + minutesString;
		}
		if (hours < 10 && type.equals("hours")) {
			hoursString = "0" + hoursString;
		}
		if (seconds < 10 && !type.equals("seconds")) {
			secondsString = "0" + secondsString;
		}
		return hoursString + minutesString + secondsString + " " + type;
	}

	class CameraTurner implements Runnable {

		private final Object targetLock = new Object();

		private volatile boolean running;
		private Obstacle target;

		public void run() {
			running = true;
			while (running) {
				synchronized (targetLock) {
					if (target != null && !calc.tileOnScreen(target)) {
						char key = KeyEvent.VK_LEFT;
						keyboard.pressKey(key);
						int i = 60;
						while (!calc.tileOnScreen(target) && --i >= 0) {
							GnomeCourse.this.sleep(50);
						}
						GnomeCourse.this.sleep(random(150, 300));
						keyboard.releaseKey(key);
						if (i >= 0 && !calc.tileOnScreen(target)) {
							key = KeyEvent.VK_LEFT;
							i = 20;
							while (!calc.tileOnScreen(target) && --i >= 0) {
								GnomeCourse.this.sleep(50);
							}
							keyboard.releaseKey(key);
						}
						target = null;
					}
				}
				GnomeCourse.this.sleep(100);
			}
		}

		public void setTarget(Obstacle target) {
			synchronized (targetLock) {
				this.target = target;
			}
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
			Point p = ctx.calc.tileToScreen(this, clickHeight);
			if (p.x != -1) {
				ctx.mouse.move(p, 5, 5);
				ctx.game.sleep(ctx.game.random(50, 300));
				return ctx.menu.doAction(action);
			}
			return false;
		}

		public boolean isOnScreen(GnomeCourse ctx) {
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