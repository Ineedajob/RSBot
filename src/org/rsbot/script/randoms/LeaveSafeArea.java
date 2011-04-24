package org.rsbot.script.randoms;

import org.rsbot.script.Random;
import org.rsbot.script.ScriptManifest;

@ScriptManifest(name = "LeaveSafeArea", authors = "Taha", version = 1.0)
public class LeaveSafeArea extends Random {

	@Override
	public boolean activateCondition() {
		return (interfaces.getComponent(212, 2).containsText("things can get more") &&
				(interfaces.getComponent(212, 2).getAbsoluteY() > 380) &&
				(interfaces.getComponent(212, 2).getAbsoluteY() < 410)) ||
				(interfaces.getComponent(236, 1).containsText("the starting area") &&
						(interfaces.getComponent(236, 1).getAbsoluteY() > 390) &&
						(interfaces.getComponent(236, 1).getAbsoluteY() < 415));
	}

	@Override
	public int loop() {
		if (interfaces.canContinue()) {
			interfaces.clickContinue();
			sleep(random(1000, 1200));
		}
		interfaces.getComponent(236, 1).doClick();
		return -1;
	}

}
