package de.htwg_konstanz.in.jca;

import java.util.ArrayList;
import java.util.Stack;

import org.apache.bcel.Repository;
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
import org.apache.bcel.generic.GotoInstruction;
import org.apache.bcel.generic.ICONST;
import org.apache.bcel.generic.IINC;
import org.apache.bcel.generic.IMPDEP1;
import org.apache.bcel.generic.IMPDEP2;
import org.apache.bcel.generic.INSTANCEOF;
import org.apache.bcel.generic.INVOKEINTERFACE;
import org.apache.bcel.generic.INVOKESPECIAL;
import org.apache.bcel.generic.INVOKESTATIC;
import org.apache.bcel.generic.INVOKEVIRTUAL;
import org.apache.bcel.generic.IfInstruction;
import org.apache.bcel.generic.InstructionHandle;
import org.apache.bcel.generic.InstructionList;
import org.apache.bcel.generic.JsrInstruction;
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
import org.apache.bcel.generic.Select;
import org.apache.bcel.generic.StoreInstruction;
import org.apache.bcel.generic.Type;

import edu.umd.cs.findbugs.BugCollection;
import edu.umd.cs.findbugs.BugInstance;
import edu.umd.cs.findbugs.SortedBugCollection;

/**
 * Analyzes constructors whether the this-reference escapes or not. Therefore a
 * virtual machine is simulated and all occurring ByteCode operations are
 * performed in the corresponding visit-method.
 * <p>
 * This class does:
 * <ul>
 * <li>Check if the this reference escapes or not
 * <li>Provide a bug collection with the found errors
 * <li>Handle the type of values
 * <li>Handle if branches by static analysis
 * <li>Detect loops
 * <li>Handle switch cases
 * </ul>
 * </p>
 * <p>
 * This class does not:
 * <ul>
 * <li>Check if the entries have the expected type
 * <li>Check if 64 bit values are set/read atomic
 * <li>Consider exact values
 * </ul>
 * </p>
 */
public class CtorAnalysisVisitor extends EmptyVisitor {
	private final LocalVars localVars;
	private final Stack<Entry> stack;
	private final Method ctor;
	private final ConstantPoolGen constantPoolGen;
	private InstructionHandle instructionHandle;
	private ArrayList<InstructionHandle> alreadyVisited;

	private volatile SortedBugCollection bugs = new SortedBugCollection();
	private volatile Entry result = null;

	CtorAnalysisVisitor(LocalVars localVars, Stack<Entry> stack, Method ctor) {
		this.localVars = localVars;
		this.stack = stack;
		this.ctor = ctor;
		this.constantPoolGen = new ConstantPoolGen(ctor.getConstantPool());
		InstructionHandle[] instructionHandles = new InstructionList(ctor
				.getCode().getCode()).getInstructionHandles();
		instructionHandle = instructionHandles[0];
		// XXX
		alreadyVisited = new ArrayList<InstructionHandle>();
	}

	/**
	 * Private constructor. Used for if branches occur. Copies the current state
	 * of a CtorAnalysisVisitor and continues execution from a given point.
	 * 
	 * @param localVars
	 *            The LocalVars to be copied.
	 * @param stack
	 *            The Stack to be copied.
	 * @param ctor
	 *            The Ctor to be copied.
	 * @param constantPoolGen
	 *            The ConstantPoolGen to be copied.
	 * @param firstInstruction
	 *            The InstructionHandle to start from.
	 * @param alreadyVisited
	 *            The BranchInstructions already visited.
	 */
	@SuppressWarnings("unchecked")
	private CtorAnalysisVisitor(LocalVars localVars, Stack<Entry> stack,
			Method ctor, ConstantPoolGen constantPoolGen,
			InstructionHandle firstInstruction,
			ArrayList<InstructionHandle> alreadyVisited) {
		this.localVars = new LocalVars(localVars);
		this.stack = (Stack<Entry>) stack.clone();
		this.ctor = ctor;
		this.constantPoolGen = constantPoolGen;
		this.instructionHandle = firstInstruction;
		// XXX
		this.alreadyVisited = (ArrayList<InstructionHandle>) alreadyVisited
				.clone();
	}

	public BugCollection doesEscape() {
		return bugs;
	}

	public BugCollection analyze() {
		instructionHandle.accept(this);
		return bugs;
	}

	public Entry getResult() {
		return result;
	}

	/**
	 * Called if a visit method is not yet implemented. Stops execution of the
	 * InstructionHandles.
	 */
	private void notImplementedYet(Object instruction) {
		System.out.println(instruction.toString());
		System.out.println("NOT IMPLEMENTED YET");
		bugs.add(new BugInstance("Warning: 'this' reference might escape", 1));
		System.out.println();
	}

	// ******************************************************************//
	// Visit section //
	// For more details on ByteCode instructions see: //
	// http://en.wikipedia.org/wiki/Java_bytecode_instruction_listings //
	// For details on enumeration see: //
	// https://docs.google.com/open?id=0B4RYegfkX-vPUnlRUm56S1YtMG8 //
	// ******************************************************************//

	// -----------------------------------------------------------------
	/**
	 * 1. ACONST_NULL<br>
	 * Called when an ACONST_NULL operation occurs. Pushes a null reference onto
	 * the stack.
	 */
	@Override
	public void visitACONST_NULL(ACONST_NULL obj) {
		System.out.println(obj.toString(false));
		// push NULL onto stack
		stack.push(Entry.notThisReference);
		instructionHandle = instructionHandle.getNext();
		instructionHandle.accept(this);
	}

	// -----------------------------------------------------------------
	/**
	 * 2. ArithmeticInstruction <br>
	 * Called when an ArithmeticInstruction occurs and handles all
	 * ArithmeticInstructions. The type and the number of consumed and produced
	 * words are taken from the ArithmeticInstruction object.
	 */
	@Override
	public void visitArithmeticInstruction(ArithmeticInstruction obj) {
		System.out.print(obj.toString(false) + ": (");
		Type type = obj.getType(constantPoolGen);
		int consumed;
		int produced;
		if (type.equals(Type.DOUBLE) || type.equals(Type.LONG)) {
			consumed = obj.consumeStack(constantPoolGen) / 2;
			produced = obj.produceStack(constantPoolGen) / 2;
		} else {
			consumed = obj.consumeStack(constantPoolGen);
			produced = obj.produceStack(constantPoolGen);
		}
		Entry entry;
		for (int i = 0; i < consumed; i++) {
			entry = stack.pop();
			System.out.print((i == 0) ? entry : ", " + entry);
		}
		System.out.print(") -> (");
		entry = Entry.getInstance(type.getSignature());
		for (int i = 0; i < produced; i++) {
			stack.push(entry);
			System.out.print((i == 0) ? entry : ", " + entry);
		}
		System.out.println(")");
		instructionHandle = instructionHandle.getNext();
		instructionHandle.accept(this);
	}

	// -----------------------------------------------------------------

	/**
	 * 3. ArrayInstruction<br>
	 * Called when an ArrayInstruction operation occurs. This visitor handles
	 * all ALOAD and ASTORE instructions distinguished by the opcode.
	 */
	@Override
	public void visitArrayInstruction(ArrayInstruction obj) {
		System.out.println(obj.toString(false));
		short opcode = obj.getOpcode();
		if (opcode >= 0x2E && opcode <= 0x35) {
			// all ALOAD instructions
			// pop index
			stack.pop();
			// pop reference
			stack.pop();
			if (opcode == 0x32) {
				// AALOAD
				stack.push(Entry.maybeThisReference);
			} else {
				// all other ALOAD instructions
				stack.push(Entry.getInstance(obj.getType(constantPoolGen)
						.getSignature()));
			}
		} else {
			// all ASTORE instructions
			// pop value
			stack.pop();
			// pop index
			stack.pop();
			// pop reference
			stack.pop();
		}
		instructionHandle = instructionHandle.getNext();
		instructionHandle.accept(this);
	}

	// -----------------------------------------------------------------
	/**
	 * 4. ARRAYLENGTH<br>
	 * Called when an ARRAYLENGTH operation occurs. Gets the length of an array.
	 * Pops an array reference from the stack and pushes the array length as an
	 * integer value.
	 */
	@Override
	public void visitARRAYLENGTH(ARRAYLENGTH obj) {
		System.out.println(obj.toString(false));
		// pops array reference
		stack.pop();
		// pushes length
		stack.push(Entry.someInt);
		instructionHandle = instructionHandle.getNext();
		instructionHandle.accept(this);
	}

	// -----------------------------------------------------------------
	/**
	 * 5. ATHROW<br>
	 * Called when an ATHROW operation occurs. Clears the stack and pushes a
	 * reference to the thrown error or exception.
	 */
	@Override
	public void visitATHROW(ATHROW obj) {
		notImplementedYet(obj);
		// TODO
		// first try
		/*
		 * System.out.println(obj.toString(false)); Class<?>[] exceptions =
		 * obj.getExceptions(); for (Class<?> exception : exceptions) {
		 * System.out.println(exception); } stack.clear();
		 * stack.push(Entry.notThisReference);
		 */
	}

	// -----------------------------------------------------------------
	/**
	 * 6. BIPUSH<br>
	 * Called when a BIPUSH operation occurs. Pushes a byte onto the stack as an
	 * integer value.
	 */
	@Override
	public void visitBIPUSH(BIPUSH obj) {
		System.out.println(obj.toString(false));
		// pushes the integer value onto the stack
		stack.push(Entry.someInt);
		instructionHandle = instructionHandle.getNext();
		instructionHandle.accept(this);
	}

	// -----------------------------------------------------------------
	/**
	 * 7. BranchInstruction<br>
	 * 7.1. GotoInstruction<br>
	 * Called when a GotoInstruction operation occurs. Shifts the
	 * InstructionHandle to the target instead of the next one.
	 */
	@Override
	public void visitGotoInstruction(GotoInstruction obj) {
		System.out.println(obj.toString(false));
		instructionHandle = obj.getTarget();
		instructionHandle.accept(this);
	}

	/**
	 * 7. BranchInstruction<br>
	 * 7.2. IfInstruction<br>
	 * Called when a IfInstruction operation occurs. Handles all IfInstructions
	 * the same way. It pops the consumed integer values from the stack and
	 * checks if the IfInstruction already occurred. If true, this method will
	 * execute the next instruction from the instructionHandle list, else it
	 * will follow both possible branches.
	 */
	@Override
	public void visitIfInstruction(IfInstruction obj) {
		System.out.println(obj.toString(false));
		for (int i = 0; i < obj.consumeStack(constantPoolGen); i++) {
			stack.pop();
		}
		// XXX
		if (alreadyVisited.contains(instructionHandle)) {
			System.out
					.println("Loop detected, skipping positive execution of IfInstruction.");
			System.out.println("Following negative path: ");
			instructionHandle = instructionHandle.getNext();
			instructionHandle.accept(this);
		} else {

			alreadyVisited.add(instructionHandle);
			System.out.println("------------------  " + alreadyVisited.size()
					+ ".A  ------------------");
			CtorAnalysisVisitor branch1Visitor = new CtorAnalysisVisitor(
					localVars, stack, ctor, constantPoolGen,
					instructionHandle.getNext(), alreadyVisited);
			bugs.addAll(branch1Visitor.analyze().getCollection());

			System.out.println("------------------  " + alreadyVisited.size()
					+ ".B  ------------------");
			CtorAnalysisVisitor branch2Visitor = new CtorAnalysisVisitor(
					localVars, stack, ctor, constantPoolGen, obj.getTarget(),
					alreadyVisited);
			bugs.addAll(branch2Visitor.analyze().getCollection());
		}
	}

	/**
	 * 7. BranchInstruction<br>
	 * 7.3. JsrInstruction<br>
	 * Called when a JsrInstruction operation occurs. TODO: JAVADOC
	 */
	@Override
	public void visitJsrInstruction(JsrInstruction obj) {
		// TODO Can we all handle them the same way?
		notImplementedYet(obj);
	}

	/**
	 * 7. BranchInstruction<br>
	 * 7.4. Select<br>
	 * Called when a Select operation occurs. Pops an integer index from the
	 * stack and follows every possible case (including fall-trough) by creating
	 * a new instance of CtorAnalysisVisitor for each case.
	 */
	@Override
	public void visitSelect(Select obj) {
		// XXX
		System.out.println(obj.toString(false));
		// pops integer index
		stack.pop();
		// gets all targets excluding the default case
		InstructionHandle[] targets = obj.getTargets();
		CtorAnalysisVisitor caseToFollow;
		// follows all targets excluding the default case
		for (int i = 0; i < targets.length; i++) {
			System.out.println("--------------- Line "
					+ targets[i].getPosition() + " ---------------");
			caseToFollow = new CtorAnalysisVisitor(localVars, stack, ctor,
					constantPoolGen, targets[i], alreadyVisited);
			caseToFollow.analyze();
		}
		// handles the default case and follows it
		// NOTE: If the keyword "Default:" is not in the switch the following
		// target is the end of the switch without executing a case.
		System.out.println("--------------- Line "
				+ obj.getTarget().getPosition()
				+ " (DefaultCase) ---------------");
		caseToFollow = new CtorAnalysisVisitor(localVars, stack, ctor,
				constantPoolGen, obj.getTarget(), alreadyVisited);
		caseToFollow.analyze();
	}

	// -----------------------------------------------------------------
	/**
	 * 8. BREAKPOINT<br>
	 * Called when a BREAKPOINT operation occurs. The BREAKPOINT operation is
	 * reserved for Java debuggers and should not appear in any class file.
	 */
	@Override
	public void visitBREAKPOINT(BREAKPOINT obj) {
		// no need to implement this one
		notImplementedYet(obj);
	}

	// -----------------------------------------------------------------
	/**
	 * 9. ConversionInstruction <br>
	 * Called when a ConversionInstruction operation occurs.Converts the type of
	 * a value to another one. Therefore a value is popped, converted and the
	 * new one pushed back onto the stack.
	 */
	@Override
	public void visitConversionInstruction(ConversionInstruction obj) {
		System.out.print(obj.toString(false) + ": (");
		// pops the value
		Entry entry = stack.pop();
		System.out.print(entry + ") -> (");
		Entry convertedEntry = Entry.getInstance(obj.getType(constantPoolGen)
				.getSignature());
		// pushes the converted value
		stack.push(convertedEntry);
		System.out.println(convertedEntry + ")");
		instructionHandle = instructionHandle.getNext();
		instructionHandle.accept(this);
	}

	// ---CPInstruction-------------------------------------------------
	/**
	 * 10. CPInstruction <br>
	 * 10.1. ANEWARRAY<br>
	 * Called when an ANEWARRAY operation occurs. An ANEWARRAY operation creates
	 * a new array. Therefore the length as an integer value is popped and the
	 * new array reference is pushed onto the stack.
	 */
	@Override
	public void visitANEWARRAY(ANEWARRAY obj) {
		System.out.println(obj.toString(false));
		// pops the length
		stack.pop();
		// pushes the array reference
		stack.push(Entry.notThisReference);
		instructionHandle = instructionHandle.getNext();
		instructionHandle.accept(this);
	}

	/**
	 * 10. CPInstruction <br>
	 * 10.2. CHECKCAST<<br>
	 * Called when a CHECKCAST operation occurs. Pops a object reference from
	 * the stack, checks if it is of a certain type and pushes the reference
	 * back onto the stack or throws an exception if not.
	 */
	@Override
	public void visitCHECKCAST(CHECKCAST obj) {
		notImplementedYet(obj);
		// TODO
		// first try
		System.out.print(obj.toString(false) + ": (");
		Entry entry = stack.pop();
		System.out.println(entry + ") ?= ("
				+ obj.getLoadClassType(constantPoolGen));

		if (entry.equals(Entry.getInstance(obj
				.getLoadClassType(constantPoolGen).getSignature()))) {
			// push entry back onto stack
			stack.push(entry);
		} else {
			// throw exception
			// TODO Exception-handling???
		}
		instructionHandle = instructionHandle.getNext();
		instructionHandle.accept(this);
	}

	/**
	 * 10. CPInstruction <br>
	 * 10.3. FieldOrMethod <br>
	 * 10.3.2. GETFIELD <br>
	 * Called when a GETFIELD operation occurs. Pops an object reference from
	 * the stack and pushes the corresponding field value onto the stack.
	 * */
	@Override
	public void visitGETFIELD(GETFIELD obj) {
		System.out.print(obj.toString(false) + ": ");

		Entry ref = stack.pop();
		System.out.println(ref + "." + obj.getFieldName(constantPoolGen));

		Entry entry = Entry.getInstance(obj.getSignature(constantPoolGen));
		if (entry.equals(Entry.notThisReference)) {
			entry = Entry.maybeThisReference;
		}
		stack.push(entry);
		instructionHandle = instructionHandle.getNext();
		instructionHandle.accept(this);
	}

	/**
	 * 10. CPInstruction <br>
	 * 10.3. FieldOrMethod <br>
	 * 10.3.3. GETSTATIC <br>
	 * Called when a GETSTATIC operation occurs. Pushes a static field value of
	 * a class onto the stack.
	 */
	@Override
	public void visitGETSTATIC(GETSTATIC obj) {
		// XXX
		// throws IncompatibleClassException if field is not static
		// TODO error handling
		System.err.println("FieldName " + obj.getFieldName(constantPoolGen));
		System.err.println("Signature " + obj.getSignature(constantPoolGen));
		System.err.println("FieldType " + obj.getFieldType(constantPoolGen));
		System.err.println("ReferencedType "
				+ obj.getReferenceType(constantPoolGen));
		System.err.println("Type " + obj.getType(constantPoolGen));
		instructionHandle = instructionHandle.getNext();
		instructionHandle.accept(this);
		// FIXME
	}

	/**
	 * 10. CPInstruction <br>
	 * 10.3. FieldOrMethod <br>
	 * 10.3.4. PUTFIELD <br>
	 * Called when a PUTFIELD operation occurs. Pops a value and an object
	 * reference from the stack. The field in the object reference is set to the
	 * value.
	 * */
	@Override
	public void visitPUTFIELD(PUTFIELD obj) {
		System.out.print(obj.toString(false) + ": ");

		// right side of assignment
		Entry right = stack.pop();

		if (right.equals(Entry.thisReference)) {
			System.out
					.println("Error: 'this' reference is assigned to some object's field and escapes.");
			bugs.add(new BugInstance(
					"Error: 'this' reference is assigned to some object's field and escapes.",
					2));

		}
		if (right.equals(Entry.maybeThisReference)) {
			System.out
					.println("Warning: 'this' reference might be assigned to some object's field and might escape.");
			bugs.add(new BugInstance(
					"Warning: 'this' reference might be assigned to some object's field and might escape.",
					1));

		}

		// pop left side of assignment off the stack, too
		Entry left = stack.pop();
		System.out.println(left + "." + obj.getFieldName(constantPoolGen)
				+ " <--" + right);
		instructionHandle = instructionHandle.getNext();
		instructionHandle.accept(this);
	}

	/**
	 * 10. CPInstruction <br>
	 * 10.3. FieldOrMethod <br>
	 * 10.3.4. PUTSTATIC <br>
	 * Called when a PUTSTATIC operation occurs. Pops a value from the stack and
	 * sets a static field in a class to the popped value.
	 */
	@Override
	public void visitPUTSTATIC(PUTSTATIC obj) {
		notImplementedYet(obj);
	}

	/**
	 * TODO: ReWrite 10. CPInstruction <br>
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
	 * TODO ReWrite 10. CPInstruction <br>
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

		bugs.addAll(superCtorAnalyzer.doesThisReferenceEscape(stack)
				.getCollection());

		if (!obj.getReturnType(constantPoolGen).equals(BasicType.VOID)) {
			stack.push(superCtorAnalyzer.getResult());
		}
		instructionHandle = instructionHandle.getNext();
		instructionHandle.accept(this);
	}

	/**
	 * TODO ReWrite 10. CPInstruction <br>
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
		// TODO
		Type[] types = obj.getArgumentTypes(constantPoolGen);
		for (Type type : types) {
			System.err.println("argType: " + type);
		}
		System.err.println("Signature " + obj.getSignature(constantPoolGen));
		System.err.println("RetType: " + obj.getReturnType(constantPoolGen));
		System.err.println("Type: " + obj.getType(constantPoolGen));
		notImplementedYet(obj);
	}

	/**
	 * TODO ReWrite 10. CPInstruction <br>
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
		Type[] types = obj.getArgumentTypes(constantPoolGen);
		for (Type type : types) {
			System.out.println("argType: " + type);
		}
		System.out.println("Signature " + obj.getSignature(constantPoolGen));
		System.out.println("RetType: " + obj.getReturnType(constantPoolGen));
		System.out.println("Type: " + obj.getType(constantPoolGen));

		// notImplementedYet(obj);
	}

	/**
	 * TODO ReWrite 10. CPInstruction <br>
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
	 * 10. CPInstruction <br>
	 * 10.6. LDC<br>
	 * 
	 * Called when a LDC instruction occurs. Pushes a constant of type String,
	 * integer or float from the constant pool onto the stack.
	 */
	@Override
	public void visitLDC(LDC obj) {
		// FIXME This method is wrong!!! It should handle Strings as well and
		// can also work with symbolic links to classes (this-escape?)
		System.out.println(obj.toString(false));

		if (obj.getType(constantPoolGen).equals(Type.INT)) {
			stack.push(Entry.someInt);
			instructionHandle = instructionHandle.getNext();
			instructionHandle.accept(this);
			return;
		}

		if (obj.getType(constantPoolGen).equals(Type.FLOAT)) {
			stack.push(Entry.someFloat);
			instructionHandle = instructionHandle.getNext();
			instructionHandle.accept(this);
			return;
		}

		stack.push(Entry.notThisReference);
		instructionHandle = instructionHandle.getNext();
		instructionHandle.accept(this);
	}

	/**
	 * 10. CPInstruction <br>
	 * 10.7. LDC2_W <br>
	 * Called when a LDC2_W instruction occurs. Pushes a constant of type double
	 * or long from the constant pool onto the stack.
	 * */
	@Override
	public void visitLDC2_W(LDC2_W obj) {
		System.out.println(obj.toString(false));
		// XXX
		Type type = obj.getType(constantPoolGen);
		if (type.equals(Type.LONG) || type.equals(Type.DOUBLE)) {
			stack.push(Entry.getInstance(type.getSignature()));
			instructionHandle = instructionHandle.getNext();
			instructionHandle.accept(this);
		}
	}

	/**
	 * 10. CPInstruction <br>
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
	 * 10. CPInstruction <br>
	 * 10.9. NEW
	 * <p>
	 * Creates new object of type identified by class reference in constant pool
	 * index (indexbyte1 << 8 + indexbyte2).
	 * <p>
	 * Stack: → objectref <br>
	 * Note: 2 other bytes (indexbyte1, indexbyte2)
	 */
	@Override
	public void visitNEW(NEW obj) {
		System.out.println(obj.toString(false));
		stack.push(Entry.notThisReference);
		instructionHandle = instructionHandle.getNext();
		instructionHandle.accept(this);
	}

	// -----------------------------------------------------------------
	/**
	 * 11. DCMPG <br>
	 * Called when a DCMPG operation occurs. Pops two double values from the
	 * stack, compares them and pushes the integer result onto the stack. If
	 * value1 is greater than value2 the result is 1, if value1 is equal to
	 * value2 the result is 0 and if value1 is smaller than value2 the result is
	 * -1. If value1 or value2 is NaN the result is 1.
	 */
	@Override
	public void visitDCMPG(DCMPG obj) {
		// XXX
		System.out.println(obj.toString(false));
		// pop value2
		stack.pop();
		// pop value1
		stack.pop();
		// check if value1 or value2 is NaN then push 1 and return
		// compare them and get result
		stack.push(Entry.someInt);
		instructionHandle = instructionHandle.getNext();
		instructionHandle.accept(this);
	}

	// -----------------------------------------------------------------
	/**
	 * 12. DCMPL <br>
	 * Called when a DCMPG operation occurs. Pops two double values from the
	 * stack, compares them and pushes the integer result onto the stack. If
	 * value1 is greater than value2 the result is 1, if value1 is equal to
	 * value2 the result is 0 and if value1 is smaller than value2 the result is
	 * -1. If value1 or value2 is NaN the result is -1.
	 */
	@Override
	public void visitDCMPL(DCMPL obj) {
		// XXX
		System.out.println(obj.toString(false));
		// pop value2
		stack.pop();
		// pop value1
		stack.pop();
		// check if value1 or value2 is NaN then push -1 and return
		// compare them and get result
		stack.push(Entry.someInt);
		instructionHandle = instructionHandle.getNext();
		instructionHandle.accept(this);
	}

	// -----------------------------------------------------------------
	/**
	 * 13. DECONST <br>
	 * Called when a DECONST operation occurs. Pushes the constant 0.0 or 1.0
	 * onto the stack.
	 * */
	@Override
	public void visitDCONST(DCONST obj) {
		System.out.println(obj.toString(false));
		stack.push(Entry.someInt);
		instructionHandle = instructionHandle.getNext();
		instructionHandle.accept(this);
	}

	// -----------------------------------------------------------------

	/**
	 * 14. FCMPG <br>
	 * Called when a FCMPG operation occurs. Pops two float values from the
	 * stack, compares them and pushes the integer result onto the stack. If
	 * value1 is greater than value2 the result is 1, if value1 is equal to
	 * value2 the result is 0 and if value1 is smaller than value2 the result is
	 * -1. If value1 or value2 is NaN the result is 1.
	 */
	@Override
	public void visitFCMPG(FCMPG obj) {
		// XXX
		System.out.println(obj.toString(false));
		// pop value2
		stack.pop();
		// pop value1
		stack.pop();
		// check if value1 or value2 is NaN then push 1 and return
		// compare them and get result
		stack.push(Entry.someInt);
		instructionHandle = instructionHandle.getNext();
		instructionHandle.accept(this);
	}

	// -----------------------------------------------------------------

	/**
	 * 15. FCMPL <br>
	 * Called when a DCMPG operation occurs. Pops two float values from the
	 * stack, compares them and pushes the integer result onto the stack. If
	 * value1 is greater than value2 the result is 1, if value1 is equal to
	 * value2 the result is 0 and if value1 is smaller than value2 the result is
	 * -1. If value1 or value2 is NaN the result is -1.
	 */
	@Override
	public void visitFCMPL(FCMPL obj) {
		// XXX
		System.out.println(obj.toString(false));
		// pop value2
		stack.pop();
		// pop value1
		stack.pop();
		// check if value1 or value2 is NaN then push -1 and return
		// compare them and get result
		stack.push(Entry.someInt);
		instructionHandle = instructionHandle.getNext();
		instructionHandle.accept(this);
	}

	// -----------------------------------------------------------------
	/**
	 * 16. FCONST <br>
	 * Called when a FCONST operation occurs. Pushes 0.0f, 1.0f or 2.0f on the
	 * stack.
	 */
	@Override
	public void visitFCONST(FCONST obj) {
		System.out.println(obj.toString(false));
		stack.push(Entry.someFloat);
		instructionHandle = instructionHandle.getNext();
		instructionHandle.accept(this);
	}

	// -----------------------------------------------------------------
	/**
	 * 17. ICONST <br>
	 * Called when an ICONST operation occurs. Loads the int value -1, 0, 1, 2,
	 * 3, 4 or 5 onto the stack.
	 */
	@Override
	public void visitICONST(ICONST obj) {
		System.out.println(obj.toString(false));
		stack.push(Entry.someInt);
		instructionHandle = instructionHandle.getNext();
		instructionHandle.accept(this);
	}

	// -----------------------------------------------------------------
	/**
	 * 18. IMPDEP1 <br>
	 * Called when an IMPDEP1 operation occurs. This method is reserved for
	 * implementation-dependent operations within debuggers and should not
	 * appear in any class file.
	 */
	@Override
	public void visitIMPDEP1(IMPDEP1 obj) {
		notImplementedYet(obj);
	}

	// -----------------------------------------------------------------
	/**
	 * 19. IMPDEP2 <br>
	 * Called when an IMPDEP2 operation occurs. This method is reserved for
	 * implementation-dependent operations within debuggers and should not
	 * appear in any class file.
	 */
	@Override
	public void visitIMPDEP2(IMPDEP2 obj) {
		notImplementedYet(obj);
	}

	// -----------------------------------------------------------------
	/**
	 * 20. LCMP <br>
	 * Called when a LCMP operation occurs. Pops two long values from the stack,
	 * compares them and pushes the integer result onto the stack.
	 */
	@Override
	public void visitLCMP(LCMP obj) {
		// XXX
		System.out.println(obj.toString(false));
		// pop value2
		stack.pop();
		// pop value1
		stack.pop();
		// compare them
		stack.push(Entry.someInt);
		instructionHandle = instructionHandle.getNext();
		instructionHandle.accept(this);
	}

	// -----------------------------------------------------------------
	/**
	 * 21. LCONST <br>
	 * Called when a LCONST operation occurs. Pushes the long 0L or 1L onto the
	 * stack.
	 */
	@Override
	public void visitLCONST(LCONST obj) {
		System.out.println(obj.toString(false));
		stack.push(Entry.someLong);
		instructionHandle = instructionHandle.getNext();
		instructionHandle.accept(this);
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
		System.out.println(obj.toString(false));
		// no change on the stack, operates on the localVariables
		instructionHandle = instructionHandle.getNext();
		instructionHandle.accept(this);
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
		instructionHandle = instructionHandle.getNext();
		instructionHandle.accept(this);
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
		instructionHandle = instructionHandle.getNext();
		instructionHandle.accept(this);
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
		instructionHandle = instructionHandle.getNext();
		instructionHandle.accept(this);
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
		instructionHandle = instructionHandle.getNext();
		instructionHandle.accept(this);
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
	 * 26. NOP <br>
	 * Called when a NOP operation occurs. Performs no operation.
	 */
	@Override
	public void visitNOP(NOP obj) {
		System.out.println(obj.toString(false));
		instructionHandle = instructionHandle.getNext();
		instructionHandle.accept(this);
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
		instructionHandle = instructionHandle.getNext();
		instructionHandle.accept(this);
	}

	// ---StackInstruction----------------------------------------------
	/**
	 * 30. StackInstructions <br>
	 * 30.1. DUP <br>
	 * Called when a DUP operation occurs. Duplicates the value on top of the
	 * stack.
	 */
	@Override
	public void visitDUP(DUP obj) {
		System.out.println(obj.toString(false));
		Entry entry = stack.pop();
		stack.push(entry);
		stack.push(entry);
		instructionHandle = instructionHandle.getNext();
		instructionHandle.accept(this);
	}

	/**
	 * 30. StackInstructions <br>
	 * 30.2. DUP_X1 <br>
	 * Called when a DUP_X1 operation occurs. Inserts a copy of the top value
	 * into the stack two values from the top. value1 and value2 must not be of
	 * the type double or long.
	 */
	@Override
	public void visitDUP_X1(DUP_X1 obj) {
		System.out.println(obj.toString(false));
		Entry value1 = stack.pop();
		Entry value2 = stack.pop();
		stack.push(value1);
		stack.push(value2);
		stack.push(value1);
		instructionHandle = instructionHandle.getNext();
		instructionHandle.accept(this);
	}

	/**
	 * 30. StackInstructions <br>
	 * 30.3. DUP_X2 <br>
	 * Called when a DUP_X2 operation occurs. Inserts a copy of the top value
	 * into the stack two (if value2 is double or long it takes up the entry of
	 * value3, too) or three values (if value2 is neither double nor long) from
	 * the top.
	 */
	@Override
	public void visitDUP_X2(DUP_X2 obj) {
		System.out.println(obj.toString(false));
		Entry value1 = stack.pop();
		Entry value2 = stack.pop();
		if (value2.equals(Entry.someLong) || value2.equals(Entry.someDouble)) {
			stack.push(value1);
			stack.push(value2);
			stack.push(value1);
		} else {
			Entry value3 = stack.pop();
			stack.push(value1);
			stack.push(value3);
			stack.push(value2);
			stack.push(value1);
		}
		instructionHandle = instructionHandle.getNext();
		instructionHandle.accept(this);
	}

	/**
	 * 30. StackInstructions <br>
	 * 30.4. DUP2 <br>
	 * Called when a DUP2 operation occurs. Duplicates top two stack words (two
	 * values, if value1 is not double nor long; a single value, if value1 is
	 * double or long).
	 */
	@Override
	public void visitDUP2(DUP2 obj) {
		System.out.println(obj.toString(false));
		Entry value1 = stack.pop();
		if (value1.equals(Entry.someLong) || value1.equals(Entry.someDouble)) {
			stack.push(value1);
			stack.push(value1);
		} else {
			Entry value2 = stack.pop();
			stack.push(value2);
			stack.push(value1);
			stack.push(value2);
			stack.push(value1);
		}
		instructionHandle = instructionHandle.getNext();
		instructionHandle.accept(this);
	}

	/**
	 * 30. StackInstructions <br>
	 * 30.5. DUP2_X1 <br>
	 * Called when a DUP2_X1 operation occurs. Duplicates top two words and
	 * insert beneath third word (see explanation 30.4.).
	 */
	@Override
	public void visitDUP2_X1(DUP2_X1 obj) {
		System.out.println(obj.toString(false));
		Entry value1, value2, value3;
		value1 = stack.pop();
		value2 = stack.pop();
		if (value1.equals(Entry.someLong) || value1.equals(Entry.someDouble)) {
			stack.push(value1);
			stack.push(value2);
			stack.push(value1);
		} else {
			value3 = stack.pop();
			stack.push(value2);
			stack.push(value1);
			stack.push(value3);
			stack.push(value2);
			stack.push(value1);
		}
		instructionHandle = instructionHandle.getNext();
		instructionHandle.accept(this);
	}

	/**
	 * 30. StackInstructions <br>
	 * 30.5. DUP2_X2 <br>
	 * Called when a DUP2_X2 operation occurs. Duplicates two words and insert
	 * beneath fourth word (see explanation 30.4.).
	 */
	@Override
	public void visitDUP2_X2(DUP2_X2 obj) {
		System.out.println(obj.toString(false));
		Entry value1, value2, value3, value4;
		value1 = stack.pop();
		value2 = stack.pop();
		if (value1.equals(Entry.someLong) || value1.equals(Entry.someDouble)) {
			if (value2.equals(Entry.someLong)
					|| value2.equals(Entry.someDouble)) {
				// form 4 in the JVM spec
				stack.push(value1);
				stack.push(value2);
				stack.push(value1);
			} else {
				// form 2 in the JVM spec
				value3 = stack.pop();
				stack.push(value1);
				stack.push(value3);
				stack.push(value2);
				stack.push(value1);
			}
		} else {
			value3 = stack.pop();
			if (value3.equals(Entry.someLong)
					|| value3.equals(Entry.someDouble)) {
				// form 3 in the JVM spec
				stack.push(value2);
				stack.push(value1);
				stack.push(value3);
				stack.push(value2);
				stack.push(value1);
			} else {
				// form 1 in the JVM spec
				value4 = stack.pop();
				stack.push(value2);
				stack.push(value1);
				stack.push(value4);
				stack.push(value3);
				stack.push(value2);
				stack.push(value1);
			}
		}
		instructionHandle = instructionHandle.getNext();
		instructionHandle.accept(this);
	}

	/**
	 * 30. StackInstuructions <br>
	 * 30.6. POP <br>
	 * Called when a POP operation occurs. Discards the top value/word on the
	 * stack.
	 */
	@Override
	public void visitPOP(POP obj) {
		System.out.println(obj.toString(false));
		stack.pop();
		instructionHandle = instructionHandle.getNext();
		instructionHandle.accept(this);
	}

	/**
	 * 30. StackInstructions <br>
	 * 30.7. POP2 <br>
	 * Called when a POP2 operation occurs. Discards the top two values on the
	 * stack (or one value, if it is a double or long).
	 */
	@Override
	public void visitPOP2(POP2 obj) {
		System.out.println(obj.toString(false));
		Entry entry = stack.pop();
		if (!entry.equals(Entry.someLong) && !entry.equals(Entry.someDouble)) {
			stack.pop();
		}
		instructionHandle = instructionHandle.getNext();
		instructionHandle.accept(this);
	}

	/**
	 * 30. StackInstructions <br>
	 * 30.8. SWAP <br>
	 * Called when a SWAP operation occurs. Swaps two top words on the stack
	 * (note that value1 and value2 must not be double or long).
	 * <p>
	 * Stack: value2, value1 → value1, value2
	 */
	@Override
	public void visitSWAP(SWAP obj) {
		Entry value1 = stack.pop();
		Entry value2 = stack.pop();
		stack.push(value1);
		stack.push(value2);
		instructionHandle = instructionHandle.getNext();
		instructionHandle.accept(this);
	}
	// -----------------------------------------------------------------

}
