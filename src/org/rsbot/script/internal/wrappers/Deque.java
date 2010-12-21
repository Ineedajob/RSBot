package org.rsbot.script.internal.wrappers;

import org.rsbot.client.NodeDeque;

@SuppressWarnings("unchecked")
public class Deque<N> {

	private final NodeDeque nl;
	private org.rsbot.client.Node current;

	public Deque(NodeDeque nl) {
		this.nl = nl;
	}

	public int size() {
		int size = 0;
		org.rsbot.client.Node node = nl.getTail().getPrevious();

		while (node != nl.getTail()) {
			node = node.getPrevious();
			size++;
		}

		return size;
	}

	public N getHead() {
		org.rsbot.client.Node node = nl.getTail().getNext();

		if (node == nl.getTail()) {
			current = null;
			return null;
		}
		current = node.getNext();

		return (N) node;
	}

	public N getTail() {
		org.rsbot.client.Node node = nl.getTail().getPrevious();

		if (node == nl.getTail()) {
			current = null;
			return null;
		}
		current = node.getPrevious();

		return (N) node;
	}

	public N getNext() {
		org.rsbot.client.Node node = current;

		if (node == nl.getTail()) {
			current = null;
			return null;
		}
		current = node.getNext();

		return (N) node;
	}

}
