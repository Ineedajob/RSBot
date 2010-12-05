package org.rsbot.script.internal.wrappers;

import org.rsbot.client.NodeSubQueue;

public class Queue {

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

	public org.rsbot.client.NodeSub getHead() {
		org.rsbot.client.NodeSub node = nl.getTail().getNextSub();

		if (node == nl.getTail()) {
			current = null;
			return null;
		}
		current = node.getNextSub();

		return node;
	}

	public org.rsbot.client.NodeSub getTail() {
		org.rsbot.client.NodeSub node = nl.getTail().getPrevSub();

		if (node == nl.getTail()) {
			current = null;
			return null;
		}
		current = node.getPrevSub();

		return node;
	}

	public org.rsbot.client.NodeSub getNext() {
		org.rsbot.client.NodeSub node = current;

		if (node == nl.getTail()) {
			current = null;
			return null;
		}
		current = node.getNextSub();

		return node;
	}

}
