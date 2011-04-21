package org.rsbot.event.impl;

import org.rsbot.bot.Bot;
import org.rsbot.client.Client;
import org.rsbot.client.input.Mouse;
import org.rsbot.event.listeners.PaintListener;
import org.rsbot.injection.Injector;

import java.awt.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class DrawMouse implements PaintListener {

	private final Client client;
	private final List<Cross> clicks = new LinkedList<Cross>();
	private final Object lock = new Object();
	private final ArrayList<Particle> p = new ArrayList<Particle>();
	private Color[] fadeArray = {Color.red, Color.white, Color.green, new Color(128, 0, 128), Color.yellow,
			Color.black, Color.orange, Color.pink};
	private Point lastPoint = new Point(0, 0);
	static java.util.Random generator = new java.util.Random();

	public static int random(int min, int max) {
		int n = Math.abs(max - min);
		return Math.min(min, max) + (n == 0 ? 0 : generator.nextInt(n));
	}

	public DrawMouse(Bot bot) {
		client = bot.getClient();
	}

	private double getRot() {
		return System.currentTimeMillis() % 3600 / 10.0D;
	}

	public void onRepaint(final Graphics render) {//TODO optimize.
		Mouse mouse = client.getMouse();
		if (mouse != null) {
			final Point location = new Point(mouse.getX(), mouse.getY());
			Graphics2D g = (Graphics2D) render.create();
			Graphics2D gg = (Graphics2D) render.create();
			g.setColor(new Color(128, 0, 128));
			g.rotate(Math.toRadians(getRot()), location.x, location.y);
			g.drawLine(location.x, location.y - 5, location.x, location.y + 5);
			g.drawLine(location.x - 5, location.y, location.x + 5, location.y);
			if (mouse.isPressed() && ((clicks.size() > 0 && clicks.get(clicks.size() - 1).getAge() > 100 && clicks.get(
					clicks.size() - 1).getStart() != mouse.getPressTime()) || clicks.size() == 0)) {
				Cross newCross = new Cross(1500, mouse.getPressTime(), location, getRot());
				if (!clicks.contains(newCross)) {
					clicks.add(newCross);
				}
			}
			if (Injector.easterMode) {
				int x = mouse.getX();
				int y = mouse.getY();
				if (Point.distance(x, y, lastPoint.x, lastPoint.y) > 15) {
					lastPoint = new Point(x, y);
					synchronized (lock) {
						for (int i = 0; i < 50; i++, p.add(new Particle(x, y, fadeArray[random(0, fadeArray.length)]))) {
							;
						}
					}
				}
				if (mouse.isPressed()) {
					lastPoint = new Point(x, y);
					synchronized (lock) {
						for (int i = 0; i < 50; i++, p.add(new Particle(x, y, Color.green.darker()))) {
							;
						}
					}
				}
				synchronized (lock) {
					Iterator<Particle> partIter = p.iterator();
					while (partIter.hasNext()) {
						Particle part = partIter.next();
						if (!part.handle(render)) {
							partIter.remove();
						}
					}
				}
			}
			synchronized (lock) {
				Iterator<Cross> clickIterator = clicks.listIterator();
				while (clickIterator.hasNext()) {
					Cross toDraw = clickIterator.next();
					if (toDraw.handle()) {
						drawPoint(toDraw.getLocation(), toDraw.getRot(), gg, toDraw.getAlpha());
					} else {
						clicks.remove(toDraw);
					}
				}
			}
		}
	}

	private void drawPoint(Point location, double rot, Graphics2D g, int al) {
		Graphics2D g1 = (Graphics2D) g.create();
		g1.setColor(new Color(Color.pink.getRed(), Color.pink.getBlue(), Color.pink.getGreen(), al));
		g1.rotate(rot, location.x, location.y);
		g1.drawLine(location.x, location.y - 5, location.x, location.y + 5);
		g1.drawLine(location.x - 5, location.y, location.x + 5, location.y);
	}

	private class Cross {

		private final long time, st;
		private final Point location;
		private final double rot;

		public Cross(long lifetime, long st, Point loc, double rot) {
			this.time = System.currentTimeMillis() + lifetime;
			location = loc;
			this.rot = rot;
			this.st = st;
		}

		public long getStart() {
			return st;
		}

		public long getAge() {
			return time - System.currentTimeMillis();
		}

		public int getAlpha() {
			return Math.min(255, Math.max(0, (int) (256.0D * (getAge() / 1500.0D))));
		}

		public boolean handle() {
			return System.currentTimeMillis() <= time;
		}

		public double getRot() {
			return rot;
		}

		public Point getLocation() {
			return location;
		}

		@Override
		public boolean equals(Object o) {
			if (o instanceof Cross) {
				Cross oo = (Cross) o;
				return oo.location.equals(this.location);
			}
			return false;
		}

	}

	private static class Particle {

		private double posX;
		private double posY;
		private double movX;
		private double movY;
		private int alpha = 255;
		private Color color;

		Particle(int pos_x, int pos_y, Color color) {
			posX = (double) pos_x;
			posY = (double) pos_y;
			movX = ((double) generator.nextInt(40) - 20) / 16;
			movY = ((double) generator.nextInt(40) - 20) / 16;
			this.color = color;
		}

		public boolean handle(Graphics page) {
			alpha -= random(1, 7);
			if (alpha <= 0) {
				return false;
			}
			page.setColor(new Color(color.getRed(), color.getBlue(), color.getGreen(), alpha));
			page.drawLine((int) posX, (int) posY, (int) posX, (int) posY);
			posX += movX;
			posY += movY;
			return true;
		}
	}

}
