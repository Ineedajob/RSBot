package org.rsbot.script.passives;

import org.rsbot.script.PassiveScript;
import org.rsbot.script.PassiveScriptManifest;
import org.rsbot.script.internal.wrappers.TileFlags;
import org.rsbot.script.methods.Web;
import org.rsbot.script.wrappers.RSTile;
import org.rsbot.util.GlobalConfiguration;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.HashMap;

@PassiveScriptManifest(name = "Web Data Loader", authors = {"Timer"})
public class WebLoader extends PassiveScript {
	@Override
	public boolean activateCondition() {
		return !Web.loaded;
	}

	@Override
	public int loop() {
		if (!Web.loaded) {
			try {
				final long startLoad = System.currentTimeMillis();
				BufferedReader br = new BufferedReader(new FileReader(GlobalConfiguration.Paths.getWebCache()));
				String line;
				final HashMap<RSTile, TileFlags> theFlagsList = new HashMap<RSTile, TileFlags>();
				while ((line = br.readLine()) != null) {
					String[] data = line.split("tile=data");
					if (data.length == 2) {
						String[] tileData = data[0].split(",");
						String[] abbData = data[1].split("=");
						if (tileData.length == 3) {
							try {
								RSTile tile = new RSTile(Integer.parseInt(tileData[0]), Integer.parseInt(tileData[1]), Integer.parseInt(tileData[2]));
								TileFlags tileFlags = new TileFlags(tile);
								for (String abb : abbData) {
									if (abb.length() > 0) {
										try {
											tileFlags.addKey(Integer.parseInt(abb));
										} catch (Exception e) {
										}
									}
								}
								theFlagsList.put(tile, tileFlags);
							} catch (Exception e) {
							}
						} else {
							log.info("Length != 3");
						}
					} else {
						log.info("Lengh != 2" + line);
					}
				}
				Web.map.putAll(theFlagsList);
				Web.loaded = true;
				final long timeTook = System.currentTimeMillis() - startLoad;
				log("Loaded " + Web.map.size() + " nodes in " + timeTook + "ms.");
			} catch (Exception e) {
				log("Failed to load the web.. trying again.");
			}
		}
		if (Web.loaded) {
			deactivate(getID());
		}
		return -1;
	}

	@Override
	public int iterationSleep() {
		return 5000;
	}
}
