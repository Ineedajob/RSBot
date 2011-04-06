package org.rsbot.event.impl;

import org.rsbot.bot.Bot;
import org.rsbot.event.listeners.TextPaintListener;
import org.rsbot.script.methods.Players;
import org.rsbot.script.wrappers.RSTile;
import org.rsbot.util.StringUtil;

import java.awt.*;

public class TPlayerPosition implements TextPaintListener {

	private final Players players;

	public TPlayerPosition(Bot bot) {
		players = bot.getMethodContext().players;
	}

	public int drawLine(final Graphics render, int idx) {
		final RSTile position = players.getMyPlayer().getLocation();
		StringUtil.drawLine(render, idx++, "Position: " + position);
		return idx;
	}

}
