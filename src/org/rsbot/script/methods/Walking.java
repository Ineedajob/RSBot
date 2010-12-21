package org.rsbot.script.methods;

import org.rsbot.script.wrappers.RSTile;
import org.rsbot.script.wrappers.RSTilePath;

import java.awt.*;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Walking related operations.
 */
@SuppressWarnings("deprecation")
public class Walking extends MethodProvider {

	public final int INTERFACE_RUN_ORB = 750;

	Walking(final MethodContext ctx) {
		super(ctx);
	}

	private ArrayList<WalkerNode> nodes = new ArrayList<WalkerNode>();
	private boolean mapLoaded = false;

	private RSTile[] lastPath;
	private RSTile lastDestination;
	private RSTile lastStep;

	/**
	 * Creates a new path based on a provided array of
	 * tile waypoints.
	 *
	 * @param tiles The waypoint tiles.
	 * @return An RSTilePath.
	 */
	public RSTilePath newTilePath(RSTile[] tiles) {
		if (tiles == null) {
			throw new IllegalArgumentException("null waypoint list");
		}
		return new RSTilePath(methods, tiles);
	}

	/**
	 * Generates a path from the player's current location
	 * to a destination tile.
	 *
	 * @param destination The destination tile.
	 * @return The path as an RSTile array.
	 */
	public RSTile[] findPath(RSTile destination) {
		return cleanPath(generateFixedPath(destination));
	}

	/**
	 * Walks one tile towards the given destination using
	 * a generated path.
	 *
	 * @param destination The destination tile.
	 * @return <tt>true</tt> if the next tile was walked
	 *         to; otherwise <tt>false</tt>.
	 */
	public boolean walkTo(RSTile destination) {
		if (destination == lastDestination &&
				methods.calc.distanceTo(lastStep) < 10) {
			lastStep = nextTile(lastPath);
			return lastStep != null && walkTileMM(lastStep);
		}
		lastDestination = destination;
		lastPath = findPath(destination);
		lastStep = nextTile(lastPath);
		RSTile dest = getDestination();
		return (dest == null || (methods.players.getMyPlayer().isMoving() &&
				methods.calc.distanceBetween(dest, lastStep) <= 1)) &&
				lastStep != null && walkTileMM(lastStep);
	}

	/**
	 * Walks to the given tile using the minimap with 1 tile randomness.
	 *
	 * @param t The tile to walk to.
	 * @return <tt>true</tt> if the tile was clicked; otherwise <tt>false</tt>.
	 * @see #walkTileMM(RSTile, int, int)
	 */
	public boolean walkTileMM(RSTile t) {
		return walkTileMM(t, 1, 1);
	}

	/**
	 * Walks to the given tile using the minimap with given randomness.
	 *
	 * @param t The tile to walk to.
	 * @param x The x randomness (between 0 and x-1).
	 * @param y The y randomness (between 0 and y-1).
	 * @return <tt>true</tt> if the tile was clicked; otherwise <tt>false</tt>.
	 */
	public boolean walkTileMM(RSTile t, int x, int y) {
		RSTile dest = new RSTile(t.getX() + random(0, x), t.getY() + random(0, y));
		Point p = methods.calc.tileToMinimap(dest);
		if (p.x != -1 && p.y != -1) {
			methods.mouse.move(p);
			Point p2 = methods.calc.tileToMinimap(dest);
			if (p2.x != -1 && p2.y != -1) {
				methods.mouse.click(p2, true);
				return true;
			}
		}
		return false;
	}

	/**
	 * Walks to a tile using onScreen clicks and not the MiniMap. If the tile is
	 * not on the screen, it will find the closest tile that is on screen and it
	 * will walk there instead.
	 *
	 * @param tileToWalk Tile to walk.
	 * @return True if successful.
	 */
	public boolean walkTileOnScreen(RSTile tileToWalk) {
		return methods.tiles.doAction(methods.calc.getTileOnScreen(tileToWalk), "Walk ");
	}

	/**
	 * Rests until 100% energy
	 *
	 * @return <tt>true</tt> if rest was enabled; otherwise false.
	 * @see #rest(int)
	 */
	public boolean rest() {
		return rest(100);
	}

	/**
	 * Rests until a certain amount of energy is reached.
	 *
	 * @param stopEnergy Amount of energy at which it should stop resting.
	 * @return <tt>true</tt> if rest was enabled; otherwise false.
	 */
	public boolean rest(int stopEnergy) {
		int energy = getEnergy();
		for (int d = 0; d < 5; d++) {
			methods.interfaces.getComponent(INTERFACE_RUN_ORB, 1).doAction("Rest");
			methods.mouse.moveSlightly();
			sleep(random(400, 600));
			int anim = methods.players.getMyPlayer().getAnimation();
			if (anim == 12108 || anim == 2033 || anim == 2716 || anim == 11786 || anim == 5713) {
				break;
			}
			if (d == 4) {
				return false;
			}
		}
		while (energy < stopEnergy) {
			sleep(random(250, 500));
			energy = getEnergy();
		}
		return true;
	}

	/**
	 * Turns run on or off using the game GUI controls.
	 *
	 * @param enable <tt>true</tt> to enable run, <tt>false</tt> to disable it.
	 */
	public void setRun(boolean enable) {
		if (isRunEnabled() != enable) {
			methods.interfaces.getComponent(INTERFACE_RUN_ORB, 0).doClick();
		}
	}

	/**
	 * Randomizes a single tile.
	 *
	 * @param tile		  The RSTile to randomize.
	 * @param maxXDeviation Max X distance from tile.getX().
	 * @param maxYDeviation Max Y distance from tile.getY().
	 * @return The randomized tile.
	 */
	@Deprecated
	public RSTile randomize(RSTile tile, int maxXDeviation, int maxYDeviation) {
		return tile.randomize(maxXDeviation, maxYDeviation);
	}

	/**
	 * Returns the closest tile on the minimap to a given tile.
	 *
	 * @param tile The destination tile.
	 * @return Returns the closest tile to the destination on the minimap.
	 */
	public RSTile getClosestTileOnMap(RSTile tile) {
		if (!methods.calc.tileOnMap(tile) && methods.game.isLoggedIn()) {
			RSTile loc = methods.players.getMyPlayer().getLocation();
			RSTile walk = new RSTile((loc.getX() + tile.getX()) / 2, (loc.getY() + tile.getY()) / 2);
			return methods.calc.tileOnMap(walk) ? walk : getClosestTileOnMap(walk);
		}
		return tile;
	}

	/**
	 * Returns whether or not run is enabled.
	 *
	 * @return <tt>true</tt> if run mode is enabled; otherwise <tt>false</tt>.
	 */
	public boolean isRunEnabled() {
		return methods.settings.getSetting(173) == 1;
	}

	/**
	 * Returns the player's current run energy.
	 *
	 * @return The player's current run energy.
	 */
	public int getEnergy() {
		try {
			return Integer.parseInt(methods.interfaces.getComponent(750, 5).getText());
		} catch (NumberFormatException e) {
			return 0;
		}
	}

	/**
	 * Gets the destination tile (where the flag is on the minimap).
	 * If there is no destination currently, null will be returned.
	 *
	 * @return The current destination tile, or null.
	 */
	public RSTile getDestination() {
		if (methods.client.getDestX() <= 0) {
			return null;
		}
		return new RSTile(methods.client.getDestX() + methods.client.getBaseX(),
				methods.client.getDestY() + methods.client.getBaseY());
	}

	/**
	 * Gets the collision flags for a given floor level in
	 * the loaded region.
	 *
	 * @param plane The floor level (0, 1, 2 or 3).
	 * @return the collision flags.
	 */
	public int[][] getCollisionFlags(int plane) {
		return methods.client.getRSGroundDataArray()[plane].getBlocks();
	}

	/**
	 * Returns the collision map offset from the current region
	 * base on a given plane.
	 *
	 * @param plane The floor level.
	 * @return The offset as an RSTile.
	 */
	public RSTile getCollisionOffset(int plane) {
		org.rsbot.client.RSGroundData data = methods.client.getRSGroundDataArray()[plane];
		return new RSTile(data.getX(), data.getY());
	}

	// DEPRECATED

	/**
	 * Randomizes a single tile.
	 *
	 * @param tile		  The RSTile to randomize.
	 * @param maxXDeviation Max X distance from tile.getX().
	 * @param maxYDeviation Max Y distance from tile.getY().
	 * @return The randomized tile.
	 * @deprecated Use {@link #randomize(org.rsbot.script.wrappers.RSTile, int, int)}.
	 */
	@Deprecated
	public RSTile randomizeTile(RSTile tile, int maxXDeviation, int maxYDeviation) {
		return randomize(tile, maxXDeviation, maxYDeviation);
	}

	/**
	 * Walks towards the end of a path. This method should be looped.
	 *
	 * @param path The path to walk along.
	 * @return <tt>true</tt> if the next tile was reached; otherwise
	 *         <tt>false</tt>.
	 * @see #walkPathMM(RSTile[], int)
	 */
	@Deprecated
	public boolean walkPathMM(RSTile[] path) {
		return walkPathMM(path, 16);
	}

	/**
	 * Walks towards the end of a path. This method should be looped.
	 *
	 * @param path	The path to walk along.
	 * @param maxDist See {@link #nextTile(RSTile[], int)}.
	 * @return <tt>true</tt> if the next tile was reached; otherwise
	 *         <tt>false</tt>.
	 * @see #walkPathMM(RSTile[], int, int)
	 */
	@Deprecated
	public boolean walkPathMM(RSTile[] path, int maxDist) {
		return walkPathMM(path, maxDist, 1, 1);
	}

	/**
	 * Walks towards the end of a path. This method should be looped.
	 *
	 * @param path  The path to walk along.
	 * @param randX The X value to randomize each tile in the path by.
	 * @param randY The Y value to randomize each tile in the path by.
	 * @return <tt>true</tt> if the next tile was reached; otherwise
	 *         <tt>false</tt>.
	 * @see #walkPathMM(RSTile[], int, int, int)
	 */
	@Deprecated
	public boolean walkPathMM(RSTile[] path, int randX, int randY) {
		return walkPathMM(path, 16, randX, randY);
	}

	/**
	 * Walks towards the end of a path. This method should be looped.
	 *
	 * @param path	The path to walk along.
	 * @param maxDist See {@link #nextTile(RSTile[], int)}.
	 * @param randX   The X value to randomize each tile in the path by.
	 * @param randY   The Y value to randomize each tile in the path by.
	 * @return <tt>true</tt> if the next tile was reached; otherwise
	 *         <tt>false</tt>.
	 */
	@Deprecated
	public boolean walkPathMM(RSTile[] path, int maxDist, int randX, int randY) {
		try {
			RSTile next = nextTile(path, maxDist);
			return next != null && walkTileMM(next, randX, randY);
		} catch (Exception e) {
			return false;
		}
	}

	/**
	 * Walks to the end of a path via the screen. This method should be looped.
	 *
	 * @param path The path to walk along.
	 * @return <tt>true</tt> if the next tile was reached; otherwise
	 *         <tt>false</tt>.
	 * @see #walkPathOnScreen(RSTile[], int)
	 */
	@Deprecated
	public boolean walkPathOnScreen(RSTile[] path) {
		return walkPathOnScreen(path, 16);
	}

	/**
	 * Walks a path using onScreen clicks and not the MiniMap. If the next tile
	 * is not on the screen, it will find the closest tile that is on screen and
	 * it will walk there instead.
	 *
	 * @param path	Path to walk.
	 * @param maxDist Max distance between tiles in the path.
	 * @return True if successful.
	 */
	@Deprecated
	public boolean walkPathOnScreen(RSTile[] path, int maxDist) {
		RSTile next = nextTile(path, maxDist);
		if (next != null) {
			RSTile os = methods.calc.getTileOnScreen(next);
			return os != null && methods.tiles.doAction(os, "Walk");
		}
		return false;
	}

	/**
	 * Reverses an array of tiles.
	 *
	 * @param other The <tt>RSTile</tt> path array to reverse.
	 * @return The reverse <tt>RSTile</tt> path for the given <tt>RSTile</tt> path.
	 */
	@Deprecated
	public RSTile[] reversePath(RSTile[] other) {
		RSTile[] t = new RSTile[other.length];
		for (int i = 0; i < t.length; i++) {
			t[i] = other[other.length - i - 1];
		}
		return t;
	}

	/**
	 * Returns the next tile to walk to on a path.
	 *
	 * @param path The path.
	 * @return The next <tt>RSTile</tt> to walk to on the provided path;
	 *         or <code>null</code> if far from path or at destination.
	 * @see #nextTile(RSTile[], int)
	 */
	@Deprecated
	public RSTile nextTile(RSTile path[]) {
		return nextTile(path, 17);
	}

	/**
	 * Returns the next tile to walk to in a path.
	 *
	 * @param path	 The path.
	 * @param skipDist If the distance to the tile after the
	 *                 next in the path is less than or equal to this distance,
	 *                 the tile after next will be returned rather than the next
	 *                 tile, skipping one. This interlacing aids continuous walking.
	 * @return The next <tt>RSTile</tt> to walk to on the provided path;
	 *         or <code>null</code> if far from path or at destination.
	 */
	@Deprecated
	public RSTile nextTile(RSTile path[], int skipDist) {
		int dist = 99;
		int closest = -1;
		for (int i = path.length - 1; i >= 0; i--) {
			RSTile tile = path[i];
			int d = methods.calc.distanceTo(tile);
			if (d < dist) {
				dist = d;
				closest = i;
			}
		}

		int feasibleTileIndex = -1;

		for (int i = closest; i < path.length; i++) {

			if (methods.calc.distanceTo(path[i]) <= skipDist)
				feasibleTileIndex = i;
			else
				break;
		}

		if (feasibleTileIndex == -1)
			return null;
		else
			return path[feasibleTileIndex];
	}

	/**
	 * Randomizes a path of tiles.
	 *
	 * @param path		  The RSTiles to randomize.
	 * @param maxXDeviation Max X distance from tile.getX().
	 * @param maxYDeviation Max Y distance from tile.getY().
	 * @return The new, randomized path.
	 */
	@Deprecated
	public RSTile[] randomizePath(RSTile[] path, int maxXDeviation, int maxYDeviation) {
		RSTile[] rez = new RSTile[path.length];
		for (int i = 0; i < path.length; i++) {
			rez[i] = randomize(path[i], maxXDeviation, maxYDeviation);
		}
		return rez;
	}

	// INTERNAL

	/**
	 * This method will remove any duplicates tiles in a RSTile[] path. This is
	 * preferably to be used with generateFixedPath. For instance:
	 * walkPathMM(cleanPath(generatedFixedPath(tile)));
	 * <p/>
	 * Written by: Taha
	 *
	 * @param path The messy RSTile[] path with duplicate methods.tiles.
	 * @return The cleaned RSTile[] path with no duplicate methods.tiles.
	 */
	private RSTile[] cleanPath(RSTile[] path) {
		LinkedList<RSTile> tempPath = new LinkedList<RSTile>();
		for (RSTile tile : path)
			if (!tempPath.contains(tile)) {
				tempPath.add(tile);
			}
		RSTile[] cleanedPath = new RSTile[tempPath.size()];
		for (int i = 0; i < tempPath.size(); i++) {
			cleanedPath[i] = tempPath.get(i);
		}
		return cleanedPath;
	}

	/**
	 * @param t The <tt>RSTile</tt> to create the path to.
	 * @return A fixed <tt>RSTile</tt> path to the given x and y tile values.
	 * @see #fixPath(RSTile[])
	 */
	private RSTile[] generateFixedPath(RSTile t) {
		return fixPath(generateProperPath(t));
	}

	/**
	 * @param path The <tt>RSTile</tt> path to fix.
	 * @return A new <tt>RSTile</tt> array representing the fixed path.
	 */
	private RSTile[] fixPath(RSTile[] path) {
		ArrayList<RSTile> newPath = new ArrayList<RSTile>();
		for (int i = 0; i < path.length - 1; i++) {
			RSTile s = path[i], d = path[i + 1];
			newPath.addAll(fixPath2(s.getX(), s.getY(), d.getX(), d.getY()));
		}
		return newPath.toArray(new RSTile[newPath.size()]);
	}

	/**
	 * @param startX	   X value based on runescape's game plane and the starting position.
	 * @param startY	   Y value based on runescape's game plane and the starting position.
	 * @param destinationX X value based on runescape's game plane and the destination position.
	 * @param destinationY Y value based on runescape's game plane and the destination position.
	 * @return A new <tt>RSTile</tt> List representing the fixed path to the destination from the starting location.
	 */
	private List<RSTile> fixPath2(int startX, int startY, int destinationX, int destinationY) {
		double dx, dy;
		ArrayList<RSTile> list = new ArrayList<RSTile>();
		list.add(new RSTile(startX, startY));
		while (Math.hypot(destinationY - startY, destinationX - startX) > 8) {
			dx = destinationX - startX;
			dy = destinationY - startY;
			int gamble = random(14, 17);
			while (Math.hypot(dx, dy) > gamble) {
				dx *= .95;
				dy *= .95;
			}
			startX += (int) dx;
			startY += (int) dy;
			list.add(new RSTile(startX, startY));
		}
		list.add(new RSTile(destinationX, destinationY));
		return list;
	}

	/**
	 * @param targetX X value based on runescape's game plane.
	 * @param targetY Y value based on runescape's game plane.
	 * @return A fixed <tt>RSTile</tt> path to the given x and y tile values.
	 */
	private RSTile[] generateProperPath(int targetX, int targetY) {
		if (!mapLoaded) {
			loadMap();
		}
		int mx = methods.players.getMyPlayer().getLocation().getX();
		int my = methods.players.getMyPlayer().getLocation().getY();
		WalkerNode target = new WalkerNode(targetX, targetY);
		WalkerNode startNode = nodes.get(0), endNode = startNode;
		int shortestDistance = distance(startNode, mx, my);
		for (WalkerNode node : nodes) {
			if (distance(node, mx, my) < shortestDistance) {
				startNode = node;
				shortestDistance = distance(node, mx, my);
			}
		}
		shortestDistance = distance(endNode, targetX, targetY);
		for (WalkerNode node : nodes) {
			if (node.distance(target) < shortestDistance) {
				endNode = node;
				shortestDistance = node.distance(target);
			}
		}
		WalkerNode[] nodePath = findPath(startNode, endNode);
		if (nodePath == null)
			return new RSTile[]{new RSTile(mx, my), new RSTile(targetX, targetY)};
		else {
			RSTile[] tilePath = new RSTile[nodePath.length];
			tilePath[0] = new RSTile(mx, my);
			for (int i = 1; i < tilePath.length - 1; i++) {
				tilePath[i] = new RSTile(nodePath[i - 1].x, nodePath[i - 1].y);
			}
			tilePath[tilePath.length - 1] = new RSTile(targetX, targetY);
			return tilePath;
		}
	}

	/**
	 * @param startNode The beginning node.
	 * @param endNode   The destination node.
	 * @return A <tt>WalkerNode</tt> array representing a path to the destination node
	 *         from the starting node.
	 */
	private WalkerNode[] findPath(WalkerNode startNode, WalkerNode endNode) {
		if (!mapLoaded) {
			loadMap();
		}
		try {
			ArrayList<WalkerNode> Q = new ArrayList<WalkerNode>();
			for (WalkerNode thisNode : nodes) {
				thisNode.distance = Integer.MAX_VALUE;
				thisNode.previous = null;
				Q.add(thisNode);
			}
			startNode.distance = 0;
			while (!Q.isEmpty()) {
				WalkerNode nearestNode = Q.get(0);
				for (WalkerNode thisNode : Q) {
					if (thisNode.distance < nearestNode.distance) {
						nearestNode = thisNode;
					}
				}
				Q.remove(Q.indexOf(nearestNode));
				if (nearestNode == endNode) {
					break;
				} else {
					for (WalkerNode neighbourNode : nearestNode.neighbours) {
						int alt = nearestNode.distance + nearestNode.distance(neighbourNode);
						if (alt < neighbourNode.distance) {
							neighbourNode.distance = alt;
							neighbourNode.previous = nearestNode;
						}
					}
				}
			}
			ArrayList<WalkerNode> nodePath = new ArrayList<WalkerNode>();
			nodePath.add(endNode);
			WalkerNode previousNode = endNode.previous;
			while (previousNode != null) {
				nodePath.add(previousNode);
				previousNode = previousNode.previous;
			}
			if (nodePath.size() == 1)
				return null;
			WalkerNode[] nodeArray = new WalkerNode[nodePath.size()];
			for (int i = nodePath.size() - 1; i >= 0; i--) {
				nodeArray[nodePath.size() - i - 1] = nodePath.get(i);
			}
			return nodeArray;
		} catch (Exception ignored) {
		}
		return null;
	}

	/**
	 * @param t The <tt>RSTile</tt> destination.
	 * @return A fixed RSTile path to the given <tt>RSTile</tt>.
	 */
	private RSTile[] generateProperPath(RSTile t) {
		return generateProperPath(t.getX(), t.getY());
	}

	/**
	 * @param startNode Starting WalkerNode.
	 * @param endX	  Ending x value.
	 * @param endY	  Ending y value.
	 * @return The distance between the starting <tt>WalkerNode</tt> and the ending X and Y values.
	 */
	private int distance(WalkerNode startNode, int endX, int endY) {
		int dx = startNode.x - endX;
		int dy = startNode.y - endY;
		return (int) Math.sqrt(dx * dx + dy * dy);
	}

	/**
	 * Refreshed the nodes and links.
	 */
	private void loadMap() {
		mapLoaded = true;
		loadNodes();
		loadLinks();
	}

	/**
	 * Adds all nodes to the <tt>ArrayList</tt> nodes.
	 *
	 * @see #nodes
	 */
	private void loadNodes() {
		String[] matrix = Walker.getWalkerNodes().split(" ");
		for (int i = 0; i < matrix.length; i += 2) {
			nodes.add(new WalkerNode(Integer.parseInt(matrix[i]), Integer.parseInt(matrix[i + 1])));
		}
	}

	/**
	 * Adds all links to the <tt>ArrayList</tt> node.
	 */
	private void loadLinks() {
		String[] matrix = Walker.getWalkerLinks().split(" ");
		for (int i = 0; i < matrix.length; i += 2) {
			int x = Integer.parseInt(matrix[i]);
			int y = Integer.parseInt(matrix[i + 1]);
			WalkerNode node = nodes.get(x);
			node.neighbours.add(nodes.get(y));
			node = nodes.get(y);
			node.neighbours.add(nodes.get(x));
		}
	}

	/**
	 * @author Aftermath
	 */
	static class WalkerNode {
		ArrayList<WalkerNode> neighbours = new ArrayList<WalkerNode>();
		WalkerNode previous;
		int x, y, distance;

		public WalkerNode(final int x, final int y) {
			this.x = x;
			this.y = y;
			previous = null;
		}

		public int distance(final WalkerNode neighbour) {
			final int dx = x - neighbour.x;
			final int dy = y - neighbour.y;
			return (int) Math.sqrt(dx * dx + dy * dy);
		}
	}

	/**
	 * @author Aftermath
	 */
	static class Walker {

		public static String getWalkerLinks() {
			return "0 1 23 22 22 21 21 20 20 1 1 2 0 20 0 2 2 3 3 4 4 5 6 5 5 7 7 8 8 9 5 24 24 7 24 25 25 8 8 26 26 25 26 9 9 10 10 11 11 14 14 10 14 15 15 11 11 27 27 15 27 28 27 29 28 29 28 30 30 31 31 32 31 33 33 32 32 36 36 33 34 33 34 35 35 33 36 35 30 32 15 16 14 16 16 17 17 15 12 14 12 13 18 17 18 19 44 42 42 41 41 40 43 41 43 40 40 39 38 40 39 38 38 19 19 37 46 47 46 45 45 37 47 48 48 49 157 50 49 157 49 50 48 158 49 158 158 47 47 85 85 87 84 85 158 83 84 83 83 85 83 82 82 86 86 84 84 88 88 86 86 91 91 88 88 90 90 89 89 88 88 87 89 98 98 97 89 97 97 90 97 96 96 95 95 97 90 93 95 93 91 90 93 91 91 92 92 93 92 86 86 81 81 82 81 92 81 78 78 80 79 78 80 79 103 79 93 94 94 95 94 99 99 101 101 102 103 102 101 100 101 104 105 104 105 106 106 107 108 107 108 109 109 110 110 111 159 111 159 110 160 159 160 110 159 161 161 112 112 113 113 114 112 114 161 113 114 162 162 113 113 116 162 116 116 115 116 118 118 117 117 115 115 118 117 119 119 115 115 120 120 119 120 121 121 119 121 132 132 119 121 130 130 132 130 122 122 120 122 162 162 124 124 122 123 124 122 123 125 123 125 124 123 126 126 125 123 129 129 122 129 126 129 130 130 131 133 131 139 133 134 133 133 138 138 139 134 139 138 134 139 135 138 135 135 136 136 140 140 137 137 150 150 148 148 128 128 127 127 126 127 136 128 136 150 140 150 136 150 128 148 141 141 150 137 141 109 111 111 108 80 77 76 77 79 76 76 103 73 76 73 74 74 77 77 73 73 75 75 70 70 68 68 69 69 66 66 65 65 67 67 68 163 68 68 71 71 70 75 71 71 163 163 72 72 71 71 74 74 72 74 75 164 163 67 163 164 67 67 57 57 56 164 56 56 67 57 65 55 164 55 56 165 55 164 165 165 163 165 72 50 51 51 52 52 53 53 54 54 55 172 171 171 170 169 170 169 168 168 166 166 167 167 168 166 157 167 173 158 157 174 37 19 174 176 174 176 175 175 45 19 177 18 177 46 180 45 180 179 180 178 177 178 179 181 178 179 181 181 180 8 182 9 182 7 6 6 183 5 183 187 186 185 184 191 189 189 188 189 190 193 192 195 194 194 191 196 195 188 197 184 188 197 184 197 0 197 187 187 188 58 56 58 57 57 60 60 58 58 59 198 59 198 58 198 60 61 198 61 60 61 63 63 62 61 62 62 64 99 100 377 102 142 141 142 147 147 148 148 142 142 146 146 147 143 146 143 147 143 144 144 145 145 151 151 152 152 153 154 153 154 155 155 156 335 156 336 335 336 337 337 338 338 339 339 340 340 342 342 341 340 341 378 341 340 378 378 343 343 340 343 379 379 337 336 379 335 344 344 156 344 345 345 346 347 346 348 347 347 345 346 348 348 349 349 347 349 350 350 351 351 352 352 354 354 353 353 352 354 355 350 383 383 349 385 387 385 386 387 384 384 383 382 383 382 386 382 384 382 381 355 381 355 356 357 358 358 360 358 359 359 361 361 360 360 359 362 360 362 361 361 363 363 364 364 362 364 365 365 363 363 388 365 388 365 366 366 364 366 367 368 366 368 367 367 221 367 365 443 448 449 447 447 446 449 460 460 459 459 458 458 457 457 456 456 439 439 438 438 437 437 461 461 440 440 441 441 442 442 443 443 444 448 444 444 445 445 446 455 447 455 449 454 455 454 453 453 452 452 451 451 450 450 23 461 436 436 435 435 432 462 432 462 430 434 432 434 435 435 433 433 434 434 431 431 462 431 463 464 463 464 473 473 463 463 462 462 473 430 474 474 462 430 427 474 427 427 428 474 428 430 429 429 412 411 429 412 411 411 413 411 410 410 413 413 408 408 410 413 409 409 408 409 412 412 415 415 409 409 407 407 415 415 406 406 407 406 405 405 416 407 414 416 414 414 422 422 416 425 414 425 423 423 427 425 427 428 423 423 426 426 424 424 428 423 422 426 421 422 475 421 475 416 475 475 417 417 405 405 404 404 406 406 400 404 400 400 402 404 402 404 403 403 417 417 419 417 418 418 419 418 420 403 402 402 401 401 393 400 392 392 391 476 392 476 391 476 393 391 390 390 2 0 389 393 394 394 13 394 395 395 13 395 396 396 397 397 398 399 398 421 481 421 420 420 481 481 480 479 480 479 478 478 477 478 399 420 477 477 419 419 403 486 464 486 485 486 465 465 464 485 484 484 483 483 482 489 488 491 490 491 482 490 489 488 487 487 482 465 466 466 467 467 469 468 469 467 468 468 466 473 493 474 473 493 492 492 465 466 492 492 468 468 470 469 472 494 472 494 495 495 472 472 471 471 470 471 496 496 470 497 496 497 499 497 498 500 498 495 502 502 501 501 500 497 516 496 516 516 517 517 512 512 511 424 511 512 518 518 421 512 513 511 518 513 514 514 515 515 507 507 508 508 509 519 509 510 519 519 479 480 519 510 479 510 478 520 510 520 519 520 521 521 526 526 525 525 524 524 523 523 522 522 399 530 529 528 529 530 528 528 527 527 525 498 503 503 504 504 505 506 505 515 506 506 507 65 257 257 60 60 65 256 258 256 257 256 255 259 256 259 260 260 261 261 262 262 263 265 266 266 261 264 370 370 283 283 265 283 282 282 281 284 281 284 282 284 285 285 372 285 160 160 372 371 372 371 370 371 373 373 374 374 375 375 376 266 267 267 280 268 280 268 267 268 269 269 254 270 254 253 270 253 254 254 255 253 252 252 251 250 251 249 250 249 248 248 247 252 273 273 272 270 271 271 272 271 275 272 275 275 276 276 271 276 279 279 277 275 277 279 278 278 289 278 288 289 277 289 290 275 290 274 272 247 531 531 532 532 292 292 233 233 532 274 531 274 291 291 292 291 290 288 281 281 278 288 287 288 286 286 284 286 287 286 306 306 305 305 304 64 63 62 199 221 222 222 226 226 225 225 227 227 228 228 214 214 229 229 247 229 231 229 230 230 231 231 232 232 233 232 234 234 235 235 237 232 237 235 334 334 323 323 326 326 332 332 334 334 331 330 331 330 322 322 324 533 323 533 331 533 330 322 533 533 324 324 321 324 326 326 333 333 332 333 318 326 318 321 318 321 326 321 325 321 327 327 324 327 320 320 329 328 320 329 328 320 319 319 329 327 319 319 321 325 319 325 318 318 317 317 316 536 318 536 325 534 536 536 317 333 317 322 537 537 330 537 324 537 327 537 320 534 325 319 534 536 535 535 534 316 315 315 314 313 294 314 313 294 293 293 290 293 300 300 299 295 294 293 295 295 299 299 302 300 302 302 301 301 299 301 298 298 296 296 295 296 297 301 303 302 303 303 304 289 538 538 287 538 539 539 305 539 304 539 303 303 307 307 304 307 311 311 312 307 308 308 309 309 149 149 148 147 149 149 310 237 236 236 235 236 238 225 224 224 226 222 241 241 240 240 224 241 221 240 242 242 243 242 241 242 244 244 246 245 244 243 245 243 239 240 239 224 239 224 540 540 225 540 227 540 239 239 541 541 238 223 226 223 222 221 220 223 220 218 220 223 218 223 217 217 226 217 216 216 223 219 218 219 216 216 215 215 210 215 211 210 211 210 542 542 205 542 203 203 202 202 201 204 203 204 201 200 201 206 205 204 205 205 209 209 208 203 205 209 542 208 211 211 209 208 207 206 207 208 206 543 207 208 543 213 212 212 211 212 208 212 543 213 211 213 544 214 544 544 212 544 545 545 229 213 546 546 214 546 544 546 227 547 238 548 245 548 547 342 339 200 199 549 64 549 206 527 556 528 556 556 555 555 527 530 556 555 554 558 557 557 527 555 557 438 560 560 456 560 439 448 561 561 445 452 562 562 451 562 450 561 564 564 448 564 563 563 562 562 564 561 565 565 564 565 562 565 452 561 566 566 565 566 453 455 567 567 454 567 566 453 567 446 568 568 561 568 567 567 447 447 568 568 445 445 566 566 568 87 569 569 592 592 87 592 591 591 590 590 593 593 591 590 594 594 593 594 595 595 589 589 590 594 589 590 595 595 596 596 589 589 588 588 595 596 597 597 588 588 596 596 587 587 588 597 598 598 587 587 597 587 586 586 598 586 597 586 599 599 585 585 586 599 598 585 598 585 607 607 586 607 582 582 583 583 584 569 570 570 571 571 569 570 572 572 573 573 570 572 574 574 571 571 572 572 575 575 573 573 574 574 575 575 576 576 577 577 581 576 608 608 577 608 581 608 607 607 581 581 585 585 608 580 577 577 578 578 580 580 581 601 580 581 601 601 585 601 600 600 599 599 601 601 602 602 600 602 580 602 603 603 580 601 603 603 579 579 578 579 580 578 603 603 605 605 604 604 602 603 604 605 602 605 606 606 604 606 614 614 613 613 606 606 612 612 605 612 613 612 611 611 609 609 610 609 612 609 605 609 603 609 579 610 611 610 624 624 611 624 623 623 611 623 612 621 625 625 623 623 621 621 622 622 617 622 616 616 617 616 615 615 613 613 616 616 646 646 614 646 615 615 643 643 616 643 642 642 617 616 642 642 618 618 617 618 620 620 621 620 628 628 627 627 626 626 625 627 647 647 629 629 630 630 628 631 629 630 631 648 631 629 648 648 632 632 631 632 633 633 634 634 632 631 633 633 636 636 634 634 635 635 636 637 635 635 633 636 637 637 649 649 635 636 649 637 638 638 639 639 640 640 644 644 645 644 643 643 641 641 642 641 639 635 650 650 639 638 640 579 651 651 610 610 579 651 578 578 652 652 651 652 577 652 653 653 577 653 576 653 654 654 576 654 575 573 654 570 655 655 569 655 656 656 569 656 87 87 655 458 444 444 459 459 445 446 460 459 446 560 457 457 657 657 442 443 657 657 560 441 657 657 444 560 658 658 437 437 440 440 658 658 441 658 657 442 659 659 448 659 662 662 441 662 661 661 440 661 660 660 435 436 660 660 432 434 660 461 663 663 436 663 660 663 661 663 440 364 369 369 366 670 369 670 669 669 668 668 667 667 666 666 665 665 664 386 671 671 672 673 665 664 673 673 672 357 674 674 356 357 664 664 358 664 360 360 665 667 675 675 668 675 669 670 676 676 369 676 366 669 677 677 678 678 679 679 680 680 681 681 682 682 683 683 684 684 685 685 686 686 687 686 707 707 708 708 709 709 711 711 710 710 709 711 712 712 713 713 716 713 717 717 716 716 712 716 710 717 714 714 713 714 718 717 718 756 718 756 719 719 715 715 756 756 714 718 719 710 708 710 757 757 708 757 707 707 685 684 750 750 683 750 682 681 754 754 755 755 668 755 669 680 758 758 677 758 755 758 754 754 750 750 751 751 754 751 752 751 759 759 760 760 720 720 752 759 720 759 752 759 754 759 755 755 760 759 668 668 760 760 667 760 666 760 761 761 720 761 666 761 721 721 722 722 723 723 724 724 725 752 753 753 720 720 721 721 753 753 726 726 722 726 727 727 731 731 732 732 733 733 734 734 735 735 736 736 737 737 738 738 739 739 740 740 762 762 738 739 762 740 741 741 742 742 743 743 744 744 745 745 746 746 747 747 748 748 749 749 730 730 729 729 728 728 727 728 731 702 700 700 699 699 697 697 703 703 698 698 699 703 699 703 704 704 705 705 706 697 696 696 695 695 694 694 692 692 689 689 688 694 764 764 763 763 687 687 688 688 763 763 692 689 763 764 693 693 694 693 692 689 690 690 692 690 688 690 691 670 775 775 669 775 677 775 774 774 773 773 772 772 767 767 771 767 770 770 771 770 769 769 766 766 770 766 767 766 768 768 769 768 765 765 766 768 687 688 768 690 776 776 765 776 688 688 765 774 811 811 775 811 817 817 775 817 812 812 811 811 810 810 812 810 809 809 808 808 807 807 806 808 806 806 805 805 807 806 802 802 803 803 805 805 802 803 804 804 805 803 818 818 802 818 801 801 800 800 818 818 799 799 798 798 797 798 800 793 792 792 791 791 790 790 789 789 788 788 787 787 786 786 816 816 815 815 813 813 812 819 817 819 813 819 676 813 814 814 368 814 815 786 785 785 787 785 784 784 788 784 783 783 782 782 781 781 780 780 778 778 794 794 795 795 796 796 797 797 799 799 801 799 800 800 797 796 793 793 795 795 792 792 794 792 779 779 791 791 781 781 789 789 783 783 788 788 782 782 790 790 780 780 792 792 778 778 777 793 794 796 798 820 794 820 778 820 777 777 521 777 821 821 525 528 822 822 529 529 823 823 530 530 824 824 556 824 823 824 825 825 555 825 556 825 554 554 559 559 558 557 559 559 555 467 826 826 466 826 465 486 826 502 827 827 501 827 500 827 831 827 830 830 829 830 832 832 829 832 831 827 832 832 828 828 831 828 829 829 799 829 801 801 830 799 828 828 797 828 504 503 828 828 796 796 504 796 505 504 797 505 795 505 797 796 506 506 795 795 507 795 820 835 793 835 834 835 792 798 836 836 797 836 796 836 793 836 835 836 834 798 833 833 834 834 798 833 800 833 818 833 802 859 858 858 857 857 860 860 856 856 855 855 854 854 853 853 852 852 849 849 848 848 847 847 850 850 848 847 849 847 846 846 850 846 845 845 842 842 843 843 844 844 840 840 841 841 839 839 837 837 859 857 859 859 838 838 857 860 838 860 839 838 837 837 840 840 838 861 838 840 861 861 839 861 841 841 842 843 861 861 844 861 842 857 856 219 210 469 494 830 862 862 827 502 862 862 863 864 865 865 866 866 867 867 868 868 870 870 804 804 864 870 869 869 868 868 871 871 869 871 872 872 869 872 874 874 871 871 873 874 873 873 875 875 876 876 877 877 875 876 878 878 877 878 879 879 877 879 880 880 878 878 881 881 879 880 881 881 882 882 908 908 883 883 882 883 884 884 908 884 902 902 883 884 882 884 885 909 886 909 885 886 887 887 888 888 889 889 890 890 892 892 891 892 893 891 890 887 894 894 886 894 896 894 895 895 896 896 897 897 903 903 898 898 902 902 903 902 901 901 883 901 898 898 900 900 901 900 904 904 907 907 899 899 904 899 900 906 904 907 906 907 863 863 900 898 904 897 905 905 906 174 910 910 176 175 910 910 37 910 45 619 620 642 619 619 618 958 952 958 950 950 949 949 948 948 947 947 961 961 946 946 947 961 960 960 959 959 955 955 960 960 956 956 955 956 959 956 957 957 958 962 957 962 955 955 954 954 953 953 941 941 942 942 943 943 944 944 917 917 875 917 877 945 944 917 945 945 967 967 946 960 946 949 961 917 918 918 919 919 920 920 921 921 922 922 923 923 924 924 922 923 925 925 924 925 926 926 927 927 925 925 928 928 927 927 929 929 930 930 935 935 934 934 933 933 932 968 931 931 932 930 968 926 928 928 929 932 936 969 968 969 912 912 913 913 914 914 915 915 916 912 911 911 936 911 691 691 936 911 937 937 938 938 940 940 941 963 940 940 939 939 963 963 965 965 966 966 691 965 964 964 963 1055 1039 1039 1040 1040 1043 1043 1053 1053 1045 1045 1044 1044 1042 1042 1046 1046 1047 1047 1048 1048 1051 1051 1049 1049 1048 1049 1050 1044 1043 1043 1042 1042 1053 1053 1052 1052 1045 1053 1054 1054 1052 1054 1056 1056 1055 1055 890 1033 888 1033 889 1033 1034 1034 1038 1038 1037 1041 1038 1037 1041 1041 1040 1039 1038 1041 1039 1039 1034 1057 1042 1057 1037 1025 1024 1024 1015 1015 1014 1014 1013 1013 1026 1026 1027 1027 1035 1035 1028 1028 1031 1031 1032 1029 1003 1031 1058 1058 1030 1030 1032 1031 1030 1058 1029 1029 1030 1058 1032 1058 1028 1028 1026 1027 1028 1026 1035 1035 1036 1036 1027 1059 1027 1027 1025 1025 1059 1059 1036 1059 1037 1037 1036 1036 1038 1059 1035 1035 1031 1031 1034 1034 1035 1034 1032 1034 1036 1035 1038 1032 1033 1003 1005 1005 1001 1001 999 999 998 998 1001 1001 1003 1005 999 999 908 882 998 998 997 997 881 997 882 997 996 996 971 971 997 971 881 971 879 971 970 970 877 970 879 970 972 972 973 973 971 973 970 972 971 973 995 995 1000 1000 1002 1002 1004 1004 1001 1004 1000 1000 998 1004 998 998 996 996 1000 996 973 996 995 995 1002 1000 1001 1004 1003 1004 1007 1007 1002 1007 1009 1009 1011 1011 1008 1008 1009 1009 1010 1010 1008 1010 1011 1010 1012 1012 1013 1026 1012 1013 1017 1017 1021 1021 1022 1022 1023 1023 1021 1017 1016 1016 1021 1016 1014 1013 1016 1017 1019 1017 1018 1018 990 990 991 1062 1061 1061 1060 1060 991 989 987 987 990 990 989 989 988 988 987 988 986 986 987 986 992 992 988 988 994 994 993 994 992 994 1006 1006 1020 1020 1019 1006 988 992 993 994 995 995 993 1002 994 1006 1007 1007 1020 1020 1002 974 977 977 975 975 974 975 972 972 974 974 993 977 986 992 977 974 992 986 985 985 977 985 976 976 977 976 975 985 978 978 976 978 979 979 980 980 981 981 982 982 983 983 984 983 980 980 982 981 983 983 979 978 980 980 1063 1063 981 1063 978 1064 978 979 1064 1064 976 1029 1065 1065 909 1066 473 1066 428 1066 511 1066 424 1066 474 1066 512 1153 1155 1155 1156 1156 1150 1150 1155 1155 1154 1154 1153 1150 1151 1151 1154 1151 1152 1152 1154 1157 1152 1157 1158 1158 1152 1156 1149 1149 1150 1149 1159 1159 1148 1148 1147 1147 1161 1161 1146 1146 1160 1160 1067 1067 1142 1142 1141 1141 1140 1140 1139 1140 1143 1143 1139 1143 1144 1144 1145 1145 1137 1145 1136 1136 1135 1136 1137 1137 1138 1138 1139 1135 1134 1134 1133 1133 1131 1131 1130 1130 1129 1129 1128 1128 1127 1127 1126 1126 1125 1125 1124 1124 1123 1123 1122 1122 1080 1080 1081 1081 1082 1082 1083 1083 1084 1068 1067 1068 1069 1069 1070 1070 1071 1071 1072 1072 1073 1073 1074 1074 1075 1075 1132 1132 1076 1076 1075 1076 1077 1077 1078 1078 1079 1079 1080 1079 1081 1132 1131 1084 1085 1085 1086 1086 1121 1121 1084 1121 1117 1086 1087 1087 1089 1089 1090 1090 1091 1091 1092 1092 1093 1093 1094 1094 1095 1095 1096 1096 1097 1097 1099 1099 1096 1099 1106 1099 1100 1100 1101 1101 1095 1095 1100 1100 1094 1094 1101 1101 1103 1103 1102 1102 1119 1119 1118 1118 1089 1118 1088 1116 1117 1116 1115 1115 1113 1113 1114 1114 1115 1162 1113 1115 1162 1162 1120 1120 1119 1120 1163 1103 1104 1104 1120 1104 1111 1111 1112 1112 1110 1110 1111 1111 1105 1105 1107 1105 1110 1110 1107 1107 1106 1106 1105 1107 1108 1108 1106 1108 1109 1100 1105 1105 1101 1101 1104 1104 1105 1162 1112 1112 1113 1116 1163 1163 1117 1118 1164 1164 1117 1093 1165 1165 1101 1101 1093 1094 1165 1165 1103 1165 1092 1092 1102 1102 1091 1102 1090 1098 1097 1166 1098 1167 696 1171 1160 1171 1067 1171 1170 1169 1168 1168 1167 1170 1173 1173 1172 1172 1169 1180 1179 1179 1178 1178 1177 1177 1182 1182 1181 1182 1176 1176 1177 1176 1175 1175 1174 1174 1173 1185 1184 1184 1166 1186 1184 1187 1186 1188 1187 1189 1188 1190 1189 1191 1190 1192 1191 1194 1193 1195 1194 1196 1195 1197 1196 1198 1197 1199 1198 1200 1199 1201 1199 1200 1201 1202 1200 1203 1202 1203 1204 1205 1203 1206 1205 1207 1206 1208 1207 1209 1208 1208 1210 1211 1210 1211 1212 1213 1209 1214 1213 1215 1213 1216 1215 1217 1216 1218 1214 1219 1218 1220 1219 1221 1220 1222 1221 1223 1222 1224 1223 1225 1224 1226 1225 1227 1225 1228 1227 1229 1228 1229 1226 1230 1228 1231 1227 1231 1230 1232 1230 1232 1231 1233 1232 1234 1232 1235 1234 1236 1233 1237 1235 1238 1237 1239 1238 1240 1239 1241 1240 1241 1194 1242 1241 1242 1193 1243 1242 1240 1243 1244 1236 1245 1244 1247 1246 1248 1247 1249 1248 1250 1249 1243 1250 1251 1239 1251 1235 1252 1235 1252 1248 1252 1249 1253 1246 1254 1253 1255 1254 1255 1184 1256 1098 1256 1257 1258 1257 1258 1253 1259 1247 1259 1245 1260 1094 1261 1260 1262 1261 1263 1262 1264 1258 1264 1263 1265 1256 1096 1265 1266 1265 1266 1094 1257 1265 1267 1247 1246 1267 1268 1267 1269 1268 1269 1190 1273 1248 1273 1272 1272 1271 1271 1270 1270 1191 1270 1269 1271 1268 1272 1267 1274 1259 1252 1274 1275 1237 1275 1238 1276 1275 1277 1275 1278 1277 1224 1278 1279 1278 1280 1277 1280 1231 1279 1280 1279 1225 1279 1227 1281 1236 1282 1233 1282 1281 1283 1244 1284 1283 1281 1284 1285 1284 1286 1285 1287 1285 1288 1287 1289 1288 1290 1289 1290 1226 1291 1216 1292 1216 1293 1217 1294 1293 1295 1217 1296 1209 1297 1296 1298 1297 1299 1298 1300 1299 1301 1300 1302 1301 1303 1302 1304 1302 1305 1304 1306 1305 1307 1306 1308 1307 1309 1308 1310 1304 1282 1230 1311 1283 1286 1311 1312 1311 1313 1312 1313 1262 1314 1244 1311 1314 1314 1313 1314 1312 1315 1314 1316 1245 1316 1315 1314 1316 1316 1246 1317 1316 1263 1317 1264 1317 1264 1253 1315 1263 1315 1262 1318 1246 1318 1316 1318 1247 1318 1259 1318 1245 1319 1253 1319 1246 1319 1316 1317 1319 1319 1264 1320 1267 1320 1253 1246 1320 1321 1188 1321 1189 1187 1321 1322 1321 1323 1322 1323 1254 1324 1261 1324 1312 1325 1286 1325 1324 1326 1261 1325 1326 1327 1260 1326 1327 1093 1260 1328 1327 1328 1092 1329 1289 1290 1329 1330 1329 1330 1290 1331 1330 1331 1290 1332 1331 1332 1225 1332 1226 1333 1331 1333 1332 1334 1203 1335 1334 1336 1335 1337 1336 1197 1337 1338 1337 1339 1338 1340 1339 1340 1194 1340 1195 1195 1241 1341 1239 1341 1195 1341 1196 1342 1238 1342 1341 1343 1276 1277 1343 1344 1278 1277 1344 1345 1344 1223 1345 1347 1333 1346 1347 1348 1223 1346 1348 1349 1346 1348 1349 1347 1350 1351 1350 1352 1350 1352 1349 1353 1234 1233 1353 1354 1245 1354 1236 1354 1244 1355 1311 1355 1284 1355 1286 1356 1324 1356 1286 1356 1325 1356 1311 1324 1313 1313 1261 1237 1234 1357 1237 1357 1232 1357 1234 1358 1357 1358 1275 1358 1280 1231 1358 1343 1344 1359 1276 1359 1238 1360 1196 1360 1342 1361 1273 1271 1361 1272 1361 1362 1361 1362 1191 1363 1242 1251 1364 1240 1364 1365 1364 1365 1250 1367 1352 1351 1367 1368 1295 1369 1368 1307 1369 1370 1368 1369 1370 1371 1307 1306 1371 1371 1370 1372 1370 1371 1372 1372 1306 1373 1305 1306 1373 1373 1372 1374 1373 1374 1305 1374 1304 1375 1373 1370 1375 1372 1375 1376 1374 1302 1376 1376 1301 1377 1310 1377 1305 1378 1306 1378 1377 1379 1378 1379 1310 1380 1378 1379 1380 1381 1307 1381 1380 1382 1308 1381 1382 1383 1308 1383 1382 1383 1309 1384 1308 1307 1384 1385 1384 1385 1369 1384 1369 1385 1368 1386 1385 1386 1308 1387 1309 1387 1386 1388 1382 1380 1388 1389 1382 1389 1388 1390 1389 1390 1388 1379 1390 1380 1390 1391 1310 1391 1304 1392 1304 1391 1392 1392 1302 1303 1392 1393 1389 1394 1393 1395 1394 1396 1395 1397 1396 1398 1397 1399 1398 1400 1399 1401 1400 1402 1401 1403 1402 1404 1403 1405 1404 1406 1405 1407 1406 1407 1405 1408 1405 1409 1408 1381 1306 1378 1381 1410 1409 1411 1410 1412 1411 1413 1412 1414 1413 1415 1414 1416 1415 1417 1416 1417 1413 1418 1413 1418 1412 1419 1411 1419 1412 1420 1419 1421 1411 1421 1420 1422 1418 1423 1422 1424 1423 1424 1420 1425 1421 1425 1424 1426 1425 1427 1426 1428 1427 1406 1428 1429 1406 1430 1406 1431 1430 1431 1432 1432 1404 1433 1410 1433 1409 1434 1410 1435 1434 1436 1435 1437 1436 1438 1437 1434 1438 1436 1413 1439 1437 1439 1440 1441 1440 1441 1415 1414 1441 1442 1415 1443 1442 1443 1440 1444 1442 1444 1416 1445 1416 1446 1445 1446 1444 1447 1445 1448 1422 1448 1447 1448 1423 1449 1430 1449 1428 1450 1428 1451 1450 1452 1395 1393 1452 1453 1395 1453 1397 1454 1398 1399 1454 1455 1399 1455 1400 1456 1400 1456 1401 1457 1401 1402 1457 1458 1402 1403 1458 1458 1404 1456 1457 1456 1455 1455 1454 1459 1454 1459 1397 1453 1459 1460 1457 1460 1458 1461 1460 1458 1461 1462 1461 1463 1461 1464 1463 1462 1464 1461 1464 1465 1463 1466 1464 1466 1465 1467 1466 1468 1466 1468 1467 1469 1467 1470 1338 1471 1470 1472 1471 1473 1472 1473 1470 1473 1337 1473 1338 1473 1471 1473 1336 1474 1336 1474 1472 1474 1473 1475 1471 1475 1474 1476 1471 1475 1476 1477 1474 1475 1477 1478 1476 1477 1478 1472 1475 1475 1478 1479 1478 1480 1479 1480 1478 1482 1481 1484 1483 1485 1484 1485 1483 1486 1485 1483 1486 1487 1484 1488 1487 1489 1488 1491 1490 1492 1490 1493 1492 1494 1493 1495 1488 1489 1495 1495 1491 1494 1496 1495 1496 1489 1496 1497 1493 1498 1489 1498 1497 1499 1497 1501 1499 1502 1501 1500 1502 1503 1500 1504 1503 1505 1504 1503 1505 1506 1505 1507 1506 1509 1508 1510 1509 1511 1509 1512 1511 1513 1512 1303 1513 1514 1513 1514 1302 1514 1301 1515 1514 1515 1301 1516 1379 1516 1310 1546 1545 1545 1544 1543 1542 1547 1542 1541 1547 1548 1541 1539 1540 1538 1539 1549 1538 1549 1537 1537 1536 1535 1536 1535 1534 1550 1534 1551 1550 1551 1533 1533 1532 1532 1531 1531 1530 1530 1529 1529 1528 1528 1527 1527 1526 1526 1525 1525 1524 1524 1523 1523 1522 1522 1366 1366 1520 1520 1519 1519 1518 1518 1517 1552 1544 1552 1543 1548 1540 1554 1553 1555 1553 1556 1546 1556 1555 1557 1553 1557 1556 1558 1554 1559 1554 1559 1558 1560 1559 1560 1558 1561 1559 1560 1561 1562 1561 1560 1562 1563 1562 1563 1561 1564 1563 1562 1564 1565 1563 1564 1565 1566 1565 1566 1564 1567 1565 1566 1567 1568 1567 1568 1565 1569 1568 1567 1569 1570 1569 1570 1567 1571 1570 1571 1566 1567 1571 1572 1569 1570 1572 1573 1572 1573 1569 1574 1573 1572 1574 1575 1573 1575 1574 1576 1575 1574 1576 1576 1391 1575 1391 1577 1576 1391 1577 1578 1577 1578 1310 1578 1516 1578 1576 1579 1390 1579 1516 1580 1516 1580 1578 1581 1580 1581 1576 1582 1581 1582 1574 1583 1582 1583 1570 1572 1583 1584 1583 1584 1571 1585 1584 1585 1571 1586 1584 1586 1585 1587 1583 1587 1586 1587 1581 1393 1588 1390 1588 1589 1452 1589 1395 1453 1589 1590 1589 1591 1587 1591 1586 1592 1591 1593 1592 1593 1591 1594 1592 1593 1594 1595 1592 1595 1594 1596 1595 1594 1596 1596 1394 1597 1394 1597 1596 1598 1597 1598 1594 1599 1594 1600 1599 1600 1593 1601 1591 1601 1586 1602 1601 1602 1603 1600 1603 1604 1603 1602 1604 1605 1602 1604 1605 1606 1602 1605 1606 1607 1606 1607 1585 1608 1605 1608 1607 1609 1585 1607 1609 1609 1566 1609 1571 1610 1609 1610 1564 1611 1564 1611 1562 1612 1562 1612 1560 1613 1558 1613 1612 1612 1611 1611 1610 1608 1614 1615 1605 1615 1614 1616 1615 1616 1604 1617 1604 1617 1616 1618 1617 1618 1615 1619 1615 1618 1619 1620 1618 1620 1619 1621 1620 1621 1619 1622 1620 1622 1621 1623 1617 1624 1623 1625 1624 1625 1599 1626 1396 1626 1597 1626 1395 1627 1401 1627 1400 1628 1627 1629 1627 1628 1629 1630 1629 1628 1630 1631 1389 1631 1383 1632 1631 1632 1389 1632 1393 1633 1632 1633 1452 1633 1393 1634 1632 1633 1634 1634 1631 1635 1309 1636 1635 1637 1636 1638 1637 1638 1634 1639 1629 1630 1639 1630 1640 1628 1640 1641 1630 1641 1640 1642 1639 1642 1641 1643 1640 1641 1643 1644 1641 1644 1643 1645 1642 1645 1644 1646 1643 1644 1646 1647 1646 1648 1647 1648 1646 1649 1648 1649 1647 1649 1621 1649 1622 1622 1647 1650 1647 1651 1650 1652 1651 1653 1624 1654 1653 1655 1654 1656 1655 1657 1653 1657 1396 1657 1626 1658 1626 1658 1625 1597 1395 1659 1645 1659 1644 1660 1644 1660 1646 1648 1660 1661 1629 1639 1661 1662 1661 1662 1627 1662 1401 1663 1662 1663 1401 1663 1402 1664 1663 1664 1403 1665 1403 1665 1664 1664 1661 1639 1665 1666 1639 1666 1665 1642 1666 1667 1666 1668 1667 1668 1645 1669 1668 1669 1645 1669 1659 1670 1667 1670 1669 1671 1433 1671 1409 1672 1408 1672 1405 1672 1674 1674 1673 1675 1673 1675 1672 1676 1673 1677 1676 1677 1404 1678 1431 1430 1678 1679 1678 1680 1679 1680 1449 1680 1451 1681 1660 1681 1659 1682 1660 1681 1682 1682 1648 1683 1682 1683 1648 1684 1621 1685 1684 1685 1614 1686 1685 1686 1619 1669 1687 1670 1688 1688 1687 1688 1669 1689 1687 1688 1689 1690 1687 1689 1690 1691 1689 1690 1691 1692 1690 1692 1691 1693 1692 1693 1691 1693 1694 1694 1692 1695 1694 1692 1695 1696 1695 1696 1692 1696 1682 1681 1697 1696 1697 1683 1696 1698 1683 1699 1698 1699 1684 1700 1696 1700 1698 1700 1695 1701 1700 1695 1701 1702 1694 1702 1701 1703 1701 1704 1703 1704 1702 1705 1702 1705 1704 1706 1694 1693 1706 1707 1297 1708 1707 1709 1708 1710 1709 1711 1710 1712 1711 1713 1712 1708 1713 1714 1712 1715 1714 1716 1715 1707 1715 1707 1716 1296 1716 1717 1296 1718 1717 1298 1718 1719 1299 1719 1718 1299 1515 1720 1489 1498 1720 1721 1720 1722 1721 1722 1211 1722 1212 1723 1211 1723 1212 1724 1723 1724 1504 1725 1499 1726 982 1726 983 1727 1158 1728 1149 1729 1728 1730 1729 1730 1151 1731 1730 1731 1152 1732 851 849 1732 1732 850 1733 242 1734 1733 1734 246 1735 547 236 1735 1736 246 1736 245 1737 1218 1219 1737 1201 1739 1739 1738 1738 1737 1737 1740 1740 1220 1220 1741 1219 1741 1742 1220 1742 1741 1742 1743 1743 1744 1747 1746 1746 1743 1742 1747 1747 1221 1222 1749 1749 1748 1748 1367 1352 1749 1749 1349 1750 1752 1752 1753 1755 1756 1756 1757 1757 1758 1758 1759 1759 1760 1760 1761 1761 1762 1767 1762 1767 1768 1768 1763 1763 1764 1764 1765 1765 1766 1766 1775 1775 1765 1765 1774 1774 1764 1764 1773 1773 1763 1763 1772 1772 1768 1768 1771 1771 1767 1767 1770 1770 1762 1762 1769 1769 1761 1756 1780 1780 1757 1757 1779 1779 1758 1758 1778 1778 1759 1759 1777 1777 1760 1760 1776 1776 1761 1779 1780 1779 1778 1778 1777 1777 1776 1776 1769 1769 1770 1770 1771 1771 1772 1772 1773 1773 1774 1774 1775 1781 1766 1781 1755 1781 1754 1754 1753 1785 1780 1785 1784 1784 1783 1783 1782 1782 1750 1479 1798 1798 1797 1797 1796 1796 1795 1795 1794 1794 1793 1793 1792 1792 1791 1791 1789 1789 1788 1788 1790 1790 1787 1787 1799 1799 1786 1800 1801 1800 1786 1801 1802 1802 1803 1803 1804 1804 1805 1805 1806 1806 1807 1807 1811 1811 1812 1812 1813 1813 1814 1814 1815 1815 1816 1816 1818 1818 1819 1819 1820 1820 1821 1821 1822 1822 1823 1823 1824 1824 1825 1825 1826 1826 1827 1827 1805 1807 1808 1808 1809 1809 1810 1810 1817 1817 1816 1803 1831 1831 1804 1804 1830 1830 1805 1805 1829 1829 1806 1806 1828 1828 1807 1811 1828 1828 1829 1829 1830 1830 1831 1831 1836 1836 1832 1832 1833 1833 1834 1834 1835 1835 1811 1835 1828 1828 1834 1834 1829 1829 1833 1833 1830 1830 1832 1832 1831 1836 1839 1839 1832 1832 1838 1838 1833 1833 1837 1837 1834 1837 1838 1838 1839 1841 1837 1841 1838 1838 1840 1840 1839 1840 1841 1840 1842 1485 1843 1843 1486 1843 1844 1844 1847 1847 1845 1845 1844 1847 1846 1846 1769 1846 1776 1849 1847 1846 1848 1848 1849 1848 1776 1780 1853 1853 1779 1779 1852 1853 1854 1854 1852 1852 1778 1852 1851 1851 1850 1850 1848 1850 1776 1850 1777 1777 1851 1851 1778 1568 1856 1856 1855 1855 1509 1856 1857 1857 1573 1569 1857 1857 1568 1858 1457 1456 1858 1859 1858 1859 1456 1860 1858 1860 1460 1861 1464 1861 1467 1862 1387 1862 1635 1863 1635 1862 1863 1864 1783 1864 1782 1865 1750 1865 1782 1866 1750 1865 1866 1867 1865 1866 1867 1864 1865 1868 1864 1868 1867 1869 1783 1869 1864 1870 1869 1870 1868 1871 1870 1872 1869 1871 1869 1872 1871 1873 1872 1873 1871 1878 1869 1878 1876 1876 1877 1877 1853 1854 1875 1875 1874 1874 1872 1783 1878 1878 1784 1784 1876 1876 1785 1785 1877 1877 1780 1854 1877 1877 1875 1875 1876 1876 1874 1874 1878 1872 1879 1879 1878 1880 1875 1880 1854 1854 1881 1881 1852 1845 1882 1882 1769 1770 1882 1882 1883 1883 1770 1884 1883 1486 1885 1885 1884 1886 1481 1479 1886 1795 1890 1890 1889 1889 1888 1888 1887 1887 1886 1887 1798 1798 1888 1797 1888 1797 1889 1889 1796 1891 1478 1891 1479 1891 1886 1891 1481 1892 1481 1892 1478 1477 1892 1926 1928 1928 1927 1927 1926 1926 1924 1924 1922 1922 1926 1927 1922 1922 1925 1925 1927 1925 1900 1900 1901 1901 1902 1902 1925 1900 1899 1899 1904 1904 1901 1902 1903 1903 1904 1904 1905 1905 1898 1898 1899 1898 1896 1896 1894 1894 1893 1893 1895 1895 1910 1910 1897 1897 1906 1906 1905 1905 1897 1897 1909 1909 1908 1908 1907 1907 1906 1903 1929 1929 1907 1929 1908 1908 1931 1931 1930 1930 1911 1911 1912 1912 1913 1913 1914 1914 1915 1915 1916 1916 1917 1917 1918 1918 1919 1919 1920 1920 1921 1921 1923 1923 1922 1924 1921 1923 1924 1923 1920 1932 1919 1932 1923 1933 1923 1933 1922 1934 1902 1934 1933 1935 1933 1935 1903 1919 1930 1913 1936 1911 1936 1918 1936 1937 1913 1937 1914 1937 1917 1937 1918 1936 1930 1938 1916 1939 1938 1940 1939 1941 1940 1942 1474 1942 1892 1942 1477 1944 1794 1944 1793 1945 1793 1792 1945 1944 1945 1946 1945 1946 1944 1947 1946 1943 1947 1948 1943 1948 1947 1949 1943 1950 1949 1951 1950 1951 1773 1951 1774 1952 1949 1952 1890 1953 1889 1952 1953 1953 1950 1276 1954 1331 1347 1955 1330 1955 1331 1956 1329 1330 1956 1957 1367 1957 1748 1958 1748 1958 1221 1959 1222 1959 1223 1960 1223 1960 1345 1961 1278 1961 1345 1961 1344 1962 1344 1963 1191 1963 1190 1964 1963 1965 1964 1966 1965 1966 1963 1967 1966 1967 1963 1967 1191 1968 1967 1969 1192 1970 1969 1363 1970 1970 1193 1968 1192 1968 1969 1971 1965 1972 1971 1973 1972 1973 1969 1973 1968 1974 1971 1974 1965 1975 1974 1976 1975 1977 1976 1978 1977 1979 1977 1980 1979 1981 1980 1982 1981 1983 1982 1983 1978 1984 1896 1984 1894 1984 1895 1985 1508 1985 1893 1986 1894 1986 1985 1986 1508 1987 1893 1987 1508 1988 1893 1988 1895 1989 1910 1989 1895 1989 1909 1990 1984 1990 1897 1897 1896 1991 1990 1991 1898 1992 1775 1992 1774 1993 1992 1993 1951 1994 1188 1995 1994 1996 1995 1997 1996 1998 1997 1998 1999 2000 1998 2000 1997 2001 1184 2001 1166 2001 2002 2002 2003 2003 2004 2004 2005 2005 2006 2006 2007 2010 2011 2007 2087 2087 2008 2008 2009 2087 2010 2011 2012 2012 2089 2089 2013 2013 2088 2088 2014 2014 2015 2015 2016 2016 2017 2017 2018 2018 2019 2019 2020 2020 2021 2021 2022 2022 2023 2023 2024 2024 2025 2025 2026 2026 2027 2027 2028 2028 2029 2029 2030 2030 2031 2031 2032 1998 2090 2090 1997 2090 2091 2091 2092 2092 2093 2093 2017 2093 2094 2094 2016 2014 2095 2095 2094 2018 2097 2097 2016 2016 2096 2096 2097 2096 2015 2014 2098 2098 2096 2097 2100 2100 2096 2096 2099 2099 2098 2099 2100 2032 2033 2033 2034 2034 2035 2035 2048 2048 2049 2049 2050 2050 2051 2051 2052 2052 2053 2053 2054 2054 2055 2055 2056 2056 2070 2070 2057 2057 2058 2058 2059 2059 2060 2060 2045 2045 2044 2044 2043 2043 2042 2042 2061 2061 2043 2061 2064 2064 2065 2065 2066 2064 2062 2062 2061 2062 2063 2042 2041 2041 2040 2040 2039 2039 2038 2038 2037 2037 2036 2036 2047 2047 2046 2046 2045 2046 2036 2036 2035 2035 2047 2047 2048 2054 2067 2067 2068 2068 2069 2069 2071 2071 2072 2072 2073 2073 2074 2070 2069 2070 2071 2071 2077 2077 2069 2068 2075 2075 2076 2076 2068 2075 2079 2079 2077 2077 2075 2075 2078 2078 2081 2081 2080 2080 2078 2078 2076 2076 2080 2081 2083 2083 2082 2082 2084 2084 2085 2085 2086 2086 2084 2084 2083 2082 2085 2082 2078 2101 2081 2080 2101 2102 2083 2103 2084 2083 2103 2102 2101 2027 2029 2005 2109 2109 2107 2107 2108 2108 2002 2107 2110 2110 2106 2106 2105 2105 1186 1187 2113 2113 2112 2112 2111 2111 2106 2115 2111 2110 2115 2116 1269 2116 1190 1189 2116 2116 1321 1268 2117 2117 1320 1875 2118 2118 1874 2118 1880 2118 1872 2030 2119 2119 2034 2119 2031 2119 2033 2028 2121 2121 2029 2029 2031 2120 2029 2030 2120 2120 2031 2120 2121 2120 2032 2133 2132 2132 2131 2131 2130 2130 2149 2149 2129 2129 2148 2148 2147 2147 2146 2146 2125 2125 2145 2145 2150 2150 2144 2124 2123 2123 2122 2122 2143 2143 2142 2142 2141 2141 2140 2141 2125 2125 2126 2126 2140 2140 2139 2139 2127 2127 2126 2126 2147 2147 2128 2128 2127 2127 2137 2137 2136 2136 2128 2128 2129 2149 2136 2136 2130 2130 2135 2135 2131 2131 2134 2134 2132 2134 2135 2135 2136 2137 2135 2137 2138 2138 2127 2138 2139 2148 2151 2151 2146 2146 2152 2124 2153 2153 2144 2144 2123 2144 2122 2123 2142 2145 2153 2124 2141 2152 2145 2152 2154 2154 2150 2152 2151 2155 2149 2155 2148 2156 2137 2156 2138 2134 2158 2158 2132 2157 2158 2134 2157 2157 2156 2135 2157 2121 2160 2160 2159 2159 2154 2159 2144 2122 2160 2122 2120 2032 2143 2142 2161 2161 2140 2162 2026 2162 2029 2164 2030 2030 2163 2163 2162 2164 2119 2164 2163 2028 2165 2165 2121 2165 2160 2166 2165 2166 2159 2154 2167 2167 2166 2170 2169 2169 2168 2168 2172 2172 2173 2173 2175 2175 2174 2174 2180 2180 2178 2178 2181 2181 2177 2177 2179 2179 2174 2174 2176 2176 2179 2180 2175 2173 2176 2176 2171 2171 2173 2172 2171 2182 2172 2182 2175 2177 2183 2183 2178 2183 2174 2174 2173 2172 2169 2170 2171 2170 2164 2163 2169 2169 2162 2162 2168 2168 2026 2168 2184 2184 2182 2184 2026 1953 2187 2187 2186 2186 2185 2185 1887 1886 2185 2185 1888 2186 1888 1889 2187 2196 2195 2195 2194 2194 2193 2193 2192 2192 2191 2191 2190 2190 2189 2189 2188 2188 1179 2188 1180 1956 2197 1955 2197 1957 1958 2198 1862 1863 2198 1511 1510 2199 1508 1507 2199 2199 1987 1987 1988 1988 1989 1932 1920 1932 1935 2200 1932 2200 1930 2200 1919 1928 2225 2225 2224 2224 2223 2223 2222 2222 2221 2221 2220 2220 2219 2219 2218 2218 2217 2217 2216 2216 2215 2215 2214 2214 2213 2213 2212 2212 2211 2211 2210 2210 2209 2209 2208 2208 2207 2207 2206 2206 2205 2205 2204 2204 2203 2203 2202 2202 2226 2226 2201 1744 1745 2227 1745 2227 1293 2227 1294 2247 2246 2246 2245 2245 2244 2244 2239 2239 2240 2238 2237 2239 2248 2248 2249 2249 2238 2237 2243 2243 2241 2241 2240 2240 2243 2241 2242 2242 2236 2236 2235 2235 2234 2234 2250 2250 2233 2233 2232 2232 2231 2231 2230 2230 2229 2229 2228 2251 2228 1546 2251 2236 2237 2240 2244 2239 2245 2244 2246 2262 1469 2261 2262 2261 2263 2263 2260 2260 2259 2259 2258 2258 2257 2257 2256 2256 2255 2255 2254 2254 2253 2253 2252 2252 2247 2245 2247";
		}

		public static String getWalkerNodes() {
			return "3230 3217 3235 3217 3234 3223 3241 3224 3249 3224 3255 3225 3263 3226 3257 3231 3256 3240 3251 3248 3247 3254 3247 3260 3233 3260 3223 3260 3240 3260 3245 3269 3235 3272 3237 3281 3236 3292 3237 3306 3234 3209 3235 3200 3244 3199 3244 3192 3250 3232 3246 3238 3251 3243 3250 3265 3256 3265 3256 3260 3259 3271 3255 3276 3260 3284 3249 3284 3244 3286 3246 3293 3255 3291 3245 3313 3230 3306 3220 3307 3223 3319 3216 3327 3210 3330 3223 3326 3207 3337 3253 3321 3263 3323 3268 3330 3262 3331 3256 3335 3248 3336 3241 3335 3226 3334 3226 3342 3225 3349 3225 3356 3218 3360 3213 3365 3207 3358 3201 3353 3206 3369 3198 3370 3190 3370 3195 3376 3185 3376 3213 3374 3219 3377 3226 3370 3235 3371 3230 3375 3243 3371 3244 3364 3248 3359 3255 3369 3254 3363 3248 3367 3264 3369 3262 3364 3271 3358 3271 3367 3267 3362 3275 3353 3275 3346 3272 3340 3278 3335 3274 3331 3280 3344 3281 3327 3285 3336 3295 3332 3291 3343 3285 3346 3283 3353 3295 3350 3302 3357 3301 3348 3306 3342 3300 3340 3306 3334 3295 3365 3297 3373 3290 3375 3283 3369 3275 3371 3291 3387 3291 3394 3288 3400 3291 3408 3288 3414 3282 3417 3277 3425 3288 3427 3279 3449 3284 3454 3279 3458 3296 3468 3293 3460 3308 3466 3302 3462 3305 3475 3293 3475 3295 3482 3282 3478 3273 3479 3275 3469 3268 3477 3265 3484 3268 3497 3258 3503 3279 3484 3292 3488 3291 3498 3307 3489 3289 3511 3290 3519 3272 3518 3267 3510 3252 3516 3281 3516 3280 3510 3261 3516 3245 3516 3237 3515 3223 3513 3212 3515 3201 3516 3231 3517 3237 3506 3247 3504 3243 3497 3253 3509 3194 3519 3181 3521 3168 3520 3157 3520 3146 3520 3137 3520 3255 3343 3265 3339 3282 3435 3271 3429 3284 3442 3283 3465 3235 3365 3227 3364 3236 3360 3254 3346 3256 3350 3248 3351 3244 3349 3243 3344 3235 3344 3233 3347 3263 3349 3239 3317 3244 3329 3237 3324 3241 3300 3248 3302 3253 3308 3259 3316 3261 3302 3259 3247 3262 3214 3218 3207 3210 3202 3206 3231 3216 3229 3218 3217 3214 3217 3214 3222 3214 3212 3213 3225 3208 3226 3213 3209 3209 3208 3205 3208 3221 3216 3200 3361 3184 3366 3177 3360 3167 3359 3156 3358 3156 3368 3163 3367 3160 3377 3171 3377 3173 3385 3168 3390 3160 3385 3147 3389 3158 3396 3165 3401 3161 3406 3161 3419 3146 3398 3135 3399 3135 3409 3124 3401 3131 3391 3120 3408 3113 3418 3124 3423 3126 3410 3133 3427 3139 3421 3132 3418 3149 3420 3156 3424 3169 3431 3163 3440 3172 3438 3172 3452 3181 3451 3176 3460 3165 3464 3158 3457 3163 3454 3152 3452 3139 3439 3126 3435 3118 3429 3117 3442 3128 3445 3119 3452 3129 3458 3106 3458 3177 3428 3178 3418 3176 3412 3180 3407 3186 3401 3193 3408 3201 3405 3209 3403 3210 3396 3210 3388 3209 3379 3199 3390 3217 3390 3226 3389 3232 3389 3238 3388 3246 3388 3255 3399 3242 3404 3235 3399 3232 3412 3225 3410 3218 3409 3208 3410 3208 3419 3198 3425 3199 3412 3195 3430 3208 3429 3216 3420 3217 3431 3228 3428 3221 3424 3227 3417 3239 3430 3243 3419 3242 3413 3243 3430 3256 3429 3243 3440 3236 3441 3234 3434 3221 3436 3210 3438 3197 3442 3189 3451 3212 3445 3206 3452 3212 3456 3209 3465 3204 3468 3220 3465 3219 3454 3220 3447 3227 3465 3230 3455 3239 3465 3244 3462 3245 3452 3246 3448 3247 3469 3243 3478 3246 3488 3237 3495 3259 3468 3260 3459 3196 3456 3195 3465 3194 3476 3192 3488 3185 3489 3173 3491 3164 3506 3150 3503 3162 3491 3149 3484 3164 3477 3156 3487 3171 3498 3167 3482 3152 3496 3146 3510 3156 3509 3142 3478 3153 3470 3175 3471 3178 3482 3165 3471 3133 3511 3130 3500 3135 3494 3132 3486 3130 3476 3118 3477 3111 3472 3119 3467 3116 3490 3124 3516 3118 3517 3117 3508 3110 3511 3111 3502 3101 3508 3097 3501 3100 3493 3099 3485 3098 3477 3092 3484 3084 3485 3077 3484 3081 3465 3087 3459 3095 3451 3084 3450 3093 3444 3086 3440 3098 3436 3090 3430 3101 3427 3091 3419 3102 3420 3098 3411 3084 3422 3257 3410 3264 3415 3262 3424 3270 3409 3269 3401 3274 3393 3278 3387 3287 3361 3110 3482 3125 3494 3118 3517 3084 3494 3085 3501 3090 3506 3086 3512 3072 3519 3070 3506 3079 3519 3103 3434 3231 3226 3225 3231 3221 3237 3210 3237 3213 3251 3216 3260 3216 3269 3213 3276 3203 3279 3195 3277 3187 3281 3199 3238 3203 3252 3200 3246 3191 3249 3192 3242 3182 3241 3188 3236 3180 3234 3193 3222 3182 3222 3189 3210 3172 3211 3169 3223 3182 3216 3167 3234 3176 3228 3173 3242 3180 3251 3173 3257 3184 3260 3168 3260 3158 3257 3165 3242 3156 3236 3148 3245 3160 3229 3153 3244 3150 3227 3146 3234 3160 3217 3147 3218 3135 3209 3152 3206 3144 3195 3147 3201 3154 3193 3154 3187 3156 3170 3155 3161 3153 3152 3166 3176 3174 3174 3181 3170 3189 3166 3195 3163 3203 3159 3211 3156 3218 3152 3197 3171 3218 3145 3242 3185 3241 3178 3238 3170 3239 3161 3239 3153 3229 3151 3162 3150 3176 3150 3188 3150 3201 3150 3210 3149 3155 3180 3142 3215 3126 3214 3118 3216 3111 3219 3103 3225 3091 3226 3098 3234 3083 3234 3099 3243 3091 3248 3080 3247 3127 3224 3140 3226 3169 3250 3215 3245 3178 3269 3176 3282 3166 3280 3160 3273 3159 3264 3108 3166 3112 3176 3111 3187 3112 3201 3114 3212 3116 3160 3113 3150 3102 3145 3098 3154 3098 3164 3112 3228 3122 3227 3071 3245 3076 3253 3101 3250 3101 3262 3101 3272 3090 3261 3090 3270 3082 3267 3074 3265 3106 3285 3108 3293 3116 3296 3125 3296 3135 3294 3143 3295 3151 3293 3168 3286 3142 3251 3136 3259 3132 3269 3132 3276 3132 3288 3114 3259 3123 3260 3148 3259 3158 3285 3162 3292 3163 3299 3186 3290 3187 3298 3183 3306 3175 3310 3169 3303 3174 3318 3166 3323 3158 3327 3164 3330 3184 3431 3184 3439 3156 3480 3173 3506 3183 3506 3181 3496 3146 3493 3229 3447 3237 3456 3144 3430 3148 3444 3152 3381 3175 3394 3165 3412 3170 3420 3155 3413 3144 3456 3134 3457 3177 3381 3158 3499 3219 3378 2522 3601 3225 3427 3194 3331 3184 3326 3173 3330 3189 3316 3202 3314 3198 3324 3165 3160 3213 3165 3232 3179 3220 3184 3214 3175 3226 3171 3226 3163 3227 3157 3217 3159 3281 3318 3273 3308 3285 3306 3277 3296 3270 3294 3284 3290 3278 3284 3281 3273 3282 3261 3281 3249 3283 3237 3289 3249 3296 3262 3297 3278 3297 3293 3297 3306 3305 3264 3311 3273 3314 3283 3314 3293 3314 3306 3313 3315 3301 3323 3290 3323 3317 3322 3324 3314 3326 3304 3326 3295 3325 3285 3323 3276 3318 3263 3314 3250 3301 3251 3301 3241 3292 3237 3306 3233 3296 3226 3307 3218 3299 3272 3291 3270 3283 3227 3271 3226 3278 3218 3290 3215 3298 3207 3313 3209 3308 3198 3299 3195 3292 3190 3292 3181 3294 3170 3282 3180 3280 3194 3288 3196 3277 3206 3269 3215 3266 3197 3264 3185 3260 3173 3275 3167 3267 3151 3275 3155 3283 3151 3286 3138 3297 3150 3299 3137 3308 3151 3310 3140 3320 3152 3322 3162 3309 3167 3324 3170 3306 3177 3297 3181 3308 3188 3321 3185 3326 3192 3318 3203 3259 3159 3276 3143 3325 3143 3305 3158 3273 3241 3274 3255 3273 3269 3268 3280 3271 3318 3275 3324 3179 3161 3167 3166 3187 3174 3160 3197 3174 3189 3181 3180 3164 3186 3077 3458 3072 3452 3069 3447 3067 3440 3060 3432 3065 3422 3073 3418 3070 3494 3070 3477 3069 3465 3077 3471 3067 3430 3083 3414 3056 3418 3050 3410 3043 3412 3039 3417 3034 3424 3024 3426 3017 3424 3008 3430 2993 3432 2985 3427 2984 3419 2976 3414 2966 3412 2962 3404 2962 3387 2955 3418 2947 3424 2944 3434 2943 3441 2941 3452 2940 3460 2940 3471 2948 3477 2950 3486 2952 3494 2953 3500 2940 3481 2938 3493 2935 3501 2936 3514 2981 3435 2972 3440 2964 3445 2971 3455 2962 3453 2958 3463 2959 3480 2968 3491 2977 3514 2969 3470 2973 3483 2978 3497 2980 3510 3050 3455 3049 3464 3049 3472 3048 3482 3048 3494 3049 3503 3041 3472 3034 3471 3028 3466 3019 3459 3011 3456 3028 3476 3017 3485 3021 3493 3028 3500 3033 3512 3032 3519 3021 3526 3013 3533 3008 3537 2998 3522 2994 3513 2991 3501 2991 3491 2989 3479 2982 3473 2981 3463 2987 3454 2996 3450 3001 3455 3021 3434 3033 3446 3038 3456 3043 3464 3038 3431 3052 3429 2972 3504 2978 3446 3047 3420 3046 3440 3057 3443 3058 3456 3008 3528 2967 3425 2955 3430 2983 3400 3002 3399 3019 3396 2992 3411 3000 3417 3015 3409 3027 3411 3036 3390 3050 3392 3061 3400 3068 3409 2973 3403 3154 3310 3144 3317 3144 3328 3145 3337 3144 3348 3142 3359 3140 3368 3136 3378 3130 3383 3120 3384 3124 3380 3132 3369 3133 3360 3132 3348 3132 3339 3130 3326 3121 3319 3135 3315 3127 3308 3114 3306 3101 3304 3097 3317 3087 3304 3086 3316 3074 3306 3074 3329 3067 3328 3057 3320 3062 3336 3071 3343 3064 3348 3070 3361 3070 3372 3074 3383 3065 3393 3077 3390 3089 3396 3095 3403 3098 3392 3110 3388 3072 3401 3077 3320 3080 3403 3142 3308 3164 3313 3156 3319 3157 3340 3171 3337 3183 3336 3096 3212 3077 3279 3095 3295 3080 3300 3071 3292 3088 3286 3080 3290 3088 3326 3104 3327 3119 3326 3109 3316 3106 3333 3107 3340 3106 3349 3098 3335 3097 3348 3090 3349 3088 3340 3089 3334 3085 3357 3085 3365 3085 3374 3089 3382 3095 3381 3090 3369 3102 3380 3111 3378 3117 3377 3121 3368 3125 3357 3123 3348 3120 3340 3118 3332 3113 3335 3114 3347 3096 3341 3068 3278 3059 3275 3060 3311 3061 3303 3053 3300 3044 3301 3044 3313 3041 3322 3050 3317 3032 3317 3031 3324 3018 3318 3021 3322 3008 3316 3007 3309 2996 3313 3003 3303 2996 3297 3007 3292 3003 3286 3008 3276 3016 3273 3017 3263 3016 3250 3026 3240 3026 3228 3025 3220 3025 3213 3024 3204 3032 3201 3028 3198 3034 3191 3034 3233 3044 3233 3039 3239 3039 3249 3038 3261 3052 3270 3042 3272 3032 3273 3027 3265 3032 3254 3048 3266 3051 3251 3059 3260 3056 3270 3009 3267 3017 3240 3244 3320 2962 3378 2961 3368 2961 3358 2961 3348 2962 3342 2971 3340 3003 3324 2995 3324 2988 3318 2977 3318 2964 3315 2954 3313 2944 3314 2947 3322 2938 3325 2945 3330 2936 3339 2944 3339 2939 3347 2944 3353 2947 3368 2947 3377 2940 3375 2936 3369 2937 3358 2954 3381 2973 3377 2980 3375 2988 3371 2994 3366 3001 3360 3003 3350 3004 3338 3004 3331 3011 3328 3032 3331 3042 3331 3050 3331 3052 3341 3052 3349 3052 3359 3056 3370 3012 3361 3021 3361 3028 3356 3038 3357 3040 3368 3052 3366 3039 3351 3030 3345 3038 3337 3027 3367 2993 3376 2992 3384 2982 3387 2972 3387 3023 3330 2948 3358 2955 3364 2984 3303 2991 3289 2973 3298 2979 3286 2963 3291 2960 3301 2948 3300 2952 3290 2934 3297 2930 3309 2922 3303 2911 3299 2910 3308 2922 3314 2920 3324 2943 3292 2949 3280 2941 3274 2951 3268 2939 3264 2937 3258 2921 3260 2958 3278 2967 3278 2963 3268 2974 3274 2989 3278 2999 3278 2996 3269 3006 3262 2985 3267 2997 3255 2979 3261 2996 3246 2989 3255 3006 3251 2963 3259 2973 3254 2975 3243 2969 3238 2975 3229 2982 3238 2969 3229 2964 3222 2957 3217 2956 3208 2952 3227 2946 3233 2941 3244 2954 3245 2963 3254 2938 3229 2929 3220 2919 3230 2958 3196 2973 3195 2972 3216 2976 3207 2982 3220 2998 3237 3003 3225 2993 3217 3004 3214 3012 3215 2997 3207 2986 3210 2986 3198 2985 3182 2994 3197 3005 3194 3003 3181 2993 3187 2993 3166 3002 3173 3001 3161 3010 3154 2997 3147 2994 3137 3000 3129 2996 3121 2993 3113 2995 3127 3019 3159 3008 3168 3022 3175 3015 3197 3020 3189 2985 3172 2991 3226 2978 3196 2915 3268 2906 3279 2902 3292 2920 3293 2939 3305 3007 3238 3136 3241 2867 3435 2860 3445 2861 3455 2853 3467 2852 3480 2856 3489 2858 3501 2852 3510 2838 3508 2834 3502 2839 3492 2846 3484 2843 3473 2836 3466 2844 3458 2842 3450 2850 3445 2851 3434 2850 3426 2837 3434 2827 3436 2819 3438 2811 3437 2803 3434 2794 3433 2785 3437 2777 3443 2769 3452 2765 3459 2758 3465 2753 3473 2744 3478 2766 3472 2774 3462 2781 3456 2798 3444 2789 3453 2793 3461 2781 3469 2770 3477 2781 3483 2774 3492 2779 3503 2790 3480 2790 3470 2800 3475 2812 3474 2816 3479 2817 3469 2823 3460 2830 3453 2816 3446 2804 3447 2806 3457 2840 3441 2832 3477 2828 3488 2820 3498 2809 3499 2801 3495 2790 3499 2792 3512 2801 3522 2810 3522 2823 3525 2830 3511 2831 3529 2843 3527 2861 3525 2870 3517 2871 3503 2868 3493 2864 3484 2864 3472 2868 3466 2871 3449 2858 3484 2862 3496 2862 3508 2871 3414 2884 3398 2889 3388 2903 3370 2910 3360 2913 3349 2917 3336 2928 3356 2923 3345 2923 3360 2913 3365 2919 3329 2906 3327 2895 3378 2869 3425 2874 3406 2807 3465 2816 3458 2825 3448 2784 3446 2734 3481 2934 3451 2930 3450 2918 3453 2883 3446 2877 3438 2905 3456 2890 3456 2892 3468 2896 3478 2899 3485 2902 3491 2907 3499 2911 3508 2904 3511 2891 3501 2896 3495 3114 3310 2720 3485 2720 3493 2708 3484 2696 3483 2680 3484 2674 3475 2667 3464 2660 3453 2650 3443 2639 3418 2640 3405 2638 3391 2639 3375 2629 3373 2631 3359 2630 3344 2625 3333 2636 3334 2617 3337 2610 3337 2611 3331 2602 3334 2602 3324 2602 3312 2601 3301 2607 3295 2598 3295 2590 3295 2581 3296 2620 3297 2633 3302 2630 3287 2636 3279 2635 3270 2642 3306 2652 3304 2666 3304 2680 3304 2688 3312 2693 3323 2698 3334 2706 3343 2719 3347 2704 3355 2708 3362 2713 3354 2707 3374 2698 3365 2697 3378 2702 3387 2688 3385 2682 3395 2711 3397 2681 3379 2670 3379 2662 3388 2655 3398 2646 3401 2649 3415 2659 3407 2719 3409 2711 3416 2705 3439 2697 3428 2684 3423 2674 3417 2666 3410 2674 3392 2682 3409 2715 3453 2718 3467 2719 3477 2745 3463 2739 3456 2727 3456 2702 3421 2767 3436 2754 3430 2741 3432 2728 3437 2727 3448 2752 3457 2761 3452 2694 3446 2682 3452 2673 3457 2666 3454 2674 3446 2683 3439 2682 3430 2692 3417 2677 3364 2665 3362 2681 3353 2689 3344 2696 3351 2689 3360 2717 3390 2711 3381 2727 3404 2727 3396 2737 3389 2744 3398 2737 3375 2734 3361 2729 3350 2725 3343 2639 3290 2648 3283 2647 3275 2647 3265 2634 3264 2600 3286 2604 3278 2596 3271 2593 3264 2600 3262 2591 3256 2584 3249 2574 3245 2595 3241 2603 3241 2614 3240 2623 3238 2631 3231 2643 3226 2599 3228 2734 3408 2738 3417 2740 3426 2726 3418 2726 3429 2714 3429 2721 3437 2706 3430 2715 3442 2701 3453 2684 3473 2697 3469 2708 3469 2748 3419 2750 3409 2755 3420 2765 3428 2775 3430 2734 3341 2728 3333 2719 3333 2713 3339 2711 3329 2606 3342 2607 3359 2610 3371 2619 3375 2622 3387 2627 3395 2634 3398 2650 3388 2656 3380 2672 3352 2677 3342 2682 3330 2705 3316 2714 3321 2698 3322 2698 3313 2712 3310 2707 3298 2704 3307 2695 3390 2711 3408 2735 3398 2743 3411 2687 3377 2687 3370 2667 3372 2648 3381 2675 3438 2666 3448 2645 3423 2664 3394 2665 3402 2527 3152 2700 3300 2631 3257 2627 3248 2622 3254 2619 3245 2612 3250 2607 3248 2599 3250 2611 3260 2592 3251 2603 3234 2609 3233 2606 3225 2615 3227 2619 3234 2622 3228 2632 3225 2630 3240 2633 3247 2635 3237 2640 3232 2619 3222 2625 3215 2613 3216 2588 3232 2584 3240 2620 3204 2614 3198 2616 3188 2613 3179 2617 3170 2617 3158 2616 3149 2616 3137 2618 3125 2618 3115 2612 3108 2609 3100 2599 3094 2600 3084 2607 3090 2590 3094 2583 3095 2575 3095 2571 3087 2561 3087 2551 3088 2546 3090 2540 3089 2541 3082 2547 3081 2555 3084 2567 3083 2567 3077 2573 3079 2554 3077 2558 3073 2566 3073 2573 3074 2582 3074 2590 3074 2596 3080 2592 3085 2607 3082 2613 3087 2612 3094 2576 3102 2568 3098 2560 3095 2552 3095 2554 3101 2560 3103 2548 3103 2540 3102 2540 3095 2535 3089 2534 3095 2535 3080 2541 3074 2536 3074 2546 3071 2553 3074 2601 3079 2593 3074 2599 3072 2622 3195 2618 3177 2621 3153 2622 3142 2624 3133 2625 3120 2619 3106 2620 3164 2624 3110 2623 3099 2618 3092 2625 3089 2621 3084 2632 3084 2626 3080 2620 3072 2628 3072 2613 3064 2612 3389 2604 3389 2604 3381 2611 3382 2602 3373 2596 3382 2594 3390 2592 3376 2585 3384 2574 3390 2582 3394 2576 3371 2576 3363 2577 3354 2580 3344 2573 3347 2568 3354 2580 3335 2576 3328 2576 3318 2566 3327 2573 3327 2564 3323 2564 3317 2567 3316 2572 3320 2572 3315 2562 3309 2572 3311 2558 3298 2562 3280 2558 3293 2559 3285 2570 3278 2579 3280 2576 3271 2576 3263 2567 3261 2555 3257 2556 3249 2558 3247 2560 3254 2567 3254 2574 3251 2582 3256 2588 3260 2600 3220 2509 3156 2513 3156 2514 3152 2520 3152 2526 3152 2532 3152 2538 3152 2539 3145 2535 3148 2532 3142 2539 3142 2543 3145 2543 3152 2545 3155 2539 3156 2539 3161 2544 3162 2539 3172 2538 3175 2530 3174 2527 3177 2519 3175 2515 3175 2514 3177 2517 3182 2527 3182 2520 3185 2507 3185 2501 3189 2496 3190 2521 3178 2514 3182 2524 3176 2544 3172 2544 3167 2513 3185 2511 3200 2519 3201 2504 3204 2499 3198 2505 3196 2523 3197 2523 3207 2530 3201 2533 3209 2538 3204 2541 3210 2546 3206 2552 3213 2555 3207 2560 3212 2558 3219 2567 3218 2569 3212 2563 3205 2574 3217 2573 3225 2579 3222 2580 3230 2584 3225 2589 3227 2592 3223 2604 3218 2592 3217 2586 3218 2580 3218 2578 3212 2570 3205 2565 3196 2576 3201 2580 3208 2619 3211 2622 3185 2623 3177 2585 3197 2594 3198 2591 3194 2601 3196 2601 3201 2609 3198 2611 3191 2604 3194 2598 3189 2590 3188 2578 3193 2576 3187 2583 3187 2578 3182 2570 3181 2569 3189 2564 3192 2564 3182 2556 3196 2549 3199 2542 3199 2535 3196 2529 3195 2556 3175 2568 3173 2575 3176 2581 3174 2573 3170 2568 3164 2571 3160 2564 3154 2571 3152 2587 3174 2595 3174 2598 3183 2609 3185 2610 3130 2602 3133 2605 3127 2596 3127 2630 3218 2628 3207 2628 3198 2635 3204 2650 3221 2652 3213 2646 3207 2640 3203 2600 3118 2592 3134 2588 3128 2591 3120 2580 3132 2578 3126 2583 3120 2572 3134 2572 3144 2569 3136 2568 3147 2582 3144 2590 3144 2598 3143 2602 3170 2607 3166 2607 3160 2607 3153 2607 3177 2603 3187 2575 3118 2571 3128 2604 3121 2609 3125 2611 3120 2609 3114 2604 3109 2597 3113 2590 3112 2586 3116 2580 3113 2584 3109 2582 3103 2594 3096 2598 3104 2598 3096 2595 3102 2601 3104 2605 3101 2613 3080 2613 3073 2606 3072 2570 3123 2565 3129 2560 3133 2555 3154 2554 3165 2562 3164 2573 3112 2579 3109 2567 3109 2562 3113 2558 3109 2554 3113 2549 3109 2545 3113 2550 3121 2559 3125 2565 3124 2555 3139 2554 3146 2554 3130 2545 3127 2544 3120 2539 3131 2541 3123 2539 3117 2540 3112 2608 3281 2611 3276 2613 3269 2619 3267 2620 3274 2620 3279 2616 3279 2620 3285 2614 3287 2608 3288 2594 3282 2589 3276 2592 3270 2580 3312 2584 3305 2586 3298 2587 3291 2584 3284 2553 3297 2913 3317 2896 3325 2894 3362 2895 3350 2904 3347 2908 3339 3092 3374 3107 3442 3106 3450 3151 3462 3118 3458 2653 3313 2645 3319 2642 3327 2661 3312 2657 3297 2666 3293 2668 3286 2660 3285 2657 3279 2676 3286 2677 3294 2692 3301 2693 3307 2452 3379 2460 3384 2466 3388 2474 3387 2484 3387 2491 3376 2491 3368 2495 3359 2500 3356 2508 3352 2519 3352 2527 3358 2534 3366 2526 3387 2517 3390 2508 3390 2499 3388 2533 3373 2530 3381 2536 3356 2541 3370 2538 3380 2533 3388 2523 3395 2512 3397 2502 3398 2527 3350 2514 3346 2501 3348 2494 3351 2485 3360 2492 3386 2457 3369 2461 3361 2471 3358 2478 3359 2523 3496 2524 3480 2538 3476 2545 3473 2530 3474 2545 3465 2545 3454 2551 3447 2554 3434 2558 3424 2562 3415 2565 3407 2569 3398 2525 3488 2521 3504 2518 3507 2518 3513 2518 3521 2515 3529 2515 3537 2515 3548 2518 3558 2526 3559 2533 3559 2535 3567 2512 3562 2511 3570 2513 3577 2517 3583 2526 3585 2535 3582 2536 3575 2542 3580 2549 3577 2551 3570 2550 3564 2550 3556 2549 3548 2549 3540 2540 3539 2532 3540 2524 3540 2510 3553 2508 3541 2507 3532 2510 3521 2502 3526 2502 3538 2502 3548 2507 3560 2504 3518 2496 3544 2496 3532 2496 3521 2489 3528 2491 3538 2483 3529 2564 3344 2552 3345 2543 3350 2533 3349 2542 3342 2529 3342 2533 3340 2520 3341 2506 3340 2495 3343 2484 3353 2487 3344 2555 3237 2554 3230 2563 3227 2631 3126 2627 3136 2628 3116 2618 3079 2649 3232 2658 3228 2448 3361 2445 3370 2439 3378 2436 3369 2439 3362 2454 3352 2444 3350 2450 3340 2456 3338 2450 3332 2467 3341 2477 3341 2473 3350 2479 3352 2462 3352 2459 3345 2480 3337 2492 3339 2544 3360 2548 3369 2558 3366 2563 3360 2569 3378 2566 3391 2561 3398 2553 3408 2551 3416 2576 3381 2584 3371 2542 3257 2543 3248 2532 3256 2533 3242 2525 3250 2521 3236 2514 3236 2508 3231 2503 3236 2499 3239 2501 3243 2508 3240 2516 3242 2516 3251 2510 3251 2510 3259 2521 3260 2527 3255 2499 3270 2499 3274 2489 3275 2481 3275 2474 3275 2469 3274 2477 3270 2486 3266 2489 3257 2482 3249 2476 3244 2488 3231 2486 3240 2477 3234 2499 3227 2480 3222 2491 3218 2483 3212 2504 3250 2497 3262 2503 3260 2489 3250 2491 3242 2494 3235 2496 3245 2492 3269 2483 3272 2458 3272 2449 3279 2449 3290 2450 3300 2593 3366 2530 3418 2544 3433 2538 3443 2531 3435 2528 3426 2520 3418 2534 3408 2535 3400 2521 3401 2544 3409 2546 3403 2658 3362 2721 3327 2736 3334 2693 3295 2686 3304 2683 3317 2685 3324 2683 3339 2673 3338 2652 3462 2645 3466 2639 3462 2645 3458 2650 3454 2650 3447 2643 3438 2638 3429 2637 3454 2638 3445 2642 3442 2631 3461 2624 3457 2620 3464 2615 3471 2615 3481 2608 3471 2601 3472 2598 3482 2604 3488 2611 3488 2534 3249 2549 3258 2548 3251 2550 3260 2536 3261 2528 3259 2528 3247 2524 3242 2507 3405 2516 3406 2679 3495 2676 3507 2669 3511 2658 3511 2649 3512 2638 3510 2652 3503 2729 3487 2732 3494 2737 3499 2736 3507 2735 3515 2736 3521 2736 3528 2736 3544 2736 3552 2731 3539 2727 3543 2716 3544 2704 3544 2688 3546 2685 3548 2679 3547 2670 3548 2668 3555 2667 3559 2658 3558 2654 3565 2654 3572 2648 3577 2648 3584 2648 3597 2648 3608 2652 3612 2658 3615 2654 3623 2654 3629 2657 3633 2663 3638 2661 3645 2655 3649 2655 3657 2647 3659 2642 3655 2635 3659 2629 3658 2619 3660 2617 3665 2620 3673 2626 3672 2636 3673 2640 3671 2645 3664 2651 3662 2659 3662 2662 3668 2660 3674 2660 3681 2665 3685 2668 3690 2668 3697 2662 3702 2657 3700 2650 3699 2646 3693 2639 3687 2643 3680 2625 3680 2623 3690 2620 3693 2619 3682 2614 3682 2613 3687 2667 3706 2663 3712 2656 3711 2655 3705 2651 3713 2646 3712 2640 3712 2636 3712 2665 3718 2667 3715 2657 3718 2671 3720 2660 3722 2674 3717 2677 3722 2673 3727 2677 3730 2671 3734 2666 3730 2661 3734 2736 3536 2696 3544 2711 3545 2651 3521 2655 3529 2665 3535 2669 3541 2675 3539 2682 3540 2681 3555 2674 3556 2691 3553 2688 3563 2679 3564 2680 3717 2681 3727 2678 3735 2608 3495 2710 3492 2711 3503 2723 3504 2728 3498 2731 3510 2717 3507 2705 3505 2697 3504 2697 3490 2697 3497 2710 3510 2676 3466 2691 3454 2469 3336 2653 3638 2663 3630 2662 3621 2670 3632 2678 3634 2685 3632 2696 3627 2704 3630 2713 3633 2715 3626 2718 3619 2729 3620 2734 3624 2740 3627 2750 3628 2735 3632 2730 3628 2725 3625 2718 3632 2713 3640 2706 3637 2698 3635 2692 3634 2684 3639 2672 3636 2676 3627 2689 3624 2699 3621 2707 3624 2711 3616 2726 3616 2683 3623 2704 3615 2692 3615 2686 3629 2682 3615 2717 3613 2723 3639 2730 3637 2740 3637 2675 3618 2667 3620 2693 3640 2647 3617 2647 3624 2647 3632 2663 3613 2670 3613 2676 3612 2642 3612 2640 3621 2639 3629 2632 3624 2634 3616 2627 3619 2617 3617 2621 3611 2620 3625 2606 3622 2610 3612 2612 3624 2614 3609 2602 3614 2628 3610 2609 3617 2636 3607 2561 3382 2557 3389 2551 3396 2912 3517 2912 3527 2920 3531 2920 3540 2911 3547 2900 3546 2890 3546 2883 3547 2877 3547 2729 3328 2658 3237 2559 3261 2492 3254 2439 3086 2452 3088 2452 3094 2450 3099 2447 3105 2443 3109 2439 3114 2439 3117 2433 3120 2428 3122 2427 3129 2429 3134 2436 3139 2440 3143 2446 3147 2449 3151 2454 3158 2460 3162 2460 3168 2458 3175 2460 3181 2466 3188 2471 3192 2473 3199 2479 3205 2446 3087 2651 3274 2486 3184 2485 3173 2480 3167 2480 3156 2480 3151 2481 3142 2492 3125 2503 3128 2514 3131 2526 3130 2534 3124 2524 3115 2518 3116 2514 3121 2513 3127 2521 3123 2517 3105 2524 3102 2520 3094 2523 3088 2529 3117 2529 3117 2483 3131 2489 3191 2524 3081 2528 3071 2532 3068 2541 3066 2548 3065 2559 3065 2566 3065 2576 3064 2583 3064 2597 3063 2604 3063 2589 3062 2951 3270";
		}

	}

}
