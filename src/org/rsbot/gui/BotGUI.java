package org.rsbot.gui;

import org.rsbot.bot.Bot;
import org.rsbot.bot.input.Listener;
import org.rsbot.event.EventMulticaster;
import org.rsbot.event.impl.*;
import org.rsbot.event.listeners.PaintListener;
import org.rsbot.event.listeners.TextPaintListener;
import org.rsbot.script.Script;
import org.rsbot.script.ScriptHandler;
import org.rsbot.script.ScriptManifest;
import org.rsbot.script.methods.MethodContext;
import org.rsbot.util.GlobalConfiguration;
import org.rsbot.util.GlobalConfiguration.OperatingSystem;
import org.rsbot.util.ScreenshotUtil;
import org.rsbot.util.UpdateUtil;
import org.rsbot.util.logging.TextAreaLogHandler;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.*;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.util.*;
import java.util.List;
import java.util.logging.Logger;

public class BotGUI extends JFrame implements ActionListener {

    private static final long serialVersionUID = -4411033752001988794L;
    private static final Logger log = Logger.getLogger(BotGUI.class.getName());

    private static final Map<String, Class<?>> DEBUG_MAP = new LinkedHashMap<String, Class<?>>();

    private static TrayIcon trayIcon;
    private static MenuItem hideShow;

    static {
        JPopupMenu.setDefaultLightWeightPopupEnabled(false);

        // Text
        DEBUG_MAP.put("Login Index", TLoginIndex.class);
        DEBUG_MAP.put("Current Tab", TTab.class);
        DEBUG_MAP.put("Camera", TCamera.class);
        DEBUG_MAP.put("Animation", TAnimation.class);
        DEBUG_MAP.put("Floor Height", TFloorHeight.class);
        DEBUG_MAP.put("Player Position", TPlayerPosition.class);
        DEBUG_MAP.put("Mouse Position", TMousePosition.class);
        DEBUG_MAP.put("Actual Mouse Position", TActualMousePosition.class);
        DEBUG_MAP.put("Input Allowed", TUserInputAllowed.class);
        DEBUG_MAP.put("Menu Actions", TMenuActions.class);
        DEBUG_MAP.put("FPS", TFPS.class);

        // Paint
        DEBUG_MAP.put("Players", DrawPlayers.class);
        DEBUG_MAP.put("NPCs", DrawNPCs.class);
        DEBUG_MAP.put("Objects", DrawObjects.class);
        DEBUG_MAP.put("Models", DrawModel.class);
        DEBUG_MAP.put("Mouse", DrawMouse.class);
        DEBUG_MAP.put("Inventory", DrawInventory.class);
        DEBUG_MAP.put("Ground Items", DrawItems.class);
        DEBUG_MAP.put("Calc Test", DrawBoundaries.class);
        DEBUG_MAP.put("Settings", DrawSettings.class);

        // Other
        DEBUG_MAP.put("Character Moved (LAG)", CharacterMovedLogger.class);
        DEBUG_MAP.put("Server Messages", ServerMessageLogger.class);

    }

    private Bot bot;

    private Map<String, JCheckBoxMenuItem> commandCheckMap = new HashMap<String, JCheckBoxMenuItem>();
    private Map<String, JMenuItem> commandMenuItem = new HashMap<String, JMenuItem>();
    private Map<String, EventListener> listeners = new TreeMap<String, EventListener>();

    private final EventMulticaster eventMulticaster = new EventMulticaster();
    private File menuSetting = null;
    private JScrollPane textScroll;


    private JToolBar toolBar;
    private JButton userInputButton;
    private JButton userPauseButton;

    private final JMenuItem pauseResumeScript;
    private final Dimension minsize;


    public BotGUI() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (final Exception ignored) {
        }

        initializeGUI();
        pauseResumeScript = commandMenuItem.get("Pause");
        pauseResumeScript.setEnabled(false);

        setTitle();
        setLocationRelativeTo(getOwner());

        try {
            setIconImage(Toolkit.getDefaultToolkit().getImage(GlobalConfiguration.RUNNING_FROM_JAR ? getClass().getResource(GlobalConfiguration.Paths.Resources.ICON) : new File(GlobalConfiguration.Paths.ICON).toURI().toURL()));
        } catch (final Exception e) {
            e.printStackTrace();
        }

        minsize = getSize();
        setMinimumSize(minsize);
        setResizable(true);
        setVisible(true);
        BotGUI.log.info("Welcome to " + GlobalConfiguration.NAME + "!");
        createTray();
        bot.startClient();

        if (GlobalConfiguration.RUNNING_FROM_JAR) {
            final BotGUI parent = this;
            new Thread(new Runnable() {
                public void run() {
                    final UpdateUtil updater = new UpdateUtil(parent);
                    updater.checkUpdate(false);
                }
            }).start();
        }
    }

    public Bot getBot() {
        return bot;
    }

    public void updatePauseButton(final String text, final String pathResource, final String pathFile) {
        userPauseButton.setText(text);
        try {
            userPauseButton.setIcon(new ImageIcon(GlobalConfiguration.RUNNING_FROM_JAR ? getClass().getResource(pathResource) : new File(pathFile).toURI().toURL()));
        } catch (final MalformedURLException e1) {
            e1.printStackTrace();
        }
    }

    public void refreshMenu() {
        initListeners();
    }

    public void actionPerformed(final ActionEvent e) {
        final String action = e.getActionCommand();
        final int z = action.indexOf('.');
        final String[] command = new String[2];
        if (z == -1) {
            command[0] = action;
            command[1] = "";
        } else {
            command[0] = action.substring(0, z);
            command[1] = action.substring(z + 1);
        }
        if (command[0].equals("File")) {
            final ScriptHandler sh = bot.getScriptHandler();
            if ("Run".equals(command[1])) {
                showRunScriptSelector();
                final Map<Integer, Script> running = sh.getRunningScripts();
                if (running.size() > 0) {
                    pauseResumeScript.setText("Pause");
                    pauseResumeScript.setEnabled(true);
                    updatePauseButton("Pause", GlobalConfiguration.Paths.Resources.ICON_PAUSE, GlobalConfiguration.Paths.ICON_PAUSE);
                } else {
                    pauseResumeScript.setEnabled(false);
                    updatePauseButton("Run", GlobalConfiguration.Paths.Resources.ICON_PLAY, GlobalConfiguration.Paths.ICON_PLAY);
                }
            } else if ("Stop".equals(command[1])) {
                showStopScriptSelector();
            } else if ("Pause".equals(command[1]) || "Resume Script".equals(command[1])) {
                     pauseScript();
            } else if ("Screenshot".equals(command[1])) {
                ScreenshotUtil.takeScreenshot(getBot(), isLoggedIn());
            } else if ("Screenshot (Uncensored)".equals(command[1])) {
                ScreenshotUtil.takeScreenshot(getBot(), false);
            } else if ("Exit".equals(command[1])) {
                System.exit(0);
            }
        } else if (command[0].equals("Edit")) {
            if ("Accounts".equals(command[1])) {
                AccountManager.getInstance().showGUI();
            } else if ("Block Input".equals(command[1])) {
                Listener.blocked = !Listener.blocked;
                try {
                    userInputButton.setIcon(new ImageIcon(Listener.blocked ? (GlobalConfiguration.RUNNING_FROM_JAR ? getClass().getResource(GlobalConfiguration.Paths.Resources.ICON_DELETE) : new File(GlobalConfiguration.Paths.ICON_DELETE).toURI().toURL()) : GlobalConfiguration.RUNNING_FROM_JAR ? getClass().getResource(GlobalConfiguration.Paths.Resources.ICON_TICK) : new File(GlobalConfiguration.Paths.ICON_TICK).toURI().toURL()));
                } catch (final MalformedURLException e1) {
                    e1.printStackTrace();
                }
            } else if ("Less CPU".equals(command[1])) {
                getBot().disableRendering = ((JCheckBoxMenuItem) e.getSource()).isSelected();
            } else if ("Disable Anti-Randoms".equals(command[1])) {
                getBot().disableRandoms = ((JCheckBoxMenuItem) e.getSource()).isSelected();
            } else if ("Disable Auto Login".equals(command[1])) {
                getBot().disableAutoLogin = ((JCheckBoxMenuItem) e.getSource()).isSelected();
            } else if ("Disable Break Handler".equals(command[1])) {
                getBot().disableBreakHandler = ((JCheckBoxMenuItem) e.getSource()).isSelected();
            }
        } else if (command[0].equals("View")) {
            final boolean selected = ((JCheckBoxMenuItem) e.getSource()).isSelected();
            if ("All Debugging".equals(command[1])) {
                for (final String key : DEBUG_MAP.keySet()) {
                    final Class<?> el = DEBUG_MAP.get(key);
                    final boolean wasSelected = commandCheckMap.get(key).isSelected();
                    commandCheckMap.get(key).setSelected(selected);
                    if (selected) {
                        if (!wasSelected) {
                            addListener(el);
                        }
                    } else {
                        if (wasSelected) {
                            removeListener(el);
                        }
                    }
                }
                commandCheckMap.get("All Text Debugging").setSelected(selected);
                commandCheckMap.get("All Paint Debugging").setSelected(selected);
            } else if ("Hide Toolbar".equals(command[1])) {
                toggleViewState(toolBar, selected);
            } else if ("Hide Log Window".equals(command[1])) {
                toggleViewState(textScroll, selected);
            } else if ("All Text Debugging".equals(command[1])) {
                if (!selected) {
                    commandCheckMap.get("All Debugging").setSelected(false);
                }
                for (final String key : DEBUG_MAP.keySet()) {
                    final Class<?> el = DEBUG_MAP.get(key);
                    if (TextPaintListener.class.isAssignableFrom(el)) {
                        final boolean wasSelected = commandCheckMap.get(key).isSelected();
                        commandCheckMap.get(key).setSelected(selected);
                        if (selected) {
                            if (!wasSelected) {
                                addListener(el);
                            }
                        } else {
                            if (wasSelected) {
                                removeListener(el);
                            }
                        }
                    }
                }
            } else if ("All Paint Debugging".equals(command[1])) {
                if (!selected) {
                    commandCheckMap.get("All Debugging").setSelected(false);
                }
                for (final String key : DEBUG_MAP.keySet()) {
                    final Class<?> el = DEBUG_MAP.get(key);
                    if (PaintListener.class.isAssignableFrom(el)) {
                        final boolean wasSelected = commandCheckMap.get(key).isSelected();
                        commandCheckMap.get(key).setSelected(selected);
                        if (selected) {
                            if (!wasSelected) {
                                addListener(el);
                            }
                        } else {
                            if (wasSelected) {
                                removeListener(el);
                            }
                        }
                    }
                }
            } else {
                final Class<?> el = DEBUG_MAP.get(command[1]);
                commandCheckMap.get(command[1]).setSelected(selected);
                if (selected) {
                    addListener(el);
                } else {
                    commandCheckMap.get("All Text Debugging").setSelected(false);
                    commandCheckMap.get("All Paint Debugging").setSelected(false);
                    commandCheckMap.get("All Debugging").setSelected(false);
                    removeListener(el);
                }
            }
        } else if (command[0].equals("Help")) {
            if ("Site".equals(command[1])) {
                openURL(GlobalConfiguration.Paths.URLs.SITE);
            } else if ("Project".equals(command[1])) {
                openURL(GlobalConfiguration.Paths.URLs.PROJECT);
            } else if ("About".equals(command[1])) {
                JOptionPane.showMessageDialog(this, new String[]{"An open source bot.", "Visit " + GlobalConfiguration.Paths.URLs.SITE + "/ for more information."}, "About", JOptionPane.INFORMATION_MESSAGE);
            }
        }
    }

    public void openURL(final String url) {
        final OperatingSystem os = GlobalConfiguration.getCurrentOperatingSystem();
        try {
            if (os == OperatingSystem.MAC) {
                final Class<?> fileMgr = Class.forName("com.apple.eio.FileManager");
                final Method openURL = fileMgr.getDeclaredMethod("openURL", new Class[]{String.class});
                openURL.invoke(null, url);
            } else if (os == OperatingSystem.WINDOWS) {
                Runtime.getRuntime().exec("rundll32 url.dll,FileProtocolHandler " + url);
            } else { // assume Unix or Linux
                final String[] browsers = {"firefox", "opera", "konqueror", "epiphany", "mozilla", "netscape"};
                String browser = null;
                for (int count = 0; (count < browsers.length) && (browser == null); count++) {
                    if (Runtime.getRuntime().exec(new String[]{"which", browsers[count]}).waitFor() == 0) {
                        browser = browsers[count];
                    }
                }
                if (browser == null)
                    throw new Exception("Could not find web browser");
                else {
                    Runtime.getRuntime().exec(new String[]{browser, url});
                }
            }
        } catch (final Exception e) {
            JOptionPane.showMessageDialog(this, "Error Opening Browser", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    void runScript(final String name, final Script script, final Map<String, String> args) {
        bot.setAccount(name);
        setTitle();
        bot.getScriptHandler().runScript(script, args);
        if (!Listener.blocked) {
            commandCheckMap.get("Block Input").doClick();
        }
    }

    void pauseScript() {
        final ScriptHandler sh = bot.getScriptHandler();
        final Map<Integer, Script> running = sh.getRunningScripts();
        if (running.size() > 0) {
            final int id = running.keySet().iterator().next();
            final Script s = running.get(id);
            final ScriptManifest prop = s.getClass().getAnnotation(ScriptManifest.class);
            final String name = prop.name();
            if (running.get(id).isPaused) {
                sh.pauseScript(id);
                BotGUI.log.info(name + " has resumed!");
            } else {
                sh.pauseScript(id);
                BotGUI.log.info(name + " has been paused!");
            }
            if (running.get(id).isPaused) {
                pauseResumeScript.setText("Resume Script");
                updatePauseButton("Resume Script", GlobalConfiguration.Paths.Resources.ICON_PLAY, GlobalConfiguration.Paths.ICON_PLAY);
            } else {
                pauseResumeScript.setText("Pause");
                updatePauseButton("Pause", GlobalConfiguration.Paths.Resources.ICON_PAUSE, GlobalConfiguration.Paths.ICON_PAUSE);
            }
        }
    }

    private void addListener(Class<?> clazz) {
        EventListener el = instantiateListener(clazz);
        listeners.put(el.getClass().getName(), el);
        eventMulticaster.addListener(el);
    }

    private void removeListener(Class<?> clazz) {
        EventListener el = listeners.get(clazz.getName());
        listeners.remove(clazz.getName());
        eventMulticaster.removeListener(el);
    }

    private String[] constructDebugs() {
        final List<String> debugItems = new ArrayList<String>();
        debugItems.add("All Debugging");
        debugItems.add("Hide Toolbar");
        debugItems.add("Hide Log Window");
        debugItems.add("-");
        debugItems.add("All Paint Debugging");
        for (final String key : DEBUG_MAP.keySet()) {
            final Class<?> el = DEBUG_MAP.get(key);
            if (PaintListener.class.isAssignableFrom(el)) {
                debugItems.add(key);
            }
        }
        debugItems.add("-");
        debugItems.add("All Text Debugging");
        for (final String key : DEBUG_MAP.keySet()) {
            final Class<?> el = DEBUG_MAP.get(key);
            if (TextPaintListener.class.isAssignableFrom(el)) {
                debugItems.add(key);
            }
        }
        debugItems.add("-");
        for (final String key : DEBUG_MAP.keySet()) {
            final Class<?> el = DEBUG_MAP.get(key);
            if (!(TextPaintListener.class.isAssignableFrom(el)) && !(PaintListener.class.isAssignableFrom(el))) {
                debugItems.add(key);
            }
        }
        for (final ListIterator<String> it = debugItems.listIterator(); it.hasNext();) {
            final String s = it.next();
            if (s.equals("-")) {
                continue;
            }
            it.set("ToggleF " + s);
        }
        return debugItems.toArray(new String[debugItems.size()]);
    }

    private JMenuBar constructMenuBar() {
        final String[] debugs = constructDebugs();
        final String[] titles = new String[]{"File", "Edit", "View", "Help"};
        final String[][] elements = new String[][]{{"Run", "Stop", "Pause", "-", "Screenshot", "Screenshot (Uncensored)", "-", "Exit"}, {"Accounts", "-", "ToggleF Block Input", "ToggleF Less CPU", "-", "ToggleF Disable Anti-Randoms", "ToggleF Disable Auto Login", "ToggleF Disable Break Handler"}, debugs, {"Site", "Project", "About"}};
        final JMenuBar bar = new JMenuBar();
        for (int i = 0; i < titles.length; i++) {
            final String title = titles[i];
            final String[] elems = elements[i];
            bar.add(constructMenu(title, elems));
        }
        return bar;
    }

    private JMenu constructMenu(String title, String[] elems) {
        final JMenu menu = new JMenu(title);
        for (String e : elems) {
            if (e.equals("-")) {
                menu.add(new JSeparator());
            } else {
                JMenuItem jmi;
                if (e.startsWith("Toggle")) {
                    e = e.substring("Toggle".length());
                    final char state = e.charAt(0);
                    e = e.substring(2);
                    jmi = new JCheckBoxMenuItem(e);
                    if ((state == 't') || (state == 'T')) {
                        jmi.setSelected(true);
                    }
                    commandCheckMap.put(e, (JCheckBoxMenuItem) jmi);
                } else {
                    jmi = new JMenuItem(e);
                    commandMenuItem.put(e, jmi);
                }
                jmi.addActionListener(this);
                jmi.setActionCommand(title + "." + e);
                menu.add(jmi);
            }
        }
        return menu;
    }

    private EventListener instantiateListener(Class<?> clazz) {
        try {
            EventListener listener;
            try {
                Constructor<?> constructor = clazz.getConstructor(Bot.class);
                listener = (EventListener) constructor.newInstance(getBot());
            } catch (Exception e) {
                listener = clazz.asSubclass(EventListener.class).newInstance();
            }
            return listener;
        } catch (Exception ex) {
        }
        return null;
    }

    private void initializeGUI() {
        bot = new Bot();

        bot.getEventManager().getMulticaster().addListener(eventMulticaster);
        Listener.blocked = false;

        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);

        addWindowListener(new WindowAdapter() {
            public void windowClosing(final WindowEvent e) {
                if (safeClose()) {
                    dispose();
                    shutdown();
                }
            }
        });

        Runtime.getRuntime().addShutdownHook(new Thread() {
            public void run() {
                try {
                    final BufferedWriter bw = new BufferedWriter(new FileWriter(menuSetting));
                    boolean f = true;
                    for (final JCheckBoxMenuItem item : commandCheckMap.values()) {
                        if (item == null) {
                            continue;
                        }

                        if (item.isSelected() && !item.getText().startsWith("All")) {
                            if (!f) {
                                bw.newLine();
                            }
                            f = false;

                            bw.write(item.getText());
                        }
                    }
                    bw.close();
                } catch (final Exception e) {
                    e.printStackTrace();
                }
            }
        });
        setJMenuBar(constructMenuBar());

        try {
            userInputButton = new JButton("Input", new ImageIcon(GlobalConfiguration.RUNNING_FROM_JAR ? getClass().getResource(GlobalConfiguration.Paths.Resources.ICON_TICK) : new File(GlobalConfiguration.Paths.ICON_TICK).toURI().toURL()));
        } catch (final MalformedURLException e1) {
            e1.printStackTrace();
        }
        userInputButton.addActionListener(new ActionListener() {
            public void actionPerformed(final ActionEvent arg0) {
                commandCheckMap.get("Block Input").doClick();
            }
        });
        userInputButton.setFocusable(false);

        try {
            userPauseButton = new JButton("Run", new ImageIcon(GlobalConfiguration.RUNNING_FROM_JAR ? getClass().getResource(GlobalConfiguration.Paths.Resources.ICON_PLAY) : new File(GlobalConfiguration.Paths.ICON_PLAY).toURI().toURL()));
        } catch (final MalformedURLException e1) {
            e1.printStackTrace();
        }
        userPauseButton.addActionListener(new ActionListener() {
            public void actionPerformed(final ActionEvent arg0) {
                final ScriptHandler sh = bot.getScriptHandler();
                final Map<Integer, Script> running = sh.getRunningScripts();
                if (running.size() >= 1) {
                    pauseResumeScript.doClick();
                }
                if (running.size() == 0) {
                    commandMenuItem.get("Run").doClick();
                }

            }
        });
        userPauseButton.setFocusable(false);

        toolBar = new JToolBar();
        toolBar.setFloatable(false);
        toolBar.add(Box.createHorizontalGlue());
        toolBar.add(userPauseButton);
        toolBar.add(userInputButton);

        // applet
        final Dimension dim = new Dimension(765, 503);
        bot.getLoader().setPreferredSize(dim);

        // log
        textScroll = new JScrollPane(TextAreaLogHandler.textArea, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        textScroll.setBorder(null);
        textScroll.setPreferredSize(new Dimension(dim.width, 120));
        textScroll.setVisible(true);

        add(toolBar, BorderLayout.NORTH);
        add(bot.getLoader(), BorderLayout.CENTER);
        add(textScroll, BorderLayout.SOUTH);

        pack();
    }

    private void initListeners() {
        if (menuSetting == null) {
            menuSetting = new File(GlobalConfiguration.Paths.getMenuCache());
        }
        if (!menuSetting.exists()) {
            try {
                if (menuSetting.createNewFile()) {
                    BotGUI.log.warning("Failed to create settings file.");
                }
            } catch (final IOException e) {
                BotGUI.log.warning("Failed to create settings file.");
            }
        } else {
            try {
                final BufferedReader br = new BufferedReader(new FileReader(menuSetting));
                String s;
                while ((s = br.readLine()) != null) {
                    final JCheckBoxMenuItem item = commandCheckMap.get(s);
                    if (item != null) {
                        item.doClick();
                    }
                }
            } catch (final IOException e) {
                BotGUI.log.warning("Unable to read settings.");
            }
        }

    }

    private boolean isLoggedIn() {
        final MethodContext methods = bot.getMethodContext();
        if (methods != null && methods.game.isLoggedIn()) {
            return true;
        }
        return false;
    }

    private boolean safeClose() {
        boolean pass = true;
        if (isLoggedIn()) {
            final int result = JOptionPane.showConfirmDialog(this, "Are you sure you would like to quit?", "Close", JOptionPane.WARNING_MESSAGE, JOptionPane.YES_NO_OPTION);
            pass = result == JOptionPane.YES_OPTION;
        }
        return pass;
    }

    private void setTitle() {
        final String name = bot.getAccountName();
        setTitle((name.isEmpty() ? "" : name + " - ") + GlobalConfiguration.NAME + " v" + ((float) GlobalConfiguration.getVersion() / 100));
        if(trayIcon != null)
        trayIcon.setToolTip(this.getTitle());
    }

    private void showRunScriptSelector() {
        if (AccountManager.getAccountNames().length == 0) {
            JOptionPane.showMessageDialog(this, "No accounts found! Please create one before using the bot.");
            AccountManager.getInstance().showGUI();
        } else {
            final ScriptHandler sh = bot.getScriptHandler();
            final Map<Integer, Script> running = sh.getRunningScripts();
            if (running.size() > 0) {
            	log.warning("A script is already running.");
            } else {
                ScriptSelector.getInstance(this).showSelector();
            }
        }
    }

    private void showStopScriptSelector() {
        final ScriptHandler sh = bot.getScriptHandler();
        final Map<Integer, Script> running = sh.getRunningScripts();
        if (running.size() > 0) {
            final int id = running.keySet().iterator().next();
            final Script s = running.get(id);
            final ScriptManifest prop = s.getClass().getAnnotation(ScriptManifest.class);
            final int result = JOptionPane.showConfirmDialog(this, "Would you like to stop the script " + prop.name() + "?", "Script", JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);

            if (result == JOptionPane.OK_OPTION) {
                sh.stopScript(id);

                pauseResumeScript.setText("Pause");
                pauseResumeScript.setEnabled(false);
                updatePauseButton("Run", GlobalConfiguration.Paths.Resources.ICON_PLAY, GlobalConfiguration.Paths.ICON_PLAY);
            }
        }
    }

    private void shutdown() {
        BotGUI.log.info("Closing");
        System.exit(0);
    }

    private void toggleViewState(final Component component, final boolean visible) {
        final Dimension size = minsize;
        size.height += component.getSize().height * (visible ? -1 : 1);
        component.setVisible(!visible);
        setMinimumSize(size);
        if ((getExtendedState() & Frame.MAXIMIZED_BOTH) != Frame.MAXIMIZED_BOTH) {
            pack();
        }
    }

    private void createTray() {
        try {
            Image icon = Toolkit.getDefaultToolkit().getImage(GlobalConfiguration.RUNNING_FROM_JAR ? getClass().getResource(GlobalConfiguration.Paths.Resources.ICON) : new File(GlobalConfiguration.Paths.ICON).toURI().toURL());
            this.setIconImage(icon);
            trayIcon = new TrayIcon(icon, this.getTitle(), rightClickMenu());
            trayIcon.setImageAutoSize(true);
            trayIcon.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    doubleClickOnTrayPerformed();
                }
            });
            SystemTray.getSystemTray().add(trayIcon);
        } catch (MalformedURLException e) {
            log.warning("Could not download needed icon for tray.");
        } catch (Exception e) {
            log.warning("Could not create system tray for bot.");
        }
    }

    private PopupMenu rightClickMenu() {
        PopupMenu menu = new PopupMenu();
        menu.setFont(new Font("Tahoma", 0, 11));

        MenuItem exit = new MenuItem("Exit");
        exit.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (safeClose()) {
                    dispose();
                    shutdown();
                }
            }
        });

        hideShow = new MenuItem("Hide");
        hideShow.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                doubleClickOnTrayPerformed();
            }
        });
        menu.add(hideShow);
        menu.add(exit);

        return menu;
    }

    private void doubleClickOnTrayPerformed() {
        this.setVisible(!isVisible());
        hideShow.setLabel(isVisible() ? "Hide" : "Show");
    }

}