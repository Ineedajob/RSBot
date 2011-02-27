package org.rsbot.script.randoms;

import org.rsbot.gui.AccountManager;
import org.rsbot.script.Random;
import org.rsbot.script.ScriptManifest;
import org.rsbot.script.wrappers.RSComponent;

@ScriptManifest(authors = { "Holo", "Gnarly", "Salty_Fish", "Pervy Shuya" }, name = "BankPin", version = 2.2)
public class BankPins extends Random {

	@Override
	public boolean activateCondition() {
		return interfaces.get(13).isValid();
	}

	public void enterCode(final String pin) {
		if (!interfaces.get(13).isValid())
			return;
		final RSComponent[] children = interfaces.get(13).getComponents();
		int state = 0;
		for (int i = 1; i < 5; i++) {
			if (children[i].containsText("?")) {
				state++;
			}
		}
		state = 4 - state;
		if (!interfaces.get(759).isValid())
			return;
		final RSComponent[] bankPin = interfaces.get(759).getComponents();
		for (int i = 0; i < bankPin.length; i++) {
			if (bankPin[i].containsText(pin.substring(state, state + 1))) {
				bankPin[i].doClick(true);
				sleep(random(500, 1000));
				break;
			}
		}
	}

	@Override
	public int loop() {
		String pin = AccountManager.getPin(account.getName(), getClass());
		if ((pin == null) || (pin.length() != 4)) {
			log.severe("You must add a bank pin to your account.");
			stopScript(false);
		}
		if (interfaces.get(14).isValid() || !interfaces.get(13).isValid()) {
			interfaces.get(14).getComponent(3).doClick();
			return -1;
		}
		enterCode(pin);
		if (interfaces.get(211).isValid()) {
			interfaces.get(211).getComponent(3).doClick();
		} else if (interfaces.get(217).isValid()) {
			sleep(random(10500, 12000));
		}
		return 500;
	}
}