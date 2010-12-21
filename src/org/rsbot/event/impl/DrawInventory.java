package org.rsbot.event.impl;

import org.rsbot.bot.Bot;
import org.rsbot.event.listeners.PaintListener;
import org.rsbot.script.methods.Game;
import org.rsbot.script.methods.MethodContext;
import org.rsbot.script.wrappers.RSItem;

import java.awt.*;

public class DrawInventory implements PaintListener {

	private MethodContext ctx;

	public DrawInventory(Bot bot) {
		ctx = bot.getMethodContext();
	}

	public void onRepaint(final Graphics render) {
		if (!ctx.game.isLoggedIn())
			return;

		if (ctx.game.getCurrentTab() != Game.TAB_INVENTORY)
			return;

		render.setColor(Color.WHITE);
		final RSItem[] inventoryItems = ctx.inventory.getItems();

		for (int off = 0; off < inventoryItems.length; off++) {
			if (inventoryItems[off].getID() != -1) {
				final Point location = inventoryItems[off].getComponent().getCenter();
				render.drawString("" + inventoryItems[off].getID(), location.x, location.y);
			}
		}
	}
}
