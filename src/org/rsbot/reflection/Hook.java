package org.rsbot.reflection;

public class Hook {

	private final String hookName, className, fieldName;

	public Hook(final String hookName, final String className, final String fieldName) {
		this.hookName = hookName;
		this.className = className;
		this.fieldName = fieldName;
	}

	public String getHookName() {
		return this.hookName;
	}

	public String getClassName() {
		return this.className;
	}

	public String getFieldName() {
		return this.fieldName;
	}

}