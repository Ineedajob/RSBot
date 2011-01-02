package org.rsbot.script.internal;

import java.util.Random;

public class BreakHandler {

	protected final Random random = new Random();

	private long nextBreak = System.currentTimeMillis() + random(40, 120) * 60000;

	public int tick() {
		if (nextBreak < System.currentTimeMillis()) {
			int offset = random(20, 120) * 60;
			nextBreak = System.currentTimeMillis() + offset;
			if (random(0, 4) != 0) {
				return random(2, 40) * 60 + offset / 6;
			} else {
				return random(10, 60);
			}
		}
		return 0;
	}

	private int random(int min, int max) {
		int n = Math.abs(max - min);
		return Math.min(min, max) + (n == 0 ? 0 : random.nextInt(n));
	}

}
