package org.rsbot.bot;

import org.rsbot.accessors.Loader;
import org.rsbot.util.GlobalConfiguration;
import org.rsbot.util.Injector;

import java.applet.Applet;
import java.awt.*;
import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Qauters This class will be used instead of the their own
 *         loader.
 */
public class RSLoader extends Applet implements Runnable, Loader {
	private final Logger log = Logger.getLogger(RSLoader.class.getName());

	private static final long serialVersionUID = 6288499508495040201L;
	/**
	 * The applet of the client
	 */
	private Applet client;
	private Runnable loadedCallback;
	
	/**
	 * The injector
	 */
	Injector injector = new Injector();

	/**
	 * The rs loader, used to load the classes. This basicaly makes it to
	 * load our classes instead of their.
	 */
	public RSClassLoader rSClassLoader;
	/**
	 * Overridden void destroy()
	 */
	@Override
	public final synchronized void destroy() {
		if (client != null) {
			client.destroy();
		}
	}

	/**
	 * Will return the applet of the client
	 */
	public Applet getClient() {
		return client;
	}

	/**
	 * This will initialize the loader
	 */
	@Override
	public final synchronized void init() {
		if (client != null) {
			client.init();
		}
	}

	/**
	 * Overridden void paint(Graphics)
	 */
	@Override
	public final void paint(final Graphics graphics) {
		if (client != null) {
			client.paint(graphics);
		}
		else
		{
			Font font = new Font("Helvetica", 1,13);
			FontMetrics fontMetrics = getFontMetrics(font);
			graphics.setColor(Color.black);
            graphics.fillRect(0, 0, 768, 503);
            graphics.setColor(Color.RED);
            graphics.drawRect(232, 232, 303, 33);
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
			rSClassLoader = new RSClassLoader(injector);
/*			// Load required classes
			final Class<?> signLink = rSClassLoader.loadClass("SignLink");
			final Constructor<?> sl = signLink.getConstructors()[0];
			int i = 32;
			try {
				final String mode = getParameter("modewhat");
				if (mode != null && !mode.isEmpty()) {
					i += Integer.parseInt(mode);
				}
			} catch (final NumberFormatException e) {
				log.log(Level.WARNING, "", e);
			}
			final Object slInitialized = sl.newInstance(this, i, injector.generateTargetName(), 32);*/
			
			/*  Loader is changed from version #611.
				A small note to this is:
				The loader now loads ClientDiskCache (thats how Jagex calls it) and initializes it.
				This is a static class, however this class is also reinitialized in Signlink.
				This is the reason I didn't do it here, and it seems to work perfectly.
				We now call provideLoaderApplet instead of provideSignlink,
				the loader applet thats provided is 'this', which used to be provided through Signlink
			*/
				
			final Class<?> c = rSClassLoader.loadClass("client");

			// Run client
			client = (Applet) c.newInstance();
			loadedCallback.run();
			c.getMethod("provideLoaderApplet", new Class[] { java.applet.Applet.class }).invoke(null, new Object[] { this });
			client.init();
			client.start();
		} catch (final Exception e) {
			log.log(Level.SEVERE, "Unable to load client, please check your firewall and internet connection");
			e.printStackTrace();
			File versionFile = new File(GlobalConfiguration.Paths.getVersionCache());
			versionFile.delete();
		}
	}

	public void setCallback(final Runnable r) {
		loadedCallback = r;
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
	 * Overridden void stop()
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
	public final void update(final Graphics graphics) {
		if (client != null) {
			client.update(graphics);
		}
	}
}
