package org.rsbot.script.randoms;

import org.rsbot.gui.AccountManager;
import org.rsbot.script.Random;
import org.rsbot.script.ScriptManifest;
import org.rsbot.script.wrappers.RSComponent;

@ScriptManifest(authors = { "Zenzie" }, name = "BankPin", version = 1.0)
public class BankPins extends Random {

	@Override
	public boolean activateCondition() {
		return interfaces.get(13).getComponent(26).containsText("Please enter your PIN");
	}

	@Override
	public int loop() {
        char[] bankPin = AccountManager.getPin(account.getName(), this.getClass()).toCharArray();
        if(bankPin == null || bankPin.length != 4) {
            log("There is no pin entered for this account. Stopping script.");
            stopScript(true);
            return -1;
        }
        RSComponent[] bankComponents = interfaces.get(759).getComponents();
        for(char ch : bankPin) {
            for(RSComponent comp : bankComponents) {
                if(comp.getText().equals(Character.toString(ch))) {
                    comp.doClick();
                    sleep(random(750, 2500));
                }
            }
        }
		return -1;
	}
}
