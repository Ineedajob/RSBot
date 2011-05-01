package org.rsbot.service;

import org.rsbot.script.internal.wrappers.TileFlags;
import org.rsbot.util.CacheWriter;
import org.rsbot.util.GlobalConfiguration;

import java.util.Iterator;
import java.util.List;

public class WebQueue {
	private static CacheWriter cacheWriter = null;

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
		addedString = null;
	}

	public static void Destroy() {
		cacheWriter.destroy();
	}
}
