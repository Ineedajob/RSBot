package org.rsbot.client;

public interface RSInterface {

	String[] getActions();

	int getBorderThickness();

	int getBoundsArrayIndex();

	int getComponentID();

	int getComponentIndex();

	String getComponentName();

	RSInterface[] getComponents();

	int getComponentStackSize();

	int getHeight();

	int getHeight2();

	int getHorizontalScrollBarSize();

	int getHorizontalScrollBarThumbPosition();

	int getHorizontalScrollBarThumbSize();

	int getID();

	int[] getInventory();

	int[] getInventoryStackSizes();

	int getInvSpritePadX();

	int getInvSpritePadY();

	int getMasterX();

	int getMasterY();

	int getModelID();

	int getModelType();

	int getModelZoom();

	int getParentID();

	String getSelectedActionName();

	int getShadowColor();

	int getSpecialType();

	String getSpellName();

	String getText();

	int getTextColor();

	int getTextureID();

	String getToolTip();

	int getType();

	int[][] getValueIndexArray();

	int getVerticalScrollBarPosition();

	int getVerticalScrollBarSize();

	int getVerticalScrollBarThumbSize();

	int getWidth();

	int getWidth2();

	int getX();

	int getXRotation();

	int getY();

	int getYRotation();

	int getZRotation();

	boolean isHorizontallyFlipped();

	boolean isInventoryRSInterface();

	boolean isVerticallyFlipped();

}
