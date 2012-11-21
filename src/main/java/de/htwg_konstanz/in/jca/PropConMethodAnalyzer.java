package de.htwg_konstanz.in.jca;

import java.util.Stack;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.bcel.classfile.Method;
import org.apache.bcel.generic.ConstantPoolGen;
import org.apache.bcel.generic.InstructionHandle;
import org.apache.bcel.generic.InstructionList;
import org.apache.bcel.generic.MethodGen;
import org.apache.bcel.generic.Type;
import org.apache.bcel.verifier.structurals.ExceptionHandlers;

import edu.umd.cs.findbugs.BugCollection;

/**
 * Analyzes methods.
 */
public class PropConMethodAnalyzer {
	private static final Logger logger = Logger
			.getLogger("PropConMethodAnalyzer");

	/** The method to analyze. */
	private final Method method;
	private final ExceptionHandlers exceptionHandlers;

	/** The visitor which inspects the method's bytecode instructions. */
	private PropConInstructionsAnalysisVisitor visitor = null;

	/**
	 * Constructor.
	 * 
	 * @param method
	 *            The method to analyze, not null.
	 */
	public PropConMethodAnalyzer(MethodGen methodGen) {
		this.method = methodGen.getMethod();
		this.exceptionHandlers = new ExceptionHandlers(methodGen);
	}

	/**
	 * Checks whether the reference of the checked object is passed to another
	 * object in the method.
	 */
	public void analyze() {
		Stack<Slot> callerStack = new Stack<Slot>();

		// push this + args onto the stack
		callerStack.push(Slot.thisReference);

		Type[] argTypes = method.getArgumentTypes();

		for (Type argType : argTypes) {
			DataType dataType = DataType.getDataType(argType);
			for (int i = 0; i < dataType.getNumSlots(); i++) {
				callerStack.push(Slot.getDefaultInstance(dataType));
			}

		}

		analyze(callerStack);
	}

	/**
	 * Checks whether the reference of the checked object is passed to another
	 * object in the method.
	 * 
	 * @param callerStack
	 *            the content of the local variable table of the constructor.
	 */
	public void analyze(Stack<Slot> callerStack) {
		int numSlots = Slot.numRequiredSlots(method.getArgumentTypes());

		// if non static method +1 because of hidden this reference
		numSlots = method.isStatic() ? numSlots : numSlots + 1;

		Frame calleeFrame = new Frame(method.getCode().getMaxLocals(),
				callerStack, numSlots);

		InstructionHandle[] instructionHandles = new InstructionList(method
				.getCode().getCode()).getInstructionHandles();

		visitor = new PropConInstructionsAnalysisVisitor(calleeFrame,
				new ConstantPoolGen(method.getConstantPool()),
				instructionHandles[0], exceptionHandlers);

		logger.log(Level.FINE, "vvvvvvvvvvvvvvvvvvvvvvvvvv");
		instructionHandles[0].accept(visitor);
		logger.log(Level.FINE, "^^^^^^^^^^^^^^^^^^^^^^^^^^");
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
	public Slot getResult() {
		if (visitor == null) {
			throw new IllegalStateException(
					"analyze() must be called before getResult()");
		}
		return visitor.getResult();
	}

}
