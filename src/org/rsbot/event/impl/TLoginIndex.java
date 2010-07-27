package org.rsbot.event.impl;

import java.awt.Graphics;

import org.rsbot.bot.Bot;
import org.rsbot.event.listeners.TextPaintListener;
import org.rsbot.script.methods.Game;
import org.rsbot.util.StringUtil;

public class TLoginIndex implements TextPaintListener {
	
	private Game game;

    public TLoginIndex(Bot bot) {
    	game = bot.getMethodContext().game;
    }

    public int drawLine(final Graphics render, int idx) {
        StringUtil.drawLine(render, idx++, "Login Index: " + game.getLoginIndex());
        return idx;
    }
    
}
