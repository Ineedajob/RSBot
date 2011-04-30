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
}
