/**
 *
 */
package org.rsbot.event.listeners;

import java.util.EventListener;

import org.rsbot.event.events.CharacterMovedEvent;

/**
 * @author Qauters
 */
public interface CharacterMovedListener extends EventListener {
    public void characterMoved(CharacterMovedEvent e);
}
