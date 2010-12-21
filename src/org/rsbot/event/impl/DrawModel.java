package org.rsbot.event.impl;

import org.rsbot.bot.Bot;
import org.rsbot.event.listeners.PaintListener;
import org.rsbot.script.methods.MethodContext;
import org.rsbot.script.wrappers.RSModel;
import org.rsbot.script.wrappers.RSObject;

import java.awt.*;
import java.util.HashMap;

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
		/*StatusQueue snl = new StatusQueue(ctx.client.getRSInteractingDefList());
				for (StatusNode sn = snl.getTail(); sn != null; sn = snl.getNext()) {
					//See if it's an RSInteractableDef, since users reported it sometimes failed.
					//In my opinion that shouldn't be possible, unless the list doesn't work properly.
					//However better check then spitting out errors.
					if (!(sn instanceof RSInteractableDef))
						continue;

					RSInteractableDef cur = (RSInteractableDef) sn;
					if (cur.getRSInteractable() instanceof RSObject && cur.getRSInteractable() instanceof RSAnimable) {
						render.setColor(Color.YELLOW);

						RSObject object = (RSObject) cur.getRSInteractable();
						RSAnimable animable = (RSAnimable) object;
						Model model;
						try { model = object.getModel(); } catch (AbstractMethodError e) { continue; }
						if (model == null || !(model instanceof LDModel))
							continue;

						//Calculate screen coords of the model
						Point[] screenCoords = new Point[model.getXPoints().length];
						for (int i = 0; i < screenCoords.length; i++) {
							int x = model.getXPoints()[i] + animable.getX();
							int z = model.getZPoints()[i] + animable.getY();
							int y = model.getYPoints()[i] + ctx.calc.tileHeight(animable.getX(), animable.getY());
							screenCoords[i] = ctx.calc.worldToScreen(x, y, z);
						}

						int[] xPoints = new int[4];
						int[] yPoints = new int[4];

						int length = ((LDModel) model).getIndices3().length;
						for (int i = 0; i < length; i++) {
							int index1 = ((LDModel) model).getIndices1()[i];
							if (screenCoords[index1].x == -1 || screenCoords[index1].y == -1)
								continue;

							xPoints[0] = screenCoords[index1].x;
							yPoints[0] = screenCoords[index1].y;
							xPoints[3] = screenCoords[index1].x;
							yPoints[3] = screenCoords[index1].y;

							int index2 = ((LDModel) model).getIndices2()[i];
							if (screenCoords[index2].x == -1 || screenCoords[index2].y == -1)
								continue;

							xPoints[1] = screenCoords[index2].x;
							yPoints[1] = screenCoords[index2].y;

							int index3 = ((LDModel) model).getIndices3()[i];
							if (screenCoords[index3].x == -1 || screenCoords[index3].y == -1)
								continue;

							xPoints[2] = screenCoords[index3].x;
							yPoints[2] = screenCoords[index3].y;

							render.drawPolyline(xPoints, yPoints, 4);
						}
					}
				}*/

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
	}
}
