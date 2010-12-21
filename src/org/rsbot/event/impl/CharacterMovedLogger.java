package org.rsbot.event.impl;

import org.rsbot.event.events.CharacterMovedEvent;
import org.rsbot.event.listeners.CharacterMovedListener;

import java.util.logging.Logger;

public class CharacterMovedLogger implements CharacterMovedListener {

	private final Logger log = Logger.getLogger(CharacterMovedLogger.class.getName());

	public void characterMoved(final CharacterMovedEvent e) {
		log.info("Character Moved: " + String.format("%2d %s", e.getDirection(), e.getCharacter().toString()));
	}
}
