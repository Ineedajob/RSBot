package org.rsbot.event.impl;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.HashMap;

import org.rsbot.bot.Bot;
import org.rsbot.event.listeners.PaintListener;
import org.rsbot.script.methods.MethodContext;
import org.rsbot.script.wrappers.RSGroundItem;
import org.rsbot.script.wrappers.RSModel;
import org.rsbot.script.wrappers.RSObject;

//Draws a nice wire frame on interactable objects on screen.
//Thanks to Kosaki for his initial idea and part of this code:)
public class DrawModel implements PaintListener, MouseListener {

	private static final HashMap<RSObject.Type, Color> color_map = new HashMap<RSObject.Type, Color>();

	static {
		color_map.put(RSObject.Type.BOUNDARY, Color.BLACK);
		color_map.put(RSObject.Type.FLOOR_DECORATION, Color.YELLOW);
		color_map.put(RSObject.Type.INTERACTABLE, Color.WHITE);
		color_map.put(RSObject.Type.WALL_DECORATION, Color.GRAY);
	}

	private final static String[] WORD_LIST = { "Objects", "Players", "NPCS",
			"GroundItem" };
	private static boolean[] Click = { true, true, true, true };

	private MethodContext ctx;

	public DrawModel(Bot bot) {
		this.ctx = bot.getMethodContext();
	}

	public void onRepaint(Graphics render) {
		DrawRect(render);
		if (Click[0])
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
		if (Click[1])
			for (org.rsbot.script.wrappers.RSCharacter c : ctx.players.getAll()) {
				RSModel model = c.getModel();
				if (model != null) {
					render.setColor(Color.RED);
					for (Polygon polygon : model.getTriangles()) {
						render.drawPolygon(polygon);
					}
				}
			}
		if (Click[2])
			for (org.rsbot.script.wrappers.RSCharacter c : ctx.npcs.getAll()) {
				RSModel model = c.getModel();
				if (model != null) {
					render.setColor(Color.MAGENTA);
					for (Polygon polygon : model.getTriangles()) {
						render.drawPolygon(polygon);
					}
				}
			}
		if (Click[3])
			for (RSGroundItem item : ctx.groundItems.getAll()) {
				RSModel model = item.getModel();
				if (model != null) {
					render.setColor(Color.CYAN);
					for (Polygon polygon : model.getTriangles()) {
						render.drawPolygon(polygon);
					}
				}
			}
	}

	public final void DrawRect(Graphics render) {
		Color j = Color.BLACK;
		Color w = Color.white;
		for (int i = 0; i < WORD_LIST.length; i++) {
			int alpha = 150;
			render.setColor(new Color(j.getRed(), j.getGreen(), j.getBlue(),
					alpha));
			if (Click[i])
				render.setColor(new Color(w.getRed(), w.getGreen(),
						w.getBlue(), alpha));
			render.fillRect(90 + (80 * i), 3, 80, 12);
			render.setColor(Color.white);
			if (Click[i])
				render.setColor(Color.BLACK);
			render.drawString(WORD_LIST[i], 90 + (80 * i) + 10, 13);
			render.setColor(Color.black);
			render.drawRect(90 + (80 * i), 3, 80, 12);
		}
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		for (int i = 0; i < WORD_LIST.length; i++) {
			Rectangle rect = new Rectangle(90 + (80 * i), 3, 80, 12);
			if (rect.contains(e.getPoint())) {
				Click[i] = !Click[i];
			}
		}
	}

	@Override
	public void mouseEntered(MouseEvent arg0) {

	}

	@Override
	public void mouseExited(MouseEvent arg0) {

	}

	@Override
	public void mousePressed(MouseEvent arg0) {

	}

	@Override
	public void mouseReleased(MouseEvent arg0) {

	}
}
