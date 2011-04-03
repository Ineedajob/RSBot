package org.rsbot.event.listeners;

import java.util.EventListener;

import org.rsbot.event.events.ServerMessageEvent;

@Deprecated
public interface ServerMessageListener extends EventListener {
	abstract void serverMessageRecieved(ServerMessageEvent e);
}
