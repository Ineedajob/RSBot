package org.rsbot.script.randoms;

import org.rsbot.script.Random;
import org.rsbot.script.ScriptManifest;
import org.rsbot.script.wrappers.RSInterface;
import org.rsbot.script.wrappers.RSComponent;

import java.awt.*;

/**
 * @author Iscream
 */
@ScriptManifest(authors = {"Iscream"}, name = "LoginBot", version = 1.1)
public class LoginBot extends Random {

    private static final int INTERFACE_MAIN = 905;
    private static final int INTERFACE_MAIN_CHILD = 59;
    private static final int INTERFACE_MAIN_CHILD_COMPONENT_ID = 4;
    private static final int INTERFACE_LOGIN_SCREEN = 596;
    private static final int INTERFACE_USERNAME = 71;
    private static final int INTERFACE_USERNAME_WINDOW = 67;
    private static final int INTERFACE_PASSWORD = 91;
    private static final int INTERFACE_PASSWORD_WINDOW = 74;
    private static final int INTERFACE_BUTTON_LOGIN = 75;
    private static final int INTERFACE_TEXT_RETURN = 30;
    private static final int INTERFACE_WELCOME_SCREEN = 906;
    private static final int INTERFACE_WELCOME_SCREEN_BUTTON_PLAY_1 = 178;
    private static final int INTERFACE_WELCOME_SCREEN_BUTTON_PLAY_2 = 180;

    private static final int INDEX_LOGGED_OUT = 3;
    private static final int INDEX_LOBBY = 7;

    private int invalidCount, worldFullCount;

    public boolean activateCondition() {
        int idx = game.getLoginIndex();
        return idx == INDEX_LOGGED_OUT || idx == INDEX_LOBBY;
    }

    public int loop() {
        String username = account.getName().replaceAll("_", " ").toLowerCase().trim();
        String returnText = interfaces.get(INTERFACE_LOGIN_SCREEN).
                getComponent(INTERFACE_TEXT_RETURN).getText().toLowerCase();
        int textlength;
        if (game.getLoginIndex() != INDEX_LOGGED_OUT) {
            if (!game.isWelcomeScreen()) {
                sleep(random(1000, 2000));
            }
            if (game.getLoginIndex() == INDEX_LOBBY) {
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

                for (int i = 0; i < 4 && game.getLoginIndex() == 6; i++)
                    sleep(500);
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
            if (returnText.contains("members")) {
                log("We have attempted to log into a members world as a free to play player, stopping script.");
                stopScript(false);
            }
            if (returnText.contains("incorrect")) {
                log("Failed to login five times in a row. Stopping all scripts.");
                stopScript(false);
            }
            if (returnText.contains("invalid")) {
                if (invalidCount > 6) {
                    log("Unable To Login After 6 Attempts, Stopping Script.");
                    log("Please verify that your RSBot account profile is correct.");
                    stopScript(false);
                }
                interfaces.get(INTERFACE_LOGIN_SCREEN).getComponent(38).doClick();
                invalidCount++;
                return (random(500, 2000));
            }
            if (returnText.contains("full")) {
                if (worldFullCount > 30) {
                    log("World Is Full. Waiting for 15 seconds.");
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
        if (game.getLoginIndex() == INDEX_LOGGED_OUT) {
            if (!atLoginScreen()) {
                interfaces.getComponent(INTERFACE_MAIN, INTERFACE_MAIN_CHILD).getComponent(INTERFACE_MAIN_CHILD_COMPONENT_ID).doAction("");
                return random(500, 600);
            }
            if (isUsernameFilled() && isPasswordFilled()) {
                interfaces.get(INTERFACE_LOGIN_SCREEN).getComponent(INTERFACE_BUTTON_LOGIN).doClick();
                return random(500, 600);
            }
            if (!isUsernameFilled()) {
                atLoginInterface(interfaces.get(INTERFACE_LOGIN_SCREEN).getComponent(INTERFACE_USERNAME_WINDOW));
                sleep(random(500, 700));
                textlength = interfaces.get(INTERFACE_LOGIN_SCREEN).getComponent(INTERFACE_USERNAME).getText().length() + random(3, 5);
                for (int i = 0; i <= textlength + random(1, 5); i++) {
                    keyboard.sendText("\b", false);
                    if (random(0, 2) == 1) {
                        sleep(random(25, 100));
                    }
                }
                keyboard.sendText(username, false);
            }
            if (isUsernameFilled() && !isPasswordFilled()) {
                atLoginInterface(interfaces.get(INTERFACE_LOGIN_SCREEN).getComponent(INTERFACE_PASSWORD_WINDOW));
                sleep(random(500, 700));
                textlength = interfaces.get(INTERFACE_LOGIN_SCREEN).getComponent(INTERFACE_PASSWORD).getText().length() + random(3, 5);
                for (int i = 0; i <= textlength + random(1, 5); i++) {
                    keyboard.sendText("\b", false);
                    if (random(0, 2) == 1) {
                        sleep(random(25, 100));
                    }
                }
                keyboard.sendText(account.getPassword(), false);
            }
        }
        return random(500, 2000);
    }

    // Clicks past all of the letters

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

    /*
     * Returns x int based on the letters in a Child
     * Only the password text is needed as the username text cannot reach past the middle of the interface
     */
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
        return interfaces.get(596).isValid();
    }

    private boolean isUsernameFilled() {
        String username = account.getName().replaceAll("_", " ").toLowerCase().trim();
        return interfaces.get(INTERFACE_LOGIN_SCREEN).getComponent(INTERFACE_USERNAME).getText().toLowerCase().equalsIgnoreCase(username);
    }

    private boolean isPasswordFilled() {
        return interfaces.get(INTERFACE_LOGIN_SCREEN).getComponent(INTERFACE_PASSWORD).getText().toLowerCase().length() == account.getPassword().length();
    }
}