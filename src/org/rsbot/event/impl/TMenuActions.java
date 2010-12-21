package org.rsbot.event.impl;

import org.rsbot.bot.Bot;
import org.rsbot.event.listeners.TextPaintListener;
import org.rsbot.script.methods.Menu;
import org.rsbot.util.StringUtil;

import java.awt.*;

public class TMenuActions implements TextPaintListener {

	private Menu menu;

	public TMenuActions(Bot bot) {
		menu = bot.getMethodContext().menu;
	}

	public int drawLine(final Graphics render, int idx) {
		final String[] items = menu.getItems();
		int i = 0;
		for (final String item : items) {
			StringUtil.drawLine(render, idx++, i++ + ": [red]" + item);
		}
		return idx;
	}
}
