package org.rsbot.client.input;

import java.awt.*;
import java.awt.event.*;

public abstract class Mouse extends Focus implements MouseListener,
		MouseMotionListener, MouseWheelListener {

	private int clientX;
	private int clientY;
	private int clientPressX = -1;
	private int clientPressY = -1;
	private long clientPressTime = -1;
	private boolean clientPresent;
	private boolean clientPressed;

	public abstract void _mouseClicked(MouseEvent e);

	public abstract void _mouseDragged(MouseEvent e);

	public abstract void _mouseEntered(MouseEvent e);

	public abstract void _mouseExited(MouseEvent e);

	public abstract void _mouseMoved(MouseEvent e);

	public abstract void _mousePressed(MouseEvent e);

	public abstract void _mouseReleased(MouseEvent e);

	public abstract void _mouseWheelMoved(MouseWheelEvent e);

	public abstract Component getComponent();

	public int getX() {
		return clientX;
	}

	public int getY() {
		return clientY;
	}

	public int getPressX() {
		return clientPressX;
	}

	public int getPressY() {
		return clientPressY;
	}

	public long getPressTime() {
		return clientPressTime;
	}

	public boolean isPressed() {
		return clientPressed;
	}

	public boolean isPresent() {
		return clientPresent;
	}

	public final void mouseClicked(MouseEvent e) {
		clientX = e.getX();
		clientY = e.getY();
		_mouseClicked(e);
		e.consume();
	}

	public final void mouseDragged(MouseEvent e) {
		clientX = e.getX();
		clientY = e.getY();
		_mouseDragged(e);
		e.consume();
	}

	public final void mouseEntered(MouseEvent e) {
		clientPresent = true;
		clientX = e.getX();
		clientY = e.getY();
		_mouseEntered(e);
		e.consume();
	}

	public final void mouseExited(MouseEvent e) {
		clientPresent = false;
		clientX = e.getX();
		clientY = e.getY();
		_mouseExited(e);
		e.consume();
	}

	public final void mouseMoved(MouseEvent e) {
		clientX = e.getX();
		clientY = e.getY();
		_mouseMoved(e);
		e.consume();
	}

	public final void mousePressed(MouseEvent e) {
		clientPressed = true;
		clientX = e.getX();
		clientY = e.getY();
		_mousePressed(e);
		e.consume();
	}

	public final void mouseReleased(MouseEvent e) {
		clientX = e.getX();
		clientY = e.getY();
		clientPressX = e.getX();
		clientPressY = e.getY();
		clientPressTime = System.currentTimeMillis();
		clientPressed = false;

		_mouseReleased(e);
		e.consume();
	}

	public void mouseWheelMoved(MouseWheelEvent e) {
		try {
			_mouseWheelMoved(e);
		} catch (AbstractMethodError ame) {
			// it might not be implemented!
		}
		e.consume();
	}

	public final void sendEvent(MouseEvent e) {
		this.clientX = e.getX();
		this.clientY = e.getY();
		try {
			if (e.getID() == MouseEvent.MOUSE_CLICKED) {
				_mouseClicked(e);
			} else if (e.getID() == MouseEvent.MOUSE_DRAGGED) {
				_mouseDragged(e);
			} else if (e.getID() == MouseEvent.MOUSE_ENTERED) {
				clientPresent = true;
				_mouseEntered(e);
			} else if (e.getID() == MouseEvent.MOUSE_EXITED) {
				clientPresent = false;
				_mouseExited(e);
			} else if (e.getID() == MouseEvent.MOUSE_MOVED) {
				_mouseMoved(e);
			} else if (e.getID() == MouseEvent.MOUSE_PRESSED) {
				clientPressed = true;
				_mousePressed(e);
			} else if (e.getID() == MouseEvent.MOUSE_RELEASED) {
				clientPressX = e.getX();
				clientPressY = e.getY();
				clientPressTime = System.currentTimeMillis();
				clientPressed = false;
				_mouseReleased(e);
			} else if (e.getID() == MouseEvent.MOUSE_WHEEL) {
				try {
					_mouseWheelMoved((MouseWheelEvent) e);
				} catch (AbstractMethodError ignored) {
					// it might not be implemented!
				}
			} else {
				throw new InternalError(e.toString());
			}
		} catch (NullPointerException ignored) {
			// client may throw NPE when a listener
			// is being re-instantiated.
		}
	}
}
