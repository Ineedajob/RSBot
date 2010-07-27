package org.rsbot.event.impl;

import java.awt.Graphics;

import org.rsbot.bot.Bot;
import org.rsbot.bot.input.Mouse;
import org.rsbot.event.listeners.TextPaintListener;
import org.rsbot.util.StringUtil;

public class TActualMousePosition implements TextPaintListener {
	
	private Mouse mouse;
	
	public TActualMousePosition(Bot bot) {
		mouse = bot.getClient().getMouse();
	}

    public int drawLine(final Graphics render, int idx) {
        if (mouse != null) {
            StringUtil.drawLine(render, idx++, "Actual Mouse Position: " +
            		mouse.getRealX() + "," + mouse.getRealY());
        }
        return idx;
    }
}
