package playground;

import org.apache.bcel.Repository;
import org.apache.bcel.classfile.JavaClass;
import org.apache.bcel.classfile.Method;
import org.apache.bcel.generic.ConstantPoolGen;
import org.apache.bcel.generic.Instruction;
import org.apache.bcel.generic.InstructionList;
import org.apache.bcel.verifier.structurals.ExecutionVisitor;
import org.apache.bcel.verifier.structurals.Frame;


public class EvalTest {

    /**
     * @param args
     * @throws ClassNotFoundException 
     */
    public static void main(String[] args) throws ClassNotFoundException {
	// TODO Auto-generated method stub
	
	JavaClass clazz = Repository.lookupClass("playground.TestClass");
	
	Method[] methods = clazz.getMethods();
	for ( Method method : methods ) {
	    ExecutionVisitor visitor = new ExecutionVisitor();
	    visitor.setConstantPoolGen(new ConstantPoolGen(method.getCode().getConstantPool()));
	    visitor.setFrame(new Frame(method.getCode().getMaxLocals(), method.getCode().getMaxStack()));
	
	    Instruction[] instructions = new InstructionList(method.getCode().getCode()).getInstructions();
		
	    for ( Instruction instruction : instructions ) {
		instruction.accept(visitor);
	    }
	    
	    
	}

    }


}
