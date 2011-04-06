package org.rsbot.script.wrappers;

import org.rsbot.client.Model;
import org.rsbot.client.RSObject;
import org.rsbot.script.methods.MethodContext;

class RSObjectModel extends RSModel {

	private final RSObject object;

	RSObjectModel(MethodContext ctx, Model model, RSObject object) {
		super(ctx, model);
		this.object = object;
	}

	protected void update() {

	}

	@Override
	protected int getLocalX() {
		return object.getX();
	}

	@Override
	protected int getLocalY() {
		return object.getY();
	}

}
