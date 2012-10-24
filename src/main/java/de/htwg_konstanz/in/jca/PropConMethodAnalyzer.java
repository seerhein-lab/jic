package de.htwg_konstanz.in.jca;

import java.util.Stack;

import org.apache.bcel.classfile.LocalVariable;
import org.apache.bcel.classfile.Method;
import org.apache.bcel.generic.ConstantPoolGen;
import org.apache.bcel.generic.InstructionHandle;
import org.apache.bcel.generic.InstructionList;

import edu.umd.cs.findbugs.BugCollection;

/**
 * Analyzes methods.
 */
public class PropConMethodAnalyzer {

	/** The method to analyze. */
	private final Method method;
	private PropConInstructionsAnalysisVisitor visitor = null;

	/**
	 * Constructor.
	 * 
	 * @param method
	 *            The method to analyze, not null.
	 */
	public PropConMethodAnalyzer(Method method) {
		this.method = method;
	}

	/**
	 * Checks whether the reference of the checked object is passed to another
	 * object in the method.
	 * 
	 * @return BugCollection containing potential error messages
	 */
	public void analyze() {
		Stack<Entry> callerStack = new Stack<Entry>();

		// push this + args onto the stack
		callerStack.push(Entry.thisReference);

		LocalVariable[] localVars = (method.getCode().getLocalVariableTable() == null) ? new LocalVariable[0]
				: method.getCode().getLocalVariableTable()
						.getLocalVariableTable();

		for (int i = 1; i < method.getArgumentTypes().length + 1; i++) {
			callerStack.push(Entry.getInstance(localVars[i].getSignature()));
		}

		analyze(callerStack);
	}

	/**
	 * Checks whether the reference of the checked object is passed to another
	 * object in the method.
	 * 
	 * @param callerStack
	 *            the content of the local variable table of the constructor.
	 * 
	 * @return BugCollection containing potential error messages
	 */
	public void analyze(Stack<Entry> callerStack) {
		LocalVars localVars = new LocalVars(
				(method.getLocalVariableTable() == null) ? new LocalVariable[0]
						: method.getLocalVariableTable()
								.getLocalVariableTable());
		// if non static method +1 because of hidden this-reference
		for (int i = 0; i < method.getArgumentTypes().length; i++) {
			System.out.println(method.getArgumentTypes()[i]);
		}
		System.out.println(method.getName());
		System.out.println(method.isStatic() ? "static" : "non-static");
		localVars.initWithArgs(
				callerStack,
				method.isStatic() ? method.getArgumentTypes().length : method
						.getArgumentTypes().length + 1);
		Stack<Entry> stack = new Stack<Entry>();

		visitor = new PropConInstructionsAnalysisVisitor(localVars, stack,
				new ConstantPoolGen(method.getConstantPool()));

		InstructionHandle[] instructionHandles = new InstructionList(method
				.getCode().getCode()).getInstructionHandles();

		System.out.println("vvvvvvvvvvvvvvvvvvvvvvvvvv");
		instructionHandles[0].accept(visitor);
		System.out.println("^^^^^^^^^^^^^^^^^^^^^^^^^^");
	}

	public BugCollection getBugs() {
		if (visitor == null) {
			throw new IllegalStateException(
					"analyze() must be called before getBugs()");
		}
		return visitor.getBugs();
	}

	/**
	 * Returns the result of the method call.
	 * 
	 * @return the result of the method call, or null if not yet determined.
	 */
	public Entry getResult() {
		if (visitor == null) {
			throw new IllegalStateException(
					"analyze() must be called before getResult()");
		}
		return visitor.getResult();
	}

}
