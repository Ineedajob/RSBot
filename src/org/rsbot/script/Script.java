package org.rsbot.script;

import org.rsbot.script.methods.MethodContext;
import org.rsbot.script.methods.Methods;
import org.rsbot.script.randoms.LoginBot;

import java.util.EventListener;
import java.util.Map;
import java.util.logging.Level;

public abstract class Script extends Methods implements EventListener {

    public int ID = -1;
    public volatile boolean isActive = false;
    public volatile boolean isPaused = false;
    
    private MethodContext ctx;

    /**
     * The start method. Called before loop() is first called, but after canStart()
     * is returned true. If <tt>false</tt>
     * is returned, the script will not start and loop() will never be called.
     *
     * @param map The arguments passed in from the description.
     * @return <tt>true</tt> if the script can start.
     */
    public boolean onStart(final Map<String, String> map) {
    	return true;
    }

    /**
     * The main loop. Called if you return true from main. Called until you
     * return a negative number. Avoid causing execution to pause using sleep()
     * within this method in favor of returning the number of milliseconds to
     * sleep. This ensures that pausing and anti-randoms perform normally.
     *
     * @return The number of milliseconds that the manager should sleep before
     *         calling it again. Returning a negative number will stop the
     *         script.
     */
    public abstract int loop();

    /**
     * Perform any clean up such as unregistering any event listeners.
     */
    @Override
    public void onFinish() {
    }
    
    public final void init(Script script) {
    	init(script.ctx);
    }
    
    public final void init(MethodContext ctx) {
    	super.init(ctx);
    	this.ctx = ctx;
    }

    public final void run(final Map<String, String> map) {
        ctx.bot.getEventManager().registerListener(this);
        menu.setupListener();
        log.info("Script started.");
        boolean start = false;
        try {
            start = onStart(map);
        } catch (final ThreadDeath ignored) {
        } catch (final Throwable ex) {
            log.log(Level.SEVERE, "Error starting script: ", ex);
        }
        if (start) {
            isActive = true;
            try {
                while (isActive) {
                    if (!isPaused) {
                        if (checkForRandoms()) {
                            continue;
                        }
                        int timeOut = -1;
                        if (!ctx.bot.disableBreakHandler && ctx.bot.getBreakHandler().isBreaking()) {
                    		timeOut = ctx.bot.getBreakHandler().loop();
                    	} else {
                    		try {
                            	timeOut = loop();
                            } catch (final ThreadDeath td) {
                                break;
                            } catch (final Exception ex) {
                                log.log(Level.WARNING, "Uncaught exception from script: ", ex);
                            }
                    	}
                        if (timeOut == -1) {
                            break;
                        }
                        try {
                            sleep(timeOut);
                        } catch (final ThreadDeath td) {
                            break;
                        } catch (final RuntimeException e) {
                            e.printStackTrace();
                            break;
                        }
                    } else {
                        try {
                            sleep(1000);
                        } catch (final ThreadDeath td) {
                            break;
                        } catch (final RuntimeException e) {
                            e.printStackTrace();
                            break;
                        }
                    }
                }
                try {
                    onFinish();
                } catch (final ThreadDeath ignored) {
                } catch (final RuntimeException e) {
                    e.printStackTrace();
                }
            } catch (final ThreadDeath td) {
                onFinish();
            }
            isActive = false;
            log.info("Script stopped.");
        } else {
            log.severe("Failed to start up.");
        }
        mouse.moveOffScreen();
        ctx.bot.getEventManager().removeListener(this);
        ctx.bot.getScriptHandler().removeScript(ID);
    }

    private boolean checkForRandoms() {

        if(ctx.bot.disableRandoms)
            return false;
            
        for (final Random random : ctx.bot.getScriptHandler().getRandoms()) {
            if (random instanceof LoginBot) {
                if (ctx.bot.disableAutoLogin) {
                    continue;
                }
            } else if (!random.isEnabled()) {
                continue;
            }
            if (random.runRandom()) {
                return true;
            }
        }
        return false;
    }

}
