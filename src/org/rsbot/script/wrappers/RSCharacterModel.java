package org.rsbot.script.wrappers;

import org.rsbot.accessors.LDModel;
import org.rsbot.accessors.RSCharacter;
import org.rsbot.script.methods.MethodContext;

class RSCharacterModel extends RSModel {
	
	protected RSCharacter c;
	
	RSCharacterModel(MethodContext ctx, LDModel model, RSCharacter c) {
		super(ctx, model);
		this.c = c;
	}

	@Override
	protected int getLocalX() {
		return c.getX();
	}

	@Override
	protected int getLocalY() {
		return c.getY();
	}

}
