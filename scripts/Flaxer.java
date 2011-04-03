import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.text.NumberFormat;
import java.util.Locale;

import org.rsbot.event.listeners.PaintListener;
import org.rsbot.script.Script;
import org.rsbot.script.ScriptManifest;
import org.rsbot.script.methods.Game;
import org.rsbot.script.methods.Skills;
import org.rsbot.script.wrappers.RSArea;
import org.rsbot.script.wrappers.RSInterface;
import org.rsbot.script.wrappers.RSObject;
import org.rsbot.script.wrappers.RSTile;
import org.rsbot.script.wrappers.RSTilePath;

@ScriptManifest(authors = "Vastico", name = "Flaxer", version = 0.12)
public class Flaxer extends Script implements PaintListener {

	/*
	 * Constants
	 */
	private static enum Action {
		WALK_TO_FIELD, WALK_TO_BANK, PICK, BANK
	}

	private static final RSTile[] PATH = { new RSTile(2726, 3491),
			new RSTile(2725, 3481), new RSTile(2728, 3472),
			new RSTile(2726, 3464), new RSTile(2730, 3453),
			new RSTile(2735, 3447), new RSTile(2739, 3443) };

	private static final int FLAX_OBJECT = 2646;

	private static final int FLAX_ITEM = 1779;

	private static final RSArea FLAX_FIELD = new RSArea(2737, 3436, 2751, 3451);

	private static final RSArea BANK_AREA = new RSArea(2722, 3490, 2730, 3493);

	private static final Color MOUSE_COLOR = new Color(0, 0, 0, 50);

	private static final Color MOUSE_BORDER_COLOR = new Color(255, 252, 0, 50);

	private static final RenderingHints RENDERING_HINTS = new RenderingHints(
			RenderingHints.KEY_TEXT_ANTIALIASING,
			RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

	private RSTilePath pathToFlax;
	private RSTilePath pathToBank;

	/* Script State */

	private int nextMinRunEnergy;

	private int flaxPrice;
	private int flaxCollected;
	private long startTime;

	/*
	 * Overridden Methods
	 */

	@Override
	public boolean onStart() {
		flaxPrice = grandExchange.lookup(FLAX_ITEM).getGuidePrice();
		return true;
	}

	/*
	 * Implemented Methods
	 */

	@Override
	public int loop() {
		if (startTime == 0) {
			if (skills.getCurrentLevel(Skills.CONSTITUTION) > 1) {
				initializeState();
			} else {
				return 500;
			}
		}
		if (!walking.isRunEnabled() && walking.getEnergy() > nextMinRunEnergy) {
			nextMinRunEnergy = random(30, 40);
			walking.setRun(true);
		}
		switch (getAction()) {
		case WALK_TO_BANK:
			if (pathToBank.traverse()) {
				sleep(500);
			}
			break;
		case WALK_TO_FIELD:
			if (pathToFlax.traverse()) {
				sleep(500);
			}
			break;
		case PICK:
			RSObject flax = objects.getNearest(FLAX_OBJECT);
			if (flax != null && flax.isOnScreen()) {
				int startCount = inventory.getCount();
				if (pickFlax(flax)) {
					long picked = System.currentTimeMillis();
					while (startCount == inventory.getCount()
							&& System.currentTimeMillis() - picked < 10000) {
						sleep(100);
					}
					if (startCount < inventory.getCount()) {
						flaxCollected++;
					}
				}
			}
			break;
		case BANK:
			if (!bank.isOpen()) {
				bank.open();
			} else {
				bank.depositAll();
				long deposit = System.currentTimeMillis();
				while (inventory.getCount() > 0
						&& System.currentTimeMillis() - deposit < 10000) {
					sleep(100);
				}
			}
		}
		return random(100, 200);
	}

	@Override
	public void onRepaint(Graphics render) {
		if (startTime == 0) {
			return;
		}

		Graphics2D g = (Graphics2D) render;
		g.setRenderingHints(RENDERING_HINTS);
		NumberFormat comma = NumberFormat.getNumberInstance(new Locale("en",
				"IN"));
		RSInterface chatBox = interfaces.get(Game.INTERFACE_CHAT_BOX);
		int y = 344;
		if (chatBox != null && game.isLoggedIn()) {
			y = chatBox.getComponent(0).getLocation().y;
		}
		long millis = System.currentTimeMillis() - startTime;
		long hours = millis / (1000 * 60 * 60);
		millis -= hours * 1000 * 60 * 60;
		long minutes = millis / (1000 * 60);
		millis -= minutes * 1000 * 60;
		long seconds = millis / 1000;

		g.setColor(new Color(168, 9, 9, 255));
		g.setFont(new Font("Arial", Font.BOLD, 12));

		g.drawString("Time Running:", 12, y + 41);
		g.drawString("Flax Picked:", 12, y + 57);
		g.drawString("Flax Value:", 12, y + 73);

		g.setColor(new Color(0, 0, 0, 255));
		g.setFont(new Font("Arial", Font.PLAIN, 12));

		g.drawString(hours + " Hours " + minutes + " Minutes " + seconds
				+ " Seconds", 120, y + 41);
		g.drawString(comma.format(flaxCollected), 120, y + 57);
		g.drawString(comma.format((flaxPrice * flaxCollected)), 120, y + 73);

		g.setFont(new Font("Arial", Font.PLAIN, 9));
		g.drawString("V. "
				+ getClass().getAnnotation(ScriptManifest.class).version(),
				478, y + 124);
		drawMouse(g);

	}

	/*
	 * Defined Methods
	 */

	private void initializeState() {
		nextMinRunEnergy = 20;
		startTime = System.currentTimeMillis();
		flaxCollected = 0;

		pathToFlax = walking.newTilePath(PATH);
		pathToBank = walking.newTilePath(PATH).reverse();
	}

	private Action getAction() {
		if (inventory.isFull()) {
			if (inArea(BANK_AREA)) {
				return Action.BANK;
			}
			return Action.WALK_TO_BANK;
		}
		if (inArea(FLAX_FIELD)) {
			return Action.PICK;
		}
		return Action.WALK_TO_FIELD;
	}

	public void drawMouse(final Graphics g) {
		Point location = mouse.getLocation();
		if (mouse.isPressed()) {
			g.setColor(new Color(255, 252, 0, 150));
			g.fillOval(location.x - 5, location.y - 5, 10, 10);
		}
		g.setColor(MOUSE_BORDER_COLOR);
		g.drawLine(location.x, 0, location.x, game.getHeight());
		g.drawLine(0, location.y, game.getWidth(), location.y);
		g.setColor(MOUSE_COLOR);
		g.drawLine(location.x + 1, 0, location.x + 1, game.getHeight());
		g.drawLine(location.x - 1, 0, location.x - 1, game.getHeight());
		g.drawLine(0, location.y + 1, game.getWidth(), location.y + 1);
		g.drawLine(0, location.y - 1, game.getWidth(), location.y - 1);
	}

	private boolean pickFlax(RSObject obj) {
		if (getMyPlayer().isMoving()) {
			for (int i = 0, len = random(2, 5); i < len; ++i) {
				mouse.move(obj.getModel().getPoint());
				sleep(random(20, 100));
			}
			return menu.doAction("Pick");
		} else {
			return obj.doAction("Pick");
		}
	}

	private boolean inArea(RSArea area) {
		if (area != null && !area.contains(getMyPlayer().getLocation())) {
			RSTile dest = walking.getDestination();
			return dest != null && getMyPlayer().isMoving()
					&& area.contains(dest) && calc.distanceTo(dest) < 8;
		}
		return true;
	}

}