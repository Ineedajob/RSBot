package org.rsbot.script.internal;

public class NodeList {

	private final org.rsbot.accessors.NodeList nl;
	private org.rsbot.accessors.Node c_node;

	public NodeList(final org.rsbot.accessors.NodeList nl) {
		this.nl = nl;
	}

	public org.rsbot.accessors.Node getFirst() {
		final org.rsbot.accessors.Node node = nl.getHead().getNext();

		if (node == nl.getHead()) {
			c_node = null;
			return null;
		}
		c_node = node.getNext();

		return node;
	}

	public org.rsbot.accessors.Node getLast() {
		final org.rsbot.accessors.Node node = nl.getHead().getPrevious();

		if (node == nl.getHead()) {
			c_node = null;
			return null;
		}
		c_node = node.getPrevious();

		return node;
	}

	public int getListSize() {
		int size = 0;
		org.rsbot.accessors.Node node = nl.getHead().getPrevious();

		while (node != nl.getHead()) {
			node = node.getPrevious();
			size++;
		}

		return size;
	}

	public org.rsbot.accessors.Node getNext() {
		final org.rsbot.accessors.Node node = c_node;

		if (node == nl.getHead()) {
			c_node = null;
			return null;
		}
		c_node = node.getNext();

		return node;
	}

	public org.rsbot.accessors.Node getPrevious() {
		final org.rsbot.accessors.Node node = c_node;

		if (node == nl.getHead()) {
			c_node = null;
			return null;
		}
		c_node = node.getNext();

		return node;
	}

}
