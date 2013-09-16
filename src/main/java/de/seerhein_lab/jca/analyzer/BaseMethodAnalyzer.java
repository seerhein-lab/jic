package de.seerhein_lab.jca.analyzer;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.bcel.classfile.Method;
import org.apache.bcel.generic.CodeExceptionGen;
import org.apache.bcel.generic.InstructionHandle;
import org.apache.bcel.generic.InstructionList;
import org.apache.bcel.generic.MethodGen;
import org.apache.bcel.generic.Type;

import de.seerhein_lab.jca.Pair;
import de.seerhein_lab.jca.ResultValue;
import de.seerhein_lab.jca.Utils;
import de.seerhein_lab.jca.slot.ReferenceSlot;
import de.seerhein_lab.jca.slot.Slot;
import de.seerhein_lab.jca.vm.Frame;
import de.seerhein_lab.jca.vm.Heap;
import de.seerhein_lab.jca.vm.OpStack;
import de.seerhein_lab.jca.vm.PC;
import edu.umd.cs.findbugs.BugInstance;
import edu.umd.cs.findbugs.ba.ClassContext;

/**
 * Analyzes methods.
 */
public abstract class BaseMethodAnalyzer {
	private static final Logger logger = Logger.getLogger("BaseMethodAnalyzer");
	protected final ClassContext classContext;
	protected final Set<Pair<Method, Slot[]>> alreadyVisitedMethods;
	protected final int depth;

	/** The method to analyze. */
	protected final Method method;
	protected final CodeExceptionGen[] exceptionHandlers;

	/** The visitor which inspects the method's bytecode instructions. */
	protected BaseInstructionsVisitor visitor = null;

	/**
	 * Constructor.
	 * 
	 * @param classContext
	 * 
	 * @param method
	 *            The method to analyze, not null.
	 * 
	 */
	public BaseMethodAnalyzer(ClassContext classContext, MethodGen methodGen) {
		this(classContext, methodGen, new HashSet<Pair<Method, Slot[]>>(), -1);
	}

	public BaseMethodAnalyzer(ClassContext classContext, MethodGen methodGen,
			Set<Pair<Method, Slot[]>> alreadyVisitedMethods, int depth) {
		this.classContext = classContext;
		this.method = methodGen.getMethod();
		exceptionHandlers = methodGen.getExceptionHandlers();
		this.alreadyVisitedMethods = alreadyVisitedMethods;
		this.depth = depth + 1;
	}

	protected abstract BaseInstructionsVisitor getInstructionVisitor(
			Frame frame, Heap heap, PC pc);

	/**
	 * Checks whether the reference of the checked object is passed to another
	 * object in the method.
	 */
	public final void analyze() {
		OpStack callerStack = new OpStack();
		Heap callerHeap = getHeap();

		// push this onto the stack, if not static
		if (!method.isStatic()) {
			callerStack.push(ReferenceSlot.createNewInstance(callerHeap
					.getThisInstance()));
		}

		// push args onto the stack
		for (Type argType : method.getArgumentTypes()) {
			Slot argument = Slot.getDefaultSlotInstance(argType);
			if (argument instanceof ReferenceSlot) {
				argument = ReferenceSlot.createNewInstance(callerHeap
						.getExternalObject());
			}
			for (int i = 0; i < argument.getNumSlots(); i++) {
				callerStack.push(argument);
			}
		}

		analyze(callerStack, callerHeap);
	}

	public abstract Heap getHeap();

	/**
	 * Checks whether the reference of the checked object is passed to another
	 * object in the method.
	 * 
	 * @param callerStack
	 *            the content of the local variable table of the constructor.
	 */
	public void analyze(OpStack callerStack, Heap heap) {
		Frame calleeFrame = createCalleeFrame(callerStack);

		InstructionHandle[] instructionHandles = new InstructionList(method
				.getCode().getCode()).getInstructionHandles();

		PC pc = new PC(instructionHandles[0]);

		visitor = getInstructionVisitor(calleeFrame, heap, pc);

		logger.log(Level.FINE, Utils.formatLoggingOutput(this.depth)
				+ "vvvvvvvvvvvvvvvvvvvvvvvvvv");
		while (pc.isValid()) {
			// visitor is expected to 
			// (1) either execute the current opcode and then update the pc, or 
			// (2) deliver a (possibly multi-value) result and invalidate the pc.
			//     The result can be computed by execution of the last opcode in 
			//     the list, or by recursively instantiating other analyzers.
			pc.getCurrentInstruction().accept(visitor);
			// TODO remove next line eventually
			pc.invalidate();
		}

		logger.log(Level.FINE, Utils.formatLoggingOutput(this.depth)
				+ "^^^^^^^^^^^^^^^^^^^^^^^^^^");
	}

	protected Frame createCalleeFrame(OpStack callerOpStack) {
		int numSlots = method.isStatic() ? 0 : 1;

		for (Type type : method.getArgumentTypes()) {
			numSlots += Slot.getDefaultSlotInstance(type).getNumSlots();
		}

		Frame calleeFrame = new Frame(method.getCode().getMaxLocals(),
				callerOpStack, numSlots);
		return calleeFrame;
	}

	public Slot[] getActualParams(Frame frame) {
		OpStack opStack = new OpStack(frame.getStack());
		return createCalleeFrame(opStack).getLocalVars();
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
	public Collection<BugInstance> getBugs() {
		if (visitor == null) {
			throw new IllegalStateException(
					"analyze() must be called before getBugs()");
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
	public Set<ResultValue> getResult() {

		if (visitor == null) {
			throw new IllegalStateException(
					"analyze() must be called before getResult()");
		}
		return visitor.getResult();
	}

}
