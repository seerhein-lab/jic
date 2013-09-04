package de.seerhein_lab.jca.analyzer.noMutators;

import java.util.Set;
import java.util.Stack;

import org.apache.bcel.classfile.Method;
import org.apache.bcel.generic.ConstantPoolGen;
import org.apache.bcel.generic.InstructionHandle;
import org.apache.bcel.generic.MethodGen;
import org.apache.bcel.generic.Type;

import de.seerhein_lab.jca.Frame;
import de.seerhein_lab.jca.Pair;
import de.seerhein_lab.jca.analyzer.BaseInstructionsAnalysisVisitor;
import de.seerhein_lab.jca.analyzer.BaseMethodAnalyzer;
import de.seerhein_lab.jca.heap.Heap;
import de.seerhein_lab.jca.slot.ReferenceSlot;
import de.seerhein_lab.jca.slot.Slot;
import edu.umd.cs.findbugs.ba.ClassContext;

public class FieldsNotModifiedMethodAnalyzer extends BaseMethodAnalyzer {

	private Set<Heap> heaps;

	public FieldsNotModifiedMethodAnalyzer(ClassContext classContext,
			MethodGen methodGen, Set<Heap> heaps) {
		super(classContext, methodGen);
		this.heaps = heaps;
	}

	public FieldsNotModifiedMethodAnalyzer(ClassContext classContext,
			MethodGen methodGen,
			Set<Pair<Method, Slot[]>> alreadyVisitedMethods, int depth) {
		super(classContext, methodGen, alreadyVisitedMethods, depth);
	}

	protected BaseInstructionsAnalysisVisitor getInstructionAnalysisVisitor(
			Frame frame, InstructionHandle instructionHandle) {
		return new FieldsNotModifiedAnalysisVisitor(classContext, method,
				frame, new ConstantPoolGen(method.getConstantPool()),
				instructionHandle, exceptionHandlers, alreadyVisitedMethods,
				depth);
	}

	@Override
	public void analyze() {
		for (Heap callerHeap : heaps) {
			Stack<Slot> callerStack = new Stack<Slot>();

			Slot externalReference = ReferenceSlot.createNewInstance(
					callerHeap.getExternalObject());

			// push args + this (if not static) onto the stack
			if (!method.isStatic()) {
				Slot thisReference = ReferenceSlot.createNewInstance(
						callerHeap.getThisInstance());
				callerStack.push(thisReference);
			}

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

			Frame callerFrame = new Frame(callerStack, callerHeap);

			analyze(callerFrame);
		}
	}
}