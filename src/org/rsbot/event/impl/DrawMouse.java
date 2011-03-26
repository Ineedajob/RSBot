package org.rsbot.event.impl;

import org.rsbot.bot.Bot;
import org.rsbot.client.Client;
import org.rsbot.client.input.Mouse;
import org.rsbot.event.listeners.PaintListener;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Point;
import java.awt.RadialGradientPaint;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.geom.Point2D;
import java.util.LinkedList;
import java.util.List;

public class DrawMouse implements PaintListener {

	private Client client;
	private MouseTrail trail = new MouseTrail();
	private List<Particle> clicks = new LinkedList<Particle>();

	public DrawMouse(Bot bot) {
		client = bot.getClient();
	}

	public void onRepaint(final Graphics render) {
		Mouse mouse = client.getMouse();
		if (mouse != null) {
			Point Location = new Point(mouse.getX(), mouse.getY());
			if (mouse.isPressed())
				if (clicks.size() <= 5)
					clicks.add(new Particle(System.currentTimeMillis(), Location));
			trail.draw((Graphics2D) render, Color.red);
			render.setColor(Color.GREEN);
			render.drawLine(Location.x - 5, Location.y, Location.x + 5, Location.y);
			render.drawLine(Location.x, Location.y - 5, Location.x, Location.y + 5);
			if (!clicks.isEmpty())
				for (Particle click : clicks) {
					if (mouse.isPressed())
						if (clicks.size() <= 5)
							clicks.add(new Particle(System.currentTimeMillis(), Location));
					if (click.getAge() < 1000) {
						render.setColor(Color.red);
						render.drawLine(click.getLocation().x - 5, click
								.getLocation().y, click.getLocation().x + 5,
								click.getLocation().y);
						render.drawLine(click.getLocation().x, click
								.getLocation().y - 5, click.getLocation().x,
								click.getLocation().y + 5);
					} else {
						clicks.remove(click);
					}
				}

		}
	}

	/**
	 * @author Baheer (Doout).
	 */
	public class MouseTrail {

		private int lifeTime = 1500;
		private LinkedList<Particle> trail = new LinkedList<Particle>();
		private Point prev = new Point(-1, -1);

		private void getLocation() {
			Point m = new Point(client.getMouse().getX(), client.getMouse().getY());
			if (accept(m)) {
				prev = m;
				trail.add(new Particle(System.currentTimeMillis(), m));
			}
		}

		private boolean accept(Point p) {
			if (p.equals(prev)) {
				return false;
			}
			for (Particle t : trail) {
				if (t.getLocation().equals(p)) {
					return false;
				}
			}
			return true;
		}

		public void draw(Graphics2D g, Color c) {
			getLocation();
			g.setRenderingHints(new RenderingHints(
					RenderingHints.KEY_TEXT_ANTIALIASING,
					RenderingHints.VALUE_TEXT_ANTIALIAS_ON));
			if (!trail.isEmpty())
				for (Particle particle : trail) {
					int i = (int) (lifeTime - particle.getAge()) / 100;
					if (i >= 2) {
						Rectangle rect = new Rectangle(particle.getLocation().x - i / 2,
								particle.getLocation().y - i / 2, i, i);
						Paint prevPaint = g.getPaint();
						RadialGradientPaint gradPaint = new RadialGradientPaint(
								new Point2D.Double(rect.x + rect.width / 2.0D,
										rect.y + rect.height / 2.0D),
										(float) (rect.getWidth() / 2.0D),
								new float[] { 0.0F, 1.0F }, new Color[] {
										new Color(c.getRed(), c.getGreen(), c.getBlue(), 40),
										new Color(0.0F, 0.0F, 0.0F, 0.4F) });
						g.setPaint(gradPaint);
						g.fillRoundRect(rect.x, rect.y,
								rect.width, rect.height,
								rect.width, rect.height);
						g.setPaint(prevPaint);
					}
				}
			if (!trail.isEmpty()) {
				if (trail.getFirst().getAge() > 1000) {
					trail.removeFirst();
				}
			}
		}

	}

	private class Particle {

		private long time;
		private Point location;

		public Particle(long time, Point loc) {
			this.time = time;
			location = loc;
		}

		public long getAge() {
			return System.currentTimeMillis() - time;
		}

		public Point getLocation() {
			return location;
		}

	}

}
