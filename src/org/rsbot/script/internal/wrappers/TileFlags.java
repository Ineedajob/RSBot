package org.rsbot.script.internal.wrappers;

import org.rsbot.script.wrappers.RSTile;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class TileFlags {
	public static interface Keys {
		static final int TILE_CLEAR = 0;
		static final int TILE_WATER = 1280;
		static final int WALL_NORTH_WEST = 1;
		static final int WALL_NORTH = 2;
		static final int WALL_NORTH_EAST = 4;
		static final int WALL_EAST = 8;
		static final int WALL_SOUTH_EAST = 10;
		static final int WALL_SOUTH = 20;
		static final int WALL_SOUTH_WEST = 40;
		static final int WALL_WEST = 80;
		static final int BLOCKED = 100;
	}

	public static interface Flags {
		public static final int WALL_NORTH_WEST = 0x1;
		public static final int WALL_NORTH = 0x2;
		public static final int WALL_NORTH_EAST = 0x4;
		public static final int WALL_EAST = 0x8;
		public static final int WALL_SOUTH_EAST = 0x10;
		public static final int WALL_SOUTH = 0x20;
		public static final int WALL_SOUTH_WEST = 0x40;
		public static final int WALL_WEST = 0x80;
		public static final int BLOCKED = 0x100;
		public static final int WATER = 0x1280100;
	}

	private RSTile tile;
	private List<Integer> keys = new ArrayList<Integer>();

	public TileFlags(RSTile tile) {
		this.tile = tile;
	}

	public RSTile getTile() {
		return tile;
	}

	public void addKey(final int key) {
		keys.add(key);
	}

	@Override
	public String toString() {
		String flags = "";
		Iterator<Integer> keysIterator = keys.listIterator();
		while (keysIterator.hasNext()) {
			int flag = keysIterator.next();
			flags += flag + "=";
		}
		return tile.toString() + flags;
	}
}