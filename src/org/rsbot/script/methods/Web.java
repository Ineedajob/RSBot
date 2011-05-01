package org.rsbot.script.methods;

import org.rsbot.script.internal.wrappers.TileFlags;
import org.rsbot.script.wrappers.RSTile;

import java.util.HashMap;

public class Web extends MethodProvider {
	public static final HashMap<RSTile, TileFlags> rs_map = new HashMap<RSTile, TileFlags>();

	Web(final MethodContext ctx) {
		super(ctx);
	}
}
