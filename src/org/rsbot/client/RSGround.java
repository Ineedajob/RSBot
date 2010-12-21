package org.rsbot.client;

public interface RSGround {

	byte getPlane1();

	byte getPlane2();

	RSAnimableNode getRSAnimableList();

	RSInteractable getRSObject1();

	RSInteractable getRSObject2_0();

	RSInteractable getRSObject2_1();

	RSInteractable getRSObject3_0();

	RSInteractable getRSObject3_1();

	RSInteractable getRSObject4();

	RSGround getUnknownRSGround();

	RSGroundEntity getGroundObject();

	short getX();

	short getY();

}
