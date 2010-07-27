package org.rsbot.event.events;

import java.util.EventListener;

import org.rsbot.event.EventMulticaster;
import org.rsbot.event.listeners.ServerMessageListener;

/**
 * A server message event.
 */
public class ServerMessageEvent extends RSEvent {
    private static final long serialVersionUID = -2786472026976811201L;
    private final String message;

    public ServerMessageEvent(final String message) {
        this.message = message;
    }

    @Override
    public void dispatch(final EventListener el) {
        ((ServerMessageListener) el).serverMessageRecieved(this);
    }

    @Override
    public long getMask() {
        return EventMulticaster.SERVER_MESSAGE_EVENT;
    }

    public String getMessage() {
        return message;
    }
}
