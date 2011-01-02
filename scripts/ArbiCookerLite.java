import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;

import org.rsbot.event.events.MessageEvent;
import org.rsbot.event.listeners.MessageListener;
import org.rsbot.event.listeners.PaintListener;
import org.rsbot.script.Script;
import org.rsbot.script.ScriptManifest;
import org.rsbot.script.methods.Skills;
import org.rsbot.script.wrappers.RSItem;
import org.rsbot.script.util.Timer;
import org.rsbot.script.wrappers.RSNPC;
import org.rsbot.script.wrappers.RSObject;

@ScriptManifest(authors = { "LastCoder" }, keywords = "Cooker, Auto", name = "ArbiCookerLite", version = 2.0, description = "Start, all options are in GUI. Rogue Den only")
public class ArbiCookerLite extends Script implements MessageListener,
		PaintListener {

	private static final int FIRE_ID = 2732;
	private static final int BANKER_ID = 2271;
	private static final Color COLOR_1 = new Color(0, 0, 0, 155);
	private static final Color COLOR_2 = new Color(0, 0, 0);
	private static final Color COLOR_3 = new Color(255, 255, 255);
    private static final BasicStroke STROKE = new BasicStroke(1);
	private static final Font FONT_1 = new Font("Arial", 0, 17);
	private static final Font FONT_2 = new Font("Arial", 0, 9);
	
	private long startExp;
	private long expGained;
	private int expHour;
	private int foodCooked;
	private static int food_id;
	private static boolean guiOn = false;
	private long startTime;

	private static enum state {
		USE_ITEM, SLEEP, BANK, INTERFACE
	}

	private long activityTime;

	private boolean busy() {
		if (System.currentTimeMillis() - activityTime < 8000) {
			return true;
		}
		return false;
	}

	private state getState() {
		if(interfaces.get(905).isValid()) {
			return state.INTERFACE;
		} else if (inventory.contains(food_id)) {
			if (!busy()) {
				return state.USE_ITEM;
			} else {
				return state.SLEEP;
			}
		} else {
			return state.BANK;
		}

	}

	@Override
	public boolean onStart() {
		new Gui().setVisible(true);
		while(guiOn) {
			sleep(20);
		}
		startExp = (long) skills.getCurrentExp(Skills.COOKING);
		startTime = System.currentTimeMillis();
		return game.isLoggedIn();
	}

	@Override
	public int loop() {
		switch (getState()) {
		case INTERFACE:
			interfaces.get(905).getComponent(14).doAction("All");
			break;
		case SLEEP:
			sleep(20);
			break;
		case USE_ITEM:
			RSObject fire = objects.getNearest(FIRE_ID);
			if(bank.isOpen()) {
				bank.close();
			} else {
				if (fire != null) {
					if (!fire.isOnScreen()) {
						camera.turnToObject(fire);
						for (int i = 0; i < 100 && !fire.isOnScreen(); i++)
							sleep(20);
					} else {
						if (!inventory.isItemSelected()) {
							RSItem item = inventory.getItem(food_id);
							if (item != null) {
								item.doAction("Use");
								for (int i = 0; i < 100
										&& !inventory.isItemSelected(); i++)
									sleep(20);
							}
						} else {
							fire.doAction("fire");
						}
					}
				}
			}
			break;
		case BANK:
			if (!bank.isOpen()) {
				RSNPC banker = npcs.getNearest(BANKER_ID);
				if(banker != null) {
					if (!banker.isOnScreen()) {
						camera.turnToCharacter(banker);
						for (int i = 0; i < 100 && !banker.isOnScreen(); i++)
							sleep(20);
					} else {
						banker.doAction("Bank");
					}
				}
				for (int i = 0; i < 100 && !bank.isOpen(); i++)
					sleep(20);
			} else {
				if (!inventory.contains(food_id)) {
					if (inventory.getCount() > 1) {
						bank.depositAll();
					}
					bank.withdraw(food_id, 0);
					for (int i = 0; i < 100 && !inventory.contains(food_id); i++)
						sleep(20);
				} else {
					bank.close();
				}
			}
			break;
		}
		return random(600,1200);
	}

	
	public void messageReceived(MessageEvent e) {
		// TODO Auto-generated method stub
		String msg = e.getMessage();
		if (msg.contains("You")) {
			activityTime = System.currentTimeMillis();
			foodCooked++;
		}

	}


	public void onRepaint(Graphics g1) {
		Graphics2D g = (Graphics2D) g1;
		long millis = System.currentTimeMillis() - startTime;
		String time = Timer.format(millis);
		if ((skills.getCurrentExp(Skills.COOKING) - startExp) > 0
				&& startExp > 0) {
			expGained = skills.getCurrentExp(Skills.COOKING) - startExp;
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
		g.drawString("AutoCooker", 209, 374);
		g.setFont(FONT_2);
		g.drawString("EXP/Hr: " + expHour, 18, 390);
		g.drawString("EXP Gained: " + expGained, 18, 400);
		g.drawString("Time Ran: " + time,
				182, 390);
		g.drawString("Status: " + getState().toString(), 182, 400);
		g.drawString("Food Cooked: " + foodCooked, 395, 390);
	}
	
	
	static class Gui extends javax.swing.JFrame {

	    /** Creates new form Gui */
	    public Gui() {
	        initComponents();
	    }

	    /** This method is called from within the constructor to
	     * initialize the form.
	     * WARNING: Do NOT modify this code. The content of this method is
	     * always regenerated by the Form Editor.
	     */
	    @SuppressWarnings("unchecked")
	    // <editor-fold defaultstate="collapsed" desc="Generated Code">
	    private void initComponents() {
	    	guiOn = true;

	        jLabel1 = new javax.swing.JLabel();
	        jLabel2 = new javax.swing.JLabel();
	        jLabel3 = new javax.swing.JLabel();
	        jTextField1 = new javax.swing.JTextField();
	        jButton1 = new javax.swing.JButton();

	        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

	        jLabel1.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N
	        jLabel1.setText("AutoCooker");

	        jLabel2.setText("Only supports Rogue Den currently");

	        jLabel3.setText("Enter ID:");

	        jTextField1.setText("0000");

	        jButton1.setText("START");
	        jButton1.addActionListener(new java.awt.event.ActionListener() {
	            public void actionPerformed(java.awt.event.ActionEvent evt) {
	                jButton1ActionPerformed(evt);
	            }
	        });

	        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
	        getContentPane().setLayout(layout);
	        layout.setHorizontalGroup(
	            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
	            .addGroup(layout.createSequentialGroup()
	                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
	                    .addGroup(layout.createSequentialGroup()
	                        .addGap(103, 103, 103)
	                        .addComponent(jLabel1))
	                    .addGroup(layout.createSequentialGroup()
	                        .addContainerGap()
	                        .addComponent(jLabel2))
	                    .addGroup(layout.createSequentialGroup()
	                        .addContainerGap()
	                        .addComponent(jLabel3)
	                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
	                        .addComponent(jTextField1, javax.swing.GroupLayout.PREFERRED_SIZE, 104, javax.swing.GroupLayout.PREFERRED_SIZE))
	                    .addGroup(layout.createSequentialGroup()
	                        .addContainerGap()
	                        .addComponent(jButton1, javax.swing.GroupLayout.DEFAULT_SIZE, 275, Short.MAX_VALUE)))
	                .addContainerGap())
	        );
	        layout.setVerticalGroup(
	            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
	            .addGroup(layout.createSequentialGroup()
	                .addContainerGap()
	                .addComponent(jLabel1)
	                .addGap(18, 18, 18)
	                .addComponent(jLabel2)
	                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
	                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
	                    .addComponent(jLabel3)
	                    .addComponent(jTextField1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
	                .addGap(26, 26, 26)
	                .addComponent(jButton1, javax.swing.GroupLayout.PREFERRED_SIZE, 59, javax.swing.GroupLayout.PREFERRED_SIZE)
	                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
	        );

	        pack();
	    }// </editor-fold>

	    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {
	        // TODO add your handling code here:
	    	setVisible(false);
	    	food_id = Integer.parseInt(jTextField1.getText());
	    	guiOn = false;
	    }

	  
	    // Variables declaration - do not modify
	    private javax.swing.JButton jButton1;
	    private javax.swing.JLabel jLabel1;
	    private javax.swing.JLabel jLabel2;
	    private javax.swing.JLabel jLabel3;
	    private javax.swing.JTextField jTextField1;
	    // End of variables declaration

	}


}
