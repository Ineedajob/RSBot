package org.rsbot.client;

public interface MRUNodes {

    int getInitialCount();

    NodeSubList getList();

    NodeCache getNodeCache();

    int getSpaceLeft();

}
