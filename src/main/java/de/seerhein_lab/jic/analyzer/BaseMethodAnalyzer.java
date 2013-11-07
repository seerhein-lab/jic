package de.seerhein_lab.jic.analyzer;

import static org.apache.bcel.Constants.CONSTRUCTOR_NAME;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.jcip.annotations.ThreadSafe;

import org.apache.bcel.generic.CodeExceptionGen;
import org.apache.bcel.generic.InstructionHandle;
import org.apache.bcel.generic.InstructionList;
import org.apache.bcel.generic.MethodGen;
import org.apache.bcel.generic.Type;

import de.seerhein_lab.jic.Pair;
import de.seerhein_lab.jic.ResultValue;
import de.seerhein_lab.jic.Utils;
import de.seerhein_lab.jic.cache.AnalysisCache;
import de.seerhein_lab.jic.cache.AnalysisCache.Check;
import de.seerhein_lab.jic.cache.AnalysisResult;
import de.seerhein_lab.jic.cache.AnalyzedMethod;
import de.seerhein_lab.jic.slot.ReferenceSlot;
import de.seerhein_lab.jic.slot.Slot;
import de.seerhein_lab.jic.slot.VoidSlot;
import de.seerhein_lab.jic.vm.ClassInstance;
import de.seerhein_lab.jic.vm.Frame;
import de.seerhein_lab.jic.vm.Heap;
import de.seerhein_lab.jic.vm.HeapObject;
import de.seerhein_lab.jic.vm.OpStack;
import de.seerhein_lab.jic.vm.PC;
import edu.umd.cs.findbugs.BugInstance;
import edu.umd.cs.findbugs.ba.ClassContext;

@ThreadSafe
// follows the Java monitor pattern
public abstract class BaseMethodAnalyzer {
	private static final Logger logger = Logger.getLogger("BaseMethodAnalyzer");
	protected final ClassContext classContext;
	protected final Set<MethodInvocation> alreadyVisitedMethods;
	protected final int depth;
	protected final MethodGen methodGen;
	protected final CodeExceptionGen[] exceptionHandlers;
	protected BaseVisitor visitor = null;
	protected final AnalysisCache cache;
	private Collection<BugInstance> cachedBugs;
	private HashSet<ResultValue> cachedResults;

	protected BaseMethodAnalyzer(ClassContext classContext, MethodGen methodGen,
			Set<MethodInvocation> alreadyVisitedMethods, int depth, AnalysisCache cache) {
		if (classContext == null || methodGen == null || alreadyVisitedMethods == null)
			throw new AssertionError("Params must not be null.");

		// if (depth > 25)
		// throw new OutOfMemoryError(
		// "emergency brake to avoid out of memory error (method stack depth exceeded)");

		this.classContext = classContext;
		this.methodGen = methodGen;
		exceptionHandlers = methodGen.getExceptionHandlers();
		this.alreadyVisitedMethods = alreadyVisitedMethods;

		this.depth = depth + 1;
		this.cache = cache;
	}

	protected abstract BaseVisitor getInstructionVisitor(Frame frame, Heap heap, PC pc,
			Set<Pair<InstructionHandle, Boolean>> alreadyVisitedIfBranch);

	protected abstract Heap getHeap();

	private Frame createCalleeFrame(OpStack callerOpStack) {
		int numSlots = methodGen.isStatic() ? 0 : 1;

		for (Type type : methodGen.getArgumentTypes()) {
			numSlots += Slot.getDefaultSlotInstance(type).getNumSlots();
		}

		Frame calleeFrame = new Frame(methodGen.getMethod().getCode().getMaxLocals(),
				callerOpStack, numSlots);
		return calleeFrame;
	}

	public final synchronized void analyze() {
		OpStack callerStack = new OpStack();
		Heap callerHeap = getHeap();

		// push this onto the stack, if not static
		// if (!method.isStatic()) {
		callerStack.push(ReferenceSlot.createNewInstance(callerHeap.getThisInstance()));
		// }

		// push args onto the stack
		for (Type argType : methodGen.getArgumentTypes()) {
			Slot argument = Slot.getDefaultSlotInstance(argType);
			if (argument instanceof ReferenceSlot) {
				argument = ReferenceSlot.createNewInstance(callerHeap.getExternalObject());
			}
			for (int i = 0; i < argument.getNumSlots(); i++) {
				callerStack.push(argument);
			}
		}

		analyze(callerStack, callerHeap);
	}

	public synchronized void analyze(OpStack callerStack, Heap heap) {

		AnalyzedMethod method = new AnalyzedMethod(methodGen.getClassName(), methodGen.getMethod());

		if (cache.contains(method) && cache.get(method).isCached(getCheck())) {
			logger.log(Level.FINE, Utils.formatLoggingOutput(this.depth) + method
					+ " already evaluated - taking result out of the cache");

			cachedBugs = cache.get(method).getBugs(getCheck());
			cachedResults = new HashSet<ResultValue>();

			for (ResultValue resultValue : cache.get(method).getResults()) {
				HeapObject resultObject = resultValue.getHeap().getObject(
						(ReferenceSlot) resultValue.getSlot());
				if (resultValue.getKind().equals(ResultValue.Kind.EXCEPTION)) {
					cachedResults.add(new ResultValue(resultValue.getKind(), ReferenceSlot
							.createNewInstance((ClassInstance) resultObject.deepCopy(heap)), heap));
				} else {
					((ClassInstance) heap.getObject((ReferenceSlot) callerStack.pop()))
							.copyReferredObjectsTo(resultObject, heap);
					cachedResults.add(new ResultValue(ResultValue.Kind.REGULAR, VoidSlot
							.getInstance(), heap));
				}
			}

		} else {
			Slot firstParam = callerStack.size() == 0 ? null : new OpStack(callerStack).pop();
			Frame calleeFrame = createCalleeFrame(callerStack);

			InstructionHandle[] instructionHandles = new InstructionList(methodGen.getMethod()
					.getCode().getCode()).getInstructionHandles();

			analyze(instructionHandles[0], calleeFrame, heap,
					new HashSet<Pair<InstructionHandle, Boolean>>());

			if (methodGen.getMethod().getName().equals(CONSTRUCTOR_NAME)
					&& methodGen.getMethod().getArgumentTypes().length == 0) {
				logger.log(Level.FINE,
						Utils.formatLoggingOutput(this.depth) + "Put " + methodGen.getClassName()
								+ methodGen.getMethod().getName() + " in the Cache");
				AnalysisResult result = new AnalysisResult(getResult(), firstParam);
				result.setBugs(getCheck(), getBugs());
				cache.add(method, result, getCheck());
			}
		}
	}

	protected abstract Check getCheck();

	public final synchronized void analyze(InstructionHandle ih, Frame frame, Heap heap,
			Set<Pair<InstructionHandle, Boolean>> alreadyVisitedIfBranch) {

		PC pc = new PC(ih);

		visitor = getInstructionVisitor(frame, heap, pc, alreadyVisitedIfBranch);

		logger.log(Level.FINE, Utils.formatLoggingOutput(this.depth) + "vvvvvvvvvvvvvvvvvvvvvvvvvv");
		while (pc.isValid()) {
			// visitor is expected to
			// (1) either execute the current opcode and then update the pc, or
			// (2) deliver a (possibly multi-value) result and invalidate the
			// pc.
			// The result can be computed by execution of the last opcode in
			// the list, or by recursively instantiating other analyzers.
			pc.getCurrentInstruction().accept(visitor);
		}

		logger.log(Level.FINE, Utils.formatLoggingOutput(this.depth) + "^^^^^^^^^^^^^^^^^^^^^^^^^^");
	}

	/**
	 * Returns the bugs found in the analysis. The method analyze() must be
	 * called before this method is called.
	 * 
	 * @return the bug list, not null.
	 * 
	 * @throws IllegalStateException
	 *             if analyze() was not called beforehand.
	 */
	public final synchronized Collection<BugInstance> getBugs() {
		if (visitor == null) {
			// throw new
			// IllegalStateException("analyze() must be called before getBugs()");
			return cachedBugs;
		}
		return visitor.getBugs().getCollection();
	}

	/**
	 * Returns the result of the method call. The method analyze() must be
	 * called before this method is called.
	 * 
	 * @return the result of the method call, or null if the method is a void
	 *         method.
	 * 
	 * @throws IllegalStateException
	 *             if analyze() was not called beforehand.
	 */
	public final synchronized Set<ResultValue> getResult() {
		if (visitor == null) {
			return cachedResults;
			// throw new
			// IllegalStateException("analyze() must be called before getResult()");
		}
		return visitor.getResult();
	}

}
