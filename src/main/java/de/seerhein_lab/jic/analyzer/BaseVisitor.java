package de.seerhein_lab.jic.analyzer;

import static org.apache.bcel.Constants.CONSTRUCTOR_NAME;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
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
import org.apache.bcel.generic.BasicType;
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

import de.seerhein_lab.jic.Pair;
import de.seerhein_lab.jic.ResultValue;
import de.seerhein_lab.jic.ResultValue.Kind;
import de.seerhein_lab.jic.Utils;
import de.seerhein_lab.jic.analyzer.eval.EvaluationOnlyAnalyzer;
import de.seerhein_lab.jic.analyzer.recursion.RecursionAnalyzer;
import de.seerhein_lab.jic.cache.AnalysisCache;
import de.seerhein_lab.jic.cache.AnalysisCache.Check;
import de.seerhein_lab.jic.cache.AnalysisResult;
import de.seerhein_lab.jic.cache.AnalyzedMethod;
import de.seerhein_lab.jic.slot.DoubleSlot;
import de.seerhein_lab.jic.slot.LongSlot;
import de.seerhein_lab.jic.slot.ReferenceSlot;
import de.seerhein_lab.jic.slot.Slot;
import de.seerhein_lab.jic.slot.VoidSlot;
import de.seerhein_lab.jic.vm.Array;
import de.seerhein_lab.jic.vm.ClassInstance;
import de.seerhein_lab.jic.vm.ExternalObject;
import de.seerhein_lab.jic.vm.Frame;
import de.seerhein_lab.jic.vm.Heap;
import de.seerhein_lab.jic.vm.HeapObject;
import de.seerhein_lab.jic.vm.OpStack;
import de.seerhein_lab.jic.vm.PC;
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
public abstract class BaseVisitor extends SimpleVisitor {

	private enum Staticality {
		STATIC, NONSTATIC
	};

	protected static final Logger logger = Logger.getLogger("BaseInstructionsAnalysisVisitor");
	protected final ClassContext classContext;
	protected final MethodGen methodGen;

	protected final CodeExceptionGen[] exceptionHandlers;
	protected Set<Pair<InstructionHandle, Boolean>> alreadyVisitedIfBranch;
	protected final Set<MethodInvocation> alreadyVisitedMethods;
	protected SortedBugCollection bugs = new SortedBugCollection();
	protected final AnalysisCache cache;
	protected Set<ResultValue> result = new HashSet<ResultValue>();
	public static long cacheMisses = 0;
	public static long cacheHits = 0;

	// protected abstract BaseVisitor getInstructionsAnalysisVisitor(
	// Frame frame, Heap heap,
	// Set<Pair<InstructionHandle, Boolean>> alreadyVisited,
	// InstructionHandle instructionHandle);

	protected abstract BaseMethodAnalyzer getMethodAnalyzer(MethodGen targetMethodGen,
			Set<MethodInvocation> alreadyVisitedMethods);

	// methods for bug detection
	protected abstract void detectVirtualMethodBug(ReferenceSlot argument);

	protected abstract void detectXAStoreBug(ReferenceSlot arrayReference, Slot valueToStore);

	protected abstract void detectPutFieldBug(ReferenceSlot targetReference, Slot valueToPut);

	protected abstract void detectPutStaticBug(ReferenceSlot referenceToPut);

	// public BaseVisitor(ClassContext classContext, Method method,
	// Frame frame, Heap heap, ConstantPoolGen constantPoolGen,
	// InstructionHandle instructionHandle,
	// CodeExceptionGen[] exceptionHandlers,
	// Set<Pair<Method, Slot[]>> alreadyVisitedMethods, int depth) {
	// this(classContext, method, frame, heap, constantPoolGen,
	// new HashSet<Pair<InstructionHandle, Boolean>>(),
	// alreadyVisitedMethods, instructionHandle, exceptionHandlers,
	// depth);
	// }

	protected BaseVisitor(ClassContext classContext, MethodGen methodGen, Frame frame, Heap heap,
			ConstantPoolGen constantPoolGen, Set<Pair<InstructionHandle, Boolean>> alreadyVisited,
			Set<MethodInvocation> alreadyVisitedMethods, PC pc,
			CodeExceptionGen[] exceptionHandlers, int depth, AnalysisCache cache) {
		super(frame, heap, constantPoolGen, pc, depth);

		this.classContext = classContext;
		this.methodGen = methodGen;
		this.alreadyVisitedIfBranch = alreadyVisited;
		this.alreadyVisitedMethods = alreadyVisitedMethods;
		this.exceptionHandlers = exceptionHandlers;
		this.cache = cache;
	}

	public BugCollection getBugs() {
		return bugs;
	}

	public Set<ResultValue> getResult() {
		return result;
	}

	protected void addBug(Confidence confidence, String message, InstructionHandle instructionHandle) {
		BugInstance bugInstance = Utils.createBug(confidence, message, classContext.getJavaClass());

		bugInstance.addSourceLine(classContext, methodGen.getMethod(), instructionHandle);
		bugs.add(bugInstance);

	}

	// handle section

	protected void handleException(ReferenceSlot exception) {
		frame.getStack().clear();
		frame.getStack().push(exception);

		for (CodeExceptionGen exceptionHandler : exceptionHandlers) {
			if (MethodHelper.protectsInstruction(exceptionHandler, pc.getCurrentInstruction())) {
				logger.log(Level.FINE, indentation + "vvvvv " + exceptionHandler.toString()
						+ ": start vvvvv");

				// ************
				BaseMethodAnalyzer analyzer = getMethodAnalyzer(methodGen, alreadyVisitedMethods);
				analyzer.analyze(exceptionHandler.getHandlerPC(), new Frame(frame), new Heap(heap),
						alreadyVisitedIfBranch);

				bugs.addAll(analyzer.getBugs());
				result.addAll(analyzer.getResult());
				logger.log(Level.FINE, indentation + "^^^^^ " + exceptionHandler.toString()
						+ ": end ^^^^^");

			}
		}
		result.add(new ResultValue(Kind.EXCEPTION, exception, heap));
		pc.invalidate();
	}

	protected abstract Check getCheck();

	private Set<ResultValue> useCachedResults(AnalyzedMethod method) {
		Set<ResultValue> targetResults;
		targetResults = new HashSet<ResultValue>();

		ReferenceSlot topOfStack = (ReferenceSlot) frame.getStack().pop();

		for (ResultValue resultValue : cache.get(method).getResults()) {
			Heap resultHeap = new Heap(heap);
			HeapObject resultObject = resultValue.getHeap().getObject(
					(ReferenceSlot) resultValue.getSlot());

			if (resultValue.getKind().equals(ResultValue.Kind.EXCEPTION)) {
				targetResults.add(new ResultValue(resultValue.getKind(), ReferenceSlot
						.createNewInstance((ClassInstance) resultObject.deepCopy(resultHeap)),
						resultHeap));
			} else {
				((ClassInstance) resultHeap.getObject((ReferenceSlot) topOfStack))
						.copyReferredObjectsTo(resultObject, resultHeap);
				targetResults.add(new ResultValue(ResultValue.Kind.REGULAR, VoidSlot.getInstance(),
						resultHeap));
			}
		}
		return targetResults;
	}

	private void cacheResults(MethodGen targetMethodGen, AnalyzedMethod method,
			Collection<BugInstance> targetBugs, Set<ResultValue> targetResults, Slot firstParam) {
		logger.log(Level.FINE,
				Utils.formatLoggingOutput(this.depth) + "Put " + targetMethodGen.getClassName()
						+ targetMethodGen.getMethod().getName() + " in the Cache");

		AnalysisResult result = new AnalysisResult(targetResults, firstParam);
		result.setBugs(getCheck(), targetBugs);
		cache.add(method, result, getCheck());
	}

	private void wrapNestedBugs(JavaClass targetClass, Method targetMethod,
			Collection<BugInstance> targetBugs) {
		for (Iterator<BugInstance> it = targetBugs.iterator(); it.hasNext();) {
			BugInstance bug = it.next();

			if (targetClass.equals(classContext.getJavaClass())) {
				bugs.add(bug);
			}

			addBug(Confidence.HIGH,
					"subsequent bug caused by [" + bug.getMessage() + " in "
							+ targetClass.getClassName() + "." + targetMethod.getName()
							+ targetMethod.getSignature() + ":"
							+ bug.getPrimarySourceLineAnnotation().getStartLine() + "]",
					pc.getCurrentInstruction());
		}
	}

	private void continueWithResults(Set<ResultValue> targetResults) {
		for (ResultValue calleeResult : targetResults) {
			if (calleeResult.getKind().equals(Kind.REGULAR)) {

				BaseMethodAnalyzer analyzer = getMethodAnalyzer(methodGen, alreadyVisitedMethods);

				Frame newFrame = new Frame(frame);
				newFrame.pushStackByRequiredSlots(calleeResult.getSlot());

				analyzer.analyze(pc.getCurrentInstruction().getNext(), newFrame,
						calleeResult.getHeap(), alreadyVisitedIfBranch);

				bugs.addAll(analyzer.getBugs());
				result.addAll(analyzer.getResult());

			} else {
				Frame savedFrame = new Frame(frame);
				InstructionHandle currentInstruction = pc.getCurrentInstruction();
				handleException((ReferenceSlot) calleeResult.getSlot());
				pc.setInstruction(currentInstruction);
				frame = savedFrame;
			}
		}
	}

	private void analyzeMethod(InvokeInstruction obj, JavaClass targetClass, Method targetMethod) {
		logger.log(Level.FINE, indentation + obj.toString(false));
		logger.log(Level.FINEST, indentation + "\t" + obj.getLoadClassType(constantPoolGen) + "."
				+ obj.getMethodName(constantPoolGen) + obj.getSignature(constantPoolGen));

		MethodGen targetMethodGen = new MethodGen(targetMethod, targetClass.getClassName(),
				new ConstantPoolGen(targetClass.getConstantPool()));

		MethodInvocation invocation = new MethodInvocation(targetClass, targetMethod);

		if (alreadyVisitedMethods.contains(invocation)) {
			handleRecursion(obj, targetMethodGen);
			return;
		}

		Set<MethodInvocation> nowVisitedMethods = new HashSet<MethodInvocation>();
		nowVisitedMethods.addAll(alreadyVisitedMethods);
		nowVisitedMethods.add(invocation);

		AnalyzedMethod method = new AnalyzedMethod(targetMethodGen.getClassName(),
				targetMethodGen.getMethod());

		Collection<BugInstance> targetBugs = null;
		Set<ResultValue> targetResults = null;

		if (cache.contains(method) && cache.get(method).isCached(getCheck())) {
			logger.log(Level.FINE, Utils.formatLoggingOutput(this.depth) + method
					+ " already evaluated - taking result out of the cache");
			cacheHits++;

			targetBugs = cache.get(method).getBugs(getCheck());
			targetResults = useCachedResults(method);

		} else {
			cacheMisses++;
			Slot firstParam = frame.getStack().size() == 0 ? null : new OpStack(frame.getStack())
					.pop();

			BaseMethodAnalyzer targetMethodAnalyzer;

			if (targetMethod.getName().equals(CONSTRUCTOR_NAME)
					&& targetMethod.getArgumentTypes().length == 0
					&& firstParam instanceof ReferenceSlot
					&& !heap.getObject(((ReferenceSlot) firstParam)).equals(heap.getThisInstance())) {

				targetMethodAnalyzer = new EvaluationOnlyAnalyzer(classContext, targetMethodGen,
						alreadyVisitedMethods, depth, cache);
			} else {
				targetMethodAnalyzer = getMethodAnalyzer(targetMethodGen, nowVisitedMethods);
			}

			targetMethodAnalyzer.analyze(frame.getStack(), heap);

			targetBugs = targetMethodAnalyzer.getBugs();
			targetResults = targetMethodAnalyzer.getResult();

			if (targetMethodGen.getMethod().getName().equals(CONSTRUCTOR_NAME)
					&& targetMethodGen.getMethod().getArgumentTypes().length == 0) {
				cacheResults(targetMethodGen, method, targetBugs, targetResults, firstParam);
			}
		}

		wrapNestedBugs(targetClass, targetMethod, targetBugs);
		continueWithResults(targetResults);

		pc.invalidate();
	}

	protected void handleRecursion(InvokeInstruction obj, MethodGen targetMethodGen) {
		logger.log(Level.FINE, indentation + "Recursion found: Get result of recursive call.");

		Type returnType = obj.getReturnType(constantPoolGen);
		if (returnType instanceof BasicType) {
			handleSimpleInstruction(obj, Slot.getDefaultSlotInstance(returnType));
			logger.log(
					Level.FINE,
					indentation + "Result of recursive call: "
							+ Slot.getDefaultSlotInstance(returnType));
			return;
		}

		BaseMethodAnalyzer recursionAnalyzer = new RecursionAnalyzer(classContext, targetMethodGen,
				alreadyVisitedMethods, depth, cache);

		recursionAnalyzer.analyze(new OpStack(frame.getStack()), heap);
		Set<ResultValue> recursionResults = recursionAnalyzer.getResult();

		Frame currentFrame = frame;
		Heap currentHeap = heap;

		for (ResultValue res : recursionResults) {
			logger.log(Level.FINE, indentation + "Result of recursive call: " + res.getSlot());
			frame = new Frame(currentFrame);
			heap = new Heap(currentHeap);

			BaseMethodAnalyzer analyzer = getMethodAnalyzer(methodGen, alreadyVisitedMethods);
			handleSimpleInstruction(obj, res.getSlot());
			analyzer.analyze(pc.getCurrentInstruction().getNext(), new Frame(currentFrame),
					new Heap(currentHeap), alreadyVisitedIfBranch);

			bugs.addAll(analyzer.getBugs());
			result.addAll(analyzer.getResult());
		}

		pc.invalidate();
		return;
	}

	private void dontAnalyzeMethod(InvokeInstruction obj, Staticality staticality) {
		logger.log(Level.FINE, indentation + obj.toString(false));
		logger.log(Level.FINEST, indentation + "\t" + obj.getLoadClassType(constantPoolGen) + "."
				+ obj.getMethodName(constantPoolGen) + obj.getSignature(constantPoolGen));

		// get number of args
		Type[] type = obj.getArgumentTypes(constantPoolGen);
		// get return value

		Slot argument;
		// pop a value for each arg and 1 for the hidden 'this' reference

		int arguments = (staticality == Staticality.STATIC) ? type.length : type.length + 1;

		for (int i = 0; i < arguments; i++) {
			argument = frame.popStackByRequiredSlots();
			if (argument instanceof ReferenceSlot) {
				ReferenceSlot reference = (ReferenceSlot) argument;
				// check for bugs
				detectVirtualMethodBug(reference);
				heap.publish(heap.getObject(reference));
			}
		}

		Slot returnValue = Slot.getDefaultSlotInstance(obj.getReturnType(constantPoolGen));

		// return external reference if returnType reference is expected
		if (returnValue instanceof ReferenceSlot)
			returnValue = ReferenceSlot.createNewInstance(heap.getExternalObject());

		// works also for void results, because number of required slots = 0
		frame.pushStackByRequiredSlots(returnValue);

		pc.advance();
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
		pc.advance();
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

		pc.advance();
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
		Slot returnType = Slot.getDefaultSlotInstance(obj.getType(constantPoolGen));
		logger.log(Level.FINEST, indentation + "\t" + returnType);

		if (returnType instanceof VoidSlot)
			result.add(new ResultValue(Kind.REGULAR, returnType, heap));
		else
			result.add(new ResultValue(Kind.REGULAR, frame.popStackByRequiredSlots(), heap));
		pc.invalidate();
	}

	// ---StackInstruction----------------------------------------------

	@Override
	public void visitStackInstruction(StackInstruction obj) {
		Slot slot1, slot2, slot3, slot4;
		switch (obj.getOpcode()) {
		case 0x59:
			// DUB
			logger.log(Level.FINE, indentation + obj.toString(false));
			slot1 = frame.getStack().pop();

			frame.getStack().push(slot1);
			frame.getStack().push(slot1);
			break;
		case 0x5a:
			// DUB_X1
			logger.log(Level.FINE, indentation + obj.toString(false));
			slot1 = frame.getStack().pop();
			slot2 = frame.getStack().pop();

			frame.getStack().push(slot1);
			frame.getStack().push(slot2);
			frame.getStack().push(slot1);
			break;
		case 0x5b:
			// DUB_X2
			// pop values
			logger.log(Level.FINE, indentation + obj.toString(false));
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
			logger.log(Level.FINE, indentation + obj.toString(false));
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
			logger.log(Level.FINE, indentation + obj.toString(false));
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
			logger.log(Level.FINE, indentation + obj.toString(false));
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
			logger.log(Level.FINE, indentation + obj.toString(false));
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
		pc.advance();
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

		pc.advance();
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
			ReferenceSlot arrayReference = (ReferenceSlot) frame.getStack().pop();

			detectXAStoreBug(arrayReference, value);

			pc.advance();
		}
	}

	@Override
	public void visitAALOAD(AALOAD obj) {
		logger.log(Level.FINE, indentation + obj.toString(false));

		// pop array index
		frame.getStack().pop();
		// pop array reference
		ReferenceSlot arrayReference = (ReferenceSlot) frame.getStack().pop();

		// CAUTION: try to make this also work with external object

		if (heap.getObject(arrayReference) instanceof ExternalObject) {
			frame.getStack().push(ReferenceSlot.createNewInstance(heap.getExternalObject()));
			pc.advance();
			return;
		}

		// END CAUTION: try to make this also work with external object

		Array array = (Array) heap.getObject(arrayReference);

		if (array == null) {
			logger.log(
					Level.WARNING,
					"AALOAD on null in "
							+ classContext.getFullyQualifiedMethodName(methodGen.getMethod())
							+ " - stopping evaluation of an unreachable path");
			pc.invalidate();
			return;
			// throw new AssertionError("AALOAD on null in "
			// +
			// classContext.getFullyQualifiedMethodName(methodGen.getMethod()));
		}

		for (Iterator<HeapObject> iterator = array.getReferredIterator(); iterator.hasNext();) {

			// ************
			BaseMethodAnalyzer analyzer = getMethodAnalyzer(methodGen, alreadyVisitedMethods);

			Frame newFrame = new Frame(frame);
			newFrame.getStack().push(ReferenceSlot.createNewInstance(iterator.next()));

			analyzer.analyze(pc.getCurrentInstruction().getNext(), newFrame, new Heap(heap),
					alreadyVisitedIfBranch);

			bugs.addAll(analyzer.getBugs());
			result.addAll(analyzer.getResult());

			// ************

			// Frame newFrame = new Frame(frame);
			// newFrame.getStack().push(
			// ReferenceSlot.createNewInstance(iterator.next()));
			// BaseVisitor visitor = getInstructionsAnalysisVisitor(
			// newFrame, new Heap(heap), alreadyVisitedIfBranch,
			// instructionHandle.getNext());
			// instructionHandle.getNext().accept(visitor);
			// bugs.addAll(visitor.getBugs().getCollection());
			// result.addAll(visitor.getResult());
		}
		pc.invalidate();
	}

	@Override
	public void visitAASTORE(AASTORE obj) {
		logger.log(Level.FINE, indentation + obj.toString(false));

		// pop value
		ReferenceSlot value = (ReferenceSlot) frame.popStackByRequiredSlots();
		HeapObject component = heap.getObject(value);

		// pop array index
		frame.getStack().pop();
		// pop array reference
		ReferenceSlot arrayReference = (ReferenceSlot) frame.getStack().pop();

		detectXAStoreBug(arrayReference, value);

		HeapObject array = heap.getObject(arrayReference);

		if (array instanceof ExternalObject)
			heap.publish(component);
		else
			((Array) array).addComponent(component);

		pc.advance();
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
		pc.setInstruction(obj.getTarget());
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
		boolean analyzeElseBranch = true;
		boolean analyzeThenBranch = true;

		// ifnull / ifnonnull
		if (obj.getOpcode() == 0xc6 || obj.getOpcode() == 0xc7) {
			ReferenceSlot refSlot = (ReferenceSlot) frame.getStack().pop();
			if (obj.getOpcode() == 0xc6) { // ifnull
				analyzeThenBranch = refSlot.isNullReference();
				analyzeElseBranch = !refSlot.isNullReference();
			}
			if (obj.getOpcode() == 0xc7) { // ifnonnull
				analyzeThenBranch = !refSlot.isNullReference();
				analyzeElseBranch = refSlot.isNullReference();
			}
		} else {
			for (int i = 0; i < obj.consumeStack(constantPoolGen); i++) {
				frame.getStack().pop();
			}
		}

		if (analyzeElseBranch) {
			logger.log(Level.FINEST,
					indentation + "------------------  " + alreadyVisitedIfBranch.size()
							+ ".else  (condition might be inverted!) ------------------");
			Pair<InstructionHandle, Boolean> elseBranch = new Pair<InstructionHandle, Boolean>(
					pc.getCurrentInstruction(), false);

			if (alreadyVisitedIfBranch.add(elseBranch)) {
				// Set<Pair<InstructionHandle, Boolean>> newAlreadyVisited = new
				// HashSet<Pair<InstructionHandle, Boolean>>();
				// newAlreadyVisited.addAll(alreadyVisitedIfBranch);

				// ****************************
				BaseMethodAnalyzer elseAnalyzer = getMethodAnalyzer(methodGen,
						alreadyVisitedMethods);

				// elseAnalyzer.analyze(pc.getCurrentInstruction().getNext(),
				// new Frame(frame), new Heap(heap), newAlreadyVisited);

				elseAnalyzer.analyze(pc.getCurrentInstruction().getNext(), new Frame(frame),
						new Heap(heap), alreadyVisitedIfBranch);

				bugs.addAll(elseAnalyzer.getBugs());
				result.addAll(elseAnalyzer.getResult());
				// ****************************

				// BaseVisitor elseBranchVisitor =
				// getInstructionsAnalysisVisitor(
				// new Frame(frame), new Heap(heap), newAlreadyVisited,
				// instructionHandle.getNext());
				// instructionHandle.getNext().accept(elseBranchVisitor);
				// bugs.addAll(elseBranchVisitor.getBugs().getCollection());
				// result.addAll(elseBranchVisitor.getResult());
			} else {
				logger.log(Level.FINEST, indentation + "Loop detected, do not re-enter.");
			}
		}

		if (analyzeThenBranch) {
			logger.log(Level.FINEST,
					indentation + "------------------  " + alreadyVisitedIfBranch.size()
							+ ".then  (condition might be inverted!) ------------------");
			Pair<InstructionHandle, Boolean> thenBranch = new Pair<InstructionHandle, Boolean>(
					pc.getCurrentInstruction(), true);

			if (alreadyVisitedIfBranch.add(thenBranch)) {
				// Set<Pair<InstructionHandle, Boolean>> newAlreadyVisited = new
				// HashSet<Pair<InstructionHandle, Boolean>>();
				// newAlreadyVisited.addAll(alreadyVisitedIfBranch);

				// ****************************
				BaseMethodAnalyzer thenAnalyzer = getMethodAnalyzer(methodGen,
						alreadyVisitedMethods);

				// thenAnalyzer.analyze(obj.getTarget(), new Frame(frame), new
				// Heap(
				// heap), newAlreadyVisited);

				thenAnalyzer.analyze(obj.getTarget(), new Frame(frame), new Heap(heap),
						alreadyVisitedIfBranch);

				bugs.addAll(thenAnalyzer.getBugs());
				result.addAll(thenAnalyzer.getResult());
				// ****************************

				// BaseVisitor thenBranchVisitor =
				// getInstructionsAnalysisVisitor(
				// new Frame(frame), new Heap(heap), newAlreadyVisited,
				// obj.getTarget());
				// obj.getTarget().accept(thenBranchVisitor);
				// bugs.addAll(thenBranchVisitor.getBugs().getCollection());
				// result.addAll(thenBranchVisitor.getResult());
			} else {
				logger.log(Level.FINEST, indentation + "Loop detected, do not re-enter.");
			}
		}
		pc.invalidate();
	}

	/**
	 * 7. BranchInstruction<br>
	 * 7.3. JsrInstruction<br>
	 * Called when a JsrInstruction operation occurs.
	 */
	@Override
	public void visitJsrInstruction(JsrInstruction obj) {
		logger.log(Level.FINE, indentation + obj.toString(false));
		logger.log(Level.WARNING, "Untested Code Warning: Executing JSR instruction");
		bugs.add(new BugInstance("Untested Code Warning: Executing JSR instruction", 1));
		frame.getStack().push(ReferenceSlot.createNewInstance(heap.newClassInstance()));

		// TODO: Broken with introduction of PC!

		pc.setInstruction(obj.getTarget());

		// InstructionHandle savedInstructionHandle = instructionHandle;
		// instructionHandle = obj.getTarget();
		// instructionHandle.accept(this);
		//
		// instructionHandle = savedInstructionHandle.getNext();
		// instructionHandle.accept(this);
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
		// follows all targets excluding the default case
		for (int i = 0; i < targets.length; i++) {
			logger.log(Level.FINEST,
					indentation + "--------------- Line " + targets[i].getPosition()
							+ " ---------------");

			// ****************************
			BaseMethodAnalyzer caseAnalyzer = getMethodAnalyzer(methodGen, alreadyVisitedMethods);

			caseAnalyzer.analyze(targets[i], new Frame(frame), new Heap(heap),
					alreadyVisitedIfBranch);

			bugs.addAll(caseAnalyzer.getBugs());
			result.addAll(caseAnalyzer.getResult());
			// ****************************

			// caseToFollow = getInstructionsAnalysisVisitor(new Frame(frame),
			// new Heap(heap), alreadyVisitedIfBranch, targets[i]);
			// targets[i].accept(caseToFollow);
			// // adding occurred bugs to bug-collection
			// bugs.addAll(caseToFollow.getBugs().getCollection());
			// // adding result of the case to a result-list
			// result.addAll(caseToFollow.getResult());
		}
		// handles the default case and follows it
		logger.log(Level.FINEST, indentation + "--------------- Line "
				+ obj.getTarget().getPosition() + " (DefaultCase) ---------------");
		// NOTE: If the keyword "Default:" is not in the switch the following
		// target is the end of the switch without executing a case.

		// ****************************
		BaseMethodAnalyzer defaultAnalyzer = getMethodAnalyzer(methodGen, alreadyVisitedMethods);

		defaultAnalyzer.analyze(obj.getTarget(), new Frame(frame), new Heap(heap),
				alreadyVisitedIfBranch);

		bugs.addAll(defaultAnalyzer.getBugs());
		result.addAll(defaultAnalyzer.getResult());
		// ****************************

		// caseToFollow = getInstructionsAnalysisVisitor(new Frame(frame),
		// new Heap(heap), alreadyVisitedIfBranch, obj.getTarget());
		// obj.getTarget().accept(caseToFollow);
		// // adding occurred bugs to bug-collection
		// bugs.addAll(caseToFollow.getBugs().getCollection());
		// // adding result of the case to a result-list
		// result.addAll(caseToFollow.getResult());

		pc.invalidate();
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
		frame.getStack().push(ReferenceSlot.createNewInstance(heap.newArray()));

		pc.advance();
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
		frame.getStack().push(objRef);

		if (objRef.isNullReference()) {
			pc.advance();
			return;
		}

		// ****************************
		BaseMethodAnalyzer analyzer = getMethodAnalyzer(methodGen, alreadyVisitedMethods);

		analyzer.analyze(pc.getCurrentInstruction().getNext(), new Frame(frame), new Heap(heap),
				alreadyVisitedIfBranch);

		bugs.addAll(analyzer.getBugs());
		result.addAll(analyzer.getResult());
		// ****************************

		// BaseVisitor regularCaseVisitor = getInstructionsAnalysisVisitor(
		// new Frame(frame), new Heap(heap), alreadyVisitedIfBranch,
		// instructionHandle.getNext());
		//
		// regularCaseVisitor.frame.getStack().push(objRef);

		logger.log(Level.FINEST,
				indentation + "\t" + objRef + " ?= " + obj.getLoadClassType(constantPoolGen));

		// instructionHandle.getNext().accept(regularCaseVisitor);
		//
		// bugs.addAll(regularCaseVisitor.getBugs().getCollection());
		// result.addAll(regularCaseVisitor.getResult());

		// 2nd case: type cast is invalid, throw ClassCastException
		handleException(ReferenceSlot.createNewInstance(heap.newClassInstance()));
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

		if (o.getID() == null) {
			logger.log(
					Level.WARNING,
					"GETFIELD on null in "
							+ classContext.getFullyQualifiedMethodName(methodGen.getMethod())
							+ " - stopping evaluation of an unreachable path");
			pc.invalidate();
			return;
			// throw new AssertionError("GETFIELD on null");
		}

		// obj.getSignature() refers to desired field
		Slot f = Slot.getDefaultSlotInstance(obj.getType(constantPoolGen));
		if (f instanceof ReferenceSlot) {
			if (heap.getObject(o) instanceof ExternalObject) {
				// if left side is external return external
				f = ReferenceSlot.createNewInstance(heap.getExternalObject());
			} else {
				// get the ClassInstance linked to the desired field
				f = ReferenceSlot.createNewInstance(((ClassInstance) heap.get(o.getID()))
						.getField(obj.getFieldName(constantPoolGen)));
			}
		}
		frame.pushStackByRequiredSlots(f);

		logger.log(Level.FINEST,
				indentation + "\t" + heap.getObject(o) + "." + obj.getFieldName(constantPoolGen));

		pc.advance();
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
		log.append(obj.getLoadClassType(constantPoolGen) + "." + obj.getFieldName(constantPoolGen)
				+ " (");

		Slot f = Slot.getDefaultSlotInstance(obj.getFieldType(constantPoolGen));
		// if a reference is expected
		if (f instanceof ReferenceSlot) {
			// static values are always external
			f = ReferenceSlot.createNewInstance(heap.getExternalObject());
		}

		log.append((f instanceof DoubleSlot || f instanceof LongSlot) ? f + ", " + f : f);
		frame.pushStackByRequiredSlots(f);
		log.append(")");
		logger.log(Level.FINEST, indentation + log);

		pc.advance();
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
		HeapObject o = heap.getObject(oRef);
		HeapObject v = null;
		if (vRef instanceof ReferenceSlot) {
			v = heap.getObject((ReferenceSlot) vRef);

			if (o instanceof ExternalObject)
				heap.publish(v);
			else
				((ClassInstance) o).setField(obj.getFieldName(constantPoolGen), v);
		}

		logger.log(Level.FINEST, indentation + o + "." + obj.getFieldName(constantPoolGen) + " <--"
				+ ((vRef instanceof ReferenceSlot) ? v : vRef));

		pc.advance();
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
			heap.publish(heap.getObject((ReferenceSlot) v));
		}

		// write log
		String log = "\t" + obj.getReferenceType(constantPoolGen) + "."
				+ obj.getName(constantPoolGen) + " <-- "
				+ ((v.getNumSlots() == 2) ? v + ", " + v : v);

		logger.log(Level.FINEST, indentation + log);

		pc.advance();
	}

	/**
	 * 10. CPInstruction <br>
	 * 10.4. InvokeInstruction <br>
	 * 10.4.1. INVOKEINTERFACE <br>
	 */
	@Override
	public void visitINVOKEINTERFACE(INVOKEINTERFACE obj) {
		dontAnalyzeMethod(obj, Staticality.NONSTATIC);
	}

	private void handleSpecialOrStaticInvocation(InvokeInstruction obj) {
		JavaClass targetClass = null;
		try {
			targetClass = Repository.lookupClass(obj.getLoadClassType(constantPoolGen).toString());
		} catch (ClassNotFoundException e) {
			throw new AssertionError(obj.getLoadClassType(constantPoolGen).toString()
					+ " cannot be loaded.");
		}

		Method targetMethod = new ClassHelper(targetClass).getMethod(
				obj.getMethodName(constantPoolGen), obj.getArgumentTypes(constantPoolGen));
		if (targetMethod == null)
			throw new AssertionError("targetMethod " + obj.getMethodName(constantPoolGen)
					+ " not found in " + obj.getLoadClassType(constantPoolGen) + ": "
					+ obj.toString(true));

		if (targetMethod.isNative()) {
			logger.log(Level.FINE, indentation
					+ "Native method must be dealt with like virtual method.");

			dontAnalyzeMethod(obj, targetMethod.isStatic() ? Staticality.STATIC
					: Staticality.NONSTATIC);
		} else
			analyzeMethod(obj, targetClass, targetMethod);
	}

	/**
	 * 10. CPInstruction <br>
	 * 10.4. InvokeInstruction <br>
	 * 10.4.2. INVOKESPECIAL <br>
	 */
	@Override
	public void visitINVOKESPECIAL(INVOKESPECIAL obj) {
		handleSpecialOrStaticInvocation(obj);
	}

	/**
	 * 10. CPInstruction <br>
	 * 10.4. InvokeInstruction <br>
	 * 10.4.3. INVOKESTATIC <br>
	 */
	@Override
	public void visitINVOKESTATIC(INVOKESTATIC obj) {
		handleSpecialOrStaticInvocation(obj);
	}

	/**
	 * 10. CPInstruction <br>
	 * 10.4. InvokeInstruction <br>
	 * 10.4.4. INVOKEVIRTUAL <br>
	 */
	@Override
	public void visitINVOKEVIRTUAL(INVOKEVIRTUAL obj) {
		JavaClass targetClass = null;
		try {
			targetClass = Repository.lookupClass(obj.getLoadClassType(constantPoolGen).toString());
		} catch (ClassNotFoundException e) {
			throw new AssertionError(obj.getLoadClassType(constantPoolGen).toString()
					+ " cannot be loaded.");
		}

		Method targetMethod = new ClassHelper(targetClass).getMethod(
				obj.getMethodName(constantPoolGen), obj.getArgumentTypes(constantPoolGen));

		while (targetMethod == null) {
			try {
				targetClass = targetClass.getSuperClass();
				targetMethod = new ClassHelper(targetClass).getMethod(
						obj.getMethodName(constantPoolGen), obj.getArgumentTypes(constantPoolGen));
			} catch (ClassNotFoundException e) {
				throw new AssertionError("targetMethod not found in Class");
			}
		}

		if ((targetClass.isFinal() || targetMethod.isFinal()) && !targetMethod.isNative()) {
			logger.log(Level.FINE, indentation + "Final virtual method can be analyzed.");
			analyzeMethod(obj, targetClass, targetMethod);
		} else
			dontAnalyzeMethod(obj, Staticality.NONSTATIC);
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
			value = ReferenceSlot.createNewInstance(heap.newClassInstance());
		}
		frame.pushStackByRequiredSlots(value);

		logger.log(Level.FINEST, indentation + "\t" + value);

		pc.advance();
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
			value = ReferenceSlot.createNewInstance(heap.newClassInstance());
		}
		frame.pushStackByRequiredSlots(value);

		logger.log(Level.FINEST, indentation + "\t" + value);

		pc.advance();
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

			Array newArray = heap.newArray();

			if (i == 0) {
				slot = ReferenceSlot.createNewInstance(newArray);
			} else {
				array.addComponent(newArray);
			}
			array = newArray;
		}

		// push array reference onto stack
		frame.getStack().push(slot);

		pc.advance();
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

		ClassInstance instance = heap.newClassInstance();
		ReferenceSlot slot = ReferenceSlot.createNewInstance(instance);

		frame.getStack().push(slot);

		pc.advance();
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
		logger.log(Level.FINEST,
				indentation + "\t" + "(" + Slot.getDefaultSlotInstance(obj.getType()) + ")");

		// pop length of new array (integer)
		frame.getStack().pop();

		// push reference to new array onto the stack
		ReferenceSlot slot = ReferenceSlot.createNewInstance(heap.newArray());

		frame.getStack().push(slot);

		pc.advance();
	}
}
