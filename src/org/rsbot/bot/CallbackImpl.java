package org.rsbot.bot;

import org.rsbot.client.Callback;
import org.rsbot.client.Render;
import org.rsbot.client.RenderData;
import org.rsbot.event.events.CharacterMovedEvent;
import org.rsbot.event.events.MessageEvent;
import org.rsbot.event.events.ServerMessageEvent;
import org.rsbot.script.methods.MethodContext;

@SuppressWarnings("deprecation")
public class CallbackImpl implements Callback {

	private final Bot bot;

	public CallbackImpl(Bot bot) {
		this.bot = bot;
	}

	public Bot getBot() {
		return bot;
	}

	public void notifyMessage(final int id, final String sender, final String msg) {
		MessageEvent m = new MessageEvent(sender, id, msg);
		bot.getEventManager().dispatchEvent(m);
		if (id == MessageEvent.MESSAGE_SERVER || id == MessageEvent.MESSAGE_ACTION) {
			ServerMessageEvent e = new ServerMessageEvent(msg);
			bot.getEventManager().dispatchEvent(e);
		}
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
