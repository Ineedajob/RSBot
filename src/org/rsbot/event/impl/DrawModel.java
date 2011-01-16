package org.rsbot.event.impl;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Polygon;
import java.util.HashMap;

import org.rsbot.bot.Bot;
import org.rsbot.event.listeners.PaintListener;
import org.rsbot.script.methods.MethodContext;
import org.rsbot.script.wrappers.RSGroundItem;
import org.rsbot.script.wrappers.RSModel;
import org.rsbot.script.wrappers.RSObject;

//Draws a nice wire frame on interactable objects on screen.
//Thanks to Kosaki for his initial idea and part of this code:)
public class DrawModel implements PaintListener {

	private static final HashMap<RSObject.Type, Color> color_map = new HashMap<RSObject.Type, Color>();

	static {
		color_map.put(RSObject.Type.BOUNDARY, Color.BLACK);
		color_map.put(RSObject.Type.FLOOR_DECORATION, Color.YELLOW);
		color_map.put(RSObject.Type.INTERACTABLE, Color.WHITE);
		color_map.put(RSObject.Type.WALL_DECORATION, Color.GRAY);
	}

	private MethodContext ctx;

	public DrawModel(Bot bot) {
		this.ctx = bot.getMethodContext();
	}

	public void onRepaint(Graphics render) {
		for (org.rsbot.script.wrappers.RSObject o : ctx.objects.getAll()) {
			RSModel model = o.getModel();
			if (model != null) {
				render.setColor(color_map.get(o.getType()));
				for (Polygon polygon : model.getTriangles()) {
					render.drawPolygon(polygon);
				}
				render.setColor(Color.GREEN);
				Point p = model.getPoint();
				render.fillOval(p.x - 1, p.y - 1, 2, 2);
			}
		}

		for (org.rsbot.script.wrappers.RSCharacter c : ctx.players.getAll()) {
			RSModel model = c.getModel();
			if (model != null) {
				render.setColor(Color.RED);
				for (Polygon polygon : model.getTriangles()) {
					render.drawPolygon(polygon);
				}
			}
		}

		for (org.rsbot.script.wrappers.RSCharacter c : ctx.npcs.getAll()) {
			RSModel model = c.getModel();
			if (model != null) {
				render.setColor(Color.MAGENTA);
				for (Polygon polygon : model.getTriangles()) {
					render.drawPolygon(polygon);
				}
			}
		}
		
		for(RSGroundItem item : ctx.groundItems.getAll()) {
			RSModel model = item.getModel();
			if(model != null) {
				render.setColor(Color.CYAN);
				for (Polygon polygon : model.getTriangles()) {
					render.drawPolygon(polygon);
				}
			}
		}
	}
}
