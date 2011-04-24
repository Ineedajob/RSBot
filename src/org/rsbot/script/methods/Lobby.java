package org.rsbot.script.methods;

import org.rsbot.script.wrappers.RSComponent;

import java.util.ArrayList;

/**
 * Methods for lobby interface
 *
 * @Author Debauchery
 */
@SuppressWarnings("unused")
public class Lobby extends MethodProvider {

    public Lobby(MethodContext ctx) {
        super(ctx);
    }

    private static final int SELECTED_TEXTURE = 4671;

    public final static int TAB_PLAYER_INFO = 0;
    public final static int TAB_WORLD_SELECT = 1;
    public final static int TAB_FRIENDS = 2;
    public final static int TAB_FRIENDS_CHAT = 3;
    public final static int TAB_CLAN_CHAT = 4;
    public final static int TAB_OPTIONS = 5;

    private final static int PLAYER_INFO_INTERFACE = 906;
    private final static int PLAYER_INFO_INTERFACE_PLAY_BUTTON = 106;
    private final static int LOGOUT_COMPONENT = 195;

    private final static int WORLD_SELECT_INTERFACE = 910;
    private final static int WORLD_SELECT_INTERFACE_CURRENT_WORLD = 11;
    private final static int WORLD_SELECT_INTERFACE_WORLD_LIST = 77;
    private final static int WORLD_SELECT_INTERFACE_WORLD_NAME = 69;
    private final static int WORLD_SELECT_INTERFACE_AMOUNT_OF_PLAYERS = 71;
    private final static int WORLD_SELECT_INTERFACE_WORLD_ACTIVITY = 72;
    private final static int WORLD_SELECT_INTERFACE_WORLD_TYPE = 74;
    private final static int WORLD_SELECT_INTERFACE_WORLD_PING = 76;
    private final static int WORLD_SELECT_INTERFACE_SCROLL_AREA = 86;
    private final static int WORLD_SELECT_INTERFACE_SCROLL_BAR = 1;

    private final static int FRIENDS_INTERFACE = 909;

    private final static int FRIENDS_CHAT_INTERFACE = 589;

    private final static int CLAN_CHAT_INTERFACE = 912;

    private final static int OPTIONS_INTERFACE = 978;

    private final static int[] TABS = new int[]{188, 189, 190, 191, 192, 193};
    private final static int[] TABS_TEXTURE = new int[]{0, 12, 11, 254, 10, 9};

    /**
     * Checks that current game is in lobby.
     *
     * @return <tt>true</tt> if the tab is opened.
     */
    public boolean inLobby() {
        return methods.game.getClientState() == Game.INDEX_LOBBY_SCREEN;
    }

    /**
     * Gets the currently open tab.
     *
     * @return The currently open tab or the logout tab by default.
     */
    public int getCurrentTab() {
        if (!inLobby()) {
            return -1;
        }
        for (int i = 0; i < TABS.length; i++) {
            if (methods.interfaces.getComponent(TAB_PLAYER_INFO, TABS_TEXTURE[i]).getBackgroundColor() == SELECTED_TEXTURE) {
                return i;
            }
        }
        return 1;
    }

    /**
     * Opens the specified tab at the specified index.
     *
     * @param i The tab to open.
     * @return <tt>true</tt> if tab successfully selected; otherwise
     *         <tt>false</tt>.
     */
    public boolean open(int i) {
        if (inLobby()) {
            if (i == getCurrentTab()) {
                return true;
            } else {
                methods.interfaces.getComponent(PLAYER_INFO_INTERFACE, TABS[i]).doClick();
                sleep(random(400, 700));
            }
        }
        return i == getCurrentTab();
    }

    /**
     * Finds out which world is selected from the lobby interface.
     *
     * @return The world number that is currently selected
     */
    public int getSelectedWorld() {
        if (!inLobby()) {
            return -1;
        }
        if (!methods.interfaces.get(WORLD_SELECT_INTERFACE).isValid() || getCurrentTab() != TAB_WORLD_SELECT) {
            open(TAB_WORLD_SELECT);
        }
        if (methods.interfaces.getComponent(WORLD_SELECT_INTERFACE, WORLD_SELECT_INTERFACE_CURRENT_WORLD).isValid()) {
            String worldText = methods.interfaces.getComponent(WORLD_SELECT_INTERFACE,
                    WORLD_SELECT_INTERFACE_CURRENT_WORLD).getText().trim().substring(
                    methods.interfaces.getComponent(
                            WORLD_SELECT_INTERFACE, WORLD_SELECT_INTERFACE_CURRENT_WORLD).getText().trim().indexOf(
                            "World ") + 6);
            return Integer.parseInt(worldText);
        }
        return -1;
    }

    /**
     * Finds all available worlds if in lobby.
     *
     * @param includingFull If true it will include all full worlds when returned
     * @return All available worlds as a String arrat
     */
    public String[] getAvailableWorlds(boolean includingFull) {
        ArrayList<String> tempList = new ArrayList<String>();
        if (!inLobby()) {
            return new String[0];
        }
        if (!methods.interfaces.get(WORLD_SELECT_INTERFACE).isValid() || getCurrentTab() != TAB_WORLD_SELECT) {
            open(TAB_WORLD_SELECT);
            sleep(500);
        }
        for (int i = 0; i < methods.interfaces.getComponent(WORLD_SELECT_INTERFACE,
                WORLD_SELECT_INTERFACE_WORLD_NAME).getComponents().length;
             i++) {
            String amount = methods.interfaces.getComponent(WORLD_SELECT_INTERFACE,
                    WORLD_SELECT_INTERFACE_AMOUNT_OF_PLAYERS).getComponents()[i].getText();
            String number = methods.interfaces.getComponent(WORLD_SELECT_INTERFACE,
                    WORLD_SELECT_INTERFACE_WORLD_NAME).getComponents()[i].getText();
            if (!amount.contains("OFFLINE") && !amount.contains("0")) {
                if (!includingFull) {
                    if (!amount.contains("FULL")) {
                        tempList.add(number);
                    }
                } else {
                    tempList.add(number);
                }
            }
        }
        {
            String[] temp = new String[tempList.size()];
            tempList.toArray(temp);
            return temp;
        }
    }


    /**
     * Checks if the chosen world is open.
     *
     * @param world
     * @param includeFull
     * @return <tt>true</tt> is available, else <tt>false</tt>
     */
    public boolean isAvailable(int world, boolean includeFull) {
        for (String s : getAvailableWorlds(includeFull)) {
            if (Integer.parseInt(s) == world) {
                return true;
            }
        }
        return false;
    }

    /**
     * Enters a world from the lobby.
     *
     * @param world
     * @return <tt>true</tt> If correctly entered the world else <tt>false</tt>
     * @see org.rsbot.script.methods.Game switchWorld(int world)
     */
    public boolean switchWorlds(int world) {
        if (!inLobby()) {
            return false;
        }
        if (!methods.interfaces.get(WORLD_SELECT_INTERFACE).isValid() || getCurrentTab() != TAB_WORLD_SELECT) {
            open(TAB_WORLD_SELECT);
            sleep(random(600, 800));
        }
        if (getSelectedWorld() == world) {
            methods.interfaces.getComponent(PLAYER_INFO_INTERFACE, PLAYER_INFO_INTERFACE_PLAY_BUTTON).doClick();
        }
        if (isAvailable(world, false)) {
            RSComponent comp = getWorldComponent(world);
            if (comp != null) {
                methods.interfaces.scrollTo(comp, methods.interfaces.getComponent(WORLD_SELECT_INTERFACE,
                        WORLD_SELECT_INTERFACE_SCROLL_AREA));
                comp.doClick();
                sleep(random(500, 800));
                if (getSelectedWorld() == world) {
                    methods.interfaces.getComponent(PLAYER_INFO_INTERFACE, PLAYER_INFO_INTERFACE_PLAY_BUTTON).doClick();
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Gets the component of any world on the lobby interface
     *
     * @param world
     * @return  The component corresponding to the world.
     */
    public RSComponent getWorldComponent(int world) {
        if(!inLobby()){
            return null;
        }
        if(!methods.interfaces.get(WORLD_SELECT_INTERFACE).isValid()){
            open(TAB_WORLD_SELECT);
        }
        for (int i = 0; i < methods.interfaces.getComponent(WORLD_SELECT_INTERFACE,
                WORLD_SELECT_INTERFACE_WORLD_NAME).getComponents().length;
             i++) {
            RSComponent comp = methods.interfaces.getComponent(WORLD_SELECT_INTERFACE,
                    WORLD_SELECT_INTERFACE_WORLD_NAME).getComponents()[i];
            if (comp != null) {
                String number = comp.getText();
                if (Integer.parseInt(number) == world) {
                    return methods.interfaces.getComponent(WORLD_SELECT_INTERFACE,
                            WORLD_SELECT_INTERFACE_WORLD_LIST).getComponents()[i];
                }
            }
        }
        return null;
    }

    /**
     * Used for logging out if in lobby
     *
     * @return <tt>true</tt> if correctly logged out else false
     */
    public boolean logout() {
        if (inLobby()) {
            methods.interfaces.getComponent(PLAYER_INFO_INTERFACE, LOGOUT_COMPONENT).doClick();
        }
        return !methods.game.isLoggedIn();
    }

}
