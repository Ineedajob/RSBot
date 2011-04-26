package org.rsbot.script.wrappers;

public class WebLadder extends WebTile {
	private int nUp;
	private int nDown;

	private static int[] constructNeighbors(int neighborUp, int neighborDown,
			int[] otherNeighbors) {
		int[] neighbors = new int[otherNeighbors.length + 2];
		for (int i = 0; i < otherNeighbors.length; i++)
			neighbors[i] = otherNeighbors[i];
		neighbors[neighbors.length - 1] = neighborDown;
		neighbors[neighbors.length - 2] = neighborUp;
		return neighbors;
	}

	public WebLadder(RSTile tile, int index, int neighborUp, int neighborDown,
			int[] otherNeighbors) {
		super(tile, index, constructNeighbors(neighborUp, neighborDown,
				otherNeighbors), null);
		nUp = neighborUp;
		nDown = neighborDown;
	}

	public int getUp() {
		return nUp;
	}

	public int getDown() {
		return nDown;
	}

}
