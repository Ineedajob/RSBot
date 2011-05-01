package org.rsbot.script.passives;

import org.rsbot.script.PassiveScript;
import org.rsbot.script.PassiveScriptManifest;

@PassiveScriptManifest(name = "Web Data Collector", authors = {"Timer"})
public class Web extends PassiveScript {
	@Override
	public boolean activateCondition() {
		return true;
	}

	@Override
	public int loop() {
		log("We're a passive script that's suppose to be collecting web data!");
		return 1000;
	}
}
