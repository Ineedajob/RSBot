package org.rsbot.client;

/**
 * GraphicsToolkit
 */
public interface Render {

	float getAbsoluteX1();

	float getAbsoluteX2();

	float getAbsoluteY1();

	float getAbsoluteY2();

	int getXMultiplier();

	int getYMultiplier();

	int getZFar();

	int getZNear();

}
