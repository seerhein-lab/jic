package de.htwg_konstanz.in.jca;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

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
import org.apache.bcel.generic.CHECKCAST;
import org.apache.bcel.generic.CodeExceptionGen;
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
import org.apache.bcel.generic.Instruction;
import org.apache.bcel.generic.InstructionHandle;
import org.apache.bcel.generic.InvokeInstruction;
import org.apache.bcel.generic.JsrInstruction;
import org.apache.bcel.generic.LCMP;
import org.apache.bcel.generic.LCONST;
import org.apache.bcel.generic.LDC;
import org.apache.bcel.generic.LDC2_W;
import org.apache.bcel.generic.LoadInstruction;
import org.apache.bcel.generic.MONITORENTER;
import org.apache.bcel.generic.MONITOREXIT;
import org.apache.bcel.generic.MULTIANEWARRAY;
import org.apache.bcel.generic.MethodGen;
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

import de.htwg_konstanz.in.jca.PropConMethodAnalyzer.AlreadyVisitedMethod;
import de.htwg_konstanz.in.jca.ResultValue.Kind;
import edu.umd.cs.findbugs.BugCollection;
import edu.umd.cs.findbugs.BugInstance;
import edu.umd.cs.findbugs.Priorities;
import edu.umd.cs.findbugs.SortedBugCollection;
import edu.umd.cs.findbugs.ba.ClassContext;

;

/**
 * Analyzes constructors whether the this-reference escapes or not. Therefore a
 * virtual machine is simulated and all occurring byte code operations are
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
 * <li>Consider exact values
 * <li>Consider storage into arrays
 * </ul>
 * </p>
 */
public class BaseInstructionsAnalysisVisitor extends EmptyVisitor {
	protected static final Logger logger = Logger
			.getLogger("PropConInstructionsAnalysisVisitor");
	protected final ClassContext classContext;
	protected final Method method;
	protected final String indentation;
	protected final int depth;

	protected Frame frame;
	protected final ConstantPoolGen constantPoolGen;
	protected InstructionHandle instructionHandle;
	// protected final ExceptionHandlers exceptionHandlers;
	protected final CodeExceptionGen[] exceptionHandlers;
	protected ArrayList<AlreadyVisitedIfInstruction> alreadyVisited;
	protected final ArrayList<AlreadyVisitedMethod> alreadyVisitedMethods;
	protected SortedBugCollection bugs = new SortedBugCollection();
	protected Set<ResultValue> result = new HashSet<ResultValue>();

	protected static class AlreadyVisitedIfInstruction {
		private final InstructionHandle ifInstruction;
		private final boolean thenBranchVisited;

		public AlreadyVisitedIfInstruction(InstructionHandle ifInstruction,
				boolean thenBranchVisited) {
			this.ifInstruction = ifInstruction;
			this.thenBranchVisited = thenBranchVisited;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result
					+ ((ifInstruction == null) ? 0 : ifInstruction.hashCode());
			result = prime * result + (thenBranchVisited ? 1231 : 1237);
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (!(obj instanceof AlreadyVisitedIfInstruction))
				return false;
			AlreadyVisitedIfInstruction other = (AlreadyVisitedIfInstruction) obj;
			if (ifInstruction == null) {
				if (other.ifInstruction != null)
					return false;
			} else if (!ifInstruction.equals(other.ifInstruction))
				return false;
			if (thenBranchVisited != other.thenBranchVisited)
				return false;
			return true;
		}

	}

	public BaseInstructionsAnalysisVisitor(ClassContext classContext,
			Method method, Frame frame, ConstantPoolGen constantPoolGen,
			InstructionHandle instructionHandle,
			CodeExceptionGen[] exceptionHandlers,
			ArrayList<AlreadyVisitedMethod> alreadyVisitedMethods, int depth) {
		this(classContext, method, frame, constantPoolGen,
				new ArrayList<AlreadyVisitedIfInstruction>(),
				alreadyVisitedMethods, instructionHandle, exceptionHandlers,
				depth);
	}

	protected BaseInstructionsAnalysisVisitor(ClassContext classContext,
			Method method, Frame frame, ConstantPoolGen constantPoolGen,
			ArrayList<AlreadyVisitedIfInstruction> alreadyVisited,
			ArrayList<AlreadyVisitedMethod> alreadyVisitedMethods,
			InstructionHandle instructionHandle,
			CodeExceptionGen[] exceptionHandlers, int depth) {
		this.classContext = classContext;
		this.method = method;
		this.frame = frame;
		this.constantPoolGen = constantPoolGen;
		this.alreadyVisited = alreadyVisited;
		this.alreadyVisitedMethods = alreadyVisitedMethods;
		this.instructionHandle = instructionHandle;
		this.exceptionHandlers = exceptionHandlers;
		this.depth = depth;
		this.indentation = Utils.formatLoggingOutput(depth);
	}

	public BugCollection getBugs() {
		return bugs;
	}

	public Set<ResultValue> getResult() {
		return result;
	}

	protected void addBug(int priority, String message,
			InstructionHandle instructionHandle) {
		int prior;
		switch (priority) {
		case 2:
			prior = Priorities.NORMAL_PRIORITY;
			break;
		case 3:
			prior = Priorities.LOW_PRIORITY;
			break;
		case 4:
			prior = Priorities.EXP_PRIORITY;
			break;
		default:
			prior = Priorities.HIGH_PRIORITY;
			break;
		}

		BugInstance bugInstance = new BugInstance("PROPER_CONSTRUCTION_BUG",
				prior);
		bugInstance.addString(message);
		// bugInstance
		// .addClass("de.htwg_konstanz.in.jca.testclasses.UtilsTestClass");

		// System.out.println("bug instance created: " + message);

		if (classContext != null) {
			bugInstance.addClass(classContext.getJavaClass()).addSourceLine(
					classContext, method, instructionHandle);
			// System.out.println("bug instance annotated.");
		}
		bugs.add(bugInstance);
		// System.out.println("bug instance added.");

	}

	// ******************************************************************//
	// Visit section //
	// ******************************************************************//

	/**
	 * used by visitINVOKESTATIC and visitINVOKESPECIAL
	 * 
	 * @param obj
	 *            node to be visited
	 */
	protected void handleMethodThatIsAnalyzed(InvokeInstruction obj) {
		logger.log(Level.FINE, indentation + obj.toString(false));
		logger.log(
				Level.FINEST,
				indentation + "\t" + obj.getLoadClassType(constantPoolGen)
						+ "." + obj.getMethodName(constantPoolGen)
						+ obj.getSignature(constantPoolGen));
		JavaClass targetClass = null;
		try {
			targetClass = Repository.lookupClass(obj.getReferenceType(
					constantPoolGen).toString());
		} catch (ClassNotFoundException e) {
			logger.log(Level.SEVERE, "Could not load class!");
		}

		PropConClassAnalyzer targetClassAnalyzer = new PropConClassAnalyzer(
				targetClass, classContext);
		Method targetMethod = targetClassAnalyzer.getMethod(
				obj.getMethodName(constantPoolGen),
				obj.getArgumentTypes(constantPoolGen));
		MethodGen targetMethodGen = new MethodGen(targetMethod,
				targetClass.getClassName(), new ConstantPoolGen(
						targetClass.getConstantPool()));

		PropConMethodAnalyzer targetMethodAnalyzer = new PropConMethodAnalyzer(
				classContext, targetMethodGen, alreadyVisitedMethods, depth);

		// for detection of recursion
		AlreadyVisitedMethod thisMethod = new AlreadyVisitedMethod(
				targetMethod, targetMethodAnalyzer.getActualParams(frame
						.getStack()));
		if (alreadyVisitedMethods.contains(thisMethod)) {
			logger.log(Level.FINE, indentation
					+ "Recursion found: Method already analyzed.");
			// if already visited then do not analyze again
			handleMethodThatIsNotAnalyzed(obj);
			return;
		}
		alreadyVisitedMethods.add(thisMethod);

		targetMethodAnalyzer.analyze(frame.getStack());

		bugs.addAll(targetMethodAnalyzer.getBugs().getCollection());

		Set<ResultValue> calleeResults = ResultValue
				.combineReferences(targetMethodAnalyzer.getResult());

		for (ResultValue calleeResult : calleeResults) {
			if (calleeResult.getKind().equals(Kind.REGULAR)) {
				BaseInstructionsAnalysisVisitor specificCalleeResultVisitor = getCorrectInstructionsAnalysisVisitor(
						classContext, method, new Frame(frame),
						constantPoolGen, alreadyVisited, alreadyVisitedMethods,
						instructionHandle.getNext(), exceptionHandlers, depth);

				specificCalleeResultVisitor.frame
						.pushStackByRequiredSlots(calleeResult.getSlot());

				instructionHandle.getNext().accept(specificCalleeResultVisitor);

				bugs.addAll(specificCalleeResultVisitor.getBugs()
						.getCollection());
				result.addAll(specificCalleeResultVisitor.getResult());
			} else {
				Frame savedFrame = new Frame(frame);
				handleException(calleeResult.getSlot());
				frame = savedFrame;
			}

		}

	}

	/**
	 * used by visitINVOKEINTERFACE and visitINVOKEVIRTUAL
	 * 
	 * @param obj
	 *            node to be visited
	 */
	protected void handleMethodThatIsNotAnalyzed(InvokeInstruction obj) {
		logger.log(Level.FINE, indentation + obj.toString(false));
		logger.log(Level.FINEST,
				indentation + "\t" + obj.getSignature(constantPoolGen));
		// get number of args
		Type[] type = obj.getArgumentTypes(constantPoolGen);
		// get return value

		Slot returnValue = Slot.getDefaultInstance(DataType.getDataType(obj
				.getReturnType(constantPoolGen)));

		Slot argument;
		// pop a value for each arg and 1 for the hidden reference
		for (int i = 0; i < type.length + 1; i++) {
			argument = frame.popStackByRequiredSlots();
			if (argument.equals(Slot.maybeThisReference)
					|| argument.equals(Slot.thisReference)) {
				if (returnValue.getDataType().equals(DataType.referenceType)) {
					returnValue = Slot.maybeThisReference;
				}
			}
		}

		// works also for void results, because number of required slots = 0
		frame.pushStackByRequiredSlots(returnValue);

		instructionHandle = instructionHandle.getNext();
		instructionHandle.accept(this);
	}

	/**
	 * used by all CMPG instructions <br>
	 * Pops two values from the stack, compares them and pushes the integer
	 * result onto the stack. If value1 is greater than value2 the result is 1,
	 * if value1 is equal to value2 the result is 0 and if value1 is smaller
	 * than value2 the result is -1. If value1 or value2 is NaN the result is 1.
	 * 
	 * @param obj
	 *            the instruction
	 */
	protected void handleCMPG(Instruction obj) {
		logger.log(Level.FINE, indentation + obj.toString(false));

		// pop value2
		frame.popStackByRequiredSlots();
		// pop value1
		frame.popStackByRequiredSlots();
		// check if value1 or value2 is NaN then push 1 and return
		// compare them and get result
		frame.getStack().push(Slot.someInt);

		instructionHandle = instructionHandle.getNext();
		instructionHandle.accept(this);
	}

	/**
	 * used by all CMPL instructions <br>
	 * Called when a DCMPG operation occurs. Pops two values from the stack,
	 * compares them and pushes the integer result onto the stack. If value1 is
	 * greater than value2 the result is 1, if value1 is equal to value2 the
	 * result is 0 and if value1 is smaller than value2 the result is -1. If
	 * value1 or value2 is NaN the result is -1.
	 */
	protected void handleCMPL(Instruction obj) {
		logger.log(Level.FINE, indentation + obj.toString(false));
		// pop value2
		frame.popStackByRequiredSlots();
		// pop value1
		frame.popStackByRequiredSlots();
		// check if value1 or value2 is NaN then push -1 and return
		// compare them and get result
		frame.getStack().push(Slot.someInt);

		instructionHandle = instructionHandle.getNext();
		instructionHandle.accept(this);
	}

	/**
	 * used by all CONST instruction. Pushes a constant value of Type targetType
	 * onto the stack.
	 * 
	 * @param targetType
	 *            the Type to push
	 */
	protected void handleCONST(Type targetType) {
		frame.pushStackByDataType(DataType.getDataType(targetType));

		instructionHandle = instructionHandle.getNext();
		instructionHandle.accept(this);
	}

	protected BaseInstructionsAnalysisVisitor getCorrectInstructionsAnalysisVisitor(
			ClassContext classContext, Method method, Frame frame,
			ConstantPoolGen constantPoolGen,
			ArrayList<AlreadyVisitedIfInstruction> alreadyVisited,
			ArrayList<AlreadyVisitedMethod> alreadyVisitedMethods,
			InstructionHandle instructionHandle,
			CodeExceptionGen[] exceptionHandlers, int depth) {
		return new BaseInstructionsAnalysisVisitor(classContext, method, frame,
				constantPoolGen, alreadyVisited, alreadyVisitedMethods,
				instructionHandle, exceptionHandlers, depth);

	}

	// -----------------------------------------------------------------
	/**
	 * 1. ACONST_NULL<br>
	 * Called when an ACONST_NULL operation occurs. Pushes a null reference onto
	 * the stack.
	 */
	@Override
	public void visitACONST_NULL(ACONST_NULL obj) {
		logger.log(Level.FINE, indentation + obj.toString(false));
		// push NULL onto stack
		frame.getStack().push(Slot.notThisReference);
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
		logger.log(Level.FINE, indentation + obj.toString(false));
		String log = "\t" + "(";

		DataType targetDataType = DataType.getDataType(obj
				.getType(constantPoolGen));
		int consumed = obj.consumeStack(constantPoolGen);
		int produced = obj.produceStack(constantPoolGen);
		Slot entry;

		for (int i = 0; i < consumed; i++) {
			entry = frame.getStack().pop();
			log += (i == 0) ? entry : ", " + entry;
		}
		log += ") -> (";

		entry = Slot.getDefaultInstance(targetDataType);
		for (int i = 0; i < produced; i++) {
			frame.getStack().push(entry);
			log += (i == 0) ? entry : ", " + entry;
		}

		log += ")";
		logger.log(Level.FINEST, indentation + log);

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
		logger.log(Level.FINE, indentation + obj.toString(false));

		short opcode = obj.getOpcode();
		if (opcode >= 0x2E && opcode <= 0x35) {
			// all ALOAD instructions
			// pop array index
			frame.getStack().pop();
			// pop array reference
			frame.getStack().pop();
			if (opcode == 0x32) {
				// AALOAD, array might contain this-reference
				frame.getStack().push(Slot.maybeThisReference);
			} else {
				// all other ALOAD instructions
				frame.pushStackByDataType(DataType.getDataType(obj
						.getType(constantPoolGen)));
			}
		} else {
			// all ASTORE instructions, might copy this-reference into array
			// pop value
			frame.popStackByRequiredSlots();
			// pop array index
			frame.getStack().pop();
			// pop array reference
			frame.getStack().pop();
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
		logger.log(Level.FINE, indentation + obj.toString(false));
		// pops array reference
		frame.getStack().pop();
		// pushes length
		frame.getStack().push(Slot.someInt);
		instructionHandle = instructionHandle.getNext();
		instructionHandle.accept(this);
	}

	protected void handleException(Slot exception) {
		frame.getStack().clear();
		frame.getStack().push(exception);

		for (CodeExceptionGen exceptionHandler : exceptionHandlers) {
			if (PropConMethodAnalyzer.protectsInstruction(exceptionHandler,
					instructionHandle)) {
				logger.log(Level.FINE, indentation + "vvvvv "
						+ exceptionHandler.toString() + ": start vvvvv");
				BaseInstructionsAnalysisVisitor excepHandlerVisitor = new BaseInstructionsAnalysisVisitor(
						classContext, method, new Frame(frame),
						constantPoolGen, alreadyVisited, alreadyVisitedMethods,
						exceptionHandler.getHandlerPC(), exceptionHandlers,
						depth);
				exceptionHandler.getHandlerPC().accept(excepHandlerVisitor);
				bugs.addAll(excepHandlerVisitor.getBugs().getCollection());
				result.addAll(excepHandlerVisitor.getResult());
				logger.log(Level.FINE, indentation + "^^^^^ "
						+ exceptionHandler.toString() + ": end ^^^^^");
			}
		}
		result.add(new ResultValue(Kind.EXCEPTION, Slot.notThisReference));
	}

	// -----------------------------------------------------------------
	/**
	 * 5. ATHROW<br>
	 * Called when an ATHROW operation occurs. Clears the stack and pushes a
	 * reference to the thrown error or exception.
	 */
	@Override
	public void visitATHROW(ATHROW obj) {
		logger.log(Level.FINE, indentation + obj.toString(false));
		Slot exception = frame.getStack().pop();
		handleException(exception);
	}

	// -----------------------------------------------------------------
	/**
	 * 6. BIPUSH<br>
	 * Called when a BIPUSH operation occurs. Pushes a byte onto the stack as an
	 * integer value.
	 */
	@Override
	public void visitBIPUSH(BIPUSH obj) {
		logger.log(Level.FINE, indentation + obj.toString(false));
		// pushes the integer value onto the stack
		frame.getStack().push(Slot.someInt);
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
		logger.log(Level.FINE, indentation + obj.toString(false));
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
		logger.log(Level.FINE, indentation + obj.toString(false));
		for (int i = 0; i < obj.consumeStack(constantPoolGen); i++) {
			frame.getStack().pop();
		}

		logger.log(Level.FINEST, indentation + "------------------  "
				+ alreadyVisited.size()
				+ ".else  (condition might be inverted!) ------------------");
		AlreadyVisitedIfInstruction elseBranch = new AlreadyVisitedIfInstruction(
				instructionHandle, false);
		if (!alreadyVisited.contains(elseBranch)) {
			@SuppressWarnings("unchecked")
			ArrayList<AlreadyVisitedIfInstruction> newAlreadyVisited = (ArrayList<AlreadyVisitedIfInstruction>) alreadyVisited
					.clone();
			newAlreadyVisited.add(elseBranch);
			BaseInstructionsAnalysisVisitor elseBranchVisitor = getCorrectInstructionsAnalysisVisitor(
					classContext, method, new Frame(frame), constantPoolGen,
					newAlreadyVisited, alreadyVisitedMethods,
					instructionHandle.getNext(), exceptionHandlers, depth);
			instructionHandle.getNext().accept(elseBranchVisitor);
			bugs.addAll(elseBranchVisitor.getBugs().getCollection());
			result.addAll(elseBranchVisitor.getResult());
		} else {
			logger.log(Level.FINEST, indentation
					+ "Loop detected, do not re-enter.");
		}
		logger.log(Level.FINEST, indentation + "------------------  "
				+ alreadyVisited.size()
				+ ".then  (condition might be inverted!) ------------------");
		AlreadyVisitedIfInstruction thenBranch = new AlreadyVisitedIfInstruction(
				instructionHandle, true);
		if (!alreadyVisited.contains(thenBranch)) {
			@SuppressWarnings("unchecked")
			ArrayList<AlreadyVisitedIfInstruction> newAlreadyVisited = (ArrayList<AlreadyVisitedIfInstruction>) alreadyVisited
					.clone();
			newAlreadyVisited.add(thenBranch);

			BaseInstructionsAnalysisVisitor thenBranchVisitor = getCorrectInstructionsAnalysisVisitor(
					classContext, method, new Frame(frame), constantPoolGen,
					newAlreadyVisited, alreadyVisitedMethods, obj.getTarget(),
					exceptionHandlers, depth);
			obj.getTarget().accept(thenBranchVisitor);
			bugs.addAll(thenBranchVisitor.getBugs().getCollection());
			result.addAll(thenBranchVisitor.getResult());
		} else {
			logger.log(Level.FINEST, indentation
					+ "Loop detected, do not re-enter.");
		}
	}

	/**
	 * 7. BranchInstruction<br>
	 * 7.3. JsrInstruction<br>
	 * Called when a JsrInstruction operation occurs.
	 */
	@Override
	public void visitJsrInstruction(JsrInstruction obj) {
		logger.log(Level.FINE, indentation + obj.toString(false));
		logger.log(Level.WARNING,
				"Untested Code Warning: Executing JSR instruction");
		bugs.add(new BugInstance(
				"Untested Code Warning: Executing JSR instruction", 1));
		frame.getStack().push(Slot.notThisReference);
		InstructionHandle savedInstructionHandle = instructionHandle;
		instructionHandle = obj.getTarget();
		instructionHandle.accept(this);

		instructionHandle = savedInstructionHandle.getNext();
		instructionHandle.accept(this);
	}

	/**
	 * 7. BranchInstruction<br>
	 * 7.4. Select<br>
	 * Called when a Select operation occurs. Pops an integer index from the
	 * stack and follows every possible case (including fall-trough) by creating
	 * a new instance of MethodAnalysisVisitor for each case.
	 */
	@Override
	public void visitSelect(Select obj) {
		logger.log(Level.FINE, indentation + obj.toString(false));
		// pops integer index
		frame.getStack().pop();

		// gets all targets excluding the default case
		InstructionHandle[] targets = obj.getTargets();
		BaseInstructionsAnalysisVisitor caseToFollow;
		// follows all targets excluding the default case
		for (int i = 0; i < targets.length; i++) {
			logger.log(Level.FINEST, indentation + "--------------- Line "
					+ targets[i].getPosition() + " ---------------");
			caseToFollow = getCorrectInstructionsAnalysisVisitor(classContext,
					method, new Frame(frame), constantPoolGen, alreadyVisited,
					alreadyVisitedMethods, targets[i], exceptionHandlers, depth);
			targets[i].accept(caseToFollow);
			// adding occurred bugs to bug-collection
			bugs.addAll(caseToFollow.getBugs().getCollection());
			// adding result of the case to a result-list
			result.addAll(caseToFollow.getResult());
		}
		// handles the default case and follows it
		logger.log(Level.FINEST, indentation + "--------------- Line "
				+ obj.getTarget().getPosition()
				+ " (DefaultCase) ---------------");
		// NOTE: If the keyword "Default:" is not in the switch the following
		// target is the end of the switch without executing a case.
		caseToFollow = getCorrectInstructionsAnalysisVisitor(classContext,
				method, new Frame(frame), constantPoolGen, alreadyVisited,
				alreadyVisitedMethods, obj.getTarget(), exceptionHandlers,
				depth);
		obj.getTarget().accept(caseToFollow);
		// adding occurred bugs to bug-collection
		bugs.addAll(caseToFollow.getBugs().getCollection());
		// adding result of the case to a result-list
		result.addAll(caseToFollow.getResult());

	}

	// -----------------------------------------------------------------
	/**
	 * 8. BREAKPOINT<br>
	 * Called when a BREAKPOINT operation occurs. The BREAKPOINT operation is
	 * reserved for Java debuggers and should not appear in any class file.
	 */
	@Override
	public void visitBREAKPOINT(BREAKPOINT obj) {
		throw new AssertionError(
				"The BREAKPOINT operation is reserved for Java debuggers and should not appear in any class file");
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
		logger.log(Level.FINE, indentation + obj.toString(false));
		String log = "\t" + "(";

		// pop the consumed values
		for (int i = 0; i < obj.consumeStack(constantPoolGen); i++) {
			log += (i == 0) ? frame.getStack().pop() : ", "
					+ frame.getStack().pop();
		}

		log += ") -> (";

		// push the produced values
		DataType targetType = DataType
				.getDataType(obj.getType(constantPoolGen));
		for (int i = 0; i < obj.produceStack(constantPoolGen); i++) {
			log += (i == 0) ? Slot.getDefaultInstance(targetType) : ", "
					+ Slot.getDefaultInstance(targetType);
			frame.getStack().push(Slot.getDefaultInstance(targetType));
		}

		log += ")";
		logger.log(Level.FINEST, indentation + log);

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
		logger.log(Level.FINE, indentation + obj.toString(false));

		// pops the length
		frame.getStack().pop();

		// pushes the array reference
		frame.getStack().push(Slot.notThisReference);

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
		logger.log(Level.FINE, indentation + obj.toString(false));
		Slot objRef = frame.getStack().pop();
		// check type of popped object reference

		// 1st case: type cast is valid, continue execution in a separate
		// visitor
		BaseInstructionsAnalysisVisitor regularCaseVisitor = getCorrectInstructionsAnalysisVisitor(
				classContext, method, new Frame(frame), constantPoolGen,
				alreadyVisited, alreadyVisitedMethods,
				instructionHandle.getNext(), exceptionHandlers, depth);

		regularCaseVisitor.frame.getStack().push(objRef);

		logger.log(
				Level.FINEST,
				indentation + "\t" + objRef + " ?= "
						+ obj.getLoadClassType(constantPoolGen));
		instructionHandle.getNext().accept(regularCaseVisitor);

		bugs.addAll(regularCaseVisitor.getBugs().getCollection());
		result.addAll(regularCaseVisitor.getResult());

		// 2nd case: type cast is invalid, throw ClassCastException
		handleException(Slot.notThisReference);
	}

	/**
	 * 10. CPInstruction <br>
	 * 10.3. FieldOrMethod <br>
	 * 10.3.2. GETFIELD <br>
	 * Called when a GETFIELD operation occurs. Pops an object reference from
	 * the stack and pushes the value of the specified field onto the stack.
	 * */
	@Override
	public void visitGETFIELD(GETFIELD obj) {
		logger.log(Level.FINEST, indentation + obj.toString(false));

		// pop object reference
		Slot ref = frame.getStack().pop();

		// obj.getSignature() refers to desired field
		DataType targetType = DataType.getDataType(obj
				.getSignature(constantPoolGen));
		// field might contain this-reference, type permitting
		Slot value = (targetType.equals(DataType.referenceType)) ? Slot.maybeThisReference
				: Slot.getDefaultInstance(targetType);
		frame.pushStackByRequiredSlots(value);

		logger.log(
				Level.FINEST,
				indentation + "\t" + ref + "."
						+ obj.getFieldName(constantPoolGen));

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
		logger.log(Level.FINEST, indentation + obj.toString(false));

		DataType targetType = DataType.getDataType(obj
				.getFieldType(constantPoolGen));
		// might be this, we do not know
		Slot valueToGet = (targetType.equals(DataType.referenceType)) ? Slot.maybeThisReference
				: Slot.getDefaultInstance(targetType);

		frame.pushStackByRequiredSlots(valueToGet);

		String log = "\t";
		log += obj.getLoadClassType(constantPoolGen) + "."
				+ obj.getFieldName(constantPoolGen) + " (";
		log += (targetType.equals(DataType.doubleType) || targetType
				.equals(DataType.longType)) ? valueToGet + ", " + valueToGet
				: valueToGet;
		log += ")";
		logger.log(Level.FINEST, indentation + log);

		instructionHandle = instructionHandle.getNext();
		instructionHandle.accept(this);
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
		logger.log(Level.FINEST, indentation + obj.toString(false));

		// right side of assignment
		Slot right = frame.popStackByRequiredSlots();
		String logPart = "";
		logPart += (right.getDataType().equals(DataType.doubleType) || right
				.getDataType().equals(DataType.longType)) ? right + ", "
				+ right : right;

		// pop left side of assignment off the stack, too
		Slot left = frame.getStack().pop();

		logger.log(Level.FINEST,
				indentation + left + "." + obj.getFieldName(constantPoolGen)
						+ " <--" + logPart);

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
		logger.log(Level.FINE, indentation + obj.toString(false));

		// popping value from stack
		Slot toPut = frame.popStackByRequiredSlots();

		// writing log
		String log = "\t";
		log += obj.getReferenceType(constantPoolGen) + ".";
		log += obj.getName(constantPoolGen) + " <-- ";
		log += (toPut.getDataType().getNumSlots() == 2) ? toPut + ", " + toPut
				: toPut;
		logger.log(Level.FINEST, indentation + log);

		instructionHandle = instructionHandle.getNext();
		instructionHandle.accept(this);
	}

	/**
	 * 10. CPInstruction <br>
	 * 10.4. InvokeInstruction <br>
	 * 10.4.1. INVOKEINTERFACE<br>
	 * Called when an INVOKEINTERFACE instruction occurs. Due to static analysis
	 * this instruction cannot check late binding. It pulls the arguments (and
	 * the hidden reference) passed to the method to invoke from the stack and
	 * checks whether they are of type "maybeThis"/"this" or not and pushes the
	 * return value onto the stack. If the arguments are of type "maybeThis" or
	 * "this" an error is added and the result "maybeThis" is pushed. If void no
	 * value is pushed.
	 */
	@Override
	public void visitINVOKEINTERFACE(INVOKEINTERFACE obj) {
		handleMethodThatIsNotAnalyzed(obj);
	}

	/**
	 * 10. CPInstruction <br>
	 * 10.4. InvokeInstruction <br>
	 * 10.4.2. INVOKESPECIAL<br>
	 * Called when an INVOKESPECIAL operation occurs. Invokes instance method on
	 * object object reference, where the method is identified by method
	 * reference index in the constant pool.
	 */
	@Override
	public void visitINVOKESPECIAL(INVOKESPECIAL obj) {
		handleMethodThatIsAnalyzed(obj);
	}

	/**
	 * 10. CPInstruction <br>
	 * 10.4. InvokeInstruction <br>
	 * 10.4.3. INVOKESTATIC <br>
	 * Called when an INVOKESTATIC instruction occurs. Invokes a static method,
	 * where the method is identified by the constant pool. For the method to
	 * invoke a new framework is created.
	 */
	@Override
	public void visitINVOKESTATIC(INVOKESTATIC obj) {
		handleMethodThatIsAnalyzed(obj);
	}

	/**
	 * 10. CPInstruction <br>
	 * 10.4. InvokeInstruction <br>
	 * 10.4.4. INVOKEVIRTUAL <br>
	 * Called when an INVOKEVIRTUAL instruction occurs. Due to static analysis
	 * this instruction cannot check late binding. It pulls the arguments (and
	 * the hidden reference) passed to the method to invoke from the stack and
	 * checks whether they are of type "maybeThis"/"this" or not and pushes the
	 * return value onto the stack. If the arguments are of type "maybeThis" or
	 * "this" an error is added and the result "maybeThis" is pushed. If void no
	 * value is pushed.
	 */
	@Override
	public void visitINVOKEVIRTUAL(INVOKEVIRTUAL obj) {
		handleMethodThatIsNotAnalyzed(obj);
	}

	/**
	 * 10. CPInstruction <br>
	 * 10.5. INSTANCEOF
	 * <p>
	 * Determines if an object objectref is of a given type, identified by class
	 * reference index in constant pool (indexbyte1 << 8 + indexbyte2).
	 * <p>
	 * Stack: objectref result <br>
	 * Note: 2 other bytes (indexbyte1, indexbyte2)
	 */
	@Override
	public void visitINSTANCEOF(INSTANCEOF obj) {
		logger.log(Level.FINE, indentation + obj.toString(false));

		frame.getStack().pop();
		frame.pushStackByDataType(DataType.intType);

		instructionHandle = instructionHandle.getNext();
		instructionHandle.accept(this);
	}

	/**
	 * 10. CPInstruction <br>
	 * 10.6. LDC<br>
	 * Called when a LDC instruction occurs. Pushes a constant of type String,
	 * integer or float from the constant pool onto the stack.
	 */
	@Override
	public void visitLDC(LDC obj) {
		logger.log(Level.FINE, indentation + obj.toString(false));

		DataType type = DataType.getDataType(obj.getType(constantPoolGen));
		// pushes an integer, a float or notThisReference (String) onto the
		// stack
		frame.getStack().push(Slot.getDefaultInstance(type));

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
		logger.log(Level.FINE, indentation + obj.toString(false));

		DataType type = DataType.getDataType(obj.getType(constantPoolGen));
		// pushes two halfDoubles or two halfLongs onto the stack
		frame.pushStackByDataType(type);

		instructionHandle = instructionHandle.getNext();
		instructionHandle.accept(this);
	}

	/**
	 * 10. CPInstruction <br>
	 * 10.8. MULTIANEWARRAY<br>
	 * Called when a MULTIANEWARRAY operation occurs. Creates a new array of
	 * dimensions (index byte) with elements of type identified by class
	 * reference in constant pool index. The sizes of each dimension is popped
	 * from the stack as an integer value.
	 */
	@Override
	public void visitMULTIANEWARRAY(MULTIANEWARRAY obj) {

		logger.log(Level.FINE, indentation + obj.toString(false));
		String log = "\t";
		log += obj.getLoadClassType(constantPoolGen);
		log += "[" + obj.getDimensions() + "][]";
		logger.log(Level.FINEST, indentation + log);

		for (int i = 0; i < obj.consumeStack(constantPoolGen); i++) {
			// pop the 2nd dimension as integer value
			frame.getStack().pop();
		}

		// push array reference onto stack
		frame.getStack().push(Slot.notThisReference);

		instructionHandle = instructionHandle.getNext();
		instructionHandle.accept(this);
	}

	/**
	 * 10. CPInstruction <br>
	 * 10.9. NEW <br>
	 * Called when a NEW operation occurs. Creates a new object of type
	 * identified by class reference in constant pool index an pushes the
	 * reference onto the stack.
	 */
	@Override
	public void visitNEW(NEW obj) {
		logger.log(Level.FINE, indentation + obj.toString(false));

		frame.getStack().push(Slot.notThisReference);

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
		handleCMPG(obj);
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
		handleCMPL(obj);
	}

	// -----------------------------------------------------------------
	/**
	 * 13. DCONST <br>
	 * Called when a DECONST operation occurs. Pushes the double constant 0.0 or
	 * 1.0 onto the stack.
	 * */
	@Override
	public void visitDCONST(DCONST obj) {
		logger.log(Level.FINE, indentation + obj.toString(false));
		handleCONST(obj.getType(constantPoolGen));
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
		handleCMPG(obj);
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
		handleCMPL(obj);
	}

	// -----------------------------------------------------------------
	/**
	 * 16. FCONST <br>
	 * Called when a FCONST operation occurs. Pushes 0.0f, 1.0f or 2.0f on the
	 * stack.
	 */
	@Override
	public void visitFCONST(FCONST obj) {
		logger.log(Level.FINE, indentation + obj.toString(false));
		handleCONST(obj.getType(constantPoolGen));
	}

	// -----------------------------------------------------------------
	/**
	 * 17. ICONST <br>
	 * Called when an ICONST operation occurs. Loads the integer value -1, 0, 1,
	 * 2, 3, 4 or 5 onto the stack.
	 */
	@Override
	public void visitICONST(ICONST obj) {
		logger.log(Level.FINE, indentation + obj.toString(false));
		handleCONST(obj.getType(constantPoolGen));
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
		throw new AssertionError(
				"IMPDEP1 is reserved for implementation-dependent operations within debuggers "
						+ "and should not appear in any class file.");
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
		throw new AssertionError(
				"IMPDEP2 is reserved for implementation-dependent operations within debuggers "
						+ "and should not appear in any class file.");
	}

	// -----------------------------------------------------------------
	/**
	 * 20. LCMP <br>
	 * Called when a LCMP operation occurs. Pops two long values from the stack,
	 * compares them and pushes the integer result onto the stack.
	 */
	@Override
	public void visitLCMP(LCMP obj) {
		handleCMPL(obj);
	}

	// -----------------------------------------------------------------
	/**
	 * 21. LCONST <br>
	 * Called when a LCONST operation occurs. Pushes the long 0L or 1L onto the
	 * stack.
	 */
	@Override
	public void visitLCONST(LCONST obj) {
		logger.log(Level.FINE, indentation + obj.toString(false));
		handleCONST(obj.getType(constantPoolGen));
	}

	// ---LocalVariableInstruction--------------------------------------

	/**
	 * 22. LocalVariableInstruction<br>
	 * 22.1. IINC<br>
	 * Called when a IINC operation occurs. Increments a local variable by
	 * signed byte constant.
	 */
	@Override
	public void visitIINC(IINC obj) {
		logger.log(Level.FINE, indentation + obj.toString(false));
		// no change on the stack, operates on the localVariables
		instructionHandle = instructionHandle.getNext();
		instructionHandle.accept(this);
	}

	/**
	 * 22. LocalVariableInstruction <br>
	 * 22.2. LoadInstruction <br>
	 * Called when a LoadInstruction occurs. Loads a value from a local variable
	 * and pushes it onto the stack.
	 */
	@Override
	public void visitLoadInstruction(LoadInstruction obj) {
		logger.log(Level.FINE, indentation + obj.toString(false));
		frame.pushStackByRequiredSlots(frame.getLocalVars()[obj.getIndex()]);
		instructionHandle = instructionHandle.getNext();
		instructionHandle.accept(this);
	}

	/**
	 * 22. LocalVariableInstruction <br>
	 * 22.3. StoreInstruction<br>
	 * Called when a StoreInstruction occurs. Pops a value from the stack and
	 * stores it in a local variable.
	 */
	@Override
	public void visitStoreInstruction(StoreInstruction obj) {
		logger.log(Level.FINE, indentation + obj.toString(false));
		for (int i = 0; i < obj.consumeStack(constantPoolGen); i++) {
			frame.getLocalVars()[obj.getIndex() + i] = frame.getStack().pop();
		}

		instructionHandle = instructionHandle.getNext();
		instructionHandle.accept(this);
	}

	// -----------------------------------------------------------------
	/**
	 * 23. MONITORENTER<br>
	 * Called when a MONITORENTER occurs. Pops an object reference from the
	 * stack and holds it as a lock for synchronization.
	 */
	@Override
	public void visitMONITORENTER(MONITORENTER obj) {
		logger.log(Level.FINE, indentation + "MONITORENTER " + ": No Escape");

		// pop a reference
		frame.getStack().pop();

		instructionHandle = instructionHandle.getNext();
		instructionHandle.accept(this);
	}

	// -----------------------------------------------------------------
	/**
	 * 24. MONITOREXIT<br>
	 * Called when a MONITOREXIT operation occurs. Releases the lock from a
	 * synchronized section for a popped object reference.
	 */
	@Override
	public void visitMONITOREXIT(MONITOREXIT obj) {
		logger.log(Level.FINE, indentation + "MONITOREXIT " + ": No Escape");
		// pop a reference
		frame.getStack().pop();

		instructionHandle = instructionHandle.getNext();
		instructionHandle.accept(this);
	}

	// -----------------------------------------------------------------
	/**
	 * 25. NEWARRAY<br>
	 * Called when a NEWARRAY operation occurs. Creates a new array with
	 * primitive types. The length is popped as an integer value from the stack
	 * and the type is identified by a type byte.
	 */
	@Override
	public void visitNEWARRAY(NEWARRAY obj) {
		logger.log(Level.FINE, indentation + obj.toString(false));
		logger.log(Level.FINEST,
				indentation + "\t" + "(" + DataType.getDataType(obj.getType())
						+ ")");

		// pop length of new array (integer)
		frame.getStack().pop();

		// push reference to new array onto the stack
		frame.getStack().push(Slot.notThisReference);

		instructionHandle = instructionHandle.getNext();
		instructionHandle.accept(this);
	}

	// -----------------------------------------------------------------
	/**
	 * 26. NOP <br>
	 * Called when a NOP operation occurs. Performs no operation.
	 */
	@Override
	public void visitNOP(NOP obj) {
		logger.log(Level.FINE, indentation + obj.toString(false));

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
		logger.log(Level.FINE, indentation + obj.toString(false));
	}

	// -----------------------------------------------------------------
	/**
	 * 28. ReturnInstruction<br>
	 * Called when a ReturnInstruction occurs. Returns a value (or nothing if
	 * void) from a method.
	 */
	@Override
	public void visitReturnInstruction(ReturnInstruction obj) {
		logger.log(Level.FINE, indentation + obj.toString(false));
		logger.log(
				Level.FINEST,
				indentation + "\t"
						+ DataType.getDataType(obj.getType(constantPoolGen)));

		if (obj.getType(constantPoolGen).equals(Type.VOID))
			result.add(new ResultValue(Kind.REGULAR, Slot.noSlot));
		else
			result.add(new ResultValue(Kind.REGULAR, frame
					.popStackByRequiredSlots()));
	}

	// -----------------------------------------------------------------
	/**
	 * 29. SIPUSH<br>
	 * Called when a SIPUSH operation occurs. Pushes a short identified by 2
	 * index bytes onto the stack.
	 */
	@Override
	public void visitSIPUSH(SIPUSH obj) {
		logger.log(Level.FINE, indentation + obj.toString(false));
		logger.log(Level.FINEST, indentation + "\t" + obj.getValue());

		frame.getStack().push(Slot.someShort);

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
		logger.log(Level.FINE, indentation + obj.toString(false));

		Slot entry = frame.getStack().pop();
		frame.getStack().push(entry);
		frame.getStack().push(entry);

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
		logger.log(Level.FINE, indentation + obj.toString(false));

		Slot value1 = frame.getStack().pop();
		Slot value2 = frame.getStack().pop();

		frame.getStack().push(value1);
		frame.getStack().push(value2);
		frame.getStack().push(value1);

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
		logger.log(Level.FINE, indentation + obj.toString(false));

		// pop values
		Slot slot1 = frame.getStack().pop();
		Slot slot2 = frame.getStack().pop();
		Slot slot3 = frame.getStack().pop();

		// push them again (stack: s1, s3, s2, s1)
		frame.getStack().push(slot1);
		frame.getStack().push(slot3);
		frame.getStack().push(slot2);
		frame.getStack().push(slot1);

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
		logger.log(Level.FINE, indentation + obj.toString(false));

		// pop slots
		Slot slot1 = frame.getStack().pop();
		Slot slot2 = frame.getStack().pop();

		// push them again (stack: s2, s1, s2, s1
		frame.getStack().push(slot2);
		frame.getStack().push(slot1);
		frame.getStack().push(slot2);
		frame.getStack().push(slot1);

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
		logger.log(Level.FINE, indentation + obj.toString(false));

		// pop the slots
		Slot slot1 = frame.getStack().pop();
		Slot slot2 = frame.getStack().pop();
		Slot slot3 = frame.getStack().pop();

		// push them again (stack: s2, s1, s3, s2, s1)
		frame.getStack().push(slot2);
		frame.getStack().push(slot1);
		frame.getStack().push(slot3);
		frame.getStack().push(slot2);
		frame.getStack().push(slot1);

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
		logger.log(Level.FINE, indentation + obj.toString(false));

		// pop the slots
		Slot slot1 = frame.getStack().pop();
		Slot slot2 = frame.getStack().pop();
		Slot slot3 = frame.getStack().pop();
		Slot slot4 = frame.getStack().pop();

		// push them again (stack: s2, s1, s4, s3, s2, s1)
		frame.getStack().push(slot2);
		frame.getStack().push(slot1);
		frame.getStack().push(slot4);
		frame.getStack().push(slot3);
		frame.getStack().push(slot2);
		frame.getStack().push(slot1);

		instructionHandle = instructionHandle.getNext();
		instructionHandle.accept(this);
	}

	/**
	 * 30. StackInstructions <br>
	 * 30.6. POP <br>
	 * Called when a POP operation occurs. Discards the top value/word on the
	 * stack.
	 */
	@Override
	public void visitPOP(POP obj) {
		logger.log(Level.FINE, indentation + obj.toString(false));

		// may not be called for long or double
		frame.getStack().pop();

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
		logger.log(Level.FINE, indentation + obj.toString(false));

		// pop two slots
		frame.getStack().pop();
		frame.getStack().pop();

		instructionHandle = instructionHandle.getNext();
		instructionHandle.accept(this);
	}

	/**
	 * 30. StackInstructions <br>
	 * 30.8. SWAP <br>
	 * Called when a SWAP operation occurs. Swaps two top words on the stack
	 * (note that value1 and value2 must not be double or long).
	 */
	@Override
	public void visitSWAP(SWAP obj) {
		logger.log(Level.FINE, indentation + obj.toString(false));

		// pop the values
		Slot slot1 = frame.getStack().pop();
		Slot slot2 = frame.getStack().pop();

		// reorder them
		frame.getStack().push(slot1);
		frame.getStack().push(slot2);

		instructionHandle = instructionHandle.getNext();
		instructionHandle.accept(this);
	}
	// -----------------------------------------------------------------

}
