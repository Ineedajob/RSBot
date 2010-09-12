package org.rsbot.client;

import java.awt.Canvas;
import java.awt.Rectangle;

import org.rsbot.client.input.Keyboard;
import org.rsbot.client.input.Mouse;

public interface Client {

    ChatLine[] getChatLines();

    NodeList getActionDataList();

    int getBaseX();

    int getBaseY();

    Callback getCallBack();
    
    Canvas getCanvas();

    int getCameraPitch();

    int getCameraYaw();

    int getCamPosX();

    int getCamPosY();

    int getCamPosZ();

    String getCurrentPassword();

    String getCurrentUsername();

    int getDestX();

    int getDestY();

    DetailInfo getDetailInfo();

    byte[][][] getGroundByteArray();

    int getGUIRSInterfaceIndex();

    int getIdleTime();

    Keyboard getKeyboard();

    int getLoginIndex();

    int getLoopCycle();

    int getMenuOptionsCount();

    int getMenuX();

    int getMenuY();

    int getMinimapAngle();

    float getMinimapOffset();

    int getMinimapScale();

    int getMinimapSetting();

    Mouse getMouse();

    //MouseWheel getMouseWheel();

    RSPlayer getMyRSPlayer();

    int getPlane();

    int getPublicChatMode();

    RSGround[][][] getRSGroundArray();

    RSGroundData[] getRSGroundDataArray();

    StatusNodeList getRSInteractingDefList();

    Rectangle[] getRSInterfaceBoundsArray();

    RSInterface[][] getRSInterfaceCache();

    NodeCache getRSInterfaceNC();

    NodeCache getRSItemNodeCache();

    NodeCache getRSNPCNC();

    int getRSNPCCount();

    int[] getRSNPCIndexArray();

    RSPlayer[] getRSPlayerArray();

    int getRSPlayerCount();

    int[] getRSPlayerIndexArray();

    String getSelectedItemName();

    int getSelfInteracting();

    Settings getSettingArray();

    Signlink getSignLink();

    int[] getSkillExperiences();

    int[] getSkillExperiencesMax();

    int[] getSkillLevelMaxes();

    int[] getSkillLevels();

    TileData[] getTileData();

    boolean[] getValidRSInterfaceArray();

    boolean isFlagged();

    int isItemSelected();

    boolean isMenuOpen();

    boolean isSpellSelected();

    RSItemDefFactory getRSItemDefFactory();
    
    RSNPCDefFactory getRSNPCDefFactory();
    
    RSObjectDefFactory getRSObjectDefFactory();

    void setCallback(Callback cb);

}
