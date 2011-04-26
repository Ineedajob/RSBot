package org.rsbot.reflection;

import org.rsbot.bot.Bot;

import java.awt.*;

public class Reflection {
	private Hooks hooks;
	private Bot bot;

	public Reflection(Bot bot) {
		this.bot = bot;
		hooks = new Hooks(bot);
	}

	public int getFieldInt(String hookName, Object parent) {
		try {
			return (Integer) hooks.getHookValue(parent, hookName);
		} catch (Exception e) {
			return -1;
		}
	}

	public long getFieldLong(String hookName, Object parent) {
		try {
			return (Long) hooks.getHookValue(parent, hookName);
		} catch (Exception e) {
			return -1;
		}
	}

	public float getFieldFloat(String hookName, Object parent) {
		try {
			return (Float) hooks.getHookValue(parent, hookName);
		} catch (Exception e) {
			return -1;
		}
	}

	public int[] getFieldIntArray(String hookName, Object parent) {
		try {
			return (int[]) hooks.getHookValue(parent, hookName);
		} catch (Exception e) {
			return null;
		}
	}

	public int[][] getFieldInt2DArray(String hookName, Object parent) {
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

	public short getFieldShort(String hookName, Object parent) {
		try {
			return (Short) hooks.getHookValue(parent, hookName);
		} catch (Exception e) {
			return -1;
		}
	}

	public short[] getFieldShortArray(String hookName, Object parent) {
		try {
			return (short[]) hooks.getHookValue(parent, hookName);
		} catch (Exception e) {
			return null;
		}
	}

	public byte getFieldByte(String hookName, Object parent) {
		try {
			return (Byte) hooks.getHookValue(parent, hookName);
		} catch (Exception e) {
			return -1;
		}
	}

	public boolean getFieldBoolean(String hookName, Object parent) {
		try {
			return (Boolean) hooks.getHookValue(parent, hookName);
		} catch (Exception e) {
			return false;
		}
	}

	public boolean[] getFieldBooleanArray(String hookName, Object parent) {
		try {
			return (boolean[]) hooks.getHookValue(parent, hookName);
		} catch (Exception e) {
			return null;
		}
	}

	public Object getFieldObject(String hookName, Object parent) {
		try {
			return hooks.getHookValue(parent, hookName);
		} catch (Exception e) {
			return null;
		}
	}

	public Object[] getFieldObjectArray(String hookName, Object parent) {
		try {
			return (Object[]) hooks.getHookValue(parent, hookName);
		} catch (Exception e) {
			return null;
		}
	}

	public Object[][] getFieldObject2DArray(String hookName, Object parent) {
		try {
			return (Object[][]) hooks.getHookValue(parent, hookName);
		} catch (Exception e) {
			return null;
		}
	}

	public Object[][][] getFieldObject3DArray(String hookName, Object parent) {
		try {
			return (Object[][][]) hooks.getHookValue(parent, hookName);
		} catch (Exception e) {
			return null;
		}
	}

	public Rectangle[] getFieldRectangleArray(String hookName, Object parent) {
		try {
			return (Rectangle[]) hooks.getHookValue(parent, hookName);
		} catch (Exception e) {
			return null;
		}
	}
}