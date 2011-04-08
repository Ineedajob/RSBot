package org.rsbot.script.wrappers;

import org.rsbot.script.methods.MethodContext;
import org.rsbot.script.methods.MethodProvider;

import java.util.EnumSet;

/**
 * A class that walks a web skeleton path.
 *
 * @author Timer
 * @author Aut0r
 */

public abstract class WebSkeleton extends MethodProvider {

	/**
	 * Optional handlers while walking the web path.
	 */
	public static enum TraversalOption {
		HANDLE_RUN, SPACE_ACTIONS
	}

	public WebSkeleton(final MethodContext ctx) {
		super(ctx);
	}

	/**
	 * Takes a step on the web path.
	 *
	 * @param options The flags to take into account while traversing the path.
	 * @return If a step was taken on the path.
	 */
	public abstract boolean traverse(EnumSet<TraversalOption> options);

	/**
	 * Takes a step on the web path with traversal options.
	 *
	 * @return If a step was taken on the path.
	 */
	public boolean traverse() {
		return traverse(EnumSet.of(
				TraversalOption.HANDLE_RUN,
				TraversalOption.SPACE_ACTIONS));
	}

}
