package org.rsbot.util.logging;

import java.io.PrintStream;
import java.util.logging.Level;
import java.util.logging.Logger;

public class LoggingBootstrap {
	private static final Logger uncaughtExceptionLog = Logger.getLogger("EXCEPTION");
	private static final Logger errLog = Logger.getLogger("STDERR");

	public static void bootstrap() {
		Logger.getLogger("").addHandler(new ConsoleLogger());

		Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
			public void uncaughtException(final Thread t, final Throwable e) {
				LoggingBootstrap.uncaughtExceptionLog.logp(Level.SEVERE, "EXCEPTION", "", "Unhandled exception in thread " + t.getName() + ": ", e);
			}
		});

		System.setErr(new PrintStream(new LoggingOutputStream(LoggingBootstrap.errLog, Level.SEVERE), true));
	}
}
