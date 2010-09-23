/***
 * ASM: a very small and fast Java bytecode manipulation framework
 * Copyright (c) 2000-2007 INRIA, France Telecom
 * All rights reserved.
 */
package org.rsbot.injector.asm;

/**
 * Information about the input and output stack map frames of a basic block.
 * 
 * @author Eric Bruneton
 */
final class Frame {

    /**
     * Mask to get the dimension of a frame type. This dimension is a signed
     * integer between -8 and 7.
     */
    static final int DIM = 0xF0000000;

    /**
     * Constant to be added to a type to get a type with one more dimension.
     */
    static final int ARRAY_OF = 0x10000000;

    /**
     * Constant to be added to a type to get a type with one less dimension.
     */
    static final int ELEMENT_OF = 0xF0000000;

    /**
     * Mask to get the kind of a frame type.
     * 
     * @see #BASE
     * @see #LOCAL
     * @see #STACK
     */
    static final int KIND = 0xF000000;

    /**
     * Flag used for LOCAL and STACK types. Indicates that if this type happens
     * to be a long or double type (during the computations of input frames), 
     * then it must be set to TOP because the second word of this value has
     * been reused to store other data in the basic block. Hence the first word 
     * no longer stores a valid long or double value.
     */
    static final int TOP_IF_LONG_OR_DOUBLE = 0x800000;

    /**
     * Mask to get the value of a frame type.
     */
    static final int VALUE = 0x7FFFFF;

    /**
     * Mask to get the kind of base types.
     */
    static final int BASE_KIND = 0xFF00000;

    /**
     * Mask to get the value of base types.
     */
    static final int BASE_VALUE = 0xFFFFF;

    /**
     * Kind of the types that are not relative to an input stack map frame.
     */
    static final int BASE = 0x1000000;

    /**
     * Base kind of the base reference types. The BASE_VALUE of such types is an
     * index into the type table.
     */
    static final int OBJECT = BASE | 0x700000;

    /**
     * Base kind of the uninitialized base types. The BASE_VALUE of such types
     * in an index into the type table (the Item at that index contains both an
     * instruction offset and an internal class name).
     */
    static final int UNINITIALIZED = BASE | 0x800000;

    /**
     * Kind of the types that are relative to the local variable types of an
     * input stack map frame. The value of such types is a local variable index.
     */
    private static final int LOCAL = 0x2000000;

    /**
     * Kind of the the types that are relative to the stack of an input stack
     * map frame. The value of such types is a position relatively to the top of
     * this stack.
     */
    private static final int STACK = 0x3000000;

    /**
     * The TOP type. This is a BASE type.
     */
    static final int TOP = BASE | 0;

    /**
     * The BOOLEAN type. This is a BASE type mainly used for array types.
     */
    static final int BOOLEAN = BASE | 9;

    /**
     * The BYTE type. This is a BASE type mainly used for array types.
     */
    static final int BYTE = BASE | 10;

    /**
     * The CHAR type. This is a BASE type mainly used for array types.
     */
    static final int CHAR = BASE | 11;

    /**
     * The SHORT type. This is a BASE type mainly used for array types.
     */
    static final int SHORT = BASE | 12;

    /**
     * The INTEGER type. This is a BASE type.
     */
    static final int INTEGER = BASE | 1;

    /**
     * The FLOAT type. This is a BASE type.
     */
    static final int FLOAT = BASE | 2;

    /**
     * The DOUBLE type. This is a BASE type.
     */
    static final int DOUBLE = BASE | 3;

    /**
     * The LONG type. This is a BASE type.
     */
    static final int LONG = BASE | 4;

    /**
     * The NULL type. This is a BASE type.
     */
    static final int NULL = BASE | 5;

    /**
     * The UNINITIALIZED_THIS type. This is a BASE type.
     */
    static final int UNINITIALIZED_THIS = BASE | 6;

    /**
     * The stack size variation corresponding to each JVM instruction. This
     * stack variation is equal to the size of the values produced by an
     * instruction, minus the size of the values consumed by this instruction.
     */
    static final int[] SIZE;

    /**
     * Computes the stack size variation corresponding to each JVM instruction.
     */
    static {
        int i;
        int[] b = new int[202];
        String s = "EFFFFFFFFGGFFFGGFFFEEFGFGFEEEEEEEEEEEEEEEEEEEEDEDEDDDDD"
                + "CDCDEEEEEEEEEEEEEEEEEEEEBABABBBBDCFFFGGGEDCDCDCDCDCDCDCDCD"
                + "CDCEEEEDDDDDDDCDCDCEFEFDDEEFFDEDEEEBDDBBDDDDDDCCCCCCCCEFED"
                + "DDCDCDEEEEEEEEEEFEEEEEEDDEEDDEE";
        for (i = 0; i < b.length; ++i) {
            b[i] = s.charAt(i) - 'E';
        }
        SIZE = b;
    }

    /**
     * The label (i.e. basic block) to which these input and output stack map
     * frames correspond.
     */
    Label owner;

    /**
     * The input stack map frame locals.
     */
    int[] inputLocals;

    /**
     * The input stack map frame stack.
     */
    int[] inputStack;

    /**
     * The output stack map frame locals.
     */
    private int[] outputLocals;

    /**
     * The output stack map frame stack.
     */
    private int[] outputStack;

    /**
     * Relative size of the output stack. The exact semantics of this field
     * depends on the algorithm that is used.
     * 
     * When only the maximum stack size is computed, this field is the size of
     * the output stack relatively to the top of the input stack.
     * 
     * When the stack map frames are completely computed, this field is the
     * actual number of types in {@link #outputStack}.
     */
    private int outputStackTop;

    /**
     * Number of types that are initialized in the basic block.
     * 
     * @see #initializations
     */
    private int initializationCount;

    /**
     * The types that are initialized in the basic block. A constructor
     * invocation on an UNINITIALIZED or UNINITIALIZED_THIS type must replace
     * <i>every occurence</i> of this type in the local variables and in the
     * operand stack. This cannot be done during the first phase of the
     * algorithm since, during this phase, the local variables and the operand
     * stack are not completely computed. It is therefore necessary to store the
     * types on which constructors are invoked in the basic block, in order to
     * do this replacement during the second phase of the algorithm, where the
     * frames are fully computed. Note that this array can contain types that
     * are relative to input locals or to the input stack (see below for the
     * description of the algorithm).
     */
    private int[] initializations;

    /**
     * Returns the output frame local variable type at the given index.
     * 
     * @param local the index of the local that must be returned.
     * @return the output frame local variable type at the given index.
     */
    private int get(final int local) {
        if (outputLocals == null || local >= outputLocals.length) {
            // this local has never been assigned in this basic block,
            // so it is still equal to its value in the input frame
            return LOCAL | local;
        } else {
            int type = outputLocals[local];
            if (type == 0) {
                // this local has never been assigned in this basic block,
                // so it is still equal to its value in the input frame
                type = outputLocals[local] = LOCAL | local;
            }
            return type;
        }
    }

    /**
     * Sets the output frame local variable type at the given index.
     * 
     * @param local the index of the local that must be set.
     * @param type the value of the local that must be set.
     */
    private void set(final int local, final int type) {
        // creates and/or resizes the output local variables array if necessary
        if (outputLocals == null) {
            outputLocals = new int[10];
        }
        int n = outputLocals.length;
        if (local >= n) {
            int[] t = new int[Math.max(local + 1, 2 * n)];
            System.arraycopy(outputLocals, 0, t, 0, n);
            outputLocals = t;
        }
        // sets the local variable
        outputLocals[local] = type;
    }

    /**
     * Pushes a new type onto the output frame stack.
     * 
     * @param type the type that must be pushed.
     */
    private void push(final int type) {
        // creates and/or resizes the output stack array if necessary
        if (outputStack == null) {
            outputStack = new int[10];
        }
        int n = outputStack.length;
        if (outputStackTop >= n) {
            int[] t = new int[Math.max(outputStackTop + 1, 2 * n)];
            System.arraycopy(outputStack, 0, t, 0, n);
            outputStack = t;
        }
        // pushes the type on the output stack
        outputStack[outputStackTop++] = type;
        // updates the maximun height reached by the output stack, if needed
        int top = owner.inputStackTop + outputStackTop;
        if (top > owner.outputStackMax) {
            owner.outputStackMax = top;
        }
    }

    /**
     * Pushes a new type onto the output frame stack.
     * 
     * @param cw the ClassWriter to which this label belongs.
     * @param desc the descriptor of the type to be pushed. Can also be a method
     *        descriptor (in this case this method pushes its return type onto
     *        the output frame stack).
     */
    private void push(final ClassWriter cw, final String desc) {
        int type = type(cw, desc);
        if (type != 0) {
            push(type);
            if (type == LONG || type == DOUBLE) {
                push(TOP);
            }
        }
    }

    /**
     * Returns the int encoding of the given type.
     * 
     * @param cw the ClassWriter to which this label belongs.
     * @param desc a type descriptor.
     * @return the int encoding of the given type.
     */
    private static int type(final ClassWriter cw, final String desc) {
        String t;
        int index = desc.charAt(0) == '(' ? desc.indexOf(')') + 1 : 0;
        switch (desc.charAt(index)) {
            case 'V':
                return 0;
            case 'Z':
            case 'C':
            case 'B':
            case 'S':
            case 'I':
                return INTEGER;
            case 'F':
                return FLOAT;
            case 'J':
                return LONG;
            case 'D':
                return DOUBLE;
            case 'L':
                // stores the internal name, not the descriptor!
                t = desc.substring(index + 1, desc.length() - 1);
                return OBJECT | cw.addType(t);
                // case '[':
            default:
                // extracts the dimensions and the element type
                int data;
                int dims = index + 1;
                while (desc.charAt(dims) == '[') {
                    ++dims;
                }
                switch (desc.charAt(dims)) {
                    case 'Z':
                        data = BOOLEAN;
                        break;
                    case 'C':
                        data = CHAR;
                        break;
                    case 'B':
                        data = BYTE;
                        break;
                    case 'S':
                        data = SHORT;
                        break;
                    case 'I':
                        data = INTEGER;
                        break;
                    case 'F':
                        data = FLOAT;
                        break;
                    case 'J':
                        data = LONG;
                        break;
                    case 'D':
                        data = DOUBLE;
                        break;
                    // case 'L':
                    default:
                        // stores the internal name, not the descriptor
                        t = desc.substring(dims + 1, desc.length() - 1);
                        data = OBJECT | cw.addType(t);
                }
                return (dims - index) << 28 | data;
        }
    }

    /**
     * Pops a type from the output frame stack and returns its value.
     * 
     * @return the type that has been popped from the output frame stack.
     */
    private int pop() {
        if (outputStackTop > 0) {
            return outputStack[--outputStackTop];
        } else {
            // if the output frame stack is empty, pops from the input stack
            return STACK | -(--owner.inputStackTop);
        }
    }

    /**
     * Pops the given number of types from the output frame stack.
     * 
     * @param elements the number of types that must be popped.
     */
    private void pop(final int elements) {
        if (outputStackTop >= elements) {
            outputStackTop -= elements;
        } else {
            // if the number of elements to be popped is greater than the number
            // of elements in the output stack, clear it, and pops the remaining
            // elements from the input stack.
            owner.inputStackTop -= elements - outputStackTop;
            outputStackTop = 0;
        }
    }

    /**
     * Pops a type from the output frame stack.
     * 
     * @param desc the descriptor of the type to be popped. Can also be a method
     *        descriptor (in this case this method pops the types corresponding
     *        to the method arguments).
     */
    private void pop(final String desc) {
        char c = desc.charAt(0);
        if (c == '(') {
            pop((Type.getArgumentsAndReturnSizes(desc) >> 2) - 1);
        } else if (c == 'J' || c == 'D') {
            pop(2);
        } else {
            pop(1);
        }
    }

    /**
     * Adds a new type to the list of types on which a constructor is invoked in
     * the basic block.
     * 
     * @param var a type on a which a constructor is invoked.
     */
    private void init(final int var) {
        // creates and/or resizes the initializations array if necessary
        if (initializations == null) {
            initializations = new int[2];
        }
        int n = initializations.length;
        if (initializationCount >= n) {
            int[] t = new int[Math.max(initializationCount + 1, 2 * n)];
            System.arraycopy(initializations, 0, t, 0, n);
            initializations = t;
        }
        // stores the type to be initialized
        initializations[initializationCount++] = var;
    }

    /**
     * Replaces the given type with the appropriate type if it is one of the
     * types on which a constructor is invoked in the basic block.
     * 
     * @param cw the ClassWriter to which this label belongs.
     * @param t a type
     * @return t or, if t is one of the types on which a constructor is invoked
     *         in the basic block, the type corresponding to this constructor.
     */
    private int init(final ClassWriter cw, final int t) {
        int s;
        if (t == UNINITIALIZED_THIS) {
            s = OBJECT | cw.addType(cw.thisName);
        } else if ((t & (DIM | BASE_KIND)) == UNINITIALIZED) {
            String type = cw.typeTable[t & BASE_VALUE].strVal1;
            s = OBJECT | cw.addType(type);
        } else {
            return t;
        }
        for (int j = 0; j < initializationCount; ++j) {
            int u = initializations[j];
            int dim = u & DIM;
            int kind = u & KIND;
            if (kind == LOCAL) {
                u = dim + inputLocals[u & VALUE];
            } else if (kind == STACK) {
                u = dim + inputStack[inputStack.length - (u & VALUE)];
            }
            if (t == u) {
                return s;
            }
        }
        return t;
    }

    /**
     * Initializes the input frame of the first basic block from the method
     * descriptor.
     * 
     * @param cw the ClassWriter to which this label belongs.
     * @param access the access flags of the method to which this label belongs.
     * @param args the formal parameter types of this method.
     * @param maxLocals the maximum number of local variables of this method.
     */
    void initInputFrame(
        final ClassWriter cw,
        final int access,
        final Type[] args,
        final int maxLocals)
    {
        inputLocals = new int[maxLocals];
        inputStack = new int[0];
        int i = 0;
        if ((access & Opcodes.ACC_STATIC) == 0) {
            if ((access & MethodWriter.ACC_CONSTRUCTOR) == 0) {
                inputLocals[i++] = OBJECT | cw.addType(cw.thisName);
            } else {
                inputLocals[i++] = UNINITIALIZED_THIS;
            }
        }
        for (int j = 0; j < args.length; ++j) {
            int t = type(cw, args[j].getDescriptor());
            inputLocals[i++] = t;
            if (t == LONG || t == DOUBLE) {
                inputLocals[i++] = TOP;
            }
        }
        while (i < maxLocals) {
            inputLocals[i++] = TOP;
        }
    }

    /**
     * Simulates the action of the given instruction on the output stack frame.
     * 
     * @param opcode the opcode of the instruction.
     * @param arg the operand of the instruction, if any.
     * @param cw the class writer to which this label belongs.
     * @param item the operand of the instructions, if any.
     */
    void execute(
        final int opcode,
        final int arg,
        final ClassWriter cw,
        final Item item)
    {
        int t1, t2, t3, t4;
        switch (opcode) {
            case Opcodes.NOP:
            case Opcodes.INEG:
            case Opcodes.LNEG:
            case Opcodes.FNEG:
            case Opcodes.DNEG:
            case Opcodes.I2B:
            case Opcodes.I2C:
            case Opcodes.I2S:
            case Opcodes.GOTO:
            case Opcodes.RETURN:
                break;
            case Opcodes.ACONST_NULL:
                push(NULL);
                break;
            case Opcodes.ICONST_M1:
            case Opcodes.ICONST_0:
            case Opcodes.ICONST_1:
            case Opcodes.ICONST_2:
            case Opcodes.ICONST_3:
            case Opcodes.ICONST_4:
            case Opcodes.ICONST_5:
            case Opcodes.BIPUSH:
            case Opcodes.SIPUSH:
            case Opcodes.ILOAD:
                push(INTEGER);
                break;
            case Opcodes.LCONST_0:
            case Opcodes.LCONST_1:
            case Opcodes.LLOAD:
                push(LONG);
                push(TOP);
                break;
            case Opcodes.FCONST_0:
            case Opcodes.FCONST_1:
            case Opcodes.FCONST_2:
            case Opcodes.FLOAD:
                push(FLOAT);
                break;
            case Opcodes.DCONST_0:
            case Opcodes.DCONST_1:
            case Opcodes.DLOAD:
                push(DOUBLE);
                push(TOP);
                break;
            case Opcodes.LDC:
                switch (item.type) {
                    case ClassWriter.INT:
                        push(INTEGER);
                        break;
                    case ClassWriter.LONG:
                        push(LONG);
                        push(TOP);
                        break;
                    case ClassWriter.FLOAT:
                        push(FLOAT);
                        break;
                    case ClassWriter.DOUBLE:
                        push(DOUBLE);
                        push(TOP);
                        break;
                    case ClassWriter.CLASS:
                        push(OBJECT | cw.addType("java/lang/Class"));
                        break;
                    // case ClassWriter.STR:
                    default:
                        push(OBJECT | cw.addType("java/lang/String"));
                }
                break;
            case Opcodes.ALOAD:
                push(get(arg));
                break;
            case Opcodes.IALOAD:
            case Opcodes.BALOAD:
            case Opcodes.CALOAD:
            case Opcodes.SALOAD:
                pop(2);
                push(INTEGER);
                break;
            case Opcodes.LALOAD:
            case Opcodes.D2L:
                pop(2);
                push(LONG);
                push(TOP);
                break;
            case Opcodes.FALOAD:
                pop(2);
                push(FLOAT);
                break;
            case Opcodes.DALOAD:
            case Opcodes.L2D:
                pop(2);
                push(DOUBLE);
                push(TOP);
                break;
            case Opcodes.AALOAD:
                pop(1);
                t1 = pop();
                push(ELEMENT_OF + t1);
                break;
            case Opcodes.ISTORE:
            case Opcodes.FSTORE:
            case Opcodes.ASTORE:
                t1 = pop();
                set(arg, t1);
                if (arg > 0) {
                    t2 = get(arg - 1);
                    // if t2 is of kind STACK or LOCAL we cannot know its size!
                    if (t2 == LONG || t2 == DOUBLE) {
                        set(arg - 1, TOP);
                    } else if ((t2 & KIND) != BASE) {
                        set(arg - 1, t2 | TOP_IF_LONG_OR_DOUBLE);
                    }
                }
                break;
            case Opcodes.LSTORE:
            case Opcodes.DSTORE:
                pop(1);
                t1 = pop();
                set(arg, t1);
                set(arg + 1, TOP);
                if (arg > 0) {
                    t2 = get(arg - 1);
                    // if t2 is of kind STACK or LOCAL we cannot know its size!
                    if (t2 == LONG || t2 == DOUBLE) {
                        set(arg - 1, TOP);
                    } else if ((t2 & KIND) != BASE) {
                        set(arg - 1, t2 | TOP_IF_LONG_OR_DOUBLE);
                    }
                }
                break;
            case Opcodes.IASTORE:
            case Opcodes.BASTORE:
            case Opcodes.CASTORE:
            case Opcodes.SASTORE:
            case Opcodes.FASTORE:
            case Opcodes.AASTORE:
                pop(3);
                break;
            case Opcodes.LASTORE:
            case Opcodes.DASTORE:
                pop(4);
                break;
            case Opcodes.POP:
            case Opcodes.IFEQ:
            case Opcodes.IFNE:
            case Opcodes.IFLT:
            case Opcodes.IFGE:
            case Opcodes.IFGT:
            case Opcodes.IFLE:
            case Opcodes.IRETURN:
            case Opcodes.FRETURN:
            case Opcodes.ARETURN:
            case Opcodes.TABLESWITCH:
            case Opcodes.LOOKUPSWITCH:
            case Opcodes.ATHROW:
            case Opcodes.MONITORENTER:
            case Opcodes.MONITOREXIT:
            case Opcodes.IFNULL:
            case Opcodes.IFNONNULL:
                pop(1);
                break;
            case Opcodes.POP2:
            case Opcodes.IF_ICMPEQ:
            case Opcodes.IF_ICMPNE:
            case Opcodes.IF_ICMPLT:
            case Opcodes.IF_ICMPGE:
            case Opcodes.IF_ICMPGT:
            case Opcodes.IF_ICMPLE:
            case Opcodes.IF_ACMPEQ:
            case Opcodes.IF_ACMPNE:
            case Opcodes.LRETURN:
            case Opcodes.DRETURN:
                pop(2);
                break;
            case Opcodes.DUP:
                t1 = pop();
                push(t1);
                push(t1);
                break;
            case Opcodes.DUP_X1:
                t1 = pop();
                t2 = pop();
                push(t1);
                push(t2);
                push(t1);
                break;
            case Opcodes.DUP_X2:
                t1 = pop();
                t2 = pop();
                t3 = pop();
                push(t1);
                push(t3);
                push(t2);
                push(t1);
                break;
            case Opcodes.DUP2:
                t1 = pop();
                t2 = pop();
                push(t2);
                push(t1);
                push(t2);
                push(t1);
                break;
            case Opcodes.DUP2_X1:
                t1 = pop();
                t2 = pop();
                t3 = pop();
                push(t2);
                push(t1);
                push(t3);
                push(t2);
                push(t1);
                break;
            case Opcodes.DUP2_X2:
                t1 = pop();
                t2 = pop();
                t3 = pop();
                t4 = pop();
                push(t2);
                push(t1);
                push(t4);
                push(t3);
                push(t2);
                push(t1);
                break;
            case Opcodes.SWAP:
                t1 = pop();
                t2 = pop();
                push(t1);
                push(t2);
                break;
            case Opcodes.IADD:
            case Opcodes.ISUB:
            case Opcodes.IMUL:
            case Opcodes.IDIV:
            case Opcodes.IREM:
            case Opcodes.IAND:
            case Opcodes.IOR:
            case Opcodes.IXOR:
            case Opcodes.ISHL:
            case Opcodes.ISHR:
            case Opcodes.IUSHR:
            case Opcodes.L2I:
            case Opcodes.D2I:
            case Opcodes.FCMPL:
            case Opcodes.FCMPG:
                pop(2);
                push(INTEGER);
                break;
            case Opcodes.LADD:
            case Opcodes.LSUB:
            case Opcodes.LMUL:
            case Opcodes.LDIV:
            case Opcodes.LREM:
            case Opcodes.LAND:
            case Opcodes.LOR:
            case Opcodes.LXOR:
                pop(4);
                push(LONG);
                push(TOP);
                break;
            case Opcodes.FADD:
            case Opcodes.FSUB:
            case Opcodes.FMUL:
            case Opcodes.FDIV:
            case Opcodes.FREM:
            case Opcodes.L2F:
            case Opcodes.D2F:
                pop(2);
                push(FLOAT);
                break;
            case Opcodes.DADD:
            case Opcodes.DSUB:
            case Opcodes.DMUL:
            case Opcodes.DDIV:
            case Opcodes.DREM:
                pop(4);
                push(DOUBLE);
                push(TOP);
                break;
            case Opcodes.LSHL:
            case Opcodes.LSHR:
            case Opcodes.LUSHR:
                pop(3);
                push(LONG);
                push(TOP);
                break;
            case Opcodes.IINC:
                set(arg, INTEGER);
                break;
            case Opcodes.I2L:
            case Opcodes.F2L:
                pop(1);
                push(LONG);
                push(TOP);
                break;
            case Opcodes.I2F:
                pop(1);
                push(FLOAT);
                break;
            case Opcodes.I2D:
            case Opcodes.F2D:
                pop(1);
                push(DOUBLE);
                push(TOP);
                break;
            case Opcodes.F2I:
            case Opcodes.ARRAYLENGTH:
            case Opcodes.INSTANCEOF:
                pop(1);
                push(INTEGER);
                break;
            case Opcodes.LCMP:
            case Opcodes.DCMPL:
            case Opcodes.DCMPG:
                pop(4);
                push(INTEGER);
                break;
            case Opcodes.JSR:
            case Opcodes.RET:
                throw new RuntimeException("JSR/RET are not supported with computeFrames option");
            case Opcodes.GETSTATIC:
                push(cw, item.strVal3);
                break;
            case Opcodes.PUTSTATIC:
                pop(item.strVal3);
                break;
            case Opcodes.GETFIELD:
                pop(1);
                push(cw, item.strVal3);
                break;
            case Opcodes.PUTFIELD:
                pop(item.strVal3);
                pop();
                break;
            case Opcodes.INVOKEVIRTUAL:
            case Opcodes.INVOKESPECIAL:
            case Opcodes.INVOKESTATIC:
            case Opcodes.INVOKEINTERFACE:
                pop(item.strVal3);
                if (opcode != Opcodes.INVOKESTATIC) {
                    t1 = pop();
                    if (opcode == Opcodes.INVOKESPECIAL
                            && item.strVal2.charAt(0) == '<')
                    {
                        init(t1);
                    }
                }
                push(cw, item.strVal3);
                break;
            case Opcodes.INVOKEDYNAMIC:
                pop(item.strVal2);
                push(cw, item.strVal2);
                break;
            case Opcodes.NEW:
                push(UNINITIALIZED | cw.addUninitializedType(item.strVal1, arg));
                break;
            case Opcodes.NEWARRAY:
                pop();
                switch (arg) {
                    case Opcodes.T_BOOLEAN:
                        push(ARRAY_OF | BOOLEAN);
                        break;
                    case Opcodes.T_CHAR:
                        push(ARRAY_OF | CHAR);
                        break;
                    case Opcodes.T_BYTE:
                        push(ARRAY_OF | BYTE);
                        break;
                    case Opcodes.T_SHORT:
                        push(ARRAY_OF | SHORT);
                        break;
                    case Opcodes.T_INT:
                        push(ARRAY_OF | INTEGER);
                        break;
                    case Opcodes.T_FLOAT:
                        push(ARRAY_OF | FLOAT);
                        break;
                    case Opcodes.T_DOUBLE:
                        push(ARRAY_OF | DOUBLE);
                        break;
                    // case Opcodes.T_LONG:
                    default:
                        push(ARRAY_OF | LONG);
                        break;
                }
                break;
            case Opcodes.ANEWARRAY:
                String s = item.strVal1;
                pop();
                if (s.charAt(0) == '[') {
                    push(cw, '[' + s);
                } else {
                    push(ARRAY_OF | OBJECT | cw.addType(s));
                }
                break;
            case Opcodes.CHECKCAST:
                s = item.strVal1;
                pop();
                if (s.charAt(0) == '[') {
                    push(cw, s);
                } else {
                    push(OBJECT | cw.addType(s));
                }
                break;
            // case Opcodes.MULTIANEWARRAY:
            default:
                pop(arg);
                push(cw, item.strVal1);
                break;
        }
    }

    /**
     * Merges the input frame of the given basic block with the input and output
     * frames of this basic block. Returns <tt>true</tt> if the input frame of
     * the given label has been changed by this operation.
     * 
     * @param cw the ClassWriter to which this label belongs.
     * @param frame the basic block whose input frame must be updated.
     * @param edge the kind of the {@link Edge} between this label and 'label'.
     *        See {@link Edge#info}.
     * @return <tt>true</tt> if the input frame of the given label has been
     *         changed by this operation.
     */
    boolean merge(final ClassWriter cw, final Frame frame, final int edge) {
        boolean changed = false;
        int i, s, dim, kind, t;

        int nLocal = inputLocals.length;
        int nStack = inputStack.length;
        if (frame.inputLocals == null) {
            frame.inputLocals = new int[nLocal];
            changed = true;
        }

        for (i = 0; i < nLocal; ++i) {
            if (outputLocals != null && i < outputLocals.length) {
                s = outputLocals[i];
                if (s == 0) {
                    t = inputLocals[i];
                } else {
                    dim = s & DIM;
                    kind = s & KIND;
                    if (kind == BASE) {
                        t = s;
                    } else {
                        if (kind == LOCAL) {
                            t = dim + inputLocals[s & VALUE];
                        } else {
                            t = dim + inputStack[nStack - (s & VALUE)];
                        }
                        if ((s & TOP_IF_LONG_OR_DOUBLE) != 0 && (t == LONG || t == DOUBLE)) {
                            t = TOP;
                        }
                    }
                }
            } else {
                t = inputLocals[i];
            }
            if (initializations != null) {
                t = init(cw, t);
            }
            changed |= merge(cw, t, frame.inputLocals, i);
        }

        if (edge > 0) {
            for (i = 0; i < nLocal; ++i) {
                t = inputLocals[i];
                changed |= merge(cw, t, frame.inputLocals, i);
            }
            if (frame.inputStack == null) {
                frame.inputStack = new int[1];
                changed = true;
            }
            changed |= merge(cw, edge, frame.inputStack, 0);
            return changed;
        }

        int nInputStack = inputStack.length + owner.inputStackTop;
        if (frame.inputStack == null) {
            frame.inputStack = new int[nInputStack + outputStackTop];
            changed = true;
        }

        for (i = 0; i < nInputStack; ++i) {
            t = inputStack[i];
            if (initializations != null) {
                t = init(cw, t);
            }
            changed |= merge(cw, t, frame.inputStack, i);
        }
        for (i = 0; i < outputStackTop; ++i) {
            s = outputStack[i];
            dim = s & DIM;
            kind = s & KIND;
            if (kind == BASE) {
                t = s;
            } else {
                if (kind == LOCAL) {
                    t = dim + inputLocals[s & VALUE];
                } else {
                    t = dim + inputStack[nStack - (s & VALUE)];
                }
                if ((s & TOP_IF_LONG_OR_DOUBLE) != 0 && (t == LONG || t == DOUBLE)) {
                    t = TOP;
                }
            }
            if (initializations != null) {
                t = init(cw, t);
            }
            changed |= merge(cw, t, frame.inputStack, nInputStack + i);
        }
        return changed;
    }

    /**
     * Merges the type at the given index in the given type array with the given
     * type. Returns <tt>true</tt> if the type array has been modified by this
     * operation.
     * 
     * @param cw the ClassWriter to which this label belongs.
     * @param t the type with which the type array element must be merged.
     * @param types an array of types.
     * @param index the index of the type that must be merged in 'types'.
     * @return <tt>true</tt> if the type array has been modified by this
     *         operation.
     */
    private static boolean merge(
        final ClassWriter cw,
        int t,
        final int[] types,
        final int index)
    {
        int u = types[index];
        if (u == t) {
            // if the types are equal, merge(u,t)=u, so there is no change
            return false;
        }
        if ((t & ~DIM) == NULL) {
            if (u == NULL) {
                return false;
            }
            t = NULL;
        }
        if (u == 0) {
            // if types[index] has never been assigned, merge(u,t)=t
            types[index] = t;
            return true;
        }
        int v;
        if ((u & BASE_KIND) == OBJECT || (u & DIM) != 0) {
            // if u is a reference type of any dimension
            if (t == NULL) {
                // if t is the NULL type, merge(u,t)=u, so there is no change
                return false;
            } else if ((t & (DIM | BASE_KIND)) == (u & (DIM | BASE_KIND))) {
                if ((u & BASE_KIND) == OBJECT) {
                    // if t is also a reference type, and if u and t have the
                    // same dimension merge(u,t) = dim(t) | common parent of the
                    // element types of u and t
                    v = (t & DIM) | OBJECT
                            | cw.getMergedType(t & BASE_VALUE, u & BASE_VALUE);
                } else {
                    // if u and t are array types, but not with the same element
                    // type, merge(u,t)=java/lang/Object
                    v = OBJECT | cw.addType("java/lang/Object");
                }
            } else if ((t & BASE_KIND) == OBJECT || (t & DIM) != 0) {
                // if t is any other reference or array type,
                // merge(u,t)=java/lang/Object
                v = OBJECT | cw.addType("java/lang/Object");
            } else {
                // if t is any other type, merge(u,t)=TOP
                v = TOP;
            }
        } else if (u == NULL) {
            // if u is the NULL type, merge(u,t)=t,
            // or TOP if t is not a reference type
            v = (t & BASE_KIND) == OBJECT || (t & DIM) != 0 ? t : TOP;
        } else {
            // if u is any other type, merge(u,t)=TOP whatever t
            v = TOP;
        }
        if (u != v) {
            types[index] = v;
            return true;
        }
        return false;
    }
}
