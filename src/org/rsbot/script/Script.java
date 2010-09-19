package org.rsbot.script;

import org.rsbot.script.methods.Environment;
import org.rsbot.script.methods.MethodContext;
import org.rsbot.script.methods.Methods;
import org.rsbot.script.randoms.LoginBot;

import java.util.EventListener;
import java.util.Map;
import java.util.logging.Level;

public abstract class Script extends Methods implements EventListener, Runnable {

    private volatile boolean active = false;
    private volatile boolean paused = false;

	private int id = -1;
    private MethodContext ctx;

    /**
     * Deprecated. Use {@link #onStart()} instead. Finalized to
	 * cause errors intentionally to avoid confusion (yea I
	 * know how to deal with these script writers ;)).
     *
	 * @deprecated Use {@link #onStart()} instead.
     * @param map The arguments passed in from the description.
     * @return <tt>true</tt> if the script can start.
     */
	@Deprecated
    public final boolean onStart(Map<String, String> map) {
    	return true;
    }

	/**
	 * Called before loop() is first called, after this script has
	 * been initialized with all method providers.
	 *
	 * @return <tt>true</tt> if the script can start.
	 */
	public boolean onStart() {
		return true;
	}

    /**
     * The main loop. Called if you return true from main. Called until you
     * return a negative number. Avoid causing execution to pause using sleep()
     * within this method in favor of returning the number of milliseconds to
     * sleep. This ensures that pausing and anti-randoms perform normally.
     *
     * @return The number of milliseconds that the manager should sleep before
     * calling it again. Returning a negative number will stop the script.
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

	public final void stop(int id) {
		if (id != this.id) {
			throw new IllegalStateException("Invalid id!");
		}
		this.active = false;
	}

	public final void setID(int id) {
		if (this.id != -1) {
			throw new IllegalStateException("Already added to pool!");
		}
		this.id = id;
	}

	public final void setPaused(boolean paused) {
		this.paused = paused;
	}

	public final boolean isActive() {
		return active;
	}

	public final boolean isPaused() {
		return paused;
	}

    public final void run() {
        ctx.bot.getEventManager().addListener(this);
        menu.setupListener();
        log.info("Script started.");
        boolean start = false;
        try {
            start = onStart();
        } catch (ThreadDeath ignored) {
        } catch (Throwable ex) {
            log.log(Level.SEVERE, "Error starting script: ", ex);
        }
        if (start) {
            active = true;
            try {
                while (active) {
                    if (!paused) {
                        if (checkForRandoms()) {
                            continue;
                        }
                        int timeOut = -1;
                        if (!ctx.bot.disableBreakHandler && ctx.bot.getBreakHandler().isBreaking()) {
                    		timeOut = ctx.bot.getBreakHandler().loop();
                    	} else {
                    		try {
                            	timeOut = loop();
                            } catch (ThreadDeath td) {
                                break;
                            } catch (Exception ex) {
                                log.log(Level.WARNING, "Uncaught exception from script: ", ex);
                            }
                    	}
                        if (timeOut == -1) {
                            break;
                        }
                        try {
                            sleep(timeOut);
                        } catch (ThreadDeath td) {
                            break;
                        } catch (RuntimeException e) {
                            e.printStackTrace();
                            break;
                        }
                    } else {
                        try {
                            sleep(1000);
                        } catch (ThreadDeath td) {
                            break;
                        } catch (RuntimeException e) {
                            e.printStackTrace();
                            break;
                        }
                    }
                }
                try {
                    onFinish();
                } catch (ThreadDeath ignored) {
                } catch (RuntimeException e) {
                    e.printStackTrace();
                }
            } catch (ThreadDeath td) {
                onFinish();
            }
            active = false;
            log.info("Script stopped.");
        } else {
            log.severe("Failed to start up.");
        }
        mouse.moveOffScreen();
        ctx.bot.getEventManager().removeListener(this);
        ctx.bot.getScriptHandler().removeScript(id);
		ctx.bot.inputMask = Environment.INPUT_KEYBOARD;
		id = -1;
    }

    private boolean checkForRandoms() {
        if(ctx.bot.disableRandoms)
            return false;
            
        for (Random random : ctx.bot.getScriptHandler().getRandoms()) {
            if (random instanceof LoginBot) {
                if (ctx.bot.disableAutoLogin) {
                    continue;
                }
            } else if (!random.isEnabled()) {
                continue;
            }
            if (random.run(this)) {
                return true;
            }
        }
        return false;
    }

}
