package org.rsbot.bot.input;

import java.awt.AWTEvent;
import java.awt.Canvas;
import java.awt.Graphics;
import java.awt.GraphicsConfiguration;
import java.awt.event.FocusEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import org.rsbot.Application;
import org.rsbot.bot.Bot;
import org.rsbot.event.EventMulticaster;
import org.rsbot.event.events.PaintUpdateEvent;
import org.rsbot.event.events.TextPaintEvent;

public class CanvasWrapper extends Canvas {

	private static final long serialVersionUID = -2276037172265300477L;
	private static boolean didGraphicsCheck = false;

	private final Logger log = Logger.getLogger(CanvasWrapper.class.getName());

	private BufferedImage botBuffer;
	private int gameWidth = 765;
	private int gameHeight = 503;

	private Bot bot;
	private EventMulticaster eventMulticaster;
	private BufferedImage gameBuffer;
	private PaintUpdateEvent paintEvent;
	private TextPaintEvent textPaintEvent;

	public boolean hasFocus = false;

	public CanvasWrapper() {
		super();
		setup();
	}

	public CanvasWrapper(final GraphicsConfiguration config) {
		super(config);
		setup();
	}

	public BufferedImage getBotBuffer() {
		return botBuffer;
	}

	public int getGameWidth() {
		return gameWidth;
	}

	public int getGameHeight() {
		return gameHeight;
	}

	@Override
	public final Graphics getGraphics() {
		if (bot == null)
			return super.getGraphics();
		if (!CanvasWrapper.didGraphicsCheck) {
			if (bot.getClient().getDetailInfo() != null &&
					bot.getClient().getDetailInfo().getDetailLevel() != 0) {
				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						JOptionPane.showMessageDialog(CanvasWrapper.this,
								new String[]{
								"Your graphics detail level is not set to 'Safe Mode'.",
								"Please go to 'Graphics Options' press 'Custom' and select 'Safe Mode'.",
						}, "Graphics Options", JOptionPane.WARNING_MESSAGE);
					}
				});
				CanvasWrapper.didGraphicsCheck = true;
			}
		}
		final Graphics render = botBuffer.getGraphics();
		render.drawImage(gameBuffer, 0, 0, null);

		try {
			if (eventMulticaster.isEnabled(EventMulticaster.PAINT_EVENT)) {
				paintEvent.graphics = render;
				eventMulticaster.fireEvent(paintEvent);
			}
			if (eventMulticaster.isEnabled(EventMulticaster.TEXT_PAINT_EVENT)) {
				textPaintEvent.graphics = render;
				textPaintEvent.idx = 0;
				eventMulticaster.fireEvent(textPaintEvent);
			}
		} catch (final Throwable e) {
			log.log(Level.WARNING, "", e);
		}
		render.dispose();
		final Graphics g = super.getGraphics();
		try {
			g.drawImage(botBuffer, 0, 0, null);
		} catch (final NullPointerException e) {
		}
		final Graphics g2 = gameBuffer.getGraphics();
		if ((getWidth() != gameWidth) || (getHeight() != gameHeight)) {
			createBufferedImages(getWidth(), getHeight());
		}
		return g2;
	}

	@Override
	protected final void processEvent(final AWTEvent e) {
		if ((e.getID() == MouseEvent.MOUSE_PRESSED) && !hasFocus()) {
			requestFocus();
		}
		if (e instanceof KeyEvent) {
			processEventReal(e);
		} else if (e instanceof MouseEvent) {// TODO move the filtering here
			processEventReal(e);
		} else if (e instanceof FocusEvent) {
			if (!Listener.blocked) { // Block redundant events
				if (e.getID() == FocusEvent.FOCUS_GAINED) {
					if (!hasFocus) {
						processEventReal(e);
					}
				} else if (e.getID() == FocusEvent.FOCUS_LOST) {
					if (hasFocus) {
						processEventReal(e);
					}
				}
			}
		} else {
			log.warning("Unknown event: " + e);
			processEventReal(e);
		}
	}

	public final void processEventReal(final AWTEvent e) {
		if (e.getID() == FocusEvent.FOCUS_GAINED) {
			hasFocus = true;
		} else if (e.getID() == FocusEvent.FOCUS_LOST) {
			hasFocus = false;
		}
		super.processEvent(e);
	}

	private void createBufferedImages(final int width, final int height) {
		botBuffer = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		gameBuffer = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		gameWidth = width;
		gameHeight = height;
	}

	private void setup() {
		try {
			bot = Application.getGUI().getBot();
			createBufferedImages(gameWidth, gameHeight);
			eventMulticaster = bot.getEventManager().getMulticaster();
			textPaintEvent = new TextPaintEvent();
			paintEvent = new PaintUpdateEvent();
		} catch (final Throwable e) {
			log.log(Level.SEVERE, "", e);
		}
	}
}
