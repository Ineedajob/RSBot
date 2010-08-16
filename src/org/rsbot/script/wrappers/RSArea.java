package org.rsbot.script.wrappers;

/**
 * An area of RSTiles.
 * 
 * @author Jacmob
 */
public class RSArea {

	private final int x, y, width, height, plane;

	public RSArea(RSTile sw, RSTile ne) {
		this(sw, ne, 0);
	}

	public RSArea(RSTile sw, RSTile ne, int plane) {
		this.x = sw.getX();
		this.y = sw.getY();
		this.width = ne.getX() - sw.getX() + 1;
		this.height = ne.getY() - sw.getY() + 1;
		this.plane = plane;
	}

	public int getPlane() {
		return plane;
	}

	public RSTile getCentralTile() {
		return new RSTile(x + (width - 1) / 2, y + (height - 1) / 2);
	}

	public RSTile getNearestTile(RSTile to) {
		double dist = 999;
		RSTile nearest = null;
		RSTile[] tiles = getTileArray();
		for (RSTile tile : tiles) {
			double d = Math.hypot((to.getX() - x), (to.getY() - y));
			if (d < dist) {
				dist = d;
				nearest = tile;
			}
		}
		return nearest;
	}

	public RSTile[][] getTiles() {
		RSTile[][] tiles = new RSTile[width][height];
		for (int i = 0; i < width; ++i) {
			for (int j = 0; j < height; ++j) {
				tiles[i][j] = new RSTile(x + i, y + j);
			}
		}
		return tiles;
	}

	public RSTile[] getTileArray() {
		RSTile[] tiles = new RSTile[width * height];
		for (int i = 0; i < width; ++i) {
			for (int j = 0; j < height; ++j) {
				tiles[i * height + j] = new RSTile(x + i, y + j);
			}
		}
		return tiles;
	}

	public boolean contains(int x, int y) {
		return (x >= this.x) && (x <= (this.x + this.width - 1))
				&& (y >= this.y) && (y <= (this.y + this.height - 1));
	}

	public boolean contains(int x, int y, int plane) {
		return this.plane == plane && (x >= this.x)
				&& (x <= (this.x + this.width - 1)) && (y >= this.y)
				&& (y <= (this.y + this.height - 1));
	}

	public boolean contains(RSTile tile) {
		return contains(tile.getX(), tile.getY());
	}

	public boolean contains(RSTile tile, int plane) {
		return contains(tile.getX(), tile.getY(), plane);
	}

}