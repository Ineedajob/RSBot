package org.rsbot.script.internal.wrappers;

class StatusQueue {

	private final org.rsbot.client.StatusNodeList nl;
	private org.rsbot.client.StatusNode c_node;

	public StatusQueue(final org.rsbot.client.StatusNodeList nl) {
		this.nl = nl;
	}

	public org.rsbot.client.StatusNode getFirst() {
		final org.rsbot.client.StatusNode node = nl.getHead().getNext();

		if (node == nl.getHead()) {
			c_node = null;
			return null;
		}
		c_node = node.getNext();

		return node;
	}

	public org.rsbot.client.StatusNode getLast() {
		final org.rsbot.client.StatusNode node = nl.getHead().getPrevious();

		if (node == nl.getHead()) {
			c_node = null;
			return null;
		}
		c_node = node.getPrevious();

		return node;
	}

	public org.rsbot.client.StatusNode getNext() {
		final org.rsbot.client.StatusNode node = c_node;

		if (node == nl.getHead() || node == null) {
			c_node = null;
			return null;
		}
		c_node = node.getNext();

		return node;
	}

	public org.rsbot.client.StatusNode getPrevious() {
		final org.rsbot.client.StatusNode node = c_node;

		if (node == nl.getHead() || node == null) {
			c_node = null;
			return null;
		}
		c_node = node.getNext();

		return node;
	}

	public int size() {
		int size = 0;
		org.rsbot.client.StatusNode node = nl.getHead().getPrevious();

		while (node != nl.getHead()) {
			node = node.getPrevious();
			size++;
		}

		return size;
	}

}
