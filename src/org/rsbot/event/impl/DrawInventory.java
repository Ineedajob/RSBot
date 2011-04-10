package org.rsbot.event.impl;

import org.rsbot.bot.Bot;
import org.rsbot.event.listeners.PaintListener;
import org.rsbot.script.methods.Game;
import org.rsbot.script.methods.MethodContext;
import org.rsbot.script.wrappers.RSItem;

import java.awt.*;

public class DrawInventory implements PaintListener {

	private final MethodContext ctx;

	public DrawInventory(Bot bot) {
		ctx = bot.getMethodContext();
	}

	public void onRepaint(final Graphics render) {
		if (!ctx.game.isLoggedIn()) {
			return;
		}

		if (ctx.game.getCurrentTab() != Game.TAB_INVENTORY) {
			return;
		}

		render.setColor(Color.WHITE);
		final RSItem[] inventoryItems = ctx.inventory.getItems();

		for (RSItem inventoryItem : inventoryItems) {
			if (inventoryItem.getID() != -1) {
				final Point location = inventoryItem.getComponent().getCenter();
				render.drawString("" + inventoryItem.getID(), location.x, location.y);
			}
		}
	}
}
