package org.rsbot.script.util;

/**
 * A Timer
 */
public class Timer {

	private long endTime;
	private long startTime;
	private long timeLimit;

	/**
	 * Instantiates a new Timer with a given time
	 * period in milliseconds.
	 * 
	 * @param period Time period in milliseconds.
	 */
	public Timer(long period) {
		this.timeLimit = period;
		this.startTime = System.currentTimeMillis();
		this.endTime = startTime + period;
	}

	/**
	 * Returns the number of milliseconds elapsed since
	 * the start time.
	 * 
	 * @return The elapsed time in milliseconds.
	 */
	public long getElapsed() {
		return (System.currentTimeMillis() - startTime);
	}

	/**
	 * Returns the number of milliseconds remaining
	 * until the timer is up.
	 * 
	 * @return The remaining time in milliseconds.
	 */
	public long getRemaining() {
		if (isNotUp()) {
			return (endTime - System.currentTimeMillis());
		}
		return 0;
	}

	/**
	 * Returns <tt>true</tt> if this timer's time period
	 * has elapsed.
	 * 
	 * @return <tt>true</tt> if the time period has passed.
	 */
	public boolean isUp() {
		return (System.currentTimeMillis() >= endTime);
	}

	/**
	 * Returns <tt>true</tt> if this timer's time period
	 * has not yet elapsed.
	 * 
	 * @return <tt>true</tt> if the time period has not yet passed.
	 */
	public boolean isNotUp() {
		return (System.currentTimeMillis() < endTime);
	}

	/**
	 * Restarts this timer using the same period.
	 */
	public void reset() {
		this.endTime = System.currentTimeMillis() + timeLimit;
	}

	/**
	 * Sets the end time of this timer to a given number of
	 * milliseconds from the time it is called. This does
	 * not edit the period of the timer (so will not affect
	 * operation after reset).
	 * 
	 * @param ms The number of milliseconds before the timer
	 * should go up.
	 * @return The new end time.
	 */
	public long setTimerToEndIn(long ms) {
		this.endTime = System.currentTimeMillis() + ms;
		return endTime;
	}

	/**
	 * Returns a formatted String of the time elapsed.
	 * @return The elapsed time formatted hh:mm:ss.
	 */
	public String toStringElapsed() {
		return timeToString(getElapsed());
	}

	/**
	 * Returns a formatted String of the time remaining.
	 * @return The remaining time formatted hh:mm:ss.
	 */
	public String toStringRemaining() {
		return timeToString(getRemaining());
	}

	/**
	 * Converts milliseconds to a String in the format
	 * hh:mm:ss.
	 * 
	 * @param time The number of milliseconds.
	 * @return The formatted String.
	 */
	public static String timeToString(long time) {
		final StringBuilder t = new StringBuilder();
		final long TotalSec = time / 1000;
		final long TotalMin = TotalSec / 60;
		final long TotalHour = TotalMin / 60;
		final int second = (int) TotalSec % 60;
		final int minute = (int) TotalMin % 60;
		final int hour = (int) TotalHour % 60;
		if (hour < 10) {
			t.append("0");
		}
		t.append(hour);
		t.append(":");
		if (minute < 10) {
			t.append("0");
		}
		t.append(minute);
		t.append(":");
		if (second < 10) {
			t.append("0");
		}
		t.append(second);
		return t.toString();
	}
}