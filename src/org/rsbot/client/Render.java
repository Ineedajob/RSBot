package org.rsbot.client;

/**
 * Global interface for Render, implemented in both the low detail and high
 * detail Render classes.
 */
public interface Render {

    // Absolute coordinates, float because of the HD
    float getAbsoluteX1();

    float getAbsoluteX2();

    float getAbsoluteY1();

    float getAbsoluteY2();

    // x and y multipliers
    int getXMultiplier();

    int getYMultiplier();

    // zNear and zFar
    int getZFar();

    int getZNear();
}
