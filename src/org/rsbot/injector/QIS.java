package org.rsbot.injector;

import com.sun.org.apache.bcel.internal.Constants;
import com.sun.org.apache.bcel.internal.classfile.Method;
import com.sun.org.apache.bcel.internal.generic.BranchHandle;
import com.sun.org.apache.bcel.internal.generic.CHECKCAST;
import com.sun.org.apache.bcel.internal.generic.CPInstruction;
import com.sun.org.apache.bcel.internal.generic.ClassGen;
import com.sun.org.apache.bcel.internal.generic.ConstantPoolGen;
import com.sun.org.apache.bcel.internal.generic.Instruction;
import com.sun.org.apache.bcel.internal.generic.InstructionHandle;
import com.sun.org.apache.bcel.internal.generic.InstructionList;
import com.sun.org.apache.bcel.internal.generic.LocalVariableInstruction;
import com.sun.org.apache.bcel.internal.generic.MethodGen;
import com.sun.org.apache.bcel.internal.generic.ObjectType;

import java.util.ArrayList;

/**
 * This is an instruction searcher,
 * use it to locate instructions in an easy way.
 *
 * @author Qauters
 */
@SuppressWarnings("unused")
public class QIS {

	private int index = -1;
	private ConstantPoolGen cp = null;
	private MethodGen mg = null;
	private InstructionList il = null;
	private InstructionHandle[] handles = null;
	private String fullName = null;

	/**
	 * Constructs a new QIS
	 *
	 * @param cg The ClassGen where the method belongs to.
	 * @param m  The Method to search in
	 */
	public QIS(ClassGen cg, Method m) {
		this.cp = cg.getConstantPool();
		this.mg = new MethodGen(m, cg.getClassName(), cp);
		this.il = this.mg.getInstructionList();
		this.handles = this.il.getInstructionHandles();
		this.fullName = m.toString().replace(m.getName() + "(", cg.getClassName() + "." + m.getName() + "(");
	}

	/**
	 * Gets the full name of the method, like int[] client.someMethod(int, byte)
	 *
	 * @return the full name of the method
	 */
	public String getFullName() {
		return this.fullName;
	}

	/**
	 * The current instruction list, useful if you delete instructions.
	 * Note: If you delete instructions, you may still find them when you search!!!
	 * Don't delete instructions if you reloop over it later (otherwise you're bound to get errors)
	 *
	 * @return InstructionList
	 */
	public InstructionList getInstructionList() {
		return il;
	}

	/**
	 * Useful if you modified the instruction list, and want to get the modified method.
	 *
	 * @return The method
	 */
	public Method getMethod() {
		mg.setInstructionList(il);
		mg.stripAttributes(true);
		mg.setMaxLocals();
		mg.setMaxStack();
		return mg.getMethod();
	}

	/**
	 * Gets the current index
	 *
	 * @return current index
	 */
	public int getIndex() {
		return this.index;
	}

	/**
	 * Set's the current index
	 *
	 * @param index Index to set to
	 */
	public void setIndex(int index) {
		this.index = index;
	}

	/**
	 * Set's index to begin
	 */
	public void gotoBegin() {
		this.index = 0;
	}

	/**
	 * Set's index to end
	 */
	public void gotoEnd() {
		this.index = this.handles.length - 1;
	}

	/**
	 * Gets the constant pool index, using a given constant tag and value.
	 *
	 * @param tag   Tag of the constant (Like Constants.CONSTANT_INTEGER)
	 * @param value value to search for (Like new Double(3.4)), that way you can use primitives.
	 * @return index of the constant or -1 if not found.
	 */
	public int getConstantPoolIndex(byte tag, Object value) {
		switch (tag) {
			case Constants.CONSTANT_Class:
				if (value instanceof String) {
					return cp.lookupClass((String) value);
				}
				break;
			case Constants.CONSTANT_Double:
				if (value instanceof Double) {
					return cp.lookupDouble((Double) value);
				}
				break;
			case Constants.CONSTANT_Float:
				if (value instanceof Float) {
					return cp.lookupFloat((Float) value);
				}
				break;
			case Constants.CONSTANT_Integer:
				if (value instanceof Integer) {
					return cp.lookupInteger((Integer) value);
				}
				break;
			case Constants.CONSTANT_InterfaceMethodref:
				if (value instanceof MethodGen) {
					return cp.lookupInterfaceMethodref((MethodGen) value);
				}
				break;
			case Constants.CONSTANT_Long:
				if (value instanceof Long) {
					return cp.lookupLong((Long) value);
				}
				break;
			case Constants.CONSTANT_Methodref:
				if (value instanceof MethodGen) {
					return cp.lookupMethodref((MethodGen) value);
				}
				break;
			case Constants.CONSTANT_String:
				if (value instanceof String) {
					return cp.lookupString((String) value);
				}
				break;
			case Constants.CONSTANT_Utf8:
				if (value instanceof String) {
					return cp.lookupUtf8((String) value);
				}
		}

		return -1;
	}

	/**
	 * Gets the argument instructions of the current instruction.
	 * First dimension will be the number of arguments.
	 * Second dimension the instructions related to that argument.
	 * <p/>
	 * Example:
	 * <br>
	 * IfInstructions have two arguments. But one of them could be (GETSTATIC ICONST_M1 IXOR) etc.
	 *
	 * @return The argument instructions of the current instruction.
	 */
	public InstructionHandle[][] getArgumentInstructions() {
		int stackConsuming = this.handles[this.index].getInstruction().consumeStack(cp);
		ArrayList<InstructionHandle[]> args = new ArrayList<InstructionHandle[]>();
		int t_index = this.index;

		while (stackConsuming > 0) {
			ArrayList<InstructionHandle> instructions = new ArrayList<InstructionHandle>();

			int argument_consuming_neg = Integer.MIN_VALUE;
			int t_consuming = 0;
			while (t_consuming > argument_consuming_neg) {
				//Previous instruction
				t_index--;
				if (t_index < 0) throw new RuntimeException("Stack calculation error! More produced then consumed.");

				int produceStack = this.handles[t_index].getInstruction().produceStack(cp);
				int consumeStack = this.handles[t_index].getInstruction().consumeStack(cp);

				if (produceStack == Constants.UNDEFINED || consumeStack == Constants.UNDEFINED)
					throw new RuntimeException("Undefined instruction error!");
				if (produceStack == Constants.UNPREDICTABLE || consumeStack == Constants.UNPREDICTABLE)
					throw new RuntimeException("Unpredictable stack error!");

				instructions.add(this.handles[t_index]);

				if (argument_consuming_neg == Integer.MIN_VALUE)
					argument_consuming_neg = -produceStack;

				t_consuming -= produceStack;
				t_consuming += consumeStack;
			}

			stackConsuming += argument_consuming_neg;
			args.add(instructions.toArray(new InstructionHandle[instructions.size()]));
		}

		//Inverse arrays!
		InstructionHandle[][] values = args.toArray(new InstructionHandle[args.size()][]);
		InstructionHandle[][] returnValues = new InstructionHandle[values.length][];

		for (int off = 0; off < values.length; off++) {
			int vOff = values.length - off - 1;
			returnValues[off] = new InstructionHandle[values[vOff].length];

			for (int off2 = 0; off2 < values[vOff].length; off2++) {
				int vOff2 = values[vOff].length - off2 - 1;
				returnValues[off][off2] = values[vOff][vOff2];
			}
		}

		return returnValues;
	}

	/**
	 * Gets the current instruction
	 *
	 * @return The current instruction or null if none
	 */
	public Instruction current() {
		if (this.index < 0 || this.index >= handles.length) return null;
		return this.handles[this.index].getInstruction();
	}

	/**
	 * Gets the current instruction handle.
	 * Useful for getting the target etc,
	 * because bcel doesn't copy it, into the instruction.
	 *
	 * @return The current instruction handle or null if none
	 */
	public InstructionHandle currentHandle() {
		if (this.index < 0 || this.index >= handles.length) return null;
		return this.handles[this.index];
	}

	/**
	 * Gets the next instruction
	 *
	 * @return The next instruction or null if none
	 */
	public Instruction next() {
		this.index++;
		return current();
	}

	/**
	 * Gets previous instruction or null if none
	 *
	 * @return The previous instruction or null
	 */
	public Instruction previous() {
		this.index--;
		return current();
	}

	/**
	 * Gets the next instruction, using the given type.
	 * Like next(AALOAD.class) will return an AALOAD instruction!
	 *
	 * @param type the type of the instruction to find (like AALOAD.class)
	 * @return The next instruction, using the given type or null if none
	 */
	public <T> T next(Class<T> type) {
		for (int off = this.index + 1; off < this.handles.length; off++) {
			if (this.handles[off].getInstruction() == null) continue; //instruction has been deleted!

			if (type.isAssignableFrom(this.handles[off].getInstruction().getClass())) {
				this.index = off;
				return type.cast(this.handles[off].getInstruction());
			}
		}
		return null;
	}

	/**
	 * Gets the previous instruction, using the given type.
	 * Like previous(AALOAD.class) will return an AALOAD instruction!
	 *
	 * @param type the type of the instruction to find (like AALOAD.class)
	 * @return The previous instruction, using the given type or null if none
	 */
	public <T> T previous(Class<T> type) {
		for (int off = this.index - 1; off >= 0; off--) {
			if (this.handles[off].getInstruction() == null) continue; //instruction has been deleted!

			if (type.isAssignableFrom(this.handles[off].getInstruction().getClass())) {
				this.index = off;
				return type.cast(this.handles[off].getInstruction());
			}
		}
		return null;
	}

	/**
	 * Gets the target of the current instruction
	 *
	 * @return Index of the target, or -1 if none!
	 */
	public int getTargetIndex() {
		if (current() == null) return -1;
		if (!(this.handles[this.index] instanceof BranchHandle)) return -1;

		InstructionHandle target = ((BranchHandle) this.handles[this.index]).getTarget();
		if (target == null) return -1;

		for (int hOff = 0; hOff < this.handles.length; hOff++) {
			if (this.handles[hOff].equals(target)) return hOff;
		}

		return -1;
	}

	/**
	 * Gets the next constantpool instruction using the given index
	 *
	 * @param index Index to search for
	 * @return the found CPInstruction or null
	 */
	public CPInstruction nextConstantPoolInstruction(int index) {
		for (int off = this.index + 1; off < this.handles.length; off++) {
			if (this.handles[off].getInstruction() == null) continue; //instruction has been deleted!

			if (this.handles[off].getInstruction() instanceof CPInstruction) {
				if (((CPInstruction) this.handles[off].getInstruction()).getIndex() == index) {
					this.index = off;
					return (CPInstruction) this.handles[off].getInstruction();
				}
			}
		}

		return null;
	}

	/**
	 * Gets the previous constantpool instruction using the given index
	 *
	 * @param index Index to search for
	 * @return the found CPInstruction or null
	 */
	public CPInstruction previousConstantPoolInstruction(int index) {
		for (int off = this.index - 1; off >= 0; off--) {
			if (this.handles[off].getInstruction() == null) continue; //instruction has been deleted!

			if (this.handles[off].getInstruction() instanceof CPInstruction) {
				if (((CPInstruction) this.handles[off].getInstruction()).getIndex() == index) {
					this.index = off;
					return (CPInstruction) this.handles[off].getInstruction();
				}
			}
		}

		return null;
	}

	/**
	 * Gets the next Local Variable Instruction, using given index and name
	 *
	 * @param index Index of the local variable
	 * @param name  Name of the instruction, or 0 if any (Constants.ILOAD etc)
	 * @return the LocalVariableInstruction or null
	 */
	public LocalVariableInstruction nextLocalVariableInstruction(int index, short name) {
		for (int off = this.index + 1; off < this.handles.length; off++) {
			if (this.handles[off].getInstruction() == null) continue; //instruction has been deleted!

			if (this.handles[off].getInstruction() instanceof LocalVariableInstruction) {
				LocalVariableInstruction lvi = (LocalVariableInstruction) this.handles[off].getInstruction();
				if ((name == 0 || lvi.getOpcode() == name) && lvi.getIndex() == index) {
					this.index = off;
					return (LocalVariableInstruction) this.handles[off].getInstruction();
				}
			}
		}

		return null;
	}

	/**
	 * Gets the next local variable instruction, using the given instruction's index and name
	 *
	 * @param i	The instruction containing the index
	 * @param name Name of the instruction, or 0 if any (Constants.ILOAD etc)
	 * @return The local variable instruction or null
	 * @see #nextLocalVariableInstruction(int, short)
	 */
	public LocalVariableInstruction nextLocalVariableInstruction(Instruction i, short name) {
		if (!(i instanceof LocalVariableInstruction)) return null;
		return nextLocalVariableInstruction(((LocalVariableInstruction) i).getIndex(), name);
	}

	/**
	 * Gets the previous Local Variable Instruction, using given index and name
	 *
	 * @param index Index of the local variable
	 * @param name  Name of the instruction, or 0 if any (Constants.ILOAD etc)
	 * @return the LocalVariableInstruction or null
	 */
	public LocalVariableInstruction previousLocalVariableInstruction(int index, short name) {
		for (int off = this.index - 1; off >= 0; off--) {
			if (this.handles[off].getInstruction() == null) continue; //instruction has been deleted!

			if (this.handles[off].getInstruction() instanceof LocalVariableInstruction) {
				LocalVariableInstruction lvi = (LocalVariableInstruction) this.handles[off].getInstruction();
				if ((name == 0 || lvi.getOpcode() == name) && lvi.getIndex() == index) {
					this.index = off;
					return (LocalVariableInstruction) this.handles[off].getInstruction();
				}
			}
		}

		return null;
	}

	/**
	 * Gets the previous local variable instruction, using the given instruction's index and name
	 *
	 * @param i	The instruction containing the index
	 * @param name Name of the instruction, or 0 if any (Constants.ILOAD etc)
	 * @return The local variable instruction or null
	 * @see #previousLocalVariableInstruction(int, short)
	 */
	public LocalVariableInstruction previousLocalVariableInstruction(Instruction i, short name) {
		if (!(i instanceof LocalVariableInstruction)) return null;
		return previousLocalVariableInstruction(((LocalVariableInstruction) i).getIndex(), name);
	}

	/**
	 * Gets the next CheckCast using given object type.
	 *
	 * @param type ObjectType to search for. Using null, will return null!
	 * @return the found checkcast, or null if not found.
	 */
	public CHECKCAST nextCheckCast(ObjectType type) {
		if (type == null) return null;

		for (int off = this.index + 1; off < this.handles.length; off++) {
			if (handles[off].getInstruction() instanceof CHECKCAST &&
					((CHECKCAST) this.handles[off].getInstruction()).getLoadClassType(this.cp).equals(type)) {
				this.index = off;
				return (CHECKCAST) this.handles[off].getInstruction();
			}
		}

		return null;
	}

	/**
	 * Gets the previous CheckCast using given object type.
	 *
	 * @param type ObjectType to search for. Using null, will return null!
	 * @return the found checkcast, or null if not found.
	 */
	public CHECKCAST previousCheckCast(ObjectType type) {
		if (type == null) return null;

		for (int off = this.index - 1; off >= 0; off--) {
			if (this.handles[off].getInstruction() instanceof CHECKCAST &&
					((CHECKCAST) this.handles[off].getInstruction()).getLoadClassType(this.cp).equals(type)) {
				this.index = off;
				return (CHECKCAST) this.handles[off].getInstruction();
			}
		}

		return null;
	}
}