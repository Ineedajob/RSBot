package org.rsbot.event.impl;

import org.rsbot.bot.Bot;
import org.rsbot.event.listeners.TextPaintListener;
import org.rsbot.script.methods.Game;
import org.rsbot.util.StringUtil;

import java.awt.*;

public class TFloorHeight implements TextPaintListener {

	private final Game game;

	public TFloorHeight(Bot bot) {
		game = bot.getMethodContext().game;
	}

	public int drawLine(final Graphics render, int idx) {
		final int floor = game.getPlane();
		StringUtil.drawLine(render, idx++, "Floor " + floor);
		return idx;
	}

}
