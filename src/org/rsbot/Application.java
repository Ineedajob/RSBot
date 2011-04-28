package org.rsbot;

import org.rsbot.bot.Bot;
import org.rsbot.gui.BotGUI;
import org.rsbot.log.LogOutputStream;
import org.rsbot.log.SystemConsoleHandler;
import org.rsbot.util.Extractor;

import java.awt.*;
import java.io.PrintStream;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Application {
	private static BotGUI gui;

	public static void main(final String[] args) throws Exception {
		bootstrap();
		new Extractor(args).run();
		gui = new BotGUI();
		gui.setVisible(true);
		gui.addBot();
		// stats();
	}

	/**
	 * Returns the Bot for any object loaded in its client. For internal use
	 * only (not useful for script writers).
	 *
	 * @param o Any object from within the client.
	 * @return The Bot for the client.
	 */
	public static Bot getBot(Object o) {
		return gui.getBot(o);
	}

	/**
	 * Returns the size of the panel that clients should be drawn into. For
	 * internal use.
	 *
	 * @return The client panel size.
	 */
	public static Dimension getPanelSize() {
		return gui.getPanel().getSize();
	}

	private static void bootstrap() {
		Logger.getLogger("").setLevel(Level.ALL);
		Logger.getLogger("").addHandler(new SystemConsoleHandler());
		Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
			private final Logger log = Logger.getLogger("EXCEPTION");

			public void uncaughtException(final Thread t, final Throwable e) {
				log.logp(Level.SEVERE, "EXCEPTION", "", "Unhandled exception in thread " + t.getName() + ": ", e);
			}
		});
		System.setErr(new PrintStream(new LogOutputStream(Logger.getLogger("STDERR"), Level.SEVERE), true));
	}

/*	private static void stats() {
		sync(true);
		Runtime.getRuntime().addShutdownHook(new Thread() {

			public void run() {
				sync(false);
			}
		});
	}

	*//**
	 * Notifies the project site of bot activity anonymously for
	 * general statistics. Although this anonymous submission can
	 * easily be abused, this will give some useful data over time.
	 *
	 * @param start <tt>true</tt> if sending startup message;
	 *              <tt>false</tt> if sending shutdown message.
	 *//*
	private static void sync(boolean start) {
		try {
			URL url = new URL(GlobalConfiguration.Paths.URLs.STATS);
			HttpURLConnection connect = (HttpURLConnection) url.openConnection();
			connect.setRequestMethod("GET");
			connect.setDoOutput(true);
			connect.setDoInput(true);
			connect.setUseCaches(false);
			connect.setAllowUserInteraction(false);
			StringBuilder write = new StringBuilder("s=");
			if (start) {
				write.append("1");
				startTime = System.currentTimeMillis();
			} else {
				write.append("0&t=").append((System.currentTimeMillis() - startTime) /
						60000);
			}
			Writer writer = new OutputStreamWriter(connect.getOutputStream(),
					"UTF-8");
			writer.write(write.toString());
			writer.flush();
			writer.close();
			connect.disconnect();
		} catch (IOException ignored) {
		}
	}*/
}
