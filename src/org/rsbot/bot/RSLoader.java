package org.rsbot.bot;

import org.rsbot.Application;
import org.rsbot.client.Loader;
import org.rsbot.loader.ClientLoader;
import org.rsbot.loader.script.ParseException;
import org.rsbot.util.GlobalConfiguration;

import java.applet.Applet;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Qauters
 */
public class RSLoader extends Applet implements Runnable, Loader {
	private final Logger log = Logger.getLogger(RSLoader.class.getName());

	private static final long serialVersionUID = 6288499508495040201L;

	/**
	 * The applet of the client
	 */
	private Applet client;

	private Runnable loadedCallback;

	private String targetName;

	private Dimension size = Application.getPanelSize();

	/**
	 * The game class loader
	 */
	private RSClassLoader classLoader;

	@Override
	public final synchronized void destroy() {
		if (client != null) {
			client.destroy();
		}
	}

	@Override
	public boolean isShowing() {
		return true;
	}

	@Override
	public final synchronized void init() {
		if (client != null) {
			client.init();
		}
	}

	@Override
	public final void paint(final Graphics graphics) {
		if (client != null) {
			client.paint(graphics);
		} else {
			Font font = new Font("Helvetica", 1, 13);
			FontMetrics fontMetrics = getFontMetrics(font);
			graphics.setColor(Color.black);
			graphics.fillRect(0, 0, 768, 503);
			graphics.setColor(new Color(150, 0, 0));
			graphics.drawRect(230, 233, 303, 33);
			String s = "Loading...";
			graphics.setFont(font);
			graphics.setColor(Color.WHITE);
			graphics.drawString(s, (768 - fontMetrics.stringWidth(s)) / 2, 255);
		}
	}

	/**
	 * The run void of the loader
	 */
	public void run() {
		try {
			Class<?> c = classLoader.loadClass("client");
			client = (Applet) c.newInstance();
			loadedCallback.run();
			c.getMethod("provideLoaderApplet", new Class[]{java.applet.Applet.class}).invoke(null, this);
			client.init();
			client.start();
		} catch (final Throwable e) {
			log.severe("Unable to load client, please check your firewall and internet connection.");
			File versionFile = new File(GlobalConfiguration.Paths.getVersionCache());
			if (versionFile.exists() && !versionFile.delete()) {
				log.warning("Unable to clear cache.");
			}

			log.log(Level.SEVERE, "Error reason:", e);
		}
	}

	public Applet getClient() {
		return client;
	}

	public void load() {
		try {
			WebLoader webLoader = new WebLoader();
			if (webLoader.load()) {
				ClientLoader cl = new ClientLoader();
				cl.init(new URL(GlobalConfiguration.Paths.URLs.UPDATE),
						new File(GlobalConfiguration.Paths.getModScriptCache()));
				cl.load(new File(GlobalConfiguration.Paths.getClientCache()),
						new File(GlobalConfiguration.Paths.getVersionCache()));
				targetName = cl.getTargetName();
				classLoader = new RSClassLoader(cl.getClasses(), new URL("http://" + targetName + ".com/"));
			} else {
				log.severe("Unable to download web data.");
			}
		} catch (IOException ex) {
			log.severe("Unable to load client - " + ex.getMessage());
		} catch (ParseException ex) {
			log.severe("Unable to load client - " + ex.toString());
		}
	}

	public void setCallback(final Runnable r) {
		loadedCallback = r;
	}

	public String getTargetName() {
		return targetName;
	}

	/**
	 * Overridden void start()
	 */
	@Override
	public final synchronized void start() {
		if (client != null) {
			client.start();
		}
	}

	/**
	 * Overridden void deactivate()
	 */
	@Override
	public final synchronized void stop() {
		if (client != null) {
			client.stop();
		}
	}

	/**
	 * Overridden void update(Graphics)
	 */
	@Override
	public final void update(Graphics graphics) {
		if (client != null) {
			client.update(graphics);
		} else {
			paint(graphics);
		}
	}

	public final void setSize(int width, int height) {
		super.setSize(width, height);
		size = new Dimension(width, height);
	}

	public final Dimension getSize() {
		return size;
	}

	public RSClassLoader getClassLoader() {
		return classLoader;
	}
}
