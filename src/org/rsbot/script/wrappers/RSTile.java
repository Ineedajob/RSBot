package org.rsbot.script.wrappers;

/**
 * A tile at an absolute location in the game world.
 */
public class RSTile {

	private final int x;
	private final int y;
	private final int z;

	/**
	 * @param x the x axel of the Tile
	 * @param y the y axel of the Tile
	 */
	public RSTile(final int x, final int y) {
		this.x = x;
		this.y = y;
		this.z = 0;
	}

	/**
	 * @param x the x axel of the Tile
	 * @param y the y axel of the Tile
	 * @param z the z axel of the Tile( the floor)
	 */
	public RSTile(final int x, final int y, final int z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}

	public int getX() {
		return x;
	}

	public int getY() {
		return y;
	}

	public int getZ() {
		return z;
	}

	/**
	 * Randomizes this tile.
	 *
	 * @param maxXDeviation Max X distance from tile x.
	 * @param maxYDeviation Max Y distance from tile y.
	 * @return The randomized tile
	 */
	public RSTile randomize(final int maxXDeviation, final int maxYDeviation) {
		int x = getX();
		int y = getY();
		if (maxXDeviation > 0) {
			double d = Math.random() * 2 - 1.0;
			d *= maxXDeviation;
			x += (int) d;
		}
		if (maxYDeviation > 0) {
			double d = Math.random() * 2 - 1.0;
			d *= maxYDeviation;
			y += (int) d;
		}
		return new RSTile(x, y);
	}

	@Override
	public int hashCode() {
		return x * 31 + y;
	}

	@Override
	public boolean equals(final Object obj) {
		if (obj == this) {
			return true;
		}
		if (obj instanceof RSTile) {
			final RSTile tile = (RSTile) obj;
			return (tile.x == x) && (tile.y == y) && (tile.z == z);
		}
		return false;
	}

	@Override
	public String toString() {
		return "(X: " + x + ", Y:" + y + ", Z:" + z + ")";
	}

}
