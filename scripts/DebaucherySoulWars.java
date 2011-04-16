// <editor-fold defaultstate="collapsed" desc="Imports">
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.imageio.ImageIO;

import org.rsbot.event.events.MessageEvent;
import org.rsbot.event.listeners.MessageListener;
import org.rsbot.event.listeners.PaintListener;
import org.rsbot.script.Script;
import org.rsbot.script.ScriptManifest;
import org.rsbot.script.methods.Bank;
import org.rsbot.script.methods.Equipment;
import org.rsbot.script.methods.Game;
import org.rsbot.script.methods.Skills;
import org.rsbot.script.util.Filter;
import org.rsbot.script.util.Timer;
import org.rsbot.script.wrappers.RSArea;
import org.rsbot.script.wrappers.RSComponent;
import org.rsbot.script.wrappers.RSGroundItem;
import org.rsbot.script.wrappers.RSItem;
import org.rsbot.script.wrappers.RSModel;
import org.rsbot.script.wrappers.RSNPC;
import org.rsbot.script.wrappers.RSObject;
import org.rsbot.script.wrappers.RSPath;
import org.rsbot.script.wrappers.RSPlayer;
import org.rsbot.script.wrappers.RSTile;
// </editor-fold>

@ScriptManifest(authors = "Debauchery", name = "DebaucherySoulWars", version = 0.51, description = "Start the script in soulwars lobby")
public class DebaucherySoulWars extends Script implements MouseListener, MessageListener, PaintListener {

    // <editor-fold defaultstate="collapsed" desc="Constants & Variables">
    private final int randomTeamID = 42031, blueBarrierID = 42029,
            bandageID = 14640, pyresID = 8598, jelliesID = 8599,
            redBarrierID = 42030, bonesID = 14638;
    private int zeal, won, lost, drew, blueShouts, redShouts, kicked,
            maxiumTimeForBreak, maxiumTimeUntillBreak, minimiumTimeForBreak,
            minimiumTimeUntillBreak, specUsage, lowActivity = 250,
            EasternGraveyard = 0, WesternGraveyard = 0;
    private final int[] BarrierID = {42013, 42014, 42015, 42016, 42017, 42018},
            bandageTableID = {42023, 42024},
            barricadeID = {8600},
            blueAvatarID = {8597}, redAvatarID = {8596}, fragmentID = {
        14646, 15792}, arrowsID = {9242, 13280, 9142, 864, 863};
    private String result, task, breakHandlerStatus;
    @SuppressWarnings("unused")
    private boolean randomTeam, lastWonTeam, lastLostTeam, redTeam, blueTeam,
            clanChatTeam, takeBreak, attackAvatar, attackPyres, attackJellies,
            getSupplies, healOthers, pickUpBones, attackPlayers, randomStrat,
            pureMode, blueLast, redLast, inClan, buryAtGrave, pickUpArrows,
            weaponSpec, quickPrayer, attackEverywhere, enableSummoning,
            withdrawPouches, useScrolls, takingBreak, startedBreak,
            stoppedBreak, startScript, hide;
    private Summoning chosenFamiliar;
    private Location chosenLocation;
    private String chosenLocationString;
    private SWGUI gui;
    private Pitch pitch;
    private Angle angle;
    private BreakHandler breakHandler;
    private Timer run;
    private RSTile tempTile;
    private Strategies current;
    private RSPath path;
    private Rectangle hideRect = new Rectangle(504, 209, 14, 14);

    // </editor-fold>
    // <editor-fold defaultstate="collapsed" desc="Start">
    @Override
    public boolean onStart() {
        if (game.isLoggedIn()) {
            gui = new SWGUI();
            gui.setVisible(true);
            while (!startScript) {
                sleep(10);
            }
            gui.setVisible(false);
            run = new Timer(0);
            mouse.setSpeed(random(8, 10));
            angle = new Angle();
            pitch = new Pitch();
            pitch.start();
            angle.start();
            if (takeBreak) {
                breakHandler = new BreakHandler();
                breakHandler.start();
            }
            if (weaponSpec) {
                specUsage = specialUsage();
            }
            combat.setAutoRetaliate((pureMode ? false : true));
            mouse.setSpeed(random(3, 7));
            return true;
        } else {
            log(Color.RED, "Please log in before starting");
            return false;
        }
    }

    // </editor-fold>
    // <editor-fold defaultstate="collapsed" desc="Loop">
    @Override
    public int loop() {
        path = null;
        tempTile = null;
        if ((getMyLocation().equals(Location.OUTSIDE)
                || interfaces.get(Game.INTERFACE_LOGOUT_LOBBY).isValid() || !game.isLoggedIn()) && takingBreak) {
            task = "Waiting in lobby.";
            if (!interfaces.get(Game.INTERFACE_LOGOUT_LOBBY).isValid()
                    || game.isLoggedIn()) {
                sleep(1000, 12000);
                game.logout(true);
            }
            startedBreak = true;
            stoppedBreak = false;
            inClan = false;
            env.disbleRandoms();
        } else {
            env.enableRandoms();
            startedBreak = false;
            stoppedBreak = true;
            if (game.isLoggedIn()
                    && !interfaces.get(Game.INTERFACE_LOGOUT_LOBBY).isValid()) {
                if (interfaces.get(Game.INTERFACE_LEVEL_UP).isValid()) {
                    if (interfaces.canContinue()) {
                        interfaces.clickContinue();
                    }
                    sleep(100, 600);
                }
                if (getMyPlayer().getTeam() == 1) {
                    redLast = false;
                    blueLast = true;
                } else if (getMyPlayer().getTeam() == 2) {
                    redLast = true;
                    blueLast = false;
                }
                if (getMyLocation().equals(Location.OUTSIDE)) {
                    if (interfaces.getComponent(243, 4).isValid()
                            && interfaces.getComponent(243, 4).containsText(
                            "You were removed from the game due")) {
                        result = "drewLast";
                        lowActivity = lowActivity + 25;
                        kicked += 1;
                        interfaces.clickContinue();
                    }
                    if (enableSummoning) {
                        if (!summoning.isFamiliarSummoned()) {
                            if (inventory.contains(chosenFamiliar.getPouchID())) {
                                RSItem pouch = inventory.getItem(chosenFamiliar.getPouchID());
                                if (pouch != null
                                        && summoning.getSummoningPoints() <= 0) {
                                    pouch.doAction("Summon");
                                }
                            } else if (withdrawPouches) {
                                RSObject chest = objects.getNearest(Bank.BANK_CHESTS);
                                if (chest != null && !chest.isOnScreen()) {
                                    for (int i = 0; i > 5; i++) {
                                        walking.walkTileMM(chest.getLocation());
                                        if (!chest.isOnScreen()) {
                                            break;
                                        }
                                    }
                                }
                                bank.open();
                                if (bank.isOpen()) {
                                    bank.withdraw(chosenFamiliar.getPouchID(),
                                            5);
                                }
                            }
                        }
                    }
                    if (clanChatTeam && !inClan) {
                        friendChat.joinLastChannel();
                        inClan = friendChat.isInChannel();
                    }
                    current = null;
                    WesternGraveyard = 0;
                    EasternGraveyard = 0;
                    task = "Joining Team.";
                    joinTeam();
                    if (waitTime() > 0) {
                        sleep(waitTime() * 50000);
                    }
                } else if (getMyLocation().equals(Location.RED_WAITING)
                        || getMyLocation().equals(Location.BLUE_WAITING)) {
                    current = null;
                    task = "Waiting for next game.";
                    waitingAntiban();
                } else if (getMyLocation().equals(Location.RED_SPAWN)
                        || getMyLocation().equals(Location.BLUE_SPAWN)
                        || getMyLocation().equals(Location.EAST_GRAVE)
                        || getMyLocation().equals(Location.WEST_GRAVE)) {
                    current = null;
                    task = "Leaving grave or spawn.";
                    leave();
                    sleep(1000, 2000);
                    if (!listenTime()) {
                        blueShouts = 0;
                        redShouts = 0;
                    }
                } else {
                    chosenLocation = locationNameToObject(chosenLocationString);
                    if (game.getClientState() == Game.INDEX_LOBBY_SCREEN
                            || !game.isLoggedIn()) {
                        return 1;
                    }
                    if (barricade()) {
                        return 1;
                    }
                    if (current == null) {
                        current = getStrategies();
                    } else {
                        switch (current) {
                            case PICKUP_BONES:
                                task = "Picking up bones.";
                                bonesStrat();
                                break;
                            case HEAL_PLAYERS:
                                task = "Healing others.";
                                healPlayersStrat();
                                break;
                            case ATTACK_AVATAR:
                                if (getActivityBarPercent() < lowActivity) {
                                    if (pickUpBones) {
                                        task = "Picking Bones(Low activity).";
                                        bonesStrat();
                                        break;
                                    }
                                } else {
                                    task = "Attack avatar.";
                                    avatarStrat();
                                }
                                break;
                            case ATTACK_PYRES:
                                if (getActivityBarPercent() < lowActivity) {
                                    if (pickUpBones) {
                                        task = "Picking Bones(Low activity).";
                                        bonesStrat();
                                        break;
                                    }
                                } else {
                                    task = "Attacking Pyres.";
                                    pyresStrat();
                                }
                                break;
                            case ATTACK_JELLIES:
                                if (getActivityBarPercent() < lowActivity) {
                                    if (pickUpBones) {
                                        task = "Picking Bones(Low activity).";
                                        bonesStrat();
                                        break;
                                    }
                                } else {
                                    task = "Attacking Jellies.";
                                    jelliesStrat();
                                }
                                break;
                            case ATTACK_PLAYERS:
                                if (getActivityBarPercent() < lowActivity) {
                                    if (pickUpBones) {
                                        task = "Picking Bones(Low activity).";
                                        bonesStrat();
                                        break;
                                    }
                                } else {
                                    task = "Attacking Players.";
                                    playersStrat();
                                }
                                break;
                        }
                    }
                }
            }
        }
        return 1;
    }

    // </editor-fold>
    // <editor-fold defaultstate="collapsed" desc="Methods">
    private int findClan() {
        sleep(1500, 1800);
        int blueUsers = 0;
        int redUsers = 0;
        String[] clanUsers;
        try {
            clanUsers = friendChat.getChannelUsers();
            for (int x = 0; x < clanUsers.length; x++) {
                RSPlayer temp = players.getNearest(clanUsers[x]);
                if (temp != null) {
                    RSTile tempTile = temp.getLocation();
                    if (tempTile != null) {
                        if (Location.BLUE_WAITING.containsTile(tempTile)) {
                            blueUsers++;
                        } else if (Location.RED_WAITING.containsTile(tempTile)) {
                            redUsers++;
                        }
                    }
                }
            }
            return (blueUsers > redUsers ? 1 : 2);
        } catch (Exception e) {
        }
        return 0;
    }

    private int waitTime() {
        try {
            if (interfaces.getComponent(211, 1).isValid()
                    && interfaces.getComponent(211, 1).containsText(
                    "You left a game of Soul Wars early")) {
                String getLine = interfaces.getComponent(211, 1).getText();
                String subLine = getLine.substring(
                        getLine.indexOf("wait ") + 5,
                        getLine.indexOf(" minutes"));
                return Integer.parseInt(subLine);
            }
        } catch (Exception e) {
        }
        return 0;
    }

    private int getOpponentAvatarLevel() {
        String s = null;
        try {
            if (getMyPlayer().getTeam() == 1) {
                s = interfaces.get(836).getComponent(14).getText();
                if (!s.equals("---")) {
                    return Integer.parseInt(s);
                }
            } else {
                s = interfaces.get(836).getComponent(10).getText();
                if (!s.equals("---")) {
                    return Integer.parseInt(s);
                }
            }
        } catch (Exception e) {
            return 100;
        }
        return 100;
    }

    private int getActivityBarPercent() {
        return settings.getSetting(1380);
    }

    private boolean controlsWesternGraveyard() {
        return (WesternGraveyard == getMyPlayer().getTeam());
    }

    private boolean controlsEasternGraveyard() {
        return (EasternGraveyard == getMyPlayer().getTeam());
    }

    private int chooseTeam() {
        if (clanChatTeam) {
            if (blueShouts != redShouts) {
                return (blueShouts > redShouts ? 1 : 2);
            } else {
                return findClan();
            }
        } else if (lastWonTeam || lastLostTeam) {
            if (result != null) {
                if (result.equals("drawLast")) {
                    return 0;
                } else if (result.equals("wonLast")) {
                    if (blueLast) {
                        return (lastWonTeam ? 1 : 2);
                    } else {
                        return (lastWonTeam ? 2 : 1);
                    }
                } else if (result.equals("lostLast")) {
                    if (redLast) {
                        return (lastWonTeam ? 1 : 2);
                    } else {
                        return (lastWonTeam ? 2 : 1);
                    }
                }
            }
        } else if (redTeam) {
            return 2;
        } else if (blueTeam) {
            return 1;
        } else if (randomTeam) {
            return 0;
        }
        return 0;
    }

    private void joinTeam() {
        interfaces.get(199).getComponent(0).getText();
        if (interfaces.get(211).getComponent(3).isValid()
                && interfaces.get(211).getComponent(3) != null) {
            interfaces.get(211).getComponent(3).doClick();
            sleep(500, 800);
        } else if (interfaces.get(228).getComponent(2).isValid()
                && interfaces.get(228).getComponent(2) != null) {
            interfaces.get(228).getComponent(2).doClick();
            sleep(500, 800);
        } else {
            switch (chooseTeam()) {
                case 0:
                    // random
                    RSObject randomTeam = objects.getNearest(randomTeamID);
                    if (randomTeam != null) {
                        RSTile randomTeamTile = randomTeam.getLocation();
                        if (randomTeamTile != null) {
                            if (randomTeam.isOnScreen()) {
                                switch (random(0, 2)) {
                                    case 0:
                                        if (getMyLocation().equals(Location.OUTSIDE)) {
                                            if (randomTeam.doAction("Join-team")) {
                                                sleep(500, 800);
                                            }
                                        }
                                        break;
                                    // Join-team
                                    case 1:
                                        RSModel mod = randomTeam.getModel();
                                        if (mod != null) {
                                            Point p = mod.getPoint();
                                            if (p != null) {
                                                mouse.hop(p);
                                                if (getMyLocation().equals(
                                                        Location.OUTSIDE)) {
                                                    mouse.click(true);
                                                    sleep(500, 800);
                                                }
                                            }
                                        }
                                        break;
                                }
                            } else if (!randomTeam.isOnScreen()
                                    && calc.distanceTo(randomTeamTile) <= 4) {
                                camera.getObjectAngle(randomTeam);
                            } else {
                                RSTile closestTileToRandomTeam = walking.getClosestTileOnMap(randomTeamTile);
                                if (closestTileToRandomTeam != null) {
                                    walking.walkTileMM(closestTileToRandomTeam);
                                    sleep(400, 800);
                                }
                            }
                        }
                    }
                    break;
                case 1:
                    // blue
                    RSObject blueBarrier = objects.getNearest(blueBarrierID);
                    if (blueBarrier != null) {
                        RSTile blueBarrierTile = blueBarrier.getLocation();
                        if (blueBarrierTile != null) {
                            if (blueBarrier.isOnScreen()) {
                                switch (random(0, 2)) {
                                    case 0:
                                        RSModel mod = blueBarrier.getModel();
                                        if (mod != null) {
                                            Point p = mod.getPoint();
                                            if (p != null) {
                                                mouse.hop(p);
                                                if (getMyLocation().equals(
                                                        Location.OUTSIDE)) {
                                                    mouse.click(true);
                                                    sleep(500, 800);
                                                }
                                            }
                                        }
                                        break;
                                    case 1:
                                        if (getMyLocation().equals(Location.OUTSIDE)) {
                                            blueBarrier.doAction("Pass");
                                        }
                                        sleep(500, 800);
                                        break;
                                }
                            } else if (!blueBarrier.isOnScreen()
                                    && calc.distanceTo(blueBarrierTile) <= 4) {
                                camera.getObjectAngle(blueBarrier);
                                camera.setPitch(true);
                            } else {
                                RSTile closestTileToBlueBarrier = walking.getClosestTileOnMap(blueBarrierTile);
                                if (closestTileToBlueBarrier != null) {
                                    walking.walkTileMM(closestTileToBlueBarrier);
                                    sleep(500, 800);
                                }
                            }
                        }
                    }
                    break;
                case 2:
                    // red
                    RSObject redBarrier = objects.getNearest(redBarrierID);
                    if (redBarrier != null) {
                        RSTile redBarrierTile = redBarrier.getLocation();
                        if (redBarrierTile != null) {
                            if (redBarrier.isOnScreen()) {
                                switch (random(0, 2)) {
                                    case 0:
                                        RSModel mod = redBarrier.getModel();
                                        if (mod != null) {
                                            Point p = mod.getPoint();
                                            if (p != null) {
                                                mouse.hop(p);
                                                if (getMyLocation().equals(
                                                        Location.OUTSIDE)) {
                                                    mouse.click(true);
                                                }
                                                sleep(500, 800);
                                            }
                                        }
                                        break;
                                    case 1:
                                        if (getMyLocation().equals(Location.OUTSIDE)) {
                                            if (redBarrier.doAction("Pass")) {
                                                sleep(500, 800);
                                            }
                                        }
                                        break;
                                }
                            } else if (!redBarrier.isOnScreen()
                                    && calc.distanceTo(redBarrierTile) <= 4) {
                                camera.getObjectAngle(redBarrier);
                                camera.setPitch(true);
                            } else {
                                RSTile closestTileToRedBarrier = walking.getClosestTileOnMap(redBarrierTile);
                                if (closestTileToRedBarrier != null) {
                                    walking.walkTileMM(closestTileToRedBarrier);
                                    sleep(500, 800);
                                }
                            }
                        }
                    }
                    break;
            }
        }

    }

    private void waitingAntiban() {
        switch (random(0, 7)) {
            case 0:
                sleep(600, 700);
                break;
            case 1:
                mouse.moveRandomly(200);
                break;
            case 2:
                camera.moveRandomly(random(1000, 1200));
                break;
            case 3:
                mouse.moveOffScreen();
                sleep(1200, 2000);
                break;
            case 4:
                sleep(800, 1000);
                break;
            case 5:
                if (getMyLocation().equals(Location.RED_WAITING)) {
                    RSTile center = Location.RED_WAITING.getRSArea().getCentralTile();
                    if (center != null && calc.distanceTo(center) > 2) {
                        walking.walkTileMM(center, 2, 2);
                    }
                } else if (getMyLocation().equals(Location.BLUE_WAITING)) {
                    RSTile center = Location.BLUE_WAITING.getRSArea().getCentralTile();
                    if (center != null && calc.distanceTo(center) > 2) {
                        walking.walkTileMM(center, 2, 2);
                    }
                }
                break;
            case 6:
                sleep(200);
                break;
        }
    }

    private void leave() {
        try {
            RSObject Barrier = objects.getNearest(BarrierID);
            if (getMyLocation().equals(Location.RED_SPAWN)
                    || getMyLocation().equals(Location.BLUE_SPAWN)
                    || getMyLocation().equals(Location.EAST_GRAVE)
                    || getMyLocation().equals(Location.WEST_GRAVE)) {
                if (getMyPlayer().getNPCID() == 8623) {
                    if (random(1, 100) < 25) {
                        if (Barrier != null) {
                            walking.walkTileMM(Barrier.getLocation());
                            sleep(600, 1000);
                        }
                    }
                    if (random(1, 100) < 10) {
                        if (getMyLocation().equals(Location.RED_SPAWN)
                                || getMyLocation().equals(Location.BLUE_SPAWN)
                                || getMyLocation().equals(Location.EAST_GRAVE)
                                || getMyLocation().equals(Location.WEST_GRAVE)) {
                            Barrier.doAction("Pass");
                        }
                    }
                    sleep(100, 600);
                } else if (Barrier != null) {
                    if (!Barrier.isOnScreen()
                            && calc.distanceTo(Barrier.getLocation()) < 2) {
                        camera.turnTo(Barrier);
                    }
                    if (!Barrier.isOnScreen()) {
                        walking.walkTileMM(Barrier.getLocation());
                    }
                    if (Barrier.isOnScreen()
                            && getMyPlayer().getAnimation() != 1) {
                        RSModel mod = Barrier.getModel();
                        camera.turnTo(Barrier);
                        sleep(2000, 4000);
                        switch (random(0, 5)) {
                            case 2:
                                if (mod != null) {
                                    Point p = mod.getPoint();
                                    if (p != null) {
                                        if (getMyLocation().equals(
                                                Location.RED_SPAWN)
                                                || getMyLocation().equals(
                                                Location.BLUE_SPAWN)
                                                || getMyLocation().equals(
                                                Location.EAST_GRAVE)
                                                || getMyLocation().equals(
                                                Location.WEST_GRAVE)) {
                                            mouse.hop(p);
                                            mouse.click(true);
                                        }
                                    }
                                }
                                break;
                            default:
                                if (getMyLocation().equals(Location.RED_SPAWN)
                                        || getMyLocation().equals(
                                        Location.BLUE_SPAWN)
                                        || getMyLocation().equals(
                                        Location.EAST_GRAVE)
                                        || getMyLocation().equals(
                                        Location.WEST_GRAVE)) {
                                    Barrier.doAction("Pass");
                                }
                                break;
                        }
                    }
                }
            }
            sleep(1000, 2000);
        } catch (Exception e) {
        }
    }

    private boolean getSupplies() {
        if (inventory.containsOneOf(bandageID)) {
            return true;
        }
        RSObject bandages = objects.getNearest(bandageTableID);
        int supplyNum = random(28, 100);
        if (bandages != null && getMyPlayer().getInteracting() == null) {
            if (!bandages.isOnScreen()) {
                camera.turnTo(bandages);
            }
            if (bandages.isOnScreen()) {
                if (bandages.doAction("Take-x")) {
                    sleep(2500, 3000);
                    if (calc.distanceTo(bandages) <= 2
                            && getMyPlayer().getInteracting() == null) {
                        if (interfaces.getComponent(752, 5) != null
                                && !interfaces.getComponent(752, 5).getText().contains(String.valueOf(supplyNum))
                                && !getMyPlayer().isInCombat()) {
                            keyboard.sendText(String.valueOf(supplyNum), false);
                            sleep(1000, 1400);
                        }
                        if (interfaces.getComponent(752, 5) != null
                                && interfaces.getComponent(752, 5).getText().contains(String.valueOf(supplyNum))
                                && !getMyPlayer().isInCombat()) {
                            keyboard.sendText("", true);
                            sleep(1800, 2000);
                        }
                    }
                }
            } else {
                if (!getMyPlayer().isMoving()) {
                    walking.walkTileMM(walking.getClosestTileOnMap(bandages.getLocation()));
                    sleep(1000, 2000);
                }
            }
        }
        return false;
    }

    public RSPlayer teamMate() {
        return players.getNearest(new Filter<RSPlayer>() {

            @Override
            public boolean accept(RSPlayer player) {
                return player != null
                        && player.getTeam() == getMyPlayer().getTeam()
                        && player.getHPPercent() < 100;
            }
        });
    }

    private void healOthers() {
        camera.setPitch(true);
        try {
            if (inventory.getSelectedItem() == null
                    && inventory.getSelectedItem().getID() == bandageID) {
                RSItem bandage = inventory.getItem(bandageID);
                if (bandage != null) {
                    bandage.doAction("Use");
                }
            } else {
                switch (random(1, 5)) {
                    case 1:
                        if (teamMate() != null) {
                            RSModel model = teamMate().getModel();
                            if (model != null) {
                                Point p = model.getPoint();
                                if (p != null) {
                                    mouse.hop(p);
                                    mouse.click(true);
                                    sleep(3000, 5000);
                                }
                            }
                        }
                        break;
                    default:
                        opponent().doAction("Heal " + teamMate().getName());
                        sleep(3000, 5000);
                        break;
                }
            }
        } catch (Exception e) {
        }
    }

    public RSPlayer opponent() {
        return players.getNearest(new Filter<RSPlayer>() {

            @Override
            public boolean accept(RSPlayer player) {
                return player != null
                        && player.getTeam() != getMyPlayer().getTeam()
                        && player.getHPPercent() > 1;
            }
        });
    }

    private void attack() {
        camera.setPitch(true);
        try {
            RSPlayer opp = opponent();
            if (opp != null && Location.OBELISK.containsTile(opp.getLocation())) {
                if (getMyPlayer().getInteracting() == null) {
                    switch (random(1, 3)) {
                        case 1:
                            if (opp != null) {
                                RSModel model = opp.getModel();
                                if (model != null) {
                                    Point p = model.getPoint();
                                    if (p != null) {
                                        mouse.hop(p);
                                        mouse.click(true);
                                        sleep(3000, 5000);
                                    }
                                }
                            }
                            break;
                        case 2:
                            opp.doAction("Attack");
                            sleep(3000, 5000);
                            break;
                    }
                }
            } else {
                if (pickUpBones) {
                    if (getActivityBarPercent() < 1000 || buryAtGrave) {
                        RSGroundItem bones = groundItems.getNearest(bonesID);
                        bones.doAction("Take " + bones.getItem().getName());
                        return;
                    }
                }
                pickUpArrows();
                if (!getMyPlayer().isMoving()
                        || calc.distanceTo(walking.getDestination()) < 5) {
                    walking.walkTileMM(walking.getClosestTileOnMap((getMyPlayer().getTeam() == 1 ? new RSTile(
                            1901, 3231) : new RSTile(1871, 3233))));
                    sleep(1000, 3000);
                }
            }
        } catch (Exception e) {
        }
    }

    private void attackAvatar() {
        if (getMyPlayer().getTeam() == 1) {
            RSNPC avatar = npcs.getNearest(redAvatarID);
            if (avatar != null) {
                if (avatar.isOnScreen()) {
                    if (getMyPlayer().getInteracting() == null
                            || !getMyPlayer().getInteracting().getName().equals(avatar.getName())) {
                        avatar.doAction("Attack " + avatar.getName());
                        sleep(3000, 5000);
                    }
                } else if (avatar.isOnScreen() && calc.distanceTo(avatar) < 4) {
                    camera.getCharacterAngle(avatar);
                } else {
                    RSTile avatarLoc = avatar.getLocation();
                    if (avatarLoc != null) {
                        if (calc.distanceTo(walking.getDestination()) < 4
                                || !getMyPlayer().isMoving()) {
                            walking.walkTileMM(walking.getClosestTileOnMap(avatarLoc));
                        }
                    }
                }
            }
        } else {
            RSNPC avatar = npcs.getNearest(blueAvatarID);
            if (avatar != null) {
                if (avatar.isOnScreen()) {
                    if (getMyPlayer().getInteracting() == null
                            || !getMyPlayer().getInteracting().getName().equals(avatar.getName())) {
                        avatar.doAction("Attack");
                        sleep(3000, 5000);
                    }
                } else if (avatar.isOnScreen() && calc.distanceTo(avatar) < 4) {
                    camera.getCharacterAngle(avatar);
                } else {
                    RSTile avatarLoc = avatar.getLocation();
                    if (avatarLoc != null) {
                        if (calc.distanceTo(walking.getDestination()) < 4
                                || !getMyPlayer().isMoving()) {
                            walking.walkTileMM(walking.getClosestTileOnMap(avatarLoc));
                        }
                    }
                }
            }
        }
    }

    private void buryBones() {
        if (getActivityBarPercent() < random(600, 800)) {
            if (inventory.containsOneOf(bonesID)) {
                RSItem inventBones = inventory.getItem(bonesID);
                if (inventBones != null) {
                    inventBones.doAction("Bury");
                    sleep(700, 1200);
                }
            }
        }
    }

    private void destroyBones() {
        if (inventory.containsOneOf(bonesID)) {
            RSItem inventBones = inventory.getItem(bonesID);
            if (inventBones != null) {
                inventBones.doAction("Destroy");
                sleep(700, 1200);
                if (interfaces.get(94).isValid()) {
                    RSComponent tick = interfaces.get(94).getComponent(3);
                    tick.doAction("Continue");
                }
                sleep(700, 1200);
            }
        }
    }

    private boolean barricade() {
        try {
            RSNPC barricade = npcs.getNearest(barricadeID);
            if (barricade != null) {
                RSTile barricadeLoc = barricade.getLocation();
                if (barricadeLoc != null) {
                    if (calc.distanceTo(barricadeLoc) <= 2) {
                        if (getMyPlayer().getInteracting() != null) {
                            return true;
                        } else {
                            barricade.doAction("Attack " + barricade.getName());
                            sleep(3000, 5000);
                            return true;
                        }
                    }
                }
            }
            return false;
        } catch (Exception e) {
            return false;
        }
    }

    private void attackJellies() {
        RSNPC jellies = npcs.getNearest(jelliesID);
        if (jellies != null) {
            if (jellies != null) {
                camera.setPitch(true);
                if (calc.distanceTo(jellies) > 4 && !jellies.isOnScreen()) {
                    walking.walkTileMM(walking.getClosestTileOnMap(jellies.getLocation()));
                }
                if (!jellies.isOnScreen()) {
                    camera.turnTo(jellies);
                }
                if (jellies.isOnScreen()
                        && (getMyPlayer().getInteracting() == null || !getMyPlayer().getInteracting().getName().equals(jellies.getName()))) {
                    jellies.doAction("Attack");
                    sleep(3000, 5000);
                }
            }
        }
    }

    private void attackPyres() {
        RSNPC pyres = npcs.getNearest(pyresID);
        if (pyres != null) {
            if (pyres != null) {
                camera.setPitch(true);
                if (calc.distanceTo(pyres) > 4 && !pyres.isOnScreen()) {
                    walking.walkTileMM(walking.getClosestTileOnMap(pyres.getLocation()));
                }
                if (!pyres.isOnScreen()) {
                    camera.turnTo(pyres);
                }
                if (pyres.isOnScreen()
                        && (getMyPlayer().getInteracting() == null || !getMyPlayer().getInteracting().getName().equals(pyres.getName()))) {
                    pyres.doAction("Attack");
                    sleep(3000, 5000);
                }
            }
        }
    }

    private void heal() {
        if (getMyPlayer().getHPPercent() < 60) {
            RSItem bandage = inventory.getItem(bandageID);
            if (game.getCurrentTab() != Game.TAB_INVENTORY) {
                game.openTab(Game.TAB_INVENTORY);
            }
            if (bandage != null) {
                bandage.doAction("Heal");
                sleep(200, 400);
            }
        }
    }

    private void qPrayer() {
        if (quickPrayer) {
            if (getMyPlayer().getHPPercent() < random(65, 90)) {
                if (!prayer.isQuickPrayerOn() && combat.getPrayerPoints() > 1) {
                    interfaces.getComponent(749, 2).doClick();
                }
            }
        }
    }

    private void weaponSpec() {
        if (weaponSpec) {
            if (getMyPlayer().getInteracting() != null) {
                if (!combat.isSpecialEnabled()
                        && settings.getSetting(300) >= specUsage * 10) {
                    if (game.getCurrentTab() != Game.TAB_ATTACK) {
                        switch (random(1, 3)) {
                            case 1:
                                game.openTab(Game.TAB_ATTACK);
                                break;
                            case 2:
                                game.openTab(zeal, true);
                                break;
                        }
                        sleep(random(300, 900));
                    }
                    mouse.click(645 + random(0, 4), 425 + random(0, 4), true);
                    sleep(3000, 5000);
                }
            }
        }
    }

    private void pickUpArrows() {
        if (pickUpArrows) {
            RSGroundItem arrow = null;
            for (int x = 0; arrow == null || x < arrowsID.length; x++) {
                arrow = groundItems.getNearest(arrowsID[x]);
                if (arrow != null) {
                    arrow.doAction("Take");
                    return;
                }
            }
        }
    }

    private Location nearestPyres() {
        int distanceToNorth = (calc.distanceTo(Location.NORTHWEST_PRYES.getRSArea().getNearestTile(getMyPlayer().getLocation())));
        int distanceToSouth = (calc.distanceTo(Location.SOUTHEAST_PYRES.getRSArea().getNearestTile(getMyPlayer().getLocation())));
        return (distanceToSouth > distanceToNorth ? Location.NORTHWEST_PRYES
                : Location.SOUTHEAST_PYRES);
    }

    private Location nearestJellies() {
        int distanceToNorth = (calc.distanceTo(Location.NORTH_JELLIES.getRSArea().getNearestTile(getMyPlayer().getLocation())));
        int distanceToSouth = (calc.distanceTo(Location.SOUTH_JELLIES.getRSArea().getNearestTile(getMyPlayer().getLocation())));
        return (distanceToSouth > distanceToNorth ? Location.NORTH_JELLIES
                : Location.SOUTH_JELLIES);
    }

    private Location nearestSupplies() {
        int distanceToBlue = (calc.distanceTo(Location.BLUE_SUPPLIES.getRSArea().getNearestTile(getMyPlayer().getLocation())));
        int distanceToRed = (calc.distanceTo(Location.RED_SUPPLIES.getRSArea().getNearestTile(getMyPlayer().getLocation())));
        return (distanceToRed > distanceToBlue ? Location.BLUE_SUPPLIES
                : Location.RED_SUPPLIES);
    }

    private Location nearestOwnedGrave() {
        if (controlsWesternGraveyard() && controlsEasternGraveyard()) {
            int distanceToEast = (calc.distanceTo(Location.EAST_GRAVE_OUT.getRSArea().getNearestTile(getMyPlayer().getLocation())));
            int distanceToWest = (calc.distanceTo(Location.WEST_GRAVE_OUT.getRSArea().getNearestTile(getMyPlayer().getLocation())));
            return (distanceToEast < distanceToWest ? Location.EAST_GRAVE_OUT
                    : Location.WEST_GRAVE_OUT);
        } else if (controlsWesternGraveyard()) {
            return Location.WEST_GRAVE_OUT;
        } else if (controlsEasternGraveyard()) {
            return Location.EAST_GRAVE_OUT;
        }
        return null;
    }

    private boolean listenTime() {
        if (getMyLocation().equals(Location.OUTSIDE)) {
            return true;
        } else {
            String time = interfaces.get(836).getComponent(27).getText();
            return (time == null || (time != null && (time.equals("7 mins")
                    || time.equals("6 mins") || time.equals("5 mins")
                    || time.equals("4 mins") || time.equals("3 mins")
                    || time.equals("2 mins") || time.equals("1 min"))));
        }
    }

    private int specialUsage() {
        int[] amountUsage = {10, 25, 33, 35, 45, 50, 55, 60, 80, 85, 100};
        String[][] weapons = {
            {"Rune thrownaxe", "Rod of ivandis"},
            {"Dragon Dagger", "Dragon dagger (p)", "Dragon dagger (p+)",
                "Dragon dagger (p++)", "Dragon Mace", "Dragon Spear",
                "Dragon longsword", "Rune claws"},
            {"Dragon Halberd"},
            {"Magic Longbow"},
            {"Magic Composite Bow"},
            {"Dragon Claws", "Abyssal Whip", "Granite Maul", "Darklight",
                "Barrelchest Anchor", "Armadyl Godsword"},
            {"Magic Shortbow"},
            {"Dragon Scimitar", "Dragon 2H Sword", "Zamorak Godsword",
                "Korasi's sword"},
            {"Dorgeshuun Crossbow", "Bone Dagger"},
            {"Brine Sabre"},
            {"Bandos Godsword", "Dragon Battleaxe", "Dragon Hatchet",
                "Seercull Bow", "Excalibur", "Enhanced excalibur",
                "Ancient Mace", "Saradomin sword"}};
        String str = equipment.getItem(Equipment.WEAPON).getName();
        str = str.substring(str.indexOf(">") + 1);
        for (int i = 0; i < weapons.length; i++) {
            for (int j = 0; j < weapons[i].length; j++) {
                if (weapons[i][j].equalsIgnoreCase(str)) {
                    log(Color.green, "Special percent set at: "
                            + amountUsage[i] + " Using " + str);
                    return amountUsage[i];
                }
            }
        }
        log(Color.red,
                "Your weapon ethier doesn't have special or i don't have it saved please post a comment so i can add it.");
        return 0;
    }

    private boolean pickUpFrag() {
        try {
            RSGroundItem frag = groundItems.getNearest(fragmentID);
            if (frag != null) {
                if (getMyPlayer().isInCombat()) {
                    return false;
                }
                if (frag.isOnScreen() && !inventory.isFull()
                        && calc.distanceTo(frag.getLocation()) < 4) {
                    frag.doAction("Take " + frag.getItem().getName());
                    return true;
                }
            }
            return false;
        } catch (Exception e) {
            return false;
        }
    }

    // </editor-fold>
    // <editor-fold defaultstate="collapsed" desc="Strategies">
    private void playersStrat() {
        if (getMyLocation().equals(chosenLocation)) {
            heal();
            qPrayer();
            attack();
            weaponSpec();
        } else {
            if (attackEverywhere) {
                try {
                    RSPlayer opp = opponent();
                    if (opp != null && opp.isOnScreen()) {
                        if (getMyPlayer().getInteracting() == null) {
                            sleep(200, 400);
                            if (getMyPlayer().getInteracting() == null) {
                                if (opp != null) {
                                    RSModel model = opp.getModel();
                                    if (model != null) {
                                        Point p = model.getPoint();
                                        if (p != null) {
                                            mouse.hop(p);
                                            mouse.click(true);
                                            sleep(3000, 5000);
                                        }
                                    }
                                }
                            }
                        }
                        qPrayer();
                        weaponSpec();
                        return;
                    }
                } catch (Exception e) {
                }
            }
            if (!inventory.containsOneOf(bandageID)
                    && (getSupplies || healOthers)) {
                if (getMyLocation().equals(nearestSupplies())) {
                    getSupplies();
                } else {
                    walkTo(nearestSupplies());
                }
            } else {
                walkTo(chosenLocation);
            }
        }
    }

    private void pyresStrat() {
        if (getMyLocation().equals(nearestPyres())) {
            heal();
            qPrayer();
            if (!pickUpFrag()) {
                attackPyres();
            }
            weaponSpec();
        } else {
            if (!inventory.containsOneOf(bandageID)
                    && (getSupplies || healOthers)) {
                if (getMyLocation().equals(nearestSupplies())) {
                    getSupplies();
                } else {
                    walkTo(nearestSupplies());
                }
            } else {
                walkTo(nearestPyres());
            }
        }
    }

    private void jelliesStrat() {
        if (getMyLocation().equals(nearestJellies())) {
            heal();
            qPrayer();
            if (!pickUpFrag()) {
                attackJellies();
            }
            weaponSpec();
        } else {
            if (!inventory.containsOneOf(bandageID)
                    && (getSupplies || healOthers)) {
                if (getMyLocation().equals(nearestSupplies())) {
                    getSupplies();
                } else {
                    walkTo(nearestSupplies());
                }
            } else {
                walkTo(nearestJellies());
            }
        }
    }

    private void healPlayersStrat() {
        try {
            if (!inventory.containsOneOf(bandageID)) {
                if (getMyLocation().equals(nearestSupplies())) {
                    getSupplies();
                } else {
                    walkTo(nearestSupplies());
                }
            } else {
                if (getMyLocation().equals(chosenLocation)) {
                    if (getActivityBarPercent() < 800) {
                        healOthers();
                    } else {
                        pickUpArrows();
                    }
                } else {
                    walkTo(chosenLocation);
                }
            }
        } catch (Exception e) {
        }
    }

    private void bonesStrat() {
        try {
            if (buryAtGrave
                    && (getMyLocation().equals(Location.EAST_GRAVE_OUT) || getMyLocation().equals(Location.WEST_GRAVE_OUT))
                    && inventory.containsOneOf(bonesID)) {
                if (getActivityBarPercent() < 800) {
                    buryBones();
                } else {
                    RSGroundItem bones = groundItems.getNearest(bonesID);
                    if (bones != null) {
                        if (bones.isOnScreen() && !inventory.isFull()) {
                            bones.doAction("Take");
                            return;
                        }
                    }
                }
            } else {
                if (buryAtGrave && inventory.isFull()) {
                    if (nearestOwnedGrave() != null) {
                        walkTo(nearestOwnedGrave());
                        return;
                    }
                }
                RSGroundItem bones = groundItems.getNearest(bonesID);
                if (bones != null) {
                    if (calc.distanceTo(bones.getLocation()) < 5
                            && !inventory.isFull()) {
                        if (getActivityBarPercent() < random(500, 800)
                                || buryAtGrave) {
                            bones.doAction("Take");
                            return;
                        }
                    }
                }
                if (getMyLocation().equals(chosenLocation)) {
                    bones = groundItems.getNearest(bonesID);
                    if (bones != null) {
                        if (!bones.isOnScreen()) {
                            camera.turnTo(bones.getLocation());
                            if (!bones.isOnScreen()) {
                                walking.walkTileMM(bones.getLocation());
                                sleep(1000, 1200);
                            }
                        } else {
                            if (!inventory.isFull()) {
                                if (getActivityBarPercent() < 700) {
                                    bones.doAction("Take");
                                } else {
                                    pickUpArrows();
                                }
                            } else {
                                if (!buryAtGrave && nearestOwnedGrave() != null) {
                                    destroyBones();
                                }
                            }
                        }
                    }
                } else {
                    walkTo(chosenLocation);
                }
            }
        } catch (Exception e) {
        }
    }

    private void avatarStrat() {
        if (getMyLocation().equals(
                (getMyPlayer().getTeam() == 1 ? Location.RED_AVATAR
                : Location.BLUE_AVATAR))) {
            heal();
            qPrayer();
            attackAvatar();
            weaponSpec();
        } else {
            if (!inventory.containsOneOf(bandageID)
                    && (getSupplies || healOthers)) {
                if (getMyLocation().equals(nearestSupplies())) {
                    getSupplies();
                } else {
                    walkTo(nearestSupplies());
                }
            } else {
                walkTo((getMyPlayer().getTeam() == 1 ? Location.RED_AVATAR
                        : Location.BLUE_AVATAR));
            }
        }

    }

    private enum Strategies {

        ATTACK_PLAYERS, ATTACK_PYRES, ATTACK_JELLIES, HEAL_PLAYERS, PICKUP_BONES, ATTACK_AVATAR;
    }

    private Strategies getStrategies() {
        if (randomStrat) {
            switch (random(0, 5)) {
                case 0:
                    if (pickUpBones) {
                        return Strategies.PICKUP_BONES;
                    }
                case 1:
                    if (healOthers) {
                        return Strategies.HEAL_PLAYERS;
                    }
                case 2:
                    if (skills.getCurrentLevel(Skills.SLAYER) >= 30) {
                        return Strategies.ATTACK_PYRES;
                    }
                case 3:
                    if (skills.getCurrentLevel(Skills.SLAYER) >= 52) {
                        return Strategies.ATTACK_JELLIES;
                    }
                case 4:
                    return Strategies.ATTACK_PLAYERS;
                default:
                    return Strategies.ATTACK_PLAYERS;
            }
        }
        if (pureMode) {
            if (pickUpBones && healOthers) {
                switch (random(0, 2)) {
                    case 0:
                        return Strategies.PICKUP_BONES;
                    case 1:
                        return Strategies.HEAL_PLAYERS;
                }
            }
            if (pickUpBones) {
                return Strategies.PICKUP_BONES;
            }
            if (healOthers) {
                return Strategies.HEAL_PLAYERS;
            }
        }
        if (getActivityBarPercent() < 700) {
            if (pickUpBones && healOthers) {
                switch (random(0, 2)) {
                    case 0:
                        return Strategies.PICKUP_BONES;
                    case 1:
                        return Strategies.HEAL_PLAYERS;
                }
            }
            if (pickUpBones) {
                return Strategies.PICKUP_BONES;
            }
            if (healOthers) {
                return Strategies.HEAL_PLAYERS;
            }
        } else {
            if (getOpponentAvatarLevel() < skills.getRealLevel(Skills.SLAYER)
                    && attackAvatar) {
                return Strategies.ATTACK_AVATAR;
            }
            if (attackPyres) {
                return Strategies.ATTACK_PYRES;
            } else if (attackJellies) {
                return Strategies.ATTACK_JELLIES;
            } else if (attackPlayers) {
                return Strategies.ATTACK_PLAYERS;
            }
        }
        return null;
    }

    // </editor-fold>
    // <editor-fold defaultstate="collapsed" desc="Location">
    private enum Location {

        RED_WAITING(new RSArea(new RSTile(1909, 3167), new RSTile(1899, 3156)),
        "Red waiting", false), BLUE_WAITING(new RSArea(new RSTile(1880,
        3167), new RSTile(1869, 3157)), "Blue waiting", false), OUTSIDE(
        new RSArea(new RSTile(1918, 3187), new RSTile(1862, 3149)),
        "Lobby", false), RED_SPAWN(new RSArea(new RSTile(1951, 3234),
        new RSTile(1958, 3244)), "Inside red spawn", false), BLUE_SPAWN(
        new RSArea(new RSTile(1816, 3220), new RSTile(1823, 3230)),
        "Inside blue spawn", false), BLUE_SPAWN_OUT(new RSArea(
        new RSTile(1815, 3231), new RSTile(1808, 3219)),
        "Outside blue spawn", true), RED_SPAWN_OUT(new RSArea(
        new RSTile(1965, 3245), new RSTile(1959, 3232)),
        "Outside red spawn", true), EAST_GRAVE(new RSArea(new RSTile(
        1932, 3244), new RSTile(1934, 3246)), "Inside east grave",
        false), WEST_GRAVE(new RSArea(new RSTile(1841, 3217),
        new RSTile(1843, 3219)), "Inside west grave", false), EAST_GRAVE_OUT(
        new RSArea(new RSTile(1937, 3250), new RSTile(1927, 3242)),
        "Outside east grave", true), WEST_GRAVE_OUT(new RSArea(
        new RSTile(1847, 3223), new RSTile(1837, 3214)),
        "Outside west grave", true), RED_SUPPLIES(new RSArea(
        new RSTile(1977, 3213), new RSTile(1961, 3203)),
        "Red supplies", true), BLUE_SUPPLIES(new RSArea(new RSTile(
        1812, 3261), new RSTile(1795, 3250)), "Blue Supplies", true), RED_AVATAR(
        new RSArea(new RSTile(1976, 3261), new RSTile(1959, 3244)),
        "Red avatar", true), BLUE_AVATAR(new RSArea(new RSTile(1816,
        3220), new RSTile(1798, 3202)), "Blue avatar", true), SOUTHEAST_PYRES(
        new RSArea(new RSTile(1938, 3222), new RSTile(1917, 3204)),
        "Southeast pryes", true), NORTHWEST_PRYES(new RSArea(
        new RSTile(1856, 3258), new RSTile(1834, 3238)),
        "Northwest pryes", true), NORTH_JELLIES(new RSArea(new RSTile(
        1906, 3263), new RSTile(1869, 3248)), "North jellies", true), SOUTH_JELLIES(
        new RSArea(new RSTile(1899, 3214), new RSTile(1873, 3199)),
        "South jellies", true), OBELISK(new RSArea(new RSTile(1901,
        3241), new RSTile(1872, 3221)), "Obelisk", true), OTHER(
        new RSArea(new RSTile(0, 0), new RSTile(0, 0)), "Other", false);
        private final RSArea area;
        private final String name;
        private final boolean combatArea;

        Location(RSArea area, String name, boolean combatArea) {
            this.area = area;
            this.name = name;
            this.combatArea = combatArea;
        }

        RSArea getRSArea() {
            return area;
        }

        String getName() {
            return name;
        }

        boolean isCombatArea() {
            return combatArea;
        }

        boolean containsTile(RSTile tile) {
            return this.area.contains(tile);
        }
    }

    private Location getMyLocation() {
        RSTile player = getMyPlayer().getLocation();
        for (Location loc : Location.values()) {
            if (loc.containsTile(player)) {
                return loc;
            }
        }
        return Location.OTHER;
    }

    private RSTile divideTile(RSTile tile) {
        RSTile loc = getMyPlayer().getLocation();
        return new RSTile((loc.getX() + 4 * tile.getX()) / 5,
                (loc.getY() + 4 * tile.getY()) / 5);
    }

    private void walkTo(Location loc) {
        try {
            path = walking.getPath(loc.getRSArea().getCentralTile());
            tempTile = loc.getRSArea().getCentralTile();
            while (path.getNext() == null) {
                if (game.getClientState() == Game.INDEX_LOBBY_SCREEN
                        || !game.isLoggedIn()
                        || getMyLocation().equals(Location.OUTSIDE)
                        || getMyLocation().equals(Location.RED_SPAWN)
                        || getMyLocation().equals(Location.BLUE_SPAWN)
                        || getMyLocation().equals(Location.EAST_GRAVE)
                        || getMyLocation().equals(Location.WEST_GRAVE)) {
                    return;
                }
                tempTile = divideTile(tempTile);
                path = walking.getPath(tempTile);
            }
            if (path.getNext() != null) {
                path.traverse();
            }
        } catch (Exception e) {
        }
    }

    private String[] allCombatLocations() {
        List<String> list = new ArrayList<String>();
        list.add("Opponents spawn");
        list.add("Opponents grave");
        list.add("Opponents avatar");
        list.add("Nearest pyres");
        list.add("Nearest jellies");
        for (Location s : Location.values()) {
            if (s.isCombatArea()) {
                list.add(s.getName());
            }
        }
        String[] stringList = new String[list.size()];
        Collections.reverse(list);
        list.toArray(stringList);
        return stringList;
    }

    private Location locationNameToObject(String name) {
        if (name.equals("Opponents grave")) {
            return opponentsGrave();
        }
        if (name.equals("Opponents spawn")) {
            return opponentsSpawn();
        }
        if (name.equals("Opponents avatar")) {
            return opponentsAvatar();
        }
        if (name.equals("Nearest pyres")) {
            return nearestPyres();
        }
        if (name.equals("Nearest jellies")) {
            return nearestJellies();
        }
        for (Location s : Location.values()) {
            if (s.getName().equals(name)) {
                return s;
            }
        }
        return null;
    }

    private Location opponentsGrave() {
        if (!controlsWesternGraveyard() && !controlsEasternGraveyard()) {
            int distanceToEast = (calc.distanceTo(Location.EAST_GRAVE_OUT.getRSArea().getNearestTile(getMyPlayer().getLocation())));
            int distanceToWest = (calc.distanceTo(Location.WEST_GRAVE_OUT.getRSArea().getNearestTile(getMyPlayer().getLocation())));
            return (distanceToEast < distanceToWest ? Location.EAST_GRAVE_OUT
                    : Location.WEST_GRAVE_OUT);
        } else if (!controlsWesternGraveyard()) {
            return Location.WEST_GRAVE_OUT;
        } else if (!controlsEasternGraveyard()) {
            return Location.EAST_GRAVE_OUT;
        }
        return opponentsSpawn();
    }

    private Location opponentsSpawn() {
        return (getMyPlayer().getTeam() == 1 ? Location.RED_SPAWN_OUT
                : Location.BLUE_SPAWN_OUT);
    }

    private Location opponentsAvatar() {
        return (getMyPlayer().getTeam() == 1 ? Location.RED_AVATAR
                : Location.BLUE_AVATAR);
    }

    // </editor-fold>
    // <editor-fold defaultstate="collapsed" desc="Summoning">
    private enum Summoning {

        SPIRITWOLF("Spirit wolf", 12047, 12425), DREADFOWL("Dreadfowl", 12043,
        12445), MEERKAT("Meerkat", 19622, 19621), SPIRITSPIDER(
        "Spirit spider", 12059, 12428), THORNYSNAIL("Thorny snail",
        12019, 12459), GRANITECRAB("Granite crab", 12009, 12533), SPIRITMOSQUITO(
        "Spirit mosquito", 12778, 12838), DESERTWYRM("Desert wyrm",
        12049, 12460), SPIRITSCORPION("Spirit scorpion", 12055, 12432), SPIRITTZKIH(
        "Spirit tz-kih", 12808, 12839), ALBINORAT("Albino rat", 12067,
        12430), SPIRITKALPHITE("Spirit kalphite", 12063, 12446), COMPSTMOUND(
        "Compost mound", 12091, 12440), GIANTCHINCHOMPA(
        "Giant chinchompa", 12800, 12834), VAMPIREBAT("Vampire bat",
        12053, 12447), HONEYBADGER("Honey badger", 12065, 12433), BEAVER(
        "Beaver", 12021, 12429), VOIDRAVAGER("Void ravager", 12818,
        12443), VOIDSPINNER("Void spinner", 12780, 12443), VOIDTOUCHER(
        "Void torcher", 12798, 12443), VOIDSHIFTER("Void shifter",
        12814, 12443), BULLANT("Bull ant", 12087, 12431), MACAW(
        "Macaw", 12071, 12422), EVILTURNIP("Evil turnip", 12051, 12448), SPCOCKATRICE(
        "Sp. cockatrice", 12095, 12458), SPGUTHATRICE("Sp. guthatrice",
        12097, 12458), SPSARATRICE("Sp. saratrice", 12099, 12458), SPZAMATRICE(
        "Sp. zamatrice", 12101, 12458), SPPENGATRICE("Sp. pengatrice",
        12103, 12458), SPCORAXTRICE("Sp. coraxatrice", 12105, 12458), SPVULATRICE(
        "Sp. vulatrice", 12107, 12458), PYRELORD("Pyrelord", 12816,
        12829), MAGPIE("Magpie", 12041, 12426), BLOATEDLEECH(
        "Bloated leech", 12061, 12444), SPIRITTERRORBIRD(
        "Spirit terrorbird", 12007, 12441), ABYSSALPARASITE(
        "Abyssal parasite", 12035, 12454), SPIRITJELLY("Spirit jelly",
        12027, 12453), IBIS("Ibis", 12531, 12424), SPIRITKYATT(
        "Spirit kyatt", 12812, 12836), SPIRITLARUPIA("Spirit larupia",
        12784, 12840), SPIRITGRAAK("Spirit graahk", 12810, 12835), KARAMOVERLOAD(
        "Karam. overlord", 12023, 12455), SMOKEDEVIL("Smoke devil",
        12085, 12468), ABYSSALLURKER("Abyssal lurker", 12037, 12427), SPIRITCOBRA(
        "Spirit cobra", 12015, 12436), STRANGERPLANT("Stranger plant",
        12045, 12467), BARKERTOAD("Barker toad", 12123, 12452), WARTORTOISE(
        "War tortoise", 12031, 12439), BUNYIP("Bunyip", 12029, 12438), FRUITBAT(
        "Fruit bat", 12033, 12423), RAVENOUSLOCUST("Ravenous locust",
        12820, 12830), ARCTICBEAR("Arctic bear", 12057, 12451), PHOENIX(
        "Phoenix", 14623, 14622), OBSIDIANGOLEM("Obsidian golem",
        12792, 12826), GRANITELOBSTER("Granite lobster", 12069, 12449), PRAYINGMANTIS(
        "Praying mantis", 12011, 12450), FORGEREGENT("Forge regent",
        12782, 12841), TALONBEAST("Talon beast", 12794, 12831), GIANTENT(
        "Giant ent", 12013, 12457), FIRETITAN("Fire titan", 12802,
        12824), MOSSTITAN("Moss titan", 12804, 12824), ICETITAN(
        "Ice titan", 12806, 12824), HYDRA("Hydra", 12025, 12442), SPIRITDAGANNOTH(
        "Spirit dagannoth", 12017, 12456), LAVATITAN("Lava titan",
        12788, 12837), SWAMPTITAN("Swamp titan", 12776, 12832), BRONZEMINOTAUR(
        "Bronze minotaur", 12073, 12461), IRONMINOTOUR("Iron minotaur",
        12075, 12462), STEELMINOTOUR("Steel minotaur", 12077, 12463), MITHRILMINOTAUR(
        "Mithril minotaur", 12079, 12464), ADAMANTMINOTAUR(
        "Adamant minotaur", 12081, 12465), RUNEMINOTAUR(
        "Rune minotaur", 12083, 12466), UNICORNSTALLION(
        "Unicorn stallion", 12039, 12434), GEYSERTITAN("Geyser titan",
        12786, 12833), WOLPERTINGER("Wolpertinger", 12089, 12437), ABYSSALTITAN(
        "Abyssal titan", 12796, 12827), IRONTITAN("Iron titan", 12822,
        12828), PACKYAK("Pack yak", 12093, 12435), STEELTITAN(
        "Steel titan", 12790, 12825);
        private final String name;
        private final int pouchID;

        Summoning(String name, int pouchID, int scrollID) {
            this.name = name;
            this.pouchID = pouchID;
        }

        String getName() {
            return this.name;
        }

        int getPouchID() {
            return this.pouchID;
        }
    }

    private String[] allFamiliars() {
        List<String> list = new ArrayList<String>();
        for (Summoning s : Summoning.values()) {
            list.add(s.getName());
        }
        String[] stringList = new String[list.size()];
        list.toArray(stringList);
        return stringList;
    }

    private Summoning farmiliarNameToObject(String name) {
        for (Summoning s : Summoning.values()) {
            if (s.getName().equals(name)) {
                return s;
            }
        }
        return null;
    }

    // </editor-fold>
    // <editor-fold defaultstate="collapsed" desc="GUI">
    public class SWGUI extends javax.swing.JFrame {

        private static final long serialVersionUID = -7750165076281848642L;

        public SWGUI() {
            initComponents();
        }

        // <editor-fold defaultstate="collapsed" desc="Init">
        private void initComponents() {

            jLabel1 = new javax.swing.JLabel();
            jLabel2 = new javax.swing.JLabel();
            jTabbedPane1 = new javax.swing.JTabbedPane();
            jPanel1 = new javax.swing.JPanel();
            jLabel3 = new javax.swing.JLabel();
            jSeparator1 = new javax.swing.JSeparator();
            jLabel4 = new javax.swing.JLabel();
            ChooseTeam = new javax.swing.JComboBox();
            jLabel5 = new javax.swing.JLabel();
            ChooseActivity = new javax.swing.JComboBox();
            AttackAvatar = new javax.swing.JCheckBox();
            PickUpBones = new javax.swing.JCheckBox();
            HealOthers = new javax.swing.JCheckBox();
            Start = new javax.swing.JButton();
            GetSupplies = new javax.swing.JCheckBox();
            info = new javax.swing.JButton();
            donate = new javax.swing.JButton();
            BuryAtGrave = new javax.swing.JCheckBox();
            PickUpArrows = new javax.swing.JCheckBox();
            QuickPrayer = new javax.swing.JCheckBox();
            WeponSpec = new javax.swing.JCheckBox();
            AttackEverywhere = new javax.swing.JCheckBox();
            jLabel18 = new javax.swing.JLabel();
            ChooseLocation = new javax.swing.JComboBox();
            jPanel4 = new javax.swing.JPanel();
            WithdrawPouches = new javax.swing.JCheckBox();
            ChooseFamiliar = new javax.swing.JComboBox();
            jLabel17 = new javax.swing.JLabel();
            EnableSummoning = new javax.swing.JCheckBox();
            UseScrolls = new javax.swing.JCheckBox();
            jPanel3 = new javax.swing.JPanel();
            jScrollPane1 = new javax.swing.JScrollPane();
            jList1 = new javax.swing.JList();
            jLabel13 = new javax.swing.JLabel();
            jComboBox1 = new javax.swing.JComboBox();
            jLabel14 = new javax.swing.JLabel();
            jComboBox2 = new javax.swing.JComboBox();
            jButton1 = new javax.swing.JButton();
            jLabel15 = new javax.swing.JLabel();
            jLabel16 = new javax.swing.JLabel();
            jPanel2 = new javax.swing.JPanel();
            TakeBreak = new javax.swing.JCheckBox();
            jLabel6 = new javax.swing.JLabel();
            jLabel7 = new javax.swing.JLabel();
            MaxiumTimeUntillBreak = new javax.swing.JTextField();
            MinimiumTimeUntillBreak = new javax.swing.JTextField();
            jLabel8 = new javax.swing.JLabel();
            MinimiumTimeForBreak = new javax.swing.JTextField();
            jLabel9 = new javax.swing.JLabel();
            MaxiumTimeForBreak = new javax.swing.JTextField();
            jLabel10 = new javax.swing.JLabel();
            jLabel11 = new javax.swing.JLabel();
            jLabel12 = new javax.swing.JLabel();

            setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
            setTitle("DebaucherySoulWars");
            setAlwaysOnTop(true);
            setBackground(java.awt.SystemColor.activeCaption);
            setCursor(new java.awt.Cursor(java.awt.Cursor.CROSSHAIR_CURSOR));
            setForeground(java.awt.Color.gray);
            setResizable(false);

            jLabel1.setText("<html><center><img src=\"http://img13.imageshack.us/img13/2519/sswz.png\"/></center></html>");

            jLabel2.setFont(new java.awt.Font("Tahoma", 0, 10));
            jLabel2.setText("Made/updated by Debauchery");

            jLabel3.setFont(new java.awt.Font("Tahoma", 1, 18));
            jLabel3.setText("Please fill in all areas;");

            jLabel4.setFont(new java.awt.Font("Tahoma", 0, 14));
            jLabel4.setText("Choose team:");

            ChooseTeam.setModel(new javax.swing.DefaultComboBoxModel(
                    new String[]{"Random", "Red", "Blue", "Last Won",
                        "Last Lost", "Clan Chat"}));

            jLabel5.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
            jLabel5.setText("Choose Activity: ");

            ChooseActivity.setModel(new javax.swing.DefaultComboBoxModel(
                    new String[]{"Random", "Attack Players", "Attack Pyres",
                        "Attack Jellies", "Pure Mode"}));

            AttackAvatar.setText("Attack Avatar");

            PickUpBones.setText("Pick Up Bones");

            HealOthers.setText("Heal Others");

            Start.setFont(new java.awt.Font("Tahoma", 0, 24)); // NOI18N
            Start.setText("Start");
            Start.addActionListener(new java.awt.event.ActionListener() {

                @Override
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    StartActionPerformed(evt);
                }
            });

            GetSupplies.setText("Get Supplies");

            info.setText("Infomation");
            info.addActionListener(new java.awt.event.ActionListener() {

                @Override
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    infoActionPerformed(evt);
                }
            });

            donate.setText("Donate");
            donate.addActionListener(new java.awt.event.ActionListener() {

                @Override
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    donateActionPerformed(evt);
                }
            });

            BuryAtGrave.setText("Bury At Grave");

            PickUpArrows.setText("Pick Up Arrows");

            QuickPrayer.setText("Quick Prayer");

            WeponSpec.setText("Weapon Spec");

            AttackEverywhere.setText("Atk Everywhere");

            jLabel18.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
            jLabel18.setText("Choose Location:");

            ChooseLocation.setModel(new javax.swing.DefaultComboBoxModel(
                    allCombatLocations()));

            javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(
                    jPanel1);
            jPanel1.setLayout(jPanel1Layout);
            jPanel1Layout.setHorizontalGroup(jPanel1Layout.createParallelGroup(
                    javax.swing.GroupLayout.Alignment.LEADING).addGroup(
                    jPanel1Layout.createSequentialGroup().addComponent(jLabel3).addContainerGap(108,
                    Short.MAX_VALUE)).addComponent(jSeparator1,
                    javax.swing.GroupLayout.DEFAULT_SIZE, 392,
                    Short.MAX_VALUE).addGroup(
                    jPanel1Layout.createSequentialGroup().addContainerGap().addGroup(
                    jPanel1Layout.createParallelGroup(
                    javax.swing.GroupLayout.Alignment.LEADING).addGroup(
                    jPanel1Layout.createSequentialGroup().addGroup(
                    jPanel1Layout.createParallelGroup(
                    javax.swing.GroupLayout.Alignment.LEADING).addGroup(
                    jPanel1Layout.createSequentialGroup().addGroup(
                    jPanel1Layout.createParallelGroup(
                    javax.swing.GroupLayout.Alignment.TRAILING).addGroup(
                    jPanel1Layout.createSequentialGroup().addComponent(
                    donate,
                    javax.swing.GroupLayout.DEFAULT_SIZE,
                    101,
                    Short.MAX_VALUE).addGap(8,
                    8,
                    8)).addGroup(
                    jPanel1Layout.createSequentialGroup().addComponent(
                    info,
                    javax.swing.GroupLayout.DEFAULT_SIZE,
                    103,
                    Short.MAX_VALUE).addPreferredGap(
                    javax.swing.LayoutStyle.ComponentPlacement.RELATED))).addComponent(
                    Start,
                    javax.swing.GroupLayout.PREFERRED_SIZE,
                    153,
                    javax.swing.GroupLayout.PREFERRED_SIZE).addGap(19,
                    19,
                    19)).addGroup(
                    javax.swing.GroupLayout.Alignment.TRAILING,
                    jPanel1Layout.createSequentialGroup().addGroup(
                    jPanel1Layout.createParallelGroup(
                    javax.swing.GroupLayout.Alignment.LEADING).addComponent(
                    jLabel4).addComponent(
                    jLabel5)).addGap(32,
                    32,
                    32).addGroup(
                    jPanel1Layout.createParallelGroup(
                    javax.swing.GroupLayout.Alignment.TRAILING,
                    false).addComponent(
                    ChooseTeam,
                    0,
                    javax.swing.GroupLayout.DEFAULT_SIZE,
                    Short.MAX_VALUE).addComponent(
                    ChooseActivity,
                    0,
                    145,
                    Short.MAX_VALUE).addComponent(
                    ChooseLocation,
                    javax.swing.GroupLayout.Alignment.LEADING,
                    0,
                    javax.swing.GroupLayout.DEFAULT_SIZE,
                    Short.MAX_VALUE)))).addGap(101,
                    101,
                    101)).addGroup(
                    jPanel1Layout.createSequentialGroup().addGroup(
                    jPanel1Layout.createParallelGroup(
                    javax.swing.GroupLayout.Alignment.TRAILING).addGroup(
                    jPanel1Layout.createParallelGroup(
                    javax.swing.GroupLayout.Alignment.LEADING).addComponent(
                    PickUpBones).addComponent(
                    GetSupplies)).addComponent(
                    WeponSpec)).addPreferredGap(
                    javax.swing.LayoutStyle.ComponentPlacement.UNRELATED).addGroup(
                    jPanel1Layout.createParallelGroup(
                    javax.swing.GroupLayout.Alignment.LEADING).addComponent(
                    PickUpArrows).addComponent(
                    HealOthers).addComponent(
                    QuickPrayer)).addPreferredGap(
                    javax.swing.LayoutStyle.ComponentPlacement.RELATED).addGroup(
                    jPanel1Layout.createParallelGroup(
                    javax.swing.GroupLayout.Alignment.LEADING).addGroup(
                    jPanel1Layout.createSequentialGroup().addComponent(
                    BuryAtGrave,
                    javax.swing.GroupLayout.DEFAULT_SIZE,
                    111,
                    Short.MAX_VALUE).addContainerGap(
                    81,
                    javax.swing.GroupLayout.PREFERRED_SIZE)).addGroup(
                    jPanel1Layout.createSequentialGroup().addComponent(
                    AttackAvatar,
                    javax.swing.GroupLayout.DEFAULT_SIZE,
                    186,
                    Short.MAX_VALUE).addContainerGap()))))).addGroup(
                    javax.swing.GroupLayout.Alignment.TRAILING,
                    jPanel1Layout.createSequentialGroup().addGap(200, 200, 200).addComponent(
                    AttackEverywhere,
                    javax.swing.GroupLayout.DEFAULT_SIZE,
                    186, Short.MAX_VALUE).addContainerGap()).addGroup(
                    jPanel1Layout.createSequentialGroup().addContainerGap().addComponent(jLabel18).addContainerGap(275,
                    Short.MAX_VALUE)));
            jPanel1Layout.setVerticalGroup(jPanel1Layout.createParallelGroup(
                    javax.swing.GroupLayout.Alignment.LEADING).addGroup(
                    jPanel1Layout.createSequentialGroup().addComponent(jLabel3).addGap(8, 8, 8).addComponent(
                    jSeparator1,
                    javax.swing.GroupLayout.PREFERRED_SIZE,
                    javax.swing.GroupLayout.DEFAULT_SIZE,
                    javax.swing.GroupLayout.PREFERRED_SIZE).addPreferredGap(
                    javax.swing.LayoutStyle.ComponentPlacement.RELATED).addGroup(
                    jPanel1Layout.createParallelGroup(
                    javax.swing.GroupLayout.Alignment.BASELINE).addComponent(
                    jLabel4).addComponent(
                    ChooseTeam,
                    javax.swing.GroupLayout.PREFERRED_SIZE,
                    javax.swing.GroupLayout.DEFAULT_SIZE,
                    javax.swing.GroupLayout.PREFERRED_SIZE)).addPreferredGap(
                    javax.swing.LayoutStyle.ComponentPlacement.RELATED).addGroup(
                    jPanel1Layout.createParallelGroup(
                    javax.swing.GroupLayout.Alignment.BASELINE).addComponent(
                    jLabel5).addComponent(
                    ChooseActivity,
                    javax.swing.GroupLayout.PREFERRED_SIZE,
                    javax.swing.GroupLayout.DEFAULT_SIZE,
                    javax.swing.GroupLayout.PREFERRED_SIZE)).addPreferredGap(
                    javax.swing.LayoutStyle.ComponentPlacement.RELATED).addGroup(
                    jPanel1Layout.createParallelGroup(
                    javax.swing.GroupLayout.Alignment.BASELINE).addComponent(
                    jLabel18).addComponent(
                    ChooseLocation,
                    javax.swing.GroupLayout.PREFERRED_SIZE,
                    javax.swing.GroupLayout.DEFAULT_SIZE,
                    javax.swing.GroupLayout.PREFERRED_SIZE)).addPreferredGap(
                    javax.swing.LayoutStyle.ComponentPlacement.RELATED,
                    8, Short.MAX_VALUE).addGroup(
                    jPanel1Layout.createParallelGroup(
                    javax.swing.GroupLayout.Alignment.BASELINE).addComponent(
                    AttackEverywhere).addComponent(
                    QuickPrayer).addComponent(
                    WeponSpec)).addPreferredGap(
                    javax.swing.LayoutStyle.ComponentPlacement.UNRELATED).addGroup(
                    jPanel1Layout.createParallelGroup(
                    javax.swing.GroupLayout.Alignment.BASELINE).addComponent(
                    GetSupplies).addComponent(
                    HealOthers).addComponent(
                    AttackAvatar,
                    javax.swing.GroupLayout.PREFERRED_SIZE,
                    23,
                    javax.swing.GroupLayout.PREFERRED_SIZE)).addPreferredGap(
                    javax.swing.LayoutStyle.ComponentPlacement.UNRELATED).addGroup(
                    jPanel1Layout.createParallelGroup(
                    javax.swing.GroupLayout.Alignment.BASELINE).addComponent(
                    PickUpBones).addComponent(
                    PickUpArrows).addComponent(
                    BuryAtGrave)).addPreferredGap(
                    javax.swing.LayoutStyle.ComponentPlacement.UNRELATED).addGroup(
                    jPanel1Layout.createParallelGroup(
                    javax.swing.GroupLayout.Alignment.TRAILING,
                    false).addGroup(
                    jPanel1Layout.createSequentialGroup().addComponent(
                    info).addPreferredGap(
                    javax.swing.LayoutStyle.ComponentPlacement.RELATED,
                    javax.swing.GroupLayout.DEFAULT_SIZE,
                    Short.MAX_VALUE).addComponent(
                    donate)).addComponent(
                    Start,
                    javax.swing.GroupLayout.PREFERRED_SIZE,
                    55,
                    javax.swing.GroupLayout.PREFERRED_SIZE)).addContainerGap()));

            jTabbedPane1.addTab("General", jPanel1);

            WithdrawPouches.setText("Withdraw more pouches");

            ChooseFamiliar.setModel(new javax.swing.DefaultComboBoxModel(
                    allFamiliars()));

            jLabel17.setText("Select your summoning monster");

            EnableSummoning.setText("Enable summoning");

            UseScrolls.setText("Use scrolls");

            javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(
                    jPanel4);
            jPanel4.setLayout(jPanel4Layout);
            jPanel4Layout.setHorizontalGroup(jPanel4Layout.createParallelGroup(
                    javax.swing.GroupLayout.Alignment.LEADING).addGroup(
                    jPanel4Layout.createSequentialGroup().addGroup(
                    jPanel4Layout.createParallelGroup(
                    javax.swing.GroupLayout.Alignment.LEADING).addGroup(
                    jPanel4Layout.createSequentialGroup().addGap(92,
                    92,
                    92).addComponent(
                    EnableSummoning)).addGroup(
                    jPanel4Layout.createSequentialGroup().addContainerGap().addComponent(
                    jLabel17)).addGroup(
                    jPanel4Layout.createSequentialGroup().addContainerGap().addComponent(
                    ChooseFamiliar,
                    0,
                    284,
                    Short.MAX_VALUE)).addGroup(
                    jPanel4Layout.createSequentialGroup().addContainerGap().addComponent(
                    WithdrawPouches)).addGroup(
                    jPanel4Layout.createSequentialGroup().addContainerGap().addComponent(
                    UseScrolls))).addContainerGap()));
            jPanel4Layout.setVerticalGroup(jPanel4Layout.createParallelGroup(
                    javax.swing.GroupLayout.Alignment.LEADING).addGroup(
                    jPanel4Layout.createSequentialGroup().addContainerGap().addComponent(EnableSummoning).addPreferredGap(
                    javax.swing.LayoutStyle.ComponentPlacement.RELATED).addComponent(jLabel17).addPreferredGap(
                    javax.swing.LayoutStyle.ComponentPlacement.RELATED).addComponent(
                    ChooseFamiliar,
                    javax.swing.GroupLayout.PREFERRED_SIZE,
                    javax.swing.GroupLayout.DEFAULT_SIZE,
                    javax.swing.GroupLayout.PREFERRED_SIZE).addPreferredGap(
                    javax.swing.LayoutStyle.ComponentPlacement.UNRELATED).addComponent(WithdrawPouches).addPreferredGap(
                    javax.swing.LayoutStyle.ComponentPlacement.RELATED).addComponent(UseScrolls).addContainerGap(141,
                    Short.MAX_VALUE)));

            jTabbedPane1.addTab("Summoning", jPanel4);

            jScrollPane1.setViewportView(jList1);

            jLabel13.setText("Reward");

            jComboBox1.setModel(new javax.swing.DefaultComboBoxModel(
                    new String[]{"Attack", "Defence", "Strenght", "Magic",
                        "Range", "Health", "Prayer", "Slayer"}));

            jLabel14.setText("Amount");

            jComboBox2.setModel(new javax.swing.DefaultComboBoxModel(
                    new String[]{"Every Game", "10", "20", "50",
                        "100", "150"}));

            jButton1.setText("Add >");

            jLabel15.setText("Note. This is still not ");

            jLabel16.setText("complete, just a preview");

            javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(
                    jPanel3);
            jPanel3.setLayout(jPanel3Layout);
            jPanel3Layout.setHorizontalGroup(jPanel3Layout.createParallelGroup(
                    javax.swing.GroupLayout.Alignment.LEADING).addGroup(
                    jPanel3Layout.createSequentialGroup().addContainerGap().addGroup(
                    jPanel3Layout.createParallelGroup(
                    javax.swing.GroupLayout.Alignment.LEADING).addGroup(
                    jPanel3Layout.createParallelGroup(
                    javax.swing.GroupLayout.Alignment.TRAILING).addGroup(
                    jPanel3Layout.createParallelGroup(
                    javax.swing.GroupLayout.Alignment.LEADING,
                    false).addComponent(
                    jComboBox2,
                    0,
                    javax.swing.GroupLayout.DEFAULT_SIZE,
                    Short.MAX_VALUE).addComponent(
                    jLabel13).addComponent(
                    jComboBox1,
                    0,
                    114,
                    Short.MAX_VALUE).addComponent(
                    jLabel14)).addComponent(
                    jButton1)).addComponent(
                    jLabel15).addComponent(
                    jLabel16)).addPreferredGap(
                    javax.swing.LayoutStyle.ComponentPlacement.RELATED).addComponent(
                    jScrollPane1,
                    javax.swing.GroupLayout.DEFAULT_SIZE,
                    162, Short.MAX_VALUE).addContainerGap()));
            jPanel3Layout.setVerticalGroup(jPanel3Layout.createParallelGroup(
                    javax.swing.GroupLayout.Alignment.LEADING).addGroup(
                    jPanel3Layout.createSequentialGroup().addContainerGap().addGroup(
                    jPanel3Layout.createParallelGroup(
                    javax.swing.GroupLayout.Alignment.LEADING).addGroup(
                    jPanel3Layout.createSequentialGroup().addComponent(
                    jLabel13).addPreferredGap(
                    javax.swing.LayoutStyle.ComponentPlacement.RELATED).addComponent(
                    jComboBox1,
                    javax.swing.GroupLayout.PREFERRED_SIZE,
                    javax.swing.GroupLayout.DEFAULT_SIZE,
                    javax.swing.GroupLayout.PREFERRED_SIZE).addPreferredGap(
                    javax.swing.LayoutStyle.ComponentPlacement.RELATED).addComponent(
                    jLabel14).addPreferredGap(
                    javax.swing.LayoutStyle.ComponentPlacement.RELATED).addComponent(
                    jComboBox2,
                    javax.swing.GroupLayout.PREFERRED_SIZE,
                    javax.swing.GroupLayout.DEFAULT_SIZE,
                    javax.swing.GroupLayout.PREFERRED_SIZE).addPreferredGap(
                    javax.swing.LayoutStyle.ComponentPlacement.RELATED).addComponent(
                    jButton1).addGap(6,
                    6,
                    6).addComponent(
                    jLabel15).addPreferredGap(
                    javax.swing.LayoutStyle.ComponentPlacement.RELATED).addComponent(
                    jLabel16)).addComponent(
                    jScrollPane1,
                    javax.swing.GroupLayout.DEFAULT_SIZE,
                    244,
                    Short.MAX_VALUE)).addContainerGap()));

            jTabbedPane1.addTab("Trade in Zeals", jPanel3);

            TakeBreak.setFont(new java.awt.Font("Tahoma", 0, 14));
            TakeBreak.setText("Take Breaks");

            jLabel6.setFont(new java.awt.Font("Tahoma", 0, 14));
            jLabel6.setText("Minimium Time Untill Break: ");

            jLabel7.setFont(new java.awt.Font("Tahoma", 0, 14));
            jLabel7.setText("Maxium Time Untill Break:");

            MaxiumTimeUntillBreak.setText("70");

            MinimiumTimeUntillBreak.setText("40");

            jLabel8.setFont(new java.awt.Font("Tahoma", 0, 14));
            jLabel8.setText("Minimium Time For Break:");

            MinimiumTimeForBreak.setText("15");

            jLabel9.setFont(new java.awt.Font("Tahoma", 0, 14));
            jLabel9.setText("Maxium Time For Break:");

            MaxiumTimeForBreak.setText("60");

            jLabel10.setFont(new java.awt.Font("Tahoma", 0, 10));
            jLabel10.setText("Please don't use the times supplied if using them");

            jLabel11.setFont(new java.awt.Font("Tahoma", 0, 10));
            jLabel11.setText("use them as a guide line, but if the same times are used all the");

            jLabel12.setFont(new java.awt.Font("Tahoma", 0, 10));
            jLabel12.setText("time it will become an easy way to detect a bot.");

            javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(
                    jPanel2);
            jPanel2.setLayout(jPanel2Layout);
            jPanel2Layout.setHorizontalGroup(jPanel2Layout.createParallelGroup(
                    javax.swing.GroupLayout.Alignment.LEADING).addGroup(
                    jPanel2Layout.createSequentialGroup().addGroup(
                    jPanel2Layout.createParallelGroup(
                    javax.swing.GroupLayout.Alignment.LEADING).addGroup(
                    jPanel2Layout.createSequentialGroup().addContainerGap().addGroup(
                    jPanel2Layout.createParallelGroup(
                    javax.swing.GroupLayout.Alignment.LEADING).addComponent(
                    jLabel6).addComponent(
                    jLabel7).addComponent(
                    jLabel8).addComponent(
                    jLabel9).addComponent(
                    TakeBreak)).addGap(18,
                    18,
                    18).addGroup(
                    jPanel2Layout.createParallelGroup(
                    javax.swing.GroupLayout.Alignment.LEADING,
                    false).addComponent(
                    MaxiumTimeForBreak).addComponent(
                    MaxiumTimeUntillBreak).addComponent(
                    MinimiumTimeUntillBreak,
                    javax.swing.GroupLayout.DEFAULT_SIZE,
                    82,
                    Short.MAX_VALUE).addComponent(
                    MinimiumTimeForBreak))).addGroup(
                    jPanel2Layout.createSequentialGroup().addGap(32,
                    32,
                    32).addComponent(
                    jLabel12)).addGroup(
                    jPanel2Layout.createSequentialGroup().addContainerGap().addComponent(
                    jLabel11)).addGroup(
                    jPanel2Layout.createSequentialGroup().addGap(36,
                    36,
                    36).addComponent(
                    jLabel10))).addContainerGap(22,
                    Short.MAX_VALUE)));
            jPanel2Layout.setVerticalGroup(jPanel2Layout.createParallelGroup(
                    javax.swing.GroupLayout.Alignment.LEADING).addGroup(
                    jPanel2Layout.createSequentialGroup().addContainerGap().addComponent(TakeBreak).addPreferredGap(
                    javax.swing.LayoutStyle.ComponentPlacement.RELATED).addGroup(
                    jPanel2Layout.createParallelGroup(
                    javax.swing.GroupLayout.Alignment.BASELINE).addComponent(
                    jLabel6).addComponent(
                    MinimiumTimeUntillBreak,
                    javax.swing.GroupLayout.PREFERRED_SIZE,
                    javax.swing.GroupLayout.DEFAULT_SIZE,
                    javax.swing.GroupLayout.PREFERRED_SIZE)).addPreferredGap(
                    javax.swing.LayoutStyle.ComponentPlacement.RELATED).addGroup(
                    jPanel2Layout.createParallelGroup(
                    javax.swing.GroupLayout.Alignment.BASELINE).addComponent(
                    jLabel7).addComponent(
                    MaxiumTimeUntillBreak,
                    javax.swing.GroupLayout.PREFERRED_SIZE,
                    javax.swing.GroupLayout.DEFAULT_SIZE,
                    javax.swing.GroupLayout.PREFERRED_SIZE)).addPreferredGap(
                    javax.swing.LayoutStyle.ComponentPlacement.RELATED).addGroup(
                    jPanel2Layout.createParallelGroup(
                    javax.swing.GroupLayout.Alignment.BASELINE).addComponent(
                    jLabel8).addComponent(
                    MinimiumTimeForBreak,
                    javax.swing.GroupLayout.PREFERRED_SIZE,
                    20,
                    javax.swing.GroupLayout.PREFERRED_SIZE)).addPreferredGap(
                    javax.swing.LayoutStyle.ComponentPlacement.RELATED).addGroup(
                    jPanel2Layout.createParallelGroup(
                    javax.swing.GroupLayout.Alignment.BASELINE).addComponent(
                    jLabel9).addComponent(
                    MaxiumTimeForBreak,
                    javax.swing.GroupLayout.PREFERRED_SIZE,
                    javax.swing.GroupLayout.DEFAULT_SIZE,
                    javax.swing.GroupLayout.PREFERRED_SIZE)).addPreferredGap(
                    javax.swing.LayoutStyle.ComponentPlacement.RELATED,
                    72, Short.MAX_VALUE).addComponent(
                    jLabel10,
                    javax.swing.GroupLayout.PREFERRED_SIZE,
                    13,
                    javax.swing.GroupLayout.PREFERRED_SIZE).addPreferredGap(
                    javax.swing.LayoutStyle.ComponentPlacement.RELATED).addComponent(
                    jLabel11,
                    javax.swing.GroupLayout.PREFERRED_SIZE,
                    13,
                    javax.swing.GroupLayout.PREFERRED_SIZE).addPreferredGap(
                    javax.swing.LayoutStyle.ComponentPlacement.RELATED).addComponent(jLabel12).addContainerGap()));

            jTabbedPane1.addTab("Break Handler", jPanel2);

            javax.swing.GroupLayout layout = new javax.swing.GroupLayout(
                    getContentPane());
            getContentPane().setLayout(layout);
            layout.setHorizontalGroup(layout.createParallelGroup(
                    javax.swing.GroupLayout.Alignment.LEADING).addGroup(
                    layout.createSequentialGroup().addGroup(
                    layout.createParallelGroup(
                    javax.swing.GroupLayout.Alignment.LEADING).addGroup(
                    layout.createSequentialGroup().addGap(91,
                    91,
                    91).addComponent(
                    jLabel2)).addGroup(
                    layout.createSequentialGroup().addContainerGap().addComponent(
                    jLabel1,
                    javax.swing.GroupLayout.DEFAULT_SIZE,
                    309,
                    Short.MAX_VALUE)).addGroup(
                    javax.swing.GroupLayout.Alignment.TRAILING,
                    layout.createSequentialGroup().addContainerGap(
                    javax.swing.GroupLayout.DEFAULT_SIZE,
                    Short.MAX_VALUE).addComponent(
                    jTabbedPane1,
                    javax.swing.GroupLayout.PREFERRED_SIZE,
                    309,
                    javax.swing.GroupLayout.PREFERRED_SIZE))).addContainerGap()));
            layout.setVerticalGroup(layout.createParallelGroup(
                    javax.swing.GroupLayout.Alignment.LEADING).addGroup(
                    layout.createSequentialGroup().addComponent(
                    jLabel1,
                    javax.swing.GroupLayout.PREFERRED_SIZE,
                    javax.swing.GroupLayout.DEFAULT_SIZE,
                    javax.swing.GroupLayout.PREFERRED_SIZE).addPreferredGap(
                    javax.swing.LayoutStyle.ComponentPlacement.RELATED).addComponent(
                    jTabbedPane1,
                    javax.swing.GroupLayout.DEFAULT_SIZE,
                    294, Short.MAX_VALUE).addPreferredGap(
                    javax.swing.LayoutStyle.ComponentPlacement.RELATED).addComponent(jLabel2)));

            pack();
        }// </editor-fold>

        // <editor-fold defaultstate="collapsed" desc="GUI Actions">
        private void StartActionPerformed(java.awt.event.ActionEvent evt) {
            startScript = true;
            switch (ChooseTeam.getSelectedIndex()) {
                case 0:
                    randomTeam = true;
                    redTeam = false;
                    blueTeam = false;
                    lastWonTeam = false;
                    lastLostTeam = false;
                    clanChatTeam = false;
                    break;
                case 1:
                    randomTeam = false;
                    redTeam = true;
                    blueTeam = false;
                    lastWonTeam = false;
                    lastLostTeam = false;
                    clanChatTeam = false;
                    break;
                case 2:
                    randomTeam = false;
                    redTeam = false;
                    blueTeam = true;
                    lastWonTeam = false;
                    lastLostTeam = false;
                    clanChatTeam = false;
                    break;
                case 3:
                    randomTeam = false;
                    redTeam = false;
                    blueTeam = false;
                    lastWonTeam = true;
                    lastLostTeam = false;
                    clanChatTeam = false;
                    break;
                case 4:
                    randomTeam = false;
                    redTeam = false;
                    blueTeam = false;
                    lastWonTeam = false;
                    lastLostTeam = true;
                    clanChatTeam = false;
                    break;
                case 5:
                    randomTeam = false;
                    redTeam = false;
                    blueTeam = false;
                    lastWonTeam = false;
                    lastLostTeam = false;
                    clanChatTeam = true;
                    break;
            }
            switch (ChooseActivity.getSelectedIndex()) {
                case 0:
                    randomStrat = true;
                    attackPlayers = false;
                    attackPyres = false;
                    attackJellies = false;
                    pureMode = false;
                    break;
                case 1:
                    randomStrat = false;
                    attackPlayers = true;
                    attackPyres = false;
                    attackJellies = false;
                    pureMode = false;
                    break;
                case 2:
                    randomStrat = false;
                    attackPlayers = false;
                    attackPyres = true;
                    attackJellies = false;
                    pureMode = false;
                    break;
                case 3:
                    randomStrat = false;
                    attackPlayers = false;
                    attackPyres = false;
                    attackJellies = true;
                    pureMode = false;
                    break;
                case 4:
                    randomStrat = false;
                    attackPlayers = false;
                    attackPyres = false;
                    attackJellies = false;
                    pureMode = true;
                    break;
            }
            startScript = true;
            maxiumTimeForBreak = Integer.parseInt(MaxiumTimeForBreak.getText());
            minimiumTimeForBreak = Integer.parseInt(MinimiumTimeForBreak.getText());
            maxiumTimeUntillBreak = Integer.parseInt(MaxiumTimeUntillBreak.getText());
            minimiumTimeUntillBreak = Integer.parseInt(MinimiumTimeUntillBreak.getText());
            healOthers = HealOthers.isSelected();
            pickUpBones = PickUpBones.isSelected();
            attackAvatar = AttackAvatar.isSelected();
            getSupplies = GetSupplies.isSelected();
            takeBreak = TakeBreak.isSelected();
            pickUpArrows = PickUpArrows.isSelected();
            buryAtGrave = BuryAtGrave.isSelected();
            weaponSpec = WeponSpec.isSelected();
            quickPrayer = QuickPrayer.isSelected();
            attackEverywhere = AttackEverywhere.isSelected();
            enableSummoning = EnableSummoning.isSelected();
            withdrawPouches = WithdrawPouches.isSelected();
            useScrolls = UseScrolls.isSelected();
            chosenFamiliar = farmiliarNameToObject(allFamiliars()[ChooseFamiliar.getSelectedIndex()]);
            chosenLocationString = allCombatLocations()[ChooseLocation.getSelectedIndex()];
        }

        private void infoActionPerformed(java.awt.event.ActionEvent evt) {
            String info = "http://www.powerbot.org/vb/showthread.php?t=560367";
            try {
                java.awt.Desktop.getDesktop().browse(java.net.URI.create(info));
            } catch (Exception e) {
            }
        }

        private void donateActionPerformed(java.awt.event.ActionEvent evt) {
            String donate = "https://www.paypal.com/cgi-bin/webscr?cmd=_s-xclick&hosted_button_id=3QP2NLZS3N65W";
            try {
                java.awt.Desktop.getDesktop().browse(
                        java.net.URI.create(donate));
            } catch (Exception e) {
            }
        }
        // </editor-fold>
        // <editor-fold defaultstate="collapsed" desc="Variables declaration">
        private javax.swing.JCheckBox AttackAvatar;
        private javax.swing.JCheckBox AttackEverywhere;
        private javax.swing.JCheckBox BuryAtGrave;
        private javax.swing.JComboBox ChooseActivity;
        private javax.swing.JComboBox ChooseFamiliar;
        private javax.swing.JComboBox ChooseLocation;
        private javax.swing.JComboBox ChooseTeam;
        private javax.swing.JCheckBox GetSupplies;
        private javax.swing.JCheckBox HealOthers;
        private javax.swing.JTextField MaxiumTimeForBreak;
        private javax.swing.JTextField MaxiumTimeUntillBreak;
        private javax.swing.JTextField MinimiumTimeForBreak;
        private javax.swing.JTextField MinimiumTimeUntillBreak;
        private javax.swing.JCheckBox PickUpArrows;
        private javax.swing.JCheckBox PickUpBones;
        private javax.swing.JCheckBox QuickPrayer;
        private javax.swing.JButton Start;
        private javax.swing.JCheckBox EnableSummoning;
        private javax.swing.JCheckBox TakeBreak;
        private javax.swing.JCheckBox UseScrolls;
        private javax.swing.JCheckBox WeponSpec;
        private javax.swing.JCheckBox WithdrawPouches;
        private javax.swing.JButton donate;
        private javax.swing.JButton info;
        private javax.swing.JButton jButton1;
        private javax.swing.JComboBox jComboBox1;
        private javax.swing.JComboBox jComboBox2;
        private javax.swing.JLabel jLabel1;
        private javax.swing.JLabel jLabel10;
        private javax.swing.JLabel jLabel11;
        private javax.swing.JLabel jLabel12;
        private javax.swing.JLabel jLabel13;
        private javax.swing.JLabel jLabel14;
        private javax.swing.JLabel jLabel15;
        private javax.swing.JLabel jLabel16;
        private javax.swing.JLabel jLabel17;
        private javax.swing.JLabel jLabel18;
        private javax.swing.JLabel jLabel2;
        private javax.swing.JLabel jLabel3;
        private javax.swing.JLabel jLabel4;
        private javax.swing.JLabel jLabel5;
        private javax.swing.JLabel jLabel6;
        private javax.swing.JLabel jLabel7;
        private javax.swing.JLabel jLabel8;
        private javax.swing.JLabel jLabel9;
        private javax.swing.JList jList1;
        private javax.swing.JPanel jPanel1;
        private javax.swing.JPanel jPanel2;
        private javax.swing.JPanel jPanel3;
        private javax.swing.JPanel jPanel4;
        private javax.swing.JScrollPane jScrollPane1;
        private javax.swing.JSeparator jSeparator1;
        private javax.swing.JTabbedPane jTabbedPane1;
        // End of variables declaration
        // </editor-fold>
    }

    // </editor-fold>
    // <editor-fold defaultstate="collapsed" desc="MessageListener">
    @Override
    public void messageReceived(MessageEvent e) {
        String message = e.getMessage().toLowerCase();
        if (e.getID() == MessageEvent.MESSAGE_SERVER
                || e.getID() == MessageEvent.MESSAGE_CLIENT
                || e.getID() == MessageEvent.MESSAGE_ACTION) {
            if (message.contains("you receive 1 zeal")) {
                lost++;
                result = "lostLast";
                zeal++;
            } else if (message.contains("you receive 2 zeal")) {
                drew++;
                result = "drewLast";
                zeal += 2;
            } else if (message.contains("you receive 3 zeal")) {
                won++;
                result = "wonLast";
                zeal += 3;
            } else if (message.contains("This chat is currently full")) {
                inClan = false;
            } else if (message.contains("You have been kicked from the channel")) {
                inClan = false;
                log("You got kicked from clan channel!");
            } else if (message.contains("You are temporarily banned from this clan channel")) {
                inClan = false;
                log("You got banned from clan channel!");
            } else if (message.contains("You cannot take non-combat items into the arena")) {
                log("You have something that it stopping us from entering a game.");
                game.logout(true);
                stopScript();
            }
            // Grave stuff.
            if (message.contains("eastern")) {
                if (message.contains("taken")) {
                    if (message.contains("red")) {
                        EasternGraveyard = 2;
                    } else if (message.contains("blue")) {
                        EasternGraveyard = 1;
                    }
                } else if (message.contains("lost")) {
                    EasternGraveyard = 0;
                }
            } else if (message.contains("western")) {
                if (message.contains("taken")) {
                    if (message.contains("red")) {
                        WesternGraveyard = 2;
                    } else if (message.contains("blue")) {
                        WesternGraveyard = 1;
                    }
                } else if (message.contains("lost")) {
                    WesternGraveyard = 0;
                }
            }
        }

        if (clanChatTeam && listenTime()) {
            if (e.getID() == MessageEvent.MESSAGE_CLAN_CHAT) {
                message.replace("-", "");
                message.replace("=", "");
                message.replace(";", "");
                message.replace(":", "");
                message.replace("*", "");
                message.replace("$", "");
                message.replace("\u00A3", "");
                message.replace("_", "");
                message.replace(" ", "");
                if (!message.contains("bot") && !message.contains("winnin")
                        && !message.contains("losin")) {
                    if (message.contains("blu") || message.contains("b1u")) {
                        blueShouts += 1;
                    } else if (message.contains("red")
                            || message.contains("4ed")
                            || message.contains("r3d")
                            || message.contains("43d")) {
                        redShouts += 1;
                    }
                }
            }
        }
    }

    // </editor-fold>
    // <editor-fold defaultstate="collapsed" desc="Paint">
    private Image getImage(String url) {
        try {
            return ImageIO.read(new URL(url));
        } catch (IOException e) {
            return null;
        }
    }
    private final Image logo = getImage("http://img43.imageshack.us/img43/5479/logoym.png");
    private final Image hideImg = getImage("http://www.authorstream.com/images/close_icon.gif");

    private String intToStringTeam(int team) {
        switch (team) {
            case 0:
                return "Unowned";
            case 1:
                return "Blue";
            case 2:
                return "Red";
        }
        return "";
    }

    @Override
    public void onRepaint(Graphics g1) {
        Graphics2D g = (Graphics2D) g1;
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        if (!hide) {

            hideRect = new Rectangle(503, 210, 14, 14);
            g.setColor(new Color(0, 0, 0, 100));
            g.fillRect(0, 208, 519, 130);
            g.setColor(new Color(0, 0, 0));
            g.setStroke(new BasicStroke(1));
            g.drawRect(0, 208, 519, 130);
            g.drawImage(logo, 2, 209, null);
            g.setFont(new Font("SansSerif", 0, 15));
            g.setColor(new Color(255, 255, 255, 200));
            g.drawString(
                    "Version: "
                    + getClass().getAnnotation(ScriptManifest.class).version(), 119, 332);
            g.setFont(new Font("SansSerif", 0, 10));
            g.drawString("Time:  " + run.toElapsedString(), 300, 219);
            g.drawString("Current Task:  " + task, 300, 234);
            g.drawString("Zeal:  " + zeal, 300, 251);
            g.drawString("Won:  " + won, 300, 268);
            g.drawString("Lost:  " + lost, 300, 285);
            g.drawString("Drew:  " + drew, 300, 302);
            g.drawString("Kicked:  " + kicked, 300, 319);
            if (clanChatTeam) {
                g.drawString("Clan Team Calls", 420, 285);
                g.drawString("Blue:  " + blueShouts, 420, 302);
                g.drawString("Red:  " + redShouts, 420, 319);
            }
            if (takeBreak) {
                g.drawString("" + breakHandlerStatus, 300, 336);
            }
            g.drawString("Eastern Graveyard : "
                    + intToStringTeam(EasternGraveyard), 119, 302);
            g.drawString("Western Graveyard : "
                    + intToStringTeam(WesternGraveyard), 119, 312);

            g.drawImage(hideImg, 504, 210, null);
        } else {
            hideRect = new Rectangle(503, 324, 14, 14);
            g.setColor(new Color(0, 0, 0, 100));
            g.fillRect(368, 322, 150, 16);
            g.setColor(new Color(0, 0, 0));
            g.setStroke(new BasicStroke(1));
            g.drawRect(368, 322, 150, 16);
            g.drawImage(hideImg, 503, 324, null);
            g.setColor(new Color(255, 255, 255, 200));
            g.setFont(new Font("SansSerif", 0, 10));
            g.drawString("Time: " + run.toElapsedString() + " | Zeal: " + zeal,
                    372, 335);
        }
    }

    // </editor-fold>
    // <editor-fold defaultstate="collapsed" desc="MouseListener">
    @Override
    public void mouseClicked(MouseEvent e) {
        Point p = e.getPoint();
        if (hideRect.contains(p)) {
            hide = (hide ? false : true);
        }
    }

    @Override
    public void mousePressed(MouseEvent e) {
    }

    @Override
    public void mouseReleased(MouseEvent e) {
    }

    @Override
    public void mouseEntered(MouseEvent e) {
    }

    @Override
    public void mouseExited(MouseEvent e) {
    }

    // </editor-fold>
    // <editor-fold defaultstate="collapsed" desc="Antiban">
    class Angle extends Thread {

        private volatile boolean stop = false;
        private volatile boolean pause = false;

        @Override
        public void run() {
            try {
                while (!stop) {
                    if (takingBreak) {
                        while (takingBreak) {
                            Thread.sleep(random(25, 150));
                        }
                    }
                    if (pause) {
                        while (pause) {
                            Thread.sleep(random(25, 150));
                        }
                    }
                    if (random(1, 15000) == 342) {
                        char key = 0;
                        if (random(1, 5) == 2) {
                            key = KeyEvent.VK_RIGHT;
                        }
                        if (random(1, 5) == 2) {
                            key = KeyEvent.VK_LEFT;
                        }
                        final long endTime = System.currentTimeMillis()
                                + random(250, 1200);
                        if (key != 0) {
                            keyboard.pressKey(key);
                            while (System.currentTimeMillis() < endTime) {
                                if (pause) {
                                    break;
                                }
                                Thread.sleep(random(5, 15));
                            }
                            keyboard.releaseKey(key);
                        }
                        Thread.sleep(random(random(6000, 8000),
                                random(19000, 20000)));
                    }
                }
            } catch (Exception e) {
            }
        }
    }

    class Pitch extends Thread {

        private volatile boolean stop = false;
        private volatile boolean pause = false;

        @Override
        public void run() {
            try {
                while (!stop) {
                    if (takingBreak) {
                        while (takingBreak) {
                            Thread.sleep(random(25, 150));
                        }
                    }
                    if (pause) {
                        while (pause) {
                            Thread.sleep(random(25, 150));
                        }
                    }
                    if (random(1, 15000) == 342) {
                        char key = 0;
                        if (camera.getPitch() < random(random(50, 86), 100)) {
                            key = KeyEvent.VK_UP;
                        }
                        if (camera.getPitch() >= random(random(50, 86), 100)) {
                            key = KeyEvent.VK_DOWN;
                        }
                        if (key != 0) {
                            keyboard.pressKey(key);
                            final long endTime = System.currentTimeMillis()
                                    + random(random(50, 150), 500);
                            while (System.currentTimeMillis() < endTime) {
                                if (pause) {
                                    break;
                                } else if (camera.getPitch() == 0) {
                                    if (key == KeyEvent.VK_DOWN) {
                                        break;
                                    }
                                } else if (camera.getPitch() == 100) {
                                    if (key == KeyEvent.VK_UP) {
                                        break;
                                    }
                                }
                                Thread.sleep(random(5, 15));
                            }
                            keyboard.releaseKey(key);
                        }
                        Thread.sleep(random(random(6000, 8000),
                                random(19000, 20000)));
                    }
                }
            } catch (Exception e) {
            }
        }
    }

    // </editor-fold>
    // <editor-fold defaultstate="collapsed" desc="Break Handler">
    class BreakHandler extends Thread {

        private volatile boolean stop = false;

        @Override
        public void run() {
            try {
                while (!stop) {
                    if (takeBreak) {
                        long takingBreakIn = random(
                                minimiumTimeUntillBreak * 60000,
                                maxiumTimeUntillBreak * 60000);
                        long ranFor = System.currentTimeMillis();
                        while ((ranFor + takingBreakIn) > System.currentTimeMillis() && !stop) {
                            Thread.sleep(1);
                            breakHandlerStatus = "Taking break in:  "
                                    + Timer.format((ranFor + takingBreakIn)
                                    - System.currentTimeMillis());
                        }
                        takingBreak = true;
                        while (!startedBreak && !stop) {
                            Thread.sleep(1);
                            breakHandlerStatus = "Taking break after this game";
                        }
                        long takingBreakFor = random(
                                minimiumTimeForBreak * 60000,
                                maxiumTimeForBreak * 60000);
                        long breakingFor = System.currentTimeMillis();
                        while ((breakingFor + takingBreakFor) > System.currentTimeMillis() && !stop) {
                            Thread.sleep(1);
                            breakHandlerStatus = "Taking break for:  "
                                    + Timer.format(((breakingFor + takingBreakFor) - System.currentTimeMillis()));
                        }
                        takingBreak = false;
                        while (!stoppedBreak && !stop) {
                            Thread.sleep(1);
                            breakHandlerStatus = "Logging back in hopefully...";
                        }
                    }
                }
            } catch (Exception e) {
            }
        }
    }

    // </editor-fold>
    // <editor-fold defaultstate="collapsed" desc="Finish">
    @Override
    public void onFinish() {
        angle.stop = true;
        pitch.stop = true;
        if (breakHandler != null) {
            breakHandler.stop = true;
        }
        log("Thanks for using DebaucherySoulWars");
    }
    // </editor-fold>
}
