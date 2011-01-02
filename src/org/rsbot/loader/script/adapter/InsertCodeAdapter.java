package org.rsbot.loader.script.adapter;

import org.rsbot.loader.asm.AnnotationVisitor;
import org.rsbot.loader.asm.Attribute;
import org.rsbot.loader.asm.ClassAdapter;
import org.rsbot.loader.asm.ClassVisitor;
import org.rsbot.loader.asm.Label;
import org.rsbot.loader.asm.MethodVisitor;
import org.rsbot.loader.script.CodeReader;

import java.util.Map;

/**
 * @author Jacmob
 */
public class InsertCodeAdapter extends ClassAdapter {

	private String method_name;
	private String method_desc;
	private Map<Integer, byte[]> fragments;
	private int max_locals;
	private int max_stack;

	public InsertCodeAdapter(
			ClassVisitor delegate,
			String method_name,
			String method_desc,
			Map<Integer, byte[]> fragments,
			int max_locals,
			int max_stack) {
		super(delegate);
		this.method_name = method_name;
		this.method_desc = method_desc;
		this.fragments = fragments;
		this.max_locals = max_locals;
		this.max_stack = max_stack;
	}

	public MethodVisitor visitMethod(
			final int access,
			final String name,
			final String desc,
			final String signature,
			final String[] exceptions) {
		MethodVisitor mv = cv.visitMethod(access, name, desc, signature, exceptions);
		if (name.equals(method_name) && desc.equals(method_desc)) {
			return new MethodAdapter(mv, fragments, max_locals, max_stack);
		}
		return mv;
	}

	static class MethodAdapter implements MethodVisitor {

		private MethodVisitor mv;
		private Map<Integer, byte[]> fragments;
		private int max_locals;
		private int max_stack;

		private int idx = 0;

		MethodAdapter(
				MethodVisitor delegate,
				Map<Integer, byte[]> fragments,
				int max_locals,
				int max_stack) {
			this.mv = delegate;
			this.fragments = fragments;
			this.max_locals = max_locals;
			this.max_stack = max_stack;
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
			checkFragments();
			mv.visitInsn(opcode);
		}

		public void visitIntInsn(int opcode, int operand) {
			checkFragments();
			mv.visitIntInsn(opcode, operand);
		}

		public void visitVarInsn(int opcode, int var) {
			checkFragments();
			mv.visitVarInsn(opcode, var);
		}

		public void visitTypeInsn(int opcode, String type) {
			checkFragments();
			mv.visitTypeInsn(opcode, type);
		}

		public void visitFieldInsn(int opcode, String owner, String name, String desc) {
			checkFragments();
			mv.visitFieldInsn(opcode, owner, name, desc);
		}

		public void visitMethodInsn(int opcode, String owner, String name, String desc) {
			checkFragments();
			mv.visitMethodInsn(opcode, owner, name, desc);
		}

		public void visitJumpInsn(int opcode, Label label) {
			checkFragments();
			mv.visitJumpInsn(opcode, label);
		}

		public void visitLabel(Label label) {
			checkFragments();
			mv.visitLabel(label);
		}

		public void visitLdcInsn(Object cst) {
			checkFragments();
			mv.visitLdcInsn(cst);
		}

		public void visitIincInsn(int var, int increment) {
			checkFragments();
			mv.visitIincInsn(var, increment);
		}

		public void visitTableSwitchInsn(int min, int max, Label dflt, Label[] labels) {
			checkFragments();
			mv.visitTableSwitchInsn(min, max, dflt, labels);
		}

		public void visitLookupSwitchInsn(Label dflt, int[] keys, Label[] labels) {
			checkFragments();
			mv.visitLookupSwitchInsn(dflt, keys, labels);
		}

		public void visitMultiANewArrayInsn(String desc, int dims) {
			checkFragments();
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
			if (max_stack == -1) {
				mv.visitMaxs(maxStack, maxLocals);
			} else {
				mv.visitMaxs(max_stack, max_locals);
			}
		}

		public void visitEnd() {
			mv.visitEnd();
		}

		private void checkFragments() {
			if (fragments.containsKey(++idx)) {
				new CodeReader(fragments.get(idx)).accept(mv);
			}
		}

	}

}
