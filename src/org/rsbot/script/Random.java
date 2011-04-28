package org.rsbot.script;

import org.rsbot.event.listeners.PaintListener;
import org.rsbot.script.methods.MethodContext;
import org.rsbot.script.methods.Methods;
import org.rsbot.script.wrappers.RSComponent;
import org.rsbot.script.wrappers.RSInterface;
import org.rsbot.script.wrappers.RSNPC;
import org.rsbot.script.wrappers.RSObject;
import org.rsbot.service.StatisticHandler;

import java.awt.*;
import java.util.logging.Level;

public abstract class Random extends Methods implements PaintListener {

	protected String name;

	private volatile boolean enabled = true;

	public int i = 50;

	public boolean up = false;

	private Script script;

	private final long timeout = random(240, 300);

//	private Color[] fadeArray = {Color.red, Color.white, Color.green, new Color(128, 0, 128), Color.yellow,
//	                             Color.black, Color.orange, Color.pink};
//
//	private int currentIndex = 0;

	/**
	 * Detects whether or not this anti-random should
	 * activate.
	 *
	 * @return <tt>true</tt> if the current script
	 *         should be paused and control passed to this
	 *         anti-random's loop.
	 */
	public abstract boolean activateCondition();

	public abstract int loop();


	/**
	 * Called after the method providers for this Random
	 * become available for use in initialization.
	 */
	public void onStart() {

	}

	public void onFinish() {

	}

	/**
	 * Override to provide a time limit in seconds for
	 * this anti-random to complete.
	 *
	 * @return The number of seconds after activateCondition
	 *         returns <tt>true</tt> before the anti-random should be
	 *         detected as having failed. If this time is reached
	 *         the random and running script will be stopped.
	 */
	public long getTimeout() {
		return timeout;
	}

	@Override
	public final void init(MethodContext ctx) {
		super.init(ctx);
		onStart();
	}

	public final boolean isActive() {
		return script != null;
	}

	public final boolean isEnabled() {
		return enabled;
	}

	public final void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	/**
	 * Stops the current script; player can be logged out before
	 * the script is stopped.
	 *
	 * @param logout <tt>true</tt> if the player should be logged
	 *               out before the script is stopped.
	 */
	protected void stopScript(boolean logout) {
		script.stopScript(logout);
	}

	public final void run(Script ctx) {
		script = ctx;
		name = getClass().getAnnotation(ScriptManifest.class).name();
		ctx.ctx.bot.getEventManager().removeListener(ctx);
		for (Script s : ctx.delegates) {
			ctx.ctx.bot.getEventManager().removeListener(s);
		}
		ctx.ctx.bot.getEventManager().addListener(this);
		log("Random event started: " + name);
		try {
			StatisticHandler.ReportRandom(name, "Random has initiated.");
		} catch (Exception ignored) {
		}
		long timeout = getTimeout();
		if (timeout > 0) {
			timeout *= 1000;
			timeout += System.currentTimeMillis();
		}
		while (ctx.isRunning()) {
			try {
				int wait = loop();
				if (wait == -1) {
					break;
				} else if (timeout > 0 && System.currentTimeMillis() >= timeout) {
					log.warning("Time limit reached for " + name + ".");
					try {
						String debug = genDebug();
						StatisticHandler.ReportRandom(name, "Random has failed, timeout was reached.\n" + debug);
					} catch (Exception ignored) {
					}
					ctx.stopScript();
				} else {
					sleep(wait);
				}
			} catch (Exception ex) {
				log.log(Level.SEVERE, "Uncaught exception: ", ex);
				break;
			}
		}
		script = null;
		onFinish();
		log("Random event finished: " + name);
		try {
			StatisticHandler.ReportRandom(name, "Random has been completed successfully.");
		} catch (Exception ignored) {
		}
		ctx.ctx.bot.getEventManager().removeListener(this);
		sleep(1000);
		ctx.ctx.bot.getEventManager().addListener(ctx);
		for (Script s : ctx.delegates) {
			ctx.ctx.bot.getEventManager().addListener(s);
		}
	}

	private String genDebug() {
		String r = "- Interfaces -\n";
		RSInterface[] interfacez = interfaces.getAll();
		for (RSInterface getD : interfacez) {
			r += "      " + getD.getIndex();
			for (RSComponent c : getD.getComponents()) {
				r += "           Component Name: " + c.getComponentName();
				r += "          Text: " + c.getText() + "\n";
				r += "          Tooltip: " + c.getTooltip() + "\n";
				r += "          Back Color: " + c.getBackgroundColor() + "\n";
				r += "          Thickness: " + c.getBorderThickness() + "\n";
				r += "          Component ID: " + c.getComponentID() + "\n";
				r += "          Component Index: " + c.getComponentIndex() + "\n";
				r += "          Model ID: " + c.getModelID() + "\n";
				r += "          Shadow Color: " + c.getShadowColor() + "\n";
				r += "          Special Type: " + c.getSpecialType() + "\n";
				r += "          Type: " + c.getType() + "\n";
			}
			r += "\n\n";
		}
		r += "- NPCs -\n";
		for (RSNPC n : npcs.getAll()) {
			r += n.getName() + "\n";
			r += " Mess: " + n.getMessage() + "\n";
			r += " Ani: " + n.getAnimation() + "\n";
			r += " Height: " + n.getHeight() + "\n";
			r += " ID: " + n.getID() + "\n";
			r += " Level: " + n.getLevel() + "\n";
			r += " Location: " + n.getLocation().getX() + ", " + n.getLocation().getY() + "\n";
			r += "\n\n";
		}
		r += "- Objects -\n";
		for (RSObject o : objects.getAll()) {
			r += " ID: " + o.getID() + "\n";
			r += " Type: " + o.getType() + "\n";
			r += " Name: " + o.getDef().getName() + "\n";
			r += "\n\n";
		}
		return r;
	}

	public final void onRepaint(Graphics g) {
		Point p = mouse.getLocation();
		int w = game.getWidth(), h = game.getHeight();
		if (i >= 70 && !up) {
			i--;
		} else {
			i++;
			up = i < 130;
//			if (!up) {
//				currentIndex++;
//				if (currentIndex >= fadeArray.length) {
//					currentIndex = 0;
//				}
//			}
		}
		g.setColor(new Color(0, 255, 0, i));
//		Color cur = fadeArray[currentIndex];
//		g.setColor(new Color(cur.getRed(), cur.getBlue(), cur.getGreen(), i));
		g.fillRect(0, 0, p.x - 1, p.y - 1);
		g.fillRect(p.x + 1, 0, w - (p.x + 1), p.y - 1);
		g.fillRect(0, p.y + 1, p.x - 1, h - (p.y - 1));
		g.fillRect(p.x + 1, p.y + 1, w - (p.x + 1), h - (p.y - 1));
		g.setColor(Color.RED);
		g.drawString("Random Active: " + name, 540, 20);
	}

}
