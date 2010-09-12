package org.rsbot.bot;

import org.rsbot.client.Callback;
import org.rsbot.client.Render;
import org.rsbot.client.RenderData;
import org.rsbot.event.events.CharacterMovedEvent;
import org.rsbot.event.events.ServerMessageEvent;
import org.rsbot.script.methods.MethodContext;

public class CallbackImpl implements Callback {
	
    private final Bot bot;

    public CallbackImpl(Bot bot) {
        this.bot = bot;
    }

    public Bot getBot() {
        return bot;
    }

    public void notifyServerMessage(final String s) {
    	ServerMessageEvent e = new ServerMessageEvent(s);
        bot.getEventManager().dispatchEvent(e);
    }

    public void rsCharacterMoved(final org.rsbot.client.RSCharacter c, final int i) {
    	CharacterMovedEvent e = new CharacterMovedEvent(bot.getMethodContext(), c, i);
        bot.getEventManager().dispatchEvent(e);
    }

    public void updateRenderInfo(final Render r, final RenderData rd) {
    	MethodContext ctx = bot.getMethodContext();
    	if (ctx != null) {
    		ctx.calc.updateRenderInfo(r, rd);
    	}
    }
}
