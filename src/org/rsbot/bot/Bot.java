package org.rsbot.bot;

import org.rsbot.Application;
import org.rsbot.accessors.Client;
import org.rsbot.bot.input.CanvasWrapper;
import org.rsbot.event.EventManager;
import org.rsbot.gui.AccountManager;
import org.rsbot.script.BreakHandler;
import org.rsbot.script.InputManager;
import org.rsbot.script.ScriptHandler;
import org.rsbot.script.methods.MethodContext;

public class Bot {
	
    private String account;
    private BotStub botStub;
    private Client client;
    private MethodContext methods;
    private EventManager eventManager;
    private InputManager im;
    private RSLoader loader;
    private ScriptHandler sh;
    private BreakHandler bh;

    public volatile boolean disableRandoms = false;
    public volatile boolean disableAutoLogin = false;
    public volatile boolean disableBreakHandler = false;
    public volatile boolean disableRendering = false;

    public String getAccountName() {
        return account;
    }

    public Client getClient() {
        return client;
    }
    
    public CanvasWrapper getCanvas() {
    	return (CanvasWrapper) client.getCanvas();
    }
    
    public MethodContext getMethodContext() {
    	return methods;
    }

    public EventManager getEventManager() {
        return eventManager;
    }

    public InputManager getInputManager() {
        return im;
    }
    
    public BreakHandler getBreakHandler() {
    	return bh;
    }

    public ScriptHandler getScriptHandler() {
        return sh;
    }

    public boolean setAccount(final String name) {
        boolean exist = false;
        for (final String s : AccountManager.getAccountNames()) {
            if (s.toLowerCase().equals(name.toLowerCase())) {
                exist = true;
            }
        }
        if (!exist)
            return false;
        account = name;
        return true;
    }

    // Constructor
    public Bot() {
        account = "";
        init();
    }

    public BotStub getBotStub() {
        return botStub;
    }

    public RSLoader getLoader() {
        return loader;
    }

    public void init() {
        im = new InputManager(this);
        loader = new RSLoader();
        botStub = new BotStub(loader);
        loader.setStub(botStub);
        loader.setCallback(new Runnable() {
            public void run() {
                setClient((Client) loader.getClient());
                Application.getGUI().refreshMenu();
            }
        });
        sh = new ScriptHandler(this);
        bh = new BreakHandler();
        eventManager = new EventManager();
        eventManager.start();
    }

    public void setClient(final Client cl) {
        client = cl;
        client.setCallback(new CallbackImpl(this));
        methods = new MethodContext(this);
        sh.init();
    }

    public void startClient() {
        botStub.setActive(true);
        final ThreadGroup tg = new ThreadGroup("RSClient");
        final Thread thread = new Thread(tg, loader, "Loader");
        thread.start();
    }
}
