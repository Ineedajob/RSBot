package org.rsbot.script.wrappers;

/**
 * An object definition.
 */
public class RSObjectDef {
    org.rsbot.accessors.RSObjectDef od;

    public RSObjectDef(final org.rsbot.accessors.RSObjectDef od) {
        this.od = od;
    }

    public String[] getActions() {
        return od.getActions();
    }

    public int[] getChildIDs() {
        return od.getChildrenIDs();
    }

    public int getID() {
        // return od.getType();
        return -1;
    }

    public String getName() {
        return od.getName();
    }

}
