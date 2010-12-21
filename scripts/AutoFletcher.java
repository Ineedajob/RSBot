import org.rsbot.event.events.MessageEvent;
import org.rsbot.event.listeners.MessageListener;
import org.rsbot.event.listeners.PaintListener;
import org.rsbot.script.Script;
import org.rsbot.script.ScriptManifest;
import org.rsbot.script.methods.Skills;
import org.rsbot.script.util.Filter;
import org.rsbot.script.wrappers.RSComponent;
import org.rsbot.script.wrappers.RSInterface;
import org.rsbot.script.wrappers.RSItem;
import org.rsbot.script.wrappers.RSObject;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * An auto-fletching script that strings and cuts.
 *
 * @author Speed
 */
@ScriptManifest(authors = "Speed", keywords = "Fletching", name = "AutoFletcher", version = 1.2, description = "Bow fletcher and stringer.")
public class AutoFletcher extends Script implements PaintListener, MessageListener {

	private static final int KNIFE_ID = 946, CLAY_KNIFE = 14111;
	private static final int BOWSTRING_ID = 1777;
	private static final String[] TYPE_OPTION = new String[]{"Fletch", "String"};
	private static final String[] SELECT_OPTION = new String[]{"Shortbow", "Longbow", "Oak shortbow", "Oak longbow",
			"Willow shortbow", "Willow longbow", "Maple shortbow", "Maple longbow", "Yew shortbow", "Yew longbow",
			"Magic shortbow", "Magic longbow"};
	private static final int[] UNSTRUNG_BOWS = new int[]{50, 48, 54, 56, 60, 58, 64, 62, 68, 66, 72, 70};
	private static final int[] LOG_IDS = new int[]{1511, 1511, 1521, 1521, 1519, 1519, 1517, 1517, 1515, 1515, 1513,
			1513};
	private static final Filter<RSObject> FILTER_VISIBLE = new Filter<RSObject>() {
		public boolean accept(RSObject obj) {
			return obj.isOnScreen();
		}
	};

	private Type type;
	private int logIndex;
	private boolean shortbow;
	private int startXP, startLevel;
	private int item1, item2;
	private long startTime;
	private long minutes;
	private long hours;

	private enum State {
		BANK, FLETCH, ANTIBAN, OPENBANK, CLOSEBANK
	}

	private enum Type {
		CUT, STRING
	}

	@Override
	public boolean onStart() {
		final GUI gui = new GUI();
		while (gui.isVisible()) {
			sleep(500);
		}
		shortbow = logIndex == 0 || (logIndex % 2) == 0;
		return true;
	}

	public int loop() {
		if (startTime == 0 && skills.getRealLevel(Skills.CONSTITUTION) > 1) {
			startTime = System.currentTimeMillis();
			startXP = skills.getCurrentExp(Skills.FLETCHING);
			startLevel = skills.getRealLevel(Skills.FLETCHING);
		}
		switch (getState()) {
			case BANK:
				switch (type) {
					case CUT:
						if (inventory.getCount() > 1) {
							bank.depositAllExcept(item1);
							sleep(200);
							if (inventory.getCount() > 1) {
								sleep(500);
							}
						} else {
							if (bank.getCount(item2) < 27) {
								log("Finished");
								return -1;
							} else if (bank.getCount(item2) > 27) {
								bank.withdraw(item2, 27);
							}
							if (!inventory.contains(item1)) {
								if (bank.getCount(item1) >= 1) {
									bank.withdraw(item1, 1);
									sleep(500);
								}
							}
						}
						break;
					case STRING:
						if (inventory.getCount() > 0) {
							bank.depositAll();
							sleep(200);
							if (inventory.getCount() > 0) {
								sleep(500);
							}
						} else {
							if (bank.getCount(item1) < 14 || bank.getCount(item2) < 14) {
								log("Finished");
								return -1;
							} else if (bank.getCount(item1) > 14 && bank.getCount(item2) > 14) {
								if (!inventory.contains(item1)) {
									bank.withdraw(item1, 14);
								}
								if (!inventory.contains(item2)) {
									bank.withdraw(item2, 14);
								}

							}
						}
						break;
					default:
						break;
				}
				return random(500, 800);
			case FLETCH:
				final RSInterface inf = interfaces.get(905);
				RSComponent child;
				switch (type) {
					case STRING:
						child = inf.getComponent(14);
						if (child != null && child.isValid()) {
							if (child.doAction("Make All")) {
								sleep(900);
							}
						} else if (child != null && !child.isValid()) {
							RSItem item = inventory.getSelectedItem();
							int selected = item == null ? -1 : item.getID();
							if (selected == item1 || selected == item2) {
								if (selected == item1) {
									inventory.getItem(item2).doAction("Use");
								} else {
									inventory.getItem(item1).doAction("Use");
								}
								sleep(600);
							} else {
								inventory.getItem(item1, item2).doAction("Use");
							}
						}
						break;
					case CUT:
						if (shortbow) {
							if (logIndex < 2) {
								child = inf.getComponent(15);
							} else {
								child = inf.getComponent(14);
							}
						} else {
							if (logIndex < 2) {
								child = inf.getComponent(16);
							} else {
								child = inf.getComponent(15);
							}
						}
						if (child != null && child.isValid()) {
							child.doClick();
						} else if (child != null && !child.isValid()) {
							if (inventory.getSelectedItem() != null) {
								int selected = inventory.getSelectedItem().getID();
								if (selected == item1 || selected == item2) {
									if (selected == item1) {
										inventory.getItem(item2).doAction("Use");
									} else {
										inventory.getItem(item1).doAction("Use");
									}
									sleep(500);
								} else {
									inventory.getSelectedItem().doClick(true);
								}
							} else {
								inventory.getItem(item1, item2).doAction("Use");
							}
						}
						break;
					default:
						break;
				}
				return random(300, 600);
			case OPENBANK:
				if (!bank.isOpen())
					bank.open();
				return random(200, 800);
			case CLOSEBANK:
				if (bank.isOpen())
					bank.close();
				return random(300, 600);
			case ANTIBAN:
				if (random(0, 5) == 0) {
					switch (random(1, 5)) {
						case 1:
							camera.setAngle((int) (((camera.getAngle() + random(0, 180)) * 3) / 2));
							break;
						case 2:
							camera.setPitch(true);
							break;
						case 3:
							camera.setPitch(false);
							break;
						case 4:
							RSObject[] obj = objects.getAll(FILTER_VISIBLE);
							if (obj.length > 0) {
								camera.turnToTile(obj[random(0, obj.length - 1)].getLocation());
							}
							break;
						default:
							break;
					}
				}
				if (random(0, 5) == 0) {
					RSObject[] objs = objects.getAll(FILTER_VISIBLE);
					switch (random(1, 5)) {
						case 1:
							mouse.move((int) ((mouse.getLocation().x * 3.5) / 2.5) + random(10, 30),
									((int) ((mouse.getLocation().y * 3.5) / 2.5) + random(10, 30)));
							break;
						case 2:
							RSItem[] items = inventory.getItems();
							items[random(0, items.length - 1)].getComponent().doHover();
							break;
						case 3:
							if (objs.length > 0) {
								objs[random(0, objs.length - 1)].doHover();
							}
							break;
						case 4:
							mouse.move((int) ((mouse.getLocation().x * 3.5) / 2.5) + random(5, 30),
									((int) ((mouse.getLocation().y * 3.5) / 2.5) + random(5, 30)));
							break;
						default:
							break;
					}
				}
				return random(600, 800);
			default:
				return random(200, 400);
		}
	}

	private boolean isBusy() {
		for (int i = 0; i < 50; i++) {
			if (getMyPlayer().isMoving() || getMyPlayer().getAnimation() != -1) {
				return true;
			}
			sleep(random(24, 26));
		}
		return false;
	}

	public void messageReceived(final MessageEvent e) {

	}

	public void onRepaint(final Graphics g) {
		if (startTime == 0) {
			return;
		}
		long runTime = System.currentTimeMillis() - startTime;
		long seconds = runTime / 1000;
		if (seconds >= 60) {
			minutes = seconds / 60;
			seconds -= (minutes * 60);
		}
		if (minutes >= 60) {
			hours = minutes / 60;
			minutes -= (hours * 60);
		}
		final int gainedXp = skills.getCurrentExp(Skills.FLETCHING) - startXP;
		final int gainedLevels = skills.getRealLevel(Skills.FLETCHING) - startLevel;
		g.setColor(new Color(0, 0, 0, 150));
		g.fill3DRect(20, 297, 150, 80, true);
		g.setColor(Color.WHITE);
		g.drawString("AutoFletcher by Speed", 25, 310);
		g.drawString("Gained XP: " + gainedXp, 25, 325);
		g.drawString("Gained Levels: " + gainedLevels, 25, 340);

		if ((runTime / 1000) > 0 && gainedXp > 0) {
			g.drawString("Run time: " + hours + ":" + minutes + ":" + seconds, 25, 355);
			int xpPerHour = (int) ((3600000.0 / (double) runTime) * gainedXp);
			g.drawString("XP/hour: " + xpPerHour, 25, 370);
		}
	}

	public State getState() {
		if (!isBusy()) {
			if (!inventory.containsAll(item1, item2) && !bank.isOpen()) {
				return State.OPENBANK;
			} else if (bank.isOpen() && !inventory.containsAll(item1, item2)) {
				return State.BANK;
			} else if (bank.isOpen() && inventory.containsAll(item1, item2)) {
				return State.CLOSEBANK;
			} else if (inventory.containsAll(item1, item2) && !bank.isOpen()) {
				return State.FLETCH;
			}
		}
		return State.ANTIBAN;
	}

	class GUI extends JFrame {

		private static final long serialVersionUID = 4491918393383484729L;
		private JComboBox selector, select1;
		private JLabel label, label1;
		private JButton startButton, cancelButton;
		private JCheckBox clayKnife;

		public GUI() {
			createAndShowGUI();
			setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		}

		private void createAndShowGUI() {
			selector = new JComboBox(TYPE_OPTION);
			select1 = new JComboBox(SELECT_OPTION);
			label = new JLabel("Fletching type:");
			label1 = new JLabel("Make:");
			startButton = new JButton("Start");
			cancelButton = new JButton("Cancel");
			clayKnife = new JCheckBox("Clay knife?");
			startButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					if (selector.getSelectedItem().equals(TYPE_OPTION[0])) {
						logIndex = select1.getSelectedIndex();
						type = Type.CUT;
						item1 = clayKnife.isSelected() ? CLAY_KNIFE : KNIFE_ID;
						item2 = LOG_IDS[select1.getSelectedIndex()];
					} else {
						logIndex = select1.getSelectedIndex();
						type = Type.STRING;
						item1 = BOWSTRING_ID;
						item2 = UNSTRUNG_BOWS[select1.getSelectedIndex()];
					}
					dispose();
				}
			});
			cancelButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					dispose();
					stopScript();
				}

			});
			setSize(400, 300);
			setLayout(new GridLayout(0, 2));
			add(label);
			add(selector);
			add(label1);
			add(select1);
			add(clayKnife);
			add(new JLabel());// empty space
			add(startButton);
			add(cancelButton);
			setVisible(true);
		}
	}

}