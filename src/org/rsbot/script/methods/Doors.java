package org.rsbot.script.methods;

import java.util.ArrayList;
import java.util.List;

import org.rsbot.script.util.Filter;
import org.rsbot.script.wrappers.RSDoor;
import org.rsbot.script.wrappers.RSObject;
import org.rsbot.script.wrappers.RSObjectDef;
import org.rsbot.script.wrappers.RSTile;

/**
 * Manages the doors and all of it's functions.
 * 
 * @author Boolean
 */
public class Doors extends MethodProvider {

	Doors(final MethodContext ctx) {
		super(ctx);
	}

	private final static String[] DOOR_NAMES = { "door", "gate" };
	/**
	 * Filter for all doors
	 */
	public final static Filter<RSObject> DOOR_FILTER = new Filter<RSObject>() {
		@Override
		public boolean accept(RSObject o) {
			RSObjectDef def = o.getDef();
			if (def != null && def.getName() != null) {
				for (String s : DOOR_NAMES)
					if (s.trim().equalsIgnoreCase(def.getName().trim()))
						return true;
			}
			return false;
		}
	};

	/**
	 * Checks if the provided object is a door
	 * 
	 * @param object
	 * @return Is the object a door
	 */
	public boolean isDoor(RSObject object) {
		return DOOR_FILTER.accept(object);
	}

	/**
	 * Checks if a specified door is opened.
	 * 
	 * @param door
	 *            The door object.
	 * @return True if it's open, otherwise false.
	 */
	public boolean isOpen(RSDoor door) {
		return door.isOpen();
	}

	/**
	 * Checks if a specified door is closed.
	 * 
	 * @param door
	 *            The door object.
	 * @return True if it's closed, otherwise false.
	 */
	public boolean isClosed(RSDoor door) {
		return !door.isOpen();
	}

	/**
	 * Opens a door.
	 * 
	 * @param doors
	 *            The RSDoor to open.
	 * @return
	 */
	public boolean open(RSDoor door) {
		return door.open();
	}

	/**
	 * Closes a door.
	 * 
	 * @param doors
	 *            The RSDoor to close.
	 * @return
	 */
	public boolean close(RSDoor door) {
		return door.close();
	}

	/**
	 * Gets all loaded doors
	 * 
	 * @return All loaded RSDoors
	 */
	public RSDoor[] getAll() {
		List<RSDoor> doors = new ArrayList<RSDoor>();
		for (RSObject o : methods.objects.getAll(DOOR_FILTER))
			doors.add(new RSDoor(methods, o.getLocation()));
		return doors.toArray(new RSDoor[doors.size()]);
	}

	/**
	 * Gets the door at the specified tile or null if there is no door
	 * 
	 * @param tile
	 * @return The door at tile or null
	 */
	public RSDoor getDoorAt(RSTile tile) {
		RSDoor door = new RSDoor(methods, tile);
		return door.isDoor() ? door : null;
	}

	/**
	 * Checks if there is a door at the specified tile
	 * 
	 * @param tile
	 * @return Is there a door at the tile
	 */
	public boolean isDoorAt(RSTile tile) {
		return getDoorAt(tile) != null;
	}

	/**
	 * Checks if there is a door at the specified tile and its open state is
	 * equal to isOpenDoor
	 * 
	 * @param tile
	 * @param isOpenDoor
	 *            Should the door be open
	 * @return Is the door there and is it's state correct
	 */
	public boolean isDoorAt(RSTile tile, boolean isOpenDoor) {
		RSDoor door = getDoorAt(tile);
		return door != null && door.isOpen() == isOpenDoor;
	}

	/**
	 * Checks if there is a closed door at the specified tile.
	 * 
	 * @param tile
	 * @return Is there a closed door there
	 */
	public boolean isClosedDoorAt(RSTile tile) {
		return isDoorAt(tile, false);
	}

	/**
	 * Checks if there is a open door at the specified tile.
	 * 
	 * @param tile
	 * @return Is there a open door there
	 */
	public boolean isOpenDoorAt(RSTile tile) {
		return isDoorAt(tile, true);
	}

	/**
	 * Gets the RSDoor nearest to you
	 * 
	 * @return Nearest RSDoor
	 */
	public RSDoor getNearest() {
		RSDoor cur = null;
		double dist = -1;
		RSDoor[] doors = getAll();
		for (RSDoor d : doors) {
			double distTmp = methods.calc.distanceBetween(methods.players
					.getMyPlayer().getLocation(), d.getLocation());
			if (cur == null) {
				dist = distTmp;
				cur = d;
			} else if (distTmp < dist) {
				cur = d;
				dist = distTmp;
			}
			if (distTmp == 0 && cur != null)
				break;
		}
		return cur;
	}
}
