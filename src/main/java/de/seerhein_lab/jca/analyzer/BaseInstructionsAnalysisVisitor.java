package de.seerhein_lab.jca.analyzer;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.bcel.Repository;
import org.apache.bcel.classfile.JavaClass;
import org.apache.bcel.classfile.Method;
import org.apache.bcel.generic.AALOAD;
import org.apache.bcel.generic.AASTORE;
import org.apache.bcel.generic.ACONST_NULL;
import org.apache.bcel.generic.ANEWARRAY;
import org.apache.bcel.generic.ATHROW;
import org.apache.bcel.generic.ArrayInstruction;
import org.apache.bcel.generic.CHECKCAST;
import org.apache.bcel.generic.CodeExceptionGen;
import org.apache.bcel.generic.ConstantPoolGen;
import org.apache.bcel.generic.GETFIELD;
import org.apache.bcel.generic.GETSTATIC;
import org.apache.bcel.generic.GotoInstruction;
import org.apache.bcel.generic.INVOKEINTERFACE;
import org.apache.bcel.generic.INVOKESPECIAL;
import org.apache.bcel.generic.INVOKESTATIC;
import org.apache.bcel.generic.INVOKEVIRTUAL;
import org.apache.bcel.generic.IfInstruction;
import org.apache.bcel.generic.InstructionHandle;
import org.apache.bcel.generic.InvokeInstruction;
import org.apache.bcel.generic.JsrInstruction;
import org.apache.bcel.generic.LDC;
import org.apache.bcel.generic.LDC2_W;
import org.apache.bcel.generic.LoadInstruction;
import org.apache.bcel.generic.MULTIANEWARRAY;
import org.apache.bcel.generic.MethodGen;
import org.apache.bcel.generic.NEW;
import org.apache.bcel.generic.NEWARRAY;
import org.apache.bcel.generic.PUTFIELD;
import org.apache.bcel.generic.PUTSTATIC;
import org.apache.bcel.generic.ReturnInstruction;
import org.apache.bcel.generic.Select;
import org.apache.bcel.generic.StackInstruction;
import org.apache.bcel.generic.StoreInstruction;
import org.apache.bcel.generic.Type;

import de.seerhein_lab.jca.Frame;
import de.seerhein_lab.jca.Pair;
import de.seerhein_lab.jca.ResultValue;
import de.seerhein_lab.jca.ResultValue.Kind;
import de.seerhein_lab.jca.heap.Array;
import de.seerhein_lab.jca.heap.ClassInstance;
import de.seerhein_lab.jca.heap.ExternalObject;
import de.seerhein_lab.jca.heap.Heap;
import de.seerhein_lab.jca.heap.HeapObject;
import de.seerhein_lab.jca.slot.DoubleSlot;
import de.seerhein_lab.jca.slot.LongSlot;
import de.seerhein_lab.jca.slot.ReferenceSlot;
import de.seerhein_lab.jca.slot.Slot;
import de.seerhein_lab.jca.slot.VoidSlot;
import edu.umd.cs.findbugs.BugCollection;
import edu.umd.cs.findbugs.BugInstance;
import edu.umd.cs.findbugs.SortedBugCollection;
import edu.umd.cs.findbugs.annotations.Confidence;
import edu.umd.cs.findbugs.ba.ClassContext;

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
public abstract class BaseInstructionsAnalysisVisitor extends
		SimpleInstructionsAnalysisVisitor {
	protected static final Logger logger = Logger
			.getLogger("BaseInstructionsAnalysisVisitor");
	protected final ClassContext classContext;
	protected final Method method;

	protected final CodeExceptionGen[] exceptionHandlers;
	protected Set<Pair<InstructionHandle, Boolean>> alreadyVisitedIfBranch;
	protected final Set<Pair<Method, Slot[]>> alreadyVisitedMethods;
	protected SortedBugCollection bugs = new SortedBugCollection();
	protected Set<ResultValue> result = new HashSet<ResultValue>();

	protected abstract BaseInstructionsAnalysisVisitor getInstructionsAnalysisVisitor(
			Frame frame, Set<Pair<InstructionHandle, Boolean>> alreadyVisited,
			InstructionHandle instructionHandle);

	protected abstract BaseMethodAnalyzer getMethodAnalyzer(
			MethodGen targetMethodGen);

	// methods for bug detection
	protected abstract void detectVirtualMethodBug(ReferenceSlot argument);

	protected abstract void detectXAStoreBug(ReferenceSlot arrayReference,
			Slot valueToStore);

	protected abstract void detectPutFieldBug(ReferenceSlot targetReference,
			Slot valueToPut);

	protected abstract void detectPutStaticBug(ReferenceSlot referenceToPut);

	protected abstract String getBugType();

	public BaseInstructionsAnalysisVisitor(ClassContext classContext,
			Method method, Frame frame, ConstantPoolGen constantPoolGen,
			InstructionHandle instructionHandle,
			CodeExceptionGen[] exceptionHandlers,
			Set<Pair<Method, Slot[]>> alreadyVisitedMethods, int depth) {
		this(classContext, method, frame, constantPoolGen,
				new HashSet<Pair<InstructionHandle, Boolean>>(),
				alreadyVisitedMethods, instructionHandle, exceptionHandlers,
				depth);
	}

	protected BaseInstructionsAnalysisVisitor(ClassContext classContext,
			Method method, Frame frame, ConstantPoolGen constantPoolGen,
			Set<Pair<InstructionHandle, Boolean>> alreadyVisited,
			Set<Pair<Method, Slot[]>> alreadyVisitedMethods,
			InstructionHandle instructionHandle,
			CodeExceptionGen[] exceptionHandlers, int depth) {
		super(frame, constantPoolGen, instructionHandle, depth);

		this.classContext = classContext;
		this.method = method;
		this.alreadyVisitedIfBranch = alreadyVisited;
		this.alreadyVisitedMethods = alreadyVisitedMethods;
		this.exceptionHandlers = exceptionHandlers;
	}

	public BugCollection getBugs() {
		return bugs;
	}

	public Set<ResultValue> getResult() {
		return result;
	}

	protected void addBug(Confidence confidence, String message,
			InstructionHandle instructionHandle) {
		BugInstance bugInstance = new BugInstance(getBugType(),
				confidence.getConfidenceValue());

		// param {0} in messages.xml
		bugInstance.addString(message);

		if (classContext != null) {
			bugInstance.addClass(classContext.getJavaClass()).addSourceLine(
					classContext, method, instructionHandle);
		} else {
			bugInstance
					.addClass("de.htwg_konstanz.in.jca.testclasses.UtilsTestClass");
		}
		bugs.add(bugInstance);

	}

	// handle section

	protected void handleException(ReferenceSlot exception) {
		frame.getStack().clear();
		frame.getStack().push(exception);

		for (CodeExceptionGen exceptionHandler : exceptionHandlers) {
			if (BaseMethodAnalyzer.protectsInstruction(exceptionHandler,
					instructionHandle)) {
				logger.log(Level.FINE, indentation + "vvvvv "
						+ exceptionHandler.toString() + ": start vvvvv");
				BaseInstructionsAnalysisVisitor excepHandlerVisitor = getInstructionsAnalysisVisitor(
						new Frame(frame), alreadyVisitedIfBranch,
						exceptionHandler.getHandlerPC());
				exceptionHandler.getHandlerPC().accept(excepHandlerVisitor);
				bugs.addAll(excepHandlerVisitor.getBugs().getCollection());
				result.addAll(excepHandlerVisitor.getResult());
				logger.log(Level.FINE, indentation + "^^^^^ "
						+ exceptionHandler.toString() + ": end ^^^^^");
			}
		}
		result.add(new ResultValue(Kind.EXCEPTION, exception, frame.getHeap()));
	}

	/**
	 * used by visitINVOKESTATIC and visitINVOKESPECIAL
	 * 
	 * @param obj
	 *            node to be visited
	 */
	protected void handleNonVirtualMethod(InvokeInstruction obj) {
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

		ClassAnalyzer targetClassAnalyzer = new ClassAnalyzer(targetClass,
				classContext);
		Method targetMethod = targetClassAnalyzer.getMethod(
				obj.getMethodName(constantPoolGen),
				obj.getArgumentTypes(constantPoolGen));
		MethodGen targetMethodGen = new MethodGen(targetMethod,
				targetClass.getClassName(), new ConstantPoolGen(
						targetClass.getConstantPool()));

		BaseMethodAnalyzer targetMethodAnalyzer = getMethodAnalyzer(targetMethodGen);

		// for detection of recursion
		Pair<Method, Slot[]> thisMethod = new Pair<Method, Slot[]>(
				targetMethod, targetMethodAnalyzer.getActualParams(frame));
		if (!alreadyVisitedMethods.add(thisMethod)) {
			logger.log(Level.FINE, indentation
					+ "Recursion found: Method already analyzed.");
			// if already visited then do not analyze again
			handleVirtualMethod(obj);
			return;
		}

		targetMethodAnalyzer.analyze(frame);

		bugs.addAll(targetMethodAnalyzer.getBugs().getCollection());

		Set<ResultValue> calleeResults = targetMethodAnalyzer.getResult();

		for (ResultValue calleeResult : calleeResults) {
			if (calleeResult.getKind().equals(Kind.REGULAR)) {
				BaseInstructionsAnalysisVisitor specificCalleeResultVisitor = getInstructionsAnalysisVisitor(
						new Frame(frame, calleeResult.getHeap()),
						alreadyVisitedIfBranch, instructionHandle.getNext());

				specificCalleeResultVisitor.frame
						.pushStackByRequiredSlots(calleeResult.getSlot());
				instructionHandle.getNext().accept(specificCalleeResultVisitor);

				bugs.addAll(specificCalleeResultVisitor.getBugs()
						.getCollection());
				result.addAll(specificCalleeResultVisitor.getResult());
			} else {
				Frame savedFrame = new Frame(frame);
				handleException((ReferenceSlot) calleeResult.getSlot());
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
	protected void handleVirtualMethod(InvokeInstruction obj) {
		logger.log(Level.FINE, indentation + obj.toString(false));
		logger.log(Level.FINEST,
				indentation + "\t" + obj.getSignature(constantPoolGen));
		// get number of args
		Type[] type = obj.getArgumentTypes(constantPoolGen);
		// get return value

		Slot argument;
		// pop a value for each arg and 1 for the hidden 'this' reference

		for (int i = 0; i < type.length + 1; i++) {
			argument = frame.popStackByRequiredSlots();
			if (argument instanceof ReferenceSlot) {
				ReferenceSlot reference = (ReferenceSlot) argument;
				// check for bugs
				detectVirtualMethodBug(reference);
				frame.getHeap().publish(frame.getHeap().getObject(reference));
			}
		}

		Slot returnValue = Slot.getDefaultSlotInstance(obj
				.getReturnType(constantPoolGen));

		// return external reference if returnType reference is expected
		if (returnValue instanceof ReferenceSlot)
			returnValue = new ReferenceSlot(frame.getHeap().getExternalObject());

		// works also for void results, because number of required slots = 0
		frame.pushStackByRequiredSlots(returnValue);

		instructionHandle = instructionHandle.getNext();
		instructionHandle.accept(this);
	}

	// ******************************************************************//
	// Visit section //
	// ******************************************************************//

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
	 * 28. ReturnInstruction<br>
	 * Called when a ReturnInstruction occurs. Returns a value (or nothing if
	 * void) from a method.
	 */
	@Override
	public void visitReturnInstruction(ReturnInstruction obj) {
		logger.log(Level.FINE, indentation + obj.toString(false));
		Slot returnType = Slot.getDefaultSlotInstance(obj
				.getType(constantPoolGen));
		logger.log(Level.FINEST, indentation + "\t" + returnType);

		if (returnType instanceof VoidSlot)
			result.add(new ResultValue(Kind.REGULAR, returnType, frame
					.getHeap()));
		else
			result.add(new ResultValue(Kind.REGULAR, frame
					.popStackByRequiredSlots(), frame.getHeap()));
	}

	// ---StackInstruction----------------------------------------------

	@Override
	public void visitStackInstruction(StackInstruction obj) {
		logger.log(Level.FINE, indentation + obj.toString(false));
		Slot slot1, slot2, slot3, slot4;
		switch (obj.getOpcode()) {
		case 0x59:
			// DUB
			slot1 = frame.getStack().pop();

			frame.getStack().push(slot1);
			frame.getStack().push(slot1);
			break;
		case 0x5a:
			// DUB_X1
			slot1 = frame.getStack().pop();
			slot2 = frame.getStack().pop();

			frame.getStack().push(slot1);
			frame.getStack().push(slot2);
			frame.getStack().push(slot1);
			break;
		case 0x5b:
			// DUB_X2
			// pop values
			slot1 = frame.getStack().pop();
			slot2 = frame.getStack().pop();
			slot3 = frame.getStack().pop();

			// push them again (stack: s1, s3, s2, s1)
			frame.getStack().push(slot1);
			frame.getStack().push(slot3);
			frame.getStack().push(slot2);
			frame.getStack().push(slot1);
			break;
		case 0x5c:
			// DUB2
			// pop slots
			slot1 = frame.getStack().pop();
			slot2 = frame.getStack().pop();

			// push them again (stack: s2, s1, s2, s1)
			frame.getStack().push(slot2);
			frame.getStack().push(slot1);
			frame.getStack().push(slot2);
			frame.getStack().push(slot1);
			break;
		case 0x5d:
			// DUB2_X1
			// pop the slots
			slot1 = frame.getStack().pop();
			slot2 = frame.getStack().pop();
			slot3 = frame.getStack().pop();

			// push them again (stack: s2, s1, s3, s2, s1)
			frame.getStack().push(slot2);
			frame.getStack().push(slot1);
			frame.getStack().push(slot3);
			frame.getStack().push(slot2);
			frame.getStack().push(slot1);
			break;
		case 0x5e:
			// DUB2_X2
			// pop the slots
			slot1 = frame.getStack().pop();
			slot2 = frame.getStack().pop();
			slot3 = frame.getStack().pop();
			slot4 = frame.getStack().pop();

			// push them again (stack: s2, s1, s4, s3, s2, s1)
			frame.getStack().push(slot2);
			frame.getStack().push(slot1);
			frame.getStack().push(slot4);
			frame.getStack().push(slot3);
			frame.getStack().push(slot2);
			frame.getStack().push(slot1);
			break;
		case 0x5f:
			// SWAP
			// pop the values
			slot1 = frame.getStack().pop();
			slot2 = frame.getStack().pop();

			// reorder them
			frame.getStack().push(slot1);
			frame.getStack().push(slot2);
			break;
		default:
			// avoid execution of instructions following switch statement for
			// all other opcodes
			return;
		}
		instructionHandle = instructionHandle.getNext();
		instructionHandle.accept(this);
	}

	/**
	 * 1. ACONST_NULL<br>
	 * Called when an ACONST_NULL operation occurs. Pushes a null reference onto
	 * the stack.
	 */
	@Override
	public void visitACONST_NULL(ACONST_NULL obj) {
		logger.log(Level.FINE, indentation + obj.toString(false));

		// push 'null' onto the stack
		frame.getStack().push(ReferenceSlot.getNullReference());

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
		super.visitArrayInstruction(obj);
		switch (obj.getOpcode()) {
		case 0x4f: // iastore
		case 0x50: // lastore
		case 0x51: // fastore
		case 0x52: // dastore
		case 0x54: // bastore
		case 0x55: // castore
		case 0x56: // sastore
			// pop value
			Slot value = frame.popStackByRequiredSlots();
			// pop array index
			frame.getStack().pop();
			// pop array reference
			ReferenceSlot arrayReference = (ReferenceSlot) frame.getStack()
					.pop();

			detectXAStoreBug(arrayReference, value);

			instructionHandle = instructionHandle.getNext();
			instructionHandle.accept(this);
		}
	}

	@Override
	public void visitAALOAD(AALOAD obj) {
		// pop array index
		frame.getStack().pop();
		// pop array reference
		ReferenceSlot arrayReference = (ReferenceSlot) frame.getStack().pop();
		Array array = (Array) frame.getHeap().get(arrayReference.getID());

		for (Iterator<HeapObject> iterator = array.getReferredIterator(); iterator
				.hasNext();) {
			Frame newFrame = new Frame(frame);
			newFrame.getStack().push(new ReferenceSlot(iterator.next()));
			BaseInstructionsAnalysisVisitor visitor = getInstructionsAnalysisVisitor(
					newFrame, alreadyVisitedIfBranch,
					instructionHandle.getNext());
			instructionHandle.getNext().accept(visitor);
			bugs.addAll(visitor.getBugs().getCollection());
			result.addAll(visitor.getResult());
		}
	}

	@Override
	public void visitAASTORE(AASTORE obj) {
		// pop value
		ReferenceSlot value = (ReferenceSlot) frame.popStackByRequiredSlots();
		HeapObject component = frame.getHeap().getObject(value);
		
		// pop array index
		frame.getStack().pop();
		// pop array reference
		ReferenceSlot arrayReference = (ReferenceSlot) frame.getStack().pop();
		
		detectXAStoreBug(arrayReference, value);
	
		Heap heap = frame.getHeap();
		HeapObject array =  heap.getObject(arrayReference);
		
		if ( array instanceof ExternalObject ) 
			heap.publish(component);
		else
			((Array) array).addComponent(component);
				
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
		logger.log(Level.FINE, indentation + obj.toString(false));
		ReferenceSlot exception = (ReferenceSlot) frame.getStack().pop();
		handleException(exception);
	}

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
				+ alreadyVisitedIfBranch.size()
				+ ".else  (condition might be inverted!) ------------------");
		Pair<InstructionHandle, Boolean> elseBranch = new Pair<InstructionHandle, Boolean>(
				instructionHandle, false);
		if (alreadyVisitedIfBranch.add(elseBranch)) {
			Set<Pair<InstructionHandle, Boolean>> newAlreadyVisited = new HashSet<Pair<InstructionHandle, Boolean>>();
			newAlreadyVisited.addAll(alreadyVisitedIfBranch);
			BaseInstructionsAnalysisVisitor elseBranchVisitor = getInstructionsAnalysisVisitor(
					new Frame(frame), newAlreadyVisited,
					instructionHandle.getNext());
			instructionHandle.getNext().accept(elseBranchVisitor);
			bugs.addAll(elseBranchVisitor.getBugs().getCollection());
			result.addAll(elseBranchVisitor.getResult());
		} else {
			logger.log(Level.FINEST, indentation
					+ "Loop detected, do not re-enter.");
		}
		logger.log(Level.FINEST, indentation + "------------------  "
				+ alreadyVisitedIfBranch.size()
				+ ".then  (condition might be inverted!) ------------------");
		Pair<InstructionHandle, Boolean> thenBranch = new Pair<InstructionHandle, Boolean>(
				instructionHandle, true);
		if (alreadyVisitedIfBranch.add(thenBranch)) {

			Set<Pair<InstructionHandle, Boolean>> newAlreadyVisited = new HashSet<Pair<InstructionHandle, Boolean>>();
			newAlreadyVisited.addAll(alreadyVisitedIfBranch);

			BaseInstructionsAnalysisVisitor thenBranchVisitor = getInstructionsAnalysisVisitor(
					new Frame(frame), newAlreadyVisited, obj.getTarget());
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
		frame.getStack().push(
				new ReferenceSlot(frame.getHeap().newClassInstance()));
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
			caseToFollow = getInstructionsAnalysisVisitor(new Frame(frame),
					alreadyVisitedIfBranch, targets[i]);
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
		caseToFollow = getInstructionsAnalysisVisitor(new Frame(frame),
				alreadyVisitedIfBranch, obj.getTarget());
		obj.getTarget().accept(caseToFollow);
		// adding occurred bugs to bug-collection
		bugs.addAll(caseToFollow.getBugs().getCollection());
		// adding result of the case to a result-list
		result.addAll(caseToFollow.getResult());
	}

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

		// pops length,
		frame.getStack().pop();

		// pushes new array reference
		frame.getStack().push(new ReferenceSlot(frame.getHeap().newArray()));

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
		ReferenceSlot objRef = (ReferenceSlot) frame.getStack().pop();
		// check type of popped object reference

		// 1st case: type cast is valid, continue execution in a separate
		// visitor
		BaseInstructionsAnalysisVisitor regularCaseVisitor = getInstructionsAnalysisVisitor(
				new Frame(frame), alreadyVisitedIfBranch,
				instructionHandle.getNext());

		regularCaseVisitor.frame.getStack().push(objRef);

		logger.log(
				Level.FINEST,
				indentation + "\t" + objRef + " ?= "
						+ obj.getLoadClassType(constantPoolGen));
		instructionHandle.getNext().accept(regularCaseVisitor);

		bugs.addAll(regularCaseVisitor.getBugs().getCollection());
		result.addAll(regularCaseVisitor.getResult());

		// 2nd case: type cast is invalid, throw ClassCastException
		handleException(new ReferenceSlot(frame.getHeap().newClassInstance()));
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
		// Notation: gets o.f

		// pop object reference
		ReferenceSlot o = (ReferenceSlot) frame.getStack().pop();

		// obj.getSignature() refers to desired field
		Slot f = Slot.getDefaultSlotInstance(obj.getType(constantPoolGen));
		if (f instanceof ReferenceSlot) {
			if (o.getID().equals(frame.getHeap().getExternalObject().getId())) {
				// if left side is external return external
				f = new ReferenceSlot(frame.getHeap().getExternalObject());
			} else {
				// get the ClassInstance linked to the desired field
				f = new ReferenceSlot(((ClassInstance) frame.getHeap().get(
						o.getID())).getField(obj.getFieldName(constantPoolGen)));
			}
		}
		frame.pushStackByRequiredSlots(f);

		logger.log(
				Level.FINEST,
				indentation + "\t" + o + "."
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
		// Notation: gets f

		StringBuilder log = new StringBuilder();
		log.append(indentation + "\t");
		log.append(obj.getLoadClassType(constantPoolGen) + "."
				+ obj.getFieldName(constantPoolGen) + " (");

		Slot f = Slot.getDefaultSlotInstance(obj.getFieldType(constantPoolGen));
		// if a reference is expected
		if (f instanceof ReferenceSlot) {
			// static values are always external
			f = new ReferenceSlot(frame.getHeap().getExternalObject());
		}

		log.append((f instanceof DoubleSlot || f instanceof LongSlot) ? f
				+ ", " + f : f);
		frame.pushStackByRequiredSlots(f);
		log.append(")");
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
		// Notation: puts o.f = v

		// right side of assignment
		Slot vRef = frame.popStackByRequiredSlots();

		// pop left side of assignment off the stack, too
		ReferenceSlot oRef = (ReferenceSlot) frame.getStack().pop();
		detectPutFieldBug(oRef, vRef);
		if (vRef instanceof ReferenceSlot) {
			Heap heap = frame.getHeap();
			HeapObject v = heap.getObject((ReferenceSlot)vRef);
			HeapObject o = heap.getObject(oRef);
			
			if ( o instanceof ExternalObject ) 
				heap.publish(v);
			else
				((ClassInstance) o).setField(obj.getFieldName(constantPoolGen), v);
		}

		logger.log(
				Level.FINEST,
				indentation
						+ oRef
						+ "."
						+ obj.getFieldName(constantPoolGen)
						+ " <--"
						+ ((vRef instanceof DoubleSlot || vRef instanceof LongSlot) ? vRef
								+ ", " + vRef
								: vRef));

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
		// Notation: f = v

		// popping value from stack
		Slot v = frame.popStackByRequiredSlots();

		// a reference is assigned to a static field
		if (v instanceof ReferenceSlot) {
			detectPutStaticBug((ReferenceSlot) v);
			// make it external
			frame.getHeap().publish(
					frame.getHeap().getObject((ReferenceSlot) v));
		}

		// write log
		String log = "\t" + obj.getReferenceType(constantPoolGen) + "."
				+ obj.getName(constantPoolGen) + " <-- "
				+ ((v.getNumSlots() == 2) ? v + ", " + v : v);

		logger.log(Level.FINEST, indentation + log);

		instructionHandle = instructionHandle.getNext();
		instructionHandle.accept(this);
	}

	/**
	 * 10. CPInstruction <br>
	 * 10.4. InvokeInstruction <br>
	 * 10.4.1. INVOKEINTERFACE <br>
	 */
	@Override
	public void visitINVOKEINTERFACE(INVOKEINTERFACE obj) {
		handleVirtualMethod(obj);
	}

	/**
	 * 10. CPInstruction <br>
	 * 10.4. InvokeInstruction <br>
	 * 10.4.2. INVOKESPECIAL <br>
	 */
	@Override
	public void visitINVOKESPECIAL(INVOKESPECIAL obj) {
		handleNonVirtualMethod(obj);
	}

	/**
	 * 10. CPInstruction <br>
	 * 10.4. InvokeInstruction <br>
	 * 10.4.3. INVOKESTATIC <br>
	 */
	@Override
	public void visitINVOKESTATIC(INVOKESTATIC obj) {
		handleNonVirtualMethod(obj);
	}

	/**
	 * 10. CPInstruction <br>
	 * 10.4. InvokeInstruction <br>
	 * 10.4.4. INVOKEVIRTUAL <br>
	 */
	@Override
	public void visitINVOKEVIRTUAL(INVOKEVIRTUAL obj) {
		handleVirtualMethod(obj);
	}

	/**
	 * 10. CPInstruction <br>
	 * 10.6. LDC <br>
	 */
	@Override
	public void visitLDC(LDC obj) {
		logger.log(Level.FINE, indentation + obj.toString(false));
		Slot value = Slot.getDefaultSlotInstance(obj.getType(constantPoolGen));
		// pushes an integer, a float, a long, a double or a String
		// (notThis) onto the stack
		if (value instanceof ReferenceSlot) {
			// it is a String
			value = new ReferenceSlot(frame.getHeap().newClassInstance());
		}
		frame.pushStackByRequiredSlots(value);

		logger.log(Level.FINEST, indentation + "\t" + value);

		instructionHandle = instructionHandle.getNext();
		instructionHandle.accept(this);
	}

	/**
	 * 10. CPInstruction <br>
	 * 10.7 LDC2_W <br>
	 */
	@Override
	public void visitLDC2_W(LDC2_W obj) {
		logger.log(Level.FINE, indentation + obj.toString(false));
		Slot value = Slot.getDefaultSlotInstance(obj.getType(constantPoolGen));
		// pushes an integer, a float, a long, a double or a String
		// (notThis) onto the stack
		if (value instanceof ReferenceSlot) {
			// it is a String
			value = new ReferenceSlot(frame.getHeap().newClassInstance());
		}
		frame.pushStackByRequiredSlots(value);

		logger.log(Level.FINEST, indentation + "\t" + value);

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

		ReferenceSlot slot = null;
		Array array = null;

		for (int i = 0; i < obj.consumeStack(constantPoolGen); i++) {
			// pop count values for each dimension
			frame.getStack().pop();

			Array newArray = frame.getHeap().newArray();

			if (i == 0) {
				slot = new ReferenceSlot(newArray);
			} else {
				array.addComponent(newArray);
			}
			array = newArray;
		}

		// push array reference onto stack
		frame.getStack().push(slot);

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

		ClassInstance instance = frame.getHeap().newClassInstance();
		ReferenceSlot slot = new ReferenceSlot(instance);

		frame.getStack().push(slot);

		instructionHandle = instructionHandle.getNext();
		instructionHandle.accept(this);
	}

	/**
	 * 25. NEWARRAY<br>
	 * Called when a NEWARRAY operation occurs. Creates a new array with
	 * primitive types. The length is popped as an integer value from the stack
	 * and the type is identified by a type byte.
	 */
	@Override
	public void visitNEWARRAY(NEWARRAY obj) {
		logger.log(Level.FINE, indentation + obj.toString(false));
		logger.log(
				Level.FINEST,
				indentation + "\t" + "("
						+ Slot.getDefaultSlotInstance(obj.getType()) + ")");

		// pop length of new array (integer)
		frame.getStack().pop();

		// push reference to new array onto the stack
		ReferenceSlot slot = new ReferenceSlot(frame.getHeap().newArray());

		frame.getStack().push(slot);

		instructionHandle = instructionHandle.getNext();
		instructionHandle.accept(this);
	}
}
