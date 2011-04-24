package org.rsbot.loader;

import org.rsbot.loader.asm.*;

/**
 * @author Jacmob
 */
public class VersionVisitor implements ClassVisitor {

	private int version;

	public int getVersion() {
		return version;
	}

	public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {

	}

	public void visitSource(String source, String debug) {

	}

	public void visitOuterClass(String owner, String name, String desc) {

	}

	public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
		return null;
	}

	public void visitAttribute(Attribute attr) {

	}

	public void visitInnerClass(String name, String outerName, String innerName, int access) {

	}

	public FieldVisitor visitField(int access, String name, String desc, String signature, Object value) {
		return null;
	}

	public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
		if (!name.equals("main")) {
			return null;
		}
		return new MethodVisitor() {

			public AnnotationVisitor visitAnnotationDefault() {
				return null;
			}

			public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
				return null;
			}

			public AnnotationVisitor visitParameterAnnotation(int parameter, String desc, boolean visible) {

				return null;
			}

			public void visitAttribute(Attribute attr) {

			}

			public void visitCode() {

			}

			public void visitFrame(int type, int nLocal, Object[] local, int nStack, Object[] stack) {

			}

			public void visitInsn(int opcode) {

			}

			public void visitIntInsn(int opcode, int operand) {
				if (opcode == Opcodes.SIPUSH && operand > 400 && operand < 768) {
					version = operand;
				}
			}

			public void visitVarInsn(int opcode, int var) {

			}

			public void visitTypeInsn(int opcode, String type) {

			}

			public void visitFieldInsn(int opcode, String owner, String name, String desc) {

			}

			public void visitMethodInsn(int opcode, String owner, String name, String desc) {

			}

			public void visitJumpInsn(int opcode, Label label) {

			}

			public void visitLabel(Label label) {

			}

			public void visitLdcInsn(Object cst) {

			}

			public void visitIincInsn(int var, int increment) {

			}

			public void visitTableSwitchInsn(int min, int max, Label dflt, Label[] labels) {

			}

			public void visitLookupSwitchInsn(Label dflt, int[] keys, Label[] labels) {

			}

			public void visitMultiANewArrayInsn(String desc, int dims) {

			}

			public void visitTryCatchBlock(Label start, Label end, Label handler, String type) {

			}

			public void visitLocalVariable(String name, String desc, String signature, Label start, Label end, int index) {

			}

			public void visitLineNumber(int line, Label start) {

			}

			public void visitMaxs(int maxStack, int maxLocals) {

			}

			public void visitEnd() {

			}
		};
	}

	public void visitEnd() {

	}

}