import java.util.Calendar;
import java.util.Properties;
import java.text.SimpleDateFormat;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.text.NumberFormat;
import java.text.DecimalFormat;

import javax.imageio.ImageIO;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFormattedTextField;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

import org.rsbot.event.events.ServerMessageEvent;
import org.rsbot.event.listeners.ServerMessageListener;
import org.rsbot.event.listeners.PaintListener;
import org.rsbot.script.Script;
import org.rsbot.script.ScriptManifest;
import org.rsbot.script.methods.Players;
import org.rsbot.script.methods.Skills;
import org.rsbot.script.wrappers.RSComponent;
import org.rsbot.script.wrappers.RSInterface;
import org.rsbot.script.wrappers.RSNPC;
import org.rsbot.script.wrappers.RSPlayer;
import org.rsbot.script.wrappers.RSTile;
import org.rsbot.script.wrappers.RSObject;
import org.rsbot.util.GlobalConfiguration;
/**
 * @author White Bear
 */
@ScriptManifest(authors = { "WhiteBear" }, keywords = "Mining", name = "White Bear Essence", version = 5.66, description =
	"Flawless essence miner for Varrock and Yanille", website = "http://whitebearrs.orgfree.com")
public class WhiteBearEssenceMiner extends Script implements PaintListener, ServerMessageListener, MouseListener, MouseMotionListener {

	final ScriptManifest properties = getClass().getAnnotation(ScriptManifest.class);
	final WhiteBearPaint thePainter = new WhiteBearPaint();
	ChatResponder chatResThread; TheBrain theBrainThread;
	Point p = new Point(0,0);
	BufferedImage normal = null, normal2 = null;
	private RSTile[] path; private RSTile oldLoc = null;
	private Color popupBack, fonts, normalBack, hiddenPaint, lines;
	public Properties WBini = new Properties();
	FontChange fontGui = new FontChange();

	private int runEnergy = random(50, 95), useX = 0, useY = 0, sAdj = 0, lvlAmt = 0;
	private int valueMax, valueMid = 69, valueMin, itemCount, itemtypeint = 0, amtEarned = 0;
	private int exitStage = 0, camTurned = 0, swungPick = 0, restCount = 0, resCount = 0;
	private int minTime = 90, maxTime = 120, minLength = 10, maxLength = 20, invalidCount = 0;
	private int paintX = 7, paintY = 345, counter = 0, relogAfter = -1, clickNPC = 0, clickPortal = 0;
	private int allRand = 21, cam = 27, skill = 45, player = 18;
	private long stopTime = -1, chatResToogle = System.currentTimeMillis(), lastSaidHi = System.currentTimeMillis() - 110000, lastDenyBot = System.currentTimeMillis() - 110000, lastSaidLevel = System.currentTimeMillis() - 110000, memClean = System.currentTimeMillis() - 600005;
	private long nextBreak = System.currentTimeMillis(), nextLength = 60000;
	private long nextModAlert = System.currentTimeMillis(), sayNo = System.currentTimeMillis();
	private long lastAction = System.currentTimeMillis();
	private long nextRun = System.currentTimeMillis();
	private boolean checkPickaxe = false, setAltitude = false, foundType = false, antialias = false, mapPaint = false, firstClean = true;
	private boolean atVarrock = false, logOutInfo = false, doRest = false, tradeResponse = false, leftMine = true;
	private boolean doWalkBack = false, useChatRes = true, showMyPaint = true, showSettings = false;
	private boolean guiStart = false, useBreaking = false, randomBreaking = false, checkUpdates = false, currentlyBreaking = false;
	private boolean checkingSkill = false, suppressBrain = false, catRandLogout = true;
	private boolean useRemote = false, doingRemote = false, doRelog = false, useBrain = false, logOutR = false;
	private String theMessage = "I recommend using The Brain.", totalTime = "00:00:00", itemtype = "Unknown", font = "sansserif";
	private String remoteName = "", remoteMsg = "", status = "Loading...", colour, lastMessage = null, pickaxeName = "";

	final int pickaxe[] = {1265, 1267, 1269, 1273, 1271, 1275, 15259, 14069, 16912};
	final String pickaxeNames[] = {"Bronze Pickaxe", "Iron Pickaxe", "Steel Pickaxe", "Mithril Pickaxe", "Adamant Pickaxe", "Rune Pickaxe", "Dragon Pickaxe", "Inferno Adze", "Sacred Clay Pickaxe"};
	final int[] bankerIDs = {5913, 5912, 494, 495};
	final int bankBooth[] = {11402, 2213};

	final int essenceArea[] = {2950, 4870, 2870, 4790}, varrockBankArea[] = {3257, 3423, 3250, 3420};
	final RSTile varrockBank = new RSTile(3254, 3420);
	final RSTile varrockPath[] = {new RSTile(3253, 3421), new RSTile(3254, 3427), new RSTile(3262, 3421), new RSTile(3262, 3415), new RSTile(3259, 3411), new RSTile(3257, 3404), new RSTile(3254, 3398), new RSTile(3253, 3401)};
	final RSTile varrockDoor = new RSTile(3253, 3399), varrockDoorCheck = new RSTile(3253, 3398);
	final int yanilleBankArea[] = {2613, 3097, 2609, 3088};
	final RSTile yanilleBank = new RSTile(2612, 3091), yanilleDoor = new RSTile(2597, 3088);
	final RSTile yanillePath[] = {new RSTile(2611, 3091), new RSTile(2604,3090), new RSTile(2597, 3087)};

	final RSTile[] miningTiles = {new RSTile(2927, 4818), new RSTile(2931, 4818), new RSTile(2931, 4814), new RSTile(2927, 4814), new RSTile(2897, 4816), new RSTile(2897, 4812), new RSTile(2893, 4812), new RSTile(2893, 4816), new RSTile(2895, 4847), new RSTile(2891, 4847), new RSTile(2891, 4851), new RSTile(2895, 4851), new RSTile(2925, 4848), new RSTile(2925, 4852), new RSTile(2929, 4852), new RSTile(2929, 4848)};
	final int[] tilesX = {1, 0, 0, 1, 0, 0, 1, 1, 0, 1, 1, 0, 1, 1, 0, 0};
	final int[] tilesY = {0, 0, 1, 1, 0, 1, 1, 0, 1, 1, 0, 0, 1, 0, 0, 1};

	final int mageGuildX[] = new int[]{2590,2593,2597,2597,2597,2593,2586,2585,2585,2586,2588};
	final int mageGuildY[] = new int[]{3094,3094,3090,3088,3085,3081,3082,3087,3088,3090,3092};
	final Polygon mageGuild = new Polygon(mageGuildX, mageGuildY, 11);
	private enum State{ Antiban, ExitMines, Mining, GoAubury, GoDistentor, GoYanilleBank, GoVarrockBank, DoBank, Rest }
	//----------Main Loop------------\\
	public int loop() {
		try{
			lastAction = System.currentTimeMillis();
			if (game.getClientState() != 10){
				int i = loginLoop();
				if (i < 1){
					return random(100, 200);
				}else{
					return i;
				}
			}
			if (counter == 399){
				env.saveScreenshot(false);
				counter = 398;
			}
			mouse.setSpeed(random(4 + sAdj, 6 + sAdj));
			if (logOutR || exitStage == 2){
				logOutNow(false,true);
				return 1000;
			}
			if (stopTime >= 0 && System.currentTimeMillis() > stopTime) {
				log("Stop Time Reached. Logging off in 5 seconds.");
				sleep(random(4950, 5600));
				logOutNow(false, true);
			}
			if (doingRemote){
				if (doRelog){
					suppressBrain = true;
					long endTime = System.currentTimeMillis() + (relogAfter * 60 * 1000);
					logOutNow(false,false);
					while (endTime > System.currentTimeMillis()){
						sleep(100);
					}
					doingRemote = false;
					suppressBrain = false;
				}else{
					logOutNow(false,true);
					return 100;
				}
			}
			if (breakingMethod(2) == true && useBreaking == true){
				suppressBrain = true;
				long endTime = System.currentTimeMillis() + nextLength;
				currentlyBreaking = true;
				log("Taking a break for " + thePainter.formatTime((int) nextLength));
				while (System.currentTimeMillis() < endTime && currentlyBreaking == true){
					sleep(1000);
				}
				currentlyBreaking = false;
				loginLoop();
				breakingMethod(1);
				suppressBrain = false;
				return 10;
			}
			// ----------- Walk Back -------------- //
			if (setAltitude){
				if (doWalkBack) {
					walkBack();
					return random(400, 600);
				}
				if (!playerInArea(essenceArea) && !playerInArea(2594, 9489, 2582, 9484)){
					if (atVarrock == true){
						if (calc.distanceTo(varrockBank) >= 40) {
							status = "Walking Back to Varrock";
							doWalkBack = true;
							return random(100,200);
						}
					}else{
						if (calc.distanceTo(yanilleBank) >= 40) {
							status = "Walking Back to Yanille";
							doWalkBack = true;
							return random(100,200);
						}
					}
				}
			}
			//-------------------------------------//

			thePainter.scriptRunning = true;
			if (!thePainter.savedStats) { thePainter.saveStats(); return 100; }

			//levelup screen (flashing icon)
			interfaces.getComponent(741,9).doClick();
			//collect, trade, shop window checks
			interfaces.getComponent(620,18).doClick();
			interfaces.getComponent(109,13).doClick();
			interfaces.getComponent(335,19).doClick();

			if(!setAltitude) {
				log.severe("[Message] " + theMessage);
				camera.setPitch(true);
				setAltitude = true;
			}

			if (!checkPickaxe) {
				int amtMax = pickaxe.length + 1;
				if (equipment.containsOneOf(pickaxe)){
					sleep(100);
					for (int i = 0; i<amtMax; i++){
						if (equipment.containsOneOf(pickaxe[i])){
							pickaxeName = pickaxeNames[i];
							break;
						}
					}
					checkPickaxe = true;
				}else if (inventory.containsOneOf(pickaxe)){
					sleep(100);
					for (int i = 0; i<amtMax; i++){
						if (inventory.contains(pickaxe[i])){
							pickaxeName = pickaxeNames[i];
							break;
						}
					}
					checkPickaxe = true;
				}else{
					status = "Pickaxe not Found!";
					return random(1000,2000);
				}
			}
			if (game.getPlane() == 1 && playerInArea(3257, 3423, 3250, 3416)) {//varUP
				if (onTile(new RSTile(3256, 3421), "Climb", 0.5, 0.5, 0)) {
					sleep(random(1500, 2000));
					while (getMyPlayer().isMoving()) {
						sleep(random(90, 110));
					}
					sleep(random(1500, 2000));
				}
				return random(50, 100);
			}
			if(playerInArea(2594, 9489, 2582, 9484)){//yanDOWN
				RSObject ladder = objects.getNearest(1757);
				if(ladder != null){
					if(calc.tileOnScreen(ladder.getLocation())){
						ladder.doAction("Climb");
					}else{
						walking.walkPathMM(walking.randomizePath(walking.findPath(ladder.getLocation()),1,1));
					}
					return random(1000,2000);
				}
			}
			if(playerInArea(mageGuild) && game.getPlane() == 1){
				RSObject ladder = objects.getNearest(1723);
				if(ladder != null){
					if(calc.tileOnScreen(ladder.getLocation())){
						ladder.doAction("Climb");
					}else{
						walking.walkPathMM(walking.randomizePath(walking.findPath(ladder.getLocation()),1,1));
					}
					return random(1000,2000);
				}
			}
			if(playerInArea(3251, 3410, 3246, 3403) && anvilDoor()){//varANVIL
				return random(10,20);
			}
			lastAction = System.currentTimeMillis();

			if(!foundType){
				if (inventory.containsOneOf(1436)) {
					valueMax = grandExchange.loadItemInfo(1436).getMaxPrice();
					valueMid = grandExchange.loadItemInfo(1436).getMarketPrice();
					valueMin = grandExchange.loadItemInfo(1436).getMinPrice();
					itemtypeint = 1436; itemtype = "Rune Essence"; foundType = true;
				} else if (inventory.containsOneOf(7936)) {
					valueMax = grandExchange.loadItemInfo(7936).getMaxPrice();
					valueMid = grandExchange.loadItemInfo(7936).getMarketPrice();
					valueMin = grandExchange.loadItemInfo(7936).getMinPrice();
					itemtypeint = 7936; itemtype = "Pure Essence"; foundType = true;
				}
			}
			try{RSPlayer modC = getNearbyMod();
			if (modC != null){
				if (System.currentTimeMillis() < nextModAlert){
					nextModAlert += 150000;
					log.severe("[MOD] There is a Moderator nearby! Name: " + modC.getName());
				}
			}}catch(Exception e){}

			startRunning(runEnergy);
			return doAction();
		}catch(java.lang.Throwable t){
			return 100;
		}
	}

	public int doAction() {
		lastAction = System.currentTimeMillis();
		switch(getState()){
		case Mining:
			clickNPC = 0;
			antiBan(true);
			boolean doWait = false;
			while (getMyPlayer().isMoving() && calc.distanceTo(walking.getDestination()) >= 3) {
				sleep(10);
			}
			while (getMyPlayer().getAnimation()!= -1 && inventory.getCount() < 28){
				lastAction = System.currentTimeMillis();
				antiBan(true);
				sleep(100);
				doWait = true;
			}
			int skill = skills.getCurrentLevel(Skills.MINING);
			if (inventory.isFull()){
				return random(10,20);
			}else if (doWait){
				int miningPause = 0;
				while (inventory.getCount() < 28 && miningPause < 18){
					lastAction = System.currentTimeMillis();
					antiBan(true);
					sleep(100);
					miningPause += 1;
					if (skills.getCurrentLevel(Skills.MINING) != skill){
						interfaces.getComponent(741,9).doClick();
						miningPause = 99;
					}
				}
			}
			if (getMyPlayer().getAnimation() != -1){
				return random(100,200);
			}
			RSTile epos = nearEssTile();
			if(!onTile(epos, "Mine", useX, useY, 0)){
				if (calc.distanceTo(epos) > 1){
					if(!walking.walkPathMM(walking.randomizePath(walking.findPath(epos),1,1))){
						return 10;
					}
					sleep(150);
					if (calc.distanceTo(walking.getDestination()) >= 6)
						return random(1300,1500);
					else
						return random(700,900);
				}else
					return 100;
			}else{
				int clickWait2 = 0;
				while (getMyPlayer().getAnimation() == -1 && inventory.getCount() < 28 && clickWait2 < 11){
					sleep(100);
					clickWait2 += 1;
				}
				return 10;
			}
		case GoAubury:
			//******************************************************//
			//                     Go Aubury                        //
			//******************************************************//
			RSTile aubLoc = getMyPlayer().getLocation(); //loc check
			if (playerInArea(3254,3404,3252,3399) || playerInArea(3255,3402,3255,3400)){
				RSNPC aub = npcs.getNearest(553);
				try{
					mouse.move(calc.tileToScreen(aub.getLocation()));
					if (tiles.doAction(aub.getLocation(),"Teleport")){
						clickNPC += 1;
					}
					int failCount = 0;
					while (!playerInArea(essenceArea) && failCount < (clickNPC * 6)) {
						sleep(random(96, 105));
						if (getMyPlayer().isMoving())
							failCount = 0;
						failCount++;
					}
				}catch (java.lang.Exception e){}
				return random(20,80);
			}
			if(auburyDoorGoingIn()){
				return 300;
			}
			RSNPC Aubury = npcs.getNearest(553);
			if(Aubury != null){
				if(calc.tileOnScreen(Aubury.getLocation())){
					mouse.move(calc.tileToScreen(Aubury.getLocation()));
					if (Aubury.getScreenLocation().x <= 0){
						if(!tiles.doAction(Aubury.getLocation(),"Teleport")){
							if (calc.distanceTo(Aubury.getLocation())>=3)
								walking.walkTileMM(new RSTile(3253,3401),1,1);
							return random(100,200);
						}
					}else{
						if(!Aubury.doAction("Teleport")){
							if (calc.distanceTo(Aubury.getLocation())>=3)
								walking.walkTileMM(new RSTile(3253,3401),1,1);
							return random(100,200);
						}
					}
					clickNPC += 1;
					int counter = 0;
					while (getMyPlayer().isMoving() == true && !playerInArea(essenceArea) && counter < 70) {
						lastAction = System.currentTimeMillis();
						sleep(random(300,330));
						counter += 1;
					}
					int failCount = 0;
					while (!playerInArea(essenceArea) && failCount < (clickNPC * 6)) {
						sleep(random(96, 105));
						if (getMyPlayer().isMoving())
							failCount = 0;
						failCount++;
					}
					return 20;
				}else{
					if (walking.walkTileMM(new RSTile(3253,3401),1,1)){
						sleep(600);
						while (getMyPlayer().isMoving() && calc.distanceTo(walking.getDestination())>3){
							lastAction = System.currentTimeMillis();
							sleep(300);
						}
					}else
						walking.walkPathMM(walking.findPath(new RSTile(3253,3401)),1,1);
					sleep(random(300,500));
					if (getMyPlayer().getLocation() == aubLoc){//turns camera if same loc (prevents stuck)
						turnCamera();
					}
					return random(20,30);
				}
			}else{
				if(playerInArea(essenceArea))
					return 10;
				if (walking.walkTileMM(new RSTile(3253,3401),0,0)) {
					sleep(random(1200,1300));
					while (getMyPlayer().isMoving() && calc.distanceTo(walking.getDestination()) >= 5) {
						lastAction = System.currentTimeMillis();
						sleep(random(100,150));
						antiBan(false);
					}
					if (getMyPlayer().getLocation() == aubLoc){//turns camera if same loc (prevents stuck)
						turnCamera();
					}
					return 10;
				}
				if (!walking.walkTileMM(new RSTile(3258,3409),1,1)) {
					if (!walking.walkTileMM(new RSTile(3261,3414),1,1)){
						walking.walkPathMM(walking.findPath(new RSTile(3253,3401)));
					}
				}
				sleep(random(300,500));
				if (getMyPlayer().getLocation() == aubLoc){//turns camera if same loc (prevents stuck)
					turnCamera();
				}
				return random(10,20);
			}
		case GoDistentor:
			RSTile disLoc = getMyPlayer().getLocation(); //loc check
			int failCount2 = 0;
			if (npcs.getNearest(462)== null){
				if (walking.walkPathMM(yanillePath)){
					walking.sleep(random(960,1060));
					if (getMyPlayer().getLocation() == disLoc){//turns camera if same loc (prevents stuck)
						turnCamera();
					}
				}
				return random(50, 100);
			}
			if (!playerInArea(mageGuild)) {
				if (calc.distanceTo(yanilleDoor) >= 4){
					walking.walkTileMM(new RSTile(2597, 3087));
					sleep(random(960,1060));
					if (getMyPlayer().getLocation() == disLoc){//turns camera if same loc (prevents stuck)
						turnCamera();
					}
					return random(50,100);
				}
				Point doorSc = calc.tileToScreen(yanilleDoor);
				mouse.move(doorSc.x,doorSc.y,5,5);
				if (onTile(yanilleDoor, "Open", random(0.12, 0.18), random(-0.5, 0.5), random(40, 50))) {
					walking.sleep(1000);
					failCount2 = 0;
					while (!playerInArea(mageGuild) && failCount2 < 20) {
						lastAction = System.currentTimeMillis();
						sleep(random(90, 110));
						if (getMyPlayer().isMoving())
							failCount2 = 0;
						failCount2++;
					}
				}
				return random(50, 100);
			}
			RSNPC dis = npcs.getNearest(462);
			if (dis.isOnScreen()) {
				Point disSc = calc.tileToScreen(npcs.getNearest(462).getLocation());
				mouse.move(disSc.x,disSc.y,5,5);
				if (dis.doAction("Teleport")) {
					walking.sleep(1000);
					sleep(random(500, 750));
					if (getMyPlayer().getInteracting() == null)
						return random(50, 100);
					failCount2 = 0;
					while (!playerInArea(essenceArea) && failCount2 < 30) {
						lastAction = System.currentTimeMillis();
						sleep(random(90, 110));
						if (getMyPlayer().isMoving())
							failCount2 = 0;
						failCount2++;
					}
				}
			} else {
				if (calc.tileOnScreen(dis.getLocation())){
					tiles.doAction(dis.getLocation(), "Teleport");
				}else{
					walking.walkPathMM(walking.findPath(npcs.getNearest(462).getLocation()));
					sleep(random(700,800));
					failCount2 = 0;
					while (getMyPlayer().isMoving() && calc.distanceTo(walking.getDestination()) >= 2 && failCount2 < 21){
						sleep(50);
						failCount2++;
					}
					return random(10,20);
				}
			}
			return random(50, 100);
		case ExitMines:
			//******************************************************//
			//                    Exiting Mines                     //
			//******************************************************//
			leftMine = false;
			boolean clickedPortal = false;
			camera.setPitch(true);
			RSObject portal = objects.getNearest(2492);
			if(portal == null){
				walking.walkPathMM(walking.findPath(nearEssTile()));
				sleep(random(600,700));
				int maxi3 = 0;
				while (getMyPlayer().isMoving() && calc.distanceTo(walking.getDestination()) > 2 && maxi3 < 40) {
					lastAction = System.currentTimeMillis();
					sleep(random(100,120));
					maxi3 += 1;
				}
			}
			if(!calc.tileOnScreen(portal.getLocation())){
				walking.walkPathMM(walking.findPath(portal.getLocation()));
				sleep(random(800,1000));
				int maxi2 = 0;
				while (getMyPlayer().isMoving() && calc.distanceTo(walking.getDestination()) > 2 && maxi2 < 42) {
					lastAction = System.currentTimeMillis();
					sleep(random(100,120));
					maxi2 += 1;

					// Clicks Portal while walking if portal is on screen
					if (!clickedPortal && calc.tileOnScreen(portal.getLocation())){
						Point pos = calc.tileToScreen(portal.getLocation());
						//hover
						mouse.move(random(pos.x - 5, pos.x + 5), random(pos.y - 5, pos.y + 5));
						if (tiles.doAction(portal.getLocation(), "Enter")){
							clickPortal += 1;
							clickedPortal = true;
							int failCount = 0;
							while (playerInArea(essenceArea) && failCount < (clickPortal * 5)) {
								sleep(100);
								try{ if (getMyPlayer().isMoving() && calc.distanceTo(walking.getDestination()) >= 1){
									lastAction = System.currentTimeMillis();
									failCount = 0;
								} }catch (Exception e){}
								failCount += 1;
							}
							break;
						}
					}
				}
			}else{
				if (calc.distanceTo(portal.getLocation()) > 2) {
					walking.walkTileMM(portal.getLocation(),1,1);
					sleep(random(850,920));
				}
				int maxi = 0;
				while (getMyPlayer().isMoving() && calc.distanceTo(walking.getDestination()) > 2 && maxi < 40) {
					lastAction = System.currentTimeMillis();
					sleep(random(100,120));
					maxi += 1;
				}
				Point pox = calc.tileToScreen(portal.getLocation());
				mouse.move(random(pox.x - 5, pox.x + 5), random(pox.y - 5, pox.y + 5));
				if(!tiles.doAction(portal.getLocation(), "Enter"))
					return random(10,20);
				else
					clickPortal += 1;
				int failCount = 0;
				while (playerInArea(essenceArea) && failCount < (clickPortal * 5)) {
					sleep(100);
					try{ if (getMyPlayer().isMoving() && calc.distanceTo(walking.getDestination()) >= 1){
						lastAction = System.currentTimeMillis();
						failCount = 0;
					} }catch (Exception e){}
					failCount += 1;
				}
				return 10;
			}
			return random(100,200);
		case GoVarrockBank:
			clickPortal = 0;
			leftMine = true;
			RSTile curLoc = getMyPlayer().getLocation();
			if(auburyDoorGoingOut()){
				return 300;
			}
			if(walking.walkTileMM(new RSTile(3253,3417),1,0)) {
				if (calc.distanceTo(new RSTile(3253,3417)) > 4) {
					sleep(random(700,800));
				}
				if (curLoc == getMyPlayer().getLocation()){
					turnCamera();
				}
				while (getMyPlayer().isMoving() && calc.distanceTo(walking.getDestination()) >= 4) {
					lastAction = System.currentTimeMillis();
					antiBan(false);
					sleep(random(100,200));
					if (playerInArea(varrockBankArea)){
						camera.setPitch(true);
						try {doBank(); }catch (Exception e){}
					}
				}
				return random(40,100);
			}
			if(!walking.walkPathMM(walking.reversePath(varrockPath),1,1)){
				if (!walking.walkTileMM(new RSTile(3253,3420))){
					walking.walkTileMM(new RSTile(3258,3411));
				}
			}
			return random(300,400);
		case GoYanilleBank:
			clickPortal = 0;
			leftMine = true;
			if (playerInArea(mageGuild)) {
				if (onTile(yanilleDoor, "Open", random(0.1, 0.2), random(-0.5, 0.5), random(40, 50))) {
					walking.sleep(1000);
					int failCount3 = 0;
					while (playerInArea(mageGuild) && failCount3 < 20) {
						lastAction = System.currentTimeMillis();
						sleep(random(90, 110));
						if (getMyPlayer().isMoving())
							failCount3 = 0;
						failCount3++;
					}
				}else{
					if (calc.distanceTo(yanilleDoor) > 2){
						if (!walking.walkTileMM(new RSTile(2596,3087))){
							walking.walkTileMM(new RSTile(2593,3087));
						}
						sleep(random(300,600));
					}
				}
				return random(50, 100);
			}
			if (!walking.walkTileMM(new RSTile(2613,3091))){
				walking.walkPathMM(walking.reversePath(yanillePath));
				/*if (!getMyPlayer().isMoving() || calc.distanceTo(walking.getDestination()) <= 5)
					if (walking.walkPathMM(walking.findPath(yanilleBank)))
						walking.sleep(1000);*/
			}
			return random(50, 100);
		case DoBank:
			if (calc.distanceTo(varrockBank) <= 75){
				if (auburyDoorGoingOut() == true){
					return 10;
				}
			}
			doBank();
			lastAction = System.currentTimeMillis();
			return random(50, 100);
		case Antiban:
			antiBan(false);
			return 10;
		case Rest:
			restCount += 1;
			rest2();
			return random(10, 20);
		}
		return 100;
	}

	public State getState(){
		try {
			if(!playerInArea(essenceArea) && !playerInArea(mageGuild)  && getMyPlayer().getAnimation() == -1 && (calc.distanceTo(walking.getDestination()) >= 5) && (calc.distanceTo(walking.getDestination()) <= 30)){
				if (leftMine == false){
					if (!playerInArea(3254,3404,3252,3399) && !playerInArea(3255,3402,3255,3400) && !playerInArea(mageGuild)){
						return State.Antiban;
					}
				}else{
					return State.Antiban;
				}
			}
		} catch(Exception e) {}
		if (playerInArea(essenceArea)) {
			if (inventory.isFull()) {
				status = "Exiting Mine";
				return State.ExitMines;
			}
			status = "Mining";
			return State.Mining;
		}
		if (atVarrock) {
			if (inventory.isFull()) {
				if (playerInArea(varrockBankArea) || (objects.getNearest(bankBooth) != null && calc.tileOnScreen(objects.getNearest(bankBooth).getLocation()))) {
					status = "Banking";
					return State.DoBank;
				}
				status = "Walking to Bank";
				return State.GoVarrockBank;
			}
			if (playerInArea(varrockBankArea) && doRest == true && walking.getEnergy() <= 21){
				status = "Resting";
				return State.Rest;
			}
			status = "Walking to Aubury";
			return State.GoAubury;
		} else {
			if (inventory.isFull()) {
				if (playerInArea(yanilleBankArea)) {
					status = "Banking";
					return State.DoBank;
				}
				status = "Walking to Bank";
				return State.GoYanilleBank;
			}
			if (playerInArea(yanilleBankArea) && doRest == true && walking.getEnergy() <= 21){
				status = "Resting";
				return State.Rest;
			}
			status = "Walking to Distentor";
			return State.GoDistentor;
		}
	}
	//--------------------------------\\
	//Credits to authors of AIOWalker "Taha, Pqqqqq, Rakura"
	public int walkBack() {
		try {
			if (playerInArea(3263,3429,3243,3395) || playerInArea(essenceArea) || playerInArea(2594, 9489, 2582, 9484)) {
				doWalkBack = false;
				path = null;
				oldLoc = null;
				return 10;
			}
			if (path == null) {
				log("[Walker] Walking you back to bank! Generating path...");
				if (atVarrock == true) {
					path = walking.findPath(varrockBank);
				}else{
					path = walking.findPath(yanilleBank);
				}
			}
			if (calc.distanceTo(path[path.length - 1]) > 5) {
				mouse.setSpeed(random(4 + sAdj, 6 + sAdj));
				camera.setPitch(true);
				if (walking.getEnergy() > 60 && random(1, 10) == 1 || walking.getEnergy() > 85) {
					walking.setRun(true);
				}
				if (getMyPlayer().getLocation().equals(oldLoc)) {
					walking.walkTileMM(walking.getClosestTileOnMap(path[path.length - 1]));
					if (atVarrock == true) {
						path = walking.findPath(varrockBank);
					}else{
						path = walking.findPath(yanilleBank);
					}
				}
				oldLoc = getMyPlayer().getLocation();
				if (calc.distanceTo(walking.getDestination()) < random(5, 12)
						|| calc.distanceTo(walking.getDestination()) > 40) {
					if (!walking.walkPathMM(path)) {
						walking.walkTo(walking.nextTile(path, 16));
					}
				}
				sleep(random(200, 400));
			} else {
				doWalkBack = false;
				path = null;
				oldLoc = null;
				log("[Walker] Successfully got you back to bank!");
				sleep(random(2200,2500));
			}
		} catch (final Exception e) {}
		return 1;
	}

	private boolean breakingMethod(int doWhat){
		switch (doWhat){
		case 1://Generate Time
			if (randomBreaking == true){
				long varTime = random(7200000,18000000);
				nextBreak = System.currentTimeMillis() + varTime;
				long varLength = random(120000,600000);
				nextLength = varLength;
			}else{
				long varTime = random(minTime * 1000 * 60, maxTime * 1000 * 60);
				nextBreak = System.currentTimeMillis() + varTime;
				long varLength = random(minLength * 1000 * 60,maxLength * 1000 * 60);
				nextLength = varLength;
			}
			return false;
		case 2://Check Time
			if (nextBreak <= System.currentTimeMillis()){
				return true;
			}
			return false;
		}
		return false;
	}
	//--On Start--\\
	public boolean onStart() {
		allRand = random(20,23);
		cam = random(26,29);
		skill = random(44,49);
		player = random(17,20);
		try{
			fontGui.WhiteBearGUI.dispose();
		}catch (java.lang.Exception e){}
		WhiteBearGUI gui = new WhiteBearGUI();
		gui.WhiteBearGUI.setVisible(true);
		while (!guiStart) {
			sleep(100);
		}
		gui = null;
		thePainter.setColour();
		if (stopTime > 0){
			log("Script will stop after " + thePainter.formatTime((int) stopTime));
			long stoppingTime = stopTime + System.currentTimeMillis();
			stopTime = stoppingTime;
		}
		log.severe("Please disable Auto Login and Break Handler (the script will start LoginBot if you are not logged in)");
		chatResThread = new ChatResponder();
		chatResThread.start();
		if (useBrain){
			theBrainThread = new TheBrain();
			theBrainThread.start();
		}
		//Updater: Credits to Enfilade
		if(checkUpdates == true){
			URLConnection url = null;
			BufferedReader in = null;
			BufferedWriter out = null;
			try{
				url = new URL("http://whitebearrs.orgfree.com/content/newapi/essenceV.txt").openConnection();
				in = new BufferedReader(new InputStreamReader(url.getInputStream()));
				if(Double.parseDouble(in.readLine()) > properties.version()) {
					log("Update found (V" + in.readLine().toString() + ")! Please update the script!");
					if(JOptionPane.showConfirmDialog(null, "Update found. Do you want to update?") == 0){
						JOptionPane.showMessageDialog(null, "Choose 'WhiteBearEssenceMiner.java' in your scripts folder and hit 'Open'");
						JFileChooser fc = new JFileChooser();
						if(fc.showOpenDialog(null) == JFileChooser.APPROVE_OPTION){
							url = new URL("http://whitebearrs.orgfree.com/content/newapi/WhiteBearEssenceMiner.java").openConnection();
							in = new BufferedReader(new InputStreamReader(url.getInputStream()));
							out = new BufferedWriter(new FileWriter(fc.getSelectedFile().getPath()));
							String inp;
							while((inp = in.readLine()) != null){
								out.write(inp);
								out.newLine();
								out.flush();
							}
							log.warning("Script successfully downloaded. Please recompile and restart RSBot!");
							return false;
						} else log("Update canceled");
					} else log("Update canceled");
				} else {
					log("You are using the latest version of this script!");
				}
				if(in != null)
					in.close();
				if(out != null)
					out.close();
			} catch (IOException e){
				log.severe("An error occurred while trying to update script!");
			}
		}
		try {
			final URL cursorURL = new URL("http://i27.tinypic.com/34zxfyr.png");
			normal = ImageIO.read(cursorURL);
			final URL cursorURL2 = new URL("http://i31.tinypic.com/bge6nc.png");
			normal2 = ImageIO.read(cursorURL2);
		} catch (MalformedURLException e) {
			log.warning("Error when buffering cursor image!");
		} catch (IOException e) {
			log.warning("Error when opening cursor image!");
		}
		breakingMethod(1);
		if (!game.isLoggedIn()){
			loginLoop();
		}
		thePainter.time_ScriptStart=System.currentTimeMillis();
		return true;
	}

	public boolean logOutNow(boolean toLobby, boolean stopScript) {
		status = "Logging out";
		while (bank.isOpen()) {
			bank.close();
			mouse.move(random(10, 430), random(10, 465));
			sleep(random(200, 400));
		}
		while (!game.isOnLogoutTab()) {
			if (interfaces.getComponent(548, 178).getAbsoluteX() <= 10){
				interfaces.get(746).getComponent(170).doClick();
			}else{
				interfaces.getComponent(548, 178).doClick();
			}
			if (bank.isOpen()){
				bank.close();
			}
			int timesToWait = 0;
			while (!game.isOnLogoutTab() && timesToWait < 5) {
				sleep(random(200, 400));
				timesToWait++;
			}
		}
		int maximum = 0;
		while (game.isLoggedIn() == true && maximum < 20){
			if (toLobby){
				interfaces.get(182).getComponent(6).doClick();
			}else{
				interfaces.get(182).getComponent(13).doClick();
			}
			sleep(1000);
		}
		if (!toLobby && stopScript){
			stopScript(false);
		}
		return true;
	}

	public void doBank() {
		int failCount = 0;
		try {
			if (!bank.isOpen()) {
				int rand = random(1,3);
				if (rand == 1) {
					if (onTile(objects.getNearest(bankBooth).getLocation(), "Use-quickly", 0.5, 0.5, 0)){
						walking.sleep(700);
						while(!bank.isOpen() && failCount < 11) {
							sleep(random(95, 105));
							if (getMyPlayer().isMoving())
								failCount = 0;
							failCount++;
						}
					}else{
						sleep(random(200,250));
					}
				}else{
					if (onTile(objects.getNearest(bankerIDs).getLocation(), "Bank Banker", 0.5, 0.5, 0)){
						walking.sleep(700);
						while(!bank.isOpen() && failCount < 11) {
							sleep(random(95, 105));
							if (getMyPlayer().isMoving())
								failCount = 0;
							failCount++;
						}
					}else{
						sleep(random(200,250));
					}
				}
			}
			if (bank.isOpen()) {
				itemCount = bank.getCount(itemtypeint);
				sleep(random(350, 450));
				if (inventory.containsOneOf(pickaxe)) {
					if (bank.depositAllExcept(pickaxe)) {
						sleep(random(500, 600));
						itemCount = bank.getCount(itemtypeint);
					}
				} else {
					if (bank.depositAll()) {
						sleep(random(500, 600));
						itemCount = bank.getCount(itemtypeint);
					}
				}
			}
		} catch (final Exception e) { }
	}
	public boolean rest2() {
		int energy = walking.getEnergy();
		for (int d = 0; d < 5; d++) {
			interfaces.getComponent(750, 6).doAction("Rest");
			mouse.moveSlightly();
			sleep(random(400, 600));
			if (getMyPlayer().getAnimation() == 12108 || getMyPlayer().getAnimation() == 2033 || getMyPlayer().getAnimation() == 2716 || getMyPlayer().getAnimation() == 11786 || getMyPlayer().getAnimation() == 5713) {
				break;
			}
			if (d == 4) {
				log("Rest failed!");
				return false;
			}
		}
		energy = walking.getEnergy();
		int maxWait = 0;
		while (energy < 95 && maxWait <= 111) {
			sleep(random(250, 300));
			energy = walking.getEnergy();
			maxWait += 1;
		}
		return true;
	}
	// VARROCK DOOR!!!  //
	private boolean openVarrockDoor(boolean fromOutside){
		boolean fromOut = fromOutside;
		if (fromOut == false){
			if (calc.distanceTo(varrockDoor)>2){
				if (!walking.walkTileMM(varrockDoor)){
					walking.walkPathMM(walking.findPath(varrockDoor));
				}
				sleep(700);
			}
			if (onTile(varrockDoor, "Open", random(0.39, 0.61),	random(0, 0.05), random(20, 50))) {
				int failCount2 = 0;
				while (auburyDoorCheck() && failCount2 < 30) {
					lastAction = System.currentTimeMillis();
					sleep(random(49, 52));
					if (getMyPlayer().isMoving())
						failCount2 = 0;
					failCount2++;
				}
				return true;
			}
			return false;
		}else{
			if (calc.distanceTo(varrockDoor)>2){
				if (!walking.walkTileMM(varrockDoor)){
					walking.walkPathMM(walking.findPath(varrockDoor));
				}
				sleep(700);
			}
			if (onTile(varrockDoor, "Open", random(0.39, 0.61),	random(0, 0.05), random(20, 50))) {
				int failCount2 = 0;
				while (auburyDoorCheck() && failCount2 < 30) {
					lastAction = System.currentTimeMillis();
					sleep(random(49, 52));
					if (getMyPlayer().isMoving())
						failCount2 = 0;
					failCount2++;
				}
				return true;
			}
			return false;
		}
	}
	private boolean auburyDoorGoingOut(){
		if (!playerInArea(3254,3404,3252,3399) && !playerInArea(3255,3402,3255,3400)){
			return false;
		}
		RSObject[] doorClosed = objects.getAllAt(new RSTile(3253,3398));
		if (doorClosed == null){
			return false;
		}
		for (int a = 0; a < doorClosed.length; a++) {
			if (doorClosed[a].getID() == 24381) {
				if (openVarrockDoor(false)){
					return false;
				}else{
					return true;
				}
			}
		}
		return false;
	}
	private boolean auburyDoorGoingIn(){
		if (playerInArea(3254,3404,3252,3399) || playerInArea(3255,3402,3255,3400)){
			return false;
		}
		RSObject[] doorClosed = objects.getAllAt(new RSTile(3253,3398));
		if (doorClosed == null){
			return false;
		}
		if (calc.distanceTo(varrockDoor)>8){
			return false;
		}
		for (int a = 0; a < doorClosed.length; a++) {
			if (doorClosed[a].getID() == 24381) {
				if (openVarrockDoor(true)){
					return false;
				}else{
					return true;
				}
			}
		}
		return false;
	}
	private boolean auburyDoorCheck(){
		RSObject[] doorClosedTile = objects.getAllAt(new RSTile(3253,3398));
		if (doorClosedTile == null){
			return false;
		}
		for (int a = 0; a < doorClosedTile.length; a++) {
			if (doorClosedTile[a].getID() == 24381) {
				return true;
			}
		}
		return false;
	}
	// END              //
	public boolean anvilDoor(){
		int failCount;
		if (objects.getAllAt(new RSTile(3248,3411)) != null && objects.getAllAt(new RSTile(3248,3410)) == null) {
			if (!playerInArea(3249,3410,3247,3409)){
				walking.walkTileMM(new RSTile(3248,3410),1,1);
			}
			sleep(random(1000,1500));
			if (onTile(new RSTile(3248,3410), "Open", random(0.39, 0.61),	random(0, 0.05), random(20, 50))) {
				failCount = 0;
				while (objects.getAllAt(new RSTile(3248,3410)) == null && failCount < 40) {
					sleep(random(50, 100));
					failCount++;
				}
			}else{
				onTile(new RSTile(3248,3411), "Open", random(0.39, 0.61),	random(0, 0.05), random(20, 50));
				failCount = 0;
				while (objects.getAllAt(new RSTile(3248,3410)) == null && failCount < 40) {
					sleep(random(50, 100));
					failCount++;
				}
			}
			if (objects.getAllAt(new RSTile(3248,3411)) == null) {
				sleep(random(50, 100));
				return false;
			}
		}else{
			return false;
		}
		return true;
	}

	public void startRunning(final int energy) {
		if (nextRun < System.currentTimeMillis() && walking.getEnergy() >= energy && !walking.isRunEnabled()) {
			nextRun = System.currentTimeMillis() + 4000;
			runEnergy = random(40, 95);
			walking.setRun(true);
			sleep(random(500, 750));
		}
	}
	//Credits to Garrett
	public boolean onTile(final RSTile tile, final String action,
			final double dx, final double dy, final int height) {
		if (!tile.isValid()) {
			return false;
		}
		Point checkScreen;
		try {
			checkScreen = calc.tileToScreen(tile, dx, dy, height);
			if (!calc.pointOnScreen(checkScreen)) {
				if (calc.distanceTo(tile) <= 8) {
					if (getMyPlayer().isMoving()) {
						return false;
					}
					walking.walkTileMM(tile);
					walking.sleep(1000);
					return false;
				}
				return false;
			}
		} catch (final Exception e) {
		}
		try {
			boolean stop = false;
			for (int i = 0; i <= 50; i++) {
				checkScreen = calc.tileToScreen(tile, dx, dy, height);
				if (!calc.pointOnScreen(checkScreen)) {
					return false;
				}
				mouse.move(checkScreen);
				final Object[] menuItems = menu.getItems();
				for (int a = 0; a < menuItems.length; a++) {
					if (menuItems[a].toString().toLowerCase().contains(action.toLowerCase())) {
						stop = true;
						break;
					}
				}
				if (stop) {
					break;
				}
			}
		} catch (final Exception e) {
		}
		try {
			return menu.doAction(action);
		} catch (final Exception e) {
		}
		return false;
	}

	private RSTile nearEssTile() {
		RSTile tile = null;
		int closest = 999;
		for (int i = 0; i < miningTiles.length; i++) {
			if(calc.distanceTo(miningTiles[i]) < closest) {
				closest = calc.distanceTo(miningTiles[i]);
				tile = miningTiles[i];
				useX = tilesX[i];
				useY = tilesY[i];
			}
		}
		return tile;
	}

	private boolean playerInArea(int maxX, int maxY, int minX, int minY) {
		int x = getMyPlayer().getLocation().getX();
		int y = getMyPlayer().getLocation().getY();
		if (x >= minX && x <= maxX && y >= minY && y <= maxY)
			return true;
		return false;
	}
	private boolean playerInArea(final int[] area) {
		final int x = getMyPlayer().getLocation().getX();
		final int y = getMyPlayer().getLocation().getY();
		if (x >= area[2] && x <= area[0] && y >= area[3] && y <= area[1])
			return true;
		return false;
	}
	private boolean playerInArea(Polygon area) {
		return area.contains(new Point(getMyPlayer().getLocation().getX(), getMyPlayer().getLocation().getY()));
	}
	//------AntiBan------\\
	public void antiBan(boolean extras) {
		mouse.setSpeed(random(5 + sAdj, 7 + sAdj));
		if(nextRun < System.currentTimeMillis() && walking.getEnergy() >= random(79,90)){
			nextRun = System.currentTimeMillis() + 4000;
			walking.setRun(true);
			sleep(100);
		}
		int random = random(1, allRand);
		if (random == 1) {
			if (random(1,3) == 1)
				mouse.move(random(5, 650), random(5, 495));
		}
		if (random == 2){
			int randCamera = random(1,cam);
			if (randCamera <= 4){
				camTurned += 1;
				final char[] LR = new char[] { KeyEvent.VK_LEFT,
						KeyEvent.VK_RIGHT };
				final char[] UD = new char[] { KeyEvent.VK_DOWN,
						KeyEvent.VK_UP };
				final char[] LRUD = new char[] { KeyEvent.VK_LEFT,
						KeyEvent.VK_RIGHT, KeyEvent.VK_UP,
						KeyEvent.VK_UP };
				final int randomLR = random(0, 2);
				final int randomUD = random(0, 2);
				final int randomAll = random(0, 4);

				if (random(0, 3) == 0) {
					keyboard.pressKey(LR[randomLR]);
					sleep(random(100, 400));
					keyboard.pressKey(UD[randomUD]);
					sleep(random(300, 500));
					keyboard.releaseKey(UD[randomUD]);
					sleep(random(100, 300));
					keyboard.releaseKey(LR[randomLR]);
				} else {
					keyboard.pressKey(LRUD[randomAll]);
					if (randomAll > 1) {
						sleep(random(300, 500));
					} else {
						sleep(random(500, 600));
					}
					keyboard.releaseKey(LRUD[randomAll]);
				}
			}
		}
		if (random == 6){
			if(random(1,skill) == 1 && getMyPlayer().getAnimation() != -1){
				checkingSkill = true;
				if(game.getCurrentTab() != 1){
					game.openTab(1);
					Point stats = new Point(interfaces.get(320).getComponent(3).getAbsoluteX()+20,interfaces.get(320).getComponent(3).getAbsoluteY()+10);
					mouse.move(stats,5,5);
					sleep(random(1400,1500));
				}
				checkingSkill = false;
			}
		}
		if (random == 7){
			if (extras == true) {
				final int chance2 = random(1,player);
				if(chance2 == 1){
					RSPlayer player = players.getNearest(Players.ALL_FILTER);
					if (player != null && calc.distanceTo(player) != 0) {
						mouse.move(player.getScreenLocation(), 5, 5);
						sleep(random(300, 400));
						mouse.click(false);
						sleep(random(750, 800));
						mouse.move(random(10, 450), random(10, 495));
					}
				}
			}
		}
		mouse.setSpeed(random(4 + sAdj, 6 + sAdj));
	}
	public void turnCamera(){
		final char[] LR = new char[] { KeyEvent.VK_LEFT,
				KeyEvent.VK_RIGHT };
		final char[] UD = new char[] { KeyEvent.VK_DOWN,
				KeyEvent.VK_UP };
		final char[] LRUD = new char[] { KeyEvent.VK_LEFT,
				KeyEvent.VK_RIGHT, KeyEvent.VK_UP,
				KeyEvent.VK_UP };
		final int randomLR = random(0, 2);
		final int randomUD = random(0, 2);
		final int randomAll = random(0, 4);

		if (random(0, 3) == 0) {
			keyboard.pressKey(LR[randomLR]);
			sleep(random(100, 400));
			keyboard.pressKey(UD[randomUD]);
			sleep(random(300, 450));
			keyboard.releaseKey(UD[randomUD]);
			sleep(random(100, 300));
			keyboard.releaseKey(LR[randomLR]);
		} else {
			keyboard.pressKey(LRUD[randomAll]);
			if (randomAll > 1) {
				sleep(random(300, 500));
			} else {
				sleep(random(450, 550));
			}
			keyboard.releaseKey(LRUD[randomAll]);
		}
	}

	private RSPlayer getNearbyMod() {
		RSPlayer[] modCheck = players.getAll();
		int Dist = 18;
		RSPlayer closest = null;
		int element = 0;
		int size = modCheck.length;
		while (element < size) {
			if (modCheck[element] != null) {
				try {
					if (modCheck[element].getName().startsWith("Mod")) {
						int distance = calc.distanceTo(modCheck[element]);
						if (distance < Dist) {
							Dist = distance;
							closest = modCheck[element];
						}
					}
				} catch (Exception ignored) {
				}
			}
			element += 1;
		}
		return closest;
	}

	public void onFinish() {
		counter = 405;
		chatResThread.run = false;
		if (game.isLoggedIn() && exitStage < 2 && thePainter.runTime >= 3600000) {//1hr
			env.saveScreenshot(false);
		}
		if (useBrain){
			theBrainThread.run = false;
		}
		log("You earned " + amtEarned + " coins in " + totalTime + ".");
	}

	public void onRepaint(final Graphics g) {
		try{

			g.setFont(new Font(font, Font.PLAIN, 12));
			try { if (thePainter.savedStats == false) {
				g.setColor(new Color(0,0,0,160));
				g.fillRect(60, 120, 240, 15);
				g.setColor(Color.YELLOW);
				g.drawString("Loading, please be patient...", 64, 132);
			} }catch (Exception e) {}
			if (antialias == true) {
				((Graphics2D) g).setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
				((Graphics2D)g).setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY);
				((Graphics2D)g).setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
			}
			if (currentlyBreaking == true){
				g.setColor(new Color(0,0,0,160));
				g.fillRect(7, 320, 300, 20);
				g.setColor(Color.WHITE);
				g.drawString("We are currently taking a break!",27, 334);
			}
			if (normal != null) {
				final int mouse_x = mouse.getLocation().x;
				final int mouse_y = mouse.getLocation().y;
				if (playerInArea(essenceArea)){
					g.drawImage(normal, mouse_x - 8, mouse_y - 8, null);
				} else {
					g.drawImage(normal2, mouse_x - 8, mouse_y - 8, null);
				}
			}
		}catch(java.lang.Throwable t){}
		try{
			if (thePainter.savedStats == true && game.getClientState() == 10) {
				thePainter.paint(g);
			}
		}catch (Exception e){}
	}
	//credits to Garrett for the Paint
	public class WhiteBearPaint {
		Rectangle clr1 = new Rectangle(210,43,15,15);
		Rectangle clr2 = new Rectangle(227,43,15,15);
		Rectangle clr3 = new Rectangle(244,43,15,15);
		Rectangle clr4 = new Rectangle(261,43,15,15);
		Rectangle clr5 = new Rectangle(278,43,15,15);
		Rectangle clr6 = new Rectangle(295,43,15,15);
		Rectangle cr1 = new Rectangle(210,61,15,15);
		Rectangle toogleRest = new Rectangle(210,79,15,15);
		Rectangle catLogout = new Rectangle(365,61,15,15);
		Rectangle changeFont = new Rectangle(365,79,83,15);
		Rectangle logOut = new Rectangle(295,79,55,15);
		Rectangle logOut2 = new Rectangle(320,220,200,70);
		Rectangle logOutYes = new Rectangle(338,255,80,20);
		Rectangle logOutNo = new Rectangle(423,255,80,20);
		//Rectangle settingsBox = new Rectangle(195,10,165,93);
		Rectangle memCleanRect = new Rectangle(334,19,17,17);
		Rectangle popupMemClean = new Rectangle(352,26,153,15);
		//Rectangle breakingNote = new Rectangle(7,320,300,20);

		Rectangle r = new Rectangle(7, 345, 408, 114);
		Rectangle r1 = new Rectangle(420, 345, 77, 20);
		Rectangle r2 = new Rectangle(420, 369, 77, 20);
		Rectangle r3 = new Rectangle(420, 392, 77, 20);
		Rectangle r4 = new Rectangle(420, 415, 77, 20);
		Rectangle r5 = new Rectangle(420, 439, 77, 20);
		Rectangle r2c = new Rectangle(415, 369, 5, 20);
		Rectangle r3c = new Rectangle(415, 392, 5, 20);
		Rectangle r4c = new Rectangle(415, 415, 5, 20);
		Rectangle r5c = new Rectangle(415, 439, 5, 20);

		Rectangle sb1 = new Rectangle(12, 350, 398, 12);
		Rectangle sb1s = new Rectangle(12, 350, 196, 12);
		boolean savedStats = false;
		boolean scriptRunning = false;
		int currentTab = 0, lastTab = 0;
		int start_exp = 0, start_lvl = 0;
		int gained_exp = 0, gained_lvl = 0;
		int movePaintNote = 451;

		Thread mouseWatcher = new Thread();
		final NumberFormat nf = NumberFormat.getInstance();

		long time_ScriptStart = System.currentTimeMillis();
		long runTime = System.currentTimeMillis() - time_ScriptStart;

		int sine = 0;
		int sineM = 1;

		public void proggiePaint(final Graphics g){
			final String DATE_FORMAT_NOW = "yyyy-MM-dd HH:mm:ss";
		    Calendar cal = Calendar.getInstance();
		    SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT_NOW);
		    String date = sdf.format(cal.getTime());
		    if (paintX <= 140 && paintY <= 86){
		    	g.setFont(new Font(font, Font.PLAIN, 12));
		    	g.setColor(normalBack);
		    	g.fillRect(7, 235, 133, 76);
		    	g.setColor(fonts);
		    	g.drawString(date, 17, 257);
		    	g.drawString("Version " + Double.toString(properties.version()), 17, 279);
		    	g.drawString(pickaxeName, 17, 301);
		    }else{
		    	g.setFont(new Font(font, Font.PLAIN, 12));
		    	g.setColor(normalBack);
		    	g.fillRect(7, 10, 133, 76);
		    	g.setColor(fonts);
		    	g.drawString(date, 17, 32);
		    	g.drawString("Version " + Double.toString(properties.version()), 17, 54);
		    	g.drawString(pickaxeName, 17, 76);
		    }
		}

		public void paint(final Graphics g) {
			r = new Rectangle(paintX, paintY, 408, 114);
			r1 = new Rectangle(paintX + 413, paintY, 77, 20);
			r2 = new Rectangle(paintX + 413, paintY + 24, 77, 20);
			r3 = new Rectangle(paintX + 413, paintY + 47, 77, 20);
			r4 = new Rectangle(paintX + 413, paintY + 70, 77, 20);
			r5 = new Rectangle(paintX + 413, paintY + 94, 77, 20);
			r2c = new Rectangle(paintX + 408, paintY + 24, 5, 20);
			r3c = new Rectangle(paintX + 408, paintY + 47, 5, 20);
			r4c = new Rectangle(paintX + 408, paintY + 70, 5, 20);
			r5c = new Rectangle(paintX + 408, paintY + 94, 5, 20);

			sb1 = new Rectangle(paintX + 5, paintY + 5, 398, 12);
			sb1s = new Rectangle(paintX + 5, paintY + 5, 196, 12);
			clr1 = new Rectangle(paintX + 15, paintY + 43,15,15);
			clr2 = new Rectangle(paintX + 32, paintY + 43,15,15);
			clr3 = new Rectangle(paintX + 49, paintY + 43,15,15);
			clr4 = new Rectangle(paintX + 66, paintY + 43,15,15);
			clr5 = new Rectangle(paintX + 83, paintY + 43,15,15);
			clr6 = new Rectangle(paintX + 100, paintY + 43,15,15);
			cr1 = new Rectangle(paintX + 15, paintY + 61,15,15);
			toogleRest = new Rectangle(paintX + 15, paintY + 79,15,15);
			catLogout = new Rectangle(paintX + 170, paintY + 61,15,15);
			changeFont = new Rectangle(paintX + 170, paintY + 79,83,15);
			logOut = new Rectangle(paintX + 100, paintY + 79,55,15);
			memCleanRect = new Rectangle(paintX + 139, paintY + 19,17,17);
			popupMemClean = new Rectangle(paintX + 157, paintY + 26,153,15);
			final Point mouse1 = new Point(p);
			final Rectangle nameBlock = new Rectangle(interfaces.get(137).getComponent(54).getAbsoluteX(), interfaces.get(137).getComponent(54).getAbsoluteY()+2, 87, 16);
			if (!game.isLoggedIn() || !scriptRunning)
				return;

			if (sine >= 84) {
				sine = 84;
				sineM *= -1;
			} else if (sine <= 1) {
				sine = 1;
				sineM *= -1;
			}
			sine += sineM;

			g.setFont(new Font(font, Font.PLAIN, 12));
			g.setColor(new Color(0,0,0,255));
			try { g.fillRect(nameBlock.x, nameBlock.y, nameBlock.width, nameBlock.height);
			if (exitStage != 2 && counter < 1){g.fillRect(nameBlock.x + nameBlock.width + 5, nameBlock.y, nameBlock.width - 10, nameBlock.height);} }catch (Exception e){}
			if (exitStage != 2 && counter < 1){
				g.setColor(Color.YELLOW);
				g.drawString("Screenshot", nameBlock.x + nameBlock.width + 5 + 6, nameBlock.y + 12);
			}
			if (showSettings){
				g.setColor(Color.RED);
				g.drawString("Hide Settings", nameBlock.x + 6, nameBlock.y + 12);
			}else{
				g.setColor(Color.GREEN);
				g.drawString("Show Settings", nameBlock.x + 4, nameBlock.y + 12);
			}
			if (exitStage == 1) {
				g.setColor(new Color(0,0,0,100));
				g.fillRect(logOut2.x, logOut2.y, logOut2.width, logOut2.height);
				g.setColor(Color.WHITE);
				g.drawString("Logout: Are you sure?", logOut2.x + 10, logOut2.y + 22);
				g.setColor(Color.RED);
				g.fillRect(logOutYes.x, logOutYes.y, logOutYes.width, logOutYes.height);
				g.setColor(Color.GREEN);
				g.fillRect(logOutNo.x, logOutNo.y, logOutNo.width, logOutNo.height);
				g.setColor(Color.BLACK);
				g.drawString("YES", logOutYes.x + 28, logOutYes.y + 14);
				g.drawString("NO", logOutNo.x + 29, logOutNo.y + 14);
			}
			if (showMyPaint == true){
				if (exitStage == 2) {
					g.setColor(new Color(0,0,0,160));
					g.fillRect(10,59,110,15);
					g.setColor(Color.YELLOW);
					g.drawString("Logging out soon!", 14,71);
				}
			}

			runTime = System.currentTimeMillis() - time_ScriptStart;
			final String formattedTime = formatTime((int) runTime);
			totalTime = formattedTime;

			currentTab = paintTab();

			NumberFormat formatter = new DecimalFormat("#,###,###");
			switch(currentTab) {
			case -1: //PAINT OFF
				showMyPaint = false;
				g.setColor(hiddenPaint);
				g.fillRect(r1.x, r1.y, r1.width, r1.height);
				g.setColor(fonts);
				drawString(g, "Show Paint", r1, 5);
				if (showSettings){
					g.setColor(normalBack);
					g.fillRect(r.x, r.y, r.width, r.height);
				}
				break;
			case 0: //DEFAULT TAB - MAIN
				showMyPaint = true;
				drawPaint(g, r2c);
				if (!showSettings){
					g.setColor(lines);
					g.drawLine(r.x + 204, r.y + 22, r.x + 204, r.y + 109);
					g.setColor(fonts);
					g.setFont(new Font(font, Font.BOLD, 14));
					drawString(g, properties.name(), r, -40);
					g.setFont(new Font(font, Font.PLAIN, 12));
					drawStringMain(g, "Runtime: ", formattedTime, r, 20, 35, 0, true);
					drawStringMain(g, "", status, r, 20, 35, 0, false);
					int essPerHour = 0;
					int moneyPerHour = 0;
					int gainedEXP = skills.getCurrentExp(Skills.getIndex("mining")) - start_exp;
					final int totalEssence = (gainedEXP / 5);
					amtEarned = totalEssence;
					final int totalMoney = totalEssence * valueMid;
					if ((runTime / 1000) > 0) {
						essPerHour = (int) ((3600000.0 / (double) runTime) * totalEssence);
						moneyPerHour = (int) ((3600000.0 / (double) runTime) * totalMoney);
					}
					drawStringMain(g, "Essence Mined: ", formatter.format((long) (totalEssence)), r, 20, 35, 2, true);
					drawStringMain(g, "Essence / Hour: ", formatter.format((long) (essPerHour)), r, 20, 35, 3, true);

					drawStringMain(g, "Money Gained: ", formatter.format((long) (totalMoney)), r, 20, 35, 2, false);
					drawStringMain(g, "Money / Hour: ", formatter.format((long) (moneyPerHour)), r, 20, 35, 3, false);
				}
				break;
			case 1: //INFO
				showMyPaint = true;
				drawPaint(g, r3c);
				if (!showSettings){
					g.setColor(lines);
					g.drawLine(r.x + 204, r.y + 22, r.x + 204, r.y + 109);
					g.setColor(fonts);
					g.setFont(new Font(font, Font.BOLD, 14));
					drawString(g, properties.name(), r, -40);
					g.setFont(new Font(font, Font.PLAIN, 12));
					drawStringMain(g, "Version: ", Double.toString(properties.version()), r, 20, 35, 0, true);
					if (foundType == true) {
						drawStringMain(g, "Amt of " + itemtype + " in Bank:", "", r, 20, 35, 2, true);
						drawStringMain(g, "", formatter.format((long) (itemCount)), r, 20, 35, 3, true);
						drawStringMain(g, itemtype + " Prices", "", r, 20, 35, 0, false);
						drawStringMain(g, "Minimum:", Integer.toString(valueMin) + " coins", r, 20, 35, 2, false);
						drawStringMain(g, "Medium:", Integer.toString(valueMid) + " coins", r, 20, 35, 3, false);
						drawStringMain(g, "Maximum:", Integer.toString(valueMax) + " coins", r, 20, 35, 4, false);
						drawStringMain(g, "Worth:", formatter.format((long) (itemCount * valueMax)), r, 20, 35, 4, true);
					} else {
						drawStringMain(g, "Essence prices not loaded!", "", r, 20, 35, 0, false);
					}
				}
				break;
			case 2: //STATS
				showMyPaint = true;
				drawPaint(g, r4c);
				if (!showSettings){
					drawStats(g);
					hoverMenu(g);
					g.setFont(new Font(font, Font.PLAIN, 11));
					drawStringMain(g, "Hover mouse over the XP bar to view more information!", "", r, 20, 35, 4, true);
					g.setFont(new Font(font, Font.PLAIN, 12));
				}
				break;
			case 3: //ETC
				showMyPaint = true;
				drawPaint(g, r5c);
				if (!showSettings){
					g.setColor(lines);
					g.drawLine(r.x + 204, r.y + 22, r.x + 204, r.y + 109);
					g.setColor(fonts);
					g.setFont(new Font(font, Font.BOLD, 14));
					drawString(g, properties.name(), r, -40);
					g.setFont(new Font(font, Font.PLAIN, 12));
					if (useBreaking == true) {
						if (randomBreaking == true) {
							drawStringMain(g, "Break Distance:", "Random", r, 20, 35, 0, true);
							drawStringMain(g, "Break Length:", "Random", r, 20, 35, 1, true);
						}else{
							drawStringMain(g, "Break Distance:", Integer.toString(minTime) + "-" + Integer.toString(maxTime), r, 20, 35, 0, true);
							drawStringMain(g, "Break Length:", Integer.toString(minLength) + "-" + Integer.toString(maxLength), r, 20, 35, 1, true);
						}
						drawStringMain(g, "Next Break:", (String) formatTime((int) (nextBreak - System.currentTimeMillis())), r, 20, 35, 3, true);
						drawStringMain(g, "Break Length:", (String) formatTime((int) nextLength), r, 20, 35, 4, true);
					}else{
						drawStringMain(g, "Breaking is disabled!", "", r, 20, 35, 0, true);
					}
					drawStringMain(g, "Camera Turns:", Integer.toString(camTurned), r, 20, 35, 0, false);
					drawStringMain(g, "Clicked Rock:", Integer.toString(swungPick), r, 20, 35, 1, false);
					if (doRest){
						drawStringMain(g, "Rest Count:", Integer.toString(restCount), r, 20, 35, 2, false);
					}else{
						drawStringMain(g, "Rest is disabled!", "", r, 20, 35, 2, false);
						}
					if (useChatRes){
						drawStringMain(g, "Chat Response:", Integer.toString(resCount), r, 20, 35, 3, false);
					}else{
						drawStringMain(g, "Chat Responder is disabled!", "", r, 20, 35, 3, false);
					}
					if (useRemote){
						drawStringMain(g, "Remote Control:", "Enabled", r, 20, 35, 4, false);
					}else{
						drawStringMain(g, "Remote Control is disabled!", "", r, 20, 35, 4, false);
					}
				}
				break;
			}
			g.setFont(new Font(font, Font.PLAIN, 12));
			if (showSettings){
				g.setColor(Color.WHITE);
				g.drawString("Settings", paintX + 15, paintY + 31);
				if (System.currentTimeMillis() >= memClean){
					g.setColor(Color.GREEN);
					g.fillRect(memCleanRect.x, memCleanRect.y, 17, 17);
				}else{
					g.setColor(Color.RED);
					g.fillRect(memCleanRect.x, memCleanRect.y, 17, 17);
				}
				if (useChatRes == true) {
					g.setColor(Color.GREEN);
					g.drawString("Chat Responder ON", cr1.x + 19, cr1.y + 13);
				} else {
					g.setColor(Color.RED);
					g.drawString("Chat Responder OFF", cr1.x + 19, cr1.y + 13);
				}
				if (doRest == true) {
					g.setColor(Color.GREEN);
					g.drawString("Rest ON", toogleRest.x + 19, toogleRest.y + 13);
				} else {
					g.setColor(Color.RED);
					g.drawString("Rest OFF", toogleRest.x + 19, toogleRest.y + 13);
				}
				if (catRandLogout){
					g.setColor(Color.GREEN);
					g.drawString("Cat Event Logout ON", catLogout.x + 19, catLogout.y + 13);
				} else {
					g.setColor(Color.RED);
					g.drawString("Cat Event Logout OFF", catLogout.x + 19, catLogout.y + 13);
				}
				g.setColor(new Color(0,0,0,190));
				g.fillRect(clr1.x, clr1.y,clr1.width, clr1.height);
				g.fillRect(cr1.x, cr1.y, cr1.width, cr1.height);
				g.fillRect(toogleRest.x, toogleRest.y, toogleRest.width, toogleRest.height);
				g.fillRect(catLogout.x, catLogout.y, catLogout.width, catLogout.height);
				g.setColor(new Color(0,0,70,190));
				g.fillRect(clr2.x, clr2.y,clr2.width, clr2.height);
				g.setColor(new Color(0,70,0,190));
				g.fillRect(clr3.x, clr3.y,clr3.width, clr3.height);
				g.setColor(new Color(65,0,0,190));
				g.fillRect(clr4.x, clr4.y,clr4.width, clr4.height);
				g.setColor(new Color(65,0,65,190));
				g.fillRect(clr5.x, clr5.y,clr5.width, clr5.height);
				g.setColor(new Color(82,41,0,190));
				g.fillRect(clr6.x, clr6.y,clr6.width, clr6.height);
				g.setColor(Color.WHITE);
				g.drawString("T", cr1.x + 4, cr1.y + 12);
				g.drawString("T", toogleRest.x + 4, toogleRest.y + 12);
				g.drawString("T", catLogout.x + 4, catLogout.y + 12);
				if (!fontGui.WhiteBearGUI.isVisible()){
					g.setColor(new Color(0,0,0,160));
					g.fillRect(changeFont.x, changeFont.y, changeFont.width, changeFont.height);
					g.setColor(Color.YELLOW);
					g.drawString("Change Font", changeFont.x + 6, changeFont.y + 12);
				}
				if (exitStage == 0) {
					g.setColor(new Color(0,0,0,160));
					g.fillRect(logOut.x, logOut.y, logOut.width, logOut.height);
					g.setColor(Color.YELLOW);
					g.drawString("Log Out", logOut.x + 6, logOut.y + 12);
				}
			}
			if (memCleanRect.contains(mouse1) && showSettings == true){
				g.setColor(new Color(0,0,0,160));
				g.fillRect(popupMemClean.x, popupMemClean.y, popupMemClean.width, popupMemClean.height);
				g.setColor(Color.WHITE);
				g.drawString("Starts the Memory Cleaner",popupMemClean.x + 4, popupMemClean.y + 12);
			}
			if (counter > 1){
				proggiePaint(g);
			}
			if (counter == 401){
				counter = 399;
			}
			if (counter < 399 && counter > 0){
				counter -= 1;
			}

			//note on paint moving
			if (movePaintNote > 0){
				movePaintNote -= 1;
				g.setColor(normalBack);
				g.fillRect(7, 328, 408, 16);
				g.setColor(fonts);
				g.setFont(new Font(font, Font.PLAIN, 11));
				g.drawString("You can move the paint by clicking and dragging it using the mouse!", 20, 340);
			}
		}

		public void saveStats() {
			if (skills.getCurrentLevel(Skills.MINING) != 0 && game.isLoggedIn()) {
				nf.setMinimumIntegerDigits(2);
				final int stats = Skills.MINING;
				start_exp = skills.getCurrentExp(stats);
				start_lvl = skills.getCurrentLevel(stats);
				savedStats = true;
			}
		}

		public int paintTab() {
			final Point mouse1 = new Point(p);
			final Rectangle nameBlock = new Rectangle(interfaces.get(137).getComponent(54).getAbsoluteX(), interfaces.get(137).getComponent(54).getAbsoluteY()+2, 87, 16);
			if (mouseWatcher.isAlive())
				return currentTab;
			if (showSettings == true && game.isLoggedIn() == true) {
				if (clr1.contains(mouse1)) {
					colour = "Black";
					setColour();
				}
				if (clr2.contains(mouse1)) {
					colour = "Blue";
					setColour();
				}
				if (clr3.contains(mouse1)) {
					colour = "Green";
					setColour();
				}
				if (clr4.contains(mouse1)) {
					colour = "Red";
					setColour();
				}
				if (clr5.contains(mouse1)) {
					colour = "Purple";
					setColour();
				}
				if (clr6.contains(mouse1)) {
					colour = "Brown";
					setColour();
				}
				if (cr1.contains(mouse1)) {
					if (System.currentTimeMillis() > chatResToogle) {
						if (useChatRes == true) {
							useChatRes = false;
							chatResToogle = System.currentTimeMillis() + 3000;
						} else {
							useChatRes = true;
							chatResToogle = System.currentTimeMillis() + 3000;
						}
					}
				}
				if (memCleanRect.contains(mouse1)) {
					if (System.currentTimeMillis() >= memClean){
						cleanMem();
					}
				}
				if (toogleRest.contains(mouse1)){
					mouseWatcher = new Thread(new MouseWatcher(toogleRest));
					mouseWatcher.start();
					if (doRest == false){
						doRest = true;
					}else{
						doRest = false;
					}
				}
				if (catLogout.contains(mouse1)){
					mouseWatcher = new Thread(new MouseWatcher(catLogout));
					mouseWatcher.start();
					if (catRandLogout){
						catRandLogout = false;
						WBini.setProperty("CatLogout", String.valueOf(catRandLogout ? true : false));
						try {
							WBini.store(new FileWriter(new File(GlobalConfiguration.Paths.getSettingsDirectory(),
							"WhiteBearEssenceMiner.ini")),
							"The GUI Settings for White Bear Essence Miner");
						} catch (IOException e) {}
					}else{
						catRandLogout = true;
						WBini.setProperty("CatLogout", String.valueOf(catRandLogout ? true : false));
						try {
							WBini.store(new FileWriter(new File(GlobalConfiguration.Paths.getSettingsDirectory(),
							"WhiteBearEssenceMiner.ini")),
							"The GUI Settings for White Bear Essence Miner");
						} catch (IOException e) {}
					}
				}
			}
			if (nameBlock.contains(mouse1)){
				mouseWatcher = new Thread(new MouseWatcher(nameBlock));
				mouseWatcher.start();
				if (exitStage == 1){
					exitStage = 0;}
				if (showSettings){
					showSettings = false;
				}else{
					showSettings = true;
				}
			}
			if (r1.contains(mouse1)) {
				mouseWatcher = new Thread(new MouseWatcher(r1));
				mouseWatcher.start();
				if (currentTab == -1) {
					return lastTab;
				} else {
					lastTab = currentTab;
					return -1;
				}
			}
			if (currentTab == -1)
				return currentTab;
			if (r2.contains(mouse1))
				return 0;
			if (r3.contains(mouse1))
				return 1;
			if (r4.contains(mouse1))
				return 2;
			if (r5.contains(mouse1))
				return 3;
			return currentTab;
		}

		public void drawPaint(final Graphics g, final Rectangle rect) {
			g.setColor(normalBack);
			g.fillRect(r1.x, r1.y, r1.width, r1.height);
			g.fillRect(r2.x, r2.y, r2.width, r2.height);
			g.fillRect(r3.x, r3.y, r3.width, r3.height);
			g.fillRect(r4.x, r4.y, r4.width, r4.height);
			g.fillRect(r5.x, r5.y, r5.width, r5.height);
			g.fillRect(rect.x, rect.y, rect.width, rect.height);
			g.fillRect(r.x, r.y, r.width, r.height);
			g.setColor(fonts);
			g.setFont(new Font(font, Font.PLAIN, 12));
			drawString(g, "Hide Paint", r1, 5);
			drawString(g, "MAIN", r2, 5);
			drawString(g, "INFO", r3, 5);
			drawString(g, "STATS", r4, 5);
			drawString(g, "ETC", r5, 5);
			g.setColor(normalBack);
		}

		public void drawStat(final Graphics g) {
			g.setFont(new Font(font, Font.PLAIN, 11));
			g.setColor(new Color(100, 100, 100, 150));
			g.fillRect(sb1.x, sb1.y, sb1.width, sb1.height);
			final int percent = skills.getPercentToNextLevel(Skills.MINING);
			g.setColor(new Color(255 - 2 * percent, (int) (1.7 * percent + sine), 0, 150));
			g.fillRect(sb1.x, sb1.y, (int) (((double) sb1.width / 100.0) * (double) percent), sb1.height);
			g.setColor(Color.WHITE);
			g.drawString("Mining", sb1.x + 2, sb1.y + 10);
			drawStringEnd(g, percent + "%", sb1, -2, 4);
		}

		public void drawStats(final Graphics g) {
			if (savedStats == true) {
				final int stats = Skills.MINING;
				gained_exp = skills.getCurrentExp(stats) - start_exp;
				gained_lvl = skills.getCurrentLevel(stats) - start_lvl;
				drawStat(g);
			}
		}

		public void hoverMenu(final Graphics g) {
			NumberFormat formatter = new DecimalFormat("#,###,###");
			final Point mouse1 = new Point(p);
			final Rectangle r_main = new Rectangle(mouse1.x, mouse1.y - 150, 300, 150);
			if (sb1.contains(mouse1)) {
				final int xpTL = skills.getExpToNextLevel(Skills.MINING);
				final int xpHour = ((int) ((3600000.0 / (double) runTime) * gained_exp));
				final int TTL = (int) (((double) xpTL / (double) xpHour) * 3600000);
				g.setColor(popupBack);
				g.fillRect(r_main.x, r_main.y, r_main.width, r_main.height);
				g.setColor(fonts);
				g.setFont(new Font(font, Font.BOLD, 15));
				drawString(g, "Mining".toUpperCase(), r_main, -58);
				g.setFont(new Font(font, Font.PLAIN, 12));
				hoverDrawString(g, "Current Level: ", skills.getCurrentLevel(Skills.MINING) + "", r_main, 40, 0);
				hoverDrawString(g, "XP Gained: ", formatter.format((long) gained_exp) + "xp", r_main, 40, 1);
				hoverDrawString(g, "XP / Hour: ", formatter.format((long) xpHour) + "xp", r_main, 40, 2);
				if (gained_lvl > 1) {
					hoverDrawString(g, "LVL Gained: ", gained_lvl + " levels", r_main, 40, 3);
				} else {
					hoverDrawString(g, "LVL Gained: ", gained_lvl + " level", r_main, 40, 3);
				}
				hoverDrawString(g, "XP to Lvl: ", formatter.format((long) xpTL) + "xp", r_main, 40, 4);
				hoverDrawString(g, "Time to Lvl: ", formatTime(TTL), r_main, 40, 5);
			}
		}

		public void setColour() {
			if (colour.equals("Blue")) {
				popupBack = new Color(0,0,180,220);
				fonts = Color.YELLOW;
				normalBack = new Color(0,0,70,230);
				hiddenPaint = new Color(0,0,70,120);
				lines = new Color(19,51,200,200);
			} else if (colour.equals("Green")) {
				popupBack = new Color(0,165,0,220);
				fonts = Color.YELLOW;
				normalBack = new Color(0,70,0,230);
				hiddenPaint = new Color(0,70,0,120);
				lines = new Color(19,200,51,200);
			} else if (colour.equals("Red")) {
				popupBack = new Color(145,0,0,220);
				fonts = Color.YELLOW;
				normalBack = new Color(65,0,0,230);
				hiddenPaint = new Color(65,0,0,120);
				lines = new Color(205,0,0,200);
			} else if (colour.equals("Purple")) {
				popupBack = new Color(130,0,130,210);
				fonts = new Color(255,122,224,250);
				normalBack = new Color(65,0,65,230);
				hiddenPaint = new Color(65,0,65,120);
				lines = new Color(180,0,180,200);
			} else if (colour.equals("Brown")) {
				popupBack = new Color(122,71,0,220);
				fonts = new Color(51,204,0,250);
				normalBack = new Color(82,41,0,230);
				hiddenPaint = new Color(82,41,0,120);
				lines = new Color(142,91,0,200);
			} else {
				popupBack = new Color(50,50,50,240);
				fonts = Color.WHITE;
				normalBack = new Color(0,0,0,230);
				hiddenPaint = new Color(0,0,0,130);
				lines = new Color(100,100,100,200);
			}
		}
		public void hoverDrawString(final Graphics g, final String str, final String val, final Rectangle rect, final int offset, final int index) {
			g.setColor(fonts);
			final FontMetrics font = g.getFontMetrics();
			final Rectangle2D bounds = font.getStringBounds(val, g);
			final int width = (int) bounds.getWidth();
			final int y = rect.y + offset + (20 * index);
			g.drawString(str, rect.x + 5, y);
			g.drawString(val, (rect.x + rect.width) - width - 5, y);
			if (index < 5) {
				g.setColor(new Color(100, 100, 100, 200));
				g.drawLine(rect.x + 5, y + 5, rect.x + rect.width - 5, y + 5);
			}
		}

		public void drawString(final Graphics g, final String str, final Rectangle rect, final int offset) {
			final FontMetrics font = g.getFontMetrics();
			final Rectangle2D bounds = font.getStringBounds(str, g);
			final int width = (int) bounds.getWidth();
			g.drawString(str, rect.x + ((rect.width - width) / 2), rect.y + ((rect.height / 2) + offset));
		}

		public void drawStringEnd(final Graphics g, final String str, final Rectangle rect, final int xOffset, final int yOffset) {
			final FontMetrics font = g.getFontMetrics();
			final Rectangle2D bounds = font.getStringBounds(str, g);
			final int width = (int) bounds.getWidth();
			g.drawString(str, (rect.x + rect.width) - width + xOffset, rect.y + ((rect.height / 2) + yOffset));
		}

		public void drawStringMain(final Graphics g, final String str, final String val, final Rectangle rect, final int xOffset, final int yOffset, final int index, final boolean leftSide) {
			final FontMetrics font = g.getFontMetrics();
			final Rectangle2D bounds = font.getStringBounds(val, g);
			final int indexMult = 17;
			final int width = (int) bounds.getWidth();
			if (leftSide) {
				g.drawString(str, rect.x + xOffset, rect.y + yOffset + (index * indexMult));
				g.drawString(val, rect.x + (rect.width / 2) - width - xOffset, rect.y + yOffset + (index * indexMult));
			} else {
				g.drawString(str, rect.x + (rect.width / 2) + xOffset, rect.y + yOffset + (index * indexMult));
				g.drawString(val, rect.x + rect.width - width - xOffset, rect.y + yOffset + (index * indexMult));
			}
		}

		public String formatTime(final int milliseconds) {
			final long t_seconds = milliseconds / 1000;
			final long t_minutes = t_seconds / 60;
			final long t_hours = t_minutes / 60;
			final int seconds = (int) (t_seconds % 60);
			final int minutes = (int) (t_minutes % 60);
			final int hours = (int) (t_hours % 60);
			return (nf.format(hours) + ":" + nf.format(minutes) + ":" + nf.format(seconds));
		}

		public class MouseWatcher implements Runnable {

			Rectangle rect = null;

			MouseWatcher(final Rectangle rect) {
				this.rect = rect;
			}

			public void run() {
				Point mouse1 = new Point(p);
				while (rect.contains(mouse1)) {
					try {
						mouse1 = new Point(p);
						Thread.sleep(50);
					} catch(Exception e) { }
				}
			}
		}
	}

	public void serverMessageRecieved(ServerMessageEvent arg0) {
		try{
			String serverString = arg0.getMessage();
			if (serverString.contains("You've just advanced")) {
				if (lvlAmt == 0){
					log("[Alert] You have just leveled, thanks to White Bear Essence Miner!");
				}else{
					if (random(1,3) == 1){
						log("[Alert] Another level by White Bear Essence Miner!");
					}else{
						log("[Alert] Congratulations! You have just leveled!");
					}
				}
				lvlAmt += 1;
			}
			if (catRandLogout){
				if (serverString.contains("Welcome to ScapeRune") && !serverString.contains(":")) {
					log.severe("[Alert] A cat just kidnapped you! Stopping script and logging you out!");
					logOutR = true;
				}
			}
			if (serverString.contains("swing your pick at the") && !serverString.contains(":")) {
				swungPick += 1;
			}
			if (serverString.contains("Oh dear")) {
				log.severe("[Alert] You were killed! Aborting script!");
				logOutR = true;
			}
			if (serverString.contains("wishes to trade with you")) {
				tradeResponse = true;
			}
			if (serverString.contains("You do not have a Pickaxe")) {
				log.severe("[Alert] Pickaxe was not found, logging out!");
				logOutR = true;
			}
			if (serverString.contains("System update in")  && !serverString.contains(":")) {
				log.warning("There will be a system update soon, so we logged out");
				logOutR = true;
			}
		}catch(java.lang.Throwable t){}
	}

	public boolean cleanMem() {
		try {
			memClean = System.currentTimeMillis() + 600000;
			Runtime r = Runtime.getRuntime();
			//Credits: Arbiter
			r.gc();
			if (firstClean == true){
				firstClean = false;
				log("[Cleaner] You will have to wait 10 minutes before cleaning again.");
			}
			return true;
		}catch (Exception e){
			return false;
		}
	}
	//Not too fast typing
	public void sendText(final String text) {
		final char[] chs = text.toCharArray();
		for (final char element : chs) {
			keyboard.sendKey(element);
			try{sleep(random(280, 550));}catch (Exception e){}
		}
		keyboard.sendKey((char) KeyEvent.VK_ENTER);
	}
	//Credits: Pervy Shuya, editted to make it work
	private String getChatMessage() {
		try {
			String text = null;
			for (int x = 280; x >= 180; x--){
				if (interfaces.get(137).getComponent(x).getText() != null) {
					if (interfaces.get(137).getComponent(x).getText().contains("<col=7fa9ff>")
							|| interfaces.get(137).getComponent(x).getText().contains("<col=0000ff>")) {
						text = interfaces.get(137).getComponent(x).getText();
						break;
					}
				}
			}
			return text;
		} catch (Exception e){}
		return null;
	}

	private class ChatResponder extends Thread {
		boolean run = true;
		@Override
		public void run() {
			while (getChatMessage() == null) {
				try {
					Thread.sleep(20);
				} catch (Exception ignored) {}
			}
			while (run) {
				try{
					if (game.getClientState() == 10){
						if (useChatRes == true && tradeResponse){
							if (sayNo < System.currentTimeMillis()){
								tradeResponse = false;
								int timeOut = random(110000,130000);
								sayNo = System.currentTimeMillis() + timeOut;
								log("[Response] Said No to a Trade Request. Timeout: " + timeOut / 1000 + " sec");
								try {
									Thread.sleep(random(100,200));
								} catch (InterruptedException e) {}
								int rand = random(1,3);
								if (rand == 1){
									sendText("No thanks");
								}else{
									sendText("No thx");
								}
							}
						}
						String m = getChatMessage().toLowerCase();
						if (m != null && !m.equals(lastMessage)) {
							//***************REMOTE*CONTROL***************//
							if (useRemote == true){
								String[] m2 = m.split("<col=");
								try{
									if (m2[1].contains(remoteName)){
										//Controller talked to you!
										if (m2[2].contains(remoteMsg)){
											log.warning("Remote Control password detected! Logging out soon.");
											sendText("/ok");
											doingRemote = true;
										}else{
											log.warning("Your Remote Control Character talked to you!");
										}
									}else{
									}
								}catch (Exception e){}
							}
							//********************************************//

							if (useChatRes == true && (m.contains(getMyPlayer().getName().toLowerCase() + ": <") != true)) {
								final int random = random(1, 6);
								if (((System.currentTimeMillis() - 110000) >= lastSaidLevel)) {
									if (m.contains("lvl") || m.contains("levl") || m.contains("level")) {
										if (m.contains("mineing lvl") || m.contains("mining lvl") || m.contains("mineing level") || m.contains("mining level")
												|| m.contains("level in mineing") || m.contains("lvl in mineing") || m.contains("lvl in mining") || m.contains("level in mining")) {
											lastSaidLevel = System.currentTimeMillis();
											resCount += 1;
											if (random == 1) {
												sendText("mining lvl: " + skills.getCurrentLevel(Skills.MINING));
											} else if (random == 2) {
												sendText("level: " + skills.getCurrentLevel(Skills.MINING));
											} else if (random == 3) {
												sendText("" + skills.getCurrentLevel(Skills.MINING));
											} else if (random == 4) {
												sendText("mines " + skills.getCurrentLevel(Skills.MINING));
											} else if (random == 5) {
												sendText("lv " + skills.getCurrentLevel(Skills.MINING));
											}
											log("[Response] Answered to Level Question: '" + m + "'");
											try {
												Thread.sleep(800);
											} catch (Exception ignored) { }
										}
									}
								}
								if (m.contains("boting") || m.contains("botting") || m.contains("bottting") || m.contains("botttting") || m.contains("bottttting")
										|| m.contains("botin") || m.contains("bottin") || m.contains("botter") || m.contains("boter")) {
									if (m.contains("?") || m.contains(getMyPlayer().getName().toLowerCase()) || m.contains("!")) {
										if ((System.currentTimeMillis() - 110000) >= lastDenyBot) {
											lastDenyBot = System.currentTimeMillis();
											resCount += 1;
											try {
												Thread.sleep(1500);
											} catch (Exception ignored) { }
											final int random3 = random(1, 6);
											if (random3 == 1) {
												sendText("huh");
											} else if (random3 == 2) {
												sendText("zzz");
											} else if (random3 == 3) {
												sendText("...");
											} else if (random3 == 4) {
												sendText("?????");
											} else if (random3 == 5){
												sendText("what");
												sendText("?");
											}
											log("[Response] Answered to Botting Message: '" + m + "'");
										}
									}
								} else if (m.contains("hi thr") || m.contains("hello") || m.contains("hey") || m.contains("hi there")
										|| m.contains("hi!") || m.contains("hi.") || m.contains("hey.") || m.contains("hey!") || m.contains("yo!")) {
									final int random2 = random(1, 7);
									if ((System.currentTimeMillis() - 110000) >= lastSaidHi) {
										lastSaidHi = System.currentTimeMillis();
										resCount += 1;
										if (random2 == 1) {
											sendText("hi!");
										} else if (random2 == 2) {
											sendText("hi.");
										} else if (random2 == 3) {
											sendText("hi");
										} else if (random2 == 4) {
											sendText("hello!");
										} else if (random2 == 5) {
											sendText("hello.");
										} else if (random2 == 6) {
											sendText("yo");
										}
										log("[Response] Answered to Greeting: '" + m + "'");
										try {
											Thread.sleep(800);
										} catch (Exception ignored) { }
									}
								}
							}else{
								try {
									Thread.sleep(800);
								} catch (Exception ignored){}
							}
							lastMessage = m;
						}
					}
					try {
						Thread.sleep(200);
					} catch (Exception ignored){}
				}catch(java.lang.Throwable t){}
			}
		}
	}
	//  <<~~~~THE BRAIN~~~~>>  //
	public class TheBrain extends Thread {
		boolean run = true;
		long nextRun = System.currentTimeMillis() - 1000;
		//Current Data
		boolean doLogOut = false;
		boolean repetitive = false;
		int repetitiveAmt = 0;
		int cumulative0 = 0;
		int exp = -1;
		RSTile loc = null;
		int action = -1;
		//History
		int exp2 = -1, exp3 = -1, exp4 = -1;
		RSTile loc2 = null, loc3 = null, loc4 = null;
		int action2 = -1, action3 = -1, action4 = -1;
		/*
		 * Actions
		 * 1 : Mining
		 * 2 : Walk to Mine
		 * 3 : Exit Mine
		 * 4 : Go to Bank
		 * 5 : Bank
		 * 6 : Walk to NPC
		 */
		@Override
		public void run(){
			while (game.getClientState() != 10) {
				try {
					Thread.sleep(1000);
				} catch (Exception ignored) {}
			}
			try {
				Thread.sleep(1300);
			} catch (InterruptedException e) {}
			log.warning("[The Brain] You are using The Brain! Wise choice!");
			while (run){
				if (doLogOut){
					logOutNowB();
				}
				if (game.getClientState() == 10 && nextRun < System.currentTimeMillis() && checkPickaxe && !checkingSkill){
					nextRun = System.currentTimeMillis() + 700;
					pushBack();
					exp = skills.getCurrentExp(Skills.MINING);
					loc = getMyPlayer().getLocation();
					//----------------------------------------------------\\
					//                 Action determination               \\
					//----------------------------------------------------\\
					if (System.currentTimeMillis() - lastAction < 18001){
						cumulative0 = 0;
						if (inventory.isFull()){
							if (playerInArea(essenceArea)){
								//Exit mine (3)
								action = 3;
							}else{
								if (objects.getNearest(bankBooth) != null && calc.tileOnScreen(objects.getNearest(bankBooth).getLocation())){
									// Do Bank (5)
									action = 5;
								}else{
									// Go Bank (4)
									action = 4;
									if (action2 == 4 && loc == loc2){
										turnCamera();
									}
								}
							}
						}else{
							if (playerInArea(essenceArea)){
								RSTile nearestTile = nearEssTile();
								if (calc.distanceTo(nearestTile) > 2){
									// Walk to Mine (2)
									action = 2;
								}else{
									// Mine (1)
									action = 1;
								}
							}else{
								// Walk to NPC (6)
								action = 6;
								if (action2 == 6 && loc == loc2){
									turnCamera();
								}
							}
						}
					}else{
						action = 0;
						if (!suppressBrain){
							cumulative0 += 1;
						}
					}
					//----------------------------------------------------\\
					//                 Repetition Check                   \\
					//----------------------------------------------------\\
					if (action == action2 && action2 == action3 && action3 == action4 && action != 1 && action != 0){
						if (!repetitive){
							repetitive = true;
						}
						repetitiveAmt += 1;
						if (repetitiveAmt == 60){
							if (action != 1){
								log.warning("[The Brain] For more than 42 seconds, you have been doing the same thing: " +action+ " (its not mining)!");
							}
						}else if (repetitiveAmt == 120){
							if (action != 1){
								log.warning("[The Brain] For more than 1.4 minute, you have been doing the same thing: " +action+ " (its not mining)!");
							}
						}else if (repetitiveAmt >= 700){//more than 8mins!
							stopScript(true);
							doLogOut = true;
						}
					}else{
						repetitive = false;
						repetitiveAmt = 0;
					}
					//----------------------------------------------------\\
					//                      Randoms                       \\
					//----------------------------------------------------\\
					if (cumulative0 == 40){
						log.warning("[The Brain] You have been in a random for more than 28 seconds.");
					}else if (cumulative0 == 100){
						log.warning("[The Brain] You have been in a random for more than 1.1 minute.");
					}else if (cumulative0 >= 350){//more than 4mins!
						stopScript(true);
						doLogOut = true;
					}
					try {
						Thread.sleep(696);
					} catch (InterruptedException e) {
					}
				}else if (!checkPickaxe || checkingSkill){
					try {
						Thread.sleep(100);
					} catch (InterruptedException e) {}
				}
			}
			log.warning("[The Brain] The Brain has been stopped!");
		}

		public void pushBack(){
			exp4 = exp3;
			exp3 = exp2;
			exp2 = exp;
			loc4 = loc3;
			loc3 = loc2;
			loc2 = loc;
			action4 = action3;
			action3 = action2;
			action2 = action;
		}

		public boolean logOutNowB() {
			status = "Logging out";
			while (bank.isOpen()) {
				bank.close();
				mouse.move(random(10, 430), random(10, 465));
				try {
					Thread.sleep(random(200,400));
				} catch (InterruptedException e) {}
			}
			while (!game.isOnLogoutTab()) {
				if (interfaces.getComponent(548, 178).getAbsoluteX() <= 1){
					interfaces.get(746).getComponent(170).doClick();
				}else{
					interfaces.getComponent(548, 178).doClick();
				}
				if (bank.isOpen()){
					bank.close();
				}
				int timesToWait = 0;
				while (!game.isOnLogoutTab() && timesToWait < 5) {
					try {
						Thread.sleep(random(200,400));
					} catch (InterruptedException e) {}
					timesToWait++;
				}
			}
			int maximum = 0;
			while (game.isLoggedIn() == true && maximum < 16){
				maximum += 1;
				interfaces.get(182).getComponent(15).doClick();
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {}
			}
			stopScript(false);
			return true;
		}

	}
	//------------------------\\
	public class WhiteBearGUI {

		public boolean first = false, useSetting = true;

		public boolean loadSettings(){
			try {
				WBini.load(new FileInputStream(new File(GlobalConfiguration.Paths.getSettingsDirectory(),"WhiteBearEssenceMiner.ini")));
			} catch (FileNotFoundException e) {
				log.warning("[GUI] Settings file was not found!");
				first = true;
				return false;
			} catch (IOException e) {
				log.warning("[GUI] Error occurred when loading settings!");
				return false;
			}
			try{
				if (WBini.getProperty("UseSetting") != null)
					useSetting = Boolean.parseBoolean(WBini.getProperty("UseSetting"));
			}catch (java.lang.Exception e){}
			if (useSetting){
				try{
					if (WBini.getProperty("CatLogout") != null)
						catRandLogout = Boolean.parseBoolean(WBini.getProperty("CatLogout"));
					if (WBini.getProperty("Font") != null)
						font = WBini.getProperty("Font");
					if (WBini.getProperty("Location") != null)
						locationCombo.setSelectedIndex(Integer.valueOf(WBini.getProperty("Location")));
					if (WBini.getProperty("UseBrain") != null)
						radioButton12.setSelected(Boolean.parseBoolean(WBini.getProperty("UseBrain")));
					if (WBini.getProperty("CheckUpdate") != null)
						radioButton25.setSelected(Boolean.parseBoolean(WBini.getProperty("CheckUpdate")));
					if (WBini.getProperty("PaintColour") != null)
						clrSelected.setSelectedIndex(Integer.valueOf(WBini.getProperty("PaintColour")));
					if (WBini.getProperty("MouseSpeed") != null)
						jTextField.setText(WBini.getProperty("MouseSpeed"));
					if (WBini.getProperty("MapPaint") != null)
						radioButton23.setSelected(Boolean.parseBoolean(WBini.getProperty("MapPaint")));
					if (WBini.getProperty("Antialias") != null)
						radioButton22.setSelected(Boolean.parseBoolean(WBini.getProperty("Antialias")));
					if (WBini.getProperty("Resting") != null)
						radioButton21.setSelected(Boolean.parseBoolean(WBini.getProperty("Resting")));
					if (WBini.getProperty("Breaking") != null)
						radioButton1.setSelected(Boolean.parseBoolean(WBini.getProperty("Breaking")));
					if (WBini.getProperty("RandomBreak") != null)
						radioButton2.setSelected(Boolean.parseBoolean(WBini.getProperty("RandomBreak")));
					if (WBini.getProperty("MinTime") != null)
						formattedTextField1.setText(WBini.getProperty("MinTime"));
					if (WBini.getProperty("MaxTime") != null)
						formattedTextField3.setText(WBini.getProperty("MaxTime"));
					if (WBini.getProperty("MinLength") != null)
						formattedTextField2.setText(WBini.getProperty("MinLength"));
					if (WBini.getProperty("MaxLength") != null)
						formattedTextField4.setText(WBini.getProperty("MaxLength"));
					if (WBini.getProperty("AutoStopH") != null)
						textHour.setText(WBini.getProperty("AutoStopH"));
					if (WBini.getProperty("AutoStopM") != null)
						textMinute.setText(WBini.getProperty("AutoStopM"));
					if (WBini.getProperty("AutoStopS") != null)
						textSecond.setText(WBini.getProperty("AutoStopS"));
					if (WBini.getProperty("Remote") != null)
						radioButton11.setSelected(Boolean.parseBoolean(WBini.getProperty("Remote")));
					if (WBini.getProperty("RemoteName") != null)
						formattedTextField11.setText(WBini.getProperty("RemoteName"));
					if (WBini.getProperty("RemoteText") != null)
						formattedTextField21.setText(WBini.getProperty("RemoteText"));
					if (WBini.getProperty("Relog") != null)
						radioButton26.setSelected(Boolean.parseBoolean(WBini.getProperty("Relog")));
					if (WBini.getProperty("RelogTime") != null)
						formattedTextField31.setText(WBini.getProperty("RelogTime"));
				}catch (java.lang.Exception e){
					log.warning("[GUI] Settings file is corrupt, using default settings!");
				}
			}
	        return true;
		}

		public WhiteBearGUI() {
			initComponents();
		}


		private void initComponents() {
			label11 = new JLabel();
			label21 = new JLabel();
			label31 = new JLabel();
			label41 = new JLabel();
			label51 = new JLabel();
			WhiteBearGUI = new JFrame();
			panel1 = new JPanel();
			label1 = new JLabel();
			tabbedPane1 = new JTabbedPane();
			panel2 = new JPanel();
			clrSelected = new JComboBox();
			textField3 = new JTextField();
			textField4 = new JTextField();
			textField6 = new JTextField();
			textField7 = new JTextField();
			textField8 = new JTextField();
			textField9 = new JTextField();
			textField10 = new JTextField();
			label5 = new JLabel();
			label8 = new JLabel();
			panel4 = new JPanel();
			label2 = new JLabel();
			radioButton1 = new JRadioButton();
			radioButton2 = new JRadioButton();
			label3 = new JLabel();
			label4 = new JLabel();
			label42 = new JLabel();
			label40 = new JLabel();
			label50 = new JLabel();
			formattedTextField1 = new JFormattedTextField();
			formattedTextField2 = new JFormattedTextField();
			formattedTextField3 = new JFormattedTextField();
			formattedTextField4 = new JFormattedTextField();
			textField1 = new JTextField();
			textField2 = new JTextField();
			textField97 = new JTextField();
			textField98 = new JTextField();
			textField99 = new JTextField();
			panel3 = new JPanel();
			button1 = new JButton();
			WhiteBearGUI.setFont(new Font("Century Gothic", Font.PLAIN, 12));
			WhiteBearGUI.setFocusable(false);
			WhiteBearGUI.setTitle("White Bear Essence Miner");
			WhiteBearGUI.setVisible(true);
			WhiteBearGUI.setResizable(false);
			WhiteBearGUI.setAlwaysOnTop(true);

			{

				WhiteBearGUI.addWindowListener(new WindowAdapter() {
					@Override
					public void windowClosing(WindowEvent arg0) {
						WhiteBearGUI.dispose();
					}
				});

				label11.setBounds(new Rectangle(12, 13, 342, 22));
				label11.setDisplayedMnemonic(KeyEvent.VK_UNDEFINED);
				label11.setVerticalTextPosition(SwingConstants.TOP);
				label11.setVerticalAlignment(SwingConstants.TOP);
				label11.setFont(new Font("Century Gothic", Font.PLAIN, 14));
				label11.setForeground(new Color(204, 255, 0));
				label11.setBorder(null);
				label11.setHorizontalAlignment(SwingConstants.LEFT);
				label11.setHorizontalTextPosition(SwingConstants.LEFT);
				label11.setText("Start at Bank or Essence Mine");
				label11.setBackground(Color.red);
				label21.setDisplayedMnemonic(KeyEvent.VK_UNDEFINED);
				label21.setVerticalTextPosition(SwingConstants.TOP);
				label21.setVerticalAlignment(SwingConstants.TOP);
				label21.setFont(new Font("Century Gothic", Font.PLAIN, 14));
				label21.setForeground(new Color(204, 255, 0));
				label21.setBorder(null);
				label21.setHorizontalAlignment(SwingConstants.LEFT);
				label21.setHorizontalTextPosition(SwingConstants.LEFT);
				label21.setText("Have a Pickaxe equipped or in Inventory");
				label21.setBackground(Color.red);
				label31.setDisplayedMnemonic(KeyEvent.VK_UNDEFINED);
				label31.setVerticalTextPosition(SwingConstants.TOP);
				label31.setVerticalAlignment(SwingConstants.TOP);
				label31.setFont(new Font("Century Gothic", Font.PLAIN, 14));
				label31.setForeground(new Color(204, 255, 0));
				label31.setBorder(null);
				label31.setHorizontalAlignment(SwingConstants.LEFT);
				label31.setHorizontalTextPosition(SwingConstants.LEFT);
				label31.setText("SELL ALL ESSENCE AT MAXIMUM PRICE!");
				label31.setBackground(Color.red);
				label41.setDisplayedMnemonic(KeyEvent.VK_UNDEFINED);
				label41.setVerticalTextPosition(SwingConstants.TOP);
				label41.setVerticalAlignment(SwingConstants.TOP);
				label41.setFont(new Font("Century Gothic", Font.PLAIN, 14));
				label41.setForeground(new Color(204, 255, 0));
				label41.setBorder(null);
				label41.setHorizontalAlignment(SwingConstants.LEFT);
				label41.setHorizontalTextPosition(SwingConstants.LEFT);
				label41.setText("Version: " + Double.toString(properties.version()));
				label41.setBackground(Color.red);
				label51.setDisplayedMnemonic(KeyEvent.VK_UNDEFINED);
				label51.setVerticalTextPosition(SwingConstants.TOP);
				label51.setVerticalAlignment(SwingConstants.TOP);
				label51.setFont(new Font("Century Gothic", Font.PLAIN, 14));
				label51.setForeground(new Color(204, 255, 0));
				label51.setBorder(null);
				label51.setHorizontalAlignment(SwingConstants.LEFT);
				label51.setHorizontalTextPosition(SwingConstants.LEFT);
				label51.setText("By: White Bear");
				label51.setBackground(Color.red);
				textField9.setText("Location");
				textField9.setHorizontalAlignment(SwingConstants.CENTER);
				textField9.setEditable(false);
				textField9.setOpaque(false);
				textField9.setBorder(null);
				textField9.setFont(new Font("Century Gothic", Font.BOLD, 14));
				textField9.setFocusable(false);
				textField9.setBounds(new Rectangle(13, 112, 68, 19));
				textField10.setText("Check for Updates");
				textField10.setHorizontalAlignment(SwingConstants.CENTER);
				textField10.setEditable(false);
				textField10.setOpaque(false);
				textField10.setBorder(null);
				textField10.setFont(new Font("Century Gothic", Font.BOLD, 14));
				textField10.setFocusable(false);
				textField10.setSize(new Dimension(132, 19));
				textField10.setLocation(new Point(140, 210));
				textField9.setForeground(new Color(255, 255, 102));
				textField10.setForeground(new Color(255, 255, 102));
				panel3.add(textField9, null);
				panel3.add(textField10, null);
				panel3.add(getLocationCombo(), null);
				panel3.add(getRadioButton12(), null);

					panel1.setBackground(Color.black);
					panel1.setFont(new Font("Century Gothic", Font.PLAIN, 12));
					panel1.setFocusable(false);
					panel1.setLayout(null);

					label1.setText("White Bear Essence Miner");
					label1.setFont(new Font("Century Gothic", Font.BOLD, 24));
					label1.setHorizontalTextPosition(SwingConstants.CENTER);
					label1.setHorizontalAlignment(SwingConstants.CENTER);
					label1.setBorder(null);
					label1.setFocusable(false);
					panel1.add(label1);
					label1.setBounds(10, 1, 376, 54);

						tabbedPane1.setBackground(Color.black);
						tabbedPane1.setFont(new Font("Century Gothic",
								Font.PLAIN, 12));
						tabbedPane1.setBorder(null);
						tabbedPane1.setFocusable(false);
						tabbedPane1.addTab("Info", null, panel3, null);
							panel3.setBackground(Color.black);
							panel3.setFocusable(false);
							panel3.setBorder(null);
							panel3.setLayout(null);

								Dimension preferredSize = new Dimension();
								for (int i = 0; i < panel3.getComponentCount(); i++) {
									Rectangle bounds = panel3.getComponent(i)
									.getBounds();
									preferredSize.width = Math
									.max(bounds.x + bounds.width,
											preferredSize.width);
									preferredSize.height = Math.max(bounds.y
											+ bounds.height,
											preferredSize.height);
								}
								Insets insets = panel3.getInsets();
								preferredSize.width += insets.right;
								preferredSize.height += insets.bottom;
								panel3.setMinimumSize(preferredSize);
								panel3.setPreferredSize(preferredSize);
								panel3.add(label11, null);

							panel2.setBackground(Color.black);
							panel2.setBorder(null);
							panel2.setForeground(Color.white);
							panel2.setFont(new Font("Century Gothic",
									Font.PLAIN, 12));
							panel2.setFocusable(false);
							panel2.setLayout(null);

							clrSelected.setModel(new DefaultComboBoxModel(
									new String[] { "Black", "Blue", "Green",
											"Red", "Purple", "Brown" }));
							clrSelected.setMaximumRowCount(6);
							clrSelected.setBorder(null);
							clrSelected.setOpaque(false);
							clrSelected.setFocusable(false);
							panel2.add(clrSelected);
							clrSelected.setBounds(125, 12, 90, 25);

							textField3.setText("Paint Colour");
							textField3
							.setHorizontalAlignment(SwingConstants.CENTER);
							textField3.setEditable(false);
							textField3.setOpaque(false);
							textField3.setBorder(null);
							textField3.setFont(new Font("Century Gothic", Font.BOLD, 14));
							textField3.setFocusable(false);
							panel2.add(textField3);
							textField3.setBounds(15, 9, 100, 30);

							textField4.setText("Mouse Speed");
							textField4
							.setHorizontalAlignment(SwingConstants.CENTER);
							textField4.setEditable(false);
							textField4.setOpaque(false);
							textField4.setBorder(null);
							textField4.setFont(new Font("Century Gothic", Font.BOLD, 14));
							textField4.setFocusable(false);
							panel2.add(textField4);
							textField6.setText("Disable Antialias");
							textField6.setHorizontalAlignment(SwingConstants.CENTER);
							textField6.setEditable(false);
							textField6.setOpaque(false);
							textField6.setBorder(null);
							textField6.setFont(new Font("Century Gothic", Font.BOLD, 14));
							textField6.setFocusable(false);
							textField7.setText("Disable Map Paint");
							textField7.setHorizontalAlignment(SwingConstants.CENTER);
							textField7.setEditable(false);
							textField7.setOpaque(false);
							textField7.setBorder(null);
							textField7.setFont(new Font("Century Gothic", Font.BOLD, 14));
							textField7.setFocusable(false);
							textField8.setText("Disable Resting");
							textField8.setHorizontalAlignment(SwingConstants.CENTER);
							textField8.setEditable(false);
							textField8.setOpaque(false);
							textField8.setBorder(null);
							textField8.setFont(new Font("Century Gothic", Font.BOLD, 14));
							textField8.setFocusable(false);
							label5.setText("Fastest");
							panel2.add(label5);
							label5.setBounds(31, 172, 60, label5
									.getPreferredSize().height);

							label8.setText("(higher = slower)");
							panel2.add(label8);
							label8.setBounds(176, 54, 107, 16);

								Dimension preferredSize2 = new Dimension();
								for (int i = 0; i < panel2.getComponentCount(); i++) {
									Rectangle bounds = panel2.getComponent(i)
									.getBounds();
									preferredSize2.width = Math
									.max(bounds.x + bounds.width,
											preferredSize2.width);
									preferredSize2.height = Math.max(bounds.y
											+ bounds.height,
											preferredSize2.height);
								}
								Insets insets2 = panel2.getInsets();
								preferredSize2.width += insets2.right;
								preferredSize2.height += insets2.bottom;
								panel2.setMinimumSize(preferredSize2);
								panel2.setPreferredSize(preferredSize2);
								panel2.add(getJTextField(), null);
							panel4.setBackground(Color.black);
							panel4.setFocusable(false);
							panel4.setLayout(null);

							label2.setText("Custom Breaking");
							label2
							.setHorizontalTextPosition(SwingConstants.CENTER);
							label2
							.setHorizontalAlignment(SwingConstants.CENTER);
							label2.setFont(new Font("Century Gothic", Font.BOLD, 18));
							panel4.add(label2);
							label2.setBounds(0, 5, 350, 46);

							radioButton1.setText("Use Breaking");
							radioButton1.setBackground(Color.red);
							radioButton1.setOpaque(false);
							radioButton1.setEnabled(true);
							panel4.add(radioButton1);
							radioButton1.setBounds(12, 62, 125, 25);

							radioButton2.setText("Completely Random");
							radioButton2.setBackground(Color.red);
							radioButton2.setOpaque(false);
							radioButton2.setEnabled(true);
							panel4.add(radioButton2);
							radioButton2.setBounds(154, 63, 154, 25);

							label3.setText("Time till break:  Between");
							label3.setEnabled(true);
							panel4.add(label3);
							label4.setText("Length of Breaks:  Between");
							label4.setEnabled(true);
							panel4.add(label4);
							label42.setText("Auto Stop");
							label42.setEnabled(true);
							panel4.add(label42);
							label40.setText("(min)");
							label40.setEnabled(true);
							panel4.add(label40);
							label50.setText("(min)");
							label50.setEnabled(true);
							panel4.add(label50);
							formattedTextField1.setText("90");
							formattedTextField1.setEnabled(true);
							panel4.add(formattedTextField1);
							formattedTextField1.setBounds(171, 114, 45, 25);

							formattedTextField2.setText("10");
							formattedTextField2.setEnabled(true);
							panel4.add(formattedTextField2);
							formattedTextField2.setBounds(171, 154, 45, 25);

							formattedTextField3.setText("120");
							formattedTextField3.setEnabled(true);
							panel4.add(formattedTextField3);
							formattedTextField3.setBounds(246, 114, 45, 25);

							formattedTextField4.setText("20");
							formattedTextField4.setEnabled(true);
							panel4.add(formattedTextField4);
							formattedTextField4.setBounds(246, 154, 45, 25);

							textField1.setText("and");
							textField1.setOpaque(false);
							textField1.setEditable(false);
							textField1.setFocusable(false);
							textField1.setCaretColor(Color.red);
							textField1.setBorder(null);
							textField1.setEnabled(true);
							panel4.add(textField1);
							textField1.setBounds(219, 112, 30, 25);

							textField2.setText("and");
							textField2.setOpaque(false);
							textField2.setEditable(false);
							textField2.setFocusable(false);
							textField2.setCaretColor(Color.red);
							textField2.setBorder(null);
							textField2.setEnabled(true);
							panel4.add(textField2);
							textField2.setBounds(219, 152, 30, 25);

							textField97.setText(":");
							textField97.setOpaque(false);
							textField97.setEditable(false);
							textField97.setFocusable(false);
							textField97.setCaretColor(Color.red);
							textField97.setBorder(null);
							textField97.setEnabled(true);
							panel4.add(textField97);
							textField97.setBounds(155, 201, 13, 25);

							textField98.setText(":");
							textField98.setOpaque(false);
							textField98.setEditable(false);
							textField98.setFocusable(false);
							textField98.setCaretColor(Color.red);
							textField98.setBorder(null);
							textField98.setEnabled(true);
							panel4.add(textField98);
							textField98.setBounds(114, 201, 12, 25);

							textField99.setText("(hr:min:sec)");
							textField99.setOpaque(false);
							textField99.setEditable(false);
							textField99.setFocusable(false);
							textField99.setCaretColor(Color.red);
							textField99.setBorder(null);
							textField99.setEnabled(true);
							panel4.add(textField99);
							textField99.setBounds(197, 201, 74, 25);

								Dimension preferredSize3 = new Dimension();
								for (int i = 0; i < panel4.getComponentCount(); i++) {
									Rectangle bounds = panel4.getComponent(i)
									.getBounds();
									preferredSize3.width = Math
									.max(bounds.x + bounds.width,
											preferredSize3.width);
									preferredSize3.height = Math.max(bounds.y
											+ bounds.height,
											preferredSize3.height);
								}
								Insets insets3 = panel4.getInsets();
								preferredSize3.width += insets3.right;
								preferredSize3.height += insets3.bottom;
								panel4.setMinimumSize(preferredSize3);
								panel4.setPreferredSize(preferredSize3);
							label501 = new JLabel();
							label501.setLocation(new Point(158, 206));
							label501.setEnabled(true);
							label501.setFont(new Font("Century Gothic", Font.PLAIN, 12));
							label501.setForeground(Color.yellow);
							label501.setText("(min)");
							label501.setSize(new Dimension(39, 21));
							label401 = new JLabel();
							label401.setLocation(new Point(136, 168));
							label401.setEnabled(true);
							label401.setFont(new Font("Century Gothic", Font.PLAIN, 12));
							label401.setForeground(Color.yellow);
							label401.setText("(both fields are NOT case sensitive)");
							label401.setSize(new Dimension(213, 23));
							label43 = new JLabel();
							label43.setLocation(new Point(17, 143));
							label43.setEnabled(true);
							label43.setFont(new Font("Century Gothic", Font.BOLD, 12));
							label43.setForeground(Color.yellow);
							label43.setText("Message:");
							label43.setSize(new Dimension(68, 22));
							label32 = new JLabel();
							label32.setLocation(new Point(18, 108));
							label32.setEnabled(true);
							label32.setFont(new Font("Century Gothic", Font.BOLD, 12));
							label32.setForeground(Color.yellow);
							label32.setToolTipText("Min and Max time between each break");
							label32.setText("Character name:");
							label32.setSize(new Dimension(114, 22));
							label22 = new JLabel();
							label22.setBounds(new Rectangle(2, 3, 350, 24));
							label22.setForeground(Color.yellow);
							label22.setHorizontalAlignment(SwingConstants.CENTER);
							label22.setHorizontalTextPosition(SwingConstants.CENTER);
							label22.setText("Remote Control");
							label22.setFont(new Font("Century Gothic", Font.BOLD, 18));
							panel5 = new JPanel();
							panel5.setLayout(null);
							panel5.setBounds(new Rectangle(385, 146, 10, 10));
							panel5.setForeground(Color.white);
							panel5.setBackground(Color.black);
							panel5.add(label22, label22.getName());
							panel5.add(getRadioButton11(), getRadioButton11().getName());
							panel5.add(getRadioButton26(), getRadioButton26().getName());
							panel5.add(label32, label32.getName());
							panel5.add(label43, label43.getName());
							panel5.add(label401, label401.getName());
							panel5.add(label501, label501.getName());
							panel5.add(getFormattedTextField11(), getFormattedTextField11().getName());
							panel5.add(getFormattedTextField21(), getFormattedTextField21().getName());
							panel5.add(getFormattedTextField31(), getFormattedTextField31().getName());
							panel5.add(getJTextArea(), null);

								Dimension preferredSize4 = new Dimension();
								for (int i = 0; i < panel5.getComponentCount(); i++) {
									Rectangle bounds = panel5.getComponent(i)
									.getBounds();
									preferredSize4.width = Math
									.max(bounds.x + bounds.width,
											preferredSize4.width);
									preferredSize4.height = Math.max(bounds.y
											+ bounds.height,
											preferredSize4.height);
								}
								Insets insets4 = panel5.getInsets();
								preferredSize4.width += insets4.right;
								preferredSize4.height += insets4.bottom;
								panel5.setMinimumSize(preferredSize4);
								panel5.setPreferredSize(preferredSize4);
						tabbedPane1.addTab("Options", null, panel2, null);
						tabbedPane1.addTab("Breaking", panel4);
						tabbedPane1.addTab("Remote", panel5);

					panel1.add(tabbedPane1);
					tabbedPane1.setBounds(20, 55, 355, 270);
					//BUTTON1 on click
					button1.addActionListener(new java.awt.event.ActionListener() {
						public void actionPerformed(
								java.awt.event.ActionEvent arg0) {
							colour = (String) clrSelected.getSelectedItem();
							useBreaking = radioButton1.isSelected();
							randomBreaking = radioButton2.isSelected();
							doRest = !radioButton21.isSelected();
							antialias = !radioButton22.isSelected();
							mapPaint = !radioButton23.isSelected();
							checkUpdates = radioButton25.isSelected();
							useBrain = radioButton12.isSelected();
							int tempAdj = Integer.parseInt(jTextField.getText());
							if (tempAdj >= 40){
								tempAdj = 39;
							}else if (tempAdj <= 0){
								tempAdj = 0;
							}
							sAdj = tempAdj;
							String tempLoc = (String) locationCombo.getSelectedItem();
							if (tempLoc.contains("anille")){
								atVarrock = false;
							}else{
								atVarrock = true;
							}
							minTime = Integer.parseInt(formattedTextField1.getText());
							if (minTime <= 30){
								minTime = 31;
							}else if (minTime >= 600){
								minTime = 599;
							}
							maxTime = Integer.parseInt(formattedTextField3.getText());
							if (maxTime <= minTime + 9){
								maxTime = minTime + 10;
							}
							if (maxTime >= 1200){
								maxTime = 1199;
							}
							minLength = Integer.parseInt(formattedTextField2.getText());
							if (minLength <= 1){
								minLength = 2;
							}else if (minLength >= 30){
								minLength = 29;
							}
							maxLength = Integer.parseInt(formattedTextField4.getText());
							if (maxLength <= minLength + 2) {
								maxLength = minLength + 3;
							}
							if (maxLength >= 60){
								maxLength = 59;
							}
							long hour = Long.parseLong(textHour.getText());
							long minute = Long.parseLong(textMinute.getText());
							long second = Long.parseLong(textSecond.getText());
							if (hour <= 0 && minute <= 0 && second <= 0){
								stopTime = -1;
							}else{
								long tempTime = 0;
								if (hour > 1){
									long tempHr = tempTime;
									tempTime = tempHr + hour * 3600000;
								}
								if (minute > 1){
									long tempMin = tempTime;
									tempTime = tempMin + minute * 60000;
								}
								if (second > 1){
									long tempSec = tempTime;
									tempTime = tempSec + second * 1000;
								}
								stopTime = hour * 3600000 + minute * 60000 + second * 1000;
							}
							//Remote control
							useRemote = radioButton11.isSelected();
							doRelog = radioButton26.isSelected();
							try{
								remoteName = formattedTextField11.getText().toLowerCase();
							}catch (Exception e){ remoteName = "";}
							try{
								remoteMsg = formattedTextField21.getText().toLowerCase();
							}catch (Exception e){ remoteMsg = "";}
							try{
								relogAfter = Integer.parseInt(formattedTextField31.getText());
							}catch (Exception e){ relogAfter = -1;}
							//TODO this is a marker
							WBini.setProperty("UseSetting", String.valueOf(useSetting ? true : false));
							if (useSetting){
								WBini.setProperty("Location", String.valueOf(locationCombo
										.getSelectedIndex()));
								WBini.setProperty("UseBrain", String.valueOf(radioButton12
										.isSelected() ? true : false));
								WBini.setProperty("CheckUpdate", String.valueOf(radioButton25
										.isSelected() ? true : false));
								WBini.setProperty("PaintColour", String.valueOf(clrSelected
										.getSelectedIndex()));
								WBini.setProperty("MouseSpeed", jTextField.getText());
								WBini.setProperty("MapPaint", String.valueOf(radioButton23
										.isSelected() ? true : false));
								WBini.setProperty("Antialias", String.valueOf(radioButton22
										.isSelected() ? true : false));
								WBini.setProperty("Resting", String.valueOf(radioButton21
										.isSelected() ? true : false));
								WBini.setProperty("Breaking", String.valueOf(radioButton1
										.isSelected() ? true : false));
								WBini.setProperty("RandomBreak", String.valueOf(radioButton2
										.isSelected() ? true : false));
								WBini.setProperty("MinTime", formattedTextField1.getText());
								WBini.setProperty("MaxTime", formattedTextField3.getText());
								WBini.setProperty("MinLength", formattedTextField2.getText());
								WBini.setProperty("MaxLength", formattedTextField4.getText());
								WBini.setProperty("AutoStopH", textHour.getText());
								WBini.setProperty("AutoStopM", textMinute.getText());
								WBini.setProperty("AutoStopS", textSecond.getText());
								WBini.setProperty("Remote", String.valueOf(radioButton11
										.isSelected() ? true : false));
								WBini.setProperty("RemoteName", formattedTextField11.getText());
								WBini.setProperty("RemoteText", formattedTextField21.getText());
								WBini.setProperty("Relog", String.valueOf(radioButton26
										.isSelected() ? true : false));
								WBini.setProperty("RelogTime", formattedTextField31.getText());
							}
							try {
								WBini.store(new FileWriter(new File(GlobalConfiguration.Paths.getSettingsDirectory(),
										"WhiteBearEssenceMiner.ini")),
										"The GUI Settings for White Bear Essence Miner");
								if (first)
									log("[GUI] Created a settings file!");
							} catch (IOException e) {
								log.warning("[GUI] Error occurred when saving GUI settings!");
							}

							guiStart = true;
							WhiteBearGUI.dispose();
						}
					});
					button1.setText("Start Mining Essence!");
					button1.setFont(new Font("Century Gothic", Font.BOLD, 18));
					button1.setBorder(null);
					button1.setFocusable(false);
					panel1.add(button1);
					button1.setBounds(33, 330, 328, 51);
				label1.setBackground(Color.red);
				label1.setForeground(new Color(153, 255, 153));
				tabbedPane1.setForeground(new Color(0, 153, 0));
				textField3.setForeground(Color.yellow);
				textField4.setForeground(Color.yellow);
				label8.setForeground(Color.yellow);
				textField6.setForeground(Color.yellow);
				textField7.setForeground(Color.yellow);
				textField8.setForeground(Color.yellow);
				panel4.setForeground(Color.white);
				label2.setForeground(Color.yellow);
				radioButton1.setForeground(Color.yellow);
				radioButton2.setForeground(Color.yellow);
				radioButton2.addActionListener(new java.awt.event.ActionListener() {
					public void actionPerformed(java.awt.event.ActionEvent e) {
						randomBreaking = radioButton2.isSelected();
						if (randomBreaking == true){
							formattedTextField1.setEnabled(false);
							formattedTextField2.setEnabled(false);
							formattedTextField3.setEnabled(false);
							formattedTextField4.setEnabled(false);
						}else{
							formattedTextField1.setEnabled(true);
							formattedTextField2.setEnabled(true);
							formattedTextField3.setEnabled(true);
							formattedTextField4.setEnabled(true);
						}
					}
				});
				panel3.add(getRadioButton25(), null);
				label3.setForeground(Color.yellow);
				label4.setForeground(Color.yellow);
				label42.setForeground(Color.yellow);
				label40.setForeground(Color.yellow);
				label50.setForeground(Color.yellow);
				textField2.setForeground(Color.yellow);
				textField1.setForeground(Color.yellow);
				panel3.add(label21, null);
				label21.setSize(new Dimension(342, 22));
				label21.setLocation(new Point(12, 43));
				panel3.add(label31, null);
				label31.setSize(new Dimension(336, 19));
				label31.setLocation(new Point(12, 73));
				label3.setSize(new Dimension(145, 35));
				label4.setSize(new Dimension(158, 35));
				label42.setSize(new Dimension(65, 35));
				label40.setSize(new Dimension(42, 35));
				label50.setSize(new Dimension(46, 35));
				label3.setLocation(new Point(11, 109));
				label4.setLocation(new Point(11, 150));
				label42.setLocation(new Point(11, 195));
				label40.setLocation(new Point(293, 110));
				label50.setLocation(new Point(293, 150));
				label51.setBounds(new Rectangle(9, 216, 108, 19));
				panel3.add(label51, null);
				label41.setBounds(new Rectangle(9, 192, 95, 19));
				panel3.add(label41, null);
				panel3.add(getJTextArea1(), null);
				textField4.setSize(new Dimension(107, 30));
				textField4.setLocation(new Point(17, 47));
				panel2.add(textField8, null);
				panel2.add(textField7, null);
				textField7.setSize(new Dimension(131, 19));
				panel2.add(textField6, null);
				textField7.setLocation(new Point(21, 100));
				textField6.setSize(new Dimension(121, 19));
				textField6.setLocation(new Point(21, 137));
				textField8.setSize(new Dimension(115, 19));
				textField8.setLocation(new Point(20, 173));
				panel2.add(getRadioButton21(), null);
				panel2.add(getRadioButton22(), null);
				panel2.add(getRadioButton23(), null);
				WhiteBearGUI.setContentPane(panel1);
				WhiteBearGUI.setSize(new Dimension(407, 424));
				textField8.setToolTipText("Should I rest at Bank?");
				label3.setToolTipText("Min and Max time between each break");
				clrSelected.setFont(new Font("Century Gothic", Font.BOLD, 12));
				label8.setFont(new Font("Century Gothic", Font.BOLD, 12));
				radioButton1.setFont(new Font("Century Gothic", Font.BOLD, 12));
				radioButton2.setFont(new Font("Century Gothic", Font.BOLD, 12));
				label3.setFont(new Font("Century Gothic", Font.BOLD, 12));
				label4.setFont(new Font("Century Gothic", Font.BOLD, 12));
				formattedTextField1.setFont(new Font("Century Gothic", Font.PLAIN, 12));
				formattedTextField2.setFont(new Font("Century Gothic", Font.PLAIN, 12));
				formattedTextField4.setFont(new Font("Century Gothic", Font.PLAIN, 12));
				formattedTextField3.setFont(new Font("Century Gothic", Font.PLAIN, 12));
				textField1.setFont(new Font("Century Gothic", Font.PLAIN, 12));
				textField2.setFont(new Font("Century Gothic", Font.PLAIN, 12));
				label40.setFont(new Font("Century Gothic", Font.PLAIN, 12));
				label50.setFont(new Font("Century Gothic", Font.PLAIN, 12));
				label42.setFont(new Font("Century Gothic", Font.BOLD, 12));
				textField97.setFont(new Font("Century Gothic", Font.PLAIN, 12));
				textField97.setForeground(Color.yellow);
				textField98.setFont(new Font("Century Gothic", Font.PLAIN, 12));
				textField98.setForeground(Color.yellow);
				textField99.setFont(new Font("Century Gothic", Font.PLAIN, 12));
				textField99.setForeground(Color.yellow);
				panel4.add(getTextHour(), null);
				panel4.add(getTextMinute(), null);
				panel4.add(getTextSecond(), null);
				formattedTextField1.setBackground(Color.gray);
				formattedTextField3.setBackground(Color.gray);
				formattedTextField4.setBackground(Color.gray);
				formattedTextField2.setBackground(Color.gray);
				formattedTextField1.setForeground(new Color(255, 255, 204));
				formattedTextField3.setForeground(new Color(255, 255, 204));
				formattedTextField4.setForeground(new Color(255, 255, 204));
				formattedTextField2.setForeground(new Color(255, 255, 204));
				button1.setForeground(new Color(0, 102, 51));
				button1.setActionCommand("Start Mining Essence!");
				loadSettings();
			}
		}

		private JFrame WhiteBearGUI;
		private JPanel panel1;
		private JLabel label1;
		private JTabbedPane tabbedPane1;
		private JPanel panel2;
		private JComboBox clrSelected;
		private JTextField textField3;
		private JTextField textField4;
		private JTextField textField6;
		private JTextField textField7;
		private JTextField textField8;
		private JTextField textField9;
		private JTextField textField10;
		private JLabel label5;
		private JLabel label8;
		private JPanel panel4;
		private JLabel label2;
		private JRadioButton radioButton1;
		private JRadioButton radioButton2;
		private JLabel label3;
		private JLabel label4;
		private JLabel label42;
		private JLabel label40;
		private JLabel label50;
		private JFormattedTextField formattedTextField1;
		private JFormattedTextField formattedTextField2;
		private JFormattedTextField formattedTextField3;
		private JFormattedTextField formattedTextField4;
		private JTextField textField1;
		private JTextField textField2;
		private JTextField textField97;
		private JTextField textField98;
		private JTextField textField99;
		private JPanel panel3;
		private JButton button1;
		private JLabel label11 = null;
		private JLabel label21 = null;
		private JLabel label31 = null;
		private JLabel label41 = null;
		private JLabel label51 = null;
		private JTextField jTextField = null;
		private JRadioButton radioButton21 = null;
		private JRadioButton radioButton22 = null;
		private JRadioButton radioButton23 = null;
		private JComboBox locationCombo = null;
		private JRadioButton radioButton25 = null;
		private JFormattedTextField textHour = null;
		private JFormattedTextField textMinute = null;
		private JFormattedTextField textSecond = null;
		private JPanel panel5 = null;
		private JLabel label22 = null;
		private JRadioButton radioButton11 = null;
		private JRadioButton radioButton26 = null;
		private JLabel label32 = null;
		private JLabel label43 = null;
		private JLabel label401 = null;
		private JLabel label501 = null;
		private JFormattedTextField formattedTextField11 = null;
		private JFormattedTextField formattedTextField21 = null;
		private JFormattedTextField formattedTextField31 = null;
		private JTextArea jTextArea = null;
		private JRadioButton radioButton12 = null;
		private JTextArea jTextArea1 = null;

		private JTextField getJTextField() {
			if (jTextField == null) {
				jTextField = new JTextField();
				jTextField.setBounds(new Rectangle(126, 52, 43, 20));
				jTextField.setFont(new Font("Century Gothic", Font.PLAIN, 12));
				jTextField.setBackground(Color.gray);
				jTextField.setForeground(new Color(255, 255, 204));
				jTextField.setText("0");
			}
			return jTextField;
		}
		private JRadioButton getRadioButton21() {
			if (radioButton21 == null) {
				radioButton21 = new JRadioButton();
				radioButton21.setBounds(new Rectangle(158, 172, 26, 24));
				radioButton21.setEnabled(true);
				radioButton21.setForeground(Color.white);
				radioButton21.setOpaque(false);
				radioButton21.setText("");
				radioButton21.setSelected(true);
				radioButton21.setBackground(Color.red);
			}
			return radioButton21;
		}
		private JRadioButton getRadioButton22() {
			if (radioButton22 == null) {
				radioButton22 = new JRadioButton();
				radioButton22.setEnabled(true);
				radioButton22.setForeground(Color.white);
				radioButton22.setOpaque(false);
				radioButton22.setText("");
				radioButton22.setSize(new Dimension(21, 21));
				radioButton22.setLocation(new Point(158, 138));
				radioButton22.setBackground(Color.red);
			}
			return radioButton22;
		}
		private JRadioButton getRadioButton23() {
			if (radioButton23 == null) {
				radioButton23 = new JRadioButton();
				radioButton23.setEnabled(true);
				radioButton23.setForeground(Color.white);
				radioButton23.setOpaque(false);
				radioButton23.setText("");
				radioButton23.setSize(new Dimension(21, 21));
				radioButton23.setLocation(new Point(158, 100));
				radioButton23.setBackground(Color.red);
			}
			return radioButton23;
		}
		private JComboBox getLocationCombo() {
			if (locationCombo == null) {
				locationCombo = new JComboBox();
				locationCombo.setBounds(new Rectangle(92, 111, 95, 25));
				locationCombo.setOpaque(false);
				locationCombo.setMaximumRowCount(2);
				locationCombo.setModel(new DefaultComboBoxModel(new String[] {"Varrock", "Yanille"}));
				locationCombo.setFont(new Font("Century Gothic", Font.BOLD, 12));
				locationCombo.setBorder(null);
			}
			return locationCombo;
		}
		private JRadioButton getRadioButton25() {
			if (radioButton25 == null) {
				radioButton25 = new JRadioButton();
				radioButton25.setEnabled(true);
				radioButton25.setForeground(Color.yellow);
				radioButton25.setOpaque(false);
				radioButton25.setMnemonic(KeyEvent.VK_UNDEFINED);
				radioButton25.setText("Yes");
				radioButton25.setSize(new Dimension(46, 21));
				radioButton25.setLocation(new Point(283, 210));
				radioButton25.setBackground(Color.red);
			}
			return radioButton25;
		}
		private JFormattedTextField getTextHour() {
			if (textHour == null) {
				textHour = new JFormattedTextField();
				textHour.setFont(new Font("Century Gothic", Font.PLAIN, 12));
				textHour.setText("0");
				textHour.setPreferredSize(new Dimension(25, 25));
				textHour.setLocation(new Point(84, 201));
				textHour.setSize(new Dimension(25, 25));
				textHour.setBackground(Color.gray);
				textHour.setForeground(new Color(255, 255, 204));
				textHour.setEnabled(true);
			}
			return textHour;
		}
		private JFormattedTextField getTextMinute() {
			if (textMinute == null) {
				textMinute = new JFormattedTextField();
				textMinute.setFont(new Font("Century Gothic", Font.PLAIN, 12));
				textMinute.setText("0");
				textMinute.setPreferredSize(new Dimension(25, 25));
				textMinute.setLocation(new Point(124, 201));
				textMinute.setSize(new Dimension(25, 25));
				textMinute.setBackground(Color.gray);
				textMinute.setForeground(new Color(255, 255, 204));
				textMinute.setEnabled(true);
			}
			return textMinute;
		}

		private JFormattedTextField getTextSecond() {
			if (textSecond == null) {
				textSecond = new JFormattedTextField();
				textSecond.setFont(new Font("Century Gothic", Font.PLAIN, 12));
				textSecond.setText("0");
				textSecond.setPreferredSize(new Dimension(25, 25));
				textSecond.setLocation(new Point(164, 201));
				textSecond.setSize(new Dimension(25, 25));
				textSecond.setBackground(Color.gray);
				textSecond.setForeground(new Color(255, 255, 204));
				textSecond.setEnabled(true);
			}
			return textSecond;
		}

		private JRadioButton getRadioButton11() {
			if (radioButton11 == null) {
				radioButton11 = new JRadioButton();
				radioButton11.setBounds(new Rectangle(7, 80, 167, 25));
				radioButton11.setEnabled(true);
				radioButton11.setFont(new Font("Century Gothic", Font.BOLD, 12));
				radioButton11.setForeground(Color.yellow);
				radioButton11.setOpaque(false);
				radioButton11.setText("Enable Remote Control");
				radioButton11.setBackground(Color.red);
			}
			return radioButton11;
		}

		private JRadioButton getRadioButton26() {
			if (radioButton26 == null) {
				radioButton26 = new JRadioButton();
				radioButton26.setBounds(new Rectangle(8, 202, 96, 25));
				radioButton26.setEnabled(true);
				radioButton26.setFont(new Font("Century Gothic", Font.BOLD, 12));
				radioButton26.setForeground(Color.yellow);
				radioButton26.setOpaque(false);
				radioButton26.setText("Relog after:");
				radioButton26.setBackground(Color.red);
			}
			return radioButton26;
		}

		private JFormattedTextField getFormattedTextField11() {
			if (formattedTextField11 == null) {
				formattedTextField11 = new JFormattedTextField();
				formattedTextField11.setBounds(new Rectangle(133, 106, 122, 25));
				formattedTextField11.setEnabled(true);
				formattedTextField11.setFont(new Font("Century Gothic", Font.PLAIN, 12));
				formattedTextField11.setForeground(new Color(255, 255, 204));
				formattedTextField11.setText("Zezima");
				formattedTextField11.setBackground(Color.gray);
			}
			return formattedTextField11;
		}

		private JFormattedTextField getFormattedTextField21() {
			if (formattedTextField21 == null) {
				formattedTextField21 = new JFormattedTextField();
				formattedTextField21.setBounds(new Rectangle(88, 142, 167, 25));
				formattedTextField21.setEnabled(true);
				formattedTextField21.setFont(new Font("Century Gothic", Font.PLAIN, 12));
				formattedTextField21.setForeground(new Color(255, 255, 204));
				formattedTextField21.setText("I hate you");
				formattedTextField21.setBackground(Color.gray);
			}
			return formattedTextField21;
		}

		private JFormattedTextField getFormattedTextField31() {
			if (formattedTextField31 == null) {
				formattedTextField31 = new JFormattedTextField();
				formattedTextField31.setBounds(new Rectangle(108, 203, 45, 25));
				formattedTextField31.setEnabled(true);
				formattedTextField31.setFont(new Font("Century Gothic", Font.PLAIN, 12));
				formattedTextField31.setForeground(new Color(255, 255, 204));
				formattedTextField31.setText("10");
				formattedTextField31.setBackground(Color.gray);
			}
			return formattedTextField31;
		}

		private JTextArea getJTextArea() {
			if (jTextArea == null) {
				jTextArea = new JTextArea();
				jTextArea.setEditable(false);
				jTextArea.setBackground(Color.gray);
				jTextArea.setForeground(new Color(255, 255, 104));
				jTextArea.setFont(new Font("Century Gothic", Font.PLAIN, 12));
				jTextArea.setLocation(new Point(0, 29));
				jTextArea.setSize(new Dimension(353, 50));
				jTextArea.setPreferredSize(new Dimension(353, 50));
				jTextArea.setText("Allows you to remotely stop the script. When the specified \ncharacter says something to your botter IN CLAN CHAT, the \nscript will log you out, allowing you to login to your account.");
			}
			return jTextArea;
		}

		private JRadioButton getRadioButton12() {
			if (radioButton12 == null) {
				radioButton12 = new JRadioButton();
				radioButton12.setBounds(new Rectangle(15, 149, 104, 24));
				radioButton12.setEnabled(true);
				radioButton12.setFont(new Font("Century Gothic", Font.BOLD, 12));
				radioButton12.setForeground(Color.yellow);
				radioButton12.setOpaque(false);
				radioButton12.setText("Use The Brain");
				radioButton12.setSelected(true);
				radioButton12.setBackground(Color.red);
			}
			return radioButton12;
		}

		private JTextArea getJTextArea1() {
			if (jTextArea1 == null) {
				jTextArea1 = new JTextArea();
				jTextArea1.setBounds(new Rectangle(121, 149, 225, 50));
				jTextArea1.setFont(new Font("Century Gothic", Font.PLAIN, 12));
				jTextArea1.setForeground(new Color(255, 255, 104));
				jTextArea1.setPreferredSize(new Dimension(353, 50));
				jTextArea1.setText("Acts as an extra failsafe.\nUses a separate thread. You can \ndisable it if your bot is lagging. ");
				jTextArea1.setEditable(false);
				jTextArea1.setBackground(Color.gray);
			}
			return jTextArea1;
		}
	}

	public class FontChange{

		public boolean useSetting = true;

		public FontChange() {
			initComponents();
		}


		private void initComponents() {
			label11 = new JLabel();
			WhiteBearGUI = new JFrame();
			panel1 = new JPanel();
			textField9 = new JTextField();
			button1 = new JButton();
			WhiteBearGUI.setFont(new Font("Century Gothic", Font.PLAIN, 12));
			WhiteBearGUI.setFocusable(false);
			WhiteBearGUI.setTitle("White Bear Essence Miner");
			WhiteBearGUI.setVisible(true);
			WhiteBearGUI.setResizable(false);
			WhiteBearGUI.setAlwaysOnTop(true);

			{

				WhiteBearGUI.addWindowListener(new WindowAdapter() {
					@Override
					public void windowClosing(WindowEvent arg0) {
						WhiteBearGUI.dispose();
					}
				});

				label11.setDisplayedMnemonic(KeyEvent.VK_UNDEFINED);
				label11.setBounds(new Rectangle(15, 18, 294, 24));
				label11.setVerticalTextPosition(SwingConstants.TOP);
				label11.setVerticalAlignment(SwingConstants.TOP);
				label11.setFont(new Font("Century Gothic", Font.PLAIN, 14));
				label11.setForeground(new Color(204, 255, 0));
				label11.setBorder(null);
				label11.setHorizontalAlignment(SwingConstants.LEFT);
				label11.setHorizontalTextPosition(SwingConstants.LEFT);
				label11.setText("Type in the font you want the paint to use");
				label11.setBackground(Color.red);
				textField9.setText("Font");
				textField9.setBounds(new Rectangle(17, 61, 40, 19));
				textField9.setHorizontalAlignment(JTextField.LEFT);
				textField9.setEditable(false);
				textField9.setOpaque(false);
				textField9.setBorder(null);
				textField9.setFont(new Font("Century Gothic", Font.BOLD, 14));
				textField9.setFocusable(false);
				textField9.setForeground(new Color(255, 255, 102));
				panel1.setBackground(Color.black);
				panel1.setFont(new Font("Century Gothic", Font.PLAIN, 12));
				panel1.setFocusable(false);
				panel1.setLayout(null);
				button1.addActionListener(new java.awt.event.ActionListener() {
					public void actionPerformed(
							java.awt.event.ActionEvent arg0) {
						//TODO this is a marker
						font = jTextField.getText();
						if (useSetting){
							WBini.setProperty("Font", jTextField.getText());
							try {
								WBini.store(new FileWriter(new File(GlobalConfiguration.Paths.getSettingsDirectory(),
								"WhiteBearEssenceMiner.ini")),
								"The GUI Settings for White Bear Essence Miner");
							} catch (IOException e) {}
						}
						WhiteBearGUI.dispose();
					}
				});
				button1.setText("Change Font");
				button1.setFont(new Font("Century Gothic", Font.BOLD, 17));
				button1.setBorder(null);
				button1.setFocusable(false);
				panel1.add(button1);
				panel1.add(label11, null);
				panel1.add(textField9, null);
				panel1.add(getJTextField(), null);
				button1.setBounds(25, 102, 277, 39);
				WhiteBearGUI.setContentPane(panel1);
				WhiteBearGUI.setSize(new Dimension(327, 186));
				button1.setForeground(new Color(0, 102, 51));
				button1.setActionCommand("Change Font");
				if (WBini.getProperty("UseSetting") != null)
					useSetting = Boolean.parseBoolean(WBini.getProperty("UseSetting"));
				jTextField.setText(font);
			}
		}

		private JFrame WhiteBearGUI;
		private JPanel panel1;
		private JTextField textField9;
		private JButton button1;
		private JLabel label11 = null;
		private JTextField jTextField = null;

		private JTextField getJTextField() {
			if (jTextField == null) {
				jTextField = new JTextField();
				jTextField.setFont(new Font("Century Gothic", Font.PLAIN, 12));
				jTextField.setForeground(new Color(255, 255, 104));
				jTextField.setBackground(Color.gray);
				jTextField.setBounds(new Rectangle(70, 62, 131, 20));
				jTextField.setText("sansserif");
			}
			return jTextField;
		}
	}
	// START LOGINBOT  //
    private static final int INTERFACE_MAIN = 905;
	private static final int INTERFACE_MAIN_CHILD = 59;
	private static final int INTERFACE_MAIN_CHILD_COMPONENT_ID = 4;
	private static final int INTERFACE_LOGIN_SCREEN = 596;
	private static final int INTERFACE_USERNAME = 59;
	private static final int INTERFACE_USERNAME_WINDOW = 55;
	private static final int INTERFACE_PASSWORD = 73;
	private static final int INTERFACE_PASSWORD_WINDOW = 62;
	private static final int INTERFACE_BUTTON_LOGIN = 63;
	private static final int INTERFACE_TEXT_RETURN = 15;
	private static final int INTERFACE_WELCOME_SCREEN = 906;
	private static final int INTERFACE_WELCOME_SCREEN_BUTTON_PLAY_1 = 178;
	private static final int INTERFACE_WELCOME_SCREEN_BUTTON_PLAY_2 = 180;
	private static final int INTERFACE_WELCOME_SCREEN_TEXT_RETURN = 36;

	private static final int INDEX_LOGGED_OUT = 3;
	private static final int INDEX_LOBBY = 7;

	private int worldFullCount;

	public boolean activateCondition() {
		int idx = game.getClientState();
		return (idx == INDEX_LOGGED_OUT || idx == INDEX_LOBBY) && account.getName() != null;
	}

	public int loginLoop() {
		String username = account.getName().replaceAll("_", " ").toLowerCase().trim();
		String returnText = interfaces.get(INTERFACE_LOGIN_SCREEN).
				getComponent(INTERFACE_TEXT_RETURN).getText().toLowerCase();
		int textlength;
		if (game.getClientState() != INDEX_LOGGED_OUT) {
			if (!game.isWelcomeScreen()) {
				sleep(random(1000, 2000));
			}
			if (game.getClientState() == INDEX_LOBBY) {
				RSInterface welcome_screen = interfaces.get(INTERFACE_WELCOME_SCREEN);
				RSComponent welcome_screen_button_play_1 = welcome_screen.getComponent(INTERFACE_WELCOME_SCREEN_BUTTON_PLAY_1);
				RSComponent welcome_screen_button_play_2 = welcome_screen.getComponent(INTERFACE_WELCOME_SCREEN_BUTTON_PLAY_2);

				mouse.click(
						welcome_screen_button_play_1.getAbsoluteX(),
						welcome_screen_button_play_1.getAbsoluteY(),
						welcome_screen_button_play_2.getAbsoluteX() + welcome_screen_button_play_2.getWidth() - welcome_screen_button_play_1.getAbsoluteX(),
						welcome_screen_button_play_1.getHeight(),
						true
				);

				for (int i = 0; i < 4 && game.getClientState() == 6; i++) {
					sleep(500);
				}
				returnText = interfaces.get(INTERFACE_WELCOME_SCREEN).
						getComponent(INTERFACE_WELCOME_SCREEN_TEXT_RETURN).getText().toLowerCase();

				if (returnText.contains("members")) {
					log("Unable to login to a members world. Stopping script.");
					RSComponent back_button1 = interfaces.get(INTERFACE_WELCOME_SCREEN).getComponent(38);
					RSComponent back_button2 = interfaces.get(INTERFACE_WELCOME_SCREEN).getComponent(41);
					mouse.click(back_button1.getAbsoluteX(),back_button1.getAbsoluteY(),
							back_button2.getAbsoluteX() + back_button2.getWidth() - back_button1.getAbsoluteX(),
							back_button1.getHeight(),true);
					interfaces.get(INTERFACE_WELCOME_SCREEN).getComponent(203).doClick();
					stopScript(false);
				}
			}
			return -1;
		}
		if (!game.isLoggedIn()) {
			if (returnText.contains("update")) {
				log("Runescape has been updated, please reload RSBot.");
				stopScript(false);
			}
			if (returnText.contains("disable")) {
				log("Your account is banned/disabled.");
				stopScript(false);
			}
			if (returnText.contains("incorrect")) {
				log("Failed to login five times in a row. Stopping script.");
				stopScript(false);
			}
			if (returnText.contains("invalid")) {
				if (invalidCount > 6) {
					log("Unable to login after 6 attempts. Stopping script.");
					log("Please verify that your RSBot account profile is correct.");
					stopScript(false);
				}
				interfaces.get(INTERFACE_LOGIN_SCREEN).getComponent(25).doClick();
				invalidCount++;
				return (random(500, 2000));
			}
			if (returnText.contains("full")) {
				if (worldFullCount > 30) {
					log("World is Full. Waiting for 15 seconds.");
					sleep(random(10000, 15000));
					worldFullCount = 0;
				}
				sleep(random(1000, 1200));
				worldFullCount++;
			}
			if (returnText.contains("world")) {
				return random(1000, 1200);
			}
			if (returnText.contains("performing login")) {
				return random(1000, 1200);
			}
		}
		if (game.getClientState() == INDEX_LOGGED_OUT) {
			if (!atLoginScreen()) {
				interfaces.getComponent(INTERFACE_MAIN, INTERFACE_MAIN_CHILD).getComponent(INTERFACE_MAIN_CHILD_COMPONENT_ID).doAction("");
				return random(500, 600);
			}
			if (isUsernameFilled() && isPasswordFilled()) {
				interfaces.get(INTERFACE_LOGIN_SCREEN).getComponent(INTERFACE_BUTTON_LOGIN).doClick();
				return random(500, 600);
			}

			if (!isUsernameFilled() && interfaces.get(INTERFACE_LOGIN_SCREEN).getComponent(INTERFACE_USERNAME).getText().length() >= 1){
				atLoginInterface(interfaces.get(INTERFACE_LOGIN_SCREEN).getComponent(INTERFACE_USERNAME_WINDOW));
				sleep(random(400,600));
				textlength = interfaces.get(INTERFACE_LOGIN_SCREEN).getComponent(INTERFACE_USERNAME).getText().length() + random(3, 5);
				for (int i = 0; i <= textlength + random(1, 5); i++) {
					keyboard.sendText("\b", false);
					if (random(0, 2) == 1) {
						sleep(random(25, 100));
					}
				}
				keyboard.sendText(username, false);
				sleep(random(100,200));
			}
			if (!isPasswordFilled() && interfaces.get(INTERFACE_LOGIN_SCREEN).getComponent(INTERFACE_PASSWORD).getText().length() >= 1){
				atLoginInterface(interfaces.get(INTERFACE_LOGIN_SCREEN).getComponent(INTERFACE_PASSWORD_WINDOW));
				sleep(random(400, 600));
				textlength = interfaces.get(INTERFACE_LOGIN_SCREEN).getComponent(INTERFACE_PASSWORD).getText().length() + random(3, 5);
				for (int i = 0; i <= textlength + random(1, 5); i++) {
					keyboard.sendText("\b", false);
					if (random(0, 2) == 1) {
						sleep(random(25, 100));
					}
				}
				sleep(random(100,200));
			}

			if (!isUsernameFilled()){
				atLoginInterface(interfaces.get(INTERFACE_LOGIN_SCREEN).getComponent(INTERFACE_USERNAME_WINDOW));
				sleep(random(400,600));
				keyboard.sendText(username, true);
				sleep(random(100,200));
				keyboard.sendText(account.getPassword(), true);
				return random(1000,1200);
			}
			if (!isPasswordFilled()) {
				atLoginInterface(interfaces.get(INTERFACE_LOGIN_SCREEN).getComponent(INTERFACE_PASSWORD_WINDOW));
				sleep(random(400,600));
				keyboard.sendText(account.getPassword(), true);
				return random(1000,1200);
			}
			return random(500,600);
		}
		return random(500, 2000);
	}

	private boolean atLoginInterface(RSComponent i) {
		if (!i.isValid())
			return false;
		Rectangle pos = i.getArea();
		if (pos.x == -1 || pos.y == -1 || pos.width == -1 || pos.height == -1)
			return false;
		int dy = (int) (pos.getHeight() - 4) / 2;
		int maxRandomX = (int) (pos.getMaxX() - pos.getCenterX());
		int midx = (int) (pos.getCenterX());
		int midy = (int) (pos.getMinY() + pos.getHeight() / 2);
		if (i.getIndex() == INTERFACE_PASSWORD_WINDOW) {
			mouse.click(minX(i), midy + random(-dy, dy), true);
		} else {
			mouse.click(midx + random(1, maxRandomX), midy + random(-dy, dy), true);
		}
		return true;
	}

	private int minX(RSComponent a) {
		int x = 0;
		Rectangle pos = a.getArea();
		int dx = (int) (pos.getWidth() - 4) / 2;
		int midx = (int) (pos.getMinX() + pos.getWidth() / 2);
		if (pos.x == -1 || pos.y == -1 || pos.width == -1 || pos.height == -1)
			return 0;
		for (int i = 0; i < interfaces.get(INTERFACE_LOGIN_SCREEN).getComponent(INTERFACE_PASSWORD).getText().length(); i++) {
			x += 11;
		}
		if (x > 44) {
			return (int) (pos.getMinX() + x + 15);
		} else {
			return midx + random(-dx, dx);
		}
	}

	private boolean atLoginScreen() {
		return interfaces.get(INTERFACE_LOGIN_SCREEN).isValid();
	}

	private boolean isUsernameFilled() {
		String username = account.getName().replaceAll("_", " ").toLowerCase().trim();
		return interfaces.get(INTERFACE_LOGIN_SCREEN).getComponent(INTERFACE_USERNAME).getText().equalsIgnoreCase(username);
	}

	private boolean isPasswordFilled() {
		return interfaces.get(INTERFACE_LOGIN_SCREEN).getComponent(INTERFACE_PASSWORD).getText().length() == account.getPassword().length();
	}
	// END   LOGINBOT  //

	public void mouseClicked(MouseEvent arg0) {}
	public void mouseEntered(MouseEvent arg0) {}
	public void mouseExited(MouseEvent arg0) {}
	public void mouseReleased(MouseEvent arg0) {}

	public void mouseDragged(MouseEvent arg0) {
		Point kk = arg0.getPoint();
		int totalWidth = 490, totalHeight = 114;
		int mouseX = kk.x;
		int mouseY = kk.y;
		//if (Listener.blocked) {
		if (mouseX >= paintX && mouseX <= (paintX + totalWidth) && mouseY >= paintY
				&& mouseY <= (paintY + totalHeight))
			if (thePainter.currentTab != -1 && showSettings == false) {
				paintX = mouseX - (totalWidth / 2);
				paintY = mouseY - (totalHeight / 2);
			}
		//}
		if (paintX < 4)
			paintX = 4;
		if (paintY < 4)
			paintY = 4;
		if ((paintX + totalWidth) > 761)
			paintX = 761 - totalWidth;
		if ((paintY + totalHeight) > 494)
			paintY = 494 - totalHeight;
	}

	public void mousePressed(MouseEvent e) {
		Point kk = e.getPoint();
		int totalWidth = 490, totalHeight = 114;
		int mouseX = kk.x;
		int mouseY = kk.y;
		//if (Listener.blocked) {
		if (mouseX >= paintX && mouseX <= (paintX + totalWidth) && mouseY >= paintY
				&& mouseY <= (paintY + totalHeight))
			if (thePainter.currentTab != -1 && showSettings == false) {
				thePainter.movePaintNote = 0;
				paintX = mouseX - (totalWidth / 2);
				paintY = mouseY - (totalHeight / 2);
			}
		//}
		if (paintX < 4)
			paintX = 4;
		if (paintY < 4)
			paintY = 4;
		if ((paintX + totalWidth) > 761)
			paintX = 761 - totalWidth;
		if ((paintY + totalHeight) > 494)
			paintY = 494 - totalHeight;
		/////////////////////////////
		if (thePainter.logOutYes.contains(kk) && exitStage == 1) {
			exitStage = 2;
			if (logOutInfo == false) {
				log("You will be logged out when the current loop ends (i.e. in a while)");
				logOutInfo = true;
			}
		}
		if (thePainter.logOutNo.contains(kk) && exitStage == 1) {
			exitStage = 0;
		}
		if (thePainter.logOut.contains(kk) && exitStage == 0 && showSettings) {
			showSettings = false;
			exitStage = 1;
		}
		final Rectangle nameBlock = new Rectangle(interfaces.get(137).getComponent(54).getAbsoluteX(), interfaces.get(137).getComponent(54).getAbsoluteY()+2, 87, 16);
		final Rectangle screenshot = new Rectangle(nameBlock.x + nameBlock.width + 5, nameBlock.y, nameBlock.width - 10, nameBlock.height);
		if (exitStage != 2 && counter == 0 && screenshot.contains(kk)){
			counter = 401;
		}
		if (thePainter.changeFont.contains(kk) && !fontGui.WhiteBearGUI.isVisible() && showSettings) {
			try{
				fontGui.jTextField.setText(font);
			}catch (Exception x){}
			fontGui.WhiteBearGUI.setVisible(true);
		}
	}

	public void mouseMoved(MouseEvent e) {
		p = e.getPoint();
	}
}