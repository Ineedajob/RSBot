package org.rsbot.script.methods;

import org.rsbot.script.util.Timer;
import org.rsbot.script.wrappers.RSCharacter;
import org.rsbot.script.wrappers.RSObject;
import org.rsbot.script.wrappers.RSTile;

import java.awt.event.KeyEvent;

/**
 * Camera related operations.
 */
public class Camera extends MethodProvider {

    Camera(final MethodContext ctx) {
        super(ctx);
    }

    /**
     * Turns to an RSCharacter (RSNPC or RSPlayer).
     *
     * @param c The RSCharacter to turn to.
     */
    public void turnToCharacter(RSCharacter c) {
        int angle = getAngleToCharacter(c);
        setAngle(angle);
    }

    /**
     * Turns to within a few degrees of an RSCharacter (RSNPC or RSPlayer).
     *
     * @param c   The RSCharacter to turn to.
     * @param dev The maximum difference in the angle.
     */
    public void turnToCharacter(RSCharacter c, int dev) {
        int angle = getAngleToCharacter(c);
        angle = random(angle - dev, angle + dev + 1);
        setAngle(angle);
    }

    /**
     * Turns to an RSObject
     *
     * @param o The RSObject to turn to.
     */
    public void turnToObject(RSObject o) {
        int angle = getAngleToObject(o);
        setAngle(angle);
    }

    /**
     * Turns to within a few degrees of an RSObject.
     *
     * @param o   The RSObject to turn to.
     * @param dev The maximum difference in the turn angle.
     */
    public void turnToObject(RSObject o, int dev) {
        int angle = getAngleToObject(o);
        angle = random(angle - dev, angle + dev + 1);
        setAngle(angle);
    }

    /**
     * Turns to a specific RSTile.
     *
     * @param tile Tile to turn to.
     */
    public void turnToTile(RSTile tile) {
        int angle = getAngleToTile(tile);
        setAngle(angle);
    }

    /**
     * Turns within a few degrees to a specific RSTile.
     *
     * @param tile Tile to turn to.
     * @param dev  Maximum deviation from the angle to the tile.
     */
    public void turnToTile(RSTile tile, int dev) {
        int angle = getAngleToTile(tile);
        angle = random(angle - dev, angle + dev + 1);
        setAngle(angle);
    }

    /**
     * Sets the altitude to max or minimum.
     * 
     * @param maxAltitude True to go up. False to go down. 
     */
    public void setAltitude(boolean maxAltitude) {
        char key = (char) (maxAltitude ? KeyEvent.VK_UP : KeyEvent.VK_DOWN);
        methods.inputManager.pressKey(key);
        sleep(random(1000, 1500));
        methods.inputManager.releaseKey(key);
    }

    /**
     * Set the camera to a certain percentage of the maximum pitch. Don't
     * rely on the return value too much - it should return whether the camera
     * was successfully set, but it isn't very accurate near the very extremes
     * of the height.
     * <p/>
     * This also depends on the maximum camera angle in a region, as it changes
     * depending on situation and surroundings. So in some areas, 68% might be
     * the maximum altitude. This method will do the best it can to switch the
     * camera altitude to what you want, but if it hits the maximum or stops
     * moving for any reason, it will return.
     * <p/>
     * Mess around a little to find the altitude percentage you like. In later
     * versions, there will be easier-to-work-with methods regarding altitude.
     *
     * @param altPercent The percentage of the maximum pitch to set the camera to.
     * @return <tt>true</tt> if the camera was successfully moved; otherwise <tt>false</tt>.
     */
    public boolean setPitch(int altPercent) {
        int alt = (int) (altPercent * 20.48 + 1024);
        int curAlt = methods.client.getCameraPitch();
        int lastAlt = 0;
        if (curAlt == alt)
            return true;
        else if (curAlt > alt) {
            methods.inputManager.pressKey((char) KeyEvent.VK_UP);
            long start = System.currentTimeMillis();
            while (curAlt > alt && System.currentTimeMillis() - start < 30) {
                if (lastAlt != curAlt) {
                    start = System.currentTimeMillis();
                }
                lastAlt = curAlt;

                sleep(1);
                curAlt = methods.client.getCameraPitch();
            }
            methods.inputManager.releaseKey((char) KeyEvent.VK_UP);
            return true;
        } else {
            methods.inputManager.pressKey((char) KeyEvent.VK_DOWN);
            long start = System.currentTimeMillis();
            while (curAlt < alt && System.currentTimeMillis() - start < 30) {
                if (lastAlt != curAlt) {
                    start = System.currentTimeMillis();
                }
                lastAlt = curAlt;
                sleep(1);
                curAlt = methods.client.getCameraPitch();
            }
            methods.inputManager.releaseKey((char) KeyEvent.VK_DOWN);
            return true;
        }
    }


    /**
     * Moves the camera in a random direction for a given time.
     * 
     * @param timeOut The maximum time in milliseconds to move
     * the camera for.
     */
    public void moveRandomly(int timeOut) {
        Timer timeToHold = new Timer(timeOut);
        int lowestCamAltPossible = random(75, 100);
        int vertical = random(0, 20) < 15 ? KeyEvent.VK_UP : KeyEvent.VK_DOWN;
        int horizontal = random(0, 20) < 5 ? KeyEvent.VK_LEFT : KeyEvent.VK_RIGHT;
        if (random(0, 10) < 8)
            methods.inputManager.pressKey((char) vertical);
        if (random(0, 10) < 8)
            methods.inputManager.pressKey((char) horizontal);
        while (timeToHold.isNotUp() && methods.client.getCamPosZ() >= lowestCamAltPossible) {
            sleep(10);
        }
        methods.inputManager.releaseKey((char) vertical);
        methods.inputManager.releaseKey((char) horizontal);
    }

    /**
     * Rotates the camera to a specific angle in the closest direction.
     *
     * @param degrees The angle to rotate to.
     */
    public void setAngle(int degrees) {
        char left = 37;
        char right = 39;
        char whichDir = left;
        int start = getAngle();
        if (start < 180) {
            start += 360;
        }
        if (degrees < 180) {
            degrees += 360;
        }
        if (degrees > start) {
            if (degrees - 180 < start) {
                whichDir = right;
            }
        } else if (start > degrees) {
            if (start - 180 >= degrees) {
                whichDir = right;
            }
        }
        degrees %= 360;
        methods.inputManager.pressKey(whichDir);
        int timeWaited = 0;
        while (getAngle() > degrees + 5 || getAngle() < degrees - 5) {
        	sleep(10);
            timeWaited += 10;
            if (timeWaited > 500) {
                int time = timeWaited - 500;
                if (time == 0) {
                    methods.inputManager.pressKey(whichDir);
                } else if (time % 40 == 0) {
                    methods.inputManager.pressKey(whichDir);
                }
            }
        }
        methods.inputManager.releaseKey(whichDir);
    }

    /**
     * Rotates the camera to the specified cardinal direction.
     * 
     * @param direction The <tt>char</tt> direction to turn the map. <tt>char</tt> options are
     * w,s,e,n and defaults to north if character is unrecognized.
     */
    public void setCompass(char direction) {
        switch (direction) {
            case 'n':
            	setAngle(359);
                break;
            case 'w':
                setAngle(89);
                break;
            case 's':
                setAngle(179);
                break;
            case 'e':
                setAngle(269);
                break;
            default:
                setAngle(359);
                break;
        }
    }
    
    /**
     * Uses the compass component to set the camera to face north.
     */
    public void setNorth() {
    	methods.interfaces.getComponent(methods.gui.getCompass().getID()).doClick();
    }

    /**
     * Returns the angle to a given RSCharacter (RSNPC or RSPlayer).
     *
     * @param n the RSCharacter
     * @return The angle
     */
    public int getAngleToCharacter(RSCharacter n) {
        return getAngleToTile(n.getLocation());
    }

    /**
     * Returns the angle to a given coordinate pair.
     *
     * @param x2 x coordinate
     * @param y2 y coordinate
     * @return The angle
     */
    public int getAngleToCoordinates(int x2, int y2) {
        int x1 = methods.players.getMyPlayer().getLocation().getX();
        int y1 = methods.players.getMyPlayer().getLocation().getY();
        int x = x1 - x2;
        int y = y1 - y2;
        double angle = Math.toDegrees(Math.atan2(y, x));
        if (x == 0 && y > 0) {
            angle = 180;
        }
        if (x < 0 && y == 0) {
            angle = 90;
        }
        if (x == 0 && y < 0) {
            angle = 0;
        }
        if (x < 0 && y == 0) {
            angle = 270;
        }
        if (x < 0 && y > 0) {
            angle += 270;
        }
        if (x > 0 && y > 0) {
            angle += 90;
        }
        if (x < 0 && y < 0) {
            angle = Math.abs(angle) - 180;
        }
        if (x > 0 && y < 0) {
            angle = Math.abs(angle) + 270;
        }
        if (angle < 0) {
            angle = 360 + angle;
        }
        if (angle >= 360) {
            angle -= 360;
        }
        return (int) angle;
    }

    /**
     * Returns the angle to a given object
     *
     * @param o The RSObject
     * @return The angle
     */
    public int getAngleToObject(RSObject o) {
        return getAngleToTile(o.getLocation());
    }

    /**
     * Returns the angle to a given tile
     *
     * @param t The RSTile
     * @return The angle
     */
    public int getAngleToTile(RSTile t) {
        return getAngleToCoordinates(t.getX(), t.getY());
    }

    /**
     * Returns the current compass orientation in degrees,
     * with North at 0, increasing counter-clockwise to 360.
     * 
     * @return The current camera angle in degrees.
     */
    public int getAngle() {
        return methods.client.getCameraYaw() / 46;
    }
    
    /**
     * Returns the current percentage of the maximum pitch
     * of the camera in an open area.
     * 
     * @return The current camera altitude percentage.
     */
    public int getPitch() {
    	return (int) ((methods.client.getCameraPitch() - 1024) / 20.48);
    }
    
    /**
     * Returns the current x position of the camera.
     * 
     * @return The x position.
     */
    public int getX() {
    	return methods.client.getCamPosX();
    }
    
    /**
     * Returns the current y position of the camera.
     * 
     * @return The y position.
     */
    public int getY() {
    	return methods.client.getCamPosY();
    }
    
    /**
     * Returns the current z position of the camera.
     * 
     * @return The z position.
     */
    public int getZ() {
    	return methods.client.getCamPosZ();
    }
    
}
