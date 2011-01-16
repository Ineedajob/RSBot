package org.rsbot.client;

public interface RSGround {

	byte getPlane1();

	byte getPlane2();

	RSAnimableNode getRSAnimableList();

	RSInteractable getFloorDecoration();

	RSInteractable getBoundary1();

	RSInteractable getBoundary2();

	RSInteractable getWallDecoration1();

	RSInteractable getWallDecoration2();

	RSGroundEntity getGroundObject();

	short getX();

	short getY();

}
