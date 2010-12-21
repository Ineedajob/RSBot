package org.rsbot.event.impl;

import org.rsbot.bot.Bot;
import org.rsbot.event.listeners.TextPaintListener;
import org.rsbot.script.methods.MethodContext;
import org.rsbot.util.StringUtil;

import java.awt.*;

public class TAnimation implements TextPaintListener {

	private MethodContext ctx;

	public TAnimation(Bot bot) {
		ctx = bot.getMethodContext();
	}

	public int drawLine(final Graphics render, int idx) {
		int animation;
		if (ctx.game.isLoggedIn()) {
			animation = ctx.players.getMyPlayer().getAnimation();
		} else {
			animation = -1;
		}
		StringUtil.drawLine(render, idx++, "Animation " + animation);
		return idx;
	}

}
