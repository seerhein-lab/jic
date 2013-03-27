package de.htwg_konstanz.in.jca.analyzer;

import java.util.ArrayList;
import java.util.Stack;

import org.apache.bcel.generic.ConstantPoolGen;
import org.apache.bcel.generic.InstructionHandle;
import org.apache.bcel.generic.MethodGen;
import org.apache.bcel.generic.Type;

import de.htwg_konstanz.in.jca.DataType;
import de.htwg_konstanz.in.jca.Frame;
import de.htwg_konstanz.in.jca.Slot;

import edu.umd.cs.findbugs.ba.ClassContext;

public class PropConMethodAnalyzer extends BaseMethodAnalyzer {

	public PropConMethodAnalyzer(ClassContext classContext, MethodGen methodGen) {
		super(classContext, methodGen);
	}

	public PropConMethodAnalyzer(ClassContext classContext,
			MethodGen methodGen,
			ArrayList<AlreadyVisitedMethod> alreadyVisitedMethods, int depth) {
		super(classContext, methodGen, alreadyVisitedMethods, depth);
	}

	protected BaseInstructionsAnalysisVisitor getInstructionAnalysisVisitor(
			Frame frame, InstructionHandle instructionHandle) {
		return new PropConInstructionsAnalysisVisitor(classContext, method,
				frame, new ConstantPoolGen(method.getConstantPool()),
				instructionHandle, exceptionHandlers, alreadyVisitedMethods,
				depth);
	}

	@Override
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
}
