package de.htwg_konstanz.in.jca.analyzer.stateUnmodFieldsModify;

import java.util.ArrayList;

import org.apache.bcel.generic.ConstantPoolGen;
import org.apache.bcel.generic.InstructionHandle;
import org.apache.bcel.generic.MethodGen;

import de.htwg_konstanz.in.jca.Frame;
import de.htwg_konstanz.in.jca.analyzer.BaseInstructionsAnalysisVisitor;
import de.htwg_konstanz.in.jca.analyzer.BaseMethodAnalyzer;
import edu.umd.cs.findbugs.ba.ClassContext;

public class StateUnmodFieldsModifyMethodAnalyzer extends BaseMethodAnalyzer {

	public StateUnmodFieldsModifyMethodAnalyzer(ClassContext classContext,
			MethodGen methodGen) {
		super(classContext, methodGen);
	}

	public StateUnmodFieldsModifyMethodAnalyzer(ClassContext classContext,
			MethodGen methodGen,
			ArrayList<AlreadyVisitedMethod> alreadyVisitedMethods, int depth) {
		super(classContext, methodGen, alreadyVisitedMethods, depth);
	}

	protected BaseInstructionsAnalysisVisitor getInstructionAnalysisVisitor(
			Frame frame, InstructionHandle instructionHandle) {
		return new StateUnmodFieldsModifyAnalysisVisitor(classContext, method,
				frame, new ConstantPoolGen(method.getConstantPool()),
				instructionHandle, exceptionHandlers, alreadyVisitedMethods,
				depth);
	}

	public void analyze() {
		System.err.println("analyze not implemented yet!");
		// TODO remove comment add specific "field" marker to all references
		// Stack<SlotOld> callerStack = new Stack<SlotOld>();
		//
		// // push this + args onto the stack
		// callerStack.push(SlotOld.thisReference);
		//
		// Type[] argTypes = method.getArgumentTypes();
		//
		// for (Type argType : argTypes) {
		// DataType dataType = DataType.getDataType(argType);
		// for (int i = 0; i < dataType.getNumSlots(); i++) {
		// callerStack.push(SlotOld.getDefaultInstance(dataType));
		// }
		//
		// }
		// analyze(callerStack);
	}
}
