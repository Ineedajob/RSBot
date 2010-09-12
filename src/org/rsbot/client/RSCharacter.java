package org.rsbot.client;

public interface RSCharacter extends RSAnimable {

    int getAnimation();

    int getHeight();

    int getHPRatio();

    int getInteracting();

    int[] getLocationX();

    int[] getLocationY();

    int getLoopCycleStatus();

    String getMessage();

    int isMoving();
    
    Model getModel();

}
