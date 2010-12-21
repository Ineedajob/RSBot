package org.rsbot.client;

import org.rsbot.bot.Bot;

public interface Callback {

	public Bot getBot();

	public void notifyMessage(int id, String sender, String msg);

	public void rsCharacterMoved(RSCharacter c, int i);

	public void updateRenderInfo(Render r, RenderData rd);
}
