package org.rsbot.event.impl;

import org.rsbot.bot.Bot;
import org.rsbot.event.listeners.PaintListener;
import org.rsbot.script.methods.MethodContext;
import org.rsbot.script.wrappers.*;

import java.awt.*;

public class DrawWeb implements PaintListener {

	private final MethodContext ctx;

	public DrawWeb(Bot bot) {
		this.ctx = bot.getMethodContext();
	}

	private Point tileToMap(final RSTile tile, final RSPlayer player) {
		double minimapAngle = -1 * Math.toRadians(ctx.camera.getAngle());
		int x = (tile.getX() - player.getLocation().getX()) * 4 - 2;
		int y = (player.getLocation().getY() - tile.getY()) * 4 - 2;
		return new Point((int) Math.round(x * Math.cos(minimapAngle) + y * Math.sin(minimapAngle) + 628),
				(int) Math.round(y * Math.cos(minimapAngle) - x * Math.sin(minimapAngle) + 87));
	}

	public void onRepaint(final Graphics render) {
		if (!ctx.game.isLoggedIn()) {
			return;
		}
		final RSPlayer player = ctx.players.getMyPlayer();
		if (player == null) {
			return;
		}
		render.setColor(Color.white);
		final WebMap map = new Web(ctx, null, null).map();
		final WebTile[] webTiles = map.getTiles();
		for (WebTile webTile : webTiles) {
			if (ctx.calc.distanceTo(webTile.tile()) < 100) {
				Point p = tileToMap(webTile, player);
				for (int l : webTile.connectingIndex()) {
					Point pp = tileToMap(webTiles[l], player);
					render.drawLine(pp.x, pp.y, p.x, p.y);
				}
			}
		}
		render.setColor(Color.red);
		for (WebTile webTile : map.getTiles()) {
			if (ctx.calc.distanceTo(webTile.tile()) < 100) {
				Point p = tileToMap(webTile, player);
				render.fillRect(p.x - 2, p.y - 2, 4, 4);
			}
		}
	}
}
