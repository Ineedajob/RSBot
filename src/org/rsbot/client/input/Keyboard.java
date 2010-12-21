package org.rsbot.client.input;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

/**
 * @author Alex
 */
public abstract class Keyboard extends Focus implements KeyListener {
	public abstract void _keyPressed(KeyEvent e);

	public abstract void _keyReleased(KeyEvent e);

	public abstract void _keyTyped(KeyEvent e);

	public void keyPressed(final KeyEvent e) {
		// System.out.println(("KP");
		_keyPressed(e);
	}

	public void keyReleased(final KeyEvent e) {
		// System.out.println(("KR");
		_keyReleased(e);
	}

	public void keyTyped(final KeyEvent e) {
		// System.out.println(("KT");
		_keyTyped(e);
	}
}
