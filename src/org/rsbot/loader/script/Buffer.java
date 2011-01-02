package org.rsbot.loader.script;

/**
 * @author Jacmob
 */
public class Buffer {

	private int pos;
	private byte[] data;

	public Buffer(byte[] buffer) {
		data = buffer;
		pos = 0;
	}

	public int g1() {
		return data[pos++] & 0xff;
	}

	public int g2() {
		pos += 2;
		return ((data[pos - 2] & 0xff) << 8) + (data[pos - 1] & 0xff);
	}

	public int g4() {
		pos += 4;
		return ((data[pos - 4] & 0xff) << 24) + ((data[pos - 3] & 0xff) << 16) + ((data[pos - 2] & 0xff) << 8) + (data[pos - 1] & 0xff);
	}

	public long g8() {
		long l = (long) g4() & 0xffffffffL;
		long r = (long) g4() & 0xffffffffL;
		return (l << 32) + r;
	}

	public String gstr() {
		int i = pos;
		while (data[pos++] != 10) {
		}
		return new String(data, i, pos - i - 1);
	}

	public byte[] gstrbyte() {
		int i = pos;
		while (data[pos++] != 10) {
		}
		byte str[] = new byte[pos - i - 1];
		System.arraycopy(data, i, str, i - i, pos - 1 - i);
		return str;
	}

	public void gdata(byte[] data, int len, int off) {
		for (int i = off; i < off + len; i++) {
			data[i] = this.data[pos++];
		}
	}

}
