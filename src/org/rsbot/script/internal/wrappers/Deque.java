package org.rsbot.script.internal.wrappers;

public class Deque {

	private final org.rsbot.client.NodeList nl;
	private org.rsbot.client.Node current;

	public Deque(org.rsbot.client.NodeList nl) {
		this.nl = nl;
	}

	public int size() {
		int size = 0;
		org.rsbot.client.Node node = nl.getHead().getPrevious();

		while (node != nl.getHead()) {
			node = node.getPrevious();
			size++;
		}

		return size;
	}

	public org.rsbot.client.Node getHead() {
		org.rsbot.client.Node node = nl.getHead().getNext();

		if (node == nl.getHead()) {
			current = null;
			return null;
		}
		current = node.getNext();

		return node;
	}

	public org.rsbot.client.Node getTail() {
		org.rsbot.client.Node node = nl.getHead().getPrevious();

		if (node == nl.getHead()) {
			current = null;
			return null;
		}
		current = node.getPrevious();

		return node;
	}

	public org.rsbot.client.Node getNext() {
		org.rsbot.client.Node node = current;

		if (node == nl.getHead()) {
			current = null;
			return null;
		}
		current = node.getNext();

		return node;
	}

}
