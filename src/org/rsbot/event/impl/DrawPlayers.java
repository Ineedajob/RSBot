package org.rsbot.event.impl;

import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Point;

import org.rsbot.bot.Bot;
import org.rsbot.event.listeners.PaintListener;
import org.rsbot.script.methods.MethodContext;
import org.rsbot.script.wrappers.RSPlayer;

public class DrawPlayers implements PaintListener {
    
	private MethodContext ctx;
	
	public DrawPlayers(Bot bot) {
		ctx = bot.getMethodContext();
	}

    public void onRepaint(final Graphics render) {
        if (!ctx.game.isLoggedIn())
            return;
        final org.rsbot.client.RSPlayer[] players = ctx.client.getRSPlayerArray();
        if (players == null)
            return;
        final FontMetrics metrics = render.getFontMetrics();
        for (final org.rsbot.client.RSPlayer element : players) {
            if (element == null) {
                continue;
            }
            final RSPlayer player = new RSPlayer(ctx, element);
            final Point location = ctx.calc.tileToScreen(player.getLocation(), -player.getHeight() / 2);
            if (!ctx.calc.pointOnScreen(location)) {
                continue;
            }
            render.setColor(Color.RED);
            render.fillRect((int) location.getX() - 1, (int) location.getY() - 1, 2, 2);
            String s = "" + player.getName() + " (" + player.getCombatLevel() + ")";
            render.setColor(player.isInCombat() ? Color.red : player.isMoving() ? Color.green : Color.WHITE);
            render.drawString(s, location.x - metrics.stringWidth(s) / 2, location.y - metrics.getHeight() / 2);

            if (player.getAnimation() != -1) {
                s = "(" + player.getAnimation() + ")";
                render.drawString(s, location.x - metrics.stringWidth(s) / 2, location.y - metrics.getHeight() * 3 / 2);
            }
        }
    }
}
