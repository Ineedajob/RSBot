import org.rsbot.event.listeners.PaintListener;
import org.rsbot.script.Script;
import org.rsbot.script.ScriptManifest;
import org.rsbot.script.methods.Skills;
import org.rsbot.script.wrappers.RSItem;
import org.rsbot.script.wrappers.RSObject;

import java.awt.*;

@ScriptManifest(authors = {"LastCoder"}, name = "Iron PowerMiner", version = 1.0, description = "Any Location, just start near rocks.")
public class PowerMiner extends Script implements PaintListener {

	private static enum State {
		MINE, DROP
	}

	private static final int[] ROCKS = new int[]{11956, 11954, 11955, 37307, 37308, 37309};
	private static final int IRON_ITEM = 440;

	private static final int TRAINING_SKILL = Skills.MINING;

	private static final Color COLOR_1 = new Color(0, 0, 0, 154);
	private static final Color COLOR_2 = new Color(0, 0, 0, 147);
	private static final Color COLOR_3 = new Color(255, 255, 255);
	private static final Font FONT_1 = new Font("Arial", 0, 12);
	private static final Font FONT_2 = new Font("Arial", 0, 10);
	private static final BasicStroke STROKE_1 = new BasicStroke(1);

	private long startExp;
	private long startLevel;
	private long startTime;

	private long expGained;
	private int expHour;

	private State getState() {
		if (inventory.isFull()) {
			return State.DROP;
		} else {
			return State.MINE;
		}
	}

	@Override
	public int loop() {
		switch (getState()) {
			case MINE:
				RSObject rock = objects.getNearest(ROCKS);
				if (rock != null) {
					if (!rock.isOnScreen()) {
						camera.turnToObject(rock);
						for (int i = 0; i < 100 && !rock.isOnScreen(); i++)
							sleep(20);
					} else {
						for (int i = 0; i < 100 && getMyPlayer().isMoving(); i++)
							sleep(20);
						rock.doAction("Mine");
						for (int i = 0; i < 100
								&& getMyPlayer().getAnimation() != -1; i++)
							sleep(20);
					}
				}
				break;
			case DROP:
				RSItem[] all = inventory.getItems();
				for (RSItem item : all) {
					if (item == null)
						continue;
					if (item.getID() == IRON_ITEM) {
						item.doAction("Drop");
						sleep(100);
					}
				}
				break;

		}
		return 0;
	}

	public boolean onStart() {
		startExp = (long) skills.getCurrentExp(Skills.MINING);
		startLevel = (long) skills.getRealLevel(Skills.MINING);
		startTime = System.currentTimeMillis();
		return game.isLoggedIn();
	}

	public void onRepaint(Graphics g1) {
		Graphics2D g = (Graphics2D) g1;
		long millis = System.currentTimeMillis() - startTime;
		long totalseconds = millis / 1000;
		long hours = millis / (1000 * 60 * 60);
		millis -= hours * 1000 * 60 * 60;
		long minutes = millis / (1000 * 60);
		millis -= minutes * 1000 * 60;
		long seconds = millis / 1000;
		if ((skills.getCurrentExp(Skills.MINING) - startExp) > 0
				&& startExp > 0) {
			expGained = skills.getCurrentExp(Skills.MINING) - startExp;
		}
		if (expGained > 0 && totalseconds > 0) {
			expHour = (int) (3600 * expGained / totalseconds);
		}
		g.setColor(COLOR_1);
		g.fillRect(366, 4, 135, 106);
		g.setColor(COLOR_2);
		g.setStroke(STROKE_1);
		g.drawRect(366, 4, 135, 106);
		g.setFont(FONT_1);
		g.setColor(COLOR_3);
		g.drawString("Iron PowerMiner", 393, 22);
		g.setFont(FONT_2);
		g.drawString("Time Run: " + hours + " : " + minutes + " : "
				+ seconds, 370, 40);
		g.drawString("EXP Gained: " + expGained, 370, 55);
		g.drawString("EXP/Hr: " + expHour, 370, 70);
		g.drawString("Levels Gained: (" + startLevel + ") Gained: "
				+ (skills.getRealLevel(TRAINING_SKILL) - startLevel), 370, 85);
	}

}