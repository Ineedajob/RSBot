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
			//TODO load the web.
			Web.loaded = true;
		}
		if (Web.loaded) {
			deactivate(getID());
		}
		return 50;
	}

	@Override
	public int iterationSleep() {
		return 5000;
	}
}
