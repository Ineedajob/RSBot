package org.rsbot.script.wrappers;

import java.util.EnumSet;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.rsbot.script.methods.MethodContext;

public class RSAdvancedPath extends RSPath {

	public static final int WALL_NORTH_WEST = 0x1;
	public static final int WALL_NORTH = 0x2;
	public static final int WALL_NORTH_EAST = 0x4;
	public static final int WALL_EAST = 0x8;
	public static final int WALL_SOUTH_EAST = 0x10;
	public static final int WALL_SOUTH = 0x20;
	public static final int WALL_SOUTH_WEST = 0x40;
	public static final int WALL_WEST = 0x80;
	public static final int BLOCKED = 0x100;

	protected RSTile end;
	protected RSTile base;
	protected int[][][] flags;
	protected int[] offX, offY;
	private boolean ended;
	protected RSTile[] tiles;

	public RSAdvancedPath(MethodContext ctx, RSTile end) {
		super(ctx);
		this.end = end;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean traverse(EnumSet<TraversalOption> options) {
		int nIdx = getNextIndex();
		RSTile next = nIdx >= 0 ? tiles[nIdx] : null;
		if (next == null) {
			return false;
		}
		RSDoor door = methods.doors.getDoorAt(tiles[Math.min(nIdx + 1,
				tiles.length - 1)]);
		if (door != null && door.isOnScreen() && door.isClosed()) {
			return door.open();
			/*
			 * } else if (ladder != null && ladder.isOnScreen()) { return
			 * ladder.climb(dir);
			 */
		} else {
			if (next.equals(getEnd())) {
				if (methods.calc.distanceTo(next) <= 1
						|| (ended && methods.players.getMyPlayer().isMoving())
						|| next.equals(methods.walking.getDestination())) {
					return false;
				}
				ended = true;
			} else {
				ended = false;
			}
			if (options != null && options.contains(TraversalOption.HANDLE_RUN)
					&& !methods.walking.isRunEnabled()
					&& methods.walking.getEnergy() > 50) {
				methods.walking.setRun(true);
				methods.walking.sleep(300);
			}
			if (options != null
					&& options.contains(TraversalOption.SPACE_ACTIONS)) {
				RSTile dest = methods.walking.getDestination();
				if (dest != null && methods.players.getMyPlayer().isMoving()
						&& methods.calc.distanceTo(dest) > 5
						&& methods.calc.distanceBetween(next, dest) < 7) {
					return true;
				}
			}
			return methods.walking.walkTileMM(next, 0, 0);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isValid() {
		return getNext() != null
				&& !methods.players.getMyPlayer().getLocation()
						.equals(getEnd());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public RSTile getNext() {
		int index = getNextIndex();
		if (index >= 0)
			return tiles[index];
		return null;
	}

	/**
	 * Gets the index of the next tile
	 * 
	 * @return index of the next tile
	 */
	public int getNextIndex() {
		if (!recheckPath())
			return -1;
		if (tiles != null)
			for (int i = tiles.length - 1; i >= 0; --i) {
				if (tiles[i] != null)
					if (methods.calc.tileOnMap(tiles[i]))
						return getTileFor(i);
			}
		return -1;
	}

	/**
	 * Updates the path
	 * 
	 * @return is successful
	 */
	private boolean recheckPath() {
		if (!methods.game.getMapBase().equals(base)) {
			int[][] flags = methods.walking.getCollisionFlags(methods.game
					.getPlane());
			if (flags != null) {
				base = methods.game.getMapBase();
				RSTile start = methods.players.getMyPlayer().getLocation();
				tiles = findPath(start, end);
				if (tiles == null) {
					base = null;
					return false;
				}
			}
		}
		return true;
	}

	/**
	 * Gets the first reachable tile between your location and the end tile.
	 * 
	 * @param endIndex
	 * @return the index
	 */
	private int getTileFor(int endIndex) {
		int startIndex = getNearestIndex();
		for (int i = startIndex; i <= endIndex; i++)
			if (methods.doors.isDoorAt(tiles[i], false)
			/* || methods.ladders.isLadderAt(tiles[i]) */)
				return Math.max(0, i - 1);
		return endIndex;
	}

	/**
	 * Gets the nearest tile index to your location
	 * 
	 * @return index
	 */
	private int getNearestIndex() {
		if (!recheckPath())
			return -1;
		int bIdx = -1;
		double bLen = Integer.MAX_VALUE;
		for (int i = 0; i < tiles.length; i++) {
			double dist = methods.calc.distanceTo(tiles[i]);
			if (dist < bLen) {
				bIdx = i;
				bLen = dist;
			}
		}
		return bIdx;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public RSTile getStart() {
		return tiles != null && tiles.length > 0 ? tiles[0] : null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public RSTile getEnd() {
		return end;
	}

	/**
	 * Returns the methods.calculated RSTile array that is currently providing
	 * data to this RSDoorPath.
	 * 
	 * @return The current RSTile array; or <code>null</code>.
	 */
	public RSTile[] getCurrentTiles() {
		return tiles;
	}

	protected class Node {

		public int x, y, z;
		public Node prev;
		public double g, f;
		public boolean border;

		public Node(int x, int y, int z, boolean border) {
			this.border = border;
			this.x = x;
			this.y = y;
			this.z = z;
			g = f = 0;
		}

		public Node(int x, int y, int z) {
			this.x = x;
			this.y = y;
			this.z = z;
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
				return x == n.x && y == n.y && z == n.z;
			}
			return false;
		}

		@Override
		public String toString() {
			return "(" + x + "," + y + "," + z + ")";
		}

		public RSTile toRSTile(int baseX, int baseY) {
			return new RSTile(x + baseX, y + baseY, z);
		}

	}

	public RSTile[] findPath(RSTile start, RSTile end) {
		return findPath(start, end, false);
	}

	private void updateFlags() {
		flags = new int[4][][];
		offX = new int[4];
		offY = new int[4];
		for (int plane = 0; plane < 4; plane++) {
			flags[plane] = methods.walking.getCollisionFlags(plane);
			RSTile offset = methods.walking.getCollisionOffset(plane);
			offX[plane] = offset.getX();
			offY[plane] = offset.getY();
		}
	}

	private RSTile[] findPath(RSTile start, RSTile end, boolean remote) {
		int base_x = base.getX(), base_y = base.getY();
		int curr_x = start.getX() - base_x, curr_y = start.getY() - base_y, curr_z = start
				.getZ();
		int dest_x = end.getX() - base_x, dest_y = end.getY() - base_y, dest_z = end
				.getZ();

		// load client data
		updateFlags();

		// loaded region only
		if (flags == null || curr_x < 0 || curr_y < 0
				|| curr_x >= flags[curr_z].length
				|| curr_y >= flags[curr_z].length || dest_x < 0 || dest_y < 0
				|| dest_x >= flags[dest_z].length
				|| dest_y >= flags[dest_z].length) {
			return null;
		}

		// structs
		HashSet<Node> open = new HashSet<Node>();
		HashSet<Node> closed = new HashSet<Node>();
		Node curr = new Node(curr_x, curr_y, curr_z);
		Node dest = new Node(dest_x, dest_y, dest_z);

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
		if (!remote || methods.calc.distanceTo(end) < 10) {
			return null;
		}
		return findPath(start, pull(end));
	}

	private RSTile pull(RSTile tile) {
		RSTile p = methods.players.getMyPlayer().getLocation();
		int x = tile.getX(), y = tile.getY();
		if (p.getX() < x) {
			x -= 2;
		} else if (p.getX() > x) {
			x += 2;
		}
		if (p.getY() < y) {
			y -= 2;
		} else if (p.getY() > y) {
			y += 2;
		}
		return new RSTile(x, y);
	}

	private double heuristic(Node start, Node end) {
		double dx = start.x - end.x;
		double dy = start.y - end.y;
		double dz = start.z - end.z;
		if (dx < 0) {
			dx = -dx;
		}
		if (dy < 0) {
			dy = -dy;
		}
		if (dz < 0) {
			dz = -dz;
		}
		return dx < dy ? (dz < dy ? dy : dz) : (dz < dx ? dz : dz);
		// double diagonal = dx > dy ? dy : dx;
		// double manhattan = dx + dy;
		// return 1.41421356 * diagonal + (manhattan - 2 * diagonal);
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

	private List<Node> successors(Node t) {
		LinkedList<Node> tiles = new LinkedList<Node>();
		int x = t.x, y = t.y, z = t.z;
		int f_x = x - offX[z], f_y = y - offY[z];
		int here = flags[z][f_x][f_y];
		int upper = flags.length - 1;
		if (base == null)
			base = methods.game.getMapBase();
		boolean isDoor = methods.doors.isDoorAt(new RSTile(x + base.getX(), y
				+ base.getY(), z));
		/*
		 * RSLadder ladder = methods.ladders.getLadderAt(new RSTile(x +
		 * base.getX(), y + base.getY(), z)); if (ladder != null &&
		 * ladder.getObject() != null) { if (z < 4 && ladder.canClimbUp()) for
		 * (RSTile tile : ladder.getObject().getArea().getTileArray())
		 * tiles.add(new Node(tile.getX() - base.getX(), tile.getY() -
		 * base.getY(), z + 1)); if (z > 0 && ladder.canClimbDown()) for (RSTile
		 * tile : ladder.getObject().getArea().getTileArray()) tiles.add(new
		 * Node(tile.getX() - base.getX(), tile.getY() - base.getY(), z - 1)); }
		 */
		if ((f_y > 0
				&& ((here & WALL_SOUTH) == 0 || isDoor || methods.doors
						.isDoorAt(new RSTile(x + base.getX(), y + base.getY()
								- 1))) && ((flags[z][f_x][f_y - 1] & BLOCKED) == 0)
		/*
		 * || methods . ladders . isLadderAt ( new RSTile ( base . getX ( ) + x
		 * , y + base . getY ( ) - 1 )
		 */)) {
			tiles.add(new Node(x, y - 1, z));
		}
		if ((f_x > 0
				&& ((here & WALL_WEST) == 0 || isDoor || methods.doors
						.isDoorAt(new RSTile(x + base.getX() - 1, y
								+ base.getY()))) && ((flags[z][f_x - 1][f_y] & BLOCKED) == 0)
		/*
		 * || methods . ladders . isLadderAt ( new RSTile ( base . getX ( ) + x
		 * - 1 , y + base . getY ( ) ) )
		 */)) {
			tiles.add(new Node(x - 1, y, z));
		}
		if ((f_y < upper && ((here & WALL_NORTH) == 0 || isDoor
		/*
		 * || methods.doors .isDoorAt(new RSTile(x + base.getX(), y +
		 * base.getY() + 1))
		 */) && ((flags[z][f_x][f_y + 1] & BLOCKED) == 0)
		/*
		 * || methods . ladders . isLadderAt ( new RSTile ( base . getX ( ) + x
		 * , y + base . getY ( ) + 1 ) )
		 */)) {
			tiles.add(new Node(x, y + 1, z));
		}
		if ((f_x < upper
				&& ((here & WALL_EAST) == 0 || isDoor || methods.doors
						.isDoorAt(new RSTile(x + base.getX() + 1, y
								+ base.getY()))) && ((flags[z][f_x + 1][f_y] & BLOCKED) == 0)
		/*
		 * || methods.ladders .isLadderAt(new RSTile(base.getX() + x + 1, y +
		 * base.getY()))
		 */)) {
			tiles.add(new Node(x + 1, y, z));
		}

		if (f_x > 0 && f_y > 0
				&& (here & (WALL_SOUTH_WEST | WALL_SOUTH | WALL_WEST)) == 0
				&& (flags[z][f_x - 1][f_y - 1] & BLOCKED) == 0
				&& (flags[z][f_x][f_y - 1] & (BLOCKED | WALL_WEST)) == 0
				&& (flags[z][f_x - 1][f_y] & (BLOCKED | WALL_SOUTH)) == 0) {
			tiles.add(new Node(x - 1, y - 1, z));
		}
		if (f_x > 0 && f_y < upper
				&& (here & (WALL_NORTH_WEST | WALL_NORTH | WALL_WEST)) == 0
				&& (flags[z][f_x - 1][f_y + 1] & BLOCKED) == 0
				&& (flags[z][f_x][f_y + 1] & (BLOCKED | WALL_WEST)) == 0
				&& (flags[z][f_x - 1][f_y] & (BLOCKED | WALL_NORTH)) == 0) {
			tiles.add(new Node(x - 1, y + 1, z));
		}
		if (f_x < upper && f_y > 0
				&& (here & (WALL_SOUTH_EAST | WALL_SOUTH | WALL_EAST)) == 0
				&& (flags[z][f_x + 1][f_y - 1] & BLOCKED) == 0
				&& (flags[z][f_x][f_y - 1] & (BLOCKED | WALL_EAST)) == 0
				&& (flags[z][f_x + 1][f_y] & (BLOCKED | WALL_SOUTH)) == 0) {
			tiles.add(new Node(x + 1, y - 1, z));
		}
		if (f_x > 0 && f_y < upper
				&& (here & (WALL_NORTH_EAST | WALL_NORTH | WALL_EAST)) == 0
				&& (flags[z][f_x + 1][f_y + 1] & BLOCKED) == 0
				&& (flags[z][f_x][f_y + 1] & (BLOCKED | WALL_EAST)) == 0
				&& (flags[z][f_x + 1][f_y] & (BLOCKED | WALL_NORTH)) == 0) {
			tiles.add(new Node(x + 1, y + 1, z));
		}

		return tiles;
	}
}