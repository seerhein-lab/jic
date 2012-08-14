package de.htwg_konstanz.in.jca;

import org.apache.bcel.Repository;
import org.apache.bcel.classfile.ConstantPool;
import org.apache.bcel.classfile.JavaClass;
import org.apache.bcel.classfile.Method;
import org.apache.bcel.generic.*;


public class CtorAnalysisVisitor extends EmptyVisitor {    
    private final LocalVars localVars;
    private final Stack stack;
    private final ConstantPoolGen constantPoolGen;
    
    private volatile ThreeValueBoolean doesEscape = ThreeValueBoolean.no;
    private volatile Entry result = null;
    
    CtorAnalysisVisitor(LocalVars localVars, Stack stack, ConstantPool constantPool) {
	this.localVars = localVars;
	this.stack = stack;
	this.constantPoolGen = new ConstantPoolGen(constantPool);
    }
    
    public ThreeValueBoolean doesEscape() { return doesEscape; }
    public Entry getResult() { return result; }

    
    private void notImplementedYet(Object instruction) {
	System.out.println(instruction.toString());
	System.out.println("NOT IMPLEMENTED YET");
	doesEscape = ThreeValueBoolean.unknown;
	System.out.println();
    }
    
    //-----------------------------------------------------------------   
    @Override public void visitACONST_NULL(ACONST_NULL obj) {
	notImplementedYet(obj);
    }
    
    //-----------------------------------------------------------------

    @Override public void visitArithmeticInstruction(ArithmeticInstruction obj) {
	notImplementedYet(obj);
    }
    
    //-----------------------------------------------------------------

    @Override public void visitArrayInstruction(ArrayInstruction obj) {
	notImplementedYet(obj);
    }
    
    //-----------------------------------------------------------------
    @Override public void visitARRAYLENGTH(ARRAYLENGTH obj) {
	notImplementedYet(obj);
    }
    
    //-----------------------------------------------------------------
    @Override public void visitATHROW(ATHROW obj) {
	notImplementedYet(obj);
    }
    
    //-----------------------------------------------------------------
    @Override public void visitBIPUSH(BIPUSH obj) {
	System.out.println(obj.toString(false));
	stack.push(Entry.someInt);
	doesEscape = ThreeValueBoolean.no;
    }
    
    //-----------------------------------------------------------------
    @Override public void visitBranchInstruction(BranchInstruction obj) {
	notImplementedYet(obj);
    }
    
    //-----------------------------------------------------------------
    @Override public void visitBREAKPOINT(BREAKPOINT obj) {
	notImplementedYet(obj);
    }
    
    //-----------------------------------------------------------------
    @Override public void visitConversionInstruction(ConversionInstruction obj) {
	notImplementedYet(obj);
    }
    
    //---CPInstruction-------------------------------------------------
    @Override public void visitANEWARRAY(ANEWARRAY obj) {
	notImplementedYet(obj);
    }
    
    @Override public void visitCHECKCAST(CHECKCAST obj) {
	notImplementedYet(obj);
    }
    
    @Override public void visitGETFIELD(GETFIELD obj) {
	notImplementedYet(obj);
    }
    
    @Override public void visitGETSTATIC(GETSTATIC obj) {
	notImplementedYet(obj);
    }
    
    @Override public void visitPUTFIELD(PUTFIELD obj) {
	System.out.print(obj.toString(false) + ": ");

	// right side of assignment
	Entry right = stack.pop();
	
	doesEscape = ThreeValueBoolean.fromBoolean(right.equals(Entry.thisReference));
	
	if ( right.equals(Entry.thisReference) ) 
	    System.out.println("'this' reference is assigned to some object's field --> ESCAPE!");
			    
	// pop left side of assignment off the stack, too
	Entry left = stack.pop();
	System.out.println(left +"." + obj.getFieldName(constantPoolGen) + " <--" + right);
	
    }
    
    @Override public void visitPUTSTATIC(PUTSTATIC obj) {
	notImplementedYet(obj);
    }
    
    @Override public void visitINVOKEINTERFACE(INVOKEINTERFACE obj) {
	notImplementedYet(obj);
    }
      
    
    @Override public void visitINVOKESPECIAL(INVOKESPECIAL obj) {
	System.out.println("INVOKESPECIAL: " + obj.getReferenceType(constantPoolGen) +"." + 
		obj.getMethodName(constantPoolGen));

	JavaClass superClazz = null;
	
	try {
	    superClazz = Repository.lookupClass(obj.getReferenceType(constantPoolGen).toString());
	} catch (ClassNotFoundException e) {
	    throw new RuntimeException(e);
	}

	ClassAnalyzer superclassAnalyzer = new ClassAnalyzer(superClazz);
	Method superCtor = superclassAnalyzer.getConstructor(obj.getArgumentTypes(constantPoolGen));

	CtorAnalyzer superCtorAnalyzer = new CtorAnalyzer(superCtor);
	doesEscape = superCtorAnalyzer.doesThisReferenceEscape(stack);
	
	
	if ( !obj.getReturnType(constantPoolGen).equals(BasicType.VOID) ) {
	    stack.push(superCtorAnalyzer.getResult());
	}
	
    }
    
    
    
    @Override public void visitINVOKESTATIC(INVOKESTATIC obj) {
	notImplementedYet(obj);
    }
    
    @Override public void visitINVOKEVIRTUAL(INVOKEVIRTUAL obj) {
	notImplementedYet(obj);
    }
    
    @Override public void visitINSTANCEOF(INSTANCEOF obj) {
	notImplementedYet(obj);
    }
    
    @Override public void visitLDC(LDC obj) {
	notImplementedYet(obj);
/*
	System.out.println(obj.toString(false));
	doesEscape = ThreeValueBoolean.no;	
	
	if ( obj.getType(constantPoolGen).equals(Type.INT) ) {
	    frame.pushStack(Entry.someInt);
	    return;
	}
	
	if ( obj.getType(constantPoolGen).equals(Type.FLOAT) ) {
	    frame.pushStack(Entry.someFloat);
	    return;
	}
	
	frame.pushStack(Entry.someReference);
	*/
    }
    
    @Override public void visitLDC2_W(LDC2_W obj) {
	notImplementedYet(obj);
/*
	System.out.println(obj.toString(false));
	doesEscape = ThreeValueBoolean.no;	
	
	if ( obj.getType(constantPoolGen).equals(Type.LONG) ) {
	    frame.pushStack(Entry.someLong);
	    return;
	}
	
	if ( obj.getType(constantPoolGen).equals(Type.DOUBLE) ) {
	    frame.pushStack(Entry.someDouble);
	    return;
	}
	
	throw new AssertionError("LDC2_W loads wrongly typed value");	
	*/
    }
    
    @Override public void visitMULTIANEWARRAY(MULTIANEWARRAY obj) {
	notImplementedYet(obj);
    }
    
    @Override public void visitNEW(NEW obj) {
	System.out.println(obj.toString(false));
	stack.push(Entry.someReference);
	doesEscape = ThreeValueBoolean.no;	
    }
    
    //-----------------------------------------------------------------
    @Override public void visitDCMPG(DCMPG obj) {
	notImplementedYet(obj);
    }
    
    //-----------------------------------------------------------------
    @Override public void visitDCMPL(DCMPL obj) {
	notImplementedYet(obj);
    }
    
    //-----------------------------------------------------------------
    @Override public void visitDCONST(DCONST obj) {
	notImplementedYet(obj);
    }
    
    //-----------------------------------------------------------------
    @Override public void visitFCMPG(FCMPG obj) {
	notImplementedYet(obj);
    }
    
    //-----------------------------------------------------------------
    @Override public void visitFCMPL(FCMPL obj) {
	notImplementedYet(obj);
    }
    
    //-----------------------------------------------------------------
    @Override public void visitFCONST(FCONST obj)  {
	notImplementedYet(obj);
    }
    
    //-----------------------------------------------------------------
    @Override public void visitICONST(ICONST obj) {
	System.out.println(obj.toString(false));
	stack.push(Entry.someInt);
	doesEscape = ThreeValueBoolean.no;
    }
    
    //-----------------------------------------------------------------
    @Override public void visitIMPDEP1(IMPDEP1 obj) {
	notImplementedYet(obj);
    }
    
    //-----------------------------------------------------------------
    @Override public void visitIMPDEP2(IMPDEP2 obj) {
	notImplementedYet(obj);
    }
    
    //-----------------------------------------------------------------
    @Override public void visitLCMP(LCMP obj) {
	notImplementedYet(obj);
    }
    
    //-----------------------------------------------------------------
    @Override public void visitLCONST(LCONST obj) {
	notImplementedYet(obj);
    }
    
    //---LocalVariableInstruction--------------------------------------
    
    @Override public void visitIINC(IINC obj) {
	notImplementedYet(obj);
    }
    
    @Override public void visitLoadInstruction(LoadInstruction obj) {
	System.out.println(obj.toString(false));
	stack.push(localVars.getForIndex(obj.getIndex()));
	doesEscape = ThreeValueBoolean.no;
   }   
    
    @Override public void visitStoreInstruction(StoreInstruction obj) {
	System.out.println(obj.toString(false));
	localVars.setForIndex(obj.getIndex(), stack.pop());
	doesEscape = ThreeValueBoolean.no;
    } 
            
    //-----------------------------------------------------------------
    @Override public void visitMONITORENTER(MONITORENTER obj) {
	System.out.println("MONITORENTER " + ": No Escape");
	stack.pop();
	doesEscape = ThreeValueBoolean.no;
    }
    
    //-----------------------------------------------------------------
    @Override public void visitMONITOREXIT(MONITOREXIT obj)  {
	System.out.println("MONITOREXIT " + ": No Escape");
	stack.pop();
	doesEscape = ThreeValueBoolean.no;
    }
    
    //-----------------------------------------------------------------
    @Override public void visitNEWARRAY(NEWARRAY obj) {
	notImplementedYet(obj);
    }
    
    //-----------------------------------------------------------------
    @Override public void visitNOP(NOP obj) {
	notImplementedYet(obj);
    }
    
    //-----------------------------------------------------------------
    @Override public void visitRET(RET obj) {
	notImplementedYet(obj);
    }
    
    //-----------------------------------------------------------------
    @Override public void visitReturnInstruction(ReturnInstruction obj) {
	System.out.println("ReturnInstruction");
	doesEscape = ThreeValueBoolean.no;
    }
    
    //-----------------------------------------------------------------
    @Override public void visitSIPUSH(SIPUSH obj)  {
	System.out.println("sipush " + obj.getValue());
	stack.push(Entry.someShort);
	doesEscape = ThreeValueBoolean.no;
    }
    
    //---StackInstruction----------------------------------------------
    @Override public void visitDUP(DUP obj)  {
	notImplementedYet(obj);
/*
	System.out.println(obj.toString(false));
	Entry entry = frame.popStack();
	frame.pushStack(entry);
	frame.pushStack(entry);
	doesEscape = ThreeValueBoolean.no;
	*/
    }
    
    @Override public void visitDUP_X1(DUP_X1 obj)  {
	notImplementedYet(obj);
    }
    
    @Override public void visitDUP_X2(DUP_X2 obj)  {
	notImplementedYet(obj);
    }
    
    @Override public void visitDUP2(DUP2 obj)  {
	notImplementedYet(obj);
    }
    
    @Override public void visitDUP2_X1(DUP2_X1 obj)  {
	notImplementedYet(obj);
    }
    
    @Override public void visitDUP2_X2(DUP2_X2 obj)  {
	notImplementedYet(obj);
    }
    
    @Override public void visitPOP(POP obj)  {
	notImplementedYet(obj);
    }
    
    @Override public void visitPOP2(POP2 obj)  {
	notImplementedYet(obj);
    }
    
    @Override public void visitSWAP(SWAP obj)  {
	notImplementedYet(obj);
    }
    //-----------------------------------------------------------------
                  
}
