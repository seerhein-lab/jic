package de.seerhein_lab.jca.analyzer.propConAnalyzer;

import java.util.Set;
import java.util.Stack;

import org.apache.bcel.generic.ConstantPoolGen;
import org.apache.bcel.generic.InstructionHandle;
import org.apache.bcel.generic.MethodGen;
import org.apache.bcel.generic.Type;

import de.seerhein_lab.jca.Frame;
import de.seerhein_lab.jca.analyzer.BaseInstructionsAnalysisVisitor;
import de.seerhein_lab.jca.analyzer.BaseMethodAnalyzer;
import de.seerhein_lab.jca.heap.Heap;
import de.seerhein_lab.jca.slot.ReferenceSlot;
import de.seerhein_lab.jca.slot.Slot;
import edu.umd.cs.findbugs.ba.ClassContext;

public class PropConMethodAnalyzer extends BaseMethodAnalyzer {

	public PropConMethodAnalyzer(ClassContext classContext, MethodGen methodGen) {
		super(classContext, methodGen);
	}

	public PropConMethodAnalyzer(ClassContext classContext,
			MethodGen methodGen,
			Set<AlreadyVisitedMethod> alreadyVisitedMethods, int depth) {
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
		Heap callerHeap = new Heap();

		ReferenceSlot thisReference = new ReferenceSlot(callerHeap.getThisID());
		ReferenceSlot externalReference = new ReferenceSlot(
				callerHeap.getExternalID());

		// push this + args onto the stack
		callerStack.push(thisReference);

		Type[] argTypes = method.getArgumentTypes();

		for (Type argType : argTypes) {
			Slot argument = Slot.getDefaultSlotInstance(argType);
			if (argument instanceof ReferenceSlot) {
				argument = externalReference;
			}
			for (int i = 0; i < argument.getNumSlots(); i++) {
				callerStack.push(argument);
			}
		}

		Frame callerFrame = new Frame(0, callerStack, 0, callerHeap);

		analyze(callerFrame);
	}
}
