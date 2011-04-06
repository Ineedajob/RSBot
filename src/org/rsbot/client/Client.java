package org.rsbot.client;

import org.rsbot.client.input.Keyboard;
import org.rsbot.client.input.Mouse;

import java.awt.*;

public interface Client {

	ChatLine[] getChatLines();

	boolean isMenuCollapsed();

	NodeDeque getMenuItems();

	NodeSubQueue getCollapsedMenuItems();

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

	DetailInfoNode getDetailInfoNode();

	byte[][][] getGroundByteArray();

	int getGUIRSInterfaceIndex();

	int getIdleTime();

	Keyboard getKeyboard();

	int getLoginIndex();

	int getLoopCycle();

	int getMenuOptionsCount();

	int getMenuX();

	int getMenuY();

	MenuGroupNode getCurrentMenuGroupNode();

	int getSubMenuX();

	int getSubMenuY();

	int getSubMenuWidth();

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

	HashTable getRSInterfaceNC();

	HashTable getRSItemHashTable();

	HashTable getRSNPCNC();

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

	RSItemDefLoader getRSItemDefLoader();

	RSObjectDefLoader getRSObjectDefLoader();

	StatusNodeListLoader getRSInteractableDefListLoader();

	Signlink getSignlink();

	ServerData getWorldData();

	void setCallback(Callback cb);

}
