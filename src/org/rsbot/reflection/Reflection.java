package org.rsbot.reflection;

import org.rsbot.bot.Bot;

import java.awt.*;

public class Reflection {
	private final Hooks hooks;

	public Reflection(Bot bot) {
		hooks = new Hooks(bot);
	}

	public int getFieldInt(Object parent, String hookName) {
		try {
			return (Integer) hooks.getHookValue(parent, hookName);
		} catch (Exception e) {
			return -1;
		}
	}

	public long getFieldLong(Object parent, String hookName) {
		try {
			return (Long) hooks.getHookValue(parent, hookName);
		} catch (Exception e) {
			return -1;
		}
	}

	public float getFieldFloat(Object parent, String hookName) {
		try {
			return (Float) hooks.getHookValue(parent, hookName);
		} catch (Exception e) {
			return -1;
		}
	}

	public int[] getFieldIntArray(Object parent, String hookName) {
		try {
			return (int[]) hooks.getHookValue(parent, hookName);
		} catch (Exception e) {
			return null;
		}
	}

	public int[][] getFieldInt2DArray(Object parent, String hookName) {
		try {
			return (int[][]) hooks.getHookValue(parent, hookName);
		} catch (Exception e) {
			return null;
		}
	}

	public String getFieldString(Object parent, String hookName) {
		try {
			return (String) hooks.getHookValue(parent, hookName);
		} catch (Exception e) {
			return "null";
		}
	}

	public String[] getFieldStringArray(Object parent, String hookName) {
		try {
			return (String[]) hooks.getHookValue(parent, hookName);
		} catch (Exception e) {
			return null;
		}
	}

	public short getFieldShort(Object parent, String hookName) {
		try {
			return (Short) hooks.getHookValue(parent, hookName);
		} catch (Exception e) {
			return -1;
		}
	}

	public short[] getFieldShortArray(Object parent, String hookName) {
		try {
			return (short[]) hooks.getHookValue(parent, hookName);
		} catch (Exception e) {
			return null;
		}
	}

	public byte getFieldByte(Object parent, String hookName) {
		try {
			return (Byte) hooks.getHookValue(parent, hookName);
		} catch (Exception e) {
			return -1;
		}
	}

	public boolean getFieldBoolean(Object parent, String hookName) {
		try {
			return (Boolean) hooks.getHookValue(parent, hookName);
		} catch (Exception e) {
			return false;
		}
	}

	public boolean[] getFieldBooleanArray(Object parent, String hookName) {
		try {
			return (boolean[]) hooks.getHookValue(parent, hookName);
		} catch (Exception e) {
			return null;
		}
	}

	public Object getFieldObject(Object parent, String hookName) {
		try {
			return hooks.getHookValue(parent, hookName);
		} catch (Exception e) {
			return null;
		}
	}

	public Object[] getFieldObjectArray(Object parent, String hookName) {
		try {
			return (Object[]) hooks.getHookValue(parent, hookName);
		} catch (Exception e) {
			return null;
		}
	}

	public Object[][] getFieldObject2DArray(Object parent, String hookName) {
		try {
			return (Object[][]) hooks.getHookValue(parent, hookName);
		} catch (Exception e) {
			return null;
		}
	}

	public Object[][][] getFieldObject3DArray(Object parent, String hookName) {
		try {
			return (Object[][][]) hooks.getHookValue(parent, hookName);
		} catch (Exception e) {
			return null;
		}
	}

	public Rectangle[] getFieldRectangleArray(Object parent, String hookName) {
		try {
			return (Rectangle[]) hooks.getHookValue(parent, hookName);
		} catch (Exception e) {
			return null;
		}
	}
}