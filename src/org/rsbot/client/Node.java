package org.rsbot.client;

public interface Node {

	long getID();

	Node getNext();

	Node getPrevious();

}
