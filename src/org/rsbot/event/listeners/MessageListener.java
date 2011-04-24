package org.rsbot.event.listeners;

import org.rsbot.event.events.MessageEvent;

import java.util.EventListener;

public interface MessageListener extends EventListener {
	abstract void messageReceived(MessageEvent e);
}
