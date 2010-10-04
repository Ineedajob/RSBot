package org.rsbot.script.methods;

import org.rsbot.client.Node;
import org.rsbot.client.RSNPCNode;
import org.rsbot.script.wrappers.RSNPC;

import java.util.HashSet;
import java.util.Set;

/**
 * Provides access to non-player characters.
 */
public class NPCs extends MethodProvider {

	/**
	 * Used to filter the NPCs found by methods in the NPCs class.
	 */
	public static interface Qualifier {
		public boolean qualifies(RSNPC npc);
	}

	/**
	 * A qualifier that accepts all matches.
	 */
	public static final Qualifier PREDICATE_QUALIFIER = new Qualifier() {
		public boolean qualifies(RSNPC npc) {
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
		return getAll(NPCs.PREDICATE_QUALIFIER);
	}

	/**
	 * Returns an array of all loaded RSNPCs that are qualified
	 * by the provided Qualifier.
	 *
	 * @param qualifier Filters unwanted matches.
	 * @return An array of the loaded RSNPCs.
	 */
	public RSNPC[] getAll(Qualifier qualifier) {
		int[] indices = methods.client.getRSNPCIndexArray();
		Set<RSNPC> npcs = new HashSet<RSNPC>();
		for (int index : indices) {
			Node node = methods.nodes.lookup(methods.client.getRSNPCNC(), index);
			if (node instanceof RSNPCNode) {
				RSNPC npc = new RSNPC(methods, ((RSNPCNode) node).getRSNPC());
				if (qualifier.qualifies(npc)) {
					npcs.add(npc);
				}
			}
		}
		return npcs.toArray(new RSNPC[npcs.size()]);
	}

	/**
	 * Returns the RSNPC that is nearest out of all of loaded RSPNCs qualified
	 * by the provided Qualifier. Can return null.
	 *
	 * @param qualifier Filters unwanted matches.
	 * @return An RSNPC object representing the nearest RSNPC qualified by the
	 * provided Qualifier; or null if there are no matching NPCs in the current region.
	 */
	public RSNPC getNearest(Qualifier qualifier) {
		int min = 20;
		RSNPC closest = null;
		int[] indices = methods.client.getRSNPCIndexArray();

		for (int index : indices) {
			Node node = methods.nodes.lookup(methods.client.getRSNPCNC(), index);
			if (node instanceof RSNPCNode) {
				RSNPC npc = new RSNPC(methods, ((RSNPCNode) node).getRSNPC());
				if (qualifier.qualifies(npc)) {
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
	 * provided name(s). Can return null.
	 *
	 * @param ids Allowed NPC IDs.
	 * @return An RSNPC object representing the nearest RSNPC with one of the
	 * provided IDs; or null if there are no matching NPCs in the current region.
	 */
	public RSNPC getNearest(final int... ids) {
		return getNearest(new Qualifier() {
			public boolean qualifies(RSNPC npc) {
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
	 * provided IDs; or null if there are no matching NPCs in the current region.
	 */
	public RSNPC getNearest(final String... names) {
		return getNearest(new Qualifier() {
			public boolean qualifies(RSNPC npc) {
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
