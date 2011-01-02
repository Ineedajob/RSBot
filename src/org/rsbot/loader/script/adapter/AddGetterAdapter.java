package org.rsbot.loader.script.adapter;

import org.rsbot.loader.asm.ClassAdapter;
import org.rsbot.loader.asm.ClassVisitor;
import org.rsbot.loader.asm.MethodVisitor;
import org.rsbot.loader.asm.Opcodes;

/**
 * @author Jacmob
 */
public class AddGetterAdapter extends ClassAdapter implements Opcodes {

	public static class Field {
		public int getter_access;
		public String getter_name;
		public String getter_desc;
		public String owner;
		public String name;
		public String desc;
	}

	private boolean virtual;
	private Field[] fields;

	private String owner;

	public AddGetterAdapter(ClassVisitor delegate, boolean virtual, Field[] fields) {
		super(delegate);
		this.virtual = virtual;
		this.fields = fields;
	}

	public void visit(
			final int version,
			final int access,
			final String name,
			final String signature,
			final String superName,
			final String[] interfaces) {
		this.owner = name;
		cv.visit(version, access, name, signature, superName, interfaces);
	}

	public void visitEnd() {
		if (virtual) {
			for (Field f : fields) {
				visitGetter(f.getter_access, f.getter_name, f.getter_desc, f.name, f.desc);
			}
		} else {
			for (Field f : fields) {
				visitGetter(f.getter_access, f.getter_name, f.getter_desc, f.owner, f.name, f.desc);
			}
		}
		cv.visitEnd();
	}

	private void visitGetter(
			final int getter_access,
			final String getter_name,
			final String getter_desc,
			final String name,
			final String desc) {
		MethodVisitor mv = cv.visitMethod(getter_access, getter_name, getter_desc, null, null);
		mv.visitCode();
		mv.visitVarInsn(ALOAD, 0);
		mv.visitFieldInsn(GETFIELD, owner, name, desc);
		int op = getReturnOpcode(desc);
		mv.visitInsn(op);
		mv.visitMaxs(op == LRETURN || op == DRETURN ? 2 : 1, (getter_access & ACC_STATIC) == 0 ? 1 : 0);
		mv.visitEnd();
	}

	private void visitGetter(
			final int getter_access,
			final String getter_name,
			final String getter_desc,
			final String owner,
			final String name,
			final String desc) {
		MethodVisitor mv = cv.visitMethod(getter_access, getter_name, getter_desc, null, null);
		mv.visitCode();
		mv.visitFieldInsn(GETSTATIC, owner, name, desc);
		int op = getReturnOpcode(desc);
		mv.visitInsn(op);
		mv.visitMaxs(op == LRETURN || op == DRETURN ? 2 : 1, (getter_access & ACC_STATIC) == 0 ? 1 : 0);
		mv.visitEnd();
	}

	private int getReturnOpcode(String desc) {
		desc = desc.substring(desc.indexOf(")") + 1);
		if (desc.length() > 1) {
			return ARETURN;
		}
		char c = desc.charAt(0);
		switch (c) {
			case 'I':
			case 'Z':
			case 'B':
			case 'S':
			case 'C':
				return IRETURN;
			case 'J':
				return LRETURN;
			case 'F':
				return FRETURN;
			case 'D':
				return DRETURN;
		}
		throw new RuntimeException("eek");
	}

}
