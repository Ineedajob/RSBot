/***
 * ASM: a very small and fast Java bytecode manipulation framework
 * Copyright (c) 2000-2007 INRIA, France Telecom
 * All rights reserved.
 */
package org.rsbot.loader.asm;

/**
 * An edge in the control flow graph of a method body. See {@link Label Label}.
 *
 * @author Eric Bruneton
 */
class Edge {

	/**
	 * Denotes a normal control flow graph edge.
	 */
	static final int NORMAL = 0;

	/**
	 * Denotes a control flow graph edge corresponding to an exception handler.
	 * More precisely any {@link Edge} whose {@link #info} is strictly positive
	 * corresponds to an exception handler. The actual value of {@link #info} is
	 * the index, in the {@link ClassWriter} type table, of the exception that
	 * is catched.
	 */
	static final int EXCEPTION = 0x7FFFFFFF;

	/**
	 * Information about this control flow graph edge. If
	 * {@link ClassWriter#COMPUTE_MAXS} is used this field is the (relative)
	 * stack size in the basic block from which this edge originates. This size
	 * is equal to the stack size at the "jump" instruction to which this edge
	 * corresponds, relatively to the stack size at the beginning of the
	 * originating basic block. If {@link ClassWriter#COMPUTE_FRAMES} is used,
	 * this field is the kind of this control flow graph edge (i.e. NORMAL or
	 * EXCEPTION).
	 */
	int info;

	/**
	 * The successor block of the basic block from which this edge originates.
	 */
	Label successor;

	/**
	 * The next edge in the list of successors of the originating basic block.
	 * See {@link Label#successors successors}.
	 */
	Edge next;
}
