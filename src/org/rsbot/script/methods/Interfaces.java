package org.rsbot.script.methods;

import org.rsbot.script.wrappers.RSComponent;
import org.rsbot.script.wrappers.RSInterface;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Provides access to interfaces.
 */
public class Interfaces extends MethodProvider {

	Interfaces(final MethodContext ctx) {
		super(ctx);
	}

	// A cache of all the interfaces. Only as big as the maximum size of the client's cache.
	private RSInterface[] mainCache = new RSInterface[0];
	// If it doesn't fit in the above cache.
	private Map<Integer, RSInterface> sparseMap = new HashMap<Integer, RSInterface>();

	/**
	 * @return <code>RSInterface</code> array containing all valid interfaces.
	 */
	public synchronized RSInterface[] getAll() {
		enlargeCache();
		final org.rsbot.client.RSInterface[][] inters = methods.client.getRSInterfaceCache();
		if (inters == null)
			return new RSInterface[0];
		final List<RSInterface> out = new ArrayList<RSInterface>();
		for (int i = 0; i < inters.length; i++) {
			if (inters[i] != null) {
				final RSInterface in = get(i);
				if (in.isValid()) {
					out.add(in);
				}
			}
		}
		return out.toArray(new RSInterface[out.size()]);
	}

	/**
	 * @param index The index of the interface.
	 * @return The <tt>RSInterface</tt> for the given index.
	 */
	public synchronized RSInterface get(final int index) {
		RSInterface inter;
		final int cacheLen = mainCache.length;
		if (index < cacheLen) {
			inter = mainCache[index];
			if (inter == null) {
				inter = new RSInterface(methods, index);
				mainCache[index] = inter;
			}
		} else {
			inter = sparseMap.get(index);
			if (inter == null) {
				enlargeCache();
				if (index < cacheLen) {
					inter = mainCache[index];
					if (inter == null) {
						inter = new RSInterface(methods, index);
						mainCache[index] = inter;
					}
				} else {
					inter = new RSInterface(methods, index);
					sparseMap.put(index, inter);
				}
			}
		}
		return inter;
	}

	/**
	 * @param index	  The parent interface index
	 * @param childIndex The component index
	 * @return <tt>RSComponent</tt> for the given index and child index.
	 */
	public RSComponent getComponent(final int index, final int childIndex) {
		return get(index).getComponent(childIndex);
	}

	/**
	 * @param id The packed interface index ((x << 16) | (y & 0xFFFF)).
	 * @return <tt>RSComponent</tt> for the given interface id.
	 */
	public RSComponent getComponent(final int id) {
		final int x = id >> 16;
		final int y = id & 0xFFFF;
		return get(x).getComponent(y);
	}

	/**
	 * @return The maximum known interface cache size.
	 */
	public synchronized int getMaxCacheSize() {
		enlargeCache();
		return mainCache.length;
	}

	/**
	 * @return <tt>true</tt> if continue component is valid; otherwise <tt>false</tt>.
	 */
	public boolean canContinue() {
		return getContinueComponent() != null;
	}

	/**
	 * @return <tt>true</tt> if continue component was clicked; otherwise <tt>false</tt>.
	 */
	public boolean clickContinue() {
		RSComponent cont = getContinueComponent();
		return cont != null && cont.isValid() && cont.doClick(true);
	}

	/**
	 * @return <tt>RSComponent</tt> containing "Click here to continue"; otherwise null.
	 */
	public RSComponent getContinueComponent() {
		if (methods.client.getRSInterfaceCache() == null)
			return null;
		RSInterface[] valid = getAll();
		for (RSInterface iface : valid) {
			if (iface.getIndex() != 137) {
				int len = iface.getChildCount();
				for (int i = 0; i < len; i++) {
					RSComponent child = iface.getComponent(i);
					if (child.containsText("Click here to continue") && child.isValid()
							&& child.getAbsoluteX() > 10 && child.getAbsoluteY() > 300)
						return child;
				}
			}
		}
		return null;
	}

	/**
	 * @param text The text to search each interface for.
	 * @return <tt>RSInterface</tt> array of the interfaces containing specified text.
	 */
	public RSInterface[] getAllContaining(String text) {
		java.util.List<RSInterface> results = new LinkedList<RSInterface>();
		for (RSInterface iface : getAll()) {
			if (iface.getText().toLowerCase().contains(text.toLowerCase())) {
				results.add(iface);
			}
		}
		return results.toArray(new RSInterface[results.size()]);
	}
	
	/**
	 * Scrolls to the component
	 * @param component component to scroll to
	 * @param scrollBarID scrollbar to scroll with
	 * 
	 * @return true when scrolled successfully
	 */
	public boolean scrollTo(RSComponent component, int scrollBarID) {
		RSComponent scrollBar = getComponent(scrollBarID);
		
		return scrollTo(component, scrollBar);
	}
	
	/**
	 * Scrolls to the component
	 * @param component component to scroll to
	 * @param scrollBar scrollbar to scroll with
	 * 
	 * @return true when scrolled successfully
	 */
	public boolean scrollTo(RSComponent component, RSComponent scrollBar) {
		//Check arguments
		if(component == null || scrollBar == null || !component.isValid())
			return false;	
		
		if(scrollBar.getComponents().length != 6)
			return true; //no scrollbar, so probably not scrollable
		
		//Find scrollable area
		RSComponent scrollableArea = component;
		while( (scrollableArea.getScrollableContentHeight() == 0) && (scrollableArea.getParentID() != -1) )
			scrollableArea = getComponent( scrollableArea.getParentID() );
		
		//Check scrollable area
		if(scrollableArea.getScrollableContentHeight() == 0)
			return false;
		
		//Get scrollable area height
		int areaY = scrollableArea.getAbsoluteY();
		int areaHeight = scrollableArea.getRealHeight();
		
		//Check if the component is already visible
		if( (component.getAbsoluteY() >= areaY) && (component.getAbsoluteY() <= areaY + areaHeight - component.getRealHeight()) )
			return true;
		
		//Calculate scroll bar position to click
		RSComponent scrollBarArea = scrollBar.getComponent(0);
		int contentHeight = scrollableArea.getScrollableContentHeight();
		
		int pos = (int) ((float) scrollBarArea.getRealHeight() / contentHeight * ( component.getRelativeY() + random(-areaHeight / 2, areaHeight / 2 - component.getRealHeight()) ));
		if(pos < 0) //inner
			pos = 0;
		else if(pos >= scrollBarArea.getRealHeight())
			pos = scrollBarArea.getRealHeight() - 1; //outer
		
		//Click on the scrollbar
		methods.mouse.click(scrollBarArea.getAbsoluteX() + random(0, scrollBarArea.getRealWidth()), 
				scrollBarArea.getAbsoluteY() + pos, true);
		
		//Wait a bit
		sleep( random(200, 400) );
		
		//Scroll to it if we missed it
		while( component.getAbsoluteY() < areaY || component.getAbsoluteY() > (areaY + areaHeight - component.getRealHeight()) ) {
			boolean scrollUp = component.getAbsoluteY() < areaY;
			scrollBar.getComponent(scrollUp ? 4 : 5).doAction("");
			
			sleep( random(100, 200) );
		}
		
		//Return whether or not the component is visible now.
		return (component.getAbsoluteY() >= areaY) && (component.getAbsoluteY() <= areaY + areaHeight - component.getRealHeight());
	}

	/**
	 * Enlarges the cache if there are more interfaces than the cache size.
	 */
	private synchronized void enlargeCache() {
		final org.rsbot.client.RSInterface[][] inters = methods.client.getRSInterfaceCache();
		if ((inters != null) && (mainCache.length < inters.length)) { // enlarge cache
			mainCache = Arrays.copyOf(mainCache, inters.length);
			for (int i = mainCache.length; i < mainCache.length; i++) {
				final RSInterface tmp = sparseMap.get(i);
				if (tmp != null) {
					sparseMap.remove(i);
					mainCache[i] = tmp;
				}
			}
		}
	}
}
