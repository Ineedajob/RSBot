package org.rsbot.event.impl;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Polygon;

import org.rsbot.bot.Bot;
import org.rsbot.event.listeners.PaintListener;
import org.rsbot.script.methods.MethodContext;
import org.rsbot.script.wrappers.RSDoor;
import org.rsbot.script.wrappers.RSTile;

public class DrawDoors implements PaintListener {

	private final MethodContext ctx;

	public DrawDoors(Bot bot) {
		ctx = bot.getMethodContext();
	}

	@Override
	public void onRepaint(Graphics render) {
		Graphics2D g = (Graphics2D) render;
		g.setColor(new Color(1f, 0f, 0f, 0.2f));
		RSDoor[] doors = ctx.doors.getAll();
		for (RSDoor d : doors) {
			if (d.isDoor()) {
				g.setColor(d.isOpen() ? new Color(0f, 1f, 0f, 0.25f)
						: new Color(1f, 0f, 0f, 0.25f));
				Polygon p = getMinimapPolygon(d.getLocation());
				Polygon pS = getScreenPolygon(d.getLocation());
				if (pS != null)
					g.fill(pS);
				g.setColor(d.isOpen() ? new Color(0f, 1f, 0f, 0.75f)
						: new Color(1f, 0f, 0f, 0.75f));
				if (p != null)
					g.fill(p);
			}
		}
	}

	public Polygon getScreenPolygon(final RSTile t) {
		if (t != null) {
			Polygon poly = new Polygon();
			Point pt = ctx.calc.tileToScreen(t, 0, 0, 0);
			poly.addPoint(pt.x, pt.y);
			pt = ctx.calc.tileToScreen(t, 0, 1, 0);
			poly.addPoint(pt.x, pt.y);
			pt = ctx.calc.tileToScreen(t, 1, 1, 0);
			poly.addPoint(pt.x, pt.y);
			pt = ctx.calc.tileToScreen(t, 1, 0, 0);
			poly.addPoint(pt.x, pt.y);
			for (int i = 0; i < poly.npoints; i++)
				if (poly.xpoints[i] < 0 || poly.ypoints[i] < 0)
					return null;
			return poly;
		}
		return null;
	}

	public Polygon getMinimapPolygon(final RSTile t) {
		if (t != null) {
			Polygon poly = new Polygon();
			Point pt = ctx.calc.tileToMinimap(t);
			poly.addPoint(pt.x, pt.y);
			pt = ctx.calc.tileToMinimap(new RSTile(t.getX(), t.getY() + 1));
			poly.addPoint(pt.x, pt.y);
			pt = ctx.calc.tileToMinimap(new RSTile(t.getX() + 1, t.getY() + 1));
			poly.addPoint(pt.x, pt.y);
			pt = ctx.calc.tileToMinimap(new RSTile(t.getX() + 1, t.getY()));
			poly.addPoint(pt.x, pt.y);
			for (int i = 0; i < poly.npoints; i++)
				if (poly.xpoints[i] < 0 || poly.ypoints[i] < 0)
					return null;
			return poly;
		}
		return null;
	}
}
