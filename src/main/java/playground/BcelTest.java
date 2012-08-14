package playground;

import java.util.List;
import java.util.Vector;

import org.apache.bcel.Repository;
import org.apache.bcel.classfile.JavaClass;
import org.apache.bcel.classfile.Method;
import org.apache.bcel.generic.Type;
import org.apache.bcel.verifier.structurals.Frame;
import org.apache.bcel.verifier.structurals.LocalVariables;
import org.apache.bcel.verifier.structurals.OperandStack;

public class BcelTest {

    /**
     * @param args
     * @throws ClassNotFoundException 
     */
    public static void main(String[] args) throws ClassNotFoundException {
	// TODO Auto-generated method stub
	
	JavaClass clazz = Repository.lookupClass("playground.TestClass");
	
	Method[] methods = clazz.getMethods();
	for ( Method method : methods ) {
	    Frame frame = new Frame(method.getCode().getMaxLocals(), 
		    method.getCode().getMaxStack());
	    LocalVariables locals = frame.getLocals();
	    System.out.println("Code: " + method.getCode());
	    System.out.println("Locals: " + locals);
	    OperandStack stack = frame.getStack();
	    System.out.println("Stack: " + stack);
	    System.out.println("---------");
	}

    }


}
