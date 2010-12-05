package org.rsbot.client;

public interface MRUNodes {

    int getInitialCount();

    NodeSubQueue getList();

    NodeCache getNodeCache();

    int getSpaceLeft();

}
