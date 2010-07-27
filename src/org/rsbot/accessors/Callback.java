package org.rsbot.accessors;

import org.rsbot.bot.Bot;

public interface Callback {

    public Bot getBot();

    public void notifyServerMessage(String s);

    public void rsCharacterMoved(RSCharacter c, int i);

    public void updateRenderInfo(Render r, RenderData rd);
}
