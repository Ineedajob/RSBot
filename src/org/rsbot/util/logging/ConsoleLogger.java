package org.rsbot.util.logging;

import java.util.logging.ConsoleHandler;

/**
 * Logs to System.out
 */
public class ConsoleLogger extends ConsoleHandler {
	public ConsoleLogger() {
		super();
		setOutputStream(System.out);
	}
}
