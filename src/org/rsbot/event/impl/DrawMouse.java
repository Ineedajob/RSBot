package org.rsbot.event.impl;

import java.awt.Color;
import java.awt.Graphics;

import org.rsbot.accessors.Client;
import org.rsbot.bot.Bot;
import org.rsbot.bot.input.Mouse;
import org.rsbot.event.listeners.PaintListener;

public class DrawMouse implements PaintListener {
	
	private Client client;

    public DrawMouse(Bot bot) {
    	client = bot.getClient();
    }

    public void onRepaint(final Graphics render) {
    	Mouse mouse = client.getMouse();
    	if (mouse != null) {
    		final int mouse_x = mouse.getClientX();
            final int mouse_y = mouse.getClientY();
            final int mouse_press_x = mouse.getClientPressX();
            final int mouse_press_y = mouse.getClientPressY();
            final long mouse_press_time = mouse.getClientPressTime();

            render.setColor(Color.YELLOW);
            render.drawLine(mouse_x - 7, mouse_y - 7, mouse_x + 7, mouse_y + 7);
            render.drawLine(mouse_x + 7, mouse_y - 7, mouse_x - 7, mouse_y + 7);
            if (System.currentTimeMillis() - mouse_press_time < 1000) {
                render.setColor(Color.RED);
                render.drawLine(mouse_press_x - 7, mouse_press_y - 7, mouse_press_x + 7, mouse_press_y + 7);
                render.drawLine(mouse_press_x + 7, mouse_press_y - 7, mouse_press_x - 7, mouse_press_y + 7);
            }

            if (mouse.isRealPresent()) {
            	int x = mouse.getRealX();
            	int y = mouse.getRealY();
                render.setColor(mouse.isRealPressed() ? Color.WHITE : Color.BLACK);
                render.drawLine(x - 7, y - 7, x + 7, y + 7);
                render.drawLine(x + 7, y - 7, x - 7, y + 7);
            }
    	}
    }
}
