package org.rsbot.script.internal.event;

import org.rsbot.script.PassiveScript;
import org.rsbot.script.internal.PassiveScriptHandler;

public interface PassiveScriptListener {

	public void scriptStarted(PassiveScriptHandler handler, PassiveScript script);

	public void scriptStopped(PassiveScriptHandler handler, PassiveScript script);

}
