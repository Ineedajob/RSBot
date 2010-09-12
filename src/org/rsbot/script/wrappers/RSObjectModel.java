package org.rsbot.script.wrappers;

import org.rsbot.client.LDModel;
import org.rsbot.client.RSObject;
import org.rsbot.script.methods.MethodContext;

class RSObjectModel extends RSModel {
	
	protected RSObject object;
	
	RSObjectModel(MethodContext ctx, LDModel model, RSObject object) {
		super(ctx, model);
		this.object = object;
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
