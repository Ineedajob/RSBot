package org.rsbot.event.impl;

import java.awt.Graphics;

import org.rsbot.bot.Bot;
import org.rsbot.event.listeners.TextPaintListener;
import org.rsbot.script.wrappers.RSTile;
import org.rsbot.util.StringUtil;

public class TWebPosition implements TextPaintListener {
	final Bot bot;
	final int xOff = 2045;
	final int yOff = 4168;

	public TWebPosition(Bot b) {
		this.bot = b;
	}

	@Override
	public int drawLine(final Graphics render, int idx) {
		RSTile me = bot.getMethodContext().players.getMyPlayer().getLocation();
		if (me != null) {
			int x = me.getX() - xOff;
			int y = yOff - me.getY();
			int z = bot.getMethodContext().game.getPlane();
			StringUtil.drawLine(render, idx++, "Web Location: "
					+ new RSTile(x, y, z).toString());
		}
		return idx;
	}
}
