package org.rsbot.script.wrappers;

import org.rsbot.script.methods.MethodContext;

import java.lang.ref.SoftReference;

/**
 * Represents a non-player character.
 */
public class RSNPC extends RSCharacter {

	private final SoftReference<org.rsbot.client.RSNPC> npc;

	public RSNPC(final MethodContext ctx, final org.rsbot.client.RSNPC npc) {
		super(ctx);
		this.npc = new SoftReference<org.rsbot.client.RSNPC>(npc);
	}

	@Override
	protected org.rsbot.client.RSCharacter getAccessor() {
		return npc.get();
	}

	public String[] getActions() {
		org.rsbot.client.RSNPCDef def = getDefInternal();
		if (def != null) {
			return def.getActions();
		}
		return new String[0];
	}

	public int getID() {
		org.rsbot.client.RSNPCDef def = getDefInternal();
		if (def != null) {
			return def.getType();
		}
		return -1;
	}

	@Override
	public String getName() {
		org.rsbot.client.RSNPCDef def = getDefInternal();
		if (def != null) {
			return def.getName();
		}
		return "";
	}

	@Override
	public int getLevel() {
		org.rsbot.client.RSNPC c = npc.get();
		if (c == null) {
			return -1;
		} else {
			return c.getLevel();
		}
	}

	/**
	 * @return <tt>true</tt> if RSNPC is interacting with RSPlayer; otherwise
	 *         <tt>false</tt>.
	 */
	@Override
	public boolean isInteractingWithLocalPlayer() {
		RSNPC npc = methods.npcs.getNearest(getID());
		return npc.getInteracting() != null && npc.getInteracting().equals(
				methods.players.getMyPlayer());
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		for (final String act : getActions()) {
			sb.append(act);
			sb.append(",");
		}
		if (sb.length() > 0) {
			sb.setLength(sb.length() - 1);
		}
		return "NPC[" + getName() + "],actions=[" + sb.toString() + "]"
				+ super.toString();
	}

	org.rsbot.client.RSNPCDef getDefInternal() {
		org.rsbot.client.RSNPC c = npc.get();
		if (c == null) {
			return null;
		} else {
			return c.getRSNPCDef();
		}
	}

}
