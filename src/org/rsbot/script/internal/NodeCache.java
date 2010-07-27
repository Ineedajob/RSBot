package org.rsbot.script.internal;

public class NodeCache {
	private final org.rsbot.accessors.NodeCache nc;
	private int c_index = 0;
	private org.rsbot.accessors.Node c_node;

	public NodeCache(final org.rsbot.accessors.NodeCache nodeCache) {
		nc = nodeCache;
	}

	public org.rsbot.accessors.Node getFirst() {
		c_index = 0;
		return getNext();
	}

	public org.rsbot.accessors.Node getNext() {
		if ((c_index > 0) && (nc.getCache()[c_index - 1] != c_node)) {
			final org.rsbot.accessors.Node node = c_node;
			c_node = node.getPrevious();
			return node;
		}

		while (nc.getCache().length > c_index) {
			final org.rsbot.accessors.Node node = nc.getCache()[c_index++].getPrevious();
			if (nc.getCache()[c_index - 1] != node) {
				c_node = node.getPrevious();
				return node;
			}
		}
		return null;
	}
}
