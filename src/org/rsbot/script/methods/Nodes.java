package org.rsbot.script.methods;

import org.rsbot.accessors.DefFactory;
import org.rsbot.accessors.Node;

/**
 * For internal use to reference data stored in the engine's
 * Node-based structures.
 */
public class Nodes extends MethodProvider {

	Nodes(MethodContext ctx) {
		super(ctx);
	}

	/**
	 * 
	 * @param nc The node cache to check
	 * @param id The id of the node
	 * @return A <tt>Node</tt> object corresponding to the ID in the nodecache.
	 */
	public Node lookup(final org.rsbot.accessors.NodeCache nc, final long id) {
		try {
			if ((nc == null) || (nc.getCache() == null) || (id < 0))
				return null;

			final Node n = nc.getCache()[(int) (id & nc.getCache().length - 1)];
			for (Node node = n.getPrevious(); node != n; node = node
					.getPrevious()) {
				if (node.getID() == id)
					return node;
			}
		} catch (final Exception ignored) {
		}
		return null;
	}

	/**
	 * 
	 * @param id The id of the node
	 * @return A <tt>Node</tt> object corresponding to the ID in the factory.
	 */
	public Node lookup(final DefFactory factory, final long id) {
		if ((factory == null) || (factory.getMRUNodes() == null)) {
			return null;
		}
		return lookup(factory.getMRUNodes().getNodeCache(), id);
	}

}
