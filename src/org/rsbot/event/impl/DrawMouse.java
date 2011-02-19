package org.rsbot.event.impl;

import org.rsbot.bot.Bot;
import org.rsbot.client.Client;
import org.rsbot.client.input.Mouse;
import org.rsbot.event.listeners.PaintListener;

import java.awt.*;
import java.awt.geom.Point2D;
import java.util.ArrayList;

public class DrawMouse implements PaintListener {

	private Client client;
	private MouseTrailing mouseTrailing = new MouseTrailing();
	private ArrayList<Mouses> Click = new ArrayList<Mouses>();

	public DrawMouse(Bot bot) {
		client = bot.getClient();
	}

	public void onRepaint(final Graphics render) {
		Mouse mouse = client.getMouse();
		if (mouse != null) {
			Point Location = new Point(mouse.getX(), mouse.getY());
			if (mouse.isPressed())
				if (Click.size() <= 5)
					Click.add(new Mouses(System.currentTimeMillis(), Location));
			mouseTrailing.draw((Graphics2D) render, Color.red);
			render.setColor(Color.GREEN);
			render.drawLine(Location.x - 5, Location.y, Location.x + 5,
					Location.y);
			render.drawLine(Location.x, Location.y - 5, Location.x,
					Location.y + 5);
			if (Click.size() > 1)
				for (Mouses click : Click) {
					if (mouse.isPressed())
						if (Click.size() <= 5)
							Click.add(new Mouses(System.currentTimeMillis(),
									Location));
					if (System.currentTimeMillis() - click.getTime() < 1000) {
						render.setColor(Color.red);
						render.drawLine(click.getLocation().x - 5, click
								.getLocation().y, click.getLocation().x + 5,
								click.getLocation().y);
						render.drawLine(click.getLocation().x, click
								.getLocation().y - 5, click.getLocation().x,
								click.getLocation().y + 5);
					} else if (System.currentTimeMillis() - click.getTime() >= 1000) {
						Click.remove(click);
					}
				}

		}
	}

	/**
	 * @author Baheer (Doout).
	 */
	public class MouseTrailing {
		private int LifeTime = 1500;
		private ArrayList<Mouses> Info = new ArrayList<Mouses>();

		private void getLocation() {
			Point m = new Point(client.getMouse().getX(), client.getMouse()
					.getY());
			if (Add(m)) {
				Info.add(new Mouses(System.currentTimeMillis(), m));
			}
		}

		public boolean Add(Point p) {
			for (Mouses Trailing : Info)
				if (Trailing.getLocation().equals(p))
					return false;
			return true;
		}

		public void draw(Graphics2D g, Color c) {
			getLocation();
			g.setRenderingHints(new RenderingHints(
					RenderingHints.KEY_TEXT_ANTIALIASING,
					RenderingHints.VALUE_TEXT_ANTIALIAS_ON));
			if (Info.size() > 1)
				for (Mouses Trailing : Info) {
					int i = (int) (Trailing.getTime() + LifeTime - System
							.currentTimeMillis()) / 100;
					if (i >= 2) {
						Rectangle localRectangle = new Rectangle(Trailing
								.getLocation().x
								- i / 2, Trailing.getLocation().y - i / 2, i, i);
						java.awt.Paint localPaint = g.getPaint();
						RadialGradientPaint localRadialGradientPaint = new RadialGradientPaint(
								new Point2D.Double(localRectangle.x
										+ localRectangle.width / 2.0D,
										localRectangle.y
												+ localRectangle.height / 2.0D),
								(float) (localRectangle.getWidth() / 2.0D),
								new float[] { 0.0F, 1.0F }, new Color[] {
										new Color(c.getRed(), c.getGreen(), c
												.getBlue(), 40),
										new Color(0.0F, 0.0F, 0.0F, 0.4F) });
						g.setPaint(localRadialGradientPaint);
						g.fillRoundRect(localRectangle.x, localRectangle.y,
								localRectangle.width, localRectangle.height,
								localRectangle.width, localRectangle.height);
						g.setPaint(localPaint);
					}
				}
			if (Info.size() > 1) {
				if (System.currentTimeMillis() > Info.get(0).getTime() + 1000) {
					Info.remove(0);
				}
			}
		}

	}

	private class Mouses {
		private long Time;
		private Point Location;

		public Mouses(long time, Point loc) {
			Time = time;
			Location = loc;
		}

		public long getTime() {
			return Time;
		}

		public Point getLocation() {
			return Location;
		}
	}
}
