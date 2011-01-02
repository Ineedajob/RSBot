import org.rsbot.event.listeners.PaintListener;
import org.rsbot.event.listeners.TextPaintListener;
import org.rsbot.script.Script;
import org.rsbot.script.ScriptManifest;
import org.rsbot.script.methods.Game;
import org.rsbot.script.methods.Walking;
import org.rsbot.script.wrappers.RSPath;
import org.rsbot.script.wrappers.RSTile;
import org.rsbot.util.StringUtil;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

/**
 * @author Jacmob
 */
@ScriptManifest(authors = {"Jacmob"}, keywords = "Development", name = "Map Explorer", version = 0.2, description = "Provides map info for developers.")
public class MapExplorer extends Script implements TextPaintListener, PaintListener, MouseListener {

	public static final Color TILE_CLEAR_FILL = new Color(0, 255, 0, 100);
	public static final Color TILE_BLOCKED_FILL = new Color(255, 0, 0, 100);
	public static final Color PATH_FILL = new Color(0, 0, 255, 100);

	public static final int WALL_NORTH_WEST = 0x1;
	public static final int WALL_NORTH = 0x2;
	public static final int WALL_NORTH_EAST = 0x4;
	public static final int WALL_EAST = 0x8;
	public static final int WALL_SOUTH_EAST = 0x10;
	public static final int WALL_SOUTH = 0x20;
	public static final int WALL_SOUTH_WEST = 0x40;
	public static final int WALL_WEST = 0x80;
	public static final int BLOCKED = 0x100;

//	public static final int BLOCK_1 = 128;
//	public static final int BLOCK_2 = 8;
//	public static final int BLOCK_3 = 2;
//	public static final int BLOCK_4 = 32;
//	public static final int BLOCK_5 = 1;
//	public static final int BLOCK_6 = 16;
//	public static final int BLOCK_7 = 64;
//	public static final int BLOCK_8 = 4;
//	public static final int BLOCK_9 = 130;
//
//	public static final int BLOCK_10 = 0x10000;
//	public static final int BLOCK_11 = 4096;
//	public static final int BLOCK_12 = 1024;
//	public static final int BLOCK_13 = 16384;
//	public static final int BLOCK_14 = 512;
//	public static final int BLOCK_15 = 2048;
//	public static final int BLOCK_16 = 8192;
//	public static final int BLOCK_17 = 32768;
//	public static final int BLOCK_18 = 0x10400;
//	public static final int BLOCK_19 = 5120;
//	public static final int BLOCK_20 = 20480;
//	public static final int BLOCK_21 = 0x14000;
//
//	public static final int BLOCK_22 = 0x400000;
//	public static final int BLOCK_23 = 0x4000000;
//	public static final int BLOCK_24 = 0x1000000;
//	public static final int BLOCK_25 = 0x10000000;
//	public static final int BLOCK_26 = 0x20800000;
//	public static final int BLOCK_27 = 0x2000000;
//	public static final int BLOCK_28 = 0x8000000;
//	public static final int BLOCK_29 = 0x2800000;
//	public static final int BLOCK_30 = 0x20000000;
//	public static final int BLOCK_31 = 0xA000000;
//	public static final int BLOCK_32 = 0x800000;
//	public static final int BLOCK_33 = 0x28000000;

	public static final RSTile[] EMPTY_PATH = new RSTile[0];

	private final AStar pf = new AStar();

	private volatile RSTile current = new RSTile(0, 0);
	private volatile RSTile[] path = EMPTY_PATH;

	RSPath walk_path;

	public boolean onStart() {
		walk_path = walking.getPath(new RSTile(3165, 3484));
		pf.init(game, walking);
		return true;
	}

	public int loop() {
		if (calc.distanceTo(current) > 100) {
			current = getMyPlayer().getLocation();
		}
		walk_path.traverse();
		return 100;
	}

	public void mouseClicked(MouseEvent e) {

	}

	public void mousePressed(MouseEvent e) {

	}

	public void mouseReleased(MouseEvent e) {
		Point p = e.getPoint();
		if (p.x > 0 && p.x < game.getWidth()) {
			RSTile tile = null;
			for (int x = 0; x < 104; x++) {
				for (int y = 0; y < 104; y++) {
					RSTile t = new RSTile(x + game.getBaseX(), y + game.getBaseY());
					Point s = calc.tileToScreen(t);
					if (s.x != -1 && s.y != -1) {
						if (tile == null) {
							tile = t;
						}
						if (calc.tileToScreen(tile).distance(p) > calc.tileToScreen(t).distance(p)) {
							tile = t;
						}
					}
				}
			}
			if (tile != null) {
				current = tile;
				long start = System.currentTimeMillis();
				RSTile[] tiles = pf.findPath(getMyPlayer().getLocation(), tile);
				if (tiles == null) {
					log.info("unreachable");
					path = EMPTY_PATH;
				} else {
					log.info("path calculated in " + (System.currentTimeMillis() - start) + "ms");
					path = tiles;
				}
			}
		}

	}

	public void mouseEntered(MouseEvent e) {

	}

	public void mouseExited(MouseEvent e) {

	}

	public void onRepaint(Graphics g) {
		((Graphics2D) g).setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		RSTile off = walking.getCollisionOffset(game.getPlane());
		int flags = walking.getCollisionFlags(game.getPlane())[current.getX() - game.getBaseX() - off.getX()][current.getY() - game.getBaseY() - off.getY()];
		if ((flags & BLOCKED) == 0) {
			highlight(g, current, TILE_CLEAR_FILL);
		} else {
			highlight(g, current, TILE_BLOCKED_FILL);
		}
		for (RSTile t : path) {
			if (!t.equals(current)) {
				highlight(g, t, PATH_FILL);
			}
		}
	}

	public int drawLine(Graphics g, int idx) {
		RSTile off = walking.getCollisionOffset(game.getPlane());
		int flags = walking.getCollisionFlags(game.getPlane())[current.getX() - game.getBaseX() - off.getX()][current.getY() - game.getBaseY() - off.getY()];
		StringUtil.drawLine(g, idx++, "Base: (" + game.getBaseX() + "," + game.getBaseY() + ")");
		StringUtil.drawLine(g, idx++, "Offset: " + off);
		StringUtil.drawLine(g, idx++, "Target: " + ((flags & BLOCKED) == 0 ? "Clear " : "[red]Blocked ") + current);
		StringUtil.drawLine(g, idx++, "Flags: B'" + getBinaryString(flags) + "' 0x" + Integer.toHexString(flags));
		drawBorderFlag(g, idx++, flags, "N", WALL_NORTH);
		drawBorderFlag(g, idx++, flags, "NE", WALL_NORTH_EAST);
		drawBorderFlag(g, idx++, flags, "E", WALL_EAST);
		drawBorderFlag(g, idx++, flags, "SE", WALL_SOUTH_EAST);
		drawBorderFlag(g, idx++, flags, "S", WALL_SOUTH);
		drawBorderFlag(g, idx++, flags, "SW", WALL_SOUTH_WEST);
		drawBorderFlag(g, idx++, flags, "W", WALL_WEST);
		drawBorderFlag(g, idx++, flags, "NW", WALL_NORTH_WEST);
		return idx;
	}

	private void drawBorderFlag(Graphics g, int idx, int flags, String label, int mask) {
		StringUtil.drawLine(g, idx, label + " Border: " + ((flags & mask) == 0 ? "Clear" : "[red]Blocked"));
	}

	private String getBinaryString(int flags) {
		String bin = Integer.toBinaryString(flags);
		StringBuilder b = new StringBuilder();
		int l = 32 - bin.length();
		for (int i = 0; i < l; ++i) {
			b.append('0');
		}
		b.append(bin);
		return b.toString();
	}

	private void highlight(Graphics g, RSTile t, Color fill) {
		Point pn = calc.tileToScreen(t, 0, 0, 0);
		Point px = calc.tileToScreen(t, 1, 0, 0);
		Point py = calc.tileToScreen(t, 0, 1, 0);
		Point pxy = calc.tileToScreen(t, 1, 1, 0);
		if (py.x != -1 && pxy.x != -1 && px.x != -1 && pn.x != -1) {
			g.setColor(fill);
			g.fillPolygon(new int[]{py.x, pxy.x, px.x, pn.x},
					new int[]{py.y, pxy.y, px.y, pn.y}, 4);
		}
	}

	/**
	 * A* impl using local rs collision map.
	 *
	 * @author Jacmob
	 */
	static class AStar {

		private static class Node {

			public int x, y;
			public Node prev;
			public double g, f;

			public Node(int x, int y) {
				this.x = x;
				this.y = y;
				g = f = 0;
			}

			@Override
			public int hashCode() {
				return (x << 4) | y;
			}

			@Override
			public boolean equals(Object o) {
				if (o instanceof Node) {
					Node n = (Node) o;
					return x == n.x && y == n.y;
				}
				return false;
			}

			@Override
			public String toString() {
				return "(" + x + "," + y + ")";
			}

			public RSTile toRSTile(int baseX, int baseY) {
				return new RSTile(x + baseX, y + baseY);
			}

		}

		private Walking walking;
		private Game game;

		private int[][] flags;
		private int off_x, off_y;

		public void init(Game game, Walking walking) {
			this.game = game;
			this.walking = walking;
		}

		public RSTile[] findPath(RSTile start, RSTile end) {
			int base_x = game.getBaseX(), base_y = game.getBaseY();
			int curr_x = start.getX() - base_x, curr_y = start.getY() - base_y;
			int dest_x = end.getX() - base_x, dest_y = end.getY() - base_y;

			// load client data
			flags = walking.getCollisionFlags(game.getPlane());
			RSTile offset = walking.getCollisionOffset(game.getPlane());
			off_x = offset.getX();
			off_y = offset.getY();

			// loaded region only
			if (flags == null || curr_x < 0 || curr_y < 0 ||
					curr_x >= flags.length || curr_y >= flags.length ||
					dest_x < 0 || dest_y < 0 ||
					dest_x >= flags.length || dest_y >= flags.length) {
				return null;
			}

			// structs
			HashSet<Node> open = new HashSet<Node>();
			HashSet<Node> closed = new HashSet<Node>();
			Node curr = new Node(curr_x, curr_y);
			Node dest = new Node(dest_x, dest_y);

			curr.f = heuristic(curr, dest);
			open.add(curr);

			// search
			while (!open.isEmpty()) {
				curr = lowest_f(open);
				if (curr.equals(dest)) {
					// reconstruct from pred tree
					return path(curr, base_x, base_y);
				}
				open.remove(curr);
				closed.add(curr);
				for (Node next : successors(curr)) {
					if (!closed.contains(next)) {
						double t = curr.g + dist(curr, next);
						boolean use_t = false;
						if (!open.contains(next)) {
							open.add(next);
							use_t = true;
						} else if (t < next.g) {
							use_t = true;
						}
						if (use_t) {
							next.prev = curr;
							next.g = t;
							next.f = t + heuristic(next, dest);
						}
					}
				}
			}

			// no path
			return null;
		}

		private double heuristic(Node start, Node end) {
			double dx = start.x - end.x;
			double dy = start.y - end.y;
			if (dx < 0) dx = -dx;
			if (dy < 0) dy = -dy;
			return dx < dy ? dy : dx;
			//double diagonal = dx > dy ? dy : dx;
			//double manhattan = dx + dy;
			//return 1.41421356 * diagonal + (manhattan - 2 * diagonal);
		}

		private double dist(Node start, Node end) {
			if (start.x != end.x && start.y != end.y) {
				return 1.41421356;
			} else {
				return 1.0;
			}
		}

		private Node lowest_f(Set<Node> open) {
			Node best = null;
			for (Node t : open) {
				if (best == null || t.f < best.f) {
					best = t;
				}
			}
			return best;
		}

		private RSTile[] path(Node end, int base_x, int base_y) {
			LinkedList<RSTile> path = new LinkedList<RSTile>();
			Node p = end;
			while (p != null) {
				path.addFirst(p.toRSTile(base_x, base_y));
				p = p.prev;
			}
			return path.toArray(new RSTile[path.size()]);
		}

		private java.util.List<Node> successors(Node t) {
			LinkedList<Node> tiles = new LinkedList<Node>();
			int x = t.x, y = t.y;
			int f_x = x - off_x, f_y = y - off_y;
			int here = flags[f_x][f_y];
			if (f_y > 0 && (here & WALL_SOUTH) == 0 && (flags[f_x][f_y - 1] & BLOCKED) == 0) {
				tiles.add(new Node(x, y - 1));
			}
			if (f_x > 0 && (here & WALL_WEST) == 0 && (flags[f_x - 1][f_y] & BLOCKED) == 0) {
				tiles.add(new Node(x - 1, y));
			}
			if (f_y < 103 && (here & WALL_NORTH) == 0 && (flags[f_x][f_y + 1] & BLOCKED) == 0) {
				tiles.add(new Node(x, y + 1));
			}
			if (f_x < 103 && (here & WALL_EAST) == 0 && (flags[f_x + 1][f_y] & BLOCKED) == 0) {
				tiles.add(new Node(x + 1, y));
			}
			if (f_x > 0 && f_y > 0 && (here & (WALL_SOUTH_WEST | WALL_SOUTH | WALL_WEST)) == 0
					&& (flags[f_x - 1][f_y - 1] & BLOCKED) == 0
					&& (flags[f_x][f_y - 1] & (BLOCKED | WALL_WEST)) == 0
					&& (flags[f_x - 1][f_y] & (BLOCKED | WALL_SOUTH)) == 0) {
				tiles.add(new Node(x - 1, y - 1));
			}
			if (f_x > 0 && f_y < 103 && (here & (WALL_NORTH_WEST | WALL_NORTH | WALL_WEST)) == 0
					&& (flags[f_x - 1][f_y + 1] & BLOCKED) == 0
					&& (flags[f_x][f_y + 1] & (BLOCKED | WALL_WEST)) == 0
					&& (flags[f_x - 1][f_y] & (BLOCKED | WALL_NORTH)) == 0) {
				tiles.add(new Node(x - 1, y + 1));
			}
			if (f_x < 103 && f_y > 0 && (here & (WALL_SOUTH_EAST | WALL_SOUTH | WALL_EAST)) == 0
					&& (flags[f_x + 1][f_y - 1] & BLOCKED) == 0
					&& (flags[f_x][f_y - 1] & (BLOCKED | WALL_EAST)) == 0
					&& (flags[f_x + 1][f_y] & (BLOCKED | WALL_SOUTH)) == 0) {
				tiles.add(new Node(x + 1, y - 1));
			}
			if (f_x > 0 && f_y < 103 && (here & (WALL_NORTH_EAST | WALL_NORTH | WALL_EAST)) == 0
					&& (flags[f_x + 1][f_y + 1] & BLOCKED) == 0
					&& (flags[f_x][f_y + 1] & (BLOCKED | WALL_EAST)) == 0
					&& (flags[f_x + 1][f_y] & (BLOCKED | WALL_NORTH)) == 0) {
				tiles.add(new Node(x + 1, y + 1));
			}
			return tiles;
		}

	}

}
