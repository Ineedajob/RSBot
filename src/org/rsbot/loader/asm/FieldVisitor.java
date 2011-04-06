/***
 * ASM: a very small and fast Java bytecode manipulation framework
 * Copyright (c) 2000-2007 INRIA, France Telecom
 * All rights reserved.
 */
package org.rsbot.loader.asm;

/**
 * A visitor to visit a Java field. The methods of this interface must be called
 * in the following order: ( <tt>visitAnnotation</tt> |
 * <tt>visitAttribute</tt> )* <tt>visitEnd</tt>.
 *
 * @author Eric Bruneton
 */
public interface FieldVisitor {

	/**
	 * Visits an annotation of the field.
	 *
	 * @param desc    the class descriptor of the annotation class.
	 * @param visible <tt>true</tt> if the annotation is visible at runtime.
	 * @return a visitor to visit the annotation values, or <tt>null</tt> if
	 *         this visitor is not interested in visiting this annotation.
	 */
	AnnotationVisitor visitAnnotation(String desc, boolean visible);

	/**
	 * Visits a non standard attribute of the field.
	 *
	 * @param attr an attribute.
	 */
	void visitAttribute(Attribute attr);

	/**
	 * Visits the end of the field. This method, which is the last one to be
	 * called, is used to inform the visitor that all the annotations and
	 * attributes of the field have been visited.
	 */
	void visitEnd();
}
