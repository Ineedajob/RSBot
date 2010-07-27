package org.rsbot.event.events;

import java.util.EventListener;

import org.rsbot.event.EventMulticaster;
import org.rsbot.event.listeners.CharacterMovedListener;
import org.rsbot.script.methods.MethodContext;

/**
 * A character moved event.
 */
public class CharacterMovedEvent extends RSEvent {
	
    private static final long serialVersionUID = 8883312847545757405L;
    
    private final MethodContext ctx;
    private final org.rsbot.accessors.RSCharacter character;
    private final int direction;
    private org.rsbot.script.wrappers.RSCharacter wrapped;

    public CharacterMovedEvent(final MethodContext ctx, final org.rsbot.accessors.RSCharacter character, final int direction) {
    	this.ctx = ctx;
        this.character = character;
        this.direction = direction;
    }

    @Override
    public void dispatch(final EventListener el) {
        ((CharacterMovedListener) el).characterMoved(this);
    }

    public org.rsbot.script.wrappers.RSCharacter getCharacter() {
        if (wrapped == null) {
            if (character instanceof org.rsbot.accessors.RSNPC) {
                final org.rsbot.accessors.RSNPC npc = (org.rsbot.accessors.RSNPC) character;
                wrapped = new org.rsbot.script.wrappers.RSNPC(ctx, npc);
            } else if (character instanceof org.rsbot.accessors.RSPlayer) {
                final org.rsbot.accessors.RSPlayer player = (org.rsbot.accessors.RSPlayer) character;
                wrapped = new org.rsbot.script.wrappers.RSPlayer(ctx, player);
            } else {
                wrapped = new org.rsbot.script.wrappers.RSCharacter(ctx, character);
            }
        }
        return wrapped;
    }

    /**
     * 0 = NW
     * 1 = N
     * 2 = NE
     * 3 = W
     * 4 = E
     * 5 = SW
     * 6 = S
     * 7 = SE
     */
    public int getDirection() {
        return direction;
    }

    @Override
    public long getMask() {
        return EventMulticaster.CHARACTER_MOVED_EVENT;
	}
}
