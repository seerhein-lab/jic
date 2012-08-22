package de.htwg_konstanz.in.jca;

import java.util.Stack;

import org.apache.bcel.classfile.LocalVariable;
import org.apache.bcel.classfile.Method;
import org.apache.bcel.generic.Instruction;
import org.apache.bcel.generic.InstructionList;


public class CtorAnalyzer {    
    private final Method ctor;
    private Entry result;
    
    public CtorAnalyzer(Method ctor) {
	this.ctor = ctor;
    }
    
    public Entry getResult() { return result; }
    

    
    public ThreeValueBoolean doesThisReferenceEscape() {
	Stack<Entry> callerStack = new Stack<Entry>();
	
	// push this + args onto the stack
	callerStack.push(Entry.thisReference);
	
	LocalVariable[] localVars = ctor.getCode().getLocalVariableTable().getLocalVariableTable();
	
	for ( int i = 1; i < localVars.length; i++ ) {
	    callerStack.push(Entry.getInstance(localVars[i].getSignature()));
	}
	
	return doesThisReferenceEscape(callerStack);
    }
	
    
    public ThreeValueBoolean doesThisReferenceEscape(Stack<Entry> callerStack) {	
	LocalVars localVars = 
			new LocalVars(ctor.getCode().getLocalVariableTable().getLocalVariableTable());

	localVars.initWithArgs(callerStack, ctor.getArgumentTypes().length +1);

	Stack<Entry> stack = new Stack<Entry>();
	
	CtorAnalysisVisitor visitor = new CtorAnalysisVisitor(localVars, stack, ctor.getCode().getConstantPool());
	Instruction[] instructions = new InstructionList(ctor.getCode().getCode()).getInstructions();
	ThreeValueBoolean doesEscape = ThreeValueBoolean.no;
	
	System.out.println("vvvvvvvvvvvvvvvvvvvvvvvvvv");
	
	for ( Instruction instruction : instructions ) {
	    instruction.accept(visitor);
		doesEscape = doesEscape.or(visitor.doesEscape()); 
	}
	
	result = visitor.getResult();
	
	System.out.println("^^^^^^^^^^^^^^^^^^^^^^^^^^^");

	return doesEscape;
    }
}
