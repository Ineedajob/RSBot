package org.rsbot.event.impl;

import java.awt.Graphics;

import org.rsbot.bot.Bot;
import org.rsbot.event.listeners.TextPaintListener;
import org.rsbot.util.StringUtil;

public class TUserInputAllowed implements TextPaintListener {

	private Bot bot;

	public TUserInputAllowed(Bot bot) {
		this.bot = bot;
	}

    public int drawLine(final Graphics render, int idx) {
        StringUtil.drawLine(render, idx++, "User Input: " + (bot.disableInput ?
				"[red]Disabled (" + bot.inputMask + ")" : "[green]Enabled"));
        return idx;
    }
}
