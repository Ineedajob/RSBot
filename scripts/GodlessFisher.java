
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.font.LineMetrics;
import java.awt.geom.GeneralPath;
import java.awt.geom.Rectangle2D;
import java.awt.image.RenderedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.imageio.ImageIO;
import org.rsbot.event.listeners.PaintListener;
import org.rsbot.script.*;
import org.rsbot.script.methods.Game;
import org.rsbot.script.methods.Skills;
import org.rsbot.script.wrappers.*;
import org.rsbot.script.util.WindowUtil;
import org.rsbot.util.GlobalConfiguration;


@ScriptManifest(authors={"Enfilade"}, keywords={"fishing","enfilade","godless"},
name="Godless Fisher", description="A fisher with multiple locations", version=1.61)
public class GodlessFisher extends Script implements PaintListener,
    MouseMotionListener, MouseListener, KeyListener {

    private final int LEVEL_UP_ID = 740, LEVEL_UP_CHILD_ID = 3;
    private final int FISHING_SKILL_ID = 320, FISHING_SKILL_CHILD_ID = 37;
    private final int RUN_ID = 750, RUN_CHILD_ID = 1;

    private State state, next, afterAntiban;

    private FishingArea area;
    private SpotInfo spots;
    private FishingStyle style;
    private InventoryHandler handler;
    private Gear[] gear;

    private ArrayList<RSTile> path;
    private RSNPC fishingSpot;
    private RSTile fishingSpotTile;
    private int inventoryRow, inventoryColumn;
    private RSItem[] oldInventory;
    
    private Timer timer = null, inventoryTimer = null;
    private boolean drop, clickedStiles, mousekeys;
    private GFFrame gui;
    private long failsafe;

    private double focus = 1.0;
    
    private int energyThreshold;
    private java.util.Random unique, r = new java.util.Random();

    private final int MINUTE = 60000;
    private int antiban, antibanState, lastAntiban;
    private String antibanDesc;
    private final LinkedList<MousePathPoint> mousePath = new LinkedList<MousePathPoint>();
    private final LinkedList<Click> clicks = new LinkedList<Click>();
    private final AntibanTimer[] antibans = {
        new AntibanGTimer("Change focus", "Changing focus", 1000, 10000, 3000, 7000),
        new AntibanTimer("Take small breaks", "Taking a short break", MINUTE*15, MINUTE*30),
        new AntibanTimer("Look offscreen", "Looking away", 20000, MINUTE),
        new AntibanTimer("Check fishing XP", "Checking fishing XP", 10*MINUTE, 30*MINUTE),
        new AntibanTimer("Click skills tab", "Opening skills tab", 5*MINUTE, 45*MINUTE),
        new AntibanTimer("Move mouse", "Moving mouse", 5000, MINUTE*2),
        new AntibanTimer("Move cursor offscreen", "Moving cursor offscreen", 1000, MINUTE*5),
        new AntibanTimer("Move camera", "Moving camera", 300, MINUTE*3),
        new AntibanTimer("Wiggle mouse", "Wiggling mouse", 1000, MINUTE),
        new AntibanTimer("Put hand on mouse", "Putting hand on mouse", 30000, MINUTE*3)
    };
    private final int WALKING_AB_START = 7;

    private final int
            CHANGE_FOCUS = 0,
            TAKING_A_BREAK = 1,
            LOOKING_AWAY = 2,
            CHECKING_FISHING_SKILL = 3,
            LOOKING_AT_SKILLS_TAB = 4,
            MOVE_MOUSE = 5,
            MOVE_CURSOR_OFF_SCREEN = 6,
            MOVING_CAMERA = 7,
            WIGGLE_MOUSE = 8,
            PUT_HAND_ON_MOUSE = 9;

    private int startingXP, startingLevel;
    private long startTime;
    public boolean onStart() {
        if(!game.isLoggedIn()) {
            log("Please log in before starting the script!");
            return false;
        }
        File f = new File(GlobalConfiguration.Paths.getScriptsDirectory() + "/gfimg");
        if(!f.exists())
            f.mkdir();
        loadImages();
        gui = new GFFrame();
        WindowUtil.position(gui);
        gui.setVisible(true);
        antiban = -1;
        lastAntiban = -1;
        log("Script will start when you hit start on the GUI.");
        log("Visit http://www.powerbot.org/vb/showthread.php?t=471556 to check for updates.");
        return true;
    }

    private final PaintText WATERMARK = new PaintText() {
                public String getText() {
                    return "Godless Fisher " + GodlessFisher.class.getAnnotation(ScriptManifest.class).version();
                }
            },
            STATE = new PaintText() {
                public String getText() {
                    String s = null;
                    if(state == State.ANTIBAN)
                        s = afterAntiban.toString() + " (AB)";
                    else if(state == State.MOVING)
                        s = next.toString() + " (Moving)";
                    else
                        s = state.toString();

                    if(antibanDesc == null)
                        return s;
                    return s + "\nAntiban: " + antibanDesc;
                }
            },
            TIME_RUNNING = new PaintText() {
                public String getText() {
                    long time = (System.currentTimeMillis() - startTime)/1000;
                    return "Runtime: " + time / 3600 + ":"
					+ (time / 60 % 60 < 10 ? "0" : "") + time / 60 % 60 + ":"
					+ (time % 60 < 10 ? "0" : "") + time % 60;
                }
            },
            CURRENT_LEVEL = new PaintText() {
                public String getText() {
                    return "Fishing Level: " + skills.getCurrentLevel(Skills.FISHING);
                }
            },
            CURRENT_XP = new PaintText() {
                public String getText() {
                    return "Fishing XP: " + skills.getCurrentExp(Skills.FISHING);
                }
            },
            LEVELS_GAINED = new PaintText() {
                public String getText() {
                    return "Levels Gained: " + (skills.getCurrentLevel(Skills.FISHING) - startingLevel);
                }
            },
            XP_GAINED = new PaintText() {
                public String getText() {
                    return "XP Gained: " + (skills.getCurrentExp(Skills.FISHING) - startingXP);
                }
            },
            XP_UNTIL_NEXT = new PaintText() {
                public String getText() {
                    return "XP 'til next level: " + skills.getExpToNextLevel(Skills.FISHING);
                }
            },
            FISH_CAUGHT = new PaintText() {
                public String getText() {
                    Fish[] fish = style.getFish();
                    String s = "";
                    for(int i = 0; i < fish.length; i++) {
                        if(i > 0)
                            s += "\n";
                        s += fish[i].getPaintText();
                        if(i < fish.length - 1)
                            s += "\n";
                    }
                    return s;
                }
            }, XP_ESTIMATES = new PaintText() {
                public String getText() {
                    double xpPerHr = (int)((skills.getCurrentExp(Skills.FISHING) - startingXP)/
                            (double)(System.currentTimeMillis() - startTime)
                            * 3600000);
                    long time = (long)(skills.getExpToNextLevel(Skills.FISHING)/xpPerHr*3600);
                    return "XP per hour: " + (int)xpPerHr +
                            "\nTime 'til next level: " +
                            time / 3600 + ":"
                            + (time / 60 % 60 < 10 ? "0" : "") + time / 60 % 60 + ":"
                            + (time % 60 < 10 ? "0" : "") + time % 60;
                }
            };

    public void loadImages() {
        log("Loading images...");
        icons = new PaintIcon[] {
            new PaintIcon(getImage("fish.png"),
                    new Rectangle(492, 315, 23, 24),
                    FISH_CAUGHT),
            new PaintIcon(getImage("xp.png"),
                    new Rectangle(492-23, 315, 23, 24),
                    CURRENT_LEVEL,
                    CURRENT_XP,
                    LEVELS_GAINED,
                    XP_GAINED,
                    XP_UNTIL_NEXT,
                    XP_ESTIMATES),
            new PaintIcon(getImage("info.png"),
                    new Rectangle(492-46, 315, 23, 24),
                    WATERMARK,
                    TIME_RUNNING,
                    STATE)
        };
        log("Images loaded successfully!");
    }

    public int loop() {
        if(game.isWelcomeScreen() || !game.isLoggedIn() || !gui.hitStart)
            return 1000;
        if(antiban >= 0) {
            antibanDesc = antibans[antiban].getPaintDescription();
            lastAntiban = antiban;
        } else {
            antibanDesc = null;
            lastAntiban = -1;
        }
        checkForNewFish();
        /* FAIL-SAFE TEMPORARILY REMOVED
         if(failsafe < System.currentTimeMillis()) {
            log("10 minutes have passed without catching a fish..");
            log("Logging out to keep your account safe.");
            game.logout(true);
            return -1;
        }*/
        RSPlayer player = getMyPlayer();
        RSCharacter interacting = player.getInteracting();
        switch(state) {
            case LOOKING:
                if(game.getCurrentTab() == Game.TAB_INVENTORY &&
                        !style.playerHasBait()) {
                    log("You do not have the required gear!");
                    return -1;
                }
                if(bank.isOpen())
                    bank.close();
                if(inventory.isFull()) {
                    state = State.HANDLING_INVENTORY;
                    drop = inventoryContainsUnwantedFish(style);
                    inventoryColumn = 0;
                    inventoryRow = 0;
                    fishingSpot = null;
                } else if(fishingSpot != null && timer != null && getMyPlayer().isMoving()) {
                    timer.reset();
                    startAntiban();
                } else if(style.playerIsFishing(player) && interacting instanceof RSNPC
                        && ((RSNPC)interacting).getID() == spots.getID()
                        /*&& fishingSpot != null*/) {
                    state = State.FISHING;
                    fishingSpot = (RSNPC)interacting;
                    fishingSpotTile = fishingSpot.getLocation();
                } else if(fishingSpot == null) {
                    fishingSpot = spots.getSpot(area, style);
                    if(fishingSpot == null) {
                        startWalking(area.getReversedGuides(), State.LOOKING);
                        return random(50, 250);
                    }
                    if(fishingSpot.isOnScreen()) {
                        if(style.clickFishingSpot(fishingSpot)) {
                            fishingSpotTile = fishingSpot.getLocation();
                            timer = new Timer(random(2000, 3000));
                        } else {
                            fishingSpot = null;
                            return random(100, 300);
                        }
                    } else {
                        walking.walkTileMM(fishingSpot.getLocation(), 2, 2);
                        startMoving(State.LOOKING, true);
                        fishingSpot = null;
                        return random(1000, 3000);
                    }
                } else if(timer.isUp())
                    fishingSpot = null;
                break;
            case FISHING:
                if(focus > 0.1 && inventory.isFull()) {
                    state = State.HANDLING_INVENTORY;
                    drop = inventoryContainsUnwantedFish(style);
                    inventoryColumn = 0;
                    inventoryRow = 0;
                    fishingSpot = null;
                    style.resetExpecting();
                    focus = 1;
                } else if((focus > 0.7 && !style.playerIsFishing(player)) ||
                        (focus > 0.9 && spotMoved(true))) {
                    state = State.LOOKING;
                    fishingSpot = null;
                    style.resetExpecting();
                    focus = 1;
                    return random(1000, 4000);
                } else if(focus > 0.3 && interfaces.get(LEVEL_UP_ID).isValid()) {
                    sleep(random(500, 1500));
                    interfaces.get(LEVEL_UP_ID).getComponent(LEVEL_UP_CHILD_ID).doClick();
                } else
                    startAntiban();
                break;
            case HANDLING_INVENTORY:
                switch(drop ? InventoryHandler.DROPPER : handler.getType()) {
                    case InventoryHandler.DROPPER:
                        RSItem fish;
                        if(inventoryContainsUnwantedFish(style)) {
                            if(mousekeys) {
                                fish = null;
                                if(inventoryRow == 0) {
                                    outer:
                                    for(; inventoryColumn < 4; inventoryColumn++)
                                        for(; inventoryRow < 6; inventoryRow++)
                                            if((fish = inventory.getItemAt(inventoryColumn+inventoryRow*4)) != null
                                                && style.containsUnwantedFishID(fish.getID()))
                                                break outer;
                                    if(fish == null)
                                        return random(100, 150);
                                } else
                                    fish = inventory.getItemAt(inventoryColumn+inventoryRow*4);
                                Point p = mouse.getLocation();
                                if(fish == null || !style.containsUnwantedFishID(fish.getID()))
                                    mouse.hop(p.x, p.y+37);
                                else {
                                    RSComponent c = fish.getComponent();
                                    if(!c.getArea().contains(mouse.getLocation())) {
                                        mouse.move(c.getAbsoluteX() + random(0, c.getWidth(), c.getWidth()/2, c.getWidth()/3),
                                                random(c.getAbsoluteY(), c.getCenter().y));
                                        p = mouse.getLocation();
                                    }
                                    mouse.click(false);
                                    sleep(random(100, 200));
                                    mouse.hop(p.x, p.y+37);
                                    sleep(random(100, 200));
                                    mouse.click(true);
                                }
                                inventoryRow++;
                                if(inventoryRow >= 6) {
                                    inventoryColumn++;
                                    inventoryRow = 0;
                                    if(inventoryColumn >= 4) {
                                        inventoryColumn = 0;
                                        state = State.LOOKING;
                                    }
                                }
                            } else if((fish = getNextUnwantedFish()) != null) {
                                if(!fish.getComponent().getArea().contains(mouse.getLocation()))
                                    fish.getComponent().doHover();
                                mouse.click(false);
                                Point p = mouse.getLocation();
                                mouse.click(gaussianRandom(p.x, 3),
                                        Math.min(game.getHeight()-85, p.y) + random(39, 53, 46, 8), true);
                                inventoryRow++;
                                if(inventoryRow >= 7) {
                                    inventoryRow = 0;
                                    inventoryColumn++;
                                    if(inventoryColumn >= 4) {
                                        inventoryColumn = 0;
                                        state = State.LOOKING;
                                    }
                                }
                            }
                        } else
                            state = State.LOOKING;
                        break;
                    case InventoryHandler.BOOTH:
                        if(inventoryContainsNoFish(style)) {
                            if(spots.getOnScreenInArea(area, style) != null)
                                state = State.LOOKING;
                            else
                                startWalking(area.getReversedGuides(), State.LOOKING);
                        } else {
                            if(bank.isOpen()) {
                                boolean gearInInventory = false;
                                outer:
                                for(RSItem i : inventory.getCachedItems())
                                    for(int id : style.getGearIDs())
                                        if(id == i.getID()) {
                                            gearInInventory = true;
                                            break outer;
                                        }
                                if(gearInInventory)
                                    bank.depositAllExcept(style.getGearIDs());
                                else bank.depositAll();
                                return random(100, 1000);
                            } else {
                                RSObject booth = objects.getNearest(handler.getID());
                                if(booth != null && (booth.isOnScreen() &&
                                        booth.getModel() != null)) {
                                    if(booth.doAction("use-quickly")) {
                                        startMoving(State.HANDLING_INVENTORY, false);
                                        getFirstInInventory(style).getComponent().doHover();
                                        return random(1000, 2000);
                                    }
                                } else
                                    startWalking(area.getGuides(), State.HANDLING_INVENTORY);
                            }
                        }
                        break;
                    case InventoryHandler.BANKER:
                        if(inventoryContainsNoFish(style)) {
                            if(spots.getOnScreenInArea(area, style) != null)
                                state = State.LOOKING;
                            else
                                startWalking(area.getReversedGuides(), State.LOOKING);
                        } else {
                            if(bank.isOpen()) {
                                boolean gearInInventory = false;
                                outer:
                                for(RSItem i : inventory.getCachedItems())
                                    for(int id : style.getGearIDs())
                                        if(id == i.getID()) {
                                            gearInInventory = true;
                                            break outer;
                                        }
                                if(gearInInventory)
                                    bank.depositAllExcept(style.getGearIDs());
                                else bank.depositAll();
                                return random(100, 1000);
                            } else {
                                RSNPC banker = npcs.getNearest(handler.getID());
                                if(banker != null && (banker.isOnScreen() &&
                                        banker.getModel() != null)) {
                                    if(banker.doAction("bank")) {
                                        startMoving(State.HANDLING_INVENTORY, false);
                                        getFirstInInventory(style).getComponent().doHover();
                                        return random(1000, 2000);
                                    }
                                } else
                                    startWalking(area.getGuides(), State.HANDLING_INVENTORY);
                            }
                        }
                        break;
                    case InventoryHandler.NOTER:
                        if(!inventory.isFull())
                            state = State.LOOKING;
                        else if(!inventoryContainsNoFish(style)) {
                            RSNPC noter = npcs.getNearest(handler.getID());
                            if(noter != null && noter.isOnScreen()) {
                                if(inventoryTimer != null && !inventoryTimer.isUp())
                                    return random(100, 200);
                                if(doAction(noter, "exchange")) {
                                    state = State.MOVING;
                                    next = State.HANDLING_INVENTORY;
                                    clickedStiles = true;
                                    if(path != null) {
                                        Point p = getFirstTileOnMap();
                                        if(p != null)
                                            mouse.move(p);
                                    }
                                    return 100;
                                }
                            } else
                                startWalking(area.getGuides(), State.HANDLING_INVENTORY);
                        } else
                            startWalking(area.getReversedGuides(), State.LOOKING);
                        break;
                }
                break;
            case WALKING_PATH:
                /*if(!style.playerIsFishing(player) && player.getAnimation() >= 0
                        && walking.getEnergy() < 98)
                    startAntiban();
                else */if(!walking.isRunEnabled() && walking.getEnergy() >= energyThreshold) {
                    //if(walking.getEnergy() >= energyThreshold) {
                        interfaces.get(RUN_ID).getComponent(RUN_CHILD_ID).doAction("turn run mode on");
                        return random(500, 1000);
                        /*return random(500, 1000);
                    } else {
                        interfaces.get(RUN_ID).getComponent(RUN_CHILD_ID).doAction("rest");
                        return random(3000, 5000);
                    }*/
                } else {
                    int whatHappened = step(path);
                    if(whatHappened != REACHED_END) {
                        startAntiban();
                    } else {
                        Point p = new Point(random(103, 516), random(103, 335));
                        mouse.move(p);
                        fishingSpot = null;
                    }
                }
                break;
            case MOVING:
                if(!player.isMoving() || (check && goalOnScreen())) {
                    state = next;
                    if(clickedStiles) {
                        inventoryTimer = new Timer(random(1500, 3000));
                        clickedStiles = false;
                    }
                }
                break;
            //<editor-fold defaultstate="collapsed" desc="antiban">
            case ANTIBAN:
                switch(antiban) {
                    case CHANGE_FOCUS:
                        if(focus < 1)
                            focus = 1;
                        else
                            focus = Math.random();
                        return finishAntiban(0, 1);
                    case TAKING_A_BREAK:
                        sleep(random(15000, 2*MINUTE));
                        return finishAntiban(0, 1);
                    case LOOKING_AWAY:
                        return finishAntiban(5000, 15000);
                    case MOVE_CURSOR_OFF_SCREEN:
                        mouse.moveOffScreen();
                        return finishAntiban(500, 10000);
                    case MOVING_CAMERA:
                        if(afterAntiban == State.WALKING_PATH) {
                            RSTile dest = walking.getDestination();
                            if(dest != null)
                                camera.turnToTile(dest, 20);
                        } else {
                            int UD = random(0, 4);
                            if(UD < 2)
                                new Thread(new CameraInput(UDkeycodes[UD], random(0, 500), random(500, 2000))).start();
                            if(afterAntiban == State.FISHING)
                                new Thread(new CameraSpinToSpotThread()).start();
                            else
                                new Thread(new CameraInput(LRkeycodes[random(0,2)], random(0, 100), random(250, 4000))).start();}
                        return finishAntiban(50, 100);
                    case PUT_HAND_ON_MOUSE:
                        int def = mouse.getSpeed();
                        mouse.setSpeed(random(4, 7));
                        moveMouseRandomly(300);
                        mouse.setSpeed(def);
                        return finishAntiban(50, 250);
                    case WIGGLE_MOUSE:
                        switch(antibanState) {
                            case 0:
                                if(!calc.pointOnScreen(mouse.getLocation()))
                                    mouse.move(random(0, game.getWidth(), game.getWidth()/2, 150), random(0, game.getHeight(), game.getHeight()/2, 150));
                                moveMouseRandomly(random(10, 100));
                                antibanState = random(0, 2);
                                return random(0, 1000, 50, 400);
                            case 1:
                                return finishAntiban(50, 250);
                        }
                    case MOVE_MOUSE:
                        mouse.move(random(0, game.getWidth(), game.getWidth()/2, 150), random(0, game.getHeight(), game.getHeight()/2, 150));
                        return finishAntiban(50, 5000);
                    case CHECKING_FISHING_SKILL:
                        switch(antibanState) {
                            case 0:
                                game.openTab(Game.TAB_STATS);
                                antibanState = 1;
                                return random(300, 2500);
                            case 1:
                                if(game.getCurrentTab() != Game.TAB_STATS) {
                                    antibanState = 0;
                                } else {
                                    interfaces.get(FISHING_SKILL_ID).getComponent(FISHING_SKILL_CHILD_ID).doHover();
                                    return finishAntiban(3000, 8000);
                                }
                        }
                    case LOOKING_AT_SKILLS_TAB:
                        game.openTab(Game.TAB_STATS);
                        return finishAntiban(1000, 8000);
                    default:
                        ArrayList<Integer> indexes = new ArrayList<Integer>();
                        for(int i = (shouldBreak()) ? 0:WALKING_AB_START; i < antibans.length; i++)
                            if(antibans[i].isUp())
                                indexes.add(i);
                        if(indexes.size() == 0)
                            state = afterAntiban;
                        else
                            antiban = indexes.get(random(0, indexes.size()));
                        break;
                }
                break;
                //</editor-fold>
        }
        return random(50, 250);
    }

    public void checkForNewFish() {
        RSItem[] inv = inventory.getCachedItems();
        for(int i = 0; i < inv.length && i < oldInventory.length; i++)
            if(inv[i].getID() != oldInventory[i].getID()) {
                style.increment(inv[i].getID());
                failsafe = System.currentTimeMillis() + 600000;
            }
        oldInventory = inv;
    }

    public void onFinish() {
        gui.dispose();
    }

    private Image getImage(String fileName) {
        try {
            File f = new File(GlobalConfiguration.Paths.getScriptsDirectory() + "/gfimg/" + fileName);
            if(f.exists())
                return ImageIO.read(f.toURI().toURL());
            Image img = ImageIO.read(new URL("http://0x098b4a40.webs.com/godless/" + fileName));
            if(img != null) {
                ImageIO.write((RenderedImage)img, "PNG", f);
                return img;
            }
        } catch(IOException e) {
        }
        return null;
    }

    private final Color BG = new Color(50, 50, 50), BG2 = new Color(0, 0, 0);
    private final Color STROKE = new Color(0, 0, 0);
    private final Color INNER_STROKE = new Color(255, 0, 0, 50);
    private final Color FG = new Color(255, 255, 255), SHADOW = Color.BLACK;
    private final Color GLOSS = new Color(255, 255, 255, 66);
    private final Color BREAK_COLOR = new Color(0, 0, 0, 150), LOOKAWAY_COLOR = new Color(0, 0, 0, 75);
    private final Color PROG_BAR_TOP = new Color(255, 0, 0),
        PROG_BAR_BOTTOM = new Color(180, 0, 0);

    private final BasicStroke ONEPXSTROKE = new BasicStroke(1);
    private final int PROG_X = 345, PROG_Y = 315;
    private final GradientPaint PROG_GRADIENT = new GradientPaint(PROG_X, PROG_Y, BG, PROG_X, PROG_Y+24, BG2);
    private final GradientPaint PROG_BLUE_GRADIENT =
            new GradientPaint(PROG_X, PROG_Y+1, PROG_BAR_TOP, PROG_X, PROG_Y+23, PROG_BAR_BOTTOM);
    private PaintIcon[] icons;
    private final Font font1 = new Font("Arial", 0, 9);
    private final Color POLY_BORDER = Color.BLACK, POLY_FILL = new Color(0, 0, 0, 150);
    private final Color IMG_FILL = new Color(150, 0, 150, 100);
    private final Color BUTTON_IN = new Color(0, 0, 0, 80);
    private Point cursor = new Point(-1, -1);

    private final double HALF_PI = Math.PI*0.5, THREE_HALF_PI = Math.PI*1.5;
    private final double[] ARROW_XS = {0, .40, .20, .20, -.20, -.20, -.40};
    private final double[] ARROW_YS = {.10, .10, .10, .10, .10, .10, .10};
    private final int[] ARROW_ZS = {0, 300, 300, 700, 700, 300, 300};
    public void onRepaint(Graphics uncast) {
        Graphics2D g = (Graphics2D)uncast;
        g.setStroke(ONEPXSTROKE);
        g.setFont(font1);
        Point clientCursor = mouse.getLocation();
        if(gui != null && gui.hitStart && gui.shouldPaint()) {
            switch(lastAntiban) {
                case TAKING_A_BREAK:
                    g.setColor(BREAK_COLOR);
                    g.fillRect(0, 0, game.getWidth(), game.getHeight());
                    drawStringInStyledBox(g, "Simulating Short Break...", 100, 100);
                    break;
                case LOOKING_AWAY:
                    g.setColor(LOOKAWAY_COLOR);
                    g.fillRect(0, 0, game.getWidth(), game.getHeight());
                    drawStringInStyledBox(g, "Simulating Looking Away from the Screen...", 100, 100);
                    break;
                case MOVE_MOUSE:
                case MOVE_CURSOR_OFF_SCREEN:
                case WIGGLE_MOUSE:
                case PUT_HAND_ON_MOUSE:
                    g.setColor(FG);
                    drawStringInStyledBox(g, antibans[lastAntiban].getPaintDescription(), clientCursor.x+9, clientCursor.y+22);
                    break;
            }
        }
        if(gui == null || !gui.hitStart || !gui.shouldPaint())
            return;
        while(!mousePath.isEmpty() && mousePath.peek().isUp())
            mousePath.remove();
        MousePathPoint mpp = new MousePathPoint(clientCursor.x, clientCursor.y, 3000);
        if(mousePath.isEmpty() || !mousePath.getLast().equals(mpp))
            mousePath.add(mpp);
        MousePathPoint lastPoint = null;
        for(MousePathPoint p : mousePath) {
            if(lastPoint != null) {
                g.setColor(p.getColor());
                g.drawLine(p.x, p.y, lastPoint.x, lastPoint.y);
            }
            lastPoint = p;
        }
        while(!clicks.isEmpty() && clicks.peek().isUp())
            clicks.remove();
        long clickTime = mouse.getPressTime();
        Point lastClickPos = mouse.getPressLocation();
        if(clicks.isEmpty() || !clicks.getLast().equals(clickTime))
            clicks.add(new Click(lastClickPos.x, lastClickPos.y, clickTime));
        for(Click c : clicks)
            c.drawTo(g);

        if(state == State.WALKING_PATH || afterAntiban == State.WALKING_PATH) {
            Line[] guides = area.getGuides();
            for(int i = 1; i < guides.length; i++)
                guides[i].drawTo(g, guides[i-1]);
            Point last = null, p;
            g.setColor(Color.RED);
            for(RSTile t : path) {
                p = tileToMinimap(t);
                if(last != null)
                    g.drawLine(p.x, p.y, last.x, last.y);
                last = p;
            }
        } else if(state == State.FISHING || afterAntiban == State.FISHING) {
            RSTile t = fishingSpotTile;

            /*double radians = (System.currentTimeMillis() - startTime) / (5000/(2*Math.PI)) % (2*Math.PI);
            double cos = Math.cos(radians);
            double sin = Math.sin(radians);
            double tan = Math.tan(radians);
            Point[] arrowSide1Points = new Point[7];
            Point[] arrowSide2Points = new Point[7];
            for(int i = 0; i < 7; i++) {
                arrowSide1Points[i] = calc.tileToScreen(t, 
                        0.5 + ARROW_XS[i]*cos - ARROW_YS[i]*sin,
                        0.5 + ARROW_XS[i]*sin + ARROW_YS[i]*cos, ARROW_ZS[i]);
                arrowSide2Points[i] = calc.tileToScreen(t, 
                        0.5 + ARROW_XS[i]*cos + ARROW_YS[i]*sin,
                        0.5 + ARROW_XS[i]*sin - ARROW_YS[i]*cos, ARROW_ZS[i]);
            }
            g.setColor(new Color(0, 0, 0, 50));
            int[] xs = new int[7], ys = new int[7];
            for(int i = 0; i < 7; i++) {
                xs[i] = arrowSide1Points[i].x;
                ys[i] = arrowSide1Points[i].y;
            }
            g.fillPolygon(xs, ys, 7);
            for(int i = 0; i < 7; i++) {
                xs[i] = arrowSide2Points[i].x;
                ys[i] = arrowSide2Points[i].y;
            }
            g.fillPolygon(xs, ys, 7);
            xs[0] = arrowSide1Points[6].x;
            ys[0] = arrowSide1Points[6].y;
            xs[1] = arrowSide2Points[6].x;
            ys[1] = arrowSide2Points[6].y;
            for(int i = 0; i < 7; i++) {
                xs[2] = arrowSide2Points[i].x;
                ys[2] = arrowSide2Points[i].y;
                xs[3] = arrowSide1Points[i].x;
                ys[3] = arrowSide1Points[i].y;
                g.fillPolygon(xs, ys, 4);
                xs[0] = xs[3];
                ys[0] = ys[3];
                xs[1] = xs[2];
                ys[1] = ys[2];
            }
            Point last = arrowSide1Points[6], p;
            g.setColor(new Color(255, 255, 255, 120));
            for(int i = 0; i < 7; i++) {
                p = arrowSide1Points[i];
                g.drawLine(p.x, p.y, last.x, last.y);
                last = p;
            }
            last = arrowSide2Points[6];
            for(int i = 0; i < 7; i++) {
                p = arrowSide2Points[i];
                g.drawLine(p.x, p.y, last.x, last.y);
                last = p;
            }
            for(int i = 0; i < 7; i++)
                g.drawLine(arrowSide1Points[i].x, arrowSide1Points[i].y,
                        arrowSide2Points[i].x, arrowSide2Points[i].y);*/
            Point[] points = new Point[12];

            points[0] = calc.tileToScreen(t, 0, 0.75, 0);
            points[1] = calc.tileToScreen(t, 0, 1, 0);
            points[2] = calc.tileToScreen(t, 0.25, 0.99, 0);

            points[3] = calc.tileToScreen(t, 0.75, 0.99, 0);
            points[4] = calc.tileToScreen(t, 0.99, 0.99, 0);
            points[5] = calc.tileToScreen(t, 0.99, 0.75, 0);

            points[6] = calc.tileToScreen(t, 0.99, 0.25, 0);
            points[7] = calc.tileToScreen(t, 0.99, 0, 0);
            points[8] = calc.tileToScreen(t, 0.75, 0, 0);

            points[9] = calc.tileToScreen(t, 0.25, 0, 0);
            points[10] = calc.tileToScreen(t, 0, 0, 0);
            points[11] = calc.tileToScreen(t, 0, 0.25, 0);

            g.setColor(Color.BLACK);
            for(int i = 0; i < 12; i += 3) {
                g.drawLine(points[i].x, points[i].y, points[i+1].x, points[i+1].y);
                g.drawLine(points[i+1].x, points[i+1].y, points[i+2].x, points[i+2].y);
            }

            points[0] = calc.tileToScreen(t, 0.40, 0.5, 0);
            points[1] = calc.tileToScreen(t, 0.60, 0.5, 0);
            points[2] = calc.tileToScreen(t, 0.5, 0.40, 0);
            points[3] = calc.tileToScreen(t, 0.5, 0.60, 0);

            g.setColor(Color.RED);
            g.drawLine(points[0].x, points[0].y, points[1].x, points[1].y);
            g.drawLine(points[2].x, points[2].y, points[3].x, points[3].y);
        }
        g.setPaint(PROG_GRADIENT);
        g.fillRect(PROG_X, PROG_Y, 170, 24);
        int h = 0;
        for(PaintIcon icon : icons)
            h += icon.paintTo(g, h);
        g.setColor(INNER_STROKE);
        g.drawRect(PROG_X+1, PROG_Y+1, 99, 21);
        g.setPaint(PROG_BLUE_GRADIENT);
        int barWidth = skills.getPercentToNextLevel(Skills.FISHING);
        String percText = barWidth + "%";
        g.fillRect(PROG_X+(101 - barWidth),
                PROG_Y+1, barWidth, 22);
        Rectangle percBounds = getStringBounds(percText, g);
        drawTextWithShadow(g, percText, PROG_X + 51 - percBounds.width/2,
                PROG_Y + 12 - percBounds.height/2 + percBounds.y);
        g.setColor(GLOSS);
        g.fillRect(PROG_X, PROG_Y, 102, 12);
        g.setColor(STROKE);
        g.drawRect(PROG_X, PROG_Y, 101, 23);
        //g.drawImage(cursorIMG, clientCursor.x, clientCursor.y, null);

        g.setColor(Color.RED);
        double radians = (System.currentTimeMillis() - startTime) / (2000/(2*Math.PI)) % Math.PI;
        g.drawLine((int)Math.round(clientCursor.x+5*Math.cos(radians)), (int)Math.round(clientCursor.y+5*Math.sin(radians)),
                (int)Math.round(clientCursor.x+5*Math.cos(radians+Math.PI)), (int)Math.round(clientCursor.y+5*Math.sin(radians+Math.PI)));
        g.drawLine((int)Math.round(clientCursor.x+5*Math.cos(radians+HALF_PI)), (int)Math.round(clientCursor.y+5*Math.sin(radians+HALF_PI)),
                (int)Math.round(clientCursor.x+5*Math.cos(radians+THREE_HALF_PI)), (int)Math.round(clientCursor.y+5*Math.sin(radians+THREE_HALF_PI)));
        g.setColor(Color.BLACK);
        g.drawOval(clientCursor.x-5, clientCursor.y-5, 10, 10);
    }
    
    public void mouseMoved(MouseEvent e) {
        cursor = e.getPoint();
        for(PaintIcon p : icons)
            p.hoverTest(cursor);
    }

    public void mouseDragged(MouseEvent e) {
        cursor = e.getPoint();
        for(PaintIcon p : icons)
            p.hoverTest(cursor);
    }

    public void mousePressed(MouseEvent e) {

    }

    public void mouseReleased(MouseEvent e) {
        for(PaintIcon p : icons)
            p.hitTest(e.getPoint());
    }

    public void mouseClicked(MouseEvent e) {

    }

    public void mouseEntered(MouseEvent e) {

    }

    public void mouseExited(MouseEvent e) {

    }

    private void drawStringInStyledBox(Graphics2D g, String s, int x, int y) {
        Rectangle bounds = getStringBounds(s, g);
        int w = bounds.width + 8, h = bounds.height + 8;
        GradientPaint paint = new GradientPaint(x, y, BG, x, y+h, BG2);
        g.setPaint(paint);
        g.fillRect(x, y, w, h);
        g.setColor(INNER_STROKE);
        g.drawRect(x+1,y+1,w-2,h-2);
        drawTextWithShadow(g, s, x+4, y+4+bounds.y);
        g.setColor(GLOSS);
        g.fillRect(x, y, w, h/2);
        g.setColor(STROKE);
        g.drawRect(x, y, w, h);
    }

    private void drawTextWithShadow(Graphics2D g, String s, int x, int y) {
        g.setColor(SHADOW);
        g.drawString(s, x+1, y+1);
        g.setColor(FG);
        g.drawString(s, x, y);
    }

    public void keyTyped(KeyEvent e) {}

    public void keyPressed(KeyEvent e) {}

    public void keyReleased(KeyEvent e) {
        if(!gui.isVisible() && e.getKeyCode() == KeyEvent.VK_F12)
            gui.setVisible(true);
    }

    private interface PaintText {
        public String getText();
    }

    private class MousePathPoint extends Point {
        private long finishTime;
        private double lastingTime;
        public MousePathPoint(int x, int y, int lastingTime) {
            super(x, y);
            this.lastingTime = lastingTime;
            finishTime = System.currentTimeMillis() + lastingTime;
        }
        public boolean isUp() { return System.currentTimeMillis() > finishTime; }
        public Color getColor() {
            return new Color(255, 255, 255, toColor(256*((finishTime-System.currentTimeMillis())/lastingTime)));
        }
    }

    private class Click {
        private long clickTime, finishTime;
        private double radians;
        private int x, y;
        public Click(int x, int y, long clickTime) {
            this.clickTime = clickTime;
            finishTime = clickTime + 5000;
            radians = (clickTime - startTime) / (2000/(2*Math.PI)) % Math.PI;
            this.x = x;
            this.y = y;
        }
        public boolean isUp() { return System.currentTimeMillis() > finishTime; }
        public void drawTo(Graphics2D g) {
            /*Composite def = g.getComposite();
            float f = (finishTime - System.currentTimeMillis())/5000f*0.75f;
            if(f < 0)
                return;
            AlphaComposite ac = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, f);
            g.setComposite(ac);
            g.drawImage(clickIMG, x, y, null);
            g.setComposite(def);*/
            int alpha = (int)((finishTime - System.currentTimeMillis())/5000.0*255);
            if(alpha < 0)
                return;
            g.setColor(new Color(0, 0, 0, alpha));
            g.drawLine((int)(x+5*Math.cos(radians)), (int)(y+5*Math.sin(radians)),
                (int)(x+5*Math.cos(radians+Math.PI)), (int)(y+5*Math.sin(radians+Math.PI)));
            g.drawLine((int)(x+5*Math.cos(radians+HALF_PI)), (int)(y+5*Math.sin(radians+HALF_PI)),
                    (int)(x+5*Math.cos(radians+THREE_HALF_PI)), (int)(y+5*Math.sin(radians+THREE_HALF_PI)));
        }
        public boolean equals(long time) {
            return clickTime == time;
        }
    }

    private int toColor(double d) {
        return Math.min(255, Math.max(0, (int)d));
    }
    
    private class PaintIcon {
        private Image icon;
        private Rectangle bounds;
        private PaintText[] list;
        private boolean open, hovering;
        public PaintIcon(Image icon, Rectangle bounds, PaintText ... texts) {
            this.icon = icon;
            this.bounds = bounds;
            list = texts;
        }
        
        public void toggle() {
            open = !open;
        }

        public void hitTest(Point p) {
            if(bounds.contains(p))
                toggle();
        }

        public void hoverTest(Point p) {
            hovering = bounds.contains(p);
        }

        public int paintTo(Graphics2D g, int h) {
            int height = 0;
            g.drawImage(icon, bounds.x, bounds.y, null);
            g.setColor(STROKE);
            g.drawRect(bounds.x, bounds.y, bounds.width, bounds.height-1);
            g.setColor(hovering ? Color.WHITE:INNER_STROKE);
            g.drawRect(bounds.x+1, bounds.y+1, bounds.width-2, bounds.height-3);
            if(open) {
                g.setColor(BUTTON_IN);
                g.fill(bounds);
            } else {
                g.setColor(GLOSS);
                g.fillRect(bounds.x+1, bounds.y+1, bounds.width-1, bounds.height/2-1);
            }
            if(hovering || open) {
                int width = 0, fontHeight = -1, fontAscend = -1;
                height = 5;
                Rectangle r;
                ArrayList<String> text = new ArrayList<String>();
                for(int i = 0; i < list.length; i++) {
                    for(String s : list[i].getText().split("\n")) {
                        r = getStringBounds(s, g);
                        if(fontHeight < 0) {
                            fontHeight = r.height + 3;
                            fontAscend = r.y;
                        }
                        if(r.width > width)
                            width = r.width;
                        height += fontHeight;
                        text.add(s);
                    }
                }
                width += 8;
                int rectX = bounds.x - width + bounds.width;
                int rectY = bounds.y - height - h;
                GradientPaint gp = new GradientPaint(rectX, rectY, BG, rectX, rectY+height, BG2);
                g.setPaint(gp);
                g.fillRect(rectX, rectY, width, height);
                g.setColor(INNER_STROKE);
                g.drawRect(rectX+1, rectY+1, width-2, height-2);
                int y = rectY + 4 + fontAscend;
                int x = rectX + 4;
                for(String s : text) {
                    g.setColor(SHADOW);
                    g.drawString(s, x+1, y+1);
                    g.setColor(FG);
                    g.drawString(s, x, y);
                    y += fontHeight;
                }
                g.setColor(GLOSS);
                g.fillRect(rectX, rectY, width, height/2);
                g.setColor(STROKE);
                g.drawRect(rectX, rectY, width, height);
            }
            return height;
        }
    }
    
    private Rectangle getStringBounds(String s, Graphics g) {
        LineMetrics l = g.getFontMetrics().getLineMetrics(s, g);
        Rectangle2D r = g.getFontMetrics().getStringBounds(s, g);
        return new Rectangle(0, (int)l.getAscent(), (int)r.getWidth(), (int)r.getHeight());
    }

    private java.util.Random generateSeededRandom(String text) {
        MessageDigest coder;
        try {
            coder = MessageDigest.getInstance("MD5");
            coder.update(text.getBytes());
            byte[] bytes = coder.digest();
            long result1 = 0, result2 = 0;
            for (int i = 0; i < 8; i++)
                result1 = (result1 << 8) | bytes[i];
            for (int i = 8; i < 16; i++)
                result2 = (result2 << 8) | bytes[i];
            return new java.util.Random(result1 - result2);
        } catch (NoSuchAlgorithmException ex) {
        }
        return new java.util.Random();
    }

    private boolean goalOnScreen() {
        switch(next) {
            case LOOKING:
                return spots.getOnScreenInArea(area, style) != null;
            case HANDLING_INVENTORY:
                switch(handler.getType()) {
                    case InventoryHandler.NOTER:
                        RSNPC noter = npcs.getNearest(handler.getID());
                        return noter != null && noter.isOnScreen();
                    case InventoryHandler.BOOTH:
                        RSObject booth = objects.getNearest(handler.getID());
                        return booth != null && booth.isOnScreen();
                    case InventoryHandler.BANKER:
                        RSNPC banker = npcs.getNearest(handler.getID());
                        return banker != null && banker.isOnScreen() &&
                                banker.getModel() != null;
                }
        }
        return false;
    }
    
    //<editor-fold defaultstate="collapsed" desc="Inventory Methods">
    private RSItem getFirstInInventory(FishingStyle style) {
        RSItem[] inv = inventory.getItems();
        for(int col = 0; col < 4; col++)
            for(int row = 0; row < 7; row++)
                if(style.containsFishID(inv[row*4+col].getID()))
                    return inv[row*4+col];
        return null;
    }
    
    private RSItem getNextUnwantedFish() {
        RSItem item;
        for(; inventoryColumn < 4; inventoryColumn++) {
            for(; inventoryRow < 7; inventoryRow++) {
                item = inventory.getItems()[inventoryRow*4+inventoryColumn];
                if(item != null && style.containsUnwantedFishID(item.getID())) {
                    return item;
                }
            }
            inventoryRow = 0;
        }
        return null;
    }

    private boolean inventoryContainsNoFish(FishingStyle style) {
        for(RSItem item : inventory.getItems())
            if(item != null && style.containsWantedFishID(item.getID()))
                return false;
        return true;
    }

    private boolean inventoryContainsUnwantedFish(FishingStyle style) {
        RSItem[] items = inventory.getItems();
        final int count = mousekeys ? 24 : 28;
        RSItem item;
        for(int i = 0; i < count; i++) {
            item = items[i];
            if(item != null && style.containsUnwantedFishID(item.getID()))
                return true;
        }
        return false;
    }
    //</editor-fold>

    private boolean spotMoved(boolean andHumanCanTell) {
        return !fishingSpotTile.equals(fishingSpot.getLocation())
                && (!andHumanCanTell || getNPCsAt(fishingSpotTile).size() == 0);
    }

    private boolean shouldBreak() {
        return !(afterAntiban == State.WALKING_PATH || afterAntiban == State.MOVING);
    }

    private void startAntiban() {
        afterAntiban = state;
        state = State.ANTIBAN;
        antiban = -1;
        antibanState = 0;
    }

    private int finishAntiban(int lo, int hi) {
        state = afterAntiban;
        afterAntiban = null;
        int index = antiban;
        antiban = -1;
        return antibans[index].resetIn(lo, hi);
    }

    private boolean check;
    private void startMoving(State nextState, boolean goalCheck) {
        check = goalCheck;
        state = State.MOVING;
        next = nextState;
    }

    private ArrayList<RSNPC> getNPCsAt(RSTile t) {
        ArrayList<RSNPC> at = new ArrayList<RSNPC>();
        for(RSNPC r : npcs.getAll()) {
            if(r == null || r.getLocation() == null)
                continue;
            if(r.getLocation().equals(t))
                at.add(r);
        }
        return at;
    }

    private ArrayList<RSNPC> getNPCsAt(RSTile t, int id) {
        ArrayList<RSNPC> at = new ArrayList<RSNPC>();
        for(RSNPC r : npcs.getAll()) {
            if(r == null || r.getLocation() == null)
                continue;
            if(r.getLocation().equals(t) && r.getID() == id)
                at.add(r);
        }
        return at;
    }

    private boolean doAction(RSNPC npc, String action) {
		for (int i = 0; i < 20; i++) {
			if (!calc.pointOnScreen(npc.getScreenLocation()))
				return false;
            Point p = null;
            RSModel m = npc.getModel();
            if(m != null && m.getTriangles().length > 0)
                p = getRandomPoint(m);
            if(p == null)
                p = new Point((int) Math.round(npc.getScreenLocation().getX()) + random(-5, 5), (int) Math.round(npc.getScreenLocation().getY()) + random(-5, 5));
			mouse.move(p);
			String[] items = menu.getItems();
			if (items.length > 0 && items[0].toLowerCase().startsWith(action.toLowerCase())) {
				mouse.click(true);
				return true;
			} else {
				String[] menuItems = menu.getItems();
				for (String item : menuItems) {
					if (item.toLowerCase().contains(action.toLowerCase())) {
						mouse.click(false);
						return menu.doAction(action);
					}
				}
			}
		}
		return false;
	}
    //<editor-fold defaultstate="collapsed" desc="Walking">

    private Line[] reverse(Line[] lines) {
        Line[] rev = new Line[lines.length];
        for(int i = 0; i < lines.length; i++)
            rev[i] = lines[lines.length - (i+1)];
        return rev;
    }

    private ArrayList<RSTile> straightPathTo(RSTile t, int variation) {
        RSTile player = getMyPlayer().getLocation();
        double xdist = t.getX() - player.getX();
        double ydist = t.getY() - player.getY();
        double dist = Math.sqrt(xdist*xdist + ydist*ydist);
        xdist /= dist;
        ydist /= dist;

        //log(xdist + ", " + ydist);

        Line l1 = new Line(player.getX() - (int)Math.round(ydist*variation), player.getY() + (int)Math.round(xdist*variation),
                player.getX() + (int)Math.round(ydist*variation), player.getY() + (int)Math.round(xdist*variation));
        Line l2 = new Line(t.getX() - (int)Math.round(ydist*variation), t.getY() + (int)Math.round(xdist*variation),
                t.getX() + (int)Math.round(ydist*variation), t.getY() + (int)Math.round(xdist*variation));
        //log(l2.x + ", " + l2.y + " " + l2.x2 + ", " + l2.y2);
        //log(l1.x + ", " + l1.y + " " + l1.x2 + ", " + l1.y2);
        return generatePath(new Line[]{l1, l2});
    }

    private int loc;
    private void startWalking(Line[] guides, State nextState) {
        state = State.WALKING_PATH;
        next = nextState;
        path = generatePath(guides);
        boolean onScreen = false;
        for(RSTile t : path)
            if(calc.tileOnMap(t)) {
                onScreen = true;
                break;
            }
        if(!onScreen) {
            int nearest = getNearestTile();
            //ArrayList<RSTile> aStarPath = generatePath(getMyPlayer().getLocation(),
                    //path.get(nearest));
            //if(aStarPath == null) {
                //log("Lost!");
                path.addAll(nearest, straightPathTo(path.get(nearest), 5));
            //} else
                //path.addAll(nearest, aStarPath);
        }
        locatePlayer();
    }

    private int getNearestTile() {
        int shortest = 0;
        int shortestDist = calc.distanceTo(path.get(0)), dist;
        for(int i = 1; i < path.size(); i++) {
            dist = calc.distanceTo(path.get(i));
            if(dist < shortestDist) {
                shortestDist = dist;
                shortest = i;
            }
        }
        return shortest;
    }

    private void locatePlayer() {
        for(loc = path.size() - 1; loc >= 0 && !calc.tileOnMap(path.get(loc)); loc--)
            continue;
        if(loc < 0)
            loc = 0;
    }

    private Point getFirstTileOnMap() {
        for(int i = 0; i < path.size(); i++)
            if(calc.tileOnMap(path.get(i)))
                return calc.tileToMinimap(path.get(i));
        return null;
    }
    
    private final int DID_NOTHING = 0, CLICKED_A_TILE = 1, REACHED_END = 2;
    private int step(ArrayList<RSTile> path) {
        if(loc >= path.size() - 1) {
            if(calc.tileOnMap(path.get(path.size() - 1))) {
                walking.walkTileMM(path.get(path.size() - 1));
                startMoving(next, true);
                return REACHED_END;
            } else
                return DID_NOTHING;
        }
        if(loc < path.size() && calc.tileOnMap(path.get(loc))) {
            walking.walkTileMM(path.get(loc));
            loc += random(6, 13);
            return CLICKED_A_TILE;
        } else if(!getMyPlayer().isMoving())
            locatePlayer();
        return DID_NOTHING;
    }

    private ArrayList<RSTile> generatePath(Line[] lines) {
        double minStep = 5, maxStep = 10, wander = 3;
        if(lines.length < 2)
            return null;
        ArrayList<RSTile> entirePath = new ArrayList<RSTile>();
        Line l1, l2 = lines[0];
        double distFromCenter = random(0, l2.getDistance()+1);
        RSTile p = l2.translate(distFromCenter);
        distFromCenter = l2.getDistance()/2 - distFromCenter;
        double centerXdist, centerYdist,
                line1Xdist, line1Ydist,
                line2Xdist, line2Ydist;
        double line1dist, line2dist, centerDist;
        double x, y;
        double distOnLine, last, cap1, cap2, move;
        double distFromCenterX1, distFromCenterY1, distFromCenterX2, distFromCenterY2;
        double force1, force2, slopeX, slopeY, slopeDist;
        boolean finished;
        int lastX = p.getX(), lastY = p.getY(), curX, curY;
        double dist, xdist, ydist;
        for(int i = 1; i < lines.length; i++) {
            l1 = l2;
            l2 = lines[i];
            centerXdist = l2.getCenterX() - l1.getCenterX();
            centerYdist = l2.getCenterY() - l1.getCenterY();
            centerDist = Math.sqrt(centerXdist*centerXdist + centerYdist*centerYdist);
            line1Xdist = l2.getX() - l1.getX();
            line1Ydist = l2.getY() - l1.getY();
            line2Xdist = l2.getX2() - l1.getX2();
            line2Ydist = l2.getY2() - l1.getY2();

            centerXdist /= centerDist;
            centerYdist /= centerDist;
            line1Xdist /= centerDist;
            line1Ydist /= centerDist;
            line2Xdist /= centerDist;
            line2Ydist /= centerDist;
            distOnLine = 0;
            last = 0;
            finished = false;
            while(!finished) {

                distOnLine += random(minStep, maxStep);
                if(distOnLine >= centerDist) {
                    distOnLine = centerDist;
                    finished = true;
                }
                x = centerXdist * distOnLine + l1.getCenterX();
                y = centerYdist * distOnLine + l1.getCenterY();

                distFromCenterX1 = x-(line1Xdist * distOnLine + l1.getX());
                distFromCenterY1 = y-(line1Ydist * distOnLine + l1.getY());

                distFromCenterX2 = x-(line2Xdist * distOnLine + l1.getX2());
                distFromCenterY2 = y-(line2Ydist * distOnLine + l1.getY2());

                slopeX = distFromCenterX2 - distFromCenterX1;
                slopeY = distFromCenterY2 - distFromCenterY1;
                slopeDist = Math.sqrt(slopeX*slopeX + slopeY*slopeY);
                slopeX /= slopeDist;
                slopeY /= slopeDist;

                line1dist = Math.sqrt(distFromCenterX1*distFromCenterX1 +
                        distFromCenterY1*distFromCenterY1);
                line2dist = Math.sqrt(distFromCenterX2*distFromCenterX2 +
                        distFromCenterY2*distFromCenterY2);

                move = (distOnLine - last)/maxStep*wander;

                force1 = line1dist+distFromCenter;
                force2 = line2dist-distFromCenter;

                cap1 = Math.min(move, force1);
                cap2 = Math.min(move, force2);

                if(force1 < 0)
                    distFromCenter -= force1;
                else if(force2 < 0)
                    distFromCenter += force2;
                else
                    distFromCenter += random(-cap1, cap2);

                if(finished) {
                    RSTile t = l2.translateFromCenter(distFromCenter);
                    curX = t.getX();
                    curY = t.getY();
                } else {
                    curX = (int)Math.round(distOnLine*centerXdist + l1.getCenterX() + distFromCenter*slopeX);
                    curY = (int)Math.round(distOnLine*centerYdist + l1.getCenterY() + distFromCenter*slopeY);
                }

                xdist = curX - lastX;
                ydist = curY - lastY;
                dist = Math.sqrt(xdist*xdist + ydist*ydist);
                xdist /= dist;
                ydist /= dist;
                for(int j = 0; j < dist; j++)
                    entirePath.add(new RSTile((int)Math.round(xdist*j + lastX), (int)Math.round(ydist*j + lastY)));

                last = distOnLine;
                lastX = curX;
                lastY = curY;
            }
        }
        return entirePath;
    }

    public ArrayList<RSTile> cutUp(ArrayList<RSTile> tiles) {
        ArrayList<RSTile> path = new ArrayList<RSTile>();
        int index = 0;
        while(index < tiles.size()) {
            path.add(tiles.get(index));
            index += random(8, 12);
        }
        if(!path.get(path.size()-1).equals(tiles.get(tiles.size()-1)))
            path.add(tiles.get(tiles.size()-1));
        return path;
    }

    //<editor-fold defaultstate="collapsed" desc="A*">
    public int[][] blocks;
    public int baseX,  baseY;

    private void validate(RSTile start){
        if(game.getBaseX() != baseX ||
                game.getBaseY() != baseY){
            baseX = game.getBaseX();
            baseY = game.getBaseY();
            blocks = walking.getCollisionFlags(game.getPlane()).clone();
        }
    }

    private boolean contains(RSTile t){
        int x = t.getX()-baseX;
        int y = t.getY()-baseY;
        return (x >= 0 || x < 104 || y >= 0 || y < 104);
    }

    private ArrayList<RSTile> generatePath(RSTile cur, RSTile dest) {
        validate(cur);
        if(!contains(dest))
            return null;
        ArrayList<PathTile> closed = new ArrayList<PathTile>(), open = new ArrayList<PathTile>();
        int curX = cur.getX() - baseX,
                curY = cur.getY() - baseY,
                destX = dest.getX() - baseX,
                destY = dest.getY() - baseY;
        PathTile current = new PathTile(curX, curY),
                destination = new PathTile(destX, destY);
        open.add(current);
        boolean found = false;
        while (open.size() > 0 && !found) {
            current = cheapestTile(open);
            closed.add(current);
            open.remove(open.indexOf(current));
            for (PathTile t : adjacentTilesTo(current)) {
                if (!closed.contains(t)) {
                    int index = open.indexOf(t);
                    if (index < 0) {
                        t.parent = current;
                        t.gScore = current.gScore + calculateGCost(t, current);
                        t.fScore = t.gScore + calculateHCost(t, destination);
                        open.add(t);
                    } else {
                        PathTile old = open.get(index);
                        if (current.gScore + calculateGCost(old, current) < old.gScore) {
                            old.parent = current;
                            old.gScore = current.gScore + calculateGCost(old, current);
                            old.fScore = old.gScore + calculateHCost(old, destination);
                        }
                    }
                }
            }
            if (closed.contains(destination))
                found = true;
        }
        if (found)
            return solve(closed.get(closed.size() - 1), baseX, baseY);
        return null;
    }

    private ArrayList<RSTile> solve(PathTile pathEnd, int baseX, int baseY) {
        ArrayList<RSTile> reversedPath = new ArrayList<RSTile>();
        PathTile p = pathEnd;
        while (p.parent != null)
            reversedPath.add(reversedPath.size(), p.toRSTile(baseX, baseY));
        return reversedPath;
    }

    private LinkedList<PathTile> adjacentTilesTo(PathTile t) {
        LinkedList<PathTile> adjacent = new LinkedList<PathTile>();
        int curX = t.x, curY = t.y;
        if (curX > 0 && curY < 103 && (blocks[curX - 1][curY + 1] & 0x1280138) == 0 && (blocks[curX - 1][curY] & 0x1280108) == 0 && (blocks[curX][curY + 1] & 0x1280120) == 0)
            adjacent.add(new PathTile(curX - 1, curY + 1));
        if (curY < 103 && (blocks[curX][curY + 1] & 0x1280120) == 0)
            adjacent.add(new PathTile(curX, curY + 1));
        if (curX > 0 && curY < 103 && (blocks[curX - 1][curY + 1] & 0x1280138) == 0 && (blocks[curX - 1][curY] & 0x1280108) == 0 && (blocks[curX][curY + 1] & 0x1280120) == 0)
            adjacent.add(new PathTile(curX + 1, curY + 1));
        if (curX > 0 && (blocks[curX - 1][curY] & 0x1280108) == 0)
            adjacent.add(new PathTile(curX - 1, curY));
        if (curX < 103 && (blocks[curX + 1][curY] & 0x1280180) == 0)
            adjacent.add(new PathTile(curX + 1, curY));
        if (curX > 0 && curY > 0 && (blocks[curX - 1][curY - 1] & 0x128010e) == 0 && (blocks[curX - 1][curY] & 0x1280108) == 0 && (blocks[curX][curY - 1] & 0x1280102) == 0)
            adjacent.add(new PathTile(curX - 1, curY - 1));
        if (curY > 0 && (blocks[curX][curY - 1] & 0x1280102) == 0)
            adjacent.add(new PathTile(curX, curY - 1));
        if (curX < 103 && curY > 0 && (blocks[curX + 1][curY - 1] & 0x1280183) == 0 && (blocks[curX + 1][curY] & 0x1280180) == 0 && (blocks[curX][curY - 1] & 0x1280102) == 0)
            adjacent.add(new PathTile(curX + 1, curY - 1));
        return adjacent;
    }

    private PathTile cheapestTile(ArrayList<PathTile> open) {
        PathTile c = null;
        for (PathTile t : open)
            if (c == null || t.fScore < c.fScore)
                c = t;
        return c;
    }

    private int calculateGCost(PathTile from, PathTile to) {
        return (int)(Math.sqrt(Math.pow(from.getX() - to.getX(), 2) + Math.pow(from.getY() - to.getY(), 2))*10);
    }

    private int calculateHCost(PathTile to, PathTile from) {
        return Math.abs(to.getX() - from.getX()) + Math.abs(to.getY() - from.getY())*10;
    }

    /*private boolean canReach(RSTile t) {
        validate(getMyPlayer().getLocation());
        int x = t.getX() - baseX, y = t.getY() - baseY;
        return (x >= 0 | x < 104 || y >= 0 || y < 104) && map[x][y];
    }*/

    private class PathTile {

        public PathTile(int x, int y) {
            this.x = x;
            this.y = y;
            fScore = gScore = 0;
        }

        public boolean isValid() {
            return !(x < 0 || y < 0 || x >= 104 || y >= 104);
        }

        public RSTile toRSTile(int baseX, int baseY) {
            return new RSTile(x + baseX, y + baseY);
        }

        public int getX() {
            return x;
        }

        public int getY() {
            return y;
        }

        public boolean equals(Object o) {
            if(o == null || !(o instanceof PathTile))
                return false;
            PathTile another = (PathTile)o;
            return x == another.getX() && y == another.getY();
        }
        private int x,  y;
        public PathTile parent;
        public int gScore,  fScore;
    }

    //</editor-fold>

    private class Line {
        private int x, y, xdist, ydist, x2, y2, centerX, centerY;
        private RSTile t1, t2;
        private double dist;

        public Line(int x1, int y1, int x2, int y2) {

            t1 = new RSTile(x1, y1);
            t2 = new RSTile(x2, y2);
            x = x1;
            y = y1;
            this.x2 = x2;
            this.y2 = y2;
            xdist = x2 - x1;
            ydist = y2 - y1;
            centerX = x + (int)(0.5 * xdist);
            centerY = y + (int)(0.5 * ydist);
            dist = Math.sqrt(xdist*xdist + ydist*ydist);
        }

        public int getCenterX() {
            return centerX;
        }

        public int getCenterY() {
            return centerY;
        }

        public RSTile getRandomRSTile() {
            double rand = Math.random();
            return new RSTile(x + (int)(xdist*rand), y + (int)(ydist*rand));
        }

        public RSTile getTile1() { return t1; }
        public RSTile getTile2() { return t2; }

        public void drawTo(Graphics g, Line line) {
            //if(!calc.tileOnMap(t1) || !calc.tileOnMap(t2))
                //return;
            //if(calc.tileOnMap(line.getTile1()) && calc.tileOnMap(line.getTile2())) {
                Point p1 = tileToMinimap(t1);
                Point p2 = tileToMinimap(t2);
                Point p3 = tileToMinimap(line.getTile2());
                Point p4 = tileToMinimap(line.getTile1());
                GeneralPath path = new GeneralPath();
                path.moveTo(p1.x, p1.y);
                path.lineTo(p2.x, p2.y);
                path.lineTo(p3.x, p3.y);
                path.lineTo(p4.x, p4.y);
                path.closePath();
                g.setColor(POLY_FILL);
                ((Graphics2D)g).fill(path);
                g.setColor(POLY_BORDER);
                ((Graphics2D)g).draw(path);
            //}
        }

        public int getX() {
            return x;
        }

        public int getY() {
            return y;
        }

        public int getX2() {
            return x2;
        }

        public int getY2() {
            return y2;
        }

        public int getXDistance() {
            return xdist;
        }

        public int getYDistance() {
            return ydist;
        }

        public double getDistance() {
            return dist;
        }

        public RSTile translate(double length) {
            return new RSTile((int)Math.round(length*(xdist/dist))+x, (int)Math.round(length*(ydist/dist))+y);
        }

        public RSTile translateFromCenter(double length) {
            return new RSTile((int)Math.round(centerX - (xdist/dist)*length), (int)Math.round(centerY - (ydist/dist)*length));
        }
    }

    //</editor-fold>
    //<editor-fold defaultstate="collapsed" desc="Fishing Info">

    private class FishingStyle {

        private String name, action, actionBefore;
        private Fish[] targets;
        private Gear[] gear;
        private int[] gearIDs;
        private int[] animations;

        private FishingStyle(String name, String action, Fish[] targets, int[] animations, Gear ... gear) {
            this.name = name;
            this.action = action.toLowerCase();
            this.animations = animations;
            this.targets = targets;
            this.gear = gear;
            if(gear.length == 1 && gear[0].bait > 0) {
                gearIDs = new int[2];
                gearIDs[0] = gear[0].id;
                gearIDs[1] = gear[0].bait;
            } else {
                gearIDs = new int[gear.length];
                for(int i = 0; i < gearIDs.length; i++)
                    gearIDs[i] = gear[i].id;
            }
        }

        private FishingStyle(String name, String action, String actionBefore, Fish[] targets, int[] animations, Gear ... gear) {
            this.name = name;
            this.action = action.toLowerCase();
            this.animations = animations;
            this.targets = targets;
            this.gear = gear;
            gearIDs = new int[gear.length];
            if(gear.length == 1 && gear[0].bait > 0) {
                gearIDs = new int[2];
                gearIDs[0] = gear[0].id;
                gearIDs[1] = gear[0].bait;
            } else {
                gearIDs = new int[gear.length];
                for(int i = 0; i < gearIDs.length; i++)
                    gearIDs[i] = gear[i].id;
            }
            this.actionBefore = actionBefore.toLowerCase();
        }

        public int[] getGearIDs() {
            return gearIDs;
        }

        public boolean playerHasBait() {
            if(gear.length == 0 || gear[0].bait == 0)
                return true;
            return inventory.contains(gear[0].bait);
        }

        public boolean clickFishingSpot(RSNPC spot) {
            while(menu.isOpen()) {
                int def = mouse.getSpeed();
                mouse.setSpeed(random(6, 12));
                mouse.move(random(0, game.getWidth()), random(0, game.getHeight()));
                mouse.setSpeed(def);
                sleep(random(5,30));
            }
            int limit = random(10, 20);
            attemptLoop:
            for(int attempts = 0; attempts < limit; attempts++) {
                if(!spot.isOnScreen())
                    return false;
                Point p = null;
                RSModel m = spot.getModel();
                if(m != null)
                    p = getRandomPoint(spot.getModel());
                if(p == null)
                    p = calc.tileToScreen(spot.getLocation());
                    
                if(!calc.pointOnScreen(p))
                    return false;
                Point p2 = new Point(p.x, p.y);
                do {
                    p2.x = random(Math.max(4, p.x - 5), Math.min(516, p.x + 5));
                    p2.y = random(Math.max(4, p.y-5), Math.min(338, p.y + 5));
                } while(!calc.pointOnScreen(p2));
                mouse.move(p2);
                sleep(random(30, 100));
                String[] actions = menu.getItems();
                if(actions.length < 3)
                    return false;
                if(actionBefore == null && actions[0].toLowerCase().contains(action)) {
                    mouse.click(true);
                    return true;
                }
                ArrayList<Integer> occuring = new ArrayList<Integer>();
                if(actionBefore == null) {
                    for(int i = 0; i < actions.length; i++)
                        if(actions[i].toLowerCase().contains(action))
                            occuring.add(i);
                    if(occuring.size() == 0)
                        continue attemptLoop;
                    mouse.click(false);
                    sleep(random(50, 250));
                    return menu.clickIndex(occuring.get(random(0, occuring.size())));
                }
                String lastAction = actions[0].toLowerCase(), a;
                for(int i = 1; i < actions.length; i++) {
                    a = actions[i].toLowerCase();
                    if(a.contains(action) && lastAction.contains(actionBefore))
                        occuring.add(i);
                    lastAction = a;
                }
                if(occuring.size() == 0)
                    continue;
                mouse.click(false);
                sleep(random(25,100));
                return menu.clickIndex(occuring.get(random(0, occuring.size())));
            }
            return false;
        }

        public boolean fishingSpotIsTarget(RSNPC npc, SpotInfo spots) {
            //return spots.getID() == npc.getID();
            //if(spots.getID() == npc.getID())
                //return true;
            String[] actions = npc.getActions();
            if(actionBefore == null) {
                for(String s : actions) {
                    if(s == null)
                        continue;
                    if(s.toLowerCase().contains(action))
                        return true;
                }
                return false;
            }
            String lastAction, a;
            if(actions[0] == null)
                lastAction = "";
            else
                lastAction = actions[0].toLowerCase();
            for(int i = 1; i < actions.length; i++) {
                if(actions[i] == null) {
                    lastAction = "";
                    continue;
                }
                a = actions[i].toLowerCase();
                if(a.contains(action) && lastAction.contains(actionBefore))
                    return true;
                lastAction = a;
            }
            return false;
        }

        public boolean playerIsFishing(RSPlayer p) {
            for(int i : animations)
                if(p.getAnimation() == i)
                    return true;
            return false;
        }

        public boolean containsFishID(int id) {
            for(int i = 0; i < targets.length; i++)
                if(id == targets[i].getID())
                    return true;
            return false;
        }

        public boolean containsWantedFishID(int id) {
            for(int i = 0; i < targets.length; i++)
                if(id == targets[i].getID() && targets[i].shouldKeep())
                    return true;
            return false;
        }

        public boolean containsUnwantedFishID(int id) {
            for(int i = 0; i < targets.length; i++)
                if(id == targets[i].getID() && !targets[i].shouldKeep())
                    return true;
            return false;
        }

        public void increment(int id) {
            for(int i = 0; i < targets.length; i++)
                if(id == targets[i].getID()) {
                    targets[i].incrementCount();
                    return;
                }
        }

        public void resetExpecting() {
            for(Fish f : targets)
                f.expectingCatch = false;
        }

        /*public int getBaitID() {
            if(this == ROD)
                return 313;
            if(this == FLY)
                return 314;
            return -1;
        }*/

        public String getName() { return name; }
        public String toString() { return name; }
        public String getAction() { return action; }
        public Fish[] getFish() { return targets; }
        public Object[][] getFishForGUI() {
            Object[][] list = new Object[targets.length][];
            for(int i = 0; i < targets.length; i++)
                list[i] = new Object[] { targets[i], targets[i].shouldKeep() };
            return list;
        }
        /*public int[] getGearIDs() {
            if(gear != null)
                return gear;
            if(gearID < 0) {
                return gear = new int[] {};
            }
            baitID = getBaitID();
            if(baitID < 0)
                return gear = new int[] { gearID };
            return gear = new int[] { gearID, baitID };
        }*/
        public Gear[] getGear() {
            return gear;
        }

        private boolean playerHasGear() {
            if(gear.length == 0)
                return true;
            for(Gear g : gear)
                if(g.exists())
                    return true;
            return false;
        }
    }

    private class Catch {
        public long catchTime, timeTaken;
        public Catch(long catchTime, long timeTaken) {
            this.catchTime = catchTime;
            this.timeTaken = timeTaken;
        }
    }

    private class Fish {

        private int id, count, price = -1, min = -1, max = -1;
        private GELookupThread looker;
        private boolean keep;
        private String name;
        private boolean expectingCatch;
        private LinkedList<Catch> catches;
        private long lastCatch;
        private Fish(String name, int id) {
            this.name = name;
            this.id = id;
            keep = true;
            catches = new LinkedList<Catch>();
        }
        public int getID() { return id; }
        public String getName() { return name; }
        public String toString() { return name; }
        public boolean shouldKeep() { return keep; }
        public void setKeep(boolean b) { keep = b; }
        public void incrementCount() { 
            count++;
            /*long currentTime = System.currentTimeMillis();
            while(!catches.isEmpty() && currentTime - catches.peek().catchTime > 3600000)
                catches.pop();
            if(expectingCatch)
                catches.add(new Catch(currentTime, currentTime - lastCatch));
            lastCatch = currentTime;
            expectingCatch = true;*/
        }
        public int getCount() { return count; }
        public String getPaintText() {

            /*long total = 0;
            for(Catch c : catches)
                total += c.timeTaken;
            int size = catches.size();
            String text = "N/A";
            if(size > 0)
                text = "" + (total / size);*/
            return name + ":\n-Caught: " + count +
                    //"\n-Time per catch: " + total +
                    "\n-Min / Mid / Max: " + 
                        shorthand(getMinPrice()) + " / " +
                        shorthand(getMarketPrice()) + " / " +
                        shorthand(getMaxPrice()) +
                    "\n-Profit: " + shorthand(getMinProfit()) + " - " + shorthand(getMaxProfit()) +
                    "\n-Catches per hour: " + (int)(count/(double)(System.currentTimeMillis() - startTime)
                            * 3600000);
        }
        public int getMinProfit() {
            return getMinPrice()*count;
        }
        public int getMaxProfit() {
            return getMaxPrice()*count;
        }
        public int getMarketPrice() {
            if(price < 0) {
                if(looker == null)
                    looker = new GELookupThread(id);
                price = looker.getMarket();
            }
            return price;
        }
        public int getMinPrice() {
            if(min < 0) {
                if(looker == null)
                    looker = new GELookupThread(id);
                min = looker.getMin();
            }
            return min;
        }
        public int getMaxPrice() {
            if(max < 0) {
                if(looker == null)
                    looker = new GELookupThread(id);
                max = looker.getMax();
            }
            return max;
        }
    }

    private String shorthand(int price) {
        if(price > 10000000)
            return price/1000000 + "M";
        if(price > 100000)
            return price/1000 + "K";
        return price+"";
    }
    
    private final Fish
        SARDINE = new Fish("Sardine", 327),
        HERRING = new Fish("Herring", 345),
        TROUT = new Fish("Trout", 335),
        SALMON = new Fish("Salmon", 331),
        LOBSTER = new Fish("Lobster", 377),
        SHRIMP = new Fish("Shrimp", 317),
        ANCHOVIES = new Fish("Anchovies", 321),
        TUNA = new Fish("Tuna", 359),
        SWORDFISH = new Fish("Swordfish", 371),
        CRAYFISH = new Fish("Crayfish", 13435),
        SHARK = new Fish("Shark", 383),
        PIKE = new Fish("Pike", 349),
        MONKFISH = new Fish("Monkfish", 7944);

    private class Gear {
        private int id, bait;
        private boolean equipped;
        public Gear(int id) {
            this.id = id;
        }
        public Gear(int id, boolean equipped) {
            this.id = id;
            this.equipped = equipped;
        }
        public Gear(int id, int bait) {
            this.bait = bait;
            this.id = id;
        }
        public boolean exists() {
            if(equipped && equipment.containsAll(id))
                return true;
            else if(bait > 0)
                return inventory.containsAll(id, bait);
            return inventory.contains(id);
        }
    }

    /*private RSTile minimapToTile(Point p) {
        RSTile player = getMyPlayer().getLocation();
        Point a = calc.tileToMinimap(player);
        Point b = calc.tileToMinimap(new RSTile(player.getX(), player.getY() + 16));
        Point c = calc.tileToMinimap(new RSTile(player.getX() + 16, player.getY()));

        double bDistX = (b.x - a.x)/16.0;
        double bDistY = (b.y - a.y)/16.0;

        double cDistX = (c.x - a.x)/16.0;
        double cDistY = (c.y - a.y)/16.0;

        int xDist = (int)Math.round(p.x - a.x);
        int yDist = (int)Math.round(p.y - a.y);

        return new RSTile(player.getX() + (int)Math.round(xDist/cDistX + yDist/bDistX),
                player.getY() + (int)Math.round(yDist/bDistY + xDist/cDistY));
    }*/

    private Point tileToMinimap(RSTile t) {
        RSTile player = getMyPlayer().getLocation();
        Point a = calc.tileToMinimap(player);
        Point b = calc.tileToMinimap(new RSTile(player.getX(), player.getY() + 16));
        Point c = calc.tileToMinimap(new RSTile(player.getX() + 16, player.getY()));

        double bDistX = (b.x - a.x)/16.0;
        double bDistY = (b.y - a.y)/16.0;

        double cDistX = (c.x - a.x)/16.0;
        double cDistY = (c.y - a.y)/16.0;

        int xDist = t.getX() - player.getX();
        int yDist = t.getY() - player.getY();

        return new Point(a.x + (int)Math.round(cDistX*xDist + bDistX*yDist),
                a.y + (int)Math.round(bDistY*yDist + cDistY*xDist));
    }

    private final Gear
            NORMAL_ROD = new Gear(307, 313),
            FLY_ROD = new Gear(309, 314),
            LOBSTER_CAGE = new Gear(301),
            SMALL_NET = new Gear(303),
            CRAY_CAGE = new Gear(13431),
            NORMAL_HARPOON = new Gear(311),
            CLAY_HARPOON = new Gear(14109, true),
            BARB_TAIL_HARPOON = new Gear(10129, true);

    private final FishingStyle ROD = new FishingStyle("Rod Fishing", "Bait", new Fish[]{SARDINE, HERRING}, new int[]{622, 623}, NORMAL_ROD),
        FLY = new FishingStyle("Fly Fishing", "Lure", new Fish[]{TROUT, SALMON}, new int[]{622, 623}, FLY_ROD),
        PIKE_FISHING = new FishingStyle("River baiting", "Bait", "Lure", new Fish[]{PIKE}, new int[]{622, 623}, NORMAL_ROD),
        CAGE = new FishingStyle("Lobster Caging", "Cage", new Fish[] {LOBSTER}, new int[]{ 619 }, LOBSTER_CAGE),
        NET = new FishingStyle("Small Net Fishing", "Net", new Fish[]{ANCHOVIES, SHRIMP}, new int[] { 621 }, SMALL_NET),
        HARPOON = new FishingStyle("Harpooning", "Harpoon", "Cage", new Fish[] { TUNA, SWORDFISH }, new int[] { 618, 10616, 5108 }, NORMAL_HARPOON, CLAY_HARPOON, BARB_TAIL_HARPOON),
        CRAYFISH_CAGE = new FishingStyle("Crayfishing", "Cage", new Fish[] { CRAYFISH }, new int[] {10009}, CRAY_CAGE),
        SHARK_HARPOONING = new FishingStyle("Shark fishing", "Harpoon", "Net", new Fish[]{SHARK}, new int[] {618, 10616, 5108}, NORMAL_HARPOON, CLAY_HARPOON, BARB_TAIL_HARPOON),
        HAND = new FishingStyle("Handfishing (Swordies/Tuna)", "Harpoon", "Cage", new Fish[] { TUNA, SWORDFISH }, new int[] { 14723, 6707, 6710, 6708, 6711, 9980 }),
        HAND_SHARK = new FishingStyle("Handfishing (Sharks)", "Harpoon", "Net", new Fish[]{SHARK}, new int[] {9980,6705,6706,14723}),
        MONK_NETTING = new FishingStyle("Monk Netting", "Net", "Harpoon", new Fish[]{MONKFISH}, new int[]{621}, SMALL_NET);



    private final InventoryHandler DROPPER = new InventoryHandler();
    private final FishingArea[] AREAS = {
         new FishingArea(2598, 3419, 2605, 3426,
            "Fishing Guild",
            new Line[] {
                new Line(2600, 3423, 2602, 3420),
                new Line(2598, 3421, 2598, 3420),
                new Line(2594, 3421, 2594, 3420),
                new Line(2590, 3424, 2590, 3420),
                new Line(2585, 3423, 2585, 3421)
            }, new InventoryHandler(InventoryHandler.BOOTH, 49018),
            new SpotInfo(CAGE, 312),
            new SpotInfo(HARPOON, 312),
            new SpotInfo(SHARK_HARPOONING, 313),
            new SpotInfo(HAND, 312),
            new SpotInfo(HAND_SHARK, 313)),
        new FishingArea(2922, 3176, 2927, 3182, 29,
                "Karamja (Stiles)",
            new Line[] {
                new Line(2924, 3180, 2925, 3180),
                new Line(2924, 3175, 2925, 3172),
                new Line(2921, 3175, 2921, 3172),
                new Line(2915, 3176, 2913, 3169),
                new Line(2909, 3173, 2908, 3171),
                new Line(2897, 3170, 2901, 3168),
                new Line(2886, 3165, 2893, 3162),
                new Line(2875, 3158, 2880, 3151),
                new Line(2866, 3151, 2865, 3147),
                new Line(2852, 3144, 2851, 3141)
            }, new InventoryHandler(InventoryHandler.NOTER, 11267),
            new SpotInfo(CAGE, 324),
            new SpotInfo(HARPOON, 324),
            new SpotInfo(HAND, 324)),
        new FishingArea(2922, 3176, 2927, 3182,
                "Karamja (Stiles - Safe Path)",
            new Line[] {
                new Line(2925, 3180, 2924, 3180),
                new Line(2925, 3173, 2924, 3174),
                new Line(2918, 3172, 2918, 3178),
                new Line(2912, 3170, 2912, 3173),
                new Line(2906, 3167, 2897, 3170),
                new Line(2900, 3153, 2895, 3157),
                new Line(2891, 3143, 2889, 3148),
                new Line(2878, 3139, 2880, 3147),
                new Line(2868, 3145, 2869, 3149),
                new Line(2859, 3144, 2859, 3148),
                new Line(2851, 3141, 2853, 3144)
            }, new InventoryHandler(InventoryHandler.NOTER, 11267),
            new SpotInfo(CAGE, 324),
            new SpotInfo(HARPOON, 324),
            new SpotInfo(HAND, 324)),
        new FishingArea(3238, 3241, 3239, 3256,
             "Goblin Hut - Lumbridge",
            new SpotInfo(FLY, 329),
            new SpotInfo(PIKE_FISHING, 329)),
        new FishingArea(3238, 3146, 3248, 3158,
            "Lumbridge Swamps",
            new SpotInfo(NET, 4908),
            new SpotInfo(ROD, 4908)),
        new FishingArea(3257, 3203, 3260, 3207,
            "Lumbridge Church Crayfishing",
            new SpotInfo(CRAYFISH_CAGE, 6267)),
        new FishingArea(3084, 3226, 3088, 3233,
            "Draynor Village",
            new Line[] {
                new Line(3087, 3229, 3089, 3229),
                new Line(3084, 3234, 3089, 3233),
                new Line(3085, 3240, 3087, 3240),
                new Line(3085, 3247, 3087, 3246),
                new Line(3091, 3250, 3091, 3247),
                new Line(3094, 3247, 3091, 3247),
                new Line(3094, 3245, 3092, 3245)
            }, new InventoryHandler(InventoryHandler.BOOTH, 2213),
            new SpotInfo(NET, 327),
            new SpotInfo(ROD, 327)),
         new FishingArea(3102, 3423, 3110, 3434,
            "Edgeville",
            new Line[] {
                new Line(3105, 3435, 3105, 3431),
                new Line(3105, 3435, 3099, 3434),
                new Line(3098, 3441, 3093, 3439),
                new Line(3092, 3445, 3089, 3446),
                new Line(3088, 3462, 3086, 3462),
                new Line(3085, 3467, 3085, 3464),
                new Line(3081, 3467, 3081, 3465),
                new Line(3080, 3468, 3079, 3468),
                new Line(3082, 3478, 3079, 3478),
                new Line(3081, 3480, 3077, 3480),
                new Line(3084, 3483, 3081, 3485),
                new Line(3090, 3489, 3090, 3492),
                new Line(3094, 3489, 3094, 3492)
            }, new InventoryHandler(InventoryHandler.BOOTH, 26972),
            new SpotInfo(FLY, 328),
            new SpotInfo(PIKE_FISHING, 328)),
         new FishingArea(2835, 3433, 2856, 3433,
            "Catherby",
            new Line[] {
                new Line(2840, 3432, 2834, 3433),
                new Line(2831, 3439, 2831, 3435),
                new Line(2821, 3439, 2821, 3435),
                new Line(2818, 3438, 2818, 3435),
                new Line(2812, 3436, 2811, 3433),
                new Line(2810, 3438, 2807, 3438),
                new Line(2811, 3441, 2807, 3441)
            }, new InventoryHandler(InventoryHandler.BOOTH, 2213),
            new SpotInfo(NET, 320),
            new SpotInfo(ROD, 320),
            new SpotInfo(CAGE, 321),
            new SpotInfo(HARPOON, 321),
            new SpotInfo(SHARK_HARPOONING, 322),
            new SpotInfo(HAND, 321),
            new SpotInfo(HAND_SHARK, 322)),
        new FishingArea(2331, 3701, 2353, 3703,
            "Piscatoris Fishing Colony",
            new Line[] {
                new Line(2340, 3701, 2347, 3701),
                new Line(2335, 3698, 2342, 3691),
                new Line(2333, 3690, 2333, 3687),
                new Line(2330, 3691, 2328, 3689)
            }, new InventoryHandler(InventoryHandler.BANKER, 3824),
            new SpotInfo(MONK_NETTING, 3848))
    };

    private class SpotInfo {
        private int id;
        private FishingStyle style;
        
        public SpotInfo(FishingStyle style, int id) {
            this.style = style;
            this.id = id;
        }
        
        public RSNPC getNearest() {
            return npcs.getNearest(id);
        }

        public RSNPC getSpot(FishingArea area, FishingStyle style) {
            RSNPC spot = getNearestOccupiedInArea(area, style);
            if(spot != null)
                return spot;
            spot = getOnScreenInArea(area, style);
            if(spot != null)
                return spot;
            return getNearest();
        }

        public RSNPC getNearestOccupiedInArea(RSArea area, FishingStyle style) {
            RSPlayer[] plays = players.getAll();
            ArrayList<RSNPC> npcList = new ArrayList<RSNPC>();
            RSCharacter interacting;
            int shortestDist = Integer.MAX_VALUE, dist;
            RSNPC cast;
            for(RSPlayer p : plays) {
                if(p == null)
                    continue;
                try {
                    interacting = p.getInteracting();
                } catch(NullPointerException e) {
                    continue;
                }
                if(interacting instanceof RSNPC) {
                    cast = (RSNPC)interacting;
                    if(!style.fishingSpotIsTarget(cast, this) || !area.contains(cast.getLocation()) || !cast.isOnScreen()
                            || npcList.contains(cast))
                        continue;
                    dist = calc.distanceTo(cast);
                    if(dist < shortestDist) {
                        npcList.clear();
                        npcList.add(cast);
                        shortestDist = dist;
                    } else if(dist == shortestDist)
                        npcList.add(cast);
                }
            }
            if(npcList.size() == 0)
                return null;
            return npcList.get(random(0, npcList.size()));
        }

        public RSNPC getOnScreenInArea(RSArea area, FishingStyle style) {
            ArrayList<RSNPC> available = new ArrayList<RSNPC>();
            int shortestDist = Integer.MAX_VALUE, dist;
            for(RSNPC npc : npcs.getAll()) {
                /*try {
                log("isNull: " + (npc==null));
                log("isTarget: " + (style.fishingSpotIsTarget(npc)));
                log("isInArea: " + (area.contains(npc.getLocation())));
                log("isOnScreen: " + (npc.isOnScreen()));
                log("ID: " + npc.getID());
                } catch(NullPointerException e) {}*/
                if(npc == null || (!style.fishingSpotIsTarget(npc, this) ||
                        !area.contains(npc.getLocation()) || !npc.isOnScreen()))
                    continue;
                dist = calc.distanceTo(npc);
                if(dist == shortestDist)
                    available.add(npc);
                else if(dist < shortestDist) {
                    available.clear();
                    available.add(npc);
                    shortestDist = dist;
                }
            }
            if(available.size() == 0)
                return null;
            return available.get(random(0, available.size()));
        }

        public FishingStyle getStyle() { return style; }
        public int getID() { return id; }
    }

    private class InventoryHandler {
        public static final int DROPPER = 0, BOOTH = 1, NOTER = 2, BANKER = 3;
        private int id, type;
        public InventoryHandler(int type, int id) {
            this.id = id;
            this.type = type;
        }
        public InventoryHandler() {
            type = 0;
            id = -1;
        }
        public int getID() { return id; }
        public int getType() { return type; }
    }

    private class FishingArea extends RSArea {
        private Line[] guides, reverse;
        private SpotInfo[] spots;
        private String name;
        private InventoryHandler handler;
        private int requiredCombatLevelForSafety;

        public FishingArea(int w, int s, int e, int n, String name,
                SpotInfo ... spots) {
            super(new RSTile(w, s), new RSTile(e, n));
            this.name = name;
            this.spots = spots;
            this.handler = DROPPER;
        }

        public FishingArea(int w, int s, int e, int n, String name, Line[] guides,
                InventoryHandler handler, SpotInfo ... spots) {
            super(new RSTile(w, s), new RSTile(e, n));
            this.name = name;
            this.spots = spots;
            this.handler = handler;
            this.guides = guides;
            reverse = reverse(guides);
        }

        public FishingArea(int w, int s, int e, int n, int levelRequired, String name, Line[] guides,
                InventoryHandler handler, SpotInfo ... spots) {
            super(new RSTile(w, s), new RSTile(e, n));
            this.name = name;
            this.spots = spots;
            this.handler = handler;
            this.guides = guides;
            requiredCombatLevelForSafety = levelRequired;
            reverse = reverse(guides);
        }
        
        public SpotInfo get(int index) { return spots[index]; }
        public FishingStyle[] getStyles() {
            FishingStyle[] styles = new FishingStyle[spots.length];
            for(int i = 0; i < styles.length; i++)
                styles[i] = spots[i].getStyle();
            return styles;
        }
        public int predictStyle() {
            for(int i = 0; i < spots.length; i++)
                if(spots[i].getStyle().playerHasGear())
                    return i;
            return 0;
        }
        public String toString() { return name; }
        public String getName() { return name; }
        public Line[] getGuides() { return guides; }
        public Line[] getReversedGuides() { return reverse; }
        public InventoryHandler getHandler() { return handler; }
    }
    //</editor-fold>
    public Point getRandomPoint(RSModel m) {
        Polygon[] triangles = m.getTriangles();
        if(triangles.length == 0)
            return null;
        Polygon p = triangles[random(0, triangles.length)];
        double a = Math.random();
        double b = Math.random();
        if(a + b >= 1) {
            a = 1 - a;
            b = 1 - b;
        }
        double c = 1 - a - b;
        return new Point(
                (int)(a*p.xpoints[0] + b*p.xpoints[1] + c*p.xpoints[2]),
                (int)(a*p.ypoints[0] + b*p.ypoints[1] + c*p.ypoints[2]));
    }
    private final char[] UDkeycodes = {KeyEvent.VK_UP, KeyEvent.VK_DOWN};
    private final char[] LRkeycodes = {KeyEvent.VK_LEFT, KeyEvent.VK_RIGHT};
    
    private class CameraInput implements Runnable {
        private int priorWait, holdWait;
        private char key;
        public CameraInput(char key, int priorWait, int holdWait) {
            this.key = key;
            this.priorWait = priorWait;
            this.holdWait = holdWait;
        }
        public void run() {
            try {
                Thread.sleep(priorWait);
                keyboard.pressKey(key);
                Thread.sleep(holdWait);
                keyboard.releaseKey(key);
            } catch(InterruptedException e) {}
        }
    }
    private class CameraSpinToSpotThread implements Runnable {
        public void run() {
            int angleToTile = (camera.getTileAngle(fishingSpotTile) + 180) % 360;
            if(Math.abs(camera.getAngle() - angleToTile) > 90)
                camera.setAngle(angleToTile + random(-45, 46, 0, 45));
        }
    }
    private class AntibanTimer extends Timer implements Runnable {
        protected int lo, hi, sleep;
        private String desc, paintDesc;
        private boolean enabled;
        public AntibanTimer(String desc, String paintDesc, int lo, int hi) {
            super(random(lo, hi));
            this.lo = lo;
            this.hi = hi;
            this.desc = desc;
            this.paintDesc = paintDesc;
            enabled = true;
        }
        public int resetIn(int waitLo, int waitHi) {
            sleep = random(waitLo, waitHi);
            new Thread(this).start();
            return sleep;
        }
        public void run() {
            try { Thread.sleep(sleep); } catch(InterruptedException e) {}
            setDuration(random(lo, hi));
            reset();
        }
        public void setEnabled(boolean b) { enabled = b; }
        public boolean isUp() { return super.isUp() && enabled; }
        public String toString() { return desc; }
        public String getPaintDescription() { return paintDesc; }
    }

    private class AntibanGTimer extends AntibanTimer {
        private int mean, sd;
        public AntibanGTimer(String desc, String paintDesc, int lo, int hi, int mean, int sd) {
            super(desc, paintDesc, lo, hi);
            this.mean = mean;
            this.sd = sd;
        }
        public void run() {
            try { Thread.sleep(sleep); } catch(InterruptedException e) {}
            setDuration(random(lo, hi, mean, sd));
            reset();
        }
    }

    private int gaussianRandom(int mean, int sd) { return gaussianRandom(r, mean, sd); }
    
    private int gaussianRandom(java.util.Random r, int mean, int sd) {
        return (int)(r.nextGaussian()*sd + mean);
    }

    private int random(int lo, int hi, int mean, int sd) {
        int rand;
        do {
            rand = (int)(r.nextGaussian()*sd + mean);
        } while(rand < lo || rand >= hi);
        return rand;
    }

    private void moveMouseRandomly(int maxDistance) {
        Point p = mouse.getLocation();
        int x = random(Math.max(0, p.x - maxDistance), Math.min(p.x + maxDistance, game.getWidth()));
        int y = random(Math.max(0, p.y - maxDistance), Math.min(p.y + maxDistance, game.getHeight()));
        mouse.move(x, y);
    }

    private Object[][] getAntibans() {
        Object[][] list = new Object[antibans.length][];
        for(int i = 0; i < antibans.length; i++)
            list[i] = new Object[] { antibans[i], true };
        return list;
    }

    private int getClosestArea() {
        int shortest = -1, shortestDist = Integer.MAX_VALUE, dist;
        RSTile t = getMyPlayer().getLocation();
        int cbLevel = getMyPlayer().getCombatLevel();
        for(int i = 0; i < AREAS.length; i++) {
            dist = calc.distanceTo(AREAS[i].getNearestTile(t));
            if(shortestDist > dist && cbLevel >= AREAS[i].requiredCombatLevelForSafety) {
                shortestDist = dist;
                shortest = i;
            }
        }
        return shortest;
    }

    private class GFFrame extends javax.swing.JFrame {

        private boolean hitStart;
        private final Color disabledBG = new Color(153, 153, 153),
                disabled = Color.WHITE;
        private Color defaultColor, defaultBGColor;

        public GFFrame() {
            initComponents();
            defaultColor = fishTable.getForeground();
            defaultBGColor = fishTable.getBackground();
            int closest = getClosestArea();
            int predictedStyle = AREAS[closest].predictStyle();
            fishTableModel = ((javax.swing.table.DefaultTableModel)fishTable.getModel());
            fishTable.setEnabled(AREAS[closest].getHandler().getType() != InventoryHandler.DROPPER);
            Object[][] rows = AREAS[closest].get(predictedStyle).getStyle().getFishForGUI();
            for(int i = 0; i < rows.length; i++) {
                if(!fishTable.isEnabled())
                    rows[i][1] = false;
                fishTableModel.addRow(rows[i]);
            }
            setColors();
            locationDropdown.setSelectedIndex(closest);
            styleDropdown.setSelectedIndex(predictedStyle);
        }

        private void setColors() {
            fishTable.setForeground(fishTable.isEnabled() ? defaultColor:disabled);
            fishTable.setBackground(fishTable.isEnabled() ? defaultBGColor:disabledBG);
        }

        @SuppressWarnings("unchecked")
        // <editor-fold defaultstate="collapsed" desc="Generated Code">
        private void initComponents() {
            setTitle("Godless Fisher");

            locationDropdown = new javax.swing.JComboBox();
            jLabel1 = new javax.swing.JLabel();
            jLabel2 = new javax.swing.JLabel();
            styleDropdown = new javax.swing.JComboBox();
            jScrollPane1 = new javax.swing.JScrollPane();
            fishTable = new javax.swing.JTable();
            jLabel3 = new javax.swing.JLabel();
            jScrollPane2 = new javax.swing.JScrollPane();
            antibanTable = new javax.swing.JTable();
            startUpdateButton = new javax.swing.JButton();
            jLabel4 = new javax.swing.JLabel();
            paintCheckbox = new javax.swing.JCheckBox();
            mousekeysCheckbox = new javax.swing.JCheckBox();
            updateButton = new javax.swing.JButton();

            setResizable(false);
            locationDropdown.setModel(new javax.swing.DefaultComboBoxModel(AREAS));
            locationDropdown.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    locationDropdownActionPerformed(evt);
                }
            });

            jLabel1.setText("Location:");

            jLabel2.setText("Style:");

            styleDropdown.setModel(new javax.swing.DefaultComboBoxModel(AREAS[0].getStyles()));
            styleDropdown.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    styleDropdownActionPerformed(evt);
                }
            });

            fishTable.getTableHeader().setReorderingAllowed(false);
            fishTable.setModel(new javax.swing.table.DefaultTableModel(
                new Object [][] {

                },
                new String [] {
                    "Fish", "Keep"
                }
            ) {
                Class[] types = new Class [] {
                    java.lang.String.class, java.lang.Boolean.class
                };
                boolean[] canEdit = new boolean [] {
                    false, true
                };

                public Class getColumnClass(int columnIndex) {
                    return types [columnIndex];
                }

                public boolean isCellEditable(int rowIndex, int columnIndex) {
                    return canEdit [columnIndex];
                }
            });
            jScrollPane1.setViewportView(fishTable);
            fishTable.getColumnModel().getColumn(0).setResizable(false);
            fishTable.getColumnModel().getColumn(0).setPreferredWidth(150);
            fishTable.getColumnModel().getColumn(1).setResizable(false);

            jLabel3.setText("Antiban Behaviors:");

            antibanTable.getTableHeader().setReorderingAllowed(false);
            antibanTable.setModel(new javax.swing.table.DefaultTableModel(
                getAntibans(),
                new String [] {
                    "Behavior", "Enabled"
                }
            ) {
                Class[] types = new Class [] {
                    java.lang.Object.class, java.lang.Boolean.class
                };
                boolean[] canEdit = new boolean [] {
                    false, true
                };

                public Class getColumnClass(int columnIndex) {
                    return types [columnIndex];
                }

                public boolean isCellEditable(int rowIndex, int columnIndex) {
                    return canEdit [columnIndex];
                }
            });
            jScrollPane2.setViewportView(antibanTable);
            antibanTable.getColumnModel().getColumn(0).setResizable(false);
            antibanTable.getColumnModel().getColumn(0).setPreferredWidth(150);
            antibanTable.getColumnModel().getColumn(1).setResizable(false);

            startUpdateButton.setText("Start");
            startUpdateButton.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    startUpdateButtonActionPerformed(evt);
                }
            });

            jLabel4.setForeground(new java.awt.Color(255, 0, 51));
            jLabel4.setText("Deselect all to powerfish");

            paintCheckbox.setSelected(true);
            paintCheckbox.setText("Show Paint");

            mousekeysCheckbox.setText("Mouse Key (fast) dropping");

            updateButton.setText("Check for newer version");
            updateButton.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    updateButtonActionPerformed(evt);
                }
            });

            javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
            getContentPane().setLayout(layout);
            layout.setHorizontalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(layout.createSequentialGroup()
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(layout.createSequentialGroup()
                            .addContainerGap()
                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addGroup(layout.createSequentialGroup()
                                    .addGap(10, 10, 10)
                                    .addComponent(locationDropdown, 0, 175, Short.MAX_VALUE))
                                .addComponent(jLabel1)
                                .addComponent(jLabel2)))
                        .addGroup(layout.createSequentialGroup()
                            .addGap(20, 20, 20)
                            .addComponent(styleDropdown, 0, 175, Short.MAX_VALUE)))
                    .addContainerGap())
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 205, Short.MAX_VALUE)
                .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                    .addGap(10, 10, 10)
                    .addComponent(jLabel3)
                    .addContainerGap(104, Short.MAX_VALUE))
                .addComponent(jScrollPane2, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 205, Short.MAX_VALUE)
                .addGroup(layout.createSequentialGroup()
                    .addContainerGap()
                    .addComponent(paintCheckbox)
                    .addContainerGap(102, Short.MAX_VALUE))
                .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                    .addContainerGap(78, Short.MAX_VALUE)
                    .addComponent(jLabel4)
                    .addContainerGap())
                .addGroup(layout.createSequentialGroup()
                    .addContainerGap()
                    .addComponent(mousekeysCheckbox)
                    .addContainerGap(46, Short.MAX_VALUE))
                .addGroup(layout.createSequentialGroup()
                    .addContainerGap()
                    .addComponent(updateButton, javax.swing.GroupLayout.DEFAULT_SIZE, 185, Short.MAX_VALUE)
                    .addContainerGap())
                .addGroup(layout.createSequentialGroup()
                    .addContainerGap()
                    .addComponent(startUpdateButton, javax.swing.GroupLayout.DEFAULT_SIZE, 185, Short.MAX_VALUE)
                    .addContainerGap())
            );
            layout.setVerticalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(layout.createSequentialGroup()
                    .addComponent(updateButton)
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                    .addComponent(jLabel1)
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                    .addComponent(locationDropdown, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                    .addComponent(jLabel2)
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                    .addComponent(styleDropdown, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 92, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                    .addComponent(jLabel4)
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                    .addComponent(jLabel3)
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                    .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 89, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                    .addComponent(paintCheckbox)
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                    .addComponent(mousekeysCheckbox)
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                    .addComponent(startUpdateButton)
                    .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            );

            pack();
        }// </editor-fold>

        private void locationDropdownActionPerformed(java.awt.event.ActionEvent evt) {
            int index = locationDropdown.getSelectedIndex();
            styleDropdown.setModel(new javax.swing.DefaultComboBoxModel(AREAS[index].getStyles()));
            fishTableModel.setRowCount(0);
            fishTable.setEnabled(AREAS[index].getHandler().getType() != InventoryHandler.DROPPER);
            Object[][] rows = AREAS[index].get(0).getStyle().getFishForGUI();
            setColors();
            for(int i = 0; i < rows.length; i++) {
                if(!fishTable.isEnabled())
                    rows[i][1] = false;
                fishTableModel.addRow(rows[i]);
            }
        }

        private void styleDropdownActionPerformed(java.awt.event.ActionEvent evt) {
            int index = locationDropdown.getSelectedIndex();
            int styleIndex = styleDropdown.getSelectedIndex();
            fishTableModel.setRowCount(0);
            Object[][] rows = AREAS[index].get(styleIndex).getStyle().getFishForGUI();
            setColors();
            for(int i = 0; i < rows.length; i++) {
                if(!fishTable.isEnabled())
                    rows[i][1] = false;
                fishTableModel.addRow(rows[i]);
            }
        }

        private final Pattern p = Pattern.compile("version\\s*=\\s*([0-9.]+)");

        private void updateButtonActionPerformed(java.awt.event.ActionEvent evt) {
            try {
                URL url = new URL("http://0x098b4a40.webs.com/GodlessFisher.java");
                BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));
                double currentVer = GodlessFisher.class.getAnnotation(ScriptManifest.class).version();
                double newVer = -1;
                String line, lines = "";
                Matcher m;
                while((line = in.readLine()) != null) {
                    lines += line + "\n";
                    if((m = p.matcher(line)).find()) {
                        newVer = Double.parseDouble(m.group(1));
                        break;
                    }
                }
                if(newVer < 0) {
                    in.close();
                    log("Unable to find the new version number. Update failed");
                    return;
                }
                if(currentVer == newVer) {
                    in.close();
                    log("You already have the latest version of the script.");
                    return;
                }
                log("Update found! Downloading version " + newVer);
                PrintWriter out = new PrintWriter(GlobalConfiguration.Paths.getScriptsSourcesDirectory()+"\\GodlessFisher.java");
                out.print(lines);
                while((line = in.readLine()) != null)
                    out.println(line);
                out.close();
                in.close();
                log("Successfully saved GodlessFisher.java to " + GlobalConfiguration.Paths.getScriptsSourcesDirectory());
                log("Compiling...");
                try {
                    Runtime.getRuntime().exec(new String[] {
                        "javac",
                        "-classpath",
                        "RSBot.jar",
                        GlobalConfiguration.Paths.getScriptsSourcesDirectory()+"\\GodlessFisher.java"
                    });
                } catch (Exception ex) {
                    log("Could not compile the script. Please manually compile to finish the update.");
                    return;
                }
                log("Update successful!");
                log("The new version will appear near the bottom of the script selector.");
                log("Stop and restart the script to run the newer version.");
            } catch(IOException e) {
                log(e.toString());
                log("Update failed.");
            }
        }

        private void startUpdateButtonActionPerformed(java.awt.event.ActionEvent evt) {
            if(!hitStart) {
                startingXP = skills.getCurrentExp(Skills.FISHING);
                startingLevel = skills.getCurrentLevel(Skills.FISHING);
                startTime = System.currentTimeMillis();
                oldInventory = inventory.getItems();
                state = State.LOOKING;
                unique = generateSeededRandom(getMyPlayer().getName());
                energyThreshold = unique.nextInt(30)+60;
            }
            failsafe = System.currentTimeMillis() + 600000;
            setVisible(false);
            startUpdateButton.setText("Update");
            hitStart = true;
            int index = locationDropdown.getSelectedIndex();
            int styleIndex = styleDropdown.getSelectedIndex();
            area = AREAS[index];
            spots = area.get(styleIndex);
            style = spots.getStyle();
            gear = style.getGear();
            handler = area.getHandler();
            mousekeys = mousekeysCheckbox.isSelected();
            for(int i = 0; i < style.getFish().length; i++)
                style.getFish()[i].setKeep((Boolean)fishTable.getValueAt(i, 1));
            boolean powerfish = true;
            for(Fish f : style.getFish())
                if(f.shouldKeep()) {
                    powerfish = false;
                    break;
                }
            if(powerfish)
                handler = DROPPER;
            for(int i = 0; i < antibans.length; i++)
                antibans[i].setEnabled((Boolean)antibanTable.getValueAt(i, 1));
            if(!style.playerHasGear())
                log("---WARNING--- You do not have the required gear! ---WARNING---");
            log("Hit F12 at any time to show the GUI again.");
        }

        public boolean shouldPaint() {
            return paintCheckbox.isSelected();
        }

        private javax.swing.table.DefaultTableModel fishTableModel;
        private javax.swing.JTable antibanTable;
        private javax.swing.JTable fishTable;
        private javax.swing.JLabel jLabel1;
        private javax.swing.JLabel jLabel2;
        private javax.swing.JLabel jLabel3;
        private javax.swing.JLabel jLabel4;
        private javax.swing.JScrollPane jScrollPane1;
        private javax.swing.JScrollPane jScrollPane2;
        private javax.swing.JComboBox locationDropdown;
        private javax.swing.JCheckBox paintCheckbox;
        private javax.swing.JCheckBox mousekeysCheckbox;
        private javax.swing.JButton startUpdateButton;
        private javax.swing.JComboBox styleDropdown;
        private javax.swing.JButton updateButton;

    }

    public class GELookupThread implements Runnable {
         private int min, market, max, itemID;

         public GELookupThread(int id) {
              itemID= id;
              min = -1;
              market = -1;
              max = -1;
              new Thread(this).start();
         }

         public void run() {
              GEItemInfo info = grandExchange.loadItemInfo(itemID);
              min = info.getMinPrice();
              market = info.getMarketPrice();
              max = info.getMaxPrice();
         }

         public int getMin() { return min; }
         public int getMarket() { return market; }
         public int getMax() { return max; }

    }

    private class Timer {

        private long endTime, time;

        public Timer(long time) {
            this.time = time;
            endTime = System.currentTimeMillis() + time;
        }

        public boolean isUp() {
            return endTime < System.currentTimeMillis();
        }

        public void reset() {
            endTime = System.currentTimeMillis() + time;
        }

        public void setDuration(long duration) {
            time = duration;
        }

    }

    private enum State {
        LOOKING("Looking for a spot"), FISHING("Fishing"), WALKING_PATH("Walking Path"),
        HANDLING_INVENTORY("Handling Inventory"), ANTIBAN("Antiban"), MOVING("Waiting to stop moving");
        private String desc;
        State(String desc) { this.desc = desc; }
        public String toString() { return desc; }
    }

}