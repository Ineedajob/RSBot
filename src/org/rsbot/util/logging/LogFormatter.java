package org.rsbot.util.logging;

import java.util.Date;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;

import org.rsbot.util.ThrowableUtils;

public class LogFormatter extends Formatter {
	private static final String LINE_SEPARATOR = System.getProperty("line.separator");

	public final boolean newLine;

	public LogFormatter() {
		super();
		newLine = true;
	}

	public LogFormatter(final boolean newLine) {
		this.newLine = newLine;
	}

	@Override
	public String format(final LogRecord record) {
		final StringBuilder result = new StringBuilder().append("[").append(record.getLevel().getName()).append("] ").append(new Date(record.getMillis())).append(": ").append(record.getLoggerName()).append(": ").append(record.getMessage()).append(ThrowableUtils.throwableToString(record.getThrown()));

		if (newLine) {
			result.append(LogFormatter.LINE_SEPARATOR);
		}

		return result.toString();
	}
}
