import org.rsbot.event.listeners.PaintListener;
import org.rsbot.script.Script;
import org.rsbot.script.ScriptManifest;
import org.rsbot.script.util.Filter;
import org.rsbot.script.wrappers.RSGroundItem;
import org.rsbot.script.wrappers.RSItem;
import org.rsbot.script.wrappers.RSNPC;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;

@ScriptManifest(authors = "Foulwerp", name = "Fighter", keywords = "Combat", description = "Basic Multipurpose Fighter...", version = 1.05)
public class Fighter extends Script implements PaintListener {

	private static final int[] skill = {0, 1, 2, 3, 4, 6};
	private static final String[] skillNames = {"Attack", "Defence", "Strength", "Constitution", "Range", "Magic"};
	private static final Color[] skillColors = {new Color(145, 25, 25).brighter(), new Color(95, 115, 185),
			Color.GREEN.darker(), Color.WHITE.darker(), new Color(70, 95, 20).brighter(),
			new Color(95, 115, 230)};

	private long start;
	private DecimalFormat k = new DecimalFormat("#.#");
	private int[] lootID, npcID, startXP;
	private static final int[] bones = {526, 532, 530, 528, 3183, 2859};
	private FighterGUI gui;

	private enum foul {
		FIGHTING, LOOTING, ATTACKING
	}

	private foul foul() {
		if (players.getMyPlayer().getInteracting() != null) {
			return foul.FIGHTING;
		} else if (pickup() != null) {
			return foul.LOOTING;
		} else {
			return foul.ATTACKING;
		}
	}

	@Override
	public boolean onStart() {
		gui = new FighterGUI();
		startXP = new int[6];
		for (int i = 0; i < skill.length; i++) {
			startXP[i] = skills.getCurrentExp(skill[i]);
		}
		start = System.currentTimeMillis();
		return true;
	}

	@Override
	public int loop() {
		mouse.setSpeed(random(4, 7));
		if (!game.isLoggedIn()) {
			return 3000;
		}
		if (!walking.isRunEnabled() && walking.getEnergy() > 60) {
			walking.setRun(true);
			return random(750, 1000);
		}
		if (combat.getLifePoints() < (skills.getRealLevel(3) * 10) / 2) {
			RSItem food = edible();
			if (food != null) {
				food.doAction("Eat ");
				return random(900, 1100);
			} else if (inventory.getItem(8015) != null && inventory.getCount(bones) > 0) {
				inventory.getItem(8015).doAction("Break");
				return random(1800, 2200);
			} else {
				log("No More Food Or Tabs...");
				game.logout(true);
				return -1;
			}
		}
		switch (foul()) {
			case FIGHTING:
				if (players.getMyPlayer().getInteracting() != null) {
					if (interfaces.canContinue()) {
						interfaces.clickContinue();
					}
					return random(900, 1100);
				}
				break;
			case LOOTING:
				RSGroundItem loot = pickup();
				if (loot != null) {
					if (players.getMyPlayer().isMoving()) {
						return random(400, 600);
					}
					if (inventory.isFull()) {
						if (inventory.getItem(bones) != null) {
							inventory.getItem(bones).doAction("Drop");
							return random(800, 1000);
						} else if (edible() != null) {
							edible().doAction("Eat");
							return random(800, 1000);
						}
					} else {
						if (!loot.isOnScreen()) {
							camera.turnToTile(loot.getLocation(), 15);
							if (!loot.isOnScreen()) {
								walking.walkTileMM(walking.getClosestTileOnMap(loot.getLocation()));
								return random(900, 1200);
							}
						}
						loot.doAction("Take " + loot.getItem().getName());
						return random(900, 1100);
					}
				}
			case ATTACKING:
				RSNPC npc = newNPC();
				if (npc == null) {
					return random(800, 1000);
				}
				if (players.getMyPlayer().isMoving() && !npc.isOnScreen()) {
					return random(400, 600);
				}
				if (!npc.isOnScreen()) {
					turnTo(camera.getCharacterAngle(npc), npc);
					if (!npc.isOnScreen()) {
						walking.walkTileMM(walking.getClosestTileOnMap(npc.getLocation()));
						return random(800, 1000);
					}
				}
				npc.doAction("Attack " + npc.getName());
		}
		return random(900, 1100);
	}

	@Override
	public void onFinish() {
		gui.dispose();
	}

	public void onRepaint(Graphics render) {
		int y = 365, z = 356, w = 196, x = 201;
		final Graphics2D g = (Graphics2D) render;
		long runTime = System.currentTimeMillis() - start;
		g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		Point m = mouse.getLocation();
		g.drawLine((int) m.getX() - 3, (int) m.getY(), (int) m.getX() + 3, (int) m.getY());
		g.drawLine((int) m.getX(), (int) m.getY() - 3, (int) m.getX(), (int) m.getY() + 3);
		g.setFont(new Font("Comic Sans MS", Font.PLAIN, 10));
		g.setColor(Color.BLACK);
		g.drawRect(w, 345, 300, 11);
		g.setColor(new Color(0, 0, 0, 220));
		g.fillRect(w, 345, 300, 11);
		g.setColor(Color.WHITE);
		g.drawString("Fighter - Run Time: " + format(runTime) + " - Version 1.05", x, 354);
		long ttl;
		int exp;
		double eph;
		for (int i = 0; i < 6; i++, exp = 0, eph = 0, ttl = 0) {
			exp = (skills.getCurrentExp(skill[i]) - startXP[i]);
			if (exp > 0) {
				eph = (exp * 3600000D / (System.currentTimeMillis() - start));
				ttl = (long) ((skills.getExpToNextLevel(skill[i]) * 3600000D) / eph);
				g.setColor(Color.BLACK);
				g.drawRect(w, z, 300, 11);
				g.setColor(new Color(0, 0, 0, 220));
				g.fillRect(w, z, 300, 11);
				g.setColor(skillColors[i]);
				g.drawString(skillNames[i] + ": " + k.format(exp / 1000D) + " K Earned - " + k.format(eph / 1000)
						+ " K P/H - " + format(ttl) + " TTL", x, y);
				y += 11;
				z += 11;
			}
		}
	}

	private String format(long time) {
		if (time <= 0) {
			return "--:--:--";
		}
		final StringBuilder t = new StringBuilder();
		final long TotalSec = time / 1000;
		final long TotalMin = TotalSec / 60;
		final long TotalHour = TotalMin / 60;
		final int second = (int) TotalSec % 60;
		final int minute = (int) TotalMin % 60;
		final int hour = (int) TotalHour;
		if (hour < 10) {
			t.append("0");
		}
		t.append(hour);
		t.append(":");
		if (minute < 10) {
			t.append("0");
		}
		t.append(minute);
		t.append(":");
		if (second < 10) {
			t.append("0");
		}
		t.append(second);
		return t.toString();
	}

	private RSGroundItem pickup() {
		return groundItems.getNearest(new Filter<RSGroundItem>() {
			public boolean accept(RSGroundItem g) {
				if (lootID == null) {
					return false;
				}
				if (inventory.getCount() >= 27) {
					for (int b : bones) {
						if (g.getItem().getID() != b) {
							continue;
						}
						return false;
					}
				}
				for (int i : lootID) {
					if (g.getItem().getID() != i) {
						continue;
					}
					return true;
				}
				return false;
			}
		});
	}

	private RSNPC newNPC() {
		RSNPC interacting = interactingNPC();
		return interacting != null ? interacting : npcs.getNearest(new Filter<RSNPC>() {
			public boolean accept(RSNPC n) {
				if (npcID == null) {
					return false;
				}
				for (int i : npcID) {
					if (n.getID() != i || n.getHPPercent() == 0 || n.isInCombat()) {
						continue;
					}
					return true;
				}
				return false;
			}
		});
	}

	private RSNPC interactingNPC() {
		return npcs.getNearest(new Filter<RSNPC>() {
			public boolean accept(RSNPC n) {
				if (n.getInteracting() == null) {
					return false;
				}
				String[] acts = n.getActions();
				if (acts == null) {
					return false;
				}
				for (String a : acts) {
					if (a == null || !a.contains("Attack")) {
						continue;
					}
					return n.getInteracting().equals(players.getMyPlayer());
				}
				return false;
			}
		});
	}

	private RSItem edible() {
		RSItem[] is = inventory.getItems();
		for (RSItem i : is) {
			if (i.getComponent().getActions() == null || i.getComponent().getActions()[0] == null) {
				continue;
			}
			if (i.getComponent().getActions()[0].contains("Eat")) {
				return i;
			}
		}
		return null;
	}

	public void turnTo(int degrees, RSNPC n) {
		char left = 37;
		char right = 39;
		char whichDir = left;
		int start = camera.getAngle();
		if (start < 180) {
			start += 360;
		}
		if (degrees < 180) {
			degrees += 360;
		}
		if (degrees > start) {
			if (degrees - 180 < start) {
				whichDir = right;
			}
		} else if (start > degrees) {
			if (start - 180 >= degrees) {
				whichDir = right;
			}
		}
		degrees %= 360;
		keyboard.pressKey(whichDir);
		int timeWaited = 0;
		while (!n.isOnScreen() && camera.getAngle() > degrees + 10 || !n.isOnScreen() && camera.getAngle() < degrees - 10) {
			sleep(10);
			timeWaited += 10;
			if (timeWaited > 500) {
				int time = timeWaited - 500;
				if (time == 0) {
					keyboard.pressKey(whichDir);
				} else if (time % 40 == 0) {
					keyboard.pressKey(whichDir);
				}
			}
		}
		keyboard.releaseKey(whichDir);
	}

	private class FighterGUI extends JFrame implements ListSelectionListener, ActionListener {

		private static final long serialVersionUID = 1L;
		private DefaultListModel model;
		private DefaultListModel model1;
		private DefaultListModel model2;
		private JPanel contentPane;
		private JTextField txtItemId;
		private JList list;
		private JList list_1;
		private JList list_2;
		private JButton btnStartScript;
		private JButton update;
		private JButton btnAdd;
		private JButton btnLoad;
		private JButton btnSave;
		private JScrollPane scrollPane;
		private JScrollPane scrollPane_1;
		private JScrollPane scrollPane_2;
		private JLabel AddItem;

		private FighterGUI() {
			setTitle("Fighter Settings 1.05");
			setBounds(100, 100, 450, 450);
			contentPane = new JPanel();
			contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
			setContentPane(contentPane);
			contentPane.setLayout(null);
			{
				JTabbedPane tabbedPane = new JTabbedPane(SwingConstants.TOP);
				tabbedPane.setBounds(10, 10, 414, 354);
				contentPane.add(tabbedPane);
				{
					JPanel panel = new JPanel();
					tabbedPane.addTab("Fighting", null, panel, null);
					panel.setLayout(null);

					model = new DefaultListModel();

					scrollPane = new JScrollPane();
					scrollPane.setBounds(10, 160, 390, 125);
					panel.add(scrollPane);
					list_1 = new JList(model);
					scrollPane.setViewportView(list_1);
					list_1.addListSelectionListener(this);
					list_1.setBorder(new LineBorder(new Color(0, 0, 0)));

					model1 = new DefaultListModel();

					scrollPane_1 = new JScrollPane();
					scrollPane_1.setBounds(10, 20, 390, 125);
					panel.add(scrollPane_1);
					list_2 = new JList(model1);
					scrollPane_1.setViewportView(list_2);
					list_2.addListSelectionListener(this);
					list_2.setBorder(new LineBorder(new Color(0, 0, 0)));

					btnStartScript = new JButton("Start Script");
					btnStartScript.addActionListener(this);
					btnStartScript.setBounds(10, 375, 200, 23);
					contentPane.add(btnStartScript);

					update = new JButton("Refresh GUI");
					update.addActionListener(this);
					update.setBounds(219, 375, 200, 23);
					contentPane.add(update);

				}
				{
					JPanel panel = new JPanel();
					tabbedPane.addTab("Items", null, panel, null);
					panel.setLayout(null);

					scrollPane_2 = new JScrollPane();
					model2 = new DefaultListModel();
					list = new JList(model2);
					list.setBorder(new LineBorder(new Color(0, 0, 0)));
					scrollPane_2.setBounds(10, 20, 390, 125);
					panel.add(scrollPane_2);
					scrollPane_2.setViewportView(list);
					list.addListSelectionListener(this);

					AddItem = new JLabel();
					AddItem.setBounds(100, 150, 100, 12);
					AddItem.setText("Item ID");
					panel.add(AddItem);

					btnAdd = new JButton("Add To List");
					btnAdd.addActionListener(this);
					btnAdd.setBounds(10, 190, 390, 23);
					panel.add(btnAdd);

					btnLoad = new JButton("Load List");
					btnLoad.addActionListener(this);
					btnLoad.setBounds(10, 215, 390, 23);
					panel.add(btnLoad);

					btnSave = new JButton("Save List");
					btnSave.addActionListener(this);
					btnSave.setBounds(10, 240, 390, 23);
					panel.add(btnSave);

					txtItemId = new JTextField();
					txtItemId.setBounds(10, 163, 190, 19);
					panel.add(txtItemId);
					txtItemId.setColumns(20);
				}
			}
			setVisible(true);
		}

		public void actionPerformed(final ActionEvent arg0) {
			if (arg0.getSource() == btnAdd) {
				if (!model2.contains(Integer.parseInt(txtItemId.getText()))) {
					try {
						Integer i = Integer.parseInt(txtItemId.getText());
						model2.addElement(i);
					} catch (NumberFormatException e) {
						log.warning("ID Should Contain Numbers Only...");
					}
				}
				txtItemId.setText("");
			}
			if (arg0.getSource() == btnLoad) {
				try {
					JFileChooser fc = new JFileChooser();
					if (fc.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
						BufferedReader in = new BufferedReader(new FileReader(fc.getSelectedFile().getPath()));
						String str;
						while ((str = in.readLine()) != null) {
							if (!model2.contains(str)) {
								try {
									model2.addElement(Integer.parseInt(str));
								} catch (NumberFormatException e) {
									log.warning("List Should Contain Only Numeric IDs... " + str);
								}
							}
						}
						in.close();
					}
				} catch (IOException e) {
				}
			}
			if (arg0.getSource() == btnSave) {
				try {
					JFileChooser fc = new JFileChooser();
					if (fc.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
						BufferedWriter in = new BufferedWriter(new FileWriter(fc.getSelectedFile().getPath()));
						String string = model2.toString();
						String fixed = string.replaceAll(", ", "\r\n");
						in.write(fixed);
						in.close();
					}
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
			if (arg0.getSource() == btnStartScript) {
				npcID = new int[model.size()];
				for (int i = 0; i < model.getSize(); i++) {
					try {
						npcID[i] = Integer.parseInt(((String) model.get(i)).split(" -")[0]);
					} catch (NumberFormatException e) {
						log("Uh-oh An ID Wasn't Just Numbers!");
					}
				}
				lootID = new int[model2.size()];
				for (int i = 0; i < model2.getSize(); i++) {
					try {
						lootID[i] = (Integer) (model2.get(i));
					} catch (NumberFormatException e) {
						log("Uh-oh An ID Wasn't Just Numbers!");
					}
				}
				gui.dispose();
			}
			if (arg0.getSource() == update) {
				final RSNPC[] validNPCs = npcs.getAll();
				for (final RSNPC element : validNPCs) {
					final RSNPC Monster = element;
					if (!model1.contains(Monster.getID() + " -" + " Name: " + Monster.getName() + " Level: " + Monster.getLevel()) && !model.contains(Monster.getID() + " -" + " Name: " + Monster.getName() + " Level: " + Monster.getLevel()) && (Monster.getLevel() != 0)) {
						model1.add(model1.getSize(), Monster.getID() + " -" + " Name: " + Monster.getName() + " Level: " + Monster.getLevel());
					}
				}
			}
		}

		public void valueChanged(ListSelectionEvent arg0) {
			if (arg0.getSource() == list) {
				if (list.getSelectedValue() == null) {
					return;
				}
				model2.remove(list.getSelectedIndex());
			}
			if (arg0.getSource() == list_2) {
				String text = (String) list_2.getSelectedValue();
				if ((text == null) || text.isEmpty()) {
					return;
				}
				model.addElement(text);
				model1.remove(list_2.getSelectedIndex());
			}
			if (arg0.getSource() == list_1) {
				String text = (String) list_1.getSelectedValue();
				if ((text == null) || text.isEmpty()) {
					return;
				}
				model1.addElement(text);
				model.remove(list_1.getSelectedIndex());
			}
		}
	}
}