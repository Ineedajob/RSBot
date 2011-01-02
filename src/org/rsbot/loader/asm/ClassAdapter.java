/***
 * ASM: a very small and fast Java bytecode manipulation framework
 * Copyright (c) 2000-2007 INRIA, France Telecom
 * All rights reserved.
 */
package org.rsbot.loader.asm;


/**
 * An empty {@link ClassVisitor} that delegates to another {@link ClassVisitor}.
 * This class can be used as a super class to quickly implement usefull class
 * adapter classes, just by overriding the necessary methods.
 *
 * @author Eric Bruneton
 */
public class ClassAdapter implements ClassVisitor {

	/**
	 * The {@link ClassVisitor} to which this adapter delegates calls.
	 */
	protected ClassVisitor cv;

	/**
	 * Constructs a new {@link ClassAdapter} object.
	 *
	 * @param cv the class visitor to which this adapter must delegate calls.
	 */
	public ClassAdapter(final ClassVisitor cv) {
		this.cv = cv;
	}

	public void visit(
			final int version,
			final int access,
			final String name,
			final String signature,
			final String superName,
			final String[] interfaces) {
		cv.visit(version, access, name, signature, superName, interfaces);
	}

	public void visitSource(final String source, final String debug) {
		cv.visitSource(source, debug);
	}

	public void visitOuterClass(
			final String owner,
			final String name,
			final String desc) {
		cv.visitOuterClass(owner, name, desc);
	}

	public AnnotationVisitor visitAnnotation(
			final String desc,
			final boolean visible) {
		return cv.visitAnnotation(desc, visible);
	}

	public void visitAttribute(final Attribute attr) {
		cv.visitAttribute(attr);
	}

	public void visitInnerClass(
			final String name,
			final String outerName,
			final String innerName,
			final int access) {
		cv.visitInnerClass(name, outerName, innerName, access);
	}

	public FieldVisitor visitField(
			final int access,
			final String name,
			final String desc,
			final String signature,
			final Object value) {
		return cv.visitField(access, name, desc, signature, value);
	}

	public MethodVisitor visitMethod(
			final int access,
			final String name,
			final String desc,
			final String signature,
			final String[] exceptions) {
		return cv.visitMethod(access, name, desc, signature, exceptions);
	}

	public void visitEnd() {
		cv.visitEnd();
	}
}
