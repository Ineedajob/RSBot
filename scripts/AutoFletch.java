import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.util.HashMap;

import org.rsbot.event.events.MessageEvent;
import org.rsbot.event.listeners.MessageListener;
import org.rsbot.event.listeners.PaintListener;
import org.rsbot.script.Script;
import org.rsbot.script.ScriptManifest;
import org.rsbot.script.methods.Skills;
import org.rsbot.script.util.Timer;
import org.rsbot.script.wrappers.RSItem;

@ScriptManifest(authors = { "LastCoder" }, keywords = " Auto", name = "AutoFletch", version = 1.0, description = "Start, all options are in GUI.")
public class AutoFletch extends Script implements MessageListener,
		PaintListener {

	private static final Log[] LOGS = new Log[] {
			new Log("Normal Log", 0, 1511), new Log("Oak Log", 0, 1521),
			new Log("Willow Log", 0, 1519), new Log("Maple Log", 0, 1517),
			new Log("Yew Log", 0, 1515), new Log("Magic Log", 0, 1513) };
	private static final int KNIFE_ID = 946;
	private static final Color COLOR_1 = new Color(0, 0, 0, 155);
	private static final Color COLOR_2 = new Color(0, 0, 0);
	private static final Color COLOR_3 = new Color(255, 255, 255);
	private static final BasicStroke STROKE = new BasicStroke(1);
	private static final Font FONT_1 = new Font("Arial", 0, 17);
	private static final Font FONT_2 = new Font("Arial", 0, 9);

	private static boolean guiOn = false;

	private static Log single_log;

	private long activityTime;
	private long startExp;
	private long expGained;
	private long startTime;

	private int expHour;

	private enum state {
		BANK, CUT, INTERFACE, SLEEP
	}

	@Override
	public boolean onStart() {
		new Gui().setVisible(true);
		while (guiOn) {
			sleep(20);
		}
		startExp = (long) skills.getCurrentExp(Skills.FLETCHING);
		startTime = System.currentTimeMillis();
		return game.isLoggedIn();
	}

	private boolean busy() {
		if (System.currentTimeMillis() - activityTime < 8000) {
			return true;
		}
		return false;
	}

	private void antiBan() {
		int random = random(1, 5);
		switch (random) {
		case 1:
			if (random(1, 25) != 1)
				return;
			mouse.move(random(10, 750), random(10, 495));
			return;
		case 2:
			if (random(1, 6) != 1)
				return;
			int angle = camera.getAngle() + random(-45, 45);
			if (angle < 0) {
				angle = random(0, 10);
			}
			if (angle > 359) {
				angle = random(0, 10);
			}
			char whichDir = 37; // left
			if (random(0, 100) < 50)
				whichDir = 39; // right
			keyboard.pressKey(whichDir);
			sleep(random(100, 500));
			keyboard.releaseKey(whichDir);
			return;
		case 3:
			if (random(1, 15) != 1)
				return;
			mouse.moveSlightly();
			return;
		default:
			return;
		}
	}

	private state getState() {
		if (interfaces.get(905).isValid()) {
			return state.INTERFACE;
		} else if (inventory.contains(single_log.id)) {
			if (!busy()) {
				return state.CUT;
			} else {
				return state.SLEEP;
			}
		} else {
			return state.BANK;
		}
	}

	@Override
	public int loop() {
		switch (getState()) {
		case BANK:
			if (!bank.isOpen()) {
				bank.open();
				for (int i = 0; i < 100 && !bank.isOpen(); i++)
					sleep(10);
				sleep(random(800, 1200));
			} else {
				if (!inventory.contains(single_log.id)) {
					if (inventory.getCount() > 2) {
						bank.depositAllExcept(KNIFE_ID);
						for (int i = 0; i < 100 && inventory.getCount() > 2; i++)
							sleep(20);
					}
					if (bank.getItem(single_log.id) == null)
						return -1;
					bank.withdraw(single_log.id, 0);
					for (int i = 0; i < 100
							&& !inventory.contains(single_log.id); i++)
						sleep(10);
					bank.close();
					for (int i = 0; i < 100 && bank.isOpen(); i++)
						sleep(10);
				}
			}
			break;
		case CUT:
			if (!inventory.isItemSelected()) {
				RSItem item = inventory.getItem(KNIFE_ID);
				if (item != null) {
					item.doAction("Use");
					for (int i = 0; i < 100 && !inventory.isItemSelected(); i++)
						sleep(10);
				}
			} else if (inventory.isItemSelected()
					&& inventory.getSelectedItem().getID() != KNIFE_ID) {
				inventory.clickSelectedItem();
				for (int i = 0; i < 100 && inventory.isItemSelected(); i++)
					sleep(10);
			} else {
				RSItem Log = inventory.getItem(single_log.id);
				if (Log != null) {
					Log.doAction("Use");
				}
			}
			break;
		case INTERFACE:
			interfaces.get(905).getComponent(15).doAction("All");
			activityTime = System.currentTimeMillis();
			break;
		case SLEEP:
			sleep(200);
			antiBan();
			break;
		}
		return random(600, 1200);
	}

	static class Gui extends javax.swing.JFrame {

		/**
		 * GUI
		 */
		private static final long serialVersionUID = 1L;
		public HashMap<String, Log> hideOptMap = new HashMap<String, Log>();
		public String[] hideOpt = new String[LOGS.length];

		/** Creates new form Gui */
		public Gui() {
			initComponents();
		}

		/**
		 * This method is called from within the constructor to initialize the
		 * form. WARNING: Do NOT modify this code. The content of this method is
		 * always regenerated by the Form Editor.
		 */
		private void initComponents() {

			guiOn = true;
			for (int i = 0; i < LOGS.length; i++) {
				hideOpt[i] = LOGS[i].name;
				hideOptMap.put(hideOpt[i], LOGS[i]);
			}
			jLabel1 = new javax.swing.JLabel();
			jLabel3 = new javax.swing.JLabel();
			jButton1 = new javax.swing.JButton();
			jComboBox1 = new javax.swing.JComboBox();

			setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

			jLabel1.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N
			jLabel1.setText("AutoFletcher");

			jLabel3.setText("Which Log: (long bow only)");

			jButton1.setText("START");
			jButton1.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent evt) {
					jButton1ActionPerformed(evt);
				}
			});

			jComboBox1.setModel(new javax.swing.DefaultComboBoxModel(hideOpt));

			javax.swing.GroupLayout layout = new javax.swing.GroupLayout(
					getContentPane());
			getContentPane().setLayout(layout);
			layout.setHorizontalGroup(layout
					.createParallelGroup(
							javax.swing.GroupLayout.Alignment.LEADING)
					.addGroup(
							layout.createSequentialGroup()
									.addGroup(
											layout.createParallelGroup(
													javax.swing.GroupLayout.Alignment.LEADING)
													.addGroup(
															layout.createSequentialGroup()
																	.addGap(103,
																			103,
																			103)
																	.addComponent(
																			jLabel1))
													.addGroup(
															layout.createSequentialGroup()
																	.addContainerGap()
																	.addComponent(
																			jButton1,
																			javax.swing.GroupLayout.DEFAULT_SIZE,
																			275,
																			Short.MAX_VALUE))
													.addGroup(
															layout.createSequentialGroup()
																	.addContainerGap()
																	.addComponent(
																			jLabel3)
																	.addPreferredGap(
																			javax.swing.LayoutStyle.ComponentPlacement.RELATED)
																	.addComponent(
																			jComboBox1,
																			javax.swing.GroupLayout.PREFERRED_SIZE,
																			102,
																			javax.swing.GroupLayout.PREFERRED_SIZE)))
									.addContainerGap()));
			layout.setVerticalGroup(layout
					.createParallelGroup(
							javax.swing.GroupLayout.Alignment.LEADING)
					.addGroup(
							layout.createSequentialGroup()
									.addContainerGap()
									.addComponent(jLabel1)
									.addGap(27, 27, 27)
									.addGroup(
											layout.createParallelGroup(
													javax.swing.GroupLayout.Alignment.BASELINE)
													.addComponent(jLabel3)
													.addComponent(
															jComboBox1,
															javax.swing.GroupLayout.PREFERRED_SIZE,
															javax.swing.GroupLayout.DEFAULT_SIZE,
															javax.swing.GroupLayout.PREFERRED_SIZE))
									.addGap(37, 37, 37)
									.addComponent(
											jButton1,
											javax.swing.GroupLayout.PREFERRED_SIZE,
											59,
											javax.swing.GroupLayout.PREFERRED_SIZE)
									.addContainerGap(
											javax.swing.GroupLayout.DEFAULT_SIZE,
											Short.MAX_VALUE)));

			pack();
		}// </editor-fold>

		private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {
			setVisible(false);
			single_log = hideOptMap
					.get(jComboBox1.getSelectedItem().toString());
			guiOn = false;
		}

		// Variables declaration - do not modify
		private javax.swing.JButton jButton1;
		private javax.swing.JComboBox jComboBox1;
		private javax.swing.JLabel jLabel1;
		private javax.swing.JLabel jLabel3;
		// End of variables declaration

	}

	public void messageReceived(MessageEvent e) {
		String msg = e.getMessage();
		if (msg.contains("You")) {
			activityTime = System.currentTimeMillis();
		}

	}

	public void onRepaint(Graphics g1) {
		Graphics2D g = (Graphics2D) g1;
		long millis = System.currentTimeMillis() - startTime;
		String time = Timer.format(millis);
		if ((skills.getCurrentExp(Skills.FLETCHING) - startExp) > 0
				&& startExp > 0) {
			expGained = skills.getCurrentExp(Skills.FLETCHING) - startExp;
		}
		if (expGained > 0 && millis > 0) {
			expHour = (int) (3600 * expGained / millis);
		}
		g.setColor(COLOR_1);
		g.fillRect(14, 350, 474, 99);
		g.setColor(COLOR_2);
		g.setStroke(STROKE);
		g.drawRect(14, 350, 474, 99);
		g.setFont(FONT_1);
		g.setColor(COLOR_3);
		g.drawString("AutoFletch", 209, 374);
		g.setFont(FONT_2);
		g.drawString("EXP/Hr: " + expHour + "k", 18, 390);
		g.drawString("EXP Gained: " + expGained, 18, 400);
		g.drawString("Time Ran: " + time, 182, 390);
		g.drawString("Status: " + getState().toString(), 182, 400);

	}

	static class Log {
		public String name;
		public int req_lvl;
		public int id;

		public Log(String name, int req_lvl, int id) {
			this.name = name;
			this.req_lvl = req_lvl;
			this.id = id;
		}
	}

}
