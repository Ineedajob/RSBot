package org.rsbot.event.impl;

import java.awt.Graphics;

import org.rsbot.bot.Bot;
import org.rsbot.event.listeners.TextPaintListener;
import org.rsbot.script.wrappers.Web;
import org.rsbot.script.wrappers.WebTile;
import org.rsbot.util.StringUtil;

public class TWebIndex implements TextPaintListener {

	private final Bot bot;
	/**
	 * The WebMap allocation.
	 */
	private Web map = null;

	private long nextUpdate = 0;

	public TWebIndex(Bot bot) {
		this.bot = bot;
		map = new Web(bot.getMethodContext(), null, null);
	}

	public int drawLine(final Graphics render, int idx) {
		if (System.currentTimeMillis() > nextUpdate) {
			nextUpdate = System.currentTimeMillis() + 30000;
			map.setMap();
		}
		if (map != null) {
			WebTile t = map.map().getWebTile(
					bot.getMethodContext().players.getMyPlayer().getLocation());
			if (t != null) {
				StringUtil.drawLine(render, idx++,
						"Nearest Web Index: " + t.indexOf());
			}
		}
		return idx;
	}
}
