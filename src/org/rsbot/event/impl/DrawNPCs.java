package org.rsbot.event.impl;

import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Point;

import org.rsbot.client.Node;
import org.rsbot.client.RSNPCNode;
import org.rsbot.bot.Bot;
import org.rsbot.event.listeners.PaintListener;
import org.rsbot.script.methods.MethodContext;
import org.rsbot.script.wrappers.RSNPC;

public class DrawNPCs implements PaintListener {

	private MethodContext ctx;
	
    public DrawNPCs(Bot bot) {
    	ctx = bot.getMethodContext();
    }

    public void onRepaint(final Graphics render) {
        if (!ctx.game.isLoggedIn())
            return;

        final FontMetrics metrics = render.getFontMetrics();
        for (int element : ctx.client.getRSNPCIndexArray()) {
            Node node = ctx.nodes.lookup(ctx.client.getRSNPCNC(), element);
            if (node == null || !(node instanceof RSNPCNode)) {
                continue;
            }
            final RSNPC npc = new RSNPC(ctx, ((RSNPCNode) node).getRSNPC());
            final Point location = npc.getScreenLocation();
            if (!ctx.calc.pointOnScreen(location)) {
                continue;
            }
            render.setColor(Color.RED);
            render.fillRect((int) location.getX() - 1, (int) location.getY() - 1, 2, 2);
            String s = "" + npc.getID();
            render.setColor(npc.isInCombat() ? Color.red : npc.isMoving() ? Color.green : Color.WHITE);
            render.drawString(s, location.x - metrics.stringWidth(s) / 2, location.y - metrics.getHeight() / 2);
            // int x = element.getX();
            // x -= ((int)(x >> 7)) << 7;
            if (npc.getAnimation() != -1) {
                s = "(" + npc.getAnimation() + ")";
                render.drawString(s, location.x - metrics.stringWidth(s) / 2, location.y - metrics.getHeight() * 3 / 2);
            }
            // s = "" + element.isMoving();
            // render.drawString(s, location.x - metrics.stringWidth(s) / 2,
            // location.y - metrics.getHeight() * 5 / 2);
        }
    }
}
