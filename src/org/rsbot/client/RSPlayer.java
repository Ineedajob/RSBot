package org.rsbot.client;

public interface RSPlayer extends RSCharacter {

	int getLevel();

	String getName();

	int getTeam();

	RSPlayerComposite getComposite();

}
