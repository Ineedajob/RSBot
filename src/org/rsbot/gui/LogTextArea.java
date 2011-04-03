package org.rsbot.gui;

import java.awt.Color;
import java.awt.Cursor;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JTextPane;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;
import javax.swing.text.StyledDocument;

import org.rsbot.log.LogFormatter;

/**
 * Non swing methods are thread safe.
 */
public class LogTextArea extends JTextPane {
	private static final long serialVersionUID = 1L;
	private final LogFormatter formatter = new LogFormatter(false);
	HashMap<String, Color> colorMap = new HashMap<String, Color>();

	/**
	 * Wrap the log records so we can control the copy paste text (via
	 * #toString)
	 */

	private class WrappedLogRecord {
		public final LogRecord record;
		@SuppressWarnings("unused")
		public final String formatted;
		public final Level level;

		public WrappedLogRecord(final LogRecord record) {
			this.record = record;
			formatted = formatter.format(record);
			level = record.getLevel();
		}

		/**
		 * Returns log level
		 */
		public Level getLevel() {
			return level;
		}

		/**
		 * Returns time of the log
		 */
		public String getTimeStamp() {
			return formatter.formatTimestamp(record);
		}

		/**
		 * Returns class that sent the log
		 */
		public String getLogClass() {
			return formatter.formatClass(record);
		}

		/**
		 * Returns log message
		 */
		public String getMessage() {
			return formatter.formatMessage(record);
		}

		/**
		 * Returns error
		 */
		public String getError() {
			return formatter.formatError(record);
		}

		@Override
		public String toString() {
			return formatter.formatTimestamp(record) + "  "
					+ formatter.formatClass(record) + "  "
					+ formatter.formatMessage(record);
		}
	}

	public LogTextArea() {
		super();
		setCursor(Cursor.getPredefinedCursor(Cursor.TEXT_CURSOR));
		setEditable(false);
	}

	public void appendTime(String timestamp) {
		StyleContext sc = new StyleContext();
		AttributeSet fg = sc.addAttribute(SimpleAttributeSet.EMPTY,
				StyleConstants.Foreground, Color.WHITE);
		AttributeSet bg = sc.addAttribute(SimpleAttributeSet.EMPTY,
				StyleConstants.Background, Color.BLACK);
		AttributeSet bold = sc.addAttribute(SimpleAttributeSet.EMPTY,
				StyleConstants.Bold, false);
		Document doc = getDocument();

		setCaretPosition(doc.getLength());

		setCharacterAttributes(fg, false);
		setCharacterAttributes(bg, false);
		setCharacterAttributes(bold, false);

		replaceSelection(timestamp);
	}

	public void appendName(String name) {
		StyleContext sc = new StyleContext();
		AttributeSet fg = sc.addAttribute(SimpleAttributeSet.EMPTY,
				StyleConstants.Foreground, scriptColor(name));
		AttributeSet bg = sc.addAttribute(SimpleAttributeSet.EMPTY,
				StyleConstants.Background, Color.WHITE);
		AttributeSet bold = sc.addAttribute(SimpleAttributeSet.EMPTY,
				StyleConstants.Bold, true);
		Document doc = getDocument();

		setCaretPosition(doc.getLength());

		setCharacterAttributes(fg, false);
		setCharacterAttributes(bg, false);
		setCharacterAttributes(bold, false);

		replaceSelection("  " + name + "\t");
	}

	public void appendMessage(WrappedLogRecord record) {
		String msg = "  " + record.getMessage() + record.getError();
		Document doc = getDocument();
		setCaretPosition(doc.getLength());
		boolean boldtext = false;

		Color foreground = null;
		Color background = Color.WHITE;

		if (record.getLevel() == Level.SEVERE) {
			foreground = Color.WHITE;
			background = Color.RED;
			boldtext = true;
		} else if (record.getLevel() == Level.WARNING) {
			foreground = Color.RED;
		} else if (record.getLevel() == Level.INFO) {
			foreground = Color.DARK_GRAY;
		} else if (record.getLevel() == Level.CONFIG) {
			foreground = Color.BLUE;
		} else if (record.getLevel() == Level.FINE
				|| record.getLevel() == Level.FINER
				|| record.getLevel() == Level.FINEST) {
			foreground = Color.DARK_GRAY;
		}

		if (msg.contains("Script started") || msg.contains("Script stopped")) {
			foreground = scriptColor(record.getLogClass());
			boldtext = true;
		} else if (record.getLogClass().contains("BotGUI")
				&& !(msg.contains("paused") || msg.contains("started"))) {
			foreground = new Color(0, 150, 0);
			boldtext = true;
		} else if (record.getLogClass().contains("BotGUI")
				&& (msg.contains("paused") || msg.contains("started"))) {
			foreground = new Color(255, 64, 0);
			boldtext = true;
		} else if (record.getLogClass().contains("LoginBot")
				|| msg.contains("Random event started")) {
			foreground = Color.BLUE;
			boldtext = true;
		}

		StyleContext sc = new StyleContext();
		AttributeSet fg = sc.addAttribute(SimpleAttributeSet.EMPTY,
				StyleConstants.Foreground, foreground);
		AttributeSet bg = sc.addAttribute(SimpleAttributeSet.EMPTY,
				StyleConstants.Background, background);
		AttributeSet bold = sc.addAttribute(SimpleAttributeSet.EMPTY,
				StyleConstants.Bold, boldtext);

		setCharacterAttributes(fg, false);
		setCharacterAttributes(bg, false);
		setCharacterAttributes(bold, false);

		final Style boldStyle = sc.addStyle("ConstantWidth", null);
		StyleConstants.setBold(boldStyle, true);

		final Style underlineStyle = sc.addStyle("ConstantWidth", null);
		StyleConstants.setBold(underlineStyle, true);
		StyleConstants.setUnderline(underlineStyle, true);

		replaceSelection(msg + "\n");

		Pattern p = Pattern.compile("\\(((\\w+?)\\.java):(\\d+)\\)");

		Matcher matcher = null;
		try {
			matcher = p.matcher(doc.getText(0, doc.getLength()));
		} catch (BadLocationException e) {
		}

		while (matcher.find()) {
			((StyledDocument) getDocument()).setCharacterAttributes(
					matcher.start(2), matcher.end(2) - matcher.start(2),
					boldStyle, false);
			((StyledDocument) getDocument()).setCharacterAttributes(
					matcher.start(3), matcher.end(3) - matcher.start(3),
					underlineStyle, false);
		}
	}

	/**
	 * Returns a random color based off of script name. Same name will always
	 * return same color.
	 * 
	 * @param String
	 * @return Color
	 */
	public Color scriptColor(String name) {
		if (colorMap.containsKey(name))
			return colorMap.get(name);
		MessageDigest coder;
		try {
			coder = MessageDigest.getInstance("MD5");
			coder.update(name.getBytes());
			byte[] bytes = coder.digest();
			long result1 = 0, result2 = 0;
			for (int i = 0; i < 8; i++)
				result1 = (result1 << 8) | bytes[i];
			for (int i = 8; i < 16; i++)
				result2 = (result2 << 8) | bytes[i];
			long seed = result1 - result2;
			Random r = new Random(seed);

			Color c = new Color(Math.abs(r.nextInt() % 128), Math.abs(r
					.nextInt() % 128), Math.abs(r.nextInt() % 128));
			colorMap.put(name, c);
			return c;
		} catch (NoSuchAlgorithmException ex) {
			return Color.BLACK;
		}
	}

	public synchronized void addRecord(LogRecord LogRecord) {
		if (getDocument().getLength() > 100000) {
			try {
				getDocument().remove(0, 50000);
			} catch (BadLocationException e) {
				e.printStackTrace();
			}
		}

		WrappedLogRecord record = new WrappedLogRecord(LogRecord);
		appendTime(record.getTimeStamp());
		appendName(record.getLogClass());
		appendMessage(record);
	}

	/**
	 * Logs a new entry to be shown in the list. Thread safe
	 */
	public void log(final LogRecord logRecord) {
		setEditable(true);
		addRecord(logRecord);
		setEditable(false);
	}
}