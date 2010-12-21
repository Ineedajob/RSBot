package org.rsbot.event.events;

import org.rsbot.event.EventMulticaster;
import org.rsbot.event.listeners.ServerMessageListener;

import java.util.EventListener;

/**
 * A server message event.
 */
@Deprecated
public class ServerMessageEvent extends RSEvent {

	private static final long serialVersionUID = -2786472026976811201L;

	private final String message;

	public ServerMessageEvent(String message) {
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
