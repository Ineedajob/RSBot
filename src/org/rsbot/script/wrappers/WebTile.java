package org.rsbot.script.wrappers;

import java.util.ArrayList;
import java.util.List;

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
	private final int index;

	public WebTile(final RSTile tile, final int index, final int[] neighbors,
			final Task requirement) {
		super(tile.getX(), tile.getY(), tile.getZ());
		this.index = index;
		List<Integer> filteredNeighbors = new ArrayList<Integer>();
		for (int i : neighbors)
			if (i >= 0)
				filteredNeighbors.add(i);
		this.neighbors = new int[filteredNeighbors.size()];
		for (int i = 0; i < filteredNeighbors.size(); i++)
			this.neighbors[i] = filteredNeighbors.get(i).intValue();
		this.req = requirement;
	}

	/**
	 * Gets the index of this WebTile
	 * 
	 * @return The index
	 */
	public int indexOf() {
		return index;
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
