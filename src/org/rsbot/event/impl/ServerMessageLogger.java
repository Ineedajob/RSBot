package org.rsbot.event.impl;

import java.util.logging.Logger;

import org.rsbot.event.events.ServerMessageEvent;
import org.rsbot.event.listeners.ServerMessageListener;

public class ServerMessageLogger implements ServerMessageListener {
	
    private final Logger log = Logger.getLogger(ServerMessageLogger.class.getName());

    public void serverMessageRecieved(final ServerMessageEvent e) {
        log.info(e.getMessage());
    }
}
