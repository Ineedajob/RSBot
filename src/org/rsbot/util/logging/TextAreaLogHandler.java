package org.rsbot.util.logging;

import java.util.logging.Handler;
import java.util.logging.LogRecord;

import org.rsbot.gui.LogTextArea;

public class TextAreaLogHandler extends Handler {
	public static final LogTextArea textArea = new LogTextArea();

	@Override
	public void close() throws SecurityException {
	}

	@Override
	public void flush() {
	}

	@Override
	public void publish(final LogRecord record) {
		TextAreaLogHandler.textArea.log(record);
	}
}
