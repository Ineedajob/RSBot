package org.rsbot.script;

import org.rsbot.script.methods.Environment;
import org.rsbot.script.methods.MethodContext;
import org.rsbot.script.methods.Methods;
import org.rsbot.script.randoms.LoginBot;

import java.util.EventListener;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;

public abstract class Script extends Methods implements EventListener, Runnable {

    private volatile boolean active = false;
    private volatile boolean paused = false;

	private int id = -1;
    private MethodContext ctx;
	private Set<Script> delegates = new HashSet<Script>();

    /**
     * Finalized to cause errors intentionally to avoid confusion
	 * (yea I know how to deal with these script writers ;)).
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
	 * been initialized with all method providers. Override to
	 * perform any initialization or prevent script start.
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
     * calling it again. Returning a negative number will deactivate the script.
     */
    public abstract int loop();

    /**
     * Override to perform any clean up on script stopScript.
     */
    public void onFinish() {

    }

	/**
	 * Initializes this script with another script's
	 * context.
	 *
	 * @param script The context providing Script.
	 * @see #delegateTo(Script)
	 */
    public final void init(Script script) {
    	init(script.ctx);
    }

	/**
	 * Initializes this script with a given context.
	 *
	 * @param ctx The MethodContext.
	 */
    public final void init(MethodContext ctx) {
    	super.init(ctx);
    	this.ctx = ctx;
    }

	/**
	 * Initializes the provided script with this script's
	 * method context and adds the delegate as a listener
	 * to the event manager, allowing it to receive client
	 * events. The script will be stored as a delegate of
	 * this script and removed from the event manager when
	 * this script is stopped. The onStart(), loop() and
	 * onFinish() methods are not automatically called on
	 * the delegate.
	 *
	 * @param script The script to delegate to.
	 */
	public final void delegateTo(Script script) {
		script.init(ctx);
		ctx.bot.getEventManager().addListener(script);
		delegates.add(script);
	}

	/**
	 * For internal use only. Deactivates this script if
	 * the appropriate id is provided.
	 *
	 * @param id The id from ScriptHandler.
	 */
	public final void deactivate(int id) {
		if (id != this.id) {
			throw new IllegalStateException("Invalid id!");
		}
		this.active = false;
	}

	/**
	 * For internal use only. Sets the pool id of this script.
	 *
	 * @param id The id from ScriptHandler.
	 */
	public final void setID(int id) {
		if (this.id != -1) {
			throw new IllegalStateException("Already added to pool!");
		}
		this.id = id;
	}

	/**
	 * Pauses/resumes this script.
	 *
	 * @param paused <tt>true</tt> to pause; <tt>false</tt> to resume.
	 */
	public final void setPaused(boolean paused) {
		this.paused = paused;
	}

	/**
	 * Returns whether or not this script is running.
	 *
	 * @return <tt>true</tt> if active; otherwise <tt>false</tt>.
	 */
	public final boolean isActive() {
		return active;
	}

	/**
	 * Returns whether or not this script is paused.
	 *
	 * @return <tt>true</tt> if paused; otherwise <tt>false</tt>.
	 */
	public final boolean isPaused() {
		return paused;
	}

    /**
     * Stops the current script without logging out.
     */
    public void stopScript() {
        stopScript(false);
    }

    /**
     * Stops the current script; player can be logged out before
     * the script is stopped.
     *
     * @param logout <tt>true</tt> if the player should be logged
     * out before the script is stopped.
     */
    public void stopScript(boolean logout) {
        log.info("Script stopped.");
        if (bank.isOpen()) {
            bank.close();
        }
        if (game.isLoggedIn() && logout) {
            game.logout(false);
        }
        ctx.bot.getScriptHandler().stopScript();
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
		for (Script s : delegates) {
			ctx.bot.getEventManager().removeListener(s);
		}
		delegates.clear();
        ctx.bot.getEventManager().removeListener(this);
        ctx.bot.getScriptHandler().removeScript(id);
		ctx.bot.inputMask = Environment.INPUT_KEYBOARD;
		id = -1;
    }

    private boolean checkForRandoms() {
        if (ctx.bot.disableRandoms) {
            return false;
		}
        for (Random random : ctx.bot.getScriptHandler().getRandoms()) {
            if (!random.isEnabled() || (random instanceof LoginBot && ctx.bot.disableAutoLogin)) {
                continue;
            }
            if (random.run(this)) {
                return true;
            }
        }
        return false;
    }

}
