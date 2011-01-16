package org.rsbot.script.internal;

import java.util.Random;

public class BreakHandler {

	protected final Random random = new Random();

	private long nextBreak;
	private long breakEnd;
	private int ticks = 0;

	public boolean isBreaking() {
		return ticks > 50 && nextBreak > 0 && nextBreak < System.currentTimeMillis()
				&& breakEnd > System.currentTimeMillis();
	}

	public void tick() {
		++ticks;
		if (nextBreak < 0 || nextBreak - System.currentTimeMillis() < -30000) {
			ticks = 0;
			int offset = random(20, 120) * 60000;
			nextBreak = System.currentTimeMillis() + offset;
			if (random(0, 4) != 0) {
				breakEnd = nextBreak + random(2, 40) * 60000 + offset / 6;
			} else {
				breakEnd = nextBreak + random(10, 60) * 1000;
			}
		}
	}

	public long getBreakTime() {
		return breakEnd - System.currentTimeMillis();
	}

	private int random(int min, int max) {
		int n = Math.abs(max - min);
		return Math.min(min, max) + (n == 0 ? 0 : random.nextInt(n));
	}

}
