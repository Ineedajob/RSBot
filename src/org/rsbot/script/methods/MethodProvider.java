package org.rsbot.script.methods;

/**
 * A class that provides methods that use data from the game client.
 * For internal use.
 * 
 * @author Jacmob
 */
public abstract class MethodProvider {
	
	protected final MethodContext methods;
	
	public MethodProvider(MethodContext ctx) {
		this.methods = ctx;
	}
	
	/**
	 * 
	 * @param min The inclusive lower bound.
	 * @param max The exclusive upper bound.
	 * @return Random integer min <= n < max.
	 */
	public int random(int min, int max) {
        int n = Math.abs(max - min);
        return Math.min(min, max) + (n == 0 ? 0 : methods.random.nextInt(n));
    }

	/**
	 * 
	 * @param min The inclusive lower bound.
	 * @param max The exclusive upper bound.
	 * @return Random min <= n < max.
	 */
    public double random(double min, double max) {
        return Math.min(min, max) + methods.random.nextDouble() * Math.abs(max - min);
    }
	
    /**
     * @param toSleep The time to sleep in milliseconds.
     */
	public void sleep(int toSleep) {
        try {
            long start = System.currentTimeMillis();
            Thread.sleep(toSleep);
            long now;	// Guarantee minimum sleep
            while (start + toSleep > (now = System.currentTimeMillis())) {
                Thread.sleep(start + toSleep - now);
            }
        } catch (InterruptedException ignored) {
        }
    }

}
