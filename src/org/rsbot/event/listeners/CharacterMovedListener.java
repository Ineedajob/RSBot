/**
 *
 */
package org.rsbot.event.listeners;

import org.rsbot.event.events.CharacterMovedEvent;

import java.util.EventListener;

/**
 * @author Qauters
 */
public interface CharacterMovedListener extends EventListener {
	public void characterMoved(CharacterMovedEvent e);
}
