package org.rsbot.script.internal;

public class StatusNodeList {

	private final org.rsbot.accessors.StatusNodeList nl;
	private org.rsbot.accessors.StatusNode c_node;

	public StatusNodeList(final org.rsbot.accessors.StatusNodeList nl) {
		this.nl = nl;
	}

	public org.rsbot.accessors.StatusNode getFirst() {
		final org.rsbot.accessors.StatusNode node = nl.getHead().getNext();

		if (node == nl.getHead()) {
			c_node = null;
			return null;
		}
		c_node = node.getNext();

		return node;
	}

	public org.rsbot.accessors.StatusNode getLast() {
		final org.rsbot.accessors.StatusNode node = nl.getHead().getPrevious();

		if (node == nl.getHead()) {
			c_node = null;
			return null;
		}
		c_node = node.getPrevious();

		return node;
	}

	public int getListSize() {
		int size = 0;
		org.rsbot.accessors.StatusNode node = nl.getHead().getPrevious();

		while (node != nl.getHead()) {
			node = node.getPrevious();
			size++;
		}

		return size;
	}

	public org.rsbot.accessors.StatusNode getNext() {
		final org.rsbot.accessors.StatusNode node = c_node;

		if (node == nl.getHead() || node == null) {
			c_node = null;
			return null;
		}
		c_node = node.getNext();

		return node;
	}

	public org.rsbot.accessors.StatusNode getPrevious() {
		final org.rsbot.accessors.StatusNode node = c_node;

		if (node == nl.getHead() || node == null) {
			c_node = null;
			return null;
		}
		c_node = node.getNext();

		return node;
	}

}
