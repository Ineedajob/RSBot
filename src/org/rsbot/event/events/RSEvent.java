package org.rsbot.event.events;

import java.util.EventListener;
import java.util.EventObject;

public abstract class RSEvent extends EventObject {
    private static final Object dumy = new Object();
    private static final long serialVersionUID = 6977096569226837605L;

    public RSEvent() {
        super(RSEvent.dumy);
    }

    public abstract void dispatch(EventListener el);

    public abstract long getMask();
}
