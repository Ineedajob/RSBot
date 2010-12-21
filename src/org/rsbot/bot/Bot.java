package org.rsbot.bot;

import org.rsbot.Application;
import org.rsbot.client.Client;
import org.rsbot.client.input.Canvas;
import org.rsbot.event.EventManager;
import org.rsbot.event.events.PaintEvent;
import org.rsbot.event.events.TextPaintEvent;
import org.rsbot.gui.AccountManager;
import org.rsbot.script.internal.BreakHandler;
import org.rsbot.script.internal.InputManager;
import org.rsbot.script.internal.ScriptHandler;
import org.rsbot.script.methods.Environment;
import org.rsbot.script.methods.MethodContext;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.lang.reflect.Constructor;
import java.util.EventListener;
import java.util.Map;
import java.util.TreeMap;

public class Bot {

	private String account;
	private BotStub botStub;
	private Client client;
	private MethodContext methods;
	private Component panel;
	private PaintEvent paintEvent;
	private TextPaintEvent textPaintEvent;
	private EventManager eventManager;
	private BufferedImage backBuffer;
	private BufferedImage frontBuffer;
	private BufferedImage image;
	private InputManager im;
	private RSLoader loader;
	private ScriptHandler sh;
	private BreakHandler bh;
	private Map<String, EventListener> listeners;

	/**
	 * Whether or not user input is allowed despite a script's preference.
	 */
	public volatile boolean overrideInput = false;

	/**
	 * Whether or not all anti-randoms are enabled.
	 */
	public volatile boolean disableRandoms = false;

	/**
	 * Whether or not the login screen anti-random is enabled.
	 */
	public volatile boolean disableAutoLogin = false;

	/**
	 * Whether or not rendering is enabled.
	 */
	public volatile boolean disableRendering = false;

	/**
	 * Defines what types of input are enabled when overrideInput is false.
	 * Defaults to 'keyboard only' whenever a script is started.
	 */
	public volatile int inputMask = Environment.INPUT_KEYBOARD | Environment.INPUT_MOUSE;

	public Bot() {
		im = new InputManager(this);
		loader = new RSLoader();
		final Dimension size = Application.getPanelSize();
		loader.setCallback(new Runnable() {
			public void run() {
				setClient((Client) loader.getClient());
				resize(size.width, size.height);
				methods.menu.setupListener();
			}
		});
		sh = new ScriptHandler(this);
		bh = new BreakHandler();
		frontBuffer = new BufferedImage(size.width, size.height, BufferedImage.TYPE_INT_RGB);
		backBuffer = new BufferedImage(size.width, size.height, BufferedImage.TYPE_INT_RGB);
		image = new BufferedImage(size.width, size.height, BufferedImage.TYPE_INT_RGB);
		paintEvent = new PaintEvent();
		textPaintEvent = new TextPaintEvent();
		paintLoading();
		eventManager = new EventManager();
		listeners = new TreeMap<String, EventListener>();
	}

	private void paintLoading() {
		Graphics graphics = frontBuffer.getGraphics();
		Font font = new Font("Helvetica", 1, 13);
		FontMetrics fontMetrics = loader.getFontMetrics(font);
		graphics.setColor(Color.black);
		graphics.fillRect(0, 0, 768, 503);
		graphics.setColor(Color.RED);
		graphics.drawRect(232, 232, 303, 33);
		String s = "Loading...";
		graphics.setFont(font);
		graphics.setColor(Color.WHITE);
		graphics.drawString(s, (768 - fontMetrics.stringWidth(s)) / 2, 255);
	}

	public void start() {
		try {
			loader.loadClasses();
			botStub = new BotStub(loader);
			loader.setStub(botStub);
			eventManager.start();
			botStub.setActive(true);
			ThreadGroup tg = new ThreadGroup("RSClient");
			Thread thread = new Thread(tg, loader, "Loader");
			thread.start();
		} catch (Exception ignored) {
		}
	}

	public void stop() {
		eventManager.killThread(false);
		sh.stopScript();
		loader.stop();
		loader.destroy();
		loader = null;
	}

	public void resize(int width, int height) {
		frontBuffer = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		backBuffer = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		// client reads size of loader applet for drawing
		loader.setSize(width, height);
		// simulate loader repaint awt event dispatch
		loader.update(backBuffer.getGraphics());
		loader.paint(backBuffer.getGraphics());
	}

	public boolean setAccount(final String name) {
		boolean exist = false;
		for (String s : AccountManager.getAccountNames()) {
			if (s.toLowerCase().equals(name.toLowerCase())) {
				exist = true;
			}
		}
		if (exist) {
			account = name;
			return true;
		}
		account = null;
		return false;
	}

	public void setPanel(Component c) {
		this.panel = c;
	}

	public void addListener(Class<?> clazz) {
		EventListener el = instantiateListener(clazz);
		listeners.put(clazz.getName(), el);
		eventManager.addListener(el);
	}

	public void removeListener(Class<?> clazz) {
		EventListener el = listeners.get(clazz.getName());
		listeners.remove(clazz.getName());
		eventManager.removeListener(el);
	}

	public boolean hasListener(Class<?> clazz) {
		return clazz != null && listeners.get(clazz.getName()) != null;
	}

	public String getAccountName() {
		return account;
	}

	public Client getClient() {
		return client;
	}

	public Canvas getCanvas() {
		if (client == null) {
			return null;
		}
		return (Canvas) client.getCanvas();
	}

	public Graphics getBufferGraphics() {
		Graphics front = frontBuffer.getGraphics();
		front.drawImage(backBuffer, 0, 0, null);
		paintEvent.graphics = front;
		textPaintEvent.graphics = front;
		textPaintEvent.idx = 0;
		eventManager.processEvent(paintEvent);
		eventManager.processEvent(textPaintEvent);
		front.dispose();
		image.getGraphics().drawImage(frontBuffer, 0, 0, null);
		if (panel != null) {
			panel.repaint();
		}
		return backBuffer.getGraphics();
	}

	public BufferedImage getImage() {
		return image;
	}

	public BotStub getBotStub() {
		return botStub;
	}

	public RSLoader getLoader() {
		return loader;
	}

	public MethodContext getMethodContext() {
		return methods;
	}

	public EventManager getEventManager() {
		return eventManager;
	}

	public InputManager getInputManager() {
		return im;
	}

	public BreakHandler getBreakHandler() {
		return bh;
	}

	public ScriptHandler getScriptHandler() {
		return sh;
	}

	private void setClient(final Client cl) {
		client = cl;
		client.setCallback(new CallbackImpl(this));
		methods = new MethodContext(this);
		sh.init();
	}

	private EventListener instantiateListener(Class<?> clazz) {
		try {
			EventListener listener;
			try {
				Constructor<?> constructor = clazz.getConstructor(Bot.class);
				listener = (EventListener) constructor.newInstance(this);
			} catch (Exception e) {
				listener = clazz.asSubclass(EventListener.class).newInstance();
			}
			return listener;
		} catch (Exception ignored) {
		}
		return null;
	}

}
