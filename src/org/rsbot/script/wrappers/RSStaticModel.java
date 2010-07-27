package org.rsbot.script.wrappers;

import org.rsbot.accessors.LDModel;
import org.rsbot.script.methods.MethodContext;

class RSStaticModel extends RSModel {
	
	protected int x, y;

	RSStaticModel(MethodContext ctx, LDModel model, int x, int y) {
		super(ctx, model);
		this.x = x;
		this.y = y;
	}

	@Override
	protected int getLocalX() {
		return x;
	}

	@Override
	protected int getLocalY() {
		return y;
	}

}
