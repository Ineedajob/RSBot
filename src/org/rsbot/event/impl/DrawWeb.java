package org.rsbot.event.impl;

import org.rsbot.bot.Bot;
import org.rsbot.event.listeners.PaintListener;
import org.rsbot.script.internal.wrappers.TileFlags;
import org.rsbot.script.methods.MethodContext;
import org.rsbot.script.methods.Web;
import org.rsbot.script.wrappers.RSTile;

import java.awt.*;
import java.util.Iterator;
import java.util.Map;

public class DrawWeb implements PaintListener {

	private final MethodContext ctx;

	public DrawWeb(Bot bot) {
		this.ctx = bot.getMethodContext();
	}

	public void onRepaint(final Graphics render) {
		if (!ctx.game.isLoggedIn()) {
			return;
		}
		Iterator<Map.Entry<RSTile, TileFlags>> rs = Web.map.entrySet().iterator();
		while (rs.hasNext()) {
			TileFlags t = rs.next().getValue();
			render.setColor(t.isBlocked() ? Color.red : Color.green);
			if (ctx.calc.tileOnMap(t.getTile())) {
				Point p = ctx.calc.tileToMinimap(t.getTile());
				render.drawLine(p.x, p.y, p.x, p.y);
			}
		}
	}
}
