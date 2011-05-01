package org.rsbot.script.methods;

import org.rsbot.script.internal.wrappers.TileFlags;
import org.rsbot.script.wrappers.RSTile;

import java.util.HashMap;

public class Web extends MethodProvider {
	public static final HashMap<RSTile, TileFlags> map = new HashMap<RSTile, TileFlags>();
	public static boolean loaded = false;

	Web(final MethodContext ctx) {
		super(ctx);
	}
}
