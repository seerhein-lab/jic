package de.htwg_konstanz.in.jca;

import java.util.Stack;

import org.apache.bcel.Repository;
import org.apache.bcel.classfile.ConstantPool;
import org.apache.bcel.classfile.JavaClass;
import org.apache.bcel.classfile.Method;
import org.apache.bcel.generic.ACONST_NULL;
import org.apache.bcel.generic.ANEWARRAY;
import org.apache.bcel.generic.ARRAYLENGTH;
import org.apache.bcel.generic.ATHROW;
import org.apache.bcel.generic.ArithmeticInstruction;
import org.apache.bcel.generic.ArrayInstruction;
import org.apache.bcel.generic.BIPUSH;
import org.apache.bcel.generic.BREAKPOINT;
import org.apache.bcel.generic.BasicType;
import org.apache.bcel.generic.BranchInstruction;
import org.apache.bcel.generic.CHECKCAST;
import org.apache.bcel.generic.ConstantPoolGen;
import org.apache.bcel.generic.ConversionInstruction;
import org.apache.bcel.generic.DCMPG;
import org.apache.bcel.generic.DCMPL;
import org.apache.bcel.generic.DCONST;
import org.apache.bcel.generic.DUP;
import org.apache.bcel.generic.DUP2;
import org.apache.bcel.generic.DUP2_X1;
import org.apache.bcel.generic.DUP2_X2;
import org.apache.bcel.generic.DUP_X1;
import org.apache.bcel.generic.DUP_X2;
import org.apache.bcel.generic.EmptyVisitor;
import org.apache.bcel.generic.FCMPG;
import org.apache.bcel.generic.FCMPL;
import org.apache.bcel.generic.FCONST;
import org.apache.bcel.generic.GETFIELD;
import org.apache.bcel.generic.GETSTATIC;
import org.apache.bcel.generic.ICONST;
import org.apache.bcel.generic.IINC;
import org.apache.bcel.generic.IMPDEP1;
import org.apache.bcel.generic.IMPDEP2;
import org.apache.bcel.generic.INSTANCEOF;
import org.apache.bcel.generic.INVOKEINTERFACE;
import org.apache.bcel.generic.INVOKESPECIAL;
import org.apache.bcel.generic.INVOKESTATIC;
import org.apache.bcel.generic.INVOKEVIRTUAL;
import org.apache.bcel.generic.LCMP;
import org.apache.bcel.generic.LCONST;
import org.apache.bcel.generic.LDC;
import org.apache.bcel.generic.LDC2_W;
import org.apache.bcel.generic.LoadInstruction;
import org.apache.bcel.generic.MONITORENTER;
import org.apache.bcel.generic.MONITOREXIT;
import org.apache.bcel.generic.MULTIANEWARRAY;
import org.apache.bcel.generic.NEW;
import org.apache.bcel.generic.NEWARRAY;
import org.apache.bcel.generic.NOP;
import org.apache.bcel.generic.POP;
import org.apache.bcel.generic.POP2;
import org.apache.bcel.generic.PUTFIELD;
import org.apache.bcel.generic.PUTSTATIC;
import org.apache.bcel.generic.RET;
import org.apache.bcel.generic.ReturnInstruction;
import org.apache.bcel.generic.SIPUSH;
import org.apache.bcel.generic.SWAP;
import org.apache.bcel.generic.StoreInstruction;
import org.apache.bcel.generic.Type;

public class CtorAnalysisVisitor extends EmptyVisitor {
	private final LocalVars localVars;
	private final Stack<Entry> stack;
	private final ConstantPoolGen constantPoolGen;

	private volatile ThreeValueBoolean doesEscape = ThreeValueBoolean.no;
	private volatile Entry result = null;

	CtorAnalysisVisitor(LocalVars localVars, Stack<Entry> stack,
			ConstantPool constantPool) {
		this.localVars = localVars;
		this.stack = stack;
		this.constantPoolGen = new ConstantPoolGen(constantPool);
	}

	public ThreeValueBoolean doesEscape() {
		return doesEscape;
	}

	public Entry getResult() {
		return result;
	}

	private void notImplementedYet(Object instruction) {
		System.out.println(instruction.toString());
		System.out.println("NOT IMPLEMENTED YET");
		doesEscape = ThreeValueBoolean.unknown;
		System.out.println();
	}

	// ******************************************************************//
	// Visit section //
	// For more details on bytecode instructions see: //
	// http://en.wikipedia.org/wiki/Java_bytecode_instruction_listings //
	// For details on enumeration see: //
	// https://docs.google.com/open?id=0B4RYegfkX-vPUnlRUm56S1YtMG8 //
	// ******************************************************************//

	// -----------------------------------------------------------------
	/**
	 * 1. ACONST_NULL:
	 * <p>
	 * Pushes a null reference onto the stack
	 * <p>
	 * Stack: → null
	 */
	@Override
	public void visitACONST_NULL(ACONST_NULL obj) {
		notImplementedYet(obj);
	}

	// -----------------------------------------------------------------
	/**
	 * 2. ArithmeticInstruction
	 * <p>
	 * ToDo: Add description
	 * <p>
	 * ToDo: Add Stack
	 */
	@Override
	public void visitArithmeticInstruction(ArithmeticInstruction obj) {
		notImplementedYet(obj);
	}

	// -----------------------------------------------------------------

	/**
	 * 3. ArrayInstruction
	 * <p>
	 * ToDo: Add description
	 * <p>
	 * ToDo: Add Stack
	 */
	@Override
	public void visitArrayInstruction(ArrayInstruction obj) {
		notImplementedYet(obj);
	}

	// -----------------------------------------------------------------
	/**
	 * 4. ARRAYLENGTH
	 * <p>
	 * Gets the length of an array.
	 * <p>
	 * Stack: arrayref → length
	 */
	@Override
	public void visitARRAYLENGTH(ARRAYLENGTH obj) {
		notImplementedYet(obj);
	}

	// -----------------------------------------------------------------
	/**
	 * 5. ATHROW
	 * <p>
	 * Throws an error or exception (notice that the rest of the stack is
	 * cleared, leaving only a reference to the Throwable).
	 * <p>
	 * Stack: objectref → [empty], objectref
	 */
	@Override
	public void visitATHROW(ATHROW obj) {
		notImplementedYet(obj);
	}

	// -----------------------------------------------------------------
	/**
	 * 5. BIPUSH
	 * <p>
	 * Pushes a byte onto the stack as an integer value.
	 * <p>
	 * Stack: → value
	 * <p>
	 * DoesEscape: no, only 32 bit operation.
	 */
	@Override
	public void visitBIPUSH(BIPUSH obj) {
		System.out.println(obj.toString(false));
		stack.push(Entry.someInt);
		doesEscape = ThreeValueBoolean.no;
	}

	// -----------------------------------------------------------------
	/**
	 * 6. BranchInstruction
	 * <p>
	 * ToDo: add description
	 * <p>
	 * ToDo: add Stack
	 */
	@Override
	public void visitBranchInstruction(BranchInstruction obj) {
		notImplementedYet(obj);
	}

	// -----------------------------------------------------------------
	/**
	 * 8. BREAKPOINT
	 * <p>
	 * Reserved for breakpoints in Java debuggers; should not appear in any
	 * class file.
	 */
	@Override
	public void visitBREAKPOINT(BREAKPOINT obj) {
		notImplementedYet(obj);
	}

	// -----------------------------------------------------------------
	/**
	 * 9. ConversionInstruction
	 * <p>
	 * ToDo: add description
	 * <p>
	 * ToDo: add stack
	 */
	@Override
	public void visitConversionInstruction(ConversionInstruction obj) {
		notImplementedYet(obj);
	}

	// ---CPInstruction-------------------------------------------------
	/**
	 * 10. ConversionInstruction <br>
	 * 10.1. ANEWARRAY
	 * <p>
	 * Creates a new array of references of length count and component type
	 * identified by the class reference index (indexbyte1 << 8 + indexbyte2) in
	 * the constant pool.
	 * <p>
	 * Stack: count → arrayref <br>
	 * Note: 2 other bytes (indexbyte1, indexbyte2)
	 */
	@Override
	public void visitANEWARRAY(ANEWARRAY obj) {
		notImplementedYet(obj);
	}

	/**
	 * 10. ConversionInstruction <br>
	 * 10.2. CHECKCAST
	 * <p>
	 * Checks whether an objectref is of a certain type, the class reference of
	 * which is in the constant pool at index (indexbyte1 << 8 + indexbyte2).
	 * <p>
	 * Stack: objectref → objectref <br>
	 * Note: 2 other bytes (indexbyte1, indexbyte2)
	 */
	@Override
	public void visitCHECKCAST(CHECKCAST obj) {
		notImplementedYet(obj);
	}

	/**
	 * 10. ConversionInstruction <br>
	 * 10.3. FieldOrMethod <br>
	 * 10.3.2. GETFIELD
	 * <p>
	 * Gets a field value of an object objectref, where the field is identified
	 * by field reference in the constant pool index (index1 << 8 + index2).
	 * <p>
	 * Stack: objectref → value <br>
	 * Note: 2 other bytes (index1, index2)
	 */
	@Override
	public void visitGETFIELD(GETFIELD obj) {
		notImplementedYet(obj);
	}

	/**
	 * 10. ConversionInstruction <br>
	 * 10.3. FieldOrMethod <br>
	 * 10.3.3. GETSTATIC
	 * <p>
	 * Gets a static field value of a class, where the field is identified by
	 * field reference in the constant pool index (index1 << 8 + index2).
	 * <p>
	 * Stack: → value <br>
	 * Note: 2 other bytes (index1, index2)
	 */
	@Override
	public void visitGETSTATIC(GETSTATIC obj) {
		notImplementedYet(obj);
	}

	/**
	 * 10. ConversionInstruction <br>
	 * 10.3. FieldOrMethod <br>
	 * 10.3.4. PUTFIELD
	 * <p>
	 * Sets field to value in an object objectref, where the field is identified
	 * by a field reference index in constant pool (indexbyte1 << 8 +
	 * indexbyte2).
	 * <p>
	 * Stack: objectref, value → <br>
	 * Note: 2 other bytes (indexbyte1, indexbyte2)
	 */
	@Override
	public void visitPUTFIELD(PUTFIELD obj) {
		System.out.print(obj.toString(false) + ": ");

		// right side of assignment
		Entry right = stack.pop();

		doesEscape = ThreeValueBoolean.fromBoolean(right
				.equals(Entry.thisReference));

		if (right.equals(Entry.thisReference))
			System.out
					.println("'this' reference is assigned to some object's field --> ESCAPE!");

		// pop left side of assignment off the stack, too
		Entry left = stack.pop();
		System.out.println(left + "." + obj.getFieldName(constantPoolGen)
				+ " <--" + right);

	}

	/**
	 * 10. ConversionInstruction <br>
	 * 10.3. FieldOrMethod <br>
	 * 10.3.4. PUTSTATIC
	 * <p>
	 * Sets static field to value in a class, where the field is identified by a
	 * field reference index in constant pool (indexbyte1 << 8 + indexbyte2).
	 * <p>
	 * Stack: value → <br>
	 * Note: 2 other bytes (indexbyte1, indexbyte2)
	 */
	@Override
	public void visitPUTSTATIC(PUTSTATIC obj) {
		notImplementedYet(obj);
	}

	/**
	 * 10. ConversionInstruction <br>
	 * 10.4. InvokeInstruction <br>
	 * 10.4.1. INVOKEINTERFACE
	 * <p>
	 * Invokes an interface method on object objectref, where the interface
	 * method is identified by method reference index in constant pool
	 * (indexbyte1 << 8 + indexbyte2).
	 * <p>
	 * Stack: objectref, [arg1, arg2, ...] → <br>
	 * Note: 4 other bytes (indexbyte1, indexbyte2, count, 0)
	 */
	@Override
	public void visitINVOKEINTERFACE(INVOKEINTERFACE obj) {
		notImplementedYet(obj);
	}

	/**
	 * 10. ConversionInstruction <br>
	 * 10.4. InvokeInstruction <br>
	 * 10.4.2. INVOKESPECIAL
	 * <p>
	 * Invokes instance method on object objectref, where the method is
	 * identified by method reference index in constant pool (indexbyte1 << 8 +
	 * indexbyte2).
	 * <p>
	 * Stack: objectref, [arg1, arg2, ...] → <br>
	 * Note: 2 other bytes (indexbyte1, indexbyte2)
	 */
	@Override
	public void visitINVOKESPECIAL(INVOKESPECIAL obj) {
		System.out.println("INVOKESPECIAL: "
				+ obj.getReturnType(constantPoolGen) + " "
				+ obj.getReferenceType(constantPoolGen) + "."
				+ obj.getMethodName(constantPoolGen));

		JavaClass superClazz = null;

		try {
			superClazz = Repository.lookupClass(obj.getReferenceType(
					constantPoolGen).toString());
		} catch (ClassNotFoundException e) {
			throw new RuntimeException(e);
		}

		ClassAnalyzer superclassAnalyzer = new ClassAnalyzer(superClazz);
		Method superCtor = superclassAnalyzer.getConstructor(obj
				.getArgumentTypes(constantPoolGen));

		CtorAnalyzer superCtorAnalyzer = new CtorAnalyzer(superCtor);
		doesEscape = superCtorAnalyzer.doesThisReferenceEscape(stack);

		if (!obj.getReturnType(constantPoolGen).equals(BasicType.VOID)) {
			stack.push(superCtorAnalyzer.getResult());
		}

	}

	/**
	 * 10. ConversionInstruction <br>
	 * 10.4. InvokeInstruction <br>
	 * 10.4.3. INVOKESTATIC
	 * <p>
	 * Invokes a static method, where the method is identified by method
	 * reference index in constant pool (indexbyte1 << 8 + indexbyte2).
	 * <p>
	 * Stack: [arg1, arg2, ...] → <br>
	 * Note: 2 other bytes (indexbyte1, indexbyte2)
	 */
	@Override
	public void visitINVOKESTATIC(INVOKESTATIC obj) {
		notImplementedYet(obj);
	}

	/**
	 * 10. ConversionInstruction <br>
	 * 10.4. InvokeInstruction <br>
	 * 10.4.4. INVOKEVIRTUAL
	 * <p>
	 * Invokes virtual method on object objectref, where the method is
	 * identified by method reference index in constant pool (indexbyte1 << 8 +
	 * indexbyte2).
	 * <p>
	 * Stack: objectref, [arg1, arg2, ...] → <br>
	 * Note: 2 other bytes (indexbyte1, indexbyte2)
	 */
	@Override
	public void visitINVOKEVIRTUAL(INVOKEVIRTUAL obj) {
		notImplementedYet(obj);
	}

	/**
	 * 10. ConversionInstruction <br>
	 * 10.5. INSTANCEOF
	 * <p>
	 * Determines if an object objectref is of a given type, identified by class
	 * reference index in constant pool (indexbyte1 << 8 + indexbyte2).
	 * <p>
	 * Stack: objectref → result <br>
	 * Note: 2 other bytes (indexbyte1, indexbyte2)
	 */
	@Override
	public void visitINSTANCEOF(INSTANCEOF obj) {
		notImplementedYet(obj);
	}

	/**
	 * 10. ConversionInstruction <br>
	 * 10.6. LDC
	 * <p>
	 * Pushes a constant #index from a constant pool (String, int or float) onto
	 * the stack.
	 * <p>
	 * Stack: → value <br>
	 * Note: 1 other byte (index)
	 */
	@Override
	public void visitLDC(LDC obj) {
		System.out.println(obj.toString(false));
		doesEscape = ThreeValueBoolean.no;

		if (obj.getType(constantPoolGen).equals(Type.INT)) {
			stack.push(Entry.someInt);
			return;
		}

		if (obj.getType(constantPoolGen).equals(Type.FLOAT)) {
			stack.push(Entry.someFloat);
			return;
		}

		stack.push(Entry.someReference);
	}

	/**
	 * 10. ConversionInstruction <br>
	 * 10.7. LDC2_W
	 * <p>
	 * Pushes a constant #index from a constant pool (double or long) onto the
	 * stack (wide index is constructed as indexbyte1 << 8 + indexbyte2).
	 * <p>
	 * Stack: → value <br>
	 * Note: 2 other bytes (indexbyte1, indexbyte2)
	 * <p>
	 * DoesEscape: No, but why?
	 */
	@Override
	public void visitLDC2_W(LDC2_W obj) {
		System.out.println(obj.toString(false));
		doesEscape = ThreeValueBoolean.no;

		if (obj.getType(constantPoolGen).equals(Type.LONG)) {
			stack.push(Entry.someLong);
			return;
		}

		if (obj.getType(constantPoolGen).equals(Type.DOUBLE)) {
			stack.push(Entry.someDouble);
			return;
		}

		throw new AssertionError("LDC2_W loads wrongly typed value");

	}

	/**
	 * 10. ConversionInstruction <br>
	 * 10.8. MULTIANEWARRAY
	 * <p>
	 * Creates a new array of dimensions dimensions with elements of type
	 * identified by class reference in constant pool index (indexbyte1 << 8 +
	 * indexbyte2); the sizes of each dimension is identified by count1,
	 * [count2, etc.].
	 * <p>
	 * Stack: count1, [count2,...] → arrayref <br>
	 * Note: 3 other bytes (indexbyte1, indexbyte2, dimensions)
	 */
	@Override
	public void visitMULTIANEWARRAY(MULTIANEWARRAY obj) {
		notImplementedYet(obj);
	}

	/**
	 * 10. ConversionInstruction <br>
	 * 10.9. NEW
	 * <p>
	 * Creates new object of type identified by class reference in constant pool
	 * index (indexbyte1 << 8 + indexbyte2).
	 * <p>
	 * Stack: → objectref <br>
	 * Note: 2 other bytes (indexbyte1, indexbyte2)
	 * <p>
	 * DoesEscape: NO, new object with 32 bit reference.
	 */
	@Override
	public void visitNEW(NEW obj) {
		System.out.println(obj.toString(false));
		stack.push(Entry.someReference);
		doesEscape = ThreeValueBoolean.no;
	}

	// -----------------------------------------------------------------
	/**
	 * 11. DCMPG
	 * <p>
	 * Compares two doubles.
	 * <p>
	 * Stack: value1, value2 → result
	 * <p>
	 */
	@Override
	public void visitDCMPG(DCMPG obj) {
		notImplementedYet(obj);
	}

	// -----------------------------------------------------------------
	/**
	 * 12. DCMPL
	 * <p>
	 * Compares two doubles.
	 * <p>
	 * Stack: value1, value2 → result
	 * <p>
	 */
	@Override
	public void visitDCMPL(DCMPL obj) {
		notImplementedYet(obj);
	}

	// -----------------------------------------------------------------
	/**
	 * 13. DECONST
	 * <p>
	 * Pushes the constant 0.0 | 1.0 onto the stack.
	 * <p>
	 * Stack: → 0.0 | 1.0
	 */
	@Override
	public void visitDCONST(DCONST obj) {
		notImplementedYet(obj);
	}

	// -----------------------------------------------------------------

	/**
	 * 14. FCMPG
	 * <p>
	 * Compares two floats.
	 * <p>
	 * Stack: value1, value2 → result
	 */
	@Override
	public void visitFCMPG(FCMPG obj) {
		notImplementedYet(obj);
	}

	// -----------------------------------------------------------------

	/**
	 * 15. FCMPL
	 * <p>
	 * Compares two floats.
	 * <p>
	 * Stack: value1, value2 → result
	 */
	@Override
	public void visitFCMPL(FCMPL obj) {
		notImplementedYet(obj);
	}

	// -----------------------------------------------------------------
	/**
	 * 16. FCONST
	 * <p>
	 * Pushes 0.0f | 1.0f | 2.0f on the stack.
	 * <p>
	 * Stack: → 0.0f | 1.0f | 2.0f
	 */
	@Override
	public void visitFCONST(FCONST obj) {
		notImplementedYet(obj);
	}

	// -----------------------------------------------------------------
	/**
	 * 17. ICONST
	 * <p>
	 * Loads the int value -1 | 0 | 1 | 2 | 3 | 4 | 5 onto the stack.
	 * <p>
	 * Stack: → -1 | 0 | 1 | 2 | 3 | 4 | 5
	 */
	@Override
	public void visitICONST(ICONST obj) {
		System.out.println(obj.toString(false));
		stack.push(Entry.someInt);
		doesEscape = ThreeValueBoolean.no;
	}

	// -----------------------------------------------------------------
	/**
	 * 18. IMPDEP1
	 * <p>
	 * Reserved for implementation-dependent operations within debuggers; should
	 * not appear in any class file.
	 */
	@Override
	public void visitIMPDEP1(IMPDEP1 obj) {
		notImplementedYet(obj);
	}

	// -----------------------------------------------------------------
	/**
	 * 19. IMPDEP2
	 * <p>
	 * Reserved for implementation-dependent operations within debuggers; should
	 * not appear in any class file.
	 */
	@Override
	public void visitIMPDEP2(IMPDEP2 obj) {
		notImplementedYet(obj);
	}

	// -----------------------------------------------------------------
	/**
	 * 20. LCMP
	 * <p>
	 * Compares two long values.
	 * <p>
	 * Stack: value1, value2 → result
	 */
	@Override
	public void visitLCMP(LCMP obj) {
		notImplementedYet(obj);
	}

	// -----------------------------------------------------------------
	/**
	 * 21. LCONST
	 * <p>
	 * Pushes the long 0 | 1 onto the stack.
	 * <p>
	 * Stack: → 0L | 1L
	 */
	@Override
	public void visitLCONST(LCONST obj) {
		notImplementedYet(obj);
	}

	// ---LocalVariableInstruction--------------------------------------

	/**
	 * 22. LocalVariableInstruction<br>
	 * 22.1. IINC
	 * <p>
	 * Increments local variable #index by signed byte const.
	 * <p>
	 * Stack: No change. <br>
	 * Note: 2 other bytes (index, const)
	 */
	@Override
	public void visitIINC(IINC obj) {
		notImplementedYet(obj);
	}

	/**
	 * 22. LocalVariableInstruction <br>
	 * 22.2. LoadInstruction
	 * <p>
	 * Loads a value from a local variable.
	 * <p>
	 * Stack: → value
	 */
	@Override
	public void visitLoadInstruction(LoadInstruction obj) {
		System.out.println(obj.toString(false));
		stack.push(localVars.getForIndex(obj.getIndex()));
		doesEscape = ThreeValueBoolean.no;
	}

	/**
	 * 22. LocalVariableInstruction <br>
	 * 22.3. StoreInstruction
	 * <p>
	 * Stores a value in a local variable.
	 * <p>
	 * Stack: value →
	 */
	@Override
	public void visitStoreInstruction(StoreInstruction obj) {
		System.out.println(obj.toString(false));
		localVars.setForIndex(obj.getIndex(), stack.pop());
		doesEscape = ThreeValueBoolean.no;
	}

	// -----------------------------------------------------------------
	/**
	 * 23. MONITORENTER
	 * <p>
	 * Enters monitor for object ("grab the lock" - start of synchronized()
	 * section).
	 * <p>
	 * Stack: objectref →
	 * <p>
	 * DoesEscape: No, enters synchronized section.
	 */
	@Override
	public void visitMONITORENTER(MONITORENTER obj) {
		System.out.println("MONITORENTER " + ": No Escape");
		stack.pop();
		doesEscape = ThreeValueBoolean.no;
	}

	// -----------------------------------------------------------------
	/**
	 * 24. MINITOREXIT
	 * <p>
	 * exit monitor for object ("release the lock" - end of synchronized()
	 * section).
	 * <p>
	 * Stack: objectref →
	 * <p>
	 * DoesEscape: No, uses synchronization.
	 */
	@Override
	public void visitMONITOREXIT(MONITOREXIT obj) {
		System.out.println("MONITOREXIT " + ": No Escape");
		stack.pop();
		doesEscape = ThreeValueBoolean.no;
	}

	// -----------------------------------------------------------------
	/**
	 * 25. NEWARRAY
	 * <p>
	 * Creates new array with count elements of primitive type identified by
	 * atype.
	 * <p>
	 * Stack: count → arrayref <br>
	 * Note: 1 other byte (atype)
	 */
	@Override
	public void visitNEWARRAY(NEWARRAY obj) {
		notImplementedYet(obj);
	}

	// -----------------------------------------------------------------
	/**
	 * 26. NOP
	 * <p>
	 * Performs no operation.
	 * <p>
	 * Stack: No change.
	 */
	@Override
	public void visitNOP(NOP obj) {
		notImplementedYet(obj);
	}

	// -----------------------------------------------------------------
	/**
	 * 27. RET
	 * <p>
	 * Continues execution from address taken from a local variable #index (the
	 * asymmetry with jsr is intentional).
	 * <p>
	 * Stack: No change.
	 */
	@Override
	public void visitRET(RET obj) {
		notImplementedYet(obj);
	}

	// -----------------------------------------------------------------
	/**
	 * 28. ReturnInstruction
	 * <p>
	 * Returns a value (or nothing if void) from a method.
	 * <p>
	 * Stack: value → [empty] (or → [empty] if void)
	 */
	@Override
	public void visitReturnInstruction(ReturnInstruction obj) {
		System.out.println("ReturnInstruction");
		doesEscape = ThreeValueBoolean.no;
	}

	// -----------------------------------------------------------------
	/**
	 * 29. SIPUSH
	 * <p>
	 * Pushes a short onto the stack.
	 * <p>
	 * Stack: → value <br>
	 * Note: 2 other bytes (byte1, byte2)
	 */
	@Override
	public void visitSIPUSH(SIPUSH obj) {
		System.out.println("sipush " + obj.getValue());
		stack.push(Entry.someShort);
		doesEscape = ThreeValueBoolean.no;
	}

	// ---StackInstruction----------------------------------------------
	/**
	 * 30. StackInstructions <br>
	 * 30.1. DUP
	 * <p>
	 * Duplicates the value on top of the stack.
	 * <p>
	 * Stack: value → value, value
	 */
	@Override
	public void visitDUP(DUP obj) {
		System.out.println(obj.toString(false));
		Entry entry = stack.pop();
		stack.push(entry);
		stack.push(entry);
		doesEscape = ThreeValueBoolean.no;
	}

	/**
	 * 30. StackInstructions <br>
	 * 30.2. DUP_X1
	 * <p>
	 * Inserts a copy of the top value into the stack two values from the top.
	 * value1 and value2 must not be of the type double or long.
	 * <p>
	 * Stack: value2, value1 → value1, value2, value1
	 */
	@Override
	public void visitDUP_X1(DUP_X1 obj) {
		notImplementedYet(obj);
	}

	/**
	 * 30. StackInstructions <br>
	 * 30.3. DUP_X2
	 * <p>
	 * Inserts a copy of the top value into the stack two (if value2 is double
	 * or long it takes up the entry of value3, too) or three values (if value2
	 * is neither double nor long) from the top.
	 * <p>
	 * Stack: value3, value2, value1 → value1, value3, value2, value1
	 */
	@Override
	public void visitDUP_X2(DUP_X2 obj) {
		notImplementedYet(obj);
	}

	/**
	 * 30. StackInstructions <br>
	 * 30.4. DUP2
	 * <p>
	 * Duplicates top two stack words (two values, if value1 is not double nor
	 * long; a single value, if value1 is double or long).
	 * <p>
	 * Stack: {value2, value1} → {value2, value1}, {value2, value1}
	 */
	@Override
	public void visitDUP2(DUP2 obj) {
		notImplementedYet(obj);
	}

	/**
	 * 30. StackInstructions <br>
	 * 30.5. DUP2_X1
	 * <p>
	 * Duplicates two words and insert beneath third word (see explanation
	 * 30.4.).
	 * <p>
	 * Stack: value3, {value2, value1} → {value2, value1}, value3, {value2,
	 * value1}
	 */
	@Override
	public void visitDUP2_X1(DUP2_X1 obj) {
		notImplementedYet(obj);
	}

	/**
	 * 30. StackInstructions <br>
	 * 30.5. DUP2_X2
	 * <p>
	 * Duplicates two words and insert beneath fourth word (see explanation
	 * 30.4.).
	 * <p>
	 * Stack: {value4, value3}, {value2, value1} → {value2, value1}, {value4,
	 * value3}, {value2, value1}
	 */
	@Override
	public void visitDUP2_X2(DUP2_X2 obj) {
		notImplementedYet(obj);
	}

	/**
	 * 30. StackInstuructions <br>
	 * 30.6. POP
	 * <p>
	 * Discards the top value on the stack.
	 * <p>
	 * Stack: value →
	 */
	@Override
	public void visitPOP(POP obj) {
		notImplementedYet(obj);
	}

	/**
	 * 30. StackInstructions <br>
	 * 30.7. POP2
	 * <p>
	 * Discards the top two values on the stack (or one value, if it is a double
	 * or long).
	 * <p>
	 * Stack: {value2, value1} →
	 */
	@Override
	public void visitPOP2(POP2 obj) {
		notImplementedYet(obj);
	}

	/**
	 * 30. StackInstructions <br>
	 * 30.8. SWAP
	 * <p>
	 * Swaps two top words on the stack (note that value1 and value2 must not be
	 * double or long).
	 * <p>
	 * Stack: value2, value1 → value1, value2
	 */
	@Override
	public void visitSWAP(SWAP obj) {
		notImplementedYet(obj);
	}
	// -----------------------------------------------------------------

}
