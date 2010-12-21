package org.rsbot.script.methods;

import org.rsbot.client.Node;
import org.rsbot.client.RSNPCNode;
import org.rsbot.script.util.Filter;
import org.rsbot.script.wrappers.RSNPC;

import java.util.HashSet;
import java.util.Set;

/**
 * Provides access to non-player characters.
 */
public class NPCs extends MethodProvider {

	/**
	 * A filter that accepts all matches.
	 */
	public static final Filter<RSNPC> ALL_FILTER = new Filter<RSNPC>() {
		public boolean accept(RSNPC npc) {
			return true;
		}
	};

	NPCs(final MethodContext ctx) {
		super(ctx);
	}

	/**
	 * Returns an array of all loaded RSNPCs.
	 *
	 * @return An array of the loaded RSNPCs.
	 */
	public RSNPC[] getAll() {
		return getAll(NPCs.ALL_FILTER);
	}

	/**
	 * Returns an array of all loaded RSNPCs that are accepted
	 * by the provided Filter
	 *
	 * @param filter Filters out unwanted matches.
	 * @return An array of the loaded RSNPCs.
	 */
	public RSNPC[] getAll(Filter<RSNPC> filter) {
		int[] indices = methods.client.getRSNPCIndexArray();
		Set<RSNPC> npcs = new HashSet<RSNPC>();
		for (int index : indices) {
			Node node = methods.nodes.lookup(methods.client.getRSNPCNC(), index);
			if (node instanceof RSNPCNode) {
				RSNPC npc = new RSNPC(methods, ((RSNPCNode) node).getRSNPC());
				if (filter.accept(npc)) {
					npcs.add(npc);
				}
			}
		}
		return npcs.toArray(new RSNPC[npcs.size()]);
	}

	/**
	 * Returns the RSNPC that is nearest out of all of loaded RSPNCs accepted
	 * by the provided Filter.
	 *
	 * @param filter Filters out unwanted matches.
	 * @return An RSNPC object representing the nearest RSNPC accepted by the
	 *         provided Filter; or null if there are no matching NPCs in the current region.
	 */
	public RSNPC getNearest(Filter<RSNPC> filter) {
		int min = 20;
		RSNPC closest = null;
		int[] indices = methods.client.getRSNPCIndexArray();

		for (int index : indices) {
			Node node = methods.nodes.lookup(methods.client.getRSNPCNC(), index);
			if (node instanceof RSNPCNode) {
				RSNPC npc = new RSNPC(methods, ((RSNPCNode) node).getRSNPC());
				if (filter.accept(npc)) {
					int distance = methods.calc.distanceTo(npc);
					if (distance < min) {
						min = distance;
						closest = npc;
					}
				}
			}
		}
		return closest;
	}

	/**
	 * Returns the RSNPC that is nearest out of all of the RSPNCs with the
	 * provided ID(s). Can return null.
	 *
	 * @param ids Allowed NPC IDs.
	 * @return An RSNPC object representing the nearest RSNPC with one of the
	 *         provided IDs; or null if there are no matching NPCs in the current region.
	 */
	public RSNPC getNearest(final int... ids) {
		return getNearest(new Filter<RSNPC>() {
			public boolean accept(RSNPC npc) {
				for (int id : ids) {
					if (npc.getID() == id) {
						return true;
					}
				}
				return false;
			}
		});
	}

	/**
	 * Returns the RSNPC that is nearest out of all of the RSPNCs with the
	 * provided name(s). Can return null.
	 *
	 * @param names Allowed NPC names.
	 * @return An RSNPC object representing the nearest RSNPC with one of the
	 *         provided names; or null if there are no matching NPCs in the current region.
	 */
	public RSNPC getNearest(final String... names) {
		return getNearest(new Filter<RSNPC>() {
			public boolean accept(RSNPC npc) {
				for (String name : names) {
					if (npc.getName().equals(name)) {
						return true;
					}
				}
				return false;
			}
		});
	}

}
