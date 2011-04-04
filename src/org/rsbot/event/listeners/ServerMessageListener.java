package org.rsbot.event.listeners;

import org.rsbot.event.events.ServerMessageEvent;

import java.util.EventListener;

@Deprecated
public interface ServerMessageListener extends EventListener {
	abstract void serverMessageRecieved(ServerMessageEvent e);
}
