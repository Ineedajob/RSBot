package org.rsbot.script.wrappers;

import org.rsbot.client.RSInterfaceNode;
import org.rsbot.script.internal.wrappers.HashTable;
import org.rsbot.script.methods.MethodContext;
import org.rsbot.script.methods.MethodProvider;

import java.awt.*;

/**
 * Represents an interface component. An RSComponent may or
 * may not have a parent component, and will always have a
 * parent RSInterface.
 *
 * @author Qauters
 * @author Jacmob
 */
public class RSComponent extends MethodProvider {

	/**
	 * The index of this interface in the parent. If this
	 * component does not have a parent component, this
	 * represents the index in the parent interface;
	 * otherwise this represents the component index in
	 * the parent component.
	 */
	private final int index;

	/**
	 * The parent interface containing this component.
	 */
	private final RSInterface parInterface;

	/**
	 * The parent component
	 */
	private final RSComponent parent;

	/**
	 * Initializes the component.
	 *
	 * @param ctx    The method context.
	 * @param parent The parent interface.
	 * @param index  The child index of this child.
	 */
	RSComponent(final MethodContext ctx, final RSInterface parent, final int index) {
		super(ctx);
		this.parInterface = parent;
		this.index = index;
		this.parent = null;
	}

	/**
	 * Initializes the component.
	 *
	 * @param ctx          The method context.
	 * @param parInterface The parent interface.
	 * @param parent       The parent component.
	 * @param index        The child index of this child.
	 */
	RSComponent(final MethodContext ctx, final RSInterface parInterface, final RSComponent parent, final int index) {
		super(ctx);
		this.parInterface = parInterface;
		this.parent = parent;
		this.index = index;
	}

	/**
	 * Performs the given action on this RSInterfaceChild if it is
	 * showing (valid).
	 *
	 * @param action The menu action to click.
	 * @return <tt>true</tt> if the action was clicked; otherwise <tt>false</tt>.
	 */
	public boolean doAction(final String action) {
		return doAction(action, null);
	}

	/**
	 * Performs the given action on this RSInterfaceChild if it is
	 * showing (valid).
	 *
	 * @param action The menu action to click.
	 * @param option The option of the menu action to click.
	 * @return <tt>true</tt> if the action was clicked; otherwise <tt>false</tt>.
	 */
	public boolean doAction(final String action, final String option) {
		if (!isValid()) {
			return false;
		}
		Rectangle rect = getArea();
		if (rect.x == -1 || rect.y == -1 || rect.width == -1 || rect.height == -1) {
			return false;
		}
		if (!rect.contains(methods.mouse.getLocation())) {
			int min_x = rect.x + 1, min_y = rect.y + 1;
			int max_x = min_x + rect.width - 2, max_y = min_y + rect.height - 2;

			methods.mouse.move(random(min_x, max_x, rect.width / 3),
					random(min_y, max_y, rect.height / 3));
			sleep(random(40, 80));
		}
		return methods.menu.doAction(action, option);
	}

	/**
	 * Left-clicks this component.
	 *
	 * @return <tt>true</tt> if the component was clicked.
	 */
	public boolean doClick() {
		return doClick(true);
	}

	/**
	 * Clicks this component.
	 *
	 * @param leftClick <tt>true</tt> to left-click; <tt>false</tt>
	 *                  to right-click.
	 * @return <tt>true</tt> if the component was clicked.
	 */
	public boolean doClick(boolean leftClick) {
		if (!isValid()) {
			return false;
		}

		Rectangle rect = getArea();
		if (rect.x == -1 || rect.y == -1 || rect.width == -1 || rect.height == -1) {
			return false;
		}
		if (rect.contains(methods.mouse.getLocation())) {
			methods.mouse.click(true);
			return true;
		}

		int min_x = rect.x + 1, min_y = rect.y + 1;
		int max_x = min_x + rect.width - 2, max_y = min_y + rect.height - 2;

		methods.mouse.click(random(min_x, max_x, rect.width / 3),
				random(min_y, max_y, rect.height / 3), leftClick);
		return true;
	}

	/**
	 * Moves the mouse over this component (with normally distributed randomness)
	 * if it is not already.
	 *
	 * @return <tt>true</tt> if the mouse was moved; otherwise <tt>false</tt>.
	 */
	public boolean doHover() {
		if (!isValid()) {
			return false;
		}

		Rectangle rect = getArea();
		if (rect.x == -1 || rect.y == -1 || rect.width == -1 || rect.height == -1) {
			return false;
		}
		if (rect.contains(methods.mouse.getLocation())) {
			return false;
		}

		int min_x = rect.x + 1, min_y = rect.y + 1;
		int max_x = min_x + rect.width - 2, max_y = min_y + rect.height - 2;

		methods.mouse.move(random(min_x, max_x, rect.width / 3),
				random(min_y, max_y, rect.height / 3));
		return true;
	}

	/**
	 * Checks the actions of the child for a given substring
	 *
	 * @param phrase The phrase to check for
	 * @return <tt>true</tt> if found
	 */
	public boolean containsAction(final String phrase) {
		for (final String action : getActions()) {
			if (action.toLowerCase().contains(phrase.toLowerCase())) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Checks the text of this component for a given substring
	 *
	 * @param phrase The phrase to check for
	 * @return <tt>true</tt> if the text contained the phrase
	 * @see #getText()
	 */
	public boolean containsText(final String phrase) {
		return getText().contains(phrase);
	}

	/**
	 * Gets the absolute x position of the child, calculated from
	 * the beginning of the game screen
	 *
	 * @return the absolute x or -1 if null
	 */
	public int getAbsoluteX() {
		// Get internal Interface
		org.rsbot.client.RSInterface inter = getInterfaceInternal();
		if (inter == null) {
			return -1;
		}

		// Define x
		int x;

		// Find parentX
		final int parentID = getParentID();
		if (parentID != -1) {
			x = methods.interfaces.getComponent(parentID >> 16, parentID & 0xFFFF).getAbsoluteX();
		} else // No parentX so get the baseX, using bounds or masterX
		{
			// Get bounds array
			final Rectangle[] bounds = methods.client.getRSInterfaceBoundsArray();

			// Get bounds array index
			final int bi = inter.getBoundsArrayIndex();
			if ((bi >= 0) && (bounds != null) && (bi < bounds.length) && (bounds[bi] != null)) {
				return bounds[bi].x; // Return x here, since it already contains
			}
			// our x!
			else {
				x = inter.getMasterX();
			}
		}

		// Add our x
		x += inter.getX();

		// Find scrollable area
		if (inter.getParentID() != -1) {
			inter = methods.interfaces.getComponent(inter.getParentID() >> 16,
					inter.getParentID() & 0xFFFF).getInterfaceInternal();
			if (inter.getHorizontalScrollBarSize() != 0) {
				x -= inter.getHorizontalScrollBarThumbPosition();
			}
		}

		// Return x
		return x;
	}

	/**
	 * Gets the absolute y position of the child, calculated from
	 * the beginning of the game screen
	 *
	 * @return the absolute y position or -1 if null
	 */
	public int getAbsoluteY() {
		// Get internal Interface
		org.rsbot.client.RSInterface inter = getInterfaceInternal();
		if (inter == null) {
			return -1;
		}

		// Define y
		int y;

		// Find parentY
		final int parentID = getParentID();
		if (parentID != -1) {
			y = methods.interfaces.getComponent(parentID >> 16, parentID & 0xFFFF).getAbsoluteY();
		} else // No parentY so get the baseY, using bounds or masterY
		{
			// Get bounds array
			final Rectangle[] bounds = methods.client.getRSInterfaceBoundsArray();

			// Get bounds array index
			final int bi = inter.getBoundsArrayIndex();
			if ((bi >= 0) && (bounds != null) && (bi < bounds.length) && (bounds[bi] != null)) {
				return bounds[bi].y; // Return y here, since it already contains
			}
			// our y!
			else {
				y = inter.getMasterY();
			}
		}

		// Add our y
		y += inter.getY();

		// Find scrollable area
		if (inter.getParentID() != -1) {
			inter = methods.interfaces.getComponent(inter.getParentID() >> 16,
					inter.getParentID() & 0xFFFF).getInterfaceInternal();
			if (inter.getVerticalScrollBarSize() != 0) {
				y -= inter.getVerticalScrollBarPosition();
			}
		}

		// Return y
		return y;
	}

	/**
	 * Gets the actions of this component.
	 *
	 * @return the actions or an empty array if null
	 */
	public String[] getActions() {
		final org.rsbot.client.RSInterface inter = getInterfaceInternal();
		if (inter != null) {
			return inter.getActions();
		}
		return new String[0];
	}

	/**
	 * Gets the area of this component, calculated from its absolute position
	 *
	 * @return the area or new Rectangle(-1, -1, -1, -1) if null
	 */
	public Rectangle getArea() {
		return new Rectangle(getAbsoluteX(), getAbsoluteY(), getWidth(), getHeight());
	}

	/**
	 * Gets the background color of this component
	 *
	 * @return the background color or -1 if null
	 */
	public int getBackgroundColor() {
		final org.rsbot.client.RSInterface inter = getInterfaceInternal();
		if (inter != null) {
			return inter.getTextureID();
		}
		return -1;
	}

	/**
	 * Gets the border thickness of this component
	 *
	 * @return the border thickness or -1 if null
	 */
	public int getBorderThickness() {
		final org.rsbot.client.RSInterface inter = getInterfaceInternal();
		if (inter != null) {
			return inter.getBorderThickness();
		}
		return -1;
	}

	/**
	 * Gets the bounds array index of this component
	 *
	 * @return the bounds array index or -1 if null
	 */
	public int getBoundsArrayIndex() {
		final org.rsbot.client.RSInterface inter = getInterfaceInternal();
		if (inter != null) {
			return inter.getBoundsArrayIndex();
		}

		return -1;
	}

	/**
	 * The child components (bank items etc) of this component.
	 *
	 * @return The components or RSInterfaceComponent[0] if null
	 */
	public RSComponent[] getComponents() {
		final org.rsbot.client.RSInterface inter = getInterfaceInternal();
		if ((inter != null) && (inter.getComponents() != null)) {
			final RSComponent[] components = new RSComponent[inter.getComponents().length];
			for (int i = 0; i < components.length; i++) {
				components[i] = new RSComponent(methods, parInterface, this, i);
			}
			return components;
		}
		return new RSComponent[0];
	}

	/**
	 * Gets the child component at a given index
	 *
	 * @param idx The child index
	 * @return The child component, or null
	 */
	public RSComponent getComponent(int idx) {
		RSComponent[] components = getComponents();
		if (idx >= 0 && idx < components.length) {
			return components[idx];
		}
		return null;
	}

	/**
	 * Gets the id of this component
	 *
	 * @return The id of this component, or -1 if component == null
	 */
	public int getComponentID() {
		final org.rsbot.client.RSInterface inter = getInterfaceInternal();
		if (inter != null) {
			return inter.getComponentID();
		}
		return -1;
	}

	/**
	 * Gets the index of this component
	 *
	 * @return The index of this component, or -1 if component == null
	 */
	public int getComponentIndex() {
		final org.rsbot.client.RSInterface component = getInterfaceInternal();
		if (component != null) {
			return component.getComponentIndex();
		}

		return -1;
	}

	/**
	 * Gets the stack size of this component
	 *
	 * @return The stack size of this component, or -1 if component == null
	 */
	public int getComponentStackSize() {
		final org.rsbot.client.RSInterface component = getInterfaceInternal();
		if (component != null) {
			return component.getComponentStackSize();
		}

		return -1;
	}

	/**
	 * Gets the name of this component
	 *
	 * @return The name of this component, or "" if component == null
	 */
	public String getComponentName() {
		final org.rsbot.client.RSInterface component = getInterfaceInternal();
		if (component != null) {
			return component.getComponentName();
		}

		return "";
	}

	/**
	 * Gets the height of this component
	 *
	 * @return the height of this component or -1 if null
	 */
	public int getHeight() {
		if (!isInScrollableArea()) {
			return getRealHeight();
		}

		final org.rsbot.client.RSInterface childInterface = getInterfaceInternal();
		if (childInterface != null) {
			return childInterface.getHeight() - 4;
		}
		return -1;
	}

	public int getID() {
		final org.rsbot.client.RSInterface inter = getInterfaceInternal();
		if (inter != null) {
			return inter.getID();
		}
		return -1;

	}

	/**
	 * Returns the index of this interface in the parent.
	 * If this component does not have a parent component,
	 * this represents the index in the parent interface;
	 * otherwise this represents the component index in
	 * the parent component.
	 *
	 * @return The index of this interface.
	 * @see #getInterface()
	 * @see #getParent()
	 */
	public int getIndex() {
		return index;
	}

	/**
	 * Gets the model ID of this component
	 *
	 * @return the model ID or -1 if null
	 */
	public int getModelID() {
		final org.rsbot.client.RSInterface inter = getInterfaceInternal();
		if (inter != null) {
			return inter.getModelID();
		}

		return -1;
	}

	/**
	 * Gets the model type of this component
	 *
	 * @return the model type or -1 if null
	 */
	public int getModelType() {
		final org.rsbot.client.RSInterface inter = getInterfaceInternal();
		if (inter != null) {
			return inter.getModelType();
		}

		return -1;
	}

	public int getModelZoom() {
		final org.rsbot.client.RSInterface inter = getInterfaceInternal();
		if (inter != null) {
			return inter.getModelZoom();
		}
		return -1;

	}

	/**
	 * Gets the parent id of this component. It will first look at the internal
	 * parentID, if that's -1 then it will search the RSInterfaceNC to find its
	 * parent.
	 *
	 * @return the parentID or -1 if none
	 */
	public int getParentID() {
		final org.rsbot.client.RSInterface inter = getInterfaceInternal();
		if (inter == null) {
			return -1;
		}

		if (inter.getParentID() != -1) {
			return inter.getParentID();
		}

		final int mainID = getID() >>> 16;
		final HashTable ncI = new HashTable(methods.client.getRSInterfaceNC());

		for (RSInterfaceNode node = (RSInterfaceNode) ncI.getFirst(); node != null;
		     node = (RSInterfaceNode) ncI.getNext()) {
			if (mainID == node.getMainID()) {
				return (int) node.getID();
			}
		}

		return -1;
	}

	/**
	 * Gets the parent interface of this component.
	 * This component may be nested from its parent
	 * interface in parent components.
	 *
	 * @return The parent interface.
	 */
	public RSInterface getInterface() {
		return parInterface;
	}

	/**
	 * Gets the parent component of this component,
	 * or null if this is a top-level component.
	 *
	 * @return The parent component, or null.
	 */
	public RSComponent getParent() {
		return parent;
	}

	/**
	 * Gets the absolute position of the child
	 *
	 * @return the absolute position or new Point(-1, -1) if null
	 */
	public Point getLocation() {
		return new Point(getAbsoluteX(), getAbsoluteY());
	}

	/**
	 * Returns the center point of this interface
	 *
	 * @return The center point of this interface
	 */
	public Point getCenter() {
		return new Point(getAbsoluteX() + getWidth() / 2, getAbsoluteY() + getHeight() / 2);
	}

	/**
	 * Gets the relative x position of the child, calculated from the beginning
	 * of the interface
	 *
	 * @return the relative x position or -1 if null
	 */
	public int getRelativeX() {
		final org.rsbot.client.RSInterface childInterface = getInterfaceInternal();
		if (childInterface != null) {
			return childInterface.getX();
		}
		return -1;
	}

	/**
	 * Gets the relative y position of the child, calculated from the beginning
	 * of the interface
	 *
	 * @return the relative y position -1 if null
	 */
	public int getRelativeY() {
		final org.rsbot.client.RSInterface childInterface = getInterfaceInternal();
		if (childInterface != null) {
			return childInterface.getY();
		}
		return -1;
	}

	public int getVerticalScrollPosition() {
		final org.rsbot.client.RSInterface childInterface = getInterfaceInternal();
		if (childInterface != null) {
			return childInterface.getVerticalScrollBarPosition();
		}
		return -1;
	}

	public int getHorizontalScrollPosition() {
		final org.rsbot.client.RSInterface childInterface = getInterfaceInternal();
		if (childInterface != null) {
			return childInterface.getHorizontalScrollBarThumbPosition();
		}
		return -1;
	}

	public int getScrollableContentHeight() {
		final org.rsbot.client.RSInterface childInterface = getInterfaceInternal();
		if (childInterface != null) {
			return childInterface.getVerticalScrollBarSize();
		}
		return -1;
	}

	public int getScrollableContentWidth() {
		final org.rsbot.client.RSInterface childInterface = getInterfaceInternal();
		if (childInterface != null) {
			return childInterface.getHorizontalScrollBarSize();
		}
		return -1;
	}

	public int getRealHeight() {
		final org.rsbot.client.RSInterface childInterface = getInterfaceInternal();
		if (childInterface != null) {
			return childInterface.getVerticalScrollBarThumbSize();
		}
		return -1;
	}

	public int getRealWidth() {
		final org.rsbot.client.RSInterface childInterface = getInterfaceInternal();
		if (childInterface != null) {
			return childInterface.getHorizontalScrollBarThumbSize();
		}
		return -1;
	}

	public boolean isInScrollableArea() {
		//Check if we have a parent
		if (this.getParentID() == -1) {
			return false;
		}

		//Find scrollable area
		RSComponent scrollableArea = methods.interfaces.getComponent(this.getParentID());
		while ((scrollableArea.getScrollableContentHeight() == 0) && (scrollableArea.getParentID() != -1)) {
			scrollableArea = methods.interfaces.getComponent(scrollableArea.getParentID());
		}

		//Return if we are in a scrollable area
		return (scrollableArea.getScrollableContentHeight() != 0);
	}

	/**
	 * Gets the selected action name of this component
	 *
	 * @return the selected action name or "" if null
	 */
	public String getSelectedActionName() {
		final org.rsbot.client.RSInterface inter = getInterfaceInternal();
		if (inter != null) {
			return inter.getSelectedActionName();
		}
		return "";
	}

	public int getShadowColor() {
		final org.rsbot.client.RSInterface inter = getInterfaceInternal();
		if (inter != null) {
			return inter.getShadowColor();
		}
		return -1;

	}

	public int getSpecialType() {
		final org.rsbot.client.RSInterface inter = getInterfaceInternal();
		if (inter != null) {
			return inter.getSpecialType();
		}

		return -1;
	}

	/**
	 * Gets the spell name of this component
	 *
	 * @return the spell name or "" if null
	 */
	public String getSpellName() {
		final org.rsbot.client.RSInterface inter = getInterfaceInternal();
		if (inter != null) {
			return inter.getSpellName();
		}
		return "";
	}

	/**
	 * Gets the text of this component
	 *
	 * @return the text or "" if null
	 */
	public String getText() {
		final org.rsbot.client.RSInterface inter = getInterfaceInternal();
		if (inter != null) {
			return inter.getText();
		}
		return "";
	}

	/**
	 * Gets the text color of this component
	 *
	 * @return the text color or -1 if null
	 */
	public int getTextColor() {
		final org.rsbot.client.RSInterface inter = getInterfaceInternal();
		if (inter != null) {
			return inter.getTextColor();
		}
		return -1;
	}

	/**
	 * Gets the tooltip of this component
	 *
	 * @return the tooltip or "" if null
	 */
	public String getTooltip() {
		final org.rsbot.client.RSInterface inter = getInterfaceInternal();
		if (inter != null) {
			return inter.getToolTip();
		}
		return "";
	}

	/**
	 * Gets the type of this component
	 *
	 * @return the type or -1 if null
	 */
	public int getType() {
		final org.rsbot.client.RSInterface inter = getInterfaceInternal();
		if (inter != null) {
			return inter.getType();
		}
		return -1;
	}

	/**
	 * Gets the value index array of this component
	 *
	 * @return the value index array or new int[0][0] if null
	 */
	public int[][] getValueIndexArray() {
		final org.rsbot.client.RSInterface childInterface = getInterfaceInternal();
		if (childInterface != null) {
			final int[][] vindex = childInterface.getValueIndexArray();
			if (vindex != null) { // clone does NOT deep copy
				final int[][] out = new int[vindex.length][0];
				for (int i = 0; i < vindex.length; i++) {
					final int[] cur = vindex[i];
					if (cur != null) {
						// clone, otherwise you have a pointer
						out[i] = cur.clone();
					}
				}
				return out;
			}
		}
		return new int[0][0];
	}

	/**
	 * Gets the width of this component
	 *
	 * @return the width of the component or -1 if null
	 */
	public int getWidth() {
		if (!isInScrollableArea()) {
			return getRealWidth();
		}

		final org.rsbot.client.RSInterface childInterface = getInterfaceInternal();
		if (childInterface != null) {
			return childInterface.getWidth() - 4;
		}
		return -1;
	}

	/**
	 * Gets the xRotation of this component
	 *
	 * @return xRotation of this component
	 */
	public int getXRotation() {
		final org.rsbot.client.RSInterface inter = getInterfaceInternal();
		if (inter != null) {
			return inter.getXRotation();
		}
		return -1;

	}

	/**
	 * Gets the yRotation of this component
	 *
	 * @return yRotation of this component
	 */
	public int getYRotation() {
		final org.rsbot.client.RSInterface inter = getInterfaceInternal();
		if (inter != null) {
			return inter.getYRotation();
		}
		return -1;

	}

	/**
	 * Gets the zRotation of this component
	 *
	 * @return zRotation of this component
	 */
	public int getZRotation() {
		final org.rsbot.client.RSInterface inter = getInterfaceInternal();
		if (inter != null) {
			return inter.getZRotation();
		}
		return -1;
	}

	/**
	 * Determines whether or not this component is
	 * vertically flipped.
	 *
	 * @return <tt>true</tt> if this component is vertically flipped.
	 */
	public boolean isVerticallyFlipped() {
		final org.rsbot.client.RSInterface inter = getInterfaceInternal();
		return (inter != null) && inter.isVerticallyFlipped();
	}

	/**
	 * Determines whether or not this component is
	 * horizontally flipped.
	 *
	 * @return <tt>true</tt> if this component is horizontally flipped.
	 */
	public boolean isHorizontallyFlipped() {
		final org.rsbot.client.RSInterface inter = getInterfaceInternal();
		return (inter != null) && inter.isHorizontallyFlipped();
	}

	/**
	 * Whether or not this child is an inventory interface
	 *
	 * @return <tt>true</tt> if it's an inventory interface
	 */
	public boolean isInventory() {
		final org.rsbot.client.RSInterface inter = getInterfaceInternal();
		return (inter != null) && inter.isInventoryRSInterface();
	}

	/**
	 * Determines whether or not this component is loaded for display.
	 *
	 * @return whether or not the component is valid
	 */
	public boolean isValid() {
		return parInterface.isValid() && getBoundsArrayIndex() != -1;
	}

	@Override
	public boolean equals(final Object obj) {
		if (obj == this) {
			return true;
		}
		if (obj instanceof RSComponent) {
			final RSComponent child = (RSComponent) obj;
			return (index == child.index) && child.parInterface.equals(parInterface);
		}
		return false;
	}

	@Override
	public int hashCode() {
		return parInterface.getIndex() * 31 + index;
	}

	/**
	 * @return The interface represented by this object.
	 */
	org.rsbot.client.RSInterface getInterfaceInternal() {
		if (parent != null) {
			org.rsbot.client.RSInterface p = parent.getInterfaceInternal();
			if (p != null) {
				final org.rsbot.client.RSInterface[] components = p.getComponents();
				if ((components != null) && (index >= 0) && (index < components.length)) {
					return components[index];
				}
			}
		} else {
			final org.rsbot.client.RSInterface[] children = parInterface.getChildrenInternal();
			if ((children != null) && (index < children.length)) {
				return children[index];
			}
		}
		return null;
	}

}
