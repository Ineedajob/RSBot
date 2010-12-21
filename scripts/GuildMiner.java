import org.rsbot.event.listeners.PaintListener;
import org.rsbot.script.Script;
import org.rsbot.script.ScriptManifest;
import org.rsbot.script.util.Filter;
import org.rsbot.script.util.Timer;
import org.rsbot.script.wrappers.RSArea;
import org.rsbot.script.wrappers.RSModel;
import org.rsbot.script.wrappers.RSObject;
import org.rsbot.script.wrappers.RSPlayer;
import org.rsbot.script.wrappers.RSTile;

import java.awt.*;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * @author Jacmob
 */
@ScriptManifest(name = "GuildMiner", authors = "Jacmob", keywords = "Mining", version = 1.3,
		description = "Mining guild coal miner.")
public class GuildMiner extends Script implements PaintListener {

	public static interface GameConstants {

		RSArea BANK_AREA = new RSArea(3009, 3355, 3018, 3358);

		RSArea ENTRANCE_AREA = new RSArea(3015, 3336, 3024, 3342);

		RSArea EXIT_AREA = new RSArea(3017, 9736, 3022, 9742);

		RSArea MINE_AREA = new RSArea(3016, 9730, 3055, 9756);

		int ENTRANCE_LADDER = 2113;

		int EXIT_LADDER = 6226;

		int COAL_ORE = 453;

		int[] COAL_ROCKS = {5770, 5771, 5772};

		int[] EMPTY_ROCKS = {5763, 5764, 5765};

		int[] PICKAXES = {1265, 1267, 1269, 1296, 1273, 1271, 1275, 15259};

	}

	public static interface Strategy {

		public void execute();

		public boolean isValid();

	}

	public class GuildBankingStrategy implements Strategy {

		private int counted;

		public void execute() {
			if (GameConstants.BANK_AREA.contains(getMyPlayer().getLocation())) {
				if (bank.isOpen()) {
					int count = inventory.getCount(GameConstants.COAL_ORE);
					if (count == 28 || !inventory.containsOneOf(GameConstants.PICKAXES)) {
						bank.depositAll();
					} else {
						if (pickaxe == 0) {
							log.info("Detected pickaxe in inventory. Script will withdraw if removed.");
						}
						pickaxe = inventory.getItem(GameConstants.PICKAXES).getID();
						bank.depositAllExcept(GameConstants.PICKAXES);
					}
					banked += count - counted;
					counted = count;
					sleep(400);
					if (inventory.containsOneOf(GameConstants.COAL_ORE)) {
						sleep(400);
					}
					if (pickaxe > 0 && !inventory.containsOneOf(GameConstants.PICKAXES)) {
						bank.withdraw(pickaxe, 1);
						sleep(2000);
						if (!inventory.containsOneOf(GameConstants.PICKAXES)) {
							log.warning("Unable to withdraw pickaxe.");
							stopScript(false);
						}
					}
				} else {
					counted = 0;
					bank.open();
				}
			} else if (GameConstants.MINE_AREA.contains(getMyPlayer().getLocation())) {
				RSObject ladder = objects.getNearest(GameConstants.EXIT_LADDER);
				if (ladder != null && ladder.isOnScreen()) {
					highlight = ladder.getModel();
					if (ladder.doAction("Climb")) {
						sleep(1000);
						highlight = null;
					}
				} else {
					walking.walkTileMM(walking.getClosestTileOnMap(GameConstants.EXIT_AREA.getCentralTile()));
					sleep(random(1000, 2000));
				}
			} else {
				if (!walking.isRunEnabled() && walking.getEnergy() > 20) {
					walking.setRun(true);
					sleep(500);
				}
				RSTile dest = walking.getDestination();
				if (dest == null || !GameConstants.BANK_AREA.contains(dest)) {
					RSTile c = GameConstants.BANK_AREA.getCentralTile();
					if (!walking.walkTo(c) && !getMyPlayer().isMoving()) {
						walking.walkTileMM(walking.getClosestTileOnMap(c));
					}
					sleep(random(1000, 2000));
				}
			}
		}

		public boolean isValid() {
			return !canMine();
		}
	}

	public class BasicMiningStrategy implements Strategy {

		public static final int DIST_EXPONENT = 2;
		public static final int MAX_OTHER_DIST = 10;
		public static final int MY_PLAYER_WEIGHT = 2;

		private RSArea area;
		private int[] rock_ids;
		private int[] empty_ids;
		private RSTile[] rock_tiles;
		private RSObject last;
		private RSObject over;
		private int fail;

		public BasicMiningStrategy(final RSArea area, final int[] empty_ids, final int[] rock_ids) {
			this.area = area;
			this.rock_ids = rock_ids;
			this.empty_ids = empty_ids;
		}

		public void execute() {
			RSObject[] rocks = getNearestRocks();
			if (rocks.length > 0) {
				RSObject rock = rocks[0];
				if (accept(rock)) {
					highlight = rock.getModel();
					last = rock;
					if (fail > 5) {
						camera.turnToObject(rock, 20);
					}
					if (rock.isOnScreen()) {
						if (rock.doAction("Mine")) {
							fail = 0;
							sleep(500);
						} else {
							++fail;
						}
					} else if (calc.distanceTo(rock) < 8) {
						walking.walkTileOnScreen(rock.getLocation());
						sleep(500);
					} else if (walking.walkTo(rock.getLocation()) ||
							walking.walkTileMM(walking.getClosestTileOnMap(rock.getLocation()))) {
						sleep(500);
					}
				} else if (random(0, 4) == 0 && rocks.length > 1 && !rocks[1].equals(over)) {
					over = rocks[1];
					mouse.move(rocks[1].getModel().getPoint());
				} else {
					idle();
				}
			}
		}

		public boolean isValid() {
			// is able to mine
			return area.contains(getMyPlayer().getLocation());
		}

		private RSObject[] getNearestRocks() {
			List<RSObject> rocks = getRocks();
			if (rocks.size() == 0) {
				return new RSObject[0];
			}
			final RSPlayer me = getMyPlayer();
			RSPlayer[] nearby = players.getAll(new Filter<RSPlayer>() {
				public boolean accept(RSPlayer player) {
					return !player.equals(me);
				}
			});
			int lowest_cost = 999999, next_cost = 999999;
			int lowest_ptr = 0, next_ptr = 0;
			double max = Math.pow(MAX_OTHER_DIST, DIST_EXPONENT);
			for (int i = 0; i < rocks.size(); ++i) {
				RSObject rock = rocks.get(i);
				RSTile loc = rock.getLocation();
				int cost = (int) Math.pow(calc.distanceTo(loc), DIST_EXPONENT) * MY_PLAYER_WEIGHT;
				for (RSPlayer player : nearby) {
					double dist = calc.distanceBetween(player.getLocation(), loc);
					if (dist < MAX_OTHER_DIST) {
						cost += max / Math.pow(dist, DIST_EXPONENT);
					}
				}
				if (cost < lowest_cost) {
					next_cost = lowest_cost;
					next_ptr = lowest_ptr;
					lowest_cost = cost;
					lowest_ptr = i;
				}
			}
			if (next_cost == 999999) {
				return new RSObject[]{rocks.get(lowest_ptr)};
			}
			RSObject[] nearest = new RSObject[]{rocks.get(lowest_ptr), rocks.get(next_ptr)};
			if (nearest[1] != null && nearest[1].equals(last)) {
				RSObject temp = nearest[0];
				nearest[0] = nearest[1];
				nearest[1] = temp;
			}
			if (nearest[0] == null) {
				return new RSObject[0];
			}
			return nearest;
		}

		private List<RSObject> getRocks() {
			if (rock_tiles == null) { // objects.getAll each exec would be too expensive
				RSObject[] rocks = objects.getAll(new Filter<RSObject>() {
					public boolean accept(RSObject o) {
						if (area.contains(o.getLocation())) {
							int oid = o.getID();
							for (int id : rock_ids) {
								if (id == oid) {
									return true;
								}
							}
							for (int id : empty_ids) {
								if (id == oid) {
									return true;
								}
							}
						}
						return false;
					}
				});
				if (rocks.length > 0) {
					rock_tiles = new RSTile[rocks.length];
					for (int i = 0, rocksLength = rocks.length; i < rocksLength; i++) {
						rock_tiles[i] = rocks[i].getLocation();
					}
				} else {
					return new ArrayList<RSObject>(0);
				}
			}
			// loop appropriate tiles only since rock tiles don't change
			ArrayList<RSObject> rocks = new ArrayList<RSObject>();
			for (RSTile t : rock_tiles) {
				RSObject obj = objects.getTopAt(t);
				if (obj != null) {
					int oid = obj.getID();
					for (int id : rock_ids) {
						if (id == oid) {
							rocks.add(obj);
							break;
						}
					}
				}
			}
			return rocks;
		}

		private boolean accept(RSObject rock) {
			if (last != null && (rock.equals(last) ||
					objects.getTopAt(last.getLocation()).getID() == last.getID())) {
				for (int i = 1; ; ++i) {
					if (!getMyPlayer().isIdle()) {
						return false;
					}
					if (i == 10) {
						break;
					}
					sleep(50);
				}
			}
			return true;
		}

	}

	public class GuildEntranceStrategy implements Strategy {

		public void execute() {
			if (GameConstants.ENTRANCE_AREA.contains(getMyPlayer().getLocation())) {
				RSObject ladder = objects.getNearest(GameConstants.ENTRANCE_LADDER);
				if (ladder != null) {
					if (ladder.isOnScreen()) {
						if (ladder.doAction("Climb-down")) {
							sleep(2000);
						}
					} else {
						walking.walkTileOnScreen(ladder.getLocation());
						sleep(500);
					}
				}
			} else {
				RSTile dest = walking.getDestination();
				if (dest == null || !GameConstants.ENTRANCE_AREA.contains(dest)) {
					RSTile c = GameConstants.ENTRANCE_AREA.getCentralTile();
					if (!walking.walkTo(c) && !getMyPlayer().isMoving()) {
						walking.walkTileMM(walking.getClosestTileOnMap(c));
					}
					sleep(random(1000, 2000));
				}
			}
		}

		public boolean isValid() {
			// player was able to mine and not in mine
			return true;
		}
	}

	public static final Color HIGHLIGHT_COLOR = new Color(0, 255, 0, 100);
	public static final Color MINE_COLOR = new Color(0, 255, 0, 150);
	public static final Color FALADOR_COLOR = new Color(0, 0, 0, 200);

	private List<Strategy> strategies = new LinkedList<Strategy>();
	private RSModel highlight;
	private long start_time;
	private int pickaxe = 0;
	private int banked = 0;

	public boolean onStart() {
		strategies.add(new GuildBankingStrategy());
		strategies.add(new BasicMiningStrategy(GameConstants.MINE_AREA,
				GameConstants.EMPTY_ROCKS, GameConstants.COAL_ROCKS));
		strategies.add(new GuildEntranceStrategy());
		mouse.setSpeed(7);
		start_time = System.currentTimeMillis();
		return true;
	}

	public int loop() {
		for (Strategy strategy : strategies) {
			if (strategy.isValid()) {
				strategy.execute();
				return random(150, 300);
			}
		}
		return -1;
	}

	public void onRepaint(Graphics g) {
		RSModel m = highlight;
		if (m != null) {
			g.setColor(HIGHLIGHT_COLOR);
			for (Polygon p : m.getTriangles()) {
				g.fillPolygon(p);
			}
		}
		if (GameConstants.MINE_AREA.contains(getMyPlayer().getLocation())) {
			g.setColor(MINE_COLOR);
		} else {
			g.setColor(FALADOR_COLOR);
		}
		g.drawString("GuildMiner by Jacmob", 20, 55);
		g.drawString(Timer.format(System.currentTimeMillis() - start_time), 20, 75);
		g.drawString("Banked: " + banked, 20, 95);
	}

	private boolean canMine() {
		return !inventory.isFull() && (pickaxe == 0 || inventory.containsOneOf(GameConstants.PICKAXES));
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

}
