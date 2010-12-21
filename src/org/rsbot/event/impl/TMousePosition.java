package org.rsbot.event.impl;

import org.rsbot.bot.Bot;
import org.rsbot.client.Client;
import org.rsbot.client.input.Mouse;
import org.rsbot.event.listeners.TextPaintListener;
import org.rsbot.util.StringUtil;

import java.awt.*;

public class TMousePosition implements TextPaintListener {

	private Client client;

	public TMousePosition(Bot bot) {
		client = bot.getClient();
	}

	public int drawLine(final Graphics render, int idx) {
		final Mouse mouse = client.getMouse();
		if (mouse != null) {
			final int mouse_x = mouse.getX();
			final int mouse_y = mouse.getY();
			String off = mouse.isPresent() ? "" : " (off)";
			StringUtil.drawLine(render, idx++, "Mouse Position: (" + mouse_x + "," + mouse_y + ")" + off);
		}

		return idx;
	}
}
