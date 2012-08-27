package de.htwg_konstanz.in.jca;

import java.util.Stack;

import org.apache.bcel.classfile.LocalVariable;
import org.apache.bcel.classfile.LocalVariableTable;
import org.apache.bcel.classfile.Method;
import org.apache.bcel.generic.Instruction;
import org.apache.bcel.generic.InstructionList;

/**
 * Analyzes constructors.
 */
public class CtorAnalyzer {

	/** The constructor to analyze. */
	private final Method ctor;

	/** The result of the constructor call. */
	private Entry result;

	/**
	 * Constructor.
	 * 
	 * @param ctor
	 *            The constructor to analyze, not null.
	 */
	public CtorAnalyzer(Method ctor) {
		this.ctor = ctor;
	}

	/**
	 * Returns the result of the constructor call.
	 * 
	 * @return the result of the constructor call, or null if not yet
	 *         determined.
	 */
	public Entry getResult() {
		return result;
	}

	/**
	 * Checks whether the reference of the checked object is passed to another
	 * object in the constructor.
	 * 
	 * @return whether the reference of the checked object escapes.
	 */
	public ThreeValueBoolean doesThisReferenceEscape() {
		Stack<Entry> callerStack = new Stack<Entry>();

		// push this + args onto the stack
		callerStack.push(Entry.thisReference);

		LocalVariable[] localVars = (ctor.getCode().getLocalVariableTable() == null) ? new LocalVariable[0]
				: ctor.getCode().getLocalVariableTable()
						.getLocalVariableTable();

		for (int i = 1; i < localVars.length; i++) {
			callerStack.push(Entry.getInstance(localVars[i].getSignature()));
		}

		return doesThisReferenceEscape(callerStack);
	}

	/**
	 * Checks whether the reference of the checked object is passed to another
	 * object in the constructor.
	 * 
	 * @param callerStack
	 *            the content of the local variable table of the constructor.
	 * 
	 * @return whether the reference of the checked object escapes.
	 */
	public ThreeValueBoolean doesThisReferenceEscape(Stack<Entry> callerStack) {
		LocalVariableTable lvt = ctor.getLocalVariableTable();
		LocalVariable[] lva = null;

		if (lvt == null) {
			lva = new LocalVariable[1];

		}

		LocalVars localVars = new LocalVars(
				(ctor.getLocalVariableTable() == null) ? new LocalVariable[0]
						: ctor.getLocalVariableTable().getLocalVariableTable());

		localVars.initWithArgs(callerStack, ctor.getArgumentTypes().length + 1);

		Stack<Entry> stack = new Stack<Entry>();

		CtorAnalysisVisitor visitor = new CtorAnalysisVisitor(localVars, stack,
				ctor.getCode().getConstantPool());
		Instruction[] instructions = new InstructionList(ctor.getCode()
				.getCode()).getInstructions();
		ThreeValueBoolean doesEscape = ThreeValueBoolean.no;

		System.out.println("vvvvvvvvvvvvvvvvvvvvvvvvvv");

		for (Instruction instruction : instructions) {
			instruction.accept(visitor);
			doesEscape = doesEscape.or(visitor.doesEscape());
		}

		result = visitor.getResult();

		System.out.println("^^^^^^^^^^^^^^^^^^^^^^^^^^^");

		return doesEscape;
	}
}
