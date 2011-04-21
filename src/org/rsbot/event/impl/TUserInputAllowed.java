package org.rsbot.event.impl;

import org.rsbot.bot.Bot;
import org.rsbot.event.listeners.TextPaintListener;
import org.rsbot.util.StringUtil;

import java.awt.*;

public class TUserInputAllowed implements TextPaintListener {

	private final Bot bot;

	public TUserInputAllowed(Bot bot) {
		this.bot = bot;
	}

	public int drawLine(final Graphics render, int idx) {
		StringUtil.drawLine(render, idx++, "User Input: " +
				(bot.inputFlags == 0 && !bot.overrideInput ?
						"[red]Disabled (" + bot.inputFlags + ")" : "[green]Enabled"));
		return idx;
	}
}
