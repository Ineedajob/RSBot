package org.rsbot.script.methods;

import org.rsbot.script.internal.MouseHandler;

import java.awt.*;

/**
 * Mouse related operations.
 */
public class Mouse extends MethodProvider {

	private int mouseSpeed = MouseHandler.DEFAULT_MOUSE_SPEED;

	Mouse(final MethodContext ctx) {
		super(ctx);
	}

	/**
	 * Moves the mouse randomly between the two distances.
	 *
	 * @param minDistance The minimum distance to move.
	 * @param maxDistance The maximum distance to move.
	 * @see #moveRandomly(int)
	 * @see #getRandomX(int)
	 * @see #getRandomY(int)
	 */
	public void moveRandomly(int minDistance, int maxDistance) {
		if (minDistance == 0) {
			minDistance = 2;
		}
		if (minDistance > maxDistance) {
			int temp = minDistance;
			minDistance = maxDistance;
			maxDistance = temp;
		}
		move(getRandomX(random(minDistance, maxDistance)),
				getRandomY(random(minDistance, maxDistance)));
	}

	/**
	 * Moves the mouse randomly between the maximum distance.
	 *
	 * @param maxDistance The maximum distance to move.
	 * @see #getRandomX(int)
	 * @see #getRandomY(int)
	 */
	public void moveRandomly(int maxDistance) {
		move(getRandomX(random(maxDistance / 2, maxDistance)),
				getRandomY(random(maxDistance / 2, maxDistance)));
		if (random(0, 10) == 0) {
			moveRandomly(maxDistance / 2);
		}
	}

	/**
	 * Moves the mouse off the screen in a random direction.
	 */
	public void moveOffScreen() {
		if (isPresent()) {
			switch (random(0, 4)) {
				case 0: // up
					move(random(-10, methods.game.getWidth() + 10),
							random(-100, -10));
					break;
				case 1: // down
					move(random(-10, methods.game.getWidth() + 10),
							methods.game.getHeight() + random(10, 100));
					break;
				case 2: // left
					move(random(-100, -10),
							random(-10, methods.game.getHeight() + 10));
					break;
				case 3: // right
					move(random(10, 100) + methods.game.getWidth(),
							random(-10, methods.game.getHeight() + 10));
					break;
			}
		}
	}

	/**
	 * Drag the mouse from the current position to a certain other position.
	 *
	 * @param x The x coordinate to drag to.
	 * @param y The y coordinate to drag to.
	 */
	public void drag(int x, int y) {
		methods.inputManager.dragMouse(x, y);
	}

	/**
	 * Drag the mouse from the current position to a certain other position.
	 *
	 * @param p The point to drag to.
	 * @see #drag(int, int)
	 */
	public void drag(Point p) {
		drag(p.x, p.y);
	}


	/**
	 * Clicks the mouse at its current location.
	 *
	 * @param leftClick <tt>true</tt> to left-click, <tt>false</tt>to right-click.
	 */
	public void click(boolean leftClick) {
		click(leftClick, MouseHandler.DEFAULT_MAX_MOVE_AFTER);
	}

	public synchronized void click(boolean leftClick, int moveAfterDist) {
		methods.inputManager.clickMouse(leftClick);
		if (moveAfterDist > 0) {
			sleep(random(50, 350));
			Point pos = getLocation();
			move(pos.x - moveAfterDist, pos.y - moveAfterDist,
					moveAfterDist * 2, moveAfterDist * 2);
		}
	}

	/**
	 * Moves the mouse to a given location then clicks.
	 *
	 * @param x		 x coordinate
	 * @param y		 y coordinate
	 * @param leftClick <tt>true</tt> to left-click, <tt>false</tt>to right-click.
	 */
	public void click(int x, int y, boolean leftClick) {
		click(x, y, 0, 0, leftClick);
	}

	/**
	 * Moves the mouse to a given location with given randomness then clicks.
	 *
	 * @param x		 x coordinate
	 * @param y		 y coordinate
	 * @param randX	 x randomness (added to x)
	 * @param randY	 y randomness (added to y)
	 * @param leftClick <tt>true</tt> to left-click, <tt>false</tt>to right-click.
	 * @see #move(int, int, int, int)
	 */
	public synchronized void click(int x, int y, int randX, int randY, boolean leftClick) {
		move(x, y, randX, randY);
		sleep(random(50, 350));
		click(leftClick, MouseHandler.DEFAULT_MAX_MOVE_AFTER);
	}

	/**
	 * Moves the mouse to a given location with given randomness then clicks,
	 * then moves a random distance up to <code>afterOffset</code>.
	 *
	 * @param x			 x coordinate
	 * @param y			 y coordinate
	 * @param randX		 x randomness (added to x)
	 * @param randY		 y randomness (added to y)
	 * @param leftClick	 <tt>true</tt> to left-click, <tt>false</tt>to right-click.
	 * @param moveAfterDist The maximum distance in pixels to move on both axes shortly
	 *                      after moving to the destination.
	 */
	public synchronized void click(int x, int y, int randX, int randY, boolean leftClick,
								   int moveAfterDist) {
		move(x, y, randX, randY);
		sleep(random(50, 350));
		click(leftClick, moveAfterDist);
	}

	/**
	 * Moves the mouse to a given location then clicks.
	 *
	 * @param p		 The point to click.
	 * @param leftClick <tt>true</tt> to left-click, <tt>false</tt>to right-click.
	 */
	public void click(Point p, boolean leftClick) {
		click(p.x, p.y, leftClick);
	}

	public void click(Point p, int x, int y, boolean leftClick) {
		click(p.x, p.y, x, y, leftClick);
	}

	/**
	 * Moves the mouse to a given location with given randomness then clicks,
	 * then moves a random distance up to <code>afterOffset</code>.
	 *
	 * @param p			 The destination Point.
	 * @param x			 x coordinate
	 * @param y			 y coordinate
	 * @param leftClick	 <tt>true</tt> to left-click, <tt>false</tt>to right-click.
	 * @param moveAfterDist The maximum distance in pixels to move on both axes shortly
	 *                      after moving to the destination.
	 */
	public void click(Point p, int x, int y, boolean leftClick, int moveAfterDist) {
		click(p.x, p.y, x, y, leftClick, moveAfterDist);
	}

	/**
	 * Moves the mouse slightly depending on where it currently is and clicks.
	 */
	public void clickSlightly() {
		Point p = new Point(
				(int) (getLocation().getX() + (Math.random() * 50 > 25 ?
						1 : -1) * (30 + Math.random() * 90)),
				(int) (getLocation().getY() + (Math.random() * 50 > 25 ?
						1 : -1) * (30 + Math.random() * 90)));
		if (p.getX() < 1 || p.getY() < 1 || p.getX() > 761 || p.getY() > 499) {
			clickSlightly();
			return;
		}
		click(p, true);
	}

	/**
	 * Gets the mouse speed.
	 *
	 * @return the current mouse speed.
	 * @see #setSpeed(int)
	 */
	public int getSpeed() {
		return mouseSpeed;
	}

	/**
	 * Changes the mouse speed
	 *
	 * @param speed The speed to move the mouse at. 4-10 is advised, 1 being the fastest.
	 * @see #getSpeed()
	 */
	public void setSpeed(int speed) {
		mouseSpeed = speed;
	}

	/**
	 * Moves mouse to location (x,y) at default speed.
	 *
	 * @param x x coordinate
	 * @param y y coordinate
	 * @see #move(int, int, int, int)
	 * @see #setSpeed(int)
	 */
	public void move(int x, int y) {
		move(x, y, 0, 0);
	}

	/**
	 * @see #move(int, int, int, int, int, int)
	 */
	public void move(int x, int y, int afterOffset) {
		move(getSpeed(), x, y, 0, 0, afterOffset);
	}

	/**
	 * Moves the mouse to the specified point at default speed.
	 *
	 * @param x	 The x destination.
	 * @param y	 The y destination.
	 * @param randX x-axis randomness (added to x).
	 * @param randY y-axis randomness (added to y).
	 * @see #move(int, int, int, int, int, int)
	 * @see #setSpeed(int)
	 */
	public void move(int x, int y, int randX, int randY) {
		move(getSpeed(), x, y, randX, randY, 0);
	}

	/**
	 * Moves the mouse to the specified point at a certain speed.
	 *
	 * @param speed The lower, the faster.
	 * @param x	 The x destination.
	 * @param y	 The y destination.
	 * @param randX x-axis randomness (added to x).
	 * @param randY y-axis randomness (added to y).
	 * @see #move(int, int, int, int, int, int)
	 */
	public void move(int speed, int x, int y, int randX, int randY) {
		move(speed, x, y, randX, randY, 0);
	}

	/**
	 * Moves the mouse to the specified point at a certain speed,
	 * then moves a random distance up to <code>afterOffset</code>.
	 *
	 * @param speed	   The lower, the faster.
	 * @param x		   The x destination.
	 * @param y		   The y destination.
	 * @param randX	   X-axis randomness (added to x).
	 * @param randY	   X-axis randomness (added to y).
	 * @param afterOffset The maximum distance in pixels to move on both axes shortly
	 *                    after moving to the destination.
	 */
	public synchronized void move(int speed, int x, int y, int randX, int randY,
								  int afterOffset) {
		if (x != -1 || y != -1) {
			methods.inputManager.moveMouse(speed, x, y, randX, randY);
			if (afterOffset > 0) {
				sleep(random(60, 300));
				Point pos = getLocation();
				move(pos.x - afterOffset, pos.y - afterOffset, afterOffset * 2,
						afterOffset * 2);
			}
		}
	}

	/**
	 * @see #move(int, int, int, int, int, int)
	 */
	public void move(int speed, Point p) {
		move(speed, p.x, p.y, 0, 0, 0);
	}

	/**
	 * @see #move(int, int, int, int)
	 */
	public void move(Point p) {
		move(p.x, p.y, 0, 0);
	}

	/**
	 * @see #move(int, int, int, int, int, int)
	 */
	public void move(Point p, int afterOffset) {
		move(getSpeed(), p.x, p.y, 0, 0, afterOffset);
	}

	/**
	 * @see #move(int, int, int, int)
	 */
	public void move(Point p, int randX, int randY) {
		move(p.x, p.y, randX, randY);
	}

	/**
	 * @see #move(int, int, int, int, int, int)
	 */
	public void move(Point p, int randX, int randY, int afterOffset) {
		move(getSpeed(), p.x, p.y, randX, randY, afterOffset);
	}

	/**
	 * Hops mouse to the specified coordinate.
	 *
	 * @param x The x coordinate.
	 * @param y The y coordinate
	 */
	public synchronized void hop(int x, int y) {
		methods.inputManager.hopMouse(x, y);
	}

	/**
	 * Hops mouse to the specified point.
	 *
	 * @param p The coordinate point.
	 * @see #hop(Point)
	 */
	public void hop(Point p) {
		hop(p.x, p.y);
	}

	/**
	 * Hops mouse to the certain coordinate.
	 *
	 * @param x	 The x coordinate.
	 * @param y	 The y coordinate.
	 * @param randX The x coordinate randomization.
	 * @param randY The y coordinate randomization.
	 * @see #hop(int, int)
	 */
	public void hop(int x, int y, int randX, int randY) {
		hop(x + random(-randX, randX), y + random(-randX, randY));
	}

	/**
	 * Hops mouse to the certain point.
	 *
	 * @param p	 The coordinate point.
	 * @param randX The x coordinate randomization.
	 * @param randY The y coordinate randomization.
	 * @see #hop(int, int, int, int)
	 */
	public void hop(Point p, int randX, int randY) {
		hop(p.x, p.y, randX, randY);
	}

	/**
	 * Moves the mouse slightly depending on where it currently is.
	 */
	public void moveSlightly() {
		Point p = new Point(
				(int) (getLocation().getX() + (Math.random() * 50 > 25 ? 1
						: -1)
						* (30 + Math.random() * 90)),
				(int) (getLocation().getY() + (Math.random() * 50 > 25 ? 1
						: -1)
						* (30 + Math.random() * 90)));
		if (p.getX() < 1 || p.getY() < 1 || p.getX() > 761 || p.getY() > 499) {
			moveSlightly();
			return;
		}
		move(p);
	}

	/**
	 * @param maxDistance The maximum distance outwards.
	 * @return A random x value between the current client location and the max distance outwards.
	 */
	public int getRandomX(int maxDistance) {
		Point p = getLocation();
		if (p.x < 0)
			return -1;
		if (random(0, 2) == 0)
			return p.x - random(0, p.x < maxDistance ? p.x : maxDistance);
		else {
			int dist = methods.game.getWidth() - p.x;
			return p.x + random(1, dist < maxDistance && dist > 0 ? dist : maxDistance);
		}
	}

	/**
	 * @param maxDistance The maximum distance outwards.
	 * @return A random y value between the current client location and the max distance outwards.
	 */
	public int getRandomY(int maxDistance) {
		Point p = getLocation();
		if (p.y < 0)
			return -1;
		if (random(0, 2) == 0)
			return p.y - random(0, p.y < maxDistance ? p.y : maxDistance);
		else {
			int dist = methods.game.getHeight() - p.y;
			return p.y + random(1, dist < maxDistance && dist > 0 ? dist : maxDistance);
		}
	}

	/**
	 * The location of the bot's mouse; or Point(-1, -1) if off screen.
	 *
	 * @return A <tt>Point</tt> containing the bot's mouse's x & y coordinates.
	 */
	public Point getLocation() {
		org.rsbot.client.input.Mouse m = methods.client.getMouse();
		return new Point(m.getX(), m.getY());
	}

	/**
	 * @return The <tt>Point</tt> at which the bot's mouse was last clicked.
	 */
	public Point getPressLocation() {
		org.rsbot.client.input.Mouse m = methods.client.getMouse();
		return new Point(m.getPressX(), m.getPressY());
	}

	/**
	 * @return The system time when the bot's mouse was last pressed.
	 */
	public long getPressTime() {
		org.rsbot.client.input.Mouse mouse = methods.client.getMouse();
		return mouse == null ? 0 : mouse.getPressTime();
	}

	/**
	 * @return <tt>true</tt> if the bot's mouse is present.
	 */
	public boolean isPresent() {
		org.rsbot.client.input.Mouse mouse = methods.client.getMouse();
		return mouse != null && mouse.isPresent();
	}

	/**
	 * @return <tt>true</tt> if the bot's mouse is pressed.
	 */
	public boolean isPressed() {
		org.rsbot.client.input.Mouse mouse = methods.client.getMouse();
		return mouse != null && mouse.isPressed();
	}

}
