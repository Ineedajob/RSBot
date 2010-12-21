package org.rsbot.event.impl;

import org.rsbot.bot.Bot;
import org.rsbot.client.Client;
import org.rsbot.client.input.Mouse;
import org.rsbot.event.listeners.PaintListener;

import java.awt.*;

public class DrawMouse implements PaintListener {

	private Client client;

	public DrawMouse(Bot bot) {
		client = bot.getClient();
	}

	public void onRepaint(final Graphics render) {
		Mouse mouse = client.getMouse();
		if (mouse != null) {
			int mouse_x = mouse.getX();
			int mouse_y = mouse.getY();
			int mouse_press_x = mouse.getPressX();
			int mouse_press_y = mouse.getPressY();
			long mouse_press_time = mouse.getPressTime();

			render.setColor(Color.GREEN);
			render.drawLine(mouse_x - 7, mouse_y - 7, mouse_x + 7, mouse_y + 7);
			render.drawLine(mouse_x + 7, mouse_y - 7, mouse_x - 7, mouse_y + 7);
			if (System.currentTimeMillis() - mouse_press_time < 1000) {
				render.setColor(Color.RED);
				render.drawLine(mouse_press_x - 7, mouse_press_y - 7, mouse_press_x + 7, mouse_press_y + 7);
				render.drawLine(mouse_press_x + 7, mouse_press_y - 7, mouse_press_x - 7, mouse_press_y + 7);
			}
		}
	}
}
