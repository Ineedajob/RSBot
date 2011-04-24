package org.rsbot.client;

/**
 * @author Jacmob
 */
public interface MenuGroupNode extends NodeSub {

	NodeSubQueue getItems();

	String getOption();

	int size();

}
