package org.rsbot.accessors;

public interface RSItemDef {

    String[] getActions();

    int getCertID();

    int getCertTemplateID();

    String[] getGroundActions();

    int getID();

    String getName();

    boolean isMembersObject();

    RSItemDefFactory getFactory();
}
