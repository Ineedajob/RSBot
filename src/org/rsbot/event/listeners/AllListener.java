package org.rsbot.event.listeners;

import java.util.EventListener;
import java.util.EventObject;

public interface AllListener extends EventListener {
    public void fireEvent(EventObject e, long mask);

    public boolean isEnabled(long mask);
}
