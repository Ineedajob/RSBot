
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.GeneralPath;
import java.awt.image.BufferedImage;

import java.io.*;
import java.util.*;
import java.net.*;
import java.lang.reflect.InvocationTargetException;

import javax.swing.*;
import javax.imageio.ImageIO;

import org.rsbot.script.Script;
import org.rsbot.script.ScriptManifest;
import org.rsbot.script.util.Filter;
import org.rsbot.script.internal.MouseHandler;
import org.rsbot.script.methods.Bank;
import org.rsbot.script.methods.GrandExchange.GEItem;
import org.rsbot.script.wrappers.*;
import org.rsbot.event.listeners.*;
import org.rsbot.event.events.*;
import org.rsbot.util.GlobalConfiguration;

@ScriptManifest(authors = "icnhzabot, Aut0r", keywords = "Clay, Softener", name = "Aut0ClaySoftener", version = 1.0, description = "Softens clay at multiple locations!")
public class Aut0ClaySoftener extends Script implements PaintListener, MessageListener {

    private GUI gui = null;
    private InventoryListener inv;
    private Thread invThread;
    private Locations loc;
    private boolean running;
    private int failCounter;
    private int numDone;
    private int softClay = 1761, clay = 434;
    private int softClayPrice = -1, clayPrice = -1;
    private Timekeeper timekeeper = new Timekeeper();
    private antiban Antiban;
    private CameraMovementChecker cameraCheck;
    private MouseMovementChecker mouseCheck;
    private Camera cameraHandler = new Camera();
    private splines alternateSplineGen = new splines();
    private boolean canAB = false;
    private long mousemovet = System.currentTimeMillis();
    private Walking walker = new Walking();
    private RSTile[] drawPath = null;
    private SettingsManager sm = new SettingsManager(GlobalConfiguration.Paths.getSettingsDirectory() + File.separator + "Aut0ClaySoftener.SETTINGS");
    BufferedImage nmouse = null;
    BufferedImage nclicked = null;

    /*GUI Options*/
    private boolean clicks = true;
    private boolean defmouse = true;
    private boolean rotmouse = true;
    private boolean showmouse = true;
    private boolean paths = false;
    private boolean stats = true;
    private boolean beziers = true;
    private boolean shortsplines = true;
    private int numABThreads = 2;
    private int mouseLo = 5;
    private int mouseHi = 10;

    @Override
    public void onFinish() {
        running = false;
        inv.kill();
        Antiban.kill();
        log("Thank you for using Aut0ClaySoftener!");
        running = false;
    }

    @Override
    public boolean onStart() {
        log("Welcome to Aut0ClaySoftener. Starting GUI and loading prices...");
        sleep(100);
        if (!game.isLoggedIn() || game.isLoginScreen() || game.isWelcomeScreen()) {
            env.enableRandom("Login");
        }
        Thread priceloader = new Thread(new PriceLoader());
        priceloader.start();
        createAndWaitforGUI();
        sleep(75);
        if (gui.isCanceled() == true) {
            return false;
        }
        final int pp = (int) gui.softenLocation();
        switch (pp) {
            case locVar.fally:
                loc = new Falador();
                break;
            case locVar.edge:
                loc = new Edgeville();
                break;
            default:
                return false;
        }
        running = true;
        inv = new InventoryListener();
        invThread = new Thread(inv);
        invThread.start();
        while (priceloader.isAlive()) {
            sleep(100);
        }
        if (softClayPrice == -1 || clayPrice == -1) {
            log.severe("Unable to load G.E. item info. Please try again.");
            return false;
        } else {
            if (nclicked == null || nmouse == null) {
                log.severe("Unable to load images. Please try again. If this keeps happening,");
                log.severe("please PM icnhzabot.");
                return false;
            }
            log("Done. Soft clay is " + softClayPrice + " GP. Clay is " + clayPrice + " GP.");
        }
        mouse.setSpeed(random(mouseLo, mouseHi));
        mouseCheck = new MouseMovementChecker();
        cameraCheck = new CameraMovementChecker();
        Antiban = new antiban();
        canAB = true;
        System.currentTimeMillis();
        return true;
    }
    private final RenderingHints antialiasing = new RenderingHints(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    private final BasicStroke stroke = new BasicStroke(1);
    private final Font calibri20 = new Font("Calibri", 0, 20);
    private final Color color1 = new Color(0, 0, 0);
    private final Color color2 = new Color(0, 0, 255, 108);
    private final Color blue = Color.BLUE;
    private final Color cyan = Color.CYAN;
    private final Color darkCyan = Color.CYAN.darker();

    private void drawPath(Graphics2D g) {
        g.setColor(Color.GREEN.brighter());
        try {
            RSTile[] path = drawPath;
            if (path != null) {
                Point last = tileToMap(path[0]);
                for (RSTile curr : path) {
                    Point onmap = tileToMap(curr);
                    ((Graphics2D) g).drawLine(last.x, last.y, onmap.x, onmap.y);
                    last = onmap;
                }
            }
        } catch (Exception e) {
        }
    }

    @Override
    public void onRepaint(Graphics g1) {
        Graphics2D g = (Graphics2D) g1;
        if (isPaused() || !isActive() || !isRunning() || !game.isLoggedIn() || game.isLoginScreen() || game.isWelcomeScreen()) {
            if (timekeeper.getState() == 0) {
                timekeeper.setPaused();
            }
        } else {
            if (timekeeper.getState() == 1) {
                timekeeper.setResumed();
            }
        }
        long totalProfit = (numDone * softClayPrice) - (numDone * clayPrice);
        g.setRenderingHints(antialiasing);
        if (stats) {
            g.setStroke(stroke);
            g.setColor(color1);
            g.drawOval(5, 135, 280, 205);
            g.setColor(color2);
            g.fillOval(5, 135, 280, 205);
        }
        if (clicks && showmouse) {
            long time = (System.currentTimeMillis() - mouse.getPressTime());
            if (defmouse) {
                if (time <= 1200) {
                    g.setColor(Color.RED);
                    Point clicked = mouse.getPressLocation();
                    g.drawLine(clicked.x, clicked.y, clicked.x + 5, clicked.y);
                    g.drawLine(clicked.x, clicked.y, clicked.x - 5, clicked.y);
                    g.drawLine(clicked.x, clicked.y, clicked.x, clicked.y + 5);
                    g.drawLine(clicked.x, clicked.y, clicked.x, clicked.y - 5);
                }
            } else {
                if (time <= 1200) {
                    Point clicked = mouse.getPressLocation();
                    g.drawImage(nclicked, clicked.x - 6, clicked.y - 6, null);
                }
            }
        }
        g.setColor((System.currentTimeMillis() - mouse.getPressTime()) > 175 ? cyan : Color.RED.darker());
        Point loc = mouse.getLocation();
        if (showmouse) {
            if (defmouse) {
                int midUp = (int) (game.getHeight() / 2);
                int midSide = (int) (game.getWidth() / 2);
                Point midLeft = new Point(0, midUp);
                Point midRight = new Point(game.getWidth(), midUp);
                Point midUpp = new Point(midSide, game.getHeight());
                Point midDown = new Point(midSide, 0);
                GeneralPath mouseDrawer = new GeneralPath();
                mouseDrawer.moveTo(loc.x, loc.y);
                Point pointBetween = pointInMiddle(loc, midLeft);
                mouseDrawer.lineTo(pointBetween.x, pointBetween.y);
                mouseDrawer.moveTo(loc.x, loc.y);
                pointBetween = pointInMiddle(loc, midRight);
                mouseDrawer.lineTo(pointBetween.x, pointBetween.y);
                mouseDrawer.moveTo(loc.x, loc.y);
                pointBetween = pointInMiddle(loc, midUpp);
                mouseDrawer.lineTo(pointBetween.x, pointBetween.y);
                mouseDrawer.moveTo(loc.x, loc.y);
                pointBetween = pointInMiddle(loc, midDown);
                mouseDrawer.lineTo(pointBetween.x, pointBetween.y);
                g.draw(mouseDrawer);
            }
        }
        if (stats) {
            g.setColor(Color.WHITE);
            g.setFont(calibri20);
            g.drawString("Runtime: " + (timekeeper.getState() == 0 ? timekeeper.getRuntimeString() : "Paused"), 30, 195);
            g.drawString("Clay Softened: " + numDone, 15, 220);
            g.drawString("Soft Clay / Hour: " + timekeeper.calcPerHour(numDone), 10, 245);
            g.drawString("Total Profit: " + totalProfit, 20, 270);
            g.drawString("Profit / Hour: " + timekeeper.calcPerHour(totalProfit), 30, 295);
        }
        if (paths) {
            drawPath(g);
        }
        if (showmouse) {
            if (defmouse) {
                loc = mouse.getLocation();
                g.setColor((System.currentTimeMillis() - mouse.getPressTime()) > 175 ? color2 : Color.RED);
                g.drawOval(loc.x - 9, loc.y - 9, 18, 18);
                g.setColor((System.currentTimeMillis() - mouse.getPressTime()) > 175 ? blue : Color.RED);
                if (rotmouse) {
                    g.rotate(Math.toRadians(getRotate(5)), loc.x, loc.y);
                }
                loc = mouse.getLocation();
                g.drawRect(loc.x - 6, loc.y - 6, 12, 12);
                g.setColor((System.currentTimeMillis() - mouse.getPressTime()) > 175 ? darkCyan : Color.RED);
                if (rotmouse) {
                    g.rotate(Math.toRadians(getRotate(5)), loc.x, loc.y);
                }
                loc = mouse.getLocation();
                g.drawRect(loc.x - 3, loc.y - 3, 6, 6);
            } else {
                loc = mouse.getLocation();
                if (rotmouse) {
                    g.rotate(Math.toRadians(getRotate(5)), loc.x, loc.y);
                }
                g.drawImage(nmouse, loc.x - 8, loc.y - 8, null);
            }
        }
        g.setColor(color1);
    }

    /*Calculations for paint*/
    private Point pointInMiddle(Point start, Point end) {
        return new Point((int) ((start.x + end.x) / 2), (int) ((start.y + end.y) / 2));
    }

    private double getRotate(int ticks) {
        return (System.currentTimeMillis() % (360 * ticks)) / ticks;
    }

    private Point tileToMap(RSTile tile) {
        RSPlayer player = getMyPlayer();
        double minimapAngle = -1 * Math.toRadians(camera.getAngle());
        int x = (tile.getX() - player.getLocation().getX()) * 4 - 2;
        int y = (player.getLocation().getY() - tile.getY()) * 4 - 2;
        return new Point((int) Math.round(x * Math.cos(minimapAngle) + y * Math.sin(minimapAngle) + 628),
                (int) Math.round(y * Math.cos(minimapAngle) - x * Math.sin(minimapAngle) + 87));
    }

    @Override
    public void messageReceived(MessageEvent event) {
        int id = event.getID();
        if (id == MessageEvent.MESSAGE_ACTION) {
            String message = event.getMessage().toLowerCase();
            if (message.contains("soft, workable") && message.contains("clay")) {
                numDone++;
            }
        }
    }

    /*STATES*/
    private enum States {

        OPENBANK, BANK, SOFTEN, TOBANK, FROMBANK, WAITFORINVENTORY, CHILL, REWARDSBOX
    }

    private States getState() {
        /*START: Super-Massive getState() method!*/
        canAB = true;
        if (isPaused() || !game.isLoggedIn() || getMyPlayer().isMoving() || !isRunning() || !isActive()) {
            return States.CHILL;
        }
        if (inventory.contains(softClay)) {
            if (atBank()) {
                return (!bank.isOpen() ? States.OPENBANK : States.BANK);
            } else if (atObject()) {
                if (inventory.contains(clay)) {
                    if (inv.idle() >= 60 || !inventory.contains(softClay)) {
                        return States.SOFTEN;
                    } else if (inv.idle() < 60) {
                        return States.WAITFORINVENTORY;
                    }
                } else if (inventory.getCount(clay) < 1) {
                    return States.TOBANK;
                }
            } else if (!atObject() && !atBank()) {
                if (inventory.contains(softClay)) {
                    return States.TOBANK;
                } else if (inventory.contains(clay) && !inventory.contains(softClay)) {
                    return States.FROMBANK;
                } else {
                    return States.TOBANK;
                }
            }
        } else if (inventory.contains(clay)) {
            if (atBank()) {
                if (inventory.contains(softClay)) {
                    return (!bank.isOpen() ? States.OPENBANK : States.BANK);
                } else if (inventory.contains(clay)) {
                    return States.FROMBANK;
                } else if (!inventory.contains(softClay) && !inventory.contains(clay)) {
                    return (!bank.isOpen() ? States.OPENBANK : States.BANK);
                }
            } else if (atObject()) {
                if (inventory.contains(clay)) {
                    if (inv.idle() > 60 || !inventory.contains(softClay)) {
                        return States.SOFTEN;
                    } else if (inv.idle() < 60) {
                        return States.WAITFORINVENTORY;
                    }
                } else if (inventory.getCount(clay) < 1) {
                    return States.TOBANK;
                }
            } else if (!atObject() && !atBank()) {
                if (inventory.contains(softClay)) {
                    return States.TOBANK;
                } else if (inventory.contains(clay) && !inventory.contains(softClay)) {
                    return States.FROMBANK;
                } else {
                    return States.TOBANK;
                }
            }
        } else if (!inventory.contains(clay) && !inventory.contains(softClay)) {
            if (atBank()) {
                return (!bank.isOpen() ? States.OPENBANK : States.BANK);
            }
            return States.TOBANK;
        }
        if (atBank()) {
            if (inventory.contains(softClay)) {
                if (!bank.isOpen()) {
                    return States.OPENBANK;
                } else {
                    return States.BANK;
                }
            } else if (atBank() && inventory.contains(softClay) && !inventory.contains(clay)) {
                if (!bank.isOpen()) {
                    return States.OPENBANK;
                } else {
                    return States.BANK;
                }
            } else if (atBank() && inventory.contains(clay)) {
                return States.FROMBANK;
            }
        }
        if (atObject()) {
            if (inventory.contains(clay)) {
                if (inv.idle() > 60 || !inventory.contains(softClay)) {
                    return States.SOFTEN;
                } else if (inv.idle() < 60) {
                    return States.WAITFORINVENTORY;
                }
            } else if (inventory.getCount(clay) < 1) {
                return States.TOBANK;
            }
        }
        if (!atBank() && !atObject()) {
            if (inventory.getCount(clay) >= 1) {
                return States.FROMBANK;
            } else if (!inventory.contains(clay) || inventory.contains(softClay)) {
                return States.TOBANK;
            }
        }
        return States.CHILL;
    }

    /*LOOP*/
    @Override
    public int loop() {
        canAB = true;
        if (isPaused() || !isRunning() || !isActive() || !game.isLoggedIn() || game.isWelcomeScreen() || game.isLoginScreen()) {
            return 1;
        }
        try {
            mouse.setSpeed(random(mouseLo, mouseHi));
            final States state = getState();
            switch (state) {
                case OPENBANK:
                    canAB = false;
                    if (!bank.isOpen()) {
                        if (!openBank()) {
                            canAB = true;
                            break;
                        }
                        for (int i = 0; i < 100 && !bank.isOpen(); i++) {
                            sleep(20);
                        }
                        canAB = true;
                        sleep(random(200, 320));
                    }
                    canAB = true;
                    break;
                case BANK:
                    if (inventory.contains(softClay)) {
                        canAB = false;
                        sleep(random(100, 195));
                        bank.depositAll();
                        for (int i = 0; i < 100 && !inventory.contains(softClay); i++) {
                            sleep(20);
                        }
                        canAB = true;
                        sleep(random(320, 450));
                    }
                    if (isPaused() || !isActive() || !isRunning()) {
                        break;
                    }
                    if (bank.getCount(clay) > 28 && inventory.getCount(clay) <= 27) {
                        canAB = false;
                        withdraw(clay, 28);
                        for (int i = 0; i < 100 && !inventory.contains(clay); i++) {
                            sleep(20);
                        }
                        canAB = true;
                    } else {
                        if (bank.getCount(clay) <= 27 && inventory.getCount(clay) < 1) {
                            running = false;
                            canAB = false;
                            inv.kill();
                            Antiban.kill();
                            log.severe("Out of clay to soften. Stopping script...");
                            bank.close();
                            stopScript(true);
                            game.logout(false);
                            sleep(800);
                            stopScript();
                        }
                    }
                    if (isPaused() || !isActive() || !isRunning()) {
                        break;
                    }
                    if (bank.isOpen() && inventory.contains(clay)) {
                        canAB = false;
                        sleep(random(120, 210));
                        bank.close();
                        for (int i = 0; i > 100 && !bank.isOpen(); i++) {
                            sleep(20);
                        }
                        canAB = true;
                        break;
                    }
                    canAB = true;
                    break;
                case SOFTEN:
                    boolean isWFally = loc instanceof Falador;
                    canAB = false;
                    sleep(random(800, 1000));
                    if (interfaces.getComponent(905, 14).isValid()) {
                        Rectangle area = interfaces.getComponent(905, 14).getArea();
                        int randx = (int) random(area.getMinX(), area.getMaxX());
                        int randy = (int) random(area.getMinY(), area.getMaxY());
                        mouseMove(new Point(randx, randy));
                        interfaces.getComponent(905, 14).doClick();
                        sleep(random(400, 1000));
                        if (random(1, 2) == random(1, 2)) {
                            Antiban.antibanmouse();
                        }
                        canAB = true;
                        inv.reset(0);
                        failCounter = 0;
                        sleep(random(230, 450));
                        break;
                    }
                    if (isPaused() || !isRunning() || !isActive()) {
                        break;
                    }
                    if (inv.idle() < 60) {
                        canAB = true;
                        if (!inventory.contains(clay)) {
                            break;
                        }
                        sleep(random(173, 357));
                        if (!inventory.contains(clay)) {
                            break;
                        }
                        if (inv.idle() < 60) {
                            return random(400, 700);
                        }
                    }
                    if (inv.idle() >= 60 && inventory.contains(clay) && !interfaces.getComponent(905, 14).isValid()) {
                        RSItem clayItem = getHardClay();
                        if (loc.getRSObject() != null && clayItem != null && inventory.contains(clay)) {
                            canAB = false;
                            if (!inventory.isItemSelected()) {
                                Rectangle area = clayItem.getComponent().getArea();
                                int randx = (int) random(area.getMinX(), area.getMaxX());
                                int randy = (int) random(area.getMinY(), area.getMaxY());
                                mouseMove(new Point(randx, randy));
                                clayItem.doAction("Use");
                                for (int i = 0; i < 100 && !inventory.isItemSelected(); i++) {
                                    sleep(20);
                                }
                                sleep(20);
                            }
                            if (isPaused() || !isActive() || !isRunning()) {
                                break;
                            }
                            canAB = true;
                            if (inventory.isItemSelected() && inventory.getSelectedItem().getID() != clay) {
                                Rectangle area = inventory.getSelectedItem().getComponent().getArea();
                                int randx = (int) random(area.getMinX(), area.getMaxX());
                                int randy = (int) random(area.getMinY(), area.getMaxY());
                                canAB = false;
                                mouseMove(new Point(randx, randy));
                                inventory.getSelectedItem().doClick(true);
                                if (isPaused() || !isActive() || !isRunning()) {
                                    break;
                                }
                                for (int i = 0; i < 100 && inventory.isItemSelected(); i++) {
                                    sleep(20);
                                }
                                sleep(20);
                                Rectangle area2 = clayItem.getComponent().getArea();
                                int randx2 = (int) random(area2.getMinX(), area2.getMaxX());
                                int randy2 = (int) random(area2.getMinY(), area2.getMaxY());
                                mouseMove(new Point(randx2, randy2));
                                clayItem.doAction("Use");
                                for (int i = 0; i < 100 && !inventory.isItemSelected(); i++) {
                                    sleep(20);
                                }
                                canAB = true;
                                sleep(20);
                            }
                            if (isPaused() || !isActive() || !isRunning()) {
                                break;
                            }
                            canAB = true;
                            if (isWFally && camera.getPitch() < 94) {
                                int rand = random(1, 6);
                                switch (rand) {
                                    case 1: {
                                        camera.setCompass('w');
                                    }
                                    break;
                                    case 2: {
                                        camera.setPitch(random(94, random(98, 100)));
                                    }
                                    break;
                                    case 3: {
                                        int rand2 = random(1, 2);
                                        if (rand2 == 1) {
                                            camera.setPitch(true);
                                        }
                                        camera.setCompass('e');
                                        if (camera.getPitch() <= 94) {
                                            camera.setPitch(true);
                                        }
                                    }
                                    break;
                                    default: {
                                        try {
                                            camera.turnTo(loc.getRSObject());
                                        } catch (NullPointerException npe) {
                                            camera.setPitch(true);
                                        }
                                    }
                                    break;
                                }
                            }
                            if (isPaused() || !isActive() || !isRunning()) {
                                break;
                            }
                            if (loc.getRSModel() != null) {
                                if (failCounter > 2) {
                                    failCounter = 0;
                                    canAB = true;
                                    inventory.clickSelectedItem();
                                    break;
                                }
                                canAB = false;
                                if (doAction(loc.getRSObject(), "Use", "Clay", loc.getObjectName())) {
                                    for (int i = 0; i < 100 && !interfaces.getComponent(905, 14).isValid(); i++) {
                                        sleep(35);
                                    }
                                    if (isPaused() || !isActive() || !isRunning()) {
                                        break;
                                    }
                                    canAB = true;
                                    sleep(random(160, 250));
                                    canAB = false;
                                    if (interfaces.getComponent(905, 14).isValid()) {
                                        Rectangle area = interfaces.getComponent(905, 14).getArea();
                                        int randx = (int) random(area.getMinX(), area.getMaxX());
                                        int randy = (int) random(area.getMinY(), area.getMaxY());
                                        mouseMove(new Point(randx, randy));
                                        interfaces.getComponent(905, 14).doClick();
                                        sleep(random(400, 1000));
                                        if (random(1, 2) == random(1, 2)) {
                                            Antiban.antibanmouse();
                                        }
                                        canAB = true;
                                        inv.reset(0);
                                        failCounter = 0;
                                        if (isPaused() || !isRunning() || !isActive()) {
                                            break;
                                        }
                                        sleep(random(230, 450));
                                        break;
                                    }
                                    failCounter++;
                                    canAB = true;
                                    break;
                                } else {
                                    canAB = false;
                                    Point ploc = getLocation(loc.getRSObject());
                                    mouseMove(ploc);
                                    if (!loc.getRSObject().doAction("Use Clay -> " + loc.getObjectName())) {
                                        if (isPaused() || !isActive() || !isRunning()) {
                                            break;
                                        }
                                        camera.turnTo(loc.getRSObject());
                                        failCounter++;
                                        canAB = true;
                                        break;
                                    } else {
                                        for (int i = 0; i < 200 && !interfaces.getComponent(905, 14).isValid(); i++) {
                                            sleep(35);
                                        }
                                        if (isPaused() || !isActive() || !isRunning()) {
                                            break;
                                        }
                                        canAB = true;
                                        sleep(random(160, 250));
                                        canAB = false;
                                        if (interfaces.getComponent(905, 14).isValid()) {
                                            Rectangle area = interfaces.getComponent(905, 14).getArea();
                                            int randx = (int) random(area.getMinX(), area.getMaxX());
                                            int randy = (int) random(area.getMinY(), area.getMaxY());
                                            mouseMove(new Point(randx, randy));
                                            interfaces.getComponent(905, 14).doClick();
                                            sleep(random(400, 1000));
                                            if (random(1, 2) == random(1, 2)) {
                                                Antiban.antibanmouse();
                                            }
                                            canAB = true;
                                            inv.reset(0);
                                            failCounter = 0;
                                            if (isPaused() || !isActive() || !isRunning()) {
                                                break;
                                            }
                                            sleep(random(230, 450));
                                            break;
                                        }
                                        failCounter++;
                                        canAB = true;
                                        break;
                                    }
                                }
                            }
                        }
                    }
                    canAB = true;
                    break;
                case TOBANK:
                    loc.walkToBank();
                    break;
                case FROMBANK:
                    loc.walkToObject();
                    break;
                case CHILL:
                    sleep(500);
                    break;
                case WAITFORINVENTORY: {
                    canAB = true;
                    if (isPaused() || !isActive() || !isRunning()) {
                        break;
                    }
                    if (inv.idle() < 60) {
                        if (!inventory.contains(clay)) {
                            break;
                        }
                        sleep(random(100, 225));
                        if (!inventory.contains(clay)) {
                            break;
                        }
                        if (isPaused() || !isActive() || !isRunning()) {
                            break;
                        }
                        if (inv.idle() < 60) {
                            return random(700, 1100);
                        }
                    }
                }
                break;
            }
        } catch (NullPointerException npe) {
            return 1;
        } catch (ArrayIndexOutOfBoundsException aio) {
            return 1;
        } catch (IllegalArgumentException iae) {
        }
        return random(95, 175);
    }

    /*Defined Methods*/

    /*Moves the mouse using one of three methods: RSBot's API's mouse.move(), hopping along points of a bezier curve,
     *and moveMouse(), which kind of squiggles the mouse in a fairly straight line to the destination.
     */
    private void mouseMove(Point destination) {
        try {
            canAB = false;
            mousemovet = System.currentTimeMillis();
            sleep(random(40, 60));
            if (!beziers && !shortsplines) {
                mouse.move(destination);
                canAB = true;
                return;
            } else if (beziers && shortsplines) {
                int rand = random(1, 18);
                if (rand <= 3 && calc.distanceBetween(mouse.getLocation(), destination) >= 300) {
                    rand = random(4, 18);
                }
                switch (rand) {
                    case 1:
                    case 2:
                    case 3: {
                        moveMouse(destination);
                        break;
                    }
                    case 4:
                    case 5:
                    case 6:
                    case 7:
                    case 8: {
                        alternateSplineGen.mouseTo(destination);
                        break;
                    }
                    default: {
                        mouse.move(destination);
                        break;
                    }
                }
                canAB = true;
                return;
            } else if (beziers && !shortsplines) {
                int rand = random(1, 15);
                switch (rand) {
                    case 1:
                    case 2:
                    case 3:
                    case 4: {
                        alternateSplineGen.mouseTo(destination);
                        break;
                    }
                    default: {
                        mouse.move(destination);
                        break;
                    }
                }
                canAB = true;
                return;
            } else {
                int rand = random(1, 13);
                if (rand <= 2 && calc.distanceBetween(mouse.getLocation(), destination) >= 300) {
                    rand = random(3, 13);
                }
                switch (rand) {
                    case 1:
                    case 2: {
                        moveMouse(destination);
                        break;
                    }
                    default: {
                        mouse.move(destination);
                        break;
                    }
                }
                canAB = true;
                return;
            }
        } catch (Exception e) {
            mouse.move(destination);
            canAB = true;
            return;
        }
    }

    private boolean openBank() {
        try {
            if (!bank.isOpen()) {
                if (menu.isOpen()) {
                    mouse.moveSlightly();
                    sleep(random(40, 60));
                }
                RSObject bankBooth = objects.getNearest(Bank.BANK_BOOTHS);
                RSNPC banker = npcs.getNearest(Bank.BANKERS);
                RSObject bankChest = objects.getNearest(Bank.BANK_CHESTS);
                int dist = calc.distanceTo(bankBooth);
                if (banker != null && bankBooth != null && calc.distanceTo(banker) < dist) {
                    if (calc.distanceBetween(banker.getLocation(), bankBooth.getLocation()) <= 1) {
                        if (random(1, 3) >= 2) {
                            banker = null;
                        } else {
                            bankBooth = null;
                        }
                    } else {
                        bankBooth = null;
                    }
                }
                if (bankChest != null && calc.distanceTo(bankChest) < dist) {
                    bankBooth = null;
                    banker = null;
                }
                if (((bankBooth != null) && (calc.distanceTo(bankBooth) < 5) && calc.tileOnMap(
                        bankBooth.getLocation()) && calc.canReach(bankBooth.getLocation(), true))
                        || ((banker != null) && (calc.distanceTo(banker) < 8) && calc.tileOnMap(banker.getLocation())
                        && calc.canReach(banker.getLocation(), true)) || ((bankChest != null) && (calc.distanceTo(
                        bankChest) < 8) && calc.tileOnMap(bankChest.getLocation()) && calc.canReach(
                        bankChest.getLocation(), true) && !bank.isOpen())) {
                    if (bankBooth != null) {
                        Point loc = getLocation(bankBooth);
                        for (int i = 0; i < 10 && !menu.contains("Use-Quickly"); i++) {
                            mouseMove(loc);
                            if (menu.contains("Use-Quickly")) {
                                sleep(random(20, 60));
                                if (menu.contains("Use-Quickly")) {
                                    sleep(random(20, 60));
                                    if (menu.contains("Use-Quickly")) {
                                        break;
                                    }
                                }
                            }
                        }
                        if (doMenuAction("Use-Quickly")) {
                            int count = 0;
                            while (!bank.isOpen() && ++count <= 10) {
                                sleep(random(200, 300));
                                if (getMyPlayer().isMoving()) {
                                    count = 0;
                                }
                            }
                        } else {
                            camera.turnTo(bankBooth);
                        }
                    } else if (banker != null) {
                        RSModel m = banker.getModel();
                        if (m == null) {
                            m = banker.getModel();
                            if (m == null) {
                                return false;
                            }
                        }
                        Point loc = pointOnScreen(m);
                        for (int i = 0; i < 10 && !menu.contains("Bank Banker"); i++) {
                            mouseMove(loc);
                            if (menu.contains("Bank Banker")) {
                                sleep(random(20, 60));
                                if (menu.contains("Bank Banker")) {
                                    sleep(random(20, 60));
                                    if (menu.contains("Bank Banker")) {
                                        break;
                                    }
                                }
                            }
                        }
                        if (doMenuAction("Use-Quickly")) {
                            int count = 0;
                            while (!bank.isOpen() && ++count <= 10) {
                                sleep(random(200, 300));
                                if (getMyPlayer().isMoving()) {
                                    count = 0;
                                }
                            }
                        } else {
                            camera.turnTo(banker);
                        }
                    } else if (bankChest != null) {
                        Point loc = getLocation(bankChest);
                        for (int i = 0; i < 10 && !menu.contains("Bank") && !menu.contains("Use"); i++) {
                            mouseMove(loc);
                            if (menu.contains("Bank") || menu.contains("Use")) {
                                sleep(random(20, 60));
                                if (menu.contains("Bank") || menu.contains("Use")) {
                                    sleep(random(20, 60));
                                    if (menu.contains("Bank") || menu.contains("Use")) {
                                        break;
                                    }
                                }
                            }
                        }
                        if (doMenuAction("Bank") || doMenuAction("Use")) {
                            int count = 0;
                            while (!bank.isOpen() && ++count <= 10) {
                                sleep(random(200, 300));
                                if (getMyPlayer().isMoving()) {
                                    count = 0;
                                }
                            }
                        } else {
                            camera.turnTo(bankBooth);
                        }
                    }
                } else {
                    if (bankBooth != null) {
                        walking.walkTo(bankBooth.getLocation());
                    } else if (banker != null) {
                        walking.walkTo(banker.getLocation());
                    } else if (bankChest != null) {
                        walking.walkTo(bankChest.getLocation());
                    } else {
                        return false;
                    }
                }
            }
            return bank.isOpen();
        } catch (Exception e) {
            return false;
        }
    }

    /*Creates, shows, and waits for the GUI. Does not give thousands of lines of errors if users have installed
     *themes.
     */
    private Point pointOnScreen(RSModel m) {
        if (m == null) {
            return new Point(-1, -1);
        }
        Point[] all = m.getPoints();
        if (all == null) {
            return new Point(-1, -1);
        }
        ArrayList<Point> onscreen = new ArrayList<Point>();
        for (Point p : all) {
            if (calc.pointOnScreen(p)) {
                onscreen.add(p);
            }
        }
        if (onscreen.isEmpty()) {
            return new Point(-1, -1);
        }
        return onscreen.get(random(0, onscreen.size() - 1));
    }

    private void createAndWaitforGUI() {
        if (sm == null) {
            sm = new SettingsManager(GlobalConfiguration.Paths.getSettingsDirectory() + File.separator + "Aut0ClaySoftener.dat");
        }
        if (SwingUtilities.isEventDispatchThread()) {
            gui = new GUI(sm);
            gui.setVisible(true);
        } else {
            try {
                SwingUtilities.invokeAndWait(new Runnable() {

                    @Override
                    public void run() {
                        gui = new GUI(sm);
                        gui.setVisible(true);
                    }
                });
            } catch (InvocationTargetException ite) {
            } catch (InterruptedException ie) {
            }
        }
        sleep(100);
        while (gui.isVisible()) {
            sleep(100);
        }
        if (!gui.isCanceled()) {
            sm.save();
        }
    }

    private Point getLocation(RSObject obj) {
        if (obj == null) {
            return null;
        }
        RSModel model = obj.getModel();
        if (model == null) {
            model = obj.getModel();
            if (model == null) {
                return calc.tileToScreen(obj.getLocation(), 0.5, 0.5, 0);
            }
        }
        Point toreturn = pointOnScreen(model);
        if (toreturn == null) {
            toreturn = model.getPointOnScreen();
            if (toreturn == null) {
                Point[] points = model.getPoints();
                toreturn = points[points.length / 2];
            }
        }
        return toreturn;
    }

    private boolean doAction(RSObject object, String doaction, String item, String obj) {
        canAB = false;
        String action = doaction + " " + item + " -> " + obj;
        Point loc = getLocation(object);
        int rand = random(10, 13);
        while (--rand > 0 && !menu.contains(action)) {
            mouseMove(loc);
            sleep(random(30, 70));
            if (menu.contains(action)) {
                sleep(random(30, 70));
                if (menu.contains(action)) {
                    sleep(random(30, 70));
                    if (menu.contains(action)) {
                        break;
                    }
                }
            }
            loc = getLocation(object);
        }
        if (menu.contains(action)) {
            return doMenuAction(action);
        } else {
            RSModel model = object.getModel();
            Point move;
            if (model == null) {
                move = getLocation(object);
            } else {
                move = model.getCentralPoint();
            }
            mouseMove(move);
            sleep(random(40, 70));
            if (!doMenuAction(action)) {
                sleep(random(30, 70));
                loc = getLocation(object);
                mouse.move(loc);
                sleep(random(30, 60));
                return menu.doAction(action);
            } else {
                canAB = true;
                return true;
            }
        }
    }

    private boolean clickmenuidx(int idx) {
        Point beginloc = mouse.getLocation();
        if (idx == 0) {
            mouse.click(true);
            return true;
        }
        if (menu.isOpen()) {
            if (menu.getItems().length < idx) {
                return menu.doAction("Cancel");
            }
        } else {
            mouse.click(false);
            for (int i = 0; i < 100 && !menu.isOpen(); i++) {
                sleep(20);
            }
        }
        String[] items = menu.getItems();
        if (idx >= items.length) {
            return false;
        }
        if (!menu.clickIndex(idx)) {
            mouseMove(beginloc);
            mouse.click(beginloc, false);
            for (int i = 0; i < 100 && !menu.isOpen(); i++) {
                sleep(20);
            }
            items = menu.getItems();
            int index = 0;
            for (String current : items) {
                if (menu.getIndex(current) == idx) {
                    break;
                }
                index++;
            }
            if (index >= items.length) {
                return false;
            }
            if (idx != index) {
                return false;
            }
            return menu.clickIndex(index);
        }
        return true;
    }

    private boolean doMenuAction(String action) {
        if (menu.isOpen()) {
            if (!menu.contains(action)) {
                menu.doAction("Cancel");
            }
        }
        String[] items = menu.getItems();
        if (items[0].contains(action)) {
            return clickmenuidx(0);
        }
        boolean contains = false;
        int index = 0;
        for (String check : items) {
            if (check.contains(action)) {
                contains = true;
                break;
            } else if (items[index].contains(action)) {
                contains = true;
                break;
            }
            index++;
        }
        if (!contains || index < items.length) {
            mouse.click(false);
            for (int i = 0; i < 100 && !menu.isOpen(); i++) {
                sleep(20);
            }
            try {
                int idx = menu.getIndex(action);
                if (idx < items.length) {
                    return clickmenuidx(idx);
                } else {
                    return false;
                }
            } catch (ArrayIndexOutOfBoundsException aioe) {
                return false;
            }
        }
        return clickmenuidx(index);
    }

    /*For moving the mouse precisely (In a fairly straight line)*/
    private void moveMouse(Point location) {
        Point mlocation = mouse.getLocation();
        Point mid = pointInMiddle(mlocation, location);
        ArrayList<Point> midPoints = new ArrayList<Point>();
        midPoints.add(mlocation);
        if (random(1, 3) == random(1, 3)) {
            midPoints.add(pointInMiddle(mlocation, mid));
        }
        midPoints.add(mid);
        if (random(1, 2) == random(1, 2) || (midPoints.size() <= 1 && random(1, 3) >= 2)) {
            midPoints.add(pointInMiddle(mid, location));
        }
        midPoints.add(location);
        for (Point curr : midPoints) {
            mouse.move(curr);
        }
    }

    private RSItem getHardClay() {
        RSItem[] allClay = inventory.getItems(clay);
        int rand = random(0, allClay.length - 1);
        RSItem randClay = allClay[rand];
        if (randClay == null) {
            randClay = inventory.getItem(clay);
        }
        return randClay;
    }

    private boolean withdraw(final int itemID, final int count) {
        if (count < 0) {
            throw new IllegalArgumentException("count < 0 (" + count + ")");
        }
        if (!bank.isOpen() || isPaused() || !isActive() || !isRunning()) {
            return false;
        }
        final RSItem item = bank.getItem(itemID);
        if (item == null) {
            return false;
        }
        RSComponent comp = item.getComponent();
        if (comp == null) {
            return false;
        }
        while (comp.getRelativeX() == 0 && bank.getCurrentTab() != 0) {
            interfaces.getComponent(Bank.INTERFACE_BANK, Bank.INTERFACE_BANK_TAB[0]).doClick();
            sleep(random(600, 1100));
        }
        if (!interfaces.scrollTo(comp, (Bank.INTERFACE_BANK << 16) + Bank.INTERFACE_BANK_SCROLLBAR)) {
            return false;
        }
        sleep(random(60, 200));
        Rectangle area = comp.getArea();
        int randx = (int) random(area.getMinX(), area.getMaxX());
        int randy = (int) random(area.getMinY(), area.getMaxY());
        Point itemPoint = new Point(randx, randy);
        final int inventoryCount = inventory.getCount(true);
        switch (count) {
            case 0: // Withdraw All
                mouseMove(itemPoint);
                doMenuAction("Withdraw-All");
                break;
            case 1: // Withdraw 1
                mouseMove(itemPoint);
                item.doClick(true);
                break;
            case 5: // Withdraw 5
            case 10: // Withdraw 10
                mouseMove(itemPoint);
                doMenuAction("Withdraw-" + count);
                break;
            default: // Withdraw x
                mouseMove(itemPoint);
                sleep(random(100, 500));
                if (menu.contains("Withdraw-" + count)) {
                    if (doMenuAction("Withdraw-" + count)) {
                        sleep(random(100, 200));
                        return true;
                    }
                    return false;
                }
                if (!area.contains(mouse.getLocation())) {
                    mouseMove(itemPoint);
                }
                if (doMenuAction("Withdraw-X")) {
                    sleep(random(1000, 1300));
                    keyboard.sendText(Integer.toString(count), true);
                }
                sleep(random(100, 200));
                break;
        }
        return (inventory.getCount(true) > inventoryCount)
                || (inventory.getCount(true) == 28);
    }

    private boolean atBank() {
        return loc.getBankArea().contains(players.getMyPlayer().getLocation());
    }

    private boolean atObject() {
        return loc.getObjectArea().contains(players.getMyPlayer().getLocation());
    }

    /* Classes */

    /*For Antiban purposes; Less botlike if mouse isn't always moved using the same spline-generating algorithm*/
    private class splines {

        private void mouseTo(Point dest) {
            waypoint[] path = genBezier(genControls(mouse.getLocation(), dest));
            int x1 = mouse.getLocation().x, y1 = mouse.getLocation().y, x2 = dest.x, y2 = dest.y;
            int timeToMove = (int) MouseHandler.fittsLaw(Math.sqrt(Math.pow(x2 - x1, 2) + Math.pow(y2 - y1, 2)), 10);
            waypoint[] curve1 = applyDynamism(path, timeToMove, mouse.getSpeed() + 1);
            Point[] curve = clean(new ArrayList<waypoint>(Arrays.asList(curve1)));
            for (Point curr : curve) {
                hop(curr);
                try {
                    Thread.sleep(random(1, 3) >= 2 ? Math.max(1, mouse.getSpeed() - 2 + rand.nextInt(4))
                            : Math.max(1, mouse.getSpeed() - 3 + rand.nextInt(6)));
                } catch (InterruptedException ie) {
                }
            }
            if (!mouse.getLocation().equals(dest)) {
                mouseMove(dest);
                sleep(random(20, 60));
                if (!mouse.getLocation().equals(dest)) {
                    hop(dest);
                    sleep(random(20, 60));
                    if (!mouse.getLocation().equals(dest)) {
                        mouse.hop(dest);
                    }
                }
            }
        }

        private double gaussian(double t) {
            t = 10D * t - 5D;
            return 1D / (Math.sqrt(5D) * Math.sqrt(2D * Math.PI)) * Math.exp(-t * t / 20D);
        }

        private double[] gaussTable(final int steps) {
            final double[] table = new double[steps];
            final double step = 1D / steps;
            double sum = 0;
            for (int i = 0; i < steps; i++) {
                sum += gaussian(i * step);
            }
            for (int i = 0; i < steps; i++) {
                table[i] = gaussian(i * step) / sum;
            }
            return table;
        }

        public waypoint[] applyDynamism(final waypoint[] spline, final int msForMove, final int msPerMove) {
            final int numPoints = spline.length;
            final double msPerPoint = (double) msForMove / (double) numPoints;
            final double undistStep = msPerMove / msPerPoint;
            final int steps = (int) Math.floor(numPoints / undistStep);
            final waypoint[] result = new waypoint[steps];
            final double[] gaussValues = gaussTable(result.length);
            double currentPercent = 0;
            for (int i = 0; i < steps; i++) {
                currentPercent += gaussValues[i];
                final int nextIndex = (int) Math.floor(numPoints * currentPercent);
                if (nextIndex < numPoints) {
                    result[i] = spline[nextIndex];
                } else {
                    result[i] = spline[numPoints - 1];
                }
            }
            if (currentPercent < 1D) {
                result[steps - 1] = spline[numPoints - 1];
            }
            return result;
        }

        private waypoint[] adaptivemids(waypoint[] points1) {
            int i = 0;
            ArrayList<waypoint> points = new ArrayList<waypoint>(Arrays.asList(points1));
            while (i < points.size() - 1) {
                final waypoint a = points.get(i++);
                final waypoint b = points.get(i);
                if ((Math.abs(a.x - b.x) > 1) || (Math.abs(a.y - b.y) > 1)) {
                    if (Math.abs(a.x - b.x) != 0) {
                        final double slope = (double) (a.y - b.y) / (double) (a.x - b.x);
                        final double incpt = a.y - slope * a.x;
                        for (double c = a.x < b.x ? a.x + 1 : b.x - 1; a.x < b.x ? c < b.x : c > a.x; c += a.x < b.x ? 1 : -1) {
                            points.add(i++, new waypoint(c, Math.round(incpt + slope * c)));
                        }
                    } else {
                        for (double c = a.y < b.y ? a.y + 1 : b.y - 1; a.y < b.y ? c < b.y : c > a.y; c += a.y < b.y ? 1 : -1) {
                            points.add(i++, new waypoint(a.x, c));
                        }
                    }
                }
            }
            return points.toArray(new waypoint[points.size()]);
        }

        private void hop(Point hopdest) {
            Point mloc = mouse.getLocation();
            Point[] controls = MouseHandler.generateControls(mloc.x, mloc.y, hopdest.x, hopdest.y, 50, 100);
            Point[] spline = MouseHandler.generateSpline(controls);
            ArrayList<Point> path = new ArrayList<Point>();
            path.addAll(Arrays.asList(spline));
            path.add(hopdest);
            for (Point onPath : path) {
                mouse.hop(onPath);
            }
        }

        private Point[] genControls(Point start, Point end) {
            random(1, 8);
            ArrayList<Point> controls = new ArrayList<Point>();
            controls.add(start);
            /*Cubic Bezier*/
            if (random(1, 2) == 1) {
                int x = random(0, game.getWidth());
                int y = random(0, game.getHeight());
                Point cp1 = new Point(x, y);
                Point cp2 = new Point(x, y);
                int loops = 0;
                while (calc.distanceBetween(cp1, cp2) <= 200 && ((loops++) < 200)) {
                    if (loops >= 200) {
                        break;
                    }
                    x = random(0, game.getWidth() + 200);
                    x = x - random(0, 200);
                    y = random(0, game.getHeight() + 200);
                    y = y - random(0, 200);
                    cp2 = new Point(x, y);
                }
                if (loops >= 200) {
                    cp2 = new Point(cp2.y, cp2.x);
                }
                if (random(1, 2) == random(1, 2)) {
                    controls.add(cp1);
                    controls.add(cp2);
                } else {
                    controls.add(cp2);
                    controls.add(cp1);
                }
                controls.add(end);
                return controls.toArray(new Point[controls.size()]);
            } else {
                Point[] controls2 = genRelativeControls(start, end, 4);
                return controls2;

            }
        }

        /*Generates controls which are evenly spaced, not completely random.
         *
         * Partially copied from MouseHandler.java and edited
         */
        private final Random rand = new Random();

        private Point[] genRelativeControls(Point start, Point end, int numofcontrols) {
            if (numofcontrols < 3 || numofcontrols > 4) { //Can't make a curve with 2 points. More than 4 points is not supported.
                return null;
            }
            calc.distanceBetween(start, end);
            double angle = Math.atan2(end.y - start.y, end.x - start.x);
            ArrayList<Point> result = new ArrayList<Point>();
            result.add(start);
            int ctrlSpacing = random(70, 80);
            for (int i = 1; i < numofcontrols; i++) {
                ctrlSpacing = random(70, 80);
                double radius = ctrlSpacing * i;
                Point cur = new Point((int) (start.x + radius * Math.cos(angle)), (int) (start.y + radius * Math.sin(angle)));
                double percent = 1D - (double) (i - 1) / (double) numofcontrols;
                percent = percent > 0.5 ? percent - 0.5 : percent;
                percent += 0.25;
                int curVariance = (int) (random(115, 130) * percent);
                cur.setLocation((int) (cur.y + curVariance * 2 * rand.nextDouble()
                        - curVariance), (int) (cur.x + curVariance * 2 * rand.nextDouble()
                        - curVariance));
                result.add(cur);
            }
            if (numofcontrols == 3) {
                result.add(result.get(result.size() - 1));
            }
            result.add(end);
            return result.toArray(new Point[result.size()]);
        }

        private waypoint[] genBezier(final Point[] controls) {
            Point[] coordlist = controls;
            double x1, x2, y1, y2;
            x1 = coordlist[0].x;
            y1 = coordlist[0].y;
            ArrayList<waypoint> points = new ArrayList<waypoint>();
            points.add(new waypoint(x1, y1));
            for (double t = 0; t <= 1; t += 0.01) {
                /*BERNSTEIN POLYNOMINALS.
                 *
                 *Check out http://math.fullerton.edu/mathews/n2003/BezierCurveMod.html
                 */
                x2 = (coordlist[0].x + t * (-coordlist[0].x * 3 + t * (3 * coordlist[0].x
                        - coordlist[0].x * t))) + t * (3 * coordlist[1].x + t * (-6 * coordlist[1].x
                        + coordlist[1].x * 3 * t)) + t * t * (coordlist[2].x * 3 - coordlist[2].x * 3 * t)
                        + coordlist[3].x * t * t * t;
                y2 = (coordlist[0].y + t * (-coordlist[0].y * 3 + t * (3 * coordlist[0].y
                        - coordlist[0].y * t))) + t * (3 * coordlist[1].y + t * (-6 * coordlist[1].y
                        + coordlist[1].y * 3 * t)) + t * t * (coordlist[2].y * 3 - coordlist[2].y * 3 * t)
                        + coordlist[3].y * t * t * t;
                points.add(new waypoint(x2, y2));
            }
            if (!points.get(points.size() - 1).equals(new waypoint(coordlist[3].getX(), coordlist[3].getY()))) {
                points.add(new waypoint(coordlist[3].getX(), coordlist[3].getY()));
            }
            return adaptivemids(points.toArray(new waypoint[points.size()]));
        }

        private Point[] clean(ArrayList<waypoint> points) {
            Vector<Point> cleaned = new Vector<Point>();
            for (waypoint p : points) {
                try {
                    if (!p.topoint().equals(cleaned.lastElement())) {
                        cleaned.add(p.topoint());
                    }
                } catch (Exception e) {
                    cleaned.add(p.topoint());
                }
            }
            return cleaned.toArray(new Point[cleaned.size()]);
        }
    }

    private class waypoint {

        public double x, y;

        public waypoint(double x1, double y1) {
            x = x1;
            y = y1;
        }

        public Point topoint() {
            return new Point((int) x, (int) y);
        }
    }

    protected static class locVar {

        public static final int fally = 0;
        public static final int edge = 1;
    }

    private abstract class Locations {

        public abstract RSArea getObjectArea();

        public abstract RSArea getBankArea();

        public abstract void walkToBank();

        public abstract void walkToObject();

        public abstract RSObject getRSObject();

        public abstract RSModel getRSModel();

        public abstract String getObjectName();
    }

    /*Falador West*/
    private class Falador extends Locations {

        private final RSArea object = new RSArea(new RSTile(2948, 3382), new RSTile(
                2950, 3384));
        private final RSArea bank = new RSArea(new RSTile(2945, 3368), new RSTile(
                2947, 3370));
        private final RSTile bankTile = new RSTile(2946, 3368);
        private final RSTile objectTile = new RSTile(2949, 3382);
        private int objectId = 11661;
        private String objectName = "Waterpump";

        @Override
        public String getObjectName() {
            return objectName;
        }

        @Override
        public RSObject getRSObject() {
            return objects.getNearest(objectId);
        }

        @Override
        public RSModel getRSModel() {
            return objects.getNearest(objectId).getModel();
        }

        @Override
        public RSArea getObjectArea() {
            return object;
        }

        @Override
        public RSArea getBankArea() {
            return bank;
        }

        @Override
        public void walkToBank() {
            walker.walkTo(bankTile, true);
            canAB = true;
        }

        @Override
        public void walkToObject() {
            walker.walkTo(objectTile, true);
            canAB = true;
        }
    }

    /*Edgeville Well*/
    private class Edgeville extends Locations {

        private final RSArea bank = new RSArea(new RSTile[]{new RSTile(3098, 3498), new RSTile(3090, 3499), new RSTile(3090, 3491), new RSTile(3093, 3489), new RSTile(3098, 3487), new RSTile(3099, 3492)});
        private final RSArea object = new RSArea(new RSTile[]{new RSTile(3084, 3496), new RSTile(3089, 3501), new RSTile(3086, 3506), new RSTile(3081, 3504), new RSTile(3081, 3497)});
        private final RSTile bankTile = new RSTile(3093, 3493);
        private final RSTile objectTile = new RSTile(3085, 3500);
        private final RSTile onObjTile = new RSTile(3084, 3501);
        private String objectName = "Well";

        @Override
        public RSObject getRSObject() {
            RSObject well = objects.getNearest(new Filter<RSObject>() {

                @Override
                public boolean accept(RSObject check) {
                    RSObjectDef def = check.getDef();
                    if (def == null) {
                        def = check.getDef();
                    }
                    if (def == null) {
                        return false;
                    }
                    String name = def.getName();
                    if (name == null) {
                        name = def.getName();
                    }
                    if (name == null) {
                        return false;
                    }
                    if (name.toLowerCase().equals(objectName.toLowerCase()) && check.getLocation().equals(onObjTile)) {
                        return true;
                    }
                    return false;
                }
            });
            if (well == null) {
                RSObject obj = objects.getTopAt(onObjTile);
                well = (obj != null ? obj : objects.getNearest(objectName));
            }
            return well;
        }

        @Override
        public RSModel getRSModel() {
            return getRSObject().getModel();
        }

        @Override
        public RSArea getObjectArea() {
            return object;
        }

        @Override
        public RSArea getBankArea() {
            return bank;
        }

        @Override
        public void walkToBank() {
            walker.walkTo(bankTile, true);
            canAB = true;
        }

        @Override
        public String getObjectName() {
            return objectName;
        }

        @Override
        public void walkToObject() {
            walker.walkTo(objectTile, true);
            canAB = true;
        }
    }

    private class Walking {

        private utils walkutils = new utils();
        private walker Walker = new walker();
        private pathfinder pfinder = new pathfinder();

        public boolean walkTo(RSTile destination, boolean randomize) {
            canAB = false;
            if (Walker.walkTo(destination, randomize)) {
                canAB = true;
                return true;
            } else {
                canAB = true;
                return false;
            }
        }

        private class utils {

            private RSTile[] clean(RSTile[] path) {
                ArrayList<RSTile> cleanpath = new ArrayList<RSTile>();
                ArrayList<Integer> badidx = new ArrayList<Integer>();
                for (int i = 0; i < path.length; i++) {
                    try {
                        if (!badidx.contains(i)) {
                            cleanpath.add(path[i]);
                            badidx.add(i);
                        }
                        if (cleanpath.contains(path[i + 1])) {
                            badidx.add(i + 1);
                        }
                        if (cleanpath.contains(path[i + 2])) {
                            badidx.add(i + 2);
                        }
                    } catch (Exception e) {
                        continue;
                    }
                }
                if (!cleanpath.get(cleanpath.size() - 1).equals(path[path.length - 1])) {
                    cleanpath.add(path[path.length - 1]);
                }
                RSTile[] cleaned = cleanpath.toArray(new RSTile[cleanpath.size()]);
                return (cleaned != null ? cleaned : path);
            }

            private RSTile[] lengthenPath(RSTile[] path) {
                ArrayList<RSTile> tiles = new ArrayList<RSTile>();
                try {
                    for (int i = 0; i < path.length; i++) {
                        RSTile current = path[i];
                        if (current == null) {
                            continue;
                        }
                        tiles.add(current);
                        RSTile next = path[i + 1];
                        if (next != null && walking.isLocal(current) && walking.isLocal(next)) {
                            RSTile[] pathTo = pfinder.findPath(current, next, false);
                            if (pathTo != null) {
                                tiles.addAll(Arrays.asList(pathTo));
                            } else {
                                pathTo = pfinder.findPath(current, next, false);
                                if (pathTo != null) {
                                    tiles.addAll(Arrays.asList(pathTo));
                                }
                            }
                        }
                    }
                } catch (ArrayIndexOutOfBoundsException aioe) {
                }
                if (!tiles.get(tiles.size() - 1).equals(path[path.length - 1])) {
                    tiles.add(path[path.length - 1]);
                }
                return tiles.toArray(new RSTile[tiles.size()]);
            }

            private RSTile[] randPath(RSTile[] path, int randomize) {
                ArrayList<RSTile> randomized = new ArrayList<RSTile>();
                RSTile start = path[0];
                RSTile finish = path[path.length - 1];
                randomized.add(start);
                for (int i = 0; i < path.length; i++) {
                    RSTile current = path[i];
                    int x = current.getX(), y = current.getY(), z = current.getZ();
                    if (random(1, 2) == 1) {
                        x -= random(0, randomize);
                    } else {
                        x += random(0, randomize);
                    }
                    if (random(1, 2) == 1) {
                        y -= random(0, randomize);
                    } else {
                        y += random(0, randomize);
                    }
                    randomized.add(new RSTile(x, y, z));
                }
                randomized.add(finish);
                RSTile[] randPath = randomized.toArray(new RSTile[randomized.size()]);
                return lengthenPath(randPath);
            }

            public RSTile[] generatePath(RSTile destination, int randomize) {
                try {
                    RSTile[] path = pfinder.findPath(getMyPlayer().getLocation(), destination);
                    if (randomize != 0) {
                        path = randPath(path, randomize);
                    }
                    path = lengthenPath(path);
                    path = clean(path);
                    ArrayList<RSTile> tiles = new ArrayList<RSTile>(Arrays.asList(path));
                    if (!tiles.get(tiles.size() - 1).equals(destination)) {
                        tiles.add(destination);
                    }
                    return tiles.toArray(new RSTile[tiles.size()]);
                } catch (Exception e) {
                    return null;
                }
            }
        }

        private class walker {

            public boolean walkTo(RSTile destination, boolean randomize) {
                return walkTo(destination, randomize ? 2 : 0);
            }

            public boolean walkTo(RSTile destination, int randomize) {
                long timeout = 9000;
                long walktimeout = 10000;
                long runtimeout = 9000;
                int loops = 0;
                long starttime = System.currentTimeMillis();
                canAB = false;
                RSTile[] path = walkutils.generatePath(destination, randomize);
                if (path == null) {
                    return false;
                }
                RSTilePath tp = walking.newTilePath(path);
                while (tp.traverse() && isRunning() && isActive() && !isPaused() && calc.distanceTo(destination) > 3) {
                    drawPath = path;
                    if (path == null) {
                        path = walkutils.generatePath(destination, randomize);
                        if (path == null) {
                            return false;
                        }
                    }
                    drawPath = path;
                    tp = walking.newTilePath(path);
                    loops++;
                    sleep(random(60, 80));
                    canAB = true;
                    if (!getMyPlayer().isMoving()) {
                        path = walkutils.generatePath(destination, randomize);
                        drawPath = path;
                        tp = walking.newTilePath(path);
                        sleep(200);
                        if (!getMyPlayer().isMoving()) {
                            tp.traverse();
                            sleep(300);
                            if (!getMyPlayer().isMoving()) {
                                canAB = true;
                                return calc.distanceTo(destination) <= 3;
                            }
                        }
                    }
                    if (walking.isRunEnabled()) {
                        timeout = runtimeout;
                    } else {
                        timeout = walktimeout;
                    }
                    if ((System.currentTimeMillis() - starttime) > (timeout * (tp.toArray().length - 1))) {
                        canAB = true;
                        return calc.distanceTo(destination) <= 3;
                    }
                    if (loops >= 2) {
                        path = walkutils.generatePath(destination, randomize);
                        drawPath = path;
                        tp = walking.newTilePath(path);
                        loops = 0;
                    }
                    canAB = false;
                    sleep(random(100, 200));
                    drawPath = path;
                }
                canAB = true;
                return calc.distanceTo(destination) <= 3;
            }
        }

        private class pathfinder {
            /*This was copied from RSLocalPath's A* methods. I copied because
             *The methods are deprecated and only work for generating paths
             * from the current player's location. All credits to Jacmob for
             *this class.
             */

            public static final int WALL_NORTH_WEST = 0x1;
            public static final int WALL_NORTH = 0x2;
            public static final int WALL_NORTH_EAST = 0x4;
            public static final int WALL_EAST = 0x8;
            public static final int WALL_SOUTH_EAST = 0x10;
            public static final int WALL_SOUTH = 0x20;
            public static final int WALL_SOUTH_WEST = 0x40;
            public static final int WALL_WEST = 0x80;
            public static final int BLOCKED = 0x100;
            protected RSTile base;
            protected int[][] flags;
            protected int offX, offY;

            protected class Node {

                public int x, y;
                public Node prev;
                public double g, f;

                public Node(int x, int y) {
                    this.x = x;
                    this.y = y;
                    g = f = 0;
                }

                @Override
                public int hashCode() {
                    return (x << 4) | y;
                }

                @Override
                public boolean equals(Object o) {
                    if (o instanceof Node) {
                        Node n = (Node) o;
                        return x == n.x && y == n.y;
                    }
                    return false;
                }

                @Override
                public String toString() {
                    return "(" + x + "," + y + ")";
                }

                public RSTile toRSTile(int baseX, int baseY) {
                    return new RSTile(x + baseX, y + baseY);
                }
            }

            protected RSTile[] findPath(RSTile start, RSTile end) {
                return findPath(start, end, false);
            }

            protected RSTile[] findPath(RSTile start, RSTile end, boolean remote) {
                base = game.getMapBase();
                int base_x = base.getX(), base_y = base.getY();
                int curr_x = start.getX() - base_x, curr_y = start.getY() - base_y;
                int dest_x = end.getX() - base_x, dest_y = end.getY() - base_y;
                int plane = game.getPlane();
                flags = walking.getCollisionFlags(plane);
                RSTile offset = walking.getCollisionOffset(plane);
                offX = offset.getX();
                offY = offset.getY();
                if (flags == null || curr_x < 0 || curr_y < 0 || curr_x >= flags.length || curr_y >= flags.length) {
                    return null;
                } else if (dest_x < 0 || dest_y < 0 || dest_x >= flags.length || dest_y >= flags.length) {
                    remote = true;
                    if (dest_x < 0) {
                        dest_x = 0;
                    } else if (dest_x >= flags.length) {
                        dest_x = flags.length - 1;
                    }
                    if (dest_y < 0) {
                        dest_y = 0;
                    } else if (dest_y >= flags.length) {
                        dest_y = flags.length - 1;
                    }
                }

                // structs
                HashSet<Node> open = new HashSet<Node>();
                HashSet<Node> closed = new HashSet<Node>();
                Node curr = new Node(curr_x, curr_y);
                Node dest = new Node(dest_x, dest_y);

                curr.f = heuristic(curr, dest);
                open.add(curr);

                // search
                while (!open.isEmpty()) {
                    curr = lowest_f(open);
                    if (curr.equals(dest)) {
                        // reconstruct from pred tree
                        return path(curr, base_x, base_y);
                    }
                    open.remove(curr);
                    closed.add(curr);
                    for (Node next : successors(curr)) {
                        if (!closed.contains(next)) {
                            double t = curr.g + dist(curr, next);
                            boolean use_t = false;
                            if (!open.contains(next)) {
                                open.add(next);
                                use_t = true;
                            } else if (t < next.g) {
                                use_t = true;
                            }
                            if (use_t) {
                                next.prev = curr;
                                next.g = t;
                                next.f = t + heuristic(next, dest);
                            }
                        }
                    }
                }

                // no path
                if (!remote || calc.distanceTo(end) < 10) {
                    return null;
                }
                return findPath(start, pull(end));
            }

            private RSTile pull(RSTile tile) {
                RSTile p = getMyPlayer().getLocation();
                int x = tile.getX(), y = tile.getY();
                if (p.getX() < x) {
                    x -= 2;
                } else if (p.getX() > x) {
                    x += 2;
                }
                if (p.getY() < y) {
                    y -= 2;
                } else if (p.getY() > y) {
                    y += 2;
                }
                return new RSTile(x, y);
            }

            private double heuristic(Node start, Node end) {
                double dx = start.x - end.x;
                double dy = start.y - end.y;
                if (dx < 0) {
                    dx = -dx;
                }
                if (dy < 0) {
                    dy = -dy;
                }
                return dx < dy ? dy : dx;
                //double diagonal = dx > dy ? dy : dx;
                //double manhattan = dx + dy;
                //return 1.41421356 * diagonal + (manhattan - 2 * diagonal);
            }

            private double dist(Node start, Node end) {
                if (start.x != end.x && start.y != end.y) {
                    return 1.41421356;
                } else {
                    return 1.0;
                }
            }

            private Node lowest_f(Set<Node> open) {
                Node best = null;
                for (Node t : open) {
                    if (best == null || t.f < best.f) {
                        best = t;
                    }
                }
                return best;
            }

            private RSTile[] path(Node end, int base_x, int base_y) {
                LinkedList<RSTile> path = new LinkedList<RSTile>();
                Node p = end;
                while (p != null) {
                    path.addFirst(p.toRSTile(base_x, base_y));
                    p = p.prev;
                }
                return path.toArray(new RSTile[path.size()]);
            }

            private java.util.List<Node> successors(Node t) {
                LinkedList<Node> tiles = new LinkedList<Node>();
                int x = t.x, y = t.y;
                int f_x = x - offX, f_y = y - offY;
                int here = flags[f_x][f_y];
                int upper = flags.length - 1;
                if (f_y > 0 && (here & WALL_SOUTH) == 0 && (flags[f_x][f_y - 1] & BLOCKED) == 0) {
                    tiles.add(new Node(x, y - 1));
                }
                if (f_x > 0 && (here & WALL_WEST) == 0 && (flags[f_x - 1][f_y] & BLOCKED) == 0) {
                    tiles.add(new Node(x - 1, y));
                }
                if (f_y < upper && (here & WALL_NORTH) == 0 && (flags[f_x][f_y + 1] & BLOCKED) == 0) {
                    tiles.add(new Node(x, y + 1));
                }
                if (f_x < upper && (here & WALL_EAST) == 0 && (flags[f_x + 1][f_y] & BLOCKED) == 0) {
                    tiles.add(new Node(x + 1, y));
                }
                if (f_x > 0 && f_y > 0 && (here & (WALL_SOUTH_WEST | WALL_SOUTH | WALL_WEST)) == 0
                        && (flags[f_x - 1][f_y - 1] & BLOCKED) == 0
                        && (flags[f_x][f_y - 1] & (BLOCKED | WALL_WEST)) == 0
                        && (flags[f_x - 1][f_y] & (BLOCKED | WALL_SOUTH)) == 0) {
                    tiles.add(new Node(x - 1, y - 1));
                }
                if (f_x > 0 && f_y < upper && (here & (WALL_NORTH_WEST | WALL_NORTH | WALL_WEST)) == 0
                        && (flags[f_x - 1][f_y + 1] & BLOCKED) == 0
                        && (flags[f_x][f_y + 1] & (BLOCKED | WALL_WEST)) == 0
                        && (flags[f_x - 1][f_y] & (BLOCKED | WALL_NORTH)) == 0) {
                    tiles.add(new Node(x - 1, y + 1));
                }
                if (f_x < upper && f_y > 0 && (here & (WALL_SOUTH_EAST | WALL_SOUTH | WALL_EAST)) == 0
                        && (flags[f_x + 1][f_y - 1] & BLOCKED) == 0
                        && (flags[f_x][f_y - 1] & (BLOCKED | WALL_EAST)) == 0
                        && (flags[f_x + 1][f_y] & (BLOCKED | WALL_SOUTH)) == 0) {
                    tiles.add(new Node(x + 1, y - 1));
                }
                if (f_x > 0 && f_y < upper && (here & (WALL_NORTH_EAST | WALL_NORTH | WALL_EAST)) == 0
                        && (flags[f_x + 1][f_y + 1] & BLOCKED) == 0
                        && (flags[f_x][f_y + 1] & (BLOCKED | WALL_EAST)) == 0
                        && (flags[f_x + 1][f_y] & (BLOCKED | WALL_NORTH)) == 0) {
                    tiles.add(new Node(x + 1, y + 1));
                }
                return tiles;
            }
        }
    }
    /* GUI */

    private class GUI extends javax.swing.JFrame {

        private PaintOptions paintoptions;
        private ABSettings antibanoptions;
        private static final long serialVersionUID = 1L;
        private boolean isCanceled = false;

        public GUI(SettingsManager sm) {
            initComponents();
            sm.add("Location", location);
            sm.add("Mouse Low", mouselo);
            sm.add("Mouse High", mousehi);
            paintoptions = new PaintOptions(sm);
            antibanoptions = new ABSettings(sm);
            sm.load();
        }

        public int softenLocation() {
            return location.getSelectedIndex();
        }

        public boolean isCanceled() {
            return isCanceled;
        }

        private void initComponents() {

            jLabel1 = new javax.swing.JLabel();
            jLabel2 = new javax.swing.JLabel();
            location = new javax.swing.JComboBox();
            jLabel3 = new javax.swing.JLabel();
            mouselo = new javax.swing.JComboBox();
            mousehi = new javax.swing.JComboBox();
            jLabel4 = new javax.swing.JLabel();
            paint = new javax.swing.JButton();
            antiban = new javax.swing.JButton();
            start = new javax.swing.JButton();
            cancel = new javax.swing.JButton();

            setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

            jLabel1.setFont(new java.awt.Font("Tahoma", 1, 18)); // NOI18N
            jLabel1.setText("Aut0ClaySoftener");

            jLabel2.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
            jLabel2.setText("Location:");

            location.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
            location.setModel(new javax.swing.DefaultComboBoxModel(new String[]{"West Falador", "Edgeville"}));

            jLabel3.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
            jLabel3.setText("Mouse Lo / Mouse Hi:");

            mouselo.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
            mouselo.setModel(new javax.swing.DefaultComboBoxModel(new String[]{"0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "20", "21", "22", "23", "24", "25"}));
            mouselo.setSelectedIndex(5);

            mousehi.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
            mousehi.setModel(new javax.swing.DefaultComboBoxModel(new String[]{"0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "20", "21", "22", "23", "24", "25"}));
            mousehi.setSelectedIndex(10);

            jLabel4.setText("/");

            paint.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
            paint.setText("Paint Options");
            paint.addActionListener(new java.awt.event.ActionListener() {

                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    paintActionPerformed(evt);
                }
            });

            antiban.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
            antiban.setText("Antiban Options");
            antiban.addActionListener(new java.awt.event.ActionListener() {

                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    antibanActionPerformed(evt);
                }
            });

            start.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
            start.setText("Start!");
            start.addActionListener(new java.awt.event.ActionListener() {

                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    startActionPerformed(evt);
                }
            });

            cancel.setText("Cancel");
            cancel.addActionListener(new java.awt.event.ActionListener() {

                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    cancelActionPerformed(evt);
                }
            });

            javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
            getContentPane().setLayout(layout);
            layout.setHorizontalGroup(
                    layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGroup(layout.createSequentialGroup().addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGroup(layout.createSequentialGroup().addGap(55, 55, 55).addComponent(jLabel1)).addGroup(layout.createSequentialGroup().addContainerGap().addComponent(jLabel2).addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED).addComponent(location, 0, 199, Short.MAX_VALUE)).addGroup(layout.createSequentialGroup().addContainerGap().addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false).addComponent(antiban, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE).addComponent(jLabel3, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)).addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED).addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGroup(layout.createSequentialGroup().addComponent(mouselo, javax.swing.GroupLayout.PREFERRED_SIZE, 52, javax.swing.GroupLayout.PREFERRED_SIZE).addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED).addComponent(jLabel4, javax.swing.GroupLayout.PREFERRED_SIZE, 11, javax.swing.GroupLayout.PREFERRED_SIZE).addGap(3, 3, 3).addComponent(mousehi, 0, 50, Short.MAX_VALUE)).addComponent(paint, javax.swing.GroupLayout.DEFAULT_SIZE, 126, Short.MAX_VALUE)))).addContainerGap()).addGroup(layout.createSequentialGroup().addGap(45, 45, 45).addComponent(start, javax.swing.GroupLayout.DEFAULT_SIZE, 193, Short.MAX_VALUE).addGap(48, 48, 48)).addGroup(layout.createSequentialGroup().addGap(76, 76, 76).addComponent(cancel, javax.swing.GroupLayout.PREFERRED_SIZE, 130, javax.swing.GroupLayout.PREFERRED_SIZE).addContainerGap(80, Short.MAX_VALUE)));
            layout.setVerticalGroup(
                    layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGroup(layout.createSequentialGroup().addContainerGap().addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE).addGap(18, 18, 18).addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE).addComponent(jLabel2).addComponent(location, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)).addGap(11, 11, 11).addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE).addComponent(jLabel3).addComponent(mouselo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE).addComponent(mousehi, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE).addComponent(jLabel4, javax.swing.GroupLayout.PREFERRED_SIZE, 24, javax.swing.GroupLayout.PREFERRED_SIZE)).addGap(11, 11, 11).addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE).addComponent(antiban).addComponent(paint)).addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 35, Short.MAX_VALUE).addComponent(start, javax.swing.GroupLayout.PREFERRED_SIZE, 39, javax.swing.GroupLayout.PREFERRED_SIZE).addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED).addComponent(cancel).addGap(18, 18, 18)));

            pack();
        }

        private void paintActionPerformed(java.awt.event.ActionEvent evt) {
            if (!paintoptions.isVisible()) {
                paintoptions.setVisible(true);
            }
        }

        private void antibanActionPerformed(java.awt.event.ActionEvent evt) {
            if (!antibanoptions.isVisible()) {
                antibanoptions.setVisible(true);
            }
        }

        private void startActionPerformed(java.awt.event.ActionEvent evt) {
            isCanceled = false;
            paintoptions.dispose();
            antibanoptions.dispose();
            this.dispose();
            setVars();
        }

        private void cancelActionPerformed(java.awt.event.ActionEvent evt) {
            isCanceled = true;
            paintoptions.dispose();
            antibanoptions.dispose();
            this.dispose();
        }

        private void setVars() {
            clicks = paintoptions.clicks();
            defmouse = paintoptions.defaultMouse();
            rotmouse = paintoptions.rotateMouse();
            showmouse = paintoptions.showMouse();
            paths = paintoptions.drawPaths();
            stats = paintoptions.drawStats();
            beziers = antibanoptions.useBeziers();
            shortsplines = antibanoptions.shortSplines();
            numABThreads = antibanoptions.ABThreads();
            mouseLo = mouselo.getSelectedIndex();
            mouseHi = mousehi.getSelectedIndex();
        }
        // Variables declaration - do not modify
        private javax.swing.JButton antiban;
        private javax.swing.JButton cancel;
        private javax.swing.JLabel jLabel1;
        private javax.swing.JLabel jLabel2;
        private javax.swing.JLabel jLabel3;
        private javax.swing.JLabel jLabel4;
        private javax.swing.JComboBox location;
        private javax.swing.JComboBox mousehi;
        private javax.swing.JComboBox mouselo;
        private javax.swing.JButton paint;
        private javax.swing.JButton start;
        // End of variables declaration
    }

    public class PaintOptions extends javax.swing.JFrame {

        /**
		 * 
		 */
		private static final long serialVersionUID = -2632279993945486958L;
		public PaintOptions(SettingsManager sm) {
            initComponents();
            sm.add("Show Mouse", showmouse);
            sm.add("Show Stats", showstats);
            sm.add("Show Clicks", showclicks);
            sm.add("Draw Paths", showpaths);
            sm.add("Default Mouse", defaultmouse);
            sm.add("NeXus Mouse", nexusmouse);
            sm.add("Rotate Mouse", rotatemouse);
        }

        public boolean clicks() {
            return showmouse.isSelected() && showclicks.isSelected();
        }

        public boolean defaultMouse() {
            return showmouse.isSelected() && defaultmouse.isSelected();
        }

        public boolean rotateMouse() {
            return showmouse.isSelected() && rotatemouse.isSelected();
        }

        public boolean showMouse() {
            return showmouse.isSelected();
        }

        public boolean drawPaths() {
            return showpaths.isSelected();
        }

        public boolean drawStats() {
            return showstats.isSelected();
        }

        private void initComponents() {

            buttonGroup1 = new javax.swing.ButtonGroup();
            showmouse = new javax.swing.JRadioButton();
            showstats = new javax.swing.JRadioButton();
            showclicks = new javax.swing.JRadioButton();
            showpaths = new javax.swing.JRadioButton();
            defaultmouse = new javax.swing.JRadioButton();
            nexusmouse = new javax.swing.JRadioButton();
            rotatemouse = new javax.swing.JRadioButton();
            ok = new javax.swing.JButton();

            showmouse.setSelected(true);
            showmouse.setText("Paint Mouse");
            showmouse.addActionListener(new java.awt.event.ActionListener() {

                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    showmouseActionPerformed(evt);
                }
            });

            showstats.setSelected(true);
            showstats.setText("Paint Statistics (Runtime, Profit, etc)");

            showclicks.setSelected(true);
            showclicks.setText("Paint Mouse Clicks");

            showpaths.setText("Paint Walk Paths");

            buttonGroup1.add(defaultmouse);
            defaultmouse.setSelected(true);
            defaultmouse.setText("Mouse Type: Wacky colors and lines. Best with rotation");

            buttonGroup1.add(nexusmouse);
            nexusmouse.setText("Mouse Type: iBot (NeXus) mouse");

            rotatemouse.setSelected(true);
            rotatemouse.setText("Rotate Mouse");

            ok.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
            ok.setText("OK");
            ok.addActionListener(new java.awt.event.ActionListener() {

                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    okActionPerformed(evt);
                }
            });

            javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
            getContentPane().setLayout(layout);
            layout.setHorizontalGroup(
                    layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGroup(layout.createSequentialGroup().addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE).addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addComponent(showmouse).addComponent(showstats).addComponent(showclicks).addComponent(showpaths)).addGap(32, 32, 32).addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false).addComponent(nexusmouse).addComponent(rotatemouse).addComponent(defaultmouse).addGroup(layout.createSequentialGroup().addComponent(ok, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE).addContainerGap()))));
            layout.setVerticalGroup(
                    layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGroup(layout.createSequentialGroup().addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGroup(layout.createSequentialGroup().addContainerGap().addComponent(showmouse).addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED).addComponent(showstats).addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED).addComponent(showclicks).addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED).addComponent(showpaths)).addGroup(layout.createSequentialGroup().addComponent(defaultmouse).addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED).addComponent(nexusmouse).addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED).addComponent(rotatemouse).addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED).addComponent(ok))).addContainerGap(11, Short.MAX_VALUE)));

            pack();
        }

        private void okActionPerformed(java.awt.event.ActionEvent evt) {
            this.setVisible(false);
        }

        private void showmouseActionPerformed(java.awt.event.ActionEvent evt) {
            boolean b = showmouse.isSelected();
            nexusmouse.setEnabled(b);
            defaultmouse.setEnabled(b);
            rotatemouse.setEnabled(b);
        }
        // Variables declaration - do not modify
        private javax.swing.ButtonGroup buttonGroup1;
        private javax.swing.JRadioButton defaultmouse;
        private javax.swing.JRadioButton nexusmouse;
        private javax.swing.JButton ok;
        private javax.swing.JRadioButton rotatemouse;
        private javax.swing.JRadioButton showclicks;
        private javax.swing.JRadioButton showmouse;
        private javax.swing.JRadioButton showpaths;
        private javax.swing.JRadioButton showstats;
        // End of variables declaration
    }

    public class ABSettings extends javax.swing.JFrame {

        /**
		 * 
		 */
		private static final long serialVersionUID = 7138312375117656560L;
		public ABSettings(SettingsManager sm) {
            initComponents();
            sm.add("Use Bezier Curves", beziercurves);
            sm.add("Use short splines to points on a linde", shortsplines);
            sm.add("Number of Antiban Threads", numABthreads);
        }

        public boolean useBeziers() {
            return beziercurves.isSelected();
        }

        public boolean shortSplines() {
            return shortsplines.isSelected();
        }

        public int ABThreads() {
            return numABthreads.getSelectedIndex();
        }

        private void initComponents() {

            jLabel1 = new javax.swing.JLabel();
            apimethods = new javax.swing.JRadioButton();
            beziercurves = new javax.swing.JRadioButton();
            shortsplines = new javax.swing.JRadioButton();
            jLabel2 = new javax.swing.JLabel();
            numABthreads = new javax.swing.JComboBox();
            ok = new javax.swing.JButton();

            jLabel1.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
            jLabel1.setText("Mouse Spline Generation (How the mouse moves)");

            apimethods.setSelected(true);
            apimethods.setText("RSBot's API's Methods");
            apimethods.setEnabled(false);

            beziercurves.setSelected(true);
            beziercurves.setText("Cubic Bezier Curves (A basic curve with 4 control points)");

            shortsplines.setSelected(true);
            shortsplines.setText("<html><body><font size=\\\"3\\\">Short Splines to and from points on a straight line (Generates<br/>the splines with RSBot's methods. The mouse moves fairly straight<br/>and very squiggly)</font></body></html>");

            jLabel2.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
            jLabel2.setText("<html><body><font size=\\\"3\\\">Number of Antiban Threads: <br/>(1-2 is Recommended.)</font></body></html>");

            numABthreads.setModel(new javax.swing.DefaultComboBoxModel(new String[]{"0", "1", "2", "3", "4", "5"}));
            numABthreads.setSelectedIndex(2);

            ok.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
            ok.setText("OK");
            ok.addActionListener(new java.awt.event.ActionListener() {

                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    okActionPerformed(evt);
                }
            });

            javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
            getContentPane().setLayout(layout);
            layout.setHorizontalGroup(
                    layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGroup(layout.createSequentialGroup().addContainerGap().addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING).addComponent(ok, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 343, Short.MAX_VALUE).addComponent(jLabel1, javax.swing.GroupLayout.Alignment.LEADING).addComponent(apimethods, javax.swing.GroupLayout.Alignment.LEADING).addComponent(beziercurves, javax.swing.GroupLayout.Alignment.LEADING).addComponent(shortsplines, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE).addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup().addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE).addGap(10, 10, 10).addComponent(numABthreads, 0, 177, Short.MAX_VALUE))).addContainerGap()));
            layout.setVerticalGroup(
                    layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGroup(layout.createSequentialGroup().addContainerGap().addComponent(jLabel1).addGap(10, 10, 10).addComponent(apimethods).addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED).addComponent(beziercurves).addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED).addComponent(shortsplines, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE).addGap(35, 35, 35).addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE).addComponent(numABthreads, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)).addGap(28, 28, 28).addComponent(ok, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE).addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)));

            pack();
        }

        private void okActionPerformed(java.awt.event.ActionEvent evt) {
            this.setVisible(false);
        }
        // Variables declaration - do not modify
        private javax.swing.JRadioButton apimethods;
        private javax.swing.JRadioButton beziercurves;
        private javax.swing.JLabel jLabel1;
        private javax.swing.JLabel jLabel2;
        private javax.swing.JComboBox numABthreads;
        private javax.swing.JButton ok;
        private javax.swing.JRadioButton shortsplines;
        // End of variables declaration
    }

    private class SettingsManager {

        private String name;
        private LinkedList<Pair> pairs = new LinkedList<Pair>();

        public SettingsManager(String name) {
            this.name = name;
        }

        public void add(String key, JComponent component) {
            pairs.add(new Pair(key, component));
        }

        public void load() {
            try {
                File file = new File(name);
                if (!file.exists()) {
                    return;
                }
                FileReader rd = new FileReader(file);
                Properties prop = new Properties();
                prop.load(rd);
                for (Pair pair : pairs) {
                    String value = prop.getProperty(pair.key);
                    if (value == null) {
                        continue;
                    }
                    if (pair.component instanceof JComboBox) {
                        ((JComboBox) pair.component).setSelectedIndex(Integer.parseInt(value));
                    } else if (pair.component instanceof JCheckBox) {
                        ((JCheckBox) pair.component).setSelected(Boolean.parseBoolean(value));
                    } else if (pair.component instanceof JTextField) {
                        ((JTextField) pair.component).setText(value);
                    } else if (pair.component instanceof JTextArea) {
                        ((JTextArea) pair.component).setText(value);
                    } else if (pair.component instanceof JRadioButton) {
                        ((JRadioButton) pair.component).setSelected(Boolean.parseBoolean(value));
                    }
                }
                rd.close();
            } catch (Exception e) {
            }
        }

        public void save() {
            try {
                File file = new File(name);
                FileWriter wr = new FileWriter(file);
                Properties prop = new Properties();
                for (Pair pair : pairs) {
                    String value = "";
                    if (pair.component instanceof JComboBox) {
                        value = Integer.toString(((JComboBox) pair.component).getSelectedIndex());
                    } else if (pair.component instanceof JCheckBox) {
                        value = Boolean.toString(((JCheckBox) pair.component).isSelected());
                    } else if (pair.component instanceof JTextField) {
                        value = ((JTextField) pair.component).getText();
                    } else if (pair.component instanceof JTextArea) {
                        value = ((JTextArea) pair.component).getText();
                    } else if (pair.component instanceof JRadioButton) {
                        value = Boolean.toString(((JRadioButton) pair.component).isSelected());
                    }
                    prop.setProperty(pair.key, value);
                }
                prop.store(wr, "SettingsManager by NoEffex, edited by icnhzabot. Feel free to use with credits.");
                wr.close();
            } catch (Exception e) {
            }
        }

        class Pair {

            String key;
            JComponent component;

            public Pair(String key, JComponent component) {
                this.key = key;
                this.component = component;
            }
        }
    }

    private class Timekeeper {

        long startTime = System.currentTimeMillis();
        long pausedTime = 0;
        long pausedTemp = 0;
        int state = 0;

        public int getState() {
            return state;
        }

        private void setPausedTime(long setTime) {
            pausedTime += setTime;
        }

        private long getPausedTime() {
            if (pausedTemp != 0) {
                return (System.currentTimeMillis() - pausedTemp);
            } else {
                return 0;
            }
        }

        public long getMillis() {
            if (state == 1) {
                return System.currentTimeMillis() - startTime - getPausedTime();
            } else {
                return System.currentTimeMillis() - startTime - pausedTime;
            }
        }

        public long getSeconds() {
            return this.getMillis() / 1000;
        }

        public long getMinutes() {
            return this.getSeconds() / 60;
        }

        public long getHours() {
            return this.getMinutes() / 60;
        }

        public String getRuntimeString() {
            StringBuilder builder = new StringBuilder();
            long HoursRan = this.getHours();
            long MinutesRan = this.getMinutes();
            long SecondsRan = this.getSeconds();
            MinutesRan = MinutesRan % 60;
            SecondsRan = SecondsRan % 60;
            if (HoursRan < 10) {
                builder.append("0");
            }
            builder.append(HoursRan);
            builder.append(":");
            if (MinutesRan < 10) {
                builder.append("0");
            }
            builder.append(MinutesRan);
            builder.append(":");
            if (SecondsRan < 10) {
                builder.append("0");
            }
            builder.append(SecondsRan);
            return builder.toString();
        }

        public void setPaused() {
            state = 1;
            pausedTemp = System.currentTimeMillis();
        }

        public void setResumed() {
            state = 0;
            this.setPausedTime(this.getPausedTime());
            pausedTemp = 0;
        }

        public long calcPerHour(final long i) {
            return calcPerHour((double) i);
        }

        public long calcPerHour(final double i) {
            final double elapsed_millis = this.getMillis();
            return (long) ((i / elapsed_millis) * 3600000);
        }
    }

    /*A camera handler for Antiban purposes*/
    private class Camera {

        private char up = KeyEvent.VK_UP;
        private char down = KeyEvent.VK_DOWN;
        private char left = KeyEvent.VK_LEFT;
        private char right = KeyEvent.VK_RIGHT;

        public void turnRandomly(long averageTimeout) {
            int sleep = Math.abs((int) (random(averageTimeout - 200, averageTimeout + 200)));
            char dir = (random(1, 2) == random(1, 2) ? left : right);
            new Thread(new charpresser(sleep, dir)).start();
        }

        public void pitchRandomly(long averageTimeout) {
            int pitch = random(0, 100);
            pitch = Math.abs(random(pitch - 20, pitch + 20));
            if (pitch > 100) {
                pitch = 100;
            }
            if (Math.abs(pitch - camera.getPitch()) >= 15) {
                camera.setPitch(pitch);
            } else {
                camera.setPitch(random(1, 2) == random(1, 2) ? true : false);
            }
        }

        public void moveRandomly(long averageTimeout, boolean doubledir) {
            try {
                int sleep = Math.abs((int) (random(averageTimeout - 200, averageTimeout + 200)));
                int sleep2 = Math.abs((int) (random(averageTimeout - 200, averageTimeout + 200)));
                char begin = 0;
                char next = 0;
                int rand = random(1, 4);
                int rand2 = random(1, 2);
                if (rand == 1) {
                    begin = right;
                    if (rand2 == 1) {
                        next = down;
                    } else if (rand2 == 2) {
                        next = up;
                    }
                } else if (rand == 2) {
                    begin = left;
                    if (rand2 == 1) {
                        next = down;
                    } else if (rand2 == 2) {
                        next = up;
                    }
                } else if (rand == 3) {
                    begin = up;
                    if (rand2 == 1) {
                        next = left;
                    } else if (rand2 == 2) {
                        next = right;
                    }
                } else if (rand == 4) {
                    begin = down;
                    if (rand2 == 1) {
                        next = left;
                    } else if (rand2 == 2) {
                        next = right;
                    }
                } else {
                    begin = up;
                    if (rand2 == 1) {
                        next = left;
                    } else if (rand2 == 2) {
                        next = right;
                    }
                }
                if (doubledir) {
                    new Thread(new charpresser(sleep, begin)).start();
                    sleep(random(0, 5));
                    new Thread(new charpresser(sleep2, next)).start();
                } else {
                    new Thread(new charpresser(sleep, begin)).start();
                }
            } catch (NullPointerException ignored) {
            }
        }

        public void lookAtRandThings() {
            try {
                RSObject[] allPossibleObjects = objects.getAll(new Filter<RSObject>() {

                    @Override
                    public boolean accept(RSObject o) {
                        return calc.distanceTo(o) <= 8 && (o.getType().equals(RSObject.Type.INTERACTABLE) || o.getType().equals(RSObject.Type.BOUNDARY));
                    }
                });
                RSObject objecttochoose = allPossibleObjects[random(0, allPossibleObjects.length - 1)];
                RSNPC[] allPossibleNPCs = npcs.getAll(new Filter<RSNPC>() {

                    @Override
                    public boolean accept(RSNPC npc) {
                        return calc.distanceTo(npc) <= 8 && npc.isValid();
                    }
                });
                RSNPC npctochoose = allPossibleNPCs[random(0, allPossibleNPCs.length - 1)];
                if (npctochoose == null || objecttochoose == null) {
                    npctochoose = allPossibleNPCs[0];
                    objecttochoose = allPossibleObjects[0];
                }
                boolean useobj = true;
                if (calc.distanceTo(npctochoose) <= 5) {
                    useobj = false;
                }
                if (calc.distanceTo(objecttochoose) <= 5) {
                    useobj = true;
                } else if (!(calc.distanceTo(npctochoose) <= 5)) {
                    useobj = (random(1, 2) == random(1, 2) ? true : false);
                }
                if (useobj) {
                    RSTile loc = objecttochoose.getLocation();
                    int angle = angleToTile(loc);
                    setAngle(angle);
                } else {
                    RSTile loc = npctochoose.getLocation();
                    int angle = angleToTile(loc);
                    setAngle(angle);
                }
            } catch (Exception e) {
            }
        }

        private void setAngle(int angle) {
            if (camera.getAngleTo(angle) > 5) {
                charpresser presser = new charpresser((char) KeyEvent.VK_LEFT);
                new Thread(presser).start();
                while (camera.getAngleTo(angle) >= random(5, 8)) {
                    sleep(10);
                }
                presser.stop = true;
                presser.stopThread();
                keyboard.releaseKey((char) KeyEvent.VK_LEFT);
            } else if (camera.getAngleTo(angle) <= -5) {
                charpresser presser = new charpresser((char) KeyEvent.VK_RIGHT);
                new Thread(presser).start();
                while (camera.getAngleTo(angle) <= -6) {
                    sleep(10);
                }
                presser.stop = true;
                presser.stopThread();
                keyboard.releaseKey((char) KeyEvent.VK_RIGHT);
            } else {
                camera.setAngle(angle);
            }
        }

        private int angleToTile(RSTile tile) {
            int angle = (calc.angleToTile(tile) - 90) % 360;
            return (angle < 0 ? angle + 360 : angle);
        }
    }

    /*Runnables*/
    private class charpresser implements Runnable {

        private char topress;
        private long tohold = -1;
        public boolean stop = false;

        public charpresser(char press) {
            topress = press;
        }

        public charpresser(long timeout, char press) {
            topress = press;
            tohold = timeout;
        }

        public void run() {
            try {
                Thread.sleep(random(0, 70));
                if (tohold != -1) {
                    keyboard.pressKey(topress);
                    Thread.sleep(tohold);
                    keyboard.releaseKey(topress);
                } else {
                    keyboard.pressKey(topress);
                    while (!stop) {
                        Thread.sleep(10);
                    }
                    keyboard.releaseKey(topress);
                }
            } catch (InterruptedException ie) {
            }
        }

        public void stopThread() {
            stop = true;
            Thread.interrupted();
        }
    }

    private class PriceLoader implements Runnable {

        public void run() {
            try {
                GEItem softclay = grandExchange.lookup(softClay);
                GEItem clayitem = grandExchange.lookup(clay);
                if (softclay == null || clayitem == null) {
                    return;
                }
                int softprice = softclay.getGuidePrice();
                int price = clayitem.getGuidePrice();
                if (softprice == -1 || price == -1) {
                    return;
                }
                softClayPrice = softprice;
                clayPrice = price;
                URL mouseurl = new URL("http://i49.tinypic.com/35bh2rq.png");
                URL clickedurl = new URL("http://i50.tinypic.com/rk0b3r.png");
                nmouse = ImageIO.read(mouseurl);
                nclicked = ImageIO.read(clickedurl);
            } catch (MalformedURLException mue) {
                return;
            } catch (IOException ioe) {
                return;
            }
        }
    }

    private class CameraMovementChecker implements Runnable {

        private int x = 0, y = 0, z = 0;
        private boolean movement = false;

        public CameraMovementChecker() {
            new Thread(this).start();
        }

        private void sleep(int t) {
            try {
                Thread.sleep(t);
            } catch (InterruptedException e) {
            }
        }

        public void run() {
            while (running) {
                try {
                    if (isPaused()) {
                        sleep(random(900, 1100));
                        continue;
                    }
                    x = camera.getX();
                    y = camera.getY();
                    z = camera.getZ();
                    if (camera.getX() != x || camera.getY() != y || camera.getZ() != z) {
                        movement = true;
                    } else {
                        movement = false;
                    }
                    sleep(random(200, 300));
                    if (camera.getX() != x || camera.getY() != y || camera.getZ() != z) {
                        movement = true;
                    } else {
                        movement = false;
                    }
                } catch (Exception e) {
                }
            }
        }

        public boolean movedRecently() {
            return movement;
        }
    }

    private class MouseMovementChecker implements Runnable {

        public boolean movement = false;
        private long time = 0, mouseTime = 0;
        private Point mousePoint = new Point(-1, -1);

        private void sleep(int t) {
            try {
                Thread.sleep(t);
            } catch (InterruptedException e) {
            }
        }

        public MouseMovementChecker() {
            new Thread(this).start();
        }

        public void run() {
            while (running) {
                try {
                    if (isPaused()) {
                        sleep(random(900, 1100));
                        continue;
                    }
                    time = System.currentTimeMillis();
                    mouseTime = mouse.getPressTime();
                    mousePoint = mouse.getLocation();
                    if (time - mouseTime <= 900) {
                        movement = true;
                    } else {
                        movement = false;
                    }
                    sleep(random(100, 150));
                    if (System.currentTimeMillis() - mouseTime >= 1100) {
                        movement = false;
                    } else {
                        movement = true;
                    }
                    sleep(random(200, 150));
                    if (System.currentTimeMillis() - mouseTime >= 1225) {
                        movement = false;
                    } else {
                        movement = true;
                    }
                    if (!mousePoint.equals(mouse.getLocation())) {
                        movement = true;
                    } else {
                        movement = false;
                    }
                    if (System.currentTimeMillis() - mouse.getPressTime() >= 1000) {
                        movement = false;
                    } else {
                        movement = true;
                    }
                } catch (Exception e) {
                }
            }
        }

        public boolean movedRecently() {
            return movement && ((System.currentTimeMillis() - mousemovet) >= 800);
        }
    }

    private class antiban implements Runnable {

        private void sleep(long timeout) {
            try {
                Thread.sleep(timeout);
            } catch (InterruptedException ie) {
            }
        }

        public antiban() {
            switch (numABThreads) {
                case 0: {
                    break;
                }
                case 1: {
                    new Thread(this).start();
                    break;
                }
                case 2: {
                    new Thread(this).start();
                    new Thread(this).start();
                    break;
                }
                case 3: {
                    new Thread(this).start();
                    new Thread(this).start();
                    new Thread(this).start();
                    break;
                }
                case 4: {
                    new Thread(this).start();
                    new Thread(this).start();
                    new Thread(this).start();
                    new Thread(this).start();
                    break;
                }
                case 5: {
                    new Thread(this).start();
                    new Thread(this).start();
                    new Thread(this).start();
                    new Thread(this).start();
                    new Thread(this).start();
                    break;
                }
                default: {
                    new Thread(this).start();
                    break;
                }
            }
        }

        public void kill() {
            running = false;
            Thread.interrupted();
        }

        public void run() {
            while (running) {
                try {
                    sleep(random(90, 210));
                    if (isPaused()) {
                        sleep(random(900, 1100));
                        continue;
                    }
                    if (!game.isLoggedIn() || game.isWelcomeScreen() || game.isLoginScreen()) {
                        env.enableRandom("Login");
                        sleep(random(900, 1100));
                        continue;
                    }
                    int rand = random(1, 45);
                    switch (rand) {
                        case 1: {
                            if (canAB && !mouseCheck.movedRecently() && !cameraCheck.movedRecently()) {
                                this.doAntiban();
                            }
                        }
                        break;
                        case 2:
                        case 3: {
                            if (canAB && !mouseCheck.movedRecently()) {
                                this.antibanmouse();
                            }
                        }
                        break;
                        case 4:
                        case 5: {
                            if (canAB && !cameraCheck.movedRecently()) {
                                this.antibancamera();
                            }
                        }
                        break;
                    }
                } catch (Exception e) {
                } catch (Error e) {
                }
                sleep(random(90, 210));
            }
        }

        public void antibanmouse() {
            int rand = random(1, 26);
            switch (rand) {
                case 1: {
                    mouse.moveSlightly();
                    mouse.moveSlightly();
                    mousemovet = System.currentTimeMillis();
                    break;
                }
                case 2: {
                    if ((random(1, 2) == random(1, 2) || random(1, 3) == random(1, 3)) || !calc.pointOnScreen(mouse.getLocation())) {
                        Point dest = new Point(random(random(0, game.getWidth()), random(game.getWidth() / 2, 150)), random(random(0, game.getHeight()), random(game.getHeight() / 2, 150)));
                        mouseMove(dest);
                    } else {
                        mouse.moveRandomly(random(15, 300));
                    }
                    mousemovet = System.currentTimeMillis();
                    break;
                }
                case 3: {
                    Point p = new Point(random(175, 325), random(75, 225));
                    mouseMove(p);
                    mousemovet = System.currentTimeMillis();
                    break;
                }
                case 4: {
                    mouse.moveSlightly();
                    mouse.moveRandomly(random(30, 39), random(40, 60));
                    mousemovet = System.currentTimeMillis();
                    break;
                }
                case 5: {
                    mouse.moveSlightly();
                    mouse.moveSlightly();
                    mouse.moveSlightly();
                    mouse.moveSlightly();
                    mousemovet = System.currentTimeMillis();
                    break;
                }
                case 6: {
                    Point p = new Point(random(120, 350), random(50, 300));
                    mouseMove(p);
                    mouse.moveRandomly(random(5, 10), random(20, 30));
                    mousemovet = System.currentTimeMillis();
                    break;
                }
                case 7: {
                    mouse.moveOffScreen();
                    mouse.moveSlightly();
                    mousemovet = System.currentTimeMillis();
                    break;
                }
                case 8: {
                    mouse.moveRandomly(random(5, 10), random(20, 30));
                    mouse.moveOffScreen();
                    mousemovet = System.currentTimeMillis();
                    break;
                }
                case 9: {
                    mouse.moveSlightly();
                    mouse.moveRandomly(random(5, 10), random(20, 30));
                    mouse.moveSlightly();
                    mousemovet = System.currentTimeMillis();
                    break;
                }
                case 10: {
                    Point p = new Point(random(40, 200), random(10, 200));
                    mouseMove(p);
                    mousemovet = System.currentTimeMillis();
                    break;
                }
                case 11: {
                    int rand2 = random(1, 6);
                    switch (rand2) {
                        case 1: {
                            mouse.moveSlightly();
                            break;
                        }
                        case 2: {
                            mouse.moveRandomly(random(15, 300));
                            break;
                        }
                        case 3: {
                            Point p = new Point(random(40, 200), random(10, 200));
                            mouseMove(p);
                            break;
                        }
                        case 4: {
                            Point p = new Point(random(175, 325), random(75, 225));
                            mouseMove(p);
                            break;
                        }
                        case 5: {
                            mouse.moveRandomly(random(5, 10), random(20, 30));
                            break;
                        }
                    }
                    mouse.moveOffScreen();
                    mousemovet = System.currentTimeMillis();
                    break;
                }
                case 12: {
                    /*Wiggle Mouse*/
                    Point loc = mouse.getLocation();
                    int x = random(500, 650);
                    int y = random(300, 420);
                    mouseMove(new Point(x, y));
                    int randm = random(1, 2);
                    sleep(random(38, 642));
                    if (randm == 1) {
                        x = Math.abs(x - random(75, 175));
                        int random = random(1, 2);
                        if (random == 1) {
                            y = Math.abs(y - random(75, 175));
                        } else {
                            y = Math.abs(y + random(75, 175));
                        }
                    } else {
                        x = Math.abs(x + random(75, 175));
                        int random = random(1, 2);
                        if (random == 1) {
                            y = Math.abs(y + random(75, 175));
                        } else {
                            y = Math.abs(y - random(75, 175));
                        }
                    }
                    mouseMove(new Point(x, y));
                    sleep(random(0, 320));
                    x = loc.x;
                    y = loc.y;
                    int rand2 = random(1, 2);
                    if (rand2 == 1) {
                        x = Math.abs(x - random(50, 200));
                        int random = random(1, 2);
                        if (random == 1) {
                            y = Math.abs(y - random(50, 200));
                        } else {
                            y = Math.abs(y + random(50, 200));
                        }
                    } else {
                        x = Math.abs(x + random(50, 200));
                        int random = random(1, 2);
                        if (random == 1) {
                            y = Math.abs(y + random(50, 200));
                        } else {
                            y = Math.abs(y - random(50, 200));
                        }
                    }
                    mouseMove(new Point(x, y));
                    sleep(random(0, 200));
                    mousemovet = System.currentTimeMillis();
                    break;
                }
                case 13: {
                    /*Wiggle Mouse*/
                    Point loc = mouse.getLocation();
                    int x = random(300, 450);
                    int y = random(100, 220);
                    mouseMove(new Point(x, y));
                    int randm = random(1, 2);
                    sleep(random(38, 642));
                    if (randm == 1) {
                        x = Math.abs(x - random(75, 175));
                        int random = random(1, 2);
                        if (random == 1) {
                            y = Math.abs(y - random(75, 175));
                        } else {
                            y = Math.abs(y + random(75, 175));
                        }
                    } else {
                        x = Math.abs(x + random(75, 175));
                        int random = random(1, 2);
                        if (random == 1) {
                            y = Math.abs(y + random(75, 175));
                        } else {
                            y = Math.abs(y - random(75, 175));
                        }
                    }
                    mouseMove(new Point(x, y));
                    sleep(random(0, 320));
                    x = loc.x;
                    y = loc.y;
                    int rand2 = random(1, 2);
                    if (rand2 == 1) {
                        x = Math.abs(x - random(50, 200));
                        int random = random(1, 2);
                        if (random == 1) {
                            y = Math.abs(y - random(50, 200));
                        } else {
                            y = Math.abs(y + random(50, 200));
                        }
                    } else {
                        x = Math.abs(x + random(50, 200));
                        int random = random(1, 2);
                        if (random == 1) {
                            y = Math.abs(y + random(50, 200));
                        } else {
                            y = Math.abs(y - random(50, 200));
                        }
                    }
                    mouseMove(new Point(x, y));
                    sleep(random(0, 200));
                    mousemovet = System.currentTimeMillis();
                    break;
                }
            }
            if (rand >= 25) {
                mouse.moveSlightly();
            }
        }

        private void antibancamera() {
            if (bank.isOpen()) {
                return;
            }
            int rand = random(1, 30);
            switch (rand) {
                case 1: {
                    cameraHandler.pitchRandomly(random(500, 800));
                    break;
                }
                case 2: {
                    cameraHandler.pitchRandomly(random(100, 600));
                    break;
                }
                case 3: {
                    cameraHandler.pitchRandomly(random(800, 1100));
                    break;
                }
                case 4: {
                    cameraHandler.turnRandomly(random(500, 800));
                    break;
                }
                case 5: {
                    cameraHandler.turnRandomly(random(100, 600));
                    break;
                }
                case 6: {
                    cameraHandler.turnRandomly(random(800, 1100));
                    break;
                }
                case 7: {
                    cameraHandler.moveRandomly(random(600, 900), (random(1, 4) >= 2));
                    break;
                }
                case 8: {
                    cameraHandler.moveRandomly(random(300, 600), (random(1, 4) >= 2));
                    break;
                }
                case 9: {
                    cameraHandler.moveRandomly(random(950, 1475), (random(1, 5) >= 2));
                    break;
                }
                case 10: {
                    camera.setPitch(random(1, 2) == random(1, 2));
                    break;
                }
                case 11: {
                    camera.setPitch(random(1, 2) == random(1, 2));
                    break;
                }
                case 12: {
                    camera.moveRandomly(random(800, 1200));
                    break;
                }
                case 13: {
                    camera.setAngle(random(13, 43));
                    sleep(random(5, 30));
                    camera.setAngle(random(13, 43));
                    break;
                }
                case 14: {
                    camera.moveRandomly(random(200, 800));
                    break;
                }
                case 15: {
                    camera.moveRandomly(random(200, 800));
                    break;
                }
                case 16: {
                    cameraHandler.lookAtRandThings();
                    break;
                }
                case 17: {
                    cameraHandler.lookAtRandThings();
                    break;
                }
                case 18: {
                    try {
                        camera.turnTo(loc.getRSObject());
                    } catch (NullPointerException npe) {
                        cameraHandler.lookAtRandThings();
                    }
                    break;
                }
                case 19: {
                    try {
                        camera.turnTo(loc.getRSObject());
                    } catch (NullPointerException npe) {
                        cameraHandler.lookAtRandThings();
                    }
                    break;
                }
            }
            if (rand >= 28) {
                camera.moveRandomly(random(250, 500));
            }
        }

        private void doAntiban() {
            int rand = random(1, 6);
            if (rand <= 2 && !bank.isOpen()) {
                antibancamera();
            } else if (rand >= 5) {
                antibanmouse();
            } else {
                if (random(1, 3) >= random(1, 3)) {
                    antibanmouse();
                } else if (!bank.isOpen()) {
                    antibancamera();
                } else {
                    antibanmouse();
                }
            }
        }
    }

    private class InventoryListener implements Runnable {

        private long idle = 50;
        private int lastCount = 0;
        private int lastCount2 = 0;

        private void sleep(int t) {
            try {
                Thread.sleep(t);
            } catch (final Exception e) {
            }
        }

        public long idle() {
            return idle;
        }

        public void reset(long t) {
            idle = t;
        }

        public void run() {
            while (running) {
                sleep(90);
                if (isPaused()) {
                    continue;
                }
                if (lastCount < inventory.getCount(softClay) || lastCount2 > inventory.getCount(clay)) {
                    idle = 0;
                } else {
                    idle++;
                }
                lastCount = inventory.getCount(softClay);
                lastCount2 = inventory.getCount(clay);
            }
        }

        public void kill() {
            running = false;
            Thread.interrupted();
        }
    }
}
