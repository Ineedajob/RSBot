package org.rsbot;

import org.rsbot.gui.BotGUI;
import org.rsbot.util.logging.LoggingBootstrap;

public class Application {

	private static BotGUI gui;

	public static BotGUI getGUI() {
		return gui;
	}

	public static void main(final String[] args) {
		LoggingBootstrap.bootstrap();
		new ScriptExtract(args).run();
		gui = new BotGUI();
	}
}
