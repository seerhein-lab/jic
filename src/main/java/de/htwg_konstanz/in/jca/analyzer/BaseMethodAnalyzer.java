package de.htwg_konstanz.in.jca.analyzer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Set;
import java.util.Stack;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.bcel.classfile.Method;
import org.apache.bcel.generic.CodeExceptionGen;
import org.apache.bcel.generic.InstructionHandle;
import org.apache.bcel.generic.InstructionList;
import org.apache.bcel.generic.MethodGen;
import org.apache.bcel.generic.Type;

import de.htwg_konstanz.in.jca.Frame;
import de.htwg_konstanz.in.jca.ResultValue;
import de.htwg_konstanz.in.jca.Utils;
import de.htwg_konstanz.in.jca.slot.Slot;
import edu.umd.cs.findbugs.BugCollection;
import edu.umd.cs.findbugs.ba.ClassContext;

/**
 * Analyzes methods.
 */
public abstract class BaseMethodAnalyzer {
	protected static final Logger logger = Logger
			.getLogger("BaseMethodAnalyzer");
	protected final ClassContext classContext;
	protected final ArrayList<AlreadyVisitedMethod> alreadyVisitedMethods;
	protected final int depth;
	protected final String indentation;

	/** The method to analyze. */
	protected final Method method;
	// private final ExceptionHandlers exceptionHandlers;
	protected final CodeExceptionGen[] exceptionHandlers;

	/** The visitor which inspects the method's bytecode instructions. */
	protected BaseInstructionsAnalysisVisitor visitor = null;

	public static class AlreadyVisitedMethod {
		final Method method;
		final Slot[] localVars;

		public AlreadyVisitedMethod(Method method, Slot[] localVars) {
			this.method = method;
			this.localVars = localVars;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + Arrays.hashCode(localVars);
			result = prime * result
					+ ((method == null) ? 0 : method.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (!(obj instanceof AlreadyVisitedMethod))
				return false;
			AlreadyVisitedMethod other = (AlreadyVisitedMethod) obj;
			if (!Arrays.equals(localVars, other.localVars))
				return false;
			if (method == null) {
				if (other.method != null)
					return false;
			} else if (!method.equals(other.method))
				return false;
			return true;
		}
	}

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
		this(classContext, methodGen, new ArrayList<AlreadyVisitedMethod>(), -1);
	}

	public BaseMethodAnalyzer(ClassContext classContext, MethodGen methodGen,
			ArrayList<AlreadyVisitedMethod> alreadyVisitedMethods, int depth) {
		this.classContext = classContext;
		this.method = methodGen.getMethod();
		// this.exceptionHandlers = new ExceptionHandlers(methodGen);
		exceptionHandlers = methodGen.getExceptionHandlers();
		this.alreadyVisitedMethods = alreadyVisitedMethods;
		this.depth = depth + 1;
		this.indentation = Utils.formatLoggingOutput(this.depth);
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
	public void analyze(Stack<Slot> callerStack) {
		Frame calleeFrame = createCalleeFrame(callerStack);

		InstructionHandle[] instructionHandles = new InstructionList(method
				.getCode().getCode()).getInstructionHandles();

		visitor = getInstructionAnalysisVisitor(calleeFrame,
				instructionHandles[0]);

		logger.log(Level.FINE, indentation + "vvvvvvvvvvvvvvvvvvvvvvvvvv");
		instructionHandles[0].accept(visitor);
		logger.log(Level.FINE, indentation + "^^^^^^^^^^^^^^^^^^^^^^^^^^");
	}

	protected Frame createCalleeFrame(Stack<Slot> callerStack) {
		int numSlots = method.isStatic() ? 0 : 1;

		for (Type type : method.getArgumentTypes()) {
			numSlots += Slot.getDefaultSlotInstance(type).getNumSlots();
		}

		// if non static method +1 because of hidden 'this' reference
		numSlots = method.isStatic() ? numSlots : numSlots + 1;

		Frame calleeFrame = new Frame(method.getCode().getMaxLocals(),
				callerStack, numSlots);
		return calleeFrame;
	}

	public Slot[] getActualParams(Stack<Slot> callerStack) {
		Stack<Slot> clonedStack = new Stack<Slot>();
		for (Slot slot : callerStack.toArray(new Slot[0])) {
			clonedStack.add(slot.copy());
		}
		return createCalleeFrame(clonedStack).getLocalVars();

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
	public BugCollection getBugs() {
		if (visitor == null) {
			throw new IllegalStateException(
					"analyze() must be called before getBugs()");
		}
		return visitor.getBugs();
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
