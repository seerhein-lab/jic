package de.seerhein_lab.jca.analyzer.noMutators;

import java.util.Set;

import org.apache.bcel.classfile.Method;
import org.apache.bcel.generic.ConstantPoolGen;
import org.apache.bcel.generic.MethodGen;

import de.seerhein_lab.jca.Pair;
import de.seerhein_lab.jca.analyzer.BaseInstructionsVisitor;
import de.seerhein_lab.jca.analyzer.BaseMethodAnalyzer;
import de.seerhein_lab.jca.slot.Slot;
import de.seerhein_lab.jca.vm.Frame;
import de.seerhein_lab.jca.vm.Heap;
import de.seerhein_lab.jca.vm.PC;
import edu.umd.cs.findbugs.ba.ClassContext;

public class NoMutatorsAnalyzer extends BaseMethodAnalyzer {

	private Heap heap;

	public NoMutatorsAnalyzer(ClassContext classContext,
			MethodGen methodGen, Heap heap) {
		super(classContext, methodGen);
		this.heap = heap;
	}

	public NoMutatorsAnalyzer(ClassContext classContext,
			MethodGen methodGen,
			Set<Pair<Method, Slot[]>> alreadyVisitedMethods, int depth) {
		super(classContext, methodGen, alreadyVisitedMethods, depth);
	}

	protected BaseInstructionsVisitor getInstructionVisitor(Frame frame,
			Heap heap, PC pc) {
		return new NoMutatorsVisitor(classContext, method, frame, heap,
				new ConstantPoolGen(method.getConstantPool()),
				pc.getCurrentInstruction(), exceptionHandlers,
				alreadyVisitedMethods, depth);
	}

	@Override
	public Heap getHeap() {
		return heap;
	}
}