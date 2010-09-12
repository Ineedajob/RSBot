package org.rsbot.script.methods;

import java.util.LinkedHashSet;
import java.util.Set;

import org.rsbot.client.RSAnimableNode;
import org.rsbot.script.wrappers.RSObject;
import org.rsbot.script.wrappers.RSObjectDef;
import org.rsbot.script.wrappers.RSTile;

/**
 * Provides access to in-game physical objects.
 */
public class Objects extends MethodProvider {
	
	public static final int TYPE_INTERACTABLE = 1;
	public static final int TYPE_FLOOR_DECORATION = 2;
	public static final int TYPE_BOUNDARY = 4;
	public static final int TYPE_WALL_DECORATION = 8;

    Objects(final MethodContext ctx) {
        super(ctx);
    }
    
    /**
     * Returns all the <tt>RSObject</tt>s in the local region.
     * 
     * @return An <tt>RSObject[]</tt> of all objects in the loaded region.
     */
    public RSObject[] getAll() {
    	Set<RSObject> objects = new LinkedHashSet<RSObject>();
    	for (int x = 0; x < 104; x++) {
            for (int y = 0; y < 104; y++) {
            	objects.addAll(getAtLocal(x, y, -1));
            }
    	}
    	return objects.toArray(new RSObject[objects.size()]);
    }

    /**
     * Returns the <tt>RSObject</tt> that is nearest, out of all of the
     * RSObjects with the provided ID(s). Can return null.
     *
     * @param ids The ID(s) of the RSObject that you are searching.
     * @return An <tt>RSObject</tt> representing the nearest object with one of the
     *         provided IDs; or null if there are no
     *         matching objects in the current region.
     */
    public RSObject getNearest(int... ids) {
        RSObject cur = null;
        double dist = -1;
        for (int x = 0; x < 104; x++) {
            for (int y = 0; y < 104; y++) {
                Set<RSObject> objs = getAtLocal(x, y, -1);
                for (RSObject o : objs) {
                    boolean isObject = false;
                    for (int id : ids) {
                        if (o.getID() == id) {
                            isObject = true;
                            break;
                        }
                    }
                    if (isObject) {
                        double distTmp = methods.calc.distanceBetween(methods.players.getMyPlayer().getLocation(), o.getLocation());
                        if (cur == null) {
                            dist = distTmp;
                            cur = o;
                        } else if (distTmp < dist) {
                            cur = o;
                            dist = distTmp;
                        }
                        break;
                    }
                }
            }
        }
        return cur;
    }

    /**
     * Returns the <tt>RSObject</tt> that is nearest, out of all of the
     * RSObjects with the provided name(s). Can return null.
     *
     * @param names The name(s) of the RSObject that you are searching.
     * @return An <tt>RSObject</tt> representing the nearest object with one of the
     *         provided names; or null if there are no
     *         matching objects in the current region.
     */
    public RSObject getNearest(String... names) {
        RSObject cur = null;
        double dist = -1;
        for (int x = 0; x < 104; x++) {
            for (int y = 0; y < 104; y++) {
                Set<RSObject> objs = getAtLocal(x, y, -1);
                for (RSObject o : objs) {
                    boolean isObject = false;
                    for (String name : names) {
                    	RSObjectDef def = o.getDef();
                        if (def != null && def.getName().toLowerCase().contains(name.toLowerCase())) {
                            isObject = true;
                            break;
                        }
                    }
                    if (isObject) {
                        double distTmp = methods.calc.distanceBetween(methods.players.getMyPlayer().getLocation(), o.getLocation());
                        if (cur == null) {
                            dist = distTmp;
                            cur = o;
                        } else if (distTmp < dist) {
                            cur = o;
                            dist = distTmp;
                        }
                        break;
                    }
                }
            }
        }
        return cur;
    }
    
    /**
     * Returns the top <tt>RSObject</tt> on the specified tile.
     * 
     * @param t The tile on which to search.
     * @return The top RSObject on the provided tile; or null if none found.
     */
    public RSObject getTopAt(RSTile t) {
    	return getTopAt(t, -1);
    }
    
    /**
     * Returns the top <tt>RSObject</tt> on the specified tile
     * matching types specified by the flags in the provided mask.
     * 
     * @param t The tile on which to search.
     * @param mask The type flags.
     * @return The top RSObject on the provided tile matching the
     * specified flags; or null if none found.
     */
    public RSObject getTopAt(RSTile t, int mask) {
    	RSObject[] objects = getAt(t, mask);
    	return objects.length > 0 ? objects[0] : null;
    }
    
    /**
     * Returns the <tt>RSObject</tt>s which are on the specified <tt>RSTile</tt>
     * matching types specified by the flags in the provided mask.
     *
     * @param t The tile on which to search.
     * @param mask The type flags.
     * @return An RSObject[] of the objects on the specified tile.
     */
    public RSObject[] getAt(RSTile t, int mask) {
    	Set<RSObject> objects = getAtLocal(t.getX() - methods.client.getBaseX(), t.getY() - methods.client.getBaseY(), mask);
        return objects.toArray(new RSObject[objects.size()]);
    }
    
    /**
     * Returns the <tt>RSObject</tt>s which are on the specified <tt>RSTile</tt>.
     *
     * @param t The tile on which to search.
     * @return An RSObject[] of the objects on the specified tile.
     */
    public RSObject[] getAllAt(RSTile t) {
    	Set<RSObject> objects = getAtLocal(t.getX() - methods.client.getBaseX(), t.getY() - methods.client.getBaseY(), -1);
        return objects.toArray(new RSObject[objects.size()]);
    }

    private Set<RSObject> getAtLocal(int x, int y, int mask) {
    	org.rsbot.client.Client client = methods.client;
    	Set<RSObject> objects = new LinkedHashSet<RSObject>();
        if (client.getRSGroundArray() == null) {
            return objects;
        }

        try {
        	int plane = client.getPlane();
            org.rsbot.client.RSGround rsGround = client.getRSGroundArray()[plane][x][y];

            if (rsGround != null) {
                org.rsbot.client.RSObject rsObj;
                org.rsbot.client.RSInteractable obj;
                
                x += methods.client.getBaseX();
                y += methods.client.getBaseY();

                // Interactable (e.g. Trees)
                if ((mask & TYPE_INTERACTABLE) != 0) {
                	for (RSAnimableNode node = rsGround.getRSAnimableList(); node != null; node = node.getNext()) {
                        obj = node.getRSAnimable();
                        if (obj != null && obj instanceof org.rsbot.client.RSObject) {
                            rsObj = (org.rsbot.client.RSObject) obj;
                            if (rsObj.getID() != -1) {
                                objects.add(new RSObject(methods, rsObj, RSObject.Type.INTERACTABLE, plane));
                            }
                        }
                    }
                }

                // Ground Decorations
                if ((mask & TYPE_FLOOR_DECORATION) != 0) {
                	obj = rsGround.getRSObject1();
                    if (obj != null) {
                        rsObj = (org.rsbot.client.RSObject) obj;
                        if (rsObj.getID() != -1) {
                        	objects.add(new RSObject(methods, rsObj, RSObject.Type.FLOOR_DECORATION, plane));
                        }
                    }
                }
                

                // Boundaries / Doors / Fences / Walls
                if ((mask & TYPE_BOUNDARY) != 0) {
                    obj = rsGround.getRSObject2_0();
                    if (obj != null) {
                        rsObj = (org.rsbot.client.RSObject) obj;
                        if (rsObj.getID() != -1)
                        	objects.add(new RSObject(methods, rsObj, RSObject.Type.BOUNDARY, plane));
                    }

                    obj = rsGround.getRSObject2_1();
                    if (obj != null) {
                        rsObj = (org.rsbot.client.RSObject) obj;
                        if (rsObj.getID() != -1)
                        	objects.add(new RSObject(methods, rsObj, RSObject.Type.BOUNDARY, plane));
                    }
                }

                // Wall Decorations
                if ((mask & TYPE_WALL_DECORATION) != 0) {
                    obj = rsGround.getRSObject3_0();
                    if (obj != null) {
                        rsObj = (org.rsbot.client.RSObject) obj;
                        if (rsObj.getID() != -1)
                        	objects.add(new RSObject(methods, rsObj, RSObject.Type.WALL_DECORATION, plane));
                    }

                    obj = rsGround.getRSObject3_1();
                    if (obj != null) {
                        rsObj = (org.rsbot.client.RSObject) obj;
                        if (rsObj.getID() != -1)
                        	objects.add(new RSObject(methods, rsObj, RSObject.Type.WALL_DECORATION, plane));
                    }
                }
            }
        } catch (Exception ignored) {
        }
        return objects;
    }

}
