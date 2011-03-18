import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

import javax.imageio.ImageIO;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.WindowConstants;

import org.rsbot.event.events.MessageEvent;
import org.rsbot.event.listeners.MessageListener;
import org.rsbot.event.listeners.PaintListener;
import org.rsbot.script.Script;
import org.rsbot.script.ScriptManifest;
import org.rsbot.script.methods.Game;
import org.rsbot.script.methods.Skills;
import org.rsbot.script.wrappers.RSGroundItem;
import org.rsbot.script.wrappers.RSItem;
import org.rsbot.script.wrappers.RSModel;
import org.rsbot.script.wrappers.RSNPC;
import org.rsbot.script.wrappers.RSObject;
import org.rsbot.script.wrappers.RSTile;

@SuppressWarnings("deprecation")
@ScriptManifest(authors = { "Conderoga" }, keywords = "Woodcutting", name = "Conderoga's Chopper", version = 1.23, description = "Chops trees and stuff.")
public class CChop extends Script implements PaintListener, MessageListener,
		MouseListener {

	private boolean guiWait = true;
	private boolean guiExit = true;
	private String location;
	private RSTile tree1, tree2, tree3, tree4, tree5, tree6, tree7;
	private RSTile[] treesToBank;
	private RSTile[] bankToTrees;
	private boolean tree1Status, tree2Status, tree3Status, tree4Status,
			tree5Status, tree6Status, tree7Status;
	private long tree1TimeDead, tree2TimeDead, tree3TimeDead, tree4TimeDead,
			tree5TimeDead, tree6TimeDead, tree7TimeDead;
	private long tree1DeadTime, tree2DeadTime, tree3DeadTime, tree4DeadTime,
			tree5DeadTime, tree6DeadTime, tree7DeadTime;
	private long tree1TimeAlive, tree2TimeAlive, tree3TimeAlive,
			tree4TimeAlive, tree5TimeAlive, tree6TimeAlive, tree7TimeAlive;
	private long tree1AliveTime, tree2AliveTime, tree3AliveTime,
			tree4AliveTime, tree5AliveTime, tree6AliveTime, tree7AliveTime;
	private int nextTree = 0;
	private int bankerID;
	private int yewPrice = 0;
	private int magicPrice = 0;
	private int willowPrice = 0;
	private int maplePrice = 0;
	private int oakPrice = 0;

	public CChopGUI gui;
	private boolean nearest;
	private boolean takeBreaks;
	private boolean breakActive;
	private int breakLength;
	private long breakStart;
	private Point stump1, stump2, stump3, stump4, stump5, stump6, stump7;
	public int[] BNIDs = { 5070, 5071, 5072, 5073, 5074, 5075, 5076, 7413,
			11966 };
	public int[] hatchetIDs = { 1349, 1351, 1353, 1355, 1357, 1359, 1361, 6739,
			13470 };
	private int yewsCut = 0;
	private int magicsCut = 0;
	private int ivyCut = 0;
	private int willowsCut = 0;
	private int maplesCut = 0;
	private int oaksCut = 0;
	private int nests = 0;
	private boolean full = false;
	private int startLevel;
	private int startExp;
	private int expGained;
	private int lvlsGained;
	private long startTime, failSafeTimer;
	private double startTimeDbl;
	private int safety = 0;
	private String status;
	private Point p;
	private final String version = "v1.23";
	private String currentVersion = "";
	private String treeType = ".";
	private int treeID;
	private final ArrayList<Integer> ivyIDs = new ArrayList<Integer>();
	private final ArrayList<Integer> stumpIDs = new ArrayList<Integer>();
	private int stumpID;
	private RSTile bankLocation;
	private String chopType;
	private String command;
	private boolean showPaint = true;
	private boolean fancyPaint = true;
	private final Image img1 = getImage("http://i263.photobucket.com/albums/ii158/zpogo/Logo.png");
	private final Image img2 = getImage("http://i263.photobucket.com/albums/ii158/zpogo/Bar.png");
	private final Image img4 = getImage("http://i263.photobucket.com/albums/ii158/zpogo/EXPback-1copy.png");
	private Color mouseColor = new Color(51, 153, 0, 255);

	@Override
	public boolean onStart() {
		/*
		 * final int checkUpdate = JOptionPane.showConfirmDialog(null,
		 * "Would you like to check for a script update?"
		 * ,"Check for update?",JOptionPane.YES_NO_OPTION); if(checkUpdate==0) {
		 * if(!checkCurrentVersion()) { final int updateQ =
		 * JOptionPane.showConfirmDialog
		 * (null,"Your script is out of date. Would you like to update?"
		 * ,"Update?",JOptionPane.YES_NO_OPTION); if(updateQ==0){ final String
		 * notice = "Commencing Update!";
		 * JOptionPane.showMessageDialog(null,notice); update(); } } else
		 * log("Your script is up to date."); }
		 */
		startTime = System.currentTimeMillis();
		startTimeDbl = System.currentTimeMillis();
		// Credits to Zombieknight for this:-----------------
		final int welcome = JOptionPane
				.showConfirmDialog(
						null,
						"Before using my script, would you like to thank me\nby clicking some adverts?",
						"Welcome", JOptionPane.YES_NO_OPTION);
		if (welcome == 0) {
			final String message = "<html>Thank you for your support!<br /></html>";
			JOptionPane.showMessageDialog(null, message);
			openURL("http://adf.ly/317631/agsn");
		}
		// =------------------------------------------------------
		if (Integer.parseInt(account.getPin()) != -1)
			log("Your account has a pin and may not be able to bank.");
		gui = new CChopGUI();
		gui.setVisible(true);
		while (guiWait) {
			sleep(100);
		}
		return !guiExit;
	}

	public void pickUpNest() {
		RSGroundItem o = groundItems.getNearest(BNIDs);
		if (o != null && !inventory.isFull()) {
			status = "Nest!";
			sleep(random(600, 800));
			if (o.doAction("Take"))
				nests++;
			sleep(random(600, 800));
		}
	}

	public boolean chopCheck() {
		if (!treeType.equals("ivy")) {
			if (players.getMyPlayer().getAnimation() == 867
					|| players.getMyPlayer().getAnimation() == 2846
					|| players.getMyPlayer().getAnimation() == 10251
					|| players.getMyPlayer().getAnimation() == 875
					|| players.getMyPlayer().getAnimation() == 869) {
				status = "Chopping!";
				return true;
			} else
				status = "Not chopping.";
		} else {
			for (int i = 0; i < 5; i++) {
				if (players.getMyPlayer().getAnimation() == 870
						|| players.getMyPlayer().getAnimation() == 872
						|| players.getMyPlayer().getAnimation() == 10251) {
					status = "Chopping!";
					return true;
				}
				sleep(random(200, 300));
			}
			status = "Not chopping.";
		}
		return false;
	}

	public void updateStatus() {
		antiBan(random(1, 100));
		boolean temp = chopCheck();
		if (!temp && !tree7Status && !tree6Status && !tree5Status
				&& !tree4Status && !tree3Status && !tree2Status && !tree1Status) {
			status = "Waiting.";
		}
	}

	public void setUp() {
		status = "Starting up.";
		command = location + " - " + chopType;
		if (command.equals("Draynor - Oaks")) {
			treeType = "oaks";
			treeID = 1281;
			stumpID = 1356;
			bankerID = 494;
			tree1 = new RSTile(3102, 3242);
			tree2 = new RSTile(3107, 3248);
			tree3 = new RSTile(0, 0);
			tree4 = new RSTile(0, 0);
			tree5 = new RSTile(0, 0);
			tree6 = new RSTile(0, 0);
			tree7 = new RSTile(0, 0);

			treesToBank = new RSTile[3];
			bankToTrees = new RSTile[2];

			treesToBank[0] = new RSTile(3107, 3250);
			treesToBank[1] = new RSTile(3101, 3244);
			treesToBank[2] = new RSTile(3092, 3244);

			bankToTrees[0] = new RSTile(3092, 3244);
			bankToTrees[1] = new RSTile(3101, 3244);

		} else if (command.equals("Grand Exchange - Yews")) {
			treeType = "yews";
			treeID = 1309;
			stumpID = 7402;
			bankerID = 6533;
			tree1 = new RSTile(3205, 3504);
			tree2 = new RSTile(3210, 3504);
			tree3 = new RSTile(3222, 3503);
			tree4 = new RSTile(0, 0);
			tree5 = new RSTile(0, 0);
			tree6 = new RSTile(0, 0);
			tree7 = new RSTile(0, 0);

			treesToBank = new RSTile[6];
			bankToTrees = new RSTile[6];

			bankToTrees[0] = new RSTile(3167, 3489);
			bankToTrees[1] = new RSTile(3173, 3491);
			bankToTrees[2] = new RSTile(3183, 3490);
			bankToTrees[3] = new RSTile(3195, 3494);
			bankToTrees[4] = new RSTile(3204, 3502);
			bankToTrees[5] = new RSTile(3208, 3502);

			treesToBank[0] = new RSTile(3208, 3502);
			treesToBank[1] = new RSTile(3204, 3502);
			treesToBank[2] = new RSTile(3195, 3494);
			treesToBank[3] = new RSTile(3183, 3490);
			treesToBank[4] = new RSTile(3173, 3491);
			treesToBank[5] = new RSTile(3167, 3489);
		} else if (command.equals("Edgeville - Yews")) {
			treeType = "yews";
			treeID = 1309;
			stumpID = 7402;
			bankerID = 5912;
			tree1 = new RSTile(3086, 3481);
			tree2 = new RSTile(3086, 3469);
			tree3 = new RSTile(0, 0);
			tree4 = new RSTile(0, 0);
			tree5 = new RSTile(0, 0);
			tree6 = new RSTile(0, 0);
			tree7 = new RSTile(0, 0);

			treesToBank = new RSTile[5];
			bankToTrees = new RSTile[5];

			bankToTrees[0] = new RSTile(3093, 3490);
			bankToTrees[1] = new RSTile(3088, 3487);
			bankToTrees[2] = new RSTile(3093, 3482);
			bankToTrees[3] = new RSTile(3094, 3476);
			bankToTrees[4] = new RSTile(3087, 3475);

			treesToBank[0] = new RSTile(3087, 3475);
			treesToBank[1] = new RSTile(3094, 3476);
			treesToBank[2] = new RSTile(3093, 3482);
			treesToBank[3] = new RSTile(3088, 3487);
			treesToBank[4] = new RSTile(3093, 3490);
		} else if (command.equals("Rimmington - Yews")) {
			treeType = "yews";
			treeID = 1309;
			stumpID = 7402;
			bankerID = 6200;
			tree1 = new RSTile(2941, 3233);
			tree2 = new RSTile(2934, 3234);
			tree3 = new RSTile(2936, 3230);
			tree4 = new RSTile(2935, 3226);
			tree5 = new RSTile(0, 0);
			tree6 = new RSTile(0, 0);
			tree7 = new RSTile(0, 0);

			treesToBank = new RSTile[13];
			bankToTrees = new RSTile[13];

			bankToTrees[0] = new RSTile(3047, 3236);
			bankToTrees[1] = new RSTile(3037, 3235);
			bankToTrees[2] = new RSTile(3027, 3241);
			bankToTrees[3] = new RSTile(3016, 3242);
			bankToTrees[4] = new RSTile(3004, 3238);
			bankToTrees[5] = new RSTile(2993, 3235);
			bankToTrees[6] = new RSTile(2988, 3225);
			bankToTrees[7] = new RSTile(2979, 3223);
			bankToTrees[8] = new RSTile(2969, 3223);
			bankToTrees[9] = new RSTile(2959, 3223);
			bankToTrees[10] = new RSTile(2950, 3228);
			bankToTrees[11] = new RSTile(2942, 3230);
			bankToTrees[12] = new RSTile(2938, 3231);

			treesToBank = walking.reversePath(bankToTrees);

		} else if (command.equals("Catherby - Yews")) {
			treeType = "yews";
			treeID = 1309;
			stumpID = 7402;
			if (random(1, 2) == 1)
				bankerID = 494;
			else
				bankerID = 495;

			tree1 = new RSTile(2766, 3428);
			tree2 = new RSTile(2760, 3428);
			tree3 = new RSTile(2761, 3432);
			tree4 = new RSTile(2771, 3438);
			tree5 = new RSTile(2756, 3431);
			tree6 = new RSTile(2758, 3434);
			tree7 = new RSTile(2755, 3434);

			treesToBank = new RSTile[5];
			bankToTrees = new RSTile[5];

			bankToTrees[0] = new RSTile(2809, 3440);
			bankToTrees[1] = new RSTile(2801, 3434);
			bankToTrees[2] = new RSTile(2789, 3436);
			bankToTrees[3] = new RSTile(2777, 3436);
			bankToTrees[4] = new RSTile(2767, 3431);

			treesToBank[0] = new RSTile(2767, 3431);
			treesToBank[1] = new RSTile(2777, 3436);
			treesToBank[2] = new RSTile(2789, 3436);
			treesToBank[3] = new RSTile(2801, 3434);
			treesToBank[4] = new RSTile(2809, 3440);
		} else if (command.equals("Seer's Village - Yews")) {
			treeType = "yews";
			treeID = 1309;
			stumpID = 7402;
			bankerID = 495;
			tree1 = new RSTile(2715, 3460);
			tree2 = new RSTile(2706, 3460);
			tree3 = new RSTile(2706, 3465);
			tree4 = new RSTile(0, 0);
			tree5 = new RSTile(0, 0);
			tree6 = new RSTile(0, 0);
			tree7 = new RSTile(0, 0);

			treesToBank = new RSTile[4];
			bankToTrees = new RSTile[4];

			bankToTrees[0] = new RSTile(2726, 3493);
			bankToTrees[1] = new RSTile(2724, 3483);
			bankToTrees[2] = new RSTile(2723, 3474);
			bankToTrees[3] = new RSTile(2714, 3462);

			treesToBank[0] = new RSTile(2714, 3462);
			treesToBank[1] = new RSTile(2723, 3474);
			treesToBank[2] = new RSTile(2724, 3483);
			treesToBank[3] = new RSTile(2726, 3493);
		} else if (command.equals("South Falador - Yews")) {
			treeType = "yews";
			treeID = 1309;
			stumpID = 7402;
			bankerID = 6200;
			tree1 = new RSTile(2997, 3312);
			tree2 = new RSTile(3020, 3316);
			tree3 = new RSTile(3042, 3320);
			tree4 = new RSTile(0, 0);
			tree5 = new RSTile(0, 0);
			tree6 = new RSTile(0, 0);
			tree7 = new RSTile(0, 0);

			treesToBank = new RSTile[9];
			bankToTrees = new RSTile[6];

			bankToTrees[0] = new RSTile(3012, 3356);
			bankToTrees[1] = new RSTile(3006, 3357);
			bankToTrees[2] = new RSTile(3006, 3346);
			bankToTrees[3] = new RSTile(3006, 3339);
			bankToTrees[4] = new RSTile(3006, 3331);
			bankToTrees[5] = new RSTile(3007, 3322);

			treesToBank[0] = new RSTile(3040, 3320);
			treesToBank[1] = new RSTile(3028, 3321);
			treesToBank[2] = new RSTile(3016, 3320);
			treesToBank[3] = new RSTile(3007, 3322);
			treesToBank[4] = new RSTile(3006, 3331);
			treesToBank[5] = new RSTile(3006, 3339);
			treesToBank[6] = new RSTile(3006, 3346);
			treesToBank[7] = new RSTile(3006, 3357);
			treesToBank[8] = new RSTile(3012, 3356);
		} else if (command.equals("Draynor - Yews")) {
			treeType = "yews";
			treeID = 1309;
			stumpID = 7402;
			bankerID = 494;
			tree1 = new RSTile(3147, 3255);
			tree2 = new RSTile(3152, 3231);
			tree3 = new RSTile(3166, 3220);
			tree4 = new RSTile(3185, 3227);
			tree5 = new RSTile(0, 0);
			tree6 = new RSTile(0, 0);
			tree7 = new RSTile(0, 0);

			treesToBank = new RSTile[19];
			bankToTrees = new RSTile[7];

			treesToBank[0] = new RSTile(3185, 3227);
			treesToBank[1] = new RSTile(3177, 3226);
			treesToBank[2] = new RSTile(3172, 3226);
			treesToBank[3] = new RSTile(3166, 3222);
			treesToBank[4] = new RSTile(3161, 3227);
			treesToBank[5] = new RSTile(3158, 3232);
			treesToBank[6] = new RSTile(3153, 3239);
			treesToBank[7] = new RSTile(3152, 3245);
			treesToBank[8] = new RSTile(3147, 3252);
			treesToBank[9] = new RSTile(3146, 3244);
			treesToBank[10] = new RSTile(3147, 3235);
			treesToBank[11] = new RSTile(3145, 3230);
			treesToBank[12] = new RSTile(3137, 3228);
			treesToBank[13] = new RSTile(3126, 3226);
			treesToBank[14] = new RSTile(3119, 3226);
			treesToBank[15] = new RSTile(3110, 3229);
			treesToBank[16] = new RSTile(3105, 3235);
			treesToBank[17] = new RSTile(3101, 3242);
			treesToBank[18] = new RSTile(3092, 3244);

			bankToTrees[0] = new RSTile(3092, 3244);
			bankToTrees[1] = new RSTile(3104, 3238);
			bankToTrees[2] = new RSTile(3114, 3229);
			bankToTrees[3] = new RSTile(3126, 3225);
			bankToTrees[4] = new RSTile(3137, 3226);
			bankToTrees[5] = new RSTile(3144, 3229);
			bankToTrees[6] = new RSTile(3150, 3231);
		} else if (command.equals("Tree Gnome - Yews")) {
			treeType = "yews";
			treeID = 1309;
			stumpID = 7402;
			bankerID = 166;
			tree1 = new RSTile(2439, 3436);
			tree2 = new RSTile(2433, 3441);
			tree3 = new RSTile(2433, 3426);
			tree4 = new RSTile(0, 0);
			tree5 = new RSTile(0, 0);
			tree6 = new RSTile(0, 0);
			tree7 = new RSTile(0, 0);

			treesToBank = new RSTile[2];
			bankToTrees = new RSTile[2];

			treesToBank[0] = new RSTile(2442, 3438);
			treesToBank[1] = new RSTile(2445, 3427);

			bankToTrees[0] = new RSTile(2445, 3425);
			bankToTrees[1] = new RSTile(2440, 3434);

		} else if (command.equals("Seer's Village - Maples")) {
			treeType = "maples";
			treeID = 1307;
			stumpID = 7400;
			bankerID = 495;
			tree1 = new RSTile(2722, 3502);
			tree2 = new RSTile(2728, 3502);
			tree3 = new RSTile(2731, 3501);
			tree4 = new RSTile(2733, 3500);
			tree5 = new RSTile(0, 0);
			tree6 = new RSTile(0, 0);
			tree7 = new RSTile(0, 0);

			treesToBank = new RSTile[2];
			bankToTrees = new RSTile[2];

			bankToTrees[0] = new RSTile(2726, 3493);
			bankToTrees[1] = new RSTile(2729, 3501);

			treesToBank[0] = new RSTile(2729, 3501);
			treesToBank[1] = new RSTile(2726, 3493);

		} else if (command.equals("Seer's Village - Magics")) {
			treeType = "magics";
			treeID = 1306;
			stumpID = 7401;
			bankerID = 495;
			tree1 = new RSTile(2697, 3424);
			tree2 = new RSTile(2692, 3425);
			tree3 = new RSTile(2691, 3428);
			tree4 = new RSTile(0, 0);
			tree5 = new RSTile(0, 0);
			tree6 = new RSTile(0, 0);
			tree7 = new RSTile(0, 0);

			treesToBank = new RSTile[9];
			bankToTrees = new RSTile[9];

			bankToTrees[0] = new RSTile(2726, 3493);
			bankToTrees[1] = new RSTile(2725, 3487);
			bankToTrees[2] = new RSTile(2725, 3477);
			bankToTrees[3] = new RSTile(2723, 3466);
			bankToTrees[4] = new RSTile(2724, 3456);
			bankToTrees[5] = new RSTile(2713, 3449);
			bankToTrees[6] = new RSTile(2707, 3439);
			bankToTrees[7] = new RSTile(2703, 3429);
			bankToTrees[8] = new RSTile(2694, 3424);

			treesToBank[0] = new RSTile(2694, 3424);
			treesToBank[1] = new RSTile(2703, 3429);
			treesToBank[2] = new RSTile(2707, 3439);
			treesToBank[3] = new RSTile(2713, 3449);
			treesToBank[4] = new RSTile(2724, 3456);
			treesToBank[5] = new RSTile(2723, 3466);
			treesToBank[6] = new RSTile(2725, 3477);
			treesToBank[7] = new RSTile(2725, 3487);
			treesToBank[8] = new RSTile(2726, 3493);
		} else if (command.equals("Sorcerer's Tower - Magics")) {
			treeType = "magics";
			treeID = 1306;
			stumpID = 7401;
			bankerID = 495;
			tree1 = new RSTile(2705, 3397);
			tree2 = new RSTile(2705, 3399);
			tree3 = new RSTile(2699, 3399);
			tree4 = new RSTile(2699, 3397);
			tree5 = new RSTile(0, 0);
			tree6 = new RSTile(0, 0);
			tree7 = new RSTile(0, 0);

			treesToBank = new RSTile[12];
			bankToTrees = new RSTile[12];

			bankToTrees[0] = new RSTile(2726, 3493);
			bankToTrees[1] = new RSTile(2725, 3487);
			bankToTrees[2] = new RSTile(2725, 3477);
			bankToTrees[3] = new RSTile(2723, 3466);
			bankToTrees[4] = new RSTile(2724, 3456);
			bankToTrees[5] = new RSTile(2713, 3449);
			bankToTrees[6] = new RSTile(2707, 3439);
			bankToTrees[7] = new RSTile(2703, 3429);
			bankToTrees[8] = new RSTile(2709, 3418);
			bankToTrees[9] = new RSTile(2715, 3406);
			bankToTrees[10] = new RSTile(2714, 3396);
			bankToTrees[11] = new RSTile(2701, 3398);

			treesToBank[0] = new RSTile(2701, 3398);
			treesToBank[1] = new RSTile(2714, 3396);
			treesToBank[2] = new RSTile(2715, 3406);
			treesToBank[3] = new RSTile(2709, 3418);
			treesToBank[4] = new RSTile(2703, 3429);
			treesToBank[5] = new RSTile(2707, 3439);
			treesToBank[6] = new RSTile(2713, 3449);
			treesToBank[7] = new RSTile(2724, 3456);
			treesToBank[8] = new RSTile(2723, 3466);
			treesToBank[9] = new RSTile(2725, 3477);
			treesToBank[10] = new RSTile(2725, 3487);
			treesToBank[11] = new RSTile(2726, 3493);
		} else if (command.equals("Mage Training Area - Magics")) {
			treeType = "magics";
			treeID = 1306;
			stumpID = 7401;
			bankLocation = new RSTile(3381, 3269);

			tree1 = new RSTile(3357, 3312);
			tree2 = new RSTile(3369, 3312);
			tree3 = new RSTile(0, 0);
			tree4 = new RSTile(0, 0);
			tree5 = new RSTile(0, 0);
			tree6 = new RSTile(0, 0);
			tree7 = new RSTile(0, 0);

			treesToBank = new RSTile[7];
			bankToTrees = new RSTile[7];

			bankToTrees[0] = new RSTile(3381, 3268);
			bankToTrees[1] = new RSTile(3370, 3266);
			bankToTrees[2] = new RSTile(3358, 3265);
			bankToTrees[3] = new RSTile(3349, 3274);
			bankToTrees[4] = new RSTile(3348, 3282);
			bankToTrees[5] = new RSTile(3360, 3287);
			bankToTrees[6] = new RSTile(3363, 3298);

			treesToBank[0] = new RSTile(3363, 3298);
			treesToBank[1] = new RSTile(3360, 3287);
			treesToBank[2] = new RSTile(3348, 3282);
			treesToBank[3] = new RSTile(3349, 3274);
			treesToBank[4] = new RSTile(3358, 3265);
			treesToBank[5] = new RSTile(3370, 3266);
			treesToBank[6] = new RSTile(3382, 3268);

		} else if (command.equals("Castle Wars - Ivy")) {
			bankLocation = new RSTile(2445, 3083);
			treeType = "ivy";

			if (random(1, 2) == 1)
				camera.setAngle(random(1, 30));
			else
				camera.setAngle(random(340, 359));

			tree1 = new RSTile(2430, 3068);
			tree2 = new RSTile(2429, 3068);
			tree3 = new RSTile(2428, 3068);
			tree4 = new RSTile(2426, 3068);
			tree5 = new RSTile(2425, 3068);
			tree6 = new RSTile(2424, 3068);
			tree7 = new RSTile(2423, 3068);

			treesToBank = new RSTile[3];
			bankToTrees = new RSTile[3];

			bankToTrees[0] = new RSTile(2443, 3084);
			bankToTrees[1] = new RSTile(2444, 3073);
			bankToTrees[2] = new RSTile(2429, 3068);

			treesToBank[0] = new RSTile(2429, 3068);
			treesToBank[1] = new RSTile(2444, 3073);
			treesToBank[2] = new RSTile(2443, 3084);
		}

		else if (command.equals("Grand Exchange - Ivy")) {
			treeType = "ivy";
			bankerID = 6533;

			camera.setAngle(random(170, 190));

			tree1 = new RSTile(3219, 3498);
			tree2 = new RSTile(3218, 3498);
			tree3 = new RSTile(3217, 3498);
			tree4 = new RSTile(3216, 3498);
			tree5 = new RSTile(0, 0);
			tree6 = new RSTile(0, 0);
			tree7 = new RSTile(0, 0);

			treesToBank = new RSTile[6];
			bankToTrees = new RSTile[6];

			bankToTrees[0] = new RSTile(3167, 3489);
			bankToTrees[1] = new RSTile(3173, 3491);
			bankToTrees[2] = new RSTile(3183, 3490);
			bankToTrees[3] = new RSTile(3195, 3494);
			bankToTrees[4] = new RSTile(3204, 3502);
			bankToTrees[5] = new RSTile(3217, 3500);

			treesToBank[0] = new RSTile(3217, 3500);
			treesToBank[1] = new RSTile(3204, 3502);
			treesToBank[2] = new RSTile(3195, 3494);
			treesToBank[3] = new RSTile(3183, 3490);
			treesToBank[4] = new RSTile(3173, 3491);
			treesToBank[5] = new RSTile(3167, 3489);
		}

		else if (command.equals("Taverly - Ivy")) {
			treeType = "ivy";
			log("Nest banking is unavailable at the Taverly location.");

			camera.setAngle(random(260, 280));

			tree1 = new RSTile(2943, 3416);
			tree2 = new RSTile(2943, 3417);
			tree3 = new RSTile(2943, 3418);
			tree4 = new RSTile(2943, 3419);
			tree5 = new RSTile(2943, 3420);
			tree6 = new RSTile(0, 0);
			tree7 = new RSTile(0, 0);

		} else if (command.equals("Yanille - Ivy")) {
			bankerID = 494;
			treeType = "ivy";

			camera.setAngle(random(170, 190));

			tree1 = new RSTile(2597, 3111);
			tree2 = new RSTile(2596, 3111);
			tree3 = new RSTile(2595, 3111);
			tree4 = new RSTile(2593, 3111);
			tree5 = new RSTile(2592, 3111);
			tree6 = new RSTile(2591, 3111);
			tree7 = new RSTile(0, 0);

			treesToBank = new RSTile[4];
			bankToTrees = new RSTile[4];

			bankToTrees[0] = new RSTile(2613, 3094);
			bankToTrees[1] = new RSTile(2616, 3106);
			bankToTrees[2] = new RSTile(2603, 3113);
			bankToTrees[3] = new RSTile(2597, 3111);

			treesToBank[0] = new RSTile(2597, 3111);
			treesToBank[1] = new RSTile(2603, 3113);
			treesToBank[2] = new RSTile(2616, 3106);
			treesToBank[3] = new RSTile(2613, 3094);
		} else if (command.equals("Ardougne - Ivy")) {
			bankerID = 494;
			treeType = "ivy";

			camera.setAngle(random(80, 100));

			tree1 = new RSTile(2622, 3304);
			tree2 = new RSTile(2622, 3305);
			tree3 = new RSTile(2622, 3307);
			tree4 = new RSTile(2622, 3308);
			tree5 = new RSTile(2622, 3310);
			tree6 = new RSTile(0, 0);
			tree7 = new RSTile(0, 0);

			treesToBank = new RSTile[5];
			bankToTrees = new RSTile[5];

			bankToTrees[0] = new RSTile(2614, 3333);
			bankToTrees[1] = new RSTile(2607, 3326);
			bankToTrees[2] = new RSTile(2607, 3314);
			bankToTrees[3] = new RSTile(2614, 3312);
			bankToTrees[4] = new RSTile(2622, 3310);

			treesToBank[0] = new RSTile(2622, 3310);
			treesToBank[1] = new RSTile(2614, 3312);
			treesToBank[2] = new RSTile(2607, 3314);
			treesToBank[3] = new RSTile(2607, 3326);
			treesToBank[4] = new RSTile(2614, 3333);
		} else if (command.equals("Varrock Palace - Ivy")) {
			bankerID = 5912;
			treeType = "ivy";

			camera.setAngle(random(250, 280));

			tree1 = new RSTile(3233, 3456);
			tree2 = new RSTile(3233, 3457);
			tree3 = new RSTile(3233, 3459);
			tree4 = new RSTile(3233, 3460);
			tree5 = new RSTile(3233, 3461);
			tree6 = new RSTile(0, 0);
			tree7 = new RSTile(0, 0);

			treesToBank = new RSTile[5];
			bankToTrees = new RSTile[5];

			bankToTrees[0] = new RSTile(3253, 3420);
			bankToTrees[1] = new RSTile(3246, 3431);
			bankToTrees[2] = new RSTile(3245, 3441);
			bankToTrees[3] = new RSTile(3244, 3454);
			bankToTrees[4] = new RSTile(3233, 3460);

			treesToBank[0] = new RSTile(3233, 3460);
			treesToBank[1] = new RSTile(3244, 3454);
			treesToBank[2] = new RSTile(3245, 3441);
			treesToBank[3] = new RSTile(3246, 3431);
			treesToBank[4] = new RSTile(3253, 3420);
		} else if (command.equals("South Falador - Ivy")) {
			bankerID = 6200;
			treeType = "ivy";

			if (random(1, 2) == 1)
				camera.setAngle(random(1, 30));
			else
				camera.setAngle(random(340, 359));

			tree1 = new RSTile(3052, 3328);
			tree2 = new RSTile(3051, 3328);
			tree3 = new RSTile(3049, 3328);
			tree4 = new RSTile(3048, 3328);
			tree5 = new RSTile(3047, 3328);
			tree6 = new RSTile(3045, 3328);
			tree7 = new RSTile(3044, 3328);

			treesToBank = new RSTile[10];
			bankToTrees = new RSTile[10];

			bankToTrees[0] = new RSTile(3012, 3356);
			bankToTrees[1] = new RSTile(3006, 3357);
			bankToTrees[2] = new RSTile(3006, 3346);
			bankToTrees[3] = new RSTile(3006, 3339);
			bankToTrees[4] = new RSTile(3006, 3331);
			bankToTrees[5] = new RSTile(3007, 3322);
			bankToTrees[6] = new RSTile(3017, 3323);
			bankToTrees[7] = new RSTile(3027, 3326);
			bankToTrees[8] = new RSTile(3036, 3326);
			bankToTrees[9] = new RSTile(3044, 3327);

			treesToBank[0] = new RSTile(3044, 3327);
			treesToBank[1] = new RSTile(3036, 3326);
			treesToBank[2] = new RSTile(3027, 3326);
			treesToBank[3] = new RSTile(3017, 3323);
			treesToBank[4] = new RSTile(3007, 3322);
			treesToBank[5] = new RSTile(3006, 3331);
			treesToBank[6] = new RSTile(3006, 3339);
			treesToBank[7] = new RSTile(3006, 3346);
			treesToBank[8] = new RSTile(3006, 3357);
			treesToBank[9] = new RSTile(3012, 3356);
		} else if (command.equals("North Falador - Ivy")) {
			bankerID = 6200;
			treeType = "ivy";

			camera.setAngle(random(170, 190));

			tree1 = new RSTile(3018, 3392);
			tree2 = new RSTile(3017, 3392);
			tree3 = new RSTile(3016, 3392);
			tree4 = new RSTile(3015, 3392);
			tree5 = new RSTile(3014, 3392);
			tree6 = new RSTile(3012, 3392);
			tree7 = new RSTile(3011, 3392);

			treesToBank = new RSTile[11];
			bankToTrees = new RSTile[11];

			bankToTrees[0] = new RSTile(2946, 3368);
			bankToTrees[1] = new RSTile(2948, 3376);
			bankToTrees[2] = new RSTile(2952, 3381);
			bankToTrees[3] = new RSTile(2963, 3386);
			bankToTrees[4] = new RSTile(2968, 3397);
			bankToTrees[5] = new RSTile(2974, 3397);
			bankToTrees[6] = new RSTile(2980, 3397);
			bankToTrees[7] = new RSTile(2989, 3397);
			bankToTrees[8] = new RSTile(2994, 3396);
			bankToTrees[9] = new RSTile(3005, 3397);
			bankToTrees[10] = new RSTile(3012, 3393);

			treesToBank[0] = new RSTile(3012, 3393);
			treesToBank[1] = new RSTile(3005, 3397);
			treesToBank[2] = new RSTile(2994, 3396);
			treesToBank[3] = new RSTile(2989, 3397);
			treesToBank[4] = new RSTile(2980, 3397);
			treesToBank[5] = new RSTile(2974, 3397);
			treesToBank[6] = new RSTile(2968, 3397);
			treesToBank[7] = new RSTile(2963, 3386);
			treesToBank[8] = new RSTile(2952, 3381);
			treesToBank[9] = new RSTile(2948, 3376);
			treesToBank[10] = new RSTile(2946, 3368);
		} else if (command.equals("Draynor - Willows")) {
			treeType = "willows";
			bankerID = 494;
			tree1 = new RSTile(3089, 3228);
			tree2 = new RSTile(3088, 3232);
			tree3 = new RSTile(3089, 3235);
			tree4 = new RSTile(3086, 3236);
			tree5 = new RSTile(3084, 3238);
			tree6 = new RSTile(0, 0);
			tree7 = new RSTile(0, 0);

			treesToBank = new RSTile[2];
			bankToTrees = new RSTile[2];

			bankToTrees[0] = new RSTile(3092, 3245);
			bankToTrees[1] = new RSTile(3087, 3237);

			treesToBank[0] = new RSTile(3090, 3236);
			treesToBank[1] = new RSTile(3092, 3245);
		} else if (command.equals("Port Salim - Willows")) {
			treeType = "willows";
			tree1 = new RSTile(3063, 3255);
			tree2 = new RSTile(3063, 3253);
			tree3 = new RSTile(3058, 3255);
			tree4 = new RSTile(3058, 3252);
			tree5 = new RSTile(0, 0);
			tree6 = new RSTile(0, 0);
			tree7 = new RSTile(0, 0);

			treesToBank = new RSTile[4];

			treesToBank[0] = new RSTile(3059, 3252);
			treesToBank[1] = new RSTile(3049, 3246);
			treesToBank[2] = new RSTile(3042, 3240);
			treesToBank[3] = new RSTile(3047, 3236);

			bankToTrees = walking.reversePath(treesToBank);

		} else {
			log("Incorrect combination of chop type / location");
		}
		if (treeType.equals("ivy")) {
			ivyIDs.add(46318);
			ivyIDs.add(46320);
			ivyIDs.add(46322);
			ivyIDs.add(46324);
			stumpIDs.add(46325);
			stumpIDs.add(46323);
			stumpIDs.add(46321);
			stumpIDs.add(46319);
		}
		if (treeType.equals("willows")) {
			ivyIDs.add(5553);
			ivyIDs.add(5552);
			ivyIDs.add(5551);
			stumpIDs.add(5554);
		}
		status = "Finished set up.";
	}

	public int floorHeight() {
		RSNPC testNPC = npcs.getNearest(bankerID);
		if (testNPC != null)
			return 1;
		else
			return 0;
	}

	public void checkTrees() {
		RSObject[] check1 = objects.getAllAt(tree1);
		for (int i = 0; i < check1.length; i++) {
			if (check1[i].getID() == stumpID && tree1Status) {
				tree1Status = false;
				tree1DeadTime = System.currentTimeMillis();
				tree1TimeAlive = -1;
			} else if (check1[i].getID() == treeID && !tree1Status) {
				tree1Status = true;
				tree1AliveTime = System.currentTimeMillis();
				tree1TimeDead = -1;
			} else if (check1[i].getID() == treeID) {
				tree1Status = true;
				tree1TimeAlive = System.currentTimeMillis() - tree1AliveTime;
			} else if (check1[i].getID() == stumpID) {
				tree1Status = false;
				tree1TimeDead = System.currentTimeMillis() - tree1DeadTime;
			}
		}
		RSObject[] check2 = objects.getAllAt(tree2);
		for (int i = 0; i < check2.length; i++) {
			if (check2[i].getID() == stumpID && tree2Status) {
				tree2Status = false;
				tree2DeadTime = System.currentTimeMillis();
				tree2TimeAlive = -1;
			} else if (check2[i].getID() == treeID && !tree2Status) {
				tree2Status = true;
				tree2AliveTime = System.currentTimeMillis();
				tree2TimeDead = -1;
			} else if (check2[i].getID() == treeID) {
				tree2Status = true;
				tree2TimeAlive = System.currentTimeMillis() - tree2AliveTime;
			} else if (check2[i].getID() == stumpID) {
				tree2Status = false;
				tree2TimeDead = System.currentTimeMillis() - tree2DeadTime;
			}
		}
		if (tree3.getX() != 0) {
			RSObject[] check3 = objects.getAllAt(tree3);
			{
				for (int i = 0; i < check3.length; i++) {
					if (check3[i].getID() == stumpID && tree3Status) {
						tree3Status = false;
						tree3DeadTime = System.currentTimeMillis();
						tree3TimeAlive = -1;
					} else if (check3[i].getID() == treeID && !tree3Status) {
						tree3Status = true;
						tree3AliveTime = System.currentTimeMillis();
						tree3TimeDead = -1;
					} else if (check3[i].getID() == treeID) {
						tree3Status = true;
						tree3TimeAlive = System.currentTimeMillis()
								- tree3AliveTime;
					} else if (check3[i].getID() == stumpID) {
						tree3Status = false;
						tree3TimeDead = System.currentTimeMillis()
								- tree3DeadTime;
					}
				}
			}
		} else {
			tree3Status = false;
			tree3TimeDead = 0;
			tree3TimeAlive = 0;
		}
		if (tree4.getX() != 0) {
			RSObject[] check4 = objects.getAllAt(tree4);
			{
				for (int i = 0; i < check4.length; i++) {
					if (check4[i].getID() == stumpID && tree4Status) {
						tree4Status = false;
						tree4DeadTime = System.currentTimeMillis();
						tree4TimeAlive = -1;
					} else if (check4[i].getID() == treeID && !tree4Status) {
						tree4Status = true;
						tree4AliveTime = System.currentTimeMillis();
						tree4TimeDead = -1;
					} else if (check4[i].getID() == treeID) {
						tree4Status = true;
						tree4TimeAlive = System.currentTimeMillis()
								- tree4AliveTime;
					} else if (check4[i].getID() == stumpID) {
						tree4Status = false;
						tree4TimeDead = System.currentTimeMillis()
								- tree4DeadTime;
					}
				}
			}
		} else {
			tree4Status = false;
			tree4TimeDead = 0;
			tree4TimeAlive = 0;
		}
		if (tree5.getX() != 0) {
			RSObject[] check5 = objects.getAllAt(tree5);
			{
				for (int i = 0; i < check5.length; i++) {
					if (check5[i].getID() == stumpID && tree5Status) {
						tree5Status = false;
						tree5DeadTime = System.currentTimeMillis();
						tree5TimeAlive = -1;
					} else if (check5[i].getID() == treeID && !tree5Status) {
						tree5Status = true;
						tree5AliveTime = System.currentTimeMillis();
						tree5TimeDead = -1;
					} else if (check5[i].getID() == treeID) {
						tree5Status = true;
						tree5TimeAlive = System.currentTimeMillis()
								- tree5AliveTime;
					} else if (check5[i].getID() == stumpID) {
						tree5Status = false;
						tree5TimeDead = System.currentTimeMillis()
								- tree5DeadTime;
					}
				}
			}
		} else {
			tree5Status = false;
			tree5TimeDead = 0;
			tree5TimeAlive = 0;
		}
		if (tree6.getX() != 0) {
			RSObject[] check6 = objects.getAllAt(tree6);
			{
				for (int i = 0; i < check6.length; i++) {
					if (check6[i].getID() == stumpID && tree6Status) {
						tree6Status = false;
						tree6DeadTime = System.currentTimeMillis();
						tree6TimeAlive = -1;
					} else if (check6[i].getID() == treeID && !tree6Status) {
						tree6Status = true;
						tree6AliveTime = System.currentTimeMillis();
						tree6TimeDead = -1;
					} else if (check6[i].getID() == treeID) {
						tree6Status = true;
						tree6TimeAlive = System.currentTimeMillis()
								- tree6AliveTime;
					} else if (check6[i].getID() == stumpID) {
						tree6Status = false;
						tree6TimeDead = System.currentTimeMillis()
								- tree6DeadTime;
					}
				}
			}
		} else {
			tree6Status = false;
			tree6TimeDead = 0;
			tree6TimeAlive = 0;
		}
		if (tree7.getX() != 0) {
			RSObject[] check7 = objects.getAllAt(tree7);
			{
				for (int i = 0; i < check7.length; i++) {
					if (check7[i].getID() == stumpID && tree7Status) {
						tree7Status = false;
						tree7DeadTime = System.currentTimeMillis();
						tree7TimeAlive = -1;
					} else if (check7[i].getID() == treeID && !tree7Status) {
						tree7Status = true;
						tree7AliveTime = System.currentTimeMillis();
						tree7TimeDead = -1;
					} else if (check7[i].getID() == treeID) {
						tree7Status = true;
						tree7TimeAlive = System.currentTimeMillis()
								- tree7AliveTime;
					} else if (check7[i].getID() == stumpID) {
						tree7Status = false;
						tree7TimeDead = System.currentTimeMillis()
								- tree7DeadTime;
					}
				}
			}
		} else {
			tree7Status = false;
			tree7TimeDead = 0;
			tree7TimeAlive = 0;
		}
	}

	public void checkIvy() {
		RSObject[] check1 = objects.getAllAt(tree1);
		for (int i = 0; i < check1.length; i++) {
			if (stumpIDs.contains(check1[i].getID()) && tree1Status) {
				tree1Status = false;
				tree1DeadTime = System.currentTimeMillis();
				tree1TimeAlive = -1;
			} else if (ivyIDs.contains(check1[i].getID()) && !tree1Status) {
				tree1Status = true;
				tree1AliveTime = System.currentTimeMillis();
				tree1TimeDead = -1;
			} else if (ivyIDs.contains(check1[i].getID())) {
				tree1Status = true;
				tree1TimeAlive = System.currentTimeMillis() - tree1AliveTime;
			} else if (stumpIDs.contains(check1[i].getID())) {
				tree1Status = false;
				tree1TimeDead = System.currentTimeMillis() - tree1DeadTime;
			}
		}
		RSObject[] check2 = objects.getAllAt(tree2);
		for (int i = 0; i < check2.length; i++) {
			if (stumpIDs.contains(check2[i].getID()) && tree2Status) {
				tree2Status = false;
				tree2DeadTime = System.currentTimeMillis();
				tree2TimeAlive = -1;
			} else if (ivyIDs.contains(check2[i].getID()) && !tree2Status) {
				tree2Status = true;
				tree2AliveTime = System.currentTimeMillis();
				tree2TimeDead = -1;
			} else if (ivyIDs.contains(check2[i].getID())) {
				tree2Status = true;
				tree2TimeAlive = System.currentTimeMillis() - tree2AliveTime;
			} else if (stumpIDs.contains(check2[i].getID())) {
				tree2Status = false;
				tree2TimeDead = System.currentTimeMillis() - tree2DeadTime;
			}
		}
		if (tree3.getX() != 0) {
			RSObject[] check3 = objects.getAllAt(tree3);
			{
				for (int i = 0; i < check3.length; i++) {
					if (stumpIDs.contains(check3[i].getID()) && tree3Status) {
						tree3Status = false;
						tree3DeadTime = System.currentTimeMillis();
						tree3TimeAlive = -1;
					} else if (ivyIDs.contains(check3[i].getID())
							&& !tree3Status) {
						tree3Status = true;
						tree3AliveTime = System.currentTimeMillis();
						tree3TimeDead = -1;
					} else if (ivyIDs.contains(check3[i].getID())) {
						tree3Status = true;
						tree3TimeAlive = System.currentTimeMillis()
								- tree3AliveTime;
					} else if (stumpIDs.contains(check3[i].getID())) {
						tree3Status = false;
						tree3TimeDead = System.currentTimeMillis()
								- tree3DeadTime;
					}
				}
			}
		} else {
			tree3Status = false;
			tree3TimeDead = 0;
			tree3TimeAlive = 0;
		}
		if (tree4.getX() != 0) {
			RSObject[] check4 = objects.getAllAt(tree4);
			{
				for (int i = 0; i < check4.length; i++) {
					if (stumpIDs.contains(check4[i].getID()) && tree4Status) {
						tree4Status = false;
						tree4DeadTime = System.currentTimeMillis();
						tree4TimeAlive = -1;
					} else if (ivyIDs.contains(check4[i].getID())
							&& !tree4Status) {
						tree4Status = true;
						tree4AliveTime = System.currentTimeMillis();
						tree4TimeDead = -1;
					} else if (ivyIDs.contains(check4[i].getID())) {
						tree4Status = true;
						tree4TimeAlive = System.currentTimeMillis()
								- tree4AliveTime;
					} else if (stumpIDs.contains(check4[i].getID())) {
						tree4Status = false;
						tree4TimeDead = System.currentTimeMillis()
								- tree4DeadTime;
					}
				}
			}
		} else {
			tree4Status = false;
			tree4TimeDead = 0;
			tree4TimeAlive = 0;
		}
		if (tree5.getX() != 0) {
			RSObject[] check5 = objects.getAllAt(tree5);
			{
				for (int i = 0; i < check5.length; i++) {
					if (stumpIDs.contains(check5[i].getID()) && tree5Status) {
						tree5Status = false;
						tree5DeadTime = System.currentTimeMillis();
						tree5TimeAlive = -1;
					} else if (ivyIDs.contains(check5[i].getID())
							&& !tree5Status) {
						tree5Status = true;
						tree5AliveTime = System.currentTimeMillis();
						tree5TimeDead = -1;
					} else if (ivyIDs.contains(check5[i].getID())) {
						tree5Status = true;
						tree5TimeAlive = System.currentTimeMillis()
								- tree5AliveTime;
					} else if (stumpIDs.contains(check5[i].getID())) {
						tree5Status = false;
						tree5TimeDead = System.currentTimeMillis()
								- tree5DeadTime;
					}
				}
			}
		} else {
			tree5Status = false;
			tree5TimeDead = 0;
			tree5TimeAlive = 0;
		}
		if (tree6.getX() != 0) {
			RSObject[] check6 = objects.getAllAt(tree6);
			{
				for (int i = 0; i < check6.length; i++) {
					if (stumpIDs.contains(check6[i].getID()) && tree6Status) {
						tree6Status = false;
						tree6DeadTime = System.currentTimeMillis();
						tree6TimeAlive = -1;
					} else if (ivyIDs.contains(check6[i].getID())
							&& !tree6Status) {
						tree6Status = true;
						tree6AliveTime = System.currentTimeMillis();
						tree6TimeDead = -1;
					} else if (ivyIDs.contains(check6[i].getID())) {
						tree6Status = true;
						tree6TimeAlive = System.currentTimeMillis()
								- tree6AliveTime;
					} else if (stumpIDs.contains(check6[i].getID())) {
						tree6Status = false;
						tree6TimeDead = System.currentTimeMillis()
								- tree6DeadTime;
					}
				}
			}
		} else {
			tree6Status = false;
			tree6TimeDead = 0;
			tree6TimeAlive = 0;
		}
		if (tree7.getX() != 0) {
			RSObject[] check7 = objects.getAllAt(tree7);
			{
				for (int i = 0; i < check7.length; i++) {
					if (stumpIDs.contains(check7[i].getID()) && tree7Status) {
						tree7Status = false;
						tree7DeadTime = System.currentTimeMillis();
						tree7TimeAlive = -1;
					} else if (ivyIDs.contains(check7[i].getID())
							&& !tree7Status) {
						tree7Status = true;
						tree7AliveTime = System.currentTimeMillis();
						tree7TimeDead = -1;
					} else if (ivyIDs.contains(check7[i].getID())) {
						tree7Status = true;
						tree7TimeAlive = System.currentTimeMillis()
								- tree7AliveTime;
					} else if (stumpIDs.contains(check7[i].getID())) {
						tree7Status = false;
						tree7TimeDead = System.currentTimeMillis()
								- tree7DeadTime;
					}
				}
			}
		} else {
			tree7Status = false;
			tree7TimeDead = 0;
			tree7TimeAlive = 0;
		}

	}

	public void calculateNextTree() {
		if (!tree7Status && !tree6Status && !tree5Status && !tree4Status
				&& !tree3Status && !tree2Status && !tree1Status) {
			long num = -2;
			int index = -1;
			long[] temp = { tree1TimeDead, tree2TimeDead, tree3TimeDead,
					tree4TimeDead, tree5TimeDead, tree6TimeDead, tree7TimeDead };
			for (int i = 0; i < temp.length; i++) {
				if (temp[i] >= num) {
					index = i;
					num = temp[i];
				}
			}
			nextTree = index + 1;
		} else if (nearest) {
			RSTile[] trees = { tree1, tree2, tree3, tree4, tree5, tree6, tree7 };
			int[] distances = new int[7];
			boolean[] temp = { tree1Status, tree2Status, tree3Status,
					tree4Status, tree5Status, tree6Status, tree7Status };
			for (int i = 0; i < temp.length; i++)
				if (temp[i])
					distances[i] = distanceTo(trees[i]);
				else
					distances[i] = 500;
			int min = 500;
			int index = -1;
			if (!status.equals("Chopping!")) {
				for (int i = 0; i < distances.length; i++)
					if (distances[i] < min) {
						index = i;
						min = distances[i];
					}
			} else {
				min = 499;
				int index2 = -1;
				for (int i = 0; i < distances.length; i++) {
					if (distances[i] < min && min == 499) {
						index2 = i;
						min = distances[i];
					} else if (distances[i] < min && min != 499) {
						index = index2;
						index2 = i;
					}
				}
			}
			nextTree = index + 1;
		} else {
			long num = -2;
			int index = -1;
			long[] temp = { tree1TimeAlive, tree2TimeAlive, tree3TimeAlive,
					tree4TimeAlive, tree5TimeAlive, tree6TimeAlive,
					tree7TimeAlive };
			if (!status.equals("Chopping!")) {
				for (int i = 0; i < temp.length; i++) {
					if (temp[i] >= num) {
						index = i;
						num = temp[i];
					}
				}
			} else {
				int index2 = -1;
				for (int i = 0; i < temp.length; i++) {
					if (temp[i] >= num && num == -2) {
						index2 = i;
						num = temp[i];
					} else if (temp[i] >= num) {
						index = index2;
						index2 = i;
					}
				}
			}
			nextTree = index + 1;
		}
	}

	public RSObject getNextTree() {
		if (!treeType.equals("ivy") && !treeType.equals("willows")) {
			RSObject[] temp = new RSObject[10];
			if (nextTree == 1)
				temp = objects.getAllAt(tree1);
			if (nextTree == 2)
				temp = objects.getAllAt(tree2);
			if (nextTree == 3)
				temp = objects.getAllAt(tree3);
			if (nextTree == 4)
				temp = objects.getAllAt(tree4);
			if (nextTree == 5)
				temp = objects.getAllAt(tree5);
			if (nextTree == 6)
				temp = objects.getAllAt(tree6);
			if (nextTree == 7)
				temp = objects.getAllAt(tree7);
			for (int i = 0; i < temp.length; i++)
				if (temp[i].getID() == treeID)
					return temp[i];
		} else {
			RSObject[] temp = new RSObject[10];
			if (nextTree == 1)
				temp = objects.getAllAt(tree1);
			if (nextTree == 2)
				temp = objects.getAllAt(tree2);
			if (nextTree == 3)
				temp = objects.getAllAt(tree3);
			if (nextTree == 4)
				temp = objects.getAllAt(tree4);
			if (nextTree == 5)
				temp = objects.getAllAt(tree5);
			if (nextTree == 6)
				temp = objects.getAllAt(tree6);
			if (nextTree == 7)
				temp = objects.getAllAt(tree7);
			for (int i = 0; i < temp.length; i++)
				if (ivyIDs.contains(temp[i].getID()))
					return temp[i];
		}
		return null;
	}

	public RSTile getMidTile(RSTile tile1, RSTile tile2) {
		int x1 = tile1.getX();
		int y1 = tile1.getY();
		int x2 = tile2.getX();
		int y2 = tile2.getY();

		return new RSTile((x1 + x2) / 2, (y1 + y2) / 2);
	}

	public void moveToNextTree() {
		status = "Moving to tree: " + nextTree;
		RSTile[] trees = { tree1, tree2, tree3, tree4, tree5, tree6, tree7 };
		RSTile tileToWalkTo = getMidTile(players.getMyPlayer().getLocation(),
				trees[nextTree - 1]);
		if (!walking.isRunEnabled() && walking.getEnergy() > 20) {
			walking.setRun(true);
			sleep(random(600, 800));
		}
		if (command.equals("South Falador - Yews")
				|| command.equals("Draynor - Yews")) {
			moveToNextTreeLong();
		} else if (command.equals("Edgeville - Yews")) {
			if (nextTree == 1 && calc.tileOnMap(tree1))
				tileToWalkTo = new RSTile(tree1.getX(), tree1.getY() - 2);
			if (nextTree == 2 && calc.tileOnMap(tree2))
				tileToWalkTo = new RSTile(tree2.getX(), tree2.getY() + 2);
		} else if (command.equals("Grand Exchange - Yews")) {
			if (nextTree == 1 && calc.tileOnMap(tree1))
				tileToWalkTo = new RSTile(tree1.getX() + 2, tree1.getY());
			if (nextTree == 2 && calc.tileOnMap(tree2))
				tileToWalkTo = new RSTile(tree2.getX(), tree2.getY());
			if (nextTree == 3 && calc.tileOnMap(tree3))
				tileToWalkTo = new RSTile(tree3.getX() - 2, tree2.getY());
		} else if (command.equals("Catherby - Yews")) {
			if (nextTree == 1 && calc.tileOnMap(tree1))
				tileToWalkTo = new RSTile(tree1.getX(), tree1.getY() - 2);
			if (nextTree == 2 && calc.tileOnMap(tree2))
				tileToWalkTo = new RSTile(tree2.getX(), tree2.getY() - 2);
			if (nextTree == 3 && calc.tileOnMap(tree3))
				tileToWalkTo = new RSTile(tree3.getX(), tree3.getY() - 2);
			if (nextTree == 4 && calc.tileOnMap(tree4))
				tileToWalkTo = new RSTile(tree4.getX(), tree4.getY() - 2);
			if (nextTree == 5 && calc.tileOnMap(tree5))
				tileToWalkTo = new RSTile(tree5.getX(), tree5.getY() - 2);
			if (nextTree == 6 && calc.tileOnMap(tree6))
				tileToWalkTo = new RSTile(tree6.getX(), tree6.getY() - 2);
			if (nextTree == 7 && calc.tileOnMap(tree7))
				tileToWalkTo = new RSTile(tree7.getX(), tree7.getY() - 2);
		} else if (command.equals("Catherby - Yews")) {
			if (nextTree == 1 && calc.tileOnMap(tree1))
				tileToWalkTo = new RSTile(tree1.getX() - 1, tree1.getY() + 2);
			if (nextTree == 2 && calc.tileOnMap(tree2))
				tileToWalkTo = new RSTile(tree2.getX(), tree2.getY() + 2);
			if (nextTree == 3 && calc.tileOnMap(tree3))
				tileToWalkTo = new RSTile(tree3.getX(), tree2.getY() - 2);
		} else {
			if (nextTree == 1 && calc.tileOnMap(tree1))
				tileToWalkTo = tree1;
			if (nextTree == 2 && calc.tileOnMap(tree2))
				tileToWalkTo = tree2;
			if (nextTree == 3 && calc.tileOnMap(tree3))
				tileToWalkTo = tree3;
			if (nextTree == 4 && calc.tileOnMap(tree4))
				tileToWalkTo = tree4;
			if (nextTree == 5 && calc.tileOnMap(tree5))
				tileToWalkTo = tree5;
			if (nextTree == 6 && calc.tileOnMap(tree6))
				tileToWalkTo = tree6;
			if (nextTree == 7 && calc.tileOnMap(tree7))
				tileToWalkTo = tree7;
		}
		if (!command.equals("South Falador - Yews")
				&& !command.equals("Draynor - Yews")) {
			boolean didClick = false;
			if (calc.tileOnScreen(tileToWalkTo)
					&& !command.equals("Mage Training Area - Magics")) {
				if (walking.walkTileOnScreen(tileToWalkTo))
					didClick = true;
			} else if (walking.walkTileMM(tileToWalkTo))
				didClick = true;
			if (command.equals("Mage Training Area - Magics") && didClick)
				while (getMyPlayer().isMoving())
					sleep(random(600, 800));
			sleep(random(600, 800));
			if (didClick) {
				long start = System.currentTimeMillis();
				while (!playerIsNear(trees[nextTree - 1], 4)) {
					if (System.currentTimeMillis() - start > 2000)
						break;
					if (System.currentTimeMillis() - start > random(600, 800)
							&& !getMyPlayer().isMoving())
						walking.walkTileMM(tileToWalkTo);
					sleep(random(100, 200));
				}
				if (!playerIsNear(trees[nextTree - 1], 4)) // try again
					walking.walkTileMM(tileToWalkTo);
			}
		}
	}

	public void moveToNextTreeLong() {
		RSTile[] trees = { tree1, tree2, tree3, tree4, tree5, tree6, tree7 };
		long start = System.currentTimeMillis();
		if (location.equals("South Falador")) {
			RSTile[] p1to3 = { tree1, new RSTile(3007, 3318), tree2,
					new RSTile(3030, 3321), tree3 };
			RSTile[] p3to1 = walking.reversePath(p1to3);
			RSTile[] p1to2 = { tree1, new RSTile(3007, 3318), tree2 };
			RSTile[] p2to1 = walking.reversePath(p1to2);
			RSTile[] p2to3 = { tree2, new RSTile(3030, 3321), tree3 };
			RSTile[] p3to2 = walking.reversePath(p2to3);

			RSTile[][] paths = { p1to3, p3to1, p1to2, p2to1, p2to3, p3to2 };

			int choice = -1;
			if (nextTree == 1 && !atTrees())
				choice = 1;
			if (nextTree == 1 && atTrees())
				choice = 3;
			else if (nextTree == 2 && playerIsNear(tree3, 4))
				choice = 5;
			else if (nextTree == 2 && playerIsNear(tree1, 4))
				choice = 2;
			else if (nextTree == 3)
				choice = 0;
			else if (nextTree == 2)
				choice = 2;

			if (choice >= 0) {
				status = "Moving to tree: " + nextTree;
				int lastTileToWalkTo = -1;
				int tempTileToWalkTo = walkPath(paths[choice]);
				boolean clickedLastTile = false;
				while (!playerIsNear(trees[nextTree - 1], 4)
						&& System.currentTimeMillis() - start < 20000) {
					boolean clicked = false;
					tempTileToWalkTo = walkPath(paths[choice]);
					if (!clickedLastTile) {
						if (tempTileToWalkTo > lastTileToWalkTo) {
							if (calc.tileOnScreen(paths[choice][tempTileToWalkTo])) {
								if (!walking
										.walkTileOnScreen(paths[choice][tempTileToWalkTo]))
									if (walking.walkTileMM(
											paths[choice][tempTileToWalkTo], 1,
											1))
										clicked = true;
									else
										clicked = false;
								else
									clicked = true;
							} else
								clicked = walking.walkTileMM(
										paths[choice][tempTileToWalkTo], 1, 1);
						}

						if (clicked) {
							lastTileToWalkTo = tempTileToWalkTo;
							if (tempTileToWalkTo == paths[choice].length - 1) {
								clickedLastTile = true;
							}
						}
					}
					sleep(random(200, 300));
				}
			}
		}
		if (location.equals("Draynor")) {
			RSTile[] p1to2 = { tree1, new RSTile(3149, 3244), tree2 };
			RSTile[] p2to1 = walking.reversePath(p1to2);
			RSTile[] p2to3 = { tree2, new RSTile(3161, 3222), tree3 };
			RSTile[] p3to2 = walking.reversePath(p2to3);
			RSTile[] p3to4 = { tree3, new RSTile(3176, 3224), tree4 };
			RSTile[] p4to3 = walking.reversePath(p3to4);
			RSTile[] p2to4 = { tree2, new RSTile(3162, 3227),
					new RSTile(3171, 3227), tree4 };
			RSTile[] p4to2 = walking.reversePath(p2to4);
			RSTile[] p1to3 = { tree1, new RSTile(3151, 3248),
					new RSTile(3153, 3241), new RSTile(3161, 3228), tree3 };
			RSTile[] p3to1 = walking.reversePath(p1to3);
			RSTile[] p1to4 = { tree1, new RSTile(3156, 3244),
					new RSTile(3165, 3235), new RSTile(3175, 3229), tree4 };
			RSTile[] p4to1 = walking.reversePath(p1to4);

			RSTile[][] paths = { p1to2, p2to1, p2to3, p3to2, p3to4, p4to3,
					p2to4, p4to2, p1to3, p3to1, p1to4, p4to1 };

			int choice = -1;
			status = "Moving to tree: " + nextTree;
			if (nextTree == 1 && playerIsNear(tree3, 4))
				choice = 9;
			else if (nextTree == 1 && playerIsNear(tree2, 4))
				choice = 1;
			else if (nextTree == 2 && playerIsNear(tree3, 4))
				choice = 3;
			else if (nextTree == 2 && playerIsNear(tree1, 4))
				choice = 0;
			else if (nextTree == 3 && playerIsNear(tree1, 4))
				choice = 8;
			else if (nextTree == 3 && playerIsNear(tree2, 4))
				choice = 2;
			else if (nextTree == 4 && playerIsNear(tree3, 4))
				choice = 4;
			else if (nextTree == 3 && playerIsNear(tree4, 4))
				choice = 5;
			else if (nextTree == 4 && playerIsNear(tree2, 4))
				choice = 6;
			else if (nextTree == 2 && playerIsNear(tree4, 4))
				choice = 7;
			else if (nextTree == 4 && playerIsNear(tree1, 4))
				choice = 10;
			else if (nextTree == 1 && playerIsNear(tree4, 4))
				choice = 11;
			else if (playerIsNear(new RSTile(3149, 3237), 4))
				choice = 0;
			else if (playerIsNear(new RSTile(3161, 3222), 4))
				choice = 2;
			else if (playerIsNear(new RSTile(3176, 3224), 4))
				choice = 4;
			else if (playerIsNear(new RSTile(3171, 3227), 4)
					|| playerIsNear(new RSTile(3162, 3227), 4))
				choice = 6;
			else if (playerIsNear(new RSTile(3151, 3248), 4)
					|| playerIsNear(new RSTile(3153, 3241), 4)
					|| playerIsNear(new RSTile(3161, 3228), 4))
				choice = 8;
			else if (playerIsNear(new RSTile(3156, 3240), 4)
					|| playerIsNear(new RSTile(3166, 3232), 4)
					|| playerIsNear(new RSTile(3176, 3229), 4))
				choice = 10;

			if (choice >= 0) {
				int lastTileToWalkTo = -1;
				int tempTileToWalkTo = walkPath(paths[choice]);
				boolean clickedLastTile = false;
				while (!playerIsNear(trees[nextTree - 1], 4)
						&& System.currentTimeMillis() - start < 20000) {
					boolean clicked = false;
					tempTileToWalkTo = walkPath(paths[choice]);
					if (!clickedLastTile) {
						if (tempTileToWalkTo > lastTileToWalkTo) {
							if (calc.tileOnScreen(paths[choice][tempTileToWalkTo])) {
								if (!walking
										.walkTileOnScreen(paths[choice][tempTileToWalkTo]))
									if (walking.walkTileMM(
											paths[choice][tempTileToWalkTo], 1,
											1))
										clicked = true;
									else
										clicked = false;
								else
									clicked = true;
							} else
								clicked = walking.walkTileMM(
										paths[choice][tempTileToWalkTo], 1, 1);

						}
						if (clicked) {
							lastTileToWalkTo = tempTileToWalkTo;
							if (tempTileToWalkTo == paths[choice].length - 1) {
								clickedLastTile = true;
							}
						}
					}
					sleep(random(200, 300));
				}
			}
		}
	}

	public void chop(boolean secondTry) {
		safety = 1;
		RSObject treeToChop = getNextTree();
		RSModel m = treeToChop.getModel();

		if (!secondTry && treeType.equals("ivy")) {

			if (command.equals("Castle Wars - Ivy")
					|| command.equals("South Falador - Ivy")) {
				int tempNum = random(1, 2);
				if (tempNum == 1)
					camera.setAngle(random(1, 30));
				if (tempNum == 2)
					camera.setAngle(random(340, 359));
			}
			if (command.equals("Grand Exchange - Ivy")
					|| command.equals("Yanille - Ivy")
					|| command.equals("North Falador - Ivy")) {
				camera.setAngle(random(170, 190));
			}
			if (command.equals("Taverly - Ivy")
					|| command.equals("Varrock Palace - Ivy")) {
				camera.setAngle(random(260, 280));
			}
			if (command.equals("Ardougne - Ivy")) {
				camera.setAngle(random(80, 100));
			}
		}
		Point temp = m.getPoint();
		mouse.move(temp);
		if (treeType.equals("yews"))
			menu.doAction("Chop down Yew");
		else if (treeType.equals("magics"))
			menu.doAction("Chop down Magic tree");
		else if (treeType.equals("ivy"))
			menu.doAction("Chop Ivy");
		else if (treeType.equals("willows"))
			menu.doAction("Chop down Willow");
		else if (treeType.equals("maples"))
			menu.doAction("Chop down Maple tree");
		else if (treeType.equals("oaks"))
			menu.doAction("Chop down Oak");

		sleep(random(600, 800));
		while (players.getMyPlayer().isMoving()) {
			sleep(random(100, 200));
		}
		if (!chopCheck() && !secondTry)
			chop(true);
	}

	public void hoverMouse() {
		if (!players.getMyPlayer().isMoving()) {
			RSTile[] trees = { tree1, tree2, tree3, tree4, tree5, tree6, tree7 };
			if (calc.tileOnScreen(trees[nextTree - 1])) {
				Point temp = calc.tileToScreen(trees[nextTree - 1]);
				try {
					if (temp.getY() - 50 > 1)
						mouse.move(
								new Point((int) temp.getX(),
										(int) temp.getY() - 50), 1, 1);
					else
						mouse.move(
								new Point((int) temp.getX(), (int) temp.getY()),
								1, 1);
				} catch (Exception e) {
				}
				;
			}
		}
	}

	public boolean atBank() {
		if (command.equals("Grand Exchange - Yews")
				|| command.equals("Grand Exchange - Ivy")) {
			if (getMyPlayer().getLocation().getX() < 3171
					&& getMyPlayer().getLocation().getX() >= 3167) {
			} else
				return false;
			if (getMyPlayer().getLocation().getY() >= 3486
					&& getMyPlayer().getLocation().getY() <= 3492)
				return true;
		}
		if (command.equals("Edgeville - Yews")) {
			if (getMyPlayer().getLocation().getX() < 3096
					&& getMyPlayer().getLocation().getX() >= 3091) {
			} else
				return false;
			if (getMyPlayer().getLocation().getY() >= 3488
					&& getMyPlayer().getLocation().getY() <= 3494)
				return true;
		}
		if (command.equals("South Falador - Yews")
				|| command.equals("South Falador - Ivy")) {
			if (getMyPlayer().getLocation().getX() < 3016
					&& getMyPlayer().getLocation().getX() >= 3010) {
			} else
				return false;
			if (getMyPlayer().getLocation().getY() >= 3355
					&& getMyPlayer().getLocation().getY() <= 3359)
				return true;
		}
		if (command.equals("Rimmington - Yews")
				|| command.equals("Port Salim - Willows")) {
			return (getMyPlayer().getLocation().getX() >= 3045
					&& getMyPlayer().getLocation().getY() <= 3237 && getMyPlayer()
					.getLocation().getY() >= 3234);
		}

		if (command.equals("Catherby - Yews")) {
			if (getMyPlayer().getLocation().getX() <= 2811
					&& getMyPlayer().getLocation().getX() >= 2807) {
			} else
				return false;
			if (getMyPlayer().getLocation().getY() >= 3439
					&& getMyPlayer().getLocation().getY() <= 3441)
				return true;
		}
		if (command.equals("Tree Gnome - Yews")) {
			if (getMyPlayer().getLocation().getX() <= 2446
					&& getMyPlayer().getLocation().getX() >= 2445) {
			} else
				return false;
			if (getMyPlayer().getLocation().getY() >= 3423
					&& getMyPlayer().getLocation().getY() <= 3428)
				return true;
		}
		if (location.equals("Seer's Village")
				|| command.equals("Sorcerer's Tower - Magics")) {
			if (getMyPlayer().getLocation().getX() >= 2724
					&& getMyPlayer().getLocation().getX() <= 2728) {
			} else
				return false;
			if (getMyPlayer().getLocation().getY() >= 3491
					&& getMyPlayer().getLocation().getY() <= 3495)
				return true;
		}
		if (command.equals("Mage Training Area - Magics")) {
			return playerIsNear(bankLocation, 5);
		}
		if (command.equals("Castle Wars - Ivy")) {
			if (getMyPlayer().getLocation().getX() < 2445
					&& getMyPlayer().getLocation().getX() >= 3086) {
			} else
				return false;
			if (getMyPlayer().getLocation().getY() >= 3083
					&& getMyPlayer().getLocation().getY() <= 3086)
				return true;
		}
		if (command.equals("Yanille - Ivy")) {
			if (getMyPlayer().getLocation().getX() < 2614
					&& getMyPlayer().getLocation().getX() >= 2611) {
			} else
				return false;
			if (getMyPlayer().getLocation().getY() >= 3092
					&& getMyPlayer().getLocation().getY() <= 3095)
				return true;
		}
		if (command.equals("Ardougne - Ivy")) {
			if (getMyPlayer().getLocation().getX() < 2616
					&& getMyPlayer().getLocation().getX() >= 2613) {
			} else
				return false;
			if (getMyPlayer().getLocation().getY() >= 3332
					&& getMyPlayer().getLocation().getY() <= 3334)
				return true;
		}
		if (command.equals("Varrock Palace - Ivy")) {
			if (getMyPlayer().getLocation().getX() < 3255
					&& getMyPlayer().getLocation().getX() >= 3250) {
			} else
				return false;
			if (getMyPlayer().getLocation().getY() >= 3252
					&& getMyPlayer().getLocation().getY() <= 3422)
				return true;
		}
		if (command.equals("North Falador - Ivy")) {
			if (getMyPlayer().getLocation().getX() < 2948
					&& getMyPlayer().getLocation().getX() >= 2944) {
			} else
				return false;
			if (getMyPlayer().getLocation().getY() >= 3368
					&& getMyPlayer().getLocation().getY() <= 3371)
				return true;
		}
		if (location.equals("Draynor")) {
			if (getMyPlayer().getLocation().getX() < 3094
					&& getMyPlayer().getLocation().getX() >= 3092) {
			} else
				return false;
			if (getMyPlayer().getLocation().getY() >= 3244
					&& getMyPlayer().getLocation().getY() <= 3246)
				return true;
		}
		return false;
	}

	public boolean atTrees() {
		if (command.equals("Draynor - Oaks"))
			return playerIsNear(tree1, 4) || playerIsNear(tree2, 4);
		if (command.equals("Grand Exchange - Yews")) {
			if (getMyPlayer().getLocation().getX() >= 3203
					&& getMyPlayer().getLocation().getX() <= 3222) {
			} else
				return false;
			if (getMyPlayer().getLocation().getY() >= 3500
					&& getMyPlayer().getLocation().getY() <= 3505)
				return true;
		}
		if (command.equals("Edgeville - Yews")) {
			if (getMyPlayer().getLocation().getX() < 3090
					&& getMyPlayer().getLocation().getX() >= 3085) {
			} else
				return false;
			if (getMyPlayer().getLocation().getY() >= 3468
					&& getMyPlayer().getLocation().getY() <= 3482)
				return true;
		}
		if (command.equals("Rimmington - Yews")) {
			if (getMyPlayer().getLocation().getX() < 2942
					&& getMyPlayer().getLocation().getX() >= 2932) {
			} else
				return false;
			if (getMyPlayer().getLocation().getY() >= 3225
					&& getMyPlayer().getLocation().getY() <= 3236)
				return true;
		}
		if (command.equals("Catherby - Yews")) {
			if (getMyPlayer().getLocation().getX() < 2769
					&& getMyPlayer().getLocation().getX() >= 2751) {
			} else
				return false;
			if (getMyPlayer().getLocation().getY() >= 3427
					&& getMyPlayer().getLocation().getY() <= 3438)
				return true;
		}
		if (command.equals("Seer's Village - Yews")) {
			if (getMyPlayer().getLocation().getX() < 2717
					&& getMyPlayer().getLocation().getX() >= 2712) {
			} else
				return false;
			if (getMyPlayer().getLocation().getY() >= 3462
					&& getMyPlayer().getLocation().getY() <= 3464)
				return true;
		}
		if (command.equals("South Falador - Yews")) {
			if (getMyPlayer().getLocation().getX() < 3010
					&& getMyPlayer().getLocation().getX() >= 3004) {
			} else
				return false;
			if (getMyPlayer().getLocation().getY() >= 3317
					&& getMyPlayer().getLocation().getY() <= 3325)
				return true;
		}
		if (command.equals("Draynor - Yews")) {
			return playerIsNear(tree2, 5);
		}
		if (command.equals("Tree Gnome - Yews")) {
			return floorHeight() == 0
					&& playerIsNear(new RSTile(2444, 3434), 5);
		}
		if (command.equals("Seer's Village - Magics")) {
			if (getMyPlayer().getLocation().getX() >= 2690
					&& getMyPlayer().getLocation().getX() <= 2699) {
			} else
				return false;
			if (getMyPlayer().getLocation().getY() >= 3422
					&& getMyPlayer().getLocation().getY() <= 3426)
				return true;
		}
		if (command.equals("Sorcerer's Tower - Magics")) {
			if (getMyPlayer().getLocation().getX() <= 2704
					&& getMyPlayer().getLocation().getX() >= 2700) {
			} else
				return false;
			if (getMyPlayer().getLocation().getY() >= 3396
					&& getMyPlayer().getLocation().getY() <= 3400)
				return true;
		}
		if (command.equals("Mage Training Area - Magics")) {
			if (getMyPlayer().getLocation().getX() <= 3365
					&& getMyPlayer().getLocation().getX() >= 3361) {
			} else
				return false;
			if (getMyPlayer().getLocation().getY() >= 3296
					&& getMyPlayer().getLocation().getY() <= 3300)
				return true;
		}
		if (command.equals("Castle Wars - Ivy")) {
			if (getMyPlayer().getLocation().getX() < 2431
					&& getMyPlayer().getLocation().getX() >= 2423) {
			} else
				return false;
			if (getMyPlayer().getLocation().getY() >= 3067
					&& getMyPlayer().getLocation().getY() <= 3068)
				return true;
		}
		if (command.equals("Grand Exchange - Ivy")) {
			if (getMyPlayer().getLocation().getX() < 3220
					&& getMyPlayer().getLocation().getX() >= 3215) {
			} else
				return false;
			if (getMyPlayer().getLocation().getY() >= 3498
					&& getMyPlayer().getLocation().getY() <= 3502)
				return true;
		}
		if (command.equals("Yanille - Ivy")) {
			if (getMyPlayer().getLocation().getX() < 2599
					&& getMyPlayer().getLocation().getX() >= 2594) {
			} else
				return false;
			if (getMyPlayer().getLocation().getY() >= 3111
					&& getMyPlayer().getLocation().getY() <= 3112)
				return true;
		}
		if (command.equals("Ardougne - Ivy")) {
			if (getMyPlayer().getLocation().getX() < 2624
					&& getMyPlayer().getLocation().getX() >= 2622) {
			} else
				return false;
			if (getMyPlayer().getLocation().getY() >= 3304
					&& getMyPlayer().getLocation().getY() <= 3310)
				return true;
		}
		if (command.equals("Varrock Palace - Ivy")) {
			if (getMyPlayer().getLocation().getX() < 3234
					&& getMyPlayer().getLocation().getX() >= 3232) {
			} else
				return false;
			if (getMyPlayer().getLocation().getY() >= 3456
					&& getMyPlayer().getLocation().getY() <= 3461)
				return true;
		}
		if (command.equals("South Falador - Ivy")) {
			if (getMyPlayer().getLocation().getX() < 3053
					&& getMyPlayer().getLocation().getX() >= 3044) {
			} else
				return false;
			if (getMyPlayer().getLocation().getY() >= 3326
					&& getMyPlayer().getLocation().getY() <= 3328)
				return true;
		}
		if (command.equals("North Falador - Ivy")) {
			if (getMyPlayer().getLocation().getX() < 3019
					&& getMyPlayer().getLocation().getX() >= 3010) {
			} else
				return false;
			if (getMyPlayer().getLocation().getY() >= 3392
					&& getMyPlayer().getLocation().getY() <= 3394)
				return true;
		}
		if (command.equals("Draynor - Willows")) {
			if (getMyPlayer().getLocation().getX() < 3092
					&& getMyPlayer().getLocation().getX() >= 3083) {
			} else
				return false;
			if (getMyPlayer().getLocation().getY() >= 3233
					&& getMyPlayer().getLocation().getY() <= 3239)
				return true;
		}
		if (command.equals("Port Salim - Willows")) {
			if (getMyPlayer().getLocation().getX() < 3064
					&& getMyPlayer().getLocation().getX() >= 3056) {
			} else
				return false;
			if (getMyPlayer().getLocation().getY() >= 3250
					&& getMyPlayer().getLocation().getY() <= 3256)
				return true;
		}
		if (command.equals("Seer's Village - Maples")) {
			if (getMyPlayer().getLocation().getX() < 2731
					&& getMyPlayer().getLocation().getX() >= 2727) {
			} else
				return false;
			if (getMyPlayer().getLocation().getY() >= 3499
					&& getMyPlayer().getLocation().getY() <= 3502)
				return true;
		}
		return false;
	}

	public void gate() {
		if (calc.tileToScreen(new RSTile(3348, 3279)).x != -1) {
			RSObject gate = getGate();
			while (isGateClosed()) {
				status = "Opening gate.";
				walking.walkTileMM(new RSTile(3348, 3279), 1, 1);
				RSModel m = gate.getModel();
				Point temp = m.getPoint();
				mouse.move(temp, 1, 1);
				if (menu.doAction("Open Gate"))
					status = "Gate opened.";
				sleep(random(1000, 1200));
			}
		}
	}

	public boolean isGateClosed() {
		RSObject[] test = objects.getAllAt(new RSTile(3348, 3279));
		for (int i = 0; i < test.length; i++) {
			if (test[i].getID() == 27852) {
				return true;
			}
		}
		return false;
	}

	public RSObject getGate() {
		RSObject[] test = objects.getAllAt(new RSTile(3348, 3279));
		RSObject gate = null;
		for (int i = 0; i < test.length; i++) {
			if (test[i].getID() == 27852) {
				gate = test[i];
			}
		}
		return gate;
	}

	public void walkToBank() {
		System.currentTimeMillis();
		status = "Walking to bank.";
		if (random(1, 3) == 2)
			camera.setPitch(true);
		int lastTileToWalkTo = -1;
		int tempTileToWalkTo = 0;
		while (!atBank()) {
			if (!walking.isRunEnabled() && walking.getEnergy() > 20) {
				walking.setRun(true);
				sleep(random(600, 800));
			}
			status = "Walking to bank..";
			if (command.equals("Mage Training Area - Magics")) {
				gate();
			}
			if (command.equals("Rimmington - Yews")) {
				if (!getMyPlayer().isMoving()
						&& (playerIsNear(new RSTile(2933, 3236), 3) || playerIsNear(
								tree4, 3))) {
					walking.walkTileMM(treesToBank[0], 1, 1);
					sleep(random(600, 800));
					while (getMyPlayer().isMoving())
						sleep(random(100, 200));
				}
			}
			if (command.equals("South Falador - Yews")) {
				if (playerIsNear(tree3, 4)) {
					walking.walkTileMM(new RSTile(3030, 3321), 1, 1);
					while (calc.tileToMinimap(tree2).getX() == -1) {
						sleep(random(100, 200));
					}
					walking.walkTileMM(tree2, 1, 1);
					sleep(random(1200, 1500));
					while (players.getMyPlayer().isMoving()) {
						sleep(random(100, 200));
					}
				}
			}
			if (command.equals("Tree Gnome - Yews")) {
				if (floorHeight() == 0) {
					if (!playerIsNear(new RSTile(2444, 3434), 4)) {
						walking.walkTileMM(new RSTile(2444, 3434));
						while (getMyPlayer().isMoving())
							sleep(random(100, 200));
					} else {
						for (int inc = 0; inc < 3; inc++) {
							RSObject stairs = null;
							RSObject[] tester = objects.getAllAt(new RSTile(
									2446, 3435));
							for (int i = 0; i < tester.length; i++) {
								if (tester[i].getID() == 1742)
									stairs = tester[i];
							}
							if (stairs != null) {
								RSModel m = stairs.getModel();
								Point toClick = m.getPoint();
								mouse.move(toClick);
								mouse.click(true);
								sleep(random(1000, 1500));
							}
						}
						if (floorHeight() == 1)
							break;
					}
				}
			}
			if (command.equals("Grand Exchange - Yews")) {
				if (playerIsNear(tree3, 5)) {
					walking.walkTileMM(tree2);
					sleep(random(600, 800));
				}
			}

			if (!getMyPlayer().isMoving()) {
				sleep(random(600, 800));
				if (!getMyPlayer().isMoving())
					lastTileToWalkTo = -1;
			}
			boolean clicked = false;
			tempTileToWalkTo = walkPath(treesToBank);
			if (tempTileToWalkTo > lastTileToWalkTo) {
				if (walking.walkTileMM(treesToBank[tempTileToWalkTo], 1, 1))
					clicked = true;
				sleep(random(400, 500));
				if (getMyPlayer().isMoving())
					clicked = true;
			}
			if (clicked) {
				lastTileToWalkTo = tempTileToWalkTo;
			}
			sleep(random(200, 300));
		}
	}

	public boolean compareTiles(RSTile t1, RSTile t2) {
		return t1.getX() == t2.getX() && t1.getY() == t2.getY();
	}

	public int walkPath(RSTile[] path) {
		// if(playerIsNear(path[path.length-2],5))
		// walking.walkTileMM(path[path.length-1],1,1);
		// else{
		int temp = 0;
		boolean nearSomething = false;
		for (int i = 0; i < path.length; i++) {
			if (playerIsNear(path[i], 5)) {
				nearSomething = true;
				temp = i + 1;
				antiBan(random(1, 200));
				for (int j = i; j < path.length; j++) {
					if (calc.tileToMinimap(path[j]).x < 0) {
						temp = j - 1;
						break;
					}
					if (j == path.length - 1
							&& calc.tileToMinimap(path[j]).x > 0) {
						temp = j;
						break;
					}
				}
				return temp;
			}
		}
		if (!nearSomething) {
			RSTile tempTileToCompare = walking.nextTile(path);
			for (int i = 0; i < path.length; i++) {
				if (compareTiles(path[i], tempTileToCompare))
					return i;
			}

		}
		return 0;
	}

	public RSItem[] getNestLocations() {
		RSItem[] inv = inventory.getItems();
		int length = 0;
		for (int i = 0; i < inv.length; i++)
			for (int j = 0; j < BNIDs.length; j++)
				if (inv[i].getID() == BNIDs[j])
					length++;
		RSItem[] toReturn = new RSItem[length];
		int index = 0;
		for (int i = 0; i < inv.length; i++)
			for (int j = 0; j < BNIDs.length; j++)
				if (inv[i].getID() == BNIDs[j])
					toReturn[index++] = inv[i];
		return toReturn;
	}

	public void bank(boolean npc) {
		mouse.setSpeed(random(7, 8));
		if (command.equals("Rimmington - Yews")
				|| command.equals("Port Salim - Willows")) {
			mouse.setSpeed(random(7, 8));
			boolean tryAgain = true;
			while (inventory.isFull()) {
				tryAgain = true;
				while (tryAgain) {
					status = "Opening bank.";
					RSObject[] test = objects.getAllAt(new RSTile(3047, 3237));
					RSObject bankBox = null;
					for (int i = 0; i < test.length; i++)
						if (test[i].getID() == 36788)
							bankBox = test[i];
					RSModel m = bankBox.getModel();
					Point p = m.getPoint();
					mouse.move(p, 1, 1);
					if (menu.doAction("Deposit Bank deposit box"))
						tryAgain = false;
				}
				sleep(random(3000, 4000));
				status = "Depositing.";
				RSItem[] tempArray = getNestLocations();
				if (tempArray.length != 0) {
					for (int i = 0; i < tempArray.length; i++)
						tempArray[i].doAction("Deposit Bird's nest");
				}
				bank.depositAllExcept(hatchetIDs);
			}

		} else if (npc) {
			RSNPC banker = npcs.getNearest(bankerID);
			if (banker != null) {
				do {
					while (!bank.isOpen()) {
						camera.setAngle(random(1, 359));
						status = "Opening bank.";
						if (location.equals("Grand Exchange")
								|| location.equals("Draynor")) {
							mouse.move(banker.getModel().getPoint(), 1, 1);
							mouse.click(false);
							menu.doAction("Bank banker");
							sleep(random(600, 800));
						}
						if (!bank.isOpen())
							bank.open();
					}
					if (inventory.getCount(hatchetIDs) > 0) {
						status = "Depositing.";
						RSItem[] tempArray = getNestLocations();
						if (tempArray.length != 0) {
							for (int i = 0; i < tempArray.length; i++)
								tempArray[i].doAction("Deposit Bird's nest");
						}
						bank.depositAllExcept(hatchetIDs);
					} else {
						status = "Depositing All.";
						bank.depositAll();
						sleep(random(600, 800));
						antiBan(random(100, 200));
					}
				} while (inventory.isFull());
				full = inventory.isFull();
				bank.close();
			} else
				log("Can't find banker.");
		} else {
			RSTile banker = bankLocation;
			if (calc.tileOnScreen(banker)) {
				do {
					while (!bank.isOpen()) {
						status = "Opening bank.";
						mouse.move(calc.tileToScreen(banker));
						sleep(random(600, 800));
						mouse.click(false);
						menu.doAction("Bank");
						sleep(random(1000, 2000));
					}
					if (inventory.getCount(hatchetIDs) > 0) {
						status = "Depositing.";
						RSItem[] tempArray = getNestLocations();
						if (tempArray.length != 0) {
							for (int i = 0; i < tempArray.length; i++)
								tempArray[i].doAction("Deposit Bird's nest");
						}
						bank.depositAllExcept(hatchetIDs);
					} else {
						status = "Depositing All.";
						bank.depositAll();
					}
					sleep(random(1000, 2000));
				} while (inventory.isFull());
				full = inventory.isFull();
			} else
				log("Can't find banker.");
		}
	}

	public void walkToTrees() {
		System.currentTimeMillis();
		status = "Walking to trees.";
		int lastTileToWalkTo = -1;
		int tempTileToWalkTo = walkPath(bankToTrees);
		while (!atTrees()) {
			if (!walking.isRunEnabled() && walking.getEnergy() > 20) {
				walking.setRun(true);
				sleep(random(600, 800));
			}
			status = "Walking to trees.";
			if (command.equals("Mage Training Area - Magics")) {
				gate();
			}
			if (command.equals("Tree Gnome - Yews")) {
				if (floorHeight() == 1) {
					if (!playerIsNear(new RSTile(2445, 3433), 5)) {
						walking.walkTileMM(new RSTile(2445, 3433));
						while (getMyPlayer().isMoving())
							sleep(random(100, 200));
					} else {
						RSObject temp = objects.getNearest(1744);
						if (temp != null)
							mouse.move(temp.getModel().getPoint());
						mouse.click(true);
						sleep(random(600, 800));
					}
				}
			}
			if (!getMyPlayer().isMoving()) {
				sleep(random(600, 800));
				if (!getMyPlayer().isMoving())
					lastTileToWalkTo = -1;
			}
			boolean clicked = false;
			tempTileToWalkTo = walkPath(bankToTrees);
			if (tempTileToWalkTo > lastTileToWalkTo) {
				if (walking.walkTileMM(bankToTrees[tempTileToWalkTo], 1, 1))
					clicked = true;
				sleep(random(400, 500));
				if (getMyPlayer().isMoving())
					clicked = true;
			}
			if (clicked) {
				lastTileToWalkTo = tempTileToWalkTo;
			}
			sleep(random(200, 300));
		}
	}

	public void doBank(int num) {
		if (num == 0) {
			walkToBank();
			sleep(random(600, 800));
		}
		if (!command.equals("Castle Wars - Ivy")
				&& !command.equals("Mage Training Area - Magics"))
			bank(true);
		else
			bank(false);
		sleep(random(600, 800));
		walkToTrees();
		sleep(random(600, 800));
	}

	@Override
	public void messageReceived(final MessageEvent a) {
		final String serverString = a.getMessage();
		if (serverString.toLowerCase().contains("you get some yew logs"))
			yewsCut++;
		else if (serverString.toLowerCase().contains("you get some magic logs"))
			magicsCut++;
		else if (serverString.toLowerCase().contains(
				"your inventory is too full to hold any more logs"))
			full = true;
		else if (serverString.toLowerCase().contains(
				"you successfully chop away some ivy"))
			ivyCut++;
		else if (serverString.toLowerCase()
				.contains("you get some willow logs"))
			willowsCut++;
		else if (serverString.toLowerCase().contains("you get some maple logs"))
			maplesCut++;
	}

	public boolean playerIsNear(RSTile tile, int d) {
		return (Math.abs(getMyPlayer().getLocation().getX() - tile.getX()) < d && Math
				.abs(getMyPlayer().getLocation().getY() - tile.getY()) < d);
	}

	public boolean checkCurrentVersion() {
		try {
			URL checkPage = new URL(
					"http://www.conderogascripts.99k.org/CChopVersion.txt");
			BufferedReader reader = new BufferedReader(new InputStreamReader(
					new BufferedInputStream(checkPage.openConnection()
							.getInputStream())));
			currentVersion = reader.readLine().trim();
			reader.close();
		} catch (Exception E) {
		}
		;
		if (currentVersion.equals(version))
			return true;
		else {
			log("Your version of the script is out of date.");
		}
		return false;
	}

	public boolean update() {
		try {

			saveUrl("http://www.conderogascripts.99k.org/CChop.txt",
					"CChop.java");

			log("Update was successful. The new CChop.java file is wherever your RSBot file is.");
			log("Copy the file into your RSBot/Scripts/Sources folder, compile, and restart the script.");
			stopScript();
		} catch (Exception e) {
			log("Update was unsuccessful.");
			return false;
		}
		;
		return true;
	}

	public void saveUrl(String urlString, String filename)
			throws MalformedURLException, IOException {
		BufferedInputStream in = null;
		FileOutputStream fout = null;
		try {
			in = new BufferedInputStream(new URL(urlString).openStream());
			fout = new FileOutputStream(filename);

			byte data[] = new byte[1024];
			int count;
			while ((count = in.read(data, 0, 1024)) != -1) {
				fout.write(data, 0, count);
			}
		} finally {
			if (in != null)
				in.close();
			if (fout != null)
				fout.close();
		}
	}

	@Override
	public void mouseExited(MouseEvent e) {
	}

	@Override
	public void mouseEntered(MouseEvent e) {
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		mouseColor = new Color(51, 153, 0, 255);
	}

	@Override
	public void mousePressed(MouseEvent e) {
		mouseColor = new Color(255, 0, 0, 100);
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		p = e.getPoint();
		if (showPaint && p.getX() > 447 && p.getX() < 513 && p.getY() > 458
				&& p.getY() < 473)
			showPaint = false;
		else if (!showPaint && p.getX() > 447 && p.getX() < 513
				&& p.getY() > 458 && p.getY() < 473)
			showPaint = true;
		else if (showPaint && fancyPaint && p.getX() > 397 && p.getX() < 448
				&& p.getY() > 458 && p.getY() < 473)
			fancyPaint = false;
		else if (showPaint && !fancyPaint && p.getX() > 397 && p.getX() < 448
				&& p.getY() > 458 && p.getY() < 473)
			fancyPaint = true;
		mouseColor = new Color(255, 0, 0, 100);
	}

	public int distanceTo(RSTile test) {
		RSTile myLoc = players.getMyPlayer().getLocation();
		return (int) Math.pow(
				Math.pow(test.getY() - myLoc.getY(), 2)
						+ Math.pow(test.getX() - myLoc.getX(), 2), .5);
	}

	public int distanceBetween(RSTile t1, RSTile t2) {
		return (int) Math.pow(
				Math.pow(t1.getY() - t2.getY(), 2)
						+ Math.pow(t1.getX() - t2.getX(), 2), .5);
	}

	public ArrayList<RSTile> fixPath(ArrayList<RSTile> path) {
		boolean madeSwap = false;
		do {
			madeSwap = false;
			for (int i = 0; i < path.size() - 1; i++)
				if (distanceBetween(path.get(i), path.get(i + 1)) > 7) {
					path.add(i + 1, getMidTile(path.get(i), path.get(i + 1)));
					madeSwap = true;
				}
		} while (madeSwap);
		return path;
	}

	public void getUnstuck() {
		log("Stuck! Moving to Closest Tree/Bank");
		RSTile[] trees = { tree1, tree2, tree3, tree4, tree5, tree6, tree7 };
		int[] distances = new int[8];
		for (int i = 0; i < 7; i++)
			distances[i] = distanceTo(trees[i]);
		if (treesToBank.length > 0)
			distances[7] = distanceTo(treesToBank[treesToBank.length - 1]);
		else
			distances[7] = 9999;
		int min = 9998;
		int minIndex = -1;
		for (int i = 0; i < distances.length; i++) {
			if (distances[i] < min) {
				min = distances[i];
				minIndex = i;
			}
		}
		// log("Moving to: "+minIndex);
		RSTile toWalkTo = (minIndex == 7) ? treesToBank[treesToBank.length - 1]
				: trees[minIndex];
		ArrayList<RSTile> path = new ArrayList<RSTile>();
		path.add(getMyPlayer().getLocation());
		path.add(toWalkTo);
		path = fixPath(path);
		RSTile[] toWalk = new RSTile[path.size()];
		for (int i = 0; i < path.size(); i++)
			toWalk[i] = path.get(i);
		// log("Path Length: "+path.size());
		long start = System.currentTimeMillis();
		int lastTileToWalkTo = -1;
		int tempTileToWalkTo = walkPath(toWalk);
		boolean clickedLastTile = false;
		while (!playerIsNear(toWalkTo, 5)
				&& System.currentTimeMillis() - start < 20000) {
			boolean clicked = false;
			tempTileToWalkTo = walkPath(toWalk);
			if (!clickedLastTile) {
				if (tempTileToWalkTo > lastTileToWalkTo) {
					if (calc.tileOnScreen(toWalk[tempTileToWalkTo])) {
						if (!walking.walkTileOnScreen(toWalk[tempTileToWalkTo]))
							if (walking.walkTileMM(toWalk[tempTileToWalkTo], 1,
									1))
								clicked = true;
							else
								clicked = false;
						else
							clicked = true;
					} else
						clicked = walking.walkTileMM(toWalk[tempTileToWalkTo],
								1, 1);

				}
				if (clicked) {
					lastTileToWalkTo = tempTileToWalkTo;
					if (tempTileToWalkTo == toWalk.length - 1) {
						clickedLastTile = true;
					}
				}
			}
			sleep(random(200, 300));
		}
	}

	public static int getGEValue(String name, int id) {
		try {
			String[] temp = name.split(" ");
			String newName = "";
			for (int i = 0; i < temp.length; i++)
				if (i == temp.length - 1)
					newName += temp[i];
				else
					newName += "_" + temp[i];
			URL url = new URL("http://services.runescape.com/m=itemdb_rs/"
					+ newName + "/viewitem.ws?obj=" + id);
			InputStream is = url.openStream();
			InputStreamReader isr = new InputStreamReader(is);
			BufferedReader br = new BufferedReader(isr);
			String line;
			String num = "";
			while ((line = br.readLine()) != null) {
				if (line.contains("Current guide price:"))
					num = line;
			}
			System.out.println(num);
			if (num.length() < 29)
				return -1;
			else {
				num = num.substring(28);
				num = num.replaceAll(",", "");
				return Integer.parseInt(num);
			}

		} catch (Exception e) {
			return -1;
		}
	}

	// LOOP====================================================================================================================
	@Override
	public int loop() {
		try {
			if (safety < 1) {
				if (!game.isFixed())
					for (int i = 0; i < 3; i++)
						log("For best results, please set your window mode to 'fixed'.");
				setUp();
				log("Chopping Command: " + command);
				if (treeType.equals("yews") || treeType.equals("magics")
						|| treeType.equals("willows")
						|| treeType.equals("maples") || treeType.equals("oaks")) {
					status = "Looking up GE data.";
					log("Looking up log prices...");
					if (treeType.equals("yews")) {
						for (int i = 0; i < 3; i++) {
							yewPrice = getGEValue("Yew logs", 1515);
							if (yewPrice != -1)
								break;
						}
						log("Yew log price: " + yewPrice);
					} else if (treeType.equals("magics")) {
						for (int i = 0; i < 3; i++) {
							magicPrice = getGEValue("Magic logs", 1513);
							if (magicPrice != -1)
								break;
						}
						log("Magic logs price: " + magicPrice);
					} else if (treeType.equals("willows")) {
						for (int i = 0; i < 3; i++) {
							willowPrice = getGEValue("Willow logs", 1519);
							if (willowPrice != -1)
								break;
						}
						log("Willow logs price: " + willowPrice);
					} else if (treeType.equals("maples")) {
						for (int i = 0; i < 3; i++) {
							maplePrice = getGEValue("Maple logs", 1517);
							if (maplePrice != -1)
								break;
						}
						log("Maple logs price: " + maplePrice);
					} else if (treeType.equals("oaks")) {
						for (int i = 0; i < 3; i++) {
							oakPrice = getGEValue("Oak logs", 1521);
							if (oakPrice != -1)
								break;
						}
						log("Oak logs price: " + oakPrice);
					}
					camera.setPitch(true);
				} else
					camera.setPitch(false);
				safety = 1;
				full = inventory.isFull();
				if (!full && atBank())
					while (!atTrees())
						walkToTrees();
				if (full && atBank())
					doBank(1);
				if (full && atTrees())
					doBank(0);
			}

			RSTile[] trees = { tree1, tree2, tree3, tree4, tree5, tree6, tree7 };
			mouse.setSpeed(random(5, 8));
			antiBan(random(1, 120));
			updateStatus();
			full = inventory.isFull();

			if (!full) // NEST PICKUP
				pickUpNest();
			if (full && !atBank()) {
				walkToBank();
			} else if (!full && atBank()) {
				walkToTrees();
			} else if (full) // BANK
			{
				if (!location.equals("Taverly"))
					doBank(0);
			}
			if (status.equals("Chopping!")) {
				if (safety < 3) {
					hoverMouse();
					safety++;
				}
			} else if (status.equals("Not chopping.")
					|| status.contains("Moving to tree")) {
				long tempTimer = System.currentTimeMillis();
				while (!getNextTree().isOnScreen()
						&& System.currentTimeMillis() - tempTimer < 5000)
					moveToNextTree();
				if (!getNextTree().isOnScreen()) {
					// log("else");
					if (!getMyPlayer().isMoving() && failSafeTimer == 0)
						failSafeTimer = System.currentTimeMillis();
					if (!getMyPlayer().isMoving()
							&& System.currentTimeMillis() - failSafeTimer > 10000) {
						getUnstuck();
						failSafeTimer = 0;
					}
				}
				chop(false);
			} else if (status.equals("Waiting.")) {
				if (safety == 3) {
					System.currentTimeMillis();
					while (!playerIsNear(trees[nextTree - 1], 4))
						moveToNextTree();
					if (!chopType.equals("ivy"))
						camera.turnTo(trees[nextTree - 1]);
					camera.setPitch(random(20, 50));
				}
				if (safety < 5)
					hoverMouse();
				safety++;
			} else {

			}
		} catch (Exception e) {/*
								 * log("Error: "+e); log(e.getMessage()+"");
								 */
		}
		return random(300, 500);
	}

	// LOOP====================================================================================================================
	public void antiBan(int rand) {
		status += "(AB)";
		mouse.setSpeed(random(7, 8));
		if (rand == 1)
			if (random(1, 8) == 2)
				mouse.moveRandomly(900, 1200);
		if (rand == 2)
			if (random(1, 8) == 2)
				mouse.moveRandomly(400, 800);
		if (rand == 3)
			if (random(1, 8) == 2)
				mouse.moveRandomly(200, 700);

		if (rand == 4) // THIS CHECKS THE WOODCUTTING STAT
			if (random(1, 12) == 2) {
				game.openTab(Game.TAB_STATS);
				sleep(random(600, 800));
				mouse.move(random(681, 690), random(365, 370), 0, 0);
				sleep(random(1900, 2000));
				game.openTab(Game.TAB_INVENTORY);
			}
		if (rand == 5) // THIS CLICKS THE XP BUTTON UNDER THE COMPASS
			if (random(1, 12) == 2 && game.isFixed()) {
				mouse.move(random(527, 540), random(58, 65), 0, 0);
				sleep(random(800, 1000));
				mouse.click(true);
				mouse.moveRandomly(20, 50);
				sleep(random(1000, 1500));
			}
		if (takeBreaks)
			if (rand == 6)
				if (random(1, 6) == 2) {
					if (random(1, 2) == 1)
						mouse.moveRandomly(50, 75);
					breakActive = true;
					breakLength = random(5, 13);
					breakStart = System.currentTimeMillis();
					sleep(breakLength * 1000);
					breakActive = false;
				}
		if (rand == 7) // RANDOM SPIN
			if (random(1, 3) == 2) {
				int r = random(1, 2);
				camera.setAngle(random(1, 359));
				if (r != 1)
					camera.setPitch(random(1, 99));
			}

		if (rand == 8) // THIS CHECKS A RANDOM TAB
			if (random(1, 4) == 2) {
				int[] tabs = { 0, 2, 3, 5, 6, 7, 9, 10, 11, 12, 13, 14, 15 };
				game.openTab(tabs[random(0, tabs.length - 1)]);
				mouse.moveRandomly(175, 200);
				sleep(random(1600, 1800));
				sleep(random(900, 1000));

			}
		if (rand == 9)
			if (random(1, 10) == 2)
				mouse.moveOffScreen();
		status = status.substring(0, status.indexOf('('));

	}

	@Override
	public void onRepaint(Graphics g) {
		if (game.isLoggedIn()) {
			try {
				if (breakActive) {
					g.setColor(new Color(0, 0, 0, 100));
					g.fillRect(0, 0, 764, 501);
					g.setColor(new Color(255, 0, 0));
					long timeLeft = (breakLength * 1000)
							- (System.currentTimeMillis() - breakStart);
					long secondsLeft = timeLeft / 1000;
					timeLeft -= secondsLeft * 1000;
					String toOutput = "";
					if (timeLeft < 10)
						toOutput = "00" + timeLeft;
					else if (timeLeft < 100)
						toOutput = "0" + timeLeft;
					else
						toOutput = "" + timeLeft;

					timeLeft -= secondsLeft * 1000;
					g.drawString("Script will resume in: " + secondsLeft + ":"
							+ toOutput, 50, 200);
				}
				if (!treeType.equals("ivy") && !treeType.equals(".")
						&& !treeType.equals("willows"))
					checkTrees();
				else
					checkIvy();
				calculateNextTree();
				if (startExp == 0) {
					startExp = skills.getCurrentExp(Skills
							.getIndex("woodcutting"));
					startLevel = skills.getCurrentLevel(Skills
							.getIndex("woodcutting"));
				}
				lvlsGained = skills.getCurrentLevel(Skills
						.getIndex("woodcutting")) - startLevel;
				expGained = skills
						.getCurrentExp(Skills.getIndex("woodcutting"))
						- startExp;
				long expToLvl = skills.getExpToNextLevel(Skills
						.getIndex("woodcutting"));
				// setting up the time
				long ms = System.currentTimeMillis() - startTime;
				double ms2 = System.currentTimeMillis() - startTimeDbl;
				long hours = ms / 3600000;
				ms = ms - (hours * 3600000);
				long minutes = ms / 60000;
				ms = ms - (minutes * 60000);
				long seconds = ms / 1000;
				long time2Lvl = 0;
				long time2LvlHrs = 0;
				long time2LvlMins = 0;
				long time2LvlSec = 0;
				if (ms2 != 0 && expGained != 0) {
					time2Lvl = (long) (expToLvl / (expGained / (ms2 / 3600000)) * 3600000);
					time2LvlHrs = time2Lvl / 3600000;
					time2Lvl -= time2LvlHrs * 3600000;
					time2LvlMins = time2Lvl / 60000;
					time2Lvl -= time2LvlMins * 60000;
					time2LvlSec = time2Lvl / 1000;
				}
				yewsCut = expGained / 175;
				magicsCut = expGained / 250;
				ivyCut = (int) ((expGained + 1) / 332.5);
				willowsCut = (int) ((expGained + 1) / 67.5);
				maplesCut = expGained / 100;
				oaksCut = (int) ((expGained + 1) / 37.5);

				if (fancyPaint) {
					if (showPaint) {
						onRepaint2(g, true);

						// Text Color and Output
						g.setColor(new Color(0, 0, 0, 255));
						g.setFont(new Font("Comic Sans MS", Font.PLAIN, 12));
						g.drawString("  " + version, 450, 360);
						g.setFont(new Font("Comic Sans MS", Font.PLAIN, 11));
						g.drawString("Levels Gained: " + lvlsGained, 16, 40);
						if (treeType.equals("yews"))
							g.drawString("Yews Chopped: " + yewsCut, 395, 22);
						else if (treeType.equals("magics"))
							g.drawString("Magics Chopped: " + magicsCut, 395,
									22);
						else if (treeType.equals("ivy"))
							g.drawString("Ivy Chopped: " + ivyCut, 395, 22);
						else if (treeType.equals("willows"))
							g.drawString("Willows Chopped: " + willowsCut, 395,
									22);
						else if (treeType.equals("maples"))
							g.drawString("Maples Chopped: " + maplesCut, 395,
									22);
						else if (treeType.equals("oaks"))
							g.drawString("Oaks Chopped: " + oaksCut, 395, 22);
						g.drawString("Exp Gained: " + expGained, 16, 55);
						g.drawString("Time Running: " + hours + ":" + minutes
								+ ":" + seconds, 16, 97);

						// Determine Length:
						double difference = Skills.XP_TABLE[skills
								.getCurrentLevel(Skills.getIndex("woodcutting")) + 1]
								- Skills.XP_TABLE[skills.getCurrentLevel(Skills
										.getIndex("woodcutting"))];
						double barLength = ((difference - expToLvl) / difference) * 512;

						// Progress Bar
						g.setColor(new Color(255, 0, 0, 255));
						g.fillRoundRect(4, 320, 512, 18, 8, 8); // Bar
																// background
						g.setColor(new Color(51, 153, 0, 255)); // GREEN
						g.fillRoundRect(4, 320, (int) barLength, 18, 4, 4);
						g.setColor(new Color(0, 0, 0, 255));
						g.drawString(
								skills.getPercentToNextLevel(Skills
										.getIndex("woodcutting"))
										+ "% to: "
										+ (skills.getCurrentLevel(Skills
												.getIndex("woodcutting")) + 1)
										+ " (" + expToLvl + " exp)", 215, 334);
						g.setColor(new Color(255, 255, 255, 100));
						g.fillRoundRect(4, 320, 512, 9, 4, 4);

						if (ms2 != 0)
							g.setColor(new Color(0, 0, 0, 255));
						g.drawString("Exp/Hr: "
								+ (int) (expGained / (ms2 / 3600000)), 16, 69);
						g.drawString("Status: " + status, 16, 22);

						if (treeType.equals("yews")) {
							g.drawString("Money Gained: " + yewPrice * yewsCut,
									154, 22);
							if (ms2 != 0)
								g.drawString(
										"Money/Hr: "
												+ (int) ((yewPrice * yewsCut) / (ms2 / 3600000)),
										289, 22);
						} else if (treeType.equals("magics")) {
							g.drawString("Money Gained: " + magicPrice
									* magicsCut, 154, 22);
							if (ms2 != 0)
								g.drawString(
										"Money/Hr: "
												+ (int) ((magicPrice * magicsCut) / (ms2 / 3600000)),
										289, 22);
						} else if (treeType.equals("ivy")) {
							g.drawString("Nests Collected: " + nests, 154, 22);
							if (ms2 != 0)
								g.drawString("Nests/Hr: "
										+ (int) (nests / (ms2 / 3600000)), 289,
										22);
						} else if (treeType.equals("willows")) {
							g.drawString("Money Gained: " + willowPrice
									* willowsCut, 154, 22);
							if (ms2 != 0)
								g.drawString(
										"Money/Hr: "
												+ (int) ((willowPrice * willowsCut) / (ms2 / 3600000)),
										289, 22);
						} else if (treeType.equals("maples")) {
							g.drawString("Money Gained: " + maplePrice
									* maplesCut, 154, 22);
							if (ms2 != 0)
								g.drawString(
										"Money/Hr: "
												+ (int) ((maplePrice * maplesCut) / (ms2 / 3600000)),
										289, 22);
						} else if (treeType.equals("oaks")) {
							g.drawString("Money Gained: " + oakPrice * oaksCut,
									154, 22);
							if (ms2 != 0)
								g.drawString(
										"Money/Hr: "
												+ (int) ((oakPrice * oaksCut) / (ms2 / 3600000)),
										289, 22);
						}
						g.drawString("Est. Time to Lvl: " + time2LvlHrs + ":"
								+ time2LvlMins + ":" + time2LvlSec, 16, 83);
						// Mouse Stuff
						Point tempPoint = mouse.getLocation();
						int tempXCoordinate = (int) tempPoint.getX();
						int tempYCoordinate = (int) tempPoint.getY();
						g.setColor(mouseColor);
						g.drawLine(tempXCoordinate, 0, tempXCoordinate, 501);
						g.drawLine(0, tempYCoordinate, 764, tempYCoordinate);
						g.fillRect(tempXCoordinate - 1, tempYCoordinate - 1, 3,
								3);
						Color tempColor = new Color(255, 0, 0, 100);
						if (mouseColor.equals(tempColor))
							mouseColor = new Color(51, 153, 0, 255);
						// Stump time drawing
						g.setColor(Color.GREEN);
						stump1 = calc.tileToScreen(tree1);
						stump2 = calc.tileToScreen(tree2);
						stump3 = calc.tileToScreen(tree3);
						stump4 = calc.tileToScreen(tree4);
						stump5 = calc.tileToScreen(tree5);
						stump6 = calc.tileToScreen(tree6);
						stump7 = calc.tileToScreen(tree7);
						// ALIVE TREES
						if (tree1Status && tree1AliveTime != 0)
							g.drawString((int) (tree1TimeAlive / 1000) + "s",
									(int) stump1.getX(), (int) stump1.getY());
						if (tree2Status && tree2AliveTime != 0)
							g.drawString((int) (tree2TimeAlive / 1000) + "s",
									(int) stump2.getX(), (int) stump2.getY());
						if (tree3Status && tree3AliveTime != 0)
							g.drawString((int) (tree3TimeAlive / 1000) + "s",
									(int) stump3.getX(), (int) stump3.getY());
						if (tree4Status && tree4AliveTime != 0)
							g.drawString((int) (tree4TimeAlive / 1000) + "s",
									(int) stump4.getX(), (int) stump4.getY());
						if (tree5Status && tree5AliveTime != 0)
							g.drawString((int) (tree5TimeAlive / 1000) + "s",
									(int) stump5.getX(), (int) stump5.getY());
						if (tree6Status && tree6AliveTime != 0)
							g.drawString((int) (tree6TimeAlive / 1000) + "s",
									(int) stump6.getX(), (int) stump6.getY());
						if (tree7Status && tree7AliveTime != 0)
							g.drawString((int) (tree7TimeAlive / 1000) + "s",
									(int) stump7.getX(), (int) stump7.getY());
						// DEAD TREES
						g.setColor(Color.MAGENTA);
						if (!tree1Status && tree1DeadTime != 0)
							g.drawString((int) (tree1TimeDead / 1000) + "s",
									(int) stump1.getX(), (int) stump1.getY());
						if (!tree2Status && tree2DeadTime != 0)
							g.drawString((int) (tree2TimeDead / 1000) + "s",
									(int) stump2.getX(), (int) stump2.getY());
						if (!tree3Status && tree3DeadTime != 0)
							g.drawString((int) (tree3TimeDead / 1000) + "s",
									(int) stump3.getX(), (int) stump3.getY());
						if (!tree4Status && tree4DeadTime != 0)
							g.drawString((int) (tree4TimeDead / 1000) + "s",
									(int) stump4.getX(), (int) stump4.getY());
						if (!tree5Status && tree5DeadTime != 0)
							g.drawString((int) (tree5TimeDead / 1000) + "s",
									(int) stump5.getX(), (int) stump5.getY());
						if (!tree6Status && tree6DeadTime != 0)
							g.drawString((int) (tree6TimeDead / 1000) + "s",
									(int) stump6.getX(), (int) stump6.getY());
						if (!tree7Status && tree7DeadTime != 0)
							g.drawString((int) (tree7TimeDead / 1000) + "s",
									(int) stump7.getX(), (int) stump7.getY());
					} else {
						g.setColor(new Color(51, 153, 0, 255));
						g.fillRect(448, 459, (512 - 448), (472 - 459));
						g.setColor(new Color(0, 0, 0, 255));
						g.drawRect(448, 459, (512 - 448), (472 - 459));
						g.drawString("Hide/Show", 450, 470);
					}

				} else {
					if (showPaint) {
						onRepaint2(g, false);
						g.setColor(new Color(0, 0, 0, 205));
						g.fillRoundRect(333, 160, 181, 179, 6, 6);
						g.setColor(new Color(255, 0, 0, 255));
						g.setFont(new Font("Comic Sans MS", Font.PLAIN, 12));
						g.drawString("Conderoga's Chopper " + version, 338, 177);
						g.setFont(new Font("Comic Sans MS", Font.PLAIN, 11));
						g.drawString("Levels Gained: " + lvlsGained, 344, 192);
						if (treeType.equals("yews"))
							g.drawString("Yews Chopped: " + yewsCut, 344, 207);
						else if (treeType.equals("magics"))
							g.drawString("Magics Chopped: " + magicsCut, 344,
									207);
						else if (treeType.equals("ivy"))
							g.drawString("Ivy Chopped: " + ivyCut, 344, 207);
						else if (treeType.equals("willows"))
							g.drawString("Willows Chopped: " + willowsCut, 344,
									207);
						else if (treeType.equals("maples"))
							g.drawString("Maples Chopped: " + maplesCut, 344,
									207);
						else if (treeType.equals("oaks"))
							g.drawString("Oaks Chopped: " + oaksCut, 344, 207);
						g.drawString("Exp Gained: " + expGained, 344, 222);
						g.drawString("Time Running: " + hours + ":" + minutes
								+ ":" + seconds, 344, 237);

						// Progress Bar

						g.setColor(new Color(255, 0, 0, 255));
						g.fillRoundRect(344, 241, 150, 20, 8, 8); // Bar
																	// background
						g.setColor(new Color(0, 255, 0, 255)); // GREEN
						g.fillRoundRect(344, 241, (int) (skills
								.getPercentToNextLevel(Skills
										.getIndex("woodcutting")) * 1.5), 20,
								8, 8);
						g.setColor(new Color(255, 255, 255, 100));
						g.drawString(
								skills.getPercentToNextLevel(Skills
										.getIndex("woodcutting"))
										+ "% to: "
										+ (skills.getCurrentLevel(Skills
												.getIndex("woodcutting")) + 1)
										+ " (" + expToLvl + " exp)", 348, 256);
						g.fillRoundRect(345, 251, 148, 10, 8, 8);
						g.setColor(new Color(0, 0, 0, 255));
						g.drawString(
								skills.getPercentToNextLevel(Skills
										.getIndex("woodcutting"))
										+ "% to: "
										+ (skills.getCurrentLevel(Skills
												.getIndex("woodcutting")) + 1)
										+ " (" + expToLvl + " exp)", 347, 255);
						g.setColor(new Color(255, 0, 0, 255));
						if (ms2 != 0)
							g.drawString("Exp/Hr: "
									+ (int) (expGained / (ms2 / 3600000)), 344,
									274);
						g.drawString("Status: " + status, 344, 289);

						if (treeType.equals("yews")) {
							g.drawString("Money Gained: " + yewPrice * yewsCut,
									344, 304);
							if (ms2 != 0)
								g.drawString(
										"Money/Hr: "
												+ (int) ((yewPrice * yewsCut) / (ms2 / 3600000)),
										344, 319);
						} else if (treeType.equals("magics")) {
							g.drawString("Money Gained: " + magicPrice
									* magicsCut, 344, 304);
							if (ms2 != 0)
								g.drawString(
										"Money/Hr: "
												+ (int) ((magicPrice * magicsCut) / (ms2 / 3600000)),
										344, 319);
						} else if (treeType.equals("ivy")) {
							g.drawString("Nests Collected: " + nests, 344, 304);
							if (ms2 != 0)
								g.drawString("Nests/Hr: "
										+ (int) (nests / (ms2 / 3600000)), 344,
										319);
						} else if (treeType.equals("willows")) {
							g.drawString("Money Gained: " + willowPrice
									* willowsCut, 344, 304);
							if (ms2 != 0)
								g.drawString(
										"Money/Hr: "
												+ (int) ((willowPrice * willowsCut) / (ms2 / 3600000)),
										344, 319);
						} else if (treeType.equals("maples")) {
							g.drawString("Money Gained: " + maplePrice
									* maplesCut, 344, 304);
							if (ms2 != 0)
								g.drawString(
										"Money/Hr: "
												+ (int) ((maplePrice * maplesCut) / (ms2 / 3600000)),
										344, 319);
						} else if (treeType.equals("oaks")) {
							g.drawString("Money Gained: " + oakPrice * oaksCut,
									344, 304);
							if (ms2 != 0)
								g.drawString(
										"Money/Hr: "
												+ (int) ((oakPrice * oaksCut) / (ms2 / 3600000)),
										344, 319);
						}
						g.drawString("Est. Time to Lvl: " + time2LvlHrs + ":"
								+ time2LvlMins + ":" + time2LvlSec, 344, 334);
						// Mouse Stuff
						Point tempPoint = mouse.getLocation();
						int tempXCoordinate = (int) tempPoint.getX();
						int tempYCoordinate = (int) tempPoint.getY();
						g.setColor(new Color(0, 255, 0, 100));
						g.drawLine(tempXCoordinate, 0, tempXCoordinate, 501);
						g.drawLine(0, tempYCoordinate, 764, tempYCoordinate);
						// Stump time drawing
						g.setColor(Color.GREEN);
						stump1 = calc.tileToScreen(tree1);
						stump2 = calc.tileToScreen(tree2);
						stump3 = calc.tileToScreen(tree3);
						stump4 = calc.tileToScreen(tree4);
						stump5 = calc.tileToScreen(tree5);
						stump6 = calc.tileToScreen(tree6);
						stump7 = calc.tileToScreen(tree7);
						// ALIVE TREES
						if (tree1Status && tree1AliveTime != 0)
							g.drawString((int) (tree1TimeAlive / 1000) + "s",
									(int) stump1.getX(), (int) stump1.getY());
						if (tree2Status && tree2AliveTime != 0)
							g.drawString((int) (tree2TimeAlive / 1000) + "s",
									(int) stump2.getX(), (int) stump2.getY());
						if (tree3Status && tree3AliveTime != 0)
							g.drawString((int) (tree3TimeAlive / 1000) + "s",
									(int) stump3.getX(), (int) stump3.getY());
						if (tree4Status && tree4AliveTime != 0)
							g.drawString((int) (tree4TimeAlive / 1000) + "s",
									(int) stump4.getX(), (int) stump4.getY());
						if (tree5Status && tree5AliveTime != 0)
							g.drawString((int) (tree5TimeAlive / 1000) + "s",
									(int) stump5.getX(), (int) stump5.getY());
						if (tree6Status && tree6AliveTime != 0)
							g.drawString((int) (tree6TimeAlive / 1000) + "s",
									(int) stump6.getX(), (int) stump6.getY());
						if (tree7Status && tree7AliveTime != 0)
							g.drawString((int) (tree7TimeAlive / 1000) + "s",
									(int) stump7.getX(), (int) stump7.getY());
						// DEAD TREES
						g.setColor(Color.MAGENTA);
						if (!tree1Status && tree1DeadTime != 0)
							g.drawString((int) (tree1TimeDead / 1000) + "s",
									(int) stump1.getX(), (int) stump1.getY());
						if (!tree2Status && tree2DeadTime != 0)
							g.drawString((int) (tree2TimeDead / 1000) + "s",
									(int) stump2.getX(), (int) stump2.getY());
						if (!tree3Status && tree3DeadTime != 0)
							g.drawString((int) (tree3TimeDead / 1000) + "s",
									(int) stump3.getX(), (int) stump3.getY());
						if (!tree4Status && tree4DeadTime != 0)
							g.drawString((int) (tree4TimeDead / 1000) + "s",
									(int) stump4.getX(), (int) stump4.getY());
						if (!tree5Status && tree5DeadTime != 0)
							g.drawString((int) (tree5TimeDead / 1000) + "s",
									(int) stump5.getX(), (int) stump5.getY());
						if (!tree6Status && tree6DeadTime != 0)
							g.drawString((int) (tree6TimeDead / 1000) + "s",
									(int) stump6.getX(), (int) stump6.getY());
						if (!tree7Status && tree7DeadTime != 0)
							g.drawString((int) (tree7TimeDead / 1000) + "s",
									(int) stump7.getX(), (int) stump7.getY());

					} else {
						g.setColor(new Color(51, 153, 0, 255));
						g.fillRect(448, 459, (512 - 448), (472 - 459));
						g.setColor(new Color(0, 0, 0, 255));
						g.drawRect(448, 459, (512 - 448), (472 - 459));
						g.drawString("Hide/Show", 450, 470);
					}
				}
			} catch (Exception e) {
			}
			;
		}
	}

	private Image getImage(String url) {
		try {
			return ImageIO.read(new URL(url));
		} catch (IOException e) {
			return null;
		}
	}

	public void onRepaint2(Graphics g1, boolean fancy) {
		Graphics2D g = (Graphics2D) g1;
		if (fancy) {
			g.drawImage(img1, 342, 350, null);
			g.drawImage(img2, 5, 6, null);
			g.drawImage(img4, 5, 29, null);
		}
		g.setColor(new Color(51, 153, 0, 255));
		g.fillRect(448, 459, (512 - 448), (472 - 459));
		g.fillRect(398, 459, (448 - 398), (472 - 459));
		g.setColor(new Color(0, 0, 0, 255));
		g.drawRect(448, 459, (512 - 448), (472 - 459));
		g.drawRect(398, 459, (448 - 398), (472 - 459));
		g.drawString("Hide/Show", 450, 470);
		if (fancy)
			g.drawString("Simple", 405, 470);
		else
			g.drawString("Adv.", 415, 470);
	}

	@Override
	public void onFinish() {
		log("Exp gained: " + expGained);
		log("Levels gained: " + lvlsGained);
		log("Thanks for using Conderoga's Chopper!");
	}

	public void openURL(final String url) { // Credits ZombieKnight
		// who gave credits to Dave who gave credits
		// to
		// some guy who made this.
		final String osName = System.getProperty("os.name");
		try {
			if (osName.startsWith("Mac OS")) {
				final Class<?> fileMgr = Class
						.forName("com.apple.eio.FileManager");
				final Method openURL = fileMgr.getDeclaredMethod("openURL",
						new Class[] { String.class });
				openURL.invoke(null, new Object[] { url });
			} else if (osName.startsWith("Windows")) {
				Runtime.getRuntime().exec(
						"rundll32 url.dll,FileProtocolHandler " + url);
			} else { // assume Unix or Linux
				final String[] browsers = { "firefox", "opera", "konqueror",
						"epiphany", "mozilla", "netscape" };
				String browser = null;
				for (int count = 0; count < browsers.length && browser == null; count++) {
					if (Runtime.getRuntime()
							.exec(new String[] { "which", browsers[count] })
							.waitFor() == 0) {
						browser = browsers[count];
					}
				}
				if (browser == null) {
					throw new Exception("Could not find web browser");
				} else {
					Runtime.getRuntime().exec(new String[] { browser, url });
				}
			}
		} catch (final Exception e) {
		}
	}

	public class CChopGUI extends JFrame {
		private static final long serialVersionUID = 1L;

		public CChopGUI() {
			initComponents();
		}

		private void button4ActionPerformed(ActionEvent e) {
			try {
				guiWait = false;
				guiExit = true;
				dispose();
			} catch (Exception ex) {
				log("WTF?!?");
			}
			;
		}

		private void button2ActionPerformed(ActionEvent e) {
			try {
				location = comboBox2.getSelectedItem().toString();
				takeBreaks = checkBox1.isSelected();
				nearest = true;
				guiExit = false;
				guiWait = false;
				dispose();
			} catch (Exception ex) {
				log("WTF?!?1");
			}
			;
		}

		private void button1ActionPerformed(ActionEvent e) {
			try {
				chopType = comboBox1.getSelectedItem().toString();
				initializeStage2Components();
			} catch (Exception ex) {
				log("WTF?!?2");
			}
			;
		}

		/*
		 * private void button3ActionPerformed(ActionEvent e) {
		 * initComponents(); chopType = null; }
		 */
		private void initComponents() {
			label1 = new JLabel();
			label2 = new JLabel();
			label3 = new JLabel();
			button1 = new JButton();
			comboBox1 = new JComboBox();
			button4 = new JButton();

			// ======== this ========
			setAlwaysOnTop(true);
			setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
			setFont(new Font("Dialog", Font.PLAIN, 16));
			setResizable(false);
			setTitle("Conderoga's Chopper");
			Container contentPane = getContentPane();
			contentPane.setLayout(null);

			// ---- label1 ----
			label1.setText("Conderoga's Chopper Settings");
			label1.setFont(new Font("Comic Sans MS", Font.PLAIN, 20));
			contentPane.add(label1);
			label1.setBounds(25, 15, 315, 39);

			// ---- label2 ----
			label2.setText("Version: " + version);
			contentPane.add(label2);
			label2.setBounds(new Rectangle(new Point(25, 50), label2
					.getPreferredSize()));

			// ---- label3 ----
			label3.setText("Select what you wish to chop:");
			label3.setFont(label3.getFont().deriveFont(
					label3.getFont().getSize() + 2f));
			contentPane.add(label3);
			label3.setBounds(new Rectangle(new Point(10, 105), label3
					.getPreferredSize()));

			// ---- button1 ----
			button1.setText("Confirm and Proceed");
			button1.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					button1ActionPerformed(e);
				}
			});
			contentPane.add(button1);
			button1.setBounds(85, 145, 195, 35);

			// ---- comboBox1 ----
			comboBox1.setMaximumRowCount(6);
			contentPane.add(comboBox1);
			comboBox1.setModel(new DefaultComboBoxModel(new String[] {

			"Yews", "Magics", "Ivy", "Willows", "Maples", "Oaks"

			}));
			comboBox1.setBounds(205, 100, 145, 25);

			// ---- button4 ----
			button4.setText("Exit");
			button4.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					button4ActionPerformed(e);
				}
			});
			contentPane.add(button4);
			button4.setBounds(0, 325, 85, 38);

			{ // compute preferred size
				Dimension preferredSize = new Dimension();
				for (int i = 0; i < contentPane.getComponentCount(); i++) {
					Rectangle bounds = contentPane.getComponent(i).getBounds();
					preferredSize.width = Math.max(bounds.x + bounds.width,
							preferredSize.width);
					preferredSize.height = Math.max(bounds.y + bounds.height,
							preferredSize.height);
				}
				Insets insets = contentPane.getInsets();
				preferredSize.width += insets.right;
				preferredSize.height += insets.bottom;
				contentPane.setMinimumSize(preferredSize);
				contentPane.setPreferredSize(preferredSize);
			}
			pack();
			setLocationRelativeTo(getOwner());

		}

		private void initializeStage2Components() {
			checkBox1 = new JCheckBox("Take short breaks?", false);
			// checkBox2 = new JCheckBox("Chop nearest?",true);
			label4 = new JLabel();
			comboBox2 = new JComboBox();
			button2 = new JButton();
			label1 = new JLabel();
			label2 = new JLabel();
			button4 = new JButton();

			// ======== this ========
			setAlwaysOnTop(true);
			setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
			setFont(new Font("Dialog", Font.PLAIN, 16));
			setResizable(false);
			setTitle("Conderoga's Chopper");
			Container contentPane = getContentPane();
			contentPane.setLayout(null);

			// ---- label1 ----
			label1.setText("Conderoga's Chopper Settings");
			label1.setFont(new Font("Comic Sans MS", Font.PLAIN, 20));
			contentPane.add(label1);
			label1.setBounds(25, 15, 315, 39);

			// ---- label2 ----
			label2.setText("Version: " + version);
			contentPane.add(label2);
			label2.setBounds(new Rectangle(new Point(25, 50), label2
					.getPreferredSize()));

			// ---- label4 ----
			label4.setText("Select where you wish to chop:");
			label4.setFont(label4.getFont().deriveFont(
					label4.getFont().getSize() + 2f));
			contentPane.add(label4);
			label4.setBounds(10, 210, 180, 16);

			// ---- comboBox2 ----
			if (chopType.equals("Yews")) {
				comboBox2.setMaximumRowCount(8);
				comboBox2.setModel(new DefaultComboBoxModel(new String[] {

				"Grand Exchange", "Edgeville", "Rimmington", "Catherby",
						"Seer's Village", "South Falador", "Draynor",
						"Tree Gnome"

				}));
			} else if (chopType.equals("Magics")) {
				comboBox2.setMaximumRowCount(3);
				comboBox2.setModel(new DefaultComboBoxModel(new String[] {

				"Seer's Village", "Sorcerer's Tower", "Mage Training Area"

				}));
			} else if (chopType.equals("Ivy")) {
				comboBox2.setMaximumRowCount(8);
				comboBox2.setModel(new DefaultComboBoxModel(new String[] {

				"Castle Wars", "Grand Exchange", "Taverly", "Yanille",
						"Varrock Palace", "Ardougne", "South Falador",
						"North Falador"

				}));
			} else if (chopType.equals("Willows")) {
				comboBox2.setMaximumRowCount(2);
				comboBox2.setModel(new DefaultComboBoxModel(new String[] {

				"Draynor", "Port Salim"

				}));
			} else if (chopType.equals("Maples")) {
				comboBox2.setMaximumRowCount(1);
				comboBox2.setModel(new DefaultComboBoxModel(new String[] {

				"Seer's Village"

				}));
			} else if (chopType.equals("Oaks")) {
				comboBox2.setMaximumRowCount(1);
				comboBox2.setModel(new DefaultComboBoxModel(new String[] {

				"Draynor"

				}));
			}
			contentPane.add(comboBox2);
			comboBox2.setBounds(205, 205, 145, 25);

			// ---- checkBox1 ----
			checkBox1.setText("Take short breaks?");
			contentPane.add(checkBox1);
			checkBox1.setBounds(new Rectangle(new Point(120, 250), checkBox1
					.getPreferredSize()));

			// ---- checkBox2 ----
			// checkBox2.setText("Chop nearest?");
			// contentPane.add(checkBox2);
			// checkBox2.setBounds(new Rectangle(new Point(120, 270),
			// checkBox1.getPreferredSize()));

			// ---- button2 ----
			button2.setText("Start");
			button2.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					button2ActionPerformed(e);
				}
			});
			contentPane.add(button2);
			button2.setBounds(175, 325, 175, 38);

			/*
			 * /---- button3 ---- button3.setText("Choose Again");
			 * button3.addActionListener(new ActionListener() { public void
			 * actionPerformed(ActionEvent e) { button2ActionPerformed(e); } });
			 * contentPane.add(button3); button3.setBounds(85, 325, 130, 38);
			 */
			// ---- button4 ----
			button4.setText("Exit");
			button4.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					button2ActionPerformed(e);
				}
			});
			contentPane.add(button4);
			button4.setBounds(0, 325, 85, 38);

			{ // compute preferred size
				Dimension preferredSize = new Dimension();
				for (int i = 0; i < contentPane.getComponentCount(); i++) {
					Rectangle bounds = contentPane.getComponent(i).getBounds();
					preferredSize.width = Math.max(bounds.x + bounds.width,
							preferredSize.width);
					preferredSize.height = Math.max(bounds.y + bounds.height,
							preferredSize.height);
				}
				Insets insets = contentPane.getInsets();
				preferredSize.width += insets.right;
				preferredSize.height += insets.bottom;
				contentPane.setMinimumSize(preferredSize);
				contentPane.setPreferredSize(preferredSize);
			}
			pack();
			setLocationRelativeTo(getOwner());

		}

		private JCheckBox checkBox1;
		// private JCheckBox checkBox2;
		private JLabel label1;
		private JLabel label2;
		private JLabel label3;
		private JButton button1;
		private JComboBox comboBox1;
		private JLabel label4;
		private JComboBox comboBox2;
		private JButton button2;
		private JButton button4;
	}
}