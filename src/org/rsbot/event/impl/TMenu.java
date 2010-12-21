package org.rsbot.event.impl;

import org.rsbot.bot.Bot;
import org.rsbot.client.Client;
import org.rsbot.event.listeners.TextPaintListener;
import org.rsbot.util.StringUtil;

import java.awt.*;

public class TMenu implements TextPaintListener {

	private Client client;

	public TMenu(Bot bot) {
		client = bot.getClient();
	}

	public int drawLine(Graphics render, int idx) {
		StringUtil.drawLine(render, idx++, "Menu " + (client.isMenuOpen() ? "Open" : "Closed") +
				" & " + (client.isMenuCollapsed() ? "Collapsed" : "Expanded"));
		StringUtil.drawLine(render, idx++, "Menu Location: (" +
				client.getMenuX() + "," + client.getMenuY() + ")");
		StringUtil.drawLine(render, idx++, "Sub-Menu Location: (" +
				client.getSubMenuX() + "," + client.getSubMenuY() + ")");
		StringUtil.drawLine(render, idx++, "Sub-Menu Width: " + client.getSubMenuWidth());
		return idx;
	}
}
