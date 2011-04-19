package org.rsbot.script.randoms;

import org.rsbot.gui.AccountManager;
import org.rsbot.script.Random;
import org.rsbot.script.ScriptManifest;
import org.rsbot.script.wrappers.RSComponent;

@ScriptManifest(authors = {"Holo", "Gnarly", "Salty_Fish", "Pervy Shuya", "Doout"}, name = "BankPin", version = 3.0)
public class BankPins extends Random {

	public boolean activateCondition() {
		return interfaces.get(13).isValid() || interfaces.get(14).isValid();
	}

	void enterCode(final String pin) {
		if (!interfaces.get(13).isValid()) {
			return;
		}
		final RSComponent[] children = interfaces.get(13).getComponents();
		int state = 0;
		for (int i = 1; i < 5; i++) {
			if (children[i].containsText("?")) {
				state++;
			}
		}
		state = 4 - state;
		if (!interfaces.get(759).isValid()) {
			return;
		}
		final RSComponent[] bankPin = interfaces.get(759).getComponents();
		for (RSComponent aBankPin : bankPin) {
			if (aBankPin.containsText(pin.substring(state, state + 1))) {
				aBankPin.doClick(true);
				sleep(random(500, 1000));
				break;
			}
		}
	}

	@Override
	public int loop() {
		if (interfaces.get(14).isValid()) {
			interfaces.getComponent(14, 33).doClick();
			sleep(300);
		} else {
			String pin = AccountManager.getPin(account.getName());
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
		}
		return 500;
	}
}