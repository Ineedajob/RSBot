package org.rsbot.script.wrappers;

import java.awt.Polygon;
import java.awt.Rectangle;
import java.util.ArrayList;

/**
 * Represents a shape made of RSTiles.
 *
 * @author SpeedWing
 */
public class RSArea {

	private Polygon area;
	private int plane;

	/**
	 * @param tiles An Array containing of <b>RSTiles</b> forming a polygon shape.
	 * @param plane The plane of the <b>RSArea</b>.
	 */
	public RSArea(RSTile[] tiles, int plane) {
		this.area = tileArrayToPolygon(tiles);
		this.plane = plane;
	}

	/**
	 * @param tiles An Array containing of <b>RSTiles</b> forming a polygon shape.
	 */
	public RSArea(RSTile[] tiles) {
		this(tiles, 0);
	}

	/**
	 * @param sw	The south west <b>RSTile</b> of the <b>RSArea</b>
	 * @param ne	The north east <b>RSTile</b> of the <b>RSArea</b>
	 * @param plane The plane of the <b>RSArea</b>.
	 */
	public RSArea(RSTile sw, RSTile ne, int plane) {
		this(new RSTile[]{sw, new RSTile(ne.getX() + 1, sw.getY()),
				new RSTile(ne.getX() + 1, ne.getY() + 1),
				new RSTile(sw.getX(), ne.getY() + 1)}, plane);
	}

	/**
	 * @param sw The south west <b>RSTile</b> of the <b>RSArea</b>
	 * @param ne The north east <b>RSTile</b> of the <b>RSArea</b>
	 */
	public RSArea(RSTile sw, RSTile ne) {
		this(sw, ne, 0);
	}

	/**
	 * @param x The x location of the <b>RSTile</b> that will be checked.
	 * @param y The y location of the <b>RSTile</b> that will be checked.
	 * @return True if the <b>RSArea</b> contains the given <b>RSTile</b>.
	 */
	public boolean contains(int x, int y) {
		return this.contains(new RSTile(x, y));
	}

	/**
	 * @param tiles The <b>RSTile(s)</b> that will be checked.
	 * @return True if the <b>RSArea</b> contains the given <b>RSTile(s)</b>.
	 */
	public boolean contains(RSTile... tiles) {
		RSTile[] areaTiles = this.getTileArray();
		for (RSTile check : tiles) {
			for (RSTile space : areaTiles) {
				if (check.equals(space)) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * @return The nearest <b>RSTile</b> in the <b>RSArea</b> to the
	 * given <b>RSTile</b>.
	 */
	public RSTile getNearestTile(RSTile base) {
		RSTile[] tiles = this.getTileArray();
		RSTile cur = null;
		double dist = -1;
		for (RSTile tile : tiles) {
			double distTmp = distanceBetween(tile, base);
			if (cur == null) {
				dist = distTmp;
				cur = tile;
			} else if (distTmp < dist) {
				cur = tile;
				dist = distTmp;
			}
		}
		return cur;
	}

	/**
	 * @return The <b>RSTiles</b> the <b>RSArea</b> contains.
	 */
	public RSTile[] getTileArray() {
		ArrayList<RSTile> list = new ArrayList<RSTile>();
		for (int x = this.getX(); x <= (this.getX() + this.getWidth()); x++) {
			for (int y = this.getY(); y <= (this.getY() + this.getHeight()); y++) {
				if (this.area.contains(x, y)) {
					list.add(new RSTile(x, y));
				}
			}
		}
		RSTile[] tiles = new RSTile[list.size()];
		for (int i = 0; i < list.size(); i++)
			tiles[i] = list.get(i);
		return tiles;
	}

	/**
	 * @return The <b>RSTiles</b> the <b>RSArea</b> contains.
	 */
	public RSTile[][] getTiles() {
		RSTile[][] tiles = new RSTile[this.getWidth() + 1][this.getHeight() + 1];
		for (int i = 0; i < this.getHeight(); ++i) {
			for (int j = 0; j < this.getWidth(); ++j) {
				if (this.area.contains(this.getX() + i, this.getY() + j)) {
					tiles[i][j] = new RSTile(this.getX() + i, this.getY() + j);
				}
			}
		}
		return tiles;
	}

	/**
	 * @return The distance between the the <b>RSTile</b> that's most East and
	 *         the <b>RSTile</b> that's most West.
	 */
	public int getWidth() {
		return this.area.getBounds().width;
	}

	/**
	 * @return The distance between the the <b>RSTile</b> that's most South and
	 *         the <b>RSTile</b that's most North.
	 */
	public int getHeight() {
		return this.area.getBounds().height;
	}

	/**
	 * @return The X axle of the <b>RSTile</b> that's most West.
	 */
	public int getX() {
		return this.area.getBounds().x;
	}

	/**
	 * @return The Y axle of the <b>RSTile</b> that's most South.
	 */
	public int getY() {
		return this.area.getBounds().y;
	}

	/**
	 * @return The plane of the <b>RSArea</b>.
	 */
	public int getPlane() {
		return plane;
	}

	/**
	 * @return The bounding box of the <b>RSArea</b>.
	 */
	public Rectangle getBounds() {
		return new Rectangle(this.area.getBounds().x + 1,
				this.area.getBounds().y + 1, this.getWidth(), this.getHeight());
	}

	/**
	 * Converts an shape made of <b>RSTile</b> to a polygon.
	 *
	 * @param tiles The <b>RSTile</b> of the Polygon.
	 * @return The Polygon of the <b>RSTile</b>.
	 */
	private Polygon tileArrayToPolygon(RSTile[] tiles) {
		Polygon poly = new Polygon();
		for (RSTile t : tiles) {
			poly.addPoint(t.getX(), t.getY());
		}
		return poly;
	}

	/**
	 * @param curr first <b>RSTile</b>
	 * @param dest second <b>RSTile</b>
	 * @return the distance between the first and the second rstile
	 */
	private double distanceBetween(RSTile curr, RSTile dest) {
		return Math.sqrt((curr.getX() - dest.getX())
				* (curr.getX() - dest.getX()) + (curr.getY() - dest.getY())
				* (curr.getY() - dest.getY()));
	}

}