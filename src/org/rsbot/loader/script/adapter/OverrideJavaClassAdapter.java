package org.rsbot.loader.script.adapter;

import org.rsbot.loader.asm.AnnotationVisitor;
import org.rsbot.loader.asm.Attribute;
import org.rsbot.loader.asm.ClassAdapter;
import org.rsbot.loader.asm.ClassVisitor;
import org.rsbot.loader.asm.FieldVisitor;
import org.rsbot.loader.asm.Label;
import org.rsbot.loader.asm.MethodVisitor;

/**
 * @author Liang
 */
public class OverrideJavaClassAdapter extends ClassAdapter {

	private String old_clazz;
	private String new_clazz;

	public OverrideJavaClassAdapter(ClassVisitor delegate, String old_clazz, String new_clazz) {
		super(delegate);
		this.old_clazz = old_clazz;
		this.new_clazz = new_clazz;
	}

	public MethodVisitor visitMethod(
			final int access,
			final String name,
			final String desc,
			final String signature,
			final String[] exceptions) {
		return new MethodAdapter(cv.visitMethod(access, name, desc, signature, exceptions), old_clazz, new_clazz);
	}

	public FieldVisitor visitField(int access, String name, String desc, String signature, Object value) {
		if(desc.equals("L" + old_clazz + ";"))
			desc = "L" + new_clazz + ";";

		return cv.visitField(access, name, desc, signature, value);
	}

	static class MethodAdapter implements MethodVisitor {

		private MethodVisitor mv;
		private String old_clazz;
		private String new_clazz;

		MethodAdapter(
				MethodVisitor delegate,
				String old_clazz,
				String new_clazz) {
			this.mv = delegate;
			this.old_clazz = old_clazz;
			this.new_clazz = new_clazz;
		}

		public AnnotationVisitor visitAnnotationDefault() {
			return mv.visitAnnotationDefault();
		}

		public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
			return mv.visitAnnotation(desc, visible);
		}

		public AnnotationVisitor visitParameterAnnotation(int parameter, String desc, boolean visible) {
			return mv.visitParameterAnnotation(parameter, desc, visible);
		}

		public void visitAttribute(Attribute attr) {
			mv.visitAttribute(attr);
		}

		public void visitCode() {
			mv.visitCode();
		}

		public void visitFrame(int type, int nLocal, Object[] local, int nStack, Object[] stack) {

		}

		public void visitInsn(int opcode) {
			mv.visitInsn(opcode);
		}

		public void visitIntInsn(int opcode, int operand) {
			mv.visitIntInsn(opcode, operand);
		}

		public void visitVarInsn(int opcode, int var) {
			mv.visitVarInsn(opcode, var);
		}

		public void visitTypeInsn(int opcode, String type) {
			if(type.equals(old_clazz))
				type = new_clazz;

			mv.visitTypeInsn(opcode, type);
		}

		public void visitFieldInsn(int opcode, String owner, String name, String desc) {
			if(desc.contains(old_clazz))
				desc = desc.replace("L" + old_clazz + ";", "L" + new_clazz + ";");

			mv.visitFieldInsn(opcode, owner, name, desc);
		}

		public void visitMethodInsn(int opcode, String owner, String name, String desc) {
			if(owner.equals(old_clazz)) {
				owner = new_clazz;
				desc = desc.replace("L" + old_clazz + ";", "L" + new_clazz + ";");
			}

			mv.visitMethodInsn(opcode, owner, name, desc);
		}

		public void visitJumpInsn(int opcode, Label label) {
			mv.visitJumpInsn(opcode, label);
		}

		public void visitLabel(Label label) {
			mv.visitLabel(label);
		}

		public void visitLdcInsn(Object cst) {
			mv.visitLdcInsn(cst);
		}

		public void visitIincInsn(int var, int increment) {
			mv.visitIincInsn(var, increment);
		}

		public void visitTableSwitchInsn(int min, int max, Label dflt, Label[] labels) {
			mv.visitTableSwitchInsn(min, max, dflt, labels);
		}

		public void visitLookupSwitchInsn(Label dflt, int[] keys, Label[] labels) {
			mv.visitLookupSwitchInsn(dflt, keys, labels);
		}

		public void visitMultiANewArrayInsn(String desc, int dims) {
			mv.visitMultiANewArrayInsn(desc, dims);
		}

		public void visitTryCatchBlock(Label start, Label end, Label handler, String type) {
			mv.visitTryCatchBlock(start, end, handler, type);
		}

		public void visitLocalVariable(String name, String desc, String signature, Label start, Label end, int index) {
			mv.visitLocalVariable(name, desc, signature, start, end, index);
		}

		public void visitLineNumber(int line, Label start) {

		}

		public void visitMaxs(int maxStack, int maxLocals) {
			mv.visitMaxs(maxStack, maxLocals);
		}

		public void visitEnd() {
			mv.visitEnd();
		}

	}

}
