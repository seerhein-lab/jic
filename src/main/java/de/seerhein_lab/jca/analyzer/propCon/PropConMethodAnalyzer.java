package de.seerhein_lab.jca.analyzer.propCon;

import java.util.Set;
import java.util.Stack;

import org.apache.bcel.classfile.Method;
import org.apache.bcel.generic.ConstantPoolGen;
import org.apache.bcel.generic.InstructionHandle;
import org.apache.bcel.generic.MethodGen;
import org.apache.bcel.generic.Type;

import de.seerhein_lab.jca.Pair;
import de.seerhein_lab.jca.analyzer.BaseInstructionsVisitor;
import de.seerhein_lab.jca.analyzer.BaseMethodAnalyzer;
import de.seerhein_lab.jca.slot.ReferenceSlot;
import de.seerhein_lab.jca.slot.Slot;
import de.seerhein_lab.jca.vm.Frame;
import de.seerhein_lab.jca.vm.Heap;
import de.seerhein_lab.jca.vm.OpStack;
import de.seerhein_lab.jca.vm.PC;
import edu.umd.cs.findbugs.ba.ClassContext;

public class PropConMethodAnalyzer extends BaseMethodAnalyzer {

	public PropConMethodAnalyzer(ClassContext classContext, MethodGen methodGen) {
		super(classContext, methodGen);
	}

	public PropConMethodAnalyzer(ClassContext classContext,
			MethodGen methodGen,
			Set<Pair<Method, Slot[]>> alreadyVisitedMethods, int depth) {
		super(classContext, methodGen, alreadyVisitedMethods, depth);
	}

	protected BaseInstructionsVisitor getInstructionVisitor(
			Frame frame, Heap heap, PC pc) {
		return new PropConInstructionsVisitor(classContext, method,
				frame, heap, new ConstantPoolGen(method.getConstantPool()),
				pc.getCurrentInstruction(), exceptionHandlers, alreadyVisitedMethods,
				depth);
	}

	@Override
	public void analyze() {
		OpStack callerStack = new OpStack();
		Heap callerHeap = new Heap();

		ReferenceSlot thisReference = ReferenceSlot.createNewInstance(
				callerHeap.getThisInstance());
		ReferenceSlot externalReference = ReferenceSlot.createNewInstance(
				callerHeap.getExternalObject());

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
		analyze(callerStack, callerHeap);
	}
}
