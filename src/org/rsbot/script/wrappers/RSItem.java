package org.rsbot.script.wrappers;

import org.rsbot.client.HardReference;
import org.rsbot.client.SoftReference;
import org.rsbot.script.methods.MethodContext;
import org.rsbot.script.methods.MethodProvider;

/**
 * Represents an item (with an id and stack size). May or may not
 * wrap a component.
 */
public class RSItem extends MethodProvider {

	private final int id;
	private final int stack;
	private RSComponent component;

	public RSItem(final MethodContext ctx, final int id, final int stack) {
		super(ctx);
		this.id = id;
		this.stack = stack;
	}

	public RSItem(final MethodContext ctx, final org.rsbot.client.RSItem item) {
		super(ctx);
		id = item.getID();
		stack = item.getStackSize();
	}

	public RSItem(final MethodContext ctx, final RSComponent item) {
		super(ctx);
		id = item.getComponentID();
		stack = item.getComponentStackSize();
		component = item;
	}

	/**
	 * Gets this item's definition if available.
	 *
	 * @return The RSItemDef; or <code>null</code> if unavailable.
	 */
	public RSItemDef getDefinition() {
		try {
			org.rsbot.client.Node ref = methods.nodes.lookup(methods.client.getRSItemDefLoader(), id);

			if (ref != null) {
				if (ref instanceof HardReference) {
					return new RSItemDef((org.rsbot.client.RSItemDef) (((HardReference) ref).get()));
				} else if (ref instanceof SoftReference) {
					Object def = ((SoftReference) ref).getReference().get();

					if (def != null) {
						return new RSItemDef((org.rsbot.client.RSItemDef) def);
					}
				}
			}
			return null;
		} catch (final ClassCastException e) {
			return null;
		}
	}

	/**
	 * Gets this item's id.
	 *
	 * @return The id.
	 */
	public int getID() {
		return id;
	}

	/**
	 * Gets this item's stack size.
	 *
	 * @return The stack size.
	 */
	public int getStackSize() {
		return stack;
	}

	/**
	 * Returns whether or not this item has an available definition.
	 *
	 * @return <tt>true</tt> if an item definition is available;
	 *         otherwise <tt>false</tt>.
	 */
	public boolean hasDefinition() {
		return getDefinition() != null;
	}

	/**
	 * Gets the component wrapped by this RSItem.
	 *
	 * @return The wrapped component or <code>null</code>.
	 */
	public RSComponent getComponent() {
		return component;
	}

	/**
	 * Checks whether or not a valid component is being wrapped.
	 *
	 * @return <tt>true</tt> if there is a visible wrapped component.
	 */
	public boolean isComponentValid() {
		return component != null && component.isValid();
	}

	/**
	 * Gets the name of this item using the wrapped component's name
	 * if available, otherwise the definition if available.
	 *
	 * @return The item's name or <code>null</code> if not found.
	 */
	public String getName() {
		if (component != null) {
			return component.getComponentName();
		} else {
			RSItemDef definition = getDefinition();
			if (definition != null) {
				return definition.getName();
			}
		}
		return null;
	}

	/**
	 * Performs the given action on the component wrapped by
	 * this RSItem if possible.
	 *
	 * @param action The action to perform.
	 * @return <tt>true</tt> if the component was clicked
	 *         successfully; otherwise <tt>false</tt>.
	 */
	public boolean doAction(final String action) {
		return doAction(action, null);
	}

	/**
	 * Performs the given action on the component wrapped by
	 * this RSItem if possible.
	 *
	 * @param action The action to perform.
	 * @param option The option of the action to perform.
	 * @return <tt>true</tt> if the component was clicked
	 *         successfully; otherwise <tt>false</tt>.
	 */
	public boolean doAction(final String action, final String option) {
		return component != null && component.doAction(action, option);
	}

	/**
	 * Clicks the component wrapped by this RSItem if possible.
	 *
	 * @param left <tt>true</tt> if the component should be
	 *             left-click; <tt>false</tt> if it should be right-clicked.
	 * @return <tt>true</tt> if the component was clicked
	 *         successfully; otherwise <tt>false</tt>.
	 */
	public boolean doClick(boolean left) {
		return component != null && component.doClick(left);
	}

}
