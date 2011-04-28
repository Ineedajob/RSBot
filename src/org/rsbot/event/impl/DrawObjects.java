package org.rsbot.event.impl;

import org.rsbot.bot.Bot;
import org.rsbot.event.listeners.PaintListener;
import org.rsbot.script.methods.MethodContext;
import org.rsbot.script.wrappers.RSObject;
import org.rsbot.script.wrappers.RSPlayer;
import org.rsbot.script.wrappers.RSTile;

import java.awt.*;
import java.util.HashMap;

public class DrawObjects implements PaintListener {

	private final MethodContext ctx;

	public DrawObjects(Bot bot) {
		ctx = bot.getMethodContext();
	}

	private static final HashMap<RSObject.Type, Color> color_map = new HashMap<RSObject.Type, Color>();

	static {
		color_map.put(RSObject.Type.BOUNDARY, Color.BLACK);
		color_map.put(RSObject.Type.FLOOR_DECORATION, Color.YELLOW);
		color_map.put(RSObject.Type.INTERACTABLE, Color.WHITE);
		color_map.put(RSObject.Type.WALL_DECORATION, Color.GRAY);
	}

	public void onRepaint(final Graphics render) {
		if (!ctx.game.isLoggedIn()) {
			return;
		}
		final RSPlayer player = ctx.players.getMyPlayer();
		if (player == null) {
			return;
		}
		final FontMetrics metrics = render.getFontMetrics();
		final RSTile location = player.getLocation();
		final int locX = location.getX();
		final int locY = location.getY();
		final int tHeight = metrics.getHeight();
		for (int x = locX - 25; x < locX + 25; x++) {
			for (int y = locY - 25; y < locY + 25; y++) {
				RSTile tile = new RSTile(x, y);
				final Point screen = ctx.calc.tileToScreen(tile);
				if (!ctx.calc.pointOnScreen(screen)) {
					continue;
				}
				final RSObject[] objects = ctx.objects.getAllAt(tile);
				int i = 0;
				for (RSObject object : objects) {
					Point real = ctx.calc.tileToScreen(object.getLocation());
					if (!ctx.calc.pointOnScreen(real)) {
						continue;
					}
					if (screen.x > -1) {
						render.setColor(Color.GREEN);
						render.fillRect(screen.x - 1, screen.y - 1, 2, 2);
						render.setColor(Color.RED);
						render.drawLine(screen.x, screen.y, real.x, real.y);
					}
					final String s = "" + object.getID();
					final int ty = real.y - tHeight / 2 - (i++) * 15;
					final int tx = real.x - metrics.stringWidth(s) / 2;
					render.setColor(color_map.get(object.getType()));
					render.drawString(s, tx, ty);
				}
			}
		}
	}
}
