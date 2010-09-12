package org.rsbot.script.methods;

import org.rsbot.client.Node;
import org.rsbot.client.RSNPCNode;
import org.rsbot.script.wrappers.RSNPC;

import java.util.ArrayList;

/**
 * Provides access to non-player characters.
 */
public class NPCs extends MethodProvider {

    NPCs(final MethodContext ctx) {
        super(ctx);
    }

    /**
     * Returns the RSNPC that is nearest, out of all of the RSPNCs with the
     * provided ID(s), that is not currently in combat. Can return null.
     *
     * @param ids The ID(s) of the NPCs that you are searching.
     * @return An RSNPC object representing the nearest RSNPC with one of the
     *         provided IDs that is not in combat; or null if there are no
     *         matching NPCs in the current region.
     * @see #getNearest(int...)
     * @see #getNearestToAttack(int...)
     * @see #getNearestFreeToAttack(int...)
     */
    public RSNPC getNearestFree(int... ids) {
        int Dist = 20;
        RSNPC closest = null;
        int[] validNPCs = methods.client.getRSNPCIndexArray();

        for (int element : validNPCs) {
            Node node = methods.nodes.lookup(methods.client.getRSNPCNC(), element);
            if (node == null || !(node instanceof RSNPCNode)) {
                continue;
            }
            RSNPC Monster = new RSNPC(methods, ((RSNPCNode) node).getRSNPC());
            try {
                for (int id : ids) {
                    if (id != Monster.getID() || Monster.isInCombat()) {
                        continue;
                    }
                    int distance = methods.calc.distanceTo(Monster);
                    if (distance < Dist) {
                        Dist = distance;
                        closest = Monster;
                    }
                }
            } catch (Exception ignored) {
            }
        }
        return closest;
    }

    /**
     * Returns the RSNPC that is nearest, out of all of the RSPNCs with the
     * provided name(s), that is not currently in combat. Can return null.
     *
     * @param names The name(s) of the NPCs that you are searching.
     * @return An RSNPC object representing the nearest RSNPC with one of the
     *         provided names that is not in combat; or null if there are no
     *         mathching NPCs in the current region.
     * @see #getNearest(int...)
     * @see #getNearestFree(int...)
     * @see #getNearestToAttack(int...)
     * @see #getNearestFreeToAttack(int...)
     */
    public RSNPC getNearestFree(String... names) {
        int Dist = 20;
        RSNPC closest = null;
        int[] validNPCs = methods.client.getRSNPCIndexArray();

        for (int element : validNPCs) {
            Node node = methods.nodes.lookup(methods.client.getRSNPCNC(), element);
            if (node == null || !(node instanceof RSNPCNode)) {
                continue;
            }
            RSNPC Monster = new RSNPC(methods, ((RSNPCNode) node).getRSNPC());
            try {
                for (String name : names) {
                    if (name == null || !name.equals(Monster.getName()) || Monster.isInCombat()) {
                        continue;
                    }
                    int distance = methods.calc.distanceTo(Monster);
                    if (distance < Dist) {
                        Dist = distance;
                        closest = Monster;
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return closest;
    }

    /**
     * Returns the RSNPC that is nearest, out of all of the RSPNCs with the
     * provided ID(s), that is not currently in combat and does not have 0% HP.
     * Can return null.
     *
     * @param ids The ID(s) of the NPCs that you are searching.
     * @return An RSNPC object representing the nearest RSNPC with one of the
     *         provided IDs that is not in combat and does not have 0% HP (is
     *         attackable); or null if there are no mathching NPCs in the
     *         current region.
     * @see #getNearest(int...)
     * @see #getNearestFree(int...)
     * @see #getNearestToAttack(String...)
     */
    public RSNPC getNearestFreeToAttack(int... ids) {
        int Dist = 20;
        RSNPC closest = null;
        int[] validNPCs = methods.client.getRSNPCIndexArray();

        for (int element : validNPCs) {
            Node node = methods.nodes.lookup(methods.client.getRSNPCNC(), element);
            if (node == null || !(node instanceof RSNPCNode)) {
                continue;
            }
            RSNPC Monster = new RSNPC(methods, ((RSNPCNode) node).getRSNPC());
            try {
                for (int id : ids) {
                    if (id != Monster.getID() || Monster.isInCombat() || Monster.getHPPercent() == 0) {
                        continue;
                    }
                    int distance = methods.calc.distanceTo(Monster);
                    if (distance < Dist) {
                        Dist = distance;
                        closest = Monster;
                    }
                }
            } catch (Exception ignored) {
            }
        }
        return closest;
    }

    /**
     * Returns the RSNPC that is nearest, out of all of the RSPNCs with the
     * provided name(s), that is not currently in combat and does not have 0%
     * HP. Can return null.
     *
     * @param names The names(s) of the NPCs that you are searching.
     * @return An RSNPC object representing the nearest RSNPC with one of the
     *         provided names that is not in combat and does not have 0% HP (is
     *         attackable); or null if there are no mathching NPCs in the
     *         current region.
     * @see #getNearest(int...)
     * @see #getNearestFree(int...)
     * @see #getNearestToAttack(int...)
     * @see #getNearestFreeToAttack(int...)
     */
    public RSNPC getNearestFreeToAttack(String... names) {
        int Dist = 20;
        RSNPC closest = null;
        int[] validNPCs = methods.client.getRSNPCIndexArray();

        for (int element : validNPCs) {
            Node node = methods.nodes.lookup(methods.client.getRSNPCNC(), element);
            if (node == null || !(node instanceof RSNPCNode)) {
                continue;
            }
            RSNPC Monster = new RSNPC(methods, ((RSNPCNode) node).getRSNPC());
            try {
                for (String name : names) {
                    if (name == null || !name.equals(Monster.getName()) || Monster.isInCombat() || Monster.getHPPercent() == 0) {
                        continue;
                    }
                    int distance = methods.calc.distanceTo(Monster);
                    if (distance < Dist) {
                        Dist = distance;
                        closest = Monster;
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return closest;
    }

    /**
     * Returns the RSNPC that is nearest, out of all of the RSPNCs with the
     * provided ID(s). Can return null.
     *
     * @param ids The ID(s) of the NPCs that you are searching.
     * @return An RSNPC object representing the nearest RSNPC with one of the
     *         provided IDs; or null if there are no matching NPCs in the
     *         current region.
     * @see #getNearestFree(int...)
     * @see #getNearestToAttack(int...)
     * @see #getNearestFreeToAttack(int...)
     */
    public RSNPC getNearest(int... ids) {
        int Dist = 20;
        RSNPC closest = null;
        int[] validNPCs = methods.client.getRSNPCIndexArray();

        for (int element : validNPCs) {
            Node node = methods.nodes.lookup(methods.client.getRSNPCNC(), element);
            if (node == null || !(node instanceof RSNPCNode)) {
                continue;
            }
            RSNPC Monster = new RSNPC(methods, ((RSNPCNode) node).getRSNPC());
            try {
                for (int id : ids) {
                    if (id != Monster.getID()) {
                        continue;
                    }
                    int distance = methods.calc.distanceTo(Monster);
                    if (distance < Dist) {
                        Dist = distance;
                        closest = Monster;
                    }
                }
            } catch (Exception ignored) {
            }
        }
        return closest;
    }

    /**
     * Returns the RSNPC that is nearest, out of all of the RSPNCs with the
     * provided name(s). Can return null.
     *
     * @param names The name(s) of the NPCs that you are searching.
     * @return An RSNPC object representing the nearest RSNPC with one of the
     *         provided names; or null if there are no mathching NPCs in the
     *         current region.
     * @see #getNearest(int...)
     * @see #getNearestFree(int...)
     * @see #getNearestToAttack(int...)
     * @see #getNearestFreeToAttack(int...)
     */
    public RSNPC getNearest(String... names) {
        int Dist = 20;
        RSNPC closest = null;
        int[] validNPCs = methods.client.getRSNPCIndexArray();

        for (int element : validNPCs) {
            Node node = methods.nodes.lookup(methods.client.getRSNPCNC(), element);
            if (node == null || !(node instanceof RSNPCNode)) {
                continue;
            }
            RSNPC Monster = new RSNPC(methods, ((RSNPCNode) node).getRSNPC());
            try {
                for (String name : names) {
                    if (name == null || !name.equals(Monster.getName())) {
                        continue;
                    }
                    int distance = methods.calc.distanceTo(Monster);
                    if (distance < Dist) {
                        Dist = distance;
                        closest = Monster;
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return closest;
    }

    /**
     * Returns the RSNPC that is nearest, out of all of the RSPNCs with the
     * provided ID(s), that does not have 0% HP. Can return null.
     *
     * @param ids The ID(s) of the NPCs that you are searching.
     * @return An RSNPC object representing the nearest RSNPC with one of the
     *         provided IDs that does not have 0% HP (is attackable); or null if
     *         there are no matching NPCs in the current region.
     * @see #getNearest(int...)
     * @see #getNearestFree(int...)
     * @see #getNearestToAttack(int...)
     * @see #getNearestFreeToAttack(int...)
     */
    public RSNPC getNearestToAttack(int... ids) {
        int Dist = 20;
        RSNPC closest = null;
        int[] validNPCs = methods.client.getRSNPCIndexArray();

        for (int element : validNPCs) {
            Node node = methods.nodes.lookup(methods.client.getRSNPCNC(), element);
            if (node == null || !(node instanceof RSNPCNode)) {
                continue;
            }
            RSNPC Monster = new RSNPC(methods, ((RSNPCNode) node).getRSNPC());
            try {
                for (int id : ids) {
                    if (id != Monster.getID() || Monster.getHPPercent() == 0) {
                        continue;
                    }
                    int distance = methods.calc.distanceTo(Monster);
                    if (distance < Dist) {
                        Dist = distance;
                        closest = Monster;
                    }
                }
            } catch (Exception ignored) {
            }
        }
        return closest;
    }

    /**
     * Returns the RSNPC that is nearest, out of all of the RSPNCs with the
     * provided names(s), that does not have 0% HP (is attackable). Can return
     * null.
     *
     * @param names The name(s) of the NPCs that you are searching.
     * @return An RSNPC object representing the nearest RSNPC with one of the
     *         provided name(s) that does not have 0% HP (is attackable); or
     *         null if there are no mathching NPCs in the current region.
     * @see #getNearest(int...)
     * @see #getNearestFree(int...)
     * @see #getNearestToAttack(int...)
     * @see #getNearestFreeToAttack(int...)
     */
    public RSNPC getNearestToAttack(String... names) {
        int Dist = 20;
        RSNPC closest = null;
        int[] validNPCs = methods.client.getRSNPCIndexArray();

        for (int element : validNPCs) {
            Node node = methods.nodes.lookup(methods.client.getRSNPCNC(), element);
            if (node == null || !(node instanceof RSNPCNode)) {
                continue;
            }
            RSNPC Monster = new RSNPC(methods, ((RSNPCNode) node).getRSNPC());
            try {
                for (String name : names) {
                    if (name == null || !name.equals(Monster.getName()) || Monster.getHPPercent() == 0) {
                        continue;
                    }
                    int distance = methods.calc.distanceTo(Monster);
                    if (distance < Dist) {
                        Dist = distance;
                        closest = Monster;
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return closest;
    }
    
    /**
     * Returns an array of all loaded RSNPCs.
     * 
     * @return An array of the loaded RSNPCs.
     * @see #getAll(boolean)
     */
    public RSNPC[] getAll() {
    	return getAll(false);
    }

    /**
     * Returns an array of all loaded RSNPCs, with an option of whether
     * or not to exclude interacting RSNPCs.
     *
     * @param excludeInteracting a boolean on whether to exclude or include interacting RSNPCs.
     * @return An array of the loaded RSNPCs.
     */
    public RSNPC[] getAll(boolean excludeInteracting) {
        int[] validNPCs = methods.client.getRSNPCIndexArray();
        ArrayList<RSNPC> realNPCs = new ArrayList<RSNPC>();
        for (int element : validNPCs) {
            Node node = methods.nodes.lookup(methods.client.getRSNPCNC(), element);
            if (node == null || !(node instanceof RSNPCNode) || excludeInteracting && methods.players.getMyPlayer().getInteracting() != null &&
                    methods.players.getMyPlayer().getInteracting().equals(new RSNPC(methods, ((RSNPCNode) node).getRSNPC()))) {
                continue;
            }
            realNPCs.add(new RSNPC(methods, ((RSNPCNode) node).getRSNPC()));
        }
        RSNPC[] temp = new RSNPC[realNPCs.size()];
        for (int i = 0; i < temp.length; i++) {
            temp[i] = realNPCs.get(i);
        }
        return temp;
    }
   
}
