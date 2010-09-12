package org.rsbot;

import java.awt.Dimension;
import java.io.PrintStream;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.rsbot.bot.Bot;
import org.rsbot.gui.BotGUI;
import org.rsbot.log.LogOutputStream;
import org.rsbot.log.SystemConsoleHandler;
import org.rsbot.util.Extractor;

public class Application {
	
	private static BotGUI gui;

	public static void main(final String[] args) {
		bootstrap();
		new Extractor(args).run();
		gui = new BotGUI();
		gui.setVisible(true);
	}
	
	public static Bot getBot(Object o) {
		return gui.getBot(o);
	}
	
	public static Dimension getPanelSize() {
		return gui.getPanel().getSize();
	}

	public static void bootstrap() {
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
