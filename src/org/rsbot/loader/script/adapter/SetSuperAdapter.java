package org.rsbot.loader.script.adapter;

import org.rsbot.loader.asm.*;

/**
 * @author Jacmob
 */
public class SetSuperAdapter extends ClassAdapter {

	private String superName;
	private final String newSuperName;

	public SetSuperAdapter(ClassVisitor delegate, String superName) {
		super(delegate);
		newSuperName = superName;
	}

	public void visit(
			final int version,
			final int access,
			final String name,
			final String signature,
			final String superName,
			final String[] interfaces) {
		this.superName = superName;
		cv.visit(version, access, name, signature, newSuperName, interfaces);
	}

	public MethodVisitor visitMethod(
			final int access,
			final String name,
			final String desc,
			final String signature,
			final String[] exceptions) {
		if (name.equals("<init>")) {
			return new MethodAdapter(cv.visitMethod(access, name, desc, signature, exceptions)) {
				public void visitMethodInsn(int opcode, String owner, String name, String desc) {
					if (opcode == Opcodes.INVOKESPECIAL && owner.equals(superName)) {
						owner = newSuperName;
					}
					mv.visitMethodInsn(opcode, owner, name, desc);
				}
			};
		}
		return cv.visitMethod(access, name, desc, signature, exceptions);
	}

}
