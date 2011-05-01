package org.rsbot.script.passives;

import org.rsbot.script.PassiveScript;
import org.rsbot.script.PassiveScriptManifest;
import org.rsbot.script.methods.Web;

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
				Web.loaded = true;
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
