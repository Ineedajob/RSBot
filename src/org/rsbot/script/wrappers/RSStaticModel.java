package org.rsbot.script.wrappers;

import org.rsbot.client.LDModel;
import org.rsbot.script.methods.MethodContext;

class RSStaticModel extends RSModel {

	private final int x, y;

	RSStaticModel(MethodContext ctx, LDModel model, int x, int y) {
		super(ctx, model);
		this.x = x;
		this.y = y;
	}

	protected void update() {

	}

	protected int getLocalX() {
		return x;
	}

	protected int getLocalY() {
		return y;
	}

}
