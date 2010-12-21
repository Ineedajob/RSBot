package org.rsbot.client;

public interface RSCharacter extends RSAnimable {

	int getAnimation();

	int getGraphic();

	int getHeight();

	int getHPRatio();

	int getInteracting();

	int[] getLocationX();

	int[] getLocationY();

	int getOrientation();

	int getLoopCycleStatus();

	String getMessage();

	int isMoving();

	Model getModel();

}
