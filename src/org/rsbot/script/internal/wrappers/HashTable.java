package org.rsbot.script.internal.wrappers;

public class HashTable {

	private org.rsbot.client.NodeCache nc;
	private org.rsbot.client.Node current;
	private int c_index = 0;

	public HashTable(org.rsbot.client.NodeCache nodeCache) {
		nc = nodeCache;
	}

	public org.rsbot.client.Node getFirst() {
		c_index = 0;
		return getNext();
	}

	public org.rsbot.client.Node getNext() {
		if (c_index > 0 && nc.getCache()[c_index - 1] != current) {
			org.rsbot.client.Node node = current;
			current = node.getPrevious();
			return node;
		}
		while (c_index < nc.getCache().length) {
			org.rsbot.client.Node node = nc.getCache()[c_index++].getPrevious();
			if (nc.getCache()[c_index - 1] != node) {
				current = node.getPrevious();
				return node;
			}
		}
		return null;
	}
}
