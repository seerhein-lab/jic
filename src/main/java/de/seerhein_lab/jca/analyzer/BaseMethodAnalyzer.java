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

import de.seerhein_lab.jca.Frame;
import de.seerhein_lab.jca.Pair;
import de.seerhein_lab.jca.ResultValue;
import de.seerhein_lab.jca.Utils;
import de.seerhein_lab.jca.slot.Slot;
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
	// private final ExceptionHandlers exceptionHandlers;
	protected final CodeExceptionGen[] exceptionHandlers;

	/** The visitor which inspects the method's bytecode instructions. */
	protected BaseInstructionsAnalysisVisitor visitor = null;

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
		// this.exceptionHandlers = new ExceptionHandlers(methodGen);
		exceptionHandlers = methodGen.getExceptionHandlers();
		this.alreadyVisitedMethods = alreadyVisitedMethods;
		this.depth = depth + 1;
	}

	protected static boolean protectsInstruction(
			CodeExceptionGen exceptionHandler, InstructionHandle instruction) {
		for (InstructionHandle protectedInstruction = exceptionHandler
				.getStartPC(); !protectedInstruction.equals(exceptionHandler
				.getEndPC()); protectedInstruction = protectedInstruction
				.getNext()) {
			// System.out.println(protectedInstruction + " | " + instruction);
			if (protectedInstruction.getPosition() == instruction.getPosition())
				return true;
		}
		return (exceptionHandler.getEndPC().getPosition() == instruction
				.getPosition());
	}

	protected abstract BaseInstructionsAnalysisVisitor getInstructionAnalysisVisitor(
			Frame frame, InstructionHandle instructionHandle);

	/**
	 * Checks whether the reference of the checked object is passed to another
	 * object in the method.
	 */
	public abstract void analyze();

	/**
	 * Checks whether the reference of the checked object is passed to another
	 * object in the method.
	 * 
	 * @param callerStack
	 *            the content of the local variable table of the constructor.
	 */
	public void analyze(Frame callerFrame) {
		Frame calleeFrame = createCalleeFrame(callerFrame);

		InstructionHandle[] instructionHandles = new InstructionList(method
				.getCode().getCode()).getInstructionHandles();

		visitor = getInstructionAnalysisVisitor(calleeFrame,
				instructionHandles[0]);

		logger.log(Level.FINE, Utils.formatLoggingOutput(this.depth)
				+ "vvvvvvvvvvvvvvvvvvvvvvvvvv");
		instructionHandles[0].accept(visitor);
		logger.log(Level.FINE, Utils.formatLoggingOutput(this.depth)
				+ "^^^^^^^^^^^^^^^^^^^^^^^^^^");
	}

	protected Frame createCalleeFrame(Frame callerFrame) {
		int numSlots = method.isStatic() ? 0 : 1;

		for (Type type : method.getArgumentTypes()) {
			numSlots += Slot.getDefaultSlotInstance(type).getNumSlots();
		}

		Frame calleeFrame = new Frame(method.getCode().getMaxLocals(),
				callerFrame, numSlots);
		return calleeFrame;
	}

	public Slot[] getActualParams(Frame frame) {
		Frame clonedFrame = new Frame(frame);
		return createCalleeFrame(clonedFrame).getLocalVars();

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
