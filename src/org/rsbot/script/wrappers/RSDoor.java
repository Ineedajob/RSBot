package org.rsbot.script.wrappers;

import org.rsbot.script.methods.MethodContext;
import org.rsbot.script.methods.MethodProvider;
import org.rsbot.script.methods.Objects;

public class RSDoor extends MethodProvider {
	private final RSTile doorLocation;
	private int openedID = 0;
	private int closedID = 0;

	public RSDoor(MethodContext methods, RSTile doorLocation) {
		super(methods);
		this.doorLocation = doorLocation;
	}

	/**
	 * Checks if this door is actually has a door object
	 * 
	 * @return Is this door actually a door
	 */
	public boolean isDoor() {
		return getObject() != null;
	}

	/**
	 * Gets this door's RSTile
	 * 
	 * @return The location of the door
	 */
	public RSTile getLocation() {
		return doorLocation;
	}

	/**
	 * Gets this door's object
	 * 
	 * @return the door object
	 */
	public RSObject getObject() {
		RSObject[] objects = methods.objects.getAt(doorLocation,
				Objects.TYPE_INTERACTABLE | Objects.TYPE_BOUNDARY);
		for (RSObject o : objects)
			if (methods.doors.isDoor(o))
				return o;
		return null;
	}

	/**
	 * Checks if this door is open, comparing to the cached object ids if
	 * available
	 * 
	 * @return If the door is open
	 */
	public boolean isOpen() {
		RSObject obj = getObject();
		if (obj != null) {
			if (openedID > 0)
				if (obj.getID() == openedID)
					return true;
			if (closedID > 0)
				if (obj.getID() == closedID)
					return false;
			RSObjectDef def = obj.getDef();
			if (def != null && def.getActions() != null) {
				for (String s : def.getActions())
					if (s != null && s.contains("Open")) {
						closedID = obj.getID();
						return false;
					} else if (s != null && s.contains("Close")) {
						openedID = obj.getID();
						return true;
					}
			}
		}
		return false;
	}

	/**
	 * Checks if this door is closed
	 * 
	 * @return If the door is closed
	 */
	public boolean isClosed() {
		return !isOpen();
	}

	/**
	 * Open this door
	 * 
	 * @return If the door was opened
	 */
	public boolean open() {
		RSObject door = getObject();
		if (door != null) {
			if (door.doAction("Open"))
				for (byte b = 0; b < 50 && !isOpen(); b++)
					sleep(125);
			return isOpen();
		}
		return false;
	}
	
	/**
	 * Close this door
	 * 
	 * @return If the door was closed
	 */
	public boolean close() {
		RSObject door = getObject();
		if (door != null) {
			if (door.doAction("Close"))
				for (byte b = 0; b < 50 && isOpen(); b++)
					sleep(125);
			return !isOpen();
		}
		return false;
	}

	/**
	 * Determines if this door is on screen
	 * 
	 * @return true if this door is on screen
	 */
	public boolean isOnScreen() {
		RSObject door = getObject();
		return door != null && door.isOnScreen();
	}
}
