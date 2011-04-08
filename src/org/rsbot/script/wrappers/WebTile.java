package org.rsbot.script.wrappers;

/**
 * A RSTile modified for A-Star.
 *
 * @author Timer
 * @author Aut0r
 */
public class WebTile extends RSTile {

	private int[] neighbors = null;
	public WebTile parent = null;
	public Task req = null;
	public double g = 0.00, f = 0.00;

	public WebTile(final RSTile tile, final int[] neighbors,
	               final Task requirement) {
		super(tile.getX(), tile.getY());
		this.neighbors = neighbors;
		this.req = requirement;
	}

	/**
	 * Returns the tile.
	 *
	 * @return This RSTile.
	 */
	public RSTile tile() {
		return this;
	}

	/**
	 * Returns the connecting indices.
	 *
	 * @return The indices.
	 */
	public int[] connectingIndex() {
		return neighbors;
	}

	/**
	 * Task class.
	 */
	public abstract class Task {

		public abstract boolean meetsRequirement();

		public abstract boolean task();

		public boolean canDo() {
			return meetsRequirement();
		}

		public boolean execute() {
			return task();
		}

	}

}
