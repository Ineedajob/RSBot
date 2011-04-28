package org.rsbot.script.wrappers;

import org.rsbot.client.Client;
import org.rsbot.script.methods.MethodContext;
import org.rsbot.script.methods.MethodProvider;

import java.awt.*;
import java.util.Arrays;
import java.util.Iterator;

/**
 * Represents an interface. Each interface consists
 * of a group of components.
 *
 * @author Qauters
 */
public class RSInterface extends MethodProvider implements Iterable<RSComponent> {

	/**
	 * Cache of this interface's children.
	 */
	private RSComponent[] childCache = new RSComponent[0];

	private final Object childLock = new Object();

	/**
	 * The index of this interface.
	 */
	private final int index;

	public RSInterface(final MethodContext ctx, final int iface) {
		super(ctx);
		index = iface;
	}

	/**
	 * Searches all it's actions, to find your phrase
	 *
	 * @param phrase Text to search for
	 * @return true if found
	 */
	public boolean containsAction(final String phrase) {
		for (final RSComponent child : getComponents()) {
			if (child == null) {
				continue;
			}
			if (child.getActions() == null) {
				return false;
			}
			for (final String action : child.getActions()) {
				if (action == null) {
					continue;
				}
				if (action.toLowerCase().contains(phrase.toLowerCase())) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * Searches all components' text.
	 *
	 * @param phrase Text to search for
	 * @return true if found, false if null
	 */
	public boolean containsText(final String phrase) {
		return getText().contains(phrase);
	}

	/**
	 * Gets the child component at the given index.
	 *
	 * @param id The index of the child.
	 * @return The child component.
	 */
	public RSComponent getComponent(final int id) { // TODO sparseMap
		synchronized (childCache) {
			final org.rsbot.client.RSInterface[] children = getChildrenInternal();
			final int ensureLen = Math.max(children != null ? children.length : 0, id + 1);
			if (childCache.length < ensureLen) { // extend if necessary
				final int prevLen = childCache.length;
				childCache = Arrays.copyOf(childCache, ensureLen);
				for (int i = prevLen; i < ensureLen; i++) {
					childCache[i] = new RSComponent(methods, this, i);
				}
			}
			return childCache[id];
		}
	}

	/**
	 * Gets the amount of child components.
	 *
	 * @return the amount of children, or 0 if null
	 */
	public int getChildCount() {
		final org.rsbot.client.RSInterface[] children = getChildrenInternal();
		if (children != null) {
			return children.length;
		}
		return 0;
	}

	/**
	 * Gets all child components of this interface.
	 *
	 * @return the component array
	 */
	public RSComponent[] getComponents() {
		synchronized (childLock) {
			final org.rsbot.client.RSInterface[] children = getChildrenInternal();
			if (children == null) {
				return childCache.clone(); // return as is
			} else {
				if (childCache.length < children.length) { // extend if necessary
					final int prevLen = childCache.length;
					childCache = Arrays.copyOf(childCache, children.length);
					for (int i = prevLen; i < childCache.length; i++) {
						childCache[i] = new RSComponent(methods, this, i);
					}
				}
				return childCache.clone();
			}
		}
	}

	/**
	 * Gets the index of this interface.
	 *
	 * @return The index of this interface.
	 */
	public int getIndex() {
		return index;
	}

	/**
	 * Gets the screen location of the interface.
	 *
	 * @return the exact location of the interface, return (-1, -1) if
	 *         interface was null.
	 */
	public Point getLocation() {
		final org.rsbot.client.RSInterface[] children = getChildrenInternal();
		if (children != null) {
			for (final org.rsbot.client.RSInterface child : children) {
				if (child != null) {
					if ((child.getMasterX() != -1) && (child.getMasterY() != -1)) {
						return new Point(child.getMasterX(), child.getMasterY());
					}
				}
			}
		}
		return new Point(-1, -1);
	}

	/**
	 * Finds all the text in it, searches all his children for it.
	 *
	 * @return all the text found separated by newlines, empty if null
	 */
	public String getText() {
		final StringBuilder sb = new StringBuilder();
		final org.rsbot.client.RSInterface[] children = getChildrenInternal();
		if (children != null) {
			for (final org.rsbot.client.RSInterface child : children) {
				String string;
				if ((child != null) && ((string = child.getText()) != null)) {
					sb.append(string);
					sb.append("\r\n");
				}
			}
		}
		if (sb.length() > 2) {
			sb.setLength(sb.length() - 2);
		}
		return sb.toString();
	}

	/**
	 * Checks whether or not this interface is valid
	 *
	 * @return <tt>true</tt> if it's valid
	 */
	public boolean isValid() {
		if (getChildrenInternal() == null) {
			return false;
		}
		int idx = getIndex();
		boolean[] validArray = methods.client.getValidRSInterfaceArray();
		if (idx >= 0 && validArray != null && idx < validArray.length && validArray[idx]) {
			org.rsbot.client.RSInterface[][] inters = methods.client.getRSInterfaceCache();
			if (idx < inters.length && inters[idx] != null) {
				RSComponent[] children = getComponents();
				int count = 0;
				for (RSComponent child : children) {
					if (child.getBoundsArrayIndex() == -1) {
						++count;
					}
				}
				return count != children.length;
			}
		}
		return false;
	}

	/**
	 * Iterates over the children of the interface. Will never return null even
	 * if the underlying interface is null.
	 */
	public Iterator<RSComponent> iterator() {
		return new Iterator<RSComponent>() {
			private int nextIdx = 0;

			public boolean hasNext() {
				return !isValid() && (getChildCount() >= nextIdx);
			}

			public RSComponent next() {
				final RSComponent child = getComponent(nextIdx);
				nextIdx++;
				return child;
			}

			public void remove() {
				throw new UnsupportedOperationException();
			}
		};
	}

	/**
	 * Safely gets the array of children.
	 *
	 * @return The child interfaces of the client.
	 */
	org.rsbot.client.RSInterface[] getChildrenInternal() {
		final Client c = methods.client;
		if (c == null) {
			return null;
		}
		final org.rsbot.client.RSInterface[][] inters = c.getRSInterfaceCache();
		if ((inters != null) && (index < inters.length)) {
			return inters[index];
		}
		return null;
	}

	void setChild(final RSComponent child) {
		synchronized (childLock) {
			// safe that the index isn't excessive since it comes from child
			final int idx = child.getIndex();
			if (idx >= childCache.length) {
				getComponent(idx);
				childCache[idx] = child;
			}
		}
	}

	/**
	 * @inheritDoc java/lang/Object#equals(java/lang/Object)
	 */
	@Override
	public boolean equals(final Object obj) {
		if (obj == this) {
			return true;
		}
		if (obj instanceof RSInterface) {
			final RSInterface inter = (RSInterface) obj;
			return inter.index == index;
		}
		return false;
	}

	/**
	 * @inheritDoc java/lang/Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return index;
	}

}
