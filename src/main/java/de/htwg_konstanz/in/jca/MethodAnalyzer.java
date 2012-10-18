package de.htwg_konstanz.in.jca;

import java.util.Stack;

import org.apache.bcel.classfile.LocalVariable;
import org.apache.bcel.classfile.Method;

import edu.umd.cs.findbugs.BugCollection;

/**
 * Analyzes methods.
 */
public class MethodAnalyzer {

	/** The method to analyze. */
	private final Method method;

	/** The result of the constructor call. */
	private Entry result;

	/**
	 * Constructor.
	 * 
	 * @param method
	 *            The method to analyze, not null.
	 */
	public MethodAnalyzer(Method method) {
		this.method = method;
	}

	/**
	 * Returns the result of the method call.
	 * 
	 * @return the result of the method call, or null if not yet determined.
	 */
	public Entry getResult() {
		return result;
	}

	/**
	 * Checks whether the reference of the checked object is passed to another
	 * object in the method.
	 * 
	 * @return BugCollection containing potential error messages
	 */
	public BugCollection doesThisReferenceEscape() {
		Stack<Entry> callerStack = new Stack<Entry>();

		// push this + args onto the stack
		callerStack.push(Entry.thisReference);

		LocalVariable[] localVars = (method.getCode().getLocalVariableTable() == null) ? new LocalVariable[0]
				: method.getCode().getLocalVariableTable()
						.getLocalVariableTable();

		for (int i = 1; i < method.getArgumentTypes().length + 1; i++) {
			callerStack.push(Entry.getInstance(localVars[i].getSignature()));
		}

		return doesThisReferenceEscape(callerStack);
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
	public BugCollection doesThisReferenceEscape(Stack<Entry> callerStack) {
		LocalVars localVars = new LocalVars(
				(method.getLocalVariableTable() == null) ? new LocalVariable[0]
						: method.getLocalVariableTable()
								.getLocalVariableTable());
		// if non static method +1 because of hidden this-reference
		for (int i = 0; i < method.getArgumentTypes().length; i++) {
			System.out.println(method.getArgumentTypes()[i]);
		}
		System.out.println(method.getName());
		System.out.println(method.isStatic());
		localVars.initWithArgs(
				callerStack,
				method.isStatic() ? method.getArgumentTypes().length : method
						.getArgumentTypes().length + 1);
		Stack<Entry> stack = new Stack<Entry>();

		MethodAnalysisVisitor visitor = new MethodAnalysisVisitor(localVars,
				stack, callerStack, method);

		System.out.println("vvvvvvvvvvvvvvvvvvvvvvvvvv");
		BugCollection bugs = visitor.analyze();
		System.out.println("^^^^^^^^^^^^^^^^^^^^^^^^^^");

		return bugs;
	}
}
