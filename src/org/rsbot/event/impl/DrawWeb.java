package org.rsbot.event.impl;

import org.rsbot.bot.Bot;
import org.rsbot.event.listeners.PaintListener;
import org.rsbot.script.internal.wrappers.TileFlags;
import org.rsbot.script.methods.MethodContext;
import org.rsbot.script.methods.Web;
import org.rsbot.script.wrappers.RSPlayer;
import org.rsbot.script.wrappers.RSTile;

import java.awt.*;
import java.util.Iterator;
import java.util.Map;

public class DrawWeb implements PaintListener {

	private final MethodContext ctx;

	private Point tileToMap(final RSTile tile, final RSPlayer player) {
		double minimapAngle = -1 * Math.toRadians(ctx.camera.getAngle());
		int x = (tile.getX() - player.getLocation().getX()) * 4 - 2;
		int y = (player.getLocation().getY() - tile.getY()) * 4 - 2;
		return new Point((int) Math.round(x * Math.cos(minimapAngle) + y * Math.sin(minimapAngle) + 628), (int) Math.round(y * Math.cos(minimapAngle) - x * Math.sin(minimapAngle) + 87));
	}

	public DrawWeb(Bot bot) {
		this.ctx = bot.getMethodContext();
	}

	public void onRepaint(final Graphics render) {
		if (!ctx.game.isLoggedIn()) {
			return;
		}
		final RSPlayer player = ctx.players.getMyPlayer();
		if (player == null) {
			return;
		}
		Iterator<Map.Entry<RSTile, TileFlags>> rs = Web.map.entrySet().iterator();
		while (rs.hasNext()) {
			TileFlags t = rs.next().getValue();
			render.setColor(t.isWalkable() ? t.isQuestionable() ? Color.yellow : Color.green : t.isWater() ? Color.cyan : Color.red);
			Point p = tileToMap(t.getTile(), player);
			render.drawLine(p.x, p.y, p.x, p.y);
		}
	}
}