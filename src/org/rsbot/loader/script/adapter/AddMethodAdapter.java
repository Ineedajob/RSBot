package org.rsbot.loader.script.adapter;

import org.rsbot.loader.asm.ClassAdapter;
import org.rsbot.loader.asm.ClassVisitor;
import org.rsbot.loader.asm.MethodVisitor;
import org.rsbot.loader.script.CodeReader;

/**
 * @author Jacmob
 */
public class AddMethodAdapter extends ClassAdapter {

	public static class Method {
		public int access;
		public String name;
		public String desc;
		public byte[] code;
		public int max_stack;
		public int max_locals;
	}

	private Method[] methods;

	public AddMethodAdapter(ClassVisitor delegate, Method[] methods) {
		super(delegate);
		this.methods = methods;
	}

	public void visitEnd() {
		for (Method m : methods) {
			MethodVisitor mv = cv.visitMethod(m.access, m.name, m.desc, null, null);
			mv.visitCode();
			new CodeReader(m.code).accept(mv);
			mv.visitMaxs(m.max_stack, m.max_locals);
			mv.visitEnd();
		}
		cv.visitEnd();
	}

}
