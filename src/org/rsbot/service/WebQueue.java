package org.rsbot.service;

import org.rsbot.script.internal.wrappers.TileFlags;
import org.rsbot.script.methods.Web;
import org.rsbot.script.wrappers.RSTile;
import org.rsbot.util.CacheWriter;
import org.rsbot.util.GlobalConfiguration;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.logging.Logger;

public class WebQueue {
	private static CacheWriter cacheWriter = null;
	private static final Logger log = Logger.getLogger("WebQueue");

	public static void Create() {
		if (cacheWriter == null) {
			cacheWriter = new CacheWriter(GlobalConfiguration.Paths.getWebCache());
		}
	}

	public static void Add(final HashMap<RSTile, TileFlags> theFlagsList) {
		Web.map.putAll(theFlagsList);
		new Thread() {
			public void run() {
				String addedString = "";
				final HashMap<RSTile, TileFlags> theFlagsList2 = new HashMap<RSTile, TileFlags>();
				theFlagsList2.putAll(theFlagsList);
				final Map<RSTile, TileFlags> tl = Collections.unmodifiableMap(theFlagsList2);
				Iterator<Map.Entry<RSTile, TileFlags>> tileFlagsIterator = tl.entrySet().iterator();
				while (tileFlagsIterator.hasNext()) {
					TileFlags tileFlags = tileFlagsIterator.next().getValue();
					if (tileFlags != null) {
						addedString += tileFlags.toString() + "\n";
						try {
							Thread.sleep(5);
						} catch (InterruptedException ignored) {
						}
					}
				}
				cacheWriter.add(addedString);
				addedString = null;
				theFlagsList2.clear();
			}
		}.start();
	}

	public static void Destroy() {
		cacheWriter.destroy();
	}
}
