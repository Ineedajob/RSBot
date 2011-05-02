package org.rsbot.gui;

import org.rsbot.bot.Bot;
import org.rsbot.log.TextAreaLogHandler;
import org.rsbot.script.PassiveScript;
import org.rsbot.script.Script;
import org.rsbot.script.ScriptManifest;
import org.rsbot.script.internal.PassiveScriptHandler;
import org.rsbot.script.internal.ScriptHandler;
import org.rsbot.script.internal.event.PassiveScriptListener;
import org.rsbot.script.internal.event.ScriptListener;
import org.rsbot.script.methods.Environment;
import org.rsbot.script.util.WindowUtil;
import org.rsbot.service.ScriptDeliveryNetwork;
import org.rsbot.service.TwitterUpdates;
import org.rsbot.service.WebQueue;
import org.rsbot.util.GlobalConfiguration;
import org.rsbot.util.ScreenshotUtil;
import org.rsbot.util.UpdateUtil;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

/**
 * @author Jacmob
 */
public class BotGUI extends JFrame implements ActionListener, ScriptListener, PassiveScriptListener {
	public static final int PANEL_WIDTH = 765, PANEL_HEIGHT = 503, LOG_HEIGHT = 120;
	private static final long serialVersionUID = -5411033752001988794L;
	private static final Logger log = Logger.getLogger(BotGUI.class.getName());
	private BotPanel panel;
	private JScrollPane scrollableBotPanel;
	private BotToolBar toolBar;
	private BotMenuBar menuBar;
	private JScrollPane textScroll;
	private BotHome home;
	private final List<Bot> bots = new ArrayList<Bot>();
	private boolean showAds = true;
	private boolean disableConfirmations = false;
	private static final ScriptDeliveryNetwork sdn = ScriptDeliveryNetwork.getInstance();
	private final List<Bot> noModificationBots = new ArrayList<Bot>();

	public BotGUI() {
		init();
		pack();
		setTitle(null);
		setLocationRelativeTo(getOwner());
		setMinimumSize(getSize());
		setResizable(true);
		menuBar.loadPrefs();
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				JPopupMenu.setDefaultLightWeightPopupEnabled(false);
				ToolTipManager.sharedInstance().setLightWeightPopupEnabled(false);
				if (showAds) {
					new SplashAd(BotGUI.this).display();
				}
				if (GlobalConfiguration.RUNNING_FROM_JAR) {
					UpdateUtil updater = new UpdateUtil(BotGUI.this);
					updater.checkUpdate(false);
				}
				if (GlobalConfiguration.Twitter.ENABLED) {
					TwitterUpdates.loadTweets(GlobalConfiguration.Twitter.MESSAGES);
				}
				(new Thread() {
					public void run() {
						ScriptDeliveryNetwork.getInstance().start();
					}
				}).start();
			}
		});
	}

	@Override
	public void setTitle(String title) {
		String t = GlobalConfiguration.NAME + " v" + GlobalConfiguration.getVersionFormatted();
		final int v = GlobalConfiguration.getVersion(), l = UpdateUtil.getLatestVersion();
		if (v > l) {
			t += " beta";
		}
		if (title != null) {
			t = title + " - " + t;
		}
		super.setTitle(t);
	}

	public void actionPerformed(ActionEvent evt) {
		String action = evt.getActionCommand();
		String menu, option;
		int z = action.indexOf('.');
		if (z == -1) {
			menu = action;
			option = "";
		} else {
			menu = action.substring(0, z);
			option = action.substring(z + 1);
		}
		if (menu.equals("Close")) {
			if (confirmRemoveBot()) {
				int idx = Integer.parseInt(option);
				removeBot(bots.get(idx - 1));
			}
		} else if (menu.equals("File")) {
			if (option.equals("New Bot")) {
				addBot();
			} else if (option.equals("Close Bot")) {
				if (confirmRemoveBot()) {
					removeBot(getCurrentBot());
				}
			} else if (option.equals("Run Script")) {
				Bot current = getCurrentBot();
				if (current != null) {
					showScriptSelector(current);
				}
			} else if (option.equals("Service Key")) {
				serviceKeyQuery(option);
			} else if (option.equals("Stop Script")) {
				Bot current = getCurrentBot();
				if (current != null) {
					showStopScript(current);
				}
			} else if (option.equals("Pause Script")) {
				Bot current = getCurrentBot();
				if (current != null) {
					pauseScript(current);
				}
			} else if (option.equals("Save Screenshot")) {
				Bot current = getCurrentBot();
				if (current != null) {
					ScreenshotUtil.saveScreenshot(current, current.getMethodContext().game.isLoggedIn());
				}
			} else if (option.equals("Exit")) {
				cleanExit();
			}
		} else if (menu.equals("Edit")) {
			if (option.equals("Accounts")) {
				AccountManager.getInstance().showGUI();
			} else if (option.equals("Disable Advertisements")) {
				showAds = !((JCheckBoxMenuItem) evt.getSource()).isSelected();
			} else if (option.equals("Disable Confirmations")) {
				disableConfirmations = ((JCheckBoxMenuItem) evt.getSource()).isSelected();
			} else {
				Bot current = getCurrentBot();
				if (current != null) {
					if (option.equals("Force Input")) {
						boolean selected = ((JCheckBoxMenuItem) evt.getSource()).isSelected();
						current.overrideInput = selected;
						toolBar.setOverrideInput(selected);
					} else if (option.equals("Disable Rendering")) {
						current.disableRendering = ((JCheckBoxMenuItem) evt.getSource()).isSelected();
					} else if (option.equals("Disable Canvas")) {
						current.disableCanvas = ((JCheckBoxMenuItem) evt.getSource()).isSelected();
					} else if (option.equals("Disable Anti-Randoms")) {
						current.disableRandoms = ((JCheckBoxMenuItem) evt.getSource()).isSelected();
					} else if (option.equals("Disable Auto Login")) {
						current.disableAutoLogin = ((JCheckBoxMenuItem) evt.getSource()).isSelected();
					}
				}
			}
		} else if (menu.equals("View")) {
			Bot current = getCurrentBot();
			boolean selected = ((JCheckBoxMenuItem) evt.getSource()).isSelected();
			if (option.equals("Hide Toolbar")) {
				toggleViewState(toolBar, selected);
			} else if (option.equals("Hide Log Window")) {
				toggleViewState(textScroll, selected);
			} else if (current != null) {
				if (option.equals("All Debugging")) {
					for (String key : BotMenuBar.DEBUG_MAP.keySet()) {
						Class<?> el = BotMenuBar.DEBUG_MAP.get(key);
						boolean wasSelected = menuBar.getCheckBox(key).isSelected();
						menuBar.getCheckBox(key).setSelected(selected);
						if (selected) {
							if (!wasSelected) {
								current.addListener(el);
							}
						} else {
							if (wasSelected) {
								current.removeListener(el);
							}
						}
					}
				} else {
					Class<?> el = BotMenuBar.DEBUG_MAP.get(option);
					menuBar.getCheckBox(option).setSelected(selected);
					if (selected) {
						current.addListener(el);
					} else {
						menuBar.getCheckBox("All Debugging").setSelected(false);
						current.removeListener(el);
					}
				}
			}
		} else if (menu.equals("Help")) {
			if (option.equals("Site")) {
				openURL(GlobalConfiguration.Paths.URLs.SITE);
			} else if (option.equals("Project")) {
				openURL(GlobalConfiguration.Paths.URLs.PROJECT);
			} else if (option.equals("About")) {
				JOptionPane.showMessageDialog(this, new String[]{"An open source bot developed by the community.", "Visit " + GlobalConfiguration.Paths.URLs.SITE + "/ for more information."}, "About", JOptionPane.INFORMATION_MESSAGE);
			}
		} else if (menu.equals("Tab")) {
			Bot curr = getCurrentBot();
			menuBar.setBot(curr);
			panel.setBot(curr);
			panel.repaint();
			toolBar.setHome(curr == null);
			if (curr == null) {
				setTitle(null);
				toolBar.setScriptButton(BotToolBar.RUN_SCRIPT);
				toolBar.setOverrideInput(false);
				toolBar.setInputState(Environment.INPUT_KEYBOARD | Environment.INPUT_MOUSE);
				toolBar.updateInputButton();
			} else {
				setTitle(curr.getAccountName());
				Map<Integer, Script> scriptMap = curr.getScriptHandler().getRunningScripts();
				if (scriptMap.size() > 0) {
					if (scriptMap.values().iterator().next().isPaused()) {
						toolBar.setScriptButton(BotToolBar.RESUME_SCRIPT);
					} else {
						toolBar.setScriptButton(BotToolBar.PAUSE_SCRIPT);
					}
				} else {
					toolBar.setScriptButton(BotToolBar.RUN_SCRIPT);
				}
				toolBar.setOverrideInput(curr.overrideInput);
				toolBar.setInputState(curr.inputFlags);
				toolBar.updateInputButton();
			}
		} else if (menu.equals("Run")) {
			Bot current = getCurrentBot();
			if (current != null) {
				showScriptSelector(current);
			}
		} else if (menu.equals("Pause") || menu.equals("Resume")) {
			Bot current = getCurrentBot();
			if (current != null) {
				pauseScript(current);
			}
		} else if (menu.equals("Input")) {
			Bot current = getCurrentBot();
			if (current != null) {
				boolean override = !current.overrideInput;
				current.overrideInput = override;
				menuBar.setOverrideInput(override);
				toolBar.setOverrideInput(override);
				toolBar.updateInputButton();
			}
		}
	}

	private void serviceKeyQuery(String option) {
		String key = (String) JOptionPane.showInputDialog(this, null, option, JOptionPane.QUESTION_MESSAGE, null, null, sdn.getKey());
		if (key == null || key.length() == 0) {
			log.info("Services have been disabled");
		} else if (key.length() != 40) {
			log.warning("Invalid service key");
		} else {
			log.info("Services have been linked to {0}");
		}
	}

	public BotPanel getPanel() {
		return panel;
	}

	public Bot getBot(Object o) {
		ClassLoader cl = o.getClass().getClassLoader();
		for (Bot bot : bots) {
			if (cl == bot.getLoader().getClient().getClass().getClassLoader()) {
				panel.offset();
				return bot;
			}
		}
		return null;
	}

	public void addBot() {
		final int max = 6;
		if (bots.size() >= max && GlobalConfiguration.RUNNING_FROM_JAR) {
			log.warning("Cannot run more than " + Integer.toString(max) + " bots");
			return;
		}
		final Bot bot = new Bot();
		bots.add(bot);
		toolBar.addTab();
		bot.getScriptHandler().addScriptListener(this);
		bot.getPassiveScriptHandler().addScriptListener(this);
		new Thread(new Runnable() {
			public void run() {
				bot.start();
				home.setBots(bots);
			}
		}).start();
	}

	public void removeBot(final Bot bot) {
		int idx = bots.indexOf(bot);
		if (idx >= 0) {
			toolBar.removeTab(idx + 1);
		}
		bots.remove(idx);
		bot.getScriptHandler().stopAllScripts();
		bot.getScriptHandler().removeScriptListener(this);
		bot.getPassiveScriptHandler().stopAllScripts();
		bot.getPassiveScriptHandler().removeScriptListener(this);
		home.setBots(bots);
		new Thread(new Runnable() {
			public void run() {
				bot.stop();
				System.gc();
			}
		}).start();
	}

	void pauseScript(Bot bot) {
		ScriptHandler sh = bot.getScriptHandler();
		Map<Integer, Script> running = sh.getRunningScripts();
		if (running.size() > 0) {
			int id = running.keySet().iterator().next();
			sh.pauseScript(id);
		}
	}

	private Bot getCurrentBot() {
		int idx = toolBar.getCurrentTab() - 1;
		if (idx >= 0) {
			return bots.get(idx);
		}
		return null;
	}

	private void showScriptSelector(Bot bot) {
		if (AccountManager.getAccountNames() == null || AccountManager.getAccountNames().length == 0) {
			JOptionPane.showMessageDialog(this, "No accounts found! Please create one before using the bot.");
			AccountManager.getInstance().showGUI();
		} else if (bot.getMethodContext() == null) {
			JOptionPane.showMessageDialog(this, "The client is not currently loaded!");
		} else {
			new ScriptSelector(this, bot).showGUI();
		}
	}

	private void showStopScript(Bot bot) {
		ScriptHandler sh = bot.getScriptHandler();
		Map<Integer, Script> running = sh.getRunningScripts();
		if (running.size() > 0) {
			int id = running.keySet().iterator().next();
			Script s = running.get(id);
			ScriptManifest prop = s.getClass().getAnnotation(ScriptManifest.class);
			int result = JOptionPane.showConfirmDialog(this, "Would you like to stop the script " + prop.name() + "?", "Script", JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);
			if (result == JOptionPane.OK_OPTION) {
				sh.stopScript(id);
				toolBar.setScriptButton(BotToolBar.RUN_SCRIPT);
			}
		}
	}

	private void toggleViewState(Component component, boolean visible) {
		Dimension size = getSize();
		size.height += component.getSize().height * (visible ? -1 : 1);
		component.setVisible(!visible);
		setMinimumSize(size);
		if ((getExtendedState() & Frame.MAXIMIZED_BOTH) != Frame.MAXIMIZED_BOTH) {
			pack();
		}
	}

	private void lessCpu(final boolean enable) {
		if (enable) {
			noModificationBots.clear();
			for (final Bot bot : bots) {
				if (bot.disableCanvas || bot.disableRendering) {
					noModificationBots.add(bot);
				}
			}
		}
		for (final Bot bot : bots) {
			boolean restore = !enable && noModificationBots.contains(bot);
			int botIndex = noModificationBots.indexOf(bot);
			Bot rBot = restore ? noModificationBots.get(botIndex) : null;
			bot.disableCanvas = rBot != null ? rBot.disableCanvas : enable;
			bot.disableRendering = rBot != null ? rBot.disableRendering : enable;
		}
	}

	private void init() {
		setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
		WebQueue.Create();
		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				if (cleanExit()) {
					dispose();
				}
			}
		});
		addWindowStateListener(new WindowStateListener() {
			public void windowStateChanged(WindowEvent arg0) {
				switch (arg0.getID()) {
					case WindowEvent.WINDOW_ICONIFIED:
						lessCpu(true);
						break;
					case WindowEvent.WINDOW_DEICONIFIED:
						lessCpu(false);
						break;
				}
			}
		});
		setIconImage(GlobalConfiguration.getImage(GlobalConfiguration.Paths.Resources.ICON));
		JPopupMenu.setDefaultLightWeightPopupEnabled(false);
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (final Exception ignored) {
		}
		WindowUtil.setFrame(this);
		home = new BotHome();
		panel = new BotPanel(home);
		toolBar = new BotToolBar(this);
		menuBar = new BotMenuBar(this);
		panel.setFocusTraversalKeys(0, new HashSet<AWTKeyStroke>());
		toolBar.setHome(true);
		menuBar.setBot(null);
		setJMenuBar(menuBar);
		textScroll = new JScrollPane(TextAreaLogHandler.TEXT_AREA, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		textScroll.setBorder(null);
		textScroll.setPreferredSize(new Dimension(PANEL_WIDTH, LOG_HEIGHT));
		textScroll.setVisible(true);
		scrollableBotPanel = new JScrollPane(panel);
		add(toolBar, BorderLayout.NORTH);
		add(scrollableBotPanel, BorderLayout.CENTER);
		add(textScroll, BorderLayout.SOUTH);
	}

	public void scriptStarted(final ScriptHandler handler, Script script) {
		java.awt.EventQueue.invokeLater(new Runnable() {
			public void run() {
				Bot bot = handler.getBot();
				if (bot == getCurrentBot()) {
					bot.inputFlags = Environment.INPUT_KEYBOARD;
					bot.overrideInput = false;
					toolBar.setScriptButton(BotToolBar.PAUSE_SCRIPT);
					toolBar.setInputState(bot.inputFlags);
					toolBar.setOverrideInput(false);
					menuBar.setOverrideInput(false);
					String acct = bot.getAccountName();
					toolBar.setTabLabel(bots.indexOf(bot) + 1, acct == null ? "RuneScape" : acct);
					toolBar.updateInputButton();
					setTitle(acct);
				}
			}
		});
	}

	public void scriptStopped(ScriptHandler handler, Script script) {
		Bot bot = handler.getBot();
		if (bot == getCurrentBot()) {
			bot.inputFlags = Environment.INPUT_KEYBOARD | Environment.INPUT_MOUSE;
			bot.overrideInput = false;
			toolBar.setScriptButton(BotToolBar.RUN_SCRIPT);
			toolBar.setInputState(bot.inputFlags);
			toolBar.setOverrideInput(false);
			menuBar.setOverrideInput(false);
			menuBar.setPauseScript(false);
			toolBar.setTabLabel(bots.indexOf(bot) + 1, "RuneScape");
			toolBar.updateInputButton();
			setTitle(null);
		}
	}

	public void scriptResumed(ScriptHandler handler, Script script) {
		if (handler.getBot() == getCurrentBot()) {
			toolBar.setScriptButton(BotToolBar.PAUSE_SCRIPT);
			menuBar.setPauseScript(false);
		}
	}

	public void scriptPaused(ScriptHandler handler, Script script) {
		if (handler.getBot() == getCurrentBot()) {
			toolBar.setScriptButton(BotToolBar.RESUME_SCRIPT);
			menuBar.setPauseScript(true);
		}
	}

	public void inputChanged(Bot bot, int mask) {
		bot.inputFlags = mask;
		toolBar.setInputState(mask);
		toolBar.updateInputButton();
	}

	public static void openURL(final String url) {
		GlobalConfiguration.OperatingSystem os = GlobalConfiguration.getCurrentOperatingSystem();
		try {
			if (os == GlobalConfiguration.OperatingSystem.MAC) {
				Class<?> fileMgr = Class.forName("com.apple.eio.FileManager");
				Method openURL = fileMgr.getDeclaredMethod("openURL", new Class[]{String.class});
				openURL.invoke(null, url);
			} else if (os == GlobalConfiguration.OperatingSystem.WINDOWS) {
				Runtime.getRuntime().exec("rundll32 url.dll,FileProtocolHandler " + url);
			} else {
				String[] browsers = {"firefox", "opera", "konqueror", "epiphany", "mozilla", "netscape", "google-chrome", "chromium-browser"};
				String browser = null;
				for (int count = 0; (count < browsers.length) && (browser == null); count++) {
					if (Runtime.getRuntime().exec(new String[]{"which", browsers[count]}).waitFor() == 0) {
						browser = browsers[count];
					}
				}
				if (browser == null) {
					throw new Exception("Could not find web browser");
				} else {
					Runtime.getRuntime().exec(new String[]{browser, url});
				}
			}
		} catch (Exception e) {
			log.warning("Unable to open " + url);
		}
	}

	private boolean confirmRemoveBot() {
		if (!disableConfirmations) {
			int result = JOptionPane.showConfirmDialog(this, "Are you sure you want to close this bot?", "Close Bot", JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);
			return (result == JOptionPane.OK_OPTION);
		} else {
			return true;
		}
	}

	public boolean cleanExit() {
		if (!disableConfirmations) {
			disableConfirmations = true;
			for (Bot bot : bots) {
				if (bot.getAccountName() != null) {
					disableConfirmations = true;
					break;
				}
			}
		}
		boolean doExit = true;
		if (!disableConfirmations) {
			final String message = "Are you sure you want to exit?";
			int result = JOptionPane.showConfirmDialog(this, message, "Exit", JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);
			if (result != JOptionPane.OK_OPTION) {
				doExit = false;
			}
		}
		WebQueue.Destroy();
		setVisible(false);
		while (WebQueue.IsRunning()) {
			try {
				Thread.sleep(50);
			} catch (Exception e) {
			}
		}
		if (doExit) {
			menuBar.savePrefs();
			System.exit(0);
		} else {
			setVisible(true);
		}
		return doExit;
	}

	public void scriptStarted(PassiveScriptHandler handler, PassiveScript script) {
	}

	public void scriptStopped(PassiveScriptHandler handler, PassiveScript script) {
	}
}