package org.rsbot.service;

import org.rsbot.script.internal.wrappers.TileFlags;
import org.rsbot.script.methods.Web;
import org.rsbot.script.wrappers.RSTile;
import org.rsbot.util.CacheWriter;
import org.rsbot.util.GlobalConfiguration;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

public class WebQueue {
	private static CacheWriter cacheWriter = null;
	public static final HashMap<RSTile, TileFlags> rs_map = new HashMap<RSTile, TileFlags>();

	public static void Create() {
		if (cacheWriter == null) {
			cacheWriter = new CacheWriter(GlobalConfiguration.Paths.getWebCache());
		}
	}

	public static void Add(final List<TileFlags> tileFlagsList) {
		String addedString = "";
		Iterator<TileFlags> tileFlagsIterator = tileFlagsList.listIterator();
		while (tileFlagsIterator.hasNext()) {
			TileFlags tileFlags = tileFlagsIterator.next();
			if (tileFlags != null) {
				addedString += tileFlags.toString() + "\n";
			}
		}
		cacheWriter.add(addedString);
		Web.map.putAll(rs_map);
		rs_map.clear();
		addedString = null;
	}

	public static void Destroy() {
		cacheWriter.destroy();
	}
}
