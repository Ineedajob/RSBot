package org.rsbot.injector;


import com.sun.org.apache.bcel.internal.classfile.Method;
import com.sun.org.apache.bcel.internal.generic.*;
import com.sun.org.apache.bcel.internal.util.InstructionFinder;

import java.util.Iterator;

@SuppressWarnings("unused")
public class InstructionSearcher {

	public Instruction[] instructions = null;
	private InstructionList il;
	public ConstantPoolGen cp = null;
	public ClassGen classGen = null;
	public int index = -1;
	private String fullName = null;


	public boolean isLastInstruction(int index) {
		return index == instructions.length - 1;
	}

	public InstructionSearcher(ClassGen cg, Method m) {
		index = -1;
		this.cp = cg.getConstantPool();
		this.classGen = cg;
		this.il = new MethodGen(m, cg.getClassName(), cp).getInstructionList();
		this.instructions = il.getInstructions();
		this.fullName = m.toString().replace(m.getName() + "(", cg.getClassName() + "." + m.getName() + "(");
	}

	public String getFullName() {
		return this.fullName;
	}

	public Instruction[] getInstructions() {
		return instructions;
	}

	public int index() {
		return index;
	}

	public Instruction current() {
		if (index >= instructions.length) return null;
		return instructions[index];
	}

	public Instruction next() {
		if (index < instructions.length)
			index++;
		return current();
	}

	public Instruction previous() {
		if (index > 0)
			index--;
		return current();
	}

	public void setPosition(int index) {
		this.index = index;
	}

	public Instruction next(Instruction instr) {
		for (int i = index + 1; i < instructions.length; i++) {
			if (instructions[i].equals(instr)) {
				index = i;
				return current();
			}
		}
		return null;
	}

	public Instruction next(String... search) {
		for (int off = index + 1; off < instructions.length; off++) {
			for (String s : search)
				if (instructions[off].getName().equalsIgnoreCase(s)) {
					index = off;
					return instructions[off];
				}
		}
		return null;
	}

	public Instruction previous(String... search) {
		for (int off = index - 1; off >= 0; off--) {
			for (String s : search)
				if (instructions[off].getName().equalsIgnoreCase(s)) {
					index = off;
					return instructions[off];
				}
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	public InstructionHandle[] nextPattern(String pattern) {
		InstructionFinder ifinder = new InstructionFinder(il);
		Iterator<InstructionHandle[]> i = ifinder.search(pattern);
		while (i.hasNext()) {
			InstructionHandle[] ihs = i.next();
			int pos;
			for (int i1 = 0; i1 < instructions.length; i1++) {
				if (instructions[i1].equals(ihs[0].getInstruction())) {
					pos = i1;
					for (int off = 0; off < ihs.length; off++) {
						if (!instructions[i1 + off].getName().equals(ihs[off].getInstruction().getName())) {
							pos = -1;
							break;
						}
					}
					if (pos != -1 && pos > index) {
						this.index = pos;
						return ihs;
					}
				}
			}
		}
		return null;
	}

	public CPInstruction nextCPInstruction() {
		for (int i = index + 1; i < instructions.length; i++) {
			if (instructions[i] instanceof CPInstruction) {
				index = i;
				return (CPInstruction) instructions[i];
			}
		}
		return null;
	}

	public CPInstruction nextCPInstruction(int ind) {
		for (int i = index + 1; i < instructions.length; i++) {
			if (instructions[i] instanceof CPInstruction) {
				if (((CPInstruction) instructions[i]).getIndex() == ind) {
					index = i;
					return (CPInstruction) instructions[i];
				}
			}
		}
		return null;
	}

	public IfInstruction nextIfInstruction() {
		for (++index; index < instructions.length; ++index) {
			if (current() instanceof IfInstruction)
				return (IfInstruction) current();
		}
		return null;
	}

	public IfInstruction previousIfInstruction() {
		for (--index; index >= 0; --index) {
			if (current() instanceof IfInstruction)
				return (IfInstruction) current();
		}
		return null;
	}

	public BIPUSH nextBIPUSH(int... values) {
		for (int i = index + 1; i < instructions.length; i++) {
			if (instructions[i] instanceof BIPUSH) {
				for (int value : values) {
					if (((BIPUSH) instructions[i]).getValue().intValue() == value) {
						index = i;
						return (BIPUSH) instructions[i];
					}
				}
			}
		}
		return null;
	}

	public Instruction nextNumber(int... values) {
		int startIDX = index;
		for (++index; index < instructions.length; ++index) {
			for (int value : values) {
				if (current() instanceof BIPUSH) {
					if (((BIPUSH) current()).getValue().intValue() == value) return current();
				} else if (current() instanceof ICONST) {
					if (((ICONST) current()).getValue().intValue() == value) return current();
				} else if (current() instanceof SIPUSH) {
					if (((SIPUSH) current()).getValue().intValue() == value) return current();
				}
			}
		}
		setPosition(startIDX);
		return null;
	}

	public Instruction previousNumber(int... values) {
		int startIDX = index;
		for (--index; index >= 0; --index) {
			for (int value : values) {
				if (current() instanceof BIPUSH) {
					if (((BIPUSH) current()).getValue().intValue() == value) return current();
				} else if (current() instanceof ICONST) {
					if (((ICONST) current()).getValue().intValue() == value) return current();
				} else if (current() instanceof SIPUSH) {
					if (((SIPUSH) current()).getValue().intValue() == value) return current();
				}
			}
		}
		setPosition(startIDX);
		return null;
	}

	public SIPUSH previousSIPUSH(int... values) {
		SIPUSH toReturn;
		int startIndex = index;
		for (int value : values) {
			if ((toReturn = previousSIPUSH(value)) != null)
				return toReturn;
			setPosition(startIndex);
		}
		return null;
	}

	public SIPUSH nextSIPUSH(int... values) {
		SIPUSH toReturn;
		int startIDX = index;
		for (int value : values) {
			if ((toReturn = nextSIPUSH(value)) != null)
				return toReturn;
			setPosition(startIDX);
		}
		return null;
	}

	public BIPUSH nextBIPUSH(int value) {
		for (++index; index < instructions.length; ++index) {
			if (current() instanceof BIPUSH) {
				if (((BIPUSH) current()).getValue().intValue() == value)
					return (BIPUSH) current();
			}
		}
		return null;
	}

	public BIPUSH previousBIPUSH(int value) {
		for (--index; index > -1; --index) {
			if (current() instanceof BIPUSH) {
				if (((BIPUSH) current()).getValue().intValue() == value)
					return (BIPUSH) current();
			}
		}
		return null;
	}

	public SIPUSH nextSIPUSH(int value) {
		for (++index; index < instructions.length; ++index) {
			if (current() instanceof SIPUSH) {
				if (((SIPUSH) current()).getValue().intValue() == value)
					return (SIPUSH) current();
			}
		}
		return null;
	}

	public SIPUSH previousSIPUSH(int value) {
		for (--index; index > -1; --index) {
			if (current() instanceof SIPUSH) {
				if (((SIPUSH) current()).getValue().intValue() == value)
					return (SIPUSH) current();
			}
		}
		return null;
	}

	public LDC nextLDC(int ref) {
		for (++index; index < instructions.length; ++index) {
			if (current() instanceof LDC) {
				if (((LDC) current()).getIndex() == (ref))
					return (LDC) current();
			}
		}
		return null;
	}

	public LDC nextIntLDC(int... values) {
		for (++index; index < instructions.length; ++index) {
			if (current() instanceof LDC) {
				for (int value : values) {
					if (((LDC) current()).getValue(cp).equals(value)) {
						return (LDC) current();
					}
				}
			}
		}
		return null;
	}

	public LDC nextLDC(Object value) {
		for (++index; index < instructions.length; ++index) {
			if (current() instanceof LDC) {
				if (((LDC) current()).getValue(cp).equals(value)) {
					return (LDC) current();
				}
			}
		}
		return null;
	}

	public LDC previousLDC(String value) {
		for (--index; index > -1; --index) {
			if (current() instanceof LDC) {
				if (((LDC) current()).getValue(cp).equals(value))
					return (LDC) current();
			}
		}
		return null;
	}

	public FieldInstruction nextFieldInstruction() {
		for (++index; index < instructions.length; ++index) {
			if (current() instanceof FieldInstruction) {
				return (FieldInstruction) current();
			}
		}
		return null;
	}

	public FieldInstruction previousFieldInstruction() {
		for (--index; index > -1; --index) {
			if (current() instanceof FieldInstruction) {
				return (FieldInstruction) current();
			}
		}
		return null;
	}

	public FieldInstruction nextFieldInstruction(int ref) {
		for (++index; index < instructions.length; ++index) {
			if (current() instanceof FieldInstruction) {
				if (((FieldInstruction) current()).getIndex() == ref)
					return (FieldInstruction) current();
			}
		}
		return null;
	}

	public FieldInstruction previousFieldInstruction(int ref) {
		for (--index; index > -1; --index) {
			if (current() instanceof FieldInstruction) {
				if (((FieldInstruction) current()).getIndex() == (ref))
					return (FieldInstruction) current();
			}
		}
		return null;
	}

	public FieldInstruction nextFieldInstructionType(Type type) {
		for (++index; index < instructions.length; ++index) {
			if (current() instanceof FieldInstruction) {
				if (((FieldInstruction) current()).getFieldType(cp).equals(type))
					return (FieldInstruction) current();
			}
		}
		return null;
	}

	public FieldInstruction nextFieldInstruction(String className) {
		for (++index; index < instructions.length; ++index) {
			if (current() instanceof FieldInstruction) {
				if (((FieldInstruction) current()).getClassName(cp).equals(className))
					return (FieldInstruction) current();
			}
		}
		return null;
	}

	public FieldInstruction nextFieldInstruction(String className, String fieldName) {
		for (++index; index < instructions.length; ++index) {
			if (current() instanceof FieldInstruction) {
				if (((FieldInstruction) current()).getClassName(cp).equals(className) &&
						((FieldInstruction) current()).getFieldName(cp).equals(fieldName))
					return (FieldInstruction) current();
			}
		}
		return null;
	}

	public FieldInstruction nextFieldInstructionSignature(String signature) {
		for (++index; index < instructions.length; ++index) {
			if (current() instanceof FieldInstruction) {
				if (((FieldInstruction) current()).getSignature(cp).equals(signature))
					return (FieldInstruction) current();
			}
		}
		return null;
	}

	public FieldInstruction previousFieldInstructionType(Type type) {
		int t_index = index;
		for (--index; index > -1; --index) {
			if (current() instanceof FieldInstruction) {
				if (((FieldInstruction) current()).getFieldType(cp).equals(type))
					return (FieldInstruction) current();
			}
		}
		index = t_index;
		return null;
	}

	public FieldInstruction previousFieldInstruction(String className, String fieldName) {
		for (--index; index > 0; --index) {
			if (current() instanceof FieldInstruction) {
				if (((FieldInstruction) current()).getClassName(cp).equals(className) &&
						((FieldInstruction) current()).getFieldName(cp).equals(fieldName))
					return (FieldInstruction) current();
			}
		}
		return null;
	}

	public FieldInstruction nextGETSTATICType(Type type) {
		for (++index; index < instructions.length; ++index) {
			if (current() instanceof GETSTATIC) {
				if (((GETSTATIC) current()).getFieldType(cp).equals(type))
					return (GETSTATIC) current();
			}
		}
		return null;
	}

	public FieldInstruction nextGETFIELDType(Type type) {
		int i = index;
		for (++index; index < instructions.length; ++index) {
			if (current() instanceof GETFIELD) {
				if (((GETFIELD) current()).getFieldType(cp).equals(type))
					return (GETFIELD) current();
			}
		}
		index = i;
		return null;
	}

	public FieldInstruction previousGETSTATICType(Type type) {
		for (--index; index > -1; --index) {
			if (current() instanceof GETSTATIC) {
				if (((GETSTATIC) current()).getFieldType(cp).equals(type))
					return (GETSTATIC) current();
			}
		}
		return null;
	}

	public InvokeInstruction nextInvokeInstruction(int ref) {
		for (++index; index < instructions.length; ++index) {
			if (current() instanceof InvokeInstruction) {
				if (((InvokeInstruction) current()).getIndex() == (ref))
					return (InvokeInstruction) current();
			}
		}
		return null;
	}

	public InvokeInstruction nextInvokeInstruction() {
		for (++index; index < instructions.length; ++index) {
			if (current() instanceof InvokeInstruction) {
				return (InvokeInstruction) current();
			}
		}
		return null;
	}

	public InvokeInstruction previousInvokeInstruction() {
		for (--index; index > -1; --index) {
			if (current() instanceof InvokeInstruction) {
				return (InvokeInstruction) current();
			}
		}
		return null;
	}

	public InvokeInstruction nextInvokeInstruction(String methodName) {
		for (++index; index < instructions.length; ++index) {
			if (current() instanceof InvokeInstruction) {
				if (((InvokeInstruction) current()).getMethodName(cp).equals(methodName))
					return (InvokeInstruction) current();
			}
		}
		return null;
	}

	public InvokeInstruction previousInvokeInstruction(String value) {
		for (--index; index > -1; --index) {
			if (current() instanceof InvokeInstruction) {
				if (((InvokeInstruction) current()).getMethodName(cp).equals(value))
					return (InvokeInstruction) current();
			}
		}
		return null;
	}

	public ConstantPushInstruction nextConstantPushInstruction(int con) {
		for (++index; index < instructions.length; ++index) {
			Instruction instruction = instructions[index];
			if (instruction instanceof ConstantPushInstruction) {
				ConstantPushInstruction cpi = (ConstantPushInstruction) instruction;
				if (cpi.getValue().intValue() == con) {
					return cpi;
				}
			}
		}
		return null;
	}

	public ConstantPushInstruction previousConstantPushInstruction(int con) {
		for (--index; index > -1; --index) {
			Instruction instruction = instructions[index];
			if (instruction instanceof ConstantPushInstruction) {
				ConstantPushInstruction cpi = (ConstantPushInstruction) instruction;
				if (cpi.getValue().intValue() == con) {
					return cpi;
				}
			}
		}
		return null;
	}

	public MULTIANEWARRAY nextMultiANewArray(ObjectType type, int dimensions) {
		for (int off = index + 1; off < instructions.length; off++) {
			if (instructions[off] instanceof MULTIANEWARRAY) {
				if (((MULTIANEWARRAY) instructions[off]).getLoadClassType(cp) != null) {
					if (((MULTIANEWARRAY) instructions[off]).getLoadClassType(cp).equals(type)) {
						if (((MULTIANEWARRAY) instructions[off]).getDimensions() == dimensions) {
							index = off;
							return (MULTIANEWARRAY) current();
						}
					}
				}
			}
		}
		return null;
	}

	public LocalVariableInstruction previousLocalVariableInstruction() {
		for (int off = index; off >= 0; off--) {
			if (instructions[off] instanceof LocalVariableInstruction) {
				index = off;
				return (LocalVariableInstruction) instructions[off];
			}
		}
		return null;
	}

	public LocalVariableInstruction nextLocalVariableInstruction() {
		for (int off = index + 1; off < instructions.length; off++) {
			if (instructions[off] instanceof LocalVariableInstruction) {
				index = off;
				return (LocalVariableInstruction) instructions[off];
			}
		}
		return null;
	}

	public LocalVariableInstruction previousLocalVariableInstruction(int ind, boolean store) {//true is store, false is load!
		for (int off = index; off >= 0; off--) {
			if (store ? instructions[off] instanceof StoreInstruction : instructions[off] instanceof LoadInstruction) {
				if (((LocalVariableInstruction) instructions[off]).getIndex() == ind) {
					index = off;
					return (LocalVariableInstruction) instructions[off];
				}
			}
		}
		return null;
	}

	public LocalVariableInstruction nextLocalVariableInstruction(int ind, boolean store) { //true is store, false is load!
		for (int off = index + 1; off < instructions.length; off++) {
			if (store ? instructions[off] instanceof StoreInstruction : instructions[off] instanceof LoadInstruction) {
				if (((LocalVariableInstruction) instructions[off]).getIndex() == ind) {
					index = off;
					return (LocalVariableInstruction) instructions[off];
				}
			}
		}
		return null;
	}
}

//np0lv4m