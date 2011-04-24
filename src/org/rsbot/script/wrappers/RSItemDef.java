package org.rsbot.script.wrappers;

/**
 * An item definition.
 */
public class RSItemDef {
	private final org.rsbot.client.RSItemDef id;

	public RSItemDef(final org.rsbot.client.RSItemDef id) {
		this.id = id;
	}

	public String[] getActions() {
		return id.getActions();
	}

	public String[] getGroundActions() {
		return id.getGroundActions();
	}

	public String getName() {
		return id.getName();
	}

	public boolean isMembers() {
		return id.isMembersObject();
	}

}
