package org.rsbot.script.internal.wrappers;

import org.rsbot.client.NodeSubQueue;

@SuppressWarnings("unchecked")
public class Queue<N extends org.rsbot.client.NodeSub> {

	private final NodeSubQueue nl;
	private org.rsbot.client.NodeSub current;

	public Queue(NodeSubQueue nl) {
		this.nl = nl;
	}

	public int size() {
		int size = 0;
		org.rsbot.client.NodeSub node = nl.getTail().getPrevSub();

		while (node != nl.getTail()) {
			node = node.getPrevSub();
			size++;
		}

		return size;
	}

	public N getHead() {
		org.rsbot.client.NodeSub node = nl.getTail().getNextSub();

		if (node == nl.getTail()) {
			current = null;
			return null;
		}
		current = node.getNextSub();

		return (N) node;
	}

	public N getNext() {
		org.rsbot.client.NodeSub node = current;

		if (node == nl.getTail()) {
			current = null;
			return null;
		}
		current = node.getNextSub();

		return (N) node;
	}

}
