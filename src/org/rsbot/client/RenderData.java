package org.rsbot.client;

/**
 * Global interface for RenderData, implemented in both the low detail and high
 * detail RenderData classes.
 */
public interface RenderData {

    // x calculation values
    float getXOff();

    float getXX();

    float getXY();

    float getXZ();

    // y calculation values
    float getYOff();

    float getYX();

    float getYY();

    float getYZ();

    // z calculation values
    float getZOff();

    float getZX();

    float getZY();

    float getZZ();

}
