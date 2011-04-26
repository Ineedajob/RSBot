package org.rsbot.reflection;

import org.rsbot.bot.Bot;

import java.lang.reflect.Field;
import java.util.HashMap;

public class Hooks {
	private static final HashMap<String, Hook> hooks = new HashMap<String, Hook>();
	private final Bot bot;

	public Hooks(Bot bot) {
		this.bot = bot;
	}


	Object getHookValue(Object parent, String hookName) {
		Field field = getHookField(hookName);
		if (field == null) {
			return null;
		}
		field.setAccessible(true);
		try {
			return field.get(parent);
		} catch (Exception e) {
			return null;
		}
	}

	public boolean isHookValid(String hookName) {
		return Hooks.hooks.containsKey(hookName);
	}

	private Field getHookField(String className, String fieldName) {
		try {
			if (className == null || fieldName == null) {
				return null;
			}
			Class<?> theClass = this.bot.getLoader().getClassLoader().loadClass(className);
			Field theField = theClass.getDeclaredField(fieldName);
			theField.setAccessible(true);
			return theField;
		} catch (Exception e) {
			return null;
		}
	}

	private Field getHookField(String hookName) {
		if (hookName == null || !isHookValid(hookName)) {
			return null;
		}
		Hook data = Hooks.hooks.get(hookName);
		if (data == null) {
			return null;
		}
		return getHookField(data.getClassName(), data.getFieldName());
	}
}