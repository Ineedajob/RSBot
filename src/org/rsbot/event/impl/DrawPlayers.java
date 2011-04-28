package org.rsbot.event.impl;

import org.rsbot.bot.Bot;
import org.rsbot.event.listeners.PaintListener;
import org.rsbot.script.methods.MethodContext;
import org.rsbot.script.wrappers.RSPlayer;

import java.awt.*;

public class DrawPlayers implements PaintListener {

	private final MethodContext ctx;

	public DrawPlayers(Bot bot) {
		ctx = bot.getMethodContext();
	}

	public void onRepaint(final Graphics render) {
		if (!ctx.game.isLoggedIn()) {
			return;
		}
		final org.rsbot.client.RSPlayer[] players = ctx.client.getRSPlayerArray();
		if (players == null) {
			return;
		}
		final FontMetrics metrics = render.getFontMetrics();
		for (final org.rsbot.client.RSPlayer element : players) {
			if (element == null) {
				continue;
			}
			final RSPlayer player = new RSPlayer(ctx, element);
			final Point location = ctx.calc.tileToScreen(player.getLocation(), player.getHeight() / 2);
			if (!ctx.calc.pointOnScreen(location)) {
				continue;
			}
			render.setColor(Color.RED);
			render.fillRect((int) location.getX() - 1, (int) location.getY() - 1, 2, 2);
			String s = "" + player.getName() + " (" + player.getCombatLevel() + ")";
			render.setColor(player.isInCombat() ? Color.RED : player.isMoving() ? Color.GREEN : Color.WHITE);
			render.drawString(s, location.x - metrics.stringWidth(s) / 2, location.y - metrics.getHeight() / 2);
			String msg = player.getMessage();
			boolean raised = false;
			if (player.getAnimation() != -1 || player.getGraphic() != -1 || player.getNPCID() != -1) {
				if (player.getNPCID() != -1) {
					s = "(NPC: " + player.getNPCID() + " | L: " + player.getLevel() + " | A: " + player.getAnimation() + " | G: " + player.getGraphic() + ")";
				} else {
					s = "(A: " + player.getAnimation() + " | L: " + player.getLevel() + " | G: " + player.getGraphic() + ")";
				}
				render.drawString(s, location.x - metrics.stringWidth(s) / 2, location.y - metrics.getHeight() * 3 / 2);
				raised = true;
			}
			if (msg != null) {
				render.setColor(Color.ORANGE);
				render.drawString(msg, location.x - metrics.stringWidth(msg) / 2,
						location.y - metrics.getHeight() * (raised ? 5 : 3) / 2);
			}
		}
	}
}
