import org.rsbot.event.listeners.PaintListener;
import org.rsbot.script.Script;
import org.rsbot.script.ScriptManifest;
import org.rsbot.script.methods.Skills;
import org.rsbot.script.util.Timer;
import org.rsbot.script.wrappers.RSArea;
import org.rsbot.script.wrappers.RSComponent;
import org.rsbot.script.wrappers.RSItem;
import org.rsbot.script.wrappers.RSNPC;
import org.rsbot.script.wrappers.RSObject;
import org.rsbot.script.wrappers.RSTile;

import java.awt.*;
import java.util.HashSet;
import java.util.Set;

@ScriptManifest(name = "Guild Ranger", authors = {"Vastico"}, description = "Trains range at the ranging guild on the targets.", version = 1.7, keywords = {
		"Combat", "Ranged"})
public class GuildRanger extends Script implements PaintListener {

	private interface GameConstants {

		RSArea SHOOTING_AREA = new RSArea(2669, 3417, 2673, 3420);

		RSArea SAFE_AREA = new RSArea(2646, 3440, 2643, 3444);

		RSTile GUILD_DOOR_TILE = new RSTile(2659, 3437);

		RSTile SAFE_DOOR_TILE = new RSTile(2656, 3440);

		int TARGET = 2513;

		int BRONZE_ARROW = 882;

		int COMPETITION_JUDGE = 693;

		int GUILD_DOOR = 2514;

		int PAYMENT_INTERFACE = 236;

		int TARGET_INTERFACE = 325;

		Color BACKGROUND_COLOR = new Color(0, 0, 0, 100);

		Color TEXT_COLOR = new Color(255, 255, 255, 255);

		RenderingHints ANTI_ALIASING = new RenderingHints(
				RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_ON);

	}

	private abstract class Action {

		public abstract void execute();

		public abstract void complete();

		public abstract boolean isValid();

		public abstract String getDescription();

		public void paint(Graphics render) {
		}

	}

	private abstract class ObjectAction extends Action {

		private final int id;
		private final String action;
		private Point location;
		private int attempts = 0;

		public ObjectAction(int id, String action) {
			this.id = id;
			this.action = action;
		}

		public void execute() {
			if (location != null) {
				mouse.move(location, 1, 1);
				if (isTargetInterfaceOpen()) {
					mouse.click(location, 1, 1, true);
				}
				if (menu.doAction(action)) {
					sleep(20);
					attempts = 0;
				} else if (attempts > 8) {
					failsafe = true;
				} else {
					attempts++;
				}
			} else {
				RSObject obj = objects.getNearest(id);
				if (obj != null) {
					if (obj.isOnScreen()) {
						location = obj.getModel().getPoint();
					} else {
						camera.turnToObject(obj, 10);
					}
				}
			}
		}

		public void complete() {
			attempts = 0;
			location = null;
		}

		public String getDescription() {
			return "Object Action [" + action + "]";
		}

	}

	private abstract class NPCAction extends Action {

		private int id;
		private String action;

		public NPCAction(int id, String action) {
			this.id = id;
			this.action = action;
		}

		public void execute() {
			if (judge == null) {
				judge = npcs.getNearest(id);
			}
			if (judge != null) {
				if (!judge.isOnScreen()) {
					camera.turnToCharacter(judge, 10);
				} else if (judge.doAction(action)) {
					if (!getMyPlayer().isIdle()) {
						sleep(500);
					}
					sleep(1500);
				}
			}
		}

		public void complete() {

		}

		public String getDescription() {
			return "NPC Action [" + action + "]";
		}

	}

	private abstract class UniversalAction extends Action {

		public void execute() {
		}

		public void complete() {
		}

		public boolean isValid() {
			return false;
		}

		public String getDescription() {
			return "";
		}

	}

	private abstract class WalkToArea extends Action {

		private final RSArea destination;
		private final String description;
		private RSTile location;

		public WalkToArea(RSArea destination, String description) {
			this.destination = destination;
			this.description = description;
		}

		public abstract boolean isTargetValid();

		public void execute() {
			if (!walking.isRunEnabled() && walking.getEnergy() > random(20, 50)) {
				walking.setRun(true);
				sleep(500);
			}
			RSTile tile = destination.getCentralTile();
			if (location == null
					|| getMyPlayer().isIdle()
					|| (calc.distanceTo(location) < 10 && !destination
					.contains(location))) {
				if (calc.tileOnScreen(tile) && random(0, 10) > 7) {
					walking.walkTileOnScreen(calc.getTileOnScreen(tile));
				} else {
					walking.walkTo(tile);
				}
				location = walking.getDestination();
				sleep(random(1000, 1800));
			}
		}

		public void complete() {
			location = null;
		}

		public boolean isValid() {
			return isTargetValid()
					&& !destination.contains(getMyPlayer().getLocation());
		}

		public String getDescription() {
			return "Walk To Area [" + description + "]";
		}

	}

	private Set<Action> actions;
	private long startTime = 0L;
	private int startXp = 0;
	private int startLvl = 0;
	private RSNPC judge;
	private Action action;
	private boolean failsafe = false;

	public boolean onStart() {
		actions = new HashSet<Action>();

		actions.add(new UniversalAction() {

			public void execute() {
				RSComponent closeInterface = interfaces.getComponent(
						GameConstants.TARGET_INTERFACE, 40);
				if (closeInterface != null && closeInterface.isValid()) {
					closeInterface.doClick();
					sleep(500);
				}
				failsafe = false;
			}

			public boolean isValid() {
				return failsafe && isTargetInterfaceOpen();
			}

			public String getDescription() {
				return "Closing Failsafe Interface";
			}

		});

		actions.add(new UniversalAction() {

			public void execute() {
				RSComponent paymentInterface = interfaces.getComponent(
						GameConstants.PAYMENT_INTERFACE, 1);
				if (paymentInterface != null && paymentInterface.isValid()) {
					paymentInterface.doClick();
					sleep(500);
				}
			}

			public boolean isValid() {
				return isPaymentInterfaceOpen();
			}

			public String getDescription() {
				return "Paying Competition Judge";
			}

		});

		actions.add(new UniversalAction() {

			public void execute() {
				RSItem bronzeArrow = inventory
						.getItem(GameConstants.BRONZE_ARROW);
				if (bronzeArrow != null) {
					bronzeArrow.doAction("Wield");
					sleep(1200);
				}
			}

			public boolean isValid() {
				return hasArrowsInInventory();
			}

			public String getDescription() {
				return "Wielding Arrows";
			}

		});

		actions.add(new WalkToArea(GameConstants.SAFE_AREA, "safe area") {
			public void execute() {
				RSObject obj = objects.getNearest(GameConstants.GUILD_DOOR);
				if (obj != null
						&& calc.distanceBetween(obj.getLocation(),
						GameConstants.GUILD_DOOR_TILE) < 2) {
					if (obj.isOnScreen()) {
						if (obj.doAction("Open")) {
							sleep(random(1000, 2000));
						}
					} else if (!GameConstants.GUILD_DOOR_TILE.equals(walking
							.getDestination())) {
						walking.walkTo(GameConstants.GUILD_DOOR_TILE);
					}
				} else {
					super.execute();
				}
			}

			public boolean isTargetValid() {
				return isAttackingRanger();
			}
		});

		actions.add(new WalkToArea(GameConstants.SHOOTING_AREA, "shooting area") {
			public void execute() {
				RSObject obj = objects.getNearest(GameConstants.GUILD_DOOR);
				if (obj != null
						&& calc.distanceBetween(obj.getLocation(),
						GameConstants.SAFE_DOOR_TILE) < 2) {
					if (obj.isOnScreen()) {
						if (obj.doAction("Open")) {
							sleep(random(1000, 2000));
						}
					} else if (!GameConstants.SAFE_DOOR_TILE.equals(walking
							.getDestination())) {
						walking.walkTo(GameConstants.SAFE_DOOR_TILE);
					}
				} else {
					super.execute();
				}
			}

			public boolean isTargetValid() {
				return !inShootingArea() && !isAttackingRanger();
			}
		});

		actions.add(new ObjectAction(GameConstants.TARGET, "Fire-at") {
			public void execute() {
				super.execute();
			}

			public boolean isValid() {
				return !canCompete() && inShootingArea()
						&& !hasArrowsInInventory() && !failsafe;
			}
		});

		actions.add(new NPCAction(GameConstants.COMPETITION_JUDGE, "Compete") {
			public boolean isValid() {
				return canCompete() && !isPaymentInterfaceOpen()
						&& !hasArrowsInInventory();
			}
		});

		return true;
	}

	public int loop() {
		mouse.setSpeed(random(6, 8));
		if (camera.getPitch() > 1) {
			camera.setPitch(false);
		}
		if (action != null) {
			if (action.isValid()) {
				action.execute();
			} else {
				action.complete();
				action = null;
			}
		} else {
			for (Action a : actions) {
				if (a.isValid()) {
					action = a;
					break;
				}
			}
		}
		return random(50, 100);
	}

	public void onFinish() {
	}

	private boolean inShootingArea() {
		return GameConstants.SHOOTING_AREA
				.contains(getMyPlayer().getLocation());
	}

	private boolean isAttackingRanger() {
		return getMyPlayer().getInteracting() != null
				&& getMyPlayer().getInteracting().getName() != null
				&& getMyPlayer().getInteracting().getName().equals("Guard");
	}

	private boolean canCompete() {
		return settings.getSetting(156) == 0 || interfaces.get(243).isValid();
	}

	private boolean hasArrowsInInventory() {
		return inventory.getCount(GameConstants.BRONZE_ARROW) > 0;
	}

	private boolean isTargetInterfaceOpen() {
		return interfaces.get(GameConstants.TARGET_INTERFACE).isValid();
	}

	private boolean isPaymentInterfaceOpen() {
		return interfaces.get(GameConstants.PAYMENT_INTERFACE).isValid();
	}

	public void onRepaint(Graphics render) {
		if (game.isLoggedIn() && skills.getRealLevel(Skills.RANGE) >= 40) {
			Graphics2D g = (Graphics2D) render;
			g.setRenderingHints(GameConstants.ANTI_ALIASING);
			if (startTime == 0) {
				startTime = System.currentTimeMillis();
			}
			if (startXp == 0) {
				startXp = skills.getCurrentExp(Skills.RANGE);
			}
			if (startLvl == 0) {
				startLvl = skills.getRealLevel(Skills.RANGE);
			}
			if (action != null) {
				action.paint(g);
			}
			g.setColor(GameConstants.BACKGROUND_COLOR);
			g.fillRect(10, 35, 205, 195);
			g.setColor(GameConstants.TEXT_COLOR);
			g.drawRect(10, 35, 205, 195);
			g.drawString("GuildRanger by Vastico", 20, 55);
			g.drawString(Timer.format(System.currentTimeMillis() - startTime),
					20, 75);
			g.drawString(action != null ? action.getDescription()
					: "Calculating...", 20, 95);
			g.drawString("XP Gained: "
					+ (skills.getCurrentExp(Skills.RANGE) - startXp), 20, 115);
			g.drawString("XP Per Hour: " + calculateXpPerHour(), 20, 135);
			g.drawString("Current Level: " + skills.getRealLevel(Skills.RANGE),
					20, 155);
			g.drawString("Levels Gained: "
					+ (skills.getRealLevel(Skills.RANGE) - startLvl), 20, 175);
			g.drawString("Current Score: " + settings.getSetting(157), 20, 195);
			int hit = settings.getSetting(156);
			hit = hit >= 1 ? hit - 1 : 0;
			g.drawString("Arrows Fired(10): " + hit, 20, 215);
		}
	}

	private int calculateXpPerHour() {
		int gainedXp = (skills.getCurrentExp(Skills.RANGE) - startXp);
		return (int) ((3600000.0 / (double) (System.currentTimeMillis() - startTime)) * gainedXp);
	}

}