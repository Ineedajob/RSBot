package org.rsbot.loader.script;

import org.rsbot.loader.asm.Label;
import org.rsbot.loader.asm.MethodVisitor;

/**
 * @author Jacmob
 */
public class CodeReader {

	static interface Opcodes {
		int INSN = 1;
		int INT_INSN = 2;
		int VAR_INSN = 3;
		int TYPE_INSN = 4;
		int FIELD_INSN = 5;
		int METHOD_INSN = 6;
		int JUMP_INSN = 7;
		int LDC_INSN = 8;
		int IINC_INSN = 9;
		int TABLESWITCH_INSN = 10;
		int LOOKUPSWITCH_INSN = 11;
		int MULTIANEWARRAY_INSN = 12;
		int TRY_CATCH_BLOCK = 13;
		int LOCAL_VARIABLE = 14;
		int LABEL = 15;
	}

	private Buffer code;

	public CodeReader(byte[] code) {
		this.code = new Buffer(code);
	}

	public void accept(MethodVisitor v) {
		int len = code.g2();
		Label[] labels = new Label[code.g1()];
		for (int i = 0, l = labels.length; i < l; ++i) {
			labels[i] = new Label();
		}
		while (len-- > 0) {
			int op = code.g1();
			if (op == Opcodes.INSN) {
				v.visitInsn(code.g1());
			} else if (op == Opcodes.INT_INSN) {
				v.visitIntInsn(code.g1(), code.g2());
			} else if (op == Opcodes.VAR_INSN) {
				v.visitVarInsn(code.g1(), code.g1());
			} else if (op == Opcodes.TYPE_INSN) {
				v.visitTypeInsn(code.g1(), code.gstr());
			} else if (op == Opcodes.FIELD_INSN) {
				v.visitFieldInsn(code.g1(), code.gstr(), code.gstr(), code.gstr());
			} else if (op == Opcodes.METHOD_INSN) {
				v.visitMethodInsn(code.g1(), code.gstr(), code.gstr(), code.gstr());
			} else if (op == Opcodes.JUMP_INSN) {
				v.visitJumpInsn(code.g1(), labels[code.g1()]);
			} else if (op == Opcodes.LDC_INSN) {
				int type = code.g1();
				if (type == 1) {
					v.visitLdcInsn(code.g4());
				} else if (type == 2) {
					v.visitLdcInsn(Float.parseFloat(code.gstr()));
				} else if (type == 3) {
					v.visitLdcInsn(code.g8());
				} else if (type == 4) {
					v.visitLdcInsn(Double.parseDouble(code.gstr()));
				} else if (type == 5) {
					v.visitLdcInsn(code.gstr());
				}
			} else if (op == Opcodes.IINC_INSN) {
				v.visitIincInsn(code.g1(), code.g1());
			} else if (op == Opcodes.TABLESWITCH_INSN) {
				int min = code.g2();
				int max = code.g2();
				Label dflt = labels[code.g1()];
				int n = code.g1(), ptr = 0;
				Label[] lbls = new Label[n];
				while (ptr < n) {
					lbls[ptr++] = labels[code.g1()];
				}
				v.visitTableSwitchInsn(min, max, dflt, lbls);
			} else if (op == Opcodes.LOOKUPSWITCH_INSN) {
				Label dflt = labels[code.g1()];
				int n = code.g1(), ptr = 0;
				int[] keys = new int[n];
				while (ptr < n) {
					keys[ptr++] = code.g2();
				}
				n = code.g1();
				ptr = 0;
				Label[] lbls = new Label[n];
				while (ptr < n) {
					lbls[ptr++] = labels[code.g1()];
				}
				v.visitLookupSwitchInsn(dflt, keys, lbls);
			} else if (op == Opcodes.MULTIANEWARRAY_INSN) {
				v.visitMultiANewArrayInsn(code.gstr(), code.g1());
			} else if (op == Opcodes.TRY_CATCH_BLOCK) {
				v.visitTryCatchBlock(labels[code.g1()], labels[code.g1()], labels[code.g1()], code.gstr());
			} else if (op == Opcodes.LOCAL_VARIABLE) {
				v.visitLocalVariable(code.gstr(), code.gstr(), code.gstr(), labels[code.g1()], labels[code.g1()], code.g1());
			} else if (op == Opcodes.LABEL) {
				v.visitLabel(labels[code.g1()]);
			}
		}
	}

}
