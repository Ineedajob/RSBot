package org.rsbot.event.impl;

import java.awt.Graphics;

import org.rsbot.accessors.Client;
import org.rsbot.bot.Bot;
import org.rsbot.bot.input.Mouse;
import org.rsbot.event.listeners.TextPaintListener;
import org.rsbot.util.StringUtil;

public class TMousePosition implements TextPaintListener {
	
	private Client client;
	
	public TMousePosition(Bot bot) {
		client = bot.getClient();
	}

    public int drawLine(final Graphics render, int idx) {
        final Mouse mouse = client.getMouse();
        if (mouse != null) {
            final int mouse_x = mouse.getClientX();
            final int mouse_y = mouse.getClientY();
            String off = mouse.isClientPresent() ? "" : " (off)";
            StringUtil.drawLine(render, idx++, "Mouse Position " + mouse_x + "," + mouse_y + off);
        }

        return idx;
    }
}
