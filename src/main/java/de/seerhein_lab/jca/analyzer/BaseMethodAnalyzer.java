package de.seerhein_lab.jca.analyzer;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.jcip.annotations.ThreadSafe;

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
import edu.umd.cs.findbugs.annotations.Confidence;
import edu.umd.cs.findbugs.ba.ClassContext;


@ThreadSafe // follows the Java monitor pattern
public abstract class BaseMethodAnalyzer {
	private static final Logger logger = Logger.getLogger("BaseMethodAnalyzer");
	protected final ClassContext classContext;
	protected final Set<Pair<Method, Slot[]>> alreadyVisitedMethods;
	protected final int depth;
	protected final MethodGen methodGen;
	protected final CodeExceptionGen[] exceptionHandlers;
	protected BaseVisitor visitor = null;


	protected BaseMethodAnalyzer(ClassContext classContext, MethodGen methodGen,
			Set<Pair<Method, Slot[]>> alreadyVisitedMethods, int depth) {
		if ( classContext == null || methodGen == null || alreadyVisitedMethods == null ) 
			throw new AssertionError("Params must not be null.");
		
		this.classContext = classContext;
		this.methodGen = methodGen;
		exceptionHandlers = methodGen.getExceptionHandlers();
		this.alreadyVisitedMethods = alreadyVisitedMethods;
		this.depth = depth + 1;
	}

	protected abstract BaseVisitor getInstructionVisitor(Frame frame, Heap heap, PC pc);

	protected abstract Heap getHeap();
//	protected abstract String getMessage4NativeMethod();


	public final synchronized void analyze() {
		OpStack callerStack = new OpStack();
		Heap callerHeap = getHeap();

		// push this onto the stack, if not static
//		if (!method.isStatic()) {
			callerStack.push(ReferenceSlot.createNewInstance(callerHeap
					.getThisInstance()));
//		}

		// push args onto the stack
		for (Type argType : methodGen.getArgumentTypes()) {
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
	
	public final synchronized void analyze(InstructionHandle ih, Frame frame, Heap heap) {
		PC pc = new PC(ih);

		visitor = getInstructionVisitor(frame, heap, pc);

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
	

	public final synchronized void analyze(OpStack callerStack, Heap heap) {
		Frame calleeFrame = createCalleeFrame(callerStack);
		
		InstructionHandle[] instructionHandles = new InstructionList(methodGen.getMethod()
				.getCode().getCode()).getInstructionHandles();
		
		analyze(instructionHandles[0], calleeFrame, heap);
		

//		PC pc = new PC(instructionHandles[0]);
//
//		visitor = getInstructionVisitor(calleeFrame, heap, pc);
//
//		logger.log(Level.FINE, Utils.formatLoggingOutput(this.depth)
//				+ "vvvvvvvvvvvvvvvvvvvvvvvvvv");
//		while (pc.isValid()) {
//			// visitor is expected to 
//			// (1) either execute the current opcode and then update the pc, or 
//			// (2) deliver a (possibly multi-value) result and invalidate the pc.
//			//     The result can be computed by execution of the last opcode in 
//			//     the list, or by recursively instantiating other analyzers.
//			pc.getCurrentInstruction().accept(visitor);
//			// TODO remove next line eventually
//			pc.invalidate();
//		}
//
//		logger.log(Level.FINE, Utils.formatLoggingOutput(this.depth)
//				+ "^^^^^^^^^^^^^^^^^^^^^^^^^^");
	}

	private Frame createCalleeFrame(OpStack callerOpStack) {
		int numSlots = methodGen.isStatic() ? 0 : 1;

		for (Type type : methodGen.getArgumentTypes()) {
			numSlots += Slot.getDefaultSlotInstance(type).getNumSlots();
		}

		Frame calleeFrame = new Frame(methodGen.getMethod().getCode().getMaxLocals(),
				callerOpStack, numSlots);
		return calleeFrame;
	}

	public final synchronized Slot[] getActualParams(Frame frame) {
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
	public final synchronized Collection<BugInstance> getBugs() {
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
	public final synchronized Set<ResultValue> getResult() {

		if (visitor == null) {
			throw new IllegalStateException(
					"analyze() must be called before getResult()");
		}
		return visitor.getResult();
	}

}
