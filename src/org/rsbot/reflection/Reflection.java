package org.rsbot.reflection;

import org.rsbot.bot.Bot;

import java.awt.*;

public class Reflection {
	private final Hooks hooks;

	public Reflection(Bot bot) {
		hooks = new Hooks(bot);
	}

	public int invokeInt(Object parent, String hookName) {
		try {
			return (Integer) hooks.getHookValue(parent, hookName);
		} catch (Exception e) {
			return -1;
		}
	}

	public long invokeLong(Object parent, String hookName) {
		try {
			return (Long) hooks.getHookValue(parent, hookName);
		} catch (Exception e) {
			return -1;
		}
	}

	public float invokeFloat(Object parent, String hookName) {
		try {
			return (Float) hooks.getHookValue(parent, hookName);
		} catch (Exception e) {
			return -1;
		}
	}

	public int[] invokeIntArray(Object parent, String hookName) {
		try {
			return (int[]) hooks.getHookValue(parent, hookName);
		} catch (Exception e) {
			return null;
		}
	}

	public int[][] invokeInt2DArray(Object parent, String hookName) {
		try {
			return (int[][]) hooks.getHookValue(parent, hookName);
		} catch (Exception e) {
			return null;
		}
	}

	public String invokeString(Object parent, String hookName) {
		try {
			return (String) hooks.getHookValue(parent, hookName);
		} catch (Exception e) {
			return "null";
		}
	}

	public String[] invokeStringArray(Object parent, String hookName) {
		try {
			return (String[]) hooks.getHookValue(parent, hookName);
		} catch (Exception e) {
			return null;
		}
	}

	public short invokeShort(Object parent, String hookName) {
		try {
			return (Short) hooks.getHookValue(parent, hookName);
		} catch (Exception e) {
			return -1;
		}
	}

	public short[] invokeShortArray(Object parent, String hookName) {
		try {
			return (short[]) hooks.getHookValue(parent, hookName);
		} catch (Exception e) {
			return null;
		}
	}

	public byte invokeByte(Object parent, String hookName) {
		try {
			return (Byte) hooks.getHookValue(parent, hookName);
		} catch (Exception e) {
			return -1;
		}
	}

	public boolean invokeBoolean(Object parent, String hookName) {
		try {
			return (Boolean) hooks.getHookValue(parent, hookName);
		} catch (Exception e) {
			return false;
		}
	}

	public boolean[] invokeBooleanArray(Object parent, String hookName) {
		try {
			return (boolean[]) hooks.getHookValue(parent, hookName);
		} catch (Exception e) {
			return null;
		}
	}

	public Object invokeObject(Object parent, String hookName) {
		try {
			return hooks.getHookValue(parent, hookName);
		} catch (Exception e) {
			return null;
		}
	}

	public Object[] invokeObjectArray(Object parent, String hookName) {
		try {
			return (Object[]) hooks.getHookValue(parent, hookName);
		} catch (Exception e) {
			return null;
		}
	}

	public Object[][] invokeObject2DArray(Object parent, String hookName) {
		try {
			return (Object[][]) hooks.getHookValue(parent, hookName);
		} catch (Exception e) {
			return null;
		}
	}

	public Object[][][] invokeObject3DArray(Object parent, String hookName) {
		try {
			return (Object[][][]) hooks.getHookValue(parent, hookName);
		} catch (Exception e) {
			return null;
		}
	}

	public Rectangle[] invokeRectangleArray(Object parent, String hookName) {
		try {
			return (Rectangle[]) hooks.getHookValue(parent, hookName);
		} catch (Exception e) {
			return null;
		}
	}
}