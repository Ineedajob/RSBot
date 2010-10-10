package org.rsbot.script.util;

/**
 * @author Jacmob
 */
public interface Filter<T> {

	public boolean accept(T t);

}
