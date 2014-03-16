package de.seerhein_lab.jic.analyzer;

import static org.apache.bcel.Constants.CONSTRUCTOR_NAME;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
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

import de.seerhein_lab.jic.AnalysisResult;
import de.seerhein_lab.jic.EvaluationResult;
import de.seerhein_lab.jic.EvaluationResult.Kind;
import de.seerhein_lab.jic.Pair;
import de.seerhein_lab.jic.ThreeValueBoolean;
import de.seerhein_lab.jic.Utils;
import de.seerhein_lab.jic.analyzer.eval.EvaluationOnlyAnalyzer;
import de.seerhein_lab.jic.analyzer.recursion.RecursionAnalyzer;
import de.seerhein_lab.jic.cache.AnalysisCache;
import de.seerhein_lab.jic.cache.AnalysisCache.Check;
import de.seerhein_lab.jic.cache.AnalysisResults;
import de.seerhein_lab.jic.slot.DoubleSlot;
import de.seerhein_lab.jic.slot.LongSlot;
import de.seerhein_lab.jic.slot.Slot;
import de.seerhein_lab.jic.slot.VoidSlot;
import de.seerhein_lab.jic.vm.Array;
import de.seerhein_lab.jic.vm.ClassInstance;
import de.seerhein_lab.jic.vm.ExternalObject;
import de.seerhein_lab.jic.vm.Frame;
import de.seerhein_lab.jic.vm.Heap;
import de.seerhein_lab.jic.vm.HeapObject;
import de.seerhein_lab.jic.vm.PC;
import de.seerhein_lab.jic.vm.ReferenceSlot;
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
	protected static final Logger logger = Logger.getLogger("BaseInstructionsAnalysisVisitor");
	protected final ClassContext classContext;
	protected final MethodGen methodGen;

	protected final CodeExceptionGen[] exceptionHandlers;
	protected Set<Pair<InstructionHandle, Boolean>> alreadyVisitedIfBranch;
	protected final Set<QualifiedMethod> alreadyVisitedMethods;
	protected SortedBugCollection bugs = new SortedBugCollection();
	protected final AnalysisCache cache;
	protected Set<EvaluationResult> result = new HashSet<EvaluationResult>();
	public static long cacheMisses = 0;
	public static long cacheHits = 0;

	protected abstract BaseMethodAnalyzer getMethodAnalyzer(MethodGen targetMethodGen,
			Set<QualifiedMethod> alreadyVisitedMethods, int methodInvocationDepth);

	// methods for bug detection
	protected abstract void detectVirtualMethodBug(ReferenceSlot argument);

	protected abstract void detectXAStoreBug(ReferenceSlot arrayReference, Slot valueToStore);

	protected abstract void detectPutFieldBug(ReferenceSlot targetReference, Slot valueToPut);

	protected abstract void detectPutStaticBug(ReferenceSlot referenceToPut);

	protected abstract void detectAReturnBug(ReferenceSlot returnSlot);

	protected BaseVisitor(ClassContext classContext, MethodGen methodGen, Frame frame, Heap heap,
			ConstantPoolGen constantPoolGen, Set<Pair<InstructionHandle, Boolean>> alreadyVisited,
			Set<QualifiedMethod> alreadyVisitedMethods, PC pc,
			CodeExceptionGen[] exceptionHandlers, int depth, AnalysisCache cache,
			int methodInvocationDepth) {
		super(frame, heap, constantPoolGen, pc, depth, methodInvocationDepth);

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

	public Set<EvaluationResult> getResult() {
		return result;
	}

	protected void addBug(String pattern, Confidence confidence, String message,
			InstructionHandle instructionHandle) {
		logger.fine(indentation + "\t" + message + " (" + instructionHandle.getInstruction() + ")");
		BugInstance bugInstance = Utils.createBug(pattern, confidence, message,
				classContext.getJavaClass());

		bugInstance.addSourceLine(classContext, methodGen.getMethod(), instructionHandle);
		bugs.add(bugInstance);

	}

	// handle section

	protected void handleException(ReferenceSlot exception, Heap heap) {
		frame.getStack().clear();
		frame.getStack().push(exception);

		for (CodeExceptionGen exceptionHandler : exceptionHandlers) {
			if (MethodHelper.protectsInstruction(exceptionHandler, pc.getCurrentInstruction())) {
				logger.fine(indentation + "vvvvv " + exceptionHandler.toString() + ": start vvvvv");

				// ************
				BaseMethodAnalyzer analyzer = getMethodAnalyzer(methodGen, alreadyVisitedMethods,
						methodInvocationDepth + 1);
				AnalysisResult analysisResult = analyzer.analyze(exceptionHandler.getHandlerPC(),
						new Frame(frame), new Heap(heap), alreadyVisitedIfBranch);

				bugs.addAll(analysisResult.getBugs());
				result.addAll(analysisResult.getResults());
				logger.fine(indentation + "^^^^^ " + exceptionHandler.toString() + ": end ^^^^^");

			}
		}
		result.add(new EvaluationResult(Kind.EXCEPTION, exception, heap));
		pc.invalidate();
	}

	protected abstract Check getCheck();

	private Set<EvaluationResult> useCachedResults(QualifiedMethod method) {
		Set<EvaluationResult> targetResults;
		targetResults = new HashSet<EvaluationResult>();

		for (Type argument : method.getMethod().getArgumentTypes()) {
			for (int i = 0; i < argument.getSize(); i++)
				frame.getStack().pop();
		}

		ReferenceSlot topOfStack = (ReferenceSlot) frame.getStack().pop();

		for (EvaluationResult resultValue : cache.get(method).getResults()) {
			Heap resultHeap = new Heap(heap);
			HeapObject resultObject = ((ReferenceSlot) resultValue.getSlot()).getObject(resultValue
					.getHeap());

			if (resultValue.getKind().equals(EvaluationResult.Kind.EXCEPTION)) {
				targetResults.add(new EvaluationResult(resultValue.getKind(), new ReferenceSlot(
						resultHeap.newClassInstance(resultObject.isImmutable())), resultHeap));
			} else {
				((ClassInstance) ((ReferenceSlot) topOfStack).getObject(resultHeap))
						.copyReferredObjectsTo(resultObject);
				targetResults.add(new EvaluationResult(EvaluationResult.Kind.REGULAR, VoidSlot
						.getInstance(), resultHeap));
			}
		}
		return targetResults;
	}

	private void cacheResults(MethodGen targetMethodGen, QualifiedMethod method,
			AnalysisResult methodResult, Slot objectUnderConstruction) {
		logger.fine(indentation + "Put " + targetMethodGen.getClassName()
				+ targetMethodGen.getMethod().getName() + " in the Cache");

		AnalysisResults result = new AnalysisResults(methodResult.getResults(),
				objectUnderConstruction);
		result.setBugs(getCheck(), methodResult.getBugs());

		cache.add(method, result, getCheck());
	}

	private void wrapNestedBugs(QualifiedMethod targetMethod, Collection<BugInstance> targetBugs) {
		for (Iterator<BugInstance> it = targetBugs.iterator(); it.hasNext();) {
			BugInstance bug = it.next();

			if (targetMethod.getJavaClass().equals(classContext.getJavaClass())) {
				bugs.add(bug);
			}

			// addBug(bug.getBugPattern().getType(), Confidence.HIGH,
			// "subsequent bug caused by ["
			// + bug.getMessage() + " in " +
			// targetMethod.getJavaClass().getClassName() + "."
			// + targetMethod.getMethod().getName() +
			// targetMethod.getMethod().getSignature()
			// + ":" + bug.getPrimarySourceLineAnnotation().getStartLine() +
			// "]",
			// pc.getCurrentInstruction());

			addBug(bug.getBugPattern().getType().startsWith("SUBSEQUENT_") ? bug.getBugPattern()
					.getType() : "SUBSEQUENT_" + bug.getBugPattern().getType(), Confidence.HIGH,
					"subsequent bug caused by bug in " + targetMethod.getJavaClass().getClassName()
							+ "." + targetMethod.getMethod().getName()
							+ targetMethod.getMethod().getSignature() + ":"
							+ bug.getPrimarySourceLineAnnotation().getStartLine() + "]",
					pc.getCurrentInstruction());
		}
	}

	private void continueWithResults(Set<EvaluationResult> targetResults) {
		for (EvaluationResult calleeResult : targetResults) {
			if (calleeResult.getKind().equals(Kind.REGULAR)) {

				if (targetResults.size() == 1) {
					frame.getStack().pushByRequiredSize(calleeResult.getSlot());
					heap = calleeResult.getHeap();
					pc.advance();
					return;
				}

				BaseMethodAnalyzer analyzer = getMethodAnalyzer(methodGen, alreadyVisitedMethods,
						methodInvocationDepth);

				Frame newFrame = new Frame(frame);
				newFrame.getStack().pushByRequiredSize(calleeResult.getSlot());

				AnalysisResult analysisResult = analyzer.analyze(pc.getCurrentInstruction()
						.getNext(), newFrame, calleeResult.getHeap(), alreadyVisitedIfBranch);

				bugs.addAll(analysisResult.getBugs());
				result.addAll(analysisResult.getResults());

			} else {
				Frame savedFrame = new Frame(frame);
				InstructionHandle currentInstruction = pc.getCurrentInstruction();
				handleException((ReferenceSlot) calleeResult.getSlot(), calleeResult.getHeap());
				pc.setInstruction(currentInstruction);
				frame = savedFrame;
			}
		}
		pc.invalidate();
	}

	private void handleEarlyBoundMethod(InvokeInstruction obj, QualifiedMethod targetMethod) {
		logger.fine(indentation + obj.toString(false));
		logger.finest(indentation + "\t" + obj.getLoadClassType(constantPoolGen) + "."
				+ obj.getMethodName(constantPoolGen) + obj.getSignature(constantPoolGen));

		MethodGen targetMethodGen = new MethodGen(targetMethod.getMethod(), targetMethod
				.getJavaClass().getClassName(), new ConstantPoolGen(targetMethod.getJavaClass()
				.getConstantPool()));

		AnalysisResult methodResult;

		if (cache.isCacheable(targetMethod)) {
			if (cache.contains(targetMethod) && cache.get(targetMethod).isCached(getCheck())) {
				logger.fine(indentation + targetMethod
						+ " already evaluated - taking result out of the cache");
				cacheHits++;

				methodResult = new AnalysisResult(useCachedResults(targetMethod), cache.get(
						targetMethod).getBugs(getCheck()));

			} else {
				cacheMisses++;

				int stackOffset = 0;
				for (Type argument : targetMethod.getMethod().getArgumentTypes())
					stackOffset += argument.getSize();

				Slot objectUnderConstruction = frame.getStack().get(
						frame.getStack().size() - 1 - stackOffset);
				methodResult = analyzeMethod(targetMethod, targetMethodGen, alreadyVisitedMethods,
						objectUnderConstruction);

				cacheResults(targetMethodGen, targetMethod, methodResult, objectUnderConstruction);
			}
		} else {
			cacheMisses++;

			Slot firstParam = frame.getStack().size() == 0 ? null : frame.getStack().peek();
			methodResult = analyzeMethod(targetMethod, targetMethodGen, alreadyVisitedMethods,
					firstParam);

		}

		wrapNestedBugs(targetMethod, methodResult.getBugs());
		continueWithResults(methodResult.getResults());
	}

	private AnalysisResult analyzeMethod(QualifiedMethod targetMethod, MethodGen targetMethodGen,
			Set<QualifiedMethod> alreadyVisitedMethods, Slot firstParam) {

		Set<QualifiedMethod> nowVisitedMethods = new HashSet<QualifiedMethod>();
		nowVisitedMethods.addAll(alreadyVisitedMethods);
		nowVisitedMethods.add(targetMethod);

		AnalysisResult methodResult;
		BaseMethodAnalyzer targetMethodAnalyzer;

		if (alreadyVisitedMethods.contains(targetMethod)) {
			return handleRecursion(targetMethodGen);
		}

		if (targetMethod.getMethod().getName().equals(CONSTRUCTOR_NAME)
				&& targetMethod.getMethod().getArgumentTypes().length == 0
				// && firstParam instanceof ReferenceSlot
				&& !((ReferenceSlot) firstParam).getObject(heap).equals(heap.getThisInstance())) {

			targetMethodAnalyzer = new EvaluationOnlyAnalyzer(classContext, targetMethodGen,
					nowVisitedMethods, depth, cache, methodInvocationDepth + 1);
		} else {
			targetMethodAnalyzer = getMethodAnalyzer(targetMethodGen, nowVisitedMethods,
					methodInvocationDepth + 1);
		}
		methodResult = targetMethodAnalyzer.analyze(frame.getStack(), heap);
		return methodResult;
	}

	protected AnalysisResult handleRecursion(
	// InvokeInstruction obj,
			MethodGen targetMethodGen) {
		logger.fine(indentation + "\tRecursion found: Get result of recursive call.");

		BaseMethodAnalyzer recursionAnalyzer = new RecursionAnalyzer(classContext, targetMethodGen,
				alreadyVisitedMethods, depth, cache, methodInvocationDepth + 1);

		AnalysisResult result = recursionAnalyzer.analyze(frame.getStack(), heap);
		logger.fine(indentation + "Recursion results: " + result.getResults());
		return result;
	}

	private void handleLatelyBoundMethod(InvokeInstruction obj) {
		logger.fine(indentation + obj.toString(false));
		logger.finest(indentation + "\t" + obj.getLoadClassType(constantPoolGen) + "."
				+ obj.getMethodName(constantPoolGen) + obj.getSignature(constantPoolGen));

		for (int i = 0; i < obj.consumeStack(constantPoolGen); i++) {
			Slot argument = frame.getStack().pop();
			if (argument instanceof ReferenceSlot) {
				ReferenceSlot reference = (ReferenceSlot) argument;
				// check for bugs
				detectVirtualMethodBug(reference);
				heap.publish(reference.getObject(heap));
			}
		}

		Slot returnValue = Slot.getDefaultSlotInstance(obj.getReturnType(constantPoolGen));

		// return external reference if returnType reference is expected
		if (returnValue instanceof ReferenceSlot)
			returnValue = ReferenceSlot.getExternalReference(heap,
					ClassHelper.isImmutableAndFinal(obj.getReturnType(constantPoolGen)));

		// works also for void results, because number of required slots = 0
		frame.getStack().pushByRequiredSize(returnValue);

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
		logger.fine(indentation
				+ obj.toString(false)
				+ (frame.getLocalVars()[obj.getIndex()] instanceof ReferenceSlot ? " ("
						+ ((ReferenceSlot) frame.getLocalVars()[obj.getIndex()]).getObject(heap)
						+ ")" : ""));
		if (frame.getLocalVars()[obj.getIndex()] == null)
			throw new AssertionError("wrong index for local vars");
		frame.getStack().pushByRequiredSize(frame.getLocalVars()[obj.getIndex()]);
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
		logger.fine(indentation + obj.toString(false));
		// + (frame.getStack().peek() instanceof ReferenceSlot ? " ("
		// + heap.getObject((ReferenceSlot) frame.getStack().peek()) + ")" :
		// " ("
		// + frame.getStack().peek() + ")"));

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

		logger.fine(indentation + obj.toString(false));
		Slot returnType = Slot.getDefaultSlotInstance(obj.getType(constantPoolGen));
		logger.finest(indentation + "\t" + returnType);

		if (returnType instanceof VoidSlot)
			result.add(new EvaluationResult(Kind.REGULAR, returnType, heap));
		else {
			Slot returnSlot = frame.getStack().popByRequiredSize();
			if (returnType instanceof ReferenceSlot)
				detectAReturnBug((ReferenceSlot) returnSlot);
			result.add(new EvaluationResult(Kind.REGULAR, returnSlot, heap));
		}
		pc.invalidate();
	}

	// ---StackInstruction----------------------------------------------

	@Override
	public void visitStackInstruction(StackInstruction obj) {
		Slot slot1, slot2, slot3, slot4;
		switch (obj.getOpcode()) {
		case 0x59:
			// DUB
			logger.fine(indentation + obj.toString(false));
			slot1 = frame.getStack().pop();

			frame.getStack().push(slot1);
			frame.getStack().push(slot1);
			break;
		case 0x5a:
			// DUB_X1
			logger.fine(indentation + obj.toString(false));
			slot1 = frame.getStack().pop();
			slot2 = frame.getStack().pop();

			frame.getStack().push(slot1);
			frame.getStack().push(slot2);
			frame.getStack().push(slot1);
			break;
		case 0x5b:
			// DUB_X2
			// pop values
			logger.fine(indentation + obj.toString(false));
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
			logger.fine(indentation + obj.toString(false));
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
			logger.fine(indentation + obj.toString(false));
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
			logger.fine(indentation + obj.toString(false));
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
			logger.fine(indentation + obj.toString(false));
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
		logger.fine(indentation + obj.toString(false));

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
			Slot value = frame.getStack().popByRequiredSize();
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
		logger.fine(indentation + obj.toString(false));

		// pop array index
		frame.getStack().pop();
		// pop array reference
		ReferenceSlot arrayReference = (ReferenceSlot) frame.getStack().pop();

		if (arrayReference.isNullReference()) {
			logger.warning("AALOAD on null in "
					+ classContext.getFullyQualifiedMethodName(methodGen.getMethod())
					+ " - stopping evaluation of an unreachable path");
			pc.invalidate();
			return;
		}

		if (arrayReference.getObject(heap) instanceof ExternalObject) {
			frame.getStack().push(ReferenceSlot.getExternalReference(heap, false));
			pc.advance();
			return;
		}

		Array array = (Array) arrayReference.getObject(heap);

		for (HeapObject referredObject : array.getReferredObjects()) {

			BaseMethodAnalyzer analyzer = getMethodAnalyzer(methodGen, alreadyVisitedMethods,
					methodInvocationDepth);

			Frame newFrame = new Frame(frame);
			newFrame.getStack().push(new ReferenceSlot(referredObject));

			AnalysisResult analysisResult = analyzer.analyze(pc.getCurrentInstruction().getNext(),
					newFrame, new Heap(heap), alreadyVisitedIfBranch);

			bugs.addAll(analysisResult.getBugs());
			result.addAll(analysisResult.getResults());
		}

		// final option: component is null
		frame.getStack().push(ReferenceSlot.getNullReference());

		BaseMethodAnalyzer analyzer = getMethodAnalyzer(methodGen, alreadyVisitedMethods,
				methodInvocationDepth);
		AnalysisResult analysisResult = analyzer.analyze(pc.getCurrentInstruction().getNext(),
				frame, heap, alreadyVisitedIfBranch);

		bugs.addAll(analysisResult.getBugs());
		result.addAll(analysisResult.getResults());

		pc.invalidate();
	}

	@Override
	public void visitAASTORE(AASTORE obj) {
		logger.fine(indentation + obj.toString(false));

		// pop value
		ReferenceSlot valueRef = (ReferenceSlot) frame.getStack().popByRequiredSize();

		// pop array index
		frame.getStack().pop();
		// pop array reference
		ReferenceSlot arrayReference = (ReferenceSlot) frame.getStack().pop();

		if (arrayReference.isNullReference()) {
			logger.warning("AASTORE on null in "
					+ classContext.getFullyQualifiedMethodName(methodGen.getMethod())
					+ " - stopping evaluation of an unreachable path");
			pc.invalidate();
			return;
		}

		detectXAStoreBug(arrayReference, valueRef);

		if (!valueRef.isNullReference()) {
			HeapObject array = arrayReference.getObject(heap);

			if (array instanceof ExternalObject)
				heap.publish(valueRef.getObject(heap));
			else
				((Array) array).addReferredObject(valueRef.getObject(heap));
		}

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
		logger.fine(indentation + obj.toString(false));
		ReferenceSlot exception = (ReferenceSlot) frame.getStack().pop();

		if (exception.isNullReference()) {
			exception = new ReferenceSlot(
					heap.newClassInstanceOfDynamicType("java.lang.NullPointerException"));
		}

		handleException(exception, heap);
	}

	/**
	 * 7. BranchInstruction<br>
	 * 7.1. GotoInstruction<br>
	 * Called when a GotoInstruction operation occurs. Shifts the
	 * InstructionHandle to the target instead of the next one.
	 */
	@Override
	public void visitGotoInstruction(GotoInstruction obj) {
		logger.fine(indentation + obj.toString(false));
		pc.setInstruction(obj.getTarget());
	}

	private ThreeValueBoolean evaluateCondition(IfInstruction instruction) {
		if (instruction.getOpcode() == 0xc6) // ifnull
			return ThreeValueBoolean.fromBoolean(((ReferenceSlot) frame.getStack().pop())
					.isNullReference());

		if (instruction.getOpcode() == 0xc7) // ifnonnull
			return ThreeValueBoolean.fromBoolean(!((ReferenceSlot) frame.getStack().pop())
					.isNullReference());

		// all other conditions
		for (int i = 0; i < instruction.consumeStack(constantPoolGen); i++)
			frame.getStack().pop();

		return ThreeValueBoolean.unknown;
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
		logger.fine(indentation + obj.toString(false));

		ThreeValueBoolean condition = evaluateCondition(obj);

		if (condition.maybeFalse()) {
			logger.finest(indentation + "------------------  " + alreadyVisitedIfBranch.size()
					+ ".else  (condition might be inverted!) ------------------");
			Pair<InstructionHandle, Boolean> elseBranch = new Pair<InstructionHandle, Boolean>(
					pc.getCurrentInstruction(), false);

			if (alreadyVisitedIfBranch.add(elseBranch)) {
				BaseMethodAnalyzer elseAnalyzer = getMethodAnalyzer(methodGen,
						alreadyVisitedMethods, methodInvocationDepth);

				AnalysisResult analysisResult = !condition.maybeTrue() ? elseAnalyzer.analyze(pc
						.getCurrentInstruction().getNext(), frame, heap, alreadyVisitedIfBranch)
						: elseAnalyzer.analyze(pc.getCurrentInstruction().getNext(), new Frame(
								frame), new Heap(heap), alreadyVisitedIfBranch);

				bugs.addAll(analysisResult.getBugs());
				result.addAll(analysisResult.getResults());

			} else {
				logger.finest(indentation + "Loop detected, do not re-enter.");
			}
		}

		if (condition.maybeTrue()) {
			logger.finest(indentation + "------------------  " + alreadyVisitedIfBranch.size()
					+ ".then  (condition might be inverted!) ------------------");
			Pair<InstructionHandle, Boolean> thenBranch = new Pair<InstructionHandle, Boolean>(
					pc.getCurrentInstruction(), true);

			if (alreadyVisitedIfBranch.add(thenBranch)) {
				BaseMethodAnalyzer thenAnalyzer = getMethodAnalyzer(methodGen,
						alreadyVisitedMethods, methodInvocationDepth);

				AnalysisResult analysisResult = thenAnalyzer.analyze(obj.getTarget(), frame, heap,
						alreadyVisitedIfBranch);

				bugs.addAll(analysisResult.getBugs());
				result.addAll(analysisResult.getResults());

			} else {
				logger.finest(indentation + "Loop detected, do not re-enter.");
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
		throw new AssertionError("handling of jsr instruction is not yet implemented.");
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
		logger.fine(indentation + obj.toString(false));

		// pops integer index
		frame.getStack().pop();

		// gets all targets except the default case
		InstructionHandle[] targets = obj.getTargets();
		// follows all targets except the default case
		for (int i = 0; i < targets.length; i++) {
			logger.finest(indentation + "--------------- Line " + targets[i].getPosition()
					+ " ---------------");

			// ****************************
			BaseMethodAnalyzer caseAnalyzer = getMethodAnalyzer(methodGen, alreadyVisitedMethods,
					methodInvocationDepth);

			AnalysisResult analysisResult = caseAnalyzer.analyze(targets[i], new Frame(frame),
					new Heap(heap), alreadyVisitedIfBranch);

			bugs.addAll(analysisResult.getBugs());
			result.addAll(analysisResult.getResults());

			// ****************************

		}
		// handles the default case and follows it
		logger.finest(indentation + "--------------- Line " + obj.getTarget().getPosition()
				+ " (DefaultCase) ---------------");
		// NOTE: If the keyword "Default:" is not in the switch the following
		// target is the end of the switch without executing a case.

		// ****************************
		BaseMethodAnalyzer defaultAnalyzer = getMethodAnalyzer(methodGen, alreadyVisitedMethods,
				methodInvocationDepth);

		AnalysisResult analysisResult = defaultAnalyzer.analyze(obj.getTarget(), frame, heap,
				alreadyVisitedIfBranch);

		bugs.addAll(analysisResult.getBugs());
		result.addAll(analysisResult.getResults());

		// ****************************

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
		logger.fine(indentation + obj.toString(false));

		// pops length,
		frame.getStack().pop();

		// pushes new array reference
		frame.getStack().push(new ReferenceSlot(heap.newArray()));

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
		logger.fine(indentation + obj.toString(false));
		ReferenceSlot objRef = (ReferenceSlot) frame.getStack().peek();

		if (objRef.isNullReference()) {
			pc.advance();
			return;
		}

		BaseMethodAnalyzer analyzer = getMethodAnalyzer(methodGen, alreadyVisitedMethods,
				methodInvocationDepth + 1);

		AnalysisResult analysisResult = analyzer.analyze(pc.getCurrentInstruction().getNext(),
				new Frame(frame), new Heap(heap), alreadyVisitedIfBranch);

		bugs.addAll(analysisResult.getBugs());
		result.addAll(analysisResult.getResults());

		logger.finest(indentation + "\t" + objRef + " ?= " + obj.getLoadClassType(constantPoolGen));

		handleException(
				new ReferenceSlot(
						heap.newClassInstanceOfDynamicType("java.lang.ClassCastException")), heap);

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
		logger.finest(indentation + obj.toString(false));
		// Notation: gets o.f

		// pop object reference
		ReferenceSlot o = (ReferenceSlot) frame.getStack().pop();

		if (o.isNullReference()) {
			logger.warning("GETFIELD on null in "
					+ classContext.getFullyQualifiedMethodName(methodGen.getMethod())
					+ " - stopping evaluation of an unreachable path");
			pc.invalidate();
			return;
		}

		Slot f = Slot.getDefaultSlotInstance(obj.getType(constantPoolGen));
		if (f instanceof ReferenceSlot) {
			if (o.getObject(heap) instanceof ExternalObject) {
				// if left side is external return external
				f = ReferenceSlot.getExternalReference(heap,
						ClassHelper.isImmutableAndFinal(obj.getFieldType(constantPoolGen)));
			} else {
				// get the HeapObject linked to the desired field
				HeapObject referredByF = ((ClassInstance) o.getObject(heap)).getField(obj
						.getFieldName(constantPoolGen));
				if (referredByF == null) {
					f = ReferenceSlot.getNullReference();
				} else {
					f = new ReferenceSlot(referredByF);
				}
			}
		}
		frame.getStack().pushByRequiredSize(f);

		logger.finest(indentation + "\t" + o.getObject(heap) + "."
				+ obj.getFieldName(constantPoolGen));

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
		logger.finest(indentation + obj.toString(false));
		// Notation: gets f

		StringBuilder log = new StringBuilder();
		log.append(indentation + "\t");
		log.append(obj.getLoadClassType(constantPoolGen) + "." + obj.getFieldName(constantPoolGen)
				+ " (");

		Slot f = Slot.getDefaultSlotInstance(obj.getFieldType(constantPoolGen));

		if (f instanceof ReferenceSlot) {
			// static values are always external
			f = ReferenceSlot.getExternalReference(heap,
					ClassHelper.isImmutableAndFinal(obj.getFieldType(constantPoolGen)));
		}

		log.append((f instanceof DoubleSlot || f instanceof LongSlot) ? f + ", " + f : f);
		frame.getStack().pushByRequiredSize(f);
		log.append(")");
		logger.finest(indentation + log);

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
		logger.finest(indentation + obj.toString(false));
		// Notation: puts o.f = v

		// right side of assignment
		Slot vRef = frame.getStack().popByRequiredSize();

		// pop left side of assignment off the stack, too
		ReferenceSlot oRef = (ReferenceSlot) frame.getStack().pop();

		if (oRef.isNullReference()) {
			logger.warning("PUTFIELD on null in "
					+ classContext.getFullyQualifiedMethodName(methodGen.getMethod())
					+ " - stopping evaluation of an unreachable path");
			pc.invalidate();
			return;
		}

		detectPutFieldBug(oRef, vRef);
		HeapObject o = oRef.getObject(heap);
		HeapObject v = null;
		if (vRef instanceof ReferenceSlot) {
			v = ((ReferenceSlot) vRef).getObject(heap);

			if (o instanceof ExternalObject)
				heap.publish(v);
			else
				((ClassInstance) o).setField(obj.getFieldName(constantPoolGen), v);
		}

		logger.finest(indentation + "\t" + o + "." + obj.getFieldName(constantPoolGen) + " <--"
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
		logger.fine(indentation + obj.toString(false));
		// Notation: f = v

		// popping value from stack
		Slot v = frame.getStack().popByRequiredSize();

		// a reference is assigned to a static field
		if (v instanceof ReferenceSlot) {
			detectPutStaticBug((ReferenceSlot) v);
			// make it external
			heap.publish(((ReferenceSlot) v).getObject(heap));
		}

		// write log
		String log = "\t" + obj.getReferenceType(constantPoolGen) + "."
				+ obj.getName(constantPoolGen) + " <-- "
				+ ((v.getNumSlots() == 2) ? v + ", " + v : v);

		logger.finest(indentation + log);

		pc.advance();
	}

	/**
	 * 10. CPInstruction <br>
	 * 10.4. InvokeInstruction <br>
	 * 10.4.1. INVOKEINTERFACE <br>
	 */
	@Override
	public void visitINVOKEINTERFACE(INVOKEINTERFACE obj) {
		handleLatelyBoundMethod(obj);
	}

	private QualifiedMethod getTargetMethod(InvokeInstruction instruction) {
		JavaClass targetClass = null;
		try {
			targetClass = Repository.lookupClass(instruction.getLoadClassType(constantPoolGen)
					.toString());
		} catch (ClassNotFoundException e) {
			throw new AssertionError(instruction.getLoadClassType(constantPoolGen).toString()
					+ " cannot be loaded.");
		}

		Method targetMethod = new ClassHelper(targetClass).getMethod(
				instruction.getMethodName(constantPoolGen),
				instruction.getArgumentTypes(constantPoolGen));

		while (targetMethod == null) {
			try {
				targetClass = targetClass.getSuperClass();
				targetMethod = new ClassHelper(targetClass).getMethod(
						instruction.getMethodName(constantPoolGen),
						instruction.getArgumentTypes(constantPoolGen));
			} catch (ClassNotFoundException e) {
				throw new AssertionError("targetMethod "
						+ instruction.getMethodName(constantPoolGen) + " not found in "
						+ instruction.getLoadClassType(constantPoolGen) + " or its supertypes");
			}
		}

		return new QualifiedMethod(targetClass, targetMethod);
	}

	private void handleSpecialOrStaticInvocation(InvokeInstruction obj) {
		QualifiedMethod targetMethod = getTargetMethod(obj);

		if (targetMethod.getMethod().isNative()) {
			logger.fine(indentation + "Native method must be dealt with like virtual method.");

			handleLatelyBoundMethod(obj);

		} else
			handleEarlyBoundMethod(obj, targetMethod);
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
		QualifiedMethod targetMethod = getTargetMethod(obj);

		if ((targetMethod.getJavaClass().isFinal() || targetMethod.getMethod().isFinal())
				&& !targetMethod.getMethod().isNative()) {
			logger.fine(indentation + "Final virtual method can be analyzed.");
			handleEarlyBoundMethod(obj, targetMethod);
		} else
			handleLatelyBoundMethod(obj);
	}

	/**
	 * 10. CPInstruction <br>
	 * 10.6. LDC <br>
	 */
	@Override
	public void visitLDC(LDC obj) {
		logger.fine(indentation + obj.toString(false));
		Slot value = Slot.getDefaultSlotInstance(obj.getType(constantPoolGen));
		// pushes an integer, a float, a long, a double or a String
		// (notThis) onto the stack
		if (value instanceof ReferenceSlot) {
			// it is a String
			value = new ReferenceSlot(heap.newClassInstanceOfStaticType(obj
					.getType(constantPoolGen)));
		}
		frame.getStack().pushByRequiredSize(value);

		logger.finest(indentation + "\t" + value);

		pc.advance();
	}

	/**
	 * 10. CPInstruction <br>
	 * 10.7 LDC2_W <br>
	 */
	@Override
	public void visitLDC2_W(LDC2_W obj) {
		logger.fine(indentation + obj.toString(false));
		Slot value = Slot.getDefaultSlotInstance(obj.getType(constantPoolGen));
		// pushes an integer, a float, a long, a double or a String
		// (notThis) onto the stack
		if (value instanceof ReferenceSlot) {
			// it is a String
			value = new ReferenceSlot(heap.newClassInstanceOfStaticType(obj
					.getType(constantPoolGen)));
		}
		frame.getStack().pushByRequiredSize(value);

		logger.finest(indentation + "\t" + value);

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

		logger.fine(indentation + obj.toString(false));
		String log = "\t";
		log += obj.getLoadClassType(constantPoolGen);
		log += "[" + obj.getDimensions() + "][]";
		logger.finest(indentation + log);

		ReferenceSlot slot = null;
		Array array = null;

		for (int i = 0; i < obj.consumeStack(constantPoolGen); i++) {
			// pop count values for each dimension
			frame.getStack().pop();

			Array newArray = heap.newArray();

			if (i == 0) {
				slot = new ReferenceSlot(newArray);
			} else {
				array.addReferredObject(newArray);
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
		ClassInstance instance = heap.newClassInstanceOfDynamicType(obj
				.getLoadClassType(constantPoolGen));
		ReferenceSlot slot = new ReferenceSlot(instance);

		logger.fine(indentation + obj.toString(false) + " (" + slot.getObject(heap) + ")");

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
		logger.fine(indentation + obj.toString(false));
		logger.finest(indentation + "\t" + "(" + Slot.getDefaultSlotInstance(obj.getType()) + ")");

		// pop length of new array (integer)
		frame.getStack().pop();

		// push reference to new array onto the stack
		ReferenceSlot slot = new ReferenceSlot(heap.newArray());

		frame.getStack().push(slot);

		pc.advance();
	}
}
